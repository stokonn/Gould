plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    // Android consumes the JVM artifact now; deterministic commonMain rules can
    // gain native targets later without moving Room or platform APIs.
    jvm {
        compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
    val iosTargets = listOf(iosArm64(), iosSimulatorArm64())
    iosTargets.forEach { target ->
        target.binaries.framework {
            baseName = "ActivityDomain"
            isStatic = true
        }
    }
    sourceSets {
        commonTest.dependencies { implementation(kotlin("test")) }
    }
}
