package com.opus.piano.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.opus.piano.R
import com.opus.piano.admin.AdminAuditEvent
import com.opus.piano.admin.AdminFailure
import com.opus.piano.admin.AdminResult
import com.opus.piano.i18n.L
import com.opus.piano.ui.EditorialHeader
import com.opus.piano.ui.JournalSpace
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Stage 7B.7 surface — role-scoped audit log over the verified
 * `GET /v1/admin/audit` walk (append-only; the client NEVER mutates records).
 * First pass lands with 7B.3 so the dashboard deep link resolves to a real,
 * honest screen; client-side search/filter over fetched pages continues in 7B.7
 * (server-side search remains gap G3).
 */
@Composable internal fun AdminAuditScreen(model: AdminViewModel, back: () -> Unit) {
    val repo = model.repository
    var items by remember { mutableStateOf<List<AdminAuditEvent>>(emptyList()) }
    var nextCursor by remember { mutableStateOf<String?>(null) }
    var failure by remember { mutableStateOf<AdminFailure?>(null) }
    var loading by remember { mutableStateOf(true) }
    var refresh by remember { mutableStateOf(0) }
    var append by remember { mutableStateOf(0) }

    suspend fun load(reset: Boolean) {
        loading = true
        failure = null
        val cursor = if (reset) null else nextCursor
        if (reset) items = emptyList()
        when (val page = repo.api.audit(cursor)) {
            is AdminResult.Err -> failure = page.failure
            is AdminResult.Ok -> {
                items = items + page.value.items
                nextCursor = page.value.nextCursor
            }
        }
        loading = false
    }

    LaunchedEffect(refresh) { load(reset = true) }
    LaunchedEffect(append) { if (append > 0) load(reset = false) }

    Column(Modifier.fillMaxSize().padding(horizontal = JournalSpace.page, vertical = JournalSpace.small)) {
        EditorialHeader(title = L.s(R.string.stage7b_audit_log), back = back)
        Text(
            L.s(R.string.stage7b_audit_empty),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(JournalSpace.small))
        when {
            loading && items.isEmpty() -> AdminLoadingRow(L.s(R.string.stage7b_loading))
            failure != null && items.isEmpty() -> AdminFailureMessage(failure!!) { refresh++ }
            items.isEmpty() -> Unit // empty note already shown above
            else -> LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(items, key = { it.seq }) { event -> AuditRow(event) }
                if (nextCursor != null) {
                    item {
                        TextButton(onClick = { append++ }, enabled = !loading, modifier = Modifier.fillMaxWidth()) {
                            Text(L.s(R.string.stage7b_load_more))
                        }
                    }
                }
            }
        }
        if (failure != null && items.isNotEmpty()) AdminFailureMessage(failure!!, onRetry = { append++ })
    }
}

@Composable
private fun AuditRow(event: AdminAuditEvent) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = .45f),
        ),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "#${event.seq} · ${event.action}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "${event.subject} · ${event.actor} · ${formatAuditTime(event.ts)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** Application-locale audit timestamp. */
private fun formatAuditTime(millis: Long): String = try {
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", L.locale)
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(millis))
} catch (_: Exception) {
    ""
}
