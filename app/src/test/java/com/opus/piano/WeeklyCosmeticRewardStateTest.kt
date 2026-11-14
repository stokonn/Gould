package com.opus.piano

import com.opus.piano.activity.domain.FreezeState
import com.opus.piano.activity.domain.Week
import com.opus.piano.activity.domain.WeekTotals
import com.opus.piano.activity.domain.WeeklyGoal
import com.opus.piano.data.ActivityRewardGrant
import com.opus.piano.data.ActivitySnapshot
import com.opus.piano.ui.ActivityUiState
import com.opus.piano.ui.WeeklyCosmeticRewardState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class WeeklyCosmeticRewardStateTest {
    private val monday = LocalDate.parse("2026-11-09")
    private val grant = ActivityRewardGrant(monday.toString(), "cosmetic.test", "COSMETIC", 10L)

    private fun snapshot() = ActivitySnapshot(
        firstUseDay = monday,
        week = Week(monday.toEpochDay(), "UTC"),
        goal = WeeklyGoal(monday.toEpochDay(), 3, null),
        days = emptyList(),
        totals = WeekTotals(0, 0),
        allTimeWorkouts = 0,
        allTimeMinutes = 0,
        currentWeeklyStreak = 0,
        longestWeeklyStreak = 0,
        freezes = FreezeState(),
        todayDurationMillis = 0,
        dailyStreak = 0,
        recentDailyDurationMillis = List(7) { 0L },
        allTimeDurationMillis = 0,
    )

    @Test fun rewardStateWaitsForDatabaseObservationBeforeShowingLocked() {
        assertEquals(WeeklyCosmeticRewardState.Loading, ActivityUiState(snapshot = snapshot()).cosmeticRewardState)
        assertEquals(
            WeeklyCosmeticRewardState.Locked,
            ActivityUiState(snapshot = snapshot(), rewardStateLoaded = true).cosmeticRewardState,
        )
    }

    @Test fun pendingRewardMapsToClaimState() {
        val state = ActivityUiState(pendingReward = grant, rewardStateLoaded = true)
        assertEquals(WeeklyCosmeticRewardState.Claim(grant), state.cosmeticRewardState)
    }

    @Test fun pendingRewardRemainsClaimableWithoutSnapshotAndTakesPrecedenceOverClaimedHistory() {
        val olderClaim = grant.copy(weekMonday = monday.minusWeeks(1).toString(), acknowledgedAt = 25L)
        val state = ActivityUiState(pendingReward = grant, claimedReward = olderClaim, rewardStateLoaded = true)
        assertEquals(WeeklyCosmeticRewardState.Claim(grant), state.cosmeticRewardState)
    }

    @Test fun persistedClaimMapsToClaimedStateAndNotClaim() {
        val claimed = grant.copy(acknowledgedAt = 25L)
        val state = ActivityUiState(claimedReward = claimed, rewardStateLoaded = true)
        assertEquals(WeeklyCosmeticRewardState.Claimed(claimed), state.cosmeticRewardState)
        assertTrue(state.cosmeticRewardState is WeeklyCosmeticRewardState.Claimed)
    }
}
