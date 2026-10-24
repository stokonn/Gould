## 2026-10-24 — durable backup-key resilience drill

The hardened local harness was rerun against fresh disposable rootless
PostgreSQL 17.11 containers. GnuPG symmetric AES-256 with MDC integrity
protection, isolated temporary `GNUPGHOME` directories, external mode-600
keys, separate export/restore processes, atomic publication and SHA-256
manifests were exercised.

Result: **PASS / exit 0**. Export **3,915 ms**, restore **4,341 ms**, rotation
export **4,009 ms**, total **30,033 ms**. Wrong-key, missing-key, partial and
tampered artifacts were rejected; v2 rotation succeeded and the older v1
backup restored. Schema, synthetic row counts (2 metadata, 2 submissions,
2 audit events) and audit-chain integrity passed. No temporary plaintext,
`.part`, tampered artifact, matching container or user GnuPG state remained.

This proves only isolated synthetic recovery and keyed tamper detection. It
does not establish production backup authenticity, destination durability,
key disaster recovery, scheduling, retention, hosted restore, RPO/RTO or
legal/compliance readiness. C4 remains **8 PASS / 19 PARTIAL / 6 OPEN** and
6.0f remains open.

created by stokonn with love <3

# 2026-10-24 — synthetic encrypted backup and restore drill

The local harness `spikes/stage6-backup-local/run-local.sh` was added and
executed against a fresh disposable PostgreSQL 17.11 container. It created
only synthetic metadata, submission and two-row audit-chain fixtures. The
custom-format logical dump was encrypted with a temporary OpenSSL key; a
SHA-256 manifest rejected deliberate ciphertext tampering; the decrypted
artifact restored into a fresh database.

Final result: **PASS / exit 0**. Export elapsed **368 ms**, restore elapsed
**997 ms**, total elapsed **4,354 ms**. Restored counts matched (2 metadata,
2 submissions, 2 audit events), normalized schema dumps matched, and audit
hash-chain/head recomputation passed. The key, plaintext/decrypted dumps and
tampered copy were removed; the encrypted artifact, manifest and evidence
remain private session files. No production endpoint, secret or real record
was used.

This is a synthetic local drill, not proof of production backup readiness or
RPO/RTO. Owner-controlled destination, key custody/recovery, daily schedule,
retention, hosted restore and failure alerting remain open under
`ZERO-COST-OPS.md`.

created by stokonn with love <3

# 2026-10-24 — PostgreSQL 17 disposable verification completed

**Status: ISOLATED VERIFICATION PASSED / NOT PRODUCTION APPROVAL.**

The owner-authorized official image pull and the supplied
`run_disposable_pg17.sh` completed successfully. Podman was rootless; the
image was `docker.io/library/postgres:17`, PostgreSQL **17.11**, amd64,
manifest digest
`sha256:3518071376d0b6156b02e1db2747a78511b5b9dc3763cc79c506ebe2dc2ee71c`
and local image ID
`82903352d6faf8f8a318bc4353d1cb1db67d4a5ff968d2799b2718e2cb00afa2`.
The manifest identified the official source as
`https://github.com/docker-library/postgres.git` revision
`2603e26e245e558218728ee14e0a42dcb020dc7f`, path `17/trixie`.

## Acceptance result

The unchanged supplied runner exited **0**:

```text
PASS — public EXECUTE revoked; ensure_rls preserved; synthetic CREATE TABLE got RLS
PASS: isolated PostgreSQL 17 privilege and event trigger acceptance test.
```

Its three SQL stages all succeeded. The candidate revoked effective function
`EXECUTE` for `anon` and `authenticated`, retained the enabled `ensure_rls`
event trigger, and a synthetic `public` table automatically received RLS.
The synthetic table transaction rolled back. A separate final detail run
reported explicit effective privileges `f|f|f` for `anon`, `authenticated`
and `PUBLIC`, an ACL scan result of `f` for a PUBLIC `EXECUTE` entry,
`postgres=X/postgres` as the remaining function ACL, and
`ensure_rls|O|rls_auto_enable()`. PostgreSQL server logs show PostgreSQL
17.11 started and became ready to accept connections without error.

