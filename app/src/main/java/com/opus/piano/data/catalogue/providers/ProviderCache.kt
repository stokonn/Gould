package com.opus.piano.data.catalogue.providers

import android.util.AtomicFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** Cache files contain only adapter-projected public data, never raw requests/query strings. */
data class CachedProviderValue(val result:ProviderResult,val expiresAt:Long,val etag:String?,val lastModified:String?)
interface ProviderCache {
    suspend fun read(request:ProviderRequest):CachedProviderValue?
    suspend fun write(request:ProviderRequest,value:CachedProviderValue)
    suspend fun remove(request:ProviderRequest)
    suspend fun clear()
}
class FileProviderCache(private val directory:File,private val now:()->Long=System::currentTimeMillis,
    private val byteBudget:Long=32L*1024*1024,private val maxEntries:Int=200):ProviderCache {
    private val mutex=Mutex()
    private val accessFile=AtomicFile(File(directory,"access.json"))
    private var accessTimes:MutableMap<String,Long>?=null
    private fun accesses():MutableMap<String,Long> {
        accessTimes?.let {return it}
        val loaded=mutableMapOf<String,Long>()
        try {if(accessFile.baseFile.exists()){
            require(accessFile.baseFile.length()<=65536)
            val o=JSONObject(accessFile.openRead().use {it.bufferedReader().readText()})
            val keys=o.keys();while(keys.hasNext()){val k=keys.next();if(k.matches(Regex("[a-f0-9]{64}\\.json")))loaded[k]=o.getLong(k)}
        }}catch(_:Exception){accessFile.delete()}
        accessTimes=loaded;return loaded
    }
    private fun persistAccess() {
        val map=accesses()
        if(map.isEmpty()){accessFile.delete();return}
        val stream=accessFile.startWrite()
        try {stream.write(JSONObject(map.toMap()).toString().toByteArray());accessFile.finishWrite(stream)}catch(e:Exception){accessFile.failWrite(stream);throw e}
    }
    private fun touch(file:File) {
        val map=accesses();val previous=map.values.maxOrNull() ?: 0L
        map[file.name]=maxOf(now(),if(previous==Long.MAX_VALUE)previous else previous+1)
        persistAccess()
    }
    private fun accessed(file:File)=accesses()[file.name] ?: file.lastModified()
    private fun file(request:ProviderRequest)=File(directory,sha(request.url.toByteArray())+".json")
    override suspend fun read(request:ProviderRequest):CachedProviderValue?=withContext(Dispatchers.IO) {mutex.withLock {
        val atomic=AtomicFile(file(request))
        if(!atomic.baseFile.exists())return@withLock null
        try {
            require(atomic.baseFile.length() in 1..ProviderJson.MAX_BYTES.toLong())
            val envelope=JSONObject(atomic.openRead().use {it.readBytes().also {b->require(b.size<=ProviderJson.MAX_BYTES)}.toString(Charsets.UTF_8)})
            require(envelope.getInt("version")==2)
            val raw=envelope.getString("payload");require(sha(raw.toByteArray())==envelope.getString("sha256"))
            val value=CacheProjection.decode(request,JSONObject(raw))
            require(now()-accessed(atomic.baseFile)<=30L*86400000)
            touch(atomic.baseFile);value
        }catch(_:Exception){atomic.delete();null}
    }}
    override suspend fun write(request:ProviderRequest,value:CachedProviderValue)=withContext(Dispatchers.IO) {mutex.withLock {
        require(value.result.state==SourceState.FRESH)
        directory.mkdirs()
        val payload=CacheProjection.encode(value).toString()
        val bytes=JSONObject().put("version",2).put("payload",payload).put("sha256",sha(payload.toByteArray())).toString().toByteArray()
        if(bytes.size>ProviderJson.MAX_BYTES || directory.usableSpace-bytes.size<128L*1024*1024)return@withLock
        val atomic=AtomicFile(file(request));val stream=atomic.startWrite()
        try {stream.write(bytes);atomic.finishWrite(stream);touch(atomic.baseFile)}catch(e:Exception){atomic.failWrite(stream);throw e}
        trim()
    }}
    private fun trim() {
        val files=owned().sortedWith(compareBy<File> {accessed(it)}.thenBy {it.name})
        var bytes=files.sumOf {it.length()};var count=files.size
        for(file in files)if(bytes>byteBudget || count>maxEntries || now()-accessed(file)>30L*86400000){val length=file.length();if(file.delete()){bytes-=length;count--;accesses().remove(file.name)}}
        persistAccess()
    }
    private fun owned()=directory.listFiles()?.filter {it.isFile && it.name.matches(Regex("[0-9a-f]{64}\\.json"))}.orEmpty()
    override suspend fun remove(request:ProviderRequest)=withContext(Dispatchers.IO){mutex.withLock {val f=file(request);AtomicFile(f).delete();accesses().remove(f.name);persistAccess()}}
    override suspend fun clear()=withContext(Dispatchers.IO) {mutex.withLock {owned().forEach {AtomicFile(it).delete()};accesses().clear();accessFile.delete()}}
    suspend fun bytes()=withContext(Dispatchers.IO){mutex.withLock {owned().sumOf {it.length()}}}
}
internal object CacheProjection {
    private fun ref(r:ProviderReference)=JSONObject().put("providerKind",r.providerKind).put("externalId",r.externalId).put("kind",r.kind).put("title",r.title).put("subtype",r.subtype)
    private fun reference(o:JSONObject)=ProviderReference(o.getString("providerKind"),o.getString("externalId"),o.getString("kind"),o.getString("title"),o.optString("subtype"))
    fun encode(value:CachedProviderValue):JSONObject {
        val result=value.result
        val records=result.records.map {record->JSONObject().put("providerKind",record.providerKind).put("externalId",record.externalId).put("kind",record.kind).put("title",record.title)
            .put("fields",JSONArray(record.fields.map {JSONObject().put("field",it.field).put("value",it.value).put("language",it.language)}))
            .put("aliases",JSONArray(record.aliases.map {JSONObject().put("language",it.first).put("value",it.second)}))
            .put("subtype",record.subtype).put("classificationPending",record.classificationPending)
            .put("credits",JSONArray(record.credits.map {JSONObject().put("id",it.id).put("name",it.name).put("role",it.role)}))
            .put("relationships",JSONArray(record.relationships.map {JSONObject().put("type",it.type).put("from",ref(it.from)).put("to",ref(it.to)).put("position",it.position).put("medium",it.medium).put("role",it.role)}))}
        return JSONObject().put("provider",result.request.provider).put("records",JSONArray(records)).put("revision",result.revision).put("hash",result.hash).put("observedAt",result.observedAt)
            .put("expiresAt",value.expiresAt).put("etag",value.etag ?: JSONObject.NULL).put("lastModified",value.lastModified ?: JSONObject.NULL)
            .put("license",result.license).put("licenseUrl",result.licenseUrl).put("candidates",JSONArray(result.candidates.map {JSONObject().put("id",it.id).put("title",it.title).put("description",it.description)}))
            .put("supplement",JSONObject(result.supplement))
    }
    fun decode(request:ProviderRequest,o:JSONObject):CachedProviderValue {
        require(o.getString("provider")==request.provider && o.getString("hash").matches(Regex("[0-9a-f]{64}")))
        fun rows(a:JSONArray):List<JSONObject> {require(a.length()<=200);return (0 until a.length()).map {a.getJSONObject(it)}}
        val records=rows(o.getJSONArray("records")).map {r->ProviderRecord(r.getString("providerKind"),r.getString("externalId"),r.getString("kind"),r.getString("title"),
            rows(r.getJSONArray("fields")).map {FieldValue(it.getString("field"),it.getString("value"),it.getString("language"))},
            rows(r.getJSONArray("aliases")).map {it.getString("language") to it.getString("value")},r.getString("subtype"),rows(r.getJSONArray("credits")).map {ProviderCredit(it.getString("id"),it.getString("name"),it.getString("role"))},r.getBoolean("classificationPending"),
            rows(r.getJSONArray("relationships")).map {ProviderEdge(it.getString("type"),reference(it.getJSONObject("from")),reference(it.getJSONObject("to")),it.getInt("position"),it.getInt("medium"),it.getString("role"))})}
        val supplement=ProviderJson.parse(o.getJSONObject("supplement").toString().toByteArray())
        val result=ProviderResult(request,SourceState.FRESH,records,o.getString("revision"),o.getString("hash"),o.getString("observedAt"),license=o.getString("license"),licenseUrl=o.getString("licenseUrl"),
            candidates=rows(o.getJSONArray("candidates")).map {ProviderCandidate(it.getString("id"),it.getString("title"),it.getString("description"))},supplement=supplement)
        return CachedProviderValue(result,o.getLong("expiresAt"),if(o.isNull("etag"))null else o.getString("etag"),if(o.isNull("lastModified"))null else o.getString("lastModified"))
    }
}
