package com.opus.piano

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.data.catalogue.providers.*
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class Stage4ProviderCacheTest {
    private val context=InstrumentationRegistry.getInstrumentation().targetContext
    private val adapter=OpenOpusAdapter()
    private val request=adapter.search("raw-private-query-marker","works","en").single()
    private val body="""{"status":{"success":"true"},"results":[{"work":{"id":"1","title":"Authored fixture"}}]}""".toByteArray()
    private fun directory()=File(context.cacheDir,"stage4-test-${UUID.randomUUID()}").apply {mkdirs()}
    @Test fun waitingSuccessorCanTakeOverCancelledOwnerExactlyOnce()=runBlocking {
        val started=CompletableDeferred<Unit>();val calls=AtomicInteger()
        val client=ProviderClient(ProviderTransport {
            if(calls.incrementAndGet()==1){started.complete(Unit);awaitCancellation()} else ProviderResponse(200,body)
        },{true})
        val owner=launch {client.fetch(adapter,request)};started.await()
        val successor=async {client.fetch(adapter,request)}
        yield();delay(50);owner.cancelAndJoin()
        assertEquals(SourceState.FRESH,withTimeout(5000){successor.await()}.state)
        assertEquals(2,calls.get())
    }
    @Test fun legacyCacheForcesUnconditionalRefreshAndRejectsUnsolicitedNotModified()=runBlocking {
        val directory=directory();val cache=FileProviderCache(directory);var calls=0
        val initial=ProviderClient(ProviderTransport {ProviderResponse(200,body,mapOf("ETag" to "legacy-etag","Cache-Control" to "max-age=3600"))},{true},cache=cache)
        try {
            initial.fetch(adapter,request)
            val file=directory.listFiles()!!.single {it.name.matches(Regex("[0-9a-f]{64}\\.json"))}
            val envelope=org.json.JSONObject(file.readText());val payload=org.json.JSONObject(envelope.getString("payload"))
            payload.remove("nextOffset");val raw=payload.toString()
            envelope.put("version",2).put("payload",raw).put("sha256",sha(raw.toByteArray()));file.writeText(envelope.toString())
            val reopened=FileProviderCache(directory)
            val offline=ProviderClient(ProviderTransport {error("No offline request")},{false},cache=reopened)
            assertEquals(SourceState.STALE,offline.fetch(adapter,request).state)
            val refresh=ProviderClient(ProviderTransport {r->calls++;assertTrue(r.headers.isEmpty());ProviderResponse(304,byteArrayOf())},{true},cache=reopened)
            val rejected=refresh.fetch(adapter,request)
            assertEquals(SourceState.STALE,rejected.state);assertEquals(SourceState.INVALID,rejected.refreshState)
            assertEquals("unexpected_not_modified",rejected.error);assertEquals(1,calls)
            assertEquals("Authored fixture",rejected.records.single().title)
            val empty=ProviderClient(ProviderTransport {ProviderResponse(304,byteArrayOf())},{true})
            assertEquals(SourceState.INVALID,empty.fetch(adapter,request).state)
        }finally {directory.deleteRecursively()}
    }
    @Test fun projectedCacheIsPersistentPrivateBoundedAndConditionallyRevalidated()=runBlocking {
        val directory=directory();var now=100000L;val cache=FileProviderCache(directory,{now})
        var calls=0;var conditional:String?=null
        val transport=ProviderTransport {r->calls++;conditional=r.headers["If-None-Match"];if(calls==1)ProviderResponse(200,body,mapOf("ETag" to "fixture-etag","Cache-Control" to "max-age=2")) else ProviderResponse(304,byteArrayOf(),mapOf("Cache-Control" to "max-age=60"))}
        val client=ProviderClient(transport,{true},now={now},monotonic={now},cache=cache)
        try {
            assertEquals(SourceState.FRESH,client.fetch(adapter,request).state)
            assertEquals(SourceState.CACHED,client.fetch(adapter,request).state);assertEquals(1,calls)
            val persisted=directory.listFiles()!!.joinToString {it.readText()};assertFalse(persisted.contains("raw-private-query-marker"))
            now+=3000
            val response=client.fetch(adapter,request);assertEquals(SourceState.CACHED,response.state);assertEquals("fixture-etag",conditional);assertEquals(2,calls)
            val reopened=FileProviderCache(directory,{now})
            val offline=ProviderClient(ProviderTransport {error("No offline request")},{false},now={now},cache=reopened)
            assertEquals(SourceState.CACHED,offline.fetch(adapter,request).state)
            now+=61000
            assertEquals(SourceState.STALE,offline.fetch(adapter,request).state)
            assertEquals("Authored fixture",offline.fetch(adapter,request).records.single().title)
        } finally {directory.deleteRecursively()}
    }
    @Test fun failedRefreshRetainsAcceptedRevisionAndHonorsBackoff()=runBlocking {
        val directory=directory();var now=100000L;var calls=0
        val cache=FileProviderCache(directory,{now})
        val client=ProviderClient(ProviderTransport {calls++;if(calls==1)ProviderResponse(200,body,mapOf("Cache-Control" to "max-age=0")) else ProviderResponse(503,byteArrayOf(),mapOf("retry-after" to "60"))},{true},now={now},monotonic={now},cache=cache)
        try {
            val accepted=client.fetch(adapter,request);now+=2000
            val stale=client.fetch(adapter,request);assertEquals(SourceState.STALE,stale.state);assertEquals(SourceState.RATE_LIMITED,stale.refreshState);assertEquals(accepted.hash,stale.hash)
            val next=client.fetch(adapter,request);assertEquals(SourceState.STALE,next.state);assertEquals(2,calls)
            assertEquals(60000,next.retryAfterMillis)
        } finally {directory.deleteRecursively()}
    }
    @Test fun corruptCacheIsDisposableAndDoesNotHideValidProviderData()=runBlocking {
        val directory=directory();val cache=FileProviderCache(directory);var calls=0
        val client=ProviderClient(ProviderTransport {calls++;ProviderResponse(200,body)},{true},cache=cache)
        try {
            client.fetch(adapter,request);directory.listFiles()!!.single {it.name.matches(Regex("[0-9a-f]{64}\\.json"))}.writeText("not valid JSON")
            assertEquals(SourceState.FRESH,client.fetch(adapter,request).state);assertEquals(2,calls)
            cache.clear();assertEquals(0L,cache.bytes())
        } finally {directory.deleteRecursively()}
    }
    @Test fun concurrentCallsCoalesceAndCancelledOwnerDoesNotPoisonLaterRequests()=runBlocking {
        val started=CompletableDeferred<Unit>();val release=CompletableDeferred<Unit>();val calls=AtomicInteger()
        val client=ProviderClient(ProviderTransport {calls.incrementAndGet();started.complete(Unit);release.await();ProviderResponse(200,body)},{true})
        val first=async {client.fetch(adapter,request)};started.await()
        val second=async {client.fetch(adapter,request)};yield();release.complete(Unit)
        assertEquals(first.await().hash,second.await().hash);assertEquals(1,calls.get())
        val entered=CompletableDeferred<Unit>();var count=0
        val cancellation=ProviderClient(ProviderTransport {if(++count==1){entered.complete(Unit);awaitCancellation()} else ProviderResponse(200,body)},{true})
        val owner=launch {cancellation.fetch(adapter,request)};entered.await();owner.cancelAndJoin()
        assertEquals(SourceState.FRESH,cancellation.fetch(adapter,request).state);assertEquals(2,count)
    }
    @Test fun noStoreDoesNotPersistAndNoCacheRequiresRevalidation()=runBlocking {
        val directory=directory();val cache=FileProviderCache(directory);var calls=0
        val client=ProviderClient(ProviderTransport {calls++;ProviderResponse(200,body,mapOf("Cache-Control" to "no-store"))},{true},cache=cache)
        try {client.fetch(adapter,request);assertEquals(0L,cache.bytes());client.fetch(adapter,request);assertEquals(2,calls)}finally {directory.deleteRecursively()}
    }
    @Test fun invalidRightsAndJsonErrorsNeverBecomeSuccessfulEmptyContent()=runBlocking {
        val wiki=WikipediaAdapter();val wikiRequest=wiki.document("Authored fixture","en")
        val client=ProviderClient(ProviderTransport {ProviderResponse(200,"{\"html\":\"<p>Fixture</p>\",\"latest\":{\"id\":1},\"license\":{\"url\":\"unknown\"}}".toByteArray())},{true})
        assertEquals(SourceState.BLOCKED_RIGHTS,client.fetch(wiki,wikiRequest).state)
        val wd=WikidataAdapter();val request=wd.lookup("Q123")
        val errors=ProviderClient(ProviderTransport {ProviderResponse(200,"{\"error\":{\"code\":\"maxlag\"}}".toByteArray())},{true})
        assertEquals(SourceState.RATE_LIMITED,errors.fetch(wd,request).state)
    }
}
