package com.opus.piano.ui

import com.opus.piano.i18n.L
import com.opus.piano.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.*

@Composable internal fun LegalScreen(close: () -> Unit) {
    val context = LocalContext.current
    val content by produceState("") { value = withContext(Dispatchers.IO) {
        listOf("legal/third-party-notices.md","licenses/Apache-2.0.txt","licenses/CormorantGaramond-OFL.txt","licenses/Inter-OFL.txt").joinToString("\n\n") { path -> context.assets.open(path).bufferedReader().use { it.readText() } }
    } }
    Dialog(onDismissRequest = close) { Surface(shape = MaterialTheme.shapes.medium) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            SectionTitle(L.s(R.string.msg_privacy_licenses_2fee)); Text(L.s(R.string.privacy_text));Text(L.s(R.string.copyright_notices));Text(content,style=MaterialTheme.typography.bodySmall)
            TextButton(onClick=close) { Text(L.s(R.string.msg_close_bbfa)) }
        }
    } }
}
