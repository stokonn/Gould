#!/usr/bin/env bash
# RELEASE tier — mandatory full release verification for Gould (final source freeze).
#
# Executes the project-policy mandatory host command EXACTLY as defined in
# AGENTS.md (clean build, no daemon, single worker — reproducibility over speed),
# plus every mandatory pure-Python policy check. This script never publishes,
# never signs, never tags and never touches main. Release-specific artifact
# gates (signing identity, ZIP integrity, genuine published-version upgrade,
# security/privacy/licensing) run afterwards through their own documented
# scripts against the exact frozen commit — see VERIFICATION-MATRIX.md.
set -euo pipefail
cd "$(dirname "$0")/.."

start=$(date +%s)
detail_parts=()

fail() {
  scripts/verify-audit.py RELEASE FAIL $(($(date +%s) - start)) "${detail_parts[*]:-failed}" || true
  exit 1
}

echo "== RELEASE: mandatory policy build & tests (clean, no-daemon, max-workers=1) =="
./gradlew clean assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug \
  --no-daemon --max-workers=1 || fail
detail_parts+=("policy-clean-gate")

echo "== RELEASE: mandatory policy checks =="
for check in check_agent_policy check_localization check_migrations \
             check_backup_fixtures check_history_paging; do
  python3 "scripts/$check.py" || fail
done
detail_parts+=("policy-checks(5)")

elapsed=$(($(date +%s) - start))
scripts/verify-audit.py RELEASE PASS "$elapsed" "${detail_parts[*]}"
echo "== RELEASE host gate PASS in ${elapsed}s =="
echo "Remaining release gates (signing artifact, upgrade, security/privacy/licensing,"
echo "performance, emulator acceptance) are executed separately per VERIFICATION-MATRIX.md"
echo "against this exact commit before any publication decision."
