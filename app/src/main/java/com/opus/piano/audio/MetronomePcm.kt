package com.opus.piano.audio

import kotlin.math.roundToInt

/** Bounded static bar; no practice clock, microphone or timing-credit dependency. */
object MetronomePcm {
    const val RATE = 22050
    fun bar(bpm: Int, beats: Int): ShortArray {
        require(bpm in 40..240 && beats in 1..12)
        val beatSamples = (RATE * 60.0 / bpm).roundToInt()
        return ShortArray(beatSamples * beats).also { pcm ->
            // A short broadband wooden tick, deliberately without a sustained tonal tail.
            // Deterministic noise permits exact detector regression fixtures.
            repeat(beats) { beat ->
                var noise = 0x1234567
                val length = RATE * 6 / 1000
                repeat(length) { i ->
                    noise = noise xor (noise shl 13); noise = noise xor (noise ushr 17); noise = noise xor (noise shl 5)
                    val envelope = (length - i).toDouble() / length
                    val amplitude = if (beat == 0) 7000 else 4500
                    pcm[beat * beatSamples + i] = ((noise.toDouble() / Int.MAX_VALUE) * amplitude * envelope * envelope).toInt().toShort()
                }
            }
        }
    }
}
