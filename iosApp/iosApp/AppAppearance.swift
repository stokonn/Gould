import SwiftUI
import Combine
import CoreText

/// Persisted appearance preferences — the iOS counterpart of Android's
/// `ProfileSettings.theme/typography` (Room) + `Appearance` object
/// (`goldberg-appearance` SharedPreferences). Same keys, same defaults,
/// same validation rules (`AppearanceRules`).
@MainActor
final class AppAppearance: ObservableObject {
    static let shared = AppAppearance()

    @Published var theme: String { didSet { defaults.set(theme, forKey: Keys.theme) } }
    @Published var typography: String { didSet { defaults.set(typography, forKey: Keys.typography) } }
    @Published var accent: String { didSet { defaults.set(accent, forKey: Keys.accent) } }
    @Published var cards: String { didSet { defaults.set(cards, forKey: Keys.cards) } }
    /// "" = follow system, otherwise "cs" / "en" (Android `PortablePreferences.locale`).
    @Published var localeOverride: String { didSet { defaults.set(localeOverride, forKey: Keys.locale) } }

    private let defaults = UserDefaults.standard

    private enum Keys {
        static let theme = "appearance.theme"
        static let typography = "appearance.typography"
        static let accent = "appearance.accent"
        static let cards = "appearance.cards"
        static let locale = "appearance.locale"
    }

    static let themes = ["System", "Light", "Dark"]
    static let typographies = ["Editorial", "System"]
    static let accents = ["Gold", "Olive", "Rose", "Slate"]
    static let cards = ["Soft", "Outline", "Open"]

    init() {
        Self.registerFonts()
        let d = UserDefaults.standard
        theme = Self.themes.contains(d.string(forKey: Keys.theme) ?? "") ? (d.string(forKey: Keys.theme) ?? "") : "System"
        typography = Self.typographies.contains(d.string(forKey: Keys.typography) ?? "") ? (d.string(forKey: Keys.typography) ?? "") : "Editorial"
        accent = Self.accents.contains(d.string(forKey: Keys.accent) ?? "") ? (d.string(forKey: Keys.accent) ?? "") : "Gold"
        cards = Self.cards.contains(d.string(forKey: Keys.cards) ?? "") ? (d.string(forKey: Keys.cards) ?? "") : "Soft"
        let locale = d.string(forKey: Keys.locale) ?? ""
        localeOverride = ["", "cs", "en"].contains(locale) ? locale : ""
    }

    private static func registerFonts() {
        ["cormorant_garamond", "inter"].forEach { name in
            guard let url = Bundle.main.url(forResource: name, withExtension: "ttf", subdirectory: "Fonts")
                    ?? Bundle.main.url(forResource: name, withExtension: "ttf") else { return }
            CTFontManagerRegisterFontsForURL(url as CFURL, .process, nil)
        }
    }

    /// Resolves the effective dark-mode flag (Android `OpusTheme` logic).
    func isDark(systemDark: Bool) -> Bool {
        switch theme {
        case "Dark": return true
        case "Light": return false
        default: return systemDark
        }
    }

    func colors(systemDark: Bool) -> JournalColors {
        JournalColors(dark: isDark(systemDark: systemDark), accent: accent, cards: cards)
    }

    /// `cornerRadius` for `MaterialTheme.shapes.medium` (0 when cards are Open).
    var mediumRadius: CGFloat { cards == "Open" ? 0 : JournalRadius.medium }

    func reset() {
        theme = "System"
        typography = "Editorial"
        accent = "Gold"
        cards = "Soft"
    }
}
