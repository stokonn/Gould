package com.opus.piano.audio

import com.opus.piano.i18n.L
import com.opus.piano.R
import android.media.*
import android.os.Build
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean

/** The service is the sole owner. No sample leaves this bounded in-memory buffer. */
class MicrophoneController(private val context: android.content.Context) {
    @Volatile private var recorder: AudioRecord? = null
    private val stopped = AtomicBoolean(false)
    suspend fun capture(sensitivity: () -> Int, evidence: suspend (Boolean) -> Unit) = withContext(Dispatchers.IO) {
        check(androidx.core.content.ContextCompat.checkSelfPermission(context,android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) { "Microphone permission unavailable" }
        val rate = listOf(22050,44100,16000).firstOrNull { AudioRecord.getMinBufferSize(it, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) > 0 } ?: error(L.s(R.string.msg_microphone_format_unavailable_f7b8))
        val min = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        val format = AudioFormat.Builder().setSampleRate(rate).setChannelMask(AudioFormat.CHANNEL_IN_MONO).setEncoding(AudioFormat.ENCODING_PCM_16BIT).build()
        val source = if (context.getSystemService(AudioManager::class.java).getProperty(AudioManager.PROPERTY_SUPPORT_AUDIO_SOURCE_UNPROCESSED) == "true") MediaRecorder.AudioSource.UNPROCESSED else MediaRecorder.AudioSource.VOICE_RECOGNITION
        val builder = AudioRecord.Builder().setAudioSource(source).setAudioFormat(format).setBufferSizeInBytes(maxOf(min, 8192))
        if (Build.VERSION.SDK_INT >= 30) builder.setPrivacySensitive(true)
        val audio = builder.build()
        recorder = audio
        val samples = ShortArray(2048)
        val detector = PianoActivityDetector()
        val route = AtomicBoolean(false)
        val listener = AudioRouting.OnRoutingChangedListener { route.set(true) }
        try {
            check(!stopped.get() && audio.state == AudioRecord.STATE_INITIALIZED)
            audio.addOnRoutingChangedListener(listener, android.os.Handler(android.os.Looper.getMainLooper()))
            audio.startRecording()
            // Initial routing is expected; later changes interrupt rather than silently changing calibration.
            var warmed = 0
            while (currentCoroutineContext().isActive && !stopped.get()) {
                val count = audio.read(samples,0,samples.size,AudioRecord.READ_BLOCKING)
                check(count > 0) { L.s(R.string.msg_microphone_interrupted_b420) }
                if (Build.VERSION.SDK_INT >= 29) check(audio.activeRecordingConfiguration?.isClientSilenced != true) { L.s(R.string.msg_microphone_is_unavailable_0f16) }
                if (warmed++ > 3) check(!route.getAndSet(false)) { L.s(R.string.msg_microphone_route_changed_5688) } else route.set(false)
                evidence(detector.likely(samples,count,sensitivity()))
            }
        } finally {
            audio.removeOnRoutingChangedListener(listener)
            runCatching { audio.stop() }; audio.release(); recorder = null; samples.fill(0)
        }
    }
    fun stop() { stopped.set(true); recorder?.let { runCatching { it.stop() } } }
}
