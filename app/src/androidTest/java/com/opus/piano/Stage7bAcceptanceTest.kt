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
    @android.annotation.SuppressLint("NewApi") // This evidence venue is explicitly API36.
    private fun export(name: String, write: (File) -> Unit) {
        val file = File(app.filesDir, "stage7b-evidence/$name").apply { parentFile!!.mkdirs() }
        write(file)
        shell("mkdir -p /data/local/tmp/goldberg-stage7b")
        // executeShellCommand(String) does not parse shell quoting. Send bytes
        // through API31+ stdin instead of silently relying on sh -c quoting.
        val pipes = inst.uiAutomation.executeShellCommandRw("dd of=/data/local/tmp/goldberg-stage7b/$name")
        android.os.ParcelFileDescriptor.AutoCloseOutputStream(pipes[1]).use { it.write(file.readBytes()) }
        android.os.ParcelFileDescriptor.AutoCloseInputStream(pipes[0]).use { it.readBytes() }
        val exported = inst.uiAutomation.executeShellCommand("cat /data/local/tmp/goldberg-stage7b/$name").use {
            android.os.ParcelFileDescriptor.AutoCloseInputStream(it).readBytes()
        }
        assertArrayEquals("Exported evidence differs: $name", file.readBytes(), exported)
    }
    private fun nodes(root: AccessibilityNodeInfo?): List<AccessibilityNodeInfo> {
        if (root == null) return emptyList()
        return listOf(root) + (0 until root.childCount).flatMap { nodes(root.getChild(it)) }
    }
    @android.annotation.SuppressLint("NewApi") // This evidence venue is explicitly API36.
    private fun tree(): List<AccessibilityNodeInfo> {
        // Role/model state can be ready before the accessibility client cache receives
        // a screen-change event. Never export the previous route's cached hierarchy.
        inst.uiAutomation.clearCache()
        return nodes(inst.uiAutomation.rootInActiveWindow)
    }
    private fun awaitStableBounds() {
        // Accessibility scroll actions launch Compose animations. Main-looper idleness
        // alone does not mean that a frame-driven animation has reached its target.
        fun bounds() = tree().map {
            val rect = Rect(); it.getBoundsInScreen(rect)
            "${it.text}|${it.contentDescription}|$rect|${it.actionList.map { action -> action.id }}"
        }
        var previous = bounds()
        var stable = 0
        waitUntil {
            Thread.sleep(80)
            val current = bounds()
            stable = if (current == previous) stable + 1 else 0
            previous = current
            stable >= 4
        }
    }
    private fun waitUntil(check: () -> Boolean) {
        val end = android.os.SystemClock.uptimeMillis() + 15000
        while (android.os.SystemClock.uptimeMillis() < end) {
            inst.waitForIdleSync()
            if (check()) return
            Thread.sleep(50)
        }
        fail("Acceptance condition timed out")
    }
    private fun click(label: String) {
        var action: AccessibilityNodeInfo? = null
        waitUntil {
            action = tree().asSequence().filter {
                it.text?.toString() == label || it.contentDescription?.toString() == label || it.contentDescription?.toString()?.startsWith("$label:") == true
            }.mapNotNull {
                var target: AccessibilityNodeInfo? = it
                while (target != null && !target.isClickable) target = target.parent
                target?.takeIf { node -> node.isEnabled }
            }.firstOrNull()
            action != null && action!!.isEnabled
        }
        assertTrue("Action failed: $label", action!!.performAction(AccessibilityNodeInfo.ACTION_CLICK))
        inst.waitForIdleSync()
    }
    private fun enterText(value: String) {
        val edit = tree().first { it.isEditable }
        edit.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        assertTrue(edit.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, android.os.Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,value)
        }))
        inst.waitForIdleSync()
    }
    private fun capture(name: String) {
        inst.waitForIdleSync(); Thread.sleep(180)
        val currentTree = tree()
        export("$name.png") { file ->
            val bitmap = inst.uiAutomation.takeScreenshot()
            assertNotNull("No emulator screenshot", bitmap)
            file.outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
            bitmap.recycle()
        }
        export("$name.txt") { file ->
            file.writeText("locale=${L.locale} font=${app.resources.configuration.fontScale} size=${shell("wm size").trim()} density=${shell("wm density").trim()}\n" + currentTree.joinToString("\n") {
                val r = Rect(); it.getBoundsInScreen(r)
                "bounds=$r text=${it.text} description=${it.contentDescription} clickable=${it.isClickable} enabled=${it.isEnabled} scrollable=${it.isScrollable} focusable=${it.isFocusable} checkable=${it.isCheckable} checked=${it.isChecked} class=${it.className} actions=${it.actionList.map { action -> action.id }}"
            })
        }
        assertTrue("Unnamed checkable control in $name", currentTree.filter { it.isCheckable }.all {
            !it.text.isNullOrBlank() || !it.contentDescription.isNullOrBlank()
        })
    }
    @Test fun adminCheckablesExportNameStateAndClickOnSameNode() {
        for (locale in listOf("en", "cs")) {
            val configuration = android.content.res.Configuration(app.resources.configuration).apply {
                setLocales(android.os.LocaleList.forLanguageTags(locale))
            }
            val context = app.createConfigurationContext(configuration)
            val labels = listOf(R.string.stage7b_pending, R.string.stage7b_sync, R.string.stage7b_auto_next).map { context.getString(it) }
            val disabledLabel = context.getString(R.string.stage7b_ack_revision, 1)
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                scenario.onActivity { activity ->
                    activity.setContent {
                        var selected by remember { mutableStateOf(false) }
                        var switched by remember { mutableStateOf(false) }
                        var checked by remember { mutableStateOf(false) }
                        var disabledChecked by remember { mutableStateOf(false) }
                        OpusTheme("Light") { AdminTheme {
                            Box(Modifier.fillMaxSize()) {
                                Column {
                                    AdminFilterChip(selected, labels[0]) { selected = !selected }
                                    AdminSwitch(labels[1], switched) { switched = it }
                                    AdminCheckbox(checked, labels[2]) { checked = it }
                                    AdminCheckbox(disabledChecked, disabledLabel, enabled = false) { disabledChecked = it }
                                }
                            }
                        } }
                    }
                }
                fun control(label: String) = tree().singleOrNull {
                    it.isCheckable && it.contentDescription?.toString() == label
                }
                for (label in labels) {
                    waitUntil { control(label) != null }
                    assertFalse(control(label)!!.isChecked)
                    assertTrue(control(label)!!.isClickable)
                    assertTrue(control(label)!!.isEnabled)
                    assertTrue("Input focus semantics missing: $label", control(label)!!.isFocusable)
                    assertTrue(control(label)!!.performAction(AccessibilityNodeInfo.ACTION_FOCUS))
                    waitUntil { control(label)?.isFocused == true }
                    click(label)
                    waitUntil { control(label)?.isChecked == true }
                    click(label)
                    waitUntil { control(label)?.isChecked == false }
                }
                val disabled = control(disabledLabel)!!
                assertFalse(disabled.isEnabled)
                assertFalse(disabled.isFocusable)
                disabled.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                inst.waitForIdleSync()
                assertFalse("Disabled revision acknowledgement changed", control(disabledLabel)!!.isChecked)
                capture("stage7b-checkables-$locale")
            }
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
                        inst.runOnMainSync { AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(locale)); L.refresh(locale) }
                        val fixture = Fixture()
                        val route = mutableStateOf("dashboard")
                        try { ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                            lateinit var model: AdminViewModel
                            scenario.onActivity { activity ->
                                activity.resources.configuration.setLocales(android.os.LocaleList.forLanguageTags(locale))
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
                            assertEquals("Requested font scale not applied at $id", font.toFloat(), app.resources.configuration.fontScale, 0.01f)
                            assertEquals(locale, L.locale.language)
                            if (font.toFloat() <= 1.3f) assertFalse("Dashboard scroll at $id", tree().any { it.isScrollable })
                            // Larger text needs reachability, not forced scroll when
                            // the complete content already fits a standard viewport.
                            try {
                            for (label in listOf(R.string.stage7b_moderation_queue, R.string.stage7b_audit_log, R.string.stage7b_admin_settings, R.string.stage7b_refresh)) {
                                    fun visible() = tree().any {
                                        if (it.text?.toString() != L.s(label) && it.contentDescription?.toString() != L.s(label)) return@any false
                                        var target = it
                                        while (!target.isClickable && target.parent != null) target = target.parent
                                        val r = Rect(); target.getBoundsInScreen(r)
                                        // Require the complete 48dp action, not a sliver of its
                                        // icon at the scroll viewport edge (density is 320dpi).
                                        target.isVisibleToUser && target.isClickable && r.width() >= 96 && r.height() >= 96 && r.top >= 0 && r.bottom <= pixels.substringAfter('x').toInt()
                                    }
                                    if (font.toFloat()>1.3f && !visible()) {
                                        repeat(6) {
                                            if (!visible()) {
                                                val scroll = tree().firstOrNull { it.isScrollable && it.actionList.any { action -> action.id == AccessibilityNodeInfo.ACTION_SCROLL_FORWARD } }
                                                assertNotNull("No forward scroll action for ${L.s(label)} at $id", scroll)
                                                assertTrue("Scroll action rejected at $id", scroll!!.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD))
                                                awaitStableBounds()
                                            }
                                        }
                                    }
                                    assertTrue("Dashboard action unreachable/clipped at $id: ${L.s(label)}",visible())
                                    if (font.toFloat()<=1.3f) {
                                        var target = tree().first { it.text?.toString()==L.s(label) || it.contentDescription?.toString()==L.s(label) }
                                        while(!target.isClickable && target.parent!=null) target=target.parent
                                        val bounds=Rect(); target.getBoundsInScreen(bounds)
                                        assertTrue("Dashboard touch target below48dp at $id: ${L.s(label)} $bounds",target.isClickable && bounds.width()>=96 && bounds.height()>=96)
                                    }
                                }
                            if(font.toFloat()>1.3f) capture("$id-dashboard-reachable-footer")
                            // Detailed destinations at standard1.0 and small2.0, both locales/themes.
                            if (size == "standard" && font == "1.0" || size == "small" && font == "2.0") {
                                for (destination in listOf("queue", "proposal", "audit", "settings")) {
                                    inst.runOnMainSync { route.value = destination }
                                    val title = when (destination) {
                                        "queue" -> R.string.stage7b_moderation_queue
                                        "proposal" -> R.string.stage7b_review_proposal
                                        "audit" -> R.string.stage7b_audit_log
                                        else -> R.string.stage7b_admin_settings
                                    }
                                    waitUntil {
                                        val current = tree()
                                        current.any { it.text?.toString() == L.s(title) } &&
                                        current.any { it.contentDescription?.toString() == L.s(R.string.msg_back_b52b) } && when(destination) {
                                        "queue" -> current.any { it.isEditable } && model.queue.state.value.rows.isNotEmpty() && !model.queue.state.value.loading
                                        "proposal" -> fixture.repo.review.state.value.verified
                                        "audit" -> current.any { it.isEditable } && fixture.repo.audit.state.value.loaded
                                        else -> true
                                    } }
                                    Thread.sleep(350); capture("$id-$destination")
                                    if (destination == "queue" || destination == "audit") {
                                        fun row(): AccessibilityNodeInfo? = tree().firstOrNull {
                                            it.isVisibleToUser && it.text?.toString()?.contains("sub_1") == true
                                        }
                                        fun reachable(): Boolean {
                                            var target = row() ?: return false
                                            if (destination == "queue") {
                                                while (!target.isClickable && target.parent != null) target = target.parent
                                            }
                                            val bounds = Rect(); target.getBoundsInScreen(bounds)
                                            return target.isVisibleToUser && bounds.width() > 0 &&
                                                bounds.height() >= (if (destination == "queue") 96 else 40) &&
                                                (destination != "queue" || target.isClickable)
                                        }
                                        repeat(8) {
                                            if (!reachable()) {
                                                val scroll = tree().firstOrNull { it.isScrollable && it.actionList.any { action -> action.id == AccessibilityNodeInfo.ACTION_SCROLL_FORWARD } }
                                                assertNotNull("No scroll to $destination row at $id", scroll)
                                                assertTrue(scroll!!.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD))
                                                awaitStableBounds()
                                            }
                                        }
                                        assertTrue("$destination row unreachable at $id", reachable())
                                        capture("$id-$destination-row-reachable")
                                        if (destination == "queue") {
                                            var target = row()!!
                                            while (!target.isClickable && target.parent != null) target = target.parent
                                            assertTrue(target.performAction(AccessibilityNodeInfo.ACTION_CLICK))
                                            waitUntil {
                                                tree().any { it.text?.toString() == L.s(R.string.stage7b_review_proposal) } &&
                                                    fixture.repo.review.state.value.verified && fixture.repo.review.state.value.detail?.id == "sub_1"
                                            }
                                            click(L.s(R.string.msg_back_b52b))
                                            waitUntil {
                                                val current = tree()
                                                current.any { it.text?.toString() == L.s(R.string.stage7b_moderation_queue) } &&
                                                    current.any { it.isEditable } && !model.queue.state.value.loading
                                            }
                                        }
                                    }
                                    if (destination == "queue" && font == "1.0") {
                                        fixture.revision = 2
                                        runBlocking { model.queue.revalidate() }
                                        assertTrue(model.queue.state.value.pendingUpdates>0)
                                        capture("$id-queue-stale-update")
                                        runBlocking { model.queue.refresh() }
                                        assertEquals(0,model.queue.state.value.pendingUpdates)
                                    }
                                    if (destination == "settings" && font == "1.0") {
                                        click(L.s(R.string.stage7b_mobile_consent))
                                        waitUntil { tree().any { it.text?.toString() == L.s(R.string.stage7b_mobile_confirm) } }
                                        capture("$id-metered-confirmation")
                                        assertFalse(model.syncPolicy.value.mobileConsent)
                                        click(L.s(R.string.stage7b_cancel))
                                        click(L.s(R.string.stage7b_endpoint))
                                        waitUntil { tree().any { it.isEditable } }
                                        enterText("http://synthetic-invalid.invalid")
                                        capture("$id-endpoint-keyboard")
                                        click(L.s(R.string.stage7b_save))
                                        waitUntil { tree().any { it.text?.toString() == L.s(R.string.stage7b_reason_invalid) } }
                                        capture("$id-endpoint-validation")
                                        click(L.s(android.R.string.cancel))
                                    }
                                    if (destination == "proposal" && font == "1.0") {
                                        for ((tabName,tabLabel) in listOf("details" to R.string.stage7b_details,"history" to R.string.stage7b_history,"evidence" to R.string.stage7b_evidence,"comparison" to R.string.stage7b_comparison)) {
                                            click(L.s(tabLabel)); capture("$id-proposal-tab-$tabName")
                                        }
                                        // Actual row navigation reviewed revision1 before the
                                        // stale-queue fixture advanced to revision2. Respect the
                                        // real re-review gate before exercising confirmations.
                                        assertTrue("Changed revision bypassed re-review at $id", fixture.repo.review.state.value.reReview)
                                        val revision = fixture.repo.review.state.value.detail!!.revision
                                        click(L.s(R.string.stage7b_ack_revision, revision))
                                        click(L.s(R.string.stage7b_complete_re_review))
                                        waitUntil { fixture.repo.review.state.value.verified && !fixture.repo.review.state.value.reReview }
                                        capture("$id-re-review-acknowledged")
                                        for (decision in AdminDecision.entries) {
                                            runBlocking { fixture.repo.review.prepare(decision) }
                                            assertNotNull(fixture.repo.review.state.value.confirmation)
                                            capture("$id-confirm-${decision.wire}")
                                            if (decision != AdminDecision.APPROVE) {
                                                enterText("Synthetic reason: preserve evidence and request a clear citation.")
                                                capture("$id-confirm-${decision.wire}-reason-keyboard")
                                                inst.sendKeyDownUpSync(android.view.KeyEvent.KEYCODE_BACK)
                                            }
                                            fixture.repo.review.cancelConfirmation()
                                        }
                                        fixture.revision = 3
                                        runBlocking { fixture.repo.review.validate("sub_1") }
                                        assertTrue(fixture.repo.review.state.value.reReview)
                                        capture("$id-re-review-incomplete-history")
                                    }
                                    repeat(6) {
                                        tree().firstOrNull { it.isScrollable && it.actionList.any { action -> action.id == AccessibilityNodeInfo.ACTION_SCROLL_FORWARD } }
                                            ?.let { assertTrue(it.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)); awaitStableBounds() }
                                    }
                                    capture("$id-$destination-bottom")
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
                                    inst.runOnMainSync { route.value = "audit" }
                                    Thread.sleep(if(mode == "loading") 100 else 500)
                                    capture("$id-audit-$mode")
                                }
                            }
                            scenario.onActivity { it.setContent {} }
                            } catch (failure: Throwable) {
                                // Keep the failing viewport before ActivityScenario closes it.
                                try { capture("$id-FAIL-in-activity") } catch (evidenceFailure: Throwable) {
                                    failure.addSuppressed(evidenceFailure)
                                }
                                throw failure
                            }
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
            inst.runOnMainSync { L.clearOverride(); AppCompatDelegate.setApplicationLocales(oldLocales) }
        }
    }
}
