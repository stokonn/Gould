# Stage 6 — Supabase Security & Auth Readiness Audit (read-only)

**2026-10-22; base 4f0cd51; documentation/research only.** Purpose: assess two
Security Advisor findings without confusing broad EXECUTE grants with a demonstrated
exploit; prepare a catalog/configuration-only audit of Auth, RLS and API exposure.
Scope: existing Stage 6 documentation, public official sources and a proposed single
Issue #31 C4 sentence. No hosted SQL or configuration operation executed by this agent.

## Stage checklist and progress

- [x] Recover current branch/worktrees, latest Issue/comments and preserved gates.
- [x] Record owner-supplied catalog findings with their provenance and limitations.
- [x] Review current official Supabase guidance and PostgreSQL 17 documentation/source.
- [x] Separate direct-call restrictions from indirect DDL-event risk; propose conditional least-privilege hardening.
- [x] Prepare the read-only Auth/RLS/API verification checklist and catalog queries below.
- [x] Run documentation/policy checks and record actual results.
- [x] Obtain approval, publish only the single C4-sentence Issue update and verify exact body/unchanged metadata.

Documentary analysis/verification COMPLETE; hosted audit remains UNEXECUTED. Hosted checklist items stay
unchecked until independently executed evidence is available. This is not Stage 6
acceptance, a vulnerability exploit test, or authorization for any corrective write.

## 1. Evidence ledger (facts versus unknowns)

| Item | Evidence and scope |
|---|---|
| Two Security Advisor warnings concern public.rls_auto_enable() | Supplied in the owner's current handoff; raw warning IDs/text/version not supplied. Do not invent their IDs or mark resolved. |
| SECURITY DEFINER true; owner postgres | Owner-supplied read-only catalog observation, not a check executed by this agent. Effective owner role attributes/delegated capabilities still require inspection. |
| EXECUTE permitted to anon and authenticated | Owner-supplied effective-access observation. Explicit versus PUBLIC/inherited grants and grant options have not been identified. |
| Return type event_trigger; enabled DDL event trigger ensure_rls | Owner-supplied catalog observation. Exact function OID/signature/language, trigger-to-function binding, event/tags/enabled mode and all dependencies still need recorded metadata. |
| No public application tables | Owner-supplied observation; earlier external ChatGPT integration inventory also reported 0 public tables. Does not prove all schemas empty, no functions exposed, or policy correctness for future tables. |
| Existing project Free/tier_free, eu-central-1, ACTIVE_HEALTHY, PostgreSQL 17.11; 0 recorded migrations/Edge Functions | Previously recorded external ChatGPT connected-integration verification in C4 §H. Not re-executed here; this agent has no callable Supabase tools in this session. |
| Function body, function-local search_path, language, overloads, exposed API configuration, wrappers and actual DDL rights | UNVERIFIED. The function name is not proof of provenance or equivalence to a published example. |
| Auth/security flags, payment methods, billing/usage and direct DB connectivity from this agent | UNVERIFIED. No secret-bearing settings, auth/user records, credentials or data rows requested. |

## 2. Risk analysis against official sources

**Conclusion: excess-privilege/privileged-hook finding requiring review; direct
anon/authenticated privilege escalation is NOT demonstrated. Not classified as an
exploitable vulnerability or dismissed as a false positive.**

