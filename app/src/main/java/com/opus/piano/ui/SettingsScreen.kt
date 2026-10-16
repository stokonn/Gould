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

@Composable internal fun SettingsScreen(profile: ProfileViewModel, model: OpusViewModel, onboarding: () -> Unit, back: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val local by profile.local.collectAsStateWithLifecycle()
    var legal by remember { mutableStateOf(false) }
    var secret by remember { mutableStateOf(false) }
    var taps by remember { mutableIntStateOf(0) }
    var lastTap by remember { mutableLongStateOf(0) }
    val sessionCsv = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { it?.let { uri -> profile.csv(uri,false) } }
    val partCsv = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { it?.let { uri -> profile.csv(uri,true) } }
    val notifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) local?.let { profile.reminder(true,it.reminderHour,it.reminderMinute) }
        else profile.notice.value = "Notifications are blocked. Enable them in Android Settings to receive reminders."
    }
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
            Text("Format-1 backups from 0.6 and 0.7 remain importable. New backups use format 2. Backups are not encrypted.", style = MaterialTheme.typography.bodySmall)
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        } }
        item { Panel {
            SectionTitle("Statistics export")
            Text("Separate UTF-8 reports prevent repeated session totals. CSV is not a backup. Notes are exported in spreadsheet-safe form.")
            OutlinedButton(onClick = { sessionCsv.launch("Goldberg-sessions.csv") }, enabled = !busy) { Text("Export sessions CSV") }
            OutlinedButton(onClick = { partCsv.launch("Goldberg-pieces.csv") }, enabled = !busy) { Text("Export piece times CSV") }
        } }
        item { Panel {
            SectionTitle("Practice reminder")
            val value = local
            if (value != null) {
                Text("Around %02d:%02d daily. Android may delay delivery.".format(value.reminderHour,value.reminderMinute))
                Switch(value.reminderEnabled,{ enabled ->
                    if (enabled && android.os.Build.VERSION.SDK_INT >= 33 && androidx.core.content.ContextCompat.checkSelfPermission(context,android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) notifications.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    else profile.reminder(enabled,value.reminderHour,value.reminderMinute)
                },enabled = !busy)
                OutlinedButton(onClick = { android.app.TimePickerDialog(context,{ _,h,m -> profile.reminder(value.reminderEnabled,h,m) },value.reminderHour,value.reminderMinute,true).show() }) { Text("Choose time") }
            }
        } }
        item { Panel {
            SectionTitle("About")
            TextButton(onClick = {
                val now = android.os.SystemClock.elapsedRealtime()
                taps = if (now-lastTap <= 1500) taps+1 else 1; lastTap=now
                if (taps>=7) {
                    taps=0
                    if (model.timer.value.running) profile.notice.value="Pause practice before opening Bach’s Secret."
                    else { secret=true; profile.curious() }
                }
            }) { Text("Goldberg 0.8.0 · Smart Practice") }
            TextButton(onClick = onboarding) { Text("Revisit introduction") }
            TextButton(onClick = { legal=true }) { Text("Privacy & licenses") }
        } }
    }
    if (legal) LegalScreen { legal=false }
    if (secret) HarpsichordScreen { secret=false }
    preview?.let { summary ->
        val timer by model.timer.collectAsStateWithLifecycle()
        AlertDialog(onDismissRequest = profile::cancelRestore, title = { Text("Replace this device’s journal?") }, text = { Text("$summary\n\nAll current saved data will be replaced. Export a backup first. Any unfinished timer will be discarded only after a successful restore.${if (timer.elapsed > 0 || timer.running) " You have an unfinished session." else ""}") },
            confirmButton = { TextButton(enabled = !busy && !timer.saving, onClick = { profile.restore(model::reset) }) { Text("Replace & restore") } },
            dismissButton = { TextButton(enabled = !busy, onClick = profile::cancelRestore) { Text("Cancel") } })
    }
}
