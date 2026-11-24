import SwiftUI

/// Gould iOS application entry point.
/// Shared weekly activity rules come from the Kotlin `ActivityDomain` framework;
/// persistence and UI stay platform-side, mirroring the Android boundary.
@main
struct GouldApp: App {
    @StateObject private var store = ActivityStore()

    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(store)
        }
    }
}
