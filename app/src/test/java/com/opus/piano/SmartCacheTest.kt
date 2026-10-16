package com.opus.piano

import com.opus.piano.data.*
import org.junit.Assert.*
import org.junit.Test

class SmartCacheTest {
    @Test fun statisticPreferencesRecoverMissingAndUnknownMetricsWithoutDuplicates() {
        assertEquals(StatisticLayout.defaults, StatisticLayout.normalize(emptyList()))
        assertEquals(listOf("streak", "time", "pieces", "sessions"), StatisticLayout.normalize(listOf("streak", "unknown", "streak")))
        assertTrue(StatisticLayout.valid("sessions,streak,time,pieces"))
        assertFalse(StatisticLayout.valid("time,time,pieces,sessions"))
        assertEquals("Editorial", ProfileSettings().typography)
    }
    @Test fun sharedSummaryCountsParentSessionsOnceAndPreservesDeletedPieceAttribution() {
        val session = PracticeSession(id = 8, pieceId = null, pieceTitle = "Two works", startedAt = 1, completedAt = 90000, durationMillis = 60000)
        val parts = listOf(SessionPart(sessionId = 8, pieceId = 1, title = "First", durationMillis = 20000), SessionPart(sessionId = 8, pieceId = 2, title = "Second", durationMillis = 40000))
        val summary = summarizeJournal(listOf(session), parts)
        assertEquals(60000L, summary.totalMillis)
        assertEquals(mapOf(1L to 20000L, 2L to 40000L), summary.practiceTotals)
        assertEquals(90000L, summary.lastPractice[2L])
        assertEquals(parts, summary.partsBySession[8L])
        assertEquals(0L, summarizeJournal(emptyList(), emptyList()).totalMillis)
        assertTrue(summarizeJournal(emptyList(), emptyList()).practiceTotals.isEmpty())
        // An edited or restored snapshot must produce a new result, never reuse an old total.
        assertEquals(120000L, summarizeJournal(listOf(session.copy(durationMillis = 120000)), emptyList()).totalMillis)
        assertEquals(60000L, summary.totalMillis)
    }
}
