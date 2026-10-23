package com.opus.piano.community

import com.opus.piano.data.community.*
import kotlinx.coroutines.*
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.util.UUID

private fun id()=UUID.randomUUID().toString()
private fun payload(title: String="Explicitly synthetic contribution")=ContributionPayload(CatalogueUuid(id()),CommunityKind.WORK,listOf(ContributionField("title",title,listOf(SourceEvidence("reference","Goldberg authored synthetic fixture","CC0-1.0")))),"CC0-1.0")
private class Credentials: CommunityCredentials {var value: CommunitySession?=null;override fun current()=value;override fun clear(){value=null}}
private class Venue(responses: List<TransportResult> =emptyList(),transport: CommunityTransport?=null) {
    val root=Files.createTempDirectory("goldberg-community-test-").toFile()
    val credentials=Credentials().apply {value=CommunitySession(id(),"synthetic-token-never-production",Long.MAX_VALUE,true)}
    val fixture=FixtureCommunityTransport(responses)
    var now=1000L
    val store=CommunityDraftStore(File(root,"drafts"))
    val repo=LocalCommunityRepository(store,credentials,transport?:fixture,CommunityOverlayCache(File(root,"overlay.json")),clock={now})
}
class CommunityRepositoryTest {
    @Test fun dtoUsesExplicitNestedAllowlists() {
        val o=JSONObject(CommunityCodec.request(payload(),ContributionCommand(ContributionOperation.CREATE),id()))
        assertEquals(setOf("schemaVersion","payload","expectedRevision","idempotencyKey"),o.keys().asSequence().toSet())
        assertEquals(setOf("schemaVersion","target","changeKind","fields","intendedLicense"),o.getJSONObject("payload").keys().asSequence().toSet())
        assertFalse(o.toString().contains("credential"));assertFalse(o.toString().contains("principal"))
    }
    @Test fun privateObjectSmugglingFailsAtEveryPayloadLevel() {
        for(name in listOf("sessions","journal","notes","microphone","recordings","backups","profile","repertoire","author","roles","status"))for(level in 0..3) {
            val p=CommunityCodec.payload(payload());val obj=when(level){0->p;1->p.getJSONObject("target");2->p.getJSONArray("fields").getJSONObject(0);else->p.getJSONArray("fields").getJSONObject(0).getJSONArray("evidence").getJSONObject(0)}
            obj.put(name,JSONObject().put("private","sentinel"))
            assertThrows(IllegalArgumentException::class.java){CommunityCodec.decodePayload(p)}
        }
    }
    @Test fun strictSchemaVersionRejectsCoercions() {
        for(v in listOf("1",1.0,2,JSONObject.NULL)) {val p=CommunityCodec.payload(payload()).put("schemaVersion",v);assertThrows(IllegalArgumentException::class.java){CommunityCodec.decodePayload(p)}}
    }
    @Test fun unknownFieldsAndInvalidInputsNeverBecomeWireBodies() {
        val p=payload();for(bad in listOf(p.copy(fields=listOf(p.fields[0].copy(field="journal"))),p.copy(fields=listOf(p.fields[0].copy(newValue="<script>"))),p.copy(fields=List(21){p.fields[0]}),p.copy(intendedLicense="")))assertThrows(IllegalArgumentException::class.java){CommunityCodec.payload(bad)}
    }
    @Test fun reviseAndWithdrawAreTypedBoundedContractRequests() {
        val p=payload();val sid=id();val key=id()
        val revision=JSONObject(CommunityCodec.request(p,ContributionCommand(ContributionOperation.REVISE,2,sid,3),key))
        assertEquals(3,revision.getInt("expectedSubmissionRevision"));assertEquals(sid,revision.getString("id"))
        val withdraw=JSONObject(CommunityCodec.request(p,ContributionCommand(ContributionOperation.WITHDRAW,2,sid,3,"Synthetic withdrawal"),key));assertFalse(withdraw.has("payload"));assertEquals("Synthetic withdrawal",withdraw.getString("reason"))
    }
    @Test fun bigintReadsAreLosslessAndWritesRejectUnsafeJsonNumbers() {
        assertEquals("9223372036854775807",CommunityRevision("9223372036854775807").value)
        assertThrows(IllegalArgumentException::class.java){CommunityRevision("9223372036854775808")}
        assertThrows(IllegalArgumentException::class.java){CommunityCodec.request(payload(),ContributionCommand(ContributionOperation.CREATE,9007199254740992L),id())}
    }
    @Test fun savedDraftSurvivesRepositoryAndStoreRecreation()=runBlocking {
        val v=Venue();val d=v.repo.create(payload());val saved=v.repo.edit(d.id,d.version,payload("Edited synthetic title"))
        assertEquals(saved,CommunityDraftStore(File(v.root,"drafts")).get(saved.owner,saved.id))
    }
    @Test fun incompleteLocalDraftSurvivesAndCannotSubmit()=runBlocking {
        val v=Venue();val d=v.repo.create(payload(""));assertEquals("",CommunityDraftStore(File(v.root,"drafts")).get(d.owner,d.id)!!.payload.fields[0].newValue)
        assertEquals(CommunityProblem.VALIDATION,(v.repo.submit(d.id) as CommunityAttempt.Failed).failure.problem);assertTrue(v.fixture.calls.isEmpty())
    }
    @Test fun staleEditCannotSilentlyOverwrite()=runBlocking {
        val v=Venue();val d=v.repo.create(payload());v.repo.edit(d.id,d.version,payload("First save"))
        assertThrows(DraftConflict::class.java){runBlocking {v.repo.edit(d.id,d.version,payload("Stale save"))}}
        assertEquals("First save",v.repo.drafts()[0].payload.fields[0].newValue)
    }
    @Test fun atomicFaultPreservesOriginalDraft()=runBlocking {
        val v=Venue();val d=v.repo.create(payload());val fault=CommunityDraftStore(File(v.root,"drafts")){throw java.io.IOException("synthetic storage failure")}
        assertThrows(java.io.IOException::class.java){fault.save(d.copy(payload=payload("Lost write")),d.version)}
        assertEquals(d,v.store.get(d.owner,d.id))
    }
    @Test fun corruptDraftIsNotSilentlyReplaced()=runBlocking {
        val v=Venue();val d=v.repo.create(payload());val file=File(v.root,"drafts").walk().first {it.name==d.id+".json"};file.writeText("broken synthetic JSON")
        assertThrows(Exception::class.java){runBlocking {v.repo.drafts()}};assertEquals("broken synthetic JSON",file.readText())
    }
    @Test fun draftPersistsAcrossAnActualSeparateJvmProcess()=runBlocking {
        val root=Files.createTempDirectory("goldberg-community-restart-").toFile();val store=CommunityDraftStore(root);val owner=store.guest();val d=store.save(CommunityDraft(id(),owner,0,payload("Restart fixture")),0)
        // Gradle workers use isolated classloaders; java.class.path alone omits test classes.
        val childClasspath=listOf(DraftRestartProbe::class.java,CommunityDraftStore::class.java,JSONObject::class.java,kotlin.Unit::class.java)
            .map {File(it.protectionDomain.codeSource.location.toURI()).path}.distinct().joinToString(File.pathSeparator)
        val p=ProcessBuilder("java","-cp",childClasspath,"com.opus.piano.community.DraftRestartProbe",root.path,d.id).redirectErrorStream(true).start()
        val text=p.inputStream.bufferedReader().readText();assertEquals(text,0,p.waitFor());assertTrue(text.contains("Restart fixture"))
    }
    @Test fun accountSwitchHidesOtherDraftsAndCannotSendThem()=runBlocking {
        val v=Venue();val a=v.credentials.value!!;val d=v.repo.create(payload());v.credentials.value=CommunitySession(id(),"synthetic-account-b",Long.MAX_VALUE,true)
        assertTrue(v.repo.drafts().isEmpty());assertEquals(CommunityProblem.OWNERSHIP,(v.repo.submit(d.id) as CommunityAttempt.Failed).failure.problem);assertTrue(v.fixture.calls.isEmpty())
        v.credentials.value=a;assertEquals(d,v.repo.drafts().single())
    }
    @Test fun signOutClearsOnlyCredentialsAndRetainsAccountAndGuestDrafts()=runBlocking {
        val v=Venue();val account=v.credentials.value!!;val a=v.repo.create(payload());v.repo.signOut();assertNull(v.credentials.value);assertTrue(v.repo.drafts().isEmpty())
        val guest=v.repo.create(payload("Guest fixture"));v.credentials.value=account;assertEquals(a,v.repo.drafts().single());v.repo.signOut();assertEquals(guest,v.repo.drafts().single())
    }
    @Test fun guestWithoutCommunityAuthorizationCannotSubmit()=runBlocking {
        val v=Venue();v.credentials.clear();val d=v.repo.create(payload());assertEquals(CommunityProblem.UNAUTHENTICATED,(v.repo.submit(d.id) as CommunityAttempt.Failed).failure.problem);assertTrue(v.fixture.calls.isEmpty())
    }
    @Test fun expiredAndUnprivilegedSessionsCannotSubmit()=runBlocking {
        val v=Venue();val a=v.credentials.value!!;val d=v.repo.create(payload());v.credentials.value=CommunitySession(a.principalId,"synthetic-expired",0,true)
        assertEquals(CommunityProblem.UNAUTHENTICATED,(v.repo.submit(d.id) as CommunityAttempt.Failed).failure.problem)
        v.credentials.value=CommunitySession(a.principalId,"synthetic-denied",Long.MAX_VALUE,false);assertEquals(CommunityProblem.FORBIDDEN,(v.repo.submit(d.id) as CommunityAttempt.Failed).failure.problem);assertTrue(v.fixture.calls.isEmpty())
    }
    @Test fun offlineRetryKeepsExactBodyAndKeyAcrossRestart()=runBlocking {
        val v=Venue(listOf(TransportResult.Unreachable,TransportResult.Unreachable));val d=v.repo.create(payload());v.repo.submit(d.id)
        val repo=LocalCommunityRepository(CommunityDraftStore(File(v.root,"drafts")),v.credentials,v.fixture,CommunityOverlayCache(File(v.root,"overlay.json")))
        repo.submit(d.id);assertEquals(2,v.fixture.calls.size);assertEquals(v.fixture.calls[0],v.fixture.calls[1]);assertEquals(d.id,JSONObject(v.fixture.calls[0].second).getString("idempotencyKey"));assertNotNull(repo.drafts().single().pending)
    }
    @Test fun attemptedBodyCannotBeEditedOrSilentlyAssignedANewKey()=runBlocking {
        val v=Venue();val d=v.repo.create(payload());v.repo.submit(d.id);val attempted=v.repo.drafts().single()
        assertThrows(IllegalStateException::class.java){runBlocking {v.repo.edit(d.id,attempted.version,payload("Changed"))}}
        assertEquals(CommunityProblem.FROZEN,(v.repo.submit(d.id,ContributionCommand(ContributionOperation.CREATE,2)) as CommunityAttempt.Failed).failure.problem)
        val copy=v.repo.copyForNewRequest(d.id);assertNotEquals(d.id,copy.id);assertNull(copy.pending);assertNotNull(v.repo.drafts().first {it.id==d.id}.pending)
    }
    @Test fun conflictAndMismatchKeepDraftsAndRefuseAutomaticResend()=runBlocking {
        for(code in listOf(409,422)){val v=Venue(listOf(TransportResult.Response(code,"{}")));val d=v.repo.create(payload());v.repo.submit(d.id);val first=v.repo.drafts().single();v.repo.submit(d.id);assertEquals(1,v.fixture.calls.size);assertEquals(first,v.repo.drafts().single());assertNotNull(first.pending)}
    }
    @Test fun everyServerErrorRetainsDraftAndMapsDistinctly()=runBlocking {
        val expected=mapOf(400 to CommunityProblem.VALIDATION,401 to CommunityProblem.UNAUTHENTICATED,403 to CommunityProblem.FORBIDDEN,404 to CommunityProblem.NOT_FOUND,409 to CommunityProblem.CONFLICT,422 to CommunityProblem.IDEMPOTENCY_MISMATCH,429 to CommunityProblem.RATE_LIMITED,503 to CommunityProblem.UNAVAILABLE,500 to CommunityProblem.SERVER)
        for((status,problem) in expected){val v=Venue(listOf(TransportResult.Response(status,"PRIVATE server stack", "2")));val d=v.repo.create(payload());assertEquals(problem,(v.repo.submit(d.id) as CommunityAttempt.Failed).failure.problem);assertEquals(d.payload,v.repo.drafts().single().payload);assertNull(v.repo.drafts().single().receipt)}
    }
    @Test fun rateLimitHonorsRetryAfterAndReusesRequest()=runBlocking {
        val v=Venue(listOf(TransportResult.Response(429,"{}","2"),TransportResult.Unreachable));val d=v.repo.create(payload());v.repo.submit(d.id);v.repo.submit(d.id);assertEquals(1,v.fixture.calls.size)
        v.now=3000;v.repo.submit(d.id);assertEquals(2,v.fixture.calls.size);assertEquals(v.fixture.calls[0],v.fixture.calls[1])
    }
    @Test fun retryAfterParsesHttpDateAndRejectsInvalidOrOverflow() {
        assertEquals(2000L,CommunityErrors.map(429,"Thu, 1 Jan 1970 00:00:02 GMT",1000).retryAtMillis)
        for(v in listOf("-1","nonsense","9223372036854775807"))assertNull(CommunityErrors.map(429,v,1000).retryAtMillis)
    }
    @Test fun failureBeforeRequestSnapshotPreventsTransport()=runBlocking {
        val v=Venue();val d=v.repo.create(payload());val broken=LocalCommunityRepository(CommunityDraftStore(File(v.root,"drafts")){throw java.io.IOException()},v.credentials,v.fixture,CommunityOverlayCache(File(v.root,"overlay.json")))
        assertEquals(CommunityProblem.STORAGE,(broken.submit(d.id) as CommunityAttempt.Failed).failure.problem);assertTrue(v.fixture.calls.isEmpty());assertEquals(d,v.store.get(d.owner,d.id))
    }
    @Test fun signOutDuringFlightPreventsReceiptAndPreservesPendingRequest()=runBlocking {
        val started=CompletableDeferred<Unit>();val finish=CompletableDeferred<Unit>()
        val transport=object: CommunityTransport {override val availability=CommunityAvailability.SYNTHETIC_FIXTURE;override suspend fun invoke(operation: String,body: String,credential: String?): TransportResult {started.complete(Unit);finish.await();return TransportResult.Response(201,"{\"id\":\"${id()}\",\"status\":\"submitted\"}")}}
        val v=Venue(transport=transport);val d=v.repo.create(payload());val original=v.credentials.value!!;val pending=async {v.repo.submit(d.id)};started.await();v.repo.signOut();finish.complete(Unit)
        assertEquals(CommunityProblem.UNAUTHENTICATED,(pending.await() as CommunityAttempt.Failed).failure.problem);v.credentials.value=original;assertNull(v.repo.drafts().single().receipt);assertNotNull(v.repo.drafts().single().pending)
    }
    @Test fun fixtureReceiptIsExplicitlySyntheticAndNeverRemovesDraft()=runBlocking {
        val sid=id();val v=Venue(listOf(TransportResult.Response(201,"{\"id\":\"$sid\",\"status\":\"submitted\"}")));val d=v.repo.create(payload());assertEquals(sid,(v.repo.submit(d.id) as CommunityAttempt.SyntheticReceipt).submissionId);assertEquals(d.payload,v.repo.drafts().single().payload);assertEquals(CommunityProblem.FROZEN,(v.repo.submit(d.id) as CommunityAttempt.Failed).failure.problem)
    }
    @Test fun malformedSuccessIsNeverReportedAsSuccess()=runBlocking {
        val v=Venue(listOf(TransportResult.Response(201,"{}")));val d=v.repo.create(payload());assertEquals(CommunityProblem.SERVER,(v.repo.submit(d.id) as CommunityAttempt.Failed).failure.problem);assertNull(v.repo.drafts().single().receipt)
    }
    @Test fun disabledProductionBoundaryNeverSendsAndRetainsDraft()=runBlocking {
        val v=Venue(transport=DisabledCommunityTransport);val d=v.repo.create(payload());assertEquals(CommunityAvailability.DISABLED,v.repo.availability);assertEquals(CommunityProblem.DISABLED,(v.repo.submit(d.id) as CommunityAttempt.Failed).failure.problem);assertEquals(d,v.repo.drafts().single());assertTrue(v.fixture.calls.isEmpty())
    }
    @Test fun repositoryConstructionAndLocalEditingDoNotUploadOrFetch()=runBlocking {
        val v=Venue();assertTrue(v.fixture.calls.isEmpty());val d=v.repo.create(payload());v.repo.edit(d.id,d.version,payload());v.repo.drafts();v.repo.preview(d.id);assertTrue(v.fixture.calls.isEmpty())
    }
    @Test fun cachedOverlayIsExplicitlyStaleSyntheticAndSurvivesRestart()=runBlocking {
        val uuid=id();val raw="""{"items":[{"kind":"work","catalogueUuid":"$uuid","displayTitle":"Synthetic public title","revision":"9223372036854775807","provenanceSummary":{"licenses":["CC0-1.0"],"sourceCount":1},"privateNote":"DO NOT CACHE"}],"nextCursor":null}"""
        val v=Venue(listOf(TransportResult.Response(200,raw),TransportResult.Unreachable));assertFalse(v.repo.overlay()!!.stale);val stale=v.repo.overlay()!!;assertTrue(stale.stale);assertTrue(stale.synthetic);assertEquals(1000,stale.synchronizedAt)
        assertFalse(File(v.root,"overlay.json").readText().contains("DO NOT CACHE"));val recreated=LocalCommunityRepository(v.store,v.credentials,DisabledCommunityTransport,CommunityOverlayCache(File(v.root,"overlay.json")));assertTrue(recreated.overlay()!!.stale)
    }
    @Test fun changeFeedPreservesNumericOrderingAndRejectsLexicographicOrder() {
        fun raw(seqs: List<String>)="""{"items":[${seqs.joinToString(","){"""{"sequence":"$it","kind":"work","catalogueUuid":"00000000-0000-4000-8000-000000000001","revision":"1","event":"publication","status":"published","createdAt":"2030-01-01T00:00:00Z","provenanceSummary":{"licenses":[],"sourceCount":0}}"""}}],"nextCursor":null,"pollCursor":"cursor"}"""
        assertEquals(listOf("9","10","9223372036854775807"),CommunityCodec.changes(raw(listOf("9","10","9223372036854775807"))).items.map {it.sequence.value})
        assertThrows(IllegalArgumentException::class.java){CommunityCodec.changes(raw(listOf("10","9")))}
    }
    @Test fun backupAndStartupBoundariesAreExplicitInAndroidSource() {
        val root=File(System.getProperty("goldberg.projectRoot") ?: if(File("src/main/AndroidManifest.xml").isFile) ".." else ".");val manifest=File(root,"app/src/main/AndroidManifest.xml").readText();assertTrue(manifest.contains("android:allowBackup=\"false\""));assertTrue(manifest.contains("android:fullBackupContent=\"false\""))
        val xml=javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(File(root,"app/src/main/res/xml/data_extraction_rules.xml"))
        for(name in listOf("cloud-backup","device-transfer")){val block=xml.getElementsByTagName(name).item(0) as org.w3c.dom.Element;val exclusions=block.getElementsByTagName("exclude");assertTrue((0 until exclusions.length).any {val e=exclusions.item(it) as org.w3c.dom.Element;e.getAttribute("domain")=="file"&&e.getAttribute("path")=="."})}
        val app=File(root,"app/src/main/java/com/opus/piano/OpusApplication.kt").readText();val startup=app.substringBefore("override fun onConfigurationChanged");assertFalse(startup.contains("community"));assertTrue(app.contains("val community by lazy"))
    }
    @Test fun credentialsAreRedactedAndNotInFixtureCallsOrPersistedDrafts()=runBlocking {
        val v=Venue();val d=v.repo.create(payload());v.repo.submit(d.id);assertEquals("CommunitySession(redacted)",v.credentials.value.toString())
        assertFalse(v.fixture.calls.toString().contains("synthetic-token"));assertFalse(File(v.root,"drafts").walk().filter {it.extension=="json"}.any {it.readText().contains("synthetic-token")})
    }
    @Test fun transportTimeoutIsTruthfulAndKeepsFrozenDraft()=runBlocking {
        val t=object: CommunityTransport {override val availability=CommunityAvailability.SYNTHETIC_FIXTURE;override suspend fun invoke(operation: String,body: String,credential: String?): TransportResult {awaitCancellation()}}
        val v=Venue(transport=t);val repo=LocalCommunityRepository(v.store,v.credentials,t,CommunityOverlayCache(File(v.root,"timeout.json")),requestTimeoutMillis=10)
        val d=repo.create(payload());assertEquals(CommunityProblem.OFFLINE,(repo.submit(d.id) as CommunityAttempt.Failed).failure.problem);assertNotNull(repo.drafts().single().pending)
    }
    @Test fun callerCancellationLeavesDurableRetrySnapshot()=runBlocking {
        val started=CompletableDeferred<Unit>();val t=object: CommunityTransport {override val availability=CommunityAvailability.SYNTHETIC_FIXTURE;override suspend fun invoke(operation: String,body: String,credential: String?): TransportResult {started.complete(Unit);awaitCancellation()}}
        val v=Venue(transport=t);val d=v.repo.create(payload());val call=launch {v.repo.submit(d.id)};started.await();call.cancelAndJoin();assertEquals(d.id,v.repo.drafts().single().pending!!.key)
    }
    @Test fun missingRetryAfterNeverCausesImmediateResend()=runBlocking {
        val v=Venue(listOf(TransportResult.Response(429,"{}")));val d=v.repo.create(payload());v.repo.submit(d.id);v.repo.submit(d.id);assertEquals(1,v.fixture.calls.size);assertEquals(CommunityProblem.RATE_LIMITED,v.repo.drafts().single().pending!!.outcome)
    }

