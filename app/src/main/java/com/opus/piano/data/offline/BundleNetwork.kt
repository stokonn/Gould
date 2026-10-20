package com.opus.piano.data.offline

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Live network facts for bundle transfers.
 *
 * Deliberately a tiny interface: [BundlePolicy] decisions are pure and JVM-tested, while the
 * production implementation reads the real [ConnectivityManager]. The engine calls [facts]
 * before every file **and** during streaming, so consent revocation, Wi-Fi to mobile-data
 * transitions, captive portals and Data Saver take effect within one read window.
 */
fun interface BundleNetwork {
    fun facts(): NetworkFacts
}

/**
 * Real device/network snapshot (D5.3/D7).
 *
 *  - `connected`    -> the active network advertises INTERNET
 *  - `validated`    -> Google connectivity check passed (no captive portal / blackhole)
 *  - `metered`      -> ConnectivityManager says the active network is metered, which also
 *                      covers metered Wi-Fi and Data Saver's per-app metering signals
 *  - `backgroundRestricted` -> Data Saver restricts background traffic for this app
 *
 * VPN/captive-portal/metered-Wi-Fi cases are therefore never inferred from UNMETERED
 * scheduling alone (see docs/planning/goldberg-0.11/OFFLINE-CACHE.md).
 */
class AndroidBundleNetwork(context: Context) : BundleNetwork {
    private val connectivity = context.getSystemService(ConnectivityManager::class.java)

    override fun facts(): NetworkFacts {
        val network = connectivity.activeNetwork ?: return NetworkFacts.DISCONNECTED
        val capabilities = connectivity.getNetworkCapabilities(network) ?: return NetworkFacts.DISCONNECTED
        val connected = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        if (!connected) return NetworkFacts.DISCONNECTED
        val validated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        val metered = connectivity.isActiveNetworkMetered
        return NetworkFacts(
            connected = true,
            validated = validated,
            unmetered = !metered,
            metered = metered,
            backgroundRestricted = backgroundRestricted()
        )
    }

    /**
     * Data Saver ("Restrict background data"). API 36 renamed and moved this query; the
     * androidx compat path keeps API 26+ devices working with the same status values.
     */
    @Suppress("DEPRECATION")
    private fun backgroundRestricted(): Boolean {
        val status = if (android.os.Build.VERSION.SDK_INT >= 36) {
            connectivity.getRestrictBackgroundStatus()
        } else {
            androidx.core.net.ConnectivityManagerCompat.getRestrictBackgroundStatus(connectivity)
        }
        // "Enabled" == Data Saver is on and this app is not exempted from the restriction.
        return status == ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED
    }
}
