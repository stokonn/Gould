package com.opus.piano.ui

import com.opus.piano.i18n.L
import com.opus.piano.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.opus.piano.data.compactPracticeTime

/** Delay prevents flashing a placeholder for immediately available local content. No animation. */
@Composable internal fun EditorialSkeleton() {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { kotlinx.coroutines.delay(180); visible = true }
    if (visible) Column(Modifier.fillMaxWidth().semantics { contentDescription = L.s(R.string.msg_loading_saved_content_47e0) }, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf(.55f, 1f, .8f).forEach { fraction ->
            Spacer(Modifier.fillMaxWidth(fraction).height(22.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = .07f), MaterialTheme.shapes.small))
        }
    }
}

/** Editorial serif section title with an optional "See all" affordance (Stage 7A). */
@Composable internal fun EditorialSectionHeader(title: String, subtitle: String = "", actionLabel: String? = null, action: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
            if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (actionLabel != null) JournalTextButton(onClick = action, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)) {
            Text(actionLabel, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Icon(Icons.Outlined.ChevronRight, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
        }
    }
}

/** Local/Online availability pill. Truthful labels only: "Local" means the content is on this device. */
@Composable internal fun AvailabilityPill(online: Boolean, modifier: Modifier = Modifier) {
    val tint = MaterialTheme.colorScheme.primary
    Surface(modifier = modifier, shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surface, contentColor = tint) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(if (online) Icons.Outlined.Language else Icons.Outlined.PhoneAndroid, null, Modifier.size(13.dp), tint = tint)
            Text(L.s(if (online) R.string.stage7a_online else R.string.stage7a_local), style = MaterialTheme.typography.labelSmall, color = tint, maxLines = 1)
        }
    }
}

/** One rounded search field style for editorial destinations (Stage 7A). */
@Composable internal fun EditorialSearchField(query: String, change: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(query, change, modifier.fillMaxWidth(), label = { Text(L.s(R.string.stage7a_search_placeholder), maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingIcon = { Icon(Icons.Outlined.Search, null) }, singleLine = true,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
}
