package com.opus.piano

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.data.*
import com.opus.piano.data.catalogue.*
import com.opus.piano.data.catalogue.providers.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.toList
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class Stage4PagingTest {
    private val context=InstrumentationRegistry.getInstrumentation().targetContext
    private fun id()=UUID.randomUUID().toString()
    private suspend fun environment(block:suspend (LocalCatalogueStore)->Unit) {
        val name="stage4-pages-${id()}";val personal=Room.inMemoryDatabaseBuilder(context,OpusDatabase::class.java).addCallback(PersonalCatalogueBridge.callback).build()
        val store=LocalCatalogueStore(context,personal,name)
        try {block(store)}finally {store.close();personal.close();context.deleteDatabase(name)}
    }
    private val adapter=object:ProviderAdapter by OpenOpusAdapter() {override val intervalMillis=0L}
    private fun response(offset:Int):ProviderResponse {
        val count=if(offset<40)20 else 3
        val rows=JSONArray((1..count).map {n->JSONObject().put("work",JSONObject().put("id",(offset+n).toString()).put("title","Authored work ${offset+n}"))})
        return ProviderResponse(200,JSONObject().put("status",JSONObject().put("success","true")).put("results",rows).put("next",if(offset<40)offset+20 else JSONObject.NULL).toString().toByteArray())
    }
    @Test fun providerContinuationReplaysWithoutDuplicatesAndRejectsOtherQueryLanguageCategoryOrGeneration()=runBlocking {
        environment {store->
            val calls=AtomicInteger();val client=ProviderClient(ProviderTransport {r->calls.incrementAndGet();response(r.offset)},{true})
            val repository=DiscoverRepository(store,client,{true},adapters=listOf(adapter))
            val first=repository.search("Authored",SearchFilter("works"),"en",true).toList().last()
            assertEquals(20,first.page.items.size);assertNotNull(first.continuation);assertEquals(1,calls.get())
            val cursor=first.continuation!!
            for(action in listOf<suspend ()->Unit>(
                {repository.continueSearch("Other",SearchFilter("works"),"en",true,cursor)},
                {repository.continueSearch("Authored",SearchFilter("artists"),"en",true,cursor)},
                {repository.continueSearch("Authored",SearchFilter("works"),"cs",true,cursor)},
                {repository.continueSearch("Authored",SearchFilter("works"),"en",false,cursor)}))assertTrue(runCatching {action()}.isFailure)
            assertEquals(1,calls.get())
            val second=repository.continueSearch("Authored",SearchFilter("works"),"en",true,cursor)
            assertEquals(20,second.page.items.size);assertEquals(2,calls.get())
            assertEquals(second,repository.continueSearch("Authored",SearchFilter("works"),"en",true,cursor));assertEquals(2,calls.get())
            assertTrue(first.page.items.map {it.id}.intersect(second.page.items.map {it.id}.toSet()).isEmpty())
            val third=repository.continueSearch("Authored",SearchFilter("works"),"en",true,second.continuation!!)
            assertEquals(3,third.page.items.size);assertNull(third.continuation);assertEquals(3,calls.get())
            assertTrue(runCatching {repository.continueSearch("Authored",SearchFilter("works"),"en",true,cursor)}.isFailure)
            assertEquals(3,calls.get());assertTrue(repository.landing().items.isNotEmpty())
        }
    }
    @Test fun localPagingAndConcurrentContinuationStayBoundedAndNetworkFree()=runBlocking {
        environment {store->
            store.access {repo->repeat(65){val key=id();repo.add(CatalogueEntity(key,"work","Authored $it"),CatalogueWork(key))}}
            val calls=AtomicInteger();val repository=DiscoverRepository(store,ProviderClient(ProviderTransport {calls.incrementAndGet();error("Unexpected network")},{true}),{true})
            val first=repository.search("Authored",SearchFilter(),"en",false).toList().last()
            val cursor=first.continuation!!;assertEquals(40,first.page.items.size)
            val results=coroutineScope {List(2){async {repository.continueSearch("Authored",SearchFilter(),"en",false,cursor)}}.awaitAll()}
            assertEquals(results[0],results[1]);assertEquals(25,results[0].page.items.size);assertNull(results[0].continuation);assertEquals(0,calls.get())
            assertTrue(first.page.items.map {it.id}.intersect(results[0].page.items.map {it.id}.toSet()).isEmpty())
        }
    }
    @Test fun externalGenerationChangeRejectsBeforeSendingProviderQuery()=runBlocking {
        environment {store->
            val calls=AtomicInteger();val repository=DiscoverRepository(store,ProviderClient(ProviderTransport {calls.incrementAndGet();response(it.offset)},{true}),{true},adapters=listOf(adapter))
            val first=repository.search("Authored",SearchFilter(),"en",true).toList().last()
            store.access {it.state("generation",id())}
            assertTrue(runCatching {repository.continueSearch("Authored",SearchFilter(),"en",true,first.continuation!!)}.isFailure)
            assertEquals(1,calls.get())
        }
    }
    @Test fun revokedNetworkPolicyNeverSendsContinuation()=runBlocking {
        environment {store->
            var allowed=true;val calls=AtomicInteger()
            val repository=DiscoverRepository(store,ProviderClient(ProviderTransport {calls.incrementAndGet();response(it.offset)},{allowed}),{allowed},adapters=listOf(adapter))
            val first=repository.search("Authored",SearchFilter(),"en",true).toList().last();allowed=false
            repository.continueSearch("Authored",SearchFilter(),"en",true,first.continuation!!)
            assertEquals(1,calls.get());assertTrue(repository.landing().items.isNotEmpty())
        }
    }
    @Test fun opaquePagingStateExpiresAndEvictsOnlyTransientSessions()=runBlocking {
        var time=0L;val paging=DiscoverPaging {time}
        fun frame(query:String)=DiscoverFrame(query,id(),"generation",null,emptySet(),emptySet(),emptyList(),emptyList())
        val old=paging.issue(frame("old"))!!
        repeat(8){paging.issue(frame("other-$it"))}
        assertTrue(runCatching {paging.get(old,"old")}.isFailure)
        val last=paging.issue(frame("last"))!!;time+=15*60*1000+1
        assertTrue(runCatching {paging.get(last,"last")}.isFailure)
        assertNull(paging.issue(frame("limit").copy(ordinal=12)))
        assertNull(paging.issue(frame("limit").copy(seen=List(500){id()}.toSet())))
    }
}
