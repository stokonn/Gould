"""M2 evidence/current source check; M1 verifier/evidence remain immutable historical records."""
from pathlib import Path
import hashlib,importlib.util,json,subprocess,sys
ROOT=Path(__file__).resolve().parents[2];BASE=Path(__file__).resolve().parent
sys.path.insert(0,str(ROOT/'spikes/stage6-security-local'))
from tap_report import parse_tap

def captures(path):
 spec=importlib.util.spec_from_file_location('approved_dto',ROOT/'contracts/admin/v1/decision-proposal/verify.py')
 module=importlib.util.module_from_spec(spec);spec.loader.exec_module(module)
 definitions=json.loads((ROOT/'contracts/admin/v1/decision-proposal/dto.schema.json').read_text())['$defs']
 values=json.loads(path.read_text());assert values
 for value in values:module.validate(value,definitions['Detail'],definitions)
 print(f'PASS: {len(values)} actual PostgreSQL-backed Detail responses match approved exact DTO schema; synthetic content only.')

def verify(run):
 assert run.resolve().parent==(BASE/'results/m2').resolve()
 result=json.loads((run/'result.json').read_text());assert result['acceptance']=='PASS'
 assert result['cleanup']=='removed-and-absence-verified' and result['sourceUnchanged']
 assert result.get('volumes') and all(x==1 for x in result['volumeAbsenceExits'])
 assert result['httpExecutions']==result['androidExecutions']==0
 before=json.loads((run/'source-before.json').read_text());assert before==json.loads((run/'source-after.json').read_text())
 for name,digest in before.items():assert hashlib.sha256((ROOT/name).read_bytes()).hexdigest()==digest,'tested source changed: '+name
 for name,digest in result['artifacts'].items():assert hashlib.sha256((run/name).read_bytes()).hexdigest()==digest,'artifact changed: '+name
 for name,n in [('postgres.tap',19),('adapter.tap',106),('t22.tap',38)]:
  stats=parse_tap(run/name,n,True);assert stats['pass']==n
 assert 'Ran 18 tests' in (run/'design-tests.log').read_text() and '\nOK\n' in (run/'design-tests.log').read_text()
 assert 'Pinned client provenance only' in (run/'original-contract-pinned.log').read_text()
 assert 'PASS:' in (run/'agent-policy.log').read_text()
 protected=['app','scripts','contracts/admin/v1','spikes/stage6-auth-local','spikes/stage6-handlers-local','spikes/stage6-schema-pg','spikes/stage6-public-admin-abuse','spikes/stage6-security-local','docs/planning/goldberg-0.11/stage-6/results']
 assert not subprocess.check_output(['git','diff','0782e22b17b55fe9850934feafca4af391a508d2','--name-only','--',*protected],cwd=ROOT,text=True).strip()
 captures(run/'detail-captures.json')
 print('PASS:19 actual PostgreSQL tests,106 adapter/wire +38 T22 scripted/unit regressions,18 planning tests; zero failures/skips; current source/raw hashes/cleanup/protected baseline verified. No HTTP/Android/full-int8-write/production compatibility acceptance.')
if __name__=='__main__':
 if len(sys.argv)==3 and sys.argv[1]=='--captures':captures(Path(sys.argv[2]))
 elif len(sys.argv)==2:verify(Path(sys.argv[1]))
 else:raise SystemExit('usage: verify-m2.py RUN_DIRECTORY | --captures FILE')
