## Stage6 Cloud — re-verification/publication (2026-10-25)

**PRIVATE STAGING NO-GO.** Owner authorized scoped review,reruns,commit/ordinary
push only. Branch `feature/goldberg-0.11-stage-6-cloud`;base006fa577.

- [x] Recover Git/Issue31/private PMP;review all changed/new files;preserve old
  result bytes and all existing worktrees. Correct cf worker nesting;strengthen
  expected-error assertions and command/source/cleanup evidence.
- [x]20 PostgreSQL checks PASS in332ae776;30 source Worker requests PASS;six
  validators/independent hashes PASS. Strict typecheck/local535-byte ESM bundle
  and30 bundled Worker requests PASS with pinned temporary toolchain.
- [ ]Staged scope/credential review,scoped commit/push,remote-SHA/exact-head CI
  verification and final Issue/Project/local/private checkpoint receipts.
- [ ]C4 billing/privacy/security/backup/owner closure;hosted ACL/runtime/schema/
  private-ingress authorization and6.0f acceptance remain open.

Purpose/scope,commands/source/result paths and honest unexecuted checks:
`docs/planning/goldberg-0.11/stage-6/READINESS-PUBLICATION-2026-10-25.md`.
Exact next-stage gates:`docs/planning/goldberg-0.11/stage-6/NEXT-PRIVATE-DEPLOYMENT-CHECKLIST.md`.
C4=8 PASS/19 PARTIAL/6 OPEN;Issue31 OPEN/Goldberg Development In Progress.
No remote schema/Worker/OAuth/public/paid/data/merge/release action.

---

## Historical October24 preparation checkpoint (preserved)

## Stage 6 private cloud deployment readiness — 2026-10-24

**Purpose/scope:** prepare Free-tier private schema/ACL/Worker/verification
candidates locally. Authorized preparation complete; **private staging NO-GO**.
Branch `feature/goldberg-0.11-stage-6-cloud`, base
`006fa577ff4faef8ee7a25daf851620feb5bc3a8`; work remains uncommitted locally.

- [x] Existing project/organization read-only metadata:healthy,Free,Frankfurt,PG17.11;
  zero public/private application tables and recorded migrations;two ACL warnings.
- [x] Assess33 C4 rows/9 day-of steps; retain8 PASS/19 PARTIAL/6 OPEN and billing gap.
- [x] Prepare narrow ACL correction preserving ensure_rls; fix PUBLIC pseudo-role
  assertion; prepare atomic checksum-guarded 001–004 private schema and inert
  route-free Worker shell/cf template/server-only secret contract.
- [x] 20 isolated synthetic PostgreSQL checks PASS,source hashes unchanged,container/
  volumes absence verified. Six shell method cases/five paths each PASS (one Node
  file wrapper reported). Schema replay/rollback,RLS with seeded row,ACL/trigger/
  unrelated grants,logical restore and restored denial verified.
- [ ] C4 closure:billing/cards/charges/add-ons/usage,terms/application,transfers/TIA/
  retention/monitoring,MFA/SSL/API configuration and operational encrypted backup.
- [ ] Separate owner/security authorization for exact hosted ACL fix,private schema,
  runtime/secrets/private ingress and synthetic hosted restore/measurements.
- [ ] Hosted Advisor postflight,connectivity/authorization/outage/CPU/backup acceptance;
  6.0f owner disposition. C2 §12 and G1–G9/G11 integration remain independently open.

**Verification/limits:** local preparation only;no hosted mutations,cloud runtime,
Worker build/typecheck,encrypted drill rerun,Android/Gradle/APK/UI/screenshot/physical
device checks. Historical Stage6 evidence/all other worktrees preserved. Stage7
owner acceptance observed in latest Issue retained;Stage8 remains pending.
No paid resources,OAuth,public routes,real data,commit/push/merge/tag/release.
Issue31 OPEN / Goldberg Development In Progress.

Detailed packet,next steps,source-bound final20-check result and remaining gates:
`docs/planning/goldberg-0.11/stage-6/PRIVATE-CLOUD-READINESS.md`.
Resume there and latest Issue checkpoint;obtain missing read-only evidence first.
No SQL application or deployment until all required prerequisites and scoped
authorizations are recorded.

Six repository validators,independent evidence/hash audit and whitespace PASS.
Issue description/checkpoint readback verified: https://github.com/stokonn/Goldberg/issues/31#issuecomment-6388528121

---

## Stage 6 HTTP → PostgreSQL integration — VERIFIED LOCALLY (2026-10-24)

**Purpose/scope:** close the separate HTTP/PG venue gap, implement O1–O8 approved
read DTOs and verify local transactions. API adapter/projections/tests/runner and
tracking only; native services/schema, Android and Stage7 worktrees unchanged.
Branch `feature/goldberg-0.11-stage-6-api-v1-local`; baseline
`944280bee28d3280479db3b4e311e69b8c49027c`. Explicit owner instruction authorizes
this local batch; older no-further-local-implementation statements are historical.

- [x] Queue safe DTOs, null unapproved labels, exact evidence counts, bounded SQL
  filters/literal NFC search and signed caller/filter/scope-bound keyset cursors.
- [x] Canonical audit DTO/filtering, fixed highwater/head, chain/filtered coverage.
- [x] Exact PostgreSQL repeatable-read statistics, Prague-day/DST boundaries,
  concurrent publication consistency and single-count approval semantics.
- [x] Real HTTP lifecycle, capabilities, moderation, dual revisions, Comparison,
  idempotent exact-byte replay, races, account isolation, role revocation, rollback,
  24h receipt/15m cursor expiry and SQL timeout recovery for all three reads.
- [x] Final source-bound run `spikes/stage6-api-v1-local/results/http-pg/pg-n4udfonz`:
  **24 HTTP→PG +240 independent PG** tests PASS (Auth50/schema34/handlers87/
  operations34/M2-19/M3-16); **API117/T22 scripted-unit38/operations unit32** PASS;
  **planning18/security-evidence17** PASS; **39 actual HTTP read DTOs** validate.
- [x] Policy, three portable backup fixtures, ACL proposal and whitespace checks
  PASS. Source/raw SHA-256 and exact disposable rootless PG17.11 container/volume
  absence verified. `verify-http-pg.py .../pg-n4udfonz` PASS.
- [x] Preserve failed/disqualified attempts and previously untracked M2/M3 evidence;
  private checkpoint `/tmp/goldberg-stage6-private-pmp/checkpoint.md` outside Git.
- [x] Implementation/evidence `ead440a27f5564b2366b50e3fb850a4f2c713fc5`
  ordinary-pushed and exact remote SHA read back; [published checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6382021802).
  Project remains In Progress; Issue31 OPEN. Original two untracked M2/M3 evidence
  directories preserved. Documentation-only receipt follows the verified source.

**Completion:** local engineering verification complete and source published;
this documentation-only receipt changes no tested behavior.
Stage6 remains OPEN,6.12 PARTIAL,6.17 NOT FROZEN. G1/G2/G3/G5/G6/G9 now have
combined local backend evidence; G4 HTTP Comparison available/unavailable verified;
G8 exact reads/max-int8 and fail-closed unsupported writes; G11 validation/default-deny
verified locally. G1–G9/G11 **consumer integration stays OPEN**; G10 static disabled
Android boundary CLOSED. `/v1` compatibility UNCONFIRMED. Title/composer policy,
full-int8 domain writes, final query budgets, client refetch/reconfirmation and
broader hosted/browser/operational/legal acceptance remain open.
C4 **8 PASS/19 PARTIAL/6 OPEN**;6.0f unchecked; C2 §12 remains independent.

No production Supabase/Cloudflare/OAuth, real credentials/data, paid resources,
Stage7 edits, device/ADB, merge/main/tag/release or public API activation. No
Gradle/JVM/lint/APK/instrumentation/screenshots/browser/proxy/hosted tests executed;
no APK/hash/signing claim. Prior encrypted-restore/ACL SQL runs are preserved
historical evidence, not rerun by the static backup/ACL checks above.

Full endpoint/G1–G11/evidence ledger:
`docs/planning/goldberg-0.11/stage-6/API-HTTP-PG-INTEGRATION.md`.
Next: separately scope consumer compatibility/shared fixtures, client reconfirmation,
full-int8 mutations and real policy/operational evidence; keep Android transport,
public API activation, hosted changes and Stage6 acceptance gated.

created by stokonn with love <3

## Stage 6 durable backup-key resilience verification — 2026-10-24

- [x] Add the design-only zero-cost backup operations runbook and link it from
  `ZERO-COST-OPS.md`.
- [x] Recheck official Cloudflare and Supabase pricing, backup, region, DPA and
  transfer pages; preserve the 33-item C4 matrix at **8 PASS / 19 PARTIAL /
  6 OPEN** because account, billing, legal, hosted and day-of evidence remain
  incomplete.

**Purpose:** verify recoverable synthetic encrypted backups after process
termination and key rotation without activating production operations.

**Scope:** `spikes/stage6-backup-local/`, private synthetic evidence and Stage
6 documentation only; no Supabase, real data, scheduler, cloud storage,
Android or Stage 7 worktree.

**Tasks**

- [x] Use external mode-600 recovery-key custody and isolated per-process
  GnuPG state.
- [x] Verify separate-process export/restore, wrong/missing-key rejection and
  v2 rotation with recovery of the older v1 backup.
- [x] Reject partial/tampered archives; verify atomic publication and SHA-256
  manifests.
- [x] Verify schema, 2 metadata, 2 submissions, 2 audit events and audit-chain
  integrity, with cleanup and secret scans.
- [x] Remove interrupted `.part` and manifest-temporary artifacts on phase
  failure.
- [ ] Approve production destination, disaster-recovery key custody, daily
  scheduling, monitoring, retention, hosted restore and measurable RTO.

**Verification:** fresh rootless PostgreSQL 17.11 run exited **0**; export
**3,915 ms**, restore **4,341 ms**, rotation export **4,009 ms**, total
**30,033 ms**. No production RPO/RTO claim is made.

**Completion status:** synthetic resilience drill **COMPLETE**; production
backup readiness and Stage 6 remain **OPEN**. C4 remains **8 PASS / 19
PARTIAL / 6 OPEN** and 6.0f remains unchecked.

created by stokonn with love <3

## C4 read-only verification and Q1-D owner decision — current2026-10-23

Purpose: reconcile current official cloud documentation,inspect only existing
read-only account metadata,and record the owner-reported professional outcome.
Scope: Stage6/Q1/C4 planning and docs/ai only; no backend/Android/schema/cloud changes.
Branch `feature/goldberg-0.11-stage-6-api-v1-local`, recovered clean/remote-matched
HEAD `f3668fc963e1c3b55e5137d397e1881e88237a79`; M3 source/evidence preserved.

- [x] Recover C4,Q1/operator/professional brief,cloud runbook,open decisions,Stage6
  handoffs/checklists and current Issue31; preserve earlier evidence and comments.
- [x] Record **Q1-D SELECTED by owner: operation under owner's own name**.
  Owner reports qualified professional outcome **acceptable / no blocking objection**
  for operator model,contractual eligibility and relevant privacy/protection obligations.
  **D5b RESOLVED only in that reported scope**; no independent professional verification,
  document/signature/legal citation or individual C-1…C-6 answers invented.
  Review date not supplied; report recorded2026-10-23,not inferred as review date.
- [x] Re-fetch official current sources and classify every C4 item in C4 §M.
  **33 items:8 PASS /19 PARTIAL /6 OPEN**,24 official source entries accessed2026-10-23.
- [x] Documentation coverage/history/link/gate/privacy assertions,agent policy and
  whitespace PASS; current M3 source/raw evidence verifier PASS with zero test reruns.
  Evidence:`docs/planning/goldberg-0.11/stage-6/c4-2026-10-23/verification.json`.
- [x] Actual authorized Supabase metadata:1 organization Free/tier_free,1 existing
  eu-central-1/ACTIVE_HEALTHY project,PG17.11.0.003. No identifying metadata stored.
  No current Cloudflare callable tool/plugin found; prior observations remain historical.
- [x] Billing observability checked: metadata omits cards,invoices,add-ons and charges.
  Billing/payment status **NOT VERIFIED** for either provider;0CZK remains goal,not proof.
- [x] Prepare exact day-of evidence matrix without entering any creation/configuration flow.
- [ ] C4 technical/billing/backup/transfer/retention/day-of evidence and owner closure.

**C1 APPROVED; Q1-D SELECTED/D5b bounded owner-reported resolution; C4 OPEN;
ORD-A sequencing only;6.0f UNCHECKED; cloud provisioning/deployment/OAuth/permission
changes NOT AUTHORIZED.** No GDPR/legal-compliance claim or real-user collection.
Other privacy/contact/consent/retention and actual configuration checks remain open.
No cloud resource created,changed or deleted; no keys,production credentials or user
rows accessed; no hosted SQL.3 read-only Supabase metadata calls only. No Android,
Gradle/APK/lint/screenshots/emulator/physical-device/hosted runtime/restore execution.
Stage7A acceptance and X10 deferral Gould0.12 preserved; Stage7B worktree untouched.

New owner selection and direct Supabase metadata materially change the record:
one combined factual Issue31 checkpoint is justified; no duplicate issue or old
checkpoint edit. Issue31 stays OPEN / Goldberg Development In Progress.
Smallest immediate next action: authorized read-only billing/payment/add-on/charge
verification for both providers. This alone cannot close remaining C4 evidence.
Earlier blanket Q1-unresolved/no-selection statements below are historical and
superseded only for this explicitly reported scope; M3 backend status is unchanged.

