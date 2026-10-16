package com.opus.piano.ui

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

@Composable internal fun ProfileScreen(state: LibraryState, badges: List<BadgeProgress>, profile: ProfileViewModel, badgeDetail: (String) -> Unit) {
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
    val totals = remember(filtered, state.summary.attributed) { val ids = filtered.map { it.id }.toSet(); state.summary.attributed.filter { it.id in ids }.groupBy { it.catalogWorkId?.let { id -> "catalog:$id" } ?: it.pieceId?.let { id -> "piece:$id" } ?: "free" }.values.map { entries -> entries.first().pieceTitle to entries.sumOf { it.durationMillis } }.sortedByDescending { it.second }.take(5) }
    val showcased = remember(settings.badgeIds, badges) { settings.badgeIds.mapNotNull { id -> badges.firstOrNull { it.definition.id == id && it.unlocked } } }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Surface(Modifier.size(64.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                    if (avatar != null) Image(avatar!!.asImageBitmap(), "Profile avatar", contentScale = ContentScale.Crop)
                    else Icon(Icons.Outlined.Person, "Default avatar", Modifier.padding(16.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(settings.username, style = MaterialTheme.typography.titleLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    TextButton(onClick = { edit = true }, contentPadding = PaddingValues(horizontal = 0.dp)) { Text("Edit Profile") }
                }
            }
        }
        if (state.loading) item { EditorialSkeleton() }
        if (!state.loading) {
        item {
            val metrics = mapOf("time" to ("Total practice" to compactPracticeTime(lifetime)), "pieces" to ("Repertoire pieces" to state.pieces.size.toString()), "sessions" to ("Practice sessions" to state.sessions.size.toString()), "streak" to ("Current streak" to "$streak ${if (streak == 1) "day" else "days"}"))
            val ordered = StatisticLayout.normalize(settings.statistics.split(','))
            val primary = metrics.getValue(ordered.first())
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(primary.first.uppercase(), style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                    TextButton(onClick = { customize = true }) { Text("Customize") }
                }
                Text(primary.second, style = MaterialTheme.typography.displayMedium.copy(fontFamily = MaterialTheme.typography.bodyLarge.fontFamily), color = MaterialTheme.colorScheme.primary)
                HorizontalDivider()
                ordered.drop(1).forEach { id ->
                    val metric = metrics.getValue(id)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(metric.first, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        Text(metric.second, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
        item {
            SectionTitle("Practice activity")
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Week", "Month", "Year", "All time").forEach { label -> FilterChip(period == label, { period = label; limit = 20 }, label = { Text(label) }) }
            }
            Text("${compactPracticeTime(periodTime)} · ${filtered.size} ${if (filtered.size == 1) "session" else "sessions"}", style = MaterialTheme.typography.titleMedium)
            Text(when (period) { "Week" -> "Last 7 days"; "Month" -> "Last 30 days"; "Year" -> "Last 365 days"; else -> "Your complete practice history" }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            ActivityChart(filtered, period, today, zone)
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Badge showcase", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                TextButton(onClick = { manage = true }) { Text("Edit") }
            }
            if (showcased.isEmpty()) Text("Make this space yours. Choose up to five earned badges using Edit.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            else LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(showcased, key = { it.definition.id }) { badge ->
                    Surface(onClick = { badgeDetail(badge.definition.id) }, shape = MaterialTheme.shapes.medium, modifier = Modifier.width(180.dp)) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("NO. ${showcased.indexOf(badge) + 1}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            MedalArtwork(badge, Modifier.size(52.dp))
                            Text(badge.definition.name, style = MaterialTheme.typography.titleLarge)
                            Text(localDateTime(badge.unlockedAt!!), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
        item { SectionTitle("Most practiced") }
        if (totals.isEmpty()) item { Text("Save a practice session to see your most-played compositions here.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        items(totals) { (title, time) ->
            Surface(shape = MaterialTheme.shapes.medium) {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(title, style = MaterialTheme.typography.bodyLarge)
                    Text(compactPracticeTime(time), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        item { SectionTitle("Session history") }
        if (filtered.isEmpty()) item { Text("No sessions in this period yet. Your next practice will appear here.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        items(filtered.take(limit), key = { "session:${it.id}" }) { SessionRow(it, partsBySession[it.id].orEmpty()) }
        if (filtered.size > limit) item { OutlinedButton(onClick = { limit += 20 }) { Text("Show older sessions") } }
        }
    }
    if (customize) StatisticsEditor(settings, busy, profile) { customize = false }
    if (edit) EditProfile(settings, avatar, busy, profile) { edit = false }
    if (manage) ShowcaseEditor(settings, badges, busy, profile) { manage = false }
}

@Composable private fun StatisticsEditor(settings: ProfileSettings, busy: Boolean, profile: ProfileViewModel, dismiss: () -> Unit) {
    val ids = StatisticLayout.normalize(settings.statistics.split(','))
    val names = mapOf("time" to "Total practice", "pieces" to "Repertoire pieces", "sessions" to "Practice sessions", "streak" to "Current streak")
    AlertDialog(onDismissRequest = dismiss, title = { Text("Your statistics") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("The first metric is prominent. Use the arrows to order the supporting statistics.")
            ids.forEachIndexed { index, id ->
                Text("${if (index == 0) "Primary · " else ""}${names.getValue(id)}")
                Row {
                    if (index > 0) TextButton(modifier = Modifier.weight(1f), enabled = !busy, onClick = { profile.statistics(listOf(id) + (ids - id)) }) { Text("Make primary") }
                    IconButton(enabled = index > 0 && !busy, onClick = { val order = ids.toMutableList(); java.util.Collections.swap(order, index, index - 1); profile.statistics(order) }) { Icon(Icons.Outlined.ArrowUpward, "Move ${names[id]} earlier") }
                    IconButton(enabled = index < ids.lastIndex && !busy, onClick = { val order = ids.toMutableList(); java.util.Collections.swap(order, index, index + 1); profile.statistics(order) }) { Icon(Icons.Outlined.ArrowDownward, "Move ${names[id]} later") }
                }
            }
        }
    }, confirmButton = { TextButton(onClick = dismiss) { Text("Done") } })
}

@Composable private fun EditProfile(settings: ProfileSettings, avatar: android.graphics.Bitmap?, busy: Boolean, profile: ProfileViewModel, dismiss: () -> Unit) {
    var name by rememberSaveable(settings.username) { mutableStateOf(settings.username) }
    val photo = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { it?.let(profile::avatar) }
    AlertDialog(onDismissRequest = { if (!busy) dismiss() }, title = { Text("Edit Profile") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(Modifier.size(72.dp), shape = CircleShape) {
                if (avatar != null) Image(avatar.asImageBitmap(), "Current avatar", contentScale = ContentScale.Crop)
                else Icon(Icons.Outlined.Person, "Default avatar", Modifier.padding(16.dp))
            }
            OutlinedTextField(name, { name = it.take(80) }, label = { Text("Display name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Button(onClick = { profile.username(name) }, enabled = !busy && name.isNotBlank() && name.trim() != settings.username) { Text("Save name") }
            OutlinedButton(onClick = { photo.launch("image/*") }, enabled = !busy) { Text("Choose avatar") }
            if (settings.avatar.isNotBlank()) TextButton(onClick = profile::clearAvatar, enabled = !busy) { Text("Use default avatar") }
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        }
    }, confirmButton = { TextButton(onClick = dismiss, enabled = !busy) { Text("Done") } })
}

@Composable private fun ShowcaseEditor(settings: ProfileSettings, badges: List<BadgeProgress>, busy: Boolean, profile: ProfileViewModel, dismiss: () -> Unit) {
    AlertDialog(onDismissRequest = dismiss, title = { Text("Edit showcase · ${settings.badgeIds.size}/5") }, text = {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(settings.badgeIds, key = { "selected:$it" }) { id ->
                val index = settings.badgeIds.indexOf(id)
                Column {
                    Text(badges.firstOrNull { it.definition.id == id }?.definition?.name ?: id)
                    Row {
                        IconButton(enabled = index > 0 && !busy, onClick = { val ids = settings.badgeIds.toMutableList(); java.util.Collections.swap(ids, index, index - 1); profile.showcase(ids) }) { Icon(Icons.Outlined.ArrowUpward, "Move badge earlier") }
                        IconButton(enabled = index < settings.badgeIds.lastIndex && !busy, onClick = { val ids = settings.badgeIds.toMutableList(); java.util.Collections.swap(ids, index, index + 1); profile.showcase(ids) }) { Icon(Icons.Outlined.ArrowDownward, "Move badge later") }
                        IconButton(enabled = !busy, onClick = { profile.showcase(settings.badgeIds - id) }) { Icon(Icons.Outlined.Close, "Remove badge from showcase") }
                    }
                }
            }
            items(badges.filter { it.unlocked && it.definition.id !in settings.badgeIds }, key = { it.definition.id }) { badge ->
                OutlinedButton(onClick = { profile.showcase(settings.badgeIds + badge.definition.id) }, enabled = !busy && settings.badgeIds.size < 5) { Text("Add ${badge.definition.name}") }
            }
            if (badges.none { it.unlocked }) item { Text("Your earned badges will be available here after your first achievement.") }
        }
    }, confirmButton = { TextButton(onClick = dismiss) { Text("Done") } })
}

@Composable private fun ActivityChart(sessions: List<PracticeSession>, period: String, today: LocalDate, zone: ZoneId) {
    val bins = remember(sessions, period, today, zone) { activityBins(sessions, period, today, zone) }
    if (sessions.sumOf { it.durationMillis } == 0L) {
        Surface(shape = MaterialTheme.shapes.medium) { Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("A fresh page", style = MaterialTheme.typography.titleMedium)
            Text("No practice recorded in this period. Start a session when you’re ready.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } }
        return
    }
    val peak = bins.maxOf { it.second }.coerceAtLeast(1L)
    val fontScale = LocalDensity.current.fontScale
    Surface(shape = MaterialTheme.shapes.medium) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Peak ${compactPracticeTime(peak)}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            BoxWithConstraints {
                val barWidth = maxOf(32.dp * fontScale, (maxWidth - 6.dp * (bins.size - 1)) / bins.size)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    bins.forEach { (label, time) ->
                        val display = when (period) {
                            "Year", "All time" -> runCatching { YearMonth.parse(label).format(DateTimeFormatter.ofPattern("MMM\nyy")) }.getOrDefault(label)
                            else -> label
                        }
                        Column(Modifier.width(barWidth).semantics(mergeDescendants = true) { contentDescription = "Period ending $label: ${duration(time)} practice" }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.fillMaxWidth().height(100.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = .05f), MaterialTheme.shapes.small), contentAlignment = Alignment.BottomCenter) {
                                if (time > 0) Box(Modifier.fillMaxWidth().height(maxOf(2.dp, 100.dp * (time.toFloat() / peak))).background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small))
                            }
                            Text(display, style = MaterialTheme.typography.labelSmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }
            }
            Text(if (period == "Month") "Five-day totals · labels show the ending day" else if (period == "All time") "Grouped monthly totals · labels show the ending month" else if (period == "Year") "Monthly totals · includes partial boundary months" else "Daily totals", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
