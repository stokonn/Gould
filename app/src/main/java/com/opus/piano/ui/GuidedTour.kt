package com.opus.piano.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.opus.piano.R
import com.opus.piano.data.LocalAppState
import com.opus.piano.i18n.L
import kotlinx.coroutines.launch

internal class TourAnchorRegistry {
    var bounds by mutableStateOf<Map<String, Rect>>(emptyMap())
        private set
    var overlayOrigin by mutableStateOf(Offset.Zero)
        private set
    fun update(id: String, rect: Rect) { if (bounds[id] != rect) bounds = bounds + (id to rect) }
    fun updateOverlayOrigin(origin: Offset) { if (overlayOrigin != origin) overlayOrigin = origin }
}

@Composable
internal fun TourAnchor(id: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val registry = LocalTourAnchorRegistry.current
    Box(modifier.onGloballyPositioned { registry.update(id, it.boundsInRoot()) }) { content() }
}

internal val LocalTourAnchorRegistry = androidx.compose.runtime.staticCompositionLocalOf { TourAnchorRegistry() }

@Composable
internal fun GuidedTourHost(
    state: LocalAppState,
    tour: TourViewModel,
    registry: TourAnchorRegistry,
    replay: Boolean,
    persist: suspend (Set<String>, Boolean?, Boolean?) -> Unit,
    navigate: (String) -> Unit,
    done: () -> Unit,
) {
    val ids = TourProgress.stepIds
    val seenFromStore = state.tourSeenSteps.split(',').filter(String::isNotEmpty).toSet()
    val seen = rememberSaveable(state.id) { mutableStateOf(seenFromStore) }
    val index by tour.index.collectAsState()
    val scope = rememberCoroutineScope()
    val steps = listOf(
        TourStep("home", Routes.OVERVIEW, R.string.tour_topic_home, R.string.tour_body_home),
        TourStep("activity", Routes.ACTIVITY, R.string.tour_topic_activity, R.string.tour_body_activity),
        TourStep("streaks", Routes.ACTIVITY, R.string.tour_topic_streaks, R.string.tour_body_streaks),
        TourStep("profile", Routes.PROFILE, R.string.tour_topic_profile, R.string.tour_body_profile),
        TourStep("collection", Routes.BADGES, R.string.tour_topic_collection, R.string.tour_body_collection),
        TourStep("settings", Routes.SETTINGS, R.string.tour_topic_settings, R.string.tour_body_settings),
    )
    LaunchedEffect(state.id, replay) { tour.resume(replay, seenFromStore) }
    val step = steps[index.coerceIn(0, steps.lastIndex)]
    LaunchedEffect(state.tourSeenSteps, replay) { if (!replay) seen.value = seenFromStore }
    LaunchedEffect(step.id) { persist(seen.value, null, null) }
    BackHandler { scope.launch { persist(seen.value, false, true); done() } }

    Box(Modifier.fillMaxSize().zIndex(10f).onGloballyPositioned { registry.updateOverlayOrigin(it.positionInRoot()) }) {
        Canvas(Modifier.fillMaxSize()) { drawRect(Color.Black.copy(alpha = .46f)) }
        registry.bounds[step.id]?.let { rect ->
            val density = LocalDensity.current
            Box(Modifier.offset(with(density) { (rect.left - registry.overlayOrigin.x).toDp() }, with(density) { (rect.top - registry.overlayOrigin.y).toDp() })
                .size(with(density) { rect.width.toDp() }, with(density) { rect.height.toDp() })
                .border(BorderStroke(3.dp, MaterialTheme.colorScheme.primary), RoundedCornerShape(20.dp)))
        }
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().widthIn(max = 600.dp)
                .padding(16.dp).semantics { liveRegion = LiveRegionMode.Polite },
            shape = RoundedCornerShape(24.dp), tonalElevation = 8.dp,
        ) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(L.s(R.string.tour_heading), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text(L.s(step.title), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
                Text(L.s(step.description), style = MaterialTheme.typography.bodyLarge)
                Text(L.s(R.string.tour_step_of, index + 1, steps.size), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val actions: @Composable () -> Unit = {
                    JournalTextButton(onClick = { scope.launch { persist(seen.value, false, true); done() } }) {
                        Text(L.s(R.string.tour_skip))
                    }
                    JournalPrimaryButton(onClick = {
                        scope.launch {
                            val updated = TourProgress.completedThrough(index, seen.value)
                            seen.value = updated
                            val last = index == steps.lastIndex
                            persist(updated, last, false)
                            if (last) done() else {
                                tour.continueTo(index + 1)
                                navigate(steps[index + 1].route)
                            }
                        }
                    }) { Text(L.s(if (index == steps.lastIndex) R.string.tour_finish else R.string.tour_continue)) }
                    }
                    if (maxWidth < 380.dp || LocalDensity.current.fontScale > 1.3f) {
                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            actions()
                        }
                    } else {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { actions() }
                    }
                }
            }
        }
    }
}

private data class TourStep(val id: String, val route: String, val title: Int, val description: Int)
