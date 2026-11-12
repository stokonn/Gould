package com.opus.piano.data

import androidx.room.withTransaction
import com.opus.piano.activity.domain.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.UUID
import kotlin.random.Random

data class ActivitySnapshot(
    val firstUseDay: LocalDate,
    val week: Week,
    val goal: WeeklyGoal,
    val days: List<DayActivity>,
    val totals: WeekTotals,
    val allTimeWorkouts: Int,
    val allTimeMinutes: Long,
    val currentWeeklyStreak: Int,
    val longestWeeklyStreak: Int,
    val freezes: FreezeState,
)

/** Android/Room adapter for the platform-free weekly rules. */
class ActivityRepository(private val db: OpusDatabase) {
    val goals: Flow<List<ActivityWeeklyGoal>> get() = db.activityDao().goals()
    val manualEntries: Flow<List<ManualActivityEntry>> get() = db.activityDao().manualEntries()
    val ownedCosmetics: Flow<List<CosmeticOwnership>> get() = db.activityDao().ownedCosmetics()
    val loadout: Flow<List<CosmeticLoadout>> get() = db.activityDao().loadout()
    val pendingReward: Flow<ActivityRewardGrant?> get() = db.activityDao().pendingReward()

    suspend fun initialize(today: LocalDate = LocalDate.now(), zone: ZoneId = ZoneId.systemDefault(), now: Long = System.currentTimeMillis()): ActivitySettings = db.withTransaction {
        val dao = db.activityDao()
        dao.initialize(ActivitySettings(firstUseDay = today.toString(), firstUseZoneId = zone.id))
        val settings = checkNotNull(dao.settings())
        val firstUse = LocalDate.parse(settings.firstUseDay)
        val firstMonday = monday(firstUse)
        if (dao.goal(firstMonday.toString()) == null) dao.saveGoal(ActivityWeeklyGoal(firstMonday.toString(), 3, null))
        ensureWeekRows(settings, today, zone, now)
        settings
    }

    suspend fun changeGoals(sessions: Int, minutes: Int?, today: LocalDate = LocalDate.now(), zone: ZoneId = ZoneId.systemDefault()) {
        require(sessions in 1..7 && (minutes == null || minutes > 0))
        initialize(today, zone)
        val effective = nextGoalMonday(today)
        db.activityDao().saveGoal(ActivityWeeklyGoal(effective.toString(), sessions, minutes))
    }

    suspend fun addManualActivity(day: LocalDate, minutes: Int, today: LocalDate = LocalDate.now(), zone: ZoneId = ZoneId.systemDefault(), now: Long = System.currentTimeMillis()): String {
        require(minutes in 1..1440) { "Enter a duration from 1 to 1,440 minutes." }
        val settings = initialize(today, zone)
        require(day < today) { "Activity can only be added through yesterday." }
        require(day >= LocalDate.parse(settings.firstUseDay)) { "Activity must be within Gould Activity history." }
        val id = UUID.randomUUID().toString()
        db.activityDao().addManualEntry(ManualActivityEntry(id, day.toString(), zone.id, minutes * 60_000L, now))
        return id
    }

    suspend fun deleteManualActivity(id: String) {
        require(db.activityDao().deleteManualEntry(id) == 1) { "Manual activity entry no longer exists." }
    }

    suspend fun snapshot(today: LocalDate = LocalDate.now(), zone: ZoneId = ZoneId.systemDefault(), now: Long = System.currentTimeMillis()): ActivitySnapshot = withContext(Dispatchers.Default) {
        val settings = initialize(today, zone, now)
        val firstUse = LocalDate.parse(settings.firstUseDay)
        val monday = monday(today)
        val weekRecord = checkNotNull(db.activityDao().week(monday.toString()))
        val week = Week(monday.toEpochDay(), weekRecord.zoneId, weekRecord.beta)
        val goals = db.activityDao().allGoals().map { WeeklyGoal(LocalDate.parse(it.effectiveMonday).toEpochDay(), it.requiredSessions, it.minutesGoal) }
        val goal = activeGoal(goals, week.monday)
        val workouts = workouts(today, weekRecord.zoneId)
        evaluateRewards(firstUse, monday, goals, workouts, now)
        val currentWorkouts = workouts.filter { it.epochDay in week.monday..week.sunday }
        val totals = aggregate(currentWorkouts, week)
        val daily = classifyWeek(week, goal, currentWorkouts, today.toEpochDay())
        val closed = evaluateHistory(firstUse, monday, settings, goals, workouts)
        val state = closed.lastOrNull()?.streak ?: FreezeState()
        val currentMet = !week.beta && totals.sessions >= goal.sessions
        val visibleStreak = state.currentStreak + if (currentMet) 1 else 0
        ActivitySnapshot(firstUse, week, goal, daily, totals, workouts.size,
            workouts.fold(0L) { total, item -> saturatingAdd(total, item.durationMillis) } / 60_000L,
            visibleStreak, maxOf(state.longestStreak, visibleStreak), state.copy(currentStreak = visibleStreak, longestStreak = maxOf(state.longestStreak, visibleStreak)))
    }

