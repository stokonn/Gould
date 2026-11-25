import SwiftUI

// MARK: - Design tokens (mirrors Android `ui/Theme.kt`)
//
// Every colour, radius, spacing and type step here is a 1:1 port of the
// Compose design system (`OpusTheme` / `journalColors` / `JournalSpace` /
// `journalTypography`). Screens must never hardcode colours or metrics.

enum JournalSpace {
    static let tiny: CGFloat = 4
    static let small: CGFloat = 8
    static let medium: CGFloat = 16
    static let page: CGFloat = 20
    static let large: CGFloat = 28
    static let touch: CGFloat = 48
}

enum JournalRadius {
    static let small: CGFloat = 12
    static let medium: CGFloat = 14
    static let large: CGFloat = 20
    /// Extra-large dialog surface (`MaterialTheme.shapes.extraLarge` ≈ 28).
    static let dialog: CGFloat = 28
}

/// Semantic colour roles. Values come straight from the Android palettes.
enum JournalPalette {
    // Nocturne (dark)
    static let darkPrimary = Color(hex: 0xFFE9C58A)
    static let darkOnPrimary = Color(hex: 0xFF211C15)
    static let darkBackground = Color(hex: 0xFF0B0D0E)
    static let darkOnBackground = Color(hex: 0xFFF0E8DE)
    static let darkSurface = Color(hex: 0xFF151819)
    static let darkOnSurface = Color(hex: 0xFFF0E8DE)
    static let darkSurfaceVariant = Color(hex: 0xFF242829)
    static let darkOnSurfaceVariant = Color(hex: 0xFFBDBCB7)
    static let darkOutline = Color(hex: 0xFF827C70)
    static let darkOutlineVariant = Color(hex: 0xFF343938)
    static let darkPrimaryContainer = Color(hex: 0xFF393024)
    static let darkOnPrimaryContainer = Color(hex: 0xFFF2E4CF)

    // Ivory (light)
    static let lightPrimary = Color(hex: 0xFF825E2C)
    static let lightOnPrimary = Color.white
    static let lightBackground = Color(hex: 0xFFF5F1EA)
    static let lightOnBackground = Color(hex: 0xFF27221E)
    static let lightSurface = Color.white
    static let lightOnSurface = Color(hex: 0xFF27221E)
    static let lightSurfaceVariant = Color(hex: 0xFFECE5D9)
    static let lightOnSurfaceVariant = Color(hex: 0xFF716656)
    static let lightOutline = Color(hex: 0xFF827665)
    static let lightOutlineVariant = Color(hex: 0xFFE4DCD0)
    static let lightPrimaryContainer = Color(hex: 0xFFEFE1CC)
    static let lightOnPrimaryContainer = Color(hex: 0xFF382918)

    /// Fixed semantic Activity colours (`ActivityColors`): recognisable in both themes.
    static let training = Color(hex: 0xFF4F8A62)
    static let dayOff = Color(hex: 0xFFB59C67)
    static let miss = Color(hex: 0xFFB65C63)
}

/// Resolved semantic colours for the active theme + accent + card style.
struct JournalColors {
    let primary: Color
    let onPrimary: Color
    let background: Color
    let onBackground: Color
    var surface: Color
    let onSurface: Color
    let surfaceVariant: Color
    let onSurfaceVariant: Color
    let outline: Color
    let outlineVariant: Color
    let primaryContainer: Color
    let onPrimaryContainer: Color
    let secondary: Color
    let error: Color

