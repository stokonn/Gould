package com.opus.piano

import android.app.Application
import androidx.room.Room
import kotlinx.coroutines.launch
import com.opus.piano.data.*

class OpusApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        com.opus.piano.i18n.L.initialize(this)
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            com.opus.piano.i18n.L.refresh()
            runCatching { com.opus.piano.reminders.ReminderScheduler(this@OpusApplication).schedule() }
        }
    }
    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        com.opus.piano.i18n.L.refresh()
    }
    val identity by lazy { com.opus.piano.identity.GoogleIdentity(this, database) }
    val media by lazy { com.opus.piano.preferences.MediaPreferences(this) }
    val quotes by lazy { com.opus.piano.data.DailyQuotes(this) }
    val appearance by lazy { com.opus.piano.preferences.AppearanceStore(this) }
    val practiceRequest = kotlinx.coroutines.flow.MutableStateFlow("")
    val openPractice = kotlinx.coroutines.flow.MutableStateFlow(0L)
    val database by lazy {
        Room.databaseBuilder(this, OpusDatabase::class.java, "opus.db")
            .addMigrations(OpusDatabase.MIGRATION_1_2, OpusDatabase.MIGRATION_2_3, OpusDatabase.MIGRATION_3_4, OpusDatabase.MIGRATION_4_5, OpusDatabase.MIGRATION_5_6, OpusDatabase.MIGRATION_6_7).build()
    }
    val practiceRepository by lazy { PracticeRepository(database, repository) }
    val practice by lazy { com.opus.piano.practice.PracticeCoordinator(practiceRepository) }
    val achievements by lazy { AchievementStore(database) }
    val recordings by lazy { RecordingRepository(database) }
    val repository by lazy { OpusRepository(database, achievements) }
    val catalogRepository by lazy { CatalogRepository(database, filesDir, achievements = achievements, mediaAllowed = { media.allowed() }) }
}
