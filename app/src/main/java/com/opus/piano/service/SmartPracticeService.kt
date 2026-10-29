package com.opus.piano.service

import com.opus.piano.i18n.L
import com.opus.piano.R
import android.app.*
import android.content.*
import android.content.pm.ServiceInfo
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.opus.piano.MainActivity
import com.opus.piano.OpusApplication
import com.opus.piano.audio.MicrophoneController
import com.opus.piano.audio.RecordingController
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/** Single microphone owner. Capture runs while practice is active OR recording is active.
 *  Detection and recording are separate consumers of the same PCM stream. */
class SmartPracticeService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val practice get() = (application as OpusApplication).practice
    private val recording get() = (application as OpusApplication).recording
    private var microphone: MicrophoneController? = null
    private var capture: Job? = null
    @Volatile private var closing = false
    @Volatile private var destroyed = false
    @Volatile private var microphoneReady = false
    private var token = ""
    private var execution = -1L
    override fun onBind(intent: Intent?) = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (closing) {
            scope.launch { recording.interrupt("CaptureClosing") }
            return START_NOT_STICKY
        }
        when (intent?.action) {
            START -> { if (!practice.state.value.ready) { stopSelf(); return START_NOT_STICKY }; startOrExtend(detection = true) }
            START_RECORDING -> { if (!practice.state.value.ready && recording.state.value.phase != RecordingController.Phase.Recording) { stopSelf(); return START_NOT_STICKY }; startOrExtend(detection = false) }
            else -> { if (capture == null) stopSelf(); return START_NOT_STICKY }
        }
        return START_NOT_STICKY
    }
    private fun startOrExtend(detection: Boolean) {
        if (capture?.isActive == true) {
            if (microphoneReady) {
                val id = recording.state.value.id
                scope.launch { if (!closing) recording.captureStarted(id) }
            }
            // Already running: start practice if requested and not already running.
            if (detection && !practice.state.value.running) {
                scope.launch {
                    try {
                        val settings = (application as OpusApplication).database.personalDao().getSettings() ?: com.opus.piano.data.ProfileSettings()
                        val started = practice.startSmart(settings.sensitivity, settings.pauseSeconds)
                        token = started.token; execution = started.execution
                    } catch (_: Exception) { }
                }
            }
            return
        }
        try {
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(NotificationChannel(CHANNEL,L.s(R.string.msg_smart_practice_1d73),NotificationManager.IMPORTANCE_LOW))
            ServiceCompat.startForeground(this,ID,notification("Starting microphone"),if (Build.VERSION.SDK_INT >= 30) ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE else 0)
            capture = scope.launch {
                try {
                    var startedToken = ""
                    var startedExecution = -1L
                    if (detection) {
                        val settings = (application as OpusApplication).database.personalDao().getSettings() ?: com.opus.piano.data.ProfileSettings()
                        val started = practice.startSmart(settings.sensitivity, settings.pauseSeconds)
                        startedToken = started.token; startedExecution = started.execution
                    }
                    token = startedToken; execution = startedExecution
                    val mic = MicrophoneController(this@SmartPracticeService); microphone = mic
                    practice.releaseCapture = {
                        // Stop mic only when recording is not also consuming it.
                        if (recording.state.value.phase !in setOf(RecordingController.Phase.Preparing, RecordingController.Phase.Recording)) mic.stop()
                    }
                    launch {
                        combine(
                            practice.state.map { Triple(it.running && it.smart,it.token,it.status) }.distinctUntilChanged(),
                            recording.state.map { it.phase to it.id }.distinctUntilChanged()
                        ) { practiceState, audioState -> practiceState to audioState }
                        .collect { (practiceState, audioState) ->
                            val practiceRunning = practiceState.first
                            val recordingActive = audioState.first in setOf(RecordingController.Phase.Preparing, RecordingController.Phase.Recording)
                            if (!practiceRunning && !recordingActive &&
                                !(practice.state.value.running && practice.state.value.smart) &&
                                recording.state.value.phase !in setOf(RecordingController.Phase.Preparing, RecordingController.Phase.Recording)) {
                                closing = true; stopping.value = true; microphoneReady = false
                                microphone?.stop(); stopForeground(STOP_FOREGROUND_REMOVE); stopSelf()
                            }
                            else manager.notify(ID,notification(practice.state.value.status))
                        }
                    }
                    mic.capture(
                        sensitivity = { practice.state.value.sensitivity },
                        preferredRate = recording.currentRate(),
                        recordingSink = recording.sink,
                        detectionEnabled = { practice.state.value.running && practice.state.value.smart },
                        started = {
                            microphoneReady = true; captureActive.value = true
                            if (!closing) recording.captureStarted(recording.state.value.id)
                        }
                    ) { practice.evidence(token,execution,it) }
                } catch (c: CancellationException) { throw c }
                catch (error: Exception) {
                    if (!closing) {
                    if (token.isNotEmpty()) practice.captureIssue.value=if(error is SecurityException) "PermissionDenied" else if(error is IllegalStateException) "Unavailable" else "DetectionError"
                    practice.notice.value = L.s(R.string.msg_smart_practice_interrupted_open_practice_to_3170)
                    }
                } finally {
                    closing = true; if (!destroyed) stopping.value = true
                    microphoneReady = false; captureActive.value = false
                    microphone?.stop(); practice.releaseCapture = null
                    withContext(NonCancellable) {
                        practice.interrupt(token,execution)
                        recording.interrupt("Interrupted")
                    }
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    val finalState = practice.state.value
                    if (token.isNotEmpty() && androidx.core.app.NotificationManagerCompat.from(this@SmartPracticeService).areNotificationsEnabled()) {
                        val open = PendingIntent.getActivity(this@SmartPracticeService,0,Intent(this@SmartPracticeService,MainActivity::class.java).putExtra("practice",true).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                        val text = if (finalState.status == "Review") L.fixed("Session ready to review") else L.s(R.string.notification_resume,L.fixed(finalState.status))
                        try { manager.notify(ID,NotificationCompat.Builder(this@SmartPracticeService,CHANNEL).setSmallIcon(com.opus.piano.R.drawable.ic_goldberg_monochrome).setContentTitle(L.s(R.string.msg_goldberg_smart_practice_73e7)).setContentText(text).setContentIntent(open).setAutoCancel(true).setSilent(true).build()) } catch (_: SecurityException) { /* Respect notification denial. */ }
                    }
                    stopSelf()
                }
            }
        } catch (_: Exception) {
            scope.launch { recording.interrupt("ForegroundStartFailed") }
            microphone?.stop(); practice.notice.value = L.s(R.string.msg_microphone_service_could_not_start_keep_gold_2f00)
            stopForeground(STOP_FOREGROUND_REMOVE); stopSelf()
        }
    }
    private fun notification(status: String): Notification {
        val state = practice.state.value
        fun action(name: String) = PendingIntent.getBroadcast(this, if (name == PAUSE) 1 else 2,
            Intent(this,PracticeActionReceiver::class.java).setAction(name).putExtra("token",state.token).putExtra("execution",state.execution).putExtra("recordingId",recording.state.value.id), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val open = PendingIntent.getActivity(this,0,Intent(this,MainActivity::class.java).putExtra("practice",true).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val preparing = recording.state.value.phase == RecordingController.Phase.Preparing
        val recordingActive = preparing || recording.state.value.phase == RecordingController.Phase.Recording
        val title = if (recordingActive && !state.running) L.s(R.string.msg_studio_recording) else L.s(R.string.msg_goldberg_smart_practice_73e7)
        val text = when {
            preparing -> L.s(R.string.studio_recording_preparing)
            recordingActive && !state.running -> L.s(R.string.label_recording)
            status == "Listening" -> L.s(R.string.msg_listening_for_piano_playing_93b1)
            else -> L.fixed(status)
        }
        val builder = NotificationCompat.Builder(this,CHANNEL).setSmallIcon(com.opus.piano.R.drawable.ic_goldberg_monochrome)
            .setContentTitle(title).setContentText(text)
            .setContentIntent(open).setOngoing(true).setOnlyAlertOnce(true).setSilent(true).setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        if (state.running) builder.addAction(0,L.fixed("Pause"),action(PAUSE))
        return builder.addAction(0,L.fixed("Stop"),action(STOP)).build()
    }
    override fun onDestroy() {
        destroyed = true; closing = true; stopping.value = true; microphoneReady = false
        microphone?.stop(); scope.cancel(); super.onDestroy()
        stopping.value = false
    }
    companion object {
        /** Observable ownership for native privacy acceptance; never starts a microphone. */
        internal val captureActive = MutableStateFlow(false)
        private val stopping = MutableStateFlow(false)
        /** A new explicit take waits for an old record-only service to release its
         * stream. An already running Smart stream is intentionally shared. */
        suspend fun awaitReadyForNewTake(context: Context) {
            val state = (context.applicationContext as OpusApplication).practice.state.value
            if (state.running && state.smart) return
            withTimeout(5000) { combine(captureActive,stopping) { active,closing -> !active && !closing }.first { it } }
        }
        const val START = "com.opus.piano.START_SMART"
        const val START_RECORDING = "com.opus.piano.START_RECORDING"
        const val PAUSE = "com.opus.piano.PAUSE"
        const val STOP = "com.opus.piano.STOP"
        const val CHANNEL = "smart-practice"
        const val ID = 80
        fun start(context: Context): Boolean = try {
            check((context as? androidx.lifecycle.LifecycleOwner)?.lifecycle?.currentState?.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED) == true) { "Open Goldberg to start listening" }
            androidx.core.content.ContextCompat.startForegroundService(context,Intent(context,SmartPracticeService::class.java).setAction(START)); true
        } catch (_: Exception) { (context.applicationContext as OpusApplication).practice.notice.value = L.s(R.string.msg_cannot_start_microphone_while_goldberg_is_no_ee1e); false }
        fun startRecording(context: Context): Boolean = try {
            check((context as? androidx.lifecycle.LifecycleOwner)?.lifecycle?.currentState?.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED) == true) { "Open Goldberg to start recording" }
            androidx.core.content.ContextCompat.startForegroundService(context,Intent(context,SmartPracticeService::class.java).setAction(START_RECORDING)); true
        } catch (_: Exception) { false }
    }
}
class PracticeActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as OpusApplication
        val practice = app.practice
        val token = intent.getStringExtra("token")
        val execution = intent.getLongExtra("execution",-1)
        val audioId = intent.getStringExtra("recordingId")
        val pending = goAsync()
        practice.command {
            val sameSession = state.value.token == token && state.value.execution == execution
            val sameRecording = !audioId.isNullOrEmpty() && app.recording.state.value.id == audioId &&
                app.recording.state.value.phase == RecordingController.Phase.Recording
            when (intent.action) {
                SmartPracticeService.PAUSE -> if (sameSession && state.value.running) pause()
                SmartPracticeService.STOP -> {
                    if (sameSession && state.value.token.isNotEmpty()) review()
                    else if (sameRecording) app.recording.stop()
                }
            }
        }.invokeOnCompletion { pending?.finish() }
    }
}
