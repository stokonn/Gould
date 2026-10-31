#!/usr/bin/env bash
# Local unsigned R8 experiment only. Never signs, installs, tags or publishes.
set -euo pipefail
cd "$(dirname "$0")/.."
if [[ "${GOULD_GRADLE_LOCK_HELD:-}" != 1 ]]; then
  exec scripts/gradle-lock.sh "$PWD/scripts/build-optimized-local.sh" "$@"
fi
[[ $# -eq 0 ]] || { echo "This bounded build accepts no extra tasks or flags." >&2; exit 2; }
if pgrep -x 'qemu-system.*' >/dev/null; then
  echo "Stop the emulator before this memory-intensive R8 build." >&2
  exit 2
fi
mkdir -p .gradle/renaissance
run_dir="$(mktemp -d ".gradle/renaissance/r8-$(date -u +%Y%m%dT%H%M%SZ)-XXXX")"
echo "Local unsigned R8 build; logs: $run_dir"
start=$(date +%s)
status=0
/usr/bin/time -v -o "$run_dir/time.txt" ./gradlew :app:assembleRelease \
  --max-workers=1 --no-parallel --no-daemon --offline --console=plain \
  '-Dorg.gradle.jvmargs=-Xmx2560m -XX:ActiveProcessorCount=2 -Dfile.encoding=UTF-8' \
  >"$run_dir/build.log" 2>&1 || status=$?
if [[ "$status" -ne 0 ]]; then
  tail -60 "$run_dir/build.log"
  scripts/verify-audit.py INTEGRATION FAIL "$(($(date +%s)-start))" "Local R8 artifact build failed; $run_dir/build.log"
  exit "$status"
fi
python3 - <<'APK' || status=$?
from pathlib import Path
import hashlib, zipfile
p=Path('app/build/outputs/apk/release/app-release-unsigned.apk')
with zipfile.ZipFile(p) as z:
    assert z.testzip() is None, 'APK ZIP integrity failure'
size=p.stat().st_size
print(f'{p}: {size} bytes; {(1-size/18757232)*100:.4f}% below 0.12 baseline')
print('SHA-256:',hashlib.sha256(p.read_bytes()).hexdigest())
APK
if [[ "$status" -ne 0 ]]; then
  scripts/verify-audit.py INTEGRATION FAIL "$(($(date +%s)-start))" "Local R8 artifact validation failed; $run_dir/build.log"
  exit "$status"
fi
scripts/verify-audit.py INTEGRATION PARTIAL "$(($(date +%s)-start))" "Local unsigned R8 artifact build PASS; runtime acceptance separate; $run_dir/build.log"
echo "No full verification tier or runtime acceptance is implied by this build."