Supabase [Advisors](https://supabase.com/docs/guides/observability/advisors) are
static/deterministic checks and require context. Current upstream [lint 0028](https://raw.githubusercontent.com/supabase/splinter/main/lints/0028_anon_security_definer_function_executable.sql)
and [lint 0029](https://raw.githubusercontent.com/supabase/splinter/main/lints/0029_authenticated_security_definer_function_executable.sql)
check SECURITY DEFINER, effective role EXECUTE and exposed-schema membership; neither
SQL predicate excludes event_trigger return types. Those rules explain a possible
warning mechanism, not the unprovided actual warning IDs or deployed linter version.
Their RPC wording does not prove that this particular function body can execute.
The query-parameter advisor pages rendered unrelated default detail, so the primary
lint SQL above is used for the rule analysis instead of that rendered detail.

### Direct invocation is different from trigger execution

PostgreSQL describes event_trigger as a special callback return type. For the
PL/pgSQL language used in Supabase's published example, the [PostgreSQL 17 compiler](https://raw.githubusercontent.com/postgres/postgres/REL_17_STABLE/src/pl/plpgsql/src/pl_comp.c)
rejects an ordinary call returning event_trigger with SQLSTATE 0A000: trigger
functions can only be called as triggers. An EXECUTE ACL does not manufacture event
context. This blocks the ordinary direct-call route **if the deployed function is
PL/pgSQL**; its actual language is still unknown. Other language implementations
must be evaluated on their own semantics. PostgREST/GraphQL exposure, wrapper paths
and installed versions are also unknown. No direct SELECT/RPC/GraphQL invocation
was attempted: an apparent read request can execute privileged function code.

### Creation rights do not determine who can fire an existing hook

[PostgreSQL CREATE EVENT TRIGGER](https://www.postgresql.org/docs/17/sql-createeventtrigger.html)
restricts creation to superusers. Supabase's [event-trigger guide](https://supabase.com/docs/guides/database/postgres/event-triggers)
explains its Supautils delegation to postgres; owner name does not prove rolsuper.
But [event-trigger behavior](https://www.postgresql.org/docs/17/event-trigger-definition.html)
allows an existing hook to fire on matching DDL, including DDL performed by a
non-superuser with suitable rights. Thus “anon cannot create an event trigger”
alone is not a complete risk argument. Inspect effective schema/database CREATE,
object ownership, role memberships and any privileged DDL wrapper. No DDL probe is
permitted by this audit.

The [PG17 dispatcher](https://raw.githubusercontent.com/postgres/postgres/REL_17_STABLE/src/backend/commands/event_trigger.c)
invokes registered callbacks via the event context; its event invocation path does
not use the ordinary caller EXECUTE ACL as the firing gate. Consequently a future
ACL reduction should not be presented as preventing an existing trigger from firing.
Actual managed-runtime compatibility remains untested. A dangerous function body,
untrusted name resolution, unsafe dynamic SQL or attacker-controlled DDL objects
can remain a concern even when direct calls are rejected. No public tables today
reduces present application-table exposure; it does not prove this DDL path safe.

### Security intent and safest recommendation

Supabase publishes an RLS auto-enable example with the same function/trigger names,
SECURITY DEFINER, a pinned pg_catalog search path, bounded public-table targets and
DDL tag filtering. This supports a legitimate protective purpose, not proof that
the live body matches it. Disabling/removing ensure_rls merely to clear a warning
could remove an RLS guard for future tables.

**No corrective mutation is justified automatically from the supplied facts.**
First collect the missing catalog/definition properties read-only. If the function
is confirmed to be an internal callback with no intended API callers, the preferred
future hardening candidate is a **function-specific removal of unnecessary EXECUTE
from PUBLIC and direct anon/authenticated grants**, reviewing inherited access too.
Retain legitimate event-trigger bindings, required owner privileges and necessary
internal dependencies; do not blanket-revoke every schema function or change
project-wide defaults in this pass. Any inherited granting role needs a separate
scope/impact review, not indiscriminate revocation.

[Supabase function guidance](https://supabase.com/docs/guides/database/functions)
recommends least privilege and a controlled search path for definer functions.
[PostgreSQL safe definer guidance](https://www.postgresql.org/docs/17/sql-createfunction.html#SQL-CREATEFUNCTION-SECURITY)
requires excluding untrusted object resolution, including temporary-object traps.
If inspection finds an unsafe search path/body, design a narrow correction with
qualified references and safe dynamic identifier handling; do not infer that this
finding exists merely because search_path has not yet been supplied. Do not switch
to SECURITY INVOKER or change owner as an automatic fix: that may break the guard.
All ACL/body/path changes require separate explicit write authorization, reviewed
compatibility evidence and a rollback plan; none is authorized or performed here.

## 3. Read-only verification checklist (NOT EXECUTED on hosted project)

Use an existing authorized connection with catalog/configuration read scope. Return
only allowlisted metadata/booleans, no keys, passwords, auth records, tokens, user
logs or complete management-config dumps. Keep account identifiers private. Review
function source privately for embedded secrets before returning sanitized structural
facts; do not paste arbitrary function definitions into GitHub or the model.

### A. Function / event-trigger / effective-role metadata — first priority

- [ ] Fetch sanitized advisor IDs/text/object identity and observation date; establish exactly which two checks fired.
- [ ] Confirm function OID/signature/language/prosecdef/owner and all same-name overloads; inspect only the function-local search_path setting, not all proconfig values.
- [ ] Confirm ensure_rls binding OID, event/tags and enabled mode; inventory other references/wrappers and managed-extension ownership/dependencies without changing them.
- [ ] Resolve PUBLIC, direct and inherited EXECUTE; inspect anon/authenticated schema/database CREATE and effective role/owner capabilities, without reading pg_authid or passwords.
- [ ] Have a privileged reviewer inspect the body privately: bounded target schemas/tags/object kinds, qualified lookup, trusted name resolution including pg_temp, safe identifier formatting and no arbitrary privileged DDL/data access. Return only sanitized findings; function name alone is insufficient.
- [ ] Review whether any untrusted role can cause a matching DDL event or influence its inputs; no CREATE TABLE, SELECT INTO, test migration, function call or RPC probe.

### B. Auth flags and admin access

- [ ] Read enabled-provider booleans only, including Google/email/anonymous sign-in; do not request provider secrets/client keys. Hosted Google settings remain UNVERIFIED until read.
- [ ] Check signup/anonymous-signup and email-confirmation flags, password/OTP policy, rate-limit/CAPTCHA status and feature availability on Free; do not send email or create/sign in users.
- [ ] Review Site URL and redirect allowlist privately for unexpected/wildcard destinations; publish only a sanitized assessment. Do not configure OAuth.
- [ ] Read session/JWT expiry, refresh rotation/reuse and relevant security flags only; no signing secret, access/refresh token or auth-session record. Runtime revocation/rotation tests remain separate gated work.
- [ ] Confirm operator/org MFA enforcement/availability through sanitized flags, distinct from application-user MFA; no enrollment, invitations or role changes. Paid-only capabilities become blockers, never upgrades.

References: [Auth general configuration](https://supabase.com/docs/guides/auth/general-configuration),
[redirect URLs](https://supabase.com/docs/guides/auth/redirect-urls),
[operator MFA](https://supabase.com/docs/guides/platform/multi-factor-authentication),
[production checklist](https://supabase.com/docs/guides/deployment/going-into-prod).
These pages supply a review checklist; their mutation/setup examples are NOT executed.

### C. RLS, grants and API surfaces

- [ ] Read schema/table/view/materialized/foreign-table inventory metadata and RLS/force-RLS flags; inspect policy definitions privately and report sanitized role/command coverage, never application rows.
- [ ] Confirm the current zero-public-table inventory. With no application tables, table-policy coverage is not evidence that future application policies pass; other schemas remain in scope for metadata review.
- [ ] Inspect table/sequence/function grants and per-creator/global default ACLs, including PUBLIC and inherited rights, without changing them. RLS does not gate function EXECUTE.
- [ ] Read Data API enabled state, exposed schemas and relevant search-path settings only; inventory security-definer/invoker RPC and API views and any enabled GraphQL/Realtime/Storage exposure as metadata, not record reads.
- [ ] Check SSL enforcement and network-restriction flags as available on Free; do not run endpoint/RPC/auth probes or change restrictions. Assess key-handling architecture without retrieving any keys.
- [ ] Compare metadata to AUTH.md/THREAT-MODEL.md and the permanent 0-CZK plan; preserve all unverified integration/runtime rows and the collection embargo.

References: [Data API security](https://supabase.com/docs/guides/api/securing-your-api),
[RLS](https://supabase.com/docs/guides/database/postgres/row-level-security).

## 4. Catalog-only query examples — prepared, NOT EXECUTED

These SELECTs never call the project function, perform DDL or read user tables.
Use an existing read-only connection/transaction; keep raw identifiers in private
notes. Permission denial is UNVERIFIED, not proof of absent objects or safety.
Do not broaden a failing query's privileges or install tooling/extensions.

```sql
-- A1: exact candidate metadata; no raw source or arbitrary config values.
SELECT p.oid AS function_oid,
       pg_catalog.pg_get_function_identity_arguments(p.oid) AS arguments,
       l.lanname AS language,
       p.prosecdef AS security_definer,
       pg_catalog.pg_get_userbyid(p.proowner) AS owner_role,
       pg_catalog.format_type(p.prorettype, NULL) AS return_type,
       (SELECT setting FROM pg_catalog.unnest(p.proconfig) AS setting
        WHERE setting LIKE 'search_path=%' LIMIT 1) AS function_search_path,
       pg_catalog.has_function_privilege('anon', p.oid, 'EXECUTE') AS anon_execute,
       pg_catalog.has_function_privilege('authenticated', p.oid, 'EXECUTE') AS authenticated_execute
FROM pg_catalog.pg_proc AS p
JOIN pg_catalog.pg_namespace AS n ON n.oid = p.pronamespace
JOIN pg_catalog.pg_language AS l ON l.oid = p.prolang
WHERE n.nspname = 'public' AND p.proname = 'rls_auto_enable'
  AND p.prokind = 'f';

-- A2: registered event/binding; metadata only.
SELECT e.evtname, e.evtevent, e.evtenabled, e.evttags,
       e.evtfoid AS function_oid,
       pg_catalog.pg_get_userbyid(e.evtowner) AS trigger_owner
FROM pg_catalog.pg_event_trigger AS e
WHERE e.evtname = 'ensure_rls';

-- A3: limited effective privileges; no SET ROLE or DDL test.
SELECT r.rolname, r.rolsuper, r.rolinherit, r.rolcreaterole,
       r.rolcreatedb, r.rolbypassrls,
       pg_catalog.has_schema_privilege(r.rolname, 'public', 'CREATE') AS public_create,
       pg_catalog.has_database_privilege(r.rolname, pg_catalog.current_database(), 'CREATE') AS database_create
FROM pg_catalog.pg_roles AS r
WHERE r.rolname IN ('anon', 'authenticated', 'postgres');
```

A1 effective booleans do not identify grant provenance; a separate sanitized ACL
and membership read is still necessary. A null local search_path means no local
pin was found, not a proof of exploitable lookup. Database CREATE and public CREATE
do not alone enumerate every way to cause DDL (other schemas, object ownership,
wrappers and temporary objects still require contextual review).

## 5. Acceptance, unknowns and independently authorized next step

Next concrete action already within the owner's READ-ONLY authorization: run A1–A3
through the existing authorized Supabase metadata connection, then obtain sanitized
ACL provenance, event binding and private source-review facts. This session exposes
no Supabase tools, so that hosted step is NOT EXECUTED here. It needs tool access,
not write authorization, and never requires sharing credentials with the agent.
Unknown raw advisor IDs, language/body/path, effective DDL rights, API/Auth flags,
billing/payment-method evidence and other C4 legal/transfer prerequisites remain
open. No claim of no risk, verified exploitation, fixed warnings or public readiness.

**Unchanged gates:** Q1/D5b UNRESOLVED/blocking; C4 OPEN; C2 §12 OPEN; 6.0f
UNCHECKED; 6.0d/6.0e NOT AUTHORIZED; 6.1–6.18 UNCHECKED; ORD-A sequencing
only; 0 CZK/month; real-user collection NOT AUTHORIZED. Stage 7 unchanged.

created by stokonn with love <3


## 6. Executed verification and publication (2026-10-22)

- Public official guidance/source review executed; the source links above are the
  references actually retrieved. Supabase hosted checks are owner-supplied findings,
  not runtime checks executed by this agent.
- Five existing host commands PASS: `python3 scripts/check_agent_policy.py`,
  `check_backup_fixtures.py` (3 fixtures), `check_history_paging.py` (106 sessions/4
  pages), `check_localization.py` (922 EN/CZ resources), `check_migrations.py`
  (v1–9→10 DDL). These do not verify hosted Auth/RLS/API behavior.
- Markdown/local links, nine-file documentation-only scope, original checkbox
  preservation and unchanged non-Stage-6 sections PASS; git diff --check PASS.
- Prepared SQL: three catalog SELECTs, no project-function call or write statement
  in static review. An initial ad-hoc safety check falsely treated the privilege
  string literal 'CREATE' as a command; corrected literal-aware check PASS. SQL was
  NOT executed or runtime-validated; no claim of a passed hosted negative test.
- Owner approved the one-sentence C4 update; exact published content verified, all
  63 full comment records/Issue metadata and Project In Progress unchanged. No new
  checkpoint comments, Stage 7 changes, cloud writes, grants, event-trigger change,
  OAuth flow, app builds, screenshots or repeated PG/SQLite concurrency tests.

Current [Issue #31](https://github.com/stokonn/Goldberg/issues/31) baseline: 9,442 Unicode characters/52 lines,
verified 2026-10-22T19:56:59Z; UTF-8 SHA-256
`2abc94feba38ded15eccb75b9bc61c884dff3c6d973e432db2190c23e9cb50dc`.
