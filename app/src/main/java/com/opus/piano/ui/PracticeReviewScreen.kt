package com.opus.piano.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.opus.piano.OpusApplication
import com.opus.piano.data.*
import kotlinx.coroutines.*

@Composable internal fun PracticeReviewScreen(timer: TimerState, model: OpusViewModel) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Heading("A MOMENT TO REFLECT","Practice Review","Your draft is saved locally until you save or explicitly discard it.")
        Text("Elapsed ${duration(timer.wallElapsed)} · credited ${duration(timer.elapsed)}")
        if (timer.mode != "Manual") Text("Detected active ${duration(timer.detectedActive)} · ${timer.mode}")
        Text("Timing: ${timer.timingQuality}")
        timer.intervals.filter { it.creditedMillis == 0L }.groupBy { it.reason }.forEach { (reason,rows) -> Text("$reason · ${duration(rows.sumOf { it.endMillis-it.startMillis })}") }
        timer.parts.forEachIndexed { i,p ->
            Text("${p.title} · ${duration(p.durationMillis)}")
            OutlinedTextField(p.notes,{ model.note(i,it) },label = { Text("Piece session notes") },modifier = Modifier.fillMaxWidth())
        }
        OutlinedTextField(timer.notes,{ text -> model.practice.command { sessionNotes(text,state.value.reflection) } },label = { Text("Session notes") },modifier = Modifier.fillMaxWidth())
        OutlinedTextField(timer.reflection,{ text -> model.practice.command { sessionNotes(state.value.notes,text) } },label = { Text("Reflection (optional)") },modifier = Modifier.fillMaxWidth())
        if (timer.elapsed < 1000) Text("Notes-only entry: this will not count toward practice milestones.")
        Button(onClick = model::saveSession,enabled = !timer.saving) { Text(if (timer.saving) "Saving…" else "Save session") }
        TextButton(onClick = { model.practice.command { pause() } },enabled = !timer.saving) { Text("Return to paused practice") }
    }
}
@Composable internal fun HistoricalReview(session: PracticeSession, original: List<SessionPart>, close: () -> Unit) {
    val app = LocalContext.current.applicationContext as OpusApplication
    val scope = rememberCoroutineScope()
    var notes by remember { mutableStateOf(session.notes) }
    var reflection by remember { mutableStateOf(session.reflection) }
    var parts by remember { mutableStateOf(original) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var intervals by remember { mutableStateOf<List<SessionInterval>>(emptyList()) }
    LaunchedEffect(session.id) { intervals = app.database.practiceDao().intervals(session.id) }
    Dialog(onDismissRequest = { if (!busy) close() }) {
        Surface(shape = MaterialTheme.shapes.medium) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Practice Review")
                Text("Credited ${duration(session.durationMillis)} · ${session.practiceMode}")
                Text(session.elapsedMillis?.let { "Elapsed ${duration(it)} · ${session.timingQuality}" } ?: "Elapsed and detected time were not measured for this historical entry.")
                session.detectedActiveMillis?.let { Text("Detected active ${duration(it)}") }
                intervals.filter { it.creditedMillis == 0L }.groupBy { it.reason }.forEach { (reason,rows) -> Text("$reason · ${duration(rows.sumOf { it.endMillis-it.startMillis })}") }
                parts.forEachIndexed { i,p ->
                    Text("${p.title} · ${duration(p.durationMillis)}")
                    OutlinedTextField(p.notes,{ text -> parts = parts.mapIndexed { j,row -> if (i==j) row.copy(notes=text.take(10000)) else row } },label = { Text("Piece session notes") })
                }
                OutlinedTextField(notes,{ notes=it.take(10000) },label = { Text("Session notes") })
                OutlinedTextField(reflection,{ reflection=it.take(10000) },label = { Text("Reflection") })
                error?.let { Text(it) }
                Button(enabled = !busy,onClick = { busy=true; scope.launch {
                    try { app.practiceRepository.editNotes(session.id,notes,reflection,parts); close() }
                    catch (c: CancellationException) { throw c }
                    catch (_: Exception) { error="Notes could not be saved. Please retry." }
                    finally { busy=false }
                } }) { Text("Save notes") }
                TextButton(onClick = close,enabled = !busy) { Text("Close") }
            }
        }
    }
}
