"""Disposable PG17 integration, exact source receipts, no hosted credentials."""
from pathlib import Path
import hashlib,json,os,signal,subprocess,sys,time,uuid
ROOT=Path(__file__).resolve().parents[2]
sys.path.insert(0,str(ROOT/'spikes/stage6-api-v1-local'))
from current_provenance import source_files,tap_suites

def main(client_worktree=None,client_only=False):
    run=ROOT/'services/community-bff/results'/('local-'+uuid.uuid4().hex)
    run.mkdir(parents=True)
    name='goldberg-final-bff-'+uuid.uuid4().hex
    result={'syntheticOnly':True,'container':name,'acceptance':'FAILED','commands':[]}
    def sources():
        files=source_files()
        for p in (ROOT/'services/community-bff').rglob('*'):
            if p.is_file() and 'results' not in p.parts and 'node_modules' not in p.parts:
                files[str(p.relative_to(ROOT))]=hashlib.sha256(p.read_bytes()).hexdigest()
        if client_worktree:
            for p in (client_worktree/'app/src').rglob('*'):
                if p.is_file():files['integration/'+str(p.relative_to(client_worktree))]=hashlib.sha256(p.read_bytes()).hexdigest()
            files['integration/app/build.gradle.kts']=hashlib.sha256((client_worktree/'app/build.gradle.kts').read_bytes()).hexdigest()
        return files
    before=sources();created=False
    def call(args,log,env=None,check=True,cwd=ROOT):
        p=subprocess.run(args,cwd=cwd,env=env,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True)
        (run/log).write_text(p.stdout);result['commands'].append({'argv':args,'exitCode':p.returncode,'log':log})
        if check and p.returncode:raise RuntimeError(log+' failed')
        return p
    def interrupted(*_):raise KeyboardInterrupt()
    signal.signal(signal.SIGTERM,interrupted)
    try:
        assert call(['podman','info','--format','{{.Host.Security.Rootless}}'],'rootless.log').stdout.strip()=='true'
        image=call(['podman','image','inspect','docker.io/library/postgres:17','--format','{{.Id}}'],'image.log').stdout.strip()
        result['image']=image
        call(['podman','run','-d','--pull=never','--name',name,'--label','goldberg.scope=final-local-bff','-e','POSTGRES_USER=schema_prototype','-e','POSTGRES_DB=schema_prototype','-e','POSTGRES_HOST_AUTH_METHOD=trust','-p','127.0.0.1::5432',image],'container.log');created=True
        result['volumes']=[m['Name'] for m in json.loads(call(['podman','inspect',name,'--format','{{json .Mounts}}'],'mounts.json').stdout) if m['Type']=='volume']
        binding=call(['podman','port',name,'5432/tcp'],'binding.log').stdout.strip();assert binding.startswith('127.0.0.1:') and '\n' not in binding
        env=os.environ.copy();env['SCHEMA_PG_PORT']=binding.split(':')[1]
        for i in range(60):
            if call(['podman','exec',name,'pg_isready','-U','schema_prototype'],f'ready-{i}.log',check=False).returncode==0:break
            time.sleep(.2)
        else:raise RuntimeError('PG not ready')
        suites={'fetch.tap':['services/community-bff/tests/fetch.test.mjs','services/community-bff/tests/identity.test.mjs'],'bff-postgres.tap':['services/community-bff/tests/postgres.test.mjs'],'workerd-postgres.tap':['services/community-bff/tests/runtime.test.mjs'],**tap_suites()}
        if client_only:suites={}
        result['counts']={}
        sys.path.insert(0,str(ROOT/'spikes/stage6-security-local'))
        from tap_report import parse_tap
        for log,files in suites.items():
            call(['node','--test','--test-reporter=tap',*files],log,env)
            result['counts'][log]=parse_tap(run/log,require_success=True)
        if client_worktree:
            env['GOLDBERG_BFF_SOURCE']=str(ROOT)
            env['ANDROID_HOME']=env.get('ANDROID_HOME') or env.get('ANDROID_SDK_ROOT') or str(Path.home()/'Android/Sdk')
            # The BFF lives outside this Gradle project's inputs. Cached test
            # XML can refer to a different server, so force actual execution.
            call(['./gradlew','testDebugUnitTest','--rerun-tasks','-PgoldbergCanonicalIntegration=true','--tests','com.opus.piano.data.community.canonical.CanonicalBffHttpIntegrationTest','--no-daemon','--max-workers=1'],'kotlin-bff-http.log',env,cwd=client_worktree)
            import xml.etree.ElementTree as ET
            xml=client_worktree/'app/build/test-results/testDebugUnitTest/TEST-com.opus.piano.data.community.canonical.CanonicalBffHttpIntegrationTest.xml'
            data=xml.read_bytes();(run/'kotlin-bff-http.xml').write_bytes(data)
            counts={k:int(ET.fromstring(data).get(k,0)) for k in ['tests','failures','errors','skipped']}
            assert counts=={'tests':1,'failures':0,'errors':0,'skipped':0},counts
            result['kotlinIntegration']=counts
        result['acceptance']='PASS'
    except (Exception,KeyboardInterrupt) as e:result['error']=str(e)
    finally:
        if created:
            cleanup=call(['podman','rm','-f','-v','--time','0',name],'cleanup.log',check=False)
            absent=call(['podman','container','exists',name],'absent.log',check=False)
            volumes=[call(['podman','volume','exists',v],f'volume-{i}.log',check=False).returncode for i,v in enumerate(result.get('volumes',[]))]
            result['cleanup']=cleanup.returncode==0 and absent.returncode==1 and all(x==1 for x in volumes)
        result['sourceUnchanged']=before==sources();result['sourceHashes']=before
        result['artifacts']={p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in run.iterdir() if p.is_file()}
        (run/'result.json').write_text(json.dumps(result,indent=2)+'\n')
        print(json.dumps({'run':str(run.relative_to(ROOT)),**{k:v for k,v in result.items() if k in ['acceptance','counts','cleanup','sourceUnchanged','error']}},indent=2))
    return 0 if result['acceptance']=='PASS' and result.get('cleanup') and result['sourceUnchanged'] else 1
if __name__=='__main__':
    import argparse
    parser=argparse.ArgumentParser();parser.add_argument('--client-worktree',type=Path);parser.add_argument('--client-only',action='store_true');args=parser.parse_args()
    if args.client_worktree:args.client_worktree=args.client_worktree.resolve()
    if args.client_only and not args.client_worktree:parser.error('--client-only requires --client-worktree')
    sys.exit(main(args.client_worktree,args.client_only))
