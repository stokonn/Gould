package com.opus.piano.data.offline

/**
 * Minimal key/value persistence behind [BundleJobStore].
 *
 * Extracted so JVM unit tests can run the durable-job semantics (per-job consent, revocation,
 * active/cancelled flags, corrupt-record tolerance) against an in-memory fake, while the
 * Android implementation is a plain SharedPreferences file.
 */
interface BundleJobPreferences {
    fun read(key: String): String?
    fun write(key: String, value: String?)
    fun keys(): Set<String>
}

/**
 * Durable job state (D5.3/D7): per-job metered allowance, confirmation, attempts and
 * cancellation survive process death. Writes are synchronous (`commit` semantics) because a
 * consent flag that "may not have been persisted" would be a safety bug, not a performance one.
 *
 * A corrupt/undecodable record is treated as **absent** — never as consented.
 */
class BundleJobStore(
    private val prefs: BundleJobPreferences,
    private val now: () -> Long = System::currentTimeMillis
) {
    fun put(job: BundleJob) {
        prefs.write(PREFIX + job.jobId, BundleJobCodec.encode(job.copy(updatedAt = now())))
    }

    fun get(jobId: String): BundleJob? = read(PREFIX + jobId)

    fun find(bundleId: String, revision: Long): BundleJob? = get(BundleJob.id(bundleId, revision))

    /** Oldest first; used by startup reconciliation and diagnostics. */
    fun list(): List<BundleJob> =
        prefs.keys()
            .filter { it.startsWith(PREFIX) }
            .mapNotNull { read(it) }
            .sortedWith(compareBy({ it.createdAt }, { it.revision }))

    fun update(jobId: String, transform: (BundleJob) -> BundleJob): BundleJob? {
        val current = get(jobId) ?: return null
        val updated = transform(current)
        put(updated)
        return updated
    }

    /** D7: grant/revoke happen per job and take effect on the very next policy check. */
    fun grantMetered(jobId: String): BundleJob? = update(jobId) { it.grantMetered() }

    fun revokeMetered(jobId: String): BundleJob? = update(jobId) { it.revokeMetered() }

    /** Deactivated jobs are never re-enqueued automatically (explicit cancel/permanent error). */
    fun deactivate(jobId: String, error: String?): BundleJob? =
        update(jobId) { it.deactivated(error) }

    fun recordAttempt(jobId: String, error: String?): BundleJob? =
        update(jobId) { it.attempt(error) }

    fun remove(jobId: String) {
        prefs.write(PREFIX + jobId, null)
    }

    private fun read(key: String): BundleJob? {
        val raw = prefs.read(key) ?: return null
        return try {
            BundleJobCodec.decode(raw)
        } catch (_: Exception) {
            // Unreadable metadata never grants consent; the job simply does not exist.
            null
        }
    }

    companion object {
        const val PREFIX = "job:"
    }
}
