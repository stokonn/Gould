package com.opus.piano.data.catalogue.providers

import android.util.JsonReader
import android.util.JsonToken
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.io.ByteArrayOutputStream
import java.util.concurrent.ConcurrentHashMap

fun interface ProviderTransport { suspend fun get(request:ProviderRequest):ProviderResponse }

/** Strict bounded JSON, including depth/count/string limits before creating provider objects. */
object ProviderJson {
    fun parse(bytes:ByteArray):Map<String,Any?> = try {decode(bytes)}
        catch(e:java.io.IOException){throw IllegalArgumentException("Invalid provider JSON encoding or syntax",e)}
        catch(e:IllegalStateException){throw IllegalArgumentException("Invalid provider JSON structure",e)}
    private fun decode(bytes:ByteArray):Map<String,Any?> {
        require(bytes.size<=MAX_BYTES)
        var count=0
        JsonReader(InputStreamReader(ByteArrayInputStream(bytes),Charsets.UTF_8.newDecoder().onMalformedInput(java.nio.charset.CodingErrorAction.REPORT).onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT))).use { reader ->
            reader.isLenient=false
            fun value(depth:Int):Any? {
                require(depth<=32 && ++count<=100000)
                fun text(s:String):String {require(s.length<=MAX_BYTES);return s}
                return when(reader.peek()) {
                    JsonToken.BEGIN_OBJECT -> {reader.beginObject();val map=linkedMapOf<String,Any?>();while(reader.hasNext()){val k=text(reader.nextName());require(!map.containsKey(k));map[k]=value(depth+1)};reader.endObject();map}
                    JsonToken.BEGIN_ARRAY -> {reader.beginArray();val list=mutableListOf<Any?>();while(reader.hasNext())list+=value(depth+1);reader.endArray();list}
                    JsonToken.STRING -> text(reader.nextString())
                    JsonToken.NUMBER -> reader.nextString().let {it.toLongOrNull() ?: it.toDouble().also {n->require(n.isFinite())}}
                    JsonToken.BOOLEAN -> reader.nextBoolean()
                    JsonToken.NULL -> {reader.nextNull();null}
                    else -> error("Invalid provider JSON")
                }
            }
            @Suppress("UNCHECKED_CAST")
            val root=value(0) as? Map<String,Any?> ?: error("Expected object")
            require(reader.peek()==JsonToken.END_DOCUMENT);return root
        }
    }
    const val MAX_BYTES=2*1024*1024
}

class HttpsProviderTransport(private val connect:(URL)->HttpURLConnection={it.openConnection() as HttpURLConnection}, private val allowed:()->Boolean) : ProviderTransport {
    override suspend fun get(request:ProviderRequest):ProviderResponse = withContext(Dispatchers.IO) {
        val url=URL(request.url)
        require(url.protocol=="https" && url.port in setOf(-1,443) && url.userInfo==null && url.host in setOf("api.openopus.org","musicbrainz.org","www.wikidata.org","en.wikipedia.org","cs.wikipedia.org","commons.wikimedia.org"))
        check(allowed())
        val connection=connect(url)
        // Cancellation closes a blocking socket without waiting for readTimeout.
        val cancellation=CoroutineScope(currentCoroutineContext()).launch(Dispatchers.IO, start=CoroutineStart.UNDISPATCHED) { try { awaitCancellation() } finally { connection.disconnect() } }
        try {
            connection.connectTimeout=10000;connection.readTimeout=12000;connection.instanceFollowRedirects=false
            connection.setRequestProperty("User-Agent","Goldberg/0.11-stage4 (https://github.com/stokonn/Goldberg)")
            connection.setRequestProperty("Accept","application/json")
            connection.setRequestProperty("Accept-Encoding","identity")
            request.headers.filterKeys {it in setOf("If-None-Match","If-Modified-Since")}.forEach { (name,value)->
                require(value.length<=512 && !value.contains('\r') && !value.contains('\n'));connection.setRequestProperty(name,value)
            }
            val status=connection.responseCode
            // Redirects never silently cross hosts or forward future credentials.
            if(status in 300..399 && status!=304)return@withContext ProviderResponse(status,byteArrayOf())
            val headers=listOf("Retry-After","ETag","Last-Modified","Cache-Control").mapNotNull { key->connection.getHeaderField(key)?.let {key to it} }.toMap()
            require(connection.contentLengthLong<=ProviderJson.MAX_BYTES)
            val out=ByteArrayOutputStream()
            val stream=if(status==304)null else if(status in 200..299)connection.inputStream else connection.errorStream
            stream?.use { input ->
                val buffer=ByteArray(8192)
                while(true){currentCoroutineContext().ensureActive();check(allowed());val n=input.read(buffer);if(n<0)break;require(out.size()+n<=ProviderJson.MAX_BYTES);out.write(buffer,0,n)}
            }
            ProviderResponse(status,out.toByteArray(),headers)
        } finally {cancellation.cancel();connection.disconnect()}
    }
}

