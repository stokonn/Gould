"""Only new synthetic probe-role checks. No accepted test bodies are rerun."""
from pathlib import Path
import subprocess,uuid,json,hashlib,signal,time,sys
BASE=Path(__file__).resolve().parent
ROOT=BASE.parents[1]
IMAGE='docker.io/library/postgres@sha256:e4cb93687b062cd25a0e8f672d85a7708235009d70d31f8a7698d9370092a9c4'
name='goldberg-private-probe-'+uuid.uuid4().hex
out=BASE/'results'/name;out.mkdir(parents=True)
sources=[Path(__file__).resolve(),BASE/'probe-role.candidate.sql',BASE/'disable-probe.candidate.sql']
sources+=[ROOT/'spikes/stage6-cloud-readiness'/n for n in ['acl-fix.candidate.sql','private-schema.candidate.sql']]
before={str(p.relative_to(ROOT)):hashlib.sha256(p.read_bytes()).hexdigest() for p in sources}
created=False;volumes=[];checks=[];commands=[];status='FAILED'
def call(args,content=None,ok=True,save=True):
 p=subprocess.run(args,input=content,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=45)
 log=f'{len(commands):03}.log'
 (out/log).write_text(p.stdout if save else 'Synthetic dump bytes='+str(len(p.stdout.encode()))+' SHA256='+hashlib.sha256(p.stdout.encode()).hexdigest()+'\n')
 commands.append({'argv':args,'exitCode':p.returncode,'log':log})
 if ok and p.returncode:raise RuntimeError(log+':'+p.stdout)
 return p
def sql(s,user='postgres',db='postgres',ok=True):
 return call(['podman','exec','-i',name,'psql','-X','-At','-v','ON_ERROR_STOP=1','-U',user,'-d',db],s,ok)
def check(label,s,expected=None,error=None,user='postgres',db='postgres'):
 p=sql(s,user,db,ok=error is None)
 if error:assert p.returncode!=0 and error in p.stdout,(label,p.stdout)
 if expected:assert p.stdout.strip().endswith(expected),(label,p.stdout)
 checks.append(label)
