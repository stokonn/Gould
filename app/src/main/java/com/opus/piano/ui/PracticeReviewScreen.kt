package com.opus.piano.ui

import com.opus.piano.i18n.L
import com.opus.piano.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.window.Dialog
import com.opus.piano.OpusApplication
import com.opus.piano.data.*
import kotlinx.coroutines.*

@Composable internal fun PracticeReviewScreen(timer: TimerState, model: OpusViewModel) {
    val unsaved by model.practice.unsaved.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Heading(L.s(R.string.msg_a_moment_to_reflect_6277),L.s(R.string.msg_practice_review_6b11),L.s(R.string.msg_your_draft_is_saved_locally_until_you_save_o_0e7a))
        if (unsaved) { Text(L.s(R.string.practice_unsaved),color=MaterialTheme.colorScheme.error);TextButton(onClick={model.practice.retryPersistence()}) { Text(L.s(R.string.practice_retry)) } }
        Text(L.s(R.string.msg_elapsed_duration_timer_wallelapsed_credited_b95d, (duration(timer.wallElapsed)), (duration(timer.elapsed))))
        if (timer.mode != "Manual") Text(L.s(R.string.msg_detected_active_duration_timer_detectedactiv_5cb7, (duration(timer.detectedActive)), (L.fixed(timer.mode))))
        Text(L.s(R.string.msg_timing_timer_timingquality_0713, (L.fixed(timer.timingQuality))))
        timer.intervals.filter { it.creditedMillis == 0L }.groupBy { it.reason }.forEach { (reason,rows) -> Text(L.s(R.string.msg_reason_duration_rows_sumof_it_endmillis_it_s_13b7, (L.fixed(reason)), (duration(rows.sumOf { it.endMillis-it.startMillis })))) }
        timer.parts.forEachIndexed { i,p ->
            Text("${p.displayTitle} · ${duration(p.durationMillis)}")
            OutlinedTextField(p.notes,{ model.note(i,it) },label = { Text(L.s(R.string.msg_piece_session_notes_ce2f)) },modifier = Modifier.fillMaxWidth())
        }
        OutlinedTextField(timer.notes,{ text -> model.practice.command { sessionNotes(text,state.value.reflection) } },label = { Text(L.s(R.string.msg_session_notes_a84f)) },modifier = Modifier.fillMaxWidth())
        OutlinedTextField(timer.reflection,{ text -> model.practice.command { sessionNotes(state.value.notes,text) } },label = { Text(L.s(R.string.msg_reflection_optional_8d70)) },modifier = Modifier.fillMaxWidth())
        if (timer.elapsed < 1000) Text(L.s(R.string.msg_notes_only_entry_this_will_not_count_toward_d840))
        Button(onClick = model::saveSession,enabled = !timer.saving) { Text(if (timer.saving) L.s(R.string.msg_saving_56a2) else L.s(R.string.msg_save_session_8c74)) }
        TextButton(onClick = { model.practice.command { pause() } },enabled = !timer.saving) { Text(L.s(R.string.msg_return_to_paused_practice_a77c)) }
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
                SectionTitle(L.s(R.string.msg_practice_review_6b11))
                Text(L.s(R.string.msg_credited_duration_session_durationmillis_ses_1cd0, (duration(session.durationMillis)), (L.fixed(session.practiceMode))))
                Text(session.elapsedMillis?.let { L.s(R.string.msg_elapsed_duration_it_session_timingquality_abc4, (duration(it)), (L.fixed(session.timingQuality))) } ?: L.s(R.string.msg_elapsed_and_detected_time_were_not_measured_79ee))
                session.detectedActiveMillis?.let { Text(L.s(R.string.msg_detected_active_duration_it_c417, (duration(it)))) }
                intervals.filter { it.creditedMillis == 0L }.groupBy { it.reason }.forEach { (reason,rows) -> Text(L.s(R.string.msg_reason_duration_rows_sumof_it_endmillis_it_s_13b7, (L.fixed(reason)), (duration(rows.sumOf { it.endMillis-it.startMillis })))) }
                parts.forEachIndexed { i,p ->
                    Text("${p.displayTitle} · ${duration(p.durationMillis)}")
                    OutlinedTextField(p.notes,{ text -> parts = parts.mapIndexed { j,row -> if (i==j) row.copy(notes=text.take(10000)) else row } },label = { Text(L.s(R.string.msg_piece_session_notes_ce2f)) })
                }
                OutlinedTextField(notes,{ notes=it.take(10000) },label = { Text(L.s(R.string.msg_session_notes_a84f)) })
                OutlinedTextField(reflection,{ reflection=it.take(10000) },label = { Text(L.s(R.string.msg_reflection_fda0)) })
                error?.let { Text(it) }
                Button(enabled = !busy,onClick = { busy=true; scope.launch {
                    try { app.practiceRepository.editNotes(session.id,notes,reflection,parts); close() }
                    catch (c: CancellationException) { throw c }
                    catch (_: Exception) { error=L.s(R.string.msg_notes_could_not_be_saved_please_retry_825a) }
                    finally { busy=false }
                } }) { Text(L.s(R.string.msg_save_notes_ea7d)) }
                TextButton(onClick = close,enabled = !busy) { Text(L.s(R.string.msg_close_bbfa)) }
            }
        }
    }
}
