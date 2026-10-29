#!/usr/bin/env bash
set -euo pipefail
SERIAL="emulator-5554"
ATTACHED="$(adb -s "$SERIAL" devices | awk '$2=="device"' | wc -l)"
if [ "$ATTACHED" -ne 1 ]; then echo "ERROR: expected exactly one attached device, found $ATTACHED"; adb -s "$SERIAL" devices; exit 1; fi
QEMU="$(adb -s "$SERIAL" shell getprop ro.kernel.qemu | tr -d '\r')"
if [ "$QEMU" != "1" ]; then echo "ERROR: $SERIAL is not a verified emulator (ro.kernel.qemu=$QEMU)"; exit 1; fi
export ANDROID_SERIAL="$SERIAL"
echo "Running acceptance subset on verified emulator $SERIAL"
mkdir -p stage5-ci-results
trap 'adb -s "$SERIAL" logcat -d > stage5-ci-results/logcat.txt 2>/dev/null || true' EXIT
# (a) Seeded in-place upgrade from the debug rebuild of the exact v0.10.1
#     tag (same runner debug keystore as the candidate, so signatures
#     match). The helper asserts baseline schema, version and per-table
#     data equality after migration; disposable emulator seeding only.
python3 scripts/verify_emulator_upgrade.py \
  --serial "$SERIAL" \
  --baseline baseline-src/app/build/outputs/apk/debug/app-debug.apk \
  --baseline-version 0.10.1 --baseline-schema 8 --expected-schema 11 \
  --candidate app/build/outputs/apk/debug/app-debug.apk \
  --output stage5-ci-results/upgrade \
  --seed-disposable --offline --startup-runs 5
# (b) Wi-Fi -> mobile default-network transition and Data Saver, with real
#     state assertions (not just collected logs). The upgrade helper ran
#     with --offline (wifi AND mobile data disabled), so both are restored
#     before asserting the transition.
cat > stage5-ci-results/assert_default_network.py <<'PYEOF'
import re
import sys

expected, dump_path = sys.argv[1], sys.argv[2]
dump = open(dump_path, encoding='utf-8', errors='replace').read()
match = re.search(r'^\s*Active default network: (.*)$', dump, re.M)
if not match:
    sys.exit('FATAL: "Active default network:" line missing from dumpsys connectivity')
net_id = match.group(1).strip()
if not re.fullmatch(r'\d+', net_id):
    sys.exit(f'FATAL: expected a numeric active default network for transport {expected}, got: {net_id!r}')
blocks = re.findall(r'^[ \t]*NetworkAgentInfo\{network\{' + re.escape(net_id) + r'\}.*$', dump, re.M)
if not blocks:
    sys.exit(f'FATAL: no NetworkAgentInfo block found for active default network {net_id}')
block = blocks[0]
ni = re.search(r'ni\{([^}]*)\}', block)
# API 36 (run 39552116455 evidence, connectivity-dump.txt) prints the
# transport ONLY in the nc{[ Transports: ... ]} capabilities section;
# the legacy ni{...} "type:" token does not exist there
# (ni{WIFI CONNECTED extra: }). Assertions unchanged: the active default
# network must be numeric and its NAI must declare the expected
# transport (WIFI->WIFI, MOBILE->CELLULAR naming mapped explicitly).
nc = re.search(r'nc\{\[\s*Transports:\s*(\w+)', block)
if nc:
    found = nc.group(1)
    wanted = {'WIFI': 'WIFI', 'MOBILE': 'CELLULAR'}.get(expected, expected)
    if found != wanted:
        sys.exit(f'FATAL: active default network {net_id} transport is {found}, expected {wanted} ({expected})')
    print(f'OK: active default network {net_id} uses transport {found} ({expected})')
    sys.exit(0)
legacy = re.search(r'type: (\w+)', ni.group(1)) if ni else None
if not legacy:
    sys.exit(f'FATAL: no transport token (nc Transports / ni type:) in active-network block: {block[:240]!r}')
found = legacy.group(1)
if found != expected:
    sys.exit(f'FATAL: active default network {net_id} transport is {found}, expected {expected}')
