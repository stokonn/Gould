package com.opus.piano

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.audio.RecordingController
import com.opus.piano.data.*
import com.opus.piano.practice.PracticeCoordinator
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

@RunWith(AndroidJUnit4::class)
class RecordingLifecycleTest {
    private suspend fun fixture(test: suspend (OpusDatabase, LocalRecordingRepository, RecordingController, File) -> Unit) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(context.cacheDir, "recording-lifecycle-${UUID.randomUUID()}")
        val db = Room.inMemoryDatabaseBuilder(context, OpusDatabase::class.java).build()
        val repo = LocalRecordingRepository(db, directory)
        val controller = RecordingController(repo)
        try { test(db, repo, controller, directory) }
        finally { controller.interrupt("TestComplete"); db.close(); directory.deleteRecursively() }
    }
    @Test fun retainedCaptureCallbackCannotAppendAfterStopOrIntoNextTake() = runBlocking { fixture { _, repo, c, _ ->
        c.start(16000, "First", "", null)
        val oldSink = requireNotNull(c.sink.get())
        oldSink(ShortArray(16000) { 7 }, 16000)
        val id = c.state.value.id
        c.stop()
        val original = repo.playable(id).readBytes()
        oldSink(ShortArray(500) { 99 }, 500)
        assertArrayEquals(original, repo.playable(id).readBytes())
        c.start(16000, "Second", "", null)
        oldSink(ShortArray(500) { 99 }, 500)
        c.sink.get()!!.invoke(ShortArray(16000) { 3 }, 16000)
        c.stop()
        assertEquals(1000L, c.state.value.durationMillis)
        assertArrayEquals(original, repo.playable(id).readBytes())
    } }
    @Test fun interruptionClosesWriterAndRetainsSamplesForExplicitRecovery() = runBlocking { fixture { db, repo, c, dir ->
        c.start(16000, "Interrupted", "", null)
        val late = c.sink.get()!!
        late(ShortArray(16000) { 17 }, 16000)
        val id = c.state.value.id
        c.interrupt("RouteLost")
        assertNull(c.sink.get()); assertEquals(0, c.currentRate())
        assertEquals("Recoverable", db.studioDao().audio(id)!!.state)
        val bytes = File(dir, "$id.wav.part").readBytes()
        late(ShortArray(100), 100)
        assertArrayEquals(bytes, File(dir, "$id.wav.part").readBytes())
        assertFalse(File(dir, "$id.wav").exists())
        repo.recover(id)
        assertEquals(bytes.drop(44), repo.playable(id).readBytes().drop(44))
    } }
    @Test fun concurrentStopAndInterruptPublishAtMostOnceAndNeverLeakActiveSink() = runBlocking { fixture { db, repo, c, _ ->
        c.start(16000, "Concurrent", "", null)
        c.sink.get()!!.invoke(ShortArray(16000) { 8 }, 16000)
        val id = c.state.value.id
        coroutineScope {
            launch(Dispatchers.Default) { c.stop() }
            launch(Dispatchers.Default) { c.interrupt("Interrupted") }
            launch(Dispatchers.Default) { c.stop() }
        }
        assertNull(c.sink.get())
        if (c.state.value.phase == RecordingController.Phase.Recoverable) repo.recover(id)
        assertEquals(1000L, db.studioDao().audio(id)!!.durationMillis)
        assertEquals(1, db.studioDao().events().count { it.kind == StudioAchievementRules.RECORDING_SAVED })
    } }
    @Test fun delayedStartupReconciliationCannotMarkCurrentLiveTakeInterrupted() = runBlocking { fixture { db, _, c, _ ->
        c.start(16000,"Live","",null)
        c.sink.get()!!.invoke(ShortArray(16000){1},16000)
        c.reconcile()
        assertEquals("Recording",db.studioDao().audio(c.state.value.id)!!.state)
        assertNotNull(c.sink.get())
        c.stop()
        assertEquals(RecordingController.Phase.Saved,c.state.value.phase)
    } }
    @Test fun captureAcknowledgmentIsRequiredAndStaleAcknowledgmentCannotRestartAudio() = runBlocking { fixture { _, repo, c, dir ->
        c.start(16000,"Prepared","",null,awaitCapture=true)
        val id=c.state.value.id
        assertEquals(RecordingController.Phase.Preparing,c.state.value.phase)
        c.sink.get()!!.invoke(ShortArray(100){9},100)
        assertEquals(44L,File(dir,"$id.wav.part").length())
        c.captureStarted("obsolete")
        assertEquals(RecordingController.Phase.Preparing,c.state.value.phase)
        c.captureStarted(id)
        c.sink.get()!!.invoke(ShortArray(16000){9},16000)
        c.stop();c.captureStarted(id)
        assertEquals(RecordingController.Phase.Saved,c.state.value.phase)
        assertEquals(32044L,repo.playable(id).length())
    } }
    @Test fun rejectedPreparingCaptureRetainsOriginalAndCannotBeActivatedLate() = runBlocking { fixture { db, _, c, dir ->
        c.start(16000,"Rejected","",null,awaitCapture=true)
        val id=c.state.value.id
        c.interrupt("ForegroundRejected");c.captureStarted(id)
        assertEquals(RecordingController.Phase.Recoverable,c.state.value.phase)
        assertEquals("Recoverable",db.studioDao().audio(id)!!.state)
        assertEquals(44L,File(dir,"$id.wav.part").length())
        assertNull(c.sink.get())
    } }
    @Test fun pieceSnapshotsUseExactSampleBoundariesAndNeverReassignExistingAudio() = runBlocking { fixture { db, repo, c, _ ->
        c.start(16000,"First","Bach",null,pieceKey="piece:1")
        c.sink.get()!!.invoke(ShortArray(8000){1},8000)
        c.associate(SessionPart(pieceId=2,title="Second",composer="Mozart"))
        c.sink.get()!!.invoke(ShortArray(4000){2},4000)
        c.associate(SessionPart())
        c.associate(SessionPart(pieceId=1,title="First",composer="Bach"))
        c.linkSession("new-explicit-practice")
        c.sink.get()!!.invoke(ShortArray(4000){3},4000)
        c.stop()
        val id=c.state.value.id
        val parts=db.studioDao().audioParts(id)
        assertEquals(listOf(0L,8000L,12000L),parts.map {it.offsetSamples})
        assertEquals(listOf("piece:1","piece:2","piece:1"),parts.map {it.pieceKey})
        assertEquals("new-explicit-practice",db.studioDao().audio(id)!!.sessionToken)
        val pcm=repo.playable(id).readBytes().drop(44)
        assertEquals(32000,pcm.size)
        assertEquals(1,pcm[0].toInt());assertEquals(2,pcm[16000].toInt());assertEquals(3,pcm[24000].toInt())
        com.opus.piano.data.backup.StudioBackup.validate(db)
    } }
    @Test fun frameAlreadyBeingReadCannotBeClaimedByNewRecordingConsent() = runBlocking { fixture { _, repo, c, _ ->
        val beforeConsent=c.sink.get()
        c.start(16000,"Consent boundary","",null)
        com.opus.piano.audio.captureConsumer(beforeConsent,c.sink.get())?.invoke(ShortArray(16000){7},16000)
        val authorized=c.sink.get()
        com.opus.piano.audio.captureConsumer(authorized,c.sink.get())!!.invoke(ShortArray(16000){3},16000)
        val id=c.state.value.id
        c.stop()
        com.opus.piano.audio.captureConsumer(authorized,c.sink.get())?.invoke(ShortArray(16000){9},16000)
        assertEquals(1000L,c.state.value.durationMillis)
        val pcm=repo.playable(id).readBytes().drop(44)
        assertEquals(32000,pcm.size)
        assertTrue(pcm.chunked(2).all {it[0]==3.toByte() && it[1]==0.toByte()})
    } }
    @Test fun failedPreparationExposesNewRecoverableIdentityAndKeepsExistingBytes() = runBlocking { fixture { db, _, _, dir ->
        dir.writeText("Existing blocking file")
        val c = RecordingController(LocalRecordingRepository(db, dir))
        try { c.start(16000, "Cannot allocate", "", null); fail() } catch (_: LocalRecordingRepository.PreparationFailure) { }
        assertEquals(RecordingController.Phase.Recoverable, c.state.value.phase)
        assertTrue(c.state.value.id.isNotBlank())
        assertEquals("Recoverable", db.studioDao().audio(c.state.value.id)!!.state)
        assertEquals("Existing blocking file", dir.readText())
        assertNull(c.sink.get())
    } }
    @Test fun failedFinalizationDetachesAndClosesEmptyTakeWithoutDeletingRecovery() = runBlocking { fixture { db, _, c, dir ->
        c.start(16000, "Empty", "", null)
        val id = c.state.value.id
        try { c.stop(); fail() } catch (_: IllegalArgumentException) { }
        assertEquals(RecordingController.Phase.Recoverable, c.state.value.phase)
        assertNull(c.sink.get())
        assertEquals("Recoverable", db.studioDao().audio(id)!!.state)
        assertEquals(44L, File(dir, "$id.wav.part").length())
    } }
    @Test fun startupAssociationPersistenceDoesNotCreatePracticeCredit() = runBlocking { fixture { db, _, _, _ ->
        val time=AtomicLong(1000)
        val coordinator=PracticeCoordinator(PracticeRepository(db,OpusRepository(db,AchievementStore(db))),time::get)
        withTimeout(5000){coordinator.state.first {it.ready}}
        coordinator.sessionStarted={time.set(21000)}
        coordinator.command {start(false)}.join()
        time.set(22000)
        coordinator.command {pause()}.join()
        assertEquals(1000L,coordinator.state.value.elapsed)
        assertEquals(1000L,coordinator.state.value.wallElapsed)
        coordinator.close()
    } }
    @Test fun reviewFreezesCreditedTimeBeforeWaitingForAudioFinalization() = runBlocking { fixture { db, _, _, _ ->
        val time = AtomicLong(1000)
        val coordinator = PracticeCoordinator(PracticeRepository(db, OpusRepository(db, AchievementStore(db))), time::get)
        withTimeout(5000) { coordinator.state.first { it.ready } }
        coordinator.command { start(false) }.join()
        time.set(2500)
        var finalized = false
        coordinator.finishRecording = { time.set(22500); delay(30); finalized = true }
        coordinator.command { review() }.join()
        assertTrue(finalized)
        assertEquals("Review", coordinator.state.value.status)
        assertEquals(1500L, coordinator.state.value.elapsed)
        assertEquals(1500L, coordinator.state.value.wallElapsed)
        assertNotNull(db.practiceDao().draft())
        coordinator.close()
    } }
}
