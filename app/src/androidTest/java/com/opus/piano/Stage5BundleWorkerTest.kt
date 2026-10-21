package com.opus.piano

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import com.opus.piano.data.offline.BundleDownloadWorker
import com.opus.piano.data.offline.BundleJob
import com.opus.piano.data.offline.BundleJobPreferences
import com.opus.piano.data.offline.BundleJobStore
import com.opus.piano.data.offline.BundleManifest
import com.opus.piano.data.offline.BundleNetwork
import com.opus.piano.data.offline.BundlePolicy
import com.opus.piano.data.offline.BundleScheduler
import com.opus.piano.data.offline.BundleState
import com.opus.piano.data.offline.BundleStore
import com.opus.piano.data.offline.NetworkFacts
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

/**
 * Stage 5 WorkManager tests on real infrastructure: the actual [BundleDownloadWorker] executed
 * through work-testing's [TestListenableWorkerBuilder], and [BundleScheduler] bound to a real
 * WorkManager initialized by [WorkManagerTestInitHelper] (test database, synchronous executor,
 * [androidx.work.testing.TestDriver]).
 *
 * Hermetic by construction: network facts and HTTP connections are injected through the
 * `OpusApplication` test seams (`bundleNetworkOverride` / `bundleConnectOverride`), so no
 * provider host, real socket or mobile byte is ever touched. Worker fixtures live in the
 * application's real bundle store and job preferences — the worker is bound to them by design —
 * and are removed again in [tearDown]; scheduler fixtures use isolated stores that cannot
 * touch application data.
 *
 * Requires an Android emulator/device: these tests are compile-verified only until Stage 5
 * task 5.10 executes them on an explicitly identified emulator.
 */
