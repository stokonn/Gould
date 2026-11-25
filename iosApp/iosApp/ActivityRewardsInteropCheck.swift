import ActivityDomain

/// Compile-only coverage for the Kotlin/Native reward API. Keep this out of UI
/// flows; the method is intentionally never called.
enum ActivityRewardsInteropCheck {
    static func compile() {
        let monday: Int64 = 20_052
        let week = Week(monday: monday, zoneId: "UTC", beta: false)
        let goal = WeeklyGoal(
            effectiveMonday: monday,
            sessions: 1,
            minutes: KotlinInt(int: 1)
        )
        let workout = Workout(
            epochDay: monday,
            durationMillis: Int64(60_000),
            sourceId: "interop-check",
            source: WorkoutSource.manualEntry
        )
        let workouts = [workout]

        let cosmetics = ActivityCosmetics.shared
        let all: [ActivityCosmetic] = cosmetics.all
        let byId = cosmetics.byId
        let indexed: ActivityCosmetic? = byId["avatar_ivory_note"]

        let rewards = ActivityRewards.shared
        let streak = ActivityDomainKt.weeklyStreakSummary(
            historyWeeks: [], currentWeek: week, goals: [goal], workouts: workouts
        )
        _ = streak.currentStreak
        _ = ActivityBoardKt.activityBoard(year: Int32(2024), today: monday, activities: [])
        _ = ActivityDomainKt.classifyWeek(week: week, goal: goal, workouts: workouts, through: week.sunday)
        _ = ActivityDomainKt.goalMet(totals: WeekTotals(sessions: 1, minutes: 1), goal: goal)
        _ = rewards.eligible(
            closedWeek: true,
            beta: week.beta,
            week: week,
            goal: goal,
            workouts: workouts
        )
        let selected = rewards.select(weekKey: "2024-11-25", available: all)
        _ = rewards.select(weekKey: "empty", available: [])
        _ = rewards.claimable(
            cosmeticId: selected?.id,
            result: ActivityRewardsKt.REWARD_RESULT_COSMETIC,
            acknowledgedAt: Int64(0)
        )
        _ = rewards.claimable(cosmeticId: nil, result: ActivityRewardsKt.REWARD_RESULT_COLLECTION_COMPLETE, acknowledgedAt: Int64(1))

        _ = CosmeticCategory.avatar
        _ = CosmeticCategory.frame
        _ = CosmeticCategory.background
        _ = CosmeticCategory.heatmap
        _ = CosmeticCategory.graph
        _ = CosmeticRarity.common
        _ = CosmeticRarity.rare
        _ = CosmeticRarity.epic
        _ = CosmeticRarity.legendary
        _ = indexed
    }
}
