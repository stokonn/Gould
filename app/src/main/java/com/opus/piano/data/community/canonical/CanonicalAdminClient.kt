package com.opus.piano.data.community.canonical

import com.opus.piano.data.community.CommunityErrors
import com.opus.piano.data.community.CommunityFailure
import com.opus.piano.data.community.CommunityProblem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.UUID

/** Injected boundary only. No URL, socket implementation, credential persistence or app wiring. */
fun interface CanonicalAdminTransport {
    suspend fun exchange(method: String, path: String, body: String?, accessToken: String?): AdminResponse
}
class AdminResponse(val status: Int, val body: String, val retryAfter: String? = null) {
    override fun toString() = "AdminResponse(status=$status)"
}
class AdminClientException(val failure: CommunityFailure) : Exception(failure.problem.name)
class AdminReview internal constructor(internal val epoch: Long, val detail: AdminDocument, val comparison: AdminDocument)
data class AdminLogout(val locallyCleared: Boolean, val revocationConfirmed: Boolean)

/** Canonical local/provisional v1 client. Production activation and public freeze remain separate. */
class CanonicalAdminClient(
    private val contract: AdminContract,
    private val transport: CanonicalAdminTransport,
    private val now: () -> Long = System::currentTimeMillis,
) {
    private class Tokens(val principal: String, val session: String, val access: String, val refresh: String)
    private class Pending(var epoch: Long, val path: String, val body: String, val dto: String, val status: Int, val created: Long)
    private val guard = Any()
    private var epoch = 0L
    private var tokens: Tokens? = null
    private var pending: Pending? = null
    private var refreshing: CompletableDeferred<Unit>? = null
    private var deciding = false
    val authenticated: Boolean get() = synchronized(guard) { tokens != null }
    val hasUncertainDecision: Boolean get() = synchronized(guard) { pending != null }

    fun clear() = synchronized(guard) { clearLocked() }
    private fun clearLocked() { epoch++; tokens = null; pending = null; refreshing = null }
    private fun denied(problem: CommunityProblem): Nothing = throw AdminClientException(CommunityFailure(problem))
    private fun snapshot(): Pair<Long, Tokens> = synchronized(guard) { epoch to (tokens ?: denied(CommunityProblem.UNAUTHENTICATED)) }
    private fun checkEpoch(expected: Long) = synchronized(guard) { if (epoch != expected) denied(CommunityProblem.OWNERSHIP) }
    private fun readTokens(doc: AdminDocument): Tokens = doc.json().let {
        Tokens(it.getString("principalId"), it.getString("sessionId"), it.getString("accessToken"), it.getString("refreshToken"))
    }
    private suspend fun parse(dto: String, raw: String): AdminDocument = withContext(Dispatchers.Default) {
        try { contract.decode(dto, raw) } catch (_: Exception) { denied(CommunityProblem.UNAVAILABLE) }
    }
    private fun encode(dto: String, obj: JSONObject) = contract.decode(dto, obj.toString(), request = true).json().toString()
    private fun request() = JSONObject().put("schemaVersion", 1)

    suspend fun challenge(): AdminDocument = publicCall("/v1/session/challenge", encode("EmptyRequest", request()), "Challenge", 201)
    suspend fun login(challenge: String, credential: String) {
        val expected = synchronized(guard) { clearLocked(); epoch }
        val result = publicCall("/v1/session", encode("LoginRequest", request().put("challenge", challenge).put("credential", credential)), "Tokens", 201)
        synchronized(guard) { checkEpoch(expected); tokens = readTokens(result) }
    }
    private suspend fun exchange(method: String, path: String, body: String?, access: String?): AdminResponse = try {
        transport.exchange(method, path, body, access)
    } catch (cancelled: CancellationException) { throw cancelled }
    catch (_: java.io.IOException) { denied(CommunityProblem.OFFLINE) }
    catch (_: Exception) { denied(CommunityProblem.UNAVAILABLE) }

    private suspend fun publicCall(path: String, body: String, dto: String, status: Int): AdminDocument {
        val response = exchange("POST", path, body, null)
        if (response.status != status) throw AdminClientException(CommunityErrors.map(response.status, response.retryAfter, now()))
        return parse(dto, response.body)
    }
    private suspend fun checked(method: String, path: String, body: String? = null): AdminResponse {
        val (expected, identity) = snapshot()
        val response = exchange(method, path, body, identity.access)
        synchronized(guard) {
            checkEpoch(expected)
            if (response.status == 401) clearLocked()
            if (response.status == 403) { epoch++; pending = null; refreshing = null }
        }
        if (response.status !in 200..299) throw AdminClientException(CommunityErrors.map(response.status, response.retryAfter, now()))
        return response
    }
    private suspend fun read(path: String, dto: String): AdminDocument {
        val expected = snapshot().first
        val response = checked("GET", path)
        if (response.status != 200) denied(CommunityProblem.UNAVAILABLE)
        val doc = parse(dto, response.body)
        checkEpoch(expected)
        return doc
    }
    suspend fun session(): AdminDocument = identityRead("/v1/session", "Session")
    suspend fun capabilities(): AdminDocument = identityRead("/v1/me/capabilities", "Capabilities")
    private suspend fun identityRead(path: String, dto: String): AdminDocument {
        val (expected, identity) = snapshot()
        val doc = read(path, dto)
        synchronized(guard) {
            checkEpoch(expected)
            if (doc.json().getString("principalId") != identity.principal || doc.json().getString("sessionId") != identity.session) {
                clearLocked(); denied(CommunityProblem.OWNERSHIP)
            }
        }
        return doc
    }

    /** One in-flight refresh per identity. A lost rotation response requires new authentication. */
    suspend fun refresh() {
        val expected: Long
        val previous: Tokens
        val operation: CompletableDeferred<Unit>
        val owner: Boolean
        synchronized(guard) {
            val snapshot = snapshot(); expected = snapshot.first; previous = snapshot.second
            val active = refreshing
            owner = active == null; operation = active ?: CompletableDeferred<Unit>().also { refreshing = it }
        }
        if (!owner) { operation.await(); return }
        try {
            val doc = publicCall("/v1/session/refresh", encode("RefreshRequest", request().put("refreshToken", previous.refresh)), "Tokens", 200)
            val fresh = readTokens(doc)
            synchronized(guard) {
                checkEpoch(expected)
                if (fresh.principal != previous.principal || fresh.session == previous.session) denied(CommunityProblem.UNAUTHENTICATED)
                tokens = fresh; epoch++
                // An exact uncertain request remains replayable; old review acknowledgements do not.
                pending?.takeIf { it.epoch == expected }?.epoch = epoch
            }
            operation.complete(Unit)
        } catch (failure: Throwable) {
            synchronized(guard) { if (epoch == expected) clearLocked() }
            operation.completeExceptionally(failure)
            throw failure
        } finally { synchronized(guard) { if (refreshing === operation) refreshing = null } }
    }

    suspend fun logout(): AdminLogout {
        val prior = synchronized(guard) { val old = tokens; clearLocked(); old }
        if (prior == null) return AdminLogout(true, false)
        return try {
            val response = exchange("DELETE", "/v1/session", null, prior.access)
            AdminLogout(true, response.status == 204 && response.body.isEmpty())
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { AdminLogout(true, false) }
    }

    suspend fun queue(filters: Map<String, String> = emptyMap()) = read(query("/v1/admin/queue", filters, setOf("status", "kind", "q", "limit", "cursor")), "QueuePage")
    suspend fun audit(filters: Map<String, String> = emptyMap()): AdminDocument {
        val doc = read(query("/v1/admin/audit", filters, setOf("action", "actorId", "subjectId", "from", "to", "q", "limit", "cursor")), "AuditPage")
        withContext(Dispatchers.Default) {
            try {
                val items = doc.json().getJSONArray("items")
                for (i in 0 until items.length()) AdminAuditVerifier.verifyEvent(items.getJSONObject(i))
            } catch (_: Exception) { denied(CommunityProblem.UNAVAILABLE) }
        }
        return doc
    }

    fun auditWalk(filters: Map<String, String> = emptyMap()): AuditWalk {
        require("cursor" !in filters)
        return AuditWalk(snapshot().first, filters.toMap())
    }
    inner class AuditWalk internal constructor(private val identityEpoch: Long, private val filters: Map<String, String>) {
        private val verifier = AdminAuditVerifier()
        private var cursor: String? = null
        private var complete = false
        private var inFlight = false
        suspend fun next(): AdminDocument {
            synchronized(guard) { checkEpoch(identityEpoch); require(!complete && !inFlight); inFlight = true }
            try {
                val doc = audit(filters + (cursor?.let { mapOf("cursor" to it) } ?: emptyMap()))
                synchronized(guard) {
                    checkEpoch(identityEpoch)
                    try { verifier.accept(doc) } catch (_: Exception) { denied(CommunityProblem.UNAVAILABLE) }
                    cursor = doc.json().get("nextCursor").let { if (it === JSONObject.NULL) null else it as String }
                    complete = cursor == null
                }
                return doc
            } finally { synchronized(guard) { inFlight = false } }
        }
    }
    suspend fun statistics(): AdminDocument {
        val expected = snapshot().first
        val r = checked("GET", "/v1/admin/stats")
        if (r.status != 200) denied(CommunityProblem.UNAVAILABLE)
        val state = runCatching { JSONObject(r.body).getString("state") }.getOrNull()
        val doc = parse(if (state == "exact") "Stats" else "StatsUnavailable", r.body)
        checkEpoch(expected)
        return doc
    }
    suspend fun detail(id: String): AdminDocument {
        val doc = read("/v1/admin/submissions/${uuid(id)}", "Detail")
        if (doc.json().getString("submissionId") != id) denied(CommunityProblem.UNAVAILABLE)
        return doc
    }
    suspend fun comparison(entity: String, submission: String): AdminDocument {
        val expected = snapshot().first
        val r = checked("GET", "/v1/admin/entities/${uuid(entity)}/comparison?submissionId=${uuid(submission)}")
        if (r.status != 200) denied(CommunityProblem.UNAVAILABLE)
        val state = runCatching { JSONObject(r.body).getString("state") }.getOrNull()
        val doc = parse(if (state == "available") "Comparison" else "ComparisonUnavailable", r.body)
        checkEpoch(expected)
        return doc
    }
    private fun uuid(value: String): String { val id = UUID.fromString(value); require(id.toString() == value && id.version() in 1..5 && id.variant() == 2); return value }
    private fun query(path: String, values: Map<String, String>, allowed: Set<String>): String {
        require(values.keys.all { it in allowed })
        values.forEach { (key, value) ->
            require(value.toByteArray(Charsets.UTF_8).size <= if (key == "cursor") 4096 else 200)
            require(value.isNotBlank() && Charsets.UTF_8.newEncoder().canEncode(value) && value.none { it < ' ' || it == '\u007f' })
            if (key == "cursor") require(Regex("[A-Za-z0-9_-]+").matches(value))
            if (key in setOf("actorId", "subjectId")) uuid(value)
            if (key in setOf("from", "to")) { require(Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{6}Z").matches(value)); java.time.Instant.parse(value) }
            if (key == "limit") require(value.toInt() in 1..20 && value.toInt().toString() == value)
        }
        if (values["from"] != null && values["to"] != null) require(values.getValue("from") < values.getValue("to"))
        return path + if (values.isEmpty()) "" else values.toSortedMap().entries.joinToString("&", "?") { (key, value) ->
            key + "=" + URLEncoder.encode(value, "UTF-8")
        }
    }

    suspend fun review(id: String): AdminReview {
        val (expected, identity) = snapshot()
        val caps = capabilities().json().getJSONArray("capabilities")
        if ((0 until caps.length()).none { caps.getString(it) == "decide" }) {
            synchronized(guard) { checkEpoch(expected); epoch++; pending = null }
            denied(CommunityProblem.FORBIDDEN)
        }
        val detail = detail(id)
        if (detail.json().getString("authorId") == identity.principal) denied(CommunityProblem.FORBIDDEN)
        val target = detail.json().get("targetEntityId")
        if (target !is String) denied(CommunityProblem.UNAVAILABLE)
        val comparison = comparison(target, id)
        if (comparison.json().getString("submissionId") != id || comparison.json().get("entityId") != target) denied(CommunityProblem.UNAVAILABLE)
        checkEpoch(expected)
        return AdminReview(expected, detail, comparison)
    }

    /** Explicit open is separate from decision. No automatic retry or implicit state transition. */
    suspend fun open(review: AdminReview): AdminDocument {
        checkEpoch(review.epoch)
        require(review.detail.json().getString("status") == "submitted")
        val d = review.detail.json()
        val body = encode("OpenRequest", request().put("expectedEntityRevision", d.getString("expectedEntityRevision"))
            .put("expectedSubmissionRevision", d.getString("submissionRevision")).put("idempotencyKey", UUID.randomUUID().toString()))
        val r = checked("POST", "/v1/admin/submissions/${uuid(d.getString("submissionId"))}/open", body)
        if (r.status != 200) denied(CommunityProblem.UNAVAILABLE)
        val result = parse("OpenResult", r.body)
        checkEpoch(review.epoch)
        if (result.json().getString("submissionId") != d.getString("submissionId")) denied(CommunityProblem.UNAVAILABLE)
        return result
    }

    suspend fun confirm(review: AdminReview, decision: String, reason: String? = null, acknowledged: Boolean = false): AdminDocument {
        require(acknowledged)
        synchronized(guard) { checkEpoch(review.epoch); if (pending != null || deciding) denied(CommunityProblem.CONFLICT) }
        val fresh = review(review.detail.json().getString("submissionId"))
        checkEpoch(review.epoch)
        if (withContext(Dispatchers.Default) { fingerprint(review) != fingerprint(fresh) } || fresh.detail.json().getString("status") != "in_review") denied(CommunityProblem.CONFLICT)
        val d = fresh.detail.json(); val c = fresh.comparison.json()
        if (decision == "approve") {
            if (c.getString("state") != "available" && d.getString("expectedEntityRevision") != "0") denied(CommunityProblem.UNAVAILABLE)
            if (c.getString("state") == "available" && (c.getString("entityRevision") != d.getString("expectedEntityRevision") || c.getString("entityStatus") != "published")) denied(CommunityProblem.CONFLICT)
        }
        val dto = when (decision) { "approve" -> "Approve"; "reject" -> "Reject"; "request_changes" -> "Changes"; else -> throw IllegalArgumentException("Unknown decision") }
        val obj = request().put("expectedEntityRevision", d.getString("expectedEntityRevision"))
            .put("expectedSubmissionRevision", d.getString("submissionRevision")).put("decision", decision).put("idempotencyKey", UUID.randomUUID().toString())
        reason?.let { obj.put("reason", it) }
        val body = encode(dto + "Request", obj)
        synchronized(guard) {
            checkEpoch(review.epoch)
            if (pending != null || deciding) denied(CommunityProblem.CONFLICT)
            pending = Pending(epoch, "/v1/admin/submissions/${uuid(d.getString("submissionId"))}/decisions", body, dto + "Result", if (decision == "approve") 201 else 200, now())
        }
        return retryPending()
    }

    /** Call only following an explicit user action for the identical uncertain request. */
    suspend fun retryPending(): AdminDocument {
        val p = synchronized(guard) {
            val intent = pending ?: denied(CommunityProblem.CONFLICT)
            checkEpoch(intent.epoch)
            if (deciding) denied(CommunityProblem.CONFLICT)
            if (now() < intent.created || now() - intent.created >= 86400000L) { pending = null; denied(CommunityProblem.CONFLICT) }
            deciding = true; intent
        }
        try {
            val caps = capabilities().json().getJSONArray("capabilities")
            if ((0 until caps.length()).none { caps.getString(it) == "decide" }) {
                synchronized(guard) { pending = null; epoch++ }; denied(CommunityProblem.FORBIDDEN)
            }
            synchronized(guard) { checkEpoch(p.epoch); if (pending !== p) denied(CommunityProblem.OWNERSHIP) }
            val response = checked("POST", p.path, p.body)
            if (response.status != p.status) denied(CommunityProblem.UNAVAILABLE)
            val result = parse(p.dto, response.body)
            checkEpoch(p.epoch)
            if (result.json().getString("submissionId") != p.path.substringAfter("submissions/").substringBefore('/')) denied(CommunityProblem.UNAVAILABLE)
            synchronized(guard) { if (pending === p) pending = null }
            return result
        } catch (failure: AdminClientException) {
            if (failure.failure.problem in setOf(CommunityProblem.VALIDATION, CommunityProblem.NOT_FOUND, CommunityProblem.CONFLICT, CommunityProblem.IDEMPOTENCY_MISMATCH)) synchronized(guard) { if (pending === p) pending = null }
            throw failure
        } finally { synchronized(guard) { deciding = false } }
    }

    private fun fingerprint(review: AdminReview): String = canonical(JSONObject().put("detail", review.detail.json().also { it.remove("requestId") })
        .put("comparison", review.comparison.json().also { it.remove("requestId") }))
    private fun canonical(value: Any): String = when (value) {
        is JSONObject -> value.keys().asSequence().toList().sorted().joinToString(",", "{", "}") { JSONObject.quote(it) + ":" + canonical(value.get(it)) }
        is JSONArray -> (0 until value.length()).joinToString(",", "[", "]") { canonical(value.get(it)) }
        is String -> JSONObject.quote(value)
        else -> value.toString()
    }
}
