package com.opus.piano.data.offline

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.opus.piano.OpusApplication
import kotlinx.coroutines.CancellationException

/**
 * WorkManager entry point for one bundle download job (D5.3).
 *
 * Deliberately thin: all policy/state/storage logic lives in [BundleJobRunner], which is
 * independently instrumented. The worker only loads the durable job record, refuses to run
 * cancelled jobs, and maps [BundleJobRunner.Outcome] onto WorkManager's contract:
 *
 *  | outcome     | WorkManager | durable state                      |
 *  |-------------|-------------|------------------------------------|
 *  | Completed   | success     | READY (promoted atomically)        |
 *  | Waiting     | retry       | WAITING_FOR_POLICY                 |
 *  | Paused      | success     | PAUSED (needs explicit re-enqueue) |
 *  | Retryable   | retry       | FAILED_RETRYABLE                   |
 *  | Quarantined | failure     | QUARANTINED                        |
 *  | Permanent   | failure     | FAILED_RETRYABLE, job deactivated  |
 *
 * `Result.retry()` keeps the unique work alive, so startup reconciliation never double-enqueues.
 */
class BundleDownloadWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? OpusApplication ?: return Result.failure()
        val bundleId = inputData.getString(KEY_BUNDLE_ID) ?: return Result.failure()
        val revision = inputData.getLong(KEY_REVISION, -1L)
        if (revision < 1) return Result.failure()
        val job = app.bundleJobs.get(BundleJob.id(bundleId, revision)) ?: return Result.failure()
        if (!job.active) return Result.success() // cancelled while queued: nothing may move

        val outcome = try {
            app.bundleRunner.run(job)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Defensive: the runner persists its own failure states; this only bounds the crash.
            BundleJobRunner.Outcome.Retryable("${BundlePolicy.Reasons.TRANSPORT}:${e.javaClass.simpleName}")
        }
        return when (outcome) {
            is BundleJobRunner.Outcome.Completed -> Result.success()
            is BundleJobRunner.Outcome.Waiting -> Result.retry()
            is BundleJobRunner.Outcome.Paused -> Result.success()
            is BundleJobRunner.Outcome.Retryable -> Result.retry()
            is BundleJobRunner.Outcome.Quarantined -> Result.failure()
            is BundleJobRunner.Outcome.Permanent -> Result.failure()
        }
    }

    companion object {
        const val KEY_BUNDLE_ID = "bundleId"
        const val KEY_REVISION = "revision"
    }
}
