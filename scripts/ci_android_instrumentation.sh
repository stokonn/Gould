#!/usr/bin/env bash
# Full connected suite on the single disposable Actions emulator. Never a phone.
set -euo pipefail
mkdir -p stage5-ci-results
export ANDROID_SERIAL=emulator-5554
SERIAL="$ANDROID_SERIAL"
QEMU="$(adb -s "$SERIAL" shell getprop ro.kernel.qemu | tr -d '\r')"
test "$QEMU" = 1 || { echo 'Refusing non-emulator target'; exit 1; }
# Reject additional devices rather than allowing Gradle to select them.
adb -s "$SERIAL" devices > stage5-ci-results/adb-devices.txt
test "$(awk 'NR>1 && NF>=2 {count++} END {print count+0}' stage5-ci-results/adb-devices.txt)" = 1
{
  echo "serial=$SERIAL"
  adb -s "$SERIAL" shell getprop ro.build.version.sdk
  adb -s "$SERIAL" shell getprop ro.build.fingerprint
  adb -s "$SERIAL" shell wm size
  adb -s "$SERIAL" shell wm density
  adb -s "$SERIAL" shell settings get system font_scale
} > stage5-ci-results/emulator-identity.txt
test "$(adb -s "$SERIAL" shell getprop ro.build.version.sdk | tr -d '\r')" = 36
collect() {
  # Instrumentation exports synthetic Admin renders before suite uninstall.
  adb -s "$SERIAL" pull /data/local/tmp/goldberg-stage7b stage5-ci-results/admin-evidence > stage5-ci-results/admin-evidence-pull.txt 2>&1 || true
  adb -s "$SERIAL" logcat -d -v threadtime > stage5-ci-results/logcat.txt 2>&1 || true
  adb -s "$SERIAL" shell dumpsys activity activities > stage5-ci-results/activity.txt 2>&1 || true
  adb -s "$SERIAL" exec-out screencap -p > stage5-ci-results/final-screen.png 2> stage5-ci-results/screencap.err || true
}
trap collect EXIT
set -o pipefail
./gradlew --no-daemon --max-workers=1 connectedDebugAndroidTest 2>&1 | tee stage5-ci-results/instrumentation-gradle.log
# A real UI smoke artifact, not proof of the Admin visual acceptance matrix.
# The connected-test runner removes its installed app during suite teardown.
# Restore the same built APK; keep signature compatibility strict with install -r.
adb -s "$SERIAL" install -r app/build/outputs/apk/debug/app-debug.apk > stage5-ci-results/smoke-install.txt
adb -s "$SERIAL" shell am start -W -n com.opus.piano/.MainActivity > stage5-ci-results/ui-start.txt
sleep 2
adb -s "$SERIAL" exec-out screencap -p > stage5-ci-results/app-smoke.png
