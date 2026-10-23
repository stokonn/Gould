package com.opus.piano.admin

/**
 * Administration facade (Stage 7B.2/7B.3): one entry point owning the verified API
 * client, session token, trust window and personal markers. Screens never talk to
 * [AdminApi] directly for role or trust decisions — this class is the single place
 * where "verified role" and "trusted window" are granted or invalidated.
 */
class AdminRepository(
    val api: AdminApi,
    val session: AdminSessionStore,
    val trust: AdminTrustPolicy,
    val markers: AdminMarkers,
    private val stats: AdminStatsService,
) {
    /**
     * Fresh role verification via the verified `GET /v1/me/capabilities`.
     * On SUCCESS the trust window is re-granted (fresh grant, non-sliding 10 min).
     * Failures never grant trust and never fabricate capabilities.
     */
    suspend fun verifyRole(): AdminResult<AdminCapabilities> =
        when (val result = api.capabilities()) {
            is AdminResult.Err -> result
            is AdminResult.Ok -> {
                val caps = AdminCapabilities(result.value.principalId, result.value.capabilities)
                trust.grant()
                AdminResult.Ok(caps)
            }
        }

    /** Whether the local capabilities answer (within the trust window) grants any admin surface. */
    fun hasAdminSurface(caps: AdminCapabilities?): Boolean = caps?.let {
        it.canQueueRead || it.canAuditRead || it.canDecide
    } == true

    /** Dashboard stats — exact-or-unavailable, bounded, never partial-as-total. */
    suspend fun stats(nowMillis: Long): AdminStats = stats.load(nowMillis)

    /**
     * Session sign-out (contract `DELETE /v1/session`). Local credential, trust and
     * markers are ALWAYS cleared on user request — even if the server revoke cannot
     * be reached — so a lost device or unreachable backend never keeps a local admin
     * session alive. The server-side revoke outcome is returned honestly; local
     * clearing is not presented as server revocation.
     */
    suspend fun signOut(): AdminResult<Boolean> {
        val server = api.signOut()
        session.clear()
        trust.invalidate()
        markers.clearAccount()
        return server
    }

    /** Local-only re-lock (e.g. trust window expiry handling or account switch). */
    fun lockLocally() {
        trust.invalidate()
    }
}
