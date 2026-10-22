## Current checkpoint — local Auth/session/AuthZ prototype (2026-10-22)

Owner explicitly authorized isolated LOCAL-ONLY task-design 6.2/6.3 prototype,
not formal acceptance or gate amendment. Branch spike/goldberg-0.11-stage-6-auth-local,
base 0f966dc (actual latest schema remote, newer than observed f54881e), isolated
clone .gradle/stage6-auth-prototype. Original schema branch/evidence and Stage 5/7
worktrees/code preserved. This source checkpoint is its containing commit.

Implemented spikes/stage6-auth-local: strict jose 6.2.13 JWT/JWKS verification,
bounded injected keys/clock, stable issuer/subject mapping, additive auth_families/
challenges/refresh migration, hashed CSPRNG opaque tokens, expiry/revocation/logout,
atomic refresh replay-family revocation, fresh server capabilities/ownership,
no self-grant/operator privilege, safe in-process local transport/errors/logs.
No HTTP listener or deployment interface. No default launch administrator seed.

Executed final: 50 new Auth/security tests + 34 original schema regressions =
84 passed, zero failed/skipped/cancelled on PostgreSQL 17.11. Initial 41+34 and
second 48+34 also passed before final coverage/guards added. Five host scripts PASS.
Original schema source/hash/34-test artifact unchanged; no old benchmark/audit rerun.
Local T1–T9/T22 coverage is qualified in results/SUMMARY.md, not full threat-row,
real-provider/hosted/HTTP/RLS/CSRF acceptance. Code/SQL/tests/source hashes and exact
commands: docs/planning/goldberg-0.11/stage-6/LOCAL-AUTH-PROTOTYPE.md and
spikes/stage6-auth-local/results/. No Supabase, real credentials/users, production
retention/deletion, Android/APK/device, cloud, merge or release activity.

Original API/model untouched. Existing consumer review in LOCAL-CORE-ADOPTION.md
reused: changes_requested canonical, draft device-local, returned UI label only;
old C6 broader guard/evidence preserved. Patch remains for owner review. Auth work
was independent and authorized; no formal policy exception applied.

Next, only after separate owner scope: local 6.4 typed bounded submissions/revisions
with full validation; 6.5 authenticated atomic moderation using this context and
existing lock/head design; 6.6 request-bound idempotency/replay/mismatch/expiry.
Do not automatically proceed. Q1/D5b unresolved/blocking; C4/C2 §12 OPEN;
6.0f/6.1–6.18 UNCHECKED; 6.0d/6.0e NOT AUTHORIZED; 0 CZK/month; collection NOT
AUTHORIZED. Stage 7A conditional/X10 OPEN, Stage 7B untouched. Issue checkpoint
and minimal latest-link dashboard proposal will be published/prepared after commit;
description edits require explicit approval and fresh conflict check.

### Executed local Auth milestone verification

- `spikes/stage6-auth-local/run-local.sh > spikes/stage6-auth-local/results/final.tap 2>&1`: PASS, 50 new tests + 34 original schema regressions, zero failures/skips/cancellations. PostgreSQL 17.11/Node v22.23.1/pg 8.23.1/jose 6.2.13; pinned disposable container and source hashes in the new results directory. Original 34-test files not overwritten.
- `python3 scripts/check_agent_policy.py`: PASS; runtime agent loading not tested.
- `python3 scripts/check_backup_fixtures.py`: PASS, 3 formats.
- `python3 scripts/check_history_paging.py`: PASS, 106 sessions/4 pages.
- `python3 scripts/check_localization.py`: PASS, 922 EN/CZ resources.
- `python3 scripts/check_migrations.py`: PASS, host v1–9→10 DDL; Android runtime unexecuted.
- New module/source hashes, unchanged original schema/evidence, authorized scope/local links/original gate boxes/non-Stage-6 sections and whitespace checks: verified before commit.

Test results cover actual reusable modules and PostgreSQL persistence, not mocks of
security behavior. Runtime uses maintained jose verification/standard CSPRNG+hash
primitives; fake JWKS/clock are test dependencies and generated signing keys stay
in memory. Scope/remaining T1–T9/T22 qualifications are in new results/SUMMARY.md.
No failed or skipped tests in the three executed runs; no completed benchmark/audit
rerun or real provider/cloud/phone/browser/app/APK/production RLS acceptance claim.
Original schema branch 0f966dc and Stage 7A 576f0d2 preserved. Formal gates unchanged.

## Current checkpoint — approved dashboard and adoption preparation (2026-10-22)

Base f54881e on spike/goldberg-0.11-stage-6-schema-pg. Owner-approved exact Issue
#31 Stage 6 row/latest-checkpoint edit PUBLISHED; independent readback PASS:
9,592 Unicode characters/52 lines, 2026-10-22T20:58:16Z, UTF-8 SHA-256
`dd2829c6576e9648396505c9cd87e70e3ae8667e4f35a7ac571bb90e59e35f2a`.
All 64 existing comment records and metadata/Project In Progress preserved at
publication. Other stage rows and all gates unchanged. Supersedes prior pending
9,564-character dashboard baseline below.

Owner reviewed the prototype and selected draft=device-local only,
changes_requested=canonical author-revision server state, returned=UI label only
if needed. Dependency review COMPLETE before any API edit: prototype already
matches; old isolated C6 simulator permits all three; no matching current app or
Stage 7A consumer found. Unknown external consumers are not proven absent. Approved
API/model text and old simulator/evidence remain unchanged.

Concrete adoption/compatibility report:
docs/planning/goldberg-0.11/stage-6/LOCAL-CORE-ADOPTION.md. Includes exact proposed
API patch and narrow local 6.1–6.3 authorization text under adoption-proposals/.
These are REVIEW DRAFTS, not policy in force. Reuse the hash-verified 34/34 prototype
for local 6.1, then fake-clock/JWKS/session Auth 6.2 and server capability/ownership
AuthZ 6.3. No actual extraction, new Auth/AuthZ code or formal 6.1 start this session.

Exact required owner decision: approve local-only formal 6.1–6.3 ahead of 6.0f,
amending only that subset's ordering in CHECKLIST/D8b, OPEN-DECISIONS,
CLOUD-READINESS-PLAN and POSTGRESQL-DESIGN-REQUIREMENTS. 6.0f stays OPEN/UNCHECKED;
ORD-A/cloud gates remain unchanged. Separately approve the API patch after reviewing
compatibility impact. This is needed because the current rule still says before
any implementation; prototype review alone does not amend it. No general audit
or replanning prerequisite; the concrete approval is the next action.

Q1/D5b unresolved/blocking; C4/C2 §12 OPEN; 6.0f/6.1–6.18 UNCHECKED;
6.0d/6.0e NOT AUTHORIZED; 0 CZK/month; collection NOT AUTHORIZED. Stage 5/7 and
unrelated worktrees/files preserved. Tested SQL/source/runner hashes unchanged;
34/34 historical evidence reused, no repeated PG benchmarks or hosted operations.
Publication/checkpoint SHA is the containing documentation commit on this branch.

### Adoption-preparation verification

Executed on the isolated spike branch; all PASS:

- `python3 scripts/check_agent_policy.py` (runtime loading not tested).
- `python3 scripts/check_backup_fixtures.py` — 3 formats.
- `python3 scripts/check_history_paging.py` — 106 sessions/4 pages.
- `python3 scripts/check_localization.py` — 922 EN/CZ resources.
- `python3 scripts/check_migrations.py` — host v1–9→10 DDL, Android runtime not executed.
- `git apply --check --unidiff-zero docs/planning/goldberg-0.11/stage-6/adoption-proposals/API-REVISION-STATUS.patch` — exact review patch applies to the inspected baseline; NOT applied. Zero-context hunks avoid whitespace-only context lines in the saved patch artifact.
- Twelve-file documentation/proposal scope, local links, unchanged original gate checkboxes/operative policy and non-Stage-6 sections, untouched approved API/model and 34-test source hashes: PASS.
- `git diff --check` / staged diff check: PASS.

Compatibility work is executed source/reference inspection, not a live client or
hosted database test. Original C6 guard found at src/server.mjs:558 and canonical
request-changes regression at tests/v4-moderation.test.mjs:75; Stage 7A 576f0d2
inspected read-only with its unrelated visual-reference files preserved. No source
extraction or formal 6.1–6.3 implementation; no old suite/benchmark rerun, cloud
operation, app build, screenshot, APK/signing, deployment or release.

Approved Issue body published exactly after fresh conflict check: 9,592 chars/52
lines; all 64 full comments/metadata unchanged at publication; Project In Progress.
No policy amendment or API update beyond the separate review artifacts is applied.

## Local prototype publication — 2026-10-22

