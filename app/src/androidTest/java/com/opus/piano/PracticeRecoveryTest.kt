package com.opus.piano

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.opus.piano.data.*
import com.opus.piano.practice.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PracticeRecoveryTest {
    @Test fun durableDraftCompletesOnceAndRoundTripsNewBackup() = runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val name="draft08-${System.nanoTime()}.db"
        fun open()=Room.databaseBuilder(context,OpusDatabase::class.java,name).build()
        var db=open()
        try {
            fun repo()=PracticeRepository(db,OpusRepository(db,AchievementStore(db)))
            val e=PracticeEngine();e.begin(0,1000,"completion-test",false)
            e.select(1000,Piece(7,"Study","Composer"));e.review(3000)
            e.edit { it.copy(notes="Session notes",reflection="Next time",parts=it.parts.map { p -> p.copy(notes="A note") }) }
            repo().checkpoint(e.state);db.close();db=open()
            val restored=repo().recover()!!
            assertFalse(restored.running);assertEquals("Review",restored.status);assertEquals(3000L,restored.elapsed)
            val id=repo().complete(restored);assertEquals(id,repo().complete(restored))
            assertEquals(1,db.dao().allSessions().size);assertNull(db.practiceDao().draft())
            repo().editNotes(id,"Edited","Reflection",db.personalDao().allParts())
            val raw=BackupRepository(context,db).export()
            assertEquals(4,org.json.JSONObject(raw).getInt("version"))
            BackupRepository(context,db).restore(raw)
            assertEquals("Edited",db.dao().allSessions().single().notes)
            assertEquals(3000L,db.personalDao().allParts().sumOf { it.durationMillis })
            assertEquals(3000L,db.practiceDao().allIntervals().sumOf { it.creditedMillis })
        } finally { db.close();context.deleteDatabase(name) }
    }
    @Test fun oldBackupImportsAndDeviceStateDoesNotTravel() = runBlocking {
        val instrumentation=InstrumentationRegistry.getInstrumentation();val context=instrumentation.targetContext
        val db=Room.inMemoryDatabaseBuilder(context,OpusDatabase::class.java).build()
        try {
            db.practiceDao().local(LocalAppState(onboardingDone=true,reminderEnabled=false,reminderHour=7))
            val raw=instrumentation.context.assets.open("backups/goldberg-0.6.0-format1.json").bufferedReader().use { it.readText() }
            BackupRepository(context,db).restore(raw)
            assertTrue(db.dao().allSessions().all { it.elapsedMillis==null })
            assertEquals(7,db.practiceDao().localNow()!!.reminderHour)
            val export=org.json.JSONObject(BackupRepository(context,db).export()).getJSONObject("tables")
            assertFalse(export.has("practice_drafts"));assertFalse(export.has("local_app_state"))
            db.experienceDao().unlock(listOf(BadgeUnlock("curious_pianist",123)))
            db.experienceDao().unlock(listOf(BadgeUnlock("curious_pianist",456)))
            assertEquals(123L,db.experienceDao().allUnlocks().first { it.achievementId=="curious_pianist" }.unlockedAt)
        } finally { db.close() }
    }
}
