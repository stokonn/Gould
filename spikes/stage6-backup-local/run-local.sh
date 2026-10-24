#!/usr/bin/env bash
set -Eeuo pipefail

# Disposable local PostgreSQL 17 only. No database URL, host port, or host
# data mount is accepted. All fixture data is synthetic.
cd -- "$(dirname -- "$0")"
umask 077

RUNTIME="${RUNTIME:-podman}"
IMAGE='docker.io/library/postgres:17'
CONTAINER="goldberg-stage6-backup-$$"
RESULTS_DIR="${BACKUP_RESULTS_DIR:-}"
PRIVATE_RESULTS=false
CREATED=false
START_NS="$(date +%s%N)"
KEY_FILE=''
PLAIN_DUMP=''
DECRYPTED_DUMP=''
SOURCE_SCHEMA=''
RESTORE_SCHEMA=''
ENCRYPTED_DUMP=''
MANIFEST=''

die() {
  echo "FAIL: $*" >&2
  exit 1
}

cleanup() {
  local rc=$?
  set +e
  [[ -n "$KEY_FILE" ]] && rm -f -- "$KEY_FILE"
  [[ -n "$PLAIN_DUMP" ]] && rm -f -- "$PLAIN_DUMP"
  [[ -n "$DECRYPTED_DUMP" ]] && rm -f -- "$DECRYPTED_DUMP"
  [[ -n "$SOURCE_SCHEMA" ]] && rm -f -- "$SOURCE_SCHEMA"
  [[ -n "$RESTORE_SCHEMA" ]] && rm -f -- "$RESTORE_SCHEMA"
  if [[ "$CREATED" == true ]]; then
    "$RUNTIME" rm -f "$CONTAINER" >/dev/null 2>&1
  fi
  if [[ "$PRIVATE_RESULTS" == true && -n "$RESULTS_DIR" ]]; then
    rm -rf -- "$RESULTS_DIR"
  fi
  if [[ "$rc" -ne 0 ]]; then
    echo "FAIL: backup harness exited with code $rc" >&2
  fi
  exit "$rc"
}
trap cleanup EXIT

command -v "$RUNTIME" >/dev/null 2>&1 || die "Podman is unavailable"
command -v openssl >/dev/null 2>&1 || die "OpenSSL is unavailable"
command -v sha256sum >/dev/null 2>&1 || die "sha256sum is unavailable"
"$RUNTIME" image inspect "$IMAGE" >/dev/null 2>&1 ||
  die "required cached image $IMAGE is unavailable; refusing an implicit pull"

if [[ -z "$RESULTS_DIR" ]]; then
  RESULTS_DIR="$(mktemp -d "${TMPDIR:-/tmp}/goldberg-stage6-backup.XXXXXX")"
  PRIVATE_RESULTS=true
