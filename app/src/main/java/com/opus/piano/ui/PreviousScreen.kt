package com.opus.piano.ui

import com.opus.piano.i18n.L
import com.opus.piano.R
import android.media.MediaPlayer
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.opus.piano.OpusApplication
import com.opus.piano.data.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/** Previous: Practice History + Recordings tabs. Library-associated route; tab/scroll/Back state retained. */
@Composable internal fun PreviousScreen(back: () -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    BackHandler(onBack = back)
    Column(Modifier.fillMaxSize()) {
        CatalogBack(L.fixed("Previous"), back)
        ScrollableTabRow(tab, containerColor = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.primary, edgePadding = JournalSpace.small) {
            Tab(tab == 0, onClick = { tab = 0 }, text = { Text(L.s(R.string.msg_practice_history_76e7)) })
            Tab(tab == 1, onClick = { tab = 1 }, text = { Text(L.s(R.string.msg_recordings)) })
        }
        when (tab) {
            0 -> PracticeHistoryTab()
            1 -> RecordingsTab()
        }
    }
}

/** Practice History tab: bounded keyset paging, same data as the standalone history route. */
@Composable private fun PracticeHistoryTab() {
    val dao = (LocalContext.current.applicationContext as OpusApplication).database.practiceDao()
    var sessions by remember { mutableStateOf<List<PracticeSession>>(emptyList()) }
    var parts by remember { mutableStateOf<Map<Long, List<SessionPart>>>(emptyMap()) }
    var request by remember { mutableIntStateOf(0) }
    var more by remember { mutableStateOf(true) }
    var loading by remember { mutableStateOf(true) }
    var failed by remember { mutableStateOf(false) }
    LaunchedEffect(request) {
        loading = true; failed = false
        try {
            val cursor = sessions.lastOrNull()
            val page = dao.historyPage(null, null, cursor?.completedAt ?: Long.MAX_VALUE, cursor?.id ?: Long.MAX_VALUE, 31)
            val batch = page.take(30)
            val rows = if (batch.isEmpty()) emptyMap() else dao.historyParts(batch.map { it.id }).groupBy { it.sessionId }
            sessions = (sessions + batch).distinctBy { it.id }; parts = parts + rows; more = page.size > 30
        } catch (c: CancellationException) { throw c }
        catch (_: Exception) { failed = true }
        finally { loading = false }
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(JournalSpace.page), verticalArrangement = Arrangement.spacedBy(JournalSpace.small)) {
        if (!loading && !failed && sessions.isEmpty()) item { Text(L.s(R.string.msg_no_sessions_yet_your_practice_will_appear_he_37e7)) }
        items(sessions, key = { it.id }) { s ->
            SessionRow(s, parts[s.id].orEmpty(), s.durationMillis)
        }
        if (loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        if (failed) item { Text(L.s(R.string.msg_saved_journal_could_not_be_loaded_restart_go_cfce)); JournalTextButton(onClick = { request++ }) { Text(L.s(R.string.practice_retry)) } }
        else if (more && !loading) item { JournalTextButton(onClick = { request++ }) { Text(L.s(R.string.msg_show_older_sessions_97d8)) } }
    }
}

/** Recordings tab: bounded newest-first paging, real durations, play/pause/export/delete. */
@Composable private fun RecordingsTab() {
    val app = LocalContext.current.applicationContext as OpusApplication
    val repository = remember { com.opus.piano.data.LocalRecordingRepository(app.database, java.io.File(app.filesDir, "recordings")) }
    val recordings by repository.recordings.collectAsState(initial = emptyList())
    val visible = recordings.filter { it.state == "Saved" || it.state == "Recoverable" }
    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    var playingId by remember { mutableStateOf<String?>(null) }
    var deleting by remember { mutableStateOf<LocalAudio?>(null) }
    var exportItem by remember { mutableStateOf<LocalAudio?>(null) }
    var playError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("audio/wav")) { uri ->
        val item = exportItem; exportItem = null
        if (uri != null && item != null) scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    requireNotNull(app.contentResolver.openOutputStream(uri, "wt")).use { out ->
                        repository.exportOriginal(item.id, out)
                    }
                }
            } catch (c: CancellationException) { throw c }
            catch (_: Exception) { playError = L.s(R.string.practice_unsaved) }
        }
    }
    DisposableEffect(Unit) { onDispose { player?.release(); player = null } }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(JournalSpace.page), verticalArrangement = Arrangement.spacedBy(JournalSpace.small)) {
        if (visible.isEmpty()) item { Text(L.s(R.string.msg_no_sessions_yet_your_practice_will_appear_he_37e7)) }
        items(visible, key = { it.id }) { audio ->
            JournalCard {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(JournalSpace.small)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(JournalSpace.tiny)) {
                        Text(audio.title.ifBlank { L.s(R.string.msg_recording_title) }, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(localDateTime(audio.createdAt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (audio.durationMillis > 0) Text(duration(audio.durationMillis), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (audio.state == "Recoverable") Text(L.s(R.string.msg_recording_interrupted), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                    }
                    IconButton(onClick = {
                        if (playingId == audio.id) { player?.release(); player = null; playingId = null }
                        else {
                            player?.release(); player = null; playingId = null; playError = null
                            scope.launch {
                                try {
                                    val file = withContext(Dispatchers.IO) { repository.playable(audio.id) }
                                    val m = MediaPlayer().apply { setDataSource(file.absolutePath); prepare(); start() }
                                    m.setOnCompletionListener { playingId = null; m.release(); if (player === m) player = null }
                                    player = m; playingId = audio.id
                                } catch (c: CancellationException) { throw c }
                                catch (_: Exception) { playError = audio.id }
                            }
                        }
                    }) {
                        Icon(if (playingId == audio.id) Icons.Outlined.Pause else Icons.Outlined.PlayArrow, L.s(R.string.msg_record_audio))
                    }
                    IconButton(onClick = { exportItem = audio; exportLauncher.launch("${audio.title.ifBlank { "recording" }}.wav") }) {
                        Icon(Icons.Outlined.Share, L.s(R.string.msg_record_audio))
                    }
                    if (audio.state == "Saved") IconButton(onClick = { deleting = audio }) {
                        Icon(Icons.Outlined.Delete, L.s(R.string.msg_delete_f6fd))
                    }
                }
            }
        }
        if (playError != null) item { Text(L.s(R.string.msg_recording_interrupted), color = MaterialTheme.colorScheme.error) }
    }
    deleting?.let { audio ->
        AlertDialog(onDismissRequest = { deleting = null }, title = { Text(L.s(R.string.msg_delete_f6fd)) },
            text = { Text(L.s(R.string.msg_remove_p_title_your_completed_practice_histo_32b9, (audio.title.ifBlank { L.s(R.string.msg_recording_title) }))) },
            confirmButton = { JournalTextButton(onClick = { scope.launch { try { withContext(Dispatchers.IO) { repository.delete(audio.id) } } catch (_: Exception) { } }; deleting = null }) { Text(L.s(R.string.msg_delete_f6fd)) } },
            dismissButton = { JournalTextButton(onClick = { deleting = null }) { Text(L.s(R.string.msg_keep_piece_c264)) } })
    }
}
