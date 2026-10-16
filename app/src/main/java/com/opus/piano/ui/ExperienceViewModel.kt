package com.opus.piano.ui

import com.opus.piano.i18n.L
import com.opus.piano.R
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
        store.badges.catch { _error.value = L.s(R.string.msg_achievements_could_not_be_loaded_try_again_3d6d); emit(emptyList()) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val recordings = recordingRevision.flatMapLatest {
        recordingRepository.recordings.onStart { _recordingsLoading.value = true }
            .onEach { _recordingsLoading.value = false; _recordingError.value = null }
            .catch { _recordingsLoading.value = false; _recordingError.value = L.s(R.string.recordings_load_error); emit(emptyList()) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    init { viewModelScope.launch { kotlinx.coroutines.delay(750); retryEvaluation() } }
    fun retryEvaluation() = viewModelScope.launch {
        try { store.evaluate(); _error.value = null; badgeRevision.update { it + 1 } }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { _error.value = L.s(R.string.msg_achievements_could_not_be_verified_your_prac_b6c3) }
    }
    fun retryRecordings() { recordingRevision.update { it + 1 } }
    fun saveRecording(recording: SavedRecording, done: (String?) -> Unit) = viewModelScope.launch {
        try { recordingRepository.save(recording); done(null) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (error: Exception) { done(if (error is IllegalArgumentException) error.message else L.s(R.string.recordings_save_error)) }
    }
    fun prefer(id: Long) = viewModelScope.launch {
        try { recordingRepository.prefer(id) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { _notice.value = L.s(R.string.msg_could_not_change_the_preferred_interpretatio_d21c) }
    }
    fun removeRecording(id: Long) = viewModelScope.launch {
        try { recordingRepository.remove(id) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { _notice.value = L.s(R.string.msg_could_not_remove_this_interpretation_please_0553) }
    }
    fun dismissNotice() { _notice.value = null }
}
