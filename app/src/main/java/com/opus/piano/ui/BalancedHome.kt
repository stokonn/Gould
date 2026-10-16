package com.opus.piano.ui

import com.opus.piano.i18n.L
import com.opus.piano.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.opus.piano.data.*
import java.time.*

@Composable internal fun BalancedHome(library:LibraryState,name:String,start:()->Unit,choose:()->Unit,piece:(Long)->Unit,history:()->Unit) {
    val a=LocalAppearance.current
    val lifecycle=LocalLifecycleOwner.current.lifecycle
    val now by produceState(LocalDateTime.now(),lifecycle) { lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) { while(true) { value=LocalDateTime.now();kotlinx.coroutines.delay(60000) } } }
    val greeting=when(now.hour) { in 5..11 -> "Good morning";in 12..17 -> "Good afternoon";else -> "Good evening" }
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(JournalSpace.page),verticalArrangement=Arrangement.spacedBy(24.dp)) {
        item { Text(L.fixed(greeting),style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.primary);Text(name.takeUnless { it.isBlank() || it=="Musician" } ?: L.s(R.string.msg_your_practice_journal_ed6e),style=MaterialTheme.typography.headlineLarge) }
        item { Button(onClick=start,modifier=Modifier.fillMaxWidth().heightIn(min=56.dp)) { Text(L.s(R.string.msg_start_practice_ae1b)) };TextButton(onClick=choose) { Text(L.s(R.string.msg_choose_piece_b73e)) } }
        if(library.loading) item { EditorialSkeleton() }
        a.homeSections.filter { it !in a.hiddenHome }.forEach { section -> when(section) {
            "statistics" -> item {
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(16.dp)) {
                    a.metrics.forEach { metric -> Column(Modifier.weight(1f)) {
                        val value=PracticeInsights.metric(metric,library.pieces,library.sessions,now.toLocalDate(),ZoneId.systemDefault())
                        Text(L.fixed(metricLabel(metric)),style=MaterialTheme.typography.bodySmall)
                        Text(if(metric in listOf("today","week","month")) localizedPracticeTime(value) else java.text.NumberFormat.getIntegerInstance().format(value),style=MaterialTheme.typography.headlineMedium)
                    } }
                }
            }
            "recent" -> item {
                Row { Text(L.s(R.string.msg_recently_practiced_99a8),Modifier.weight(1f),style=MaterialTheme.typography.titleLarge);TextButton(onClick=history) { Text(L.s(R.string.msg_view_all_efd8)) } }
                val recent=remember(library.sessions,library.parts,library.pieces) { PracticeInsights.recent(library.pieces,library.sessions,library.parts) }
                if(recent.isEmpty()) Text(L.s(R.string.msg_your_practiced_pieces_will_appear_here_39a5),color=MaterialTheme.colorScheme.onSurfaceVariant)
                recent.forEach { p -> Surface(onClick={piece(p.id)},color=MaterialTheme.colorScheme.background) {
                    Column(Modifier.fillMaxWidth().padding(vertical=12.dp)) { Text(p.title,style=MaterialTheme.typography.titleLarge);Text(p.composer,style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant) }
                };HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant) }
            }
        } }
    }
}
internal fun metricLabel(id:String)=when(id) { "today"->"Today";"week"->"This week";"month"->"This month";"streak"->"Current streak";"sessions"->"Practice sessions";else->"Repertoire pieces" }
