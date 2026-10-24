# Stage6 / Stage7B — executable integration readiness

Status: LOCAL ACCEPTANCE VERIFIED, 2026-10-24; implementation `b2fed621ef32665072faa6172d7947c99db3af61` source published/exact remote verified; final CI receipt in Issue31. Stage6 recovered baseline
`a3fb7e2f1436c93b49a0f4f0596d8d06e241fcef`; branch
`feature/goldberg-0.11-stage-6-api-v1-local`. Stage7B source inspected read-only at
`0887d3fe33cf5d49d46e14e1de95168f08a493a2`; its executable correction remains
`fb1d3f971ed79de09dccbdc771e1c45a09dc0a6c`. Actual local/remote heads matched.
Its acceptance40097540179 and final owner report are independent evidence, not
live backend compatibility. Its tracked sources/private/untracked artifacts were
not changed. Existing Stage6 M2/M3 untracked evidence and private PMP preserved.

## Implemented foundation

`contracts/admin/shared/v1` is one package version over the existing approved
O1–O8 schema, not a new HTTP dialect. It includes executable canonical scene
requirements, source-pinned consumer cases, machine-checked compatibility rows,
version evidence and a local reference consumer. Final scene/results below are
separate from the original61-case offline corpus and the469-body baseline.

The new tests issue actual loopback HTTP requests into disposable PostgreSQL:
challenge/login/identity, access/family expiry, refresh rotation/replay/family
revocation, logout204, role revocation/suspension/account isolation, destroyed
refresh/decision response streams, receipt replay/mismatch/expiry, concurrent
decisions, generic conflicts and explicit reconfirmation, queue/audit pagination,
coherent server statistics/DST, default-deny/null metadata, actual timeouts/outages,
unknown fields, unsafe legacy representations and maximum signed int8.

The consumer reference immediately clears local credentials before logout I/O,
coalesces refresh within one account, requires fresh authentication after lost
refresh response, rejects stale account responses, invalidates acknowledgement
on observed privilege loss and never auto-retries privileged decisions. Existing
entity approval needs available current values and matching recorded baseline;
unavailable/rebase cases issue no POST. Lost decision response retains exactly
the original body/key for explicit replay, while409 clears it and requires new
refetch/reconfirmation. This is an injected local reference, not Android wiring.

Selected **unmodified actual Kotlin** AdminApi/Models/WireValues/Stats files are
compiled from the pinned Git objects into/tmp with cached compiler/dependencies.
The isolated JVM probe replays actual HTTP/PG response captures through their
actual parser/API seam. It executes no Android storage, screens, process lifecycle,
network socket or other-worktree Gradle build. Dependency/source hashes and raw
compiler/runtime output are retained. Static facts are separately identified.

Two local backend adaptations:

1. Recognize `application/json; charset=utf-8` used by the inspected client,
   retaining fatal UTF8/duplicate-key/size/DTO validation and rejection of other
   charsets. No legacy revision or DTO alias is introduced.
2. Bound candidate queue headers to limit+1 before joining revision payloads and
   computing evidence. Every filter/keyset predicate stays before the inner bound,
   with the same ordering and statement snapshot. Existing FK-backed pointers
   guarantee the corresponding committed revision. Measured original default and
   keyset plans hash-joined all5000 revisions; the new regression requires≤21
   revision rows of work. No schema, migration, index or dependency change.

## Consumer inventory and exact next implementation

Actual sources are pinned in `consumer-source.json`, not inferred from filenames:

| Actual component | Inspected behavior | Required canonical integration |
|---|---|---|
| AdminApi / AdminHttpTransport | injectable HTTPS API,512KiB bounds, no redirects; app uses DisabledAdminTransport | retain security boundary; explicit canonical methods/DTOs only after separate integration authorization |
| AdminModels / AdminWireValues | legacy id/owner/revision:Int, numeric timestamps, structured wrappers absent; signed cursor string accepted | exact DTO names/wrappers/nullability, dual decimal-string revisions and ISO microseconds; no opt-default fabrication |
| AdminSessionStore | one encrypted opaque token; no refresh family/principal/session/expiry model | extend identity-bound lifecycle; preserve Keystore/backup exclusions, never persist credential-bearing logout work |
| AdminRepository | role/account guard and trust invalidation; logout clear in finally after I/O | clear credentials/account-sensitive state synchronously before awaiting network; do not claim revocation on outage |
| AdminReviewService | refetch/acknowledge/re-review, explicit uncertain-outcome replay; one Int revision; decisions disabled by app | bind acknowledgement to both revisions, principal/session generation, proposal hash and current Comparison; explicit open; no409 auto-retry |
| AdminQueueService | bounded local detail walk/search, legacy row fields; server status/cursor/limit only | canonical server filters/search and explicit page coverage; reset cursors on account/filter/scope change |
| AdminAuditService | legacy seq/ts/actor/subject and chainHead | canonical actor/subject/time/content/highwater/headHash/coverage; preserve exact canonical bytes for hashes |
| AdminStatsService | bounded queue/audit derivation, device timezone, decision.approve/reject | exact server Stats/StatsUnavailable, database asOf/highwater and Prague civil window; never derive totals from live pages |

