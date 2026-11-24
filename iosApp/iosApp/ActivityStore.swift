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
    let startedAtMillis: Int64?
    let completedAtMillis: Int64?

    init(epochDay: Int64, durationMillis: Int64, sourceId: String, sourceRaw: String,
         startedAtMillis: Int64? = nil, completedAtMillis: Int64? = nil) {
        self.epochDay = epochDay
        self.durationMillis = durationMillis
        self.sourceId = sourceId
        self.sourceRaw = sourceRaw
        self.startedAtMillis = startedAtMillis
        self.completedAtMillis = completedAtMillis
    }
    var id: String { sourceId }
}

struct StoredGoal: Codable, Equatable {
    let effectiveMonday: Int64
    let sessions: Int
    let minutes: Int?
}

struct StoredWeek: Codable, Equatable {
    let monday: Int64
    let zoneId: String
    let beta: Bool
}

struct StoredRewardGrant: Codable, Identifiable, Equatable {
    let weekMonday: Int64
    let cosmeticId: String?
    let result: String
    let grantedAt: Int64
    var acknowledgedAt: Int64
    var id: Int64 { weekMonday }
}

struct StoredCosmeticOwnership: Codable, Identifiable, Equatable {
    let cosmeticId: String
    let earnedAt: Int64
    let sourceWeekMonday: Int64?
    var id: String { cosmeticId }
}

struct StoredCosmeticLoadout: Codable, Identifiable, Equatable {
    let category: String
    let cosmeticId: String
    var id: String { category }
}

struct StoredPracticeDraft: Codable, Equatable {
    let token: String
    let startedAtMillis: Int64
    var elapsedMillis: Int64
    var isRunning: Bool
}

private struct StorePayload: Codable {
    var firstUseDay: Int64?
    var workouts: [StoredWorkout]
    var goals: [StoredGoal]
    var weeks: [StoredWeek]
    var rewards: [StoredRewardGrant]
    var ownedCosmetics: [StoredCosmeticOwnership]
    var loadout: [StoredCosmeticLoadout]
    var practiceDraft: StoredPracticeDraft?

    init(firstUseDay: Int64?, workouts: [StoredWorkout], goals: [StoredGoal], weeks: [StoredWeek],
         rewards: [StoredRewardGrant], ownedCosmetics: [StoredCosmeticOwnership], loadout: [StoredCosmeticLoadout],
         practiceDraft: StoredPracticeDraft? = nil) {
        self.firstUseDay = firstUseDay
        self.workouts = workouts
        self.goals = goals
        self.weeks = weeks
        self.rewards = rewards
        self.ownedCosmetics = ownedCosmetics
        self.loadout = loadout
        self.practiceDraft = practiceDraft
    }

    private enum CodingKeys: String, CodingKey {
        case firstUseDay, workouts, goals, weeks, rewards, ownedCosmetics, loadout, practiceDraft
    }

    init(from decoder: Decoder) throws {
        let values = try decoder.container(keyedBy: CodingKeys.self)
        firstUseDay = try values.decodeIfPresent(Int64.self, forKey: .firstUseDay)
        // These two keys existed in the original file format; a file missing them is
        // incomplete data, not a legacy version that can safely migrate to empty.
        workouts = try values.decode([StoredWorkout].self, forKey: .workouts)
        goals = try values.decode([StoredGoal].self, forKey: .goals)
        weeks = try values.decodeIfPresent([StoredWeek].self, forKey: .weeks) ?? []
        rewards = try values.decodeIfPresent([StoredRewardGrant].self, forKey: .rewards) ?? []
        ownedCosmetics = try values.decodeIfPresent([StoredCosmeticOwnership].self, forKey: .ownedCosmetics) ?? []
        loadout = try values.decodeIfPresent([StoredCosmeticLoadout].self, forKey: .loadout) ?? []
        practiceDraft = try values.decodeIfPresent(StoredPracticeDraft.self, forKey: .practiceDraft)
    }
}

private enum ActivityStoreError: Error { case unreadableExistingFile }

// MARK: - Activity store

