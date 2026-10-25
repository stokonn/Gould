package com.opus.piano.data.community.canonical

import com.opus.piano.data.community.CommunityProblem
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class CanonicalAdminClientTest {
    private val root = generateSequence(File(requireNotNull(System.getProperty("user.dir")))) { it.parentFile }
        .first { File(it, "contracts/admin/shared/v1/contract.json").exists() }
    private val codec = AdminContract(File(root, "contracts/admin/v1/decision-proposal/dto.schema.json").readText())
    private val captures = JSONObject(File(root, "spikes/stage6-api-v1-local/results/http-pg/pg-ojxz4mbi/shared-contract-captures.json").readText()).getJSONArray("captures")
    private fun capture(dto: String, predicate: (JSONObject) -> Boolean = { true }): JSONObject =
        (0 until captures.length()).map { captures.getJSONObject(it) }.first { it.optString("dto") == dto && predicate(it.getJSONObject("response")) }.getJSONObject("response").let { JSONObject(it.toString()) }
    private fun raw(dto: String) = capture(dto).toString()
    private fun token(session: String? = null): String = capture("Tokens").also { session?.let { s -> it.put("sessionId", s) } }.toString()
    private suspend fun authenticate(client: CanonicalAdminClient) = client.login("synthetic-challenge", "synthetic-credential")
    private fun expect(problem: CommunityProblem, action: suspend () -> Unit) = runBlocking {
        try { action(); fail("Expected $problem") } catch (e: AdminClientException) { assertEquals(problem, e.failure.problem) }
    }

    @Test fun actualAcceptedHttpPostgresCapturesValidateInKotlin() {
        var count = 0
        for (i in 0 until captures.length()) {
            val c = captures.getJSONObject(i)
            if (!c.has("dto") || c.isNull("response")) continue
            val dto = c.getString("dto")
            if (dto == "Empty204") continue
            codec.decode(dto, c.getJSONObject("response").toString())
            count++
        }
        assertTrue("Must replay actual scene corpus", count >= 50)
    }

    @Test fun strictWireRejectsLegacyNumbersUnknownRequestsAndInvalidJson() {
        val queue = capture("QueuePage")
        queue.getJSONArray("items").getJSONObject(0).put("submissionRevision", 1)
        assertTrue(runCatching { codec.decode("QueuePage", queue.toString()) }.isFailure)
        val open = JSONObject().put("schemaVersion", 1).put("expectedEntityRevision", "0").put("expectedSubmissionRevision", "9223372036854775807")
            .put("idempotencyKey", "00000000-0000-4000-8000-000000000001")
        codec.decode("OpenRequest", open.toString(), true)
        open.put("expectedRevision", 1)
        assertTrue(runCatching { codec.decode("OpenRequest", open.toString(), true) }.isFailure)
        for (bad in listOf("{'schemaVersion':1}", "{schemaVersion:1}", "{\"schemaVersion\":1,}", "{\"schemaVersion\":01}", "{\"schemaVersion\":1}[]", "{\"schemaVersion\":1,\"schemaVersion\":1}")) {
            assertTrue(bad, runCatching { codec.decode("EmptyRequest", bad, true) }.isFailure)
        }
    }

    @Test fun fullInt8MicrosecondsNullsAndAdditiveResponsesRemainExact() {
        val queue = capture("QueuePage")
        val item = queue.getJSONArray("items").getJSONObject(0)
        item.put("submissionRevision", "9223372036854775807").put("createdAt", "2026-10-25T08:00:00.123456Z")
        item.getJSONObject("display").put("title", JSONObject.NULL).put("composer", JSONObject.NULL)
        queue.put("futureMember", true)
        val decoded = codec.decode("QueuePage", queue.toString()).json()
        assertEquals("9223372036854775807", decoded.getJSONArray("items").getJSONObject(0).getString("submissionRevision"))
        assertTrue(decoded.getJSONArray("items").getJSONObject(0).getJSONObject("display").isNull("title"))
        assertEquals("2026-10-25T08:00:00.123456Z", decoded.getJSONArray("items").getJSONObject(0).getString("createdAt"))
        item.put("submissionRevision", "9223372036854775808")
        assertTrue(runCatching { codec.decode("QueuePage", queue.toString()) }.isFailure)
        item.put("submissionRevision", "1").put("createdAt", "2026-02-30T08:00:00.123456Z")
        assertTrue(runCatching { codec.decode("QueuePage", queue.toString()) }.isFailure)
    }

    @Test fun reasonUsesUtf8BytesAndDocumentDoesNotExposeMutableOrSecretState() {
        val req = JSONObject().put("schemaVersion", 1).put("expectedEntityRevision", "0").put("expectedSubmissionRevision", "1")
            .put("idempotencyKey", "00000000-0000-4000-8000-000000000001").put("decision", "reject").put("reason", "ž".repeat(500))
        codec.decode("RejectRequest", req.toString(), true)
        req.put("reason", "ž".repeat(501))
        assertTrue(runCatching { codec.decode("RejectRequest", req.toString(), true) }.isFailure)
        val doc = codec.decode("Tokens", token())
        doc.json().put("accessToken", "changed")
        assertNotEquals("changed", doc.json().getString("accessToken"))
        assertFalse(doc.toString().contains("SYNTHETIC"))
        assertEquals("AdminResponse(status=200)", AdminResponse(200, "secret").toString())
    }

    @Test fun logoutClearsBeforeIoAndAcceptsOnlyEmpty204() = runBlocking {
        lateinit var client: CanonicalAdminClient
        client = CanonicalAdminClient(codec, CanonicalAdminTransport { method, _, _, _ ->
            if (method == "DELETE") { assertFalse(client.authenticated); AdminResponse(204, "") }
            else AdminResponse(201, token())
        })
        authenticate(client)
        assertEquals(AdminLogout(true, true), client.logout())
        assertFalse(client.authenticated)
        assertEquals(AdminLogout(true, false), client.logout())
    }

    @Test fun concurrentRefreshCoalescesAndRotatesIdentity() = runBlocking {
        val entered = CompletableDeferred<Unit>(); val release = CompletableDeferred<Unit>(); var refreshes = 0
        val client = CanonicalAdminClient(codec, CanonicalAdminTransport { _, path, _, _ ->
            if (path.endsWith("refresh")) { refreshes++; entered.complete(Unit); release.await(); AdminResponse(200, token("00000000-0000-4000-8000-000000000002")) }
            else AdminResponse(201, token())
        })
        authenticate(client)
        val a = async { client.refresh() }; entered.await()
        val b = async { client.refresh() }; yield(); release.complete(Unit)
        a.await(); b.await(); assertEquals(1, refreshes); assertTrue(client.authenticated)
    }

    @Test fun lostRefreshResponseRequiresFreshAuthentication() = runBlocking {
        val client = CanonicalAdminClient(codec, CanonicalAdminTransport { _, path, _, _ ->
            if (path.endsWith("refresh")) throw java.io.IOException("synthetic transport loss")
            AdminResponse(201, token())
        })
        authenticate(client)
        assertTrue(runCatching { client.refresh() }.isFailure)
        assertFalse(client.authenticated)
    }

    @Test fun staleAccountResponseCannotReplaceNewIdentity() = runBlocking {
        val entered = CompletableDeferred<Unit>(); val release = CompletableDeferred<Unit>()
        val client = CanonicalAdminClient(codec, CanonicalAdminTransport { method, _, _, _ ->
            if (method == "GET") { entered.complete(Unit); release.await(); AdminResponse(200, raw("Session")) }
            else AdminResponse(201, token())
        })
        authenticate(client)
        val stale = async { runCatching { client.session() } }; entered.await()
        client.clear(); authenticate(client); release.complete(Unit)
        val error = stale.await().exceptionOrNull() as AdminClientException
        assertEquals(CommunityProblem.OWNERSHIP, error.failure.problem)
        assertTrue(client.authenticated)
    }

    @Test fun httpStatusesKeepDistinctErrorsAndNeverInventStats() = runBlocking {
        var status = 503
        val client = CanonicalAdminClient(codec, CanonicalAdminTransport { method, _, _, _ ->
            if (method == "POST") AdminResponse(201, token()) else AdminResponse(status, "untrusted error body", "10")
        }, now = { 1000 })
        val cases = mapOf(400 to CommunityProblem.VALIDATION, 401 to CommunityProblem.UNAUTHENTICATED, 403 to CommunityProblem.FORBIDDEN,
            404 to CommunityProblem.NOT_FOUND, 409 to CommunityProblem.CONFLICT, 422 to CommunityProblem.IDEMPOTENCY_MISMATCH,
            429 to CommunityProblem.RATE_LIMITED, 503 to CommunityProblem.UNAVAILABLE)
        for ((code, kind) in cases) {
            authenticate(client); status = code
            val error = runCatching { client.statistics() }.exceptionOrNull() as AdminClientException
            assertEquals(kind, error.failure.problem)
            if (code == 429) assertEquals(11000L, error.failure.retryAtMillis)
            if (code == 401) assertFalse(client.authenticated)
        }
    }

    private class ReviewVenue(val fixture: (String) -> String) : CanonicalAdminTransport {
        var failDecision = true
        var conflict = false
        var revoked = false
        var decisionBody: String? = null
        var postCount = 0
        override suspend fun exchange(method: String, path: String, body: String?, accessToken: String?): AdminResponse = when {
            path == "/v1/session" && method == "POST" -> AdminResponse(201, fixture("Tokens"))
            path.endsWith("capabilities") -> if (revoked) AdminResponse(403, "") else AdminResponse(200, fixture("Capabilities"))
            path.contains("comparison?") -> AdminResponse(200, fixture("Comparison"))
            method == "GET" -> AdminResponse(200, fixture("Detail"))
            else -> {
                postCount++
                if (decisionBody == null) decisionBody = body else assertEquals(decisionBody, body)
                if (failDecision) throw java.io.IOException("lost decision response")
                if (conflict) AdminResponse(409, "") else AdminResponse(201, fixture("ApproveResult"))
            }
        }
    }
    private fun reviewFixture(dto: String): String {
        val tokens = capture("Tokens")
        val detail = capture("Detail").put("status", "in_review").put("expectedEntityRevision", "1")
        val comparison = capture("Comparison").put("submissionId", detail.getString("submissionId"))
            .put("entityId", detail.getString("targetEntityId")).put("entityStatus", "published").put("entityRevision", detail.getString("expectedEntityRevision"))
        if (dto == "Tokens") return tokens.toString()
        if (dto == "Detail") return detail.toString()
        if (dto == "Comparison") return comparison.toString()
        if (dto == "Capabilities") return capture(dto).put("principalId", tokens.getString("principalId")).put("sessionId", tokens.getString("sessionId")).toString()
        return capture(dto).put("submissionId", detail.getString("submissionId")).toString()
    }

    @Test fun lostDecisionResponseReplaysExactBodyOnlyOnExplicitAction() = runBlocking {
        val venue = ReviewVenue(::reviewFixture); val client = CanonicalAdminClient(codec, venue)
        authenticate(client); val id = JSONObject(reviewFixture("Detail")).getString("submissionId")
        val review = client.review(id)
        assertTrue(runCatching { client.confirm(review, "approve", acknowledged = true) }.isFailure)
        assertEquals(1, venue.postCount); assertTrue(client.hasUncertainDecision)
        venue.failDecision = false
        client.retryPending()
        assertEquals(2, venue.postCount); assertFalse(client.hasUncertainDecision)
    }

    @Test fun conflictDropsIntentAndNeverAutomaticallyRetries() = runBlocking {
        val venue = ReviewVenue(::reviewFixture).also { it.failDecision = false; it.conflict = true }
        val client = CanonicalAdminClient(codec, venue); authenticate(client)
        val review = client.review(JSONObject(reviewFixture("Detail")).getString("submissionId"))
        val failure = runCatching { client.confirm(review, "approve", acknowledged = true) }.exceptionOrNull() as AdminClientException
        assertEquals(CommunityProblem.CONFLICT, failure.failure.problem)
        assertFalse(client.hasUncertainDecision); assertEquals(1, venue.postCount)
    }

    @Test fun revocationCancelsUncertainIntentBeforeAnyReplayPost() = runBlocking {
        val venue = ReviewVenue(::reviewFixture); val client = CanonicalAdminClient(codec, venue); authenticate(client)
        val review = client.review(JSONObject(reviewFixture("Detail")).getString("submissionId"))
        runCatching { client.confirm(review, "approve", acknowledged = true) }
        venue.revoked = true
        assertTrue(runCatching { client.retryPending() }.isFailure)
        assertEquals(1, venue.postCount); assertFalse(client.hasUncertainDecision)
    }
    @Test fun actualAuditHashesAndFieldsVerifyAndTamperingIsDenied() {
        val page = codec.decode("AuditPage", raw("AuditPage"))
        AdminAuditVerifier().accept(page)
        val changed = page.json()
        changed.getJSONArray("items").getJSONObject(0).put("action", "session.logout")
        assertTrue(runCatching { AdminAuditVerifier().accept(codec.decode("AuditPage", changed.toString())) }.isFailure)
        val hashChanged = page.json()
        hashChanged.getJSONArray("items").getJSONObject(0).put("thisHash", "a".repeat(64))
        assertTrue(runCatching { AdminAuditVerifier().accept(codec.decode("AuditPage", hashChanged.toString())) }.isFailure)
    }

    @Test fun auditWalkRejectsChangedSnapshotAndCannotCrossAccountGeneration() = runBlocking {
        val page = capture("AuditPage").put("nextCursor", "YWJj")
        var get = 0
        val client = CanonicalAdminClient(codec, CanonicalAdminTransport { method, _, _, _ ->
            if (method == "POST") AdminResponse(201, token())
            else { get++; AdminResponse(200, page.toString()) }
        })
        authenticate(client)
        val walk = client.auditWalk()
        walk.next()
        page.put("headHash", "b".repeat(64))
        assertTrue(runCatching { walk.next() }.isFailure)
        client.clear(); authenticate(client)
        val before = get
        assertTrue(runCatching { walk.next() }.isFailure)
        assertEquals(before, get)
    }

    @Test fun unavailableStatisticsAndQueueFiltersStayCanonical() = runBlocking {
        var lastPath = ""
        val client = CanonicalAdminClient(codec, CanonicalAdminTransport { method, path, _, _ ->
            lastPath = path
            if (method == "POST") AdminResponse(201, token())
            else if (path.startsWith("/v1/admin/queue")) AdminResponse(200, raw("QueuePage"))
            else AdminResponse(200, JSONObject().put("schemaVersion",1).put("requestId","00000000-0000-4000-8000-000000000001")
                .put("asOf","2026-10-25T08:00:00.000000Z").put("state","unavailable").put("reason","service_unavailable").toString())
        })
        authenticate(client)
        assertEquals("StatsUnavailable", client.statistics().dto)
        client.queue(mapOf("q" to "Bach & son", "limit" to "20"))
        assertTrue(lastPath.contains("q=Bach+%26+son"))
        assertTrue(runCatching { client.queue(mapOf("unknown" to "1")) }.isFailure)
        assertTrue(runCatching { client.queue(mapOf("limit" to "21")) }.isFailure)
    }

    @Test fun uncertainIntentExpiresWithoutAnotherPostAndOfflineIsTyped() = runBlocking {
        var clock = 0L
        val venue = ReviewVenue(::reviewFixture)
        val client = CanonicalAdminClient(codec, venue, now = { clock }); authenticate(client)
        val review = client.review(JSONObject(reviewFixture("Detail")).getString("submissionId"))
        val loss = runCatching { client.confirm(review, "approve", acknowledged = true) }.exceptionOrNull() as AdminClientException
        assertEquals(CommunityProblem.OFFLINE, loss.failure.problem)
        clock = 86400000L
        assertTrue(runCatching { client.retryPending() }.isFailure)
        assertEquals(1, venue.postCount); assertFalse(client.hasUncertainDecision)
    }

    @Test fun invalidUnicodeAndLeapSecondAreRejectedWithoutWeakeningSchema() {
        val doc = capture("Tokens").put("accessToken", "\ud800")
        assertTrue(runCatching { codec.decode("Tokens", doc.toString()) }.isFailure)
        doc.put("accessToken", "synthetic").put("expiresAt", "2026-10-25T08:00:60.123456Z")
        assertTrue(runCatching { codec.decode("Tokens", doc.toString()) }.isFailure)
    }

    @Test fun responseIdentityMismatchClearsAuthentication() = runBlocking {
        val wrong = capture("Session").put("principalId", "00000000-0000-4000-8000-000000000001")
        val client = CanonicalAdminClient(codec, CanonicalAdminTransport { method, _, _, _ ->
            if (method == "POST") AdminResponse(201, token()) else AdminResponse(200, wrong.toString())
        })
        authenticate(client)
        val error = runCatching { client.session() }.exceptionOrNull() as AdminClientException
        assertEquals(CommunityProblem.OWNERSHIP, error.failure.problem)
        assertFalse(client.authenticated)
    }

    @Test fun changedReviewOrMissingAcknowledgementCannotPublish() = runBlocking {
        var changed = false
        val venue = ReviewVenue { dto ->
            if (dto == "Detail" && changed) JSONObject(reviewFixture(dto)).put("submissionRevision", "2").toString()
            else reviewFixture(dto)
        }
        val client = CanonicalAdminClient(codec, venue); authenticate(client)
        val review = client.review(JSONObject(reviewFixture("Detail")).getString("submissionId"))
        assertTrue(runCatching { client.confirm(review, "approve") }.isFailure)
        changed = true
        val failure = runCatching { client.confirm(review, "approve", acknowledged = true) }.exceptionOrNull() as AdminClientException
        assertEquals(CommunityProblem.CONFLICT, failure.failure.problem)
        assertEquals(0, venue.postCount)
    }

}
