package com.opus.piano

import com.opus.piano.data.ManualActivityEntry
import com.opus.piano.data.PracticeSession
import com.opus.piano.data.unifiedActivityHistory
import com.opus.piano.data.manualActivityEditDayAllowed
import com.opus.piano.data.activityWorkouts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class UnifiedActivityHistoryTest {
    private val zone = ZoneId.of("UTC")
    private val today = LocalDate.parse("2026-11-20")

    @Test fun unifiedHistoryContainsBothTypesAndOrdersNewestFirst() {
        val session = PracticeSession(id = 1, pieceId = null, pieceTitle = "P", startedAt = 1, completedAt = today.toEpochDay() * 86400000L, durationMillis = 1000, practiceDay = today.toString())
        val manual = ManualActivityEntry("m1", today.minusDays(1).toString(), zone.id, 2000, 2)
        val history = unifiedActivityHistory(listOf(session), listOf(manual), today, zone)
        assertEquals(2, history.size)
        assertTrue(history.first().sourceId.startsWith("practice:") || history.first().epochDay >= history.last().epochDay)
    }

    @Test fun unifiedHistoryDeduplicatesBySourceId() {
        val session = PracticeSession(id = 1, pieceId = null, pieceTitle = "P", startedAt = 1, completedAt = 1000, durationMillis = 1000)
        val history = unifiedActivityHistory(listOf(session, session), emptyList(), today, zone)
        assertEquals(1, history.size)
    }

    @Test fun manualEditDayAllowedKeepsOriginalOrWindow() {
        assertTrue(manualActivityEditDayAllowed("2026-11-18", "2026-11-18", today))
        assertTrue(manualActivityEditDayAllowed("2026-11-18", today.toString(), today))
        assertTrue(!manualActivityEditDayAllowed("2026-11-18", "2026-11-01", today))
    }

    @Test fun activityWorkoutsDistinctSourceIds() {
        val session = PracticeSession(id = 1, pieceId = null, pieceTitle = "P", startedAt = 1, completedAt = today.toEpochDay() * 86400000L, durationMillis = 1000, practiceDay = today.toString(), practiceZone = zone.id)
        val manual = ManualActivityEntry("m1", today.toString(), zone.id, 2000, 2)
        val workouts = activityWorkouts(listOf(session), listOf(manual), today, zone.id)
        assertEquals(2, workouts.distinctBy { it.sourceId }.size)
        assertEquals(2, workouts.size)
    }

    @Test fun thirtyEightDayActivityStreakCountsConsecutiveDays() {
        val todayEpoch = today.toEpochDay()
        val workouts = (0..37).map { offset ->
            com.opus.piano.activity.domain.Workout(
                todayEpoch - offset,
                60_000L,
                "day-$offset",
                com.opus.piano.activity.domain.WorkoutSource.MANUAL_ENTRY,
            )
        }
        assertEquals(38, com.opus.piano.activity.domain.currentActivityStreak(workouts, todayEpoch))
    }

    @Test fun activityStreakStopsAtFirstMissingDay() {
        val todayEpoch = today.toEpochDay()
        val workouts = listOf(
            com.opus.piano.activity.domain.Workout(todayEpoch, 60_000L, "today", com.opus.piano.activity.domain.WorkoutSource.MANUAL_ENTRY),
            com.opus.piano.activity.domain.Workout(todayEpoch - 2, 60_000L, "gap", com.opus.piano.activity.domain.WorkoutSource.MANUAL_ENTRY),
        )
        assertEquals(1, com.opus.piano.activity.domain.currentActivityStreak(workouts, todayEpoch))
    }

    @Test fun weekRolloverRequiredDetectsMondayChange() {
        assertTrue(com.opus.piano.activity.domain.weekRolloverRequired(today.toEpochDay(), today.plusDays(7).toEpochDay()))
        assertFalse(com.opus.piano.activity.domain.weekRolloverRequired(today.toEpochDay(), today.plusDays(1).toEpochDay()))
    }
}
