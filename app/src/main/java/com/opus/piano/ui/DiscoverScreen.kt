package com.opus.piano.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.opus.piano.OpusApplication
import com.opus.piano.R
import com.opus.piano.i18n.L
import com.opus.piano.data.catalogue.*
import com.opus.piano.data.catalogue.providers.SourceState
import kotlinx.coroutines.*

/** Nav-entry memory only. Queries survive detail/Back and recreation, never process storage. */
internal class DiscoverSearchSession:androidx.lifecycle.ViewModel() {
    var query by mutableStateOf("")
}
@Composable internal fun DiscoverScreen(open:(String)->Unit,openPiece:(Long)->Unit,openCatalogWork:(String)->Unit,library:LibraryState,repository:DiscoverRepository?=null) {
    val app=LocalContext.current.applicationContext as OpusApplication
    val discover=repository ?: app.discover
    // Raw catalogue queries are transient; exclude them from persisted saved state.
    val session:DiscoverSearchSession=androidx.lifecycle.viewmodel.compose.viewModel()
    val query=session.query
    var active by rememberSaveable {mutableStateOf(false)}
    // Settings consent enables debounced provider search. The transient query is never saved
    // across process death; screen restoration without a query cannot make a request.
    val catalogOnline by app.catalogOnline.enabled.collectAsState()
    // Stage 7A round 3: Discover header privacy popover state (browser site-info style).
    var privacyInfo by remember {mutableStateOf(false)}
    var category by rememberSaveable {mutableStateOf("all")}
    var artistId by rememberSaveable {mutableStateOf<String?>(null)}
    var period by rememberSaveable {mutableStateOf<String?>(null)}
    var availableFilters by remember {mutableStateOf(DiscoverFilters())}
    var filtersExpanded by rememberSaveable {mutableStateOf(false)}
    var artistMenu by remember {mutableStateOf(false)}
    var periodMenu by remember {mutableStateOf(false)}
    var paging by remember {mutableStateOf(false)}
    var pageJob by remember {mutableStateOf<Job?>(null)}
    var searchEpoch by remember {mutableLongStateOf(0L)}
    var limitReached by remember {mutableStateOf(false)}
    val filter=SearchFilter(category,artistId,period)
    val searchKey=listOf(query,category,artistId,period,active.toString(),catalogOnline.toString())
    val currentSearchKey by rememberUpdatedState(searchKey)
    var rows by remember {mutableStateOf(emptyList<CatalogueEntity>())}
    var initialLocalIds by remember {mutableStateOf(emptySet<String>())}
    var providerEntityIds by remember {mutableStateOf(emptySet<String>())}
    var recent by remember {mutableStateOf(emptyList<CatalogueEntity>())}
    var workTypes by remember {mutableStateOf(emptyMap<String,String>())}
    var rowInfo by remember {mutableStateOf(emptyMap<String,CatalogueRowInfo>())}
    var recentTypes by remember {mutableStateOf(emptyMap<String,String>())}
    var toolsExpanded by rememberSaveable {mutableStateOf(false)}
    var sources by remember {mutableStateOf(emptyList<SearchSource>())}
    var candidates by remember {mutableStateOf(emptyList<CatalogueCandidate>())}
    var cursor by remember {mutableStateOf<String?>(null)}
    var loading by remember {mutableStateOf(false)}
    var error by remember {mutableStateOf(false)}
    var revision by remember {mutableIntStateOf(0)}
    var cacheBytes by remember {mutableLongStateOf(0L)}
    var historyEnabled by remember {mutableStateOf(app.discoverHistory.enabled)}
    val scope=rememberCoroutineScope()
    val language=LocalConfiguration.current.locales[0].language.let {if(it=="cs")"cs" else "en"}
    LaunchedEffect(query,category,artistId,period,active,catalogOnline,revision,language) {
        pageJob?.cancel();searchEpoch++;paging=false;limitReached=false;sources=emptyList();candidates=emptyList()
        error=false;loading=true;rows=emptyList();initialLocalIds=emptySet();providerEntityIds=emptySet()
        cacheBytes=app.providerCache.bytes()
        try {
            if(!active) {val page=discover.landing();rows=page.items;workTypes=page.workTypes;rowInfo=page.rowInfo;cursor=null;sources=emptyList()}
            else {
                var initial:List<CatalogueEntity>?=null
                discover.automaticSearch(query,filter,language,catalogOnline).collect {result->
                    if(initial==null){initial=result.page.items;initialLocalIds=result.page.items.map {it.id}.toSet()}
                    rows=(initial.orEmpty()+result.page.items.filterNot {it.id in initialLocalIds}).take(500)
                    providerEntityIds=result.providerEntityIds
                    workTypes=workTypes+result.page.workTypes;rowInfo=rowInfo+result.page.rowInfo;cursor=result.continuation;sources=result.sources
                    candidates=result.candidates;limitReached=result.limitReached
                }
            }
            availableFilters=discover.filters()
            val opened=withContext(Dispatchers.IO){app.discoverHistory.entries().mapNotNull {id->(discover.detail(id) as? CatalogueAccess.Available)?.value}}
            recent=opened.map {it.entity};recentTypes=opened.mapNotNull {detail->detail.workType?.let {detail.entity.id to it}}.toMap()
        } catch(e:CancellationException){throw e} catch(_:Exception){error=true} finally {loading=false}
    }
    // "Available offline" shows only genuinely local content: the user's catalogue-linked
    // repertoire plus locally cached catalogue works. Stage 5 bundles ship no management UI yet,
    // so no bundle entries are shown (plan C6).
    val catalogSnapshot by app.catalogRepository.snapshot.collectAsState(com.opus.piano.data.CatalogSnapshot(emptyList(),emptyList()))
    val offlineRows=remember(library.pieces,catalogSnapshot) {
        val worksById=catalogSnapshot.works.associateBy {it.id}
        val composersById=catalogSnapshot.composers.associateBy {it.id}
        val linked=library.pieces.filter {it.catalogWorkId!=null && worksById.containsKey(it.catalogWorkId)}.distinctBy {it.catalogWorkId}.take(4)
        val linkedIds=linked.mapNotNull {it.catalogWorkId}.toSet()
        val cached=catalogSnapshot.works.filter {(it.popular||it.recommended) && it.id !in linkedIds}.take(maxOf(0,4-linked.size))
        linked.map {piece->OfflineRow(worksById[piece.catalogWorkId]?.title ?: piece.title,piece.composer,
            worksById[piece.catalogWorkId]?.catalogLabel.orEmpty(),piece.id,piece.catalogWorkId ?: "",composersById[worksById[piece.catalogWorkId]?.composerId])}+
            cached.map {work->OfflineRow(work.title,composersById[work.composerId]?.name.orEmpty(),work.catalogLabel,null,work.id,composersById[work.composerId])}
    }
    LazyColumn(contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item {
            // Stage 7A round 3 (owner): header row with the privacy affordance in the
            // upper-right, browser site-information style. The persistent privacy card
            // below the search field is removed; the same truthful disclosure opens as a
            // compact anchored popover that dismisses on outside tap or Back.
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.Top,horizontalArrangement=Arrangement.spacedBy(JournalSpace.small)) {
                Column(Modifier.weight(1f).padding(top=JournalSpace.tiny),verticalArrangement=Arrangement.spacedBy(JournalSpace.tiny)) {
                    Text(L.s(R.string.discover),style=MaterialTheme.typography.headlineLarge,modifier=Modifier.semantics {heading()})
                    Text(L.s(R.string.discover_explore),style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box {
                    IconButton(onClick={privacyInfo=true},modifier=Modifier.semantics {contentDescription=L.s(R.string.stage7a_privacy_info)}) {
                        Icon(Icons.Outlined.Lock,L.s(R.string.stage7a_privacy_info),tint=MaterialTheme.colorScheme.primary)
                    }
                    DropdownMenu(expanded=privacyInfo,onDismissRequest={privacyInfo=false}) {
                        DropdownMenuItem(text={Text(L.s(R.string.stage7a_privacy_notice),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)},
                            onClick={privacyInfo=false},leadingIcon={Icon(Icons.Outlined.Lock,null,tint=MaterialTheme.colorScheme.primary)})
                    }
                }
            }
        }
        item {EditorialSearchField(query,{session.query=it;active=true})}
        if(active && !catalogOnline)item {
            Text(L.s(R.string.stage7a_online_off_hint),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {LazyRow(horizontalArrangement=Arrangement.spacedBy(JournalSpace.tiny),contentPadding=PaddingValues(horizontal=JournalSpace.small)) {
            items(listOf("all","works","artists","albums","lists")) {key->CategoryTile(key,category==key){category=key;active=true}}
        }}
        if(availableFilters.artists.isNotEmpty() || availableFilters.periods.isNotEmpty())item {
            TextButton({filtersExpanded=!filtersExpanded}){Text(L.s(R.string.discover_filters))}
            if(filtersExpanded) {
                Box {
                    TextButton({artistMenu=true}){Text(availableFilters.artists.firstOrNull {it.id==artistId}?.let {shownTitle("artist",it.title)} ?: L.s(R.string.discover_any_artist))}
                    DropdownMenu(artistMenu,{artistMenu=false}) {
                        DropdownMenuItem(text={Text(L.s(R.string.discover_any_artist))},onClick={artistId=null;artistMenu=false;active=true})
                        availableFilters.artists.forEach {artist->DropdownMenuItem(text={Text(shownTitle("artist",artist.title))},onClick={artistId=artist.id;artistMenu=false;active=true})}
                    }
                }
                Box {
                    TextButton({periodMenu=true}){Text(period ?: L.s(R.string.discover_any_period))}
                    DropdownMenu(periodMenu,{periodMenu=false}) {
                        DropdownMenuItem(text={Text(L.s(R.string.discover_any_period))},onClick={period=null;periodMenu=false;active=true})
                        availableFilters.periods.forEach {value->DropdownMenuItem(text={Text(value)},onClick={period=value;periodMenu=false;active=true})}
                    }
                }
            }
        }
        if(loading && rows.isEmpty())item {LinearProgressIndicator(Modifier.fillMaxWidth())}
        if(error)item {Text(L.s(R.string.discover_error));TextButton({revision++}){Text(L.s(R.string.discover_retry))}}
        val sourceFailure=sources.any {it.state !in setOf(SourceState.FRESH,SourceState.CACHED) || it.refreshState!=null}
        // Stage 7A (owner): never claim "no local content" on the landing screen while the
        // "Available offline" rows below show real local results — only a search with no
        // matches shows this.
        if(active && rows.isEmpty() && !loading)item {Text(L.s(R.string.discover_empty))}
        val localRows=rows.filter {it.id in initialLocalIds || it.id !in providerEntityIds}
        val onlineRows=rows.filter {it.id !in initialLocalIds && it.id in providerEntityIds}
        if(active && localRows.isNotEmpty())item {EditorialSectionHeader(L.s(R.string.discover_local_results))}
        items(if(active)localRows else rows,key={it.id}) {entity->CatalogueRow(entity,workTypes[entity.id],rowInfo[entity.id]){open(entity.id)}}
        if(active && catalogOnline && query.trim().length>=2) {
            item {EditorialSectionHeader(L.s(R.string.discover_online_results))}
            if(loading)item {LinearProgressIndicator(Modifier.fillMaxWidth())}
            if(sourceFailure)item {
                val unavailableConnection=sources.any {it.error=="network_policy"}
                Text(L.s(if(unavailableConnection)R.string.discover_connection_unavailable else R.string.discover_source_warning),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(enabled=!loading,onClick={revision++}){Text(L.s(R.string.discover_retry))}
            }
            if(!loading && !sourceFailure && onlineRows.isEmpty())item {Text(L.s(R.string.discover_no_additional_results),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
        }
        items(onlineRows,key={it.id}) {entity->CatalogueRow(entity,workTypes[entity.id],rowInfo[entity.id]){open(entity.id)}}
        val unresolved=candidates.filterNot {candidate->rows.any {it.id==candidate.id}}
        if(unresolved.isNotEmpty())item {EditorialSectionHeader(L.s(R.string.discover_results_to_check),L.s(R.string.discover_candidate))}
        items(unresolved,key={"candidate:"+it.id}) {candidate->
            CatalogueRow(CatalogueEntity(candidate.id,candidate.kindHint,candidate.title,status="quarantined")){open(candidate.id)}
        }
        if(limitReached)item {Text(L.s(R.string.discover_refine_search))}
        if(cursor!=null)item {TextButton(enabled=!paging && !loading,onClick={
            val next=cursor ?: return@TextButton
            val requestedKey=searchKey;val requestedEpoch=searchEpoch;val requestedQuery=query;val requestedFilter=filter;val requestedOnline=catalogOnline && query.trim().length>=2
            paging=true
            pageJob=scope.launch {
                try {
                    val result=discover.continueSearch(requestedQuery,requestedFilter,language,requestedOnline,next)
                    val page=result.page
                    if(searchEpoch==requestedEpoch && currentSearchKey==requestedKey){rows=(rows+page.items).distinctBy {it.id};workTypes=workTypes+page.workTypes;rowInfo=rowInfo+page.rowInfo;cursor=result.continuation;candidates=(candidates+result.candidates).distinctBy {it.id};sources=result.sources;providerEntityIds=providerEntityIds+result.providerEntityIds;limitReached=result.limitReached}
                } catch(e:CancellationException){throw e}catch(_:Exception){if(searchEpoch==requestedEpoch && currentSearchKey==requestedKey){cursor=null;error=true}}
                finally {if(searchEpoch==requestedEpoch && currentSearchKey==requestedKey)paging=false}
            }
        }){Text(L.s(R.string.discover_more))}}
        if(!active && recent.isNotEmpty()) {
            item {EditorialSectionHeader(L.s(R.string.stage7a_recently_viewed))}
            item {LazyRow(horizontalArrangement=Arrangement.spacedBy(JournalSpace.tiny),contentPadding=PaddingValues(horizontal=JournalSpace.small)) {
                items(recent,key={"recent:"+it.id}) {entity->
                    RecentCard(entity,recentTypes[entity.id],{open(entity.id)}) {scope.launch {withContext(Dispatchers.IO){app.discoverHistory.remove(entity.id)};recent=recent.filterNot {it.id==entity.id}}}
                }
            }}
        }
        if(!active && offlineRows.isNotEmpty()) {
            item {EditorialSectionHeader(L.s(R.string.stage7a_available_offline),L.s(R.string.stage7a_offline_subtitle))}
            items(offlineRows,key={"offline:"+(it.pieceId ?: it.workId)}) {row->
                OfflineRowCard(row) {if(row.pieceId!=null) openPiece(row.pieceId) else openCatalogWork(row.workId)}
            }
        }
        item {EditorialDisclosure(L.s(R.string.discover_advanced),toolsExpanded,{toolsExpanded=!toolsExpanded})}
        if(toolsExpanded) {
            if(sources.isNotEmpty())item {EditorialSectionHeader(L.s(R.string.discover_search_sources))}
            items(sources,key={"source:"+it.provider+":"+it.requestKind}) {source->
                Text(providerLabel(source.provider)+source.requestKind?.let {" · "+sourceKindLabel(it)}.orEmpty(),style=MaterialTheme.typography.labelLarge)
                Text(sourceLabel(source.refreshState ?: source.state),style=MaterialTheme.typography.bodySmall)
                if(source.state==SourceState.STALE)Text(L.s(R.string.discover_cached_kept),style=MaterialTheme.typography.bodySmall)
            }
        item {
            HorizontalDivider()
            Text(L.s(R.string.discover_cache_consequences),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            Text(android.text.format.Formatter.formatShortFileSize(LocalContext.current,cacheBytes),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick={scope.launch {discover.clearResponseCache();cacheBytes=app.providerCache.bytes()}}){Text(L.s(R.string.discover_clear_cache))}
        }
            item {
                HorizontalDivider()
                Row(verticalAlignment=Alignment.CenterVertically) {
                    Switch(historyEnabled,{value->
                        historyEnabled=value
                        scope.launch {
                            withContext(Dispatchers.IO){app.discoverHistory.enabled(value)}
                            if(!value)recent=emptyList()
                        }
                    },modifier=Modifier.semantics {contentDescription=L.s(R.string.discover_history)})
                    Text(L.s(R.string.discover_history))
                }
            }
            item {TextButton({scope.launch {withContext(Dispatchers.IO){app.discoverHistory.clear()};recent=emptyList()}}){Text(L.s(R.string.discover_clear_history))}}
        }

    }
}
/** One row of the "Available offline" section; pieceId links personal repertoire, workId a cached catalogue work. */
private class OfflineRow(val title:String,val composer:String,val meta:String,val pieceId:Long?,val workId:String,val composerRef:com.opus.piano.data.CatalogComposer?)
@Composable private fun OfflineRowCard(row:OfflineRow,onClick:()->Unit) {
    Surface(onClick=onClick,shape=MaterialTheme.shapes.medium,color=MaterialTheme.colorScheme.surface) {
        Row(Modifier.fillMaxWidth().padding(horizontal=12.dp,vertical=12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            ComposerPortrait(row.composerRef)
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(2.dp)) {
                Text(row.title,style=MaterialTheme.typography.titleMedium,maxLines=2,overflow=TextOverflow.Ellipsis)
                if(row.composer.isNotBlank()) Text(row.composer,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant,maxLines=1,overflow=TextOverflow.Ellipsis)
                if(row.meta.isNotBlank()) Text(row.meta,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant,maxLines=1,overflow=TextOverflow.Ellipsis)
            }
            AvailabilityPill(online=false)
            Icon(Icons.Outlined.ChevronRight,null,Modifier.size(18.dp),tint=MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
@Composable private fun CategoryTile(key:String,selected:Boolean,onClick:()->Unit) {
    val tint=MaterialTheme.colorScheme.primary
    Surface(onClick=onClick,shape=MaterialTheme.shapes.medium,color=if(selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface) {
        Column(Modifier.width(92.dp).padding(horizontal=10.dp,vertical=12.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Icon(categoryIcon(key),null,tint=tint,modifier=Modifier.size(24.dp))
            Text(categoryLabel(key),style=MaterialTheme.typography.labelLarge,color=if(selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,maxLines=1,overflow=TextOverflow.Ellipsis)
        }
    }
}
@Composable private fun RecentCard(entity:CatalogueEntity,workType:String?,open:()->Unit,remove:()->Unit) {
    Surface(onClick=open,shape=MaterialTheme.shapes.medium,color=MaterialTheme.colorScheme.surface) {
        Column(Modifier.width(150.dp)) {
            Box(Modifier.fillMaxWidth().height(88.dp)) {
                Surface(Modifier.fillMaxSize(),color=MaterialTheme.colorScheme.surfaceVariant) {Icon(categoryIcon(entity.kind),null,Modifier.fillMaxSize().padding(26.dp),tint=MaterialTheme.colorScheme.primary)}
                AvailabilityPill(online=false,modifier=Modifier.align(Alignment.BottomStart).padding(6.dp))
                IconButton(onClick=remove,modifier=Modifier.align(Alignment.TopEnd).size(32.dp)) {Icon(Icons.Outlined.Close,L.s(R.string.discover_remove),Modifier.size(16.dp),tint=MaterialTheme.colorScheme.onSurfaceVariant)}
            }
            Column(Modifier.padding(start=10.dp,end=10.dp,top=8.dp,bottom=10.dp),verticalArrangement=Arrangement.spacedBy(2.dp)) {
                Text(shownTitle(entity.kind,entity.title),style=MaterialTheme.typography.titleSmall,maxLines=2,overflow=TextOverflow.Ellipsis)
                Row(verticalAlignment=Alignment.CenterVertically) {
                    Text(kindLabel(workType ?: entity.kind),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.weight(1f),maxLines=1,overflow=TextOverflow.Ellipsis)
                    Icon(Icons.Outlined.ChevronRight,null,Modifier.size(16.dp),tint=MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
private fun categoryIcon(key:String)=when(key){"works","work","arrangement"->Icons.Outlined.MusicNote;"artists","artist"->Icons.Outlined.Person;"albums","album","release"->Icons.Outlined.Album;"lists","collection"->Icons.Outlined.List;else->Icons.Outlined.LibraryMusic}
@Composable internal fun CatalogueRow(entity:CatalogueEntity,workType:String?=null,info:CatalogueRowInfo?=null,onClick:()->Unit) {
    Surface(onClick=onClick,shape=MaterialTheme.shapes.medium,color=MaterialTheme.colorScheme.surface) {
        Row(Modifier.fillMaxWidth().padding(horizontal=12.dp,vertical=12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            Icon(categoryIcon(entity.kind),null,tint=MaterialTheme.colorScheme.primary,modifier=Modifier.size(20.dp))
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(2.dp)) {
                Text(shownTitle(entity.kind,entity.title),style=MaterialTheme.typography.titleMedium,maxLines=2,overflow=TextOverflow.Ellipsis)
                if(!info?.composers.isNullOrEmpty())Text(info!!.composers.joinToString(", "),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.primary,maxLines=2,overflow=TextOverflow.Ellipsis)
                val type=when(info?.artistType){"composer"->L.s(R.string.discover_composer);"person"->L.s(R.string.discover_person);"group"->L.s(R.string.discover_group);else->kindLabel(workType ?: entity.kind)}
                val provider=info?.providers.orEmpty().joinToString(" · "){providerLabel(it)}
                Text(type+if(provider.isBlank())"" else " · "+provider,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant,maxLines=1,overflow=TextOverflow.Ellipsis)
            }
            Icon(Icons.Outlined.ChevronRight,null,Modifier.size(18.dp),tint=MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
@Composable internal fun DiscoverDetailScreen(id:String,back:()->Unit,open:(String)->Unit,openPiece:(Long)->Unit) {
    val app=LocalContext.current.applicationContext as OpusApplication
    val scope=rememberCoroutineScope();val uri=LocalUriHandler.current
    val language=LocalConfiguration.current.locales[0].language.let {if(it=="cs")"cs" else "en"}
    var supplementSources by remember {mutableStateOf(emptyList<SearchSource>())}
    var detail by remember(id){mutableStateOf<DiscoverDetail?>(null)}
    var loaded by remember(id){mutableStateOf(false)}
    var candidate by remember(id){mutableStateOf<CatalogueCandidate?>(null)}
    var adding by remember {mutableStateOf(false)}
    var selection by rememberSaveable(id){mutableStateOf(listOf<String>())}
    var separate by rememberSaveable(id){mutableStateOf(false)}
    var notice by remember {mutableStateOf<String?>(null)}
    var existingPiece by remember {mutableStateOf<Long?>(null)}
    var portraitSnapshot by remember(id){mutableStateOf<com.opus.piano.data.catalogue.CataloguePortraits.Snapshot?>(null)}
    val portrait=portraitSnapshot?.bitmap
    val portraitCredit=portraitSnapshot?.credits
    var creditsExpanded by rememberSaveable(id){mutableStateOf(false)}
    var advancedExpanded by rememberSaveable(id){mutableStateOf(false)}
    var biographyExpanded by rememberSaveable(id){mutableStateOf(false)}
    var expandedEvidence by rememberSaveable(id){mutableStateOf(listOf<Int>())}
    var correction by rememberSaveable(id){mutableStateOf(false)}
    var correctionField by rememberSaveable(id){mutableStateOf("")}
    var correctionValue by rememberSaveable(id){mutableStateOf("")}
    var correctionReason by rememberSaveable(id){mutableStateOf("")}
    var correctionExport by remember {mutableStateOf<String?>(null)}
    val context=LocalContext.current
    val exportCorrection=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) {destination->
        val document=correctionExport
        if(destination!=null && document!=null)scope.launch {
            try {withContext(Dispatchers.IO){context.contentResolver.openOutputStream(destination)?.use {it.write(document.toByteArray())} ?: error("Output unavailable")};notice=L.s(R.string.discover_correction_saved)}
            catch(e:CancellationException){throw e}catch(_:Exception){notice=L.s(R.string.discover_error)}
        }
    }
    LaunchedEffect(id) {
        detail=(app.discover.detail(id) as? CatalogueAccess.Available)?.value;candidate=if(detail==null)app.discover.candidate(id) else null;loaded=true
        if(detail!=null){existingPiece=app.catalogueRepertoire.existingPiece(detail!!);val canonical=detail!!.entity.id;withContext(Dispatchers.IO){app.discoverHistory.opened(id)};portraitSnapshot=app.cataloguePortraits.snapshot(canonical)}
    }
    val presentation by produceState<CataloguePresentation.Prepared?>(null,detail,language) {
        value=detail?.let {withContext(Dispatchers.Default){CataloguePresentation.prepare(it,language)}}
    }
    LazyColumn(contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item {TextButton(back){Text(L.s(R.string.discover_back))}}
        if(!loaded)item {LinearProgressIndicator(Modifier.fillMaxWidth())}
        val data=detail
        val biography=presentation?.biography
        if(loaded && data==null && candidate!=null)item {
            Text(candidate!!.title,style=MaterialTheme.typography.headlineLarge)
            Text(L.s(R.string.discover_candidate))
            Button(onClick={scope.launch {val resolved=app.discover.resolveCandidate(candidate!!);if(resolved!=null)open(resolved) else notice=L.s(R.string.discover_error)}}){Text(L.s(R.string.discover_verify_candidate))}
            notice?.let {Text(it)}
        }
        if(loaded && data==null && candidate==null)item {Text(L.s(R.string.discover_missing))}
        if(data!=null) {
            item {
                Column(verticalArrangement=Arrangement.spacedBy(JournalSpace.tiny)) {
                    Text(shownTitle(data.entity.kind,presentation?.title ?: data.entity.title),style=MaterialTheme.typography.headlineMedium,modifier=Modifier.semantics {heading()})
                    Text(if(data.identities.any {it.provider=="openopus" && it.providerKind=="composer" && it.reviewState=="reviewed"})L.s(R.string.discover_composer) else kindLabel(data.workType ?: data.entity.kind),style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.onSurfaceVariant)
                    data.relationships.filter {it.first.type=="credit" && it.first.role=="composer" && it.first.fromId==data.entity.id}
                        .distinctBy {it.second.id}.forEach {(_,composer)->
                            JournalTextButton(onClick={open(composer.id)}) {Text(composer.title,style=MaterialTheme.typography.titleMedium,color=MaterialTheme.colorScheme.primary)}
                        }
                }
            }
            val essentials=presentation?.essentials.orEmpty()
            essentials.filter {it.field in setOf("description","subtitle")}.forEach {row->item {Text(row.value,style=MaterialTheme.typography.bodyMedium)}}
            val metadata=essentials.filterNot {it.field in setOf("description","subtitle")}
            if(metadata.isNotEmpty())item {EditorialMetadata(metadata.take(3).map {essentialValue(it) to fieldLabel(it.field)})}

            if(presentation?.conflicts?.isNotEmpty()==true)item {Text(L.s(R.string.discover_conflict_notice))}
            if(data.entity.kind in setOf("work","arrangement") && data.identities.any {it.reviewState=="reviewed"}) {
                val parts=data.relationships.filter {it.first.type=="part" && it.first.toId==data.entity.id}
                if(parts.isNotEmpty()) {
                    item {Text(L.s(R.string.discover_parts_choice))}
                    items(parts,key={"select:"+it.first.id}) {(_,part)->Row {Checkbox(part.id in selection,{checked->selection=if(checked)selection+part.id else selection-part.id},modifier=Modifier.semantics {contentDescription=part.title});Text(part.title)}}
                    if(selection.isNotEmpty())item {Row {Checkbox(separate,{separate=it},modifier=Modifier.semantics {contentDescription=L.s(R.string.discover_separate)});Text(L.s(R.string.discover_separate))}}
                }
                item {JournalPrimaryButton(modifier=Modifier.fillMaxWidth().heightIn(min=52.dp),enabled=!adding,onClick={
                    val saved=existingPiece
                    if(saved!=null && selection.isEmpty())openPiece(saved) else {adding=true;scope.launch {
                    try {
                        val selected=selection.mapNotNull {partId->(app.discover.detail(partId) as? CatalogueAccess.Available)?.value}
                        check(selected.size==selection.size)
                        val added=app.catalogueRepertoire.add(data,selected,separate)
                        if(added.any {it.selectionConflict}){existingPiece=added.first {it.selectionConflict}.pieceId;notice=L.s(R.string.discover_selection_conflict)}
                        else if(added.size==1)openPiece(added.single().pieceId) else notice=L.s(R.string.discover_added)
                    }catch(e:CancellationException){throw e}catch(_:Exception){notice=L.s(R.string.discover_error)}finally{adding=false}
                }}}){Text(L.s(if(selection.isNotEmpty())R.string.discover_add_parts else if(existingPiece!=null)R.string.discover_open_existing else R.string.discover_add_work))}}
            }
            presentation?.listeningUrls.orEmpty().forEach {url->item {
                JournalSecondaryButton(onClick={uri.openUri(url)}){Text(L.s(R.string.discover_listen))}
            }}
            notice?.let {item {Text(it)}}
            if(notice!=null)existingPiece?.let {piece->item {TextButton({openPiece(piece)}){Text(L.s(R.string.discover_open_existing))}}}
            val related=data.relationships.distinctBy {Triple(it.first.type,it.first.role,it.second.id)}.filterNot {
                it.first.type=="credit" && it.first.role=="composer" && it.first.fromId==data.entity.id
            }
            related.groupBy {it.second.kind=="artist"}.forEach {(people,edges)->
                item {EditorialSectionHeader(L.s(if(people)R.string.discover_related_people else R.string.discover_related_music))}
                items(edges,key={"edge:"+it.first.id}) {(_,entity)->CatalogueRow(entity,data.workTypes[entity.id]){open(entity.id)}}
            }
            biography?.let {row->item {
                EditorialSectionHeader(L.s(R.string.discover_field_biography))
                Text(presentation?.biographyText.orEmpty(),style=MaterialTheme.typography.bodyLarge,maxLines=if(biographyExpanded)Int.MAX_VALUE else 5,overflow=TextOverflow.Ellipsis)
                TextButton({biographyExpanded=!biographyExpanded}){Text(L.s(if(biographyExpanded)R.string.discover_show_less else R.string.discover_read_more))}
                Text(L.s(R.string.discover_document_language,CataloguePresentation.languageLabel(presentation?.biographyLanguage.orEmpty(),LocalConfiguration.current.locales[0],L.s(R.string.discover_language_unspecified))),style=MaterialTheme.typography.bodySmall)
                if(presentation?.biographyLanguage.orEmpty()!=language)Text(L.s(R.string.discover_biography_fallback),style=MaterialTheme.typography.bodySmall)
                Text(row.rights.attribution,style=MaterialTheme.typography.bodySmall)
                TextButton({uri.openUri(row.rights.licenseUrl)}){Text(L.s(R.string.discover_license))}
                val revision=presentation?.biographyRevisionUrl
                TextButton({uri.openUri(revision ?: row.source.sourceUrl)}){Text(L.s(R.string.discover_source_link))}
            }}
            portrait?.let {bitmap->item {Image(bitmap.asImageBitmap(),contentDescription=L.s(R.string.discover_portrait_description),modifier=Modifier.fillMaxWidth().heightIn(max=240.dp))}}
            if(portrait!=null && portraitCredit!=null)item {
                val credit=portraitCredit!!;val metadata=credit.getJSONObject("extmetadata")
                Text(credit.optString("displayAttribution"),style=MaterialTheme.typography.bodySmall)
                Text(L.s(R.string.discover_portrait_original),style=MaterialTheme.typography.bodySmall)
                TextButton({uri.openUri(credit.getString("descriptionurl"))}){Text(L.s(R.string.discover_source_link))}
                TextButton({uri.openUri(metadata.getJSONObject("LicenseUrl").getString("value"))}){Text(L.s(R.string.discover_license))}
            }
            if(data.assertions.any {it.assertion.field in setOf("wikipedia_title","commons_file")})item {
                TextButton(onClick={scope.launch {supplementSources=app.discover.supplement(data,language)
                    val canonical=data.entity.id
                    val metadata=app.discover.portraitMetadata(canonical)
                    if(metadata!=null)try {if(app.cataloguePortraits.download(canonical,metadata)){portraitSnapshot=app.cataloguePortraits.snapshot(canonical)}}catch(e:CancellationException){throw e}catch(_:Exception){notice=L.s(R.string.discover_portrait_unavailable)}
                    detail=(app.discover.detail(id) as? CatalogueAccess.Available)?.value}}){Text(L.s(R.string.discover_load_encyclopedia))}
            }
            items(supplementSources,key={"supplement:"+it.provider+":"+it.requestKind.orEmpty()}) {Text(it.provider+" · "+it.requestKind.orEmpty().substringAfter(':')+" · "+sourceLabel(it.state))}
            item {EditorialDisclosure(L.s(R.string.discover_sources),creditsExpanded,{creditsExpanded=!creditsExpanded})}
            if(creditsExpanded) {
                val groups=presentation?.groups.orEmpty()
                items(groups.indices.toList(),key={"assertion-group:$it"}) {index->
                    val group=groups[index]
                    val expanded=index in expandedEvidence
                    Surface(shape=MaterialTheme.shapes.medium,color=MaterialTheme.colorScheme.surface) {
                        Column(Modifier.fillMaxWidth().padding(12.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                            Text(fieldLabel(group.field),style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                            val value=if(group.evidence.none {it.rights.reviewState=="approved"})L.s(R.string.discover_blocked) else when(group.field) {
                                "document_fallback_reason"->L.s(if(group.value.isBlank())R.string.discover_requested_language_available else R.string.discover_biography_fallback)
                                "adaptation"->L.s(R.string.discover_excerpt_notice)
                                "document_requested_language","language"->CataloguePresentation.languageLabel(group.value,LocalConfiguration.current.locales[0],L.s(R.string.discover_language_unspecified))
                                else->group.value
                            }
                            Text(value,style=MaterialTheme.typography.bodyMedium,maxLines=if(expanded)Int.MAX_VALUE else 3,overflow=TextOverflow.Ellipsis)
                            TextButton({expandedEvidence=if(expanded)expandedEvidence-index else expandedEvidence+index}) {
                                Text(L.s(R.string.discover_evidence_count,group.evidence.size))
                            }
                            if(expanded)group.evidence.forEach {row->
                                HorizontalDivider()
                                Text(providerLabel(row.source.provider)+" · "+CataloguePresentation.languageLabel(group.language,LocalConfiguration.current.locales[0],L.s(R.string.discover_language_unspecified)),style=MaterialTheme.typography.bodySmall)
                                Text(CataloguePresentation.retrievedLabel(row.source.retrievedAt,LocalConfiguration.current.locales[0]),style=MaterialTheme.typography.bodySmall)
                                Text(licenseLabel(row.rights.license)+" · "+row.rights.attribution,style=MaterialTheme.typography.bodySmall)
                                if(!row.assertion.selected)Text(L.s(R.string.discover_alternate),style=MaterialTheme.typography.bodySmall)
                                Text(fieldLabel("article_revision")+": "+row.source.revision,style=MaterialTheme.typography.bodySmall)
                                Text("SHA-256: "+row.source.payloadSha256,style=MaterialTheme.typography.bodySmall)
                                TextButton({uri.openUri(row.source.sourceUrl)}){Text(L.s(R.string.discover_source_link))}
                                TextButton({uri.openUri(row.rights.licenseUrl)}){Text(L.s(R.string.discover_license))}
                            }
                        }
                    }
                }
            }
            item {EditorialDisclosure(L.s(R.string.discover_advanced),advancedExpanded,{advancedExpanded=!advancedExpanded})}
            if(advancedExpanded) {
            if(data.identities.any {it.provider=="musicbrainz"})item {
                TextButton(onClick={scope.launch {supplementSources=listOf(app.discover.refreshMetadata(data));detail=(app.discover.detail(id) as? CatalogueAccess.Available)?.value}}){Text(L.s(R.string.discover_refresh_metadata))}
            }
            item {Text(L.s(if(data.identities.any {it.reviewState=="reviewed"})R.string.discover_identity_notice else R.string.discover_candidate),style=MaterialTheme.typography.bodySmall)}
            item {TextButton({correction=!correction}){Text(L.s(R.string.discover_correction))}}
            if(correction)item {
                OutlinedTextField(correctionField,{correctionField=it.take(80)},label={Text(L.s(R.string.discover_correction_field))},modifier=Modifier.fillMaxWidth())
                OutlinedTextField(correctionValue,{correctionValue=it.take(4096)},label={Text(L.s(R.string.discover_correction_value))},modifier=Modifier.fillMaxWidth())
                OutlinedTextField(correctionReason,{correctionReason=it.take(4096)},label={Text(L.s(R.string.discover_correction_reason))},modifier=Modifier.fillMaxWidth())
                Text(L.s(R.string.discover_correction_private),style=MaterialTheme.typography.bodySmall)
                TextButton(enabled=correctionField.isNotBlank() && correctionValue.isNotBlank() && correctionReason.isNotBlank(),onClick={scope.launch {correctionExport=withContext(Dispatchers.Default){CataloguePresentation.correction(data,correctionField,correctionValue,correctionReason)};exportCorrection.launch("Goldberg-catalogue-correction.json")}}){Text(L.s(R.string.discover_correction_export))}
            }
            }
        }
    }
}
private fun categoryLabel(key:String)=L.s(when(key){"works"->R.string.discover_works;"artists"->R.string.discover_artists;"albums"->R.string.discover_albums;"lists"->R.string.discover_lists;else->R.string.discover_all})
/** Legacy numeric/opaque fallbacks render as a localized unnamed label, never as an invented name. */
private fun shownTitle(kind:String,title:String)=if(CataloguePresentation.unnamed(kind,title))L.s(if(kind=="artist")R.string.discover_unnamed_artist else R.string.discover_unnamed_entry) else title
private fun fieldLabel(key:String)=L.s(when(key) {
    "title"->R.string.discover_field_title;"alias"->R.string.discover_field_alias;"preferred_name"->R.string.discover_field_name
    "biography"->R.string.discover_field_biography;"description"->R.string.discover_field_description
    "wikipedia_title"->R.string.discover_field_article;"commons_file","portrait_source"->R.string.discover_field_portrait
    "article_revision","wikidata_revision"->R.string.discover_field_revision;"article_revision_url"->R.string.discover_field_revision_link;"article_history"->R.string.discover_field_history
    "document_requested_language"->R.string.discover_field_requested_language;"document_fallback_reason"->R.string.discover_field_fallback;"adaptation"->R.string.discover_field_adaptation
    "identity_conflict"->R.string.discover_field_identity_conflict;"verified_musicbrainz_identity"->R.string.discover_field_musicbrainz
    "catalogue","catalogue_number"->R.string.discover_field_catalogue;"epoch"->R.string.discover_field_period;"genre"->R.string.discover_field_genre
    "birth"->R.string.discover_field_birth;"death"->R.string.discover_field_death;"durationMillis"->R.string.discover_field_duration
    "country"->R.string.discover_field_country;"date","first-release-date"->R.string.discover_field_date;"type","primary-type"->R.string.discover_field_type
    "sort-name"->R.string.discover_field_sort_name;"listening_url"->R.string.discover_listen
    else->R.string.discover_field_evidence
})
private fun kindLabel(key:String)=L.s(when(key){"work","composition"->R.string.discover_work;"movement"->R.string.discover_movement;"part"->R.string.discover_part;"medley"->R.string.discover_medley;"mashup"->R.string.discover_mashup;"arrangement"->R.string.discover_arrangement;"artist"->R.string.discover_artist;"album"->R.string.discover_album;"release"->R.string.discover_release;"recording"->R.string.discover_recording;"collection"->R.string.discover_collection;else->R.string.discover_unknown_entity})
private fun relationLabel(key:String)=L.s(when(key){"part"->R.string.discover_part_relation;"credit"->R.string.discover_credit_relation;"performance"->R.string.discover_performance_relation;else->R.string.discover_related})
private fun sourceLabel(state:SourceState)=L.s(when(state){SourceState.FRESH->R.string.discover_fresh;SourceState.CACHED->R.string.discover_cached;SourceState.STALE->R.string.discover_stale;SourceState.RATE_LIMITED->R.string.discover_rate_limited;SourceState.BLOCKED_RIGHTS->R.string.discover_blocked;SourceState.MISSING->R.string.discover_missing;SourceState.INVALID->R.string.discover_invalid;else->R.string.discover_unavailable})

private fun sourceKindLabel(kind:String)=when(kind){"search"->L.s(R.string.discover_all);"release-group"->L.s(R.string.discover_albums);"series"->L.s(R.string.discover_lists);else->kindLabel(kind)}

private fun providerLabel(provider:String)=when(provider){"openopus"->"Open Opus";"musicbrainz"->"MusicBrainz";"wikidata"->"Wikidata";"wikipedia"->"Wikipedia";"commons"->"Wikimedia Commons";else->provider}
private fun licenseLabel(license:String)=when(license){"CC0-1.0"->L.s(R.string.discover_license_cc0);"CC-BY-SA-4.0"->L.s(R.string.discover_license_by_sa);"Public-domain-verified"->L.s(R.string.discover_license_public_domain);else->license}

/** Translate only controlled classification values, never names or user-entered text. */
private fun essentialValue(row:AssertionGroup)=if(row.field in setOf("genre","epoch"))L.fixed(row.value) else row.value
