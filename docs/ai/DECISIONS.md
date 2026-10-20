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

## Stage 6 pointer (added 2026-10-21 on the Stage 6 planning branch)

Stage 6 (secured optional community backend) planning is prepared at
`docs/planning/goldberg-0.11/stage-6/`. This file does not change any decision above.

- **D4/D5/D8 remain UNRESOLVED owner decisions.** The complete alternatives,
  trade-offs, security implications and planning recommendations are in
  `stage-6/OPEN-DECISIONS.md`. The Supabase Free moderation-only pilot is a
  **proposal, not an approved backend selection**; Workers+D1 stays an unproven
  alternative; deferral/no-backend remains a legitimate outcome.
- Owner-approved on 2026-10-21: Stage 6 documentation preparation only
  (10 documents + additive checklist/STAGES/DECISIONS/HANDOFF updates on the isolated
  `docs/goldberg-0.11-stage-6-planning` branch). Explicitly **not** approved yet:
  implementation, provisioning, real Google OAuth configuration, commit/push (pending
  review), D4/D5/D8 finalization.
- No identity, controller contact, legal or credential data was invented; privacy,
  retention, licensing, audience/age safeguards and account deletion are recorded as
  explicit unresolved decisions requiring joint owner review before any collection.
- No legal compliance is claimed; free hosting is not proof of compliance.
