package com.opus.piano

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.data.*
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GoldbergPersistenceTest {
    @Test fun opus05UpgradePreservesIdsNotesHistoryUnlocksAndLinks() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val name = "goldberg-upgrade-${System.nanoTime()}.db"
        val schema = JSONObject(instrumentation.context.assets.open("schemas/3.json").bufferedReader().use { it.readText() }).getJSONObject("database")
        val file = context.getDatabasePath(name).apply { parentFile?.mkdirs() }
        SQLiteDatabase.openOrCreateDatabase(file, null).use { old ->
            val entities = schema.getJSONArray("entities")
            for (i in 0 until entities.length()) {
                val e = entities.getJSONObject(i)
                old.execSQL(e.getString("createSql").replace("\${TABLE_NAME}", e.getString("tableName")))
                val indices = (e.optJSONArray("indices") ?: org.json.JSONArray())
                for (j in 0 until indices.length()) old.execSQL(indices.getJSONObject(j).getString("createSql").replace("\${TABLE_NAME}", e.getString("tableName")))
            }
            old.execSQL("INSERT INTO pieces(id,title,composer,difficulty,status,notes) VALUES(17,'Legacy','Bach','ADVANCED','READY','Retain me')")
            old.execSQL("INSERT INTO sessions(id,pieceId,pieceTitle,startedAt,completedAt,durationMillis) VALUES(9,17,'Legacy',1,10001,10000)")
            old.execSQL("INSERT INTO badge_unlocks VALUES('first_notes',123)")
            old.execSQL("INSERT INTO saved_recordings VALUES(5,'piece:17','https://youtu.be/abcdefghijk','','','YOUTUBE',123,1)")
            old.version = 3
        }
        val db: OpusDatabase = Room.databaseBuilder(context, OpusDatabase::class.java, name).addMigrations(*OpusDatabase.ALL_MIGRATIONS).addCallback(PersonalCatalogueBridge.callback).build()
        try {
            assertEquals("Retain me", db.dao().piece(17)!!.notes)
            assertEquals(10000L, db.dao().allSessions().single().durationMillis)
            val part = db.personalDao().allParts().single()
            assertEquals(9L, part.sessionId); assertEquals(17L, part.pieceId); assertEquals(10000L, part.durationMillis)
            assertEquals(123L, db.experienceDao().allUnlocks().single().unlockedAt)
            assertTrue(db.experienceDao().recording(5)!!.preferred)
        } finally { db.close(); context.deleteDatabase(name) }
    }
    @Test fun multiplePiecesReopenAndBackupRestoreIntoExistingJournalIsIdempotent() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "goldberg-persistence-${System.nanoTime()}.db"
        fun open(): OpusDatabase = Room.databaseBuilder(context, OpusDatabase::class.java, name).build()
        var db = open()
        try {
            db.dao().save(Piece(id = 1, title = "A", composer = "Bach", notes = "Keep"))
            db.dao().save(Piece(id = 2, title = "B", composer = "Chopin"))
            OpusRepository(db, AchievementStore(db)).complete(PracticeSession(pieceId = null, pieceTitle = "A · B", startedAt = 1, completedAt = 10001, durationMillis = 9000), listOf(SessionPart(pieceId = 1, title = "A", durationMillis = 4000, notes = "Slow passage"), SessionPart(pieceId = 2, title = "B", durationMillis = 5000)))
            db.personalDao().saveSettings(ProfileSettings(username = "Pianist", theme = "Light", showcase = "first_notes"))
            db.close(); db = open()
            assertEquals(9000L, db.personalDao().allParts().sumOf { it.durationMillis })
            assertEquals("Slow passage", db.personalDao().allParts().first().notes)
            val backup = BackupRepository(context, db)
            val raw = backup.export()
            backup.validate(raw)
            db.dao().save(Piece(id = 3, title = "After backup", composer = "Composer"))
            backup.restore(raw); backup.restore(raw)
            assertEquals(2, db.dao().allPieces().size)
            assertEquals(1, db.dao().allSessions().size)
            assertEquals(2, db.personalDao().allParts().size)
            assertEquals("first_notes", db.personalDao().getSettings()!!.showcase)
            assertEquals("Light", db.personalDao().getSettings()!!.theme)
            val before = backup.export()
            for (bad in listOf("broken", JSONObject(raw).put("version", 99).toString(), JSONObject(raw).apply { getJSONObject("tables").getJSONArray("session_parts").getJSONObject(0).put("durationMillis", -1) }.toString())) {
                var rejected = false
                try { backup.restore(bad) } catch (_: Exception) { rejected = true }
                assertTrue(rejected)
                assertEquals(2, db.dao().allPieces().size)
                assertEquals(9000L, db.dao().allSessions().single().durationMillis)
            }
            assertTrue(before.contains("Slow passage"))
        } finally { db.close(); context.deleteDatabase(name) }
    }
}
