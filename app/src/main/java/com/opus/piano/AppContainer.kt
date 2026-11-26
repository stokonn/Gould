package com.opus.piano

import androidx.room.Room
import android.content.Context
import com.opus.piano.data.ActivityRepository
import com.opus.piano.data.OpusDatabase
import com.opus.piano.data.AchievementStore
import com.opus.piano.data.OpusRepository
import com.opus.piano.data.PracticeRepository
import com.opus.piano.data.CatalogRepository
import com.opus.piano.data.RecordingRepository
import com.opus.piano.identity.GoogleIdentity
import com.opus.piano.preferences.AppearanceStore
import com.opus.piano.preferences.MediaPreferences

/** Lazy application service graph for core persisted state and repositories. */
internal class AppTestHooks {
    var bundleNetwork: com.opus.piano.data.offline.BundleNetwork? = null
    var bundleConnect: ((java.net.URL) -> java.net.HttpURLConnection)? = null
}

class AppContainer internal constructor(private val app: OpusApplication, internal val testHooks: AppTestHooks = AppTestHooks()) {
    val context: Context get() = app
    val practiceRequest = kotlinx.coroutines.flow.MutableStateFlow("")
    val openPractice = kotlinx.coroutines.flow.MutableStateFlow(0L)
    val database by lazy {
        Room.databaseBuilder(app, OpusDatabase::class.java, "opus.db")
            .addMigrations(*OpusDatabase.ALL_MIGRATIONS)
            .addCallback(com.opus.piano.data.PersonalCatalogueBridge.callback)
            .addCallback(com.opus.piano.data.BackupState.callback)
            .build()
    }
    val identity by lazy { GoogleIdentity(app, database) }
    val media by lazy { MediaPreferences(app) }
    val appearance by lazy { AppearanceStore(app) }
    val achievements by lazy { AchievementStore(database) }
    val activityRepository by lazy { ActivityRepository(database) }
    val repository by lazy { OpusRepository(database, achievements) }
    val recordings by lazy { RecordingRepository(database) }
    val catalogRepository by lazy { CatalogRepository(database, app.filesDir, achievements = achievements, mediaAllowed = { media.allowed() }) }
    val practiceRepository by lazy { PracticeRepository(database, repository) }
    val recording by lazy { com.opus.piano.audio.RecordingController(com.opus.piano.data.LocalRecordingRepository(database, java.io.File(app.filesDir, "recordings"))) }
    private val practiceDelegate = lazy {
        com.opus.piano.practice.PracticeCoordinator(practiceRepository).also { coordinator ->
            suspend fun audioMetadata(block: suspend () -> Unit) {
                try { block() }
                catch (c: kotlinx.coroutines.CancellationException) { throw c }
                catch (_: Exception) {
                    recording.interrupt("AssociationStorage")
                    coordinator.notice.value = com.opus.piano.i18n.L.s(com.opus.piano.R.string.msg_recording_interrupted)
                }
            }
            coordinator.finishRecording = { recording.stop() }
            coordinator.selectionChanged = { part -> audioMetadata { recording.associate(part) } }
            coordinator.sessionStarted = { token -> audioMetadata { recording.linkSession(token) } }
            com.opus.piano.widgets.WidgetRefresh.watchPractice(this@AppContainer, coordinator)
        }
    }
    val practice by practiceDelegate
    fun currentPracticeForWidgets(): com.opus.piano.practice.PracticeCoordinator? = if (practiceDelegate.isInitialized()) practice else null

    // Transitional adapters keep older services lazy while callers move onto this graph.
    val community get() = app.community
    val portablePreferences get() = app.portablePreferences
    val catalogOnline get() = app.catalogOnline
    val quotes get() = app.quotes
    val localCatalogue get() = app.localCatalogue
    val catalogueStorage get() = app.catalogueStorage
    val providerCache get() = app.providerCache
    val discover get() = app.discover
    val legacyCatalogueImport get() = app.legacyCatalogueImport
    val cataloguePortraits get() = app.cataloguePortraits
    val bundleStore get() = app.bundleStore
    val bundleJobs get() = app.bundleJobs
    val bundleNetwork get() = app.bundleNetwork
    val bundleScheduler get() = app.bundleScheduler
    val bundleRunner get() = app.bundleRunner
    val discoverHistory get() = app.discoverHistory
    val catalogueRepertoire get() = app.catalogueRepertoire
    val localPlayback get() = app.localPlayback
    val metronome get() = app.metronome
}

/** Android component boundary: depend on the graph contract instead of the concrete Application. */
interface ContainerOwner { val container: AppContainer }

val Context.appContainer: AppContainer
    get() = (applicationContext as ContainerOwner).container
