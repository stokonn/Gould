package com.opus.piano.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.opus.piano.R
import com.opus.piano.admin.*
import com.opus.piano.i18n.L
import com.opus.piano.ui.EditorialHeader
import com.opus.piano.ui.JournalSpace
import kotlinx.coroutines.launch
import org.json.JSONObject

@OptIn(ExperimentalLayoutApi::class)
@Composable internal fun AdminProposalScreen(model: AdminViewModel, id: String, back: () -> Unit, next: (String) -> Unit) {
    val service = model.repository.review
    val state by service.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var tab by rememberSaveable(id) { mutableStateOf(0) }
    var acknowledged by remember(id, state.detail?.revision, state.conflicts) { mutableStateOf(false) }
    var autoNext by rememberSaveable { mutableStateOf(false) }
    var deferred by remember(id) { mutableStateOf(model.repository.markers.isMarked(AdminMarkerKind.DEFERRED,id)) }
    AdminOnResume(onResume = { service.validate(id) }, onPause = { service.lock() })
    LaunchedEffect(state.success) {
        if (state.success != null) {
            model.refresh()
            model.queue.refresh()
            if (autoNext) model.queue.state.value.rows.firstOrNull { it.id != id && it.status?.decidable == true }?.let { next(it.id) }
        }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = JournalSpace.page, vertical = JournalSpace.small),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        EditorialHeader(title = L.s(R.string.stage7b_review_proposal), back = back)
        Text("#$id", style = MaterialTheme.typography.titleSmall)
        if (state.loading) AdminLoadingRow(L.s(R.string.stage7b_validating))
        state.failure?.let { AdminFailureMessage(it) { scope.launch { service.validate(id) } } }
        if (state.pending != null) {
            Text(L.s(R.string.stage7b_uncertain))
            TextButton({ scope.launch { service.retryPending() } }, enabled = !state.loading) { Text(L.s(R.string.stage7b_reconcile)) }
        }
        if (state.success != null) Text(L.s(R.string.stage7b_decision_confirmed), color = AdminPalette.ApproveGreen)
        val detail = state.detail?.takeIf { it.id == id }
        if (detail != null) {
            Text("${L.s(R.string.stage7b_revision,detail.revision)} · ${statusLabel(detail.status)}")
            Text(if (state.verified) L.s(R.string.stage7b_verified) else L.s(R.string.stage7b_unverified))
            if (state.reReview) {
                OutlinedCard {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(L.s(R.string.stage7b_re_review_required), color = AdminPalette.Warning)
                        Text(L.s(R.string.stage7b_re_review_help))
                        // Revision history is complete only when every intermediate snapshot is supplied.
                        val start = state.previous?.revision ?: 1
                        val supplied = detail.revisions.map { it.revision }.toSet()
                        if ((start..detail.revision).any { it !in supplied }) Text(L.s(R.string.stage7b_history_incomplete))
                        val prior = state.previous?.latest?.payload
                        if (prior != null) {
                            Text(L.s(R.string.stage7b_previous_proposal))
                            val current = detail.latest?.payload
                            if (current != null) proposalFieldChanges(prior, current).forEach { change ->
                                OutlinedCard(Modifier.fillMaxWidth()) {
                                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("${change.name} · ${L.s(R.string.stage7b_updated)}", color = AdminPalette.Warning)
                                        change.before?.opt("newValue")?.let {
                                            Text(it.toString(), textDecoration = TextDecoration.LineThrough)
                                        }
                                        Text(L.s(R.string.stage7b_proposed_value), style = MaterialTheme.typography.labelMedium)
                                        Text(change.after?.opt("newValue")?.toString() ?: L.s(R.string.stage7b_removed), color = AdminPalette.ApproveGreen)
                                        if (change.evidenceChanged) {
                                            Text(L.s(R.string.stage7b_evidence_updated), color = AdminPalette.Warning)
                                            change.after?.opt("evidence")?.let { PayloadValues(it, 0) }
                                        }
                                    }
                                }
                            }
                        }
                        Row {
                            Checkbox(acknowledged, { acknowledged = it }, enabled = state.verified && !state.loading,
                                modifier = Modifier.semantics { contentDescription = L.s(R.string.stage7b_ack_revision, detail.revision) })
                            Text(L.s(R.string.stage7b_ack_revision, detail.revision), Modifier.padding(top = 12.dp))
                        }
                        Button({ scope.launch { service.acknowledge(detail.revision) } }, enabled = acknowledged && state.verified && !state.loading) {
                            Text(L.s(R.string.stage7b_complete_re_review))
                        }
                    }
                }
            }
            if (state.conflicts >= 3) Text(L.s(R.string.stage7b_stability_advisory))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(R.string.stage7b_comparison,R.string.stage7b_details,R.string.stage7b_history,R.string.stage7b_evidence).forEachIndexed { index,label ->
                    FilterChip(tab == index,{ tab = index },{ Text(L.s(label)) },
                        modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = L.s(label) })
                }
            }
            when (tab) {
                0 -> {
                    Text(L.s(R.string.stage7b_catalogue_before_missing))
                    val payload = detail.latest?.payload
                    val fields = payload?.optJSONArray("fields")
                    if (fields != null) for (i in 0 until fields.length()) {
                        val field = fields.optJSONObject(i) ?: continue
                        OutlinedCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Text(field.optString("field"), style = MaterialTheme.typography.labelLarge)
                                Text(L.s(R.string.stage7b_proposed_value))
                                Text(field.opt("newValue")?.toString() ?: L.s(R.string.stage7b_unavailable))
                            }
                        }
                    }
                }
                1 -> detail.latest?.payload?.let { PayloadCard(it) }
                2 -> {
                    detail.revisions.sortedBy { it.revision }.forEach { rev ->
                        Text("${L.s(R.string.stage7b_revision,rev.revision)} · ${formatAdminTime(rev.createdAt)}")
                        PayloadCard(rev.payload)
                    }
                    detail.reviewEvents.sortedBy { it.createdAt }.forEach { event ->
                        Text("${event.decision} · ${L.s(R.string.stage7b_revision,event.revision)} · ${formatAdminTime(event.createdAt)}")
                        Text(event.reason)
                    }
                }
                3 -> {
                    // Full server snapshot retains nested evidence values, URLs, licenses and notes.
                    Text(L.s(R.string.stage7b_evidence_help))
                    detail.latest?.payload?.let { PayloadCard(it) }
                }
            }
            TextButton({ deferred = !deferred; if (deferred) service.defer() else service.removeDeferred() }) {
                Text(L.s(if (deferred) R.string.stage7b_remove_deferred else R.string.stage7b_defer))
            }
            Row {
                Checkbox(autoNext, { autoNext = it }, modifier = Modifier.semantics { contentDescription = L.s(R.string.stage7b_auto_next) })
                Text(L.s(R.string.stage7b_auto_next), Modifier.padding(top = 12.dp))
            }
            if (!model.repository.decisionsEnabled) Text(L.s(R.string.stage7b_contract_unavailable))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AdminDecision.entries.forEach { decision ->
                    OutlinedButton({ scope.launch { service.prepare(decision) } },
                        enabled = model.repository.decisionsEnabled && state.verified && !state.loading && !state.reReview && detail.status.decidable && state.pending == null && state.success == null) {
                        Text(decisionLabel(decision))
                    }
                }
            }
        }
    }
    state.confirmation?.let { confirmation ->
        var reason by remember(confirmation) { mutableStateOf("") }
        AlertDialog(onDismissRequest = { service.cancelConfirmation() },
            title = { Text(decisionLabel(confirmation.decision)) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("#${confirmation.id} · ${L.s(R.string.stage7b_revision,confirmation.revision)} · ${statusLabel(confirmation.status)}")
                    Text(L.s(R.string.stage7b_confirm_review))
                    if (confirmation.decision != AdminDecision.APPROVE) OutlinedTextField(reason,{ if(it.length<=2000) reason=it },label={Text(L.s(R.string.stage7b_reason_required))})
                }
            },
            confirmButton = {
                TextButton({ scope.launch { service.confirm(reason) } }, enabled = !state.loading && (confirmation.decision == AdminDecision.APPROVE || reason.isNotBlank())) {
                    Text(decisionLabel(confirmation.decision))
                }
            }, dismissButton = { TextButton({ service.cancelConfirmation() }) { Text(L.s(R.string.stage7b_cancel)) } })
    }
}

