package com.opus.piano.ui

import com.opus.piano.i18n.L
import com.opus.piano.R
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

internal fun localDateTime(timestamp: Long): String = com.opus.piano.i18n.JournalFormatting.dateTime(timestamp,L.locale,ZoneId.systemDefault())
private fun localDate(timestamp: Long): String = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(L.locale).withZone(ZoneId.systemDefault()).format(Instant.ofEpochMilli(timestamp))

@OptIn(ExperimentalLayoutApi::class)
@Composable fun BadgesScreen(model: ExperienceViewModel, detail: (String) -> Unit) {
    val badges by model.badges.collectAsStateWithLifecycle()
    val error by model.error.collectAsStateWithLifecycle()
    var filter by rememberSaveable { mutableStateOf("All") }
    var sort by rememberSaveable { mutableStateOf("Collection") }
    val unlocked = badges.count { it.unlocked }
    val matches = remember(badges, filter, sort) {
        val filtered = badges.filter { when (filter) { "Unlocked" -> it.unlocked; "Locked" -> !it.unlocked; "Practice" -> it.definition.category == BadgeCategory.PRACTICE; "Repertoire" -> it.definition.category == BadgeCategory.REPERTOIRE; "Composers" -> it.definition.category == BadgeCategory.COMPOSERS; else -> true } }
        when (sort) { "Rarity" -> filtered.sortedByDescending { it.definition.rarity.ordinal }; "Name" -> filtered.sortedBy { it.definition.localizedName }; "Recently unlocked" -> filtered.sortedByDescending { it.unlockedAt ?: Long.MIN_VALUE }; else -> filtered }
    }
    val minCard = if (LocalDensity.current.fontScale > 1.35f) 180.dp else 136.dp
    LazyVerticalGrid(columns = GridCells.Adaptive(minCard), modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item(span = { GridItemSpan(maxLineSpan) }) { Heading(L.s(R.string.msg_the_permanent_collection_1719), L.s(R.string.msg_badges_2c57), L.s(R.string.msg_your_musical_achievements_496e)) }
        error?.let { message -> item(span = { GridItemSpan(maxLineSpan) }) { CatalogFeedback(message) { model.retryEvaluation() } } }
        item(span = { GridItemSpan(maxLineSpan) }) {
            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SmallStatistic("$unlocked", "Unlocked")
                SmallStatistic("${AchievementRegistry.definitions.size}", "Available")
                SmallStatistic("${if (badges.isEmpty()) 0 else unlocked * 100 / badges.size}%", "Collection")
            }
        }
        if (badges.isEmpty() && error == null) item(span = { GridItemSpan(maxLineSpan) }) { EditorialSkeleton() }
        if (unlocked > 0) item(span = { GridItemSpan(maxLineSpan) }) { RecentlyUnlocked(badges, detail) }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionTitle(L.s(R.string.msg_the_collection_3e91))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("All", "Unlocked", "Locked", "Practice", "Repertoire", "Composers").forEach { label -> FilterChip(filter == label, onClick = { filter = label }, label = { Text(L.fixed(label)) }) } }
                Choice(L.s(R.string.msg_sort_adc4), listOf("Collection", "Name", "Rarity", "Recently unlocked"), sort) { sort = it }
            }
        }
        if (matches.isEmpty() && badges.isNotEmpty()) item(span = { GridItemSpan(maxLineSpan) }) { Panel { Text(L.s(R.string.msg_no_achievements_in_this_selection_yet_5a00), color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        items(matches, key = { it.definition.id }) { badge ->
            Surface(onClick = { detail(badge.definition.id) }, shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    androidx.compose.animation.Crossfade(badge.unlocked, label = "Badge unlock") { unlocked ->
                        MedalArtwork(badge.copy(unlockedAt = if (unlocked) badge.unlockedAt else null), Modifier.size(100.dp))
                    }
                    Text(badge.definition.localizedName, style = MaterialTheme.typography.titleMedium)
                    Text(L.fixed(badge.definition.rarity.label), style = MaterialTheme.typography.labelSmall, color = if (badge.unlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    if (badge.unlocked) Text(L.s(R.string.msg_unlocked_localdate_badge_unlockedat_5f9a, (localDate(badge.unlockedAt!!))), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    else {
                        Text(badge.definition.localizedRequirement, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        LinearProgressIndicator(progress = { badge.fraction }, modifier = Modifier.fillMaxWidth().height(3.dp))
                        Text(badge.localizedProgressText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        SectionTitle(L.s(R.string.msg_recently_unlocked_4c22))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(recent, key = { it.definition.id }) { badge ->
                Surface(onClick = { detail(badge.definition.id) }, shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface) {
                    Row(Modifier.width(250.dp).padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MedalArtwork(badge, Modifier.size(64.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(badge.definition.localizedName, style = MaterialTheme.typography.titleMedium)
                            Text(L.fixed(badge.definition.rarity.label), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
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
        CatalogBack(L.s(R.string.msg_the_badge_collection_2ae7), back)
        if (badge == null) { Text(if (badges.isEmpty()) L.s(R.string.msg_loading_achievement_789b) else L.s(R.string.msg_this_achievement_is_not_in_the_current_colle_a8c7)); return@Column }
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { MedalArtwork(badge, Modifier.sizeIn(maxWidth = 224.dp, maxHeight = 224.dp).fillMaxWidth().aspectRatio(1f)) }
        Heading(L.fixed(badge.definition.rarity.label), badge.definition.localizedName, badge.definition.localizedDescription)
        Panel {
            Text(if (badge.unlocked) L.s(R.string.msg_permanently_earned_4658) else L.s(R.string.msg_to_unlock_6d3e), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Text(badge.definition.localizedRequirement)
            if (badge.unlocked) {
                Text(L.s(R.string.msg_unlocked_localdatetime_badge_unlockedat_5e87, (localDateTime(badge.unlockedAt!!))), color = MaterialTheme.colorScheme.primary)
                Text(L.s(R.string.msg_zoneid_systemdefault_id_first_verified_unloc_caf7, (ZoneId.systemDefault().id)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(L.s(R.string.msg_current_recorded_progress_0454), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(badge.localizedProgressText)
            LinearProgressIndicator(progress = { badge.fraction }, modifier = Modifier.fillMaxWidth())
        }
        if (badge.definition.metric == AchievementMetric.STREAK) Text(L.s(R.string.msg_progress_uses_your_longest_recorded_streak_m_0e50), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (badge.definition.category == BadgeCategory.COMPOSERS) Text(L.s(R.string.msg_composer_identity_uses_catalogue_ids_or_exac_7961), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (badge.unlocked) Text(L.s(R.string.msg_this_badge_remains_earned_when_repertoire_or_0722), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
