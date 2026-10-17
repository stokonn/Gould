package com.opus.piano.ui

import com.opus.piano.i18n.L
import com.opus.piano.R
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp

val Gold = Color(0xFFB28B54)
val Ivory = Color(0xFFF0E8DE)
object JournalSpace { val tiny=4.dp; val small=8.dp; val medium=16.dp; val page=20.dp; val large=28.dp; val touch=48.dp }
val LocalAppearance = androidx.compose.runtime.staticCompositionLocalOf { com.opus.piano.preferences.Appearance() }
private val Palette = darkColorScheme(
    primary=Color(0xFFC2B5A3),onPrimary=Color(0xFF211C15),background=Color(0xFF171717),onBackground=Color(0xFFF0E8DE),
    surface=Color(0xFF242321),onSurface=Color(0xFFF0E8DE),surfaceVariant=Color(0xFF30332F),onSurfaceVariant=Color(0xFFB9B1A4),
    outline=Color(0xFF827C70),outlineVariant=Color(0xFF44473F),primaryContainer=Color(0xFF393024),onPrimaryContainer=Color(0xFFF2E4CF),
    secondary=Color(0xFFC2B5A3),secondaryContainer=Color(0xFF393024),onSecondaryContainer=Color(0xFFF2E4CF))
private val LightPalette = lightColorScheme(
    primary=Color(0xFF776956),onPrimary=Color.White,background=Color(0xFFF5F1EA),onBackground=Color(0xFF27221E),
    surface=Color(0xFFFFFFFF),onSurface=Color(0xFF27221E),surfaceVariant=Color(0xFFECE5D9),onSurfaceVariant=Color(0xFF716656),
    outline=Color(0xFF827665),outlineVariant=Color(0xFFE4DCD0),primaryContainer=Color(0xFFEFE1CC),onPrimaryContainer=Color(0xFF382918),
    secondary=Color(0xFF776956),secondaryContainer=Color(0xFFEFE1CC),onSecondaryContainer=Color(0xFF382918))
private val EditorialSerif = FontFamily(Font(R.font.cormorant_garamond, FontWeight.Normal), Font(R.font.cormorant_garamond, FontWeight.Medium), Font(R.font.cormorant_garamond, FontWeight.SemiBold), Font(R.font.cormorant_garamond, FontWeight.Bold))
private val EditorialSans = FontFamily(Font(R.font.inter, FontWeight.Normal), Font(R.font.inter, FontWeight.Medium), Font(R.font.inter, FontWeight.SemiBold), Font(R.font.inter, FontWeight.Bold))
private fun journalTypography(system: Boolean): Typography {
    val sans = if (system) FontFamily.Default else EditorialSans
    val serif = if (system) FontFamily.Default else EditorialSerif
    fun text(size: Int, height: Int, heading: Boolean = false, weight: FontWeight = FontWeight.Normal) =
        TextStyle(fontFeatureSettings = "tnum", fontFamily = if (heading) serif else sans, fontSize = size.sp, lineHeight = height.sp, fontWeight = weight)
    return Typography(
        displayLarge = text(44, 52), displayMedium = text(38, 46), displaySmall = text(32, 40),
        headlineLarge = text(32, 40, true), headlineMedium = text(26, 34, true), headlineSmall = text(24, 32, true),
        titleLarge = text(22, 30, true, FontWeight.Medium), titleMedium = text(17, 25, weight = FontWeight.Medium), titleSmall = text(15, 22, weight = FontWeight.Medium),
        bodyLarge = text(16, 25), bodyMedium = text(14, 22), bodySmall = text(12, 18),
        labelLarge = text(14, 20, weight = FontWeight.Medium), labelMedium = text(12, 18, weight = FontWeight.Medium), labelSmall = text(11, 16, weight = FontWeight.Medium)
    )
}
@Composable fun OpusTheme(theme: String = "System", typography: String = "Editorial", appearance: com.opus.piano.preferences.Appearance = com.opus.piano.preferences.Appearance(), content: @Composable () -> Unit) {
    val dark=theme=="Dark" || (theme=="System" && androidx.compose.foundation.isSystemInDarkTheme())
    val base=if(dark) Palette else LightPalette
    val accent=when(appearance.accent) {
        "Olive" -> if(dark) Color(0xFFB4BF95) else Color(0xFF516344)
        "Rose" -> if(dark) Color(0xFFD6A3AE) else Color(0xFF8C4D5B)
        "Slate" -> if(dark) Color(0xFFA6BACC) else Color(0xFF466179)
        else -> base.primary
    }
    androidx.compose.runtime.CompositionLocalProvider(LocalAppearance provides appearance) {
        MaterialTheme(colorScheme=base.copy(primary=accent,secondary=accent,surface=if(appearance.cards=="Open") base.background else base.surface),
            typography=androidx.compose.runtime.remember(typography) { journalTypography(typography=="System") },
            shapes=Shapes(small=androidx.compose.foundation.shape.RoundedCornerShape(12.dp),medium=androidx.compose.foundation.shape.RoundedCornerShape(if(appearance.cards=="Open") 0.dp else 14.dp),large=androidx.compose.foundation.shape.RoundedCornerShape(20.dp)),content=content)
    }
}

/** Decorative palettes stay centralized; never use these for ordinary screen text. */
object MusicalColors {
    val natural=Color(0xFFF3E7D2);val naturalPressed=Color(0xFFD2C0A3);val naturalInk=Color(0xFF27221E)
    val accidental=Color(0xFF25211E);val accidentalPressed=Color(0xFF62584D);val accidentalInk=Color(0xFFFFF7E9)
    val terminal=Color(0xFF211323);val terminalInk=Color(0xFFFFAFD7)
}
