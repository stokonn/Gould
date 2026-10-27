package com.opus.piano.admin

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Stage 7B.3 stats derivation gates (owner decision 2026-10-23):
 * exact counts ONLY from complete bounded walks; partial coverage is NEVER shown
 * as a total; failures map to honest per-tile unavailable states; no invented endpoint.
 */
class AdminStatsTest {

    private val todayWindow = 1_000_000L until 2_000_000L // [start, end)
    private val today = 1_500_000L
    private val yesterday = 900_000L

    /** Scripted transport: serves queued bodies per resource (queue/audit), in order. */
    private class ScriptedTransport(private val scripts: Map<String, List<Script>>) : AdminTransport {
        data class Script(val code: Int, val body: String)
        private val cursors = mutableMapOf<String, Int>()
        var callCount = 0
            private set
        override suspend fun call(
            method: String, url: String, headers: Map<String, String>, body: ByteArray?
        ): AdminHttpResponse {
            callCount++
            val resource = when {
                url.contains("/v1/admin/queue") -> "queue"
                url.contains("/v1/admin/audit") -> "audit"
                else -> "other"
            }
            val list = scripts[resource].orEmpty()
            val index = cursors.getOrPut(resource) { 0 }.coerceAtMost(list.lastIndex)
            cursors[resource] = index + 1
            val script = list[index] ?: return AdminHttpResponse(500, "missing script".toByteArray(), null)
            return AdminHttpResponse(script.code, script.body.toByteArray(), null)
        }
    }

    private fun item(id: String, status: String, created: Long = 1_100_000L): String =
        """{"id":"$id","owner":"u","status":"$status","revision":1,"evidenceCount":0,"createdAt":$created}"""

    private fun queuePage(items: String, next: String?) =
        """{"items":[$items]${next?.let { ""","nextCursor":"$it"""" } ?: ""}}"""

    private fun auditPage(events: String, next: String?) =
        """{"items":[$events]${next?.let { ""","nextCursor":"$it"""" } ?: ""},"chainHead":"h"}"""

    private fun event(seq: Long, ts: Long, action: String) =
        """{"seq":$seq,"ts":$ts,"actor":"m","action":"$action","subject":"submission:s","prevHash":"a","thisHash":"b"}"""

    private fun service(transport: AdminTransport, maxQueue: Int = 8, maxAudit: Int = 8): AdminStatsService =
        AdminStatsService(
            api = AdminApi(baseUrl = { "https://admin.example.test" }, transport = transport),
            maxQueuePages = maxQueue,
            maxAuditPages = maxAudit,
            dayWindow = { todayWindow },
        )

    private fun apiService(transport: AdminTransport): AdminStatsService =
        AdminStatsService(AdminApi(baseUrl = { "https://admin.example.test" }, transport = transport))

    private fun value(stats: AdminStats, tile: AdminTile): AdminTileValue = stats.tiles.getValue(tile)

    @Test
    fun completeWalksProduceExactCountsForAllFourTiles() = runBlocking {
        val transport = ScriptedTransport(
            mapOf(
                "queue" to listOf(
                    ScriptedTransport.Script(
                        200,
                        queuePage(
                            listOf(
                                item("a", "submitted"), item("b", "in_review"),
                                item("c", "in_review"), item("d", "changes_requested"),
                            ).joinToString(","),
                            "next1",
                        )
                    ),
                    ScriptedTransport.Script(
                        200,
                        queuePage(
                            listOf(item("e", "submitted"), item("f", "changes_requested")).joinToString(","),
                            null,
                        )
                    ),
                ),
                "audit" to listOf(
                    ScriptedTransport.Script(
                        200,
                        auditPage(
                            listOf(
                                event(1, yesterday, "decision.approve"), // outside today
                                event(2, today, "decision.approve"),
                                event(3, today, "decision.approve"),
                                event(4, today, "decision.reject"),
                                event(5, today, "decision.request_changes"), // not a today-count verb
                                event(6, today, "submission.created"),
                            ).joinToString(","),
                            null,
                        )
                    ),
                ),
            )
        )
        val stats = service(transport).load(today)
        assertEquals(AdminTileValue.Exact(4), value(stats, AdminTile.PENDING)) // a,b,c submitted/in_review + e
        assertEquals(AdminTileValue.Exact(2), value(stats, AdminTile.NEEDS_INFO)) // d,f
        assertEquals(AdminTileValue.Exact(2), value(stats, AdminTile.APPROVED_TODAY))
        assertEquals(AdminTileValue.Exact(1), value(stats, AdminTile.REJECTED_TODAY))
        assertTrue(stats.allExact)
        assertEquals(today, stats.updatedAt)
    }

