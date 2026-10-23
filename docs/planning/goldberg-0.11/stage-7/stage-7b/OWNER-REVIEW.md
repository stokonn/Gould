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
