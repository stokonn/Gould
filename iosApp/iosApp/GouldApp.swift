import SwiftUI

/// Gould iOS application entry point.
/// Shared weekly activity rules come from the Kotlin `ActivityDomain` framework;
/// persistence and UI stay platform-side, mirroring the Android boundary.
@main
struct GouldApp: App {
    @StateObject private var store = ActivityStore()
    @StateObject private var appearance = AppAppearance.shared
    @StateObject private var reminders = ReminderSettings.shared

    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(store)
                .environmentObject(appearance)
                .environmentObject(reminders)
                .task { await reminders.restoreSchedule() }
        }
    }
}
