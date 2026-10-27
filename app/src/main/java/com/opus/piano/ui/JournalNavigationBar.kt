package com.opus.piano.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.semantics.focused
import androidx.compose.ui.semantics.requestFocus
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp

internal data class JournalNavigationItem(val route: String, val label: String, val icon: ImageVector)

/** Equal editorial tabs whose natural label height grows with Android font scaling.
 * Unlike a fixed-height bar, every item can wrap without an ellipsis or smaller text.
 * The selectable owner preserves one complete name, selected state and keyboard action.
 */
@Composable
internal fun JournalNavigationBar(
    items: List<JournalNavigationItem>,
    selectedRoute: String,
    onSelect: (String) -> Unit,
    onLabelLayout: ((String, TextLayoutResult) -> Unit)? = null,
) {
    Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
        Row(
            Modifier.fillMaxWidth().windowInsetsPadding(NavigationBarDefaults.windowInsets)
                .height(IntrinsicSize.Min).selectableGroup(),
        ) {
            items.forEach { item ->
                val selected = item.route == selectedRoute
                val requester = remember { FocusRequester() }
                var hasFocus by remember { mutableStateOf(false) }
                val tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                Column(
                    Modifier.weight(1f).fillMaxHeight().heightIn(min = 80.dp)
                        .focusRequester(requester).onFocusChanged { hasFocus = it.isFocused }
                        .selectable(selected, role = Role.Tab, onClick = { onSelect(item.route) })
                        .clearAndSetSemantics {
                            contentDescription = item.label
                            role = Role.Tab
                            this.selected = selected
                            focused = hasFocus
                            requestFocus { requester.requestFocus() }
                            onClick { onSelect(item.route); true }
                        }
                        .padding(horizontal = 1.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                ) {
                    Icon(item.icon, null, Modifier.size(24.dp), tint = tint)
                    Text(
                        item.label,
                        modifier = Modifier.clearAndSetSemantics {},
                        style = MaterialTheme.typography.labelSmall.copy(lineBreak = LineBreak.Heading, hyphens = Hyphens.Auto),
                        color = tint,
                        textAlign = TextAlign.Center,
                        onTextLayout = { onLabelLayout?.invoke(item.route, it) },
                    )
                }
            }
        }
    }
}
