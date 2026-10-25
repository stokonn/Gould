package com.opus.piano.data.catalogue

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

internal data class ProviderPosition(val provider:String,val kind:String,val offset:Int)
internal data class DiscoverFrame(
    val binding:String,val queryId:String,val generation:String,val lastId:String?,
    val seen:Set<String>,val seenCandidates:Set<String>,val providers:List<ProviderPosition>,
    val sources:List<SearchSource>,val ordinal:Int=0,
    val providerEntityIds:Set<String> = emptySet(),
    val sort:String="append-relevance-uuid-v1"
)

/** Opaque process-local cursor1. Queries are never persisted. Eight active sessions, at most
 * twelve transitions each, fifteen-minute idle expiry. Frames bind source continuations and
 * local ordering/generation, not user authority or provider identity. Replay is deterministic. */
internal class DiscoverPaging(private val now:()->Long) {
    class Slot(val frame:DiscoverFrame,var touched:Long) {
        val lock=Mutex()
        var replay:DiscoverSearch?=null
    }
    private val lock=Mutex()
    private val slots=linkedMapOf<String,Slot>()
    companion object {
        const val MAX_SEEN=500
        const val MAX_PAGES=12
        fun binding(query:String,filter:SearchFilter,language:String,online:Boolean)=SearchText.hash(
            org.json.JSONArray(listOf(query.trim(),filter.category,filter.artistId,filter.period,language,online,"append-relevance-uuid-v1")).toString())
    }
    suspend fun issue(frame:DiscoverFrame):String?=lock.withLock {
        if(frame.ordinal>=MAX_PAGES || frame.seen.size>=MAX_SEEN || frame.seenCandidates.size>=MAX_SEEN)return@withLock null
        require(frame.providers.size<=8 && frame.providers.all {it.offset in 0..10000})
        frame.lastId?.let {CatalogueValidation.uuid(it)}
        val time=now();slots.entries.removeAll {time-it.value.touched>15*60*1000}
        // Twelve cursors per bounded active session; evict entire oldest sessions together.
        val sessions=slots.values.groupBy {it.frame.queryId}
        if(frame.queryId !in sessions && sessions.size>=8) {
            val oldest=sessions.minByOrNull {(_,rows)->rows.maxOf {it.touched}}!!.key
            slots.entries.removeAll {it.value.frame.queryId==oldest}
        }
        val token="discover1:"+UUID.randomUUID()
        slots[token]=Slot(frame,time);token
    }
    suspend fun get(token:String,binding:String):Slot=lock.withLock {
        require(token.length==46 && token.startsWith("discover1:"))
        val slot=requireNotNull(slots[token]) {"Expired or incompatible search cursor"}
        require(now()-slot.touched<=15*60*1000 && slot.frame.binding==binding && slot.frame.sort=="append-relevance-uuid-v1")
        slot.touched=now();slot
    }
}
