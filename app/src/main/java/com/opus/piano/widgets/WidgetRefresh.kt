package com.opus.piano.widgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.work.*
import com.opus.piano.OpusApplication
import com.opus.piano.practice.PracticeCoordinator
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

object WidgetRefresh {
    internal val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var observations: Job? = null
    private var practiceObservation: Job? = null
    private var watchedPractice: PracticeCoordinator? = null
    private const val TAG = "gould-widget-display"
    fun installed(context: Context): Boolean {
        val manager = AppWidgetManager.getInstance(context)
        return listOf(QuickPracticeWidget::class.java, WeeklyActivityWidget::class.java)
            .any { manager.getAppWidgetIds(ComponentName(context, it)).isNotEmpty() }
    }
    @Synchronized fun observe(app: OpusApplication) {
        if (!installed(app)) return
        if (observations?.isActive != true) observations = scope.launch {
            coroutineScope {
                launch {
                    app.database.invalidationTracker.createFlow("sessions", "session_parts", "pieces", "profile_settings")
                        .drop(1).debounce(750).collect { request(app) }
                }
                launch { app.appearance.state.map { it.accent to it.cards }.distinctUntilChanged().drop(1).collect { request(app) } }
            }
        }
        app.currentPracticeForWidgets()?.let { watchPractice(app, it) }
        val periodic = PeriodicWorkRequestBuilder<WidgetRefreshWorker>(6, TimeUnit.HOURS).addTag(TAG).build()
        WorkManager.getInstance(app).enqueueUniquePeriodicWork("gould-widget-periodic", ExistingPeriodicWorkPolicy.KEEP, periodic)
    }
    @Synchronized fun watchPractice(app: OpusApplication, coordinator: PracticeCoordinator) {
        if (!installed(app) || watchedPractice === coordinator) return
        practiceObservation?.cancel(); watchedPractice = coordinator
        practiceObservation = scope.launch {
            // Exclude credit ticks and notes. Only navigation-relevant context changes trigger work.
            coordinator.state.map { state ->
                val context = widgetPractice(state, null, false)
                Triple(state.ready, context, state.token.isNotBlank())
            }.distinctUntilChanged().drop(1).debounce(750).collect { request(app) }
        }
    }
    fun request(context: Context) {
        if (!installed(context)) return
        val work = OneTimeWorkRequestBuilder<WidgetRefreshWorker>().addTag(TAG).build()
        WorkManager.getInstance(context).enqueueUniqueWork("gould-widget-refresh", ExistingWorkPolicy.REPLACE, work)
    }
    fun rollover(context: Context, now: ZonedDateTime = ZonedDateTime.now()) {
        val next = widgetNextMidnight(now)
        val work = OneTimeWorkRequestBuilder<WidgetRefreshWorker>()
            .setInitialDelay(Duration.between(now, next).toMillis().coerceAtLeast(1), TimeUnit.MILLISECONDS).addTag(TAG).build()
        WorkManager.getInstance(context).enqueueUniqueWork("gould-widget-day-${next.toLocalDate()}-${now.zone.id}", ExistingWorkPolicy.KEEP, work)
    }
    @Synchronized fun disabled(context: Context) {
        if (installed(context)) return
        observations?.cancel(); observations = null
        practiceObservation?.cancel(); practiceObservation = null; watchedPractice = null
        WorkManager.getInstance(context).cancelAllWorkByTag(TAG)
    }
}

class WidgetRefreshWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        if (!WidgetRefresh.installed(applicationContext)) return@withContext Result.success()
        val app = applicationContext as OpusApplication
        val store = WidgetSnapshotStore(app)
        try {
            com.opus.piano.i18n.L.refresh()
            val snapshot = loadWidgetSnapshot(app)
            store.write(snapshot)
            WidgetRenderer.renderAll(app, snapshot)
            WidgetRefresh.rollover(app)
            Result.success()
        } catch (c: CancellationException) { throw c }
        catch (_: Exception) {
            // A failed projection never becomes a verified fresh zero. Retain the previous week/date.
            runCatching { WidgetRenderer.renderAll(app, store.read(), forceStale = true) }
            Result.retry()
        }
    }
}
