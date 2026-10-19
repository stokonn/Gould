package com.opus.piano.data.catalogue

import org.junit.Assert.*
import org.junit.Test

class CatalogueBudgetTest {
    @Test fun dynamicReserveIncludesDatabaseCriticalFilesTwoExportsAndJournalGrowth() {
        val mib=1024L*1024
        assertEquals(128*mib,CatalogueBudget.reserve(1*mib))
        assertEquals(296*mib,CatalogueBudget.reserve(150*mib,50*mib))
        assertEquals(Long.MAX_VALUE,CatalogueBudget.reserve(Long.MAX_VALUE,Long.MAX_VALUE))
        assertTrue(runCatching {CatalogueBudget.reserve(-1)}.isFailure)
    }
    @Test fun admissionCannotOverflowOrConsumeCriticalReserve() {
        assertTrue(CatalogueBudget.fits(1000,100,900))
        assertFalse(CatalogueBudget.fits(1000,101,900))
        assertFalse(CatalogueBudget.fits(1000,Long.MAX_VALUE,900))
        assertFalse(CatalogueBudget.fits(Long.MAX_VALUE,1,Long.MAX_VALUE))
        assertFalse(CatalogueBudget.fits(-1,0,0))
    }
}
