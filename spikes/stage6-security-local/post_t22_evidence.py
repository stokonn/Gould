"""Verify post-T22 Fedora v3 execution; no network/database/emulator operations."""
from pathlib import Path
import hashlib,json,re,subprocess
from tap_report import parse_tap
TESTED='6d0abad3decf0b724c0e00f1b4e270bf8d244fba'
WRAPPER='docs/planning/goldberg-0.11/stage-6/results/security-local/run-x0nDSTXd'
NESTED='spikes/stage6-public-admin-abuse/results/run-TEmZzZQg'
CHECKS=['check_agent_policy','check_backup_fixtures','check_history_paging','check_localization','check_migrations']
# Only evidence/index/documentation tooling may differ from this verified run.
ALLOWED_CURRENT_EDITS={f'spikes/stage6-security-local/{p}' for p in ['build-matrix.py','publish-host.py','verified_evidence.py','README.md']}
ALLOWED_CURRENT_EDITS|={f'contracts/admin/v1/{p}' for p in ['source-provenance.json','readiness.json','README.md']}
def require(condition,message):
 if not condition:raise ValueError(message)
def digest(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def read_runs(wrapper,nested):
 wrapper=Path(wrapper);nested=Path(nested);result=json.loads((wrapper/'result.json').read_text());execution=json.loads((wrapper/'execution.json').read_text())
 require(execution['head']==TESTED and execution['runnerVersion']==3,'wrong tested revision/runner')
 require(result['summaryErrors']==[] and result['checksOrTestsFailed'] is False and result['postgresBlocked'] is False and result['sourceUnchanged'] is True,'outer runner unsuccessful')
 require((wrapper/'postgres-runner.exit').read_text().strip()=='0','nested runner nonzero/missing exit status')
 counts={}
 for label,folder,suites in [('outer',wrapper,{'focused.tap':27,'unit.tap':32,'t22.tap':38,'contract.tap':11}),('nested',nested,{'auth-regression.tap':50,'handlers-regression.tap':87,'schema-regression.tap':34,'postgres.tap':34,'unit.tap':32})]:
  counts[label]={name:parse_tap(folder/name,expected,True) for name,expected in suites.items()}
  if label=='outer':require(result['counts']==counts[label],'outer result/TAP mismatch')
  require(json.loads((folder/'preflight.json').read_text())['localTcp']=='available','host preflight did not pass')
  for name in CHECKS:
   log=(folder/(name+'.log')).read_text();require(log.startswith('PASS') and not re.search(r'^FAIL|Traceback',log,re.M),'host check failed: '+name)
 sequence=['unit.tap','postgres.tap','handlers-regression.tap','auth-regression.tap','schema-regression.tap']+[p+'.log' for p in CHECKS]
 require((wrapper/'postgres-runner.log').read_text()==''.join((nested/p).read_text() for p in sequence),'nested stdout linkage mismatch')
 names=lambda p:re.findall(r'^ok \d+ - (.+)$',p.read_text(),re.M)
 require(names(wrapper/'unit.tap')==names(nested/'unit.tap'),'repeated unit suite differs')
 require((nested/'postgres-version.log').read_text().strip()=='postgres (PostgreSQL) 17.11','wrong PostgreSQL version')
 require(bool(re.fullmatch('[0-9a-f]{64}\\n?',(nested/'container.log').read_text())),'missing container ID')
 removed=(nested/'cleanup.log').read_text().strip();require(bool(re.fullmatch('goldberg-public-admin-abuse-[0-9]+',removed)),'missing successful removal output')
 # Require exact named executed rows that contain the inspected physical assertions.
 required={
  'auth-regression.tap':['T22 admin cannot self-grant; operator capability/role stays outside application roles','T9/T22 public database role has no private-table or privileged-function access','T9/T22 local transport exposes no audit mutation or arbitrary RPC, even to admin','self-grant and self-suspension cannot bypass guards using uppercase UUID spellings'],
  'handlers-regression.tap':['T18 immutable publication/audit/review/feed; exported chain tamper detected; no audit mutation adapter'],
  'postgres.tap':['admin audit paginates numerically with hash head and has no mutation endpoint','role/suspension integration reuses live Auth, denies escalation, self grant and revoked replay'],
  'schema-regression.tap':['immutable revision, published revision, review, audit and feed reject UPDATE/DELETE/TRUNCATE']}
 for file,tests in required.items():require(set(tests)<=set(names(nested/file)),'required T22 body not executed: '+file)
 total=sum(c['pass'] for suites in counts.values() for c in suites.values());unique=total-counts['nested']['unit.tap']['pass'];database=sum(c['pass'] for name,c in counts['nested'].items() if name!='unit.tap')
 require((total,unique,database)==(345,313,205),'unexpected execution totals')
 return {'counts':counts,'successfulExecutions':total,'testsCountingRepeated32SuiteOnce':unique,'databaseBackedBodies':database,'nestedExit':0,'removedContainer':removed,'launchedContainerId':(nested/'container.log').read_text().strip(),'T22':'LOCAL_PASS under explicit owner-approved clarification; no HTTP/hosted claim','overall':'PARTIAL; other local/integration gates OPEN','requiredPhysicalTests':required}
def verify(root):
 root=Path(root);base=root/'docs/planning/goldberg-0.11/stage-6/results/security-local'
 for name,h in json.loads((base/'post-t22-run-artifacts-sha256.json').read_text()).items():require(digest(root/name)==h,'immutable raw artifact mismatch: '+name)
 wrapper=root/WRAPPER;nested=root/NESTED;manifest=json.loads((wrapper/'source-sha256.json').read_text());require(len(manifest)==82,'expected82 at-run source hashes')
 edits=[]
 for name,h in manifest.items():
  original=subprocess.check_output(['git','show',f'{TESTED}:{name}'],cwd=root);require(hashlib.sha256(original).hexdigest()==h,'at-run source/commit mismatch: '+name)
  if digest(root/name)!=h:require(name in ALLOWED_CURRENT_EDITS,'tested service/fixture changed: '+name);edits.append(name)
 result=read_runs(wrapper,nested);result.update({'testedHead':TESTED,'sourceHashes':len(manifest),'currentEvidenceToolEdits':sorted(edits),'cleanupQualification':'Removal stdout and nested exit0 verified; not a new live Podman inventory check'})
 return result
if __name__=='__main__':print(json.dumps(verify(Path(__file__).resolve().parents[2]),indent=2))
