package com.opus.piano.i18n

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

/** Explicit locale and zone keep application language independent of system defaults. */
object JournalFormatting {
    fun weekday(date: LocalDate, locale: Locale): String = date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
    fun dateTime(timestamp: Long, locale: Locale, zone: ZoneId): String =
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(locale).withZone(zone).format(Instant.ofEpochMilli(timestamp))
}
