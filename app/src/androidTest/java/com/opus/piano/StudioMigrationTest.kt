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
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class StudioMigrationTest {
    @Test fun schemaTenUpgradeRetainsOpaqueDraftProfileOriginalBadgeAndBookmarks() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val name = "studio-upgrade-${UUID.randomUUID()}.db"
        val schema = JSONObject(instrumentation.context.assets.open("schemas/10.json").bufferedReader().use { it.readText() }).getJSONObject("database")
        context.getDatabasePath(name).parentFile!!.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(name), null).use { sql ->
            val entities = schema.getJSONArray("entities")
            for (i in 0 until entities.length()) {
                val entity = entities.getJSONObject(i); val table = entity.getString("tableName")
                sql.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}", table))
                val indices = entity.getJSONArray("indices")
                for (j in 0 until indices.length()) sql.execSQL(indices.getJSONObject(j).getString("createSql").replace("\${TABLE_NAME}", table))
            }
            sql.execSQL("INSERT INTO badge_unlocks VALUES('first_notes',123)")
            sql.execSQL("INSERT INTO profile_settings(id,username,avatar,theme,showcase,favoriteEra,biography) VALUES(1,'Pianista','opaque-avatar','Dark','first_notes','Baroque','Žlutá hudba')")
            sql.execSQL("INSERT INTO practice_drafts VALUES(1,?,555)", arrayOf("opaque\u0000draft\nŽ"))
            sql.execSQL("INSERT INTO saved_recordings VALUES(7,'piece:1','https://youtu.be/abcdefghijk','Link','Artist','YOUTUBE',321,1)")
            sql.version = 10
        }
        val db = Room.databaseBuilder(context, OpusDatabase::class.java, name).addMigrations(*OpusDatabase.ALL_MIGRATIONS).build()
        try {
            assertEquals(11, db.openHelper.writableDatabase.version)
            assertEquals("Žlutá hudba", db.personalDao().getSettings()!!.biography)
            assertEquals("Baroque", db.personalDao().getSettings()!!.favoriteEra)
            db.openHelper.readableDatabase.query("SELECT hex(CAST(payload AS BLOB)) FROM practice_drafts").use { c ->
                assertTrue(c.moveToFirst());assertEquals("opaque\u0000draft\nŽ".toByteArray(Charsets.UTF_8).joinToString(""){"%02X".format(it)},c.getString(0))
            }
            assertEquals(123L, db.experienceDao().allUnlocks().single().unlockedAt)
            assertEquals("Link", db.experienceDao().recording(7)!!.title)
            db.openHelper.readableDatabase.query("SELECT acknowledgedAt FROM badge_notices WHERE achievementId='first_notes'").use { assertTrue(it.moveToFirst()); assertEquals(123L, it.getLong(0)) }
            assertTrue(db.studioDao().allAudio().isEmpty())
            db.openHelper.readableDatabase.query("PRAGMA foreign_key_check").use { assertFalse(it.moveToFirst()) }
        } finally { db.close(); context.deleteDatabase(name) }
    }
}
