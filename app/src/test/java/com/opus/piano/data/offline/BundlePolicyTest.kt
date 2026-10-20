package com.opus.piano.data.offline

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/** D7 policy matrix: validated networks, per-job consent, revocation, confirmation gates. */
class BundlePolicyTest {

    private val wifi = NetworkFacts(connected = true, validated = true, unmetered = true, metered = false)
    private val mobile = NetworkFacts(connected = true, validated = true, unmetered = false, metered = true)

    @Test
    fun unmeteredValidatedNetworkNeedsNoConsent() {
        assertTrue(BundlePolicy.allowsTransfer(wifi, jobMeteredAllowed = false))
        assertTrue(BundlePolicy.allowsTransfer(wifi, jobMeteredAllowed = true))
        assertTrue(BundlePolicy.allowsBackgroundJob(wifi))
    }

    @Test
    fun meteredRequiresExplicitPerJobAllowance() {
        assertFalse(BundlePolicy.allowsTransfer(mobile, jobMeteredAllowed = false))
        assertTrue(BundlePolicy.allowsTransfer(mobile, jobMeteredAllowed = true))
    }

    @Test
    fun backgroundJobsAreNeverMetered() {
        // D7: no automatic mobile background download, even if a foreground allowance exists.
        assertFalse(BundlePolicy.allowsBackgroundJob(mobile))
    }

    @Test
    fun unvalidatedOrDisconnectedNeverTransfers() {
        val captive = NetworkFacts(connected = true, validated = false, unmetered = true, metered = false)
        assertFalse(BundlePolicy.allowsTransfer(captive, jobMeteredAllowed = false))
        assertFalse(BundlePolicy.allowsTransfer(captive, jobMeteredAllowed = true))
        assertFalse(BundlePolicy.allowsTransfer(NetworkFacts.DISCONNECTED, jobMeteredAllowed = true))
        assertFalse(BundlePolicy.allowsBackgroundJob(NetworkFacts.DISCONNECTED))
    }

    @Test
    fun revokingAllowanceDuringStreamingStopsTheJob() {
        assertTrue(BundlePolicy.keepsRunning(mobile, jobMeteredAllowed = true))
        assertFalse(BundlePolicy.keepsRunning(mobile, jobMeteredAllowed = false))
        // Unmetered stays on regardless of any metered allowance.
        assertTrue(BundlePolicy.keepsRunning(wifi, jobMeteredAllowed = false))
    }

    @Test
    fun confirmationGatesFollowD7Thresholds() {
        assertTrue("unknown size always needs bounded approval", BundlePolicy.requiresConfirmation(null))
        assertTrue("negative estimate is invalid", BundlePolicy.requiresConfirmation(-1L))
        assertFalse(BundlePolicy.requiresConfirmation(1L))
        assertFalse(BundlePolicy.requiresConfirmation(BundlePolicy.LARGE_JOB_BYTES))
        assertTrue(BundlePolicy.requiresConfirmation(BundlePolicy.LARGE_JOB_BYTES + 1))
    }

    @Test
    fun policyConstantsMatchApprovedD7Values() {
        assertEquals(8L * 1024 * 1024, BundlePolicy.LARGE_JOB_BYTES)
        assertEquals(256L * 1024 * 1024, BundlePolicy.MAX_TOTAL_BYTES)
        assertEquals(128L * 1024 * 1024, BundlePolicy.MIN_FREE_RESERVE_BYTES)
    }

    @Test
    fun inconsistentNetworkFactsAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            NetworkFacts(connected = true, validated = true, unmetered = true, metered = true)
        }
    }

    @Test
    fun dataSaverBlocksMeteredBackgroundTrafficButNotUnmetered() {
        val meteredRestricted = mobile.copy(backgroundRestricted = true)
        assertFalse(BundlePolicy.allowsTransfer(meteredRestricted, jobMeteredAllowed = true))
        assertFalse(BundlePolicy.allowsBackgroundJob(meteredRestricted))
        // Data Saver restricts metered networks only; Wi-Fi transfers are unaffected.
        assertTrue(BundlePolicy.allowsTransfer(wifi.copy(backgroundRestricted = true), jobMeteredAllowed = false))
    }

    @Test
    fun stopReasonDistinguishesWaitingFromConsent() {
        assertEquals(BundlePolicy.Reasons.OFFLINE, BundlePolicy.Reasons.stopReason(NetworkFacts.DISCONNECTED, false))
        val captive = NetworkFacts(connected = true, validated = false, unmetered = true, metered = false)
        assertEquals(BundlePolicy.Reasons.UNVALIDATED, BundlePolicy.Reasons.stopReason(captive, true))
        org.junit.Assert.assertNull(BundlePolicy.Reasons.stopReason(wifi, false))
        assertEquals(BundlePolicy.Reasons.DATA_SAVER, BundlePolicy.Reasons.stopReason(mobile.copy(backgroundRestricted = true), true))
        assertEquals(BundlePolicy.Reasons.CONSENT, BundlePolicy.Reasons.stopReason(mobile, false))
        assertEquals(null, BundlePolicy.Reasons.stopReason(mobile, true))
        // Environment problems are waited out; consent is a durable pause.
        assertTrue(BundlePolicy.Reasons.OFFLINE in BundlePolicy.Reasons.WAITING)
        assertTrue(BundlePolicy.Reasons.DATA_SAVER in BundlePolicy.Reasons.WAITING)
        assertFalse(BundlePolicy.Reasons.CONSENT in BundlePolicy.Reasons.WAITING)
    }
}
