package com.opus.piano.ui

import com.opus.piano.i18n.L
import com.opus.piano.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.opus.piano.data.*
import kotlinx.coroutines.CancellationException
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable internal fun CatalogBack(label: String, back: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = back) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, L.s(R.string.msg_back_b52b)) }
        Text(L.fixed(label).uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
    }
}
@Composable internal fun CatalogFeedback(message: String, retry: () -> Unit) {
    Panel {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CloudOff, null, tint = MaterialTheme.colorScheme.primary)
            Text(message, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        }
        JournalTextButton(onClick = retry) { Text(L.s(R.string.msg_try_again_042c)) }
    }
}
@Composable internal fun SourceLink(label: String, url: String) {
    val uri = LocalUriHandler.current
    if (url.startsWith("https://")) JournalTextButton(onClick = { try { uri.openUri(url) } catch (_: IllegalArgumentException) { } catch (_: android.content.ActivityNotFoundException) { } }) {
        Icon(Icons.AutoMirrored.Outlined.OpenInNew, null, Modifier.size(14.dp))
        Spacer(Modifier.width(8.dp))
        Text(L.fixed(label))
    }
}
@Composable internal fun CacheCaption(at: Long) {
    if (at > 0) Text(L.s(R.string.msg_saved_on_device_instant_ofepochmilli_at_atzo_ab8d, (Instant.ofEpochMilli(at).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("d MMM yyyy").withLocale(L.locale)))), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
internal fun alreadyAdded(work: CatalogWork, composer: CatalogComposer?, pieces: List<Piece>): Boolean = pieces.any {
    it.catalogWorkId == work.id || (it.catalogWorkId == null && catalogText(it.title) == catalogText(work.title) && catalogText(it.composer) == catalogText(composer?.name.orEmpty()))
}

@Composable fun CatalogScreen(model: CatalogViewModel, library: LibraryState, back: () -> Unit, composerPage: (String) -> Unit, workPage: (String) -> Unit, piecePage: (Long) -> Unit = {}, embedded: Boolean = false, profile:ProfileViewModel) {
    val favorites by profile.favoriteComposers.collectAsState()
    LaunchedEffect(Unit) { model.startBrowsing() }
    val state by model.browse.collectAsStateWithLifecycle()
    val results by model.results.collectAsStateWithLifecycle()
    val snapshot by model.snapshot.collectAsStateWithLifecycle()
    val adding by model.adding.collectAsStateWithLifecycle()
    val composers = remember(snapshot.composers) { snapshot.composers.associateBy { it.id } }
    val repertoire = remember(library.pieces, state.query) {
        val tokens = catalogText(state.query).split(' ').filter { it.isNotBlank() }
        if (tokens.isEmpty()) emptyList() else library.pieces.filter { p -> val text = catalogText("${p.title} ${p.composer}"); tokens.all { it in text } }
    }
    var limit by rememberSaveable(state.activeQuery) { mutableIntStateOf(40) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (!embedded) item { CatalogBack(L.s(R.string.msg_your_musical_discoveries_c39e), back) }
        if (!embedded) item { Heading("", L.s(R.string.msg_catalogue_5a24), L.s(R.string.msg_search_repertoire_works_or_composers_0ff5)) }
        item {
            OutlinedTextField(state.query, model::query, Modifier.fillMaxWidth(), label = { Text(L.s(R.string.msg_search_repertoire_works_or_composers_0ff5)) },
                placeholder = { Text(L.s(R.string.msg_title_composer_or_catalogue_number_bf09)) }, leadingIcon = { Icon(Icons.Outlined.Search, null) },
                trailingIcon = { if (state.query.isNotEmpty()) IconButton(onClick = { model.query("") }) { Icon(Icons.Outlined.Close, L.s(R.string.msg_clear_search_6730)) } }, singleLine = true)
        }
        if(favorites.isNotEmpty()) item {FavoriteComposerStrip(snapshot,favorites,composerPage,profile::favoriteComposer)}
        if (state.loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        state.error?.let { message -> item { CatalogFeedback(message, model::refreshSearch) } }
        state.warning?.let { message -> item { CatalogFeedback(message, model::refreshSearch) } }
        item {
            Text(if (state.query.isBlank()) L.s(R.string.msg_composers_in_your_saved_catalogue_bf6f) else L.s(R.string.msg_suggestions_saved_new_discoveries_6c53), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))
            Text(L.s(R.string.msg_search_ignores_accents_offline_results_inclu_3f32), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (repertoire.isNotEmpty()) item { SectionTitle(L.s(R.string.msg_your_repertoire_74de)) }
        items(repertoire.take(limit), key = { "piece:${it.id}" }) { piece ->
            JournalSecondaryButton(onClick = { piecePage(piece.id) }, modifier = Modifier.fillMaxWidth()) { Text("${piece.title} · ${piece.composer}") }
        }
        if (results.composers.isNotEmpty()) {
            item { SectionTitle(L.s(R.string.msg_composers_707f)) }
            items(results.composers.take(limit), key = { "composer:${it.id}" }) { composer ->
                Surface(onClick = { composerPage(composer.id) }, shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, border = if(LocalAppearance.current.cards=="Outline") BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant) else null) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ComposerPortrait(composer)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(composer.name, style = MaterialTheme.typography.titleMedium)
                            Text("${composer.years} · ${L.fixed(composer.epoch)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        ComposerFavoriteButton(composer,favorites.any {it.composerId==composer.id},profile::favoriteComposer)
                    }
                }
            }
        }
        if (results.works.isNotEmpty()) item { SectionTitle(L.s(R.string.msg_works_bb38)) }
        items(results.works.take(limit), key = { "work:${it.id}" }) { work ->
            WorkCard(work, composers[work.composerId], alreadyAdded(work, composers[work.composerId], library.pieces), work.id in adding,
                { workPage(work.id) }, { composers[work.composerId]?.let { model.add(work, it) } })
        }
        if (!state.loading && results.composers.isEmpty() && results.works.isEmpty() && repertoire.isEmpty()) item {
            Panel {
                Icon(Icons.Outlined.LibraryMusic, null, tint = MaterialTheme.colorScheme.primary)
                SectionTitle(if (state.error != null) L.s(R.string.msg_your_discoveries_will_live_here_691a) else L.s(R.string.msg_no_matching_music_97fe))
                Text(if (state.error != null) L.s(R.string.msg_an_internet_connection_is_needed_for_your_fi_eb85) else L.s(R.string.msg_try_a_different_title_composer_or_catalogue_16ea), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (results.works.size > limit || results.composers.size > limit || repertoire.size > limit) item { JournalSecondaryButton(onClick = { limit += 40 }) { Text(L.s(R.string.msg_more_saved_suggestions_d7e1)) } }
        if (state.next != null) item { JournalPrimaryButton(onClick = model::nextPage, enabled = !state.loading) { Text(L.s(R.string.msg_discover_more_results_f817)) } }
        item { CacheCaption(state.fetchedAt); SourceLink(L.s(R.string.msg_catalogue_open_opus_public_domain_36b6), "https://openopus.org/") }
    }
}

@Composable internal fun WorkCard(work: CatalogWork, composer: CatalogComposer?, added: Boolean, adding: Boolean, open: () -> Unit, add: () -> Unit) {
    Surface(onClick = open, shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface) {
        Row(Modifier.fillMaxWidth().padding(start = 16.dp, top = 10.dp, bottom = 10.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ComposerPortrait(composer)
            PieceRowContent(work.title,composer?.name.orEmpty(),
                listOf(work.catalogLabel.takeUnless { catalogText(work.title).contains(catalogText(it)) }.orEmpty(),L.fixed(work.genre),if(added) L.s(R.string.msg_in_repertoire_b185) else "").filter { it.isNotBlank() }.joinToString(" · "),
                Modifier.weight(1f))
            IconButton(onClick = add, enabled = !added && !adding && composer != null) {
                if (adding) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else androidx.compose.animation.AnimatedContent(added, label = "Repertoire addition") { isAdded -> Icon(if (isAdded) Icons.Outlined.Check else Icons.Outlined.Add, if (added) L.s(R.string.msg_already_in_repertoire_eac8) else L.s(R.string.msg_add_work_title_to_repertoire_326f, (work.title)), tint = if (isAdded) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary) }
            }
        }
    }
}
