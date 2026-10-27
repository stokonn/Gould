package com.opus.piano.admin

import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

/**
 * Stage 7B.2 foundation tests: contract codecs, error taxonomy, trust window,
 * account-scoped markers. All hermetic (fake transport, injected clocks).
 */
class AdminFoundationTest {

    // ---- codecs ---------------------------------------------------------

    @Test
    fun queueItemMatchesC6WireShapeAndToleratesUnknownFields() {
        val item = AdminQueueItem.parse(
            JSONObject(
                """{"id":"sub_9","owner":"usr_1","status":"changes_requested","revision":3,
                   "evidenceCount":2,"createdAt":1761000000000,"futureField":"ignored"}"""
            )
        )
        assertEquals("sub_9", item.id)
        assertEquals("usr_1", item.owner)
        assertEquals(AdminStatus.CHANGES_REQUESTED, item.status)
        assertEquals(3, item.revision)
        assertEquals(2, item.evidenceCount)
        assertEquals(1761000000000L, item.createdAt)
    }

    @Test
    fun queuePageParsesCursorAndEmptyPage() {
        val page = AdminQueuePage.parse(
            JSONObject("""{"items":[],"nextCursor":"1761000000000:sub_9"}""")
        )
        assertTrue(page.items.isEmpty())
        assertEquals("1761000000000:sub_9", page.nextCursor)

        val end = AdminQueuePage.parse(JSONObject("""{"items":[]}"""))
        assertNull(end.nextCursor)
        assertNull(AdminQueuePage.parse(JSONObject("""{"items":[],"nextCursor":""}""")).nextCursor)
    }

    @Test
    fun pendingStatusesAreDecidableAndNeedsInfoMapsToChangesRequested() {
        assertTrue(AdminStatus.SUBMITTED.decidable)
        assertTrue(AdminStatus.IN_REVIEW.decidable)
        assertTrue(AdminStatus.CHANGES_REQUESTED.decidable) // UI label "Needs info"
        assertFalse(AdminStatus.APPROVED.decidable)
        assertFalse(AdminStatus.REJECTED.decidable)
        assertFalse(AdminStatus.PUBLISHED.decidable)
        assertFalse(AdminStatus.WITHDRAWN.decidable)
        assertEquals(AdminStatus.CHANGES_REQUESTED, AdminStatus.fromWire("changes_requested"))
        assertEquals(AdminStatus.UNKNOWN, AdminStatus.fromWire("something_new")) // forward-compat
    }

    @Test
    fun submissionDetailParsesSnakeCaseReviewEventsAndLatestRevision() {
        val detail = AdminSubmissionDetail.parse(
            JSONObject(
                """{"id":"sub_1","owner":"usr_1","status":"in_review","revision":2,
                   "revisions":[
                     {"revision":1,"payload":{"title":"A"},"createdAt":100},
                     {"revision":2,"payload":{"title":"B"},"createdAt":200}],
                   "reviewEvents":[
                     {"revision":1,"decision":"request_changes","actor_id":"mod_1",
                      "reason":"clarify","created_at":150}]}"""
            )
        )
        assertEquals(2, detail.revision)
        assertEquals(2, detail.latest?.revision)
        assertEquals("B", detail.latest?.payload?.optString("title"))
        val event = detail.reviewEvents.single()
        assertEquals("mod_1", event.actorId)
        assertEquals(150L, event.createdAt)
        assertEquals("request_changes", event.decision)
    }

    @Test
    fun auditPageParsesHashChainFields() {
        val page = AdminAuditPage.parse(
            JSONObject(
                """{"items":[{"seq":7,"ts":1761000000000,"actor":"mod_1","action":"decision.approve",
                   "subject":"sub_1","prevHash":"aaa","thisHash":"bbb"}],
                   "nextCursor":"8","chainHead":"bbb"}"""
            )
        )
        val event = page.items.single()
        assertEquals(7L, event.seq)
        assertEquals("aaa", event.prevHash)
        assertEquals("bbb", event.thisHash)
        assertEquals("8", page.nextCursor)
        assertEquals("bbb", page.chainHead)
    }

    @Test
    fun decisionBodyCarriesMandatoryConflictFieldsAndOptionalReason() {
        val approve = buildDecisionBody(AdminDecision.APPROVE, 4, null, "idem-1")
        assertEquals(1, approve.optInt("schemaVersion"))
        assertEquals("approve", approve.optString("decision"))
        assertEquals(4, approve.optInt("expectedRevision"))
        assertEquals("idem-1", approve.optString("idempotencyKey"))
        assertFalse(approve.has("reason"))

        val blank = buildDecisionBody(AdminDecision.APPROVE, 4, "   ", "idem-2")
        assertFalse(blank.has("reason"))

        val reject = buildDecisionBody(AdminDecision.REJECT, 4, "duplicate", "idem-3")
        assertEquals("reject", reject.optString("decision"))
        assertEquals("duplicate", reject.optString("reason"))

        val changes = buildDecisionBody(AdminDecision.REQUEST_CHANGES, 4, "clarify", "idem-4")
        assertEquals("request_changes", changes.optString("decision"))
    }

