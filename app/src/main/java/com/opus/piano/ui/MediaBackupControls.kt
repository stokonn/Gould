package com.opus.piano.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.opus.piano.OpusApplication
import com.opus.piano.R
import com.opus.piano.audio.RecordingController
import com.opus.piano.data.MediaBackupRepository
import com.opus.piano.i18n.L
import kotlinx.coroutines.*

/** Explicit SAF-only audio archive flow. Staging receipts survive UI/process loss. */
@Composable internal fun MediaBackupControls() {
    val context = LocalContext.current
    val app = context.applicationContext as OpusApplication
    val media = remember { MediaBackupRepository(app,app.database) }
    val scope = rememberCoroutineScope()
    val recording by app.recording.state.collectAsStateWithLifecycle()
    var pending by remember { mutableStateOf<List<String>>(emptyList()) }
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    var complete by remember { mutableStateOf(false) }
    val active = recording.phase in setOf(RecordingController.Phase.Preparing,RecordingController.Phase.Recording,RecordingController.Phase.Finalizing)
    LaunchedEffect(media) { pending = media.pendingOperations() }
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) scope.launch {
            busy = true; failed = false; complete = false
            try {
                app.recording.whileIdle {
                    withContext(Dispatchers.IO) {
                        requireNotNull(context.contentResolver.openOutputStream(uri,"wt")).use { media.export(it) }
                    }
                }
                complete = true
            } catch (c: CancellationException) { throw c }
            catch (_: Exception) { failed = true }
            finally { busy = false }
        }
    }
    val import = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            busy = true; failed = false; complete = false
            try {
                selected = withContext(Dispatchers.IO) {
                    requireNotNull(context.contentResolver.openInputStream(uri)).use { media.receive(it) }
                }
                pending = media.pendingOperations()
            } catch (c: CancellationException) { throw c }
            catch (_: Exception) { failed = true }
            finally { busy = false }
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(JournalSpace.small)) {
        Text(L.s(R.string.studio_archive_disclosure),style = MaterialTheme.typography.bodySmall)
        if (active) Text(L.s(R.string.studio_archive_stop_first),color = MaterialTheme.colorScheme.error)
        JournalSecondaryButton(onClick = { export.launch("Gould-journal-and-audio.zip") },enabled = !busy && !active) { Text(L.s(R.string.studio_archive_export)) }
        JournalSecondaryButton(onClick = { import.launch(arrayOf("application/zip","application/octet-stream")) },enabled = !busy && !active) { Text(L.s(R.string.studio_archive_import)) }
        pending.forEach { id ->
            JournalTextButton(onClick = { selected = id },enabled = !busy && !active) { Text(L.s(R.string.studio_archive_retry)) }
        }
        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (failed) Text(L.s(R.string.studio_audio_operation_failed),color = MaterialTheme.colorScheme.error)
        if (complete) Text(L.s(R.string.studio_archive_complete))
    }
    selected?.let { id ->
        AlertDialog(onDismissRequest = { if (!busy) selected = null },title = { Text(L.s(R.string.studio_archive_import)) },
            text = { Text(L.s(R.string.studio_archive_confirm)) },
            confirmButton = { JournalTextButton(enabled = !busy && !active,onClick = {
                scope.launch {
                    busy = true; failed = false; complete = false
                    try {
                        app.practice.replacing(preservesRecovery = true) {
                            app.recording.whileIdle {
                                app.localPlayback.stopAndAwait(); app.metronome.stop()
                                app.catalogRepository.restoreJournal { media.restore(id) }
                            }
                        }
                        selected = null; pending = media.pendingOperations(); complete = true
                    } catch (c: CancellationException) { throw c }
                    catch (_: Exception) { failed = true }
                    finally { busy = false }
                }
            }) { Text(L.s(R.string.msg_import_backup_1b40)) } },
            dismissButton = { JournalTextButton(enabled = !busy,onClick = { selected = null }) { Text(L.s(R.string.msg_cancel_77df)) } })
    }
}
