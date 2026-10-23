"""Read-only verification of exact M1 source, raw executed TAP and immutable baseline."""
from pathlib import Path
import hashlib,json,subprocess,sys
ROOT=Path(__file__).resolve().parents[2];BASE=Path(__file__).resolve().parent
sys.path.insert(0,str(ROOT/'spikes/stage6-security-local'))
from tap_report import parse_tap
BASELINE='6dd3136269ec7f5932c8c4b3689fb065d4db9105'

def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def check():
 run=ROOT/json.loads((BASE/'results/LATEST.json').read_text())['run']
 assert run.parent==BASE/'results' and run.name.startswith('run-')
 result=json.loads((run/'result.json').read_text());assert result['sourceUnchanged'] is True
 before=json.loads((run/'source-before.json').read_text());after=json.loads((run/'source-after.json').read_text());assert before==after
 for name,digest in before.items():assert sha(ROOT/name)==digest,'tested source changed: '+name
 assert all(c['exitCode']==0 for c in result['commands'])
 assert result['httpRequestsExecuted']==result['androidBodiesExecuted']==result['postgres']['databaseBodiesExecuted']==0
 assert result['prior345Rerun'] is False
 assert parse_tap(run/'adapter.tap',106,True)['pass']==106
 assert parse_tap(run/'t22.tap',38,True)['pass']==38
 assert 'Ran 18 tests' in (run/'design-tests.log').read_text() and '\nOK\n' in (run/'design-tests.log').read_text()
 assert 'PASS:' in (run/'original-contract.log').read_text() and 'PASS:' in (run/'agent-policy.log').read_text()
 # Existing services, Android, old contracts and immutable evidence must be byte-unchanged.
 protected=['app','scripts','contracts/admin/v1','spikes/stage6-auth-local','spikes/stage6-handlers-local','spikes/stage6-schema-pg','spikes/stage6-public-admin-abuse','spikes/stage6-security-local','docs/planning/goldberg-0.11/stage-6/results']
 assert not subprocess.check_output(['git','diff','--name-only',BASELINE,'--',*protected],cwd=ROOT,text=True).strip()
 artifact_manifest=json.loads((BASE/'results/artifact-sha256.json').read_text())
 for name,digest in artifact_manifest.items():assert sha(BASE/'results'/name)==digest,'raw artifact changed: '+name
 names=[line for line in (run/'adapter.tap').read_text().splitlines() if line.startswith('ok ')]
 assert sum('actual adapter fixture request:' in line for line in names)==27
 assert sum('approved protocol expectation ' in line for line in names)==8
 print('PASS:106 adapter/wire tests +38 relevant T22 regressions;18 planning-verifier tests; source-before/after/current and raw hashes; old services/Android/contracts/evidence unchanged. PG/HTTP/Android bodies0; version/integration gates OPEN.')
 return run
if __name__=='__main__':check()
