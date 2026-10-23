package com.opus.piano.admin

/**
 * Role-trust window for the Administration section (Stage 7B.2).
 *
 * Contract (owner-approved): a **10-minute NON-sliding** trust window. The window is
 * fixed at the moment of a successful capability verification ([grant]); it never
 * extends while the user stays on an admin screen. It is invalidated by logout,
 * server revocation (401/403), or process restart (the policy object is in-memory, so
 * every cold start begins untrusted). Sensitive actions (decisions) must re-verify
 * [GET /v1/me/capabilities] whenever [isTrusted] is false.
 *
 * Pure Kotlin (no Android) so the window semantics are JVM-testable.
 */
class AdminTrustPolicy(
    /** Wall-clock milliseconds supplier; injectable for tests. */
    private val now: () -> Long = System::currentTimeMillis,
    /** Window length; parameterised for tests, production value is [WINDOW_MILLIS]. */
    private val windowMillis: Long = WINDOW_MILLIS,
) {
    /** Timestamp of the last successful verification, or null when untrusted. */
    @Volatile private var grantedAt: Long? = null

    /** True while a non-sliding verification window is active. */
    val isTrusted: Boolean
        get() {
            val at = grantedAt ?: return false
            val age = now() - at
            return age >= 0 && age < windowMillis
        }

    /** Age of the current window in ms, or -1 when untrusted (for "verified N ago" UI). */
    val ageMillis: Long
        get() {
            if (!isTrusted) return -1L
            return now() - (grantedAt ?: return -1L)
        }

    /** Call ONLY after a fresh, successful role/capability verification. */
    fun grant() { grantedAt = now() }

    /** Logout / revocation / explicit re-lock. */
    fun invalidate() { grantedAt = null }

    companion object {
        const val WINDOW_MILLIS: Long = 10 * 60 * 1000L
    }
}