else
  mkdir -p -- "$RESULTS_DIR"
  [[ "$RESULTS_DIR" = /* ]] || die "BACKUP_RESULTS_DIR must be absolute"
fi

KEY_FILE="$(mktemp "${TMPDIR:-/tmp}/goldberg-stage6-key.XXXXXX")"
openssl rand -hex 32 >"$KEY_FILE"
chmod 600 "$KEY_FILE"
PLAIN_DUMP="$(mktemp "${TMPDIR:-/tmp}/goldberg-stage6-plain.XXXXXX")"
DECRYPTED_DUMP="$(mktemp "${TMPDIR:-/tmp}/goldberg-stage6-decrypted.XXXXXX")"
SOURCE_SCHEMA="$(mktemp "${TMPDIR:-/tmp}/goldberg-stage6-source-schema.XXXXXX")"
RESTORE_SCHEMA="$(mktemp "${TMPDIR:-/tmp}/goldberg-stage6-restore-schema.XXXXXX")"
ENCRYPTED_DUMP="$RESULTS_DIR/synthetic-backup.dump.enc"
MANIFEST="$RESULTS_DIR/SHA256SUMS"

"$RUNTIME" run --pull=never --detach --rm --name "$CONTAINER" \
  --label goldberg.scope=local-synthetic-stage6 \
  --env POSTGRES_PASSWORD=synthetic-disposable-only \
  --env POSTGRES_DB=postgres "$IMAGE" >/dev/null
CREATED=true

READY=false
for _ in $(seq 1 40); do
  if "$RUNTIME" exec "$CONTAINER" pg_isready -U postgres -d postgres >/dev/null 2>&1; then
    READY=true
    break
  fi
  sleep 0.25
done
[[ "$READY" == true ]] || die "PostgreSQL did not become ready"

"$RUNTIME" exec -i "$CONTAINER" psql -X -v ON_ERROR_STOP=1 -U postgres -d postgres \
  <sql/01_fixture.sql

EXPORT_START="$(date +%s%N)"
"$RUNTIME" exec "$CONTAINER" pg_dump -U postgres -d postgres --format=custom \
  --file=/tmp/synthetic-backup.dump
"$RUNTIME" cp "$CONTAINER:/tmp/synthetic-backup.dump" "$PLAIN_DUMP"
openssl enc -aes-256-cbc -pbkdf2 -iter 200000 -salt \
  -in "$PLAIN_DUMP" -out "$ENCRYPTED_DUMP" -pass "file:$KEY_FILE"
rm -f -- "$PLAIN_DUMP"
PLAIN_DUMP=''
(cd "$RESULTS_DIR" && sha256sum "$(basename "$ENCRYPTED_DUMP")") >"$MANIFEST"
EXPORT_END="$(date +%s%N)"

TAMPERED="$RESULTS_DIR/synthetic-backup.tampered.enc"
cp -- "$ENCRYPTED_DUMP" "$TAMPERED"
printf '\001' | dd of="$TAMPERED" bs=1 seek=32 conv=notrunc status=none
TAMPERED_MANIFEST="$RESULTS_DIR/tampered.SHA256SUMS"
sed "s#synthetic-backup.dump.enc#synthetic-backup.tampered.enc#" \
  "$MANIFEST" >"$TAMPERED_MANIFEST"
if (cd "$RESULTS_DIR" && sha256sum --check "$(basename "$TAMPERED_MANIFEST")" >/dev/null 2>&1); then
  die "tampered backup unexpectedly passed manifest verification"
fi
rm -f -- "$TAMPERED" "$TAMPERED_MANIFEST"
(cd "$RESULTS_DIR" && sha256sum --check "$MANIFEST" >/dev/null)

RESTORE_START="$(date +%s%N)"
openssl enc -d -aes-256-cbc -pbkdf2 -iter 200000 \
  -in "$ENCRYPTED_DUMP" -out "$DECRYPTED_DUMP" -pass "file:$KEY_FILE"
"$RUNTIME" cp "$DECRYPTED_DUMP" "$CONTAINER:/tmp/synthetic-backup.restore.dump"
"$RUNTIME" exec "$CONTAINER" createdb -U postgres restored_synthetic
"$RUNTIME" exec "$CONTAINER" pg_restore -U postgres -d restored_synthetic \
  --exit-on-error --no-owner /tmp/synthetic-backup.restore.dump
"$RUNTIME" exec "$CONTAINER" pg_dump -U postgres -d postgres --schema-only \
  --no-owner --no-privileges |
  sed '/^\\restrict /d; /^\\unrestrict /d' >"$SOURCE_SCHEMA"
"$RUNTIME" exec "$CONTAINER" pg_dump -U postgres -d restored_synthetic --schema-only \
  --no-owner --no-privileges |
  sed '/^\\restrict /d; /^\\unrestrict /d' >"$RESTORE_SCHEMA"
if ! diff -u "$SOURCE_SCHEMA" "$RESTORE_SCHEMA" >/dev/null; then
  echo "FAIL: restored schema dump differs from source" >&2
  diff -u "$SOURCE_SCHEMA" "$RESTORE_SCHEMA" >&2 || true
  die "schema consistency check failed"
fi
"$RUNTIME" exec -i "$CONTAINER" psql -X -v ON_ERROR_STOP=1 \
  -U postgres -d restored_synthetic <sql/02_verify_restore.sql
RESTORE_END="$(date +%s%N)"

[[ ! -e "$PLAIN_DUMP" ]] || die "plaintext dump survived cleanup"
rm -f -- "$DECRYPTED_DUMP" "$SOURCE_SCHEMA" "$RESTORE_SCHEMA"
DECRYPTED_DUMP=''
SOURCE_SCHEMA=''
RESTORE_SCHEMA=''

EXPORT_MS=$(( (EXPORT_END - EXPORT_START) / 1000000 ))
RESTORE_MS=$(( (RESTORE_END - RESTORE_START) / 1000000 ))
TOTAL_MS=$(( ($(date +%s%N) - START_NS) / 1000000 ))
cat >"$RESULTS_DIR/evidence.json" <<EOF
{
  "syntheticOnly": true,
  "image": "$IMAGE",
  "exportMilliseconds": $EXPORT_MS,
  "restoreMilliseconds": $RESTORE_MS,
  "totalMilliseconds": $TOTAL_MS,
  "encryptedArtifact": "$(basename "$ENCRYPTED_DUMP")",
  "manifest": "$(basename "$MANIFEST")",
  "plaintextRemovedBeforeSuccess": true,
  "tamperManifestRejected": true,
  "schemaDumpMatched": true,
  "auditChainVerified": true,
  "productionRpoOrRtoClaimed": false
}
EOF
echo "PASS: synthetic encrypted backup, manifest, tamper rejection, restore, schema and audit chain"
echo "Evidence: $RESULTS_DIR/evidence.json"
if [[ "$PRIVATE_RESULTS" == true ]]; then
  echo "Private temporary evidence will be removed on exit."
fi