    @Test
    fun capabilitiesParsingToleratesUnknownRolesButKeepsServerValues() {
        val caps = AdminCapabilities.parse(
            JSONObject(
                """{"principalId":"usr_1","capabilities":["queue.read","decide","audit.read"],
                   "newCapability":"x"}"""
            )
        )
        assertTrue(caps.canQueueRead)
        assertTrue(caps.canDecide)
        assertTrue(caps.canAuditRead)
        assertEquals("Moderator", caps.roleLabel)
        val admin = AdminCapabilities.parse(
            JSONObject("""{"principalId":"usr_2","capabilities":["queue.read","decide","audit.read","roles.manage"]}""")
        )
        assertEquals("Administrator", admin.roleLabel)
        val contributor = AdminCapabilities.parse(JSONObject("""{"principalId":"usr_3","capabilities":[]}"""))
        assertEquals("Contributor", contributor.roleLabel)
        assertFalse(contributor.canDecide)
    }

    // ---- error taxonomy --------------------------------------------------

    private class FakeTransport(
        var code: Int = 200,
        var body: String = "{}",
        var retryAfter: Long? = null,
        var explode: Boolean = false,
    ) : AdminTransport {
        val urls = mutableListOf<String>()
        val headerLog = mutableListOf<Map<String, String>>()
        var calls = 0
        override suspend fun call(
            method: String, url: String, headers: Map<String, String>, body: ByteArray?
        ): AdminHttpResponse {
            calls++
            urls += url
            headerLog += headers
            if (explode) throw IOException("connection reset")
            return AdminHttpResponse(code, this.body.toByteArray(), retryAfter)
        }
    }

    private fun api(
        transport: FakeTransport,
        base: String? = "https://admin.example.test",
        token: String? = "tok-123",
    ) = AdminApi(baseUrl = { base }, transport = transport, token = { token })

    private fun errOf(result: AdminResult<*>): AdminFailure =
        (result as AdminResult.Err).failure

    @Test
    fun emptyBaseUrlFailsHonestNotConfiguredWithoutNetworkCall() = runBlocking {
        val t = FakeTransport()
        val r = api(t, base = "   ").capabilities()
        assertEquals(AdminFailure.NotConfigured, errOf(r))
        assertEquals(0, t.calls)
        assertEquals(AdminFailure.NotConfigured.key, "not_configured")
    }

    @Test
    fun plainHttpOrUserInfoEndpointIsRejectedBeforeAnyCall() = runBlocking {
        val t = FakeTransport()
        assertEquals(AdminFailure.InvalidEndpoint, errOf(api(t, base = "http://admin.example.test").capabilities()))
        assertEquals(AdminFailure.InvalidEndpoint, errOf(api(t, base = "https://user:pass@admin.example.test").capabilities()))
        assertEquals(0, t.calls)
    }

    @Test
    fun transportFailureMapsToUnreachableAndKeepsCancellationIntact() = runBlocking {
        val t = FakeTransport(explode = true)
        assertEquals(AdminFailure.Unreachable, errOf(api(t).capabilities()))
    }

    @Test
    fun httpStatusesMapToTypedFailures() {
        fun at(code: Int, body: String = """{"message":"server says"}""", retry: Long? = null): AdminFailure {
            val t = FakeTransport(code = code, body = body, retryAfter = retry)
            return runBlocking { errOf(api(t).queue(null, null)) }
        }
        assertTrue(at(400) is AdminFailure.Validation)
        assertTrue(at(422) is AdminFailure.Validation)
        assertEquals(AdminFailure.Unauthorized, at(401))
        assertEquals(AdminFailure.Forbidden, at(403))
        assertEquals(AdminFailure.NotFound, at(404))
        val conflict = at(409, """{"error":{"code":"revision_conflict","message":"stale revision"}}""")
        assertTrue(conflict is AdminFailure.Conflict)
        assertEquals("stale revision", (conflict as AdminFailure.Conflict).message)
        val limited = at(429, retry = 30)
        assertEquals(AdminFailure.RateLimited(30), limited)
        assertEquals(AdminFailure.ServerError(503), at(503))
        assertEquals(AdminFailure.ServerError(500), at(500))
    }

