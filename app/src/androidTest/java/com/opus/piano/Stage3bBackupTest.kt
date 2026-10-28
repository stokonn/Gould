package com.opus.piano

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.data.*
import com.opus.piano.data.backup.*
import com.opus.piano.data.catalogue.*
import com.opus.piano.practice.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.json.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.*
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class Stage3bBackupTest {
    private val inst=InstrumentationRegistry.getInstrumentation()
    private val context=inst.targetContext
    private fun fixture(name:String="full-v5.json")=inst.context.assets.open("stage3b/$name").bufferedReader().use{it.readText()}
    private class Env(val context:android.content.Context) {
        val id=UUID.randomUUID().toString();val name="stage3b-$id.db";val directory=File(context.cacheDir,"stage3b-$id")
        val preferences=PortablePreferences(context,"stage3b-$id-")
        fun open()=Room.databaseBuilder(context,OpusDatabase::class.java,name).addMigrations(*OpusDatabase.ALL_MIGRATIONS).addCallback(PersonalCatalogueBridge.callback).addCallback(BackupState.callback).build()
        var db=open()
        fun repo(files:BackupFiles=BackupFiles(),phase:(String)->Unit={})=BackupRepository(context,db,preferences,files,directory,phase)
        fun close(){db.close();context.deleteDatabase(name);directory.deleteRecursively()}
    }
    private fun sections(o:JSONObject,key:String)=o.getJSONObject("sections").getJSONObject(key).getJSONObject("data")
    private fun signed(o:JSONObject):String {
        o.remove("sha256");val s=o.getJSONObject("sections")
        s.keys().forEach{k->val section=s.getJSONObject(k);section.put("sha256",BackupJson.hash(section.getJSONObject("data")))}
        o.put("sha256",BackupJson.hash(o));return o.toString()
    }
    private fun bytes(row:JSONObject)=android.util.Base64.decode(row.getString("rawBase64"),android.util.Base64.NO_WRAP)
    private fun replaceDraft(o:JSONObject,data:ByteArray) {
        sections(o,"recovery").getJSONArray("drafts").getJSONObject(0).put("rawBase64",android.util.Base64.encodeToString(data,android.util.Base64.NO_WRAP)).put("sha256",BackupState.sha(data))
    }
    private suspend fun rejected(block:suspend()->Unit){assertTrue("Expected rejection",runCatching{block()}.isFailure)}
    private suspend fun seed(e:Env) {
        e.db.dao().save(Piece(id=41,title="Destination original",composer="Manual",notes="Destination notes"))
        e.db.practiceDao().checkpoint(PracticeDraft(payload="Destination opaque recovery\nŽ\u0000",updatedAt=555))
        e.db.practiceDao().archive(RecoveryArchive(payload="Destination archive",updatedAt=444,archivedAt=555))
    }
    @Test fun fullV5PreservesIdentitiesJournalRawArchivesAndAllowlistedPreferences()=runBlocking<Unit> {
        val e=Env(context)
        try {
            val source=fixture();e.repo().validate(source);e.repo().restore(source)
            val actual=JSONObject(e.repo().export());val original=JSONObject(source)
            assertEquals(6,actual.getInt("version"));assertEquals(11,actual.getInt("journalSchema"));assertFalse(actual.getJSONObject("sections").getJSONObject("studio").getJSONObject("data").getBoolean("audioFilesIncluded"))
            assertEquals(BackupJson.hash(sections(original,"journal")),BackupJson.hash(sections(actual,"journal")))
            assertEquals(BackupJson.hash(sections(original,"identities")),BackupJson.hash(sections(actual,"identities")))
            assertArrayEquals(bytes(sections(original,"recovery").getJSONArray("drafts").getJSONObject(0)),BackupState.rawDraft(e.db)!!.first)
            assertEquals(777L,BackupState.rawDraft(e.db)!!.second)
            val expectedArchives=sections(original,"recovery").getJSONArray("archives")
            for(i in 0 until expectedArchives.length())assertArrayEquals(bytes(expectedArchives.getJSONObject(i)),BackupState.rawPayload(e.db,"practice_recovery_archives",expectedArchives.getJSONObject(i).getLong("id")))
            assertEquals(8000L,e.db.dao().allSessions().sumOf{it.durationMillis})
            val p=e.preferences.snapshot();assertEquals("cs",p.getString("locale"));assertFalse(p.getBoolean("previousMeteredConsent"))
            assertEquals("Slate",p.getJSONObject("appearance").getString("accent"));assertEquals("Composer",p.getJSONObject("appearance").getString("sort"))
            assertEquals("applied",e.db.backupStateDao().state()!!.status)
            assertTrue(e.db.practiceDao().localNow()!!.reminderEnabled);assertEquals(19,e.db.practiceDao().localNow()!!.reminderHour)
            assertTrue(e.db.personalDao().getSettings()!!.smartEnabled)
            assertTrue(e.directory.listFiles()!!.any{it.name.startsWith("input-") && it.readText()==source})
        } finally {e.close()}
    }
    @Test fun destinationDraftArchivesAndIdentityContextSurviveReplacement()=runBlocking<Unit> {
        val e=Env(context)
        try {
            seed(e);val raw=BackupState.rawDraft(e.db)!!.first
            val identity=e.db.personalCatalogueDao().anchors("piece","41").single().identityKey
            e.repo().restore(fixture())
            val evidence=e.db.backupStateDao().evidence()
            assertTrue(evidence.any{it.rawBytes.contentEquals(raw) && it.updatedAt==555L && it.classification=="replaced_destination"})
            assertTrue(evidence.any{it.classification=="identity_context" && it.rawBytes.toString(Charsets.UTF_8).contains(identity)})
            assertTrue(evidence.any{it.rawBytes.toString(Charsets.UTF_8)=="Destination archive"})
            assertEquals(3,e.db.practiceDao().archives().size)
            val previous=File(e.directory,e.db.backupStateDao().state()!!.previousSnapshot)
            val backup=BackupJson.parse(previous);assertEquals("Destination original",sections(backup,"journal").getJSONArray("pieces").getJSONObject(0).getString("title"))
            assertArrayEquals(raw,bytes(sections(backup,"recovery").getJSONArray("drafts").getJSONObject(0)))
        } finally {e.close()}
    }
    @Test fun unreadableBinaryDraftCanBeExportedAndArchivedWithoutRepair()=runBlocking<Unit> {
        val e=Env(context)
        try {
            val source=fixture("unknown-v5.json");e.repo().restore(source)
            val original=bytes(sections(JSONObject(source),"recovery").getJSONArray("drafts").getJSONObject(0))
            val coordinator=PracticeCoordinator(PracticeRepository(e.db,OpusRepository(e.db,AchievementStore(e.db))))
            try {
                withTimeout(5000){coordinator.recoveryFailed.first{it}}
                assertFalse(coordinator.state.value.ready);assertFalse(coordinator.state.value.running)
                assertArrayEquals(original,withContext(Dispatchers.Main){coordinator.recoveryBytes()})
                coordinator.freshAfterExport(byteArrayOf(1,2)).join();assertTrue(coordinator.recoveryFailed.value)
                coordinator.freshAfterExport(original).join()
                assertFalse(coordinator.recoveryFailed.value);assertTrue(coordinator.state.value.ready);assertFalse(coordinator.state.value.running)
                assertNull(BackupState.rawDraft(e.db))
                assertTrue(e.db.backupStateDao().evidence().any{it.rawBytes.contentEquals(original)})
                val exported=JSONObject(e.repo().export());val archives=sections(exported,"recovery").getJSONArray("archives")
                assertTrue((0 until archives.length()).any{bytes(archives.getJSONObject(it)).contentEquals(original)})
            } finally {coordinator.close()}
        } finally {e.close()}
    }
    @Test fun validDraftRegeneratesExecutionOnlyAndCompletesExactlyOnce()=runBlocking<Unit> {
        val e=Env(context)
        try {
            e.repo().restore(fixture());val raw=BackupState.rawDraft(e.db)!!.first
            val repository=PracticeRepository(e.db,OpusRepository(e.db,AchievementStore(e.db)))
            val first=repository.recover()!!;val second=repository.recover()!!
            assertNotEquals(77L,first.execution);assertNotEquals(first.execution,second.execution)
            assertEquals("active-fixture",first.token);assertEquals(1500L,first.elapsed);assertFalse(first.running)
            assertArrayEquals(raw,BackupState.rawDraft(e.db)!!.first)
            val coordinator=PracticeCoordinator(repository,{1000L})
            try {
                withTimeout(5000){coordinator.state.first{it.ready}}
                var capture=0;coordinator.releaseCapture={capture++}
                assertFalse(coordinator.state.value.running);assertEquals(0,capture)
                coordinator.command {review();save()}.join()
                assertEquals(9500L,e.db.dao().allSessions().sumOf{it.durationMillis})
                repository.complete(first)
                assertEquals(9500L,e.db.dao().allSessions().sumOf{it.durationMillis})
            } finally {coordinator.close()}
            // Restoring an earlier backup cannot revive a token already completed here.
            e.repo().restore(fixture());assertNull(BackupState.rawDraft(e.db))
            assertEquals(8000L,e.db.dao().allSessions().sumOf{it.durationMillis})
            assertTrue(e.db.backupStateDao().evidence().any{it.classification=="already_completed" && it.completionToken=="active-fixture"})
            rejected{repository.complete(first)}
            assertEquals(8000L,e.db.dao().allSessions().sumOf{it.durationMillis})
        } finally {e.close()}
    }
    @Test fun conflictingTokenRetainsBothRecordsAndBlocksAutomaticCredit()=runBlocking<Unit> {
        val e=Env(context)
        try {
            val o=JSONObject(fixture());val draft=JSONObject(bytes(sections(o,"recovery").getJSONArray("drafts").getJSONObject(0)).toString(Charsets.UTF_8)).put("token","completed-fixture")
            replaceDraft(o,draft.toString().toByteArray());e.repo().restore(signed(o))
            val original=BackupState.rawDraft(e.db)!!.first
            val repo=PracticeRepository(e.db,OpusRepository(e.db,AchievementStore(e.db)))
            rejected{repo.recover()};rejected{repo.complete(PracticeRepository.decode(original.toString(Charsets.UTF_8)))}
            assertArrayEquals(original,BackupState.rawDraft(e.db)!!.first)
            assertEquals(8000L,e.db.dao().allSessions().sumOf{it.durationMillis})
            assertTrue(e.db.backupStateDao().evidence().any{it.classification=="token_conflict"})
        } finally {e.close()}
    }
    @Test fun minimalAndRepeatedRestoreWorkWithoutCatalogueOrAccount()=runBlocking<Unit> {
        val e=Env(context)
        try {
            repeat(3){e.repo().restore(fixture("minimal-v5.json"))}
            assertTrue(e.db.dao().allPieces().isEmpty());assertTrue(e.db.dao().allSessions().isEmpty());assertNull(BackupState.rawDraft(e.db))
            e.repo().validate(e.repo().export())
            e.repo().restore(fixture());repeat(2){e.repo().restore(fixture())}
            assertEquals(2,e.db.dao().allSessions().size);assertEquals(8000L,e.db.dao().allSessions().sumOf{it.durationMillis})
            val identities=e.db.personalCatalogueDao().identities("",100).map{it.canonicalId}
            assertEquals(6,identities.size);assertEquals(identities.distinct(),identities)
        } finally {e.close()}
    }
    @Test fun historicalOneThroughFourRemainReadableAndRetainOriginalFields()=runBlocking<Unit> {
        val e=Env(context)
        try {
            val inputs=listOf("backups/goldberg-0.6.0-format1.json","backups/goldberg-0.9.1-format2.json","backups/goldberg-0.9.2-format3.json","stage3b/historical-format4.json")
            for(path in inputs) {
                val source=inst.context.assets.open(path).bufferedReader().use{it.readText()};e.repo().restore(source)
                val old=JSONObject(source).getJSONObject("tables");val actual=JSONObject(e.repo().exportLegacy()).getJSONObject("tables")
                old.keys().forEach {table->val rows=old.getJSONArray(table);val result=actual.getJSONArray(table);assertEquals(rows.length(),result.length())
                    for(i in 0 until rows.length()){val row=rows.getJSONObject(i);assertTrue((0 until result.length()).any{j->row.keys().asSequence().all{k->row.get(k)==result.getJSONObject(j).get(k)}})}
                }
                e.repo().validate(e.repo().export())
            }
        } finally {e.close()}
    }
    @Test fun badHashesSectionsReferencesEnumsAndVersionsRejectBeforeReplacement()=runBlocking<Unit> {
        val e=Env(context)
        try {
            seed(e);val original=BackupState.rawDraft(e.db)!!.first
            val mutations:List<(JSONObject)->Unit> = listOf(
                {it.put("version",99)}, {it.put("version","5")}, {it.put("createdAt","1000")}, {it.put("journalSchema",999)}, {it.getJSONObject("sections").remove("recovery")},
                {sections(it,"preferences").put("adminRole","admin")}, {sections(it,"preferences").getJSONObject("appearance").put("accent","Neon")},
                {sections(it,"journal").getJSONArray("session_intervals").getJSONObject(0).put("sessionId",99999)},
                {sections(it,"identities").getJSONArray("personal_catalogue_anchors").getJSONObject(0).put("identityKey","missing")},
                {sections(it,"identities").getJSONArray("personal_catalogue_identities").getJSONObject(0).put("canonicalId","invalid")},
                {sections(it,"recovery").getJSONArray("drafts").getJSONObject(0).put("sha256","0".repeat(64))},
                {sections(it,"journal").getJSONArray("session_parts").getJSONObject(0).put("durationMillis",-1)},
                {sections(it,"preferences").getJSONObject("local").put("reminderHour",24)},
                {sections(it,"identities").getJSONArray("personal_catalogue_identities").getJSONObject(0).put("kind","artist")}
            )
            for(mutate in mutations){val root=JSONObject(fixture());mutate(root);rejected{e.repo().restore(signed(root))}}
            val broken=JSONObject(fixture());sections(broken,"journal").getJSONArray("pieces").getJSONObject(0).put("notes","damaged");rejected{e.repo().restore(broken.toString())}
            for(text in listOf("{", "{\"version\":5,\"version\":4}","{\"x\":\"\\ud800\"}","[".repeat(100)+"]".repeat(100)))rejected{e.repo().restore(text)}
            assertEquals("Destination notes",e.db.dao().piece(41)!!.notes);assertArrayEquals(original,BackupState.rawDraft(e.db)!!.first)
        } finally {e.close()}
    }
    @Test fun failureBeforeCommitLeavesDestinationIntactAndAfterCommitReplaysIdempotently()=runBlocking<Unit> {
        for(point in listOf("validated","destination_preserved","before_commit","after_commit","before_preferences","after_preferences","before_ack")) {
            val e=Env(context)
            try {
                seed(e);val original=BackupState.rawDraft(e.db)!!.first
                rejected{e.repo(phase={if(it==point)error("Injected $point")}).restore(fixture())}
                e.db.close();e.db=e.open()
                if(point in setOf("validated","destination_preserved","before_commit")) {
                    assertEquals("Destination notes",e.db.dao().piece(41)!!.notes);assertArrayEquals(original,BackupState.rawDraft(e.db)!!.first)
                } else {
                    assertEquals("pending",e.db.backupStateDao().state()!!.status)
                    assertEquals(8000L,e.db.dao().allSessions().sumOf{it.durationMillis})
                    e.repo().validate(e.repo().export()) // desired settings remain exportable while pending
                    e.repo().replayPendingPreferences();e.repo().replayPendingPreferences()
                    assertEquals("applied",e.db.backupStateDao().state()!!.status);assertFalse(e.preferences.snapshot().getBoolean("previousMeteredConsent"))
                    assertTrue(e.db.backupStateDao().evidence().any{it.rawBytes.contentEquals(original)})
                }
            } finally {e.close()}
        }
    }
    @Test fun exportIsAtomicOnLowSpaceAndInterruptedWritesAndInputIsBounded()=runBlocking<Unit> {
        val e=Env(context)
        try {
            e.repo().restore(fixture());val file=File(e.directory,"published.json");e.repo().exportTo(file);val before=file.readBytes()
            rejected{e.repo(files=BackupFiles(available={0})).exportTo(file)};assertArrayEquals(before,file.readBytes())
            rejected{e.repo(files=BackupFiles(fail={if(it=="export_chunk")throw IOException("Injected ENOSPC")})).exportTo(file)}
            assertArrayEquals(before,android.util.AtomicFile(file).openRead().use{it.readBytes()})
            val input=object:InputStream(){var count=0;override fun read():Int=if(count++<=BackupRepository.MAX_BYTES)32 else -1;override fun read(b:ByteArray,off:Int,len:Int):Int{if(count>BackupRepository.MAX_BYTES)return -1;val n=minOf(len,BackupRepository.MAX_BYTES-count+1);b.fill(32,off,off+n);count+=n;return n}}
            rejected{e.repo().receive(input)};assertEquals(8000L,e.db.dao().allSessions().sumOf{it.durationMillis})
        } finally {e.close()}
    }
    @Test fun conflictingEvidenceIdsKeepBothPayloads()=runBlocking<Unit> {
        val e=Env(context)
        try {
            val root=JSONObject(fixture());val row=sections(root,"recovery").getJSONArray("evidence").getJSONObject(0)
            val old="Different destination evidence".toByteArray()
            e.db.backupStateDao().evidence(RecoveryEvidence(row.getString("id"),old,BackupState.sha(old),12,13,"destination","unreadable",null))
            e.repo().restore(root.toString())
            val entries=e.db.backupStateDao().evidence()
            assertTrue(entries.any{it.rawBytes.contentEquals(old)});assertTrue(entries.any{it.rawBytes.contentEquals(bytes(row))})
        } finally {e.close()}
    }
    @Test fun restoredForeignScopeRebuildsCatalogueAndKeepsCanonicalIds()=runBlocking<Unit> {
        val e=Env(context);val name="stage3b-catalogue-${e.id}.db";val store=LocalCatalogueStore(context,e.db,name)
        try {
            e.db.dao().save(Piece(id=1,title="Old catalogue",composer="Old",catalogWorkId="100"));store.initialize()
            e.repo().restore(fixture());assertTrue(store.initialize() is CatalogueAccess.Available)
            val ids=e.db.personalCatalogueDao().identities("",100).map{it.canonicalId}.toSet()
            val catalogue=(store.access{it.page(limit=100)} as CatalogueAccess.Available).value.map{it.id}.toSet()
            assertEquals(ids,catalogue)
            assertEquals(CatalogueAccess.Available(Unit),store.rebuild());assertEquals(8000L,e.db.dao().allSessions().sumOf{it.durationMillis})
        } finally {store.close();e.close();context.getDatabasePath(name).parentFile!!.listFiles()?.filter{it.name.startsWith(name)}?.forEach{it.delete()}}
    }
    @Test fun coordinatorReplacementCannotClearTheImportedDraft()=runBlocking<Unit> {
        val e=Env(context);val coordinator=PracticeCoordinator(PracticeRepository(e.db,OpusRepository(e.db,AchievementStore(e.db))))
        try {
            withTimeout(5000){coordinator.state.first{it.ready}}
            coordinator.replacing(preservesRecovery=true){e.repo().restore(fixture())}
            assertEquals("active-fixture",coordinator.state.value.token);assertEquals(1500L,coordinator.state.value.elapsed)
            assertFalse(coordinator.state.value.running);assertNotNull(BackupState.rawDraft(e.db))
        } finally {coordinator.close();e.close()}
    }
    @Test fun measuredJournalAndLargeRawEvidenceRoundTripWithoutCursorTruncation()=runBlocking<Unit> {
        val e=Env(context)
        try {
            e.repo().restore(fixture())
            e.db.withTransaction {
                val sql=e.db.openHelper.writableDatabase
                repeat(1000){i->
                    val id=10000L+i
                    sql.execSQL("INSERT INTO sessions(id,pieceId,pieceTitle,startedAt,completedAt,durationMillis,notes) VALUES(?,7,'Measurement snapshot',1000,2000,1000,?)",arrayOf(id,"n".repeat(1024)))
                    sql.execSQL("INSERT INTO session_parts(sessionId,pieceId,title,composer,catalogWorkId,composerId,musicalEra,durationMillis,notes) VALUES(?,7,'Measurement snapshot','Synthetic',NULL,NULL,'',1000,'exact')",arrayOf(id))
                }
            }
            val payload=ByteArray(2*1024*1024+4096){(it%251).toByte()}
            e.db.withTransaction{BackupState.preserve(e.db,payload,777,"measurement","unreadable")}
            fun now()=android.os.SystemClock.elapsedRealtimeNanos()
            val memory=android.os.Debug.MemoryInfo();android.os.Debug.getMemoryInfo(memory)
            val start=now();val file=e.repo().exportSnapshot();val exported=now()
            e.repo().validate(file);val validated=now()
            e.repo().restore(file);val restored=now()
            e.repo().restore(file);val repeated=now()
            assertEquals(1002,e.db.dao().allSessions().size);assertEquals(1008000L,e.db.dao().allSessions().sumOf{it.durationMillis})
            val output=JSONObject(e.repo().export());val raw=sections(output,"recovery").getJSONArray("evidence")
            assertTrue((0 until raw.length()).any{bytes(raw.getJSONObject(it)).contentEquals(payload)})
            val result=JSONObject().put("api",android.os.Build.VERSION.SDK_INT).put("sessions",1002).put("sessionNoteBytes",1024000).put("rawEvidenceBytes",payload.size).put("backupBytes",file.length())
                .put("exportMillis",(exported-start)/1000000).put("validationMillis",(validated-exported)/1000000).put("restoreMillis",(restored-validated)/1000000).put("repeatRestoreMillis",(repeated-restored)/1000000).put("beforePssKiB",memory.totalPss)
            android.os.Debug.getMemoryInfo(memory);result.put("afterPssKiB",memory.totalPss)
            File(context.filesDir,"stage3b-performance-api${android.os.Build.VERSION.SDK_INT}.json").writeText(result.toString(2))
        } finally {e.close()}
    }

    @Test fun malformedDraftTimingIsPreservedRatherThanInvented()=runBlocking<Unit> {
        val e=Env(context)
        try {
            for(change in listOf<(JSONObject)->Unit>(
                {it.put("intervals",JSONArray())},
                {it.getJSONArray("intervals").getJSONObject(0).put("part",1)},
                {it.getJSONArray("parts").getJSONObject(0).put("duration",Long.MAX_VALUE)},
                {it.put("version","1")}, {it.put("token",false)}, {it.put("credited","1500")}, {it.put("notes",123)}, {it.put("active",4294967296L)}
            )) {
                val root=JSONObject(fixture());val draft=JSONObject(bytes(sections(root,"recovery").getJSONArray("drafts").getJSONObject(0)).toString(Charsets.UTF_8))
                change(draft);val raw=draft.toString().toByteArray();replaceDraft(root,raw);e.repo().restore(signed(root))
                rejected{PracticeRepository(e.db,OpusRepository(e.db,AchievementStore(e.db))).recover()}
                assertArrayEquals(raw,BackupState.rawDraft(e.db)!!.first);assertEquals(8000L,e.db.dao().allSessions().sumOf{it.durationMillis})
            }
        } finally {e.close()}
    }

    @Test fun destinationIdentityEvidenceSurvivesEvenWithoutDraftsOrArchives()=runBlocking<Unit> {
        val e=Env(context)
        try {
            e.db.dao().save(Piece(id=41,title="Manual original",composer="Original"))
            val anchor=e.db.personalCatalogueDao().anchors("piece","41").single()
            val identity=e.db.personalCatalogueDao().identity(anchor.identityKey)!!
            assertNull(BackupState.rawDraft(e.db));assertTrue(e.db.practiceDao().archives().isEmpty())
            e.repo().restore(fixture())
            val context=e.db.backupStateDao().evidence().single{it.classification=="identity_context"}
            assertTrue(context.rawBytes.toString(Charsets.UTF_8).contains(identity.canonicalId))
            val portable=JSONObject(e.repo().export());val evidence=sections(portable,"recovery").getJSONArray("evidence")
            assertTrue((0 until evidence.length()).any{bytes(evidence.getJSONObject(it)).contentEquals(context.rawBytes)})
        } finally {e.close()}
    }

    @Test fun duplicateCompletionCannotRemoveAnUnrelatedActiveDraft()=runBlocking<Unit> {
        val e=Env(context)
        try {
            e.repo().restore(fixture());val raw=BackupState.rawDraft(e.db)!!.first
            val repository=PracticeRepository(e.db,OpusRepository(e.db,AchievementStore(e.db)))
            assertEquals(3L,repository.complete(PracticeState(token="completed-fixture",elapsed=3000)))
            assertArrayEquals(raw,BackupState.rawDraft(e.db)!!.first);assertEquals(8000L,e.db.dao().allSessions().sumOf{it.durationMillis})
        } finally {e.close()}
    }
    @Test fun unstartedNotesAndSelectionRecoverWithoutInventingPracticeCredit()=runBlocking<Unit> {
        val e=Env(context)
        try {
            val engine=PracticeEngine();engine.select(1000,Piece(7,"Prepared selection","Original"));engine.edit{it.copy(notes="Notes prepared before Start")}
            val repository=PracticeRepository(e.db,OpusRepository(e.db,AchievementStore(e.db)));repository.checkpoint(engine.state)
            val raw=BackupState.rawDraft(e.db)!!.first
            e.repo().restore(e.repo().export())
            val recovered=repository.recover()!!
            assertEquals("",recovered.token);assertEquals(0L,recovered.elapsed);assertEquals(0L,recovered.wallElapsed)
            assertEquals("Notes prepared before Start",recovered.notes);assertFalse(recovered.running)
            assertTrue(recovered.parts.any{it.title=="Prepared selection"});assertArrayEquals(raw,BackupState.rawDraft(e.db)!!.first)
            assertTrue(e.db.dao().allSessions().isEmpty())
            e.repo().validate(e.repo().export())
        } finally {e.close()}
    }

    @Test fun freshRestoreCannotReuseDeletedOwnerIdsAndAdoptTheirUuid()=runBlocking<Unit> {
        val source=Env(context);val target=Env(context)
        try {
            source.db.dao().save(Piece(id=900,title="Deleted original",composer="Original"))
            val old=source.db.personalCatalogueDao().anchors("piece","900").single()
            val uuid=source.db.personalCatalogueDao().identity(old.identityKey)!!.canonicalId
            source.db.openHelper.writableDatabase.execSQL("DELETE FROM pieces WHERE id=900")
            target.repo().restore(source.repo().export())
            target.db.dao().save(Piece(title="New separate piece",composer="New"))
            val piece=target.db.dao().allPieces().single();assertTrue(piece.id>900)
            val anchor=target.db.personalCatalogueDao().anchors("piece",piece.id.toString()).single()
            assertNotEquals(uuid,target.db.personalCatalogueDao().identity(anchor.identityKey)!!.canonicalId)
            assertEquals(uuid,target.db.personalCatalogueDao().identity(old.identityKey)!!.canonicalId)
        } finally {source.close();target.close()}
    }

}
