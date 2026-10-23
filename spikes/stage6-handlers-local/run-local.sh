#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "$0")"
image='docker.io/library/postgres@sha256:e4cb93687b062cd25a0e8f672d85a7708235009d70d31f8a7698d9370092a9c4'
container="goldberg-handlers-prototype-$$"
created=false
cleanup(){ if [[ "$created" == true ]]; then podman rm --force --time 0 "$container" >/dev/null; fi; }
trap cleanup EXIT
podman run --detach --rm --pull=never --name "$container" --label goldberg.scope=local-synthetic-stage6-handlers --env POSTGRES_USER=schema_prototype --env POSTGRES_DB=schema_prototype --env POSTGRES_HOST_AUTH_METHOD=trust --publish 127.0.0.1::5432 "$image" >/dev/null
created=true
binding="$(podman port "$container" 5432/tcp)"
[[ "$binding" == 127.0.0.1:* ]] || exit 1
export SCHEMA_PG_PORT="${binding##*:}"
ready=false
for attempt in {1..40}; do
 if podman exec "$container" pg_isready -h 127.0.0.1 -U schema_prototype -d schema_prototype >/dev/null 2>&1; then ready=true; break; fi
 sleep 0.2
done
[[ "$ready" == true ]] || exit 1
node --input-type=module - <<'JS'
import {writeFile} from 'node:fs/promises';import {localClient} from '../stage6-schema-pg/src/database.mjs';
const c=localClient();await c.connect();const {rows:[v]}=await c.query('SELECT version() AS postgres');await c.end();
await writeFile('results/environment.json',JSON.stringify({node:process.version,...v,port:Number(process.env.SCHEMA_PG_PORT),syntheticOnly:true,cloud:false},null,2)+'\n');
JS
npm test | tee results/final.tap
npm test --prefix ../stage6-auth-local > results/auth-regression.tap 2>&1
cat results/auth-regression.tap
npm test --prefix ../stage6-schema-pg > results/schema-regression.tap 2>&1
cat results/schema-regression.tap
