package com.opus.piano

import androidx.activity.compose.setContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.ui.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Uses the real Compose navigator and production tab-switch helper; never edits personal data. */
@RunWith(AndroidJUnit4::class)
class NavigationRegressionTest {
    @Test fun searchProfileSettingsAndRepeatedTabSwitchesKeepBackStackCorrect() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        lateinit var nav: NavHostController
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity -> activity.setContent {
                val controller = rememberNavController()
                SideEffect { nav = controller }
                NavHost(controller, startDestination = "HOME", enterTransition = { EnterTransition.None }, exitTransition = { ExitTransition.None }) {
                    listOf("HOME", "REPERTOIRE", "PRACTICE", "BADGES", "WRAPPED", "catalog", "settings").forEach { route -> composable(route) { Text(route) } }
                }
            } }
            instrumentation.waitForIdleSync()
            fun step(route: String, action: () -> Unit) {
                instrumentation.runOnMainSync { action() }
                instrumentation.waitForIdleSync()
                instrumentation.runOnMainSync { assertEquals(route, nav.currentDestination?.route) }
            }
            repeat(3) {
                step("catalog") { nav.navigate("catalog") }
                step("REPERTOIRE") { nav.selectTopLevel("REPERTOIRE") }
                step("catalog") { nav.navigate("catalog") }
                step("REPERTOIRE") { nav.selectTopLevel("REPERTOIRE") }
                step("HOME") { nav.selectTopLevel("HOME") }
                step("catalog") { nav.navigate("catalog") }
                step("HOME") { nav.selectTopLevel("HOME") }
                step("settings") { nav.navigate("settings") }
                step("HOME") { assertTrue(nav.popBackStack()) }
                step("PRACTICE") { nav.selectTopLevel("PRACTICE") }
                step("BADGES") { nav.selectTopLevel("BADGES") }
                step("HOME") { nav.selectTopLevel("HOME") }
            }
            instrumentation.runOnMainSync { assertNull(nav.previousBackStackEntry) }
        }
    }
}
