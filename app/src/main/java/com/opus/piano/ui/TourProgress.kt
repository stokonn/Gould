package com.opus.piano.ui

import com.opus.piano.data.TourRules

/** Pure progress rules shared by the persisted tour state and UI. */
internal object TourProgress {
    val stepIds = TourRules.stepIds

    fun resumeIndex(completedSteps: Set<String>): Int =
        stepIds.indexOfFirst { it !in completedSteps }.let { if (it < 0) stepIds.lastIndex else it }

    fun completedThrough(index: Int, previouslyCompleted: Set<String>): Set<String> =
        previouslyCompleted + stepIds.take(index + 1)
}
