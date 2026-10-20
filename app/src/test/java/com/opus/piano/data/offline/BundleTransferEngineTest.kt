package com.opus.piano.data.offline

import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files

/**
 * Streaming transfer engine regression tests (D7): hard byte caps, per-chunk policy
 * rechecks (consent revocation, Wi-Fi to mobile, Data Saver), storage admission,
 * hash/length verification and source rights — all executed on the JVM with a fake
 * connection, so no emulator and no real network is involved.
 */
class BundleTransferEngineTest {

    private val content = "Stage 5 offline bundle payload".toByteArray()
    private val sha = BundleStoreLogic.sha256(content)
    private val tempDirs = mutableListOf<File>()

    @After
    fun cleanup() {
        tempDirs.forEach { it.deleteRecursively() }
    }

    private val wifi = NetworkFacts(connected = true, validated = true, unmetered = true, metered = false)
    private val mobile = NetworkFacts(connected = true, validated = true, unmetered = false, metered = true)

    private fun file(
        bytes: ByteArray = content,
        declaredSha: String = sha,
        url: String? = "https://commons.wikimedia.org/wiki/File:Stage5.jpg",
        license: String = "CC0 1.0"
    ) = BundleFile(
        sha256 = declaredSha,
        bytes = bytes.size.toLong(),
        mime = "image/jpeg",
        relativePath = "media/stage5.jpg",
        language = "en",
        rights = BundleRights(
            license = license,
            licenseUrl = "https://creativecommons.org/publicdomain/zero/1.0/",
            attribution = "Open Opus contributors",
            sourceName = "Wikimedia Commons",
            sourceUrl = url
        )
    )

    /** Fake HTTPS endpoint: bounded chunks, optional per-read hook, no real sockets. */
    private class FakeConnection(
        private val body: ByteArray,
        private val status: Int = 200,
        private val declaredLength: Long? = null,
        private val chunkSize: Int = 4,
        private val onRead: () -> Unit = {}
    ) : HttpURLConnection(URL("https://commons.wikimedia.org/wiki/File:Stage5.jpg")) {
        var reads = 0
        override fun connect() {}
        override fun disconnect() {}
        override fun usingProxy() = false
        override fun getRequestMethod() = "GET"
        override fun getResponseCode() = status
        override fun getContentLengthLong() = declaredLength ?: body.size.toLong()
        override fun getInputStream(): InputStream = object : InputStream() {
            private var position = 0
            override fun read(): Int {
                if (position >= body.size) return -1
                val value = body[position].toInt() and 0xff
                position++
                reads++
                onRead()
                return value
            }
            override fun read(b: ByteArray, off: Int, len: Int): Int {
                if (position >= body.size) return -1
                val count = minOf(len, chunkSize, body.size - position)
                System.arraycopy(body, position, b, off, count)
                position += count
                reads++
                onRead()
                return count
            }
        }
    }

    private class Harness(
        var facts: NetworkFacts,
        var allowance: Boolean,
        var admit: Boolean
    ) {
        val progress = mutableListOf<Long>()
        val staging: File = Files.createTempDirectory("stage5-engine").toFile()
        var connection: FakeConnection? = null
        var connect: (URL) -> HttpURLConnection = { throw IllegalStateException("no endpoint configured") }

        fun engine(gateEveryBytes: Long = 4L) = BundleTransferEngine(
            stagingDir = staging,
            network = { facts },
            meteredAllowed = { allowance },
            admission = { admit },
            connect = { url -> connect(url).also { connection = it as? FakeConnection } },
            onProgress = { written -> progress += written },
            gateEveryBytes = gateEveryBytes
        )
    }

    private fun harness(
        facts: NetworkFacts = wifi,
        allowance: Boolean = false,
        admit: Boolean = true,
        connect: (URL) -> HttpURLConnection
    ): Harness = Harness(facts, allowance, admit).also {
        tempDirs += it.staging
        it.connect = connect
    }

