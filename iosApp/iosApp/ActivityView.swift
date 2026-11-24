import SwiftUI
import ActivityDomain

/// Activity screen: week grid, goal status, streak and manual entries.
/// Every weekly-rule decision is delegated to the shared Kotlin `ActivityDomain`
/// framework through `ActivityStore`; this file only renders and edits.
struct ActivityView: View {
    @EnvironmentObject private var store: ActivityStore
    @Environment(\.journal) private var journal
    @Environment(\.scenePhase) private var scenePhase
    @State private var showGoalEditor = false
    @State private var showEntryEditor = false
    @State private var showCollection = false
    @State private var selectedDayEpoch: Int64?
    @State private var selectedYear = Calendar.current.component(.year, from: Date())
    @State private var editingEntry: StoredWorkout?
    @State private var currentDate = Date()
    @State private var showAllEntries = false
    @State private var confirmPracticeSave = false
    @State private var confirmPracticeDiscard = false

    var body: some View {
        NavigationStack {
            ScrollView {
                let overview = store.overview(now: currentDate)
                let board = store.activityBoard(year: selectedYear, now: currentDate)

                VStack(alignment: .leading, spacing: 20) {
                    if let error = store.lastError {
                        Text(error)
                            .journalFont(.bodySmall)
                            .foregroundStyle(.red)
                            .accessibilityAddTraits(.isStaticText)
                    }

                    practiceCard
                    goalCard(overview)
                    rewardCard
                    weekGrid(overview)
                    heatmap(board)
                    statsRow
                    weeklyStreakCard(now: currentDate)
                    entriesSection
                }
                .padding()
            }
            .navigationTitle(tr("activity.title"))
            .toolbar {
                ToolbarItemGroup(placement: .topBarTrailing) {
                    Button(action: { showGoalEditor = true }) {
                        Image(systemName: "target").frame(minWidth: JournalSpace.touch, minHeight: JournalSpace.touch)
                    }
                    .accessibilityLabel(tr("activity.editGoal"))
                    Button(action: { showEntryEditor = true }) {
                        Image(systemName: "plus.circle").frame(minWidth: JournalSpace.touch, minHeight: JournalSpace.touch)
                    }
                    .accessibilityLabel(tr("activity.addEntry"))
                    Button { showCollection = true } label: { Image(systemName: "paintpalette").frame(minWidth: JournalSpace.touch, minHeight: JournalSpace.touch) }
                        .accessibilityLabel(tr("cosmetic.collection"))
                }
            }
            .task(id: store.practiceDraft?.isRunning) {
                while !Task.isCancelled {
                    currentDate = Date()
                    store.refreshActivity(now: currentDate)
                    let practiceRunning = store.practiceDraft?.isRunning == true
                    if practiceRunning { store.checkpointPractice() }
                    do { try await Task.sleep(nanoseconds: practiceRunning ? 1_000_000_000 : 30_000_000_000) }
                    catch { break }
                }
            }
            .onChange(of: scenePhase) { _, phase in
                if phase != .active { store.pausePractice() }
            }
            .confirmationDialog(tr("practice.saveConfirmTitle"), isPresented: $confirmPracticeSave, titleVisibility: .visible) {
                Button(tr("practice.save")) { _ = store.completePractice() }
                Button(tr("common.cancel"), role: .cancel) {}
            } message: {
                Text(tr("practice.saveConfirmMessage", durationText(store.practiceElapsedMillis)))
            }
            .confirmationDialog(tr("practice.discardConfirmTitle"), isPresented: $confirmPracticeDiscard, titleVisibility: .visible) {
                Button(tr("practice.discard"), role: .destructive) { store.discardPractice() }
                Button(tr("common.cancel"), role: .cancel) {}
            } message: {
                Text(tr("practice.discardConfirmMessage"))
            }
            .sheet(isPresented: $showGoalEditor) {
                let goal = store.overview(now: currentDate).goal
                GoalEditorView(sessions: Int(goal.sessions),
                               minutes: goal.minutes.map { Int(truncating: $0) }) { sessions, minutes in
                    store.saveGoal(sessions: sessions, minutes: minutes)
                    showGoalEditor = false
                }
            }
            .sheet(isPresented: $showEntryEditor) {
                EntryEditorView(allowedDays: store.allowedManualDays()) { minutes, day in
                    store.addManualEntry(minutes: minutes, day: day)
                    showEntryEditor = false
                }
            }
            .sheet(isPresented: $showCollection) {
                CosmeticCollectionView()
            }
            .sheet(item: $editingEntry) { entry in
                EntryEditorView(allowedDays: Array(Set(store.allowedManualDays() + [entry.epochDay])),
                                initialDay: entry.epochDay,
                                initialMinutes: Int(entry.durationMillis / 60_000)) { minutes, day in
                    store.updateManualEntry(id: entry.id, minutes: minutes, day: day)
                    editingEntry = nil
                }
            }
            .sheet(isPresented: Binding(get: { selectedDayEpoch != nil },
                                        set: { if !$0 { selectedDayEpoch = nil } })) {
                if let epoch = selectedDayEpoch {
                    let day = store.overview(now: currentDate).days.first { $0.epochDay == epoch }
                    let historical = store.activityBoard(year: selectedYear, now: currentDate).lazy.flatMap(\.cells).first { $0.epochDay == epoch }?.activity
                    DayDetailView(epochDay: epoch, day: day ?? historical)
                }
            }
        }
    }

