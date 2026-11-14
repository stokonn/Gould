package com.opus.piano

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.activity.domain.DayKind
import com.opus.piano.data.*
import com.opus.piano.ui.ActivityViewModel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class ActivityRepositoryTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    private suspend fun withRepository(test: suspend (OpusDatabase, ActivityRepository) -> Unit) {
        val db = Room.inMemoryDatabaseBuilder(context, OpusDatabase::class.java).build()
        try { test(db, ActivityRepository(db)) } finally { db.close() }
    }

    private fun day(text: String) = LocalDate.parse(text)
    private val zone = ZoneId.of("Europe/Prague")
    private suspend fun addPerfectWeek(db: OpusDatabase, monday: LocalDate, prefix: String, zone: ZoneId = this.zone) {
        repeat(3) { offset ->
            db.activityDao().addManualEntry(ManualActivityEntry(
                "$prefix-$offset", monday.plusDays(offset.toLong()).toString(), zone.id,
                20 * 60_000L, offset.toLong() + 1,
            ))
        }
    }

    @Test fun betaWeekGoalEffectiveDateAndManualBoundsArePersisted() = runBlocking { withRepository { db, repo ->
        val beta = repo.snapshot(day("2026-11-12"), zone, now=1)
        assertTrue(beta.week.beta)
        assertEquals(3, beta.goal.sessions)
        repo.changeGoals(2, 90, day("2026-11-12"), zone)
        assertEquals(3, repo.snapshot(day("2026-11-12"), zone, now=2).goal.sessions)
        val nextWeek = repo.snapshot(day("2026-11-16"), zone, now=3)
        assertFalse(nextWeek.week.beta)
        assertEquals(2, nextWeek.goal.sessions)
        val id = repo.addManualActivity(day("2026-11-16"), 25, day("2026-11-17"), zone, now=4)
        val todayId = repo.addManualActivity(day("2026-11-17"), 10, day("2026-11-17"), zone, now=5)
        val twoDaysAgoId = repo.addManualActivity(day("2026-11-15"), 10, day("2026-11-17"), zone, now=6)
        val summary = repo.snapshot(day("2026-11-17"), zone, now=5)
        // Weekly totals exclude the valid manual entry from the prior Sunday.
        assertEquals(2, summary.totals.sessions)
        assertEquals(35L, summary.totals.minutes)
        assertEquals(DayKind.TRAINING, summary.days.first { it.epochDay == day("2026-11-16").toEpochDay() }.kind)
        assertEquals(1, summary.days.first { it.epochDay == day("2026-11-17").toEpochDay() }.workouts)
        // Dates outside today through two days ago are rejected by repository validation.
        assertThrows(IllegalArgumentException::class.java) { runBlocking { repo.addManualActivity(day("2026-11-14"), 10, day("2026-11-17"), zone) } }
        // Earlier Activity activation does not narrow the supported rolling calendar window.
        val withinWindowBeforeActivation = repo.addManualActivity(day("2026-11-15"), 10, day("2026-11-17"), zone)
        repo.deleteManualActivity(withinWindowBeforeActivation)
        repo.deleteManualActivity(todayId)
        repo.deleteManualActivity(twoDaysAgoId)
        repo.deleteManualActivity(id)
        assertEquals(0, repo.snapshot(day("2026-11-17"), zone, now=7).totals.sessions)
        assertEquals("2026-11-12", db.activityDao().settings()!!.firstUseDay)
    } }

    @Test fun completedBetaWeekCanGrantCosmeticWithoutAdvancingOfficialStreak() = runBlocking { withRepository { db, repo ->
        val betaMonday = day("2026-11-09")
        repo.initialize(day("2026-11-12"), zone, now=1)
        addPerfectWeek(db, betaMonday, "beta-cosmetic")

        val officialMonday = day("2026-11-16")
        val official = repo.snapshot(officialMonday, zone, now=2)
        assertFalse(official.week.beta)
        assertEquals(1, db.activityDao().grants().count { it.weekMonday == betaMonday.toString() && it.result == "COSMETIC" })
        assertEquals("The Beta Week did not enter the Freeze milestone", 0, official.freezes.successfulWeeksTowardFreeze)
        assertEquals("Existing session rows remain untouched", 3, db.activityDao().allManualEntries().size)
    } }

    @Test fun activeBetaWeekWithSixSessionsAndNinetySixMinutesImmediatelyGrantsOneCosmetic() = runBlocking { withRepository { db, repo ->
        val today = day("2026-11-12")
        val betaMonday = day("2026-11-09")
        repo.initialize(today, zone, now=1)
        db.activityDao().saveGoal(ActivityWeeklyGoal(betaMonday.toString(), requiredSessions=3, minutesGoal=90))
        repeat(6) { index ->
            val activityDay = betaMonday.plusDays((index % 4).toLong())
            db.activityDao().addManualEntry(ManualActivityEntry(
                "beta-6-of-3-$index", activityDay.toString(), zone.id, 16 * 60_000L, index.toLong() + 10,
            ))
        }

        val fulfilled = repo.snapshot(today, zone, now=2)
        assertTrue("Beta Week remains active", fulfilled.week.beta)
        assertEquals(6, fulfilled.totals.sessions)
        assertEquals(96L, fulfilled.totals.minutes)
        assertEquals(3, fulfilled.goal.sessions)
        assertEquals(90, fulfilled.goal.minutes)
        assertEquals("Exactly one persisted cosmetic grant is created", 1, db.activityDao().grants().size)
        assertEquals(betaMonday.toString(), repo.pendingReward.first()?.weekMonday)
        assertEquals("The Beta week does not enter Freeze progress", 0, fulfilled.freezes.successfulWeeksTowardFreeze)

        val repeated = repo.snapshot(today, zone, now=3)
        assertEquals(1, db.activityDao().grants().size)
        assertEquals(betaMonday.toString(), repo.pendingReward.first()?.weekMonday)
        assertEquals(0, repeated.freezes.successfulWeeksTowardFreeze)
    } }

    @Test fun manualAndRecordedWorkoutsShareTotalsStreakAndSurviveDatabaseReload() = runBlocking {
        val name = "activity-combined-${UUID.randomUUID()}.db"
        val today = day("2026-11-17")
        var db = Room.databaseBuilder(context, OpusDatabase::class.java, name).build()
        try {
            var repo = ActivityRepository(db)
            repo.initialize(today, zone, now=1)
            repo.addManualActivity(day("2026-11-15"), 20, today, zone, now=2)
            repo.addManualActivity(day("2026-11-16"), 25, today, zone, now=3)
            repo.addManualActivity(today, 10, today, zone, now=4)
            fun practice(date: LocalDate, minutes: Long): PracticeSession {
                val instant = date.atTime(12, 0).atZone(zone).toInstant().toEpochMilli()
                return PracticeSession(pieceId=null, pieceTitle="Combined fixture", startedAt=instant-minutes*60_000,
                    completedAt=instant, durationMillis=minutes*60_000, practiceDay=date.toString(), practiceZone=zone.id)
            }
            db.dao().insert(practice(day("2026-11-16"), 15))
            db.dao().insert(practice(today, 5))
            val combined = repo.snapshot(today, zone, now=5)
            assertEquals(5, combined.allTimeWorkouts)
            assertEquals(75L, combined.allTimeMinutes)
            assertEquals(75 * 60_000L, combined.allTimeDurationMillis)
            assertEquals(15 * 60_000L, combined.todayDurationMillis)
            assertEquals(3, combined.dailyStreak)

            db.close()
            db = Room.databaseBuilder(context, OpusDatabase::class.java, name).build()
            repo = ActivityRepository(db)
            val restarted = repo.snapshot(today, zone, now=6)
            assertEquals(combined.allTimeWorkouts, restarted.allTimeWorkouts)
            assertEquals(combined.allTimeDurationMillis, restarted.allTimeDurationMillis)
            assertEquals(combined.dailyStreak, restarted.dailyStreak)
        } finally {
            db.close()
            context.deleteDatabase(name)
        }
    }

    @Test fun observedWeekKeepsItsTimeZoneUntilTheFollowingMonday() = runBlocking { withRepository { db, repo ->
        val monday = day("2026-10-19")
        val prague = ZoneId.of("Europe/Prague")
        val honolulu = ZoneId.of("Pacific/Honolulu")
        repo.snapshot(day("2026-10-19"), prague, now=1)
        val sameWeek = repo.snapshot(day("2026-10-23"), honolulu, now=2)
        assertEquals("Europe/Prague", sameWeek.week.zoneId)
        assertEquals("Europe/Prague", db.activityDao().week(monday.toString())!!.zoneId)
        val nextWeek = repo.snapshot(day("2026-10-26"), honolulu, now=3)
        assertEquals("Pacific/Honolulu", nextWeek.week.zoneId)
        assertEquals(7, com.opus.piano.activity.domain.Week(monday.toEpochDay(), "Europe/Prague").dates().size)
    } }

    @Test fun yearHeatmapProjectionIncludesLegacySessionsAndSelectedManualDates() = runBlocking { withRepository { db, repo ->
        val firstUse = day("2026-11-12")
        repo.initialize(firstUse, zone, now=1)
        fun session(sessionDay: LocalDate): PracticeSession {
            val timestamp = sessionDay.atTime(12, 0).atZone(zone).toInstant().toEpochMilli()
            return PracticeSession(pieceId=null, pieceTitle="Heatmap fixture", startedAt=timestamp-60_000,
                completedAt=timestamp, durationMillis=60_000, practiceDay=sessionDay.toString(), practiceZone=zone.id)
        }
        db.dao().insert(session(day("2026-11-11"))) // Existing journal history before Activity activation.
        db.dao().insert(session(day("2026-11-15")))
        repo.addManualActivity(day("2026-11-16"), 20, day("2026-11-17"), zone, now=2)
        val days = repo.yearActivity(2026, day("2026-11-17"), zone).associateBy { LocalDate.ofEpochDay(it.epochDay) }
        assertEquals(1, days[day("2026-11-11")]?.workouts)
        assertEquals(1, days[day("2026-11-15")]?.workouts)
        assertEquals(1, days[day("2026-11-16")]?.workouts)
        assertEquals(1L, days[day("2026-11-15")]?.minutes)
        assertEquals(20L, days[day("2026-11-16")]?.minutes)
    } }

    @Test fun retrospectiveCorrectionRecomputesWeeksAndNeverConsumesFreezeTwice() = runBlocking { withRepository { _, repo ->
        repo.snapshot(day("2026-11-09"), zone, now=1) // Monday start: not Beta Week.
        val ids = (0..2).map { repo.addManualActivity(day("2026-11-09"), 10, day("2026-11-10"), zone, now=2L+it) }
        val closed = repo.snapshot(day("2026-11-16"), zone, now=10)
        assertEquals(1, closed.currentWeeklyStreak)
        assertEquals(1, closed.freezes.balance)
        repo.deleteManualActivity(ids.first())
        val corrected = repo.snapshot(day("2026-11-16"), zone, now=11)
        assertEquals(0, corrected.currentWeeklyStreak)
        assertEquals(0, corrected.freezes.balance)
        val repeated = repo.snapshot(day("2026-11-16"), zone, now=12)
        assertEquals(corrected.freezes, repeated.freezes)
    } }

    @Test fun onePerfectWeekCreatesItsOwnClaimableCosmeticAndClaimPersists() = runBlocking { withRepository { db, repo ->
        repo.snapshot(day("2026-11-16"), zone, now=1)
        repo.changeGoals(1, 30, day("2026-11-16"), zone)
        val perfectMonday = day("2026-11-23")
        addPerfectWeek(db, perfectMonday, "one-perfect-week")
        repo.snapshot(perfectMonday, zone, now=2)
        assertTrue("The active week is not yet eligible", db.activityDao().grants().isEmpty())
        assertNull(repo.pendingReward.first())
        assertNull(repo.claimedReward.first())

        repo.snapshot(day("2026-11-30"), zone, now=3)
        val grant=db.activityDao().grants().single()
        assertEquals("COSMETIC",grant.result)
        assertEquals("2026-11-23", grant.weekMonday)
        assertTrue(db.activityDao().allOwnedCosmetics().isEmpty())
        assertEquals(grant.cosmeticId, repo.pendingReward.first()?.cosmeticId)
        repo.snapshot(day("2026-11-30"),zone,now=4)
        assertEquals(1,db.activityDao().grants().size)
        assertFalse(db.activityDao().owns(checkNotNull(grant.cosmeticId)))
        assertTrue(repo.claimReward(grant.weekMonday, now=5))
        assertTrue(db.activityDao().owns(checkNotNull(grant.cosmeticId)))
        assertNull(repo.pendingReward.first())
        assertEquals(grant.weekMonday, repo.claimedReward.first()?.weekMonday)
        assertFalse(repo.claimReward(grant.weekMonday, now=6))
        assertEquals(5L, db.activityDao().grant(grant.weekMonday)?.acknowledgedAt)
        db.activityDao().deleteManualEntry("one-perfect-week-0")
        repo.snapshot(day("2026-11-30"),zone,now=7)
        assertEquals(1,db.activityDao().allOwnedCosmetics().size)
        assertEquals(grant.cosmeticId,db.activityDao().allOwnedCosmetics().single().cosmeticId)
    } }

    @Test fun fourCompletedPerfectWeeksRecoverRewardsExactlyOnceAcrossDatabaseReload() = runBlocking {
        val name = "activity-reward-${UUID.randomUUID()}"
        var db = Room.databaseBuilder(context, OpusDatabase::class.java, name).build()
        try {
            var repo = ActivityRepository(db)
            repo.snapshot(day("2026-11-09"), zone, now=1)
            val completedWeeks = listOf("2026-11-09", "2026-11-16", "2026-11-23", "2026-11-30")
            completedWeeks.forEachIndexed { weekIndex, monday -> addPerfectWeek(db, day(monday), "perfect-$weekIndex") }
            // A fifth closed week with only two sessions is not perfect and earns nothing.
            repeat(2) { offset ->
                db.activityDao().addManualEntry(ManualActivityEntry(
                    "partial-$offset", day("2026-12-07").plusDays(offset.toLong()).toString(), zone.id,
                    20 * 60_000L, 100L + offset,
                ))
            }
            repo.snapshot(day("2026-12-14"), zone, now=200)
            val initialGrants = db.activityDao().grants()
            assertEquals(4, initialGrants.size)
            assertEquals(completedWeeks.toSet(), initialGrants.map { it.weekMonday }.toSet())
            assertTrue("Eligibility creates pending claims, not ownership", db.activityDao().allOwnedCosmetics().isEmpty())
            assertEquals(initialGrants.first().weekMonday, repo.pendingReward.first()?.weekMonday)
            assertEquals(4, initialGrants.mapNotNull { it.cosmeticId }.toSet().size)
            repo.snapshot(day("2026-12-14"), zone, now=201)
            assertEquals(initialGrants, db.activityDao().grants())

            db.close()
            db = Room.databaseBuilder(context, OpusDatabase::class.java, name).build()
            repo = ActivityRepository(db)
            repo.snapshot(day("2026-12-14"), zone, now=202)
            assertEquals(initialGrants, db.activityDao().grants())
            assertEquals(initialGrants.first().weekMonday, repo.pendingReward.first()?.weekMonday)
            val reward = initialGrants.first()
            assertFalse(db.activityDao().owns(checkNotNull(reward.cosmeticId)))
            assertTrue(repo.claimReward(reward.weekMonday, now=300L))
            assertTrue(db.activityDao().owns(checkNotNull(reward.cosmeticId)))
            assertFalse("A claimed week cannot be claimed twice", repo.claimReward(reward.weekMonday, now=400L))
            assertEquals(initialGrants[1].weekMonday, repo.pendingReward.first()?.weekMonday)
            assertEquals(reward.weekMonday, repo.claimedReward.first()?.weekMonday)

            db.close()
            db = Room.databaseBuilder(context, OpusDatabase::class.java, name).build()
            repo = ActivityRepository(db)
            assertEquals("Other weeks stay pending after restart", initialGrants[1].weekMonday, repo.pendingReward.first()?.weekMonday)
            assertEquals(1, db.activityDao().allOwnedCosmetics().size)
            assertEquals(reward.weekMonday, repo.claimedReward.first()?.weekMonday)
            assertFalse(repo.claimReward(reward.weekMonday, now=500L))
        } finally {
            db.close()
            context.deleteDatabase(name)
        }
    }

    @Test fun fourPerfectWeeksGrantFourCosmeticsAndExactlyOneAdditionalFreeze() = runBlocking { withRepository { db, repo ->
        repo.snapshot(day("2026-11-09"), zone, now=1)
        listOf("2026-11-09", "2026-11-16", "2026-11-23", "2026-11-30")
            .forEachIndexed { index, monday -> addPerfectWeek(db, day(monday), "milestone-$index") }

        val completed = repo.snapshot(day("2026-12-07"), zone, now=10)
        assertEquals(4, db.activityDao().grants().size)
        assertTrue(db.activityDao().grants().all { it.result == "COSMETIC" })
        assertEquals(4, db.activityDao().grants().mapNotNull { it.cosmeticId }.toSet().size)
        assertEquals("One separate milestone Freeze is added to the initial balance", 2, completed.freezes.balance)
        assertEquals(0, completed.freezes.successfulWeeksTowardFreeze)

        val repeated = repo.snapshot(day("2026-12-07"), zone, now=11)
        assertEquals(4, db.activityDao().grants().size)
        assertEquals(2, repeated.freezes.balance)
        assertEquals(0, repeated.freezes.successfulWeeksTowardFreeze)
    } }

    @Test fun brokenPerfectWeekRunResetsOnlyFreezeProgressAndKeepsEarlierCosmeticsClaimable() = runBlocking { withRepository { db, repo ->
        repo.snapshot(day("2026-11-09"), zone, now=1)
        listOf("2026-11-09", "2026-11-16", "2026-11-30", "2026-12-07", "2026-12-14")
            .forEachIndexed { index, monday -> addPerfectWeek(db, day(monday), "broken-run-$index") }

        val afterThreeSinceBreak = repo.snapshot(day("2026-12-21"), zone, now=10)
        assertEquals(5, db.activityDao().grants().size)
        assertEquals(0, afterThreeSinceBreak.freezes.balance)
        assertEquals(3, afterThreeSinceBreak.freezes.successfulWeeksTowardFreeze)
        val originalPending = repo.pendingReward.first()
        assertEquals("2026-11-09", originalPending?.weekMonday)

        assertTrue(repo.claimReward(checkNotNull(originalPending).weekMonday, now=11))
        assertEquals("2026-11-16", repo.pendingReward.first()?.weekMonday)
        assertEquals("2026-11-09", repo.claimedReward.first()?.weekMonday)
        assertEquals(5, db.activityDao().grants().size)

        addPerfectWeek(db, day("2026-12-21"), "after-break-fourth")
        val afterFourthSinceBreak = repo.snapshot(day("2026-12-28"), zone, now=12)
        assertEquals(6, db.activityDao().grants().size)
        assertEquals(1, afterFourthSinceBreak.freezes.balance)
        assertEquals(0, afterFourthSinceBreak.freezes.successfulWeeksTowardFreeze)
        val repeated = repo.snapshot(day("2026-12-28"), zone, now=13)
        assertEquals(6, db.activityDao().grants().size)
        assertEquals(1, repeated.freezes.balance)
    } }

    @Test fun viewModelRechecksClosedWeekRewardsWhenActivityChangesAfterScreenLoad() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, OpusDatabase::class.java).build()
        val today = LocalDate.now(zone)
        val currentMonday = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
        val firstUse = currentMonday.minusWeeks(6)
        val repo = ActivityRepository(db)
        val store = androidx.lifecycle.ViewModelStore()
        try {
            repo.initialize(firstUse, zone, now=1)
            val model = ActivityViewModel(repo)
            store.put("activity", model)
            kotlinx.coroutines.withTimeout(15_000) { model.state.first { it.snapshot != null } }
            assertTrue(db.activityDao().grants().isEmpty())
            (1L..4L).forEachIndexed { weekIndex, offset -> addPerfectWeek(db, currentMonday.minusWeeks(offset), "reactive-$weekIndex") }
            kotlinx.coroutines.withTimeout(15_000) { model.state.first { db.activityDao().grants().size == 4 } }
            val reward = kotlinx.coroutines.withTimeout(15_000) { model.state.first { it.pendingReward != null }.pendingReward!! }
            assertEquals("COSMETIC", reward.result)
            assertFalse("The available reward stays unowned until claimed", db.activityDao().owns(checkNotNull(reward.cosmeticId)))
            assertEquals(4, db.activityDao().grants().size)
            assertTrue(db.activityDao().grants().all { it.result == "COSMETIC" && it.cosmeticId != null })
            assertTrue(model.claimReward(reward.weekMonday))
            assertTrue(db.activityDao().owns(checkNotNull(reward.cosmeticId)))
            kotlinx.coroutines.withTimeout(15_000) { model.state.first { it.claimedReward?.weekMonday == reward.weekMonday } }
            assertTrue(model.state.value.rewardStateLoaded)
            assertNotNull("Other weeks remain independently claimable", model.state.value.pendingReward)
            assertNotEquals(reward.weekMonday, model.state.value.pendingReward?.weekMonday)
            assertFalse(model.claimReward(reward.weekMonday))
        } finally {
            store.clear()
            db.close()
        }
    }

    @Test fun cosmeticApplyRequiresOwnershipAndPersistsLoadoutAcrossDatabaseReload() = runBlocking {
        val name = "activity-cosmetic-${UUID.randomUUID()}.db"
        var db = Room.databaseBuilder(context, OpusDatabase::class.java, name).build()
        val cosmetic = ActivityCosmetics.all.first()
        try {
            var repo = ActivityRepository(db)
            assertThrows(IllegalArgumentException::class.java) {
                runBlocking { repo.changeLoadout(cosmetic.category.name, cosmetic.id) }
            }
            db.activityDao().own(CosmeticOwnership(cosmetic.id, 1L, "2026-11-09"))
            repo.changeLoadout(cosmetic.category.name, cosmetic.id)
            assertEquals(cosmetic.id, db.activityDao().allLoadout().single().cosmeticId)

            db.close()
            db = Room.databaseBuilder(context, OpusDatabase::class.java, name).build()
            repo = ActivityRepository(db)
            assertTrue(db.activityDao().owns(cosmetic.id))
            assertEquals(cosmetic.id, db.activityDao().allLoadout().single().cosmeticId)
            repo.changeLoadout(cosmetic.category.name, null)
            assertTrue(db.activityDao().allLoadout().isEmpty())
        } finally {
            db.close()
            context.deleteDatabase(name)
        }
    }

    @Test fun exhaustedCosmeticCollectionProducesSafeNonDuplicateResult() = runBlocking { withRepository { db, repo ->
        repo.snapshot(day("2026-11-16"),zone,now=1)
        repo.changeGoals(1,20,day("2026-11-16"),zone)
        ActivityCosmetics.all.forEachIndexed { index, item -> db.activityDao().own(CosmeticOwnership(item.id,index+1L,null)) }
        listOf("2026-11-23", "2026-11-30", "2026-12-07", "2026-12-14").forEachIndexed { index, monday ->
            addPerfectWeek(db, day(monday), "complete-$index")
        }
        // A completed collection is recorded for each eligible perfect week.
        repo.snapshot(day("2026-12-21"),zone,now=3)
        assertEquals(4, db.activityDao().grants().size)
        assertTrue(db.activityDao().grants().all { it.result == "COLLECTION_COMPLETE" })
        assertTrue(db.activityDao().grants().all { it.cosmeticId == null && it.acknowledgedAt > 0L })
        assertNull(repo.pendingReward.first())
        assertEquals(ActivityCosmetics.all.size,db.activityDao().allOwnedCosmetics().size)
    } }

    @Test fun legacyAutomaticallyOwnedRewardIsNotOfferedForASecondClaim() = runBlocking { withRepository { db, repo ->
        val cosmetic = ActivityCosmetics.all.first()
        val week = "2026-11-09"
        db.activityDao().own(CosmeticOwnership(cosmetic.id, 1L, week))
        db.activityDao().grant(ActivityRewardGrant(week, cosmetic.id, "COSMETIC", 1L))
        assertNull(repo.pendingReward.first())
        assertFalse(repo.claimReward(week, now=2L))
        assertTrue(db.activityDao().owns(cosmetic.id))
        assertEquals(2L, db.activityDao().grant(week)?.acknowledgedAt)
    } }
}
