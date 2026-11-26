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

    @Test fun activityProgressPluralStringsRespectLocaleAndGoalQuantity() {
        val base=InstrumentationRegistry.getInstrumentation().targetContext
        fun resources(tag:String)=base.createConfigurationContext(
            android.content.res.Configuration(base.resources.configuration).apply {
                setLocales(android.os.LocaleList.forLanguageTags(tag))
            }).resources
        val en=resources("en")
        val cs=resources("cs")
        assertEquals("2/5 sessions",en.getQuantityString(R.plurals.activity_sessions_progress,5,2,5))
        assertEquals("Complete 1 session this week to continue your streak.",
            en.getQuantityString(R.plurals.activity_goal_waiting,1,1))
        assertEquals("2/5 cvičení",cs.getQuantityString(R.plurals.activity_sessions_progress,5,2,5))
        assertEquals("2/5 minut",cs.getQuantityString(R.plurals.activity_minutes_progress,5,2,5))
    }
}
