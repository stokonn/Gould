plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    // Android consumes the JVM artifact now; deterministic commonMain rules can
    // gain native targets later without moving Room or platform APIs.
    jvm {
        compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
    sourceSets {
        commonTest.dependencies { implementation(kotlin("test")) }
    }
}
