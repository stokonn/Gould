-- REVIEWABLE ONLY: requires owner authorization for a new runtime window.
-- Changes expiry only. Never changes password, privileges, RLS, data or billing.
BEGIN;
SET LOCAL lock_timeout='2s';
SET LOCAL statement_timeout='5s';
DO $guard$
DECLARE r pg_roles;
BEGIN
 SELECT * INTO STRICT r FROM pg_roles WHERE rolname='goldberg_bff_catalogue_login';
 IF NOT r.rolcanlogin OR r.rolsuper OR r.rolcreatedb OR r.rolcreaterole
 OR r.rolinherit OR r.rolreplication OR r.rolbypassrls OR r.rolconnlimit<>4
 OR EXISTS(SELECT 1 FROM pg_auth_members WHERE member=r.oid)
 OR EXISTS(SELECT 1 FROM pg_class WHERE relowner=r.oid)
 OR (SELECT enabled FROM goldberg_private.intake_control)
 THEN RAISE EXCEPTION 'Catalogue scope changed; stop without repair'; END IF;
END $guard$;
ALTER ROLE goldberg_bff_catalogue_login VALID UNTIL '2026-11-03 04:30:00+00';
COMMIT;
