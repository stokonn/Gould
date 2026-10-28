package com.opus.piano.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.opus.piano.R
import com.opus.piano.data.*
import com.opus.piano.i18n.L
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Activity-scoped modal: route changes never dispose an unfinished profile draft. */
@Composable internal fun ProfileEditor(profile: ProfileViewModel) {
    val draft by profile.editDraft.collectAsStateWithLifecycle()
    val settings by profile.settings.collectAsStateWithLifecycle()
    val writing by profile.busy.collectAsStateWithLifecycle()
    val decoding by profile.draftAvatarBusy.collectAsStateWithLifecycle()
    val error by profile.editError.collectAsStateWithLifecycle()
    val draftProblem by profile.draftLoadError.collectAsStateWithLifecycle()
    val recoveryVisible by profile.draftRecoveryVisible.collectAsStateWithLifecycle()
    val rawExport = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) {
        it?.let(profile::exportProfileDraft)
    }
    if (draftProblem) {
        if (recoveryVisible) AlertDialog(onDismissRequest = profile::dismissDraftRecovery, title = { Text(L.s(R.string.profile_draft_recovery_title)) },
            text = { Text(L.s(R.string.profile_draft_recovery_message)) },
            confirmButton = { JournalPrimaryButton(enabled = !writing, onClick = profile::retryProfileDraft) { Text(L.s(R.string.profile_draft_retry)) } },
            dismissButton = { Column {
                JournalTextButton(enabled = !writing, onClick = { rawExport.launch("gould-profile-draft-original.json") }) { Text(L.s(R.string.profile_draft_export)) }
                JournalTextButton(onClick = profile::dismissDraftRecovery) { Text(L.s(R.string.msg_cancel_77df)) }
            } })
        return
    }
    val current = draft ?: return
    val busy = writing || decoding
    var leaving by rememberSaveable { mutableStateOf(false) }
    val requestClose = {
        if (!busy) {
            if (current.dirty) leaving = true else profile.discardProfileEdit()
        }
    }
    val avatar by produceState<android.graphics.Bitmap?>(null, current.avatar, settings.avatar) {
        value = runCatching {
            if (current.avatar == "keep") withContext(Dispatchers.Default) {
                val bytes = android.util.Base64.decode(settings.avatar, android.util.Base64.DEFAULT)
                android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } else profile.draftAvatar(current)
        }.getOrNull()
    }
    val photo = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let(profile::chooseDraftAvatar)
    }
    fun change(edit: (ProfileEditValues) -> ProfileEditValues) = profile.changeProfileDraft(edit)
    AlertDialog(onDismissRequest = requestClose, properties = DialogProperties(dismissOnBackPress = !busy, dismissOnClickOutside = !busy),
        title = { Text(L.s(R.string.msg_edit_profile_cd28)) }, text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(Modifier.size(72.dp), shape = CircleShape) {
                    if (avatar != null) Image(avatar!!.asImageBitmap(), L.s(R.string.msg_current_avatar_8634), contentScale = ContentScale.Crop)
                    else Icon(Icons.Outlined.Person, L.s(R.string.msg_default_avatar_400b), Modifier.padding(16.dp))
                }
                OutlinedTextField(current.value.name, { change { v -> v.copy(name = it.take(80)) } },
                    label = { Text(L.s(R.string.msg_display_name_c787)) }, enabled = !busy, singleLine = true, modifier = Modifier.fillMaxWidth())
                JournalSecondaryButton(onClick = { photo.launch("image/*") }, enabled = !busy) { Text(L.s(R.string.msg_choose_avatar_7fa8)) }
                JournalTextButton(onClick = profile::defaultDraftAvatar, enabled = !busy) { Text(L.s(R.string.msg_use_default_avatar_62c9)) }
                ArtistAutocomplete(current.value.artist, { change { v -> v.copy(artist = it) } }, enabled = !busy)
                OutlinedTextField(current.value.piece, { change { v -> v.copy(piece = it.take(240)) } },
                    label = { Text(L.s(R.string.favorite_piece)) }, enabled = !busy, modifier = Modifier.fillMaxWidth())
                EraAutocomplete(current.value.era, { change { v -> v.copy(era = it) } }, enabled = !busy)
                OutlinedTextField(current.value.biography, { change { v -> v.copy(biography = it.take(400)) } },
                    label = { Text(L.s(R.string.musical_bio)) }, enabled = !busy, modifier = Modifier.fillMaxWidth())
                val ids = StatisticLayout.normalize(current.value.statistics.split(','))
                val names = mapOf("time" to L.s(R.string.msg_total_practice_a63c), "pieces" to L.s(R.string.msg_repertoire_pieces_e09d),
                    "sessions" to L.s(R.string.msg_practice_sessions_60c7), "streak" to L.s(R.string.msg_current_streak_2ff8))
                Text(L.fixed("Statistics"), style = MaterialTheme.typography.titleMedium)
                ids.forEachIndexed { index, id ->
                    Text(names.getValue(id), style = MaterialTheme.typography.bodyMedium)
                    Row {
                        IconButton(enabled = !busy && index > 0, onClick = {
                            change { it.copy(statistics = ids.toMutableList().apply { java.util.Collections.swap(this, index, index - 1) }.joinToString(",")) }
                        }) { Icon(Icons.Outlined.ArrowUpward, L.s(R.string.msg_move_names_id_earlier_11d2, names[id])) }
                        IconButton(enabled = !busy && index < ids.lastIndex, onClick = {
                            change { it.copy(statistics = ids.toMutableList().apply { java.util.Collections.swap(this, index, index + 1) }.joinToString(",")) }
                        }) { Icon(Icons.Outlined.ArrowDownward, L.s(R.string.msg_move_names_id_later_4339, names[id])) }
                    }
                }
                if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            }
        }, confirmButton = {
            JournalPrimaryButton(onClick = { leaving = false; profile.saveProfileEdit() }, enabled = !busy && current.value.name.isNotBlank()) { Text(L.s(R.string.msg_save_efc0)) }
        }, dismissButton = { JournalTextButton(onClick = requestClose, enabled = !busy) { Text(L.s(R.string.msg_done_e9b4)) } })
    if (leaving) AlertDialog(onDismissRequest = { leaving = false }, title = { Text(L.s(R.string.profile_edit_unsaved)) },
        text = { Text(L.s(R.string.profile_edit_leave_message)) },
        confirmButton = { JournalPrimaryButton(enabled = !busy && current.value.name.isNotBlank(), onClick = { leaving = false; profile.saveProfileEdit() }) { Text(L.s(R.string.msg_save_efc0)) } },
        dismissButton = {
            Column {
                JournalTextButton(enabled = !busy, onClick = { leaving = false; profile.discardProfileEdit() }) { Text(L.s(R.string.profile_edit_discard)) }
                JournalTextButton(onClick = { leaving = false }) { Text(L.s(R.string.profile_edit_keep)) }
            }
        })
}

