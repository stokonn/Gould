# `rls_auto_enable()` ACL migration proposal

**Status: proposal only; not executed against Supabase.** This document
describes a narrowly scoped, reversible production migration for a future
owner-authorized maintenance window. The supplied candidate was verified
against a disposable PostgreSQL 17 fixture; that local result is not hosted
deployment evidence.

## Scope and safety properties

The change affects only `public.rls_auto_enable()`:

- preserve the existing `SECURITY DEFINER` function and `ensure_rls` event
  trigger;
- revoke only function-level `EXECUTE` inherited by `PUBLIC`, `anon`, and
  `authenticated`;
- do not drop, replace, move, or alter the function body, owner, search path,
  event trigger, schemas, tables, or unrelated ACLs;
- fail closed before any change if the expected function or trigger identity
  differs.

The migration must be run as a transaction by an appropriately privileged
operator. No application role, hosted connection string, or credential is
contained here.

## Preflight

Run these read-only queries first and retain their output with the change
record:

```sql
SELECT p.oid::regprocedure AS function_name,
       pg_get_userbyid(p.proowner) AS owner,
       p.prosecdef AS security_definer,
       p.proconfig,
       p.proacl,
       t.evtname,
       t.evtenabled,
       t.evtfoid::regprocedure AS trigger_function
FROM pg_proc p
LEFT JOIN pg_event_trigger t ON t.evtfoid = p.oid
WHERE p.oid = to_regprocedure('public.rls_auto_enable()');

SELECT grantee, privilege_type, is_grantable
FROM information_schema.routine_privileges
WHERE specific_schema = 'public'
  AND routine_name = 'rls_auto_enable'
ORDER BY grantee, privilege_type;
```

The expected identity is one `public.rls_auto_enable()` function, owned by the
existing owner, `SECURITY DEFINER`, with the existing `search_path`, and one
enabled `ensure_rls` trigger bound to that exact OID. The preflight ACL output
is the rollback source of truth; never substitute a guessed owner or ACL.

## Transactional migration and postflight

The following is a proposal for review, not an instruction to run now:

```sql
BEGIN;

DO $guard$
DECLARE
  f oid := to_regprocedure('public.rls_auto_enable()');
  trigger_count integer;
BEGIN
  IF f IS NULL THEN
    RAISE EXCEPTION 'Expected public.rls_auto_enable() is missing';
  END IF;
  IF NOT EXISTS (
    SELECT 1 FROM pg_proc
    WHERE oid = f
      AND prosecdef
      AND prorettype = 'pg_catalog.event_trigger'::regtype
  ) THEN
    RAISE EXCEPTION 'Function identity or SECURITY DEFINER property changed';
  END IF;
  SELECT count(*) INTO trigger_count
  FROM pg_event_trigger
  WHERE evtname = 'ensure_rls'
    AND evtfoid = f
    AND evtenabled IN ('O', 'A');
  IF trigger_count <> 1 THEN
    RAISE EXCEPTION 'Expected exactly one enabled ensure_rls trigger';
  END IF;
END
$guard$;

REVOKE EXECUTE ON FUNCTION public.rls_auto_enable()
  FROM PUBLIC, anon, authenticated;

DO $verify$
DECLARE
  f oid := to_regprocedure('public.rls_auto_enable()');
BEGIN
  IF has_function_privilege('anon', f, 'EXECUTE')
     OR has_function_privilege('authenticated', f, 'EXECUTE')
     OR EXISTS (SELECT 1 FROM pg_proc p,
       LATERAL aclexplode(coalesce(p.proacl, acldefault('f', p.proowner))) a
       WHERE p.oid=f AND a.grantee=0 AND a.privilege_type='EXECUTE') THEN
    RAISE EXCEPTION 'Function EXECUTE remains available through a revoked role';
  END IF;
  IF NOT EXISTS (
    SELECT 1 FROM pg_event_trigger
    WHERE evtname = 'ensure_rls'
      AND evtfoid = f
      AND evtenabled IN ('O', 'A')
  ) THEN
    RAISE EXCEPTION 'ensure_rls trigger was not preserved';
  END IF;
END
$verify$;

COMMIT;
```

The postflight must compare the retained preflight function identity,
`proconfig`, owner, trigger binding, and all unrelated ACL rows. A mismatch
requires rollback and investigation, not a broadened grant.

## Rollback

Rollback during the same session is a normal `ROLLBACK`. For a later
rollback, use the exact retained `proacl` from preflight and restore it with
`REVOKE ALL ON FUNCTION public.rls_auto_enable() FROM PUBLIC, anon,
authenticated` followed by explicit `GRANT` statements generated from the
captured ACL entries (including grant options). Verify the function OID,
owner, `SECURITY DEFINER`, `proconfig`, trigger binding, and ACL equality
against the preflight output before committing the rollback. Never use
`GRANT EXECUTE ... TO PUBLIC` as a generic restoration shortcut.

## Local evidence

The supplied disposable PostgreSQL 17 procedure passed with effective
`EXECUTE` denied for `PUBLIC`, `anon`, and `authenticated`; the enabled
`ensure_rls` trigger remained bound to the function and automatically enabled
RLS on a synthetic table. No hosted database was contacted. The candidate
does not prove production authorization or provider configuration.

created by stokonn with love <3

## 2026-10-24 audit correction / executable review candidate

`PUBLIC` is a PostgreSQL pseudo-role, not a catalog role suitable for the
three-argument `has_function_privilege` call. The earlier PUBLIC assertion
would fail; the corrected example above checks `aclexplode` grantee zero.
Earlier local evidence is retained as supplied historical evidence; it does
not establish execution of the formerly printed SQL. The new independently
executable candidate is `spikes/stage6-cloud-readiness/acl-fix.candidate.sql`.
It guards observed owner/search_path/tags and compares function definition,
OID, trigger identity and unrelated grants before commit. No hosted fix applied.

Current read-only audit: owner postgres, SECURITY DEFINER,
search_path=pg_catalog, default (NULL) proacl, ensure_rls enabled O with
CREATE TABLE/CREATE TABLE AS/SELECT INTO tags; two Advisor warnings confirmed.
Function catches DDL errors and logs them; private-schema RLS is therefore
explicitly enabled rather than relying on this public-only event trigger.

Rollback before commit preserves exact catalog state including NULL proacl.
After commit, restoring captured default grants restores effective privileges
but not necessarily the NULL-versus-explicit-ACL representation; never update
pg_proc directly to force equality. Any later ACL restoration must be explicitly
reviewed against retained preflight evidence. Existing data stays in place.