    suspend fun yearActivity(year: Int, today: LocalDate = LocalDate.now(), zone: ZoneId = ZoneId.systemDefault()): List<DayActivity> = withContext(Dispatchers.Default) {
        require(year in 1..today.year)
        val settings = initialize(today, zone)
        val firstUse = LocalDate.parse(settings.firstUseDay)
        val first = LocalDate.of(year, 1, 1)
        val last = minOf(LocalDate.of(year, 12, 31), today)
        if (last < first) return@withContext emptyList()
        val goals = db.activityDao().allGoals().map { WeeklyGoal(LocalDate.parse(it.effectiveMonday).toEpochDay(), it.requiredSessions, it.minutesGoal) }
        val weeks = db.activityDao().weeks().associateBy { it.monday }
        val allWorkouts = workouts(today, zone.id)
        val result = mutableListOf<DayActivity>()
        // Preserve visible legacy practice history before Activity activation without
        // assigning retroactive Day Off/Miss labels to time before weekly goals existed.
        allWorkouts.asSequence().filter { LocalDate.ofEpochDay(it.epochDay) < firstUse }
            .groupBy { it.epochDay }.forEach { (day, rows) ->
                if (LocalDate.ofEpochDay(day).year == year) result += DayActivity(day, rows.size,
                    rows.fold(0L) { total, row -> saturatingAdd(total, row.durationMillis) } / 60_000L, DayKind.TRAINING)
            }
        var monday = first.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val endMonday = last.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        while (monday <= endMonday) {
            val stored = weeks[monday.toString()]
            val beta = stored?.beta ?: (monday(firstUse) == monday && firstUse.dayOfWeek != DayOfWeek.MONDAY)
            val week = Week(monday.toEpochDay(), stored?.zoneId ?: settings.firstUseZoneId, beta)
            val goal = activeGoal(goals, week.monday)
            result += classifyWeek(week, goal, allWorkouts, last.toEpochDay()).filter { epoch ->
                val date = LocalDate.ofEpochDay(epoch.epochDay)
                date >= firstUse && date.year == year
            }
            monday = monday.plusWeeks(1)
        }
        result
    }

    suspend fun changeLoadout(category: String, cosmeticId: String?) {
        require(category in setOf("AVATAR", "FRAME", "BACKGROUND", "HEATMAP", "GRAPH"))
        if (cosmeticId == null) db.activityDao().clearLoadout(category)
        else {
            require(db.activityDao().owns(cosmeticId)) { "Cosmetic is not owned." }
            db.activityDao().equip(CosmeticLoadout(category, cosmeticId))
        }
    }

    suspend fun acknowledgeReward(weekMonday: String, now: Long = System.currentTimeMillis()) {
        db.activityDao().acknowledgeReward(weekMonday, now)
    }

    private suspend fun evaluateRewards(firstUse: LocalDate, currentMonday: LocalDate, goals: List<WeeklyGoal>, workouts: List<Workout>, now: Long) {
        var monday = monday(firstUse)
        while (monday <= currentMonday) {
            val week = Week(monday.toEpochDay(), db.activityDao().week(monday.toString())?.zoneId ?: "UTC")
            val goal = activeGoal(goals, week.monday)
            val minutesTarget = goal.minutes
            if (minutesTarget != null && aggregate(workouts, week).minutes >= minutesTarget) grantReward(monday, now)
            monday = monday.plusWeeks(1)
        }
    }

