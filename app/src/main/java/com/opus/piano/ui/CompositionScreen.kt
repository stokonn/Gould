package com.opus.piano.ui

import com.opus.piano.i18n.L
import com.opus.piano.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.opus.piano.data.*

@OptIn(ExperimentalLayoutApi::class)
@Composable fun CompositionScreen(workId: String?, pieceId: Long?, catalogModel: CatalogViewModel, experience: ExperienceViewModel,
    library: LibraryState, model: OpusViewModel, back: () -> Unit, composerPage: (String) -> Unit, recordingsPage: (String) -> Unit, history: (Long?, String?) -> Unit, practice: (Piece) -> Unit) {
    val snapshot by catalogModel.snapshot.collectAsStateWithLifecycle()
    val page by catalogModel.work.collectAsStateWithLifecycle()
    val recordings by experience.recordings.collectAsStateWithLifecycle()
    val adding by catalogModel.adding.collectAsStateWithLifecycle()
    val personal = library.pieces.firstOrNull { if (pieceId != null) it.id == pieceId else it.catalogWorkId == workId }
    val sourceId = workId ?: personal?.catalogWorkId
    val work = snapshot.works.firstOrNull { it.id == sourceId }
    val composer = snapshot.composers.firstOrNull { it.id == work?.composerId }
    val title = if (pieceId != null) personal?.title ?: work?.title else work?.title ?: personal?.title
    val composerName = if (pieceId != null) personal?.composer ?: composer?.name.orEmpty() else composer?.name ?: personal?.composer.orEmpty()
    val key = sourceId?.let { "catalog:$it" } ?: pieceId?.let { "piece:$it" }
    var studyExpanded by rememberSaveable { mutableStateOf(false) }
    var editor by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(sourceId) { if (pieceId == null) sourceId?.let { catalogModel.loadWork(it) } }
    val sessions=remember(library.sessions,library.parts,library.links,personal,sourceId) { PracticeInsights.forPiece(library.sessions,library.parts,personal?.id,sourceId,library.links) }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { CatalogBack(L.s(R.string.msg_the_composition_53cf), back) }
        if (sourceId != null && page.loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        if (title == null) {
            if (library.loading || (sourceId != null && page.loading)) item { Text(L.s(R.string.msg_loading_composition_03a3), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            else item { Panel { SectionTitle(L.s(R.string.msg_composition_unavailable_9973)); Text(L.s(R.string.msg_this_work_has_no_saved_information_on_this_d_c958), color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        } else {
            // Header: verified title, composer attribution and catalogue designation; artwork only
            // from the real licensed portrait pipeline, with a graceful fallback (Stage 7A).
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    ComposerPortrait(composer, Modifier.width(96.dp).height(116.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(title, style = MaterialTheme.typography.headlineMedium, maxLines = 4, overflow = TextOverflow.Ellipsis)
                        if (composerName.isNotBlank()) Text(composerName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        val catalogue = work?.catalogLabel.orEmpty().takeUnless { catalogText(title).contains(catalogText(it)) }.orEmpty()
                        if (catalogue.isNotBlank()) Text(catalogue, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            // Prominent primary action: practice selection for saved pieces, otherwise add to
            // repertoire with the existing duplicate prevention (UUID-safe catalogModel.add).
            if (personal != null) item {
                Text(L.fixed(personal.status.label), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                JournalPrimaryButton(onClick = { practice(personal) }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text(L.s(R.string.select_in_practice)) }
            } else if (work != null && composer != null) item {
                val already = alreadyAdded(work, composer, library.pieces)
                JournalPrimaryButton(onClick = { catalogModel.add(work, composer) }, enabled = !already && work.id !in adding, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                    Text(if (already) L.s(R.string.msg_already_in_repertoire_eac8) else if (work.id in adding) L.s(R.string.msg_adding_ffb2) else L.s(R.string.msg_add_to_repertoire_f27e))
                }
                Text(L.s(R.string.msg_some_works_share_a_title_check_the_composer_a406), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            // Compact verified-metadata row: only fields that actually exist (Stage 7A).
            item {
                val cells = listOf(
                    (composer?.epoch?.let(L::fixed) ?: personal?.musicalEra.orEmpty()) to L.s(R.string.discover_field_period),
                    (work?.genre?.let(L::fixed) ?: personal?.genre.orEmpty()) to L.s(R.string.msg_genre_2aa3),
                    work?.catalogLabel.orEmpty() to L.s(R.string.discover_field_catalogue)
                ).filter { it.first.isNotBlank() }
                if (cells.isNotEmpty()) Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface) {
                    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                        cells.forEachIndexed { index, (value, label) ->
                            if (index > 0) VerticalDivider(Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)
                            Column(Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(value, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
            item { EditorialSectionHeader(L.fixed("Overview")) }
            if (!work?.subtitle.isNullOrBlank()) item { Text(work!!.subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            if (!personal?.notes.isNullOrBlank()) item { Text(personal!!.notes, style = MaterialTheme.typography.bodyMedium) }
            item {
                Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface) {
                    Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(L.s(R.string.msg_my_practice_0448), style = MaterialTheme.typography.titleSmall)
                        Text("${localizedPracticeTime(sessions.sumOf { it.durationMillis })} · ${L.quantity(R.plurals.sessions_count, sessions.size)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        sessions.firstOrNull()?.let { Text(localDateTime(it.completedAt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        JournalTextButton(onClick = { history(personal?.id, sourceId) }) { Text(L.s(R.string.msg_practice_history_76e7)) }
                    }
                }
            }
            item { EditorialSectionHeader(L.s(R.string.stage7a_structure_movements)) }
            item {
                Text(L.s(R.string.msg_no_verified_movement_list_is_supplied_by_the_d571), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (key != null) JournalTextButton(onClick = { studyExpanded = !studyExpanded }) { Text(L.s(R.string.msg_sources_study_bfcb)) }
            }
            if (key != null && studyExpanded) item { ListenAndStudy(key, composerName, title, work?.catalogLabel.orEmpty(), recordings.filter { it.workKey == key }, recordingsPage) }
            item { EditorialSectionHeader(L.s(R.string.stage7a_sources_references)) }
            if (composer != null) item { JournalTextButton(onClick = { composerPage(composer.id) }) { Text(L.s(R.string.msg_explore_composer_e4ca)) } }
            if (personal != null) {
                item {
                    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface) {
                        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(L.s(R.string.msg_your_practice_e682), style = MaterialTheme.typography.titleSmall)
                            Choice(L.s(R.string.msg_learning_status_30b8), LearningStatus.entries.map { it.label }, personal.status.label) { label -> model.save(personal.copy(status = LearningStatus.entries.first { it.label == label })) {} }
                            JournalPrimaryButton(onClick = { practice(personal) }) { Text(L.s(R.string.select_in_practice)) }
                            JournalTextButton(onClick = { editor = true }) { Text(L.s(R.string.msg_edit_your_repertoire_entry_4636)) }
                        }
                    }
                }
                items(sessions.take(3), key = { it.id }) { attributed ->
                    val parent = library.sessions.first { it.id == attributed.id }
                    SessionRow(parent, library.summary.partsBySession[parent.id].orEmpty())
                }
                item { JournalTextButton(onClick = { history(personal?.id, sourceId) }) { Text(L.s(R.string.msg_practice_history_4d00)) } }
            }
        }
        if (sourceId != null && page.id == sourceId) {
            page.error?.let { message -> item { CatalogFeedback(message) { catalogModel.loadWork(sourceId, true) } } }
            page.warning?.let { message -> item { CatalogFeedback(message) { catalogModel.loadWork(sourceId, true) } } }
            item { CacheCaption(page.fetchedAt); SourceLink(L.s(R.string.msg_source_open_opus_public_domain_9285), "https://api.openopus.org/work/detail/$sourceId.json") }
        }
    }
    if (editor && personal != null) PieceEditor(personal, { editor = false }, model::save)
}
@Composable private fun MetadataLine(label: String, value: String) {
    if(value.isNotBlank()) Column(verticalArrangement=Arrangement.spacedBy(3.dp)) { Text(L.fixed(label).uppercase(),style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.primary);Text(value,color=MaterialTheme.colorScheme.onSurface) }
}
