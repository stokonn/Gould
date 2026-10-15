package com.opus.piano.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp

val Gold = Color(0xFFD4AF70)
val Ivory = Color(0xFFF3EDE3)
private val Palette = darkColorScheme(
    primary = Color(0xFFD4AF70), onPrimary = Color(0xFF241D13), primaryContainer = Color(0xFF342C24), onPrimaryContainer = Ivory,
    background = Color(0xFF10100F), onBackground = Ivory,
    surface = Color(0xFF1B1A18), onSurface = Ivory,
    surfaceVariant = Color(0xFF24221F), onSurfaceVariant = Color(0xFFA9A39A),
    surfaceContainerLowest = Color(0xFF0D0D0C), surfaceContainerLow = Color(0xFF171614),
    surfaceContainer = Color(0xFF1B1A18), surfaceContainerHigh = Color(0xFF24221F), surfaceContainerHighest = Color(0xFF2D2A25),
    surfaceDim = Color(0xFF10100F), surfaceBright = Color(0xFF39362F),
    secondaryContainer = Color(0xFF3F3526), onSecondaryContainer = Ivory,
    outline = Color(0xFF777167), secondary = Color(0xFFD4AF70), error = Color(0xFFFFB4AB)
)
private val LightPalette = lightColorScheme(
    primary = Color(0xFF795419), onPrimary = Color.White,
    primaryContainer = Color(0xFFF0E3CC), onPrimaryContainer = Color(0xFF2B2114),
    background = Color(0xFFFAF7F0), onBackground = Color(0xFF211F1B),
    surface = Color(0xFFF2EDE3), onSurface = Color(0xFF211F1B),
    onSurfaceVariant = Color(0xFF555047), outline = Color(0xFF7C7468)
)
@Composable fun OpusTheme(theme: String = "System", content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (theme == "Dark" || (theme == "System" && androidx.compose.foundation.isSystemInDarkTheme())) Palette else LightPalette, typography = Typography(
        headlineLarge = TextStyle(fontSize = 32.sp, lineHeight = 38.sp),
        headlineMedium = TextStyle(fontSize = 28.sp, lineHeight = 34.sp),
        titleLarge = TextStyle(fontSize = 21.sp, lineHeight = 27.sp),
        labelSmall = TextStyle(fontSize = 11.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Medium)
    ), shapes = Shapes(
        small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        medium = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        large = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
    ), content = content)
}
