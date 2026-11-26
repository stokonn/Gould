package com.opus.piano

import android.app.Application
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import com.opus.piano.data.*

class OpusApplication : Application(), ContainerOwner {
    override val container by lazy { AppContainer(this) }
    override fun onCreate() {
        super.onCreate()
        // Stop any legacy periodic admin sync restored by WorkManager on upgrade.
        runCatching {
            androidx.work.WorkManager.getInstance(this).cancelUniqueWork("goldberg-admin-queue-check")
            getSystemService(android.app.NotificationManager::class.java).cancel(701)
        }
        com.opus.piano.i18n.L.initialize(this)
        com.opus.piano.widgets.WidgetRefresh.observe(this, container)
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            runCatching { BackupRepository(this@OpusApplication, database).replayPendingPreferences() }
            // Guarded consistently with every other startup step: a preference or
            // resources failure must never crash the process during onCreate.
            runCatching { portablePreferences.restoreDesiredLocale() }
            runCatching { com.opus.piano.i18n.L.refresh() }
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
    val identity get() = container.identity
    val media get() = container.media
    // Stage 7A: persisted online-catalogue toggle (default OFF); online search stays explicit.
    val catalogOnline by lazy { com.opus.piano.preferences.CatalogPreferences(this) }
    val quotes by lazy { com.opus.piano.data.DailyQuotes(this) }
    val appearance get() = container.appearance
    val practiceRequest get() = container.practiceRequest
    val openPractice get() = container.openPractice
    val database get() = container.database
    // Optional: never referenced by journal/recovery startup or existing legacy Library paths.
    val localCatalogue by lazy { com.opus.piano.data.catalogue.LocalCatalogueStore(this, database, intake=catalogueStorage.intake, storage=catalogueStorage) }
    val catalogueStorage by lazy { com.opus.piano.data.catalogue.CatalogueStorage(this) }
    private val catalogueBundleInstaller by lazy { com.opus.piano.data.offline.CatalogueBundleInstaller(bundleStore, localCatalogue) }
    private val bundledCatalogueLock = kotlinx.coroutines.sync.Mutex()
    private suspend fun installBundledCatalogue() = bundledCatalogueLock.withLock {
        val manifest = com.opus.piano.data.catalogue.CataloguePerformance.measure("bundle_discovery") {
            assets.open("catalogue/musicbrainz-core-20261114/manifest.json").bufferedReader().use { it.readText() }
        }
        val installed = com.opus.piano.data.catalogue.CataloguePerformance.measureSuspend("bundle_revision_check") {
            catalogueBundleInstaller.isBundledRevisionInstalled(manifest)
        }
        if (installed) return@withLock
        val expectedBytes = com.opus.piano.data.offline.BundleManifestCodec.decode(manifest).expectedBytes
        check(catalogueStorage.admitsInstall(expectedBytes, copies=2)) { "insufficient free space to install the bundled MusicBrainz catalogue safely" }
        val staged = java.io.File(cacheDir, "musicbrainz-core-catalogue.db")
        try {
            com.opus.piano.data.catalogue.CataloguePerformance.measure("bundle_decompress") {
                assets.open("catalogue/musicbrainz-core-20261114/catalogue.dbz").buffered(1024 * 1024).use { compressed ->
                    java.util.zip.GZIPInputStream(compressed, 1024 * 1024).buffered(1024 * 1024).use { input ->
                        staged.outputStream().buffered(1024 * 1024).use { output -> input.copyTo(output, 1024 * 1024) }
                    }
                }
            }
            val result = com.opus.piano.data.catalogue.CataloguePerformance.measureSuspend("bundle_install") {
                catalogueBundleInstaller.importBundledPackage(manifest, staged)
            }
            check(result is com.opus.piano.data.catalogue.CatalogueAccess.Available) { "Bundled MusicBrainz catalogue could not be installed: $result" }
        } finally { staged.delete() }
    }
    val providerCache by lazy { com.opus.piano.data.catalogue.providers.FileProviderCache(java.io.File(cacheDir,"catalogue-stage4/responses"),reserveBytes={catalogueStorage.requiredReserve()},intake=catalogueStorage.intake) }
    val discover by lazy {
        com.opus.piano.data.catalogue.DiscoverRepository(localCatalogue,
            com.opus.piano.data.catalogue.providers.ProviderClient(com.opus.piano.data.catalogue.providers.HttpsProviderTransport { catalogOnline.enabled.value && media.allowed() }, { catalogOnline.enabled.value && media.allowed() }, cache=providerCache), { catalogOnline.enabled.value && media.allowed() }, catalogueStorage,
            prepareLocal={
                com.opus.piano.data.catalogue.CataloguePerformance.measureSuspend("bundle_install_or_reuse") { installBundledCatalogue() }
                com.opus.piano.data.catalogue.CataloguePerformance.measureSuspend("legacy_catalogue_sync") { legacyCatalogueImport.synchronize() }
                Unit
            })
    }
    val legacyCatalogueImport by lazy { com.opus.piano.data.catalogue.LegacyCatalogueImport(database,localCatalogue,catalogueStorage) }
    val cataloguePortraits by lazy { com.opus.piano.data.catalogue.CataloguePortraits(java.io.File(cacheDir,"catalogue-stage4/portraits"),reserveBytes={catalogueStorage.requiredReserve()},intake=catalogueStorage.intake) { media.allowed() } }
    // Stage 5 offline bundles (D5.1 file store, D5.3 WorkManager, D7 budgets/consent).
    // Lazy on purpose: nothing in journal/recovery/startup paths ever touches these.
    // Downloaded bundles keep their own 256 MiB ceiling (D7). The pinned offline
    // catalogue has a separate 8 GiB budget and is not evictable, so counting its
    // database bytes against the bundle ceiling would permanently refuse every
    // download once the bundled catalogue exceeds 256 MiB (as it does since 0.15).
    // Only disposable catalogue media shares the bundle ceiling here.
    val bundleStore by lazy { com.opus.piano.data.offline.BundleStore(java.io.File(filesDir,"offline-bundles"),java.io.File(cacheDir,"offline-bundles/staging"),reserveBytes={catalogueStorage.requiredReserve()},otherUsedBytes={catalogueStorage.usage().mediaBytes}) }
    val bundleJobs by lazy { com.opus.piano.data.offline.BundleJobStore(com.opus.piano.data.offline.AndroidBundleJobPreferences(this)) }
    private val androidBundleNetwork by lazy { com.opus.piano.data.offline.AndroidBundleNetwork(this) }
    val bundleNetwork by lazy { com.opus.piano.data.offline.BundleNetwork { (container.testHooks.bundleNetwork ?: androidBundleNetwork).facts() } }
    val bundleScheduler by lazy { com.opus.piano.data.offline.BundleScheduler(androidx.work.WorkManager.getInstance(this),bundleJobs,bundleStore,bundleNetwork) }
    val bundleRunner by lazy { com.opus.piano.data.offline.BundleJobRunner(bundleStore,bundleJobs,bundleNetwork,java.io.File(cacheDir,"offline-bundles/staging"),connect={ url -> container.testHooks.bundleConnect?.invoke(url) ?: (url.openConnection() as java.net.HttpURLConnection) }) }
    val discoverHistory by lazy { com.opus.piano.data.catalogue.DiscoverHistory(this) }
    val catalogueRepertoire by lazy { com.opus.piano.data.catalogue.CatalogueRepertoire(database) }
    val practiceRepository get() = container.practiceRepository
    val practice get() = container.practice
    fun currentPracticeForWidgets() = container.currentPracticeForWidgets()
    val localPlayback by lazy { com.opus.piano.audio.LocalPlaybackController(this, LocalRecordingRepository(database, java.io.File(filesDir,"recordings"))) {
        recording.state.value.phase !in setOf(com.opus.piano.audio.RecordingController.Phase.Preparing,com.opus.piano.audio.RecordingController.Phase.Recording) &&
            !(practice.state.value.running && practice.state.value.smart) && !com.opus.piano.service.SmartPracticeService.captureActive.value
    } }
    val metronome by lazy { com.opus.piano.audio.MetronomeController(this) }
    val recording get() = container.recording
    val achievements get() = container.achievements
    val activityRepository get() = container.activityRepository
    val recordings get() = container.recordings
    val repository get() = container.repository
    val catalogRepository get() = container.catalogRepository
}
