package com.opus.piano.data.offline

import android.util.AtomicFile
import com.opus.piano.data.catalogue.CatalogueIntake
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Content-addressed bundle store (D5.1 file-based, D5.2 mark-and-sweep, D7 budgets).
 *
 * Layout (all app-private):
 * ```
 * filesDir/offline-bundles/manifests/<bundleId>.json        active revision (atomic writes)
 * filesDir/offline-bundles/pending/<bundleId>.rev<N>.json   newer revision while refreshing
 * filesDir/offline-bundles/blobs/<sha256>                   shared, never evicted while referenced
 * filesDir/offline-bundles/quarantine/                      unparsable manifests + corrupt blobs (moved, never deleted)
 * cacheDir/offline-bundles/staging/                         disposable partial downloads
 * ```
 *
 * Invariants:
 *  - a refresh builds `pending/` and is promoted by a single atomic rename **only after**
 *    validation, so an interrupted refresh leaves the previous READY revision untouched
 *  - deletion is mark-and-sweep over published **and** pending manifests with reference
 *    counting by sha256; shared media survives until the last referencing manifest goes
 *  - the protected free-space reserve and the D7 combined ceiling gate every write; nothing
 *    pinned is ever evicted to make room — admission refusal is explicit
 *  - corrupt manifests are quarantined (moved, bytes preserved), never deleted
 *
 * I/O only; all arithmetic and mark decisions live in [BundleStoreLogic] (JVM-tested).
 * Not JVM-unit-testable itself because of `AtomicFile` — covered by Stage 5 instrumentation tests.
 */
