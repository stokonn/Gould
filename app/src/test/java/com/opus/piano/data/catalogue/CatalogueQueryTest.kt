package com.opus.piano.data.catalogue

import org.junit.Assert.*
import org.junit.Test

class CatalogueQueryTest {
    @Test fun fts4PrefixesAreQuotedInsideTokens() {
        assertEquals("\"zlutoucka*\" \"sklad*\"",CatalogueValidation.query("Žluťoučká sklad*"))
        assertEquals("\"or*\" \"near*\"",CatalogueValidation.query("\" OR * NEAR ()"))
        assertEquals("",CatalogueValidation.query("*()\""))
        assertEquals("試験作品",CatalogueValidation.normalized("試験作品"))
        assertEquals(8,CatalogueValidation.query((1..20).joinToString(" ")).split(' ').size)
    }
    @Test fun uuidRequiresCanonicalSpelling() {
        CatalogueValidation.uuid("6e37c82a-260e-5375-8d92-429f75e599f0")
        assertTrue(runCatching {CatalogueValidation.uuid("1-1-1-1-1")}.isFailure)
        assertTrue(runCatching {CatalogueValidation.uuid("6E37C82A-260E-5375-8D92-429F75E599F0")}.isFailure)
    }
}
