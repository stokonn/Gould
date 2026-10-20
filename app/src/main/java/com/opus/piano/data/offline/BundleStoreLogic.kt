package com.opus.piano.data.offline

import com.opus.piano.data.catalogue.CatalogueBudget
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

/**
 * Pure decision logic for the content-addressed bundle store (D5.1/D5.2).
 *
 * Kept free of Android APIs (`AtomicFile`, `Context`) so every rule below runs as a JVM
 * unit test. `BundleStore` performs I/O and delegates all arithmetic/mark decisions here.
 */
internal object BundleStoreLogic {

    val SHA256 = Regex("[0-9a-f]{64}")

    /** Aligned with D7 / CatalogueStorage.MAX_TOTAL; injected at wiring so tests can vary it. */
    const val DEFAULT_MAX_TOTAL_BYTES = 256L * 1024 * 1024

    fun isValidSha(sha256: String): Boolean = SHA256.matches(sha256)

    fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

    fun sha256Of(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    /** Every content file kept alive by any manifest — published *or* pending (D5.2). */
    fun referencedShas(manifests: Iterable<BundleManifest>): Set<String> {
        val referenced = mutableSetOf<String>()
        manifests.forEach { manifest -> manifest.files.forEach { referenced += it.sha256 } }
        return referenced
    }

    /** Mark phase result: available content minus everything any manifest still references. */
    fun unreferenced(available: Set<String>, referenced: Set<String>): Set<String> = available - referenced

    /**
     * Admission math (D7): estimated bytes must fit the combined 256 MiB ceiling *and* leave
     * the protected free-space reserve intact. Never evicts pinned content — a refusal is a
     * hard "cannot fit", not a silent deletion.
     */
    fun admits(
        estimatedBytes: Long,
        bundleUsedBytes: Long,
        otherUsedBytes: Long,
        freeBytes: Long,
        reserveBytes: Long,
        maxTotalBytes: Long = DEFAULT_MAX_TOTAL_BYTES
    ): Boolean {
        if (estimatedBytes < 0 || bundleUsedBytes < 0 || otherUsedBytes < 0) return false
        if (estimatedBytes > maxTotalBytes) return false
        if (bundleUsedBytes + otherUsedBytes + estimatedBytes > maxTotalBytes) return false
        return CatalogueBudget.fits(freeBytes, estimatedBytes, reserveBytes)
    }

    /** Combined bundle footprint (blobs + manifests + staging) for admission and reporting. */
    fun directoryBytes(directory: File): Long =
        if (!directory.exists()) 0L else directory.walkTopDown().filter { it.isFile }.sumOf { it.length() }

    /**
     * Staged partial files that may be deleted after a crash: present on disk but not
     * expected by any still-active (pending) manifest. Reconciliation removes only these.
     */
    fun orphanedStagedNames(stagedNames: Set<String>, expectedByPending: Set<String>): Set<String> =
        stagedNames - expectedByPending
}
