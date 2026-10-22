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
import org.json.JSONObject

/** Nav-entry memory only. Queries survive detail/Back and recreation, never process storage. */
internal class DiscoverSearchSession:androidx.lifecycle.ViewModel() {
    var query by mutableStateOf("")
}
@Composable internal fun DiscoverScreen(open:(String)->Unit,openPiece:(Long)->Unit,openCatalogWork:(String)->Unit,library:LibraryState) {
    val app=LocalContext.current.applicationContext as OpusApplication
    // Raw catalogue queries are transient; exclude them from persisted saved state.
    val session:DiscoverSearchSession=androidx.lifecycle.viewmodel.compose.viewModel()
    val query=session.query
    var active by rememberSaveable {mutableStateOf(false)}
    // Stage 7A (owner): online catalogue results are a persisted Settings toggle, default OFF.
    // The online leg of a search is only ever triggered by an explicit "Search online" tap;
    // opening or restoring this screen never starts a network request. Plain remember on
    // purpose: restoring after process death must not silently re-run an online search.
    val catalogOnline by app.catalogOnline.enabled.collectAsState()
    var onlineRequested by remember {mutableStateOf(false)}
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
    val searchKey=listOf(query,category,artistId,period,active.toString(),onlineRequested.toString())
    val currentSearchKey by rememberUpdatedState(searchKey)
    var rows by remember {mutableStateOf(emptyList<CatalogueEntity>())}
    var recent by remember {mutableStateOf(emptyList<CatalogueEntity>())}
    var workTypes by remember {mutableStateOf(emptyMap<String,String>())}
    var recentTypes by remember {mutableStateOf(emptyMap<String,String>())}
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
    LaunchedEffect(query,category,artistId,period,active,onlineRequested,revision,language) {
        pageJob?.cancel();searchEpoch++;paging=false;limitReached=false;sources=emptyList();candidates=emptyList()
        error=false;loading=true
        cacheBytes=app.providerCache.bytes()
        try {
            if(!active) {val page=app.discover.landing();rows=page.items;workTypes=page.workTypes;cursor=null;sources=emptyList()}
            else {
                val localOrder=linkedMapOf<String,CatalogueEntity>()
                app.discover.search(query,filter,language,false).collect {result->rows=result.page.items;workTypes=result.page.workTypes;cursor=result.continuation;candidates=result.candidates;limitReached=result.limitReached;rows.forEach {localOrder[it.id]=it}}
                if(onlineRequested && catalogOnline) {
                    delay(400)
                    app.discover.search(query,filter,language,true).collect {result->
                        result.page.items.forEach {localOrder[it.id]=it}
                        workTypes=workTypes+result.page.workTypes
                        rows=localOrder.values.take(500).toList();cursor=result.continuation;sources=result.sources;candidates=result.candidates;limitReached=result.limitReached
                    }
                }
            }
            availableFilters=app.discover.filters()
            val opened=withContext(Dispatchers.IO){app.discoverHistory.entries().mapNotNull {id->(app.discover.detail(id) as? CatalogueAccess.Available)?.value}}
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
        item {Column(verticalArrangement=Arrangement.spacedBy(JournalSpace.tiny)) {
            Text(L.s(R.string.discover),style=MaterialTheme.typography.headlineLarge,modifier=Modifier.semantics {heading()})
            Text(L.s(R.string.discover_explore),style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
        }}
        item {EditorialSearchField(query,{session.query=it;active=true;onlineRequested=false})}
        item {
            // Owner-approved truthful privacy wording (stage-7 plan §9 C1); concise
            // third-party disclosure only — never claims "our servers". This is the single
            // privacy notice on the landing (Stage 7A refinement).
            Surface(shape=MaterialTheme.shapes.medium,color=MaterialTheme.colorScheme.surfaceVariant.copy(alpha=.55f)) {
                Row(Modifier.fillMaxWidth().padding(12.dp),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Outlined.Lock,null,Modifier.padding(top=1.dp).size(18.dp),tint=MaterialTheme.colorScheme.primary)
                    Text(L.s(R.string.stage7a_privacy_notice),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        // Stage 7A refinement: contextual online-search controls, shown only while the user
        // is actually searching — the landing stays the single concise privacy notice, and
        // technical network status is kept away from prominent landing content.
        if(active) {
            item { if(catalogOnline) {
                // Explicit, user-initiated online search (owner privacy requirement): the button
                // is the only path that ever reaches external providers, and it re-runs only for
                // the current query/filter state.
                JournalSecondaryButton(onClick={onlineRequested=true;active=true},enabled=!loading && query.isNotBlank()) {Text(L.s(R.string.stage7a_search_online))}
            } else {
                // Contextual hint: only useful when the user is searching without the
                // catalogue preference enabled.
                Text(L.s(R.string.stage7a_online_off_hint),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            } }
            if(catalogOnline) item {Text(L.s(if(app.media.allowed())R.string.discover_network_ready else R.string.discover_offline),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
        }
        // Browsable categories rendered from the real supported search categories.
        item {EditorialSectionHeader(L.s(R.string.stage7a_browse_categories))}
        item {LazyRow(horizontalArrangement=Arrangement.spacedBy(JournalSpace.tiny),contentPadding=PaddingValues(horizontal=JournalSpace.small)) {
            items(listOf("all","works","artists","albums","lists")) {key->CategoryTile(key,category==key){category=key;active=true;onlineRequested=false}}
        }}
        if(availableFilters.artists.isNotEmpty() || availableFilters.periods.isNotEmpty())item {
            TextButton({filtersExpanded=!filtersExpanded}){Text(L.s(R.string.discover_filters))}
            if(filtersExpanded) {
                Box {
                    TextButton({artistMenu=true}){Text(availableFilters.artists.firstOrNull {it.id==artistId}?.let {shownTitle("artist",it.title)} ?: L.s(R.string.discover_any_artist))}
                    DropdownMenu(artistMenu,{artistMenu=false}) {
                        DropdownMenuItem(text={Text(L.s(R.string.discover_any_artist))},onClick={artistId=null;artistMenu=false;active=true;onlineRequested=false})
                        availableFilters.artists.forEach {artist->DropdownMenuItem(text={Text(shownTitle("artist",artist.title))},onClick={artistId=artist.id;artistMenu=false;active=true;onlineRequested=false})}
                    }
                }
                Box {
                    TextButton({periodMenu=true}){Text(period ?: L.s(R.string.discover_any_period))}
                    DropdownMenu(periodMenu,{periodMenu=false}) {
                        DropdownMenuItem(text={Text(L.s(R.string.discover_any_period))},onClick={period=null;periodMenu=false;active=true;onlineRequested=false})
                        availableFilters.periods.forEach {value->DropdownMenuItem(text={Text(value)},onClick={period=value;periodMenu=false;active=true;onlineRequested=false})}
                    }
                }
            }
        }
        if(loading)item {LinearProgressIndicator(Modifier.fillMaxWidth())}
        if(error)item {Text(L.s(R.string.discover_error));TextButton({revision++}){Text(L.s(R.string.discover_retry))}}
        items(sources,key={"source:"+it.provider+":"+it.requestKind}) {source->
            Text(source.provider+(source.requestKind?.let {" · "+sourceKindLabel(it)} ?: "")+" · "+sourceLabel(source.state)+(source.refreshState?.let {" · "+sourceLabel(it)} ?: ""),style=MaterialTheme.typography.bodySmall)
            if(source.state !in setOf(SourceState.FRESH,SourceState.CACHED))TextButton({revision++}){Text(L.s(R.string.discover_retry))}
        }
        // Stage 7A (owner): never claim "no local content" on the landing screen while the
        // "Available offline" rows below show real local results — only a search with no
        // matches shows this.
        if(active && rows.isEmpty() && !loading)item {Text(L.s(R.string.discover_empty))}
        items(rows,key={it.id}) {entity->CatalogueRow(entity,workTypes[entity.id]){open(entity.id)}}
        items(candidates.filterNot {candidate->rows.any {it.id==candidate.id}},key={"candidate:"+it.id}) {candidate->
            Text(L.s(R.string.discover_candidate),style=MaterialTheme.typography.labelSmall)
            CatalogueRow(CatalogueEntity(candidate.id,candidate.kindHint,candidate.title,status="quarantined")){open(candidate.id)}
        }
        if(limitReached)item {Text(L.s(R.string.discover_refine_search))}
        if(cursor!=null)item {TextButton(enabled=!paging && !loading,onClick={
            val next=cursor ?: return@TextButton
            val requestedKey=searchKey;val requestedEpoch=searchEpoch;val requestedQuery=query;val requestedFilter=filter;val requestedOnline=onlineRequested
            paging=true
            pageJob=scope.launch {
                try {
                    val result=app.discover.continueSearch(requestedQuery,requestedFilter,language,requestedOnline,next)
                    val page=result.page
                    if(searchEpoch==requestedEpoch && currentSearchKey==requestedKey){rows=(rows+page.items).distinctBy {it.id};workTypes=workTypes+page.workTypes;cursor=result.continuation;candidates=(candidates+result.candidates).distinctBy {it.id};sources=result.sources;limitReached=result.limitReached}
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
        item {
            HorizontalDivider()
            Text(L.s(R.string.discover_cache_consequences),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            Text(android.text.format.Formatter.formatShortFileSize(LocalContext.current,cacheBytes),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick={scope.launch {app.discover.clearResponseCache();cacheBytes=app.providerCache.bytes()}}){Text(L.s(R.string.discover_clear_cache))}
        }
        item {HorizontalDivider();Row(verticalAlignment=Alignment.CenterVertically) {Switch(historyEnabled,{value->historyEnabled=value;scope.launch {withContext(Dispatchers.IO){app.discoverHistory.enabled(value)};revision++}});Text(L.s(R.string.discover_history))}}
        item {TextButton({scope.launch {withContext(Dispatchers.IO){app.discoverHistory.clear()};recent=emptyList()}}){Text(L.s(R.string.discover_clear_history))}}
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
private fun categoryIcon(key:String)=when(key){"works"->Icons.Outlined.MusicNote;"artists"->Icons.Outlined.Person;"albums"->Icons.Outlined.Album;"lists"->Icons.Outlined.List;else->Icons.Outlined.LibraryMusic}
@Composable private fun CatalogueRow(entity:CatalogueEntity,workType:String?=null,onClick:()->Unit) {
    Surface(onClick=onClick,shape=MaterialTheme.shapes.medium,color=MaterialTheme.colorScheme.surface) {
        Row(Modifier.fillMaxWidth().padding(horizontal=12.dp,vertical=12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            Icon(categoryIcon(entity.kind),null,tint=MaterialTheme.colorScheme.primary,modifier=Modifier.size(20.dp))
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(2.dp)) {
                Text(shownTitle(entity.kind,entity.title),style=MaterialTheme.typography.titleMedium,maxLines=2,overflow=TextOverflow.Ellipsis)
                Text(kindLabel(workType ?: entity.kind),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant,maxLines=1,overflow=TextOverflow.Ellipsis)
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
        if(detail!=null){val canonical=detail!!.entity.id;withContext(Dispatchers.IO){app.discoverHistory.opened(id)};portraitSnapshot=app.cataloguePortraits.snapshot(canonical)}
    }
    LazyColumn(contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item {TextButton(back){Text(L.s(R.string.discover_back))}}
        if(!loaded)item {LinearProgressIndicator(Modifier.fillMaxWidth())}
        val data=detail
        val biography=data?.let {CataloguePresentation.biography(it,language)}
        if(loaded && data==null && candidate!=null)item {
            Text(candidate!!.title,style=MaterialTheme.typography.headlineLarge)
            Text(L.s(R.string.discover_candidate))
            Button(onClick={scope.launch {val resolved=app.discover.resolveCandidate(candidate!!);if(resolved!=null)open(resolved) else notice=L.s(R.string.discover_error)}}){Text(L.s(R.string.discover_verify_candidate))}
            notice?.let {Text(it)}
        }
        if(loaded && data==null && candidate==null)item {Text(L.s(R.string.discover_missing))}
        if(data!=null) {
            item {Text(shownTitle(data.entity.kind,CataloguePresentation.title(data,language)),style=MaterialTheme.typography.headlineLarge,modifier=Modifier.semantics {heading()});Text(kindLabel(data.workType ?: data.entity.kind))}
            biography?.let {row->item {
                val body=JSONObject(row.assertion.valueJson)
                Text(body.getString("value"),style=MaterialTheme.typography.bodyLarge)
                Text(L.s(R.string.discover_document_language,body.getString("language")),style=MaterialTheme.typography.bodySmall)
                if(body.getString("language")!=language)Text(L.s(R.string.discover_biography_fallback),style=MaterialTheme.typography.bodySmall)
                Text(row.rights.attribution,style=MaterialTheme.typography.bodySmall)
                TextButton({uri.openUri(row.rights.licenseUrl)}){Text(L.s(R.string.discover_license))}
                val revision=data.assertions.firstOrNull {it.assertion.sourceId==row.source.id && it.assertion.field=="article_revision_url"}
                    ?.let {JSONObject(it.assertion.valueJson).optString("value")}
                    ?.takeIf {it.matches(Regex("https://(en|cs)\\.wikipedia\\.org/w/index\\.php\\?oldid=[0-9]+"))}
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
            if(CataloguePresentation.conflicts(data).isNotEmpty())item {Text(L.s(R.string.discover_conflict_notice))}
            if(data.identities.any {it.provider=="musicbrainz"})item {
                TextButton(onClick={scope.launch {supplementSources=listOf(app.discover.refreshMetadata(data));detail=(app.discover.detail(id) as? CatalogueAccess.Available)?.value}}){Text(L.s(R.string.discover_refresh_metadata))}
            }
            item {Text(L.s(if(data.identities.any {it.reviewState=="reviewed"})R.string.discover_identity_notice else R.string.discover_candidate),style=MaterialTheme.typography.bodySmall)}
            if(data.entity.kind in setOf("work","arrangement") && data.identities.any {it.reviewState=="reviewed"}) {
                val parts=data.relationships.filter {it.first.type=="part" && it.first.toId==data.entity.id}
                if(parts.isNotEmpty()) {
                    item {Text(L.s(R.string.discover_parts_choice))}
                    items(parts,key={"select:"+it.first.id}) {(_,part)->Row {Checkbox(part.id in selection,{checked->selection=if(checked)selection+part.id else selection-part.id},modifier=Modifier.semantics {contentDescription=part.title});Text(part.title)}}
                    if(selection.isNotEmpty())item {Row {Checkbox(separate,{separate=it},modifier=Modifier.semantics {contentDescription=L.s(R.string.discover_separate)});Text(L.s(R.string.discover_separate))}}
                }
                item {Button(enabled=!adding,onClick={adding=true;scope.launch {
                    try {
                        val selected=selection.mapNotNull {partId->(app.discover.detail(partId) as? CatalogueAccess.Available)?.value}
                        check(selected.size==selection.size)
                        val added=app.catalogueRepertoire.add(data,selected,separate)
                        if(added.any {it.selectionConflict}){existingPiece=added.first {it.selectionConflict}.pieceId;notice=L.s(R.string.discover_selection_conflict)}
                        else if(added.size==1)openPiece(added.single().pieceId) else notice=L.s(R.string.discover_added)
                    }catch(e:CancellationException){throw e}catch(_:Exception){notice=L.s(R.string.discover_error)}finally{adding=false}
                }}){Text(L.s(if(selection.isEmpty())R.string.discover_add_work else R.string.discover_add_parts))}}
            }
            notice?.let {item {Text(it)}}
            existingPiece?.let {piece->item {TextButton({openPiece(piece)}){Text(L.s(R.string.discover_open_existing))}}}
            items(data.relationships,key={"edge:"+it.first.id}) {(edge,entity)->Text(relationLabel(edge.type),style=MaterialTheme.typography.labelSmall);CatalogueRow(entity,data.workTypes[entity.id]){open(entity.id)}}
            if(data.assertions.any {it.assertion.field in setOf("wikipedia_title","commons_file")})item {
                TextButton(onClick={scope.launch {supplementSources=app.discover.supplement(data,language)
                    val canonical=data.entity.id
                    val metadata=app.discover.portraitMetadata(canonical)
                    if(metadata!=null)try {if(app.cataloguePortraits.download(canonical,metadata)){portraitSnapshot=app.cataloguePortraits.snapshot(canonical)}}catch(e:CancellationException){throw e}catch(_:Exception){notice=L.s(R.string.discover_portrait_unavailable)}
                    detail=(app.discover.detail(id) as? CatalogueAccess.Available)?.value}}){Text(L.s(R.string.discover_load_encyclopedia))}
            }
            items(supplementSources,key={"supplement:"+it.provider+":"+it.requestKind.orEmpty()}) {Text(it.provider+" · "+it.requestKind.orEmpty().substringAfter(':')+" · "+sourceLabel(it.state))}
            item {Text(L.s(R.string.discover_sources),style=MaterialTheme.typography.titleLarge)}
            item {TextButton({correction=!correction}){Text(L.s(R.string.discover_correction))}}
            if(correction)item {
                OutlinedTextField(correctionField,{correctionField=it.take(80)},label={Text(L.s(R.string.discover_correction_field))},modifier=Modifier.fillMaxWidth())
                OutlinedTextField(correctionValue,{correctionValue=it.take(4096)},label={Text(L.s(R.string.discover_correction_value))},modifier=Modifier.fillMaxWidth())
                OutlinedTextField(correctionReason,{correctionReason=it.take(4096)},label={Text(L.s(R.string.discover_correction_reason))},modifier=Modifier.fillMaxWidth())
                Text(L.s(R.string.discover_correction_private),style=MaterialTheme.typography.bodySmall)
                TextButton(enabled=correctionField.isNotBlank() && correctionValue.isNotBlank() && correctionReason.isNotBlank(),onClick={scope.launch {correctionExport=withContext(Dispatchers.Default){CataloguePresentation.correction(data,correctionField,correctionValue,correctionReason)};exportCorrection.launch("Goldberg-catalogue-correction.json")}}){Text(L.s(R.string.discover_correction_export))}
            }
            items(data.assertions.filterNot {it.assertion.field.startsWith("claim:") || it.assertion.id==biography?.assertion?.id},key={it.assertion.id}) {assertion->
                val json=runCatching {JSONObject(assertion.assertion.valueJson)}.getOrNull()
                val value=when(assertion.assertion.field) {
                    "document_fallback_reason"->if(json?.optString("value").isNullOrBlank())L.s(R.string.discover_requested_language_available) else L.s(R.string.discover_biography_fallback)
                    "adaptation"->L.s(R.string.discover_excerpt_notice)
                    else->json?.optString("value") ?: assertion.assertion.valueJson
                }
                Text(fieldLabel(assertion.assertion.field)+": "+value,style=MaterialTheme.typography.bodyMedium)
                Text(assertion.source.provider+" · "+json?.optString("language").orEmpty()+" · "+assertion.rights.license+" · "+assertion.source.retrievedAt,style=MaterialTheme.typography.bodySmall)
                if(assertion.assertion.field=="listening_url" && com.opus.piano.data.catalogue.providers.ListeningDestination.accepts(json?.optString("value").orEmpty(),data.entity.kind))
                    TextButton({uri.openUri(json!!.getString("value"))}){Text(L.s(R.string.discover_listen))}
                if(!assertion.assertion.selected)Text(L.s(R.string.discover_alternate),style=MaterialTheme.typography.bodySmall)
                TextButton({uri.openUri(assertion.source.sourceUrl)}){Text(L.s(R.string.discover_source_link))}
            }
            item {Text(L.s(R.string.discover_no_media),style=MaterialTheme.typography.bodySmall)}
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
