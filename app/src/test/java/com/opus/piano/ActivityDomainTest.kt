package com.opus.piano

import com.opus.piano.activity.domain.*
import com.opus.piano.data.AchievementEngine
import com.opus.piano.data.PracticeSession
import com.opus.piano.data.currentPracticeStreak
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class ActivityDomainTest {
    private fun d(value: String) = LocalDate.parse(value).toEpochDay()
    private val week = Week(d("2026-11-09"), "Europe/Prague")
    private val goal = WeeklyGoal(week.monday, 5, 60)
    private fun workout(day: Int, id: String = "w$day") = Workout(week.monday + day, 10 * 60_000L, id, WorkoutSource.MANUAL_ENTRY)

    @Test fun goalChangesAreStrictlyNextMonday() {
        assertEquals(d("2026-11-16"), nextGoalMonday(d("2026-11-09")))
        assertEquals(d("2026-11-16"), nextGoalMonday(d("2026-11-12")))
        val history = listOf(goal, WeeklyGoal(d("2026-11-16"), 2, null))
        assertEquals(week.monday, activeGoal(history, week.sunday).effectiveMonday)
        assertEquals(d("2026-11-16"), activeGoal(history, week.monday + 7).effectiveMonday)
    }

    @Test fun weekAndBetaUseMondayAndStableZone() {
        assertEquals(week, weekFor(d("2026-11-15"), "Europe/Prague"))
        assertTrue(weekFor(d("2026-11-12"), "Europe/Prague", d("2026-11-12")).beta)
        assertFalse(weekFor(week.monday, "Europe/Prague", week.monday).beta)
        assertEquals("Europe/Prague", weekFor(week.monday, "Europe/Prague").zoneId)
    }

    @Test fun localCalendarWeekSpansDstWithoutChangingItsSevenDates() {
        val zone = java.time.ZoneId.of("Europe/Prague")
        val monday = LocalDate.parse("2026-10-19")
        val week = Week(monday.toEpochDay(), zone.id)
        assertEquals(7, week.dates().size)
        assertEquals(169L, java.time.Duration.between(monday.atStartOfDay(zone), monday.plusDays(7).atStartOfDay(zone)).toHours())
    }

    @Test fun dayClassificationIsChronologicalAndIndependentOfInsertionOrder() {
        val rows = listOf(workout(4), workout(6))
        val forward = classifyWeek(week, goal, rows)
        assertEquals(listOf(DayKind.DAY_OFF, DayKind.DAY_OFF, DayKind.MISS, DayKind.MISS, DayKind.TRAINING, DayKind.MISS, DayKind.TRAINING), forward.map { it.kind })
        assertEquals(forward, classifyWeek(week, goal, rows.reversed()))
    }

    @Test fun futureDatesAreHiddenAndDayDetailsAggregateManualAndPracticeInputs() {
        val rows = listOf(workout(0, "practice"), workout(0, "manual"))
        val projected = classifyWeek(week, goal, rows, through = week.monday + 1)
        assertEquals(2, projected.size)
        assertEquals(2, projected.first().workouts)
        assertEquals(20L, projected.first().minutes)
    }

    @Test fun weeklySuccessIsIndependentFromMissDaysAndExtraWorkoutsDoNotRaiseTarget() {
        val six = (0..5).map { workout(it) }
        assertTrue(weekSuccess(week, goal, six, week.sunday)!!)
        assertEquals(6, aggregate(six, week).sessions)
        assertEquals(60L, aggregate(six, week).minutes)
        assertNull(weekSuccess(week, goal, six, week.monday + 5))
        assertNull(weekSuccess(week.copy(beta = true), goal, six, week.sunday))
    }

    @Test fun freezeStartsAtOneEarnsEveryFourCapsAndPausesAtCapacity() {
        var state = FreezeState()
        repeat(4) { state = closeWeek(state, true).first }
        assertEquals(2, state.balance)
        assertEquals(0, state.successfulWeeksTowardFreeze)
        state = state.copy(balance = 3)
        repeat(8) { state = closeWeek(state, true).first }
        assertEquals(0, state.successfulWeeksTowardFreeze)
        assertEquals(3, state.balance)
    }

    @Test fun failedWeekConsumesFreezeOnceResetsCounterAndPreservesStreak() {
        val before = FreezeState(balance = 2, successfulWeeksTowardFreeze = 3, currentStreak = 6, longestStreak = 6)
        val (after, consumed) = closeWeek(before, success = false)
        assertTrue(consumed)
        assertEquals(1, after.balance)
        assertEquals(0, after.successfulWeeksTowardFreeze)
        assertEquals(6, after.currentStreak)
        assertEquals(0, closeWeek(after, success = false).first.balance)
        val (broken, noFreeze) = closeWeek(after.copy(balance = 0), success = false)
        assertFalse(noFreeze)
        assertEquals(0, broken.currentStreak)
        assertEquals(6, broken.longestStreak)
    }

    @Test fun betaWeeksAreNeutralAndDuplicateWeekEvaluationsAreRejected() {
        val state = FreezeState()
        assertEquals(state, closeWeek(state, success = false, beta = true).first)
        val result = evaluateWeeks(state, listOf(week to null, week.plusOne() to true))
        assertNull(result.first().success)
        assertEquals(1, result.last().streak.currentStreak)
        try {
            evaluateWeeks(state, listOf(week to true, week to true))
            fail("duplicate weeks must be rejected")
        } catch (_: IllegalArgumentException) { }
    }

    @Test fun legacySessionDatesMinutesAndDailyStreakRemainCompatible() {
        val zone = ZoneId.of("Europe/Prague")
        val sessions = listOf(
            session("2026-11-09", 25), session("2026-11-10", 35), session("2026-11-10", 10),
            session("2026-11-13", 20)
        )
        val existingDays = sessions.map { AchievementEngine.recordedDay(it, zone) }.toSet()
        assertEquals(2, currentPracticeStreak(sessions, LocalDate.parse("2026-11-10"), zone))
        val originalDailyMinutes = sessions.groupBy { AchievementEngine.recordedDay(it, zone) }
            .mapValues { (_, rows) -> rows.sumOf { it.durationMillis } / 60_000L }
        val mapped = sessions.mapIndexed { index, row ->
            Workout(AchievementEngine.recordedDay(row, zone).toEpochDay(), row.durationMillis, "session-$index", WorkoutSource.PRACTICE_SESSION)
        }
        val totals = aggregate(mapped, week)
        assertEquals(sessions.size, totals.sessions)
        assertEquals(sessions.sumOf { it.durationMillis } / 60_000L, totals.minutes)
        val activityDailyMinutes = mapped.groupBy { LocalDate.ofEpochDay(it.epochDay) }
            .mapValues { (_, rows) -> rows.sumOf { it.durationMillis } / 60_000L }
        assertEquals(originalDailyMinutes, activityDailyMinutes)
        assertEquals(setOf(d("2026-11-09"), d("2026-11-10"), d("2026-11-13")), existingDays.map { it.toEpochDay() }.toSet())
    }

    private fun session(day: String, minutes: Long) = PracticeSession(
        pieceId = null, pieceTitle = "Free practice", startedAt = d(day) * 86_400_000L,
        completedAt = d(day) * 86_400_000L + minutes * 60_000L, durationMillis = minutes * 60_000L,
        practiceDay = day, practiceZone = "Europe/Prague"
    )

    private fun Week.plusOne() = copy(monday = monday + 7)
}
