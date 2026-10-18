package com.opus.piano

import android.os.SystemClock
import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.opus.piano.data.*
import com.opus.piano.data.catalogue.*
import com.opus.piano.practice.PracticeCoordinator
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import org.json.JSONArray
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class Stage3aPerformanceTest {
    @Test fun boundedQueriesAndJournalPriorityUnderCatalogueLoad()=runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val name="stage3a-perf-${UUID.randomUUID()}.db"
        val db=CatalogueDatabase.builder(context,name).build();val repo=CatalogueRepository(db)
        val journal=Room.inMemoryDatabaseBuilder(context,OpusDatabase::class.java).addCallback(PersonalCatalogueBridge.callback).build()
        try {
            val begin=SystemClock.elapsedRealtimeNanos()
            db.withTransaction { repeat(10000) {i ->
                val id=UUID.nameUUIDFromBytes("isolated-performance:$i".toByteArray()).toString()
                repo.add(CatalogueEntity(id,"work","Fixture study %05d".format(java.util.Locale.ROOT,i)),CatalogueWork(id))
            } }
            val buildMs=(SystemClock.elapsedRealtimeNanos()-begin)/1_000_000.0
            val times=mutableListOf<Double>()
            repeat(31) {
                val start=SystemClock.elapsedRealtimeNanos()
                assertEquals(40,repo.search("fixture study 000",limit=40).size)
                times.add((SystemClock.elapsedRealtimeNanos()-start)/1_000_000.0)
            }
            val load=launch(Dispatchers.IO) {repeat(100) {repo.search("fixture study 000",limit=40)} }
            val start=SystemClock.elapsedRealtimeNanos()
            val coordinator=PracticeCoordinator(PracticeRepository(journal,OpusRepository(journal,AchievementStore(journal))))
            withTimeout(5000) {coordinator.state.first{it.ready}}
            val readyMs=(SystemClock.elapsedRealtimeNanos()-start)/1_000_000.0
            coordinator.command { start(false) }
            withTimeout(5000) {coordinator.state.first{it.running}}
            val token=coordinator.state.value.token
            delay(250)
            coordinator.command {pause();review();save()}
            withTimeout(5000) {coordinator.state.first{it.token.isEmpty()}}
            load.join()
            val session=journal.dao().allSessions().single()
            assertEquals(token,session.completionToken)
            assertEquals(session.durationMillis,journal.personalDao().allParts().sumOf{it.durationMillis})
            assertNull(journal.practiceDao().draft())
            db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()
            val sorted=times.sorted()
            val result=JSONObject().put("api",android.os.Build.VERSION.SDK_INT).put("rows",10000)
                .put("buildMs",buildMs).put("querySamplesMs",JSONArray(times))
                .put("queryMedianMs",sorted[15]).put("queryP95Ms",sorted[29]).put("journalReadyUnderLoadMs",readyMs)
                .put("databaseBytes",context.getDatabasePath(name).length()).put("sessionDurationMillis",session.durationMillis)
                .put("singleCreditReconciled",true)
            File(context.filesDir,"stage3a-performance.json").writeText(result.toString(2))
            println("STAGE3A_PERFORMANCE $result")
        } finally {db.close();journal.close();context.deleteDatabase(name)}
    }
}
