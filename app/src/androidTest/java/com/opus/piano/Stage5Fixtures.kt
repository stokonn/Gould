package com.opus.piano

import com.opus.piano.data.offline.BundleFile
import com.opus.piano.data.offline.BundleManifest
import com.opus.piano.data.offline.BundleProgress
import com.opus.piano.data.offline.BundleRights
import com.opus.piano.data.offline.BundleState
import com.opus.piano.data.offline.BundleStoreLogic
import com.opus.piano.data.offline.BundleType
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Shared fixtures for the Stage 5 instrumentation suites. Endpoints are in-process fakes:
 * no real network, no provider traffic, deterministic bytes.
 */
internal object Stage5Fixtures {

    fun shaOf(bytes: ByteArray): String = BundleStoreLogic.sha256(bytes)

    fun bundleFile(
        content: ByteArray,
        relativePath: String = "media/fixture.jpg",
        url: String? = "https://commons.wikimedia.org/wiki/File:Stage5Fixture.jpg",
        license: String = "CC0 1.0"
    ) = BundleFile(
        sha256 = shaOf(content),
        bytes = content.size.toLong(),
        mime = "image/jpeg",
        relativePath = relativePath,
        language = "en",
        rights = BundleRights(
            license = license,
            licenseUrl = "https://creativecommons.org/publicdomain/zero/1.0/",
            attribution = "Open Opus contributors",
            sourceName = "Wikimedia Commons",
            sourceUrl = url
        )
    )

    fun manifest(
        bundleId: String = UUID.randomUUID().toString(),
        revision: Long = 1L,
        files: List<BundleFile>,
        state: BundleState = BundleState.QUEUED,
        attribution: String = "Open Opus contributors",
        minReaderVersion: Int = BundleManifest.READER_VERSION,
        now: Long = System.currentTimeMillis()
    ) = BundleManifest(
        bundleId = bundleId,
        type = BundleType.WORK,
        entityId = "entity-$bundleId",
        schemaVersion = 1,
        revision = revision,
        language = "en",
        createdAt = now,
        validatedAt = if (state == BundleState.READY) now else null,
        dependencies = emptyList(),
        expectedBytes = files.sumOf { it.bytes },
        files = files,
        providerKind = "openopus",
        providerRevision = "fixture",
        attribution = attribution,
        minReaderVersion = minReaderVersion,
        state = state,
        progress = BundleProgress(),
        lastError = null,
        updatedAt = now
    )

    /** Deterministic in-process "https" endpoint; never opens a socket. */
    class FakeEndpoint(
        private val body: ByteArray,
        private val status: Int = 200,
        private val chunkSize: Int = 8 * 1024,
        private val onRead: () -> Unit = {},
        private val failAfterReads: Int = -1
    ) : HttpURLConnection(URL("https://commons.wikimedia.org/wiki/File:Stage5Fixture.jpg")) {
        @Volatile var reads = 0
        @Volatile var hangUntilClosed = false
        private val closed = CountDownLatch(1)

        override fun connect() {}
        override fun disconnect() { closed.countDown() }
        override fun usingProxy() = false
        override fun getRequestMethod() = "GET"
        override fun getResponseCode() = status
        override fun getContentLengthLong() = body.size.toLong()

        override fun getInputStream(): InputStream = object : InputStream() {
            private var position = 0
            override fun read(): Int {
                if (position >= body.size) return -1
                val value = body[position].toInt() and 0xff
                position++
                afterRead()
                return value
            }
            override fun read(b: ByteArray, off: Int, len: Int): Int {
                if (hangUntilClosed) {
                    closed.await(60, TimeUnit.SECONDS)
                    throw IOException("endpoint closed")
                }
                if (position >= body.size) return -1
                val count = minOf(len, chunkSize, body.size - position)
                System.arraycopy(body, position, b, off, count)
                position += count
                afterRead()
                return count
            }
            private fun afterRead() {
                reads++
                onRead()
                if (failAfterReads in 0 until reads) throw IOException("authored connection drop")
            }
        }
    }

    /** Polls a condition on the test thread; throws with context when the deadline passes. */
    suspend fun waitFor(
        what: String,
        timeoutMs: Long = 60_000,
        condition: suspend () -> Boolean
    ) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (condition()) return
            kotlinx.coroutines.delay(250)
        }
        throw AssertionError("timed out waiting for: $what")
    }
}
