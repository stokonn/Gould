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
            (application as OpusApplication).openPractice.value = System.nanoTime()
            intent.removeExtra("practice")
        }
    }
    override fun onResume() {
        super.onResume()
        com.opus.piano.widgets.WidgetRefresh.request(this)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.opus.piano.i18n.L.refresh()
        if (savedInstanceState == null && intent.getBooleanExtra("practice",false))
            (application as OpusApplication).openPractice.value = System.nanoTime()
        intent.removeExtra("practice")
        enableEdgeToEdge()
        setContent {
            val model: OpusViewModel = viewModel(factory = viewModelFactory {
                initializer { OpusViewModel((application as OpusApplication).repository, createSavedStateHandle(), (application as OpusApplication).practice, (application as OpusApplication).recording, application) }
            })
            val catalog: CatalogViewModel = viewModel(factory = viewModelFactory {
                initializer { CatalogViewModel((application as OpusApplication).catalogRepository, createSavedStateHandle()) }
            })
            val experience: ExperienceViewModel = viewModel(factory = viewModelFactory {
                initializer { ExperienceViewModel((application as OpusApplication).achievements, (application as OpusApplication).recordings) }
            })
            val profile: ProfileViewModel = viewModel(factory = viewModelFactory {
                initializer { ProfileViewModel(application, createSavedStateHandle()) }
            })
            val admin: com.opus.piano.ui.admin.AdminViewModel = viewModel()
            val settings by profile.settings.collectAsStateWithLifecycle()
            val appearance by (application as OpusApplication).appearance.state.collectAsStateWithLifecycle()
            OpusTheme(settings.theme, settings.typography, appearance) {
                val light = androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() > .5f
                androidx.compose.runtime.SideEffect {
                    androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = light; isAppearanceLightNavigationBars = light
                    }
                }
                OpusApp(model, catalog, experience, profile, admin)
            }
        }
    }
}
