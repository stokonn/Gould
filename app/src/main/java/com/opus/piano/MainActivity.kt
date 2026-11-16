package com.opus.piano

import android.os.Bundle
import androidx.compose.ui.graphics.luminance
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import com.opus.piano.ui.*

class MainActivity : androidx.appcompat.app.AppCompatActivity() {
    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra("practice",false)) {
            application.appContainer.openPractice.value = System.nanoTime()
            intent.removeExtra("practice")
        }
    }
    override fun onResume() {
        super.onResume()
        com.opus.piano.widgets.WidgetRefresh.request(this)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application.appContainer
        com.opus.piano.i18n.L.refresh()
        if (savedInstanceState == null && intent.getBooleanExtra("practice",false))
            app.openPractice.value = System.nanoTime()
        intent.removeExtra("practice")
        enableEdgeToEdge()
        setContent {
            val model: OpusViewModel = viewModel(factory = viewModelFactory {
                initializer { OpusViewModel(app.repository, createSavedStateHandle(), app.practice, app.recording, application) }
            })
            val catalog: CatalogViewModel = viewModel(factory = viewModelFactory {
                initializer { CatalogViewModel(app.catalogRepository, createSavedStateHandle()) }
            })
            val experience: ExperienceViewModel = viewModel(factory = viewModelFactory {
                initializer { ExperienceViewModel(app.achievements, app.recordings) }
            })
            val profile: ProfileViewModel = viewModel(factory = viewModelFactory {
                initializer { ProfileViewModel(application, createSavedStateHandle()) }
            })
            val activity: ActivityViewModel = viewModel(factory = viewModelFactory {
                initializer { ActivityViewModel(app.activityRepository) }
            })
            val settings by profile.settings.collectAsStateWithLifecycle()
            val appearance by app.appearance.state.collectAsStateWithLifecycle()
            OpusTheme(settings.theme, settings.typography, appearance) {
                val light = androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() > .5f
                androidx.compose.runtime.SideEffect {
                    androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = light; isAppearanceLightNavigationBars = light
                    }
                }
                OpusApp(model, catalog, experience, profile, activity)
            }
        }
    }
}
