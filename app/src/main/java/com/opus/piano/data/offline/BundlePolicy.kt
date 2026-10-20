package com.opus.piano.data.offline

/**
 * Network and consent policy for bundle transfers (D7, approved 2026-10-20).
 *
 * Rules enforced here, in priority order:
 *  - only a **validated** connection may move bundle bytes
 *  - unmetered (Wi-Fi/ethernet) transfers need no job allowance
 *  - metered transfers require an explicit **per-job** allowance — never a global default,
 *    never the general INTERNET permission, and never for automatic background work
 *  - policy is re-evaluated before every file and during streaming: revoking the allowance
 *    makes [allowsTransfer] false immediately, which pauses the job (it never deletes data)
 *
 * Pure Kotlin on purpose: the matrix is JVM-testable without an emulator.
 */
data class NetworkFacts(
    val connected: Boolean,
    val validated: Boolean,
    val unmetered: Boolean,
    val metered: Boolean,
    /**
     * Data Saver ("Restrict background data") is active for this process. Android only
     * restricts background traffic on **metered** networks, so [BundlePolicy] honours it on
     * exactly that path; unmetered transfers are unaffected.
     */
    val backgroundRestricted: Boolean = false
) {
    init {
        require(unmetered != metered || !connected) {
            "connected networks must be exactly one of unmetered/metered"
        }
    }

    companion object {
        val DISCONNECTED = NetworkFacts(connected = false, validated = false, unmetered = false, metered = false)
    }
}

object BundlePolicy {

    /** D7: jobs above this size need explicit confirmation before any byte moves. */
    const val LARGE_JOB_BYTES = 8L * 1024 * 1024

    /** D7: combined catalogue/cache/offline ceiling (aligned with CatalogueStorage.MAX_TOTAL). */
    const val MAX_TOTAL_BYTES = 256L * 1024 * 1024

    /** D7: free-space floor; the real reserve may grow (CatalogueBudget.reserve). */
    const val MIN_FREE_RESERVE_BYTES = 128L * 1024 * 1024

    /**
     * Stable, persisted stop codes (`lastError`). Never free text: they are durable metadata
     * that survives process death, drives the waiting-vs-paused decision, and can be localized
     * by a future UI without parsing messages.
     */
    object Reasons {
        const val OFFLINE = "offline"
        const val UNVALIDATED = "unvalidated_network"
        const val DATA_SAVER = "data_saver"
        const val CONSENT = "consent_missing"
        const val POLICY = "policy"
        const val STORAGE = "storage"
        const val INTEGRITY = "integrity"
        const val RIGHTS = "rights"
        const val SOURCE = "source"
        const val TRANSPORT = "transport"
        const val CANCELLED = "cancelled"
        const val MANIFEST_MISSING = "manifest_missing"
        const val READER_VERSION = "reader_version"

        /** Environment problems we simply wait out; consent is not one of them. */
        val WAITING = setOf(OFFLINE, UNVALIDATED, DATA_SAVER, POLICY)

        /**
         * Why a byte must not move right now, or `null` when the policy allows it.
         * Single source of truth shared by the pre-file check and the streaming re-check.
         */
        fun stopReason(network: NetworkFacts, jobMeteredAllowed: Boolean): String? = when {
            !network.connected -> OFFLINE
            !network.validated -> UNVALIDATED
            network.unmetered -> null
            network.backgroundRestricted -> DATA_SAVER
            jobMeteredAllowed -> null
            else -> CONSENT
        }
    }

    /**
     * Confirmation gate shown before a job starts. Unknown size (`null`) always needs an
     * explicit bounded approval because the streaming cap cannot be inferred (D7).
     */
    fun requiresConfirmation(estimatedBytes: Long?): Boolean =
        estimatedBytes == null || estimatedBytes < 0 || estimatedBytes > LARGE_JOB_BYTES

    /** Single source of truth for "may a byte move right now, for THIS job?". */
    fun allowsTransfer(network: NetworkFacts, jobMeteredAllowed: Boolean): Boolean =
        Reasons.stopReason(network, jobMeteredAllowed) == null

    /**
     * Background (WorkManager, D5.3) jobs are unmetered-only by construction: a per-job
     * metered allowance never applies without explicit consent (D7: no automatic mobile
     * background downloads).
     */
    fun allowsBackgroundJob(network: NetworkFacts): Boolean = allowsTransfer(network, jobMeteredAllowed = false)

    /** Rechecked during streaming: false means pause now, keep already-verified content. */
    fun keepsRunning(network: NetworkFacts, jobMeteredAllowed: Boolean): Boolean =
        allowsTransfer(network, jobMeteredAllowed)
}