/** One scheduler per app. Different sources are independent; no periodic/background work. */
class ProviderClient(private val transport:ProviderTransport, private val permitted:()->Boolean,
    private val now:()->Long=System::currentTimeMillis, private val monotonic:()->Long={System.nanoTime()/1000000}, private val cache:ProviderCache?=null) {
    private data class Gate(val mutex:Mutex=Mutex(),var next:Long=0,var blockedUntil:Long=0,var overloads:Int=0)
    private val gates=ConcurrentHashMap<String,Gate>()
    private val flights=Mutex()
    private val inFlight=mutableMapOf<String,CompletableDeferred<ProviderResult>>()
    suspend fun fetch(adapter:ProviderAdapter,request:ProviderRequest,force:Boolean=false):ProviderResult=coordinated(adapter,request,force,true)
    private suspend fun coordinated(adapter:ProviderAdapter,request:ProviderRequest,force:Boolean,rejoin:Boolean):ProviderResult {
        require(adapter.id==request.provider)
        val saved=try {cache?.read(request)}catch(e:CancellationException){throw e}catch(_:Exception){null}
        if(!force && saved!=null && saved.expiresAt>now())return saved.result.copy(request=request,state=SourceState.CACHED)
        if(!permitted())return saved?.result?.copy(request=request,state=SourceState.STALE,error="network_policy",refreshState=SourceState.UNAVAILABLE) ?: ProviderResult(request,SourceState.UNAVAILABLE,error="network_policy")
        val (pending,owner)=flights.withLock {inFlight[request.url]?.let {it to false} ?: (CompletableDeferred<ProviderResult>().also {inFlight[request.url]=it} to true)}
        if(!owner)return try {pending.await()}catch(e:CancellationException) {
            currentCoroutineContext().ensureActive()
            flights.withLock {if(inFlight[request.url]===pending)inFlight.remove(request.url)}
            if(rejoin)coordinated(adapter,request,force,false)
            else saved?.result?.copy(request=request,state=SourceState.STALE,error="cancelled_shared_owner",refreshState=SourceState.UNAVAILABLE)
                ?: ProviderResult(request,SourceState.UNAVAILABLE,error="cancelled_shared_owner")
        }
        try {val result=withContext(Dispatchers.IO){execute(adapter,request,saved)}
            val output=if(result.state !in setOf(SourceState.FRESH,SourceState.CACHED) && saved!=null)
                saved.result.copy(request=request,state=SourceState.STALE,error=result.error,retryAfterMillis=result.retryAfterMillis,refreshState=result.state) else result
            pending.complete(output);return output}
        catch(e:Throwable){pending.completeExceptionally(e);throw e}
        finally {withContext(NonCancellable) {flights.withLock {if(inFlight[request.url]===pending)inFlight.remove(request.url)}}}
    }
    suspend fun clearCache(){cache?.clear()}
    private suspend fun execute(adapter:ProviderAdapter,request:ProviderRequest,saved:CachedProviderValue?):ProviderResult {
        val gate=gates.getOrPut(adapter.id){Gate()}
        return gate.mutex.withLock {
            if(!permitted())return@withLock ProviderResult(request,SourceState.UNAVAILABLE,error="network_policy")
            if(gate.blockedUntil>monotonic())return@withLock ProviderResult(request,SourceState.RATE_LIMITED,retryAfterMillis=gate.blockedUntil-monotonic(),error="provider_backoff")
            delay((gate.next-monotonic()).coerceAtLeast(0))
            var last:ProviderResult?=null
            repeat(2) { attempt ->
                currentCoroutineContext().ensureActive()
                if(!permitted())return@withLock ProviderResult(request,SourceState.UNAVAILABLE,error="network_policy")
                gate.next=monotonic()+adapter.intervalMillis
                try {
                    val conditional=request.copy(headers=if(saved?.etag!=null)mapOf("If-None-Match" to saved.etag) else if(saved?.lastModified!=null)mapOf("If-Modified-Since" to saved.lastModified) else emptyMap())
                    val response=withTimeout(18000){transport.get(conditional)}
                    fun header(key:String)=response.headers.entries.firstOrNull {it.key.equals(key,true)}?.value
                    val cacheControl=header("Cache-Control").orEmpty().lowercase(java.util.Locale.ROOT)
                    val freshness=if(cacheControl.contains("no-cache"))0L else Regex("(?:^|,)\\s*max-age=([0-9]+)").find(cacheControl)?.groupValues?.get(1)?.toLongOrNull()?.coerceAtMost(86400)?.times(1000) ?: 86400000L
                    if(response.code==304) {
                        gate.overloads=0
                        if(saved==null || conditional.headers.isEmpty())return@withLock ProviderResult(request,SourceState.INVALID,error="unexpected_not_modified")
                        try {if(cacheControl.contains("no-store"))cache?.remove(request) else cache?.write(request,saved.copy(expiresAt=now()+freshness))}catch(e:CancellationException){throw e}catch(_:Exception){}
                        return@withLock saved.result.copy(request=request,state=SourceState.CACHED)
                    }
                    val root=if(response.code in 200..299)ProviderJson.parse(response.body) else emptyMap()
                    val failure=ProviderErrors.classify(response.code,root)
                    val retry=ProviderErrors.retryAfter(header("Retry-After"),now())
                    if(failure==SourceState.RATE_LIMITED) {
                        gate.overloads=(gate.overloads+1).coerceAtMost(8)
                        val wait=maxOf(retry,5000L*(1L shl (gate.overloads-1))+(Math.random()*1000).toLong())
                        gate.blockedUntil=if(wait>Long.MAX_VALUE-monotonic())Long.MAX_VALUE else monotonic()+wait
                        return@withLock ProviderResult(request,failure,retryAfterMillis=wait,error="overload")
                    }
                    if(failure!=null)return@withLock ProviderResult(request,failure,error="http_${response.code}")
                    if(adapter is WikidataAdapter && request.externalId!=null && root.obj("entities").obj(request.externalId).containsKey("missing"))
                        return@withLock ProviderResult(request,SourceState.MISSING,error="missing_entity")
                    gate.overloads=0
                    val hash=sha(response.body)
                    val result=ProviderResult(request,SourceState.FRESH,adapter.parse(request,root),hash,hash,
                        nextOffset=adapter.nextOffset(request,root),
                        candidates=if(adapter is WikidataAdapter && request.kind=="candidate")adapter.observations(root) else emptyList(),
                        supplement=when(adapter){is WikipediaAdapter->adapter.project(root);is CommonsAdapter->adapter.project(root);else->emptyMap()},
                        license=if(adapter is WikipediaAdapter)"CC-BY-SA-4.0" else if(adapter is CommonsAdapter)"per-file" else "CC0-1.0",
                        licenseUrl=if(adapter is WikipediaAdapter)"https://creativecommons.org/licenses/by-sa/4.0/" else if(adapter is CommonsAdapter)"https://commons.wikimedia.org/wiki/Commons:Licensing" else "https://creativecommons.org/publicdomain/zero/1.0/")
                    try {if(cacheControl.contains("no-store"))cache?.remove(request) else cache?.write(request,CachedProviderValue(result,now()+freshness,header("ETag"),header("Last-Modified")))}catch(e:CancellationException){throw e}catch(_:Exception){ /* Optional cache failure cannot hide valid source data. */ }
                    return@withLock result
                } catch(e:TimeoutCancellationException){last=ProviderResult(request,SourceState.UNAVAILABLE,error="timeout")}
                catch(e:CancellationException){throw e}
                catch(_:BlockedRightsException){return@withLock ProviderResult(request,SourceState.BLOCKED_RIGHTS,error="rights_not_verified")}
                catch(_:IllegalArgumentException){return@withLock ProviderResult(request,SourceState.INVALID,error="invalid_payload")}
                catch(_:Exception){last=ProviderResult(request,SourceState.UNAVAILABLE,error="transport")}
                if(attempt==0)delay(maxOf(adapter.intervalMillis,500L)+(Math.random()*500).toLong())
            }
            last!!
        }
    }
}
