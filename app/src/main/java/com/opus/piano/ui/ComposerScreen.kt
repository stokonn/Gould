package com.opus.piano.ui

import com.opus.piano.i18n.L
import com.opus.piano.R
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
        item { CatalogBack(L.s(R.string.msg_the_composer_s_archive_2190), back) }
        if (composer == null) item { CatalogFeedback(L.s(R.string.composer_unavailable)) { model.loadComposer(id, true) } }
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
            item { TabRow(tab, containerColor = MaterialTheme.colorScheme.background, contentColor = Gold) { listOf("Overview", "Works", "About").forEachIndexed { i, label -> Tab(tab == i, onClick = { tab = i }, text = { Text(if(label=="About") L.s(R.string.msg_biography_53c2) else L.fixed(label)) }) } } }
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
                        SectionTitle(L.s(R.string.msg_a_life_in_music_9319))
                        Spacer(Modifier.height(8.dp))
                        if (page.profileLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
                        Text(profile?.text?.ifBlank { null } ?: if (page.profileLoading) L.s(R.string.msg_loading_the_introduction_f296) else L.s(R.string.msg_no_verified_biography_is_available_8fab),
                            maxLines = if (expanded) Int.MAX_VALUE else 4, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (!profile?.text.isNullOrBlank()) TextButton(onClick = { expanded = !expanded }) { Text(if (expanded) L.s(R.string.msg_read_less_32ab) else L.s(R.string.msg_read_more_ad08)) }
                        TextButton(onClick = { tab = 2 }) { Text(L.s(R.string.msg_sources_credits_874b)) }
                    }
                    item { SectionTitle(L.s(R.string.msg_from_your_repertoire_f345)) }
                    if (personal.isEmpty()) item { Text(L.s(R.string.msg_add_a_work_by_composer_name_to_start_your_ow_2f92, (composer.name)), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    items(personal.take(4), key = { "personal:${it.id}" }) { p ->
                        Panel { Text(p.title, style = MaterialTheme.typography.titleMedium); Text(L.fixed(p.status.label), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall) }
                    }
                    if (featured.isNotEmpty()) item { SectionTitle(L.s(R.string.msg_featured_by_open_opus_7787)) }
                    items(featured, key = { "featured:${it.id}" }) { w -> WorkCard(w, composer, alreadyAdded(w, composer, library.pieces), w.id in adding, { workPage(w.id) }, { model.add(w, composer) }) }
                    if (works.isNotEmpty()) item { OutlinedButton(onClick = { tab = 1 }) { Text(L.s(R.string.msg_explore_works_size_loaded_works_c20f, (works.size))) } }
                }
                1 -> {
                    item { OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text(L.s(R.string.msg_find_a_composition_bfc2)) }) }
                    item { FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Choice(L.s(R.string.msg_genre_2aa3), listOf("All") + works.map { it.genre }.filter { it.isNotBlank() }.distinct().sorted(), genre) { genre = it }
                        Choice(L.s(R.string.msg_collection_30c5), listOf("All", L.s(R.string.msg_in_repertoire_b185), "Not added"), collection) { collection = it }
                        Choice(L.s(R.string.msg_sort_adc4), listOf("Title", L.s(R.string.msg_catalogue_5a24)), sort) { sort = it }
                    } }
                    if (page.loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
                    if (!page.loading && matches.isEmpty()) item { Text(L.s(R.string.msg_no_works_match_this_selection_85cd), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    items(matches.take(limit), key = { it.id }) { w -> WorkCard(w, composer, alreadyAdded(w, composer, library.pieces), w.id in adding, { workPage(w.id) }, { model.add(w, composer) }) }
                    if (matches.size > limit) item { OutlinedButton(onClick = { limit += 40 }) { Text(L.s(R.string.msg_show_more_works_matches_size_limit_remaining_2020, (matches.size - limit))) } }
                }
                2 -> {
                    item { SectionTitle(L.s(R.string.msg_biography_53c2)); Spacer(Modifier.height(8.dp)); Text(profile?.text?.ifBlank { null } ?: L.s(R.string.msg_no_verified_biography_is_available_8fab), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    item { Panel {
                        Text(L.s(R.string.msg_historical_details_67be), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Text(L.s(R.string.msg_era_composer_epoch_ifblank_not_supplied_6765, (composer.epoch.ifBlank { L.fixed("Not supplied") })))
                        Text(L.s(R.string.msg_born_profile_birth_ifblank_null_composer_bir_2f27, (profile?.birth?.ifBlank { null } ?: composer.birth.take(4).ifBlank { L.fixed("Not supplied") })))
                        Text(L.s(R.string.msg_died_profile_death_ifblank_null_composer_dea_e1b2, (profile?.death?.ifBlank { null } ?: composer.death.take(4).ifBlank { L.fixed("Not supplied") })))
                        profile?.origin?.takeIf { it.isNotBlank() }?.let { Text(L.s(R.string.msg_birthplace_citizenship_it_4edb, (it))) }
                    } }
                    item { ProfileCredits(profile) }
                    item { CacheCaption(page.fetchedAt); TextButton(onClick = { model.loadComposer(id, true) }) { Text(L.s(R.string.msg_refresh_composer_1c6b)) } }
                }
            }
            page.error?.let { message -> item { CatalogFeedback(message) { model.loadComposer(id, true) } } }
            page.profileError?.let { message -> item { CatalogFeedback(message) { model.loadComposer(id, true) } } }
            page.warning?.let { message -> item { CatalogFeedback(message) { model.loadComposer(id, true) } } }
        }
    }
}

@Composable internal fun SmallStatistic(value: String, label: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) { Text(value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary); Text(L.fixed(label), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
}
@Composable private fun PortraitImage(portrait: Portrait?, repository: CatalogRepository, name: String, width: Int) {
    val bitmap by produceState<android.graphics.Bitmap?>(null, portrait?.url) {
        if (portrait != null) try { value = repository.portrait(portrait.url) } catch (cancelled: CancellationException) { throw cancelled } catch (_: Exception) { value = null }
    }
    Surface(Modifier.width(width.dp).height((width * 1.2f).dp), shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceVariant) {
        if (bitmap != null) Image(bitmap!!.asImageBitmap(), L.s(R.string.msg_portrait_of_name_attribution_in_about_source_e948, (name)), contentScale = ContentScale.Crop)
        else Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.PersonOutline, L.s(R.string.msg_portrait_unavailable_6f4e), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(38.dp)) }
    }
}
@Composable private fun ProfileCredits(profile: ComposerProfile?) {
    Panel {
        SectionTitle(L.s(R.string.msg_sources_credits_874b))
        SourceLink(L.s(R.string.msg_catalogue_metadata_open_opus_public_domain_c841), "https://openopus.org/")
        if (profile != null) {
            SourceLink(L.s(R.string.msg_biography_wikipedia_contributors_0d3b), profile.articleUrl)
            SourceLink(L.s(R.string.msg_article_history_authors_89e1), if (profile.articleUrl.isBlank()) "" else "${profile.articleUrl}?action=history")
            SourceLink(profile.license, profile.licenseUrl)
            SourceLink(L.s(R.string.msg_identity_dates_wikidata_cc0_f066), profile.wikidataUrl)
            if (profile.text.isNotBlank()) Text(L.s(R.string.msg_english_wikipedia_introduction_unchanged_10f1), style = MaterialTheme.typography.bodySmall)
            profile.portrait?.let { p ->
                HorizontalDivider()
                Text(p.artist, style = MaterialTheme.typography.bodySmall)
                if (p.credit.isNotBlank()) Text(p.credit, style = MaterialTheme.typography.bodySmall)
                SourceLink(L.s(R.string.msg_portrait_source_wikimedia_commons_a86c), p.sourceUrl)
                if (p.licenseUrl.isNotBlank()) SourceLink(p.license, p.licenseUrl) else Text(p.license)
                Text(L.s(R.string.msg_portrait_displayed_as_an_editorial_crop_cdaf), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
