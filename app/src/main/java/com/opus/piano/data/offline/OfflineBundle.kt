package com.opus.piano.data.offline

/**
 * Stage 5 offline bundle model.
 *
 * Manifests describe explicitly pinned encyclopedia/media content and live entirely in
 * app-private storage. They never reference journal rows, never store personal practice
 * data and never participate in portable backup. Removing every bundle or cache entry
 * therefore cannot affect the personal journal, recovery drafts or repertoire.
 *
 * Storage classes (see docs/planning/goldberg-0.11/OFFLINE-CACHE.md):
 *  - critical personal data  -> Room journal (untouched by this package)
 *  - regenerable cache       -> FileProviderCache / CataloguePortraits
 *  - explicitly pinned bundle-> BundleStore content store (this package)
 *  - staging                 -> cacheDir partial downloads (disposable)
 */

/** Durable lifecycle states for a bundle. A partial bundle is never [READY]. */
enum class BundleState {
    QUEUED,
    WAITING_FOR_POLICY,
    DOWNLOADING,
    VALIDATING,
    INSTALLING,
    READY,
    PAUSED,
    FAILED_RETRYABLE,
    STALE,
    QUARANTINED;

    val isTerminalReady: Boolean get() = this == READY
    val canHoldContent: Boolean get() = this == READY
}

/** Bounded dependency scopes. Full discographies/list graphs are intentionally absent. */
enum class BundleType { WORK, ARTIST, ALBUM, COLLECTION }

/** Per-item rights retained so offline rendering can show complete attribution. */
data class BundleRights(
    val license: String,
    val licenseUrl: String?,
    val attribution: String,
    val sourceName: String,
    val sourceUrl: String?
)

/**
 * One content-addressed file inside a bundle.
 *
 * [sha256] is both the integrity digest and the storage address, which makes shared
 * dependency accounting and mark-and-sweep deletion exact: two bundles referencing the
 * same bytes share one physical file.
 *
 * [relativePath] is the logical position used for rendering/attribution only. It is
 * validated against path traversal before it is ever accepted.
 */
data class BundleFile(
    val sha256: String,
    val bytes: Long,
    val mime: String,
    val relativePath: String,
    val language: String?,
    val rights: BundleRights
)

/** Persisted progress so an interrupted transfer resumes/restarts without losing state. */
data class BundleProgress(
    val completedBytes: Long = 0,
    val totalBytes: Long = 0,
    val completedFiles: Int = 0
) {
    init {
        require(completedBytes >= 0 && totalBytes >= 0 && completedFiles >= 0)
        require(totalBytes == 0L || completedBytes <= totalBytes)
    }
}

/**
 * Durable bundle manifest.
 *
 * Published manifests are the only roots that keep content files alive. A refresh builds
 * a *new* manifest revision in pending storage and publishes it atomically only after
 * validation, so the previously [BundleState.READY] revision survives an interrupted
 * refresh.
 */
data class BundleManifest(
    val bundleId: String,
    val type: BundleType,
    val entityId: String,
    val schemaVersion: Int,
    val revision: Long,
    val language: String,
    val createdAt: Long,
    val validatedAt: Long?,
    val dependencies: List<String>,
    val expectedBytes: Long,
    val files: List<BundleFile>,
    val providerKind: String?,
    val providerRevision: String?,
    val attribution: String,
    val minReaderVersion: Int,
    val state: BundleState,
    val progress: BundleProgress,
    val lastError: String?,
    val updatedAt: Long
) {
    init {
        require(bundleId.matches(ID)) { "bundleId must be a UUID" }
        require(entityId.isNotBlank()) { "entityId required" }
        require(revision >= 1) { "revision must be positive" }
        require(schemaVersion >= 1) { "schemaVersion must be positive" }
        require(minReaderVersion >= 1) { "minReaderVersion must be positive" }
        require(expectedBytes >= 0) { "expectedBytes must not be negative" }
        require(dependencies.size <= MAX_DEPENDENCIES) { "Bounded dependency expansion exceeded" }
        require(dependencies.none { it == bundleId }) { "A bundle cannot depend on itself" }
        require(dependencies.distinct().size == dependencies.size) { "Duplicate dependency" }
        require(language.isNotBlank()) { "language required" }
        require(files.size <= MAX_FILES) { "Bounded file count exceeded" }
        require(files.distinctBy { it.sha256 }.size == files.size) { "Duplicate content reference" }
        require(attribution.isNotBlank()) { "Offline attribution is mandatory" }
        // Freshness is separate from usability: a ready/paused/stale bundle still shows saved time.
        require(state == BundleState.READY || validatedAt == null || state == BundleState.STALE || state == BundleState.QUARANTINED) {
            "Only ready/stale/quarantined bundles may carry a validation timestamp"
        }
    }

    val expectedUncompressedBytes: Long get() = files.sumOf { it.bytes }

    fun readyRevisionOf(existing: BundleManifest?): Boolean = existing?.let { state == BundleState.READY && it.bundleId == bundleId } ?: false

    companion object {
        val ID = Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
        const val READER_VERSION = 1
        const val MAX_FILES = 512
        const val MAX_DEPENDENCIES = 64
        const val MAX_BYTES = 256L * 1024 * 1024
    }
}
