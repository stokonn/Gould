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

Eight-file documentation/link/gate/SQL checks and five existing host checks PASS.
Exact commands/limitations: docs/ai/VERIFICATION.md. Documentation-only publication
is authorized; no hosted or application test is implied.

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

Eight-file documentation consistency/link/gate/SQL-preservation checks and the
five existing host scripts PASS; see docs/ai/VERIFICATION.md for exact commands
and limitations. No hosted checks or completed concurrency tests rerun by this agent.

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

# Goldberg 0.11 — Stage 5 STATE (recovery checkpoint)

> **SUPERSESSION NOTE (2026-10-21, Stage 6 planning branch):** frozen historical
> checkpoint from before Stage 5 completion. **Stage 5 is COMPLETE and owner-approved
> (2026-10-21)** — CI runs 39557624554 (177/0/0/2) and 39576173002 (12/12 screenshots);
> the authoritative record is on `feature/goldberg-0.11-stage-5` (completion commit
> `736f6d3`). Everything below is retained as historical evidence only.

> **Stage 6 addendum (2026-10-21, later session — Stage 6 planning branch,
> documentation-only):** C2 spike executed → **CONDITIONAL PASS**
> (`spike/goldberg-0.11-c2-bff`, final commit `7bc6976`, spike code commit
> `d2ce77b`; 19/19 security, P1–P7 executed, kernel CPU **2.03
> ms/invocation**; per-invocation production CPU **unverified**; §12
> production-runtime verification **owner-gated**; spike code unmerged).
> C3 edge-privacy documentary review **complete**
> (`docs/planning/goldberg-0.11/stage-6/C3-EDGE-PRIVACY-REVIEW.md`: conditional
> privacy viability at 0 CZK, **no compliance claim**, legal/operator questions
> open — controller/operator identity incl. the minor-owner legal-capacity
> question is now blocking for 6.0d). **Q1 operator-readiness research + C4
> pre-provisioning checklist prepared 2026-10-21**
> (`stage-6/Q1-OPERATOR-READINESS.md`: verified facts, **Options A/B/C with no
> selection, no legal conclusion — D5b still unresolved/blocking**; `stage-6/
> C4-PREPROVISIONING-CHECKLIST.md`: dated baseline + day-of runbook, **C4 final
> verification still OPEN**; EUR-Lex GDPR retrieval failed 0 bytes ×2, recorded
> as not-retrieved). C4 open; **task 6.0f unchecked; Stage 6
> implementation NOT STARTED**; nothing provisioned or deployed. Authoritative
> current state: `docs/planning/goldberg-0.11/stage-6/CHECKLIST.md` (progress
> log) + GitHub Issue #31 checkpoints.

> **Stage 6 addendum 2 (2026-10-21, later session — cloud readiness, docs only):**
> the owner chose the **cloud architecture as the intended deployment path**
> (Cloudflare Workers Free static + `/v1` BFF; Supabase Free `eu-central-1`;
> strict 0 CZK) **instead of the temporary local-only Option C** — Option C is
> now only the current gated posture; **no gate changed, nothing provisioned**
> (documentation/verification planning only). The earlier "owner is a minor"
> premise is **reclassified as an unverified assumption, not a verified current
> fact** — no age/identity/family detail requested or recorded; **D5b stays
> legally unresolved and blocking for 6.0d**. New:
> `docs/planning/goldberg-0.11/stage-6/CLOUD-READINESS-PLAN.md` — the 6.0f ↔
> provisioning **gate-ordering** analysis (explicit minimal sequence S1–S8;
> alternatives **ORD-A/B/C pending an owner decision**) and the future
> private-cloud verification runbook CR-0…CR-8 (accounts, no-billing checks,
> Frankfurt region, secrets+MFA, CPU/cold-start/Supabase-latency/quota/
> fail-closed/logging measurements, rollback+cleanup; **synthetic data only**;
> 0 CZK estimates; stop conditions). **6.0f stays UNCHECKED; Stage 6
> implementation NOT STARTED; C4 open; D5b open; Stage 5 remains COMPLETE.**

> **Stage 6 addendum 3 (2026-10-21, ORD-A approval session — docs only):** the
> owner **APPROVED ORD-A — Scoped Private Cloud Verification Provisioning** as
> the sequencing strategy for closing 6.0f (**ORD-B/ORD-C not selected**;
> sequencing-only — not acceptance of 6.0f, not a D5b resolution, not
> authorization to create accounts, provision, deploy, configure OAuth or
> implement). Recorded in `stage-6/CLOUD-READINESS-PLAN.md` §§3–5: the
> approved 11-step sequence, the reconciled gate table (C1 approved · C2
> conditional pass · C2 §12 open · C3 complete · C4 open · Q1/D5b open ·
> ORD-A approved (sequencing only) · **6.0f UNCHECKED** · 6.0d/6.0e NOT
> AUTHORIZED · Stage 6 implementation NOT STARTED), the ORD-A readiness
> checklist for the future owner decision, and the concise owner-facing Q1 §4
> professional-review question list (§9.1, routed outside GitHub). Q1 Options
> A/B/C (operator arrangements) remain distinct and unselected; the "minor
> owner" premise remains an unverified assumption; no personal data of any
> kind is requested or recorded. **Next owner action: route the Q1 §4
> questions to a professional review to resolve D5b** — until then nothing may
> be provisioned. Stage 5 remains COMPLETE.

