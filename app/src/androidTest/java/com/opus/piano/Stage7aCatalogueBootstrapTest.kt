package com.opus.piano

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Stage 7A emulator-evidence helper: populates the legacy catalogue through the app's OWN
 * repository code — [com.opus.piano.data.CatalogRepository.featured] and
 * [com.opus.piano.data.CatalogRepository.composerWorks], exactly the calls
 * `CatalogScreen.startBrowsing()` and `ComposerScreen.loadComposer()` make — so the redesigned
 * Artist Profile and Work Profile screens can be inspected with genuine OpenOpus data on a
 * fresh install.
 *
 * Why a helper is needed: on a fresh install no production entry reaches the legacy `catalog`
 * navigation route (documented Stage 7A investigation finding — the route lost its production
 * entry during the Stage 4 Discover redesign; Stage 7A intentionally performs no navigation
 * cleanup or restoration). Every Artist/Work Profile entry point (Library Artists chip,
 * catalogue work cards, Discover offline rows) reads the opus.db catalogue tables that only
 * this repository populates, so those screens cannot render real content until the catalogue
 * is bootstrapped.
 *
 * Data integrity: nothing is invented or hand-written. The helper performs the same explicit
 * HTTPS fetches the product performs and writes only through the product's own DAO layer; the
 * fetched OpenOpus rows carry their source attribution in `catalog_cache`. With no network the
 * test skips via a JUnit assumption — it never fabricates catalogue rows.
 *
 * Safety: the helper mutates the real app database with live catalogue data, so it refuses to
 * run outside an emulator (verified via Build hardware/fingerprint) and must never be executed
 * against a device holding owner data. Runs in the app process (instrumentation target), so the
 * populated opus.db is the real app database on the disposable emulator.
 */
@RunWith(AndroidJUnit4::class)
class Stage7aCatalogueBootstrapTest {
    @Test fun appOwnCatalogueBootstrapFeedsEditorialScreens() = runBlocking {
        val hardware = Build.HARDWARE.lowercase()
        val fingerprint = Build.FINGERPRINT.lowercase()
        assumeTrue(
            "Disposable-emulator verification helper — refusing to run outside an emulator " +
                "(hardware=$hardware)",
            hardware in setOf("ranchu", "goldfish") ||
                fingerprint.contains("emulator") || fingerprint.contains("sdk_gphone") || fingerprint.contains("vbox86")
        )
        val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as OpusApplication
        // The app's own featured() bootstrap — the same explicit OpenOpus fetch
        // CatalogScreen.startBrowsing() performs. Offline: skip, never fabricate.
        val featured = runCatching { app.catalogRepository.featured() }
        assumeTrue("OpenOpus unreachable — bootstrap skipped, no catalogue data is fabricated", featured.isSuccess)
        val composers = withTimeout(15_000) { app.database.catalogDao().composers().first { it.isNotEmpty() } }
        // One composer's full works list — the same call ComposerScreen.loadComposer() makes —
        // so catalogue work cards, Essential works and Discover offline rows render real metadata.
        app.catalogRepository.composerWorks(composers.first().id)
        val works = withTimeout(15_000) { app.database.catalogDao().works().first { it.isNotEmpty() } }
        assertTrue(composers.isNotEmpty())
        assertTrue(works.isNotEmpty())
        println("Stage7a bootstrap: ${composers.size} composers, ${works.size} works of '${composers.first().name}'")
    }
}
