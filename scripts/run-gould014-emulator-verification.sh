#!/usr/bin/env bash
# Reproducible API 37 local runtime gate for Gould 0.14.
# Uses one explicit disposable emulator, validates AVD metadata and Android
# service readiness, performs launcher and instrumentation smoke checks before
# focused runtime instrumentation, then shuts down cleanly even after failure.
set -euo pipefail
cd "$(dirname "$0")/.."

SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-$HOME/Android/Sdk}}"
ADB="$SDK/platform-tools/adb"
EMULATOR="$SDK/emulator/emulator"
AVD="${GOULD014_AVD:-gould014-api37-gapi}"
PORT="${GOULD014_EMULATOR_PORT:-5586}"
SERIAL="emulator-$PORT"
AVD_HOME="${ANDROID_AVD_HOME:-$HOME/.android/avd}"
AVD_INI="$AVD_HOME/$AVD.ini"
AVD_CONFIG="$AVD_HOME/$AVD.avd/config.ini"
CLASSES="${GOULD014_TEST_CLASSES:-com.opus.piano.ActivityRepositoryTest,com.opus.piano.ActivityMigrationTest,com.opus.piano.WidgetTest,com.opus.piano.Stage8cUiRegressionTest}"
SMOKE_CLASS="${GOULD014_SMOKE_TEST_CLASS:-com.opus.piano.AppLanguageTest}"
RESULTS="${GOULD014_RESULTS_DIR:-/tmp/gould014-emulator-verification}"
mkdir -p "$RESULTS"
LOG="$RESULTS/emulator.log"
GRADLE_LOG="$RESULTS/instrumentation.log"
EMU_PID=""

