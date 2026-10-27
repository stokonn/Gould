package com.opus.piano.preferences

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Persisted, default-off consent for automatic debounced Discover provider search.
 * Query text stays transient. Preference keys/backup compatibility are unchanged. */
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