The unchanged runner's full output is preserved outside the repository at
`/home/stokonn/.copilot/session-state/3a888b7e-8982-45f3-b923-86283dda25eb/files/stage6-acl-pg17-run.log`.
The detailed final ACL/log output is at
`stage6-acl-pg17-detail-final.log` in the same private session directory.
Two earlier auxiliary detail captures stopped with exit 1 because of
diagnostic-query qualification/cast errors; they did not indicate a failure
of the candidate SQL. Their raw logs remain preserved, and the corrected
detail run passed.

Podman showed no remaining matching container or volume after cleanup. No
hosted Supabase connection, production migration, real data or cloud resource
was used.

**Conclusion:** the candidate is verified in the synthetic PostgreSQL 17
fixture and no unexpected authorization change was observed. Hosted
compatibility, migration approval, rollback planning and the existing
broader Stage 6 gates remain separate and open.

created by stokonn with love <3

# 2026-10-24 — supplied ACL candidate review and isolated PG17 verification

**Status: REVIEWED / POSTGRESQL VERIFICATION BLOCKED / NOT APPROVED FOR HOSTED DEPLOYMENT.**

The extracted review package was inspected at
`$XDG_DOWNLOAD_DIR/stage6-review/stage6_acl_review` with
`$XDG_DOWNLOAD_DIR=/home/stokonn/Stažené`. `README.md`, `BACKUP-REVIEW.md`,
`run_disposable_pg17.sh`, `01_bootstrap_disposable.sql`,
`02_candidate_privilege_hardening.sql` and `03_verify_disposable.sql` were
read. The package is explicitly candidate-only and contains no authorization to
change the hosted Supabase project.

## Candidate review

`02_candidate_privilege_hardening.sql` is narrowly scoped: it aborts unless
`public.rls_auto_enable()` is a `SECURITY DEFINER` event-trigger function and
the enabled `ensure_rls` trigger is still bound to that exact function OID. It
then revokes function-specific `EXECUTE` from `PUBLIC`, `anon` and
`authenticated`, and verifies effective execution denial for both API roles.
It does not drop, replace or disable the event trigger, change ownership,
`SECURITY DEFINER`, `search_path`, schema defaults or unrelated privileges.
The explicit `anon`/`authenticated` revokes are harmlessly redundant when the
access is inherited only from `PUBLIC`; the effective-privilege assertions are
the material checks.

The candidate therefore addresses the observed excessive effective
`EXECUTE` privilege without an apparent unrelated authorization change.
However, the supplied scripts do not independently exercise every indirect DDL
or delegated authorization path, and no hosted compatibility evidence exists.
The candidate remains **CONDITIONALLY ACCEPTABLE FOR DISPOSABLE TESTING ONLY**;
no live migration or production approval is implied.

## Actual isolated verification attempt

The exact supplied command was run from the extracted package:

```text
(cd /home/stokonn/Stažené/stage6-review/stage6_acl_review && ./run_disposable_pg17.sh)
```

Podman was available, but `docker.io/library/postgres:17` was not already
cached. The runner correctly refused an automatic pull and exited **2**:

```text
BLOCKED: Image docker.io/library/postgres:17 is not already cached. No automatic image pull attempted.
exit_code=2
```

No container was created, no SQL script ran, no pass/fail assertions were
executed, and no live or hosted database was contacted. The raw command log is
preserved outside the repository at
`/home/stokonn/.copilot/session-state/3a888b7e-8982-45f3-b923-86283dda25eb/files/stage6-acl-pg17-run.log`.
The earlier wrong-directory probe in that log is retained as raw evidence; the
package-directory attempt is the authoritative result.

## Backup/recovery review

`BACKUP-REVIEW.md` is consistent with `ZERO-COST-OPS.md`: daily encrypted
logical exports, owner-controlled private storage, SHA-256 manifests, target
RPO <=24 hours and isolated restore verification remain design requirements.
Destination, encryption/decryption key handling, scheduler, real export,
restore drill and measured RTO remain unverified or unimplemented. No real
records were exported and no scheduler or production backup was activated.

**Stage 6 consequence:** ACL hardening is not PostgreSQL-verified; the event
trigger's continued behavior and the no-escalation assertion remain OPEN.
Backup readiness remains DESIGN ONLY. C4 stays 8 PASS / 19 PARTIAL / 6 OPEN,
6.0f stays unchecked, and no Stage 6 completion or hosted security change is
claimed.

