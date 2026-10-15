#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
app_version="$(sed -n 's/.*versionName = "\([^"]*\)".*/\1/p' app/build.gradle.kts)"
[[ "$app_version" =~ ^[0-9]+\.[0-9]+\.[0-9]+([.-][A-Za-z0-9.]+)?$ ]] || { printf '%s\n' "Invalid app version" >&2; exit 1; }
./gradlew assembleDebug
apk_path="app/build/outputs/apk/debug/Goldberg-v${app_version}.apk"
cp app/build/outputs/apk/debug/app-debug.apk "$apk_path"
printf '%s\n' "APK: $PWD/$apk_path"
