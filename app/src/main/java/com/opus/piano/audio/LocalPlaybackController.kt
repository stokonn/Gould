package com.opus.piano.audio

import android.content.*
import android.media.*
import android.os.*
import androidx.core.content.ContextCompat
import com.opus.piano.data.LocalRecordingRepository
import com.opus.piano.data.StudioAchievementRules
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/** One foreground UI player. prepareAsync keeps decoding off main; integrity and
 * file opening use IO. Platform player calls stay on its owning main looper. */
class LocalPlaybackController(context: Context, private val repository: LocalRecordingRepository, private val mayPlay: () -> Boolean = { true }) {
    data class State(val id: String = "", val loading: Boolean = false, val playing: Boolean = false,
        val position: Long = 0, val duration: Long = 0, val failed: Boolean = false, val blockedByCapture: Boolean = false)
    private val app = context.applicationContext
    private val manager = app.getSystemService(AudioManager::class.java)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mutable = MutableStateFlow(State())
    val state = mutable.asStateFlow()
    private var player: MediaPlayer? = null
    private var focus: AudioFocusRequest? = null
    private var preparing: Job? = null
    private var generation = 0L
    private var seeking = false
    private var queuedSeek: Long? = null
    private var evidence = PlaybackEvidence()
    private var awarded = false
    private val pendingAwards = mutableSetOf<Job>()
    private var registered = false
    private val noisy = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) { if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) pause() }
    }
    init {
        scope.launch {
            mutable.map { it.playing }.distinctUntilChanged().collectLatest { playing ->
                if (playing) while (isActive) {
                    delay(100)
                    if (!seeking) sample()
                }
            }
        }
    }
    fun play(id: String) { scope.launch {
        if (!mayPlay()) { clear(); mutable.value = State(id=id,blockedByCapture=true); return@launch }
        if (mutable.value.id == id && player != null && !mutable.value.loading && !mutable.value.failed) {
            if (mutable.value.playing) pause() else resume()
            return@launch
        }
        clear()
        val expected = generation
        mutable.value = State(id = id, loading = true)
        preparing = scope.launch {
            var descriptor: ParcelFileDescriptor? = null
            try {
                descriptor = withContext(Dispatchers.IO) {
                    ParcelFileDescriptor.open(repository.playable(id), ParcelFileDescriptor.MODE_READ_ONLY)
                }
                if (expected != generation) return@launch
                val media = MediaPlayer()
                player = media
                media.setAudioAttributes(attributes())
                media.setDataSource(descriptor.fileDescriptor)
                media.setOnPreparedListener {
                    if (player === media) {
                        mutable.value = mutable.value.copy(loading = false, duration = media.duration.toLong())
                        evidence.boundary(media.currentPosition.toLong(), SystemClock.elapsedRealtime())
                        resume()
                    }
                }
                media.setOnSeekCompleteListener {
                    if (player === media) {
                        val next = queuedSeek; queuedSeek = null
                        if (seeking && next != null) {
                            media.seekTo(next,MediaPlayer.SEEK_CLOSEST)
                        } else {
                        seeking = false
                        evidence.boundary(media.currentPosition.toLong(), SystemClock.elapsedRealtime())
                        mutable.value = mutable.value.copy(position = media.currentPosition.toLong())
                        }
                    }
                }
                media.setOnCompletionListener {
                    if (player === media) {
                        sample()
                        mutable.value = mutable.value.copy(playing = false, position = mutable.value.duration)
                        abandonFocus()
                    }
                }
                media.setOnErrorListener { _, _, _ -> if (player === media) fail(); true }
                media.prepareAsync()
            } catch (c: CancellationException) { throw c }
            catch (_: Exception) { if (expected == generation) fail() }
            finally { descriptor?.let { withContext(NonCancellable + Dispatchers.IO) { it.close() } } }
        }
    } }
    private fun attributes() = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
    private fun resume() {
        val media = player ?: return
        if (!mayPlay()) { val id=mutable.value.id; clear(); mutable.value=State(id=id,blockedByCapture=true); return }
        try {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(attributes()).setOnAudioFocusChangeListener({ change ->
                    if (change != AudioManager.AUDIOFOCUS_GAIN) pause()
                }, Handler(Looper.getMainLooper())).build()
            focus = request
            check(manager.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
            if (!registered) { ContextCompat.registerReceiver(app, noisy, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY), ContextCompat.RECEIVER_NOT_EXPORTED); registered = true }
            if (media.currentPosition >= media.duration) { seeking = true; queuedSeek = null; media.seekTo(0,MediaPlayer.SEEK_CLOSEST) }
            evidence.boundary(media.currentPosition.toLong(), SystemClock.elapsedRealtime())
            media.start()
            mutable.value = mutable.value.copy(playing = true)
        } catch (_: Exception) { fail() }
    }
    fun pause() { scope.launch {
        if (mutable.value.playing) {
            sample()
            runCatching { player?.pause() }
            mutable.value = mutable.value.copy(playing = false)
            abandonFocus()
        } else if (mutable.value.loading) clear()
    } }
    fun seek(position: Long) { scope.launch {
        val media = player ?: return@launch
        if (mutable.value.loading) return@launch
        val target = position.coerceIn(0,mutable.value.duration)
        if (!seeking) {
            sample(); seeking = true
            media.seekTo(target,MediaPlayer.SEEK_CLOSEST)
        } else queuedSeek = target
        mutable.value = mutable.value.copy(position = target)
    } }
    private fun sample() {
        val media = player ?: return
        if (seeking || mutable.value.loading) return
        val position = runCatching { media.currentPosition.toLong() }.getOrNull() ?: return
        if (mutable.value.playing) evidence.advance(position, SystemClock.elapsedRealtime())
        else evidence.boundary(position, SystemClock.elapsedRealtime())
        mutable.value = mutable.value.copy(position = position)
        if (!awarded && evidence.playedMillis >= StudioAchievementRules.playbackThreshold(mutable.value.duration)) {
            awarded = true
            val id = mutable.value.id; val played = evidence.playedMillis
            val job = scope.launch(start=CoroutineStart.LAZY) {
                try { repository.verifiedPlayback(id,played) }
                catch (c: CancellationException) { throw c }
                catch (_: Exception) { if (mutable.value.id == id) fail() }
            }
            pendingAwards += job
            job.invokeOnCompletion { pendingAwards -= job }
            job.start()
        }
    }
    private fun abandonFocus() { focus?.let { manager.abandonAudioFocusRequest(it) }; focus = null }
    private fun clear() {
        generation++
        preparing?.cancel(); preparing = null
        player?.release(); player = null
        abandonFocus()
        if (registered) { runCatching { app.unregisterReceiver(noisy) }; registered = false }
        evidence = PlaybackEvidence(); awarded = false; seeking = false; queuedSeek = null
        mutable.value = State()
    }
    private fun fail() { val id = mutable.value.id; clear(); mutable.value = State(id=id,failed=true) }
    fun stop() { scope.launch { clear() } }
    suspend fun stopAndAwait() = withContext(Dispatchers.Main.immediate) {
        if (mutable.value.playing) sample()
        clear()
        // An explicit delete/restore must not race past already decoded listening
        // evidence and remove the file before its integrity/event commit finishes.
        pendingAwards.toList().joinAll()
    }
}
