package com.opus.piano.ui

import com.opus.piano.i18n.L
import com.opus.piano.R
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
    if (visible) Column(Modifier.fillMaxWidth().semantics { contentDescription = L.s(R.string.msg_loading_saved_content_47e0) }, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf(.55f, 1f, .8f).forEach { fraction ->
            Spacer(Modifier.fillMaxWidth(fraction).height(22.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = .07f), MaterialTheme.shapes.small))
        }
    }
}

@Composable internal fun JournalHome(state: LibraryState, name: String, practice: () -> Unit, repertoire: () -> Unit, profile: () -> Unit, wrapped: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        item { Heading(L.s(R.string.msg_your_practice_journal_d0b0), L.s(R.string.msg_welcome_name_5c46, (name)), L.s(R.string.msg_a_little_time_at_the_piano_a_lasting_musical_9009)) }
        if (state.loading) item { EditorialSkeleton() }
        else {
            item {
                Text(L.s(R.string.msg_time_well_spent_a467), style = MaterialTheme.typography.labelSmall)
                Text(localizedPracticeTime(state.summary.totalMillis), style = MaterialTheme.typography.displayMedium.copy(fontFamily = MaterialTheme.typography.bodyLarge.fontFamily), color = MaterialTheme.colorScheme.primary)
                Text(L.s(R.string.msg_state_sessions_count_com_opus_piano_data_ach_7ea4, (state.sessions.count(com.opus.piano.data.AchievementEngine::valid)), (state.pieces.size)), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(16.dp)); HorizontalDivider()
            }
            item {
                Button(onClick = practice, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text(L.s(R.string.msg_begin_practice_c5a7)) }
                TextButton(onClick = repertoire) { Text(L.s(R.string.msg_open_your_repertoire_a1aa)) }
            }
            item {
                SectionTitle(L.s(R.string.msg_the_latest_entry_4499))
                Spacer(Modifier.height(12.dp))
                state.sessions.firstOrNull()?.let { SessionRow(it, state.summary.partsBySession[it.id].orEmpty()) }
                    ?: Text(L.s(R.string.msg_your_first_completed_session_will_begin_the_bbf1), style = MaterialTheme.typography.bodyMedium)
            }
            item {
                HorizontalDivider(); Spacer(Modifier.height(12.dp))
                TextButton(onClick = profile) { Text(L.s(R.string.msg_your_profile_achievements_b225)) }
                TextButton(onClick = wrapped) { Text(L.s(R.string.msg_wrapped_preview_fbae)) }
            }
        }
    }
}
