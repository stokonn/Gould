package com.opus.piano.preferences

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Stage 7A: persisted "Include online catalogue results" preference, default OFF.
 *  Online catalogue search stays explicitly user-initiated in Discover; opening or
 *  restoring a screen never triggers a network request. Kept in SharedPreferences
 *  (not the Room database) so backup/restore and migrations are untouched. */
class CatalogPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("goldberg-catalogue", Context.MODE_PRIVATE)
    private val _enabled = MutableStateFlow(prefs.getBoolean(KEY_ONLINE_CATALOGUE, false))
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()
    fun setEnabled(value: Boolean) {
        prefs.edit().putBoolean(KEY_ONLINE_CATALOGUE, value).apply()
        _enabled.value = value
    }
    private companion object { const val KEY_ONLINE_CATALOGUE = "online-catalogue" }
}
