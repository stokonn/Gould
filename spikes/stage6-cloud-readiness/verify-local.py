"""Synthetic-only PG17 verification via isolated Podman; no host/URL input."""
from pathlib import Path
import subprocess, uuid, json, hashlib, sys, signal
from prepare import bundle, BASE, SOURCES, ROOT

IMAGE = 'docker.io/library/postgres@sha256:e4cb93687b062cd25a0e8f672d85a7708235009d70d31f8a7698d9370092a9c4'
name = 'goldberg-cloud-readiness-' + uuid.uuid4().hex
out = BASE / 'results' / name
out.mkdir(parents=True)
checks = []
created = False
volumes = []
status = 'FAILED'
commands = []
source_paths = [BASE/'prepare.py', BASE/'verify-local.py', BASE/'acl-fix.candidate.sql',
                BASE/'private-schema.candidate.sql', BASE/'worker.mjs', BASE/'worker.test.mjs',
                BASE/'cloudflare.config.ts.template', BASE/'verify-evidence.py'] + [ROOT/p for p in SOURCES]
source_before = {str(p.relative_to(ROOT)):hashlib.sha256(p.read_bytes()).hexdigest() for p in source_paths}


def call(args, content=None, ok=True):
    p = subprocess.run(args, input=content, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, timeout=60)
    log = f'command-{len(commands):03}.log'
    (out/log).write_text(p.stdout)
    commands.append({'argv':args,'exitCode':p.returncode,'log':log})
    if ok and p.returncode:
        raise RuntimeError(p.stdout)
    return p


def sql(content, ok=True):
    return call(['podman', 'exec', '-i', name, 'psql', '-X', '-v', 'ON_ERROR_STOP=1', '-U', 'postgres', '-d', 'postgres', '-At'], content, ok)


def check(label, content, expect=None, fails=False, error=None):
    p = sql(content, ok=not fails)
    (out / f'{len(checks):02}.log').write_text(p.stdout)
    assert (p.returncode != 0) if fails else (p.returncode == 0)
    if expect is not None:
        assert p.stdout.strip().endswith(expect), (label, p.stdout)
    if error is not None:
        assert error in p.stdout, (label, p.stdout)
    checks.append(label)


def interrupted(_sig, _frame):
    raise KeyboardInterrupt('runner interrupted')


