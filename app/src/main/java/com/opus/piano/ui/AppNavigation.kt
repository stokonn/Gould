package com.opus.piano.ui

import com.opus.piano.i18n.L
import com.opus.piano.R
import androidx.navigation.NavHostController

internal object Routes {
    const val OVERVIEW = "OVERVIEW"
    const val PROFILE = "HOME" // Keep the persisted 0.6.0 route identity.
    const val REPERTOIRE = "REPERTOIRE"
    const val DISCOVER = "DISCOVER"
    const val ACTIVITY = "ACTIVITY"
    const val PRACTICE = "PRACTICE"
    const val BADGES = "BADGES"
    const val WRAPPED = "WRAPPED"
    const val SETTINGS = "settings"
    const val COSMETICS = "COSMETICS"
    const val CATALOG = "catalog"
    const val CATALOG_COMPOSER = "catalog/composer/{id}"
    const val CATALOG_WORK = "catalog/work/{id}"
    const val PIECE = "piece/{id}"
    const val RECORDINGS = "recordings/{key}"
    const val HISTORY = "history"
    const val PREVIOUS = "previous"
    const val HISTORY_PIECE = "history/piece/{id}"
    const val HISTORY_CATALOG = "history/catalog/{id}"
    const val BADGE = "badge/{id}"
    const val DISCOVER_ENTITY = "discover/entity/{id}"
}

internal const val PROFILE_ROUTE = Routes.PROFILE
internal const val SETTINGS_ROUTE = Routes.SETTINGS
internal fun selectedTab(route: String): String = when {
    route == SETTINGS_ROUTE || route == PROFILE_ROUTE -> PROFILE_ROUTE
    route == Routes.COSMETICS -> PROFILE_ROUTE
    route == Routes.DISCOVER || route.startsWith("discover/") -> Routes.REPERTOIRE
    route.startsWith("catalog") || route.startsWith("piece/") || route.startsWith("recordings/") || route.startsWith("previous") -> Routes.REPERTOIRE
    route.startsWith("badge/") || route == Routes.BADGES -> PROFILE_ROUTE
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
