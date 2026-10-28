package com.opus.piano.ui

import com.opus.piano.i18n.L
import com.opus.piano.R
import android.os.SystemClock
import androidx.lifecycle.*
import com.opus.piano.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LibraryState(val pieces: List<Piece> = emptyList(), val sessions: List<PracticeSession> = emptyList(), val loading: Boolean = true, val parts: List<SessionPart> = emptyList(), val summary: JournalSummary = JournalSummary(), val links:List<SessionPieceLink> = emptyList())
typealias TimerState = com.opus.piano.practice.PracticeState

class OpusViewModel(private val repository: OpusRepository, private val saved: SavedStateHandle, val practice: com.opus.piano.practice.PracticeCoordinator, val recording: com.opus.piano.audio.RecordingController, private val appContext: android.content.Context) : ViewModel() {
    val library = repository.journal.map { (pieces, sessions, parts, links) -> LibraryState(pieces, sessions, false, parts, traceOperation("Journal.summary") { summarizeJournal(sessions, parts) },links) }
        .distinctUntilChanged().flowOn(kotlinx.coroutines.Dispatchers.Default)
        .catch { error ->
            if (error is kotlinx.coroutines.CancellationException) throw error
            practice.notice.value = L.s(R.string.msg_saved_journal_could_not_be_loaded_restart_go_cfce)
            emit(LibraryState(loading = false))
        }.stateIn(viewModelScope, SharingStarted.Eagerly, LibraryState())
    val timer = practice.state
    val message = practice.notice
    val recordingState = recording.state
    fun startRecording() = viewModelScope.launch {
        try {
            val t = practice.state.value
            val title = t.selected?.title ?: L.s(R.string.msg_recording_title)
            val composer = t.selected?.composer ?: ""
            val rate = com.opus.piano.audio.RecordingController.selectRate()
            recording.start(rate, title, composer, t.token.takeIf { it.isNotBlank() })
            com.opus.piano.service.SmartPracticeService.startRecording(appContext)
        } catch (c: kotlinx.coroutines.CancellationException) { throw c }
        catch (_: Exception) { practice.notice.value = L.s(R.string.msg_record_error) }
    }
    fun stopRecording() = viewModelScope.launch {
        try { recording.stop() }
        catch (c: kotlinx.coroutines.CancellationException) { throw c }
        catch (_: Exception) { practice.notice.value = L.s(R.string.msg_recording_interrupted) }
    }
    fun select(piece: Piece?) = practice.command { select(piece) }
    fun selectPart(index: Int) { timer.value.parts.getOrNull(index)?.let { p -> select(p.pieceId?.let { Piece(it, p.title, p.composer, catalogWorkId = p.catalogWorkId, musicalEra = p.musicalEra) }) } }
    fun remove(index: Int) = practice.command { remove(index) }
    fun note(index: Int, text: String) = practice.command { note(index, text) }
    fun focus(piece: Piece): Boolean { if (timer.value.saving || timer.value.status == "Review") return false; select(piece); return true }
    fun toggle() = practice.command { if (state.value.running) pause() else start(false) }
    fun reset() = practice.command { reset() }
    fun finish() = practice.command { review() }
    fun saveSession() = practice.command { save() }
    fun save(piece: Piece, done: (Boolean) -> Unit) = viewModelScope.launch {
        try { repository.save(piece.copy(title = piece.title.trim(), composer = piece.composer.trim(), notes = piece.notes.trim())); done(true) }
        catch (c: kotlinx.coroutines.CancellationException) { throw c }
        catch (_: Exception) { practice.notice.value = L.s(R.string.msg_could_not_save_piece_please_try_again_1474); done(false) }
    }
    fun delete(piece: Piece) = viewModelScope.launch {
        try { repository.delete(piece) } catch (c: kotlinx.coroutines.CancellationException) { throw c }
        catch (_: Exception) { practice.notice.value = L.s(R.string.msg_could_not_delete_piece_please_try_again_a18f) }
    }
    fun dismissMessage() { practice.notice.value = null }
}
