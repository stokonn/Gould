package com.opus.piano.data.catalogue

import androidx.room.withTransaction
import org.json.JSONObject

/** Bounded disposable unresolved observations only. Never accepts personal/identity/source keys.
 * Immutable accepted evidence, entity UUIDs and graph rows are outside this eviction path. */
internal class CatalogueObservationCache(private val db:CatalogueDatabase) {
    private val dao=db.dao()
    companion object {val prefixes=setOf("candidate:","pending-edge:","diagnostic:")}
    suspend fun put(prefix:String,key:String,value:String,now:Long=System.currentTimeMillis(),maxEntries:Int=200)=db.withTransaction {
        require(prefix in prefixes && key.startsWith(prefix) && key.length<=1024 && value.toByteArray().size<=32768 && maxEntries in 1..200)
        dao.state(CatalogueState(key,value))
        touch(key,now)
        var excess=dao.stateCount(prefix+"%")-maxEntries
        var removed=0
        while(excess>0) {
            val victims=dao.oldestStates(prefix+"%",minOf(excess,64))
            check(victims.isNotEmpty())
            for(row in victims){dao.removeState(row.key);dao.removeState("access:"+row.key)}
            excess-=victims.size;removed+=victims.size
        }
        if(removed>0) {
            val summaryKey="cache-evictions:$prefix"
            val previous=dao.state(summaryKey)?.value?.let {runCatching {JSONObject(it).optLong("count")}.getOrDefault(0)} ?: 0L
            dao.state(CatalogueState(summaryKey,JSONObject().put("count",previous.coerceIn(0,Long.MAX_VALUE-removed)+removed).put("at",now).toString()))
        }
    }
    suspend fun touch(key:String,now:Long=System.currentTimeMillis()) {
        require(prefixes.any {key.startsWith(it)})
        val clock="observation-access-clock"
        val previous=dao.state(clock)?.value?.toLongOrNull() ?: 0L
        val stamp=maxOf(now.coerceAtLeast(0),if(previous==Long.MAX_VALUE)previous else previous+1)
        dao.state(CatalogueState("access:$key",stamp.toString().padStart(19,'0')))
        dao.state(CatalogueState(clock,stamp.toString()))
    }
}
