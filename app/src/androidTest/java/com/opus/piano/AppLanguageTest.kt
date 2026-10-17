package com.opus.piano

import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Dedicated test installation only; does not alter user content or the system language. */
@RunWith(AndroidJUnit4::class)
class AppLanguageTest {
    @Test fun languageOverrideUsesAppCompatAndCanReturnToSystem() {
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        val original=AppCompatDelegate.getApplicationLocales()
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            try {
                scenario.onActivity { AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("cs")) }
                val deadline=android.os.SystemClock.elapsedRealtime()+10000
                var ready=false
                while(!ready && android.os.SystemClock.elapsedRealtime()<deadline) {
                    instrumentation.waitForIdleSync()
                    scenario.onActivity {ready=it.resources.configuration.locales[0].language=="cs"}
                    if(!ready) Thread.sleep(100)
                }
                assertTrue("Locale recreation did not apply Czech resources",ready)
                scenario.onActivity { activity ->
                    assertEquals("cs",AppCompatDelegate.getApplicationLocales()[0]?.language)
                    assertEquals("2 skladby",activity.resources.getQuantityString(R.plurals.pieces_count,2,2))
                    assertEquals("com.opus.piano",activity.packageName)
                }
                scenario.onActivity { AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList()) }
                instrumentation.waitForIdleSync()
                scenario.onActivity { assertTrue(AppCompatDelegate.getApplicationLocales().isEmpty) }
            } finally { instrumentation.runOnMainSync { AppCompatDelegate.setApplicationLocales(original) } }
        }
    }
}
