package com.opus.piano.data.catalogue

import org.junit.Assert.assertEquals
import org.junit.Test

class CataloguePerformanceTest {
    @Test fun performanceContextUsesStableNonQueryCorrelationFields() {
        val context = CataloguePerformance.Context("123e4567-e89b-12d3-a456-426614174000", "online", "artists")

        assertEquals("session_id=123e4567-e89b-12d3-a456-426614174000 source=online category=artists", context.fields())
    }

    @Test fun invalidSessionOrCategoriesCannotLeakThroughDiagnosticFields() {
        val context = CataloguePerformance.Context("private search text", "private source", "private category")

        assertEquals("session_id=invalid source=invalid category=invalid", context.fields())
    }
}
