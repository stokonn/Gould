package com.opus.piano

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CatalogDatabaseTest {
    @Test fun migrationPreservesVersionOneRepertoireAndSessions() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "opus-migration-${System.nanoTime()}.db"
        val path = context.getDatabasePath(name).apply { parentFile?.mkdirs() }
        // Real exported v1 DDL, not v2 entities masquerading as an old database.
        val schema = JSONObject(InstrumentationRegistry.getInstrumentation().context.assets.open("schemas/1.json").bufferedReader().use { it.readText() }).getJSONObject("database")
        SQLiteDatabase.openOrCreateDatabase(path, null).use { legacy ->
            val entities = schema.getJSONArray("entities")
            for (index in 0 until entities.length()) {
                val entity = entities.getJSONObject(index)
                legacy.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}", entity.getString("tableName")))
            }
            legacy.execSQL("INSERT INTO pieces VALUES (7, 'Legacy piece', 'Legacy composer', 'ADVANCED', 'POLISHING', 'Keep these notes')")
            legacy.execSQL("INSERT INTO sessions VALUES (3, 7, 'Legacy piece', 1000, 61000, 60000)")
            legacy.version = 1
        }
        val db = Room.databaseBuilder(context, OpusDatabase::class.java, name).addMigrations(OpusDatabase.MIGRATION_1_2, OpusDatabase.MIGRATION_2_3, OpusDatabase.MIGRATION_3_4).build()
        try {
            val piece = db.dao().observePieces().first().single()
            assertEquals(7L, piece.id)
            assertEquals("Keep these notes", piece.notes)
            assertEquals(Difficulty.ADVANCED, piece.difficulty)
            assertEquals(LearningStatus.POLISHING, piece.status)
            assertNull(piece.catalogWorkId)
            assertEquals(60000L, db.dao().observeSessions().first().single().durationMillis)
            db.dao().save(piece.copy(notes = "Edited after migration"))
            assertEquals("Edited after migration", db.dao().observePieces().first().single().notes)
            db.catalogDao().putCache(CatalogCache("test", "{}", 123))
            assertEquals(123L, db.catalogDao().cache("test")!!.fetchedAt)
        } finally { db.close(); context.deleteDatabase(name) }
    }

    @Test fun importsAreIdempotentAndSameTitlesFromDifferentComposersStayDistinct() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(context, OpusDatabase::class.java).build()
        try {
            val repository = CatalogRepository(db, context.filesDir)
            val composer = CatalogComposer("10001", "Fixture composer")
            val work = CatalogWork("10002", composer.id, "Fixture title")
            val results = coroutineScope { listOf(async { repository.add(work, composer) }, async { repository.add(work, composer) }).awaitAll() }
            assertEquals(1, results.count { it })
            assertEquals(Difficulty.UNRATED, db.dao().observePieces().first().single().difficulty)
            val other = CatalogComposer("10003", "Another fixture composer")
            assertTrue(repository.add(work.copy(id = "10004", composerId = other.id), other))
            db.dao().save(Piece(title = "Pièce manuelle", composer = "Auteur"))
            assertFalse(repository.add(work.copy(id = "10005", title = "Piece manuelle"), composer.copy(name = "Auteur")))
            val imported = db.dao().catalogPiece(work.id)!!
            db.dao().save(imported.copy(notes = "Personal notes"))
            assertFalse(repository.add(work, composer))
            assertEquals("Personal notes", db.dao().catalogPiece(work.id)!!.notes)
            db.dao().delete(imported)
            assertTrue(repository.add(work, composer))
        } finally { db.close() }
    }
}
