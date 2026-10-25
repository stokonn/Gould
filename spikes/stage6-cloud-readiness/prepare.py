"""Generate review-only SQL. No database/network client or deployment command."""
from pathlib import Path
import hashlib

BASE = Path(__file__).resolve().parent
ROOT = BASE.parents[1]
SOURCES = [
    'spikes/stage6-schema-pg/migrations/001_entities.sql',
    'spikes/stage6-schema-pg/migrations/002_integrity.sql',
    'spikes/stage6-auth-local/migrations/003_auth.sql',
    'spikes/stage6-public-admin-abuse/migrations/004_operations.sql',
]


def bundle():
    files = [(name, (ROOT / name).read_text()) for name in SOURCES]
    digest = hashlib.sha256(''.join(name + '\n' + sql for name, sql in files).encode()).hexdigest()
    sql = f"""-- REVIEW CANDIDATE ONLY. Not applied remotely. Source bundle SHA256: {digest}
-- Run only after C4/security/owner gates, in a privileged maintenance transaction.
BEGIN;
SET LOCAL lock_timeout = '5s';
SET LOCAL statement_timeout = '30s';
SELECT pg_advisory_xact_lock(hashtextextended('goldberg-private-bootstrap-v1',0));
DO $bootstrap$
DECLARE installed text; t record;
BEGIN
 IF current_setting('server_version_num')::integer NOT BETWEEN 170000 AND 179999 THEN
  RAISE EXCEPTION 'PostgreSQL 17 required'; END IF;
 IF EXISTS (SELECT 1 FROM pg_namespace WHERE nspname='goldberg_private') THEN
  IF to_regclass('goldberg_private.deployment_migrations') IS NULL THEN
   RAISE EXCEPTION 'Unrecognized existing schema; preserve and investigate'; END IF;
  EXECUTE 'SELECT checksum FROM goldberg_private.deployment_migrations WHERE version=1' INTO installed;
  IF installed IS DISTINCT FROM '{digest}' OR
     (SELECT count(*) FROM goldberg_private.deployment_migrations) <> 1 THEN
   RAISE EXCEPTION 'Deployment history/checksum mismatch'; END IF;
 ELSE
  CREATE SCHEMA goldberg_private;
  CREATE TABLE goldberg_private.deployment_migrations(version integer PRIMARY KEY, checksum text NOT NULL);
"""
    for name, content in files:
        # Dynamic execution inside an atomic guard; historical SQL remains byte-identical.
        sql += f"  -- {name}; SHA256 {hashlib.sha256(content.encode()).hexdigest()}\n"
        assert '$migration$' not in content
        sql += '  EXECUTE $migration$' + content.replace('__S__', 'goldberg_private') + '$migration$;\n'
    sql += f"""  INSERT INTO goldberg_private.deployment_migrations VALUES(1,'{digest}');
 END IF;
 -- No API/runtime grants. RLS with zero policies denies non-owner access.
 -- Owner bypass remains necessary for existing audit triggers; no FORCE RLS shortcut.
 REVOKE ALL ON SCHEMA goldberg_private FROM PUBLIC, anon, authenticated, service_role;
 REVOKE ALL ON ALL TABLES IN SCHEMA goldberg_private FROM PUBLIC, anon, authenticated, service_role;
 REVOKE ALL ON ALL SEQUENCES IN SCHEMA goldberg_private FROM PUBLIC, anon, authenticated, service_role;
 REVOKE ALL ON ALL FUNCTIONS IN SCHEMA goldberg_private FROM PUBLIC, anon, authenticated, service_role;
 ALTER DEFAULT PRIVILEGES IN SCHEMA goldberg_private REVOKE ALL ON TABLES FROM PUBLIC, anon, authenticated, service_role;
 ALTER DEFAULT PRIVILEGES IN SCHEMA goldberg_private REVOKE ALL ON SEQUENCES FROM PUBLIC, anon, authenticated, service_role;
 ALTER DEFAULT PRIVILEGES IN SCHEMA goldberg_private REVOKE EXECUTE ON FUNCTIONS FROM PUBLIC, anon, authenticated, service_role;
 FOR t IN SELECT c.relname FROM pg_class c JOIN pg_namespace n ON n.oid=c.relnamespace
   WHERE n.nspname='goldberg_private' AND c.relkind IN ('r','p') LOOP
  EXECUTE format('ALTER TABLE goldberg_private.%I ENABLE ROW LEVEL SECURITY',t.relname);
 END LOOP;
 IF (SELECT enabled FROM goldberg_private.intake_control WHERE singleton) THEN
  RAISE EXCEPTION 'Intake must remain disabled during readiness'; END IF;
END
$bootstrap$;
COMMIT;
"""
    return sql


if __name__ == '__main__':
    (BASE / 'private-schema.candidate.sql').write_text(bundle())
    print('Prepared private-schema.candidate.sql; no database contacted')
