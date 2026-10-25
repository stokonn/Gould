-- REVIEW ONLY: reversible containment; retain role/table/synthetic data.
BEGIN;
SET LOCAL lock_timeout='5s';
SET LOCAL statement_timeout='30s';
ALTER ROLE goldberg_staging_probe NOLOGIN;
REVOKE USAGE ON SCHEMA goldberg_private FROM goldberg_staging_probe;
REVOKE SELECT ON goldberg_private.staging_probe FROM goldberg_staging_probe;
COMMIT;
-- Existing sessions must be separately identified and terminated by an approved
-- operator; NOLOGIN blocks only new login, ACL revocation blocks these reads.
-- No automatic pg_terminate_backend, DROP, row deletion or project recreation.
