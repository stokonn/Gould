package com.opus.piano

import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.data.*
import com.opus.piano.data.catalogue.*
import com.opus.piano.data.catalogue.providers.*
import com.opus.piano.practice.PracticeCoordinator
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

/** Synthetic catalogue only. Timings are observations, never provider identity evidence. */
@RunWith(AndroidJUnit4::class)
class Stage4SearchVerificationTest {
    private val context=InstrumentationRegistry.getInstrumentation().targetContext
    private fun id()=UUID.randomUUID().toString()
    @Test fun authoredRelevanceNamesakesDiacriticsGenresAndFilters()=runBlocking {
        val name="stage4-relevance-${id()}";val db=CatalogueDatabase.builder(context,name).build();val repo=CatalogueRepository(db)
        try {
            val records=listOf(
                ProviderRecord("composer","1","artist","Johann Sebastian Bach",fields=listOf(FieldValue("epoch","Baroque"))),
                ProviderRecord("work","101","work","Goldberg Variations",aliases=listOf("cs" to "Goldbergovy variace","und" to "BWV 988"),credits=listOf(ProviderCredit("1","Johann Sebastian Bach","composer"))),
                ProviderRecord("work","102","work","Goldberg Variations",credits=listOf(ProviderCredit("2","Authored Namesake","composer"))),
                ProviderRecord("work","103","work","Goldberg Variations Study"),
                ProviderRecord("work","104","work","Nocturne Op. 9 No. 2",aliases=listOf("und" to "Chopin nocturne 9 2")),
                ProviderRecord("work","105","work","Žluťoučká píseň",fields=listOf(FieldValue("genre","Folk"))),
                ProviderRecord("work","106","work","Blue Train",fields=listOf(FieldValue("genre","Jazz"))),
                ProviderRecord("work","107","work","Electric Skyline",fields=listOf(FieldValue("genre","Electronic")))
            )
            repo.ingest(ProviderResult(ProviderRequest("https://api.openopus.org/fixture","openopus","search"),SourceState.FRESH,records,"1","8".repeat(64)))
            val first=repo.providerIdentity("openopus","work","101")!!.entityId
            assertEquals(first,repo.searchPage("BWV988").items.single().id)
            assertEquals(first,repo.searchPage("Bach Goldberg").items.single().id)
            assertEquals(first,repo.searchPage("Goldbergovy variace").items.single().id)
            val expectedExact=listOf("101","102").map {repo.providerIdentity("openopus","work",it)!!.entityId}.sorted()
            val prefix=repo.providerIdentity("openopus","work","103")!!.entityId
            assertEquals(expectedExact+prefix,repo.searchPage("Goldberg Variations").items.map {it.id})
            assertEquals("Nocturne Op. 9 No. 2",repo.searchPage("Chopin nocturne 9 2").items.single().title)
            assertEquals("Žluťoučká píseň",repo.searchPage("zlutoucka pisen").items.single().title)
            assertEquals("Blue Train",repo.searchPage("Blue Train").items.single().title)
            assertEquals("Electric Skyline",repo.searchPage("Electric Skyline").items.single().title)
            val bach=repo.providerIdentity("openopus","composer","1")!!.entityId
            assertEquals(first,repo.searchPage("Goldberg",SearchFilter(artistId=bach)).items.single().id)
            assertTrue(repo.searchPage("Goldberg",SearchFilter("artists")).items.isEmpty())
            assertEquals(first,repo.searchPage("Goldberg",SearchFilter("works",period="Baroque")).items.single().id)
            assertTrue(repo.searchPage("Goldberg",SearchFilter("works",period="Romantic")).items.isEmpty())
            assertTrue(repo.searchPage("Goldberg",SearchFilter("works",period="Bar%")).items.isEmpty())
            val paged=repo.searchPage("Goldberg",limit=1)
            assertTrue(runCatching {repo.searchPage("Goldberg",SearchFilter(period="Baroque"),paged.nextCursor)}.isFailure)
        } finally {db.close();context.deleteDatabase(name)}
    }
    @Test fun historyIsBoundedOptionalAndIndependentOfJournal() {
        val prefs="stage4-history-${id()}";val history=DiscoverHistory(context,prefs)
        try {
            val ids=List(25){id()};ids.forEach {history.opened(it)}
            assertEquals(ids.takeLast(20).reversed(),history.entries())
            history.opened(ids.last());assertEquals(20,history.entries().size)
            history.remove(ids.last());assertEquals(19,history.entries().size)
            history.enabled(false);history.opened(id());assertTrue(history.entries().isEmpty())
            history.enabled(true);history.opened(ids.first());assertEquals(listOf(ids.first()),history.entries())
            history.clear();assertTrue(history.entries().isEmpty())
        } finally {context.getSharedPreferences(prefs,0).edit().clear().commit()}
    }
    @Test fun measuredBoundedSearchAtOneTenAndFiftyThousandWithPracticeUnderLoad()=runBlocking {
        val name="stage4-performance-${id()}";val db=CatalogueDatabase.builder(context,name).build();val repo=CatalogueRepository(db)
        val journal=Room.inMemoryDatabaseBuilder(context,OpusDatabase::class.java).addCallback(PersonalCatalogueBridge.callback).build()
        val samples=JSONArray();var inserted=0
        fun clock()=android.os.SystemClock.elapsedRealtimeNanos()
        try {
            for(size in listOf(1000,10000,50000)) {
                val begin=clock()
                db.withTransaction {while(inserted<size) {
                    val key=UUID.nameUUIDFromBytes("stage4-performance:$inserted".toByteArray()).toString()
                    repo.add(CatalogueEntity(key,"work","Fixture study %05d".format(java.util.Locale.ROOT,inserted)),CatalogueWork(key));inserted++
                }}
                val buildMs=(clock()-begin)/1e6
                val heapBefore=Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()
                val times=List(21) {
                    val start=clock();assertEquals(40,repo.searchPage("fixture study 000",limit=40).items.size);(clock()-start)/1e6
                }
                val first=repo.searchPage("fixture",limit=40);val second=repo.searchPage("fixture",cursor=first.nextCursor,limit=40)
                assertEquals(40,second.items.size);assertTrue(first.items.map {it.id}.intersect(second.items.map {it.id}.toSet()).isEmpty())
                assertEquals(second.items.map {it.id},repo.searchPage("fixture",cursor=first.nextCursor,limit=40).items.map {it.id})
                val heapAfter=Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()
                db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()
                val sorted=times.sorted()
                samples.put(JSONObject().put("rows",size).put("incrementalBuildMs",buildMs).put("querySamplesMs",JSONArray(times)).put("medianMs",sorted[10]).put("p95Ms",sorted[19]).put("javaHeapDeltaBytes",heapAfter-heapBefore).put("databaseBytes",context.getDatabasePath(name).length()))
            }
            val load=launch(Dispatchers.IO){repeat(30){repo.searchPage("fixture study 000")}}
            val begin=clock();val coordinator=PracticeCoordinator(PracticeRepository(journal,OpusRepository(journal,AchievementStore(journal))))
            withTimeout(5000){coordinator.state.first {it.ready}}
            val readyMs=(clock()-begin)/1e6
            coordinator.command {start(false)};withTimeout(5000){coordinator.state.first {it.running}}
            delay(300);coordinator.command {pause();review();save()};withTimeout(5000){coordinator.state.first {it.token.isEmpty()}}
            load.join();val session=journal.dao().allSessions().single()
            assertEquals(session.durationMillis,journal.personalDao().allParts().sumOf {it.durationMillis});assertNull(journal.practiceDao().draft())
            val result=JSONObject().put("api",android.os.Build.VERSION.SDK_INT).put("samples",samples).put("journalReadyUnderLoadMs",readyMs).put("sessionDurationMillis",session.durationMillis).put("singleCreditReconciled",true).put("memoryCaveat","Java heap delta is noisy and excludes native/SQLite allocations; no forced GC")
            File(context.filesDir,"stage4-performance.json").writeText(result.toString(2));println("STAGE4_PERFORMANCE $result")
        } finally {db.close();journal.close();context.deleteDatabase(name)}
    }
}
