package com.opus.piano.ui

import com.opus.piano.i18n.L
import com.opus.piano.R
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.opus.piano.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.*

@OptIn(ExperimentalLayoutApi::class)
@Composable internal fun ProfileScreen(state: LibraryState, badges: List<BadgeProgress>, profile: ProfileViewModel, settingsAction: () -> Unit, allBadges: () -> Unit, history: () -> Unit, badgeDetail: (String) -> Unit) {
    val settings by profile.settings.collectAsStateWithLifecycle()
    val busy by profile.busy.collectAsStateWithLifecycle()
    val googleAccount by profile.googleAccount.collectAsStateWithLifecycle()
    val context=androidx.compose.ui.platform.LocalContext.current
    val app=context.applicationContext as com.opus.piano.OpusApplication
    val metered by app.media.cellular.collectAsStateWithLifecycle()
    LaunchedEffect(googleAccount,metered) {if(googleAccount!=null) profile.refreshGooglePhoto()}
    var edit by rememberSaveable {mutableStateOf(false)}
    var showcaseEdit by rememberSaveable {mutableStateOf(false)}
    var artistEdit by rememberSaveable {mutableStateOf(false)}
    val avatar by produceState<android.graphics.Bitmap?>(null,settings.avatar) {value=withContext(Dispatchers.Default) {runCatching {val bytes=android.util.Base64.decode(settings.avatar,android.util.Base64.DEFAULT);android.graphics.BitmapFactory.decodeByteArray(bytes,0,bytes.size)}.getOrNull()}}
    val catalog by app.catalogRepository.snapshot.collectAsStateWithLifecycle(CatalogSnapshot(emptyList(),emptyList()))
    val artist=catalog.composers.firstOrNull {catalogText(it.name)==catalogText(settings.favoriteArtist)}
    val showcased=settings.badgeIds.mapNotNull {id->badges.firstOrNull {it.definition.id==id && it.unlocked}}.take(5)
    val fontScale=LocalDensity.current.fontScale
    JournalViewport(spacing=12.dp) {
        EditorialHeader(L.fixed("Profile"),actions={
            IconButton(onClick=allBadges) {Icon(Icons.Outlined.EmojiEvents,L.s(R.string.msg_badges_2c57),tint=MaterialTheme.colorScheme.primary)}
            IconButton(onClick=settingsAction) {Icon(Icons.Outlined.Settings,L.fixed("Settings"),tint=MaterialTheme.colorScheme.primary)}
        })
        JournalCard(padding=PaddingValues(10.dp)) {
            Row(Modifier.padding(top=2.dp,bottom=4.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(16.dp)) {
                Box {
                    Surface(onClick={edit=true},modifier=Modifier.size(104.dp),shape=CircleShape,color=MaterialTheme.colorScheme.primary.copy(alpha=.1f),border=androidx.compose.foundation.BorderStroke(2.dp,MaterialTheme.colorScheme.primary.copy(alpha=.35f))) {
                        if(avatar!=null) Image(avatar!!.asImageBitmap(),L.s(R.string.msg_profile_avatar_1671),contentScale=ContentScale.Crop)
                        else Icon(Icons.Outlined.Person,L.s(R.string.msg_default_avatar_400b),Modifier.padding(24.dp),tint=MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick={edit=true},modifier=Modifier.align(Alignment.BottomEnd).size(48.dp)) {Surface(shape=CircleShape,color=MaterialTheme.colorScheme.surfaceVariant) {Icon(Icons.Outlined.Edit,L.s(R.string.msg_edit_profile_cd28),Modifier.size(32.dp).padding(7.dp))}}
                }
                Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                    Text(settings.username,style=MaterialTheme.typography.headlineLarge,maxLines=3,overflow=TextOverflow.Ellipsis)
                    Text(settings.biography.ifBlank {L.s(R.string.ui010_add_bio)},Modifier.fillMaxWidth().clickable {edit=true},style=MaterialTheme.typography.bodySmall,textAlign=androidx.compose.ui.text.style.TextAlign.Start,color=MaterialTheme.colorScheme.onSurfaceVariant)
                    if(googleAccount==null) JournalTextButton(enabled=!busy,onClick={profile.googleSignIn(context)},contentPadding=PaddingValues(0.dp)) {Text(L.s(R.string.google_sign_in),style=MaterialTheme.typography.labelMedium)}
                }
            }
            val metrics=mapOf("time" to (localizedPracticeTime(state.summary.totalMillis) to L.fixed("Total practice")),"pieces" to (java.text.NumberFormat.getIntegerInstance(L.locale).format(state.pieces.size) to L.fixed("Repertoire pieces")),"sessions" to (java.text.NumberFormat.getIntegerInstance(L.locale).format(state.sessions.count(AchievementEngine::valid)) to L.fixed("Practice sessions")),"streak" to (L.quantity(R.plurals.days_count,currentPracticeStreak(state.sessions,LocalDate.now(),ZoneId.systemDefault())) to L.fixed("Current streak")))
            BoxWithConstraints {
                val columns=if(maxWidth<300.dp || fontScale>1.15f) 2 else 4
                Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
                    StatisticLayout.normalize(settings.statistics.split(',')).chunked(columns).forEach {row->
                    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min),horizontalArrangement=Arrangement.spacedBy(JournalSpace.small)) {row.forEach {id->
                            Surface(Modifier.weight(1f).fillMaxHeight().heightIn(min=76.dp),shape=MaterialTheme.shapes.small,color=MaterialTheme.colorScheme.surface,border=androidx.compose.foundation.BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant.copy(alpha=.4f))) {
                                Column(Modifier.fillMaxHeight().padding(horizontal=6.dp,vertical=8.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(4.dp)) {
                                    Text(metrics.getValue(id).first,style=MaterialTheme.typography.bodyLarge.copy(fontWeight=androidx.compose.ui.text.font.FontWeight.SemiBold,fontFeatureSettings="tnum"),maxLines=1,softWrap=false,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
                                    Text(metrics.getValue(id).second,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
                                }
                            }
                        }}
                    }
                }
            }
        }
        Column(verticalArrangement=Arrangement.spacedBy(JournalSpace.tiny)) {
            CompactSection(L.s(R.string.favorite_artist))
            JournalCard(Modifier.clickable(role=Role.Button) {artistEdit=true}) {
                Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    ComposerPortrait(artist,Modifier.size(width=52.dp,height=64.dp))
                    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                        Text(settings.favoriteArtist.ifBlank {L.s(R.string.ui010_select_artist)},style=MaterialTheme.typography.titleMedium)
                        artist?.let {Text(listOf(L.fixed(it.epoch),it.years).filter {it.isNotBlank()}.joinToString(" · "),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
                    }
                    Icon(Icons.Outlined.ChevronRight,null,Modifier.size(20.dp),tint=MaterialTheme.colorScheme.primary)
                }
                if(settings.favoritePiece.isNotBlank() || settings.favoriteEra.isNotBlank()) {HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant.copy(alpha=.4f))}
                if(settings.favoritePiece.isNotBlank()) Text(L.s(R.string.favorite_piece)+" · "+settings.favoritePiece,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                if(settings.favoriteEra.isNotBlank()) Text(L.s(R.string.favorite_era)+" · "+settings.favoriteEra,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment=Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) {CompactSection(L.s(R.string.msg_badge_showcase_6868))}
                IconButton(onClick={showcaseEdit=true}) {Icon(Icons.Outlined.Add,L.s(R.string.msg_badge_showcase_6868))}
            }
            if(showcased.isEmpty()) Text(L.s(R.string.showcase_collection_hint),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            else Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(12.dp)) {showcased.forEach {badge->
                Surface(onClick={badgeDetail(badge.definition.id)},modifier=Modifier.size(84.dp).semantics {contentDescription=badge.definition.localizedName+" · "+localDateTime(badge.unlockedAt ?: 0)},shape=CircleShape,color=MaterialTheme.colorScheme.surface,border=androidx.compose.foundation.BorderStroke(1.dp,MaterialTheme.colorScheme.primary.copy(alpha=.35f))) {MedalArtwork(badge,Modifier.padding(5.dp))}
            }}
        }
    }
    if(showcaseEdit) ShowcaseEditor(settings,badges,busy,profile) {showcaseEdit=false}
    if(edit) EditProfile(settings,avatar,busy,profile) {edit=false}
    if(artistEdit) {
        var value by rememberSaveable {mutableStateOf(settings.favoriteArtist)}
        AlertDialog(onDismissRequest={artistEdit=false},title={Text(L.s(R.string.favorite_artist))},text={ArtistAutocomplete(value,{value=it})},confirmButton={JournalPrimaryButton(enabled=!busy,onClick={profile.favoriteArtist(value);artistEdit=false}) {Text(L.s(R.string.msg_save_efc0))}},dismissButton={JournalTextButton(onClick={artistEdit=false}) {Text(L.s(R.string.msg_cancel_77df))}})
    }
}

