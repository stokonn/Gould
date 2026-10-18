package com.opus.piano.data.catalogue.providers

import java.security.MessageDigest
import java.net.URLEncoder
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/** All inputs are catalogue actions, never personal objects. No adapter receives a journal. */
data class ProviderRequest(val url: String, val provider: String, val kind: String, val externalId: String? = null, val headers:Map<String,String> = emptyMap())
data class ProviderResponse(val code: Int, val body: ByteArray, val headers: Map<String,String> = emptyMap())
class BlockedRightsException:IllegalArgumentException("Unverified content rights")
enum class SourceState { FRESH, CACHED, STALE, MISSING, UNAVAILABLE, RATE_LIMITED, INVALID, BLOCKED_RIGHTS }
data class FieldValue(val field: String, val value: String, val language: String = "und")
data class ProviderRecord(val providerKind: String, val externalId: String, val kind: String, val title: String,
    val fields: List<FieldValue> = emptyList(), val aliases: List<Pair<String,String>> = emptyList(),
    val subtype: String = "", val credits: List<ProviderCredit> = emptyList(),
    val classificationPending:Boolean=false, val relationships:List<ProviderEdge> = emptyList())
data class ProviderReference(val providerKind:String,val externalId:String,val kind:String,val title:String,val subtype:String="")
data class ProviderEdge(val type:String,val from:ProviderReference,val to:ProviderReference,val position:Int=0,val medium:Int=1,val role:String="")
data class ProviderCredit(val id: String, val name: String, val role: String)
data class ProviderCandidate(val id:String,val title:String,val description:String)
data class ProviderResult(val request: ProviderRequest, val state: SourceState, val records: List<ProviderRecord> = emptyList(),
    val revision: String = "", val hash: String = "", val observedAt: String = Instant.now().toString(),
    val retryAfterMillis: Long = 0, val error: String? = null, val license: String = "CC0-1.0",
    val licenseUrl: String = "https://creativecommons.org/publicdomain/zero/1.0/", val candidates:List<ProviderCandidate> = emptyList(), val supplement:Map<String,Any?> = emptyMap(), val refreshState:SourceState?=null)
interface ProviderAdapter {
    val id: String
    val intervalMillis: Long
    fun search(query: String, category: String, language: String, offset: Int = 0): List<ProviderRequest>
    fun parse(request: ProviderRequest, root: Map<String,Any?>): List<ProviderRecord>
}
internal fun enc(value: String) = URLEncoder.encode(value,"UTF-8").replace("+","%20")
internal fun Map<String,Any?>.text(key:String):String = (this[key] as? String)?.take(4096).orEmpty()
internal fun Map<String,Any?>.obj(key:String):Map<String,Any?> = (this[key] as? Map<*,*>)?.entries?.mapNotNull { (k,v)->(k as? String)?.let {it to v} }?.toMap().orEmpty()
internal fun Map<String,Any?>.rows(key:String):List<Map<String,Any?>> = (this[key] as? List<*>)?.take(100)?.mapNotNull { value ->
    (value as? Map<*,*>)?.entries?.mapNotNull { (k,v)->(k as? String)?.let {it to v} }?.toMap()
}.orEmpty()
internal fun sha(bytes:ByteArray)=MessageDigest.getInstance("SHA-256").digest(bytes).joinToString(""){"%02x".format(it)}
object ProviderErrors {
    fun retryAfter(value:String?, now:Long):Long = value?.toLongOrNull()?.takeIf {it>=0}?.let { if(it>Long.MAX_VALUE/1000) Long.MAX_VALUE else it*1000 }
        ?: runCatching { (ZonedDateTime.parse(value,DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli()-now).coerceAtLeast(0) }.getOrDefault(0)
    fun classify(code:Int, root:Map<String,Any?>):SourceState? {
        val error=root.obj("error").text("code")
        return when {
            code==429 || code==503 || error in setOf("maxlag","ratelimited","readonly") -> SourceState.RATE_LIMITED
            code==404 || root.containsKey("missing") -> SourceState.MISSING
            code !in 200..299 -> SourceState.UNAVAILABLE
            root.containsKey("error") || root["errors"]!=null -> SourceState.INVALID
            else -> null
        }
    }
}
