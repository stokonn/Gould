package com.opus.piano

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.opus.piano.data.catalogue.*
import com.opus.piano.data.catalogue.providers.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class Stage4GraphTest {
    private val instrumentation=InstrumentationRegistry.getInstrumentation()
    private val context=instrumentation.targetContext
    private fun id()=UUID.randomUUID().toString()
    private fun fixture()=ProviderJson.parse(instrumentation.context.assets.open("stage4/musicbrainz-arrangement.json").use {it.readBytes()})
    @Test fun searchCandidateCannotBeAddedBeforeTypedLookupAndArrangementKeepsIdentity()=runBlocking {
        val name="stage4-graph-${id()}";val db=CatalogueDatabase.builder(context,name).build();val repo=CatalogueRepository(db);val adapter=MusicBrainzAdapter()
        try {
            val external="11111111-1111-4111-8111-111111111111"
            val request=adapter.search("Authored","works","en").single()
            val searched=adapter.parse(request,mapOf("works" to listOf(mapOf("id" to external,"title" to "Authored piano arrangement")))).single()
            assertTrue(searched.classificationPending)
            repo.ingest(ProviderResult(request,SourceState.FRESH,listOf(searched),"1","1".repeat(64)))
            assertTrue(repo.searchPage("Authored").items.isEmpty())
            val observed=repo.candidates("Authored",SearchFilter()).single();assertEquals("work",observed.kindHint)
            val lookup=adapter.lookup("work",external)
            val records=adapter.parse(lookup,fixture());assertEquals("arrangement",records.first {it.externalId==external}.kind)
            assertFalse(records.toString().contains("BLOCKED"));assertFalse(records.toString().contains("secret"))
            repo.ingest(ProviderResult(lookup,SourceState.FRESH,records,"2","2".repeat(64)))
            assertEquals("arrangement",repo.entity(observed.id)!!.kind)
            assertEquals(1,repo.searchPage("Authored piano").items.size)
            val sourceExternal="22222222-2222-4222-8222-222222222222"
            val sourceRequest=adapter.lookup("work",sourceExternal)
            val source=adapter.parse(sourceRequest,mapOf("id" to sourceExternal,"title" to "Authored source work"))
            repo.ingest(ProviderResult(sourceRequest,SourceState.FRESH,source,"3","3".repeat(64)))
            val edges=repo.linked(observed.id);assertTrue(edges.any {it.type=="arrangement_source"})
            assertTrue(edges.any {it.type=="credit"})
        } finally {db.close();context.deleteDatabase(name)}
    }
    @Test fun quarantinedConflictingParentPositionCannotPromoteACompositionToPart()=runBlocking {
        val name="stage4-rejected-part-${id()}";val db=CatalogueDatabase.builder(context,name).build();val repo=CatalogueRepository(db)
        try {
            val external=List(3){id()};val records=external.mapIndexed {index,key->ProviderRecord("work",key,"work","Authored $index")}
            val request=ProviderRequest("https://musicbrainz.org/ws/2/work/${external[0]}","musicbrainz","work",external[0])
            repo.ingest(ProviderResult(request,SourceState.FRESH,records,"1","a".repeat(64)))
            val local=external.map {repo.providerIdentity("musicbrainz","work",it)!!.entityId}
            db.openHelper.writableDatabase.execSQL("UPDATE works SET workType='part' WHERE id=?",arrayOf(local[1]))
            repo.relate(CatalogueRelationship(id(),"part",local[1],"work",local[0],"work",position=0))
            fun ref(index:Int)=ProviderReference("work",external[index],"work",records[index].title)
            val changed=records[2].copy(relationships=listOf(ProviderEdge("part",ref(2),ref(0),0)))
            repo.ingest(ProviderResult(request,SourceState.FRESH,listOf(changed),"2","b".repeat(64)))
            assertEquals("composition",repo.workType(local[2]))
            assertTrue(repo.linked(local[2]).none {it.type=="part"})
            assertEquals(1,repo.states("diagnostic:").size)
            assertEquals(local[2],repo.providerIdentity("musicbrainz","work",external[2])!!.entityId)
        }finally {db.close();context.deleteDatabase(name)}
    }
    @Test fun partsOrderAndRecordingPerformanceAreDifferentEdges() {
        val adapter=MusicBrainzAdapter();val parent=id();val child=id();val recording=id()
        val root=mapOf("id" to parent,"title" to "Suite","relations" to listOf(mapOf("type" to "parts","type-id" to MusicBrainzAdapter.PARTS,"direction" to "forward","target-type" to "work","ordering-key" to 2,"attributes" to listOf("movement"),"work" to mapOf("id" to child,"title" to "Second movement"))))
        val records=adapter.parse(adapter.lookup("work",parent),root)
        val part=records.single {it.externalId==child};assertEquals("movement",part.subtype);assertFalse(part.classificationPending)
        val edge=records.flatMap {it.relationships}.single();assertEquals("part",edge.type);assertEquals(child,edge.from.externalId);assertEquals(parent,edge.to.externalId);assertEquals(2,edge.position)
        val performance=adapter.parse(adapter.lookup("recording",recording),mapOf("id" to recording,"title" to "Performance","length" to 123456,"relations" to listOf(mapOf("type" to "performance","direction" to "forward","target-type" to "work","work" to mapOf("id" to parent,"title" to "Suite")))))
        assertEquals("performance",performance.flatMap {it.relationships}.single().type)
    }
    @Test fun linkedReleaseTrackProjectionDropsUnknownNestedRightsFields() {
        val adapter=MusicBrainzAdapter();val release=id();val album=id();val recording=id()
        val records=adapter.parse(adapter.lookup("release",release),mapOf("id" to release,"title" to "Edition","release-group" to mapOf("id" to album,"title" to "Album","primary-type" to "Album"),"media" to listOf(mapOf("position" to 1,"tracks" to listOf(mapOf("position" to 2,"title" to "Track","recording" to mapOf("id" to recording,"title" to "Recording","rating" to mapOf("value" to "BLOCKED"))))))))
        assertEquals(setOf("release","album","recording"),records.map {it.kind}.toSet());assertFalse(records.toString().contains("BLOCKED"))
        assertEquals(setOf("edition","track"),records.flatMap {it.relationships}.map {it.type}.toSet())
    }
    @Test fun aliasesCreditsCatalogueNumbersAndBoundedTypoRetrieval()=runBlocking {
        val name="stage4-search-${id()}";val db=CatalogueDatabase.builder(context,name).build();val repo=CatalogueRepository(db)
        try {
            val request=ProviderRequest("https://api.openopus.org/fixture","openopus","work")
            val record=ProviderRecord("work","123","work","Goldberg Variations BWV988",aliases=listOf("cs" to "Goldbergovy variace"),credits=listOf(ProviderCredit("87","Johann Sebastian Bach","composer")))
            val entity=repo.ingest(ProviderResult(request,SourceState.FRESH,listOf(record),"1","4".repeat(64))).single()
            for(query in listOf("BWV 988","BWV988","Goldbergovy variace","Bach Goldbergovy","Goldbrg Variations"))assertEquals(query,entity,repo.searchPage(query,SearchFilter("works")).items.single().id)
        } finally {db.close();context.deleteDatabase(name)}
    }
    @Test fun streamingLinksAreBoundToRecordingAndExactPlatformDestination() {
        assertTrue(ListeningDestination.accepts("https://open.spotify.com/track/1234567890123456789012","recording"))
        assertFalse(ListeningDestination.accepts("https://open.spotify.com/track/1234567890123456789012","work"))
        assertFalse(ListeningDestination.accepts("https://open.spotify.com.attacker.example/track/1234567890123456789012","recording"))
        assertFalse(ListeningDestination.accepts("https://user:password@open.spotify.com/track/1234567890123456789012","recording"))
        assertFalse(ListeningDestination.accepts("http://open.spotify.com/track/1234567890123456789012","recording"))
    }
    @Test fun newRelationshipUsesItsOwnObservationRatherThanTheOriginalIdentitySource()=runBlocking {
        val name="stage4-edge-source-${id()}";val db=CatalogueDatabase.builder(context,name).build();val repo=CatalogueRepository(db)
        try {
            val recording=ProviderRecord("recording",id(),"recording","Authored recording")
            val work=ProviderRecord("work",id(),"work","Authored work")
            val request=ProviderRequest("https://musicbrainz.org/ws/2/recording/${recording.externalId}","musicbrainz","recording",recording.externalId)
            repo.ingest(ProviderResult(request,SourceState.FRESH,listOf(recording,work),"1","a".repeat(64)))
            val edge=ProviderEdge("performance",ProviderReference("recording",recording.externalId,"recording",recording.title),ProviderReference("work",work.externalId,"work",work.title))
            repo.ingest(ProviderResult(request,SourceState.FRESH,listOf(recording.copy(relationships=listOf(edge))),"2","b".repeat(64)))
            val local=repo.providerIdentity("musicbrainz","recording",recording.externalId)!!
            val accepted=repo.linked(local.entityId).single {it.type=="performance"}
            assertEquals("b".repeat(64),repo.source(accepted.sourceId!!)!!.payloadSha256)
            assertEquals("a".repeat(64),repo.source(local.sourceId!!)!!.payloadSha256)
        }finally {db.close();context.deleteDatabase(name)}
    }
}