fail() { echo "ERROR: $*" >&2; exit 1; }
[[ -x "$ADB" && -x "$EMULATOR" ]] || fail "Android SDK tools not found under $SDK"
[[ -x "$SDK/cmdline-tools/latest/bin/avdmanager" ]] || fail "cmdline-tools 23.0 avdmanager is required"
grep -qx 'Pkg.Revision=23.0' "$SDK/cmdline-tools/latest/source.properties" || fail "cmdline-tools/latest must be revision 23.0"
[[ -f "$AVD_INI" && -f "$AVD_CONFIG" ]] || fail "API 37 AVD $AVD is missing; preserve existing AVDs and create a disposable one with cmdline-tools 23.0"
grep -qx 'target=android-37.0' "$AVD_INI" || fail "AVD metadata target is not android-37.0: $AVD_INI"
grep -qx 'target=android-37.0' "$AVD_CONFIG" || fail "AVD config target is not android-37.0: $AVD_CONFIG"
grep -qx 'image.sysdir.1=system-images/android-37.0/google_apis/x86_64/' "$AVD_CONFIG" || fail "AVD must use the API 37 Google APIs x86_64 image"
[[ "$PORT" =~ ^[0-9]{4}$ ]] && (( PORT >= 5555 && PORT <= 5586 )) || fail "Choose an emulator port in 5555–5586"
[[ "$CLASSES" =~ ^com\.opus\.piano\.[A-Za-z0-9_]+(\#[A-Za-z0-9_]+)?(,com\.opus\.piano\.[A-Za-z0-9_]+(\#[A-Za-z0-9_]+)?)*$ ]] || fail "Test selectors must be comma-separated Gould instrumentation classes, optionally Class#method"
[[ "$SMOKE_CLASS" =~ ^com\.opus\.piano\.[A-Za-z0-9_]+$ ]] || fail "Smoke test must be a Gould instrumentation class"

if pgrep -af '[q]emu-system-x86_64-headless' >/dev/null; then
  pgrep -af '[q]emu-system-x86_64-headless' >&2 || true
  fail "An emulator QEMU process is already running; shut it down cleanly before starting this gate"
fi
if pgrep -af '[a]db.*wait-for-device' >/dev/null; then
  pgrep -af '[a]db.*wait-for-device' >&2 || true
  fail "A background adb wait-for-device process is already running; inspect and stop its owner before retrying"
fi
mapfile -t DEVICE_ROWS < <("$ADB" devices | awk 'NR>1 && NF>=2 {print $1 ":" $2}')
[[ ${#DEVICE_ROWS[@]} -eq 0 ]] || fail "ADB already has device rows (${DEVICE_ROWS[*]}); clear stale/offline devices or stop their owner before retrying"

[[ -e /dev/kvm && -r /dev/kvm && -w /dev/kvm ]] || fail "Readable and writable host /dev/kvm is required"
"$EMULATOR" -accel-check || fail "KVM acceleration check failed"
"$ADB" start-server >/dev/null
"$EMULATOR" -avd "$AVD" -port "$PORT" -no-window -no-audio -no-boot-anim \
  -gpu swangle -read-only -no-snapshot-load -no-snapshot-save -verbose >"$LOG" 2>&1 &
EMU_PID=$!

cleanup() {
  local status=$?
  case "${1:-}" in INT) status=130 ;; TERM) status=143 ;; esac
  trap - EXIT INT TERM
  if "$ADB" -s "$SERIAL" get-state >/dev/null 2>&1; then
    "$ADB" -s "$SERIAL" emu kill >/dev/null 2>&1 || true
  fi
  if [[ -n "$EMU_PID" ]]; then
    for _ in $(seq 1 60); do
      kill -0 "$EMU_PID" 2>/dev/null || break
      sleep 1
    done
    if kill -0 "$EMU_PID" 2>/dev/null; then
      echo "ERROR: emulator did not exit after console shutdown; see $LOG" >&2
      status=1
    else
      wait "$EMU_PID" 2>/dev/null || true
    fi
  fi
  exit "$status"
}
trap cleanup EXIT
trap 'cleanup INT' INT
trap 'cleanup TERM' TERM

ready=0
for _ in $(seq 1 180); do
  kill -0 "$EMU_PID" 2>/dev/null || { tail -80 "$LOG" >&2; fail "Emulator exited before ADB registration"; }
  state=$("$ADB" -s "$SERIAL" get-state 2>/dev/null || true)
  if [[ "$state" == device ]]; then
    boot=$("$ADB" -s "$SERIAL" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r' || true)
    mount=$("$ADB" -s "$SERIAL" shell service check mount 2>/dev/null | tr -d '\r' || true)
    qemu=$("$ADB" -s "$SERIAL" shell getprop ro.kernel.qemu 2>/dev/null | tr -d '\r' || true)
    api=$("$ADB" -s "$SERIAL" shell getprop ro.build.version.sdk 2>/dev/null | tr -d '\r' || true)
    if [[ "$boot" == 1 && "$mount" == *found* && "$qemu" == 1 && "$api" == 37 ]]; then
      ready=1
      break
    fi
  fi
  sleep 2
done
[[ "$ready" -eq 1 ]] || { tail -100 "$LOG" >&2; fail "Timed out waiting for API 37 boot and mount service"; }
for _ in 1 2 3 4 5; do
  [[ $("$ADB" -s "$SERIAL" get-state) == device ]] || fail "Emulator went offline after boot"
  [[ $("$ADB" -s "$SERIAL" shell getprop sys.boot_completed | tr -d '\r') == 1 ]] || fail "Android boot state regressed"
  sleep 2
done
printf 'ready serial=%s api=%s boot=%s mount=%s renderer=swangle\n' "$SERIAL" "$api" "$boot" "$mount" | tee "$RESULTS/readiness.txt"

"$ADB" -s "$SERIAL" install -r app/build/outputs/apk/debug/app-debug.apk
"$ADB" -s "$SERIAL" shell am start -W -n com.opus.piano/.MainActivity | tee "$RESULTS/launcher-smoke.txt"
grep -q 'Status: ok' "$RESULTS/launcher-smoke.txt" || fail "MainActivity launcher smoke failed"
"$ADB" -s "$SERIAL" shell dumpsys activity activities | grep -q 'topResumedActivity=.*com.opus.piano/.MainActivity' || fail "MainActivity did not become resumed"

ANDROID_SERIAL="$SERIAL" ./gradlew :app:connectedDebugAndroidTest --no-daemon --max-workers=1 --no-parallel \
  "-Pandroid.testInstrumentationRunnerArguments.class=$SMOKE_CLASS" 2>&1 | tee "$RESULTS/instrumentation-smoke.log"
grep -Eq 'Finished [1-9][0-9]* tests on ' "$RESULTS/instrumentation-smoke.log" || fail "Instrumentation smoke test executed no tests"
if grep -Eq 'FAILED|There were [1-9][0-9]* failure' "$RESULTS/instrumentation-smoke.log"; then fail "Instrumentation smoke test reported failures"; fi

ANDROID_SERIAL="$SERIAL" ./gradlew :app:connectedDebugAndroidTest --no-daemon --max-workers=1 --no-parallel \
  "-Pandroid.testInstrumentationRunnerArguments.class=$CLASSES" 2>&1 | tee "$GRADLE_LOG"
grep -Eq 'Finished [1-9][0-9]* tests on ' "$GRADLE_LOG" || fail "Instrumentation produced no executed tests"
if grep -Eq 'FAILED|There were [1-9][0-9]* failure' "$GRADLE_LOG"; then fail "Instrumentation reported failures"; fi
echo "PASS: focused API 37 runtime verification; evidence: $RESULTS"
