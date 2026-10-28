package com.opus.piano.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.opus.piano.OpusApplication
import kotlinx.coroutines.launch

abstract class PracticeWidget : AppWidgetProvider() {
    private fun refresh(context: Context) {
        val pending = goAsync()
        WidgetRefresh.scope.launch {
            try {
                com.opus.piano.i18n.L.refresh()
                WidgetRenderer.renderAll(context, WidgetSnapshotStore(context).read())
                WidgetRefresh.observe(context.applicationContext as OpusApplication)
                WidgetRefresh.request(context)
            } catch (_: Exception) {
                // Keep the previous display if a host rejects an update; retry off the receiver.
                runCatching { WidgetRefresh.request(context) }
            } finally { pending.finish() }
        }
    }
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) = refresh(context)
    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, options: Bundle) = refresh(context)
    override fun onEnabled(context: Context) = refresh(context)
    override fun onDisabled(context: Context) = WidgetRefresh.disabled(context)
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action in setOf(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED,
                Intent.ACTION_DATE_CHANGED, Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED,
                Intent.ACTION_LOCALE_CHANGED)) refresh(context)
    }
}

class QuickPracticeWidget : PracticeWidget()
class WeeklyActivityWidget : PracticeWidget()
