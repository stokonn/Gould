package com.opus.piano.data.community

import java.util.ArrayDeque

enum class CommunityAvailability { DISABLED, SYNTHETIC_FIXTURE }
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
