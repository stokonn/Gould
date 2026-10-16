package com.opus.piano

import com.opus.piano.data.*
import com.opus.piano.ui.selectedTab
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class ProfileMaintenanceTest {
    private val today = LocalDate.of(2026, 10, 16)
    private val zone = ZoneId.of("Europe/Prague")
    private fun session(day: LocalDate, time: Long = 60000) = PracticeSession(pieceId = null, pieceTitle = "Practice", startedAt = 1, completedAt = day.atStartOfDay(zone).toInstant().toEpochMilli(), durationMillis = time, practiceDay = day.toString())
    @Test fun periodBoundariesUsePersistedLocalDates() {
        val sessions = (0..400).map { session(today.minusDays(it.toLong())) } + session(today.plusDays(1))
        assertEquals(7, profilePeriod(sessions, "Week", today, zone).size)
        assertEquals(30, profilePeriod(sessions, "Month", today, zone).size)
        assertEquals(365, profilePeriod(sessions, "Year", today, zone).size)
        assertEquals(sessions, profilePeriod(sessions, "All time", today, zone))
        val all = sessions + session(today.plusMonths(2))
        assertEquals(all.sumOf { it.durationMillis }, activityBins(all, "All time", today, zone).sumOf { it.second })
    }
    @Test fun currentStreakAllowsYesterdayButDoesNotInventDays() {
        assertEquals(0, currentPracticeStreak(emptyList(), today, zone))
        val sessions = listOf(session(today.minusDays(1)), session(today.minusDays(2)), session(today.minusDays(2)), session(today.minusDays(4)))
        assertEquals(2, currentPracticeStreak(sessions, today, zone))
        assertEquals(3, currentPracticeStreak(sessions + session(today), today, zone))
        assertEquals(0, currentPracticeStreak(listOf(session(today, 999)), today, zone))
    }
    @Test fun formattingKeepsLongPracticeTotalsAndShortSessionsReadable() {
        assertEquals("0m", compactPracticeTime(0)); assertEquals("42s", compactPracticeTime(42000))
        assertEquals("59m", compactPracticeTime(59 * 60000)); assertEquals("125h 7m", compactPracticeTime((125 * 60 + 7) * 60000L))
    }
    @Test fun settingsAndSearchSelectTheirOwningTabs() {
        assertEquals("HOME", selectedTab("HOME")); assertEquals("HOME", selectedTab("settings"))
        listOf("catalog", "catalog/work/87", "catalog/composer/87", "piece/1", "recordings/piece:1").forEach { assertEquals("REPERTOIRE", selectedTab(it)) }
        assertEquals("HOME", selectedTab("badge/first_notes")); assertEquals("PRACTICE", selectedTab("PRACTICE"))
    }
    @Test fun showcaseOrderSurvivesReorderingRemovalAndLimitChecks() {
        val unlocked = (1..7).map { "badge$it" }.toSet()
        assertEquals(listOf("badge5", "badge2", "badge1", "badge4", "badge3"), Showcase.select(listOf("badge5", "badge2", "badge1", "badge4", "badge3", "badge6"), unlocked))
        assertEquals(listOf("badge5", "badge1"), Showcase.select(listOf("badge5", "locked", "badge1", "badge5"), unlocked))
        val profile = ProfileSettings(username = "Existing musician", avatar = "retained", theme = "Dark", showcase = "badge5,badge1")
        assertEquals(listOf("badge5", "badge1"), profile.badgeIds)
    }
}
