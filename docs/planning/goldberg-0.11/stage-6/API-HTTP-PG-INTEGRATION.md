# Stage 6 — local HTTP → PostgreSQL integration

Purpose: close the separate-venue gap with actual loopback HTTP requests reaching
PostgreSQL transactions, and implement the O1–O8 approved local read projections.
Scope: `spikes/stage6-api-v1-local/` and tracking/evidence only. No native service,
schema migration, Android/Stage7 worktree, hosted configuration or policy adoption.
Starting commit: `944280bee28d3280479db3b4e311e69b8c49027c`, branch
`feature/goldberg-0.11-stage-6-api-v1-local`. Owner instruction of 2026-10-24
explicitly authorizes this local-only engineering batch. `/v1` remains provisional.

## Implemented behavior

- Queue projects exact IDs/status/kind, six-digit timestamps, independent revision
  strings and current-proposal evidence counts. Title/composer remain null because
  there is no approved stored disclosure projection. Search covers exact submission
  UUID and available kind; null display fields cannot match. Status/kind/q filters
  run in SQL before the limit (1–20). Keyset ordering is `(created_at,id)`.
- Audit projects original canonical content, hashes, actions, actor/subject and
  request IDs. AND filters run before the limit: action, actor, subject, inclusive
  from/exclusive to, and literal q in action/subject UUID/safe reason. Unfiltered
  pages identify chain coverage; filtered pages explicitly identify filtered coverage.
- Existing CursorCodec now backs real reads. Cursors bind caller, scope, normalized
  filters and order; audit additionally fixes highwater/head. Each walk expires
  15 minutes after its first page, including after adapter restart with the same
  explicitly supplied local key. Default random keys are process-local; no production
  key custody/configuration is introduced. All pages recheck live grants/session.
- Queue/audit/stats read a bounded read-only REPEATABLE READ transaction on a
  separate connection while the authorization transaction retains live principal/
  family locks. Stats additionally checks audit.read in the same locked session.
  Five-second statement timeout yields safe 503, with no approximate totals.
- Stats derive date/asOf from PostgreSQL and Prague midnight bounds from the server
  calendar helper. Counts share one content snapshot: current submitted/in_review,
  changes_requested, and exact approval/rejection audit transitions within that day.
  approved->published adds zero approvals. DST days can be 23 or 25 hours.
- SQL literal search normalizes both operands to NFC and lowercases them; percent
  and underscore are literal, with Czech diacritics preserved. No fuzzy or missing-
  metadata search claim; final free-tier query budgets remain unmeasured.
- HTTP uses deterministic JSON serialization, so initial and persisted JSONB
  receipts have identical bytes. URL form-query plus decoding is supported;
  prototype-related unknown keys and malformed dates fail closed.
- int8 reads remain decimal strings, including max-int8 queue baselines. Domain
  writes beyond JS safe integers remain explicitly unavailable; no rounding occurs.

## Endpoints exercised through actual HTTP and PostgreSQL

| Methods | Local provisional route |
|---|---|
| POST | `/v1/session/challenge` |
| POST, GET, DELETE | `/v1/session` |
| POST | `/v1/session/refresh` |
| GET | `/v1/me/capabilities` |
| GET | `/v1/admin/queue`, `/v1/admin/audit`, `/v1/admin/stats` |
| GET | `/v1/admin/submissions/{id}` |
| GET | `/v1/admin/entities/{id}/comparison?submissionId={id}` |
| POST | `/v1/admin/submissions/{id}/open`, `/v1/admin/submissions/{id}/decisions` |

Synthetic fixtures use native SubmissionService for author creation/revision;
there is no invented public contributor HTTP dialect. Synthetic identity verifier
is explicitly a test double; real signed-JWT/JWKS behavior is tested independently
by the existing Auth suite, with synthetic keys and no real OAuth credentials.

## Verification and retained attempts

- Original baseline: 117/117 API tests passed on host, including two real HTTP
  tests backed by scripted SQL. Raw `results/http-pg/baseline-944280b.tap`.
- `pg-4klrg2um`: initial combined14/20; fixed actual replay serialization and URL
  query plus decoding; corrected revision/hash/refresh race fixture assumptions.
- `pg-i9fkv740`: combined20/20, M2-19, M3-16, API117, T22-38, planning18 PASS;
  source unchanged and exact container/volume removal verified.
- `pg-2p45f5_y`: expanded24/24 and all suites PASS,37 DTO captures validate;
  source changed during the run by additional test assertions, so it is **not the
  final source-bound acceptance evidence**. Its nonzero runner exit is retained.
- `pg-c9rg4x5v`:23/24; added stats timeout fault selector did not match the
  SQL whitespace, so the healthy stats route correctly returned200. Corrected
  test selector only; this failed attempt is retained.
