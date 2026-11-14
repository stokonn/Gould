package com.opus.piano

import android.graphics.Rect
import android.app.Activity
import androidx.compose.ui.graphics.luminance
import androidx.core.view.WindowCompat
import androidx.compose.ui.Alignment
import android.view.KeyEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextLayoutResult
import androidx.core.os.LocaleListCompat
import androidx.appcompat.app.AppCompatDelegate
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.i18n.L
import com.opus.piano.ui.*
import com.opus.piano.ui.admin.AdminSwitch
import com.opus.piano.data.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/** Same routes, localized labels and icons as the real editorial bar; no network. */
internal fun stage8NavigationItems() = listOf(
    JournalNavigationItem("OVERVIEW", L.fixed("Home"), Icons.Outlined.Home),
    JournalNavigationItem("REPERTOIRE", L.fixed("Library"), Icons.Outlined.LibraryMusic),
    JournalNavigationItem("DISCOVER", L.s(R.string.discover), Icons.Outlined.Explore),
    JournalNavigationItem("PRACTICE", L.fixed("Practice"), Icons.Outlined.Piano),
    JournalNavigationItem("HOME", L.fixed("Profile"), Icons.Outlined.Person),
)

@RunWith(AndroidJUnit4::class)
class Stage8cUiRegressionTest {
    private val inst = InstrumentationRegistry.getInstrumentation()
    @android.annotation.SuppressLint("NewApi")
    private fun nodes(): List<AccessibilityNodeInfo> {
        inst.uiAutomation.clearCache()
        fun walk(n: AccessibilityNodeInfo?): List<AccessibilityNodeInfo> =
            if(n==null) emptyList() else listOf(n)+(0 until n.childCount).flatMap { walk(n.getChild(it)) }
        return walk(inst.uiAutomation.rootInActiveWindow)
    }
    private fun waitUntil(check: () -> Boolean) {
        val until=android.os.SystemClock.uptimeMillis()+15000
        do { inst.waitForIdleSync(); if(check())return; Thread.sleep(60) } while(android.os.SystemClock.uptimeMillis()<until)
        fail("Stage8C UI condition timed out; visible nodes="+nodes().mapNotNull { node ->
            listOfNotNull(node.text?.toString(),node.contentDescription?.toString()).joinToString(" | ").takeIf(String::isNotBlank)
        }.distinct().take(80).joinToString(" ; "))
    }
    private fun shell(command:String)=inst.uiAutomation.executeShellCommand(command).use {
        android.os.ParcelFileDescriptor.AutoCloseInputStream(it).bufferedReader().readText()
    }
    private fun tap(node:AccessibilityNodeInfo) {
        var candidate:AccessibilityNodeInfo?=node
        while(candidate!=null) {
            if(candidate.isClickable && candidate.performAction(AccessibilityNodeInfo.ACTION_CLICK)) { inst.waitForIdleSync();return }
            candidate=candidate.parent
        }
        val bounds=Rect();node.getBoundsInScreen(bounds);check(!bounds.isEmpty)
        shell("input tap ${bounds.centerX()} ${bounds.centerY()}");inst.waitForIdleSync()
    }

