package com.opus.piano.ui

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
import com.opus.piano.OpusApplication
import com.opus.piano.data.*
import kotlinx.coroutines.CancellationException

/** Keyset paging stays bounded and stable even when newer sessions arrive. */
@Composable internal fun PracticeHistoryScreen(back:()->Unit, pieceId:Long?=null, catalogId:String?=null) {
    val dao=(LocalContext.current.applicationContext as OpusApplication).database.practiceDao()
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
            }.sumOf { it.durationMillis } else s.durationMillis
            SessionRow(s,entries,focused)
        }
        if(loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        if(failed) item { Text(L.s(R.string.msg_saved_journal_could_not_be_loaded_restart_go_cfce));JournalTextButton(onClick={request++}) { Text(L.s(R.string.practice_retry)) } }
        else if(more && !loading) item { JournalTextButton(onClick={request++}) { Text(L.s(R.string.msg_show_older_sessions_97d8)) } }
    }
}