[Per-operation matrix](INTEGRATION-COMPATIBILITY-MATRIX.md) lists all34 cases and
remediations. Selected JVM outcomes are not complete Kotlin repository/UI acceptance.

Request construction and behavior:

- Keep revisions as validated canonical decimal strings; checked Long/BigInteger
  comparison is possible through9223372036854775807, but serialize strings. Entity
  baseline permits0; submission/publication revisions start at1. No narrowing toInt.
- Parse UTC timestamps with six fractional digits; preserve microseconds and
  canonical audit content. Millisecond default0 is not an unavailable timestamp.
- Required known fields/types must validate; unknown response members may be
  additive. Unknown request/query fields are rejected, not silently dropped.
- Capabilities bind principalId **and** sessionId. Account/session/endpoint changes
  clear sensitive cached reviews, cursors, pending intent and trust. Refresh is
  single-flight; lost refresh response requires fresh authentication, not replay.
- Logout clears locally before I/O and handles204 before JSON parsing. Server
  revocation is confirmed only by the actual successful response. No secret queue.
- `submitted` requires explicit open; `changes_requested` awaits an author revision,
  not moderator inference. Only fresh `in_review` data is decidable. Comparison
  unavailable for an existing entity blocks reference approval; absent new entity
  with baseline0 is explicitly represented as no current value. No proposal-before
  substitute. A different live entity revision requires author rebase/new proposal;
  the public adapter does not expose an invented author-revise endpoint.
- Re-fetch Detail and Comparison before confirmation/POST; invalidate acknowledgement
  when either revision, proposal hash/state or current entity status/hash changes.
  Backend transactions remain authoritative for races after this preflight.
- On409 clear pending approval, refetch and obtain explicit reconfirmation. On a
  genuinely lost decision response, replay only the identical body/key explicitly
  within the original route/account context. Revoked capabilities cancel queued
  privileged work. Changed intent under the same key returns422; do not auto-rekey.
- Reasons are plain text≤1000 UTF8 bytes, not2000 UTF16 characters. Display null
  title/composer and unavailable Comparison as unavailable, never inferred data.
- Queue cursors represent a **live keyset page walk**, not a multi-request snapshot:
  later inserts can appear. Audit cursors retain fixed highwater/head and15-minute
  lifetime for that walk. Stats alone use one coherent server snapshot, asOf and
  database Prague civil-day boundaries. Approval counts once; publication adds0.

## G1–G11 ledger

| Gate | Current executable evidence | Remaining integration |
|---|---|---|
| G1 statistics | coherent exact HTTP stats/concurrency/DST; actual legacy Kotlin returns unavailable | OPEN: replace client derivation with canonical server endpoint |
| G2 queue | canonical nullable rows/filters/signed live keysets; legacy parser rejects them | OPEN: canonical models and server filter/search wiring |
| G3 audit | actual chain/highwater/filter/cursor fixtures; legacy parser rejects structured DTO | OPEN: canonical vocabulary, subjects/content and chain handling |
| G4 Comparison | absent/available/default-deny and refetch prerequisites verified locally | OPEN: Android method/display integration not executed |
| G5 sessions | real lifecycle/replay/outage/loss and reference single-flight/account guards; capabilities subset parses | OPEN: full Android token/refresh/principal/session lifecycle |
| G6 moderation | dual revisions/open/typed results/conflict/reconfirmation/exact lost-response replay | OPEN: legacy request builder/results incompatible, new Android acknowledgement tuple |
| G7 HTTP/error | common errors parse in actual Kotlin; UTF8 media type supported; logout204 rejected by legacy client | OPEN: bodyless logout, session methods, public/browser/hosted acceptance |
| G8 int8/time | full-int8 regression retained and maximum fixtures; actual legacy Int rejects maximum | OPEN: widen revision/count models and microsecond timestamp parsing |
| G9 shared/version | maintained runtime fixtures, source/JVM matrix and history evidence exist | OPEN: adopt in consumer, owner public-version decision and compatibility scope |
| G10 disabled boundary | actual Kotlin forApplication returns ContractUnavailable; source decisionsEnabled=false | CLOSED for disabled production networking/actions only |
| G11 validation/policy | unknown fields/unsafe values/default-deny verified locally | OPEN: Android validators/nullability/1000-byte reasons and real policy approval |

