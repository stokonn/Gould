package com.opus.piano

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.data.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class SmartCachePersistenceTest {
    @Test fun freshCatalogueAvoidsRequestsAndExpiredCatalogueRemainsAvailableOffline() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(context, OpusDatabase::class.java).build()
        var requests = 0
        val offline = object : CatalogHttp() {
            override suspend fun json(url: String): org.json.JSONObject { requests++; throw java.io.IOException("Offline test") }
        }
        try {
            val payload = """{"status":{"success":"true"},"composers":[]}"""
            db.catalogDao().putCache(CatalogCache("composer/list/pop.json", payload, System.currentTimeMillis()))
            val cache = CatalogRepository(db, context.cacheDir, offline)
            assertTrue(cache.featured().cached)
            assertEquals(0, requests)
            db.catalogDao().putCache(CatalogCache("composer/list/pop.json", payload, 1))
            val saved = cache.featured()
            assertTrue(saved.cached)
            assertNotNull(saved.warning)
            assertEquals(1, requests)
            val composer = CatalogComposer("1", "Composer")
            val work = CatalogWork("2", "1", "Work")
            assertTrue(cache.add(work, composer))
            assertFalse(cache.add(work, composer))
            assertEquals(1, db.dao().allPieces().size)
        } finally { db.close() }
    }
    @Test fun clearingDownloadsPreservesJournalCatalogueUnlocksAndPreferences() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(context, OpusDatabase::class.java).build()
        val directory = File(context.cacheDir, "cache-test-${System.nanoTime()}").apply { mkdirs() }
        try {
            val id = 7L
            db.dao().save(Piece(id = id, title = "Retained", composer = "Bach"))
            val repository = OpusRepository(db, AchievementStore(db))
            repository.complete(PracticeSession(pieceId = id, pieceTitle = "Retained", startedAt = 1, completedAt = 60001, durationMillis = 60000))
            val settings = ProfileSettings(username = "Musician", theme = "Dark", typography = "System", statistics = "sessions,streak,time,pieces")
            db.personalDao().saveSettings(settings)
            db.catalogDao().putComposers(listOf(CatalogComposer("1", "Composer", "1685", "1750", "Baroque")))
            db.catalogDao().putCache(CatalogCache("copy", "{}", 1))
            val portraits = File(directory, "catalog-portraits").apply { mkdirs() }
            File(portraits, "portrait.img").writeBytes(ByteArray(256))
            val before = BackupRepository(context, db).export()
            val unlocks = db.experienceDao().allUnlocks()
            val cache = CatalogRepository(db, directory)
            assertTrue(cache.cacheBytes() > 0)
            cache.clearCache()
            assertEquals(0L, cache.cacheBytes())
            assertEquals(settings, db.personalDao().getSettings())
            assertEquals(1, db.dao().allPieces().size)
            assertEquals(60000L, db.dao().allSessions().single().durationMillis)
            assertEquals(1, db.catalogDao().allComposers().size)
            assertEquals(unlocks, db.experienceDao().allUnlocks())
            val backup = BackupRepository(context, db)
            backup.restore(before)
            backup.restore(before)
            assertEquals(settings, db.personalDao().getSettings())
            assertEquals(1, db.dao().allSessions().size)
            assertEquals(unlocks, db.experienceDao().allUnlocks())
        } finally { db.close(); directory.deleteRecursively() }
    }
    @Test fun upgradeActualSchemaFourAndReopenWithoutLosingProfileOrHistory() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val name = "upgrade-07-${System.nanoTime()}.db"
        val file = context.getDatabasePath(name)
        file.parentFile!!.mkdirs()
        val schema = org.json.JSONObject(instrumentation.context.assets.open("schemas/4.json").bufferedReader().use { it.readText() }).getJSONObject("database")
        android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(file, null).use { sql ->
            val entities = schema.getJSONArray("entities")
            for (i in 0 until entities.length()) {
                val entity = entities.getJSONObject(i)
                sql.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}", entity.getString("tableName")))
                val indices = (entity.optJSONArray("indices") ?: org.json.JSONArray())
                for (j in 0 until indices.length()) sql.execSQL(indices.getJSONObject(j).getString("createSql").replace("\${TABLE_NAME}", entity.getString("tableName")))
            }
            sql.execSQL("INSERT INTO profile_settings VALUES(1,'Legacy musician','','Dark','first_notes')")
            sql.execSQL("INSERT INTO badge_unlocks VALUES('first_notes',123)")
            sql.execSQL("INSERT INTO sessions(id,pieceId,pieceTitle,startedAt,completedAt,durationMillis,musicalEra) VALUES(3,NULL,'Legacy practice',1,60001,60000,'')")
            sql.execSQL("INSERT INTO session_parts(id,sessionId,pieceId,title,composer,musicalEra,durationMillis,notes) VALUES(1,3,NULL,'Legacy practice','','',60000,'Keep notes')")
            sql.version = 4
        }
        fun open() = Room.databaseBuilder(context, OpusDatabase::class.java, name).addMigrations(OpusDatabase.MIGRATION_4_5, OpusDatabase.MIGRATION_5_6, OpusDatabase.MIGRATION_6_7).build()
        try {
            open().let { db -> try {
                assertEquals("Editorial", db.personalDao().getSettings()!!.typography)
                assertEquals("Dark", db.personalDao().getSettings()!!.theme)
                db.personalDao().saveSettings(db.personalDao().getSettings()!!.copy(typography = "System", statistics = "streak,sessions,pieces,time"))
            } finally { db.close() } }
            open().let { db -> try {
                assertEquals("System", db.personalDao().getSettings()!!.typography)
                assertEquals("streak,sessions,pieces,time", db.personalDao().getSettings()!!.statistics)
                assertEquals(listOf("first_notes"), db.personalDao().getSettings()!!.badgeIds)
                assertEquals(123L, db.experienceDao().allUnlocks().single().unlockedAt)
                assertEquals(60000L, db.dao().allSessions().single().durationMillis)
                assertEquals("Keep notes", db.personalDao().allParts().single().notes)
            } finally { db.close() } }
        } finally { context.deleteDatabase(name) }
    }
    @Test fun schemaFourBackupGainsDefaultsAndKeepsExistingShowcaseAndDates() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val db = Room.inMemoryDatabaseBuilder(context, OpusDatabase::class.java).build()
        try {
            val raw = instrumentation.context.assets.open("backups/goldberg-0.6.0-format1.json").bufferedReader().use { it.readText() }
            BackupRepository(context, db).restore(raw)
            val settings = db.personalDao().getSettings()!!
            assertEquals("Editorial", settings.typography)
            assertEquals(StatisticLayout.defaults.joinToString(","), settings.statistics)
            assertEquals(listOf("first_notes"), settings.badgeIds)
            assertEquals(123L, db.experienceDao().allUnlocks().single().unlockedAt)
        } finally { db.close() }
    }
}