Verified source commit: 16bfdfbe213ce66973a55d9746453601605e74d7.
Verified diagnostic-format correction: f91a4ced846ad53a571c744c6c588f245fe3a582.
Both pushed to spike/goldberg-0.11-stage-6-schema-pg; tested source hashes unchanged.
[Issue #31 checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6351050432) posted under owner/workflow authorization, then
independently fetched and matched exactly. All 63 older comment records preserved;
64 total; labels/milestone/state and Project In Progress unchanged. Readiness branch
4d01489, Stage 7A 576f0d2 and all unrelated worktrees/files preserved.

Dashboard row/link proposal prepared in ignored .gradle/supabase-security-audit/
prototype-proposed-dashboard.md and shown for explicit owner approval. Currently
UNPUBLISHED; description remains 9,564 characters/52 lines with SHA-256
b3a49d6bf39b7181fd675765d3d5d8fff16d04f637ced2bddc69494c4df48549.
Before any approved body write, re-fetch for conflicts and preserve all other rows.

Formal Stage 6 remains unstarted. Local prototype milestone COMPLETE; Q1/D5b/C4/
C2 §12/6.0f still open, 6.1–6.18 unchecked; no automatic promotion, deployment,
OAuth or collection. Next: owner review of schema/contracts; formal adoption only
after gate clearance and separate approval. Full verification and the whitespace
workflow correction are in docs/ai/VERIFICATION.md; source/evidence in prototype
results/SUMMARY.md. This publication record is identified by its containing commit.

## Current Stage 6 checkpoint — local schema prototype (2026-10-22, base 4d01489)

Owner explicitly authorized isolated local-only synthetic prototype implementation,
not formal 6.1. Branch: `spike/goldberg-0.11-stage-6-schema-pg`, separate clone
`.gradle/stage6-schema-prototype`. Readiness branch remains at 4d01489; Stage 5
and Stage 7A worktrees/code/evidence untouched. Containing implementation commit
identifies this checkpoint; exact publication SHA follows in branch history.

Implemented `spikes/stage6-schema-pg`: two versioned/checksummed migrations for all
community entities, FK/unique/check graph, immutable revision/review/publication/
audit/feed history, revision hash chain, audit_head serialization, idempotency
storage and bounded local transaction exerciser; disposable PG17 runner, safe int8
conversion, independent audit export verification and targeted tests.

Executed: final clean PostgreSQL 17.11 run 34/34 PASS, zero failed/skipped; five
repository host checks PASS. Empty/seeded/repeat/concurrent/failing migration,
orphans/uniqueness, immutable mutation/truncation, six rollback boundaries,
same/different-submission target contention, named-key collision recovery/replay/
expiry, audit tamper/head/role checks, int8 boundaries and lock-timeout regression.
Earlier SQL/fixture/setup failures are preserved separately, not final failures.
No old PG/SQLite concurrency benchmarks repeated; original e37ed00/c21e550 evidence
untouched. No Supabase/production RLS/Auth/OAuth, full restore/RPO, app/APK/device
checks or deployment performed. Disposable prototype containers removed.

Contract discrepancy: API revisions permit draft/changes_requested/returned;
server model defines draft local-only and changes_requested → in_review only.
No new transition or approved API edit. Owner review required before adoption.

Evidence/recovery: docs/planning/goldberg-0.11/stage-6/LOCAL-SCHEMA-PROTOTYPE.md
and spikes/stage6-schema-pg/results/SUMMARY.md, final.tap, environment/source hashes.
Next: review this candidate for eventual 6.1, reconcile contract statuses, obtain
explicit 6.0f acceptance and separate formal implementation approval. Stop any
hosted/credential/gate-policy action without its phase-scoped authorization.
Q1/D5b unresolved/blocking; C4/C2 §12 OPEN; 6.0f/6.1–6.18 UNCHECKED;
6.0d/6.0e NOT AUTHORIZED; 0 CZK/month; real-user collection NOT AUTHORIZED.
Stage 7A conditional/X10 OPEN; Stage 7B untouched. Implementation commit/push and
existing Issue #31 checkpoint authorized by the owner/workflow; significant body
edits require review. Issue publication status is recorded in the next checkpoint.

### Publication check correction

The first staged diff check exposed trailing spaces on four blank lines in saved
initial/second failed-run TAP diagnostics. A shell lacking fail-fast handling
continued to commit/push 16bfdfb despite that formatting check failure. Source and
34/34 test results were unaffected. The original log bytes are retained under
ignored .gradle/supabase-security-audit/prototype-raw; only trailing whitespace in
these two committed diagnostics is normalized. Full base-to-current diff check
then PASS. No gate policy is waived or source/test result altered; subsequent
publication steps use fail-fast commands. This correction is an additional commit,
without rewriting the already-pushed implementation history.

### Executed commands and scope for local prototype

- `cd spikes/stage6-schema-pg && ./run-local.sh > results/final.tap 2>&1`: PASS, 34 tests, 0 failed/skipped/cancelled; PostgreSQL 17.11, Node v22.23.1, pg 8.23.1. Pinned image/env and source hashes in prototype results.
- `python3 scripts/check_agent_policy.py`: PASS (runtime agent loading not tested).
- `python3 scripts/check_backup_fixtures.py`: PASS, 3 fixture formats.
- `python3 scripts/check_history_paging.py`: PASS, 106 sessions/4 pages.
- `python3 scripts/check_localization.py`: PASS, 922 EN/CZ resources.
- `python3 scripts/check_migrations.py`: PASS, host DDL v1–9→10; Android bridge/trigger runtime unexecuted.
- `git diff --check`, local Markdown links, source-scope/original gate and non-Stage-6 checklist preservation: PASS.

Raw intermediate runs: initial migration syntax failure (0/27), corrected schema
with fixture type errors (25/27), third (31/31), first disposable-runner setup
failure before tests, then final clean-run 34/34. Final source hashes bind SQL,
modules, tests, package manifests and runner. No secrets/user data or copied old
spike implementation. The immutable evidence at original failure e37ed00 and
hardening c21e550 stays unchanged. This is no old benchmark rerun or claim of
formal 6.1 acceptance. See results/SUMMARY.md for exact coverage and unexecuted work.

## Live Supabase evidence and bounded readiness verdict — 2026-10-22 (base 31e0539)

Externally executed by ChatGPT’s connected Supabase integration, relayed by the
owner; not queried by this coding agent: ensure_rls → public.rls_auto_enable(),
normal dependency n; function also depends on plpgsql/public. Both API roles lack
CREATE in auth/extensions/graphql/graphql_public/public/realtime/storage/vault.
Private deployed-definition review confirms pinned pg_catalog, public restriction,
CREATE TABLE filtering, quoted identifiers, RLS logic and exception handling; this
is structural evidence, not complete safety proof. Inventory found ensure_rls plus
six Supabase-related system triggers, unchanged. Advisor rerun reports
anon_security_definer_function_executable and
authenticated_security_definer_function_executable both WARN, targeting the
function; unresolved. No exploit demonstrated/repaired or hosted mutation.

Current ledger/checklist/verdict: docs/planning/goldberg-0.11/stage-6/SECURITY-AUTH-READINESS-AUDIT.md
§1/§3/§9 supersede the earlier pending dependency/schema/source-structure/warning-name
items. Do not repeat A1–A3 or completed inventory/dependency/schema queries.

Plugin discovery: Supabase installed/ENABLED, but no callable Supabase tools exposed
here. Auth/API provider, signup, redirect, session/MFA, exposure and SSL/network
flags remain UNVERIFIED. No credentials, user records, raw definitions or broader
access requested. Next authorized step: one bounded sanitized settings pass through
an existing session with the required read tools; retain inaccessible fields as
limitations, then stop exploratory catalog work and resolve approval decisions.

Readiness: NOT authorized for backend implementation. Priorities: (1) owner obtains
Q1/D5b professional eligibility decision; (2) C4 transfer/retention/legal and
billing/payment/0-CZK evidence plus fresh review; (3) separately authorized C2 §12
verification and explicit 6.0f acceptance; (4) explicit implementation authorization.
A bounded Auth/API review and owner disposition of remaining WARN/unknowns are
security acceptance inputs, not permission to alter grants/triggers/functions.
Q1/D5b unresolved/blocking; C4/C2 §12 OPEN; 6.0f/6.1–6.18 UNCHECKED;
6.0d/6.0e NOT AUTHORIZED; ORD-A sequencing only; 0 CZK/month; real-user collection
NOT AUTHORIZED. Backend NOT IMPLEMENTED; Worker NOT DEPLOYED.

Issue #31 needs no material correction and stays unchanged (9,564 characters/52
lines, 63 comments; body SHA-256
b3a49d6bf39b7181fd675765d3d5d8fff16d04f637ced2bddc69494c4df48549).
Stage 7A conditional/X10 OPEN and Stage 7B not started remain untouched. Dedicated
branch docs/goldberg-0.11-stage-6-readiness; this checkpoint is its containing commit.

### Verification of the live-evidence documentation continuation

Executed in the isolated Stage 6 checkout; all PASS:

- `python3 scripts/check_agent_policy.py` — policy/references/Wiki navigation; runtime agent loading not tested.
- `python3 scripts/check_backup_fixtures.py` — 3 portable fixtures, fields retained.
- `python3 scripts/check_history_paging.py` — 106 sessions/4 pages, legacy attribution.
- `python3 scripts/check_localization.py` — 922 EN/CZ resources, placeholders/plurals.
- `python3 scripts/check_migrations.py` — host DDL v1–9→10; Android bridge/trigger runtime not executed.
- Eight-file Markdown-only scope/local links; original stage/gate checkboxes and non-Stage-6 sections unchanged. A1–A3 SQL blocks and Auth/API checklist tasks unchanged; new external evidence reconciles only the relevant catalog checklist portions.
- `git diff --check` — PASS. Git identity preserved; Stage 6 remote baseline 31e0539 and Stage 7A 576f0d2 confirmed. Issue #31 body SHA/counts still match the recorded baseline; 63 comments, no material dashboard correction needed.

Tool discovery (using the plugin-management skill) found Supabase installed/ENABLED
but no callable Supabase tools exposed. No authorized metadata/configuration read
could be executed here; all hosted facts above retain external ChatGPT provenance.
No hosted writes, user rows, secrets, app builds/device tests/screenshots, APK or
release work, repeated completed catalog/concurrency tests or Stage 7 changes.
Verified documentation-only commit/push is authorized; source SHA is this section’s
containing commit. No GitHub Issue write/comment is proposed or performed.

## Additional external catalog inventories — 2026-10-22 (base 9fe83db)

ChatGPT executed four further read-only Supabase catalog queries; results were
relayed by the owner, not executed by this coding agent. Public functions: only
rls_auto_enable(). Direct anon/authenticated memberships: none returned. Function
dependency count: 1 (identity/type/direction not supplied). Public tables, views,
materialized views and foreign tables: zero objects. No database writes or
application-function invocation. Exact evidence and reconciled checklist:
docs/planning/goldberg-0.11/stage-6/SECURITY-AUTH-READINESS-AUDIT.md §1/§3/§8.

These scoped observations supersede corresponding pending inventories below.
They do not prove full security, eliminate all indirect DDL paths, establish
complete dependency/source safety or resolve either advisor warning. PUBLIC
EXECUTE remains observed despite no direct API-role memberships. Keep ensure_rls.
Do not repeat A1–A3 or these four inventories without a concrete reason.

