# Stage 6 — local Auth/session/authorization prototype

2026-10-22; base 0f966dc (latest schema remote, newer than owner-observed f54881e).
Branch: spike/goldberg-0.11-stage-6-auth-local; isolated clone
.gradle/stage6-auth-prototype. Owner authorized this LOCAL-ONLY prototype, code,
tests, synthetic PostgreSQL, commit/push and existing Issue #31 checkpoint.
**Local milestone verified; formal 6.2/6.3 acceptance NOT granted.**

Purpose: implement reusable authentication/session and authorization components
against the verified schema without cloud credentials, real identities or hosted
resources. Scope: [spikes/stage6-auth-local](../../../../spikes/stage6-auth-local/README.md),
additive local Auth migration and Stage 6 documentation only. No Stage 5/7 changes.

- [x] Recover latest remote and preserve original 34-test schema source/evidence.
- [x] Implement strict jose JWT/JWKS verification, nonce binding, fake provider/clock fixtures and durable issuer/subject mapping.
- [x] Implement CSPRNG hashed access/refresh/challenge storage, expiration, single-use challenge, rotation/replay family revocation and idempotent logout.
- [x] Implement current server grants/state, default deny, owner predicates, actor≠author, immediate revocation/suspension, no self-grant/operator capability and strict body/error/log boundaries.
- [x] Execute 50 new Auth/security tests plus 34 original schema regressions: 84 PASS, 0 failed/skipped/cancelled.
- [x] Run five existing repository host checks: PASS; verify original hashes/gates and new documentation/source scope.
- [ ] Commit/push verified milestone and append checkpoint to existing Issue #31.
- [ ] Show minimal latest-checkpoint-link dashboard diff and obtain approval before description publication.

[Executed evidence](../../../../spikes/stage6-auth-local/results/SUMMARY.md) gives
exact commands, environments, source hashes and T1–T9/T22 local coverage/limits.
No complete threat-row or hosted pass inferred. DB audit failure rollback,
concurrent sign-in/refresh, cross-account/issuer isolation, malformed/expired
credentials/keys/cache/clock, safe 401/403/503 and local DB denial are executed.
No original 20-trial concurrency benchmark or provider audit repeated.

API revision contract remains unchanged: canonical changes_requested and local
drafts match the schema; returned is not a separate server state. Existing consumer
review/patch in [LOCAL-CORE-ADOPTION.md](LOCAL-CORE-ADOPTION.md) is reused, not
silently applied. This discrepancy did not block independent Auth/AuthZ work.

Remaining risks: real provider/JWKS HTTP integration, Google/Supabase OAuth,
production DB privileges/RLS/REST/RPC, cookies/PKCE/CSRF and all endpoint wiring are
unexecuted. Owner DB access remains a trusted boundary; local in-process guards
are not a deployed security perimeter. No retention periods, irreversible deletion,
real administrator bootstrap or production token/config defaults invented.

**Exact next proposal, not started:** separately authorize local 6.4–6.6 only:
(6.4) typed bounded submission/revision handlers and full payload/evidence/URL/
license validation; (6.5) atomic moderation bound to this verified server context,
existing submission/entity/audit locks and fault injection; (6.6) principal/request-
bound idempotency/replay/mismatch/expiry and concurrency/crash regressions using
existing storage. Review the canonical revision-state patch before endpoint adoption.
No automatic continuation, deployment or formal promotion.

Q1/D5b unresolved/blocking; C4/C2 §12 OPEN; 6.0f/6.1–6.18 UNCHECKED;
6.0d/6.0e NOT AUTHORIZED; ORD-A sequencing only; 0 CZK/month; collection NOT
AUTHORIZED. Stage 7A conditional/X10 OPEN and Stage 7B untouched.

created by stokonn with love <3
