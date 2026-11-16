package com.opus.piano

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.data.BackupState
import com.opus.piano.data.OpusDatabase
import com.opus.piano.data.PersonalCatalogueBridge
import com.opus.piano.data.Piece
import com.opus.piano.data.Difficulty
import com.opus.piano.data.catalogue.CatalogueAccess
import com.opus.piano.data.catalogue.CatalogueAssertion
import com.opus.piano.data.catalogue.CatalogueDatabase
import com.opus.piano.data.catalogue.CatalogueEntity
import com.opus.piano.data.catalogue.CatalogueRepository
import com.opus.piano.data.catalogue.CatalogueRights
import com.opus.piano.data.catalogue.CatalogueSource
import com.opus.piano.data.catalogue.CatalogueWork
import com.opus.piano.data.catalogue.LocalCatalogueStore
import com.opus.piano.data.offline.BundleFile
import com.opus.piano.data.offline.BundleManifest
import com.opus.piano.data.offline.BundleProgress
import com.opus.piano.data.offline.BundleRights
import com.opus.piano.data.offline.BundleState
import com.opus.piano.data.offline.BundleStore
import com.opus.piano.data.offline.BundleType
import com.opus.piano.data.offline.CatalogueBundleInstaller
import com.opus.piano.data.offline.BundleManifestCodec
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

