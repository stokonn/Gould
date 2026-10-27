package com.opus.piano.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.opus.piano.R
import com.opus.piano.admin.AdminResult
import com.opus.piano.i18n.L
import com.opus.piano.ui.EditorialHeader
import com.opus.piano.ui.JournalSpace

/**
 * Stage 7B.8 surface — compact Admin Settings: endpoint configuration (empty by
 * default — no Stage 6 backend is deployed), honest role/session state and contract
 * sign-out. NO Android-side role management and NO destructive database controls
 * (docs/design/stage-7b §09). Notification/sync toggles land with 7B.7/7B.8.
 */
@Composable internal fun AdminSettingsScreen(model: AdminViewModel, back: () -> Unit) {
    val caps by model.caps.collectAsStateWithLifecycle()
    val capsFailure by model.capsFailure.collectAsStateWithLifecycle()
    val signOutResult by model.signOutResult.collectAsStateWithLifecycle()
    val endpoint = model.currentEndpoint()
    var editing by remember { mutableStateOf(false) }
    val policy by model.syncPolicy.collectAsStateWithLifecycle()
    val syncFailure by model.syncFailure.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var mobileDialog by remember { mutableStateOf(false) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        model.setSyncPolicy(policy.copy(notifications = granted))
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = JournalSpace.page, vertical = JournalSpace.small)) {
        EditorialHeader(title = L.s(R.string.stage7b_admin_settings), back = back)

        Text(L.s(R.string.stage7b_wifi_default), style = MaterialTheme.typography.bodySmall)
        AdminSwitch(L.s(R.string.stage7b_notifications), policy.notifications) { enabled ->
            if (enabled && android.os.Build.VERSION.SDK_INT >= 33 &&
                androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                permission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            } else model.setSyncPolicy(policy.copy(notifications = enabled))
        }
        AdminSwitch(L.s(R.string.stage7b_sync), policy.background) { model.setSyncPolicy(policy.copy(background = it)) }
        AdminSwitch(L.s(R.string.stage7b_mobile_consent), policy.mobileConsent) {
            if (it) mobileDialog = true else model.setSyncPolicy(policy.copy(mobileConsent = false))
        }
        syncFailure?.let { AdminFailureMessage(it) }
        if (mobileDialog) AlertDialog(onDismissRequest = { mobileDialog = false },
            text = { Text(L.s(R.string.stage7b_mobile_confirm)) },
            confirmButton = { TextButton({ model.setSyncPolicy(policy.copy(mobileConsent = true)); mobileDialog = false }) { Text(L.s(R.string.stage7b_save)) } },
            dismissButton = { TextButton({ mobileDialog = false }) { Text(L.s(R.string.stage7b_cancel)) } })
        SettingsCard(
            icon = Icons.Outlined.Public,
            title = L.s(R.string.stage7b_endpoint),
            value = endpoint.ifBlank { L.s(R.string.stage7b_endpoint_unset) },
            onClick = { editing = true },
        )
        SettingsCard(
            icon = Icons.Outlined.Shield,
            title = L.s(R.string.stage7b_role_moderator),
            value = when {
                caps != null -> roleDisplay(caps!!.roleLabel)
                capsFailure != null -> adminReasonText(capsFailure!!.key)
                else -> L.s(R.string.stage7b_loading)
            },
            onClick = null,
        )
        SettingsCard(
            icon = Icons.Outlined.Logout,
            title = L.s(R.string.stage7b_sign_out),
            value = when (signOutResult) {
                null -> ""
                is AdminResult.Ok -> L.s(R.string.stage7b_sign_out_done)
                is AdminResult.Err -> adminReasonText((signOutResult as AdminResult.Err).failure.key)
            },
            onClick = { model.signOut() },
        )

        Spacer(Modifier.height(JournalSpace.small))

        if (editing) {
            EndpointDialog(
                initial = endpoint,
                onDismiss = { editing = false },
                onSave = { value -> if (model.saveEndpoint(value)) editing = false },
            )
        }
    }
}

private fun roleDisplay(wire: String): String = when (wire) {
    "Administrator" -> L.s(R.string.stage7b_role_administrator)
    "Moderator" -> L.s(R.string.stage7b_role_moderator)
    else -> L.s(R.string.stage7b_role_contributor)
}

@Composable
private fun SettingsCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    onClick: (() -> Unit)?,
) {
    val rowModifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp)
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = .45f),
        ),
        modifier = Modifier.padding(bottom = JournalSpace.small)
            .semantics { contentDescription = if (value.isBlank()) title else "$title: $value" },
    ) {
        Row(rowModifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (value.isNotBlank()) {
                    Text(
                        value,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun EndpointDialog(initial: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf(initial) }
    var error by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(L.s(R.string.stage7b_endpoint)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it; error = false },
                    singleLine = true,
                    isError = error,
                    supportingText = if (error) {
                        { Text(L.s(R.string.stage7b_reason_invalid)) }
                    } else null,
                    placeholder = { Text("https://…") },
                )
                Text(
                    L.s(R.string.stage7b_endpoint_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { if (endpointValid(text)) onSave(text) else error = true }) {
                Text(L.s(R.string.stage7b_save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(L.s(android.R.string.cancel)) } },
    )
}

private fun endpointValid(text: String): Boolean =
    text.trim().isEmpty() || com.opus.piano.admin.AdminApi.validateEndpoint(text.trim()) != null

/** One named switch owner for touch, keyboard and accessibility traversal. */
@Composable internal fun AdminSwitch(label: String, checked: Boolean, enabled: Boolean = true, change: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 48.dp)
            .semantics(mergeDescendants = true) {}
            .adminToggleSemantics(label, checked, androidx.compose.ui.semantics.Role.Switch, enabled) { change(!checked) }
            .toggleable(checked, enabled = enabled, role = androidx.compose.ui.semantics.Role.Switch, onValueChange = change)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        // The row owns the action; this indicator must not introduce another stop/action.
        Switch(checked, onCheckedChange = null, enabled = enabled)
    }
}
