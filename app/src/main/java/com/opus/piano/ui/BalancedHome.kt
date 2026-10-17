package com.opus.piano.ui

import com.opus.piano.i18n.L
import com.opus.piano.R
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.opus.piano.OpusApplication
import com.opus.piano.data.*
import java.time.*

@Composable internal fun BalancedHome(library:LibraryState,name:String,piece:(Long)->Unit,history:()->Unit) {
    val app=LocalContext.current.applicationContext as OpusApplication
    val lifecycle=LocalLifecycleOwner.current.lifecycle
    val today by produceState(LocalDate.now(),lifecycle) { lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {while(true) {value=LocalDate.now();kotlinx.coroutines.delay(30000)}} }
    val quote by produceState(QuoteLibrary.forDay(today),today) {value=app.quotes.daily(today)}
    JournalViewport {
        Column {Text(L.fixed("Home"),style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.primary);Text(name.takeUnless {it.isBlank()||it=="Musician"} ?: L.s(R.string.msg_your_practice_journal_ed6e),style=MaterialTheme.typography.headlineLarge)}
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(16.dp)) {
            MetricTile(localizedPracticeTime(PracticeInsights.metric("today",library.pieces,library.sessions,today,ZoneId.systemDefault())),L.s(R.string.today_practice),Modifier.weight(1f))
            MetricTile(L.quantity(R.plurals.days_count,currentPracticeStreak(library.sessions,today,ZoneId.systemDefault())),L.fixed("Current streak"),Modifier.weight(1f))
        }
        WeeklyJournalChart(library.sessions,today)
        Column(verticalArrangement=Arrangement.spacedBy(6.dp)) {
            Text(L.s(R.string.daily_quote),style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.primary)
            Text("“${L.s(quote.textResource)}”",style=MaterialTheme.typography.titleMedium)
            Text(L.s(R.string.quote_attribution),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        }
        val recent=remember(library.sessions,library.parts,library.pieces) {PracticeInsights.recent(library.pieces,library.sessions,library.parts)}
        Row(verticalAlignment=Alignment.CenterVertically) {Text(L.fixed("Recently practiced"),Modifier.weight(1f),style=MaterialTheme.typography.titleMedium);JournalTextButton(onClick=history) {Text(L.s(R.string.msg_view_all_efd8))}}
        if(recent.isEmpty()) {
            val latest=library.sessions.filter(AchievementEngine::valid).maxWithOrNull(compareBy<PracticeSession> {it.completedAt}.thenBy {it.id})
            if(latest==null) Text(L.s(R.string.home_no_activity),style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
            else SessionRow(latest,library.parts.filter {it.sessionId==latest.id})
        }
        recent.forEach {p-> Surface(onClick={piece(p.id)},color=MaterialTheme.colorScheme.background) {PieceRowContent(p.title,p.composer,modifier=Modifier.fillMaxWidth().padding(vertical=4.dp))} }
    }
}
@Composable internal fun MetricTile(value:String,label:String,modifier:Modifier=Modifier) {
    Column(modifier,verticalArrangement=Arrangement.spacedBy(4.dp)) {Text(label,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(value,style=MaterialTheme.typography.headlineMedium,fontFamily=MaterialTheme.typography.bodyLarge.fontFamily)}
}
@Composable internal fun WeeklyJournalChart(sessions:List<PracticeSession>,today:LocalDate) {
    val chartColor=MaterialTheme.colorScheme.primary
    val zone=ZoneId.systemDefault()
    val days=(6 downTo 0).map {today.minusDays(it.toLong())}
    val totals=days.map {d->sessions.filter {AchievementEngine.recordedDay(it,zone)==d}.sumOf {it.durationMillis}}
    val peak=totals.maxOrNull()?.coerceAtLeast(1) ?: 1
    Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Text(L.s(R.string.msg_last_7_days_df83),style=MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            days.forEachIndexed {i,d->val label=com.opus.piano.i18n.JournalFormatting.weekday(d,L.locale)
                Column(Modifier.weight(1f).semantics(mergeDescendants=true) {contentDescription="$label: ${localizedPracticeTime(totals[i])}"},horizontalAlignment=Alignment.CenterHorizontally) {
                    Box(Modifier.fillMaxWidth().height(56.dp),contentAlignment=Alignment.BottomCenter) {
                        androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                            val h=if(totals[i]==0L) 0f else maxOf(2.dp.toPx(),size.height*totals[i].toFloat()/peak)
                            if(h>0) drawRoundRect(color=chartColor,topLeft=androidx.compose.ui.geometry.Offset(size.width*.2f,size.height-h),size=androidx.compose.ui.geometry.Size(size.width*.6f,h),cornerRadius=androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()))
                        }
                    }
                    Text(label,style=MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
internal fun metricLabel(id:String)=when(id) {"today"->"Today";"week"->"This week";"month"->"This month";"streak"->"Current streak";"sessions"->"Practice sessions";else->"Repertoire pieces"}
