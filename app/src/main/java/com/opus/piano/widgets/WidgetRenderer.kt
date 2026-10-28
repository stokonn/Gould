package com.opus.piano.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.text.SpannableString
import android.text.Spanned
import android.text.style.TypefaceSpan
import android.util.SizeF
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.RemoteViews
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.res.ResourcesCompat
import com.opus.piano.MainActivity
import com.opus.piano.R
import com.opus.piano.i18n.L
import com.opus.piano.ui.journalColors
import com.opus.piano.ui.localizedPracticeTime
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle

object WidgetRenderer {
    internal fun openIntent(context: Context, kind: String, id: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
            .setData(Uri.parse("gould-widget://studio/$kind/$id"))
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .putExtra("practice", true)
        return PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
    fun renderAll(context: Context, snapshot: WidgetSnapshot?, forceStale: Boolean = false) {
        val manager = AppWidgetManager.getInstance(context)
        listOf(QuickPracticeWidget::class.java to true, WeeklyActivityWidget::class.java to false).forEach { (type, quick) ->
            manager.getAppWidgetIds(ComponentName(context, type)).forEach { id ->
                val views = if (Build.VERSION.SDK_INT >= 31) {
                    val sizes = if (quick) listOf(SizeF(48f, 48f), SizeF(120f, 52f), SizeF(250f, 90f), SizeF(250f, 180f))
                        else listOf(SizeF(120f, 80f), SizeF(140f, 160f), SizeF(280f, 160f), SizeF(280f, 230f))
                    RemoteViews(sizes.associateWith { view(context, snapshot, quick, id, it.width, it.height, forceStale) })
                } else {
                    val options = manager.getAppWidgetOptions(id)
                    val landscape = context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
                    val width = options.getInt(if (landscape) AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH else AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 140)
                    val height = options.getInt(if (landscape) AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT else AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 140)
                    view(context, snapshot, quick, id, width.toFloat(), height.toFloat(), forceStale)
                }
                manager.updateAppWidget(id, views)
            }
        }
    }
    internal fun view(context: Context, snapshot: WidgetSnapshot?, quick: Boolean, id: Int, width: Float, height: Float, forceStale: Boolean = false): RemoteViews {
        val views = RemoteViews(context.packageName, if (quick) R.layout.widget_quick_practice else R.layout.widget_weekly_activity)
        val dark = when (snapshot?.theme) {
            "Dark" -> true
            "Light" -> false
            else -> context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        }
        val colors = journalColors(dark, snapshot?.appearance ?: com.opus.piano.preferences.Appearance())
        views.setInt(R.id.widget_background, "setColorFilter", colors.surface.toArgb())
        views.setOnClickPendingIntent(R.id.widget_root, openIntent(context, if (quick) "quick" else "week", id))
        fun text(view: Int, value: String, primary: Boolean = false) {
            val rendered: CharSequence = if (snapshot?.typography == "System") SpannableString(value).apply {
                setSpan(TypefaceSpan("sans-serif"), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            } else value
            views.setTextViewText(view, rendered)
            views.setTextColor(view, (if (primary) colors.onSurface else colors.onSurfaceVariant).toArgb())
        }
        val now = System.currentTimeMillis()
        val zone = ZoneId.systemDefault()
        val stale = snapshot != null && (forceStale || widgetSnapshotStale(snapshot.generatedAt, now, snapshot.week.monday, LocalDate.now(zone), snapshot.zone, zone))
        val last = snapshot?.let {
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(L.locale)
                .format(Instant.ofEpochMilli(it.generatedAt).atZone(zone))
        }
        val status = if (last == null) L.s(R.string.widget_unavailable)
            else L.s(if (stale) R.string.widget_stale else R.string.widget_verified, last)
        val font = context.resources.configuration.fontScale
        if (quick) {
            val compact = width < 96f
            val titleOnly = !compact && font >= 1.5f && width < 180f
            val showStatus = height >= 100f * font && !titleOnly && !compact
            val label = snapshot?.practice?.let { practice ->
                val state = L.s(when (practice.mode) {
                    "active" -> R.string.widget_active
                    "draft" -> R.string.widget_draft
                    "review" -> R.string.widget_review
                    "recovery" -> R.string.widget_recovery
                    else -> R.string.widget_ready
                })
                val title = if (practice.freePractice) L.s(R.string.msg_free_practice_524f) else practice.title
                "$state · $title"
            } ?: L.s(R.string.widget_unavailable)
            text(R.id.widget_title, if (titleOnly) L.fixed("Practice") else L.s(R.string.widget_open_studio), true)
            views.setTextViewTextSize(R.id.widget_title, TypedValue.COMPLEX_UNIT_SP, if (titleOnly) 13f else 15f)
            text(R.id.widget_context, label)
            text(R.id.widget_status, status)
            text(R.id.widget_compact_status, if (stale || snapshot == null) "!" else "")
            views.setViewVisibility(R.id.widget_text, if (compact) View.GONE else View.VISIBLE)
            views.setViewVisibility(R.id.widget_logo, if (titleOnly) View.GONE else View.VISIBLE)
            views.setViewVisibility(R.id.widget_context, if (height < 70f * font || titleOnly) View.GONE else View.VISIBLE)
            views.setViewVisibility(R.id.widget_status, if (showStatus) View.VISIBLE else View.GONE)
            views.setViewVisibility(R.id.widget_compact_status, if (!showStatus && (stale || snapshot == null)) View.VISIBLE else View.GONE)
            if (compact) {
                val padding = (4 * context.resources.displayMetrics.density).toInt()
                views.setViewPadding(R.id.widget_content, padding, padding, padding, padding)
            }
            views.setInt(R.id.widget_content, "setGravity", if (compact) Gravity.CENTER else Gravity.CENTER_VERTICAL)
            views.setContentDescription(R.id.widget_root, L.s(R.string.widget_accessibility, L.s(R.string.widget_open_studio), label, status))
        } else {
            val showChart = snapshot != null && height >= maxOf(150f, 140f + 65f * (font - 1f).coerceAtLeast(0f)) && width >= (if (font > 1.5f) 230f else 130f)
            val expanded = width >= 250f && height >= 150f && font <= 1.5f
            val compact = height < 140f
            val value = snapshot?.week?.total?.let { total ->
                if (compact && total >= 3_600_000) L.s(R.string.widget_hours_short,
                    NumberFormat.getNumberInstance(L.locale).apply { maximumFractionDigits = 1 }.format(total / 3_600_000.0))
                else localizedPracticeTime(total)
            } ?: "—"
            val week = snapshot?.let { L.s(R.string.widget_week_of,
                DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(L.locale).format(it.week.monday)) }
                ?: L.s(R.string.widget_unavailable)
            text(R.id.widget_title, L.s(R.string.widget_weekly_title))
            text(R.id.widget_value, value, true)
            text(R.id.widget_context, week)
            text(R.id.widget_status, status)
            text(R.id.widget_details, snapshot?.let { L.s(R.string.widget_details,
                L.quantity(R.plurals.sessions_count, it.week.sessions), L.quantity(R.plurals.days_count, it.week.practicedDays)) }.orEmpty())
            views.setTextViewTextSize(R.id.widget_value, TypedValue.COMPLEX_UNIT_SP, if (compact) 16f else 24f)
            views.setViewVisibility(R.id.widget_chart, if (showChart) View.VISIBLE else View.GONE)
            views.setViewVisibility(R.id.widget_details, if (expanded) View.VISIBLE else View.GONE)
            views.setViewVisibility(R.id.widget_title, if (compact && font > 1.25f) View.GONE else View.VISIBLE)
            views.setViewVisibility(R.id.widget_context, if (compact) View.GONE else View.VISIBLE)
            if (compact) {
                val padding = (8 * context.resources.displayMetrics.density).toInt()
                views.setViewPadding(R.id.widget_content, padding, padding, padding, padding)
            }
            if (showChart) views.setImageViewBitmap(R.id.widget_chart, chart(context, snapshot!!.week, width - 20f, colors.primary.toArgb(), colors.onSurfaceVariant.toArgb(), colors.outlineVariant.toArgb()))
            val days = snapshot?.week?.credited?.mapIndexed { index, millis ->
                val day = snapshot.week.monday.plusDays(index.toLong()).dayOfWeek.getDisplayName(TextStyle.FULL, L.locale)
                "$day ${localizedPracticeTime(millis)}"
            }?.joinToString(". ").orEmpty()
            views.setContentDescription(R.id.widget_root, L.s(R.string.widget_accessibility,
                "${L.s(R.string.widget_weekly_title)} $value. $week", days, status))
        }
        return views
    }
    private fun chart(context: Context, week: WidgetWeek, widthDp: Float, accent: Int, ink: Int, baseline: Int): Bitmap {
        val density = context.resources.displayMetrics.density
        val width = (widthDp.coerceIn(80f, 500f) * density).toInt()
        val height = (64 * density).toInt()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val fontScale = context.resources.configuration.fontScale.coerceAtMost(2f)
        paint.typeface = ResourcesCompat.getFont(context, R.font.inter)
        paint.textSize = 10f * density * fontScale
        paint.textAlign = Paint.Align.CENTER
        val labelHeight = paint.fontMetrics.bottom - paint.fontMetrics.top
        val bottom = height - labelHeight - 4 * density
        val max = week.credited.maxOrNull()?.coerceAtLeast(1) ?: 1L
        val slot = width / 7f
        week.credited.forEachIndexed { index, value ->
            val x = slot * (index + .5f)
            paint.color = if (value > 0) accent else baseline
            val top = bottom - (value.toDouble() / max * (bottom - 3 * density)).toFloat().coerceAtLeast(2 * density)
            canvas.drawRoundRect(x - slot * .18f, top, x + slot * .18f, bottom, 2 * density, 2 * density, paint)
            paint.color = ink
            val label = week.monday.plusDays(index.toLong()).dayOfWeek.getDisplayName(TextStyle.SHORT_STANDALONE, L.locale).take(2)
            canvas.drawText(label, x, height - paint.fontMetrics.bottom, paint)
        }
        return bitmap
    }
}