class BundleStore(
    private val root: File,
    private val stagingRoot: File,
    private val reserveBytes: () -> Long,
    private val otherUsedBytes: () -> Long = { 0L },
    private val maxTotalBytes: Long = BundleStoreLogic.DEFAULT_MAX_TOTAL_BYTES
) {
    private val mutex = Mutex()
    private val manifestsDir = File(root, "manifests")
    private val pendingDir = File(root, "pending")
    private val blobsDir = File(root, "blobs")
    private val quarantineDir = File(root, "quarantine")

    /** Single-writer admission boundary bound to *this* store's budget (bundles counted). */
    private val intake = CatalogueIntake { estimatedBytes -> admits(estimatedBytes) }

    init {
        manifestsDir.mkdirs()
        pendingDir.mkdirs()
        blobsDir.mkdirs()
        stagingRoot.mkdirs()
    }

    // ------------------------------------------------------------ paths

    fun manifestFile(bundleId: String): File {
        require(bundleId.matches(BundleManifest.ID)) { "bundleId must be a UUID" }
        return File(manifestsDir, "$bundleId.json")
    }

    fun pendingFile(bundleId: String, revision: Long): File {
        require(bundleId.matches(BundleManifest.ID)) { "bundleId must be a UUID" }
        require(revision >= 1) { "revision must be positive" }
        return File(pendingDir, "$bundleId.rev$revision.json")
    }

    fun blobFile(sha256: String): File {
        require(BundleStoreLogic.isValidSha(sha256)) { "sha256 must be 64 lowercase hex characters" }
        return File(blobsDir, sha256)
    }

    // ------------------------------------------------------------ reads

    /**
     * Published manifests — authoritative states only: [BundleState.READY],
     * [BundleState.STALE] and [BundleState.QUARANTINED] (exactly the states allowed to carry
     * a validation timestamp). Working/partial revisions (QUEUED, WAITING_FOR_POLICY,
     * DOWNLOADING, VALIDATING, INSTALLING, PAUSED, FAILED_RETRYABLE) share the same
     * directory for routing but are never exposed as published. Unparsable files are
     * quarantined, not lost.
     */
    suspend fun readPublished(): BundleReadResult = withContext(Dispatchers.IO) { mutex.withLock {
        val read = readDir(manifestsDir)
        read.copy(manifests = read.manifests.filter { it.state in PUBLISHED_STATES })
    } }

    /** Working revisions that have not been promoted yet (refresh in flight). */
    suspend fun readPending(): BundleReadResult = withContext(Dispatchers.IO) { mutex.withLock {
        readDir(pendingDir)
    } }

    /** Union used by mark-and-sweep and reconciliation: both locations keep content alive. */
    suspend fun readAll(): BundleReadResult = withContext(Dispatchers.IO) { mutex.withLock {
        val published = readDir(manifestsDir)
        val pending = readDir(pendingDir)
        BundleReadResult(published.manifests + pending.manifests, published.quarantined + pending.quarantined)
    } }

    // ------------------------------------------------------------ writes

    /**
     * Atomically persists manifest state/progress. Routes a *newer* revision to `pending/`
     * (protecting the published READY copy) and the active revision to `manifests/`.
     */
    suspend fun save(manifest: BundleManifest) = withContext(Dispatchers.IO) { mutex.withLock {
        val raw = BundleManifestCodec.encode(manifest).toByteArray()
        val target = route(manifest)
        intake.commit(raw.size.toLong() + PUBLICATION_OVERHEAD_BYTES) {
            writeAtomic(target, raw)
        }
    } }

    /**
     * Publishes the newest pending revision by a single atomic rename — called only after
     * validation (state == READY, codec validation ran on decode). Returns the manifest.
     */
    suspend fun promote(bundleId: String): BundleManifest = withContext(Dispatchers.IO) { mutex.withLock {
        val candidates = pendingFiles(bundleId)
        require(candidates.isNotEmpty()) { "No pending revision for $bundleId" }
        val newest = candidates.maxBy { revisionOf(it) }
        val manifest = decodeRequired(newest)
        require(manifest.state == BundleState.READY) { "Only a validated READY revision may be published" }
        val published = manifestFile(bundleId)
        if (published.exists()) {
            require(manifest.revision > revisionOf(published)) {
                "Pending revision must be newer than the published one"
            }
        }
        // Atomic replace: a crash leaves either the previous READY revision or the new one.
        require(newest.renameTo(published)) { "Atomic publish failed for $bundleId" }
        (candidates - newest).forEach { it.delete() }
        sweepLocked()
        manifest
    } }

    /** Removes one bundle's manifests, then reclaims every now-unreferenced blob. */
    suspend fun remove(bundleId: String) = withContext(Dispatchers.IO) { mutex.withLock {
        manifestFile(bundleId).delete()
        pendingFiles(bundleId).forEach { it.delete() }
        sweepLocked()
    } }

    /** Removes all bundles (personal data is untouched: manifests contain none). */
    suspend fun removeAll() = withContext(Dispatchers.IO) { mutex.withLock {
        manifestsDir.listFiles()?.forEach { it.delete() }
        pendingDir.listFiles()?.forEach { it.delete() }
        sweepLocked()
    } }

    /**
     * Moves a corrupt content blob to quarantine (bytes preserved, never silently deleted)
     * so the next attempt can re-download it. Called only from hash verification: the bytes
     * on disk did not match their own declared digest, so nothing usable is lost.
     */
    suspend fun quarantineBlob(sha256: String, reason: String): Boolean =
        withContext(Dispatchers.IO) { mutex.withLock {
            val blob = blobFile(sha256)
            if (!blob.isFile) return@withLock false
            quarantineDir.mkdirs()
            val safeReason = reason.replace(NON_ALPHANUMERIC, "_").take(MAX_REASON_CHARS)
            val target = File(quarantineDir, "$sha256.${safeReason}.${System.currentTimeMillis()}")
            blob.renameTo(target)
        } }

    /**
     * Verifies a staged partial against its declared sha256 and moves it into the blob store
     * (deduplicating shared content). Bytes were already admitted before download, so no new
     * reservation is consumed; returns false on any integrity mismatch (never publishes bad data).
     */
    suspend fun installBlob(staged: File, sha256: String, expectedBytes: Long? = null): Boolean =
        withContext(Dispatchers.IO) {
            require(BundleStoreLogic.isValidSha(sha256)) { "sha256 must be 64 lowercase hex characters" }
            require(staged.exists()) { "staged file missing" }
            if (expectedBytes != null && staged.length() != expectedBytes) return@withContext false
            if (BundleStoreLogic.sha256Of(staged) != sha256) return@withContext false
            val target = blobFile(sha256)
            mutex.withLock {
                if (target.isFile && target.length() == staged.length() &&
                    BundleStoreLogic.sha256Of(target) == sha256
                ) {
                    staged.delete()
                    return@withLock
                }
                if (target.exists()) {
                    // Dedup found bytes that do not match their declared digest: preserve
                    // them in quarantine (never silently delete), then install the verified
                    // staged copy so this very attempt repairs the corruption.
                    quarantineDir.mkdirs()
                    val preserved = File(quarantineDir, "$sha256.$DEDUP_CORRUPT_REASON.${System.currentTimeMillis()}")
                    if (!target.renameTo(preserved)) return@withContext false
                }
                target.parentFile?.mkdirs()
                if (!staged.renameTo(target)) {
                    // Cross-volume fallback: bytes are already admitted, copy then release staging.
                    intake.commit(staged.length()) {
                        staged.inputStream().use { input ->
                            target.outputStream().use { output -> input.copyTo(output) }
                        }
                        staged.delete()
                    }
                }
            }
            true
        }

    // ------------------------------------------------------------ budgets & sweep

    /** D7 admission: combined ceiling + protected reserve; explicit refusal, never eviction. */
    fun admits(estimatedBytes: Long): Boolean {
        if (estimatedBytes < 0) return false
        val free = root.usableSpace
        val bundleUsed = BundleStoreLogic.directoryBytes(root) + BundleStoreLogic.directoryBytes(stagingRoot)
        return BundleStoreLogic.admits(
            estimatedBytes = estimatedBytes,
            bundleUsedBytes = bundleUsed,
            otherUsedBytes = otherUsedBytes(),
            freeBytes = free,
            reserveBytes = reserveBytes(),
            maxTotalBytes = maxTotalBytes
        )
    }

    /** Mark-and-sweep over published + pending manifests; also reconciles staging. */
    suspend fun sweep(): SweepReport = withContext(Dispatchers.IO) { mutex.withLock { sweepLocked() } }

    private fun sweepLocked(): SweepReport {
        val active = readDir(manifestsDir).manifests + readDir(pendingDir).manifests
        val referenced = BundleStoreLogic.referencedShas(active)

        var removedBlobs = 0
        blobsDir.listFiles().orEmpty().forEach { blob ->
            if (blob.isFile && blob.name !in referenced && BundleStoreLogic.isValidSha(blob.name)) {
                if (blob.delete()) removedBlobs++
            }
        }

        // Keep a staged partial only while some manifest still wants bytes we do not have.
        val available = blobsDir.listFiles().orEmpty().filter { it.isFile }.map { it.name }.toSet()
        val keepStaged = referenced - available
        var removedStaged = 0
        stagingRoot.listFiles().orEmpty().forEach { staged ->
            if (staged.isFile) {
                val sha = staged.name.removeSuffix(STAGED_SUFFIX)
                if (sha !in keepStaged) {
                    if (staged.delete()) removedStaged++
                }
            }
        }
        return SweepReport(removedBlobs, removedStaged, referenced.size)
    }

    /** Combined on-disk footprint of this store (reporting/metering). */
    suspend fun totalBytes(): Long = withContext(Dispatchers.IO) { mutex.withLock {
        BundleStoreLogic.directoryBytes(root) + BundleStoreLogic.directoryBytes(stagingRoot)
    } }

    // ------------------------------------------------------------ internals

    private fun route(manifest: BundleManifest): File {
        val published = manifestFile(manifest.bundleId)
        if (!published.exists()) return published
        val publishedRevision = revisionOf(published)
        return when {
            manifest.revision == publishedRevision -> published
            manifest.revision > publishedRevision -> pendingFile(manifest.bundleId, manifest.revision)
            else -> throw IllegalStateException(
                "Refusing to overwrite published revision $publishedRevision with ${manifest.revision}"
            )
        }
    }

    private fun revisionOf(file: File): Long = decodeRequired(file).revision

    private fun decodeRequired(file: File): BundleManifest {
        val raw = AtomicFile(file).openRead().use { it.readBytes() }
        return BundleManifestCodec.decode(raw.toString(Charsets.UTF_8))
    }

    /** Read a manifest directory; move unparsable files to quarantine (bytes preserved). */
    private fun readDir(directory: File): BundleReadResult {
        val manifests = mutableListOf<BundleManifest>()
        val quarantined = mutableListOf<String>()
        val files = directory.listFiles()?.filter { it.isFile && it.name.endsWith(".json") }.orEmpty()
        for (file in files) {
            try {
                manifests += decodeRequired(file)
            } catch (_: Exception) {
                quarantineDir.mkdirs()
                val target = File(quarantineDir, "${file.name}.${System.currentTimeMillis()}")
                if (!file.renameTo(target)) file.delete()
                quarantined += file.name
            }
        }
        return BundleReadResult(manifests, quarantined)
    }

    private fun pendingFiles(bundleId: String): List<File> {
        require(bundleId.matches(BundleManifest.ID)) { "bundleId must be a UUID" }
        return pendingDir.listFiles()
            .orEmpty()
            .filter { it.isFile && it.name.startsWith("$bundleId.rev") && it.name.endsWith(".json") }
    }

    private fun writeAtomic(target: File, bytes: ByteArray) {
        target.parentFile?.mkdirs()
        val atomic = AtomicFile(target)
        val stream = atomic.startWrite()
        try {
            stream.write(bytes)
            atomic.finishWrite(stream)
        } catch (e: Exception) {
            atomic.failWrite(stream)
            throw e
        }
    }

    companion object {
        const val STAGED_SUFFIX = ".part"
        const val PUBLICATION_OVERHEAD_BYTES = 64L * 1024
        const val DEDUP_CORRUPT_REASON = "dedup_corrupt"
        private val NON_ALPHANUMERIC = Regex("[^A-Za-z0-9_.-]")
        private const val MAX_REASON_CHARS = 48

        /** Authoritative states exposed by [readPublished]; working/partial states are not. */
        private val PUBLISHED_STATES = setOf(BundleState.READY, BundleState.STALE, BundleState.QUARANTINED)
    }
}

/** Read result: usable manifests plus names of files that were moved to quarantine. */
data class BundleReadResult(val manifests: List<BundleManifest>, val quarantined: List<String>)

/** Evidence-friendly sweep outcome (counts are files, not bytes). */
data class SweepReport(val removedBlobs: Int, val removedStaged: Int, val retainedReferenced: Int)
