package com.opus.piano.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

/** Exists only while the app composition exists. No background polling or scheduled network work. */
@Composable internal fun ObserveConnectivity(catalog: CatalogViewModel, route: String) {
    val manager = LocalContext.current.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val currentRoute by rememberUpdatedState(route)
    val lifecycle = androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
    DisposableEffect(manager, catalog, lifecycle) {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                    android.os.Handler(android.os.Looper.getMainLooper()).post { if (lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) catalog.retryVisible(currentRoute) }
                }
            }
        }
        var registered = false
        val observer = androidx.lifecycle.LifecycleEventObserver { _, _ ->
            val active = lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)
            if (active && !registered) { manager.registerDefaultNetworkCallback(callback); registered = true }
            else if (!active && registered) { manager.unregisterNetworkCallback(callback); registered = false }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer); if (registered) manager.unregisterNetworkCallback(callback) }
    }
}
