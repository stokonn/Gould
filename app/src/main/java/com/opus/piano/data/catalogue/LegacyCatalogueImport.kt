package com.opus.piano.data.catalogue

import com.opus.piano.data.OpusDatabase
import com.opus.piano.data.catalogue.providers.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.Instant

/** Replays original provider cache into the shared catalogue. Personal rows and
 * original cache bytes are never changed. Interrupted imports remain retryable. */
class LegacyCatalogueImport(private val personal:OpusDatabase,private val store:LocalCatalogueStore,
    private val storage:CatalogueStorage?=null) {
    private val gate=Mutex()
    suspend fun synchronize():Int=withContext(Dispatchers.IO) {gate.withLock {
        var after="";var imported=0
        while(true) {
            val batch=personal.openHelper.readableDatabase.query("SELECT cacheKey,fetchedAt FROM catalog_cache WHERE cacheKey>? ORDER BY cacheKey LIMIT 128",arrayOf(after)).use {c->
                buildList {while(c.moveToNext())add(c.getString(0) to c.getLong(1))}
            }
            if(batch.isEmpty())break
            for((key,fetched) in batch) {
                val marker="legacy-provider-v1:"+SearchText.hash("$key:$fetched")
                if((store.access {it.state(marker)!=null} as? CatalogueAccess.Available)?.value==true)continue
                val cached=personal.catalogDao().cache(key) ?: continue
                if(cached.fetchedAt!=fetched)continue
                try {
                    val results=project(key,cached.payload.toByteArray(Charsets.UTF_8),fetched)
                    val result=store.access {repo->
                        if(storage==null)repo.importLegacyResponse(marker,results)
                        else storage.intake.commit(CatalogueStorage.MAX_INTAKE){repo.importLegacyResponse(marker,results)}
                    }
                    if(result is CatalogueAccess.Available)imported++
                }catch(e:CancellationException){throw e}
                catch(_:IllegalArgumentException){/* Retain malformed source and retry eligibility. */}
            }
            after=batch.last().first
        }
        // Response caches may have been cleared while normalized metadata was kept.
        // Preserve those rows too, explicitly as unreviewed local snapshots.
        for((table,kind) in listOf("catalog_composers" to "artist","catalog_works" to "work")) {
            var rowAfter=""
            while(true) {
                val rows=personal.openHelper.readableDatabase.query("SELECT * FROM $table WHERE id>? ORDER BY id LIMIT 128",arrayOf(rowAfter)).use {c->
                    buildList {while(c.moveToNext())add(org.json.JSONObject().also {j->
                        c.columnNames.forEachIndexed {index,name->j.put(name,if(c.isNull(index))org.json.JSONObject.NULL else c.getString(index))}
                    })}
                }
                if(rows.isEmpty())break
                for(row in rows)store.access {repo->
                    if(storage==null)repo.importLegacySnapshot(kind,row)
                    else storage.intake.commit(CatalogueStorage.MAX_INTAKE){repo.importLegacySnapshot(kind,row)}
                }
                rowAfter=rows.last().getString("id")
            }
        }
        imported
    }}
    companion object {
        /** Split before bounded adapter projection; keep the exact original-byte hash. */
        internal fun project(path:String,bytes:ByteArray,fetched:Long):List<ProviderResult> {
            require(path.matches(Regex("(?:composer/list/pop|work/list/composer/[0-9]+/genre/all|work/detail/[0-9]+|omnisearch/[^/]+/[0-9]+)\\.json")))
            require(fetched>=0)
            val root=ProviderJson.parse(bytes)
            val collection=listOf("results","works","composers").firstOrNull {root[it] is List<*>}
            val groups=if(collection!=null)(root[collection] as List<*>).chunked(40).map {root+(collection to it)}
                else if(root["work"] is Map<*,*>)listOf(root+("works" to listOf(root["work"]))) else listOf(root)
            val request=ProviderRequest("https://api.openopus.org/$path","openopus","legacy-cache")
            val hash=java.security.MessageDigest.getInstance("SHA-256").digest(bytes).joinToString(""){"%02x".format(it)}
            return groups.ifEmpty {listOf(root)}.map {group->ProviderResult(request,SourceState.FRESH,
                OpenOpusAdapter().parse(request,group),revision="legacy-cache-sha256:$hash",hash=hash,
                observedAt=Instant.ofEpochMilli(fetched).toString())}
        }
    }
}
