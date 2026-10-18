package com.opus.piano

import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.opus.piano.data.*
import com.opus.piano.data.catalogue.*
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID
import java.io.File

@RunWith(AndroidJUnit4::class)
class Stage3aCatalogueTest {
    private val inst=InstrumentationRegistry.getInstrumentation()
    private val context=inst.targetContext
    private fun id()=UUID.randomUUID().toString()
    private suspend fun rejects(block:suspend ()->Unit) { assertTrue("Must reject invalid catalogue data",runCatching { block() }.isFailure) }
    private fun cleanup(name:String) { context.getDatabasePath(name).parentFile!!.listFiles()?.filter {it.name==name || it.name.startsWith("$name.") || it.name.startsWith("$name-") }?.forEach { it.delete() } }
    @Test fun authoredMultiGenreFixtureAndTypedRelationships()=runBlocking {
        val name="stage3a-catalogue-${id()}.db";val db=CatalogueDatabase.builder(context,name).build();val repo=CatalogueRepository(db)
        try {
            val fixture=JSONObject(inst.context.assets.open("stage3a/catalogue.json").bufferedReader().use {it.readText()})
            assertTrue(fixture.getBoolean("fictional"));assertEquals("CC0-1.0",fixture.getString("license"))
            val rows=fixture.getJSONArray("entities"); val roots=mutableMapOf<String,CatalogueEntity>();val keys=mutableMapOf<String,String>()
            for(i in 0 until rows.length()) {
                val row=rows.getJSONObject(i);val id=row.getString("id");val kind=row.getString("kind");val root=CatalogueEntity(id,kind,row.getString("title")); roots[id]=root;keys[row.getString("key")]=id
                val subtype:Any=when(kind) {
                    "work"->CatalogueWork(id,workType=row.getString("workType"))
                    "arrangement"->CatalogueArrangement(id,instrumentation=row.optString("instrumentation"))
                    "artist"->CatalogueArtist(id,artistType=row.getString("artistType"))
                    "album"->CatalogueAlbum(id,albumType=row.getString("albumType"))
                    "release"->CatalogueRelease(id)
                    "recording"->CatalogueRecording(id,durationMillis=row.optLong("durationMs"))
                    else->CatalogueCollection(id)
                }
                repo.add(root,subtype)
                if(kind=="release") repo.relate(CatalogueRelationship(id(),"edition",id,kind,row.getString("albumId"),"album"))
            }
            val edges=fixture.getJSONArray("relations")
            for(i in 0 until edges.length()) {
                val e=edges.getJSONObject(i);var from=e.getString("from");var to=e.getString("to")
                val type=when(val raw=e.getString("type")) {"part"->{val tmp=from;from=to;to=tmp;"part"};"medley_source","mashup_source"->"source";"performs"->"performance";else->raw}
                repo.relate(CatalogueRelationship(e.optString("memberId").ifEmpty {id()},type,from,roots[from]!!.kind,to,roots[to]!!.kind,e.optInt("position")))
            }
            assertEquals(26,repo.page(limit=100).size)
            val aliases=fixture.getJSONArray("aliases")
            for(i in 0 until aliases.length()) {val a=aliases.getJSONObject(i);repo.addAlias(CatalogueAlias(entityId=a.getString("entityId"),language=a.getString("language"),value=a.getString("value"),normalized=a.getString("normalized")))}
            assertEquals(keys["work-classical"],repo.search("Žluťoučká sklad*").single().id)
            assertTrue(repo.search("\" OR * NEAR ( )").isEmpty())
            assertEquals(2,repo.relationships(keys["medley"]!!).size)
            assertEquals(2,repo.relationships(keys["mashup"]!!).size)
            val arrangement=keys["arr-simple"]!!; val other=keys["arr-duet"]!!
            assertNotEquals(arrangement,other)
            assertEquals(repo.relationships(arrangement).single().toId,repo.relationships(other).single().toId)
            val artist=roots.values.first {it.kind=="artist"}
            rejects {repo.relate(CatalogueRelationship(id(),"arrangement_source",arrangement,"arrangement",artist.id,"artist"))}
            rejects {repo.add(CatalogueEntity(id(),"work","Wrong subtype"),CatalogueArtist(id()))}
            rejects {repo.relate(CatalogueRelationship(id(),"member",keys["collection"]!!,"collection",artist.id,"artist",0))}
            rejects {repo.relate(CatalogueRelationship(id(),"part",keys["movement-1"]!!,"work",keys["movement-1"]!!,"work"))}
            val page=repo.page(limit=10);val second=repo.page(after=page.last().id,limit=10)
            assertTrue(page.map{it.id}.intersect(second.map{it.id}.toSet()).isEmpty())
            rejects {repo.page(limit=101)}
            db.openHelper.readableDatabase.query("PRAGMA foreign_key_check").use {assertFalse(it.moveToFirst())}
        } finally {db.close();cleanup(name)}
    }
    @Test fun reversibleEvidenceMappingAndRedirects()=runBlocking {
        val name="stage3a-mapping-${id()}.db";val db=CatalogueDatabase.builder(context,name).build();val repo=CatalogueRepository(db)
        try {
            val a=id();val b=id();val c=id();val artist=id()
            listOf(a,b,c).forEach {repo.add(CatalogueEntity(it,"work","Same name"),CatalogueWork(it,workType="medley"))}
            repo.add(CatalogueEntity(artist,"artist","Same name"),CatalogueArtist(artist))
            repo.map(CatalogueExternalIdentity("fixture-a","work","87",a,"fictional evidence","reviewed"))
            repo.map(CatalogueExternalIdentity("fixture-b","work","87",b,"different namespace","reviewed"))
            repo.map(CatalogueExternalIdentity("fixture-a","work","87",b,"reviewed correction","reviewed",2))
            repo.map(CatalogueExternalIdentity("fixture-a","work","87",a,"reviewed reversal","reviewed",3))
            rejects {repo.map(CatalogueExternalIdentity("fixture-a","work","87",artist,"wrong kind","reviewed",4))}
            repo.redirect(CatalogueRedirect(a,1,b,"work","duplicate","fictional proof","fixture reviewer"))
            assertEquals(b,repo.resolve(a)!!.id)
            rejects {repo.redirect(CatalogueRedirect(b,1,a,"work","cycle","fixture","fixture"))}
            rejects {repo.redirect(CatalogueRedirect(b,1,artist,"work","wrong type","fixture","fixture"))}
            repo.redirect(CatalogueRedirect(a,2,null,"work","reverse","preserved evidence","fixture reviewer"))
            assertEquals(a,repo.resolve(a)!!.id)
            repo.relate(CatalogueRelationship(id(),"source",a,"work",b,"work"))
            repo.relate(CatalogueRelationship(id(),"source",b,"work",c,"work"))
            rejects {repo.relate(CatalogueRelationship(id(),"source",c,"work",a,"work"))}
            db.openHelper.readableDatabase.query("SELECT count(*) FROM identity_revisions").use {it.moveToFirst();assertEquals(4,it.getInt(0))}
        } finally {db.close();cleanup(name)}
    }
    @Test fun provenanceAndRightsDefaultDeny()=runBlocking {
        val name="stage3a-rights-${id()}.db";val db=CatalogueDatabase.builder(context,name).build();val repo=CatalogueRepository(db)
        try {
            val work=id();val source=id();val blocked=id();val approved=id()
            repo.add(CatalogueEntity(work,"work","Authored fixture"),CatalogueWork(work))
            repo.addSource(CatalogueSource(source,"fixture","original","v1","a".repeat(64),"2026-10-18T00:00:00Z","https://github.com/stokonn/Goldberg"))
            repo.addRights(CatalogueRights(blocked,"unknown","","","unknown"))
            rejects {repo.addRights(CatalogueRights(id(),"CC-BY-NC-4.0","https://example.invalid","Attribution","approved"))}
            rejects {repo.addDocument(CatalogueDocument(id(),work,"cs","v1","b".repeat(64),blocked,source,"text/fixture.txt","https://example.invalid/revision","https://example.invalid/history"))}
            repo.addRights(CatalogueRights(approved,"CC0-1.0","https://creativecommons.org/publicdomain/zero/1.0/","Goldberg authored fixture","approved","original text"))
            repo.addDocument(CatalogueDocument(id(),work,"en","v1","b".repeat(64),approved,source,"text/fixture.txt","https://example.invalid/revision","https://example.invalid/history"))
            rejects {repo.addMedia(CatalogueMedia(id(),"b".repeat(64),4,approved,source,"../private"))}
            rejects {repo.addAssertion(CatalogueAssertion(id(),work,"biography","{}",source,blocked,true))}
            repo.addAssertion(CatalogueAssertion(id(),work,"genre","\"fictional\"",source,approved,true))
        } finally {db.close();cleanup(name)}
    }
    @Test fun catalogueFailureDeletionRebuildAndJournalOffline()=runBlocking {
        val personalName="stage3a-personal-${id()}.db";val name="stage3a-optional-${id()}.db"
        val journal=Room.databaseBuilder(context,OpusDatabase::class.java,personalName).addCallback(PersonalCatalogueBridge.callback).build()
        var store=LocalCatalogueStore(context,journal,name)
        try {
            journal.dao().save(Piece(id=7,title="Offline manual",composer="User",notes="Keep personal notes"))
            journal.practiceDao().checkpoint(PracticeDraft(payload="original opaque recovery bytes",updatedAt=123))
            val anchor=journal.personalCatalogueDao().anchors("piece","7").single()
            val uuid=journal.personalCatalogueDao().identity(anchor.identityKey)!!.canonicalId
            assertEquals(CatalogueAccess.Available(true),store.initialize())
            assertEquals(uuid,(store.access {it.page()} as CatalogueAccess.Available).value.single().id)
            store.close();context.deleteDatabase(name)
            store=LocalCatalogueStore(context,journal,name)
            assertEquals(CatalogueAccess.Available(true),store.initialize())
            assertEquals(uuid,(store.access {it.page()} as CatalogueAccess.Available).value.single().id)
            store.close()
            // Corruption remains byte-for-byte available; explicit rebuild uses another generation.
            context.deleteDatabase(name);val corrupt=context.getDatabasePath(name);corrupt.writeText("Not a SQLite database; preserve me")
            val bytes=corrupt.readBytes()
            store=LocalCatalogueStore(context,journal,name)
            assertTrue(store.initialize() is CatalogueAccess.Unavailable)
            assertArrayEquals(bytes,corrupt.readBytes())
            assertEquals(CatalogueAccess.Available(Unit),store.rebuild())
            assertArrayEquals(bytes,corrupt.readBytes())
            assertEquals(uuid,(store.access {it.page()} as CatalogueAccess.Available).value.single().id)
            store.close();store=LocalCatalogueStore(context,journal,name)
            assertEquals(uuid,(store.access {it.page()} as CatalogueAccess.Available).value.single().id)
            val unavailable=LocalCatalogueStore(context,journal,"stage3a-no-open",opener={error("Injected unavailable catalogue")})
            assertTrue(unavailable.initialize() is CatalogueAccess.Unavailable)
            assertEquals("Keep personal notes",journal.dao().piece(7)!!.notes)
            assertEquals("original opaque recovery bytes",journal.practiceDao().draft()!!.payload)
            assertEquals(0,journal.dao().allSessions().size)
        } finally {store.close();journal.close();context.deleteDatabase(personalName);cleanup(name)}
    }
    @Test fun interruptedCopyAndRepeatedInitialization()=runBlocking {
        val personalName="stage3a-copy-personal-${id()}.db";val name="stage3a-copy-${id()}.db"
        val journal=Room.databaseBuilder(context,OpusDatabase::class.java,personalName).addCallback(PersonalCatalogueBridge.callback).build()
        val db=CatalogueDatabase.builder(context,name).build()
        try {
            for(i in 1..140) journal.dao().save(Piece(id=i.toLong(),title="Manual $i",composer="User"))
            db.openHelper.writableDatabase.execSQL("CREATE TRIGGER interrupt_copy BEFORE INSERT ON entities WHEN (SELECT count(*) FROM entities)=5 BEGIN SELECT RAISE(ABORT,'Injected interrupted copy'); END")
            var store=LocalCatalogueStore(context,journal,name,opener={db})
            assertTrue(store.initialize() is CatalogueAccess.Unavailable) // closes this failed handle
            val reopened=CatalogueDatabase.builder(context,name).build()
            assertEquals(0,CatalogueRepository(reopened).page(limit=100).size)
            reopened.openHelper.writableDatabase.execSQL("DROP TRIGGER interrupt_copy");reopened.close()
            store=LocalCatalogueStore(context,journal,name)
            assertEquals(CatalogueAccess.Available(false),store.initialize())
            assertEquals(CatalogueAccess.Available(true),store.initialize())
            assertEquals(CatalogueAccess.Available(false),store.initialize())
            assertEquals(CatalogueAccess.Available(true),store.initialize())
            val first=(store.access{it.page(limit=100)} as CatalogueAccess.Available).value
            val second=(store.access{it.page(after=first.last().id,limit=100)} as CatalogueAccess.Available).value
            assertEquals(140,first.size+second.size)
            store.close()
        } finally {db.close();journal.close();context.deleteDatabase(personalName);cleanup(name)}
    }
}
