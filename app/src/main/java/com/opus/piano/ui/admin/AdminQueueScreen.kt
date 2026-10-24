package com.opus.piano.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.opus.piano.R
import com.opus.piano.admin.*
import com.opus.piano.i18n.L
import com.opus.piano.ui.EditorialHeader
import com.opus.piano.ui.JournalSpace
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal object AdminPalette {
    val PendingAmber = Color(0xFFC9A227) // pending / awaiting first decision
    val InfoBlue = Color(0xFF6FA8DC)      // needs info (changes_requested)
    val ApproveGreen = Color(0xFF57B894)  // approved/published
    val RejectRed = Color(0xFFD46A5F)     // rejected
    val Warning = Color(0xFFD9A441)       // changed revision / conflict
}


@OptIn(ExperimentalLayoutApi::class)
@Composable internal fun AdminQueueScreen(
    model: AdminViewModel, initialFilter: String?, back: () -> Unit, open: (String) -> Unit,
) {
    val service = model.queue
    val state by service.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var filter by rememberSaveable { mutableStateOf(if (initialFilter != null) AdminQueueService.normalizeFilter(initialFilter) else model.queueFilter) }
    var query by rememberSaveable { mutableStateOf(model.queueQuery) }
    var expanded by rememberSaveable { mutableStateOf(false) }
    var authorized by remember { mutableStateOf(false) }
    var roleFailure by remember { mutableStateOf<AdminFailure?>(null) }
    val list = rememberLazyListState(model.queueScrollIndex, model.queueScrollOffset)
    suspend fun enter() {
        authorized = false
        when (val role = model.repository.verifyRole()) {
            is AdminResult.Err -> roleFailure = role.failure
            is AdminResult.Ok -> {
                authorized = role.value.canQueueRead
                roleFailure = if (authorized) null else AdminFailure.Forbidden
                if (authorized) { service.enter(); service.ensureMarked() }
            }
        }
    }
    AdminOnResume(onResume = { enter() }, onPause = { authorized = false })
    LaunchedEffect(query, authorized) {
        model.queueQuery = query
        service.clearSearch()
        if (authorized && query.isNotBlank()) { delay(350); service.search(query) }
    }
    DisposableEffect(list) {
        onDispose { model.queueScrollIndex = list.firstVisibleItemIndex; model.queueScrollOffset = list.firstVisibleItemScrollOffset }
    }
    LaunchedEffect(filter) { model.queueFilter = filter }
    Column(Modifier.fillMaxSize().padding(horizontal = JournalSpace.page, vertical = JournalSpace.small)) {
        EditorialHeader(title = L.s(R.string.stage7b_moderation_queue), back = back)
        if (roleFailure != null) {
            AdminFailureMessage(roleFailure!!) { scope.launch { enter() } }
        } else if (!authorized) AdminLoadingRow(L.s(R.string.stage7b_loading))
        if (authorized) {
            OutlinedTextField(query, { query = it }, label = { Text(L.s(R.string.stage7b_search)) },
                singleLine = true, modifier = Modifier.fillMaxWidth())
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("all", "pending", "needs_info", "re_review", "deferred").forEach { key ->
                    FilterChip(filter == key, { filter = key }, { Text(queueFilterLabel(key)) },
                        modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = queueFilterLabel(key) })
                }
            }
            val results = when (val search = state.search) {
                is AdminSearchState.Results -> service.filterFor(search.rows, filter)
                AdminSearchState.Idle -> if (query.isBlank()) service.visibleRows(filter) else emptyList()
                else -> emptyList()
            }
            LazyColumn(Modifier.weight(1f), state = list, verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)) {
                if (query.isBlank()) item {
                    TextButton({ expanded = !expanded }) { Text(L.s(R.string.stage7b_deferred_section)) }
                    if (expanded) service.visibleRows("deferred").forEach { row ->
                        QueueRow(row, open,
                            model.repository.markers.isMarked(AdminMarkerKind.DEFERRED, row.id),
                            model.repository.markers.isMarked(AdminMarkerKind.RE_REVIEW, row.id))
                    }
                }
                item {
                    when (val search = state.search) {
                        is AdminSearchState.Running -> AdminLoadingRow(L.s(R.string.stage7b_loading))
                        is AdminSearchState.Failed -> AdminFailureMessage(search.failure)
                        AdminSearchState.RequiresServerSearch -> Text(L.s(R.string.stage7b_server_search_required))
                        AdminSearchState.IdNotFound, AdminSearchState.NoMatch -> Text(L.s(R.string.stage7b_no_match))
                        else -> if (results.isEmpty() && !state.loading) Text(L.s(R.string.stage7b_queue_empty))
                    }
                    if (state.loading) AdminLoadingRow(L.s(R.string.stage7b_loading))
                    state.failure?.let { AdminFailureMessage(it) { scope.launch { service.refresh() } } }
                }
                items(results, key = { it.id }) { row -> QueueRow(row, open,
                            model.repository.markers.isMarked(AdminMarkerKind.DEFERRED, row.id),
                            model.repository.markers.isMarked(AdminMarkerKind.RE_REVIEW, row.id)) }
                if (query.isBlank() && state.nextCursor != null) item {
                    if (state.pagesLoaded < SearchPolicy.MAX_QUEUE_PAGES) {
                        TextButton({ scope.launch { service.loadMore() } }, enabled = !state.loading) { Text(L.s(R.string.stage7b_load_more)) }
                    } else Text(L.s(R.string.stage7b_queue_bound))
                }
            }
            // Separate bottom slot gives the floating pill its own safe area; never covers rows.
            if (state.pendingUpdates > 0) {
                FilledTonalButton({ scope.launch { service.refresh(); if (query.isNotBlank()) service.search(query) } }, enabled = !state.loading,
                    modifier = Modifier.fillMaxWidth()) { Text(L.s(R.string.stage7b_updates_available)) }
            }
            TextButton({ scope.launch { service.refresh(); service.ensureMarked(); if (query.isNotBlank()) service.search(query) } }, enabled = !state.loading) {
                Text(L.s(R.string.stage7b_refresh))
            }
        }
    }
}

