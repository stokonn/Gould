package com.opus.piano.widgets

import com.opus.piano.data.AchievementEngine
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

fun widgetMonday(day: LocalDate): LocalDate = day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
fun widgetNextMidnight(now: ZonedDateTime): ZonedDateTime = now.toLocalDate().plusDays(1).atStartOfDay(now.zone)

/** Consume session rows, never expanded per-piece attribution or elapsed/recording durations. */
fun widgetWeek(sessions: List<PracticeSession>, today: LocalDate, zone: ZoneId): WidgetWeek {
    val monday = widgetMonday(today)
    val days = MutableList(7) { 0L }
    var count = 0
    sessions.forEach { session ->
        require(session.durationMillis >= 0)
        if (session.completedAt <= 0 || session.durationMillis == 0L) return@forEach
        val day = AchievementEngine.recordedDay(session, zone)
        val offset = java.time.temporal.ChronoUnit.DAYS.between(monday, day)
        if (offset in 0..6) {
            days[offset.toInt()] = Math.addExact(days[offset.toInt()], session.durationMillis)
            if (AchievementEngine.valid(session)) count++
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
