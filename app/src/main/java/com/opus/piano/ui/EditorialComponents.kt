package com.opus.piano.ui

import com.opus.piano.i18n.L
import com.opus.piano.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
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
