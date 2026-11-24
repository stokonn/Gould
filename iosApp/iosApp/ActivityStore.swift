import Foundation
import Combine
import ActivityDomain

// MARK: - Epoch-day helpers (platform side; rules live in ActivityDomain)

enum EpochDay {
    /// Civil date → days since 1970-01-01 (proleptic Gregorian), matching
    /// `LocalDate.toEpochDay()` used by the shared Kotlin rules.
    static func of(_ date: Date, calendar: Calendar = .current) -> Int64 {
        let c = calendar.dateComponents([.year, .month, .day], from: calendar.startOfDay(for: date))
        var y = Int64(c.year ?? 1970)
        let m = Int64(c.month ?? 1)
        let d = Int64(c.day ?? 1)
        if m <= 2 { y -= 1 }
        let era = (y >= 0 ? y : y - 399) / 400
        let yoe = y - era * 400
        let doy = (153 * (m + (m > 2 ? -3 : 9)) + 2) / 5 + d - 1
        let doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
        return era * 146097 + doe - 719468
    }

    /// Epoch day → the local-noon `Date` of that civil day (noon avoids
    /// day drift across every UTC offset).
    static func date(_ day: Int64, calendar: Calendar = .current) -> Date {
        var utc = Calendar(identifier: .gregorian)
        utc.timeZone = TimeZone(identifier: "UTC") ?? TimeZone(secondsFromGMT: 0)!
        let base = utc.date(from: DateComponents(year: 1970, month: 1, day: 1)) ?? Date(timeIntervalSince1970: 0)
        let civil = utc.date(byAdding: .day, value: Int(day), to: base) ?? base
        let c = utc.dateComponents([.year, .month, .day], from: civil)
        var local = calendar
        local.timeZone = TimeZone.current
        return local.date(from: DateComponents(year: c.year, month: c.month, day: c.day, hour: 12)) ?? civil
    }

    /// Mirrors the shared `dayOfWeek` convention: Monday = 1 … Sunday = 7.
    static func mondayIndex(_ day: Int64) -> Int {
        Int(((day + 3) % 7 + 7) % 7) + 1
    }
}

// MARK: - Persisted iOS-side records (JSON on disk; not shared with Android)

struct StoredWorkout: Codable, Identifiable, Equatable {
    let epochDay: Int64
    let durationMillis: Int64
    let sourceId: String
    let sourceRaw: String
    var id: String { sourceId }
}

struct StoredGoal: Codable, Equatable {
    let effectiveMonday: Int64
    let sessions: Int
    let minutes: Int?
}

private struct StorePayload: Codable {
    var workouts: [StoredWorkout] = []
    var goals: [StoredGoal] = []
}

// MARK: - Activity store

/// Owns iOS persistence and bridges to the shared Kotlin `ActivityDomain`
/// framework for every weekly-rule decision (goal, classification, streaks).
@MainActor
final class ActivityStore: ObservableObject {
    @Published private(set) var workouts: [StoredWorkout] = []
    @Published private(set) var goals: [StoredGoal] = []
    @Published var lastError: String?

    private let fileURL: URL

