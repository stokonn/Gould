package com.opus.piano.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

/** Keeps the active topic through configuration changes and Android process recreation. */
internal class TourViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    val index: StateFlow<Int> = savedState.getStateFlow(INDEX_KEY, 0)

    fun resume(replay: Boolean, completedSteps: Set<String>) {
        savedState[INDEX_KEY] = if (replay) 0 else TourProgress.resumeIndex(completedSteps)
    }

    fun continueTo(index: Int) {
        savedState[INDEX_KEY] = index.coerceIn(0, TourProgress.stepIds.lastIndex)
    }

    private companion object { const val INDEX_KEY = "gould.tour.index.v1" }
}
