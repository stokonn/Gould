package com.opus.piano

import com.opus.piano.ui.TourProgress
import org.junit.Assert.assertEquals
import org.junit.Test

class TourProgressTest {
    @Test fun resumesAtFirstUncompletedStep() {
        assertEquals(0, TourProgress.resumeIndex(emptySet()))
        assertEquals(2, TourProgress.resumeIndex(setOf("home", "activity")))
        assertEquals(5, TourProgress.resumeIndex(TourProgress.stepIds.toSet()))
    }

    @Test fun progressOnlyMarksStepsTheUserContinuedPast() {
        assertEquals(setOf("home", "activity"), TourProgress.completedThrough(1, emptySet()))
        assertEquals(setOf("home", "activity", "streaks"), TourProgress.completedThrough(2, setOf("home", "activity")))
    }
}
