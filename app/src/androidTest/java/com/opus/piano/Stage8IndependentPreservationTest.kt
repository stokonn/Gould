package com.opus.piano

import android.database.sqlite.SQLiteDatabase
import android.os.Process
import android.os.SystemClock
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.data.*
import com.opus.piano.data.backup.*
import com.opus.piano.practice.PracticeCoordinator
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.json.*
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Explicit synthetic rehearsal only; ordinary suites must report these probes skipped. */
@RunWith(AndroidJUnit4::class)
class Stage8IndependentPreservationTest {
    private val inst=InstrumentationRegistry.getInstrumentation()
    private val context=inst.targetContext
    private val args get()=InstrumentationRegistry.getArguments()
    private val directory get()=File(context.filesDir,"stage8-independent").apply{mkdirs()}
    private val run get()=requireNotNull(args.getString("goldbergStage8Run")).also{require(it.matches(Regex("[a-zA-Z0-9]+")))}
    private fun write(name:String,value:JSONObject) {
        java.io.FileOutputStream(File(directory,name)).use{it.write(value.toString(2).toByteArray());it.fd.sync()}
    }
    @Before fun syntheticEmulatorOnly() {
        assumeTrue("Requires explicit synthetic Stage8 rehearsal",args.getString("goldbergStage8")=="synthetic")
        val qemu=inst.uiAutomation.executeShellCommand("getprop ro.kernel.qemu").use{android.os.ParcelFileDescriptor.AutoCloseInputStream(it).bufferedReader().readText().trim()}
        assertEquals("1",qemu)
        assertEquals("Stage8-owned synthetic emulator",File(context.filesDir,"stage8-independent-owned").readText())
    }
    private fun open(name:String)=Room.databaseBuilder(context,OpusDatabase::class.java,name)
        .addMigrations(*OpusDatabase.ALL_MIGRATIONS).addCallback(PersonalCatalogueBridge.callback).addCallback(BackupState.callback).build()
    private inline fun <T> OpusDatabase.scoped(block:(OpusDatabase)->T):T=try{block(this)}finally{close()}
    private fun sqliteSnapshot(sql:SQLiteDatabase,shape:JSONObject?=null):JSONObject {
        val tables=shape?.keys()?.asSequence()?.toList() ?: sql.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name NOT IN ('android_metadata','room_master_table') ORDER BY name",null).use{c->buildList{while(c.moveToNext())add(c.getString(0))}}
        val result=JSONObject()
        for(table in tables) {
            val columns=shape?.getJSONObject(table)?.getJSONArray("columns") ?: sql.rawQuery("PRAGMA table_info(`$table`)",null).use{c->JSONArray().apply{while(c.moveToNext())put(c.getString(1))}}
            val expressions=(0 until columns.length()).joinToString{val n=columns.getString(it);"CASE WHEN `$n` IS NULL THEN NULL ELSE typeof(`$n`)||':'||hex(`$n`) END"}
            val rows=JSONArray()
            sql.rawQuery("SELECT $expressions FROM `$table` ORDER BY rowid",null).use{c->while(c.moveToNext())rows.put(JSONArray().apply{for(i in 0 until c.columnCount)put(if(c.isNull(i))JSONObject.NULL else c.getString(i))})}
            result.put(table,JSONObject().put("columns",columns).put("rows",rows))
        }
        return result
    }
    @Test fun interruptRealMigrationProcess() {
        val name="stage8-migration-$run.db"
        val file=context.getDatabasePath(name)
        SQLiteDatabase.openDatabase(file.path,null,SQLiteDatabase.OPEN_READONLY).use {sql->
            assertEquals(8,sql.version);write("migration-before.json",sqliteSnapshot(sql))
        }
        val interruptedMigration=object:Migration(8,9){override fun migrate(db:SupportSQLiteDatabase){
            OpusDatabase.MIGRATION_8_9.migrate(db)
            write("migration-killed.json",JSONObject().put("pid",Process.myPid()).put("point","after8to9insideRoomTransaction"))
            Process.killProcess(Process.myPid());error("Process was not terminated")
        }}
        Room.databaseBuilder(context,OpusDatabase::class.java,name).addMigrations(interruptedMigration,OpusDatabase.MIGRATION_9_10).build().scoped{it.openHelper.writableDatabase}
        fail("Expected actual process termination")
    }
    @Test fun retryInterruptedMigrationRetainsEveryOriginalCell()=runBlocking {
        val name="stage8-migration-$run.db";val expected=JSONObject(File(directory,"migration-before.json").readText())
        assertNotEquals(JSONObject(File(directory,"migration-killed.json").readText()).getInt("pid"),Process.myPid())
        SQLiteDatabase.openDatabase(context.getDatabasePath(name).path,null,SQLiteDatabase.OPEN_READWRITE).use{sql->
            assertEquals("Transaction rolled back after actual process death",8,sql.version)
            assertEquals(BackupJson.hash(expected),BackupJson.hash(sqliteSnapshot(sql,expected)))
        }
        open(name).scoped{db->
            assertEquals(10,db.openHelper.writableDatabase.version)
            SQLiteDatabase.openDatabase(context.getDatabasePath(name).path,null,SQLiteDatabase.OPEN_READONLY).use{sql->
                assertEquals(BackupJson.hash(expected),BackupJson.hash(sqliteSnapshot(sql,expected)))
                sql.rawQuery("PRAGMA integrity_check",null).use{assertTrue(it.moveToFirst());assertEquals("ok",it.getString(0))}
                sql.rawQuery("PRAGMA foreign_key_check",null).use{assertFalse(it.moveToFirst())}
            }
        }
        write("migration-retry.json",JSONObject().put("schema",10).put("allOriginalTypedCellsEqual",true).put("pid",Process.myPid()))
    }
    @Test fun upgradedJournalBackupRoundTripAndRejectedRestoreRetainData()=runBlocking {
        val source=(context.applicationContext as OpusApplication).database
        val originalDraft=BackupState.rawDraft(source)!!.first
        val backup=BackupRepository(context,source).export()
        File(directory,"upgraded-export-v5.json").writeText(backup)
        val old=JSONObject(backup)
        val name="stage8-roundtrip-$run.db";assertFalse(context.getDatabasePath(name).exists())
        open(name).scoped{destination->
            val repo=BackupRepository(context,destination,PortablePreferences(context,"stage8-roundtrip-$run-"),storageDirectory=File(directory,"restore"))
            repo.restore(backup)
            val round=JSONObject(repo.export())
            for(section in listOf("journal","identities"))assertEquals(section,BackupJson.hash(old.getJSONObject("sections").getJSONObject(section).getJSONObject("data")),BackupJson.hash(round.getJSONObject("sections").getJSONObject(section).getJSONObject("data")))
            assertArrayEquals(originalDraft,BackupState.rawDraft(destination)!!.first)
            val before=repo.export()
            val broken=JSONObject(backup).put("sha256","0".repeat(64)).toString()
            assertTrue(runCatching{repo.restore(broken)}.isFailure)
            val after=repo.export()
            for(section in listOf("journal","identities","recovery","preferences"))assertEquals(section,BackupJson.hash(JSONObject(before).getJSONObject("sections").getJSONObject(section).getJSONObject("data")),BackupJson.hash(JSONObject(after).getJSONObject("sections").getJSONObject(section).getJSONObject("data")))
            assertArrayEquals(originalDraft,BackupState.rawDraft(source)!!.first)
            write("backup-roundtrip.json",JSONObject().put("version",5).put("sourceSessions",source.dao().allSessions().size).put("restoredSessions",destination.dao().allSessions().size).put("originalDraftUnchanged",true).put("badChecksumRejectedWithoutMutation",true))
        }
    }
    @Test fun prepareLivePracticeAndTerminateProcess():Unit=runBlocking<Unit> {
        val name="stage8-live-$run.db";assertFalse(context.getDatabasePath(name).exists())
        val db=open(name);val repo=PracticeRepository(db,OpusRepository(db,AchievementStore(db)))
        val co=PracticeCoordinator(repo)
        withTimeout(10000){co.state.first{it.ready}}
        val a=Piece(8001,"Synthetic Ž A","Synthetic composer");val b=Piece(8002,"Synthetic B","Synthetic composer")
        db.dao().save(a);db.dao().save(b)
        val started=SystemClock.elapsedRealtime()
        co.command{start(false);select(a)}.join();delay(3100)
        co.command{select(b);sessionNotes("Preserve Ž session notes","Preserve reflection");note(state.value.active,"Preserve part notes")}.join();delay(3100)
        co.retryPersistence().join()
        val raw=BackupState.rawDraft(db)!!.first;val persisted=PracticeRepository.decode(raw.toString(Charsets.UTF_8))
        val observed=SystemClock.elapsedRealtime()-started
        assertTrue(co.state.value.running);assertTrue(persisted.elapsed>=5500)
        assertTrue(persisted.elapsed<=observed+100 && observed-persisted.elapsed<1000)
        assertEquals(persisted.elapsed,persisted.parts.sumOf{it.durationMillis})
        assertTrue(persisted.parts.single{it.pieceId==8001L}.durationMillis>=2500)
        assertTrue(persisted.parts.single{it.pieceId==8002L}.durationMillis>=2500)
        write("practice-before-kill.json",JSONObject().put("pid",Process.myPid()).put("credited",persisted.elapsed).put("realElapsed",observed).put("token",persisted.token).put("rawBase64",android.util.Base64.encodeToString(raw,android.util.Base64.NO_WRAP)).put("preparedWhileRunning",true))
        Process.killProcess(Process.myPid());error("Process was not terminated")
    }
    @Test fun recoverLivePracticeWithoutCountingDowntime()=runBlocking {
        val expected=JSONObject(File(directory,"practice-before-kill.json").readText())
        assertNotEquals(expected.getInt("pid"),Process.myPid())
        open("stage8-live-$run.db").scoped{db->
            val repo=PracticeRepository(db,OpusRepository(db,AchievementStore(db)));val co=PracticeCoordinator(repo)
            try {
                withTimeout(10000){co.state.first{it.ready}}
                val state=co.state.value
                assertFalse(state.running);assertEquals(expected.getLong("credited"),state.elapsed)
                assertEquals(expected.getString("token"),state.token);assertEquals("Preserve Ž session notes",state.notes)
                assertEquals(state.elapsed,state.parts.sumOf{it.durationMillis})
                state.parts.forEachIndexed{i,p->assertEquals(p.durationMillis,state.intervals.filter{it.partIndex==i}.sumOf{it.creditedMillis})}
                val raw=android.util.Base64.decode(expected.getString("rawBase64"),android.util.Base64.NO_WRAP)
                assertArrayEquals(raw,BackupState.rawDraft(db)!!.first)
                delay(1200);assertEquals(state.elapsed,co.state.value.elapsed)
                assertEquals(android.content.pm.PackageManager.PERMISSION_DENIED,androidx.core.content.ContextCompat.checkSelfPermission(context,android.Manifest.permission.RECORD_AUDIO))
                write("practice-recovery-${args.getString("round")}.json",JSONObject().put("pid",Process.myPid()).put("credited",state.elapsed).put("running",false).put("draftBytesUnchanged",true).put("downtimeNotCredited",true).put("microphonePermissionGranted",false))
            } finally {co.close()}
        }
    }
    @Test fun recoveredPracticeCompletesExactlyOnce()=runBlocking {
        val expected=JSONObject(File(directory,"practice-before-kill.json").readText())
        open("stage8-live-$run.db").scoped{db->
            val repo=PracticeRepository(db,OpusRepository(db,AchievementStore(db)));val state=repo.recover()!!
            val first=repo.complete(state);assertEquals(first,repo.complete(state))
            val sessions=db.dao().allSessions();assertEquals(1,sessions.size);assertEquals(expected.getLong("credited"),sessions.single().durationMillis)
            assertEquals(sessions.single().durationMillis,db.personalDao().allParts().sumOf{it.durationMillis})
            assertNull(BackupState.rawDraft(db));assertTrue(db.backupStateDao().evidence().any{it.rawBytes.contentEquals(android.util.Base64.decode(expected.getString("rawBase64"),android.util.Base64.NO_WRAP))})
            write("practice-completion.json",JSONObject().put("sessionId",first).put("sessionCount",1).put("credited",sessions.single().durationMillis).put("originalDraftArchived",true))
        }
    }
    private fun bundleId(kind:String)=java.util.UUID.nameUUIDFromBytes((kind+run).toByteArray()).toString()
    @Test fun prepareOfflineBundleAndPeriodicWorkForReboot()=runBlocking {
        val app=context.applicationContext as OpusApplication
        assertFalse(app.bundleNetwork.facts().connected)
        val bytes="Synthetic Stage8 offline blob Ž".toByteArray()
        val file=Stage5Fixtures.bundleFile(bytes)
        val ready=Stage5Fixtures.manifest(bundleId("ready"),files=listOf(file),state=com.opus.piano.data.offline.BundleState.READY)
        val staging=File(directory,"bundle.bin").apply{writeBytes(bytes)}
        assertTrue(app.bundleStore.installBlob(staging,file.sha256,bytes.size.toLong()))
        app.bundleStore.save(ready)
        val queued=Stage5Fixtures.manifest(bundleId("queued"),files=listOf(file))
        val enqueued=app.bundleScheduler.enqueue(queued,false,true)
        assertTrue(enqueued.enqueued)
        app.adminSettings.syncPolicy=com.opus.piano.admin.AdminSyncPolicy(background=true)
        com.opus.piano.admin.AdminSyncWorker.schedule(app)
        val wm=androidx.work.WorkManager.getInstance(app)
        val until=SystemClock.elapsedRealtime()+10000
        while(wm.getWorkInfosForUniqueWork(com.opus.piano.admin.AdminSyncWorker.NAME).get().isEmpty() && SystemClock.elapsedRealtime()<until)delay(100)
        val bundle=wm.getWorkInfosForUniqueWork(enqueued.jobId).get().single()
        val periodic=wm.getWorkInfosForUniqueWork(com.opus.piano.admin.AdminSyncWorker.NAME).get().single()
        assertEquals(androidx.work.WorkInfo.State.ENQUEUED,bundle.state)
        assertEquals(androidx.work.WorkInfo.State.ENQUEUED,periodic.state)
        write("background-before.json",JSONObject().put("bundleWorkId",bundle.id.toString()).put("periodicWorkId",periodic.id.toString()).put("blobSha256",file.sha256).put("actualNetworkConnected",false).put("wallTime",System.currentTimeMillis()))
    }
    @Test fun verifyOfflineBundleAndPeriodicWorkAfterReboot()=runBlocking {
        val app=context.applicationContext as OpusApplication
        assertFalse(app.bundleNetwork.facts().connected)
        val old=JSONObject(File(directory,"background-before.json").readText())
        val wm=androidx.work.WorkManager.getInstance(app)
        val bundle=wm.getWorkInfoById(java.util.UUID.fromString(old.getString("bundleWorkId"))).get()!!
        val periodic=wm.getWorkInfoById(java.util.UUID.fromString(old.getString("periodicWorkId"))).get()!!
        assertEquals(androidx.work.WorkInfo.State.ENQUEUED,bundle.state)
        assertEquals(androidx.work.WorkInfo.State.ENQUEUED,periodic.state)
        val ready=app.bundleStore.readPublished().manifests.single{it.bundleId==bundleId("ready")}
        assertEquals(com.opus.piano.data.offline.BundleState.READY,ready.state)
        val bytes=app.bundleStore.blobFile(old.getString("blobSha256")).readBytes()
        assertEquals(old.getString("blobSha256"),Stage5Fixtures.shaOf(bytes))
        assertFalse(requireNotNull(app.bundleJobs.get(com.opus.piano.data.offline.BundleJob.id(bundleId("queued"),1))).meteredAllowed)
        assertTrue(app.bundleScheduler.cancel(bundleId("queued"),1))
        app.adminSettings.syncPolicy=com.opus.piano.admin.AdminSyncPolicy()
        com.opus.piano.admin.AdminSyncWorker.cancel(app)
        val until=SystemClock.elapsedRealtime()+10000
        while(wm.getWorkInfoById(periodic.id).get()?.state!=androidx.work.WorkInfo.State.CANCELLED && SystemClock.elapsedRealtime()<until)delay(100)
        assertEquals(androidx.work.WorkInfo.State.CANCELLED,wm.getWorkInfoById(periodic.id).get()!!.state)
        write("background-after.json",JSONObject().put("workIdsPersisted",true).put("queuedOffline",true).put("readyBlobUnchanged",true).put("cancelled",true).put("wallElapsedMs",System.currentTimeMillis()-old.getLong("wallTime")).put("realThreeHourDeliveryVerified",false))
    }

}
