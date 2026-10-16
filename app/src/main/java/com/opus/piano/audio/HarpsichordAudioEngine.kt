package com.opus.piano.audio

import android.content.Context
import android.media.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlin.math.*

/** Small synthesized plucks, no recordings or licensed sound assets. */
class HarpsichordAudioEngine(context: Context) : AutoCloseable {
    val notice = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { _,_ -> notice.value = "Audio is unavailable on this device." })
    private val notes = Channel<Int>(16)
    private val manager = context.getSystemService(AudioManager::class.java)
    private val attributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
    private val focus = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT).setAudioAttributes(attributes)
        .setOnAudioFocusChangeListener { if (it < 0) close() }.build()
    @Volatile private var track: AudioTrack? = null
    init {
        scope.launch {
            val rate = 22050
            val min = AudioTrack.getMinBufferSize(rate,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT)
            val audio = AudioTrack.Builder().setAudioAttributes(attributes).setAudioFormat(AudioFormat.Builder().setSampleRate(rate).setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build()).setBufferSizeInBytes(maxOf(min,4096)).setTransferMode(AudioTrack.MODE_STREAM).build()
            track = audio
            try {
                if (manager.requestAudioFocus(focus) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) { notice.value = "Audio is busy. Close and retry when other audio has stopped."; return@launch }
                audio.play()
                data class Voice(val frequency: Double, var age: Int = 0)
                val voices = mutableListOf<Voice>()
                val buffer = ShortArray(256)
                while (isActive) {
                    if (voices.isEmpty()) {
                        val note = notes.receiveCatching().getOrNull() ?: break
                        voices += Voice(261.625565 * 2.0.pow(note / 12.0))
                    }
                    while (true) { val note = notes.tryReceive().getOrNull() ?: break; if (voices.size >= 4) voices.removeAt(0); voices += Voice(261.625565 * 2.0.pow(note / 12.0)) }
                    for (i in buffer.indices) {
                        var value = 0.0
                        voices.forEach { v ->
                            val t = v.age++.toDouble() / rate
                            val attack = min(1.0,t/.005)
                            value += attack * exp(-t*5) * (sin(2*PI*v.frequency*t) + .4*sin(4*PI*v.frequency*t) + .2*sin(6*PI*v.frequency*t)) * .12
                        }
                        buffer[i] = (value.coerceIn(-.9,.9)*32767).toInt().toShort()
                    }
                    audio.write(buffer,0,buffer.size,AudioTrack.WRITE_BLOCKING)
                    voices.removeAll { it.age > rate }
                }
            } finally { runCatching { audio.stop() }; audio.release(); track = null; manager.abandonAudioFocusRequest(focus) }
        }
    }
    fun note(index: Int) { if (index in 0..12) notes.trySend(index) }
    override fun close() { notes.close(); scope.cancel(); track?.let { runCatching { it.pause(); it.flush() } } }
}
