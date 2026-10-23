package com.opus.piano.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.opus.piano.R
import com.opus.piano.admin.AdminFailure
import com.opus.piano.i18n.L
import kotlinx.coroutines.launch

/** Localized explanation for an honest unavailable/error state (shared by all admin screens). */
internal fun adminReasonText(reasonKey: String): String = when {
    reasonKey == "contract_unavailable" -> L.s(R.string.stage7b_contract_unavailable)
    reasonKey == "not_configured" -> L.s(R.string.stage7b_reason_not_configured)
    reasonKey == "unreachable" -> L.s(R.string.stage7b_reason_unreachable)
    reasonKey == "unauthorized" -> L.s(R.string.stage7b_reason_unauthorized)
    reasonKey == "forbidden" -> L.s(R.string.stage7b_reason_forbidden)
    reasonKey == "rate_limited" -> L.s(R.string.stage7b_reason_rate_limited)
    reasonKey.startsWith("server_error") -> L.s(R.string.stage7b_reason_server)
    reasonKey == "invalid_payload" -> L.s(R.string.stage7b_reason_invalid)
    reasonKey == "stats_budget_exceeded" -> L.s(R.string.stage7b_reason_budget)
    else -> L.s(R.string.stage7b_reason_other)
}

/** Honest failure block: reason text + retry, never a fabricated value. */
@Composable
internal fun AdminFailureMessage(failure: AdminFailure, onRetry: (() -> Unit)? = null) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = JournalAdminSpace),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            adminReasonText(failure.key),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (onRetry != null) {
            TextButton(onClick = onRetry) { Text(L.s(R.string.stage7b_retry)) }
        }
    }
}

/** Small indeterminate progress row while a bounded request walk runs. */
@Composable
internal fun AdminLoadingRow(label: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = JournalAdminSpace),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
        Spacer(Modifier.width(10.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private val JournalAdminSpace = 12.dp

/** Revalidate when returning from background; cancel reads and lock confirmations
 * as soon as the screen loses foreground status. No polling or background reads. */
@Composable
internal fun AdminOnResume(onResume: suspend () -> Unit, onPause: () -> Unit = {}) {
    val owner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val resume = androidx.compose.runtime.rememberUpdatedState(onResume)
    val pause = androidx.compose.runtime.rememberUpdatedState(onPause)
    androidx.compose.runtime.DisposableEffect(owner) {
        var job: kotlinx.coroutines.Job? = null
        fun start() { job?.cancel(); job = scope.launch { resume.value() } }
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_RESUME -> start()
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> { job?.cancel(); pause.value() }
                else -> Unit
            }
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer); job?.cancel(); pause.value() }
    }
}
