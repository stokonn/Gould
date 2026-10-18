package com.opus.piano

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.data.*
import com.opus.piano.data.backup.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Run only by the explicit-emulator probe. prepare deliberately terminates instrumentation. */
@RunWith(AndroidJUnit4::class)
class Stage3bProcessDeathTest {
    private val inst=InstrumentationRegistry.getInstrumentation()
    private val context=inst.targetContext
    private val phase get()=InstrumentationRegistry.getArguments().getString("restorePhase")
    private fun open(name:String)=Room.databaseBuilder(context,OpusDatabase::class.java,name).addMigrations(*OpusDatabase.ALL_MIGRATIONS).addCallback(PersonalCatalogueBridge.callback).addCallback(BackupState.callback).build()
    @Test fun prepareActualProcessDeath()=runBlocking {
        val phase=phase ?: return@runBlocking // ordinary full suite does not kill its process
        require(phase in setOf("before_commit","after_commit","after_preferences","before_ack"))
        val name="stage3b-process-$phase.db";context.deleteDatabase(name)
        val db=open(name)
        db.dao().save(Piece(id=41,title="Destination",composer="Manual",notes="Exact original before process death"))
        db.practiceDao().checkpoint(PracticeDraft(payload="Original process-death recovery\u0000Ž",updatedAt=555))
        val input=inst.context.assets.open("stage3b/full-v5.json").bufferedReader().use{it.readText()}
        val preferences=PortablePreferences(context,"stage3b-process-$phase-")
        BackupRepository(context,db,preferences,storageDirectory=File(context.filesDir,"stage3b-process-$phase"),phase={point->
            if(point==phase) {
                java.io.FileOutputStream(File(context.filesDir,"stage3b-process-$phase.marker")).use{out->out.write(point.toByteArray());out.fd.sync()}
                android.os.Process.killProcess(android.os.Process.myPid())
                error("Process termination did not occur")
            }
        }).restore(input)
        fail("Probe did not reach intended termination")
    }
    @Test fun verifyAfterActualProcessDeath()=runBlocking {
        val phase=phase ?: return@runBlocking
        val db=open("stage3b-process-$phase.db")
        try {
            val preferences=PortablePreferences(context,"stage3b-process-$phase-")
            assertEquals(phase,File(context.filesDir,"stage3b-process-$phase.marker").readText())
            val original="Original process-death recovery\u0000Ž".toByteArray()
            if(phase=="before_commit") {
                assertEquals("Exact original before process death",db.dao().piece(41)!!.notes)
                assertArrayEquals(original,BackupState.rawDraft(db)!!.first);assertNull(db.backupStateDao().state())
            } else {
                assertEquals("pending",db.backupStateDao().state()!!.status)
                assertEquals(8000L,db.dao().allSessions().sumOf{it.durationMillis})
                assertTrue(db.backupStateDao().evidence().any{it.rawBytes.contentEquals(original)})
                BackupRepository(context,db,preferences).replayPendingPreferences()
                assertEquals("applied",db.backupStateDao().state()!!.status)
                assertFalse(preferences.snapshot().getBoolean("previousMeteredConsent"))
                assertEquals("active-fixture",PracticeRepository(db,OpusRepository(db,AchievementStore(db))).recover()!!.token)
            }
            db.openHelper.readableDatabase.query("PRAGMA foreign_key_check").use{assertFalse(it.moveToFirst())}
        } finally {db.close()}
    }
}
