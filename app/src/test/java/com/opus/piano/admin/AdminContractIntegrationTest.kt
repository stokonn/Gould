package com.opus.piano.admin

import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

/** Shared corpus comparisons never select or activate a candidate wire dialect. */
class AdminContractIntegrationTest {
    private fun resource(name: String) = javaClass.classLoader!!.getResourceAsStream("stage7b-comparison/$name")!!.bufferedReader().use { JSONObject(it.readText()) }
    private fun fixture(id: String): JSONObject {
        val array=resource("fixtures.json").getJSONArray("cases")
        return (0 until array.length()).map { array.getJSONObject(it) }.single { it.getString("id")==id }
    }
    private fun api(id: String): AdminApi {
        val response=fixture(id).getJSONObject("response")
        return AdminApi({"https://fixture.invalid"},AdminTransport { _,_,_,_->
            AdminHttpResponse(response.getInt("status"),response.get("body").toString().toByteArray(),null)
        })
    }
    private fun body(id: String)=fixture(id).getJSONObject("response").getJSONObject("body")
    private fun invalid(result: AdminResult<*>)=assertEquals(AdminResult.Err(AdminFailure.InvalidPayload),result)

    @Test fun corpusSeparatesAuthoritiesAndDoesNotApproveMapping() {
        val root=resource("fixtures.json"); assertTrue(root.getBoolean("synthetic"));assertEquals(1,root.getInt("schemaVersion"))
        val cases=root.getJSONArray("cases")
        val entries=(0 until cases.length()).map{cases.getJSONObject(it)}
        assertEquals(29,entries.size);assertEquals(29,entries.map{it.getString("id")}.toSet().size)
        assertEquals(setOf("legacy-c6-reference","candidate-local","normative-design","client-condition"),entries.map{it.getString("classification")}.toSet())
        val proposal=resource("mapping-proposal.json")
        assertEquals("proposal-unapproved",proposal.getString("classification"));assertFalse(proposal.getBoolean("active"));assertTrue(proposal.getBoolean("requiresApproval"))
    }
    @Test fun applicationTransportStaysDisabledWithConfiguredEndpointAndCredentials()=runBlocking {
        var tokenReads=0
        val api=AdminApi.forApplication({"https://must-not-be-contacted.invalid"},{tokenReads++;"synthetic-not-a-real-token"})
        assertEquals(AdminResult.Err(AdminFailure.ContractUnavailable),api.capabilities())
        assertEquals(AdminResult.Err(AdminFailure.ContractUnavailable),api.queue(null,null))
        assertEquals(AdminResult.Err(AdminFailure.ContractUnavailable),api.decide("s",JSONObject()))
        assertEquals(0,tokenReads)
    }
    @Test fun applicationDefaultUnconfiguredRemainsHonestWithZeroCredentialsRead()=runBlocking {
        var reads=0
        val api=AdminApi.forApplication({""},{reads++;null})
        assertEquals(AdminResult.Err(AdminFailure.NotConfigured),api.audit(null));assertEquals(0,reads)
    }
    @Test fun applicationFixtureSeamIsExplicitAndNeverRequiresNetworking()=runBlocking {
        var calls=0
        val api=AdminApi.forApplication({"https://fixture.invalid"},fixtureTransportOverride=AdminTransport{_,_,_,_->calls++;AdminHttpResponse(200,body("legacy-capabilities").toString().toByteArray(),null)})
        assertTrue(api.capabilities() is AdminResult.Ok);assertEquals(1,calls)
    }
    @Test fun supportedLegacyQueueAndDetailRemainReadable()=runBlocking {
        val queue=api("legacy-queue").queue(null,null) as AdminResult.Ok
        assertEquals(3,queue.value.items.single().revision)
        val id=queue.value.items.single().id
        assertEquals(3,(api("legacy-detail").submission(id) as AdminResult.Ok).value.revision)
    }
    @Test fun candidateCapabilitiesCannotGrantAnUnboundPrincipal()=runBlocking {
        invalid(api("candidate-capabilities").capabilities())
    }
    @Test fun nonStringCapabilityCannotBecomeAClaim()=runBlocking {
        val api=AdminApi({"https://fixture.invalid"},AdminTransport{_,_,_,_->AdminHttpResponse(200,"""{"principalId":"p","capabilities":[{"role":"decide"}]}""".toByteArray(),null)})
        invalid(api.capabilities())
    }
    @Test fun candidateQueueCursorCannotBeStringifiedIntoLegacyCursor()=runBlocking {
        invalid(api("candidate-queue").queue(null,null))
    }
    @Test fun candidateDetailIsRejectedRatherThanDefaultedToProposalRevisionOne()=runBlocking {
        invalid(api("candidate-detail").submission(body("candidate-detail").getJSONObject("submission").getString("id")))
    }
    @Test fun candidateRejectWithoutRevisionIsNotInventedAsLegacySuccess()=runBlocking {
        invalid(api("candidate-reject").decide("s",JSONObject()))
    }
    @Test fun candidateAuditCannotBeMistakenForLegacyEventOrTimestamp()=runBlocking {
        invalid(api("candidate-audit").audit(null))
    }
    @Test fun auditBigintBeyondJavaScriptPrecisionIsExact()=runBlocking {
        val page=(api("legacy-audit-bigint").audit(null) as AdminResult.Ok).value
        assertEquals(9007199254740993L,page.items.single().seq)
        assertEquals("9007199254740993",page.nextCursor)
    }
    @Test fun signedLongMaximumIsAcceptedWithoutRounding() {
        val event=body("legacy-audit-bigint").getJSONArray("items").getJSONObject(0)
        event.put("seq","9223372036854775807")
        assertEquals(Long.MAX_VALUE,AdminAuditEvent.parse(event).seq)
    }
    @Test fun malformedBigintRepresentationsAreRejected() {
        for(value in listOf<Any>("9223372036854775808","-1","1.0","1e3","01",1.5,true,JSONObject.NULL)) {
            val event=body("legacy-audit-bigint").getJSONArray("items").getJSONObject(0).put("seq",value)
            try { AdminAuditEvent.parse(event); fail("Accepted invalid sequence $value") } catch (_: IllegalArgumentException) { }
        }
    }
    @Test fun invalidAuditIntegerBecomesTypedPayloadError()=runBlocking {
        val b=body("legacy-audit-bigint");b.getJSONArray("items").getJSONObject(0).put("seq","9223372036854775808")
        val api=AdminApi({"https://fixture.invalid"},AdminTransport{_,_,_,_->AdminHttpResponse(200,b.toString().toByteArray(),null)})
        invalid(api.audit(null))
    }
    @Test fun revisionOverflowFractionOrMissingNeverDefaults() {
        for(value in listOf<Any>("2147483648",4294967297L,1.5,JSONObject.NULL)) {
            val row=body("legacy-queue").getJSONArray("items").getJSONObject(0).put("revision",value)
            try { AdminQueueItem.parse(row);fail("Accepted invalid revision") } catch (_: IllegalArgumentException) { }
        }
    }
    @Test fun opaqueLegacyCursorIsPreservedByteForByte() {
        val cursor=body("candidate-audit").getString("nextCursor")
        val page=AdminQueuePage.parse(JSONObject().put("items",org.json.JSONArray()).put("nextCursor",cursor))
        assertEquals(cursor,page.nextCursor)
    }
    @Test fun candidateConflictHasNoInventedCurrentRevisionOrStatus()=runBlocking {
        val result=api("candidate-error-409").decide("s",JSONObject()) as AdminResult.Err
        val conflict=result.failure as AdminFailure.Conflict
        assertNull(conflict.currentRevision);assertNull(conflict.currentStatus)
    }
    @Test fun unsafeConflictDetailsAreUnknownRatherThanRounded()=runBlocking {
        val api=AdminApi({"https://fixture.invalid"},AdminTransport{_,_,_,_->AdminHttpResponse(409,"""{"error":{"message":"stale","details":{"currentRevision":4294967297,"currentStatus":null}}}""".toByteArray(),null)})
        val f=(api.decide("s",JSONObject()) as AdminResult.Err).failure as AdminFailure.Conflict
        assertNull(f.currentRevision);assertNull(f.currentStatus)
    }
    @Test fun malformedAuditRowsCannotBeSilentlyDroppedIntoAnEmptySuccess()=runBlocking {
        val api=AdminApi({"https://fixture.invalid"},AdminTransport{_,_,_,_->AdminHttpResponse(200,"""{"items":["invalid-row"]}""".toByteArray(),null)})
        invalid(api.audit(null))
    }
    @Test fun allCandidateFailureClassesRemainDistinct()=runBlocking {
        val expected=mapOf(400 to "validation_400",401 to "unauthorized",403 to "forbidden",404 to "not_found",409 to "conflict",422 to "validation_422",503 to "server_error_503")
        for((code,key) in expected) assertEquals(key,(api("candidate-error-$code").capabilities() as AdminResult.Err).failure.key)
    }
    @Test fun replayFixtureRequiresIdenticalBodyKeyAndOriginalResponse() {
        val original=fixture("candidate-approved");val replay=fixture("candidate-idempotent-replay")
        assertEquals(original.getJSONObject("requestBody").toString(),replay.getJSONObject("requestBody").toString())
        assertEquals(original.getJSONObject("response").toString(),replay.getJSONObject("response").toString())
        assertEquals("candidate-approved",replay.getString("replayOf"))
    }
    @Test fun entityProposalAndPublishedRevisionsStayExplicitlyDifferent() {
        val request=fixture("candidate-approved").getJSONObject("requestBody")
        assertEquals(7,request.getInt("expectedRevision"));assertEquals(3,request.getInt("expectedSubmissionRevision"))
        assertEquals(8,body("candidate-approved").getInt("revision"))
        val legacy=buildDecisionBody(AdminDecision.APPROVE,3,null,"key")
        assertEquals(3,legacy.getInt("expectedRevision"));assertFalse(legacy.has("expectedSubmissionRevision"))
        assertFalse(resource("mapping-proposal.json").getBoolean("active"))
    }
    @Test fun candidateAuditVocabularyIsNotSilentlyCountedAsLegacyDecision() {
        assertEquals("approved->published",body("candidate-audit").getJSONArray("items").getJSONObject(0).getString("action"))
        assertNotEquals("decision.approve",body("candidate-audit").getJSONArray("items").getJSONObject(0).getString("action"))
    }
}