print(f'OK: active default network {net_id} uses transport {found}')
PYEOF
wait_for_transport() {
  local expected="$1" deadline=$((SECONDS + 60))
  while [ "$SECONDS" -lt "$deadline" ]; do
    adb -s "$SERIAL" shell dumpsys connectivity > stage5-ci-results/connectivity-dump.txt 2>/dev/null || true
    if python3 stage5-ci-results/assert_default_network.py "$expected" stage5-ci-results/connectivity-dump.txt >>stage5-ci-results/network-evidence.txt 2>&1; then
      sed -n 's/.*Active default network: /active default network: /p' stage5-ci-results/connectivity-dump.txt | head -n1
      return 0
    fi
    sleep 4
  done
  echo "FATAL: active default network never reached transport $expected within 60s"
  return 1
}
{
  echo "== restore mobile data and Wi-Fi (upgrade helper left both disabled) =="
  adb -s "$SERIAL" shell cmd connectivity airplane-mode disable
  adb -s "$SERIAL" shell svc data enable
  adb -s "$SERIAL" shell settings put global mobile_data 1
  adb -s "$SERIAL" shell svc wifi enable
  echo "== expect Wi-Fi as active default network =="
  WIFI_NET="$(wait_for_transport WIFI)"
  echo "$WIFI_NET"
  echo "== disable Wi-Fi, expect mobile fallback as active default network =="
  adb -s "$SERIAL" shell svc wifi disable
  MOBILE_NET="$(wait_for_transport MOBILE)"
  echo "$MOBILE_NET"
  if [ "$WIFI_NET" = "$MOBILE_NET" ]; then
    echo "FATAL: default network did not change across the Wi-Fi -> mobile transition"; exit 1
  fi
  echo "OK: default network transitioned ($WIFI_NET -> $MOBILE_NET)"
  echo "== restore Wi-Fi for the remainder of the run =="
  adb -s "$SERIAL" shell svc wifi enable
  wait_for_transport WIFI >/dev/null
  echo "== Data Saver enable with state readback =="
  adb -s "$SERIAL" shell cmd netpolicy set restrict-background true
  sleep 2
  DS_GET="$(adb -s "$SERIAL" shell cmd netpolicy get restrict-background | tr -d '\r')"
  echo "cmd netpolicy get restrict-background: $DS_GET"
  [ "$DS_GET" = "Restrict background status: enabled" ] || { echo "FATAL: Data Saver not enabled (readback: $DS_GET)"; exit 1; }
  adb -s "$SERIAL" shell dumpsys netpolicy > stage5-ci-results/netpolicy-enabled.txt
  grep -E "Restrict background: true" stage5-ci-results/netpolicy-enabled.txt || { echo "FATAL: dumpsys netpolicy does not report Restrict background: true"; exit 1; }
  echo "== Data Saver disable with state readback =="
  adb -s "$SERIAL" shell cmd netpolicy set restrict-background false
  sleep 2
  DS_GET="$(adb -s "$SERIAL" shell cmd netpolicy get restrict-background | tr -d '\r')"
  echo "cmd netpolicy get restrict-background: $DS_GET"
  [ "$DS_GET" = "Restrict background status: disabled" ] || { echo "FATAL: Data Saver not disabled (readback: $DS_GET)"; exit 1; }
  adb -s "$SERIAL" shell dumpsys netpolicy > stage5-ci-results/netpolicy-disabled.txt
  grep -E "Restrict background: false" stage5-ci-results/netpolicy-disabled.txt || { echo "FATAL: dumpsys netpolicy does not report Restrict background: false"; exit 1; }
  echo "OK: Data Saver toggled with verified state readback"
} | tee stage5-ci-results/network-evidence.txt
# (c) Real process death + cold restart with database integrity check.
adb -s "$SERIAL" shell am start -W -n com.opus.piano/.MainActivity
sleep 5
test -n "$(adb -s "$SERIAL" shell pidof com.opus.piano | tr -d '\r')"
adb -s "$SERIAL" shell am force-stop com.opus.piano
test -z "$(adb -s "$SERIAL" shell pidof com.opus.piano | tr -d '\r')"
adb -s "$SERIAL" shell am start -W -n com.opus.piano/.MainActivity
sleep 5
test -n "$(adb -s "$SERIAL" shell pidof com.opus.piano | tr -d '\r')"
adb -s "$SERIAL" shell run-as com.opus.piano ls databases | tee stage5-ci-results/process-death-db.txt
mkdir -p stage5-ci-results/db-after-restart
for SUFFIX in '' '-wal' '-shm'; do
  adb -s "$SERIAL" exec-out run-as com.opus.piano cat databases/opus.db$SUFFIX > stage5-ci-results/db-after-restart/opus.db$SUFFIX 2>/dev/null || rm -f stage5-ci-results/db-after-restart/opus.db$SUFFIX
done
test -s stage5-ci-results/db-after-restart/opus.db
INTEGRITY="$(sqlite3 stage5-ci-results/db-after-restart/opus.db 'PRAGMA integrity_check;')"
echo "PRAGMA integrity_check: $INTEGRITY" | tee stage5-ci-results/process-death-integrity.txt
[ "$INTEGRITY" = "ok" ] || { echo "FATAL: database integrity_check failed"; exit 1; }
FK_VIOLATIONS="$(sqlite3 stage5-ci-results/db-after-restart/opus.db 'PRAGMA foreign_key_check;')"
echo "PRAGMA foreign_key_check violations: ${FK_VIOLATIONS:-none}" | tee -a stage5-ci-results/process-death-integrity.txt
[ -z "$FK_VIOLATIONS" ] || { echo "FATAL: foreign_key_check reported violations"; exit 1; }
SCHEMA="$(sqlite3 stage5-ci-results/db-after-restart/opus.db 'PRAGMA user_version;')"
echo "PRAGMA user_version: $SCHEMA" | tee -a stage5-ci-results/process-death-integrity.txt
[ "$SCHEMA" = "11" ] || { echo "FATAL: unexpected schema version $SCHEMA after restart (expected 11)"; exit 1; }
echo "OK: post-restart database passed integrity, foreign-key and schema checks"
# (d) Startup cost measurement (5 warm cold-process launches).
bash scripts/measure-startup.sh "$SERIAL" 5 | tee stage5-ci-results/startup-measurement.txt
