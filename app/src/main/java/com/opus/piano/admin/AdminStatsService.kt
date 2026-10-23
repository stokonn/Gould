package com.opus.piano.admin

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * Dashboard statistics (Stage 7B.3) — owner-approved exact-or-unavailable derivation
 * (decision 2026-10-23, CONTRACT-AUDIT.md gap G1).
 *
 * Rules enforced here, not by convention:
 *  - A tile shows an EXACT count only when its source window was covered COMPLETELY
 *    by authorized server results within the bounded page budget.
 *  - A partial walk is NEVER presented as a total: the tile becomes
 *    [AdminTileValue.Unavailable] with a reason key.
 *  - No endpoint beyond the verified queue/audit listings is used. `GET /v1/admin/stats`
 *    (Stage 6 durable fix) must replace this derivation when it exists.
 *
 * Tile definitions (documented, non-overlapping):
 *  - Pending       = `submitted` + `in_review` items in the complete default queue walk.
 *  - Needs info    = `changes_requested` items in the SAME walk (UI "Needs info" label).
 *  - Approved today= `decision.approve` audit events with ts inside the local calendar
 *                    day, from a COMPLETE audit-chain walk.
 *  - Rejected today= `decision.reject` audit events, same window.
 *
 * Budget: ≤ [maxQueuePages] + ≤ [maxAuditPages] requests per refresh (page = ≤20 items),
 * manual refresh only — no polling. Pure logic over the contract client, JVM-testable.
 */
enum class AdminTile { PENDING, NEEDS_INFO, APPROVED_TODAY, REJECTED_TODAY }

sealed class AdminTileValue {
    /** Complete coverage: an exact, server-derived count. */
    data class Exact(val count: Int) : AdminTileValue()
    /** Honest unavailable state; [reasonKey] maps to a localized explanation. */
    data class Unavailable(val reasonKey: String) : AdminTileValue()
}

data class AdminStats(
    val tiles: Map<AdminTile, AdminTileValue>,
    /** Wall-clock ms of this derivation (0 when never loaded). */
    val updatedAt: Long,
) {
    val allExact: Boolean get() = tiles.values.all { it is AdminTileValue.Exact }
    companion object {
        fun unavailable(reasonKey: String, updatedAt: Long = 0L) = AdminStats(
            tiles = AdminTile.entries.associateWith { AdminTileValue.Unavailable(reasonKey) },
            updatedAt = updatedAt,
        )
    }
}

class AdminStatsService(
    private val api: AdminApi,
    private val maxQueuePages: Int = MAX_QUEUE_PAGES,
    private val maxAuditPages: Int = MAX_AUDIT_PAGES,
    /** Returns the half-open local-day window [startMillis, endMillis) containing [now]. */
    private val dayWindow: (now: Long) -> LongRange = { now ->
        val zone = java.time.ZoneId.systemDefault()
        val today = java.time.Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        val start = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        start until end
    },
) {
    suspend fun load(now: Long): AdminStats {
        val queue = queueTiles()
        val audit = auditTiles(now)
        val tiles = LinkedHashMap<AdminTile, AdminTileValue>()
        tiles[AdminTile.PENDING] = queue.first
        tiles[AdminTile.NEEDS_INFO] = queue.second
        tiles[AdminTile.APPROVED_TODAY] = audit.first
        tiles[AdminTile.REJECTED_TODAY] = audit.second
        return AdminStats(tiles, now)
    }

    /** Complete bounded walk of the default queue set (submitted+in_review+changes_requested). */
    private suspend fun queueTiles(): Pair<AdminTileValue, AdminTileValue> {
        var pending = 0
        var needsInfo = 0
        var cursor: String? = null
        var pages = 0
        while (true) {
            currentCoroutineContext().ensureActive()
            if (pages >= maxQueuePages) return budget() // incomplete => never partial-as-total
            when (val result = api.queue(cursor, status = null)) {
                is AdminResult.Err -> return failure(result.failure)
                is AdminResult.Ok -> {
                    for (item in result.value.items) {
                        when (item.status) {
                            AdminStatus.SUBMITTED, AdminStatus.IN_REVIEW -> pending++
                            AdminStatus.CHANGES_REQUESTED -> needsInfo++
                            else -> { /* terminal rows are not in the default set */ }
                        }
                    }
                    pages++
                    cursor = result.value.nextCursor ?: break
                }
            }
        }
        return AdminTileValue.Exact(pending) to AdminTileValue.Exact(needsInfo)
    }

    /** Complete bounded walk of the append-only audit chain, filtered to local "today". */
    private suspend fun auditTiles(now: Long): Pair<AdminTileValue, AdminTileValue> {
        val window = dayWindow(now)
        var approved = 0
        var rejected = 0
        var cursor: String? = null
        var pages = 0
        while (true) {
            currentCoroutineContext().ensureActive()
            if (pages >= maxAuditPages) return budget()
            when (val result = api.audit(cursor)) {
                is AdminResult.Err -> return failure(result.failure)
                is AdminResult.Ok -> {
                    for (event in result.value.items) {
                        if (event.ts !in window) continue
                        when (event.action) {
                            "decision.approve" -> approved++
                            "decision.reject" -> rejected++
                            else -> {}
                        }
                    }
                    pages++
                    cursor = result.value.nextCursor ?: break
                }
            }
        }
        return AdminTileValue.Exact(approved) to AdminTileValue.Exact(rejected)
    }

    private fun failure(failure: AdminFailure): Pair<AdminTileValue, AdminTileValue> =
        AdminTileValue.Unavailable(failure.key) to AdminTileValue.Unavailable(failure.key)

    private fun budget(): Pair<AdminTileValue, AdminTileValue> =
        AdminTileValue.Unavailable(REASON_BUDGET) to AdminTileValue.Unavailable(REASON_BUDGET)

    companion object {
        /** ≤160 queue items per refresh at PAGE_LIMIT=20. */
        const val MAX_QUEUE_PAGES = 8
        /** ≤160 audit events per refresh; longer chains => explicit unavailable. */
        const val MAX_AUDIT_PAGES = 8
        const val REASON_BUDGET = "stats_budget_exceeded"
    }
}