    @Test fun profileCollectionShortcutShowsOwnershipAndApplyUpdatesEquippedState() {
        val dbName="cosmetic-ui-${java.util.UUID.randomUUID()}.db"
        var db=androidx.room.Room.databaseBuilder(inst.targetContext,OpusDatabase::class.java,dbName).build()
        var repository=ActivityRepository(db)
        var activityModel=ActivityViewModel(repository)
        val modelState=mutableStateOf(activityModel)
        val profileStore=androidx.lifecycle.ViewModelStore()
        val activityStore=androidx.lifecycle.ViewModelStore().also { it.put("activity",activityModel) }
        val owned=ActivityCosmetics.all.first()
        val locked=ActivityCosmetics.all.first { it.id!=owned.id }
        runBlocking { db.activityDao().own(CosmeticOwnership(owned.id,1L,"2026-11-09")) }
        try {
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                scenario.onActivity { activity ->
                    val profile=ProfileViewModel(activity.application as android.app.Application,androidx.lifecycle.SavedStateHandle())
                    profileStore.put("profile",profile)
                    activity.setContent {
                        var collection by remember { mutableStateOf(false) }
                        OpusTheme("Light") {
                            if(collection) CosmeticCollectionScreen(modelState.value) { collection=false }
                            else ProfileScreen(LibraryState(loading=false),emptyList(),profile,{},{},{},{},customizeCosmetics={collection=true},activity=modelState.value)
                        }
                    }
                }
                waitUntil { activityModel.state.value.snapshot!=null }
                val collectionLabel=L.s(R.string.activity_view_collection)
                waitUntil { nodes().any { it.contentDescription?.toString()==collectionLabel } }
                val profileNodes=nodes()
                fun spoken(node:AccessibilityNodeInfo)=listOfNotNull(node.text?.toString(),node.contentDescription?.toString()).joinToString(" ").lowercase()
                val edit=profileNodes.firstOrNull { spoken(it).contains("edit profile") || spoken(it).contains("upravit profil") }
                val goal=profileNodes.firstOrNull { spoken(it).contains("weekly session goal") || spoken(it).contains("týdenní cíl") }
                assertNotNull("Profile edit action is accessible: ${profileNodes.map(::spoken)}",edit)
                assertNull("Weekly Goal settings belong to Activity, not Profile",goal)
                tap(nodes().single { it.contentDescription?.toString()==collectionLabel })
                val ownedName=if(L.locale.language=="cs") owned.nameCs else owned.nameEn
                val lockedName=if(L.locale.language=="cs") locked.nameCs else locked.nameEn
                waitUntil { nodes().any { it.text?.toString()==ownedName } && nodes().any { it.text?.toString()==L.s(R.string.activity_locked) } }
                val ownedNode=nodes().single { it.text?.toString()==ownedName }
                tap(ownedNode)
                val apply=L.s(R.string.activity_apply_cosmetic)
                waitUntil { nodes().any { it.text?.toString()==apply } }
                val lockedNode=nodes().firstOrNull { it.text?.toString()==lockedName }
                assertNotNull("Locked cosmetic remains visible",lockedNode)
                tap(lockedNode!!)
                assertTrue("A locked item leaves the owned selection unchanged",nodes().any { it.text?.toString()==apply })
                tap(nodes().single { it.text?.toString()==apply })
                waitUntil { runBlocking { db.activityDao().allLoadout().any { it.cosmeticId==owned.id } } }
                waitUntil { nodes().any { it.text?.toString()==L.s(R.string.activity_equipped) } }
                assertFalse("An equipped item has no Apply action",nodes().any { it.text?.toString()==apply })
                tap(lockedNode)
                assertFalse("Locked cosmetic cannot expose Apply",nodes().any { it.text?.toString()==apply })
                assertEquals(owned.id,runBlocking { db.activityDao().allLoadout().single().cosmeticId })
                activityStore.clear();db.close()
                db=androidx.room.Room.databaseBuilder(inst.targetContext,OpusDatabase::class.java,dbName).build()
                repository=ActivityRepository(db);activityModel=ActivityViewModel(repository);activityStore.put("activity",activityModel)
                inst.runOnMainSync { modelState.value=activityModel }
                waitUntil { nodes().any { it.text?.toString()==L.s(R.string.activity_equipped) } }
                assertEquals("Apply remains persisted after database/ViewModel restart",owned.id,runBlocking { db.activityDao().allLoadout().single().cosmeticId })
            }
        } finally {
            profileStore.clear();activityStore.clear();db.close();inst.targetContext.deleteDatabase(dbName)
        }
    }

    @Test fun switchRowHasOneNameStateRoleAndExactlyOneActionPerInput() {
        for(locale in listOf("en","cs")) {
            val context=inst.targetContext.createConfigurationContext(android.content.res.Configuration().apply {
                setLocales(android.os.LocaleList.forLanguageTags(locale))
            })
            val label=context.getString(R.string.stage7b_notifications)
            val disabled=context.getString(R.string.stage7b_mobile_consent)
            val calls=AtomicInteger(); val disabledCalls=AtomicInteger()
            var expectedSwitchRole=""
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                scenario.onActivity { activity ->
                    expectedSwitchRole=activity.getString(androidx.compose.ui.R.string.switch_role)
                    activity.setContent {
                    var checked by remember { mutableStateOf(false) }
                    OpusTheme("Light") { AdminTheme { Column(Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.safeDrawing)) {
                        AdminSwitch(label,checked) { calls.incrementAndGet(); checked=it }
                        AdminSwitch(disabled,false,enabled=false) { disabledCalls.incrementAndGet() }
                    } } }
                } }
                fun control()=nodes().singleOrNull { it.contentDescription?.toString()==label && it.isCheckable }
                waitUntil { control()!=null }
                assertEquals("One spoken name, with no separate label leaf",1,nodes().count { it.text?.toString()==label || it.contentDescription?.toString()==label })
                assertEquals("Localized Switch role",expectedSwitchRole,
                    androidx.core.view.accessibility.AccessibilityNodeInfoCompat.wrap(control()!!).roleDescription?.toString())
                val bounds=Rect();control()!!.getBoundsInScreen(bounds)
                assertTrue(bounds.height()>=48*inst.targetContext.resources.displayMetrics.density)
                assertFalse(control()!!.isChecked)
                assertTrue(control()!!.performAction(AccessibilityNodeInfo.ACTION_CLICK))
                waitUntil { control()!!.isChecked };assertEquals(1,calls.get())
                assertTrue(control()!!.performAction(AccessibilityNodeInfo.ACTION_FOCUS))
                waitUntil { control()!!.isFocused }
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_SPACE)
                waitUntil { !control()!!.isChecked };assertEquals(2,calls.get())
                control()!!.getBoundsInScreen(bounds)
                var statusInset=0
                scenario.onActivity { activity ->
                    statusInset=androidx.core.view.ViewCompat.getRootWindowInsets(activity.window.decorView)
                        ?.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())?.top ?: 0
                }
                assertTrue("Pointer target must belong to app content below the status bar",bounds.centerY()>statusInset)
                val now=android.os.SystemClock.uptimeMillis()
                val down=android.view.MotionEvent.obtain(now,now,android.view.MotionEvent.ACTION_DOWN,bounds.centerX().toFloat(),bounds.centerY().toFloat(),0)
                val up=android.view.MotionEvent.obtain(now,now+30,android.view.MotionEvent.ACTION_UP,bounds.centerX().toFloat(),bounds.centerY().toFloat(),0)
                inst.sendPointerSync(down);inst.sendPointerSync(up);down.recycle();up.recycle()
                waitUntil { control()!!.isChecked };assertEquals(3,calls.get())
                val off=nodes().single { it.contentDescription?.toString()==disabled && it.isCheckable }
                assertFalse(off.isEnabled);assertFalse(off.isFocusable);assertFalse(off.isChecked)
                off.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                inst.waitForIdleSync();assertEquals(0,disabledCalls.get())
            }
        }
    }

    @Test fun responsiveLabelsFitAndTabsKeepNamesSelectionAndActionsAcross32Configurations() {
        val oldFont=shell("settings get system font_scale").trim()
        val oldLocale=AppCompatDelegate.getApplicationLocales()
        try {
            for(size in listOf("720x1280","824x1830")) {
                shell("wm size $size");shell("wm density 320")
                for(locale in listOf("en","cs")) for(theme in listOf("Light","Dark")) for(font in listOf("1.0","1.3","1.5","2.0")) {
                    shell("settings put system font_scale $font")
                    inst.runOnMainSync { AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(locale));L.refresh(locale) }
                    val layouts=ConcurrentHashMap<String,TextLayoutResult>();val calls=AtomicInteger()
                    val items=stage8NavigationItems()
                    ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                        scenario.onActivity { activity -> activity.setContent {
                            var selected by remember { mutableStateOf("OVERVIEW") }
                            OpusTheme(theme) { Column(Modifier.fillMaxSize(),verticalArrangement=Arrangement.Bottom) {
                                JournalNavigationBar(items,selected,{ selected=it;calls.incrementAndGet() },{ key,result -> layouts[key]=result })
                            } }
                        } }
                        waitUntil { layouts.size==5 && nodes().count { it.isSelected }==1 }
                        assertEquals("Requested font applied",font.toFloat(),inst.targetContext.resources.configuration.fontScale,.01f)
                        // Selected tabs intentionally expose no accessibility click action.
                        // Select each currently-unselected tab once, returning Home last.
                        (items.drop(1)+items.take(1)).forEach { item ->
                            val layout=layouts[item.route]!!
                            assertEquals("Measured font scale",font.toFloat(),layout.layoutInput.density.fontScale,.01f)
                            assertEquals("Original complete label text",item.label,layout.layoutInput.text.text)
                            assertFalse("Clipped ${item.label}: $size/$locale/$theme/$font",layout.hasVisualOverflow)
                            assertTrue(layout.lineCount>=1)
                            for(line in 0 until layout.lineCount)assertFalse(layout.isLineEllipsized(line))
                            val owner=nodes().single { it.contentDescription?.toString()==item.label }
                            assertFalse(owner.isSelected)
                            assertTrue("Tab clickable: ${item.label}",owner.isClickable);assertTrue("Tab focusable: ${item.label}",owner.isFocusable)
                            assertEquals(inst.targetContext.getString(androidx.compose.ui.R.string.tab),
                                androidx.core.view.accessibility.AccessibilityNodeInfoCompat.wrap(owner).roleDescription?.toString())
                            assertTrue(owner.performAction(AccessibilityNodeInfo.ACTION_FOCUS))
                            waitUntil { nodes().single { it.contentDescription?.toString()==item.label }.isFocused }
                            assertTrue(owner.performAction(AccessibilityNodeInfo.ACTION_CLICK))
                            waitUntil { nodes().single { it.contentDescription?.toString()==item.label }.isSelected }
                        }
                        assertEquals(5,calls.get())
                    }
                }
            }
        } finally {
            shell("settings put system font_scale $oldFont");shell("wm size reset");shell("wm density reset")
            inst.runOnMainSync { AppCompatDelegate.setApplicationLocales(oldLocale);L.refresh() }
        }
    }
    @Test fun scaffoldWindowFlagsFollowAppThemeWhenSystemThemeDiffers() {
        val old=shell("cmd uimode night").trim()
        try {
            for(system in listOf("yes","no")) for(theme in listOf("Light","Dark")) {
                shell("cmd uimode night $system")
                ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                    scenario.onActivity { activity -> activity.setContent {
                        OpusTheme(theme) { Stage8ScaffoldWindowAppearance(activity) }
                    } }
                    waitUntil {
                        var correct=false
                        scenario.onActivity { activity ->
                            val bars=WindowCompat.getInsetsController(activity.window,activity.window.decorView)
                            correct=bars.isAppearanceLightStatusBars==(theme=="Light") && bars.isAppearanceLightNavigationBars==(theme=="Light")
                        }
                        correct
                    }
                }
            }
        } finally { shell("cmd uimode night "+when { old.contains("yes")->"yes";old.contains("no")->"no";else->"auto" }) }
    }

}


@Composable internal fun Stage8ScaffoldWindowAppearance(activity: Activity) {
    val light=androidx.compose.material3.MaterialTheme.colorScheme.background.luminance()>.5f
    SideEffect { WindowCompat.getInsetsController(activity.window,activity.window.decorView).apply {
        isAppearanceLightStatusBars=light;isAppearanceLightNavigationBars=light
    } }
}
