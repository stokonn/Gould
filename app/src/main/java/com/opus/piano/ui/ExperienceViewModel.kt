package com.opus.piano.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opus.piano.data.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ExperienceViewModel(private val store: AchievementStore, private val recordingRepository: RecordingRepository) : ViewModel() {
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    private val _notice = MutableStateFlow<String?>(null)
    val notice = _notice.asStateFlow()
    private val _recordingError = MutableStateFlow<String?>(null)
    val recordingError = _recordingError.asStateFlow()
    private val _recordingsLoading = MutableStateFlow(true)
    val recordingsLoading = _recordingsLoading.asStateFlow()
    private val badgeRevision = MutableStateFlow(0)
    private val recordingRevision = MutableStateFlow(0)
    val badges = badgeRevision.flatMapLatest {
        store.badges.catch { _error.value = "Achievements could not be loaded. Try again."; emit(emptyList()) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val recordings = recordingRevision.flatMapLatest {
        recordingRepository.recordings.onStart { _recordingsLoading.value = true }
            .onEach { _recordingsLoading.value = false; _recordingError.value = null }
            .catch { _recordingsLoading.value = false; _recordingError.value = "Saved interpretations could not be loaded. Try again."; emit(emptyList()) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    init { viewModelScope.launch { kotlinx.coroutines.delay(750); retryEvaluation() } }
    fun retryEvaluation() = viewModelScope.launch {
        try { store.evaluate(); _error.value = null; badgeRevision.update { it + 1 } }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { _error.value = "Achievements could not be verified. Your practice and repertoire are preserved. Try again." }
    }
    fun retryRecordings() { recordingRevision.update { it + 1 } }
    fun saveRecording(recording: SavedRecording, done: (String?) -> Unit) = viewModelScope.launch {
        try { recordingRepository.save(recording); done(null) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (error: Exception) { done(if (error is IllegalArgumentException) error.message else "Could not save this link. Please try again.") }
    }
    fun prefer(id: Long) = viewModelScope.launch {
        try { recordingRepository.prefer(id) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { _notice.value = "Could not change the preferred interpretation. Please try again." }
    }
    fun removeRecording(id: Long) = viewModelScope.launch {
        try { recordingRepository.remove(id) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { _notice.value = "Could not remove this interpretation. Please try again." }
    }
    fun dismissNotice() { _notice.value = null }
}
