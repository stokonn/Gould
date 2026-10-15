package com.opus.piano

import com.opus.piano.data.*
import org.junit.Assert.*
import org.junit.Test
import java.time.ZoneId

class MultiPieceTest {
    @Test fun switchingAndPausesNeverDoubleCount() {
        var parts = listOf(SessionPart(pieceId = 1, title = "A"), SessionPart(pieceId = 2, title = "B"), SessionPart())
        parts = charge(parts, 0, 1500)
        parts = charge(parts, 1, 700)
        // Pause contributes no delta, then resume on B.
        parts = charge(parts, 1, 800)
        parts = charge(parts, 2, 1000)
        assertEquals(listOf(1500L, 1500L, 1000L), parts.map { it.durationMillis })
        assertEquals(4000L, parts.sumOf { it.durationMillis })
    }
    @Test(expected = IllegalArgumentException::class) fun negativeDeltaIsRejected() { charge(listOf(SessionPart()), 0, -1) }
    @Test fun attributionKeepsGlobalSessionCountAndPracticeTotal() {
        val session = PracticeSession(9, null, "A · B", 1, 5000, 4000)
        val parts = listOf(SessionPart(sessionId = 9, pieceId = 1, title = "A", durationMillis = 1500, musicalEra = "Baroque"), SessionPart(sessionId = 9, pieceId = 2, title = "B", durationMillis = 2500, musicalEra = "Baroque"))
        val facts = AchievementEngine.facts(emptyList(), listOf(session), CatalogSnapshot(emptyList(), emptyList()), ZoneId.of("UTC"), parts)
        assertEquals(1L, facts[AchievementMetric.SESSIONS]); assertEquals(4000L, facts[AchievementMetric.PRACTICE_MILLIS]); assertEquals(2L, facts[AchievementMetric.BAROQUE_PRACTICED])
        assertEquals(4000L, attributedSessions(listOf(session), parts).sumOf { it.durationMillis })
    }
    @Test fun showcaseFiltersLockedDuplicatesAndCapsAtFive() {
        val unlocked = (1..8).map { "badge$it" }.toSet()
        assertEquals(listOf("badge3", "badge1", "badge2", "badge4", "badge5"), Showcase.select(listOf("locked", "badge3", "badge3", "badge1", "badge2", "badge4", "badge5", "badge6"), unlocked))
        assertEquals(emptyList<String>(), Showcase.select(listOf("locked"), unlocked))
    }
}