    private suspend fun grantReward(monday: LocalDate, now: Long) = db.withTransaction {
        val dao = db.activityDao()
        val key = monday.toString()
        if (dao.grant(key) != null) return@withTransaction
        val owned = dao.allOwnedCosmetics().mapTo(mutableSetOf()) { it.cosmeticId }
        val available = ActivityCosmetics.all.filterNot { it.id in owned }
        if (available.isEmpty()) {
            dao.grant(ActivityRewardGrant(key, null, "COLLECTION_COMPLETE", now))
            return@withTransaction
        }
        val random = Random(key.hashCode())
        val rarities = available.map { it.rarity }.distinct()
        val totalWeight = rarities.sumOf { it.weight }
        var choice = random.nextInt(totalWeight)
        val selectedRarity = rarities.first { rarity -> choice -= rarity.weight; choice < 0 }
        val sameRarity = available.filter { it.rarity == selectedRarity }
        val selected = sameRarity[random.nextInt(sameRarity.size)]
        check(dao.own(CosmeticOwnership(selected.id, now, key)) != -1L) { "Cosmetic ownership already exists." }
        check(dao.grant(ActivityRewardGrant(key, selected.id, "COSMETIC", now)) != -1L) { "Reward grant already exists." }
    }

    private suspend fun workouts(today: LocalDate, currentZone: String): List<Workout> {
        val sessions = db.dao().allSessions().filter(AchievementEngine::valid)
        val manuals = db.activityDao().allManualEntries()
        return buildList {
            sessions.forEach { session ->
                val fallback = runCatching { ZoneId.of(session.practiceZone ?: currentZone) }.getOrElse { ZoneId.of(currentZone) }
                val day = AchievementEngine.recordedDay(session, fallback)
                if (day <= today) add(Workout(day.toEpochDay(), session.durationMillis, "practice:${session.id}", WorkoutSource.PRACTICE_SESSION))
            }
            manuals.forEach { entry ->
                val day = LocalDate.parse(entry.activityDay)
                if (day <= today) add(Workout(day.toEpochDay(), entry.durationMillis, "manual:${entry.id}", WorkoutSource.MANUAL_ENTRY))
            }
        }
    }

    private suspend fun evaluateHistory(firstUse: LocalDate, currentMonday: LocalDate, settings: ActivitySettings, goals: List<WeeklyGoal>, workouts: List<Workout>): List<WeekOutcome> {
        var start = monday(firstUse)
        val pairs = mutableListOf<Pair<Week, Boolean?>>()
        while (start < currentMonday) {
            val record = db.activityDao().week(start.toString())
            val stableZone = record?.zoneId ?: settings.firstUseZoneId
            val beta = record?.beta ?: (firstUse.dayOfWeek != DayOfWeek.MONDAY && start == monday(firstUse))
            val week = Week(start.toEpochDay(), stableZone, beta)
            val goal = activeGoal(goals, week.monday)
            val result = weekSuccess(week, goal, workouts, week.sunday)
            pairs += week to result
            start = start.plusWeeks(1)
        }
        return evaluateWeeks(FreezeState(), pairs)
    }

    private suspend fun ensureWeekRows(settings: ActivitySettings, today: LocalDate, currentZone: ZoneId, now: Long) {
        var start = monday(LocalDate.parse(settings.firstUseDay))
        val end = monday(today)
        var inheritedZone = settings.firstUseZoneId
        while (start <= end) {
            val prior = db.activityDao().week(start.toString())
            if (prior != null) inheritedZone = prior.zoneId
            else {
                val isCurrent = start == end
                val first = start == monday(LocalDate.parse(settings.firstUseDay))
                val beta = first && LocalDate.parse(settings.firstUseDay).dayOfWeek != DayOfWeek.MONDAY
                val record = ActivityWeekRecord(start.toString(), if (isCurrent) currentZone.id else inheritedZone, beta, now)
                db.activityDao().observeWeek(record)
                inheritedZone = record.zoneId
            }
            start = start.plusWeeks(1)
        }
    }

    private fun monday(day: LocalDate) = day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    private fun nextGoalMonday(day: LocalDate) = day.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
}
