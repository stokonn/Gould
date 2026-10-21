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
