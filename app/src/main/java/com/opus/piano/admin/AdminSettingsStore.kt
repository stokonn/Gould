package com.opus.piano.admin

import android.content.Context
import android.content.SharedPreferences

/**
 * Admin settings storage (Stage 7B.3 foundation; extended by 7B.8 Admin Settings).
 *
 * `endpoint` is the admin API base URL. It is EMPTY by default — Goldberg ships with
 * no deployed Stage 6 backend (CONTRACT-AUDIT.md), so the app must present an honest
 * "not configured" state instead of guessing a host. Excluded from backup by the
 * app-wide `allowBackup="false"` rules.
 */
class AdminSettingsStore(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    /** Configured base URL, blank until the operator/administrator sets one. */
    var endpoint: String
        get() = prefs.getString(KEY_ENDPOINT, "").orEmpty()
        set(value) { prefs.edit().putString(KEY_ENDPOINT, value.trim()).apply() }

    companion object {
        private const val FILE_NAME = "goldberg-admin-settings"
        private const val KEY_ENDPOINT = "endpoint"
    }
}

/** Android persistence for account-scoped Deferred/Re-review markers (ids only). */
class AndroidAdminMarkerStore(context: Context) : AdminMarkerStore {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    override fun load(account: String, kind: AdminMarkerKind): Set<String> =
        prefs.getStringSet(key(account, kind), null)?.toSet() ?: emptySet()

    override fun save(account: String, kind: AdminMarkerKind, ids: Set<String>) {
        prefs.edit().putStringSet(key(account, kind), ids.toSet()).apply()
    }

    private fun key(account: String, kind: AdminMarkerKind): String = "${kind.name}:$account"

    companion object {
        private const val FILE_NAME = "goldberg-admin-markers"
    }
}
