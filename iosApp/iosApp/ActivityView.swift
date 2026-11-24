import SwiftUI
import ActivityDomain

/// Activity screen: week grid, goal status, streak and manual entries.
/// Every weekly-rule decision is delegated to the shared Kotlin `ActivityDomain`
/// framework through `ActivityStore`; this file only renders and edits.
struct ActivityView: View {
    @EnvironmentObject private var store: ActivityStore
    @State private var showGoalEditor = false
    @State private var showEntryEditor = false
    @State private var selectedDayEpoch: Int64?

    var body: some View {
        NavigationStack {
            ScrollView {
                let overview = store.overview()

                VStack(alignment: .leading, spacing: 20) {
                    if let error = store.lastError {
                        Text(error)
                            .font(.footnote)
                            .foregroundStyle(.red)
                            .accessibilityAddTraits(.isStaticText)
                    }

                    goalCard(overview)
                    weekGrid(overview)
                    statsRow(overview)
                    streakCard(overview)
                    entriesSection(overview)
                }
                .padding()
            }
            .navigationTitle(tr("activity.title"))
            .toolbar {
                ToolbarItemGroup(placement: .topBarTrailing) {
                    Button(action: { showGoalEditor = true }) {
                        Image(systemName: "target")
                    }
                    .accessibilityLabel(tr("activity.editGoal"))
                    Button(action: { showEntryEditor = true }) {
                        Image(systemName: "plus.circle")
                    }
                    .accessibilityLabel(tr("activity.addEntry"))
                }
            }
            .sheet(isPresented: $showGoalEditor) {
                let goal = store.overview().goal
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
            .sheet(isPresented: Binding(get: { selectedDayEpoch != nil },
                                        set: { if !$0 { selectedDayEpoch = nil } })) {
                if let epoch = selectedDayEpoch {
                    let day = store.overview().days.first { $0.epochDay == epoch }
                    DayDetailView(epochDay: epoch, day: day)
                }
            }
        }
    }

    // MARK: Goal card

    private func goalCard(_ overview: ActivityOverview) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack {
                Text(tr("activity.weekGoal"))
                    .font(.headline)
                Spacer()
                if overview.goalMet {
                    Label(tr("activity.goalMet"), systemImage: "checkmark.circle.fill")
                        .font(.subheadline.weight(.semibold))
                        .foregroundStyle(.green)
                        .accessibilityLabel(tr("activity.goalMet"))
                } else {
                    Label(tr("activity.goalPending"), systemImage: "circle.dashed")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                        .accessibilityLabel(tr("activity.goalPending"))
                }
            }
            let sessions = Int(overview.totals.sessions)
            let required = Int(overview.goal.sessions)
            Text("\(sessions)/\(required) " + tr("activity.sessionsUnit"))
                .font(.title3.weight(.semibold))
            if let minutesGoal = overview.goal.minutes.map({ Int(truncating: $0) }) {
                Text("\(overview.totals.minutes)/\(Int64(minutesGoal)) " + tr("activity.minutesUnit"))
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            } else {
                Text(tr("activity.noMinutesGoal"))
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding()
        .background(.quaternary.opacity(0.4), in: RoundedRectangle(cornerRadius: 14))
        .accessibilityElement(children: .combine)
    }

    // MARK: Week grid (Mon–Sun)

    private func weekGrid(_ overview: ActivityOverview) -> some View {
        let days = overview.days
        let monday = overview.week.monday
        return VStack(alignment: .leading, spacing: 8) {
            Text(tr("activity.thisWeek"))
                .font(.headline)
            HStack(spacing: 6) {
                ForEach(0..<7, id: \.self) { offset in
                    let epoch = monday + Int64(offset)
                    let day = days.first { $0.epochDay == epoch }
                    Button {
                        if day != nil { selectedDayEpoch = epoch }
                    } label: {
                        VStack(spacing: 4) {
                            Text(weekdayLetter(epoch))
                                .font(.caption2)
                                .foregroundStyle(.secondary)
                            Text("\(dayNumber(epoch))")
                                .font(.callout.weight(.medium))
                            Circle()
                                .fill(color(for: day?.kind))
                                .frame(width: 10, height: 10)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 8)
                        .background(.quaternary.opacity(0.3), in: RoundedRectangle(cornerRadius: 10))
                    }
                    .buttonStyle(.plain)
                    .disabled(day == nil)
                    .accessibilityLabel(accessibilityDayLabel(epoch, day))
                }
            }
        }
    }

    private func color(for kind: DayKind?) -> Color {
        guard let kind else { return .gray.opacity(0.3) }
        switch kind {
        case .training: return .green
        case .dayOff: return .blue
        case .miss: return .red
        default: return .gray
        }
    }

    private func weekdayLetter(_ epoch: Int64) -> String {
        let index = EpochDay.mondayIndex(epoch) // 1 = Monday
        let symbols = Calendar.current.veryShortWeekdaySymbols
        // veryShortWeekdaySymbols starts at Sunday (index 0).
        return symbols[(index % 7)]
    }

    private func dayNumber(_ epoch: Int64) -> Int {
        let c = Calendar.current.dateComponents([.day], from: EpochDay.date(epoch))
        return c.day ?? 0
    }

    private func accessibilityDayLabel(_ epoch: Int64, _ day: DayActivity?) -> String {
        let date = EpochDay.date(epoch)
        let formatted = DateFormatter.localizedString(from: date, dateStyle: .medium, timeStyle: .none)
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

    private func statsRow(_ overview: ActivityOverview) -> some View {
        HStack(spacing: 12) {
            statTile(value: "\(overview.totals.sessions)", label: tr("activity.sessionsUnit"))
            statTile(value: "\(overview.totals.minutes)", label: tr("activity.minutesUnit"))
            statTile(value: "\(store.workouts.count)", label: tr("activity.allTimeEntries"))
        }
    }

    private func statTile(value: String, label: String) -> some View {
        VStack(spacing: 4) {
            Text(value)
                .font(.title2.weight(.semibold))
                .monospacedDigit()
            Text(label)
                .font(.caption)
                .foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 10)
        .background(.quaternary.opacity(0.3), in: RoundedRectangle(cornerRadius: 12))
        .accessibilityElement(children: .combine)
    }

    private func streakCard(_ overview: ActivityOverview) -> some View {
        HStack {
            Image(systemName: "flame.fill")
                .foregroundStyle(.orange)
            Text("\(tr("activity.dailyStreak")): \(store.dailyStreak())")
                .font(.subheadline)
            Spacer()
            Text(tr("activity.betaNote"))
                .font(.caption2)
                .foregroundStyle(.tertiary)
        }
        .padding()
        .background(.quaternary.opacity(0.3), in: RoundedRectangle(cornerRadius: 12))
        .accessibilityElement(children: .combine)
    }

    // MARK: Entries list

    private func entriesSection(_ overview: ActivityOverview) -> some View {
        let weekWorkouts = store.workouts
            .filter { $0.epochDay >= overview.week.monday && $0.epochDay <= overview.week.sunday }
            .sorted { ($0.epochDay, $0.sourceId) > ($1.epochDay, $1.sourceId) }
        return VStack(alignment: .leading, spacing: 8) {
            Text(tr("activity.weekEntries"))
                .font(.headline)
            if weekWorkouts.isEmpty {
                Text(tr("activity.noEntries"))
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            } else {
                ForEach(weekWorkouts) { entry in
                    HStack {
                        Text(DateFormatter.localizedString(from: EpochDay.date(entry.epochDay),
                                                           dateStyle: .medium, timeStyle: .none))
                            .font(.subheadline)
                        Spacer()
                        Text("\(entry.durationMillis / 60_000) " + tr("activity.minutesUnit"))
                            .font(.subheadline)
                            .monospacedDigit()
                            .foregroundStyle(.secondary)
                        if entry.sourceRaw == "MANUAL_ENTRY" {
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
            }
        }
    }
}

#Preview {
    ActivityView().environmentObject(ActivityStore())
}
