package com.opus.piano.practice

import com.opus.piano.i18n.L
import com.opus.piano.R
import android.os.SystemClock
import com.opus.piano.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

/** Application-scoped, single-writer owner. Service and UI never own separate timers. */
class PracticeCoordinator(
    private val repository: PracticePersistence,
    private val clock: () -> Long = { SystemClock.elapsedRealtime() },
    private val message: (Int) -> String = { L.s(it) }
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutex = Mutex()
    private val engine = PracticeEngine()
    private val mutable = MutableStateFlow(engine.state)
    val state = mutable.asStateFlow()
    val captureIssue = MutableStateFlow("")
    private val completed = MutableStateFlow<Long?>(null)
    val completedSession = completed.asStateFlow()
    fun dismissCompletion() { completed.value = null }
    val notice = MutableStateFlow<String?>(null)
    private var checkpointAt = 0L
    private var notesChangedAt = 0L
    private var dirtySince: Long? = null
    val unsaved = MutableStateFlow(false)
    val recoveryFailed = MutableStateFlow(false)
    private val initialized = CompletableDeferred<Unit>()
    private var notesDirty = false
    private var publishedAt = 0L
    private var restoring = false
    private val revision = java.util.concurrent.atomic.AtomicLong(0)
    @Volatile var releaseCapture: (() -> Unit)? = null
    suspend fun startSmart(sensitivity: Int, pause: Int): PracticeState {
        initialized.await()
        return mutex.withLock {
        check(state.value.ready && !restoring && !state.value.running && state.value.status != "Review")
        engine.edit { it.copy(sensitivity = sensitivity.coerceIn(0,100), pauseSeconds = pause.coerceIn(2,30)) }
        captureIssue.value=""
        start(true)
        state.value
        }
    }
    init {
        scope.launch {
            mutex.withLock { recover() }
            initialized.complete(Unit)
            while (isActive) {
                delay(250)
                guarded {
                    if (engine.state.running) { engine.advance(now()); publish() }
                    flushDue()
                }
            }
        }
    }
    private suspend fun recover() {
        engine.replace(PracticeState(ready=false),now());publish()
        try {
            val recovered = repository.recover() ?: PracticeState(ready = true)
            engine.replace(recovered.copy(running = false), now())
            recoveryFailed.value = false
            publish()
        } catch (c: CancellationException) { throw c }
        catch (_: Exception) {
            recoveryFailed.value = true
            notice.value = message(R.string.msg_unfinished_practice_could_not_be_read_it_has_52f7)
        }
    }
    fun retryRecovery(): Job = scope.launch { initialized.await(); mutex.withLock { if (recoveryFailed.value) recover() } }
    suspend fun recoveryData(): String? = mutex.withLock { check(recoveryFailed.value); repository.recoveryData() }
    suspend fun recoveryBytes():ByteArray?=mutex.withLock {check(recoveryFailed.value);repository.recoveryBytes()}
    fun freshAfterExport(exported:ByteArray):Job=scope.launch {
        guarded {
            check(recoveryFailed.value && repository.recoveryBytes()?.contentEquals(exported)==true)
            repository.archiveRecoveryBytes(exported);revision.incrementAndGet()
            engine.replace(PracticeState(ready=true),now());recoveryFailed.value=false;publish()
        }
    }
    /** Only called after a successful user-controlled export and a separate confirmation. */
    fun freshAfterExport(exported: String): Job = scope.launch {
        guarded {
            check(recoveryFailed.value && repository.recoveryData() == exported)
            repository.archiveRecovery(exported)
            revision.incrementAndGet()
            engine.replace(PracticeState(ready = true), now())
            recoveryFailed.value = false; publish()
        }
    }
    private fun now() = clock()
    private fun publish() { mutable.value = engine.state; publishedAt = now() }
    private suspend fun checkpoint() {
        unsaved.value = true
        repository.checkpoint(engine.state)
        checkpointAt = now(); notesDirty = false; dirtySince = null; unsaved.value = false
    }
    private suspend fun flushDue() {
        if ((notesDirty && (now() - notesChangedAt >= 500 || now() - (dirtySince ?: now()) >= 5000)) ||
            (engine.state.running && now() - checkpointAt >= 5000)) checkpoint()
    }
    suspend fun flushIfDue() = mutex.withLock { flushDue() }
    fun retryPersistence(): Job = scope.launch { guarded { check(state.value.ready); checkpoint() } }
    fun close() { releaseCapture?.invoke(); scope.cancel() }
    private suspend fun guarded(block: suspend () -> Unit) {
        try { mutex.withLock { block() } }
        catch (c: CancellationException) { throw c }
        catch (_: Exception) { notice.value = message(R.string.msg_practice_could_not_be_persisted_keep_goldber_8615) }
    }
    fun command(block: suspend PracticeCoordinator.() -> Unit): Job {
        val expected = revision.get()
        return scope.launch {
            initialized.await()
            guarded {
                if (state.value.ready && !restoring && expected == revision.get()) block()
                else notice.value = message(R.string.practice_request_unavailable)
            }
        }
    }
    suspend fun start(smart: Boolean) {
        if (engine.state.running) return
        check(engine.state.ready && engine.state.status != "Review")
        val previous = engine.state
        val wasUnsaved = unsaved.value
        try {
            engine.begin(now(), System.currentTimeMillis(), UUID.randomUUID().toString(), smart)
            // No observer or microphone sees a running identity before the durable write succeeds.
            checkpoint()
            engine.replace(engine.state, now())
            publish()
        } catch (error: Exception) {
            engine.replace(previous, now()); unsaved.value = wasUnsaved
            notice.value = message(R.string.practice_start_failed)
            throw error
        }
    }
    suspend fun pause(interrupted: Boolean = false) { releaseCapture?.invoke(); engine.pause(now(), interrupted); publish(); checkpoint() }
    suspend fun review() {
        releaseCapture?.invoke()
        if (engine.state.token.isEmpty()) engine.begin(now(), System.currentTimeMillis(), UUID.randomUUID().toString(), false)
        engine.review(now()); publish(); checkpoint()
    }
    suspend fun select(piece: Piece?) { if (state.value.status != "Review") { engine.select(now(), piece); publish(); checkpoint() } else notice.value = message(R.string.practice_request_unavailable) }
    suspend fun note(index: Int, text: String) { engine.edit { it.copy(parts = it.parts.mapIndexed { i, p -> if (i == index) p.copy(notes = text.take(10000)) else p }) }; notesDirty = true; if (dirtySince == null) dirtySince = now(); notesChangedAt = now(); unsaved.value = true; publish() }
    suspend fun sessionNotes(notes: String, reflection: String) { engine.edit { it.copy(notes = notes.take(10000), reflection = reflection.take(10000)) }; notesDirty = true; if (dirtySince == null) dirtySince = now(); notesChangedAt = now(); unsaved.value = true; publish() }
    suspend fun configure(sensitivity: Int, pause: Int) { engine.edit { it.copy(sensitivity = sensitivity.coerceIn(0,100), pauseSeconds = pause.coerceIn(2,30)) }; publish(); checkpoint() }
    suspend fun remove(index: Int) {
        val part = state.value.parts.getOrNull(index) ?: return
        if (part.pieceId == null) return
        if (index == state.value.active) select(null)
        engine.edit { it.copy(removed = it.removed + part.pieceId) }; publish(); checkpoint()
    }
    suspend fun reset() { completed.value=null; releaseCapture?.invoke(); repository.clear(); revision.incrementAndGet(); notesDirty = false; dirtySince = null; unsaved.value = false; engine.replace(PracticeState(ready = true), now()); publish() }
    suspend fun save() {
        if (state.value.status != "Review") return
        engine.edit { it.copy(saving = true) }; publish()
        try { val id=repository.complete(state.value); reset(); completed.value=id; notice.value = message(R.string.msg_session_saved_1cd0) }
        finally { engine.edit { it.copy(saving = false) }; publish() }
    }
    suspend fun evidence(token: String, execution: Long, likely: Boolean) = mutex.withLock {
        if (state.value.token == token && state.value.execution == execution && state.value.running && state.value.smart && !restoring) { val old = engine.state.status; engine.evidence(now(), likely); if (old != engine.state.status || now() - publishedAt >= 250) publish() }
    }
    suspend fun interrupt(token: String, execution: Long) = mutex.withLock {
        if (state.value.token == token && state.value.execution == execution && state.value.running && state.value.smart) {
            engine.pause(now(), true); publish()
            try { checkpoint() } catch (c: CancellationException) { throw c } catch (_: Exception) { notice.value = message(R.string.msg_practice_interrupted_latest_checkpoint_retai_ee8b) }
        }
    }
    /** preservesRecovery is only for BackupRepository's tested snapshot + journal transaction.
     * Ordinary replacements remain forbidden when the original draft cannot be decoded. */
    suspend fun <T> replacing(preservesRecovery:Boolean=false, block: suspend () -> T): T {
        initialized.await()
        return mutex.withLock {
        check(!recoveryFailed.value || preservesRecovery)
        restoring = true;revision.incrementAndGet()
        try {
            releaseCapture?.invoke()
            if(!recoveryFailed.value){
                val checkpointNeeded=engine.state.token.isNotBlank() || notesDirty || repository.recoveryBytes()!=null
                engine.pause(now());publish();if(checkpointNeeded)checkpoint()
            }
            block()
        } finally {
            withContext(NonCancellable) {
                completed.value=null;notesDirty=false;dirtySince=null;unsaved.value=false
                recover() // Reload either the rolled-back original or the committed imported draft.
                restoring=false
            }
        }
        }
    }
}
