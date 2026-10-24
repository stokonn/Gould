package com.opus.piano.ui.admin

import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState

/** Keep the name and state on the toggle owner, without synthetic named children. */
internal fun Modifier.adminToggleSemantics(
    label: String, checked: Boolean, role: Role, enabled: Boolean = true, toggle: () -> Unit,
): Modifier = clearAndSetSemantics {
    contentDescription = label
    this.role = role
    toggleableState = if (checked) ToggleableState.On else ToggleableState.Off
    if (!enabled) disabled()
    onClick {
        if (enabled) { toggle(); true } else false
    }
}

@Composable
internal fun AdminCheckbox(checked: Boolean, label: String, enabled: Boolean = true, change: (Boolean) -> Unit) {
    Checkbox(checked, change, enabled = enabled,
        modifier = Modifier.adminToggleSemantics(label, checked, Role.Checkbox, enabled) { change(!checked) })
}
