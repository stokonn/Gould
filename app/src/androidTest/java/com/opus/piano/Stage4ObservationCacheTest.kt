package com.opus.piano

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.data.catalogue.*
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class Stage4ObservationCacheTest {
    private suspend fun environment(block:suspend (CatalogueDatabase)->Unit) {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val name="stage4-observations-${UUID.randomUUID()}"
        val db=CatalogueDatabase.builder(context,name).build()
        try {block(db)}finally {db.close();context.deleteDatabase(name)}
    }
    @Test fun leastRecentlyAccessedObservationsEvictWithoutTouchingAcceptedRows()=runBlocking {
        environment {db->
            val cache=CatalogueObservationCache(db);val repo=CatalogueRepository(db)
            val id=UUID.randomUUID().toString();repo.add(CatalogueEntity(id,"work","Durable accepted title"),CatalogueWork(id))
            repo.state("generation","protected-generation")
            cache.put("candidate:","candidate:a","a",100,2)
            cache.put("candidate:","candidate:b","b",100,2)
            cache.touch("candidate:a",1) // Wall-clock rollback must not reverse recency.
            cache.put("candidate:","candidate:c","c",1,2)
            assertNull(repo.state("candidate:b"));assertNull(repo.state("access:candidate:b"))
            assertEquals("a",repo.state("candidate:a"));assertEquals("c",repo.state("candidate:c"))
            assertEquals("Durable accepted title",repo.entity(id)!!.title)
            assertEquals("protected-generation",repo.generation())
            assertEquals(1L,JSONObject(repo.state("cache-evictions:candidate:")!!).getLong("count"))
            repo.removeState("candidate:a");assertNull(repo.state("access:candidate:a"))
        }
    }
    @Test fun legacyRowsAreBoundedAndEvictionCountsSaturateWithoutOverflow()=runBlocking {
        environment {db->
            val dao=db.dao();val cache=CatalogueObservationCache(db)
            repeat(230){dao.state(CatalogueState("diagnostic:legacy-$it","unresolved"))}
            dao.state(CatalogueState("cache-evictions:diagnostic:",JSONObject().put("count",Long.MAX_VALUE).toString()))
            cache.put("diagnostic:","diagnostic:new","new",1)
            assertEquals(200,dao.stateCount("diagnostic:%"));assertNotNull(dao.state("diagnostic:new"))
            assertEquals(Long.MAX_VALUE,JSONObject(dao.state("cache-evictions:diagnostic:")!!.value).getLong("count"))
            assertEquals(1,dao.stateCount("access:diagnostic:%"))
        }
    }
    @Test fun protectedPrefixesAndOversizedValuesCannotEnterEvictionPath()=runBlocking {
        environment {db->
            val cache=CatalogueObservationCache(db)
            for(prefix in listOf("source:","identity:","generation","personal:"))
                assertTrue(runCatching {cache.put(prefix,prefix+"key","value")}.isFailure)
            assertTrue(runCatching {cache.put("candidate:","diagnostic:wrong","value")}.isFailure)
            assertTrue(runCatching {cache.put("candidate:","candidate:large","x".repeat(32769))}.isFailure)
            assertEquals(0,db.dao().stateCount("candidate:%"))
        }
    }
}
