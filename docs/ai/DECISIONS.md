## Automatic Discover online search — owner decision, October 25, 2026

The persisted default-off Online Catalogue setting is consent. Local results appear immediately; permitted online provider results follow after approximately 500 ms for queries with at least two characters. Query/filter/consent changes cancel obsolete work. Local/online sections deduplicate by verified canonical ID, never title. Keep paging, cache, provenance, mobile policy and rate limits. Remove per-query Search online. No community production networking is enabled. Regression/native visual acceptance is required before this implementation is marked verified.

## Permanent release policy — owner decision, October 25, 2026

Applies to Goldberg 0.11 and all future Gould versions. This supersedes earlier automatic publication authorization and historical RC instructions below.

- During development, build debug APKs only for internal testing and owner review. Do not create new release candidates, prerelease tags or GitHub Releases for development APKs.
- Preserve the existing `v0.11-rc.1` release, tag and artifacts as immutable historical evidence.
- Only after full implementation, verification and explicit owner approval, perform a fresh clean release build. It must be non-debuggable, signed with the trusted production certificate, pass all release-specific gates and support verified safe in-place upgrades.
- Publish only the explicitly approved stable version. Merging into `main` and publishing a stable GitHub Release each require explicit owner approval; passing checks alone grants neither.
- The current Stage 8 Discover repair delivers a debug APK and verification report for owner review, with no release publication.


## Stage 8 — independent Android verification executed (October 25, 2026)

Independent checks are VERIFIED; source publication/automatic CI receipt is in progress. This continues existing Stage8,without a new named stage. Full Stage8 integration and Goldberg0.11 release acceptance remain INCOMPLETE.

Purpose/scope: trusted0.10.1 upgrade/data-preservation rehearsal, actual process/migration/restore/practice/reboot boundaries, missing orientation/platform observations and guarded artifact/readiness tooling. Baseline22fb50bedec7b932e47640673e52fdb57bdc791a on feature/goldberg-0.11-stage-8. Production source remains4d245b and APK68d48b…;only test/tool/docs/evidence changes. Accepted Stage7/Stage8C behavior retained.

- [x] Recover policy/PMP/AI/readiness/Issue/Project and remote/worktree state; preserve externally owned Stage6.
- [x] Trusted baseline854bbfeb…/certificate53eface… → unchanged development candidate68d48b… in-place install;schema8→10,all12 original tables/37sessions/37attribution rows/2pieces/4badges/profile/avatar/showcase/draft/archive/preferences retained;integrity/FK PASS.
- [x] Abandoned partial installer leaves baseline unchanged;normal install retry PASS. Actual migration process kill rolls back,then retries with every original typed cell intact. Old binary's10→8 downgrade is refused without loss;forward reinstall recovers.
- [x] V5 export/restore from upgraded journal,checksum-failure preservation/retry andfour actual restore kill phases/replay/retry PASS. Original destination recovery bytes remain exportable in evidence.
- [x] Actual monotonic practice:6.008s credited during6.221s observed,per-piece/interval sums exact;actual process death/reboot preserve stopped recovery,raw draft andno downtime credit;completion idempotent.
- [x] Actual reboot preserves WorkManager IDs/offline queue/READYblob;real cancellation PASS after54.159s (not3hdelivery). DataSaver/restricted bucket45/forcedLOW readbacks observed/restored;not actual ENOSPC/mobile transfer. Retain unchanged accepted SmartCache/interrupt/metered/notification and32-config UI evidence.
- [x] Actual EN/Dark portrait/landscape plusfour CZ/Dark font1.3/2 rotations;allfive tabs,eight keyboard-focus steps andIMEBack PASS. Eight selected original frames inspected via two review sheets.
- [x] Package/manifest/assets/ZIP/trusted signer/test-fixture exclusion/source traceability PASS. Rehearsal artifact isdebuggable0.10.1/code11;strict release guard correctlyBLOCKS it. No official version/signing/RC selection.
- [ ] Commit/push verified scope,read exact remote and relevant automatic CI;final append-only Issue/private PMP/AI/matrix receipt. No unchanged full native CI redispatch.

Counts:new12 targeted JUnit PASS,0new skips,6deliberate actual process terminations (notJUnit passes),1corrected invalid-UUID fixture failure retained,0unresolved app failures. New final AndroidTest compile PASS21s;no production/JVM logic changes. Retained CI40170295032:190native/188pass/2oldexternal skips and275JVM at its original source;not new executions here.

