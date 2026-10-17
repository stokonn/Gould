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

@Composable internal fun ProfileScreen(state: LibraryState, badges: List<BadgeProgress>, profile: ProfileViewModel, settingsAction: () -> Unit, allBadges: () -> Unit, history: () -> Unit, badgeDetail: (String) -> Unit) {
    val settings by profile.settings.collectAsStateWithLifecycle()
    val busy by profile.busy.collectAsStateWithLifecycle()
    val googleAccount by profile.googleAccount.collectAsStateWithLifecycle()
    val identityContext = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(googleAccount) { if(googleAccount != null) profile.refreshGooglePhoto() }
    var edit by rememberSaveable { mutableStateOf(false) }
    var customize by rememberSaveable { mutableStateOf(false) }
    val avatar by produceState<android.graphics.Bitmap?>(null,settings.avatar) {
        value=withContext(Dispatchers.Default) { runCatching {
            val bytes=android.util.Base64.decode(settings.avatar,android.util.Base64.DEFAULT)
            android.graphics.BitmapFactory.decodeByteArray(bytes,0,bytes.size)
        }.getOrNull() }
    }
    val app=androidx.compose.ui.platform.LocalContext.current.applicationContext as com.opus.piano.OpusApplication
    val catalog by app.catalogRepository.snapshot.collectAsStateWithLifecycle(CatalogSnapshot(emptyList(),emptyList()))
    val artist=catalog.composers.firstOrNull { catalogText(it.name)==catalogText(settings.favoriteArtist) }
    val showcased=settings.badgeIds.mapNotNull {id->badges.firstOrNull {it.definition.id==id && it.unlocked}}.take(5)
    JournalViewport {
        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            Surface(onClick={edit=true},modifier=Modifier.size(80.dp),shape=CircleShape,color=MaterialTheme.colorScheme.surfaceVariant) {
                if(avatar!=null) Image(avatar!!.asImageBitmap(),L.s(R.string.msg_profile_avatar_1671),contentScale=ContentScale.Crop)
                else Icon(Icons.Outlined.Person,L.s(R.string.msg_default_avatar_400b),Modifier.padding(18.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(L.fixed("Profile"),style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.primary)
                JournalTextButton(onClick={edit=true},contentPadding=PaddingValues(0.dp)) {Text(settings.username,style=MaterialTheme.typography.headlineMedium,maxLines=2,overflow=TextOverflow.Ellipsis)}
            }
            Column {
                Row {IconButton(onClick=allBadges) {Icon(Icons.Outlined.EmojiEvents,L.s(R.string.msg_badges_2c57))};IconButton(onClick=settingsAction) {Icon(Icons.Outlined.Settings,L.fixed("Settings"))}}
            }
        }
        if(googleAccount == null) JournalTextButton(enabled=!busy,onClick={profile.googleSignIn(identityContext)},contentPadding=PaddingValues(0.dp)) {Text(L.s(R.string.google_sign_in))}
        val metrics=mapOf(
            "time" to (localizedPracticeTime(state.summary.totalMillis) to L.fixed("Total practice")),
            "pieces" to (java.text.NumberFormat.getIntegerInstance(L.locale).format(state.pieces.size) to L.fixed("Repertoire pieces")),
            "sessions" to (java.text.NumberFormat.getIntegerInstance(L.locale).format(state.sessions.count(AchievementEngine::valid)) to L.fixed("Practice sessions")),
            "streak" to (L.quantity(R.plurals.days_count,currentPracticeStreak(state.sessions,LocalDate.now(),ZoneId.systemDefault())) to L.fixed("Current streak")))
        Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
            StatisticLayout.normalize(settings.statistics.split(',')).take(4).chunked(2).forEach {pair->
                Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {pair.forEach {id->val value=metrics.getValue(id);MetricTile(value.first,value.second,Modifier.weight(1f))}}
            }
        }
        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            ComposerPortrait(artist)
            Column(Modifier.weight(1f)) {
                Text(L.s(R.string.favorite_artist),style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                Text(settings.favoriteArtist.ifBlank {L.s(R.string.not_selected)},style=MaterialTheme.typography.bodyLarge)
                Text(listOf(settings.favoritePiece,settings.favoriteEra).filter {it.isNotBlank()}.joinToString(" · ").ifBlank {L.s(R.string.profile_favorites_hint)},style=MaterialTheme.typography.bodySmall,maxLines=3)
            }
        }
        if(settings.biography.isNotBlank()) Text(settings.biography,style=MaterialTheme.typography.bodyMedium,maxLines=3,overflow=TextOverflow.Ellipsis)
        Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text(L.s(R.string.msg_badge_showcase_6868),style=MaterialTheme.typography.titleMedium)
            if(showcased.isEmpty()) Text(L.s(R.string.showcase_collection_hint),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            else Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(4.dp)) {
                showcased.forEach {badge->Surface(onClick={badgeDetail(badge.definition.id)},modifier=Modifier.weight(1f),color=MaterialTheme.colorScheme.background) {
                    Column(Modifier.padding(vertical=4.dp).heightIn(min=48.dp),horizontalAlignment=Alignment.CenterHorizontally) {MedalArtwork(badge,Modifier.size(40.dp));Text(badge.definition.localizedName,style=MaterialTheme.typography.labelSmall,maxLines=2,overflow=TextOverflow.Ellipsis)}
                }}
            }
        }
        JournalTextButton(onClick=history,contentPadding=PaddingValues(0.dp)) {Text(L.s(R.string.msg_practice_history_76e7))}
    }
    if(edit) EditProfile(settings,avatar,busy,profile) {edit=false}
    if(customize) StatisticsEditor(settings,busy,profile) {customize=false}
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
                    if (index > 0) JournalTextButton(modifier = Modifier.weight(1f), enabled = !busy, onClick = { profile.statistics(listOf(id) + (ids - id)) }) { Text(L.s(R.string.msg_make_primary_81d3)) }
                    IconButton(enabled = index > 0 && !busy, onClick = { val order = ids.toMutableList(); java.util.Collections.swap(order, index, index - 1); profile.statistics(order) }) { Icon(Icons.Outlined.ArrowUpward, L.s(R.string.msg_move_names_id_earlier_11d2, (names[id]))) }
                    IconButton(enabled = index < ids.lastIndex && !busy, onClick = { val order = ids.toMutableList(); java.util.Collections.swap(order, index, index + 1); profile.statistics(order) }) { Icon(Icons.Outlined.ArrowDownward, L.s(R.string.msg_move_names_id_later_4339, (names[id]))) }
                }
            }
        }
    }, confirmButton = { JournalTextButton(onClick = dismiss) { Text(L.s(R.string.msg_done_e9b4)) } })
}

