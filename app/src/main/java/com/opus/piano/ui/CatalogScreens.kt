package com.opus.piano.ui

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
        IconButton(onClick = back) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back") }
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
    }
}
@Composable internal fun CatalogFeedback(message: String, retry: () -> Unit) {
    Panel {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CloudOff, null, tint = MaterialTheme.colorScheme.primary)
            Text(message, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        }
        TextButton(onClick = retry) { Text("Try again") }
    }
}
@Composable internal fun SourceLink(label: String, url: String) {
    val uri = LocalUriHandler.current
    if (url.startsWith("https://")) TextButton(onClick = { try { uri.openUri(url) } catch (_: IllegalArgumentException) { } catch (_: android.content.ActivityNotFoundException) { } }) {
        Icon(Icons.AutoMirrored.Outlined.OpenInNew, null, Modifier.size(14.dp))
        Spacer(Modifier.width(8.dp))
        Text(label)
    }
}
@Composable internal fun CacheCaption(at: Long) {
    if (at > 0) Text("Saved on device · ${Instant.ofEpochMilli(at).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("d MMM yyyy"))}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
internal fun alreadyAdded(work: CatalogWork, composer: CatalogComposer?, pieces: List<Piece>): Boolean = pieces.any {
    it.catalogWorkId == work.id || (it.catalogWorkId == null && catalogText(it.title) == catalogText(work.title) && catalogText(it.composer) == catalogText(composer?.name.orEmpty()))
}

@Composable fun CatalogScreen(model: CatalogViewModel, library: LibraryState, back: () -> Unit, composerPage: (String) -> Unit, workPage: (String) -> Unit, piecePage: (Long) -> Unit = {}) {
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
        item { CatalogBack("Your musical discoveries", back) }
        item { Heading("OPEN OPUS COLLECTION", "Universal search", "Discover composers and works. Make the next piece part of your story.") }
        item {
            OutlinedTextField(state.query, model::query, Modifier.fillMaxWidth(), label = { Text("Search repertoire, works or composers") },
                placeholder = { Text("Title, composer or catalogue number") }, leadingIcon = { Icon(Icons.Outlined.Search, null) },
                trailingIcon = { if (state.query.isNotEmpty()) IconButton(onClick = { model.query("") }) { Icon(Icons.Outlined.Close, "Clear search") } }, singleLine = true)
        }
        if (state.loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        state.error?.let { message -> item { CatalogFeedback(message, model::refreshSearch) } }
        state.warning?.let { message -> item { CatalogFeedback(message, model::refreshSearch) } }
        item {
            Text(if (state.query.isBlank()) "COMPOSERS IN YOUR SAVED CATALOGUE" else "SUGGESTIONS · SAVED & NEW DISCOVERIES", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))
            Text("Search ignores accents. Offline results include previously loaded information; connect to discover more. Catalogue titles are supplied by Open Opus in their original language.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (repertoire.isNotEmpty()) item { SectionTitle("Your repertoire") }
        items(repertoire.take(limit), key = { "piece:${it.id}" }) { piece ->
            OutlinedButton(onClick = { piecePage(piece.id) }, modifier = Modifier.fillMaxWidth()) { Text("${piece.title} · ${piece.composer}") }
        }
        if (results.composers.isNotEmpty()) {
            item { SectionTitle("Composers") }
            items(results.composers.take(limit), key = { "composer:${it.id}" }) { composer ->
                Surface(onClick = { composerPage(composer.id) }, shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, Gold.copy(alpha = .1f))) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Icon(Icons.Outlined.PersonOutline, null, tint = MaterialTheme.colorScheme.primary)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(composer.name, style = MaterialTheme.typography.titleMedium)
                            Text("${composer.years} · ${composer.epoch}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Outlined.ChevronRight, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        if (results.works.isNotEmpty()) item { SectionTitle("Works") }
        items(results.works.take(limit), key = { "work:${it.id}" }) { work ->
            WorkCard(work, composers[work.composerId], alreadyAdded(work, composers[work.composerId], library.pieces), work.id in adding,
                { workPage(work.id) }, { composers[work.composerId]?.let { model.add(work, it) } })
        }
        if (!state.loading && results.composers.isEmpty() && results.works.isEmpty() && repertoire.isEmpty()) item {
            Panel {
                Icon(Icons.Outlined.LibraryMusic, null, tint = MaterialTheme.colorScheme.primary)
                SectionTitle(if (state.error != null) "Your discoveries will live here" else "No matching music")
                Text(if (state.error != null) "An internet connection is needed for your first catalogue discovery." else "Try a different title, composer, or catalogue number. A title may refer to several different works.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (results.works.size > limit || results.composers.size > limit || repertoire.size > limit) item { OutlinedButton(onClick = { limit += 40 }) { Text("More saved suggestions") } }
        if (state.next != null) item { Button(onClick = model::nextPage, enabled = !state.loading) { Text("Discover more results") } }
        item { CacheCaption(state.fetchedAt); SourceLink("Catalogue: Open Opus · public domain", "https://openopus.org/") }
    }
}

@Composable internal fun WorkCard(work: CatalogWork, composer: CatalogComposer?, added: Boolean, adding: Boolean, open: () -> Unit, add: () -> Unit) {
    Surface(onClick = open, shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface) {
        Row(Modifier.fillMaxWidth().padding(start = 16.dp, top = 10.dp, bottom = 10.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(work.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (composer != null) Text(composer.name, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                Text(listOf(work.catalogLabel, work.genre, if (added) "In repertoire" else "").filter { it.isNotBlank() }.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = add, enabled = !added && !adding && composer != null) {
                if (adding) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else androidx.compose.animation.AnimatedContent(added, label = "Repertoire addition") { isAdded -> Icon(if (isAdded) Icons.Outlined.Check else Icons.Outlined.Add, if (added) "Already in repertoire" else "Add ${work.title} to repertoire", tint = if (isAdded) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary) }
            }
        }
    }
}
