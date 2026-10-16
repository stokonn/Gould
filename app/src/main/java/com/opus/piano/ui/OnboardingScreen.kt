package com.opus.piano.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable internal fun OnboardingScreen(done: () -> Unit) {
    var page by remember { mutableIntStateOf(0) }
    val pages = listOf(
        "Your repertoire" to "Discover a classical work or add a piece manually. Select it in Practice; Goldberg does not identify pieces from sound.",
        "Your time at the piano" to "Use the manual timer or optional experimental Smart Practice. Smart Practice analyzes ambient sound locally and can continue while your screen is locked. No audio is stored or uploaded. Microphone permission is requested only when you enable listening.",
        "Your musical journal" to "Review sessions, edit practice notes and explore Profile statistics. Enable optional reminders in Settings. Export a backup to preserve your journal; CSV exports are for spreadsheets, not restoration."
    )
    Dialog(onDismissRequest = done) { Surface(shape = MaterialTheme.shapes.medium) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(24.dp),verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text("WELCOME TO GOLDBERG · ${page+1}/3",style = MaterialTheme.typography.labelSmall)
            Heading("A LITTLE PRACTICE",pages[page].first,pages[page].second)
            Button(onClick = { if (page==2) done() else page++ }) { Text(if (page==2) "Begin" else "Continue") }
            TextButton(onClick = done) { Text("Skip") }
        }
    } }
}
