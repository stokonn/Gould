package com.opus.piano.ui

import com.opus.piano.i18n.L
import com.opus.piano.R
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.opus.piano.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.*
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable internal fun ProfileScreen(state: LibraryState, badges: List<BadgeProgress>, profile: ProfileViewModel, settingsAction: () -> Unit, allBadges: () -> Unit, history: () -> Unit, badgeDetail: (String) -> Unit) {
    val appearance=LocalAppearance.current
    val settings by profile.settings.collectAsStateWithLifecycle()
    val busy by profile.busy.collectAsStateWithLifecycle()
    var edit by rememberSaveable { mutableStateOf(false) }
    var customize by rememberSaveable { mutableStateOf(false) }
    var manage by rememberSaveable { mutableStateOf(false) }
    var period by rememberSaveable { mutableStateOf("Week") }
    var limit by rememberSaveable { mutableIntStateOf(20) }
    val avatar by produceState<android.graphics.Bitmap?>(null, settings.avatar) {
        value = withContext(Dispatchers.Default) { runCatching {
            val bytes = android.util.Base64.decode(settings.avatar, android.util.Base64.DEFAULT)
            android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }.getOrNull() }
    }
    val lifecycle = androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
    val today by produceState(LocalDate.now(), lifecycle) {
        lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
            while (true) { value = LocalDate.now(); kotlinx.coroutines.delay(60_000) }
        }
    }
    val zone = ZoneId.systemDefault()
    val filtered = remember(state.sessions, period, today, zone) { profilePeriod(state.sessions, period, today, zone) }
    val lifetime = state.summary.totalMillis
    val periodTime = remember(filtered) { filtered.sumOf { it.durationMillis } }
    val streak = remember(state.sessions, today, zone) { currentPracticeStreak(state.sessions, today, zone) }
    val partsBySession = state.summary.partsBySession
    val totals = remember(filtered, state.summary.attributed) { val ids = filtered.map { it.id }.toSet(); state.summary.attributed.filter { it.id in ids }.groupBy { it.catalogWorkId?.let { id -> "catalog:$id" } ?: it.pieceId?.let { id -> "piece:$id" } ?: "free" }.values.map { entries -> (if (entries.first().pieceId == null && entries.first().catalogWorkId == null && entries.first().pieceTitle == "Free practice") L.fixed("Free practice") else entries.first().pieceTitle) to entries.sumOf { it.durationMillis } }.sortedByDescending { it.second }.take(5) }
    val showcased = remember(settings.badgeIds, badges) { settings.badgeIds.take(5).mapNotNull { id -> badges.firstOrNull { it.definition.id == id && it.unlocked } } }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Surface(Modifier.size(64.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                    if (avatar != null) Image(avatar!!.asImageBitmap(), L.s(R.string.msg_profile_avatar_1671), contentScale = ContentScale.Crop)
                    else Icon(Icons.Outlined.Person, L.s(R.string.msg_default_avatar_400b), Modifier.padding(16.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(settings.username, style = MaterialTheme.typography.titleLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    TextButton(onClick = { edit = true }, contentPadding = PaddingValues(horizontal = 0.dp)) { Text(L.s(R.string.msg_edit_profile_cd28)) }
                }
                IconButton(onClick = settingsAction) { Icon(Icons.Outlined.Settings, L.fixed("Settings")) }
            }
        }
        item { FlowRow { TextButton(onClick=allBadges) { Text(L.s(R.string.msg_view_all_badges_06e1)) };TextButton(onClick=history) { Text(L.s(R.string.msg_practice_history_76e7)) } } }
        if (state.loading) item { EditorialSkeleton() }
        if (!state.loading) {
        appearance.profileSections.filter { it !in appearance.hiddenProfile && it != "history" }.forEach { section -> when(section) {
        "statistics" -> { item {
            val metrics = mapOf("time" to ("Total practice" to localizedPracticeTime(lifetime)), "pieces" to ("Repertoire pieces" to state.pieces.size.toString()), "sessions" to ("Practice sessions" to state.sessions.count(AchievementEngine::valid).toString()), "streak" to ("Current streak" to L.quantity(R.plurals.days_count,streak)))
            val ordered = StatisticLayout.normalize(settings.statistics.split(','))
            val primary = metrics.getValue(ordered.first())
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(L.fixed(primary.first).uppercase(L.locale), style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                    TextButton(onClick = { customize = true }) { Text(L.s(R.string.msg_customize_239d)) }
                }
                Text(primary.second, style = MaterialTheme.typography.displayMedium.copy(fontFamily = MaterialTheme.typography.bodyLarge.fontFamily), color = MaterialTheme.colorScheme.primary)
                HorizontalDivider()
                ordered.drop(1).forEach { id ->
                    val metric = metrics.getValue(id)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(L.fixed(metric.first), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        Text(metric.second, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
        } "activity" -> { item {
            SectionTitle(L.s(R.string.msg_practice_activity_767e))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Week", "Month", "Year", "All time").forEach { label -> FilterChip(period == label, { period = label; limit = 20 }, label = { Text(L.fixed(label)) }) }
            }
            Text(L.s(R.string.profile_period_summary,localizedPracticeTime(periodTime),L.quantity(R.plurals.sessions_count,filtered.count(AchievementEngine::valid))), style = MaterialTheme.typography.titleMedium)
            Text(when (period) { "Week" -> L.s(R.string.msg_last_7_days_df83); "Month" -> L.s(R.string.msg_last_30_days_6b32); "Year" -> L.s(R.string.msg_last_365_days_254b); else -> L.s(R.string.msg_your_complete_practice_history_da91) }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            ActivityChart(filtered, period, today, zone)
        }
        } "badges" -> { item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(L.s(R.string.msg_badge_showcase_6868), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                TextButton(onClick = { manage = true }) { Text(L.s(R.string.msg_edit_5301)) }
            }
            if (showcased.isEmpty()) Text(L.s(R.string.msg_make_this_space_yours_choose_up_to_five_earn_1116), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            else LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(showcased, key = { it.definition.id }) { badge ->
                    Surface(onClick = { badgeDetail(badge.definition.id) }, shape = MaterialTheme.shapes.medium, modifier = Modifier.width(180.dp)) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(L.s(R.string.msg_no_showcased_indexof_badge_1_a87f, (showcased.indexOf(badge) + 1)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            MedalArtwork(badge, Modifier.size(52.dp))
                            Text(badge.definition.localizedName, style = MaterialTheme.typography.titleLarge)
                            Text(localDateTime(badge.unlockedAt!!), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
        } "frequent" -> {
        item { SectionTitle(L.s(R.string.msg_most_practiced_1b36)) }
        if (totals.isEmpty()) item { Text(L.s(R.string.msg_save_a_practice_session_to_see_your_most_pla_c06c), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        items(totals) { (title, time) ->
            Surface(shape = MaterialTheme.shapes.medium) {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(title, style = MaterialTheme.typography.bodyLarge)
                    Text(localizedPracticeTime(time), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        } } }
        }
    }
    if (customize) StatisticsEditor(settings, busy, profile) { customize = false }
    if (edit) EditProfile(settings, avatar, busy, profile) { edit = false }
    if (manage) ShowcaseEditor(settings, badges, busy, profile) { manage = false }
}

@Composable private fun StatisticsEditor(settings: ProfileSettings, busy: Boolean, profile: ProfileViewModel, dismiss: () -> Unit) {
    val ids = StatisticLayout.normalize(settings.statistics.split(','))
    val names = mapOf("time" to L.s(R.string.msg_total_practice_a63c), "pieces" to L.s(R.string.msg_repertoire_pieces_e09d), "sessions" to L.s(R.string.msg_practice_sessions_60c7), "streak" to L.s(R.string.msg_current_streak_2ff8))
    AlertDialog(onDismissRequest = dismiss, title = { Text(L.s(R.string.msg_your_statistics_502c)) }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(L.s(R.string.msg_the_first_metric_is_prominent_use_the_arrows_1043))
            ids.forEachIndexed { index, id ->
                Text(L.s(R.string.msg_if_index_0_primary_else_names_getvalue_id_4c74, (if (index == 0) L.fixed("Primary · ") else ""), (names.getValue(id))))
                Row {
                    if (index > 0) TextButton(modifier = Modifier.weight(1f), enabled = !busy, onClick = { profile.statistics(listOf(id) + (ids - id)) }) { Text(L.s(R.string.msg_make_primary_81d3)) }
                    IconButton(enabled = index > 0 && !busy, onClick = { val order = ids.toMutableList(); java.util.Collections.swap(order, index, index - 1); profile.statistics(order) }) { Icon(Icons.Outlined.ArrowUpward, L.s(R.string.msg_move_names_id_earlier_11d2, (names[id]))) }
                    IconButton(enabled = index < ids.lastIndex && !busy, onClick = { val order = ids.toMutableList(); java.util.Collections.swap(order, index, index + 1); profile.statistics(order) }) { Icon(Icons.Outlined.ArrowDownward, L.s(R.string.msg_move_names_id_later_4339, (names[id]))) }
                }
            }
        }
    }, confirmButton = { TextButton(onClick = dismiss) { Text(L.s(R.string.msg_done_e9b4)) } })
}

@Composable private fun EditProfile(settings: ProfileSettings, avatar: android.graphics.Bitmap?, busy: Boolean, profile: ProfileViewModel, dismiss: () -> Unit) {
    var name by rememberSaveable(settings.username) { mutableStateOf(settings.username) }
    val photo = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { it?.let(profile::avatar) }
    AlertDialog(onDismissRequest = { if (!busy) dismiss() }, title = { Text(L.s(R.string.msg_edit_profile_cd28)) }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(Modifier.size(72.dp), shape = CircleShape) {
                if (avatar != null) Image(avatar.asImageBitmap(), L.s(R.string.msg_current_avatar_8634), contentScale = ContentScale.Crop)
                else Icon(Icons.Outlined.Person, L.s(R.string.msg_default_avatar_400b), Modifier.padding(16.dp))
            }
            OutlinedTextField(name, { name = it.take(80) }, label = { Text(L.s(R.string.msg_display_name_c787)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Button(onClick = { profile.username(name) }, enabled = !busy && name.isNotBlank() && name.trim() != settings.username) { Text(L.s(R.string.msg_save_name_8ce6)) }
            OutlinedButton(onClick = { photo.launch("image/*") }, enabled = !busy) { Text(L.s(R.string.msg_choose_avatar_7fa8)) }
            if (settings.avatar.isNotBlank()) TextButton(onClick = profile::clearAvatar, enabled = !busy) { Text(L.s(R.string.msg_use_default_avatar_62c9)) }
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        }
    }, confirmButton = { TextButton(onClick = dismiss, enabled = !busy) { Text(L.s(R.string.msg_done_e9b4)) } })
}

@Composable private fun ShowcaseEditor(settings: ProfileSettings, badges: List<BadgeProgress>, busy: Boolean, profile: ProfileViewModel, dismiss: () -> Unit) {
    AlertDialog(onDismissRequest = dismiss, title = { Text(L.s(R.string.msg_edit_showcase_settings_badgeids_size_5_872e, (settings.badgeIds.size))) }, text = {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(settings.badgeIds, key = { "selected:$it" }) { id ->
                val index = settings.badgeIds.indexOf(id)
                Column {
                    Text(badges.firstOrNull { it.definition.id == id }?.definition?.localizedName ?: id)
                    Row {
                        IconButton(enabled = index > 0 && !busy, onClick = { val ids = settings.badgeIds.toMutableList(); java.util.Collections.swap(ids, index, index - 1); profile.showcase(ids) }) { Icon(Icons.Outlined.ArrowUpward, L.s(R.string.msg_move_badge_earlier_ba86)) }
                        IconButton(enabled = index < settings.badgeIds.lastIndex && !busy, onClick = { val ids = settings.badgeIds.toMutableList(); java.util.Collections.swap(ids, index, index + 1); profile.showcase(ids) }) { Icon(Icons.Outlined.ArrowDownward, L.s(R.string.msg_move_badge_later_aa77)) }
                        IconButton(enabled = !busy, onClick = { profile.showcase(settings.badgeIds - id) }) { Icon(Icons.Outlined.Close, L.s(R.string.msg_remove_badge_from_showcase_e306)) }
                    }
                }
            }
            items(badges.filter { it.unlocked && it.definition.id !in settings.badgeIds }, key = { it.definition.id }) { badge ->
                OutlinedButton(onClick = { profile.showcase(settings.badgeIds + badge.definition.id) }, enabled = !busy && settings.badgeIds.size < 5) { Text(L.s(R.string.msg_add_badge_definition_name_ca2c, (badge.definition.localizedName))) }
            }
            if (badges.none { it.unlocked }) item { Text(L.s(R.string.msg_your_earned_badges_will_be_available_here_af_2f62)) }
        }
    }, confirmButton = { TextButton(onClick = dismiss) { Text(L.s(R.string.msg_done_e9b4)) } })
}

@Composable private fun ActivityChart(sessions: List<PracticeSession>, period: String, today: LocalDate, zone: ZoneId) {
    val bins = remember(sessions, period, today, zone) { activityBins(sessions, period, today, zone) }
    if (sessions.sumOf { it.durationMillis } == 0L) {
        Surface(shape = MaterialTheme.shapes.medium) { Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(L.s(R.string.msg_a_fresh_page_7041), style = MaterialTheme.typography.titleMedium)
            Text(L.s(R.string.msg_no_practice_recorded_in_this_period_start_a_68b4), color = MaterialTheme.colorScheme.onSurfaceVariant)
        } }
        return
    }
    val peak = bins.maxOf { it.second }.coerceAtLeast(1L)
    val fontScale = LocalDensity.current.fontScale
    Surface(shape = MaterialTheme.shapes.medium) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(L.s(R.string.msg_peak_compactpracticetime_peak_7989, (localizedPracticeTime(peak))), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            BoxWithConstraints {
                val barWidth = maxOf(32.dp * fontScale, (maxWidth - 6.dp * (bins.size - 1)) / bins.size)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    bins.forEachIndexed { index, (label, time) ->
                        val display = when (period) {
                            "Year", "All time" -> runCatching { YearMonth.parse(label).format(DateTimeFormatter.ofPattern("MMM\nyy").withLocale(L.locale)) }.getOrDefault(label)
                            "Week" -> com.opus.piano.i18n.JournalFormatting.weekday(today.minusDays((6-index).toLong()),L.locale)
                            else -> label
                        }
                        Column(Modifier.width(barWidth).semantics(mergeDescendants = true) { contentDescription = L.s(R.string.msg_period_ending_label_duration_time_practice_ac3c, (display), (duration(time))) }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.fillMaxWidth().height(100.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = .05f), MaterialTheme.shapes.small), contentAlignment = Alignment.BottomCenter) {
                                if (time > 0) Box(Modifier.fillMaxWidth().height(maxOf(2.dp, 100.dp * (time.toFloat() / peak))).background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small))
                            }
                            Text(display, style = MaterialTheme.typography.labelSmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }
            }
            Text(if (period == "Month") L.s(R.string.msg_five_day_totals_labels_show_the_ending_day_01e3) else if (period == "All time") L.s(R.string.msg_grouped_monthly_totals_labels_show_the_endin_1c56) else if (period == "Year") L.s(R.string.monthly_partial) else L.s(R.string.daily_totals), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
