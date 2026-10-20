package com.opus.piano.data.offline

import com.opus.piano.data.offline.BundleState.*

/**
 * Explicit, reviewed lifecycle transitions for offline bundles.
 *
 * The pipeline required by OFFLINE-CACHE.md is
 * `QUEUED -> WAITING_FOR_POLICY -> DOWNLOADING -> VALIDATING -> INSTALLING -> READY`
 * with `PAUSED`, `FAILED_RETRYABLE`, `STALE` and `QUARANTINED` branches.
 *
 * Safety properties enforced here:
 *  - a partial bundle can never reach READY (only INSTALLING -> READY)
 *  - rights problems lead to QUARANTINED, never silently to READY
 *  - READY is not a download entry point: a refresh builds a separate pending revision
 *    and is published atomically, so an interrupted refresh keeps the old READY bytes
 *  - every non-terminal state may fall back to PAUSED/FAILED_RETRYABLE so a process kill
 *    always leaves a retryable state rather than a wedged one
 */
object BundleStateMachine {

    private val transitions: Map<BundleState, Set<BundleState>> = mapOf(
        QUEUED to setOf(WAITING_FOR_POLICY, DOWNLOADING, PAUSED, FAILED_RETRYABLE),
        WAITING_FOR_POLICY to setOf(QUEUED, DOWNLOADING, PAUSED, FAILED_RETRYABLE),
        DOWNLOADING to setOf(VALIDATING, WAITING_FOR_POLICY, PAUSED, FAILED_RETRYABLE),
        VALIDATING to setOf(INSTALLING, PAUSED, FAILED_RETRYABLE, QUARANTINED),
        INSTALLING to setOf(READY, PAUSED, FAILED_RETRYABLE, QUARANTINED),
        READY to setOf(STALE, QUARANTINED),
        STALE to setOf(QUEUED, DOWNLOADING, PAUSED, QUARANTINED),
        PAUSED to setOf(QUEUED, WAITING_FOR_POLICY, DOWNLOADING, FAILED_RETRYABLE),
        FAILED_RETRYABLE to setOf(QUEUED, WAITING_FOR_POLICY, DOWNLOADING, PAUSED, QUARANTINED),
        QUARANTINED to setOf(PAUSED, QUEUED)
    )

    fun allowed(from: BundleState): Set<BundleState> = transitions.getValue(from)

    fun canTransition(from: BundleState, to: BundleState): Boolean = to in allowed(from)

    /**
     * Shortest legal transition path from [from] to [to] (empty when already there).
     *
     * [READY] is never used as an intermediate state: a recovery/cleanup path may not pass
     * through "published" to reach some other state. Callers persist only the final state,
     * so every persisted transition stays inside this machine even when an error happened
     * mid-pipeline (e.g. DOWNLOADING -> QUARANTINED via VALIDATING).
     */
    fun path(from: BundleState, to: BundleState): List<BundleState> {
        if (from == to) return emptyList()
        val queue = ArrayDeque<Pair<BundleState, List<BundleState>>>()
        queue.add(from to emptyList())
        val seen = mutableSetOf(from)
        while (queue.isNotEmpty()) {
            val (state, travelled) = queue.removeFirst()
            for (next in allowed(state)) {
                if (next in seen) continue
                if (next == READY && to != READY) continue
                val route = travelled + next
                if (next == to) return route
                seen += next
                queue.add(next to route)
            }
        }
        return emptyList()
    }


    /** Throws with an actionable message instead of silently corrupting lifecycle state. */
    fun requireTransition(from: BundleState, to: BundleState) {
        require(canTransition(from, to)) { "Illegal bundle transition $from -> $to" }
    }

    /**
     * State to adopt when a process dies mid-flight. Recovery never jumps to READY:
     * content that was not validated and published stays explicitly partial/retryable.
     */
    fun afterProcessRestart(state: BundleState): BundleState = when (state) {
        DOWNLOADING, VALIDATING, INSTALLING -> PAUSED
        else -> state
    }

    /** True only for states that may legitimately hold published, readable content. */
    fun holdsPublishedContent(state: BundleState): Boolean = state == READY

    /**
     * Whether a state machine step may write bytes for [state] under the current policy.
     * TRANSFER is deliberately gated so a policy check must happen before any byte moves.
     */
    enum class Step { TRANSFER, VALIDATE, PUBLISH }

    fun permits(state: BundleState, step: Step): Boolean = when (step) {
        Step.TRANSFER -> state in setOf(QUEUED, WAITING_FOR_POLICY, DOWNLOADING)
        Step.VALIDATE -> state == VALIDATING
        Step.PUBLISH -> state == INSTALLING
    }
}
