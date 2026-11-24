import SwiftUI
import ActivityDomain

/// Weekly goal editor. Saving applies the shared rule: a change takes effect
/// next Monday, never in the current week (`nextGoalMonday`).
struct GoalEditorView: View {
    @Environment(\.dismiss) private var dismiss
    let sessions: Int
    let minutes: Int?
    let onSave: (Int, Int?) -> Void

    @State private var selectedSessions: Int
    @State private var useMinutesGoal: Bool
    @State private var selectedMinutes: Int

    init(sessions: Int, minutes: Int?, onSave: @escaping (Int, Int?) -> Void) {
        self.sessions = sessions
        self.minutes = minutes
        self.onSave = onSave
        _selectedSessions = State(initialValue: sessions)
        _useMinutesGoal = State(initialValue: minutes != nil)
        _selectedMinutes = State(initialValue: minutes ?? 30)
    }

    var body: some View {
        NavigationStack {
            Form {
                Section(tr("goal.sessionsSection")) {
                    Stepper(value: $selectedSessions, in: 1...7) {
                        Text("\(selectedSessions) " + tr("activity.sessionsUnit"))
                    }
                    .accessibilityLabel(tr("goal.sessionsSection"))
                }
                Section(tr("goal.minutesSection")) {
                    Toggle(tr("goal.useMinutesGoal"), isOn: $useMinutesGoal)
                    if useMinutesGoal {
                        Stepper(value: $selectedMinutes, in: 5...600, step: 5) {
                            Text("\(selectedMinutes) " + tr("activity.minutesUnit"))
                        }
                        .accessibilityLabel(tr("goal.minutesSection"))
                    }
                }
                Section {
                    Text(tr("goal.appliesNextMonday"))
                        .journalFont(.bodySmall)
                        .foregroundStyle(.secondary)
                }
            }
            .navigationTitle(tr("goal.title"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(tr("common.cancel")) { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button(tr("common.save")) {
                        onSave(selectedSessions, useMinutesGoal ? selectedMinutes : nil)
                    }
                }
            }
        }
    }
}

/// Manual activity entry editor. The shared rule allows only today and the
/// previous two calendar days; the picker range is restricted accordingly.
struct EntryEditorView: View {
    @Environment(\.dismiss) private var dismiss
    let allowedDays: [Int64]
    let initialDay: Int64?
    let onSave: (Int, Int64) -> Void

    @State private var minutes: Int
    @State private var selectedDay: Int64
    @State private var initialized: Bool

    init(allowedDays: [Int64], initialDay: Int64? = nil, initialMinutes: Int = 15,
         onSave: @escaping (Int, Int64) -> Void) {
        self.allowedDays = allowedDays
        self.initialDay = initialDay
        self.onSave = onSave
        _minutes = State(initialValue: initialMinutes)
        _selectedDay = State(initialValue: initialDay ?? 0)
        _initialized = State(initialValue: initialDay != nil)
    }

    private var sortedDays: [Int64] { allowedDays.sorted() }

    var body: some View {
        NavigationStack {
            Form {
                Section(tr("entry.dateSection")) {
                    Picker(tr("entry.dateSection"), selection: $selectedDay) {
                        ForEach(sortedDays, id: \.self) { day in
                            Text(formatted(day)).tag(day)
                        }
                    }
                    .pickerStyle(.segmented)
                }
                Section(tr("entry.durationSection")) {
                    Stepper(value: $minutes, in: 1...1_440, step: 5) {
                        Text("\(minutes) " + tr("activity.minutesUnit"))
                    }
                    .accessibilityLabel(tr("entry.durationSection"))
                }
                Section {
                    Text(tr("entry.manualOnlyNote"))
                        .journalFont(.bodySmall)
                        .foregroundStyle(.secondary)
                }
            }
            .navigationTitle(tr(initialDay == nil ? "entry.title" : "entry.editTitle"))
            .navigationBarTitleDisplayMode(.inline)
            .onAppear {
                if !initialized {
                    selectedDay = sortedDays.last ?? 0
                    initialized = true
                }
            }
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(tr("common.cancel")) { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button(tr("common.save")) {
                        onSave(minutes, selectedDay)
                    }
                    .disabled(sortedDays.isEmpty)
                }
            }
        }
    }

    private func formatted(_ day: Int64) -> String {
        let date = EpochDay.date(day)
        let isToday = day == EpochDay.of(Date())
        if isToday { return tr("entry.today") }
        return formattedDate(date)
    }
}

/// Detail sheet for one day of the current week.
struct DayDetailView: View {
    @Environment(\.dismiss) private var dismiss
    let epochDay: Int64
    let day: DayActivity?

    var body: some View {
        NavigationStack {
            List {
                Section {
                    LabeledContent(tr("entry.dateSection")) {
                        Text(formattedDate(EpochDay.date(epochDay), dateStyle: .full))
                    }
                    if let day {
                        LabeledContent(tr("activity.sessionsUnit")) {
                            Text("\(day.workouts)").monospacedDigit()
                        }
                        LabeledContent(tr("activity.minutesUnit")) {
                            Text("\(day.minutes)").monospacedDigit()
                        }
                        LabeledContent(tr("day.kind")) {
                            Text(kindLabel(day.kind))
                        }
                    }
                }
            }
            .navigationTitle(tr("day.title"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button(tr("common.close")) { dismiss() }
                }
            }
        }
    }

    private func kindLabel(_ kind: DayKind) -> String {
        switch kind {
        case .training: return tr("day.training")
        case .dayOff: return tr("day.dayOff")
        case .miss: return tr("day.miss")
        default: return ""
        }
    }
}
