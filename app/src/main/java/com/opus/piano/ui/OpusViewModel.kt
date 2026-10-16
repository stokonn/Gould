package com.opus.piano.ui

import android.os.SystemClock
import androidx.lifecycle.*
import com.opus.piano.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LibraryState(val pieces: List<Piece> = emptyList(), val sessions: List<PracticeSession> = emptyList(), val loading: Boolean = true, val parts: List<SessionPart> = emptyList(), val summary: JournalSummary = JournalSummary())
typealias TimerState = com.opus.piano.practice.PracticeState

class OpusViewModel(private val repository: OpusRepository, private val saved: SavedStateHandle, val practice: com.opus.piano.practice.PracticeCoordinator) : ViewModel() {
    val library = repository.journal.map { (pieces, sessions, parts) -> LibraryState(pieces, sessions, false, parts, traceOperation("Journal.summary") { summarizeJournal(sessions, parts) }) }
        .distinctUntilChanged().flowOn(kotlinx.coroutines.Dispatchers.Default)
        .catch { error ->
            if (error is kotlinx.coroutines.CancellationException) throw error
            practice.notice.value = "Saved journal could not be loaded. Restart Goldberg to retry. No stored data was removed."
            emit(LibraryState(loading = false))
        }.stateIn(viewModelScope, SharingStarted.Eagerly, LibraryState())
    val timer = practice.state
    val message = practice.notice
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
        catch (_: Exception) { practice.notice.value = "Could not save piece. Please try again."; done(false) }
    }
    fun delete(piece: Piece) = viewModelScope.launch {
        try { repository.delete(piece) } catch (c: kotlinx.coroutines.CancellationException) { throw c }
        catch (_: Exception) { practice.notice.value = "Could not delete piece. Please try again." }
    }
    fun dismissMessage() { practice.notice.value = null }
}
