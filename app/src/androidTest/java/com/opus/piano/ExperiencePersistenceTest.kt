package com.opus.piano

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.data.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.time.ZoneId

@RunWith(AndroidJUnit4::class)
class ExperiencePersistenceTest {
    @Test fun upgradeFromV2RetainsCatalogueHistoryAndFirstVerifiedUnlockAcrossRestart() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val name = "opus-v05-${System.nanoTime()}.db"
        val file = context.getDatabasePath(name).apply { parentFile?.mkdirs() }
        val schema = JSONObject(instrumentation.context.assets.open("schemas/2.json").bufferedReader().use { it.readText() }).getJSONObject("database")
        SQLiteDatabase.openOrCreateDatabase(file, null).use { old ->
            val entities = schema.getJSONArray("entities")
            for (i in 0 until entities.length()) {
                val entity = entities.getJSONObject(i)
                old.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}", entity.getString("tableName")))
                val indices = entity.optJSONArray("indices")
                if (indices != null) for (j in 0 until indices.length()) old.execSQL(indices.getJSONObject(j).getString("createSql").replace("\${TABLE_NAME}", entity.getString("tableName")))
            }
            old.execSQL("INSERT INTO pieces VALUES (7, 'Legacy piano work', 'Legacy composer', 'ADVANCED', 'READY', 'Keep notes', '200')")
            old.execSQL("INSERT INTO sessions VALUES (3, 7, 'Legacy piano work', 1, 1767310200000, 60000)")
            old.execSQL("INSERT INTO catalog_composers VALUES ('87', 'Johann Sebastian Bach', '1685-01-01', '1750-01-01', 'Baroque')")
            old.execSQL("INSERT INTO catalog_works VALUES ('200', '87', 'Legacy piano work', '', 'Keyboard', '', 'BWV', '200', '')")
            old.execSQL("INSERT INTO catalog_cache VALUES ('retained', '{}', 123)")
            old.version = 2
        }
        fun open() = Room.databaseBuilder(context, OpusDatabase::class.java, name).addMigrations(OpusDatabase.MIGRATION_2_3, OpusDatabase.MIGRATION_3_4, OpusDatabase.MIGRATION_4_5, OpusDatabase.MIGRATION_5_6, OpusDatabase.MIGRATION_6_7).build()
        var db = open()
        try {
            assertEquals("Keep notes", db.dao().allPieces().single().notes)
            assertEquals(0L, db.dao().allPieces().single().createdAt)
            assertEquals(123L, db.catalogDao().cache("retained")!!.fetchedAt)
            val firstStore = AchievementStore(db, { 123456789L }, { ZoneId.of("Europe/Prague") })
            firstStore.evaluate()
            assertEquals(123456789L, db.experienceDao().allUnlocks().first { it.achievementId == "first_notes" }.unlockedAt)
            val day = db.dao().allSessions().single().practiceDay
            assertEquals("Europe/Prague", db.dao().allSessions().single().practiceZone)
            assertEquals("87", db.dao().allSessions().single().composerId)
            assertEquals("Baroque", db.dao().allSessions().single().musicalEra)
            db.close()
            db = open()
            val secondStore = AchievementStore(db, { 999999999L }, { ZoneId.of("America/Los_Angeles") })
            secondStore.evaluate()
            assertEquals(day, db.dao().allSessions().single().practiceDay)
            assertEquals(123456789L, db.experienceDao().allUnlocks().first { it.achievementId == "first_notes" }.unlockedAt)
            db.experienceDao().unlock(listOf(BadgeUnlock("first_notes", 1)))
            assertEquals(123456789L, db.experienceDao().allUnlocks().first { it.achievementId == "first_notes" }.unlockedAt)
            db.dao().delete(db.dao().allPieces().single())
            secondStore.evaluate()
            assertTrue(db.experienceDao().allUnlocks().any { it.achievementId == "first_piece" })
            assertEquals(60000L, db.dao().allSessions().single().durationMillis)
            assertEquals("200", db.dao().allSessions().single().catalogWorkId)
        } finally { db.close(); context.deleteDatabase(name) }
    }
    @Test fun savedInterpretationsRetainDatesAndPreferenceAndSupportEditsAndRemoval() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "opus-recordings-${System.nanoTime()}.db"
        fun open() = Room.databaseBuilder(context, OpusDatabase::class.java, name).build()
        var db = open()
        try {
            var repository = RecordingRepository(db)
            repository.save(SavedRecording(workKey = "catalog:200", url = "https://youtu.be/abcdefghijk", platform = "YOUTUBE", savedAt = 0, preferred = true))
            val one = db.experienceDao().recordingsFor("catalog:200").single()
            repository.save(SavedRecording(workKey = "catalog:200", url = "https://open.spotify.com/track/fixture123", platform = "SPOTIFY", savedAt = 0, preferred = true))
            assertEquals(1, db.experienceDao().recordingsFor("catalog:200").count { it.preferred })
            repository.save(one.copy(title = "Edited title", performer = "Performer", savedAt = 1, preferred = true))
            assertEquals(one.savedAt, db.experienceDao().recording(one.id)!!.savedAt)
            assertEquals(1, db.experienceDao().recordingsFor("catalog:200").count { it.preferred })
            var rejected = false
            try { repository.save(one.copy(id = 0)) } catch (_: IllegalArgumentException) { rejected = true }
            assertTrue(rejected)
            db.close(); db = open(); repository = RecordingRepository(db)
            assertEquals("Edited title", db.experienceDao().recording(one.id)!!.title)
            assertTrue(db.experienceDao().recording(one.id)!!.preferred)
            repository.remove(one.id)
            assertNull(db.experienceDao().recording(one.id))
            assertEquals(1, db.experienceDao().recordingsFor("catalog:200").size)
        } finally { db.close(); context.deleteDatabase(name) }
    }
}
