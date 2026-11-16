package com.opus.piano

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.data.*
import com.opus.piano.data.catalogue.*
import com.opus.piano.data.catalogue.providers.ProviderClient
import com.opus.piano.data.catalogue.providers.ProviderTransport
import com.opus.piano.data.offline.*
import com.opus.piano.ui.LibraryState
import com.opus.piano.ui.OpusViewModel
import com.opus.piano.ui.RepertoireScreen
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID
import java.util.zip.GZIPInputStream
import java.util.concurrent.atomic.AtomicInteger

/** Exercises the actual packaged catalogue through repository search and both consuming screens. */
@RunWith(AndroidJUnit4::class)
class BundledCatalogueAcceptanceTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun id() = UUID.randomUUID().toString()

    @Test fun bundledComposersWorksLibraryAndDiscoverUsePublicRecordsOnly() = runBlocking {
        val personalName = "bundled-personal-${id()}.db"
        val catalogueName = "bundled-catalogue-${id()}.db"
        val temp = File(context.cacheDir, "bundled-catalogue-test-${id()}").apply { mkdirs() }
        val staged = File(temp, "catalogue.db")
        val personal = Room.databaseBuilder(context, OpusDatabase::class.java, personalName)
            .addMigrations(*OpusDatabase.ALL_MIGRATIONS)
            .addCallback(PersonalCatalogueBridge.callback)
            .addCallback(BackupState.callback)
            .build()
        val store = LocalCatalogueStore(context, personal, catalogueName)
        try {
            personal.openHelper.writableDatabase
            val privatePiece = personal.dao().insertCatalogPiece(Piece(
                title = "Private Chopin Ballade", composer = "Frédéric Chopin",
                difficulty = Difficulty.UNRATED, createdAt = 1L
            ))
            val privateIdentityKey = personal.personalCatalogueDao().anchors("piece", privatePiece.toString()).single().identityKey
            val privateIdentity = requireNotNull(personal.personalCatalogueDao().identity(privateIdentityKey))

            val manifest = context.assets.open("catalogue/musicbrainz-core-20261114/manifest.json")
                .bufferedReader().use { it.readText() }
            context.assets.open("catalogue/musicbrainz-core-20261114/catalogue.dbz").use { input ->
                GZIPInputStream(input, 1024 * 1024).buffered(1024 * 1024).use { compressed ->
                    staged.outputStream().buffered(1024 * 1024).use { compressed.copyTo(it, 1024 * 1024) }
                }
            }
            val installer = CatalogueBundleInstaller(
                BundleStore(File(temp, "bundles"), File(temp, "staging"), reserveBytes = { 0L }), store
            )
            val installStartedAt = CataloguePerformance.start()
            assertTrue(installer.importBundledPackage(manifest, staged) is CatalogueAccess.Available)
            CataloguePerformance.finish("benchmark_first_bundle_install", installStartedAt, "bytes=${staged.length()}")

            val preparationCalls = AtomicInteger()
            val discover = DiscoverRepository(
                store,
                ProviderClient(ProviderTransport { error("Offline acceptance must not request a provider") }, { false }),
                { false },
                prepareLocal = { preparationCalls.incrementAndGet() }
            )
            val firstSearchStartedAt = CataloguePerformance.start()
            val chopin = discover.search("chopin", SearchFilter("artists"), "en", online = false).first().page.items
            CataloguePerformance.finish("benchmark_first_local_search", firstSearchStartedAt, "result_count=${chopin.size}")
            assertTrue(chopin.any { it.title.contains("Chopin", ignoreCase = true) })
            for (name in listOf("Bach", "Mozart", "Beethoven")) {
                assertTrue("Bundled composer missing: $name", discover.search(name, SearchFilter("artists"), "en", false).first().page.items.any { it.title.contains(name, true) })
            }
            assertTrue(discover.search("frédéric", SearchFilter("artists"), "en", false).first().page.items.isNotEmpty())
            assertTrue(discover.search("chopi", SearchFilter("artists"), "en", false).first().page.items.isNotEmpty())
            val chopinId = chopin.first().id
            val works = discover.search("Ballade", SearchFilter("works", chopinId), "en", false).first().page.items
            assertTrue("Real Chopin work missing", works.any { it.kind == "work" })

            val libraryPage = discover.library("Chopin")
            assertTrue(libraryPage.items.any { it.id == chopinId })
            assertFalse(libraryPage.items.any { it.id == privateIdentity.canonicalId })
            val privateSearch = discover.search("Private Chopin", SearchFilter(), "en", false).first().page.items
            assertFalse(privateSearch.any { it.id == privateIdentity.canonicalId })
            assertTrue(discover.search("Catalogue phrase with no match", SearchFilter(), "en", false).first().page.items.isEmpty())
            val firstPage = discover.library("")
            assertEquals("catalogue pages stay bounded", 40, firstPage.items.size)
            val nextPage = discover.library("", "all", requireNotNull(firstPage.nextCursor))
            assertTrue(firstPage.items.map { it.id }.intersect(nextPage.items.map { it.id }.toSet()).isEmpty())
            assertEquals(firstPage.items.map { it.id }, discover.library("").items.map { it.id })
            assertEquals("local preparation must be reused across searches", 1, preparationCalls.get())
            assertEquals("Private Chopin Ballade", personal.dao().piece(privatePiece)?.title)
            assertEquals(privateIdentity, personal.personalCatalogueDao().identity(privateIdentityKey))

            val app = context.applicationContext as OpusApplication
            val model = OpusViewModel(app.container.repository, androidx.lifecycle.SavedStateHandle(), app.container.practice, app.container.recording, context)
            val personalPiece = requireNotNull(personal.dao().piece(privatePiece))
            compose.setContent {
                MaterialTheme {
                    RepertoireScreen(LibraryState(pieces=listOf(personalPiece),loading=false),CatalogSnapshot(emptyList(),emptyList()),model,{}, {},catalogueRepository=discover)
                }
            }
            compose.onNodeWithText(personalPiece.title).assertExists()
            compose.onNodeWithText(works.first().title).assertDoesNotExist()
            val tabs = compose.onAllNodes(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.Role, Role.Tab)).fetchSemanticsNodes()
            assertEquals("Library should expose its two tabs", 2, tabs.size)
            val repertoireTab = tabs[0].boundsInRoot
            val catalogueTab = tabs[1].boundsInRoot
            assertTrue("Library tabs should have equal widths", kotlin.math.abs(repertoireTab.width - catalogueTab.width) <= 1f)
            assertTrue("Library tabs should be adjacent", kotlin.math.abs(repertoireTab.right - catalogueTab.left) <= 1f)
            assertTrue("Library tabs should have a comfortable touch height", repertoireTab.height >= 64f * compose.activity.resources.displayMetrics.density)
            compose.onNodeWithText(context.getString(R.string.msg_catalogue_5a24)).performClick()
            compose.onNode(hasSetTextAction()).performTextInput("chopin")
            compose.waitUntil(30_000) {
                compose.onAllNodes(hasText(chopin.first().title, substring = true)).fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithText(personalPiece.title).assertDoesNotExist()
            compose.onNode(hasSetTextAction()).performTextClearance()
            compose.onNode(hasSetTextAction()).performTextInput(works.first().title)
            compose.waitUntil(30_000) {
                compose.onAllNodes(hasText(works.first().title, substring = true)).fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithText(personalPiece.title).assertDoesNotExist()
            compose.onNode(hasSetTextAction()).performTextClearance()
            compose.onNode(hasSetTextAction()).performTextInput("no matching public work 847291")
            compose.waitUntil(30_000) {
                compose.onAllNodes(hasText(context.getString(R.string.discover_empty))).fetchSemanticsNodes().isNotEmpty()
            }

            val initializationGate=CompletableDeferred<Unit>()
            val loadingRepository=DiscoverRepository(
                store,
                ProviderClient(ProviderTransport { error("Offline catalogue must not request a provider") }, { false }),
                { false },
                adapters=emptyList(),
                prepareLocal={ initializationGate.await() }
            )
            compose.setContent {
                MaterialTheme {
                    RepertoireScreen(LibraryState(pieces=listOf(personalPiece),loading=false),CatalogSnapshot(emptyList(),emptyList()),model,{}, {},catalogueRepository=loadingRepository)
                }
            }
            compose.onNodeWithText(context.getString(R.string.msg_catalogue_5a24)).performClick()
            compose.waitUntil(10_000) {
                compose.onAllNodes(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).fetchSemanticsNodes().isNotEmpty()
            }
            initializationGate.complete(Unit)
            compose.waitUntil(30_000) {
                compose.onAllNodes(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).fetchSemanticsNodes().isEmpty()
            }

            val failedInitialization = DiscoverRepository(
                store,
                ProviderClient(ProviderTransport { error("Offline catalogue must not request a provider") }, { false }),
                { false },
                adapters=emptyList(),
                prepareLocal={ error("fixture catalogue initialization failure") }
            )
            compose.setContent {
                MaterialTheme {
                    RepertoireScreen(LibraryState(pieces=listOf(personalPiece),loading=false),CatalogSnapshot(emptyList(),emptyList()),model,{}, {},catalogueRepository=failedInitialization)
                }
            }
            compose.onNodeWithText(context.getString(R.string.msg_catalogue_5a24)).performClick()
            compose.waitUntil(30_000) {
                compose.onAllNodes(hasText(context.getString(R.string.discover_error))).fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithText(context.getString(R.string.discover_retry)).assertExists()
            compose.onNodeWithText(context.getString(R.string.discover_retry)).performClick()
            compose.waitUntil(30_000) {
                compose.onAllNodes(hasText(context.getString(R.string.discover_error))).fetchSemanticsNodes().isNotEmpty()
            }
        } finally {
            compose.setContent {}
            store.close(); personal.close()
            staged.delete(); temp.deleteRecursively()
            context.deleteDatabase(personalName)
            context.getDatabasePath(catalogueName).parentFile?.listFiles()
                ?.filter { it.name == catalogueName || it.name.startsWith("$catalogueName.") || it.name.startsWith("$catalogueName-") }
                ?.forEach { it.delete() }
        }
    }
}
