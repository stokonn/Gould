package com.opus.piano.data.offline

import org.json.JSONObject

/**
 * Durable record of one scheduled bundle download job (D5.3 + D7).
 *
 * A job binds exactly one manifest revision (`bundleId:revision`) to:
 *  - the user's **per-job** metered allowance ([meteredAllowed]) — never a global switch,
 *  - whether the size estimate was explicitly confirmed ([confirmed], D7 8 MiB / unknown),
 *  - whether the job may be (re)started automatically ([active]) — a cancelled job is not.
 *
 * Jobs are metadata only: no personal practice data, never part of a portable backup.
 * They survive process death in `goldberg-bundle-jobs` preferences and are re-read by
 * `BundleScheduler.reconcileAfterRestart()`.
 */
data class BundleJob(
    val bundleId: String,
    val revision: Long,
    /** `null` = unknown size, which always requires explicit confirmation (D7). */
    val estimatedBytes: Long?,
    val confirmed: Boolean,
    val meteredAllowed: Boolean,
    val active: Boolean,
    val attempts: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val lastError: String?
) {
    /** Unique WorkManager work name; deduplication key for in-flight requests. */
    val jobId: String get() = id(bundleId, revision)

    init {
        require(bundleId.matches(BundleManifest.ID)) { "bundleId must be a UUID" }
        require(revision >= 1) { "revision must be positive" }
        require(estimatedBytes == null || estimatedBytes >= 0) { "estimatedBytes must not be negative" }
        require(attempts >= 0) { "attempts must not be negative" }
    }

    fun grantMetered() = copy(meteredAllowed = true)

    fun revokeMetered() = copy(meteredAllowed = false)

    fun attempt(error: String?) = copy(attempts = attempts + 1, lastError = error)

    fun succeeded() = copy(active = false, lastError = null)

    fun deactivated(error: String?) = copy(active = false, lastError = error)

    companion object {
        fun id(bundleId: String, revision: Long): String = "$bundleId:$revision"
    }
}

/**
 * Versioned JSON codec for [BundleJob] records. Bounded and strict like the manifest codec:
 * an unreadable job record must never silently become "metered consent granted".
 */
object BundleJobCodec {

    const val FORMAT_VERSION = 1
    const val MAX_ENCODED_BYTES = 16 * 1024

    fun encode(job: BundleJob): String {
        val json = JSONObject().apply {
            put("formatVersion", FORMAT_VERSION)
            put("bundleId", job.bundleId)
            put("revision", job.revision)
            put("estimatedBytes", job.estimatedBytes ?: JSONObject.NULL)
            put("confirmed", job.confirmed)
            put("meteredAllowed", job.meteredAllowed)
            put("active", job.active)
            put("attempts", job.attempts)
            put("createdAt", job.createdAt)
            put("updatedAt", job.updatedAt)
            put("lastError", job.lastError ?: JSONObject.NULL)
        }
        val raw = json.toString()
        require(raw.toByteArray(Charsets.UTF_8).size <= MAX_ENCODED_BYTES) {
            "job record exceeds $MAX_ENCODED_BYTES bytes"
        }
        return raw
    }

    fun decode(raw: String): BundleJob {
        require(raw.toByteArray(Charsets.UTF_8).size <= MAX_ENCODED_BYTES) {
            "job record exceeds $MAX_ENCODED_BYTES bytes"
        }
        val json = JSONObject(raw)
        require(json.optInt("formatVersion", -1) == FORMAT_VERSION) {
            "unsupported job formatVersion ${json.optInt("formatVersion", -1)}"
        }
        val estimated: Long? =
            if (!json.has("estimatedBytes") || json.isNull("estimatedBytes")) null
            else json.getLong("estimatedBytes").also { require(it >= 0) { "estimatedBytes must not be negative" } }
        return BundleJob(
            bundleId = requireString(json, "bundleId"),
            revision = requireLong(json, "revision"),
            estimatedBytes = estimated,
            confirmed = json.getBoolean("confirmed"),
            meteredAllowed = json.getBoolean("meteredAllowed"),
            active = json.getBoolean("active"),
            attempts = json.getInt("attempts"),
            createdAt = requireLong(json, "createdAt"),
            updatedAt = requireLong(json, "updatedAt"),
            lastError = if (json.isNull("lastError")) null else json.getString("lastError")
        )
    }

    private fun requireString(json: JSONObject, key: String): String {
        require(json.has(key) && !json.isNull(key)) { "missing required field '$key'" }
        val value = json.getString(key)
        require(value.isNotBlank()) { "field '$key' must not be blank" }
        return value
    }

    private fun requireLong(json: JSONObject, key: String): Long {
        require(json.has(key) && !json.isNull(key)) { "missing required field '$key'" }
        return json.getLong(key)
    }
}

/**
 * Pure WorkManager scheduling decisions (D5.3).
 *
 * Returns plain strings so the rule is JVM-testable without the WorkManager classpath;
 * `BundleScheduler` maps them to `androidx.work.NetworkType`.
 *
 *  - no per-job metered allowance -> **UNMETERED**: the work waits for Wi-Fi/ethernet and can
 *    never move a byte on mobile data (no automatic background mobile downloads, ever)
 *  - explicit per-job metered allowance -> CONNECTED, and the transfer engine still re-checks
 *    that allowance before every file and during streaming, so revocation pauses immediately
 */
object BundleWorkPolicy {
    const val UNMETERED = "UNMETERED"
    const val CONNECTED = "CONNECTED"

    fun networkType(meteredAllowed: Boolean): String =
        if (meteredAllowed) CONNECTED else UNMETERED
}