    init(dark: Bool, accent: String) {
        let base: (primary: Color, onPrimary: Color, background: Color, onBackground: Color,
                   surface: Color, onSurface: Color, surfaceVariant: Color, onSurfaceVariant: Color,
                   outline: Color, outlineVariant: Color, primaryContainer: Color,
                   onPrimaryContainer: Color) = dark
            ? (JournalPalette.darkPrimary, JournalPalette.darkOnPrimary,
               JournalPalette.darkBackground, JournalPalette.darkOnBackground,
               JournalPalette.darkSurface, JournalPalette.darkOnSurface,
               JournalPalette.darkSurfaceVariant, JournalPalette.darkOnSurfaceVariant,
               JournalPalette.darkOutline, JournalPalette.darkOutlineVariant,
               JournalPalette.darkPrimaryContainer, JournalPalette.darkOnPrimaryContainer)
            : (JournalPalette.lightPrimary, JournalPalette.lightOnPrimary,
               JournalPalette.lightBackground, JournalPalette.lightOnBackground,
               JournalPalette.lightSurface, JournalPalette.lightOnSurface,
               JournalPalette.lightSurfaceVariant, JournalPalette.lightOnSurfaceVariant,
               JournalPalette.lightOutline, JournalPalette.lightOutlineVariant,
               JournalPalette.lightPrimaryContainer, JournalPalette.lightOnPrimaryContainer)

        let resolvedAccent: Color
        switch accent {
        case "Olive": resolvedAccent = dark ? Color(hex: 0xFFB4BF95) : Color(hex: 0xFF516344)
        case "Rose": resolvedAccent = dark ? Color(hex: 0xFFD6A3AE) : Color(hex: 0xFF8C4D5B)
        case "Slate": resolvedAccent = dark ? Color(hex: 0xFFA6BACC) : Color(hex: 0xFF466179)
        default: resolvedAccent = base.primary
        }

        primary = resolvedAccent
        secondary = resolvedAccent
        onPrimary = base.onPrimary
        background = base.background
        onBackground = base.onBackground
        surface = base.surface
        onSurface = base.onSurface
        surfaceVariant = base.surfaceVariant
        onSurfaceVariant = base.onSurfaceVariant
        outline = base.outline
        outlineVariant = base.outlineVariant
        primaryContainer = base.primaryContainer
        onPrimaryContainer = base.onPrimaryContainer
        error = dark ? Color(hex: 0xFFF2B8B5) : Color(hex: 0xFFB3261E)
    }

    /// `cards == "Open"` collapses surfaces into the background (`journalColors`).
    init(dark: Bool, accent: String, cards: String) {
        self.init(dark: dark, accent: accent)
        if cards == "Open" {
            surface = background
        }
    }
}

// MARK: - Typography (`journalTypography`)

/// The two editorial families, bundled from the same TTFs as Android.
enum JournalFont {
    static let serif = "Cormorant Garamond"
    static let sans = "Inter"

    static func serif(_ size: CGFloat, weight: Font.Weight = .regular, relativeTo style: Font.TextStyle? = nil) -> Font {
        let font = style.map { Font.custom(JournalFont.serif, size: size, relativeTo: $0) }
            ?? Font.custom(JournalFont.serif, size: size)
        return font.weight(weight)
    }

    static func sans(_ size: CGFloat, weight: Font.Weight = .regular, relativeTo style: Font.TextStyle? = nil) -> Font {
        let font = style.map { Font.custom(JournalFont.sans, size: size, relativeTo: $0) }
            ?? Font.custom(JournalFont.sans, size: size)
        return font.weight(weight)
    }
}

/// Material-typography equivalents (`journalTypography`), Editorial (serif headings) mode.
enum JournalType {
    static let displayLarge = JournalFont.sans(44, weight: .regular, relativeTo: .largeTitle)
    static let displayMedium = JournalFont.sans(38, weight: .regular, relativeTo: .largeTitle)
    static let displaySmall = JournalFont.sans(32, weight: .regular, relativeTo: .largeTitle)

    static let headlineLarge = JournalFont.serif(34, weight: .bold, relativeTo: .largeTitle)
    static let headlineMedium = JournalFont.serif(28, weight: .semibold, relativeTo: .title)
    static let headlineSmall = JournalFont.serif(24, weight: .regular, relativeTo: .title2)

    static let titleLarge = JournalFont.serif(22, weight: .semibold, relativeTo: .title2)
    static let titleMedium = JournalFont.sans(16, weight: .medium, relativeTo: .subheadline)
    static let titleSmall = JournalFont.sans(15, weight: .medium, relativeTo: .subheadline)

    static let bodyLarge = JournalFont.sans(15, weight: .regular, relativeTo: .subheadline)
    static let bodyMedium = JournalFont.sans(14, weight: .regular, relativeTo: .footnote)
    static let bodySmall = JournalFont.sans(12, weight: .regular, relativeTo: .caption)

