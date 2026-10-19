package com.opus.piano

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.opus.piano.data.*
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class Stage3aMigrationTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    /** Discover may already have a real catalogue. Migrations must preserve its durable files,
     * or leave them absent when absent, rather than relying on an empty application sandbox. */
    private fun catalogueSnapshot():Map<String,String> {
        val base=com.opus.piano.data.catalogue.CatalogueDatabase.FILENAME
        return context.getDatabasePath(base).parentFile!!.listFiles().orEmpty()
            .filter {it.isFile && (it.name==base || it.name.startsWith("$base.") || it.name.startsWith("$base-")) && !it.name.endsWith("-shm")}
            .associate {file->
                val digest=java.security.MessageDigest.getInstance("SHA-256")
                file.inputStream().use {input->val buffer=ByteArray(8192);while(true){val count=input.read(buffer);if(count<0)break;digest.update(buffer,0,count)}}
                file.name to digest.digest().joinToString(""){"%02x".format(it)}
            }
    }
    private fun seed(name: String, version: Int): Map<String, Pair<List<String>, List<List<String?>>>> {
        val schema = JSONObject(instrumentation.context.assets.open("schemas/$version.json").bufferedReader().use { it.readText() }).getJSONObject("database")
        context.getDatabasePath(name).parentFile!!.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(name),null).use { db ->
            val entities=schema.getJSONArray("entities")
            for(i in 0 until entities.length()) {
                val e=entities.getJSONObject(i); val table=e.getString("tableName")
                db.execSQL(e.getString("createSql").replace("\${TABLE_NAME}",table))
                e.optJSONArray("indices")?.let { indices -> for(j in 0 until indices.length()) db.execSQL(indices.getJSONObject(j).getString("createSql").replace("\${TABLE_NAME}",table)) }
            }
            db.execSQL("INSERT INTO pieces(id,title,composer,difficulty,status,notes) VALUES(7,'Original – Žlutá','Bach','ADVANCED','POLISHING','do not edit original notes')")
            db.execSQL("INSERT INTO pieces(id,title,composer,difficulty,status,notes) VALUES(8,'Collision','Same composer','BEGINNER','PLANNED','manual one')")
            db.execSQL("INSERT INTO pieces(id,title,composer,difficulty,status,notes) VALUES(9,'Collision','Same composer','BEGINNER','PLANNED','manual two')")
            db.execSQL("INSERT INTO sessions(id,pieceId,pieceTitle,startedAt,completedAt,durationMillis) VALUES(3,7,'Historical snapshot',1000,61000,60000)")
            if(version>=2) {
                db.execSQL("UPDATE pieces SET catalogWorkId='87' WHERE id=7")
                db.execSQL("INSERT INTO catalog_works(id,composerId,title,subtitle,genre,searchTerms,catalogue,catalogueNumber,musicalKey) VALUES('87','87','Original cached work','Subtitle','Keyboard','Search','BWV','1','C')")
                db.execSQL("INSERT INTO catalog_composers VALUES('87','Same numeric artist','1685','','Baroque')")
                db.execSQL("INSERT INTO catalog_cache VALUES('retained','{\"raw\":\"cache\"}',123)")
            }
            if(version>=3) {
                db.execSQL("UPDATE sessions SET catalogWorkId='87',composerId='87',practiceDay='2026-10-18',practiceZone='Europe/Prague'")
                db.execSQL("INSERT INTO badge_unlocks VALUES('first_piece',123)")
                db.execSQL("INSERT INTO saved_recordings VALUES(12,'catalog:87','https://youtu.be/abcdefghijk','Saved link','Performer','YOUTUBE',321,1)")
            }
            if(version>=4) {
                db.execSQL("INSERT INTO session_parts(sessionId,pieceId,title,composer,catalogWorkId,composerId,musicalEra,durationMillis,notes) VALUES(3,7,'Snapshot part','Historical composer','87','87','Baroque',60000,'session note')")
                db.execSQL("INSERT INTO session_parts(sessionId,pieceId,title,composer,catalogWorkId,composerId,musicalEra,durationMillis,notes) VALUES(3,99,'Removed piece','Other','999','9','',0,'note only')")
                db.execSQL("INSERT INTO profile_settings(id,username,avatar,theme,showcase) VALUES(1,'Existing musician','profile-avatar-bytes','Dark','first_piece')")
            }
            if(version>=6) {
                db.execSQL("UPDATE sessions SET completionToken='one-credit-only',elapsedMillis=61000,detectedActiveMillis=60000,notes='session notes',reflection='retained'")
                db.execSQL("INSERT INTO local_app_state VALUES(1,1,0,18,0,4,'2026-10-18')")
                db.execSQL("INSERT INTO practice_drafts VALUES(1,?,123)",arrayOf("original malformed draft\nŽ\u0000bytes"))
                db.execSQL("INSERT INTO session_intervals(sessionId,partIndex,startMillis,endMillis,creditedMillis,reason) VALUES(3,0,0,60000,60000,'Manual')")
            }
            if(version>=7) {
                db.execSQL("UPDATE profile_settings SET biography='Original bio',favoriteArtist='Private favorite',favoritePiece='Manual title'")
                db.execSQL("INSERT INTO practice_recovery_archives(payload,updatedAt,archivedAt) VALUES(?,111,222)",arrayOf("opaque archived {bytes}"))
            }
            if(version>=8) {
                db.execSQL("INSERT INTO session_piece_links VALUES(3,8,'Untimed link','Same composer',NULL,NULL,'',123)")
                db.execSQL("INSERT INTO favorite_composers VALUES('87',444)")
            }
            db.version=version
            // Exact old columns/values. HEX(TEXT/BLOB) also protects embedded NUL/raw UTF-8 bytes.
            val result=linkedMapOf<String,Pair<List<String>,List<List<String?>>>>()
            for(i in 0 until entities.length()) {
                val table=entities.getJSONObject(i).getString("tableName")
                val columns=db.rawQuery("PRAGMA table_info(`$table`)",null).use { c -> buildList { while(c.moveToNext()) add(c.getString(1)) } }
                result[table]=columns to snapshot(db,table,columns)
            }
            return result
        }
    }
    private fun snapshot(db: SQLiteDatabase,table: String,columns: List<String>) = db.rawQuery("SELECT ${columns.joinToString { "CASE WHEN `$it` IS NULL THEN NULL ELSE typeof(`$it`)||':'||hex(`$it`) END" }} FROM `$table` ORDER BY rowid",null).use { c -> buildList { while(c.moveToNext()) add(columns.indices.map { if(c.isNull(it)) null else c.getString(it) }) } }
    private fun open(name: String) = Room.databaseBuilder(context,OpusDatabase::class.java,name).addMigrations(*OpusDatabase.ALL_MIGRATIONS).addCallback(PersonalCatalogueBridge.callback).build()
    private fun migrate(version: Int) = runBlocking {
        val catalogueBefore=catalogueSnapshot()
        val name="stage3a-v$version-${UUID.randomUUID()}.db"
        val original=seed(name,version)
        var db=open(name)
        try {
            assertEquals(10,db.openHelper.writableDatabase.version)
            db.openHelper.readableDatabase.query("PRAGMA foreign_key_check").use { assertFalse(it.moveToFirst()) }
            SQLiteDatabase.openDatabase(context.getDatabasePath(name).path,null,SQLiteDatabase.OPEN_READONLY).use { sql ->
                original.forEach { (table,data) -> assertEquals("v$version $table original bytes",data.second,snapshot(sql,table,data.first)) }
            }
            val bridge=db.personalCatalogueDao()
            val piece=bridge.anchors("piece","7").single()
            val uuid=bridge.identity(piece.identityKey)!!.canonicalId
            assertEquals(4,UUID.fromString(uuid).version())
            assertNotEquals(bridge.anchors("piece","8").single().identityKey,bridge.anchors("piece","9").single().identityKey)
            assertEquals(60000L,db.dao().allSessions().sumOf { it.durationMillis })
            if(version>=3) {
                val artist=bridge.identity("legacy:artist:87")!!
                assertNotEquals(uuid,artist.canonicalId)
                assertEquals("87",db.dao().piece(7)!!.catalogWorkId)
                assertEquals(piece.identityKey,bridge.anchors("recording","12").single().identityKey)
            }
            db.close();db=open(name)
            assertEquals(uuid,db.personalCatalogueDao().identity(piece.identityKey)!!.canonicalId)
            assertEquals("do not edit original notes",db.dao().piece(7)!!.notes)
            assertEquals("Personal migration must not create or change durable catalogue generations",catalogueBefore,catalogueSnapshot())
        } finally { db.close();context.deleteDatabase(name) }
    }
    @Test fun schema1()=migrate(1)
    @Test fun schema2()=migrate(2)
    @Test fun schema3()=migrate(3)
    @Test fun schema4()=migrate(4)
    @Test fun schema5()=migrate(5)
    @Test fun schema6()=migrate(6)
    @Test fun schema7()=migrate(7)
    @Test fun schema8()=migrate(8)
    @Test fun schema9PreservesExistingUuidRegistryAndSeedsCompletionEvidence()=runBlocking {
        val name="stage3b-v9-${UUID.randomUUID()}.db"
        val original=seed(name,9);val uuid=UUID.randomUUID().toString();val scope=UUID.randomUUID().toString()
        SQLiteDatabase.openDatabase(context.getDatabasePath(name).path,null,SQLiteDatabase.OPEN_READWRITE).use { sql ->
            sql.execSQL("INSERT INTO personal_catalogue_state VALUES(1,?)",arrayOf(scope))
            sql.execSQL("INSERT INTO personal_catalogue_identities VALUES('legacy:work:87',?,'work','openopus','87','original evidence')",arrayOf(uuid))
            sql.execSQL("INSERT INTO personal_catalogue_anchors VALUES('piece','7','work','legacy:work:87','Original title','Original creator','87','original evidence')")
        }
        val db=open(name)
        try {
            assertEquals(10,db.openHelper.writableDatabase.version)
            assertEquals(uuid,db.personalCatalogueDao().identity("legacy:work:87")!!.canonicalId)
            assertEquals(scope,db.personalCatalogueDao().scope()!!.scope)
            assertEquals("Original title",db.personalCatalogueDao().anchors("piece","7").single().originalTitle)
            assertEquals(60000L,db.backupStateDao().completed("one-credit-only").single().durationMillis)
            SQLiteDatabase.openDatabase(context.getDatabasePath(name).path,null,SQLiteDatabase.OPEN_READONLY).use { sql ->
                original.filterKeys{!it.startsWith("personal_catalogue_")}.forEach{(table,data)->assertEquals("schema9 $table bytes",data.second,snapshot(sql,table,data.first))}
            }
            db.openHelper.readableDatabase.query("PRAGMA foreign_key_check").use{assertFalse(it.moveToFirst())}
        } finally {db.close();context.deleteDatabase(name)}
    }
    @Test fun failureRollsBackPersonalMigration() {
        val name="stage3a-failed-${UUID.randomUUID()}.db";seed(name,8)
        val failing=object:Migration(8,9) { override fun migrate(db:SupportSQLiteDatabase) { OpusDatabase.MIGRATION_8_9.migrate(db);error("Injected migration interruption") } }
        val db=Room.databaseBuilder(context,OpusDatabase::class.java,name).addMigrations(failing,OpusDatabase.MIGRATION_9_10).build()
        try {
            assertTrue(runCatching { db.openHelper.writableDatabase }.isFailure)
            SQLiteDatabase.openDatabase(context.getDatabasePath(name).path,null,SQLiteDatabase.OPEN_READONLY).use { old ->
                assertEquals(8,old.version)
                old.rawQuery("SELECT name FROM sqlite_master WHERE name='personal_catalogue_anchors'",null).use { assertFalse(it.moveToFirst()) }
                old.rawQuery("SELECT hex(payload) FROM practice_drafts",null).use { assertTrue(it.moveToFirst()); assertEquals("original malformed draft\nŽ\u0000bytes".toByteArray().joinToString(""){b->"%02X".format(b)},it.getString(0)) }
            }
        } finally {db.close();context.deleteDatabase(name)}
    }
}
