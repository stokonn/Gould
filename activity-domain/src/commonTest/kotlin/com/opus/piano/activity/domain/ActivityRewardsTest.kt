package com.opus.piano.activity.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ActivityRewardsTest {
    private val monday = 20_052L // 2024-11-25, a Monday
    private val week = Week(monday, "Europe/Prague")
    private val goal = WeeklyGoal(monday, sessions = 3, minutes = 30)
    private val perfect = listOf(
        Workout(monday, 15 * 60_000L, "a", WorkoutSource.PRACTICE_SESSION),
        Workout(monday + 1, 15 * 60_000L, "b", WorkoutSource.PRACTICE_SESSION),
        Workout(monday + 2, 30 * 60_000L, "c", WorkoutSource.MANUAL_ENTRY),
    )

    @Test fun rewardsGrantOnlyForPerfectClosedWeeksOrActiveBetaWeeks() {
        // Regular week: only after closing.
        assertTrue(ActivityRewards.eligible(closedWeek = true, beta = false, week, goal, perfect))
        assertFalse(ActivityRewards.eligible(closedWeek = false, beta = false, week, goal, perfect))
        // Active Beta week may grant immediately.
        assertTrue(ActivityRewards.eligible(closedWeek = false, beta = true, week.copy(beta = true), goal, perfect))
        // An imperfect week never grants.
        assertFalse(ActivityRewards.eligible(closedWeek = true, beta = false, week, goal, perfect.drop(1)))
    }

    @Test fun rewardSelectionIsDeterministicForTheSameKeyAndSet() {
        val available = ActivityCosmetics.all
        val first = ActivityRewards.select("2026-11-09", available)
        val again = ActivityRewards.select("2026-11-09", available)
        assertNotNull(first)
        assertEquals(first, again)
        // A different week key must not be pinned to the same cosmetic forever.
        val otherKeys = (0 until 8).map { ActivityRewards.select("2026-1${it}-0$it", available) }
        assertTrue(otherKeys.filterNotNull().distinct().size > 1)
    }

    @Test fun rewardSelectionSkipsReservedCosmeticsAndSignalsCollectionComplete() {
        val all = ActivityCosmetics.all
        val reservedAll = all.map { it.id }
        assertNull(ActivityRewards.select("2026-11-09", all.filterNot { it.id in reservedAll }))

        val oneLeft = all.filter { it.id == "graph_celestial" }
        val picked = ActivityRewards.select("2026-11-09", oneLeft)
        assertEquals("graph_celestial", picked?.id)
    }

    @Test fun claimableRewardsFireOnceAndOnlyForCosmeticGrants() {
        assertTrue(ActivityRewards.claimable("frame_meadow", REWARD_RESULT_COSMETIC, acknowledgedAt = 0L))
        assertFalse(ActivityRewards.claimable(null, REWARD_RESULT_COSMETIC, acknowledgedAt = 0L))
        assertFalse(ActivityRewards.claimable(null, REWARD_RESULT_COLLECTION_COMPLETE, 5L))
        assertFalse(ActivityRewards.claimable("frame_meadow", REWARD_RESULT_COSMETIC, acknowledgedAt = 9L))
        assertFalse(ActivityRewards.claimable("frame_meadow", "OTHER", acknowledgedAt = 0L))
    }
}