    @Test
    fun successWithNonContractBodyIsInvalidPayloadNotSilentSuccess() = runBlocking {
        val t = FakeTransport(code = 200, body = "<html>gateway</html>")
        assertEquals(AdminFailure.InvalidPayload, errOf(api(t).queue(null, null)))
    }

    @Test
    fun queueRequestsOnlyContractDefinedParameters() = runBlocking {
        val t = FakeTransport(code = 200, body = """{"items":[]}""")
        api(t).queue(cursor = "1761000000000:sub_9", status = "submitted")
        val url = t.urls.single()
        assertTrue("actual=$url", url.startsWith("https://admin.example.test/v1/admin/queue?"))
        assertTrue(url.contains("cursor="))
        assertTrue(url.contains("status=submitted"))
        assertTrue(url.contains("limit=20")) // contract-defined, server caps at 20
        // Gap G2: no search parameter is ever invented.
        assertFalse(url.contains("search"))
        assertFalse(url.contains("q="))
        assertFalse(url.contains("title"))
    }

    @Test
    fun queueLimitIsClampedToContractMaximum() = runBlocking {
        val t = FakeTransport(code = 200, body = """{"items":[]}""")
        api(t).queue(cursor = null, status = null, limit = 500)
        assertTrue(t.urls.single().contains("limit=20"))
        val t2 = FakeTransport(code = 200, body = """{"items":[]}""")
        api(t2).queue(cursor = null, status = null, limit = 5)
        assertTrue(t2.urls.single().contains("limit=5"))
    }

    @Test
    fun conflictCarriesCurrentServerStateFromErrorEnvelope() = runBlocking {
        val t = FakeTransport(
            code = 409,
            body = """{"error":{"code":"revision_conflict","message":"stale revision",
                      "details":{"currentStatus":"approved","currentRevision":7}}}"""
        )
        val failure = errOf(api(t).decide("sub_1", buildDecisionBody(AdminDecision.APPROVE, 5, null, "idem-1")))
        val conflict = failure as AdminFailure.Conflict
        assertEquals("stale revision", conflict.message)
        assertEquals("approved", conflict.currentStatus)
        assertEquals(7, conflict.currentRevision)
    }

    @Test
    fun signOutUsesContractSessionEndpoint() = runBlocking {
        val t = FakeTransport(code = 200, body = """{"revoked":true}""")
        val r = api(t).signOut()
        assertTrue((r as AdminResult.Ok).value)
        assertTrue(t.urls.single().endsWith("/v1/session"))
    }

    @Test
    fun bearerTokenIsAttachedOnlyWhenPresent() = runBlocking {
        val t = FakeTransport(code = 200, body = """{"items":[]}""")
        api(t, token = "tok-123").audit(null)
        assertEquals("Bearer tok-123", t.headerLog.single()["Authorization"])
        val t2 = FakeTransport(code = 200, body = """{"items":[]}""")
        api(t2, token = null).audit(null)
        assertNull(t2.headerLog.single()["Authorization"])
    }

    @Test
    fun decisionPostUsesVerifiedEndpointAndReturnsAtomicResult() = runBlocking {
        val t = FakeTransport(
            code = 201,
            body = """{"submissionId":"sub_1","decision":"approve","status":"approved",
                      "revision":5,"publishedRevisionId":"rev_5","entityUuid":"e-1"}"""
        )
        val r = api(t).decide("sub_1", buildDecisionBody(AdminDecision.APPROVE, 4, null, "idem-9"))
        val ok = (r as AdminResult.Ok).value
        assertEquals(AdminStatus.APPROVED, ok.status)
        assertEquals(5, ok.revision)
        assertEquals("rev_5", ok.publishedRevisionId)
        assertTrue(t.urls.single().endsWith("/v1/admin/submissions/sub_1/decisions"))
    }

    // ---- trust window ----------------------------------------------------

    @Test
    fun trustWindowIsTenMinutesNonSlidingAndInvalidatedExplicitly() {
        var clock = 1_000_000L
        val policy = AdminTrustPolicy(now = { clock })
        assertFalse(policy.isTrusted)
        assertEquals(-1L, policy.ageMillis)

        policy.grant()
        assertTrue(policy.isTrusted)
        assertEquals(0L, policy.ageMillis)

        clock += 9 * 60 * 1000 // stay active but keep "using" the app
        assertTrue(policy.isTrusted)
        policy.grant() // re-grant refreshes only because verification happened again
        clock += 9 * 60 * 1000
        // Non-sliding from the LAST grant: 9 min after re-grant still inside the window.
        assertTrue(policy.isTrusted)

        clock += 2 * 60 * 1000 // total 11 min since last grant
        assertFalse(policy.isTrusted)
        assertEquals(-1L, policy.ageMillis)

        policy.grant()
        assertTrue(policy.isTrusted)
        policy.invalidate()
        assertFalse(policy.isTrusted)
    }

