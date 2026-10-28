package com.opus.piano

import com.opus.piano.data.*
import com.opus.piano.practice.PracticeState
import com.opus.piano.widgets.*
import org.junit.Assert.*
import org.junit.Test
import java.time.*

class WidgetProjectionTest {
    private val prague = ZoneId.of("Europe/Prague")
    private val today = LocalDate.of(2026, 10, 29)
    private fun row(day: String, duration: Long = 60_000, elapsed: Long = 999_999): PracticeSession {
        val instant = LocalDate.parse(day).atTime(12, 0).atZone(prague).toInstant().toEpochMilli()
        return PracticeSession(pieceId = null, pieceTitle = "Actual journal", startedAt = instant - duration,
            completedAt = instant, durationMillis = duration, practiceDay = day, elapsedMillis = elapsed)
    }
    @Test fun calendarWeekIsMondayThroughSundayAndNeverTrailingSevenDays() {
        val rows = listOf(row("2026-10-25"), row("2026-10-26", 10_000), row("2026-11-01", 20_000), row("2026-11-02"))
        val week = widgetWeek(rows, today, prague)
        assertEquals(LocalDate.parse("2026-10-26"), week.monday)
        assertEquals(listOf(10_000L, 0L, 0L, 0L, 0L, 0L, 20_000L), week.credited)
        assertEquals(30_000L, week.total); assertEquals(2, week.sessions); assertEquals(2, week.practicedDays)
    }
    @Test fun totalsUseCreditedSessionRowsRatherThanElapsedOrRecordingTime() {
        val first = row("2026-10-29", 12_000, Long.MAX_VALUE)
        val second = row("2026-10-29", 23_000, 999_999_999)
        assertEquals(35_000L, widgetWeek(listOf(first, second), today, prague).total)
    }
    @Test fun yearBoundaryKeepsOneCalendarWeekAcrossBothYears() {
        val week = widgetWeek(listOf(row("2026-12-28"), row("2027-01-01"), row("2027-01-03")), LocalDate.parse("2027-01-01"), prague)
        assertEquals(LocalDate.parse("2026-12-28"), week.monday); assertEquals(180_000L, week.total)
    }
    @Test fun daylightSavingWeekStillHasSevenLocalDates() {
        val day = LocalDate.parse("2026-03-29")
        val week = widgetWeek((0..6).map { row("2026-03-${23 + it}") }, day, prague)
        assertEquals(7, week.credited.size); assertEquals(7, week.practicedDays)
        assertEquals(420_000L, week.total)
        val hours = Duration.between(week.monday.atStartOfDay(prague), week.monday.plusDays(7).atStartOfDay(prague)).toHours()
        assertEquals(167L, hours)
    }
    @Test fun storedPracticeDayIsNotReassignedWhenDeviceZoneChanges() {
        val stored = row("2026-10-26").copy(completedAt = Instant.parse("2026-10-25T23:30:00Z").toEpochMilli())
        val week = widgetWeek(listOf(stored), today, ZoneId.of("America/Los_Angeles"))
        assertEquals(60_000L, week.credited.first())
    }
    @Test fun legacyRowsUseExistingRecordedDayFallbackInCurrentZone() {
        val legacy = row("2026-10-26").copy(practiceDay = null, completedAt = Instant.parse("2026-10-26T00:30:00Z").toEpochMilli())
        assertEquals(60_000L, widgetWeek(listOf(legacy), today, prague).total)
        assertEquals(0L, widgetWeek(listOf(legacy), today, ZoneId.of("America/Los_Angeles")).total)
    }
    @Test fun genuineEmptyWeekHasVerifiedZerosWithoutInventingSessions() {
        val week = widgetWeek(emptyList(), today, prague)
        assertEquals(List(7) { 0L }, week.credited); assertEquals(0, week.sessions)
    }
    @Test(expected = ArithmeticException::class) fun overflowFailsInsteadOfPublishingWrappedTotals() {
        widgetWeek(listOf(row("2026-10-29", Long.MAX_VALUE), row("2026-10-29", 1)), today, prague)
    }
    @Test(expected = IllegalArgumentException::class) fun corruptNegativeCreditCannotBecomeFreshZero() {
        widgetWeek(listOf(row("2026-10-29", -1)), today, prague)
    }
    @Test fun intelligentContextUsesLiveStateOverObsoleteSnapshotWithoutChangingIt() {
        val live = PracticeState(ready = true, running = true, token = "live",
            parts = listOf(SessionPart(pieceId = 7, title = "Dvořák original title")))
        val obsolete = live.copy(running = false, status = "Review", token = "old")
        assertEquals(WidgetPractice("active", "Dvořák original title", false), widgetPractice(live, obsolete, false))
        assertTrue(live.running); assertEquals("live", live.token)
    }
    @Test fun unreadableRecoveryAndDurableDraftNeverPretendReadyOrAutoResume() {
        assertEquals("recovery", widgetPractice(PracticeState(ready = false), null, true).mode)
        val durable = PracticeState(token = "unfinished", status = "Interrupted", ready = false)
        assertEquals("draft", widgetPractice(null, durable, false).mode)
        assertEquals("review", widgetPractice(null, durable.copy(status = "Review"), false).mode)
        assertEquals("ready", widgetPractice(null, null, false).mode)
        assertFalse(durable.running)
    }
    @Test fun staleChecksCoverAgeFutureClockWeekRolloverAndZoneChange() {
        val now = 1_800_000_000_000L; val monday = widgetMonday(today)
        assertFalse(widgetSnapshotStale(now - 1000, now, monday, today, prague.id, prague))
        assertTrue(widgetSnapshotStale(now - 6 * 3_600_000 - 1, now, monday, today, prague.id, prague))
        assertTrue(widgetSnapshotStale(now + 1, now, monday, today, prague.id, prague))
        assertTrue(widgetSnapshotStale(now, now, monday, today.plusWeeks(1), prague.id, prague))
        assertTrue(widgetSnapshotStale(now, now, monday, today, "UTC", prague))
    }
    @Test fun rolloverUsesLocalCalendarMidnightAcrossBothDstTransitions() {
        val spring = LocalDate.parse("2026-03-29").atStartOfDay(prague)
        val autumn = LocalDate.parse("2026-10-25").atStartOfDay(prague)
        assertEquals(23L, Duration.between(spring, widgetNextMidnight(spring)).toHours())
        assertEquals(25L, Duration.between(autumn, widgetNextMidnight(autumn)).toHours())
    }
}
