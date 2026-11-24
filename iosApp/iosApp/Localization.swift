import Foundation

/// Localized string lookup for the iOS app (EN/CZ, matching the Android set).
/// User-entered text and catalogue names are never passed through this.
func tr(_ key: String) -> String {
    NSLocalizedString(key, comment: "")
}
