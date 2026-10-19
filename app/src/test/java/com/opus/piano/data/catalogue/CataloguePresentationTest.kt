package com.opus.piano.data.catalogue

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Legacy numeric/opaque fallbacks must never render as invented artist or work names. */
class CataloguePresentationTest {
    @Test fun legacyNumericAndOpaqueFallbacksAreNeverPresentedAsNames() {
        // A numeric legacy composer identifier is a label, not a name.
        assertTrue(CataloguePresentation.unnamed("artist", "87"))
        assertTrue(CataloguePresentation.unnamed("artist", "007"))
        assertTrue(CataloguePresentation.unnamed("artist", "   "))
        assertFalse(CataloguePresentation.unnamed("artist", "Johann Sebastian Bach"))
        // Numeric-looking real titles of non-artist kinds are preserved verbatim.
        assertFalse(CataloguePresentation.unnamed("album", "1984"))
        assertFalse(CataloguePresentation.unnamed("work", "4'33\""))
        assertFalse(CataloguePresentation.unnamed("collection", "2026"))
        // Personal copy falls back to a blank title or the opaque identity key.
        assertTrue(CataloguePresentation.unnamed("work", ""))
        assertTrue(CataloguePresentation.unnamed("work", "12345678-1234-4234-8234-123456789abc"))
        assertTrue(CataloguePresentation.unnamed("collection", " 12345678-1234-4234-8234-123456789abc "))
        assertFalse(CataloguePresentation.unnamed("collection", "12345678-1234-4234-8234-123456789abz"))
        assertFalse(CataloguePresentation.unnamed("collection", "Goldberg Variations"))
    }
}
