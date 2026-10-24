"""Source-bound real HTTP/PG evidence verification; old provenance remains immutable."""
from pathlib import Path
import hashlib,importlib.util,json,sys,subprocess,re
from current_provenance import source_files, CUR, sha, tap_suites
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

def verify(run,live_cleanup=True):
 run=run.resolve()
 assert run.resolve().parent==(BASE/'results/http-pg').resolve()
 result=json.loads((run/'result.json').read_text());assert result['acceptance']=='PASS'
 assert result['cleanup']=='removed-and-absence-verified' and result['sourceUnchanged']
 assert result.get('volumes') and all(x==1 for x in result['volumeAbsenceExits'])
 assert len(result['volumes'])==len(result['volumeAbsenceExits'])
 assert result['httpExecutions']=='real-loopback-to-PG17; see http-postgres.tap' and result['androidExecutions']==0
 before=json.loads((run/'source-before.json').read_text());assert before==json.loads((run/'source-after.json').read_text())
 current=source_files();current[str(CUR.relative_to(ROOT))]=sha(CUR)
 assert before==current, 'source inventory/hash mismatch'
 for name,digest in before.items():assert hashlib.sha256((ROOT/name).read_bytes()).hexdigest()==digest,'tested source changed: '+name
 assert {'source-before.json','source-after.json'} <= result['artifacts'].keys()
 assert all(c['log'] in result['artifacts'] for c in result['commands'])
 for name,digest in result['artifacts'].items():assert hashlib.sha256((run/name).read_bytes()).hexdigest()==digest,'artifact changed: '+name
 suites=tap_suites();assert result['suiteSources']==suites
 counts={}
 for name,files in suites.items():
  stats=parse_tap(run/name,require_success=True);assert stats['tests']>0
  raw=(run/name).read_text()
  assert len(re.findall(r'^1\.\.'+str(stats['tests'])+r'$',raw,re.M))==1
  assert not any(line.startswith('# Subtest: ') and line.endswith('.test.mjs') for line in raw.splitlines())
  assert any(c['log']==name and c['exitCode']==0 and c['argv']==['node','--test','--test-reporter=tap',*files] for c in result['commands'])
  counts[name]=stats
 assert result['testCounts']==counts, 'reported counts do not match executed TAP'
 assert result['startingBranch']=='feature/goldberg-0.11-stage-6-api-v1-local' and re.fullmatch('[0-9a-f]{40}',result['startingCommit'])
 expected_checks={
  'consumer-source-before.log':['python3',str(BASE/'consumer-compatibility.py')],
  'consumer-source-after.log':['python3',str(BASE/'consumer-compatibility.py')],
  'consumer-replay.log':['python3',str(BASE/'consumer-compatibility.py'),'--replay',str(run/'shared-contract-captures.json'),str(run/'consumer-results.json')],
  'shared-contract-verifier.log':['python3',str(BASE/'verify-shared-contract.py'),str(run)],
  'http-dto-contract.log':['python3',str(BASE/'verify-http-pg.py'),'--captures',str(run/'http-read-captures.json')],
  'comparison-contract.log':['python3',str(BASE/'verify-m3.py'),'--captures',str(run/'detail-captures.json')],
  'current-provenance-before.log':['python3',str(BASE/'verify-current-provenance.py')],
  'current-provenance.log':['python3',str(BASE/'verify-current-provenance.py')],
  'current-dto-schema.log':['python3',str(BASE/'validate-current-corpus.py')],
  'historical-design-tests.log':['python3',str(BASE/'verify-historical-provenance.py')],
  'agent-policy.log':['python3','scripts/check_agent_policy.py'],
  'historical-security-evidence.log':['python3',str(BASE/'verify-historical-provenance.py'),'--security'],
  'tap-parser-tests.log':['python3','-m','unittest','discover','-s','spikes/stage6-security-local/tests','-p','test_tap_report.py'],
  'backup.log':['python3','scripts/check_backup_fixtures.py'],
  'acl.log':['python3','scripts/check_stage6_acl_proposal.py'],
  'whitespace.log':['git','diff','--check'],
 }
 for log,argv in expected_checks.items():
  assert any(c['log']==log and c['argv']==argv and c['exitCode']==0 for c in result['commands']), 'missing successful check: '+log
  output=(run/log).read_text()
  if log in ['tap-parser-tests.log','historical-security-evidence.log']:assert re.search(r'Ran [1-9][0-9]* tests',output) and '\nOK\n' in output
  else:assert 'PASS' in output or log=='whitespace.log'
 historical=(run/'historical-design-tests.log').read_text()
 metadata=json.loads((ROOT/'contracts/admin/v1/decision-proposal/metadata.json').read_text())
 assert f'Ran {metadata["verifierTests"]} tests' in historical and '\nOK\n' in historical
 assert 'original Git source' in historical and 'Pinned client provenance only' in historical
 expected_failure='historical-pin-current-source-EXPECTED-FAIL.log'
 assert result['historicalCurrentFilePin'].startswith('EXPECTED_FAILURE;')
 assert any(c['log']==expected_failure and c['exitCode']!=0 for c in result['commands'])
 assert 'immutable evidence changed: spikes/stage6-handlers-local/src/' in (run/expected_failure).read_text()
 for c in result['commands']:
  if c['log'].startswith(('ready-','cleanup-probe','cleanup-volume')) or c['log']==expected_failure:continue
  assert c['exitCode']==0, c['log']
 # The independent verifier reruns exact resource probes; no list-wide cleanup.
 if live_cleanup:
  assert re.fullmatch(r'goldberg-api-http-pg-[0-9a-f]{32}',result['container'])
  assert subprocess.run(['podman','container','exists',result['container']]).returncode==1
  for volume in result['volumes']:
   assert re.fullmatch(r'[0-9a-f]{64}',volume)
   assert subprocess.run(['podman','volume','exists',volume]).returncode==1
  print('PASS: independent live container/volume absence probes.')
 print('Executed TAP counts: '+json.dumps({name:c['tests'] for name,c in counts.items()},sort_keys=True))
 subprocess.run(['python3',str(BASE/'verify-shared-contract.py'),str(run)],check=True)
 captures(run/'http-read-captures.json')
 print('PASS: real HTTP/PG and independent PostgreSQL/scripted/unit suites; source/raw hashes and recorded container/volume absence verified. Local-only; no Android/production/API freeze acceptance.')
if __name__=='__main__':
 if len(sys.argv)==3 and sys.argv[1]=='--captures':captures(Path(sys.argv[2]))
 elif len(sys.argv)==2:verify(Path(sys.argv[1]))
 elif len(sys.argv)==3 and sys.argv[1]=='--recorded-only':verify(Path(sys.argv[2]),live_cleanup=False)
 else:raise SystemExit('usage: verify-http-pg.py RUN_DIRECTORY | --captures FILE')
