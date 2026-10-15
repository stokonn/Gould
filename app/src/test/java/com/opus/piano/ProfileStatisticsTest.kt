package com.opus.piano

import com.opus.piano.data.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class ProfileStatisticsTest {
    private val today = LocalDate.of(2026, 10, 15)
    private val zone = ZoneId.of("UTC")
    private fun session(day: LocalDate) = PracticeSession(pieceId = null, pieceTitle = "Free practice", startedAt = 1, completedAt = day.atStartOfDay(zone).toInstant().toEpochMilli(), durationMillis = 60000, practiceDay = day.toString())
    @Test fun trailingYearIncludesBothPartialOctoberMonths() {
        val sessions = (0..364).map { session(today.minusDays(it.toLong())) }
        val bins = activityBins(sessions, "Year", today, zone)
        assertEquals(13, bins.size)
        assertEquals(365 * 60000L, bins.sumOf { it.second })
        assertEquals("2025-10", bins.first().first)
        assertEquals("2026-10", bins.last().first)
    }
    @Test fun periodsKeepEveryMinuteAndAllTimeStaysBounded() {
        for ((period, days) in listOf("Week" to 7, "Month" to 30, "All time" to 8000)) {
            val sessions = (0 until days).map { session(today.minusDays(it.toLong())) }
            val bins = activityBins(sessions, period, today, zone)
            assertEquals(days * 60000L, bins.sumOf { it.second })
            if (period == "All time") assertTrue(bins.size <= 12)
        }
    }
}
