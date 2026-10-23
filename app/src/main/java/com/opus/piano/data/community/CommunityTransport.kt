package com.opus.piano.data.community

import java.util.ArrayDeque

enum class CommunityAvailability { DISABLED, SYNTHETIC_FIXTURE }
enum class CommunityProblem { DISABLED, OFFLINE, UNAVAILABLE, VALIDATION, UNAUTHENTICATED, FORBIDDEN, NOT_FOUND, CONFLICT, IDEMPOTENCY_MISMATCH, RATE_LIMITED, SERVER, STORAGE, FROZEN, OWNERSHIP }
data class CommunityFailure(val problem: CommunityProblem, val retryAtMillis: Long?=null)
sealed interface TransportResult {
    data class Response(val status: Int, val body: String, val retryAfter: String?=null): TransportResult
    data object Unreachable: TransportResult
}
interface CommunityTransport {
    val availability: CommunityAvailability
    suspend fun invoke(operation: String, body: String, credential: String?): TransportResult
}
/** No URL, HTTP library, connection factory or activation flag exists in production. */
object DisabledCommunityTransport: CommunityTransport {
    override val availability=CommunityAvailability.DISABLED
    override suspend fun invoke(operation: String, body: String, credential: String?)=TransportResult.Unreachable
}
/** Explicitly synthetic transport, never constructed by OpusApplication. */
class FixtureCommunityTransport(responses: List<TransportResult>): CommunityTransport {
    override val availability=CommunityAvailability.SYNTHETIC_FIXTURE
    private val responses=ArrayDeque(responses)
    val calls=mutableListOf<Pair<String,String>>() // Never retains credentials.
    override suspend fun invoke(operation: String, body: String, credential: String?): TransportResult {
        calls.add(operation to body);return if(responses.isEmpty())TransportResult.Unreachable else responses.removeFirst()
    }
}
object CommunityErrors {
    fun map(status: Int, retryAfter: String?, now: Long): CommunityFailure {
        val kind=when(status) {400->CommunityProblem.VALIDATION;401->CommunityProblem.UNAUTHENTICATED;403->CommunityProblem.FORBIDDEN;404->CommunityProblem.NOT_FOUND;409->CommunityProblem.CONFLICT;422->CommunityProblem.IDEMPOTENCY_MISMATCH;429->CommunityProblem.RATE_LIMITED;503->CommunityProblem.UNAVAILABLE;else->CommunityProblem.SERVER}
        val retry=if(status==429) {
            val seconds=retryAfter?.toLongOrNull()?.takeIf {it>=0}
            if(seconds!=null) runCatching {Math.addExact(now,Math.multiplyExact(seconds,1000))}.getOrNull()
            else runCatching {java.time.ZonedDateTime.parse(retryAfter,java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli().coerceAtLeast(now)}.getOrNull()
        } else null
        return CommunityFailure(kind,retry)
    }
}
