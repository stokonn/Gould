package com.opus.piano
import com.opus.piano.audio.PlaybackEvidence
import org.junit.Assert.*
import org.junit.Test
class PlaybackEvidenceTest {
    @Test fun pauseSeekAndImpossibleJumpsCannotAwardPlayback() {
        val e = PlaybackEvidence()
        e.boundary(0,1000); e.advance(200,1200)
        e.boundary(200,5000) // paused
        e.boundary(10000,5000) // explicit seek
        e.advance(10200,5200)
        e.advance(30000,5300) // impossible progression
        e.advance(29900,5400) // backward discontinuity
        assertEquals(400L,e.playedMillis)
    }
}
