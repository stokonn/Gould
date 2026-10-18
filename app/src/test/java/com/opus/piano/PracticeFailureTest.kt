package com.opus.piano

import com.opus.piano.practice.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test

class PracticeFailureTest {
    private class Store : PracticePersistence {
        var draft: PracticeState? = null
        var failWrite = false
        var failRead = false
        var writes = 0
        override suspend fun recover(): PracticeState? { check(!failRead); return draft?.copy(running=false,ready=true,status="Interrupted") }
        override suspend fun checkpoint(state: PracticeState) { check(!failWrite); draft=state; writes++ }
        override suspend fun clear() { draft=null }
        override suspend fun complete(state: PracticeState): Long { check(!failWrite); draft=null; return 1 }
        var archived: String? = null
        override suspend fun archiveRecovery(expected: String) { check(!failWrite); archived=expected; draft=null }
        override suspend fun recoveryData() = "unchanged-original"
    }
    private suspend fun ready(c: PracticeCoordinator) = withTimeout(3000) { c.state.first { it.ready } }
    private fun coordinator(s: Store, time: () -> Long = { 1000L }) = PracticeCoordinator(s, time, { "error:$it" })
    @Test fun completionPublishedOnlyAfterSuccessfulSaveAndNeverTwice() = runBlocking {
        val store=Store();val coordinator=coordinator(store);ready(coordinator)
        coordinator.command {start(false);review()}.join()
        store.failWrite=true
        coordinator.command {save()}.join()
        assertNull(coordinator.completedSession.value)
        assertEquals("Review",coordinator.state.value.status)
        assertNotNull(store.draft)
        store.failWrite=false
        coordinator.command {save()}.join()
        assertEquals(1L,coordinator.completedSession.value)
        assertFalse(coordinator.state.value.running)
        coordinator.dismissCompletion()
        coordinator.command {save()}.join()
        assertNull(coordinator.completedSession.value)
        coordinator.close()
    }
    @Test fun failedManualStartupNeverPublishesRunningOrReplacesDraft() = runBlocking {
        val s=Store(); val c=coordinator(s); ready(c)
        c.command { select(com.opus.piano.data.Piece(7,"Original","Bach")) }.join()
        val durable=s.draft
        s.failWrite=true
        c.command { start(false) }.join()
        assertFalse(c.state.value.running); assertEquals(0,c.state.value.elapsed)
        assertEquals(durable,s.draft); assertNotNull(c.notice.value)
        s.failWrite=false; c.command { start(false) }.join()
        assertTrue(c.state.value.running); assertEquals(c.state.value.token,s.draft!!.token)
        c.close()
    }
    @Test fun failedSmartStartupDoesNotExposeCaptureIdentity() = runBlocking {
        val s=Store(); val c=coordinator(s); ready(c); s.failWrite=true
        try { c.startSmart(50,5); fail() } catch (_: IllegalStateException) { }
        assertFalse(c.state.value.running); assertEquals("",c.state.value.token); assertNull(s.draft)
        c.close()
    }
    @Test fun failedRecoveryCanRetryWithoutOverwritingOriginalOrStartingCapture() = runBlocking {
        val s=Store(); s.failRead=true; val c=coordinator(s)
        withTimeout(3000) { c.recoveryFailed.first { it } }
        c.command { start(false) }.join(); assertEquals(0,s.writes)
        assertEquals("unchanged-original",c.recoveryData())
        s.failRead=false; c.retryRecovery().join(); ready(c)
        assertFalse(c.state.value.running); assertEquals(0,s.writes); c.close()
    }
    @Test fun duplicateStartsAreIdempotent() = runBlocking {
        val s=Store(); val c=coordinator(s); ready(c)
        List(20) { c.command { start(false) } }.joinAll()
        assertEquals(1,c.state.value.execution); assertEquals(1,s.writes); c.close()
    }
    @Test fun continuousNotesFlushWithinMaximumDirtyAge() = runBlocking {
        val s=Store(); var clock=1000L; val c=coordinator(s) { clock }; ready(c)
        repeat(25) { clock+=250; c.command { sessionNotes("note $it", "") }.join(); c.flushIfDue() }
        assertTrue(s.writes>=1); assertTrue(s.draft!!.notes.isNotEmpty()); c.close()
    }
    @Test fun failedPauseKeepsUnsavedStateAndCanRetry() = runBlocking {
        val s=Store(); var clock=1000L; val c=coordinator(s) { clock }; ready(c)
        c.command { start(false) }.join(); clock=3000; s.failWrite=true
        c.command { pause() }.join()
        assertFalse(c.state.value.running); assertTrue(c.unsaved.value); assertEquals(2000,c.state.value.elapsed)
        s.failWrite=false; c.retryPersistence().join()
        assertFalse(c.unsaved.value); assertEquals(2000,s.draft!!.elapsed); c.close()
    }
    @Test fun immediateSmartStartWaitsForRecoveryWithoutDeadlock() = runBlocking {
        val s=Store(); val c=coordinator(s)
        val state=withTimeout(3000) { c.startSmart(50,5) }
        assertTrue(state.running); assertEquals(state.token,s.draft!!.token); c.close()
    }
    @Test fun freshSessionRequiresMatchingExportAndLeavesDraftOnMismatch() = runBlocking {
        val s=Store(); s.failRead=true; val c=coordinator(s)
        withTimeout(3000) { c.recoveryFailed.first { it } }
        c.freshAfterExport("different").join()
        assertTrue(c.recoveryFailed.value); assertFalse(c.state.value.ready)
        val original=requireNotNull(c.recoveryData())
        c.freshAfterExport(original).join()
        assertEquals(original,s.archived)
        assertFalse(c.recoveryFailed.value); assertTrue(c.state.value.ready)
        assertFalse(c.state.value.running); c.close()
    }
    @Test fun restoreCannotCheckpointOverUnreadableRecoveryData() = runBlocking {
        val s=Store(); s.failRead=true; val c=coordinator(s)
        withTimeout(3000) { c.recoveryFailed.first { it } }
        try { c.replacing { fail("Must not run replacement") }; fail() } catch (_: IllegalStateException) { }
        assertEquals(0,s.writes); assertEquals("unchanged-original",c.recoveryData()); c.close()
    }
    @Test fun failedArchiveCannotReplaceTheOriginalRecoveryDraft() = runBlocking {
        val s=Store();s.failRead=true;val c=coordinator(s)
        withTimeout(3000) {c.recoveryFailed.first {it}}
        s.failWrite=true;c.freshAfterExport(requireNotNull(c.recoveryData())).join()
        assertTrue(c.recoveryFailed.value);assertFalse(c.state.value.ready)
        assertEquals("unchanged-original",c.recoveryData());assertNull(s.archived);c.close()
    }
}
