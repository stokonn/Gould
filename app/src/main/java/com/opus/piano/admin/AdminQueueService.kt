package com.opus.piano.admin

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject

/**
 * Moderation-queue domain service (Stage 7B.4).
 *
 * Owns the queue list state so it survives screen navigation inside the process:
 * cursor-paged loading with dedup, bounded display hydration through the verified
 * detail endpoint (gap G2 — queue rows carry no title/composer), owner-approved
 * search bounds, opportunistic change detection for the floating update pill
 * (NEVER polling) and account-scoped personal overlays (Deferred / Re-review).
 *
 * Contract honesty rules enforced here, by construction:
 *  - Only verified endpoints are called: `GET /v1/admin/queue` and
 *    `GET /v1/admin/submissions/{id}`. No search parameter is invented (G2).
 *  - ID search resolves through the verified detail endpoint (owner decision
 *    2026-10-23). Text search runs ONLY when the entire eligible queue is
 *    exhaustible within [SearchPolicy] bounds; otherwise the caller receives
 *    [AdminSearchState.RequiresServerSearch] — never a silently partial result.
 *  - Change detection happens only opportunistically (screen-entry revalidation);
 *    an incomplete walk or a failed walk changes NOTHING — no count is ever
 *    derived from partial data. No timers, no polling.
 *  - Personal markers are cleared only after a verified server response confirms
 *    a terminal status for the id (detail lookup / hydration), never locally.
 */
enum class AdminHydration { NONE, RUNNING, COMPLETE, LIMITED }

/** Result of a queue search. [AdminSearchState.Running] carries the query so a stale
 *  in-flight answer can be recognized and dropped after the query changed. */
sealed interface AdminSearchState {
    data object Idle : AdminSearchState
    data class Running(val query: String) : AdminSearchState
    data class Results(val rows: List<AdminQueueRow>) : AdminSearchState
    /** Complete scan, nothing matched (text search). */
    data object NoMatch : AdminSearchState
    /** Detail endpoint answered 404 for an ID search (owner-approved lookup path). */
    data object IdNotFound : AdminSearchState
    /** Text search beyond [SearchPolicy] bounds — the UI must say so explicitly (G2). */
    data object RequiresServerSearch : AdminSearchState
    data class Failed(val failure: AdminFailure) : AdminSearchState
}

/**
 * Owner-approved search bounds (decision 2026-10-23, CONTRACT-AUDIT gap G2):
 * deep (title/composer/type) search may only run when the whole eligible queue is
 * exhaustively scannable within these request budgets; otherwise the UI states that
 * full search requires a Stage 6 server-side search endpoint.
 */
data class SearchPolicy(
    /** Bounded queue walk budget (same order as the 7B.3 stats budget). */
    val maxQueuePages: Int = MAX_QUEUE_PAGES,
    /** Detail fetches one deep search may issue (one contract queue page worth). */
    val maxHydrations: Int = MAX_HYDRATIONS,
) {
    fun deepSearchAllowed(itemCount: Int, pagesWalked: Int, complete: Boolean): Boolean =
        complete && pagesWalked <= maxQueuePages && itemCount <= maxHydrations

    companion object {
        const val MAX_QUEUE_PAGES = 8
        const val MAX_HYDRATIONS = 20
        /** Detail fetches for personal marker rows that are not in the loaded queue. */
        const val MAX_MARKER_HYDRATIONS = 8
    }
}

/**
 * Display labels hydrated from the LATEST revision payload — only the documented
 * allowlisted locations (DATA-MODEL.md §2 shape); absent labels stay null and are
 * never invented. [kind] supports the approved "type" facet of search.
 */
data class AdminDisplay(val title: String?, val composer: String?, val kind: String?) {
    /** Lowercased haystack for the approved deep-search matching. */
    val searchable: String
        get() = listOfNotNull(title, composer, kind).joinToString(" ").lowercase()

    companion object {
        fun from(payload: JSONObject?): AdminDisplay? {
            val p = payload ?: return null
            var title: String? = null
            var composer: String? = null
            val fields = p.optJSONArray("fields")
            if (fields != null) {
                for (i in 0 until fields.length()) {
                    val f = fields.optJSONObject(i) ?: continue
                    val value = f.optString("newValue").takeIf { it.isNotBlank() } ?: continue
                    when (f.optString("field")) {
                        "title" -> if (title == null) title = value
                        "composer", "preferred_name" -> if (composer == null) composer = value
                    }
                }
            }
            val kind = p.optJSONObject("target")?.optString("kind")?.takeIf { it.isNotBlank() }
            return if (title == null && composer == null && kind == null) {
                null
            } else {
                AdminDisplay(title, composer, kind)
            }
        }
    }
}