@Composable private fun PayloadCard(payload: JSONObject) {
    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            PayloadValues(payload, 0)
        }
    }
}
@Composable private fun PayloadValues(value: Any?, depth: Int) {
    // Payload schema is bounded server-side. Nested values are always rendered as
    // plain text, including source URLs/licenses; no HTML or executable links.
    when (value) {
        is JSONObject -> value.keys().asSequence().toList().forEach { key ->
            Text(key.replace('_', ' '), style = MaterialTheme.typography.labelMedium)
            PayloadValues(value.opt(key), depth + 1)
        }
        is org.json.JSONArray -> for (i in 0 until value.length()) {
            PayloadValues(value.opt(i), depth + 1)
            if (i < value.length()-1) HorizontalDivider()
        }
        else -> Text(if (value == null || value == JSONObject.NULL) L.s(R.string.stage7b_unavailable) else value.toString(),
            style = MaterialTheme.typography.bodyMedium)
    }
}
internal fun decisionLabel(decision: AdminDecision) = L.s(when(decision) {
    AdminDecision.APPROVE -> R.string.stage7b_approve
    AdminDecision.REJECT -> R.string.stage7b_reject
    AdminDecision.REQUEST_CHANGES -> R.string.stage7b_request_changes
})
internal fun formatAdminTime(millis: Long): String = if(millis <= 0) L.s(R.string.stage7b_unavailable) else
    java.time.format.DateTimeFormatter.ofLocalizedDateTime(java.time.format.FormatStyle.SHORT).withLocale(L.locale)
        .withZone(java.time.ZoneId.systemDefault()).format(java.time.Instant.ofEpochMilli(millis))
