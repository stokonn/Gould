"""Guarded Fedora/rootless Podman HTTP/PG integration runner; no URL input, host mounts or shared DB."""
from pathlib import Path
import hashlib,json,os,subprocess,sys,tempfile,uuid,time,signal
ROOT=Path(__file__).resolve().parents[2];BASE=Path(__file__).resolve().parent
IMAGE='docker.io/library/postgres:17'
from current_provenance import source_files, CUR, sha, tap_suites

def sources():
 files=source_files()
 files[str(CUR.relative_to(ROOT))]=sha(CUR)
 return dict(sorted(files.items()))
def main():
 def interrupted(_sig,_frame):raise KeyboardInterrupt('runner interrupted')
 signal.signal(signal.SIGTERM,interrupted)
 run=Path(tempfile.mkdtemp(prefix='pg-',dir=BASE/'results/http-pg'));name='goldberg-api-http-pg-'+uuid.uuid4().hex
 result={'container':name,'image':IMAGE,'syntheticOnly':True,'httpExecutions':0,'androidExecutions':0,'acceptance':'INTERRUPTED','created':False,'cleanup':'not-created','commands':[],'startingCommit':subprocess.check_output(['git','rev-parse','HEAD'],cwd=ROOT,text=True).strip(),'startingBranch':subprocess.check_output(['git','branch','--show-current'],cwd=ROOT,text=True).strip()}
 before=sources();(run/'source-before.json').write_text(json.dumps(before,indent=2)+'\n')
 def call(args,log,env=None,check=True):
  p=subprocess.run(args,cwd=ROOT,env=env,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True)
  (run/log).write_text(p.stdout);result['commands'].append({'argv':args,'exitCode':p.returncode,'log':log})
  if check and p.returncode:raise RuntimeError(log+' exit '+str(p.returncode))
  return p
 try:
  call(['python3',str(BASE/'verify-current-provenance.py')],'current-provenance-before.log')
  call(['python3',str(BASE/'consumer-compatibility.py')],'consumer-source-before.log')
  rootless=call(['podman','info','--format','{{.Host.Security.Rootless}}'],'podman.log').stdout.strip()
  if rootless!='true':raise RuntimeError('rootless Podman required')
  result['imageIdentity']=json.loads(call(['podman','image','inspect',IMAGE,'--format','{{json .}}'],'image.json').stdout)['Id']
  call(['podman','run','--detach','--pull=never','--name',name,'--label','goldberg.scope=local-synthetic-api-http-pg','--env','POSTGRES_USER=schema_prototype','--env','POSTGRES_DB=schema_prototype','--env','POSTGRES_HOST_AUTH_METHOD=trust','--publish','127.0.0.1::5432',result['imageIdentity']],'container.log')
  result['created']=True
  mounts=json.loads(call(['podman','inspect',name,'--format','{{json .Mounts}}'],'mounts.json').stdout)
  result['volumes']=[m['Name'] for m in mounts if m['Type']=='volume']
  binding=call(['podman','port',name,'5432/tcp'],'binding.log').stdout.strip()
  if not binding.startswith('127.0.0.1:') or '\n' in binding:raise RuntimeError('non-loopback binding')
  port=int(binding.split(':')[1]);assert 1024<=port<=65535
  for i in range(60):
   p=call(['podman','exec',name,'pg_isready','-h','127.0.0.1','-U','schema_prototype','-d','schema_prototype'],f'ready-{i:02}.log',check=False)
   if p.returncode==0:break
   time.sleep(.2)
  else:raise RuntimeError('not ready')
  call(['podman','exec',name,'psql','-U','schema_prototype','-d','schema_prototype','-Atc','SELECT version()'],'postgres-version.log')
  call(['node','--version'],'node-version.log')
  env=os.environ.copy();env['SCHEMA_PG_PORT']=str(port);env['M3_COMPARISON_PATH']=str(run/'detail-captures.json')
  result['suiteSources']=tap_suites()
  result['testCounts']={}
  sys.path.insert(0,str(ROOT/'spikes/stage6-security-local'))
  from tap_report import parse_tap
  for log,files in result['suiteSources'].items():
   suite_env=env.copy()
   if log=='shared-contract.tap':suite_env['SHARED_CONTRACT_CAPTURES']=str(run/'shared-contract-captures.json')
   if log=='query-budget.tap':suite_env['QUERY_PLAN_CAPTURES']=str(run/'query-plans.json')
   if log=='http-postgres.tap':suite_env['HTTP_PG_CAPTURES']=str(run/'http-read-captures.json')
   call(['node','--test','--test-reporter=tap',*files],log,env=suite_env)
   # These suites are flat: independently reconcile body count, plan and summary.
   counts=parse_tap(run/log,require_success=True)
   raw=(run/log).read_text()
   assert raw.count('\n1..'+str(counts['tests'])+'\n')==1, 'missing/duplicate TAP plan'
   assert counts['tests']>0 and not any(line.startswith('# Subtest: ') and line.endswith('.test.mjs') for line in raw.splitlines()), 'opaque file-level TAP is not executed body evidence'
   result['testCounts'][log]=counts
   if log=='http-postgres.tap':
    result['httpExecutions']='real-loopback-to-PG17; see http-postgres.tap'
    call(['python3',str(BASE/'verify-http-pg.py'),'--captures',str(run/'http-read-captures.json')],'http-dto-contract.log')
  call(['python3',str(BASE/'consumer-compatibility.py'),'--replay',str(run/'shared-contract-captures.json'),str(run/'consumer-results.json')],'consumer-replay.log')
  call(['python3',str(BASE/'verify-shared-contract.py'),str(run)],'shared-contract-verifier.log')
  call(['python3',str(BASE/'consumer-compatibility.py')],'consumer-source-after.log')
  call(['python3',str(BASE/'verify-m3.py'),'--captures',str(run/'detail-captures.json')],'comparison-contract.log')
  for command,log in [
   (['python3',str(BASE/'verify-current-provenance.py')],'current-provenance.log'),
   (['python3',str(BASE/'validate-current-corpus.py')],'current-dto-schema.log'),
   (['python3',str(BASE/'verify-historical-provenance.py')],'historical-design-tests.log'),
   (['python3','scripts/check_agent_policy.py'],'agent-policy.log'),
   (['python3',str(BASE/'verify-historical-provenance.py'),'--security'],'historical-security-evidence.log'),
   (['python3','-m','unittest','discover','-s','spikes/stage6-security-local/tests','-p','test_tap_report.py'],'tap-parser-tests.log'),
   (['python3','scripts/check_backup_fixtures.py'],'backup.log'),
   (['python3','scripts/check_stage6_acl_proposal.py'],'acl.log'),
   (['git','diff','--check'],'whitespace.log')]:call(command,log)
  # Preserve the strict current-file rejection as such. It is never a historical
  # PASS; historical acceptance above ran the unchanged suite on original blobs.
  historical=call(['python3','contracts/admin/v1/decision-proposal/verify.py'],'historical-pin-current-source-EXPECTED-FAIL.log',check=False)
  assert historical.returncode!=0 and 'immutable evidence changed: spikes/stage6-handlers-local/src/' in historical.stdout
  result['historicalCurrentFilePin']='EXPECTED_FAILURE; evolved handler bytes do not match historical pin'
  result['acceptance']='PASS'
 except (Exception,KeyboardInterrupt) as e:result['acceptance']='BLOCKED_OR_FAILED';result['error']=str(e)
 finally:
  if result['created']:
   p=call(['podman','rm','--force','--volumes','--time','0',name],'cleanup.log',check=False)
   probe=call(['podman','container','exists',name],'cleanup-probe.log',check=False)
   volumes=[call(['podman','volume','exists',v],f'cleanup-volume-{i}.log',check=False).returncode for i,v in enumerate(result.get('volumes',[]))]
   result['cleanup']='removed-and-absence-verified' if p.returncode==0 and probe.returncode==1 and all(code==1 for code in volumes) else 'FAILED'
   result['volumeAbsenceExits']=volumes
  after=sources();result['sourceUnchanged']=before==after
  (run/'source-after.json').write_text(json.dumps(after,indent=2)+'\n')
  result['artifacts']={p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(run.iterdir()) if p.is_file()}
  (run/'result.json').write_text(json.dumps(result,indent=2)+'\n')
  print(json.dumps({'run':str(run.relative_to(ROOT)), 'acceptance':result['acceptance'], 'cleanup':result['cleanup'], 'sourceUnchanged':result['sourceUnchanged'], 'error':result.get('error')},indent=2))
 return 0 if result['acceptance']=='PASS' and result['sourceUnchanged'] and result['cleanup']=='removed-and-absence-verified' else 1
if __name__=='__main__':sys.exit(main())
