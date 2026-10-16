package com.opus.piano

import com.opus.piano.audio.PianoActivityDetector
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.*
import kotlin.random.Random
class PianoActivityDetectorTest {
    @Test fun silenceAndBroadbandNoiseDoNotTrigger() {
        val silence=PianoActivityDetector(); repeat(30) { assertFalse(silence.likely(ShortArray(2048),2048,50)) }
        val noise=PianoActivityDetector(); val random=Random(7)
        repeat(30) { assertFalse(noise.likely(ShortArray(2048) { random.nextInt(-2000,2000).toShort() },2048,50)) }
    }
    @Test fun sensitivityChangesQuietToneThreshold() {
        fun hits(sensitivity: Int): Int {
            val detector=PianoActivityDetector()
            return (0 until 30).count { block ->
                detector.likely(ShortArray(2048) { i -> (sin(2*PI*440*(block*2048+i)/22050)*250).toInt().toShort() },2048,sensitivity)
            }
        }
        assertTrue(hits(100)>hits(0))
    }
    @Test fun isolatedImpulsesAreNotSustainedPlaying() {
        val detector=PianoActivityDetector()
        repeat(30) { block ->
            val samples=ShortArray(2048); if (block%10==0) samples[100]=20000
            assertFalse(detector.likely(samples,2048,50))
        }
    }
    @Test fun tonalAttackCanTriggerButIsNotProofOfPiano() {
        val detector=PianoActivityDetector()
        var hits=0
        repeat(30) { block ->
            val data=ShortArray(2048) { i -> (sin(2*PI*440*(block*2048+i)/22050)*8000*exp(-block*.02)).toInt().toShort() }
            if (detector.likely(data,2048,50)) hits++
        }
        assertTrue(hits>0)
    }
}
