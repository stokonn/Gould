package com.opus.piano

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.opus.piano.data.*
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.coroutines.runBlocking

@RunWith(AndroidJUnit4::class)
class Goldberg08MigrationTest {
    @Test fun schemaFiveMigrationKeepsHistoryAndSkipsOnboarding() = runBlocking {
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        val context=instrumentation.targetContext
        val name="migration08-${System.nanoTime()}.db"
        val schema=JSONObject(instrumentation.context.assets.open("schemas/5.json").bufferedReader().use { it.readText() }).getJSONObject("database")
        context.getDatabasePath(name).parentFile!!.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(name),null).use { old ->
            val entities=schema.getJSONArray("entities")
            for (i in 0 until entities.length()) {
                val entity=entities.getJSONObject(i)
                old.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}",entity.getString("tableName")))
                val indices=entity.getJSONArray("indices")
                for (j in 0 until indices.length()) old.execSQL(indices.getJSONObject(j).getString("createSql").replace("\${TABLE_NAME}",entity.getString("tableName")))
            }
            old.execSQL("INSERT INTO pieces(id,title,composer,difficulty,status,notes) VALUES(7,'Legacy','Bach','ADVANCED','POLISHING','repertoire notes')")
            old.execSQL("INSERT INTO sessions(id,pieceId,pieceTitle,startedAt,completedAt,durationMillis) VALUES(3,7,'Legacy',1000,61000,60000)")
            old.execSQL("INSERT INTO session_parts(sessionId,pieceId,title,composer,musicalEra,durationMillis,notes) VALUES(3,7,'Legacy','Bach','',60000,'piece session notes')")
            old.execSQL("INSERT INTO badge_unlocks VALUES('first_piece',123)")
            old.execSQL("INSERT INTO profile_settings VALUES(1,'Musician','','Dark','first_piece','System','streak,time,pieces,sessions')")
            old.version=5
        }
        val db=Room.databaseBuilder(context,OpusDatabase::class.java,name).addMigrations(OpusDatabase.MIGRATION_5_6).build()
        try {
            val session=db.dao().allSessions().single()
            assertEquals(3L,session.id);assertEquals(60000L,session.durationMillis);assertNull(session.elapsedMillis)
            assertEquals("piece session notes",db.personalDao().allParts().single().notes)
            assertEquals("repertoire notes",db.dao().allPieces().single().notes)
            assertEquals(123L,db.experienceDao().allUnlocks().single().unlockedAt)
            assertTrue(db.practiceDao().localNow()!!.onboardingDone)
            assertFalse(db.practiceDao().localNow()!!.reminderEnabled)
            assertEquals("Dark",db.personalDao().getSettings()!!.theme)
        } finally { db.close();context.deleteDatabase(name) }
    }
}
