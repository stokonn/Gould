package com.opus.piano.preferences

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.MutableStateFlow

/** General internet permission is never treated as metered-media consent. */
object MediaPolicy {
    fun allows(connected: Boolean, wifi: Boolean, metered: Boolean, consent: Boolean): Boolean =
        connected && ((wifi && !metered) || consent)
}
class MediaPreferences(private val context: Context) {
    private val prefs=context.getSharedPreferences("goldberg-media",Context.MODE_PRIVATE)
    val cellular=MutableStateFlow(prefs.getBoolean("metered-consent",false))
    fun consent(value: Boolean) { check(prefs.edit().putBoolean("metered-consent",value).commit());cellular.value=value }
    fun allowed(): Boolean {
        val manager=context.getSystemService(ConnectivityManager::class.java)
        val capabilities=manager.getNetworkCapabilities(manager.activeNetwork)
        return MediaPolicy.allows(capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)==true,
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)==true,manager.isActiveNetworkMetered,cellular.value)
    }
}
