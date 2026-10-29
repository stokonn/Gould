package com.opus.piano

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.i18n.L
import com.opus.piano.ui.*
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class SamsungBlockerAcceptanceTest {
    private val inst = InstrumentationRegistry.getInstrumentation()
    private val context get() = inst.targetContext
    @Test fun bundledTerminalFontHasEqualAsciiColumnsAndUnclippedScaledLineMetrics() {
        val font = requireNotNull(ResourcesCompat.getFont(context, R.font.droid_sans_mono))
        val characters = (TUX_ART + TUX_SIGNATURE).filter { it != '\n' }.toSet()
        assertFalse(TUX_ART.contains('\t'))
        assertEquals("created by stokonn with love <3", TUX_SIGNATURE)
        for (scale in listOf(1f, 1.5f, 2f)) {
            val paint = Paint().apply { typeface = font; textSize = 12 * context.resources.displayMetrics.density * scale }
            val cell = paint.measureText(" ")
            characters.forEach { assertEquals("Different advance for '$it' at $scale", cell, paint.measureText(it.toString()), .01f) }
            assertTrue(paint.fontMetrics.descent - paint.fontMetrics.ascent <= 15 * context.resources.displayMetrics.density * scale)
        }
    }
    private fun nodes(root: AccessibilityNodeInfo?): List<AccessibilityNodeInfo> =
        if (root == null) emptyList() else listOf(root) + (0 until root.childCount).flatMap { nodes(root.getChild(it)) }
    private fun tree(): List<AccessibilityNodeInfo> {
        if (Build.VERSION.SDK_INT >= 33) inst.uiAutomation.clearCache()
        return nodes(inst.uiAutomation.rootInActiveWindow)
    }
    private fun bounds(node: AccessibilityNodeInfo) = Rect().also(node::getBoundsInScreen)
    @Test fun actualHeaderAndTuxRenderAtRequiredResponsiveExtremes() {
        assumeTrue("Explicit emulator visual venue only", InstrumentationRegistry.getArguments().getString("gouldSamsungUi") == "true")
        val qemu = inst.uiAutomation.executeShellCommand("getprop ro.kernel.qemu").use {
            android.os.ParcelFileDescriptor.AutoCloseInputStream(it).bufferedReader().readText().trim()
        }
        assertEquals("1", qemu)
        val directory = File(context.filesDir, "samsung-blocker-acceptance/${UUID.randomUUID()}").apply { mkdirs() }
        fun capture(name: String) {
            val bitmap = requireNotNull(inst.uiAutomation.takeScreenshot())
            File(directory, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
            File(directory, "$name.txt").writeText(tree().filter { it.text != null || it.contentDescription != null }.joinToString("\n") {
                "${bounds(it).toShortString()} text=${it.text} description=${it.contentDescription} clickable=${it.isClickable}"
            })
        }
        data class Case(val width: Int, val font: Float, val locale: String, val theme: String)
        val cases = listOf(Case(320,1f,"en","Light"), Case(360,1f,"cs","Light"), Case(412,1f,"cs","Dark"),
            Case(320,2f,"cs","Dark"), Case(360,1.5f,"en","Light"), Case(412,2f,"en","Light"))
        try {
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                for (case in if (InstrumentationRegistry.getArguments().getString("gouldTuxOnly") == "true") emptyList() else cases) {
                    val clicks = AtomicInteger()
                    scenario.onActivity { activity ->
                        L.refresh(case.locale)
                        activity.setContent {
                            val density = LocalDensity.current.density
                            CompositionLocalProvider(LocalDensity provides Density(density, case.font)) {
                                OpusTheme(case.theme) {
                                    Surface(Modifier.fillMaxSize()) {
                                        Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                                            Box(Modifier.width(case.width.dp).padding(20.dp)) {
                                                // A real app-locale change recreates the composition; the
                                                // isolated fixture must also discard cached text lambdas.
                                                androidx.compose.runtime.key(case) { LibraryHeader { clicks.incrementAndGet() } }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    SystemClock.sleep(500); inst.waitForIdleSync()
                    val title = L.s(R.string.stage7a_library_title)
                    val subtitle = L.s(R.string.stage7a_library_subtitle)
                    var current = tree()
                    for (attempt in 0 until 15) {
                        if (current.any { it.text?.toString() == title }) break
                        SystemClock.sleep(100); current = tree()
                    }
                    capture("library-${case.width}-${case.font}-${case.locale}-${case.theme}-observed")
                    val heading = requireNotNull(current.firstOrNull { it.text?.toString() == title }) { "Missing heading: $title for $case; observed=${current.map { it.text }}" }
                    val sub = requireNotNull(current.firstOrNull { it.text?.toString() == subtitle }) { "Missing subtitle: $subtitle for $case" }
                    val actionLabel = L.fixed("Previous")
                    val action = requireNotNull(current.firstOrNull { it.text?.toString() == actionLabel || it.contentDescription?.toString() == actionLabel }) { "Missing action: $actionLabel for $case; observed=${current.map { it.text to it.contentDescription }}" }
                    var button = action
                    while (!button.isClickable && button.parent != null) button = button.parent
                    val h = bounds(heading); val b = bounds(button); val s = bounds(sub)
                    assertTrue("Previous stays beside heading", h.right <= b.left && h.top < b.bottom && b.top < h.bottom)
                    assertTrue("Subtitle stays below the heading/action row", s.top >= maxOf(h.bottom,b.bottom))
                    assertTrue("Action touch target", b.width() >= 48 * context.resources.displayMetrics.density && b.height() >= 48 * context.resources.displayMetrics.density)
                    capture("library-${case.width}-${case.font}-${case.locale}-${case.theme}")
                    assertTrue(button.performAction(AccessibilityNodeInfo.ACTION_CLICK))
                    inst.waitForIdleSync(); assertEquals(1, clicks.get())
                }
                // Android Dialog owns its density. Use the actual platform scale,
                // never label a fixture-only LocalDensity override as system font QA.
                val font = context.resources.configuration.fontScale
                InstrumentationRegistry.getArguments().getString("gouldPlatformFontScale")?.let {
                    assertEquals("Actual Android dialog scale", it.toFloat(), font, .01f)
                }
                for (theme in listOf("Light","Dark")) {
                    scenario.onActivity { activity ->
                        L.refresh("en")
                        activity.setContent {
                            OpusTheme(theme) { androidx.compose.runtime.key(font,theme) { TuxScreen {} } }
                        }
                    }
                    SystemClock.sleep(500); inst.waitForIdleSync()
                    assertTrue(tree().any { it.text?.toString() == TUX_ART })
                    assertTrue(tree().any { it.text?.toString() == TUX_SIGNATURE })
                    assertTrue(tree().any { it.text?.toString() == L.s(R.string.msg_close_bbfa) })
                    capture("tux-platform-$font-$theme")
                }
            }
            println("SAMSUNG_BLOCKER_EVIDENCE=${directory.absolutePath}")
        } finally { inst.runOnMainSync { L.clearOverride() } }
    }
}
