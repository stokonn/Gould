package com.opus.piano

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.data.OpusDatabase
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class ActivityMigrationTest {
    @Test fun populatedV11UpgradePreservesPersonalDataAndAddsEmptyActivityTables() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val name = "activity-v11-${UUID.randomUUID()}.db"
        val schema = JSONObject(instrumentation.context.assets.open("schemas/11.json").bufferedReader().use { it.readText() }).getJSONObject("database")
        context.getDatabasePath(name).parentFile!!.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(name), null).use { sql ->
            val entities = schema.getJSONArray("entities")
            for (i in 0 until entities.length()) {
                val entity = entities.getJSONObject(i)
                sql.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}", entity.getString("tableName")))
                val indices = entity.optJSONArray("indices") ?: org.json.JSONArray()
                for (j in 0 until indices.length()) sql.execSQL(indices.getJSONObject(j).getString("createSql").replace("\${TABLE_NAME}", entity.getString("tableName")))
            }
            sql.execSQL("INSERT INTO pieces(id,title,composer,difficulty,status,notes) VALUES(7,'Nocturne','Chopin','ADVANCED','POLISHING','Keep this')")
            sql.execSQL("INSERT INTO sessions(id,pieceId,pieceTitle,startedAt,completedAt,durationMillis,practiceDay,practiceZone) VALUES(9,7,'Nocturne',1000,61000,60000,'2026-11-10','Europe/Prague')")
            sql.execSQL("INSERT INTO session_intervals(sessionId,partIndex,startMillis,endMillis,creditedMillis,reason) VALUES(9,0,1000,61000,60000,'active')")
            sql.execSQL("INSERT INTO profile_settings(id,username,avatar,theme,showcase,favoriteEra,biography) VALUES(1,'Musician','opaque-avatar','Nocturne','first_notes','Romantic','Keep profile')")
            sql.execSQL("INSERT INTO badge_unlocks(achievementId,unlockedAt) VALUES('first_notes',123)")
            sql.execSQL("INSERT INTO practice_drafts VALUES(1,'unchanged draft',456)")
            sql.execSQL("INSERT INTO local_audio VALUES('audio-id',1,2,'Saved','recordings/audio.wav','WAV_PCM16',16000,16000,32000,'sha','Take','','', '')")
            sql.version = 11
        }

        val db = Room.databaseBuilder(context, OpusDatabase::class.java, name)
            .addMigrations(*OpusDatabase.ALL_MIGRATIONS).build()
        try {
            val sql = db.openHelper.writableDatabase
            assertEquals(12, sql.version)
            assertEquals("Keep this", db.dao().piece(7)!!.notes)
            val session = db.dao().allSessions().single()
            assertEquals(9L, session.id)
            assertEquals("2026-11-10", session.practiceDay)
            assertEquals("Europe/Prague", session.practiceZone)
            assertEquals("Keep profile", db.personalDao().getSettings()!!.biography)
            assertEquals(123L, db.experienceDao().allUnlocks().single().unlockedAt)
            sql.query("SELECT updatedAt FROM practice_drafts WHERE id=1").use { assertTrue(it.moveToFirst()); assertEquals(456L, it.getLong(0)) }
            assertEquals("sha", db.studioDao().allAudio().single().sha256)
            sql.query("SELECT creditedMillis FROM session_intervals WHERE sessionId=9").use { assertTrue(it.moveToFirst()); assertEquals(60000L, it.getLong(0)) }
            for (table in listOf("activity_settings", "activity_weekly_goals", "activity_weeks", "manual_activity_entries", "cosmetic_ownership", "activity_reward_grants", "cosmetic_loadout")) {
                sql.query("SELECT COUNT(*) FROM `$table`").use { assertTrue(it.moveToFirst()); assertEquals("new table $table remains empty", 0L, it.getLong(0)) }
            }
            sql.query("PRAGMA foreign_key_check").use { assertFalse(it.moveToFirst()) }
        } finally { db.close(); context.deleteDatabase(name) }
    }
}
