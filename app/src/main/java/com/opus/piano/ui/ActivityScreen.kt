package com.opus.piano.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.opus.piano.R
import com.opus.piano.activity.domain.DayActivity
import com.opus.piano.activity.domain.DayKind
import com.opus.piano.data.*
import com.opus.piano.i18n.L
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

@Composable internal fun ActivityScreen(model: ActivityViewModel, customize: () -> Unit) {
    val state by model.state.collectAsStateWithLifecycle()
    val today = LocalDate.now()
    var selectedDay by remember { mutableStateOf<DayActivity?>(null) }
    var selectedStat by remember { mutableIntStateOf(-1) }
    var goalEditor by remember { mutableStateOf(false) }
    var addEditor by remember { mutableStateOf(false) }
    var deleteEntry by remember { mutableStateOf<ManualActivityEntry?>(null) }
    var rewardShown by remember(state.pendingReward?.weekMonday) { mutableStateOf(false) }
    var showAllManual by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        EditorialHeader(L.s(R.string.activity_title), actions = { IconButton(onClick = customize) { Icon(Icons.Outlined.CollectionsBookmark, L.s(R.string.activity_view_collection)) } })
        Text(L.s(R.string.activity_subtitle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

        state.snapshot?.let { summary ->
            Panel {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(L.s(R.string.activity_week_overview), Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                    Text(L.s(R.string.activity_freezes, summary.freezes.balance), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                if (summary.week.beta) Text(L.s(R.string.activity_beta_week), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                val graphAccent = state.loadout.firstOrNull { it.category == "GRAPH" }?.cosmeticId?.let(ActivityCosmetics.byId::get)?.let { Color(it.color.toInt()) }
                WeekOverview(summary, graphAccent)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .5f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(L.s(R.string.activity_sessions_goal), Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                    TextButton(onClick = { goalEditor = true }) { Text(L.s(R.string.activity_save_goals)) }
                }
                Text(L.s(R.string.activity_goal_minimum), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (summary.totals.sessions >= summary.goal.sessions) Text(L.s(R.string.activity_goal_met), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                else Text(L.s(R.string.activity_goal_waiting, summary.goal.sessions), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEachIndexed { columnIndex, (label, value) ->
                            val index = rowIndex * 2 + columnIndex
                            Surface(onClick = { selectedStat = index }, modifier = Modifier.weight(1f).heightIn(min = 76.dp), shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .5f))) {
                                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold))
                                    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(L.s(R.string.activity_add_manual), Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                JournalPrimaryButton(onClick = { addEditor = true }) { Text(L.s(R.string.activity_add)) }
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(L.s(R.string.activity_collection), Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = customize) { Text(L.s(R.string.activity_view_collection)) }
            }
            CosmeticCollection(state.owned, state.loadout, model::equip, compact = true)
        } ?: if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth()) else JournalSecondaryButton(onClick = model::refresh) { Text(L.s(R.string.activity_title)) }
        Spacer(Modifier.height(8.dp))
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
    if (goalEditor) GoalEditor(state.snapshot, state.goalHistory, model) { goalEditor = false }
    if (addEditor) ManualActivityDialog(state.snapshot?.firstUseDay, state.error, onDismiss = { addEditor = false; model.clearError() }, onAdd = { date, minutes -> model.addManual(date, minutes); addEditor = false })
    deleteEntry?.let { entry ->
        AlertDialog(onDismissRequest = { deleteEntry = null }, title = { Text(L.s(R.string.activity_delete_title)) }, text = { Text(L.s(R.string.activity_delete_message)) },
            confirmButton = { JournalPrimaryButton(onClick = { model.deleteManual(entry.id); deleteEntry = null }) { Text(L.s(R.string.activity_delete)) } },
            dismissButton = { JournalTextButton(onClick = { deleteEntry = null }) { Text(L.s(R.string.activity_cancel)) } })
    }
    state.pendingReward?.takeIf { !rewardShown }?.let { reward ->
        LaunchedEffect(reward.weekMonday) { rewardShown = false }
        RewardDialog(reward, onClose = { model.dismissReward(reward.weekMonday); rewardShown = true }, onCollection = { model.dismissReward(reward.weekMonday); rewardShown = true; customize() })
    }
}

@Composable private fun WeekOverview(summary: ActivitySnapshot, graphAccent: Color?) {
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
    Text(L.s(R.string.activity_sessions_progress, summary.totals.sessions, summary.goal.sessions), style = MaterialTheme.typography.titleMedium)
    LinearProgressIndicator(progress = { (summary.totals.sessions.toFloat() / summary.goal.sessions).coerceIn(0f, 1f) }, Modifier.fillMaxWidth(), color = graphAccent ?: MaterialTheme.colorScheme.primary)
    Text(if (summary.goal.minutes == null) L.s(R.string.activity_minutes_unset, summary.totals.minutes) else L.s(R.string.activity_minutes_progress, summary.totals.minutes, summary.goal.minutes), style = MaterialTheme.typography.titleMedium)
    summary.goal.minutes?.let { target -> LinearProgressIndicator(progress = { (summary.totals.minutes.toFloat() / target).coerceIn(0f, 1f) }, Modifier.fillMaxWidth(), color = graphAccent ?: MaterialTheme.colorScheme.secondary) }
}

@Composable private fun YearHeatmap(year: Int, days: List<DayActivity>, today: LocalDate, accent: Color?, onDay: (DayActivity) -> Unit) {
    val first = LocalDate.of(year, 1, 1)
    val last = LocalDate.of(year, 12, 31)
    val monday = first.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val sunday = last.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    val weeks = ((sunday.toEpochDay() - monday.toEpochDay() + 1) / 7).toInt()
    val map = days.associateBy { it.epochDay }
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).semantics {
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
                        if (inYear && !future) Box(Modifier.size(12.dp).clip(RoundedCornerShape(3.dp))
                            .background(if (activity == null) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f) else activityColor(activity.kind))
                            .then(if (activity != null && accent != null) Modifier.border(BorderStroke(1.dp, accent), RoundedCornerShape(3.dp)) else Modifier))
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

@Composable private fun GoalEditor(snapshot: ActivitySnapshot?, history: List<ActivityWeeklyGoal>, model: ActivityViewModel, onDismiss: () -> Unit) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun ManualActivityDialog(firstUse: LocalDate?, error: String?, onDismiss: () -> Unit, onAdd: (LocalDate, Int) -> Unit) {
    val today = LocalDate.now()
    var showPicker by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf(today.minusDays(1)) }
    var minutes by remember { mutableStateOf("") }
    var validation by remember { mutableStateOf<String?>(null) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(L.s(R.string.activity_add_manual)) }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = { showPicker = true }, modifier = Modifier.fillMaxWidth()) { Text("${L.s(R.string.activity_add_date)} · ${formatDate(date)}") }
            OutlinedTextField(minutes, { minutes = it.filter(Char::isDigit).take(4); validation = null }, label = { Text(L.s(R.string.activity_duration_minutes)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
            validation?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        }
    }, confirmButton = { JournalPrimaryButton(onClick = {
        val value = minutes.toIntOrNull()
        if (value == null || value !in 1..1440) validation = L.s(R.string.activity_duration_invalid)
        else if (date >= today) validation = L.s(R.string.activity_date_past_only)
        else if (firstUse != null && date < firstUse) validation = L.s(R.string.activity_date_before_start)
        else onAdd(date, value)
    }) { Text(L.s(R.string.activity_add)) } }, dismissButton = { JournalTextButton(onClick = onDismiss) { Text(L.s(R.string.activity_cancel)) } })
    if (showPicker) {
        val state = androidx.compose.material3.rememberDatePickerState(initialSelectedDateMillis = date.toEpochDay() * 86_400_000L, selectableDates = object : androidx.compose.material3.SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val selected = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate()
                return selected < today && (firstUse == null || selected >= firstUse)
            }
            override fun isSelectableYear(year: Int) = year <= today.year && (firstUse == null || year >= firstUse.year)
        })
        DatePickerDialog(onDismissRequest = { showPicker = false }, confirmButton = { TextButton(onClick = {
            state.selectedDateMillis?.let { date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }; showPicker = false
        }) { Text(L.s(R.string.activity_select_date)) } }, dismissButton = { TextButton(onClick = { showPicker = false }) { Text(L.s(R.string.activity_cancel)) } }) { DatePicker(state) }
    }
}

@Composable private fun RewardDialog(reward: ActivityRewardGrant, onClose: () -> Unit, onCollection: () -> Unit) {
    val cosmetic = reward.cosmeticId?.let(ActivityCosmetics.byId::get)
    var revealed by remember(reward.weekMonday) { mutableStateOf(false) }
    LaunchedEffect(reward.weekMonday) { revealed = true }
    val previewScale by animateFloatAsState(if (revealed) 1f else .82f, animationSpec = tween(240), label = "reward-preview")
    AlertDialog(onDismissRequest = onClose, title = { Text(L.s(R.string.activity_reward_title)) }, text = {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            if (cosmetic != null) CosmeticPreview(cosmetic, Modifier.size(84.dp).graphicsLayer { scaleX = previewScale; scaleY = previewScale })
            Text(cosmetic?.let(::cosmeticName) ?: L.s(R.string.activity_collection_complete), style = MaterialTheme.typography.titleLarge)
            if (cosmetic != null) Text(rarityLabel(cosmetic.rarity), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            Text(L.s(if (cosmetic == null) R.string.activity_collection_complete else R.string.activity_reward_received), style = MaterialTheme.typography.bodyMedium)
        }
    }, confirmButton = { JournalPrimaryButton(onClick = onClose) { Text(L.s(R.string.activity_reward_close)) } },
        dismissButton = { if (cosmetic != null) JournalTextButton(onClick = onCollection) { Text(L.s(R.string.activity_view_collection)) } })
}

@Composable internal fun CosmeticCollectionScreen(model: ActivityViewModel, back: () -> Unit) {
    val state by model.state.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = back) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, L.s(R.string.activity_cancel)) }
            Text(L.s(R.string.activity_collection), style = MaterialTheme.typography.headlineMedium)
        }
        Text(L.s(R.string.activity_collection_unlock_description), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        CosmeticCollection(state.owned, state.loadout, model::equip)
    }
}

@Composable private fun CosmeticCollection(owned: List<CosmeticOwnership>, loadout: List<CosmeticLoadout>, equip: (String, String?) -> Unit, compact: Boolean = false) {
    val ownedIds = owned.mapTo(mutableSetOf()) { it.cosmeticId }
    val equipped = loadout.associate { it.category to it.cosmeticId }
    ActivityCosmetics.all.forEach { cosmetic ->
        val isOwned = cosmetic.id in ownedIds
        val active = equipped[cosmetic.category.name] == cosmetic.id
        Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .45f))) {
            Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CosmeticPreview(cosmetic, Modifier.size(if (compact) 40.dp else 52.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(cosmeticName(cosmetic), style = MaterialTheme.typography.titleSmall)
                    Text(rarityLabel(cosmetic.rarity), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(L.s(if (isOwned) R.string.activity_owned else R.string.activity_locked), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (!isOwned) Text(L.s(R.string.activity_cosmetic_unlock_requirement), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (isOwned) {
                    JournalTextButton(onClick = { equip(cosmetic.category.name, if (active) null else cosmetic.id) }) {
                        Text(L.s(if (active) R.string.activity_equipped else R.string.activity_equip))
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
