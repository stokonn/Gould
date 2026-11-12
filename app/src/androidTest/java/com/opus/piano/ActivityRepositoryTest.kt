package com.opus.piano

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.activity.domain.DayKind
import com.opus.piano.data.*
import kotlinx.coroutines.runBlocking
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
        val summary = repo.snapshot(day("2026-11-17"), zone, now=5)
        assertEquals(1, summary.totals.sessions)
        assertEquals(25L, summary.totals.minutes)
        assertEquals(DayKind.TRAINING, summary.days.first { it.epochDay == day("2026-11-16").toEpochDay() }.kind)
        assertThrows(IllegalArgumentException::class.java) { runBlocking { repo.addManualActivity(day("2026-11-17"), 10, day("2026-11-17"), zone) } }
        assertThrows(IllegalArgumentException::class.java) { runBlocking { repo.addManualActivity(day("2026-11-15"), 10, day("2026-11-17"), zone) } }
        repo.deleteManualActivity(id)
        assertEquals(0, repo.snapshot(day("2026-11-17"), zone, now=6).totals.sessions)
        assertEquals("2026-11-12", db.activityDao().settings()!!.firstUseDay)
    } }

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

    @Test fun weeklyCosmeticRewardIsDurableAndIdempotentAfterCorrection() = runBlocking { withRepository { db, repo ->
        repo.snapshot(day("2026-11-16"), zone, now=1)
        repo.changeGoals(1, 30, day("2026-11-16"), zone)
        val id=repo.addManualActivity(day("2026-11-23"),40,day("2026-11-24"),zone,now=2)
        repo.snapshot(day("2026-11-24"),zone,now=3)
        val grant=db.activityDao().grants().single()
        assertEquals("COSMETIC",grant.result)
        assertEquals(1,db.activityDao().allOwnedCosmetics().size)
        repo.snapshot(day("2026-11-24"),zone,now=4)
        assertEquals(1,db.activityDao().grants().size)
        assertEquals("2026-11-23",grant.weekMonday)
        repo.deleteManualActivity(id)
        repo.snapshot(day("2026-11-24"),zone,now=5)
        assertEquals(1,db.activityDao().allOwnedCosmetics().size)
        assertEquals(grant.cosmeticId,db.activityDao().allOwnedCosmetics().single().cosmeticId)
    } }

    @Test fun exhaustedCosmeticCollectionProducesSafeNonDuplicateResult() = runBlocking { withRepository { db, repo ->
        repo.snapshot(day("2026-11-16"),zone,now=1)
        repo.changeGoals(1,20,day("2026-11-16"),zone)
        ActivityCosmetics.all.forEachIndexed { index, item -> db.activityDao().own(CosmeticOwnership(item.id,index+1L,null)) }
        repo.addManualActivity(day("2026-11-23"),20,day("2026-11-24"),zone,now=2)
        repo.snapshot(day("2026-11-24"),zone,now=3)
        assertEquals("COLLECTION_COMPLETE",db.activityDao().grants().single().result)
        assertNull(db.activityDao().grants().single().cosmeticId)
        assertEquals(ActivityCosmetics.all.size,db.activityDao().allOwnedCosmetics().size)
    } }
}
