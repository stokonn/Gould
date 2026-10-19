package com.opus.piano

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.data.*
import com.opus.piano.data.catalogue.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class Stage4StorageAdmissionTest {
    @Test fun refusedCopyAndRebuildPreserveJournalAcceptedLedgerAndGenerationFiles()=runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val journal=Room.inMemoryDatabaseBuilder(context,OpusDatabase::class.java).addCallback(PersonalCatalogueBridge.callback).build()
        val name="stage4-admission-${UUID.randomUUID()}.db"
        var permitted=true;var opens=0
        val intake=CatalogueIntake {permitted}
        val store=LocalCatalogueStore(context,journal,name,intake=intake,opener={opens++;CatalogueDatabase.builder(context,it).build()})
        val key=UUID.randomUUID().toString()
        fun generations()=context.getDatabasePath(name).parentFile!!.listFiles().orEmpty().filter {it.name==name || it.name.startsWith("$name.") || it.name.startsWith("$name-")}.map {it.name}.toSet()
        try {
            journal.dao().save(Piece(id=41,title="Private preserved work",composer="Private author",notes="Unchanged notes"))
            assertTrue(store.initialize() is CatalogueAccess.Available)
            assertTrue(store.access {it.add(CatalogueEntity(key,"work","Authored accepted work"),CatalogueWork(key))} is CatalogueAccess.Available)
            val before=generations();val opened=opens
            permitted=false
            assertTrue(store.initialize() is CatalogueAccess.Rejected)
            assertTrue(store.rebuild() is CatalogueAccess.Rejected)
            assertEquals(opened,opens);assertEquals(before,generations())
            assertEquals("Unchanged notes",journal.dao().piece(41)!!.notes)
            assertEquals(key,(store.access {it.entity(key)} as CatalogueAccess.Available).value!!.id)
            permitted=true
            assertTrue(store.rebuild() is CatalogueAccess.Available)
            assertEquals(key,(store.access {it.entity(key)} as CatalogueAccess.Available).value!!.id)
            assertEquals("Unchanged notes",journal.dao().piece(41)!!.notes)
            // A denied post-restore rebuild must never expose the prior journal's index.
            journal.openHelper.writableDatabase.execSQL("UPDATE personal_catalogue_state SET scope=? WHERE id=1",arrayOf(UUID.randomUUID().toString()))
            permitted=false
            assertTrue(store.initialize() is CatalogueAccess.Rejected)
            assertTrue(store.access {it.page(limit=100)} is CatalogueAccess.Rejected)
            assertEquals("Unchanged notes",journal.dao().piece(41)!!.notes)
            permitted=true
            assertTrue(store.initialize() is CatalogueAccess.Available)
            assertNull((store.access {it.entity(key)} as CatalogueAccess.Available).value)
            assertTrue("Original durable catalogue generation must remain retained",context.getDatabasePath(name).exists())
        }finally {
            store.close();journal.close()
            generations().forEach {context.getDatabasePath(it).delete()}
        }
    }
}
