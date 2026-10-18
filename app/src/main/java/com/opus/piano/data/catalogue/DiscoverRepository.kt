package com.opus.piano.data.catalogue

import android.content.Context
import com.opus.piano.data.catalogue.providers.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import java.util.UUID

data class SearchSource(val provider:String,val state:SourceState,val error:String?=null,val refreshState:SourceState?=null)
data class DiscoverSearch(val queryId:String,val page:CatalogueSearchPage,val sources:List<SearchSource> = emptyList(),val candidates:List<CatalogueCandidate> = emptyList())

/** The only online entry point requires an explicit active search. Construction/landing are local. */
class DiscoverRepository(private val store:LocalCatalogueStore,private val client:ProviderClient,private val permitted:()->Boolean,
    private val storage:CatalogueStorage?=null, private val adapters:List<ProviderAdapter> = listOf(OpenOpusAdapter(),MusicBrainzAdapter(),WikidataAdapter()),private val now:()->Long=System::currentTimeMillis) {
    private fun usable(state:SourceState)=state in setOf(SourceState.FRESH,SourceState.CACHED,SourceState.STALE)
    private suspend fun canStore(bytes:Long=CatalogueStorage.MAX_RESPONSE)=withContext(Dispatchers.IO){storage?.admits(bytes) ?: true}
    suspend fun portraitMetadata(id:String):org.json.JSONObject? = (store.access {it.state("portrait:$id")?.let {value->org.json.JSONObject(value)}} as? CatalogueAccess.Available)?.value
    suspend fun clearResponseCache(){client.clearCache()}
    private suspend fun local(query:String,filter:SearchFilter,cursor:String?=null):CatalogueSearchPage =
        when(val access=store.access {it.searchPage(query,filter,cursor)}){is CatalogueAccess.Available->access.value;is CatalogueAccess.Rejected->throw IllegalArgumentException("Invalid or stale search cursor");else->CatalogueSearchPage(emptyList(),null,"unavailable")}
    suspend fun landing():CatalogueSearchPage {store.initialize();return local("",SearchFilter())}
    suspend fun filters():DiscoverFilters = when(val access=store.access {repo->DiscoverFilters(
        repo.queryEntities(androidx.sqlite.db.SimpleSQLiteQuery("SELECT e.* FROM entities e WHERE e.kind='artist' AND e.status='active' AND EXISTS(SELECT 1 FROM relationships r WHERE r.type='credit' AND r.toId=e.id) ORDER BY e.title,e.id LIMIT 40")),repo.availablePeriods())}) {
        is CatalogueAccess.Available->access.value
        else->DiscoverFilters()
    }
    suspend fun page(query:String,filter:SearchFilter,cursor:String)=local(query,filter,cursor)
    fun search(query:String,filter:SearchFilter,language:String,online:Boolean):Flow<DiscoverSearch> = channelFlow {
        require(query.length<=256 && language in setOf("cs","en"))
        val id=UUID.randomUUID().toString()
        send(DiscoverSearch(id,local(query,filter),candidates=candidates(query,filter)))
        if(!online || query.trim().length<2 || !permitted())return@channelFlow
        val states=java.util.concurrent.ConcurrentHashMap<String,SearchSource>()
        val emission=kotlinx.coroutines.sync.Mutex()
        supervisorScope {
            adapters.forEach { adapter -> launch {
                val requests=adapter.search(query.trim(),filter.category,language)
                if(requests.isEmpty())return@launch
                var state=SearchSource(adapter.id,SourceState.FRESH)
                for(request in requests) {
                    currentCoroutineContext().ensureActive()
                    val result=client.fetch(adapter,request)
                    if(adapter is WikidataAdapter && usable(result.state)) {
                        for(candidate in result.candidates.take(3)) {
                            val observation=org.json.JSONObject().put("id",candidate.id).put("title",candidate.title).put("description",candidate.description).put("state","unresolved").put("sourceHash",result.hash).put("observedAt",result.observedAt)
                            if(canStore(8192))store.access {it.state("candidate:${candidate.id}",observation.toString())}
                            val typed=client.fetch(adapter,adapter.lookup(candidate.id))
                            if(usable(typed.state) && typed.records.isNotEmpty() && canStore())store.access {it.ingest(typed.copy(state=SourceState.FRESH))}
                            if(typed.state==SourceState.RATE_LIMITED){state=SearchSource(adapter.id,typed.state,typed.error);break}
                        }
                    }
                    if(usable(result.state) && canStore()) {
                        val accepted=store.access { repo->repo.ingest(result.copy(state=SourceState.FRESH)) }
                        if(state.state!=SourceState.RATE_LIMITED)state=SearchSource(adapter.id,if(accepted is CatalogueAccess.Available)result.state else SourceState.UNAVAILABLE,if(accepted is CatalogueAccess.Available)result.error else "storage",result.refreshState)
                    } else {state=SearchSource(adapter.id,if(usable(result.state))SourceState.UNAVAILABLE else result.state,if(usable(result.state))"storage_budget" else result.error);if(result.state==SourceState.RATE_LIMITED)break}
                }
                states[adapter.id]=state
                emission.lock()
                try {send(DiscoverSearch(id,local(query,filter),adapters.mapNotNull {states[it.id]},candidates(query,filter)))} finally {emission.unlock()}
            } }
        }
    }
    private suspend fun candidates(query:String,filter:SearchFilter)=when(val access=store.access {it.candidates(query,filter)}){is CatalogueAccess.Available->access.value;else->emptyList()}
    suspend fun candidate(id:String):CatalogueCandidate?=when(val access=store.access {repo->repo.states("candidate:",200).mapNotNull {runCatching {candidateFromJson(it.value)}.getOrNull()}.firstOrNull {it.id==id}}){is CatalogueAccess.Available->access.value;else->null}
    suspend fun resolveCandidate(candidate:CatalogueCandidate):String? {
        if(!permitted())return null
        if(candidate.provider=="wikidata") {
            val wd=WikidataAdapter();val observed=client.fetch(wd,wd.lookup(candidate.externalId))
            if(!usable(observed.state))return null
            val cross=observed.records.singleOrNull()?.fields?.singleOrNull {it.field=="verified_musicbrainz_identity"}?.value?.split('/') ?: return null
            if(cross.size!=2)return null
            val mb=MusicBrainzAdapter();val typed=client.fetch(mb,mb.lookup(cross[0],cross[1]))
            if(!usable(typed.state))return null
            store.access {repo->repo.ingest(typed.copy(state=SourceState.FRESH));repo.ingest(observed.copy(state=SourceState.FRESH))}
            return (store.access {it.providerIdentity("musicbrainz",cross[0],cross[1])?.entityId} as? CatalogueAccess.Available)?.value
        }
        if(candidate.provider!="musicbrainz")return null
        val adapter=MusicBrainzAdapter()
        val result=client.fetch(adapter,adapter.lookup(candidate.providerKind,candidate.externalId))
        if(!usable(result.state))return null
        store.access {it.ingest(result.copy(state=SourceState.FRESH))}
        return (store.access {it.providerIdentity(candidate.provider,candidate.providerKind,candidate.externalId)?.entityId} as? CatalogueAccess.Available)?.value
    }
    suspend fun refreshMetadata(detail:DiscoverDetail):SearchSource {
        val identity=detail.identities.firstOrNull {it.provider=="musicbrainz"} ?: return SearchSource("musicbrainz",SourceState.MISSING)
        if(!permitted())return SearchSource("musicbrainz",SourceState.UNAVAILABLE,"network_policy")
        val adapter=MusicBrainzAdapter();val result=client.fetch(adapter,adapter.lookup(identity.providerKind,identity.externalId),force=true)
        val stored=if(usable(result.state))store.access {it.ingest(result.copy(state=SourceState.FRESH))} else null
        return SearchSource("musicbrainz",if(stored is CatalogueAccess.Unavailable)SourceState.UNAVAILABLE else result.state,result.error)
    }
    suspend fun supplement(detail:DiscoverDetail,language:String):List<SearchSource> {
        require(language in setOf("cs","en"))
        if(!permitted())return listOf(SearchSource("wikipedia",SourceState.UNAVAILABLE,"network_policy"))
        val wiki=WikipediaAdapter();val commons=CommonsAdapter();val states=mutableListOf<SearchSource>()
        fun field(name:String,lang:String?=null)=detail.assertions.firstOrNull {row->row.assertion.field==name && (lang==null || org.json.JSONObject(row.assertion.valueJson).optString("language")==lang)}?.let {org.json.JSONObject(it.assertion.valueJson).optString("value")}
        val title=field("wikipedia_title",language) ?: field("wikipedia_title","en")
        val selectedLanguage=if(field("wikipedia_title",language)!=null)language else "en"
        if(title!=null) {
            val result=client.fetch(wiki,wiki.document(title,selectedLanguage))
            if(usable(result.state) && result.supplement.isNotEmpty()) {
                val raw=result.supplement["html"] as? String ?: ""
                // No WebView or network-capable renderer. Remove executable/style content before extraction.
                val stripped=raw.replace(Regex("(?is)<(script|style|noscript)[^>]*>.*?</\\1>"),"")
                val text=withContext(Dispatchers.Default){
                    val paragraphs=Regex("(?is)<p\\b[^>]*>.*?</p>").findAll(stripped).take(20).joinToString("\n"){it.value}
                    android.text.Html.fromHtml(paragraphs,android.text.Html.FROM_HTML_MODE_LEGACY).toString().trim().take(2000)
                }
                val revision=result.supplement["revision"].toString()
                val record=ProviderRecord("document",title,"artist",title,listOf(FieldValue("biography",text,selectedLanguage),FieldValue("article_revision",revision),FieldValue("article_revision_url","https://$selectedLanguage.wikipedia.org/w/index.php?oldid=$revision"),FieldValue("article_history","https://$selectedLanguage.wikipedia.org/w/index.php?title=${com.opus.piano.data.encoded(title)}&action=history"),FieldValue("adaptation","Plain-text excerpt; truncated to 2000 characters")))
                store.access {it.supplement(detail.entity.id,result.copy(records=listOf(record),license="CC-BY-SA-4.0",licenseUrl="https://creativecommons.org/licenses/by-sa/4.0/"))}
            }
            states+=SearchSource(wiki.id,result.state,result.error)
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
    suspend fun detail(id:String)=store.access {repo->repo.entity(id)?.let {entity->
        val relationships=repo.linked(id)
        DiscoverDetail(entity,repo.identities(id),repo.assertions(id).mapNotNull {a->val source=repo.source(a.sourceId);val rights=repo.rights(a.rightsId);if(source!=null && rights!=null)SourcedAssertion(a,source,rights) else null},relationships.mapNotNull {edge->repo.entity(if(edge.fromId==id)edge.toId else edge.fromId)?.let {edge to it}})
    }}
    companion object {const val FRESH_MILLIS=24*60*60*1000L}
}
data class SourcedAssertion(val assertion:CatalogueAssertion,val source:CatalogueSource,val rights:CatalogueRights)
data class DiscoverFilters(val artists:List<CatalogueEntity> = emptyList(),val periods:List<String> = emptyList())
data class DiscoverDetail(val entity:CatalogueEntity,val identities:List<CatalogueExternalIdentity>,val assertions:List<SourcedAssertion>,val relationships:List<Pair<CatalogueRelationship,CatalogueEntity>>)

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
