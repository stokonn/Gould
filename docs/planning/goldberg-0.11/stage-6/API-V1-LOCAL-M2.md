# Local API Milestone 2 — authoritative detail and PostgreSQL

**LOCAL IMPLEMENTATION AND VERIFICATION COMPLETE; integration remains PARTIAL.**
Purpose: authoritative bounded latest-submission refetch after generic409, plus
actual disposable local PostgreSQL acceptance of the adapter and public receipts.
Scope: `spikes/stage6-api-v1-local` and tracking documentation only. Existing native
services, schemas, migrations, Android, M1 and security evidence remain unchanged.
Starting clean/remote-matched ancestor:0782e22b17b55fe9850934feafca4af391a508d2.
Branch: `feature/goldberg-0.11-stage-6-api-v1-local`. No listener or hosted integration.

## Tasks and verified behavior

- [x] Read AGENTS/maintenance/current handoffs, O1–O8, approved Detail and M1 evidence;
  inspect branch/status/ancestry, remote, tags/releases, milestone and existing Issue31.
- [x] Implement GET `/v1/admin/submissions/{id}` with strict canonical UUID, no body
  or query,405/Allow for unsupported methods, safe errors and no-store responses.
- [x] Use live session + queue.read before content lookup. Contributors cannot use
  the admin route for either own or other submissions (403 independent of existence);
  an authorized reviewer gets404 for missing submissions. Revocation/suspension and
  live authorization before receipt replay verified. No account claims from input.
- [x] Obtain header, mapping classification, revisions and reviews in one PostgreSQL
  statement snapshot; explicit SELECT/projection, no raw database records. No read
  inserts/updates, automatic open, moderation authorization or silent decision retry.
- [x] Preserve recorded `expectedEntityRevision` and independent `submissionRevision`
  as canonical decimal strings. They are not interchangeable; entity7/proposal3
  publishes8. Refetch never rewrites baseline from live catalogue state. A competing
  publication still causes409 even after refetch or substituting the current head.
- [x] Preserve six fractional timestamp digits in SQL, without Date conversion.
  Actual PG max-int8 entity baseline9223372036854775807 read exactly; proposal1 and
  microsecond123456/123457/999999 values verified. Full-int8 proposal read representation
  also remains covered by existing scalar tests, not a fabricated max-proposal PG seed.
- [x] Validate stored payload through the exact nested allowlist and trusted policy
  before returning it. Actual policy sets stay empty/default-deny: unavailable503,
  no raw data fallback. Synthetic policy alone permits synthetic test content.
- [x] Bound revisions/reviews to20 each, revision numeric descending/current first,
  review audit sequence numeric ascending. Query21 detects overflow; return503 rather
  than silently truncate or invent cursors/completeness. nextCursor=null only when
  the bounded response is complete. Total DTO byte budget262144 also enforced.
- [x] Actual PG adapter/services verification: persisted submitted/open/published and
  reject/request_changes detail; independent revisions/stale conflicts; authorization,
  role/session revocation and isolation; exact replay/changed intent422; atomic effects,
  receipt/audit persistence and rollback; concurrent moderators and same-key requests;
  fresh connections/adapter instances; completion-based24h retention at−1ms/at/+1ms.
- [x] Coherent content test holds a real fetched SQL result while another reviewer
  commits, then returns internally consistent old content; subsequent refetch is new.
- [x] Capture13 actual PostgreSQL-backed Detail responses and validate all against the
  approved exact DTO schema, including nested Payload/Revision/Review types.
- [x] Focused regressions, raw artifacts, source-before/after/current verification,
  PostgreSQL version and cleanup evidence retained. Prior345 baseline not rerun.
- [x] Synchronize verified progress with Issue31 description/comment; Issue31 OPEN /
  Goldberg Development In Progress. [Verified checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6374588115).
- [ ] Source publication: commit and ordinary push after fresh remote-divergence check;
  exact remote SHA readback before the factual completion checkpoint.

## Executed verification and venues

Final source-bound run: `spikes/stage6-api-v1-local/results/m2/pg-lg7ei69p`.

| Command / venue | Executed result |
|---|---|
| `python3 spikes/stage6-api-v1-local/run-m2-postgres.py` (authorized host, rootless Podman) | PASS; actual PostgreSQL17.11, Node v22.23.1;19 tests/19 pass/0 fail/0 skipped/0 cancelled |
| Runner's `node .../tests/adapter.test.mjs` (in-process services/scripted SQL + units) |106 tests/106 pass/0 fail/0 skipped/0 cancelled |
| Runner's `node spikes/stage6-security-local/tests/t22.test.mjs` (service/scripted storage) |38 tests/38 pass/0 fail/0 skipped/0 cancelled |
| Approved planning verifier + its unittest discovery |61 synthetic DTO fixtures,8 protocol scenes,4 civil/DST windows,10 search fixtures;18 regressions PASS; not runtime acceptance |
| Captured actual Detail DTO/schema check |13 responses PASS; synthetic-only |
| Existing original-contract `check(check_client_worktree=False)` |PASS pinned historical provenance; does not validate current external Stage7B bytes |
| Agent policy / whitespace |PASS |
| `python3 spikes/stage6-api-v1-local/verify-m2.py spikes/stage6-api-v1-local/results/m2/pg-lg7ei69p` |PASS current tested-source and raw artifact SHA-256, counts, cleanup and protected baseline |

