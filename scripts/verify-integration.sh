#!/usr/bin/env bash
# INTEGRATION tier — meaningful feature-boundary verification for Gould.
#
# Broader host verification (full JVM suite + lint + instrumentation compilation)
# and, when INSTRUMENTATION_CLASSES is set, execution of the affected native
# classes on ONE identified disposable emulator. Never runs against a phone,
# never starts a second emulator (8 GB host guard), never runs concurrently
# with another heavyweight device process.
#
# Usage:
#   scripts/verify-integration.sh                          # host integration gate
#   INSTRUMENTATION_CLASSES=com.opus.piano.DatabaseTest \
#     scripts/verify-integration.sh                        # + affected native classes
#   INSTRUMENTATION_CLASSES=... INSTRUMENTATION_SERIAL=emulator-5586 scripts/verify-integration.sh
set -euo pipefail
cd "$(dirname "$0")/.."

start=$(date +%s)
detail_parts=()

fail() {
  scripts/verify-audit.py INTEGRATION FAIL $(($(date +%s) - start)) "${detail_parts[*]:-failed}" || true
  exit 1
}

# 1. Full host JVM suite + lint + instrumentation compilation (no execution yet).
echo "== INTEGRATION: full JVM suite, lint, androidTest compilation =="
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest || fail
detail_parts+=("full-JVM+lint+androidTest-compile")

# 2. Optional native execution of affected classes on a single identified emulator.
if [[ -n "${INSTRUMENTATION_CLASSES:-}" ]]; then
  SERIAL="${INSTRUMENTATION_SERIAL:-}"
  if [[ -z "$SERIAL" ]]; then
    # Pick the only running emulator; refuse ambiguity.
    mapfile -t EMULATORS < <(adb devices | awk '$2=="device" && $1 ~ /^emulator-[0-9]+$/ {print $1}')
    [[ ${#EMULATORS[@]} -eq 1 ]] || { echo "Refusing: need exactly one running emulator, found ${#EMULATORS[@]}"; fail; }
    SERIAL="${EMULATORS[0]}"
  fi
  QEMU="$(adb -s "$SERIAL" shell getprop ro.kernel.qemu | tr -d '\r')"
  [[ "$QEMU" == 1 ]] || { echo "Refusing non-emulator target $SERIAL"; fail; }
  # 8 GB host guard: no concurrent heavyweight emulators.
  COUNT="$(adb devices | awk '$2=="device" && $1 ~ /^emulator-[0-9]+$/ {n++} END {print n+0}')"
  [[ "$COUNT" -eq 1 ]] || { echo "Refusing: $COUNT emulators running on this host"; fail; }
  echo "== INTEGRATION: native classes on $SERIAL =="
  ANDROID_SERIAL="$SERIAL" ./gradlew :app:connectedDebugAndroidTest \
    "-Pandroid.testInstrumentationRunnerArguments.class=$INSTRUMENTATION_CLASSES" || fail
  detail_parts+=("native($INSTRUMENTATION_CLASSES)@$SERIAL")
fi

elapsed=$(($(date +%s) - start))
scripts/verify-audit.py INTEGRATION PASS "$elapsed" "${detail_parts[*]}"
echo "== INTEGRATION PASS in ${elapsed}s =="
