package com.opus.piano.reminders

import com.opus.piano.i18n.L
import com.opus.piano.R
import android.app.*
import android.content.*
import androidx.core.app.NotificationCompat
import androidx.room.withTransaction
import com.opus.piano.*
import com.opus.piano.data.*
import kotlinx.coroutines.*
import java.time.*

object ReminderSchedule {
    fun next(now: ZonedDateTime, hour: Int, minute: Int): ZonedDateTime {
        require(hour in 0..23 && minute in 0..59)
        var date = now.toLocalDate()
        var value = date.atTime(hour,minute).atZone(now.zone)
        if (!value.isAfter(now)) { date = date.plusDays(1); value = date.atTime(hour,minute).atZone(now.zone) }
        return value
    }
}
class ReminderScheduler(private val context: Context) {
    private val app get() = context.applicationContext as OpusApplication
    suspend fun schedule() {
        val settings = app.database.practiceDao().localNow() ?: LocalAppState()
        val alarm = context.getSystemService(AlarmManager::class.java)
        val intent = Intent(context,ReminderReceiver::class.java).putExtra("generation",settings.generation)
        val pending = PendingIntent.getBroadcast(context,81,intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarm.cancel(pending)
        if (!settings.reminderEnabled) { context.getSystemService(NotificationManager::class.java).cancel(81); return }
        val next = ReminderSchedule.next(ZonedDateTime.now(),settings.reminderHour,settings.reminderMinute)
        alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,next.toInstant().toEpochMilli(),pending)
    }
    suspend fun configure(enabled: Boolean, hour: Int, minute: Int) {
        require(hour in 0..23 && minute in 0..59)
        app.database.withTransaction {
            val old = app.database.practiceDao().localNow() ?: LocalAppState()
            app.database.practiceDao().local(old.copy(reminderEnabled = enabled, reminderHour = hour, reminderMinute = minute, generation = old.generation + 1))
        }
        schedule()
    }
    suspend fun deliver(generation: Long) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel("practice-reminders",L.s(R.string.msg_practice_reminders_f838),NotificationManager.IMPORTANCE_DEFAULT))
        app.database.withTransaction {
            val s = app.database.practiceDao().localNow() ?: return@withTransaction
            val now = ZonedDateTime.now(); val day = now.toLocalDate().toString()
            val target = now.toLocalDate().atTime(s.reminderHour,s.reminderMinute).atZone(now.zone)
            if (!s.reminderEnabled || s.generation != generation || s.lastReminderDay == day || now.isBefore(target) || Duration.between(target,now).toMinutes() > 120) return@withTransaction
            app.database.practiceDao().local(s.copy(lastReminderDay = day))
            if (!app.practice.state.value.running && androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                val open = PendingIntent.getActivity(context,81,Intent(context,MainActivity::class.java).putExtra("practice",true).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                try { manager.notify(81,NotificationCompat.Builder(context,"practice-reminders").setSmallIcon(R.drawable.ic_goldberg_monochrome).setContentTitle(L.s(R.string.msg_a_little_time_at_the_piano_4104)).setContentText(L.s(R.string.msg_open_goldberg_when_you_re_ready_to_practice_cf21)).setContentIntent(open).setAutoCancel(true).build()) } catch (_: SecurityException) { /* User controls notifications. */ }
            }
        }
        schedule()
    }
}
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch { try { ReminderScheduler(context).deliver(intent.getLongExtra("generation",-1)) } finally { result.finish() } }
    }
}
class ReminderRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in setOf(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED, Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED)) return
        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch { try { ReminderScheduler(context).schedule() } finally { result.finish() } }
    }
}
