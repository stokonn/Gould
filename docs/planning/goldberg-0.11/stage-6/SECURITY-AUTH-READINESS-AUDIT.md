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

Documentary analysis/verification COMPLETE. A1–A3 are now EXECUTED externally by
ChatGPT’s connected Supabase integration, as relayed by the owner on 2026-10-22.
The wider hosted Auth/RLS/API audit remains INCOMPLETE; only the evidenced portions
of the checklist below are checked. This agent did not execute hosted queries. This is not Stage 6 acceptance, a vulnerability exploit test, or authorization for any corrective write.

## 1. Evidence ledger (facts versus unknowns)

| Item | Evidence and scope |
|---|---|
| Two Security Advisor warnings concern public.rls_auto_enable() | Supplied in the owner's current handoff; raw warning IDs/text/version not supplied. Do not invent their IDs or mark resolved. |
| A1 — EXECUTED externally | Function OID 17603; plpgsql; SECURITY DEFINER true; owner postgres; return event_trigger; function-local search_path pg_catalog; effective EXECUTE anon=true and authenticated=true. ChatGPT connected Supabase integration; owner-relayed results, not executed by this agent. |
| A2 — EXECUTED externally | ensure_rls; ddl_command_end; enabled O (normal mode); tags CREATE TABLE, CREATE TABLE AS, SELECT INTO; function OID 17603 matches A1; trigger owner postgres. Same external provenance. |
| A3 — EXECUTED externally | anon/authenticated: superuser, CREATEDB, CREATEROLE, BYPASSRLS, public-schema CREATE and database CREATE all false. postgres: rolsuper=false, rolbypassrls=true, public CREATE=true, database CREATE=true. Same external provenance. Unreported rolinherit values and postgres CREATEDB/CREATEROLE are not inferred. |
| Additional ACL observations | Externally reported catalog inspection found default function privileges including PUBLIC EXECUTE; no explicit separate anon/authenticated grants identified. Inherited-role paths, grant options and broader default ACL inventory remain unverified. Effective EXECUTE alone did not identify this provenance. |
| Partial structural source review | External read-only checks found DDL-event filtering, schema checks, quoted identifiers and RLS enablement patterns. This is not a complete source/qualification/dependency audit or a proof of safety; provenance equivalence to a published example is not established. |
| No public application tables | Existing external inventory/owner observation; does not prove all schemas empty, no functions exposed, or future policy correctness. No application records accessed. |
| Existing project Free/tier_free, eu-central-1, ACTIVE_HEALTHY, PostgreSQL 17.11; 0 recorded migrations/Edge Functions | Previously recorded external ChatGPT connected-integration verification in C4 §H. Not re-executed here; this agent has no callable Supabase tools in this session. |
| Complete body review, overload inventory, exposed API configuration, wrappers/dependencies and remaining DDL paths | UNVERIFIED. Confirmed metadata above narrows the earlier unknowns; it does not establish these broader properties. |
| Auth/security flags, payment methods, billing/usage and direct DB connectivity from this agent | UNVERIFIED. No secret-bearing settings, auth/user records, credentials or data rows requested. |

All three queries succeeded according to the owner’s external ChatGPT/Supabase
execution report. No database writes or function/RPC invocation occurred. Raw
advisor IDs/version, complete result dumps and a source digest were not supplied.
No vulnerability was demonstrated or repaired; both warnings remain recorded.

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
context. A1 now confirms the deployed function is PL/pgSQL, so this source-based
restriction applies to its ordinary direct-call route. This is an inference from
confirmed metadata and PostgreSQL semantics, not a hosted negative invocation test.
PostgREST/GraphQL exposure, wrapper paths and installed API versions remain unknown.
No direct SELECT/RPC/GraphQL invocation
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
permitted by this audit. A3 confirms those two API roles lack the six reported
role/CREATE capabilities, narrowing their direct DDL path in public/the database.
It does not inventory other schemas, temporary-object rights, object ownership,
role memberships or privileged wrappers. The postgres owner is not a superuser
but does have BYPASSRLS and the reported CREATE rights; the hook remains privileged.

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
Continue the remaining source/dependency and inherited-access review read-only;
do not rerun completed A1–A3 without a concrete reason. If the function is confirmed
to be an internal callback with no intended API callers, the preferred future
hardening candidate is **function-specific removal of unnecessary PUBLIC EXECUTE**,
subject to compatibility review and separate write authorization. Default PUBLIC
access was observed; separate anon/authenticated grants were not identified. Do
not prescribe removal of nonexistent direct grants; review any additional grants
only if actually found, including inherited access.
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
finding exists: A1 confirms the pinned pg_catalog search path; structural checks
alone do not settle all body qualification/temporary-object questions. Do not switch
to SECURITY INVOKER or change owner as an automatic fix: that may break the guard.
All ACL/body/path changes require separate explicit write authorization, reviewed
compatibility evidence and a rollback plan; none is authorized or performed here.

