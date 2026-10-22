package com.opus.piano

import androidx.activity.compose.setContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.ui.DiscoverScreen
import com.opus.piano.ui.DiscoverSearchSession
import com.opus.piano.ui.LibraryState
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class DiscoverNavigationTest {
    @Test fun realDiscoverEntryRetainsTransientQueryAcrossDetailAndBack() {
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        lateinit var nav:NavHostController
        lateinit var session:DiscoverSearchSession
        val ready=CountDownLatch(1)
        ActivityScenario.launch(MainActivity::class.java).use {scenario->
            scenario.onActivity {activity->activity.setContent {
                val controller=rememberNavController()
                SideEffect {nav=controller}
                NavHost(controller,startDestination="DISCOVER",enterTransition={EnterTransition.None},exitTransition={ExitTransition.None}) {
                    composable("DISCOVER") {
                        val state:DiscoverSearchSession=viewModel()
                        SideEffect {session=state;ready.countDown()}
                        DiscoverScreen({controller.navigate("detail")},{},{}, LibraryState(loading = false))
                    }
                    composable("detail"){Text("Authored local detail")}
                }
            }}
            assertTrue(ready.await(10,TimeUnit.SECONDS))
            fun settled(route:String) {
                val deadline=android.os.SystemClock.uptimeMillis()+10000
                var complete=false
                while(!complete && android.os.SystemClock.uptimeMillis()<deadline) {
                    instrumentation.waitForIdleSync()
                    instrumentation.runOnMainSync {complete=nav.currentDestination?.route==route && nav.currentBackStackEntry?.lifecycle?.currentState==androidx.lifecycle.Lifecycle.State.RESUMED && nav.visibleEntries.value.size==1}
                    if(!complete)Thread.sleep(16)
                }
                assertTrue("Unsettled navigation $route",complete)
            }
            settled("DISCOVER")
            val original=session
            instrumentation.runOnMainSync {session.query="Authored transient query";nav.navigate("detail")}
            settled("detail")
            instrumentation.runOnMainSync {assertTrue(nav.popBackStack())}
            settled("DISCOVER")
            // Stage 7A refinement: the explicit online request moved out of the session into
            // composable-local remember state, so it can never survive navigation or process
            // death by construction; only the raw query remains transient memory state here.
            instrumentation.runOnMainSync {assertSame(original,session);assertEquals("Authored transient query",session.query)}
            assertEquals("",DiscoverSearchSession().query)
        }
    }
}