## Public version proposal and capacity limits

[Version gate and concrete migration proposal](PUBLIC-VERSION-COMPATIBILITY.md).
Repository evidence finds an incompatible local synthetic Stage7B client; it does
not prove an incompatible approved/deployed public consumer, or global absence.
No version was chosen, routes renamed/exposed or API frozen.

[Measured query evidence](INTEGRATION-QUERY-EVIDENCE.md) records plans and limits.
No Supabase/Cloudflare/free-tier/production capacity or final query budget is proved.
C4 remains8 PASS/19 PARTIAL/6 OPEN;6.0f unchecked; C2 §12, legal/operational/hosted
and real source/license/field/attribution policy remain independent. Stage6/Stage7B
are not complete; their remaining owner/runtime gates are preserved.

## Verification and publication

Final source-bound run: `spikes/stage6-api-v1-local/results/http-pg/pg-ojxz4mbi`.
Runner exit0/acceptancePASS/sourceUnchanged true; independent verifier exit0,
including fresh exact container/volume absence probes.490 Node tests passed:
469 baseline plus20 shared HTTP/PG and1 query-plan body,0 failures/skips/cancellations.
66 scene responses match schema before synthetic-token scrubbing;2 actual dropped
response streams;22 actual Kotlin probes (12 compatible/10 incompatible);34 matrix
rows (12 compatible/12 incompatible/7 unexecuted/2 blocked/1 N/A). Existing39 read/
22 Comparison DTO captures, DTO61 expectations, historical18+17/current parser11,
policy/backup/ACL/whitespace all PASS. No repeated-suite count inflation.

196 inventoried source/test/input files plus integration manifest stayed unchanged.
Source-set `ddffb491f5e99f1db6bd20673312be888b8b4f846d30038d15fdcad2eea2a9c0`.
Independent receipt: `results/integration-readiness-2026-10-24/independent-pg-ojxz4mbi.log`.
Container/volume identities and raw hashes are in result.json. Implementation `b2fed621ef32665072faa6172d7947c99db3af61` ordinary-pushed, remote exact. All197 committed
input/manifest hashes match the run; recovered baseline is an ancestor. Final
documentation receipt SHA and its exact-head quality result are maintained in
Issue31/private PMP, avoiding a self-referential commit hash in this file.

Failure/intermediate history is preserved: `pg-d8h0hv95` shared16/17 (incorrect
authored suspension401 expectation corrected to inspected established403);
`pg-ipgctj55` initial487-body pass/pre-optimization EXPLAIN; `pg-mci70thp`
490-body pass before strengthening the final Kotlin identity/route-correct replay
assertions; `pg-6ape0ubv` passed before final session-generation hardening.
Only final `pg-ojxz4mbi` covers the final source, including refreshed-session
acknowledgement invalidation and exact uncertain-intent preservation across refresh. The failed Node TAP's
blank-line spaces are preserved by one exact-path attribute, not raw-log edits.
Run `build-current-provenance.py`, then `run-http-postgres.py`, then independently
`verify-http-pg.py RUN_DIRECTORY`. The new integration manifest is separate;
full-int8's manifest, metadata, fixtures and all M2/M3/T22/HTTP raw evidence remain
byte-exact. Fresh before/after/current hashes bind source/tests/package/validators;
actual Kotlin dependency/source pins and exact resource absence are verified.

Historical18 design and17 security-evidence tests execute on the original source
snapshot; current parser11 and original61 DTO expectations remain separate. Old
pin checks on evolved current handlers still fail explicitly, never relabeled PASS.
Initial suspension-expectation failure and intermediate runs remain preserved.

No production/OAuth/real user data, Android/other-worktree edits, device/emulator/
ADB, trusted APK/signing/screenshot acceptance, main merge, release or deployment.

created by stokonn with love <3

## Source publication receipt

Implementation/source/evidence commit `b2fed621ef32665072faa6172d7947c99db3af61`
was ordinary-pushed with exact remote readback. Committed-source-identity.json
verifies all197 accepted inputs/manifest against that Git commit and baseline
ancestry. This receipt modifies docs/evidence only; executable source, shared
package, final provenance and raw test bytes remain unchanged. Automatic GitHub
quality runs on the exact final receipt HEAD; its terminal result is recorded in
Issue31/private PMP. No CI result is inferred from an earlier commit or from local
Kotlin replay. Original two untracked M2/M3 evidence directories remain preserved.

Next authorized work: owner disposition on public version/real policy, separately
scoped Stage7B adoption with canonical DTO/session/reconfirmation changes, then
independent Android/hosted/security/operational acceptance. Neither stage is complete.
