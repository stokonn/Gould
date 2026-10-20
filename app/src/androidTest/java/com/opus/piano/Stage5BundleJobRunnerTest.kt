package com.opus.piano

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.data.offline.BundleJob
import com.opus.piano.data.offline.BundleJobPreferences
import com.opus.piano.data.offline.BundleJobRunner
import com.opus.piano.data.offline.BundleJobStore
import com.opus.piano.data.offline.BundleManifest
import com.opus.piano.data.offline.BundleManifestCodec
import com.opus.piano.data.offline.BundlePolicy
import com.opus.piano.data.offline.BundleState
import com.opus.piano.data.offline.BundleStore
import com.opus.piano.data.offline.BundleStoreLogic
import com.opus.piano.data.offline.NetworkFacts
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/**
 * On-device pipeline tests for [BundleJobRunner] with the real file store (AtomicFile) and
 * deterministic in-process endpoints: full download/publish, resume after interruption,
 * refresh safety, consent revocation mid-stream, Data Saver, offline, low storage, corrupt
 * content, rights quarantine and concurrent runs sharing one dependency.
 */
@RunWith(AndroidJUnit4::class)
class Stage5BundleJobRunnerTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val wifi = NetworkFacts(connected = true, validated = true, unmetered = true, metered = false)
    private val mobile = NetworkFacts(connected = true, validated = true, unmetered = false, metered = true)

    private class MemoryPreferences : BundleJobPreferences {
        private val values = linkedMapOf<String, String>()
        override fun read(key: String): String? = values[key]
        override fun write(key: String, value: String?) {
            if (value == null) values.remove(key) else values[key] = value
        }
        override fun keys(): Set<String> = values.keys.toSet()
    }

    private class Harness(
        val root: File,
        val store: BundleStore,
        val jobs: BundleJobStore,
        var facts: NetworkFacts,
        var allowance: Boolean,
        val endpoint: (URL) -> HttpURLConnection
    ) {
        val runner = BundleJobRunner(
            store, jobs,
            network = { facts },
            stagingDir = File(root, "staging"),
            connect = endpoint
        )
        val blobsDir get() = File(root, "bundles/blobs")

        suspend fun published(bundleId: String): BundleManifest? =
            store.readPublished().manifests.firstOrNull { it.bundleId == bundleId }

        suspend fun stateOf(bundleId: String, revision: Long = 1): BundleState? {
            val all = store.readAll().manifests
            return all.firstOrNull { it.bundleId == bundleId && it.revision == revision }?.state
        }
    }

    private val harnesses = mutableListOf<Harness>()

    private fun harness(
        facts: NetworkFacts = wifi,
        allowance: Boolean = false,
        reserve: () -> Long = { 0L },
        jobs: BundleJobStore = BundleJobStore(MemoryPreferences()),
        endpoint: (URL) -> HttpURLConnection
    ): Harness {
        val root = File(context.cacheDir, "stage5-runner-${UUID.randomUUID()}").apply { mkdirs() }
        val store = BundleStore(
            File(root, "bundles"), File(root, "staging"),
            reserveBytes = reserve, otherUsedBytes = { 0L }
        )
        val h = Harness(root, store, jobs, facts, allowance, endpoint)
        harnesses += h
        return h
    }

    @After
    fun cleanup() {
        harnesses.forEach { it.root.deleteRecursively() }
    }

    private fun jobFor(manifest: BundleManifest, meteredAllowed: Boolean = false) = BundleJob(
        bundleId = manifest.bundleId,
        revision = manifest.revision,
        estimatedBytes = manifest.expectedBytes,
        confirmed = true,
        meteredAllowed = meteredAllowed,
        active = true,
        attempts = 0,
        createdAt = 1L,
        updatedAt = 1L,
        lastError = null
    )

    private suspend fun runJob(h: Harness, manifest: BundleManifest, meteredAllowed: Boolean = false): BundleJobRunner.Outcome {
        h.jobs.put(jobFor(manifest, meteredAllowed))
        return h.runner.run(h.jobs.get(manifest.jobIdOf)!!)
    }

    private val BundleManifest.jobIdOf: String get() = BundleJob.id(bundleId, revision)

    // ------------------------------------------------------------ happy path

    @Test
    fun queuedBundleDownloadsValidatesAndPublishesAtomically() = runBlocking {
        val content = "complete bundle payload".toByteArray()
        val file = Stage5Fixtures.bundleFile(content)
        val h = harness { Stage5Fixtures.FakeEndpoint(content) }
        val manifest = Stage5Fixtures.manifest(files = listOf(file))
        h.store.save(manifest)

        val outcome = runJob(h, manifest)

        assertEquals(BundleJobRunner.Outcome.Completed, outcome)
        val published = h.published(manifest.bundleId)!!
        assertEquals(BundleState.READY, published.state)
        assertEquals("offline attribution survives publishing", "Open Opus contributors", published.attribution)
        assertTrue(published.validatedAt != null)
        assertTrue(h.store.blobFile(file.sha256).isFile)
        assertEquals(content.size.toLong(), h.store.blobFile(file.sha256).length())
        assertTrue("staging is empty after a successful run", File(h.root, "staging").listFiles().isEmpty())
        val job = h.jobs.get(manifest.jobIdOf)!!
        assertFalse("finished jobs never auto-restart", job.active)
        assertNull(job.lastError)
        assertEquals(content.size.toLong(), published.progress.completedBytes)
    }

    // ------------------------------------------------------------ interruption and resume

    @Test
    fun interruptedDownloadLeavesRetryableStateAndCompletesOnRetry() = runBlocking {
        val content = "interrupted download payload".toByteArray()
        val file = Stage5Fixtures.bundleFile(content)
        var failNext = true
        val h = harness {
            // Small chunks so the authored drop lands mid-stream (after 2 of 4 reads).
            Stage5Fixtures.FakeEndpoint(
                content, chunkSize = 8,
                failAfterReads = if (failNext) 2 else -1
            ).also { failNext = false }
        }
        val manifest = Stage5Fixtures.manifest(files = listOf(file))
        h.store.save(manifest)

        val first = runJob(h, manifest)
        assertTrue("expected retryable outcome, got $first", first is BundleJobRunner.Outcome.Retryable)
        assertEquals(BundleState.FAILED_RETRYABLE, h.stateOf(manifest.bundleId))
        assertNull("a partial bundle is never published", h.published(manifest.bundleId))
        assertTrue("partial staging is disposable", File(h.root, "staging").listFiles().isEmpty())
        assertEquals(1, h.jobs.get(manifest.jobIdOf)!!.attempts)

        val second = runJob(h, manifest)
        assertEquals(BundleJobRunner.Outcome.Completed, second)
        assertEquals(BundleState.READY, h.stateOf(manifest.bundleId))
        assertTrue(h.store.blobFile(file.sha256).isFile)
    }

    @Test
    fun interruptedRefreshKeepsTheOldReadyRevisionAndPublishesWhenComplete() = runBlocking {
        val oldContent = "previous ready revision".toByteArray()
        val newContent = "new revision payload".toByteArray()
        val oldFile = Stage5Fixtures.bundleFile(oldContent)
        val newFile = Stage5Fixtures.bundleFile(newContent, relativePath = "media/new.jpg")
        var failing = true
        val h = harness {
            // Small chunks so the authored drop lands mid-stream (after 1 of 3 reads).
            Stage5Fixtures.FakeEndpoint(
                newContent, chunkSize = 8,
                failAfterReads = if (failing) 1 else -1
            ).also { failing = false }
        }
        // Published READY revision 1.
        h.store.save(Stage5Fixtures.manifest(files = listOf(oldFile), state = BundleState.READY))
        val oldStaged = File(h.root, "staging/${oldFile.sha256}.part")
        oldStaged.writeBytes(oldContent)
        assertTrue(h.store.installBlob(oldStaged, oldFile.sha256, oldContent.size.toLong()))

        // Refresh revision 2 fails mid-transfer.
        val refresh = Stage5Fixtures.manifest(revision = 2, files = listOf(newFile))
        h.store.save(refresh)
        val outcome = runJob(h, refresh)
        assertTrue(outcome is BundleJobRunner.Outcome.Retryable)

        val stillReady = h.store.readPublished().manifests.single()
        assertEquals("old READY revision survives the interrupted refresh", 1L, stillReady.revision)
        assertEquals(BundleState.READY, stillReady.state)
        assertTrue(h.store.blobFile(oldFile.sha256).isFile)

        // Retry completes the refresh; promotion is atomic and reclaims revision 1.
        val retry = runJob(h, refresh.copy(state = BundleState.FAILED_RETRYABLE))
        assertEquals(BundleJobRunner.Outcome.Completed, retry)
        val promoted = h.store.readPublished().manifests.single()
        assertEquals(2L, promoted.revision)
        assertEquals(BundleState.READY, promoted.state)
        assertTrue(h.store.blobFile(newFile.sha256).isFile)
        assertFalse("old revision content reclaimed after promotion", h.store.blobFile(oldFile.sha256).exists())
    }

    @Test
    fun inFlightStatesFromProcessDeathResumeLegally() = runBlocking {
        val contentA = "crash during download".toByteArray()
        val fileA = Stage5Fixtures.bundleFile(contentA)
        val hA = harness { Stage5Fixtures.FakeEndpoint(contentA) }
        val downloading = Stage5Fixtures.manifest(files = listOf(fileA), state = BundleState.DOWNLOADING)
        hA.store.save(downloading)
        assertEquals(BundleJobRunner.Outcome.Completed, runJob(hA, downloading))
        assertEquals(BundleState.READY, hA.stateOf(downloading.bundleId))

        // Crash during validation: all bytes present, state in flight.
        val contentB = "crash during validation".toByteArray()
        val fileB = Stage5Fixtures.bundleFile(contentB, relativePath = "media/b.jpg")
        val hB = harness { Stage5Fixtures.FakeEndpoint(contentB) }
        val validating = Stage5Fixtures.manifest(files = listOf(fileB), state = BundleState.VALIDATING)
        hB.store.save(validating)
        val stagedB = File(hB.root, "staging/${fileB.sha256}.part")
        stagedB.writeBytes(contentB)
        assertTrue(hB.store.installBlob(stagedB, fileB.sha256, contentB.size.toLong()))
        assertEquals(BundleJobRunner.Outcome.Completed, runJob(hB, validating))
        assertEquals(BundleState.READY, hB.stateOf(validating.bundleId))
    }

    // ------------------------------------------------------------ consent, network, storage

    @Test
    fun consentRevocationMidStreamPausesAndKeepsReadyContent() = runBlocking {
        val oldContent = "pinned ready bytes".toByteArray()
        val newContent = "metered refresh bytes".toByteArray()
        val oldFile = Stage5Fixtures.bundleFile(oldContent)
        val newFile = Stage5Fixtures.bundleFile(newContent, relativePath = "media/new.jpg")
        // The refresh manifest and the job store are created before the harness so the
        // endpoint callback can revoke consent for exactly this job while bytes stream.
        val refresh = Stage5Fixtures.manifest(revision = 2, files = listOf(newFile))
        val jobs = BundleJobStore(MemoryPreferences())
        val h = harness(facts = mobile, allowance = true, jobs = jobs) {
            Stage5Fixtures.FakeEndpoint(
                newContent,
                onRead = { jobs.revokeMetered(BundleJob.id(refresh.bundleId, refresh.revision)) }
            )
        }
        // Published READY revision 1.
        h.store.save(Stage5Fixtures.manifest(files = listOf(oldFile), state = BundleState.READY))
        val oldStaged = File(h.root, "staging/${oldFile.sha256}.part")
        oldStaged.writeBytes(oldContent)
        assertTrue(h.store.installBlob(oldStaged, oldFile.sha256, oldContent.size.toLong()))

        h.store.save(refresh)

        val outcome = runJob(h, refresh, meteredAllowed = true)
        assertTrue("expected pause, got $outcome", outcome is BundleJobRunner.Outcome.Paused)
        assertEquals(BundlePolicy.Reasons.CONSENT, (outcome as BundleJobRunner.Outcome.Paused).error)
        assertEquals(BundleState.PAUSED, h.stateOf(refresh.bundleId, revision = 2))
        assertFalse("revocation is durable", h.jobs.get(refresh.jobIdOf)!!.meteredAllowed)
        assertEquals("prior ready content is never deleted", 1L, h.published(refresh.bundleId)!!.revision)
        assertTrue(h.store.blobFile(oldFile.sha256).isFile)
        assertFalse(h.store.blobFile(newFile.sha256).exists())
        assertTrue(File(h.root, "staging").listFiles().isEmpty())
    }

    @Test
    fun offlineNetworkWaitsWithoutMovingBytes() = runBlocking {
        var opened = 0
        val content = "offline payload".toByteArray()
        val h = harness(facts = NetworkFacts.DISCONNECTED) {
            opened++
            Stage5Fixtures.FakeEndpoint(content)
        }
        val manifest = Stage5Fixtures.manifest(files = listOf(Stage5Fixtures.bundleFile(content)))
        h.store.save(manifest)
        val outcome = runJob(h, manifest)
        assertTrue(outcome is BundleJobRunner.Outcome.Waiting)
        assertEquals(BundlePolicy.Reasons.OFFLINE, (outcome as BundleJobRunner.Outcome.Waiting).error)
        assertEquals(BundleState.WAITING_FOR_POLICY, h.stateOf(manifest.bundleId))
        assertEquals("not a single byte may move while offline", 0, opened)
    }

    @Test
    fun dataSaverPausesEvenWithPerJobConsent() = runBlocking {
        val content = "data saver payload".toByteArray()
        var opened = 0
        val h = harness(facts = mobile.copy(backgroundRestricted = true), allowance = true) {
            opened++
            Stage5Fixtures.FakeEndpoint(content)
        }
        val manifest = Stage5Fixtures.manifest(files = listOf(Stage5Fixtures.bundleFile(content)))
        h.store.save(manifest)
        val outcome = runJob(h, manifest, meteredAllowed = true)
        assertTrue(outcome is BundleJobRunner.Outcome.Waiting)
        assertEquals(BundlePolicy.Reasons.DATA_SAVER, (outcome as BundleJobRunner.Outcome.Waiting).error)
        assertEquals(0, opened)
    }

    @Test
    fun lowStorageRefusesBeforeAnyByteMoves() = runBlocking {
        // Reserve leaves room for small durable state writes but refuses the payload itself:
        // with reserve == free space even the manifest write would be admission-refused, so
        // the fixture models the realistic D7 case (state writable, content not).
        val content = ByteArray(1024 * 1024) { (it % 251).toByte() }
        var opened = 0
        val h = harness(reserve = { context.cacheDir.usableSpace - STATE_SLACK_BYTES }) {
            opened++
            Stage5Fixtures.FakeEndpoint(content)
        }
        val manifest = Stage5Fixtures.manifest(files = listOf(Stage5Fixtures.bundleFile(content)))
        h.store.save(manifest)
        val outcome = runJob(h, manifest)
        assertTrue("expected retryable storage stop, got $outcome", outcome is BundleJobRunner.Outcome.Retryable)
        assertEquals(BundlePolicy.Reasons.STORAGE, (outcome as BundleJobRunner.Outcome.Retryable).error)
        assertEquals(BundleState.FAILED_RETRYABLE, h.stateOf(manifest.bundleId))
        assertEquals(0, opened)
        assertTrue("personal export space is protected, not consumed", h.blobsDir.listFiles().isEmpty())
    }

    // ------------------------------------------------------------ integrity, rights, reader

    @Test
    fun corruptSourceContentNeverReachesReady() = runBlocking {
        val declared = "declared payload bytes".toByteArray()
        val impostor = "impostor payload bytes!".toByteArray() // same length, different digest
        val file = Stage5Fixtures.bundleFile(declared)
        val h = harness { Stage5Fixtures.FakeEndpoint(impostor) }
        val manifest = Stage5Fixtures.manifest(files = listOf(file))
        h.store.save(manifest)
        val outcome = runJob(h, manifest)
        assertTrue(outcome is BundleJobRunner.Outcome.Retryable)
        assertEquals(BundleState.FAILED_RETRYABLE, h.stateOf(manifest.bundleId))
        assertNull(h.published(manifest.bundleId))
        assertTrue(File(h.root, "staging").listFiles().isEmpty())
        assertTrue(h.blobsDir.listFiles().isEmpty())
    }

    @Test
    fun invalidRightsOnDiskQuarantinesManifestAndDeactivatesJob() = runBlocking {
        val content = "rights payload".toByteArray()
        val h = harness { Stage5Fixtures.FakeEndpoint(content) }
        val manifest = Stage5Fixtures.manifest(files = listOf(Stage5Fixtures.bundleFile(content)))
        // D5.1 makes invalid rights unreachable through save()/encode() (rejected at write
        // time); simulate on-disk rights corruption instead. The reachable contract: the
        // codec's decode re-validation quarantines the file with bytes preserved, the job
        // deactivates permanently and the bundle never becomes READY.
        val tampered = BundleManifestCodec.encode(manifest)
            .replace(Regex("\"license\":\"[^\"]*\""), "\"license\":\"\"")
        h.store.manifestFile(manifest.bundleId).writeText(tampered)

        val outcome = runJob(h, manifest)
        assertEquals(
            "a rights-corrupt manifest is permanent, not retried",
            BundleJobRunner.Outcome.Permanent(BundlePolicy.Reasons.MANIFEST_MISSING),
            outcome
        )
        assertFalse("permanent jobs are not auto-retried", h.jobs.get(manifest.jobIdOf)!!.active)
        assertEquals(BundlePolicy.Reasons.MANIFEST_MISSING, h.jobs.get(manifest.jobIdOf)!!.lastError)
        assertNull("corrupt manifest never publishes", h.published(manifest.bundleId))
        assertNull("corrupt manifest leaves no active state", h.stateOf(manifest.bundleId))
        val quarantined = File(h.root, "bundles/quarantine").listFiles()
        assertTrue("tampered bytes preserved for inspection", quarantined != null && quarantined.isNotEmpty())
        assertTrue(
            "quarantine holds the tampered manifest",
            quarantined!!.any { it.readText() == tampered }
        )
    }

    @Test
    fun futureReaderRequirementQuarantinesInsteadOfRenderingGarbage() = runBlocking {
        val content = "future reader payload".toByteArray()
        val h = harness { Stage5Fixtures.FakeEndpoint(content) }
        val manifest = Stage5Fixtures.manifest(
            files = listOf(Stage5Fixtures.bundleFile(content)),
            minReaderVersion = BundleManifest.READER_VERSION + 1
        )
        h.store.save(manifest)
        val outcome = runJob(h, manifest)
        assertTrue(outcome is BundleJobRunner.Outcome.Quarantined)
        assertEquals(BundlePolicy.Reasons.READER_VERSION, (outcome as BundleJobRunner.Outcome.Quarantined).error)
        assertTrue(h.blobsDir.listFiles().isEmpty())
    }

    @Test
    fun corruptStoredBlobIsQuarantinedAndRepairedOnRetry() = runBlocking {
        val content = "stored blob payload".toByteArray()
        val file = Stage5Fixtures.bundleFile(content)
        var serveCorrupt = false
        val h = harness { Stage5Fixtures.FakeEndpoint(if (serveCorrupt) "corrupted bytes!".toByteArray() else content) }
        val manifest = Stage5Fixtures.manifest(files = listOf(file))
        h.store.save(manifest)
        assertEquals(BundleJobRunner.Outcome.Completed, runJob(h, manifest))

        // On-disk corruption after publication: the next run detects and quarantines it.
        h.store.blobFile(file.sha256).writeBytes("corrupted bytes!".toByteArray())
        val stale = h.store.readPublished().manifests.single()
        h.store.save(stale.copy(state = BundleState.STALE, updatedAt = System.currentTimeMillis()))
        val staleManifest = h.store.readPublished().manifests.single()
        val repair = runJob(h, staleManifest)
        assertEquals("repair re-downloads and republishes", BundleJobRunner.Outcome.Completed, repair)
        assertEquals(BundleState.READY, h.stateOf(manifest.bundleId))
        assertEquals(content.size.toLong(), h.store.blobFile(file.sha256).length())
        val quarantined = File(h.root, "bundles/quarantine").listFiles()
        assertTrue("corrupt bytes preserved for inspection", quarantined != null && quarantined.isNotEmpty())
    }

    // ------------------------------------------------------------ concurrency

    @Test
    fun concurrentBundlesSharingOneDependencyKeepTheSharedContentSingle() = runBlocking {
        val shared = "shared dependency bytes".toByteArray()
        val ownA = "bundle a bytes".toByteArray()
        val ownB = "bundle b bytes".toByteArray()
        val sharedFile = Stage5Fixtures.bundleFile(shared)
        val fileA = Stage5Fixtures.bundleFile(ownA, relativePath = "media/a.jpg")
        val fileB = Stage5Fixtures.bundleFile(ownB, relativePath = "media/b.jpg")
        val manifestA = Stage5Fixtures.manifest(files = listOf(sharedFile, fileA))
        val manifestB = Stage5Fixtures.manifest(files = listOf(sharedFile, fileB))
        // Own files come from their own endpoints; the shared dependency from the shared one.
        val endpointA = Stage5Fixtures.FakeEndpoint(ownA)
        val endpointB = Stage5Fixtures.FakeEndpoint(ownB)
        val hA = harness { url -> if (endpointA.isConnectedTo(url)) endpointA else Stage5Fixtures.FakeEndpoint(shared) }
        val hB = harness { url -> if (endpointB.isConnectedTo(url)) endpointB else Stage5Fixtures.FakeEndpoint(shared) }
        hA.store.save(manifestA)
        hB.store.save(manifestB)

        val outcomeA = async { runJob(hA, manifestA) }
        val outcomeB = async { runJob(hB, manifestB) }
        assertEquals(BundleJobRunner.Outcome.Completed, outcomeA.await())
        assertEquals(BundleJobRunner.Outcome.Completed, outcomeB.await())
        assertEquals(BundleState.READY, hA.stateOf(manifestA.bundleId))
        assertEquals(BundleState.READY, hB.stateOf(manifestB.bundleId))
        // Each harness stores its own copy; within one store the shared blob exists once.
        assertEquals(2, hA.blobsDir.listFiles()!!.size)
        assertEquals(2, hB.blobsDir.listFiles()!!.size)
        assertTrue(hA.store.blobFile(sharedFile.sha256).isFile)
        assertTrue(hB.store.blobFile(sharedFile.sha256).isFile)
        assertEquals(sharedFile.sha256, BundleStoreLogic.sha256Of(hA.store.blobFile(sharedFile.sha256)))
    }
}

private fun Stage5Fixtures.FakeEndpoint.isConnectedTo(url: URL): Boolean =
    url.host == "commons.wikimedia.org" && url.query == null

/** Free-space headroom that admits manifest/state writes while refusing content payloads. */
private const val STATE_SLACK_BYTES = 512L * 1024
