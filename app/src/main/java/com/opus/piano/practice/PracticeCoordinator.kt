package com.opus.piano.practice

import android.os.SystemClock
import com.opus.piano.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

/** Application-scoped, single-writer owner. Service and UI never own separate timers. */
class PracticeCoordinator(private val repository: PracticeRepository) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutex = Mutex()
    private val engine = PracticeEngine()
    private val mutable = MutableStateFlow(engine.state)
    val state = mutable.asStateFlow()
    val notice = MutableStateFlow<String?>(null)
    private var checkpointAt = 0L
    private var notesChangedAt = 0L
    private var notesDirty = false
    private var publishedAt = 0L
    private var restoring = false
    private val revision = java.util.concurrent.atomic.AtomicLong(0)
    @Volatile var releaseCapture: (() -> Unit)? = null
    suspend fun startSmart(sensitivity: Int, pause: Int): PracticeState = mutex.withLock {
        loaded.await()
        check(state.value.ready && !restoring && !state.value.running && state.value.status != "Review")
        engine.edit { it.copy(sensitivity = sensitivity.coerceIn(0,100), pauseSeconds = pause.coerceIn(2,30)) }
        start(true)
        state.value
    }
    private val loaded = scope.async {
        try { repository.recover() } catch (c: CancellationException) { throw c }
        catch (_: Exception) { notice.value = "Unfinished practice could not be read. It has not been deleted."; throw IllegalStateException("Draft recovery failed") }
    }
    init {
        scope.launch {
            try { mutex.withLock { engine.replace(loaded.await() ?: PracticeState(ready = true), now()); publish() } }
            catch (_: Exception) { return@launch }
            while (isActive) {
                state.first { it.running || notesDirty }
                delay(250)
                guarded {
                    if (engine.state.running) { engine.advance(now()); publish() }
                    if ((notesDirty && now() - notesChangedAt >= 500) || (engine.state.running && now() - checkpointAt >= 5000)) checkpoint()
                }
            }
        }
    }
    private fun now() = SystemClock.elapsedRealtime()
    private fun publish() { mutable.value = engine.state; publishedAt = now() }
    private suspend fun checkpoint() { repository.checkpoint(engine.state); checkpointAt = now(); notesDirty = false }
    private suspend fun guarded(block: suspend () -> Unit) {
        try { mutex.withLock { block() } }
        catch (c: CancellationException) { throw c }
        catch (_: Exception) { notice.value = "Practice could not be persisted. Keep Goldberg open and retry before leaving." }
    }
    fun command(block: suspend PracticeCoordinator.() -> Unit) {
        val expected = revision.get()
        scope.launch { guarded { if (loaded.isCompleted && state.value.ready && !restoring && expected == revision.get()) block() } }
    }
    suspend fun start(smart: Boolean) {
        engine.begin(now(), System.currentTimeMillis(), UUID.randomUUID().toString(), smart); publish(); checkpoint()
    }
    suspend fun pause(interrupted: Boolean = false) { releaseCapture?.invoke(); engine.pause(now(), interrupted); publish(); checkpoint() }
    suspend fun review() {
        releaseCapture?.invoke()
        if (engine.state.token.isEmpty()) engine.begin(now(), System.currentTimeMillis(), UUID.randomUUID().toString(), false)
        engine.review(now()); publish(); checkpoint()
    }
    suspend fun select(piece: Piece?) { if (state.value.status != "Review") { engine.select(now(), piece); publish(); checkpoint() } }
    suspend fun note(index: Int, text: String) { engine.edit { it.copy(parts = it.parts.mapIndexed { i, p -> if (i == index) p.copy(notes = text.take(10000)) else p }) }; notesDirty = true; notesChangedAt = now(); publish() }
    suspend fun sessionNotes(notes: String, reflection: String) { engine.edit { it.copy(notes = notes.take(10000), reflection = reflection.take(10000)) }; notesDirty = true; notesChangedAt = now(); publish() }
    suspend fun configure(sensitivity: Int, pause: Int) { engine.edit { it.copy(sensitivity = sensitivity.coerceIn(0,100), pauseSeconds = pause.coerceIn(2,30)) }; publish(); checkpoint() }
    suspend fun remove(index: Int) {
        val part = state.value.parts.getOrNull(index) ?: return
        if (part.pieceId == null) return
        if (index == state.value.active) select(null)
        engine.edit { it.copy(removed = it.removed + part.pieceId) }; publish(); checkpoint()
    }
    suspend fun reset() { releaseCapture?.invoke(); repository.clear(); revision.incrementAndGet(); notesDirty = false; engine.replace(PracticeState(ready = true), now()); publish() }
    suspend fun save() {
        if (state.value.status != "Review") return
        engine.edit { it.copy(saving = true) }; publish()
        try { repository.complete(state.value); reset(); notice.value = "Session saved." }
        finally { engine.edit { it.copy(saving = false) }; publish() }
    }
    suspend fun evidence(token: String, execution: Long, likely: Boolean) = mutex.withLock {
        if (state.value.token == token && state.value.execution == execution && state.value.running && state.value.smart && !restoring) { val old = engine.state.status; engine.evidence(now(), likely); if (old != engine.state.status || now() - publishedAt >= 250) publish() }
    }
    suspend fun interrupt(token: String, execution: Long) = mutex.withLock {
        if (state.value.token == token && state.value.execution == execution && state.value.running && state.value.smart) {
            engine.pause(now(), true); publish()
            try { checkpoint() } catch (c: CancellationException) { throw c } catch (_: Exception) { notice.value = "Practice interrupted; latest checkpoint retained." }
        }
    }
    suspend fun <T> replacing(block: suspend () -> T): T = mutex.withLock {
        restoring = true; revision.incrementAndGet()
        try {
            releaseCapture?.invoke(); engine.pause(now()); publish(); checkpoint()
            val result = block()
            repository.clear(); notesDirty = false; engine.replace(PracticeState(ready = true), now()); publish()
            result
        } finally { restoring = false }
    }
}