    private suspend fun failure(engine: BundleTransferEngine, target: BundleFile): Exception =
        try {
            engine.fetch(target)
            throw AssertionError("expected a transfer failure")
        } catch (e: Exception) {
            e
        }

    // ------------------------------------------------------------ happy path

    @Test
    fun verifiedContentIsStagedWithMonotonicProgress() = runBlocking {
        val harness = harness { FakeConnection(content) }
        val staged = harness.engine().fetch(file())
        assertEquals(sha, BundleStoreLogic.sha256Of(staged))
        assertEquals(content.size.toLong(), staged.length())
        assertTrue(harness.progress.isNotEmpty())
        assertEquals(content.size.toLong(), harness.progress.last())
        assertEquals("progress must never decrease", harness.progress, harness.progress.sorted())
        assertEquals("one staged file for one declared file", 1, harness.staging.listFiles()!!.size)
    }

    @Test
    fun interruptedStagedFileIsRestartedNotResumed() = runBlocking {
        val harness = harness { FakeConnection(content) }
        val partial = File(harness.staging, "$sha${BundleStore.STAGED_SUFFIX}")
        partial.writeBytes(ByteArray(7)) // crash garbage from an earlier attempt
        val staged = harness.engine().fetch(file())
        assertEquals("a fresh verified file replaces the stale partial", sha, BundleStoreLogic.sha256Of(staged))
    }

    // ------------------------------------------------------------ policy gates

    @Test
    fun consentRevocationMidStreamStopsWithinOneGateWindowAndDropsThePartial() = runBlocking {
        val harness = harness(facts = mobile, allowance = true) {
            FakeConnection(content)
        }
        var reads = 0
        harness.connect = {
            FakeConnection(content, onRead = {
                reads++
                if (reads >= 3) harness.allowance = false
            })
        }
        val error = failure(harness.engine(), file())
        assertTrue("expected pause, got $error", error is BundleTransferError.Paused)
        assertEquals(BundlePolicy.Reasons.CONSENT, (error as BundleTransferError.Paused).code)
        assertTrue("partial must not survive as if it were content", harness.staging.listFiles().isEmpty())
        assertTrue("no unbounded reads after revocation, read=$reads", reads <= 5)
    }

    @Test
    fun wifiToMobileTransitionMidStreamStopsAJobWithoutConsent() = runBlocking {
        val harness = harness { FakeConnection(content) }
        var reads = 0
        harness.connect = {
            FakeConnection(content, onRead = {
                reads++
                if (reads >= 2) harness.facts = mobile
            })
        }
        val error = failure(harness.engine(), file())
        assertTrue(error is BundleTransferError.Paused)
        assertEquals(BundlePolicy.Reasons.CONSENT, (error as BundleTransferError.Paused).code)
        assertTrue(harness.staging.listFiles().isEmpty())
    }

    @Test
    fun offlineNetworkNeverOpensAConnection() = runBlocking {
        var opened = 0
        val harness = harness(facts = NetworkFacts.DISCONNECTED) {
            opened++
            FakeConnection(content)
        }
        val error = failure(harness.engine(), file())
        assertTrue(error is BundleTransferError.Paused)
        assertEquals(BundlePolicy.Reasons.OFFLINE, (error as BundleTransferError.Paused).code)
        assertEquals(0, opened)
    }

    @Test
    fun dataSaverOnMeteredNetworkStopsEvenWithConsent() = runBlocking {
        val harness = harness(facts = mobile.copy(backgroundRestricted = true), allowance = true) {
            FakeConnection(content)
        }
        val error = failure(harness.engine(), file())
        assertTrue(error is BundleTransferError.Paused)
        assertEquals(BundlePolicy.Reasons.DATA_SAVER, (error as BundleTransferError.Paused).code)
    }

