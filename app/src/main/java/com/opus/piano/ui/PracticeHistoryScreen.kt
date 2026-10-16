package com.opus.piano.ui

import com.opus.piano.i18n.L
import com.opus.piano.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.opus.piano.data.*

@Composable internal fun PracticeHistoryScreen(library:LibraryState,back:()->Unit,pieceId:Long?=null) {
    var limit by remember { mutableIntStateOf(30) }
    val ids=if(pieceId==null) null else library.parts.filter { it.pieceId==pieceId }.map { it.sessionId }.toSet()
    val sessions=library.sessions.filter { ids==null || it.id in ids }
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        item { CatalogBack(L.s(R.string.msg_practice_history_76e7),back) }
        if(sessions.isEmpty()) item { Text(L.s(R.string.msg_no_sessions_yet_your_practice_will_appear_he_37e7)) }
        items(sessions.take(limit),key={it.id}) { s -> SessionRow(s,library.summary.partsBySession[s.id].orEmpty()) }
        if(sessions.size>limit) item { TextButton(onClick={limit+=30}) { Text(L.s(R.string.msg_show_older_sessions_97d8)) } }
    }
}