    private var practiceCard: some View {
        VStack(alignment: .leading, spacing: JournalSpace.small) {
            Text(tr("practice.title")).journalFont(.titleLarge)
            Text(tr("practice.manualTimingNote"))
                .journalFont(.bodySmall)
                .foregroundStyle(.secondary)
            if let draft = store.practiceDraft {
                Text(durationText(store.practiceElapsedMillis))
                    .journalFont(.displaySmall).fontWeight(.semibold)
                    .monospacedDigit()
                    .accessibilityLabel(tr("practice.elapsed", durationText(store.practiceElapsedMillis)))
                HStack(spacing: JournalSpace.small) {
                    if draft.isRunning {
                        Button(tr("practice.pause")) { store.pausePractice() }
                            .buttonStyle(.bordered)
                    } else {
                        Button(tr("practice.resume")) { store.resumePractice() }
                            .buttonStyle(.borderedProminent)
                    }
                    Button(tr("practice.save")) { confirmPracticeSave = true }
                        .buttonStyle(.borderedProminent)
                    Button(tr("practice.discard"), role: .destructive) { confirmPracticeDiscard = true }
                        .buttonStyle(.bordered)
                }
                if !draft.isRunning { Text(tr("practice.interruptedDraft")).journalFont(.caption).foregroundStyle(.secondary) }
            } else {
                Button(tr("practice.start")) { store.startPractice() }
                    .buttonStyle(.borderedProminent)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .journalCard()
    }

    private func durationText(_ milliseconds: Int64) -> String {
        let totalSeconds = max(0, milliseconds / 1_000)
        return String(format: "%02lld:%02lld:%02lld", totalSeconds / 3_600,
                      (totalSeconds / 60) % 60, totalSeconds % 60)
    }

    private var rewardCard: some View {
        VStack(alignment: .leading, spacing: JournalSpace.small) {
            Text(tr("activity.cosmeticReward"))
                .journalFont(.titleLarge)
            if let reward = store.pendingReward {
                HStack(spacing: JournalSpace.medium) {
                    if let cosmetic = ActivityCosmetics.shared.all.first(where: { $0.id == reward.cosmeticId }) {
                        CosmeticSwatch(cosmetic: cosmetic)
                        VStack(alignment: .leading, spacing: JournalSpace.tiny) {
                            Text(tr("activity.rewardAvailable")).journalFont(.titleSmall).fontWeight(.semibold)
                            Text(cosmeticName(cosmetic)).journalFont(.bodyMedium)
                            Text(rarityName(cosmetic.rarity)).journalFont(.caption).foregroundStyle(.secondary)
                        }
                    }
                    Spacer(minLength: JournalSpace.small)
                    Button(tr("activity.claimReward")) { _ = store.claimReward(weekMonday: reward.weekMonday) }
                        .buttonStyle(.borderedProminent)
                        .disabled(reward.cosmeticId == nil)
                }
            } else if let reward = store.claimedReward,
                      let cosmetic = ActivityCosmetics.shared.all.first(where: { $0.id == reward.cosmeticId }) {
                Label(tr("activity.rewardClaimed"), systemImage: "checkmark.seal.fill")
                    .foregroundStyle(.tint)
                Text(cosmeticName(cosmetic)).journalFont(.titleSmall)
            } else {
                Label(tr("activity.rewardLocked"), systemImage: "lock")
                    .foregroundStyle(.secondary)
                Text(tr("activity.rewardLockedHint"))
                    .journalFont(.bodySmall)
                    .foregroundStyle(.secondary)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .journalCard()
    }

    private func heatmap(_ board: [ActivityBoardWeek]) -> some View {
        let accent = heatmapAccent
        return VStack(alignment: .leading, spacing: JournalSpace.small) {
            HStack {
                Button { selectedYear = max(1, selectedYear - 1) } label: {
                    Image(systemName: "chevron.left").frame(minWidth: JournalSpace.touch, minHeight: JournalSpace.touch)
                }
                .disabled(selectedYear <= 1)
                Spacer()
                Text("\(selectedYear)").journalFont(.titleLarge).monospacedDigit()
                Spacer()
                Button { selectedYear = min(Calendar.current.component(.year, from: currentDate), selectedYear + 1) } label: {
                    Image(systemName: "chevron.right").frame(minWidth: JournalSpace.touch, minHeight: JournalSpace.touch)
                }
                .disabled(selectedYear >= Calendar.current.component(.year, from: currentDate))
            }
            ScrollView(.horizontal) {
                HStack(alignment: .top, spacing: JournalSpace.tiny) {
                    ForEach(Array(board.enumerated()), id: \.offset) { item in
                        let week = item.element
                        VStack(spacing: 3) {
                            Text(week.monthLabel.map { monthName(Int(truncating: $0)) } ?? " ")
                                .journalFont(.bodySmall)
                                .frame(height: 12)
                            ForEach(week.cells, id: \.epochDay) { cell in
                                Button {
                                    if cell.inYear && !cell.future { selectedDayEpoch = cell.epochDay }
                                } label: {
                                    RoundedRectangle(cornerRadius: 2)
                                        .fill(heatmapColor(cell))
                                        .frame(width: 16, height: 16)
                                        .overlay(RoundedRectangle(cornerRadius: 2).stroke(accent ?? .clear, lineWidth: 1))
                                        .frame(width: JournalSpace.touch, height: JournalSpace.touch)
                                }
                                .buttonStyle(.plain)
                                .disabled(!cell.inYear || cell.future)
                                .accessibilityLabel(heatmapLabel(cell))
                            }
                        }
                    }
                }
            }
            .accessibilityElement(children: .contain)
            HStack(spacing: JournalSpace.medium) {
                heatmapLegend(JournalPalette.training, tr("day.training"))
                heatmapLegend(JournalPalette.dayOff, tr("day.dayOff"))
                heatmapLegend(JournalPalette.miss, tr("day.miss"))
            }
        }
        .journalCard()
    }

    private func heatmapColor(_ cell: ActivityBoardCell) -> Color {
        guard cell.inYear else { return .clear }
        guard !cell.future else { return .gray.opacity(0.12) }
        guard let day = cell.activity else { return .gray.opacity(0.22) }
        switch day.kind {
        case .training: return JournalPalette.training
        case .dayOff: return JournalPalette.dayOff
        case .miss: return JournalPalette.miss
        default: return .gray
        }
    }

    private var heatmapAccent: Color? {
        guard let id = store.loadout.first(where: { $0.category == "HEATMAP" })?.cosmeticId,
              let cosmetic = ActivityCosmetics.shared.all.first(where: { $0.id == id }) else { return nil }
        return Color(hex: UInt32(truncatingIfNeeded: cosmetic.color))
    }

    private func heatmapLabel(_ cell: ActivityBoardCell) -> String {
        let date = formattedDate(EpochDay.date(cell.epochDay))
        guard let day = cell.activity else { return date }
        return "\(date), \(day.workouts) \(tr("activity.sessionsUnit")), \(day.minutes) \(tr("activity.minutesUnit"))"
    }

    private func monthName(_ month: Int) -> String {
        var calendar = Calendar.current
        calendar.locale = appLocale()
        return calendar.monthSymbols[month - 1].uppercased(with: appLocale())
    }

    private func heatmapLegend(_ color: Color, _ label: String) -> some View {
        HStack(spacing: JournalSpace.tiny) {
            RoundedRectangle(cornerRadius: 2).fill(color).frame(width: 10, height: 10)
            Text(label).journalFont(.bodySmall).foregroundStyle(journal.onSurfaceVariant)
        }
        .accessibilityElement(children: .combine)
    }

    private func weeklyStreakCard(now: Date) -> some View {
        let streak = store.weeklyStreak(now: now)
        return VStack(alignment: .leading, spacing: JournalSpace.small) {
            Text(tr("activity.weeklyStreaks")).journalFont(.titleLarge)
            HStack(spacing: JournalSpace.small) {
                statTile(value: "\(streak.currentStreak)", label: tr("activity.currentWeeklyStreak"))
                statTile(value: "\(streak.longestStreak)", label: tr("activity.longestWeeklyStreak"))
                statTile(value: "\(streak.freezeBalance)/3", label: tr("activity.freezes"))
            }
        }
        .journalCard()
    }

    // MARK: Goal card

    private func goalCard(_ overview: ActivityOverview) -> some View {
        let goalMet = ActivityDomainKt.goalMet(totals: overview.totals, goal: overview.goal)
        VStack(alignment: .leading, spacing: 6) {
            HStack {
                Text(tr("activity.weekGoal"))
                    .journalFont(.titleLarge)
                Spacer()
                if goalMet {
                    Label(tr("activity.goalMet"), systemImage: "checkmark.circle.fill")
                        .journalFont(.titleSmall).fontWeight(.semibold)
                        .foregroundStyle(.green)
                        .accessibilityLabel(tr("activity.goalMet"))
                } else {
                    Label(tr("activity.goalPending"), systemImage: "circle.dashed")
                        .journalFont(.titleSmall)
                        .foregroundStyle(.secondary)
                        .accessibilityLabel(tr("activity.goalPending"))
                }
            }
            if overview.week.beta {
                Text(tr("activity.betaNote"))
                    .journalFont(.caption)
                    .foregroundStyle(.secondary)
            }
            let sessions = Int(overview.totals.sessions)
            let required = Int(overview.goal.sessions)
            Text("\(sessions)/\(required) " + tr("activity.sessionsUnit"))
                .journalFont(.title3).fontWeight(.semibold)
            ProgressView(value: min(1, Double(sessions) / Double(max(1, required))))
                .tint(graphAccent ?? journal.primary)
            if let minutesGoal = overview.goal.minutes.map({ Int(truncating: $0) }) {
                Text("\(overview.totals.minutes)/\(Int64(minutesGoal)) " + tr("activity.minutesUnit"))
                    .journalFont(.titleSmall)
                    .foregroundStyle(.secondary)
                ProgressView(value: min(1, Double(overview.totals.minutes) / Double(max(1, minutesGoal))))
                    .tint(graphAccent ?? journal.primary)
            } else {
                Text(tr("activity.noMinutesGoal"))
                    .journalFont(.titleSmall)
                    .foregroundStyle(.secondary)
            }
        }
        .journalCard()
        .accessibilityElement(children: .combine)
    }

    // MARK: Week grid (Mon–Sun)

    private func weekGrid(_ overview: ActivityOverview) -> some View {
        let days = overview.days
        let monday = overview.week.monday
        return VStack(alignment: .leading, spacing: 8) {
            Text(tr("activity.thisWeek"))
                .journalFont(.titleLarge)
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: JournalSpace.tiny) {
                    ForEach(0..<7, id: \.self) { offset in
                        let epoch = monday + Int64(offset)
                        let day = days.first { $0.epochDay == epoch }
                        Button {
                            if day != nil { selectedDayEpoch = epoch }
                        } label: {
                            VStack(spacing: 4) {
                                Text(weekdayLetter(epoch))
                                    .journalFont(.caption2)
                                    .foregroundStyle(.secondary)
                                Text("\(dayNumber(epoch))")
                                    .journalFont(.callout).fontWeight(.medium)
                                Circle()
                                    .fill(color(for: day?.kind))
                                    .frame(width: 10, height: 10)
                            }
                            .frame(width: 44)
                            .frame(minHeight: JournalSpace.touch)
                            .background(journal.surfaceVariant.opacity(0.55), in: RoundedRectangle(cornerRadius: JournalRadius.small))
                        }
                        .buttonStyle(.plain)
                        .disabled(day == nil)
                        .accessibilityLabel(accessibilityDayLabel(epoch, day))
                    }
                }
            }
        }
        .journalCard()
    }

    private func color(for kind: DayKind?) -> Color {
        guard let kind else { return journal.outlineVariant }
        switch kind {
        case .training: return JournalPalette.training
        case .dayOff: return JournalPalette.dayOff
        case .miss: return JournalPalette.miss
        default: return .gray
        }
    }

    private var graphAccent: Color? {
        guard let id = store.loadout.first(where: { $0.category == "GRAPH" })?.cosmeticId,
              let cosmetic = ActivityCosmetics.shared.all.first(where: { $0.id == id }) else { return nil }
        return Color(hex: UInt32(truncatingIfNeeded: cosmetic.color))
    }

    private func weekdayLetter(_ epoch: Int64) -> String {
        let index = EpochDay.mondayIndex(epoch) // 1 = Monday
        var calendar = Calendar.current
        calendar.locale = appLocale()
        let symbols = calendar.veryShortWeekdaySymbols
        // veryShortWeekdaySymbols starts at Sunday (index 0).
        return symbols[(index % 7)]
    }

    private func dayNumber(_ epoch: Int64) -> Int {
        let c = Calendar.current.dateComponents([.day], from: EpochDay.date(epoch))
        return c.day ?? 0
    }

    private func accessibilityDayLabel(_ epoch: Int64, _ day: DayActivity?) -> String {
        let date = EpochDay.date(epoch)
        let formatted = formattedDate(date)
        guard let day else { return formatted }
        let kind: String
        switch day.kind {
        case .training: kind = tr("day.training")
        case .dayOff: kind = tr("day.dayOff")
        case .miss: kind = tr("day.miss")
        default: kind = ""
        }
        return "\(formatted), \(kind), \(day.workouts) \(tr("activity.sessionsUnit"))"
    }

    // MARK: Stats + streak

    private var statsRow: some View {
        HStack(spacing: 12) {
            statTile(value: "\(store.allTimeWorkoutCount)", label: tr("activity.totalWorkouts"))
            statTile(value: "\(store.allTimeMinutes)", label: tr("activity.totalMinutes"))
            statTile(value: "\(store.dailyStreak(now: currentDate))", label: tr("activity.dailyStreak"))
        }
    }

    private func statTile(value: String, label: String) -> some View {
        VStack(spacing: 4) {
            Text(value)
                .journalFont(.title2).fontWeight(.semibold)
                .monospacedDigit()
            Text(label)
                .journalFont(.caption)
                .foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity)
        .journalCard()
        .accessibilityElement(children: .combine)
    }

    // MARK: Entries list

    private var entriesSection: some View {
        let history = store.workouts
            .sorted { ($0.epochDay, $0.sourceId) > ($1.epochDay, $1.sourceId) }
        let visible = showAllEntries ? history : Array(history.prefix(12))
        return VStack(alignment: .leading, spacing: 8) {
            Text(tr("activity.recentEntries"))
                .journalFont(.titleLarge)
            if history.isEmpty {
                Text(tr("activity.noEntries"))
                    .journalFont(.titleSmall)
                    .foregroundStyle(.secondary)
            } else {
                ForEach(visible) { entry in
                    HStack {
                        Text(formattedDate(EpochDay.date(entry.epochDay)))
                            .journalFont(.titleSmall)
                        Spacer()
                        Text("\(entry.durationMillis / 60_000) " + tr("activity.minutesUnit"))
                            .journalFont(.titleSmall)
                            .monospacedDigit()
                            .foregroundStyle(.secondary)
                        if entry.sourceRaw == "MANUAL_ENTRY" {
                            Button { editingEntry = entry } label: {
                                Image(systemName: "pencil")
                            }
                            .buttonStyle(.plain)
                            .accessibilityLabel(tr("entry.editTitle"))
                            Button {
                                store.deleteWorkout(id: entry.id)
                            } label: {
                                Image(systemName: "trash")
                                    .foregroundStyle(.red)
                            }
                            .buttonStyle(.plain)
                            .accessibilityLabel(tr("entry.delete"))
                        }
                    }
                    .padding(.vertical, 4)
                }
                if history.count > 12 {
                    Button(tr(showAllEntries ? "activity.showFewerEntries" : "activity.showAllEntries")) {
                        showAllEntries.toggle()
                    }
                    .frame(minHeight: JournalSpace.touch)
                }
            }
        }
        .journalCard()
    }
}

private func cosmeticName(_ cosmetic: ActivityCosmetic) -> String {
    (UserDefaults.standard.string(forKey: "appearance.locale") ?? Locale.current.language.languageCode?.identifier ?? "en") == "cs"
        ? cosmetic.nameCs : cosmetic.nameEn
}

private func rarityName(_ rarity: CosmeticRarity) -> String {
    switch rarity {
    case .common: tr("cosmetic.rarity.common")
    case .rare: tr("cosmetic.rarity.rare")
    case .epic: tr("cosmetic.rarity.epic")
    case .legendary: tr("cosmetic.rarity.legendary")
    default: ""
    }
}

private struct CosmeticSwatch: View {
    let cosmetic: ActivityCosmetic
    var body: some View {
        Text(cosmetic.category == .avatar ? "♫" : cosmetic.category == .frame ? "◉" : cosmetic.category == .background ? "▧" : cosmetic.category == .heatmap ? "▦" : "⌁")
            .journalFont(.title2)
            .foregroundStyle(Color(hex: UInt32(truncatingIfNeeded: cosmetic.color)))
            .frame(width: 52, height: 52)
            .background(Color(hex: UInt32(truncatingIfNeeded: cosmetic.color)).opacity(0.18), in: Circle())
            .accessibilityHidden(true)
    }
}

struct CosmeticCollectionView: View {
    @EnvironmentObject private var store: ActivityStore
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            List(ActivityCosmetics.shared.all, id: \.id) { cosmetic in
                let isOwned = store.ownedCosmetics.contains { $0.cosmeticId == cosmetic.id }
                let isEquipped = store.loadout.contains { $0.category == ActivityStore.categoryName(cosmetic.category) && $0.cosmeticId == cosmetic.id }
                HStack(spacing: JournalSpace.medium) {
                    CosmeticSwatch(cosmetic: cosmetic)
                    VStack(alignment: .leading, spacing: JournalSpace.tiny) {
                        Text(cosmeticName(cosmetic)).journalFont(.titleLarge)
                        Text(rarityName(cosmetic.rarity)).journalFont(.caption).foregroundStyle(.secondary)
                        Text(isOwned ? tr("cosmetic.owned") : tr("cosmetic.locked"))
                            .journalFont(.caption)
                            .foregroundStyle(.secondary)
                        if !isOwned { Text(tr("cosmetic.unlockRequirement")).journalFont(.caption2).foregroundStyle(.secondary) }
                    }
                    Spacer()
                    if isEquipped { Label(tr("cosmetic.equipped"), systemImage: "checkmark.circle.fill").labelStyle(.iconOnly).foregroundStyle(.tint) }
                    else if isOwned {
                        Button(tr("cosmetic.apply")) { store.equip(cosmetic) }
                            .buttonStyle(.bordered)
                    }
                }
                .padding(.vertical, JournalSpace.tiny)
                .listRowBackground(Color.clear)
            }
            .listStyle(.plain)
            .navigationTitle(tr("cosmetic.collection"))
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button(tr("common.close")) { dismiss() }
                }
            }
        }
    }
}

#Preview {
    ActivityView().environmentObject(ActivityStore())
}
