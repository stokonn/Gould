package com.opus.piano

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.data.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Regression contract: annotations must never become fabricated measured practice. */
@RunWith(AndroidJUnit4::class)
class Redesign010PersistenceTest {
    @Test fun favoritesRemainIndependentOfFeaturedArtistAndSurviveBackup()=runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val db=Room.inMemoryDatabaseBuilder(context,OpusDatabase::class.java).build()
        try {
            db.catalogDao().putComposers(listOf(CatalogComposer("1","Bach"),CatalogComposer("2","Chopin")))
            val profile=ProfileSettings(username="Manual",favoriteArtist="Featured local artist",biography="Keep me")
            db.personalDao().saveSettings(profile)
            val edits=JournalEdits(db);edits.favorite("1",true);edits.favorite("1",true);edits.favorite("2",true)
            assertEquals(2,db.journalEditsDao().allFavorites().size)
            assertEquals(profile,db.personalDao().getSettings())
            val backup=BackupRepository(context,db);backup.restore(backup.exportLegacy())
            assertEquals(2,db.journalEditsDao().allFavorites().size);assertEquals(profile,db.personalDao().getSettings())
            edits.favorite("1",false);assertEquals(listOf("2"),db.journalEditsDao().allFavorites().map {it.composerId})
            try {edits.favorite("missing",true);fail()} catch(_:IllegalArgumentException) {}
        } finally {db.close()}
    }
    @Test fun schemaSevenMigrationPreservesEveryFixtureFieldAndUnfinishedDraft()=runBlocking {
        val instrumentation=InstrumentationRegistry.getInstrumentation();val context=instrumentation.targetContext
        val name="redesign010-${System.nanoTime()}.db";val path=context.getDatabasePath(name).apply {parentFile!!.mkdirs()}
        val schema=org.json.JSONObject(instrumentation.context.assets.open("schemas/7.json").bufferedReader().use {it.readText()}).getJSONObject("database")
        val tables=org.json.JSONObject(instrumentation.context.assets.open("backups/goldberg-0.9.2-format3.json").bufferedReader().use {it.readText()}).getJSONObject("tables")
        android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(path,null).use {old->
            val entities=schema.getJSONArray("entities")
            for(i in 0 until entities.length()) {val entity=entities.getJSONObject(i);val table=entity.getString("tableName");old.execSQL(entity.getString("createSql").replace("${'$'}{TABLE_NAME}",table));val indices=entity.optJSONArray("indices") ?: org.json.JSONArray();for(j in 0 until indices.length()) old.execSQL(indices.getJSONObject(j).getString("createSql").replace("${'$'}{TABLE_NAME}",table))}
            tables.keys().forEach {table->val rows=tables.getJSONArray(table);for(i in 0 until rows.length()) {val row=rows.getJSONObject(i);val keys=row.keys().asSequence().toList();old.execSQL("INSERT INTO `$table` (${keys.joinToString()}) VALUES (${keys.joinToString {"?"}})",keys.map {key->row.get(key).let {if(it===org.json.JSONObject.NULL) null else it}}.toTypedArray())}}
            old.execSQL("INSERT INTO practice_drafts VALUES(1,'unchanged recovery payload',123)");old.execSQL("INSERT INTO local_app_state VALUES(1,1,0,18,0,0,'')");old.version=7
        }
        val db=Room.databaseBuilder(context,OpusDatabase::class.java,name).addMigrations(*OpusDatabase.ALL_MIGRATIONS).addCallback(PersonalCatalogueBridge.callback).build()
        try {
            assertEquals(37,db.dao().allSessions().size);assertEquals(2220000L,db.dao().allSessions().sumOf {it.durationMillis})
            assertEquals("unchanged recovery payload",db.practiceDao().draft()!!.payload)
            assertEquals("Preserved biography",db.personalDao().getSettings()!!.biography)
            assertEquals("Existing manual artist",db.personalDao().getSettings()!!.favoriteArtist)
            assertEquals(123456789L,db.experienceDao().allUnlocks().first {it.achievementId=="first_notes"}.unlockedAt)
            assertTrue(db.journalEditsDao().allLinks().isEmpty());assertTrue(db.journalEditsDao().allFavorites().isEmpty())
            assertEquals(11,db.openHelper.writableDatabase.version)
        } finally {db.close();context.deleteDatabase(name)}
    }
    @Test fun omittedPieceIsIdempotentUntimedAndDoesNotChangeOriginalJournal() = runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val db=Room.inMemoryDatabaseBuilder(context,OpusDatabase::class.java).build()
        try {
            db.dao().save(Piece(1,"Measured","Bach"));db.dao().save(Piece(2,"Omitted","Chopin"))
            val journal=OpusRepository(db,AchievementStore(db))
            journal.complete(PracticeSession(pieceId=1,pieceTitle="Measured",startedAt=1000,completedAt=61000,durationMillis=60000,notes="Keep notes",reflection="Keep reflection"))
            val before=db.dao().allSessions().single();val parts=db.personalDao().allParts()
            val editor=JournalEdits(db)
            editor.addPiece(before.id,2);editor.addPiece(before.id,2)
            assertEquals(before,db.practiceDao().session(before.id));assertEquals(parts,db.personalDao().allParts())
            assertEquals(1,db.journalEditsDao().allLinks().size)
            val history=db.practiceDao().historyPage(2,null,Long.MAX_VALUE,Long.MAX_VALUE,31)
            assertEquals(listOf(before.id),history.map {it.id})
            val attributed=PracticeInsights.forPiece(listOf(before),parts,2,null,db.journalEditsDao().allLinks())
            assertEquals(1,attributed.size);assertEquals(0L,attributed.single().durationMillis)
            assertEquals(60000L,summarizeJournal(listOf(before),parts).totalMillis)
            editor.removePiece(before.id,2)
            assertTrue(db.journalEditsDao().allLinks().isEmpty());assertEquals(before,db.practiceDao().session(before.id))
        } finally { db.close() }
    }
    @Test fun legacyRecordAndNotesSurviveAssociationAndBackupRoundTrip() = runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val db=Room.inMemoryDatabaseBuilder(context,OpusDatabase::class.java).build()
        try {
            db.dao().save(Piece(2,"Added later","Composer"))
            val id=db.dao().insert(PracticeSession(pieceId=null,pieceTitle="Legacy free practice",startedAt=1000,completedAt=61000,durationMillis=60000,notes="Original"))
            val before=db.practiceDao().session(id)
            JournalEdits(db).addPiece(id,2)
            assertTrue(db.personalDao().allParts().isEmpty())
            val backup=BackupRepository(context,db);val raw=backup.exportLegacy();backup.restore(raw)
            assertEquals(before,db.practiceDao().session(id));assertEquals(1,db.journalEditsDao().allLinks().size)
            assertEquals(0,db.personalDao().allParts().size)
        } finally { db.close() }
    }
    @Test fun missingOrAlreadyMeasuredPieceCannotBecomeAnotherCredit()=runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val db=Room.inMemoryDatabaseBuilder(context,OpusDatabase::class.java).build()
        try {
            db.dao().save(Piece(1,"Measured","Composer"))
            OpusRepository(db,AchievementStore(db)).complete(PracticeSession(pieceId=1,pieceTitle="Measured",startedAt=1000,completedAt=61000,durationMillis=60000))
            val id=db.dao().allSessions().single().id
            for(piece in listOf(1L,999L)) try {JournalEdits(db).addPiece(id,piece);fail()} catch(_:IllegalArgumentException) {}
            assertTrue(db.journalEditsDao().allLinks().isEmpty());assertEquals(60000L,db.dao().allSessions().single().durationMillis)
        } finally { db.close() }
    }
}
