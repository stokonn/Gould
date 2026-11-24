import SwiftUI

struct RootView: View {
    var body: some View {
        TabView {
            ActivityView()
                .tabItem {
                    Label(tr("tab.activity"), systemImage: "figure.run.circle")
                }
            SettingsView()
                .tabItem {
                    Label(tr("tab.settings"), systemImage: "gearshape")
                }
        }
    }
}

#Preview {
    RootView().environmentObject(ActivityStore())
}
