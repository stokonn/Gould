# Gradle verification helper

This directory contains one retained, historical Gradle init script used for Gould 0.15 final verification. It isolates build and Room schema output from the normal project build; it is not part of the Android application's runtime or standard build configuration.

Run the project build from the repository root with `./gradlew`. The helper is only needed when reproducing the documented Gould 0.15 final-verification commands.
