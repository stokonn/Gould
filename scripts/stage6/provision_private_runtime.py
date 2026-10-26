"""One-time private runtime login; preserves the existing NOLOGIN grant role.

Admin credentials are consumed by libpq only. Only the NEW runtime credential
and server keys enter a protected output file; plaintext passwords never enter
SQL, argv, evidence, Cloudflare logs or CI. Existing roles/passwords are not reset.
"""
import argparse
import datetime as dt
import json
import os
from pathlib import Path
import re
import secrets

from libpq_service import ServiceConnection

ROLE = 'goldberg_bff_catalogue_login'
GROUP = 'goldberg_bff_runtime'
PROJECT = 'hlfsouvndjsbsaldttvi'
LEDGER = 'd39a3afd68a2671dd642a17a82f96edea76dfc0ae1a09edb4be9ffde6aa5c935'


def catalogue_grant_statements(schema='goldberg_private', role=ROLE):
    if schema != 'goldberg_private' and not re.fullmatch(r'proto_[a-z0-9_]{1,55}', schema):
        raise ValueError('Canonical or disposable schema required')
    if not re.fullmatch(r'goldberg_bff_[a-z0-9_]{1,40}', role):
        raise ValueError('Restricted catalogue role required')
    statements = [f'GRANT USAGE ON SCHEMA {schema} TO {role}']
    for table in ['published_entities', 'published_revisions', 'change_feed', 'audit_head']:
        statements += [f'GRANT SELECT ON {schema}.{table} TO {role}',
                       f'CREATE POLICY stage6_catalogue_read ON {schema}.{table} FOR SELECT TO {role} USING(true)']
    return statements + [f'GRANT UPDATE(singleton) ON {schema}.audit_head TO {role}',
                         f'CREATE POLICY stage6_catalogue_lock ON {schema}.audit_head FOR UPDATE TO {role} USING(true) WITH CHECK(singleton)']


def secret_file(path, value):
    fd = os.open(path, os.O_CREAT | os.O_EXCL | os.O_WRONLY, 0o600)
    with os.fdopen(fd, 'w') as stream:
        json.dump(value, stream); stream.write('\n'); stream.flush(); os.fsync(stream.fileno())


def preflight(connection):
    options = connection.public_options()
    if options.get('dbname') != 'postgres' or options.get('sslmode') != 'verify-full' or options.get('port') != '5432':
        raise ValueError('Verified session-pooler transport required')
    if not re.fullmatch(r'aws-[0-9]+-eu-central-1\.pooler\.supabase\.com', options.get('host', '')):
        raise ValueError('Unexpected pooler; never guess or retarget')
    if options.get('user') != 'postgres.' + PROJECT:
        raise ValueError('Unexpected administrative service identity')
    ca = Path(options['sslrootcert']).read_text()
    if 'BEGIN CERTIFICATE' not in ca or len(ca.encode()) > 16384:
        raise ValueError('Expected bounded public CA certificate')
    rows = connection.query(f"""SELECT json_build_object(
      'actor',current_user,'database',current_database(),
      'roleExists',EXISTS(SELECT 1 FROM pg_roles WHERE rolname='{ROLE}'),
      'groupSafe',EXISTS(SELECT 1 FROM pg_roles WHERE rolname='{GROUP}' AND NOT rolcanlogin AND NOT rolsuper AND NOT rolcreatedb AND NOT rolcreaterole AND NOT rolreplication AND NOT rolbypassrls),
      'ledger',(SELECT checksum FROM goldberg_private.deployment_migrations WHERE version=1),
      'rlsTables',(SELECT count(*) FROM pg_class c JOIN pg_namespace n ON n.oid=c.relnamespace WHERE n.nspname='goldberg_private' AND c.relkind='r' AND c.relrowsecurity),
      'policies',(SELECT count(*) FROM pg_policy p JOIN pg_class c ON c.oid=p.polrelid JOIN pg_namespace n ON n.oid=c.relnamespace WHERE n.nspname='goldberg_private'),
      'applicationRows',(SELECT count(*) FROM goldberg_private.principals)+(SELECT count(*) FROM goldberg_private.sessions)+(SELECT count(*) FROM goldberg_private.audit_events),
      'anonPrivate',has_schema_privilege('anon','goldberg_private','USAGE'),
      'authenticatedPrivate',has_schema_privilege('authenticated','goldberg_private','USAGE'),
      'broadRoleCanEnableIntake',has_column_privilege('{GROUP}','goldberg_private.intake_control','enabled','UPDATE'),
      'broadRoleCanReadLedger',has_table_privilege('{GROUP}','goldberg_private.deployment_migrations','SELECT'),
      'intake',(SELECT enabled FROM goldberg_private.intake_control))::text""")
    result = json.loads(rows[0][0])
    if result != {'actor': 'postgres', 'database': 'postgres', 'roleExists': False, 'groupSafe': True,
                  'ledger': LEDGER, 'rlsTables': 20, 'policies': 50, 'applicationRows': 0,
                  'anonPrivate': False, 'authenticatedPrivate': False,
                  'broadRoleCanEnableIntake': False, 'broadRoleCanReadLedger': False, 'intake': False}:
        raise ValueError('Existing role/schema/intake state differs; stop without resetting or bootstrap')
    return options, ca, result


