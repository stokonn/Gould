package com.opus.piano

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.data.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Quality092PersistenceTest {
    @Test fun personalizationAndOriginalRecoveryArchiveRoundTripWithoutTouchingJournal()=runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val db=Room.inMemoryDatabaseBuilder(context,OpusDatabase::class.java).build()
        try {
            val journal=OpusRepository(db,AchievementStore(db))
            val repository=PracticeRepository(db,journal)
            val original="{unreadable but unchanged original data}"
            db.practiceDao().checkpoint(PracticeDraft(payload=original,updatedAt=123))
            try {repository.archiveRecovery("wrong");fail()} catch(_:IllegalStateException) {}
            assertEquals(original,db.practiceDao().draft()!!.payload)
            repository.archiveRecovery(original)
            assertNull(db.practiceDao().draft());assertEquals(original,db.practiceDao().archives().single().payload)
            val settings=ProfileSettings(username="Local nickname",avatar="",showcase="",favoriteArtist="Johann Sebastian Bach",favoritePiece="User's piece",favoriteEra="Baroque",biography="My unchanged text")
            db.personalDao().saveSettings(settings)
            val backup=BackupRepository(context,db)
            val raw=backup.exportLegacy();assertTrue(raw.contains("\"version\":4"))
            backup.restore(raw)
            assertEquals(settings,db.personalDao().getSettings());assertEquals(original,db.practiceDao().archives().single().payload)
            CatalogRepository(db,context.cacheDir).clearCache()
            assertEquals(settings,db.personalDao().getSettings());assertEquals(original,db.practiceDao().archives().single().payload)
        } finally {db.close()}
    }
    @Test fun dailyQuoteRequestsOnceAndOfflineFallbackSurvivesRestart()=runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        // Isolate the preference file using a wrapped context for this test.
        val wrapped=object:android.content.ContextWrapper(context) {
            override fun getSharedPreferences(name:String,mode:Int)=super.getSharedPreferences("test092-$name",mode)
        }
        wrapped.getSharedPreferences("goldberg-quotes",0).edit().clear().commit()
        var requests=0
        val quotes=DailyQuotes(wrapped) {requests++;throw java.io.IOException("Offline")}
        val day=java.time.LocalDate.of(2026,10,16)
        val first=quotes.daily(day)
        assertEquals(first,quotes.daily(day));assertEquals(1,requests)
        assertEquals(first,DailyQuotes(wrapped) {requests++;error("Must not request twice")}.daily(day))
        assertEquals(1,requests)
        quotes.daily(day.plusDays(1));assertEquals(2,requests)
    }
    @Test fun mediaConsentCacheOfflineFailureAndRevocationAreEnforced()=runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val db=Room.inMemoryDatabaseBuilder(context,OpusDatabase::class.java).build()
        val directory=java.io.File(context.cacheDir,"media092-${System.nanoTime()}").apply {mkdirs()}
        var allowed=false;var requests=0;var failDownload=false;var revoke=false
        val image=android.graphics.Bitmap.createBitmap(16,16,android.graphics.Bitmap.Config.ARGB_8888)
        val bytes=java.io.ByteArrayOutputStream().also {image.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)}.toByteArray();image.recycle()
        val http=object:CatalogHttp() {
            override suspend fun bytes(url:String,image:Boolean):ByteArray {requests++;if(failDownload) throw java.io.IOException("Interrupted");if(revoke) allowed=false;return bytes}
        }
        val repository=CatalogRepository(db,directory,http,mediaAllowed={allowed})
        try {
            val url="https://upload.wikimedia.org/test-licensed-portrait.png"
            assertNull(repository.portrait(url));assertEquals(0,requests)
            allowed=true;assertNotNull(repository.portrait(url));assertNotNull(repository.portrait(url));assertEquals(1,requests)
            allowed=false;assertNotNull(repository.portrait(url));assertEquals(1,requests)
            repository.clearCache();assertNull(repository.portrait(url));assertEquals(1,requests)
            allowed=true;failDownload=true
            try {repository.portrait(url);fail()} catch(_:java.io.IOException) {}
            assertEquals(0,repository.cacheBytes())
            failDownload=false;revoke=true;assertNull(repository.portrait(url));assertEquals(0,repository.cacheBytes())
        } finally {db.close();directory.deleteRecursively()}
    }
    @Test fun formatTwoSchemaSixBackupKeepsAllOriginalFieldsAndAddsSafeDefaults()=runBlocking {
        val instrumentation=InstrumentationRegistry.getInstrumentation();val context=instrumentation.targetContext
        val db=Room.inMemoryDatabaseBuilder(context,OpusDatabase::class.java).build()
        try {
            val raw=instrumentation.context.assets.open("backups/goldberg-0.9.1-format2.json").bufferedReader().use {it.readText()}
            BackupRepository(context,db).restore(raw)
            assertEquals(37,db.dao().allSessions().size);assertEquals(2220000L,db.dao().allSessions().sumOf {it.durationMillis})
            assertEquals("Emulator Upgrade",db.personalDao().getSettings()!!.username)
            assertEquals("",db.personalDao().getSettings()!!.favoriteArtist)
            assertEquals(123456789L,db.experienceDao().allUnlocks().first {it.achievementId=="first_notes"}.unlockedAt)
            assertTrue(db.practiceDao().archives().isEmpty())
        } finally {db.close()}
    }
}
