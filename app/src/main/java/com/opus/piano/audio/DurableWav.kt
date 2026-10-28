package com.opus.piano.audio

import java.io.File
import java.io.RandomAccessFile
import java.nio.file.Files
import java.security.MessageDigest

/** Streaming mono PCM16 file. A crash only invalidates the repairable header;
 * captured samples remain in the private staging file. Never replaces an existing file. */
class DurableWav private constructor(
    val staging: File, val sampleRate: Int, private val maxDataBytes: Long,
    private val availableBytes: () -> Long, private val reserveBytes: Long
) : AutoCloseable {
    private val file = RandomAccessFile(staging, "rw")
    private var closed = false
    private var syncedBytes = 0L
    val sampleCount: Long get() = ((file.length() - HEADER).coerceAtLeast(0)) / 2

    fun append(samples: ShortArray, count: Int) {
        check(!closed); require(count in 1..samples.size)
        val bytes = Math.multiplyExact(count, 2)
        val current = file.length() - HEADER
        require(current >= 0 && current % 2 == 0L)
        check(current <= maxDataBytes - bytes) { "Recording size limit reached" }
        check(availableBytes() >= reserveBytes + bytes) { "Recording storage reserve reached" }
        val buffer = ByteArray(bytes)
        repeat(count) { i -> buffer[2*i] = samples[i].toByte(); buffer[2*i+1] = (samples[i].toInt() shr 8).toByte() }
        file.seek(file.length()); file.write(buffer)
        if (file.length() - syncedBytes >= sampleRate * 2L) { file.fd.sync(); syncedBytes = file.length() }
    }

    data class Saved(val file: File, val samples: Long, val sha256: String)
    fun finish(destination: File): Saved {
        check(!closed); require(destination.parentFile?.canonicalFile == staging.parentFile?.canonicalFile)
        check(!destination.exists()) { "Recording destination already exists" }
        val dataBytes = file.length() - HEADER
        require(dataBytes in 2..maxDataBytes && dataBytes % 2 == 0L) { "Incomplete recording retained for recovery" }
        file.seek(0); file.write(header(sampleRate, dataBytes)); file.fd.sync()
        // Header is small and deterministic. Validate it before making the file visible.
        val actual = ByteArray(HEADER); file.seek(0); file.readFully(actual)
        check(actual.contentEquals(header(sampleRate, dataBytes)))
        val digest = hash(staging)
        close()
        // Atomic no-clobber publication. ATOMIC_MOVE may replace a racing destination
        // on some providers even without REPLACE_EXISTING; link creation cannot.
        Files.createLink(destination.toPath(), staging.toPath())
        Files.delete(staging.toPath())
        return Saved(destination, dataBytes / 2, digest)
    }

    override fun close() { if (!closed) { try { file.fd.sync() } finally { file.close(); closed = true } } }

    companion object {
        const val HEADER = 44
        const val MAX_DATA_BYTES = 2_000_000_000L
        fun create(staging: File, sampleRate: Int, maxDataBytes: Long = MAX_DATA_BYTES,
                   availableBytes: () -> Long = { staging.parentFile!!.usableSpace }, reserveBytes: Long = 16L*1024*1024): DurableWav {
            require(sampleRate in 8000..96000 && maxDataBytes in 2..MAX_DATA_BYTES && reserveBytes >= 0)
            staging.parentFile!!.mkdirs()
            check(availableBytes() >= reserveBytes + HEADER)
            check(staging.createNewFile()) { "Existing capture retained" }
            return DurableWav(staging,sampleRate,maxDataBytes,availableBytes,reserveBytes).also { it.file.write(header(sampleRate,0)); it.file.fd.sync() }
        }
        /** Explicit user recovery; never opens a microphone or changes sample bytes. */
        fun recover(staging: File, sampleRate: Int): DurableWav {
            require(staging.isFile && staging.length() >= HEADER && sampleRate in 8000..96000)
            return DurableWav(staging,sampleRate,MAX_DATA_BYTES,{ staging.parentFile!!.usableSpace },0)
        }
        fun header(rate: Int, dataBytes: Long): ByteArray {
            require(rate in 8000..96000 && dataBytes in 0..MAX_DATA_BYTES && dataBytes % 2 == 0L)
            val b = java.nio.ByteBuffer.allocate(HEADER).order(java.nio.ByteOrder.LITTLE_ENDIAN)
            b.put("RIFF".toByteArray(Charsets.US_ASCII));b.putInt((dataBytes+36).toInt());b.put("WAVEfmt ".toByteArray(Charsets.US_ASCII))
            b.putInt(16);b.putShort(1);b.putShort(1);b.putInt(rate);b.putInt(rate*2);b.putShort(2);b.putShort(16)
            b.put("data".toByteArray(Charsets.US_ASCII));b.putInt(dataBytes.toInt());return b.array()
        }
        fun hash(file: File): String {
            val digest=MessageDigest.getInstance("SHA-256")
            file.inputStream().use { input -> val bytes=ByteArray(64*1024);while(true){val n=input.read(bytes);if(n<0)break;digest.update(bytes,0,n)} }
            return digest.digest().joinToString(""){"%02x".format(it)}
        }
    }
}
