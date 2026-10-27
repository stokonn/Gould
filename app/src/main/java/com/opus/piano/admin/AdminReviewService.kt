package com.opus.piano.admin

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

/** In-memory only. No payload, reason, confirmation or trust is persisted. */
data class AdminConfirmation(val id: String, val revision: Int, val status: AdminStatus, val decision: AdminDecision)
data class AdminPendingDecision(val id: String, val decision: AdminDecision, val revision: Int, val reason: String?, val key: String)
data class AdminReviewState(
    val detail: AdminSubmissionDetail? = null,
    val previous: AdminSubmissionDetail? = null,
    val loading: Boolean = false,
    val verified: Boolean = false,
    val failure: AdminFailure? = null,
    val confirmation: AdminConfirmation? = null,
    val reReview: Boolean = false,
    val conflicts: Int = 0,
    val pending: AdminPendingDecision? = null,
    val success: AdminDecisionResult? = null,
)

/** Every new decision revalidates role + latest revision twice: before opening its
 * confirmation, and before POST. Server expectedRevision + atomic status guard is
 * authoritative. Uncertain outcomes retain the identical key/body for explicit
 * replay, blocking a new decision; never silently retry or auto-resubmit a conflict. */
class AdminReviewService(private val repo: AdminRepository) {
    private val mutable = MutableStateFlow(AdminReviewState())
    val state: StateFlow<AdminReviewState> = mutable
    private val mutex = Mutex()
    private var generation = 0
    fun clear(failure: AdminFailure? = null) { generation++; mutable.value = AdminReviewState(failure = failure) }
    fun lock() { generation++; mutable.value = mutable.value.copy(verified = false, loading = false, confirmation = null) }
    fun cancelConfirmation() { mutable.value = mutable.value.copy(confirmation = null) }
    fun defer() { mutable.value.detail?.let { repo.markers.set(AdminMarkerKind.DEFERRED, it.id, true) } }
    fun removeDeferred() { mutable.value.detail?.let { repo.markers.set(AdminMarkerKind.DEFERRED, it.id, false) } }

    suspend fun validate(id: String) = mutex.withLock { validateLocked(id) }

    private suspend fun validateLocked(id: String): Boolean {
        if (mutable.value.pending?.let { it.id != id } == true) return false
        val stamp = generation
        val old = mutable.value.detail?.takeIf { it.id == id }
        mutable.value = if (old == null) AdminReviewState(loading = true) else
            mutable.value.copy(loading = true, verified = false, confirmation = null, failure = null)
        try {
            val role = repo.verifyRole()
            if (stamp != generation) return false
            if (role is AdminResult.Err) { fail(role.failure); return false }
            if (!(role as AdminResult.Ok).value.canQueueRead) { fail(AdminFailure.Forbidden); return false }
            val result = repo.api.submission(id)
            if (stamp != generation) return false
            return when (result) {
                is AdminResult.Err -> { fail(result.failure); false }
                is AdminResult.Ok -> {
                    if (stamp != generation) return false
                    val detail = result.value
                    if (detail.id != id || detail.revision < 1 || detail.latest?.revision != detail.revision) {
                        fail(AdminFailure.InvalidPayload); return false
                    }
                    val changed = old != null && (old.revision != detail.revision || old.status != detail.status ||
                        old.latest?.payload.toString() != detail.latest?.payload.toString())
                    if (changed && detail.status.decidable) repo.markers.set(AdminMarkerKind.RE_REVIEW, id, true)
                    repo.markers.clearResolved(id, detail.status)
                    mutable.value = mutable.value.copy(detail = detail, previous = if (changed) old else mutable.value.previous,
                        verified = true, failure = null, reReview = repo.markers.isMarked(AdminMarkerKind.RE_REVIEW,id),
                        conflicts = mutable.value.conflicts + if (changed) 1 else 0)
                    true
                }
            }
        } finally { if (stamp == generation) mutable.value = mutable.value.copy(loading = false) }
    }

    suspend fun acknowledge(revision: Int) = mutex.withLock {
        val before = mutable.value.detail ?: return@withLock
        if (!validateLocked(before.id)) return@withLock
        if (mutable.value.detail?.revision == revision) {
            repo.markers.set(AdminMarkerKind.RE_REVIEW,before.id,false)
            mutable.value = mutable.value.copy(reReview = false, previous = null)
        }
    }

    suspend fun prepare(decision: AdminDecision) = mutex.withLock {
        if (!repo.decisionsEnabled) { fail(AdminFailure.ContractUnavailable); return@withLock }
        val stamp = generation
        val old = mutable.value.detail ?: return@withLock
        if (mutable.value.pending != null || mutable.value.success != null) return@withLock
        if (!validateLocked(old.id)) return@withLock
        val s = mutable.value
        val latest = s.detail ?: return@withLock
        if (s.reReview || !latest.status.decidable || latest.revision != old.revision || latest.status != old.status) return@withLock
        val role = repo.verifyRole()
        if (stamp != generation) return@withLock
        if (role !is AdminResult.Ok || !role.value.canDecide || role.value.principalId == latest.owner) {
            fail(if (role is AdminResult.Err) role.failure else AdminFailure.Forbidden); return@withLock
        }
        mutable.value = mutable.value.copy(confirmation = AdminConfirmation(latest.id,latest.revision,latest.status,decision))
    }

