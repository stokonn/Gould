package com.opus.piano.widgets

import com.opus.piano.data.AchievementEngine
import com.opus.piano.data.ManualActivityEntry
import com.opus.piano.data.PracticeSession
import com.opus.piano.practice.PracticeState
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters

data class WidgetWeek(val monday: LocalDate, val credited: List<Long>, val sessions: Int) {
    init { require(credited.size == 7 && credited.all { it >= 0 } && sessions >= 0) }
    val total: Long get() = credited.fold(0L, Math::addExact)
    val practicedDays: Int get() = credited.count { it > 0 }
}

data class WidgetHeatmapCellSize(val width: Float, val height: Float)

/** Keep only a small, proportional inset around contribution cells at every widget size. */
fun widgetHeatmapCellSize(slotWidth: Float, gridHeight: Float): WidgetHeatmapCellSize {
    require(slotWidth > 0f && gridHeight > 0f)
    return WidgetHeatmapCellSize(width = slotWidth * .96f, height = gridHeight * .94f)
}

/** Compact four-step intensity for the weekly heatmap; zero remains visibly inactive. */
fun widgetIntensityBucket(durationMillis: Long, weeklyMaximumMillis: Long): Int {
    require(durationMillis >= 0 && weeklyMaximumMillis >= 0)
    if (durationMillis == 0L || weeklyMaximumMillis == 0L) return 0
    val fraction = (durationMillis.toDouble() / weeklyMaximumMillis.toDouble()).coerceIn(0.0, 1.0)
    return when {
        fraction < .25 -> 1
        fraction < .5 -> 2
        fraction < .75 -> 3
        else -> 4
    }
}

fun widgetMonday(day: LocalDate): LocalDate = day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
fun widgetNextMidnight(now: ZonedDateTime): ZonedDateTime = now.toLocalDate().plusDays(1).atStartOfDay(now.zone)

/** Consume session rows, never expanded per-piece attribution or elapsed/recording durations. */
fun widgetWeek(sessions: List<PracticeSession>, today: LocalDate, zone: ZoneId, manualEntries: List<ManualActivityEntry> = emptyList()): WidgetWeek {
    val monday = widgetMonday(today)
    val days = MutableList(7) { 0L }
    var count = 0
    sessions.forEach { session ->
        require(session.durationMillis >= 0)
        if (!AchievementEngine.valid(session)) return@forEach
        val day = AchievementEngine.recordedDay(session, zone)
        val offset = java.time.temporal.ChronoUnit.DAYS.between(monday, day)
        if (offset in 0..6) {
            days[offset.toInt()] = Math.addExact(days[offset.toInt()], session.durationMillis)
            count++
        }
    }
    manualEntries.forEach { entry ->
        val day = LocalDate.parse(entry.activityDay)
        val offset = java.time.temporal.ChronoUnit.DAYS.between(monday, day)
        if (day <= today && offset in 0..6) {
            days[offset.toInt()] = Math.addExact(days[offset.toInt()], entry.durationMillis)
            count++
        }
    }
    return WidgetWeek(monday, days, count).also { it.total }
}

data class WidgetPractice(val mode: String, val title: String = "", val freePractice: Boolean = true)

/** No practice writer, permissions, service starts or intent extras are involved in this projection. */
fun widgetPractice(live: PracticeState?, durable: PracticeState?, unreadable: Boolean): WidgetPractice {
    if (unreadable && live?.ready != true) return WidgetPractice("recovery")
    val state = live?.takeIf { it.ready } ?: durable ?: return WidgetPractice("ready")
    val part = state.parts.getOrNull(state.active)
    val free = part == null || (part.pieceId == null && part.catalogWorkId == null && part.title == "Free practice")
    val mode = when {
        state.running -> "active"
        state.status == "Review" -> "review"
        state.token.isNotBlank() -> "draft"
        else -> "ready"
    }
    return WidgetPractice(mode, if (free) "" else part!!.title.take(500), free)
}

fun widgetSnapshotStale(generatedAt: Long, now: Long, monday: LocalDate, today: LocalDate, zone: String, currentZone: ZoneId): Boolean =
    generatedAt <= 0 || generatedAt > now || now - generatedAt > 6 * 60 * 60 * 1000L ||
        monday != widgetMonday(today) || zone != currentZone.id
