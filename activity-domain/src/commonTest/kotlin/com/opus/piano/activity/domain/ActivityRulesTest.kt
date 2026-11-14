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

    @Test fun manualActivityWindowContainsOnlyTodayAndTwoPriorDays() {
        assertEquals(listOf(monday, monday + 1, monday + 2), manualActivityDates(monday + 2))
        assertEquals(listOf(20_088L, 20_089L, 20_090L), manualActivityDates(20_090L)) // Year boundary
    }

    @Test fun dailyStreakCombinesSourcesCountsCalendarDaysOnceAndStopsAtMissedDay() {
        val rows = listOf(
            Workout(monday + 4, 60_000, "manual:1", WorkoutSource.MANUAL_ENTRY),
            Workout(monday + 4, 90_000, "practice:2", WorkoutSource.PRACTICE_SESSION),
            Workout(monday + 5, 60_000, "practice:3", WorkoutSource.PRACTICE_SESSION),
        )
        assertEquals(2, currentActivityStreak(rows, monday + 5))
        assertEquals(0, currentActivityStreak(rows, monday + 7))
        assertEquals(2, currentActivityStreak(rows, monday + 6)) // yesterday starts the run when today is empty
    }

    @Test fun dayOffLabelsDoNotTouchFreezeAndOnlyClosedWeeklyGoalFailureConsumesOne() {
        val fivePracticeDays = (0..4).map { Workout(monday + it, 60_000, "practice:$it", WorkoutSource.PRACTICE_SESSION) }
        val goal = WeeklyGoal(monday, 5, null)
        assertEquals(2, classifyWeek(week, goal, fivePracticeDays).count { it.kind == DayKind.DAY_OFF })
        val starting = FreezeState(balance = 2, currentStreak = 3, longestStreak = 3)
        val (success, usedOnSuccess) = closeWeek(starting, success = weekSuccess(week, goal, fivePracticeDays, week.sunday)!!)
        assertFalse(usedOnSuccess)
        assertEquals(2, success.balance)
        val fourPracticeDays = fivePracticeDays.take(4)
        assertFalse(weekSuccess(week, goal, fourPracticeDays, week.sunday)!!)
        val (rescued, consumed) = closeWeek(success, success = false)
        assertTrue(consumed)
        assertEquals(1, rescued.balance)
        assertEquals(success.currentStreak, rescued.currentStreak)
    }

    @Test fun cosmeticWeekRequiresConfiguredGoalsEvenDuringBetaButBetaNeverAdvancesStreak() {
        val sessions = (0..2).map { Workout(monday + it, 10 * 60_000, "s$it", WorkoutSource.MANUAL_ENTRY) }
        assertTrue(perfectWeek(week, WeeklyGoal(monday, 3, null), sessions))
        assertFalse(perfectWeek(week, WeeklyGoal(monday, 4, null), sessions))
        assertFalse(perfectWeek(week, WeeklyGoal(monday, 3, 31), sessions))
        assertTrue(perfectWeek(week, WeeklyGoal(monday, 3, 30), sessions))
        val betaWeek = week.copy(beta = true)
        assertTrue(perfectWeek(betaWeek, WeeklyGoal(monday, 3, null), sessions))
        assertNull(weekSuccess(betaWeek, WeeklyGoal(monday, 3, null), sessions, week.sunday))
        assertFalse(perfectWeek(betaWeek, WeeklyGoal(monday, 3, 31), sessions))
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

    @Test fun fourWeekFreezeMilestoneIsSeparateAndAWeeklyFailureResetsOnlyItsProgress() {
        var state = FreezeState(balance = 0)
        repeat(3) { state = closeWeek(state, success = true).first }
        assertEquals(0, state.balance)
        assertEquals(3, state.successfulWeeksTowardFreeze)

        state = closeWeek(state, success = false).first
        assertEquals(0, state.successfulWeeksTowardFreeze)
        repeat(3) { state = closeWeek(state, success = true).first }
        assertEquals(0, state.balance)
        assertEquals(3, state.successfulWeeksTowardFreeze)

        state = closeWeek(state, success = true).first
        assertEquals(1, state.balance)
        assertEquals(0, state.successfulWeeksTowardFreeze)
    }

    @Test fun orderedEvaluationIsIdempotentAndRejectsDuplicateWeeks() {
        val later = week.copy(monday = monday + 7)
        val earlier = week.copy(monday = monday - 7)
        val input = listOf(later to false, earlier to true)
        assertEquals(evaluateWeeks(FreezeState(), input), evaluateWeeks(FreezeState(), input))
        assertFailsWith<IllegalArgumentException> { evaluateWeeks(FreezeState(), listOf(week to true, week to false)) }
    }
}
