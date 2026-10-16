package com.opus.piano.ui

import com.opus.piano.i18n.L
import com.opus.piano.R
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
            } catch (_: Exception) { Toast.makeText(context, L.s(R.string.msg_no_application_can_open_this_link_f62f), Toast.LENGTH_LONG).show() }
        }
    }
}
@OptIn(ExperimentalLayoutApi::class)
@Composable internal fun ListenAndStudy(key: String, composer: String, title: String, catalogue: String, recordings: List<SavedRecording>, manage: (String) -> Unit) {
    val open = rememberOpenExternal()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle(L.s(R.string.msg_listen_study_0d87))
        Text(L.s(R.string.msg_search_performances_and_scores_e56b), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StudyService.entries.forEach { service ->
                val tint = MaterialTheme.colorScheme.primary
                OutlinedButton(onClick = { open(RecordingLinks.search(service, composer, title, catalogue)) }) {
                    Icon(if (service == StudyService.SPOTIFY || service == StudyService.YOUTUBE) Icons.Outlined.PlayCircleOutline else Icons.AutoMirrored.Outlined.MenuBook, null, tint = tint, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp)); Text(service.label, color = tint)
                }
            }
        }
        Text(L.s(R.string.msg_generated_searches_not_verified_recordings_a_0e42), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        recordings.firstOrNull { it.preferred }?.let { recording ->
            OutlinedButton(onClick = { open(recording.url) }) { Icon(Icons.Outlined.StarOutline, null); Text(L.s(R.string.msg_recording_title_ifblank_preferred_interpreta_0716, (recording.title.ifBlank { L.s(R.string.preferred_interpretation) }))) }
        }
        Button(onClick = { manage(key) }) { Text(L.s(R.string.msg_saved_interpretations_recordings_size_499b, (recordings.size))) }
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
        item { CatalogBack(L.s(R.string.msg_listen_study_0d87), back) }
        item { Heading(L.s(R.string.msg_your_reference_shelf_67ea), L.s(R.string.msg_saved_interpretations_b15d), title) }
        item { Button(onClick = { editingId = null; editor = true }) { Icon(Icons.Outlined.Add, null); Text(L.s(R.string.msg_add_a_link_56e6)) } }
        if (loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        error?.let { message -> item { CatalogFeedback(message, model::retryRecordings) } }
        if (!loading && error == null && items.isEmpty()) item { Panel { SectionTitle(L.s(R.string.msg_find_an_interpretation_worth_returning_to_0033)); Text(L.s(R.string.msg_save_a_performance_or_score_link_then_choose_b1cc), color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        items(items, key = { it.id }) { item ->
            Panel {
                Text(item.title.ifBlank { StudyService.entries.firstOrNull { s -> s.name == item.platform }?.label ?: item.platform }, style = MaterialTheme.typography.titleMedium)
                if (item.performer.isNotBlank()) Text(item.performer, color = MaterialTheme.colorScheme.primary)
                Text("${item.platform.lowercase().replaceFirstChar { it.titlecase() }} · ${localDateTime(item.savedAt)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(item.url, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { open(item.url) }) { Text(L.s(R.string.msg_open_cf9b)) }
                    IconButton(onClick = { model.prefer(item.id) }) { Icon(if (item.preferred) Icons.Outlined.Star else Icons.Outlined.StarOutline, if (item.preferred) L.s(R.string.preferred_interpretation) else L.s(R.string.msg_mark_preferred_cbfe), tint = MaterialTheme.colorScheme.primary) }
                    IconButton(onClick = { editingId = item.id; editor = true }) { Icon(Icons.Outlined.Edit, L.s(R.string.msg_edit_interpretation_6f23)) }
                    IconButton(onClick = { deleting = item }) { Icon(Icons.Outlined.DeleteOutline, L.s(R.string.msg_remove_interpretation_019c)) }
                }
                Text(L.s(R.string.msg_user_saved_link_not_independently_verified_bf46), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
    if (editor) RecordingEditor(key, items.firstOrNull { it.id == editingId }, { editor = false }, model)
    deleting?.let { item -> AlertDialog(onDismissRequest = { deleting = null }, title = { Text(L.s(R.string.msg_remove_saved_interpretation_0280)) }, text = { Text(L.s(R.string.msg_the_external_recording_or_score_is_unaffecte_f26a)) }, confirmButton = { TextButton(onClick = { model.removeRecording(item.id); deleting = null }) { Text(L.s(R.string.msg_remove_e963)) } }, dismissButton = { TextButton(onClick = { deleting = null }) { Text(L.s(R.string.msg_cancel_77df)) } }) }
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
                SectionTitle(if (old == null) L.s(R.string.msg_save_an_interpretation_11b5) else L.s(R.string.msg_edit_interpretation_6f23))
                OutlinedTextField(url, { url = it; error = null }, Modifier.fillMaxWidth(), label = { Text(L.s(R.string.msg_recording_or_score_url_c590)) }, isError = url.isNotBlank() && valid == null, supportingText = { Text(valid?.platform?.label ?: L.s(R.string.msg_https_links_from_spotify_youtube_imslp_or_mu_fa31)) }, maxLines = 3)
                OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text(L.s(R.string.msg_title_optional_932f)) }, singleLine = true)
                OutlinedTextField(performer, { performer = it }, Modifier.fillMaxWidth(), label = { Text(L.s(R.string.msg_performer_optional_26e9)) }, singleLine = true)
                Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(preferred, { preferred = it }); Text(L.s(R.string.msg_preferred_interpretation_4800)) }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = dismiss, enabled = !saving) { Text(L.s(R.string.msg_cancel_77df)) }
                    Button(onClick = {
                        saving = true
                        model.saveRecording(SavedRecording(old?.id ?: 0, key, url, title, performer, valid!!.platform.name, old?.savedAt ?: 0, preferred)) { failure ->
                            saving = false
                            if (failure == null) dismiss() else error = failure
                        }
                    }, enabled = valid != null && !saving) { Text(if (saving) L.s(R.string.msg_saving_56a2) else L.s(R.string.msg_save_efc0)) }
                }
            }
        }
    }
}
