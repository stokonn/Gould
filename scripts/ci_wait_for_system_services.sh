#!/usr/bin/env bash
# Shared readiness gate for the Stage 5 emulator CI scripts.
#
# android-emulator-runner v2.38.0 (the pinned action) waits only for
# sys.boot_completed before running `input keyevent 82` and then starting our
# script; system services can still be unpublished at that point:
#   - run 43246507990 job 5.10 aborted pre-script with "Can't find service:
#     input" (upstream ReactiveCircus/android-emulator-runner#432 and #489;
#     the fix PR #495 is NOT in v2.38.0),
#   - run 43243176345 job 5.10 reached the script but hit a missing `activity`
#     service / "Broken pipe" during APK installation.
#   - a local API 37 run failed `package install-create` with a
#     StorageManager.getVolumes() NullPointerException because the `mount`
#     service (android.os.storage.IStorageManager) was not yet published even
#     though input/settings/package/activity were all found - PackageInstaller
#     cannot resolve an install volume without it.
# This gate refuses to start install/test work until the services our scripts
# and the connected suite actually depend on are published, requires two
# consecutive clean probes (services up for >=3s, not a crash-loop window),
# and preserves diagnostics when it times out.
#
# Usage: wait_for_system_services <emulator-NNNN> <evidence-dir>
# Writes <evidence-dir>/services-ready.txt on success and
# <evidence-dir>/services-not-ready-logcat.txt on timeout.
# Callers run under `set -e`: a non-zero return aborts the caller script.
wait_for_system_services() {
  local serial="${1:?emulator serial required}"
  local evidence_dir="${2:?evidence directory required}"
  local deadline=$((SECONDS + 180))
  local consecutive=0
  local missing svc
  while :; do
    missing=""
    if ! adb -s "$serial" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r' | grep -qx 1; then
      missing="$missing sys.boot_completed"
    fi
    # input: the action's `input keyevent 82`; settings/package/activity:
    # launcher + connected-suite install path; mount: PackageInstaller's
    # StorageManager volume resolution (NPE without it).
    for svc in input settings package activity mount; do
      # "Service input: found" vs "Service input: not found" - anchor on
      # ": found" so a "not found" line can never satisfy the probe.
      if ! adb -s "$serial" shell service check "$svc" 2>/dev/null | tr -d '\r' | grep -q ': found$'; then
        missing="$missing $svc"
      fi
    done
    if [ -z "$missing" ]; then
      consecutive=$((consecutive + 1))
      if [ "$consecutive" -ge 2 ]; then
        echo "System services ready on $serial (boot completed; input/settings/package/activity/mount published twice in a row)"
        printf 'serial=%s\nready_at=%s\n' "$serial" "$(date -u +%Y-%m-%dT%H:%M:%SZ)" > "$evidence_dir/services-ready.txt"
        return 0
      fi
    else
      consecutive=0
    fi
    if [ "$SECONDS" -ge "$deadline" ]; then
      echo "FATAL: system services not ready after 180s on $serial - missing:$missing"
      adb devices || true
      adb -s "$serial" shell getprop sys.boot_completed || true
      adb -s "$serial" shell service list 2>/dev/null | sed -n '1,40p' || true
      adb -s "$serial" logcat -d -v threadtime > "$evidence_dir/services-not-ready-logcat.txt" 2>/dev/null || true
      return 1
    fi
    sleep 3
  done
}
