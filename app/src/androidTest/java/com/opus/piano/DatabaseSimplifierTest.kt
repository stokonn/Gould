package com.opus.piano

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.data.*
import com.opus.piano.data.catalogue.*
import com.opus.piano.data.catalogue.providers.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.toList
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/** Real SQLite and production import/read paths, with networking forbidden. */
@RunWith(AndroidJUnit4::class)
class DatabaseSimplifierTest {
    private val context=InstrumentationRegistry.getInstrumentation().targetContext
    private fun payload(count:Int=1)=JSONObject().put("status",JSONObject().put("success","true"))
        .put("composer",JSONObject().put("id","17").put("complete_name","Fixture Artist"))
        .put("works",JSONArray((1..count).map {JSONObject().put("id",it.toString()).put("title","Fixture Work $it").put("genre","Piano")})).toString()
    private fun dump(db:OpusDatabase):Map<String,List<List<String?>>> {
        val sql=db.openHelper.readableDatabase
        val tables=sql.query("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' ORDER BY name").use {c->buildList {while(c.moveToNext())add(c.getString(0))}}
        return tables.associateWith {name->sql.query("SELECT * FROM \"${name.replace("\"","\"\"")}\"").use {c->buildList {
            while(c.moveToNext())add((0 until c.columnCount).map {i->if(c.isNull(i))null else c.getString(i)})
        }}}
    }
    private suspend fun fixture(block:suspend(OpusDatabase,LocalCatalogueStore,LegacyCatalogueImport,DiscoverRepository)->Unit) {
        val id=UUID.randomUUID().toString();val personalName="simplifier-personal-$id";val catalogueName="simplifier-catalogue-$id"
        val db=Room.databaseBuilder(context,OpusDatabase::class.java,personalName)
            .addMigrations(*OpusDatabase.ALL_MIGRATIONS).addCallback(PersonalCatalogueBridge.callback).addCallback(BackupState.callback).build()
        val store=LocalCatalogueStore(context,db,catalogueName)
        val importer=LegacyCatalogueImport(db,store)
        val discover=DiscoverRepository(store,ProviderClient(ProviderTransport {error("No network permitted")},{false}),{false},
            prepareLocal={importer.synchronize();Unit})
        try {db.openHelper.writableDatabase;block(db,store,importer,discover)}
        finally {store.close();db.close();context.deleteDatabase(personalName);context.deleteDatabase(catalogueName)}
    }
    @Test fun legacyCacheAndPersonalCellsSurviveSharedOfflineReadAndIdempotentReplay()=runBlocking {fixture {db,store,importer,discover->
        db.dao().insertCatalogPiece(Piece(title="Unfinished personal work",composer="Original author",notes="Keep original notes"))
        val raw=payload();db.catalogDao().putCache(CatalogCache("work/list/composer/17/genre/all.json",raw,1000))
        val before=dump(db)
        val library=discover.library("Fixture Work", "works")
        assertEquals(1,library.items.size)
        val search=discover.search("Fixture Work",SearchFilter(category="works"),"en",false).toList().last()
        assertEquals(library.items.map {it.id},search.page.items.map {it.id})
        val detail=(discover.detail(library.items.single().id) as CatalogueAccess.Available).value!!
        assertEquals("Fixture Work 1",detail.entity.title)
        assertTrue(detail.identities.any {it.provider=="openopus" && it.externalId=="1" && it.reviewState=="reviewed"})
        assertTrue(detail.assertions.all {it.rights.license=="CC0-1.0" && it.source.payloadSha256.length==64})
        assertEquals(0,importer.synchronize())
        assertEquals(library.items.map {it.id},discover.library("Fixture Work","works").items.map {it.id})
        assertEquals(before,dump(db));assertEquals(raw,db.catalogDao().cache("work/list/composer/17/genre/all.json")!!.payload)
        assertTrue((store.access {it.identities(library.items.single().id)} as CatalogueAccess.Available).value.isNotEmpty())
    }}
    @Test fun largeLegacyResponseDoesNotLoseRowsAtAdapterPageLimit()=runBlocking {fixture {db,_,_,discover->
        db.catalogDao().putCache(CatalogCache("work/list/composer/17/genre/all.json",payload(103),2000))
        val before=dump(db);val ids=linkedSetOf<String>();var cursor:String?=null
        do {val page=discover.library("Fixture Work","works",cursor);ids.addAll(page.items.map {it.id});cursor=page.nextCursor}while(cursor!=null)
        assertEquals(103,ids.size);assertEquals(before,dump(db))
    }}
    @Test fun malformedOriginalRemainsUnchangedAndCanBeRepairedThenRetried()=runBlocking {fixture {db,_,importer,discover->
        val key="work/list/composer/17/genre/all.json"
        db.catalogDao().putCache(CatalogCache(key,"{broken",3000));val before=dump(db)
        assertEquals(0,importer.synchronize());assertEquals(before,dump(db))
        db.catalogDao().putCache(CatalogCache(key,payload(),3000))
        assertEquals(1,importer.synchronize());assertEquals(1,discover.library("Fixture Work","works").items.size)
    }}
    @Test fun normalizedMetadataWithoutResponseRemainsVisibleWithoutInventedRights()=runBlocking {fixture {db,_,_,discover->
        db.catalogDao().putComposers(listOf(CatalogComposer("44","Older Artist")))
        db.catalogDao().putWorks(listOf(CatalogWork("55","44","Older Work")))
        val before=dump(db)
        val row=discover.library("Older Work","works").items.single()
        val detail=(discover.detail(row.id) as CatalogueAccess.Available).value!!
        assertEquals("Older Work",detail.entity.title)
        assertTrue(detail.identities.all {it.reviewState=="observed"})
        assertTrue(detail.assertions.all {it.rights.license=="unknown" && it.rights.reviewState=="unreviewed"})
        assertTrue(detail.relationships.any {it.second.title=="Older Artist"})
        assertEquals(before,dump(db))
    }}
}
