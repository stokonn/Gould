#!/usr/bin/env bash
set -euo pipefail
# Run against a test installation. Never clears storage or uninstalls the application.
# Install the matching baseline/candidate APK with adb install -r before each run.
runs="${1:-10}"
[[ "$runs" =~ ^[1-9][0-9]?$ ]] || { echo 'Expected 1–99 runs' >&2; exit 1; }
adb get-state >/dev/null
adb shell getprop ro.product.model
adb shell getprop ro.build.version.sdk
for ((i=1; i<=runs; i++)); do
    adb shell am force-stop com.opus.piano
    adb shell am start -W -n com.opus.piano/.MainActivity
    sleep 2
done
