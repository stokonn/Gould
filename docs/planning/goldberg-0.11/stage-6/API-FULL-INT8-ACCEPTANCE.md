# Stage 6 — full-int8 local engineering acceptance

Status: LOCAL ENGINEERING ACCEPTANCE VERIFIED, 2026-10-24. Source publication pending.
[Verified acceptance checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6384808424). Branch
`feature/goldberg-0.11-stage-6-api-v1-local`; recovered published baseline
`0142e997f3a63a026c76219d8dcb04185b66e7ee`. The owner authorized completing the
interrupted shared-handler numeric work; O1–O8 design qualifications still apply.

## Implementation and preserved boundaries

- The public bridge passes canonical decimal strings through to the shared
  handlers. Unsafe numeric input, signs, leading zeros, fractions, underflow and
  values above signed PostgreSQL int8 maximum are rejected before dispatch.
- Shared validation permits legacy exact safe integers and canonical int8 strings;
  entity expectations are nonnegative and submission expectations positive.
  `incrementRevision` uses BigInt and returns decimal strings, with generic409 on
  int8 overflow before writes. Both publication and author revision use it.
- Existing session/idempotency/submission/entity/audit locks and transactions are
  retained. Publication allocates after the maximum immutable published revision,
  including after rollback. Full-range results/receipts/feed/audit avoid Number.
- Schema, migrations and transaction utilities are unchanged. The separate
  `publishSynthetic` prototype retains its explicit numeric-envelope `safeInt8`
  check; current HTTP writes use ModerationService and shared receipt utilities.
- No trigger or chain guard is disabled to seed a high submission revision.
  PostgreSQL tests exercise actual submission1→2 chains with maximum entity
  expectations; scripted service tests cover above-safe submission increments
  and maximum submission overflow. This is not a claim to have created billions
  of immutable PostgreSQL submission predecessors.

## Historical integrity and current evidence

Original `metadata.json`, `source-provenance.json`, fixtures, validators, source
pins and captured raw evidence remain unchanged. The design corpus has61 unique
cases:37 positive and24 negative; all retain `proposal-unapproved` classification.
The new DTO validator checks the original expected validity, including bodyless204,
request/response extension semantics, replay equality, int8 and audit accounting.

`current_provenance.py` checks historical metadata against immutable design commit
`6dd3136269ec7f5932c8c4b3689fb065d4db9105`, including the source baseline
`19f5642192148051a7b4eb5bc5efb1090192e3c0`. Frozen pins are checked against original
Git blobs; unchanged frozen artifacts are also checked on disk. Only the three
explicitly approved shared handlers may diverge from the historical source list.
The separate current manifest inventories implementation, migrations, tests,
package/lockfiles, runner/verifiers and gate inputs; its verifier recomputes the
complete inventory and hashes. The runner hashes this manifest before/after too.
Build the manifest before running acceptance; never regenerate it mid-run.

`verify-historical-provenance.py` materializes exact original blobs in a temporary
tree and executes the unchanged18-test suite, including its source pin comparison.
It also runs the original41/29 corpus verifier in pinned-client mode, without
consulting the evolving Android worktree. `--security` runs the unchanged17
historical security-evidence regressions on original source and verifies retained
raw artifact hashes. Their prior345/313/205 execution evidence is not rerun or
reclassified as current. Current T22 and PostgreSQL suites execute separately.

The frozen validator must still fail on evolved current handlers. A final run
retains this as `historical-pin-current-source-EXPECTED-FAIL.log`, never a PASS.
Current-source DTO/schema evidence has its own `current-dto-schema.log`.

## Verification commands and evidence

```sh
python3 spikes/stage6-api-v1-local/build-current-provenance.py
python3 spikes/stage6-api-v1-local/run-http-postgres.py
python3 spikes/stage6-api-v1-local/verify-http-pg.py RUN_DIRECTORY
```

Run on the authorized host for rootless Podman/loopback networking. No image pull,
external database URL, host mount or shared database. The runner uses a unique
container, cached image ID, loopback ephemeral port and synthetic data; it removes
its own container/anonymous volume in finally. The independent verifier rechecks
exact container/volume absence live. `--recorded-only` checks saved evidence and
current hashes without claiming fresh Podman probes.

Test selection derives from actual source paths and the API package's test script.
Counts derive from executed TAP bodies, plan and summaries; file-level opaque TAP,
failures, skips, cancellation and missing suites are rejected. Result counts are
recomputed independently, rather than maintained as hand-edited constants.