## 3. Read-only verification checklist (A1–A3 externally EXECUTED; remainder OPEN)

Use an existing authorized connection with catalog/configuration read scope. Return
only allowlisted metadata/booleans, no keys, passwords, auth records, tokens, user
logs or complete management-config dumps. Keep account identifiers private. Review
function source privately for embedded secrets before returning sanitized structural
facts; do not paste arbitrary function definitions into GitHub or the model.

### A. Function / event-trigger / effective-role metadata — first priority

- [ ] Fetch sanitized advisor IDs/text/object identity and observation date; establish exactly which two checks fired.
- [x] A1 externally EXECUTED: record OID/language/prosecdef/owner/return type, function-local search_path and effective anon/authenticated EXECUTE for the reported function.
- [x] A2 externally EXECUTED: record ensure_rls matching binding OID, event/tags, enabled mode and owner.
- [x] A3 externally EXECUTED: record the supplied API-role capabilities/CREATE rights and postgres owner attributes; do not infer unreported values.
- [x] Record external default-ACL PUBLIC EXECUTE and no identified separate anon/authenticated grants, plus partial structural body-check findings.
- [ ] Complete signature/same-name overload inventory and other references/wrappers/managed-extension dependency review.
- [ ] Complete inherited-role/grant-option and other-schema/temporary-object/ownership capability review without reading pg_authid or passwords.
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

## 4. Catalog-only queries — EXECUTED externally by ChatGPT; retained for reference

The owner reports all three prepared SELECTs below succeeded through ChatGPT’s
connected Supabase integration. They were not executed by this coding agent and
are retained without modification; no rerun is required.
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

A1 effective booleans alone do not identify grant provenance. Additional external
ACL inspection reported default PUBLIC EXECUTE; inherited memberships still need
review. The observed local search_path is pg_catalog, not null.
Database CREATE and public CREATE
do not alone enumerate every way to cause DDL (other schemas, object ownership,
wrappers and temporary objects still require contextual review).

## 5. Acceptance, unknowns and independently authorized next step

Next concrete action within the owner’s existing READ-ONLY authorization: obtain
sanitized advisor IDs, remaining inherited-role/DDL-path and dependency metadata,
and finish private function-body review through the existing authorized Supabase
connection. Then read allowlisted Auth/provider/redirect/session and API exposure
flags from §3 B/C without keys, user records, endpoint probes or configuration
changes. Do not repeat A1–A3. This session exposes no callable Supabase tools; any
unavailable result stays UNVERIFIED, without requesting secrets or broader access.
Billing/payment-method evidence and other C4 legal/transfer prerequisites remain
open. No claim of no risk, verified exploitation, fixed warnings or public readiness.

**Unchanged gates:** Q1/D5b UNRESOLVED/blocking; C4 OPEN; C2 §12 OPEN; 6.0f
UNCHECKED; 6.0d/6.0e NOT AUTHORIZED; 6.1–6.18 UNCHECKED; ORD-A sequencing
only; 0 CZK/month; real-user collection NOT AUTHORIZED. Stage 7 unchanged.

created by stokonn with love <3


## 6. Initial documentary verification/publication (2026-10-22, before external A1–A3)

Historical checkpoint: its hosted NOT EXECUTED statements describe this agent’s
initial session, superseded for external execution by §1 and §7.

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

Initial [Issue #31](https://github.com/stokonn/Goldberg/issues/31) baseline: 9,442 Unicode characters/52 lines,
verified 2026-10-22T19:56:59Z; UTF-8 SHA-256
`2abc94feba38ded15eccb75b9bc61c884dff3c6d973e432db2190c23e9cb50dc`.


## 7. External execution continuation (2026-10-22; base bac81fb)

- [x] Record A1–A3 EXECUTED with external ChatGPT/Supabase provenance and exact supplied metadata in §1.
- [x] Separate the partial structural/ACL observations from remaining full source/inherited-access review.
- [x] Preserve both advisor warnings, every Stage 6 gate and the read-only boundary.
- [x] Update the Stage 6 checklists and durable handoff; verify documentation using existing host checks.

No writes, function/RPC invocation, demonstrated/repaired vulnerability, cloud
changes, OAuth configuration, application implementation or Stage 7 modification.
No completed PostgreSQL/SQLite concurrency suite was rerun. Future permissions
changes require separate authorization and compatibility review; ensure_rls is
preserved. Issue publication/baseline and exact local verification are recorded
in docs/ai/VERIFICATION.md in the containing continuation commit.

Owner-approved minimal A1–A3 sentence published and verified exactly; current Issue
baseline: 9,564 Unicode characters/52 lines, 2026-10-22T20:09:06Z, UTF-8 SHA-256
`b3a49d6bf39b7181fd675765d3d5d8fff16d04f637ced2bddc69494c4df48549`.
All 63 comments, metadata and Project In Progress preserved; no new comment.
