package com.opus.piano.ui

import com.opus.piano.i18n.L
import com.opus.piano.R
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
    val appearance=LocalAppearance.current
    val store=(androidx.compose.ui.platform.LocalContext.current.applicationContext as com.opus.piano.OpusApplication).appearance
    var addMenu by remember { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var editor by rememberSaveable { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<Piece?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    var status by rememberSaveable { mutableStateOf("All") }
    var composer by rememberSaveable { mutableStateOf("All") }
    var era by rememberSaveable { mutableStateOf("All") }
    var instrument by rememberSaveable { mutableStateOf("All") }
    var genre by rememberSaveable { mutableStateOf("All") }
    val sort=appearance.sort
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
        PracticeInsights.sorted(matches,sort,lastPractice)
    }
    fun options(values: List<String>) = listOf("All") + values.filter { it.isNotBlank() }.distinctBy(::catalogText).sortedBy(::catalogText)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Row(verticalAlignment=Alignment.CenterVertically) {
            Spacer(Modifier.weight(1f))
            Box { IconButton(onClick={addMenu=true}) { Icon(Icons.Outlined.Add,L.s(R.string.msg_add_piece_2cfc)) }
                DropdownMenu(addMenu,{addMenu=false}) {
                    DropdownMenuItem(text={Text(L.s(R.string.msg_discover_in_catalogue_28f0))},onClick={addMenu=false;explore()})
                    DropdownMenuItem(text={Text(L.s(R.string.msg_add_manually_a1d9))},onClick={addMenu=false;editingId=null;editor=true})
                }
            }
        } }
        item { OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), label = { Text(L.s(R.string.msg_search_your_repertoire_556a)) }, leadingIcon = { Icon(Icons.Outlined.Search, null) }, singleLine = true) }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { filters = !filters }) { Icon(Icons.Outlined.Tune, null); Text(L.s(R.string.msg_filters_96e5)) }
                Choice(L.s(R.string.msg_sort_adc4), listOf("Alphabetical", "Composer", "Recently added", "Recently practiced"), sort) { store.edit { a -> a.copy(sort=it) } }
            }
            if (filters) FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Choice(L.s(R.string.msg_status_bae7), listOf("All") + LearningStatus.entries.map { it.label }, status) { status = it }
                Choice(L.s(R.string.msg_composer_10c3), options(state.pieces.map { it.composer }), composer, translateOptions=false) { composer = it }
                Choice(L.s(R.string.msg_era_f37a), options(eras.values.toList()), era, translateOptions=false) { era = it }
                Choice(L.s(R.string.msg_instrument_f05f), options(state.pieces.map { it.instrument }), instrument, translateOptions=false) { instrument = it }
                Choice(L.s(R.string.msg_genre_2aa3), options(genres.values.toList()), genre, translateOptions=false) { genre = it }
                TextButton(onClick = { status = "All"; composer = "All"; era = "All"; instrument = "All"; genre = "All"; query = "" }) { Text(L.s(R.string.msg_clear_filters_4122)) }
            }
        }
        if (state.loading) item { EditorialSkeleton() }
        if (!state.loading && pieces.isEmpty()) item { Panel { SectionTitle(if (state.pieces.isEmpty()) L.s(R.string.msg_your_collection_begins_here_ca6c) else L.s(R.string.msg_no_matching_compositions_391a)); Text(L.s(R.string.msg_discover_a_work_or_add_one_manually_change_t_fb4a), color = MaterialTheme.colorScheme.onSurfaceVariant); TextButton(onClick=explore) { Text(L.s(R.string.msg_discover_in_catalogue_28f0)) } } }
        items(pieces, key = { it.id }) { piece ->
            Surface(modifier = Modifier.animateItem(), onClick = { detail(piece.id) }, shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.background) {
                Column(Modifier.padding(horizontal = 12.dp, vertical = if(appearance.density=="Compact") 8.dp else 18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PieceRowContent(piece.title,piece.composer,
                            works[piece.catalogWorkId]?.catalogLabel?.takeIf { it.isNotBlank() && !catalogText(piece.title).contains(catalogText(it)) }.orEmpty(),
                            Modifier.weight(1f))
                        Box {
                            var expanded by remember { mutableStateOf(false) }
                            IconButton(onClick = { expanded = true }) { Icon(Icons.Outlined.MoreVert, L.s(R.string.msg_actions_for_piece_title_311c, (piece.title))) }
                            DropdownMenu(expanded, { expanded = false }) {
                                DropdownMenuItem(text = { Text(L.s(R.string.msg_edit_5301)) }, onClick = { expanded = false; editingId = piece.id; editor = true })
                                DropdownMenuItem(text = { Text(L.s(R.string.msg_delete_f6fd)) }, onClick = { expanded = false; deleting = piece })
                            }
                        }
                    }
                    Text("${L.fixed(piece.status.label)}${practiceTotals[piece.id]?.let { " · ${L.quantity(R.plurals.minutes_count,(it/60000).toInt())}" }.orEmpty()}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LinearProgressIndicator(progress = { learningProgress(piece.status) }, modifier = Modifier.fillMaxWidth().height(2.dp), trackColor = MaterialTheme.colorScheme.primary.copy(alpha = .1f))
                }
            }
        }
    }
    if (editor) PieceEditor(state.pieces.firstOrNull { it.id == editingId }, { editor = false }, model::save)
    deleting?.let { p -> AlertDialog(onDismissRequest = { deleting = null }, title = { Text(L.s(R.string.msg_delete_piece_1c9a)) }, text = { Text(L.s(R.string.msg_remove_p_title_your_completed_practice_histo_32b9, (p.title))) }, confirmButton = { TextButton(onClick = { model.delete(p); deleting = null }) { Text(L.s(R.string.msg_delete_f6fd)) } }, dismissButton = { TextButton(onClick = { deleting = null }) { Text(L.s(R.string.msg_keep_piece_c264)) } }) }
}
