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
- No identity, controller contact, legal or credential data was invented; no legal
  compliance is claimed; free hosting is not proof of compliance.
