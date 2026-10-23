package com.opus.piano.admin

/**
 * Account-scoped local UX markers (Stage 7B.2).
 *
 * Scope and limits (owner-approved):
 *  - Two marker kinds only: [DEFERRED] and [RE_REVIEW]. Both are **local, personal
 *    view-state markers** — they are NOT server moderation states and never claim to
 *    be; the server status shown on a row always comes from the contract.
 *  - Markers are keyed by account principal + submission id, so they never bleed
 *    between accounts, and they are non-sensitive (ids only — no titles, no reasons,
 *    no payload, no decision drafts).
 *  - A marker persists until the item is confirmed resolved (server returns a
 *    non-pending status for it) or the user removes it manually.
 *
 * Pure Kotlin with a tiny persistence seam so marker semantics are JVM-testable.
 */
enum class AdminMarkerKind { DEFERRED, RE_REVIEW }

/** Persistence seam: set of submission ids per (account, kind). */
interface AdminMarkerStore {
    fun load(account: String, kind: AdminMarkerKind): Set<String>
    fun save(account: String, kind: AdminMarkerKind, ids: Set<String>)
}

/** In-memory store for JVM tests and previews. */
class InMemoryAdminMarkerStore : AdminMarkerStore {
    private val data = mutableMapOf<Pair<String, AdminMarkerKind>, Set<String>>()
    override fun load(account: String, kind: AdminMarkerKind): Set<String> =
        data[account to kind].orEmpty()
    override fun save(account: String, kind: AdminMarkerKind, ids: Set<String>) {
        data[account to kind] = ids.toSet()
    }
}

class AdminMarkers(
    private val store: AdminMarkerStore,
    /** Current account principal id; markers are meaningless across accounts. */
    private val account: () -> String,
) {
    private fun keySet(kind: AdminMarkerKind): MutableSet<String> =
        store.load(account().ifBlank { ANONYMOUS }, kind).toMutableSet()

    fun isMarked(kind: AdminMarkerKind, submissionId: String): Boolean =
        submissionId.isNotBlank() && submissionId in store.load(account().ifBlank { ANONYMOUS }, kind)

    fun set(kind: AdminMarkerKind, submissionId: String, marked: Boolean) {
        if (submissionId.isBlank()) return
        val ids = keySet(kind)
        val changed = if (marked) ids.add(submissionId) else ids.remove(submissionId)
        if (changed) store.save(account().ifBlank { ANONYMOUS }, kind, ids)
    }

    fun all(kind: AdminMarkerKind): Set<String> = store.load(account().ifBlank { ANONYMOUS }, kind)

    /**
     * Clear markers for a submission once the server confirms a resolved (non-pending)
     * status — called from the detail/decision flow with the ATOMIC server response,
     * never from a local-only optimistic update.
     */
    fun clearResolved(submissionId: String, serverStatus: AdminStatus) {
        if (submissionId.isBlank() || serverStatus.decidable || serverStatus == AdminStatus.WITHDRAWN) return
        set(AdminMarkerKind.DEFERRED, submissionId, false)
        set(AdminMarkerKind.RE_REVIEW, submissionId, false)
    }

    /** Drop every marker of the current account (logout / account switch). */
    fun clearAccount() {
        val acct = account().ifBlank { ANONYMOUS }
        store.save(acct, AdminMarkerKind.DEFERRED, emptySet())
        store.save(acct, AdminMarkerKind.RE_REVIEW, emptySet())
    }

    companion object {
        private const val ANONYMOUS = "__anonymous__"
    }
}
