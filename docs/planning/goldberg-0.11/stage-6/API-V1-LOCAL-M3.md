# Local API M3 — current-value Comparison (VERIFIED / SOURCE PUBLISHED)

Purpose: authorized live catalogue comparison for moderation conflict recovery.
Scope: isolated local API adapter/projection/tests/evidence and tracking docs only.
Starting clean, remote-matched HEAD fdbfbe06fcd3faf939d4f6a90cbdeac8ee683e67;
branch feature/goldberg-0.11-stage-6-api-v1-local. Existing M2 is published.

- [x] Recover AGENTS/maintenance/handoffs, Stage6 checklists, O1–O8, contract,
  proposal, M1/M2 ledgers and live Issue31; verify HEAD/ancestry/status/remote/releases.
- [x] Implement exact approved Comparison DTO and strict local GET route; coherent
  SQL snapshot, live queue.read plus submission/entity binding and denied404.
- [x] Retain Detail baseline/proposal semantics; no automatic decision or read mutation.
- [x] Validate live fields/evidence through trusted default-deny policy; no private rows,
  no truncation, absent/unmapped/unsupported projections use approved unavailable union.
- [x] Actual disposable PostgreSQL behavior/concurrency/conflict/precision/auth tests;
  affected M1/M2/T22 regressions; raw logs/source hashes/container+volume cleanup.
- [x] Synchronize verified docs/Issue31/Project progress; commit and ordinary push
  implementation/evidence `b01f1e924601d2571ba399888755369de2db3f08`; exact remote SHA matched.
  Completion checkpoint is published to existing Issue31 after final tracking-commit
  remote readback; no second backend implementation or acceptance claim.

Comparison is the approved live-only DTO; it adds no baseline/proposal fields or
new timestamps. Detail supplies recorded expectedEntityRevision and current
submissionRevision separately. No cross-request snapshot guarantee. Client
reconfirmation is deferred. Real policies empty/default-deny; public /v1 unconfirmed;
full-int8 writes separate; G10 CLOSED,6.12 PARTIAL,6.17 NOT FROZEN. No formal6.13–6.18,
HTTP listener, Android/device/emulator, cloud/hosted/OAuth, merge/deploy/release.
Stage7A acceptance preserved; X10 deferred Gould0.12. No APK or screenshot applicable.

Implementation and required scoped verification PASS; source publication verified.

## Verification attempts (preserved)

- Initial runner invocation before results directory existed failed before any PG work.
- `pg-0wno17g1`: sandbox Podman denied, no container or PG bodies.
- `pg-0pwh1el8`: actual PostgreSQL17.11,12/12 behavior tests PASS; capture checker
  failed because runner used M3_DETAIL_PATH instead of M3_COMPARISON_PATH. No DTO
  acceptance claimed from that attempt. Exact container+volume removed/absence verified.
- Final `pg-ygh16ba3`:16/16 M3 +19/19 affected M2 actual PostgreSQL tests PASS;
 106/106 adapter/wire +38/38 T22 service/scripted-SQL regressions PASS;18 planning
 unittest cases PASS. Zero failed/skipped/cancelled bodies.22 captured Comparison
 DTOs match exact approved available/unavailable schemas. Before/after/current source,
 raw artifacts and protected baseline verified; container AND volume cleanup verified.
 PostgreSQL17.11, Node v22.23.1, pinned local image, rootless host Podman, ephemeral
 loopback-only port. No host mounts/shared DB/hosted credentials accepted.

## Exact commands and evidence

- `python3 spikes/stage6-api-v1-local/run-m3-postgres.py` — final PASS on authorized
 host, result/raw logs/source manifests in `spikes/stage6-api-v1-local/results/m3/pg-ygh16ba3`.
 Runner executes `node --test spikes/stage6-api-v1-local/tests/m3-postgres.test.mjs`
 and `node --test spikes/stage6-api-v1-local/tests/m2-postgres.test.mjs` against the
 actual disposable engine, then `node spikes/stage6-api-v1-local/tests/adapter.test.mjs`,
 `node spikes/stage6-security-local/tests/t22.test.mjs`, approved planning verifier
 and its unittest discovery, pinned historical-contract provenance, policy/whitespace.
- `python3 spikes/stage6-api-v1-local/verify-m3.py spikes/stage6-api-v1-local/results/m3/pg-ygh16ba3`
 — PASS, validates current tested-source and raw SHA-256 bindings, exact counts,
 DTO captures, protected source and cleanup receipts. Not HTTP acceptance.
- All M1/M2/security prior raw evidence and failures preserved. Prior345 baseline
 not repeated. M2 implementation/publication not recreated. Real PostgreSQL accepted
 only16 M3+19 M2 bodies; scripted SQL and fixtures are reported separately.

No Android/JVM/Gradle/APK/lint/device/emulator/rendered UI/screenshots executed;
no APK/hash/signing applies. No listener, HTTP serialization, hosted/cloud/OAuth,
production-policy activation, formal6.13–6.18, freeze, merge/deploy/release.


## Semantics and limitations

GET `/v1/admin/entities/{id}/comparison?submissionId={id}` is in-process only.
Strict canonical UUID/query, GET only, no body, no-store and safe errors. Live session
and queue.read required; denied authorization becomes404 (401 stays401); absent
submission or mismatched target returns404. Role/session/account locks and live
capability lookup reuse existing services. Submission target/mapping and current
entity head/payload/hash come from one SELECT snapshot; no raw records returned.

The approved Comparison has no baseline, proposal revision or timestamp fields;
no DTO extension was invented. Detail still returns recorded expectedEntityRevision,
current submissionRevision and exact timestamps. Comparison.entityRevision is the
live head. Requests are individually coherent; no shared snapshot across requests.
Later decisions still check both expectations and live state; no automatic retry.

Unmapped returns entityId=null/reason=unmapped; absent head returns reason=absent.
Unsupported kind, unavailable/malformed/disallowed stored fields/evidence and
projection bounds return projection_unavailable without partial fields. Current
text/evidence only: existing Validator exact allowlists and source/license tuples;
contributorNote, raw source_evidence, actor and private provenance never projected.
Production Validator default remains empty; synthetic permissions only in tests.
Published targets cannot be deleted under existing restrictive FKs; tests verify
that rejection and absent-head behavior. No destructive fixture guard bypass.
Initial max-int8 published-head fixture exercises reads with FK/hash/evidence guards,
not domain writes or a max-int8 submission revision chain. No full-int8 write claim.


created by stokonn with love <3

## Publication and continuity

Implementation/evidence commit `b01f1e924601d2571ba399888755369de2db3f08` ordinary-pushed,
exact remote branch SHA readback matched; API worktree was clean. Current-source
checker PASS after commit. Final documentation follow-up records this publication;
its exact SHA and authoritative completion URL live in Issue31's M3 dashboard.
[Start checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6374949198)
and [verified progress](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6375047517)
remain chronological history. Issue31 stays OPEN / Goldberg Development In Progress.

Next executor: recover actual HEAD/status and latest M3 Issue31 checkpoint first;
M2/M3 backend implementation is done. Smallest justified next milestone is separately
authorized client Detail/Comparison refetch plus explicit reconfirmation integration.
No cross-request snapshot, full-int8-write, public-version or real policy permission
is implied. No essential DTO semantics gap found for this bounded projection;
real-world disclosure policy remains deliberately unapproved/default-deny.
