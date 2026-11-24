package com.opus.piano.ui

import com.opus.piano.appContainer

import com.opus.piano.i18n.L
import com.opus.piano.R
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.opus.piano.data.*
import com.opus.piano.data.ActivityHistoryEntry
import com.opus.piano.data.ManualHistoryEntry
import com.opus.piano.data.PracticeHistoryEntry
import com.opus.piano.data.unifiedActivityHistory
import com.opus.piano.ui.ActivityViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.ZoneId
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import kotlinx.coroutines.CancellationException

/** Keyset paging stays bounded and stable even when newer sessions arrive. */
@Composable internal fun PracticeHistoryScreen(back:()->Unit, pieceId:Long?=null, catalogId:String?=null) {
    val dao=(LocalContext.current.appContainer).database.practiceDao()
    var sessions by remember(pieceId,catalogId) { mutableStateOf<List<PracticeSession>>(emptyList()) }
    var parts by remember(pieceId,catalogId) { mutableStateOf<Map<Long,List<SessionPart>>>(emptyMap()) }
    var request by remember(pieceId,catalogId) { mutableIntStateOf(0) }
    var more by remember(pieceId,catalogId) { mutableStateOf(true) }
    var loading by remember { mutableStateOf(true) }
    var failed by remember { mutableStateOf(false) }
    BackHandler(onBack=back)
    LaunchedEffect(pieceId,catalogId,request) {
        loading=true; failed=false
        try {
            val cursor=sessions.lastOrNull()
            val page=dao.historyPage(pieceId,catalogId,cursor?.completedAt ?: Long.MAX_VALUE,cursor?.id ?: Long.MAX_VALUE,31)
            val batch=page.take(30)
            val rows=if(batch.isEmpty()) emptyMap() else dao.historyParts(batch.map { it.id }).groupBy { it.sessionId }
            sessions=(sessions+batch).distinctBy { it.id }; parts=parts+rows; more=page.size>30
        } catch(c:CancellationException) { throw c }
        catch(_:Exception) { failed=true }
        finally { loading=false }
    }
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(JournalSpace.page),verticalArrangement=Arrangement.spacedBy(JournalSpace.small)) {
        item { CatalogBack(L.s(R.string.msg_practice_history_76e7),back) }
        if(!loading && !failed && sessions.isEmpty()) item { Text(L.s(R.string.msg_no_sessions_yet_your_practice_will_appear_he_37e7)) }
        items(sessions,key={it.id}) { s ->
            val entries=parts[s.id].orEmpty()
            val focused=if ((pieceId!=null || catalogId!=null) && entries.isNotEmpty()) entries.filter {
                (pieceId!=null && it.pieceId==pieceId) || (catalogId!=null && it.catalogWorkId==catalogId)
            }.sumOf { it.durationMillis } else if(pieceId==null && catalogId==null || pieceId!=null && s.pieceId==pieceId || catalogId!=null && s.catalogWorkId==catalogId) s.durationMillis else 0L
            SessionRow(s,entries,focused,untimed=(pieceId!=null || catalogId!=null) && focused==0L)
        }
        if(loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        if(failed) item { Text(L.s(R.string.msg_saved_journal_could_not_be_loaded_restart_go_cfce));JournalTextButton(onClick={request++}) { Text(L.s(R.string.practice_retry)) } }
        else if(more && !loading) item { JournalTextButton(onClick={request++}) { Text(L.s(R.string.msg_show_older_sessions_97d8)) } }
    }
}

@Composable internal fun UnifiedActivityHistoryScreen(back: () -> Unit, activity: ActivityViewModel) {
    val app = LocalContext.current.appContainer
    val sessions by app.database.dao().observeSessions().collectAsState(initial = emptyList())
    val activityState by activity.state.collectAsStateWithLifecycle()
    val today = remember { LocalDate.now(ZoneId.systemDefault()) }
    val zone = ZoneId.systemDefault()
    val history = remember(sessions, activityState.manualEntries, today) {
        unifiedActivityHistory(sessions, activityState.manualEntries, today, zone)
    }
    var editEntry by remember { mutableStateOf<ManualActivityEntry?>(null) }
    var deleteEntry by remember { mutableStateOf<ManualActivityEntry?>(null) }
    BackHandler(onBack = back)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(JournalSpace.page), verticalArrangement = Arrangement.spacedBy(JournalSpace.small)) {
        item { CatalogBack(L.s(R.string.activity_history_title), back) }
        if (history.isEmpty()) item { Text(L.s(R.string.msg_no_sessions_yet_your_practice_will_appear_he_37e7)) }
        items(history, key = { it.sourceId }) { entry ->
            when (entry) {
                is PracticeHistoryEntry -> SessionRow(entry.session)
                is ManualHistoryEntry -> {
                    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .4f))) {
                        Row(Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.EditNote, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 8.dp))
                            Column(Modifier.weight(1f).padding(vertical = 10.dp)) {
                                Text(L.fixed("Manual activity"), style = MaterialTheme.typography.titleSmall)
                                Text(formatDate(LocalDate.ofEpochDay(entry.epochDay)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(L.s(R.string.activity_minutes_unset, entry.durationMillis / 60_000L), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { editEntry = entry.entry }) { Icon(Icons.Outlined.Edit, L.s(R.string.activity_edit)) }
                            IconButton(onClick = { deleteEntry = entry.entry }) { Icon(Icons.Outlined.DeleteOutline, L.s(R.string.activity_delete)) }
                        }
                    }
                }
            }
        }
    }
    editEntry?.let { entry ->
        ManualActivityEditDialog(entry, today, activityState.error, onDismiss = { editEntry = null; activity.clearError() }, onSave = { date, minutes -> activity.updateManual(entry.id, date, minutes); editEntry = null })
    }
    deleteEntry?.let { entry ->
        AlertDialog(onDismissRequest = { deleteEntry = null }, title = { Text(L.s(R.string.activity_delete_title)) }, text = { Text(L.s(R.string.activity_delete_message)) },
            confirmButton = { JournalPrimaryButton(onClick = { activity.deleteManual(entry.id); deleteEntry = null }) { Text(L.s(R.string.activity_delete)) } },
            dismissButton = { JournalTextButton(onClick = { deleteEntry = null }) { Text(L.s(R.string.activity_cancel)) } })
    }
}
