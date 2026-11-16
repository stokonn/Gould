package com.opus.piano

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.runtime.*
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.opus.piano.data.LocalAppState
import com.opus.piano.data.TourRules
import com.opus.piano.ui.*
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GuidedTourUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun continueNavigatesTopicsAndSkipPersistsTheChoice() {
        val tour = TourViewModel(SavedStateHandle())
        var selectedRoute = Routes.OVERVIEW
        var completed = false
        var skipped = false
        compose.activity.setContent {
            OpusTheme("Light") {
                val anchors = remember { TourAnchorRegistry() }
                CompositionLocalProvider(LocalTourAnchorRegistry provides anchors) {
                Box(Modifier.fillMaxSize()) {
                    TourAnchor(if (selectedRoute == Routes.ACTIVITY) "activity" else "home") { Text(selectedRoute) }
                    GuidedTourHost(LocalAppState(), tour, anchors, false,
                        persist = { _, done, skip -> if (done == true) completed = true; if (skip == true) skipped = true },
                        navigate = { selectedRoute = it }, done = { completed = true })
                }
                }
            }
        }
        compose.onNodeWithText(compose.activity.getString(R.string.tour_topic_home)).assertIsDisplayed()
        compose.onNodeWithText(compose.activity.getString(R.string.tour_continue)).performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.tour_topic_activity)).assertIsDisplayed()
        assertEquals(Routes.ACTIVITY, selectedRoute)
        compose.onNodeWithText(compose.activity.getString(R.string.tour_skip)).performClick()
        compose.waitUntil(5_000) { completed && skipped }
    }

    @Test fun completedStepResumesAfterCompositionRecreationAndReplayStartsAtHome() {
        val tour = TourViewModel(SavedStateHandle())
        val persisted = mutableStateOf("")
        var replay by mutableStateOf(false)
        var recreation by mutableIntStateOf(0)
        compose.activity.setContent {
            OpusTheme("Light") {
                key(recreation) {
                    val anchors = remember { TourAnchorRegistry() }
                    CompositionLocalProvider(LocalTourAnchorRegistry provides anchors) {
                        Box(Modifier.fillMaxSize()) {
                            TourAnchor("home") { Text("Home anchor") }
                            GuidedTourHost(LocalAppState(tourSeenSteps = persisted.value), tour, anchors, replay,
                                persist = { seen, _, _ -> persisted.value = seen.joinToString(",") },
                                navigate = {}, done = {})
                        }
                    }
                }
            }
        }
        compose.onNodeWithText(compose.activity.getString(R.string.tour_continue)).performClick()
        compose.waitUntil(5_000) { "home" in persisted.value }
        compose.runOnIdle { recreation++ } // Recreate the host from persisted topic history.
        compose.onNodeWithText(compose.activity.getString(R.string.tour_topic_activity)).assertIsDisplayed()
        compose.runOnIdle { replay = true }
        compose.onNodeWithText(compose.activity.getString(R.string.tour_topic_home)).assertIsDisplayed()
        assertEquals("home", TourRules.stepIds.first())
    }
}
