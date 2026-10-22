# Stage 6 local Auth/session/AuthZ — executed evidence

2026-10-22; branch spike/goldberg-0.11-stage-6-auth-local; base 0f966dc.

**Final: 50 new Auth/security tests PASS + 34 original schema regressions PASS =
84 passed, 0 failed, 0 skipped, 0 cancelled.**
Command from repository root:
`spikes/stage6-auth-local/run-local.sh > spikes/stage6-auth-local/results/final.tap 2>&1`.
Real local PostgreSQL 17.11, Node v22.23.1, pg 8.23.1, jose 6.2.13; image/binding in
[environment.json](environment.json). Source hashes in source-sha256.json bind the
new runtime modules, SQL, tests, manifests and runner. Original schema source and
all original evidence remain unchanged; its new run output is schema-regression.tap.

First run: 41/41 new tests + 34/34 regressions. Second: 48/48 + 34/34 after added
UUID/audience/clock rollback/issuer/concurrency cases. Final: 50/50 + 34/34 after
clock-failure and future-grant enforcement. No failed/skipped test in these runs.
No old PostgreSQL/SQLite concurrency benchmark or provider/security audit rerun.

## Threat coverage — local evidence only, no complete-row/integration pass claim

| Row | Executed local coverage | Still unexecuted / qualification |
|---|---|---|
| T1 | JWT/session expiry, immediate revocation/logout, family expiry, secret-safe denial log, malformed/absent tokens and 401 vs valid-but-blocked 403 | Real provider/integration behavior and transport deployment. Overall row OPEN. |
| T2 | RS256 and configured ES256 real signatures, bad signature/none/HS confusion, unknown kid, single-flight/cooldown, rotation, cache expiry, provider outage/timeout, malformed/private/ambiguous JWKS, no attacker URL, required claims and clock failure | Real Google/Supabase rotation/JWKS HTTP adapter. Overall row OPEN. |
| T3 | Nonce binding, single-use/expired challenge, concurrent credential exchange, refresh rotation, concurrent refresh winner invalidated by replay, committed family revocation/audit, rollback | Actual Google credential/managed Auth/browser replay and PKCE/CSRF lifecycle. Overall row OPEN. |
| T4 | Exact issuer/audience, cross-issuer subject isolation, no multi-audience implicit acceptance | Hosted client IDs/issuer integration. Overall row OPEN. |
| T5 | Fresh DB role/state/session checks, revoked moderator, suspended/deleted principal, immediate administrative revocation and suspension, no early future grants | Future endpoint wiring still required; tested core subchecks only. |
| T6 | JWT role claims ignored, body role/author/status escalation rejected with 400, empty/unknown capabilities deny | Complete 6.4 payload allowlists and all endpoint wiring not implemented. PARTIAL. |
| T7 | Owner query predicate; cross-user/nonexistent private submission both 404, moderator/admin access correctly scoped | Full future HTTP read handlers. Core guard coverage only. |
| T8 | Cross-user revision guard denied, actor≠author enforced, contributor/moderator/admin boundaries and uppercase UUID self-action rejection | Actual 6.4/6.5 mutation handlers/transactions not implemented here. PARTIAL. |
| T9 | Local anonymous DB role cannot read private tables or call append_audit; no arbitrary function interface; no grants from token/UI claims | Hosted Supabase REST/RPC/RLS paths not exercised or changed. PARTIAL. |
| T22 | No application operator capability/role, admin no self-grant, no audit mutation operation, direct SQL permission denial plus original immutable-audit regressions | Real bootstrap/operator MFA/infrastructure trust and actual endpoint integration unverified. Local subchecks only. |

Additional executed cases: seeded/repeated Auth migration and incompatible-seed
rollback; token/hash secrecy; account/family isolation; concurrent principal mapping;
login/refresh/logout/role-change rollback on mandatory audit failure; family audit
chain recomputation; unavailable DB/clock safe 503 without exception/token leakage.

The explicit role model grants contributor and staff capabilities separately;
there is no automatic administrator/operator seed. Role management tests bootstrap
only generated synthetic test identities through private fixture SQL, never a
launch admin account. Tests use actual jose signatures, actual local PostgreSQL
transactions and reusable runtime modules; fake provider/clock are dependency
fixtures, not disabled verification or fake persistence.

## Repository verification

All PASS from repository root:

- `python3 scripts/check_agent_policy.py` (runtime loading not tested).
- `python3 scripts/check_backup_fixtures.py` — 3 formats.
- `python3 scripts/check_history_paging.py` — 106 sessions/4 pages.
- `python3 scripts/check_localization.py` — 922 EN/CZ resources.
- `python3 scripts/check_migrations.py` — host v1–9→10 DDL, Android bridge/trigger runtime unexecuted.
- Source hashes/original schema package preservation, local documentation links,
  original gate boxes/non-Stage-6 scope and staged whitespace checks — recorded in
  docs/ai/VERIFICATION.md. No source change after final test hash capture.

Not performed: real cloud/Google identities, Supabase reads/writes, production RLS,
public HTTP, browser/CSRF/PKCE integration, real operator bootstrap/MFA, complete
T1–T9/T22 acceptance, full restore/RPO, app/Android/device/APK, deploy/merge/release.
No unresolved GDPR retention period or irreversible deletion implemented.

## Contract and next step

Revision-state consumer review is reused from LOCAL-CORE-ADOPTION.md (0f966dc):
prototype accepts changes_requested → in_review and excludes server draft/returned;
old C6 simulator has the broader guard, current app/Stage 7A has no matching
consumer found. Approved API patch remains unapplied. No migration of unseen data
or new server transition inferred; Auth/AuthZ is independent of this discrepancy.

Propose a separate local 6.4–6.6 milestone: typed bounded submissions/revisions with
full payload/evidence/license validation; authenticated atomic moderation reusing
server context plus existing lock/head design; principal/request-bound idempotency
replay/mismatch/expiry and failure regressions. No automatic continuation.
Q1/D5b unresolved/blocking; C4/C2 §12 OPEN; 6.0f/6.1–6.18 UNCHECKED;
6.0d/6.0e NOT AUTHORIZED; 0 CZK/month; collection NOT AUTHORIZED. Stage 5/7 preserved.

created by stokonn with love <3