    @Test fun externalIdentitiesStayTypedAndNeverImplyAnAutomaticMerge() {
        val external=CommunityExternalIdentity(CommunityProvider.WIKIDATA,CommunityKind.WORK,"Q42")
        val p=payload().copy(target=null,externalIdentity=external);val wire=CommunityCodec.payload(p);assertFalse(wire.getJSONObject("target").has("catalogueUuid"));assertEquals(p,CommunityCodec.decodePayload(wire))
        wire.getJSONObject("target").getJSONObject("externalIdentity").put("roles","administrator");assertThrows(IllegalArgumentException::class.java){CommunityCodec.decodePayload(wire)}
        assertThrows(IllegalArgumentException::class.java){CommunityCodec.payload(p.copy(externalIdentity=external.copy(providerKind=CommunityKind.ARTIST)))}
        assertThrows(IllegalArgumentException::class.java){CommunityCodec.payload(p.copy(externalIdentity=null))}
    }
    @Test fun completeWireBodyAndUrlCountLimitsAreEnforced() {
        val p=payload();val fields=listOf("title","name","displayTitle","description","genre","instrument","musicalEra").map {p.fields[0].copy(field=it,newValue="x".repeat(2048),evidence=List(2){p.fields[0].evidence[0].copy(value="x".repeat(500))})}
        CommunityCodec.payload(p.copy(fields=fields)) // Valid individual bounds; total wire request exceeds 16 KiB.
        assertThrows(IllegalArgumentException::class.java){CommunityCodec.request(p.copy(fields=fields),ContributionCommand(ContributionOperation.CREATE),id())}
        val evidence=List(6){SourceEvidence("source_url","https://evidence.synthetic.invalid/source","CC0-1.0")}
        assertThrows(IllegalArgumentException::class.java){CommunityCodec.payload(p.copy(fields=listOf(ContributionField("title","a",evidence),ContributionField("name","b",evidence))))}
        assertThrows(IllegalArgumentException::class.java){CatalogueUuid("00000000-0000-0000-0000-000000000000")}
    }

