package com.opus.piano

import android.graphics.Bitmap
import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.os.LocaleListCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.admin.*
import com.opus.piano.i18n.L
import com.opus.piano.ui.*
import com.opus.piano.ui.admin.*
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Real production screen composables in a test scaffold with the production
 * five-tab/bars budget. Synthetic legacy C6 fixtures, not live integration or a
 * full OpusApp navigation acceptance. No fixture is packaged in the app APK. */
@RunWith(AndroidJUnit4::class)
class Stage7bAcceptanceTest {
    private val inst = InstrumentationRegistry.getInstrumentation()
    private val app get() = inst.targetContext.applicationContext as OpusApplication
    private fun shell(command: String): String = inst.uiAutomation.executeShellCommand(command).use {
        android.os.ParcelFileDescriptor.AutoCloseInputStream(it).bufferedReader().readText()
    }
    private fun export(name: String, write: (File) -> Unit) {
        val file = File(app.filesDir, "stage7b-evidence/$name").apply { parentFile!!.mkdirs() }
        write(file)
        shell("mkdir -p /data/local/tmp/goldberg-stage7b")
        shell("sh -c 'run-as com.opus.piano cat files/stage7b-evidence/$name > /data/local/tmp/goldberg-stage7b/$name'")
    }
    private fun nodes(root: AccessibilityNodeInfo?): List<AccessibilityNodeInfo> {
        if (root == null) return emptyList()
        return listOf(root) + (0 until root.childCount).flatMap { nodes(root.getChild(it)) }
    }
    private fun tree() = nodes(inst.uiAutomation.rootInActiveWindow)
    private fun waitUntil(check: () -> Boolean) {
        val end = android.os.SystemClock.uptimeMillis() + 15000
        while (android.os.SystemClock.uptimeMillis() < end) {
            inst.waitForIdleSync()
            if (check()) return
            Thread.sleep(50)
        }
        fail("Acceptance condition timed out")
    }
    private fun capture(name: String) {
        inst.waitForIdleSync(); Thread.sleep(180)
        export("$name.png") { file ->
            val bitmap = inst.uiAutomation.takeScreenshot()
            assertNotNull("No emulator screenshot", bitmap)
            file.outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
            bitmap.recycle()
        }
        export("$name.txt") { file ->
            file.writeText("locale=${L.locale} font=${app.resources.configuration.fontScale} size=${shell("wm size").trim()} density=${shell("wm density").trim()}\n" + tree().joinToString("\n") {
                val r = Rect(); it.getBoundsInScreen(r)
                "bounds=$r text=${it.text} description=${it.contentDescription} clickable=${it.isClickable} enabled=${it.isEnabled} scrollable=${it.isScrollable} focusable=${it.isFocusable}"
            })
        }
    }
    private class Fixture {
        var mode = "ready"
        var revision = 1
        val api = AdminApi({ "https://synthetic.invalid" }, AdminTransport { _, url, _, _ ->
            if (mode == "loading") kotlinx.coroutines.delay(800)
            if (mode == "error") return@AdminTransport AdminHttpResponse(503, "{}".toByteArray(), null)
            val payload = """{"title":"Synthetic Invention — long contributor title retained verbatim", "composer":"Johann Sebastian Bach", "fields":[{"field":"title","newValue":"Synthetic title $revision — preserved contributor text", "evidence":{"url":"https://source.invalid/fixture","license":"CC0 synthetic fixture","note":"Synthetic evidence note; no external request"}}]}"""
            val detail = """{"id":"sub_1","owner":"synthetic-author","status":"submitted","revision":$revision,"revisions":[{"revision":$revision,"createdAt":1700000000000,"payload":$payload}],"reviewEvents":[]}"""
            val body = when {
                url.contains("capabilities") -> """{"principalId":"synthetic-moderator","capabilities":["queue.read","audit.read","decide"]}"""
                url.contains("/submissions/") -> detail
                url.contains("/queue") -> if (mode == "empty") """{"items":[],"nextCursor":null}""" else """{"items":[{"id":"sub_1","owner":"synthetic-author","status":"submitted","revision":$revision,"evidenceCount":1,"createdAt":1700000000000}],"nextCursor":null}"""
                else -> if (mode == "empty") """{"items":[],"nextCursor":null,"chainHead":"synthetic"}""" else """{"items":[{"seq":1,"ts":1700000000000,"actor":"synthetic-moderator","action":"decision.reject","subject":"sub_1","prevHash":"synthetic","thisHash":"synthetic"}],"nextCursor":null,"chainHead":"synthetic"}"""
            }
            AdminHttpResponse(200, body.toByteArray(), null)
        }, token = { "synthetic-not-a-credential" })
        val repo = AdminRepository(api, InMemoryAdminSessionStore(), AdminTrustPolicy(),
            AdminMarkers(InMemoryAdminMarkerStore()) { "synthetic" }, AdminStatsService(api), decisionsEnabled = true)
    }

