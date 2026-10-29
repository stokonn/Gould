package com.opus.piano.practice

import com.opus.piano.i18n.L
import com.opus.piano.R
import com.opus.piano.data.*

data class QueueIntent(val active: Int, val order: List<Int>, val removed: Set<Long>)

data class PracticeState(
    val token: String = "", val execution: Long = 0, val startedAt: Long = 0, val elapsed: Long = 0, val wallElapsed: Long = 0,
    val detectedActive: Long = 0, val mode: String = "Manual", val smart: Boolean = false,
    val signal: String = "Ready", val running: Boolean = false, val status: String = "Ready", val saving: Boolean = false,
    val parts: List<SessionPart> = listOf(SessionPart()), val active: Int = 0,
    val queueOrder: List<Int>? = null, val queueUndo: QueueIntent? = null,
    val removed: Set<Long> = emptySet(), val selected: Piece? = null,
    val notes: String = "", val reflection: String = "", val intervals: List<SessionInterval> = emptyList(),
    val sensitivity: Int = 50, val pauseSeconds: Int = 5, val timingQuality: String = "Measured",
    val ready: Boolean = false
)

/** Stable part indexes are ledger identities; display order never renumbers them. */
fun PracticeState.visibleQueue(): List<Int> = queueOrder ?: parts.indices.filter { parts[it].pieceId !in removed }

/** One monotonic clock; UI, capture and notification commands are serialized by the coordinator. */
class PracticeEngine(initial: PracticeState = PracticeState()) {
    var state = initial
        private set
    private var last = -1L
    private var evidenceAt = -1L
    private var candidate = -1L
    private var silence = -1L
    private var playing = false

