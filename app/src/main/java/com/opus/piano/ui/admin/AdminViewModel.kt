package com.opus.piano.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.opus.piano.OpusApplication
import com.opus.piano.admin.AdminApi
import com.opus.piano.admin.AdminCapabilities
import com.opus.piano.admin.AdminFailure
import com.opus.piano.admin.AdminMarkerKind
import com.opus.piano.admin.AdminRepository
import com.opus.piano.admin.AdminResult
import com.opus.piano.admin.AdminStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Administration dashboard state (Stage 7B.3).
 *
 * Loading model: ONE bounded refresh per explicit action (initial entry + manual
 * refresh) — verify role (1 request) + stats walk (≤16 requests) — never polling.
 * With the default empty endpoint configuration this performs ZERO network calls
 * (AdminApi fails fast with NotConfigured) and surfaces honest unavailable states.
 */
class AdminViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as OpusApplication
    private val admin: AdminRepository = app.adminRepository
    private val settings = app.adminSettings

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    /** Last successfully verified capabilities, or null while unverified. */
    private val _caps = MutableStateFlow<AdminCapabilities?>(null)
    val caps: StateFlow<AdminCapabilities?> = _caps

    /** Failure of the most recent role verification (null when never failed). */
    private val _capsFailure = MutableStateFlow<AdminFailure?>(null)
    val capsFailure: StateFlow<AdminFailure?> = _capsFailure

    private val _stats = MutableStateFlow<AdminStats?>(null)
    val stats: StateFlow<AdminStats?> = _stats

    /** Account-scoped Resume #ID for the queue row's second line (null = no marker). */
    private val _resumeId = MutableStateFlow<String?>(null)
    val resumeId: StateFlow<String?> = _resumeId

    /** Result of the last sign-out attempt: Ok(true)=server revoked, Err=server unreachable (local always cleared). */
    private val _signOutResult = MutableStateFlow<AdminResult<Boolean>?>(null)
    val signOutResult: StateFlow<AdminResult<Boolean>?> = _signOutResult

    /** Currently configured endpoint (may be blank). */
    fun currentEndpoint(): String = settings.endpoint

    /** Persist the admin base URL (empty = honest not-configured default). */
    fun saveEndpoint(raw: String): Boolean {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) {
            // Explicitly clearing the endpoint returns the app to the honest
            // "not configured" state (the shipped default).
            settings.endpoint = ""
            refresh()
            return true
        }
        if (AdminApi.validateEndpoint(trimmed) == null) return false
        settings.endpoint = trimmed
        refresh()
        return true
    }

    /** Contract sign-out; local credential/trust/markers are always cleared (see repository). */
    fun signOut() {
        viewModelScope.launch { _signOutResult.value = admin.signOut() }
    }

    init {
        // Entry gating needs a fresh role check. Costs 0 requests while the endpoint
        // is unconfigured (the default); at most 1 capabilities request per process
        // otherwise. Server always enforces — this is convenience only.
        refresh()
    }

    /**
     * Profile → Administration entry visibility (convenience, never authorization):
     *  - verified WITH an admin capability → visible;
     *  - verified WITHOUT any admin capability → hidden;
     *  - explicit 403 from the server → hidden;
     *  - unverified / not configured / unreachable / 401 → visible, so the dashboard
     *    and Admin Settings remain reachable and can show the honest state.
     */
    fun entryVisible(caps: AdminCapabilities?, failure: AdminFailure?): Boolean = when {
        caps != null -> admin.hasAdminSurface(caps)
        failure is AdminFailure.Forbidden -> false
        else -> true
    }

    /** Single bounded refresh: role verification + exact-or-unavailable stats. */
    fun refresh() {
        if (_loading.value) return
        _loading.value = true
        viewModelScope.launch {
            try {
                when (val role = admin.verifyRole()) {
                    is AdminResult.Ok -> { _caps.value = role.value; _capsFailure.value = null }
                    is AdminResult.Err -> { _capsFailure.value = role.failure; /* keep last good caps only within trust */ if (!admin.trust.isTrusted) _caps.value = null }
                }
                _stats.value = admin.stats(System.currentTimeMillis())
                _resumeId.value = resumeMarker()
            } finally {
                _loading.value = false
            }
        }
    }

    /** Deterministic resume candidate: newest-kind first (re-review), then deferred. */
    private fun resumeMarker(): String? =
        admin.markers.all(AdminMarkerKind.RE_REVIEW).sorted().firstOrNull()
            ?: admin.markers.all(AdminMarkerKind.DEFERRED).sorted().firstOrNull()

    val repository: AdminRepository get() = admin
}
