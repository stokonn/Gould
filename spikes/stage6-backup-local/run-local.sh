#!/usr/bin/env bash
set -Eeuo pipefail

# Synthetic-only resilience drill. Recovery keys are intentionally supplied
# outside the repository and outside BACKUP_RESULTS_DIR.
cd -- "$(dirname -- "$0")"
umask 077

RUNTIME="${RUNTIME:-podman}"
IMAGE='docker.io/library/postgres:17'
KEY_DIR="${BACKUP_KEY_DIR:-}"
RESULTS_DIR="${BACKUP_RESULTS_DIR:-}"
START_NS="$(date +%s%N)"

die() { echo "FAIL: $*" >&2; exit 1; }
cleanup() {
  rm -f -- \
    "$RESULTS_DIR/backup-v1.partial.dump.gpg" \
    "$RESULTS_DIR/backup-v1.partial.dump.gpg.sha256" \
    "$RESULTS_DIR/backup-v1.tampered.dump.gpg" \
    "$RESULTS_DIR/backup-v1.tampered.dump.gpg.sha256"
}
trap cleanup EXIT
command -v "$RUNTIME" >/dev/null 2>&1 || die "Podman is unavailable"
command -v gpg >/dev/null 2>&1 || die "GnuPG is unavailable"
command -v openssl >/dev/null 2>&1 || die "OpenSSL is unavailable"
command -v sha256sum >/dev/null 2>&1 || die "sha256sum is unavailable"
"$RUNTIME" image inspect "$IMAGE" >/dev/null 2>&1 ||
  die "required cached image $IMAGE is unavailable; refusing an implicit pull"