def interrupted(_s,_f):raise KeyboardInterrupt('interrupted')
signal.signal(signal.SIGTERM,interrupted)
try:
 assert call(['podman','info','--format','{{.Host.Security.Rootless}}']).stdout.strip()=='true'
 call(['podman','run','--detach','--pull=never','--network=none','--name',name,'--label','goldberg.scope=private-probe-synthetic','--env','POSTGRES_HOST_AUTH_METHOD=trust',IMAGE]);created=True
 mounts=json.loads(call(['podman','inspect',name,'--format','{{json .Mounts}}']).stdout);volumes=[m['Name'] for m in mounts if m['Type']=='volume']
 for _ in range(60):
  if call(['podman','exec',name,'pg_isready','-U','postgres'],ok=False).returncode==0:break
  time.sleep(.2)
 else:raise RuntimeError('not ready')
 # Setup only: reused accepted SQL/ACL candidates are not counted as new tests.
 sql("""CREATE ROLE anon;CREATE ROLE authenticated;CREATE ROLE service_role BYPASSRLS;
 CREATE FUNCTION public.rls_auto_enable() RETURNS event_trigger LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$BEGIN RETURN;END;$$;
 CREATE EVENT TRIGGER ensure_rls ON ddl_command_end WHEN TAG IN ('CREATE TABLE','CREATE TABLE AS','SELECT INTO') EXECUTE FUNCTION public.rls_auto_enable();""")
 sql((ROOT/'spikes/stage6-cloud-readiness/acl-fix.candidate.sql').read_text())
 sql((ROOT/'spikes/stage6-cloud-readiness/private-schema.candidate.sql').read_text())
 candidate=(BASE/'probe-role.candidate.sql').read_text()
 check('role/table transactional rollback',candidate.replace('COMMIT;','ROLLBACK;')+"SELECT NOT EXISTS(SELECT 1 FROM pg_roles WHERE rolname='goldberg_staging_probe') AND to_regclass('goldberg_private.staging_probe') IS NULL;",'t')
 check('probe access apply',candidate)
 check('role flags and bounded connection count',"SELECT rolcanlogin AND NOT rolinherit AND NOT rolsuper AND NOT rolcreaterole AND NOT rolcreatedb AND NOT rolreplication AND NOT rolbypassrls AND rolconnlimit=2 FROM pg_roles WHERE rolname='goldberg_staging_probe';",'t')
 check('actual synthetic login sees exactly probe marker',"SELECT id=1 AND marker='synthetic-only' FROM goldberg_private.staging_probe;",'t',user='goldberg_staging_probe')
 check('application tables not granted',"SELECT * FROM goldberg_private.principals;",error='permission denied for table principals',user='goldberg_staging_probe')
 check('application security-definer execution denied',"SELECT goldberg_private.append_audit(NULL,NULL,NULL,NULL,NULL,NULL);",error='permission denied for function append_audit',user='goldberg_staging_probe')
 for label,statement in [('insert','INSERT INTO goldberg_private.staging_probe VALUES(1,\'synthetic-only\');'),('update',"UPDATE goldberg_private.staging_probe SET marker='synthetic-only';"),('delete','DELETE FROM goldberg_private.staging_probe;')]:
  check('probe '+label+' denied',statement,error='permission denied for table staging_probe',user='goldberg_staging_probe')
 check('actual login statement timeout',"SELECT pg_sleep(5);",error='canceling statement due to statement timeout',user='goldberg_staging_probe')
 for role in ['anon','authenticated','service_role']:
  check(role+' probe denied','SET ROLE '+role+'; SELECT * FROM goldberg_private.staging_probe;',error='permission denied for schema goldberg_private')
 check('reapply refuses existing role without replacing data',candidate,error='Existing probe role: preserve and review')
 dump=call(['podman','exec',name,'pg_dump','-U','postgres','-d','postgres','--schema=goldberg_private','--no-owner','--no-privileges'],save=False).stdout
 sql('CREATE DATABASE probe_restore;');sql(dump,db='probe_restore')
 sql((ROOT/'spikes/stage6-cloud-readiness/private-schema.candidate.sql').read_text(),db='probe_restore')
 check('restore denied before explicit probe ACL',"SELECT * FROM goldberg_private.staging_probe;",error='permission denied for schema goldberg_private',user='goldberg_staging_probe',db='probe_restore')
 sql('GRANT USAGE ON SCHEMA goldberg_private TO goldberg_staging_probe;GRANT SELECT ON goldberg_private.staging_probe TO goldberg_staging_probe;',db='probe_restore')
 check('isolated restored fixture with reviewed minimal grants',"SELECT id=1 AND marker='synthetic-only' FROM goldberg_private.staging_probe;",'t',user='goldberg_staging_probe',db='probe_restore')
 check('disable containment preserves fixture',(BASE/'disable-probe.candidate.sql').read_text()+"SELECT count(*)=1 FROM goldberg_private.staging_probe;",'t')
 check('disabled role cannot login',"SELECT 1;",error='is not permitted to log in',user='goldberg_staging_probe')
 status='PASS'
except (Exception,KeyboardInterrupt) as e:(out/'error.log').write_text(str(e))
finally:
 cleanup='not-created'
 if created:
  p=call(['podman','rm','--force','--volumes','--time','0',name],ok=False)
  absent=call(['podman','container','exists',name],ok=False).returncode==1
  no_volumes=all(call(['podman','volume','exists',v],ok=False).returncode==1 for v in volumes)
  cleanup='removed-and-absence-verified' if p.returncode==0 and absent and no_volumes else 'FAILED'
 unchanged=before=={str(p.relative_to(ROOT)):hashlib.sha256(p.read_bytes()).hexdigest() for p in sources}
 result={'status':status,'count':len(checks),'checks':checks,'cleanup':cleanup,'sourceUnchanged':unchanged,'sources':before,'commands':commands,'syntheticOnly':True,'remoteWrites':0,'artifacts':{p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in out.iterdir() if p.is_file()}}
 (out/'result.json').write_text(json.dumps(result,indent=2)+'\n');print(json.dumps({'path':str(out.relative_to(ROOT)),'status':status,'count':len(checks),'cleanup':cleanup,'sourceUnchanged':unchanged}))
sys.exit(0 if status=='PASS' and cleanup=='removed-and-absence-verified' and unchanged else 1)
