package com.opus.piano.ui

import com.opus.piano.appContainer

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.opus.piano.data.*
import java.time.*

@Composable internal fun BalancedHome(library:LibraryState,name:String,piece:(Long)->Unit,history:()->Unit,settings:()->Unit,activity:ActivityViewModel) {
    val activityState by activity.state.collectAsStateWithLifecycle()
    val app=LocalContext.current.appContainer
    val lifecycle=LocalLifecycleOwner.current.lifecycle
    val today by produceState(LocalDate.now(),lifecycle) {lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {while(true) {value=LocalDate.now();kotlinx.coroutines.delay(30000)}}}
    val quote by produceState(QuoteLibrary.forDay(today),today) {value=app.quotes.daily(today)}
    var quoteExpanded by remember {mutableStateOf(false)}
    val fontScale=LocalDensity.current.fontScale
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val availableWidth=maxWidth
        JournalViewport {
            val greeting:@Composable ()->Unit={Column(verticalArrangement=Arrangement.spacedBy(4.dp)) {
                Text(L.s(when(LocalTime.now().hour) {in 5..11->R.string.ui010_morning;in 12..17->R.string.ui010_afternoon;else->R.string.ui010_evening}),style=MaterialTheme.typography.bodyMedium.copy(fontSize=MaterialTheme.typography.bodyMedium.fontSize * 1.06f),color=MaterialTheme.colorScheme.onSurfaceVariant)
                Text(name.takeUnless {it.isBlank()||it=="Musician"} ?: L.s(R.string.ui010_pianist),style=MaterialTheme.typography.headlineLarge.copy(fontSize=MaterialTheme.typography.headlineLarge.fontSize * 1.04f),maxLines=2,overflow=TextOverflow.Ellipsis)
                Text(L.s(R.string.ui010_home_support),style=MaterialTheme.typography.bodySmall.copy(fontSize=MaterialTheme.typography.bodySmall.fontSize * 1.06f),color=MaterialTheme.colorScheme.onSurfaceVariant)
            }}
            val quotation:@Composable ()->Unit={NatureQuoteCard(Modifier.clickable(onClickLabel=L.s(R.string.ui010_read_quote),role=Role.Button) {quoteExpanded=true}) {
                Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Outlined.FormatQuote,null,Modifier.size(20.dp),tint=MaterialTheme.colorScheme.primary)
                    Column(verticalArrangement=Arrangement.spacedBy(6.dp),modifier=Modifier.semantics(mergeDescendants=true) {contentDescription=L.s(quote.textResource)+" · "+L.s(R.string.quote_attribution)}) {
                        Text(L.s(quote.textResource),style=MaterialTheme.typography.bodySmall,maxLines=4,overflow=TextOverflow.Ellipsis)
                        Text("— Robert Schumann",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.primary)
                    }
                }
            }}
            if(availableWidth<330.dp || fontScale>1.2f) {TourAnchor("home") { greeting() };quotation()}
            else Row(horizontalArrangement=Arrangement.spacedBy(12.dp),verticalAlignment=Alignment.CenterVertically) {
                Box(Modifier.weight(1.05f)) {TourAnchor("home") { greeting() }};Box(Modifier.weight(1f)) {quotation()}
            }
            Row(Modifier.height(IntrinsicSize.Min),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                JournalCard(Modifier.weight(1f).fillMaxHeight()) {
                    Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {Icon(Icons.Outlined.CalendarToday,null,Modifier.size(20.dp),tint=MaterialTheme.colorScheme.primary);Text(L.s(R.string.today_practice),style=MaterialTheme.typography.bodySmall,maxLines=2,overflow=TextOverflow.Ellipsis)}
                    Text(localizedPracticeTime(activityState.snapshot?.todayDurationMillis ?: PracticeInsights.metric("today",library.pieces,library.sessions,today,ZoneId.systemDefault())),style=MaterialTheme.typography.headlineMedium.copy(fontFamily=MaterialTheme.typography.bodyLarge.fontFamily,fontFeatureSettings="tnum"),maxLines=2,overflow=TextOverflow.Ellipsis)
                }
                JournalCard(Modifier.weight(1f).fillMaxHeight()) {
                    Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {Icon(Icons.Outlined.LocalFireDepartment,null,Modifier.size(20.dp),tint=MaterialTheme.colorScheme.primary);Text(L.s(R.string.activity_current_streak),style=MaterialTheme.typography.bodySmall,maxLines=2,overflow=TextOverflow.Ellipsis)}
                    Text(L.quantity(R.plurals.activity_week_streak,activityState.snapshot?.currentWeeklyStreak ?: 0),style=MaterialTheme.typography.headlineMedium.copy(fontFamily=MaterialTheme.typography.bodyLarge.fontFamily,fontFeatureSettings="tnum"),maxLines=2,overflow=TextOverflow.Ellipsis)
                }
                JournalCard(Modifier.weight(1f).fillMaxHeight()) {
                    Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {Icon(Icons.Outlined.LocalFireDepartment,null,Modifier.size(20.dp),tint=MaterialTheme.colorScheme.primary);Text(L.s(R.string.activity_daily_streak),style=MaterialTheme.typography.bodySmall,maxLines=2,overflow=TextOverflow.Ellipsis)}
                    Text(L.quantity(R.plurals.days_count,activityState.snapshot?.dailyStreak ?: 0),style=MaterialTheme.typography.headlineMedium.copy(fontFamily=MaterialTheme.typography.bodyLarge.fontFamily,fontFeatureSettings="tnum"),maxLines=2,overflow=TextOverflow.Ellipsis)
                }
            }
            JournalCard {WeeklyJournalChart(activityState.snapshot?.recentDailyDurationMillis,library.sessions,today,history)}
            activityState.snapshot?.weekly?.let { weekly ->
                JournalCard {
                    Text(L.s(R.string.home_week_summary,
                        L.quantity(R.plurals.sessions_count,weekly.sessions),weekly.minutes,
                        L.quantity(R.plurals.home_active_days,weekly.activeDays)),
                        style=MaterialTheme.typography.bodySmall.copy(fontSize=if(androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp<360) 11.sp else 12.sp),maxLines=1,softWrap=false,overflow=TextOverflow.Ellipsis)
                    Text(L.s(R.string.home_last_week_summary,
                        L.quantity(R.plurals.sessions_count,weekly.previousSessions),weekly.previousMinutes,
                        L.quantity(R.plurals.home_active_days,weekly.previousActiveDays)),
                        style=MaterialTheme.typography.labelSmall.copy(fontSize=if(androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp<360) 10.sp else 11.sp),color=MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines=1,softWrap=false,overflow=TextOverflow.Ellipsis)
                }
            }
        }
    }
    if(quoteExpanded) AlertDialog(onDismissRequest={quoteExpanded=false},title={Text(L.s(R.string.daily_quote))},text={Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {Text(L.s(quote.textResource));Text(L.s(R.string.quote_attribution),style=MaterialTheme.typography.bodySmall)}},confirmButton={JournalTextButton(onClick={quoteExpanded=false}) {Text(L.s(R.string.msg_close_bbfa))}})
}
@Composable internal fun MetricTile(value:String,label:String,modifier:Modifier=Modifier) {
    Column(modifier,verticalArrangement=Arrangement.spacedBy(4.dp)) {Text(label,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(value,style=MaterialTheme.typography.headlineMedium.copy(fontFamily=MaterialTheme.typography.bodyLarge.fontFamily,fontFeatureSettings="tnum"))}
}
@Composable internal fun WeeklyJournalChart(activityDurations:List<Long>?,sessions:List<PracticeSession>,today:LocalDate,history:()->Unit={}) {
    val color=MaterialTheme.colorScheme.primary
    val fontScale=LocalDensity.current.fontScale
    val compactWeekdays=androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp<360 || fontScale>1.2f
    val zone=ZoneId.systemDefault();val days=(6 downTo 0).map {today.minusDays(it.toLong())}
    val totals=activityDurations ?: days.map {day->sessions.filter {AchievementEngine.valid(it) && AchievementEngine.recordedDay(it,zone)==day}.sumOf {it.durationMillis}}
    val peak=totals.maxOrNull()?.coerceAtLeast(1) ?: 1
    Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment=Alignment.CenterVertically) {Text(L.s(R.string.ui010_weekly_activity),Modifier.weight(1f),style=MaterialTheme.typography.titleSmall);JournalTextButton(onClick=history,contentPadding=PaddingValues(0.dp)) {Text(localizedPracticeTime(totals.sum()),style=MaterialTheme.typography.labelSmall);Icon(Icons.Outlined.ChevronRight,null,Modifier.size(16.dp))}}
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(4.dp)) {
            days.forEachIndexed {i,day->val label=com.opus.piano.i18n.JournalFormatting.weekday(day,L.locale)
                Column(Modifier.weight(1f).semantics(mergeDescendants=true) {contentDescription="$label: ${localizedPracticeTime(totals[i])}"},horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.fillMaxWidth().height(60.dp),contentAlignment=Alignment.BottomCenter) {
                        androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                            if(totals[i]>0) {val height=maxOf(2.dp.toPx(),size.height*totals[i].toFloat()/peak);drawRoundRect(color,androidx.compose.ui.geometry.Offset(size.width*.15f,size.height-height),androidx.compose.ui.geometry.Size(size.width*.7f,height),androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()))}
                        }
                    }
                    Text(if(compactWeekdays)label.take(2).trimEnd('.', ',') else label,
                        style=MaterialTheme.typography.labelSmall,maxLines=1,softWrap=false,overflow=TextOverflow.Clip)
                }
            }
        }
    }
}
internal fun metricLabel(id:String)=when(id) {"today"->"Today";"week"->"This week";"month"->"This month";"streak"->"Current streak";"sessions"->"Practice sessions";else->"Repertoire pieces"}
