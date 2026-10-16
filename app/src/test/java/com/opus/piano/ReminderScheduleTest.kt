package com.opus.piano

import com.opus.piano.reminders.ReminderSchedule
import org.junit.Assert.*
import org.junit.Test
import java.time.*
class ReminderScheduleTest {
    @Test fun nextReminderFollowsLocalDateAcrossDaylightSaving() {
        val now=ZonedDateTime.of(2026,3,28,19,0,0,0,ZoneId.of("Europe/Prague"))
        val next=ReminderSchedule.next(now,2,30)
        assertEquals(LocalDate.of(2026,3,29),next.toLocalDate());assertEquals(3,next.hour)
        assertTrue(next.isAfter(now))
    }
    @Test fun exactlyDueSchedulesTomorrowNotDuplicateToday() {
        val now=ZonedDateTime.parse("2026-10-16T18:00:00+02:00[Europe/Prague]")
        assertEquals(now.toLocalDate().plusDays(1),ReminderSchedule.next(now,18,0).toLocalDate())
    }
}
