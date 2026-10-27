package com.opus.piano

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.data.*
import com.opus.piano.data.backup.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class Stage8RestoreRetryTest {
    @Test fun actualKilledRestoreCanRetryAndChecksumFailureCanRetryWithoutDataLoss()=runBlocking {
        val inst=InstrumentationRegistry.getInstrumentation();val context=inst.targetContext
        assumeTrue(InstrumentationRegistry.getArguments().getString("goldbergStage8")=="synthetic")
        assertEquals("Stage8-owned synthetic emulator",File(context.filesDir,"stage8-independent-owned").readText())
        val marker=File(context.filesDir,"stage3b-process-before_commit.marker")
        assertEquals("before_commit",marker.readText())
        val db=Room.databaseBuilder(context,OpusDatabase::class.java,"stage3b-process-before_commit.db")
            .addMigrations(*OpusDatabase.ALL_MIGRATIONS).addCallback(PersonalCatalogueBridge.callback).addCallback(BackupState.callback).build()
        try {
            val original=BackupState.rawDraft(db)!!.first
            assertEquals("Exact original before process death",db.dao().piece(41)!!.notes)
            val repo=BackupRepository(context,db,PortablePreferences(context,"stage3b-process-before_commit-"),storageDirectory=File(context.filesDir,"stage3b-process-before_commit"))
            val input=inst.context.assets.open("stage3b/full-v5.json").bufferedReader().use{it.readText()}
            repo.restore(input)
            assertEquals(8000L,db.dao().allSessions().sumOf{it.durationMillis})
            assertTrue(db.backupStateDao().evidence().any{it.rawBytes.contentEquals(original)})
            val before=org.json.JSONObject(repo.export()).getJSONObject("sections")
            val bad=org.json.JSONObject(input).put("sha256","0".repeat(64)).toString()
            assertTrue(runCatching{repo.restore(bad)}.isFailure)
            val after=org.json.JSONObject(repo.export()).getJSONObject("sections")
            for(key in listOf("journal","identities","recovery","preferences"))assertEquals(key,BackupJson.hash(before.getJSONObject(key).getJSONObject("data")),BackupJson.hash(after.getJSONObject(key).getJSONObject("data")))
            repo.restore(input)
            assertEquals(2,db.dao().allSessions().size)
            assertEquals(8000L,db.dao().allSessions().sumOf{it.durationMillis})
            assertTrue(db.backupStateDao().evidence().any{it.rawBytes.contentEquals(original)})
            db.openHelper.readableDatabase.query("PRAGMA integrity_check").use{assertTrue(it.moveToFirst());assertEquals("ok",it.getString(0))}
            db.openHelper.readableDatabase.query("PRAGMA foreign_key_check").use{assertFalse(it.moveToFirst())}
            File(context.filesDir,"stage8-independent/restore-retry.json").writeText(org.json.JSONObject().put("actualBeforeCommitKillRetry",true).put("badChecksumRetry",true).put("sessions",2).put("durationMillis",8000).put("originalDestinationDraftRetainedInEvidence",true).toString(2))
        } finally {db.close()}
    }
}
