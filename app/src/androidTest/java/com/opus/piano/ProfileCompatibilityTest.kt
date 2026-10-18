package com.opus.piano

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.data.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** A format-1/schema-4 fixture matching the unchanged 0.6.0 backup contract. */
@RunWith(AndroidJUnit4::class)
class ProfileCompatibilityTest {
    @Test fun restore060FormatIntoExistingJournalAndKeepProfileAcrossThemeChanges() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val db = Room.inMemoryDatabaseBuilder(instrumentation.targetContext, OpusDatabase::class.java).build()
        try {
            db.dao().save(Piece(title = "Existing test row", composer = "Composer"))
            val raw = instrumentation.context.assets.open("backups/goldberg-0.6.0-format1.json").bufferedReader().use { it.readText() }
            val backup = BackupRepository(instrumentation.targetContext, db)
            backup.restore(raw)
            assertEquals(17L, db.dao().allPieces().single().id)
            assertEquals("Preserve original notes", db.dao().allPieces().single().notes)
            assertEquals(60000L, db.dao().allSessions().single().durationMillis)
            assertEquals("Keep these session notes", db.personalDao().allParts().single().notes)
            assertEquals(123L, db.experienceDao().allUnlocks().single().unlockedAt)
            val profile = db.personalDao().getSettings()!!
            assertEquals("Existing musician", profile.username)
            assertEquals(listOf("first_notes"), profile.badgeIds)
            for (theme in listOf("Light", "Dark", "System")) {
                db.personalDao().saveSettings(profile.copy(theme = theme))
                assertEquals(profile.copy(theme = theme), db.personalDao().getSettings())
                val exported = backup.exportLegacy()
                backup.restore(exported)
                assertEquals(theme, db.personalDao().getSettings()!!.theme)
                assertEquals(listOf("first_notes"), db.personalDao().getSettings()!!.badgeIds)
            }
        } finally { db.close() }
    }
}
