#!/usr/bin/env bash
# Version 2. Local synthetic verification only. No installs, downloads or hosted configuration.
set -euo pipefail
cd -- "$(dirname -- "$0")/../.."
results="$(mktemp -d docs/planning/goldberg-0.11/stage-6/results/security-local/run-XXXXXXXX)"
printf 'Evidence directory: %s\n' "$results"
python3 - "$results" <<'PY'
from pathlib import Path
import hashlib,json,subprocess,sys
out=Path(sys.argv[1]);files=[]
for name in ['stage6-auth-local','stage6-handlers-local','stage6-schema-pg','stage6-public-admin-abuse','stage6-security-local']:
 for p in (Path('spikes')/name).rglob('*'):
  if p.is_file() and not any(part in ['node_modules','results'] for part in p.parts):files.append(p)
files+=list(Path('scripts').glob('check_*.py'))
(out/'source-sha256.json').write_text(json.dumps({str(p):hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(set(files))},indent=2)+'\n')
(out/'execution.json').write_text(json.dumps({'runnerVersion':2,'head':subprocess.check_output(['git','rev-parse','HEAD'],text=True).strip(),'branch':subprocess.check_output(['git','branch','--show-current'],text=True).strip(),'scope':'local synthetic; no emulator or external network'},indent=2)+'\n')
PY
node --version > "$results/node-version.log"
failed=0
if node spikes/stage6-security-local/tests/security.test.mjs > "$results/focused.tap" 2>&1; then :; else failed=1; fi
if node spikes/stage6-public-admin-abuse/tests/unit.test.mjs > "$results/unit.tap" 2>&1; then :; else failed=1; fi
for check in check_agent_policy check_backup_fixtures check_history_paging check_localization check_migrations; do
 if python3 "scripts/$check.py" > "$results/$check.log" 2>&1; then :; else failed=1; fi
done
blocked=false
if python3 - > "$results/preflight.json" <<'PY'
import socket,json,sys
try:
 with socket.socket(socket.AF_INET,socket.SOCK_STREAM) as probe:
  probe.bind(('127.0.0.1',0));probe.listen(1)
 print(json.dumps({'localTcp':'available','databaseTestBodiesExecuted':0}))
except OSError as error:
 print(json.dumps({'localTcp':'blocked','errno':error.errno,'error':error.strerror,'databaseTestBodiesExecuted':0}));sys.exit(78)
PY
then
 # Existing version-pinned disposable PG17 runner, unchanged; never points at a hosted DB.
 if bash spikes/stage6-public-admin-abuse/run-local.sh > "$results/postgres-runner.log" 2>&1; then pg_exit=0; else pg_exit=$?; failed=1; fi
 printf '%s\n' "$pg_exit" > "$results/postgres-runner.exit"
else blocked=true
fi
if python3 spikes/stage6-security-local/tap_report.py "$results" "$failed" "$blocked"; then :; else failed=1; fi
if [[ "$failed" != 0 ]]; then exit 1; fi
if [[ "$blocked" == true ]]; then exit 78; fi
