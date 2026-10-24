package com.opus.piano.ui.admin

import androidx.compose.material3.Checkbox
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.focused
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.requestFocus
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState

/** Keep the name and state on the toggle owner, without synthetic named children. */
@Composable
internal fun Modifier.adminToggleSemantics(
    label: String, checked: Boolean, role: Role, enabled: Boolean = true,
    selection: Boolean = false, toggle: () -> Unit,
): Modifier {
    val requester = remember { FocusRequester() }
    var hasFocus by remember { mutableStateOf(false) }
    return focusRequester(requester).onFocusChanged { hasFocus = it.isFocused }.clearAndSetSemantics {
        contentDescription = label
        this.role = role
        if (selection) selected = checked
        else toggleableState = if (checked) ToggleableState.On else ToggleableState.Off
        if (enabled) {
            // Clearing descendant semantics also clears the foundation Focused /
            // RequestFocus properties. Reconnect them to the real inner focus target.
            focused = hasFocus
            requestFocus { requester.requestFocus() }
        } else disabled()
        onClick {
            if (enabled) { toggle(); true } else false
        }
    }
}

@Composable
internal fun AdminCheckbox(checked: Boolean, label: String, enabled: Boolean = true, change: (Boolean) -> Unit) {
    Checkbox(checked, change, enabled = enabled,
        modifier = Modifier.adminToggleSemantics(label, checked, Role.Checkbox, enabled) { change(!checked) })
}
