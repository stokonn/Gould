package com.opus.piano.audio

import com.opus.piano.data.LocalRecordingRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    private val lifecycle = Mutex()
    private val maintenanceRevision = java.util.concurrent.atomic.AtomicLong()
    private var writer: DurableWav? = null
    private var samplesWritten = 0L
    @Volatile private var activeRate: Int = 0
    private val writerLock = Any()
    /** A capture iteration may retain a callback after detach. Its identity check under
     * writerLock makes that callback harmless across stop, interruption and a new take. */
    val sink = AtomicReference<((ShortArray, Int) -> Unit)?>(null)

    /** Durable identity precedes samples. Transition completion survives caller cancellation. */
    suspend fun start(rate: Int, title: String, composer: String, token: String?, awaitCapture: Boolean = false, pieceKey: String = "free") {
        val revision = maintenanceRevision.get()
        lifecycle.withLock {
        check(revision == maintenanceRevision.get()) { "Retry recording after data maintenance" }
        check(mutable.value.phase in setOf(Phase.Idle, Phase.Saved, Phase.Recoverable)) {
            "Recording cannot start from ${mutable.value.phase}"
        }
        withContext(NonCancellable) {
            mutable.value = State(Phase.Preparing)
            try {
                val (row, w) = repository.prepare(rate, title, composer, token, pieceKey)
                synchronized(writerLock) {
                    writer = w
                    activeRate = rate
                    mutable.value = State(if (awaitCapture) Phase.Preparing else Phase.Recording, id = row.id, title = row.title,
                        composer = row.composer, sessionToken = row.sessionToken)
                    samplesWritten = 0L
                    sink.set { samples, count ->
                        synchronized(writerLock) {
                            if (writer === w && mutable.value.phase == Phase.Recording) {
                                w.append(samples, count)
                                samplesWritten += count
                                val duration = samplesWritten * 1000 / rate
                                // Publish at most ten duration updates per second.
                                if (duration / 100 != mutable.value.durationMillis / 100)
                                    mutable.value = mutable.value.copy(durationMillis = duration)
                            }
                        }
                    }
                }
            } catch (error: Exception) {
                val id = (error as? LocalRecordingRepository.PreparationFailure)?.recordingId.orEmpty()
                mutable.value = State(Phase.Recoverable, id = id, error = error.message ?: "Storage")
                throw error
            }
        }
    }

    }

    /** Lock order is practice coordinator → recording lifecycle → backup repository. */
    suspend fun <T> whileIdle(block: suspend () -> T): T = lifecycle.withLock {
        check(mutable.value.phase !in setOf(Phase.Preparing, Phase.Recording, Phase.Finalizing)) { "Stop recording first" }
        maintenanceRevision.incrementAndGet()
        try { block() } finally { maintenanceRevision.incrementAndGet() }
    }

    suspend fun associate(part: com.opus.piano.data.SessionPart) = lifecycle.withLock {
        val current = mutable.value
        if (current.phase !in setOf(Phase.Preparing,Phase.Recording)) return@withLock
        val offset = synchronized(writerLock) { samplesWritten }
        repository.associate(current.id,offset,part.pieceId?.let { "piece:$it" } ?: "free",part.title,part.composer)
    }
    suspend fun linkSession(token: String) = lifecycle.withLock {
        val current = mutable.value
        if (current.phase !in setOf(Phase.Preparing,Phase.Recording) || current.sessionToken != null) return@withLock
        repository.linkSession(current.id,token)
        synchronized(writerLock) { mutable.value = mutable.value.copy(sessionToken=token) }
    }

    /** Called only by the sole service after AudioRecord actually starts, or when
     * a prepared take joins that same live stream. Obsolete acknowledgments do nothing. */
    suspend fun captureStarted(id: String) = lifecycle.withLock {
        synchronized(writerLock) {
            if (mutable.value.phase == Phase.Preparing && mutable.value.id == id && writer != null)
                mutable.value = mutable.value.copy(phase = Phase.Recording)
        }
    }

    private fun detach(): DurableWav? = synchronized(writerLock) {
        sink.set(null)
        activeRate = 0
        writer.also { writer = null }
    }

    /** Serialized with interruption/new starts; detached callbacks cannot append to this take. */
    suspend fun stop() = lifecycle.withLock {
        val current = mutable.value
        if (current.phase !in setOf(Phase.Preparing, Phase.Recording)) return@withLock
        withContext(NonCancellable) {
            val w = detach()
            mutable.value = current.copy(phase = Phase.Finalizing)
            try {
                val saved = repository.finalize(current.id, checkNotNull(w))
                mutable.value = State(Phase.Saved, id = saved.id, durationMillis = saved.durationMillis,
                    title = saved.title, composer = saved.composer, sessionToken = saved.sessionToken)
            } catch (error: Exception) {
                withContext(Dispatchers.IO) { runCatching { w?.close() } }
                runCatching { repository.interrupted(current.id, "Finalize") }
                mutable.value = current.copy(phase = Phase.Recoverable, error = error.message ?: "Finalize")
                throw error
            }
        }
    }

    /** Preserve and sync interrupted bytes. Recovery is a separate explicit user action. */
    suspend fun interrupt(reason: String) = lifecycle.withLock {
        val current = mutable.value
        if (current.phase !in setOf(Phase.Preparing, Phase.Recording)) return@withLock
        withContext(NonCancellable) {
            val w = detach()
            val closed = withContext(Dispatchers.IO) { runCatching { w?.close() } }
            val marked = runCatching { repository.interrupted(current.id, reason) }
            mutable.value = current.copy(phase = Phase.Recoverable,
                error = if (closed.isFailure || marked.isFailure) "$reason: Storage" else reason)
        }
    }

    fun currentRate(): Int = activeRate
    suspend fun reconcile() = lifecycle.withLock { repository.reconcile(mutable.value.id.takeIf { mutable.value.phase in setOf(Phase.Preparing, Phase.Recording) }) }
    fun close() { scope.launch { try { interrupt("Closed") } finally { scope.cancel() } } }

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
