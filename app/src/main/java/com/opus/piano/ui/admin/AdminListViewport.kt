package com.opus.piano.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.opus.piano.ui.JournalSpace

/** Expanded headers must not measure the list down to a few pixels at large fonts. */
@Composable
internal fun AdminListViewport(content: @Composable ColumnScope.(Modifier) -> Unit) {
    val expanded = LocalDensity.current.fontScale > 1.3f
    val scroll = rememberScrollState()
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val viewportHeight = maxHeight
        Column(
            Modifier.fillMaxSize()
                .then(if (expanded) Modifier.verticalScroll(scroll) else Modifier)
                .padding(horizontal = JournalSpace.page, vertical = JournalSpace.small),
        ) {
            // The nested lazy list always receives finite constraints. Its own saved
            // scroll anchor is unchanged; the outer fallback exposes headers/footer.
            val listModifier = if (expanded) Modifier.fillMaxWidth()
                .heightIn(min = minOf(240.dp, viewportHeight), max = viewportHeight)
            else Modifier.weight(1f)
            content(listModifier)
        }
    }
}