    @Test
    fun storageAdmissionRefusalNeverOpensAConnection() = runBlocking {
        var opened = 0
        val harness = harness(admit = false) {
            opened++
            FakeConnection(content)
        }
        val error = failure(harness.engine(), file())
        assertTrue(error is BundleTransferError.Storage)
        assertEquals(0, opened)
    }

    // ------------------------------------------------------------ integrity and rights

    @Test
    fun sourceReturningMoreThanDeclaredIsRejected() = runBlocking {
        val harness = harness { FakeConnection(content + ByteArray(8)) }
        val error = failure(harness.engine(), file())
        assertTrue(error is BundleTransferError.Integrity)
        assertTrue(harness.staging.listFiles().isEmpty())
    }

    @Test
    fun lyingContentLengthHeaderIsRejectedBeforeStreaming() = runBlocking {
        val harness = harness { FakeConnection(content, declaredLength = content.size + 1000L) }
        val error = failure(harness.engine(), file())
        assertTrue(error is BundleTransferError.Integrity)
    }

    @Test
    fun shortResponseIsRejectedAndDropped() = runBlocking {
        val harness = harness { FakeConnection(content.copyOf(content.size - 5)) }
        val error = failure(harness.engine(), file())
        assertTrue(error is BundleTransferError.Integrity)
        assertTrue(harness.staging.listFiles().isEmpty())
    }

    @Test
    fun hashMismatchIsNeverStaged() = runBlocking {
        val impostorSha = BundleStoreLogic.sha256("a different payload".toByteArray())
        val harness = harness { FakeConnection(content) }
        val error = failure(harness.engine(), file(declaredSha = impostorSha))
        assertTrue(error is BundleTransferError.Integrity)
        assertTrue(harness.staging.listFiles().isEmpty())
    }

    @Test
    fun blankLicenseIsARightsFailure() = runBlocking {
        val harness = harness { FakeConnection(content) }
        val error = failure(harness.engine(), file(license = ""))
        assertTrue(error is BundleTransferError.Rights)
    }

    @Test
    fun missingOrUnapprovedSourcesArePermanent() = runBlocking {
        var opened = 0
        val harness = harness {
            opened++
            FakeConnection(content)
        }
        assertTrue(failure(harness.engine(), file(url = null)) is BundleTransferError.Source)
        assertTrue(failure(harness.engine(), file(url = "https://evil.example.com/stage5.jpg")) is BundleTransferError.Source)
        assertEquals("no connection for unapproved sources", 0, opened)
    }

    @Test
    fun httpStatusesSeparateTransientFromPermanentFailures() = runBlocking {
        val notFound = harness { FakeConnection(content, status = 404) }
        assertTrue(failure(notFound.engine(), file()) is BundleTransferError.Source)
        val serverError = harness { FakeConnection(content, status = 503) }
        assertTrue(failure(serverError.engine(), file()) is BundleTransferError.Transport)
        val overload = harness { FakeConnection(content, status = 429) }
        assertTrue(failure(overload.engine(), file()) is BundleTransferError.Transport)
    }

    // ------------------------------------------------------------ source policy

    @Test
    fun sourceUrlPolicyRejectsEverythingButApprovedHttpsProviders() {
        val approved = BundleTransferEngine.requireSourceUrl("https://upload.wikimedia.org/wikipedia/x.jpg")
        assertEquals("https", approved.protocol)
        assertEquals("upload.wikimedia.org", approved.host)
        for (bad in listOf(
            "http://commons.wikimedia.org/x.jpg",
            "https://evil.example.com/x.jpg",
            "https://commons.wikimedia.org:8443/x.jpg",
            "https://user:pass@commons.wikimedia.org/x.jpg",
            "file:///etc/passwd"
        )) {
            val error = assertThrows("must reject $bad", BundleTransferError.Source::class.java) {
                BundleTransferEngine.requireSourceUrl(bad)
            }
            assertTrue(error.message!!.contains("approved"))
        }
    }
}
