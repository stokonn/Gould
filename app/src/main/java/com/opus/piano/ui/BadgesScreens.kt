package com.opus.piano.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.opus.piano.data.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

internal fun localDateTime(timestamp: Long): String = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(Locale.getDefault()).withZone(ZoneId.systemDefault()).format(Instant.ofEpochMilli(timestamp))
private fun localDate(timestamp: Long): String = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault()).withZone(ZoneId.systemDefault()).format(Instant.ofEpochMilli(timestamp))

@OptIn(ExperimentalLayoutApi::class)
@Composable fun BadgesScreen(model: ExperienceViewModel, detail: (String) -> Unit) {
    val badges by model.badges.collectAsStateWithLifecycle()
    val error by model.error.collectAsStateWithLifecycle()
    var filter by rememberSaveable { mutableStateOf("All") }
    var sort by rememberSaveable { mutableStateOf("Collection") }
    val unlocked = badges.count { it.unlocked }
    val matches = remember(badges, filter, sort) {
        val filtered = badges.filter { when (filter) { "Unlocked" -> it.unlocked; "Locked" -> !it.unlocked; "Practice" -> it.definition.category == BadgeCategory.PRACTICE; "Repertoire" -> it.definition.category == BadgeCategory.REPERTOIRE; "Composers" -> it.definition.category == BadgeCategory.COMPOSERS; else -> true } }
        when (sort) { "Rarity" -> filtered.sortedByDescending { it.definition.rarity.ordinal }; "Name" -> filtered.sortedBy { it.definition.name }; "Recently unlocked" -> filtered.sortedByDescending { it.unlockedAt ?: Long.MIN_VALUE }; else -> filtered }
    }
    val minCard = if (LocalDensity.current.fontScale > 1.35f) 180.dp else 136.dp
    LazyVerticalGrid(columns = GridCells.Adaptive(minCard), modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item(span = { GridItemSpan(maxLineSpan) }) { Heading("THE PERMANENT COLLECTION", "Badges", "Your musical achievements") }
        error?.let { message -> item(span = { GridItemSpan(maxLineSpan) }) { CatalogFeedback(message) { model.retryEvaluation() } } }
        item(span = { GridItemSpan(maxLineSpan) }) {
            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SmallStatistic("$unlocked", "Unlocked")
                SmallStatistic("${AchievementRegistry.definitions.size}", "Available")
                SmallStatistic("${if (badges.isEmpty()) 0 else unlocked * 100 / badges.size}%", "Collection")
            }
        }
        if (badges.isEmpty() && error == null) item(span = { GridItemSpan(maxLineSpan) }) { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        if (unlocked > 0) item(span = { GridItemSpan(maxLineSpan) }) { RecentlyUnlocked(badges, detail) }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionTitle("The collection")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("All", "Unlocked", "Locked", "Practice", "Repertoire", "Composers").forEach { label -> FilterChip(filter == label, onClick = { filter = label }, label = { Text(label) }) } }
                Choice("Sort", listOf("Collection", "Name", "Rarity", "Recently unlocked"), sort) { sort = it }
            }
        }
        if (matches.isEmpty() && badges.isNotEmpty()) item(span = { GridItemSpan(maxLineSpan) }) { Panel { Text("No achievements in this selection yet.", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        items(matches, key = { it.definition.id }) { badge ->
            Surface(onClick = { detail(badge.definition.id) }, shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    androidx.compose.animation.Crossfade(badge.unlocked, label = "Badge unlock") { unlocked ->
                        MedalArtwork(badge.copy(unlockedAt = if (unlocked) badge.unlockedAt else null), Modifier.size(100.dp))
                    }
                    Text(badge.definition.name, style = MaterialTheme.typography.titleMedium)
                    Text(badge.definition.rarity.label, style = MaterialTheme.typography.labelSmall, color = if (badge.unlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    if (badge.unlocked) Text("Unlocked ${localDate(badge.unlockedAt!!)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    else {
                        Text(badge.definition.requirement, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        LinearProgressIndicator(progress = { badge.fraction }, modifier = Modifier.fillMaxWidth().height(3.dp))
                        Text(badge.progressText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable internal fun RecentlyUnlocked(badges: List<BadgeProgress>, detail: (String) -> Unit, compact: Boolean = false) {
    val recent = remember(badges) { badges.filter { it.unlocked }.sortedByDescending { it.unlockedAt }.take(if (compact) 2 else 5) }
    if (recent.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle("Recently Unlocked")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(recent, key = { it.definition.id }) { badge ->
                Surface(onClick = { detail(badge.definition.id) }, shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface) {
                    Row(Modifier.width(250.dp).padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MedalArtwork(badge, Modifier.size(64.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(badge.definition.name, style = MaterialTheme.typography.titleMedium)
                            Text(badge.definition.rarity.label, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                            Text(localDate(badge.unlockedAt!!), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable fun BadgeDetailScreen(id: String, model: ExperienceViewModel, back: () -> Unit) {
    val badges by model.badges.collectAsStateWithLifecycle()
    val badge = badges.firstOrNull { it.definition.id == id }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        CatalogBack("The badge collection", back)
        if (badge == null) { Text(if (badges.isEmpty()) "Loading achievement…" else "This achievement is not in the current collection."); return@Column }
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { MedalArtwork(badge, Modifier.sizeIn(maxWidth = 224.dp, maxHeight = 224.dp).fillMaxWidth().aspectRatio(1f)) }
        Heading(badge.definition.rarity.label, badge.definition.name, badge.definition.description)
        Panel {
            Text(if (badge.unlocked) "PERMANENTLY EARNED" else "TO UNLOCK", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Text(badge.definition.requirement)
            if (badge.unlocked) {
                Text("Unlocked · ${localDateTime(badge.unlockedAt!!)}", color = MaterialTheme.colorScheme.primary)
                Text("${ZoneId.systemDefault().id} · First verified unlock time", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("Current recorded progress", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(badge.progressText)
            LinearProgressIndicator(progress = { badge.fraction }, modifier = Modifier.fillMaxWidth())
        }
        if (badge.definition.metric == AchievementMetric.STREAK) Text("Progress uses your longest recorded streak. Multiple sessions on one calendar day count once. New sessions keep their completion-day time zone; older sessions were assigned a day using the time zone at upgrade.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (badge.definition.category == BadgeCategory.COMPOSERS) Text("Composer identity uses catalogue IDs, or exact recognized full names for manual entries. Ambiguous surnames do not count.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (badge.unlocked) Text("This badge remains earned when repertoire or practice records change. Current progress reflects the records available now.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
