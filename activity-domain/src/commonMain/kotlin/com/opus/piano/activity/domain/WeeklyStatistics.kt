package com.opus.piano.activity.domain

/** Weekly-first statistics shared by Home and Profile. Both screens read this
 *  one definition so they can never disagree about the current or previous week.
 *  Minutes are whole minutes (same rounding as [aggregate]); [activeDays] counts
 *  distinct calendar days with at least one workout inside the week. */
data class WeeklyStatistics(
    val sessions: Int,
    val minutes: Long,
    val activeDays: Int,
    val previousSessions: Int,
    val previousMinutes: Long,
    val previousActiveDays: Int,
)

/** Monday-based current/previous week totals over already-unified workouts.
 *  The current week only counts days up to and including [today]; the previous
 *  week is always complete. */
fun weeklyStatistics(today: Long, workouts: List<Workout>): WeeklyStatistics {
    val currentMonday = mondayOf(today)
    val previousMonday = currentMonday - DAYS_PER_WEEK
    fun summarize(rows: List<Workout>): Triple<Int, Long, Int> = Triple(
        rows.size,
        rows.fold(0L) { total, row -> saturatingAdd(total, row.durationMillis) } / 60_000L,
        rows.mapTo(mutableSetOf()) { it.epochDay }.size,
    )
    val (sessions, minutes, activeDays) = summarize(workouts.filter { it.epochDay in currentMonday..today })
    val (previousSessions, previousMinutes, previousActiveDays) =
        summarize(workouts.filter { it.epochDay in previousMonday until currentMonday })
    return WeeklyStatistics(sessions, minutes, activeDays, previousSessions, previousMinutes, previousActiveDays)
}
