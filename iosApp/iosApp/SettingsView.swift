import SwiftUI

struct SettingsView: View {
    @EnvironmentObject private var store: ActivityStore
    @State private var confirmReset = false

    var body: some View {
        NavigationStack {
            List {
                Section(tr("settings.appearance")) {
                    Label(tr("settings.systemThemeNote"), systemImage: "circle.lefthalf.filled")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }
                Section(tr("settings.data")) {
                    LabeledContent(tr("settings.localEntries")) {
                        Text("\(store.workouts.count)").monospacedDigit()
                    }
                    LabeledContent(tr("settings.localGoals")) {
                        Text("\(store.goals.count)").monospacedDigit()
                    }
                    Button(tr("settings.reset"), role: .destructive) {
                        confirmReset = true
                    }
                    .accessibilityLabel(tr("settings.reset"))
                }
                Section(tr("settings.about")) {
                    LabeledContent(tr("settings.version")) {
                        Text(Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String ?? "-")
                    }
                    Text(tr("settings.sharedRulesNote"))
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                    Text("created by stokonn with love <3")
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                }
            }
            .navigationTitle(tr("tab.settings"))
            .confirmationDialog(tr("settings.resetConfirm"),
                                isPresented: $confirmReset, titleVisibility: .visible) {
                Button(tr("settings.reset"), role: .destructive) {
                    store.resetAllData()
                }
                Button(tr("common.cancel"), role: .cancel) {}
            }
        }
    }
}

#Preview {
    SettingsView().environmentObject(ActivityStore())
}
