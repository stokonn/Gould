package com.opus.piano.ui

import android.os.SystemClock
import androidx.lifecycle.*
import com.opus.piano.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LibraryState(val pieces: List<Piece> = emptyList(), val sessions: List<PracticeSession> = emptyList(), val loading: Boolean = true, val parts: List<SessionPart> = emptyList())
data class TimerState(val elapsed: Long = 0, val running: Boolean = false, val selected: Piece? = null, val saving: Boolean = false,
    val parts: List<SessionPart> = listOf(SessionPart()), val active: Int = 0, val removed: Set<Long> = emptySet())

class OpusViewModel(private val repository: OpusRepository, private val saved: SavedStateHandle) : ViewModel() {
    val library = combine(repository.pieces, repository.sessions, repository.parts) { pieces, sessions, parts -> LibraryState(pieces, sessions, false, parts) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LibraryState())
    private fun restored(): TimerState = runCatching {
        val raw = saved.get<String>("timerParts")
        if (raw == null) {
            val old = saved.get<Long>("pieceId")
            val elapsed = saved.get<Long>("elapsed") ?: 0L
            TimerState(elapsed = elapsed, parts = listOf(SessionPart(pieceId = old, title = saved["pieceTitle"] ?: "Free practice", composer = saved["pieceComposer"] ?: "", catalogWorkId = saved["pieceCatalogId"], musicalEra = saved["pieceEra"] ?: "", durationMillis = elapsed)))
        } else {
            val array = org.json.JSONArray(raw)
            val parts = (0 until array.length()).map { i -> val o = array.getJSONObject(i)
                SessionPart(pieceId = if (o.isNull("pieceId")) null else o.getLong("pieceId"), title = o.getString("title"), composer = o.getString("composer"), catalogWorkId = o.optString("catalog").takeIf { it.isNotEmpty() }, musicalEra = o.optString("era"), durationMillis = o.getLong("time"), notes = o.getString("notes")) }
            TimerState(elapsed = parts.sumOf { it.durationMillis }, parts = parts, active = (saved.get<Int>("active") ?: 0).coerceIn(parts.indices), removed = saved.get<LongArray>("removed")?.toSet().orEmpty())
        }
    }.getOrElse { TimerState() }
    private val _timer = MutableStateFlow(restored())
    val timer = _timer.asStateFlow()
    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()
    private var runStart = 0L
    private var tickJob: kotlinx.coroutines.Job? = null
    private var sessionStart: Long = saved["sessionStart"] ?: 0L
    private fun checkpoint() {
        val t = _timer.value
        saved["timerParts"] = org.json.JSONArray().apply { t.parts.forEach { p -> put(org.json.JSONObject().apply {
            put("pieceId", p.pieceId ?: org.json.JSONObject.NULL); put("title", p.title); put("composer", p.composer); put("catalog", p.catalogWorkId.orEmpty()); put("era", p.musicalEra); put("time", p.durationMillis); put("notes", p.notes)
        }) } }.toString()
        saved["elapsed"] = t.elapsed; saved["active"] = t.active; saved["removed"] = t.removed.toLongArray()
    }
    private fun settle() {
        if (!_timer.value.running) return
        val now = SystemClock.elapsedRealtime()
        val delta = (now - runStart).coerceAtLeast(0)
        runStart = now
        _timer.update { val parts = charge(it.parts, it.active, delta); it.copy(parts = parts, elapsed = parts.sumOf { p -> p.durationMillis }) }
        checkpoint()
    }
    fun select(piece: Piece?) {
        if (_timer.value.saving) return
        settle()
        val t = _timer.value
        val index = t.parts.indexOfFirst { it.pieceId == piece?.id }
        val parts = if (index >= 0) t.parts else t.parts + SessionPart(pieceId = piece?.id, title = piece?.title ?: "Free practice", composer = piece?.composer.orEmpty(), catalogWorkId = piece?.catalogWorkId, musicalEra = piece?.musicalEra.orEmpty())
        _timer.value = t.copy(selected = piece, parts = parts, active = if (index >= 0) index else parts.lastIndex, removed = t.removed - (piece?.id ?: 0L))
        checkpoint()
    }
    fun selectPart(index: Int) {
        val p = _timer.value.parts.getOrNull(index) ?: return
        select(p.pieceId?.let { Piece(it, p.title, p.composer, catalogWorkId = p.catalogWorkId, musicalEra = p.musicalEra) })
    }
    fun remove(index: Int) {
        if (_timer.value.saving || _timer.value.parts.getOrNull(index)?.pieceId == null) return
        if (_timer.value.active == index) select(null) else settle()
        _timer.update { it.copy(removed = it.removed + it.parts[index].pieceId!!) }; checkpoint()
    }
    fun note(index: Int, text: String) {
        if (_timer.value.saving) return
        _timer.update { it.copy(parts = it.parts.mapIndexed { i, p -> if (i == index) p.copy(notes = text.take(10000)) else p }) }; checkpoint()
    }
    fun focus(piece: Piece): Boolean { if (_timer.value.saving) return false; select(piece); return true }
    fun toggle() {
        if (_timer.value.saving) return
        if (_timer.value.running) {
            settle(); tickJob?.cancel(); _timer.update { it.copy(running = false) }; checkpoint()
        } else {
            if (sessionStart == 0L) { sessionStart = System.currentTimeMillis(); saved["sessionStart"] = sessionStart }
            runStart = SystemClock.elapsedRealtime(); _timer.update { it.copy(running = true) }
            tickJob = viewModelScope.launch { while (true) { delay(1000); settle() } }
        }
    }
    fun reset() {
        if (_timer.value.saving) return
        tickJob?.cancel(); sessionStart = 0; saved["sessionStart"] = 0L
        _timer.value = TimerState(); checkpoint()
    }
    fun finish() {
        if (_timer.value.saving) return
        if (_timer.value.running) toggle()
        val snapshot = _timer.value
        if (snapshot.elapsed < 1000) { _message.value = "Practice for at least one second before saving."; return }
        _timer.update { it.copy(saving = true) }
        viewModelScope.launch {
            try {
                val parts = snapshot.parts.filter { it.durationMillis > 0 || it.notes.isNotBlank() }
                repository.complete(PracticeSession(pieceId = null, pieceTitle = parts.filter { it.durationMillis > 0 }.joinToString(" · ") { it.title }, startedAt = sessionStart, completedAt = System.currentTimeMillis(), durationMillis = snapshot.elapsed), parts)
                _timer.update { it.copy(saving = false) }; reset()
                _message.value = "Session saved. Every minute matters."
            } catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
            catch (_: Exception) { _timer.update { it.copy(saving = false) }; _message.value = "Could not save session. Your timer is preserved; please try again." }
        }
    }
    fun save(piece: Piece, done: (Boolean) -> Unit) = viewModelScope.launch {
        try { repository.save(piece.copy(title = piece.title.trim(), composer = piece.composer.trim(), notes = piece.notes.trim())); done(true) }
        catch (_: Exception) { _message.value = "Could not save piece. Please try again."; done(false) }
    }
    fun delete(piece: Piece) = viewModelScope.launch {
        try { repository.delete(piece) } catch (_: Exception) { _message.value = "Could not delete piece. Please try again." }
    }
    fun dismissMessage() { _message.value = null }
}
