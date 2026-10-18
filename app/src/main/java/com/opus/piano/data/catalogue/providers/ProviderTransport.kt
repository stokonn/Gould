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
    fun parse(bytes:ByteArray):Map<String,Any?> {
        require(bytes.size<=MAX_BYTES)
        var count=0
        JsonReader(InputStreamReader(ByteArrayInputStream(bytes),Charsets.UTF_8)).use { reader ->
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

class HttpsProviderTransport(private val allowed:()->Boolean) : ProviderTransport {
    override suspend fun get(request:ProviderRequest):ProviderResponse = withContext(Dispatchers.IO) {
        val url=URL(request.url)
        require(url.protocol=="https" && url.port in setOf(-1,443) && url.userInfo==null && url.host in setOf("api.openopus.org","musicbrainz.org","www.wikidata.org","en.wikipedia.org","cs.wikipedia.org","commons.wikimedia.org"))
        check(allowed())
        val connection=url.openConnection() as HttpURLConnection
        // Cancellation closes a blocking socket without waiting for readTimeout.
        val cancellation=CoroutineScope(currentCoroutineContext()).launch(Dispatchers.IO, start=CoroutineStart.UNDISPATCHED) { try { awaitCancellation() } finally { connection.disconnect() } }
        try {
            connection.connectTimeout=10000;connection.readTimeout=12000;connection.instanceFollowRedirects=false
            connection.setRequestProperty("User-Agent","Goldberg/0.11-stage4 (https://github.com/stokonn/Goldberg)")
            connection.setRequestProperty("Accept","application/json")
            connection.setRequestProperty("Accept-Encoding","identity")
            val status=connection.responseCode
            // Redirects never silently cross hosts or forward future credentials.
            if(status in 300..399)return@withContext ProviderResponse(status,byteArrayOf())
            val headers=listOf("Retry-After","ETag","Last-Modified","Cache-Control").mapNotNull { key->connection.getHeaderField(key)?.let {key to it} }.toMap()
            require(connection.contentLengthLong<=ProviderJson.MAX_BYTES)
            val out=ByteArrayOutputStream()
            val stream=if(status in 200..299)connection.inputStream else connection.errorStream
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
    private val now:()->Long=System::currentTimeMillis, private val monotonic:()->Long={System.nanoTime()/1000000}) {
    private data class Gate(val mutex:Mutex=Mutex(),var next:Long=0,var blockedUntil:Long=0)
    private val gates=ConcurrentHashMap<String,Gate>()
    private val flights=Mutex()
    private val inFlight=mutableMapOf<String,CompletableDeferred<ProviderResult>>()
    suspend fun fetch(adapter:ProviderAdapter,request:ProviderRequest):ProviderResult {
        val (pending,owner)=flights.withLock {inFlight[request.url]?.let {it to false} ?: (CompletableDeferred<ProviderResult>().also {inFlight[request.url]=it} to true)}
        if(!owner)return pending.await()
        try {val result=execute(adapter,request);pending.complete(result);return result}
        catch(e:Throwable){pending.completeExceptionally(e);throw e}
        finally {flights.withLock {inFlight.remove(request.url)}}
    }
    private suspend fun execute(adapter:ProviderAdapter,request:ProviderRequest):ProviderResult {
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
                    val response=withTimeout(18000){transport.get(request)}
                    val root=if(response.code in 200..299)ProviderJson.parse(response.body) else emptyMap()
                    val failure=ProviderErrors.classify(response.code,root)
                    val retry=ProviderErrors.retryAfter(response.headers["Retry-After"],now())
                    if(failure==SourceState.RATE_LIMITED) {
                        val wait=maxOf(retry,5000L+(Math.random()*1000).toLong())
                        gate.blockedUntil=if(wait>Long.MAX_VALUE-monotonic())Long.MAX_VALUE else monotonic()+wait
                        return@withLock ProviderResult(request,failure,retryAfterMillis=wait,error="overload")
                    }
                    if(failure!=null)return@withLock ProviderResult(request,failure,error="http_${response.code}")
                    val hash=sha(response.body)
                    return@withLock ProviderResult(request,SourceState.FRESH,adapter.parse(request,root),hash,hash,
                        candidates=if(adapter is WikidataAdapter && request.kind=="candidate")adapter.observations(root) else emptyList(),
                        supplement=when(adapter){is WikipediaAdapter->adapter.project(root);is CommonsAdapter->adapter.project(root);else->emptyMap()})
                } catch(e:TimeoutCancellationException){last=ProviderResult(request,SourceState.UNAVAILABLE,error="timeout")}
                catch(e:CancellationException){throw e}
                catch(_:IllegalArgumentException){return@withLock ProviderResult(request,SourceState.INVALID,error="invalid_payload")}
                catch(_:Exception){last=ProviderResult(request,SourceState.UNAVAILABLE,error="transport")}
                if(attempt==0)delay(maxOf(adapter.intervalMillis,500L)+(Math.random()*500).toLong())
            }
            last!!
        }
    }
}
