package com.opus.piano.ui

import android.app.UiModeManager
import android.graphics.BitmapFactory
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.opus.piano.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Original authorized local artwork; decoding never blocks Home composition. */
@Composable internal fun NatureQuoteCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val open = LocalAppearance.current.cards == "Open"
    val landscape by produceState<ImageBitmap?>(null, context, open) {
        value = withContext(Dispatchers.IO) {
            val highContrast = Build.VERSION.SDK_INT >= 34 &&
                (context.getSystemService(UiModeManager::class.java)?.contrast ?: 0f) > .5f
            if (open || highContrast) null else runCatching {
                BitmapFactory.decodeResource(context.resources, R.drawable.quote_nature,
                    BitmapFactory.Options().apply { inSampleSize = 2; inScaled = false })?.asImageBitmap()
            }.getOrNull()
        }
    }
    val surface = MaterialTheme.colorScheme.surface
    val dark = surface.luminance() < .5f
    JournalCard(modifier, padding = PaddingValues(0.dp)) {
        Box(Modifier.fillMaxWidth()) {
            landscape?.let {
                Image(it, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize())
                Box(Modifier.matchParentSize().background(Brush.horizontalGradient(listOf(
                    surface.copy(alpha = .97f), surface.copy(alpha = if (dark) .90f else .93f)))))
            }
            Box(Modifier.padding(12.dp)) { content() }
        }
    }
}