@Composable private fun EditProfile(settings: ProfileSettings, avatar: android.graphics.Bitmap?, busy: Boolean, profile: ProfileViewModel, dismiss: () -> Unit) {
    var statistics by remember {mutableStateOf(false)}
    if(statistics) StatisticsEditor(settings,busy,profile) {statistics=false}
    var artist by rememberSaveable(settings.favoriteArtist) {mutableStateOf(settings.favoriteArtist)}
    var piece by rememberSaveable(settings.favoritePiece) {mutableStateOf(settings.favoritePiece)}
    var era by rememberSaveable(settings.favoriteEra) {mutableStateOf(settings.favoriteEra)}
    var bio by rememberSaveable(settings.biography) {mutableStateOf(settings.biography)}
    var name by rememberSaveable(settings.username) { mutableStateOf(settings.username) }
    val photo = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { it?.let(profile::avatar) }
    AlertDialog(onDismissRequest = { if (!busy) dismiss() }, title = { Text(L.s(R.string.msg_edit_profile_cd28)) }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(Modifier.size(72.dp), shape = CircleShape) {
                if (avatar != null) Image(avatar.asImageBitmap(), L.s(R.string.msg_current_avatar_8634), contentScale = ContentScale.Crop)
                else Icon(Icons.Outlined.Person, L.s(R.string.msg_default_avatar_400b), Modifier.padding(16.dp))
            }
            OutlinedTextField(name, { name = it.take(80) }, label = { Text(L.s(R.string.msg_display_name_c787)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            JournalPrimaryButton(onClick = { profile.username(name) }, enabled = !busy && name.isNotBlank() && name.trim() != settings.username) { Text(L.s(R.string.msg_save_name_8ce6)) }
            JournalSecondaryButton(onClick = { photo.launch("image/*") }, enabled = !busy) { Text(L.s(R.string.msg_choose_avatar_7fa8)) }
            if (settings.avatar.isNotBlank()) JournalTextButton(onClick = profile::clearAvatar, enabled = !busy) { Text(L.s(R.string.msg_use_default_avatar_62c9)) }
            OutlinedTextField(artist,{artist=it.take(120)},label={Text(L.s(R.string.favorite_artist))},modifier=Modifier.fillMaxWidth())
            OutlinedTextField(piece,{piece=it.take(240)},label={Text(L.s(R.string.favorite_piece))},modifier=Modifier.fillMaxWidth())
            OutlinedTextField(era,{era=it.take(80)},label={Text(L.s(R.string.favorite_era))},modifier=Modifier.fillMaxWidth())
            OutlinedTextField(bio,{bio=it.take(400)},label={Text(L.s(R.string.musical_bio))},modifier=Modifier.fillMaxWidth())
            JournalTextButton(onClick={statistics=true}) {Text(L.fixed("Statistics"))}
            JournalPrimaryButton(onClick={profile.personalization(artist,piece,era,bio)},enabled=!busy) {Text(L.s(R.string.save_personalization))}
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        }
    }, confirmButton = { JournalTextButton(onClick = dismiss, enabled = !busy) { Text(L.s(R.string.msg_done_e9b4)) } })
}

@Composable internal fun ShowcaseEditor(settings: ProfileSettings, badges: List<BadgeProgress>, busy: Boolean, profile: ProfileViewModel, dismiss: () -> Unit) {
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
                JournalSecondaryButton(onClick = { profile.showcase(settings.badgeIds + badge.definition.id) }, enabled = !busy && settings.badgeIds.size < 5) { Text(L.s(R.string.msg_add_badge_definition_name_ca2c, (badge.definition.localizedName))) }
            }
            if (badges.none { it.unlocked }) item { Text(L.s(R.string.msg_your_earned_badges_will_be_available_here_af_2f62)) }
        }
    }, confirmButton = { JournalTextButton(onClick = dismiss) { Text(L.s(R.string.msg_done_e9b4)) } })
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
