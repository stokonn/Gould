package com.opus.piano.data.offline

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

/**
 * Durable job state (D5.3/D7): per-job metered consent, confirmation, cancellation and
 * corrupt-record tolerance, all on the JVM with an in-memory preferences fake.
 */
class BundleJobTest {

    private class MemoryPreferences : BundleJobPreferences {
        val values = linkedMapOf<String, String>()
        override fun read(key: String): String? = values[key]
        override fun write(key: String, value: String?) {
            if (value == null) values.remove(key) else values[key] = value
        }
        override fun keys(): Set<String> = values.keys.toSet()
    }

    private val bundleId = UUID.randomUUID().toString()

    private fun job(
        revision: Long = 1,
        meteredAllowed: Boolean = false,
        confirmed: Boolean = true,
        active: Boolean = true,
        estimatedBytes: Long? = 4L * 1024 * 1024
    ) = BundleJob(
        bundleId = bundleId,
        revision = revision,
        estimatedBytes = estimatedBytes,
        confirmed = confirmed,
        meteredAllowed = meteredAllowed,
        active = active,
        attempts = 0,
        createdAt = 10L,
        updatedAt = 11L,
        lastError = null
    )

    // ------------------------------------------------------------ codec

    @Test
    fun jobCodecRoundTripsEveryFieldIncludingNullEstimate() {
        val original = job(meteredAllowed = true, estimatedBytes = null).copy(
            attempts = 3,
            lastError = BundlePolicy.Reasons.CONSENT
        )
        val decoded = BundleJobCodec.decode(BundleJobCodec.encode(original))
        assertEquals(original, decoded)
        assertEquals("${original.bundleId}:1", original.jobId)
    }

    @Test
    fun jobCodecRejectsUnknownFormatAndMalformedBodies() {
        assertThrows(IllegalArgumentException::class.java) {
            BundleJobCodec.decode("""{"formatVersion":99}""")
        }
        assertThrows(Exception::class.java) {
            BundleJobCodec.decode("not json")
        }
        val valid = BundleJobCodec.encode(job())
        assertTrue(valid.toByteArray(Charsets.UTF_8).size <= BundleJobCodec.MAX_ENCODED_BYTES)
    }

    @Test
    fun malformedJobShapeIsRejectedAtConstruction() {
        assertThrows(IllegalArgumentException::class.java) { job(revision = 0) }
        assertThrows(IllegalArgumentException::class.java) {
            job().copy(bundleId = "not-a-uuid")
        }
        assertThrows(IllegalArgumentException::class.java) { job(estimatedBytes = -1L) }
    }

    // ------------------------------------------------------------ store

    @Test
    fun storePersistsGrantsRevocationsAndAttempts() {
        val prefs = MemoryPreferences()
        val store = BundleJobStore(prefs) { 42L }
        store.put(job())
        assertEquals(42L, store.get("${bundleId}:1")!!.updatedAt)

        assertTrue(store.grantMetered("${bundleId}:1")!!.meteredAllowed)
        assertTrue(store.get("${bundleId}:1")!!.meteredAllowed)
        // D7 revocation is durable and immediate.
        assertTrue(store.revokeMetered("${bundleId}:1")!!.meteredAllowed.not())
        assertFalse(store.get("${bundleId}:1")!!.meteredAllowed)

        store.recordAttempt("${bundleId}:1", BundlePolicy.Reasons.TRANSPORT)
        val attempted = store.get("${bundleId}:1")!!
        assertEquals(1, attempted.attempts)
        assertEquals(BundlePolicy.Reasons.TRANSPORT, attempted.lastError)

        store.deactivate("${bundleId}:1", BundlePolicy.Reasons.CANCELLED)
        val cancelled = store.get("${bundleId}:1")!!
        assertFalse("cancelled jobs must never auto-restart", cancelled.active)
        // Attempts survive cancellation; revocation never resurrects a cancelled job.
        assertEquals(1, cancelled.attempts)
    }

    @Test
    fun storeNeverTreatsCorruptRecordsAsConsent() {
        val prefs = MemoryPreferences()
        val store = BundleJobStore(prefs)
        prefs.write(BundleJobStore.PREFIX + "$bundleId:7", "{corrupt")
        assertNull(store.get("$bundleId:7"))
        assertNull(store.grantMetered("$bundleId:7"))
        assertNull(store.deactivate("$bundleId:7", "x"))
        assertTrue(store.list().isEmpty())
        store.put(job(revision = 7))
        assertEquals(1, store.list().size)
        store.remove("$bundleId:7")
        assertNull(store.get("$bundleId:7"))
    }

    @Test
    fun storeListsOldestFirstAndKeepsIndependentJobs() {
        val prefs = MemoryPreferences()
        val store = BundleJobStore(prefs) { 99L }
        store.put(job(revision = 2))
        store.put(job(revision = 1))
        val listed = store.list()
        assertEquals(listOf(1L, 2L), listed.map { it.revision })
        // One revision's revocation must not touch another job.
        store.revokeMetered("${bundleId}:2")
        assertTrue(store.get("${bundleId}:1")!!.meteredAllowed.not())
        assertTrue(store.get("${bundleId}:2")!!.meteredAllowed.not())
        store.grantMetered("${bundleId}:1")
        assertFalse("consent is per job, never global", store.get("${bundleId}:2")!!.meteredAllowed)
    }

    // ------------------------------------------------------------ scheduling policy

    @Test
    fun backgroundWorkIsUnmeteredUnlessTheJobHoldsConsent() {
        assertEquals(BundleWorkPolicy.UNMETERED, BundleWorkPolicy.networkType(meteredAllowed = false))
        assertEquals(BundleWorkPolicy.CONNECTED, BundleWorkPolicy.networkType(meteredAllowed = true))
    }

    @Test
    fun confirmationFlagIsIndependentOfConsent() {
        val needsConfirmation = job(estimatedBytes = BundlePolicy.LARGE_JOB_BYTES + 1, confirmed = false)
        assertTrue(BundlePolicy.requiresConfirmation(needsConfirmation.estimatedBytes))
        assertFalse(needsConfirmation.confirmed)
        val unknownSize = job(estimatedBytes = null, confirmed = true, meteredAllowed = true)
        assertTrue("unknown size always needs bounded approval", BundlePolicy.requiresConfirmation(unknownSize.estimatedBytes))
        assertTrue(unknownSize.confirmed)
        assertTrue(unknownSize.meteredAllowed)
    }
}
