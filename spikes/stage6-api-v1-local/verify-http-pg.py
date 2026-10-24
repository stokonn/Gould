"""Source-bound real HTTP/PG evidence verification; old provenance remains immutable."""
from pathlib import Path
import hashlib,importlib.util,json,sys
ROOT=Path(__file__).resolve().parents[2];BASE=Path(__file__).resolve().parent
sys.path.insert(0,str(ROOT/'spikes/stage6-security-local'))
from tap_report import parse_tap

def captures(path):
 spec=importlib.util.spec_from_file_location('approved_dto',ROOT/'contracts/admin/v1/decision-proposal/verify.py')
 module=importlib.util.module_from_spec(spec);spec.loader.exec_module(module)
 definitions=json.loads((ROOT/'contracts/admin/v1/decision-proposal/dto.schema.json').read_text())['$defs']
 values=json.loads(path.read_text());assert values
 types=set()
 for entry in values:
  assert entry['dto'] in ['QueuePage','AuditPage','Stats']
  module.validate(entry['value'],definitions[entry['dto']],definitions,response=True);types.add(entry['dto'])
 assert types=={'QueuePage','AuditPage','Stats'}
 print(f'PASS: {len(values)} actual HTTP-to-PostgreSQL read DTOs match the approved schema (queue/audit/stats).')

def verify(run):
 assert run.resolve().parent==(BASE/'results/http-pg').resolve()
 result=json.loads((run/'result.json').read_text());assert result['acceptance']=='PASS'
 assert result['cleanup']=='removed-and-absence-verified' and result['sourceUnchanged']
 assert result.get('volumes') and all(x==1 for x in result['volumeAbsenceExits'])
 assert result['httpExecutions']=='real-loopback-to-PG17; see http-postgres.tap' and result['androidExecutions']==0
 before=json.loads((run/'source-before.json').read_text());assert before==json.loads((run/'source-after.json').read_text())
 for name,digest in before.items():assert hashlib.sha256((ROOT/name).read_bytes()).hexdigest()==digest,'tested source changed: '+name
 for name,digest in result['artifacts'].items():assert hashlib.sha256((run/name).read_bytes()).hexdigest()==digest,'artifact changed: '+name
 for name,n in [('http-postgres.tap',24),('postgres.tap',16),('m2-postgres.tap',19),('api.tap',117),('t22.tap',38),('auth.tap',50),('schema.tap',34),('handlers.tap',87),('operations-pg.tap',34),('operations-unit.tap',32)]:
  stats=parse_tap(run/name,n,True);assert stats['pass']==n
 assert 'Ran 18 tests' in (run/'design-tests.log').read_text() and '\nOK\n' in (run/'design-tests.log').read_text()
 assert 'Pinned client provenance only' in (run/'original-contract-pinned.log').read_text()
 assert all(c['exitCode']==0 for c in result['commands'] if not c['log'].startswith(('ready-','cleanup-probe','cleanup-volume')))
 captures(run/'http-read-captures.json')
 print('PASS: real HTTP/PG and independent PostgreSQL/scripted/unit suites; source/raw hashes and exact container/volume absence verified. Local-only; no Android/production/API freeze acceptance.')
if __name__=='__main__':
 if len(sys.argv)==3 and sys.argv[1]=='--captures':captures(Path(sys.argv[2]))
 elif len(sys.argv)==2:verify(Path(sys.argv[1]))
 else:raise SystemExit('usage: verify-http-pg.py RUN_DIRECTORY | --captures FILE')
