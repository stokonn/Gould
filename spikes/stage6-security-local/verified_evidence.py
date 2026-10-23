"""Validate immutable owner host evidence; current tooling edits are separately identified."""
from pathlib import Path
import hashlib,json,re,subprocess
from tap_report import parse_tap
TESTED='faf9ba3ec24e7098289c80d6e119519ccc21b95e'
WRAPPER='docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5'
NESTED='spikes/stage6-public-admin-abuse/results/run-qx9TUTDk'
CHECKS=['check_agent_policy','check_backup_fixtures','check_history_paging','check_localization','check_migrations']
TOOL_EDITS={f'spikes/stage6-security-local/{name}' for name in ['run-local.sh','build-matrix.py','publish-host.py','README.md']}
def require(condition,message):
 if not condition:raise ValueError(message)
def digest(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def verify_host_evidence(root):
 root=Path(root);base=root/'docs/planning/goldberg-0.11/stage-6/results/security-local';wrapper=root/WRAPPER;nested=root/NESTED
 # Current coverage/index supersede older versions; their original hashes stay verifiable in Git.
 for name,sha in json.loads((base/'artifact-sha256.json').read_text()).items():
  if digest(base/name)!=sha:
   require(name in {'coverage.json','test-inventory.json'},'original evidence modified: '+name)
   original=subprocess.check_output(['git','show',f'{TESTED}:{base.relative_to(root)}/{name}'],cwd=root)
   require(hashlib.sha256(original).hexdigest()==sha,'original index hash differs: '+name)
 for name,sha in json.loads((base/'host-run-artifacts-sha256.json').read_text()).items():require(digest(root/name)==sha,'immutable host artifact mismatch: '+name)
 execution=json.loads((wrapper/'execution.json').read_text());require(execution['head']==TESTED and execution['runnerVersion']==1,'unexpected tested head/runner')
 manifest=json.loads((wrapper/'source-sha256.json').read_text());require(len(manifest)==62,'expected62 at-run source hashes')
 edits=[]
 for name,sha in manifest.items():
  if digest(root/name)!=sha:
   require(name in TOOL_EDITS,'tested application/service source changed: '+name)
   original=subprocess.check_output(['git','show',f'{TESTED}:{name}'],cwd=root)
   require(hashlib.sha256(original).hexdigest()==sha,'at-run source does not match tested commit: '+name);edits.append(name)
 result=json.loads((wrapper/'result.json').read_text())
 require(result['checksOrTestsFailed'] is False and result['postgresBlocked'] is False and result['sourceUnchanged'] is True,'host wrapper did not succeed')
 counts={};names=[]
 for folder,suites in [(wrapper,{'focused.tap':27,'unit.tap':32}),(nested,{'auth-regression.tap':50,'handlers-regression.tap':87,'schema-regression.tap':34,'postgres.tap':34,'unit.tap':32})]:
  for name,expected in suites.items():
   path=folder/name;counts[str(path.relative_to(root))]=parse_tap(path,expected,True)
   if folder==wrapper:require(result['counts'][name]==counts[str(path.relative_to(root))],'wrapper result/TAP counts differ')
  for check in CHECKS:
   text=(folder/(check+'.log')).read_text();require(text.startswith('PASS') and not re.search(r'^FAIL|Traceback',text,re.M),'host check failed: '+check)
  require(json.loads((folder/'preflight.json').read_text())['localTcp']=='available','host preflight unavailable')
 wrapper_names=re.findall(r'^ok \d+ - (.+)$',(wrapper/'unit.tap').read_text(),re.M)
 nested_names=re.findall(r'^ok \d+ - (.+)$',(nested/'unit.tap').read_text(),re.M)
 require(wrapper_names==nested_names,'repeated unit suite differs')
 sequence=['unit.tap','postgres.tap','handlers-regression.tap','auth-regression.tap','schema-regression.tap']+[c+'.log' for c in CHECKS]
 require((wrapper/'postgres-runner.log').read_text()==''.join((nested/p).read_text() for p in sequence),'nested runner stdout linkage mismatch')
 require((nested/'postgres-version.log').read_text().strip()=='postgres (PostgreSQL) 17.11','unexpected PostgreSQL version')
 require(bool(re.fullmatch(r'[0-9a-f]{64}\n?',(nested/'container.log').read_text())),'container ID absent')
 require(bool(re.fullmatch(r'goldberg-public-admin-abuse-\d+\n?',(nested/'cleanup.log').read_text())),'cleanup removal output absent')
 tool_log=(base/'runner-v2/unittest.log').read_text()
 require(bool(re.search(r'^Ran 10 tests in ',tool_log,re.M)) and bool(re.search(r'^OK$',tool_log,re.M)) and len(re.findall(r'^test_.* \.\.\. ok$',tool_log,re.M))==10,'reporting regression evidence differs')
 total=sum(c['pass'] for c in counts.values());require(total==296,'expected296 successful executions')
 return {'counts':counts,'successfulExecutions':total,'testsCountingRepeatedSuiteOnce':total-32,'testedHead':TESTED,'runnerVersion':1,'sourceHashes':len(manifest),'currentToolingEdits':sorted(edits),'overall':'PARTIAL; T22 FAIL and other local/hosted gaps remain OPEN','exitEvidence':'v1 successful wrapper result/control flow and owner report; numeric exit sidecar absent','cleanupEvidence':'successful removal stdout; live agent inventory blocked by read-only Podman runtime'}
if __name__=='__main__':
 print(json.dumps(verify_host_evidence(Path(__file__).resolve().parents[2]),indent=2))