created by stokonn with love <3

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
Additional inventories and live evidence are externally EXECUTED (owner-relayed
2026-10-22; §8/§9). Catalog evidence reconciliation is complete for this snapshot;
remaining work is a bounded Auth/API configuration review and approval decisions.
The wider hosted Auth/RLS/API audit remains INCOMPLETE; only the
evidenced portions of the checklist below are checked. This agent did not execute
hosted queries. This is not Stage 6 acceptance, a vulnerability exploit test, or
authorization for any corrective write.

## 1. Evidence ledger (facts versus unknowns)

| Item | Evidence and scope |
|---|---|
| Security Advisor rerun — EXECUTED externally | anon_security_definer_function_executable: WARN; authenticated_security_definer_function_executable: WARN. Both target rls_auto_enable and remain unresolved. Exact check names are now externally observed, not inferred from upstream rules; full warning payload/linter version not supplied. |
| A1 — EXECUTED externally | Function OID 17603; plpgsql; SECURITY DEFINER true; owner postgres; return event_trigger; function-local search_path pg_catalog; effective EXECUTE anon=true and authenticated=true. ChatGPT connected Supabase integration; owner-relayed results, not executed by this agent. |
| A2 — EXECUTED externally | ensure_rls; ddl_command_end; enabled O (normal mode); tags CREATE TABLE, CREATE TABLE AS, SELECT INTO; function OID 17603 matches A1; trigger owner postgres. Same external provenance. |
| A3 — EXECUTED externally | anon/authenticated: superuser, CREATEDB, CREATEROLE, BYPASSRLS, public-schema CREATE and database CREATE all false. postgres: rolsuper=false, rolbypassrls=true, public CREATE=true, database CREATE=true. Same external provenance. Unreported rolinherit values and postgres CREATEDB/CREATEROLE are not inferred. |
| Additional ACL observations | Externally reported catalog inspection found default function privileges including PUBLIC EXECUTE; no explicit separate anon/authenticated grants identified. A subsequent direct-membership inventory returned no rows for anon/authenticated; broader privilege paths, grant options and default ACL inventory remain unverified. Effective EXECUTE alone did not identify this provenance. |
| Private deployed definition / structural review — EXECUTED externally | ChatGPT retrieved the definition privately; confirmed pinned pg_catalog search_path, public-schema restriction, CREATE TABLE filtering, quoted dynamic SQL identifiers, RLS enablement and exception handling. Source is not reproduced here. Structural evidence is not formal proof of complete safety, exact equivalence to a published example, or verified failure behavior. |
| Additional public function inventory — EXECUTED externally | Only public.rls_auto_enable() found. No additional public functions/overloads or public function wrappers were reported by this inventory. Other schemas, API exposure and complete call paths remain unverified. |
| Additional direct role-membership inventory — EXECUTED externally | No memberships returned for anon or authenticated. This narrows the membership review; it does not remove PUBLIC privileges, prove the absence of other DDL paths or constitute a complete authorization audit. |
| Function dependency identity/direction — EXECUTED externally | Previously counted dependency identified as event trigger ensure_rls → function public.rls_auto_enable(), type n (normal). Function also has catalog dependencies on language plpgsql and schema public. The earlier count of 1 is not a total of both dependency directions or proof of all runtime call paths. |
| Schema CREATE review — EXECUTED externally | For both anon and authenticated, CREATE=false on auth, extensions, graphql, graphql_public, public, realtime, storage and vault. This does not prove every indirect DDL path impossible or every possible namespace/object/temporary privilege reviewed. |
| Event-trigger inventory — EXECUTED externally | ensure_rls present alongside six Supabase-related system event triggers, as externally reported. No trigger changed. Names/definitions of the six are not supplied; no safety or ownership equivalence inferred. |
| Additional public relation inventory — EXECUTED externally | Zero public-schema tables, views, materialized views or foreign tables returned. No applicable current public relation/policy target in that inventory; future RLS coverage is not verified. Other schemas, sequences and other object kinds are not established empty. No user rows accessed. |
| Existing project Free/tier_free, eu-central-1, ACTIVE_HEALTHY, PostgreSQL 17.11; 0 recorded migrations/Edge Functions | Previously recorded external ChatGPT connected-integration verification in C4 §H. Not re-executed here; this agent has no callable Supabase tools in this session. |
| Residual security limits | Structural definition review and the reported dependency/trigger/schema inventories are recorded. Complete source safety, exception failure behavior, cross-schema runtime callers and elimination of all indirect DDL paths are NOT proven. These are limits/future design-review requirements, not a reason to repeat completed catalog queries indefinitely. |
| Auth/security flags, payment methods, billing/usage and direct DB connectivity from this agent | UNVERIFIED. No secret-bearing settings, auth/user records, credentials or data rows requested. |

