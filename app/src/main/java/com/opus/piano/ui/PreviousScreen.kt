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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.opus.piano.OpusApplication
import com.opus.piano.data.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
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

/** Bounded keyset pages; foreground lifecycle-owned playback and explicit recovery. */
@Composable private fun RecordingsTab() {
    val app = LocalContext.current.applicationContext as OpusApplication
    val repository = remember { LocalRecordingRepository(app.database, File(app.filesDir,"recordings")) }
    val player = app.localPlayback
    val playback by player.state.collectAsState()
    val owner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val initialCaptureKey = remember(app.recording) {
        app.recording.state.value.let { it.id to it.phase }
    }
    val captureKey by remember(app.recording) {
        app.recording.state.map { it.id to it.phase }.distinctUntilChanged()
    }.collectAsStateWithLifecycle(initialValue = initialCaptureKey)
    var observedCapture by remember { mutableStateOf(captureKey) }
    var visible by remember { mutableStateOf<List<LocalAudio>>(emptyList()) }
    var request by remember { mutableIntStateOf(0) }
    var refresh by remember { mutableIntStateOf(0) }
    var more by remember { mutableStateOf(true) }
    var loading by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<LocalAudio?>(null) }
    var exportItem by remember { mutableStateOf<LocalAudio?>(null) }
    var operationFailed by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var reviewId by rememberSaveable { mutableStateOf<Long?>(null) }
    val reviewed by produceState<Pair<PracticeSession,List<SessionPart>>?>(null,reviewId) {
        value = reviewId?.let { id -> app.database.practiceDao().session(id)?.let { it to app.database.practiceDao().historyParts(listOf(id)) } }
    }
    reviewed?.let { HistoricalReview(it.first,it.second) { reviewId = null } }
    DisposableEffect(owner,player) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _,event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_STOP) player.pause()
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer); player.stop() }
    }
    LaunchedEffect(request,refresh) {
        loading = true; failed = false
        try {
            val cursor = visible.lastOrNull()
            val page = app.database.studioDao().audioPage(cursor?.createdAt ?: Long.MAX_VALUE, cursor?.id ?: "~",31)
            visible = (visible + page.take(30)).distinctBy { it.id }
            more = page.size > 30
        } catch (c: CancellationException) { throw c }
        catch (_: Exception) { failed = true }
        finally { loading = false }
    }
    fun reload() { visible = emptyList(); refresh++ }
    LaunchedEffect(captureKey) {
        if (captureKey != observedCapture) {
            observedCapture = captureKey
            reload()
        }
    }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("audio/wav")) { uri ->
        val item = exportItem; exportItem = null
        if (uri != null && item != null) scope.launch {
            busy = true
            try {
                withContext(Dispatchers.IO) {
                    requireNotNull(app.contentResolver.openOutputStream(uri,"wt")).use { repository.exportOriginal(item.id,it) }
                }
                operationFailed = false
            } catch (c: CancellationException) { throw c }
            catch (_: Exception) { operationFailed = true }
            finally { busy = false }
        }
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(JournalSpace.page), verticalArrangement = Arrangement.spacedBy(JournalSpace.small)) {
        item { JournalTextButton(onClick = { player.stop(); reload() }, enabled = !loading && !busy) { Text(L.s(R.string.practice_retry)) } }
        if (!loading && !failed && visible.isEmpty()) item { Text(L.s(R.string.studio_recordings_empty)) }
        items(visible,key = { it.id }) { audio ->
            val active = audio.state in setOf("Preparing","Recording","Finalizing")
            val originalAvailable by produceState<Boolean?>(null,audio.id,audio.state) {
                value = try { repository.originalAvailable(audio.id) }
                    catch (c: CancellationException) { throw c }
                    catch (_: Exception) { false }
            }
            val associations by produceState<List<LocalAudioPart>>(emptyList(),audio.id,audio.state) {
                value = app.database.studioDao().audioPartPreview(audio.id,6)
            }
            val session by produceState<PracticeSession?>(null,audio.id,audio.sessionToken) {
                value = audio.sessionToken?.let { app.database.practiceDao().completed(it) }
            }
            JournalCard {
                Column(verticalArrangement = Arrangement.spacedBy(JournalSpace.small)) {
                    Text(audio.title.ifBlank { L.s(R.string.msg_recording_title) },style = MaterialTheme.typography.titleMedium)
                    if (audio.composer.isNotBlank()) Text(audio.composer,style = MaterialTheme.typography.bodySmall)
                    if (associations.isNotEmpty()) Text(L.s(R.string.studio_recorded_pieces,
                        associations.take(5).map { if(it.pieceKey=="free") L.s(R.string.msg_free_practice_524f) else it.title }.distinct().joinToString(" · ") + if(associations.size>5) " …" else ""),style=MaterialTheme.typography.bodySmall)
                    session?.let { related -> JournalTextButton(onClick = { player.pause(); reviewId = related.id }) { Text(L.s(R.string.studio_view_practice)) } }
                    Text(localDateTime(audio.createdAt),style = MaterialTheme.typography.bodySmall,color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (audio.durationMillis > 0) Text(duration(audio.durationMillis),style = MaterialTheme.typography.labelMedium)
                    when (audio.state) {
                        "Preparing","Recording","Finalizing" -> Text(L.s(R.string.studio_audio_in_progress),style=MaterialTheme.typography.bodySmall)
                        "Missing" -> Text(L.s(R.string.studio_audio_missing),style=MaterialTheme.typography.bodySmall)
                        "Deleting" -> Text(L.s(R.string.studio_audio_deletion_pending),style=MaterialTheme.typography.bodySmall)
                        "Recoverable" -> {
                            Text(L.s(R.string.msg_recording_interrupted),color = MaterialTheme.colorScheme.error)
                            Text(L.s(R.string.studio_recovery_export_note),style=MaterialTheme.typography.bodySmall)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(JournalSpace.small)) {
                        if (audio.state == "Saved") IconButton(onClick = { player.play(audio.id) },enabled = !busy) {
                            Icon(if (playback.id == audio.id && playback.playing) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                                L.s(if (playback.id == audio.id && playback.playing) R.string.msg_pause_7819 else R.string.studio_play))
                        }
                        IconButton(onClick = { exportItem = audio; exportLauncher.launch("Gould-recording-${audio.id}.wav"+if(audio.state=="Saved") "" else ".part") },enabled = !busy && !active && originalAvailable == true) {
                            Icon(Icons.Outlined.FileDownload,L.s(if(audio.state=="Saved") R.string.studio_export_original else R.string.studio_export_recovery))
                        }
                        IconButton(onClick = { deleting = audio },enabled = !busy && !active) { Icon(Icons.Outlined.Delete,L.s(R.string.msg_delete_f6fd)) }
                    }
                    if (audio.state == "Recoverable") JournalSecondaryButton(enabled = !busy,onClick = {
                        scope.launch {
                            busy = true
                            try { repository.recover(audio.id); operationFailed = false; reload() }
                            catch (c: CancellationException) { throw c }
                            catch (_: Exception) { operationFailed = true }
                            finally { busy = false }
                        }
                    }) { Text(L.s(R.string.studio_recover_take)) }
                    if (playback.id == audio.id) {
                        if (playback.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
                        if (playback.duration > 0) {
                            Text("${duration(playback.position)} / ${duration(playback.duration)}",style = MaterialTheme.typography.bodySmall)
                            Slider(value = playback.position.toFloat().coerceIn(0f,playback.duration.toFloat()),
                                onValueChange = { player.seek(it.toLong()) },valueRange = 0f..playback.duration.toFloat(),
                                modifier = Modifier.semantics { contentDescription = L.s(R.string.studio_seek) })
                        }
                        if (playback.blockedByCapture) Text(L.s(R.string.studio_playback_capture_blocked),color=MaterialTheme.colorScheme.error)
                        if (playback.failed) Text(L.s(R.string.msg_recording_interrupted),color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
        if (loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        if (failed || operationFailed) item { Text(L.s(R.string.studio_audio_operation_failed),color = MaterialTheme.colorScheme.error) }
        if (more && !loading && !failed) item { JournalTextButton(onClick = { request++ }) { Text(L.s(R.string.studio_older_recordings)) } }
    }
    deleting?.let { audio ->
        AlertDialog(onDismissRequest = { if (!busy) deleting = null }, title = { Text(L.s(R.string.msg_delete_f6fd)) },
            text = { Column { Text(L.s(R.string.studio_delete_audio_confirm)); if (operationFailed) Text(L.s(R.string.studio_audio_operation_failed),color = MaterialTheme.colorScheme.error) } },
            confirmButton = { JournalTextButton(enabled = !busy,onClick = { scope.launch {
                busy = true
                try { player.stopAndAwait(); repository.delete(audio.id); operationFailed = false; deleting = null; reload() }
                catch (c: CancellationException) { throw c }
                catch (_: Exception) { operationFailed = true }
                finally { busy = false }
            } }) { Text(L.s(R.string.msg_delete_f6fd)) } },
            dismissButton = { JournalTextButton(enabled = !busy,onClick = { deleting = null }) { Text(L.s(R.string.msg_keep_466f)) } })
    }
}
