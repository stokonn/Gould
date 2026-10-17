package com.opus.piano

import com.opus.piano.identity.IdentityPolicy
import org.junit.Assert.assertEquals
import org.junit.Test

class GoogleIdentityPolicyTest {
    @Test fun initializesDefaultButPreservesLegacyNickname() {
        assertEquals("Anna", IdentityPolicy.name("Musician", null, "Anna", false))
        assertEquals("My nickname", IdentityPolicy.name("My nickname", null, "Anna", false))
    }
    @Test fun synchronizesOnlyUnmodifiedGoogleName() {
        assertEquals("New name", IdentityPolicy.name("Old name", "Old name", "New name", false))
        assertEquals("Old name", IdentityPolicy.name("Old name", "Old name", "New name", true))
        assertEquals("Musician", IdentityPolicy.name("Musician", null, "New name", true))
    }
    @Test fun missingAndOversizedNamesAreSafe() {
        assertEquals("Musician", IdentityPolicy.name("Musician", null, "  ", false))
        assertEquals(80, IdentityPolicy.name("Musician", null, "a".repeat(200), false).length)
    }
}
