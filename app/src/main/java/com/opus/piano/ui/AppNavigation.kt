package com.opus.piano.ui

import com.opus.piano.i18n.L
import com.opus.piano.R
import androidx.navigation.NavHostController

internal const val PROFILE_ROUTE = "HOME" // Keep the persisted 0.6.0 route identity.
internal const val SETTINGS_ROUTE = "settings"
internal fun selectedTab(route: String): String = when {
    route == SETTINGS_ROUTE || route == PROFILE_ROUTE -> PROFILE_ROUTE
    route.startsWith("catalog") || route.startsWith("piece/") || route.startsWith("recordings/") -> "REPERTOIRE"
    route.startsWith("badge/") || route == "BADGES" -> PROFILE_ROUTE
    route.startsWith("history") -> PROFILE_ROUTE
    else -> route
}
internal fun NavHostController.selectTopLevel(route: String) {
    if (currentDestination?.route == route) return
    // A tab tap selects its root, never a saved Search/detail stack associated with that root.
    if (popBackStack(route, false)) return
    navigate(route) {
        popUpTo(graph.startDestinationId)
        launchSingleTop = true
    }
}
