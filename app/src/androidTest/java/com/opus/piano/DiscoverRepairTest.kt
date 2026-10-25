package com.opus.piano

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.data.catalogue.*
import com.opus.piano.data.catalogue.providers.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.toList
import androidx.room.Room
import com.opus.piano.data.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

/** Disposable catalogue fixtures; existing personal history and provider evidence stay untouched. */
@RunWith(AndroidJUnit4::class)
class DiscoverRepairTest {
    private val context=InstrumentationRegistry.getInstrumentation().targetContext
    private fun id()=UUID.randomUUID().toString()

    @Test fun composerSurnameOutranksLooseNamesAndPaginationKeepsRankAndIdentity()=runBlocking {
        val name="discover-repair-${id()}";val db=CatalogueDatabase.builder(context,name).build();val repo=CatalogueRepository(db)
        try {
            val adapter=OpenOpusAdapter();val request=adapter.search("Chopin","all","en").single()
            val composer=ProviderRecord("composer","152","artist","Frédéric Chopin",listOf(FieldValue("epoch","Romantic")),subtype="person")
            val imported=repo.ingest(ProviderResult(request,SourceState.FRESH,listOf(composer),"1","a".repeat(64))).single()
            for(title in listOf("Chopin University Press","Chopin Themes","Chopin Died")) {
                val key=id();repo.add(CatalogueEntity(key,"artist",title),CatalogueArtist(key,artistType="group"))
            }
            val first=repo.searchPage("Chopin",SearchFilter("artists"),limit=1)
            assertEquals(imported,first.items.single().id)
            assertEquals("composer",first.rowInfo[imported]!!.artistType)
            assertEquals(listOf("openopus"),first.rowInfo[imported]!!.providers)
            val remaining=repo.searchPage("Chopin",SearchFilter("artists"),cursor=first.nextCursor,limit=40)
            assertEquals(3,remaining.items.size);assertFalse(remaining.items.any {it.id==imported})
            assertTrue(repo.searchPage("Chopin",SearchFilter("works")).items.isEmpty())
            val title="Goldberg Variations"
            val a=id();val b=id()
            repo.add(CatalogueEntity(a,"work",title),CatalogueWork(a));repo.add(CatalogueEntity(b,"work",title),CatalogueWork(b))
            assertEquals(setOf(a,b),repo.searchPage(title,SearchFilter("works")).items.map {it.id}.toSet())
        } finally {db.close();context.deleteDatabase(name)}
    }

    @Test fun debounceConsentCancellationRankingAndProviderOriginsStayBounded()=runBlocking {
        val personalName="discover-personal-${id()}";val catalogueName="discover-catalogue-${id()}"
        val personal=Room.databaseBuilder(context,OpusDatabase::class.java,personalName)
            .addMigrations(*OpusDatabase.ALL_MIGRATIONS).addCallback(PersonalCatalogueBridge.callback).addCallback(BackupState.callback).build()
        val store=LocalCatalogueStore(context,personal,catalogueName)
        var consent=true;var calls=0
        val adapter=object:ProviderAdapter by OpenOpusAdapter(){override val intervalMillis=0L}
        val client=ProviderClient(ProviderTransport {
            calls++
            ProviderResponse(200,"""{"status":{"success":"true"},"results":[{"composer":{"id":"152","complete_name":"Frédéric Chopin","epoch":"Romantic"}}]}""".toByteArray())
        },{consent})
        val repository=DiscoverRepository(store,client,{consent},adapters=listOf(adapter))
        try {
            repository.automaticSearch("Chopin",SearchFilter("artists"),"en",false).toList();assertEquals(0,calls)
            repository.automaticSearch(" ",SearchFilter(),"en",true).toList()
            repository.automaticSearch("x",SearchFilter(),"en",true).toList();assertEquals(0,calls)
            val obsolete=launch {repository.automaticSearch("obsolete",SearchFilter(),"en",true).toList()}
            delay(150);obsolete.cancelAndJoin();assertEquals(0,calls)
            val localId=id();store.access {it.add(CatalogueEntity(localId,"artist","Chopin University Press"),CatalogueArtist(localId,artistType="group"))}
            val results=repository.automaticSearch("Chopin",SearchFilter("artists"),"en",true).toList()
            assertEquals(1,calls);assertEquals(localId,results.first().page.items.single().id)
            assertEquals("Frédéric Chopin",results.last().page.items.first().title)
            val onlineId=results.last().page.items.first().id
            assertEquals(setOf(onlineId),results.last().providerEntityIds)
            assertEquals(2,results.last().page.items.map {it.id}.distinct().size)
            consent=false
            val offline=repository.automaticSearch("Chopin",SearchFilter("artists"),"en",true).toList().last()
            assertEquals(1,calls);assertEquals(2,offline.page.items.size)
            assertEquals("network_policy",offline.sources.single().error)
        } finally {store.close();personal.close();context.deleteDatabase(personalName);context.deleteDatabase(catalogueName)}
    }

    @Test fun invalidResponseDiagnosticsIdentifyLayerAndPreserveCachedResultsWithoutRetry()=runBlocking {
        val adapter=object:ProviderAdapter by OpenOpusAdapter(){override val intervalMillis=0L}
        val request=adapter.search("fixture","works","en").single()
        val good="""{"status":{"success":"true"},"results":[{"work":{"id":"1","title":"Authored work"}}]}""".toByteArray()
        val bad=listOf("not json" to "invalid_json","{}" to "invalid_provider_schema","""{"status":{"success":"true"},"results":[],"next":"20"}""" to "invalid_pagination")
        for((body,expected) in bad) {
            var calls=0
            val client=ProviderClient(ProviderTransport {calls++;ProviderResponse(200,body.toByteArray())},{true})
            val failure=client.fetch(adapter,request)
            assertEquals(SourceState.INVALID,failure.state);assertEquals(expected,failure.error);assertEquals(1,calls)
        }
        val directory=File(context.cacheDir,"discover-repair-${id()}")
        try {
            val cache=FileProviderCache(directory);var calls=0
            ProviderClient(ProviderTransport {ProviderResponse(200,good,mapOf("Cache-Control" to "max-age=0"))},{true},cache=cache).fetch(adapter,request)
            val client=ProviderClient(ProviderTransport {calls++;ProviderResponse(200,"{}".toByteArray())},{true},cache=cache)
            val stale=client.fetch(adapter,request)
            assertEquals(SourceState.STALE,stale.state);assertEquals(SourceState.INVALID,stale.refreshState)
            assertEquals("Authored work",stale.records.single().title);assertEquals(1,calls)
            val offline=ProviderClient(ProviderTransport {error("Offline must not request")},{false},cache=cache).fetch(adapter,request)
            assertEquals("Authored work",offline.records.single().title)
        } finally {directory.deleteRecursively()}
        var calls=0
        val empty=ProviderClient(ProviderTransport {calls++;ProviderResponse(200,"""{"status":{"success":"false","error":"Nothing found"}}""".toByteArray())},{true}).fetch(adapter,request)
        assertEquals(SourceState.FRESH,empty.state);assertTrue(empty.records.isEmpty());assertEquals(1,calls)
    }
}
