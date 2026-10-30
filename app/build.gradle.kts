plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}
android {
    namespace = "com.opus.piano"
    compileSdk = 36
    defaultConfig { applicationId = "com.opus.piano"; minSdk = 26; targetSdk = 36; versionCode = 14; versionName = if (providers.gradleProperty("goldbergStable").orNull == "true") "0.12" else "0.12-dev"; testInstrumentationRunner = if (providers.gradleProperty("goldbergStage8Probe").orNull == "true") "com.opus.piano.Stage8bAssessmentProbe" else "androidx.test.runner.AndroidJUnitRunner" }
    buildFeatures { compose = true }
    // Shared synthetic contract corpus is available to JVM tests only, never the APK.
    sourceSets.getByName("test").resources.srcDir("../contracts/admin/v1")
    if (providers.gradleProperty("goldbergCanonicalIntegration").orNull == "true")
        sourceSets.getByName("test").kotlin.srcDir("src/canonicalIntegration/java")
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
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation(platform("androidx.compose:compose-bom:2025.08.01"))
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.2")
    implementation("androidx.navigation:navigation-compose:2.9.3")
    implementation("androidx.room:room-runtime:2.7.2")
    implementation("androidx.room:room-ktx:2.7.2")
    // Stage 5 (D5.3, owner-mandated): persistent background scheduling for offline bundles.
    implementation("androidx.work:work-runtime-ktx:2.12.0")
    ksp("androidx.room:room-compiler:2.7.2")
    testImplementation("junit:junit:4.13.2")
    // Test-only real org.json so JVM unit tests can execute the manifest codec; the
    // Android mockable jar stubs org.json and throws "not mocked". Not shipped in the APK.
    testImplementation("org.json:json:20240303")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.work:work-testing:2.12.0")
}
