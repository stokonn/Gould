package com.opus.piano.admin

/** Shared consent rules for UI scheduling and the worker. All opt-ins start off. */
data class AdminSyncPolicy(val background: Boolean = false, val notifications: Boolean = false, val mobileConsent: Boolean = false) {
    fun permitsNetwork(wifi: Boolean, metered: Boolean): Boolean = background && (mobileConsent || wifi && !metered)
    fun permitsNotification(permissionGranted: Boolean): Boolean = notifications && permissionGranted
}

internal fun adminFingerprint(value: String): String = java.security.MessageDigest.getInstance("SHA-256")
    .digest(value.toByteArray()).joinToString("") { "%02x".format(it) }
