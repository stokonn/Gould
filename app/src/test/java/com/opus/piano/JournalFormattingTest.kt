package com.opus.piano

import com.opus.piano.i18n.JournalFormatting
import org.junit.Assert.*
import org.junit.Test
import java.time.*
import java.util.Locale

class JournalFormattingTest {
    @Test fun weekdaysFollowApplicationLocale() {
        val monday=LocalDate.of(2026,10,12)
        assertEquals("Mon",JournalFormatting.weekday(monday,Locale.ENGLISH))
        assertEquals("po",JournalFormatting.weekday(monday,Locale.forLanguageTag("cs-CZ")))
    }
    @Test fun dateTimeUsesExplicitLocaleAndZoneAcrossMidnight() {
        val timestamp=Instant.parse("2026-10-16T23:30:00Z").toEpochMilli()
        val cs=JournalFormatting.dateTime(timestamp,Locale.forLanguageTag("cs-CZ"),ZoneId.of("Europe/Prague"))
        val en=JournalFormatting.dateTime(timestamp,Locale.US,ZoneId.of("UTC"))
        assertTrue(cs.contains("17. 10. 2026")); assertTrue(cs.contains("1:30"))
        assertTrue(en.contains("Oct 16, 2026")); assertTrue(en.contains("11:30"))
    }
}
