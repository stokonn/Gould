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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val model: OpusViewModel = viewModel(factory = viewModelFactory {
                initializer { OpusViewModel((application as OpusApplication).repository, createSavedStateHandle()) }
            })
            val catalog: CatalogViewModel = viewModel(factory = viewModelFactory {
                initializer { CatalogViewModel((application as OpusApplication).catalogRepository, createSavedStateHandle()) }
            })
            val experience: ExperienceViewModel = viewModel(factory = viewModelFactory {
                initializer { ExperienceViewModel((application as OpusApplication).achievements, (application as OpusApplication).recordings) }
            })
            val profile: ProfileViewModel = viewModel()
            val settings by profile.settings.collectAsStateWithLifecycle()
            OpusTheme(settings.theme) {
                val light = androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() > .5f
                androidx.compose.runtime.SideEffect {
                    androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = light; isAppearanceLightNavigationBars = light
                    }
                }
                OpusApp(model, catalog, experience, profile)
            }
        }
    }
}
