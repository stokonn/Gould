package com.opus.piano.activity.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ActivityRulesTest {
    @Test fun sharedOverviewUsesCurrentWeekAndAllConfiguredGoalDimensions() {
        val today = 20_003L // Monday
        val goal = WeeklyGoal(today, sessions = 2, minutes = 30)
        val workouts = listOf(
            Workout(today, 15 * 60_000L, "a", WorkoutSource.PRACTICE_SESSION),
            Workout(today + 1, 15 * 60_000L, "b", WorkoutSource.MANUAL_ENTRY),
            Workout(today - 1, 90 * 60_000L, "outside", WorkoutSource.PRACTICE_SESSION),
        )

        val overview = activityOverview(today + 1, "UTC", listOf(goal), workouts)

        assertEquals(goal, overview.goal)
        assertEquals(WeekTotals(2, 30), overview.totals)
        assertEquals(1, overview.days.first { it.epochDay == today }.workouts)
        assertTrue(overview.goalMet)
    }

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

    @Test fun weeklySuccessRequiresSessionsAndMinutesTogether() {
        val goal = WeeklyGoal(monday, sessions = 2, minutes = 30)
        val mixed = listOf(
            Workout(monday, 10 * 60_000L, "one", WorkoutSource.PRACTICE_SESSION),
            Workout(monday + 1, 10 * 60_000L, "two", WorkoutSource.MANUAL_ENTRY),
        )
        assertEquals(2, aggregate(mixed, week).sessions)
        assertEquals(20, aggregate(mixed, week).minutes)
        assertFalse(goalMet(aggregate(mixed, week), goal))
        assertFalse(weekSuccess(week, goal, mixed, week.sunday)!!)
        assertTrue(goalMet(aggregate(mixed + Workout(monday + 2, 10 * 60_000L, "three", WorkoutSource.PRACTICE_SESSION), week), goal))
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

    // --- Issue #55: the weekly view must roll over exactly at the Monday boundary. ---
    // 2026-11-16 Mon .. 2026-11-22 Sun .. 2026-11-23 Mon (epoch days 20773/20779/20780),
    // the Europe/Prague week that Issue #55's screenshot shows as completed.
    private val issueSunday = 20_779L
    private val issueMonday = 20_780L
    private val issueWeekStart = 20_773L
    private val prague = "Europe/Prague"

    @Test fun weekAdvancesAtMondayBoundaryAndResetsCurrentTotals() {
        val goal = WeeklyGoal(issueWeekStart, sessions = 5, minutes = 90)
        val completed = (issueWeekStart..issueSunday).map {
            Workout(it, 30 * 60_000L, "practice:$it", WorkoutSource.PRACTICE_SESSION)
        }

        // Sunday: the open week still reports the whole completed week.
        val sunday = activityOverview(issueSunday, prague, listOf(goal), completed)
        assertEquals(issueWeekStart, sunday.week.monday)
        assertEquals(WeekTotals(7, 210), sunday.totals)
        assertTrue(sunday.goalMet)
        assertEquals((issueWeekStart..issueSunday).toList(), sunday.days.map { it.epochDay })

        // Monday: the very next day must show the NEW week, not the completed one.
        val mondayView = activityOverview(issueMonday, prague, listOf(goal), completed)
        assertEquals(issueMonday, mondayView.week.monday)
        assertEquals(WeekTotals(0, 0), mondayView.totals)
        assertFalse(mondayView.goalMet)
        assertEquals(listOf(issueMonday), mondayView.days.map { it.epochDay })
        assertEquals(goal, mondayView.goal)
    }

    @Test fun renderedWeekRequiresRereadExactlyAtMondayBoundary() {
        // Sunday 22 Nov 2026: the displayed week (16 Nov) is still today's week.
        assertFalse(weekRolloverRequired(issueWeekStart, issueSunday))
        // Monday 23 Nov 2026: the same snapshot is one week behind and must be re-read.
        assertTrue(weekRolloverRequired(issueWeekStart, issueMonday))
        // Already-current snapshots never force needless refreshes.
        assertFalse(weekRolloverRequired(issueMonday, issueMonday))
        // Comparison is by Monday identity, not by calendar distance.
        assertTrue(weekRolloverRequired(issueWeekStart, issueMonday + 30))
    }

    @Test fun closedWeekBecomesEvaluableOnlyAfterItsSunday() {
        val goal = WeeklyGoal(issueWeekStart, sessions = 5, minutes = 90)
        val completed = (issueWeekStart..issueSunday).map {
            Workout(it, 30 * 60_000L, "practice:$it", WorkoutSource.PRACTICE_SESSION)
        }
        val open = Week(issueWeekStart, prague)
        // Still open on Saturday: no outcome yet.
        assertNull(weekSuccess(open, goal, completed, issueSunday - 1))
        // Closes on Sunday and is judged on Monday.
        assertEquals(true, weekSuccess(open, goal, completed, issueSunday))
        val failed = weekSuccess(open, goal, completed.take(3), issueSunday)
        assertEquals(false, failed)
    }

    @Test fun weeklyStreakSummaryMatchesAndroidFreezeAndCurrentWeekProjection() {
        val first = Week(monday, "UTC")
        val failed = Week(monday + 7, "UTC")
        val current = Week(monday + 14, "UTC")
        val goal = WeeklyGoal(monday, 1, null)
        val workouts = listOf(
            Workout(monday, 60_000, "first", WorkoutSource.PRACTICE_SESSION),
            Workout(current.monday, 60_000, "current", WorkoutSource.PRACTICE_SESSION),
        )

        assertEquals(
            WeeklyStreakSummary(currentStreak = 2, longestStreak = 2, freezeBalance = 0, successfulWeeksTowardFreeze = 0),
            weeklyStreakSummary(listOf(first, failed), current, listOf(goal), workouts),
        )
        assertEquals(
            WeeklyStreakSummary(currentStreak = 1, longestStreak = 1, freezeBalance = 0, successfulWeeksTowardFreeze = 0),
            weeklyStreakSummary(listOf(first, failed), current.copy(beta = true), listOf(goal), workouts),
        )
    }

    @Test fun weeklyStreakIsContinuousAcrossTheMondayBoundary() {
        val goal = WeeklyGoal(issueWeekStart, sessions = 5, minutes = 90)
        val priorWeek = Week(issueWeekStart - 7, prague)
        val currentWeek = Week(issueWeekStart, prague)
        val currentWork = (currentWeek.monday..currentWeek.sunday).map {
            Workout(it, 30 * 60_000L, "current:$it", WorkoutSource.PRACTICE_SESSION)
        }
        val idleWork = emptyList<Workout>()

        // ActivityRepository.snapshot() renders
        //   visibleStreak = closedWeeks.last().streak.currentStreak + (goalMet ? 1 : 0)
        fun visible(closedInput: List<Pair<Week, Boolean?>>, openWeek: Week, openWork: List<Workout>): Int {
            val closed = evaluateWeeks(FreezeState(), closedInput).lastOrNull()?.streak ?: FreezeState()
            val met = !openWeek.beta && goalMet(aggregate(openWork, openWeek), goal)
            return closed.currentStreak + if (met) 1 else 0
        }

        // Sunday: prior week closed and counted; the OPEN week (Nov 16) is counted once via goalMet.
        val sunday = visible(listOf(priorWeek to true), currentWeek, currentWork)
        // Monday: that same week is now CLOSED and counted exactly once, while the OPEN week
        // is the brand-new Nov 23 week with no workouts yet, so goalMet contributes nothing.
        val newWeek = Week(issueMonday, prague)
        val monday = visible(listOf(priorWeek to true, currentWeek to true), newWeek, idleWork)
        assertEquals(2, sunday)
        assertEquals(sunday, monday)

        // An unmet current week contributes nothing on either side of the boundary either.
        assertEquals(
            visible(listOf(priorWeek to true), currentWeek, idleWork),
            visible(listOf(priorWeek to true, currentWeek to false), newWeek, idleWork),
        )
    }
}
