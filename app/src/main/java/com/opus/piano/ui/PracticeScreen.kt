package com.opus.piano.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.opus.piano.service.SmartPracticeService

@Composable internal fun PracticeScreen(library: LibraryState, model: OpusViewModel, profile: ProfileViewModel) {
    val timer by model.timer.collectAsStateWithLifecycle()
    val settings by profile.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var smart by remember { mutableStateOf(timer.smart || settings.smartEnabled) }
    var privacy by remember { mutableStateOf(false) }
    var convert by remember { mutableStateOf(false) }
    var discard by remember { mutableStateOf(false) }
    var choose by remember { mutableStateOf(false) }
    fun startSmart() {
        SmartPracticeService.start(context)
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result[Manifest.permission.RECORD_AUDIO] == true || ContextCompat.checkSelfPermission(context,Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) startSmart()
        else model.practice.notice.value = "Microphone permission denied. Manual practice is available."
    }
    fun start() {
        if (smart) privacy = true else model.practice.command { start(false) }
    }
    if (timer.status == "Review") { PracticeReviewScreen(timer,model); return }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Heading("ONE NOTE AT A TIME","Practice","Your piece, your pace.")
        if (!timer.ready) { Text("Recovering your practice…"); return@Column }
        Row { Switch(smart,{ value -> if (!timer.running) { smart = value; profile.smartPreference(value) } },enabled = !timer.running); Text("Smart Practice · experimental",Modifier.padding(12.dp)) }
        Text(if (smart) "Local piano-like activity detection. Can mistake speech or recorded music for playing." else "Manual timing: you decide when practice starts and pauses.")
        if (smart) {
            var sensitivity by remember(settings.sensitivity) { mutableFloatStateOf(settings.sensitivity.toFloat()) }
            Text("Sensitivity: ${sensitivity.toInt()}")
            Slider(sensitivity,{ sensitivity = it },onValueChangeFinished = { profile.smartSettings(sensitivity.toInt(),settings.pauseSeconds) },valueRange = 0f..100f, enabled = !timer.running)
            Choice("Auto-pause",(2..30).map { "$it seconds" },"${settings.pauseSeconds} seconds") { val seconds = it.substringBefore(' ').toInt(); profile.smartSettings(settings.sensitivity,seconds); model.practice.command { configure(settings.sensitivity,seconds) } }
        }
        Panel {
            Text(timer.parts[timer.active].title,style = MaterialTheme.typography.titleLarge)
            Text(duration(timer.elapsed),style = MaterialTheme.typography.displaySmall)
            Text("Credited practice · elapsed ${duration(timer.wallElapsed)}")
            if (timer.mode != "Manual") Text("Detected active ${duration(timer.detectedActive)}")
            Text(if (timer.running && timer.smart) "Microphone active · ${timer.status}" else timer.status)
            Button(onClick = {
                if (timer.running) model.practice.command { pause() }
                else if (timer.token.isNotEmpty() && timer.smart != smart) convert = true else start()
            },Modifier.fillMaxWidth(),enabled = !timer.saving) { Text(if (timer.running) "Pause" else "Start / resume") }
            OutlinedButton(onClick = model::finish,enabled = timer.token.isNotEmpty() || timer.notes.isNotBlank() || timer.parts.any { it.notes.isNotBlank() }) { Text("Finish & review") }
            if (timer.token.isNotEmpty()) TextButton(onClick = { discard = true }) { Text("Discard session") }
        }
        Panel {
            Text("PIECES IN THIS SESSION",style = MaterialTheme.typography.labelSmall)
            OutlinedButton(onClick = { choose = true }) { Text("Select / add piece") }
            DropdownMenu(choose,{ choose = false }) {
                DropdownMenuItem(text = { Text("Free practice") },onClick = { model.select(null); choose = false })
                library.pieces.forEach { piece -> DropdownMenuItem(text = { Text("${piece.title} · ${piece.composer}") },onClick = { model.select(piece); choose = false }) }
            }
            timer.parts.forEachIndexed { i,p -> if (p.pieceId !in timer.removed) {
                Row {
                    TextButton(onClick = { model.selectPart(i) },Modifier.weight(1f)) { Text("${if (i == timer.active) "▶ " else ""}${p.title} · ${duration(p.durationMillis)}") }
                    if (p.pieceId != null) TextButton(onClick = { model.remove(i) }) { Text("Remove") }
                }
                if (i == timer.active) OutlinedTextField(p.notes,{ model.note(i,it) },label = { Text("Piece notes for this session") },modifier = Modifier.fillMaxWidth())
            } }
        }
        OutlinedTextField(timer.notes,{ text -> model.practice.command { sessionNotes(text,state.value.reflection) } },label = { Text("Session notes") },modifier = Modifier.fillMaxWidth())
        Text("Repertoire notes are separate. Session notes and time are checkpointed locally. Screen-off Smart Practice continues until you pause or stop.")
    }
    if (privacy) AlertDialog(onDismissRequest = { privacy = false },title = { Text("Listen during practice?") },text = { Text("Goldberg analyzes ambient microphone sound locally, including while locked or in another app. No audio is stored or uploaded. A foreground service provides Pause and Stop controls. If notifications are blocked, use Goldberg or Android Active apps to stop. Detection is experimental.") },confirmButton = { TextButton(onClick = {
        privacy = false
        val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= 33) permissions += Manifest.permission.POST_NOTIFICATIONS
        permission.launch(permissions.toTypedArray())
    }) { Text("Enable listening") } },dismissButton = { TextButton(onClick = { privacy = false }) { Text("Cancel") } })
    if (convert) AlertDialog(onDismissRequest = { convert = false },title = { Text("Change timing mode?") },text = { Text("This becomes a Mixed session. Existing credited time is retained; only future time uses the selected mode.") },confirmButton = { TextButton(onClick = { convert = false; start() }) { Text("Convert & continue") } },dismissButton = { TextButton(onClick = { convert = false }) { Text("Cancel") } })
    if (discard) AlertDialog(onDismissRequest = { discard = false },title = { Text("Discard unfinished practice?") },text = { Text("This deletes the unfinished draft and its notes.") },confirmButton = { TextButton(onClick = { model.reset(); discard = false }) { Text("Discard") } },dismissButton = { TextButton(onClick = { discard = false }) { Text("Keep") } })
}
