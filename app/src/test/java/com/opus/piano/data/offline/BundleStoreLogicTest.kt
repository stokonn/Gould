package com.opus.piano.data.offline

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * JVM tests for the store's mark-and-sweep, admission and integrity decisions (D5.2, D7).
 * I/O here is plain java.io — no Android APIs — which is exactly why this logic is separable.
 */
class BundleStoreLogicTest {

    private val shaA = "a".repeat(64)
    private val shaB = "b".repeat(64)
    private val shaC = "c".repeat(64)

    private fun rights() = BundleRights(
        license = "CC0 1.0",
        licenseUrl = "https://creativecommons.org/publicdomain/zero/1.0/",
        attribution = "Open Opus contributors",
        sourceName = "Open Opus",
        sourceUrl = "https://example.org/source"
    )

    private fun manifest(bundleId: String, shas: List<String>): BundleManifest = BundleManifest(
        bundleId = bundleId,
        type = BundleType.WORK,
        entityId = "entity-$bundleId",
        schemaVersion = 1,
        revision = 1,
        language = "en",
        createdAt = 0L,
        validatedAt = 1L,
        dependencies = listOf(),
        expectedBytes = shas.size * 100L,
        files = shas.mapIndexed { index, sha ->
            BundleFile(sha, 100L, "image/jpeg", "media/file$index.jpg", "en", rights())
        },
        providerKind = null,
        providerRevision = null,
        attribution = "Open Opus contributors",
        minReaderVersion = 1,
        state = BundleState.READY,
        progress = BundleProgress(),
        lastError = null,
        updatedAt = 1L
    )

    // ------------------------------------------------------------ integrity

