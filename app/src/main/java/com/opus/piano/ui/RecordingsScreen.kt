package com.opus.piano.ui

import android.content.Intent
import androidx.core.net.toUri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.opus.piano.data.*

@Composable internal fun rememberOpenExternal(): (String) -> Unit {
    val context = LocalContext.current
    return remember(context) {
        { url ->
            try {
                require(url.startsWith("https://"))
                context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()).addCategory(Intent.CATEGORY_BROWSABLE))
            } catch (_: Exception) { Toast.makeText(context, "No application can open this link.", Toast.LENGTH_LONG).show() }
        }
    }
}
@OptIn(ExperimentalLayoutApi::class)
@Composable internal fun ListenAndStudy(key: String, composer: String, title: String, catalogue: String, recordings: List<SavedRecording>, manage: (String) -> Unit) {
    val open = rememberOpenExternal()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle("Listen & Study")
        Text("Search performances and scores", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StudyService.entries.forEach { service ->
                val tint = when (service) { StudyService.SPOTIFY -> Color(0xFF8DB99B); StudyService.YOUTUBE -> Color(0xFFCD9990); StudyService.IMSLP -> Gold; StudyService.MUSESCORE -> Color(0xFF9AACCB) }
                OutlinedButton(onClick = { open(RecordingLinks.search(service, composer, title, catalogue)) }) {
                    Icon(if (service == StudyService.SPOTIFY || service == StudyService.YOUTUBE) Icons.Outlined.PlayCircleOutline else Icons.AutoMirrored.Outlined.MenuBook, null, tint = tint, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp)); Text(service.label, color = tint)
                }
            }
        }
        Text("Generated searches, not verified recordings. Availability and access are determined by each service.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        recordings.firstOrNull { it.preferred }?.let { recording ->
            OutlinedButton(onClick = { open(recording.url) }) { Icon(Icons.Outlined.StarOutline, null); Text("  ${recording.title.ifBlank { "Preferred interpretation" }}") }
        }
        Button(onClick = { manage(key) }) { Text("Saved interpretations · ${recordings.size}") }
    }
}

@Composable fun RecordingsScreen(key: String, title: String, model: ExperienceViewModel, back: () -> Unit) {
    val all by model.recordings.collectAsStateWithLifecycle()
    val loading by model.recordingsLoading.collectAsStateWithLifecycle()
    val error by model.recordingError.collectAsStateWithLifecycle()
    val items = all.filter { it.workKey == key }
    var editor by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var deleting by remember { mutableStateOf<SavedRecording?>(null) }
    val open = rememberOpenExternal()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { CatalogBack("Listen & Study", back) }
        item { Heading("YOUR REFERENCE SHELF", "Saved interpretations", title) }
        item { Button(onClick = { editingId = null; editor = true }) { Icon(Icons.Outlined.Add, null); Text("Add a link") } }
        if (loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        error?.let { message -> item { CatalogFeedback(message, model::retryRecordings) } }
        if (!loading && error == null && items.isEmpty()) item { Panel { SectionTitle("Find an interpretation worth returning to"); Text("Save a performance or score link, then choose your preferred interpretation. Nothing is downloaded.", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        items(items, key = { it.id }) { item ->
            Panel {
                Text(item.title.ifBlank { StudyService.entries.firstOrNull { s -> s.name == item.platform }?.label ?: item.platform }, style = MaterialTheme.typography.titleMedium)
                if (item.performer.isNotBlank()) Text(item.performer, color = MaterialTheme.colorScheme.primary)
                Text("${item.platform.lowercase().replaceFirstChar { it.titlecase() }} · ${localDateTime(item.savedAt)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(item.url, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { open(item.url) }) { Text("Open") }
                    IconButton(onClick = { model.prefer(item.id) }) { Icon(if (item.preferred) Icons.Outlined.Star else Icons.Outlined.StarOutline, if (item.preferred) "Preferred interpretation" else "Mark preferred", tint = MaterialTheme.colorScheme.primary) }
                    IconButton(onClick = { editingId = item.id; editor = true }) { Icon(Icons.Outlined.Edit, "Edit interpretation") }
                    IconButton(onClick = { deleting = item }) { Icon(Icons.Outlined.DeleteOutline, "Remove interpretation") }
                }
                Text("User-saved link · not independently verified", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
    if (editor) RecordingEditor(key, items.firstOrNull { it.id == editingId }, { editor = false }, model)
    deleting?.let { item -> AlertDialog(onDismissRequest = { deleting = null }, title = { Text("Remove saved interpretation?") }, text = { Text("The external recording or score is unaffected.") }, confirmButton = { TextButton(onClick = { model.removeRecording(item.id); deleting = null }) { Text("Remove") } }, dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancel") } }) }
}
@Composable private fun RecordingEditor(key: String, old: SavedRecording?, dismiss: () -> Unit, model: ExperienceViewModel) {
    var url by rememberSaveable { mutableStateOf(old?.url.orEmpty()) }
    var title by rememberSaveable { mutableStateOf(old?.title.orEmpty()) }
    var performer by rememberSaveable { mutableStateOf(old?.performer.orEmpty()) }
    var preferred by rememberSaveable { mutableStateOf(old?.preferred ?: false) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val valid = RecordingLinks.validate(url)
    Dialog(onDismissRequest = { if (!saving) dismiss() }) {
        Surface(shape = MaterialTheme.shapes.large) {
            Column(Modifier.imePadding().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle(if (old == null) "Save an interpretation" else "Edit interpretation")
                OutlinedTextField(url, { url = it; error = null }, Modifier.fillMaxWidth(), label = { Text("Recording or score URL") }, isError = url.isNotBlank() && valid == null, supportingText = { Text(valid?.platform?.label ?: "HTTPS links from Spotify, YouTube, IMSLP or MuseScore") }, maxLines = 3)
                OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text("Title (optional)") }, singleLine = true)
                OutlinedTextField(performer, { performer = it }, Modifier.fillMaxWidth(), label = { Text("Performer (optional)") }, singleLine = true)
                Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(preferred, { preferred = it }); Text("Preferred interpretation") }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = dismiss, enabled = !saving) { Text("Cancel") }
                    Button(onClick = {
                        saving = true
                        model.saveRecording(SavedRecording(old?.id ?: 0, key, url, title, performer, valid!!.platform.name, old?.savedAt ?: 0, preferred)) { failure ->
                            saving = false
                            if (failure == null) dismiss() else error = failure
                        }
                    }, enabled = valid != null && !saving) { Text(if (saving) "Saving…" else "Save") }
                }
            }
        }
    }
}
