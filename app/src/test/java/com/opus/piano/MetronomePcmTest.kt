package com.opus.piano

import com.opus.piano.audio.*
import com.opus.piano.practice.*
import org.junit.Assert.*
import org.junit.Test

class MetronomePcmTest {
    @Test fun boundedDeterministicBarHasAccentedBeatsAndSilentGaps() {
        val pcm = MetronomePcm.bar(120, 4)
        assertEquals(44100, pcm.size)
        assertArrayEquals(pcm, MetronomePcm.bar(120,4))
        assertTrue(pcm.take(132).maxOf { kotlin.math.abs(it.toInt()) } > pcm.drop(11025).take(132).maxOf { kotlin.math.abs(it.toInt()) })
        assertTrue(pcm.slice(132 until 11025).all { it == 0.toShort() })
        assertEquals(396900, MetronomePcm.bar(40,12).size)
    }
    @Test fun isolatedMetronomeAcrossTempoAndSensitivityCannotEarnSmartCredit() {
        for (bpm in listOf(40,80,120,200,240)) for (sensitivity in listOf(0,50,100)) {
            val pcm = MetronomePcm.bar(bpm,4)
            val detector = PianoActivityDetector()
            val engine = PracticeEngine(PracticeState(ready=true))
            engine.begin(0,1000,"metronome-fixture",true)
            repeat(250) { block ->
                val samples = ShortArray(2048) { i -> pcm[(block * 2048 + i) % pcm.size] }
                val now = block * 2048L * 1000 / MetronomePcm.RATE
                engine.evidence(now, detector.likely(samples, samples.size, sensitivity))
            }
            assertEquals("bpm=$bpm sensitivity=$sensitivity",0L,engine.state.elapsed)
        }
    }
}
