package com.opus.piano.data.catalogue

import android.content.Context
import com.opus.piano.data.catalogue.providers.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

data class SearchSource(val provider:String,val state:SourceState,val error:String?=null,val refreshState:SourceState?=null,val requestKind:String?=null)
data class DiscoverSearch(val queryId:String,val page:CatalogueSearchPage,val sources:List<SearchSource> = emptyList(),val candidates:List<CatalogueCandidate> = emptyList(),val continuation:String?=null,val limitReached:Boolean=false,val providerEntityIds:Set<String> = emptySet())
internal fun isActiveCatalogueSearchProvider(provider:String)=provider=="musicbrainz"

/** The only online entry point requires an explicit active search. Construction/landing are local. */
class DiscoverRepository(private val store:LocalCatalogueStore,private val client:ProviderClient,private val permitted:()->Boolean,
    private val storage:CatalogueStorage?=null, adapters:List<ProviderAdapter> = listOf(MusicBrainzAdapter()),private val now:()->Long=System::currentTimeMillis,
    private val prepareLocal:suspend ()->Unit = {}) {
    private val adapters = adapters.filter { isActiveCatalogueSearchProvider(it.id) }
    private val paging=DiscoverPaging(now)
    private val localPreparation = kotlinx.coroutines.sync.Mutex()
    private var localPrepared = false
    @Volatile private var localReady = false
    private val localSearchCount = AtomicLong()
    private suspend fun prepareLocalOnce(context: CataloguePerformance.Context) = localPreparation.withLock {
        val reusedAt = CataloguePerformance.start()
        if (localReady) {
            CataloguePerformance.finish("catalogue_initialize_reused", reusedAt, "outcome=ok", context)
            return@withLock
        }
        var initialization = CataloguePerformance.measureSuspend("catalogue_initialize", context=context) { store.initialize(context) }
        logInitializationResult(initialization, context)
        val preparationStartedAt = CataloguePerformance.start()
        if (!localPrepared) {
            CataloguePerformance.measureSuspend("catalogue_prepare", context=context) { prepareLocal() }
            localPrepared = true
            if (initialization !is CatalogueAccess.Available) {
                initialization = CataloguePerformance.measureSuspend("catalogue_initialize_retry_after_prepare", context=context) { store.initialize(context) }
                logInitializationResult(initialization, context)
            }
        } else {
            CataloguePerformance.finish("catalogue_prepare_reused", preparationStartedAt, "outcome=ok", context)
        }
        val complete = when (initialization) {
            is CatalogueAccess.Available -> initialization.value
            is CatalogueAccess.Rejected -> throw IllegalArgumentException(initialization.reason)
            is CatalogueAccess.Unavailable -> throw IllegalStateException(initialization.reason, initialization.cause)
        }
        CataloguePerformance.event("catalogue_initialization_state", "outcome=${if(complete)"complete" else "incomplete"}", context)
        localReady = complete
    }
    private fun logInitializationResult(result: CatalogueAccess<Boolean>, context: CataloguePerformance.Context) {
        CataloguePerformance.event("catalogue_initialize_result", when (result) {
            is CatalogueAccess.Available -> "outcome=available complete=${result.value}"
            is CatalogueAccess.Rejected -> "outcome=rejected"
            is CatalogueAccess.Unavailable -> "outcome=unavailable"
        }, context)
    }
    private fun usable(state:SourceState)=state in setOf(SourceState.FRESH,SourceState.CACHED,SourceState.STALE)
    private suspend fun canStore(bytes:Long=CatalogueStorage.MAX_RESPONSE)=withContext(Dispatchers.IO){storage?.admits(bytes) ?: true}
    /** Admission and commit share the store lock: parallel providers cannot spend the same space. */
    private suspend fun <T> intake(block:suspend (CatalogueRepository)->T):CatalogueAccess<T> = store.access {repo->
        if(storage!=null)storage.intake.commit(CatalogueStorage.MAX_INTAKE){block(repo)}
        else {currentCoroutineContext().ensureActive();block(repo)}
    }
    suspend fun portraitMetadata(id:String):org.json.JSONObject? = (store.access {it.state("portrait:$id")?.let {value->org.json.JSONObject(value)}} as? CatalogueAccess.Available)?.value
    suspend fun clearResponseCache(){client.clearCache()}
    private suspend fun local(query:String,filter:SearchFilter,cursor:String?=null,excluded:Set<String> = emptySet(),limit:Int=40,sessionId:String="startup"):CatalogueSearchPage {
        val context = CataloguePerformance.Context(sessionId, "local", filter.category)
        val requestStartedAt = CataloguePerformance.start()
        var startedAt:Long?=null
        var phase="catalogue_local_query"
        try {
            prepareLocalOnce(context)
            val sequence = localSearchCount.incrementAndGet()
            phase = if(sequence==1L)"local_search_first" else "local_search_repeat"
            val queryStartedAt=CataloguePerformance.start()
            startedAt=queryStartedAt
            val page = when(val access=store.access(context) {it.searchPage(query,filter,cursor,limit,excluded)}){
                is CatalogueAccess.Available->access.value
                is CatalogueAccess.Rejected->throw IllegalArgumentException("Invalid or stale search cursor")
                is CatalogueAccess.Unavailable->throw IllegalStateException(access.reason,access.cause)
            }
            CataloguePerformance.finish(phase,queryStartedAt,"outcome=ok result_count=${page.items.size}",context)
            CataloguePerformance.finish("catalogue_local_result",requestStartedAt,"outcome=ok result_count=${page.items.size}",context)
            return page
        } catch(e:CancellationException) {
            startedAt?.let {CataloguePerformance.finish(phase,it,"outcome=cancelled result_count=0",context)}
            CataloguePerformance.finish("catalogue_local_result",requestStartedAt,"outcome=cancelled result_count=0",context)
            throw e
        } catch(e:Exception) {
            startedAt?.let {CataloguePerformance.finish(phase,it,"outcome=error result_count=0 error_class=${e.javaClass.simpleName}",context)}
            CataloguePerformance.finish("catalogue_local_result",requestStartedAt,"outcome=error result_count=0 error_class=${e.javaClass.simpleName}",context)
            throw e
        }
    }
    suspend fun landing(sessionId:String=UUID.randomUUID().toString()):CatalogueSearchPage = local("",SearchFilter("artists"),limit=25,sessionId=sessionId)
    /** Same local data, identity, pagination and detail path in both tabs. No network. */
    suspend fun library(query:String,category:String="all",cursor:String?=null,sessionId:String=UUID.randomUUID().toString()):CatalogueSearchPage {
        require(query.length<=256 && category in setOf("all","artists","works"))
        return local(query,SearchFilter(category=category),cursor,sessionId=sessionId)
    }
    suspend fun filters():DiscoverFilters = filters(CataloguePerformance.Context())
    internal suspend fun filters(context:CataloguePerformance.Context):DiscoverFilters {
        val startedAt=CataloguePerformance.start()
        val access=store.access(context) {repo->DiscoverFilters(
            repo.queryEntities(androidx.sqlite.db.SimpleSQLiteQuery("SELECT e.* FROM entities e WHERE e.kind='artist' AND e.status='active' AND EXISTS(SELECT 1 FROM relationships r WHERE r.type='credit' AND r.toId=e.id) ORDER BY e.title,e.id LIMIT 40")),repo.availablePeriods())
        }
        val outcome=when(access) {
            is CatalogueAccess.Available->"ok"
            is CatalogueAccess.Rejected->"rejected"
            is CatalogueAccess.Unavailable->"unavailable"
        }
        val value=(access as? CatalogueAccess.Available)?.value ?: DiscoverFilters()
        CataloguePerformance.finish("discover_filter_load",startedAt,"outcome=$outcome result_count=${value.artists.size+value.periods.size}",context)
        return value
    }
    suspend fun page(query:String,filter:SearchFilter,cursor:String?=null)=local(query,filter,cursor)
    private fun combine(previous:SearchSource?,next:SearchSource):SearchSource {
        fun severity(state:SourceState)=when(state){SourceState.FRESH,SourceState.CACHED->0;SourceState.STALE->1;SourceState.MISSING->2;SourceState.UNAVAILABLE->3;SourceState.INVALID->4;SourceState.BLOCKED_RIGHTS->5;SourceState.RATE_LIMITED->6}
        return if(previous!=null && maxOf(severity(previous.state),previous.refreshState?.let(::severity) ?: 0)>maxOf(severity(next.state),next.refreshState?.let(::severity) ?: 0))previous else next
    }
    private fun orderedSources(states:Collection<SearchSource>)=adapters.flatMap {adapter->states.filter {it.provider==adapter.id}.sortedBy {it.requestKind}}
    private fun sourceKey(source:SearchSource)=source.provider+":"+source.requestKind.orEmpty()
    private suspend fun acquire(adapter:ProviderAdapter,request:ProviderRequest,sessionId:String,category:String):Pair<ProviderResult,SearchSource> {
        require(isActiveCatalogueSearchProvider(adapter.id) && isActiveCatalogueSearchProvider(request.provider))
        val context=CataloguePerformance.Context(sessionId,"online",category)
        val result=CataloguePerformance.measureSuspend("online_search", "provider=${adapter.id} request_kind=${request.kind}",context) { client.fetch(adapter,request) }
        var source=SearchSource(adapter.id,result.state,result.error,result.refreshState)
        if(usable(result.state)) {
            if(canStore()) {
                val accepted=intake {it.ingest(result.copy(state=SourceState.FRESH))}
                if(accepted !is CatalogueAccess.Available)source=SearchSource(adapter.id,SourceState.UNAVAILABLE,"storage")
            } else source=SearchSource(adapter.id,SourceState.UNAVAILABLE,"storage_budget")
        }
        return result to source.copy(requestKind=request.kind)
    }
    private suspend fun finish(query:String,filter:SearchFilter,frame:DiscoverFrame,page:CatalogueSearchPage,observations:List<CatalogueCandidate>):DiscoverSearch {
        val context=CataloguePerformance.Context(frame.queryId,"local",filter.category)
        val moreStartedAt=CataloguePerformance.start()
        val more=frame.providers.isNotEmpty() || page.nextCursor!=null || candidates(query,filter,frame.seenCandidates,frame.queryId).isNotEmpty()
        val token=if(more)paging.issue(frame) else null
        CataloguePerformance.finish("catalogue_paging_availability",moreStartedAt,"outcome=ok result_count=${page.items.size} has_more=$more",context)
        return DiscoverSearch(frame.queryId,page,frame.sources,observations,token,more && token==null,frame.providerEntityIds)
    }
    /** The persisted Settings toggle is consent. Callers cancel this flow on query/filter/toggle
     * changes; local results precede the debounce and no empty/one-character query goes online. */
    fun automaticSearch(query:String,filter:SearchFilter,language:String,online:Boolean,sessionId:String=UUID.randomUUID().toString()):Flow<DiscoverSearch> = flow {
        val sessionStartedAt=CataloguePerformance.start()
        var initial:DiscoverSearch?=null
        search(query,filter,language,false,sessionId,sessionStartedAt).collect {initial=it;emit(it)}
        val context=CataloguePerformance.Context(sessionId,"online",filter.category)
        if(!online)CataloguePerformance.event("catalogue_online_skipped","outcome=consent_off result_count=0",context)
        else if(query.trim().length<2)CataloguePerformance.event("catalogue_online_skipped","outcome=query_too_short result_count=0",context)
        else {
            CataloguePerformance.measureSuspend("catalogue_online_debounce","target_ms=500",context) {delay(500)}
            search(query,filter,language,true,sessionId,sessionStartedAt,initial).collect {emit(it)}
        }
    }
    private suspend fun observedIds(result:ProviderResult):Set<String> =
        (store.access {repo->result.records.mapNotNull {record->
            repo.providerIdentity(result.request.provider,record.providerKind,record.externalId)?.entityId?.let {repo.resolve(it)?.id}
        }.toSet()} as? CatalogueAccess.Available)?.value.orEmpty()
    fun search(query:String,filter:SearchFilter,language:String,online:Boolean,sessionId:String=UUID.randomUUID().toString(),onlineStartedAtNanos:Long?=null,initial:DiscoverSearch?=null):Flow<DiscoverSearch> = channelFlow {
        require(query.length<=256 && language in setOf("cs","en"))
        val id=sessionId
        val searchContext=CataloguePerformance.Context(id,"local",filter.category)
        val searchStartedAt=CataloguePerformance.start()
        val onlineStartedAt=onlineStartedAtNanos ?: searchStartedAt
        CataloguePerformance.event("catalogue_search_started","outcome=started result_count=0 online_requested=$online",searchContext)
        val items=linkedMapOf<String,CatalogueEntity>()
        val first=initial?.page ?: local(query,filter,sessionId=id);first.items.forEach {items[it.id]=it}
        val firstCandidates=initial?.candidates ?: candidates(query,filter,sessionId=id)
        if(initial==null) {
            send(DiscoverSearch(id,first,candidates=firstCandidates))
            CataloguePerformance.finish("catalogue_first_local_result",searchStartedAt,"outcome=ok result_count=${first.items.size}",searchContext)
        } else CataloguePerformance.event("catalogue_local_seed_reused","outcome=ok result_count=${first.items.size}",searchContext)
        val providerIds=linkedSetOf<String>()
        val states=java.util.concurrent.ConcurrentHashMap<String,SearchSource>()
        val positions=linkedMapOf<String,ProviderPosition>()
        var currentPage=first
        var currentCandidates=firstCandidates
        val emission=kotlinx.coroutines.sync.Mutex()
        var firstOnlineResult=true
        if(online && query.trim().length>=2 && permitted())supervisorScope {
            adapters.forEach {adapter->launch {
                for(request in adapter.search(query.trim(),filter.category,language)) {
                    currentCoroutineContext().ensureActive()
                    val (result,source)=acquire(adapter,request,id,filter.category)
                    emission.lock()
                    try {
                        val mergeStartedAt=CataloguePerformance.start()
                        providerIds.addAll(observedIds(result).take(500-providerIds.size))
                        states[sourceKey(source)]=source
                        if(usable(result.state))result.nextOffset?.let {positions[adapter.id+":"+request.kind]=ProviderPosition(adapter.id,request.kind,it)}
                        val page=if(usable(result.state) && result.records.isNotEmpty()) local(query,filter,sessionId=id) else currentPage
                        if(page!==currentPage){currentPage=page;items.clear();page.items.forEach {items[it.id]=it}}
                        // Classification-pending records are stored as candidates, not entities, so
                        // the list must be re-read after ingestion or the UI never shows them.
                        val refreshedCandidates=if(usable(result.state) && result.records.isNotEmpty()) candidates(query,filter,sessionId=id) else currentCandidates
                        currentCandidates=refreshedCandidates
                        CataloguePerformance.event("catalogue_provider_result","provider=${adapter.id} request_kind=${request.kind} outcome=${result.state.name.lowercase(java.util.Locale.ROOT)} result_count=${result.records.size}",CataloguePerformance.Context(id,"online",filter.category))
                        if(firstOnlineResult) {
                            firstOnlineResult=false
                            CataloguePerformance.finish("catalogue_first_online_result",onlineStartedAt,"outcome=${result.state.name.lowercase(java.util.Locale.ROOT)} result_count=${result.records.size}",CataloguePerformance.Context(id,"online",filter.category))
                        }
                        val merged=page.copy(items=items.values.toList())
                        val unresolved=refreshedCandidates
                        CataloguePerformance.finish("catalogue_provider_result_refresh",mergeStartedAt,"outcome=ok result_count=${merged.items.size} candidate_count=${unresolved.size}",CataloguePerformance.Context(id,"local_online",filter.category))
                        send(DiscoverSearch(id,merged,orderedSources(states.values),unresolved,providerEntityIds=providerIds.toSet()))
                    }finally {emission.unlock()}
                    if(result.state==SourceState.RATE_LIMITED)break
                }
            }}
        }
        if(online && query.trim().length>=2 && !permitted())adapters.forEach {adapter->
            val source=SearchSource(adapter.id,SourceState.UNAVAILABLE,"network_policy")
            states[sourceKey(source)]=source
        }
        if(online && query.trim().length>=2 && !permitted())CataloguePerformance.event("catalogue_online_skipped","outcome=network_policy result_count=0",CataloguePerformance.Context(id,"online",filter.category))
        val page=currentPage
        val candidates=currentCandidates
        val frame=DiscoverFrame(DiscoverPaging.binding(query,filter,language,online),id,page.generation,items.keys.lastOrNull(),items.keys.toSet(),candidates.map {it.id}.toSet(),positions.values.toList(),orderedSources(states.values),providerEntityIds=providerIds.toSet())
        send(finish(query,filter,frame,page.copy(items=items.values.toList()),candidates))
    }
    suspend fun continueSearch(query:String,filter:SearchFilter,language:String,online:Boolean,cursor:String):DiscoverSearch {
        require(query.length<=256 && language in setOf("cs","en"))
        val slot=paging.get(cursor,DiscoverPaging.binding(query,filter,language,online))
        slot.lock.lock()
        try {
            val frame=slot.frame
            val expected=slot.replay?.page?.generation ?: frame.generation
            require(local(query,filter,limit=1,sessionId=frame.queryId).generation==expected){"Stale catalogue generation"}
            slot.replay?.let {return it}
            var page=local(query,filter,excluded=frame.seen,limit=minOf(40,500-frame.seen.size),sessionId=frame.queryId)
            var candidates=candidates(query,filter,frame.seenCandidates,frame.queryId).take(500-frame.seenCandidates.size)
            var positions=frame.providers
            val providerIds=frame.providerEntityIds.toMutableSet()
            val states=frame.sources.associateBy {sourceKey(it)}.toMutableMap()
            if(online && positions.isNotEmpty() && !permitted())positions.forEach {position->
                val source=SearchSource(position.provider,SourceState.UNAVAILABLE,"network_policy",requestKind=position.kind);states[sourceKey(source)]=source
            }
            if(page.items.isEmpty() && candidates.isEmpty() && online && permitted()) {
                val next=mutableListOf<ProviderPosition>()
                supervisorScope {positions.map {position->async {
                    val adapter=requireNotNull(adapters.firstOrNull {it.id==position.provider})
                    val request=adapter.search(query.trim(),filter.category,language,position.offset).single {it.kind==position.kind}
                    Triple(position,request,acquire(adapter,request,frame.queryId,filter.category))
                }}.awaitAll()}.forEach {(position,_,resultAndState)->
                    val (result,source)=resultAndState;providerIds.addAll(observedIds(result).take(500-providerIds.size));states[sourceKey(source)]=source
                    if(usable(result.state))result.nextOffset?.let {next+=position.copy(offset=it)} else next+=position
                }
                positions=next
                page=local(query,filter,excluded=frame.seen,limit=minOf(40,500-frame.seen.size),sessionId=frame.queryId);candidates=candidates(query,filter,frame.seenCandidates,frame.queryId).take(500-frame.seenCandidates.size)
            }
            val seen=frame.seen+page.items.map {it.id};val observed=frame.seenCandidates+candidates.map {it.id}
            val nextFrame=frame.copy(generation=page.generation,lastId=page.items.lastOrNull()?.id ?: frame.lastId,seen=seen,seenCandidates=observed,providers=positions,sources=orderedSources(states.values),ordinal=frame.ordinal+1,providerEntityIds=providerIds.toSet())
            return finish(query,filter,nextFrame,page,candidates).also {slot.replay=it}
        }finally {slot.lock.unlock()}
    }
    private suspend fun candidates(query:String,filter:SearchFilter,excluded:Set<String> = emptySet(),sessionId:String="startup"):List<CatalogueCandidate> {
        val startedAt=CataloguePerformance.start()
        val context=CataloguePerformance.Context(sessionId,"local_cache",filter.category)
        return try {
            val access=store.access(context) {it.candidates(query,filter,excluded)}
            val outcome=when(access) {
                is CatalogueAccess.Available->"ok"
                is CatalogueAccess.Rejected->"rejected"
                is CatalogueAccess.Unavailable->"unavailable"
            }
            val rows=(access as? CatalogueAccess.Available)?.value.orEmpty().filter { isActiveCatalogueSearchProvider(it.provider) }
            CataloguePerformance.finish("catalogue_candidate_results",startedAt,"outcome=$outcome result_count=${rows.size}",context)
            rows
        } catch(e:CancellationException) {
            CataloguePerformance.finish("catalogue_candidate_results",startedAt,"outcome=cancelled result_count=0",context)
            throw e
        } catch(e:Exception) {
            CataloguePerformance.finish("catalogue_candidate_results",startedAt,"outcome=error result_count=0 error_class=${e.javaClass.simpleName}",context)
            throw e
        }
    }
    suspend fun candidate(id:String):CatalogueCandidate?=when(val access=store.access {repo->repo.states("candidate:",200).firstOrNull {runCatching {candidateFromJson(it.value).id==id}.getOrDefault(false)}?.let {repo.touchObservation(it.key);candidateFromJson(it.value)}}){is CatalogueAccess.Available->access.value;else->null}
    suspend fun resolveCandidate(candidate:CatalogueCandidate):String? {
        if(!isActiveCatalogueSearchProvider(candidate.provider) || !permitted())return null
        val adapter=MusicBrainzAdapter()
        val result=client.fetch(adapter,adapter.lookup(candidate.providerKind,candidate.externalId))
        if(!usable(result.state))return null
        if(intake {it.ingest(result.copy(state=SourceState.FRESH))} !is CatalogueAccess.Available)return null
        return (store.access {it.providerIdentity(candidate.provider,candidate.providerKind,candidate.externalId)?.entityId} as? CatalogueAccess.Available)?.value
    }
    suspend fun refreshMetadata(detail:DiscoverDetail):SearchSource {
        val identity=detail.identities.firstOrNull {it.provider=="musicbrainz"} ?: return SearchSource("musicbrainz",SourceState.MISSING)
        if(!permitted())return SearchSource("musicbrainz",SourceState.UNAVAILABLE,"network_policy")
        val adapter=MusicBrainzAdapter();val result=client.fetch(adapter,adapter.lookup(identity.providerKind,identity.externalId),force=true)
        val stored=if(usable(result.state))intake {it.ingest(result.copy(state=SourceState.FRESH))} else null
        return SearchSource("musicbrainz",if(usable(result.state) && stored !is CatalogueAccess.Available)SourceState.UNAVAILABLE else result.state,if(usable(result.state) && stored !is CatalogueAccess.Available)"storage_budget" else result.error)
    }
    suspend fun supplement(detail:DiscoverDetail,language:String):List<SearchSource> {
        require(language in setOf("cs","en"))
        if(!permitted())return listOf(SearchSource("wikipedia",SourceState.UNAVAILABLE,"network_policy"))
        val wiki=WikipediaAdapter();val commons=CommonsAdapter();val states=mutableListOf<SearchSource>()
        fun field(name:String,lang:String?=null)=detail.assertions.firstOrNull {row->row.rights.reviewState=="approved" && row.source.provider=="wikidata" && row.assertion.field==name && (lang==null || org.json.JSONObject(row.assertion.valueJson).optString("language")==lang)}?.let {org.json.JSONObject(it.assertion.valueJson).optString("value")}
        val titles=listOf(language,"en").distinct().mapNotNull {lang->field("wikipedia_title",lang)?.let {lang to it}}
        var fallbackReason=if(titles.firstOrNull()?.first!=language)"requested_language_title_missing" else ""
        for((selectedLanguage,title) in titles) {
            if(!permitted())break
            val result=client.fetch(wiki,wiki.document(title,selectedLanguage))
            states+=SearchSource(wiki.id,result.state,result.error,result.refreshState,"document:$selectedLanguage")
            if(usable(result.state) && result.supplement.isNotEmpty()) {
                val raw=result.supplement["html"] as? String ?: ""
                // No WebView or network-capable renderer; extraction runs away from the UI thread.
                val text=withContext(Dispatchers.Default){
                    val stripped=raw.replace(Regex("(?is)<(script|style|noscript)[^>]*>.*?</\\1>"),"")
                    val paragraphs=Regex("(?is)<p\\b[^>]*>.*?</p>").findAll(stripped).take(20).joinToString("\n"){it.value}
                    android.text.Html.fromHtml(paragraphs,android.text.Html.FROM_HTML_MODE_LEGACY).toString().trim().take(2000)
                }
                if(text.isNotBlank()) {
                    val revision=result.supplement["revision"].toString()
                    val record=ProviderRecord("document",title,"artist",title,listOf(FieldValue("biography",text,selectedLanguage),FieldValue("article_revision",revision),FieldValue("article_revision_url","https://$selectedLanguage.wikipedia.org/w/index.php?oldid=$revision"),FieldValue("article_history","https://$selectedLanguage.wikipedia.org/w/index.php?title=${com.opus.piano.data.encoded(title)}&action=history"),FieldValue("adaptation","Plain-text excerpt; truncated to 2000 characters"),FieldValue("document_requested_language",language),FieldValue("document_fallback_reason",fallbackReason)))
                    val stored=if(canStore())intake {it.supplement(detail.entity.id,result.copy(records=listOf(record),license="CC-BY-SA-4.0",licenseUrl="https://creativecommons.org/licenses/by-sa/4.0/"))} else null
                    if(stored !is CatalogueAccess.Available)states[states.lastIndex]=SearchSource(wiki.id,SourceState.UNAVAILABLE,"storage_budget",requestKind="document:$selectedLanguage")
                    break
                }
                states[states.lastIndex]=SearchSource(wiki.id,SourceState.MISSING,"empty_excerpt",requestKind="document:$selectedLanguage")
                fallbackReason="requested_language_excerpt_empty"
            } else fallbackReason="requested_language_"+result.state.name.lowercase(java.util.Locale.ROOT)
            // A provider-wide outage/overload must not trigger more calls to another edition.
            if(result.state in setOf(SourceState.RATE_LIMITED,SourceState.UNAVAILABLE) || result.refreshState in setOf(SourceState.RATE_LIMITED,SourceState.UNAVAILABLE))break
        }
        val file=field("commons_file")
        if(file!=null) {
            val result=client.fetch(commons,commons.metadata(file))
            val blocked=result.supplement["blocked"]==true
            if(usable(result.state) && !blocked && result.supplement.isNotEmpty()) {
                store.access {it.state("portrait:${detail.entity.id}",org.json.JSONObject(result.supplement).put("sourceHash",result.hash).put("observedAt",result.observedAt).toString())}
            }
            states+=SearchSource(commons.id,if(blocked)SourceState.BLOCKED_RIGHTS else result.state,result.error)
        }
        return states
    }
    suspend fun detail(id:String)=store.access {repo->repo.resolve(id)?.let {entity->
        val resolved=entity.id
        val related=repo.linked(resolved).mapNotNull {edge->repo.entity(if(edge.fromId==resolved)edge.toId else edge.fromId)?.let {edge to it}}
        // Related subtype labels come from the same read; no extra store access from the UI.
        val relatedTypes=related.mapNotNull {(_,linked)->if(linked.kind=="work")repo.workType(linked.id)?.let {linked.id to it}else null}.toMap()
        DiscoverDetail(entity,repo.identities(resolved),repo.assertions(resolved).mapNotNull {a->val source=repo.source(a.sourceId);val rights=repo.rights(a.rightsId);if(source!=null && rights!=null)SourcedAssertion(a,source,rights) else null},related,repo.workType(resolved),relatedTypes)
    }}
    companion object {const val FRESH_MILLIS=24*60*60*1000L}
}
data class SourcedAssertion(val assertion:CatalogueAssertion,val source:CatalogueSource,val rights:CatalogueRights)
data class DiscoverFilters(val artists:List<CatalogueEntity> = emptyList(),val periods:List<String> = emptyList())
data class DiscoverDetail(val entity:CatalogueEntity,val identities:List<CatalogueExternalIdentity>,val assertions:List<SourcedAssertion>,val relationships:List<Pair<CatalogueRelationship,CatalogueEntity>>,val workType:String?=null,val workTypes:Map<String,String> = emptyMap())

/** History contains opaque entity IDs only, no raw queries; separate from journal/backups. */
class DiscoverHistory(context:Context,private val preferenceName:String="goldberg-discover-history") {
    private val preferences=context.getSharedPreferences(preferenceName,Context.MODE_PRIVATE)
    val enabled get()=preferences.getBoolean("enabled",true)
    fun entries():List<String> = preferences.getString("ids","").orEmpty().split(',').filter {runCatching {CatalogueValidation.uuid(it)}.isSuccess}.distinct().take(20)
    fun opened(id:String) {CatalogueValidation.uuid(id);if(enabled)write(listOf(id)+entries().filterNot {it==id})}
    fun remove(id:String)=write(entries().filterNot {it==id})
    fun clear()=write(emptyList())
    fun enabled(value:Boolean){check(preferences.edit().putBoolean("enabled",value).commit());if(!value)clear()}
    private fun write(ids:List<String>){check(preferences.edit().putString("ids",ids.take(20).joinToString(",")).commit())}
}
