package com.opus.piano

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.app.UiAutomation
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.luminance
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.runner.AndroidJUnitRunner
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.*
import androidx.work.testing.TestListenableWorkerBuilder
import com.opus.piano.admin.*
import com.opus.piano.ui.*
import com.opus.piano.ui.admin.AdminViewModel
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** Disposable-emulator-only, explicitly selected instrumentation component.
 * Uses the real OpusApp/NavHost with an in-memory Admin transport. No production
 * transport/decision enablement. No @Test methods or changes to the CI suite.
 * Host commands and TalkBack perform interactions; this probe only records them.
 */
class Stage8bAssessmentProbe : AndroidJUnitRunner() {
    private val app get() = targetContext.applicationContext as OpusApplication
    private val directory get() = File(app.filesDir, "stage7b-manual").apply { mkdirs() }
    private val eventLock = Any()
    private var scenario: ActivityScenario<MainActivity>? = null
    private var manualProfile: ProfileViewModel? = null
    private val automation get() = getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES)

    override fun onStart() {
        val phase = InstrumentationRegistry.getArguments().getString("goldbergManual")
        try {
            require(phase in listOf("interactive", "revoked")) { "Explicit goldbergManual phase required" }
            require(android.os.Build.FINGERPRINT.contains("generic") || android.os.Build.FINGERPRINT.contains("emu"))
            automation.setOnAccessibilityEventListener { event ->
                if (event.eventType in listOf(AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED,
                        AccessibilityEvent.TYPE_VIEW_FOCUSED, AccessibilityEvent.TYPE_VIEW_CLICKED,
                        AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED, AccessibilityEvent.TYPE_ANNOUNCEMENT,
                        AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START,
                        AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END)) {
                    val source = event.source
                    val item = JSONObject().put("uptime", android.os.SystemClock.uptimeMillis())
                        .put("type", AccessibilityEvent.eventTypeToString(event.eventType))
                        .put("package", event.packageName?.toString()).put("text", event.text.toString())
                        .put("description", event.contentDescription?.toString())
                        .put("sourceText", source?.text?.toString())
                        .put("sourceDescription", source?.contentDescription?.toString())
                    synchronized(eventLock) { File(directory,"events.jsonl").appendText("$item\n") }
                }
            }
            if (phase == "revoked") {
                check(ContextCompat.checkSelfPermission(app, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED)
                check(app.getSystemService(NotificationManager::class.java).activeNotifications.none { it.id == AdminSyncWorker.NOTIFICATION_ID })
                workerPermissionProbe(false)
                snapshot("after-revocation-worker")
            } else {
                val expectedPermission = InstrumentationRegistry.getArguments().getString("goldbergExpectedPermission") ?: "granted"
                require(expectedPermission in listOf("granted", "denied"))
                // Offline assessment intentionally excludes the unrelated worker connectivity preflight.
                val requestedTheme = InstrumentationRegistry.getArguments().getString("goldbergTheme")
                require(requestedTheme == null || requestedTheme in listOf("Light", "Dark"))
                val requestedLocale = InstrumentationRegistry.getArguments().getString("goldbergLocale") ?: "en"
                require(requestedLocale in listOf("en", "cs"))
                runOnMainSync { androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(androidx.core.os.LocaleListCompat.forLanguageTags(requestedLocale)); com.opus.piano.i18n.L.refresh(requestedLocale) }
                scenario = ActivityScenario.launch(MainActivity::class.java)
                scenario!!.onActivity { activity ->
                    val profile = ProfileViewModel(app, androidx.lifecycle.SavedStateHandle())
                    manualProfile = profile
                    profile.finishOnboarding() // Only this new disposable emulator's synthetic local profile.
                    val model = OpusViewModel(app.repository, SavedStateHandle(), app.practice, app.recording, app)
                    val catalog = CatalogViewModel(app.catalogRepository, SavedStateHandle())
                    val experience = ExperienceViewModel(app.achievements, app.recordings)
                    val admin = AdminViewModel(app, repository())
                    for ((key,value) in listOf("profile" to profile,"model" to model,"catalog" to catalog,"experience" to experience,"admin" to admin)) {
                        activity.viewModelStore.put("manual-$key",value)
                    }
                    activity.setContent {
                        val settings by profile.settings.collectAsStateWithLifecycle()
                        val appearance by app.appearance.state.collectAsStateWithLifecycle()
                        OpusTheme(settings.theme, settings.typography, appearance) {
                            val scaffold = InstrumentationRegistry.getArguments().getString("goldbergScaffold") == "true"
                            Stage8ScaffoldWindowAppearance(activity)
                            if (scaffold) {
                                androidx.compose.material3.Scaffold(bottomBar = {
                                    JournalNavigationBar(stage8NavigationItems(), "HOME", {})
                                }) { padding ->
                                    androidx.compose.foundation.layout.Box(androidx.compose.ui.Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).windowInsetsPadding(androidx.compose.foundation.layout.WindowInsets.safeDrawing.only(androidx.compose.foundation.layout.WindowInsetsSides.Top + androidx.compose.foundation.layout.WindowInsetsSides.Horizontal))) {
                                        AdminTheme { com.opus.piano.ui.admin.AdminSettingsScreen(admin) {} }
                                    }
                                }
                            } else {
                            OpusApp(model,catalog,experience,profile,admin)
                            }
                        }
                    }
                }
                if (requestedTheme != null) {
                    // Profile actions intentionally reject concurrent edits. Finish
                    // disposable onboarding before requesting its theme preference.
                    val until = android.os.SystemClock.uptimeMillis()+10000
                    while (manualProfile!!.busy.value && android.os.SystemClock.uptimeMillis()<until) Thread.sleep(20)
                    check(!manualProfile!!.busy.value)
                    runOnMainSync { manualProfile!!.theme(requestedTheme) }
                    while (manualProfile!!.settings.value.theme != requestedTheme && android.os.SystemClock.uptimeMillis()<until) Thread.sleep(20)
                    check(manualProfile!!.settings.value.theme == requestedTheme)
                }
                Thread.sleep(1500)
                snapshot("ready")
                File(directory,"ready.txt").writeText("pid=${Process.myPid()}\n")
                val deadline = android.os.SystemClock.uptimeMillis()+20*60*1000
                while (android.os.SystemClock.uptimeMillis()<deadline) {
                    val requests=app.filesDir.listFiles().orEmpty().filter { it.name.startsWith("manual-capture-") }
                    for(request in requests) {
                        val name=request.name.removePrefix("manual-capture-")
                        require(name.matches(Regex("[A-Za-z0-9_-]+")))
                        snapshot(name);request.delete()
                    }
                    if(File(app.filesDir,"manual-stop").exists()) break
                    Thread.sleep(100)
                }
            }
            scenario?.close()
            finish(Activity.RESULT_OK, Bundle().apply { putString("stream","Manual probe finished: $phase; inspect retained evidence, no JUnit pass implied.\n") })
        } catch(failure:Throwable) {
            File(directory,"probe-failure.txt").writeText(failure.stackTraceToString())
            finish(Activity.RESULT_CANCELED,Bundle().apply {putString("stream",failure.stackTraceToString())})
        }
    }

    private fun repository(): AdminRepository {
        val api=AdminApi({"https://synthetic-manual.invalid"},AdminTransport { _,url,_,_ ->
            val body=when {
                url.contains("capabilities") -> """{"principalId":"synthetic-moderator","capabilities":["queue.read","audit.read","decide"]}"""
                url.contains("/submissions/") -> """{"id":"sub_1","owner":"synthetic-author","status":"submitted","revision":1,"revisions":[{"revision":1,"createdAt":1700000000000,"payload":{"title":"Synthetic manual proposal","composer":"Johann Sebastian Bach","fields":[{"field":"title","newValue":"Synthetic manual proposal","evidence":{"url":"https://source.invalid/fixture","license":"CC0 synthetic fixture"}}]}}],"reviewEvents":[]}"""
                url.contains("/queue") -> """{"items":[{"id":"sub_1","owner":"synthetic-author","status":"submitted","revision":1,"evidenceCount":1,"createdAt":1700000000000}],"nextCursor":null}"""
                else -> """{"items":[{"seq":1,"ts":1700000000000,"actor":"synthetic-moderator","action":"decision.reject","subject":"sub_1","prevHash":"synthetic","thisHash":"synthetic"}],"nextCursor":null,"chainHead":"synthetic"}"""
            }
            AdminHttpResponse(200,body.toByteArray(),null)
        },{"synthetic-not-a-credential"})
        return AdminRepository(api,InMemoryAdminSessionStore(),AdminTrustPolicy(),
            AdminMarkers(InMemoryAdminMarkerStore()){"synthetic-manual"},AdminStatsService(api),decisionsEnabled=true)
    }

    private fun workerPermissionProbe(granted:Boolean)= runBlocking {
        val endpoint=app.adminSettings.endpoint;val policy=app.adminSettings.syncPolicy;val token=app.adminSession.readToken()
        val notices=app.getSystemService(NotificationManager::class.java)
        var revision=1;var calls=0
        try {
            check((ContextCompat.checkSelfPermission(app,Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED)==granted)
            app.adminSettings.endpoint="https://synthetic-manual-permission.invalid"
            app.adminSettings.syncPolicy=AdminSyncPolicy(true,true,true);app.adminSession.writeToken("synthetic-not-a-credential")
            val api=AdminApi({app.adminSettings.endpoint},AdminTransport { _,url,_,_ ->
                calls++
                AdminHttpResponse(200,(if(url.contains("capabilities"))
                    """{"principalId":"synthetic-permission-moderator","capabilities":["queue.read"]}"""
                    else """{"items":[{"id":"synthetic-permission","owner":"synthetic-author","status":"submitted","revision":$revision,"createdAt":1}],"nextCursor":null}""").toByteArray(),null)
            },{"synthetic-not-a-credential"})
            val repo=AdminRepository(api,InMemoryAdminSessionStore(),AdminTrustPolicy(),AdminMarkers(InMemoryAdminMarkerStore()){"synthetic-manual-permission"},AdminStatsService(api))
            val scope=AdminSyncWorker.scope(app)
            val key=adminFingerprint("$scope:synthetic-permission-moderator")
            suspend fun execute()=TestListenableWorkerBuilder<AdminSyncWorker>(app).setInputData(workDataOf("scope" to scope))
                .setWorkerFactory(object:WorkerFactory(){override fun createWorker(context:Context,name:String,parameters:WorkerParameters):ListenableWorker=AdminSyncWorker(context,parameters,repo)})
                .build().doWork()
            execute();val before=app.adminSettings.syncSignature(key);check(before!=null)
            revision=2;execute();check(app.adminSettings.syncSignature(key)!=before);check(calls>0)
            val until=android.os.SystemClock.uptimeMillis()+5000
            if(granted) while(notices.activeNotifications.none {it.id==AdminSyncWorker.NOTIFICATION_ID} && android.os.SystemClock.uptimeMillis()<until) Thread.sleep(50)
            val posted=notices.activeNotifications.any {it.id==AdminSyncWorker.NOTIFICATION_ID};check(posted==granted)
            File(directory,if(granted)"worker-granted.json" else "worker-revoked.json").writeText(
                JSONObject().put("pid",Process.myPid()).put("granted",granted).put("fixtureCalls",calls).put("signatureChanged",true).put("notificationPosted",posted).toString(2))
        } finally {
            app.adminSettings.endpoint=endpoint;app.adminSettings.syncPolicy=policy
            if(token==null)app.adminSession.clear() else app.adminSession.writeToken(token)
        }
    }

    @android.annotation.SuppressLint("NewApi") // Explicit API36 emulator venue.
    private fun snapshot(name:String) {
        val ui=automation;ui.clearCache();val nodes=JSONArray()
        fun walk(node:AccessibilityNodeInfo?,parent:Int) {
            if(node==null)return
            val index=nodes.length();val rect=Rect();node.getBoundsInScreen(rect)
            nodes.put(JSONObject().put("index",index).put("parent",parent).put("text",node.text?.toString())
                .put("description",node.contentDescription?.toString()).put("bounds",rect.flattenToString())
                .put("roleDescription",androidx.core.view.accessibility.AccessibilityNodeInfoCompat.wrap(node).roleDescription?.toString())
                .put("class",node.className?.toString()).put("visible",node.isVisibleToUser)
                .put("clickable",node.isClickable).put("enabled",node.isEnabled).put("editable",node.isEditable)
                .put("selected",node.isSelected)
                .put("focusable",node.isFocusable).put("focused",node.isFocused).put("accessibilityFocused",node.isAccessibilityFocused)
                .put("checkable",node.isCheckable).put("checked",node.isChecked)
                .put("actions",JSONArray(node.actionList.map {it.id})))
            for(i in 0 until node.childCount)walk(node.getChild(i),index)
        }
        walk(ui.rootInActiveWindow,-1)
        val state=JSONObject().put("pid",Process.myPid()).put("uptime",android.os.SystemClock.uptimeMillis())
            .put("locale",app.resources.configuration.locales[0].toLanguageTag())
            .put("fontScale",app.resources.configuration.fontScale)
            .put("densityDpi",app.resources.displayMetrics.densityDpi)
            .put("screenWidthDp",app.resources.configuration.screenWidthDp)
            .put("screenHeightDp",app.resources.configuration.screenHeightDp)
            .put("profileTheme",manualProfile?.settings?.value?.theme)
            .put("nodes",nodes).put("services",Settings.Secure.getString(app.contentResolver,"enabled_accessibility_services"))
            .put("touchExploration",Settings.Secure.getInt(app.contentResolver,"touch_exploration_enabled",0))
            .put("animatorScale",Settings.Global.getFloat(app.contentResolver,"animator_duration_scale",1f))
            .put("transitionScale",Settings.Global.getFloat(app.contentResolver,"transition_animation_scale",1f))
            .put("windowScale",Settings.Global.getFloat(app.contentResolver,"window_animation_scale",1f))
            .put("notificationGranted",ContextCompat.checkSelfPermission(app,Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED)
            .put("activeNotificationIds",JSONArray(app.getSystemService(NotificationManager::class.java).activeNotifications.map {it.id}))
        scenario?.onActivity { activity ->
            val root=activity.window.decorView.rootWindowInsets
            if(root!=null) {
                val insets=WindowInsetsCompat.toWindowInsetsCompat(root)
                val bars=WindowCompat.getInsetsController(activity.window,activity.window.decorView)
                state.put("lightStatusBars",bars.isAppearanceLightStatusBars).put("lightNavigationBars",bars.isAppearanceLightNavigationBars)
                state.put("systemNight",app.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                state.put("scaffold",InstrumentationRegistry.getArguments().getString("goldbergScaffold")=="true")
                state.put("imeVisible",insets.isVisible(WindowInsetsCompat.Type.ime()))
                state.put("imeBottom",insets.getInsets(WindowInsetsCompat.Type.ime()).bottom)
            }
        }
        File(directory,"$name.json").writeText(state.toString(2))
        ui.takeScreenshot()?.let {bitmap ->File(directory,"$name.png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()}
    }
}
