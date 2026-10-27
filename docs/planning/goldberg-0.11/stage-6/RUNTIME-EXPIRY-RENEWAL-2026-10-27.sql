-- Owner-authorized manual renewal:90days from this transaction execution.
-- Changes expiry only. Never changes password, privileges, RLS, data or billing.
BEGIN;
SET LOCAL lock_timeout='2s';
SET LOCAL statement_timeout='5s';
SET LOCAL TIME ZONE 'UTC';
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
 EXECUTE format('ALTER ROLE %I VALID UNTIL %L',r.rolname,transaction_timestamp()+interval '90 days');
END $guard$;
COMMIT;
