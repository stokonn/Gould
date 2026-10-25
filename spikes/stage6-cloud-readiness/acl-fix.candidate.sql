-- REVIEW ONLY. No hosted execution authorized by this file.
BEGIN;
SET LOCAL lock_timeout = '5s';
SET LOCAL statement_timeout = '30s';
CREATE TEMP TABLE goldberg_acl_before ON COMMIT DROP AS
 SELECT p.oid, p.proowner, p.prosecdef, p.proconfig, p.proacl,
        pg_get_functiondef(p.oid) AS definition,
        t.oid AS trigger_oid, t.evtname, t.evtenabled, t.evttags, t.evtfoid
 FROM pg_proc p JOIN pg_event_trigger t ON t.evtfoid=p.oid
 WHERE p.oid=to_regprocedure('public.rls_auto_enable()') AND t.evtname='ensure_rls';
DO $guard$
BEGIN
 IF (SELECT count(*) FROM goldberg_acl_before) <> 1 OR NOT EXISTS (
  SELECT 1 FROM goldberg_acl_before b JOIN pg_proc p ON p.oid=b.oid
  WHERE b.prosecdef AND b.evtenabled='O' AND pg_get_userbyid(b.proowner)='postgres'
    AND b.proconfig=ARRAY['search_path=pg_catalog']
    AND p.prorettype='pg_catalog.event_trigger'::regtype
    AND b.evttags @> ARRAY['CREATE TABLE','CREATE TABLE AS','SELECT INTO']::text[]
    AND cardinality(b.evttags)=3
 ) THEN RAISE EXCEPTION 'Unexpected function/trigger identity; stop for review'; END IF;
END
$guard$;
REVOKE EXECUTE ON FUNCTION public.rls_auto_enable() FROM PUBLIC, anon, authenticated;
DO $verify$
DECLARE f oid := to_regprocedure('public.rls_auto_enable()');
BEGIN
 IF has_function_privilege('anon', f, 'EXECUTE')
 OR has_function_privilege('authenticated', f, 'EXECUTE')
 OR EXISTS (SELECT 1 FROM pg_proc p,
    LATERAL aclexplode(coalesce(p.proacl,acldefault('f',p.proowner))) a
    WHERE p.oid=f AND a.grantee=0 AND a.privilege_type='EXECUTE') THEN
  RAISE EXCEPTION 'Effective client or PUBLIC execution remains'; END IF;
 IF NOT EXISTS (
  SELECT 1 FROM goldberg_acl_before b JOIN pg_proc p ON p.oid=b.oid
  JOIN pg_event_trigger t ON t.oid=b.trigger_oid
  WHERE p.proowner=b.proowner AND p.prosecdef=b.prosecdef
    AND p.proconfig IS NOT DISTINCT FROM b.proconfig
    AND pg_get_functiondef(p.oid)=b.definition
    AND (t.evtname,t.evtenabled,t.evttags,t.evtfoid)=
        (b.evtname,b.evtenabled,b.evttags,b.evtfoid)
 ) THEN RAISE EXCEPTION 'Function or event trigger changed'; END IF;
 -- Preserve every ACL grant except the three explicitly revoked grantees.
 IF EXISTS (
  (SELECT a.* FROM goldberg_acl_before b,
   LATERAL aclexplode(coalesce(b.proacl,acldefault('f',b.proowner))) a
   WHERE a.grantee NOT IN (0,'anon'::regrole::oid,'authenticated'::regrole::oid)
   EXCEPT
   SELECT a.* FROM pg_proc p, LATERAL aclexplode(p.proacl) a WHERE p.oid=f)
 ) THEN RAISE EXCEPTION 'Unrelated grants changed'; END IF;
END
$verify$;
COMMIT;
