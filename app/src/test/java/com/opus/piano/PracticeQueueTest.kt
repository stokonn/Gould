package com.opus.piano

import com.opus.piano.data.Piece
import com.opus.piano.practice.*
import org.junit.Assert.*
import org.junit.Test

class PracticeQueueTest {
    private val bach = Piece(1, "Prelude", "Bach")
    private val mozart = Piece(2, "Sonata", "Mozart")
    @Test fun undoSelectionChargesCurrentPieceThroughUndoAndNeverMovesPastCredit() {
        val engine = PracticeEngine(PracticeState(ready = true))
        engine.begin(0, 1000, "token", false)
        engine.queueSelect(1000, bach)
        engine.queueSelect(3000, mozart)
        val before = engine.state.intervals.toList()
        engine.undoQueue(6000)
        assertEquals(1, engine.state.active)
        assertEquals(listOf(1000L, 2000L, 3000L), engine.state.parts.map { it.durationMillis })
        assertEquals(before, engine.state.intervals.take(before.size))
        engine.advance(7000)
        assertEquals(listOf(1000L, 3000L, 3000L), engine.state.parts.map { it.durationMillis })
        assertEquals(7000L, engine.state.elapsed)
        assertNull(engine.state.queueUndo)
        assertEquals(listOf(0, 1), engine.state.visibleQueue())
    }
    @Test fun removalAndReorderingKeepLedgerIndexesAndUndoRestoresOnlyIntent() {
        val engine = PracticeEngine(PracticeState(ready = true))
        engine.begin(0, 1000, "token", false)
        engine.queueSelect(1000, bach); engine.queueSelect(2000, mozart)
        engine.queueMove(3000, 2, -1)
        assertEquals(listOf(0, 2, 1), engine.state.visibleQueue())
        engine.undoQueue(4000)
        assertEquals(listOf(0, 1, 2), engine.state.visibleQueue())
        engine.queueRemove(5000, 2)
        assertEquals(0, engine.state.active)
        assertEquals(listOf(0, 1), engine.state.visibleQueue())
        engine.undoQueue(6000)
        assertEquals(2, engine.state.active)
        assertEquals(listOf(2000L, 1000L, 3000L), engine.state.parts.map { it.durationMillis })
        assertEquals(6000L, engine.state.intervals.sumOf { it.creditedMillis })
        assertTrue(engine.state.intervals.all { it.partIndex in engine.state.parts.indices })
    }
}
