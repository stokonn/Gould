package com.opus.piano.widgets

import android.content.Context
import androidx.room.withTransaction
import com.opus.piano.OpusApplication
import com.opus.piano.data.PracticeRepository
import com.opus.piano.data.ProfileSettings
import com.opus.piano.preferences.Appearance
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Disposable, non-sensitive display cache. Never a journal, audio or identity backup. */
data class WidgetSnapshot(
    val generatedAt: Long, val zone: String, val week: WidgetWeek,
    val practice: WidgetPractice, val theme: String, val typography: String,
    val accent: String, val cards: String,
) {
    val appearance get() = Appearance(accent = accent, cards = cards)
}

class WidgetSnapshotStore(context: Context) {
    private val prefs = context.getSharedPreferences("gould-widget-snapshot-v1", Context.MODE_PRIVATE)
    fun read(): WidgetSnapshot? = runCatching {
        val raw = prefs.getString("display", null) ?: return null
        require(raw.length <= 32 * 1024)
        val json = JSONObject(raw)
        fun integer(key: String): Long { val value = json.get(key); require(value is Int || value is Long); return (value as Number).toLong() }
        require(integer("version") == 1L)
        val values = json.getJSONArray("days"); require(values.length() == 7)
        val monday = LocalDate.parse(json.getString("monday")); require(widgetMonday(monday) == monday)
        val count = integer("sessions"); require(count in 0..Int.MAX_VALUE.toLong())
        val week = WidgetWeek(monday, (0..6).map {
            val value = values.get(it); require(value is Int || value is Long); (value as Number).toLong()
        }, count.toInt()); week.total
        val mode = json.getString("practice"); require(mode in listOf("ready", "active", "draft", "review", "recovery"))
        val theme = json.getString("theme"); require(theme in listOf("System", "Light", "Dark"))
        val typography = json.getString("typography"); require(typography in listOf("Editorial", "System"))
        val accent = json.getString("accent"); require(accent in com.opus.piano.preferences.AppearanceRules.accents)
        val cards = json.getString("cards"); require(cards in listOf("Soft", "Outline", "Open"))
        val zone = json.getString("zone"); ZoneId.of(zone)
        val created = integer("generated"); require(created > 0)
        require(json.get("title") is String && json.get("free") is Boolean)
        WidgetSnapshot(created, zone, week, WidgetPractice(mode, json.getString("title").take(500), json.getBoolean("free")), theme, typography, accent, cards)
    }.getOrNull()
    fun write(snapshot: WidgetSnapshot) {
        val raw = JSONObject().put("version", 1).put("generated", snapshot.generatedAt).put("zone", snapshot.zone)
            .put("monday", snapshot.week.monday.toString()).put("days", JSONArray(snapshot.week.credited))
            .put("sessions", snapshot.week.sessions).put("practice", snapshot.practice.mode)
            .put("title", snapshot.practice.title).put("free", snapshot.practice.freePractice)
            .put("theme", snapshot.theme).put("typography", snapshot.typography).put("accent", snapshot.accent).put("cards", snapshot.cards)
        check(prefs.edit().putString("display", raw.toString()).commit())
    }
}

/** One persisted transaction, independent of the capture pipeline and expanded attribution. */
internal suspend fun loadWidgetSnapshot(app: OpusApplication, now: Long = System.currentTimeMillis(), zone: ZoneId = ZoneId.systemDefault()): WidgetSnapshot {
    val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
    val monday = widgetMonday(today)
    val live = app.currentPracticeForWidgets()?.state?.value
    val appearance = app.appearance.current()
    return app.database.withTransaction {
        val settings = app.database.personalDao().getSettings() ?: ProfileSettings()
        val sessions = app.database.practiceDao().widgetSessions(monday.toString(), monday.plusDays(6).toString(),
            monday.atStartOfDay(zone).toInstant().toEpochMilli(), monday.plusDays(7).atStartOfDay(zone).toInstant().toEpochMilli())
        val raw = app.database.practiceDao().draft()
        val decoded = raw?.let { runCatching { PracticeRepository.decode(it.payload) } }
        WidgetSnapshot(now, zone.id, widgetWeek(sessions, today, zone),
            widgetPractice(live, decoded?.getOrNull(), decoded?.isFailure == true),
            settings.theme, settings.typography, appearance.accent, appearance.cards)
    }
}