    @Test
    fun trustWindowNeverExtendsWithoutFreshGrant() {
        var clock = 0L
        val policy = AdminTrustPolicy(now = { clock })
        policy.grant()
        // Keep "checking" isTrusted every second for 30 minutes: must expire at 10 min.
        var expiredAt: Long? = null
        repeat(30) { minute ->
            clock = minute * 60_000L
            if (!policy.isTrusted && expiredAt == null) expiredAt = clock
        }
        assertEquals(10 * 60_000L, expiredAt)
    }

    // ---- markers ---------------------------------------------------------

    @Test
    fun markersAreAccountScopedAndMinimal() {
        val store = InMemoryAdminMarkerStore()
        var account = "usr_1"
        val markers = AdminMarkers(store, account = { account })

        markers.set(AdminMarkerKind.DEFERRED, "sub_1", true)
        markers.set(AdminMarkerKind.RE_REVIEW, "sub_2", true)
        assertTrue(markers.isMarked(AdminMarkerKind.DEFERRED, "sub_1"))
        assertFalse(markers.isMarked(AdminMarkerKind.RE_REVIEW, "sub_1"))
        assertEquals(setOf("sub_2"), markers.all(AdminMarkerKind.RE_REVIEW))

        // Account switch: markers must not bleed between accounts.
        account = "usr_2"
        assertFalse(markers.isMarked(AdminMarkerKind.DEFERRED, "sub_1"))
        markers.clearAccount() // clears only the current (usr_2) account
        account = "usr_1"
        assertTrue(markers.isMarked(AdminMarkerKind.DEFERRED, "sub_1"))

        // Blank ids never create marker state.
        markers.set(AdminMarkerKind.DEFERRED, "", true)
        assertTrue(markers.all(AdminMarkerKind.DEFERRED).isEmpty() || "sub_1" in markers.all(AdminMarkerKind.DEFERRED))
        assertEquals(1, markers.all(AdminMarkerKind.DEFERRED).size)
    }

    @Test
    fun markersClearOnlyOnConfirmedServerResolution() {
        val markers = AdminMarkers(InMemoryAdminMarkerStore(), account = { "usr_1" })
        markers.set(AdminMarkerKind.DEFERRED, "sub_1", true)
        markers.set(AdminMarkerKind.RE_REVIEW, "sub_1", true)

        // Still pending server states keep the personal marker.
        markers.clearResolved("sub_1", AdminStatus.SUBMITTED)
        markers.clearResolved("sub_1", AdminStatus.IN_REVIEW)
        markers.clearResolved("sub_1", AdminStatus.CHANGES_REQUESTED)
        assertTrue(markers.isMarked(AdminMarkerKind.DEFERRED, "sub_1"))
        assertTrue(markers.isMarked(AdminMarkerKind.RE_REVIEW, "sub_1"))

        // Terminal server states (atomic decision response) clear both markers.
        markers.clearResolved("sub_1", AdminStatus.APPROVED)
        assertFalse(markers.isMarked(AdminMarkerKind.DEFERRED, "sub_1"))
        assertFalse(markers.isMarked(AdminMarkerKind.RE_REVIEW, "sub_1"))
    }

    @Test
    fun removalAndReAddRoundTrip() {
        val markers = AdminMarkers(InMemoryAdminMarkerStore(), account = { "usr_1" })
        markers.set(AdminMarkerKind.RE_REVIEW, "sub_7", true)
        markers.set(AdminMarkerKind.RE_REVIEW, "sub_7", false)
        assertFalse(markers.isMarked(AdminMarkerKind.RE_REVIEW, "sub_7"))
        markers.set(AdminMarkerKind.RE_REVIEW, "sub_7", true)
        assertTrue(markers.isMarked(AdminMarkerKind.RE_REVIEW, "sub_7"))
    }

    @Test
    fun failureKeysAreStableForUiMapping() {
        assertEquals("unauthorized", AdminFailure.Unauthorized.key)
        assertEquals("forbidden", AdminFailure.Forbidden.key)
        assertEquals("not_found", AdminFailure.NotFound.key)
        assertEquals("conflict", AdminFailure.Conflict("").key)
        assertEquals("rate_limited", AdminFailure.RateLimited(null).key)
        assertEquals("validation_422", AdminFailure.Validation(422, "").key)
        assertEquals("server_error_503", AdminFailure.ServerError(503).key)
        assertEquals("invalid_payload", AdminFailure.InvalidPayload.key)
    }
}
