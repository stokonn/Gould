package com.opus.piano.audio

import kotlin.math.*

/** Experimental tonal/onset heuristic. Deliberately no speech model or piece identification. */
class PianoActivityDetector {
    private var noise = 0.002
    private var previousEnergy = 0.0
    private var smooth = 0.0
    private val real = DoubleArray(2048)
    private val imag = DoubleArray(2048)
    fun likely(samples: ShortArray, count: Int, sensitivity: Int): Boolean {
        require(count in 1..samples.size)
        val mean = (0 until count).sumOf { samples[it].toDouble() } / count
        var energy = 0.0
        for (i in real.indices) {
            val v = if (i < count) (samples[i] - mean) / 32768.0 else 0.0
            energy += v * v
            real[i] = v * (0.5 - 0.5 * cos(2 * PI * i / (real.size - 1)))
            imag[i] = 0.0
        }
        val rms = sqrt(energy / count)
        fft()
        var sum = 0.0; var log = 0.0; var peak = 0.0
        for (i in 4 until 900) {
            val power = real[i] * real[i] + imag[i] * imag[i] + 1e-12
            sum += power; log += ln(power); peak = max(peak, power)
        }
        val flatness = exp(log / 896) / (sum / 896 + 1e-12)
        val tonal = flatness < .18 && peak / (sum + 1e-12) > .035
        val onset = rms > previousEnergy * 1.12
        val threshold = noise * (4.5 - sensitivity.coerceIn(0,100) * .025)
        val signal = rms > max(.0015, threshold) && tonal && (onset || smooth > .25)
        smooth = smooth * .65 + (if (signal) 1.0 else 0.0) * .35
        if (!signal && smooth < .2) noise = noise * .995 + min(rms, .025) * .005
        previousEnergy = rms
        return smooth > .42
    }
    private fun fft() {
        val n = real.size
        var j = 0
        for (i in 1 until n) {
            var bit = n shr 1
            while (j and bit != 0) { j = j xor bit; bit = bit shr 1 }
            j = j xor bit
            if (i < j) { val r = real[i]; real[i] = real[j]; real[j] = r; val v = imag[i]; imag[i] = imag[j]; imag[j] = v }
        }
        var len = 2
        while (len <= n) {
            val angle = -2 * PI / len
            for (offset in 0 until n step len) {
                var wr = 1.0; var wi = 0.0
                for (k in 0 until len / 2) {
                    val a = offset + k; val b = a + len / 2
                    val tr = real[b] * wr - imag[b] * wi; val ti = real[b] * wi + imag[b] * wr
                    real[b] = real[a] - tr; imag[b] = imag[a] - ti; real[a] += tr; imag[a] += ti
                    val next = wr * cos(angle) - wi * sin(angle); wi = wr * sin(angle) + wi * cos(angle); wr = next
                }
            }
            len *= 2
        }
    }
}