internal fun queueFilterLabel(key: String): String = L.s(when (key) {
    "pending" -> R.string.stage7b_pending
    "needs_info" -> R.string.stage7b_needs_info
    "deferred" -> R.string.stage7b_deferred
    "re_review" -> R.string.stage7b_re_review
    else -> R.string.stage7b_all
})

@Composable private fun QueueRow(row: AdminQueueRow, open: (String) -> Unit, deferred: Boolean, reReview: Boolean) {
    OutlinedCard(onClick = { open(row.id) }, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(row.display?.title ?: row.id, style = MaterialTheme.typography.titleSmall)
            row.display?.composer?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            Text("#${row.id} · ${row.revision?.let { L.s(R.string.stage7b_revision, it) } ?: L.s(R.string.stage7b_unavailable)}",
                style = MaterialTheme.typography.bodySmall)
            Text(statusLabel(row.status), style = MaterialTheme.typography.labelMedium)
            if (deferred) Text(L.s(R.string.stage7b_deferred), style = MaterialTheme.typography.labelSmall)
            if (reReview) Text(L.s(R.string.stage7b_re_review), style = MaterialTheme.typography.labelSmall, color = AdminPalette.Warning)
        }
    }
}

internal fun statusLabel(status: AdminStatus?): String = L.s(when (status) {
    AdminStatus.SUBMITTED -> R.string.stage7b_status_submitted
    AdminStatus.IN_REVIEW -> R.string.stage7b_status_in_review
    AdminStatus.CHANGES_REQUESTED -> R.string.stage7b_needs_info
    AdminStatus.APPROVED -> R.string.stage7b_approved
    AdminStatus.PUBLISHED -> R.string.stage7b_published
    AdminStatus.REJECTED -> R.string.stage7b_rejected
    AdminStatus.WITHDRAWN -> R.string.stage7b_withdrawn
    else -> R.string.stage7b_unavailable
})
