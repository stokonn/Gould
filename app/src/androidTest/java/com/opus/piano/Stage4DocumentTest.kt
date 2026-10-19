package com.opus.piano

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.data.*
import com.opus.piano.data.catalogue.*
import com.opus.piano.data.catalogue.providers.*
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/** Authored public-document shapes, never live requests or certified real identities. */
@RunWith(AndroidJUnit4::class)
class Stage4DocumentTest {
    private val context=InstrumentationRegistry.getInstrumentation().targetContext
    private fun id()=UUID.randomUUID().toString()
    private suspend fun environment(block:suspend (LocalCatalogueStore,DiscoverDetail)->Unit) {
        val name="stage4-documents-${id()}"
        val personal=Room.inMemoryDatabaseBuilder(context,OpusDatabase::class.java).addCallback(PersonalCatalogueBridge.callback).build()
        val store=LocalCatalogueStore(context,personal,name)
        try {
            val artist=CatalogueEntity(id(),"artist","Authored original name")
            store.access {it.add(artist,CatalogueArtist(artist.id))}
            val source=CatalogueSource(id(),"wikidata","entity:Q123","1","a".repeat(64),"2026-10-19T00:00:00Z","https://www.wikidata.org/wiki/Q123")
            val rights=CatalogueRights(id(),"CC0-1.0","https://creativecommons.org/publicdomain/zero/1.0/","Authored metadata fixture","approved")
            val fields=listOf("cs" to "Authored Czech title","en" to "Authored English title").map {(language,title)->
                SourcedAssertion(CatalogueAssertion(id(),artist.id,"wikipedia_title",JSONObject().put("value",title).put("language",language).toString(),source.id,rights.id),source,rights)
            }
            block(store,DiscoverDetail(artist,emptyList(),fields,emptyList()))
        }finally {store.close();personal.close();context.deleteDatabase(name)}
    }
    private fun document()=ProviderResponse(200,JSONObject().put("title","Authored English title").put("html","<script>private script marker</script><p>Authored sourced biography.</p>").put("latest",JSONObject().put("id",42).put("timestamp","2026-10-19T00:00:00Z")).put("license",JSONObject().put("url","https://creativecommons.org/licenses/by-sa/4.0/")).toString().toByteArray())
    @Test fun missingRequestedDocumentFallsBackOnlyToVerifiedEnglishTitleWithItsOwnRevision()=runBlocking {
        environment {store,detail->
            val requests=mutableListOf<String>();val repository=DiscoverRepository(store,ProviderClient(ProviderTransport {r->requests+=r.url;if(r.url.startsWith("https://cs."))ProviderResponse(404,byteArrayOf()) else document()},{true}),{true})
            val states=repository.supplement(detail,"cs")
            assertEquals(2,requests.size);assertEquals(listOf("document:cs","document:en"),states.map {it.requestKind})
            assertEquals(listOf(SourceState.MISSING,SourceState.FRESH),states.map {it.state})
            val loaded=(repository.detail(detail.entity.id) as CatalogueAccess.Available).value!!
            fun value(field:String)=JSONObject(loaded.assertions.single {it.assertion.field==field}.assertion.valueJson).getString("value")
            val biography=JSONObject(loaded.assertions.single {it.assertion.field=="biography"}.assertion.valueJson)
            assertEquals("en",biography.getString("language"));assertEquals("Authored sourced biography.",biography.getString("value"))
            assertEquals("requested_language_missing",value("document_fallback_reason"));assertEquals("cs",value("document_requested_language"))
            assertEquals("42",value("article_revision"));assertEquals("https://en.wikipedia.org/w/index.php?oldid=42",value("article_revision_url"))
            assertEquals("Authored original name",loaded.entity.title)
            assertTrue(loaded.assertions.all {it.source.sourceUrl.startsWith("https://en.wikipedia.org/") && it.rights.license=="CC-BY-SA-4.0"})
        }
    }
    @Test fun overloadStopsBeforeEnglishAndOfflineNeverSendsAnArticleRequest()=runBlocking {
        environment {store,detail->
            var calls=0;val repository=DiscoverRepository(store,ProviderClient(ProviderTransport {calls++;ProviderResponse(503,byteArrayOf(),mapOf("Retry-After" to "60"))},{true}),{true})
            assertEquals(SourceState.RATE_LIMITED,repository.supplement(detail,"cs").single().state);assertEquals(1,calls)
            val offline=DiscoverRepository(store,ProviderClient(ProviderTransport {calls++;error("No offline request")},{false}),{false})
            assertEquals(SourceState.UNAVAILABLE,offline.supplement(detail,"cs").single().state);assertEquals(1,calls)
            assertTrue(((repository.detail(detail.entity.id) as CatalogueAccess.Available).value!!).assertions.isEmpty())
        }
    }
    @Test fun missingSelectedLanguageTitleUsesVerifiedFallbackWithoutInventingTranslation()=runBlocking {
        environment {store,detail->
            var calls=0;val repository=DiscoverRepository(store,ProviderClient(ProviderTransport {r->calls++;assertTrue(r.url.startsWith("https://en."));document()},{true}),{true})
            repository.supplement(detail.copy(assertions=detail.assertions.filter {JSONObject(it.assertion.valueJson).getString("language")=="en"}),"cs")
            assertEquals(1,calls)
            val loaded=(repository.detail(detail.entity.id) as CatalogueAccess.Available).value!!
            assertEquals("requested_language_title_missing",JSONObject(loaded.assertions.single {it.assertion.field=="document_fallback_reason"}.assertion.valueJson).getString("value"))
        }
    }
    @Test fun unapprovedOrUnsupportedTitleEvidenceCannotTriggerOnlineContentLookup()=runBlocking {
        environment {store,detail->
            val repository=DiscoverRepository(store,ProviderClient(ProviderTransport {error("Unapproved evidence must never trigger requests")},{true}),{true})
            val unapproved=detail.copy(assertions=detail.assertions.map {it.copy(rights=it.rights.copy(reviewState="pending"))})
            assertTrue(repository.supplement(unapproved,"cs").isEmpty())
            val unsupported=detail.copy(assertions=detail.assertions.map {it.copy(source=it.source.copy(provider="unknown"))})
            assertTrue(repository.supplement(unsupported,"cs").isEmpty())
        }
    }
    @Test fun detailExposesActualMovementSubtypeWithoutChangingItsIdentity()=runBlocking {
        environment {store,_->
            val work=CatalogueEntity(id(),"work","Authored movement")
            store.access {it.add(work,CatalogueWork(work.id,workType="movement"))}
            val repository=DiscoverRepository(store,ProviderClient(ProviderTransport {error("No detail network request")},{false}),{false})
            val loaded=(repository.detail(work.id) as CatalogueAccess.Available).value!!
            assertEquals("movement",loaded.workType);assertEquals(work,loaded.entity)
        }
    }
}
