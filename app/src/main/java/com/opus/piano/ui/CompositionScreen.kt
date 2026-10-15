package com.opus.piano.ui

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
    var tab by rememberSaveable(workId, pieceId) { mutableIntStateOf(0) }
    var editor by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(sourceId) { sourceId?.let { catalogModel.loadWork(it) } }
    val sessions = remember(library.sessions, library.parts, personal, sourceId) { attributedSessions(library.sessions, library.parts).filter { (personal != null && it.pieceId == personal.id) || (sourceId != null && it.catalogWorkId == sourceId) } }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { CatalogBack("The composition", back) }
        if (sourceId != null && page.loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        if (title == null) {
            if (library.loading || (sourceId != null && page.loading)) item { Text("Loading composition…", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            else item { Panel { SectionTitle("Composition unavailable"); Text("This work has no saved information on this device.", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        } else {
            item {
                Text((work?.genre ?: personal?.genre.orEmpty()).ifBlank { "COMPOSITION" }.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                Text(title, style = MaterialTheme.typography.headlineMedium)
                Text(composerName, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                if (composer != null) TextButton(onClick = { composerPage(composer.id) }) { Text("Explore composer") }
                val metadata = listOf(work?.catalogLabel.orEmpty(), personal?.instrument.orEmpty(), composer?.epoch ?: personal?.musicalEra.orEmpty()).filter { it.isNotBlank() }
                if (metadata.isNotEmpty()) Text(metadata.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Overview", "Movements", "Listen & Study", "Practice").forEachIndexed { i, label -> FilterChip(selected = tab == i, onClick = { tab = i }, label = { Text(label) }) }
                }
            }
            when (tab) {
                0 -> {
                    item { Panel {
                        SectionTitle("About the work")
                        Text(work?.subtitle?.ifBlank { null } ?: personal?.notes?.ifBlank { null } ?: "No sourced description or historical context is available for this composition.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (personal != null && work != null && personal.notes.isNotBlank() && personal.notes != work.subtitle) { Text("YOUR NOTES", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary); Text(personal.notes) }
                    } }
                    item { Panel {
                        MetadataLine("Catalogue", work?.catalogLabel.orEmpty())
                        MetadataLine(if (work?.musicalKey.isNullOrBlank() && !work?.keyFromTitle.isNullOrBlank()) "Key · stated in source title" else "Key", work?.musicalKey?.ifBlank { work.keyFromTitle }.orEmpty())
                        MetadataLine("Genre", personal?.genre?.ifBlank { work?.genre.orEmpty() } ?: work?.genre.orEmpty())
                        MetadataLine("Instrumentation", personal?.instrument.orEmpty())
                        if (personal != null) MetadataLine("Difficulty", personal.difficulty.label)
                        MetadataLine("Era", personal?.musicalEra?.ifBlank { composer?.epoch.orEmpty() } ?: composer?.epoch.orEmpty())
                        if (sourceId != null) Text("Open Opus #$sourceId", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } }
                }
                1 -> item { Panel {
                    SectionTitle("Movements")
                    Text("No verified movement list is supplied by the current catalogue. Consult a score through Listen & Study; movement names and ordering are not inferred from the title.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = { tab = 2 }) { Text("Find a score") }
                } }
                2 -> if (key != null) item { ListenAndStudy(key, composerName, title, work?.catalogLabel.orEmpty(), recordings.filter { it.workKey == key }, recordingsPage) }
                3 -> {
                    if (personal != null) {
                        item { Panel {
                            SectionTitle("Your practice")
                            Choice("Learning status", LearningStatus.entries.map { it.label }, personal.status.label) { label -> model.save(personal.copy(status = LearningStatus.entries.first { it.label == label })) {} }
                            Text("${sessions.size} completed sessions · ${sessions.sumOf { it.durationMillis } / 60000} minutes", color = MaterialTheme.colorScheme.primary)
                            Button(onClick = { practice(personal) }) { Text("Practice this piece") }
                            TextButton(onClick = { editor = true }) { Text("Edit your repertoire entry") }
                        } }
                    }
                    item { SectionTitle("Practice history") }
                    if (sessions.isEmpty()) item { Text("No completed sessions for this composition yet.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    items(sessions.take(30), key = { it.id }) { SessionRow(it) }
                    if (sessions.size > 30) item { Text("Showing the 30 most recent sessions of ${sessions.size}.", style = MaterialTheme.typography.bodySmall) }
                }
            }
            if (personal == null && work != null && composer != null) item {
                val already = alreadyAdded(work, composer, library.pieces)
                Button(onClick = { catalogModel.add(work, composer) }, enabled = !already && work.id !in adding, modifier = Modifier.fillMaxWidth()) { Text(if (already) "Already in repertoire" else if (work.id in adding) "Adding…" else "Add to repertoire") }
                Text("Some works share a title. Check the composer and catalogue number before adding.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (sourceId != null && page.id == sourceId) {
            page.error?.let { message -> item { CatalogFeedback(message) { catalogModel.loadWork(sourceId, true) } } }
            page.warning?.let { message -> item { CatalogFeedback(message) { catalogModel.loadWork(sourceId, true) } } }
            item { CacheCaption(page.fetchedAt); SourceLink("Source · Open Opus · public domain", "https://api.openopus.org/work/detail/$sourceId.json") }
        }
    }
    if (editor && personal != null) PieceEditor(personal, { editor = false }, model::save)
}
@Composable private fun MetadataLine(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) { Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary); Text(value.ifBlank { "Not supplied" }, color = if (value.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else Ivory) }
}
