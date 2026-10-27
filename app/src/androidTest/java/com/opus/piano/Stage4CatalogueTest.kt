package com.opus.piano

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.opus.piano.data.*
import com.opus.piano.data.catalogue.*
import com.opus.piano.data.catalogue.providers.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.toList
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/** Disposable databases and authored CC0 observations; no real-world identity claims. */
@RunWith(AndroidJUnit4::class)
class Stage4CatalogueTest {
    private val context=InstrumentationRegistry.getInstrumentation().targetContext
    private fun id()=UUID.randomUUID().toString()
    private fun personal(name:String)=Room.databaseBuilder(context,OpusDatabase::class.java,name).addMigrations(*OpusDatabase.ALL_MIGRATIONS).addCallback(PersonalCatalogueBridge.callback).addCallback(BackupState.callback).build()
    @Test fun repeatedProviderImportKeepsIdentityAndContradictionsWithoutPersonalWrites()=runBlocking {
        val name="stage4-${id()}";val db=CatalogueDatabase.builder(context,name).build();val repo=CatalogueRepository(db)
        try {
            val request=ProviderRequest("https://musicbrainz.org/ws/2/work/?query=fixture","musicbrainz","work")
            val external=id();val first=ProviderResult(request,SourceState.FRESH,listOf(ProviderRecord("work",external,"work","Fixture Goldberg Variations BWV 988",aliases=listOf("cs" to "Goldbergovy variace"))),"1","a".repeat(64))
            val key=repo.ingest(first).single();assertEquals(key,repo.ingest(first).single())
            assertEquals(1,repo.assertions(key).count {it.field=="title"});assertEquals(1,repo.assertions(key).count {it.field=="alias"})
            val second=first.copy(records=listOf(first.records.single().copy(title="Conflicting revised title")),revision="2",hash="b".repeat(64))
            assertEquals(key,repo.ingest(second).single());assertEquals(first.records.single().title,repo.entity(key)!!.title)
            assertEquals(2,repo.assertions(key).count {it.field=="title"});assertEquals(1,repo.assertions(key).count {it.selected && it.field=="title"})
            assertEquals(key,repo.searchPage("Goldbergovy variace").items.single().id)
            assertEquals(key,repo.searchPage("BWV988").items.single().id)
            val other=repo.ingest(first.copy(records=listOf(first.records.single().copy(externalId=id())),hash="c".repeat(64))).single()
            assertNotEquals(key,other)
        } finally {db.close();context.deleteDatabase(name)}
    }
    @Test fun explicitCrossIdEvidenceReconcilesButNamesDoNot()=runBlocking {
        val name="stage4-${id()}";val db=CatalogueDatabase.builder(context,name).build();val repo=CatalogueRepository(db)
        try {
            val mbid=id();val a=repo.ingest(ProviderResult(ProviderRequest("https://musicbrainz.org/fixture","musicbrainz","work"),SourceState.FRESH,listOf(ProviderRecord("work",mbid,"work","Authored name")),"1","d".repeat(64))).single()
            val wd=ProviderResult(ProviderRequest("https://www.wikidata.org/fixture","wikidata","entity","Q123"),SourceState.FRESH,listOf(ProviderRecord("entity","Q123","work","Authored alternate name",listOf(FieldValue("verified_musicbrainz_identity","work/$mbid")))),"2","e".repeat(64))
            assertEquals(a,repo.ingest(wd).single());assertEquals(2,repo.identities(a).size)
            val separate=repo.ingest(wd.copy(records=listOf(wd.records.single().copy(externalId="Q124",fields=emptyList())),hash="f".repeat(64))).single()
            assertNotEquals(a,separate)
        } finally {db.close();context.deleteDatabase(name)}
    }
    @Test fun stablePagingRejectsOldGenerationAndDoesNotOverlap()=runBlocking {
        val name="stage4-${id()}";val db=CatalogueDatabase.builder(context,name).build();val repo=CatalogueRepository(db)
        try {
            repeat(45){val key=id();repo.add(CatalogueEntity(key,"work","Fixture work $it"),CatalogueWork(key))}
            val first=repo.searchPage("Fixture",limit=20);val second=repo.searchPage("Fixture",cursor=first.nextCursor,limit=20)
            assertEquals(20,first.items.size);assertTrue(first.items.map {it.id}.intersect(second.items.map {it.id}.toSet()).isEmpty())
            repo.state("generation",id());assertTrue(runCatching {repo.searchPage("Fixture",cursor=first.nextCursor)}.isFailure)
        } finally {db.close();context.deleteDatabase(name)}
    }
    @Test fun openingDiscoverAndOfflineSearchSendNothing()=runBlocking {
        val name="stage4-personal-${id()}";val db=personal(name);val cat="stage4-catalogue-${id()}";val store=LocalCatalogueStore(context,db,cat)
        var calls=0;val client=ProviderClient(ProviderTransport {calls++;error("Unexpected provider request")},{true})
        val discover=DiscoverRepository(store,client,{true})
        try {
            discover.landing();assertEquals(0,calls)
            discover.search("private title",SearchFilter(),"en",false).toList();assertEquals(0,calls)
        } finally {store.close();db.close();context.deleteDatabase(name);context.deleteDatabase(cat)}
    }
    @Test fun repertoireDuplicateAddPreservesUuidManualEditsHistoryAndRawDraft()=runBlocking {
        val name="stage4-personal-${id()}";val db=personal(name)
        try {
            db.openHelper.writableDatabase
            val external=id();val entity=CatalogueEntity(id(),"work","Authored jazz work")
            val detail=DiscoverDetail(entity,listOf(CatalogueExternalIdentity("musicbrainz","work",external,entity.id,"fixture evidence","reviewed")),emptyList(),emptyList())
            val repo=CatalogueRepertoire(db);assertNull(repo.existingPiece(detail))
            val added=repo.add(detail).single();assertTrue(added.created)
            assertEquals(added.pieceId,repo.existingPiece(detail))
            assertNull(repo.existingPiece(detail.copy(identities=listOf(detail.identities.single().copy(externalId=id())))))
            val anchor=db.personalCatalogueDao().anchors("piece",added.pieceId.toString()).single();val before=db.personalCatalogueDao().identity(anchor.identityKey)!!
            val original=db.dao().piece(added.pieceId)!!;db.dao().save(original.copy(title="My private title",composer="My creator",notes="Private notes"))
            val again=repo.add(detail).single();assertFalse(again.created);assertEquals(added.pieceId,again.pieceId)
            assertEquals(before.canonicalId,db.personalCatalogueDao().identity(anchor.identityKey)!!.canonicalId)
            assertEquals("My private title",db.dao().piece(added.pieceId)!!.title);assertEquals("Private notes",db.dao().piece(added.pieceId)!!.notes)
            assertTrue(db.dao().allSessions().isEmpty())
            assertTrue(runCatching {repo.add(detail.copy(entity=entity.copy(kind="recording")))}.isFailure)
        } finally {db.close();context.deleteDatabase(name)}
    }
    @Test fun hostileJsonDepthDuplicateKeysAndOversizeRejected() {
        assertTrue(runCatching {ProviderJson.parse("{\"a\":1,\"a\":2}".toByteArray())}.isFailure)
        assertTrue(runCatching {ProviderJson.parse(("{\"x\":"+"[".repeat(40)+"0"+"]".repeat(40)+"}").toByteArray())}.isFailure)
        assertTrue(runCatching {ProviderJson.parse(ByteArray(ProviderJson.MAX_BYTES+1))}.isFailure)
        assertEquals("Žluťoučký",ProviderJson.parse("{\"name\":\"Žluťoučký\",\"decimal\":1.25}".toByteArray())["name"])
    }
    @Test fun providerPartialSuccessAndJsonOverloadRemainIndependent()=runBlocking {
        val name="stage4-personal-${id()}";val db=personal(name);val cat="stage4-catalogue-${id()}";val store=LocalCatalogueStore(context,db,cat)
        val transport=ProviderTransport {request->when(request.provider){"openopus"->ProviderResponse(200,"{\"status\":{\"success\":\"true\"},\"results\":[{\"composer\":{\"id\":\"1\",\"complete_name\":\"Fixture artist\"},\"work\":{\"id\":\"2\",\"title\":\"Fixture song\"}}]}".toByteArray());else->ProviderResponse(200,"{\"error\":{\"code\":\"maxlag\"}}".toByteArray(),mapOf("Retry-After" to "60"))}}
        val discover=DiscoverRepository(store,ProviderClient(transport,{true}),{true},adapters=listOf(OpenOpusAdapter(),WikidataAdapter()))
        try {
            val results=discover.search("Fixture",SearchFilter(),"en",true).toList();val final=results.last()
            assertTrue(final.page.items.any {it.title=="Fixture song"});assertEquals(SourceState.RATE_LIMITED,final.sources.first {it.provider=="wikidata"}.state)
            assertTrue(discover.landing().items.isNotEmpty())
        } finally {store.close();db.close();context.deleteDatabase(name);context.deleteDatabase(cat)}
    }
}
