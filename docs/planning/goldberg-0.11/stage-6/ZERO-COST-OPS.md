# Stage 6 — Zero-cost operations, monitoring and recovery plan

**Design only.** Applies the standing [CLOUD.md](../CLOUD.md) zero-cost operating
contract (§"Zero-cost operating contract", "Export, recovery and security gates") to
the concrete Stage 6 service. Nothing here is provisioned or scheduled yet. **Owner
decisions (2026-10-21): Supabase Free with strict 0 CZK/month is approved (D4a), and
inactivity suspension/quota limitations are explicitly accepted with graceful
degradation and fail-closed behavior (D4b)** — the standing rules below are therefore
binding requirements, not proposals.

## 1. Standing rules (restated, binding)

The executable operator procedure is
[BACKUP-OPERATIONS-RUNBOOK.md](BACKUP-OPERATIONS-RUNBOOK.md). This file
defines the design contract; the runbook defines the future, separately
authorized daily and quarterly procedures without activating them.

1. Free plan/organization only; free provider subdomain only; **no trial that
   converts to billing, no paid add-ons, no automatic upgrade**. Supabase Pro's
   spend cap is not a 0 CZK path (it has a fee). *(Owner-approved, D4a.)*
2. Re-verify dashboard plan, organization membership and current terms
   **immediately before any future provisioning**.
3. No fake keepalive traffic to evade inactivity policy — pausing is accepted (D4b)
   and is resumed by the owner, deliberately, from the dashboard.
4. No published SLA; the app must treat this service as hobby-scale best-effort;
   community features degrade gracefully and core local Goldberg functionality
   remains operational whenever the backend is unavailable *(D4b)*.
5. If Free terms change to require payment: **disable the optional service, export,
   prepare a separately reviewed migration** — never silently charge or silently
   collect elsewhere, and never switch backend/provider without a new owner decision.
6. Privileged operator access (console, secrets, exports) must be **MFA-protected**
   *(D8c)*; every grant/recovery action is audited.

## 2. Proposed budgets and warning/stop behavior

Design targets from CLOUD.md (not provider guarantees, not measured load):

| Signal | Warning (degrade) | Stop (intake off) |
|---|---|---|
| Database size | 250 MB | 350 MB |
| Egress (monthly, uncached) | 2.5 GB | 3.5 GB |
| Function/API invocations (monthly) | 250k | 350k |
| Worker requests/day *(D8b BFF topology — C1 owner-approved 2026-10-21, [BFF-FEASIBILITY.md](BFF-FEASIBILITY.md); effective only after 6.0d provisioning)* | 5k sustained (5% of the Workers Free 100k/day cap) | 10k (10%) or intake kill switch |
| Worker requests/day *(Workers variant, historical)* | 50k | 70k |
| D1 rows/day *(Workers variant, historical)* | 2.5 M read / 50k write | 3.5 M read / 70k write |

Behavior ladder (CLOUD.md §5, made operational):

- **Warning:** pause background overlay refresh; moderators notified (manual — no
  email service assumed); continue bounded reads.
- **Stop:** contribution intake returns 503 via the global kill switch; published
  read may continue; **pending submissions and audit evidence are never deleted to
  regain quota**.
- **Resume:** only after safe export, reviewed retention/capacity checks — never
  automatically, never by upgrading.

## 3. Monitoring

Manual owner dashboard review (no paid monitoring, no third-party telemetry):

- Weekly (or after any incident): plan status, DB size, egress, invocations/requests,
  error rate, auth active users, **export age**, pending-queue depth.
- All traffic counts — dev/test/admin/export included.
- Kill-switch state and any 429/503 spikes noted in an operations log (simple
  dated file in the owner's private notes; not in the repo).

## 4. Export and recovery

- **Cadence:** daily encrypted logical export of moderation data, mappings, sessions
  metadata and the full audit chain to an existing owner-controlled location (no new
  paid backup service; no identities in public GitHub releases/Wiki).
- **Content:** full rows for all tables in DATA-MODEL.md §1 + chain head +
  schema version; hash manifest over every file.
- **Target RPO ≤ 24 h**; **RTO not promised** until a drill measures it (honest
  caveat from CLOUD.md kept).
- **Restore drill:** at least quarterly and before any public collection gate:
  restore export into a fresh isolated database, recompute the audit hash chain,
  compare counts and spot-check records (THREAT-MODEL T19).
- Export failure ⇒ unsafe publication disabled (service degrades read-only), never
  "assumed backed up".

## 5. Incident procedures

| Incident | Procedure |
|---|---|
| Provider pause (Supabase inactivity) | Owner resumes from dashboard; verify export freshness after resume; record in ops log. |
| Quota exhaustion | Confirm warning/stop ladder fired; wait for reset; resume only after checks. |
| Suspected credential leak | Rotate JWT/service secrets in provider secrets manager; revoke all sessions (`sessions` table clear via console); audit-chain review for abuse; rotate export keys. |
| Admin account lost | AUTH.md §7 out-of-band re-grant via provider console; audit the grant. |
| Malicious/ spam content published | Quarantine + rollback (API-CONTRACTS §6); audit review; suspend principal; consider intake kill switch. |
| Provider withdraws Free / terms change | Disable service (kill switch + client feature-off), export, stop; migration proposal to owner; **no silent collection elsewhere**. |
| Backend breach indicators | Kill switch on; sessions revoked; export preserved as evidence; owner decides on notification duties (**no invented legal obligations — D5 territory**). |

## 6. Cost audit

Zero paid resources are expected: backend Free tier, static Web Admin on a free
Pages-type subdomain (static only — Functions/Workers usage would consume the same
free quota, which is acceptable *within* free limits but must be counted in the
budgets above), owner-controlled export storage already in place. Any proposed
resource with a price tag stops the stage for owner review.

created by stokonn with love <3
## Backup acceptance checkpoint — 2026-10-24

[BACKUP-ACCEPTANCE-MATRIX.md](BACKUP-ACCEPTANCE-MATRIX.md) now maps the
verified synthetic controls to the remaining production gates. Encryption,
manifest validation, tamper/partial rejection, atomic publication,
separate-process recovery, key rotation, schema/count checks, audit-chain
fixture checks, and cleanup remain local synthetic PASS evidence. Scheduling,
freshness monitoring, retention execution, hosted restore, owner-held disaster
recovery custody, and measurable production RTO remain design-only or OPEN.
No production scheduler or backup was activated.
