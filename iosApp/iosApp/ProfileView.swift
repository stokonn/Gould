import SwiftUI
import ActivityDomain

struct ProfileView: View {
    @EnvironmentObject private var store: ActivityStore
    @Environment(\.journal) private var journal
    @State private var showCollection = false

    private func equipped(_ category: CosmeticCategory) -> ActivityCosmetic? {
        guard let id = store.loadout.first(where: { $0.category == ActivityStore.categoryName(category) })?.cosmeticId else { return nil }
        return ActivityCosmetics.shared.all.first(where: { $0.id == id && $0.category == category })
    }

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: JournalSpace.medium) {
                    profileCard
                    HStack(spacing: JournalSpace.small) {
                        metric("\(store.allTimeWorkoutCount)", tr("activity.totalWorkouts"))
                        metric("\(store.allTimeMinutes)", tr("activity.totalMinutes"))
                        metric("\(store.dailyStreak())", tr("activity.dailyStreak"))
                    }
                    Text(tr("profile.cosmeticNote"))
                        .journalFont(.bodyMedium)
                        .foregroundStyle(journal.onSurfaceVariant)
                }
                .padding()
            }
            .navigationTitle(tr("tab.profile"))
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button { showCollection = true } label: {
                        Image(systemName: "paintpalette").frame(minWidth: JournalSpace.touch, minHeight: JournalSpace.touch)
                    }
                    .accessibilityLabel(tr("cosmetic.collection"))
                }
            }
            .sheet(isPresented: $showCollection) { CosmeticCollectionView() }
        }
    }

    private var profileCard: some View {
        VStack(spacing: JournalSpace.small) {
            ZStack {
                Circle()
                    .fill(color(equipped(.background))?.opacity(0.32) ?? journal.surfaceVariant)
                    .frame(width: 132, height: 132)
                Circle()
                    .stroke(color(equipped(.frame)) ?? journal.primary.opacity(0.5), lineWidth: 5)
                    .frame(width: 112, height: 112)
                Text(avatarGlyph)
                    .journalFont(.displayMedium).fontWeight(.semibold)
                    .foregroundStyle(color(equipped(.avatar)) ?? journal.primary)
                    .accessibilityHidden(true)
            }
            Text(tr("profile.title"))
                .journalFont(.title2)
            if let avatar = equipped(.avatar) { Text(cosmeticName(avatar)).journalFont(.bodyMedium) }
            if let frame = equipped(.frame) { Text(tr("profile.frame", cosmeticName(frame))).journalFont(.bodySmall).foregroundStyle(.secondary) }
            if let background = equipped(.background) { Text(tr("profile.background", cosmeticName(background))).journalFont(.bodySmall).foregroundStyle(.secondary) }
            Button(tr("cosmetic.collection")) { showCollection = true }
                .buttonStyle(.borderedProminent)
        }
        .frame(maxWidth: .infinity)
        .journalCard()
        .accessibilityElement(children: .contain)
    }

    private var avatarGlyph: String {
        guard let cosmetic = equipped(.avatar) else { return "♫" }
        return cosmetic.id.contains("golden") ? "𝄞" : "♫"
    }

    private func color(_ cosmetic: ActivityCosmetic?) -> Color? {
        guard let cosmetic else { return nil }
        return Color(hex: UInt32(truncatingIfNeeded: cosmetic.color))
    }

    private func metric(_ value: String, _ label: String) -> some View {
        VStack(spacing: JournalSpace.tiny) {
            Text(value).journalFont(.titleMedium).monospacedDigit()
            Text(label).journalFont(.labelSmall).foregroundStyle(journal.onSurfaceVariant).multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity, minHeight: 72)
        .journalCard()
        .accessibilityElement(children: .combine)
    }
}

private func cosmeticName(_ cosmetic: ActivityCosmetic) -> String {
    (UserDefaults.standard.string(forKey: "appearance.locale") ?? Locale.current.language.languageCode?.identifier ?? "en") == "cs"
        ? cosmetic.nameCs : cosmetic.nameEn
}

#Preview {
    ProfileView().environmentObject(ActivityStore())
}
