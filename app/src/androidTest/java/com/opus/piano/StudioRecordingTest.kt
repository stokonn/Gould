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
    @Test fun activeExportWritesNothingAndPreservesOriginalForExplicitStoppedExport()=runBlocking { fixture { db,repo,directory ->
        val (row,writer)=repo.prepare(16000,"Active take","",null)
        writer.append(ShortArray(2048){37},2048)
        val original=File(directory,row.relativeFile).readBytes()
        try {
            for (state in listOf("Preparing","Recording","Finalizing")) {
                db.studioDao().updateAudio(row.copy(state=state))
                val output=java.io.ByteArrayOutputStream()
                assertTrue(runCatching { repo.exportOriginal(row.id,output) }.isFailure)
                assertEquals(0,output.size())
                assertArrayEquals(original,File(directory,row.relativeFile).readBytes())
                assertEquals(state,db.studioDao().audio(row.id)!!.state)
            }
        } finally { writer.close() }
        repo.interrupted(row.id,"Explicit fixture interruption")
        val stopped=java.io.ByteArrayOutputStream();repo.exportOriginal(row.id,stopped)
        assertArrayEquals(original,stopped.toByteArray())
    } }
    @Test fun activeTakeAppearsInPagedRecordingsUntilExplicitDeletion()=runBlocking { fixture { db,repo,_ ->
        val (row,writer)=repo.prepare(16000,"Live take","",null)
        writer.append(ShortArray(2048){11},2048)
        val first=db.studioDao().audioPage(Long.MAX_VALUE,"~",31)
        assertEquals(listOf(row.id),first.map {it.id})
        assertEquals("Recording",first.single().state)
        val saved=repo.finalize(row.id,writer)
        assertEquals(listOf(saved.id),db.studioDao().audioPage(Long.MAX_VALUE,"~",31).map {it.id})
        repo.delete(row.id)
        assertEquals("Deleted",db.studioDao().audio(row.id)!!.state)
        assertTrue(db.studioDao().audioPage(Long.MAX_VALUE,"~",31).isEmpty())
        assertEquals(1,db.studioDao().allAudio().size)
        assertEquals("Deleted",db.studioDao().allAudio().single().state)
    } }
    @Test fun batchedAssociationPreviewIsBoundedPerRecording()=runBlocking { fixture { db,_,_ ->
        val ids = (0 until 30).map { UUID.randomUUID().toString() }
        ids.forEachIndexed { index, id ->
            db.studioDao().insertAudio(LocalAudio(id,index.toLong(),relativeFile="$id.wav",sampleRate=16000))
            db.studioDao().associate((0 until 100).map { offset ->
                LocalAudioPart(id,offset.toLong(),"piece:$offset","Piece $offset","Composer")
            })
        }
        val previews = db.studioDao().audioPartPreviews(ids,6)
        assertEquals(30 * 6,previews.size)
        assertTrue(previews.groupBy { it.recordingId }.values.all { rows ->
            rows.map { it.offsetSamples } == (0L until 6L).toList()
        })
    } }
    @Test fun metadataOnlyAudioIsUnavailableButRetainedUnverifiedBytesStayExportable()=runBlocking { fixture { db,repo,directory ->
        val id=UUID.randomUUID().toString()
        db.studioDao().insertAudio(LocalAudio(id,1,stoppedAt=2,state="Missing",relativeFile="$id.wav",sampleRate=16000,
            sampleCount=16000,byteCount=32044,sha256="1".repeat(64)))
        assertFalse(repo.originalAvailable(id))
        directory.mkdirs();val original=byteArrayOf(1,2,3);File(directory,"$id.wav").writeBytes(original)
        assertTrue(repo.originalAvailable(id))
        val output=java.io.ByteArrayOutputStream();repo.exportOriginal(id,output)
        assertArrayEquals(original,output.toByteArray())
        assertTrue(runCatching { repo.playable(id) }.isFailure)
        assertEquals("Missing",db.studioDao().audio(id)!!.state)
    } }
}
