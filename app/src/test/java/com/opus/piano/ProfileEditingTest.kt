package com.opus.piano

import com.opus.piano.data.*
import org.junit.Assert.*
import org.junit.Test

class ProfileEditingTest {
    private val settings = ProfileSettings(username = "Original", favoriteArtist = "Dvořák", favoriteEra = "My historical era",
        biography = "Original bio", avatar = "original image", theme = "Dark", showcase = "first_steps")
    private val original = ProfileEditValues.from(settings)

    @Test fun draftRoundTripsAllFieldsAndPrivateImageIdentity() {
        val draft = ProfileEditDraft(original, original.copy(name = "Unsaved", era = "Vlastní období"), "original-hash", "1234-abcd.jpg")
        assertEquals(draft, ProfileEditDraft.restore(draft.fields()))
        assertTrue(draft.dirty)
        assertFalse(ProfileEditDraft(original, original, "hash").dirty)
        assertNull(ProfileEditDraft.restore(listOf("unsupported")))
    }
    @Test fun saveMergesOnlyEditedFieldsAndRetainsUnrelatedSettings() {
        val newer = settings.copy(theme = "Light", showcase = "curious_pianist", favoriteArtist = "Newer composer", avatar = "newer image")
        val saved = mergeProfileEdit(newer, original, original.copy(biography = " New bio "))
        assertEquals(newer.copy(biography = "New bio"), saved)
    }
    @Test fun unchangedHistoricalValuesAreNeverTrimmedOrTruncated() {
        val historical = settings.copy(favoriteEra = "  custom era  ", biography = "b".repeat(450))
        assertEquals(historical, mergeProfileEdit(historical, ProfileEditValues.from(historical), ProfileEditValues.from(historical)))
    }
    @Test(expected = ProfileEditConflict::class) fun conflictingWritesCannotOverwriteNewerIdentity() {
        mergeProfileEdit(settings.copy(username = "Newer"), original, original.copy(name = "My draft"))
    }
    @Test fun retryAfterCommittedWriteIsIdempotent() {
        val draft = original.copy(name = "New name", biography = "New bio")
        val first = mergeProfileEdit(settings, original, draft)
        assertEquals(first, mergeProfileEdit(first, original, draft))
    }
    @Test(expected = IllegalArgumentException::class) fun blankNameCannotProduceSuccessfulSave() {
        mergeProfileEdit(settings, original, original.copy(name = "  "))
    }
    @Test fun statisticsCommitNormalizesOrderWithoutTouchingOtherPreferences() {
        val saved = mergeProfileEdit(settings, original, original.copy(statistics = "streak,time,time,unknown"))
        assertEquals(StatisticLayout.normalize(listOf("streak", "time", "time", "unknown")).joinToString(","), saved.statistics)
        assertEquals(settings.theme, saved.theme)
    }
    @Test fun erasMatchLocalizedAndCanonicalAccentsWithDistinctLocalEntries() {
        val periods = listOf("Baroque", "Romantic", "Baroque", "", "Modern")
        val labels = mapOf("Baroque" to "Baroko", "Romantic" to "Romantismus", "Modern" to "Současná hudba")
        assertEquals(listOf("Modern"), eraSuggestions(periods, "soucasna") { labels.getValue(it) })
        assertEquals(listOf("Baroque"), eraSuggestions(periods, "baroque") { labels.getValue(it) })
        assertEquals(listOf("Baroque"), eraSuggestions(periods, "baroko") { labels.getValue(it) })
        assertEquals(3, eraSuggestions(periods, "") { labels.getValue(it) }.size)
        assertTrue(eraSuggestions(periods, "invented era") { labels.getValue(it) }.isEmpty())
    }
}
