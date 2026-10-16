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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.opus.piano.data.*

@OptIn(ExperimentalLayoutApi::class)
@Composable fun CompositionScreen(workId: String?, pieceId: Long?, catalogModel: CatalogViewModel, experience: ExperienceViewModel,
    library: LibraryState, model: OpusViewModel, back: () -> Unit, composerPage: (String) -> Unit, recordingsPage: (String) -> Unit, practice: (Piece) -> Unit) {
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
    var tab by rememberSaveable(workId, pieceId) { mutableIntStateOf(0) }
    var editor by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(sourceId) { sourceId?.let { catalogModel.loadWork(it) } }
    val sessions=remember(library.sessions,library.parts,personal,sourceId) { PracticeInsights.forPiece(library.sessions,library.parts,personal?.id,sourceId) }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { CatalogBack(L.s(R.string.msg_the_composition_53cf), back) }
        if (sourceId != null && page.loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        if (title == null) {
            if (library.loading || (sourceId != null && page.loading)) item { Text(L.s(R.string.msg_loading_composition_03a3), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            else item { Panel { SectionTitle(L.s(R.string.msg_composition_unavailable_9973)); Text(L.s(R.string.msg_this_work_has_no_saved_information_on_this_d_c958), color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        } else {
            item {
                Text((work?.genre ?: personal?.genre.orEmpty()).ifBlank { L.s(R.string.msg_composition_e860) }.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                Text(title, style = MaterialTheme.typography.headlineMedium)
                Text(composerName, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                if (personal != null) {
                    Text(L.fixed(personal.status.label),style=MaterialTheme.typography.labelLarge)
                    Button(onClick={practice(personal)},modifier=Modifier.fillMaxWidth().heightIn(min=52.dp)) { Text(L.s(R.string.msg_start_practice_ae1b)) }
                }
                if (composer != null) TextButton(onClick = { composerPage(composer.id) }) { Text(L.s(R.string.msg_explore_composer_e4ca)) }
                val metadata = listOf(work?.catalogLabel.orEmpty().takeUnless { catalogText(title).contains(catalogText(it)) }.orEmpty(), work?.musicalKey?.ifBlank { work.keyFromTitle }.orEmpty(), composer?.epoch ?: personal?.musicalEra.orEmpty()).filter { it.isNotBlank() }
                if (metadata.isNotEmpty()) Text(metadata.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Overview", "Movements", "Sources & Study", "My Practice").forEachIndexed { i, label -> FilterChip(selected = tab == i, onClick = { tab = i }, label = { Text(L.fixed(label)) }) }
                }
            }
            when (tab) {
                0 -> {
                    if (!work?.subtitle.isNullOrBlank()) item { SectionTitle(L.s(R.string.msg_about_the_work_0fef));Text(work!!.subtitle) }
                    if (!personal?.notes.isNullOrBlank()) item { SectionTitle(L.s(R.string.msg_repertoire_notes_f720));Text(personal!!.notes) }
                    item {
                        SectionTitle(L.s(R.string.msg_my_practice_0448))
                        Text(localizedPracticeTime(sessions.sumOf { it.durationMillis }))
                        Text(L.quantity(R.plurals.sessions_count,sessions.size))
                        sessions.firstOrNull()?.let { Text(localDateTime(it.completedAt)) }
                        TextButton(onClick={tab=3}) { Text(L.s(R.string.msg_practice_history_76e7)) }
                    }
                    item { Column(verticalArrangement=Arrangement.spacedBy(6.dp)) {
                        MetadataLine("Genre", personal?.genre?.ifBlank { work?.genre.orEmpty() } ?: work?.genre.orEmpty())
                        MetadataLine("Instrumentation", personal?.instrument.orEmpty())
                        if (personal != null && personal.difficulty != Difficulty.UNRATED) MetadataLine("Difficulty", L.fixed(personal.difficulty.label))
                    } }
                }
                1 -> item { Panel {
                    SectionTitle(L.s(R.string.msg_movements_e4a3))
                    Text(L.s(R.string.msg_no_verified_movement_list_is_supplied_by_the_d571), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = { tab = 2 }) { Text(L.s(R.string.msg_find_a_score_6ee5)) }
                } }
                2 -> if (key != null) item {
                    TextButton(onClick={studyExpanded=!studyExpanded}) { Text(L.s(R.string.msg_sources_study_bfcb)) }
                    if(studyExpanded) ListenAndStudy(key, composerName, title, work?.catalogLabel.orEmpty(), recordings.filter { it.workKey == key }, recordingsPage)
                }
                3 -> {
                    if (personal != null) {
                        item { Panel {
                            SectionTitle(L.s(R.string.msg_your_practice_e682))
                            Choice(L.s(R.string.msg_learning_status_30b8), LearningStatus.entries.map { it.label }, personal.status.label) { label -> model.save(personal.copy(status = LearningStatus.entries.first { it.label == label })) {} }
                            Text(L.s(R.string.msg_sessions_size_completed_sessions_sessions_su_05a0, (sessions.size), (sessions.sumOf { it.durationMillis } / 60000)), color = MaterialTheme.colorScheme.primary)
                            Button(onClick = { practice(personal) }) { Text(L.s(R.string.msg_practice_this_piece_d355)) }
                            TextButton(onClick = { editor = true }) { Text(L.s(R.string.msg_edit_your_repertoire_entry_4636)) }
                        } }
                    }
                    item { SectionTitle(L.s(R.string.msg_practice_history_4d00)) }
                    if (sessions.isEmpty()) item { Text(L.s(R.string.msg_no_completed_sessions_for_this_composition_y_1b30), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    items(sessions.take(30), key = { it.id }) { attributed ->
                        Text("${L.fixed("My Practice")} · ${localizedPracticeTime(attributed.durationMillis)}",style=MaterialTheme.typography.labelLarge)
                        val parent=library.sessions.first { it.id==attributed.id }
                        SessionRow(parent, library.summary.partsBySession[parent.id].orEmpty())
                    }
                    if (sessions.size > 30) item { Text(L.s(R.string.msg_showing_the_30_most_recent_sessions_of_sessi_304f, (sessions.size)), style = MaterialTheme.typography.bodySmall) }
                }
            }
            if (personal == null && work != null && composer != null) item {
                val already = alreadyAdded(work, composer, library.pieces)
                Button(onClick = { catalogModel.add(work, composer) }, enabled = !already && work.id !in adding, modifier = Modifier.fillMaxWidth()) { Text(if (already) L.s(R.string.msg_already_in_repertoire_eac8) else if (work.id in adding) L.s(R.string.msg_adding_ffb2) else L.s(R.string.msg_add_to_repertoire_f27e)) }
                Text(L.s(R.string.msg_some_works_share_a_title_check_the_composer_a406), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
