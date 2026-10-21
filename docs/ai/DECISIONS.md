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
- No identity, controller contact, legal or credential data was invented; no legal
  compliance is claimed; free hosting is not proof of compliance.
