package com.opus.piano

import android.Manifest
import android.os.Build
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.audio.RecordingController
import com.opus.piano.data.LocalRecordingRepository
import com.opus.piano.service.SmartPracticeService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Actual emulator AudioRecord and foreground service. No synthesized sink callback. */
@RunWith(AndroidJUnit4::class)
class RecordingServiceTest {
    @Test fun actualMicrophoneAcknowledgesTwoExplicitTakesAndReleasesBetweenThem() = runBlocking {
        check(Build.HARDWARE in setOf("ranchu","goldfish"))
        val inst = InstrumentationRegistry.getInstrumentation()
        val app = inst.targetContext.applicationContext as OpusApplication
        inst.uiAutomation.executeShellCommand("pm grant com.opus.piano android.permission.RECORD_AUDIO").use {
            android.os.ParcelFileDescriptor.AutoCloseInputStream(it).readBytes()
        }
        assertEquals(android.content.pm.PackageManager.PERMISSION_GRANTED,
            androidx.core.content.ContextCompat.checkSelfPermission(app,Manifest.permission.RECORD_AUDIO))
        withTimeout(10000){app.practice.state.first {it.ready}}
        // Pause any test-owned timing state without discarding its durable journal/draft.
        app.practice.command { pause() }.join()
        assertTrue("Use a fixture without an unrelated unfinished practice",app.practice.state.value.token.isEmpty())
        app.practice.command { start(false) }.join()
        val manualToken=app.practice.state.value.token
        assertTrue(app.practice.state.value.running)
        val sessionsBefore=app.database.dao().allSessions().size
        try { ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            var previousId: String? = null
            repeat(2) { take ->
                SmartPracticeService.awaitReadyForNewTake(app)
                app.recording.start(RecordingController.selectRate(),"Emulator service acceptance $take","",null,awaitCapture=true)
                assertEquals(RecordingController.Phase.Preparing,app.recording.state.value.phase)
                var accepted=false
                scenario.onActivity { accepted=SmartPracticeService.startRecording(it) }
                assertTrue(accepted)
                withTimeout(8000){app.recording.state.first {it.phase==RecordingController.Phase.Recording && it.durationMillis>=200}}
                assertTrue(SmartPracticeService.captureActive.value)
                previousId?.let { recorded ->
                    app.localPlayback.play(recorded)
                    withTimeout(3000){app.localPlayback.state.first {it.blockedByCapture}}
                    assertFalse(app.localPlayback.state.value.playing)
                }
                val id=app.recording.state.value.id
                app.recording.stop()
                assertEquals(RecordingController.Phase.Saved,app.recording.state.value.phase)
                val repo=LocalRecordingRepository(app.database,File(app.filesDir,"recordings"))
                assertTrue(repo.playable(id).length()>44)
                previousId = id
                // Starting the next explicit take immediately must await ownership release.
            }
            SmartPracticeService.awaitReadyForNewTake(app)
            assertFalse(SmartPracticeService.captureActive.value)
            assertEquals(manualToken,app.practice.state.value.token)
            assertTrue("Stopping audio must preserve manual timing",app.practice.state.value.running)
            app.practice.command { pause() }.join()
            assertEquals(sessionsBefore,app.database.dao().allSessions().size)
        } } finally {
            app.recording.stop()
            SmartPracticeService.awaitReadyForNewTake(app)
            // Remove only this test's explicitly created practice. Preserve unrelated
            // recovery data and the actual recorded WAVs used as integrity evidence.
            if (app.practice.state.value.token == manualToken) {
                app.practice.command { pause(); reset() }.join()
            }
        }
    }
    @Test fun recordOnlyOnRecoveredDraftKeepsExactDraftAndDoesNotStartPractice()=runBlocking {
        check(Build.HARDWARE in setOf("ranchu","goldfish"))
        val inst=InstrumentationRegistry.getInstrumentation()
        val app=inst.targetContext.applicationContext as OpusApplication
        inst.uiAutomation.executeShellCommand("pm grant com.opus.piano android.permission.RECORD_AUDIO").use {
            android.os.ParcelFileDescriptor.AutoCloseInputStream(it).readBytes()
        }
        assertEquals(android.content.pm.PackageManager.PERMISSION_GRANTED,
            androidx.core.content.ContextCompat.checkSelfPermission(app,Manifest.permission.RECORD_AUDIO))
        withTimeout(10000){app.practice.state.first {it.ready}}
        val before=app.practice.state.value
        assertFalse("Recovered practice must remain stopped",before.running)
        val raw=app.database.practiceDao().draft()?.payload
        val sessions=app.database.dao().allSessions().size
        ActivityScenario.launch(MainActivity::class.java).use {scenario->
            SmartPracticeService.awaitReadyForNewTake(app)
            app.recording.start(RecordingController.selectRate(),"Emulator recovered-draft recording","",before.token.takeIf {it.isNotBlank()},awaitCapture=true)
            var accepted=false
            scenario.onActivity {accepted=SmartPracticeService.startRecording(it)}
            assertTrue(accepted)
            withTimeout(8000){app.recording.state.first {it.phase==RecordingController.Phase.Recording && it.durationMillis>=200}}
            val id=app.recording.state.value.id
            app.database.studioDao().allAudio().firstOrNull {it.state=="Saved" && it.id!=id}?.let { older ->
                app.localPlayback.play(older.id)
                withTimeout(3000){app.localPlayback.state.first {it.blockedByCapture}}
                assertFalse(app.localPlayback.state.value.playing)
            }
            app.recording.stop();SmartPracticeService.awaitReadyForNewTake(app)
            assertEquals(RecordingController.Phase.Saved,app.recording.state.value.phase)
            assertTrue(LocalRecordingRepository(app.database,File(app.filesDir,"recordings")).playable(id).length()>44)
            assertFalse(SmartPracticeService.captureActive.value)
            assertFalse(app.practice.state.value.running)
            assertEquals(before.token,app.practice.state.value.token)
            assertEquals(before.elapsed,app.practice.state.value.elapsed)
            assertEquals(before.notes,app.practice.state.value.notes)
            assertEquals(raw,app.database.practiceDao().draft()?.payload)
            assertEquals(sessions,app.database.dao().allSessions().size)
        }
    }

}
