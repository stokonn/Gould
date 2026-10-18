package com.opus.piano

import androidx.room.Room
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
class Stage3aBackupTest {
    @Test fun historicalReadersAndV4WriterKeepFieldsAndNoStaleLocalAnchors()=runBlocking {
        val inst=InstrumentationRegistry.getInstrumentation();val context=inst.targetContext
        val name="stage3a-backups-${UUID.randomUUID()}.db"
        val db=Room.databaseBuilder(context,OpusDatabase::class.java,name).addCallback(PersonalCatalogueBridge.callback).build()
        try {
            val backup=BackupRepository(context,db)
            for(file in listOf("goldberg-0.6.0-format1.json","goldberg-0.9.1-format2.json","goldberg-0.9.2-format3.json")) {
                val input=inst.context.assets.open("backups/$file").bufferedReader().use {it.readText()}
                backup.validate(input);backup.restore(input)
                val exported=JSONObject(backup.export())
                assertEquals(4,exported.getInt("version"));assertEquals(8,exported.getInt("schema"))
                val original=JSONObject(input).getJSONObject("tables");val actual=exported.getJSONObject("tables")
                original.keys().forEach { table ->
                    val oldRows=original.getJSONArray(table);val newRows=actual.getJSONArray(table)
                    assertEquals(table,oldRows.length(),newRows.length())
                    for(i in 0 until oldRows.length()) {
                        val row=oldRows.getJSONObject(i)
                        assertTrue("$file $table",(0 until newRows.length()).any { j -> val candidate=newRows.getJSONObject(j); row.keys().asSequence().all {k ->row.get(k)==candidate.get(k)} })
                    }
                }
                // Existing v4 export remains readable and idempotent on schema9.
                val raw=exported.toString();backup.validate(raw);backup.restore(raw);backup.restore(raw)
                assertEquals(actual.getJSONArray("sessions").length(),db.dao().allSessions().size)
                db.openHelper.readableDatabase.query("PRAGMA foreign_key_check").use {assertFalse(it.moveToFirst())}
            }
            db.dao().save(Piece(id=900,title="First manual journal",composer="User"))
            val first=db.personalCatalogueDao().anchors("piece","900").single()
            val firstUuid=db.personalCatalogueDao().identity(first.identityKey)!!.canonicalId
            val raw=JSONObject(backup.export());val pieces=raw.getJSONObject("tables").getJSONArray("pieces")
            for(i in 0 until pieces.length()) if(pieces.getJSONObject(i).getLong("id")==900L) pieces.getJSONObject(i).put("title","Different imported journal")
            backup.restore(raw.toString())
            val second=db.personalCatalogueDao().anchors("piece","900").single()
            assertNotEquals(firstUuid,db.personalCatalogueDao().identity(second.identityKey)!!.canonicalId)
            assertEquals("Different imported journal",second.originalTitle)
            assertNotNull(db.personalCatalogueDao().identity(first.identityKey)) // preserve prior identity evidence
        } finally {db.close();context.deleteDatabase(name)}
    }
    @Test fun legacyRestoreRejectsUnfinishedRecoveryWithoutChangingBytes()=runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val name="stage3a-restore-draft-${UUID.randomUUID()}.db"
        val db=Room.databaseBuilder(context,OpusDatabase::class.java,name).addCallback(PersonalCatalogueBridge.callback).build()
        try {
            db.dao().save(Piece(id=1,title="Keep",composer="User",notes="Keep notes"))
            val backup=BackupRepository(context,db);val raw=backup.export()
            val original="Unparseable original\nŽ\u0000raw";db.practiceDao().checkpoint(PracticeDraft(payload=original,updatedAt=123))
            assertTrue(runCatching {backup.restore(raw)}.isFailure)
            assertEquals(original,db.practiceDao().draft()!!.payload)
            assertEquals(123,db.practiceDao().draft()!!.updatedAt.toInt())
            assertEquals("Keep notes",db.dao().piece(1)!!.notes)
        } finally {db.close();context.deleteDatabase(name)}
    }
}