Next authorized read-only work: resolve dependency identity/type/direction and
remaining cross-schema/DDL privilege paths, complete private body review, then
allowlisted Auth/provider/redirect/session/MFA and API-exposure flags (§3 B/C).
Other-schema RLS/grants, raw advisor IDs/version, billing/payment and C4 legal
requirements remain open. No secrets, user records, function/RPC or DDL probes.
Future permissions changes need separate authorization and compatibility review.

Issue #31 remains accurate and unchanged: 9,564 Unicode characters/52 lines,
SHA-256 b3a49d6bf39b7181fd675765d3d5d8fff16d04f637ced2bddc69494c4df48549;
63 comments preserved. No dashboard edit or checkpoint comment is necessary for
these scoped inventory details; the dashboard already links the living checklist.

Q1/D5b unresolved/blocking; C4 and C2 §12 OPEN; 6.0f/6.1–6.18 UNCHECKED;
6.0d/6.0e NOT AUTHORIZED; ORD-A sequencing only; 0 CZK/month; real-user collection
NOT AUTHORIZED. Production backend NOT IMPLEMENTED; Worker NOT DEPLOYED. Stage 7A
conditional/X10 OPEN and 7B not started remain unchanged. Branch:
`docs/goldberg-0.11-stage-6-readiness`; checkpoint SHA is its containing commit.

### Verification of this documentation continuation

Executed in the isolated Stage 6 checkout; all PASS:

- `python3 scripts/check_agent_policy.py` — canonical policy/references/Wiki navigation; runtime agent loading not tested.
- `python3 scripts/check_backup_fixtures.py` — 3 portable fixtures, original fields retained.
- `python3 scripts/check_history_paging.py` — 106 sessions/4 pages and legacy attribution checks.
- `python3 scripts/check_localization.py` — 922 matching EN/CZ resources.
- `python3 scripts/check_migrations.py` — host DDL v1–9→10; Android bridge/trigger runtime not executed.
- Documentation consistency — eight Markdown-only files; local links resolve; unchanged original gate checkboxes/non-Stage-6 sections; SQL blocks, Auth tasks and remaining API/grant tasks preserved.
- `git diff --check` — PASS. Existing Git identity preserved; remote Stage 6 baseline 9fe83db and Stage 7A 576f0d2 checked before editing.

No Supabase query or application-function invocation by this agent; the four new
hosted observations are externally executed evidence. No completed inventory,
concurrency suite or source-research rerun; no app build, device test, screenshot,
APK/signing/release work, hosted mutation or Stage 7 change. Documentation-only
commit/push is authorized after these checks; use the containing commit as the
checkpoint SHA. Issue #31 is unchanged and requires no publication approval in this
continuation because no edit/comment is proposed.

## External A1–A3 continuation — 2026-10-22 (base bac81fb)

A1–A3 EXECUTED successfully by ChatGPT’s connected Supabase integration; results
relayed by the owner, not executed by this coding agent. Evidence ledger and
partial-completion checklist: docs/planning/goldberg-0.11/stage-6/SECURITY-AUTH-READINESS-AUDIT.md.
OID 17603 is plpgsql/SECURITY DEFINER/postgres/event_trigger with search_path
pg_catalog and anon/authenticated effective EXECUTE. ensure_rls binds that OID,
ddl_command_end, normal mode O, tags CREATE TABLE/CREATE TABLE AS/SELECT INTO.
API roles lack the six reported superuser/role/CREATE capabilities; postgres has
BYPASSRLS and public/database CREATE, but rolsuper=false. Default PUBLIC EXECUTE
was observed; no separate anon/authenticated grants identified. Structural body
checks are partial, not proof of safety. Both advisor warnings remain unresolved;
no exploit demonstrated or repaired, no function/RPC invocation or database writes.

This supersedes earlier pending language/path/binding/A1–A3 statements below.
Auth/API flags, inherited-role and remaining DDL paths, complete source/dependency
review, actual advisor IDs/version and billing/payment evidence remain UNVERIFIED.
Next independently authorized step: those remaining sanitized catalog/private
source checks, then allowlisted Auth/API flags through the existing authorized
connection. No callable Supabase tools here; do not repeat A1–A3 or request secrets.
Any future function-specific PUBLIC ACL reduction requires separate write approval,
compatibility review and rollback design; preserve ensure_rls.

Q1/D5b unresolved/blocking; C4 and C2 §12 OPEN; 6.0f/6.1–6.18 UNCHECKED;
6.0d/6.0e NOT AUTHORIZED; ORD-A sequencing only; 0 CZK/month; collection NOT
AUTHORIZED. No production backend, Worker deployment or Stage 7 changes. This
checkpoint is identified by its containing documentation commit on
`docs/goldberg-0.11-stage-6-readiness`.

### Published Issue #31 baseline and verification

