package com.opus.piano.data.offline

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Executes one bundle download job end to end, persisting every transition through
 * [BundleStore] so a process kill at **any** boundary leaves a legal, retryable state.
 *
 * Pipeline (OFFLINE-CACHE.md): `QUEUED -> WAITING_FOR_POLICY -> DOWNLOADING -> VALIDATING ->
 * INSTALLING -> READY`, with `PAUSED`, `FAILED_RETRYABLE`, `STALE` and `QUARANTINED`
 * branches handled through [BundleStateMachine.path].
 *
 * Safety properties:
 *  - the previously READY revision is never touched: a refresh builds `pending/` and is
 *    published by one atomic rename only after full validation
 *  - consent/network/storage are revalidated before every file **and** during streaming;
 *    revocation pauses (PAUSED) and never deletes installed content
 *  - per-job allowance is re-read live from [BundleJobStore], so revocation applies to the
 *    very next gate even inside a running attempt
 *  - rights are revalidated per attempt; failures QUARANTINE instead of retrying blindly
 *  - cancellation persists PAUSED before the coroutine dies (NonCancellable)
 */
class BundleJobRunner(
    private val store: BundleStore,
    private val jobs: BundleJobStore,
    private val network: BundleNetwork,
    private val stagingDir: File,
    private val connect: (URL) -> HttpURLConnection = { it.openConnection() as HttpURLConnection },
    private val now: () -> Long = System::currentTimeMillis
) {

    /** Machine-readable result; the worker maps these onto WorkManager's Result contract. */
    sealed interface Outcome {
        data object Completed : Outcome
        /** Environment not ready (offline/captive portal/Data Saver): retry under backoff. */
        data class Waiting(val error: String) : Outcome
        /** Deliberate durable stop (consent/refusal/cancel): needs an explicit re-enqueue. */
        data class Paused(val error: String) : Outcome
        /** Transient failure: durable FAILED_RETRYABLE, retried by WorkManager backoff. */
        data class Retryable(val error: String) : Outcome
        /** Rights/unsafe content: durable QUARANTINED, never retried into READY. */
        data class Quarantined(val error: String) : Outcome
        /** Permanent authoring/source problem: durable error, job deactivated. */
        data class Permanent(val error: String) : Outcome
    }

    private data class Located(val manifest: BundleManifest, val inPending: Boolean)

    suspend fun run(job: BundleJob): Outcome {
        val located = locate(job)
        if (located == null) {
            jobs.deactivate(job.jobId, BundlePolicy.Reasons.MANIFEST_MISSING)
            return Outcome.Permanent(BundlePolicy.Reasons.MANIFEST_MISSING)
        }
        var manifest = located.manifest
        try {
            if (manifest.state == BundleState.READY) {
                if (located.inPending) store.promote(manifest.bundleId)
                jobs.update(job.jobId) { it.succeeded() }
                return Outcome.Completed
            }
            // Rights and reader capability are decided before a single byte is requested.
            if (manifest.minReaderVersion > BundleManifest.READER_VERSION) {
                return quarantine(manifest, job, BundlePolicy.Reasons.READER_VERSION)
            }
            validateRights(manifest)
            manifest = enterAttempt(manifest)

            // 1. Network + per-job consent (D7) before anything moves.
            stopReason(job)?.let { return stop(manifest, job, it) }

            // 2. Storage ceiling + protected reserve (D7) before anything is written.
            val missing = manifest.expectedUncompressedBytes - installedBytes(manifest)
            if (missing > 0 && !store.admits(missing)) {
                return retryable(manifest, job, BundlePolicy.Reasons.STORAGE)
            }

            // 3. Download every missing file, content-addressed, hash-verified.
            manifest = settle(manifest, BundleState.DOWNLOADING, error = null)
            var completedBytes = installedBytes(manifest)
            var completedFiles = countInstalled(manifest)
            manifest = saveProgress(manifest, completedBytes, completedFiles)
            for (file in manifest.files) {
                if (hasBlob(file)) continue
                val base = completedBytes
                val staged = engineFor(job) { written ->
                    persistThrottled(manifest, base + written, completedFiles)
                }.fetch(file)
                if (!store.installBlob(staged, file.sha256, file.bytes)) {
                    staged.delete()
                    throw BundleTransferError.Integrity("integrity mismatch installing ${file.relativePath}")
                }
                completedBytes += file.bytes
                completedFiles += 1
                manifest = saveProgress(manifest, completedBytes, completedFiles)
            }

            // 4. Full validation of every stored blob + rights, before publishing anything.
            manifest = settle(manifest, BundleState.VALIDATING, error = null)
            verify(manifest)

            // 5. Publish: one atomic rename, so an interrupted publish keeps the old revision.
            manifest = settle(manifest, BundleState.INSTALLING, error = null)
            val ready = manifest.copy(
                state = BundleState.READY,
                validatedAt = now(),
                lastError = null,
                updatedAt = now(),
                progress = BundleProgress(
                    completedBytes = manifest.expectedUncompressedBytes,
                    totalBytes = manifest.expectedUncompressedBytes,
                    completedFiles = manifest.files.size
                )
            )
            store.save(ready)
            if (store.pendingFile(ready.bundleId, ready.revision).exists()) {
                store.promote(ready.bundleId)
            }
            jobs.update(job.jobId) { it.succeeded() }
            return Outcome.Completed
        } catch (e: CancellationException) {
            withContext(NonCancellable) {
                runCatching { stop(manifest, job, BundlePolicy.Reasons.CANCELLED) }
            }
            throw e
        } catch (e: BundleTransferError) {
            // NonCancellable: durable state must land even while the worker is being stopped.
            return withContext(NonCancellable) { mapError(manifest, job, e) }
        } catch (e: Exception) {
            return withContext(NonCancellable) {
                retryable(manifest, job, "${BundlePolicy.Reasons.TRANSPORT}:${e.javaClass.simpleName}")
            }
        }
    }

    // ---------------------------------------------------------------- steps

    /**
     * Resolves the manifest a job runs against, from BOTH directories: since
     * [BundleStore.readPublished] exposes only authoritative states, a working copy
     * (QUEUED/FAILED_RETRYABLE/... for a bundle whose first save landed in `manifests/`)
     * is only visible through [BundleStore.readAll]. `inPending` is resolved separately so
     * a completed refresh that has not been promoted yet is still published on recovery.
     */
    private suspend fun locate(job: BundleJob): Located? {
        val match = store.readAll().manifests
            .firstOrNull { it.bundleId == job.bundleId && it.revision == job.revision }
            ?: return null
        val inPending = store.readPending().manifests
            .any { it.bundleId == job.bundleId && it.revision == job.revision }
        return Located(match, inPending = inPending)
    }

    /** Fresh attempt: land in a state that may transfer, without touching READY content. */
    private suspend fun enterAttempt(manifest: BundleManifest): BundleManifest = when (manifest.state) {
        BundleState.QUEUED, BundleState.WAITING_FOR_POLICY, BundleState.DOWNLOADING -> manifest
        else -> settle(manifest, BundleState.QUEUED, manifest.lastError)
    }

    private fun validateRights(manifest: BundleManifest) {
        try {
            manifest.files.forEach { BundleManifestCodec.validateFile(it) }
        } catch (e: Exception) {
            throw BundleTransferError.Rights("rights revalidation failed: ${e.message}")
        }
    }

    private suspend fun verify(manifest: BundleManifest) {
        validateRights(manifest)
        for (file in manifest.files) {
            val blob = store.blobFile(file.sha256)
            if (!blob.isFile || blob.length() != file.bytes) {
                throw BundleTransferError.Integrity("missing or incomplete blob for ${file.relativePath}")
            }
            if (BundleStoreLogic.sha256Of(blob) != file.sha256) {
                // Bytes do not match their own digest: preserve them, then require a re-download.
                runCatching { store.quarantineBlob(file.sha256, "sha256_mismatch") }
                throw BundleTransferError.Integrity("corrupt blob for ${file.relativePath}")
            }
        }
    }

    // ---------------------------------------------------------------- mapping

    private suspend fun mapError(manifest: BundleManifest, job: BundleJob, e: BundleTransferError): Outcome =
        when (e) {
            is BundleTransferError.Paused -> stop(manifest, job, e.code)
            is BundleTransferError.Storage -> retryable(manifest, job, BundlePolicy.Reasons.STORAGE)
            is BundleTransferError.Integrity -> retryable(manifest, job, BundlePolicy.Reasons.INTEGRITY)
            is BundleTransferError.Transport -> retryable(manifest, job, BundlePolicy.Reasons.TRANSPORT)
            is BundleTransferError.Rights -> quarantine(manifest, job, BundlePolicy.Reasons.RIGHTS)
            is BundleTransferError.Source -> permanent(manifest, job, BundlePolicy.Reasons.SOURCE)
        }

    private suspend fun stop(manifest: BundleManifest, job: BundleJob, reason: String): Outcome {
        val target =
            if (reason in BundlePolicy.Reasons.WAITING) BundleState.WAITING_FOR_POLICY
            else BundleState.PAUSED
        val updated = settleQuietly(manifest, target, reason)
        jobs.update(job.jobId) { it.copy(lastError = reason) }
        return if (target == BundleState.WAITING_FOR_POLICY) Outcome.Waiting(reason) else Outcome.Paused(reason)
    }

    private suspend fun retryable(manifest: BundleManifest, job: BundleJob, reason: String): Outcome {
        settleQuietly(manifest, BundleState.FAILED_RETRYABLE, reason)
        jobs.recordAttempt(job.jobId, reason)
        return Outcome.Retryable(reason)
    }

    private suspend fun quarantine(manifest: BundleManifest, job: BundleJob, reason: String): Outcome {
        settleQuietly(manifest, BundleState.QUARANTINED, reason)
        jobs.deactivate(job.jobId, reason)
        return Outcome.Quarantined(reason)
    }

    private suspend fun permanent(manifest: BundleManifest, job: BundleJob, reason: String): Outcome {
        settleQuietly(manifest, BundleState.FAILED_RETRYABLE, reason)
        jobs.deactivate(job.jobId, reason)
        return Outcome.Permanent(reason)
    }

    // ---------------------------------------------------------------- state persistence

    /**
     * Persisted transition through the reviewed state machine; strict in the happy path.
     *
     * The validation timestamp follows the manifest invariant: only authoritative targets
     * (READY/STALE/QUARANTINED) keep it — settling a non-authoritative state (e.g.
     * STALE -> QUEUED in [enterAttempt]) drops it so the copy can never violate
     * [BundleManifest]'s `require` and wedge the bundle.
     */
    private suspend fun settle(manifest: BundleManifest, target: BundleState, error: String?): BundleManifest {
        if (manifest.state != target && BundleStateMachine.path(manifest.state, target).isEmpty()) {
            throw IllegalStateException("No legal transition ${manifest.state} -> $target")
        }
        if (manifest.state == target && manifest.lastError == error) return manifest
        val updated = manifest.copy(
            state = target,
            lastError = error,
            updatedAt = now(),
            // Non-authoritative states drop the timestamp to keep the manifest invariant:
            // only READY/STALE/QUARANTINED may carry validatedAt.
            validatedAt = if (
                target == BundleState.READY || target == BundleState.STALE || target == BundleState.QUARANTINED
            ) manifest.validatedAt else null
        )
        store.save(updated)
        return updated
    }

    /** Best-effort variant used on error/cancel paths so reporting never throws away state. */
    private suspend fun settleQuietly(manifest: BundleManifest, target: BundleState, error: String?): BundleManifest =
        try {
            settle(manifest, target, error)
        } catch (_: Exception) {
            manifest
        }

    private suspend fun saveProgress(manifest: BundleManifest, completedBytes: Long, completedFiles: Int): BundleManifest {
        val updated = manifest.copy(
            progress = BundleProgress(
                completedBytes = completedBytes.coerceAtMost(manifest.expectedUncompressedBytes),
                totalBytes = manifest.expectedUncompressedBytes,
                completedFiles = completedFiles
            ),
            updatedAt = now()
        )
        if (updated == manifest) return manifest
        store.save(updated)
        return updated
    }

    private suspend fun persistThrottled(manifest: BundleManifest, completedBytes: Long, completedFiles: Int) {
        val stamp = now()
        if (stamp - lastProgressPersist < PROGRESS_PERSIST_MILLIS) return
        lastProgressPersist = stamp
        runCatching { saveProgress(manifest, completedBytes, completedFiles) }
    }

    private var lastProgressPersist = 0L

    // ---------------------------------------------------------------- helpers

    /**
     * Live policy check: consent is re-read from the durable job record, so a revocation
     * applies to the running attempt too, and a deactivated (cancelled) job stops as well.
     */
    private fun stopReason(job: BundleJob): String? {
        val live = jobs.get(job.jobId)
        if (live != null && !live.active) return BundlePolicy.Reasons.CANCELLED
        return BundlePolicy.Reasons.stopReason(network.facts(), live?.meteredAllowed ?: job.meteredAllowed)
    }

    private fun engineFor(job: BundleJob, onProgress: suspend (Long) -> Unit): BundleTransferEngine =
        BundleTransferEngine(
            stagingDir = stagingDir,
            network = network,
            meteredAllowed = { jobs.get(job.jobId)?.meteredAllowed ?: job.meteredAllowed },
            admission = { store.admits(it) },
            connect = connect,
            onProgress = onProgress
        )

    private fun hasBlob(file: BundleFile): Boolean {
        val blob = store.blobFile(file.sha256)
        return blob.isFile && blob.length() == file.bytes
    }

    private fun installedBytes(manifest: BundleManifest): Long =
        manifest.files.filter { hasBlob(it) }.sumOf { it.bytes }

    private fun countInstalled(manifest: BundleManifest): Int = manifest.files.count { hasBlob(it) }

    companion object {
        const val PROGRESS_PERSIST_MILLIS = 750L
    }
}
