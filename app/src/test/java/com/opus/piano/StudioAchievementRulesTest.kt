package com.opus.piano

import com.opus.piano.data.*
import org.junit.Assert.*
import org.junit.Test

class StudioAchievementRulesTest {
    private fun session(id: Long = 1, at: Long = 1000, credit: Long = 1000) = PracticeSession(id, null, "Historical Goldberg", 1, at, credit)
    @Test fun duplicateImportedIdsAndRepeatedSaveEventsDoNotIncreaseRecordingCount() {
        val sha = "a".repeat(64)
        val event = AchievementEvent(StudioAchievementRules.recordingKey(sha), "recording_saved", sha, 1000)
        assertEquals(1L, StudioAchievementRules.distinctRecordings(listOf(event, event.copy(occurredAt = 2000), event.copy(eventKey = "new-imported-id"))))
        assertEquals(2L, StudioAchievementRules.distinctRecordings(listOf(event, event.copy(eventKey = StudioAchievementRules.recordingKey("b".repeat(64)), subject = "b".repeat(64)))))
    }
    @Test fun failedEmptyAndClickEventsNeverCountAsRecordings() {
        val sha = "a".repeat(64)
        val base = AchievementEvent(StudioAchievementRules.recordingKey(sha), "recording_saved", sha, 1000)
        assertEquals(0L, StudioAchievementRules.distinctRecordings(listOf(base.copy(value = 0), base.copy(occurredAt = 0), base.copy(kind = "record_clicked"), base.copy(subject = "file://unverified"))))
    }
    @Test fun foundingRecognizesHistoricalGoldbergAndDelayedImportsButNotCutoffOrNewUsers() {
        assertTrue(StudioAchievementRules.foundingPractice(listOf(session(at = 999)), 3000, 1000))
        assertFalse(StudioAchievementRules.foundingPractice(listOf(session(at = 1000)), 3000, 1000))
        assertFalse(StudioAchievementRules.foundingPractice(listOf(session(at = 1001)), 3000, 1000))
        assertTrue(StudioAchievementRules.foundingPractice(listOf(session(at = 1000)), 2000, null))
        assertFalse(StudioAchievementRules.foundingPractice(listOf(session(at = 3000)), 2000, null))
        assertFalse(StudioAchievementRules.foundingPractice(listOf(session(credit = 0)), 2000, null))
    }
    @Test fun versatileRequiresThreeDistinctCreditedPiecesInOneValidSession() {
        val parts = (1L..3L).map { SessionPart(sessionId = 1, pieceId = it, durationMillis = 1000) }
        assertTrue(StudioAchievementRules.versatile(listOf(session(credit = 3000)), parts))
        assertFalse(StudioAchievementRules.versatile(listOf(session()), parts.map { it.copy(pieceId = 1) }))
        assertFalse(StudioAchievementRules.versatile(listOf(session()), parts.map { it.copy(durationMillis = 0) }))
        assertFalse(StudioAchievementRules.versatile(listOf(session()), parts.map { it.copy(sessionId = it.pieceId!!) }))
    }
    @Test fun playbackRequiresRealPositiveProgressAndCapsAtFullShortRecording() {
        assertEquals(10_000L, StudioAchievementRules.playbackThreshold(30_000))
        assertEquals(1500L, StudioAchievementRules.playbackThreshold(1500))
        assertThrows(IllegalArgumentException::class.java) { StudioAchievementRules.playbackThreshold(0) }
    }
}
