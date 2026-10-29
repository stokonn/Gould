package com.opus.piano

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.data.*
import com.opus.piano.data.backup.BackupJson
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class StudioBackupTest {
    private val context=InstrumentationRegistry.getInstrumentation().targetContext
    private suspend fun fixture(test:suspend (OpusDatabase,File)->Unit) {
        val db=Room.inMemoryDatabaseBuilder(context,OpusDatabase::class.java).build()
        val root=File(context.cacheDir,"studio-backup-test-${UUID.randomUUID()}");root.mkdirs()
        try {test(db,root)} finally {db.close();root.deleteRecursively()}
    }
    private fun backup(db:OpusDatabase,root:File,phase:(String)->Unit={})=BackupRepository(context,db,
        storageDirectory=File(root,"backups"),phase=phase,audioDirectory=File(root,"audio"))
    private suspend fun take(db:OpusDatabase,root:File,sample:Short):LocalAudio {
        val repo=LocalRecordingRepository(db,File(root,"audio"))
        val (row,writer)=repo.prepare(16000,"Real saved take","",null)
        writer.append(ShortArray(16000){sample},16000);return repo.finalize(row.id,writer)
    }
    @Test fun journalOnlyDisclosesExcludedBytesAndRestoresMissingRatherThanFakePlayableAudio()=runBlocking { fixture { source,root ->
        val saved=take(source,root,17);val text=backup(source,root).export();val document=JSONObject(text)
        assertEquals(6,document.getInt("version"));assertEquals(11,document.getInt("journalSchema"))
        val studio=document.getJSONObject("sections").getJSONObject("studio").getJSONObject("data")
        assertFalse(studio.getBoolean("audioFilesIncluded"));assertFalse(text.contains("rawAudio"))
        fixture { target,targetRoot ->
            backup(target,targetRoot).restore(text)
            val restored=target.studioDao().audio(saved.id)!!
            assertEquals("Missing",restored.state);assertEquals(saved.sha256,restored.sha256)
            assertFalse(File(targetRoot,"audio/${saved.id}.wav").exists())
            assertEquals(1L,StudioAchievementRules.distinctRecordings(target.studioDao().events()))
        }
        assertEquals(saved.sha256,com.opus.piano.audio.DurableWav.hash(File(root,"audio/${saved.id}.wav")))
    } }
    @Test fun journalRestorePreservesDestinationAudioAndRepeatedImportsDoNotFarmEvents()=runBlocking { fixture { source,root ->
        take(source,root,17);val text=backup(source,root).export()
        fixture { target,targetRoot ->
            val original=take(target,targetRoot,19)
            repeat(2){backup(target,targetRoot).restore(text)}
            assertEquals(2,target.studioDao().allAudio().size)
            assertEquals(2L,StudioAchievementRules.distinctRecordings(target.studioDao().events()))
            assertEquals(original.sha256,com.opus.piano.audio.DurableWav.hash(File(targetRoot,"audio/${original.id}.wav")))
        }
    } }
    @Test fun historicalV5StillImportsWithoutErasingNewAudio()=runBlocking { fixture { source,root ->
        source.personalDao().saveSettings(ProfileSettings(username="Historical Goldberg",favoriteEra="Baroque"))
        val old=JSONObject(backup(source,root).export());old.put("version",5);old.put("journalSchema",10)
        old.getJSONObject("sections").remove("studio");old.remove("sha256");old.put("sha256",BackupJson.hash(old))
        fixture { target,targetRoot ->
            val original=take(target,targetRoot,21);backup(target,targetRoot).restore(old.toString())
            assertEquals("Historical Goldberg",target.personalDao().getSettings()!!.username)
            assertEquals("Baroque",target.personalDao().getSettings()!!.favoriteEra)
            assertEquals(original,target.studioDao().audio(original.id))
        }
    } }
    @Test fun interruptedJournalTransactionRetainsDestinationAndAudio()=runBlocking { fixture { source,root ->
        source.personalDao().saveSettings(ProfileSettings(username="Incoming"));val text=backup(source,root).export()
        fixture { target,targetRoot ->
            target.personalDao().saveSettings(ProfileSettings(username="Original"))
            val original=take(target,targetRoot,23)
            var failed=false
            try {backup(target,targetRoot){if(it=="before_commit")throw java.io.IOException("injected interruption")}.restore(text)} catch(_:java.io.IOException){failed=true}
            assertTrue(failed);assertEquals("Original",target.personalDao().getSettings()!!.username)
            assertEquals(original,target.studioDao().audio(original.id))
            assertEquals(original.sha256,com.opus.piano.audio.DurableWav.hash(File(targetRoot,"audio/${original.id}.wav")))
        }
    } }
    @Test fun mediaArchivePublicationIsAndroidCompatibleAndInterruptedRestoreCanRetry()=runBlocking { fixture { source,root ->
        val original=take(source,root,31)
        val archive=java.io.ByteArrayOutputStream()
        MediaBackupRepository(context,source,File(root,"audio"),File(root,"operations"),backup(source,root)).export(archive)
        fixture { target,targetRoot ->
            target.personalDao().saveSettings(ProfileSettings(username="Before media restore"))
            var interrupt=true
            val media=MediaBackupRepository(context,target,File(targetRoot,"audio"),File(targetRoot,"operations"),backup(target,targetRoot)) {
                if(it=="media_published" && interrupt) throw java.io.IOException("injected after publication")
            }
            val operation=media.receive(archive.toByteArray().inputStream())
            try {media.restore(operation);fail("Expected injected interruption")} catch(_:java.io.IOException) { }
            assertEquals("Before media restore",target.personalDao().getSettings()!!.username)
            val published=File(targetRoot,"audio/${original.id}.wav")
            assertEquals(original.sha256,com.opus.piano.audio.DurableWav.hash(published))
            assertTrue(operation in media.pendingOperations())
            interrupt=false;media.restore(operation)
            assertEquals("Saved",target.studioDao().audio(original.id)!!.state)
            assertEquals(original.sha256,com.opus.piano.audio.DurableWav.hash(LocalRecordingRepository(target,File(targetRoot,"audio")).playable(original.id)))
            assertFalse(operation in media.pendingOperations())
            assertEquals(original.sha256,com.opus.piano.audio.DurableWav.hash(File(root,"audio/${original.id}.wav")))
        }
    } }
    @Test fun conflictingArchivePublicationNeverReplacesDestinationBytes()=runBlocking { fixture { source,root ->
        val original=take(source,root,37)
        val archive=java.io.ByteArrayOutputStream()
        MediaBackupRepository(context,source,File(root,"audio"),File(root,"operations"),backup(source,root)).export(archive)
        fixture { target,targetRoot ->
            val media=MediaBackupRepository(context,target,File(targetRoot,"audio"),File(targetRoot,"operations"),backup(target,targetRoot))
            val operation=media.receive(archive.toByteArray().inputStream())
            val destination=File(targetRoot,"audio/${original.id}.wav").apply {parentFile!!.mkdirs();writeText("Owner recovery bytes")}
            try {media.restore(operation);fail("Expected collision rejection")} catch(_:IllegalArgumentException) { }
            assertEquals("Owner recovery bytes",destination.readText())
            assertTrue(operation in media.pendingOperations())
            assertTrue(target.studioDao().allAudio().isEmpty())
        }
    } }
    @Test fun liveRecordingBlocksJournalAndMediaRestoreBeforePublication()=runBlocking { fixture { source,root ->
        val original=take(source,root,41)
        val text=backup(source,root).export()
        val archive=java.io.ByteArrayOutputStream()
        MediaBackupRepository(context,source,File(root,"audio"),File(root,"operations"),backup(source,root)).export(archive)
        fixture { target,targetRoot ->
            val repo=LocalRecordingRepository(target,File(targetRoot,"audio"))
            val (live,writer)=repo.prepare(16000,"Live destination","",null)
            writer.append(ShortArray(1600){7},1600)
            val bytes=File(targetRoot,"audio/${live.id}.wav.part").readBytes()
            try {
                try {backup(target,targetRoot).restore(text);fail("Must reject live capture")} catch(_:IllegalArgumentException) { }
                val media=MediaBackupRepository(context,target,File(targetRoot,"audio"),File(targetRoot,"operations"),backup(target,targetRoot))
                val operation=media.receive(archive.toByteArray().inputStream())
                try {media.restore(operation);fail("Must reject before publication")} catch(_:IllegalArgumentException) { }
                assertArrayEquals(bytes,File(targetRoot,"audio/${live.id}.wav.part").readBytes())
                assertFalse(File(targetRoot,"audio/${original.id}.wav").exists())
                assertEquals("Recording",target.studioDao().audio(live.id)!!.state)
                assertTrue(operation in media.pendingOperations())
            } finally {writer.close()}
        }
    } }

    @Test fun mediaArchivePreservesEveryPieceOffsetSnapshotAndSample()=runBlocking { fixture { source,root ->
        val repo=LocalRecordingRepository(source,File(root,"audio"))
        val (row,writer)=repo.prepare(16000,"First","Bach",null,pieceKey="piece:7")
        writer.append(ShortArray(8000){11},8000)
        repo.associate(row.id,8000,"piece:8","Second","Mozart")
        writer.append(ShortArray(8000){13},8000)
        val saved=repo.finalize(row.id,writer)
        val associations=source.studioDao().audioParts(row.id)
        val archive=java.io.ByteArrayOutputStream()
        MediaBackupRepository(context,source,File(root,"audio"),File(root,"operations"),backup(source,root)).export(archive)
        fixture { target,targetRoot ->
            val media=MediaBackupRepository(context,target,File(targetRoot,"audio"),File(targetRoot,"operations"),backup(target,targetRoot))
            media.restore(media.receive(archive.toByteArray().inputStream()))
            assertEquals(associations,target.studioDao().audioParts(row.id))
            assertEquals(listOf(0L,8000L),target.studioDao().audioParts(row.id).map {it.offsetSamples})
            assertEquals(saved.sha256,com.opus.piano.audio.DurableWav.hash(LocalRecordingRepository(target,File(targetRoot,"audio")).playable(row.id)))
        }
    } }

}