    fun replace(value: PracticeState, now: Long) { state = value; last = now; clearDetection() }
    private fun clearDetection() { evidenceAt = -1; candidate = -1; silence = -1; playing = false }
    fun advance(now: Long) {
        if (last < 0) { last = now; return }
        require(now >= last)
        val delta = now - last
        last = now
        if (state.token.isEmpty() || state.status == "Review" || delta == 0L) return
        if (state.smart && playing && evidenceAt >= 0 && now - evidenceAt > 350) { playing = false; candidate = -1 }
        // Old activity evidence never licenses credit across capture stalls or suspended execution.
        val active = state.running && (!state.smart || (playing && evidenceAt >= 0 && now - evidenceAt <= 350))
        val credit = if (active) delta.coerceAtMost(if (state.smart) 350 else Long.MAX_VALUE) else 0L
        val reason = when {
            active -> if (state.smart) "Detected" else "Manual"
            state.running -> "Silence"
            state.status == "Interrupted" -> "Interruption"
            else -> "ManualPause"
        }
        val start = state.wallElapsed
        val row = SessionInterval(partIndex = state.active, startMillis = start, endMillis = Math.addExact(start, delta), creditedMillis = credit, reason = reason)
        val rows = state.intervals.toMutableList()
        val previous = rows.lastOrNull()
        if (previous != null && previous.partIndex == row.partIndex && previous.reason == row.reason && previous.endMillis == start) {
            rows[rows.lastIndex] = previous.copy(endMillis = row.endMillis, creditedMillis = Math.addExact(previous.creditedMillis, credit))
        } else rows += row
        require(rows.size <= 100000) { L.s(R.string.msg_session_has_too_many_timing_changes_finish_t_5101) }
        state = state.copy(wallElapsed = row.endMillis, elapsed = Math.addExact(state.elapsed, credit),
            detectedActive = Math.addExact(state.detectedActive, if (state.smart) credit else 0),
            parts = if (credit > 0) charge(state.parts, state.active, credit) else state.parts, intervals = rows)
        if (state.running && state.smart && !active) {
            if (silence < 0) silence = now
            state = state.copy(status = if (now - silence >= state.pauseSeconds * 1000L) "Automatically paused" else "Listening")
        }
    }
    fun evidence(now: Long, likely: Boolean) {
        advance(now)
        if (!state.running || !state.smart) return
        evidenceAt = now
        if (likely) {
            if (candidate < 0) candidate = now
            if (playing || now - candidate >= 400) {
                // Bridge only a short, same-piece silence; never interruptions or long auto-pauses.
                if (!playing && silence >= 0 && now - silence <= 750) bridge()
                playing = true; silence = -1
                state = state.copy(status = "Playing detected", signal = "Playing detected")
            }
        } else {
            state=state.copy(signal="Silence")
            if (playing) silence = now
            playing = false; candidate = -1
            if (silence < 0) silence = now
        }
    }
    private fun bridge() {
        val row = state.intervals.lastOrNull() ?: return
        if (row.reason != "Silence" || row.partIndex != state.active || row.endMillis - row.startMillis > 750) return
        val credit = row.endMillis - row.startMillis
        state = state.copy(elapsed = Math.addExact(state.elapsed, credit), detectedActive = Math.addExact(state.detectedActive, credit),
            parts = charge(state.parts, state.active, credit), intervals = state.intervals.dropLast(1) + row.copy(creditedMillis = credit, reason = "Detected"))
    }
    fun begin(now: Long, wall: Long, token: String, smart: Boolean) {
        advance(now)
        state = state.copy(token = state.token.ifEmpty { token }, execution = Math.addExact(state.execution,1), startedAt = state.startedAt.takeIf { it > 0 } ?: wall,
            smart = smart, mode = if (state.token.isEmpty()) { if (smart) "Smart" else "Manual" } else if (state.smart != smart) "Mixed" else state.mode,
            running = true, signal = if(smart) "Listening" else "Practicing", status = if (smart) "Listening" else "Practicing")
        clearDetection()
    }
    fun pause(now: Long, interrupted: Boolean = false) {
        advance(now); clearDetection()
        state = state.copy(running = false, status = if (interrupted) "Interrupted" else "Manually paused")
    }
    fun review(now: Long) { advance(now); clearDetection(); state = state.copy(running = false, status = "Review") }
    fun select(now: Long, piece: Piece?) {
        advance(now); clearDetection()
        val index = state.parts.indexOfFirst { it.pieceId == piece?.id }
        val parts = if (index >= 0) state.parts else state.parts + SessionPart(pieceId = piece?.id, title = piece?.title ?: "Free practice", composer = piece?.composer.orEmpty(), catalogWorkId = piece?.catalogWorkId, musicalEra = piece?.musicalEra.orEmpty())
        state = state.copy(parts = parts, active = if (index >= 0) index else parts.lastIndex, selected = piece, removed = state.removed - (piece?.id ?: 0), status = if (state.running && state.smart) "Listening" else state.status)
    }
    private fun queueIntent() = QueueIntent(state.active, state.visibleQueue(), state.removed)
    private fun pieceAt(index: Int): Piece? = state.parts[index].let { p ->
        p.pieceId?.let { Piece(it, p.title, p.composer, catalogWorkId = p.catalogWorkId, musicalEra = p.musicalEra) }
    }
    fun queueSelect(now: Long, piece: Piece?) {
        advance(now)
        val previous = queueIntent()
        select(now, piece)
        state = state.copy(queueOrder = (previous.order + state.active).distinct(), queueUndo = previous)
    }
    fun queueRemove(now: Long, index: Int) {
        if (index !in state.visibleQueue() || state.parts[index].pieceId == null) return
        advance(now)
        val previous = queueIntent()
        if (state.active == index) select(now, null)
        state = state.copy(queueOrder = (previous.order - index + state.active).distinct(),
            removed = state.removed + requireNotNull(state.parts[index].pieceId), queueUndo = previous)
    }
    fun queueMove(now: Long, index: Int, delta: Int) {
        require(delta == -1 || delta == 1)
        val order = state.visibleQueue().toMutableList()
        val from = order.indexOf(index)
        val to = from + delta
        if (from < 0 || to !in order.indices) return
        advance(now)
        val previous = queueIntent()
        order.removeAt(from); order.add(to, index)
        state = state.copy(queueOrder = order, queueUndo = previous)
    }
    fun undoQueue(now: Long) {
        val intent = state.queueUndo ?: return
        // Charge through the Undo boundary to the current piece first. Never restore credit.
        advance(now)
        select(now, pieceAt(intent.active))
        state = state.copy(queueOrder = intent.order, removed = intent.removed, queueUndo = null)
    }
    fun edit(transform: (PracticeState) -> PracticeState) { state = transform(state) }
}
