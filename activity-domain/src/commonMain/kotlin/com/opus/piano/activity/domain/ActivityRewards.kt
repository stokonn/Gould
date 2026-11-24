package com.opus.piano.activity.domain

import kotlin.random.Random

/** Reward grant result markers (persisted verbatim by every platform). */
const val REWARD_RESULT_COSMETIC = "COSMETIC"
const val REWARD_RESULT_COLLECTION_COMPLETE = "COLLECTION_COMPLETE"

/**
 * Weekly cosmetic-reward rules, shared by Android and iOS so neither platform
 * reimplements reward business logic. Persistence stays platform-side; every
 * decision that decides *whether* and *which* reward is granted lives here.
 */
object ActivityRewards {
    /**
     * Cosmetics follow weekly goals independently of Streak eligibility.
     * A fulfilled active Beta Week can grant immediately; regular weeks still
     * grant only after closing. Streak evaluation separately treats Beta as
     * neutral in `weekSuccess`/`evaluateWeeks`.
     */
    fun eligible(closedWeek: Boolean, beta: Boolean, week: Week, goal: WeeklyGoal, workouts: List<Workout>): Boolean =
        (closedWeek || beta) && perfectWeek(week, goal, workouts)

    /**
     * Deterministic seeded selection of the next unreserved cosmetic.
     * Returns null when every cosmetic is already reserved — the caller records
     * [REWARD_RESULT_COLLECTION_COMPLETE] instead. The same week key always
     * yields the same choice for the same available set on every platform.
     */
    fun select(weekKey: String, available: List<ActivityCosmetic>): ActivityCosmetic? {
        if (available.isEmpty()) return null
        val random = Random(weekKey.hashCode())
        val rarities = available.map { it.rarity }.distinct()
        val totalWeight = rarities.sumOf { it.weight }
        var choice = random.nextInt(totalWeight)
        val selectedRarity = rarities.first { rarity -> choice -= rarity.weight; choice < 0 }
        val sameRarity = available.filter { it.rarity == selectedRarity }
        return sameRarity[random.nextInt(sameRarity.size)]
    }

    /**
     * A grant is claimable exactly once, only when it actually carries a
     * cosmetic. Rows already acknowledged (including auto-claimed upgrades
     * from older builds) are never claimable again.
     */
    fun claimable(cosmeticId: String?, result: String, acknowledgedAt: Long): Boolean =
        cosmeticId != null && result == REWARD_RESULT_COSMETIC && acknowledgedAt == 0L
}
