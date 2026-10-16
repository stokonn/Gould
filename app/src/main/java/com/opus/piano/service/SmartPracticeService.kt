package com.opus.piano.service

import android.app.*
import android.content.*
import android.content.pm.ServiceInfo
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.opus.piano.MainActivity
import com.opus.piano.OpusApplication
import com.opus.piano.audio.MicrophoneController
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class SmartPracticeService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val practice get() = (application as OpusApplication).practice
    private var microphone: MicrophoneController? = null
    private var capture: Job? = null
    private var token = ""
    private var execution = -1L
    override fun onBind(intent: Intent?) = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action != START || !practice.state.value.ready || capture != null) { if (capture == null) stopSelf() else if (!practice.state.value.running) practice.notice.value = "Microphone is stopping. Please retry in a moment."; return START_NOT_STICKY }
        try {
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(NotificationChannel(CHANNEL,"Smart Practice",NotificationManager.IMPORTANCE_LOW))
            ServiceCompat.startForeground(this,ID,notification("Starting microphone"),if (Build.VERSION.SDK_INT >= 30) ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE else 0)
            capture = scope.launch {
                try {
                    val settings = (application as OpusApplication).database.personalDao().getSettings() ?: com.opus.piano.data.ProfileSettings()
                    val started = practice.startSmart(settings.sensitivity,settings.pauseSeconds)
                    token = started.token; execution = started.execution
                    val mic = MicrophoneController(this@SmartPracticeService); microphone = mic
                    practice.releaseCapture = { mic.stop() }
                    launch {
                        practice.state.map { it.status to it.running }.distinctUntilChanged().collect { (status,running) ->
                            if (!running) { microphone?.stop(); stopForeground(STOP_FOREGROUND_REMOVE); stopSelf() }
                            else manager.notify(ID,notification(status))
                            delay(1000)
                        }
                    }
                    mic.capture({ practice.state.value.sensitivity }) { practice.evidence(token,execution,it) }
                } catch (c: CancellationException) { throw c }
                catch (_: Exception) { practice.notice.value = "Smart Practice interrupted. Open Practice to resume or use the manual timer." }
                finally {
                    microphone?.stop(); practice.releaseCapture = null
                    withContext(NonCancellable) { practice.interrupt(token,execution) }
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    val finalState = practice.state.value
                    if (androidx.core.app.NotificationManagerCompat.from(this@SmartPracticeService).areNotificationsEnabled()) {
                        val open = PendingIntent.getActivity(this@SmartPracticeService,0,Intent(this@SmartPracticeService,MainActivity::class.java).putExtra("practice",true).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                        val text = if (finalState.status == "Review") "Session ready to review" else "${finalState.status} — open Goldberg to resume"
                        try { manager.notify(ID,NotificationCompat.Builder(this@SmartPracticeService,CHANNEL).setSmallIcon(com.opus.piano.R.drawable.ic_goldberg_monochrome).setContentTitle("Goldberg — Smart Practice").setContentText(text).setContentIntent(open).setAutoCancel(true).setSilent(true).build()) } catch (_: SecurityException) { /* Respect notification denial. */ }
                    }
                    stopSelf()
                }
            }
        } catch (_: Exception) {
            microphone?.stop(); practice.notice.value = "Microphone service could not start. Keep Goldberg visible and retry, or use Manual."
            stopForeground(STOP_FOREGROUND_REMOVE); stopSelf()
        }
        return START_NOT_STICKY
    }
    private fun notification(status: String): Notification {
        val state = practice.state.value
        fun action(name: String) = PendingIntent.getBroadcast(this, if (name == PAUSE) 1 else 2,
            Intent(this,PracticeActionReceiver::class.java).setAction(name).putExtra("token",state.token).putExtra("execution",state.execution), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val open = PendingIntent.getActivity(this,0,Intent(this,MainActivity::class.java).putExtra("practice",true).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this,CHANNEL).setSmallIcon(com.opus.piano.R.drawable.ic_goldberg_monochrome)
            .setContentTitle("Goldberg — Smart Practice").setContentText(if (status == "Listening") "Listening for piano playing" else status)
            .setContentIntent(open).setOngoing(true).setOnlyAlertOnce(true).setSilent(true).setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(0,"Pause",action(PAUSE)).addAction(0,"Stop",action(STOP)).build()
    }
    override fun onDestroy() { microphone?.stop(); scope.cancel(); super.onDestroy() }
    companion object {
        const val START = "com.opus.piano.START_SMART"
        const val PAUSE = "com.opus.piano.PAUSE"
        const val STOP = "com.opus.piano.STOP"
        const val CHANNEL = "smart-practice"
        const val ID = 80
        fun start(context: Context): Boolean = try {
            check((context as? androidx.lifecycle.LifecycleOwner)?.lifecycle?.currentState?.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED) == true) { "Open Goldberg to start listening" }
            androidx.core.content.ContextCompat.startForegroundService(context,Intent(context,SmartPracticeService::class.java).setAction(START)); true
        } catch (_: Exception) { (context.applicationContext as OpusApplication).practice.notice.value = "Cannot start microphone while Goldberg is not visible or permission is unavailable."; false }
    }
}
class PracticeActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val practice = (context.applicationContext as OpusApplication).practice
        val token = intent.getStringExtra("token")
        val execution = intent.getLongExtra("execution",-1)
        practice.command {
            if (state.value.token == token && state.value.execution == execution && state.value.running) {
                when (intent.action) { SmartPracticeService.PAUSE -> pause(); SmartPracticeService.STOP -> review() }
            }
        }
    }
}
