"""Read-only hosted metadata checks after catalogue credential provisioning.

Uses protected libpq service without exporting its credential. Emits aggregate
metadata only. Does not retry, bootstrap, repair grants, seed rows or reset roles.
"""
import argparse
import datetime as dt
import json
from pathlib import Path
from libpq_service import ServiceConnection
from provision_private_runtime import ROLE, GROUP, LEDGER


def verify(connection, previous_acl):
    result = json.loads(connection.query(f"""SELECT json_build_object(
      'ledger',(SELECT checksum FROM goldberg_private.deployment_migrations WHERE version=1),
      'intake',(SELECT enabled FROM goldberg_private.intake_control),
      'applicationRows',(SELECT count(*) FROM goldberg_private.principals)+(SELECT count(*) FROM goldberg_private.sessions)+(SELECT count(*) FROM goldberg_private.audit_events),
      'rlsTables',(SELECT count(*) FROM pg_tables WHERE schemaname='goldberg_private' AND rowsecurity),
      'policies',(SELECT count(*) FROM pg_policies WHERE schemaname='goldberg_private'),
      'cataloguePolicies',(SELECT count(*) FROM pg_policies WHERE schemaname='goldberg_private' AND roles=ARRAY['{ROLE}']::name[]),
      'bffPolicies',(SELECT count(*) FROM pg_policies WHERE schemaname='goldberg_private' AND roles=ARRAY['{GROUP}']::name[]),
      'bffRoleInert',(SELECT NOT rolcanlogin AND NOT rolsuper AND NOT rolcreatedb AND NOT rolcreaterole AND NOT rolreplication AND NOT rolbypassrls FROM pg_roles WHERE rolname='{GROUP}'),
      'catalogueRoleSafe',(SELECT rolcanlogin AND NOT rolsuper AND NOT rolcreatedb AND NOT rolcreaterole AND NOT rolinherit AND NOT rolreplication AND NOT rolbypassrls AND rolconnlimit=4 AND rolvaliduntil>now() FROM pg_roles WHERE rolname='{ROLE}'),
      'expiresUtc',(SELECT rolvaliduntil FROM pg_roles WHERE rolname='{ROLE}'),
      'catalogueMemberships',(SELECT count(*) FROM pg_auth_members WHERE member=(SELECT oid FROM pg_roles WHERE rolname='{ROLE}')),
      'catalogueOwnsObjects',(SELECT count(*) FROM pg_class WHERE relowner=(SELECT oid FROM pg_roles WHERE rolname='{ROLE}')),
      'anonPrivate',has_schema_privilege('anon','goldberg_private','USAGE'),
      'authenticatedPrivate',has_schema_privilege('authenticated','goldberg_private','USAGE'),
      'bffTableGrants',(SELECT json_agg(json_build_object('table',table_name,'privilege',privilege_type) ORDER BY table_name,privilege_type) FROM information_schema.table_privileges WHERE table_schema='goldberg_private' AND grantee='{GROUP}'),
      'catalogueTableGrants',(SELECT json_agg(json_build_object('table',table_name,'privilege',privilege_type) ORDER BY table_name,privilege_type) FROM information_schema.table_privileges WHERE table_schema='goldberg_private' AND grantee='{ROLE}'),
      'catalogueLockColumns',(SELECT json_agg(json_build_object('table',table_name,'column',column_name,'privilege',privilege_type) ORDER BY table_name,column_name) FROM information_schema.column_privileges WHERE table_schema='goldberg_private' AND grantee='{ROLE}' AND privilege_type<>'SELECT'),
      'tableCounts',json_build_object('principals',(SELECT count(*) FROM goldberg_private.principals),'sessions',(SELECT count(*) FROM goldberg_private.sessions),'audit_events',(SELECT count(*) FROM goldberg_private.audit_events),'published_entities',(SELECT count(*) FROM goldberg_private.published_entities),'published_revisions',(SELECT count(*) FROM goldberg_private.published_revisions),'change_feed',(SELECT count(*) FROM goldberg_private.change_feed),'staging_probe',(SELECT count(*) FROM goldberg_private.staging_probe))
    )::text""")[0][0])
    expected = {'ledger':LEDGER,'intake':False,'applicationRows':0,'rlsTables':20,
                'policies':55,'cataloguePolicies':5,'bffPolicies':49,'bffRoleInert':True,
                'catalogueRoleSafe':True,'catalogueMemberships':0,'catalogueOwnsObjects':0,
                'anonPrivate':False,'authenticatedPrivate':False}
    if any(result[k] != v for k,v in expected.items()):
        raise ValueError('Hosted metadata differs; no repair attempted')
    if result['bffTableGrants'] != previous_acl['table_grants']:
        raise ValueError('Existing BFF grants changed')
    if result['catalogueTableGrants'] != [{'table':t,'privilege':'SELECT'} for t in ['audit_head','change_feed','published_entities','published_revisions']]:
        raise ValueError('Catalogue table scope differs')
    if result['catalogueLockColumns'] != [{'table':'audit_head','column':'singleton','privilege':'UPDATE'}]:
        raise ValueError('Catalogue lock scope differs')
    if result['tableCounts'] != dict.fromkeys(['principals','sessions','audit_events','published_entities','published_revisions','change_feed'],0)|{'staging_probe':1}:
        raise ValueError('Hosted aggregate data changed')
    result.update(status='PASS',observedUtc=dt.datetime.now(dt.timezone.utc).isoformat(),metadataOnly=True,mutations=0)
    return result


if __name__=='__main__':
    parser=argparse.ArgumentParser();parser.add_argument('--service-file',type=Path,default=Path.home()/'.config/goldberg/stage6/postgres.service.conf')
    parser.add_argument('--previous-acl',type=Path,default=Path('docs/planning/goldberg-0.11/stage-6/results/runtime-continuation-2026-10-25/hosted-runtime-acl.json'))
    args=parser.parse_args()
    try:
        with ServiceConnection(args.service_file) as c: result=verify(c,json.loads(args.previous_acl.read_text()))
        print(json.dumps(result,indent=2))
    except Exception as error:
        print(json.dumps({'status':'STOPPED','errorType':type(error).__name__}));raise SystemExit(1)
