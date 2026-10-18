package com.opus.piano.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.opus.piano.OpusApplication
import com.opus.piano.R
import com.opus.piano.i18n.L
import com.opus.piano.data.catalogue.*
import com.opus.piano.data.catalogue.providers.SourceState
import kotlinx.coroutines.*
import org.json.JSONObject

@Composable internal fun DiscoverScreen(open:(String)->Unit) {
    val app=LocalContext.current.applicationContext as OpusApplication
    // Raw catalogue queries are transient; exclude them from persisted saved state.
    var query by remember {mutableStateOf("")}
    var active by rememberSaveable {mutableStateOf(false)}
    var online by remember {mutableStateOf(false)}
    var category by rememberSaveable {mutableStateOf("all")}
    var artistId by rememberSaveable {mutableStateOf<String?>(null)}
    var period by rememberSaveable {mutableStateOf<String?>(null)}
    var availableFilters by remember {mutableStateOf(DiscoverFilters())}
    var filtersExpanded by rememberSaveable {mutableStateOf(false)}
    var artistMenu by remember {mutableStateOf(false)}
    var periodMenu by remember {mutableStateOf(false)}
    var paging by remember {mutableStateOf(false)}
    val filter=SearchFilter(category,artistId,period)
    val searchKey=listOf(query,category,artistId,period,active.toString(),online.toString())
    val currentSearchKey by rememberUpdatedState(searchKey)
    var rows by remember {mutableStateOf(emptyList<CatalogueEntity>())}
    var recent by remember {mutableStateOf(emptyList<CatalogueEntity>())}
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
    LaunchedEffect(query,category,artistId,period,active,online,revision,language) {
        paging=false;sources=emptyList();candidates=emptyList()
        error=false;loading=true
        cacheBytes=app.providerCache.bytes()
        try {
            if(!active) {rows=app.discover.landing().items;cursor=null;sources=emptyList()}
            else {
                val localOrder=linkedMapOf<String,CatalogueEntity>()
                app.discover.search(query,filter,language,false).collect {result->rows=result.page.items;cursor=result.page.nextCursor;candidates=result.candidates;rows.forEach {localOrder[it.id]=it}}
                if(online) {
                    delay(400)
                    app.discover.search(query,filter,language,true).collect {result->
                        result.page.items.forEach {localOrder[it.id]=it}
                        rows=localOrder.values.take(100).toList();cursor=result.page.nextCursor;sources=result.sources;candidates=result.candidates
                    }
                }
            }
            availableFilters=app.discover.filters()
            recent=withContext(Dispatchers.IO){app.discoverHistory.entries().mapNotNull {id->(app.discover.detail(id) as? CatalogueAccess.Available)?.value?.entity}}
        } catch(e:CancellationException){throw e} catch(_:Exception){error=true} finally {loading=false}
    }
    LazyColumn(contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item {Heading(L.s(R.string.discover),L.s(R.string.discover_explore),L.s(R.string.discover_privacy))}
        item {OutlinedTextField(query,{query=it;active=true},label={Text(L.s(R.string.discover_search))},singleLine=true,modifier=Modifier.fillMaxWidth())}
        item {Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            Checkbox(online,{online=it;active=true});Text(L.s(R.string.discover_online),modifier=Modifier.weight(1f))
        }}
        item {Text(L.s(if(app.media.allowed())R.string.discover_network_ready else R.string.discover_offline),style=MaterialTheme.typography.bodySmall)}
        // Wrap categories instead of compressing five text controls at large font sizes.
        item {
            FlowRow(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                listOf("all","works","artists","albums","lists").forEach { key ->
                    FilterChip(category==key,{category=key;active=true},label={Text(categoryLabel(key))})
                }
            }
        }
        if(availableFilters.artists.isNotEmpty() || availableFilters.periods.isNotEmpty())item {
            TextButton({filtersExpanded=!filtersExpanded}){Text(L.s(R.string.discover_filters))}
            if(filtersExpanded) {
                Box {
                    TextButton({artistMenu=true}){Text(availableFilters.artists.firstOrNull {it.id==artistId}?.title ?: L.s(R.string.discover_any_artist))}
                    DropdownMenu(artistMenu,{artistMenu=false}) {
                        DropdownMenuItem(text={Text(L.s(R.string.discover_any_artist))},onClick={artistId=null;artistMenu=false;active=true})
                        availableFilters.artists.forEach {artist->DropdownMenuItem(text={Text(artist.title)},onClick={artistId=artist.id;artistMenu=false;active=true})}
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
        if(loading)item {LinearProgressIndicator(Modifier.fillMaxWidth())}
        if(error)item {Text(L.s(R.string.discover_error));TextButton({revision++}){Text(L.s(R.string.discover_retry))}}
        items(sources,key={"source:"+it.provider}) {source->
            Text(source.provider+" · "+sourceLabel(source.state)+(source.refreshState?.let {" · "+sourceLabel(it)} ?: ""),style=MaterialTheme.typography.bodySmall)
            if(source.state !in setOf(SourceState.FRESH,SourceState.CACHED))TextButton({revision++}){Text(L.s(R.string.discover_retry))}
        }
        if(rows.isEmpty() && !loading)item {Text(L.s(R.string.discover_empty))}
        items(rows,key={it.id}) {entity->CatalogueRow(entity){open(entity.id)}}
        items(candidates.filterNot {candidate->rows.any {it.id==candidate.id}},key={"candidate:"+it.id}) {candidate->
            Text(L.s(R.string.discover_candidate),style=MaterialTheme.typography.labelSmall)
            CatalogueRow(CatalogueEntity(candidate.id,candidate.kindHint,candidate.title,status="quarantined")){open(candidate.id)}
        }
        if(cursor!=null)item {TextButton(enabled=!paging,onClick={
            val next=cursor ?: return@TextButton
            val requestedKey=searchKey;val requestedQuery=query;val requestedFilter=filter
            paging=true
            scope.launch {
                try {
                    val page=app.discover.page(requestedQuery,requestedFilter,next)
                    if(currentSearchKey==requestedKey){rows=(rows+page.items).distinctBy {it.id};cursor=page.nextCursor}
                } catch(e:CancellationException){throw e}catch(_:Exception){if(currentSearchKey==requestedKey){cursor=null;error=true}}
                finally {if(currentSearchKey==requestedKey)paging=false}
            }
        }){Text(L.s(R.string.discover_more))}}
        if(!active && recent.isNotEmpty()) {
            item {Text(L.s(R.string.discover_recent),style=MaterialTheme.typography.titleLarge,modifier=Modifier.semantics {heading()})}
            items(recent,key={"recent:"+it.id}) {entity->CatalogueRow(entity){open(entity.id)};TextButton({scope.launch {withContext(Dispatchers.IO){app.discoverHistory.remove(entity.id)};recent=recent.filterNot {it.id==entity.id}}}){Text(L.s(R.string.discover_remove))}}
        }
        item {
            HorizontalDivider()
            Text(L.s(R.string.discover_cache_consequences),style=MaterialTheme.typography.bodySmall)
            Text(android.text.format.Formatter.formatShortFileSize(LocalContext.current,cacheBytes))
            TextButton(onClick={scope.launch {app.discover.clearResponseCache();cacheBytes=app.providerCache.bytes()}}){Text(L.s(R.string.discover_clear_cache))}
        }
        item {HorizontalDivider();Row {Switch(historyEnabled,{value->historyEnabled=value;scope.launch {withContext(Dispatchers.IO){app.discoverHistory.enabled(value)};revision++}});Text(L.s(R.string.discover_history))}}
        item {TextButton({scope.launch {withContext(Dispatchers.IO){app.discoverHistory.clear()};recent=emptyList()}}){Text(L.s(R.string.discover_clear_history))}}
    }
}
@Composable private fun CatalogueRow(entity:CatalogueEntity,onClick:()->Unit) {
    JournalCard(Modifier.fillMaxWidth().clickable(onClick=onClick)) {Column(Modifier.padding(12.dp)) {
        Text(entity.title,style=MaterialTheme.typography.titleMedium)
        Text(kindLabel(entity.kind),style=MaterialTheme.typography.bodySmall)
    }}
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
    var portraitCredit by remember(id){mutableStateOf<JSONObject?>(null)}
    var portrait by remember(id){mutableStateOf<android.graphics.Bitmap?>(null)}
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
        if(detail!=null){withContext(Dispatchers.IO){app.discoverHistory.opened(id)};portrait=app.cataloguePortraits.read(id);portraitCredit=app.cataloguePortraits.credits(id)}
    }
    LazyColumn(contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item {TextButton(back){Text(L.s(R.string.discover_back))}}
        if(!loaded)item {LinearProgressIndicator(Modifier.fillMaxWidth())}
        val data=detail
        if(loaded && data==null && candidate!=null)item {
            Text(candidate!!.title,style=MaterialTheme.typography.headlineLarge)
            Text(L.s(R.string.discover_candidate))
            Button(onClick={scope.launch {val resolved=app.discover.resolveCandidate(candidate!!);if(resolved!=null)open(resolved) else notice=L.s(R.string.discover_error)}}){Text(L.s(R.string.discover_verify_candidate))}
            notice?.let {Text(it)}
        }
        if(loaded && data==null && candidate==null)item {Text(L.s(R.string.discover_missing))}
        if(data!=null) {
            item {Text(CataloguePresentation.title(data,language),style=MaterialTheme.typography.headlineLarge,modifier=Modifier.semantics {heading()});Text(kindLabel(data.entity.kind))}
            portrait?.let {bitmap->item {Image(bitmap.asImageBitmap(),contentDescription=L.s(R.string.discover_portrait_description),modifier=Modifier.fillMaxWidth().heightIn(max=240.dp))}}
            if(portrait!=null && portraitCredit!=null)item {
                val credit=portraitCredit!!;val metadata=credit.getJSONObject("extmetadata")
                Text(metadata.getJSONObject("Artist").getString("value")+" · "+metadata.getJSONObject("LicenseShortName").getString("value"),style=MaterialTheme.typography.bodySmall)
                TextButton({uri.openUri(credit.getString("descriptionurl"))}){Text(L.s(R.string.discover_source_link))}
                TextButton({uri.openUri(metadata.getJSONObject("LicenseUrl").getString("value"))}){Text(L.s(R.string.discover_license))}
            }
            if(CataloguePresentation.conflicts(data).isNotEmpty())item {Text(L.s(R.string.discover_conflict_notice))}
            if(data.identities.any {it.provider=="musicbrainz"})item {
                TextButton(onClick={scope.launch {supplementSources=listOf(app.discover.refreshMetadata(data));detail=(app.discover.detail(id) as? CatalogueAccess.Available)?.value}}){Text(L.s(R.string.discover_refresh_metadata))}
            }
            item {Text(L.s(if(data.identities.any {it.reviewState=="reviewed"})R.string.discover_identity_notice else R.string.discover_candidate),style=MaterialTheme.typography.bodySmall)}
            if(data.entity.kind in setOf("work","arrangement") && data.identities.any {it.reviewState=="reviewed"}) {
                val parts=data.relationships.filter {it.first.type=="part" && it.first.toId==id}
                if(parts.isNotEmpty()) {
                    item {Text(L.s(R.string.discover_parts_choice))}
                    items(parts,key={"select:"+it.first.id}) {(_,part)->Row {Checkbox(part.id in selection,{checked->selection=if(checked)selection+part.id else selection-part.id});Text(part.title)}}
                    if(selection.isNotEmpty())item {Row {Checkbox(separate,{separate=it});Text(L.s(R.string.discover_separate))}}
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
            items(data.relationships,key={"edge:"+it.first.id}) {(edge,entity)->Text(relationLabel(edge.type),style=MaterialTheme.typography.labelSmall);CatalogueRow(entity){open(entity.id)}}
            if(data.assertions.any {it.assertion.field in setOf("wikipedia_title","commons_file")})item {
                TextButton(onClick={scope.launch {supplementSources=app.discover.supplement(data,language)
                    val metadata=app.discover.portraitMetadata(id)
                    if(metadata!=null)try {if(app.cataloguePortraits.download(id,metadata)){portrait=app.cataloguePortraits.read(id);portraitCredit=app.cataloguePortraits.credits(id)};portraitCredit=app.cataloguePortraits.credits(id)}catch(e:CancellationException){throw e}catch(_:Exception){notice=L.s(R.string.discover_portrait_unavailable)}
                    detail=(app.discover.detail(id) as? CatalogueAccess.Available)?.value}}){Text(L.s(R.string.discover_load_encyclopedia))}
            }
            items(supplementSources,key={"supplement:"+it.provider}) {Text(it.provider+" · "+sourceLabel(it.state))}
            item {Text(L.s(R.string.discover_sources),style=MaterialTheme.typography.titleLarge)}
            item {TextButton({correction=!correction}){Text(L.s(R.string.discover_correction))}}
            if(correction)item {
                OutlinedTextField(correctionField,{correctionField=it.take(80)},label={Text(L.s(R.string.discover_correction_field))},modifier=Modifier.fillMaxWidth())
                OutlinedTextField(correctionValue,{correctionValue=it.take(4096)},label={Text(L.s(R.string.discover_correction_value))},modifier=Modifier.fillMaxWidth())
                OutlinedTextField(correctionReason,{correctionReason=it.take(4096)},label={Text(L.s(R.string.discover_correction_reason))},modifier=Modifier.fillMaxWidth())
                Text(L.s(R.string.discover_correction_private),style=MaterialTheme.typography.bodySmall)
                TextButton(enabled=correctionField.isNotBlank() && correctionValue.isNotBlank() && correctionReason.isNotBlank(),onClick={scope.launch {correctionExport=withContext(Dispatchers.Default){CataloguePresentation.correction(data,correctionField,correctionValue,correctionReason)};exportCorrection.launch("Goldberg-catalogue-correction.json")}}){Text(L.s(R.string.discover_correction_export))}
            }
            items(data.assertions.filterNot {it.assertion.field.startsWith("claim:")},key={it.assertion.id}) {assertion->
                val json=runCatching {JSONObject(assertion.assertion.valueJson)}.getOrNull()
                Text(assertion.assertion.field+": "+(json?.optString("value") ?: assertion.assertion.valueJson),style=MaterialTheme.typography.bodyMedium)
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
private fun kindLabel(key:String)=L.s(when(key){"work"->R.string.discover_work;"arrangement"->R.string.discover_arrangement;"artist"->R.string.discover_artist;"album"->R.string.discover_album;"release"->R.string.discover_release;"recording"->R.string.discover_recording;else->R.string.discover_collection})
private fun relationLabel(key:String)=L.s(when(key){"part"->R.string.discover_part_relation;"credit"->R.string.discover_credit_relation;"performance"->R.string.discover_performance_relation;else->R.string.discover_related})
private fun sourceLabel(state:SourceState)=L.s(when(state){SourceState.FRESH->R.string.discover_fresh;SourceState.CACHED->R.string.discover_cached;SourceState.STALE->R.string.discover_stale;SourceState.RATE_LIMITED->R.string.discover_rate_limited;SourceState.BLOCKED_RIGHTS->R.string.discover_blocked;SourceState.MISSING->R.string.discover_missing;SourceState.INVALID->R.string.discover_invalid;else->R.string.discover_unavailable})