- **Final `pg-n4udfonz`: PASS**, source unchanged, exact container/volume absence
  verified, runner exit0 and independent source/raw verifier PASS. PostgreSQL17.11
  (Debian17.11-1.pgdg13+2); exact image ID recorded in result/image evidence.

| Venue | Executed/passed |
|---|---:|
| Actual HTTP→PostgreSQL | 24/24 |
| Independent real PostgreSQL Auth/schema/handlers/operations | 50/34/87/34 |
| Independent real PostgreSQL M2/M3 | 19/16 |
| Existing API (includes2 HTTP/scripted-DB tests) | 117/117 |
| T22 scripted/unit; operations unit | 38/38;32/32 |
| Planning; security-evidence Python | 18/18;17/17 |
| Captured actual HTTP queue/audit/stats DTO schema checks | 39/39 |
| Policy/portable backup fixtures/ACL proposal/whitespace | PASS |

All final suites have zero failures/skips/cancellations. PostgreSQL totals:
24 combined HTTP bodies plus240 independent database bodies; scripted SQL is
separately labeled. These are suite executions, not a claim of unique scenarios.
Query work is capped by1–20 rows and5s statement timeout; no production/free-tier
throughput or capacity promise follows from small synthetic fixtures.

Use `python3 spikes/stage6-api-v1-local/run-http-postgres.py` on an authorized
rootless host with the cached official `postgres:17` image. It records and runs
its inspected image ID, never pulls, accepts no remote URL or host data mount,
uses only ephemeral localhost ports and synthetic per-test schemas, then removes
its exact UUID container and anonymous volumes and verifies absence. Run
`python3 spikes/stage6-api-v1-local/verify-http-pg.py RUN_DIRECTORY` afterward.
Raw/source SHA-256 maps, failed attempts and historical M2/M3 evidence are retained.

## Publication

Implementation/evidence commit `ead440a27f5564b2366b50e3fb850a4f2c713fc5` ordinary-
pushed and exact remote SHA verified. [Published checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6382021802).
Issue31 OPEN / [Goldberg Development](https://github.com/users/stokonn/projects/2)
In Progress. Documentation-only receipt changes no tested source. The only original
untracked work is two retained M2/M3 evidence directories, deliberately not staged.
Private checkpoint is outside Git; no `.pmp/`, `.pmp.yml` or owner release notes committed.
Two narrow `.gitattributes` entries preserve Node-emitted whitespace in failed raw
TAP logs; their bytes/hashes are unchanged and all source/docs whitespace checks pass.

## G1–G11 status (local backend versus consumer integration)

| Gate | Local evidence | Remaining integration gate |
|---|---|---|
| G1 | Coherent exact snapshot/counts, concurrent publication and Prague/DST tests | Android/shared-fixture and hosted budget acceptance OPEN |
| G2 | Safe nullable queue, SQL filters/keyset/literal search/cursors | Real label disclosure policy and consumer integration OPEN |
| G3 | Canonical audit projection/filters/highwater/chain and signed paging | Consumer/hosted privacy acceptance OPEN |
| G4 | M3 live Comparison; HTTP available/unavailable, Detail independent | Real source/field/license/attribution policies default-deny; client reconfirmation OPEN |
| G5 | HTTP session lifecycle/identity/TTL/rotation/replay/logout/isolation | Android credential lifecycle and real provider setup OPEN |
| G6 | HTTP dual revisions/open/decisions/conflict/exact replay/races/rollback | Client refetch/reconfirmation and full numeric writes OPEN |
| G7 | Real local HTTP/DTOs/microseconds/cursors/errors | Public version compatibility UNCONFIRMED, no API freeze |
| G8 | Exact decimal int8 reads and fail-closed unsupported writes | Full-int8 domain mutations and Android adaptation OPEN |
| G9 | Original canonical action vocabulary, chain and single-count stats | Consumer/hosted acceptance OPEN |
| G10 | Existing disabled Android production transport boundary CLOSED | No new Android runtime claim; unchanged |
| G11 | Native policy/UTF-8/allowlist regressions; default-deny HTTP publication | Real concrete field/source/license/evidence policy and client acceptance OPEN |

Stage 6 stays OPEN, formal6.12 PARTIAL,6.17 NOT FROZEN. C4 remains **8 PASS /
19 PARTIAL /6 OPEN**,6.0f unchecked; C2 §12 and hosted/operational/legal gates
remain independent. No production Supabase/Cloudflare/OAuth, paid resources, real
users, device/ADB, merge/main/tag/release or public API activation occurred.
No Gradle/JVM/lint/APK, instrumentation, screenshots, browser/CORS/proxy, physical
device or hosted tests ran in this backend-only batch. No new APK/hash/signing claim.
Backup/ACL proposal safety and portable fixture checks are distinct from the retained
historical synthetic restore/ACL SQL evidence, which is not relabeled as rerun.

created by stokonn with love <3