Final run: `spikes/stage6-api-v1-local/results/http-pg/pg-kr8b1q9g`.
Runner exit0; acceptance PASS; before/after/current source unchanged; cleanup
`removed-and-absence-verified`. Independent verifier exit0 using the documented
relative path; live exact container/volume probes both exit1 (absent).
Receipt: `results/recovery-int8-2026-10-24/final-independent-verifier.log`.

| Executed check | Actual result |
|---|---|
| Existing HTTP→PG routes | 24/24 PASS |
| Full-int8 real HTTP→PG cases | 13/13 PASS |
| Independent PostgreSQL auth/schema/handlers/operations/M2/M3 | 50/34/87/34/19/16 PASS (240 total) |
| Current Node API package selection | 122/122 PASS |
| Current T22 scripted/unit regressions | 38/38 PASS |
| Operations unit suite | 32/32 PASS |
| Historical design suite on original snapshot | 18/18 PASS |
| Historical security-evidence suite on original snapshot | 17/17 PASS |
| Current TAP parser regressions | 11/11 PASS |
| Current DTO/schema proposal expectations | 61/61 match:37 accepted/24 intentionally rejected |
| Actual HTTP read captures | 39 Queue/Audit/Stats DTOs match approved schema |
| Actual PostgreSQL Comparison captures | approved schema PASS |
| Current provenance before/after | 181 inventoried files plus manifest; all182 run hashes unchanged |
| Historical pins/raw artifacts | 52 metadata pins, original provenance and security raw hashes PASS on correct baseline |
| Frozen pin applied to evolved current handlers | EXPECTED FAILURE retained; never marked PASS |
| Agent policy / portable backups / ACL proposal / whitespace | PASS; three portable backup fixtures retained every original field |

All469 Node bodies passed with0 failures/skips/cancellations. Python evidence is
reported separately above. The source-set SHA256 is
`b7c6db2d251429e7edbbcef4ae3095a915ee8d5330ac7233254e4573b075c745`.
Venue: rootless Podman, PostgreSQL17.11, cached image ID
`82903352d6faf8f8a318bc4353d1cb1db67d4a5ff968d2799b2718e2cb00afa2`.
Disposable container `goldberg-api-http-pg-88252f5025fe4a20a3db859abfe313a5` and
volume `ec6a6368e0d3934dfb2c861aa804b139ea72102f1fbb9f544c0bdd86d9351529`
were independently confirmed absent. No remaining implementation/test failure;
publication/CI tracking follows separately.

Retained attempts:

- Interrupted `pg-t9tcc9wr`: operations regression expected a Number; preserved.
  Its two Node-generated whitespace lines remain byte-exact, covered by a single
  exact-path `.gitattributes` exception consistent with the existing raw-log policy.
- Interrupted `pg-ltqtci3x`: int8-PG10/10, overall historical design-pin failure.
- Intermediate `pg-dwsackgr`: all matrix checks PASS and absolute-path independent
  verifier PASS; relative-path command comparison failed. Failure and successful
  output retained separately. The final normalization fix is covered by the final run.
- Recovery `pg-wnj24c4d`: all469 Node bodies PASS, then strict historical security
  pin failed on evolved moderation. Overall FAILED, source unchanged, cleanup
  verified. The historical security suite now runs on original source.
- `results/recovery-int8-2026-10-24/sandbox-api-FAILED.tap`: restricted HTTP
  attempt, not body-level acceptance. Initial host API118/118 retained separately.
- Existing M2/M3/HTTP runs and their original manifests/raw evidence are preserved;
  their original current-file verifiers are bound to their historical code.

## Remaining gates

Real policies remain empty/default-deny and title/composer null. Public `/v1`
compatibility is unconfirmed; consumer G1–G9/G11 integration, client refetch and
explicit reconfirmation, final query budgets, C2 §12, C4 and6.0f remain open.
C4 remains8 PASS/19 PARTIAL/6 OPEN; Stage6 OPEN,6.12 PARTIAL,6.17 NOT FROZEN.

No production Supabase/Cloudflare/OAuth, real users/credentials, Android/other
worktree edits, device/ADB, main merge, release or deployment. No local Android
build/JVM/lint/instrumentation/UI/screenshots/APK/signing verification was executed
or required for this backend-only task. Static backup/ACL checks do not rerun the
previous encrypted-restore or ACL SQL venue. GitHub's existing quality workflow
is inspected separately after ordinary push; no release is authorized.

created by stokonn with love <3