    @Test
    fun incompleteQueueWalkNeverPresentsPartialCountAsTotal() = runBlocking {
        val transport = ScriptedTransport(
            mapOf(
                "queue" to listOf(
                    // Cursor still present after the budget's only page => incomplete.
                    ScriptedTransport.Script(200, queuePage(item("a", "submitted"), "next1")),
                ),
                "audit" to listOf(
                    ScriptedTransport.Script(200, auditPage(event(1, today, "decision.approve"), null)),
                ),
            )
        )
        val stats = service(transport, maxQueue = 1).load(today)
        assertEquals(AdminTileValue.Unavailable(AdminStatsService.REASON_BUDGET), value(stats, AdminTile.PENDING))
        assertEquals(AdminTileValue.Unavailable(AdminStatsService.REASON_BUDGET), value(stats, AdminTile.NEEDS_INFO))
        // Audit window complete => exact, independent of the queue.
        assertEquals(AdminTileValue.Exact(1), value(stats, AdminTile.APPROVED_TODAY))
        assertEquals(AdminTileValue.Exact(0), value(stats, AdminTile.REJECTED_TODAY))
    }

    @Test
    fun incompleteAuditWalkNeverPresentsPartialCountAsTotal() = runBlocking {
        val transport = ScriptedTransport(
            mapOf(
                "queue" to listOf(ScriptedTransport.Script(200, queuePage(item("a", "submitted"), null))),
                "audit" to listOf(
                    ScriptedTransport.Script(200, auditPage(event(1, today, "decision.approve"), "c2")),
                    ScriptedTransport.Script(200, auditPage(event(2, today, "decision.reject"), "c3")),
                ),
            )
        )
        val stats = service(transport, maxAudit = 2).load(today)
        assertEquals(AdminTileValue.Exact(1), value(stats, AdminTile.PENDING))
        assertEquals(
            AdminTileValue.Unavailable(AdminStatsService.REASON_BUDGET),
            value(stats, AdminTile.APPROVED_TODAY),
        )
        assertEquals(
            AdminTileValue.Unavailable(AdminStatsService.REASON_BUDGET),
            value(stats, AdminTile.REJECTED_TODAY),
        )
    }

    @Test
    fun unconfiguredServiceYieldsNotConfiguredOnEveryTile() = runBlocking {
        val transport = ScriptedTransport(emptyMap())
        val api = AdminApi(baseUrl = { "" }, transport = transport)
        val stats = AdminStatsService(api).load(today)
        AdminTile.entries.forEach { tile ->
            assertEquals(AdminTileValue.Unavailable("not_configured"), value(stats, tile))
        }
        assertEquals(0, transport.callCount)
    }

    @Test
    fun upstreamFailureMapsToHonestPerTileUnavailable() = runBlocking {
        val transport = ScriptedTransport(
            mapOf(
                "queue" to listOf(ScriptedTransport.Script(200, queuePage(item("a", "submitted"), null))),
                "audit" to listOf(ScriptedTransport.Script(503, """{"error":{"code":"maintenance","message":"down"}}""")),
            )
        )
        val stats = apiService(transport).load(today)
        assertEquals(AdminTileValue.Exact(1), value(stats, AdminTile.PENDING))
        assertEquals(AdminTileValue.Unavailable("server_error_503"), value(stats, AdminTile.APPROVED_TODAY))
        assertEquals(AdminTileValue.Unavailable("server_error_503"), value(stats, AdminTile.REJECTED_TODAY))
    }

    @Test
    fun budgetBoundsAreDocumentedPerPageLimits() {
        assertEquals(8, AdminStatsService.MAX_QUEUE_PAGES)
        assertEquals(8, AdminStatsService.MAX_AUDIT_PAGES)
        assertTrue(AdminStatsService.MAX_QUEUE_PAGES * AdminApi.QUEUE_PAGE_LIMIT >= 160)
    }
}
