-- REVIEW ONLY. Separate explicit hosted authorization required.
-- Apply only AFTER the approved ACL fix and existing private-schema bootstrap.
-- No password is embedded or set. Until a separately authorized credential step,
-- the new LOGIN role has no usable password. This grants no moderation access.
BEGIN;
SET LOCAL lock_timeout='5s';
SET LOCAL statement_timeout='30s';
SELECT pg_advisory_xact_lock(hashtextextended('goldberg-staging-probe-v1',0));
DO $guard$
BEGIN
 IF to_regclass('goldberg_private.deployment_migrations') IS NULL THEN
  RAISE EXCEPTION 'Verified private bootstrap required'; END IF;
 IF (SELECT checksum FROM goldberg_private.deployment_migrations WHERE version=1)
    IS DISTINCT FROM 'd39a3afd68a2671dd642a17a82f96edea76dfc0ae1a09edb4be9ffde6aa5c935' THEN
  RAISE EXCEPTION 'Unexpected bootstrap checksum'; END IF;
 IF (SELECT enabled FROM goldberg_private.intake_control WHERE singleton) IS DISTINCT FROM false THEN
  RAISE EXCEPTION 'Disabled intake required'; END IF;
 IF has_function_privilege('anon','public.rls_auto_enable()','EXECUTE')
 OR has_function_privilege('authenticated','public.rls_auto_enable()','EXECUTE') THEN
  RAISE EXCEPTION 'Approved ACL maintenance must precede probe access'; END IF;
 IF EXISTS(SELECT 1 FROM pg_roles WHERE rolname='goldberg_staging_probe') THEN
  RAISE EXCEPTION 'Existing probe role: preserve and review, do not overwrite'; END IF;
 IF to_regclass('goldberg_private.staging_probe') IS NOT NULL THEN
  RAISE EXCEPTION 'Existing probe table: preserve and review'; END IF;
END
$guard$;
CREATE ROLE goldberg_staging_probe LOGIN NOINHERIT NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS CONNECTION LIMIT 2;
ALTER ROLE goldberg_staging_probe SET statement_timeout='3s';
ALTER ROLE goldberg_staging_probe SET lock_timeout='1s';
ALTER ROLE goldberg_staging_probe SET idle_in_transaction_session_timeout='3s';
ALTER ROLE goldberg_staging_probe SET search_path='pg_catalog';
CREATE TABLE goldberg_private.staging_probe (
 id integer PRIMARY KEY CHECK(id=1), marker text NOT NULL CHECK(marker='synthetic-only')
);
INSERT INTO goldberg_private.staging_probe VALUES(1,'synthetic-only');
ALTER TABLE goldberg_private.staging_probe ENABLE ROW LEVEL SECURITY;
REVOKE ALL ON goldberg_private.staging_probe FROM PUBLIC,anon,authenticated,service_role;
GRANT USAGE ON SCHEMA goldberg_private TO goldberg_staging_probe;
GRANT SELECT ON goldberg_private.staging_probe TO goldberg_staging_probe;
CREATE POLICY synthetic_probe_read ON goldberg_private.staging_probe
 FOR SELECT TO goldberg_staging_probe USING (id=1 AND marker='synthetic-only');
DO $connect$ BEGIN
 EXECUTE format('GRANT CONNECT ON DATABASE %I TO goldberg_staging_probe',current_database());
END $connect$;
COMMIT;
