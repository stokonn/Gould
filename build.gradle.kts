plugins {
    id("com.android.application") version "9.5.3" apply false
    id("org.jetbrains.kotlin.android") version "2.4.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.21" apply false
    // KSP versions are decoupled from Kotlin since 2.3.0; there is no KSP 2.4.0.
    id("com.google.devtools.ksp") version "2.3.14" apply false
    id("org.jetbrains.kotlin.multiplatform") version "2.4.21" apply false
}
