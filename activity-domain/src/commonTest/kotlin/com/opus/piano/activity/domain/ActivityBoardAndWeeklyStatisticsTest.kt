package com.opus.piano.activity.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ActivityBoardAndWeeklyStatisticsTest {
    @Test fun boardStartsOnMondayAndCoversFullYear() {
        val board = activityBoard(2024, 20_000L, emptyList())
        assertEquals(53, board.size) // 2024 has 366 days -> 53 weeks
        assertEquals(1, dayOfWeek(board.first().monday).toInt())
        val firstCell = board.first().cells.first()
        assertTrue(firstCell.epochDay <= epochDayOf(2024, 1, 1))
    }

    @Test fun boardLeapYearIncludesFebruary29() {
        val board = activityBoard(2024, 20_000L, emptyList())
        val leapDay = epochDayOf(2024, 2, 29)
        assertTrue(board.flatMap { it.cells }.any { it.epochDay == leapDay && it.inYear })
    }

    @Test fun boardMonthLabelsAlignToFirstWeekContainingMonthStart() {
        val board = activityBoard(2025, 20_000L, emptyList())
        val januaryWeek = board.first { week -> week.cells.any { it.inYear && civilFromEpochDay(it.epochDay).month == 1 && civilFromEpochDay(it.epochDay).day == 1 } }
        assertEquals(1, januaryWeek.monthLabel)
    }

    @Test fun boardDecemberJanuaryTransitionKeepsWeekdayAlignment() {
        val board = activityBoard(2024, 20_000L, emptyList())
        val decWeek = board.last { week -> week.cells.any { it.inYear && civilFromEpochDay(it.epochDay).month == 12 } }
        val janBoundaryDay = epochDayOf(2025, 1, 1)
        val janWeek = board.first { week -> week.cells.any { it.epochDay == janBoundaryDay } }
        // Monday alignment preserved across the year boundary.
        assertEquals(1, dayOfWeek(decWeek.monday).toInt())
        assertEquals(1, dayOfWeek(janWeek.monday).toInt())
        assertTrue(janWeek.cells.any { it.epochDay == janBoundaryDay && !it.inYear })
    }

    @Test fun boardDayLabelsMatchCalendarDates() {
        val board = activityBoard(2025, 20_000L, emptyList())
        val cell = board.flatMap { it.cells }.first { it.inYear && civilFromEpochDay(it.epochDay).month == 3 && civilFromEpochDay(it.epochDay).day == 15 }
        assertEquals(15, civilFromEpochDay(cell.epochDay).day)
    }

    @Test fun weeklyStatisticsCurrentAndPreviousWeeks() {
        val monday = 20_000L // arbitrary Monday-ish not required; use mondayOf
        val today = mondayOf(20_000L) + 3 // Wednesday
        val currentDay = today
        val previousDay = today - 7
        val workouts = listOf(
            Workout(currentDay, 30 * 60_000L, "a", WorkoutSource.PRACTICE_SESSION),
            Workout(currentDay - 1, 15 * 60_000L, "b", WorkoutSource.MANUAL_ENTRY),
            Workout(previousDay, 45 * 60_000L, "c", WorkoutSource.PRACTICE_SESSION),
        )
        val stats = weeklyStatistics(today, workouts)
        assertEquals(2, stats.sessions)
        assertEquals(45, stats.minutes)
        assertEquals(2, stats.activeDays)
        assertEquals(1, stats.previousSessions)
        assertEquals(45, stats.previousMinutes)
        assertEquals(1, stats.previousActiveDays)
    }

    @Test fun weeklyStatisticsEmptyPeriods() {
        val today = mondayOf(100L) + 2
        val stats = weeklyStatistics(today, emptyList())
        assertEquals(0, stats.sessions)
        assertEquals(0, stats.minutes)
        assertEquals(0, stats.activeDays)
        assertEquals(0, stats.previousSessions)
        assertEquals(0, stats.previousMinutes)
        assertEquals(0, stats.previousActiveDays)
    }

    @Test fun weeklyStatisticsWeekBoundary() {
        val monday = mondayOf(500L)
        // Sunday belongs to previous week, Monday to current.
        val sunday = monday - 1
        val workouts = listOf(
            Workout(sunday, 10 * 60_000L, "sun", WorkoutSource.PRACTICE_SESSION),
            Workout(monday, 20 * 60_000L, "mon", WorkoutSource.PRACTICE_SESSION),
        )
        val stats = weeklyStatistics(monday, workouts)
        assertEquals(1, stats.sessions)
        assertEquals(20, stats.minutes)
        assertEquals(1, stats.previousSessions)
        assertEquals(10, stats.previousMinutes)
    }
}
