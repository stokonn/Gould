package com.opus.piano.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowOutward
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Rule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.opus.piano.R
import com.opus.piano.admin.AdminStats
import com.opus.piano.admin.AdminTile
import com.opus.piano.admin.AdminTileValue
import com.opus.piano.i18n.L
import com.opus.piano.ui.JournalSpace
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Stage 7B.3 — Administration dashboard.
 *
 * NON-NEGOTIABLE layout contract: fits ONE normal supported screen with NO vertical
 * or horizontal scrolling; content is never clipped and text is never shrunk to make
 * it fit. A vertical-scroll fallback activates ONLY at extreme accessibility font
 * scaling (> [SCROLL_FALLBACK_FONT_SCALE]), where stacking beats clipping.
 *
 * Visual direction: docs/design/stage-7b (minimalist dark, quiet charcoal cards,
 * hairline borders, restrained semantic accents) expressed through the existing
 * Goldberg theme tokens — no ornamental redesign, no hardcoded mockup numbers.
 */
private const val SCROLL_FALLBACK_FONT_SCALE = 1.3f

@Composable internal fun AdminDashboardScreen(
    model: AdminViewModel,
    onQueue: (filter: String?) -> Unit,
    onAudit: (AdminTile?) -> Unit,
    onSettings: () -> Unit,
) {
    val loading by model.loading.collectAsStateWithLifecycle()
    val caps by model.caps.collectAsStateWithLifecycle()
    val capsFailure by model.capsFailure.collectAsStateWithLifecycle()
    val stats by model.stats.collectAsStateWithLifecycle()
    val resumeId by model.resumeId.collectAsStateWithLifecycle()

    AdminOnResume(onResume = { model.refresh() })
    val fontScale = LocalDensity.current.fontScale
    val scrollState = rememberScrollState()
    val scrolling = fontScale > SCROLL_FALLBACK_FONT_SCALE
    val contentPadding = PaddingValues(horizontal = JournalSpace.page, vertical = JournalSpace.small)

    Column(
        Modifier.fillMaxSize()
            .then(if (scrolling) Modifier.verticalScroll(scrollState) else Modifier)
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(JournalSpace.small),
    ) {
        DashboardHeader(
            roleLabel = when {
                caps != null -> roleLabel(caps!!.roleLabel)
                capsFailure != null -> L.s(R.string.stage7b_role_unverified)
                else -> L.s(R.string.stage7b_role_unverified)
            },
        )
        Text(
            L.s(R.string.stage7b_overview_label).uppercase(L.locale),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.semantics { heading() },
        )
        StatTileRow(
            left = AdminTile.PENDING to stats.tileValue(AdminTile.PENDING),
            right = AdminTile.NEEDS_INFO to stats.tileValue(AdminTile.NEEDS_INFO),
            onClick = { onQueue(it.name.lowercase(Locale.ROOT)) },
        )
        StatTileRow(
            left = AdminTile.APPROVED_TODAY to stats.tileValue(AdminTile.APPROVED_TODAY),
            right = AdminTile.REJECTED_TODAY to stats.tileValue(AdminTile.REJECTED_TODAY),
            onClick = { onAudit(it) },
        )
        DestinationRow(
            icon = Icons.Outlined.Rule,
            title = L.s(R.string.stage7b_moderation_queue),
            secondLine = resumeId?.let { L.s(R.string.stage7b_resume_line, it) },
            trailingText = resumeId?.let { L.s(R.string.stage7b_continue) },
            onClick = { onQueue(null) },
        )
        DestinationRow(icon = Icons.Outlined.History, title = L.s(R.string.stage7b_audit_log), onClick = { onAudit(null) })
        DestinationRow(icon = Icons.Outlined.Settings, title = L.s(R.string.stage7b_admin_settings), onClick = onSettings)

        // In fixed layout, push footer to bottom.
        // In scroll fallback, the footer simply follows the content.
        if (!scrolling) Spacer(Modifier.weight(1f))
        
        DashboardFooter(
            updatedAt = stats?.updatedAt ?: 0L,
            loading = loading,
            onRefresh = { model.refresh() },
        )
    }
}

private fun AdminStats?.tileValue(tile: AdminTile): AdminTileValue? = this?.tiles?.get(tile)

private fun roleLabel(wire: String): String = when (wire) {
    "Administrator" -> L.s(R.string.stage7b_role_administrator)
    "Moderator" -> L.s(R.string.stage7b_role_moderator)
    else -> L.s(R.string.stage7b_role_contributor)
}

@Composable
private fun DashboardHeader(roleLabel: String) {
    Row(
        Modifier.fillMaxWidth().padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(JournalSpace.small),
    ) {
        Text(
            L.s(R.string.stage7b_administration),
            style = MaterialTheme.typography.headlineMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).semantics { heading() },
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.semantics {
                contentDescription = roleLabel
            },
        ) {
            Icon(Icons.Outlined.Shield, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                roleLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun StatTileRow(
    left: Pair<AdminTile, AdminTileValue?>,
    right: Pair<AdminTile, AdminTileValue?>,
    onClick: (AdminTile) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(JournalSpace.small)) {
        StatTile(left.first, left.second, Modifier.weight(1f), onClick)
        StatTile(right.first, right.second, Modifier.weight(1f), onClick)
    }
}

@Composable
private fun StatTile(tile: AdminTile, value: AdminTileValue?, modifier: Modifier, onClick: (AdminTile) -> Unit) {
    val label = when (tile) {
        AdminTile.PENDING -> L.s(R.string.stage7b_pending)
        AdminTile.NEEDS_INFO -> L.s(R.string.stage7b_needs_info)
        AdminTile.APPROVED_TODAY -> L.s(R.string.stage7b_approved_today)
        AdminTile.REJECTED_TODAY -> L.s(R.string.stage7b_rejected_today)
    }
    val display = when (value) {
        null -> "…" // first load placeholder — never a fabricated count
        is AdminTileValue.Exact -> NumberFormat.getIntegerInstance(L.locale).format(value.count)
        is AdminTileValue.Unavailable -> adminReasonText(value.reasonKey)
    }
    val isExact = value is AdminTileValue.Exact
    val cd = "$label: $display"
    Surface(
        onClick = { onClick(tile) },
        modifier = modifier.semantics { contentDescription = cd },
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = .45f),
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            Modifier.fillMaxWidth().heightIn(min = 84.dp).padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    Icons.Outlined.ArrowOutward, null,
                    Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .7f),
                )
            }
            Text(
                display,
                style = if (isExact) {
                    MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFeatureSettings = "tnum",
                    )
                } else {
                    MaterialTheme.typography.bodyMedium
                },
                color = if (isExact) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .8f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DestinationRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    secondLine: String? = null,
    trailingText: String? = null,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = .45f),
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(icon, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (secondLine != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            secondLine,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        if (trailingText != null) {
                            Text(
                                "$trailingText →",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
            Icon(
                Icons.Outlined.ChevronRight, null,
                Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DashboardFooter(updatedAt: Long, loading: Boolean, onRefresh: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 44.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            if (updatedAt > 0) L.s(R.string.stage7b_last_updated, formatTime(updatedAt))
            else L.s(R.string.stage7b_unavailable),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Start,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onRefresh, enabled = !loading) {
            Icon(Icons.Outlined.Refresh, L.s(R.string.stage7b_refresh), tint = MaterialTheme.colorScheme.primary)
        }
    }
}

/** Application-locale 24h clock for the "last updated" footer. */
private fun formatTime(millis: Long): String = try {
    DateTimeFormatter.ofPattern("HH:mm", L.locale)
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(millis))
} catch (_: Exception) {
    ""
}
