#!/usr/bin/env bash
set -euo pipefail
# Emulator-only test installation. Never targets an implicit or physical device.
# Never clears storage or uninstalls the application.
# Install the matching baseline/candidate APK with adb install -r before each run.
serial="${1:-}"
[[ "$serial" =~ ^emulator-[0-9]+$ ]] || { echo "Usage: $0 emulator-NNNN [1-99 runs]" >&2; exit 2; }
shift
runs="${1:-10}"
adb_command=(adb -s "$serial")
[[ "$runs" =~ ^[1-9][0-9]?$ ]] || { echo 'Expected 1–99 runs' >&2; exit 1; }
"${adb_command[@]}" get-state >/dev/null
"${adb_command[@]}" shell getprop ro.product.model
"${adb_command[@]}" shell getprop ro.build.version.sdk
for ((i=1; i<=runs; i++)); do
    "${adb_command[@]}" shell am force-stop com.opus.piano
    "${adb_command[@]}" shell am start -W -n com.opus.piano/.MainActivity
    sleep 2
done
