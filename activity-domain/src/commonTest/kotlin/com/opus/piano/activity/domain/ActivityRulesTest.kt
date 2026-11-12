package com.opus.piano.activity.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ActivityRulesTest {
    private val monday = 20_052L // 2024-11-25, a Monday
    private val week = Week(monday, "Europe/Prague")

    @Test fun weekDatesAndGoalsUseMondayAndNextMondayActivation() {
        assertEquals(monday, weekFor(monday + 6, "Europe/Prague").monday)
        assertEquals(monday + 7, nextGoalMonday(monday + 2))
        val goals = listOf(WeeklyGoal(monday, 3, null), WeeklyGoal(monday + 7, 5, 60))
        assertEquals(3, activeGoal(goals, monday + 6).sessions)
        assertEquals(5, activeGoal(goals, monday + 7).sessions)
    }

    @Test fun mondayIdentityDoesNotFollowIsoWeekYearLabels() {
        val dec29 = 20_086L // 2024-12-29, Sunday
        assertEquals(dec29 - 6, weekFor(dec29, "UTC").monday)
        assertEquals(dec29 + 1, weekFor(dec29 + 1, "UTC").monday) // 2024-12-30 is a new Monday week
    }

    @Test fun betaWeekAndPartialCurrentWeekAreNotEvaluated() {
        assertTrue(weekFor(monday + 2, "Europe/Prague", monday + 2).beta)
        assertFalse(weekFor(monday, "Europe/Prague", monday).beta)
        val goal = WeeklyGoal(monday, 3, null)
        assertNull(weekSuccess(week, goal, emptyList(), monday + 5))
        assertNull(weekSuccess(week.copy(beta = true), goal, emptyList(), monday + 6))
    }

    @Test fun dayOffAndMissClassificationIsChronologicalAndInsertionIndependent() {
        val goal = WeeklyGoal(monday, 5, 60)
        val rows = listOf(Workout(monday + 4, 600_000, "late", WorkoutSource.MANUAL_ENTRY), Workout(monday + 6, 600_000, "later", WorkoutSource.PRACTICE_SESSION))
        val expected = listOf(DayKind.DAY_OFF, DayKind.DAY_OFF, DayKind.MISS, DayKind.MISS, DayKind.TRAINING, DayKind.MISS, DayKind.TRAINING)
        assertEquals(expected, classifyWeek(week, goal, rows).map { it.kind })
        assertEquals(classifyWeek(week, goal, rows), classifyWeek(week, goal, rows.reversed()))
    }

    @Test fun sessionSuccessIsIndependentFromDailyMissAndExtraWorkouts() {
        val goal = WeeklyGoal(monday, 5, 60)
        val rows = (0..5).map { Workout(monday, 10 * 60_000, "w$it", WorkoutSource.MANUAL_ENTRY) }
        assertEquals(6, aggregate(rows, week).sessions)
        assertTrue(weekSuccess(week, goal, rows, week.sunday)!!)
        assertEquals(DayKind.MISS, classifyWeek(week, goal, rows).last().kind)
    }

    @Test fun freezeCapEarningPauseConsumptionAndBreakAreDeterministic() {
        var state = FreezeState()
        repeat(4) { state = closeWeek(state, true).first }
        assertEquals(2, state.balance)
        repeat(4) { state = closeWeek(state, true).first }
        assertEquals(3, state.balance)
        repeat(8) { state = closeWeek(state, true).first }
        assertEquals(0, state.successfulWeeksTowardFreeze)
        state = state.copy(successfulWeeksTowardFreeze = 3, currentStreak = 5, longestStreak = 5)
        val (frozen, consumed) = closeWeek(state, false)
        assertTrue(consumed)
        assertEquals(0, frozen.successfulWeeksTowardFreeze)
        assertEquals(5, frozen.currentStreak)
        val broken = closeWeek(frozen.copy(balance = 0), false).first
        assertEquals(0, broken.currentStreak)
        assertEquals(5, broken.longestStreak)
    }

    @Test fun orderedEvaluationIsIdempotentAndRejectsDuplicateWeeks() {
        val later = week.copy(monday = monday + 7)
        val earlier = week.copy(monday = monday - 7)
        val input = listOf(later to false, earlier to true)
        assertEquals(evaluateWeeks(FreezeState(), input), evaluateWeeks(FreezeState(), input))
        assertFailsWith<IllegalArgumentException> { evaluateWeeks(FreezeState(), listOf(week to true, week to false)) }
    }
}
