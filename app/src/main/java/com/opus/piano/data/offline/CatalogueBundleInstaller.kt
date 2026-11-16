package com.opus.piano.data.offline

import com.opus.piano.data.catalogue.CatalogueAccess
import com.opus.piano.data.catalogue.LocalCatalogueStore
import java.io.File

/** Installs only an already-published, READY schema-1 catalogue database bundle. */
class CatalogueBundleInstaller(
    private val bundles: BundleStore,
    private val catalogue: LocalCatalogueStore
) {
    suspend fun isBundledRevisionInstalled(manifestJson: String): Boolean {
        val manifest = BundleManifestCodec.decode(manifestJson)
        val result = catalogue.access { it.state("musicbrainz_core_sha256") }
        return (result as? CatalogueAccess.Available)?.value == manifest.files.singleOrNull()?.sha256
    }

    /** Installs the APK-bundled Core DB directly, avoiding a duplicate copy in BundleStore. */
    suspend fun importBundledPackage(manifestJson: String, databaseFile: File): CatalogueAccess<Unit> {
        val manifest = BundleManifestCodec.decode(manifestJson)
        if (manifest.state != BundleState.READY || manifest.type != BundleType.COLLECTION || manifest.schemaVersion != 1 ||
            manifest.files.size != 1 || manifest.expectedBytes != manifest.files.single().bytes) {
            return CatalogueAccess.Rejected("unsupported_catalogue_bundle")
        }
        val file = manifest.files.single()
        if (file.relativePath != "catalogue.db" || file.mime != "application/vnd.sqlite3" ||
            file.rights.license != "CC0-1.0" || file.rights.licenseUrl != "https://creativecommons.org/publicdomain/zero/1.0/" ||
            file.rights.sourceName != "MusicBrainz" || file.rights.attribution.isBlank() ||
            file.rights.sourceUrl?.startsWith("https://") != true || !databaseFile.isFile || databaseFile.length() != file.bytes) {
            return CatalogueAccess.Rejected("unsupported_catalogue_file_or_rights")
        }
        val installed = catalogue.installGeneratedDatabase(databaseFile, file.sha256, file.bytes)
        if (installed is CatalogueAccess.Available) {
            val marked = catalogue.access { it.state("musicbrainz_core_sha256", file.sha256) }
            if (marked !is CatalogueAccess.Available) return marked
        }
        return installed
    }

    /** Imports the generator's existing manifest + SQLite artifact, then installs it locally. */
    suspend fun importGeneratedPackage(manifestFile: File, databaseFile: File): CatalogueAccess<Unit> {
        require(manifestFile.isFile && manifestFile.length() <= BundleManifestCodec.MAX_ENCODED_BYTES)
        val manifest = BundleManifestCodec.decode(manifestFile.readText(Charsets.UTF_8))
        if (manifest.state != BundleState.READY || manifest.type != BundleType.COLLECTION || manifest.schemaVersion != 1 ||
            manifest.files.size != 1 || manifest.expectedBytes != manifest.files.single().bytes) {
            return CatalogueAccess.Rejected("unsupported_catalogue_bundle")
        }
        val file = manifest.files.single()
        if (file.relativePath != "catalogue.db" || file.mime != "application/vnd.sqlite3" ||
            !databaseFile.isFile || databaseFile.length() != file.bytes) return CatalogueAccess.Rejected("unsupported_catalogue_file")
        if (file.rights.license != "CC0-1.0" ||
            file.rights.licenseUrl != "https://creativecommons.org/publicdomain/zero/1.0/" ||
            file.rights.sourceName != "MusicBrainz" || file.rights.attribution.isBlank() ||
            file.rights.sourceUrl?.startsWith("https://") != true) {
            return CatalogueAccess.Rejected("unsupported_catalogue_rights")
        }
        val current = bundles.readPublished().manifests.singleOrNull { it.bundleId == manifest.bundleId }
        if (current != null && manifest.revision <= current.revision && manifest != current) {
            return CatalogueAccess.Rejected("catalogue_bundle_revision_conflict")
        }
        val staged = File.createTempFile("catalogue-import-", ".part", databaseFile.absoluteFile.parentFile)
        try {
            databaseFile.inputStream().buffered(1024 * 1024).use { input ->
                staged.outputStream().buffered(1024 * 1024).use { output -> input.copyTo(output, 1024 * 1024) }
            }
            if (!bundles.installBlob(staged, file.sha256, file.bytes)) return CatalogueAccess.Rejected("catalogue_blob_invalid")
        } finally { staged.delete() }
        val installed = catalogue.installGeneratedDatabase(bundles.blobFile(file.sha256), file.sha256, file.bytes)
        if (installed !is CatalogueAccess.Available) return installed
        bundles.save(manifest)
        if (current != null && manifest.revision > current.revision) bundles.promote(manifest.bundleId)
        return installed
    }

    suspend fun install(bundleId: String, revision: Long): CatalogueAccess<Unit> {
        val manifest = bundles.readPublished().manifests.singleOrNull {
            it.bundleId == bundleId && it.revision == revision && it.state == BundleState.READY
        } ?: return CatalogueAccess.Rejected("catalogue_bundle_not_ready")
        if (manifest.type != BundleType.COLLECTION || manifest.schemaVersion != 1 || manifest.files.size != 1 ||
            manifest.expectedBytes != manifest.files.single().bytes) {
            return CatalogueAccess.Rejected("unsupported_catalogue_bundle")
        }
        val file = manifest.files.single()
        if (file.relativePath != "catalogue.db" || file.mime != "application/vnd.sqlite3") {
            return CatalogueAccess.Rejected("unsupported_catalogue_file")
        }
        val blob = bundles.blobFile(file.sha256)
        if (!blob.isFile || blob.length() != file.bytes) return CatalogueAccess.Rejected("catalogue_blob_missing")
        return catalogue.installGeneratedDatabase(blob, file.sha256, file.bytes)
    }
}