Image pinned to existing digest
`docker.io/library/postgres@sha256:e4cb93687b062cd25a0e8f672d85a7708235009d70d31f8a7698d9370092a9c4`.
No URL/configured hosted credentials accepted; schema_prototype database is inside
this run's unique disposable container. Only127.0.0.1 published, no host mounts or
system-service changes. Every successful container run recorded removal and an
`exists` exit1 probe; only this runner's UUID-named container was removed.
Cleanup follow-up: initial runs removed containers but retained four anonymous
volumes. Podman create-event adjacency (<20ms, no intervening create events), exact
container names/IDs from saved run receipts, unused anonymous-volume inspection and
absence probes bind the narrowly scoped recovery in `volume-recovery-attribution.json`
and `volume-recovery-cleanup.json`. Only those four volumes were removed; unrelated
volumes were preserved. Runner now captures Mounts, removes with --volumes and checks
each recorded volume's absence (exit1). Final run passed container AND volume cleanup.
`result.json` binds raw logs and source manifests; captures contain synthetic DTOs,
not credentials. Cleanup and version logs are in each run directory.

## Preserved attempts and honest limits

- `pg-2f80742_`: sandbox Podman setup denied; no container, zero PG bodies.
- `pg-ozahga4o`: actual PG16 tests/15 pass/1 fixture failure23514 `initial server state`.
  Initial proposal at max-int8 was invalid; corrected to proposal1/max-int8 baseline,
  preserving all revision-chain guards. Container removal/absence verified.
- `pg-tbo80wkn`: corrected16/16 actual PG tests PASS; source unchanged, cleanup verified.
  This completed despite the usage-limit interruption; recovered from saved logs.
- `pg-pl8so97p`:19/19 actual PG +106/38 regressions +18 planning tests PASS; runner
  stopped on historical verifier expecting older Stage7B ACCEPTANCE.md bytes. No
  changes made there; supported pinned-provenance mode used in final run. Cleanup PASS.
- `pg-bwbti5kp`: all tests/checks PASS before final anonymous-volume cleanup audit.
- Final `pg-lg7ei69p`: all required scoped checks PASS. Historical failed evidence and
  M1 artifacts are retained unchanged. Current Stage7B runtime acceptance is not claimed.

Approved Detail contains the submission's recorded entity baseline, not a live
catalogue revision/status field. targetState describes resolved/unmapped target intent
and zero/nonzero recorded baseline; it must not be used as a live catalogue-existence
proof. The separately designed Comparison DTO/service is not implemented in this
bounded milestone. No extra Detail field, before-value, live-head substitution or
content-policy permission was invented. Responses are coherent individually; no
cross-request snapshot guarantee is claimed. A complete conflict UI still needs the
authorized current comparison and explicit user reconfirmation; reads alone never
make a decision safe or prove which request caused current state.

No HTTP serialization/listener, Android/JVM/build/APK, emulator/physical device,
rendered screenshot, browser, hosted/Supabase, cloud, real OAuth or deployment executed.
Thus no new APK/hash/signing/source-artifact claim applies. Public `/v1` compatibility
UNCONFIRMED; routing LOCAL/PROVISIONAL. Full-int8 domain writes remain a separate gate,
with unsupported values503 after live auth and existing increment exhaustion409.
History cursors, current Comparison and client session/refetch/reconfirmation remain
open integration gaps. G10 CLOSED;6.12 PARTIAL;6.17 NOT FROZEN; no formal6.13–6.18,
merge or release. Stage7A FINAL ACCEPTED; X10 NOT FIXED/NOT VERIFIED, deferred Gould0.12.
No M2 mutations to Stage7B/security worktrees. Stage7B advanced externally from
 de792fc06f0e8d8ef898d4d2254489783e3310a5 to3a4aac27e2eb8ae70716538d86acd374cdd38077
 during this run; its changed status/diff were observed, preserved and not incorporated.
 Security worktree HEAD/status/diff remained identical. Observation receipt:
 `results/m2/unrelated-worktrees-observation.json` in the API package.
 No substantial repository cleanup performed.

Smallest justified next milestone: separately bounded authorized current Comparison
projection/refetch integration, retaining default-deny policies and explicit conflict
confirmation. Full-int8 writes/public-version compatibility remain independent gates.

created by stokonn with love <3
