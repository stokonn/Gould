package com.opus.piano

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.data.*
import com.opus.piano.practice.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PracticeCoordinatorTest {
    @Test fun reconnectingObserversNeverStartsOrDuplicatesTime() = runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val db=Room.inMemoryDatabaseBuilder(context,OpusDatabase::class.java).build()
        val coordinator=PracticeCoordinator(PracticeRepository(db,OpusRepository(db,AchievementStore(db))))
        withTimeout(5000) { coordinator.state.first { it.ready } }
        assertFalse(coordinator.state.value.running)
        coordinator.command { start(false) }
        withTimeout(5000) { coordinator.state.first { it.running } }
        val token=coordinator.state.value.token
        repeat(10) { assertEquals(token,coordinator.state.first().token) }
        coordinator.command { pause() }
        withTimeout(5000) { coordinator.state.first { !it.running } }
        val credited=coordinator.state.value.elapsed
        delay(300)
        assertEquals(credited,coordinator.state.value.elapsed)
        coordinator.command { review() }
        withTimeout(5000) { coordinator.state.first { it.status=="Review" } }
        coordinator.command { save() }
        withTimeout(5000) { coordinator.state.first { it.token.isEmpty() } }
        assertEquals(1,db.dao().allSessions().size)
        // Process-scoped coordinator intentionally remains alive; no further DB work occurs when idle.
        db.close()
    }
    @Test fun obsoleteCaptureCannotInterruptAResumedManualSession() = runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val db=Room.inMemoryDatabaseBuilder(context,OpusDatabase::class.java).build()
        val coordinator=PracticeCoordinator(PracticeRepository(db,OpusRepository(db,AchievementStore(db))))
        withTimeout(5000) { coordinator.state.first { it.ready } }
        val original=coordinator.startSmart(50,5)
        coordinator.command { pause(); start(false) }
        withTimeout(5000) { coordinator.state.first { it.execution>original.execution } }
        coordinator.interrupt(original.token,original.execution)
        assertTrue(coordinator.state.value.running)
        coordinator.command { pause() }
        withTimeout(5000) { coordinator.state.first { !it.running } }
        db.close()
    }
    @Test fun failedRestoreKeepsThePausedDraft() = runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val db=Room.inMemoryDatabaseBuilder(context,OpusDatabase::class.java).build()
        val coordinator=PracticeCoordinator(PracticeRepository(db,OpusRepository(db,AchievementStore(db))))
        withTimeout(5000) { coordinator.state.first { it.ready } }
        coordinator.command { start(false) }
        withTimeout(5000) { coordinator.state.first { it.running } }
        val token=coordinator.state.value.token
        try { coordinator.replacing<Unit> { error("Simulated rejected restore") }; fail() } catch (_: IllegalStateException) { }
        assertFalse(coordinator.state.value.running);assertEquals(token,coordinator.state.value.token)
        assertNotNull(db.practiceDao().draft())
        db.close()
    }
}
