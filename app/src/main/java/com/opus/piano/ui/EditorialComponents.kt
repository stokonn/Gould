package com.opus.piano.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.opus.piano.data.compactPracticeTime

/** Delay prevents flashing a placeholder for immediately available local content. No animation. */
@Composable internal fun EditorialSkeleton() {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { kotlinx.coroutines.delay(180); visible = true }
    if (visible) Column(Modifier.fillMaxWidth().semantics { contentDescription = "Loading saved content" }, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf(.55f, 1f, .8f).forEach { fraction ->
            Spacer(Modifier.fillMaxWidth(fraction).height(22.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = .07f), MaterialTheme.shapes.small))
        }
    }
}

@Composable internal fun JournalHome(state: LibraryState, name: String, practice: () -> Unit, repertoire: () -> Unit, profile: () -> Unit, wrapped: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        item { Heading("YOUR PRACTICE JOURNAL", "Welcome, $name", "A little time at the piano. A lasting musical life.") }
        if (state.loading) item { EditorialSkeleton() }
        else {
            item {
                Text("TIME WELL SPENT", style = MaterialTheme.typography.labelSmall)
                Text(compactPracticeTime(state.summary.totalMillis), style = MaterialTheme.typography.displayMedium.copy(fontFamily = MaterialTheme.typography.bodyLarge.fontFamily), color = MaterialTheme.colorScheme.primary)
                Text("${state.sessions.count(com.opus.piano.data.AchievementEngine::valid)} sessions · ${state.pieces.size} works in your repertoire", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(16.dp)); HorizontalDivider()
            }
            item {
                Button(onClick = practice, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Begin practice") }
                TextButton(onClick = repertoire) { Text("Open your repertoire") }
            }
            item {
                SectionTitle("The latest entry")
                Spacer(Modifier.height(12.dp))
                state.sessions.firstOrNull()?.let { SessionRow(it, state.summary.partsBySession[it.id].orEmpty()) }
                    ?: Text("Your first completed session will begin the story.", style = MaterialTheme.typography.bodyMedium)
            }
            item {
                HorizontalDivider(); Spacer(Modifier.height(12.dp))
                TextButton(onClick = profile) { Text("Your profile & achievements") }
                TextButton(onClick = wrapped) { Text("Wrapped · preview") }
            }
        }
    }
}
