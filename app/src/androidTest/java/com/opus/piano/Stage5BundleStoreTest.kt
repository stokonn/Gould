package com.opus.piano

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.data.offline.BundleManifest
import com.opus.piano.data.offline.BundleState
import com.opus.piano.data.offline.BundleStore
import com.opus.piano.data.OpusDatabase
import com.opus.piano.data.PersonalCatalogueBridge
import com.opus.piano.data.Piece
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

/**
 * Stage 5 store I/O on a real device filesystem (real `AtomicFile`): atomic publish,
 * interrupted-refresh safety, quarantine, mark-and-sweep with shared media, concurrent
 * removal, admission refusal and personal-journal isolation.
 */
@RunWith(AndroidJUnit4::class)
class Stage5BundleStoreTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val roots = mutableListOf<File>()

    private class Fixture(val store: BundleStore, val root: File) {
        val bundlesDir get() = File(root, "bundles")
        val stagingDir get() = File(root, "staging")
        fun blob(sha: String) = File(File(bundlesDir, "blobs"), sha)
        fun manifestsDir() = File(bundlesDir, "manifests")
        fun quarantineDir() = File(bundlesDir, "quarantine")
    }

    private fun fixture(
        reserve: () -> Long = { 0L },
        maxTotal: Long = 256L * 1024 * 1024
    ): Fixture {
        val root = File(context.cacheDir, "stage5-store-${UUID.randomUUID()}").apply { mkdirs() }
        roots += root
        val store = BundleStore(
            File(root, "bundles"), File(root, "staging"),
            reserveBytes = reserve, otherUsedBytes = { 0L }, maxTotalBytes = maxTotal
        )
        return Fixture(store, root)
    }

    @After
    fun cleanup() {
        roots.forEach { it.deleteRecursively() }
    }

    private suspend fun Fixture.seedReady(manifest: BundleManifest, contents: List<ByteArray>) {
        store.save(manifest)
        contents.forEach { content ->
            val staged = File(stagingDir, "${Stage5Fixtures.shaOf(content)}.part")
            staged.writeBytes(content)
            assertTrue(store.installBlob(staged, Stage5Fixtures.shaOf(content), content.size.toLong()))
        }
    }

    // ------------------------------------------------------------ publish safety

    @Test
    fun interruptedRefreshKeepsPreviousReadyRevisionUntilAtomicPromote() = runBlocking {
        val f = fixture()
        val id = UUID.randomUUID().toString()
        val contentA = "revision one bytes".toByteArray()
        val contentB = "revision two bytes, longer".toByteArray()
        val fileA = Stage5Fixtures.bundleFile(contentA)
        val fileB = Stage5Fixtures.bundleFile(contentB, relativePath = "media/fixture2.jpg")

        f.seedReady(Stage5Fixtures.manifest(id, 1, listOf(fileA), state = BundleState.READY), listOf(contentA))
        assertTrue(f.blob(fileA.sha256).isFile)

        // Interrupted refresh: rev2 persisted and partially staged, never promoted.
        f.store.save(Stage5Fixtures.manifest(id, 2, listOf(fileB)))
        val partial = File(f.stagingDir, "${fileB.sha256}.part")
        partial.writeBytes(contentB.copyOf(4))
        val sweep = f.store.sweep()

        val published = f.store.readPublished().manifests.single()
        assertEquals("old READY revision survives the interrupted refresh", 1L, published.revision)
        assertEquals(BundleState.READY, published.state)
        assertTrue("old content survives", f.blob(fileA.sha256).isFile)
        assertTrue("referenced partial staging is retained", partial.isFile)
        assertEquals("nothing unreferenced was removed", 0, sweep.removedBlobs)

        // Completing the refresh publishes by a single atomic rename.
        f.store.save(Stage5Fixtures.manifest(id, 2, listOf(fileB), state = BundleState.READY))
        val promoted = f.store.promote(id)
        assertEquals(2L, promoted.revision)
        val nowPublished = f.store.readPublished().manifests.single()
        assertEquals(BundleState.READY, nowPublished.state)
        assertEquals(2L, nowPublished.revision)
        assertTrue(f.blob(fileB.sha256).isFile)
        assertTrue("old revision content is reclaimed only after promotion", !f.blob(fileA.sha256).exists())
        assertTrue(f.store.readPending().manifests.isEmpty())
    }

    @Test
    fun storeStateSurvivesReopenLikeAProcessRestart() = runBlocking {
        val f = fixture()
        val content = "durable manifest bytes".toByteArray()
        val file = Stage5Fixtures.bundleFile(content)
        val id = UUID.randomUUID().toString()
        f.seedReady(Stage5Fixtures.manifest(id, 3, listOf(file), state = BundleState.READY), listOf(content))

        val reopened = BundleStore(f.bundlesDir, f.stagingDir, reserveBytes = { 0L })
        val restored = reopened.readPublished().manifests.single()
        assertEquals(id, restored.bundleId)
        assertEquals(BundleState.READY, restored.state)
        assertEquals(3L, restored.revision)
        assertEquals("Open Opus contributors", restored.attribution)
        assertNotNull("validation timestamp is durable metadata", restored.validatedAt)
        assertTrue(reopened.blobFile(file.sha256).isFile)
        assertEquals("bytes survive reopen exactly", content.size, reopened.blobFile(file.sha256).length())
    }

    // ------------------------------------------------------------ corruption and sweep

    @Test
    fun corruptManifestIsQuarantinedWithBytesPreservedAndOthersSurvive() = runBlocking {
        val f = fixture()
        val content = "healthy manifest content".toByteArray()
        val file = Stage5Fixtures.bundleFile(content)
        f.seedReady(Stage5Fixtures.manifest(files = listOf(file), state = BundleState.READY), listOf(content))

        val corrupt = File(f.manifestsDir(), "00000000-0000-4000-8000-000000000000.json")
        corrupt.writeText("{this is not a manifest")

        val read = f.store.readPublished()
        assertEquals(1, read.manifests.size)
        assertEquals(listOf(corrupt.name), read.quarantined)
        assertFalse("corrupt file leaves the live directory", corrupt.exists())
        val quarantined = f.quarantineDir().listFiles()!!.single()
        assertTrue("quarantine preserves bytes", quarantined.readText().contains("not a manifest"))
        // Healthy data is untouched and the second read is stable.
        assertEquals(1, f.store.readPublished().manifests.size)
        assertTrue(f.blob(file.sha256).isFile)
    }

    @Test
    fun sweepReclaimsOnlyUnreferencedContent() = runBlocking {
        val f = fixture()
        val contentA = "bundle content a".toByteArray()
        val contentB = "bundle content b".toByteArray()
        val fileA = Stage5Fixtures.bundleFile(contentA)
        val fileB = Stage5Fixtures.bundleFile(contentB, relativePath = "media/fixture2.jpg")
        val id = UUID.randomUUID().toString()
        // rev1 READY with A; rev2 pending (in-flight refresh) also wants B.
        f.seedReady(Stage5Fixtures.manifest(id, 1, listOf(fileA), state = BundleState.READY), listOf(contentA))
        f.store.save(Stage5Fixtures.manifest(id, 2, listOf(fileB)))
        val partialB = File(f.stagingDir, "${fileB.sha256}.part")
        partialB.writeBytes(contentB.copyOf(3))
        // An orphan staged file nobody references must be reclaimed.
        val orphanSha = Stage5Fixtures.shaOf("orphan".toByteArray())
        val orphan = File(f.stagingDir, "$orphanSha.part")
        orphan.writeBytes("orphan".toByteArray())
        // A stray non-content file in the blob directory is left alone unless it is a valid
        // unreferenced sha; "notasha" is not valid content addressing, so it is not touched.
        val stray = File(f.bundlesDir, "blobs/notasha")
        stray.writeText("stray")

        val report = f.store.sweep()
        assertEquals("orphan staging reclaimed", 1, report.removedStaged)
        assertTrue(!orphan.exists())
        assertTrue("referenced partial kept", partialB.isFile)
        assertTrue(f.blob(fileA.sha256).isFile)
        assertTrue("non-content-addressed stray untouched", stray.isFile)
        assertEquals(2, report.retainedReferenced)
    }

    @Test
    fun quarantineBlobPreservesBytesAndRemovesFromBlobStore() = runBlocking {
        val f = fixture()
        val content = "corrupt asset bytes".toByteArray()
        val sha = Stage5Fixtures.shaOf(content)
        val staged = File(f.stagingDir, "$sha.part")
        staged.writeBytes(content)
        assertTrue(f.store.installBlob(staged, sha, content.size.toLong()))
        assertTrue(f.blob(sha).isFile)

        assertTrue(f.store.quarantineBlob(sha, "sha256_mismatch"))
        assertFalse(f.blob(sha).exists())
        val quarantined = f.quarantineDir().listFiles()!!.single()
        assertTrue(quarantined.name.startsWith(sha))
        assertTrue(quarantined.name.contains("sha256_mismatch"))
        assertTrue("bytes preserved, never silently deleted", quarantined.readBytes().contentEquals(content))
        assertFalse("second quarantine of missing blob is a no-op", f.store.quarantineBlob(sha, "again"))
    }

    // ------------------------------------------------------------ shared media + concurrency

    @Test
    fun sharedBlobSurvivesUntilTheLastReferencingBundleIsRemoved() = runBlocking {
        val f = fixture()
        val shared = "shared dependency bytes".toByteArray()
        val ownA = "bundle a own bytes".toByteArray()
        val ownB = "bundle b own bytes".toByteArray()
        val sharedFile = Stage5Fixtures.bundleFile(shared)
        val fileA = Stage5Fixtures.bundleFile(ownA, relativePath = "media/a.jpg")
        val fileB = Stage5Fixtures.bundleFile(ownB, relativePath = "media/b.jpg")
        val idA = UUID.randomUUID().toString()
        val idB = UUID.randomUUID().toString()
        f.seedReady(Stage5Fixtures.manifest(idA, 1, listOf(sharedFile, fileA), state = BundleState.READY), listOf(shared, ownA))
        f.seedReady(Stage5Fixtures.manifest(idB, 1, listOf(sharedFile, fileB), state = BundleState.READY), listOf(ownB))

        assertEquals("content addressing stores shared bytes once", 3, File(f.bundlesDir, "blobs").listFiles()!!.size)

        f.store.remove(idA)
        assertTrue("shared media retained for the surviving bundle", f.blob(sharedFile.sha256).isFile)
        assertFalse("unreferenced own media reclaimed", f.blob(fileA.sha256).isFile)
        assertTrue(f.blob(fileB.sha256).isFile)

        f.store.remove(idB)
        assertFalse("shared media reclaimed with the last reference", f.blob(sharedFile.sha256).isFile)
    }

    @Test
    fun concurrentRemovalsAndClearNeverCorruptTheStoreOrDeleteSharedMediaEarly() = runBlocking {
        val f = fixture()
        val shared = "concurrently shared bytes".toByteArray()
        val sharedFile = Stage5Fixtures.bundleFile(shared)
        val idC = UUID.randomUUID().toString()
        val idD = UUID.randomUUID().toString()
        f.seedReady(Stage5Fixtures.manifest(idC, 1, listOf(sharedFile), state = BundleState.READY), listOf(shared))
        f.store.save(Stage5Fixtures.manifest(idD, 1, listOf(sharedFile), state = BundleState.READY))

        kotlinx.coroutines.coroutineScope {
            launch(Dispatchers.IO) { f.store.remove(idC) }
            launch(Dispatchers.IO) { f.store.remove(idD) }
        }
        assertFalse(f.blob(sharedFile.sha256).exists())
        assertTrue(f.store.readAll().manifests.isEmpty())

        // Concurrent refresh + clear: no exception and no corruption; the follow-up clear
        // makes the outcome deterministic regardless of which side won the race.
        val idE = UUID.randomUUID().toString()
        kotlinx.coroutines.coroutineScope {
            launch(Dispatchers.IO) { runCatching { f.store.save(Stage5Fixtures.manifest(idE, 1, listOf(sharedFile))) } }
            launch(Dispatchers.IO) { f.store.removeAll() }
        }
        f.store.removeAll()
        val final = f.store.sweep()
        assertTrue(f.store.readAll().manifests.isEmpty())
        assertEquals(0, final.retainedReferenced)
        assertFalse(f.blob(sharedFile.sha256).exists())
    }

    // ------------------------------------------------------------ budgets and personal data

    @Test
    fun admissionRefusesInsteadOfEvictingPinnedContent() {
        val strict = fixture(reserve = { context.cacheDir.usableSpace })
        assertFalse("protected reserve must never be consumed", strict.store.admits(1024))
        assertTrue("zero-byte admission is always possible", strict.store.admits(0))
        val capped = fixture(maxTotal = 4096)
        assertTrue(capped.store.admits(4096))
        assertFalse("combined ceiling is a hard cap", capped.store.admits(4097))
    }

    @Test
    fun removingAllBundlesLeavesPersonalJournalDataIntact() = runBlocking {
        val journal = Room.inMemoryDatabaseBuilder(context, OpusDatabase::class.java)
            .addCallback(PersonalCatalogueBridge.callback).build()
        val f = fixture()
        try {
            journal.dao().save(Piece(id = 77, title = "Private work", composer = "Private author", notes = "Unchanged notes"))
            val content = "personal-adjacent bundle bytes".toByteArray()
            val file = Stage5Fixtures.bundleFile(content)
            f.seedReady(Stage5Fixtures.manifest(files = listOf(file), state = BundleState.READY), listOf(content))

            f.store.removeAll()

            assertTrue(f.store.readAll().manifests.isEmpty())
            assertTrue(f.blob(file.sha256).exists().not())
            assertEquals("Unchanged notes", journal.dao().piece(77)!!.notes)
        } finally {
            journal.close()
        }
    }
}
