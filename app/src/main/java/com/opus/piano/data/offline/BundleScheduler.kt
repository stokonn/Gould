package com.opus.piano.data.offline

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * WorkManager-backed scheduler for bundle downloads (D5.3, owner-mandated).
 *
 * Contract:
 *  - one **unique** persistent work per `bundleId:revision` — in-flight requests deduplicate
 *    and `KEEP` preserves an existing attempt's backoff
 *  - background jobs run `UNMETERED` unless the job itself holds an explicit, persisted
 *    per-job metered allowance (D7): no automatic background mobile downloads, ever
 *  - [reconcileAfterRestart] restores durability after process death: in-flight manifests are
 *    mapped through [BundleStateMachine.afterProcessRestart] (never into READY) and re-enqueued
 *    under WorkManager's backoff; a confirmation-gated bundle is never auto-started
 *  - revocation cancels pending metered work and pauses the manifest without losing data
 */
class BundleScheduler(
    private val workManager: WorkManager,
    private val jobs: BundleJobStore,
    private val store: BundleStore,
    private val network: BundleNetwork,
    private val now: () -> Long = System::currentTimeMillis
) {

    data class EnqueueResult(val jobId: String, val enqueued: Boolean, val reason: String?)

    data class ReconcileReport(
        val recoveredInFlight: Int = 0,
        val reenqueued: Int = 0,
        val awaitingConfirmation: Int = 0,
        val skipped: Int = 0
    )

    // ---------------------------------------------------------------- enqueue

    /**
     * Validates, persists (QUEUED) and enqueues one manifest revision. Refusals are explicit
     * strings, never silent: `already_ready`, `stale_revision`, `revision_not_newer`,
     * `confirmation_required`, `storage`, `illegal_state`.
     */
    suspend fun enqueue(
        manifest: BundleManifest,
        meteredAllowed: Boolean,
        confirmed: Boolean
    ): EnqueueResult = withContext(Dispatchers.IO) {
        val jobId = BundleJob.id(manifest.bundleId, manifest.revision)
        if (manifest.state == BundleState.READY) {
            return@withContext EnqueueResult(jobId, false, "already_ready")
        }
        val refusal = validate(manifest, meteredAllowed, confirmed)
        if (refusal != null) return@withContext EnqueueResult(jobId, false, refusal)

        val queued = manifest.copy(state = BundleState.QUEUED, updatedAt = now())
        store.save(queued)
        val existing = jobs.get(jobId)
        val job = BundleJob(
            bundleId = manifest.bundleId,
            revision = manifest.revision,
            estimatedBytes = manifest.expectedBytes,
            confirmed = confirmed,
            meteredAllowed = meteredAllowed,
            active = true,
            attempts = existing?.attempts ?: 0,
            createdAt = existing?.createdAt ?: now(),
            updatedAt = now(),
            lastError = existing?.lastError
        )
        jobs.put(job)
        workManager.enqueueUniqueWork(jobId, ExistingWorkPolicy.KEEP, request(job))
        EnqueueResult(jobId, true, null)
    }

    private suspend fun validate(
        manifest: BundleManifest,
        meteredAllowed: Boolean,
        confirmed: Boolean
    ): String? {
        val published = store.readPublished().manifests
            .firstOrNull { it.bundleId == manifest.bundleId }
        if (published != null) {
            if (manifest.revision < published.revision) return "stale_revision"
            // A refresh of an already-published READY revision must be a NEW revision, so an
            // interrupted refresh can never destroy the previously published content.
            if (manifest.revision == published.revision &&
                published.state == BundleState.READY && manifest.state != BundleState.READY
            ) {
                return "revision_not_newer"
            }
        }
        // D7: unknown or large sizes require explicit confirmation; consent is per job.
        if (BundlePolicy.requiresConfirmation(manifest.expectedBytes) && !confirmed) {
            return "confirmation_required"
        }
        if (manifest.state != BundleState.QUEUED &&
            BundleStateMachine.path(manifest.state, BundleState.QUEUED).isEmpty()
        ) {
            return "illegal_state"
        }
        val installed = manifest.files
            .filter { file -> store.blobFile(file.sha256).let { it.isFile && it.length() == file.bytes } }
            .sumOf { it.bytes }
        val remaining = (manifest.expectedUncompressedBytes - installed).coerceAtLeast(0L)
        if (!store.admits(remaining)) return "storage"
        return null
    }

    private fun request(job: BundleJob): OneTimeWorkRequest {
        val networkType =
            if (BundleWorkPolicy.networkType(job.meteredAllowed) == BundleWorkPolicy.CONNECTED) {
                NetworkType.CONNECTED
            } else {
                NetworkType.UNMETERED
            }
        val input = Data.Builder()
            .putString(BundleDownloadWorker.KEY_BUNDLE_ID, job.bundleId)
            .putLong(BundleDownloadWorker.KEY_REVISION, job.revision)
            .build()
        return OneTimeWorkRequestBuilder<BundleDownloadWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(networkType).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
            .setInputData(input)
            .addTag(TAG)
            .build()
    }

    // ---------------------------------------------------------------- cancel / consent

    /**
     * Explicit stop: the job is deactivated (never auto-re-enqueued), the unique work is
     * cancelled and an in-flight manifest lands in PAUSED. Installed content is untouched.
     */
    suspend fun cancel(bundleId: String, revision: Long): Boolean = withContext(Dispatchers.IO) {
        val jobId = BundleJob.id(bundleId, revision)
        if (jobs.get(jobId) == null && !store.readAll().manifests.any {
                it.bundleId == bundleId && it.revision == revision
            }
        ) {
            return@withContext false
        }
        jobs.update(jobId) { it.deactivated(BundlePolicy.Reasons.CANCELLED) }
        workManager.cancelUniqueWork(jobId)
        pauseInFlight(bundleId, revision, BundlePolicy.Reasons.CANCELLED)
        true
    }

    /**
     * D7 revocation: the per-job allowance is withdrawn immediately, pending/running metered
     * work is cancelled and the manifest is paused. The job stays *active*, so it may still
     * resume later on an unmetered network — it can never resume on mobile data.
     */
    suspend fun revokeMeteredAllowance(jobId: String): Boolean = withContext(Dispatchers.IO) {
        val job = jobs.get(jobId) ?: return@withContext false
        jobs.revokeMetered(jobId)
        if (job.meteredAllowed) {
            workManager.cancelUniqueWork(jobId)
            pauseInFlight(job.bundleId, job.revision, BundlePolicy.Reasons.CONSENT)
        }
        true
    }

    /** Re-granting a per-job allowance re-enqueues active work with the metered constraint. */
    suspend fun grantMeteredAllowance(jobId: String): Boolean = withContext(Dispatchers.IO) {
        val job = jobs.grantMetered(jobId) ?: return@withContext false
        if (!job.active) return@withContext false
        workManager.enqueueUniqueWork(jobId, ExistingWorkPolicy.REPLACE, request(job))
        true
    }

    private suspend fun pauseInFlight(bundleId: String, revision: Long, reason: String) {
        val manifest = store.readAll().manifests
            .firstOrNull { it.bundleId == bundleId && it.revision == revision } ?: return
        if (manifest.state == BundleState.READY) return
        if (manifest.state in PAUSABLE && BundleStateMachine.path(manifest.state, BundleState.PAUSED).isNotEmpty()) {
            runCatching {
                store.save(manifest.copy(state = BundleState.PAUSED, lastError = reason, updatedAt = now()))
            }
        }
    }

    // ---------------------------------------------------------------- reconcile

    /**
     * Startup reconciliation (call once per process, off the main thread):
     *  1. in-flight manifests with no live work are mapped to PAUSED (process-death recovery);
     *  2. resumable manifests with an active job record are re-enqueued under their persisted
     *     constraint (UNMETERED unless the job holds metered consent);
     *  3. bundles that still need an explicit size confirmation are reported, never auto-started.
     */
    suspend fun reconcileAfterRestart(): ReconcileReport = withContext(Dispatchers.IO) {
        var recovered = 0
        var reenqueued = 0
        var awaiting = 0
        var skipped = 0
        for (manifest in store.readAll().manifests) {
            if (manifest.state == BundleState.READY) continue
            val jobId = BundleJob.id(manifest.bundleId, manifest.revision)
            var current = manifest
            val inFlight = current.state in IN_FLIGHT
            if (inFlight) {
                if (hasActiveWork(jobId)) {
                    skipped++
                    continue
                }
                val target = BundleStateMachine.afterProcessRestart(current.state)
                val paused = current.copy(state = target, updatedAt = now())
                runCatching { store.save(paused) }
                current = paused
                recovered++
            }
            if (current.state !in RESUMABLE) continue
            if (hasActiveWork(jobId)) {
                skipped++
                continue
            }
            val known = jobs.get(jobId)
            if (known != null && !known.active) {
                skipped++
                continue
            }
            if (known == null && BundlePolicy.requiresConfirmation(current.expectedBytes)) {
                awaiting++
                continue
            }
            val meteredAllowed = known?.meteredAllowed ?: false
            val confirmed = known?.confirmed ?: false
            val result = enqueue(current, meteredAllowed, confirmed)
            if (result.enqueued) reenqueued++ else skipped++
        }
        ReconcileReport(recovered, reenqueued, awaiting, skipped)
    }

    private fun hasActiveWork(jobId: String): Boolean {
        val infos = runCatching {
            workManager.getWorkInfosForUniqueWork(jobId).get()
        }.getOrDefault(emptyList())
        return infos.any {
            it.state == WorkInfo.State.ENQUEUED ||
                it.state == WorkInfo.State.RUNNING ||
                it.state == WorkInfo.State.BLOCKED
        }
    }

    companion object {
        const val TAG = "goldberg-bundle"
        const val BACKOFF_MILLIS = 30_000L

        /** Manifest states a process can die in; never left unattended after a restart. */
        val IN_FLIGHT = setOf(BundleState.DOWNLOADING, BundleState.VALIDATING, BundleState.INSTALLING)

        /** States that may be resumed automatically (still gated by consent + constraints). */
        val RESUMABLE = setOf(
            BundleState.QUEUED,
            BundleState.WAITING_FOR_POLICY,
            BundleState.PAUSED,
            BundleState.FAILED_RETRYABLE
        )

        private val PAUSABLE = IN_FLIGHT + BundleState.QUEUED + BundleState.WAITING_FOR_POLICY
    }
}
