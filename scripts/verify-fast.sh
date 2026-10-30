#!/usr/bin/env bash
# FAST tier — everyday Gould development verification.
#
# Compiles the affected module, runs JVM unit tests and the fast pure-Python
# static checks. Uses the Gradle daemon and incremental builds: no clean build,
# no --no-daemon, no emulator. Every run appends one audit line.
#
# Usage:
#   scripts/verify-fast.sh                     # compile + all JVM tests + static checks
#   scripts/verify-fast.sh --tests <pattern>   # compile + focused JVM tests only
#   FAST_STATIC=skip scripts/verify-fast.sh    # compile + JVM tests, skip python checks
set -euo pipefail
cd "$(dirname "$0")/.."
if [[ "${GOULD_GRADLE_LOCK_HELD:-}" != 1 ]]; then
  exec scripts/gradle-lock.sh "$PWD/scripts/verify-fast.sh" "$@"
fi

TEST_FILTER=()
if [[ "${1:-}" == "--tests" ]]; then
  TEST_FILTER=(--tests "$2")
  shift 2
fi

start=$(date +%s)
status=PASS
detail_parts=()

fail() {
  status=FAIL
  scripts/verify-audit.py FAST FAIL $(($(date +%s) - start)) "${detail_parts[*]:-failed}" || true
  exit 1
}

# 1. Affected-module compilation (single-module project: :app Kotlin/Compose/KSP).
echo "== FAST: compileDebugKotlin =="
./gradlew :app:compileDebugKotlin --max-workers=1 --no-parallel "$@" || fail
detail_parts+=("compileDebugKotlin")

# 2. JVM unit tests (full suite by default; focused with --tests).
echo "== FAST: testDebugUnitTest ${TEST_FILTER[*]:-} =="
./gradlew :app:testDebugUnitTest --max-workers=1 --no-parallel "${TEST_FILTER[@]}" || fail
detail_parts+=("testDebugUnitTest${TEST_FILTER[*]:+ (${TEST_FILTER[*]})}")

# 3. Fast static checks (pure Python against source; seconds, no Android runtime).
if [[ "${FAST_STATIC:-run}" != "skip" ]]; then
  echo "== FAST: static checks =="
  python3 scripts/check_localization.py || fail
  python3 scripts/check_migrations.py || fail
  python3 scripts/check_backup_fixtures.py || fail
  python3 scripts/check_history_paging.py || fail
  detail_parts+=("static:localization+migrations+backup+history")
fi

elapsed=$(($(date +%s) - start))
scripts/verify-audit.py FAST PASS "$elapsed" "${detail_parts[*]}"
echo "== FAST PASS in ${elapsed}s =="
