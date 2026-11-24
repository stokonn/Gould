package com.opus.piano.data

import com.opus.piano.activity.domain.Workout
import com.opus.piano.activity.domain.WorkoutSource
import com.opus.piano.activity.domain.manualActivityDates
import java.time.LocalDate
import java.time.ZoneId

/** One row of the unified activity history. Practice sessions and manually added
 *  activities share this presentation so there is exactly one history feed; the
 *  concrete type keeps both sources distinguishable and independently editable. */
sealed interface ActivityHistoryEntry {
    /** Calendar day the activity is attributed to (streak/statistics day). */
    val epochDay: Long
    /** Ordering key inside [epochDay]; newest first. */
    val sortMillis: Long
    val durationMillis: Long
    /** Stable unique id, also the aggregation key that prevents double counting. */
    val sourceId: String
}

data class PracticeHistoryEntry(
    val session: PracticeSession,
    override val epochDay: Long,
    override val sortMillis: Long,
) : ActivityHistoryEntry {
    override val durationMillis: Long get() = session.durationMillis
    override val sourceId: String get() = "practice:${session.id}"
}

data class ManualHistoryEntry(
    val entry: ManualActivityEntry,
    override val epochDay: Long,
    override val sortMillis: Long,
) : ActivityHistoryEntry {
    override val durationMillis: Long get() = entry.durationMillis
    override val sourceId: String get() = "manual:${entry.id}"
}

/** Manual-entry edit window: keep the entry's own stored day, or move it into the
 *  usual three-day window (today, yesterday, two days ago). Unparsable days are
 *  never accepted. */
fun manualActivityEditDayAllowed(originalDay: String, requestedDay: String, today: LocalDate): Boolean {
    if (requestedDay == originalDay) return true
    val requested = runCatching { LocalDate.parse(requestedDay) }.getOrNull() ?: return false
    return requested.toEpochDay() in manualActivityDates(today.toEpochDay())
}

/** Single source of truth for the unified feed: both tables merged, newest day
 *  first, ties newest-first inside the day, and every stored row represented at
 *  most once (keyed by [ActivityHistoryEntry.sourceId]). Existing historical
 *  records are never dropped: sessions keep their recorded day and manual rows
 *  keep their stored activity day, including days before Activity activation. */
fun unifiedActivityHistory(
    sessions: List<PracticeSession>,
    manualEntries: List<ManualActivityEntry>,
    today: LocalDate,
    zone: ZoneId,
): List<ActivityHistoryEntry> {
    val practice = sessions.mapNotNull { session ->
        if (session.id == 0L) return@mapNotNull null
        val day = AchievementEngine.recordedDay(session, zone)
        PracticeHistoryEntry(session, day.toEpochDay(), session.completedAt)
    }
    val manual = manualEntries.mapNotNull { entry ->
        val day = runCatching { LocalDate.parse(entry.activityDay) }.getOrNull() ?: return@mapNotNull null
        ManualHistoryEntry(entry, day.toEpochDay(), entry.createdAt)
    }
    return (practice + manual)
        .distinctBy { it.sourceId }
        .sortedWith(
            compareByDescending<ActivityHistoryEntry> { it.epochDay }
                .thenByDescending { it.sortMillis }
                .thenByDescending { it.sourceId },
        )
}

/** Builds the unified workout stream consumed by every weekly rule, streak,
 *  statistic and heatmap. Each stored row is converted exactly once with a
 *  source-unique id, so aggregation can never count one activity twice, and both
 *  sources stay distinguishable through [Workout.source]. */
fun activityWorkouts(
    sessions: List<PracticeSession>,
    manualEntries: List<ManualActivityEntry>,
    today: LocalDate,
    currentZone: String,
): List<Workout> = buildList {
    sessions.forEach { session ->
        if (!AchievementEngine.valid(session)) return@forEach
        val fallback = runCatching { ZoneId.of(session.practiceZone ?: currentZone) }
            .getOrElse { ZoneId.of(currentZone) }
        val day = AchievementEngine.recordedDay(session, fallback)
        if (day <= today) add(Workout(day.toEpochDay(), session.durationMillis, "practice:${session.id}", WorkoutSource.PRACTICE_SESSION))
    }
    manualEntries.forEach { entry ->
        if (entry.durationMillis <= 0L) return@forEach
        val day = runCatching { LocalDate.parse(entry.activityDay) }.getOrNull() ?: return@forEach
        if (day <= today) add(Workout(day.toEpochDay(), entry.durationMillis, "manual:${entry.id}", WorkoutSource.MANUAL_ENTRY))
    }
}.distinctBy { it.sourceId }
