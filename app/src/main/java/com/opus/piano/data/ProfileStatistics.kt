package com.opus.piano.data

import java.time.LocalDate
import java.time.ZoneId

/** Receives the period-filtered sessions. Include both partial boundary months of a trailing year. */
fun activityBins(sessions: List<PracticeSession>, period: String, today: LocalDate, zone: ZoneId): List<Pair<String, Long>> {
    val days = sessions.groupBy { AchievementEngine.recordedDay(it, zone) }.mapValues { (_, values) -> values.sumOf { it.durationMillis } }
    return when (period) {
        "Week" -> (6 downTo 0).map { today.minusDays(it.toLong()).let { d -> d.dayOfWeek.name.take(2) to (days[d] ?: 0L) } }
        "Month" -> (5 downTo 0).map { offset -> val end = today.minusDays(offset * 5L); "${end.dayOfMonth}" to days.filterKeys { it > end.minusDays(5) && it <= end }.values.sum() }
        else -> {
            val anchor = if (period == "All time") maxOf(java.time.YearMonth.from(today), java.time.YearMonth.from(days.keys.maxOrNull() ?: today)) else java.time.YearMonth.from(today)
            val months = if (period == "Year") 13 else ((days.keys.minOrNull()?.let { java.time.temporal.ChronoUnit.MONTHS.between(java.time.YearMonth.from(it), anchor).toInt() } ?: 0) + 1).coerceAtLeast(1)
            val group = if (period == "Year") 1 else ((months + 11) / 12).coerceAtLeast(1)
            ((months + group - 1) / group - 1 downTo 0).map { offset -> val end = anchor.minusMonths(offset * group.toLong()); end.toString() to days.filterKeys { java.time.YearMonth.from(it) <= end && java.time.YearMonth.from(it) > end.minusMonths(group.toLong()) }.values.sum() }
        }
    }
}

fun profilePeriod(sessions: List<PracticeSession>, period: String, today: LocalDate, zone: ZoneId): List<PracticeSession> {
    if (period == "All time") return sessions
    val from = today.minusDays(when (period) { "Week" -> 6; "Month" -> 29; else -> 364 }.toLong())
    return sessions.filter { AchievementEngine.recordedDay(it, zone) in from..today }
}
fun currentPracticeStreak(sessions: List<PracticeSession>, today: LocalDate, zone: ZoneId): Int {
    val days = sessions.filter(AchievementEngine::valid).map { AchievementEngine.recordedDay(it, zone) }.toSet()
    var day = if (today in days) today else today.minusDays(1)
    var count = 0
    while (day in days) { count++; day = day.minusDays(1) }
    return count
}
fun compactPracticeTime(millis: Long): String {
    val minutes = millis / 60000
    return when { minutes >= 60 -> "${minutes / 60}h ${minutes % 60}m"; minutes > 0 -> "${minutes}m"; millis > 0 -> "${millis / 1000}s"; else -> "0m" }
}
