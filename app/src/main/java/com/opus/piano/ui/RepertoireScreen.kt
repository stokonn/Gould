package com.opus.piano.ui

import com.opus.piano.appContainer

import com.opus.piano.i18n.L
import com.opus.piano.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.opus.piano.data.*
import com.opus.piano.data.catalogue.DiscoverRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable internal fun RepertoireScreen(state: LibraryState, catalog: CatalogSnapshot, model: OpusViewModel, detail: (Long) -> Unit, cataloguePage:(String)->Unit, previous: () -> Unit = {}, catalogueRepository: DiscoverRepository? = null) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val appearance=LocalAppearance.current
    val store=(androidx.compose.ui.platform.LocalContext.current.appContainer).appearance
    var addMenu by remember { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var editor by rememberSaveable { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<Piece?>(null) }
    val sort=appearance.sort
    val works = remember(catalog.works) { catalog.works.associateBy { it.id } }
    val composerMap = remember(catalog.composers) { catalog.composers.associateBy { it.id } }
    val lastPractice = state.summary.lastPractice
    val practiceTotals = state.summary.practiceTotals
    val pieces = remember(state.pieces,sort,lastPractice) { PracticeInsights.sorted(state.pieces,sort,lastPractice) }
    Column(Modifier.fillMaxSize()) {
        LibraryHeader(previous)
        PrimaryTabRow(selectedTabIndex=selectedTab,modifier=Modifier.fillMaxWidth(),
            containerColor=MaterialTheme.colorScheme.background,contentColor=MaterialTheme.colorScheme.primary) {
            listOf(L.s(R.string.ui101_my_repertoire),L.s(R.string.msg_catalogue_5a24)).forEachIndexed { index,label ->
                Tab(selectedTab==index,onClick={selectedTab=index},modifier=Modifier.heightIn(min=64.dp),
                    text={Text(label,textAlign=androidx.compose.ui.text.style.TextAlign.Center,maxLines=2,overflow=androidx.compose.ui.text.style.TextOverflow.Ellipsis,modifier=Modifier.fillMaxWidth())})
            }
        }
        if(selectedTab==1) DiscoverScreen({cataloguePage(it)},{detail(it)},{cataloguePage(it)},state,repository=catalogueRepository,embedded=true,modifier=Modifier.weight(1f))
        else LazyColumn(Modifier.weight(1f),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(JournalSpace.small)) {
                var sorting by remember { mutableStateOf(false) }
                Box(Modifier.weight(1f)) {
                    JournalSecondaryButton(onClick={sorting=true},modifier=Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Sort,null); Text(L.s(R.string.msg_sort_adc4)) }
                    DropdownMenu(sorting,{sorting=false}) {
                        listOf("Alphabetical","Composer","Recently added","Recently practiced").forEach { option ->
                            DropdownMenuItem(text={Text(L.fixed(option))},onClick={store.edit {it.copy(sort=option)};sorting=false},leadingIcon={if(sort==option) Icon(Icons.Outlined.Check,null)})
                        }
                    }
                }
                Box(Modifier.weight(1f)) {
                    JournalSecondaryButton(onClick={addMenu=true},modifier=Modifier.fillMaxWidth()) {Icon(Icons.Outlined.Add,null);Text(L.s(R.string.msg_add_piece_2cfc))}
                    DropdownMenu(addMenu,{addMenu=false}) {
                        DropdownMenuItem(text={Text(L.s(R.string.msg_catalogue_5a24))},onClick={addMenu=false;selectedTab=1})
                        DropdownMenuItem(text={Text(L.s(R.string.msg_add_manually_a1d9))},onClick={addMenu=false;editingId=null;editor=true})
                    }
                }
            }
        }
        if (state.loading) item { EditorialSkeleton() }
        if (!state.loading && pieces.isEmpty()) item {
            Panel {
                Icon(Icons.Outlined.LibraryMusic,null,tint=MaterialTheme.colorScheme.primary)
                SectionTitle(L.s(R.string.library_repertoire_empty_title))
                Text(L.s(R.string.library_repertoire_empty_body),color=MaterialTheme.colorScheme.onSurfaceVariant)
                JournalPrimaryButton(onClick={selectedTab=1}){Text(L.s(R.string.library_browse_catalogue))}
            }
        }
        items(pieces, key = { it.id }) { piece ->
            Surface(modifier = Modifier.animateItem(), onClick = { detail(piece.id) }, shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.padding(horizontal = 12.dp, vertical = if(appearance.density=="Compact") 8.dp else 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                        ComposerPortrait(composerMap[ComposerIdentity.resolve(piece,works)])
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
    }
    if (editor) PieceEditor(state.pieces.firstOrNull { it.id == editingId }, { editor = false }, model::save)
    deleting?.let { p -> AlertDialog(onDismissRequest = { deleting = null }, title = { Text(L.s(R.string.msg_delete_piece_1c9a)) }, text = { Text(L.s(R.string.msg_remove_p_title_your_completed_practice_histo_32b9, (p.title))) }, confirmButton = { JournalTextButton(onClick = { model.delete(p); deleting = null }) { Text(L.s(R.string.msg_delete_f6fd)) } }, dismissButton = { JournalTextButton(onClick = { deleting = null }) { Text(L.s(R.string.msg_keep_piece_c264)) } }) }
}

/** Keep the previous-history action beside the Library heading at narrow widths and large fonts. */
@Composable internal fun LibraryHeader(previous: () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal=20.dp).padding(top=12.dp,bottom=4.dp)) {
        val compactAction = maxWidth < 400.dp && androidx.compose.ui.platform.LocalDensity.current.fontScale >= 1.5f
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(JournalSpace.small)) {
                Text(L.s(R.string.stage7a_library_title), style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.weight(1f).semantics { heading() })
                if (compactAction) {
                    IconButton(onClick = previous) { Icon(Icons.Outlined.History, L.fixed("Previous")) }
                } else {
                    JournalTextButton(onClick = previous) {
                        Icon(Icons.Outlined.History, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(L.fixed("Previous"))
                    }
                }
        }
    }
}
