package com.opus.piano

import android.app.Application
import androidx.room.Room
import kotlinx.coroutines.launch
import com.opus.piano.data.*

class OpusApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        com.opus.piano.i18n.L.initialize(this)
        com.opus.piano.widgets.WidgetRefresh.observe(this)
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            runCatching { BackupRepository(this@OpusApplication, database).replayPendingPreferences() }
            portablePreferences.restoreDesiredLocale()
            com.opus.piano.i18n.L.refresh()
            runCatching { com.opus.piano.reminders.ReminderScheduler(this@OpusApplication).schedule() }
            // Stage 5: restore durable bundle state after process death (D5.1/D5.3/D7).
            // File + WorkManager metadata only; never touches the journal or recovery data.
            runCatching { bundleScheduler.reconcileAfterRestart() }
            // Recording: metadata reconciliation only; never capture or automatic publication.
            runCatching { recording.reconcile() }
        }
    }
    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        com.opus.piano.i18n.L.refresh()
        com.opus.piano.widgets.WidgetRefresh.request(this)
    }
    // Stage 6: no endpoint, fixture, startup job or Google-profile authorization.
    val community by lazy {
        val root=java.io.File(noBackupFilesDir,"community-v1")
        com.opus.piano.data.community.LocalCommunityRepository(
            com.opus.piano.data.community.CommunityDraftStore(java.io.File(root,"drafts")),
            com.opus.piano.data.community.AndroidCommunityCredentials(this),
            cache=com.opus.piano.data.community.CommunityOverlayCache(java.io.File(root,"public-overlay.json")))
    }
    val portablePreferences by lazy { com.opus.piano.data.backup.PortablePreferences(this,live=true) }
    val identity by lazy { com.opus.piano.identity.GoogleIdentity(this, database) }
    val media by lazy { com.opus.piano.preferences.MediaPreferences(this) }
    // Stage 7A: persisted online-catalogue toggle (default OFF); online search stays explicit.
    val catalogOnline by lazy { com.opus.piano.preferences.CatalogPreferences(this) }
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
            com.opus.piano.data.catalogue.providers.ProviderClient(com.opus.piano.data.catalogue.providers.HttpsProviderTransport { catalogOnline.enabled.value && media.allowed() }, { catalogOnline.enabled.value && media.allowed() }, cache=providerCache), { catalogOnline.enabled.value && media.allowed() }, catalogueStorage,
            prepareLocal={legacyCatalogueImport.synchronize();Unit})
    }
    val legacyCatalogueImport by lazy { com.opus.piano.data.catalogue.LegacyCatalogueImport(database,localCatalogue,catalogueStorage) }
    val cataloguePortraits by lazy { com.opus.piano.data.catalogue.CataloguePortraits(java.io.File(cacheDir,"catalogue-stage4/portraits"),reserveBytes={catalogueStorage.requiredReserve()},intake=catalogueStorage.intake) { media.allowed() } }
    // Stage 5 offline bundles (D5.1 file store, D5.3 WorkManager, D7 budgets/consent).
    // Lazy on purpose: nothing in journal/recovery/startup paths ever touches these.
    val bundleStore by lazy { com.opus.piano.data.offline.BundleStore(java.io.File(filesDir,"offline-bundles"),java.io.File(cacheDir,"offline-bundles/staging"),reserveBytes={catalogueStorage.requiredReserve()},otherUsedBytes={catalogueStorage.usage().let { it.databaseBytes + it.mediaBytes }}) }
    val bundleJobs by lazy { com.opus.piano.data.offline.BundleJobStore(com.opus.piano.data.offline.AndroidBundleJobPreferences(this)) }
    // Stage 5 instrumentation seams: hermetic network/source in tests, never set in production.
    @androidx.annotation.VisibleForTesting var bundleNetworkOverride: com.opus.piano.data.offline.BundleNetwork? = null
    @androidx.annotation.VisibleForTesting var bundleConnectOverride: ((java.net.URL) -> java.net.HttpURLConnection)? = null
    private val androidBundleNetwork by lazy { com.opus.piano.data.offline.AndroidBundleNetwork(this) }
    val bundleNetwork by lazy { com.opus.piano.data.offline.BundleNetwork { (bundleNetworkOverride ?: androidBundleNetwork).facts() } }
    val bundleScheduler by lazy { com.opus.piano.data.offline.BundleScheduler(androidx.work.WorkManager.getInstance(this),bundleJobs,bundleStore,bundleNetwork) }
    val bundleRunner by lazy { com.opus.piano.data.offline.BundleJobRunner(bundleStore,bundleJobs,bundleNetwork,java.io.File(cacheDir,"offline-bundles/staging"),connect={ url -> bundleConnectOverride?.invoke(url) ?: (url.openConnection() as java.net.HttpURLConnection) }) }
    val discoverHistory by lazy { com.opus.piano.data.catalogue.DiscoverHistory(this) }
    val catalogueRepertoire by lazy { com.opus.piano.data.catalogue.CatalogueRepertoire(database) }
    val practiceRepository by lazy { PracticeRepository(database, repository) }
    private val practiceDelegate = lazy {
        com.opus.piano.practice.PracticeCoordinator(practiceRepository).also {
            val coordinator = it
            suspend fun audioMetadata(block: suspend () -> Unit) {
                try { block() }
                catch (c: kotlinx.coroutines.CancellationException) { throw c }
                catch (_: Exception) {
                    recording.interrupt("AssociationStorage")
                    coordinator.notice.value = com.opus.piano.i18n.L.s(com.opus.piano.R.string.msg_recording_interrupted)
                }
            }
            it.finishRecording = { recording.stop() }
            it.selectionChanged = { part -> audioMetadata { recording.associate(part) } }
            it.sessionStarted = { token -> audioMetadata { recording.linkSession(token) } }
            com.opus.piano.widgets.WidgetRefresh.watchPractice(this, it)
        }
    }
    val practice by practiceDelegate
    fun currentPracticeForWidgets(): com.opus.piano.practice.PracticeCoordinator? = if (practiceDelegate.isInitialized()) practice else null
    val localPlayback by lazy { com.opus.piano.audio.LocalPlaybackController(this, LocalRecordingRepository(database, java.io.File(filesDir,"recordings"))) {
        recording.state.value.phase !in setOf(com.opus.piano.audio.RecordingController.Phase.Preparing,com.opus.piano.audio.RecordingController.Phase.Recording) &&
            !(practice.state.value.running && practice.state.value.smart) && !com.opus.piano.service.SmartPracticeService.captureActive.value
    } }
    val metronome by lazy { com.opus.piano.audio.MetronomeController(this) }
    val recording by lazy { com.opus.piano.audio.RecordingController(LocalRecordingRepository(database, java.io.File(filesDir,"recordings"))) }
    val achievements by lazy { AchievementStore(database) }
    val recordings by lazy { RecordingRepository(database) }
    val repository by lazy { OpusRepository(database, achievements) }
    val catalogRepository by lazy { CatalogRepository(database, filesDir, achievements = achievements, mediaAllowed = { media.allowed() }) }

    // Canonical Stage 6/8 service path. The accepted legacy UI stays disabled
    // until its decimal-revision/dual-confirmation model is adopted explicitly.
    @androidx.annotation.VisibleForTesting var canonicalAdminTransportOverride: com.opus.piano.admin.AdminTransport? = null
    private val canonicalAdminGuard = Any()
    @Volatile private var canonicalAdminInstance: com.opus.piano.data.community.canonical.CanonicalAdminRepository? = null
    suspend fun canonicalAdminRepository(): com.opus.piano.data.community.canonical.CanonicalAdminRepository {
        synchronized(canonicalAdminGuard) { canonicalAdminInstance }?.let { return it }
        val created = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val contract = com.opus.piano.data.community.canonical.AdminContract(
            assets.open("community/admin-canonical-schema.json").bufferedReader().use { it.readText() }
        )
        com.opus.piano.data.community.canonical.CanonicalAdminRepository(
            com.opus.piano.data.community.canonical.CanonicalAdminClient(
                contract,
                com.opus.piano.data.community.canonical.CanonicalHttpTransport(
                    endpoint = { adminSettings.endpoint },
                    transport = canonicalAdminTransportOverride ?: com.opus.piano.admin.DisabledAdminTransport,
                ),
            )
        )
        }
        return synchronized(canonicalAdminGuard) {
            canonicalAdminInstance ?: created.also { canonicalAdminInstance = it }
        }
    }

    // Stage 7B: Administration foundation. LAZY — nothing in startup/journal/recovery
    // paths ever touches these. adminTransportOverride mirrors bundleConnectOverride:
    // hermetic seam for tests/previews, never set in production.
    @androidx.annotation.VisibleForTesting var adminTransportOverride: com.opus.piano.admin.AdminTransport? = null
    val adminSettings by lazy { com.opus.piano.admin.AdminSettingsStore(this) }
    val adminSession by lazy { com.opus.piano.admin.KeystoreAdminSessionStore(this) }
    val adminRepository: com.opus.piano.admin.AdminRepository by lazy {
        val api = com.opus.piano.admin.AdminApi.forApplication(
            baseUrl = { adminSettings.endpoint },
            fixtureTransportOverride = adminTransportOverride,
            token = { adminSession.readToken() },
        )
        com.opus.piano.admin.AdminRepository(
            api = api,
            session = adminSession,
            trust = com.opus.piano.admin.AdminTrustPolicy(),
            markers = com.opus.piano.admin.AdminMarkers(com.opus.piano.admin.AndroidAdminMarkerStore(this)) {
                com.opus.piano.admin.adminFingerprint("${identity.account.value.orEmpty()}:${adminSettings.endpoint}:${adminRepository.principalId.orEmpty()}")
            },
            stats = com.opus.piano.admin.AdminStatsService(api),
            // New local Stage 6 candidate requires dual revision expectations;
            // no frozen HTTP mapping exists yet. Never send legacy C6 live decisions.
            decisionsEnabled = false,
        )
    }
}
