#!/usr/bin/env bash
set -euo pipefail
SERIAL="emulator-5554"
ATTACHED="$(adb -s "$SERIAL" devices | awk '$2=="device"' | wc -l)"
if [ "$ATTACHED" -ne 1 ]; then echo "ERROR: expected exactly one attached device, found $ATTACHED"; adb -s "$SERIAL" devices; exit 1; fi
QEMU="$(adb -s "$SERIAL" shell getprop ro.kernel.qemu | tr -d '\r')"
if [ "$QEMU" != "1" ]; then echo "ERROR: $SERIAL is not a verified emulator (ro.kernel.qemu=$QEMU)"; exit 1; fi
export ANDROID_SERIAL="$SERIAL"
echo "Running the full connected suite on verified emulator $SERIAL"
mkdir -p stage5-ci-results
trap 'adb -s "$SERIAL" logcat -d > stage5-ci-results/logcat.txt 2>/dev/null || true' EXIT
./gradlew --no-daemon --max-workers=1 connectedDebugAndroidTest
