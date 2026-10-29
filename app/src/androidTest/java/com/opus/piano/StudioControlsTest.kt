package com.opus.piano

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.audio.*
import com.opus.piano.data.*
import com.opus.piano.practice.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class StudioControlsTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    @Test fun queuedUndoAndNotesSurviveRealRoomDraftReopenWithoutChangingIntervals() = runBlocking {
        val name = "queue-${UUID.randomUUID()}.db"
        fun open() = Room.databaseBuilder(context,OpusDatabase::class.java,name).build()
        var db = open()
        try {
            val e = PracticeEngine(PracticeState(ready=true))
            e.begin(0,1000,"queue-recovery",false)
            e.queueSelect(1000,Piece(4,"Prelude","Bach")); e.queueSelect(3000,Piece(5,"Sonata","Mozart"))
            e.edit { it.copy(notes="Žluťoučký session",parts=it.parts.mapIndexed { i,p -> if(i==1)p.copy(notes="Keep original text") else p }) }
            e.advance(5000)
            val original = e.state
            PracticeRepository(db,OpusRepository(db,AchievementStore(db))).checkpoint(original)
            db.close();db=open()
            val recovered = PracticeRepository(db,OpusRepository(db,AchievementStore(db))).recover()!!
            assertEquals(original.queueOrder,recovered.queueOrder);assertEquals(original.queueUndo,recovered.queueUndo)
            assertEquals(original.intervals,recovered.intervals);assertEquals(original.notes,recovered.notes)
            assertEquals("Keep original text",recovered.parts[1].notes)
            val after = PracticeEngine(recovered)
            after.replace(recovered,0);after.undoQueue(0)
            assertEquals(original.parts.map {it.durationMillis},after.state.parts.map {it.durationMillis})
            assertEquals(original.intervals,after.state.intervals)
            assertEquals(1,after.state.active)
            val bad = org.json.JSONObject(PracticeRepository.encode(original)).put("queue",org.json.JSONArray(listOf(0,999)))
            try { PracticeRepository.decode(bad.toString());fail("Corrupt queue must not be silently dropped") } catch (_: IllegalArgumentException) { }
        } finally { db.close();context.deleteDatabase(name) }
    }
    @Test fun recordingsUseStableKeysetPagesWithEqualDatesAndHiddenTombstones() = runBlocking {
        val db=Room.inMemoryDatabaseBuilder(context,OpusDatabase::class.java).build()
        try {
            repeat(65) { i -> db.studioDao().insertAudio(LocalAudio(UUID.randomUUID().toString(),1000,state=if(i==64)"Deleted" else "Saved",relativeFile="",sampleRate=16000)) }
            val seen=mutableListOf<String>()
            var before=Long.MAX_VALUE;var id="~"
            while(true) {
                val page=db.studioDao().audioPage(before,id,17)
                assertTrue(page.size<=17)
                if(page.isEmpty())break
                seen+=page.map {it.id};before=page.last().createdAt;id=page.last().id
            }
            assertEquals(64,seen.size);assertEquals(64,seen.distinct().size)
        } finally { db.close() }
    }
    @Test fun platformPlayerActuallyPausesSeeksAndDoesNotAwardSeekedUnplayedAudio() = runBlocking {
        val db=Room.inMemoryDatabaseBuilder(context,OpusDatabase::class.java).build()
        val dir=File(context.cacheDir,"player-${UUID.randomUUID()}")
        val repo=LocalRecordingRepository(db,dir)
        val (row,w)=repo.prepare(16000,"Player fixture","",null)
        w.append(ShortArray(32000){i->(kotlin.math.sin(i*2*kotlin.math.PI*440/16000)*1000).toInt().toShort()},32000)
        repo.finalize(row.id,w)
        val player=LocalPlaybackController(context,repo)
        try {
            player.play(row.id)
            withTimeout(8000){player.state.first {it.playing && it.position>=200}}
            player.pause();withTimeout(3000){player.state.first {!it.playing}}
            val paused=player.state.value.position
            delay(200);assertEquals(paused,player.state.value.position)
            player.seek(1000);player.seek(1200);player.seek(1700);delay(200)
            player.play(row.id)
            withTimeout(5000){player.state.first {!it.playing && it.position>=1900}}
            assertTrue(db.studioDao().events().none {it.kind==StudioAchievementRules.PLAYBACK_VERIFIED})
            assertEquals(64044L,repo.playable(row.id).length())
        } finally { player.stopAndAwait();db.close();dir.deleteRecursively() }
    }
    @Test fun completeDecodedShortTakeCreatesOneVerifiedPlaybackEvent()=runBlocking {
        val db=Room.inMemoryDatabaseBuilder(context,OpusDatabase::class.java).build()
        val dir=File(context.cacheDir,"listener-${UUID.randomUUID()}")
        val repo=LocalRecordingRepository(db,dir)
        val (row,w)=repo.prepare(16000,"Listener fixture","",null)
        w.append(ShortArray(16000){i->(kotlin.math.sin(i*2*kotlin.math.PI*440/16000)*1000).toInt().toShort()},16000)
        repo.finalize(row.id,w)
        val player=LocalPlaybackController(context,repo)
        try {
            player.play(row.id)
            withTimeout(8000) {player.state.first {!it.loading && !it.playing && it.position>=1000}}
            player.stopAndAwait()
            assertEquals(32044L,repo.playable(row.id).length())
            repo.delete(row.id)
            assertEquals("Deleted",db.studioDao().audio(row.id)!!.state)
            assertEquals(1,db.studioDao().events().count {it.kind==StudioAchievementRules.PLAYBACK_VERIFIED})
        } finally {player.stopAndAwait();db.close();dir.deleteRecursively()}
    }
    @Test fun nativeMetronomeStartsAndStopsWithoutCreatingPractice()=runBlocking {
        val controller=MetronomeController(context)
        controller.configure(120,3);controller.start()
        try {
            withTimeout(5000){controller.state.first {it.playing || it.failed}}
            assertFalse(controller.state.value.failed);assertTrue(controller.state.value.playing)
            for(bpm in 130..200 step 5) controller.configure(bpm,5)
            withTimeout(5000){controller.state.first {it.playing && !it.starting && it.appliedBpm==200 && it.appliedBeats==5}}
            assertEquals(200,controller.state.value.bpm)
            controller.stop()
            assertFalse(controller.state.value.playing)
        } finally {controller.stop()}
    }
}
