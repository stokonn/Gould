package com.opus.piano.audio

import android.content.*
import android.media.*
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicLong

/** Application owner of a bounded AudioTrack loop. Stops on focus/route/lifecycle loss;
 * never starts automatically and never writes to the journal or capture pipeline. */
class MetronomeController(context: Context) {
    data class State(val bpm: Int = 80, val beats: Int = 4, val playing: Boolean = false, val failed: Boolean = false,
        val starting: Boolean = false, val appliedBpm: Int = 80, val appliedBeats: Int = 4)
    private val app = context.applicationContext
    private val manager = app.getSystemService(AudioManager::class.java)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()
    private val generation = AtomicLong()
    private val requested = java.util.concurrent.atomic.AtomicBoolean(false)
    private val mutable = MutableStateFlow(State())
    val state = mutable.asStateFlow()
    private var track: AudioTrack? = null
    private var focus: AudioFocusRequest? = null
    private var routeRegistered = false
    private val noisy = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) { if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) stop() }
    }
    fun configure(bpm: Int, beats: Int) {
        mutable.value = mutable.value.copy(bpm = bpm.coerceIn(40,240), beats = beats.coerceIn(1,12))
        if (requested.get()) start()
    }
    fun start() {
        requested.set(true)
        val request = generation.incrementAndGet()
        mutable.value = mutable.value.copy(starting=true,failed=false)
        scope.launch {
            mutex.withLock {
                cleanup()
                if (request != generation.get()) return@withLock
                val settings = mutable.value
                var candidate: AudioTrack? = null
                try {
                    val attributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
                    val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                        .setAudioAttributes(attributes).setOnAudioFocusChangeListener({ change ->
                            if (change != AudioManager.AUDIOFOCUS_GAIN) stop()
                        }, android.os.Handler(android.os.Looper.getMainLooper())).build()
                    focus = focusRequest
                    check(manager.requestAudioFocus(focusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                    val pcm = MetronomePcm.bar(settings.bpm, settings.beats)
                    candidate = AudioTrack.Builder().setAudioAttributes(attributes)
                        .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(MetronomePcm.RATE).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                        .setTransferMode(AudioTrack.MODE_STATIC).setBufferSizeInBytes(pcm.size * 2).build()
                    check(candidate.state != AudioTrack.STATE_UNINITIALIZED)
                    check(candidate.write(pcm, 0, pcm.size) == pcm.size)
                    check(candidate.state == AudioTrack.STATE_INITIALIZED)
                    check(candidate.setLoopPoints(0, pcm.size, -1) == AudioTrack.SUCCESS)
                    if (request != generation.get()) { candidate.release(); cleanup(); return@withLock }
                    track = candidate
                    ContextCompat.registerReceiver(app, noisy, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY), ContextCompat.RECEIVER_NOT_EXPORTED)
                    routeRegistered = true
                    candidate.play()
                    mutable.update { current -> if (request == generation.get()) current.copy(playing=true,failed=false,starting=false,
                        appliedBpm=settings.bpm,appliedBeats=settings.beats) else current }
                } catch (error: Exception) {
                    android.util.Log.e("GouldMetronome", "Audio output failed", error)
                    if (track !== candidate) runCatching { candidate?.release() }
                    cleanup()
                    if (request == generation.get()) {
                        requested.set(false)
                        mutable.value = mutable.value.copy(playing=false,starting=false,failed=true)
                    }
                }
            }
        }
    }
    fun stop() {
        requested.set(false)
        generation.incrementAndGet()
        mutable.value = mutable.value.copy(playing=false,starting=false)
        scope.launch { mutex.withLock { cleanup() } }
    }
    private fun cleanup() {
        mutable.value = mutable.value.copy(playing = false)
        track?.let { runCatching { it.stop() }; it.release() }; track = null
        focus?.let { manager.abandonAudioFocusRequest(it) }; focus = null
        if (routeRegistered) { runCatching { app.unregisterReceiver(noisy) }; routeRegistered = false }
    }
}