    @Test
    fun sha256MatchesKnownVector() {
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            BundleStoreLogic.sha256("abc".toByteArray())
        )
        assertEquals(64, BundleStoreLogic.sha256(ByteArray(0)).length)
    }

    @Test
    fun sha256OfReadsFileContent() {
        val file = File.createTempFile("bundle", ".tmp")
        try {
            file.writeBytes("abc".toByteArray())
            assertEquals(
                "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                BundleStoreLogic.sha256Of(file)
            )
        } finally {
            file.delete()
        }
    }

    @Test
    fun shaValidationMatchesCodecRule() {
        assertTrue(BundleStoreLogic.isValidSha(shaA))
        assertFalse(BundleStoreLogic.isValidSha("A".repeat(64)))
        assertFalse(BundleStoreLogic.isValidSha("a".repeat(63)))
        assertFalse(BundleStoreLogic.isValidSha(""))
    }

    // ------------------------------------------------------------ mark & sweep

    @Test
    fun sharedContentStaysReferencedUntilLastManifestGoes() {
        val idOne = "11111111-1111-1111-1111-111111111111"
        val idTwo = "22222222-2222-2222-2222-222222222222"
        val one = manifest(idOne, listOf(shaA, shaB))
        val two = manifest(idTwo, listOf(shaA, shaC))          // shares shaA with one
        val available = setOf(shaA, shaB, shaC)

        val referencedBoth = BundleStoreLogic.referencedShas(listOf(one, two))
        assertEquals(setOf(shaA, shaB, shaC), referencedBoth)
        assertEquals(emptySet<String>(), BundleStoreLogic.unreferenced(available, referencedBoth))

        // Removing bundle two must NOT delete shared shaA (still referenced by one).
        val referencedOne = BundleStoreLogic.referencedShas(listOf(one))
        assertEquals(setOf(shaA, shaB), referencedOne)
        assertEquals(setOf(shaC), BundleStoreLogic.unreferenced(available, referencedOne))

        // Removing everything reclaims all content.
        val referencedNone = BundleStoreLogic.referencedShas(listOf())
        assertEquals(setOf(shaA, shaB, shaC), BundleStoreLogic.unreferenced(available, referencedNone))
    }

    @Test
    fun pendingManifestsKeepContentAliveDuringRefresh() {
        val id = "33333333-3333-3333-3333-333333333333"
        val published = manifest(id, listOf(shaA))
        val pending = refreshed(published, revision = 2, shas = listOf(shaA, shaB))
        val referenced = BundleStoreLogic.referencedShas(listOf(published, pending))
        assertEquals(setOf(shaA, shaB), referenced)
        assertEquals(emptySet<String>(), BundleStoreLogic.unreferenced(setOf(shaA, shaB), referenced))
    }

    @Test
    fun orphanedStagedFilesAreExactlyTheUnreferencedOnes() {
        val staged = setOf(shaA, shaB, shaC)
        val expected = setOf(shaA, shaB)
        assertEquals(setOf(shaC), BundleStoreLogic.orphanedStagedNames(staged, expected))
        assertEquals(emptySet<String>(), BundleStoreLogic.orphanedStagedNames(expected, expected))
        assertEquals(staged, BundleStoreLogic.orphanedStagedNames(staged, emptySet()))
    }

    // ------------------------------------------------------------ admission (D7)

    private val cap = 1000L
    private val reserve = 100L

    @Test
    fun admissionAcceptsInsideCeilingAndReserve() {
        assertTrue(BundleStoreLogic.admits(100L, 0L, 0L, 1000L, reserve, cap))
        assertTrue("exact boundary leaves the reserve untouched",
            BundleStoreLogic.admits(100L, 0L, 0L, 200L, reserve, cap))
        assertTrue("combined counting with existing usage",
            BundleStoreLogic.admits(100L, 300L, 100L, 10_000L, reserve, cap))
    }

    @Test
    fun admissionRefusesWhenReserveWouldBeViolated() {
        assertFalse(BundleStoreLogic.admits(101L, 0L, 0L, 200L, reserve, cap))
        assertFalse("free space below reserve never admits",
            BundleStoreLogic.admits(0L, 0L, 0L, reserve - 1, reserve, cap))
    }

    @Test
    fun admissionRefusesCombinedCeilingOverflowInsteadOfEvicting() {
        assertFalse("bundle bytes count toward the ceiling",
            BundleStoreLogic.admits(100L, 600L, 400L, 100_000L, reserve, cap))
        assertFalse("other usage counts toward the ceiling",
            BundleStoreLogic.admits(100L, 0L, 950L, 100_000L, reserve, cap))
        assertFalse("single estimate beyond the whole ceiling",
            BundleStoreLogic.admits(cap + 1, 0L, 0L, 100_000L, reserve, cap))
        assertFalse("negative estimates are invalid",
            BundleStoreLogic.admits(-1L, 0L, 0L, 100_000L, reserve, cap))
    }

    @Test
    fun directoryBytesCountsOnlyExistingFiles() {
        val missing = File("/definitely/not/created/${System.nanoTime()}")
        assertEquals(0L, BundleStoreLogic.directoryBytes(missing))
        val dir = File.createTempFile("bytes", "dir").let { f -> f.delete(); f.mkdirs(); f }
        try {
            File(dir, "one").writeBytes(ByteArray(10))
            File(dir, "two").writeBytes(ByteArray(20))
            assertEquals(30L, BundleStoreLogic.directoryBytes(dir))
        } finally {
            dir.deleteRecursively()
        }
    }
}

/** Small test-only helper to build a refreshed revision without duplicating fixture code. */
internal fun refreshed(
    source: BundleManifest,
    revision: Long,
    shas: List<String>
): BundleManifest = source.copy(
    revision = revision,
    expectedBytes = shas.size * 100L,
    files = shas.mapIndexed { index, sha ->
        BundleFile(sha, 100L, "image/jpeg", "media/rev$revision-$index.jpg", "en",
            BundleRights("CC0 1.0", "https://creativecommons.org/publicdomain/zero/1.0/",
                "Open Opus contributors", "Open Opus", "https://example.org/source"))
    }
)