[Current C4 classifications,sources and day-of requirements](stage-6/C4-PREPROVISIONING-CHECKLIST.md#m-current-c4-verification--2026-10-23-c4-open).

---

## Stage 6 continuation recovery — 2026-10-24

- [x] Recover the canonical policy, maintenance ledger, Stage 6 planning,
  `docs/ai/*`, M1/M2/M3 ledgers, Issue #31, branch/worktrees and publication state.
- [x] Confirm M1, M2 and M3 are already implemented, PostgreSQL-verified and
  ordinarily pushed; no duplicate implementation or publication performed.
- [x] Run the source-bound M3 verifier:
  `python3 spikes/stage6-api-v1-local/verify-m3.py
  spikes/stage6-api-v1-local/results/m3/pg-ygh16ba3` — PASS; recorded evidence
  includes 22 Comparison DTOs, 16 M3 + 19 M2 PostgreSQL tests, 106 adapter/wire,
  38 T22 and 18 planning checks, with zero failures/skips.
- [ ] Obtain the separately approved read-only provider billing/payment/add-on/
  charge evidence; unavailable in this session.
- [ ] Obtain separate authorization before client refetch/reconfirmation,
  HTTP/Android integration, full-int8 writes, public compatibility or any
  production/cloud action.

Status: Stage 6 local API M1–M3 remain verified but integration is not accepted.
C4 remains 8 PASS / 19 PARTIAL / 6 OPEN; 6.0f remains unchecked. This recovery
performed no cloud, OAuth, device, Stage 7B, merge, tag or release action.

created by stokonn with love <3

## Local API M3 — VERIFIED / SOURCE PUBLISHED (2026-10-23)

Purpose: expose approved authorized live Comparison for moderation conflict recovery.
Scope: isolated API adapter/projection, tests/runner/evidence and tracking docs only;
native services/schema/migrations/Android and Stage7B untouched. Branch
`feature/goldberg-0.11-stage-6-api-v1-local`; starting HEAD
`fdbfbe06fcd3faf939d4f6a90cbdeac8ee683e67`. M2 remains published.

- [x] Recover durable checkpoints/contracts/O1–O8/M1/M2, inspect branch/status/ancestry,
  exact remote SHA/releases/Issue31; preserve legitimate work/history.
- [x] Strict local GET `/v1/admin/entities/{id}/comparison?submissionId={id}`;
  live session/queue.read, denied404, exact submission/entity binding, one coherent
  SQL content snapshot, explicit approved DTO, decimal-string live revision/hash/status.
- [x] Detail retains recorded expectedEntityRevision/current submissionRevision/exact
  timestamps. Comparison never relabels baseline, substitutes proposal values,
  mutates moderation or retries decisions; later decisions still validate both revisions.
- [x] Default-deny real policies; exact allowlisted text/evidence only, no private
  provenance. Absent/unmapped/projection_unavailable; no truncation or raw rows.
- [x] Actual PostgreSQL17.11:16 M3 +19 M2 PASS;106 adapter/wire +38 T22 scripted/unit
  regressions and18 planning tests PASS.22 actual PG Comparison DTO captures match
  schema. Zero failures/skips/cancellations in final run; failed attempts retained.
- [x] Current source/raw hashes/protected baseline PASS; exact disposable container
  AND recorded volume removed/absence verified. Evidence `results/m3/pg-ygh16ba3`.
- [x] Implementation/evidence committed and ordinary-pushed; exact remote SHA
  `b01f1e924601d2571ba399888755369de2db3f08` verified. Issue31 verified-progress
  [checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6375047517).
  Completion is tracked in the existing [Issue31](https://github.com/stokonn/Goldberg/issues/31)
  M3 dashboard/comment after final tracking-commit remote readback.

No cross-request snapshot promise; Comparison intentionally has no proposal revision
or timestamp fields. Detail supplies recorded intent separately; client reconfirmation
remains deferred. Public /v1 UNCONFIRMED; full-int8 writes separate; production
policies empty/default-deny. G10 CLOSED,6.12 PARTIAL,6.17 NOT FROZEN; no formal6.13–6.18,
HTTP/Android/APK/screenshots/device/hosted/cloud/OAuth/merge/deploy/release. Stage7A
FINAL ACCEPTED; X10 NOT FIXED/NOT VERIFIED, deferred Gould0.12. Issue31 OPEN /
Goldberg Development In Progress. Next smallest milestone: separately authorized
client Detail/Comparison refetch and explicit reconfirmation integration; no policy activation.

[Full milestone ledger](stage-6/API-V1-LOCAL-M3.md).

Continuity: M3 backend scope is verified; do not repeat M2/M3 publication or
implementation. Recover latest Issue31 M3 checkpoint and actual HEAD/status before
new work. Test-source checker is `verify-m3.py results/m3/pg-ygh16ba3` in the API
package (run from root with full paths); original M2 checker is historical/source-bound.

---

## Local API M2 — VERIFIED / SOURCE PUBLISHED (2026-10-23)

Purpose: bounded authoritative Detail/refetch and actual disposable PostgreSQL
acceptance for the existing local adapter/public receipts. Scope: isolated API
package and documentation; existing services/schema/migrations/Android untouched.

- [x] Strict GET Detail, one SQL content snapshot, live queue.read/account isolation,
  explicit approved DTO, independent baseline/proposal decimal strings, UTC microseconds,
  policy default-deny and bounded history overflow503 without truncation.
- [x] Actual PostgreSQL17.11:19 tests PASS;106 adapter/wire +38 T22 service/scripted-SQL
  regressions and18 planning tests PASS;13 real PG Detail captures match approved DTO.
  Zero failures/skips/cancellations in final run; prior345 baseline not rerun.
- [x] Source-before/after/current and raw hashes verified; UUID-named loopback-only
  container removed/absence verified; failed attempts and M1/security evidence retained.
- [x] Issue31 verified milestone description/comment synchronized; OPEN / Project In Progress.
- [x] Implementation/evidence committed and ordinary-pushed at `1ba7c1a67f5b8d1b2fea9084e9595fca97b7eb52`;
  exact remote SHA matched. [Completion checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6374687079)
  description/comment readback PASS, exactly one completion marker; Issue31 OPEN.

[Full purpose/scope/tasks, exact commands, attempts and limits](stage-6/API-V1-LOCAL-M2.md).
Current baseline is recorded submission intent, not live catalogue state. Comparison,
history cursors, client reconfirmation/full-int8 writes/public compatibility stay open.
G10 CLOSED;6.12 PARTIAL;6.17 NOT FROZEN; no formal6.13–6.18 or HTTP/Android/device/cloud/
OAuth/production/merge/release. Stage7A FINAL ACCEPTED; X10 unfixed/unverified, deferred
Gould0.12; No M2 writes to Stage7B/security worktrees. Stage7B advanced externally during this run;
its new work is preserved. Security HEAD/status/diff remained unchanged.

---

## Stage 6 API local M1 — verified source published (2026-10-23)

**LOCAL MILESTONE 1 VERIFIED / SOURCE PUBLISHED; INTEGRATION NOT ACCEPTED.**
Existing isolated worktree reused, clean at recovery HEAD `c84c4b99eb27034e3630ff18cf2c470410dbbea2`;
implementation `337847770a579e1f6689c135805ad07a9b2bab19`, approved ancestor
`6dd3136269ec7f5932c8c4b3689fb065d4db9105`. Source and security evidence were
already implemented/committed; recovery audited rather than duplicated that work.
Ordinary guarded push succeeded and exact remote SHA matched c84c4b9.
[Published factual checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6372592793). Issue31 OPEN / Goldberg Development In Progress.
Historical DNS/403 publication failures below remain unchanged audit records,
superseded by this successful authorized CLI publication. No new authentication.

- [x] Read approved design, services, adapter, test venues and current handoffs;
  retain existing separate branch/worktree and preserve other worktrees.
- [x] Execute `python3 spikes/stage6-api-v1-local/verify.py`: PASS;106 adapter/wire
  plus38 T22 and18 planning-verifier results, raw hashes and source-before/after/
  current binding verified. These are inspected prior executions, NOT tests rerun
  by publication recovery; prior345 executions were not repeated.
- [x] Protected services/schema/Android/contracts/security evidence byte-unchanged
  against approved baseline; whitespace and publication-scope/clean-HEAD guards PASS.
- [x] Guarded publisher ordinary push and Issue31 description/comment readbacks PASS;
  prior comments preserved, exactly one source-SHA-marked M1 checkpoint.
- [ ] Public `/v1` compatibility remains UNCONFIRMED; local/provisional only.
- [ ] PostgreSQL receipts/locking/durability proof, full-int8 domain writes,
  latest-detail/refetch, HTTP and client/session integration remain unverified.

Executable LOCAL operations: challenge/exchange/refresh/session/capabilities/logout,
explicit moderation open and approve/reject/request_changes. Native domain/Auth/
Community services are unchanged; the new adapter uses explicit mappings, canonical
wire validation and exact public receipts inside domain transactions. No listener.
G5–G8 LOCAL foundation verified; integration gaps remain OPEN. G1–G4 absent, G10
CLOSED, real-world policy empty/default-deny,6.12 PARTIAL,6.17 NOT FROZEN; no formal
6.13–6.18, Android/network activation, production OAuth/cloud, device, merge or release.
Historical PG preflight was EPERM; recovery launched no PG/container/listener and
executed zero PG/HTTP/Android bodies. No current-host availability inference.

Next smallest milestone: bounded authoritative latest-submission Detail/refetch for
409 recovery and permitted disposable-PG adapter/receipt verification. Full-int8
writes and public-version adoption remain separate gates. Stage7A FINAL ACCEPTED;
X10 NOT FIXED/NOT VERIFIED, deferred to Gould0.12; Stage8 readability/accessibility/
touch-target checks retained. Preserve Stage7B dirty work unchanged.

---

## Retained implementation / earlier publication record

# Goldberg 0.11 — stage checklist (permanent, living document)

## Current — Stage 6 API local Milestone 1 (2026-10-23)

**LOCAL IMPLEMENTATION VERIFIED / PROVISIONAL v1 / INTEGRATION NOT ACCEPTED.**
Owner authorization permits this isolated adapter foundation and focused verification,
superseding planning-only limits for this milestone only. Branch
`feature/goldberg-0.11-stage-6-api-v1-local`, worktree
`/home/stokonn/Projects/Goldberg-Stage6-API`, exact approved baseline
`6dd3136269ec7f5932c8c4b3689fb065d4db9105`; original security branch/evidence preserved.
[Approved-design checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6371704250).

Purpose/scope: explicit in-process public request/response adapter around existing local
services for G5–G8. No listener, Android changes, browser/hosted integration or public
version adoption. `/v1` consumer compatibility remains unconfirmed, so local paths
are provisional; never silently select `/v2`.

- [x] Inspect Stage6 worktrees and dirty Stage7B read-only; create separate branch from
  exact baseline. No unrelated worktree reset/cleanup/edit or security branch change.
- [x] Implement strict method/path/body/schemaVersion/unknown-field/duplicate-key
  validation, explicit operation projection, safe error taxonomy, request correlation
  and empty204 logout. No competing-dialect inference.
- [x] Execute challenge/exchange/session/capabilities/refresh/logout through actual
  local services; stored identity/expiry projection, live grants, family revocation,
  approved durations and controlled RSA/JWKS exchange. Account principal is server-derived.
- [x] Execute explicit open and three decisions with independent entity/proposal
  revisions; typed200/201, generic409, exact public receipts stored atomically with
  effects,24h from completion, changed intent422 and live authorization before replay.
- [x] Lossless decimal-string int8 reads; valid unsupported full-int8 writes fail503
  after authentication/authorization; malformed values400; domain safe-increment
  exhaustion409. UTC microseconds preserved, scoped signed cursor codec15m tested.
- [x]106 adapter/wire tests PASS:34 service/scripted-SQL,27 original request-scene
  adapter tests,24 routing/validation/security,21 wire/cursor/composition units.
  Includes all8 protocol scenarios and one controlled RSA/JWKS exchange.38 relevant
  T22 regressions and18 planning-verifier tests PASS separately. Source bound before/
  after run and current verification; raw logs retained, no hidden skipped bodies.
- [ ] Local PostgreSQL preflight EPERM;0 DB bodies. No actual HTTP/Android/browser/
  runtime/hosted acceptance. Prior345 executions/313 tests/205 PG-backed bodies
  unchanged and not rerun; full threat matrix remains6.12 PARTIAL/T22 LOCAL PASS.
- [ ] Public latest-detail/refetch DTO, full-int8 moderation writes, PG receipt proof,
  client single-flight/account isolation/204 handling and real HTTP binding remain open.
- [ ] No G1–G4 implementation, real-world policy approval, cloud/OAuth production,
  networking activation, formal6.13–6.18 implementation or6.17 freeze. All real
  source/field/license/attribution/evidence policies stay empty/default-deny.

G10 CLOSED; G1–G9/G11 integration acceptance BLOCKED. Original service/schema/Android/
contract/evidence files unchanged. New receipt wrapper uses existing storage helpers/
table, no migration. See `stage-6/API-V1-LOCAL-M1.md` and package README for actual
operations, test venues, compatibility findings, limitations and next milestone.

Stage7A is FINAL ACCEPTED; X10 is NOT FIXED/NOT VERIFIED, owner-deferred to Gould0.12
([acceptance](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6371745218)).
This supersedes earlier conditional/X10-open cosmetic release gating in historical
entries below. Stage8 readability/accessibility/touch-target verification remains
required; Stage7B integration acceptance remains separate and blocked.

Next smallest local milestone: bounded authoritative latest-submission Detail/refetch
projection for409 recovery, with permitted disposable-PG verification of adapter/public
receipts. Numeric full-int8 writes stay a separate tested change. Stop at all remaining
production, policy, public-version and deployment gates. Publication instructions and
actual outcomes: docs/ai/STAGE6-API-M1-PUBLICATION.md.
Publication blocked: normal push DNS error; connected new branch lookup422/no commit.
Both Issue31 writes denied403; description and81 historical comments unchanged.
No new checkpoint URL; guarded Fedora source publication remains pending.


## Current — API v1 owner design approval recorded (2026-10-23)

**O1–O8 APPROVED AS QUALIFIED DESIGN; O7 PARTIAL; UNIMPLEMENTED / INACTIVE / NOT FROZEN.**
Owner authority: explicit instruction in this conversation; durable record
`stage-6/API-V1-OWNER-DECISIONS-2026-10-23.md` and machine-readable
`contracts/admin/v1/decision-proposal/design-approval-2026-10-23/approval.json`.
Published proposal baseline `fefa6e83c2a7b9c4c12660ff1542ab086356de88` and
[checkpoint6371392069](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6371392069)
verified through connected GitHub reads. This supersedes earlier unpublished-proposal
and pending O1–O8 summaries below; all previous evidence remains historical.

Purpose/scope: record the owner's exact design decisions, reconcile planning/readiness
and verify synthetic fixtures only. Current lifecycle/TTL/revision/numeric/HTTP/audit/
validation design is approved; actual local backend and Android behavior are unchanged.

- [x] Record O1 explicit mapper/204 logout/no secret-bearing offline queue, O2 exact
  access15m/family7d/challenge5m/skew≤60s and refresh rules, O3 independent revisions/
  explicit open/typed200–201/generic409/refetch/reconfirm/exact24h receipts.
- [x] Record O4 lossless int8 strings/UTC precision/scoped cursors15m/404–405 AS DESIGN;
  O5 server Europe/Prague civil daily boundaries including DST, UTC transport,
  coherent exact snapshot and one approval count; O6 NFC/case-insensitive/
  diacritic-sensitive literal search AS DESIGN; O8 documentation sequence only.
- [x] Record O7 PARTIAL: safety validation/1000 UTF-8-byte reason/current projection
  approved by design. All concrete real-world source/field/license/attribution/
  evidence policy sets remain empty/default-deny; no synthetic promotion/legal clearance.
- [x] Preserve original61 DTO/8 protocol scenes and historical UTC alternative;
  add separate4 Prague civil/DST windows and10 CZ/EN/literal search planning cases.
- [ ] Confirm no previously approved/deployed incompatible v1 consumer before `/v1`;
  otherwise prepare `/v2` for separate owner decision. No confirmation claimed here.
- [ ] Determine final query budgets by later local measurement; separately review
  concrete policies and authorize numeric/HTTP/backend/Android implementation.
- [ ] G1–G9/G11 integration BLOCKED; G10 CLOSED;6.12 PARTIAL/T22 LOCAL PASS;
  6.17 NOT FROZEN. No formal6.13–6.18 implementation or API freeze authorized.

Prior345 executions/313 tests/205 PostgreSQL-backed bodies preserved, not rerun.
No Stage7B edits, devices, live endpoints/auth/network/actions, cloud/OAuth production,
merge, deployment or release. Stage7B acceptance BLOCKED, X10 OPEN, Stage8 pending;
existing security/hosted/runtime/zero-cost/legal gates remain. Separate implementation
and policy authorization gates are the stopping point; no repeated O1–O8 approval needed.
Verification: docs/ai/VERIFICATION.md and the separate dated approval verification log.
Publication: docs/ai/API-V1-PROPOSAL-PUBLICATION.md; guarded publisher now emits the
approval checkpoint, never republishes the superseded pending-design summary.
Publication attempt blocked: normal push DNS failure; connected branch remains fefa6e8.
Both Issue31 writes denied403; description and79 prior comments unchanged, no new URL.
Guarded Fedora publisher with final exact clean SHA remains the next publication step.


Canonical policy: [AGENTS.md](../../../AGENTS.md) (§ "Stage checklist documentation").
This checklist is synchronized continuously as work progresses. A stage is marked
**complete** only after its verification section records checks that were actually
executed and passed. Checkboxes without verified evidence must stay unchecked; checked
boxes cite their evidence below or in the linked stage ledger.

Status legend: ✅ complete (verified) · 🚧 in progress · ⬜ not started

---

## Stage 1 — Architecture & readiness planning

**Status: ✅ complete (documentation-only stage).** Branch `planning/goldberg-0.11-stage-1`,
baseline `556970ab7407ec6986e3678657adbd2168516468`; evidence:
[Stage 1 verification](VERIFICATION.md).

- **Purpose:** Define the 0.11 product architecture, stage boundaries, decision register
  (D1–D9) and acceptance gates from the approved product scope, without touching code.
- **Scope:** `docs/planning/goldberg-0.11/*` only. No application, Gradle, schema, CI,
  signing, cloud account or release change.

**Tasks**

- [x] Verify ancestry, canonical/maintenance policies and preserved owner work (RELEASE_NOTES.md untouched).
- [x] Write stage independence gates, decision register and handoff rules (STAGES.md).
- [x] Write architecture/data-model/contracts sketches and offline-cache proposal (OFFLINE-CACHE.md, DATA-MODEL.md).
- [x] Record cloud/backend/admin gating documents (CLOUD.md, COMMUNITY-ADMIN.md) as future-stage decisions.
- [x] Run baseline host gates and record exact results (no new emulator runs claimed).
- [x] Reuse existing GitHub Project/milestone/umbrella issue #31; planning-only Wiki link.

**Verification (executed 2026-10-18):** Gradle cached build BUILD SUCCESSFUL + explicit
`testDebugUnitTest --rerun` **75 tests / 0 failures**; lint **0 errors** (83 warnings);
`check_migrations`, `check_backup_fixtures`, `check_history_paging`, `check_localization`,
`check_agent_policy` all PASS; 10-doc link/fence check PASS. Instrumentation not executed
(documentation-only change set; explicitly not claimed).

---

## Stage 2 — Contracts, fixtures and measured readiness

**Status: ✅ complete (research-only stage).** Branch `planning/goldberg-0.11-stage-2`,
descends from Stage 1 `c3d5e03b9a4936d8db393df664af209a8853c767`; evidence:
[stage-2/README.md](stage-2/README.md), [stage-2/READINESS.md](stage-2/READINESS.md).

- **Purpose:** Turn the approved product scope into concrete, testable contracts: catalogue
  data model and physical separation, provider field/rights policy, identity matching,
  backup evolution, measured storage/performance budgets, and adversarial fixtures.
- **Scope:** `docs/planning/goldberg-0.11/stage-2/**` (SQL fixtures, Python research tools,
  JSON contracts). No application code, no schema export change, no deployment.

**Tasks**

- [x] Finalize logical/physical model, adapter/search/provenance/bundle envelopes (CONTRACTS.md, contracts.json).
- [x] Specify separate catalogue DB with durable personal references (DATABASE-BACKUP.md).
- [x] Build deterministic licensed fixtures + adversarial contract tests (fixtures/, verify_stage.py).
- [x] Run bounded provider investigation with overload stops; record coverage and failures (SOURCE-RESEARCH.md).
- [x] Measure query cost, memory/storage and bundle budgets (MEASUREMENTS.md).
- [x] Owner settled D1/D2/D3/D6 and D7/D9 direction (multi-genre works/arrangements, UUIDs + legacy IDs, separate catalogue DB, measured caps).
- [x] Record remaining blockers (D4/D5/D8 backend/operator/Admin) as later-stage gates.
- [x] Commit/push planning branch; update existing tracking (issue #31 evidence).

**Verification (executed 2026-10-18):** deterministic fixture/contract suite via
`stage-2/verify_stage.py` PASS; research/bench runs are recorded as measurements, not as
production capacity claims; no application build was altered (research-only artifacts).

---

## Stage 3A — Additive local persistence (journal schema9 + catalogue schema1)

**Status: ✅ complete (verified).** Branch `feature/goldberg-0.11-stage-3a`, baseline
Stage 2 `d6049add3e46399d8f48afcb2aaa03422ebe0017`, prerequisite CI
[39120954962](https://github.com/stokonn/Goldberg/actions/runs/39120954962) passed;
evidence: [stage-3a/VERIFICATION.md](stage-3a/VERIFICATION.md).

- **Purpose:** Add the safe persistence bridge: transactional personal schema 9 (UUID
  anchors, catalogue links) and an independent catalogue database (schema 1, FTS4, bounded
  DAOs) without destructive migrations or journal rewrite.
- **Scope:** `OpusDatabase` (additive schema 9 + migrations), new catalogue DB/DAO/store,
  personal↔catalogue bridge, lazy retryable catalogue reconstruction, tests.

**Tasks**

- [x] Transactional schema 9 bridge preserving all schema 8 tables/bytes.
- [x] Independent catalogue schema 1 with bounded DAOs, FTS4, typed graph/rights validation.
- [x] Lazy, retryable local reconstruction isolated from journal startup.
- [x] All historical Android migrations + journal regression + catalogue failure tests.
- [x] Seeded signed in-place 0.10.1 upgrade with original data/prefs comparison.
- [x] JVM/build/lint/host compatibility checks and targeted measurements.
- [x] Technical documentation + English Wiki sources.

**Verification (executed):** full historical migration matrix on Android and host fixtures;
seeded in-place upgrade PASS; JVM/build/lint gates PASS (see stage-3a/VERIFICATION.md for
exact counts); instrumentation executed on emulator only.

---

## Stage 3B — Portable backup v5 and safe recovery

**Status: ✅ complete (verified).** Branch `feature/goldberg-0.11-stage-3b`, descends from
verified Stage 3A `cb36535d8a733143213347dd2ce355d1bf81ae96`, prerequisite CI 39125571414
passed; evidence: [stage-3b/VERIFICATION.md](stage-3b/VERIFICATION.md),
[stage-3b/CHECKPOINT.md](stage-3b/CHECKPOINT.md).

- **Purpose:** Make new identities/settings/recovery portable: additive journal schema 10,
  bounded hash-validated portable format 5, unchanged format 1–4 readers, raw-recovery
  evidence and allowlisted preference replay.
- **Scope:** backup reader/writer, journal schema 10 (additive), recovery/draft flows,
  golden fixtures, crash/corruption/size/atomic-publication tests.

**Tasks**

- [x] Additive journal persistence for raw recovery evidence, completion evidence, preference replay.
- [x] Bounded/hash-validated portable v5; historical readers 1–4 keep their meaning.
- [x] Destination snapshots/drafts/archives and imported UUIDs preserved; paused recovery with fresh execution IDs.
- [x] Allowlisted preference replay with explicit pending/retry state.
- [x] Full/minimal golden fixtures + corruption/crash/size/atomic-publication tests.
- [x] Sequential API 26 + API 37 instrumentation, seeded upgrade, offline and process-death checks.
- [x] Implementation/verification/handoff documentation + GitHub/Wiki synchronization.

**Verification (executed):** build/JVM/lint/host checks PASS; sequential API 26/API 37
instrumentation PASS; seeded upgrade, offline and process-death (×4 boundaries) checks PASS
(see stage-3b/VERIFICATION.md for exact counts and commands).

---

## Stage 4 — Provider adapters and unified read/search

**Status: ✅ complete (verified).** Branch `feature/goldberg-0.11-stage-4`, baseline Stage 3B
`9ae8d33e113feba531cdfa8558f0505cd4205749`; verified head `99ea73a` (also the Stage 5
baseline). Evidence: [stage-4/CHECKLIST.md](stage-4/CHECKLIST.md),
[stage-4/PROGRESS.md](stage-4/PROGRESS.md), `docs/ai/VERIFICATION.md` (baseline section).

- **Purpose:** Ship the local-first catalogue experience: five independent provider
  adapters (Open Opus, MusicBrainz, Wikidata, Wikipedia, Commons) behind typed contracts,
  a unified Explore/Search UI, indexed local search with generation-bound paging, cached
  provider responses with rights/attribution, and safe work/arrangement linking.
- **Scope:** provider transport/adapters, catalogue store/search/paging, Explore/Search UI,
  catalogue cache/portraits storage admission, personal-library links, CZ/EN resources.

**Tasks**

- [x] 4A Independent adapters, bounded cancellable transport, host allowlist, rights fields, adversarial fixtures, live provider probes.
- [x] 4B Typed catalogue integration, hash-bound observations, graph/identity validation, deletion/rebuild regressions.
- [x] 4C Indexed local-first search, ranking/normalization, generation-bound cursor paging, cache budgets/coalescing, privacy/offline/performance.
- [x] 4D Explore/Search/detail UI with provenance, CZ/EN, accessibility, rendered emulator checks (0.10.1 design identity preserved; owner waived screenshot review for 0.10, documented).
- [x] 4E Explicit work/arrangement selection, duplicate prevention, durable links/snapshots, practice/recovery regressions.
- [x] 4F Host checks, JVM/lint/APK, executed API 26 + API 37 instrumentation, seeded trusted 0.10.1 upgrade, artifact identity, branch verification docs.

**Verification (executed 2026-10-20 re-verification at `99ea73a`, emulator `emulator-5592`
API 37, explicit serial):** `testDebugUnitTest` **97 tests / 0 failures**; `lintDebug`
**0 errors** (107 warnings); `assembleDebug` + `assembleDebugAndroidTest` BUILD SUCCESSFUL;
`connectedDebugAndroidTest` **143 tests / 0 failures / 2 skipped** (`assumeTrue`
external-fixture guards); host checks `check_migrations`, `check_backup_fixtures`,
`check_localization`, `check_history_paging`, `check_agent_policy` PASS; seeded in-place
upgrade `verify_emulator_upgrade.py` (0.10.1/schema8 → schema10) PASS with exact
persistent-table equality; real process death ×4 boundaries PASS; APK SHA-256
`ac1ad27a5c42ec8f30a306c522943575183e2f5f4d9e41baef8f394d333aa3f4`, signer
`53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`, `com.opus.piano 0.10.1/code 11`.

---

## Stage 5 — Offline bundles and Smart Cache 2.0

**Status: ✅ complete — owner-approved 2026-10-21.** Branch `feature/goldberg-0.11-stage-5`,
baseline Stage 4 verified head `99ea73a`; completion commit `736f6d3` (owner approval) on
head `32cf5d9`. Owner-approved decisions: D5.1 file-based manifests, D5.3 WorkManager
(owner-mandated), D7 storage caps + per-job metered consent, D5.6 test-only org.json
(see `docs/ai/DECISIONS.md`). Verified evidence: emulator regression CI run
[39557624554](https://github.com/stokonn/Goldberg/actions/runs/39557624554)
(177 instrumentation tests / 0 failures / 0 errors / 2 designed skips) and screenshot
CI run [39576173002](https://github.com/stokonn/Goldberg/actions/runs/39576173002)
(12/12 required screenshots captured and inspected). **Limitation preserved by owner
disposition:** real production-CDN bundle-transfer measurements remain a future
integration dependency (no bundle feed exists; must be re-measured when it ships —
no numbers fabricated). The authoritative Stage 5 completion record (full executed
ledger, evidence bundles under `stage-5/results/`, final verification detail) lives on
the Stage 5 branch; **this Stage 6 planning branch does not repeat it and Stage 5 is
not reopened here.** No release/merge/tag authorized.

- **Purpose:** Let users keep encyclopedia/media content available offline through
  explicitly pinned, rights-complete bundles: durable file-based manifests, a
  content-addressed store with mark-and-sweep, a WorkManager-backed background scheduler
  that never downloads over mobile data without explicit per-job consent, hash-verified
  transfers with hard byte caps, atomic publication that keeps the previous READY revision
  through interrupted refreshes, and full isolation from personal journal/recovery data.
- **Scope:** `data/offline/*` (model, codec, policy, state machine, store, job records,
  network monitor, transfer engine, runner, scheduler, worker), `OpusApplication` lazy
  wiring + startup reconcile, test-only Gradle additions (org.json, work-runtime,
  work-testing). **No** journal/database/backup/signing/package change; no UI redesign.

**Tasks**

- [x] 5.1 Durable model + strict JSON manifest codec with mandatory per-file rights (OfflineBundle.kt, BundleManifestCodec.kt) — 21/21 codec tests.
- [x] 5.2 Reviewed lifecycle state machine incl. process-restart mapping and READY-safe recovery paths (BundleStateMachine.kt) — 13/13 tests.
- [x] 5.3 D7 policy matrix: validated network, per-job consent, Data Saver, stop reasons, confirmation gates (BundlePolicy.kt, BundleNetwork.kt) — 11/11 tests.
- [x] 5.4 Content-addressed store + mark-and-sweep + staging + quarantine (BundleStore.kt, BundleStoreLogic.kt) — logic 10/10 JVM tests; file I/O needs instrumentation.
- [x] 5.5 Durable per-job state: allowance/confirmation/attempt/cancel records (BundleJob.kt, BundleJobStore.kt, AndroidBundleJobPreferences.kt) — 8/8 tests.
- [x] 5.6 Transfer engine: streaming with hard caps, per-64 KiB policy re-checks, hash verification, provider host allowlist (BundleTransferEngine.kt) — 14/14 tests.
- [x] 5.7 Pipeline runner: QUEUED→READY with durable PAUSED/WAITING/FAILED_RETRYABLE/QUARANTINED outcomes (BundleJobRunner.kt) — instrumentation tests written.
- [x] 5.8 WorkManager scheduler + worker: unique work per `bundleId:revision`, UNMETERED unless per-job consent, revoke/grant/cancel, startup reconciliation (BundleScheduler.kt, BundleDownloadWorker.kt).
- [x] 5.9 `OpusApplication` lazy wiring + startup reconcile + hermetic test seams.
- [x] 5.10 Execute `Stage5*Test` instrumentation on an explicitly identified emulator (store I/O, runner pipeline, real WorkManager execution/cancel/reconcile). — **COMPLETE (owner-approved 2026-10-21): CI run 39557624554 (attempt 6), 177 tests / 0 failures / 0 errors / 2 designed skips.**
- [x] 5.11 Emulator acceptance: Wi-Fi→mobile transition, Data Saver, consent revocation mid-stream, real process death at boundaries, low disk, seeded in-place upgrade from trusted 0.10.1, disk/network cost measurements. — **COMPLETE: every item has executed passing evidence (run 39557624554 + instrumentation); screenshots run 39576173002 GREEN 12/12 (Ivory/Nocturne × six screens, pair-by-pair inspected). Disk/network costs: executed byte accounting + real file I/O recorded; real-CDN transfer measurements owner-dispositioned as a future integration dependency (no bundle feed exists).**
- [x] 5.12 Full host gate re-run + `scripts/check_*.py` (migrations/backup/localization/paging/policy). — **COMPLETE: host gate BUILD SUCCESSFUL, unit tests 174/0/0/0, lint 0 errors / 110 pre-existing warnings; all 5 check scripts PASS.**
- [x] 5.13 Update `docs/ai/*` (STATE/VERIFICATION/HANDOFF/DECISIONS), reviewed commit, push to Stage 5 branch, verified evidence to GitHub Issue #31 + Project. No merge/tag/release/APK publication; Stage 6 not started. — **COMPLETE: docs synchronized, evidence bundles committed, Issue #31 checkpoints posted, only workflow files merged to main (PRs #35–#38); no application code merged, no tag/release/APK publication.**

**Verification (executed so far, 2026-10-20 sessions — HISTORICAL mid-progress log,
retained as audit trail):**

> **Reconciliation note (2026-10-21, Stage 6 planning branch):** this log was frozen
> mid-Stage-5 when this branch was cut (`bcd8894`). It is kept unchanged as historical
> evidence. The **authoritative final** Stage 5 verification record — including the
> green CI runs 39557624554 (177/0/0/2) and 39576173002 (12/12 screenshots), the
> root-cause triage of the earlier failed runs and the owner-approved completion
> (2026-10-21) — lives on `feature/goldberg-0.11-stage-5` (completion commit `736f6d3`,
> head `32cf5d9`) in its `docs/ai/*` files, `STAGE-CHECKLIST.md` and
> `docs/planning/goldberg-0.11/stage-5/results/`. Nothing here supersedes that record.

- Checkpoint #5 host gate (`clean assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug`) BUILD SUCCESSFUL; **146 tests / 0 failures**; lint **0 errors** (109 warnings).
- After 5.5–5.9 implementation: `compileDebugKotlin` BUILD SUCCESSFUL; `testDebugUnitTest` **174 tests / 0 failures** (28 new scheduler/engine/job tests).
- 2026-10-20 durable checkpoint: all Stage 5 work committed and pushed to the Stage 5 branch;
  permanent AGENTS.md rules (stage checklists, GitHub Issue checklists) added; STAGE-CHECKLIST.md created.
- 2026-10-20 instrumentation-fix session (uncommitted working tree, HEAD `54f2b45`):
  `Stage5BundleJobRunnerTest` compile issues fixed (6 errors, no assertion weakened) →
  **EXECUTED** `:app:assembleDebugAndroidTest` **BUILD SUCCESSFUL** (4m54s, 0 errors, 7 new
  nullable-`listFiles()` warnings); **EXECUTED** `:app:testDebugUnitTest` but all tasks
  UP-TO-DATE → 0 tests re-executed (last real counts remain the prior session's 174/0).
  `Stage5BundleWorkerTest` (12 tests, real WorkManager test infra) written; **EXECUTED**
  androidTest compile after adding it → **FAILED, exactly 1 error** (`Stage5BundleWorkerTest.kt:324`,
  nullable `TestDriver?` — one-line fix pending). Two static findings (F1 rights-quarantine
  unreachable, F2 corrupt-blob repair dead-end) recorded in `docs/ai/STATE.md`/`DECISIONS.md`,
  **not executed, no production change applied**. `docs/ai/*` checkpoint created (STATE/
  VERIFICATION/HANDOFF/DECISIONS). No tasks 5.10–5.13 completed; Stage 5 remains incomplete.
- Pending (not yet executed, no claim made): fix the worker-test compile error, re-verify
  androidTest compile + fresh `testDebugUnitTest --rerun`, resolve F1/F2, then Stage 5
  instrumentation on an explicitly identified emulator (5.10), emulator acceptance matrix (5.11),
  final host gate + `scripts/check_*.py` (5.12), `docs/ai/*` commit/push + Issue #31/Project sync (5.13).
- 2026-10-20 second resume session (checkpoint commit pushed as owner-authorized IN-PROGRESS
  checkpoint, verification incomplete): worker-test L324
  compile error FIXED → **EXECUTED** `assembleDebugAndroidTest` **BUILD SUCCESSFUL 0 errors**;
  **EXECUTED** `testDebugUnitTest --rerun` → **174/0/0/0** (fresh). F1/F2 investigated,
  confirmed against source and **owner-approved**: F1 test retargeted to the reachable contract
  (test-only), F2 `BundleStore.installBlob` dedup now verifies the existing target and
  quarantines invalid bytes before installing the verified copy (production fix, ~15 lines).
  After both changes: **EXECUTED** `testDebugUnitTest + assembleDebugAndroidTest` → **BUILD
  SUCCESSFUL, 174/0** (reports 19:23). PMP identified (Project Memory Protocol,
  `stokonn/project-memory-protocol`); local state verified **absent everywhere** (CLI, XDG
  state, `.pmp`/`.pmp.yml` in any commit, clones) — not reinitialized. Emulator toolchain
  installed (emulator 37.2.12.0 + android-36 google_apis x86_64), AVD `stage5-api36` created
  (AVD home: `/home/stokonn/.config/.android/avd` → needs `ANDROID_AVD_HOME`), **booted
  emulator-5580 (API 36, qemu=1)**; stale `app-debug.apk` (Oct 16) caught before any test and
  rebuilt (**EXECUTED** `assembleDebug` → BUILD SUCCESSFUL, fresh APK 19:45). Battery emergency
  stopped the session: emulator cleanly killed (`emu kill` OK); **zero instrumentation tests
  executed**; tasks 5.10–5.13 all remain open.
- 2026-10-20 third session (desktop machine, checkpoint commit pushed): fresh
  `assembleDebug + assembleDebugAndroidTest` **BUILD SUCCESSFUL** (1m08s, 0 errors, fresh
  APK hashes recorded in `docs/ai/VERIFICATION.md`). All five `scripts/check_*.py`
  **PASS**; full host gate re-run executed (result in VERIFICATION.md). Emulator work on
  the desktop **blocked by upstream SIGSEGV bug (Google #566807521)** on Fedora kernel
  7.2.10: both AVDs (playstore API 37 + new google_apis API 36 `stage5-api36`) and both
  emulator versions (37.2.12 + 37.3.4 canary) crash after the "protected range" warning,
  independent of GPU mode — **no instrumentation executed; tasks 5.10/5.11 deferred to the
  notebook's working API 36 emulator** (exact steps in `docs/ai/HANDOFF.md`). Host-side
  portion of task 5.12 executed on the desktop; 5.13 checkpoint docs updated + pushed, but
  5.13 stays open pending Issue #31/Project sync after 5.10/5.11. PMP local
  `.pmp/`/`.pmp.yml` preserved untouched (never reinitialized).
- 2026-10-20 GUI-mode + CI follow-up (same session): the owner-requested **GUI-mode**
  emulator attempt on the GNOME desktop (explicit `emulator-5580`, `-gpu auto`, session
  XAUTHORITY) also ended **`Killed by SIGSEGV`** with no ADB device → all local emulator
  retries stopped. A **CI verification workflow was prepared and later committed+pushed**
  as `1ef15bf` (`.github/workflows/stage5-emulator.yml`, `workflow_dispatch` only,
  SHA-pinned, full `connectedDebugAndroidTest` for 5.10, digest-verified 0.10.1 upgrade +
  network/process-death/startup evidence for 5.11); **no CI execution has happened; no CI
  results claimed**. See `docs/ai/STATE.md` items 6–7 and `docs/ai/VERIFICATION.md` rows
  11–12.
- 2026-10-20 CI-activation session (Phase A/B of the owner's CI plan; **no CI run, no
  emulator run, no instrumentation — approval gate pending**):
  - **Phase A — CI-only PR prepared, NOT merged.** Worktree off `origin/main`
    (`556970a`), branch `ci/stage5-emulator-verification`, commit `9870d99`, draft
    [PR #32](https://github.com/stokonn/Goldberg/pull/32) — diff verified **workflow-only**
    (1 file, +379): no application source, database, release, PMP or unrelated documents.
    Stage 5 working tree and all untracked files preserved untouched. Workflow safety
    re-reviewed: `workflow_dispatch` only, `contents: read`, `persist-credentials: false`,
    all five action SHA pins re-verified against their release tags, 30-day evidence
    retention, 150/120-min timeouts, per-ref concurrency, qemu-verified `emulator-NNNN`
    serial guards on every adb call.
  - **Phase B — findings investigated and resolved in the revised workflow (same file
    now also on this branch):** (1) *signing* — apksigner-verified the trusted 0.10.1
    release APK carries cert `53eface4…`; a CI debug candidate can never be installed
    over it in place, so the upgrade test now uses a **debug rebuild of the exact
    v0.10.1 tag built in the same job** (same runner keystore, signatures match by
    construction) while release-signature compatibility stays a publication gate —
    no production credentials in CI; (2) *network checks* — the old
    `cmd netpolicy set data_saver enable` is **invalid on API 36** (AOSP-verified) and
    the Wi-Fi→mobile/Data Saver steps only collected logs → replaced with
    `set/get restrict-background` state readback + `dumpsys` state assertions + a
    transport-level default-network parser (unit-tested; mobile data is re-enabled
    after the `--offline` upgrade run); (3) *process death* — now pulls the database
    after real force-stop → cold restart and asserts `PRAGMA integrity_check`/`foreign_key_check`/
    `user_version`; (4) *exclusions* — scan confirms **0** `@Ignore`/filters/excluded
    classes; the full 42-class suite runs unfiltered; (5) *coverage honesty* —
    CI-covered vs manual-only 5.11 items (consent revocation mid-stream, low disk,
    screenshots, real bundle-transfer costs) documented in the workflow header;
    (6) *SHA binding* — both jobs record and assert `git rev-parse HEAD == GITHUB_SHA`.
    Full executed/not-executed ledger: `docs/ai/VERIFICATION.md` (CI-activation section).
  - **STOPPED at the owner approval gate as instructed:** merging PR #32 into `main` and
    any manual `workflow_dispatch` require explicit owner approval (Phase C). Tasks
    5.10–5.13 remain open; no merge/tag/release; Stage 6 not started.

**Completion criteria (from STAGES.md Stage 5 acceptance):** process-death/resume,
low-space, corrupt-asset, concurrent remove/clear/restore tests; Wi-Fi→mobile,
consent-revoked and Data Saver cases on an emulator; no unexpected mobile traffic; complete
offline credits and partial states; old READY revision survives interrupted refresh;
practice data stays available when all bundles are removed; measured disk/network costs.
Mark ✅ only when all of the above have executed-passed evidence.

---

## Stage 6 — Secured optional community backend

**Status: ⬜ planning prepared and owner decisions recorded (2026-10-21);
implementation not started.** The detailed implementation-ready plan lives in
[stage-6/](stage-6/README.md) (architecture, auth, data model, API contracts, threat
model, zero-cost ops, decision register, handoff); the authoritative task list is
[stage-6/CHECKLIST.md](stage-6/CHECKLIST.md) — **every implementation task there is
unchecked.**

**Owner decision review (2026-10-21), outcomes recorded in
[stage-6/OPEN-DECISIONS.md](stage-6/OPEN-DECISIONS.md):**

- ✅ **Approved:** D4a Supabase Free (one optional backend, strict 0 CZK/month, no
  paid plans/trials/auto-upgrades); D4b free-tier suspension/quotas accepted with
  graceful degradation, core local functionality preserved and fail-closed behavior;
  D5c Frankfurt `eu-central-1` (with pre-provisioning verification of availability,
  residency, transfers, DPA, subprocessors); D5f general-audience moderated community
  with minimal data; D5g owner-only moderation initially with server-side audited
  grants; D8a in-app Android Admin (no separate APK); D8c Supabase-console bootstrap
  with MFA-protected operator access and audited recovery.
- 🟧 **Approved conditionally:** D8b Web Admin BFF (HttpOnly/Secure/SameSite=Strict
  cookies, server-side authz/CSRF, no bearer tokens in browser JS) — **must first
  prove the 0 CZK fit** (task 6.0f) or report the blocker. *Feasibility study
  executed 2026-10-21 → CONDITIONAL PASS
  ([stage-6/BFF-FEASIBILITY.md](stage-6/BFF-FEASIBILITY.md)); **C1 APPROVED
  2026-10-21** (Cloudflare Workers Free host, architecture-level only — no
  provisioning); **C2 EXECUTED 2026-10-21 → CONDITIONAL PASS** (isolated local
  `workerd` spike on `spike/goldberg-0.11-c2-bff`, final commit `7bc6976`:
  19/19 security, P1–P7 executed, kernel CPU **2.03 ms/invocation**;
  per-invocation production CPU unverified, §12 production-runtime
  verification owner-gated); **C3 documentary review COMPLETE 2026-10-21**
  ([stage-6/C3-EDGE-PRIVACY-REVIEW.md](stage-6/C3-EDGE-PRIVACY-REVIEW.md):
  conditional privacy viability at 0 CZK, no compliance claim, legal/operator
  questions open). **C4 remains open** — its pre-provisioning checklist was
  prepared 2026-10-21
  ([stage-6/C4-PREPROVISIONING-CHECKLIST.md](stage-6/C4-PREPROVISIONING-CHECKLIST.md),
  preparation ≠ final verification).*
- 🟨 **Provisional / deferred:** D5a personal contact email is the intended public
  contact but **no address was supplied or authorized — nothing may be published**;
  D5b controller/operator determination deferred to pre-launch review — **research
  deliverable [stage-6/Q1-OPERATOR-READINESS.md](stage-6/Q1-OPERATOR-READINESS.md)
  prepared 2026-10-21; still legally unresolved and blocking for 6.0d**; D5d
  retention/deletion principle approved with **details unresolved**; D5e licensing
  approved subject to pre-launch legal review.

**Separately gated (never authorized by the decision review):** implementation start,
provisioning (6.0d), Google OAuth console setup (6.0e), and public launch/collection
(pre-collection gate, OPEN-DECISIONS.md §5). No provisioning, no OAuth configuration,
no client-side speculation, no paid service has occurred.

**Cloud-readiness reconciliation (2026-10-21, later session, documentation-only):**
the owner chose the approved **cloud architecture as the intended deployment
path** (Cloudflare Workers Free + Supabase Free `eu-central-1`, strict 0 CZK)
**instead of the temporary local-only Option C** — Option C is now only the
current gated posture, not the intended path; **no gate changed**. The earlier
"owner is a minor" premise is **reclassified as an unverified assumption, not a
verified current fact** (no age/identity/family detail may be requested or
recorded); **D5b stays legally unresolved and blocking for 6.0d**, and account
registration would not establish compliance. The apparent **6.0f ↔
provisioning gate-ordering conflict** (C2 §12 production measurements need a
deployed Worker; provisioning is owner-gated on C4 + D5b) is analyzed in
[stage-6/CLOUD-READINESS-PLAN.md](stage-6/CLOUD-READINESS-PLAN.md) with an
explicit minimal sequence (documentary verification → contractual readiness →
owner-authorized *limited private verification* provisioning → day-of C4 →
isolated synthetic-data production-runtime C2 verification → 6.0f accept/
reject → separate implementation authorization → later launch gates) and
alternatives **ORD-A/B/C left pending as an owner decision**.

**ORD-A APPROVED 2026-10-21 (sequencing only, documentation-only session):**
the owner selected **ORD-A — Scoped Private Cloud Verification Provisioning**
as the sequencing strategy for closing 6.0f (**ORD-B/ORD-C not selected**),
recorded with the approved 11-step sequence, the reconciled gate table (C1
approved · C2 conditional pass · C2 §12 open · C3 complete · C4 open · Q1/D5b
open · ORD-A approved (sequencing only) · 6.0f unchecked · 6.0d/6.0e not
authorized · Stage 6 implementation not started) and the **ORD-A readiness
checklist** for the future owner decision in
[stage-6/CLOUD-READINESS-PLAN.md](stage-6/CLOUD-READINESS-PLAN.md) §§3–5. **ORD-A
approves the ordering of future steps only** — it is not acceptance of 6.0f,
not a D5b resolution, and not authorization to create accounts, provision,
deploy, configure OAuth or implement. **6.0f stays unchecked; Stage 6
implementation stays NOT STARTED; nothing provisioned.**

**Q1 natural-person operator reassessment 2026-10-21 (documentation-only,
current-state; supersedes only earlier Q1 framing, all gates unchanged):**
[stage-6/Q1-OPERATOR-READINESS.md](stage-6/Q1-OPERATOR-READINESS.md) was
reassessed after fresh source checks (Cloudflare SSSA, Supabase ToS at its new
canonical `https://supabase.com/terms` URL, Czech Civil Code verze 18 eff.
2026-01-01, Trade Act 455/1991, GDPR Art. 2/4/8 wording from an unofficial
mirror — official EUR-Lex retrieval still failing). Its option set now covers
**four frames — Q1-A (adult/guardian of record, minor as technical operator),
Q1-B (legal entity of record), Q1-C (deferred provisioning, still the current
posture), candidate Q1-D (legally capable natural-person operator acting in
their own name)** — mutually exclusive ownership/governance frames for one
future accountable GDPR controller, compared in a full A/B/C/D decision matrix
(§5.5) with a technical (non-legal) recommendation to evaluate D first after
professional review (§5.6). **Legal capacity is treated solely as an
eligibility precondition for the eventual accountable party — never a personal
fact to be requested, recorded or inferred** (PA-2 scope). The
free/non-commercial product direction is recorded as PD-1 (Goldberg intended
to remain free of charge; commercial features may be considered in a future
version; **no monetization approved or implemented**).
**No option selected; Q1/D5b remain UNRESOLVED and blocking for 6.0d; D5b is
not resolved; no GDPR compliance claimed; professional review is not
unnecessary; 6.0f stays unchecked; no account, provisioning or implementation.**
A professional-review **routing package** was added 2026-10-22:
[stage-6/Q1-PROFESSIONAL-REVIEW-BRIEF.md](stage-6/Q1-PROFESSIONAL-REVIEW-BRIEF.md)
— the identity-free handoff carrying all 13 questions Q4.1–Q4.13 verbatim with
primary sources, the neutral A/B/C/D framing (Q1-D = candidate for review, **not
approved**), official ÚOOÚ + European Commission guidance (accessed 2026-10-22)
and the C-1…C-6 confirmations required before D5b can be resolved. **The brief
performs no review and changes no gate: Q1/D5b stay UNRESOLVED, C4 OPEN, 6.0f
UNCHECKED, ORD-A sequencing-only.**

- **Purpose:** Optional, secured community backend: server-validated Google auth,
  default-deny five-role authorization, revisioned submissions with immutable
  evidence, one-transaction moderation decisions with audit trail, versioned `/v1`
  API shared by future Android/Web Admin clients — under the permanent 0 CZK
  contract, with zero practice-journal data ever crossing the boundary.
- **Scope (planned, not implemented):** backend server code + database (Supabase
  Free per D4a), Android community repository/token storage (optional feature),
  export/restore tooling. **No** journal schema/backup/signing change; no Stage 7 UI;
  no bundle changes.

**Tasks — see [stage-6/CHECKLIST.md](stage-6/CHECKLIST.md) for full definitions
(6.0a–6.0f decisions/provisioning; 6.1–6.9 server core; 6.10–6.11 Android
integration; 6.12–6.16 verification gates; 6.17–6.18 handoff/closure). All
implementation tasks UNCHECKED.**

- [x] 6.0a D4/D4b — decided 2026-10-21 (Supabase Free; graceful denial; fail closed;
      strict 0 CZK).
- [ ] 6.0b D5a–D5g — partially decided; D5a details, D5b determination, D5d details
      and D5e/D5f pre-launch reviews remain open.
- [x] 6.0c D8a–D8c — decided 2026-10-21 (D8b conditional on the 6.0f proof gate).
- [ ] 6.0d Provisioning (separate owner instruction + D5c verification list).
- [ ] 6.0e Google OAuth console configuration (separately authorized).
- [ ] 6.0f D8b zero-cost BFF hosting/runtime proof (or blocker report to owner) —
      🚧 **feasibility study executed 2026-10-21 → CONDITIONAL PASS**
      ([stage-6/BFF-FEASIBILITY.md](stage-6/BFF-FEASIBILITY.md)); **C1 APPROVED
      2026-10-21** (Cloudflare Workers Free host, single origin, architecture-level
      only — no provisioning); **C2 EXECUTED 2026-10-21 (separate owner
      authorization) → CONDITIONAL PASS** — isolated local `workerd` spike on
      `spike/goldberg-0.11-c2-bff` (final commit `7bc6976`): **19/19 security,
      P1–P7 executed, kernel-measured average CPU 2.03 ms/invocation**;
      per-invocation production CPU **unverified** and the §12
      production-runtime verification remains owner-gated; spike code stays
      isolated (unmerged); **C3 DOCUMENTARY REVIEW COMPLETE 2026-10-21**
      ([stage-6/C3-EDGE-PRIVACY-REVIEW.md](stage-6/C3-EDGE-PRIVACY-REVIEW.md)):
      conditional privacy viability at 0 CZK, no compliance claim, legal/
      operator questions open (controller identity incl. minor-owner capacity
      now blocking for 6.0d; **Q1 research deliverable prepared 2026-10-21**
      ([stage-6/Q1-OPERATOR-READINESS.md](stage-6/Q1-OPERATOR-READINESS.md) —
      Options A/B/C documented, **no option selected, D5b still legally
      unresolved**; reassessed later 2026-10-21 → **options A/B/C/D with
      candidate Q1-D added, still no selection**, see the Stage 6 Q1
      reassessment note above; **owner preference recorded 2026-10-22: Q1-D
      is the PREFERRED operator model, subject to contractual eligibility +
      professional verification — a directional preference only, NOT a
      selection or resolution; D5b stays legally unresolved and blocking**).
      **Still open: C2 §12 production verification and
      C4 terms re-verification (checklist prepared; final run required before
      any provisioning).**
- [ ] 6.1–6.9 Server core: schema, auth, authorization, submissions, decision
      transaction, idempotency, read overlay, admin endpoints, abuse controls.
- [ ] 6.10–6.11 Android optional integration with strict personal-data isolation and
      graceful offline/unavailable behavior.
- [ ] 6.12–6.16 Adversarial matrix (local + separately approved integration rows),
      export/restore drill, privacy/terms published before any collection.
- [ ] 6.17–6.18 Frozen contract + Stage 7 handoff; tracking sync with evidence.

**Verification:** none executed — nothing is claimed. Planning-document consistency
checks (links, contract reuse, decision-reference synchronization) are recorded in
the Stage 6 documentation session report, not as implementation evidence.

**C6 spike addendum (2026-10-22, isolated local prototype — NOT implementation):**
an owner-authorized, local-only verification prototype ran on
`spike/goldberg-0.11-stage-6-contract` (never merged; planning baseline
`c22adb1`): `spikes/contract-c6/` simulates the `/v1` contract in-process
(synthetic identities, fake sessions, fake clock, disposable SQLite, zero npm
deps, zero network) and executed THREAT-MODEL [L] rows T5–T18/T22 including a
10-trial multi-process approval race — **79 tests / 79 pass / 0 fail** (run
`2026-10-22T06:40Z`, Node v22.23.1; first run 69/79 with 10 root-caused,
fixed and preserved failures). Evidence — stable links at the verified spike
commit, because the executable `spikes/contract-c6/` directory exists only on
`spike/goldberg-0.11-stage-6-contract` and is deliberately absent here:
[results/SUMMARY.md](https://github.com/stokonn/Goldberg/blob/d2a95d4b83c634aacb4f7f74432f2bd8980cf218/spikes/contract-c6/results/SUMMARY.md),
[results/raw/tap.txt](https://github.com/stokonn/Goldberg/blob/d2a95d4b83c634aacb4f7f74432f2bd8980cf218/spikes/contract-c6/results/raw/tap.txt),
[README (scope/blockers)](https://github.com/stokonn/Goldberg/blob/d2a95d4b83c634aacb4f7f74432f2bd8980cf218/spikes/contract-c6/README.md)
— all at commit [`d2a95d4`](https://github.com/stokonn/Goldberg/tree/d2a95d4b83c634aacb4f7f74432f2bd8980cf218/spikes/contract-c6).
This is **contract-behavior evidence only**: no
integration/production readiness claimed, HTTP/token layer covered by C2 (not
re-tested), real Postgres isolation and all [I]-venue rows NOT executed
(provisioning gate unchanged), CPU not re-measured (C2). **This addendum closes
no task: 6.1–6.18 remain unchecked and 6.0f remains UNCHECKED; D5b/Q1
unresolved, C4 open, 6.0d/6.0e not authorized, ORD-A sequencing-only.**

**C6-PG addendum (2026-10-22, local PostgreSQL verification — NOT implementation):**
under a follow-up owner instruction, the same C6 contract suites were executed
against a **real, isolated, LOCAL PostgreSQL 17.11** (rootless podman container,
host-loopback `127.0.0.1:54329` only, trust auth on loopback, default isolation
`read committed` = Supabase PostgREST's default, synthetic data only, per-test
throwaway schemas; container removed after evidence capture) on
`spike/goldberg-0.11-stage-6-contract-pg` (from `d2a95d4`; **never merged**),
commits `f7bef8e` (port) + `e37ed00` (evidence). Same server source, same
suites, two engines (`C6_ENGINE=sqlite|postgres`); the PG venue's only new
dependency is the `pg` wire client (spike branch only; SQLite venue stays
zero-dep). **Results: PostgreSQL 86/86 pass / 0 fail (v1–v10); SQLite
post-port 79/79 / 0 fail + 7 v10-only skips (no regression); static scan
PASS.** v9 race: SQLite 10/10 `[201,409]` vs PostgreSQL **10/10
`[201,500]`/`[500,201]`** — the T10 exactly-one-decision invariant does not
hold under READ COMMITTED. Findings F1–F5 (full detail in
[results/SUMMARY-PG.md](https://github.com/stokonn/Goldberg/blob/e37ed00b9205ce22e9e8ec494651e900708ee1e3/spikes/contract-c6/results/SUMMARY-PG.md)):
F1 isolation/lost-update with both demonstrated fixes (`SELECT … FOR UPDATE`;
SERIALIZABLE + required 40001→409 mapping) as probes only; F2 rollback parity
+ missing FK constraints; F3 idempotency parity + concurrent 23505→replay
requirement; F4 audit-chain parity + audit-seq race; F5 SQLite→PG SQL/type
differences (`BEGIN IMMEDIATE` absent, 42702 column qualification, `rowid`,
`?`, int8→string default). **Honest scope: a local instance is not Supabase —
the Supabase-hosted proof stays BLOCKED (D5b/Q1 + C4 + ORD-A steps 4–6); no
production code exists; this addendum closes no task: 6.1–6.18 remain
unchecked (F1–F5 are design inputs FOR task 6.1) and 6.0f remains UNCHECKED;
D5b/Q1 unresolved (Q1-D preference unchanged), C4 open, C2 §12 open,
6.0d/6.0e not authorized, ORD-A sequencing-only.**

**C4 preliminary-verification addendum (2026-10-22, documentation
reconciliation — C4 stays OPEN):** an owner-instructed docs-only recheck
re-fetched the official Cloudflare and Supabase pricing, terms, DPA, region
and backup sources and reconciled them with the 2026-10-21 snapshot; results
recorded as §F of
[stage-6/C4-PREPROVISIONING-CHECKLIST.md](stage-6/C4-PREPROVISIONING-CHECKLIST.md)
(the §A–§C tables and all statuses untouched). **Recorded:** both provider
accounts now exist (owner-side; Cloudflare corroborated by read-only MCP
checkpoint #1, Supabase from the owner's statement only — never agent-verified);
Cloudflare MCP read-only auth verified; **Workers Free remains available**;
**Supabase Free remains available** with the snapshotted limits; **`eu-central-1`
still a documented specific region**; **automatic backups "Not included" on
Free** (official `db dump` self-export guidance confirmed); **Free quota
overage can trigger service restrictions** (pause / read-only / 402);
**billing and payment-method status NOT verified; subprocessor and transfer
reviews still incomplete.** Documentation-date diff: all 2026-10-21 stamps
unchanged (CF Website Terms date and Supabase terms version badge not
re-renderable — honest gaps, remain ⚠ day-of); the **CF observability-pricing
announcement (change 2026-12-01) is unchanged**, and its target page (upd.
2026-10-15, new source) now shows **Free = 0.5 GB/day ingestion + 7-day
retention with no paid overage available on Free** — preliminary 0-CZK read
only, to be re-verified day-of and again for any access ≥ 2026-12-01. Q1-D
recorded as the owner's preferred structure, subject to eligibility
verification (preference only, not a selection). **This addendum closes no
item: §D steps 1–9 unchecked, C4 stays OPEN, 6.0f UNCHECKED, 6.1–6.18
unchecked, D5b/Q1 unresolved, C2 §12 open, 6.0d/6.0e not authorized, ORD-A
sequencing-only, 0 CZK/month.**

**C6-PG hardening addendum (2026-10-22, experimental mitigation comparison —
NOT implementation, never merged):** the F1/F3/F4 candidate fixes were
implemented **in the isolated spike only** and compared on the same local
PostgreSQL 17.11 instance (`read committed`), same branch
`spike/goldberg-0.11-stage-6-contract-pg`, commits `d8e82d5` (code) +
`c21e550` (evidence); the faithful sources and the original `results/pg/`
deviation evidence remain byte-untouched. `src/harden.mjs` implements both
strategies over the identical `runDecision` step sequence: **`for-update`**
(submission-row lock + `audit_head` append mutex) and **`serializable`** (SSI +
faithful audit pattern + bounded retry with explicit 40001/40P01/23505
handling; exhaustion → 409, never 500), sharing the F3 hardening (concurrent
idempotency 23505 → serve stored replay; 422 on mismatch). **Executed results
(v11, 37 tests, 7/7):** the repeated 20-trial multi-process race restored
`[201,409]` in **20/20 trials per strategy** (the exact invariant the faithful
server broke 10/10 in v9); **for-update: 0 SQLSTATE retries everywhere**,
including a 20-decision F4 scale run with consecutive audit seqs and a
coherent chain; **serializable: 21× `40001`** across the race and **218×
`40001`** at 20-way scale (maxAttempts=64), **0 exhausted**; deterministic F3
store race verified. Full executor: SQLite regression **93 tests / 79 pass /
0 fail / 14 PG-only skips**; PostgreSQL venue **123/123 pass / 0 fail**; static
scan PASS (23 files). **Conclusion for task 6.1: `SELECT … FOR UPDATE` is the
recommended pattern** (deterministic, zero retries, loser's 409 via the normal
guard); serializable works but needs a generous bounded retry and 40001→409
mapping; concurrent same-key decision duplicates answer `[201,409]` while
sequential ones replay — to be stated in the production API contract. **This
addendum closes no task: experimental evidence only, not a Supabase run
(Supabase-hosted proof stays BLOCKED), production implementation remains gated
on 6.0f UNCHECKED; 6.1–6.18 remain unchecked (this is design input FOR 6.1);
D5b/Q1 unresolved, C4 open, C2 §12 open, 6.0d/6.0e not authorized, ORD-A
sequencing-only, 0 CZK/month.**

**C4 owner-observation addendum (2026-10-22, attributed owner console facts —
C4 stays OPEN):** three first-hand owner observations were recorded as §F.5
(N-11–N-13) of
[stage-6/C4-PREPROVISIONING-CHECKLIST.md](stage-6/C4-PREPROVISIONING-CHECKLIST.md),
never agent-verified, with **no screenshots or account identifiers stored in
GitHub**: **Cloudflare Workers Free active with no payment method attached**
(billing non-verification N-8 unchanged); a **Supabase "Personal Free"
organization with zero projects** (no project created; the 2-project Free cap
not consumed; existing ≠ billing evidence); **Frankfurt (`eu-central-1`)
selectable in the Supabase project-creation form** (informs SB-P7/§D step 6
but does not close it). Supabase billing/payment status remains unverified;
project creation remains unperformed and unauthorized. **This addendum closes
no item: C4 OPEN (§D steps 1–9 unchecked), 6.0f UNCHECKED, 6.1–6.18
unchecked, D5b/Q1 unresolved, C2 §12 open, 6.0d/6.0e not authorized, ORD-A
sequencing-only, 0 CZK/month.**

**Stage 6 continuation (2026-10-22, documentary readiness only):** purpose,
scope, session tasks, current progress, verification and remaining blockers in
[stage-6/READINESS-CONTINUATION.md](stage-6/READINESS-CONTINUATION.md).
Approved interrupted Issue #31 description sync applied and exact readback passed
(b3edc2a, c21e550, checkpoint 6348121556); C4 PDF now parsed, TIA not retrieved,
official GDPR still unavailable. Future F1–F5 requirements prepared in
[stage-6/POSTGRESQL-DESIGN-REQUIREMENTS.md](stage-6/POSTGRESQL-DESIGN-REQUIREMENTS.md).
No new spike execution. **6.0f and 6.1–6.18 remain unchecked; Q1/D5b blocking;
C4 and C2 §12 OPEN; 6.0d/6.0e NOT AUTHORIZED; 0 CZK/month.**
[Continuation checkpoint posted](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6349066370); verified documentation commit/push
explicitly authorized in the finalization instruction. No production implementation.

**Stage 6 documentation finalization (2026-10-22):** all 12 documentation files
reviewed, five host checks re-executed PASS, source commit `6c40799` pushed to
`docs/goldberg-0.11-stage-6-readiness` with exact remote SHA verified; continuation
checkpoint 6349066370 posted. Publication evidence and the subsequently owner-approved, published and verified
Issue dashboard baseline are documented in stage-6/READINESS-CONTINUATION.md. All Stage 6
technical gates unchanged; this is documentation completion, not backend acceptance.

**Stage 6 existing-project reconciliation (2026-10-22, documentation only):**
purpose/scope: reconcile existing Supabase project in Stage 6 planning/handoff
and one C4 dashboard bullet, without cloud changes or gate acceptance. ChatGPT's
connected Supabase integration externally verified organization/project identity,
Free/tier_free, Frankfurt/eu-central-1, ACTIVE_HEALTHY, PG 17.11, 0 public tables,
0 recorded migrations and 0 Edge Functions. This coding agent did not execute
those checks; billing/payment methods and remaining settings remain UNVERIFIED.
Current evidence: [C4 §H](stage-6/C4-PREPROVISIONING-CHECKLIST.md); tasks/results:
[readiness checklist](stage-6/READINESS-CONTINUATION.md). Owner-approved C4 correction
published and exact readback passed; all 63 comments/Issue metadata/Project preserved.
Earlier zero-project observations remain dated history. Production backend NOT
IMPLEMENTED, Worker NOT DEPLOYED in verified record, Google OAuth not configured
in implementation record/hosted settings UNVERIFIED; real-user collection NOT
AUTHORIZED. **Q1/D5b, C4, C2 §12 and 6.0f OPEN; 6.0f/6.1–6.18 UNCHECKED;
6.0d/6.0e NOT AUTHORIZED; 0 CZK/month.** Documentation verification passed
(five host scripts, scope/links/attribution/unchanged-gate checks); checkpoint
prepared for authorized commit/push. No Stage 7 changes or retroactive 6.0d
authorization/completion.

**Stage 6 Security & Auth readiness audit (2026-10-22, read-only):** purpose:
assess two owner-supplied rls_auto_enable() advisor warnings in PostgreSQL event
context; scope: Stage 6 security/Auth planning and handoff only. Evidence ledger,
individual tasks/progress, official sources, conditional least-privilege proposal,
read-only Auth/RLS/API checklist and prepared catalog SELECTs are in
[stage-6/SECURITY-AUTH-READINESS-AUDIT.md](stage-6/SECURITY-AUTH-READINESS-AUDIT.md).
Source review complete; exploitability not established; hosted checklist NOT
EXECUTED. Documentary verification passed (five host checks, links/scope/original
checkbox preservation); owner-approved C4 sentence published and exact readback
verified with all comments/metadata preserved. No hosted mutation, no repeated
concurrency suite, no Stage 7 changes.
Q1/D5b, C4, C2 §12 OPEN; 6.0f/6.1–6.18 UNCHECKED; 6.0d/6.0e NOT AUTHORIZED;
0 CZK/month. No technical gate acceptance.


### External security audit continuation — 2026-10-22 (base bac81fb)

Purpose/scope: reconcile the executed, read-only A1–A3 catalog evidence in Stage 6
documentation only. ChatGPT’s connected Supabase integration executed all three
successfully; this agent records owner-relayed external results, not its own SQL.

- [x] Record function OID 17603, plpgsql/definer/postgres/event_trigger, pg_catalog search path and effective API-role EXECUTE.
- [x] Record matching ensure_rls binding/event/tags/normal enabled mode and the supplied limited API-role/privileged owner capabilities.
- [x] Record default PUBLIC EXECUTE, no identified separate API-role grants and partial structural source checks with their limits.
- [ ] Complete inherited-access/other DDL paths, full private source/dependency review and sanitized advisor IDs/version.
- [ ] Complete read-only Auth/RLS/API configuration and billing/payment evidence review.

Evidence: [SECURITY-AUTH-READINESS-AUDIT.md](stage-6/SECURITY-AUTH-READINESS-AUDIT.md)
§1/§3/§7. Both warnings remain; no exploit demonstrated/repaired, no hosted write
or function/RPC invocation. A1–A3 are complete externally; the overall hosted audit
is INCOMPLETE. Existing five host checks and documentation consistency checks PASS
for this continuation (details in docs/ai/VERIFICATION.md); no concurrency rerun.
Q1/D5b unresolved/blocking; C4 and C2 §12 OPEN; 6.0f/6.1–6.18 UNCHECKED;
6.0d/6.0e NOT AUTHORIZED; ORD-A sequencing only; 0 CZK/month; collection NOT
AUTHORIZED. Keep ensure_rls; any permissions change requires separate authorization
and compatibility review. No Stage 7 or application changes; no gate acceptance.


### Additional external security inventories — 2026-10-22 (base 9fe83db)

Purpose/scope: reconcile four executed catalog-only findings in Stage 6 audit and
handoff documentation. ChatGPT executed these against the existing Supabase
project; the owner relayed the results. This agent did not execute hosted queries.

- [x] Record public function inventory: only rls_auto_enable().
- [x] Record direct anon/authenticated memberships: none returned.
- [x] Record function dependency count: 1, without assuming identity/type/direction.
- [x] Record public tables/views/materialized views/foreign tables: zero objects.
- [ ] Finish full private source/dependency and remaining cross-schema/DDL-path review; obtain sanitized advisor IDs/version.
- [ ] Finish read-only Auth/API/RLS configuration and billing/payment/C4 evidence review.

Progress/evidence: [security audit §1/§3/§8](stage-6/SECURITY-AUTH-READINESS-AUDIT.md).
Only these scoped inventory portions are complete; broader hosted audit remains
INCOMPLETE. Neither warning is resolved; no vulnerability demonstrated/repaired,
no database writes or application-function invocation. Do not repeat A1–A3 or the
four completed inventories. Issue #31’s concise broader status remains accurate;
no edit/comment is needed. Documentation verification is recorded in docs/ai/VERIFICATION.md.

Q1/D5b unresolved/blocking; C4 and C2 §12 OPEN; 6.0f/6.1–6.18 UNCHECKED;
6.0d/6.0e NOT AUTHORIZED; ORD-A sequencing only; 0 CZK/month; collection NOT
AUTHORIZED. Keep ensure_rls; future permissions changes require separate approval
and compatibility review. No Stage 7 or application changes; no gate acceptance.


### Live security evidence / bounded readiness — 2026-10-22 (base 31e0539)

Purpose/scope: reconcile five new external ChatGPT/Supabase read-only findings in
Stage 6 documentation; narrow remaining work to settings evidence and approval
choices, without continuing open-ended catalog exploration.

- [x] Record normal ensure_rls → rls_auto_enable dependency and function language/schema dependencies.
- [x] Record CREATE=false for anon/authenticated across all eight supplied schemas.
- [x] Record private deployed-source retrieval and structural checks, including exception handling, with safety limits.
- [x] Record ensure_rls plus six reported Supabase-related system triggers, unchanged.
- [x] Record both exact advisor names at WARN, unresolved after external rerun.
- [x] Check accessible integration tools; no callable Supabase tools here, Auth/API settings UNVERIFIED.
- [ ] Complete one sanitized Auth/API settings pass where access permits; retain inaccessible fields as limitations for scoped owner review.

[Audit §1/§3/§9](stage-6/SECURITY-AUTH-READINESS-AUDIT.md) records exact provenance,
remaining settings and prioritized readiness verdict. Catalog reconciliation of
this snapshot is complete; the broader hosted audit/Stage 6 are NOT accepted.
No completed A1–A3/dependency/schema/inventory queries or concurrency tests rerun.
Documentation verification results are in docs/ai/VERIFICATION.md.

Q1/D5b unresolved/blocking; C4/C2 §12 OPEN; 6.0f/6.1–6.18 UNCHECKED;
6.0d/6.0e NOT AUTHORIZED; ORD-A sequencing only; 0 CZK/month; collection NOT
AUTHORIZED. Prioritize owner/professional Q1, C4 evidence, separately authorized
C2 §12/6.0f acceptance, then explicit implementation authorization. Both warnings
remain; preserve ensure_rls. No hosted/production/Stage 7 changes. Issue #31’s
concise broader status remains accurate and unchanged; no edit/comment proposed.


### Owner-authorized local schema prototype — 2026-10-22

Purpose: prove physical PostgreSQL 17 schema/migration/integrity requirements using
synthetic data on a separately named spike. Scope: spikes/stage6-schema-pg and
Stage 6 checkpoints only; no production implementation or adoption into 6.1.

- [x] Create isolated spike/goldberg-0.11-stage-6-schema-pg from verified remote 4d01489.
- [x] Implement physical DDL, FK/unique/check graph, immutable histories, audit_head, idempotency and repeatable atomic migrations.
- [x] Execute disposable PostgreSQL 17.11 targeted suite: 34/34 PASS, 0 failures/skips.
- [x] Run five existing host scripts: PASS; preserve failed intermediate evidence and original PG comparison.
- [x] Flag API revision-eligible statuses versus server transitions; no approved contract changed.
- [x] Publish implementation 16bfdfb/correction f91a4ce and [Issue checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6351050432); exact readback PASS.

[Prototype checkpoint](stage-6/LOCAL-SCHEMA-PROTOTYPE.md) links source, commands, evidence
and limitations. Verification includes empty/seeded/idempotent migrations, FKs,
immutable mutation/truncation, transactional rollback, new target-contention and
idempotency regressions, audit recomputation and int8 boundaries. No benchmark
rerun; no hosted RLS/Auth/API, full restore/RPO, app build/device or release proof.

This local milestone can later supply reviewed 6.1 inputs; it does not pass or
change 6.0f. Formal 6.1–6.18 remain UNCHECKED; Q1/D5b unresolved/blocking, C4/C2 §12
OPEN; 6.0d/6.0e NOT AUTHORIZED; 0 CZK/month; collection NOT AUTHORIZED.
Exact next step: review candidate schema, resolve status-contract ambiguity with
owner, then clear gates/obtain separate formal implementation authorization.
Stage 7A conditional/X10 OPEN and Stage 7B untouched. No merge/deploy/release.


Publication: implementation commit 16bfdfbe213ce66973a55d9746453601605e74d7;
follow-up f91a4ced846ad53a571c744c6c588f245fe3a582 corrects whitespace in saved
failed-run diagnostics and records the check failure. Tested source hashes and
34/34 results are unchanged; full baseline-to-current diff check PASS. Existing
Issue #31 checkpoint 6351050432 independently read back exactly; all 63 older
comments preserved, 64 total, Project In Progress unchanged. Later publication
metadata/checkpoint commits do not change tested prototype source.


### Reviewed prototype / formal-local adoption preparation — 2026-10-22

Purpose/scope: record owner-selected revision states, inspect dependencies and
prepare an exact local 6.1–6.3 scope/policy proposal reusing the tested prototype.
Documentation/proposals only; formal implementation remains NOT STARTED.

- [x] Publish only the owner-approved Stage 6 dashboard row and checkpoint link; exact body/metadata/comments readback PASS.
- [x] Record owner direction: draft local-only, changes_requested canonical, returned UI label only.
- [x] Inspect app, Stage 7A, prototype and old C6 dependencies; no approved API edit.
- [x] Prepare exact adoption scope/map, API patch and narrow gate-exception text for review.
- [ ] Owner approves the reviewed API patch and local-only formal 6.1–6.3 policy exception.
- [ ] After that authorization: perform extraction/adoption and targeted local Auth/AuthZ implementation/tests.

[LOCAL-CORE-ADOPTION.md](stage-6/LOCAL-CORE-ADOPTION.md) holds concrete scope, compatibility
impact, retained 34/34 evidence and acceptance requirements. No tests claimed rerun
for unchanged prototype source. Local-only exception is proposed, NOT in force;
all original technical task/gate boxes unchanged. Verification: five host scripts,
source hashes, proposal applicability and links/scope/gate checks recorded in docs/ai/VERIFICATION.md.
Q1/D5b unresolved/blocking; C4/C2 §12 OPEN; 6.0f/6.1–6.18 UNCHECKED;
6.0d/6.0e NOT AUTHORIZED; 0 CZK/month; collection NOT AUTHORIZED. Stage 7A/X10
and Stage 7B preserved. No cloud, production code, API change, merge or release.


### Local Auth/session/authorization prototype — 2026-10-22

Purpose/scope: owner-authorized isolated LOCAL-ONLY prototype for 6.2/6.3 designs,
reusing the verified PostgreSQL schema. New spikes/stage6-auth-local code, additive
Auth migration, tests and Stage 6 checkpoints; no formal promotion or cloud work.

- [x] Create spike/goldberg-0.11-stage-6-auth-local from verified schema remote 0f966dc.
- [x] Implement real JWT/JWKS validation, issuer/subject identity, hashed opaque sessions, nonce/replay/rotation/family revocation and safe logout/error/log handling.
- [x] Implement fresh server role/state/capability/ownership checks, no-self-grant and actor≠author with a local in-process adapter.
- [x] Execute 50 Auth/security tests plus unchanged 34-schema regression suite: 84 PASS, 0 failures/skips/cancellations.
- [x] Run five repository host scripts: PASS; original schema/evidence/approved contracts and gate boxes preserved.
- [x] Commit/push source 76c1c87 and append [Issue checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6351715021); show latest-link-only dashboard proposal for approval.

[Local Auth checkpoint](stage-6/LOCAL-AUTH-PROTOTYPE.md) links exact source/tests/commands
and partial T1–T9/T22 coverage. Formal 6.2/6.3 and all global task boxes remain
UNCHECKED; no hosted/real-provider/HTTP/RLS/CSRF or complete threat-row pass inferred.
No original concurrency benchmark/provider audit rerun. Remaining work: separately
authorized local 6.4–6.6 with bounded typed handlers, authenticated moderation and
idempotency; canonical revision contract patch awaits review before endpoint adoption.
Q1/D5b unresolved/blocking; C4/C2 §12 OPEN; 6.0f/6.1–6.18 UNCHECKED;
6.0d/6.0e NOT AUTHORIZED; 0 CZK/month; collection NOT AUTHORIZED. No Stage 5/7,
cloud, real credentials/users, APK, deployment, merge, tag or release changes.


Publication: tested source 76c1c879b5642236490e1f14b652fdf5e5ebdc7a pushed to the
separate Auth branch. Checkpoint 6351715021 independently read back exactly; all
64 prior full comments preserved, 65 total, Project In Progress unchanged. Dashboard
link-only proposal is prepared/shown; UNPUBLISHED pending explicit approval.
Source hashes and final 50+34 test artifacts unchanged by publication bookkeeping.

## Stage 7 — Encyclopedia UX and Android/Web Admin clients

**Status: ⬜ not started.** Depends on stable Stage 4/5 contracts and Stage 6 admin actions.

## Stage 8 — Integration, legal review and release readiness

**Status: ⬜ not started.** Only after all prior gates; requires explicit release instruction
and current mandatory verification gates.

---

created by stokonn with love <3

## Stage 6 local handlers milestone — 6.4–6.6 (2026-10-23)

Purpose: implement bounded submissions, atomic authorized moderation and request-bound
idempotency on the verified PostgreSQL/Auth prototypes, using synthetic data only.
Scope: spikes/stage6-handlers-local, reviewed revision-status contract clarification,
and Stage 6 tracking. Original 34 schema/50 Auth source and historical evidence remain intact.

- [x] Verify remote Auth HEAD 7993444 and create isolated implementation branch.
- [x] Read latest Issue #31 checkpoint and publish approved link-only dashboard update.
- [x] Review exact state patch/dependencies; reconcile changes_requested canonical state.
- [x] Implement separate validation, submission, moderation, idempotency and adapter modules.
- [x] Execute focused PG17 security/concurrency/rollback tests plus all 84 regressions.
- [x] Verify source hashes, host checks and scope; push c80c03e and synchronize checkpoint #6357457691.

Progress: local code/verification/publication complete; formal acceptance pending.
Verification: PostgreSQL 17.11 — 87 new + 50 Auth + 34 schema = 171 PASS, zero
failures/skips/cancellations; five host checks and original source hashes PASS.
Initial 77/81 and corrected 81/81 artifacts retained, including diagnosed export
ordering bug and fixture correction. No hosted/HTTP/DNS/Android tests executed.
[Purpose, scope, limits and tasks](stage-6/LOCAL-HANDLERS-PROTOTYPE.md).
Completion: local milestone VERIFIED AND PUBLISHED (c80c03e, Issue checkpoint
6357457691 independently read back, 65 older comments preserved). Formal
6.4–6.6 and 6.0f remain unchecked; Q1/D5b, C4 and C2 §12 unresolved.

## Local 6.7–6.9 candidate — PARTIAL, 2026-10-23

Purpose/scope: public overlay/feed, remaining Admin operations and PostgreSQL abuse
protection under spikes/stage6-public-admin-abuse; two narrow existing handler
extensions and additive local migration 004. Branch
implement/goldberg-0.11-stage-6-7-9-local; isolated worktree
/tmp/goldberg-stage6-public-admin-abuse; live GitHub-verified base
262847ebcdef18d186ded5b7af4825436a1dc4e3, direct child of c80c03e.
Source checkpoint is its containing implementation commit; publication follows.

- [x] Read canonical instructions, latest Issue #31 body/66 comments/checkpoint,
  verify live remote ancestry and isolate work without changing earlier branches.
- [x] Add candidate public, Admin and quota services; reuse existing core and
  role/suspension functions; preserve original 171 test files and historical evidence.
- [x] Execute 30 unit checks PASS, five repository checks PASS; single-user PG17.11
  migration SQL and scoped history/feed/quota/rollback smoke assertions PASS.
- [ ] Execute 33 new PostgreSQL integration tests and original 171 regressions.
  Actual attempts: 33 + 87/50/34 hook failures, 0 bodies executed (connect EPERM).
  Separate attempts on the unchanged original worktree fail identically.
- [ ] Verify concurrency, Admin transaction boundaries, end-to-end authorization,
  idempotency, quota windows and all integration assertions in a socket venue.
- [ ] Finish independently scheduled coarse-IP expiry cleanup/retention acceptance.
- [ ] Publish source branch and append a verified partial checkpoint to Issue #31.

Podman fails on read-only /run/user/1000/libpod. A disposable PostgreSQL17.11 cluster
initialized from cached local binaries; TCP startup is denied by the sandbox.
Single-user SQL execution succeeds but cannot prove JavaScript service or concurrent
behavior. Full source hashes, commands, failed attempts and exact threat limitations:
spikes/stage6-public-admin-abuse/results/SUMMARY.md. Original 32-source manifest:
30 unchanged; only adapter and publication revision allocation intentionally changed.
No old hash/log overwritten. Local retention currently prunes only during fresh
intake; real source/field/license policy stays unresolved/default-deny. No fetcher.
T14/T16/T17/T18/T20/T22 subchecks only; no whole threat row acceptance.

Formal 6.1–6.18/6.0f unchanged; Q1/D5b/C4/C2 §12 open. No hosted, real-user,
Android/Stage7/Stage5, main merge/tag/APK/release action. Not ready for 6.10–6.11.
Next: obtain an allowed local PostgreSQL17 socket/container venue, execute
run-local.sh and fix/verify actual results, finish retention, then acceptance.
No Issue dashboard diff proposed; description and prior comments remain untouched.

## Follow-up — network diagnosis and retention candidate (2026-10-23)

PARTIAL. Local implementation checkpoint remains
`defbf346904c9aa082a4afc8cac625ce8ea8b923` on
`implement/goldberg-0.11-stage-6-7-9-local`; no new commit or successful push.
Live GitHub matching-refs confirms implementation branch absent; commit lookup
returns HTTP422 No commit found; handler base remains 262847e. Exact-SHA ordinary
push retry fails before authentication. Local IPv4 socket creation fails EPERM;
github.com DNS returns EAI_AGAIN. No force-push or existing work overwrite.

Issue #31 comment NOT POSTED: connector403 Resource not accessible by integration;
CLI cannot connect. Fresh independent reads confirm body and all 66 comments
unchanged. Exact pending comment and publication diagnostics are retained in
spikes/stage6-public-admin-abuse/results/publication-attempts.json. No dashboard diff.

Independent retention scheduler/prune follow-up exists in the working tree:
32 unit tests PASS; PostgreSQL17.11 single-user retention smoke PASS. It runs
independently of intake with explicit lifecycle, suppresses overlapping sweeps,
drains on stop and retries storage faults without private diagnostic content.
SQL checks preserve active/principal counters and submission/audit/publication
history. Compose exposes janitor.start()/stop() for the future local runtime;
no daemon, HTTP listener, hosted scheduler or infrastructure was started.
**Follow-up is UNCOMMITTED** because its PostgreSQL transaction wiring is not
verified, respecting the owner's instruction to commit/push only verified work.

Latest new integration attempt: **34 hook failures, 0 test bodies executed**,
connection EPERM. Original 87/50/34 regression attempts remain setup failures, not
passes; prior 171 PASS is historical only. No code failure conclusion can be drawn
from blocked setup. Five host checks rerun PASS; current source hashes preserved
separately in retention-candidate-sha256.json, without overwriting earlier manifests.

Unfinished 6.7: execute public/private, quarantine, supersession/rollback, equal-time,
concurrent pagination/highwater/cursor and BIGINT integration assertions.
Unfinished 6.8: execute capability/revocation/suspension/role wrapper, reason/conflict,
Admin races/idempotency, transaction-boundary faults and audit/history assertions.
Unfinished 6.9: execute PostgreSQL quota/pending/IP last-slot races, clock windows,
idempotency/fault/kill-switch/Retry-After and independent cleanup integration.
All 171 original regression bodies must run successfully in an allowed PG17 venue.
Full HTTP/browser/hosted/Android threat acceptance unexecuted; no full threat row PASS.
Formal gates unchanged; no cloud/Supabase/Stage5/7 modification. Not ready for 6.10–6.11.
Next: restore permitted local PG17 network/container execution, run run-local.sh,
fix any actual assertion failures, then commit verified follow-up and safely publish.

## Recovery attempt — permitted host execution unavailable (2026-10-23)

PARTIAL, NOT COMPLETE. No new commit/push; local HEAD remains
`defbf346904c9aa082a4afc8cac625ce8ea8b923` on
`implement/goldberg-0.11-stage-6-7-9-local`.
Preserved all existing staged/unstaged/untracked work, original evidence and 171-test
baseline. Before changes, saved tracked.patch, working-files.tar.gz and per-file
hashes for all 24 modified/untracked files under
`/tmp/goldberg-stage679-recovery-20261023T052407Z`. No existing scheduler/service/test
source or raw result was overwritten; unrelated worktrees untouched.

Minimal fresh diagnostics: IPv4 socket creation EPERM; Unix socket creation allowed
but bind/listen/connect probe fails EPERM; github.com DNS EAI_AGAIN. Seccomp is active
and NoNewPrivs=1; exact filter rule is not inspected/claimed. Rootless Podman fails
before startup on read-only /run/user/1000/libpod. These are execution-environment
failures before application/database test assertions, not proven code defects.

Requested narrowly scoped host execution of the existing disposable-PG17 runner.
Automatic approval review rejected it before execution: granular approval policy
has sandbox_approval:false and disallows require_escalated. No permission prompt
can be presented through this tool. No bypass, alternate host, deployment or hosted
fallback attempted. Host permission therefore could not be obtained in this session.

Improved only the reproduction runner: fail-fast local TCP preflight with machine-
readable diagnostics before any test hooks/container, plus all five host checks.
`bash -n` PASS; restricted preflight correctly exits78 and executes zero DB test
bodies. Fresh direct unit run: 32 PASS,0 failed/skipped/cancelled. Fresh five host
checks: all PASS. Prior single-user PG17.11 smoke evidence retained; not rerun.
Full 34 new integration bodies and 87+50+34 original regression bodies NOT executed
in this recovery. Previous connection-hook failure records retained unchanged.
Uncommitted cleanup scheduler still awaits real PostgreSQL integration; no source
acceptance or complete threat-model claim. Remaining 6.7/6.8/6.9 integration assertions
are exactly those listed in the previous checkpoint; no newly observed code defect.

Live connected GitHub read: implementation branch still absent; Issue31 OPEN with66
comments. Remote URL remains https://github.com/stokonn/Goldberg.git. No push after
unverified tests; no new public checkpoint/body/dashboard edit. Exact pending comment
remains in results/publication-attempts.json as UNPUBLISHED. Previously observed
connector403 and CLI network errors remain publication blockers.

Exact host-side command (same worktree, existing installed pinned dependencies and
cached PG17.11 image; starts/removes only its own synthetic container):
`bash /tmp/goldberg-stage6-public-admin-abuse/spikes/stage6-public-admin-abuse/run-local.sh`
It runs units, all34 new PG tests including janitor, original87/50/34 suites and the
five checks, preserving each attempt in a fresh results/run-* directory. In a
permitted host, inspect actual assertion results and fix defects before committing
and pushing normally; verify exact remote SHA before posting Issue31 checkpoint.

No hosted/Supabase/OAuth/Android/Stage5/Stage7/merge/tag/APK/release action. Formal
6.1–6.18/6.0f gates unchanged. NOT ready for Stage6.10–6.11. Next action requires a
host execution venue permitting local sockets; this session cannot grant it.

## Local 6.7–6.9 implementation VERIFIED — Fedora host evidence, 2026-10-23

Publication is PENDING. Owner explicitly reports executing the existing runner
on Fedora in the permitted host environment. The agent independently read its
retained TAP/log files at spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/;
this is owner-executed host evidence inspected by the agent, not agent-executed
sandbox PostgreSQL tests. Supersedes the earlier PARTIAL/execution-blocked entries
below for local implementation verification only. Historical failures remain intact.

Purpose/scope: reusable public catalogue overlay/change-feed, remaining Admin
operations and abuse protection/independent cleanup in the existing in-process
adapter. Original Auth/session/idempotency/audit/queue/open/decision implementations
are reused; additive local 004 migration; no HTTP listener or deployed API.
Branch implement/goldberg-0.11-stage-6-7-9-local, based on 262847e, preserving
partial source defbf346904c9aa082a4afc8cac625ce8ea8b923 as an ancestor.
Final implementation is the containing verified source commit; publication details
will be recorded separately. Stage5/Stage7A/X10 and unrelated worktrees untouched.

- [x] Inspect host preflight (localTcp available), PostgreSQL17.11, Node22.23.1,
  raw TAP test bodies/results, all five host check logs and own-container cleanup.
- [x] New PostgreSQL integration34 + unit32 + original handlers87 + Auth50 + schema34:
  **237 PASS; 0 failed, skipped, cancelled**. No hookFailed/not-ok records.
- [x] Current service/test bytes match all11 pre-run retention-candidate hashes;
  current runner matches its pre-run hash. Both manifests predate host execution.
  Owner confirms execution from this worktree; mtimes/TAP names agree. Host runner
  did not capture an at-run source manifest: do not claim cryptographic execution
  attestation. Additive final manifest covers46 source/dependency/test/migration/
  runner files;15 retained host artifacts hashed separately.
- [x] Original171 test sources, schema/Auth sources and old migration/history files
  unchanged. Original32-source manifests:30 unchanged; existing adapter and
  publication max-revision allocation are the two intentional extensions.
- [x] Public private-data separation, published projection, quarantine on old
  overlay cursors, publication/supersession/rollback events, numeric feed highwater/
  concurrent polling/equal timestamps/more-than-nine events and int8 max sequences.
- [x] Admin capability/default-deny/reason/exact-conflict checks, self-grant denial,
  role revocation/suspension/reactivation, role replay after revocation, Admin
  same-key replay/mismatch and distinct-key entity races, immutable audit export.
  Quarantine/rollback faults after lock/projection/audit/feed and at deferred commit
  preserve exact snapshots; same-key retry succeeds without rewriting history.
- [x] Daily/pending limits, competing final principal/IP quota slots, keyed coarse
  groups, duplicate replay without recounting, IP/day windows, explicit Retry-After,
  intake kill-switch read-only behavior, quota-store outage/recovery and rollback
  of both counters/business work at faults/idempotency insertion.
- [x] Independent janitor PostgreSQL cleanup with intake disabled; only expired
  coarse hashes removed, principal counters and submission/audit/publication
  evidence preserved. Unit lifecycle covers idle tick, no overlap, stop/drain,
  storage-fault retry and minimal safe diagnostics. Explicit start/stop is a future
  local runtime responsibility, not an installed daemon or hosted schedule.
- [ ] Push verified source normally and verify exact remote branch SHA.
- [ ] Append the factual verified checkpoint to existing Issue31 after remote
  verification; preserve all prior66 comments and unchanged dashboard.

Exact coverage/limitations: spikes/stage6-public-admin-abuse/results/SUMMARY.md,
verified-host-results.json, verified-host-source-sha256.json and
verified-host-artifacts-sha256.json. Source review found no genuine defect requiring
changes after this green host run; verified implementation/tests/runner unchanged.
Local T14/T16/T17/T18/T20/T22 subchecks only, no entire threat row accepted.
T14 remains no-fetch/URL deny-policy; DNS/redirect/rebinding retrieval unexecuted.
No HTTP/browser/hosted RLS/real OAuth/Android/offline/restore-RPO venue tested. Public
output supports reviewed plain-text display metadata and license/count summaries;
real source/field/license compatibility stays unresolved and default-deny. No real
users/credentials/IP diagnostics. Synthetic windows are not legal retention periods.
Number-based legacy writes still reject unsafe JSON integers; public/new Admin
int8 values use exact decimal strings. No production load/SLA claim. Event and
Admin expectedEventSeq fixtures are local extensions; client freeze remains later.

Formal6.1–6.18/6.0f and Q1/D5b/C4/C2 §12 remain unchanged; no hosted/cloud/Supabase/
release/merge/tag/APK action. Local server milestone is ready as the basis for a
separately authorized6.10–6.11 milestone; Android work has NOT started and is NOT
implicitly authorized. Remaining work in this milestone is GitHub publication,
not another general planning/security pass or a repeat of the host-green suites.

## Android 6.10–6.11 partial source checkpoint — 2026-10-23

Branch implement/goldberg-0.11-stage-6-10-11-local; isolated worktree
/tmp/goldberg-stage6-android-local, remote-verified backend base9d7150c.
Implementation commit is the containing scoped source commit. Backend237 PASS and
published Issue31 checkpoint6358952579 verified;67 old comments preserved.
Purpose: local-first Android community repository, contribution DTOs and safe
unavailable/error/retry behavior. Scope: data/community, lazy OpusApplication
composition, small Settings subpage, EN/CZ strings, JVM/native tests and host scripts.

- [x] Typed strict privacy-isolated request DTOs, public overlay/feed DTOs and
  disabled production transport, deterministic synthetic-only fixture transport.
- [x] File-backed local drafts with stable IDs/account namespaces, atomic saves,
  optimistic conflicts, incomplete editing, exact pending request/key persistence,
  explicit reviewed copy for new logical work, no background publishing.
- [x] Distinct400/401/403/404/409/422/429/503/offline/server/storage states, retry
  windows, uncertain outcome/receipt-save/cancellation safety and stale cache labels.
- [x] Candidate AndroidKeyStore AES-GCM credential adapter, no-backup location,
  local credential clearing; Google profile identity remains presentation-only.
- [x] Local draft Settings subpage using existing navigation/theme/components and
 29 localized strings; actual remote-submit control disabled, no fixture success UI.
- [x] Direct Kotlin/JVM **40 PASS,0 failures**; credential class compiles against
  SDK36. Separate JVM process recreates persisted draft store. Five host checks PASS,
 951 EN/CZ resources; host Room1→10..9→10 and backup fixtures PASS.
- [ ] Full Gradle JVM/lint/build/androidTest compilation: blocked before tasks by
  FileLockContentionHandler wildcard-IP failure. Initial raw log retained.
- [ ]5 new Android runtime tests, Room/backup instrumentation, emulator startup/
  process-death/upgrade and actual contributor screenshots. No physical phone used.
- [ ] Push verified partial source/checkpoint, then obtain permitted host execution.

Narrow host run request was AUTOMATICALLY REJECTED: sandbox_approval:false prevents
require_escalated and cannot display an approval prompt. No bypass attempted.
This is PARTIAL Android implementation, not formal6.10/6.11 completion. No actual
network privacy assertion, real service auth, hosted or UI runtime claim. Initial
new unit fixture error31/32 (Integer-vs-Long assertion) fixed; final40 PASS. Original
regression sources unchanged; full app regressions remain unexecuted.

No Room migration: schema10/backup v5/practice/profile/Google identity and backend
source untouched. Stage5/Stage7A/Stage7B branches/worktrees preserved. Backend
baseline predates six later Stage5 fixes; they remain on32cf5d9 for future integration.
No real endpoint or OAuth, upload/deploy/release/tag/main merge/subsequent-stage work.
Source/evidence/limits: stage-6/results/android-local/SUMMARY.md and source manifest.
Next: run scripts/verify-community-android-local.sh on a permitted host; inspect/fix
actual build/test results, then explicitly targeted disposable-emulator checks.
Only after acceptance recommend separate6.12–6.14 verification; do not start it.

## Android partial source saved — publication and acceptance pending

Implementation candidate commit6e4a627eab72c5c2056151c122556d5442417d91 on
implement/goldberg-0.11-stage-6-10-11-local, descendant of verified backend9d7150c.
Direct Kotlin/JUnit40 PASS and five host checks PASS; final captured source run is
stage-6/results/android-local/jvm-ofm366xf. Core/credential Kotlin was compiled;
full Android UI/build/lint and5 runtime tests remain unexecuted. Formal6.10/6.11
are unchecked and this local Android milestone is PARTIAL.

Git shell read failed resolving github.com; connected GitHub confirms the new
branch is absent. No unverified Android candidate was pushed. Issue31 remains at67
comments; no new checkpoint/dashboard edit. Prepared host verification is
scripts/verify-community-android-local.sh; optional EMULATOR_SERIAL must explicitly
name a disposable confirmed emulator. Host publication instructions and an exact
partial checkpoint are saved under ignored .gradle/community-publication/; publish
only after appropriate host verification and a normal exact-SHA remote readback.

The source commit retained raw log trailing blank lines. A scoped .gitattributes
rule disables blank-at-eof checks only for .log artifacts, preserving their raw
bytes; source/document whitespace checks remain enabled. No test failure was hidden.
Host execution approval was automatically rejected because sandbox_approval:false
prevents escalation prompts. No bypass, real network API, deployment, real account,
physical phone, main merge or release action. Next action is permitted-host Android
verification of this source, followed by emulator acceptance; not a new milestone.

## Android host Gradle verification confirmed — 2026-10-23

**JVM/build/lint VERIFIED; Android runtime acceptance still PENDING.**
Branch implement/goldberg-0.11-stage-6-10-11-local; owner-tested HEAD
1277bedf5056e794a94566e828ffecb4b14338a3 (implementation6e4a627). Before this
recording pass, working tree was clean and all15 recorded source hashes matched.
Owner executed on permitted Fedora host:
`./gradlew --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug --stacktrace`
and reported BUILD SUCCESSFUL. Agent inspected the actual generated outputs;
original console transcript was not supplied, so no compiler-warning count is
invented. No application/test source change was needed after this green run.

- [x]31 actual JUnit XML suites: **214 PASS,0 failures/errors/skips**; includes
 40 CommunityRepositoryTest tests and174 existing regressions. These40 are included
 in214, not an additional execution total. Full app/contributor UI Kotlin compiled.
- [x] LintDebug: **0 errors/fatal,109 warnings**. Category counts and raw XML/TXT/HTML
 retained. No warnings located in new data/community or CommunityDraftPanel files;
 SettingsScreen has an existing UseKtx suggestion on an unchanged expression.
- [x] Debug APK assembled, agent signature/badging inspected. ApplicationId
 com.opus.piano, inherited version0.10.1/code11, min26/target36. Trusted signing
 SHA25653eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e matches.
 APK SHA2568e30d783d223b7fc9b48c780edf6a3f5f24624aa016a77c3312d6f4149a0751d;
 preserved privately under .gradle/community-host-artifacts/1277bed/app-debug.apk.
 APK is not committed, uploaded or released.
- [x] Packaged manifest and data-extraction XML independently inspected: allowBackup
 false/fullBackupContent false; all cloud/device-transfer domains excluded. This is
 structural policy proof, not an executed Android restore/Keystore test.
- [x] Coverage mapped to exact executed XML test names for privacy/DTOs, file drafts,
 independent JVM restart, account isolation, error/retry/conflict/rate limits, cache,
 disabled transport and source-level backup/startup assertions. No network-path
 privacy assertion or Android process-death inference from JVM tests.
- [x] Direct standalone compilation of CommunityLocalInstrumentedTest.kt against
 the host-built app classes, SDK36 and cached AndroidX PASS. **0 Android test bodies
 executed**; no Gradle androidTest APK packaging claim.
- [x] Agent reran five repository checks: all PASS, including host DDL migrations
 1→10..9→10 and portable backup fixtures. No Room/personal/backup source change.
- [ ] Gradle :app:assembleDebugAndroidTest, packaging/install of test APK,5 new
 Android Keystore/tamper/backup-flag/filesystem/disabled-boundary tests.
- [ ] Existing Room/backup native migration and seeded upgrade verification;
 actual Android process death/recreation, cold startup, contributor UI/theme/font/
 CZ/EN screenshots and user-visible draft/error states. No physical phone use.
- [ ] Normal branch push/exact remote-SHA verification and Issue31 checkpoint.

Evidence copied before build outputs can be overwritten:
stage-6/results/android-local/host-confirmed-1277bed/ contains31 JUnit XML files,
raw lint reports, coverage.json, verification.json, source/artifact hashes and
APK/policy/direct-instrumentation-compile checks. Source identity relies on owner's
exact tested-revision report, initially clean tree and matching recorded hashes;
manual host command did not capture a contemporaneous source manifest. No claim
of signed execution attestation. Earlier blocked runs/failure logs remain intact.

Runtime venue: fresh executor loopback-bind probe returns EPERM; no active emulator
observed. Stage7B checkpoint6359455362 separately reports host qemu SIGSEGV. No
repeat crashing launch, AVD wipe, phone use or sandbox bypass. Existing granular
policy forbids host escalation prompts. A functioning permitted emulator venue is
required; shared Stage5/7 emulator state/worktrees must remain preserved.

All formal6.10/6.11/cloud/release gates remain unchecked; overall milestone remains
PARTIAL pending runtime acceptance. No real OAuth/endpoint/production upload,
Supabase/Cloudflare change, main merge, release or6.12–6.14 start. Next action:
compile test APK on permitted host, then execute scoped tests and native migration/
upgrade/UI acceptance on a named disposable emulator; publish this factual partial
checkpoint after normal source push and exact remote readback.

## OWNER STATUS CORRECTION — packaging and source publication confirmed (2026-10-23)

**The implementation is PUBLISHED. JVM/build/lint and instrumentation APK packaging
PASS. Android runtime/UI acceptance remains PENDING.** This entry supersedes all
older statements that packaging or source publication are blocked/unpublished.
Historical checkpoints below are preserved as point-in-time evidence.

Branch implement/goldberg-0.11-stage-6-10-11-local; published implementation HEAD
2f1524e4083a98a64d5fa9790f3cdc002d1f8237. Agent independently read the exact remote
branch HEAD and published Issue #31 checkpoint 6362788710:
https://github.com/stokonn/Goldberg/issues/31#issuecomment-6362788710.
Owner's guarded publisher record confirms preservation of all 70 earlier comments
and the Issue description at publication. Fresh connected read saw 72 comments;
no checkpoint was reposted or edited in this correction, and no build/publication
command was repeated. New documentation correction remains distinct from published
application source; application/test/build files have not changed.

- [x] Prior confirmed Fedora host JVM 214 PASS, 0 failures/errors/skips; debug APK and
 lint BUILD SUCCESSFUL (lint 0 errors / 109 warnings); results remain valid, not rerun.
- [x] Owner executed `./gradlew --no-daemon :app:assembleDebugAndroidTest --stacktrace`:
 BUILD SUCCESSFUL; 51 tasks (30 executed, 21 up-to-date). Task counts are owner-reported;
 original console transcript not supplied. Full instrumentation APK packaging PASS.
- [x] Agent inspected the actual packaged test APK/metadata/manifest/signature:
 com.opus.piano.test targets com.opus.piano using AndroidJUnitRunner, min 26 / target 36,
 trusted signing certificate matches. 991419 bytes; SHA256
 519dcf93d767e9a8fa5fade656f3361fe0f5d332e6e833603d8c66e75c60305e.
 Preserved privately at .gradle/community-host-artifacts/2f1524e/app-debug-androidTest.apk.
 All 15 recorded source hashes match. No APK upload/release.
- [x] Source branch publication and existing checkpoint read back independently.
 No duplicate Issue/checkpoint, dashboard change or repeat publisher execution.
- [x] Narrow non-destructive recovery/verification procedure prepared:
 stage-6/LOCAL-ANDROID-EMULATOR-RECOVERY.md. Preparation only; no VM was launched.
- [ ] Instrumentation execution: 0 Android test bodies; 5 new tests remain pending.
- [ ] Android Keystore/backup runtime, native Room/backup migration/seeded upgrade,
 actual app process death/recreation/cold startup and contributor UI/theme/font/
 CZ/EN screenshots/acceptance remain pending. Overall 6.10–6.11 is NOT fully accepted.

Owner diagnostics (not newly agent-executed VM tests): Fedora 44, Emulator 37.2.12,
KVM available/usable; AVDs stage5-api36 and Medium_Phone_API_37.0; no ADB-connected
device. Repeated SIGSEGV in qemu-system-x86_64-headless, command included
-gpu swiftshader_indirect; last inspected core PID 1321999. **Root cause UNCONFIRMED.**
Official graphics guidance marks this GPU mode deprecated; that is not causal proof.
Procedure retains existing diagnostic evidence and permits at most one explicit,
fresh isolated API36 desktop trial with a documented renderer, existing cached image,
separate AVD registry/data, bounded boot observation and stop-on-crash rules.
No original AVD boot/wipe/config edit, global ADB reset, SDK upgrade, permission/
SELinux change, physical-phone use or repeated crashing launch. If the trial fails,
stop and obtain stack-based diagnosis or a functioning separately selected venue.

New packaging/publication evidence and owner-attribution details:
stage-6/results/android-local/owner-status-correction-20261023/.
Existing application source, Stage 5/7 worktrees and all user/history data untouched.
No code defect/root-cause fix or runtime success is inferred. Formal 6.10/6.11/cloud/
release gates remain unchecked. No 6.12–6.14 start, merge, deployment, hosted change
or release. Exact next action: review the prepared procedure on a permitted host;
no rebuild or republishing of the already successful implementation is needed.

## Stage 6.12 — local security verification (2026-10-23; PARTIAL)

Purpose/scope: audit T1–T22 at assertion level and add focused local synthetic
regressions; verification/evidence only. No service, API, SQL or Android behavior
changes. Branch `verify/goldberg-0.11-stage-6-12-local`, based on `bcc7796`;
implementation ancestors: backend `9d7150c5b3f20880fe4d587d9c3bfd65a0f612ce`,
Android `2f1524e4083a98a64d5fa9790f3cdc002d1f8237`, both remote SHAs read back.
This owner authorization supersedes prior instructions deferring *local* 6.12;
6.13–6.18 and cloud/real OAuth/collection/merge/tag/release remain unauthorized.

- [x] Recover instructions, latest Issue #31 checkpoints and isolated worktree.
- [x] Inspect actual assertions and index exact successful test names/files/logs.
- [x] T1–T22 matrix: **13 local PASS, 6 PARTIAL, 2 NOT TESTED, 1 FAIL**. Local PASS
  can use explicit historical unchanged-source execution; no full [I] acceptance.
- [x] Fresh final run: **27 focused +32 existing unit =59 PASS**, zero failures,
  skips or cancellations; five repository checks PASS. Initial27-test log retained
  separately and not added to59. Pre/post runner source hashes unchanged.
- [x] Independently verify historical backend46 source +15 artifact hashes and
  Android15 source hashes. Retain host237 and JVM214 baselines without relabeling
  them as fresh reruns. All historical/failure evidence preserved.
- [x] Record T22 exact-status finding: unknown audit-edit operation returns400,
  threat model expects403. No mutation dispatch or privilege bypass demonstrated.
  Passing diagnostic probe records400; it does NOT pass the403 acceptance gate.
- [ ] Fresh PostgreSQL rerun: loopback socket preflight EPERM, runner exit78;
  **zero database test bodies**, not connection failures counted as executed tests.
- [ ] T13 rendering, T14 DNS/redirect/rebinding/fetch protections, T15 HTTP/browser,
  T19 actual isolated restore/RPO, T16/T20 runtime usability and T21 actual network
  privacy assertions remain OPEN. Hosted [I] requires separate authorization.
- [ ] Reconcile T22 exact status explicitly before accepting it; no silent API or
  contract change in this verification milestone.
- [ ] Normal branch push/exact remote SHA and one append-only Issue31 checkpoint
  pending at this source checkpoint; record actual publication separately.

Matrix: `docs/planning/goldberg-0.11/stage-6/LOCAL-SECURITY-MATRIX.md`.
Raw fresh results/index: `stage-6/results/security-local/`, final `run-x1eRKaqL`.
Reproducible narrow host command: `bash spikes/stage6-security-local/run-local.sh`
on a permitted local host with existing locked dependencies and cached PG17.11
image. It reuses the unchanged disposable backend runner; no hosted connection.
Do not rerun known-blocked PostgreSQL setup in this agent venue. Overall/formal
6.12 remains UNCHECKED/PARTIAL. Android6.10–6.11 runtime/emulator recovery DEFERRED;
packaging already PASS, zero native bodies. Stage5/7A/X10/7B and all user data
untouched. Exact next action: publish the verified audit scope normally, then
obtain a permitted PostgreSQL venue and resolve documented acceptance gaps.

## Stage 6.12 publication blocker — verified source preserved locally

Audit commit `ebd813509c046c3b6119e2aed953f270ec7d8d92` is LOCAL ONLY on
`verify/goldberg-0.11-stage-6-12-local`. Normal push failed before authentication:
`Could not resolve host: github.com` (exit128). Connected read after failure confirms
exact branch404. Issue31 checkpoint attempt returned403 `Resource not accessible
by integration`; fresh read found73 comments, zero matching new checkpoint marker.
**Source and new Issue checkpoint UNPUBLISHED.** Prior backend9d7150c and Android
2f1524e publication remain verified; unrelated new comment belongs to its author.

Guarded host publisher: `spikes/stage6-security-local/publish-host.py` requires an
explicit reviewed40-character HEAD, correct clean branch/origin/ancestry, matching
baseline/raw evidence/source hashes and PARTIAL findings; normal push only, exact
remote readback, duplicate guard, append-only Issue31 and old description/comments
readback. It neither builds nor deploys. Verify locally with `--verify-only`; actual
network publication remains UNEXECUTED until a permitted host runs it. This receipt
commit follows the verified audit source; use its exact current SHA explicitly.

59 tests/five checks PASS and T22 finding remain as below; no fresh PostgreSQL,
Android/emulator, hosted or later-stage execution. Next: run the guarded publisher
and unchanged disposable PostgreSQL verification on a permitted host, preserve raw
results and continue6.12 acceptance. Overall6.12 PARTIAL, all formal gates unchanged.

## Stage 6.12 — fresh owner PostgreSQL evidence confirmed (PARTIAL)

Owner executed the existing version1 security runner on Fedora at exact source
`faf9ba3ec24e7098289c80d6e119519ccc21b95e`. Agent independently inspected both runs:
`stage-6/results/security-local/run-vQd6iGn5/` and
`spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/`.
This supersedes prior statements that fresh PostgreSQL execution is blocked;
all older checkpoints and raw failures remain historical evidence.

- [x] Wrapper:27 focused +32 unit PASS; PostgreSQL17.11:50 Auth +87 handler +34
  schema +34 integration +32 unit PASS. **296 successful executions;264 tests
  counting the repeated32 unit suite once. Zero failures/skips/cancellations.**
- [x] All five repository checks PASS in both runners; wrapper flags false for
  checksOrTestsFailed/postgresBlocked, true for sourceUnchanged. At-run62 source
  hashes all matched before tool edits; immutable55 raw-artifact hashes recorded.
- [x] Exact wrapper stdout equals nested TAP/check concatenation; container removal
  log retained. Successful exit uses owner report/v1 result/control flow; no numeric
  exit sidecar was captured. Live agent Podman inventory is denied by read-only
  runtime configuration; do not claim an additional live-container check passed.
- [x] Preserve run-csaqQFXV/run-cTS0AuDe dependency-startup failures, missing TAP
  summaries and all earlier successful/blocked runs. No destructive cleanup.
- [x] Fix only local verification tooling: version2 writes structured failure JSON
  for missing/duplicate/truncated/inconsistent TAP, retains raw logs, fails closed
  on false-green summaries and source changes; records future nested exit status.
  **10 Python reporting regressions PASS**, separate from296 host executions.
- [x] Matrix uses fresh exact names/logs:168 references; **13 local PASS,6 PARTIAL,
  2 NOT TESTED,1 FAIL**. T22 remains FAIL (observed400 vs required403), with no
  mutation dispatch or demonstrated privilege bypass. No API/SQL/Android changes.
- [x] Publisher now requires the successful actual host counts, immutable artifact
  hashes, tested-source ancestry/identity and current tool/index hashes; it keeps
  PARTIAL wording and duplicate/history guards. Old postgresBlocked=true gate removed.
- [ ] Full version2 PostgreSQL runner execution NOT performed; reporting tests pass.
- [ ] T22 explicit contract/security review; T13 rendering; T14 DNS/redirect/rebinding/
  fetch; T15 HTTP/browser; T19 real restore/RPO; T16/T20 runtime usability; T21 actual
  network privacy; all hosted [I] portions remain OPEN. No automatic gate acceptance.
- [ ] Publication of this correction awaits normal push/exact remote readback and
  one new factual Issue31 checkpoint; prior failed publication remains historical.

Branch `verify/goldberg-0.11-stage-6-12-local`, based on preservedfaf9ba3;
implementation/evidence commit for this update follows that ancestor. Current tool
changes are explicitly separated from the owner-tested version1 runner; at-run
source hashes are checked against current unchanged services or immutablefaf9ba3
for the identified tooling edits. Matrix/index and source hashes remain reviewable.
Read `stage-6/LOCAL-SECURITY-MATRIX.md` and `results/security-local/host-confirmed-faf9ba3.json`.
Exact next: publish the verified evidence/tool fix normally, then resolve T22 and
remaining local acceptance gaps; hosted checks require separate authorization.
Formal6.12 stays UNCHECKED/PARTIAL. Android runtime/emulator recovery deferred;
Stage5/7A/X10/7B untouched. No hosted/OAuth/real-data/paid/merge/tag/release or6.13–6.18.

## Stage 6.12 publication outcome — host tests VERIFIED, source/checkpoint UNPUBLISHED

Fresh Fedora PostgreSQL verification is confirmed:296 executions/264 tests counting
repeated unit suite once;10 separate reporting regressions PASS. This is independent
of publication status. Source update `a21dbafb1180563b88ed36909a801da179b5bf55` preserves
owner-testedfaf9ba3; this following receipt also excludes Python bytecode caches
from the version2 runner source snapshot. Full version2 PostgreSQL run unexecuted.

Normal publication failed: guarded remote lookup and direct non-force push could
not resolve github.com; connector comment returned403; directgh API could not connect.
Issue31 readback:75 comments, all previously read bodies preserved, zero new matching
checkpoint. Exact branch read returned404; no remote SHA or checkpoint URL claimed.
**Source and checkpoint remain UNPUBLISHED.** Existing backend/Android publications
remain valid. New draft `results/security-local/ISSUE31-HOST-VERIFIED-UNPUBLISHED.md`
and raw blocker record `fresh-publication-blocked.json` are retained.

Updated `spikes/stage6-security-local/publish-host.py` passed local read-only guards
at the verified source commit. On a permitted host, supply the exact current clean
HEAD with `--expected-head` (first `--verify-only`, then publish normally). It checks
fresh successful counts/hashes and preserves all prior Issue comments/description;
no repeated PostgreSQL/build run is needed merely to publish these verified facts.
T22 remains FAIL and formal6.12 PARTIAL; remaining local/hosted gates OPEN. Android
runtime deferred, no Stage7B/emulator/hosted/merge/release or6.13–6.18 work.

## Stage6.12 — approved T22 correction and Stage7B contract coordination (PARTIAL)

Published base `e7dc7ea871f61965600ba31076e8d28dc01822fe` and Issue31 checkpoint
6369071469 independently read back; older UNPUBLISHED statements below are historical.
Continue on `verify/goldberg-0.11-stage-6-12-local`, isolated from Stage7B.
Purpose/scope: trace and correct the approved local T22 routing classification,
add focused negative regressions, and prepare exact G1–G11 shared fixture handoff.
No formal6.13–6.18 work or6.17 API freeze.

- [x] Reproduce old400 before authentication/dispatch versus Auth adapter403.
- [x] Explicit owner approval recorded: malformed/non-string/empty operation400;
  well-formed unregistered audit/bootstrap operation403 with no dispatch; registered
  missing/invalid session401; live capability/self-grant403. Applied exact local
  patch and corresponding regression expectations; no new privileged endpoint.
- [x] Final local119 PASS:38 T22 +11 shared-contract +27 security +32 unit Node tests,
  plus11 Python reporting regressions. Zero failures/skips/cancellations in final
  runs. All five repository checks PASS; raw intermediate harness failures retained.
- [x] T22 tests use real services with scripted SQL, not actual PostgreSQL. No role/
  audit mutation attempted in denied cases; expired-key housekeeping rolls back.
  Non-string operation coercion is rejected before handler selection.
- [x] Matrix13 local PASS/7 PARTIAL/2 NOT TESTED; original T22 FAIL remains historical.
  T22 now PARTIAL after explicit owner decision and local fix, not full acceptance.
- [x] Read actual Stage7B source/checklist/acceptance/proposals read-only; hash all
  inspected bytes, including uncommitted work. No Stage7B file/index/build changed.
- [x] Shared Admin v1 preparation:30 actual in-process/scripted-storage captures,
  11 inactive proposals,29 byte-preserved authored Stage7B comparison cases. Exact
  source/corpus hashes; all synthetic, no HTTP binding or PostgreSQL claim.
- [x] G1–G11 ledger records normative/backend/Android/mismatch/fixtures/resolution/
  approval/status. G10 remains CLOSED on Android by static compatibility; G1–G9
  andG11 remain blocked for real integration. No dialect or revision mapping guessed.
- [ ] Current updated PostgreSQL assertions: agent socketEPERM,0 database bodies;
  run version3 local runner on permitted host. Prior owner296 executions/264 tests
  are pre-patch baseline only; original raw manifests remain immutable/Git-verifiable.
- [ ] Hosted/browser/fetch/restore/network-privacy/Android-runtime gates remain OPEN.
- [ ] Commit/push/readback and one non-duplicate Issue31 checkpoint for this update.

Reports: `stage-6/T22-INVESTIGATION.md`, `stage-6/STAGE7B-CONTRACT-READINESS.md`,
`contracts/admin/v1/readiness.json`. Raw final evidence and owner decision:
`stage-6/results/security-local/t22-investigation/`. Production networking and
privileged Android actions remain disabled. Formal6.12 PARTIAL,6.17 not accepted;
no hosted/OAuth/real data/paid/merge/tag/release/emulator/phone/Stage7B writes.
Exact next: publish reviewed scope, execute updated PG17.11 regressions on permitted
host, and obtain G1–G9/G11 implementation/DTO/session/validation decisions before
one approved Android adapter or any live integration.

## T22/Stage7B update publication outcome — LOCAL ONLY (base publication intact)

Verified code/fixture update `262a76ed08c7910e9a4ad86018188589efc58c37` is committed
on the isolated security branch. Normal non-force push failed DNS resolution;
connected read confirms remote still `e7dc7ea871f61965600ba31076e8d28dc01822fe`.
That base and Issue31 checkpoint6369071469 remain PUBLISHED. This new update's
checkpoint write returned403; no new checkpoint URL or remote success claimed.
The prior76 comments had no matching new marker; no dashboard/history edit.

Raw blocker and draft: `results/security-local/t22-handoff-publication-blocked.json`
and `ISSUE31-T22-HANDOFF-UNPUBLISHED.md`. Updated guarded publisher passed local
read-only verification at262a76e; this following documentation receipt changes no
service/fixture/test behavior. On a permitted Fedora host, run the publisher with
exact final clean HEAD, then the updated version3 PostgreSQL runner. It checks
source/corpus/evidence/approval/counts, normal push ancestry and exact remote SHA,
appends one factual checkpoint and retains history. No repeated network attempts here.

Final119 local tests/five checks PASS; updated PostgreSQL0 bodies dueEPERM. T22
PARTIAL after explicit owner-approved local correction; G10 closed, G1–G9/G11
blocked for real integration. No HTTP freeze, Stage7B edit, emulator, hosted changes,
merge/release or6.13–6.18. Exact next action: publish current scope from permitted
host and obtain current real PostgreSQL regression evidence plus shared decisions.

## Stage6.12 — post-T22 Fedora version3 VERIFIED; T22 LOCAL PASS, overall PARTIAL

Published/tested base `6d0abad3decf0b724c0e00f1b4e270bf8d244fba` independently read
back on the existing security branch; Issue31 checkpoint6370125421 preserved.
Purpose: reconcile the new actual post-patch PostgreSQL execution and T22 criteria,
not change service behavior or activate public/client integration.

- [x] Exact outer run-x0nDSTXd:27 focused +32 unit +38 T22 +11 contract =108 PASS.
- [x] Matching nested run-TEmZzZQg PG17.11:50 Auth +87 handler +34 schema +34
  integration +32 unit =237 PASS. **345 successful executions;313 tests counting
  repeated32 unit suite once;205 PostgreSQL-backed bodies.** Zero failures/skips/
  cancellations across nine TAPs; model/fixture tests remain distinctly labeled.
- [x] Explicit nested exit0, empty summaryErrors, successful outer flags, exact
  nested stdout linkage; all five checks PASS in both runners. All82 at-run source
  hashes matched before reconciliation edits and immutable31 raw artifacts hashed.
- [x] Removal output names goldberg-public-admin-abuse-3645015; container ID/version
  retained. No new live inventory pass is claimed. All previous runs/FAIL findings
  and pre-patch296/264 baseline preserved without deletion or relabeling.
- [x] T22 LOCAL PASS: actual handler/admin assertions require403 on absent audit edit;
  live self-grant/revocation/escalation and physical SQL immutable-history guards
  executed.38 routing/service models separately verify approved400/401/403 cases.
  No HTTP listener/hosted/Android assertion inferred from local execution.
- [x] Current matrix14 local PASS/6 PARTIAL/2 NOT TESTED. Browser, SSRF fetching,
  restore/RPO, network privacy, Android runtime and hosted portions remain OPEN.
- [x] Stage7B readiness reconciled: G10 remains closed by pinned Android boundary;
  G1–G9/G11 remain BLOCKED for real integration. Corpus captures/proposals retain
  original qualifications and source identities; no public API freeze or activation.
- [x] Evidence verifier/reporting regression tests executed separately; raw results
  in results/security-local/post-t22-tools/. No service/SQL/Android code changes.
- [ ] Normal source push/exact remote readback and one new factual Issue31 checkpoint
  for this reconciliation; record actual publication separately.

Current branch `verify/goldberg-0.11-stage-6-12-local`; current commit follows the
verified6d0abad ancestor. Exact source binding is to6d0abad, with only specifically
identified evidence/index/publication/documentation metadata changed afterward.
Reports: LOCAL-SECURITY-MATRIX.md, T22-INVESTIGATION.md, STAGE7B-CONTRACT-READINESS.md,
post-t22-independent-verification.json. Next: publish verified evidence normally,
then resolve G1–G9/G11 decisions and remaining acceptance under proper authorization.
Formal6.12 PARTIAL; no6.13–6.18, hosted/OAuth/real data/paid, emulator/phone, Stage7B
writes, merge/tag/release or6.17 acceptance. Successful v3 PG run need not be repeated.

## Post-T22 reconciliation publication outcome — VERIFIED locally, update UNPUBLISHED

Reconciliation commit `f7d88aae69bdf2a4b1adb248e1020c9a9b3baa8a` preserves the
published/tested6d0abad baseline and checkpoint6370125421. Normal push failed DNS;
connected read confirms remote still6d0abad. Checkpoint write returned403, with
no new URL; prior77 comments had no matching new marker. No dashboard/history edit.
This documentation receipt changes no tested service/fixture/SQL behavior.

345 host executions/313 tests counting repeated unit once;205 PostgreSQL-backed
bodies;17 additional tool regressions PASS. T22 LOCAL PASS, overall6.12 PARTIAL.
G10 remains closed; G1–G9/G11 integration-blocked. No HTTP/hosted/Android acceptance.
New draft and exact blocker: ISSUE31-POST-T22-V3-UNPUBLISHED.md and
post-t22-publication-blocked.json under results/security-local/.

Updated guarded publisher passed local --verify-only atf7d88aa. On permitted Fedora,
supply exact final clean HEAD to publish normally; it verifies the successful v3
counts/source hashes/exit/cleanup, prior evidence, source ancestry and history,
then appends one factual checkpoint after exact remote readback. No repeat of the
successful PostgreSQL suite, force push, guard bypass, emulator, Stage7B changes,
cloud operation, merge/release or6.13–6.18 work. Next: host publication, remaining
local acceptance and explicitly authorized G1–G9/G11 decisions/integration.


## Current — API v1 owner decision preparation (2026-10-23)

**PROPOSED / INACTIVE / NOT FROZEN.** Worktree `/tmp/goldberg-stage6-security-local`,
branch `verify/goldberg-0.11-stage-6-12-local`, starts at verified published baseline
`19f5642192148051a7b4eb5bc5efb1090192e3c0` and
[checkpoint 6370511889](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6370511889).
This supersedes older “unpublished baseline” summaries below; their evidence is retained.

Purpose/scope: planning-only G1–G9/G11 contract decision package, exact public DTO/
endpoint proposals, synthetic fixtures and offline verification tooling. No production
endpoint/auth/network/moderation activation; G10 remains CLOSED. Stage7B worktree
unchanged. Existing backend, Android source, SQL and historical corpora/evidence unchanged.

- [x] Separate normative design, verified local behavior, Android legacy expectations
  and unapproved preferred design in `stage-6/API-V1-DECISION-PROPOSAL.md`.
- [x] Prepare G1 snapshot statistics, G2 queue metadata/search, G3 audit filters,
  G4 authorized current values, G5 session-derived identity/lifecycle, G6 explicit
  open + independent revisions/replay/conflict recovery, G7 binding/schemaVersion,
  G8 lossless decimal int8, G9 canonical audit/window, G11 byte/policy validation.
- [x] Prepare 61 authored synthetic DTO cases (37 positive/24 negative), 15 verifier
  regressions; preserve 41 primary cases (30 captures/11 proposals) and 29 comparison
  scenes. Exact offline verification results are in docs/ai/VERIFICATION.md.
- [ ] Owner O1–O8 choices in decision document: public lifecycle/version mapper and
  offline logout amendment; TTLs; explicit revisions/receipts; int8/cursor/version
  compatibility; server timezone/window; search/privacy; actual field/source/license
  policy; separate future implementation authorization. No approval inferred.
- [ ] G1–G9/G11 integration BLOCKED; formal 6.12 PARTIAL; 6.17 NOT FROZEN;
  no 6.13–6.18 implementation or acceptance from this task. Browser/fetch/restore/
  privacy/hosted and Android runtime acceptance remain OPEN. X10 OPEN; Stage8 pending.

Prior 345 successful executions/313 tests counting repeated 32 once/205 PostgreSQL
bodies are historical verified source evidence, **not rerun**. Q1/D5b, C4, C2 §12,
cloud/zero-cost/legal and activation gates remain unchanged. Stop at owner decision gate.
Next agent: read proposed decision package and O1–O8; collect owner decisions before
any implementation. Do not run old publisher for this new package; use the dedicated
guarded `contracts/admin/v1/decision-proposal/publish.py` with exact final SHA.
Publication receipt/fallback: `docs/ai/API-V1-PROPOSAL-PUBLICATION.md`.
Normal push blocked by DNS; both Issue31 writes denied403 by integration.
No new checkpoint URL;78 previous comments and issue description unchanged.
Publication pending via guarded Fedora publisher with exact final clean SHA.
## Stage 6 ACL hardening and backup review — 2026-10-24

**Purpose:** review the owner-supplied candidate privilege hardening and
validate it only in a disposable PostgreSQL 17 runtime, while reconciling the
zero-cost backup/recovery requirements without exporting real data.

**Scope:** supplied review package; Stage 6 security/readiness and verification
documentation; no hosted Supabase permissions, application source, Room
schema, Android integration, Stage 7 worktree or production backup scheduler.

**Tasks**

- [x] Locate the extracted package using `xdg-user-dir DOWNLOAD` and read its
  README, ACL SQL, runner and backup review.
- [x] Review candidate preflight/postchecks, effective ACL intent and trigger
  preservation; no unrelated authorization change identified.
- [x] Inspect the local runtime without pulling dependencies.
- [ ] Execute disposable PostgreSQL 17 acceptance checks.
- [ ] Verify `anon`/`authenticated` effective `EXECUTE` denial, preserved
  `ensure_rls` behavior and no unintended privilege escalation.
- [ ] Verify encrypted synthetic backup/restore, manifest and recovery
  procedure; no real-data export or scheduler activation.
- [x] Record the actual blocked result and preserve raw output privately.
- [x] Synchronize Stage 6 handoff/state/verification and security audit docs.

**Progress and verification:** Podman is installed, but the required cached
`docker.io/library/postgres:17` image is unavailable. The supplied runner
returned **BLOCKED, exit 2** and correctly attempted no pull; no container or
SQL was run. PostgreSQL test count is **0 executed**. The candidate is
conditionally suitable for disposable testing only and is not approved for
hosted deployment. Backup readiness remains design-only: destination,
encryption keys, schedule, export, isolated restore and measured RTO are not
verified.

**Completion status:** **OPEN / BLOCKED**, not complete. C4 remains 8 PASS /
19 PARTIAL / 6 OPEN and 6.0f remains unchecked. M1–M3 remain preserved and
Stage 7B was not touched.

created by stokonn with love <3
## Stage 6 ACL hardening and backup review — 2026-10-24 verification update

**Progress:** The owner-authorized official PostgreSQL image was pulled
rootlessly with Podman. The unchanged supplied runner exited **0**. PostgreSQL
17.11 synthetic acceptance passed: effective `EXECUTE` is false for
`PUBLIC`, `anon` and `authenticated`; `ensure_rls` remains enabled; synthetic
table creation enables RLS; no unexpected authorization change was observed.
The final detailed run captured clean PostgreSQL logs and removed all
disposable resources.

**Evidence:** image manifest
`sha256:3518071376d0b6156b02e1db2747a78511b5b9dc3763cc79c506ebe2dc2ee71c`;
local ID `82903352d6faf8f8a318bc4353d1cb1db67d4a5ff968d2799b2718e2cb00afa2`.
Raw runner/detail logs are preserved privately. Two auxiliary diagnostic
attempts exited 1 due query syntax errors; corrected final diagnostics passed.

**Completion status:** ACL disposable verification **COMPLETE**. Hosted
deployment approval, rollback/migration review, C4, 6.0f and backup/recovery
requirements remain open. No production or cloud change occurred.

created by stokonn with love <3
## Stage 6 backup and recovery verification — 2026-10-24

**Purpose:** exercise the documented zero-cost logical backup design against
only synthetic PostgreSQL 17 data, including encryption, integrity, isolated
restore and audit-chain verification.

**Scope:** new `spikes/stage6-backup-local/` fixture, shell harness and SQL;
private synthetic evidence; Stage 6 documentation. No Supabase, real data,
production scheduler, provider storage, Android or Stage 7 worktree.

**Tasks**

- [x] Review `ZERO-COST-OPS.md`, `BACKUP-REVIEW.md`, `DATA-MODEL.md` and
  `THREAT-MODEL.md`.
- [x] Implement a no-port, disposable PostgreSQL 17 synthetic fixture.
- [x] Export a custom-format logical dump and encrypt it with a temporary key.
- [x] Generate and verify a SHA-256 manifest; reject deliberate ciphertext
  tampering.
- [x] Decrypt and restore into a fresh database.
- [x] Verify row counts, normalized schema equality and audit-chain/head
  integrity.
- [x] Verify cleanup and scan preserved evidence for secrets/endpoints.
- [x] Record local export, restore and total elapsed times.
- [ ] Verify owner-controlled destination, key custody/recovery, schedule,
  retention, hosted restore and production backup failure handling.

**Verification:** final harness exit **0**; export **368 ms**, restore
**997 ms**, total **4,354 ms**. Restored counts were 2 metadata, 2
submissions and 2 audit events. Encryption, manifest, tamper rejection,
schema comparison, audit chain and cleanup passed. The measured timings are
synthetic local timings only; no production RPO/RTO claim is made.

**Completion status:** synthetic local drill **COMPLETE**; backup readiness
and Stage 6 remain **OPEN** pending private destination/key/schedule/restore
decisions and the independent C4/6.0f gates.

created by stokonn with love <3
## Stage 6 major engineering/readiness batch — 2026-10-24

**Purpose:** reduce independently actionable Stage 6 readiness risk without
activating production services or changing the approved C4 classification.

**Scope:** ACL migration proposal, synthetic backup acceptance, Stage 6/7B
contract comparison, local verification, and synchronized handoff documents.

**Tasks**

- [x] Prepare a narrow reversible `rls_auto_enable()` ACL migration with
  preflight, postflight, and exact-ACL rollback guidance.
- [x] Re-run the supplied disposable PostgreSQL 17 procedure unchanged.
- [x] Record backup resilience acceptance and distinguish synthetic evidence
  from production scheduling, retention, restore, and RTO gates.
- [x] Compare Stage 6 API local semantics with the immutable Stage 7B source
  and preserve the inactive mapping proposal.
- [x] Run focused API/security/policy/fixture checks.
- [ ] Resolve the pinned Stage 7B source-hash mismatch and obtain owner approval
  for the shared public API fixture decisions.
- [ ] Close hosted/legal/C4/6.0f and G1–G9/G11 requirements.

**Verification:** PostgreSQL 17 ACL procedure exit 0; API **106/106 PASS**;
security evidence regressions **17/17 PASS**; policy, backup fixture, and
whitespace checks PASS. `verify_contract.py` failed closed on the pre-existing
Stage 7B `ACCEPTANCE.md` hash mismatch. No hosted or Android runtime checks
were executed. **Status: in progress; not complete.**
## Stage 6 integration engineering batch — 2026-10-24

**Purpose:** establish executable local compatibility and provenance guards
without activating an unapproved API mapping or production service.

- [x] Compare the pinned Stage 7B hash with later branch history and current
  source; preserve historical evidence.
- [x] Add synthetic executable compatibility tests for approved distinctions.
- [x] Add a static ACL proposal safety regression.
- [ ] Obtain owner decisions and implement only an approved shared binding.

**Verification:** compatibility and ACL checks are executed with the batch
test results; no Stage 7B files, hosted services, Android devices, or live
databases were touched. Status remains **in progress**.
## Stage 6 local API contract hardening — VERIFIED LOCALLY (2026-10-24)

**Purpose:** implement independently testable, fail-closed API contract
primitives without activating the provisional `/v1` surface.

**Scope:** local API statistics/time-zone and query validation helpers plus
synthetic tests; no HTTP listener, deployment, hosted database or Android
transport.

- [x] Derive Europe/Prague civil-day windows with UTC six-digit timestamps.
- [x] Verify ordinary, spring-DST and autumn-DST windows against the approved
  fixture corpus.
- [x] Implement NFC-normalized, case-insensitive, diacritic-sensitive literal
  search.
- [x] Validate bounded queue filters and exact audit vocabulary/ranges.
- [x] Reject unknown, unsafe, malformed and ambiguous query inputs.
- [x] Run local API and policy/design checks: 115/115 API tests passed.
- [ ] Implement queue/audit DTO projections and statistics aggregation only
  after owner-approved source fields and snapshot semantics are available.

**Verification:** `npm test --silent` passed 115/115; targeted statistics tests
passed 4/4; policy, backup fixture, ACL proposal, design fixture and whitespace
checks passed. No HTTP/PG/Android runtime acceptance was claimed.

**Completion:** local contract-hardening substage verified; Stage 6 remains
incomplete because `/v1` compatibility, hosted/security/legal/operational and
G1–G9/G11 gates remain open.

created by stokonn with love <3
## Stage 6 loopback HTTP integration — VERIFIED LOCALLY (2026-10-24)

**Purpose:** exercise the approved local adapter through real HTTP request and
response handling without exposing a public listener or activating `/v1`.

**Scope:** `spikes/stage6-api-v1-local/src/http.mjs`, adapter route guards,
synthetic loopback tests and local evidence only. No hosted service, real
credentials, Android transport or Stage 7 worktree.

- [x] Bind only to `127.0.0.1` on an ephemeral port.
- [x] Preserve adapter validation, authorization, request IDs and response
  headers through HTTP.
- [x] Enforce body-size, duplicate-key, duplicate-query and unsupported-method
  rejection.
- [x] Verify challenge/login/capabilities/logout and empty 204 behavior over
  HTTP.
- [x] Recognize queue/audit/stats routes with capability guards and explicit
  unavailable responses; do not fabricate records or totals.
- [x] Run loopback tests 2/2 and complete local suite 117/117.
- [x] Re-run disposable PostgreSQL M2 and M3 acceptance; both passed with
  cleanup and source-unchanged checks.
- [ ] Add exact queue/audit DTO projections and database-backed statistics
  snapshots once approved source fields and snapshot semantics are available.

**Verification:** actual loopback HTTP 2/2 PASS; complete API suite 117/117
PASS; M2 and M3 PostgreSQL acceptance PASS. M2/M3 report `httpExecutions: 0`;
they are separate database-backed checks and do not prove HTTP behavior.

**Completion:** loopback transport substage verified. Stage 6 remains
incomplete because public-version compatibility, exact queue/audit projections,
statistics snapshots, hosted/legal/operational gates and G1–G9/G11 remain
open.

created by stokonn with love <3

## Stage 6 full-int8 recovery — LOCAL ACCEPTANCE VERIFIED (2026-10-24)

Purpose/scope: recover and complete the interrupted, owner-authorized shared
handler/local API int8 implementation, current provenance and disposable-PG
acceptance. Branch `feature/goldberg-0.11-stage-6-api-v1-local`; recovered published
baseline `0142e997f3a63a026c76219d8dcb04185b66e7ee`. Implementation/evidence commit `023d290e5f30c26e85ffba5f20b1d16914a1e1e5`
ordinary-pushed; exact remote SHA verified. Implementation [quality run40098429341](https://github.com/stokonn/Goldberg/actions/runs/40098429341) **SUCCESS**; publication receipt below.

- [x] Preserve dirty work, historical metadata/provenance/raw failures and private
  `/tmp/goldberg-stage6-private-pmp/checkpoint.md`; save new private recovery
  checkpoint separately. Other agent worktrees/processes untouched.
- [x] Canonical decimal strings through the wire/shared handler bridge; entity≥0,
  submission≥1; BigInt checked increments through signed int8 maximum;409 overflow,
  400 malformed input. Existing atomic locking/idempotency/audit mechanisms retained.
  No schema, migration, transaction utility or Android change.
- [x] Final run `results/http-pg/pg-kr8b1q9g`: **469 Node bodies PASS**, including
  **24 existing HTTP→PG +13 int8 HTTP→PG +240 independent PG** bodies;
  **API122/T22-38/operations-unit32**. Zero failures/skips/cancellations.
- [x] **Historical18 design +17 security-evidence** regressions PASS on original
  `6dd3136` source (source baseline `19f5642`); **current parser11** PASS.
  **Current DTO61 expectations** match37 positive/24 negative;39 actual HTTP read
  DTOs and PostgreSQL Comparison captures validate. Frozen current-file pin rejection
  is retained explicitly as EXPECTED FAILURE, never a current-source PASS.
- [x] Policy, three portable backup fixtures, ACL proposal and whitespace PASS.
  **181 current-source files + manifest (182 run hashes)** unchanged during execution;
  independent verifier exit0 and live exact container/volume absence probes PASS.
- [x] Ordinary commit/push/exact remote readback of `023d290e5f30c26e85ffba5f20b1d16914a1e1e5`;
  Issue31/Project publication checkpoint synchronized. All182 committed source/manifest
  files match final run.
- [x] Implementation [quality run40098429341](https://github.com/stokonn/Goldberg/actions/runs/40098429341) SUCCESS; record this documentation-only
  publication receipt and committed source identity. Issue31 stays OPEN / Goldberg
  Development In Progress. Final receipt SHA/CI are retained in Issue31/private PMP.

The source-set SHA256 is
`b7c6db2d251429e7edbbcef4ae3095a915ee8d5330ac7233254e4573b075c745`.
All commands, counts, failure history and full-range submission testing limits are
in `docs/planning/goldberg-0.11/stage-6/API-FULL-INT8-ACCEPTANCE.md`.
Extreme submission increments/overflow have scripted service coverage; actual PG
chains preserve every guard and exercise1→2, without fabricated high predecessors.

**Completion:** this local numeric substage passed acceptance. Stage6 OPEN,
6.12 PARTIAL,6.17 NOT FROZEN; C4 **8 PASS/19 PARTIAL/6 OPEN**,6.0f unchecked;
consumer G1–G9/G11, `/v1` compatibility, policy, client reconfirmation, hosted,
operational and legal gates remain open. G10 closed/Android transport disabled.
No production/cloud/OAuth/real-user/device/ADB/other-worktree/merge/release/deploy.
No local Gradle/JVM/lint/APK/signing/instrumentation/screenshots executed; existing
GitHub quality workflow is checked separately after publication. Prior restore/ACL
SQL evidence is historical, not rerun by static checks. Failed attempts retained.

created by stokonn with love <3

## Stage6/7B executable integration readiness — LOCAL ACCEPTANCE VERIFIED (2026-10-24)

**Purpose/scope:** maintained canonical shared fixtures, actual HTTP/PG behavior,
source-pinned consumer compatibility, isolated actual Kotlin JVM replay and measured
query bounds. Stage6 branch `feature/goldberg-0.11-stage-6-api-v1-local`; recovered
published baseline `a3fb7e2f1436c93b49a0f4f0596d8d06e241fcef`. Stage7B inspected
read-only at `0887d3fe33cf5d49d46e14e1de95168f08a493a2`; its source/acceptance intact.

- [x] Recover actual branch/remote/checkpoints/O1–O8/Issue31/private PMP and inspect
  actual Admin API/Models/WireValues/Repository/Review/Session/Queue/Audit/Stats.
- [x] Implement versioned `contracts/admin/shared/v1`:66 actual HTTP scene fixtures,
  canonical schema references, consumer source/history pins, executable local
  session/reconfirmation reference and34 independently checked compatibility rows.
- [x] Compile four unmodified selected actual Kotlin sources into/tmp;22 replay
  probes PASS:12 compatible/10 incompatible. Full matrix:12 VERIFIED COMPATIBLE,
  12 VERIFIED INCOMPATIBLE (including2 explicit static contradictions),7 NOT EXECUTED,
  2 BLOCKED BY OWNER/POLICY,1 NOT APPLICABLE. No Android runtime claim.
- [x] Backend accepts explicit UTF8 JSON charset; no legacy DTO/revision aliases.
  Measured queue optimization bounds header page before payload joins; all filters,
  ordering/snapshot/lock semantics preserved. No schema/index/migration change.
- [x] Final `results/http-pg/pg-ojxz4mbi`: **490 Node bodies PASS**,0 failures/skips/
  cancellations: all469 baseline bodies plus20 shared HTTP/PG and1 query-plan body.
  Existing realHTTP/PG37, full-int8 coverage, auth/T22/concurrency/replay/rollback
  retained.66 actual fixture responses validate before synthetic-token redaction;
  2 actual response streams destroyed to verify loss recovery.39 existing read and
  22 Comparison captures still match schema.
- [x] Current DTO61 expectations PASS (37 positive/24 negative), historical18 design/
  17 security-evidence tests on original snapshot and current parser11 PASS; policy,
  three backup fixtures, static ACL proposal and whitespace PASS. Historical pin on
  evolved current handlers remains EXPECTED FAILURE, not reclassified PASS.
- [x]196 source/test/input files plus separate integration manifest unchanged through
  execution and independently verified; original full-int8 manifest unchanged.
  Exact synthetic container/volume removed and independently probed absent.
- [x]5000-row actual-query EXPLAIN: five variants,5s timeout, page≤20/SQL21;
  revision work≤21. Default/keyset samples2.877/2.871ms vs13.148/13.368ms before.
  Header scans/statistics remain scaling limits; no production/free-tier budget claim.
- [x] Exact consumer request/session/conflict/metadata/freshness handoff, G1–G11,
 34-row matrix and gated concrete /v2 migration proposal prepared. External/deployed
  consumer state UNKNOWN; no public version selection/activation/freeze.
- [x] Source/evidence `b2fed621ef32665072faa6172d7947c99db3af61` ordinary-pushed and exact remote SHA verified;
  all197 committed input/manifest files match final run; recovered baseline is an
  ancestor. Documentation-only receipt records publication; exact final-head GitHub
  quality result is maintained in the final Issue31 checkpoint/private PMP.

**Source-set SHA256:**
`ddffb491f5e99f1db6bd20673312be888b8b4f846d30038d15fdcad2eea2a9c0`.
**Commands/results:** `python3 spikes/stage6-api-v1-local/build-current-provenance.py`,
`python3 spikes/stage6-api-v1-local/run-http-postgres.py`, then
`python3 spikes/stage6-api-v1-local/verify-http-pg.py spikes/stage6-api-v1-local/results/http-pg/pg-6ape0ubv`
all PASS/exit0. Selected Kotlin replay is integrated by `consumer-compatibility.py`;
independent scene/matrix/plan verification by `verify-shared-contract.py`.
Full handoff: `docs/planning/goldberg-0.11/stage-6/INTEGRATION-READINESS.md`.

**Completion:** local foundation passed acceptance and source is published; exact final-head
CI result/receipt is referenced by the existing Issue31 checkpoint. Stage6
and Stage7B remain OPEN/IN PROGRESS; G1–G9/G11 consumer adoption, owner version/real
policy, client lifecycle/UI and hosted/legal/operational gates remain open. G10
CLOSED for disabled production transport/actions. C4 **8 PASS/19 PARTIAL/6 OPEN**,
6.0f unchecked,6.12 PARTIAL,6.17 NOT FROZEN. No production/OAuth/real-user/device/
ADB/other-worktree/main merge/release/deploy. No local Android build/APK/signing/
instrumentation/screenshots; isolated Kotlin JVM replay is not Android runtime.
Failed/intermediate raw evidence and private PMP preserved; original two untracked
M2/M3 directories retained. Next: publish verified local source and inspect exact-SHA
CI; then only separately authorized remaining adoption/owner/hosted work.

created by stokonn with love <3