    @Test fun lostReceiptSaveCanOnlyRetryTheOriginalRequest()=runBlocking {
        val sid=id();val response=TransportResult.Response(201,"{\"id\":\"$sid\",\"status\":\"submitted\"}")
        val v=Venue(listOf(response,response));val d=v.repo.create(payload());var writes=0
        val store=CommunityDraftStore(File(v.root,"drafts")){writes++;if(writes==2)throw java.io.IOException("Synthetic receipt-save fault")}
        val repo=LocalCommunityRepository(store,v.credentials,v.fixture,CommunityOverlayCache(File(v.root,"overlay.json")))
        assertEquals(CommunityProblem.STORAGE,(repo.submit(d.id) as CommunityAttempt.Failed).failure.problem);assertNull(repo.drafts().single().receipt)
        assertTrue(repo.submit(d.id) is CommunityAttempt.SyntheticReceipt);assertEquals(v.fixture.calls[0],v.fixture.calls[1]);assertEquals(d.id,repo.drafts().single().pending!!.key)
    }

    @Test fun malformedEvidenceUrlIsValidationRatherThanAStorageOrNetworkFailure()=runBlocking {
        val v=Venue();val p=payload().let {it.copy(fields=listOf(it.fields[0].copy(evidence=listOf(SourceEvidence("source_url","https://synthetic.invalid/bad path","CC0-1.0")))))}
        val d=v.repo.create(p);assertEquals(CommunityProblem.VALIDATION,(v.repo.submit(d.id) as CommunityAttempt.Failed).failure.problem);assertTrue(v.fixture.calls.isEmpty());assertEquals(d.payload,v.repo.drafts().single().payload)
    }
    @Test fun publicEnvelopeAndSequenceSizesAreBounded() {
        assertThrows(IllegalArgumentException::class.java){CommunityRevision("9".repeat(1000))}
        assertThrows(IllegalArgumentException::class.java){CommunityCodec.overlay(" ".repeat(262145))}
        assertThrows(IllegalArgumentException::class.java){CommunityCodec.changes(" ".repeat(262145))}
    }

}
object DraftRestartProbe {
    @JvmStatic fun main(args: Array<String>) {val s=CommunityDraftStore(File(args[0]));println(s.get(s.guest(),args[1])!!.payload.fields[0].newValue)}
}
