package com.opus.piano.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.opus.piano.R
import com.opus.piano.admin.AdminFailure
import com.opus.piano.admin.AdminMarkerKind
import com.opus.piano.admin.AdminQueueItem
import com.opus.piano.admin.AdminResult
import com.opus.piano.admin.AdminStatus
import com.opus.piano.i18n.L
import com.opus.piano.ui.EditorialHeader
import com.opus.piano.ui.JournalSpace

/**
 * Shared Stage 7B semantic status palette (docs/design/stage-7b: limited accent
 * colors only for meaningful semantic statuses). Single source of truth so status
 * color + text are always presented together (never color alone).
 */
internal object AdminPalette {
    val PendingAmber = Color(0xFFC9A227) // pending / awaiting first decision
    val InfoBlue = Color(0xFF6FA8DC)      // needs info (changes_requested)
    val ApproveGreen = Color(0xFF57B894)  // approved/published
    val RejectRed = Color(0xFFD46A5F)     // rejected
    val Warning = Color(0xFFD9A441)       // changed revision / conflict
}

/**
 * Stage 7B.4 surface — moderation queue (server-backed list over the verified
 * `GET /v1/admin/queue` walk). First pass lands with 7B.3 so the dashboard deep
 * links resolve to a real, honest screen; search/hydration/update-pill requirements
 * continue in 7B.4.
 *
 * Rows show ONLY contract fields (id/revision/status/evidence) — queue rows carry no
 * title/composer (gap G2), so nothing display-only is fabricated here.
 */
@Composable internal fun AdminQueueScreen(
    model: AdminViewModel,
    initialFilter: String?,
    back: () -> Unit,
) {
    val repo = model.repository
    var activeFilter by rememberSaveable(initialFilter) { mutableStateOf(normalizeFilter(initialFilter)) }
    var items by remember { mutableStateOf<List<AdminQueueItem>>(emptyList()) }
    var nextCursor by remember { mutableStateOf<String?>(null) }
    var failure by remember { mutableStateOf<AdminFailure?>(null) }
    var loading by remember { mutableStateOf(true) }
    var refresh by remember { mutableStateOf(0) }
    var append by remember { mutableStateOf(0) }
    val markedDeferred = repo.markers.all(AdminMarkerKind.DEFERRED)
    val markedReReview = repo.markers.all(AdminMarkerKind.RE_REVIEW)

    suspend fun load(reset: Boolean) {
        loading = true
        failure = null
        val cursor = if (reset) null else nextCursor
        if (reset) items = emptyList()
        when (val page = repo.api.queue(cursor, status = null)) {
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

    val visible = items.filter { item ->
        when (activeFilter) {
            "pending" -> item.status == AdminStatus.SUBMITTED || item.status == AdminStatus.IN_REVIEW
            "needs_info" -> item.status == AdminStatus.CHANGES_REQUESTED
            else -> true
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = JournalSpace.page, vertical = JournalSpace.small)) {
        EditorialHeader(title = L.s(R.string.stage7b_moderation_queue), back = back)
        Row(horizontalArrangement = Arrangement.spacedBy(JournalSpace.tiny)) {
            FilterChip(activeFilter == "all", { activeFilter = "all" }, { Text(L.fixed("All")) })
            FilterChip(activeFilter == "pending", { activeFilter = "pending" }, { Text(L.s(R.string.stage7b_pending)) })
            FilterChip(
                activeFilter == "needs_info",
                { activeFilter = "needs_info" },
                { Text(L.s(R.string.stage7b_needs_info)) },
            )
        }
        Spacer(Modifier.height(JournalSpace.small))
        when {
            loading && items.isEmpty() -> AdminLoadingRow(L.s(R.string.stage7b_loading))
            failure != null && items.isEmpty() -> AdminFailureMessage(failure!!) { refresh++ }
            visible.isEmpty() -> Text(
                L.s(R.string.stage7b_queue_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            else -> LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(visible, key = { it.id }) { item ->
                    QueueRow(
                        item = item,
                        deferred = item.id in markedDeferred,
                        reReview = item.id in markedReReview,
                    )
                }
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

private fun normalizeFilter(raw: String?): String =
    when (raw) { "pending" -> "pending"; "needs_info" -> "needs_info"; else -> "all" }

@Composable
private fun FilterChip(selected: Boolean, onClick: () -> Unit, label: @Composable () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = .15f) else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = .6f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = .45f),
        ),
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) { label() }
    }
}

@Composable
private fun QueueRow(item: AdminQueueItem, deferred: Boolean, reReview: Boolean) {
    val statusLabel = when (item.status) {
        AdminStatus.SUBMITTED -> L.s(R.string.stage7b_status_submitted)
        AdminStatus.IN_REVIEW -> L.s(R.string.stage7b_status_in_review)
        AdminStatus.CHANGES_REQUESTED -> L.s(R.string.stage7b_needs_info)
        AdminStatus.APPROVED -> L.s(R.string.stage7b_status_submitted).let { L.fixed("Approved") }
        AdminStatus.PUBLISHED -> L.fixed("Published")
        AdminStatus.REJECTED -> L.fixed("Rejected")
        AdminStatus.WITHDRAWN -> L.fixed("Withdrawn")
    }
    val statusColor = when (item.status) {
        AdminStatus.SUBMITTED, AdminStatus.IN_REVIEW -> AdminPalette.PendingAmber
        AdminStatus.CHANGES_REQUESTED -> AdminPalette.InfoBlue
        AdminStatus.APPROVED, AdminStatus.PUBLISHED -> AdminPalette.ApproveGreen
        AdminStatus.REJECTED -> AdminPalette.RejectRed
        AdminStatus.WITHDRAWN -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val cd = "$statusLabel ${item.id} ${L.s(R.string.stage7b_revision, item.revision)}"
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .45f)),
        modifier = Modifier.semantics { contentDescription = cd },
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(item.id, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        L.s(R.string.stage7b_revision, item.revision),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
                Text(statusLabel, style = MaterialTheme.typography.labelMedium, color = statusColor, fontWeight = FontWeight.Medium)
            }
            if (deferred || reReview) {
                Icon(
                    Icons.Outlined.Bookmark, null,
                    Modifier.size(16.dp),
                    tint = if (reReview) AdminPalette.Warning else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                Icons.Outlined.ChevronRight, null,
                Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
