package com.opus.piano.activity.domain

/** Platform-free, common-compatible Activity rules. Dates are UTC epoch-day integers;
 * Android adapters convert LocalDate at the Room/UI boundary. Zone IDs are metadata only. */
private const val DAYS_PER_WEEK = 7L
private fun dayOfWeek(epochDay: Long): Int = (((epochDay + 3L) % DAYS_PER_WEEK + DAYS_PER_WEEK) % DAYS_PER_WEEK).toInt() + 1 // Monday=1
private fun mondayOf(epochDay: Long): Long = epochDay - (dayOfWeek(epochDay) - 1)

enum class WorkoutSource { PRACTICE_SESSION, MANUAL_ENTRY }
data class Workout(val epochDay: Long, val durationMillis: Long, val sourceId: String, val source: WorkoutSource) {
    init { require(durationMillis > 0); require(sourceId.isNotBlank()) }
}

data class WeeklyGoal(val effectiveMonday: Long, val sessions: Int, val minutes: Int?) {
    init { require(dayOfWeek(effectiveMonday) == 1); require(sessions in 1..7); require(minutes == null || minutes > 0) }
}

data class Week(val monday: Long, val zoneId: String, val beta: Boolean = false) {
    init { require(dayOfWeek(monday) == 1); require(zoneId.isNotBlank()) }
    val sunday: Long get() = monday + 6L
    fun dates(through: Long = sunday): List<Long> = (monday..minOf(through, sunday)).toList()
}

enum class DayKind { TRAINING, DAY_OFF, MISS }
data class DayActivity(val epochDay: Long, val workouts: Int, val minutes: Long, val kind: DayKind)
data class WeekTotals(val sessions: Int, val minutes: Long)
data class FreezeState(val balance: Int = 1, val successfulWeeksTowardFreeze: Int = 0, val currentStreak: Int = 0, val longestStreak: Int = 0) {
    init { require(balance in 0..3); require(successfulWeeksTowardFreeze in 0..3); require(currentStreak >= 0 && longestStreak >= currentStreak) }
}
data class WeekOutcome(val week: Week, val success: Boolean?, val streak: FreezeState, val freezeConsumed: Boolean)

/** A Monday change itself applies next Monday, so goal edits never affect the current week. */
fun nextGoalMonday(changeEpochDay: Long): Long = mondayOf(changeEpochDay) + DAYS_PER_WEEK

fun activeGoal(goals: List<WeeklyGoal>, monday: Long): WeeklyGoal =
    goals.filter { it.effectiveMonday <= monday }.maxByOrNull { it.effectiveMonday }
        ?: WeeklyGoal(mondayOf(monday), sessions = 3, minutes = null)

fun weekFor(epochDay: Long, zoneId: String, firstUseEpochDay: Long? = null): Week {
    val monday = mondayOf(epochDay)
    val betaMonday = firstUseEpochDay?.takeIf { dayOfWeek(it) != 1 }?.let(::mondayOf)
    return Week(monday, zoneId, beta = betaMonday == monday)
}

fun aggregate(workouts: List<Workout>, week: Week): WeekTotals {
    val rows = workouts.filter { it.epochDay in week.monday..week.sunday }
    return WeekTotals(rows.size, rows.fold(0L) { total, row -> saturatingAdd(total, row.durationMillis) } / 60_000L)
}

/** Actual work wins; then the earliest inactive dates use the allowance. */
fun classifyWeek(week: Week, goal: WeeklyGoal, workouts: List<Workout>, through: Long = week.sunday): List<DayActivity> {
    require(goal.effectiveMonday <= week.monday)
    val byDate = workouts.filter { it.epochDay in week.monday..week.sunday }.groupBy { it.epochDay }
    var dayOffRemaining = 7 - goal.sessions
    return week.dates(through).map { date ->
        val rows = byDate[date].orEmpty()
        val kind = when {
            rows.isNotEmpty() -> DayKind.TRAINING
            dayOffRemaining > 0 -> DayKind.DAY_OFF.also { dayOffRemaining-- }
            else -> DayKind.MISS
        }
        DayActivity(date, rows.size, rows.fold(0L) { total, row -> saturatingAdd(total, row.durationMillis) } / 60_000L, kind)
    }
}

/** A current or Beta Week is not a failed week. */
fun weekSuccess(week: Week, goal: WeeklyGoal, workouts: List<Workout>, evaluatedThrough: Long): Boolean? =
    if (week.beta || evaluatedThrough < week.sunday) null else aggregate(workouts, week).sessions >= goal.sessions

/** A protected failed week preserves the streak but earns no freeze progress. */
fun closeWeek(previous: FreezeState, success: Boolean, beta: Boolean = false): Pair<FreezeState, Boolean> {
    if (beta) return previous to false
    if (!success) {
        return if (previous.balance > 0) previous.copy(balance = previous.balance - 1, successfulWeeksTowardFreeze = 0) to true
        else previous.copy(currentStreak = 0, successfulWeeksTowardFreeze = 0) to false
    }
    val streak = previous.currentStreak + 1
    val longest = maxOf(previous.longestStreak, streak)
    if (previous.balance == 3) return previous.copy(currentStreak = streak, longestStreak = longest) to false
    val toward = previous.successfulWeeksTowardFreeze + 1
    return if (toward == 4) previous.copy(balance = previous.balance + 1, successfulWeeksTowardFreeze = 0, currentStreak = streak, longestStreak = longest) to false
    else previous.copy(successfulWeeksTowardFreeze = toward, currentStreak = streak, longestStreak = longest) to false
}

/** Evaluates weeks once, oldest first. Persistence must key outcomes/grants by week start. */
fun evaluateWeeks(initial: FreezeState, weeks: List<Pair<Week, Boolean?>>): List<WeekOutcome> {
    var state = initial
    var priorMonday: Long? = null
    return weeks.sortedBy { it.first.monday }.map { (week, success) ->
        require(priorMonday == null || week.monday > priorMonday!!) { "Duplicate weekly outcome" }
        priorMonday = week.monday
        if (success == null || week.beta) WeekOutcome(week, null, state, false)
        else {
            val (next, consumed) = closeWeek(state, success)
            state = next
            WeekOutcome(week, success, state, consumed)
        }
    }
}

fun saturatingAdd(left: Long, right: Long): Long =
    if (right > 0 && Long.MAX_VALUE - left < right) Long.MAX_VALUE else left + right
