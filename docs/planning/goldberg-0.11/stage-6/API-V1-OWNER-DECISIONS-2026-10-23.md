# API v1 — owner design approval checkpoint, 2026-10-23

Record `admin-v1-decision-1-owner-approval-2026-10-23` records the owner's explicit
O1–O8 instruction for proposal `admin-v1-decision-1`, published baseline
`fefa6e83c2a7b9c4c12660ff1542ab086356de88`,
[proposal checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6371392069).
The published proposal and earlier failures/checkpoints remain immutable in Git.
This checkpoint supersedes its pending-design choices only. **Approved design is
not implemented behavior, production policy approval or API freeze.**

| Decision | Recorded owner outcome |
|---|---|
| O1 — session | APPROVED: explicit versioned HTTP lifecycle and schemaVersion mapper; logout204 with no JSON; clear local credentials immediately even offline; no secret-bearing offline logout queue; never claim unconfirmed server revocation; preserve live revocation and account isolation. This approves the documented AUTH.md logout-queue amendment by design; old source is retained as historical evidence. |
| O2 — durations | APPROVED: access15 minutes, absolute family7 days, challenge5 minutes, provider clock skew≤60 seconds; atomic refresh rotation, replay-family revocation, single-flight refresh and fresh authentication after lost refresh response. Current code/config is unchanged. |
| O3 — moderation | APPROVED: separate expectedEntityRevision/expectedSubmissionRevision; explicit open; correct200/201 typed results; generic409 followed by refetch and explicit reconfirmation; exact receipts retained24 hours. No inferred revision, silent conflict retry or approval without live authorization. |
| O4 — wire | APPROVED AS DESIGN: canonical decimal strings preserve the signed int8 storage domain with existing field-specific positive/nonnegative ranges; UTC six-digit fractional timestamps, DTO projections, scoped signed cursors with15-minute TTL, public404/405 without privileged routes. Numeric/HTTP implementation requires separate authorization. `/v1` is conditional on confirming no previously approved/deployed incompatible consumer; otherwise prepare `/v2` for a separate owner decision. No global consumer absence is established here. |
| O5 — daily statistics | APPROVED: Europe/Prague civil calendar days; server derives local midnight boundaries including DST and transmits UTC instants. One coherent snapshot; in_review->approved counts once, approved->published adds zero. Incomplete/estimated counts remain unavailable. The earlier recommended UTC-day alternative was not selected. |
| O6 — search | APPROVED AS DESIGN: reviewed nullable metadata, bounded server filters, scoped cursors, literal substring matching; NFC normalization; case-insensitive and diacritic-sensitive; Czech/English and literal percent/underscore tests. No fuzzy/transliterated or out-of-scope completeness promise. Final query budgets await local measurement. |
| O7 — validation/policy | PARTIALLY APPROVED BY DESIGN: validation structure,1000 UTF-8-byte reasons, authorized current-value projection and strict allowlisting. No concrete real-world source, field, license, attribution or evidence policy is approved. All production policy sets remain empty/default-deny. Synthetic examples grant no permission; no legal clearance. |
| O8 — sequencing | APPROVED: documentation, durable decision updates, deterministic planning-only fixture verification, verified commit and publication attempt. No backend implementation, Android integration, HTTP activation, cloud/OAuth production,6.17 freeze, formal6.13–6.18 implementation, merge, deployment or release. No Stage7B edits or device operations. |

## Verification and limits

Deterministic planning checks PASS:61 original DTO scenes/8 protocol scenes,4 Prague
civil/DST windows,10 literal CZ/EN search cases and18 verifier regressions. Original
41 primary/29 comparison fixture/provenance/read-only Android hash checks, agent policy
and whitespace PASS. Outputs in `contracts/admin/v1/decision-proposal/`
`design-approval-2026-10-23/verification.json`. No SQL/HTTP/Android execution.
Host timezone/Unicode checks do not prove server collation or query/snapshot behavior.

## Remaining work and gates

- [x] Record explicit owner authority and exact qualifications; machine-readable record
  lives at `contracts/admin/v1/decision-proposal/design-approval-2026-10-23/approval.json`.
- [x] Distinguish current approved design from unimplemented public behavior and
  historical proposal fixtures; retain the original UTC example as historical only.
- [ ] Confirm `/v1` consumer compatibility before choosing the initial public version;
  an incompatible consumer requires a new `/v2` owner decision. No shape guessing.
- [ ] Measure final query budgets later under separately authorized local work.
- [ ] Review and explicitly authorize concrete production field/source/license/
  attribution/evidence policies; until then all remain empty/default-deny.
- [ ] Obtain separate authorization for backend numeric/HTTP implementation, Android
  integration and future acceptance. **G1–G9/G11 integration remains BLOCKED.**
- [ ] Formal6.12 remains PARTIAL; T22 LOCAL PASS and prior345 executions/313 tests/
  205 PG-backed bodies preserved, not rerun. Browser/fetch/restore/privacy/hosted/
  Android runtime and cloud/zero-cost/legal gates remain open. **G10 CLOSED.**
- [ ] **6.17 NOT FROZEN**; no6.13–6.18 implementation, activation, merge or release.

Current readiness: [STAGE7B-APPROVED-DESIGN-READINESS.md](STAGE7B-APPROVED-DESIGN-READINESS.md).
Full design: [API-V1-DECISION-PROPOSAL.md](API-V1-DECISION-PROPOSAL.md).
The original hash-bound API-CONTRACTS.md, AUTH.md, DATA-MODEL.md,
STAGE7B-CONTRACT-READINESS.md and HANDOFF-STAGE7.md stay byte-preserved; this dated
record and the current design document supply the approved amendments.

created by stokonn with love <3