    init(filename: String = "gould-activity.json") {
        let base = FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask).first
            ?? FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
        try? FileManager.default.createDirectory(at: base, withIntermediateDirectories: true)
        fileURL = base.appendingPathComponent(filename)
        load()
    }

    // MARK: Loading / saving

    private func load() {
        guard let data = try? Data(contentsOf: fileURL),
              let payload = try? JSONDecoder().decode(StorePayload.self, from: data) else { return }
        // Drop records that would trip Kotlin `require` checks — never crash the app
        // on legacy or hand-edited data.
        workouts = payload.workouts.filter {
            $0.durationMillis > 0 && !$0.sourceId.trimmingCharacters(in: .whitespaces).isEmpty
        }
        goals = payload.goals.filter {
            EpochDay.mondayIndex($0.effectiveMonday) == 1 && $0.sessions >= 1 && $0.sessions <= 7
                && ($0.minutes == nil || $0.minutes! > 0)
        }
    }

    private func save() {
        let payload = StorePayload(workouts: workouts, goals: goals)
        guard let data = try? JSONEncoder().encode(payload) else { return }
        try? data.write(to: fileURL, options: .atomic)
    }

    // MARK: Kotlin bridge

    var todayEpochDay: Int64 { EpochDay.of(Date()) }
    var zoneId: String { TimeZone.current.identifier }

    private func kotlinGoals() -> [WeeklyGoal] {
        goals.sorted { $0.effectiveMonday < $1.effectiveMonday }.map {
            WeeklyGoal(effectiveMonday: $0.effectiveMonday,
                       sessions: Int32($0.sessions),
                       minutes: $0.minutes.map { KotlinInt(int: Int32($0)) })
        }
    }

    private func kotlinWorkouts() -> [Workout] {
        workouts.compactMap { w in
            guard let source = ActivityStore.source(from: w.sourceRaw) else { return nil }
            return Workout(epochDay: w.epochDay,
                           durationMillis: w.durationMillis,
                           sourceId: w.sourceId,
                           source: source)
        }
    }

    private static func source(from raw: String) -> WorkoutSource? {
        switch raw {
        case "PRACTICE_SESSION": return .practiceSession
        case "MANUAL_ENTRY": return .manualEntry
        default: return nil
        }
    }

    /// Current-week view computed exclusively by the shared Kotlin rules.
    func overview(now: Date = Date()) -> ActivityOverview {
        ActivityDomainKt.activityOverview(today: EpochDay.of(now),
                                          zoneId: zoneId,
                                          goals: kotlinGoals(),
                                          workouts: kotlinWorkouts(),
                                          beta: false)
    }

    /// Daily streak across every persisted source (shared rule).
    func dailyStreak(now: Date = Date()) -> Int {
        Int(ActivityDomainKt.currentActivityStreak(workouts: kotlinWorkouts(),
                                                   today: EpochDay.of(now)))
    }

    /// Manual entries are limited to today and the previous two calendar days.
    func allowedManualDays(now: Date = Date()) -> [Int64] {
        ActivityDomainKt.manualActivityDates(today: EpochDay.of(now)).map { $0.int64Value }
    }

    // MARK: Mutations (validated before touching Kotlin `require` checks)

    func addManualEntry(minutes: Int, day: Int64, now: Date = Date()) {
        guard minutes >= 1 else {
            lastError = tr("entry.invalidDuration")
            return
        }
        guard allowedManualDays(now: now).contains(day) else {
            lastError = tr("entry.invalidDate")
            return
        }
        let entry = StoredWorkout(epochDay: day,
                                  durationMillis: Int64(minutes) * 60_000,
                                  sourceId: "manual-" + UUID().uuidString,
                                  sourceRaw: "MANUAL_ENTRY")
        workouts.append(entry)
        save()
    }

    func deleteWorkout(id: String) {
        workouts.removeAll { $0.id == id }
        save()
    }

    /// Goal edits apply next Monday (shared `nextGoalMonday` rule).
    func saveGoal(sessions: Int, minutes: Int?, now: Date = Date()) {
        guard sessions >= 1 && sessions <= 7 else {
            lastError = tr("goal.invalidSessions")
            return
        }
        if let minutes, minutes <= 0 {
            lastError = tr("goal.invalidMinutes")
            return
        }
        let effective = ActivityDomainKt.nextGoalMonday(changeEpochDay: EpochDay.of(now))
        let goal = StoredGoal(effectiveMonday: effective, sessions: sessions, minutes: minutes)
        if let index = goals.firstIndex(where: { $0.effectiveMonday == effective }) {
            goals[index] = goal
        } else {
            goals.append(goal)
        }
        save()
    }

    func resetAllData() {
        workouts = []
        goals = []
        save()
    }

    func clearError() {
        lastError = nil
    }
}
