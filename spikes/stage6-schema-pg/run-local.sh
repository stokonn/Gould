#!/usr/bin/env bash
# Disposable local PG17 only. Never accepts a database URL or mounts host data.
set -euo pipefail
cd -- "$(dirname -- "$0")"
image='docker.io/library/postgres@sha256:e4cb93687b062cd25a0e8f672d85a7708235009d70d31f8a7698d9370092a9c4'
container="goldberg-schema-prototype-$$"
created=false
cleanup() {
  if [[ "$created" == true ]]; then podman rm --force --time 0 "$container" >/dev/null; fi
}
trap cleanup EXIT
[[ -f node_modules/pg/package.json ]] || { echo 'Run npm ci --ignore-scripts --no-audit --no-fund first.' >&2; exit 1; }
podman run --detach --rm --pull=never --name "$container" --label goldberg.scope=local-synthetic-stage6 \
  --env POSTGRES_USER=schema_prototype --env POSTGRES_DB=schema_prototype \
  --env POSTGRES_HOST_AUTH_METHOD=trust --publish 127.0.0.1::5432 "$image" >/dev/null
created=true
binding="$(podman port "$container" 5432/tcp)"
[[ "$binding" == 127.0.0.1:* ]] || { echo 'Refusing non-loopback binding' >&2; exit 1; }
export SCHEMA_PG_PORT="${binding##*:}"
ready=false
for attempt in {1..40}; do
  if podman exec "$container" pg_isready -h 127.0.0.1 -U schema_prototype -d schema_prototype >/dev/null 2>&1; then ready=true; break; fi
  sleep 0.2
done
[[ "$ready" == true ]] || { echo 'Local PostgreSQL did not become ready' >&2; exit 1; }
node --input-type=module - <<'JS'
import {writeFile} from 'node:fs/promises';
import {localClient} from './src/database.mjs';
const c=localClient();await c.connect();
const {rows:[row]}=await c.query('SELECT version() AS postgres_version');await c.end();
await writeFile('results/environment.json',JSON.stringify({node:process.version,driver:'pg 8.23.1',...row,
 image:'docker.io/library/postgres@sha256:e4cb93687b062cd25a0e8f672d85a7708235009d70d31f8a7698d9370092a9c4',
 host:'127.0.0.1',port:Number(process.env.SCHEMA_PG_PORT),syntheticOnly:true,cloudAccess:false},null,2)+'\n');
JS
npm test
