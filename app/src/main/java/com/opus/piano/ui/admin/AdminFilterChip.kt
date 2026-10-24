package com.opus.piano.ui.admin

import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected

/** One named, checkable action, including in Android's exported accessibility tree. */
@Composable
internal fun AdminFilterChip(selected: Boolean, label: String, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        // With merging semantics Compose 1.9 exports contentDescription on a synthetic
        // child. Replace the descendant semantics so the checkable owner has the name.
        // Explicit state/action preserve both accessibility and ordinary chip input.
        modifier = Modifier.clearAndSetSemantics {
            contentDescription = label
            role = Role.Checkbox
            this.selected = selected
            onClick { onClick(); true }
        },
    )
}
