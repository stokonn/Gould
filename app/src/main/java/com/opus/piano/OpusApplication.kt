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
            runCatching { BackupRepository(this@OpusApplication, database).replayPendingPreferences() }
            portablePreferences.restoreDesiredLocale()
            com.opus.piano.i18n.L.refresh()
            runCatching { com.opus.piano.reminders.ReminderScheduler(this@OpusApplication).schedule() }
        }
    }
    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        com.opus.piano.i18n.L.refresh()
    }
    val portablePreferences by lazy { com.opus.piano.data.backup.PortablePreferences(this,live=true) }
    val identity by lazy { com.opus.piano.identity.GoogleIdentity(this, database) }
    val media by lazy { com.opus.piano.preferences.MediaPreferences(this) }
    val quotes by lazy { com.opus.piano.data.DailyQuotes(this) }
    val appearance by lazy { com.opus.piano.preferences.AppearanceStore(this) }
    val practiceRequest = kotlinx.coroutines.flow.MutableStateFlow("")
    val openPractice = kotlinx.coroutines.flow.MutableStateFlow(0L)
    val database by lazy {
        Room.databaseBuilder(this, OpusDatabase::class.java, "opus.db")
            .addMigrations(*OpusDatabase.ALL_MIGRATIONS).addCallback(PersonalCatalogueBridge.callback).addCallback(BackupState.callback).build()
    }
    // Optional: never referenced by journal/recovery startup or existing legacy Library paths.
    val localCatalogue by lazy { com.opus.piano.data.catalogue.LocalCatalogueStore(this, database, intake=catalogueStorage.intake) }
    val catalogueStorage by lazy { com.opus.piano.data.catalogue.CatalogueStorage(this) }
    val providerCache by lazy { com.opus.piano.data.catalogue.providers.FileProviderCache(java.io.File(cacheDir,"catalogue-stage4/responses"),reserveBytes={catalogueStorage.requiredReserve()},intake=catalogueStorage.intake) }
    val discover by lazy {
        com.opus.piano.data.catalogue.DiscoverRepository(localCatalogue,
            com.opus.piano.data.catalogue.providers.ProviderClient(com.opus.piano.data.catalogue.providers.HttpsProviderTransport { media.allowed() }, { media.allowed() }, cache=providerCache), { media.allowed() }, catalogueStorage)
    }
    val cataloguePortraits by lazy { com.opus.piano.data.catalogue.CataloguePortraits(java.io.File(cacheDir,"catalogue-stage4/portraits"),reserveBytes={catalogueStorage.requiredReserve()},intake=catalogueStorage.intake) { media.allowed() } }
    val discoverHistory by lazy { com.opus.piano.data.catalogue.DiscoverHistory(this) }
    val catalogueRepertoire by lazy { com.opus.piano.data.catalogue.CatalogueRepertoire(database) }
    val practiceRepository by lazy { PracticeRepository(database, repository) }
    val practice by lazy { com.opus.piano.practice.PracticeCoordinator(practiceRepository) }
    val achievements by lazy { AchievementStore(database) }
    val recordings by lazy { RecordingRepository(database) }
    val repository by lazy { OpusRepository(database, achievements) }
    val catalogRepository by lazy { CatalogRepository(database, filesDir, achievements = achievements, mediaAllowed = { media.allowed() }) }
}
