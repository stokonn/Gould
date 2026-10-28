package com.opus.piano.audio

import com.opus.piano.data.LocalAudio
import com.opus.piano.data.LocalRecordingRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.atomic.AtomicReference

/** Recording state machine: Idle → Preparing → Recording → Finalizing → Saved, with Recoverable.
 *  Application-scoped; one writer; never auto-starts. The service owns the microphone.
 *  Durable identity (DB row + staging file) precedes any captured sample. */
class RecordingController(
    private val repository: LocalRecordingRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val mutable = MutableStateFlow(State())
    val state = mutable.asStateFlow()
    private var writer: DurableWav? = null
    private var activeId: String = ""
    private var activeRate: Int = 0
    private val writerLock = Object()
    /** The capture loop reads this each iteration; null means no recording consumer.
     *  Synchronized on writerLock to prevent append/finish races. */
    val sink = AtomicReference<((ShortArray, Int) -> Unit)?>(null)

    /** Explicit user action only. Durable identity precedes any sample. Never auto-called. */
    suspend fun start(rate: Int, title: String, composer: String, token: String?) {
        val current = mutable.value
        check(current.phase == Phase.Idle || current.phase == Phase.Saved || current.phase == Phase.Recoverable) {
            "Recording cannot start from ${current.phase}"
        }
        mutable.value = current.copy(phase = Phase.Preparing)
        try {
            val (row, w) = repository.prepare(rate, title, composer, token)
            activeId = row.id
            activeRate = rate
            writer = w
            val safeAppend: (ShortArray, Int) -> Unit = { samples, count ->
                synchronized(writerLock) { w.append(samples, count) }
            }
            sink.set(safeAppend)
            mutable.value = State(
                Phase.Recording, id = row.id, title = row.title,
                composer = row.composer, sessionToken = row.sessionToken
            )
        } catch (error: Exception) {
            mutable.value = State(Phase.Recoverable, id = activeId, error = error.message ?: "Storage")
            throw error
        }
    }

    /** Stop recording and finalize audio. Never completes practice.
     *  Safe to call while capture loop is still appending: writerLock serializes. */
    suspend fun stop() {
        val current = mutable.value
        if (current.phase != Phase.Recording) return
        synchronized(writerLock) { sink.set(null) }
        mutable.value = current.copy(phase = Phase.Finalizing)
        val w = writer ?: run {
            mutable.value = mutable.value.copy(phase = Phase.Recoverable, error = "Writer")
            return
        }
        try {
            val saved = repository.finalize(activeId, w)
            writer = null
            mutable.value = State(
                Phase.Saved, id = saved.id, durationMillis = saved.durationMillis,
                title = saved.title, composer = saved.composer
            )
        } catch (error: Exception) {
            runCatching { repository.interrupted(activeId, "Finalize") }
            writer = null
            mutable.value = mutable.value.copy(phase = Phase.Recoverable, error = error.message ?: "Finalize")
            throw error
        }
    }

    /** Fatal capture error: detach sink, mark recoverable. Never silently continue. */
    suspend fun interrupt(reason: String) {
        val current = mutable.value
        if (current.phase == Phase.Recording || current.phase == Phase.Preparing) {
            synchronized(writerLock) { sink.set(null) }
            writer = null
            runCatching { repository.interrupted(activeId, reason) }
            mutable.value = current.copy(phase = Phase.Recoverable, error = reason)
        }
    }

    /** Current sample rate for the capture loop; 0 if not recording. */
    fun currentRate(): Int = activeRate

    /** Startup reconciliation only; never capture or automatic publication. */
    suspend fun reconcile() { repository.reconcile() }

    fun close() { scope.cancel() }

    enum class Phase { Idle, Preparing, Recording, Finalizing, Saved, Recoverable }

    data class State(
        val phase: Phase = Phase.Idle,
        val id: String = "",
        val title: String = "",
        val composer: String = "",
        val sessionToken: String? = null,
        val durationMillis: Long = 0,
        val error: String = ""
    )

    companion object {
        /** Probe the same rates the MicrophoneController uses so both always agree. */
        fun selectRate(): Int = listOf(22050, 44100, 16000).firstOrNull {
            android.media.AudioRecord.getMinBufferSize(
                it, android.media.AudioFormat.CHANNEL_IN_MONO,
                android.media.AudioFormat.ENCODING_PCM_16BIT
            ) > 0
        } ?: error("No supported sample rate")
    }
}
