package com.opus.piano

import android.content.res.Configuration
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.data.*
import com.opus.piano.preferences.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class RedesignPersistenceTest {
    @Test fun appearancePersistsAndResetNeverTouchesPersonalDatabase()=runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val isolated=context.createDeviceProtectedStorageContext()
        val store=AppearanceStore(isolated)
        val db=Room.inMemoryDatabaseBuilder(context,OpusDatabase::class.java).build()
        try {
            db.dao().save(Piece(7,"Untouched","Composer",notes="Personal notes"))
            db.personalDao().saveSettings(ProfileSettings(username="Existing user",showcase="curious_pianist",smartEnabled=true))
            db.experienceDao().unlock(listOf(BadgeUnlock("curious_pianist",123)))
            store.edit { it.copy(accent="Olive",metrics=listOf("week","pieces"),density="Compact") }
            withTimeout(5000) { store.state.first { it.accent=="Olive" } }
            val reopened=AppearanceStore(isolated)
            withTimeout(5000) { reopened.state.first { it.accent=="Olive" } }
            assertEquals(listOf("week","pieces"),reopened.state.value.metrics)
            reopened.reset();withTimeout(5000) { reopened.state.first { it.accent=="Gold" } }
            assertEquals("Personal notes",db.dao().allPieces().single().notes)
            assertEquals("Existing user",db.personalDao().getSettings()!!.username)
            assertTrue(db.personalDao().getSettings()!!.smartEnabled)
            assertEquals(123L,db.experienceDao().allUnlocks().single().unlockedAt)
        } finally { db.close() }
    }
    @Test fun czechPluralResourcesAndEnglishFallbackAreRealAndroidResources() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        fun resources(tag:String)=context.createConfigurationContext(Configuration(context.resources.configuration).apply { setLocale(Locale.forLanguageTag(tag)) }).resources
        val cs=resources("cs");val en=resources("en")
        assertEquals("1 skladba",cs.getQuantityString(R.plurals.pieces_count,1,1))
        assertEquals("2 skladby",cs.getQuantityString(R.plurals.pieces_count,2,2))
        assertEquals("5 skladeb",cs.getQuantityString(R.plurals.pieces_count,5,5))
        assertEquals("1 minuta",cs.getQuantityString(R.plurals.minutes_count,1,1))
        assertEquals("5 minut",cs.getQuantityString(R.plurals.minutes_count,5,5))
        assertEquals("2 pieces",en.getQuantityString(R.plurals.pieces_count,2,2))
    }
}