> **Stage 6 addendum 4 (2026-10-21, Q1 natural-person reassessment session —
> docs only):** `stage-6/Q1-OPERATOR-READINESS.md` was reassessed and rewritten
> after fresh source checks (Cloudflare SSSA re-read; Supabase ToS at new
> canonical `https://supabase.com/terms` — old path 404s; Czech Civil Code
> verze 18 eff. 2026-01-01 §§15/18/30–37/420–421; Trade Act 455/1991 §2/§3(1)(b);
> GDPR Art. 2/4/8 from a **non-official mirror — official EUR-Lex retrieval now
> failed 5× total, X-6 still ❌**). The option set is now **A/B/C/D**, adding
> candidate **Q1-D — a legally capable natural-person operator acting in their
> own name** — with a §5.5 decision matrix and a clearly labeled **technical
> (non-legal)** recommendation (§5.6) to evaluate D first *after* professional
> review; §4 extended with Q4.9–Q4.13; new §1.6 trade-licensing, §1.7 product
> direction **PD-1 (free of charge; non-commercial; no monetization approved or
> implemented)**, §10 phased GDPR controller readiness. **Capacity is treated
> solely as an eligibility precondition for the eventual accountable party —
> never a personal fact requested, recorded or inferred.** Synchronized:
> stage-6 README/OPEN-DECISIONS/CLOUD-READINESS-PLAN/C4, STAGE-CHECKLIST,
> docs/ai/*. **No option selected; D5b UNRESOLVED and blocking for 6.0d; no
> GDPR compliance claimed; professional review still required; ORD-A stays
> sequencing-only; 6.0f stays UNCHECKED; 6.0d/6.0e NOT AUTHORIZED; Stage 6
> implementation NOT STARTED; nothing provisioned; Stage 5 remains COMPLETE.**

> **Stage 6 addendum 5 (2026-10-22, Q1 professional-review handoff — docs only):**
> new `stage-6/Q1-PROFESSIONAL-REVIEW-BRIEF.md` — the identity-free routing
> package for a qualified professional: all **13 questions Q4.1–Q4.13 verbatim**
> with primary-source references; neutral Q1-A/B/C/D framing (**Q1-D is a
> candidate for review, explicitly not an approved solution**); verified law vs
> unresolved interpretation separated; official **ÚOOÚ** guidance (Základní
> příručka §7 — legal form not decisive for controller/processor, cloud provider
> as typical processor, risk-based self-analysis; Desatero for small controllers;
> consultation practice) and **European Commission** guidance (Application of the
> GDPR — household exception wording, free services in scope, Art. 4(7) roles;
> Obligations — Arts. 12–14/25/32/33–35/37 thresholds), all fetched 2026-10-22;
> and the **C-1…C-6 confirmations required before Q1/D5b can be resolved**.
> Official EUR-Lex GDPR text still unretrieved (5 failures, X-6). **No review
> performed or simulated; no option selected; no personal data; D5b UNRESOLVED,
> C4 OPEN, 6.0f UNCHECKED, 6.0d/6.0e NOT AUTHORIZED, ORD-A sequencing-only, 0 CZK
> unchanged; Stage 6 implementation NOT STARTED; Stage 5 remains COMPLETE.**

> **Last updated:** 2026-10-20, CI-activation session (Phase A/B of the owner's CI plan;
> approval gate reached — nothing dispatched or merged).
> Read order for a fresh session: `AGENTS.md` → this file → `docs/ai/HANDOFF.md` →
> `docs/planning/goldberg-0.11/STAGE-CHECKLIST.md` (Stage 5 section) → GitHub Issue #31.
> **Stage 5 is NOT complete. No merge/tag/release/APK publication performed.**

## 🚦 Current status: awaiting owner approval (Phase C gate)

- The Stage 5 emulator verification workflow was **revised and CI-only PR prepared**:
  draft [PR #32](https://github.com/stokonn/Goldberg/pull/32), branch
  `ci/stage5-emulator-verification` (based on `origin/main`), commit `9870d99` —
  **workflow-only diff (1 file), NOT merged**.
- **Phase B findings fixed in the workflow** (details in `docs/ai/VERIFICATION.md`
  CI-activation section): in-place upgrade now uses a same-keystore debug rebuild of
  the v0.10.1 tag (CI debug can never install over the release-signed APK; trusted cert
  `53eface4…` apksigner-verified); the invalid `cmd netpolicy set data_saver enable`
  was replaced with AOSP-verified `set/get restrict-background` + `dumpsys` state
  assertions; Wi-Fi→mobile transition now asserts transport + network-ID change;
  process death now includes pulled-DB `PRAGMA integrity_check/foreign_key_check/
  user_version`; source commit SHA recorded+asserted in both jobs; 30-day evidence
  retention. The identical revised workflow is committed on this Stage 5 branch too
  (so a `workflow_dispatch` against `feature/goldberg-0.11-stage-5` uses the fixed
  version).
- **STOPPED here per owner instruction:** merging PR #32 into `main` and manually
  dispatching the workflow both require explicit owner approval. Until then: no CI run,
  no instrumentation, no 5.11 evidence. Tasks 5.10–5.13 remain open.
- This checkpoint commit contains **only** the revised `.github/workflows/stage5-emulator.yml`
  (identical to PR #32's file) and documentation sync (`docs/ai/STATE.md`,
  `docs/ai/VERIFICATION.md`, `docs/ai/HANDOFF.md`,
  `docs/planning/goldberg-0.11/STAGE-CHECKLIST.md`) — **no application/test source
  changes**.

## ⚠️ Emulator blocker on the desktop machine (2026-10-20, third session)

Task 5.10/5.11 are **blocked by a host-level Android emulator bug on this desktop**:

- This is a **different machine** than the notebook (Fedora, kernel
  `7.2.10-200.fc44.x86_64`, Java 25, 15 GiB RAM, no cmdline-tools, no stage5 AVD).
- The desktop's only pre-existing AVD (`Medium_Phone_API_37.0`, google_apis_playstore)
  and a freshly created notebook-style AVD (`stage5-api36`, google_apis API 36 x86_64,
  r07 — installed via newly added cmdline-tools 12.0) were both tried.
- **Every headless boot attempt ends in `Killed by SIGSEGV`** of the qemu process a few
  minutes into guest boot, right after the persistent
  `WARNING: cannnot unmap ptr … as it is in the protected range …` line. Configurations
  tried (all with explicit serial `emulator-5580`, never a physical device):
  API 37 + swiftshader ×2, API 36 + swiftshader (emulator 37.2.12), API 36 + `-gpu auto`
  (lavapipe/swangle, 37.2.12), API 36 + swiftshader (emulator **37.3.4** canary, newest
  published build, installed side-by-side at `~/Android/Sdk/emulator-37.3.4`). One
  API 37 run with lavapipe/swangle stayed alive 13 min but the guest never completed
  boot (`adb` stuck offline).
- This matches the known upstream bug **Google Issue Tracker #566807521** ("Android
  Emulator crashes with Segmentation fault during headless boot on modern Linux/Fedora",
  same protected-range warning, KVM present, independent of GPU option). Not fixed in
  37.3.4.
- Conclusion: **no emulator on this desktop can currently boot**; tasks 5.10/5.11 cannot
  be executed here. No instrumentation tests were executed. Possible paths (owner
  decision): run 5.10/5.11 on the notebook (its `stage5-api36` AVD booted fine), wait for
  an upstream emulator fix, or try a pre-37 emulator binary (not offered by the SDK
  channel; direct-zip install would need owner approval).

## Git / branch state

- Branch `feature/goldberg-0.11-stage-5`, parent `54f2b45` + Stage 5 checkpoint commits.
  **Pushed to origin as owner-authorized IN-PROGRESS checkpoints** (verification
  incomplete — see VERIFICATION.md for what remains unexecuted). No merge/tag/release.
  The third-session checkpoint is **docs-only** (this file, HANDOFF, VERIFICATION,
  STAGE-CHECKLIST): it records the desktop emulator blocker + host-gate evidence and
  adds no production/test code changes.
- In the second-session checkpoint commit (`c61cf12`):
  - `M app/src/androidTest/java/com/opus/piano/Stage5BundleJobRunnerTest.kt` — compile fixes,
    fixture corrections, F1 retarget (owner-approved, test-only).
  - `M app/src/main/java/com/opus/piano/data/offline/BundleStore.kt` — F2 fix
    (owner-approved): installBlob dedup now verifies existing target length+sha256; invalid
    bytes moved to quarantine (`dedup_corrupt`, never deleted), verified staged copy installed
    so the same attempt repairs; quarantine-rename failure returns false (fail-safe).
  - `?? app/src/androidTest/java/com/opus/piano/Stage5BundleWorkerTest.kt` — 12-test draft,
    L324 nullable `TestDriver?` compile error FIXED (`?.setAllConstraintsMet`, assertions
    unchanged).
  - `?? docs/ai/*` — this checkpoint (STATE/VERIFICATION/HANDOFF/DECISIONS).
  - `M docs/planning/goldberg-0.11/STAGE-CHECKLIST.md` — Stage 5 progress notes.

## Executed this session (2026-10-20, third resume session — desktop) — real results

1. Environment re-verified: branch `feature/goldberg-0.11-stage-5` at `c61cf12`, in sync
   with origin; untracked private files (`.pmp/`, `.pmp.yml`, `RELEASE_NOTES.md`,
   `docs/planning/goldberg-0.11/stage-4/checkpoint-archive/`) untouched and NOT committed.
2. **EXECUTED** `./gradlew --no-daemon --max-workers=1 :app:assembleDebug
   :app:assembleDebugAndroidTest` → **BUILD SUCCESSFUL** (1m08s, Java 25 host; only the
   known nullable-`listFiles()` warnings). Fresh APKs: `app-debug.apk` 32,388,569 bytes
   (sha256 `44222c59b89ed63041d1e105f074857f5ca3c2f93efef4a9ed0eea115dd0a157`),
   `app-debug-androidTest.apk` 1,331,040 bytes (sha256
   `6030e528474334ae31f0514c0f818aeeb68dcf3f9b2ac35e453762d1e5ba8f2d`), both Oct 20 21:13.
   (These were rebuilt because the desktop's pre-existing outputs were from a different
   build context; they are Stage-5 source-faithful.)
3. Emulator toolchain: cmdline-tools 12.0 installed (side-effect: fixes sdkmanager/
   avdmanager on this desktop), `system-images;android-36;google_apis;x86_64` r07
   installed, AVD `stage5-api36` created in `~/.android/avd`. Emulator 37.3.4 downloaded
   side-by-side (SDK's 37.2.12 untouched). **All boot attempts SIGSEGV — see blocker
   section above. Zero instrumentation executed; APKs not installed (no device to install
   to; the earlier installs were on the notebook's now-unavailable emulator).**
4. **EXECUTED** `scripts/check_agent_policy.py`, `check_backup_fixtures.py`,
   `check_history_paging.py`, `check_localization.py`, `check_migrations.py` → **ALL PASS**
   (exit 0; see VERIFICATION.md row detail).
5. **EXECUTED** full host gate
   (`clean assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug`) →
   **BUILD SUCCESSFUL** (3m24s): unit tests **174/0/0/0** (30 classes), lint
   **0 errors / 110 warnings** (2 in Stage 5 offline code). Clean-build `app-debug.apk`
   22,508,542 B — same byte size as the notebook checkpoint APK (source-identity
   evidence). Task 5.12 host-side portion therefore executed and passing; 5.12 stays
   open only as a formality until 5.10/5.11 evidence exists for the final re-run.
6. **GUI-mode emulator attempt (owner-requested, one controlled configuration)** —
   first launch failed before QEMU started (tool shell lacked `XAUTHORITY`); relaunched
   with the session's `/run/user/1000/.mutter-Xwaylandauth.*` → Qt window opened on the
   GNOME desktop, then **`Killed by SIGSEGV` again** after the protected-range warning.
   GUI mode does not avoid the bug → **all local emulator retries stopped** (owner
   instruction). Details in VERIFICATION.md row 11.
7. **CI investigation + workflow prepared (not yet committed/pushed — pending owner
   approval)** — GitHub-hosted `ubuntu-24.04` runners support KVM-accelerated Android
   emulation (GitHub Changelog 2024-04-02) and
   `reactivecircus/android-emulator-runner` v2.38.0 is current. Prepared
   `.github/workflows/stage5-emulator.yml`: **`workflow_dispatch` only** (no automatic
   runs), `permissions: contents: read`, SHA-pinned actions, KVM enable step, explicit
   `emulator-NNNN` serial + `ro.kernel.qemu=1` guard with abort-on-multiple-devices.
   Job 5.10 runs the **complete** `connectedDebugAndroidTest` suite (no selection/
   skipping). Job 5.11 runs the CI-executable acceptance subset: digest-verified
   in-place upgrade from trusted 0.10.1 (script-asserted), Wi-Fi→mobile + Data Saver
   evidence, process death + cold restart with DB integrity check, 5× startup
   measurements, failure logcat capture. Explicitly NOT covered (notebook follow-up):
   consent revocation mid-stream, low disk, screenshot inspection, real bundle
   transfer costs. YAML validated (`yaml.safe_load` OK); helper scripts compile.

## Executed — second resume session (2026-10-20, battery emergency stop) — real results

1. Fixed the 1 open compile error (`Stage5BundleWorkerTest.kt:324` nullable TestDriver call).
2. **EXECUTED** `:app:assembleDebugAndroidTest` → **BUILD SUCCESSFUL** (2m02s, 0 errors;
   only the 7+ known nullable-`listFiles()` warnings).
3. **EXECUTED** `testDebugUnitTest --rerun` → **174 tests / 0 failures / 0 errors / 0 skipped**
   (30 classes, fresh evidence).
4. **F1 + F2 fully investigated and confirmed against source; owner approved both recommended
   resolutions** (recorded in `docs/ai/DECISIONS.md`):
   - F1 retarget implemented: `invalidRightsOnDiskQuarantineManifestAndDeactivatesJob`
     (hand-tampered on-disk manifest → Permanent(manifest_missing), job deactivated, bytes
     preserved in quarantine, never READY). No production change.
   - F2 `BundleStore.installBlob` fix implemented (~15 lines incl. KDoc quarantine line).
5. **EXECUTED** `:app:testDebugUnitTest :app:assembleDebugAndroidTest` after F1/F2 changes →
   **BUILD SUCCESSFUL** (2m10s); fresh JVM counts **174 / 0** (reports regenerated 19:23).
6. **PMP investigated** (see section below) — actual state confirmed, nothing reinitialized.
7. Emulator setup: **EXECUTED** sdkmanager install of `emulator` (37.2.12.0) +
   `system-images;android-36;google_apis;x86_64` (r07); AVD `stage5-api36` created.
   **AVD home is redirected**: `/home/stokonn/.config/.android/avd` — the emulator binary
   needs `ANDROID_AVD_HOME=/home/stokonn/.config/.android/avd` or it fails with
   "Unknown AVD name". Booted **emulator-5580** headless (API 36, Android 16,
   `ro.kernel.qemu=1` — verified emulator, explicit serial, no physical phone ever used).
8. **EXECUTED** `:app:assembleDebug` → BUILD SUCCESSFUL (5m44s) after catching that the
   existing `app-debug.apk` was stale (Oct 16, pre-Stage-5). Fresh `app-debug.apk`
   (22,508,542 bytes, Oct 20 19:45) exists in `app/build/outputs/apk/debug/`.
9. Installed to emulator-5580 (both `Success`) — but **before** the staleness catch, so the
   *installed* app APK was the stale one; **no instrumentation test was ever executed**.
   Emulator stopped safely on battery warning.

## PMP investigation result (question #6 — answered, NOT reinitialized)

- **PMP = Project Memory Protocol** — owner's public repo `stokonn/project-memory-protocol`
  (created 2026-10-19): "Local-first, agent-agnostic development memory, checkpoints and
  handoffs for Git projects" (Python CLI, `pmp init/status/resume/handoff`, XDG state dirs,
  `PMP_STATE_HOME` override).
- Evidence the earlier initialization succeeded: GitHub Issue #31 checkpoint comment (2026-10-20
  13:46Z) says "**PMP state local/private**"; the `docs/ai/*` HANDOFF/STATE/VERIFICATION/
  DECISIONS structure mirrors PMP's handoff style.
- **Why the previous session found nothing: no durable local trace exists anywhere now.**
  Verified absent: `pmp` CLI (PATH, `~/.local/bin`, pipx, `python3 -m pip`, uv); PMP state
  (`~/.local/state/pmp`, `~/.config/pmp`, `~/.local/share/pmp`, `~/.pmp`, `$PMP_STATE_HOME`
  unset); `.pmp/`/`.pmp.yml` in the working tree **or in any commit on any branch**; a local
  clone of the repo under `~/Projects` or `/tmp`. Conclusion: the earlier init evidently lived
  in an ephemeral location (e.g. /tmp or a since-removed worktree) or was deleted — PMP state
  is by design local/private and never committed, so its loss leaves no git trace.
- **Not reinitialized** (owner instruction: confirm state first). Re-init is an owner decision;
  `docs/ai/*` serves Stage 5 handoff in the meantime.

## Not executed / not claimed

- **All `Stage5*Test` instrumentation (tasks 5.10/5.11) — still zero tests executed; now
  blocked by the desktop emulator bug (see blocker section). The fresh APKs were built
  but not installed anywhere.**
- `lintDebug` is part of the running host gate (task 5.12) — see VERIFICATION.md.
- Emulator acceptance matrix (Wi-Fi→mobile, Data Saver, consent revocation mid-stream, real
  process death, low disk, seeded in-place upgrade from trusted 0.10.1, cost measurements).
- No screenshots, no physical phone (forbidden), no merge/tag/release (forbidden).

## Environment

Desktop (third session): Arch-notebook environment superseded — Fedora host, kernel
`7.2.10-200.fc44.x86_64`, Java 25 (Gradle 9.3.1 runs fine), 15 GiB RAM, SDK at
`~/Android/Sdk` (android-36 + android-37.0/37.2 images, build-tools 36.0.0, cmdline-tools
12.0, emulator 37.2.12 + side-by-side 37.3.4), AVDs in `~/.android/avd`
(`stage5-api36` google_apis API 36, `Medium_Phone_API_37.0` playstore). `/tmp` is a
small tmpfs (7.8 GiB) — do not unzip large archives there. All builds
`--no-daemon --max-workers=1`. All ADB calls require explicit `-s emulator-NNNN`.

## Prohibitions (still active)

No physical phone / implicit ADB device; explicit `emulator-NNNN` serial only. No push, tag,
merge, release or APK publication without separate authorization. Do not mark Stage 5 tasks
5.10–5.13 complete; do not start Stage 6. Never weaken test assertions; never fabricate results.

## Addendum — Stage 6 C6 contract spike (2026-10-22, isolated branch, local-only)

Owner-authorized local verification prototype executed on branch
`spike/goldberg-0.11-stage-6-contract` (from planning HEAD `c22adb1`; **planning
branch untouched, spike never merged**): `spikes/contract-c6/` — disposable
in-process `/v1` contract simulator (synthetic identities, fake sessions, fake
clock, per-run `node:sqlite`, zero npm deps, static scan PASS: no network
surface). Executed: **79 tests / 79 pass / 0 fail** (Node v22.23.1, run
2026-10-22T06:40Z; first run 69/79 → 10 root-caused failures fixed and
preserved in
[results/SUMMARY.md §1](https://github.com/stokonn/Goldberg/blob/d2a95d4b83c634aacb4f7f74432f2bd8980cf218/spikes/contract-c6/results/SUMMARY.md)
failure log — stable link at commit `d2a95d4`, the spike code stays isolated
on its branch). Covers THREAT-MODEL [L] rows T5–T18/T22
incl. 10-trial multi-process approve race (exactly one commit/trial), fault-
injection rollback, kill switch, quotas, SSRF static guard, audit chain.
**Not executed (precise blockers):** C2 §12 production runtime (provisioning
gate 6.0d: D5b unresolved, C4 open, ORD-A steps 4–6), all [I]-venue rows, real
Postgres isolation, HTTP/token layer (C2 S1–S19 reused), CPU (C2 measured),
tasks 6.1–6.18. **No gates changed: 6.0f UNCHECKED, 6.1–6.18 unchecked, no
accounts/provisioning/merge/tag/release, Stage 7 untouched, 0 CZK/month.**

## Addendum — Stage 6 Cloudflare MCP readiness verification CHECKPOINT #1 (2026-10-22, read-only)

Under an explicit owner instruction, the official **Cloudflare MCP server**
(OAuth, **read-only** permissions) was verified and used for a **read-only
inventory** of the owner-provisioned Cloudflare account. **No cloud resource
was created, modified or deleted; no OAuth permission, billing or account
setting was touched; no credential was accessed or recorded.** All calls were
GET. Non-identifying facts recorded (no email, account ID or credential in any
repository file):

- **MCP connection VERIFIED:** `GET /user` → HTTP 200 (success).
  `GET /user/tokens/verify` → 401 "Invalid API Token" is **expected** — that
  endpoint validates API tokens, not OAuth bearer sessions; not a defect.
- **Read-only scope evidence:** Workers R2 *read* scope present;
  **billing/subscription endpoints refused (403, no OAuth scope)** — billing
  state is **not inspectable** through this connection, and **inaccessible
  billing is explicitly NOT proof of zero costs** (the 0 CZK posture is
  enforced by the C4/ORD-A gates and the owner's accounting, never inferred
  from a tool limitation).
- **Account inventory (empty):** 1 standard account (created 2026-10-22, not
  suspended, no pro zones); **user-level 2FA ENABLED** (account-level
  2FA-enforcement flag off — relevant to D8c when actionable); **0 zones**;
  **0 Workers scripts**; **workers.dev subdomain NOT yet created** (404 /
  code 10007 — auto-created only on the first Workers & Pages dashboard
  visit; not performed); **0 D1 databases; 0 KV namespaces; R2 not enabled**
  (403 / code 10042 — not needed by the approved architecture).
- **Architecture comparison (C1):** a Workers Free verification Worker can be
  hosted when separately authorized; **nothing exists yet**; the
  no-custom-domain constraint is trivially satisfied (0 zones); **Supabase
  remains entirely unverified/unprovisioned from this side.**
- **Standing preserved:** the existing account does **NOT** resolve Q1/D5b —
  account eligibility, contractual capacity, authority-to-bind and the GDPR
  controller role all remain for professional review. **Q1/D5b UNRESOLVED
  and blocking for 6.0d; C4 OPEN; C2 §12 OPEN; 6.0f UNCHECKED; 6.0d/6.0e NOT
  AUTHORIZED; tasks 6.1–6.18 all unchecked; ORD-A sequencing-only; Stage 7
  untouched; Stage 5 remains COMPLETE; 0 CZK/month unchanged.**
  **Next steps:** (1) owner routes `Q1-PROFESSIONAL-REVIEW-BRIEF.md` to a
  qualified professional to resolve D5b; (2) only then, under a separate
  phase-scoped authorization (ORD-A steps 4–6 / CR-1…CR-5): day-of C4 →
  minimal verification resources → CR-6 measurements → CR-7 cleanup →
  owner acceptance/rejection of 6.0f.

## Addendum — Owner preference: Q1-D preferred operator model (2026-10-22, docs only)

The owner recorded **Q1-D (operation under their own name, as a legally
capable natural person acting in their own name) as the PREFERRED operator
model**, subject to contractual eligibility determination and professional
verification. **A directional preference only — NOT a selection, NOT an
approval, NOT a D5b resolution.** D5b stays **legally UNRESOLVED and blocking
for 6.0d** until the professional review (Q4.1–Q4.13 / C-1…C-6) returns and
the owner records the outcome; textual contract compatibility ≠ lawfulness;
no account may be created; ORD-A stays sequencing-only; Q1-C remains the
operative posture (nothing provisioned); no personal data requested, recorded
or implied. Synchronized: `stage-6/Q1-OPERATOR-READINESS.md` (PREFERENCE
ADDENDUM), `OPEN-DECISIONS.md` (D5b row), `CLOUD-READINESS-PLAN.md` §4.1,
stage-6 `README.md`, `CHECKLIST.md` (6.0b + progress log),
`STAGE-CHECKLIST.md`, `docs/ai/HANDOFF.md`. **6.0f UNCHECKED; 6.0d/6.0e NOT
AUTHORIZED; 6.1–6.18 unchecked; C4 OPEN; C2 §12 OPEN; Stage 7 untouched;
Stage 5 remains COMPLETE; 0 CZK/month.**

## Addendum — C6-PG spike: contract tests verified on real local PostgreSQL (2026-10-22)

Branch `spike/goldberg-0.11-stage-6-contract-pg` (**never merged**; `f7bef8e`
code + `e37ed00` evidence) ran the SAME C6 contract suites against a real,
isolated, LOCAL **PostgreSQL 17.11** (rootless podman, `127.0.0.1:54329`
loopback-only, trust auth on loopback, default isolation **`read committed`**,
synthetic data only, per-test throwaway schemas; container removed after
evidence capture). **Results: PostgreSQL 86/86 pass / 0 fail (v1–v10);
SQLite post-port 79/79 / 0 fail + 7 v10-only skips (no regression vs the
original 79/79); static scan PASS.** v9 multi-process race: SQLite 10/10
`[201,409]` vs PostgreSQL **10/10 `[201,500]`/`[500,201]`** — the T10
exactly-one-decision invariant does NOT hold under READ COMMITTED.
Findings F1–F5 (details:
[SUMMARY-PG.md](https://github.com/stokonn/Goldberg/blob/e37ed00b9205ce22e9e8ec494651e900708ee1e3/spikes/contract-c6/results/SUMMARY-PG.md)):
F1 lost-update/double-approve under READ COMMITTED with both fixes
(`SELECT … FOR UPDATE`; SERIALIZABLE + explicit 40001→409 mapping)
demonstrated as probes only; F2 rollback parity + **no FK constraints in the
C6 DDL** (task 6.1 gap); F3 idempotency parity + concurrent duplicate key →
23505 must serve replay; F4 audit chain parity + audit-seq race (seq must be
serialized); F5 SQLite→PG differences (`BEGIN IMMEDIATE` absent, 42702 column
qualification fixed portably, `rowid`, `?`, **int8→string driver default**).
**Local PostgreSQL ≠ Supabase: the Supabase-hosted proof stays BLOCKED
(D5b/Q1 + C4 + ORD-A steps 4–6). F1–F5 are design inputs for task 6.1, not
authorization. 6.0f UNCHECKED; 6.0d/6.0e NOT AUTHORIZED; 6.1–6.18
unchecked; Q1/D5b UNRESOLVED (Q1-D owner preference unchanged, preference
only); C4 OPEN; C2 §12 OPEN; ORD-A sequencing-only; Stage 7 untouched;
Stage 5 remains COMPLETE; 0 CZK/month.**

**C4 preliminary verification + documentation reconciliation (2026-10-22,
documentation-only; C4 stays OPEN):** the official Cloudflare/Supabase pricing,
terms, DPA, region and backup sources were re-fetched (public documents only —
no console login, no billing/API call, no resource, no payment method touched)
and reconciled with the 2026-10-21 snapshot; results recorded as **§F** of
`stage-6/C4-PREPROVISIONING-CHECKLIST.md` (§A–§C statuses untouched).
**Recorded: both provider accounts now exist** (owner-side; Cloudflare
corroborated by read-only MCP checkpoint #1 comment `6343122203`; Supabase
from the owner's statement only, never agent-verified); **Cloudflare MCP
read-only auth verified; Workers Free remains available; Supabase Free remains
available with stated limits; `eu-central-1` remains a documented specific
region; automatic backups NOT included on Supabase Free** (official free-tier
`db dump` self-export guidance confirmed); **Supabase Free quotas can result
in service restrictions** (pause / read-only / 402 after grace period);
**billing and payment-method status NOT verified; subprocessor and transfer
reviews still incomplete.** Doc-date diff: all 2026-10-21 stamps unchanged
except CF Website Terms date + Supabase terms version badge (not re-renderable
— honest gaps, remain ⚠ day-of); **CF observability announcement (2026-12-01)
unchanged**; its target page (upd. 2026-10-15, new source) shows **Free =
0.5 GB/day ingestion + 7-day retention, no paid overage on Free** —
preliminary 0-CZK read, re-verify day-of and for any access ≥ 2026-12-01.
**Q1-D = owner's preferred structure subject to eligibility verification
(preference only).** **Gates unchanged: C4 OPEN (§D steps 1–9 unchecked);
6.0f UNCHECKED; 6.1–6.18 unchecked; Q1/D5b UNRESOLVED and blocking; C2 §12
OPEN; 6.0d/6.0e NOT AUTHORIZED; ORD-A sequencing-only; 0 CZK/month; no
provisioning/account/billing/OAuth/deployment change; Stage 7 untouched;
Stage 5 remains COMPLETE.**

**C6-PG concurrency hardening (2026-10-22, experimental spike comparison —
never merged):** both candidate F1/F3/F4 fixes were implemented in the
isolated spike branch `spike/goldberg-0.11-stage-6-contract-pg` (commits
`d8e82d5` code + `c21e550` evidence; faithful sources and the original
`results/pg/` deviation evidence byte-untouched) and compared on local
PostgreSQL 17.11 (`read committed`): **`for-update`** (submission-row lock +
`audit_head` append mutex) vs **`serializable`** (SSI + bounded retry +
explicit 40001/40P01/23505 handling, exhaustion → 409 never 500), both with
the shared F3 store hardening (concurrent idempotency 23505 → serve stored
replay; 422 on mismatch). **Executed:** v11 7/7 (37 tests); the repeated
20-trial multi-process race restored `[201,409]` in **20/20 trials per
strategy** (vs the faithful server's 10/10 deviations in v9); **for-update
needed 0 SQLSTATE retries everywhere** (incl. a 20-decision F4 scale run with
consecutive audit seqs and a coherent chain); **serializable needed 21×
`40001`** in the race and **218× at 20-way concurrency** (maxAttempts=64),
0 exhausted; deterministic F3 store race verified. Full executor: SQLite
regression 93 tests / 79 pass / **0 fail** / 14 honest PG-only skips;
PostgreSQL venue **123/123 pass, 0 fail**; static scan PASS (23 files);
container removed after capture. **Conclusion (design input for 6.1):
`SELECT … FOR UPDATE` is the recommended pattern** (deterministic, zero
retries); serializable works but needs a generous bounded retry and
40001→409 mapping; concurrent same-key decision duplicates answer `[201,409]`
while sequential duplicates replay. Evidence at `c21e550`
(`results/pg-hardening/SUMMARY-HARDENING.md`). **Gates unchanged: 6.0f
UNCHECKED; 6.1–6.18 unchecked; Q1/D5b UNRESOLVED; C4 OPEN; C2 §12 OPEN;
6.0d/6.0e NOT AUTHORIZED; ORD-A sequencing-only; 0 CZK/month; not a Supabase
run; no merge/tag/release; Stage 7 untouched.**

**C4 owner-side console observations (2026-10-22, attributed owner facts,
never agent-verified — recorded as §F.5 / N-11–N-13 of
`stage-6/C4-PREPROVISIONING-CHECKLIST.md`):** **Cloudflare Workers Free is
active with no payment method attached** (billing non-verification unchanged —
MCP has no billing scope); a **Supabase "Personal Free" organization exists
with zero projects** (no project created; 2-project Free cap not consumed; an
organization existing is not billing evidence); **Frankfurt (`eu-central-1`)
is selectable in the Supabase project-creation form** (informs SB-P7/§D step 6
but does not close it). **No screenshots, console exports or account
identifiers are stored in GitHub.** Supabase billing/payment status remains
unverified; project creation remains unperformed and unauthorized. **Gates
unchanged: C4 OPEN (§D steps 1–9 unchecked); 6.0f UNCHECKED; 6.1–6.18
unchecked; Q1/D5b UNRESOLVED and blocking; C2 §12 OPEN; 6.0d/6.0e NOT
AUTHORIZED; ORD-A sequencing-only; 0 CZK/month; no agent console login, no
billing/API call, no resource created or modified; Stage 7 untouched; Stage 5
remains COMPLETE.**
