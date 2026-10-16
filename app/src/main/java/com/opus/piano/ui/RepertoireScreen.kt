package com.opus.piano.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.opus.piano.data.*

@OptIn(ExperimentalLayoutApi::class)
@Composable internal fun RepertoireScreen(state: LibraryState, catalog: CatalogSnapshot, model: OpusViewModel, explore: () -> Unit, detail: (Long) -> Unit) {
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var editor by rememberSaveable { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<Piece?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    var status by rememberSaveable { mutableStateOf("All") }
    var composer by rememberSaveable { mutableStateOf("All") }
    var era by rememberSaveable { mutableStateOf("All") }
    var instrument by rememberSaveable { mutableStateOf("All") }
    var genre by rememberSaveable { mutableStateOf("All") }
    var sort by rememberSaveable { mutableStateOf("Alphabetical") }
    var filters by rememberSaveable { mutableStateOf(false) }
    val works = remember(catalog.works) { catalog.works.associateBy { it.id } }
    val composerMap = remember(catalog.composers) { catalog.composers.associateBy { it.id } }
    val eras = remember(state.pieces, works, composerMap) { state.pieces.associate { p -> p.id to p.musicalEra.ifBlank { composerMap[ComposerIdentity.resolve(p, works)]?.epoch.orEmpty() } } }
    val genres = remember(state.pieces, works) { state.pieces.associate { p -> p.id to p.genre.ifBlank { works[p.catalogWorkId]?.genre.orEmpty() } } }
    val lastPractice = state.summary.lastPractice
    val practiceTotals = state.summary.practiceTotals
    val pieces = remember(state.pieces, query, status, composer, era, instrument, genre, sort, eras, genres, lastPractice, works) {
        val tokens = catalogText(query).split(' ').filter { it.isNotBlank() }
        val matches = state.pieces.filter { p ->
            val text = catalogText("${p.title} ${p.composer} ${works[p.catalogWorkId]?.catalogLabel.orEmpty()}")
            tokens.all { text.contains(it) } && (status == "All" || p.status.label == status) && (composer == "All" || catalogText(p.composer) == catalogText(composer)) &&
                (era == "All" || catalogText(eras[p.id].orEmpty()) == catalogText(era)) && (instrument == "All" || catalogText(p.instrument) == catalogText(instrument)) && (genre == "All" || catalogText(genres[p.id].orEmpty()) == catalogText(genre))
        }
        when (sort) {
            "Composer" -> matches.sortedWith(compareBy({ catalogText(it.composer) }, { catalogText(it.title) }))
            "Recently added" -> matches.sortedWith(compareByDescending<Piece> { it.createdAt }.thenByDescending { it.id })
            "Recently practiced" -> matches.sortedWith(compareByDescending<Piece> { lastPractice[it.id] ?: 0 }.thenBy { catalogText(it.title) })
            else -> matches.sortedBy { catalogText(it.title) }
        }
    }
    fun options(values: List<String>) = listOf("All") + values.filter { it.isNotBlank() }.distinctBy(::catalogText).sortedBy(::catalogText)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Heading("THE PERSONAL ARCHIVE", "Repertoire", "${state.pieces.size} compositions · ${state.pieces.count(::mastered)} performance ready") }
            IconButton(onClick = explore) { Icon(Icons.Outlined.Search, "Universal search") }
        } }
        item { FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = explore) { Icon(Icons.Outlined.TravelExplore, null); Spacer(Modifier.width(6.dp)); Text("Discover") }
            OutlinedButton(onClick = { editingId = null; editor = true }) { Icon(Icons.Outlined.Add, null); Text("Add manually") }
        } }
        item { OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), label = { Text("Search your repertoire") }, leadingIcon = { Icon(Icons.Outlined.Search, null) }, singleLine = true) }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { filters = !filters }) { Icon(Icons.Outlined.Tune, null); Text("Filters") }
                Choice("Sort", listOf("Alphabetical", "Composer", "Recently added", "Recently practiced"), sort) { sort = it }
            }
            if (filters) FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Choice("Status", listOf("All") + LearningStatus.entries.map { it.label }, status) { status = it }
                Choice("Composer", options(state.pieces.map { it.composer }), composer) { composer = it }
                Choice("Era", options(eras.values.toList()), era) { era = it }
                Choice("Instrument", options(state.pieces.map { it.instrument }), instrument) { instrument = it }
                Choice("Genre", options(genres.values.toList()), genre) { genre = it }
                TextButton(onClick = { status = "All"; composer = "All"; era = "All"; instrument = "All"; genre = "All"; query = "" }) { Text("Clear filters") }
            }
        }
        if (state.loading) item { EditorialSkeleton() }
        if (!state.loading && pieces.isEmpty()) item { Panel { SectionTitle(if (state.pieces.isEmpty()) "Your collection begins here" else "No matching compositions"); Text("Discover a work or add one manually. Change the filters to explore your collection.", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        items(pieces, key = { it.id }) { piece ->
            Surface(modifier = Modifier.animateItem(), onClick = { detail(piece.id) }, shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.background) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(piece.title, style = MaterialTheme.typography.titleLarge, maxLines = 3, overflow = TextOverflow.Ellipsis)
                            Text(piece.composer, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            works[piece.catalogWorkId]?.catalogLabel?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                        }
                        Box {
                            var expanded by remember { mutableStateOf(false) }
                            IconButton(onClick = { expanded = true }) { Icon(Icons.Outlined.MoreVert, "Actions for ${piece.title}") }
                            DropdownMenu(expanded, { expanded = false }) {
                                DropdownMenuItem(text = { Text("Edit") }, onClick = { expanded = false; editingId = piece.id; editor = true })
                                DropdownMenuItem(text = { Text("Delete") }, onClick = { expanded = false; deleting = piece })
                            }
                        }
                    }
                    Text("${piece.status.label}${practiceTotals[piece.id]?.let { " · ${it / 60000} min practiced" }.orEmpty()}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LinearProgressIndicator(progress = { learningProgress(piece.status) }, modifier = Modifier.fillMaxWidth().height(2.dp), trackColor = Gold.copy(alpha = .1f))
                }
            }
        }
    }
    if (editor) PieceEditor(state.pieces.firstOrNull { it.id == editingId }, { editor = false }, model::save)
    deleting?.let { p -> AlertDialog(onDismissRequest = { deleting = null }, title = { Text("Delete piece?") }, text = { Text("Remove “${p.title}”? Your completed practice history and earned badges will remain.") }, confirmButton = { TextButton(onClick = { model.delete(p); deleting = null }) { Text("Delete") } }, dismissButton = { TextButton(onClick = { deleting = null }) { Text("Keep piece") } }) }
}
