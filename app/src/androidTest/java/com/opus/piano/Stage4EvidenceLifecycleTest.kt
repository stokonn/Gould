package com.opus.piano

import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.data.*
import com.opus.piano.data.catalogue.*
import com.opus.piano.data.catalogue.providers.*
import kotlinx.coroutines.*
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/** Authored identities only. No provider calls or personal-database mutation. */
@RunWith(AndroidJUnit4::class)
class Stage4EvidenceLifecycleTest {
    private val context=InstrumentationRegistry.getInstrumentation().targetContext
    private fun id()=UUID.randomUUID().toString()
    private fun result(provider:String,record:ProviderRecord,revision:String="1",hash:String=SearchText.hash(record.toString()+revision))=
        ProviderResult(ProviderRequest("https://fixture.invalid/$provider/${record.externalId}",provider,record.providerKind,record.externalId),SourceState.FRESH,listOf(record),revision,hash)
    private fun music(external:String,kind:String="artist")=ProviderRecord(kind,external,kind,"Authored $external")
    private fun wikidata(external:String,music:String,kind:String="artist")=ProviderRecord("entity",external,kind,"Authored Q title",listOf(FieldValue("verified_musicbrainz_identity","$kind/$music")))
    private suspend fun database(block:suspend (CatalogueDatabase,CatalogueRepository)->Unit) {
        val name="stage4-evidence-${id()}";val db=CatalogueDatabase.builder(context,name).build()
        try {block(db,CatalogueRepository(db))}finally {db.close();context.deleteDatabase(name)}
    }
    @Test fun reverseArtistIntakeConfirmsTheSameIdentityWithoutTitleMatching()=runBlocking {
        database {_,repo->
            val external=id();val q=wikidata("Q123",external)
            val accepted=repo.ingest(result("wikidata",q)).single()
            assertEquals(accepted,repo.ingest(result("musicbrainz",music(external))).single())
            val namesake=repo.ingest(result("musicbrainz",music(id()).copy(title=q.title))).single()
            assertNotEquals(accepted,namesake)
            assertEquals(accepted,repo.providerIdentity("wikidata","entity","Q123")!!.entityId)
        }
    }
    @Test fun pendingWorkCanBeReplayedAfterItsTypedIdentityArrives()=runBlocking {
        database {_,repo->
            val external=id();val observation=result("wikidata",wikidata("Q123",external,"work"))
            assertTrue(repo.ingest(observation).isEmpty())
            val observed=repo.candidates("Authored",SearchFilter()).single()
            val accepted=repo.ingest(result("musicbrainz",music(external,"work"))).single()
            assertEquals(listOf(accepted),repo.ingest(observation))
            assertEquals(accepted,repo.providerIdentity("wikidata","entity","Q123")!!.entityId)
            assertNull(repo.state("candidate:wikidata:entity:Q123"))
            assertNull(repo.entity(observed.id))
        }
    }
    @Test fun contradictoryClaimRetainsEvidenceAndNeverMergesPreviouslyDistinctArtists()=runBlocking {
        database {db,repo->
            val first=id();val second=id()
            val a=repo.ingest(result("musicbrainz",music(first))).single()
            val b=repo.ingest(result("musicbrainz",music(second))).single()
            repo.ingest(result("wikidata",wikidata("Q123",first)))
            repo.ingest(result("wikidata",wikidata("Q124",second)))
            val contradictoryEdge=ProviderEdge("credit",ProviderReference("entity","Q123","artist","Authored"),ProviderReference("entity","Q124","artist","Authored"),role="composer")
            val revised=result("wikidata",wikidata("Q123",second).copy(title="Conflicting incoming title",relationships=listOf(contradictoryEdge)),"2")
            assertTrue(repo.ingest(revised).isEmpty());assertTrue(repo.ingest(revised).isEmpty())
            assertEquals(a,repo.providerIdentity("wikidata","entity","Q123")!!.entityId)
            assertEquals(b,repo.providerIdentity("musicbrainz","artist",second)!!.entityId)
            val evidence=repo.assertions(a).single {it.field=="identity_conflict"}
            assertFalse(evidence.selected);assertTrue(evidence.valueJson.contains(second))
            assertEquals(revised.hash,repo.source(evidence.sourceId)!!.payloadSha256)
            assertTrue(repo.linked(a).none {it.type=="credit" && it.toId==b})
            repeat(205){repo.boundedState("diagnostic:","diagnostic:$it","{}")}
            assertNotNull(repo.source(evidence.sourceId));assertEquals(evidence,repo.assertions(a).single {it.field=="identity_conflict"})
            assertEquals(2,db.openHelper.readableDatabase.query("SELECT COUNT(*) FROM identity_revisions WHERE provider='musicbrainz'").use {it.moveToFirst();it.getInt(0)})
        }
    }
    @Test fun assertionsKeepAllLanguagesAndValuesAndRemainImmutableAfterReopen()=runBlocking {
        val name="stage4-evidence-${id()}";var db=CatalogueDatabase.builder(context,name).build()
        try {
            val record=music(id()).copy(fields=listOf(FieldValue("preferred_name","Český název","cs"),FieldValue("preferred_name","English name","en"),FieldValue("preferred_name","Another English name","en")))
            var repo=CatalogueRepository(db);val response=result("musicbrainz",record)
            val accepted=repo.ingest(response).single()
            repeat(3){repo.ingest(response)}
            assertEquals(3,repo.assertions(accepted).count {it.field=="preferred_name"})
            val original=repo.assertions(accepted)
            db.close();db=CatalogueDatabase.builder(context,name).build();repo=CatalogueRepository(db)
            for(sql in listOf("DELETE FROM assertions","UPDATE assertions SET valueJson='{}'")) {
                var rejected=false
                try {db.openHelper.writableDatabase.execSQL(sql)}catch(_:android.database.sqlite.SQLiteException){rejected=true}
                assertTrue(sql,rejected)
            }
            assertEquals(original,repo.assertions(accepted))
        }finally {db.close();context.deleteDatabase(name)}
    }
    @Test fun identicalDocumentBodiesInDifferentEditionsAndEntitiesRetainTheirOwnSources()=runBlocking {
        database {_,repo->
            val artists=List(2){repo.ingest(result("musicbrainz",music(id()))).single()}
            for(artist in artists)for(language in listOf("cs","en")) {
                val request=ProviderRequest("https://$language.wikipedia.org/w/rest.php/v1/page/Authored/with_html","wikipedia","document","Authored")
                val record=ProviderRecord("document","Authored","artist","Authored",listOf(FieldValue("biography","Same authored text",language),FieldValue("article_revision","42")))
                val document=ProviderResult(request,SourceState.FRESH,listOf(record),"42","a".repeat(64),license="CC-BY-SA-4.0",licenseUrl="https://creativecommons.org/licenses/by-sa/4.0/")
                repeat(2){repo.supplement(artist,document)}
            }
            for(artist in artists) {
                val biographies=repo.assertions(artist).filter {it.field=="biography"}
                assertEquals(setOf("cs","en"),biographies.map {JSONObject(it.valueJson).getString("language")}.toSet())
                assertEquals(2,biographies.size)
                for(b in biographies)assertTrue(repo.source(b.sourceId)!!.sourceUrl.startsWith("https://"+JSONObject(b.valueJson).getString("language")+"."))
            }
        }
    }
    @Test fun concurrentReplayIsIdempotentAndCancelledTransactionCannotPublishEvidence()=runBlocking {
        database {db,repo->
            val response=result("musicbrainz",music(id()))
            val ids=coroutineScope {List(8){async(Dispatchers.IO){repo.ingest(response).single()}}.awaitAll()}
            assertEquals(1,ids.toSet().size);assertEquals(1,repo.assertions(ids.first()).size)
            val entered=CompletableDeferred<Unit>();val hold=CompletableDeferred<Unit>()
            val cancelled=launch(Dispatchers.IO) {
                db.withTransaction {
                    repo.ingest(result("musicbrainz",music("cancelled-id")))
                    entered.complete(Unit);hold.await()
                }
            }
            entered.await();cancelled.cancelAndJoin()
            assertNull(repo.providerIdentity("musicbrainz","artist","cancelled-id"))
            assertEquals(1,db.openHelper.readableDatabase.query("SELECT COUNT(*) FROM sources").use {it.moveToFirst();it.getInt(0)})
        }
    }
    @Test fun rebuildRetainsAcceptedAssertionsIdentityRevisionsAndRedirectReversal()=runBlocking {
        val name="stage4-rebuild-${id()}"
        val personal=Room.inMemoryDatabaseBuilder(context,OpusDatabase::class.java).addCallback(PersonalCatalogueBridge.callback).build()
        val store=LocalCatalogueStore(context,personal,name)
        try {
            val original=(store.access {repo->
                val a=repo.ingest(result("musicbrainz",music(id()))).single()
                val b=repo.ingest(result("musicbrainz",music(id()))).single()
                repo.redirect(CatalogueRedirect(a,1,b,"artist","Authored merge","Authored explicit evidence","test"))
                repo.redirect(CatalogueRedirect(a,2,null,"artist","Authored reversal","Authored correction","test"))
                a to repo.assertions(a)
            } as CatalogueAccess.Available).value
            assertEquals(CatalogueAccess.Available(Unit),store.rebuild())
            val restored=store.access {repo->
                assertEquals(original.second,repo.assertions(original.first))
                assertEquals(original.first,repo.resolve(original.first)!!.id)
                assertEquals(1,repo.identities(original.first).size)
                assertEquals(original.first,repo.searchPage("Authored",SearchFilter("artists")).items.first {it.id==original.first}.id)
            }
            assertTrue(restored is CatalogueAccess.Available)
        }finally {store.close();personal.close();context.deleteDatabase(name)}
    }
    @Test fun activeRedirectsAffectSearchAndDetailAndReversalInvalidatesOldCursors()=runBlocking {
        val name="stage4-redirect-${id()}"
        val personal=Room.inMemoryDatabaseBuilder(context,OpusDatabase::class.java).addCallback(PersonalCatalogueBridge.callback).build()
        val store=LocalCatalogueStore(context,personal,name)
        try {
            val keys=(store.access {repo->List(3){repo.ingest(result("musicbrainz",music(id()))).single()}} as CatalogueAccess.Available).value
            val a=keys[0];val b=keys[1]
            val cursor=(store.access {it.searchPage("Authored",limit=1).nextCursor} as CatalogueAccess.Available).value!!
            store.access {it.redirect(CatalogueRedirect(a,1,b,"artist","Authored merge","Explicit authored evidence","fixture"))}
            val search=(store.access {it.searchPage("Authored").items.map {row->row.id}} as CatalogueAccess.Available).value
            assertFalse(a in search);assertTrue(b in search)
            val discover=DiscoverRepository(store,ProviderClient(ProviderTransport {error("Local redirects cannot request network")},{false}),{false})
            assertEquals(b,(discover.detail(a) as CatalogueAccess.Available).value!!.entity.id)
            assertTrue(store.access {it.searchPage("Authored",cursor=cursor)} is CatalogueAccess.Rejected)
            store.access {it.redirect(CatalogueRedirect(a,2,null,"artist","Authored reversal","Explicit correction","fixture"))}
            assertEquals(a,(discover.detail(a) as CatalogueAccess.Available).value!!.entity.id)
            assertEquals(3,(store.access {it.searchPage("Authored").items.size} as CatalogueAccess.Available).value)
        }finally {store.close();personal.close();context.deleteDatabase(name)}
    }
}