@Composable internal fun ArtistAutocomplete(value:String,change:(String)->Unit) {
    val app=androidx.compose.ui.platform.LocalContext.current.applicationContext as com.opus.piano.OpusApplication
    val catalog by app.catalogRepository.snapshot.collectAsStateWithLifecycle(CatalogSnapshot(emptyList(),emptyList()))
    var expanded by remember {mutableStateOf(false)}
    val matches=remember(value,catalog.composers) {composerSuggestions(catalog.composers,value)}
    Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(value,{change(it.take(120));expanded=true},label={Text(L.s(R.string.favorite_artist))},leadingIcon={Icon(Icons.Outlined.Search,null)},modifier=Modifier.fillMaxWidth(),singleLine=true)
        if(expanded && value.isNotBlank()) {
            if(matches.isEmpty()) Text(L.s(R.string.ui010_local_no_match),style=MaterialTheme.typography.bodySmall)
            else Column(Modifier.heightIn(max=240.dp).verticalScroll(rememberScrollState())) {matches.forEach {composer->
                Surface(onClick={change(composer.name);expanded=false},color=MaterialTheme.colorScheme.surface) {
                    Row(Modifier.fillMaxWidth().heightIn(min=60.dp).padding(6.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {ComposerPortrait(composer);Text(composer.name,style=MaterialTheme.typography.bodyMedium)}
                }
            }}
        }
    }
}

@Composable private fun StatisticsEditor(settings: ProfileSettings, busy: Boolean, profile: ProfileViewModel, dismiss: () -> Unit) {
    val ids = StatisticLayout.normalize(settings.statistics.split(','))
    val names = mapOf("time" to L.s(R.string.msg_total_practice_a63c), "pieces" to L.s(R.string.msg_repertoire_pieces_e09d), "sessions" to L.s(R.string.msg_practice_sessions_60c7), "streak" to L.s(R.string.msg_current_streak_2ff8))
    AlertDialog(onDismissRequest = dismiss, title = { Text(L.s(R.string.msg_your_statistics_502c)) }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
            ArtistAutocomplete(artist,{artist=it})
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
