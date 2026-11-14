package com.opus.piano.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.opus.piano.R
import com.opus.piano.activity.domain.DayActivity
import com.opus.piano.activity.domain.DayKind
import com.opus.piano.activity.domain.manualActivityDates
import com.opus.piano.data.*
import com.opus.piano.i18n.L
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale

@Composable internal fun ActivityScreen(model: ActivityViewModel, customize: () -> Unit) {
    val state by model.state.collectAsStateWithLifecycle()
    val today by produceState(LocalDate.now(java.time.ZoneId.systemDefault())) {
        while (true) { value = LocalDate.now(java.time.ZoneId.systemDefault()); kotlinx.coroutines.delay(30_000) }
    }
    var selectedDay by remember { mutableStateOf<DayActivity?>(null) }
    var selectedStat by remember { mutableIntStateOf(-1) }
    var addEditor by remember { mutableStateOf(false) }
    var deleteEntry by remember { mutableStateOf<ManualActivityEntry?>(null) }
    var goalEditor by remember { mutableStateOf(false) }
    var rewardToClaim by remember { mutableStateOf<ActivityRewardGrant?>(null) }
    var showAllManual by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      Column(Modifier.fillMaxHeight().widthIn(max = 720.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        EditorialHeader(L.s(R.string.activity_title), actions = {
            IconButton(onClick = { goalEditor = true }, enabled = state.snapshot != null, modifier = Modifier.testTag("activity-edit-goal-header")) {
                Icon(Icons.Outlined.Edit, L.s(R.string.activity_edit_goal))
            }
        })
        Text(L.s(R.string.activity_subtitle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

        val graphAccent = state.loadout.firstOrNull { it.category == "GRAPH" }?.cosmeticId?.let(ActivityCosmetics.byId::get)?.let { Color(it.color.toInt()) }
        val rewardUiState = state.cosmeticRewardState
        // This section is driven independently from the summary snapshot so a
        // persisted pending reward cannot be hidden while summary loading fails.
        if (state.snapshot != null || rewardUiState is WeeklyCosmeticRewardState.Claim || rewardUiState is WeeklyCosmeticRewardState.Claimed) {
            Panel(Modifier.testTag("activity-weekly-goal-section")) {
                Text(L.s(R.string.activity_sessions_goal), style = MaterialTheme.typography.titleLarge)
                state.snapshot?.let { summary ->
                    Text(L.s(R.string.activity_goal_minimum), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(L.s(R.string.activity_sessions_progress, summary.totals.sessions, summary.goal.sessions), style = MaterialTheme.typography.titleMedium)
                    LinearProgressIndicator(progress = { (summary.totals.sessions.toFloat() / summary.goal.sessions).coerceIn(0f, 1f) }, Modifier.fillMaxWidth(), color = graphAccent ?: MaterialTheme.colorScheme.primary)
                    Text(if (summary.goal.minutes == null) L.s(R.string.activity_minutes_unset, summary.totals.minutes) else L.s(R.string.activity_minutes_progress, summary.totals.minutes, summary.goal.minutes), style = MaterialTheme.typography.titleMedium)
                    summary.goal.minutes?.let { target -> LinearProgressIndicator(progress = { (summary.totals.minutes.toFloat() / target).coerceIn(0f, 1f) }, Modifier.fillMaxWidth(), color = graphAccent ?: MaterialTheme.colorScheme.secondary) }
                    if (summary.totals.sessions >= summary.goal.sessions) Text(L.s(R.string.activity_goal_met), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    else Text(L.s(R.string.activity_goal_waiting, summary.goal.sessions), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                when (val reward = rewardUiState) {
                    WeeklyCosmeticRewardState.Loading -> Unit
                    WeeklyCosmeticRewardState.Locked -> Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Outlined.Lock, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(32.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(L.s(R.string.activity_reward_locked), style = MaterialTheme.typography.titleSmall)
                            Text(L.s(R.string.activity_reward_locked_description), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    is WeeklyCosmeticRewardState.Claim -> Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(L.s(R.string.activity_reward_available), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                            Text(L.s(R.string.activity_reward_week, formatDate(LocalDate.parse(reward.reward.weekMonday))), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            reward.reward.cosmeticId?.let(ActivityCosmetics.byId::get)?.let { cosmetic ->
                                Text(cosmeticName(cosmetic), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        JournalPrimaryButton(onClick = { rewardToClaim = reward.reward }) {
                            Icon(Icons.Outlined.CardGiftcard, null)
                            Spacer(Modifier.width(6.dp))
                            Text(L.s(R.string.activity_claim_reward))
                        }
                    }
                    is WeeklyCosmeticRewardState.Claimed -> Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Outlined.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(L.s(R.string.activity_reward_claimed), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                            reward.reward.cosmeticId?.let(ActivityCosmetics.byId::get)?.let { Text(cosmeticName(it), style = MaterialTheme.typography.bodySmall) }
                            Text(L.s(R.string.activity_reward_week, formatDate(LocalDate.parse(reward.reward.weekMonday))), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        state.snapshot?.let { summary ->
            Panel {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(L.s(R.string.activity_week_overview), Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                    Text(L.s(R.string.activity_freezes, summary.freezes.balance), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                if (summary.week.beta) Text(L.s(R.string.activity_beta_week), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                WeekOverview(summary)
            }

            Panel {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { model.selectYear(state.year - 1) }, enabled = state.year > 1) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, L.s(R.string.activity_previous_year)) }
                    Text(state.year.toString(), Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    IconButton(onClick = { model.selectYear(state.year + 1) }, enabled = state.year < today.year) { Icon(Icons.Outlined.ChevronRight, L.s(R.string.activity_next_year)) }
                }
                val heatmapAccent = state.loadout.firstOrNull { it.category == "HEATMAP" }?.cosmeticId?.let(ActivityCosmetics.byId::get)?.let { Color(it.color.toInt()) }
                YearHeatmap(state.year, state.yearDays, today, heatmapAccent) { selectedDay = it }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    ActivityLegend(DayKind.TRAINING, L.s(R.string.activity_training))
                    ActivityLegend(DayKind.DAY_OFF, L.s(R.string.activity_day_off))
                    ActivityLegend(DayKind.MISS, L.s(R.string.activity_miss))
                }
            }

            Text(L.s(R.string.activity_statistics), style = MaterialTheme.typography.titleLarge)
            val metrics = listOf(
                L.s(R.string.activity_total_workouts) to NumberFormatActivity.format(summary.allTimeWorkouts.toLong()),
                L.s(R.string.activity_total_minutes) to NumberFormatActivity.format(summary.allTimeMinutes),
                L.s(R.string.activity_current_streak) to L.quantity(R.plurals.activity_week_streak, summary.currentWeeklyStreak),
                L.s(R.string.activity_longest_streak) to L.quantity(R.plurals.activity_week_streak, summary.longestWeeklyStreak),
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                metrics.chunked(2).forEachIndexed { rowIndex, row ->
                    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEachIndexed { columnIndex, (label, value) ->
                            val index = rowIndex * 2 + columnIndex
                            Surface(onClick = { selectedStat = index }, modifier = Modifier.weight(1f).fillMaxHeight().heightIn(min = 88.dp), shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .5f))) {
                                Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.Center) {
                                    Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold))
                                    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            BoxWithConstraints(Modifier.fillMaxWidth()) {
                if (maxWidth < 420.dp) {
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(L.s(R.string.activity_add_manual), style = MaterialTheme.typography.titleLarge)
                        JournalPrimaryButton(onClick = { addEditor = true }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Outlined.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text(L.s(R.string.activity_add))
                        }
                    }
                } else {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(L.s(R.string.activity_add_manual), Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                        JournalPrimaryButton(onClick = { addEditor = true }) {
                            Icon(Icons.Outlined.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text(L.s(R.string.activity_add))
                        }
                    }
                }
            }
            if (state.manualEntries.isEmpty()) Text(L.s(R.string.activity_no_manual_entries), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            val sortedManualEntries = state.manualEntries.sortedByDescending { it.activityDay }
            sortedManualEntries.take(if (showAllManual) sortedManualEntries.size else 12).forEach { entry ->
                Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .4f))) {
                    Row(Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f).padding(vertical = 10.dp)) {
                            Text(formatDate(LocalDate.parse(entry.activityDay)), style = MaterialTheme.typography.titleSmall)
                            Text(L.s(R.string.activity_minutes_unset, entry.durationMillis / 60_000L), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { deleteEntry = entry }) { Icon(Icons.Outlined.DeleteOutline, L.s(R.string.activity_delete)) }
                    }
                }
            }
            if (sortedManualEntries.size > 12) TextButton(onClick = { showAllManual = !showAllManual }) {
                Text(L.s(if (showAllManual) R.string.activity_show_recent else R.string.activity_show_all))
            }

        } ?: if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth()) else JournalSecondaryButton(onClick = model::refresh) { Text(L.s(R.string.activity_title)) }
        Spacer(Modifier.height(8.dp))
      }
    }

    selectedDay?.let { day ->
        val date = LocalDate.ofEpochDay(day.epochDay)
        AlertDialog(onDismissRequest = { selectedDay = null }, title = { Text(L.s(R.string.activity_day_details)) },
            text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(formatDate(date), style = MaterialTheme.typography.titleMedium)
                Text(L.quantity(R.plurals.activity_workout_count, day.workouts))
                Text(L.s(R.string.activity_minutes_unset, day.minutes))
            } },
            confirmButton = { JournalTextButton(onClick = { selectedDay = null }) { Text(L.s(R.string.activity_cancel)) } })
    }
    if (selectedStat >= 0) {
        val summary = state.snapshot
        if (summary != null) {
            val detail = listOf(
                L.s(R.string.activity_total_workouts) to NumberFormatActivity.format(summary.allTimeWorkouts.toLong()),
                L.s(R.string.activity_total_minutes) to NumberFormatActivity.format(summary.allTimeMinutes),
                L.s(R.string.activity_current_streak) to L.quantity(R.plurals.activity_week_streak, summary.currentWeeklyStreak),
                L.s(R.string.activity_longest_streak) to L.quantity(R.plurals.activity_week_streak, summary.longestWeeklyStreak),
            )[selectedStat]
            AlertDialog(onDismissRequest = { selectedStat = -1 }, title = { Text(detail.first) }, text = { Text(detail.second, style = MaterialTheme.typography.headlineMedium) },
                confirmButton = { JournalTextButton(onClick = { selectedStat = -1 }) { Text(L.s(R.string.activity_cancel)) } })
        }
    }
    if (addEditor) ManualActivityDialog(today, state.error, onDismiss = { addEditor = false; model.clearError() }, onAdd = { date, minutes -> model.addManual(date, minutes); addEditor = false })
    if (goalEditor && state.snapshot != null) GoalEditor(state.snapshot, state.goalHistory, model) { goalEditor = false }
    deleteEntry?.let { entry ->
        AlertDialog(onDismissRequest = { deleteEntry = null }, title = { Text(L.s(R.string.activity_delete_title)) }, text = { Text(L.s(R.string.activity_delete_message)) },
            confirmButton = { JournalPrimaryButton(onClick = { model.deleteManual(entry.id); deleteEntry = null }) { Text(L.s(R.string.activity_delete)) } },
            dismissButton = { JournalTextButton(onClick = { deleteEntry = null }) { Text(L.s(R.string.activity_cancel)) } })
    }
    rewardToClaim?.let { reward ->
        RewardClaimDialog(
            reward = reward,
            claim = { model.claimReward(reward.weekMonday) },
            onDismiss = { rewardToClaim = null },
            onCollection = { rewardToClaim = null; customize() },
        )
    }
}

@Composable private fun WeekOverview(summary: ActivitySnapshot) {
    val monday = LocalDate.ofEpochDay(summary.week.monday)
    val today = LocalDate.now()
    val dayMap = summary.days.associateBy { it.epochDay }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        (0L..6L).forEach { offset ->
            val date = monday.plusDays(offset)
            val day = dayMap[date.toEpochDay()]
            val color = day?.kind?.let(::activityColor) ?: MaterialTheme.colorScheme.surfaceVariant
            Column(Modifier.weight(1f).semantics { contentDescription = "${date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", L.locale))}, ${day?.kind?.let(::kindLabel).orEmpty()}" }, horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.fillMaxWidth().height(36.dp).clip(RoundedCornerShape(8.dp)).background(color), contentAlignment = Alignment.Center) {
                    Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.labelMedium, color = if (day != null) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(date.format(DateTimeFormatter.ofPattern("EEEEE", L.locale)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable private fun YearHeatmap(year: Int, days: List<DayActivity>, today: LocalDate, accent: Color?, onDay: (DayActivity) -> Unit) {
    val first = LocalDate.of(year, 1, 1)
    val last = LocalDate.of(year, 12, 31)
    val monday = first.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val sunday = last.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    val weeks = ((sunday.toEpochDay() - monday.toEpochDay() + 1) / 7).toInt()
    val map = days.associateBy { it.epochDay }
    val scroll = rememberScrollState()
    val density = LocalDensity.current
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(Modifier.width(18.dp).padding(top = 2.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            repeat(7) { dayIndex ->
                Text(monday.plusDays(dayIndex.toLong()).format(DateTimeFormatter.ofPattern("EEEEE", L.locale)),
                    Modifier.height(48.dp).wrapContentHeight(Alignment.CenterVertically),
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        BoxWithConstraints(Modifier.weight(1f)) {
            LaunchedEffect(year, today, maxWidth) {
                withFrameNanos { }
                val visibleWeeks = (maxWidth / 50.dp).toInt().coerceAtLeast(1)
                val latestWeek = if (year == today.year) {
                    (ChronoUnit.DAYS.between(monday, minOf(today, last)) / 7L).toInt().coerceIn(0, weeks - 1)
                } else 0
                val leadingWeek = (latestWeek - visibleWeeks + 1).coerceAtLeast(0)
                scroll.scrollTo(with(density) { (50.dp * leadingWeek.toFloat()).roundToPx() })
            }
            Row(Modifier.fillMaxWidth().horizontalScroll(scroll).semantics {
                contentDescription = L.s(R.string.activity_heatmap_accessibility, year)
            }, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(weeks) { weekIndex ->
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        repeat(7) { dayIndex ->
                            val date = monday.plusDays(weekIndex * 7L + dayIndex)
                            val inYear = date.year == year
                            val future = date > today
                            val activity = map[date.toEpochDay()]
                            val visible = inYear && !future && activity != null
                            Box(Modifier.size(48.dp).semantics(mergeDescendants = true) {
                                if (visible) contentDescription = "${formatDate(date)}, ${activity!!.kind.let(::kindLabel)}, ${L.quantity(R.plurals.activity_workout_count, activity.workouts)}, ${L.s(R.string.activity_minutes_unset, activity.minutes)}"
                            }.then(if (visible) Modifier.clickable(role = Role.Button) { onDay(activity!!) } else Modifier), contentAlignment = Alignment.Center) {
                                if (inYear && !future) Box(Modifier.size(46.dp).clip(RoundedCornerShape(4.dp))
                                    .background(if (activity == null) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f) else activityColor(activity.kind))
                                    .then(if (activity != null && accent != null) Modifier.border(BorderStroke(1.dp, accent), RoundedCornerShape(3.dp)) else Modifier))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun ActivityLegend(kind: DayKind, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.semantics { contentDescription = label }) {
        Box(Modifier.size(11.dp).clip(RoundedCornerShape(3.dp)).background(activityColor(kind)))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable internal fun GoalEditor(snapshot: ActivitySnapshot?, history: List<ActivityWeeklyGoal>, model: ActivityViewModel, onDismiss: () -> Unit) {
    val current = snapshot ?: return
    val currentMonday = LocalDate.ofEpochDay(current.week.monday)
    val futureGoal = history.firstOrNull { LocalDate.parse(it.effectiveMonday) > currentMonday }
    var sessions by remember(current.week.monday, futureGoal) { mutableIntStateOf(futureGoal?.requiredSessions ?: current.goal.sessions) }
    var minutes by remember(current.week.monday, futureGoal) { mutableStateOf((futureGoal?.minutesGoal ?: current.goal.minutes)?.toString().orEmpty()) }
    var invalidMinutes by remember(current.week.monday, futureGoal) { mutableStateOf(false) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(L.s(R.string.activity_sessions_goal)) }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(L.s(R.string.activity_goal_minimum), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconButton(onClick = { sessions = (sessions - 1).coerceAtLeast(1) }, enabled = sessions > 1) { Icon(Icons.Outlined.Remove, null) }
                Text(L.s(R.string.activity_sessions_goal_value, sessions), style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { sessions = (sessions + 1).coerceAtMost(7) }, enabled = sessions < 7) { Icon(Icons.Outlined.Add, null) }
            }
            OutlinedTextField(minutes, { minutes = it.filter(Char::isDigit).take(10); invalidMinutes = false }, label = { Text(L.s(R.string.activity_optional_minutes_goal)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, isError = invalidMinutes, modifier = Modifier.fillMaxWidth())
            if (invalidMinutes) Text(L.s(R.string.activity_goal_minutes_invalid), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            Text(L.s(R.string.activity_goal_changes_next_monday), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }, confirmButton = { JournalPrimaryButton(onClick = {
        val value = minutes.toIntOrNull()
        if (minutes.isNotBlank() && (value == null || value <= 0)) invalidMinutes = true
        else { model.changeGoals(sessions, value); onDismiss() }
    }) { Text(L.s(R.string.activity_save_goals)) } },
        dismissButton = { JournalTextButton(onClick = onDismiss) { Text(L.s(R.string.activity_cancel)) } })
}

@Composable private fun ManualActivityDialog(today: LocalDate, error: String?, onDismiss: () -> Unit, onAdd: (LocalDate, Int) -> Unit) {
    val allowedDays = remember(today) { manualActivityDates(today.toEpochDay()).map { LocalDate.ofEpochDay(it) } }
    var date by remember(allowedDays) { mutableStateOf(today.takeIf { it in allowedDays } ?: allowedDays.lastOrNull() ?: today) }
    var minutes by remember { mutableStateOf("") }
    var validation by remember { mutableStateOf<String?>(null) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(L.s(R.string.activity_add_manual)) }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(L.s(R.string.activity_add_date), style = MaterialTheme.typography.labelLarge)
            allowedDays.forEach { candidate ->
                val label = when (candidate) {
                    today -> R.string.activity_date_today
                    today.minusDays(1) -> R.string.activity_date_yesterday
                    else -> R.string.activity_date_two_days_ago
                }
                val description = "${L.s(label)}, ${formatDate(candidate)}"
                Row(Modifier.fillMaxWidth().clickable(role = Role.RadioButton) { date = candidate }.semantics {
                    contentDescription = description
                }, verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = date == candidate, onClick = { date = candidate })
                    Column {
                        Text(L.s(label), style = MaterialTheme.typography.bodyMedium)
                        Text(formatDate(candidate), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            OutlinedTextField(minutes, { minutes = it.filter(Char::isDigit).take(4); validation = null }, label = { Text(L.s(R.string.activity_duration_minutes)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
            validation?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        }
    }, confirmButton = { JournalPrimaryButton(onClick = {
        val value = minutes.toIntOrNull()
        if (value == null || value !in 1..1440) validation = L.s(R.string.activity_duration_invalid)
        else if (date !in allowedDays) validation = L.s(R.string.activity_date_past_only)
        else onAdd(date, value)
    }) { Text(L.s(R.string.activity_add)) } }, dismissButton = { JournalTextButton(onClick = onDismiss) { Text(L.s(R.string.activity_cancel)) } })
}

@Composable private fun RewardClaimDialog(
    reward: ActivityRewardGrant,
    claim: suspend () -> Boolean,
    onDismiss: () -> Unit,
    onCollection: () -> Unit,
) {
    val cosmetic = reward.cosmeticId?.let(ActivityCosmetics.byId::get)
    val burstColors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.secondary)
    val burstHalo = MaterialTheme.colorScheme.primary.copy(alpha = .18f)
    val progress = remember(reward.weekMonday) { Animatable(0f) }
    var attempt by remember(reward.weekMonday) { mutableIntStateOf(0) }
    var complete by remember(reward.weekMonday) { mutableStateOf(false) }
    var claimed by remember(reward.weekMonday) { mutableStateOf(false) }
    LaunchedEffect(reward.weekMonday, attempt) {
        complete = false
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(1100, easing = FastOutSlowInEasing))
        claimed = claim()
        complete = true
    }
    Dialog(onDismissRequest = { if (complete) onDismiss() }) {
        Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
            Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(L.s(R.string.activity_reward_title), style = MaterialTheme.typography.headlineSmall)
                Text(L.s(R.string.activity_reward_week, formatDate(LocalDate.parse(reward.weekMonday))), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Box(Modifier.size(152.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.fillMaxSize()) {
                        if (progress.value < 1f) {
                            repeat(18) { index ->
                                val angle = (index * (Math.PI * 2 / 18)).toFloat()
                                val distance = (18f + (index % 4) * 7f) * progress.value * density
                                val center = androidx.compose.ui.geometry.Offset(
                                    size.width / 2 + kotlin.math.cos(angle) * distance,
                                    size.height / 2 + kotlin.math.sin(angle) * distance,
                                )
                                drawCircle(burstColors[index % burstColors.size], radius = (2.5f + (index % 3)) * density * (1f - progress.value * .35f), center = center)
                            }
                            drawCircle(burstHalo.copy(alpha = 1f - progress.value),
                                radius = size.minDimension * (.18f + .34f * progress.value),
                                center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2), style = Stroke(width = 2.dp.toPx()))
                        }
                    }
                    if (cosmetic != null) CosmeticPreview(cosmetic, Modifier.size(84.dp).graphicsLayer {
                        val scale = .42f + .58f * progress.value
                        scaleX = scale; scaleY = scale; rotationZ = (1f - progress.value) * -135f
                    })
                }
                if (complete && claimed && cosmetic != null) {
                    Text(L.s(R.string.activity_reward_unlocked), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Text(cosmeticName(cosmetic), style = MaterialTheme.typography.titleLarge)
                    Text(rarityLabel(cosmetic.rarity), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                } else if (complete && !claimed) {
                    Text(L.s(R.string.activity_reward_claim_failed), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text(L.s(R.string.activity_reward_unlocking), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (complete && claimed) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                        JournalTextButton(onClick = onCollection) { Text(L.s(R.string.activity_view_collection)) }
                        JournalPrimaryButton(onClick = onDismiss) { Text(L.s(R.string.activity_reward_close)) }
                    }
                } else if (complete) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                        JournalTextButton(onClick = onDismiss) { Text(L.s(R.string.activity_cancel)) }
                        JournalPrimaryButton(onClick = { attempt++ }) { Text(L.s(R.string.activity_reward_try_again)) }
                    }
                }
            }
        }
    }
}

@Composable internal fun CosmeticCollectionScreen(model: ActivityViewModel, back: () -> Unit) {
    val state by model.state.collectAsStateWithLifecycle()
    val ownedIds = state.owned.map { it.cosmeticId }.toSet()
    val activeIds = state.loadout.map { it.cosmeticId }.toSet()
    var selectedId by remember(state.owned, state.loadout) {
        mutableStateOf(activeIds.firstOrNull { it in ownedIds } ?: state.owned.firstOrNull()?.cosmeticId)
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.fillMaxHeight().widthIn(max = 720.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = back) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, L.s(R.string.activity_cancel)) }
                Text(L.s(R.string.activity_collection), style = MaterialTheme.typography.headlineMedium)
            }
            Text(L.s(R.string.activity_collection_unlock_description), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            CosmeticCollection(state.owned, state.loadout, selectedId, onSelect = { selectedId = it }, onApply = { cosmetic -> model.equip(cosmetic.category.name, cosmetic.id) })
        }
    }
}

@Composable private fun CosmeticCollection(
    owned: List<CosmeticOwnership>,
    loadout: List<CosmeticLoadout>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    onApply: (ActivityCosmetic) -> Unit,
) {
    val ownedIds = owned.mapTo(mutableSetOf()) { it.cosmeticId }
    val equipped = loadout.associate { it.category to it.cosmeticId }
    ActivityCosmetics.all.forEach { cosmetic ->
        val isOwned = cosmetic.id in ownedIds
        val active = equipped[cosmetic.category.name] == cosmetic.id
        val selected = selectedId == cosmetic.id
        Surface(shape = MaterialTheme.shapes.medium,
            color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = .3f) else MaterialTheme.colorScheme.surface,
            border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = .45f))) {
            Column(Modifier.fillMaxWidth().then(if (isOwned) Modifier.clickable(role = Role.Button) { onSelect(cosmetic.id) } else Modifier).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CosmeticPreview(cosmetic, Modifier.size(52.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(cosmeticName(cosmetic), style = MaterialTheme.typography.titleSmall)
                        Text(rarityLabel(cosmetic.rarity), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Text(L.s(if (isOwned) R.string.activity_owned else R.string.activity_locked), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (!isOwned) Text(L.s(R.string.activity_cosmetic_unlock_requirement), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        if (selected) Text(L.s(R.string.activity_cosmetic_selected), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        if (active) Text(L.s(R.string.activity_equipped), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
                if (isOwned && selected && !active) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        JournalPrimaryButton(onClick = { onApply(cosmetic) }) {
                            Icon(Icons.Outlined.Check, null)
                            Spacer(Modifier.width(6.dp))
                            Text(L.s(R.string.activity_apply_cosmetic))
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun CosmeticPreview(cosmetic: ActivityCosmetic, modifier: Modifier = Modifier) {
    val color = Color(cosmetic.color.toInt())
    Box(modifier.clip(if (cosmetic.category == CosmeticCategory.AVATAR) CircleShape else RoundedCornerShape(14.dp)).background(color.copy(alpha = .22f)), contentAlignment = Alignment.Center) {
        Text(when (cosmetic.category) {
            CosmeticCategory.AVATAR -> "♫"
            CosmeticCategory.FRAME -> "◉"
            CosmeticCategory.BACKGROUND -> "▧"
            CosmeticCategory.HEATMAP -> "▦"
            CosmeticCategory.GRAPH -> "⌁"
        }, color = color, style = MaterialTheme.typography.headlineMedium)
    }
}

private object NumberFormatActivity {
    fun format(value: Long) = java.text.NumberFormat.getIntegerInstance(L.locale).format(value)
}

private fun formatDate(date: LocalDate) = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(L.locale).format(date)
private fun cosmeticName(cosmetic: ActivityCosmetic) = if (L.locale.language == "cs") cosmetic.nameCs else cosmetic.nameEn
private fun rarityLabel(rarity: CosmeticRarity) = L.s(when (rarity) {
    CosmeticRarity.COMMON -> R.string.activity_rarity_common
    CosmeticRarity.RARE -> R.string.activity_rarity_rare
    CosmeticRarity.EPIC -> R.string.activity_rarity_epic
    CosmeticRarity.LEGENDARY -> R.string.activity_rarity_legendary
})
private fun kindLabel(kind: DayKind) = L.s(when (kind) {
    DayKind.TRAINING -> R.string.activity_training
    DayKind.DAY_OFF -> R.string.activity_day_off
    DayKind.MISS -> R.string.activity_miss
})
private fun activityColor(kind: DayKind) = when (kind) {
    DayKind.TRAINING -> ActivityColors.training
    DayKind.DAY_OFF -> ActivityColors.dayOff
    DayKind.MISS -> ActivityColors.miss
}
