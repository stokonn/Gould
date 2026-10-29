package com.opus.piano.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.opus.piano.R
import com.opus.piano.audio.MetronomeController
import com.opus.piano.i18n.L

@Composable internal fun StudioMetronome(controller: MetronomeController, expanded: Boolean, toggle: () -> Unit) {
    val state by controller.state.collectAsStateWithLifecycle()
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner, controller) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) controller.stop() }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer); controller.stop() }
    }
    JournalTextButton(onClick = toggle) { Text(L.s(R.string.studio_metronome)) }
    if (expanded) JournalCard {
        Column(verticalArrangement = Arrangement.spacedBy(JournalSpace.small)) {
            Text(L.s(R.string.studio_tempo,state.bpm))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { controller.configure(state.bpm-5,state.beats) }, enabled = state.bpm > 40) {
                    Icon(Icons.Outlined.Remove,L.s(R.string.studio_less_tempo))
                }
                Slider(value = state.bpm.toFloat(), onValueChange = { controller.configure(it.toInt(),state.beats) },
                    valueRange = 40f..240f, modifier = Modifier.weight(1f).semantics { contentDescription = L.s(R.string.studio_tempo,state.bpm) })
                IconButton(onClick = { controller.configure(state.bpm+5,state.beats) }, enabled = state.bpm < 240) {
                    Icon(Icons.Outlined.Add,L.s(R.string.studio_more_tempo))
                }
            }
            Text(L.s(R.string.studio_beats,state.beats))
            Row {
                IconButton(onClick = { controller.configure(state.bpm,state.beats-1) }, enabled = state.beats > 1) {
                    Icon(Icons.Outlined.Remove,L.s(R.string.studio_less_beats))
                }
                IconButton(onClick = { controller.configure(state.bpm,state.beats+1) }, enabled = state.beats < 12) {
                    Icon(Icons.Outlined.Add,L.s(R.string.studio_more_beats))
                }
            }
            JournalSecondaryButton(onClick = { if (state.playing || state.starting) controller.stop() else controller.start() }) {
                Text(L.s(if (state.playing || state.starting) R.string.studio_stop_clicks else R.string.studio_start_clicks))
            }
            if (state.failed) Text(L.s(R.string.studio_clicks_error),color = MaterialTheme.colorScheme.error)
        }
    }
}