def provision(connection, directory):
    options, ca, before = preflight(connection)
    directory = Path(directory); directory.mkdir(mode=0o700, parents=False, exist_ok=False)
    password = secrets.token_urlsafe(48)
    expires = dt.datetime.now(dt.timezone.utc) + dt.timedelta(hours=6)
    env = {'COMMUNITY_BFF_ENABLED': 'false', 'GOOGLE_OAUTH_ENABLED': 'false', 'DB_HOST': options['host'], 'EXPECTED_DB_HOST': options['host'],
           'DB_PORT': '5432', 'DB_USER': ROLE + '.' + PROJECT, 'DB_PASSWORD': password,
           'DB_CA_PEM': ca, 'CURSOR_KEY': secrets.token_hex(32), 'IP_KEY': secrets.token_hex(32)}
    # Preserve pending recovery material if COMMIT has an uncertain outcome.
    # A rerun refuses an existing role and never silently rotates its password.
    pending = directory / 'runtime.pending.json'; secret_file(pending, env)
    verifier = connection.password_verifier(password, ROLE)
    if not verifier.startswith('SCRAM-SHA-256$'):
        raise ValueError('libpq must prepare SCRAM without plaintext SQL')
    connection.query('BEGIN')
    try:
        connection.query("SET LOCAL lock_timeout='2s'; SET LOCAL statement_timeout='5s'")
        # CREATE intentionally fails on an existing login; no ALTER PASSWORD.
        connection.query(f"CREATE ROLE {ROLE} LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT NOREPLICATION NOBYPASSRLS CONNECTION LIMIT 4 PASSWORD '{verifier}' VALID UNTIL '{expires.isoformat()}'")
        # Current owner-approved scope is catalogue reads, not full application
        # writes. Do not inherit the existing broad BFF grant role.
        for statement in catalogue_grant_statements(): connection.query(statement)
        connection.query(f"ALTER ROLE {ROLE} SET statement_timeout='5s'; ALTER ROLE {ROLE} SET lock_timeout='2s'; ALTER ROLE {ROLE} SET idle_in_transaction_session_timeout='10s'")
        connection.query('COMMIT')
    except Exception:
        try: connection.query('ROLLBACK')
        except Exception: pass
        raise
    final = directory / 'runtime.json'; pending.rename(final)
    receipt = {'role': ROLE, 'scope': 'four catalogue tables SELECT plus audit-head column lock; no inherited application role', 'connectionLimit': 4, 'expiresUtc': expires.isoformat(),
               'administrativeCredentialExported': False, 'publicNetworking': False,
               'existingRolesPasswordsPoliciesDataChanged': False, 'addedSelectPolicies': 4, 'addedLockPolicies': 1, 'before': before,
               'secretFile': str(final), 'uncertainOutcomePolicy': 'retain protected pending file; never reset an existing role'}
    secret_file(directory / 'receipt.json', receipt)
    return receipt


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--service-file', type=Path, default=Path.home()/'.config/goldberg/stage6/postgres.service.conf')
    parser.add_argument('--provision', action='store_true')
    parser.add_argument('--output-directory', type=Path)
    args = parser.parse_args()
    try:
        with ServiceConnection(args.service_file) as connection:
            if args.provision:
                if not args.output_directory: raise ValueError('New protected custody directory required')
                result = provision(connection, args.output_directory)
            else:
                _, _, result = preflight(connection)
                result = {'preflight': result, 'clientTls': 'verify-full', 'mutations': 0}
        print(json.dumps(result))
    except Exception as error:
        # Exception messages from this module/libpq wrapper are intentionally
        # restricted. Never render SQL, driver text or a secret envelope.
        print(json.dumps({'status': 'STOPPED', 'errorType': type(error).__name__}))
        raise SystemExit(1)