/**
 * One renderable queue row. Status/revision are NULLABLE on purpose: a row built
 * only from a verified id (personal marker not yet hydrated) must not fabricate a
 * status or revision — the UI shows an explicit unknown/unavailable state instead.
 */
data class AdminQueueRow(
    val id: String,
    val status: AdminStatus? = null,
    val revision: Int? = null,
    val owner: String? = null,
    val evidenceCount: Int? = null,
    val createdAt: Long? = null,
    val display: AdminDisplay? = null,
    val displayLoading: Boolean = false,
) {
    constructor(item: AdminQueueItem) : this(
        id = item.id,
        status = item.status,
        revision = item.revision,
        owner = item.owner,
        evidenceCount = item.evidenceCount,
        createdAt = item.createdAt,
    )

    /** Row from the verified detail endpoint: authoritative status/revision, labels from payload. */
    constructor(detail: AdminSubmissionDetail) : this(
        id = detail.id,
        status = detail.status,
        revision = detail.revision,
        owner = detail.owner,
        display = AdminDisplay.from(detail.latest?.payload),
    )
}

data class AdminQueueState(
    /** At least one load finished — later screen entries revalidate instead of reloading. */
    val loaded: Boolean = false,
    val loading: Boolean = false,
    /** Failure of the most recent primary load (list rows are kept — last-known data). */
    val failure: AdminFailure? = null,
    val rows: List<AdminQueueRow> = emptyList(),
    val nextCursor: String? = null,
    val pagesLoaded: Int = 0,
    /** New ids detected by the last COMPLETE revalidation and not yet applied. */
    val pendingUpdates: Int = 0,
    val hydration: AdminHydration = AdminHydration.NONE,
    /** Rows for personal markers (both kinds), sorted by id; id-only until hydrated. */
    val marked: List<AdminQueueRow> = emptyList(),
    val search: AdminSearchState = AdminSearchState.Idle,
)

