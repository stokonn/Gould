import SwiftUI

struct RootView: View {
    @EnvironmentObject private var appearance: AppAppearance
    @Environment(\.colorScheme) private var systemColorScheme

    var body: some View {
        let dark = appearance.isDark(systemDark: systemColorScheme == .dark)
        TabView {
            ActivityView()
                .tabItem {
                    Label(tr("tab.activity"), systemImage: "figure.run.circle")
                }
            ProfileView()
                .tabItem {
                    Label(tr("tab.profile"), systemImage: "person.crop.circle")
                }
            SettingsView()
                .tabItem {
                    Label(tr("tab.settings"), systemImage: "gearshape")
                }
        }
        .environment(\.journal, appearance.colors(systemDark: systemColorScheme == .dark))
        .environment(\.journalCards, appearance.cards)
        .environment(\.journalTypography, appearance.typography)
        .environment(\.locale, Locale(identifier: appearance.localeOverride.isEmpty ? Locale.current.identifier : appearance.localeOverride))
        .font(.body)
        .tint(appearance.colors(systemDark: systemColorScheme == .dark).primary)
        .background(appearance.colors(systemDark: systemColorScheme == .dark).background.ignoresSafeArea())
        .preferredColorScheme(appearance.theme == "System" ? nil : (dark ? .dark : .light))
    }
}

#Preview {
    RootView().environmentObject(ActivityStore()).environmentObject(AppAppearance.shared)
}
