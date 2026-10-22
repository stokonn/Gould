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

# Goldberg 0.11 — Stage 5 HANDOFF (fresh-session resume instructions)

> **SUPERSESSION NOTE (2026-10-21, Stage 6 planning branch):** the Stage 5 content
> below is a frozen historical checkpoint from before Stage 5 completion. **Stage 5 is
> now COMPLETE and owner-approved (2026-10-21)** — the resume steps below are obsolete
> and must not be executed; the authoritative Stage 5 record lives on
> `feature/goldberg-0.11-stage-5` (completion commit `736f6d3`, head `32cf5d9`) and the
> canonical `STAGE-CHECKLIST.md`. See the additive note at the end of this file.

> Created 2026-10-20; updated at the end of the third resume session (desktop machine).
> Stage 5 is **in progress, incomplete**. The branch HEAD is a checkpoint commit on top of
> `54f2b45`, pushed to origin as an owner-authorized IN-PROGRESS checkpoint. Nothing was
> merged, tagged or published. **Tasks 5.10/5.11 (instrumentation + emulator acceptance)
> are the only Stage 5 tasks still open** — they are blocked on this desktop by a known
> upstream emulator bug and must be executed on the **notebook's** working API 36 emulator.

## Read order (in this order, before touching code)

1. `AGENTS.md` — permanent policy (data safety, no physical phone, publication gates).
2. `docs/ai/STATE.md` — current state, executed evidence, emulator blocker record.
3. `docs/ai/VERIFICATION.md` — executed vs. not-executed ledger.
4. `docs/ai/DECISIONS.md` — owner decisions, incl. approved F1/F2 resolutions.
5. `docs/planning/goldberg-0.11/STAGE-CHECKLIST.md` — Stage 5 section (authoritative list).
6. GitHub Issue #31 — latest checkpoint comment (do not duplicate issues).

## What the checkpoint contains (all prior work preserved)

- Everything from the previous checkpoint (`c61cf12`): F1/F2 fixes, compile-green
  `Stage5BundleJobRunnerTest`/`Stage5BundleWorkerTest`, recovery docs.