/// Owns iOS persistence and bridges to the shared Kotlin `ActivityDomain`
/// framework for every weekly-rule decision (goal, classification, streaks).
@MainActor
final class ActivityStore: ObservableObject {
    @Published private(set) var workouts: [StoredWorkout] = []
    @Published private(set) var goals: [StoredGoal] = []
    @Published private(set) var weeks: [StoredWeek] = []
    @Published private(set) var rewardGrants: [StoredRewardGrant] = []
    @Published private(set) var ownedCosmetics: [StoredCosmeticOwnership] = []
    @Published private(set) var loadout: [StoredCosmeticLoadout] = []
    @Published private(set) var practiceDraft: StoredPracticeDraft?
    @Published var lastError: String?

    private let fileURL: URL
    private var firstUseDay = EpochDay.of(Date())
    private var unreadableExistingFile = false
    private var practiceSegmentStartedAt: TimeInterval?

    init(filename: String = "gould-activity.json") {
        let base = FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask).first
            ?? FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
        try? FileManager.default.createDirectory(at: base, withIntermediateDirectories: true)
        fileURL = base.appendingPathComponent(filename)
        load()
        if !unreadableExistingFile {
            if let draft = practiceDraft, draft.isRunning {
                let paused = StoredPracticeDraft(token: draft.token, startedAtMillis: draft.startedAtMillis,
                                                 elapsedMillis: draft.elapsedMillis, isRunning: false)
                practiceDraft = paused
                do { try persist(payload(practiceDraft: .some(paused))) }
                catch { lastError = tr("activity.saveFailed") }
            }
            refreshActivity()
        }
    }

    // MARK: Loading / saving

    private func load() {
        guard FileManager.default.fileExists(atPath: fileURL.path) else { return }
        guard let data = try? Data(contentsOf: fileURL),
              let payload = try? JSONDecoder().decode(StorePayload.self, from: data) else {
            unreadableExistingFile = true
            lastError = tr("activity.loadFailed")
            return
        }
        guard Self.isValid(payload) else {
            unreadableExistingFile = true
            lastError = tr("activity.loadFailed")
            return
        }
        workouts = payload.workouts
        goals = payload.goals
        weeks = payload.weeks
        rewardGrants = payload.rewards
        ownedCosmetics = payload.ownedCosmetics
        loadout = payload.loadout
        practiceDraft = payload.practiceDraft
        // Legacy iOS activity files have no reward-era activation date. Start
        // reward eligibility at this migration, never retroactively from entries.
        firstUseDay = payload.firstUseDay ?? EpochDay.of(Date())
    }

    private func payload(workouts: [StoredWorkout]? = nil, goals: [StoredGoal]? = nil,
                         weeks: [StoredWeek]? = nil, rewards: [StoredRewardGrant]? = nil,
                         ownedCosmetics: [StoredCosmeticOwnership]? = nil,
                         loadout: [StoredCosmeticLoadout]? = nil,
                         practiceDraft: StoredPracticeDraft?? = nil) -> StorePayload {
        StorePayload(firstUseDay: firstUseDay, workouts: workouts ?? self.workouts, goals: goals ?? self.goals,
                     weeks: weeks ?? self.weeks, rewards: rewards ?? rewardGrants,
                     ownedCosmetics: ownedCosmetics ?? self.ownedCosmetics, loadout: loadout ?? self.loadout,
                     practiceDraft: practiceDraft ?? self.practiceDraft)
    }

    private func persist(_ payload: StorePayload) throws {
        guard !unreadableExistingFile else { throw ActivityStoreError.unreadableExistingFile }
        let data = try JSONEncoder().encode(payload)
        try data.write(to: fileURL, options: .atomic)
    }

    var pendingReward: StoredRewardGrant? {
        rewardGrants.sorted { $0.weekMonday < $1.weekMonday }.first { reward in
            ActivityRewards.shared.claimable(cosmeticId: reward.cosmeticId, result: reward.result,
                                             acknowledgedAt: reward.acknowledgedAt)
                && !ownedCosmetics.contains { $0.cosmeticId == reward.cosmeticId && $0.sourceWeekMonday == reward.weekMonday }
        }
    }

    var claimedReward: StoredRewardGrant? {
        rewardGrants.filter { reward in
            reward.result == ActivityRewardsKt.REWARD_RESULT_COSMETIC
                && (reward.acknowledgedAt > 0 || ownedCosmetics.contains {
                    $0.cosmeticId == reward.cosmeticId && $0.sourceWeekMonday == reward.weekMonday
                })
        }
            .max { $0.weekMonday < $1.weekMonday }
    }

    var allTimeWorkoutCount: Int { kotlinWorkouts().count }
    var allTimeMinutes: Int64 {
        kotlinWorkouts().reduce(Int64(0)) { ActivityDomainKt.saturatingAdd(left: $0, right: $1.durationMillis) } / 60_000
    }

    // MARK: Kotlin bridge

    var todayEpochDay: Int64 { EpochDay.of(Date()) }
    var zoneId: String { TimeZone.current.identifier }

    var practiceElapsedMillis: Int64 {
        guard let draft = practiceDraft else { return 0 }
        let live = practiceSegmentStartedAt.map { Int64(max(0, ProcessInfo.processInfo.systemUptime - $0) * 1_000) } ?? 0
        return ActivityDomainKt.saturatingAdd(left: draft.elapsedMillis, right: live)
    }

    func startPractice(now: Date = Date()) {
        guard practiceDraft == nil else { return }
        let draft = StoredPracticeDraft(token: UUID().uuidString, startedAtMillis: Int64(now.timeIntervalSince1970 * 1_000),
                                        elapsedMillis: 0, isRunning: true)
        do {
            try persist(payload(practiceDraft: .some(draft)))
            practiceDraft = draft
            practiceSegmentStartedAt = ProcessInfo.processInfo.systemUptime
            lastError = nil
        } catch { lastError = tr("activity.saveFailed") }
    }

    func resumePractice() {
        guard let draft = practiceDraft, !draft.isRunning else { return }
        let running = StoredPracticeDraft(token: draft.token, startedAtMillis: draft.startedAtMillis,
                                          elapsedMillis: draft.elapsedMillis, isRunning: true)
        do {
            try persist(payload(practiceDraft: .some(running)))
            practiceDraft = running
            practiceSegmentStartedAt = ProcessInfo.processInfo.systemUptime
            lastError = nil
        } catch { lastError = tr("activity.saveFailed") }
    }

    func pausePractice() {
        guard let draft = practiceDraft, draft.isRunning else { return }
        let paused = StoredPracticeDraft(token: draft.token, startedAtMillis: draft.startedAtMillis,
                                         elapsedMillis: practiceElapsedMillis, isRunning: false)
        do {
            try persist(payload(practiceDraft: .some(paused)))
            practiceDraft = paused
            practiceSegmentStartedAt = nil
            lastError = nil
        } catch { lastError = tr("activity.saveFailed") }
    }

    func checkpointPractice() {
        guard let draft = practiceDraft, draft.isRunning else { return }
        let now = ProcessInfo.processInfo.systemUptime
        let live = Int64(max(0, now - (practiceSegmentStartedAt ?? now)) * 1_000)
        let checkpoint = StoredPracticeDraft(token: draft.token, startedAtMillis: draft.startedAtMillis,
                                             elapsedMillis: ActivityDomainKt.saturatingAdd(left: draft.elapsedMillis, right: live),
                                             isRunning: true)
        do {
            try persist(payload(practiceDraft: .some(checkpoint)))
            practiceDraft = checkpoint
            practiceSegmentStartedAt = now
            lastError = nil
        } catch { lastError = tr("activity.saveFailed") }
    }

    @discardableResult
    func completePractice(now: Date = Date()) -> Bool {
        guard let draft = practiceDraft else { return false }
        let duration = practiceElapsedMillis
        guard duration >= 1_000 else { lastError = tr("practice.minimumDuration"); return false }
        let workout = StoredWorkout(epochDay: EpochDay.of(now), durationMillis: duration,
                                    sourceId: "practice:" + draft.token, sourceRaw: "PRACTICE_SESSION",
                                    startedAtMillis: draft.startedAtMillis,
                                    completedAtMillis: Int64(now.timeIntervalSince1970 * 1_000))
        guard !workouts.contains(where: { $0.sourceId == workout.sourceId }) else {
            lastError = tr("activity.loadFailed")
            return false
        }
        var next = workouts
        next.append(workout)
        do {
            try persist(payload(workouts: next, practiceDraft: .some(nil)))
            workouts = next
            practiceDraft = nil
            practiceSegmentStartedAt = nil
            lastError = nil
            refreshActivity(now: now)
            return true
        } catch { lastError = tr("activity.saveFailed"); return false }
    }

    func discardPractice() {
        guard practiceDraft != nil else { return }
        do {
            try persist(payload(practiceDraft: .some(nil)))
            practiceDraft = nil
            practiceSegmentStartedAt = nil
            lastError = nil
        } catch { lastError = tr("activity.saveFailed") }
    }

    func backupData() -> Data? {
        guard !unreadableExistingFile else { return nil }
        return try? JSONEncoder().encode(payload())
    }

    func activityCSV() -> Data {
        let rows = ["epoch_day,duration_millis,source,id,started_at_ms,completed_at_ms"] + workouts.sorted {
            ($0.epochDay, $0.sourceId) < ($1.epochDay, $1.sourceId)
        }.map { workout in
            return [String(workout.epochDay), String(workout.durationMillis), workout.sourceRaw, workout.sourceId,
                    workout.startedAtMillis.map { String($0) } ?? "",
                    workout.completedAtMillis.map { String($0) } ?? ""]
                .map(Self.csvField).joined(separator: ",")
        }
        return Data(rows.joined(separator: "\r\n").utf8)
    }

    @discardableResult
    func restoreBackup(_ data: Data) -> Bool {
        guard data.count <= 64 * 1_024 * 1_024,
              var restored = try? JSONDecoder().decode(StorePayload.self, from: data),
              Self.isValid(restored) else {
            lastError = tr("activity.loadFailed")
            return false
        }
        restored.firstUseDay = restored.firstUseDay ?? EpochDay.of(Date())
        if let draft = restored.practiceDraft, draft.isRunning {
            restored.practiceDraft = StoredPracticeDraft(token: draft.token, startedAtMillis: draft.startedAtMillis,
                                                         elapsedMillis: draft.elapsedMillis, isRunning: false)
        }
        do {
            let encoded = try JSONEncoder().encode(restored)
            try encoded.write(to: fileURL, options: .atomic)
            unreadableExistingFile = false
            firstUseDay = restored.firstUseDay ?? EpochDay.of(Date())
            workouts = restored.workouts
            goals = restored.goals
            weeks = restored.weeks
            rewardGrants = restored.rewards
            ownedCosmetics = restored.ownedCosmetics
            loadout = restored.loadout
            practiceDraft = restored.practiceDraft
            practiceSegmentStartedAt = nil
            lastError = nil
            refreshActivity()
            return true
        } catch {
            lastError = tr("activity.saveFailed")
            return false
        }
    }

    private static func isValid(_ payload: StorePayload) -> Bool {
        let minDay: Int64 = -719_162 // 0001-01-01
        let maxDay: Int64 = 2_932_896 // 9999-12-31
        func validDay(_ day: Int64) -> Bool { day >= minDay && day <= maxDay }
        func monday(_ day: Int64) -> Bool { validDay(day) && EpochDay.mondayIndex(day) == 1 }
        let cosmetics = Dictionary(uniqueKeysWithValues: ActivityCosmetics.shared.all.map { ($0.id, $0) })
        guard payload.firstUseDay.map(validDay) ?? true else { return false }
        guard payload.workouts.allSatisfy({ workout in
            validDay(workout.epochDay) && workout.durationMillis > 0
                && (workout.sourceRaw == "MANUAL_ENTRY" || (workout.sourceRaw == "PRACTICE_SESSION" && workout.durationMillis >= 1_000))
                && !workout.sourceId.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
                && ((workout.startedAtMillis == nil && workout.completedAtMillis == nil)
                    || (workout.startedAtMillis.map { $0 > 0 } == true
                        && workout.completedAtMillis.map { $0 > 0 } == true
                        && workout.completedAtMillis! >= workout.startedAtMillis!))
        }), Set(payload.workouts.map(\.sourceId)).count == payload.workouts.count else { return false }
        guard payload.goals.allSatisfy({ monday($0.effectiveMonday) && (1...7).contains($0.sessions)
            && ($0.minutes.map { $0 > 0 && $0 <= Int(Int32.max) } ?? true) }),
              Set(payload.goals.map(\.effectiveMonday)).count == payload.goals.count else { return false }
        guard payload.weeks.allSatisfy({ monday($0.monday) && TimeZone(identifier: $0.zoneId) != nil }),
              Set(payload.weeks.map(\.monday)).count == payload.weeks.count else { return false }
        guard payload.rewards.allSatisfy({ reward in
            monday(reward.weekMonday) && reward.grantedAt > 0 && reward.acknowledgedAt >= 0
                && ((reward.result == ActivityRewardsKt.REWARD_RESULT_COSMETIC
                    && reward.cosmeticId.flatMap { cosmetics[$0] } != nil)
                    || (reward.result == ActivityRewardsKt.REWARD_RESULT_COLLECTION_COMPLETE
                        && reward.cosmeticId == nil && reward.acknowledgedAt > 0))
        }), Set(payload.rewards.map(\.weekMonday)).count == payload.rewards.count else { return false }
        guard payload.ownedCosmetics.allSatisfy({ ownership in
            cosmetics[ownership.cosmeticId] != nil && ownership.earnedAt > 0
                && (ownership.sourceWeekMonday.map(monday) ?? true)
        }), Set(payload.ownedCosmetics.map(\.cosmeticId)).count == payload.ownedCosmetics.count else { return false }
        let owned = Set(payload.ownedCosmetics.map(\.cosmeticId))
        guard payload.loadout.allSatisfy({ item in
            guard let cosmetic = cosmetics[item.cosmeticId] else { return false }
            return Self.categoryName(cosmetic.category) == item.category && owned.contains(item.cosmeticId)
        }), Set(payload.loadout.map(\.category)).count == payload.loadout.count else { return false }
        if let draft = payload.practiceDraft {
            guard !draft.token.isEmpty && draft.token.count <= 256 && draft.startedAtMillis > 0 && draft.elapsedMillis >= 0
                && payload.workouts.allSatisfy({ $0.sourceId != "practice:" + draft.token }) else { return false }
        }
        return true
    }

    private static func csvField(_ value: String) -> String {
        "\"" + value.replacingOccurrences(of: "\"", with: "\"\"") + "\""
    }

    private func kotlinGoals() -> [WeeklyGoal] {
        goals.sorted { $0.effectiveMonday < $1.effectiveMonday }.map {
            WeeklyGoal(effectiveMonday: $0.effectiveMonday,
                       sessions: Int32($0.sessions),
                       minutes: $0.minutes.map { KotlinInt(int: Int32($0)) })
        }
    }

    private func kotlinWorkouts(through today: Int64 = EpochDay.of(Date())) -> [Workout] {
        var seen = Set<String>()
        return workouts.compactMap { w in
            guard w.epochDay <= today else { return nil }
            guard let source = ActivityStore.source(from: w.sourceRaw) else { return nil }
            guard seen.insert(w.sourceId).inserted else { return nil }
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
        let monday = mondayOf(EpochDay.of(now))
        let storedWeek = weeks.first { $0.monday == monday }
        let beta = storedWeek?.beta ?? (monday == mondayOf(firstUseDay) && EpochDay.mondayIndex(firstUseDay) != 1)
        ActivityDomainKt.activityOverview(today: EpochDay.of(now),
                                          zoneId: storedWeek?.zoneId ?? zoneId,
                                          goals: kotlinGoals(),
                                          workouts: kotlinWorkouts(through: EpochDay.of(now)),
                                          beta: beta)
    }

    func weeklyStreak(now: Date = Date()) -> WeeklyStreakSummary {
        let current = overview(now: now).week
        let history = weeks.filter { $0.monday < current.monday }.map {
            Week(monday: $0.monday, zoneId: $0.zoneId, beta: $0.beta)
        }
        return ActivityDomainKt.weeklyStreakSummary(historyWeeks: history,
                                                    currentWeek: current,
                                                    goals: kotlinGoals(),
                                                    workouts: kotlinWorkouts(through: EpochDay.of(now)))
    }

    func activityBoard(year: Int, now: Date = Date()) -> [ActivityBoardWeek] {
        let today = EpochDay.of(now)
        let yearStart = EpochDay.of(Calendar.current.date(from: DateComponents(year: year, month: 1, day: 1)) ?? now)
        let yearEnd = EpochDay.of(Calendar.current.date(from: DateComponents(year: year + 1, month: 1, day: 1)) ?? now) - 1
        guard yearStart <= today else { return [] }
        let last = min(yearEnd, today)
        var monday = mondayOf(yearStart)
        let endMonday = mondayOf(last)
        let records = Dictionary(weeks.map { ($0.monday, $0) }, uniquingKeysWith: { first, _ in first })
        let goals = kotlinGoals()
        let workouts = kotlinWorkouts(through: today)
        var days: [DayActivity] = []
        while monday <= endMonday {
            let stored = records[monday]
            let beta = stored?.beta ?? (monday == mondayOf(firstUseDay) && EpochDay.mondayIndex(firstUseDay) != 1)
            let week = Week(monday: monday, zoneId: stored?.zoneId ?? zoneId, beta: beta)
            let goal = ActivityDomainKt.activeGoal(goals: goals, monday: monday)
            days += ActivityDomainKt.classifyWeek(week: week, goal: goal, workouts: workouts, through: min(week.sunday, last))
                .filter { $0.epochDay >= firstUseDay && $0.epochDay >= yearStart && $0.epochDay <= last }
            monday += 7
        }
        return ActivityDomainKt.activityBoard(year: Int32(year), today: today, activities: days)
    }

    /// Creates missing week records and awards eligible cosmetics using only
    /// the shared Kotlin rules. Existing grants are never replaced or revoked.
    func refreshActivity(now: Date = Date()) {
        guard !unreadableExistingFile else { return }
        let today = EpochDay.of(now)
        let currentMonday = mondayOf(today)
        let firstMonday = mondayOf(firstUseDay)
        var nextWeeks = weeks
        var inheritedZone = nextWeeks.filter { $0.monday < firstMonday }.max { $0.monday < $1.monday }?.zoneId ?? zoneId
        var monday = firstMonday
        while monday <= currentMonday {
            if let existing = nextWeeks.first(where: { $0.monday == monday }) {
                inheritedZone = existing.zoneId
            } else {
                let beta = monday == firstMonday && EpochDay.mondayIndex(firstUseDay) != 1
                let stableZone = monday == currentMonday ? zoneId : inheritedZone
                nextWeeks.append(StoredWeek(monday: monday, zoneId: stableZone, beta: beta))
                inheritedZone = stableZone
            }
            monday += 7
        }
        nextWeeks.sort { $0.monday < $1.monday }

        var nextRewards = rewardGrants
        let kotlinGoals = self.kotlinGoals()
        let kotlinWorkouts = self.kotlinWorkouts(through: today)
        var reserved = Set(ownedCosmetics.map(\.cosmeticId) + rewardGrants.compactMap(\.cosmeticId))
        for storedWeek in nextWeeks where storedWeek.monday <= currentMonday {
            guard !nextRewards.contains(where: { $0.weekMonday == storedWeek.monday }) else { continue }
            let week = Week(monday: storedWeek.monday, zoneId: storedWeek.zoneId, beta: storedWeek.beta)
            let goal = ActivityDomainKt.activeGoal(goals: kotlinGoals, monday: storedWeek.monday)
            guard ActivityRewards.shared.eligible(closedWeek: storedWeek.monday < currentMonday,
                                                   beta: storedWeek.beta,
                                                   week: week,
                                                   goal: goal,
                                                   workouts: kotlinWorkouts) else { continue }
            let available = ActivityCosmetics.shared.all.filter { !reserved.contains($0.id) }
            let grantedAt = Int64(now.timeIntervalSince1970 * 1_000)
            if let cosmetic = ActivityRewards.shared.select(weekKey: weekKey(storedWeek.monday), available: available) {
                nextRewards.append(StoredRewardGrant(weekMonday: storedWeek.monday,
                                                     cosmeticId: cosmetic.id,
                                                     result: ActivityRewardsKt.REWARD_RESULT_COSMETIC,
                                                     grantedAt: grantedAt,
                                                     acknowledgedAt: 0))
                reserved.insert(cosmetic.id)
            } else {
                nextRewards.append(StoredRewardGrant(weekMonday: storedWeek.monday,
                                                     cosmeticId: nil,
                                                     result: ActivityRewardsKt.REWARD_RESULT_COLLECTION_COMPLETE,
                                                     grantedAt: grantedAt,
                                                     acknowledgedAt: grantedAt))
            }
        }

        guard nextWeeks != weeks || nextRewards != rewardGrants else { return }
        do {
            try persist(payload(weeks: nextWeeks, rewards: nextRewards))
            weeks = nextWeeks
            rewardGrants = nextRewards
            lastError = nil
        } catch {
            lastError = tr("activity.saveFailed")
        }
    }

    func claimReward(weekMonday: Int64, now: Date = Date()) -> Bool {
        guard let index = rewardGrants.firstIndex(where: { $0.weekMonday == weekMonday }) else { return false }
        let reward = rewardGrants[index]
        guard ActivityRewards.shared.claimable(cosmeticId: reward.cosmeticId, result: reward.result,
                                               acknowledgedAt: reward.acknowledgedAt),
              let cosmeticId = reward.cosmeticId else { return false }
        let acknowledgedAt = Int64(now.timeIntervalSince1970 * 1_000)
        var nextRewards = rewardGrants
        nextRewards[index].acknowledgedAt = acknowledgedAt
        var nextOwned = ownedCosmetics
        if ownedCosmetics.contains(where: { $0.cosmeticId == cosmeticId }) {
            do {
                try persist(payload(rewards: nextRewards))
                rewardGrants = nextRewards
                lastError = nil
            } catch { lastError = tr("activity.saveFailed") }
            return false
        }
        nextOwned.append(StoredCosmeticOwnership(cosmeticId: cosmeticId,
                                                 earnedAt: acknowledgedAt,
                                                 sourceWeekMonday: weekMonday))
        do {
            try persist(payload(rewards: nextRewards, ownedCosmetics: nextOwned))
            rewardGrants = nextRewards
            ownedCosmetics = nextOwned
            lastError = nil
            return true
        } catch {
            lastError = tr("activity.saveFailed")
            return false
        }
    }

    func equip(_ cosmetic: ActivityCosmetic) {
        guard let canonical = ActivityCosmetics.shared.all.first(where: { $0.id == cosmetic.id }),
              canonical.category == cosmetic.category,
              ownedCosmetics.contains(where: { $0.cosmeticId == cosmetic.id }) else {
            lastError = tr("cosmetic.notOwned")
            return
        }
        let category = Self.categoryName(cosmetic.category)
        var nextLoadout = loadout.filter { $0.category != category }
        nextLoadout.append(StoredCosmeticLoadout(category: category, cosmeticId: cosmetic.id))
        do {
            try persist(payload(loadout: nextLoadout))
            loadout = nextLoadout
            lastError = nil
        } catch { lastError = tr("activity.saveFailed") }
    }

    func unequip(category: String) {
        var nextLoadout = loadout.filter { $0.category != category }
        guard nextLoadout != loadout else { return }
        do {
            try persist(payload(loadout: nextLoadout))
            loadout = nextLoadout
            lastError = nil
        } catch { lastError = tr("activity.saveFailed") }
    }

    /// Daily streak across every persisted source (shared rule).
    func dailyStreak(now: Date = Date()) -> Int {
        Int(ActivityDomainKt.currentActivityStreak(workouts: kotlinWorkouts(through: EpochDay.of(now)),
                                                   today: EpochDay.of(now)))
    }

    /// Manual entries are limited to today and the previous two calendar days.
    func allowedManualDays(now: Date = Date()) -> [Int64] {
        ActivityDomainKt.manualActivityDates(today: EpochDay.of(now)).map { $0.int64Value }
    }

    // MARK: Mutations (validated before touching Kotlin `require` checks)

    func addManualEntry(minutes: Int, day: Int64, now: Date = Date()) {
        guard (1...1_440).contains(minutes) else {
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
        var next = workouts
        next.append(entry)
        do {
            try persist(payload(workouts: next))
            workouts = next
            lastError = nil
            refreshActivity(now: now)
        } catch { lastError = tr("activity.saveFailed") }
    }

    func updateManualEntry(id: String, minutes: Int, day: Int64, now: Date = Date()) {
        guard (1...1_440).contains(minutes) else { lastError = tr("entry.invalidDuration"); return }
        guard let index = workouts.firstIndex(where: { $0.id == id && $0.sourceRaw == "MANUAL_ENTRY" }) else { return }
        let originalDay = workouts[index].epochDay
        guard day == originalDay || allowedManualDays(now: now).contains(day) else {
            lastError = tr("entry.invalidDate")
            return
        }
        var next = workouts
        next[index] = StoredWorkout(epochDay: day, durationMillis: Int64(minutes) * 60_000,
                                    sourceId: id, sourceRaw: "MANUAL_ENTRY")
        do {
            try persist(payload(workouts: next))
            workouts = next
            lastError = nil
            refreshActivity(now: now)
        } catch { lastError = tr("activity.saveFailed") }
    }

    func deleteWorkout(id: String) {
        guard workouts.contains(where: { $0.id == id && $0.sourceRaw == "MANUAL_ENTRY" }) else { return }
        let next = workouts.filter { $0.id != id }
        guard next.count != workouts.count else { return }
        do {
            try persist(payload(workouts: next))
            workouts = next
            lastError = nil
            refreshActivity()
        } catch { lastError = tr("activity.saveFailed") }
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
        var next = goals
        if let index = next.firstIndex(where: { $0.effectiveMonday == effective }) {
            next[index] = goal
        } else {
            next.append(goal)
        }
        do {
            try persist(payload(goals: next))
            goals = next
            lastError = nil
            refreshActivity(now: now)
        } catch {
            lastError = tr("activity.saveFailed")
        }
    }

    func resetAllData() {
        let newFirstUseDay = EpochDay.of(Date())
        do {
            let reset = StorePayload(firstUseDay: newFirstUseDay, workouts: [], goals: [], weeks: [],
                                     rewards: [], ownedCosmetics: [], loadout: [], practiceDraft: nil)
            if unreadableExistingFile {
                let data = try JSONEncoder().encode(reset)
                try data.write(to: fileURL, options: .atomic)
                unreadableExistingFile = false
            } else {
                try persist(reset)
            }
            firstUseDay = newFirstUseDay
            workouts = []
            goals = []
            weeks = []
            rewardGrants = []
            ownedCosmetics = []
            loadout = []
            practiceDraft = nil
            practiceSegmentStartedAt = nil
            lastError = nil
            refreshActivity()
        } catch { lastError = tr("activity.saveFailed") }
    }

    func clearError() {
        lastError = nil
    }

    private func mondayOf(_ day: Int64) -> Int64 {
        day - Int64(EpochDay.mondayIndex(day) - 1)
    }

    private func weekKey(_ monday: Int64) -> String {
        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = TimeZone(secondsFromGMT: 0)!
        let formatter = DateFormatter()
        formatter.calendar = calendar
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.timeZone = calendar.timeZone
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter.string(from: Date(timeIntervalSince1970: TimeInterval(monday) * 86_400))
    }

    static func categoryName(_ category: CosmeticCategory) -> String {
        switch category {
        case .avatar: "AVATAR"
        case .frame: "FRAME"
        case .background: "BACKGROUND"
        case .heatmap: "HEATMAP"
        case .graph: "GRAPH"
        default: ""
        }
    }
}