@Composable private fun EraAutocomplete(value: String, change: (String) -> Unit, enabled: Boolean) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as com.opus.piano.OpusApplication
    val catalog by app.catalogRepository.snapshot.collectAsStateWithLifecycle(CatalogSnapshot(emptyList(), emptyList()))
    var expanded by remember { mutableStateOf(false) }
    // Deliberately use local catalogue periods; this field must never start online discovery.
    val matches = remember(value, catalog.composers, L.locale) {
        eraSuggestions(catalog.composers.map { it.epoch }, value, L::fixed)
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(value, { change(it.take(80)); expanded = true }, label = { Text(L.s(R.string.favorite_era)) },
            leadingIcon = { Icon(Icons.Outlined.Search, null) }, enabled = enabled, modifier = Modifier.fillMaxWidth(), singleLine = true)
        if (enabled && expanded) {
            if (matches.isEmpty()) Text(L.s(R.string.ui010_local_no_match), style = MaterialTheme.typography.bodySmall)
            else Column(Modifier.heightIn(max = 200.dp).verticalScroll(rememberScrollState())) {
                matches.forEach { era ->
                    Surface(onClick = { change(L.fixed(era)); expanded = false }, color = MaterialTheme.colorScheme.surface) {
                        Text(L.fixed(era), Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(12.dp), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
