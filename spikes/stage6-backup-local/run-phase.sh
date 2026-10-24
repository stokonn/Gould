#!/usr/bin/env bash
set -Eeuo pipefail

cd -- "$(dirname -- "$0")"
umask 077
MODE="${1:?mode}"
KEY_FILE="${2:?key file}"
RESULTS_DIR="${3:?results directory}"
ARTIFACT="${4:?artifact}"
RUNTIME="${RUNTIME:-podman}"
IMAGE='docker.io/library/postgres:17'
CONTAINER="goldberg-stage6-backup-phase-$$"
CREATED=false
TMP_DUMP="$(mktemp "${TMPDIR:-/tmp}/goldberg-stage6-phase.XXXXXX")"
TMP_RESTORE="$(mktemp "${TMPDIR:-/tmp}/goldberg-stage6-restore.XXXXXX")"
TMP_SCHEMA="$(mktemp "${TMPDIR:-/tmp}/goldberg-stage6-schema.XXXXXX")"
TMP_RESTORED_SCHEMA="$(mktemp "${TMPDIR:-/tmp}/goldberg-stage6-restored-schema.XXXXXX")"
GNUPGHOME_DIR="$(mktemp -d "${TMPDIR:-/tmp}/goldberg-stage6-gnupg.XXXXXX")"
PART=""
MANIFEST_TMP=""
chmod 700 "$GNUPGHOME_DIR"
export GNUPGHOME="$GNUPGHOME_DIR"

cleanup() {
  local rc=$?
  set +e
  rm -f -- "$TMP_DUMP" "$TMP_RESTORE" "$TMP_SCHEMA" "$TMP_RESTORED_SCHEMA"
  [[ -n "$PART" ]] && rm -f -- "$PART"
  [[ -n "$MANIFEST_TMP" ]] && rm -f -- "$MANIFEST_TMP"
  rm -rf -- "$GNUPGHOME_DIR"
  [[ "$CREATED" == true ]] && "$RUNTIME" rm -f "$CONTAINER" >/dev/null 2>&1
  exit "$rc"
}
trap cleanup EXIT

[[ -f "$KEY_FILE" ]] || { echo "missing recovery key" >&2; exit 2; }
[[ "$(stat -c '%a' "$KEY_FILE")" = 600 ]] || { echo "unsafe key permissions" >&2; exit 2; }
MANIFEST="$ARTIFACT.sha256"
if [[ "$MODE" = restore ]]; then
  [[ -f "$ARTIFACT" ]] || { echo "missing backup artifact" >&2; exit 2; }
  [[ -f "$MANIFEST" ]] || { echo "missing backup manifest" >&2; exit 2; }
  (cd "$RESULTS_DIR" && sha256sum --check "$(basename "$MANIFEST")" >/dev/null)
fi

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
[[ "$READY" == true ]] || { echo "PostgreSQL did not become ready" >&2; exit 1; }

if [[ "$MODE" = export ]]; then
  "$RUNTIME" exec -i "$CONTAINER" psql -X -v ON_ERROR_STOP=1 -U postgres -d postgres \
    <sql/01_fixture.sql
  "$RUNTIME" exec "$CONTAINER" pg_dump -U postgres -d postgres --format=custom \
    --file=/tmp/synthetic-backup.dump
  "$RUNTIME" cp "$CONTAINER:/tmp/synthetic-backup.dump" "$TMP_DUMP"
  PART="${ARTIFACT}.part.$$"
  rm -f -- "$PART"
  gpg --batch --yes --no-symkey-cache --pinentry-mode loopback \
    --passphrase-file "$KEY_FILE" --symmetric --cipher-algo AES256 \
    --output "$PART" "$TMP_DUMP"
  [[ -s "$PART" ]] || { echo "encrypted temporary artifact is empty" >&2; exit 1; }
  mv -f -- "$PART" "$ARTIFACT"
  MANIFEST_TMP="$MANIFEST.tmp.$$"
  (cd "$RESULTS_DIR" && sha256sum "$(basename "$ARTIFACT")") >"$MANIFEST_TMP"
  mv -f -- "$MANIFEST_TMP" "$MANIFEST"
  echo "PASS: atomic encrypted export $(basename "$ARTIFACT")"
elif [[ "$MODE" = restore ]]; then
  "$RUNTIME" exec -i "$CONTAINER" psql -X -v ON_ERROR_STOP=1 -U postgres -d postgres \
    <sql/01_fixture.sql
  gpg --batch --quiet --no-symkey-cache --pinentry-mode loopback \
    --passphrase-file "$KEY_FILE" --decrypt "$ARTIFACT" >"$TMP_RESTORE"
  "$RUNTIME" cp "$TMP_RESTORE" "$CONTAINER:/tmp/synthetic-backup.restore.dump"
  "$RUNTIME" exec "$CONTAINER" createdb -U postgres restored_synthetic
  "$RUNTIME" exec "$CONTAINER" pg_restore -U postgres -d restored_synthetic \
    --exit-on-error --no-owner /tmp/synthetic-backup.restore.dump
  "$RUNTIME" exec "$CONTAINER" pg_dump -U postgres -d postgres --schema-only \
    --no-owner --no-privileges |
    sed '/^\\restrict /d; /^\\unrestrict /d' >"$TMP_SCHEMA"
  "$RUNTIME" exec "$CONTAINER" pg_dump -U postgres -d restored_synthetic --schema-only \
    --no-owner --no-privileges |
    sed '/^\\restrict /d; /^\\unrestrict /d' >"$TMP_RESTORED_SCHEMA"
  diff -u "$TMP_SCHEMA" "$TMP_RESTORED_SCHEMA" >/dev/null
  "$RUNTIME" exec -i "$CONTAINER" psql -X -v ON_ERROR_STOP=1 \
    -U postgres -d restored_synthetic <sql/02_verify_restore.sql
  echo "PASS: restored $(basename "$ARTIFACT")"
else
  echo "unknown mode" >&2
  exit 2
fi