Owner approved only the A1–A3 C4 audit-sentence replacement. Fresh pre-write body,
metadata and comments matched the inspected baseline; exact published readback PASS.
[Issue #31](https://github.com/stokonn/Goldberg/issues/31): **9,564 Unicode characters / 52 lines**,
updated 2026-10-22T20:09:06Z; UTF-8 SHA-256
`b3a49d6bf39b7181fd675765d3d5d8fff16d04f637ced2bddc69494c4df48549`.
Previous baseline: 9,442 characters / 52 lines; change +122 characters, one sentence
only. All 63 full comment records, labels/milestone/state/title/assignees/lock and
Goldberg Development Project In Progress preserved; no checkpoint comment added.
This is the current conflict-check baseline; older baselines below are historical.

Five existing host checks PASS: agent policy; 3 backup fixtures; history paging
106 sessions/4 pages; localization 922 EN/CZ resources; migration DDL v1–9→10.
Migration/bridge Android runtime coverage is not supplied by these host checks.
Nine Markdown files only; local links, git diff --check, unchanged original
stage/gate checkboxes and non-Stage-6 sections PASS. Original SQL blocks and the
remaining Auth/API task lists are unchanged. No hosted SQL/function/RPC/DDL by this
agent, application build/device test, screenshots, cloud changes or completed
PostgreSQL/SQLite concurrency reruns. Documentation publication is authorized on
the dedicated branch; use this checkpoint’s containing commit as the source SHA.

## Previous security-audit checkpoint and Issue baseline — 2026-10-22

Read-only documentary audit completed from 4f0cd51 on
`docs/goldberg-0.11-stage-6-readiness`; actual hosted verification checklist remains
UNEXECUTED. Use SECURITY-AUTH-READINESS-AUDIT.md for source-attributed facts,
conditional risk analysis, recommendation and next catalog reads A1–A3.
Owner-approved single C4-sentence update published and exact readback verified:
9,442 Unicode characters/52 lines, 2026-10-22T19:56:59Z; SHA-256
`2abc94feba38ded15eccb75b9bc61c884dff3c6d973e432db2190c23e9cb50dc`. All 63 comments,
Issue metadata and Project In Progress preserved. No new comments. This supersedes
older dashboard baselines for future conflict checks. All Stage 6 gates unchanged.

Five host checks and Markdown scope/link/unchanged-checkbox checks PASS; three
catalog SELECT drafts statically reviewed only. No Supabase SQL/functions/RPC, DDL,
Auth mutation, grants, event-trigger change, application tests, screenshots or
completed concurrency/research-suite reruns. This checkpoint is carried by its
containing documentation commit; publication SHA is resolved from branch history.

## Security & Auth read-only audit — 2026-10-22 (base 4f0cd51)

Owner-supplied catalog facts: public.rls_auto_enable() is SECURITY DEFINER, owned
by postgres, executable by anon/authenticated, returns event_trigger; ensure_rls
is an enabled DDL event trigger; no public application tables. This agent did not
execute those hosted checks. Exact advisor IDs, language/body/search_path, grant
provenance, DDL rights and API/Auth configuration remain unverified.

Official Supabase and PG17 guidance/source reviewed: direct PL/pgSQL callback calls
are rejected outside trigger context; existing hooks can still fire on matching
non-superuser DDL. Excess privileges do not establish exploitability. Preserve the
RLS guard; inspect metadata/body safely before considering separately authorized,
function-specific ACL/search-path hardening. No grants, functions, event triggers,
OAuth settings or resources changed; no function/RPC/DDL probe or user-row read.

Current analysis, read-only checklist, A1–A3 catalog SELECTs and source references:
docs/planning/goldberg-0.11/stage-6/SECURITY-AUTH-READINESS-AUDIT.md. Hosted checklist
NOT EXECUTED (no callable Supabase tools in this session). Next independently
authorized task is those allowlisted metadata reads through an existing authorized
connection, not a mutation or privilege-escalation test. Owner approved the single C4-sentence Issue update; exact publication and
verification are recorded above and in the audit document.

Q1/D5b unresolved/blocking; C4 and C2 §12 OPEN; 6.0f/6.1–6.18 UNCHECKED;
6.0d/6.0e NOT AUTHORIZED; ORD-A sequencing only; 0 CZK/month. Stage 7 unchanged.

## Current Issue #31 baseline after Supabase reconciliation — 2026-10-22

Published with explicit owner authorization; verified at 2026-10-22T19:41:30Z:
9,276 Unicode characters/52 lines; exact UTF-8 SHA-256
`970b5a9e1dbcc632fba28403d440a4ec6183ddc6b48720d0dd30784566699328`.
Only C4 observation text changed. All 63 comment records and Issue metadata
unchanged; Goldberg Development remains In Progress. No new checkpoint comment.
Supersedes the older 8,925-character baseline for future conflict comparisons.

Executed reconciliation checks: all five existing Python scripts PASS (policy;
3 backup fixtures; 106-session/4-page paging; 922 EN/CZ resources; v1–9→10 DDL).
Markdown links, 12-file docs-only scope, original task/gate checkboxes, Q1 question
text, preserved other stage sections, external attribution and published hash PASS;
git diff --check PASS. No hosted SQL/configuration or application tests executed.

## Existing Supabase project — externally verified reconciliation (2026-10-22)

The owner relayed verification by **ChatGPT's connected Supabase integration** on
2026-10-22: existing organization/project identity, organization Free/`tier_free`,
project Frankfurt (`eu-central-1`), `ACTIVE_HEALTHY`, PostgreSQL 17.11, **0 public
tables, 0 recorded migrations, 0 Edge Functions**. These are externally verified
observations; **this coding agent did not execute those checks**. Display names
stay in ignored local notes under the public no-account-identifiers policy.
This session still exposes no callable Supabase tools; that capability limitation
does not negate the separate external verification.

Billing/payment methods, quota usage, Auth providers/security, direct database
connectivity from this agent and non-public schema configuration remain **UNVERIFIED**.
Zero public tables is not proof that every schema is empty or that Auth is disabled.
Production backend **NOT IMPLEMENTED** in the verified repository; Cloudflare Worker
**NOT DEPLOYED** in the verified record. Google OAuth not configured in Goldberg's
implementation record; hosted Google configuration UNVERIFIED. Real-user collection
**NOT AUTHORIZED**. Do not create another project or modify the existing one.

Q1/D5b, C4, C2 §12 and 6.0f remain **OPEN** (6.0f UNCHECKED); 6.0d/6.0e **NOT
AUTHORIZED**, 6.1–6.18 UNCHECKED; ORD-A sequencing only; 0 CZK/month permanently.
Project existence does not complete/authorize 6.0d. Q1-C denotes the operational
hold, not zero-resource inventory; no operator option is selected. Prior zero-project
observations are historical. Stage 7A conditional/X10 OPEN and 7B not started stay
unchanged; no Stage 7 work is performed here.

Current checklist and metadata inventory: docs/planning/goldberg-0.11/stage-6/
C4-PREPROVISIONING-CHECKLIST.md §H and READINESS-CONTINUATION.md reconciliation
entry. Base eab990b on docs/goldberg-0.11-stage-6-readiness; no newer remote commit
or conflicting local work at recovery. Issue #31 still matched the verified
8,925-character baseline on recovery; latest checkpoint 6349209052. The owner approved the C4-bullet update and its subsequent external-verification
correction; the exact published body and validation are recorded in C4 §H.
No new Issue comment was added.

## Issue #31 dashboard baseline — published and verified 2026-10-22

Owner approved the exact editorial draft; only the Issue description was replaced.
[Current dashboard](https://github.com/stokonn/Goldberg/issues/31), verified at
2026-10-22T19:23:54Z: **8,925 Unicode characters, 52 lines**, down from 25,489
characters/136 lines (64.98% character reduction). Exact UTF-8 body SHA-256:
`c6fabfe7efefa331472fab9e1ba70ea4418bafe6dfedaa02e644e34c138318b7`.
Pre-write body matched the reviewed baseline; post-write body matched the approved
draft exactly. All 63 full comment records unchanged; no new comments. Labels,
milestone 4, title, state, assignees and lock state unchanged; Goldberg Development
item remains In Progress. This supersedes earlier editorial approval-pending notes.

For future edits, fetch current body/comments and compare with this baseline;
review unrelated changes before writing, preserve evidence and update the baseline
after verified publication. The Issue is current status/navigation, repository
docs hold specifications, and comments remain chronological history.

Documentation baseline checks executed: check_agent_policy.py PASS; relative-link,
Markdown scope, unchanged technical-checkbox and non-Stage-6 preservation checks
PASS; git diff --check PASS. No application/spike/emulator tests repeated.

Technical gates unchanged: Q1/D5b unresolved/blocking; C4 and C2 §12 OPEN; 6.0f
and 6.1–6.18 UNCHECKED; 6.0d/6.0e NOT AUTHORIZED; ORD-A sequencing only;
0 CZK/month. Stage 7A conditional/X10 OPEN; 7B not started. No new implementation,
merge, deployment, release or application changes. Next product actions remain
owner-routed Q1 review and owner-performed X10; no physical-device work by agents.

## Current Stage 6 continuation — 2026-10-22 (documentation only)


## Finalization publication checkpoint — 2026-10-22

The owner explicitly authorized this documentation-only finalization. Reviewed
12 Markdown files; all five host scripts re-executed and PASS; local relative
links, unchanged original task checkboxes and preservation of other stage sections
verified; git diff --check PASS. Source documentation commit
[6c40799](https://github.com/stokonn/Goldberg/commit/6c40799fefa331385c394fb621e72a5a62c20423)
was pushed to docs/goldberg-0.11-stage-6-readiness and exact remote SHA verified.
Prepared continuation checkpoint posted unchanged at
[comment 6349066370](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6349066370).
This containing documentation commit records publication evidence; consult the
branch HEAD and the finalization Issue comment for its exact revision.

Issue dashboard editorial draft prepared from the latest body and all 62 comments,
including Stage 7A conditional acceptance 6346899885; all 19 unique links checked
live. Snapshot/proposal/link report stored locally at
/home/stokonn/Projects/Opus/.gradle/issue31-cleanup/. No description replacement
until owner approval of the final draft; re-fetch immediately before any approved
write and stop on unrelated edits. Original comments remain untouched.

All technical gates unchanged: Q1/D5b unresolved/blocking; C4 OPEN; C2 §12 OPEN;
6.0f UNCHECKED; 6.1–6.18 UNCHECKED; 6.0d/6.0e NOT AUTHORIZED; ORD-A sequencing
only; 0 CZK/month. Stage 7A conditional/X10 OPEN, 7B not started (latest separate
Stage 7 branch); no Stage 7 code or evidence modified. No Gradle, APK, screenshots,
emulator, hosted-runtime or new spike execution. No merge/tag/release/provisioning.

This entry supersedes earlier retrieval/next-action summaries only; historical
checkpoints below remain preserved. Base planning b3edc2a; completed spike c21e550;
prior Issue checkpoint 6348121556. Owner-approved Stage 6-only Issue description
sync applied and exact-body readback passed. Issue #31 and milestone 4 OPEN;
Project Goldberg Development item In Progress (verified, unchanged).

Current working checkout: /home/stokonn/Projects/Opus/.gradle/stage6-continuation,
branch docs/goldberg-0.11-stage-6-readiness, based on b3edc2a.
Owner explicitly authorized verified documentation commit/push in the finalization
instruction; publication evidence will be appended after remote verification.
Read docs/planning/goldberg-0.11/stage-6/READINESS-CONTINUATION.md first for the
session checklist, sources, owner actions and verification; then
POSTGRESQL-DESIGN-REQUIREMENTS.md for future 6.1 requirements. Completed spike
results preserved and NOT rerun. C4 PDF text parsed (22 entries); Trust Center
opened but TIA not retrieved/Free access unverified; official GDPR retrieval still
challenged; exact provider metadata retention and Supabase billing remain open.
Corrected stale no-account wording in the professional-review brief to preserve
owner-created account facts. No identity, credentials or console evidence added.

Next: owner routes the existing professional-review brief for Q1/D5b; obtain TIA
and retention clarification privately, finish documentary transfer review and
Supabase no-payment confirmation without provisioning. No contact sent. New Issue
checkpoint [posted with owner authorization](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6349066370);
local text preserved in docs/ai/STAGE6-ISSUE-CHECKPOINT-DRAFT.md as a historical
snapshot. No automatic compaction-to-GitHub synchronization assumed.
No merge is authorized. Issue description cleanup requires final draft approval.

Verification in this continuation: five existing Python host checks PASS
(policy, 3 backup fixtures, 106-session/4-page paging, 922 EN/CZ resources,
v1–9→10 DDL); git diff --check and document/scope checks PASS. These are host
checks only. No Gradle, APK, lint, instrumentation, emulator, hosted runtime or
billing tests executed; no screenshots inspected. Only verified documentation
commit/push is authorized; production gates remain unchanged.

Q1/D5b UNRESOLVED and blocking; C4 OPEN; C2 §12 OPEN; 6.0f UNCHECKED;
6.1–6.18 UNCHECKED; 6.0d/6.0e NOT AUTHORIZED; ORD-A sequencing only;
permanent 0 CZK/month. No production implementation, deployment, project, billing,
real-user data, merge, tag or release. No device/emulator tests or screenshots.

# Goldberg 0.11 — Stage 5 VERIFICATION ledger

> **NOTE (2026-10-21, Stage 6 planning branch):** frozen historical ledger from before
> Stage 5 completion. Stage 5 is COMPLETE and owner-approved (2026-10-21); the final
> executed/not-executed record (incl. runs 39557624554 and 39576173002) lives on
> `feature/goldberg-0.11-stage-5`. Rows below are retained unchanged as historical
> evidence — no row was edited, added or removed here.

## Stage 6 documentation sessions (2026-10-21 — planning branch; no application code)

| # | Action | Result |
|---|---|---|
| 1 | C2 spike execution (separate owner authorization; isolated branch `spike/goldberg-0.11-c2-bff`, loopback-only, synthetic identities, no accounts/deployments) | **EXECUTED → CONDITIONAL PASS**: security **19/19**, performance **P1–P7 executed**, kernel-measured average CPU **2.03 ms/invocation** (flat under injected latency; 10 ms Workers Free ceiling re-verified from official pricing docs). Per-invocation production CPU **NOT verified** (local upper-bound argument only); §12 production-runtime verification **not executed — owner-gated**. Evidence: `spikes/bff-c2/results/SUMMARY.md` (spike branch, final commit `7bc6976`). |
| 2 | C3 edge-privacy documentary review (Cloudflare + Supabase + EU institutional sources; all accessed 2026-10-21) | **EXECUTED (documentation)** — `stage-6/C3-EDGE-PRIVACY-REVIEW.md`: 18 primary sources recorded with titles/access dates; conditional-viability verdict; no compliance claim. Not executed: any provisioning-time verification (§11 checklist), the Supabase subprocessor PDF parse, the Supabase TIA retrieval, any legal review. |
| 3 | C2/C3 status reconciliation across Stage 6 + `docs/ai/*` docs | **EXECUTED** — stale "C2 UNEXECUTED" current-state statements updated or superseded-noted (historical specs retained verbatim under supersession headers); C1/C2/C3/C4 statuses consistent everywhere; 6.0f still unchecked. |
| 4 | Q1 operator-readiness research (Cloudflare/Supabase/Google terms + Czech Civil Code; all accessed 2026-10-21) | **EXECUTED (documentation)** — `stage-6/Q1-OPERATOR-READINESS.md`: verified facts with sources (no published minimum account age on either provider; authority-to-bind warranties; CF SSSA §2.2.1(a); Czech CC §§30–33; Google age rule), Options A/B/C documented **without selection**, professional-review question set. **NOT executed / failed:** GDPR official text (EUR-Lex CELEX:32016R0679) retrieval — **0 bytes, 2 attempts** (environment-blocked; recorded as X-6); Czech MoJ official English Civil Code PDF — **curl exit 60, TLS failure** (authentic Czech text used instead); any account-signup observation; any legal review. **No legal conclusion drawn; D5b unchanged (unresolved/blocking).** |
| 5 | C4 pre-provisioning checklist preparation (documentation only) | **EXECUTED (documentation)** — `stage-6/C4-PREPROVISIONING-CHECKLIST.md`: per-item URLs, version/date stamps (CF SSSA 2025-09-12, CF DPA v6.4, CF pricing upd. 2026-10-02 w/ Workers Logs→Observability change 2026-12-01, Supabase ToS v2/2026-05-06, Supabase DPA v1/2026-08-01, Free-tier facts incl. 1 h auth / 1 day API-DB log retention and no automatic backups), verified-2026-10-21 vs must-reverify columns + §D provisioning-day runbook. **NOT executed:** the final C4 verification itself (stays OPEN), subprocessor PDF parse (SB-P9), TIA check (SB-P10), any console/API session with either provider, any account creation. |
| 6 | Cloud-readiness session (documentation-only): operator-decision reconciliation + gate-ordering analysis + future verification runbook | **EXECUTED (documentation)** — new `stage-6/CLOUD-READINESS-PLAN.md` (cloud architecture = intended deployment path instead of temporary local-only Option C; "minor owner" premise reclassified as an **unverified assumption**; 6.0f ↔ provisioning conflict analyzed with sequence S1–S8 and alternatives **ORD-A/B/C left pending as an owner decision**; private-cloud verification runbook CR-0…CR-8, synthetic data only; 0 CZK estimates; stop/rollback conditions); synchronized updates in `stage-6/CHECKLIST.md`, `OPEN-DECISIONS.md` §6, `Q1-OPERATOR-READINESS.md` (reconciliation note), `C3-EDGE-PRIVACY-REVIEW.md` (reconciliation note), `BFF-FEASIBILITY.md` §8, `README.md`, `STAGE-CHECKLIST.md` Stage 6, `docs/ai/*`. **NOT executed:** any account creation, provisioning, deployment, OAuth configuration, C4 final verification, C2 §12 production measurement, legal review. **No gate closed; 6.0f stays unchecked; D5b unchanged (unresolved/blocking).** |
| 7 | ORD-A approval session (documentation-only): record the owner's ORD-A sequencing decision and reconcile gates | **EXECUTED (documentation)** — `stage-6/CLOUD-READINESS-PLAN.md` updated: §3 rewritten as the **approved 11-step sequence** (ORD-A, supersession-marked), §4 replaced with **ORD-A APPROVED 2026-10-21 (sequencing only; ORD-B/ORD-C not selected)** + the reconciled **gate status table** (§4.2) + preserved not-selected alternatives (§4.3); new **ORD-A readiness checklist** in §5 (aligned with CR-0…CR-8: resolved Q1/D5b, confirmed authority to accept contracts, separate temp-resource approval, day-of C4, CF Workers Free only, Supabase Free `eu-central-1`, no billing/paid features, MFA, minimal temporary protected resources, synthetic data only, no public launch, CPU/cold-start/latency/quota/fail-closed measurements, secret-safe minimized logging, mandatory deletion + credential revocation, recorded cleanup evidence, immediate stop on blockers); owner-facing Q1 §4 question list (§9.1, identity-free); synchronized updates in `stage-6/CHECKLIST.md` (6.0f + progress log), `OPEN-DECISIONS.md` §6, `README.md`, `STAGE-CHECKLIST.md` Stage 6, `docs/ai/{STATE,HANDOFF,DECISIONS}.md`. **NOT executed:** any account creation, provisioning, deployment (incl. test Worker), OAuth configuration, credential access, C4 final verification, C2 §12 production measurement, legal review, money spending. **No gate closed; 6.0f stays unchecked; Q1/D5b, C2 §12, C4 stay open; 6.0d/6.0e stay NOT AUTHORIZED.** |
| 8 | Q1 natural-person operator reassessment (documentation-only): re-verify provider terms + Czech/EU sources, add candidate Q1-D, build the A/B/C/D decision matrix, record the free/non-commercial product direction | **EXECUTED (documentation)** — `stage-6/Q1-OPERATOR-READINESS.md` rewritten: REASSESSMENT NOTE + corrected standing constraints, re-verified §§30–37, new §1.6 (Czech business-status/trade-licensing: Civil Code §§15/18/420–421, Trade Act 455/1991 §2 + §3(1)(b)), §1.7 **PD-1 product direction** (free/non-commercial; no monetization approved or implemented — owner statement), §1.8 **QD-1…QD-10 findings classification**, §2 extended with Q2.9–Q2.14, §4 extended with **Q4.9–Q4.13**, §5 **Option D** (legally capable natural person in own name, candidate/not approved), **§5.5 A/B/C/D matrix**, §5.6 technical/non-legal recommendation (evaluate D first after review), §10 phased GDPR controller readiness; capacity framed purely as an **eligibility precondition, no personal facts**. Sources accessed 2026-10-21: Cloudflare SSSA (full text re-read), Supabase ToS at canonical `https://supabase.com/terms` (old `/company/terms` → 404), Czech Civil Code verze 18 (zakonyprolidi), Trade Act via e-Sbírka/zakonyprolidi/MPO, GDPR Art. 2/4/8 via **unofficial** gdpr-info.eu mirror. Synchronized: `stage-6/{README,CHECKLIST,OPEN-DECISIONS,CLOUD-READINESS-PLAN,C4-PREPROVISIONING-CHECKLIST}.md`, `STAGE-CHECKLIST.md` Stage 6, `docs/ai/{STATE,HANDOFF,DECISIONS,VERIFICATION}.md`. **NOT executed / failed:** official EUR-Lex GDPR retrieval — **0 bytes ×5 total** (environment-blocked; X-6 still ❌), any legal/professional review, any account-signup observation, any account creation, provisioning, deployment, OAuth configuration, implementation, money spending. **No option selected; no legal conclusion; no GDPR compliance claim; D5b unchanged (unresolved/blocking); 6.0f stays unchecked; 6.0d/6.0e stay NOT AUTHORIZED.** |
| 9 | Q1 professional-review handoff brief (documentation-only): compile the identity-free routing package with primary-source references, neutral A/B/C/D framing, official Czech-DPA + European Commission guidance, and the pre-resolution confirmation checklist | **EXECUTED (documentation)** — new `stage-6/Q1-PROFESSIONAL-REVIEW-BRIEF.md`: all **13 questions Q4.1–Q4.13 preserved verbatim** from Q1 §4 with per-question primary-source citations; neutral A/B/C/D table with **Q1-D explicitly a candidate, not approved**; §3 verified law (GDPR Art. 2/4(7)/8 via non-official mirror — official EUR-Lex still not retrieved, X-6; Czech CC verze 18; Trade Act 455/1991) vs **official guidance fetched 2026-10-22** — ÚOOÚ *Základní příručka* (https://uoou.gov.cz/verejnost/zakladni-prirucka-k-ochrane-udaju: controller responsibility + demonstrable compliance, risk-based self-analysis, legal form not decisive, cloud provider as typical processor, Art. 28(3) written contract, 1-month rights deadline, 72-hour breach rule), ÚOOÚ *Desatero* announcement (small controllers/živnostníci guidance), ÚOOÚ consultation practice, EC *Application of the GDPR* (household exception wording, "(paid or for free)" scope, Art. 4(7) role tests, SME/risk-based thresholds) and EC *Obligations* (Arts. 12–14/25/32/33–35/37–39) — each quoted with access date; §6 **C-1…C-6 confirmations required before Q1/D5b can be resolved**; §7 output format; §8 standing restrictions. Synchronized: stage-6 `README.md`, `OPEN-DECISIONS.md` (D5b row), `CHECKLIST.md` (progress log), `STAGE-CHECKLIST.md` Stage 6, `docs/ai/{STATE,HANDOFF,VERIFICATION}.md`. **NOT executed:** any legal/professional review (the brief only prepares for it), any personal-data request or record, any account creation, provisioning, deployment, OAuth, credentials, implementation, real user data, monetization, Stage 7 work, money spending. **No option selected; no approval simulated or claimed; D5b UNRESOLVED/blocking for 6.0d; C4 OPEN; 6.0f UNCHECKED; 6.0d/6.0e NOT AUTHORIZED; ORD-A sequencing-only; 0 CZK unchanged.** |

**No application source, test source, Gradle or dependency change was made in
any Stage 6 session. No credentials, personal data or contact details were
introduced.**

Honest executed vs not-executed record. Only rows marked **EXECUTED** were actually run.

## Executed — CI activation session (2026-10-20, fourth session, desktop host)

Phase A/B preparation for KVM CI verification (no CI run, no emulator run, no
instrumentation executed this session — nothing is claimed beyond these host-side checks):

| # | Command / action | Result |
|---|---|---|
| 1 | `apksigner verify --print-certs` (build-tools 36.0.0) on the downloaded trusted `Goldberg-v0.10.1.apk` | **EXECUTED** — APK sha256 `854bbfebf617ec920df60d181a12e0dcbd2fed10052f2479903a78a290cef312` (matches expected); signer cert SHA-256 `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e` = the trusted certificate (DN `C=US, O=Android, CN=Android Debug`). Proves a CI-runner debug-signed candidate **cannot** be installed in place over the release-signed baseline (different keystore) → workflow revised to rebuild the v0.10.1 baseline with the same runner debug keystore; production credentials stay out of CI. |
| 2 | AOSP source inspection of API 36 network APIs (`NetworkPolicyManagerShellCommand`, `NetworkPolicyManagerService.dump`, `ConnectivityService`, `NetworkAgentInfo.toString`) | **EXECUTED** — found the previous workflow used a **non-existent command** `cmd netpolicy set data_saver enable` on API 36; correct form is `set restrict-background true\|false` with `get restrict-background` → `Restrict background status: enabled\|disabled`; `dumpsys netpolicy` reports `Restrict background: true\|false`; default-network transport is assertable via `Active default network: <id>` + the id's `NetworkAgentInfo … ni{type: WIFI\|MOBILE …}` block. |
| 3 | `git ls-remote --tags` verification of all five SHA-pinned actions | **EXECUTED** — checkout `11d5960…`=v4.4.0, setup-java `cf277c6…`=v4.9.1, setup-android `9fc6c4e…`=v3.2.2, android-emulator-runner `a421e43…`=v2.38.0, upload-artifact `ea165f8…`=v4.6.2 — every pin resolves to its claimed release tag. |
| 4 | Workflow revision + full local validation (`yaml.safe_load`; `bash -n` on all 12 run/emulator scripts; `py_compile` + functional tests of both embedded Python snippets) | **EXECUTED** — all pass. The default-network parser was unit-tested against AOSP-format dumps: WIFI→OK, MOBILE→OK, transport-mismatch→FATAL, `none`→FATAL (one parser bug found and fixed during this: indented `NetworkAgentInfo` lines). |
| 5 | Exclusion scan: `@Ignore` in `app/src/androidTest`, `testInstrumentationRunnerArguments`/`excludeClasses`/`managedDevices` in Gradle | **EXECUTED** — **0 matches**: the CI `connectedDebugAndroidTest` runs all 42 androidTest classes with no filters, exclusions or skips. |
| 6 | v0.10.1 tag build-config inspection (`git show v0.10.1:…`) | **EXECUTED** — Java 17, compileSdk 36, Gradle 9.3.1, AGP 9.1.0, versionName `0.10.1`, versionCode 11 → the tag rebuild will work with the same CI toolchain and satisfies `verify_emulator_upgrade.py --baseline-version 0.10.1 --baseline-schema 8`. |
| 7 | `origin/main` helper-script inspection | **EXECUTED** — main's `verify_emulator_upgrade.py` is an older revision without the `--baseline-schema/--expected-schema/--offline/--startup-runs` flags → the workflow must be dispatched against `feature/goldberg-0.11-stage-5` (documented in the workflow header). |
| 8 | CI-only branch + draft PR creation (worktree `/tmp/opencode/goldberg-ci-only` off `origin/main`, branch `ci/stage5-emulator-verification`) | **EXECUTED** — commit `9870d99`; `git diff origin/main --stat` = **1 file** (`.github/workflows/stage5-emulator.yml`, +379); draft [PR #32](https://github.com/stokonn/Goldberg/pull/32) opened, **not merged**; `gh pr diff --name-only` confirms the single workflow file. Stage 5 working tree and all untracked files (`.pmp/`, `.pmp.yml`, `RELEASE_NOTES.md`, stage-4 checkpoint archive) untouched. |

## Not executed — CI activation session (no claim)

- **Any GitHub Actions run** — the workflow has never been dispatched; PR #32 awaits the
  owner's Phase C approval (merge + manual dispatch are gated).
- Any emulator run, any `Stage5*Test` instrumentation, any 5.11 acceptance item — all
  still pending the CI run (or notebook); tasks 5.10/5.11 remain open.
- The strengthened Data Saver/Wi-Fi assertions were validated against AOSP source and
  synthetic dumps only; their first real-device-format execution will be the CI run
  (a format surprise there would be diagnosed, never weakened away).

## Executed — third resume session (2026-10-20, desktop machine)

| # | Command | Result |
|---|---|---|
| 1 | `./gradlew --no-daemon --max-workers=1 :app:assembleDebug :app:assembleDebugAndroidTest` | **BUILD SUCCESSFUL**, 1m08s (Java 25 host); only known nullable-`listFiles()` warnings. Fresh `app-debug.apk` 32,388,569 B sha256 `44222c59…a157`; `app-debug-androidTest.apk` 1,331,040 B sha256 `6030e528…a8f2d` (Oct 20 21:13). |
| 2 | `sdkmanager "system-images;android-36;google_apis;x86_64"` (cmdline-tools 12.0 installed first) | Image r07 installed. |
| 3 | `avdmanager create avd -n stage5-api36 -k system-images;android-36;google_apis;x86_64 -d pixel_6` | AVD created (`~/.android/avd/stage5-api36`). |
| 4 | Emulator boots (explicit `-port 5580` / serial `emulator-5580`, headless): API 37 + swiftshader ×2 (37.2.12); API 37 + auto/lavapipe (37.2.12, ran 13 min, guest never booted, adb offline); API 36 + swiftshader (37.2.12); API 36 + auto/lavapipe (37.2.12); API 36 + swiftshader (37.3.4 canary) | **FAILED — `Killed by SIGSEGV`** on every completed attempt after `WARNING: cannnot unmap ptr … protected range …`. Known upstream bug (Google #566807521), Fedora kernel 7.2.10; unfixed in 37.3.4. **No emulator runs; no instrumentation executed.** |
| 5 | `python3 scripts/check_agent_policy.py` | **PASS** (exit 0): canonical policy, thin agent references, Cursor frontmatter, local Wiki navigation; runtime agent loading not tested. |
| 6 | `python3 scripts/check_backup_fixtures.py` | **PASS** (exit 0): 0.6.0-f1 / 0.9.1-f2 / 0.9.2-f3 fixtures → schema 10, all portable fields retained. |
| 7 | `python3 scripts/check_history_paging.py` | **PASS** (exit 0): 106 sessions / 4 pages + legacy association cases. |
| 8 | `python3 scripts/check_localization.py` | **PASS** (exit 0): 922 EN/CZ resources, placeholders, Czech plurals. |
| 9 | `python3 scripts/check_migrations.py` | **PASS** (exit 0): v5–v9 → v10 DDL, legacy data retained; bridge allocation requires Android tests (noted, not run). |

Host gate executed this session (2026-10-20, desktop):

| # | Command | Result |
|---|---|---|
| 10 | `./gradlew --no-daemon --max-workers=1 clean assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug` | **BUILD SUCCESSFUL**, 3m24s, 83/84 tasks executed. `testDebugUnitTest` **174 tests / 0 failures / 0 errors / 0 skipped** (30 classes). Lint **0 errors / 110 warnings** (2 in Stage 5 offline code: `UsableSpace`, `UseKtx`; rest pre-existing). Fresh clean-build `app-debug.apk` 22,508,542 B sha256 `b6ad01206d24b74e371edd16b9aec26d720f41afca8412557f1f7b21e3fb88ff` — **same byte size as the notebook checkpoint APK** (22,508,542 B), cross-machine source-identity evidence. `app-debug-androidTest.apk` 988,287 B (clean build). Note: the earlier incremental build (row 1) produced different-size APKs; the clean-build artifacts supersede them as the authoritative Stage 5 host-gate outputs. |
| 11 | GUI-mode emulator attempt (owner-requested): `DISPLAY=:0 XAUTHORITY=… .mutter-Xwaylandauth.* emulator -avd stage5-api36 -port 5580 -gpu auto` (no `-no-window`), emulator 37.2.12 | First launch aborted before QEMU (missing X authority in tool shell). Relaunch with session XAUTHORITY: Qt window created on the GNOME desktop, then **`Killed by SIGSEGV`** after the protected-range warning → **GUI mode also fails; no ADB device ever appeared.** All local emulator retries stopped per owner instruction. |
| 12 | CI preparation: verified GitHub-hosted runner KVM support + `reactivecircus/android-emulator-runner` v2.38.0 (pinned `a421e43855164a8197daf9d8d40fe71c6996bb0d`); wrote `.github/workflows/stage5-emulator.yml` (workflow_dispatch only, SHA-pinned actions, explicit emulator-NNNN + `ro.kernel.qemu=1` guards); `yaml.safe_load` validation; `py_compile` on `verify_emulator_upgrade.py`/`emulator_ui.py`; `bash -n` on `measure-startup.sh`; trusted baseline 0.10.1 APK digest `854bbfebf617ec920df60d181a12e0dcbd2fed10052f2479903a78a290cef312` recorded from the GitHub release API | **Workflow prepared locally; later committed+pushed as `1ef15bf` and revised in the CI-activation session (see the top section) — no CI results claimed.** |

## Not executed — third session (no claim)

- All `Stage5*Test` instrumentation (task 5.10) and the emulator acceptance matrix
  (task 5.11) — blocked by the desktop emulator bug (row 4, GUI mode too per row 11);
  execution paths are the notebook (per `docs/ai/HANDOFF.md`) or the prepared CI
  workflow (row 12) — neither has produced results yet. F1/F2 runtime proof still
  pending that run.
- `scripts/emulator_ui.py` / `verify_emulator_upgrade.py` / `measure-startup.sh` (need a
  booted emulator).

## Executed — second resume session (2026-10-20, battery-emergency stop)

| # | Command | Result |
|---|---|---|
| 1 | `./gradlew --no-daemon --max-workers=1 :app:assembleDebugAndroidTest` (after L324 fix) | **BUILD SUCCESSFUL**, 2m02s, 0 errors; known nullable-`listFiles()` warnings only. |
| 2 | `./gradlew --no-daemon --max-workers=1 :app:testDebugUnitTest --rerun` | **174 tests / 0 failures / 0 errors / 0 skipped** (30 classes, fresh). |
| 3 | `./gradlew --no-daemon --max-workers=1 :app:testDebugUnitTest :app:assembleDebugAndroidTest` (after F1 retarget + F2 `installBlob` production fix) | **BUILD SUCCESSFUL**, 2m10s; fresh JVM reports 19:23 → **174 / 0 / 0 / 0**. |
| 4 | `sdkmanager "emulator" "system-images;android-36;google_apis;x86_64"` | Installed emulator 37.2.12.0 + system image r07. |
| 5 | `avdmanager create avd -n stage5-api36 …` (needs `ANDROID_AVD_HOME=/home/stokonn/.config/.android/avd` for the emulator binary) | AVD created. |
| 6 | Emulator boot `… -avd stage5-api36 -port 5580 -no-window …` | **emulator-5580** booted; API 36 / Android 16 / `ro.kernel.qemu=1`; boot completed ~82s. |
| 7 | `adb -s emulator-5580 install -r` (app + androidTest APK) | Both `Success` — **but app-debug.apk was the stale Oct 16 build at that moment**. |
| 8 | `./gradlew --no-daemon --max-workers=1 :app:assembleDebug` | **BUILD SUCCESSFUL**, 5m44s → fresh `app-debug.apk` Oct 20 19:45, 22,508,542 bytes. **Not yet reinstalled.** |
| 9 | `adb -s emulator-5580 emu kill` | `OK: killing emulator` — clean stop at battery warning. |

## Executed — first resume session (2026-10-20)

| # | Command | Result |
|---|---|---|
| 1 | `:app:assembleDebugAndroidTest` (after runner-test fixes, before worker test) | BUILD SUCCESSFUL 4m54s, 0 errors. |
| 2 | `:app:testDebugUnitTest` | UP-TO-DATE → 0 tests re-executed (superseded by row 2 above). |

## Last previously executed evidence (prior session, per STAGE-CHECKLIST)

- Full host gate `clean assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug`:
  BUILD SUCCESSFUL, **146 tests / 0 failures**, lint 0 errors (109 warnings).
- After tasks 5.5–5.9: `testDebugUnitTest` **174 tests / 0 failures** (now reproduced fresh).

## Not executed this session (no claim)

- **All instrumentation tests** (`Stage5BundleStoreTest`, `Stage5BundleJobRunnerTest`,
  `Stage5BundleWorkerTest`) — emulator was booted and identified (emulator-5580) but the
  battery emergency stopped the session **before any test execution** (tasks 5.10/5.11 open).
- `lintDebug`, `assembleDebug` was run (row 8) but the full gate + `scripts/check_*.py`
  (migrations/backup/localization/paging/policy) — task 5.12 not run.
- Emulator acceptance matrix (Wi-Fi→mobile, Data Saver, consent revocation mid-stream, real
  process death, low disk, seeded in-place upgrade from trusted 0.10.1, cost measurements).
- F1/F2 fixes are compile-verified + JVM-regression-free; their **runtime** proof requires the
  instrumentation run in 5.10 (pending).

## Warning/error inventory at checkpoint

- androidTest compile: **0 errors**; warnings: 7+ nullable `listFiles()` receivers (runner
  test), 1 pre-existing `Stage3bBackupTest` reified-type warning.
- Installed-on-emulator app APK is stale (Oct 16) — reinstall fresh APKs before running tests.

## Executed — Stage 6 C6 contract spike (2026-10-22, isolated branch, local-only)

Evidence links below point at the stable spike commit
[`d2a95d4`](https://github.com/stokonn/Goldberg/tree/d2a95d4b83c634aacb4f7f74432f2bd8980cf218/spikes/contract-c6)
because the executable `spikes/contract-c6/` directory is intentionally not
part of this planning branch (code isolation preserved).

| # | Command | Result |
|---|---|---|
| C1 | `node tools/execute.mjs` (in `spikes/contract-c6/`) | **PASS — 79 tests / 79 pass / 0 fail**, exit 0, run `2026-10-22T06:40:20Z`→`06:40:23Z`, Node v22.23.1; static isolation scan: 17 files, **0 forbidden findings** (no http/net/dns/fetch/undici/wrangler surface; `child_process` only for the in-repo race test). Raw: [tap.txt](https://github.com/stokonn/Goldberg/blob/d2a95d4b83c634aacb4f7f74432f2bd8980cf218/spikes/contract-c6/results/raw/tap.txt), [run.json](https://github.com/stokonn/Goldberg/blob/d2a95d4b83c634aacb4f7f74432f2bd8980cf218/spikes/contract-c6/results/raw/run.json), summary-data.json (same directory). |
| C2 | `node --test tests/*.test.mjs` (run 1, debugging) | **69/79 pass, 10 fail** — all root-caused and preserved: 1 real server bug (uncaught idempotency `HttpError` → now 422 response), 6 test-side bugs (invalid revision flow; session TTL vs 24 h fake-clock jumps; wrong error-envelope requestId path; `GENESIS` first-link hash), 2 SSRF gaps (WHATWG hex-form v4-mapped `::ffff:7f00:1`; non-4-part numeric host) — see [SUMMARY.md §1 failure log](https://github.com/stokonn/Goldberg/blob/d2a95d4b83c634aacb4f7f74432f2bd8980cf218/spikes/contract-c6/results/SUMMARY.md). |
| C3 | `node --test tests/*.test.mjs` (run 2, after fixes) | **79/79 pass, 0 fail**, exit 0. |

Suite coverage executed: V1 validation (16), V2 authz (14), V3 existence (9),
V4 moderation (10), V5 idempotency (7), V6 failures (7), V7 SSRF-static (8),
V8 audit chain (6), V9 multi-process race (10 trials + 1 hygiene = 12 tests).

**Not executed / no claim:** C2 §12 production runtime, all [I]-venue rows, real
Postgres isolation, HTTP/JWT/CSRF/CORS (C2 scope), CPU measurement (C2),
tasks 6.1–6.18, Android/emulator checks (untouched), lint (no app code).
`node:sqlite` ExperimentalWarning recorded honestly in TAP. No integration or
production readiness is claimed; 6.0f stays UNCHECKED.

## Executed — Stage 6 Cloudflare MCP readiness verification (2026-10-22, read-only)

Owner-instructed, read-only session against the official Cloudflare MCP server
(OAuth, read-only permissions). **No cloud resource created/modified/deleted;
no OAuth permission, billing or account setting changed; no credential
accessed or recorded; no write scope exercised.** Non-identifying facts only
(no email, account ID or token in any repository file).

| # | Read-only call | Result |
|---|---|---|
| M1 | `GET /user` (MCP connection check) | **EXECUTED — HTTP 200, success.** Connection authenticates via OAuth. |
| M2 | `GET /user/tokens/verify` | **EXECUTED — 401 "Invalid API Token".** **Expected, not a defect:** that endpoint validates API tokens, not OAuth bearer sessions (M1 proves the OAuth session works). |
| M3 | `GET /accounts` | **EXECUTED — 1 standard account**, created 2026-10-22, not suspended, no pro/business/enterprise zones, account-level 2FA-enforcement flag off. |
| M4 | `GET /user` (account fields) | **EXECUTED — user-level two-factor authentication ENABLED** (D8c-relevant when actionable). |
| M5 | `GET /user/subscriptions` | **EXECUTED — 403, "Authentication error": no OAuth scope covers this endpoint.** **Billing state is NOT inspectable via this MCP connection; inaccessible billing is explicitly NOT evidence of zero costs** — the 0 CZK posture is enforced by the C4/ORD-A gates, never inferred from a tool limitation. |
| M6 | `GET /accounts/{id}/workers/scripts` | **EXECUTED — 0 Worker scripts.** |
| M7 | `GET /accounts/{id}/workers/subdomain` | **EXECUTED — 404, code 10007:** workers.dev subdomain **not yet created** (auto-created only on the first Workers & Pages dashboard visit — not performed). |
| M8 | `GET /zones` (filtered by account) | **EXECUTED — 0 zones** (no domains on the account). |
| M9 | `GET /accounts/{id}/d1/database` | **EXECUTED — 0 D1 databases.** |
| M10 | `GET /accounts/{id}/storage/kv/namespaces` | **EXECUTED — 0 KV namespaces.** |
| M11 | `GET /accounts/{id}/r2/buckets` | **EXECUTED — 403, code 10042:** R2 **not enabled** on the account (dashboard enablement required; not performed — R2 is not needed by the approved architecture). |

**Read-scope evidence:** the Workers R2 *read* scope is present on the
connection (error text of M11 confirms it); billing endpoints are refused for
lack of any OAuth scope (M5). Read-only posture therefore verified from
observed behavior, not assumed.

**Not executed / no claim:** any write/create/update/delete call of any kind;
any billing inspection, change or interpretation; any provisioning,
deployment, Wrangler action or OAuth scope change; C4 final verification
(stays OPEN); C2 §12 production-runtime measurement (stays OPEN — no Worker
deployed); any Supabase inspection (out of MCP scope — Supabase remains
unverified/unprovisioned from this side); legal/professional review.
**No gate closed: Q1/D5b UNRESOLVED (the existing account does NOT resolve
it); 6.0f UNCHECKED; 6.0d/6.0e NOT AUTHORIZED; 6.1–6.18 unchecked; ORD-A
sequencing-only; Stage 7 untouched; 0 CZK/month unchanged.**

## C6-PG spike — executed verification record (2026-10-22)

- **Executor (executed):** `node tools/execute-pg.mjs` with
  `C6_PG_URL=postgres://c6spike@127.0.0.1:54329/c6spike` on branch
  `spike/goldberg-0.11-stage-6-contract-pg` @ `f7bef8e` (evidence committed at
  `e37ed00`), Node v22.23.1. Exit 0.
- **PostgreSQL venue (real engine):** `tests/` v1–v10 → **86 tests, 86 pass,
  0 fail, 0 skipped** — PostgreSQL **17.11** (podman `c6-pg`, removed after
  capture), `default_transaction_isolation = read committed`, host port
  `127.0.0.1:54329` (loopback only, trust auth), synthetic data, per-test
  schemas. Raw TAP:
  [tap-postgres.txt](https://github.com/stokonn/Goldberg/blob/e37ed00b9205ce22e9e8ec494651e900708ee1e3/spikes/contract-c6/results/pg/raw/tap-postgres.txt).
- **SQLite post-port regression venue:** **86 tests, 79 pass, 0 fail,
  7 skipped** (v10 is PostgreSQL-only; the skips are honest). Raw TAP:
  [tap-sqlite-postport.txt](https://github.com/stokonn/Goldberg/blob/e37ed00b9205ce22e9e8ec494651e900708ee1e3/spikes/contract-c6/results/pg/raw/tap-sqlite-postport.txt).
  Baseline comparison: the original C6 run at `d2a95d4` was 79/79 — no
  regression introduced by the async port.
- **Static isolation scan:** 20 files, 0 forbidden findings (only socket use:
  `pg` driver → loopback instance).
- **Observed race outcomes (not asserted away):** v9 SQLite 10/10
  `[201,409]`; v9 PostgreSQL 10/10 `[201,500]`/`[500,201]` (10/10 T10-invariant
  deviations), plus deterministic ISO-1..ISO-3 probes in v10 — findings
  F1–F5 written up in
  [results/SUMMARY-PG.md](https://github.com/stokonn/Goldberg/blob/e37ed00b9205ce22e9e8ec494651e900708ee1e3/spikes/contract-c6/results/SUMMARY-PG.md).
- **Not executed / not claimed:** Supabase-hosted (PostgREST) runs — blocked
  by D5b/Q1 + C4 + ORD-A steps 4–6; production implementation tests
  (6.1–6.18); HTTP/CSRF/CORS layer (C2 scope); instrumentation on any device
  (no emulator/device work in this spike); any [I]-venue row.
- **Cleanup:** container `c6-pg` stopped and removed after evidence capture;
  `.scratch/` is gitignored; no cloud resources, no billing, no user data.

**Gates unchanged: 6.0f UNCHECKED; 6.1–6.18 unchecked; Q1/D5b UNRESOLVED
(Q1-D preference only); C4 OPEN; C2 §12 OPEN; ORD-A sequencing-only;
0 CZK/month; Stage 7 untouched; Stage 5 remains COMPLETE.**

## Executed — Stage 6 C4 preliminary verification & documentation reconciliation (2026-10-22, documentation-only)

**Method:** public document fetches only (webfetch/curl). No console login, no
MCP/API billing call, no resource created, no payment method inspected, no
account setting touched. Reconciliation target: the 2026-10-21 source set in
the C4 checklist §E.

| # | Source | Result (accessed 2026-10-22) |
|---|---|---|
| V-1 | Cloudflare Workers pricing | **Workers Free available** — 100k req/day, 10 ms CPU/invocation, static assets free & unlimited; "Last updated Oct 2, 2026" = unchanged; Workers Logs Free 200k events/day, 3-day retention; "Beginning December 1, 2026 …" announcement unchanged |
| V-2 | Cloudflare Observability pricing (**NEW source, not in §E**) | **"Last updated Oct 15, 2026":** Free = 0.5 GB/day ingestion + 7-day retention included, **additional ingestion unavailable on Free** (hard stop, resumes 00:00 UTC), storage not separately metered; automatic Free/Pro/Business migration **2026-12-01**; Log Explorer separate $1/GB, 30-day pool → **preliminary 0-CZK read; re-verify day-of and ≥ 2026-12-01** |
| V-3 | Cloudflare Self-Serve Subscription Agreement | "Last Updated September 12, 2025" — unchanged |
| V-4 | Cloudflare Customer DPA | "Version 6.4 … effective April 3, 2026" — unchanged |
| V-5 | Cloudflare Workers limits | dateModified 2026-10-12 — unchanged; fail-closed error-routing code **1027** present |
| V-6 | Cloudflare subprocessors page | "Last Updated: October 1, 2025" — unchanged |
| V-7 | Cloudflare Website Terms | **date not re-extractable this session** (client-rendered) — honest gap; last verified Effective 2025-08-01 on 2026-10-21; remains ⚠ day-of |
| V-8 | Supabase pricing | **Free available** — 500 MB DB (shared CPU) / 5 GB egress / 5 GB cached / 1 GB storage / 50k MAU / 2 projects / 1 GB log ingestion / audit 1 h / API+DB logs 1 day / unlimited API requests / pause 1 week; **Automatic backups "Not included" on Free** |
| V-9 | Supabase backups docs | Daily backups Pro (7 d) / Team (14 d) / Enterprise; official guidance: "free tier plan projects regularly export their data using the Supabase CLI `db dump` command and maintain off-site backups" |
| V-10 | Supabase billing FAQ | Free quota exceed → notification → grace period → **"service restrictions will apply"**: project pause, database read-only, disabled new launches/transfers, **402 for all API requests**; no-prior-notice only for suspected abuse |
| V-11 | Supabase regions | **`eu-central-1` "Central EU (Frankfurt)" listed under Specific regions**; "Region selection is a data-location control, not proof of regulatory compliance" unchanged |
| V-12 | Supabase DPA | "Version 1 — August 1, 2026" — unchanged |
| V-13 | Supabase Terms of Service | **version badge not re-renderable this session** — honest gap; last verified "Version 2 — May 6, 2026" on 2026-10-21; remains ⚠ day-of |
| V-14 | Supabase subprocessor list page | "Updated October 12, 2026" / "Updated June 1, 2026" — unchanged; **PDF still not parsed (SB-P9 ❌)** |
| V-15 | Issue #31 MCP checkpoint `6343122203` (prior session, re-read) | read-only MCP auth verified (`GET /user` 200); 1 CF account created 2026-10-22, 0 zones / 0 Workers; billing refused 403 — **explicitly not zero-cost evidence** |

- **Recorded from owner instruction (not agent-verified):** both provider
  accounts now exist (Cloudflare corroborated by V-15; Supabase existence
  from the owner's statement only); **Q1-D = owner's preferred structure
  subject to eligibility verification** (preference only, `78fece3`).
- **Not executed / not claimed:** console sessions; billing/payment-method
  inspection; subprocessor PDF parse; TIA/Trust Center access; official GDPR
  retrieval (X-6); MoJ translation (X-7); in-flow region selection; any
  provisioning, deployment, account change, OAuth change; **C4 closure** (§D
  steps 1–9 all unchecked); any Stage 7 work.

**Gates unchanged: C4 OPEN; 6.0f UNCHECKED; 6.1–6.18 unchecked; Q1/D5b
UNRESOLVED (Q1-D preference only, subject to eligibility verification); C2 §12
OPEN; 6.0d/6.0e NOT AUTHORIZED; ORD-A sequencing-only; 0 CZK/month; Stage 7
untouched; Stage 5 remains COMPLETE.**

## C6-PG concurrency hardening — executed verification record (2026-10-22)

- **Executor (executed):** `node tools/execute-hardening.mjs` with
  `C6_PG_URL=postgres://c6spike@127.0.0.1:54329/c6spike` on branch
  `spike/goldberg-0.11-stage-6-contract-pg` (code `d8e82d5`, evidence
  `c21e550`), Node v22.23.1, PostgreSQL 17.11 (podman `c6-pg`, rootless,
  loopback-only, `read committed`, removed after capture). Exit 0.
- **PostgreSQL hardening venue:** v1–v11 → **123 tests, 123 pass, 0 fail,
  0 skipped**. Raw TAP:
  [tap-postgres-hardening.txt](https://github.com/stokonn/Goldberg/blob/c21e550ceb5ec7a42317a4f2fe02e6d1225a2cab/spikes/contract-c6/results/pg-hardening/raw/tap-postgres-hardening.txt).
- **SQLite regression venue (faithful server only):** **93 tests, 79 pass,
  0 fail, 14 skipped** (v10 + v11 are PostgreSQL-only — honest skips; v1–v9
  unchanged). Raw TAP:
  [tap-sqlite-regression.txt](https://github.com/stokonn/Goldberg/blob/c21e550ceb5ec7a42317a4f2fe02e6d1225a2cab/spikes/contract-c6/results/pg-hardening/raw/tap-sqlite-regression.txt).
- **v11 hardening suite (PG-only, 37 tests, 7/7 pass):** in-process races
  (both strategies × new/pre-existing entity) `[201,409]` with exact
  one-decision counts and coherent chain; **repeated 20-trial multi-process
  barrier race: 20/20 `[201,409]` per strategy** (for-update telemetry all
  zero; serializable 21× `40001`, 0 exhausted); contract/negative behavior
  (401/403/400/409/503, kill switch, fault-injection rollback, concurrent
  same-key `[201,409]`); **deterministic F3 store race** (23505 → stored
  replay, exactly 1 row, 422 negative); **F4 scale run** (20 concurrent
  decisions: all 201, audit seqs strictly consecutive, chain coherent;
  for-update 0 retries, serializable 218× `40001` with maxAttempts=64).
- **Static isolation scan:** 23 files, 0 forbidden findings (`child_process`
  confined to the race tests + child tools; only socket use: `pg` driver →
  loopback). Original `results/pg/` evidence verified untouched
  (`git status`: only new files added).
- **Not executed / not claimed:** any Supabase-hosted (PostgREST) run —
  blocked by 6.0f + D5b/Q1 + C4 + ORD-A steps 4–6; production implementation
  (6.1–6.18); any device/emulator work; any merge/deploy/release.
- **Cleanup:** container `c6-pg` stopped and removed after evidence capture.

**Gates unchanged: 6.0f UNCHECKED; 6.1–6.18 unchecked; Q1/D5b UNRESOLVED
(Q1-D preference only); C4 OPEN; C2 §12 OPEN; ORD-A sequencing-only;
0 CZK/month; Stage 7 untouched; Stage 5 remains COMPLETE.**

## Owner-attributed C4 console observations recorded (2026-10-22, NOT agent-executed)

- **Recorded from owner instruction (manual, first-hand, never agent-verified):**
  (a) Cloudflare Workers Free **active with no payment method attached**;
  (b) a Supabase **"Personal Free" organization exists with zero projects**
  (no project created); (c) **Frankfurt (`eu-central-1`) is selectable in the
  Supabase project-creation form**. Recorded as §F.5 (N-11–N-13) of
  `stage-6/C4-PREPROVISIONING-CHECKLIST.md`.
- **Not executed / not claimed:** any agent console session, billing or
  payment-method inspection (MCP has no billing scope — inaccessible billing
  is explicitly not zero-cost evidence), project creation, resource
  modification; any screenshot/console export stored in GitHub (none —
  privacy rule); any §D step closure. Supabase billing/payment status
  remains unverified; SB-P7/§D step 6 remains ⬜ (in-flow selectability
  partially informed, not closed).

**Gates unchanged: C4 OPEN (§D steps 1–9 unchecked); 6.0f UNCHECKED;
6.1–6.18 unchecked; Q1/D5b UNRESOLVED and blocking; C2 §12 OPEN; 6.0d/6.0e
NOT AUTHORIZED; ORD-A sequencing-only; 0 CZK/month; Stage 7 untouched;
Stage 5 remains COMPLETE.**
