package com.opus.piano.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable internal fun SettingsScreen(profile: ProfileViewModel, model: OpusViewModel, back: () -> Unit) {
    val settings by profile.settings.collectAsStateWithLifecycle()
    val busy by profile.busy.collectAsStateWithLifecycle()
    val preview by profile.preview.collectAsStateWithLifecycle()
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { it?.let(profile::export) }
    val restore = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { it?.let(profile::inspect) }
    val usage by profile.cacheUsage.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { profile.measureCache() }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { CatalogBack("Settings", back) }
        item { Panel {
            SectionTitle("Appearance")
            listOf("System", "Light", "Dark").forEach { theme ->
                Surface(modifier = Modifier.semantics { selected = settings.theme == theme; role = Role.RadioButton }, onClick = { if (!busy) profile.theme(theme) }, enabled = !busy) {
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        RadioButton(settings.theme == theme, null); Spacer(Modifier.width(12.dp)); Text(if (theme == "System") "Follow system" else theme)
                    }
                }
            }
            Text("Text size and motion follow Android accessibility settings.", style = MaterialTheme.typography.bodySmall)
        } }
        item { Panel {
            SectionTitle("Typography")
            listOf("Editorial" to "Goldberg Typography", "System" to "System Font").forEach { (id, label) ->
                Surface(onClick = { profile.typography(id) }, enabled = !busy, modifier = Modifier.semantics { selected = settings.typography == id; role = Role.RadioButton }) {
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        RadioButton(settings.typography == id, null); Spacer(Modifier.width(12.dp)); Text(label)
                    }
                }
            }
            Text("Cormorant Garamond headings with Inter text, or your device’s system font.", style = MaterialTheme.typography.bodySmall)
        } }
        item { Panel {
            SectionTitle("Cache Management")
            Text(usage?.let { "Disposable downloads: %.1f MiB".format(it / 1048576.0) } ?: "Measuring downloaded cache…")
            Text("Clearing removes downloaded images and response copies. Practice history, repertoire, badge dates, profile settings and the saved catalogue remain on this device.", style = MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick = profile::clearCache, enabled = !busy) { Text("Clear Cache") }
        } }
        item { Panel {
            SectionTitle("Backup & Restore")
            Text("Keep a local copy of your journal, profile and showcase. Restoring replaces the current saved data; export a backup first.")
            Button(onClick = { export.launch("Goldberg-backup.json") }, enabled = !busy) { Text("Export backup") }
            OutlinedButton(onClick = { restore.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) }, enabled = !busy) { Text("Import backup") }
            Text("Version 1 backups from 0.6.0 and 0.6.1 remain compatible. Backups are not encrypted.", style = MaterialTheme.typography.bodySmall)
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        } }
        item { Panel { SectionTitle("About"); Text("Goldberg 0.7.0 · Smart Cache & Classical Editorial"); Text("Cormorant Garamond & Inter · SIL Open Font License 1.1", style = MaterialTheme.typography.bodySmall) } }
    }
    preview?.let { summary ->
        val timer by model.timer.collectAsStateWithLifecycle()
        AlertDialog(onDismissRequest = profile::cancelRestore, title = { Text("Replace this device’s journal?") }, text = { Text("$summary\n\nAll current saved data will be replaced. Export a backup first. Any unfinished timer will be discarded only after a successful restore.${if (timer.elapsed > 0 || timer.running) " You have an unfinished session." else ""}") },
            confirmButton = { TextButton(enabled = !busy && !timer.saving, onClick = { profile.restore(model::reset) }) { Text("Replace & restore") } },
            dismissButton = { TextButton(enabled = !busy, onClick = profile::cancelRestore) { Text("Cancel") } })
    }
}