    static let labelLarge = JournalFont.sans(14, weight: .medium, relativeTo: .footnote)
    static let labelMedium = JournalFont.sans(12, weight: .medium, relativeTo: .caption)
    static let labelSmall = JournalFont.sans(11, weight: .medium, relativeTo: .caption2)
}

enum JournalFontRole {
    case displaySmall, displayMedium, titleLarge, titleMedium, titleSmall
    case bodyMedium, bodySmall, labelSmall, footnote, caption, caption2, callout, title2, title3
}

private struct JournalTypographyKey: EnvironmentKey {
    static let defaultValue = "Editorial"
}

extension EnvironmentValues {
    var journalTypography: String {
        get { self[JournalTypographyKey.self] }
        set { self[JournalTypographyKey.self] = newValue }
    }
}

private struct JournalFontModifier: ViewModifier {
    @Environment(\.journalTypography) private var preference
    let role: JournalFontRole

    func body(content: Content) -> some View {
        let editorial = preference == "Editorial"
        content.font(font(editorial: editorial))
    }

    private func font(editorial: Bool) -> Font {
        switch role {
        case .displaySmall: editorial ? JournalFont.sans(32, relativeTo: .largeTitle) : .largeTitle
        case .displayMedium: editorial ? JournalFont.sans(38, relativeTo: .largeTitle) : .largeTitle
        case .titleLarge: editorial ? JournalFont.serif(22, weight: .semibold, relativeTo: .title2) : .title2
        case .titleMedium: editorial ? JournalFont.sans(16, weight: .medium, relativeTo: .subheadline) : .headline
        case .titleSmall: editorial ? JournalFont.sans(15, weight: .medium, relativeTo: .subheadline) : .subheadline
        case .bodyMedium: editorial ? JournalFont.sans(14, relativeTo: .footnote) : .callout
        case .bodySmall, .footnote: editorial ? JournalFont.sans(12, relativeTo: .caption) : .footnote
        case .labelSmall, .caption2: editorial ? JournalFont.sans(11, weight: .medium, relativeTo: .caption2) : .caption2
        case .caption: editorial ? JournalFont.sans(12, relativeTo: .caption) : .caption
        case .callout: editorial ? JournalFont.sans(15, relativeTo: .callout) : .callout
        case .title2: editorial ? JournalFont.serif(28, weight: .semibold, relativeTo: .title2) : .title
        case .title3: editorial ? JournalFont.serif(24, relativeTo: .title2) : .title3
        }
    }
}

extension View {
    func journalFont(_ role: JournalFontRole) -> some View {
        modifier(JournalFontModifier(role: role))
    }
}

// MARK: - Environment

private struct JournalColorsKey: EnvironmentKey {
    static let defaultValue = JournalColors(dark: true, accent: "Gold", cards: "Soft")
}

private struct JournalCardsKey: EnvironmentKey {
    static let defaultValue = "Soft"
}

extension EnvironmentValues {
    var journal: JournalColors {
        get { self[JournalColorsKey.self] }
        set { self[JournalColorsKey.self] = newValue }
    }

    var journalCards: String {
        get { self[JournalCardsKey.self] }
        set { self[JournalCardsKey.self] = newValue }
    }
}

struct JournalCardStyle: ViewModifier {
    @Environment(\.journal) private var colors
    @Environment(\.journalCards) private var style

    func body(content: Content) -> some View {
        let shape = RoundedRectangle(cornerRadius: style == "Open" ? 0 : JournalRadius.medium)
        content
            .padding(JournalSpace.medium)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(style == "Open" ? colors.background : colors.surface, in: shape)
            .overlay {
                if style == "Outline" { shape.stroke(colors.outlineVariant, lineWidth: 1) }
            }
    }
}

extension View {
    func journalCard() -> some View { modifier(JournalCardStyle()) }
}

extension Color {
    init(hex: UInt32) {
        self.init(
            red: Double((hex >> 16) & 0xFF) / 255.0,
            green: Double((hex >> 8) & 0xFF) / 255.0,
            blue: Double(hex & 0xFF) / 255.0
        )
    }
}
