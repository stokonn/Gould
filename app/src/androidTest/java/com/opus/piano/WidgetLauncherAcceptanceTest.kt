package com.opus.piano

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.i18n.L
import com.opus.piano.widgets.*
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.UUID

/** Opt-in actual launcher probe. Never starts/resets practice or writes the display cache. */
@androidx.test.filters.SdkSuppress(minSdkVersion = 36)
@RunWith(AndroidJUnit4::class)
class WidgetLauncherAcceptanceTest {
    @Test fun actualPlacedWidgetsExposeUnavailableAndStaleStatesWithoutChangingStoredSnapshot() {
        val inst = InstrumentationRegistry.getInstrumentation()
        assumeTrue("Requires explicit real-launcher acceptance", InstrumentationRegistry.getArguments().getString("gouldWidgetLauncher") == "true")
        val qemu = inst.uiAutomation.executeShellCommand("getprop ro.kernel.qemu").use {
            android.os.ParcelFileDescriptor.AutoCloseInputStream(it).bufferedReader().readText().trim()
        }
        assertEquals("Emulator only", "1", qemu)
        val context = inst.targetContext
        val manager = AppWidgetManager.getInstance(context)
        val quick = manager.getAppWidgetIds(ComponentName(context, QuickPracticeWidget::class.java))
        val weekly = manager.getAppWidgetIds(ComponentName(context, WeeklyActivityWidget::class.java))
        assertTrue("Place two independent Quick widgets first", quick.size >= 2)
        assertTrue("Place a Weekly widget first", weekly.isNotEmpty())
        L.refresh()
        val store = WidgetSnapshotStore(context)
        val original = requireNotNull(store.read()) { "Requires an existing verified snapshot" }
        val directory = File(context.filesDir, "widget-launcher-acceptance/${UUID.randomUUID()}").apply { mkdirs() }
        inst.uiAutomation.executeShellCommand("input keyevent 3").close()
        SystemClock.sleep(1500)
        fun nodes(root: AccessibilityNodeInfo?): List<AccessibilityNodeInfo> =
            if (root == null) emptyList() else listOf(root) + (0 until root.childCount).flatMap { nodes(root.getChild(it)) }
        fun capture(name: String, expected: String) {
            var roots = emptyList<AccessibilityNodeInfo>()
            for (attempt in 0 until 10) {
                inst.uiAutomation.clearCache()
                roots = nodes(inst.uiAutomation.rootInActiveWindow).filter {
                    it.viewIdResourceName == "${context.packageName}:id/widget_root" && it.isVisibleToUser
                }
                if (roots.size == quick.size + weekly.size && roots.all { it.contentDescription?.contains(expected) == true }) break
                SystemClock.sleep(300)
            }
            assertEquals("All placed instances visible on actual launcher", quick.size + weekly.size, roots.size)
            val records = JSONArray()
            roots.forEach { node ->
                assertTrue("Honest state in accessibility description", node.contentDescription?.contains(expected) == true)
                assertTrue("Safe action stays actionable", node.isClickable && node.isEnabled)
                val bounds = Rect().also(node::getBoundsInScreen)
                val minimum = 48 * context.resources.displayMetrics.density
                assertTrue("48dp touch target", bounds.width() >= minimum && bounds.height() >= minimum)
                records.put(JSONObject().put("description", node.contentDescription).put("bounds", bounds.toShortString()))
            }
            File(directory, "$name.json").writeText(records.toString(2))
            val bitmap = requireNotNull(inst.uiAutomation.takeScreenshot())
            File(directory, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
        try {
            WidgetRenderer.renderAll(context, null)
            capture("unavailable", L.s(R.string.widget_unavailable))
            WidgetRenderer.renderAll(context, original, forceStale = true)
            val last = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(L.locale)
                .format(Instant.ofEpochMilli(original.generatedAt).atZone(ZoneId.systemDefault()))
            capture("stale", L.s(R.string.widget_stale, last))
            assertEquals("Probe must not replace persisted verified cache", original, store.read())
            println("WIDGET_LAUNCHER_EVIDENCE=${directory.absolutePath}")
        } finally {
            WidgetRenderer.renderAll(context, store.read())
        }
    }
}
