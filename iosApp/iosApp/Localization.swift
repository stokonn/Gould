import Foundation

func appLocale() -> Locale {
    let override = UserDefaults.standard.string(forKey: "appearance.locale") ?? ""
    return Locale(identifier: override.isEmpty ? Locale.current.identifier : override)
}

func formattedDate(_ date: Date, dateStyle: DateFormatter.Style = .medium,
                   timeStyle: DateFormatter.Style = .none) -> String {
    let formatter = DateFormatter()
    formatter.locale = appLocale()
    formatter.dateStyle = dateStyle
    formatter.timeStyle = timeStyle
    return formatter.string(from: date)
}

/// Localized string lookup for the iOS app (EN/CZ, matching the Android set).
/// User-entered text and catalogue names are never passed through this.
func tr(_ key: String) -> String {
    let language = UserDefaults.standard.string(forKey: "appearance.locale") ?? ""
    guard !language.isEmpty,
          let path = Bundle.main.path(forResource: language, ofType: "lproj"),
          let bundle = Bundle(path: path) else {
        return NSLocalizedString(key, comment: "")
    }
    return bundle.localizedString(forKey: key, value: nil, table: nil)
}

func tr(_ key: String, _ arguments: CVarArg...) -> String {
    String(format: tr(key), locale: appLocale(), arguments: arguments)
}