Stage6 refreshed read-only:receipt da2423bcea3b9ef8d0ea88a9b08a2e5468f173d6,verified engineering source be5104476cf6cb059b0f206b2c4f18027d6bc657/[CI40188291121](https://github.com/stokonn/Goldberg/actions/runs/40188291121). Its234JVM/20canonical/localarchive/TLS evidence is externally reported,not executed here;native/accepted-consumer/hosted acceptance remainsOPEN. Actual-client G1–G9/G11,C4,6.0f/6.17,real policy/publicversion/OAuth andB2/B4/B5/B6 remain blocked or pending;G10 disabled. No Stage6 source/DTO/auth/session/repository/fixture adoption or external writes.

Remaining gaps:final integrated/official-version/signed-release artifact acceptance;real three-hour/reboot notification delivery;audible speech/physical-device acceptance (not performed);real disk-full/production transfers (not performed). Existing accepted deterministic/transport evidence stays source-bound. Next shortest path:review final Stage6 consumer handoff/policy gates,obtain authorized integration scope,reconcile affected paths preserving current UI/data,then rerun only impacted combined-source/native/data checks plusfinal trusted-signer upgrade. No additional named stage,main merge,cloud activation,production networking,phone testing,tag orRelease.

created by stokonn with love <3

[Detailed commands,evidence,reproduction andlimits](../planning/goldberg-0.11/stage-8/INDEPENDENT-ANDROID-VERIFICATION.md).

---

## Stage 8C approved corrections and acceptance — October25,2026

Owner explicitly authorized A8-1/A8-2 production UI and A8-3 test-only corrections. Bounded milestone now COMPLETE/FIXED after actual observations and [independent native CI2](https://github.com/stokonn/Goldberg/actions/runs/40170295032) at `a41cd11a1b32af2949c43cb6e5b6aac94a3c2a51`;production correction source `4d245b46936d640740799dfcc6e8d61f3d44b249`. WholeStage8 integration/release remains blocked.

Use one merged48dp Switch row owner and a noninteractive visual indicator;retain exact names/roles/state/disabled/key/touch/actions. Use five equal responsive navigation columns with original editorial labelSmall metrics/icons/colors and full wrapping/native hyphenation;grow natural height,never shrink text or hide destinations. Share production navigation in test scaffolds;copy MainActivity-equivalent outer-theme window effect only in tests;no production theme change justified by the evidence.

CI1pointer rejection explicitly targeted SystemUI StatusBar in an unsafe isolated test layout. Correct safeDrawing padding and add a pointer-center/inset assertion;keep all other assertions. Local3casePASS preceded one meaningful CI2 dispatch. Do not erase failed attempts,wrong initial class/selected-tab assumptions or source provenance. Actual TalkBack focus is verified;optional double-tap injection/audible speech remain unverified. No unchanged quality rerun after test-only correction;final docs receipt skips redundant CI.

No DB/schema/backup/practice/backend/contract/version changes or Stage6 gate closure. Cloud56ff724 remains independent/hosted NO-GO;G1–G9/G11,C4,6.0f/live OPEN. No main/cloud/physical phone/deploy/release. Next recommend separately scoped Stage8D contract/consumer readiness,then approved source adoption/combined data/upgrade acceptance. [Current report](../planning/goldberg-0.11/stage-8/8C-TARGETED-UI-CORRECTIONS.md).

created by stokonn with love <3

---

## Stage 8B — first technical assessment verified, October 25, 2026

**TECHNICAL ASSESSMENT COMPLETE / SOURCE PUBLISHED. Full Stage8 integration/release PENDING/BLOCKED.** Branch `feature/goldberg-0.11-stage-8`, baseline2f4d1b8. Stage7 client/synthetic acceptance remains COMPLETE.

Automatic new-source quality [40147220442](https://github.com/stokonn/Goldberg/actions/runs/40147220442) **SUCCESS at394b6b58b2adcb0eb07b4f873161bbd7cc70ed3c**:clean Gradle build/JVM/androidTest compilation/lint PASS7m49s,83executed/1up-to-date;all six workflow validators PASS. Individual new JVM counts not exported;0 native suite executions. This supersedes earlier local-only no-full-host statements below;CI25 was not manually rerun. Documentation-only receipt skips redundant CI;source-bound success is not transferred to its later SHA.


Purpose/scope: reconcile accepted7A documentation, audit mainCI/13 Stage 6 overlaps, assess A8-1/A8-2/A8-3 offline, verify retained APK/source/upgrade prerequisites and map critical-data coverage. Production behavior/database/schema/contracts/CI unchanged; new opt-in instrumentation recorder/host evidence tools only.

- [x] Refresh16 worktrees/remotes/Issue31/milestone4/releases/Wiki/Goldberg Development; preserve all 2203 external original untracked files/APKs/PMP. Concurrent Cloud source 2c38177 / receipt c8b903b untouched, not adopted; PRIVATE STAGING NO-GO retained.
- [x] Reconcile9 Stage 7A documentation deltas:four isolated acceptance/follow-up files adopted, shared current dashboards preserve newer Stage7B/8 decisions. X10 NOT FIXED/NOT VERIFIED,deferred Gould0.12; material accessibility retained.
- [x] Audit main-onlyCI and classify all 13overlaps; stronger accepted Stage7B workflows retained; no source combination.
- [x] New test-recorder wrapper assembly PASS54 s; default runner manifest PASS13 s. App APK byte-identical93ae9ec…; trusted package/signature/ZIP/source/version/baseline review PASS,still0.10.1/code11.75 historical critical CI25 cases inventoried,not rerun.
- [x] Offline API36 emulator5588/qemu1:28 unique settings configurations (16small+4standard+4opposite-theme NavHost+4scaffold),29 captures including retry;161 PNG/tree pairs. Four real TalkBack traversals / 40 swipes with events. Emulator stopped; AVD/APKs retained.
- [x] A8-1 actual duplicate Settings stops and A8-2 actual large-font label ellipsis reproduced, bothMedium/P2,NOT FIXED. A8-3 scaffold statuscontrast~1.05–1.13:1 reproduced; matching real NavHost5.56–19.48:1,gesture5.74–15.25:1; no product contrast defect demonstrated in tested scope. Proposed fixes await separate implementation approval.
- [x] Evidence verifier,Python compilation,production-preservation and whitespace checks PASS. No new JUnit/full CI/lint/data-suite execution claimed; no audible speech/motion/full3h-reboot/live integration acceptance.
- [x] Source `394b6b58b2adcb0eb07b4f873161bbd7cc70ed3c` ordinary-pushed and remote SHA exact; [authoritative Issue checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6391483885), Issue OPEN / Project In Progress synchronized/read back. Final documentation receipt follows without changing runtime evidence.

[Full commands/results/limitations and next milestone](../planning/goldberg-0.11/stage-8/8B-TECHNICAL-VERIFICATION.md); [all 13risk resolutions](../planning/goldberg-0.11/stage-8/INTEGRATION-SEQUENCE.md). No production correction, networking activation, Android/backend combination, main merge, deploy or release.

Next: retain published bounded evidence; seek separate Stage8C scope for targeted row semantics/responsive labels/scaffold-window correction and affected before/after regressions. Stage6 G1–G9/G11,C4,6.0f/public-version/real-policy/live/production dependencies remain OPEN. Trusted-signer seeded0.11upgrade/version selection and broader release gates remain pending.

created by stokonn with love <3

---

## Stage8A preparation completion and publication receipt

**8A readiness preparation COMPLETE / packet PREPARED AND PUBLISHED. Stage8 integration/release acceptance PENDING/BLOCKED.** Published preparation commit `ea1758c6fb10c9f4cdecf83d4c47523e9a5b553a` on `feature/goldberg-0.11-stage-8`, ordinary push and exact remote readback PASS. [Existing Issue31 source checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6388549455) and Project overview/item readbacks PASS; Issue OPEN / umbrella In Progress. Documentation-only preservation checks passed; no runtime tests/builds/CI/merges/deploy/releases executed. Stage7 client/synthetic acceptance remains COMPLETE. Concurrent Stage6-Cloud WIP was recorded untouched, not adopted; all Stage6/live/production gates remain OPEN. Final handoff commit SHA is recorded in the next Issue31 receipt.

Verified8A documentation/preservation milestone: [Issue31 checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6388534366).270 source hashes,2203 original untracked files and Stage7B APK unchanged; document links/JSON/whitespace PASS. Concurrent Cloud WIP recorded untouched; no tests/builds/CI run. Publication/checkpoint checks completed; see the current receipt above.

# Stage 8A readiness preparation — October 24, 2026

**Stage7 Android client/synthetic acceptance COMPLETE and owner-approved. Stage8A preparation packet PREPARED; Stage8 integration/release acceptance remains PENDING/BLOCKED.** Current authorized scope: documentation, read-only inventories and separate worktree creation only. No source integration or live adoption is authorized.

Worktree `/home/stokonn/Projects/Goldberg-Stage8`, branch `feature/goldberg-0.11-stage-8`, preparation base `4c6a91d600ad18455a280f941f89740694d5afa0`. All15 prior worktrees/53 cached refs and fresh remote heads/tags/Issue31 comments were inspected first. Stage7A's sole unique acceptance commit is documentation-only; Stage6 diverges54/78 with13 potential path overlaps; main diverges81/11. [Readiness packet](../planning/goldberg-0.11/stage-8/README.md), integration sequence, release checklist, accessibility assessments, Stage6 ledger and20-row verification matrix are prepared. Existing Stage7 evidence/PMP remains in its owning worktree; no copying, deletion or reset.

Stage6 G1–G9/G11,C4,6.0f, Android/backend live integration and all production authorization gates remain OPEN. G10 is closed for disabled transport/actions only. Duplicate Settings TalkBack stops, large-font bottom-tab truncation and scaffold contrast remain NOT FIXED, awaiting explicit Stage8 release assessment. Speech/motion/exhaustive accessibility/real three-hour-reboot delivery remain UNVERIFIED. X10 remains under its existing Gould0.12 deferral.

No tests/builds/emulators/live calls/merges/deploys/releases executed in8A. Earlier source-bound CI25/275JVM/262pairs/seeded-upgrade evidence remains accepted within Stage7 scope; no new candidate exists. Next: refresh input HEADs/concurrent published work and obtain separate implementation/integration scope before source adoption or new runtime work. Blocked Stage6 gates cannot be silently omitted; production authorization remains separate.

created by stokonn with love <3

<details>
<summary>Earlier accepted Stage7 and verification checkpoints — historical evidence retained</summary>

# Stage 7 owner acceptance — October 24, 2026

**Stage 7B: ACCEPTED WITH DOCUMENTED LIMITATIONS. Stage 7 Android client/synthetic acceptance scope: COMPLETED.** Stage 7A was already owner-accepted. The owner explicitly approved the October 24 final owner report in this conversation; [permanent owner-decision checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6388393525). This decision supersedes the earlier READY FOR OWNER REVIEW / pending-approval status. It covers the verified Android client/synthetic scope only.

Purpose/scope: close owner acceptance of the existing Android Admin dashboard, Queue/re-review/decision safeguards, Audit, Settings, local sync/notifications and documented synthetic data safeguards. This update changes documentation only; application/test/CI source, the APK and historical evidence remain unchanged.

- [x] Record explicit owner acceptance of Stage 7B with the October 24 documented limitations; Stage 7A + Stage 7B Android client/synthetic acceptance scope completed.
- [x] Retain prior results without rerunning unchanged tests: CI #25 `40097540179` SUCCESS at tested source `fb1d3f971ed79de09dccbdc771e1c45a09dc0a6c`, 187 native cases /185 passed /2 existing skips /0 failures or errors, all9 Stage7B cases;262 screenshot/tree pairs reviewed; seeded12 collections/37 sessions/schema8→10 preserved. Prior275 JVM tests and seven validation scripts passed; normal restored assembly/lint passed (0 errors/119 warnings/1 hint). Representative October24 EN/Light/1.0 and CZ/Dark/1.3 emulator TalkBack/touch/Back/keyboard/IME/reduced-motion/permission-revocation observations retained. No fresh execution is claimed.
- [x] Retain exact manual tooling archive, default AndroidJUnitRunner, production APK and source manifest. Pre-decision published documentation HEAD `9f6adf806bff034e884d12bbb4259f920975248d`, branch `feature/goldberg-0.11-stage-7b`; the final publication checkpoint records this decision's documentation SHA.
- [x] Owner-approved deferral to Stage 8 for explicit release-readiness assessment: duplicate Settings TalkBack focus stops, large-font bottom-tab truncation and scaffold contrast findings. **Deferred, NOT FIXED**; acceptance does not waive Stage 8 assessment.
- [ ] **UNVERIFIED:** audible speech/pronunciation, subjective motion comfort/frame timing, exhaustive accessibility/font/keyboard/configuration coverage and real three-hour/reboot notification delivery. These checks remain unexecuted or bounded as originally recorded, not passed by owner approval.
- [ ] **OPEN:** Stage 6 G1–G9/G11, C4, 6.0f, Android/backend live integration and every production authorization gate. Live integration remains blocked pending separate authorization. **Stage 8 remains PENDING.**
- [ ] Separate integration/release verification and explicit authorization for any main merge, production activation or release remain outstanding. Issue #31 stays OPEN; Goldberg Development's umbrella item stays In Progress.

Next executor: retain the accepted Stage 7 client/synthetic disposition; assess the three deferred findings only within separately authorized Stage 8 release-readiness work. Do not mark them fixed, infer unverified checks passed, reopen completed Stage 7 tests or treat acceptance as live/backend/release authorization. Original raw observations/hashes, screenshots, logs, test results and private PMP are preserved. Earlier packets below are historical records, not the current acceptance status.

created by stokonn with love <3

# Goldberg 0.11 — DECISIONS (Stage 5 view)

## API v1 O1–O8 — owner design outcomes, 2026-10-23

O1/O2/O3/O5/O8 approved; O4/O6 approved AS DESIGN with qualifications;
O7 PARTIALLY APPROVED BY DESIGN only. Durable canonical approval:
`docs/planning/goldberg-0.11/stage-6/API-V1-OWNER-DECISIONS-2026-10-23.md`.
Europe/Prague is the approved server daily-statistics calendar, UTC timestamps;
NFC case-insensitive/diacritic-sensitive literal search. Exact token durations,
24h receipts, scoped cursor15m,204 logout/no persisted secret queue, independent
revisions and explicit open are approved design; implementation remains unauthorized.

Still pending: initial-v1 compatibility confirmation (otherwise separate v2 decision),
later measured query budgets, all concrete production source/field/license/attribution/
evidence policy approvals and separate implementation/integration authorization.
All real policy sets empty/default-deny. G1–G9/G11 integration BLOCKED; G10 CLOSED;
6.12 PARTIAL/T22 LOCAL PASS and345 prior executions unchanged;6.17 NOT FROZEN.
No changes to older D4/D5/D8/Q1/cloud/legal decisions or current runtime behavior.
Earlier “O1–O8 pending” statements below are historical, superseded by this checkpoint.


Pointer register — this file does not replace the canonical decision table in
`docs/planning/goldberg-0.11/stage-2/READINESS.md` or `docs/planning/goldberg-0.11/STAGES.md`.

## Owner-approved decisions governing Stage 5

| ID | Decision | Where it lands | Source |
|---|---|---|---|
| D5.1 | File-based manifests (not DB rows) with mandatory per-file rights | `OfflineBundle.kt`, `BundleManifestCodec.kt` (task 5.1) | Owner-approved; STAGE-CHECKLIST Stage 5 header |
| D5.3 | WorkManager for background scheduling (owner-mandated) | `BundleScheduler.kt`, `BundleDownloadWorker.kt` (task 5.8) | Owner instruction; STAGE-CHECKLIST Stage 5 header |
| D5.6 | Test-only org.json dependency (no production org.json) | androidTest/test Gradle additions | STAGE-CHECKLIST Stage 5 header |
| D7 | Measured storage caps + free-space reserve + per-job metered consent; no automatic mobile background downloads; prompt thresholds provisional (256 MiB limit / 8 MiB large-job / dynamic reserve, tunable in Stage 5) | `BundlePolicy.kt`, `BundleNetwork.kt` (task 5.3) | `stage-2/READINESS.md` D7 row; STAGE-CHECKLIST |

D4/D5/D8 (backend/Admin/cloud gates) remain unresolved later-stage dependencies and are out of
scope for Stage 5.

## Session-local test decisions (2026-10-20, test-only — no production change)

1. `Harness.published`/`stateOf` made `suspend`; consent-test `refresh` + shared `BundleJobStore`
   hoisted above the harness lambda; `harness()` accepts optional `jobs`. Rationale: suspend
   store reads were being called outside a coroutine context (compile errors).
2. Interruption fixtures use `chunkSize = 8` so `failAfterReads` fires mid-stream (the fake only
   drops when `reads > failAfterReads`; EOF reads are not counted).
3. Low-storage fixture reserves `usableSpace − 512 KiB` with a 1 MiB payload, so the manifest
   write fits but content does not (`reserve == usableSpace` would refuse even the manifest).
   Assertions unchanged.

## Owner decisions on F1/F2 (2026-10-20, resume session — both APPROVED as recommended)

- **F1 — retarget the test to the reachable contract (test-only, no production change).**
  Implemented: `invalidRightsQuarantineInsteadOfRetrying` replaced by
  `invalidRightsOnDiskQuarantineManifestAndDeactivatesJob`: a valid manifest is encoded, its
  `license` field blanked, and the JSON written straight to `manifests/` (simulating on-disk
  rights corruption, since D5.1 makes invalid rights unreachable through `save()`/`encode()`).
  Asserts: outcome `Permanent(MANIFEST_MISSING)`, job deactivated with that `lastError`, no
  published manifest, no active state, tampered bytes preserved in quarantine. No production
  change; codec validation stays as designed.
- **F2 — apply the minimal `BundleStore.installBlob` fix (production change, owner-approved).**
  Implemented: the dedup branch now verifies an existing target's length + sha256 against the
  staged copy before deduplicating. Valid target → dedup as before; invalid target → bytes
  moved to quarantine (`DEDUP_CORRUPT_REASON = "dedup_corrupt"`, never silently deleted —
  inline because `quarantineBlob` would deadlock on the already-held non-reentrant mutex), then
  the verified staged copy is installed so the **same attempt** repairs the corruption. If the
  quarantine rename fails → return `false` (fail-safe; caller turns that into a durable
  Retryable). Scope: ~12 lines in `BundleStore.kt`, no DB/backup/schema/signing impact.
  Verification: existing `corruptStoredBlobIsQuarantinedAndRepairedOnRetry` test + emulator run.

See `docs/ai/STATE.md` for the full investigation record of both findings.

## Stage 6 pointer (added 2026-10-21 on the Stage 6 planning branch; updated same day)

Stage 6 (secured optional community backend) planning is prepared at
`docs/planning/goldberg-0.11/stage-6/`. This file does not change any Stage 5 decision
above.

- **Owner architecture decision review COMPLETED 2026-10-21.** Outcomes recorded in
  `stage-6/OPEN-DECISIONS.md` (the authoritative register) and synchronized across
  the Stage 6 package:
  - **APPROVED:** D4a Supabase Free (one optional backend, strict 0 CZK/month, no
    paid plans/trials/auto-upgrades/billing escalation); D4b accept Free-tier
    inactivity suspension and quota limits — community features degrade gracefully,
    core local functionality stays operational, fail closed when unavailable;
    D5c Supabase region Frankfurt `eu-central-1` (verify availability, residency,
    transfers, DPA, subprocessors before provisioning; EU hosting ≠ all-processing-
    in-EU guarantee); D5f general-audience moderated community (no DMs, no public
    profiles, no unnecessary birth-date/age collection, public browsing without
    login, moderated contributions); D5g owner-only moderation initially with
    server-side authorized + audited grants/decisions, no self-approval;
    D8a Android Admin integrated in the existing app (four tabs preserved, hidden UI
    is not a security boundary, no separate Admin APK); D8c manual Supabase-console
    admin bootstrap after account verification, MFA-protected privileged operator
    access, audited grant/recovery, controlled out-of-band recovery.
  - **APPROVED CONDITIONALLY:** D8b Web Admin BFF (same-origin HttpOnly/Secure/
    SameSite=Strict cookies, server-side authz/session revocation/CSRF, strict CSP,
    no privileged bearer tokens in browser JS) — **before implementation, prove the
    0 CZK fit of the chosen BFF hosting/runtime; otherwise report the blocker and
    request an owner decision — never silently replace the architecture.**
    *Feasibility study executed 2026-10-21 (task 6.0f):
    **CONDITIONAL PASS** — `stage-6/BFF-FEASIBILITY.md` recommends a single-origin
    Cloudflare Workers Free Worker (static + BFF) in front of the Supabase Free
    data plane; a Supabase-only same-origin host is blocked at 0 CZK. Conditions
    C1–C4 open (owner acceptance of the Cloudflare host, measured CPU spike,
    D5b/D5c edge-location review, terms re-verification). No account or deployment
    was created.*
    **Updated 2026-10-21 (same day, later session):**
    - **C1 APPROVED** — the owner approved Cloudflare Workers Free as the D8b BFF
      hosting/runtime: Web Admin static assets + `/v1` BFF on one HTTPS origin;
      Supabase Free remains the data and authentication backend in Frankfurt
      (`eu-central-1`); no paid plans, automatic billing, chargeable trials or
      upgrades. **Architecture-decision level only — no provisioning, deployment
      or implementation authorized.** Recorded in `stage-6/OPEN-DECISIONS.md` §4a.
    - **C2 approach APPROVED FOR PLANNING ONLY** — the owner approved an isolated,
      fully local CPU/authentication feasibility spike (Cloudflare
      Workers-compatible runtime `workerd` via Wrangler local mode, synthetic
      identities, simulated Supabase/JWKS services, fake clock, latency/failure
      injection). A complete test specification was produced at
      `stage-6/C2-SPIKE-PLAN.md` (local runtime design, auth-consumption contract
      that keeps Supabase Auth as the identity provider, synthetic-only fixtures,
      S1–S19 security matrix mapped to THREAT-MODEL T1–T5/T15/T20, CPU-vs-wall-clock
      methodology, provisional P50≤3/P95≤5/P99≤8 ms targets vs the 10 ms Workers
      Free ceiling, PASS/CONDITIONAL PASS/FAIL/INCONCLUSIVE outcomes, stop
      conditions and rollback, an owner-gated future production-runtime
      verification gate). **The spike remains UNEXECUTED** *(historical statement
      from the planning checkpoint — superseded by the executed CONDITIONAL PASS
      recorded immediately below)* — no spike code, harness,
      fixtures, dependencies or tests exist or were run; executing it requires a
      separate explicit owner authorization.
    - C3 (D5b/D5c edge-location review) and C4 (terms re-verification) remain
      open; task 6.0f stays unchecked; Stage 6 implementation remains NOT STARTED.
    - **Updated 2026-10-21 (later sessions):**
      - **C2 SPIKE EXECUTED — CONDITIONAL PASS.** With separate owner
        authorization the isolated local spike ran on
        `spike/goldberg-0.11-c2-bff` (`workerd` via Wrangler local mode,
        loopback-only, synthetic identities; spike code commit `d2ce77b`,
        final docs commit `7bc6976`): security **19/19 passed**, performance
        **P1–P7 executed**, kernel-measured **average CPU 2.03
        ms/invocation** (~4.9× under the 10 ms Workers Free ceiling).
        **Preserved without exaggeration:** per-invocation production CPU is
        **unverified** (percentiles rest on conservative local upper bounds)
        and the §12 production-runtime verification remains **owner-gated**.
        Spike code was **not merged** into the planning branch or `main`.
        Report: `spikes/bff-c2/results/SUMMARY.md` (on the spike branch).
      - **C3 DOCUMENTARY REVIEW COMPLETE (documentation-only) →
        [stage-6/C3-EDGE-PRIVACY-REVIEW.md](../planning/goldberg-0.11/stage-6/C3-EDGE-PRIVACY-REVIEW.md):**
        primary-source review of Cloudflare Free (global edge in 330+ cities,
        metadata processed in US/EU core data centers, Workers Logs
        default-on with 3-day Free retention, DPA v6.4 incorporated by
        reference into the self-serve terms, DPF + EU SCCs, Developer Platform
        subprocessors incl. Google/Oracle, Data Localization Suite
        Enterprise-only) and Supabase (Frankfurt `eu-central-1` pins DB/Auth/
        backups but explicitly does **not** guarantee region-exclusive
        processing — support/account/telemetry flows to Singapore/US under
        SCCs; DPA v1; Free-plan pause/backup/email limits). Verdict:
        **conditionally viable at 0 CZK; NOT a compliance certification**;
        every configurable risk has a Free-tier mitigation (no paid feature
        needed); EU-only edge metadata processing is impossible on Workers
        Free and is disclosed as a constraint, never absorbed silently.
        **Legal/operator questions remain open** — controller/operator
        identity (D5b), heightened because the project owner is a minor
        (both providers require legal capacity to bind the account →
        adult/professional review before provisioning), retention details
        (D5d), privacy-notice content (D5a/6.16). C3 closes no legal or
        provisioning gate.
    - C4 (terms re-verification) and the C2 §12 production-runtime
      verification remain open; task 6.0f stays unchecked; Stage 6
      implementation remains NOT STARTED.
    - **2026-10-21 Q1/C4 research session (documentation only — NO new owner
      decision, no status change):** `stage-6/Q1-OPERATOR-READINESS.md` prepared —
      verified facts (neither provider's current terms publish a minimum account
      age for a first account; authority-to-bind warranties in CF DPA v6.4 /
      Supabase ToS v2; CF SSSA §2.2.1(a); Czech Civil Code §§30–33 incl. the §33
      court-approval question; Google parental-permission rule), unresolved
      questions, professional-review question set, and **operator Options A/B/C
      presented without selection**. **D5b stays DEFERRED/unresolved and blocking
      for 6.0d**; the agent chose no operator, drew no legal conclusion, requested
      no identifying information. `stage-6/C4-PREPROVISIONING-CHECKLIST.md`
      prepared as a **pre-provisioning** baseline (URLs, versions, access dates,
      re-verify-on-day columns, §D runbook) — **C4 final verification stays OPEN**.
      Retrieval failures recorded: EUR-Lex GDPR text (0 bytes ×2), Czech MoJ
      official English Civil Code PDF (TLS error). Zero-cost/legal assumption
      checks preserved: any failure → owner blocker report, never a silent paid
      substitution.
  - **PROVISIONAL:** D5a personal contact email is the intended public contact, but
    **no address was supplied or authorized — do not invent/expose/request/publish
    contact data**; verify legally required controller identification before launch.
  - **DEFERRED / DETAILS PENDING:** D5b controller/operator determination before
    public launch (privacy-by-design and minimization meanwhile; no GDPR-compliance
    claim; no real-user collection; account for requirements concerning a minor
    project owner); D5d principle approved (preserve legitimately published
    non-personal catalogue data, minimize/erase account-linked personal data,
    support account deletion) but retention periods and deletion semantics remain
    unresolved — invent no periods, claim no legal approval; D5e licensing (CC0
    structured metadata / CC BY-SA 4.0 contributor text) approved subject to
    pre-launch legal review — no automatic relicensing of Wikipedia/third-party
    prose; field-level provenance preserved.
- Owner-approved on 2026-10-21: Stage 6 documentation preparation **and** the
  decision-review documentation synchronization (documentation-only commits on
  `docs/goldberg-0.11-stage-6-planning`). Explicitly **still not authorized**:
  implementation, provisioning, Google OAuth configuration, public launch/collection.
- Stage 5 is complete and owner-approved (2026-10-21) on
  `feature/goldberg-0.11-stage-5` (completion commit `736f6d3`); the stale Stage 5
  "in progress" references on this planning branch were reconciled additively without
  touching Stage 5 code or evidence.
- **2026-10-21 cloud-readiness session (documentation-only, later same day):**
  - **D-path reconciliation (owner instruction):** the approved **cloud
    architecture is the intended Stage 6 deployment path** — Cloudflare Workers
    Free (same-origin Web Admin static + `/v1` BFF) + Supabase Free PostgreSQL/
    Auth in `eu-central-1` (Frankfurt); permanent **0 CZK/month**; no paid plans,
    chargeable trials, automatic upgrades or unapproved billing — **instead of
    the temporary local-only Option C**. Option C is now only the current gated
    posture. Documentation/verification **planning only**: this authorizes no
    provisioning, account creation, OAuth configuration, deployment or
    implementation.
  - **"Minor owner" premise reclassified:** earlier C3/Q1 statements that the
    owner is a minor are an **unverified assumption, not a verified current
    fact**; no age, birth date, identity or family detail may be requested,
    recorded or inferred. **D5b stays legally unresolved and blocking for 6.0d**;
    account eligibility, legal capacity, authority-to-bind and the GDPR
    controller role all still require professional verification (Q1 §4), and
    account registration would not establish compliance.
  - **Gate-ordering proposal (PENDING OWNER DECISION):** the apparent 6.0f ↔
    provisioning circle is analyzed in `stage-6/CLOUD-READINESS-PLAN.md` —
    explicit minimal sequence S1–S8 and alternatives **ORD-A** (scoped
    owner-authorized limited-private verification provisioning → synthetic-data
    production-runtime measurement → 6.0f accept/reject → separate
    implementation authorization), **ORD-B** (close 6.0f on local C2 evidence,
    defer §12 to the [I] venue), **ORD-C** (full 6.0d before 6.0f closes). No
    option adopted; no gate changed, checked or bypassed; 6.0f stays unchecked.
  - The same document specifies the future private-cloud verification runbook
    (CR-0…CR-8: account readiness + MFA/secrets, day-of C4, minimal private
    infrastructure in `eu-central-1`, CPU/cold-start/Supabase-latency/quota/
    fail-closed/logging measurements, mandatory recorded rollback/cleanup;
    **synthetic test data only**), Free-tier usage estimates (0 CZK, no billing
    method) and binding stop conditions.
  - **ORD-A APPROVED (2026-10-21, later session — sequencing only,
    documentation-only):** the owner selected **ORD-A — Scoped Private Cloud
    Verification Provisioning** as the sequencing strategy for closing task
    6.0f; **ORD-B and ORD-C are NOT selected**. Recorded in
    `stage-6/CLOUD-READINESS-PLAN.md` §§3–5: the approved 11-step sequence
    (documentary readiness → Q1/D5b resolution → acceptable operator
    arrangement → separate phase-scoped authorization for limited private
    verification provisioning → day-of C4 → minimum private test resources →
    C2 §12 synthetic-data production-runtime verification → resource removal,
    credential revocation and recorded cleanup → explicit owner acceptance or
    rejection of 6.0f → separate Stage 6 implementation authorization →
    pre-launch legal/privacy gates), the reconciled gate table, and the ORD-A
    readiness checklist for the future owner decision. **Clarifications:**
    6.0f remains a pre-implementation gate — narrowly scoped verification
    provisioning may precede its closure only after its own prerequisites and
    a separate, phase-scoped authorization; ORD-A is **not** acceptance of
    6.0f, **not** a D5b resolution and **not** authorization to create
    accounts, provision, deploy, configure OAuth or implement; the permanent
    0 CZK/month requirement (no billing methods, paid features, chargeable
    trials, auto-upgrades) applies to every step; a 0 CZK failure is an owner
    blocker report, never a silent upgrade. **ORD-A (gate ordering) is
    distinct from Q1 Options A/B/C (operator arrangements), none selected.**
    A concise owner-facing list of the Q1 §4 professional-review questions
    (identity-free) is prepared in CLOUD-READINESS-PLAN.md §9.1 for routing
    outside GitHub.
  - **Q1 reassessment + product direction recorded (2026-10-21, later session
    — documentation-only, owner statements recorded, NO option selected):**
    - **PD-1 (owner's product direction, verbatim intent):** Goldberg is
      currently intended to remain **free of charge**. Commercial features may
      be considered in a future version, but **no monetization is approved or
      implemented now.** Recorded in Q1 §1.7; it changes no gate and authorizes
      nothing.
    - **Option set now A/B/C/D (documentation analysis, not a decision):**
      candidate **Q1-D — a legally capable natural-person operator acting in
      their own name** — was added for evaluation, with a §5.5 matrix and a
      §5.6 technical (non-legal) recommendation. **The owner has selected
      nothing**; Q1-D is a candidate only, its prerequisites (Q4.9–Q4.13) are
      open professional questions, and legal capacity is treated purely as an
      **eligibility precondition for the eventual accountable party — no
      personal fact requested, recorded or inferred**.
    - **Unchanged:** D5b unresolved/blocking for 6.0d; no GDPR compliance
      claimed; professional review still required; ORD-A sequencing-only; C4
      open; C2 §12 open; 6.0f unchecked; 6.0d/6.0e not authorized; 0 CZK/month
      permanent; architecture = Cloudflare Workers Free + Supabase Free
      `eu-central-1`.
- No identity, controller contact, legal or credential data was invented; no legal
  compliance is claimed; free hosting is not proof of compliance.


## Owner-authorized Stage6 end-to-end continuation — October25,2026

Owner authorizes remaining local engineering,meaningful verification,scoped commits/
ordinary push and Issue31/private PMP updates. Supabase/Workers Free,existing
Frankfurt project/account,0CZK,no-card,no journal sync and bounded Q1-D/D5b decisions
are final and retained. Stage7 acceptance and Stage8 integration scope are preserved.
Owner explicitly confirms current/upcoming charges/cards/add-ons/trials remain
unverified,final durable encrypted backup destination/separate custodian unapproved,
and no blanket C4 privacy/retention/transfer disposition. C4 remains8/19/6.
Hosted ACL/schema/role/Worker/secret/session/configuration writes require one later
specific consolidated authorization after hard prerequisites pass. No approval is
inferred from this continuation; no hosted write or real-user activation occurred.
Final Stage6 local engineering/acceptance evidence:stage-6/END-TO-END-ACCEPTANCE.md.

## Owner decisions on the two open Stage 5 acceptance items (2026-10-21, post-green session)

Context: after run 39557624554 was fully green and the coverage mapping proved that
consent revocation mid-stream + low disk were already executed on the emulator inside
the 177-test suite, exactly two acceptance items remained genuinely open. Both were put
to the owner with the honest constraints; both answered:

1. **UI screenshots in both themes — APPROVED: "Build + dispatch the job".**
   A purpose-built, dispatch-only CI job (`.github/workflows/stage5-screenshots.yml`)
   boots the same proven API 36 / pixel_6 / KVM emulator configuration, installs the
   candidate debug APK, drives the real UI (uiautomator accessibility tree; onboarding
   dismissed via its own Skip button), switches the product's own persisted theme
   (`profile_settings.theme` in `opus.db`: `Light` = Ivory, `Dark` = Nocturne — written
   via `run-as` + `sqlite3` with readback assertion, app relaunched through its normal
   startup path), and captures 12 screencaps (Home/Repertoire/Discover/Practice/Profile/
   Settings × both themes) with SHA-256 manifest. Same safety rules as the green
   workflow: dispatch-only, contents read-only, no production credentials, explicit
   `emulator-NNNN` + `ro.kernel.qemu=1` guards, SHA-pinned actions, `HEAD == GITHUB_SHA`
   assertion, no tests re-run, nothing published.

2. **Real bundle-transfer disk/network costs — DECISION: "Document measured costs +
   blocker" (recommended option).**
   The requirement is dispositioned with the measured evidence that genuinely exists,
   plus a formal record of why real-CDN numbers cannot be produced today:
   - **Measured (executed) evidence accepted as the Stage 5 answer:** the run-6
     instrumented suite exercised the real transfer engine end to end with exact byte
     accounting (network bytes == manifest-declared bytes; staged/published file bytes;
     admission refusal before any byte moves on low storage; mid-stream cancellation
     byte accounting) on the emulator with real file I/O, plus `disk-usage.txt`
     emulator snapshots.
   - **Structural blocker (documented, not fabricated):** the product has **no
     production bundle feed/CDN URL and no production enqueue path** — verified:
     `BundleScheduler` has no non-test caller, no bundle URL exists anywhere in
     `app/src/main`, and the user-facing download management UI is Stage 7 scope.
     Real-CDN transfer costs therefore become measurable only when the bundle feed
     exists (Stage 6/7 dependency) and must be measured then, against the real host.
   No numbers were invented; nothing was claimed as a real-CDN measurement.

Both decisions were recorded in the Issue #31 checkpoint thread as well. Stage 5 stays
IN PROGRESS until the screenshot artifacts are captured + inspected and task 5.13
finalizes.

## Owner approval — Stage 5 completion claim APPROVED (2026-10-21, closing checkpoint)

The owner approved the Goldberg 0.11 Stage 5 completion claim based on: the
verified CI evidence (attempt-6 run 39557624554 and green screenshot run
39576173002), the completed checklist (5.10–5.13), the archived screenshots
(`results/ci-run-39576173002/`) and the final documentation. Actions taken:
GitHub Project item #31 moved to **Done**; final approval checkpoint appended to
Issue #31 (permanent audit trail preserved); docs synchronized.

**Carried-forward production-CDN limitation (future integration dependency,
explicitly preserved):** real bundle-transfer disk/network costs against a
production CDN remain **unmeasurable until a production bundle feed/CDN URL and
enqueue path exist** — none exists in the product today (`BundleScheduler` has
no non-test caller; no bundle URL in `app/src/main`; download UI is Stage 7).
The accepted Stage 5 evidence is the executed tests' exact byte accounting +
real file I/O + emulator `disk-usage.txt` snapshots. This MUST be re-measured
against the real host when the bundle feed ships (Stage 6/7); no numbers were
fabricated.

Explicitly NOT authorized by this approval: merging Stage 5 application code
into `main`, tags, APK/release publication, cloud service provisioning, OAuth
configuration, Stage 6/7 implementation. Each remains a separate owner decision.

## Owner tracking correction — Issue #31 is the RELEASE umbrella, reopened (2026-10-21)

The owner identified a tracking inconsistency: GitHub Issue #31 is the **umbrella issue for the entire Goldberg 0.11 release** (Stages 1–8), but it had been closed (and the GitHub Project item set to Done) when the **Stage 5** completion was approved. Because Stages 6–8 have not started, closing the release umbrella was incorrect.

**Correction applied:**
- Reopened Issue #31 (it stays **OPEN** as the release dashboard).
- Set the umbrella GitHub Project item to **In Progress** (back from Done). **Stage 5 itself stays Done** in its stage checklist; no duplicate issue or project item was created.
- Updated the Issue #31 description: master checklist now shows Stages 1–5 **complete**, Stages 6–8 **pending**, and **Goldberg 0.11 overall IN PROGRESS**; the stale Stage-5 "in-progress CI-attempt" status was replaced with the verified final results, while the historical audit trail (checkpoint comments, stages 1–4 summaries) is preserved unchanged.
- Synchronized STATE/HANDOFF/VERIFICATION/DECISIONS + master checklist + this register.

**Unchanged:** Stage 5 remains officially **COMPLETE and owner-approved**; it is NOT reopened or rerun. Still not authorized: merging application code, tags, releases/APK, cloud provisioning, OAuth, or Stage 6/7 implementation.

</details>
