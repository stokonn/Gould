package com.opus.piano.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import com.opus.piano.R
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
private val EditorialSerif = FontFamily(Font(R.font.cormorant_garamond, FontWeight.Normal), Font(R.font.cormorant_garamond, FontWeight.Medium), Font(R.font.cormorant_garamond, FontWeight.SemiBold), Font(R.font.cormorant_garamond, FontWeight.Bold))
private val EditorialSans = FontFamily(Font(R.font.inter, FontWeight.Normal), Font(R.font.inter, FontWeight.Medium), Font(R.font.inter, FontWeight.SemiBold), Font(R.font.inter, FontWeight.Bold))
private fun journalTypography(system: Boolean): Typography {
    val sans = if (system) FontFamily.Default else EditorialSans
    val serif = if (system) FontFamily.Default else EditorialSerif
    fun text(size: Int, height: Int, heading: Boolean = false, weight: FontWeight = FontWeight.Normal) =
        TextStyle(fontFamily = if (heading) serif else sans, fontSize = size.sp, lineHeight = height.sp, fontWeight = weight)
    return Typography(
        displayLarge = text(56, 64, true), displayMedium = text(44, 52, true), displaySmall = text(36, 44, true),
        headlineLarge = text(36, 44, true), headlineMedium = text(30, 38, true), headlineSmall = text(26, 34, true),
        titleLarge = text(24, 32, true, FontWeight.Medium), titleMedium = text(17, 25, weight = FontWeight.Medium), titleSmall = text(15, 22, weight = FontWeight.Medium),
        bodyLarge = text(16, 25), bodyMedium = text(14, 22), bodySmall = text(12, 18),
        labelLarge = text(14, 20, weight = FontWeight.Medium), labelMedium = text(12, 18, weight = FontWeight.Medium), labelSmall = text(11, 16, weight = FontWeight.Medium)
    )
}
@Composable fun OpusTheme(theme: String = "System", typography: String = "Editorial", content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (theme == "Dark" || (theme == "System" && androidx.compose.foundation.isSystemInDarkTheme())) Palette else LightPalette,
        typography = androidx.compose.runtime.remember(typography) { journalTypography(typography == "System") },
        shapes = Shapes(small = androidx.compose.foundation.shape.RoundedCornerShape(4.dp), medium = androidx.compose.foundation.shape.RoundedCornerShape(10.dp), large = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)),
        content = content
    )
}
