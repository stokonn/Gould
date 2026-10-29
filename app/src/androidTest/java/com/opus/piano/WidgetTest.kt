package com.opus.piano

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.data.*
import com.opus.piano.widgets.*
import kotlinx.coroutines.*
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.time.*
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class WidgetTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val app get() = context.applicationContext as OpusApplication
    private fun snapshot() = WidgetSnapshot(System.currentTimeMillis(), ZoneId.systemDefault().id,
        WidgetWeek(widgetMonday(LocalDate.now()), List(7) { (it + 1) * 60_000L }, 7),
        WidgetPractice("draft", "Original Dvořák title", false), "Light", "Editorial", "Gold", "Soft")
    private fun cache(test: (WidgetSnapshotStore, SharedPreferences) -> Unit) {
        val name = "widget-test-${UUID.randomUUID()}"
        val prefs = context.getSharedPreferences(name, Context.MODE_PRIVATE)
        val wrapper = object : ContextWrapper(context) {
            override fun getSharedPreferences(ignored: String, mode: Int): SharedPreferences = prefs
        }
        try { test(WidgetSnapshotStore(wrapper), prefs) } finally { context.deleteSharedPreferences(name) }
    }
    @Test fun cacheRoundTripsOnlyDisplayDataAndDistinguishesMissingFromVerifiedZero() = cache { store, prefs ->
        assertNull(store.read())
        val verified = snapshot().copy(week = WidgetWeek(widgetMonday(LocalDate.now()), List(7) { 0L }, 0))
        store.write(verified); assertEquals(verified, store.read())
        val keys = JSONObject(prefs.getString("display", null)!!).keys().asSequence().toSet()
        assertFalse(keys.any { it in setOf("notes", "biography", "username", "audio", "token", "credentials") })
        assertEquals(0L, store.read()!!.week.total)
    }
    @Test fun malformedCacheNeverBecomesFreshZeroOrTruncatedCredit() = cache { store, prefs ->
        store.write(snapshot())
        val original = prefs.getString("display", null)!!
        listOf(
            JSONObject(original).put("days", org.json.JSONArray(listOf(1, 2))),
            JSONObject(original).put("days", org.json.JSONArray(listOf(1, 2, 3, 4, 5, 6, -1))),
            JSONObject(original).put("days", org.json.JSONArray(listOf(1, 2, 3, 4, 5, 6, 1.5))),
            JSONObject(original).put("monday", "2026-10-29"),
            JSONObject(original).put("practice", "invented"),
        ).forEach { broken ->
            assertTrue(prefs.edit().putString("display", broken.toString()).commit())
            assertNull(store.read())
        }
    }
    @Test fun realRemoteViewsInflateAcrossSizesThemesAndLargeFontsWithoutUnsupportedActions() {
        val s = snapshot()
        instrumentation.runOnMainSync {
            for (font in listOf(1f, 2f)) for (dark in listOf(false, true)) {
                val config = Configuration(context.resources.configuration).apply { fontScale = font }
                val target = context.createConfigurationContext(config)
                val data = s.copy(theme = if (dark) "Dark" else "Light")
                for ((width, height) in listOf(48f to 48f, 120f to 52f, 120f to 90f, 250f to 180f)) {
                    val view = WidgetRenderer.view(target, data, true, 11, width, height).apply(target, FrameLayout(target))
                    assertTrue(view.contentDescription.isNotBlank())
                    if (width < 96) assertEquals(View.GONE, view.findViewById<View>(R.id.widget_text).visibility)
                }
                for ((width, height) in listOf(120f to 80f, 140f to 160f, 280f to 220f)) {
                    val view = WidgetRenderer.view(target, data, false, 12, width, height).apply(target, FrameLayout(target))
                    assertTrue(view.contentDescription.isNotBlank())
                    assertTrue(view.findViewById<TextView>(R.id.widget_value).text.isNotBlank())
                    if (height < 100) assertEquals(View.GONE, view.findViewById<View>(R.id.widget_chart).visibility)
                }
                val unavailable = WidgetRenderer.view(target, null, false, 13, 140f, 160f).apply(target, FrameLayout(target))
                assertEquals("—", unavailable.findViewById<TextView>(R.id.widget_value).text.toString())
                assertEquals(View.GONE, unavailable.findViewById<View>(R.id.widget_chart).visibility)
            }
        }
    }
    @Test fun weeklyFreshnessFooterRemainsInsideHostAtLargeFontsBeforeProviderRefresh() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val normal = context.createConfigurationContext(Configuration(context.resources.configuration).apply { fontScale = 1f })
            val large = context.createConfigurationContext(Configuration(context.resources.configuration).apply { fontScale = 2f })
            val data = snapshot()
            // RemoteViews can be retained by the launcher while its font changes.
            val view = WidgetRenderer.view(normal,data,false,99,280f,220f).apply(large,FrameLayout(large))
            val density = large.resources.displayMetrics.density
            val width = (280*density).toInt(); val height = (220*density).toInt()
            view.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(height,View.MeasureSpec.EXACTLY))
            view.layout(0,0,width,height)
            val footer = view.findViewById<TextView>(R.id.widget_status)
            assertTrue(footer.text.isNotBlank())
            assertTrue("Freshness footer clipped after host font change",footer.bottom <= height && footer.top >= 0)
        }
    }
    @Test fun widgetRegistrationsAndImmutablePerInstanceActivityIntentsAreCorrect() {
        val names = AppWidgetManager.getInstance(context).installedProviders.filter { it.provider.packageName == context.packageName }
        assertEquals(setOf(QuickPracticeWidget::class.java.name, WeeklyActivityWidget::class.java.name), names.map { it.provider.className }.toSet())
        assertTrue(names.all { it.widgetCategory and android.appwidget.AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN != 0 })
        val first = WidgetRenderer.openIntent(context, "quick", 31)
        val other = WidgetRenderer.openIntent(context, "quick", 32)
        val weekly = WidgetRenderer.openIntent(context, "week", 31)
        assertNotEquals(first, other); assertNotEquals(first, weekly)
        if (Build.VERSION.SDK_INT >= 31) { assertTrue(first.isImmutable); assertTrue(first.isActivity) }
        assertEquals(context.packageName, first.creatorPackage)
    }
    @Test fun boundedDaoProjectionPreservesRecordedDatesAndDoesNotCountExpandedParts() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, OpusDatabase::class.java).build()
        try {
            val zone = ZoneId.of("Europe/Prague"); val monday = LocalDate.parse("2026-10-26")
            fun epoch(day: String) = LocalDate.parse(day).atTime(12, 0).atZone(zone).toInstant().toEpochMilli()
            val one = db.dao().insert(PracticeSession(pieceId = null, pieceTitle = "One", startedAt = 1, completedAt = 2,
                durationMillis = 1000, practiceDay = monday.toString()))
            db.personalDao().insertParts(listOf(SessionPart(sessionId = one, title = "First", durationMillis = 500), SessionPart(sessionId = one, title = "Second", durationMillis = 500)))
            db.dao().insert(PracticeSession(pieceId = null, pieceTitle = "Legacy", startedAt = epoch("2026-10-31") - 2000, completedAt = epoch("2026-10-31"), durationMillis = 2000))
            db.dao().insert(PracticeSession(pieceId = null, pieceTitle = "Stored outside week", startedAt = 1, completedAt = epoch("2026-10-29"), durationMillis = 9000, practiceDay = "2026-10-20"))
            val rows = db.practiceDao().widgetSessions(monday.toString(), monday.plusDays(6).toString(), monday.atStartOfDay(zone).toInstant().toEpochMilli(), monday.plusDays(7).atStartOfDay(zone).toInstant().toEpochMilli())
            val week = widgetWeek(rows, monday.plusDays(3), zone)
            assertEquals(3000L, week.total); assertEquals(2, week.sessions)
            assertEquals(1000L, week.credited.first()); assertEquals(2000L, week.credited[5])
        } finally { db.close() }
    }
    private suspend fun ready() {
        withTimeout(10_000) { while (!app.practice.state.value.ready) delay(20) }
    }
    private suspend fun tap(id: Int) {
        WidgetRenderer.openIntent(context, "quick", id).send()
        delay(1000); instrumentation.waitForIdleSync()
    }
    @Test fun readyWidgetTapDoesNotCreateSessionDraftAudioOrStartCapture() = runBlocking {
        ready()
        val before = app.practice.state.value
        check(before.token.isBlank() && !before.running) { "Requires the disposable emulator's ready fixture" }
        val sessions = app.database.dao().allSessions()
        val draft = app.database.practiceDao().draft()
        val audio = app.database.studioDao().allAudio()
        val recording = app.recording.state.value
        tap(41)
        assertEquals(0L, app.openPractice.value)
        assertEquals(before, app.practice.state.value)
        assertEquals(sessions, app.database.dao().allSessions()); assertEquals(draft, app.database.practiceDao().draft())
        assertEquals(audio, app.database.studioDao().allAudio()); assertEquals(recording, app.recording.state.value)
    }
    @Test fun activeAndPausedWidgetTapsPreserveTheSamePracticeWithoutResumeOrDuplicateStart() = runBlocking {
        ready(); check(app.practice.state.value.token.isBlank())
        val count = app.database.dao().allSessions().size
        app.practice.command { start(false) }.join()
        val token = app.practice.state.value.token
        try {
            tap(42)
            assertEquals(token, app.practice.state.value.token); assertTrue(app.practice.state.value.running)
            app.practice.command { pause() }.join()
            val paused = app.practice.state.value
            val draft = app.database.practiceDao().draft()
            tap(43)
            assertEquals(paused, app.practice.state.value); assertFalse(app.practice.state.value.running)
            assertEquals(draft, app.database.practiceDao().draft()); assertEquals(count, app.database.dao().allSessions().size)
        } finally {
            // Only this explicitly created synthetic practice is reset, never another draft.
            if (app.practice.state.value.token == token) app.practice.command { pause(); reset() }.join()
        }
    }
}