signal.signal(signal.SIGTERM, interrupted)
try:
    assert call(['podman','info','--format','{{.Host.Security.Rootless}}']).stdout.strip() == 'true'
    call(['podman','run','--detach','--pull=never','--network=none','--name',name,
          '--label','goldberg.scope=local-synthetic-cloud-readiness',
          '--env','POSTGRES_HOST_AUTH_METHOD=trust',IMAGE])
    created = True
    mounts = json.loads(call(['podman','inspect',name,'--format','{{json .Mounts}}']).stdout)
    volumes = [m['Name'] for m in mounts if m['Type']=='volume']
    # pg_isready retries are bounded; no remote network or published port.
    import time
    for _ in range(60):
        if call(['podman','exec',name,'pg_isready','-U','postgres'],ok=False).returncode == 0:
            break
        time.sleep(.2)
    else:
        raise RuntimeError('PG not ready')
    check('PostgreSQL 17', "SELECT current_setting('server_version_num')::int BETWEEN 170000 AND 179999;", 't')
    check('provider roles and representative trigger fixture', """
CREATE ROLE anon; CREATE ROLE authenticated; CREATE ROLE service_role BYPASSRLS;
CREATE ROLE unrelated;
CREATE FUNCTION public.rls_auto_enable() RETURNS event_trigger LANGUAGE plpgsql
SECURITY DEFINER SET search_path=pg_catalog AS $$ DECLARE cmd record; BEGIN
 FOR cmd IN SELECT * FROM pg_event_trigger_ddl_commands() WHERE object_type='table' LOOP
 IF cmd.schema_name='public' THEN EXECUTE format('ALTER TABLE %s ENABLE ROW LEVEL SECURITY',cmd.object_identity); END IF;
 END LOOP; END $$;
CREATE EVENT TRIGGER ensure_rls ON ddl_command_end WHEN TAG IN ('CREATE TABLE','CREATE TABLE AS','SELECT INTO')
 EXECUTE FUNCTION public.rls_auto_enable();
GRANT EXECUTE ON FUNCTION public.rls_auto_enable() TO unrelated;
""")
    acl = (BASE/'acl-fix.candidate.sql').read_text()
    check('ACL transactional rollback', acl.replace('COMMIT;', 'ROLLBACK;') +
          "SELECT has_function_privilege('anon','public.rls_auto_enable()','EXECUTE');", 't')
    check('ACL fix', acl)
    check('ACL replay', acl)
    check('trigger preserved and unrelated grant retained', "CREATE TABLE public.synthetic_rls(id int); SELECT relrowsecurity AND has_function_privilege('unrelated','public.rls_auto_enable()','EXECUTE') FROM pg_class WHERE oid='public.synthetic_rls'::regclass;", 't')
    check('ACL guard rejects disabled trigger', 'ALTER EVENT TRIGGER ensure_rls DISABLE;\n' + acl, fails=True,
          error='Unexpected function/trigger identity; stop for review')
    sql('ALTER EVENT TRIGGER ensure_rls ENABLE;')
    candidate = (BASE/'private-schema.candidate.sql').read_text()
    assert candidate == bundle(), 'stale generated SQL'
    check('schema rollback leaves no namespace', candidate.replace('COMMIT;', 'ROLLBACK;') +
          "SELECT to_regnamespace('goldberg_private') IS NULL;", 't')
    check('private schema apply', candidate)
    check('private schema idempotent replay', candidate)
    check('all tables RLS and no policies', """SELECT bool_and(c.relrowsecurity) AND NOT EXISTS
 (SELECT 1 FROM pg_policies WHERE schemaname='goldberg_private')
 FROM pg_class c JOIN pg_namespace n ON n.oid=c.relnamespace
 WHERE n.nspname='goldberg_private' AND c.relkind='r';""", 't')
    check('all API roles have no schema/table/function access', """SELECT NOT EXISTS (
 SELECT 1 FROM pg_roles r CROSS JOIN pg_namespace n
 WHERE r.rolname IN ('anon','authenticated','service_role') AND n.nspname='goldberg_private'
 AND (has_schema_privilege(r.oid,n.oid,'USAGE') OR EXISTS
  (SELECT 1 FROM pg_class c WHERE c.relnamespace=n.oid AND c.relkind='r' AND has_table_privilege(r.oid,c.oid,'SELECT'))
 OR EXISTS (SELECT 1 FROM pg_proc p WHERE p.pronamespace=n.oid AND has_function_privilege(r.oid,p.oid,'EXECUTE'))));""", 't')
    check('direct anonymous access denied', 'SET ROLE anon; SELECT * FROM goldberg_private.principals;', fails=True,
          error='permission denied for schema goldberg_private')
    check('direct authenticated access denied', 'SET ROLE authenticated; SELECT * FROM goldberg_private.principals;', fails=True,
          error='permission denied for schema goldberg_private')
    check('synthetic seed', "INSERT INTO goldberg_private.principals(id,iss,sub) VALUES('00000000-0000-0000-0000-000000000001','synthetic.invalid','fixture');")
    check('RLS denies after temporary schema/table grants', """BEGIN;
GRANT USAGE ON SCHEMA goldberg_private TO anon;
GRANT SELECT ON goldberg_private.principals TO anon;
SET LOCAL ROLE anon; SELECT count(*) FROM goldberg_private.principals;
ROLLBACK;""", '0\nROLLBACK')
    check('replay preserves synthetic record and intake disabled', candidate +
          "SELECT count(*)=1 AND NOT (SELECT enabled FROM goldberg_private.intake_control) FROM goldberg_private.principals;", 't')
    check('checksum drift rejected atomically', "UPDATE goldberg_private.deployment_migrations SET checksum='tampered';\n" + candidate, fails=True,
          error='Deployment history/checksum mismatch')
    # Restore only disposable fixture ledger, never production data.
    checksum = hashlib.sha256(''.join(p+'\n'+(ROOT/p).read_text() for p in SOURCES).encode()).hexdigest()
    sql(f"UPDATE goldberg_private.deployment_migrations SET checksum='{checksum}';")
    dump = call(['podman','exec',name,'pg_dump','-U','postgres','-d','postgres','-n','goldberg_private','--no-owner','--no-privileges']).stdout
    (out/'synthetic-backup.sql').write_text(dump)
    sql('CREATE DATABASE readiness_restore;')
    call(['podman','exec','-i',name,'psql','-X','-v','ON_ERROR_STOP=1','-U','postgres','-d','readiness_restore'],dump)
    # --no-privileges exports require re-hardening before any runtime access.
    call(['podman','exec','-i',name,'psql','-X','-v','ON_ERROR_STOP=1','-U','postgres','-d','readiness_restore'],candidate)
    restored = call(['podman','exec',name,'psql','-X','-U','postgres','-d','readiness_restore','-Atc',
                     'SELECT count(*)=1 AND NOT (SELECT enabled FROM goldberg_private.intake_control) FROM goldberg_private.principals;']).stdout.strip()
    assert restored == 't'
    checks.append('synthetic logical backup restores in second disposable database')
    denied = call(['podman','exec',name,'psql','-X','-v','ON_ERROR_STOP=1','-U','postgres','-d','readiness_restore','-Atc',
                   'SET ROLE authenticated; SELECT * FROM goldberg_private.principals;'],ok=False)
    assert denied.returncode != 0 and 'permission denied' in denied.stdout
    checks.append('restored database remains private after re-hardening')
    status = 'PASS'
except (Exception, KeyboardInterrupt) as e:
    status = 'FAILED'
    (out/'error.log').write_text(str(e))
finally:
    cleanup = 'not-created'
    if created:
        removed = call(['podman','rm','--force','--volumes','--time','0',name],ok=False)
        absent = call(['podman','container','exists',name],ok=False).returncode == 1
        no_volumes = all(call(['podman','volume','exists',v],ok=False).returncode == 1 for v in volumes)
        cleanup = 'removed-and-absence-verified' if removed.returncode == 0 and absent and no_volumes else 'FAILED'
    source_after = {str(p.relative_to(ROOT)):hashlib.sha256(p.read_bytes()).hexdigest() for p in source_paths}
    result = {'status':status,'checks':checks,'count':len(checks),'cleanup':cleanup,
              'sources':source_before, 'sourceUnchanged':source_before==source_after,
              'image':IMAGE,'syntheticOnly':True,'remoteMutations':0,'commands':commands,
              'artifacts':{p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in out.iterdir() if p.is_file()}}
    (out/'result.json').write_text(json.dumps(result,indent=2)+'\n')
    print(json.dumps({'path':str(out.relative_to(ROOT)),**result},indent=2))
sys.exit(0 if status=='PASS' and cleanup=='removed-and-absence-verified' and source_before==source_after else 1)
