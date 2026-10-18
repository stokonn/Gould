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

@RunWith(AndroidJUnit4::class)
class Stage4SafetyTest {
    private val context=InstrumentationRegistry.getInstrumentation().targetContext
    private fun id()=UUID.randomUUID().toString()
    private fun detail(kind:String="work",title:String="Authored work"):DiscoverDetail {
        val key=id();return DiscoverDetail(CatalogueEntity(key,kind,title),listOf(CatalogueExternalIdentity("musicbrainz","work",id(),key,"authored fixture","reviewed")),emptyList(),emptyList())
    }
    @Test fun groupedPartsSnapshotAndExplicitSeparateEntriesNeverCreatePracticeCredit()=runBlocking {
        val db=Room.inMemoryDatabaseBuilder(context,OpusDatabase::class.java).addCallback(PersonalCatalogueBridge.callback).build()
        try {
            val child=detail(title="Authored movement");val parent=detail().let {it.copy(relationships=listOf(CatalogueRelationship(id(),"part",child.entity.id,"work",it.entity.id,"work",1) to child.entity))}
            val repository=CatalogueRepertoire(db)
            val grouped=repository.add(parent,listOf(child)).single();assertTrue(grouped.created)
            val anchor=db.personalCatalogueDao().anchors("piece",grouped.pieceId.toString()).single();val identity=db.personalCatalogueDao().identity(anchor.identityKey)!!
            val evidence=JSONObject(identity.evidence.substringAfter("catalogue-link-v1:"))
            assertEquals(child.entity.title,evidence.getJSONArray("partSnapshots").getJSONObject(0).getString("title"))
            assertTrue(repository.add(parent).single().selectionConflict)
            assertFalse(repository.add(parent,listOf(child)).single().selectionConflict)
            val separate=repository.add(parent,listOf(child),separateEntries=true).single();assertTrue(separate.created);assertNotEquals(grouped.pieceId,separate.pieceId)
            assertEquals(2,db.dao().allPieces().size);assertTrue(db.dao().allSessions().isEmpty())
            assertEquals(identity.canonicalId,db.personalCatalogueDao().identity(anchor.identityKey)!!.canonicalId)
        }finally{db.close()}
    }
    @Test fun arrangementPersonalUuidIsIndependentAndLegacyOpenOpusPieceIsNotDuplicated()=runBlocking {
        val db=Room.inMemoryDatabaseBuilder(context,OpusDatabase::class.java).addCallback(PersonalCatalogueBridge.callback).build()
        try {
            val arrangement=detail("arrangement");val repository=CatalogueRepertoire(db);val added=repository.add(arrangement).single()
            val anchor=db.personalCatalogueDao().anchors("piece",added.pieceId.toString()).single();val identity=db.personalCatalogueDao().identity(anchor.identityKey)!!
            assertEquals("arrangement",identity.kind);assertNotEquals(arrangement.entity.id,identity.canonicalId)
            val old=db.dao().insertCatalogPiece(Piece(title="My old edited title",composer="Private artist",catalogWorkId="123",notes="Preserve me"))
            val work=detail();val legacy=work.copy(identities=listOf(CatalogueExternalIdentity("openopus","work","123",work.entity.id,"fixture","reviewed")))
            val existing=repository.add(legacy).single();assertFalse(existing.created);assertEquals(old,existing.pieceId);assertEquals("Preserve me",db.dao().piece(old)!!.notes)
            assertEquals(2,db.dao().allPieces().size)
        }finally{db.close()}
    }
    @Test fun portableV5RetainsOptionalLinksGroupedSnapshotsUuidAndRawRecoveryWithoutCatalogue()=runBlocking {
        fun database()=Room.inMemoryDatabaseBuilder(context,OpusDatabase::class.java).addCallback(PersonalCatalogueBridge.callback).addCallback(BackupState.callback).build()
        val source=database();val target=database();val key=id()
        val sourceDir=java.io.File(context.cacheDir,"stage4-link-source-$key");val targetDir=java.io.File(context.cacheDir,"stage4-link-target-$key")
        val sourcePrefs=com.opus.piano.data.backup.PortablePreferences(context,"stage4-link-source-$key-")
        val targetPrefs=com.opus.piano.data.backup.PortablePreferences(context,"stage4-link-target-$key-")
        try {
            val child=detail(title="Authored movement")
            val parent=detail().let {it.copy(relationships=listOf(CatalogueRelationship(id(),"part",child.entity.id,"work",it.entity.id,"work",1) to child.entity))}
            val added=CatalogueRepertoire(source).add(parent,listOf(child)).single()
            val original=source.dao().piece(added.pieceId)!!
            source.dao().save(original.copy(title="Private edited title",composer="Private artist",notes="Preserve private notes"))
            source.practiceDao().checkpoint(PracticeDraft(payload="Unparsed original recovery Ž",updatedAt=123))
            val anchor=source.personalCatalogueDao().anchors("piece",added.pieceId.toString()).single()
            val identity=source.personalCatalogueDao().identity(anchor.identityKey)!!
            val raw=BackupRepository(context,source,sourcePrefs,storageDirectory=sourceDir).export()
            BackupRepository(context,target,targetPrefs,storageDirectory=targetDir).restore(raw)
            val restored=target.dao().allPieces().single()
            val restoredAnchor=target.personalCatalogueDao().anchors("piece",restored.id.toString()).single()
            val restoredIdentity=target.personalCatalogueDao().identity(restoredAnchor.identityKey)!!
            assertEquals(identity.canonicalId,restoredIdentity.canonicalId);assertEquals(identity.evidence,restoredIdentity.evidence)
            assertEquals("Private edited title",restored.title);assertEquals("Private artist",restored.composer);assertEquals("Preserve private notes",restored.notes)
            assertEquals("Unparsed original recovery Ž",target.practiceDao().draft()!!.payload)
            val duplicate=CatalogueRepertoire(target).add(parent,listOf(child)).single()
            assertFalse(duplicate.created);assertFalse(duplicate.selectionConflict);assertEquals(restored.id,duplicate.pieceId)
            assertTrue(target.dao().allSessions().isEmpty())
        } finally {source.close();target.close();sourceDir.deleteRecursively();targetDir.deleteRecursively()}
    }
    @Test fun correctionExportContainsOnlyCatalogueEvidenceAndRemainsUnsubmitted() {
        val source=CatalogueSource(id(),"fixture","work:1","revision","a".repeat(64),"2026-10-18T00:00:00Z","https://example.invalid/work/1")
        val rights=CatalogueRights(id(),"CC0-1.0","https://creativecommons.org/publicdomain/zero/1.0/","Authored fixture","approved")
        val entity=detail();val row=CatalogueAssertion(id(),entity.entity.id,"catalogue_number","{\"value\":\"Op. 1\",\"language\":\"und\"}",source.id,rights.id,true)
        val record=entity.copy(assertions=listOf(SourcedAssertion(row,source,rights)))
        val json=JSONObject(CataloguePresentation.correction(record,"catalogue_number","Op. 2","Authored conflicting evidence"))
        assertEquals("local_unsubmitted",json.getString("status"));assertFalse(json.has("journal"));assertFalse(json.has("profile"));assertEquals(source.payloadSha256,json.getJSONArray("assertions").getJSONObject(0).getString("sha256"))
    }
    @Test fun portraitDecoderRejectsUnsupportedAndOversizedContent() {
        assertNull(CataloguePortraits.decode("not an image".toByteArray()));assertNull(CataloguePortraits.decode(ByteArray(CataloguePortraits.MAX_BYTES+1)))
        val bitmap=android.graphics.Bitmap.createBitmap(2,2,android.graphics.Bitmap.Config.ARGB_8888);val stream=java.io.ByteArrayOutputStream();bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,stream)
        assertNotNull(CataloguePortraits.decode(stream.toByteArray()));bitmap.recycle()
    }
    @Test fun persistentCacheEvictsByLastAccessAndNeverTouchesOtherFiles()=runBlocking {
        val directory=java.io.File(context.cacheDir,"stage4-lru-${id()}").apply {mkdirs()};val unrelated=java.io.File(directory,"retained-draft.txt").apply {writeText("unchanged")}
        var time=100000L;val cache=FileProviderCache(directory,{time},maxEntries=2)
        val adapter=OpenOpusAdapter();val requests=(1..3).map {adapter.search("query$it","works","en").single()}
        try {
            suspend fun write(index:Int){val r=requests[index];cache.write(r,CachedProviderValue(ProviderResult(r,SourceState.FRESH,listOf(ProviderRecord("work","$index","work","Authored $index")),"1","a".repeat(64)),time+1000,null,null));time+=100}
            write(0);write(1);assertNotNull(cache.read(requests[0]));time+=100;write(2)
            assertNull(cache.read(requests[1]));assertNotNull(cache.read(requests[0]));assertNotNull(cache.read(requests[2]))
            cache.clear();assertEquals("unchanged",unrelated.readText());assertEquals(0L,cache.bytes())
        }finally{directory.deleteRecursively()}
    }
}
