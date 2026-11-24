import SwiftUI
import UniformTypeIdentifiers
import UIKit

struct SettingsView: View {
    @EnvironmentObject private var store: ActivityStore
    @EnvironmentObject private var appearance: AppAppearance
    @EnvironmentObject private var reminders: ReminderSettings
    @Environment(\.journal) private var colors
    @State private var confirmReset = false
    @State private var confirmAppearanceReset = false
    @State private var showFontLicenses = false
    @State private var showBackupExporter = false
    @State private var showCSVExporter = false
    @State private var showBackupImporter = false
    @State private var pendingBackup: Data?
    @State private var exportDocument: ActivityFileDocument?

    var body: some View {
        NavigationStack {
            List {
                appearanceSection
                dataSection
                privacySection
                aboutSection
            }
            .scrollContentBackground(.hidden)
            .background(colors.background)
            .navigationTitle(tr("tab.settings"))
            .confirmationDialog(tr("settings.resetConfirm"), isPresented: $confirmReset, titleVisibility: .visible) {
                Button(tr("settings.reset"), role: .destructive) { store.resetAllData() }
                Button(tr("common.cancel"), role: .cancel) {}
            } message: {
                Text(tr("settings.resetMessage"))
            }
            .confirmationDialog(tr("settings.resetAppearanceConfirm"), isPresented: $confirmAppearanceReset, titleVisibility: .visible) {
                Button(tr("settings.resetAppearance"), role: .destructive) { appearance.reset() }
                Button(tr("common.cancel"), role: .cancel) {}
            }
            .fileExporter(isPresented: $showBackupExporter, document: exportDocument, contentType: .json,
                          defaultFilename: "Gould-activity-backup") { result in
                if case .failure = result { store.lastError = tr("settings.exportFailed") }
                exportDocument = nil
            }
            .fileExporter(isPresented: $showCSVExporter, document: exportDocument, contentType: .commaSeparatedText,
                          defaultFilename: "Gould-activity") { result in
                if case .failure = result { store.lastError = tr("settings.exportFailed") }
                exportDocument = nil
            }
            .fileImporter(isPresented: $showBackupImporter, allowedContentTypes: [.json]) { result in
                do {
                    guard let url = try result.get().first else { return }
                    let accessed = url.startAccessingSecurityScopedResource()
                    defer { if accessed { url.stopAccessingSecurityScopedResource() } }
                    pendingBackup = try Data(contentsOf: url)
                } catch { store.lastError = tr("settings.importFailed") }
            }
            .alert(tr("settings.importConfirm"), isPresented: Binding(
                get: { pendingBackup != nil },
                set: { if !$0 { pendingBackup = nil } }
            )) {
                Button(tr("settings.importBackup"), role: .destructive) {
                    if let pendingBackup { _ = store.restoreBackup(pendingBackup) }
                    pendingBackup = nil
                }
                Button(tr("common.cancel"), role: .cancel) { pendingBackup = nil }
            } message: {
                Text(tr("settings.importMessage"))
            }
            .sheet(isPresented: $showFontLicenses) {
                NavigationStack {
                    ScrollView { Text(fontLicenses).font(.system(.footnote, design: .monospaced)).textSelection(.enabled).padding() }
                        .navigationTitle(tr("settings.fontLicenses"))
                        .toolbar { ToolbarItem(placement: .confirmationAction) { Button(tr("common.close")) { showFontLicenses = false } } }
                }
            }
        }
    }