class AdminQueueService(
    private val api: AdminApi,
    private val markers: AdminMarkers,
    private val policy: SearchPolicy = SearchPolicy(),
) {
    private val _state = MutableStateFlow(AdminQueueState())
    val state: StateFlow<AdminQueueState> = _state

    /** Serializes every action — state transitions are strictly ordered. */
    private val mutex = Mutex()
    private val markedAttempted = mutableSetOf<String>()
    private var baseline: Map<String, Pair<Int?, AdminStatus?>>? = null
    private fun snapshot(rows: List<AdminQueueRow>) = rows.associate { it.id to (it.revision to it.status) }

    fun clear() {
        _state.value = AdminQueueState()
        baseline = null
        markedAttempted.clear()
    }

    // ---- screen entry / loading -----------------------------------------

    /** First visit loads the queue; later visits revalidate opportunistically (no polling). */
    suspend fun enter() {
        if (_state.value.loaded) revalidate() else initialLoad()
    }

    suspend fun initialLoad() = mutex.withLock {
        if (_state.value.loaded || _state.value.loading) return@withLock
        loadDepth(depth = 1)
    }

    suspend fun loadMore() = mutex.withLock {
        val s = _state.value
        val cursor = s.nextCursor ?: return@withLock
        if (s.pagesLoaded >= policy.maxQueuePages) return@withLock
        mutate { it.copy(loading = true, failure = null) }
        try {
            when (val page = api.queue(cursor, status = null)) {
                is AdminResult.Err -> mutate { it.copy(failure = page.failure) }
                is AdminResult.Ok -> {
                    if (page.value.nextCursor == cursor) {
                        mutate { it.copy(failure = AdminFailure.InvalidPayload) }
                        return@withLock
                    }
                    val before = _state.value.rows
                    val merged = mergeRows(before, page.value.items, inheritDisplay = true)
                    val fresh = merged.filter { r -> before.none { it.id == r.id } }.map { it.id }
                    mutate {
                        it.copy(
                            rows = merged,
                            nextCursor = page.value.nextCursor?.takeUnless { it == cursor },
                            pagesLoaded = it.pagesLoaded + 1,
                            failure = null,
                        )
                    }
                    hydrate(fresh)
                }
            }
        } finally {
            mutate { it.copy(loading = false) }
        }
    }

    /**
     * Explicit reload (manual refresh, pill apply, retry). Restores the depth the
     * user had loaded; when updates are pending it performs a complete bounded walk
     * so the newly detected ids are actually applied. Re-baselines [AdminQueueState.pendingUpdates]
     * to 0 — this IS the apply action for the floating pill.
     */
    suspend fun refresh() = mutex.withLock {
        if (_state.value.loading) return@withLock
        // A pending pill implies the last revalidation was a COMPLETE walk within the
        // policy bound, so applying it needs the same complete bounded walk.
        val depth = if (_state.value.pendingUpdates > 0) policy.maxQueuePages
        else _state.value.pagesLoaded.coerceIn(1, policy.maxQueuePages)
        loadDepth(depth = depth)
    }

    // ---- opportunistic change detection (the floating update pill) -------

    /**
     * Complete bounded walk compared against the DISPLAYED rows; only ids not
     * currently displayed count toward the pill. An incomplete or failed walk
     * changes nothing — a pill count is never derived from partial data. The list
     * itself is not replaced: scroll/filter/search context stays intact and the
     * user applies updates explicitly via the pill.
     */
    suspend fun revalidate() = mutex.withLock {
        when (val walk = walkAll(policy.maxQueuePages)) {
            is Walk.Complete -> {
                val current = snapshot(walk.rows)
                val old = baseline
                if (old != null) {
                    val changed = (old.keys + current.keys).count { old[it] != current[it] }
                    mutate { it.copy(pendingUpdates = changed) }
                } else baseline = current
            }
            is Walk.Incomplete, is Walk.Failed -> { /* no claim from partial/failed data */ }
        }
    }

    // ---- search (owner decision 2026-10-23) ------------------------------

    suspend fun search(query: String) = mutex.withLock {
        val q = query.trim()
        if (q.isEmpty()) {
            mutate { it.copy(search = AdminSearchState.Idle) }
            return@withLock
        }
        mutate { it.copy(search = AdminSearchState.Running(q)) }
        try {
            if (looksLikeId(q)) idLookup(q.removePrefix("#").trim()) else deepSearch(q)
        } finally {
            // Cancellation mid-flight must not leave a permanent "Running" state.
            if (_state.value.search is AdminSearchState.Running) {
                mutate { it.copy(search = AdminSearchState.Idle) }
            }
        }
    }

    fun clearSearch() {
        if (_state.value.search != AdminSearchState.Idle) {
            mutate { it.copy(search = AdminSearchState.Idle) }
        }
    }

    /**
     * Owner decision: `#1042`-style / `sub_x` / pure-digit queries resolve through
     * the verified detail endpoint — authoritative server truth for that submission
     * even when it is outside the pending queue walk.
     */
    fun looksLikeId(query: String): Boolean {
        val q = query.trim().removePrefix("#").trim()
        if (q.isEmpty() || q.length > 128) return false
        if (!q.all { it.isLetterOrDigit() || it == '_' || it == '-' }) return false
        return query.trim().startsWith("#") || q.all { it.isDigit() } || q.contains('_') ||
            q.matches(Regex("[0-9a-fA-F]{8}(-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}"))
    }

    private suspend fun idLookup(id: String) {
        if (id.isEmpty()) {
            mutate { it.copy(search = AdminSearchState.IdNotFound) }
            return
        }
        when (val result = api.submission(id)) {
            is AdminResult.Err -> mutate {
                it.copy(
                    search = if (result.failure is AdminFailure.NotFound) {
                        AdminSearchState.IdNotFound
                    } else {
                        AdminSearchState.Failed(result.failure)
                    },
                )
            }
            is AdminResult.Ok -> {
                markers.clearResolved(result.value.id, result.value.status) // confirmed server status
                mutate { it.copy(search = AdminSearchState.Results(listOf(AdminQueueRow(result.value)))) }
            }
        }
    }

    private suspend fun deepSearch(q: String) {
        val needle = q.lowercase()
        when (val walk = walkAll(policy.maxQueuePages)) {
            is Walk.Failed -> mutate { it.copy(search = AdminSearchState.Failed(walk.failure)) }
            is Walk.Incomplete -> mutate { it.copy(search = AdminSearchState.RequiresServerSearch) }
            is Walk.Complete -> {
                if (!policy.deepSearchAllowed(walk.rows.size, policy.maxQueuePages, complete = true)) {
                    mutate { it.copy(search = AdminSearchState.RequiresServerSearch) }
                    return
                }
                // Strict hydration: a partial scan could silently omit title matches,
                // so ANY failure aborts with an honest error instead of partial results.
                val displays = mutableMapOf<String, AdminDisplay?>()
                val verified = mutableMapOf<String, AdminQueueRow>()
                for (row in walk.rows) {
                    currentCoroutineContext().ensureActive()
                    when (val detail = api.submission(row.id)) {
                        is AdminResult.Err -> {
                            mutate { it.copy(search = AdminSearchState.Failed(detail.failure)) }
                            return
                        }
                        is AdminResult.Ok -> {
                            markers.clearResolved(detail.value.id, detail.value.status)
                            val display = AdminDisplay.from(detail.value.latest?.payload)
                            displays[row.id] = display
                            verified[row.id] = AdminQueueRow(detail.value)
                            setRow(row.id) { it.copy(status = detail.value.status, revision = detail.value.revision, display = display) } // cache for the browse list too
                        }
                    }
                }
                val results = walk.rows.mapNotNull { verified[it.id] }.filter { it.status?.decidable == true }.filter { row ->
                    row.id.lowercase().contains(needle) ||
                        (displays[row.id]?.searchable ?: "").contains(needle)
                }.map { it.copy(display = displays[it.id]) }
                mutate {
                    it.copy(
                        search = if (results.isEmpty()) {
                            AdminSearchState.NoMatch
                        } else {
                            AdminSearchState.Results(results)
                        },
                    )
                }
            }
        }
    }

    // ---- personal markers (Deferred / Re-review overlays) ----------------

    /**
     * Ensure rows exist for every personal marker id. Ids not present in the loaded
     * queue are hydrated through the verified detail endpoint, bounded to
     * [SearchPolicy.MAX_MARKER_HYDRATIONS] per service lifetime; failures keep an
     * honest id-only row (no retry loop, no invented status).
     */
    suspend fun ensureMarked() = mutex.withLock { syncMarkedLocked(hydrateMissing = true) }

    /** Deterministic resume candidate — same rule as the dashboard queue row. */
    fun resumeCandidate(): String? =
        markers.all(AdminMarkerKind.RE_REVIEW).sorted().firstOrNull()
            ?: markers.all(AdminMarkerKind.DEFERRED).sorted().firstOrNull()

    // ---- pure projections (JVM-testable) --------------------------------

    /** Browse list for [filter]: server-status filters over loaded rows; personal
     *  overlays additionally include marker ids that are not in the loaded pages. */
    fun visibleRows(filter: String): List<AdminQueueRow> = when (filter) {
        FILTER_RE_REVIEW, FILTER_DEFERRED -> {
            val kind = if (filter == FILTER_RE_REVIEW) AdminMarkerKind.RE_REVIEW else AdminMarkerKind.DEFERRED
            overlayRows(markers.all(kind))
        }
        else -> filterFor(_state.value.rows, filter)
    }

    /** Row filtering shared by browse and search result views. */
    fun filterFor(rows: List<AdminQueueRow>, filter: String): List<AdminQueueRow> = when (filter) {
        FILTER_PENDING -> rows.filter {
            it.status == AdminStatus.SUBMITTED || it.status == AdminStatus.IN_REVIEW
        }
        FILTER_NEEDS_INFO -> rows.filter { it.status == AdminStatus.CHANGES_REQUESTED }
        FILTER_RE_REVIEW -> rows.filter { it.id in markers.all(AdminMarkerKind.RE_REVIEW) }
        FILTER_DEFERRED -> rows.filter { it.id in markers.all(AdminMarkerKind.DEFERRED) }
        else -> rows
    }

    private fun overlayRows(markedIds: Set<String>): List<AdminQueueRow> {
        val loaded = _state.value.rows.associateBy { it.id }
        val marked = _state.value.marked.associateBy { it.id }
        return markedIds.sorted().map { id -> loaded[id] ?: marked[id] ?: AdminQueueRow(id) }
    }

    // ---- internals -------------------------------------------------------

    private sealed interface Walk {
        data class Complete(val rows: List<AdminQueueRow>) : Walk
        data object Incomplete : Walk
        data class Failed(val failure: AdminFailure) : Walk
    }

    /** Bounded complete-or-incomplete walk from the queue start (deduped, server order). */
    private suspend fun walkAll(maxPages: Int): Walk {
        var rows: List<AdminQueueRow> = emptyList()
        var cursor: String? = null
        var pages = 0
        val seen = mutableSetOf<String>()
        while (pages < maxPages) {
            currentCoroutineContext().ensureActive()
            when (val page = api.queue(cursor, status = null)) {
                is AdminResult.Err -> return Walk.Failed(page.failure)
                is AdminResult.Ok -> {
                    rows = mergeRows(rows, page.value.items, inheritDisplay = true)
                    pages++
                    cursor = page.value.nextCursor
                    if (cursor == null) return Walk.Complete(rows)
                    if (!seen.add(cursor)) return Walk.Failed(AdminFailure.InvalidPayload)
                }
            }
        }
        return Walk.Incomplete // budget reached with cursor remaining — NOT complete
    }

    /**
     * Load up to [depth] pages from the start, always within the policy budget,
     * replacing the displayed rows while inheriting already-hydrated display labels
     * (and re-hydrating rows whose revision changed). Mid-walk failure keeps the
     * pages fetched so far and surfaces the failure inline — never a silent partial.
     */
    private suspend fun loadDepth(depth: Int) {
        mutate { it.copy(loading = true, failure = null) }
        try {
            var rows: List<AdminQueueRow> = emptyList()
            var cursor: String? = null
            var pages = 0
            var failure: AdminFailure? = null
            val seen = mutableSetOf<String>()
            while (pages < depth.coerceIn(1, policy.maxQueuePages)) {
                currentCoroutineContext().ensureActive()
                when (val page = api.queue(cursor, status = null)) {
                    is AdminResult.Err -> {
                        failure = page.failure
                        break
                    }
                    is AdminResult.Ok -> {
                        rows = mergeRows(rows, page.value.items, inheritDisplay = true)
                        pages++
                        cursor = page.value.nextCursor
                        if (cursor == null) break
                        if (!seen.add(cursor)) { failure = AdminFailure.InvalidPayload; break }
                    }
                }
            }
            val previous = _state.value
            if (failure != null) {
                // Failed reload: keep last-known rows, surface the failure inline.
                mutate { it.copy(failure = failure) }
                return
            }
            if (previous.pendingUpdates > 0 && cursor != null) {
                mutate { it.copy(failure = AdminFailure.Validation(400, "queue_scan_limit")) }
                return
            }
            val previousById = previous.rows.associateBy { it.id }
            val fresh = mutableListOf<String>()
            for (row in rows) {
                val old = previousById[row.id]
                val inherited = old?.display
                val displayChanged = old != null && old.revision != row.revision
                if (old == null || old.display == null) fresh += row.id
                else if (displayChanged && old.revision != null) fresh += row.id
                if (inherited != null && !displayChanged) {
                    rows = rows.map { if (it.id == row.id) it.copy(display = inherited) else it }
                }
            }
            if (cursor == null) baseline = snapshot(rows)
            mutate {
                it.copy(
                    loaded = true,
                    loading = false,
                    failure = failure,
                    rows = rows,
                    nextCursor = cursor,
                    pagesLoaded = pages,
                    pendingUpdates = 0, // an applied load re-baselines the pill
                )
            }
            hydrate(fresh)
            syncMarkedLocked(hydrateMissing = false)
        } finally {
            // Cancellation must not strand the loading flag.
            mutate { it.copy(loading = false) }
        }
    }

    private fun mergeRows(
        existing: List<AdminQueueRow>,
        incoming: List<AdminQueueItem>,
        inheritDisplay: Boolean,
    ): List<AdminQueueRow> {
        val merged = existing.associateByTo(linkedMapOf()) { it.id }
        for (item in incoming) {
            val old = merged[item.id]
            merged[item.id] = AdminQueueRow(item).copy(
                display = old?.display?.takeIf { inheritDisplay && old.revision == item.revision },
            )
        }
        return merged.values.toList()
    }

    /**
     * Bounded display hydration for rows lacking labels. Lenient for the BROWSE list:
     * a failed row keeps its verified contract fields only (id/status/revision stay
     * truthful); rate limiting stops the batch and surfaces [AdminHydration.LIMITED].
     * Budget is [SearchPolicy.MAX_HYDRATIONS] ids per action.
     */
    private suspend fun hydrate(ids: List<String>) {
        val targets = ids.distinct().filter { id ->
            _state.value.rows.any { it.id == id && it.display == null && it.status != null }
        }.take(SearchPolicy.MAX_HYDRATIONS)
        if (targets.isEmpty()) return
        mutate { it.copy(hydration = AdminHydration.RUNNING) }
        var limited = ids.distinct().size > SearchPolicy.MAX_HYDRATIONS
        try {
            for (id in targets) {
                currentCoroutineContext().ensureActive()
                when (val result = api.submission(id)) {
                    is AdminResult.Ok -> {
                        markers.clearResolved(result.value.id, result.value.status)
                        val detail = result.value
                        setRow(id) { it.copy(status = detail.status, revision = detail.revision,
                            display = AdminDisplay.from(detail.latest?.payload)) }
                    }
                    is AdminResult.Err -> {
                        if (result.failure is AdminFailure.RateLimited) {
                            limited = true
                            break
                        }
                        // Other display failures stay id-only (honest, retryable on refresh).
                    }
                }
            }
        } finally {
            mutate { s ->
                s.copy(
                    hydration = when {
                        s.hydration != AdminHydration.RUNNING -> s.hydration
                        limited -> AdminHydration.LIMITED
                        else -> AdminHydration.COMPLETE
                    },
                    rows = s.rows.map { it.copy(displayLoading = false) },
                )
            }
        }
    }

    /** Rebuild marker rows against current data; optionally hydrate unknown ids (bounded). */
    private suspend fun syncMarkedLocked(hydrateMissing: Boolean) {
        val ids = (markers.all(AdminMarkerKind.DEFERRED) + markers.all(AdminMarkerKind.RE_REVIEW))
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
        val loaded = _state.value.rows.associateBy { it.id }
        val existing = _state.value.marked.associateBy { it.id }
        val synced = ids.map { id -> loaded[id] ?: existing[id] ?: AdminQueueRow(id) }
        mutate { it.copy(marked = synced) }
        if (!hydrateMissing) return

        val budget = (SearchPolicy.MAX_MARKER_HYDRATIONS - markedAttempted.size).coerceAtLeast(0)
        val targets = synced.filter { it.status == null && it.id !in markedAttempted }.take(budget)
        for (row in targets) {
            currentCoroutineContext().ensureActive()
            when (val result = api.submission(row.id)) {
                is AdminResult.Ok -> {
                    markers.clearResolved(result.value.id, result.value.status)
                    markedAttempted += row.id
                    val fresh = AdminQueueRow(result.value)
                    mutate { s -> s.copy(marked = s.marked.map { if (it.id == row.id) fresh else it }) }
                }
                is AdminResult.Err -> {
                    // Id-only row stays (honest "unavailable"); do not retry-loop.
                    markedAttempted += row.id
                }
            }
        }
        // A confirmed terminal status may have removed markers just now.
        val stillMarked = ids.filter { it in markers.all(AdminMarkerKind.DEFERRED) || it in markers.all(AdminMarkerKind.RE_REVIEW) }
        if (stillMarked.size != ids.size) {
            mutate { s -> s.copy(marked = s.marked.filter { it.id in stillMarked }) }
        }
    }

    private fun setRow(id: String, transform: (AdminQueueRow) -> AdminQueueRow) {
        mutate { s ->
            s.copy(
                rows = s.rows.map { if (it.id == id) transform(it) else it },
                marked = s.marked.map { if (it.id == id) transform(it) else it },
            )
        }
    }

    private fun mutate(transform: (AdminQueueState) -> AdminQueueState) {
        _state.value = transform(_state.value)
    }

    companion object {
        const val FILTER_ALL = "all"
        const val FILTER_PENDING = "pending"
        const val FILTER_NEEDS_INFO = "needs_info"
        const val FILTER_RE_REVIEW = "re_review"
        const val FILTER_DEFERRED = "deferred"

        /** Deep-link normalization (dashboard tiles pass their enum name lowercased). */
        fun normalizeFilter(raw: String?): String = when (raw) {
            FILTER_PENDING, FILTER_NEEDS_INFO, FILTER_RE_REVIEW, FILTER_DEFERRED -> raw
            else -> FILTER_ALL
        }
    }
}
