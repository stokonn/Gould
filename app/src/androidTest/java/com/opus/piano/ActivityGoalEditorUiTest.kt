package com.opus.piano

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.opus.piano.data.ActivityRepository
import com.opus.piano.data.ManualActivityEntry
import com.opus.piano.data.OpusDatabase
import com.opus.piano.ui.ActivityScreen
import com.opus.piano.ui.ActivityViewModel
import com.opus.piano.ui.OpusTheme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

/** Non-visual semantics test: the Activity goal action opens the existing editor. */
@RunWith(AndroidJUnit4::class)
class ActivityGoalEditorUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun weeklyGoalEditorOpensFromActivitySection() {
        val db = Room.inMemoryDatabaseBuilder(compose.activity, OpusDatabase::class.java).build()
        val repository = ActivityRepository(db)
        val model = ActivityViewModel(repository)
        val store = ViewModelStore().also { it.put("activity", model) }
        try {
            runBlocking { repository.initialize() }
            compose.activity.setContent {
                OpusTheme("Light") { ActivityScreen(model) {} }
            }
            compose.waitUntil(10_000) { model.state.value.snapshot != null && model.state.value.rewardStateLoaded }
            compose.onNodeWithText(compose.activity.getString(R.string.activity_reward_locked)).assertIsDisplayed()
            val editDescription = compose.activity.getString(R.string.activity_edit_goal)
            compose.onNodeWithTag("activity-edit-goal-header").assertIsDisplayed()
            compose.onNodeWithContentDescription(editDescription).assertIsDisplayed().performClick()
            compose.onNodeWithText(compose.activity.getString(R.string.activity_optional_minutes_goal)).assertIsDisplayed()
            compose.onNodeWithText(compose.activity.getString(R.string.activity_claim_reward)).assertDoesNotExist()
        } finally {
            store.clear()
            db.close()
        }
    }

    @Test fun claimButtonRevealsAndOwnsThePendingCosmetic() {
        val db = Room.inMemoryDatabaseBuilder(compose.activity, OpusDatabase::class.java).build()
        val repository = ActivityRepository(db)
        val model = ActivityViewModel(repository)
        val store = ViewModelStore().also { it.put("activity", model) }
        try {
            val zone = ZoneId.systemDefault()
            val today = LocalDate.now(zone)
            val currentMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val earnedMonday = currentMonday.minusWeeks(1)
            runBlocking {
                repository.initialize(earnedMonday, zone, now=1L)
                repeat(3) { offset ->
                    val date = earnedMonday.plusDays(offset.toLong())
                    db.activityDao().addManualEntry(ManualActivityEntry(
                        "claim-ui-$offset", date.toString(), zone.id, 20 * 60_000L, 10L + offset,
                    ))
                }
                repository.snapshot(today, zone, now=100L)
            }
            compose.activity.setContent {
                OpusTheme("Light") { ActivityScreen(model) {} }
            }
            compose.waitUntil(10_000) { model.state.value.pendingReward != null && model.state.value.rewardStateLoaded }
            val reward = checkNotNull(model.state.value.pendingReward)
            assertEquals(earnedMonday.toString(), reward.weekMonday)
            val cosmeticId = checkNotNull(reward.cosmeticId)
            assertFalse(runBlocking { db.activityDao().owns(cosmeticId) })

            val section = compose.onNodeWithTag("activity-weekly-goal-section")
            val claimText = compose.activity.getString(R.string.activity_claim_reward)
            val editDescription = compose.activity.getString(R.string.activity_edit_goal)
            compose.onNodeWithTag("activity-edit-goal-header").assertIsDisplayed()
            section.assert(hasAnyDescendant(hasText(claimText)))
            compose.onNodeWithText(claimText).assertIsDisplayed()

            compose.onNodeWithText(claimText).performClick()
            compose.waitUntil(10_000) { runBlocking { db.activityDao().owns(cosmeticId) } }
            compose.onNodeWithText(compose.activity.getString(R.string.activity_reward_unlocked)).assertIsDisplayed()
            assertTrue(runBlocking { (db.activityDao().grant(reward.weekMonday)?.acknowledgedAt ?: 0L) > 0L })
            assertEquals(cosmeticId, runBlocking { db.activityDao().allOwnedCosmetics().single().cosmeticId })
            compose.waitUntil(10_000) { model.state.value.claimedReward?.weekMonday == reward.weekMonday && model.state.value.pendingReward == null }
            compose.onNodeWithText(compose.activity.getString(R.string.activity_reward_close)).performClick()
            compose.onNodeWithText(compose.activity.getString(R.string.activity_reward_claimed)).assertIsDisplayed()
            compose.onNodeWithText(claimText).assertDoesNotExist()
        } finally {
            store.clear()
            db.close()
        }
    }
}
