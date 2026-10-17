package com.opus.piano.ui

import com.opus.piano.i18n.L
import com.opus.piano.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
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
        if (unsaved) { Text(L.s(R.string.practice_unsaved),color=MaterialTheme.colorScheme.error);JournalTextButton(onClick={model.practice.retryPersistence()}) { Text(L.s(R.string.practice_retry)) } }
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
        JournalPrimaryButton(onClick = model::saveSession,enabled = !timer.saving) { Text(if (timer.saving) L.s(R.string.msg_saving_56a2) else L.s(R.string.msg_save_session_8c74)) }
        JournalTextButton(onClick = { model.practice.command { pause() } },enabled = !timer.saving) { Text(L.s(R.string.msg_return_to_paused_practice_a77c)) }
    }
}
@Composable internal fun HistoricalReview(session: PracticeSession, original: List<SessionPart>, close: () -> Unit) {
    val app = LocalContext.current.applicationContext as OpusApplication
    val catalog by app.catalogRepository.snapshot.collectAsStateWithLifecycle(CatalogSnapshot(emptyList(),emptyList()))
    val scope = rememberCoroutineScope()
    var notes by remember { mutableStateOf("") }
    var reflection by remember { mutableStateOf("") }
    var current by remember { mutableStateOf<PracticeSession?>(null) }
    var parts by remember { mutableStateOf<List<SessionPart>>(emptyList()) }
    var links by remember { mutableStateOf<List<SessionPieceLink>>(emptyList()) }
    var pieces by remember { mutableStateOf<List<Piece>>(emptyList()) }
    var busy by remember { mutableStateOf(false) }
    var loaded by remember { mutableStateOf(false) }
    var request by remember { mutableIntStateOf(0) }
    var adding by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var intervals by remember { mutableStateOf<List<SessionInterval>>(emptyList()) }
    suspend fun refreshLinks() { links=app.database.journalEditsDao().links(session.id) }
    LaunchedEffect(session.id,request) {
        try {
            val saved=requireNotNull(app.database.practiceDao().session(session.id))
            val savedParts=app.database.practiceDao().historyParts(listOf(session.id))
            intervals=app.database.practiceDao().intervals(session.id)
            refreshLinks();pieces=app.database.dao().allPieces()
            current=saved;notes=saved.notes;reflection=saved.reflection;parts=savedParts;loaded=true;error=null
        } catch(c:CancellationException) {throw c} catch(_:Exception) {error=L.s(R.string.msg_saved_journal_could_not_be_loaded_restart_go_cfce)}
    }
    fun modify(action:suspend ()->Unit) {
        if(busy) return
        busy=true;scope.launch {try {action();refreshLinks();error=null} catch(c:CancellationException) {throw c} catch(_:Exception) {error=L.s(R.string.ui010_edit_failed)} finally {busy=false}}
    }
    Dialog(onDismissRequest = { if (!busy) close() },properties=androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth=false)) {
        Surface(shape = MaterialTheme.shapes.medium,modifier=Modifier.fillMaxWidth().padding(12.dp)) {
            Column(Modifier.heightIn(max=760.dp).verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement = Arrangement.spacedBy(12.dp)) {
                EditorialHeader(L.s(R.string.ui010_edit_session),back=close)
                if(!loaded) {
                    if(error==null) LinearProgressIndicator(Modifier.fillMaxWidth())
                    else {Text(error!!);JournalTextButton(onClick={request++}) {Text(L.s(R.string.practice_retry))}}
                }
                current?.let { saved ->
                    Text(duration(saved.durationMillis),style=MaterialTheme.typography.headlineLarge,color=MaterialTheme.colorScheme.primary)
                    Text(L.s(R.string.msg_credited_duration_session_durationmillis_ses_1cd0,duration(saved.durationMillis),L.fixed(saved.practiceMode)),style=MaterialTheme.typography.bodySmall)
                    CompactSection(L.s(R.string.msg_my_pieces_f17b))
                    if(parts.isEmpty()) Text("${saved.pieceTitle} · ${duration(saved.durationMillis)}")
                    parts.forEachIndexed {i,p ->
                        JournalCard {
                            Text("${p.displayTitle} · ${duration(p.durationMillis)}",style=MaterialTheme.typography.titleMedium)
                            OutlinedTextField(p.notes,{text->parts=parts.mapIndexed {j,row->if(i==j) row.copy(notes=text.take(10000)) else row}},label={Text(L.s(R.string.msg_piece_session_notes_ce2f))},modifier=Modifier.fillMaxWidth())
                        }
                    }
                    links.forEach {link->JournalCard {Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {Text(link.title);Text(L.s(R.string.ui010_untimed),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
                        JournalTextButton(enabled=!busy,onClick={modify {JournalEdits(app.database).removePiece(saved.id,link.pieceId)}}) {Text(L.s(R.string.ui010_remove))}
                    }}}
                    Text(L.s(R.string.ui010_edit_explanation),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                    JournalSecondaryButton(enabled=!busy,onClick={adding=!adding}) {Text(L.s(R.string.msg_add_piece_2cfc))}
                    if(adding) {
                        OutlinedTextField(query,{query=it},label={Text(L.s(R.string.msg_search_your_repertoire_556a))},modifier=Modifier.fillMaxWidth())
                        val candidates=pieces.filter {p-> parts.none {it.pieceId==p.id || (p.catalogWorkId!=null && it.catalogWorkId==p.catalogWorkId)} && links.none {it.pieceId==p.id || (p.catalogWorkId!=null && it.catalogWorkId==p.catalogWorkId)} && (parts.isNotEmpty() || (saved.pieceId!=p.id && (p.catalogWorkId==null || saved.catalogWorkId!=p.catalogWorkId))) && catalogText(p.title+" "+p.composer).contains(catalogText(query))}
                        Column(Modifier.heightIn(max=260.dp).verticalScroll(rememberScrollState())) {candidates.forEach {p->JournalTextButton(enabled=!busy,onClick={modify {JournalEdits(app.database).addPiece(saved.id,p.id)}},modifier=Modifier.fillMaxWidth()) {
                            Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                                ComposerPortrait(catalog.composers.firstOrNull {catalogText(it.name)==catalogText(p.composer)},Modifier.size(width=40.dp,height=48.dp))
                                Text("${p.title} · ${p.composer}",Modifier.weight(1f),maxLines=2,overflow=androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                Icon(Icons.Outlined.Add,null,tint=MaterialTheme.colorScheme.primary)
                            }
                        }}}
                        if(candidates.isEmpty()) Text(L.s(R.string.msg_no_matching_compositions_391a))
                    }
                    OutlinedTextField(notes,{notes=it.take(10000)},label={Text(L.s(R.string.msg_session_notes_a84f))},modifier=Modifier.fillMaxWidth())
                    OutlinedTextField(reflection,{reflection=it.take(10000)},label={Text(L.s(R.string.msg_reflection_fda0))},modifier=Modifier.fillMaxWidth())
                    intervals.filter {it.creditedMillis==0L}.groupBy {it.reason}.forEach {(reason,rows)->Text(L.s(R.string.msg_reason_duration_rows_sumof_it_endmillis_it_s_13b7,L.fixed(reason),duration(rows.sumOf {it.endMillis-it.startMillis})),style=MaterialTheme.typography.bodySmall)}
                    error?.let {Text(it,color=MaterialTheme.colorScheme.error)}
                    JournalPrimaryButton(enabled=loaded && !busy,onClick={busy=true;scope.launch {try {app.practiceRepository.editNotes(saved.id,notes,reflection,parts);close()} catch(c:CancellationException) {throw c} catch(_:Exception) {error=L.s(R.string.msg_notes_could_not_be_saved_please_retry_825a)} finally {busy=false}}}) {Text(L.s(R.string.msg_save_notes_ea7d))}
                }
                JournalTextButton(onClick=close,enabled=!busy) {Text(L.s(R.string.msg_close_bbfa))}
            }
        }
    }
}
