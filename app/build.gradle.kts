plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}
android {
    namespace = "com.opus.piano"
    compileSdk = 37
    defaultConfig { applicationId = "com.opus.piano"; minSdk = 30; targetSdk = 37; versionCode = 17; versionName = if (providers.gradleProperty("goldbergStable").orNull == "true") "0.15.0" else "0.15.0-dev"; testInstrumentationRunner = if (providers.gradleProperty("goldbergStage8Probe").orNull == "true") "com.opus.piano.Stage8bAssessmentProbe" else "androidx.test.runner.AndroidJUnitRunner" }
    buildFeatures { compose = true }
    androidResources { noCompress += "dbz" }
    bundle { language { enableSplit = false } }
    buildTypes {
        release {
            // Gould 0.13: R8 code + resource shrinking for the release APK.
            // The published 0.12 baseline shipped unminified (50.7 MB of DEX).
            // Entry points (manifest activities/services/receivers, Room generated
            // code, WorkManager workers) are kept by manifest/AAR consumer rules;
            // app code uses no reflection (platform org.json only).
            // Signing is attached by the trusted private init script at
            // publication time; never embed keystore material here.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }
ksp { arg("room.schemaLocation", "$projectDir/schemas") }
dependencies {
    implementation(project(":activity-domain"))
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation("androidx.appcompat:appcompat:1.8.0")
    implementation(platform("androidx.compose:compose-bom:2026.09.00"))
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.1")
    implementation("androidx.navigation:navigation-compose:2.10.2")
    implementation("androidx.room:room-runtime:2.8.5")
    implementation("androidx.room:room-ktx:2.8.5")
    // Stage 5 (D5.3, owner-mandated): persistent background scheduling for offline bundles.
    implementation("androidx.work:work-runtime-ktx:2.12.0")
    ksp("androidx.room:room-compiler:2.8.5")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
    // Test-only real org.json so JVM unit tests can execute the manifest codec; the
    // Android mockable jar stubs org.json and throws "not mocked". Not shipped in the APK.
    testImplementation("org.json:json:20260814")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:runner:1.8.0-alpha04")
    androidTestImplementation("androidx.work:work-testing:2.12.0")
    androidTestImplementation(platform("androidx.compose:compose-bom:2026.09.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    // Compose's ui-test bridges to Espresso.onIdle. Espresso 3.5.0/3.7.0 eagerly
    // reflect android.hardware.input.InputManager.getInstance(), which Android 17
    // (API 37) removed; 3.8 obtains the service through Context.getSystemService.
    // Test-only dependency; no runtime or shipped code is affected.
    androidTestImplementation("androidx.test.espresso:espresso-core:3.8.0-alpha04")
}