/** Disposable fixture databases only; this exercises import boundaries, not real catalogue data. */
@RunWith(AndroidJUnit4::class)
class CatalogueBundleInstallerTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private fun id() = UUID.randomUUID().toString()

    @Test fun packagedMusicBrainzManifestDecodesAndPinsCoreProvenance() {
        val raw = context.assets.open("catalogue/musicbrainz-core-20261114/manifest.json").bufferedReader().use { it.readText() }
        val manifest = BundleManifestCodec.decode(raw)
        assertEquals(BundleState.READY, manifest.state)
        assertEquals("20261114-002230", manifest.providerRevision)
        assertEquals("CC0-1.0", manifest.files.single().rights.license)
        assertEquals("MusicBrainz", manifest.files.single().rights.sourceName)
        assertTrue(manifest.expectedBytes in 200_000_000L..400_000_000L)
        assertEquals(manifest.expectedBytes, manifest.files.single().bytes)
    }

    @Test fun packagedCataloguePayloadRetainsGzipBytesForStreamingInstall() {
        context.assets.open("catalogue/musicbrainz-core-20261114/catalogue.dbz").use { input ->
            assertEquals(0x1f, input.read())
            assertEquals(0x8b, input.read())
        }
    }

    @Test fun verifiedPackageSwitchPreservesJournalIdentityAndRejectsTamperedBytes() = runBlocking {
        val suffix = id()
        val personalName = "stage2-personal-$suffix"
        val catalogueName = "stage2-catalogue-$suffix"
        val generatedName = "stage2-generated-$suffix"
        val tempRoot = File(context.cacheDir, "stage2-catalogue-import-$suffix").apply { mkdirs() }
        val personal = Room.databaseBuilder(context, OpusDatabase::class.java, personalName)
            .addMigrations(*OpusDatabase.ALL_MIGRATIONS)
            .addCallback(PersonalCatalogueBridge.callback)
            .addCallback(BackupState.callback)
            .build()
        val local = LocalCatalogueStore(context, personal, catalogueName)
        var generated: CatalogueDatabase? = null
        try {
            personal.openHelper.writableDatabase
            val pieceId = personal.dao().insertCatalogPiece(
                Piece(title = "Private fixture title", composer = "Private creator", difficulty = Difficulty.UNRATED, createdAt = 1L)
            )
            val anchorBefore = personal.personalCatalogueDao().anchors("piece", pieceId.toString()).single()
            val identityBefore = requireNotNull(personal.personalCatalogueDao().identity(anchorBefore.identityKey))
            assertTrue(local.initialize() is CatalogueAccess.Available)

            generated = CatalogueDatabase.builder(context, generatedName).build()
            val repository = CatalogueRepository(generated)
            val entityId = id()
            val sourceId = id()
            val rightsId = id()
            repository.add(CatalogueEntity(entityId, "work", "Synthetic authored work"), CatalogueWork(entityId))
            repository.addSource(CatalogueSource(sourceId, "musicbrainz", "00000000-0000-4000-8000-000000000001", "fixture-r1", "a".repeat(64), "2026-11-14T00:00:00Z", "https://musicbrainz.org/work/00000000-0000-4000-8000-000000000001"))
            repository.addRights(CatalogueRights(rightsId, "CC0-1.0", "https://creativecommons.org/publicdomain/zero/1.0/", "MusicBrainz Core test fixture", "approved"))
            repository.addAssertion(CatalogueAssertion(id(), entityId, "title", "\"Synthetic authored work\"", sourceId, rightsId, true))
            generated.close()
            generated = null

            val dbFile = context.getDatabasePath(generatedName)
            val bytes = dbFile.length()
            val digest = sha256(dbFile)
            val bundleId = id()
            val now = 1_794_614_400_000L
            val manifest = BundleManifest(
                bundleId = bundleId, type = BundleType.COLLECTION, entityId = id(), schemaVersion = 1,
                revision = 1, language = "und", createdAt = now, validatedAt = now, dependencies = emptyList(),
                expectedBytes = bytes,
                files = listOf(BundleFile(digest, bytes, "application/vnd.sqlite3", "catalogue.db", null,
                    BundleRights("CC0-1.0", "https://creativecommons.org/publicdomain/zero/1.0/", "MusicBrainz Core test fixture", "MusicBrainz", "https://musicbrainz.org/"))),
                providerKind = "catalogue", providerRevision = "musicbrainz-core-test-r1", attribution = "MusicBrainz Core test fixture",
                minReaderVersion = 1, state = BundleState.READY, progress = BundleProgress(bytes, bytes, 1),
                lastError = null, updatedAt = now
            )
            val manifestFile = File(tempRoot, "manifest.json").apply { writeText(BundleManifestCodec.encode(manifest)) }
            val bundleStore = BundleStore(File(tempRoot, "bundles"), File(tempRoot, "staging"), reserveBytes = { 0L })
            val installer = CatalogueBundleInstaller(bundleStore, local)

            assertTrue(installer.importGeneratedPackage(manifestFile, dbFile) is CatalogueAccess.Available)
            val embeddedManifest = BundleManifestCodec.encode(manifest)
            assertTrue(!installer.isBundledRevisionInstalled(embeddedManifest))
            assertTrue(installer.importBundledPackage(embeddedManifest, dbFile) is CatalogueAccess.Available)
            assertTrue(installer.isBundledRevisionInstalled(embeddedManifest))
            assertEquals(entityId, local.access { it.entity(entityId)?.id }.let { (it as CatalogueAccess.Available).value })
            assertEquals(identityBefore.canonicalId, local.access { it.entity(identityBefore.canonicalId)?.id }.let { (it as CatalogueAccess.Available).value })
            assertEquals(identityBefore, personal.personalCatalogueDao().identity(anchorBefore.identityKey))
            assertEquals(anchorBefore, personal.personalCatalogueDao().anchors("piece", pieceId.toString()).single())
            assertEquals("Private fixture title", personal.dao().piece(pieceId)?.title)

            val fixtureProviderDb = File(tempRoot, "fixture-provider.db").apply { dbFile.copyTo(this) }
            android.database.sqlite.SQLiteDatabase.openDatabase(fixtureProviderDb.path, null,
                android.database.sqlite.SQLiteDatabase.OPEN_READWRITE).use { db -> db.execSQL("UPDATE sources SET provider='fixture'") }
            val fixtureProviderManifest = manifest.copy(
                bundleId = id(), entityId = id(), expectedBytes = fixtureProviderDb.length(),
                files = listOf(manifest.files.single().copy(sha256 = sha256(fixtureProviderDb), bytes = fixtureProviderDb.length())),
                progress = BundleProgress(fixtureProviderDb.length(), fixtureProviderDb.length(), 1)
            )
            val fixtureProviderManifestFile = File(tempRoot, "fixture-provider-manifest.json").apply {
                writeText(BundleManifestCodec.encode(fixtureProviderManifest))
            }
            assertTrue(installer.importGeneratedPackage(fixtureProviderManifestFile, fixtureProviderDb) !is CatalogueAccess.Available)
            assertTrue(bundleStore.readPublished().manifests.none { it.bundleId == fixtureProviderManifest.bundleId })

            val supplementaryDb = File(tempRoot, "supplementary.db").apply { dbFile.copyTo(this) }
            android.database.sqlite.SQLiteDatabase.openDatabase(supplementaryDb.path, null,
                android.database.sqlite.SQLiteDatabase.OPEN_READWRITE).use { db ->
                db.execSQL("INSERT INTO assertions(id,entityId,field,valueJson,sourceId,rightsId,selected) SELECT ?,id,'tags','[\"synthetic\"]',(SELECT id FROM sources LIMIT 1),(SELECT id FROM rights LIMIT 1),0 FROM entities LIMIT 1", arrayOf(id()))
            }
            val supplementaryManifest = manifest.copy(
                bundleId = id(), entityId = id(), expectedBytes = supplementaryDb.length(),
                files = listOf(manifest.files.single().copy(sha256 = sha256(supplementaryDb), bytes = supplementaryDb.length())),
                progress = BundleProgress(supplementaryDb.length(), supplementaryDb.length(), 1)
            )
            val supplementaryManifestFile = File(tempRoot, "supplementary-manifest.json").apply {
                writeText(BundleManifestCodec.encode(supplementaryManifest))
            }
            assertTrue(installer.importGeneratedPackage(supplementaryManifestFile, supplementaryDb) !is CatalogueAccess.Available)
            assertTrue(bundleStore.readPublished().manifests.none { it.bundleId == supplementaryManifest.bundleId })
            assertEquals(1L, bundleStore.readPublished().manifests.single().revision)

            val wrongRightsManifest = manifest.copy(
                bundleId = id(), entityId = id(),
                files = listOf(manifest.files.single().copy(rights = manifest.files.single().rights.copy(license = "CC-BY-NC-SA-3.0")))
            )
            val wrongRightsManifestFile = File(tempRoot, "wrong-rights-manifest.json").apply {
                writeText(BundleManifestCodec.encode(wrongRightsManifest))
            }
            assertTrue(installer.importGeneratedPackage(wrongRightsManifestFile, dbFile) is CatalogueAccess.Rejected)
            assertTrue(bundleStore.readPublished().manifests.none { it.bundleId == wrongRightsManifest.bundleId })
            assertEquals(identityBefore, personal.personalCatalogueDao().identity(anchorBefore.identityKey))

            val wrongSizeManifest = manifest.copy(bundleId = id(), entityId = id(), expectedBytes = bytes + 1)
            val wrongSizeManifestFile = File(tempRoot, "wrong-size-manifest.json").apply {
                writeText(BundleManifestCodec.encode(wrongSizeManifest))
            }
            assertTrue(installer.importGeneratedPackage(wrongSizeManifestFile, dbFile) is CatalogueAccess.Rejected)
            assertTrue(bundleStore.readPublished().manifests.none { it.bundleId == wrongSizeManifest.bundleId })

            val revisionTwo = manifest.copy(revision = 2, providerRevision = "fixture-r2", createdAt = now + 1,
                validatedAt = now + 1, updatedAt = now + 1)
            val revisionTwoFile = File(tempRoot, "manifest-r2.json").apply {
                writeText(BundleManifestCodec.encode(revisionTwo))
            }
            assertTrue(installer.importGeneratedPackage(revisionTwoFile, dbFile) is CatalogueAccess.Available)
            assertEquals(2L, bundleStore.readPublished().manifests.single().revision)

            val tampered = File(tempRoot, "tampered.db").apply {
                writeBytes(dbFile.readBytes().also { it[it.lastIndex] = (it.last().toInt() xor 1).toByte() })
            }
            assertTrue(installer.importGeneratedPackage(revisionTwoFile, tampered) is CatalogueAccess.Rejected)
            assertEquals(entityId, local.access { it.entity(entityId)?.id }.let { (it as CatalogueAccess.Available).value })
            assertEquals(identityBefore, personal.personalCatalogueDao().identity(anchorBefore.identityKey))

            val invalidBytes = "not a SQLite database".toByteArray()
            val invalidDatabase = File(tempRoot, "invalid.db").apply { writeBytes(invalidBytes) }
            val invalidManifest = manifest.copy(
                bundleId = id(), entityId = id(), expectedBytes = invalidBytes.size.toLong(),
                files = listOf(manifest.files.single().copy(sha256 = sha256(invalidBytes), bytes = invalidBytes.size.toLong())),
                progress = BundleProgress(invalidBytes.size.toLong(), invalidBytes.size.toLong(), 1)
            )
            val invalidManifestFile = File(tempRoot, "invalid-manifest.json").apply {
                writeText(BundleManifestCodec.encode(invalidManifest))
            }
            assertTrue(installer.importGeneratedPackage(invalidManifestFile, invalidDatabase) !is CatalogueAccess.Available)
            assertTrue(bundleStore.readPublished().manifests.none { it.bundleId == invalidManifest.bundleId })
            assertEquals(entityId, local.access { it.entity(entityId)?.id }.let { (it as CatalogueAccess.Available).value })
            assertEquals(identityBefore, personal.personalCatalogueDao().identity(anchorBefore.identityKey))
        } finally {
            generated?.close()
            local.close()
            personal.close()
            context.deleteDatabase(generatedName)
            context.deleteDatabase(catalogueName)
            context.deleteDatabase(personalName)
            tempRoot.deleteRecursively()
        }
    }

    private fun sha256(file: File): String = java.security.MessageDigest.getInstance("SHA-256")
        .digest(file.readBytes()).joinToString("") { "%02x".format(it) }

    private fun sha256(bytes: ByteArray): String = java.security.MessageDigest.getInstance("SHA-256")
        .digest(bytes).joinToString("") { "%02x".format(it) }
}