    @Test fun renderApprovedAdminMatrixAndRetainAccessibilityTrees() {
        val oldFont = shell("settings get system font_scale").trim()
        val oldLocales = AppCompatDelegate.getApplicationLocales()
        val prefix = "stage7b"
        val failures = mutableListOf<String>()
        try {
            // Small (360x640dp) and standard (412x915dp); actual display overrides.
            for ((size, pixels) in listOf("small" to "720x1280", "standard" to "824x1830")) {
                shell("wm size $pixels"); shell("wm density 320")
                for (locale in listOf("en", "cs")) for (theme in listOf("Light", "Dark")) {
                    for (font in listOf("1.0", "1.3", "1.5", "2.0")) {
                        shell("settings put system font_scale $font")
                        inst.runOnMainSync { AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(locale)); L.refresh() }
                        val fixture = Fixture()
                        val route = mutableStateOf("dashboard")
                        try { ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                            lateinit var model: AdminViewModel
                            scenario.onActivity { activity ->
                                model = AdminViewModel(app, fixture.repo)
                                activity.viewModelStore.put("stage7b-acceptance", model)
                                activity.setContent {
                                    OpusTheme(theme) {
                                        Scaffold(bottomBar = {
                                            NavigationBar {
                                                listOf(R.string.label_home, R.string.label_repertoire, R.string.discover, R.string.label_practice, R.string.label_profile).forEach {
                                                    NavigationBarItem(selected = false, onClick = {}, icon = {}, label = { Text(L.s(it), maxLines = 1) })
                                                }
                                            }
                                        }) { padding ->
                                            Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)
                                                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))) {
                                                AdminTheme {
                                                    key(route.value) {
                                                        when (route.value) {
                                                            "queue" -> AdminQueueScreen(model, null, { route.value = "dashboard" }, { route.value = "proposal" })
                                                            "proposal" -> AdminProposalScreen(model, "sub_1", { route.value = "queue" }, {})
                                                            "audit" -> AdminAuditScreen(model) { route.value = "dashboard" }
                                                            "settings" -> AdminSettingsScreen(model) { route.value = "dashboard" }
                                                            else -> AdminDashboardScreen(model, { route.value = "queue" }, { route.value = "audit" }, { route.value = "settings" })
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            waitUntil { !model.loading.value && model.stats.value?.allExact == true && tree().any { it.text?.toString() == L.s(R.string.stage7b_administration) } }
                            val id = "$prefix-$size-$locale-$theme-f$font"
                            capture("$id-dashboard")
                            if (font.toFloat() <= 1.3f) {
                                assertFalse("Dashboard scroll at $id", tree().any { it.isScrollable })
                                for (label in listOf(R.string.stage7b_moderation_queue, R.string.stage7b_audit_log, R.string.stage7b_admin_settings, R.string.stage7b_refresh)) {
                                    assertTrue("Dashboard action missing/clipped at $id: ${L.s(label)}", tree().any {
                                        val r = Rect(); it.getBoundsInScreen(r)
                                        it.isVisibleToUser && (it.text?.toString() == L.s(label) || it.contentDescription?.toString() == L.s(label)) && r.width() > 0 && r.height() > 0 && r.bottom <= pixels.substringAfter('x').toInt()
                                    })
                                }
                            } else assertTrue("Large-font scroll fallback missing", tree().any { it.isScrollable })
                            // Detailed destinations at standard1.0 and small2.0, both locales/themes.
                            if (size == "standard" && font == "1.0" || size == "small" && font == "2.0") {
                                for (destination in listOf("queue", "proposal", "audit", "settings")) {
                                    inst.runOnMainSync { route.value = destination }
                                    waitUntil { when(destination) {
                                        "queue" -> model.queue.state.value.rows.isNotEmpty()
                                        "proposal" -> fixture.repo.review.state.value.verified
                                        "audit" -> fixture.repo.audit.state.value.loaded
                                        else -> tree().any { it.text?.toString() == L.s(R.string.stage7b_admin_settings) }
                                    } }
                                    Thread.sleep(350); capture("$id-$destination")
                                    if (destination == "proposal" && font == "1.0") {
                                        for (decision in AdminDecision.entries) {
                                            runBlocking { fixture.repo.review.prepare(decision) }
                                            assertNotNull(fixture.repo.review.state.value.confirmation)
                                            capture("$id-confirm-${decision.wire}")
                                            fixture.repo.review.cancelConfirmation()
                                        }
                                        fixture.revision = 3
                                        runBlocking { fixture.repo.review.validate("sub_1") }
                                        assertTrue(fixture.repo.review.state.value.reReview)
                                        capture("$id-re-review-incomplete-history")
                                    }
                                }
                                for (mode in listOf("empty", "error", "loading")) {
                                    fixture.mode = mode
                                    fixture.repo.queue.clear(); fixture.repo.audit.clear()
                                    inst.runOnMainSync { route.value = "dashboard"; model.refresh() }
                                    if (mode != "loading") waitUntil { !model.loading.value }
                                    capture("$id-dashboard-$mode")
                                    inst.runOnMainSync { route.value = "queue" }
                                    Thread.sleep(if(mode == "loading") 100 else 500)
                                    capture("$id-queue-$mode")
                                }
                            }
                            scenario.onActivity { it.setContent {} }
                        } } catch (failure: Throwable) {
                            failures += "$size/$locale/$theme/$font: ${failure.message}"
                            capture("$prefix-$size-$locale-$theme-f$font-FAIL")
                        }
                    }
                }
            }
            export("matrix-results.txt") { it.writeText(if(failures.isEmpty()) "PASS: all configurations completed" else failures.joinToString("\n")) }
            assertTrue(failures.joinToString("\n"), failures.isEmpty())
        } finally {
            shell("wm size reset"); shell("wm density reset")
            shell("settings put system font_scale $oldFont")
            inst.runOnMainSync { AppCompatDelegate.setApplicationLocales(oldLocales); L.refresh() }
        }
    }
}
