package com.opus.piano.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.opus.piano.data.*
import kotlinx.coroutines.CancellationException

@OptIn(ExperimentalLayoutApi::class)
@Composable fun ComposerScreen(id: String, model: CatalogViewModel, library: LibraryState, back: () -> Unit, workPage: (String) -> Unit) {
    val snapshot by model.snapshot.collectAsStateWithLifecycle()
    val page by model.composer.collectAsStateWithLifecycle()
    val adding by model.adding.collectAsStateWithLifecycle()
    val composer = snapshot.composers.firstOrNull { it.id == id }
    val works = remember(snapshot.works, id) { snapshot.works.filter { it.composerId == id } }
    val featured = remember(works) { works.filter { it.popular || it.recommended }.take(3) }
    var tab by rememberSaveable(id) { mutableIntStateOf(0) }
    var expanded by rememberSaveable(id) { mutableStateOf(false) }
    var query by rememberSaveable(id) { mutableStateOf("") }
    var genre by rememberSaveable(id) { mutableStateOf("All") }
    var collection by rememberSaveable(id) { mutableStateOf("All") }
    var sort by rememberSaveable(id) { mutableStateOf("Title") }
    var limit by rememberSaveable(id, query, genre, collection, sort) { mutableIntStateOf(40) }
    val profile = page.takeIf { it.id == id }?.profile
    val personal = remember(library.pieces, composer, snapshot) { if (composer == null) emptyList() else library.pieces.filter { pieceComposerMatches(it, composer, snapshot) } }
    val linked = remember(personal, works, composer) { if (composer == null) emptySet() else works.filter { alreadyAdded(it, composer, personal) }.map { it.id }.toSet() }
    val time = remember(library.sessions, library.parts, personal, snapshot, id) { attributedSessions(library.sessions, library.parts).filter { s -> if (s.composerId != null) s.composerId == id else sessionComposerId(s, library.pieces, snapshot) == id || s.pieceId in personal.map { p -> p.id } }.sumOf { it.durationMillis } }
    val matches = remember(works, query, genre, collection, sort, linked) {
        val tokens = catalogText(query).split(' ').filter { it.isNotBlank() }
        val found = works.filter { w -> tokens.all { catalogText("${w.title} ${w.catalogLabel} ${w.subtitle}").contains(it) } &&
            (genre == "All" || w.genre == genre) && (collection == "All" || (w.id in linked) == (collection == "In repertoire")) }
        if (sort == "Catalogue") found.sortedWith(compareBy({ it.catalogue }, { it.catalogueNumber.toIntOrNull() ?: Int.MAX_VALUE }, { it.title })) else found.sortedBy { catalogText(it.title) }
    }
    LaunchedEffect(id) { model.loadComposer(id) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { CatalogBack("The composer's archive", back) }
        if (composer == null) item { CatalogFeedback("This composer is not available on this device.") { model.loadComposer(id, true) } }
        else {
            item {
                BoxWithConstraints {
                    val portraitWidth = if (maxWidth < 300.dp) 72 else 104
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        PortraitImage(profile?.portrait, model.repository, composer.name, portraitWidth)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(composer.epoch.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text(composer.name, style = MaterialTheme.typography.headlineMedium)
                            Text(composer.years, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (!profile?.origin.isNullOrBlank()) Text(profile!!.origin, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            item { TabRow(tab, containerColor = MaterialTheme.colorScheme.background, contentColor = Gold) { listOf("Overview", "Works", "About").forEachIndexed { i, label -> Tab(tab == i, onClick = { tab = i }, text = { Text(label) }) } } }
            when (tab) {
                0 -> {
                    item {
                        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            SmallStatistic("${page.totalWorks ?: works.size}", if (page.totalWorks == null) "Works loaded" else "Catalogued works")
                            SmallStatistic("${personal.size}", "In repertoire")
                            SmallStatistic("${personal.count(::mastered)}", "Mastered")
                            SmallStatistic("${time / 60000} min", "Practiced")
                        }
                    }
                    item {
                        SectionTitle("A life in music")
                        Spacer(Modifier.height(8.dp))
                        if (page.profileLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
                        Text(profile?.text?.ifBlank { null } ?: if (page.profileLoading) "Loading the introduction…" else "No verified biography is available.",
                            maxLines = if (expanded) Int.MAX_VALUE else 4, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (!profile?.text.isNullOrBlank()) TextButton(onClick = { expanded = !expanded }) { Text(if (expanded) "Read less" else "Read more") }
                        TextButton(onClick = { tab = 2 }) { Text("Sources & credits") }
                    }
                    item { SectionTitle("From your repertoire") }
                    if (personal.isEmpty()) item { Text("Add a work by ${composer.name} to start your own chapter.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    items(personal.take(4), key = { "personal:${it.id}" }) { p ->
                        Panel { Text(p.title, style = MaterialTheme.typography.titleMedium); Text(p.status.label, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall) }
                    }
                    if (featured.isNotEmpty()) item { SectionTitle("Featured by Open Opus") }
                    items(featured, key = { "featured:${it.id}" }) { w -> WorkCard(w, composer, alreadyAdded(w, composer, library.pieces), w.id in adding, { workPage(w.id) }, { model.add(w, composer) }) }
                    if (works.isNotEmpty()) item { OutlinedButton(onClick = { tab = 1 }) { Text("Explore ${works.size} loaded works") } }
                }
                1 -> {
                    item { OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Find a composition") }) }
                    item { FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Choice("Genre", listOf("All") + works.map { it.genre }.filter { it.isNotBlank() }.distinct().sorted(), genre) { genre = it }
                        Choice("Collection", listOf("All", "In repertoire", "Not added"), collection) { collection = it }
                        Choice("Sort", listOf("Title", "Catalogue"), sort) { sort = it }
                    } }
                    if (page.loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
                    if (!page.loading && matches.isEmpty()) item { Text("No works match this selection.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    items(matches.take(limit), key = { it.id }) { w -> WorkCard(w, composer, alreadyAdded(w, composer, library.pieces), w.id in adding, { workPage(w.id) }, { model.add(w, composer) }) }
                    if (matches.size > limit) item { OutlinedButton(onClick = { limit += 40 }) { Text("Show more works (${matches.size - limit} remaining)") } }
                }
                2 -> {
                    item { SectionTitle("Biography"); Spacer(Modifier.height(8.dp)); Text(profile?.text?.ifBlank { null } ?: "No verified biography is available.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    item { Panel {
                        Text("HISTORICAL DETAILS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Text("Era · ${composer.epoch.ifBlank { "Not supplied" }}")
                        Text("Born · ${profile?.birth?.ifBlank { null } ?: composer.birth.take(4).ifBlank { "Not supplied" }}")
                        Text("Died · ${profile?.death?.ifBlank { null } ?: composer.death.take(4).ifBlank { "Not supplied" }}")
                        profile?.origin?.takeIf { it.isNotBlank() }?.let { Text("Birthplace / citizenship · $it") }
                    } }
                    item { ProfileCredits(profile) }
                    item { CacheCaption(page.fetchedAt); TextButton(onClick = { model.loadComposer(id, true) }) { Text("Refresh composer") } }
                }
            }
            page.error?.let { message -> item { CatalogFeedback(message) { model.loadComposer(id, true) } } }
            page.profileError?.let { message -> item { CatalogFeedback(message) { model.loadComposer(id, true) } } }
            page.warning?.let { message -> item { CatalogFeedback(message) { model.loadComposer(id, true) } } }
        }
    }
}

@Composable internal fun SmallStatistic(value: String, label: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) { Text(value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary); Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
}
@Composable private fun PortraitImage(portrait: Portrait?, repository: CatalogRepository, name: String, width: Int) {
    val bitmap by produceState<android.graphics.Bitmap?>(null, portrait?.url) {
        if (portrait != null) try { value = repository.portrait(portrait.url) } catch (cancelled: CancellationException) { throw cancelled } catch (_: Exception) { value = null }
    }
    Surface(Modifier.width(width.dp).height((width * 1.2f).dp), shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceVariant) {
        if (bitmap != null) Image(bitmap!!.asImageBitmap(), "Portrait of $name. Attribution in About, Sources and credits.", contentScale = ContentScale.Crop)
        else Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.PersonOutline, "Portrait unavailable", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(38.dp)) }
    }
}
@Composable private fun ProfileCredits(profile: ComposerProfile?) {
    Panel {
        SectionTitle("Sources & credits")
        SourceLink("Catalogue metadata · Open Opus · public domain", "https://openopus.org/")
        if (profile != null) {
            SourceLink("Biography · Wikipedia contributors", profile.articleUrl)
            SourceLink("Article history & authors", if (profile.articleUrl.isBlank()) "" else "${profile.articleUrl}?action=history")
            SourceLink(profile.license, profile.licenseUrl)
            SourceLink("Identity & dates · Wikidata · CC0", profile.wikidataUrl)
            if (profile.text.isNotBlank()) Text("English Wikipedia introduction, unchanged.", style = MaterialTheme.typography.bodySmall)
            profile.portrait?.let { p ->
                HorizontalDivider()
                Text(p.artist, style = MaterialTheme.typography.bodySmall)
                if (p.credit.isNotBlank()) Text(p.credit, style = MaterialTheme.typography.bodySmall)
                SourceLink("Portrait source · Wikimedia Commons", p.sourceUrl)
                if (p.licenseUrl.isNotBlank()) SourceLink(p.license, p.licenseUrl) else Text(p.license)
                Text("Portrait displayed as an editorial crop.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
