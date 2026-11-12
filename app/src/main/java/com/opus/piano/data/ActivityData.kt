package com.opus.piano.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "activity_settings")
data class ActivitySettings(@PrimaryKey val id: Int = 1, val firstUseDay: String, val firstUseZoneId: String)

@Entity(tableName = "activity_weekly_goals")
data class ActivityWeeklyGoal(
    @PrimaryKey val effectiveMonday: String,
    val requiredSessions: Int,
    val minutesGoal: Int? = null,
)

/** Zone and Beta Week are fixed once a week is first initialized. */
@Entity(tableName = "activity_weeks")
data class ActivityWeekRecord(
    @PrimaryKey val monday: String,
    val zoneId: String,
    val beta: Boolean,
    val firstObservedAt: Long,
)

/** A manual Activity record is intentionally distinct from microphone/practice evidence. */
@Entity(tableName = "manual_activity_entries", indices = [Index("activityDay")])
data class ManualActivityEntry(
    @PrimaryKey val id: String,
    val activityDay: String,
    val zoneId: String,
    val durationMillis: Long,
    val createdAt: Long,
)

@Entity(tableName = "cosmetic_ownership", indices = [Index(value = ["sourceWeekMonday"], unique = true)])
data class CosmeticOwnership(
    @PrimaryKey val cosmeticId: String,
    val earnedAt: Long,
    val sourceWeekMonday: String? = null,
)

@Entity(tableName = "activity_reward_grants")
data class ActivityRewardGrant(
    @PrimaryKey val weekMonday: String,
    val cosmeticId: String?,
    val result: String,
    val grantedAt: Long,
    val acknowledgedAt: Long = 0,
)

@Entity(tableName = "cosmetic_loadout")
data class CosmeticLoadout(@PrimaryKey val category: String, val cosmeticId: String)

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activity_settings WHERE id=1") suspend fun settings(): ActivitySettings?
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun initialize(value: ActivitySettings): Long
    @Query("SELECT * FROM activity_weekly_goals ORDER BY effectiveMonday") fun goals(): Flow<List<ActivityWeeklyGoal>>
    @Query("SELECT * FROM activity_weekly_goals ORDER BY effectiveMonday") suspend fun allGoals(): List<ActivityWeeklyGoal>
    @Query("SELECT * FROM activity_weekly_goals WHERE effectiveMonday=:monday LIMIT 1") suspend fun goal(monday: String): ActivityWeeklyGoal?
    @Upsert suspend fun saveGoal(value: ActivityWeeklyGoal)

    @Query("SELECT * FROM activity_weeks ORDER BY monday") suspend fun weeks(): List<ActivityWeekRecord>
    @Query("SELECT * FROM activity_weeks WHERE monday=:monday LIMIT 1") suspend fun week(monday: String): ActivityWeekRecord?
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun observeWeek(value: ActivityWeekRecord): Long

    @Query("SELECT * FROM manual_activity_entries ORDER BY activityDay,id") fun manualEntries(): Flow<List<ManualActivityEntry>>
    @Query("SELECT * FROM manual_activity_entries ORDER BY activityDay,id") suspend fun allManualEntries(): List<ManualActivityEntry>
    @Insert suspend fun addManualEntry(value: ManualActivityEntry)
    @Query("DELETE FROM manual_activity_entries WHERE id=:id") suspend fun deleteManualEntry(id: String): Int

    @Query("SELECT * FROM cosmetic_ownership ORDER BY earnedAt,cosmeticId") fun ownedCosmetics(): Flow<List<CosmeticOwnership>>
    @Query("SELECT * FROM cosmetic_ownership ORDER BY earnedAt,cosmeticId") suspend fun allOwnedCosmetics(): List<CosmeticOwnership>
    @Query("SELECT EXISTS(SELECT 1 FROM cosmetic_ownership WHERE cosmeticId=:id)") suspend fun owns(id: String): Boolean
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun own(value: CosmeticOwnership): Long
    @Query("SELECT * FROM activity_reward_grants ORDER BY weekMonday") suspend fun grants(): List<ActivityRewardGrant>
    @Query("SELECT * FROM activity_reward_grants WHERE weekMonday=:monday") suspend fun grant(monday: String): ActivityRewardGrant?
    @Query("SELECT * FROM activity_reward_grants WHERE acknowledgedAt=0 ORDER BY weekMonday LIMIT 1") fun pendingReward(): Flow<ActivityRewardGrant?>
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun grant(value: ActivityRewardGrant): Long
    @Query("UPDATE activity_reward_grants SET acknowledgedAt=:now WHERE weekMonday=:monday AND acknowledgedAt=0") suspend fun acknowledgeReward(monday: String, now: Long): Int

    @Query("SELECT * FROM cosmetic_loadout ORDER BY category") fun loadout(): Flow<List<CosmeticLoadout>>
    @Query("SELECT * FROM cosmetic_loadout ORDER BY category") suspend fun allLoadout(): List<CosmeticLoadout>
    @Upsert suspend fun equip(value: CosmeticLoadout)
    @Query("DELETE FROM cosmetic_loadout WHERE category=:category") suspend fun clearLoadout(category: String)
}
