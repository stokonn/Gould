package com.opus.piano.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.opus.piano.audio.HarpsichordAudioEngine

@OptIn(ExperimentalLayoutApi::class)
@Composable internal fun HarpsichordScreen(close: () -> Unit) {
    val context = LocalContext.current
    val audio = remember { HarpsichordAudioEngine(context) }
    val notice by audio.notice.collectAsState()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(audio,lifecycle) {
        val observer = LifecycleEventObserver { _,event -> if (event == Lifecycle.Event.ON_STOP) { audio.close(); close() } }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer); audio.close() }
    }
    val names = listOf("C","C sharp","D","D sharp","E","F","F sharp","G","G sharp","A","A sharp","B","High C")
    Dialog(onDismissRequest = close) { Surface(shape = MaterialTheme.shapes.medium) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Heading("BACH’S SECRET","A curious prelude","A little harpsichord, hidden in your journal.")
            Text("The Curious Pianist · play a note")
            notice?.let { Text(it) }
            // Two rows retain playable touch targets on narrow screens and with large fonts.
            listOf(listOf(1,3,6,8,10),listOf(0,2,4,5,7,9,11,12)).forEach { keys ->
                androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    keys.forEach { note ->
                        Button(onClick = { audio.note(note) },modifier = Modifier.widthIn(min=48.dp).heightIn(min=72.dp),colors = if (note in listOf(1,3,6,8,10)) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.inverseSurface,contentColor=MaterialTheme.colorScheme.inverseOnSurface) else ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant,contentColor=MaterialTheme.colorScheme.onSurfaceVariant)) { Text(names[note]) }
                    }
                }
            }
            TextButton(onClick = close) { Text("Close harpsichord") }
        }
    } }
}