- This session's additions: `docs/ai/*` + STAGE-CHECKLIST updated with the **desktop
  emulator blocker** (Google Issue #566807521) and the emulator-independent host
  verification (Gradle host gate + all five `scripts/check_*.py`).
- `.pmp/`, `.pmp.yml`, `RELEASE_NOTES.md` and
  `docs/planning/goldberg-0.11/stage-4/checkpoint-archive/` are private/unrelated —
  never commit them.

## Verified state at this checkpoint (executed — see VERIFICATION.md for exact rows)

- `:app:assembleDebug :app:assembleDebugAndroidTest` **BUILD SUCCESSFUL** (desktop, Java 25
  host, 1m08s, 0 errors). Fresh APKs:
  `app-debug.apk` 32,388,569 B sha256 `44222c59b89ed63041d1e105f074857f5ca3c2f93efef4a9ed0eea115dd0a157`;
  `app-debug-androidTest.apk` 1,331,040 B sha256
  `6030e528474334ae31f0514c0f818aeeb68dcf3f9b2ac35e453762d1e5ba8f2d` (Oct 20 21:13).
- Host gate (`clean assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug`)
  + `scripts/check_*.py` — results recorded in VERIFICATION.md (all executed on the
  desktop this session).
- **Zero instrumentation tests executed anywhere** (emulator blocker below).

## Emulator blocker on the desktop (why 5.10/5.11 were not done here)

Every headless emulator boot on the desktop (Fedora kernel `7.2.10-200.fc44`) dies with
`Killed by SIGSEGV` after `WARNING: cannnot unmap ptr … protected range …`, on both
AVDs (`Medium_Phone_API_37.0` playstore and fresh `stage5-api36` google_apis API 36) and
both emulator versions (37.2.12 SDK + 37.3.4 canary side-by-side), across
swiftshader/auto(lavapipe) GPU modes. This is upstream bug **Google Issue Tracker
#566807521**; unfixed as of 37.3.4. Details in `docs/ai/STATE.md`.

## Exact resume steps for tasks 5.10/5.11 — run on the NOTEBOOK

The notebook has the previously working toolchain (emulator 37.2.12 + API 36 google_apis
x86_64 image + AVD `stage5-api36`, AVD home redirected). On the notebook:

1. Confirm the tree is the pushed Stage 5 branch at the checkpoint commit:
   `git status` (only `.pmp/`, `.pmp.yml`, `RELEASE_NOTES.md`,
   `stage-4/checkpoint-archive/` untracked), `git log --oneline -2`.
2. Rebuild **fresh** APKs there (the notebook's build outputs are from an older source
   state — do NOT reuse them):
   `./gradlew --no-daemon --max-workers=1 :app:assembleDebug :app:assembleDebugAndroidTest`
   and verify timestamps/hashes before installing (the stale-APK lesson from the battery-
   emergency session).
3. Boot the notebook emulator (AVD home redirect is mandatory there):
   `ANDROID_AVD_HOME=/home/stokonn/.config/.android/avd "$ANDROID_HOME/emulator/emulator"
   -avd stage5-api36 -port 5580 -no-window -no-audio -no-boot-anim -gpu swiftshader_indirect
   -no-snapshot -memory 2048 &`
   Keep the process alive across tool calls (managed/background shell, not a bare nohup —
   the desktop session lost nohup'd processes). Wait for
   `adb -s emulator-5580 shell getprop sys.boot_completed` = 1 and verify
   `ro.kernel.qemu`=1. **Every ADB call must use `-s emulator-5580`; never a physical phone.**
4. Install the fresh APKs explicitly:
   `adb -s emulator-5580 install -r app/build/outputs/apk/debug/app-debug.apk`
   `adb -s emulator-5580 install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk`
5. **Task 5.10** — run instrumentation explicitly, per Stage5 class with real counts:
   - `adb -s emulator-5580 shell am instrument -w -e class com.opus.piano.Stage5BundleStoreTest com.opus.piano.test/androidx.test.runner.AndroidJUnitRunner`
   - `adb -s emulator-5580 shell am instrument -w -e class com.opus.piano.Stage5BundleJobRunnerTest com.opus.piano.test/androidx.test.runner.AndroidJUnitRunner`
   - `adb -s emulator-5580 shell am instrument -w -e class com.opus.piano.Stage5BundleWorkerTest com.opus.piano.test/androidx.test.runner.AndroidJUnitRunner`
   Record exact OK/Failure counts. Investigate any failure against the F1/F2 contracts
   (DECISIONS.md); never weaken assertions.
6. **Task 5.11** — emulator acceptance matrix on the same emulator: Wi-Fi→mobile
   transition, Data Saver, consent revocation mid-stream, real process death at
   boundaries, low disk, seeded in-place upgrade from trusted 0.10.1, disk/network cost
   measurements. Stage 4 helpers: `scripts/emulator_ui.py`,
   `scripts/verify_emulator_upgrade.py`, `scripts/measure-startup.sh`.
7. **Task 5.13** — update `docs/ai/*` + STAGE-CHECKLIST with executed evidence, commit,
   push to the Stage 5 branch, sync Issue #31 + Project. **Do not mark 5.10–5.13 complete
   without executed evidence. Do not start Stage 6; no tag/merge/release.**
8. PMP: local `.pmp/`/`.pmp.yml` on the desktop were **preserved as-is, never
   reinitialized**. Leave them private.

Alternative 1 (CI, current primary path): `.github/workflows/stage5-emulator.yml`
(`workflow_dispatch` only, KVM-accelerated `ubuntu-24.04`, SHA-pinned, revised this
session — signing-resolution, state-asserted network/Data Saver checks, DB integrity
after process death, source-SHA binding) runs the full connected suite for 5.10 and the
CI-executable 5.11 subset. It is committed on this branch (`1ef15bf` + CI-activation
revision) and proposed for `main` via **draft PR #32 (workflow-only, not merged)**.
After the owner approves: merge PR #32, then dispatch the workflow **against
`feature/goldberg-0.11-stage-5`** from the Actions tab (the workflow depends on this
branch's helper scripts; `main`'s copy is older). Consent revocation mid-stream, low
disk, screenshot inspection and real bundle-transfer costs remain notebook/manual work
either way.

Alternative 2: if an upstream emulator fix for #566807521 ships (watch the issue), the
desktop's `stage5-api36` AVD + emulator 37.3.4 side-by-side install are already prepared
(`~/Android/Sdk/emulator-37.3.4/emulator -avd stage5-api36 -port 5580 …`).

## Hard prohibitions (unchanged)

No physical phone; no implicit device selection; no merge/tag/release/APK publication
without separate authorization; no weakened test assertions; no fabricated results; no
Stage 5 "complete" claim; no Stage 6 start. Never commit `.pmp/`, `.pmp.yml`,
`RELEASE_NOTES.md` or the stage-4 checkpoint archive.

## Environment

Desktop: Fedora, kernel `7.2.10-200.fc44.x86_64`, Java 25, 15 GiB RAM, Android SDK at
`~/Android/Sdk` (android-36 platform + build-tools 36.0.0 + cmdline-tools 12.0 +
emulator 37.2.12 and side-by-side 37.3.4), AVDs in `~/.android/avd`. Gradle 9.3.1,
`org.gradle.jvmargs=-Xmx3g`; always `--no-daemon --max-workers=1`. `/tmp` is a small
tmpfs — keep large archives in `~/.cache`.
Notebook (for 5.10/5.11): Arch Linux, Java 17, emulator 37.2.12, AVD `stage5-api36` with
`ANDROID_AVD_HOME=/home/stokonn/.config/.android/avd`.

## Additive note — Stage 6 planning branch (2026-10-21, separate agent, docs only)

Added on the isolated `docs/goldberg-0.11-stage-6-planning` branch only; the Stage 5
branch/tree was not modified by this work. Stage 6 (community backend) **planning
documentation** is prepared at `docs/planning/goldberg-0.11/stage-6/` (10 documents +
additive STAGE-CHECKLIST/STAGES/DECISIONS/HANDOFF updates), committed and pushed on
that branch.

**Update (2026-10-21, same day — owner decision review completed):** the owner settled
the Stage 6 design decisions. Supabase Free + strict 0 CZK + graceful denial/fail-closed
(D4a/D4b), Frankfurt region (D5c), general-audience moderation with minimal data
(D5f/D5g), in-app Android Admin (D8a) and console bootstrap with MFA (D8c) are
**approved**; the Web Admin BFF (D8b) is **approved conditionally** on proving its
zero-cost fit before implementation; D5a is a provisional preference with **no
supplied contact** and D5b/D5d details plus D5e/D5f legal reviews are **deferred to
pre-launch gates**. Full outcomes: `docs/planning/goldberg-0.11/stage-6/OPEN-DECISIONS.md`
and `docs/ai/DECISIONS.md`.

**Stage 5 reconciliation:** this branch was cut before Stage 5 completion, so its
copies of STAGE-CHECKLIST/STATE/HANDOFF carried stale "Stage 5 in progress" text. The
canonical checklist on this branch now records **Stage 5 ✅ complete, owner-approved
2026-10-21** (CI runs 39557624554 + 39576173002, 12/12 screenshots; real-CDN transfer
measurements preserved as a future integration dependency) and points to the Stage 5
branch as the authoritative record. **Stage 5 was not reopened, rerun or modified** —
no Stage 5 code or evidence was touched.

**Stage 6 implementation is still NOT started and remains blocked** on a separate
implementation instruction, the D8b zero-cost proof (new task 6.0f), the remaining D5
pre-launch gates, and separately authorized provisioning (6.0d) / OAuth console setup
(6.0e). Nothing above in the Stage 5 handoff changes.

**Addendum (2026-10-21, Stage 6 C2/C3 session — docs/planning only):** conditions
C1–C3 of the 6.0f gate now read: **C1 approved** (architecture level);
**C2 executed → CONDITIONAL PASS** (isolated local `workerd` spike on
`spike/goldberg-0.11-c2-bff`, final commit `7bc6976`, spike code commit
`d2ce77b`: 19/19 security, P1–P7 executed, kernel CPU **2.03 ms/invocation**;
per-invocation production CPU **unverified**; §12 production-runtime
verification **owner-gated**; spike code remains isolated/unmerged);
**C3 documentary review complete**
(`docs/planning/goldberg-0.11/stage-6/C3-EDGE-PRIVACY-REVIEW.md`: conditional
privacy viability at 0 CZK, no compliance claim, legal/operator questions open —
controller/operator identity incl. the **minor-owner legal-capacity question is
now blocking for 6.0d provisioning**). **Still open: C2 §12 production
verification and C4 (terms re-verification). Task 6.0f stays UNCHECKED.**
Resume order for a fresh Stage 6 session: `AGENTS.md` →
`docs/planning/goldberg-0.11/stage-6/CHECKLIST.md` (progress log) →
`Q1-OPERATOR-READINESS.md` + `C4-PREPROVISIONING-CHECKLIST.md` →
`C3-EDGE-PRIVACY-REVIEW.md` §11 (pre-provisioning checklist) → GitHub Issue #31
latest checkpoint.

**Addendum (2026-10-21, Stage 6 Q1/C4 session — docs/planning only):** the two
remaining documentation conditions are now *prepared*, neither *closed*:
**Q1 research** (`stage-6/Q1-OPERATOR-READINESS.md`) records verified contractual
facts (no published minimum account age on either provider's current terms;
authority-to-bind warranties; CF SSSA §2.2.1(a); Czech Civil Code §§30–33; Google
age rule), the professional-review question set, and **operator Options A/B/C with
no option selected** — **D5b remains legally unresolved and blocking for 6.0d
provisioning**; no identity/contact data was requested or recorded, no legal
conclusion was drawn. **C4 pre-provisioning checklist**
(`stage-6/C4-PREPROVISIONING-CHECKLIST.md`) pins the current source versions with
access dates (incl. the dated Workers Logs → Observability pricing change of
2026-12-01 and the Supabase Free "no automatic backups" fact) and provides the
§D runbook; **the final C4 verification must still run immediately before any
separately authorized provisioning — C4 stays OPEN, 6.0f stays UNCHECKED.**
Retrieval failures recorded honestly: EUR-Lex GDPR text (0 bytes ×2) and the
Czech MoJ official English Civil Code PDF (TLS error). **Next step for a fresh
agent:** read the CHECKLIST progress log, then await the owner's decisions
(professional review of the Q1 §4 questions → choose Option A/B/C) — do not
provision, do not implement, do not touch 6.0e.

**Addendum (2026-10-21, Stage 6 cloud-readiness session — docs/planning only):**
the owner chose the **cloud architecture as the intended deployment path**
(Cloudflare Workers Free same-origin static + `/v1` BFF; Supabase Free
PostgreSQL/Auth in `eu-central-1` Frankfurt; permanent 0 CZK; no paid plans,
chargeable trials, upgrades or unapproved billing) **instead of the temporary
local-only Option C** — Option C is now only the current gated posture (nothing
provisioned, nothing may be until gates pass), not the intended end state.
Instruction authorized **documentation and verification planning only**: no
accounts, credentials, deployments, endpoints or code. The earlier "owner is a
minor" premise is **reclassified as an unverified assumption, not a verified
current fact**; no age/identity/family detail may be requested or recorded, and
**D5b stays legally unresolved and blocking for 6.0d** (account registration ≠
compliance; eligibility, legal capacity, authority-to-bind and the GDPR
controller role require professional verification — Q1 §4 unchanged). New
deliverable: `stage-6/CLOUD-READINESS-PLAN.md` — (a) reconciled assumptions;
(b) the **6.0f ↔ provisioning gate-ordering** conflict analyzed with an
explicit minimal sequence (documentary verification → contractual readiness →
owner-authorized *limited private verification* provisioning → day-of C4 →
isolated synthetic-data production-runtime C2 verification → 6.0f accept/
reject → separate implementation authorization → later launch gates) and
alternatives **ORD-A/B/C left pending as an owner decision**; (c) the exact
future private-cloud verification runbook **CR-0…CR-8** (account readiness,
MFA/secrets, no-billing verification, Frankfurt region selection, minimal
private test infra, CPU-per-invocation/cold-start/Supabase-latency/quota/
fail-closed/logging measurements, mandatory recorded rollback+cleanup —
synthetic data only); (d) Free-tier usage estimates (0 CZK, no billing method)
and binding stop/rollback conditions. **Next step for a fresh agent:** read
`stage-6/CLOUD-READINESS-PLAN.md` §9 (pending owner decisions) and the CHECKLIST
progress log, then **await the owner's gate-ordering decision (ORD-A/B/C) and
the D5b professional-review routing — do not provision, do not create accounts,
do not implement, do not touch 6.0e.** 6.0f stays unchecked; Stage 6
implementation stays NOT STARTED; Stage 5 remains COMPLETE.

**Addendum (2026-10-21, Stage 6 ORD-A approval session — docs/planning only):**
the owner **APPROVED ORD-A — Scoped Private Cloud Verification Provisioning**
as the sequencing strategy for closing task 6.0f; **ORD-B and ORD-C are NOT
selected**. This is a **sequencing-only, documentation-only** approval: it
authorizes recording the decision, reconciling gates and preparing
prerequisites — it is **NOT** acceptance of 6.0f, **NOT** a D5b resolution, and
**NOT** authorization to create/configure Cloudflare or Supabase accounts,
provision resources, deploy even a test Worker, configure OAuth, touch real
API credentials, implement backend/Android code, modify Stage 5/Stage 7 code,
merge/rebase, change `main`, tag/release, publish an APK, collect personal
data, activate public endpoints or spend money. Recorded in
`stage-6/CLOUD-READINESS-PLAN.md`: the approved 11-step sequence (§3), the
reconciled gate table (§4.2 — C1 approved · C2 conditional pass · C2 §12 open
· C3 complete · C4 open · **Q1/D5b open** · ORD-A approved (sequencing only) ·
**6.0f UNCHECKED** · 6.0d/6.0e NOT AUTHORIZED · Stage 6 implementation NOT
STARTED), the **ORD-A readiness checklist** for the future owner decision (§5,
aligned with CR-0…CR-8), and the concise owner-facing list of the Q1 §4
professional-review questions (§9.1 — identity-free, to be routed **outside
GitHub**; no private replies requested in the issue). ORD-A (gate ordering) is
explicitly distinct from **Q1 Options A/B/C (operator arrangements), none of
which is selected**; Q1/D5b remains open and blocking — no provider account
may be created while the contractual/legal prerequisites are unresolved, and
creating an account would not resolve D5b or establish compliance. The "minor
owner" premise remains an **unverified assumption**; no age, birth date,
identity, family detail, contact information or credential may be requested,
inferred, committed or published. Permanent **0 CZK/month** applies to every
future step. **Next step for a fresh agent:** read
`stage-6/CLOUD-READINESS-PLAN.md` §5 (ORD-A readiness checklist) + §9.1 (Q1
questions) and the CHECKLIST progress log, then **await the owner's D5b
professional-review outcome and the later phase-scoped CR-3 authorization —
do not provision, do not create accounts, do not implement, do not touch
6.0e.** 6.0f stays unchecked; Stage 6 implementation stays NOT STARTED;
Stage 5 remains COMPLETE.

**Addendum 4 (2026-10-21, Q1 natural-person reassessment session — docs only):**
`stage-6/Q1-OPERATOR-READINESS.md` was reassessed and rewritten after fresh
source checks (Cloudflare SSSA full text re-read; Supabase ToS re-read at its
new canonical `https://supabase.com/terms` — the old `/company/terms` path now
404s; Czech Civil Code verze 18 eff. 2026-01-01 §§15/18/30–37/420–421; Trade
Act 455/1991 §2 + §3(1)(b); GDPR Art. 2/4/8 wording from a **non-official
mirror — official EUR-Lex retrieval failed 5× total, C4 X-6 still ❌**). The
option set is now **A/B/C/D**: Q1-A (adult/guardian of record), Q1-B (legal
entity), Q1-C (deferred provisioning — still the only permissible posture),
candidate **Q1-D (legally capable natural-person operator acting in their own
name)**, compared in a §5.5 decision matrix with a §5.6 **technical/non-legal**
recommendation to evaluate D first *after* professional review. Capacity is
treated **solely as an eligibility precondition for the eventual accountable
party — never a personal fact requested, recorded or inferred**. Product
direction recorded as **PD-1: Goldberg intended to remain free of charge,
non-commercial; commercial features may be considered in a future version; no
monetization approved or implemented now.** New §1.6 trade-licensing,
§1.8 QD-1…QD-10 classification, §4 Q4.9–Q4.13, §10 phased GDPR controller
readiness. Synchronized: `stage-6/{README,CHECKLIST,OPEN-DECISIONS,
CLOUD-READINESS-PLAN,C4-PREPROVISIONING-CHECKLIST}.md` (incl. SB-P1 canonical
URL), `STAGE-CHECKLIST.md` Stage 6, `docs/ai/*`. **Nothing resolved: no option
selected; D5b UNRESOLVED and blocking for 6.0d; no GDPR compliance claimed;
professional review not waived; ORD-A stays sequencing-only; 6.0f stays
UNCHECKED; 6.0d/6.0e NOT AUTHORIZED.** **Next step for a fresh agent:** read
`stage-6/Q1-OPERATOR-READINESS.md` §§5.5–5.6 + §6 (explicit owner decisions)
and the CHECKLIST progress log, then **await the owner's decision on routing
Q4.1–Q4.13 to a professional review — no selection of A/B/C/D may be made or
implied by an agent; do not provision, do not create accounts, do not
implement.** Stage 5 remains COMPLETE.

**Addendum 5 (2026-10-22, Q1 professional-review handoff — docs only):**
`stage-6/Q1-PROFESSIONAL-REVIEW-BRIEF.md` was created as the **identity-free
routing package** for the still-not-commissioned professional review: all **13
questions Q4.1–Q4.13 preserved verbatim** from Q1 §4 with primary-source
references per question; a neutral Q1-A/B/C/D summary making explicit that
**Q1-D is a candidate for review, not an approved solution** (contract-text
compatibility ≠ lawfulness); a **verified-law vs unresolved-interpretation**
separation (§3.1–§3.3); official guidance fetched 2026-10-22 from the **Czech
Data Protection Authority (ÚOOÚ)** and the **European Commission**, quoted with
access dates; and the **C-1…C-6 confirmations a qualified professional must
return before Q1/D5b can be resolved**, plus the requested output format. The
official EUR-Lex GDPR text remains unretrievable from this environment (5
attempts, C4 X-6) and is labeled as a limitation, never papered over.
Synchronized: stage-6 README/OPEN-DECISIONS/CHECKLIST, STAGE-CHECKLIST,
docs/ai/*. **Next step for a fresh agent:** the owner routes the brief to a
qualified professional (an owner action the agent cannot take); on return, the
owner records the outcome in OPEN-DECISIONS D5b and only then selects an
option. **Until then: no option selected, D5b UNRESOLVED/blocking for 6.0d, C4
OPEN, 6.0f UNCHECKED, 6.0d/6.0e NOT AUTHORIZED, ORD-A sequencing-only, no
accounts/provisioning/OAuth/implementation, 0 CZK/month, Stage 7 untouched,
Stage 5 remains COMPLETE.**

**Addendum 6 (2026-10-22, C6 contract-behavior spike — isolated, local-only):**
an owner-authorized disposable simulator was built and executed at
`spikes/contract-c6/` on branch **`spike/goldberg-0.11-stage-6-contract`**
(created from planning HEAD `c22adb1`; push that branch only; **never merge**;
planning branch remains at its own HEAD). It verifies the `/v1` contract layer
(validation allowlist/caps, roles + default-deny + live revocation/suspension,
existence-hidden reads, moderation state machine + conflicts, idempotency
replay/mismatch/expiry, fault-injection rollback + kill switch + quotas,
SSRF static guard, hash-chained audit, multi-process approval race) with
**79/79 tests passing** (Node v22.23.1, zero npm deps, zero network; first run
69/79 with 10 root-caused fixes — failure log preserved). C2's HTTP/token/CPU
results are reused, not re-tested. Evidence — stable links at the verified
spike commit [`d2a95d4`](https://github.com/stokonn/Goldberg/tree/d2a95d4b83c634aacb4f7f74432f2bd8980cf218/spikes/contract-c6)
(the executable directory is absent from this branch):
[results/SUMMARY.md](https://github.com/stokonn/Goldberg/blob/d2a95d4b83c634aacb4f7f74432f2bd8980cf218/spikes/contract-c6/results/SUMMARY.md),
[raw TAP](https://github.com/stokonn/Goldberg/blob/d2a95d4b83c634aacb4f7f74432f2bd8980cf218/spikes/contract-c6/results/raw/tap.txt),
[README (scope/blockers)](https://github.com/stokonn/Goldberg/blob/d2a95d4b83c634aacb4f7f74432f2bd8980cf218/spikes/contract-c6/README.md).
**Next step for a fresh agent:** read the spike README §"Permissible vs
blocked" and this addendum. The docs-only port of `d2a95d4` to this planning
branch is DONE (2026-10-22, no code merge); then await
owner decisions — **do not close any of 6.1–6.18, do not treat spike results
as 6.0f acceptance, do not provision, no accounts/OAuth, D5b/Q1 unresolved, C4
open, 6.0d/6.0e not authorized, ORD-A sequencing-only, 0 CZK/month, Stage 7
untouched, Stage 5 remains COMPLETE.**

**Addendum 7 (2026-10-22, Cloudflare MCP readiness verification CHECKPOINT #1 —
read-only cloud inspection, no cloud changes):**
under an explicit owner instruction, the official **Cloudflare MCP server**
(OAuth, **read-only**) was verified and used for a read-only inventory of the
owner-provisioned Cloudflare account. **MCP connection VERIFIED** (`GET /user`
→ 200; `GET /user/tokens/verify` → 401 is expected — API-token endpoint, not
OAuth). **Read-only scope evidence:** Workers R2 *read* scope present;
**billing/subscription endpoints refused (403, no OAuth scope) — billing is
not inspectable via MCP and inaccessible billing is NOT proof of zero costs.**
**Inventory (non-identifying, nothing recorded but facts):** 1 standard
account (created 2026-10-22, user-level **2FA enabled**, account-level
enforcement flag off); **0 zones; 0 Workers scripts; workers.dev subdomain not
yet created; 0 D1; 0 KV; R2 not enabled.** No resource was created, modified
or deleted; no credential accessed. **Standing preserved: the existing
account does NOT resolve Q1/D5b; Q1/D5b UNRESOLVED/blocking for 6.0d; C4
OPEN; C2 §12 OPEN; 6.0f UNCHECKED; 6.0d/6.0e NOT AUTHORIZED; 6.1–6.18 all
unchecked; ORD-A sequencing-only; Stage 7 untouched; Stage 5 remains
COMPLETE; 0 CZK/month.** **Next step for a fresh agent:** read
`stage-6/CHECKLIST.md` progress log (2026-10-22 MCP checkpoint entry) and
this addendum. The substantive blocker is unchanged: **the owner routes
`stage-6/Q1-PROFESSIONAL-REVIEW-BRIEF.md` to a qualified professional to
resolve D5b**; only afterward, under a separate phase-scoped authorization,
may ORD-A steps 4–6 (day-of C4 → minimal verification resources → CR-6
measurements → CR-7 cleanup → 6.0f accept/reject) proceed. **Do not provision,
do not deploy, do not change OAuth permissions or billing, do not implement.**

**Addendum 8 (2026-10-22, owner preference — Q1-D, docs only):** the owner
recorded **Q1-D (operation under their own name, as a legally capable natural
person acting in their own name) as the PREFERRED operator model**, subject to
contractual eligibility determination and professional verification. **A
directional preference only — NOT a selection, NOT an approval, NOT a D5b
resolution.** D5b stays **legally UNRESOLVED and blocking for 6.0d** until the
professional review (Q4.1–Q4.13 / C-1…C-6) returns; no account may be created;
ORD-A stays sequencing-only; Q1-C remains the operative posture (nothing
provisioned); no personal data requested, recorded or implied. Synchronized:
`stage-6/Q1-OPERATOR-READINESS.md` (PREFERENCE ADDENDUM), `OPEN-DECISIONS.md`
(D5b row), `CLOUD-READINESS-PLAN.md` §4.1, stage-6 `README.md`,
`CHECKLIST.md` (6.0b + progress log), `STAGE-CHECKLIST.md`. **Next step for a
fresh agent:** unchanged — the owner routes the professional-review brief;
until the review returns, do not select an option, do not provision, do not
implement. 6.0f UNCHECKED; 6.1–6.18 unchecked; Stage 7 untouched; Stage 5
remains COMPLETE.

**Addendum 9 (2026-10-22, C6-PG spike — real local PostgreSQL verification):**
the C6 contract suites were executed against a **real, isolated, LOCAL
PostgreSQL 17.11** (rootless podman, `127.0.0.1:54329` loopback-only, trust
auth on loopback, default isolation `read committed`, synthetic data only;
container removed after evidence) on branch
`spike/goldberg-0.11-stage-6-contract-pg` (**never merged**; commits
`f7bef8e` code + `e37ed00` evidence). Same server source + same suites on
both engines; `pg` is the PG venue's only new dependency (spike branch only).
**Results: PostgreSQL 86/86 pass / 0 fail; SQLite post-port 79/79 / 0 fail
(+7 v10-only skips, no regression); scan PASS.** Headline: v9 race SQLite
10/10 `[201,409]` vs PostgreSQL 10/10 `[201,500]`/`[500,201]` — **T10 does
not hold under READ COMMITTED**; findings F1–F5 recorded (lost-update +
both fixes demonstrated as probes, missing FKs, idempotency 23505→replay,
audit-seq race, `BEGIN IMMEDIATE`/42702/`rowid`/int8-string differences) in
[results/SUMMARY-PG.md](https://github.com/stokonn/Goldberg/blob/e37ed00b9205ce22e9e8ec494651e900708ee1e3/spikes/contract-c6/results/SUMMARY-PG.md).
**Next steps for a fresh agent:** unchanged gates — F1–F5 are **design inputs
for task 6.1 (gated on 6.0f acceptance)**, not authorization to implement;
the Supabase-hosted re-proof stays BLOCKED (D5b/Q1 + C4 + ORD-A steps 4–6);
the owner still routes `Q1-PROFESSIONAL-REVIEW-BRIEF.md` for D5b. Do not
provision, do not deploy, do not merge any spike branch, do not implement.
6.0f UNCHECKED; 6.1–6.18 unchecked; Q1/D5b UNRESOLVED (Q1-D preference
unchanged); C4 OPEN; C2 §12 OPEN; Stage 7 untouched; Stage 5 remains
COMPLETE; 0 CZK/month.

**Addendum 10 (2026-10-22, C4 preliminary verification + documentation
reconciliation — C4 stays OPEN):** an owner-instructed, docs-only recheck
re-fetched the official Cloudflare and Supabase pricing, terms, DPA, region
and backup documentation and reconciled it with the 2026-10-21 C4 snapshot;
results are recorded as **§F** of
[`stage-6/C4-PREPROVISIONING-CHECKLIST.md`](../planning/goldberg-0.11/stage-6/C4-PREPROVISIONING-CHECKLIST.md)
(the §A–§C tables and statuses untouched; public document fetches only — no
console login, no billing/API call, no resource, no payment method touched).
**Recorded:** both provider accounts now exist (owner-side; Cloudflare
corroborated by read-only MCP checkpoint #1 comment `6343122203`; Supabase
existence from the owner's statement only, never agent-verified); **Cloudflare
MCP read-only authentication verified; Workers Free remains available;
Supabase Free remains available with stated limits; `eu-central-1` remains a
documented specific region; automatic backups are NOT included on Supabase
Free** (official docs guide free-tier `db dump` self-export + off-site);
**Supabase Free quotas can result in service restrictions** (pause /
read-only / 402 after grace period); **billing and payment-method status NOT
verified; subprocessor and transfer reviews still incomplete.** Doc-date
diff: all 2026-10-21 stamps unchanged except the CF Website Terms date and
Supabase terms version badge (not re-renderable — honest gaps, remain ⚠
day-of); the **CF observability-pricing announcement (2026-12-01) is
unchanged**, and its target page (upd. 2026-10-15, new source) adds Free-tier
detail: **0.5 GB/day ingestion, 7-day retention, no paid overage available on
Free** — preliminary 0-CZK read only. **Q1-D = the owner's preferred structure
subject to eligibility verification (preference only, `78fece3`).**
**Next steps for a fresh agent:** unchanged — the owner routes
`Q1-PROFESSIONAL-REVIEW-BRIEF.md` to resolve D5b; the §D day-of sequence in
the C4 checklist remains fully required before any separately authorized 6.0d
provisioning; CF-P6 must be re-verified on the day and again for any access
≥ 2026-12-01. Do not provision, do not touch billing or payment methods, do
not change OAuth configuration, and do not treat accounts existing as C4
progress. 6.0f UNCHECKED; 6.1–6.18 unchecked; Q1/D5b UNRESOLVED; C4 OPEN;
C2 §12 OPEN; 6.0d/6.0e NOT AUTHORIZED; ORD-A sequencing-only; 0 CZK/month;
Stage 7 untouched; Stage 5 remains COMPLETE.

**Addendum 11 (2026-10-22, C6-PG concurrency hardening — experimental
mitigation comparison, never merged):** both candidate F1/F3/F4 fixes were
implemented **in the isolated spike only** (same branch
`spike/goldberg-0.11-stage-6-contract-pg`, commits
[`d8e82d5`](https://github.com/stokonn/Goldberg/commit/d8e82d5807b0039e0f799ae3753558e970b74205)
code + [`c21e550`](https://github.com/stokonn/Goldberg/commit/c21e550ceb5ec7a42317a4f2fe02e6d1225a2cab)
evidence) and compared on local PostgreSQL 17.11 (`read committed`). The
faithful sources and the original `results/pg/` deviation evidence are
byte-untouched. `src/harden.mjs` = hardened decision endpoint with two
strategies over the same `runDecision` steps: **`for-update`** (row lock +
`audit_head` mutex) and **`serializable`** (SSI + bounded retry + explicit
40001/40P01/23505 handling; exhaustion → 409, never 500); both share the F3
store hardening (concurrent idempotency 23505 → serve stored replay; 422 on
mismatch). **Executed:** v11 7/7 (37 tests); repeated 20-trial multi-process
race restored `[201,409]` in **20/20 per strategy**; **for-update 0 retries
everywhere** (incl. a 20-decision F4 scale run — consecutive seqs, coherent
chain); **serializable 21× `40001`** in the race and **218× at 20-way scale**
(maxAttempts=64), 0 exhausted; deterministic F3 store race verified.
Executor: SQLite 93 tests / 79 pass / 0 fail / 14 PG-only skips; PostgreSQL
**123/123 pass, 0 fail**; static scan PASS (23 files). **Engineering
conclusion: row locking recommended** (deterministic, zero retries, loser's
409 via the ordinary guard); serializable viable with a generous bounded
retry + 40001→409 mapping; concurrent same-key decision duplicates answer
`[201,409]` while sequential ones replay — state this in the 6.1 API
contract. Full write-up:
[SUMMARY-HARDENING.md](https://github.com/stokonn/Goldberg/blob/c21e550ceb5ec7a42317a4f2fe02e6d1225a2cab/spikes/contract-c6/results/pg-hardening/SUMMARY-HARDENING.md).
**Next steps for a fresh agent:** F1–F5 + this comparison remain **design
inputs for task 6.1 (gated on 6.0f acceptance)** — NOT authorization to
implement; do not merge the spike branch; the Supabase-hosted re-proof stays
BLOCKED (D5b/Q1 + C4 + ORD-A steps 4–6). 6.0f UNCHECKED; 6.1–6.18 unchecked;
Q1/D5b UNRESOLVED; C4 OPEN; C2 §12 OPEN; Stage 7 untouched; 0 CZK/month.

**Addendum 12 (2026-10-22, C4 owner-side console observations — recorded,
never agent-verified, C4 stays OPEN):** the owner reported three first-hand
console facts, recorded as **§F.5 (N-11–N-13)** of
[`stage-6/C4-PREPROVISIONING-CHECKLIST.md`](../planning/goldberg-0.11/stage-6/C4-PREPROVISIONING-CHECKLIST.md):
**Cloudflare Workers Free active with no payment method attached** (billing
non-verification N-8 unchanged — MCP has no billing scope); a **Supabase
"Personal Free" organization with zero projects** (no project created; the
2-project Free cap not consumed; an organization existing is not billing
evidence); **Frankfurt (`eu-central-1`) selectable in the Supabase
project-creation form** (informs SB-P7/§D step 6 but does NOT close it).
**Privacy: no screenshots, console exports or account identifiers are stored
in GitHub.** Supabase billing/payment status remains unverified; project
creation remains unperformed and unauthorized. **Next steps for a fresh
agent:** unchanged — the owner routes `Q1-PROFESSIONAL-REVIEW-BRIEF.md` to
resolve D5b; the §D day-of sequence remains fully required before any
separately authorized 6.0d provisioning; do not treat these observations as
C4 progress and do not create any Supabase project. 6.0f UNCHECKED;
6.1–6.18 unchecked; Q1/D5b UNRESOLVED; C4 OPEN; C2 §12 OPEN; 6.0d/6.0e NOT
AUTHORIZED; ORD-A sequencing-only; 0 CZK/month; Stage 7 untouched.

created by stokonn with love <3