All three queries succeeded according to the owner’s external ChatGPT/Supabase
execution report. No database writes or function/RPC invocation occurred. Full advisor payload/version,
complete result dumps and a source digest were not supplied.
The latest rerun supplies the two exact check names and WARN levels above.
No vulnerability was demonstrated or repaired; both warnings remain recorded.
The four additional queries were executed by ChatGPT against the existing Supabase
project, per the owner’s report, not by this coding agent. The same external
ChatGPT/Supabase provenance applies; raw SQL/result dumps were not supplied here.
No database mutation or application-function invocation occurred in these checks.

## 2. Risk analysis against official sources

**Conclusion: excess-privilege/privileged-hook finding requiring review; direct
anon/authenticated privilege escalation is NOT demonstrated. Not classified as an
exploitable vulnerability or dismissed as a false positive.**

Supabase [Advisors](https://supabase.com/docs/guides/observability/advisors) are
static/deterministic checks and require context. Current upstream [lint 0028](https://raw.githubusercontent.com/supabase/splinter/main/lints/0028_anon_security_definer_function_executable.sql)
and [lint 0029](https://raw.githubusercontent.com/supabase/splinter/main/lints/0029_authenticated_security_definer_function_executable.sql)
check SECURITY DEFINER, effective role EXECUTE and exposed-schema membership; neither
SQL predicate excludes event_trigger return types. Those rules explain a possible
warning mechanism. The later external rerun confirms these two named checks; the
deployed linter version remains unknown.
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
all other privilege paths or privileged wrappers. The later inventory returned
no direct memberships for either API role and no additional public functions;
CREATE=false is additionally confirmed in the eight named schemas above; these
scoped negative observations do not eliminate every indirect DDL path. The
postgres owner is not a superuser
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
do not rerun completed A1–A3 or the four additional inventories without a concrete
reason. If the function is confirmed
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

## 3. Read-only verification checklist (A1–A3 and four inventories externally EXECUTED; remainder OPEN)

Use an existing authorized connection with catalog/configuration read scope. Return
only allowlisted metadata/booleans, no keys, passwords, auth records, tokens, user
logs or complete management-config dumps. Keep account identifiers private. Review
function source privately for embedded secrets before returning sanitized structural
facts; do not paste arbitrary function definitions into GitHub or the model.

### A. Function / event-trigger / effective-role metadata — first priority

- [x] Record the external advisor rerun: both exact check names, WARN level and target rls_auto_enable (2026-10-22 owner-relayed evidence); findings remain unresolved. Full payload/version not supplied.
- [x] A1 externally EXECUTED: record OID/language/prosecdef/owner/return type, function-local search_path and effective anon/authenticated EXECUTE for the reported function.
- [x] A2 externally EXECUTED: record ensure_rls matching binding OID, event/tags, enabled mode and owner.
- [x] A3 externally EXECUTED: record the supplied API-role capabilities/CREATE rights and postgres owner attributes; do not infer unreported values.
- [x] Record external default-ACL PUBLIC EXECUTE and no identified separate anon/authenticated grants, plus partial structural body-check findings.
- [x] Record the external public-function inventory: only rls_auto_enable(); no additional public functions/overloads returned.
- [x] Record the external direct-membership inventory: no memberships returned for anon/authenticated.
- [x] Record the external catalog dependency count of 1 without inferring its object identity, direction or safety.
- [x] Record dependency identity/direction: ensure_rls → public.rls_auto_enable(), normal n; record function dependencies on plpgsql/public.
- [x] Record CREATE=false for both API roles across the eight named schemas in §1; preserve limits on indirect DDL conclusions.
- [x] Record private deployed-definition retrieval and six structural findings, including exception handling; no source pasted or full-safety claim.
- [x] Record ensure_rls plus six externally reported Supabase-related system triggers; no trigger changes.
- [ ] Before any future callback/ACL change, review relevant cross-schema/delegated call paths, qualified/temporary-object resolution and exception behavior with compatibility evidence. Do not repeat completed inventories or run DDL/function/RPC probes. This is targeted future review, not a formal-proof requirement to continue documentary readiness.

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

- [x] Record external public-schema table/view/materialized-view/foreign-table inventory: zero objects returned; current zero-public-table observation confirmed.
- [ ] Review other-schema relation inventories and applicable RLS/force-RLS/policy metadata privately; report sanitized role/command coverage, never application rows. Empty public inventory does not prove all schemas empty or validate future application policies.
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
ACL inspection reported default PUBLIC EXECUTE; the later direct-membership
inventory returned no memberships for either API role. Broader authorization paths
still need review. The observed local search_path is pg_catalog, not null.
Database CREATE and public CREATE
do not alone enumerate every way to cause DDL (other schemas, object ownership,
wrappers and temporary objects still require contextual review).

## 5. Acceptance, unknowns and independently authorized next step

The next concrete authorized action is one bounded read-only pass over §3 B/C:
provider/signup/confirmation flags, sanitized redirects, session/refresh/MFA flags,
Data API/exposed schemas and SSL/network posture. Read only allowlisted settings;
no keys, user records, endpoint probes or configuration changes. Stop at accessible
results and record missing settings as UNVERIFIED; do not turn this into repeated
catalog discovery or demand proof of complete database safety.

Access checked in this session: plugin discovery reports Supabase installed and
ENABLED, but no callable Supabase tools are exposed. Installation is not evidence
of usable query/config access here. Auth/API settings are therefore inaccessible
to this agent and UNVERIFIED; no hosted reads were attempted. No connection,
permission or authentication change requested. Detailed pending flags remain in
§3 B/C and the bounded matrix in §9. Do not repeat A1–A3, completed inventories,
dependency or schema-privilege queries. Billing/payment and C4 legal prerequisites
remain open. No claim of no risk, exploitation, fixed warnings or public readiness.

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


## 8. Additional external catalog inventories (2026-10-22; base 9fe83db)

Purpose/scope: reconcile four further catalog-only observations supplied by the
owner from ChatGPT’s connected Supabase verification. These are external execution
results, not tests or hosted queries run by this agent. The exact scoped results
and limits are in §1; checklist portions are reconciled in §3.

- [x] Record public functions: only rls_auto_enable().
- [x] Record direct memberships for anon/authenticated: none returned.
- [x] Record function catalog dependency count: 1; identity/type/direction still unknown.
- [x] Record public tables/views/materialized views/foreign tables: zero objects.
- [x] Preserve both warnings, future RLS requirements, remaining source/Auth/API review and every Stage 6 approval gate.

No write or application-function invocation occurred. These findings narrow the
review; they neither prove full database security nor eliminate every indirect
DDL path. They do not justify revoking grants, removing ensure_rls or closing the
Security Advisor findings. Compatibility review and separate authorization remain
required for any permissions change.

Remaining source work: privately inspect the complete callback for bounded
schema/object/tag handling, qualified references (including temporary-object
resolution), safe dynamic SQL and absence of unintended privileged operations;
resolve the dependency’s identity/type/direction and cross-schema callers. Partial
structural matches and a dependency count are insufficient proof. No need to
repeat public-function, direct-membership, count-only or public-relation queries.

Remaining Auth checks (§3 B): provider/anonymous-signup and confirmation flags,
password/OTP and rate-limit/CAPTCHA posture, sanitized redirect assessment,
session/JWT/refresh settings, and operator MFA availability/enforcement. Remaining
API/RLS checks (§3 C): Data API enablement/exposed schemas/search paths, other-schema
objects/policies/grants/default ACLs and RPC/view/GraphQL/Realtime/Storage metadata,
SSL/network flags and key-handling design without retrieving keys. No provider,
OAuth or runtime acceptance setting is marked verified by these catalog results.

Issue #31 needs no edit: its two-warnings/no-demonstrated-exploit/open broader
review statement remains accurate. The dashboard links the living Stage 6
checklist; details stay here. No description or comment change is proposed or
published; the verified 9,564-character/52-line baseline is unchanged. Verification
and publication status of these documentation changes are in docs/ai/VERIFICATION.md.


## 9. Live evidence reconciliation and readiness verdict (2026-10-22; base 31e0539)

All five new observations in §1 are externally EXECUTED by ChatGPT through the
connected Supabase integration, per the owner’s report. They were not executed
by this coding agent. This supersedes earlier pending dependency identity,
named-schema CREATE checks and warning-name/source-structure items in §8 and
older checkpoints. No raw function definition, secrets or user records published.

- [x] Record ensure_rls → public.rls_auto_enable(), normal dependency n, plus language/schema dependencies.
- [x] Record both API roles’ CREATE=false in all eight supplied schemas.
- [x] Record private deployed-definition retrieval and six structural observations, without claiming complete source safety.
- [x] Record ensure_rls and six externally reported Supabase-related system triggers, unchanged.
- [x] Record both exact advisor names as WARN/unresolved after external rerun.
- [x] Check integration access available to this agent and explicitly retain inaccessible Auth/API settings as UNVERIFIED.
- [ ] Complete the single remaining sanitized configuration pass through an existing session exposing the required authorized read tools, or retain unavailable fields as documented limitations for owner review.

### Bounded Auth/API configuration pass

| Read-only evidence requested | Current result / completion condition |
|---|---|
| Enabled Google/email/anonymous providers, signup/confirmation flags | UNVERIFIED — no callable Supabase configuration tool here; do not equate intended Google setup with a live setting. |
| Password/OTP policy, rate-limit/CAPTCHA posture, session/JWT/refresh flags | UNVERIFIED — allowlisted booleans/numbers only, no secrets or session records. |
| Site URL/redirect allowlist assessment; operator MFA availability/enforcement | UNVERIFIED — assess privately, return sanitized conclusions; no OAuth/MFA changes. |
| Data API enablement, exposed schemas/search paths and RPC/view/GraphQL/Realtime/Storage exposure | UNVERIFIED — public catalog emptiness does not verify API configuration. |
| SSL/network flags and key-handling architecture | UNVERIFIED — no endpoint probes, keys or paid changes. |

One authorized settings-read pass is sufficient for this readiness snapshot: record
available flags and retain unavailable ones as limitations. Configuration acceptance,
future application RLS tests and any callback/ACL change belong to their own scoped
approval/implementation work. No additional open-ended catalog campaign is proposed.

### Prioritized blockers to authorized implementation

**Verdict: documentary/catalog readiness improved; NOT authorized to implement or
operate the backend. No exploitable vulnerability demonstrated or warning repaired.
Neither clearing the advisor warnings automatically nor obtaining formal proof of
complete source safety is treated as an already-approved prerequisite or action.**

1. **Q1/D5b — owner/professional decision, blocking.** Resolve operator/controller eligibility via the existing professional-review brief; current preference is not final acceptance.
2. **C4 — OPEN.** Finish applicable subprocessor/transfer/TIA/retention and authentic GDPR evidence, verify billing/payment/Free zero-cost posture, and perform the fresh day-of review. Existing-project facts do not retroactively authorize or complete 6.0d. Preserve 0 CZK/month permanently.
3. **C2 §12 and 6.0f — OPEN/UNCHECKED.** Production-runtime/private verification needs its separate authorization and prerequisites; local CPU/concurrency evidence does not close it. Owner must accept the resulting evidence explicitly.
4. **Explicit implementation authorization — absent.** 6.1–6.18 remain UNCHECKED; 6.0d/6.0e remain NOT AUTHORIZED. ORD-A authorizes sequencing only. Any hosted changes, OAuth, permissions hardening and real-user collection need their own authorization; current instruction permits documentation/read-only work only.
5. **Bounded security acceptance evidence — pending configuration access.** Complete the matrix above where available, then seek a scoped owner disposition for unknowns and the two WARN findings before hosted security acceptance. Preserve the RLS hook. Targeted exception/caller/qualification compatibility review is required if a later change is proposed, not an excuse to repeat the completed queries.

No Supabase mutations, deploys, OAuth/billing changes, production implementation,
application builds, screenshots or repeated concurrency tests. Stage 7A remains
conditional/X10 OPEN; Stage 7B not started, untouched. Issue #31’s broader open-review
summary remains accurate; no materially important correction, edit or comment is
needed. Its 9,564-character/52-line body and 63 comments remain the baseline.
Verification/commit publication is recorded in docs/ai/VERIFICATION.md.
## ACL migration proposal checkpoint — 2026-10-24

The production-compatible proposal is documented in
[ACL-MIGRATION-PROPOSAL.md](ACL-MIGRATION-PROPOSAL.md). It is intentionally
not an executed migration: preflight identity checks, function-only revoke,
trigger-preservation postflight, and exact captured-ACL rollback guidance are
provided for a future approved maintenance window. The supplied disposable
PostgreSQL 17 procedure passed again with exit 0. This does not close hosted
security configuration or C4 evidence.
