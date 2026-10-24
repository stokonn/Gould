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