@RunWith(AndroidJUnit4::class)
class Stage5BundleWorkerTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val app: OpusApplication get() = context.applicationContext as OpusApplication

    private val wifi = NetworkFacts(connected = true, validated = true, unmetered = true, metered = false)
    private val mobile = NetworkFacts(connected = true, validated = true, unmetered = false, metered = true)

    private lateinit var workManager: WorkManager
    private val seededBundles = mutableListOf<String>()
    private val seededJobs = mutableListOf<String>()
    private val isolatedRoots = mutableListOf<File>()

    private class MemoryPreferences : BundleJobPreferences {
        private val values = linkedMapOf<String, String>()
        override fun read(key: String): String? = values[key]
        override fun write(key: String, value: String?) {
            if (value == null) values.remove(key) else values[key] = value
        }
        override fun keys(): Set<String> = values.keys.toSet()
    }

    private class Isolated(val root: File, val store: BundleStore, val jobs: BundleJobStore)

    private data class Fixture(val manifest: BundleManifest, val jobId: String)

    @Before
    fun setUp() {
        // Real WorkManager test infrastructure: in-memory test database, synchronous executor,
        // TestScheduler-only scheduling. setDelegate also replaces any default instance the
        // application created during startup, so every assertion below observes this WorkManager.
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        workManager = WorkManager.getInstance(context)
        app.bundleNetworkOverride = BundleNetwork { wifi }
    }

    @After
    fun tearDown() {
        runBlocking {
            runCatching { workManager.cancelAllWorkByTag(BundleScheduler.TAG).result.get() }
            runCatching {
                Stage5Fixtures.waitFor("no bundle work is active during cleanup", timeoutMs = 15_000) {
                    workManager.getWorkInfosByTag(BundleScheduler.TAG).get().none { it.state in ACTIVE_STATES }
                }
            }
            seededBundles.forEach { app.bundleStore.remove(it) }
            seededJobs.forEach { app.bundleJobs.remove(it) }
        }
        seededBundles.clear()
        seededJobs.clear()
        File(context.cacheDir, "offline-bundles/staging").listFiles()?.forEach { it.delete() }
        // Store-level quarantine of unreadable manifests: disposable test bytes only.
        File(context.filesDir, "offline-bundles/quarantine").listFiles()?.forEach { it.delete() }
        isolatedRoots.forEach { it.deleteRecursively() }
        isolatedRoots.clear()
        app.bundleNetworkOverride = null
        app.bundleConnectOverride = null
        runCatching { WorkManagerTestInitHelper.closeWorkDatabase() }
    }

    // ------------------------------------------------------------ fixtures

    /**
     * Seeds a manifest plus an active, confirmed job record into the application-scoped store
     * the worker reads — registered for exact cleanup in [tearDown].
     */
    private suspend fun seedAppBundle(
        content: ByteArray,
        state: BundleState = BundleState.QUEUED,
        license: String = "CC0 1.0",
        meteredAllowed: Boolean = false,
        active: Boolean = true
    ): Fixture {
        val manifest = Stage5Fixtures.manifest(
            files = listOf(Stage5Fixtures.bundleFile(content, license = license)),
            state = state
        )
        app.bundleStore.save(manifest)
        val jobId = BundleJob.id(manifest.bundleId, manifest.revision)
        app.bundleJobs.put(
            BundleJob(
                bundleId = manifest.bundleId,
                revision = manifest.revision,
                estimatedBytes = manifest.expectedBytes,
                confirmed = true,
                meteredAllowed = meteredAllowed,
                active = active,
                attempts = 0,
                createdAt = 1L,
                updatedAt = 1L,
                lastError = null
            )
        )
        seededBundles += manifest.bundleId
        seededJobs += jobId
        return Fixture(manifest, jobId)
    }

    private fun isolated(): Isolated {
        val root = File(context.cacheDir, "stage5-worker-${UUID.randomUUID()}").apply { mkdirs() }
        isolatedRoots += root
        return Isolated(
            root = root,
            store = BundleStore(
                File(root, "bundles"), File(root, "staging"),
                reserveBytes = { 0L }, otherUsedBytes = { 0L }
            ),
            jobs = BundleJobStore(MemoryPreferences())
        )
    }

    private fun schedulerFor(isolated: Isolated): BundleScheduler =
        BundleScheduler(workManager, isolated.jobs, isolated.store, BundleNetwork { wifi })

    /** Executes the real worker directly; [bundleId] == null exercises missing input data. */
    private suspend fun runWorker(bundleId: String?, revision: Long = 1L): ListenableWorker.Result {
        // work-testing stores the RAW context it is handed, so a ContextImpl would fail the
        // worker's `applicationContext as? OpusApplication` cast; real WorkManager hands the
        // Application itself, so bind the builder to the application context the same way.
        val builder = TestListenableWorkerBuilder<BundleDownloadWorker>(context.applicationContext)
        if (bundleId != null) {
            builder.setInputData(
                Data.Builder()
                    .putString(BundleDownloadWorker.KEY_BUNDLE_ID, bundleId)
                    .putLong(BundleDownloadWorker.KEY_REVISION, revision)
                    .build()
            )
        }
        return builder.build().doWork()
    }

    private fun stagingDir(): File = File(context.cacheDir, "offline-bundles/staging")

    // ------------------------------------------------------------ worker outcomes

    @Test
    fun queuedBundleIsDownloadedVerifiedAndPublishedByTheWorker() = runBlocking {
        val content = "worker happy path payload".toByteArray()
        app.bundleConnectOverride = { Stage5Fixtures.FakeEndpoint(content) }
        val fixture = seedAppBundle(content)

        val result = runWorker(fixture.manifest.bundleId, fixture.manifest.revision)

        assertTrue("expected success, got $result", result is ListenableWorker.Result.Success)
        val published = app.bundleStore.readPublished()
            .manifests.firstOrNull { it.bundleId == fixture.manifest.bundleId }
        assertNotNull("bundle must be published", published)
        assertEquals(BundleState.READY, published!!.state)
        assertEquals("offline attribution survives publishing", "Open Opus contributors", published.attribution)
        assertNotNull("validation timestamp is durable", published.validatedAt)
        val sha = Stage5Fixtures.shaOf(content)
        assertTrue(app.bundleStore.blobFile(sha).isFile)
        assertEquals(content.size.toLong(), app.bundleStore.blobFile(sha).length())
        assertEquals(content.size.toLong(), published.progress.completedBytes)
        assertTrue("staging is empty after a successful run", stagingDir().listFiles().orEmpty().isEmpty())
        val job = app.bundleJobs.get(fixture.jobId)!!
        assertFalse("finished jobs never auto-restart", job.active)
        assertNull(job.lastError)
    }

    @Test
    fun offlineEnvironmentRetriesWithoutMovingAByte() = runBlocking {
        var opened = 0
        val content = "offline worker payload".toByteArray()
        app.bundleNetworkOverride = BundleNetwork { NetworkFacts.DISCONNECTED }
        app.bundleConnectOverride = { opened++; Stage5Fixtures.FakeEndpoint(content) }
        val fixture = seedAppBundle(content)

        val result = runWorker(fixture.manifest.bundleId, fixture.manifest.revision)

        assertTrue("Waiting maps to WorkManager retry, got $result", result is ListenableWorker.Result.Retry)
        val stored = app.bundleStore.readAll().manifests.firstOrNull { it.bundleId == fixture.manifest.bundleId }
        assertEquals(BundleState.WAITING_FOR_POLICY, stored!!.state)
        assertNull(
            "nothing is published while offline",
            app.bundleStore.readPublished().manifests.firstOrNull { it.bundleId == fixture.manifest.bundleId }
        )
        assertEquals("not a single byte may move while offline", 0, opened)
        assertTrue(app.bundleStore.blobFile(Stage5Fixtures.shaOf(content)).exists().not())
    }

    @Test
    fun missingMeteredConsentPausesAsSuccessAndKeepsRefusalDurable() = runBlocking {
        var opened = 0
        val content = "metered worker payload".toByteArray()
        app.bundleNetworkOverride = BundleNetwork { mobile }
        app.bundleConnectOverride = { opened++; Stage5Fixtures.FakeEndpoint(content) }
        val fixture = seedAppBundle(content, meteredAllowed = false)

        val result = runWorker(fixture.manifest.bundleId, fixture.manifest.revision)

        assertTrue("Paused maps to WorkManager success, got $result", result is ListenableWorker.Result.Success)
        val stored = app.bundleStore.readAll().manifests.firstOrNull { it.bundleId == fixture.manifest.bundleId }
        assertEquals(BundleState.PAUSED, stored!!.state)
        assertEquals(BundlePolicy.Reasons.CONSENT, stored.lastError)
        val job = app.bundleJobs.get(fixture.jobId)!!
        assertTrue("a paused job stays resumable", job.active)
        assertFalse("refusal is durable per job", job.meteredAllowed)
        assertEquals("no connection without consent", 0, opened)
        assertTrue(app.bundleStore.blobFile(Stage5Fixtures.shaOf(content)).exists().not())
    }

    @Test
    fun transportFailureRetriesAndRecordsTheAttempt() = runBlocking {
        val content = "transport failure payload".toByteArray()
        app.bundleConnectOverride = { Stage5Fixtures.FakeEndpoint(content, failAfterReads = 0) }
        val fixture = seedAppBundle(content)

        val result = runWorker(fixture.manifest.bundleId, fixture.manifest.revision)

        assertTrue("transient failure maps to WorkManager retry, got $result",
            result is ListenableWorker.Result.Retry)
        val stored = app.bundleStore.readAll().manifests.firstOrNull { it.bundleId == fixture.manifest.bundleId }
        assertEquals(BundleState.FAILED_RETRYABLE, stored!!.state)
        val job = app.bundleJobs.get(fixture.jobId)!!
        assertEquals(1, job.attempts)
        assertTrue("durable transport reason is recorded", job.lastError!!.startsWith(BundlePolicy.Reasons.TRANSPORT))
        assertTrue("a failed attempt is never published",
            app.bundleStore.readPublished().manifests.none { it.bundleId == fixture.manifest.bundleId })
        assertTrue("partial staging is disposable", stagingDir().listFiles().orEmpty().isEmpty())
    }

    @Test
    fun cancelledJobSucceedsWithoutTouchingTheBundle() = runBlocking {
        var opened = 0
        val content = "cancelled job payload".toByteArray()
        app.bundleConnectOverride = { opened++; Stage5Fixtures.FakeEndpoint(content) }
        val fixture = seedAppBundle(content, active = false)

        val result = runWorker(fixture.manifest.bundleId, fixture.manifest.revision)

        assertTrue("cancelled while queued is a clean success, got $result",
            result is ListenableWorker.Result.Success)
        val stored = app.bundleStore.readAll().manifests.firstOrNull { it.bundleId == fixture.manifest.bundleId }
        assertEquals(BundleState.QUEUED, stored!!.state)
        assertEquals("nothing was downloaded", 0, opened)
        assertFalse(app.bundleStore.blobFile(Stage5Fixtures.shaOf(content)).exists())
    }

    @Test
    fun missingInputDataFailsTheWork() = runBlocking {
        val result = runWorker(bundleId = null)
        assertTrue("missing input is a permanent failure, got $result",
            result is ListenableWorker.Result.Failure)
    }

    @Test
    fun unknownBundleJobFailsTheWork() = runBlocking {
        val result = runWorker(UUID.randomUUID().toString(), revision = 1L)
        assertTrue("unknown job record is a permanent failure, got $result",
            result is ListenableWorker.Result.Failure)
    }

    // ------------------------------------------------------------ real WorkManager execution

    @Test
    fun workManagerRunsTheScheduledWorkerToACompletedBundle() = runBlocking {
        val content = "workmanager end-to-end payload".toByteArray()
        app.bundleConnectOverride = { Stage5Fixtures.FakeEndpoint(content) }
        val fixture = seedAppBundle(content)
        val scheduler = BundleScheduler(
            workManager, app.bundleJobs, app.bundleStore, BundleNetwork { wifi }
        )

        val enqueued = scheduler.enqueue(fixture.manifest, meteredAllowed = false, confirmed = true)
        assertTrue(enqueued.enqueued)
        assertNull(enqueued.reason)

        var workId: UUID? = null
        Stage5Fixtures.waitFor("bundle work is registered with WorkManager") {
            workId = workManager.getWorkInfosForUniqueWork(fixture.jobId).get().firstOrNull()?.id
            workId != null
        }
        // Unblock the UNMETERED constraint even if the emulator reports no network yet.
        runCatching { WorkManagerTestInitHelper.getTestDriver(context)?.setAllConstraintsMet(workId!!) }

        Stage5Fixtures.waitFor("the scheduled worker finishes the download") {
            val infos = workManager.getWorkInfosForUniqueWork(fixture.jobId).get()
            infos.isNotEmpty() && infos.all { it.state.isFinished }
        }
        assertEquals(
            "the scheduled worker publishes the bundle successfully",
            WorkInfo.State.SUCCEEDED,
            workManager.getWorkInfosForUniqueWork(fixture.jobId).get().last().state
        )

        val published = app.bundleStore.readPublished()
            .manifests.firstOrNull { it.bundleId == fixture.manifest.bundleId }
        assertNotNull("bundle published by the WorkManager-driven run", published)
        assertEquals(BundleState.READY, published!!.state)
        val sha = Stage5Fixtures.shaOf(content)
        assertTrue(app.bundleStore.blobFile(sha).isFile)
        assertEquals(content.size.toLong(), app.bundleStore.blobFile(sha).length())
        assertFalse("finished jobs never auto-restart", app.bundleJobs.get(fixture.jobId)!!.active)
    }

    // ------------------------------------------------------------ scheduler durability

    @Test
    fun enqueueBindsOneUniqueWorkPerRevisionWithTheD7NetworkConstraint() = runBlocking {
        val iso = isolated()
        val scheduler = schedulerFor(iso)
        val plainManifest = Stage5Fixtures.manifest(
            files = listOf(Stage5Fixtures.bundleFile("unmetered job bytes".toByteArray()))
        )
        val consentedManifest = Stage5Fixtures.manifest(
            files = listOf(
                Stage5Fixtures.bundleFile("metered job bytes".toByteArray(), relativePath = "media/metered.jpg")
            )
        )

        val plain = scheduler.enqueue(plainManifest, meteredAllowed = false, confirmed = true)
        assertTrue(plain.enqueued)
        assertNull(plain.reason)
        val plainJob = iso.jobs.get(plain.jobId)!!
        assertTrue(plainJob.active)
        assertFalse("no metered consent by default", plainJob.meteredAllowed)
        assertTrue(plainJob.confirmed)

        val consented = scheduler.enqueue(consentedManifest, meteredAllowed = true, confirmed = true)
        assertTrue(consented.enqueued)

        Stage5Fixtures.waitFor("both unique work items are registered") {
            workManager.getWorkInfosForUniqueWork(plain.jobId).get().isNotEmpty() &&
                workManager.getWorkInfosForUniqueWork(consented.jobId).get().isNotEmpty()
        }

        val plainInfo = workManager.getWorkInfosForUniqueWork(plain.jobId).get().first()
        assertEquals(
            "D7: background work without consent is UNMETERED",
            NetworkType.UNMETERED, plainInfo.constraints.requiredNetworkType
        )
        assertEquals(
            "explicit per-job consent downgrades the constraint to CONNECTED",
            NetworkType.CONNECTED,
            workManager.getWorkInfosForUniqueWork(consented.jobId).get().first().constraints.requiredNetworkType
        )
        assertTrue("work carries the scheduler tag for diagnostics", BundleScheduler.TAG in plainInfo.tags)
        assertTrue(
            "never more than one unfinished attempt per revision",
            workManager.getWorkInfosForUniqueWork(plain.jobId).get().count { !it.state.isFinished } <= 1
        )

        // KEEP re-enqueue keeps the durable record instead of duplicating the attempt.
        val before = iso.jobs.get(plain.jobId)!!
        val again = scheduler.enqueue(plainManifest, meteredAllowed = false, confirmed = true)
        assertTrue(again.enqueued)
        val after = iso.jobs.get(plain.jobId)!!
        assertEquals("re-enqueue preserves attempt history", before.attempts, after.attempts)
        assertEquals("re-enqueue preserves job identity", before.createdAt, after.createdAt)
        assertTrue(
            "still at most one unfinished attempt",
            workManager.getWorkInfosForUniqueWork(plain.jobId).get().count { !it.state.isFinished } <= 1
        )
    }

    @Test
    fun revocationWithdrawsConsentCancelsWorkAndPausesTheManifest() = runBlocking {
        val iso = isolated()
        val scheduler = schedulerFor(iso)
        val manifest = Stage5Fixtures.manifest(
            files = listOf(Stage5Fixtures.bundleFile("metered refresh bytes".toByteArray()))
        )
        val enqueued = scheduler.enqueue(manifest, meteredAllowed = true, confirmed = true)
        assertTrue(enqueued.enqueued)
        assertTrue(iso.jobs.get(enqueued.jobId)!!.meteredAllowed)

        assertTrue(scheduler.revokeMeteredAllowance(enqueued.jobId))

        val job = iso.jobs.get(enqueued.jobId)!!
        assertFalse("consent is withdrawn durably", job.meteredAllowed)
        assertTrue("the job stays resumable on an unmetered network", job.active)
        val paused = iso.store.readAll().manifests.single { it.bundleId == manifest.bundleId }
        assertEquals(BundleState.PAUSED, paused.state)
        assertEquals(BundlePolicy.Reasons.CONSENT, paused.lastError)
        Stage5Fixtures.waitFor("revoked work is no longer active") {
            workManager.getWorkInfosForUniqueWork(enqueued.jobId).get().none { it.state in ACTIVE_STATES }
        }
    }

    @Test
    fun cancelDeactivatesTheJobAndPreservesPreviouslyReadyContent() = runBlocking {
        val iso = isolated()
        val scheduler = schedulerFor(iso)
        val id = UUID.randomUUID().toString()

        // Published READY revision 1 with installed bytes.
        val readyContent = "pinned ready content".toByteArray()
        val readyFile = Stage5Fixtures.bundleFile(readyContent)
        iso.store.save(Stage5Fixtures.manifest(id, 1, listOf(readyFile), state = BundleState.READY))
        val staged = File(iso.root, "staging/${readyFile.sha256}.part")
        staged.writeBytes(readyContent)
        assertTrue(iso.store.installBlob(staged, readyFile.sha256, readyContent.size.toLong()))

        // Refresh revision 2 is queued, then cancelled explicitly.
        val refresh = Stage5Fixtures.manifest(
            id, 2,
            listOf(Stage5Fixtures.bundleFile("refreshed bytes".toByteArray(), relativePath = "media/refresh.jpg"))
        )
        val enqueued = scheduler.enqueue(refresh, meteredAllowed = false, confirmed = true)
        assertTrue(enqueued.enqueued)

        assertTrue(scheduler.cancel(id, 2))

        val job = iso.jobs.get(enqueued.jobId)!!
        assertFalse("cancelled jobs never restart automatically", job.active)
        assertEquals(BundlePolicy.Reasons.CANCELLED, job.lastError)
        val published = iso.store.readPublished().manifests.single { it.bundleId == id }
        assertEquals("previously READY revision survives cancel", 1L, published.revision)
        assertEquals(BundleState.READY, published.state)
        assertTrue("installed bytes are untouched", iso.store.blobFile(readyFile.sha256).isFile)
        val pausedRefresh = iso.store.readPending().manifests.single { it.bundleId == id }
        assertEquals(BundleState.PAUSED, pausedRefresh.state)
        Stage5Fixtures.waitFor("cancelled work is no longer active") {
            workManager.getWorkInfosForUniqueWork(enqueued.jobId).get().none { it.state in ACTIVE_STATES }
        }
    }

    @Test
    fun reconciliationRecoversInFlightWorkAndNeverStartsUnconfirmedBundles() = runBlocking {
        val iso = isolated()
        val scheduler = schedulerFor(iso)

        // Process death mid-download: in-flight manifest, no live work.
        val inflight = Stage5Fixtures.manifest(
            files = listOf(Stage5Fixtures.bundleFile("died mid download".toByteArray())),
            state = BundleState.DOWNLOADING
        )
        iso.store.save(inflight)

        // D7: a bundle above the 8 MiB confirmation gate must never auto-start.
        val unconfirmed = Stage5Fixtures.manifest(
            files = listOf(Stage5Fixtures.bundleFile("big".toByteArray()).copy(bytes = 9L * 1024 * 1024)),
            state = BundleState.QUEUED
        )
        iso.store.save(unconfirmed)

        val report = scheduler.reconcileAfterRestart()

        assertEquals("process-death state is recovered", 1, report.recoveredInFlight)
        assertEquals("resumable work is re-enqueued", 1, report.reenqueued)
        assertEquals("D7: unconfirmed large job is reported, never auto-started", 1, report.awaitingConfirmation)
        assertEquals("nothing else is skipped", 0, report.skipped)

        val recovered = iso.store.readAll().manifests.single { it.bundleId == inflight.bundleId }
        assertEquals("recovery lands in a legal resumable state", BundleState.QUEUED, recovered.state)
        assertTrue(iso.jobs.get(BundleJob.id(inflight.bundleId, 1))!!.active)
        Stage5Fixtures.waitFor("recovered work is registered with WorkManager") {
            workManager.getWorkInfosForUniqueWork(BundleJob.id(inflight.bundleId, 1)).get().isNotEmpty()
        }

        assertTrue(
            "the unconfirmed bundle gets no work at all",
            workManager.getWorkInfosForUniqueWork(BundleJob.id(unconfirmed.bundleId, 1)).get().isEmpty()
        )
        assertNull(
            "the unconfirmed bundle gets no job record",
            iso.jobs.get(BundleJob.id(unconfirmed.bundleId, 1))
        )
    }

    companion object {
        /** WorkManager states that mean "still scheduled or running" — never after a stop. */
        private val ACTIVE_STATES = setOf(
            WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING, WorkInfo.State.BLOCKED
        )
    }
}
