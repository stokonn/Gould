package com.opus.piano

import com.opus.piano.data.QuoteLibrary
import com.opus.piano.preferences.MediaPolicy
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class Quality092Test {
    @Test fun exactlyTenStableVerifiedOfflineQuotes() {
        assertEquals(10,QuoteLibrary.entries.size)
        assertEquals(10,QuoteLibrary.entries.map {it.english}.distinct().size)
        val start=LocalDate.of(2026,10,16)
        assertEquals(10,(0L..9L).map {QuoteLibrary.forDay(start.plusDays(it)).id}.toSet().size)
        assertEquals(QuoteLibrary.forDay(start),QuoteLibrary.forDay(start))
        assertEquals(QuoteLibrary.forDay(start),QuoteLibrary.forDay(start.plusDays(10)))
        assertTrue(QuoteLibrary.verified("\nPlay strictly\n in time!\n",QuoteLibrary.entries.first()))
        assertFalse(QuoteLibrary.verified("Unverified anonymous quotation",QuoteLibrary.entries.first()))
        assertEquals(9,QuoteLibrary.forDay(LocalDate.ofEpochDay(-1)).id)
    }
    @Test fun meteredAndCellularMediaAlwaysRequireConsent() {
        for(wifi in listOf(false,true)) for(metered in listOf(false,true)) {
            assertFalse(MediaPolicy.allows(false,wifi,metered,true))
            assertTrue(MediaPolicy.allows(true,wifi,metered,true))
            assertEquals(wifi && !metered,MediaPolicy.allows(true,wifi,metered,false))
        }
    }
}