    suspend fun confirm(reason: String?) = mutex.withLock {
        if (!repo.decisionsEnabled) { fail(AdminFailure.ContractUnavailable); return@withLock }
        val stamp = generation
        val confirmation = mutable.value.confirmation ?: return@withLock
        // Reject and information requests require meaningful nonblank text; no draft persisted.
        if (confirmation.decision != AdminDecision.APPROVE && reason.isNullOrBlank()) return@withLock
        if (reason != null && reason.length > 2000) return@withLock
        if (!validateLocked(confirmation.id)) return@withLock
        val current = mutable.value
        if (current.reReview || current.detail?.revision != confirmation.revision || current.detail.status != confirmation.status) return@withLock
        val role = repo.verifyRole()
        if (stamp != generation) return@withLock
        if (role !is AdminResult.Ok || !role.value.canDecide || role.value.principalId == current.detail.owner) {
            fail(if (role is AdminResult.Err) role.failure else AdminFailure.Forbidden); return@withLock
        }
        val pending = AdminPendingDecision(confirmation.id,confirmation.decision,confirmation.revision,
            reason?.trim()?.takeIf { it.isNotBlank() },UUID.randomUUID().toString())
        mutable.value = mutable.value.copy(pending = pending, confirmation = null)
        send(pending)
    }

    /** Explicit reconciliation uses precisely the original body/key. The server's
     * replay check occurs before its status guard. A plain GET cannot prove which
     * actor/key caused a terminal state; it must never be called a confirmed success. */
    suspend fun retryPending() = mutex.withLock {
        if (!repo.decisionsEnabled) { fail(AdminFailure.ContractUnavailable); return@withLock }
        val stamp = generation
        val pending = mutable.value.pending ?: return@withLock
        val role = repo.verifyRole()
        if (stamp != generation) return@withLock
        if (role !is AdminResult.Ok || !role.value.canDecide) {
            fail(if (role is AdminResult.Err) role.failure else AdminFailure.Forbidden); return@withLock
        }
        send(pending)
    }

    private suspend fun send(pending: AdminPendingDecision) {
        val stamp = generation
        mutable.value = mutable.value.copy(loading = true, verified = false)
        try {
            when (val result = repo.api.decide(pending.id, buildDecisionBody(pending.decision,pending.revision,pending.reason,pending.key))) {
                is AdminResult.Ok -> {
                    if (stamp != generation) return
                    val value = result.value
                    val expectedStatus = when (pending.decision) {
                        AdminDecision.APPROVE -> setOf(AdminStatus.APPROVED,AdminStatus.PUBLISHED)
                        AdminDecision.REJECT -> setOf(AdminStatus.REJECTED)
                        AdminDecision.REQUEST_CHANGES -> setOf(AdminStatus.CHANGES_REQUESTED)
                    }
                    if (value.submissionId != pending.id || value.decision != pending.decision.wire ||
                        value.revision != pending.revision || value.status !in expectedStatus) { fail(AdminFailure.InvalidPayload); return }
                    repo.markers.clearResolved(value.submissionId,value.status)
                    mutable.value = mutable.value.copy(success = value, pending = null, failure = null)
                }
                is AdminResult.Err -> {
                    if (stamp != generation) return
                    val f = result.failure
                    if (f is AdminFailure.Conflict) {
                        val conflicts = mutable.value.conflicts
                        mutable.value = mutable.value.copy(pending = null, reReview = true)
                        repo.markers.set(AdminMarkerKind.RE_REVIEW,pending.id,true)
                        validateLocked(pending.id)
                        if (stamp == generation) mutable.value = mutable.value.copy(conflicts = conflicts + 1)
                    } else if (f is AdminFailure.Validation || f == AdminFailure.Forbidden || f == AdminFailure.Unauthorized) {
                        mutable.value = mutable.value.copy(pending = null)
                    }
                    fail(f)
                }
            }
        } finally { if (stamp == generation) mutable.value = mutable.value.copy(loading = false) }
    }

    private fun fail(failure: AdminFailure) {
        if (failure == AdminFailure.Unauthorized || failure == AdminFailure.Forbidden) repo.trust.invalidate()
        mutable.value = mutable.value.copy(failure = failure, verified = false, confirmation = null)
    }
}

/** Compare proposal snapshots only, never imply these are live catalogue values. */
data class AdminFieldChange(val name: String, val before: org.json.JSONObject?, val after: org.json.JSONObject?) {
    val evidenceChanged: Boolean get() = before?.opt("evidence").toString() != after?.opt("evidence").toString()
}
fun proposalFieldChanges(before: org.json.JSONObject, after: org.json.JSONObject): List<AdminFieldChange> {
    fun fields(payload: org.json.JSONObject): Map<String,org.json.JSONObject> {
        val array=payload.optJSONArray("fields") ?: return emptyMap()
        return (0 until array.length()).mapNotNull { array.optJSONObject(it) }
            .associateBy { it.optString("field") }
    }
    val old=fields(before); val current=fields(after)
    return (old.keys+current.keys).mapNotNull { key ->
        val previous=old[key]; val latest=current[key]
        if (previous?.opt("newValue").toString()==latest?.opt("newValue").toString() &&
            previous?.opt("evidence").toString()==latest?.opt("evidence").toString()) null
        else AdminFieldChange(key,previous,latest)
    }
}
