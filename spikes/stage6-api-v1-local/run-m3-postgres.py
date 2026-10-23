"""Guarded Fedora/rootless Podman M3 runner; no URL input, host mounts or shared DB."""
from pathlib import Path
import hashlib,json,os,subprocess,sys,tempfile,uuid,time,signal
ROOT=Path(__file__).resolve().parents[2];BASE=Path(__file__).resolve().parent
IMAGE='docker.io/library/postgres@sha256:e4cb93687b062cd25a0e8f672d85a7708235009d70d31f8a7698d9370092a9c4'
def sources():
 paths=[]
 for folder in ['stage6-api-v1-local','stage6-schema-pg','stage6-auth-local','stage6-handlers-local']:
  p=ROOT/'spikes'/folder
  paths += [x for sub in ['src','migrations'] for x in (p/sub).glob('*') if x.is_file()]
 paths+=list((BASE/'tests').glob('*.mjs'))+[Path(__file__).resolve(),BASE/'verify-m3.py']
 paths += [ROOT/'spikes/stage6-security-local/tests'/n for n in ['t22.test.mjs','t22-fixture.mjs']]
 paths += list((ROOT/'contracts/admin/v1/decision-proposal').glob('*.json'))
 paths += [ROOT/'spikes'/f/'package-lock.json' for f in ['stage6-schema-pg','stage6-auth-local']]
 return {str(p.relative_to(ROOT)):hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(paths)}
def main():
 def interrupted(_sig,_frame):raise KeyboardInterrupt('runner interrupted')
 signal.signal(signal.SIGTERM,interrupted)
 run=Path(tempfile.mkdtemp(prefix='pg-',dir=BASE/'results/m3'));name='goldberg-api-m3-'+uuid.uuid4().hex
 result={'container':name,'image':IMAGE,'syntheticOnly':True,'httpExecutions':0,'androidExecutions':0,'acceptance':'INTERRUPTED','created':False,'cleanup':'not-created','commands':[]}
 before=sources();(run/'source-before.json').write_text(json.dumps(before,indent=2)+'\n')
 def call(args,log,env=None,check=True):
  p=subprocess.run(args,cwd=ROOT,env=env,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True)
  (run/log).write_text(p.stdout);result['commands'].append({'argv':args,'exitCode':p.returncode,'log':log})
  if check and p.returncode:raise RuntimeError(log+' exit '+str(p.returncode))
  return p
 try:
  call(['podman','info','--format','{{.Host.Security.Rootless}}'],'podman.log')
  call(['podman','run','--detach','--pull=never','--name',name,'--label','goldberg.scope=local-synthetic-api-m3','--env','POSTGRES_USER=schema_prototype','--env','POSTGRES_DB=schema_prototype','--env','POSTGRES_HOST_AUTH_METHOD=trust','--publish','127.0.0.1::5432',IMAGE],'container.log')
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
  call(['node','--test','spikes/stage6-api-v1-local/tests/m3-postgres.test.mjs'],'postgres.tap',env=env)
  call(['python3',str(BASE/'verify-m3.py'),'--captures',str(run/'detail-captures.json')],'comparison-contract.log')
  call(['node','--test','spikes/stage6-api-v1-local/tests/m2-postgres.test.mjs'],'m2-postgres.tap',env=env)
  for command,log in [
   (['node','spikes/stage6-api-v1-local/tests/adapter.test.mjs'],'adapter.tap'),
   (['node','spikes/stage6-security-local/tests/t22.test.mjs'],'t22.tap'),
   (['python3','contracts/admin/v1/decision-proposal/verify.py'],'design.log'),
   (['python3','-m','unittest','discover','-s','contracts/admin/v1/decision-proposal','-p','test_*.py'],'design-tests.log'),
   (['python3','-c',"import sys;sys.path.insert(0,'spikes/stage6-security-local');from verify_contract import check;check(check_client_worktree=False)"],'original-contract-pinned.log'),
   (['python3','scripts/check_agent_policy.py'],'agent-policy.log'),
   (['git','diff','--check'],'whitespace.log')]:call(command,log)
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
