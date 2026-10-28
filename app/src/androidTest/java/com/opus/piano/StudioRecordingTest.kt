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
import java.util.UUID

/** Repository/filesystem tests; never opens a microphone. Not a process-death test. */
@RunWith(AndroidJUnit4::class)
class StudioRecordingTest {
    private suspend fun fixture(test:suspend (OpusDatabase,LocalRecordingRepository,File)->Unit) {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val directory=File(context.cacheDir,"studio-test-${UUID.randomUUID()}")
        val db=Room.inMemoryDatabaseBuilder(context,OpusDatabase::class.java).build()
        try {test(db,LocalRecordingRepository(db,directory),directory)} finally {db.close();directory.deleteRecursively()}
    }
    @Test fun identicalSavedAudioCountsOnceAndJournalIsNeverCreated()=runBlocking { fixture { db,repo,_ ->
        repeat(2) {
            val (row,writer)=repo.prepare(16000,"Actual take","",null)
            writer.append(ShortArray(16000){i->if(i<8000)100 else 0},16000)
            val saved=repo.finalize(row.id,writer)
            assertEquals(1000L,saved.durationMillis);assertEquals(32044L,repo.playable(row.id).length())
        }
        assertEquals(2,db.studioDao().allAudio().size)
        assertEquals(1L,StudioAchievementRules.distinctRecordings(db.studioDao().events()))
        assertTrue(db.dao().allSessions().isEmpty())
    } }
    @Test fun interruptedBytesAreRetainedAndRecoverOnlyByExplicitRequest()=runBlocking { fixture { db,repo,directory ->
        val (row,writer)=repo.prepare(16000,"Interrupted","",null)
        writer.append(ShortArray(16000){17},16000);writer.close()
        val pending=File(directory,row.relativeFile);val bytes=pending.readBytes()
        repo.reconcile()
        assertEquals("Recoverable",db.studioDao().audio(row.id)!!.state)
        assertArrayEquals(bytes,pending.readBytes())
        assertFalse(File(directory,"${row.id}.wav").exists())
        val recovered=repo.recover(row.id)
        assertEquals("Saved",recovered.state);assertEquals(16000L,recovered.sampleCount)
        assertEquals(bytes.drop(44),repo.playable(row.id).readBytes().drop(44))
    } }
    @Test fun failedIntegrityLeavesOriginalExportableAndNoPlaybackAward()=runBlocking { fixture { db,repo,directory ->
        val (row,writer)=repo.prepare(16000,"Take","",null);writer.append(ShortArray(16000){7},16000)
        val saved=repo.finalize(row.id,writer)
        java.io.RandomAccessFile(File(directory,saved.relativeFile),"rw").use {it.seek(60);it.write(99)}
        var failed=false
        try {repo.verifiedPlayback(row.id,1000)} catch(_:IllegalArgumentException){failed=true}
        assertTrue(failed);assertTrue(File(directory,saved.relativeFile).exists())
        assertTrue(db.studioDao().events().none {it.kind==StudioAchievementRules.PLAYBACK_VERIFIED})
        val exported=java.io.ByteArrayOutputStream();repo.exportOriginal(row.id,exported)
        assertArrayEquals(File(directory,saved.relativeFile).readBytes(),exported.toByteArray())
    } }
    @Test fun explicitDeletionRetainsEarnedEventIdentity()=runBlocking { fixture { db,repo,_ ->
        val (row,writer)=repo.prepare(16000,"Take","",null);writer.append(ShortArray(16000){9},16000)
        repo.finalize(row.id,writer);repo.delete(row.id)
        assertEquals("Deleted",db.studioDao().audio(row.id)!!.state)
        assertEquals(1L,StudioAchievementRules.distinctRecordings(db.studioDao().events()))
    } }
}
