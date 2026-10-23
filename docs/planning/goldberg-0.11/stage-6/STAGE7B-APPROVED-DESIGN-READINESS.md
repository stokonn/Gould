# Stage 6 → Stage 7B — approved design readiness, 2026-10-23

Current companion to the hash-bound historical STAGE7B-CONTRACT-READINESS.md and
HANDOFF-STAGE7.md. [Owner approval](API-V1-OWNER-DECISIONS-2026-10-23.md) supersedes
pending O1–O8 design review from proposal baseline fefa6e8. No backend/Android source,
SQL, live networking, authentication or privileged decision behavior changes.

| Gap | Current design decision | Integration status / remaining work |
|---|---|---|
| G1 | Coherent exact snapshot or unavailable; Europe/Prague civil daily window in UTC transport | BLOCKED: implement snapshot/statistics and DST/consistency tests under later authorization. |
| G2 | Reviewed nullable metadata; NFC, case-insensitive/diacritic-sensitive literal server search | BLOCKED: metadata policy remains empty; query budgets require measurement; implementation and CZ/EN/literal-character tests pending. |
| G3 | Server audit filters/search, signed scoped cursors, highwater/coverage semantics | BLOCKED: implementation, privacy checks and measured bounds pending. |
| G4 | Authorized current-entity comparison projection; unavailable when absent | BLOCKED: concrete fields/provenance/attribution policy unapproved; service/client integration pending. |
| G5 | Session-derived principal, explicit version mapper/204 logout, approved TTLs/rotation/revocation/account isolation | BLOCKED: public session mapper/client lifecycle and acceptance absent; current local Auth allowlists unchanged. |
| G6 | Explicit open and independent revision expectations;200/201 unions;409 refetch/reconfirm;24h exact receipts | BLOCKED: public DTO/numeric adapter and client integration absent; no revision inference or silent retry. |
| G7 | Explicit HTTP/DTO projection, microseconds, scoped cursors15m,404/405 | BLOCKED: `/v1` requires incompatible-consumer absence confirmation; otherwise new `/v2` decision. No HTTP implementation or listener. |
| G8 | Canonical decimal strings preserving signed int8 storage with field-specific ranges | BLOCKED: separately authorized backend numeric changes and Android adaptation/tests absent. |
| G9 | Actual canonical actions retained; approval counted once; server Europe/Prague window | BLOCKED: reporting/audit integration and DST/chain/completeness tests pending. |
| G10 | Existing disabled production transport and independent privileged-action control | CLOSED; unchanged, no new Android/runtime acceptance claimed. |
| G11 | Safety structure and1000 UTF-8-byte limit approved; strict allowlisting | BLOCKED: all concrete source/field/license/attribution/evidence policy sets empty/default-deny; future client/server tests required. |

Existing bounded exact-or-unavailable fallback remains approved; design approval does
not manufacture totals, catalogue values, complete search or a reachable endpoint.
61 original synthetic DTO scenes and8 protocol scenes retain their historical authoring
classification and inactive flags; separate Prague/DST/search fixtures exercise the
approved qualifications only. Fixture validation is not SQL/HTTP/Android proof.

Prior345 executions/313 tests/205 PostgreSQL-backed bodies and T22 LOCAL PASS remain
unchanged;6.12 overall PARTIAL. No PostgreSQL rerun.6.17 NOT FROZEN;6.13–6.18 formal
implementation not started by this task. Stage7B acceptance BLOCKED, X10 OPEN,
Stage8 pending. Cloud/Q1/C4/C2§12/zero-cost/legal and runtime gates unchanged.
Next: separate owner policy decisions, compatibility confirmation and implementation
scope authorization; do not turn approved design into active behavior.

created by stokonn with love <3
