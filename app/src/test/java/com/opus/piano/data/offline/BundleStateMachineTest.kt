package com.opus.piano.data.offline

import com.opus.piano.data.offline.BundleState.FAILED_RETRYABLE
import com.opus.piano.data.offline.BundleState.INSTALLING
import com.opus.piano.data.offline.BundleState.PAUSED
import com.opus.piano.data.offline.BundleState.QUARANTINED
import com.opus.piano.data.offline.BundleState.READY
import com.opus.piano.data.offline.BundleState.STALE
import com.opus.piano.data.offline.BundleState.VALIDATING
import com.opus.piano.data.offline.BundleState.WAITING_FOR_POLICY
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Stage 5 regression tests for the explicit bundle lifecycle.
 * Executed on the JVM host; no emulator required.
 */
class BundleStateMachineTest {

    @Test
    fun requiredPipelineIsFullyAllowed() {
        val pipeline = listOf(
            BundleState.QUEUED, WAITING_FOR_POLICY, BundleState.DOWNLOADING,
            VALIDATING, INSTALLING, READY
        )
        pipeline.zipWithNext { from, to ->
            assertTrue("expected $from -> $to to be legal", BundleStateMachine.canTransition(from, to))
        }
    }

    @Test
    fun partialBundleCanNeverJumpToReady() {
        for (from in listOf(BundleState.QUEUED, WAITING_FOR_POLICY, BundleState.DOWNLOADING, VALIDATING, PAUSED, FAILED_RETRYABLE)) {
            assertFalse("$from -> READY must be illegal", BundleStateMachine.canTransition(from, READY))
        }
    }

    @Test
    fun readyCannotReenterTransferPipeline() {
        assertFalse(BundleStateMachine.canTransition(READY, BundleState.DOWNLOADING))
        assertFalse(BundleStateMachine.canTransition(READY, INSTALLING))
        assertFalse(BundleStateMachine.canTransition(READY, BundleState.QUEUED))
    }

    @Test
    fun quarantinedCannotReachReadyDirectly() {
        assertFalse(BundleStateMachine.canTransition(QUARANTINED, READY))
        assertTrue(BundleStateMachine.canTransition(QUARANTINED, PAUSED))
        assertTrue(BundleStateMachine.canTransition(QUARANTINED, BundleState.QUEUED))
    }

    @Test
    fun requireTransitionThrowsWithActionableMessage() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            BundleStateMachine.requireTransition(BundleState.DOWNLOADING, READY)
        }
        assertTrue(error.message!!.contains("DOWNLOADING"))
        assertTrue(error.message!!.contains("READY"))
    }

    @Test
    fun processRestartNeverResurrectsInFlightWorkAsReady() {
        assertEquals(PAUSED, BundleStateMachine.afterProcessRestart(BundleState.DOWNLOADING))
        assertEquals(PAUSED, BundleStateMachine.afterProcessRestart(VALIDATING))
        assertEquals(PAUSED, BundleStateMachine.afterProcessRestart(INSTALLING))
        assertEquals(READY, BundleStateMachine.afterProcessRestart(READY))
        assertEquals(QUARANTINED, BundleStateMachine.afterProcessRestart(QUARANTINED))
        assertEquals(PAUSED, BundleStateMachine.afterProcessRestart(PAUSED))
    }

    @Test
    fun staleContentRemainsRetryableButIsNotReady() {
        assertFalse("STALE is not 'ready', freshness is separate", BundleStateMachine.holdsPublishedContent(STALE))
        assertTrue(BundleStateMachine.holdsPublishedContent(READY))
        assertTrue(BundleStateMachine.canTransition(STALE, BundleState.DOWNLOADING))
        assertTrue(BundleStateMachine.canTransition(STALE, QUARANTINED))
    }

    @Test
    fun transferStepRequiresExplicitPolicyStates() {
        assertTrue(BundleStateMachine.permits(BundleState.QUEUED, BundleStateMachine.Step.TRANSFER))
        assertTrue(BundleStateMachine.permits(WAITING_FOR_POLICY, BundleStateMachine.Step.TRANSFER))
        assertTrue(BundleStateMachine.permits(BundleState.DOWNLOADING, BundleStateMachine.Step.TRANSFER))
        for (state in listOf(READY, VALIDATING, INSTALLING, PAUSED, QUARANTINED, STALE, FAILED_RETRYABLE)) {
            assertFalse("$state must not transfer", BundleStateMachine.permits(state, BundleStateMachine.Step.TRANSFER))
        }
    }

    @Test
    fun validateAndPublishStepsArePhaseLocked() {
        assertTrue(BundleStateMachine.permits(VALIDATING, BundleStateMachine.Step.VALIDATE))
        assertFalse(BundleStateMachine.permits(BundleState.DOWNLOADING, BundleStateMachine.Step.VALIDATE))
        assertTrue(BundleStateMachine.permits(INSTALLING, BundleStateMachine.Step.PUBLISH))
        assertFalse(BundleStateMachine.permits(VALIDATING, BundleStateMachine.Step.PUBLISH))
        assertFalse(BundleStateMachine.permits(READY, BundleStateMachine.Step.PUBLISH))
    }

    @Test
    fun everyStateHasAtLeastOneOutgoingTransition() {
        for (state in BundleState.entries) {
            assertTrue("$state is a dead end", BundleStateMachine.allowed(state).isNotEmpty())
        }
    }

    @Test
    fun recoveryPathsAreLegalAndNeverPassThroughReady() {
        for (from in BundleState.entries) {
            for (to in BundleState.entries) {
                val path = BundleStateMachine.path(from, to)
                if (from == to) {
                    assertEquals("$from -> $to", emptyList<BundleState>(), path)
                    continue
                }
                if (path.isEmpty()) continue
                // Every consecutive step must be a legal transition of the reviewed machine.
                (listOf(from) + path).zipWithNext { step, next ->
                    assertTrue("illegal step $step -> $next in $from->$to path", BundleStateMachine.canTransition(step, next))
                }
                assertEquals("path must end at target", to, path.last())
                // Recovery/cleanup paths may never travel *through* the published state.
                if (to != READY) {
                    assertTrue("READY used as intermediate in $from->$to", path.dropLast(1).none { it == READY })
                }
            }
        }
    }

    @Test
    fun interruptedInFlightStatesRecoverToPausedNotReady() {
        for (state in listOf(BundleState.DOWNLOADING, VALIDATING, INSTALLING)) {
            assertEquals("process restart of $state", PAUSED, BundleStateMachine.afterProcessRestart(state))
        }
        assertEquals(READY, BundleStateMachine.afterProcessRestart(READY))
        assertEquals(QUARANTINED, BundleStateMachine.afterProcessRestart(QUARANTINED))
    }

    @Test
    fun quarantinedManifestCanAlwaysReachAQueuedRetryLegally() {
        val path = BundleStateMachine.path(QUARANTINED, BundleState.QUEUED)
        assertTrue(path.isNotEmpty())
        assertEquals(BundleState.QUEUED, path.last())
    }
}
