package com.opus.piano.data.community

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import java.util.UUID

/** Sensitive credential is deliberately not a data class and has no diagnostic representation. */
class CommunitySession(val principalId: String, internal val credential: String, val expiresAt: Long, val canSubmit: Boolean) {
    init {require(UUID.fromString(principalId).toString()==principalId);require(credential.isNotBlank())}
    override fun toString()="CommunitySession(redacted)"
}
interface CommunityCredentials { fun current(): CommunitySession?;fun clear() }
data class OverlaySnapshot(val page: OverlayPage, val synchronizedAt: Long, val stale: Boolean, val synthetic: Boolean)
sealed interface CommunityAttempt {
    data class Failed(val failure: CommunityFailure): CommunityAttempt
    data class SyntheticReceipt(val submissionId: String, val status: String): CommunityAttempt
}
interface CommunityRepository {
    val availability: CommunityAvailability
    suspend fun drafts(): List<CommunityDraft>
    suspend fun create(payload: ContributionPayload): CommunityDraft
    suspend fun edit(id: String, expectedVersion: Long, payload: ContributionPayload): CommunityDraft
    suspend fun copyForNewRequest(id: String): CommunityDraft
    suspend fun preview(id: String, command: ContributionCommand=ContributionCommand(ContributionOperation.CREATE)): String
    suspend fun submit(id: String, command: ContributionCommand=ContributionCommand(ContributionOperation.CREATE)): CommunityAttempt
    suspend fun overlay(): OverlaySnapshot?
    suspend fun changes(cursor: String?=null): ChangePage?
    suspend fun signOut()
}
class LocalCommunityRepository(
    private val store: CommunityDraftStore,
    private val credentials: CommunityCredentials,
    private val transport: CommunityTransport=DisabledCommunityTransport,
    private val cache: CommunityOverlayCache,
    private val clock: ()->Long=System::currentTimeMillis,
    private val requestTimeoutMillis: Long=10000
): CommunityRepository {
    init {require(requestTimeoutMillis in 1..60000)}
    private val mutex=Mutex()
    override val availability get()=transport.availability
    private fun session()=credentials.current()?.takeIf {it.expiresAt>clock()}
    private fun owner()=session()?.let {CommunityOwner("principal:"+it.principalId)}?:store.guest()
    private fun get(id: String)=requireNotNull(store.get(owner(),id)) {"Draft unavailable for this account"}
    override suspend fun drafts()=withContext(Dispatchers.IO) {mutex.withLock {store.list(owner())}}
    override suspend fun create(payload: ContributionPayload)=withContext(Dispatchers.IO) {mutex.withLock {store.save(CommunityDraft(UUID.randomUUID().toString(),owner(),0,payload),0)}}
    override suspend fun edit(id: String,expectedVersion: Long,payload: ContributionPayload)=withContext(Dispatchers.IO) {mutex.withLock {
        val d=get(id);check(d.pending==null&&d.receipt==null) {"Request frozen; keep original and explicitly copy after review"};store.save(d.copy(payload=payload),expectedVersion)
    }}
    override suspend fun copyForNewRequest(id: String)=withContext(Dispatchers.IO) {mutex.withLock {
        val d=get(id);store.save(CommunityDraft(UUID.randomUUID().toString(),d.owner,0,d.payload),0)
    }}
    override suspend fun preview(id: String,command: ContributionCommand)=withContext(Dispatchers.IO) {mutex.withLock {
        val d=get(id);d.pending?.body?:CommunityCodec.request(d.payload,command,d.id)
    }}
    override suspend fun signOut()=withContext(Dispatchers.IO) {credentials.clear()} // Does not touch any draft or personal database.
    override suspend fun submit(id: String,command: ContributionCommand): CommunityAttempt=withContext(Dispatchers.IO) {mutex.withLock {
        fun failed(p: CommunityProblem)=CommunityAttempt.Failed(CommunityFailure(p))
        if(availability==CommunityAvailability.DISABLED)return@withLock failed(CommunityProblem.DISABLED)
        val auth=session()?:return@withLock failed(CommunityProblem.UNAUTHENTICATED)
        if(!auth.canSubmit)return@withLock failed(CommunityProblem.FORBIDDEN)
        var draft=store.get(CommunityOwner("principal:"+auth.principalId),id)?:return@withLock failed(CommunityProblem.OWNERSHIP)
        if(draft.receipt!=null)return@withLock failed(CommunityProblem.FROZEN)
        val existing=draft.pending
        if(existing?.outcome in setOf(CommunityProblem.VALIDATION,CommunityProblem.CONFLICT,CommunityProblem.IDEMPOTENCY_MISMATCH,CommunityProblem.NOT_FOUND))return@withLock failed(existing!!.outcome!!)
        if(existing?.outcome==CommunityProblem.RATE_LIMITED && existing.retryAtMillis==null)return@withLock failed(CommunityProblem.RATE_LIMITED)
        if(existing?.retryAtMillis!=null && clock()<existing.retryAtMillis)return@withLock CommunityAttempt.Failed(CommunityFailure(CommunityProblem.RATE_LIMITED,existing.retryAtMillis))
        val key=existing?.key?:draft.id
        val body=try {CommunityCodec.request(draft.payload,command,key)} catch(_: IllegalArgumentException) {return@withLock failed(CommunityProblem.VALIDATION)}
        if(existing!=null&&(existing.body!=body||existing.operation!=command.operation))return@withLock failed(CommunityProblem.FROZEN)
        // Persist exact immutable wire body/key BEFORE any attempt, including uncertain failures.
        try {draft=store.save(draft.copy(pending=existing?:PendingContribution(command.operation,body,key)),draft.version)}catch(_: Exception){return@withLock failed(CommunityProblem.STORAGE)}
        val result=try {withTimeout(requestTimeoutMillis) {transport.invoke(command.operation.wire,body,auth.credential)}}catch(_: TimeoutCancellationException){TransportResult.Unreachable}catch(e: CancellationException){throw e}catch(_: Exception){TransportResult.Unreachable}
        // Account change/sign-out while suspended transport was in flight cannot apply a receipt.
        if(session()!==auth)return@withLock failed(CommunityProblem.UNAUTHENTICATED)
        val response=result as? TransportResult.Response
        if(response!=null&&response.status in 200..299) {
            val receipt=runCatching {val o=JSONObject(response.body);val rid=o.getString("id");require(UUID.fromString(rid).toString()==rid);rid to o.getString("status").also {require(it in setOf("submitted","in_review","withdrawn"))}}.getOrNull()
            if(receipt!=null) {
                try {store.save(draft.copy(receipt=receipt.first),draft.version)}catch(_: Exception){return@withLock failed(CommunityProblem.STORAGE)}
                return@withLock CommunityAttempt.SyntheticReceipt(receipt.first,receipt.second)
            }
        }
        val error=if(response==null)CommunityFailure(CommunityProblem.OFFLINE) else CommunityErrors.map(response.status,response.retryAfter,clock())
        try {store.save(draft.copy(pending=draft.pending!!.copy(outcome=error.problem,retryAtMillis=error.retryAtMillis)),draft.version)}catch(_: Exception){return@withLock failed(CommunityProblem.STORAGE)}
        CommunityAttempt.Failed(error)
    }}
    override suspend fun overlay(): OverlaySnapshot?=withContext(Dispatchers.IO) {
        if(availability==CommunityAvailability.DISABLED)return@withContext cache.read()?.copy(stale=true)
        val r=try {withTimeout(requestTimeoutMillis) {transport.invoke("catalogue.overlay","{\"schemaVersion\":1}",null)}}catch(_: TimeoutCancellationException){TransportResult.Unreachable}catch(e: CancellationException){throw e}catch(_: Exception){TransportResult.Unreachable}
        val page=(r as? TransportResult.Response)?.takeIf {it.status==200}?.let {runCatching {CommunityCodec.overlay(it.body)}.getOrNull()}
        if(page==null)cache.read()?.copy(stale=true) else OverlaySnapshot(page,clock(),false,true).also {cache.write(it)}
    }
    override suspend fun changes(cursor: String?): ChangePage?=withContext(Dispatchers.IO) {
        if(availability==CommunityAvailability.DISABLED)return@withContext null
        val request=JSONObject().put("schemaVersion",1).apply {cursor?.let {put("cursor",it)}}
        val r=try {withTimeout(requestTimeoutMillis) {transport.invoke("catalogue.changes",request.toString(),null)}}catch(_: TimeoutCancellationException){TransportResult.Unreachable}catch(e: CancellationException){throw e}catch(_: Exception){TransportResult.Unreachable}
        (r as? TransportResult.Response)?.takeIf {it.status==200}?.let {runCatching {CommunityCodec.changes(it.body)}.getOrNull()}
    }
}
