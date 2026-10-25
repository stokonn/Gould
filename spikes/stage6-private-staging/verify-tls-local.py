"""New actual PostgreSQL TLS/password probe checks on disposable host loopback only.

Never selects a hosted account, database or credentials. Cached image only.
"""
from pathlib import Path
import hashlib
import json
import os
import subprocess
import tempfile
import time
import uuid
import sys

BASE = Path(__file__).resolve().parent
name = 'goldberg-probe-tls-' + uuid.uuid4().hex
out = BASE / 'results' / name
out.mkdir()
checks = []
container = False
volumes = []

def run(argv, **kw):
    return subprocess.run(argv, text=True, capture_output=True, check=True, **kw)

try:
    with tempfile.TemporaryDirectory(prefix='goldberg-local-tls-') as work:
        work = Path(work)
        # These private keys authenticate only the disposable synthetic loopback venue.
        run(['openssl', 'req', '-x509', '-newkey', 'rsa:2048', '-nodes', '-days', '1',
             '-subj', '/CN=Goldberg Synthetic Test CA', '-keyout', str(work/'ca.key'), '-out', str(work/'ca.crt')])
        run(['openssl', 'req', '-newkey', 'rsa:2048', '-nodes', '-subj', '/CN=localhost',
             '-keyout', str(work/'server.key'), '-out', str(work/'server.csr')])
        (work/'extensions').write_text('subjectAltName=DNS:localhost,IP:127.0.0.1\nextendedKeyUsage=serverAuth\n')
        run(['openssl', 'x509', '-req', '-in', str(work/'server.csr'), '-CA', str(work/'ca.crt'),
             '-CAkey', str(work/'ca.key'), '-CAcreateserial', '-days', '1', '-extfile', str(work/'extensions'), '-out', str(work/'server.crt')])
        result = run(['podman', 'create', '--pull=never', '--name', name, '--label', 'goldberg.scope=local-synthetic-stage6-tls',
                      '--publish', '127.0.0.1::5432', '--env', 'POSTGRES_PASSWORD=synthetic-container-only',
                      '--volume', str(work)+':/fixture:ro,Z', 'docker.io/library/postgres:17',
                      'bash', '-c', 'cp /fixture/server.key /fixture/server.crt /tmp/; chown postgres:postgres /tmp/server.key /tmp/server.crt; chmod 600 /tmp/server.key; exec docker-entrypoint.sh postgres -c ssl=on -c ssl_cert_file=/tmp/server.crt -c ssl_key_file=/tmp/server.key'])
        container = True
        info = json.loads(run(['podman', 'inspect', name]).stdout)[0]
        volumes = [m['Name'] for m in info.get('Mounts', []) if m.get('Type') == 'volume']
        run(['podman', 'start', name])
        ready = False
        for _ in range(80):
            p = subprocess.run(['podman', 'exec', name, 'pg_isready', '-U', 'postgres'], capture_output=True)
            if p.returncode == 0:
                ready = True
                break
            time.sleep(.25)
        if not ready:
            raise RuntimeError('Synthetic PostgreSQL startup failed')
        port = run(['podman', 'port', name, '5432/tcp']).stdout.strip()
        if not port.startswith('127.0.0.1:'):
            raise RuntimeError('Listener is not loopback-only')
        port = int(port.rsplit(':', 1)[1])
        # Exact minimal probe fixture; no accepted bootstrap test bodies are rerun.
        sql = """CREATE SCHEMA goldberg_private;
CREATE TABLE goldberg_private.staging_probe(id integer PRIMARY KEY, marker text NOT NULL);
INSERT INTO goldberg_private.staging_probe VALUES(1,'synthetic-only');
CREATE ROLE goldberg_staging_probe LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT NOBYPASSRLS PASSWORD 'synthetic-probe-only';
GRANT USAGE ON SCHEMA goldberg_private TO goldberg_staging_probe;
GRANT SELECT ON goldberg_private.staging_probe TO goldberg_staging_probe;
ALTER TABLE goldberg_private.staging_probe ENABLE ROW LEVEL SECURITY;
CREATE POLICY probe_select ON goldberg_private.staging_probe FOR SELECT TO goldberg_staging_probe USING(id=1);
"""
        run(['podman','exec','-i',name,'psql','-X','-v','ON_ERROR_STOP=1','-U','postgres'], input=sql)
        driver = Path('/tmp/goldberg-private-staging-build-20261025/node_modules/pg/lib/index.js')
        if not driver.is_file():
            raise RuntimeError('Pinned pg driver missing; never install or upgrade silently')
        script = work/'tls-test.mjs'
        script.write_text('''import {createRequire} from 'node:module';
import tls from 'node:tls';
import fs from 'node:fs';
const require=createRequire(import.meta.url);
const {Client}=require(process.argv[2]);
const port=Number(process.argv[3]);
const ca=fs.readFileSync(process.argv[4],'utf8');
const base={host:'127.0.0.1',port,user:'goldberg_staging_probe',password:'synthetic-probe-only',database:'postgres',connectionTimeoutMillis:1500,query_timeout:1500,
ssl:{ca,rejectUnauthorized:true,servername:'localhost'}};
const checks=[];
async function connect(options,accept){
 const client=new Client(options);let okay=false;
 try{await client.connect();const r=await client.query('SELECT id,marker FROM goldberg_private.staging_probe WHERE id=1');okay=r.rows.length===1&&r.rows[0].marker==='synthetic-only';}
 catch{okay=false;}finally{await client.end();}
 if(okay!==accept)throw Error('TLS/password assertion failed');
}
await connect(base,true);checks.push('verified CA/hostname/password actual SELECT');
await connect({...base,password:'wrong-synthetic-password'},false);checks.push('wrong password denied');
await connect({...base,ssl:{...base.ssl,servername:'wrong.invalid',checkServerIdentity:(_host,cert)=>tls.checkServerIdentity('wrong.invalid',cert)}},false);checks.push('wrong hostname denied');
await connect({...base,ssl:{rejectUnauthorized:true,servername:'localhost'}},false);checks.push('untrusted CA denied');
console.log(JSON.stringify({status:'PASS',checks,hostedRequests:0}));
''')
        execution = run(['node', str(script), str(driver), str(port), str(work/'ca.crt')], timeout=30)
        result = json.loads(execution.stdout)
        checks = result['checks']
        (out/'checks.json').write_text(json.dumps(result, indent=2)+'\n')
        # New archive composition: real synthetic pg_dump -> encrypted atomic store
        # -> authenticated isolated output -> fresh database, never the existing project.
        sys.path.insert(0, str(BASE.parents[1] / 'scripts/stage6'))
        from backup_store import publish, restore_dump
        archive_root = work/'archives'; archive_root.mkdir(mode=0o700)
        restore_root = work/'restore'; restore_root.mkdir(mode=0o700)
        key = work/'backup-key'; key.write_text(os.urandom(32).hex()); key.chmod(0o600)
        dump = work/'source.dump'
        exported = subprocess.run(['podman','exec',name,'pg_dump','-U','postgres','-d','postgres','--schema=goldberg_private','--format=custom'],capture_output=True,check=True)
        dump.write_bytes(exported.stdout); dump.chmod(0o600)
        archive = publish(dump, archive_root, key)
        restored = restore_dump(archive, key, restore_root/'restored.dump')
        if restored.read_bytes() != dump.read_bytes(): raise RuntimeError('Dump equality failed')
        checks.append('actual synthetic pg_dump encrypted archive roundtrip byte equality')
        run(['podman','exec',name,'createdb','-U','postgres','isolated_restore'])
        subprocess.run(['podman','exec','-i',name,'pg_restore','-U','postgres','-d','isolated_restore','--exit-on-error'],input=restored.read_bytes(),capture_output=True,check=True)
        restored_row = run(['podman','exec',name,'psql','-X','-At','-v','ON_ERROR_STOP=1','-U','postgres','-d','isolated_restore','-c',"SELECT marker FROM goldberg_private.staging_probe WHERE id=1; SELECT relrowsecurity FROM pg_class WHERE oid='goldberg_private.staging_probe'::regclass; SELECT count(*) FROM pg_policy WHERE polrelid='goldberg_private.staging_probe'::regclass;"]).stdout.strip()
        if restored_row != 'synthetic-only\nt\n1': raise RuntimeError('Restored marker/RLS/policy mismatch')
        checks.append('actual isolated database restore preserves marker/RLS/SELECT policy')
        (out/'checks.json').write_text(json.dumps({'status':'PASS','checks':checks,'hostedRequests':0},indent=2)+'\n')
        status = 'PASS'
