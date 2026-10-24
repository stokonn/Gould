> **Current recovery checkpoint — 2026-10-24:** CI #24 at dc60e83 failed the matrix because revision 2 legitimately requires re-review acknowledgement. Both retained ZIP digests/integrity and seeded data preservation independently PASS; no repeat downloads. Existing test-only acknowledgement → completion → verification correction reviewed; focused gate and seven scripts PASS. Clean host gate PASS 3m58s: 275 JVM/0 failures/errors/skips, lint 0 errors/119 warnings/1 hint, APK/signing/ZIP verified. Expected final matrix: **262 screenshot/tree pairs**. Stage 7B IN PROGRESS; owner acceptance pending. [Authoritative final recovery checklist](FINAL-RECOVERY.md). Earlier entries below are historical and do not describe the latest run status.

# Stage 7B owner review packet — recovery, October 24, 2026

**Status: IN PROGRESS; final owner acceptance is not yet ready.** Green5591e76 run is bounded evidence: detailed review found small2.0 Queue/Audit row collapse and stale previous-route hierarchy snapshots. Targeted list viewport/fresh-cache/actual-row follow-up published asdc60e83; host gates PASS, runtime258state-pair proof pending. This section is current; all earlier sections below are historical checkpoints.

Purpose/scope: resolve named checkable owners and dashboard refresh reachability, verify the approved synthetic Admin UI and preserve personal data. Current published/test-dispatch commit `dc60e8379114b44f758e0bb5184a0b6b83d7a465`, branch `feature/goldberg-0.11-stage-7b`. [Acceptance run40083355927](https://github.com/stokonn/Goldberg/actions/runs/40083355927) is PENDING. [Living implementation/verification checklist](RECOVERY-2026-10-24.md).

- [x] Exact AndroidX source/root-cause audit and preserved prior actual CI hierarchies.
- [x] Complete named-owner semantics for chips/switches/checkboxes; retain localized names, selected/toggled states, disabled guard and click callbacks. EN/CZ native regression compiled; final runtime proof pending.
- [x] Dashboard accessibility scroll synchronization with full48dp action checks at every font scale; preserve no-scroll≤1.3. Historical447738d run proves32dashboard reachability, not final follow-up visual acceptance.
- [x] Visually inspect32historical dashboards and11references; fix demonstrated small1.3 statistic ellipses with wrapping/compact padding. New wrapping render proof pending.
- [x] Clean host gate3m41s:275 JVM/0 failures/errors/skips; lint0 errors/119 warnings/1 hint; seven scripts PASS. Test-only helper follow-up host gate1m01s; production-source hashes unchanged. [Host evidence](results/recovery-followup-2026-10-24/).
- [ ] Final exact-source full instrumentation, all32rendered configurations, every exported checkable hierarchy and seeded-upgrade inspection.
- [ ] Explicit owner acceptance after reviewing evidence and limitations.

**Evidence limits that remain independent of automated green CI:**

- Native screen renders use production composables in a synthetic five-tab test scaffold. They do not establish the full production OpusApp navigation, TalkBack announcement/focus/touch exploration or every keyboard/reduced-motion interaction.
- Notification runtime test verifies platform permission denial then grant, generic private changed-scan delivery, role revocation, cancellation, scope changes and offline/recovery. It does not revoke Android POST_NOTIFICATIONS between separate live processes; do not conflate role revocation with platform permission revocation. WorkManager tests cover unique3h scheduling/constraints/cancel, not a real3h/reboot delivery.
- Seeded upgrade uses immutable v0.10.1 rebuilt with the candidate's temporary CI signer; independently checking the trusted release digest/certificate does not turn this into a released-APK installation test. Baseline has no Admin data; Admin store-recreation tests provide separate limited persistence evidence.
- Existing app-wide tab-label truncation and dark-parent status-bar contrast were visible in historical scaffold captures and remain disclosed; no navigation redesign was performed here.
- G1–G9/G11 approved live API/session/current-value integration remains blocked. Production networking and decisions remain disabled. No Stage6/7A worktree, physical device, merge or release was used.

---

# Stage 7B consolidated acceptance preparation

Status: IN PROGRESS. Implementation, executed verification and owner acceptance are separate. This packet supersedes older current-status summaries; historical records remain intact.

## Purpose and scope

Verify existing Android Admin dashboard, queue, proposal/re-review, audit and settings using synthetic fixtures, existing scheduling/storage seams and pinned contracts. Preserve personal Room data, recovery, Stage7A acceptance and Stage6 isolation. No production networking, decisions, OAuth, deployment, merge or release.

## Coordinated task checklist

- [x] Recover branch/HEAD/ancestry/remote and Issue31; initial HEAD/remote 3dc84880baf0571f06aff71b24ca135c3f505ffd, descendant of tested 3a4aac27e2eb8ae70716538d86acd374cdd38077; only private PMP untracked.
- [x] Inspect existing acceptance criteria, contract authority, upgrade helper and CI.
- [x] Check host KVM and attempt a meaningfully changed renderer configuration: emulator5584, API36, read-only/no-snapshot, current swiftshader and Vulkan disabled. Host SIGSEGV139 before ADB boot; zero app tests/screenshots. Preserve log; use existing KVM CI venue.
- [x] Add fixture UI/runtime checks; final instrumentation compile PASS21s.
- [ ] Execute runtime checks and retain actual source-bound Admin screenshots.
- [ ] Execute applicable lifecycle/scheduling/notification permission verification.
- [ ] Verify seeded upgrade baseline identity and all independently available preservation checks.
- [ ] Verify pinned contract/session cases without adopting concurrent M2/M3.
- [ ] Consolidate executed evidence, screenshots, limitations, owner review instructions and exact next actions.
- [ ] Synchronize local checklists/AI docs/Issue31, commit reviewed files, normal push and exact remote readback.
- [ ] Owner acceptance (explicit; never inferred from CI).

## Acceptance matrix

| ID | Approved criterion / evidence requirement | Dependency and venue | Status at preparation |
|---|---|---|---|
| V1 | Dashboard four tiles/three destinations; no scrolling or clipped controls ≤1.3 | Actual Admin renders, small+standard viewport, EN/CZ, Ivory/Nocturne parent, bars/navigation | NOT VERIFIED |
| V2 | Queue rows/search/filters/markers/context; empty/loading/error/stale usable | C6 fixtures; emulator + screenshots + JVM | NOT VERIFIED runtime; JVM PASS275 baseline |
| V3 | Proposal comparison/details/history/evidence; three confirmations; validation and uncertain replay | Synthetic C6 decisions only; production remains disabled | NOT VERIFIED rendering; domain fixtures PASS |
| V4 | Re-review changed fields/evidence/incomplete history, acknowledgement and conflict advisory | Controlled revision changes; emulator | NOT VERIFIED rendering; domain fixtures PASS |
| V5 | Audit read-only listing/search-scope/paging and empty/error | Synthetic audit, emulator + existing JVM | NOT VERIFIED runtime |
| V6 | Settings validation/recovery/confirmation, opt-ins OFF, metered consent | Emulator; no endpoint activation | NOT VERIFIED runtime |
| A1 | Accessible names, focus/navigation, 48dp actions, font1.5/2.0, keyboard/insets, reduced motion | Runtime accessibility tree/screenshots; TalkBack manual remains separate | NOT VERIFIED |
| S1 | Three-hour unique WorkManager scheduling, constraints, cancellation, stale scope/account isolation | Android WorkManager testing venue, synthetic input | NOT VERIFIED runtime |
| S2 | Complete queue signature/retry/offline/recovery/revocation/stale response | Domain tests + controlled worker execution; fake driver never live backend | Domain PASS; runtime NOT VERIFIED |
| S3 | Permission denied/granted/revoked; generic private notice only on changed complete scan | API36 notification manager/worker; fixture integration | NOT VERIFIED |
| P1 | Trusted exact v0.10.1 baseline, in-place schema8→10, personal/profile/settings/recovery bytes | Existing upgrade helper; disposable emulator; trusted signer or documented same-runner rebuild | NOT VERIFIED current candidate |
| P2 | Catalogue/Admin settings/markers/session persistence; account isolation | Existing store-recreation tests establish limited venue; upgrade needs applicable baseline | PASS limited native store recreation; upgrade NOT VERIFIED |
| C1 | Pinned bcc7796 provenance29 cases/15 hashes; legacy exact parsing and disabled application transport | Host/JVM/native; no shared-contract changes | PASS prior CI; recheck pending |
| C2 | Approved public adapter/session/current catalogue/dual revisions/audit validation policy | Stage6/owner freeze G1–G9/G11; M2/M3 provisional, not pinned authority | BLOCKED |
| O1 | Owner review of actual Admin experience and explicit acceptance | Consolidated package plus unresolved limitations | OWNER REVIEW pending evidence |

Stage7A FINAL ACCEPTED. X10 NOT FIXED/NOT VERIFIED, deferred to Gould0.12. Stage8 accessibility/readability/touch gates remain required.

## Host verification before dispatch

`./gradlew assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug --no-daemon --max-workers=1` SUCCESS2m6s;275 JVM tests/0 failures/errors/skips,36 suites. Final refined instrumentation `assembleDebugAndroidTest` SUCCESS21s. Lint0 errors/132 warnings/1 hint. Six host checks PASS; workflow dispatch-only YAML and every bash block syntax PASS. No new runtime body executed locally.

## Current source and running verification

Published source **63f7e2106897401e7da902b0d27066684d5f4abc**; normal push and exact remote readback PASS. [Acceptance CI40027522387](https://github.com/stokonn/Goldberg/actions/runs/40027522387) runs both existing jobs. No new runtime PASS is claimed until its results are inspected. [Dispatch checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6375168844). See [review sequence and exact next actions](REVIEW-RUNBOOK.md) and [contract verification disposition](CONTRACT-VERIFICATION-2026-10-23.md).

The only application-source change at dispatch is a default-compatible injectable AdminRepository constructor argument for AdminViewModel; production factory overload/default repository and all network/decision gates remain intact. Test fixtures live in androidTest; no personal database/schema/backup/practice behavior changes. Local APK identity is recorded in results/acceptance-preparation-2026-10-23/local-artifact.json; CI APKs use separate temporary runner signing.

## First coordinated run — historical failure retained

Run40027522387/source63f7e21: host275 JVM/0 failures; full instrumentation185 XML cases,182 passed/1 failed/0 errors/2 unchanged external-fixture skips (console187 discrepancy retained). Four original Admin native checks and both new scheduling tests PASS. Render test incorrectly demanded nonzero scroll at standard font1.5/2 even when content fit; corrected test now verifies actual action reachability while retaining strict no-scroll≤1.3 and font/locale assertions. Export copied zero files because UiAutomation command execution does not interpret quoted shell arguments. No Admin visual PASS is inferred; corrected byte-stream export now asserts exact readback. Raw evidence/digests retained under results/ci-run-40027522387.

Upgrade job failed before boot on invalid downloaded API36 image ZIP; zero upgrade checks, trusted release/baseline signing checks PASS. Bounded sdkmanager preflight preserves three attempt logs and fails closed; synthetic success/retry/final failure harness PASS. No shared SDK deletion or weakened assertions.

Source-audited accessibility defect: Admin switches and proposal checkboxes had separate visible text but no associated accessible name on the interactive node. Added localized semantic names only. New worker constructor seam retains the default two-argument WorkManager factory, with synthetic repository injection only in tests. Positive delivery, denied/granted permission, failed scan/revocation/cancellation/scope switch and offline/recovery checks require the next exact-source CI. Live networking/decisions remain disabled.
