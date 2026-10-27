package com.opus.piano.data.community.canonical

import com.opus.piano.admin.AdminTransport
import com.opus.piano.admin.DisabledAdminTransport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.net.URI
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction

/** Existing Android transport seam, with an explicit canonical schema. Production
 * uses DisabledAdminTransport; no URL or token can override that boundary. */
class CanonicalHttpTransport(
    private val endpoint: () -> String?,
    private val transport: AdminTransport = DisabledAdminTransport,
) : CanonicalAdminTransport {
    override suspend fun exchange(method: String, path: String, body: String?, accessToken: String?): AdminResponse {
        check(transport.available) { "Community networking disabled" }
        val base = URI(endpoint().orEmpty())
        require(base.scheme == "https" && base.host != null && base.rawUserInfo == null &&
            base.port in setOf(-1, 443) && base.rawQuery == null && base.rawFragment == null &&
            base.rawPath in setOf("", "/"))
        require(path.startsWith("/v1/") && !path.contains('#') && !path.contains('\\') &&
            URI(path).rawAuthority == null && URI(path).scheme == null)
        require(method in setOf("GET", "POST", "DELETE"))
        val bytes = body?.toByteArray(Charsets.UTF_8)
        require(bytes == null || bytes.size <= 16384)
        val headers = mutableMapOf("Accept" to "application/json")
        if (bytes != null) headers["Content-Type"] = "application/json; charset=utf-8"
        if (accessToken != null) {
            require(accessToken.matches(Regex("[A-Za-z0-9_-]{1,8192}")))
            headers["Authorization"] = "Bearer $accessToken"
        }
        val response = transport.call(method, base.resolve(path).toString(), headers, bytes)
        check(endpoint().orEmpty() == base.toString()) { "Endpoint changed during request" }
        require(response.body.size <= 524288)
        if (response.code == 204) require(response.body.isEmpty())
        val raw = Charsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(response.body)).toString()
        return AdminResponse(response.code, raw, response.retryAfterSeconds?.toString())
    }
}

/** Application-scoped canonical service state. No personal journal dependency,
 * persisted decision payload, stored refresh token or background retry exists.
 * Process recreation starts unauthenticated; uncertain writes require explicit
 * reconciliation within the current authenticated process. */
data class CanonicalAdminState(
    val capabilities: AdminDocument? = null,
    val queue: AdminDocument? = null,
    val statistics: AdminDocument? = null,
    val detail: AdminDocument? = null,
    val audit: AdminDocument? = null,
    val hasUncertainDecision: Boolean = false,
)
class CanonicalAdminRepository(private val client: CanonicalAdminClient) {
    private val mutable = MutableStateFlow(CanonicalAdminState())
    val state: StateFlow<CanonicalAdminState> = mutable
    val authenticated: Boolean get() = client.authenticated
    private val guard = Any()
    private var generation = 0L
    fun clear() = synchronized(guard) { generation++; client.clear(); mutable.value = CanonicalAdminState() }
    suspend fun challenge() = client.challenge()
    suspend fun authenticate(challenge: String, credential: String) {
        clear()
        val stamp = synchronized(guard) { generation }
        try {
            client.login(challenge, credential)
            val caps = client.capabilities()
            synchronized(guard) {
                check(stamp == generation)
                mutable.value = CanonicalAdminState(capabilities = caps)
            }
        } catch (error: Exception) { synchronized(guard) { if (stamp == generation) clear() }; throw error }
    }
    private suspend fun load(block: suspend () -> AdminDocument, apply: (CanonicalAdminState, AdminDocument) -> CanonicalAdminState): AdminDocument {
        val stamp = synchronized(guard) { generation }
        try {
            val document = block()
            synchronized(guard) {
                check(stamp == generation)
                mutable.value = apply(mutable.value, document)
            }
            return document
        } catch (error: Exception) {
            synchronized(guard) {
                if (stamp == generation) {
                    if (!client.authenticated) clear()
                    else mutable.value = CanonicalAdminState(hasUncertainDecision = client.hasUncertainDecision)
                }
            }
            throw error
        }
    }
    suspend fun capabilities() = load({ client.capabilities() }) { state, doc -> state.copy(capabilities = doc) }
    suspend fun queue(filters: Map<String, String> = emptyMap()) = load({ client.queue(filters) }) { state, doc -> state.copy(queue = doc) }
    suspend fun statistics() = load({ client.statistics() }) { state, doc -> state.copy(statistics = doc) }
    suspend fun detail(id: String) = load({ client.detail(id) }) { state, doc -> state.copy(detail = doc) }
    suspend fun audit(filters: Map<String, String> = emptyMap()) = load({ client.audit(filters) }) { state, doc -> state.copy(audit = doc) }
    suspend fun review(id: String): AdminReview = client.review(id)
    suspend fun open(review: AdminReview) = load({ client.open(review) }) { _, _ -> CanonicalAdminState() }
    suspend fun confirm(review: AdminReview, decision: String, reason: String? = null, acknowledged: Boolean = false) =
        load({ client.confirm(review, decision, reason, acknowledged) }) { _, _ -> CanonicalAdminState() }
    suspend fun retryPending() = load({ client.retryPending() }) { _, _ -> CanonicalAdminState() }
    suspend fun refresh() {
        val stamp = synchronized(guard) { generation }
        try { client.refresh() } finally { synchronized(guard) { if (stamp == generation && !client.authenticated) clear() } }
    }
    suspend fun logout(): AdminLogout {
        synchronized(guard) { generation++; mutable.value = CanonicalAdminState() }
        // client.logout clears local credentials synchronously before transport I/O.
        return client.logout()
    }
}
