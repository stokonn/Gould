package com.opus.piano.admin

import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class AdminReviewTest {
    private class Fixture(val decisionsEnabled: Boolean = true) {
        var revision=1
        var status="submitted"
        var principal="moderator"
        var roleCode=200
        var canDecide=true
        var decisionCode=201
        var corrupt=false
        var unknownStatus=false
        val posts=mutableListOf<JSONObject>()
        var roleCalls=0
        var onRole: (Int) -> Unit = {}
        var cancelDecision=false
        val api=AdminApi({"https://test.invalid"}, AdminTransport { method,url,_,body ->
            val code: Int
            val response: String
            if (url.endsWith("capabilities")) {
                roleCalls++; onRole(roleCalls)
                code=roleCode
                response="""{"principalId":"$principal","capabilities":["queue.read"${if(canDecide) ",\"decide\"" else ""}]}"""
            } else if(method=="POST") {
                val b=JSONObject(String(body!!)); posts+=b
                if (cancelDecision) throw kotlinx.coroutines.CancellationException("cancelled after write")
                code=decisionCode
                val after=if(unknownStatus) "unrecognized" else when(b.getString("decision")) { "approve"->"published"; "reject"->"rejected"; else->"changes_requested" }
                response=if(code==409) """{"error":{"message":"stale","details":{"currentRevision":$revision,"currentStatus":"$status"}}}"""
                else """{"submissionId":"${if(corrupt) "wrong" else "sub_1"}","decision":"${b.getString("decision")}","status":"$after","revision":${b.getInt("expectedRevision")}}"""
            } else {
                code=200
                response="""{"id":"sub_1","owner":"author","status":"$status","revision":$revision,"revisions":[{"revision":$revision,"payload":{"fields":[{"field":"title","newValue":"Title $revision"}]}}],"reviewEvents":[]}"""
            }
            AdminHttpResponse(code,response.toByteArray(),null)
        })
        val markers=AdminMarkers(InMemoryAdminMarkerStore()) {principal}
        val trust=AdminTrustPolicy()
        val repo=AdminRepository(api,InMemoryAdminSessionStore(),trust,markers,AdminStatsService(api),decisionsEnabled=decisionsEnabled)
        val s get()=repo.review
        suspend fun ready(decision: AdminDecision=AdminDecision.APPROVE) { s.validate("sub_1"); s.prepare(decision) }
    }
    @Test fun liveContractGateCannotBeBypassedByDirectServiceCall()=runBlocking {
        val f=Fixture(decisionsEnabled=false); f.ready(); f.s.confirm(null); f.s.retryPending()
        assertTrue(f.posts.isEmpty()); assertNull(f.s.state.value.confirmation)
        assertEquals(AdminFailure.ContractUnavailable,f.s.state.value.failure)
    }
    @Test fun explicitConfirmationSendsExpectedRevisionAndKey()= runBlocking {
        val f=Fixture(); f.ready(); assertNotNull(f.s.state.value.confirmation); assertTrue(f.posts.isEmpty())
        f.s.confirm(null); assertEquals(1,f.posts.size); assertEquals(1,f.posts.single().getInt("expectedRevision"))
        assertNotNull(f.s.state.value.success); assertNull(f.s.state.value.pending)
    }
    @Test fun rejectRequiresNonblankReason()= runBlocking {
        val f=Fixture(); f.ready(AdminDecision.REJECT); f.s.confirm("  "); assertTrue(f.posts.isEmpty())
        f.s.confirm("Invalid source"); assertEquals("Invalid source",f.posts.single().getString("reason"))
    }
    @Test fun changesRequireClearRequestAndConfirmedServerTransition()= runBlocking {
        val f=Fixture(); f.ready(AdminDecision.REQUEST_CHANGES); f.s.confirm(""); assertTrue(f.posts.isEmpty())
        f.s.confirm("Please cite a source"); assertEquals(AdminStatus.CHANGES_REQUESTED,f.s.state.value.success?.status)
    }
    @Test fun revisionChangeWhileConfirmingPreventsPostAndRequiresReview()= runBlocking {
        val f=Fixture(); f.ready(); f.revision=2; f.s.confirm(null)
        assertTrue(f.posts.isEmpty()); assertTrue(f.s.state.value.reReview); assertNull(f.s.state.value.confirmation)
        assertEquals(1,f.s.state.value.previous?.revision)
    }
    @Test fun stateChangeAtSameRevisionInvalidatesConfirmation()= runBlocking {
        val f=Fixture(); f.ready(); f.status="rejected"; f.s.confirm(null)
        assertTrue(f.posts.isEmpty()); assertNull(f.s.state.value.confirmation)
    }
    @Test fun revokedRoleCannotDecideAndInvalidatesTrust()= runBlocking {
        val f=Fixture(); f.ready(); f.roleCode=403; f.s.confirm(null)
        assertTrue(f.posts.isEmpty()); assertFalse(f.trust.isTrusted); assertNull(f.s.state.value.detail)
    }
    @Test fun capabilityLossBlocksPost()= runBlocking {
        val f=Fixture(); f.ready(); f.canDecide=false; f.s.confirm(null)
        assertTrue(f.posts.isEmpty()); assertEquals(AdminFailure.Forbidden,f.s.state.value.failure)
    }
    @Test fun selfDecisionIsBlocked()= runBlocking {
        val f=Fixture(); f.principal="author"; f.ready()
        assertNull(f.s.state.value.confirmation); assertTrue(f.posts.isEmpty())
    }
    @Test fun staleAcknowledgementCannotUnlockNewerRevision()= runBlocking {
        val f=Fixture(); f.s.validate("sub_1"); f.revision=2; f.s.validate("sub_1"); f.revision=3
        f.s.acknowledge(2); assertTrue(f.s.state.value.reReview)
        f.s.acknowledge(3); assertFalse(f.s.state.value.reReview)
        f.s.prepare(AdminDecision.APPROVE); assertEquals(3,f.s.state.value.confirmation?.revision)
    }
    @Test fun networkUncertaintyUsesSameKeyAndExactBodyOnExplicitReplay()= runBlocking {
        val f=Fixture(); f.ready(); f.decisionCode=503; f.s.confirm(null)
        assertNull(f.s.state.value.success); assertNotNull(f.s.state.value.pending)
        f.s.prepare(AdminDecision.REJECT); assertNull(f.s.state.value.confirmation)
        f.decisionCode=201; f.s.retryPending()
        assertEquals(2,f.posts.size); assertEquals(f.posts[0].toString(),f.posts[1].toString()); assertNotNull(f.s.state.value.success)
    }
    @Test fun conflictNeverResubmitsAndMarksRereview()= runBlocking {
        val f=Fixture(); f.ready(); f.decisionCode=409; f.s.confirm(null)
        assertEquals(1,f.posts.size); assertTrue(f.s.state.value.reReview); assertNull(f.s.state.value.pending)
        f.s.retryPending(); assertEquals(1,f.posts.size)
    }
    @Test fun mismatchedSuccessIsUnconfirmedAndReconcileable()= runBlocking {
        val f=Fixture(); f.ready(); f.corrupt=true; f.s.confirm(null)
        assertNull(f.s.state.value.success); assertNotNull(f.s.state.value.pending)
        assertEquals(AdminFailure.InvalidPayload,f.s.state.value.failure)
    }
    @Test fun unknownServerStatusNeverGrantsDecisionSuccess()= runBlocking {
        val f=Fixture(); f.ready(); f.unknownStatus=true; f.s.confirm(null)
        assertNull(f.s.state.value.success); assertNotNull(f.s.state.value.pending)
    }
    @Test fun backgroundLockCancelsConfirmation()= runBlocking {
        val f=Fixture(); f.ready(); f.s.lock(); f.s.confirm(null)
        assertNull(f.s.state.value.confirmation); assertFalse(f.s.state.value.verified); assertTrue(f.posts.isEmpty())
    }
    @Test fun changedPrincipalClearsSensitiveReviewAndQueueState()= runBlocking {
        val f=Fixture(); f.ready(); f.principal="other"; f.repo.verifyRole()
        assertNull(f.s.state.value.detail); assertNull(f.s.state.value.confirmation)
    }
    @Test fun unknownStatusesKeepMarkersAndAreNotDecidable() {
        val f=Fixture(); f.markers.set(AdminMarkerKind.DEFERRED,"sub_1",true)
        f.markers.clearResolved("sub_1",AdminStatus.fromWire("future_status"))
        assertTrue(f.markers.isMarked(AdminMarkerKind.DEFERRED,"sub_1")); assertFalse(AdminStatus.UNKNOWN.decidable)
    }
    @Test fun nullCursorMeansEndOfResults() {
        val p=AdminQueuePage.parse(JSONObject("""{"items":[],"nextCursor":null}"""))
        assertNull(p.nextCursor)
    }
    @Test fun backgroundDuringFinalRoleVerificationCannotSubmitDecision()=runBlocking {
        val f=Fixture(); f.ready(); f.onRole={ count -> if(count==5) f.s.lock() }
        f.s.confirm(null); assertTrue(f.posts.isEmpty()); assertFalse(f.s.state.value.verified)
    }
    @Test fun cancelledNetworkWriteRetainsOriginalPendingKey()=runBlocking {
        val f=Fixture(); f.ready(); f.cancelDecision=true
        try { f.s.confirm(null); fail("expected cancellation") } catch (_: kotlinx.coroutines.CancellationException) { }
        assertNotNull(f.s.state.value.pending); assertFalse(f.s.state.value.loading)
        f.cancelDecision=false; f.s.retryPending()
        assertEquals(f.posts[0].toString(),f.posts[1].toString()); assertNotNull(f.s.state.value.success)
    }
    @Test fun unknownOutcomeBlocksSwitchingToAnotherProposal()=runBlocking {
        val f=Fixture(); f.ready(); f.decisionCode=503; f.s.confirm(null)
        val key=f.s.state.value.pending?.key
        f.s.validate("sub_other"); assertEquals(key,f.s.state.value.pending?.key)
        assertEquals("sub_1",f.s.state.value.detail?.id)
    }
    @Test fun confirmedConflictIsCountedOnce()=runBlocking {
        val f=Fixture(); f.ready(); f.decisionCode=409; f.s.confirm(null)
        assertEquals(1,f.s.state.value.conflicts)
    }

    @Test fun cancelledSignOutStillClearsLocalCredentialsTrustAndMarkers()=runBlocking {
        val session=InMemoryAdminSessionStore().also {it.writeToken("secret")}
        val markers=AdminMarkers(InMemoryAdminMarkerStore()) {"account"}
        markers.set(AdminMarkerKind.DEFERRED,"sub_1",true)
        val trust=AdminTrustPolicy().also {it.grant()}
        val api=AdminApi({"https://test.invalid"},AdminTransport{_,_,_,_->throw kotlinx.coroutines.CancellationException("cancel")})
        val repo=AdminRepository(api,session,trust,markers,AdminStatsService(api))
        try {repo.signOut(); fail("expected cancellation")} catch (_: kotlinx.coroutines.CancellationException) { }
        assertNull(session.readToken()); assertFalse(trust.isTrusted)
        assertTrue(markers.all(AdminMarkerKind.DEFERRED).isEmpty())
    }
    @Test fun fieldDiffIncludesChangedAddedRemovedAndEvidenceOnlyChanges() {
        val old=JSONObject("""{"fields":[{"field":"title","newValue":"old"},{"field":"composer","newValue":"same","evidence":["old-source"]},{"field":"key","newValue":"C"},{"field":"unchanged","newValue":"same"}]}""")
        val fresh=JSONObject("""{"fields":[{"field":"title","newValue":"new"},{"field":"composer","newValue":"same","evidence":["new-source"]},{"field":"catalogue","newValue":"1"},{"field":"unchanged","newValue":"same"}]}""")
        val diff=proposalFieldChanges(old,fresh)
        assertEquals(setOf("title","composer","key","catalogue"),diff.map {it.name}.toSet())
        assertTrue(diff.single {it.name=="composer"}.evidenceChanged)
        assertNull(diff.single {it.name=="key"}.after)
        assertNull(diff.single {it.name=="catalogue"}.before)
    }
    @Test fun equalProposalSnapshotsDoNotFabricateChangedFields() {
        val p=JSONObject("""{"fields":[{"field":"title","newValue":"same"}]}""")
        assertTrue(proposalFieldChanges(p,JSONObject(p.toString())).isEmpty())
    }
    @Test fun accountSwitchDuringRequestDropsResponse()= runBlocking {
        var token="a"
        val api=AdminApi({"https://test.invalid"},AdminTransport { _,_,_,_ ->
            token="b"; AdminHttpResponse(200,"""{"items":[]}""".toByteArray(),null)
        },{token})
        assertEquals(AdminResult.Err(AdminFailure.Unauthorized),api.queue(null,null))
    }
}
