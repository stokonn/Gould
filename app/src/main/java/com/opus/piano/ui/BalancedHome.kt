package com.opus.piano.ui

import com.opus.piano.i18n.L
import com.opus.piano.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.opus.piano.OpusApplication
import com.opus.piano.data.*
import java.time.*

@Composable internal fun BalancedHome(library:LibraryState,name:String,piece:(Long)->Unit,history:()->Unit,settings:()->Unit) {
    val app=LocalContext.current.applicationContext as OpusApplication
    val lifecycle=LocalLifecycleOwner.current.lifecycle
    val today by produceState(LocalDate.now(),lifecycle) {lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {while(true) {value=LocalDate.now();kotlinx.coroutines.delay(30000)}}}
    val quote by produceState(QuoteLibrary.forDay(today),today) {value=app.quotes.daily(today)}
    var quoteExpanded by remember {mutableStateOf(false)}
    val fontScale=LocalDensity.current.fontScale
    val catalog by app.catalogRepository.snapshot.collectAsState(initial=CatalogSnapshot(emptyList(),emptyList()))
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val availableWidth=maxWidth
        val recentLimit=if(maxHeight>=720.dp && fontScale<=1.15f) 3 else 2
        JournalViewport {
            val greeting:@Composable ()->Unit={Column(verticalArrangement=Arrangement.spacedBy(4.dp)) {
                Text(L.s(when(LocalTime.now().hour) {in 5..11->R.string.ui010_morning;in 12..17->R.string.ui010_afternoon;else->R.string.ui010_evening}),style=MaterialTheme.typography.bodyMedium.copy(fontSize=MaterialTheme.typography.bodyMedium.fontSize * 1.06f),color=MaterialTheme.colorScheme.onSurfaceVariant)
                Text(name.takeUnless {it.isBlank()||it=="Musician"} ?: L.s(R.string.ui010_pianist),style=MaterialTheme.typography.headlineLarge.copy(fontSize=MaterialTheme.typography.headlineLarge.fontSize * 1.04f),maxLines=2,overflow=TextOverflow.Ellipsis)
                Text(L.s(R.string.ui010_home_support),style=MaterialTheme.typography.bodySmall.copy(fontSize=MaterialTheme.typography.bodySmall.fontSize * 1.06f),color=MaterialTheme.colorScheme.onSurfaceVariant)
            }}
            val quotation:@Composable ()->Unit={JournalCard(Modifier.clickable(onClickLabel=L.s(R.string.ui010_read_quote),role=Role.Button) {quoteExpanded=true},padding=PaddingValues(12.dp)) {
                Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Outlined.FormatQuote,null,Modifier.size(20.dp),tint=MaterialTheme.colorScheme.primary)
                    Column(verticalArrangement=Arrangement.spacedBy(6.dp),modifier=Modifier.semantics(mergeDescendants=true) {contentDescription=L.s(quote.textResource)+" · "+L.s(R.string.quote_attribution)}) {
                        Text(L.s(quote.textResource),style=MaterialTheme.typography.bodySmall,maxLines=4,overflow=TextOverflow.Ellipsis)
                        Text("— Robert Schumann",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.primary)
                    }
                }
            }}
            if(availableWidth<330.dp || fontScale>1.2f) {greeting();quotation()}
            else Row(horizontalArrangement=Arrangement.spacedBy(12.dp),verticalAlignment=Alignment.CenterVertically) {
                Box(Modifier.weight(1.05f)) {greeting()};Box(Modifier.weight(1f)) {quotation()}
            }
            Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                JournalCard(Modifier.weight(1f)) {
                    Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {Icon(Icons.Outlined.CalendarToday,null,Modifier.size(20.dp),tint=MaterialTheme.colorScheme.primary);Text(L.s(R.string.today_practice),style=MaterialTheme.typography.bodySmall)}
                    Text(localizedPracticeTime(PracticeInsights.metric("today",library.pieces,library.sessions,today,ZoneId.systemDefault())),style=MaterialTheme.typography.headlineMedium.copy(fontFamily=MaterialTheme.typography.bodyLarge.fontFamily,fontFeatureSettings="tnum"))
                }
                JournalCard(Modifier.weight(1f)) {
                    Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {Icon(Icons.Outlined.LocalFireDepartment,null,Modifier.size(20.dp),tint=MaterialTheme.colorScheme.primary);Text(L.fixed("Current streak"),style=MaterialTheme.typography.bodySmall)}
                    Text(L.quantity(R.plurals.days_count,currentPracticeStreak(library.sessions,today,ZoneId.systemDefault())),style=MaterialTheme.typography.headlineMedium.copy(fontFamily=MaterialTheme.typography.bodyLarge.fontFamily,fontFeatureSettings="tnum"))
                }
            }
            JournalCard {WeeklyJournalChart(library.sessions,today,history)}
            Column(verticalArrangement=Arrangement.spacedBy(4.dp)) {
                CompactSection(L.fixed("Recently practiced"),L.s(R.string.msg_view_all_efd8),history)
                val recent=library.sessions.filter(AchievementEngine::valid).sortedWith(compareByDescending<PracticeSession> {it.completedAt}.thenByDescending {it.id}).take(recentLimit)
                if(recent.isEmpty()) JournalCard {Text(L.s(R.string.home_no_activity),style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)}
                else JournalCard(padding=PaddingValues(horizontal=10.dp,vertical=2.dp)) {
                    recent.forEachIndexed {index,session->
                        val parts=library.parts.filter {it.sessionId==session.id}
                        val ids=parts.mapNotNull {it.pieceId}.distinct().ifEmpty {listOfNotNull(session.pieceId)}
                        val saved=ids.singleOrNull()?.let {id->library.pieces.firstOrNull {it.id==id}}
                        var review by remember(session.id) {mutableStateOf(false)}
                        if(review) HistoricalReview(session,parts) {review=false}
                        val title=parts.filter {it.durationMillis>0}.joinToString(" · ") {it.displayTitle}.ifBlank {if(session.pieceTitle=="Free practice") L.fixed("Free practice") else session.pieceTitle}
                        val composer=parts.firstOrNull()?.composer.orEmpty()
                        val artist=catalog.composers.firstOrNull {catalogText(it.name)==catalogText(composer)}
                        Row(Modifier.fillMaxWidth().clickable(role=Role.Button) {if(saved!=null) piece(saved.id) else review=true}.heightIn(min=60.dp).padding(vertical=8.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                            if(artist!=null) ComposerPortrait(artist,Modifier.size(width=38.dp,height=46.dp)) else Surface(shape=MaterialTheme.shapes.small,color=MaterialTheme.colorScheme.primary.copy(alpha=.12f)) {Icon(Icons.Outlined.MusicNote,null,Modifier.size(42.dp).padding(9.dp),tint=MaterialTheme.colorScheme.primary)}
                            Column(Modifier.weight(1f)) {Text(title,style=MaterialTheme.typography.bodyMedium,maxLines=2,overflow=TextOverflow.Ellipsis);if(composer.isNotBlank()) Text(composer,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
                            Column(horizontalAlignment=Alignment.End) {Text(localizedPracticeTime(session.durationMillis),style=MaterialTheme.typography.bodySmall);Text(Instant.ofEpochMilli(session.completedAt).atZone(ZoneId.systemDefault()).format(java.time.format.DateTimeFormatter.ofPattern("d MMM").withLocale(L.locale)),style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
                        }
                        if(index<recent.lastIndex) HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant.copy(alpha=.4f))
                    }
                }
            }
        }
    }
    if(quoteExpanded) AlertDialog(onDismissRequest={quoteExpanded=false},title={Text(L.s(R.string.daily_quote))},text={Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {Text(L.s(quote.textResource));Text(L.s(R.string.quote_attribution),style=MaterialTheme.typography.bodySmall)}},confirmButton={JournalTextButton(onClick={quoteExpanded=false}) {Text(L.s(R.string.msg_close_bbfa))}})
}
@Composable internal fun MetricTile(value:String,label:String,modifier:Modifier=Modifier) {
    Column(modifier,verticalArrangement=Arrangement.spacedBy(4.dp)) {Text(label,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(value,style=MaterialTheme.typography.headlineMedium.copy(fontFamily=MaterialTheme.typography.bodyLarge.fontFamily,fontFeatureSettings="tnum"))}
}
@Composable internal fun WeeklyJournalChart(sessions:List<PracticeSession>,today:LocalDate,history:()->Unit={}) {
    val color=MaterialTheme.colorScheme.primary
    val zone=ZoneId.systemDefault();val days=(6 downTo 0).map {today.minusDays(it.toLong())}
    val totals=days.map {day->sessions.filter {AchievementEngine.valid(it) && AchievementEngine.recordedDay(it,zone)==day}.sumOf {it.durationMillis}}
    val peak=totals.maxOrNull()?.coerceAtLeast(1) ?: 1
    Column(verticalArrangement=Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment=Alignment.CenterVertically) {Text(L.s(R.string.ui010_weekly_activity),Modifier.weight(1f),style=MaterialTheme.typography.titleSmall);JournalTextButton(onClick=history,contentPadding=PaddingValues(0.dp)) {Text(localizedPracticeTime(totals.sum()),style=MaterialTheme.typography.labelSmall);Icon(Icons.Outlined.ChevronRight,null,Modifier.size(16.dp))}}
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            days.forEachIndexed {i,day->val label=com.opus.piano.i18n.JournalFormatting.weekday(day,L.locale)
                Column(Modifier.weight(1f).semantics(mergeDescendants=true) {contentDescription="$label: ${localizedPracticeTime(totals[i])}"},horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.fillMaxWidth().height(60.dp),contentAlignment=Alignment.BottomCenter) {
                        androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                            if(totals[i]>0) {val height=maxOf(2.dp.toPx(),size.height*totals[i].toFloat()/peak);drawRoundRect(color,androidx.compose.ui.geometry.Offset(size.width*.15f,size.height-height),androidx.compose.ui.geometry.Size(size.width*.7f,height),androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()))}
                        }
                    }
                    Text(label,style=MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
internal fun metricLabel(id:String)=when(id) {"today"->"Today";"week"->"This week";"month"->"This month";"streak"->"Current streak";"sessions"->"Practice sessions";else->"Repertoire pieces"}
