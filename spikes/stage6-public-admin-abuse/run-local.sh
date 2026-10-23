#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "$0")"
results="$(mktemp -d results/run-XXXXXXXX)"
# Fail before launching PostgreSQL or registering test-hook failures if the
# execution environment cannot bind loopback sockets. No remote service is used.
python3 - > "$results/preflight.json" <<'PYPROBE'
import socket,json,sys
try:
 with socket.socket(socket.AF_INET,socket.SOCK_STREAM) as probe:
  probe.bind(('127.0.0.1',0))
  probe.listen(1)
 print(json.dumps({'localTcp':'available','testBodiesExecuted':0}))
except OSError as error:
 print(json.dumps({'localTcp':'blocked','errno':error.errno,'error':error.strerror,'testBodiesExecuted':0}))
 sys.exit(78)
PYPROBE
image='docker.io/library/postgres@sha256:e4cb93687b062cd25a0e8f672d85a7708235009d70d31f8a7698d9370092a9c4'
container="goldberg-public-admin-abuse-$$"
created=false
cleanup(){ if [[ "$created" == true ]]; then podman rm --force --time 0 "$container" >> "$results/cleanup.log" 2>&1; fi; }
trap cleanup EXIT
podman run --detach --rm --pull=never --name "$container" --label goldberg.scope=local-synthetic-stage6-679 --env POSTGRES_USER=schema_prototype --env POSTGRES_DB=schema_prototype --env POSTGRES_HOST_AUTH_METHOD=trust --publish 127.0.0.1::5432 "$image" > "$results/container.log" 2>&1
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
podman exec "$container" postgres --version > "$results/postgres-version.log"
node --version > "$results/node-version.log"
failed=0
# Direct execution preserves each node:test result even in environments whose
# isolated child test runner reports a file wrapper without its subtest results.
for suite in unit postgres; do
 if node "tests/$suite.test.mjs" > "$results/$suite.tap" 2>&1; then :; else failed=1; fi
 cat "$results/$suite.tap"
done
for module in handlers-local auth-local schema-pg; do
 case "$module" in handlers-local) suite=handlers;; auth-local) suite=auth;; schema-pg) suite=schema;; esac
 if node "../stage6-$module/tests/$suite.test.mjs" > "$results/$suite-regression.tap" 2>&1; then :; else failed=1; fi
 cat "$results/$suite-regression.tap"
done
for check in check_agent_policy check_backup_fixtures check_history_paging check_localization check_migrations; do
 if python3 "../../scripts/$check.py" > "$results/$check.log" 2>&1; then :; else failed=1; fi
 cat "$results/$check.log"
done
exit "$failed"