except Exception as failure:
    status = 'FAILED'
    # Suppress command/stdout/stderr because credential-bearing tools must never leak.
    (out/'failure.json').write_text(json.dumps({'errorType':type(failure).__name__})+'\n')
finally:
    cleanup = 'not-created'
    if container:
        removed = subprocess.run(['podman','rm','--force','--volumes',name],capture_output=True).returncode
        absent = subprocess.run(['podman','container','exists',name],capture_output=True).returncode == 1
        volume_absent = all(subprocess.run(['podman','volume','exists',v],capture_output=True).returncode == 1 for v in volumes)
        cleanup = 'removed-and-absence-verified' if removed == 0 and absent and volume_absent else 'FAILED'
        if cleanup == 'FAILED': status = 'FAILED'
    result = {'status':status,'checks':checks,'cleanup':cleanup,'container':name,'volumes':volumes,'hostedWrites':0,
              'sourceSha256':hashlib.sha256(Path(__file__).read_bytes()).hexdigest(),
              'artifacts':{p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in out.iterdir()}}
    (out/'result.json').write_text(json.dumps(result,indent=2)+'\n')
    print(json.dumps({'out':str(out),'status':status,'checks':len(checks),'cleanup':cleanup}))
    if status != 'PASS': raise SystemExit(1)