    private var appearanceSection: some View {
        Section(tr("settings.appearance")) {
            Picker(tr("settings.theme"), selection: $appearance.theme) {
                Text(tr("settings.followSystem")).tag("System")
                Text(tr("settings.light")).tag("Light")
                Text(tr("settings.dark")).tag("Dark")
            }
            Picker(tr("settings.typography"), selection: $appearance.typography) {
                Text(tr("settings.editorial")).tag("Editorial")
                Text(tr("settings.systemFont")).tag("System")
            }
            Picker(tr("settings.accent"), selection: $appearance.accent) {
                ForEach(AppAppearance.accents, id: \.self) { Text(tr("settings.accent.\($0.lowercased())")).tag($0) }
            }
            Picker(tr("settings.cards"), selection: $appearance.cards) {
                Text(tr("settings.soft")).tag("Soft")
                Text(tr("settings.outline")).tag("Outline")
                Text(tr("settings.open")).tag("Open")
            }
            Picker(tr("settings.language"), selection: $appearance.localeOverride) {
                Text(tr("settings.followSystem")).tag("")
                Text(tr("settings.czech")).tag("cs")
                Text(tr("settings.english")).tag("en")
            }
            Button(tr("settings.resetAppearance"), role: .destructive) { confirmAppearanceReset = true }
        }
    }

    private var dataSection: some View {
        Section(tr("settings.data")) {
            LabeledContent(tr("settings.localEntries")) { Text("\(store.workouts.count)").monospacedDigit() }
            LabeledContent(tr("settings.localGoals")) { Text("\(store.goals.count)").monospacedDigit() }
            LabeledContent(tr("settings.ownedCosmetics")) { Text("\(store.ownedCosmetics.count)").monospacedDigit() }
            Button(tr("settings.exportBackup")) {
                guard let data = store.backupData() else { store.lastError = tr("activity.loadFailed"); return }
                exportDocument = ActivityFileDocument(data: data)
                showBackupExporter = true
            }
            Button(tr("settings.importBackup")) { showBackupImporter = true }
            Button(tr("settings.exportCSV")) {
                exportDocument = ActivityFileDocument(data: store.activityCSV())
                showCSVExporter = true
            }
            Button(tr("settings.reset"), role: .destructive) { confirmReset = true }
        }
    }

    private var privacySection: some View {
        Section(tr("settings.privacy")) {
            Toggle(tr("settings.reminders"), isOn: Binding(
                get: { reminders.enabled },
                set: { enabled in Task { await reminders.update(enabled: enabled) } }
            ))
            if reminders.enabled {
                DatePicker(tr("settings.reminderTime"), selection: reminderTime, displayedComponents: .hourAndMinute)
            }
            if let error = reminders.error {
                Text(error).journalFont(.bodySmall).foregroundStyle(.red)
            }
            Text(tr("settings.privacyNote"))
                .journalFont(.bodySmall)
                .foregroundStyle(.secondary)
            Text(tr("settings.platformLimitations"))
                .journalFont(.bodySmall)
                .foregroundStyle(.secondary)
            Button(tr("settings.iosSettings")) {
                guard let url = URL(string: UIApplication.openSettingsURLString) else { return }
                UIApplication.shared.open(url)
            }
        }
    }

    private var aboutSection: some View {
        Section(tr("settings.about")) {
            LabeledContent(tr("settings.version")) {
                Text(Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String ?? "-")
            }
            Text(tr("settings.sharedRulesNote"))
                .journalFont(.bodySmall)
                .foregroundStyle(.secondary)
            Button(tr("settings.fontLicenses")) { showFontLicenses = true }
            Text("created by stokonn with love <3")
                .journalFont(.bodySmall)
                .foregroundStyle(.secondary)
        }
    }

    private var fontLicenses: String {
        ["CormorantGaramond-OFL", "Inter-OFL"].compactMap { name in
            guard let url = Bundle.main.url(forResource: name, withExtension: "txt", subdirectory: "Licenses")
                    ?? Bundle.main.url(forResource: name, withExtension: "txt") else { return nil }
            return try? String(contentsOf: url, encoding: .utf8)
        }.joined(separator: "\n\n")
    }

    private var reminderTime: Binding<Date> {
        Binding(get: {
            Calendar.current.date(from: DateComponents(hour: reminders.hour, minute: reminders.minute)) ?? Date()
        }, set: { value in
            let time = Calendar.current.dateComponents([.hour, .minute], from: value)
            Task { await reminders.update(enabled: true, hour: time.hour, minute: time.minute) }
        })
    }
}

#Preview {
    SettingsView().environmentObject(ActivityStore()).environmentObject(AppAppearance.shared).environmentObject(ReminderSettings.shared)
}