[[ "$KEY_DIR" = /* ]] || die "BACKUP_KEY_DIR must be an absolute private path"
[[ "$RESULTS_DIR" = /* ]] || die "BACKUP_RESULTS_DIR must be an absolute path"
[[ "$KEY_DIR" != "$PWD"/* ]] || die "recovery keys must be outside the repository"
[[ "$KEY_DIR" != "$RESULTS_DIR"/* && "$RESULTS_DIR" != "$KEY_DIR"/* ]] ||
  die "key custody and backup destination must be different directories"
mkdir -p -- "$KEY_DIR" "$RESULTS_DIR"
chmod 700 "$KEY_DIR" "$RESULTS_DIR"

for version in v1 v2; do
  key="$KEY_DIR/recovery-$version.key"
  if [[ ! -e "$key" ]]; then
    openssl rand -hex 32 >"$key"
    chmod 600 "$key"
  fi
  [[ -f "$key" && "$(stat -c '%a' "$key")" = 600 ]] ||
    die "recovery key $version is not a mode-600 regular file"
done

run_phase() {
  "$PWD/run-phase.sh" "$@"
}

EXPORT_START="$(date +%s%N)"
run_phase export "$KEY_DIR/recovery-v1.key" "$RESULTS_DIR" "$RESULTS_DIR/backup-v1.dump.gpg"
EXPORT_END="$(date +%s%N)"

RESTORE_START="$(date +%s%N)"
run_phase restore "$KEY_DIR/recovery-v1.key" "$RESULTS_DIR" "$RESULTS_DIR/backup-v1.dump.gpg"
RESTORE_END="$(date +%s%N)"

set +e
run_phase restore "$KEY_DIR/recovery-v2.key" "$RESULTS_DIR" "$RESULTS_DIR/backup-v1.dump.gpg" \
  >/dev/null 2>&1
WRONG_KEY_RC=$?
run_phase restore "$KEY_DIR/missing.key" "$RESULTS_DIR" "$RESULTS_DIR/backup-v1.dump.gpg" \
  >/dev/null 2>&1
MISSING_KEY_RC=$?
set -e
[[ "$WRONG_KEY_RC" -ne 0 ]] || die "wrong key unexpectedly restored a backup"
[[ "$MISSING_KEY_RC" -ne 0 ]] || die "missing key unexpectedly restored a backup"

cp -- "$RESULTS_DIR/backup-v1.dump.gpg" "$RESULTS_DIR/backup-v1.partial.dump.gpg"
truncate -s -32 "$RESULTS_DIR/backup-v1.partial.dump.gpg"
sha256sum "$RESULTS_DIR/backup-v1.partial.dump.gpg" >"$RESULTS_DIR/backup-v1.partial.dump.gpg.sha256"
set +e
run_phase restore "$KEY_DIR/recovery-v1.key" "$RESULTS_DIR" \
  "$RESULTS_DIR/backup-v1.partial.dump.gpg" >/dev/null 2>&1
PARTIAL_RC=$?
set -e
rm -f -- "$RESULTS_DIR/backup-v1.partial.dump.gpg" "$RESULTS_DIR/backup-v1.partial.dump.gpg.sha256"
[[ "$PARTIAL_RC" -ne 0 ]] || die "partial backup unexpectedly restored"

cp -- "$RESULTS_DIR/backup-v1.dump.gpg" "$RESULTS_DIR/backup-v1.tampered.dump.gpg"
printf '\377' | dd of="$RESULTS_DIR/backup-v1.tampered.dump.gpg" bs=1 seek=100 conv=notrunc status=none
sha256sum "$RESULTS_DIR/backup-v1.tampered.dump.gpg" >"$RESULTS_DIR/backup-v1.tampered.dump.gpg.sha256"
set +e
run_phase restore "$KEY_DIR/recovery-v1.key" "$RESULTS_DIR" \
  "$RESULTS_DIR/backup-v1.tampered.dump.gpg" >/dev/null 2>&1
TAMPER_RC=$?
set -e
rm -f -- "$RESULTS_DIR/backup-v1.tampered.dump.gpg" "$RESULTS_DIR/backup-v1.tampered.dump.gpg.sha256"
[[ "$TAMPER_RC" -ne 0 ]] || die "tampered backup unexpectedly restored"

ROTATE_EXPORT_START="$(date +%s%N)"
run_phase export "$KEY_DIR/recovery-v2.key" "$RESULTS_DIR" "$RESULTS_DIR/backup-v2.dump.gpg"
ROTATE_EXPORT_END="$(date +%s%N)"
run_phase restore "$KEY_DIR/recovery-v2.key" "$RESULTS_DIR" "$RESULTS_DIR/backup-v2.dump.gpg"
run_phase restore "$KEY_DIR/recovery-v1.key" "$RESULTS_DIR" "$RESULTS_DIR/backup-v1.dump.gpg"

EXPORT_MS=$(( (EXPORT_END - EXPORT_START) / 1000000 ))
RESTORE_MS=$(( (RESTORE_END - RESTORE_START) / 1000000 ))
ROTATE_EXPORT_MS=$(( (ROTATE_EXPORT_END - ROTATE_EXPORT_START) / 1000000 ))
TOTAL_MS=$(( ($(date +%s%N) - START_NS) / 1000000 ))
if find "$RESULTS_DIR" -maxdepth 1 -type f \( -name '*.part' -o -name '*.partial*' -o -name '*.tampered*' \) | grep -q .; then
  die "temporary or interrupted backup artifacts survived"
fi

cat >"$RESULTS_DIR/resilience-evidence.json" <<EOF
{
  "syntheticOnly": true,
  "authenticatedEncryption": "GnuPG symmetric AES256 with MDC integrity protection",
  "keyCustodyOutsideRepositoryAndDestination": true,
  "separateProcessRecovery": true,
  "wrongKeyRejected": true,
  "missingKeyRejected": true,
  "partialBackupRejected": true,
  "tamperedBackupRejected": true,
  "atomicPublicationVerified": true,
  "keyRotationVerified": true,
  "olderBackupRecoveredAfterRotation": true,
  "exportMilliseconds": $EXPORT_MS,
  "restoreMilliseconds": $RESTORE_MS,
  "rotationExportMilliseconds": $ROTATE_EXPORT_MS,
  "totalMilliseconds": $TOTAL_MS,
  "productionRpoOrRtoClaimed": false
}
EOF
rm -f -- "$RESULTS_DIR/backup-v1.partial.dump.gpg.sha256" "$RESULTS_DIR/backup-v1.tampered.dump.gpg.sha256"
echo "PASS: durable key custody, authenticated backup recovery, rotation, tamper/partial rejection and atomic cleanup"
echo "Evidence: $RESULTS_DIR/resilience-evidence.json"
