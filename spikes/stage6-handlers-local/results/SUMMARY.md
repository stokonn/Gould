# Local handler verification — 2026-10-23

Executed `spikes/stage6-handlers-local/run-local.sh` on the isolated branch
`spike/goldberg-0.11-stage-6-handlers-local`, baseline Auth `7993444`.
Node v22.23.1; pg 8.23.1; jose 6.2.13; PostgreSQL **17.11**, pinned image
`docker.io/library/postgres@sha256:e4cb93687b062cd25a0e8f672d85a7708235009d70d31f8a7698d9370092a9c4`.
Disposable loopback-only engine, synthetic identities, no host-data mounts/cloud.
Final runner exit **0**, container removed (label-filter readback empty).
Source commit is the containing implementation commit; source-sha256.json binds
all 10 implementation/test/package/runner files. No tested code changed afterward.

| Run | New handlers | Auth regressions | Schema regressions |
|---|---|---|---|
| Initial, initial.log | 77/81 passed, 4 failed | Unexecuted (runner stopped on failures) | Unexecuted |
| Corrected, second.log | 81/81 passed | 50/50 passed | 34/34 passed |
| Final, final.tap / final.log | **87/87 passed** | **50/50 passed** | **34/34 passed** |

Final: **171 passed, 0 failed, 0 skipped, 0 cancelled**. Separate final regression
artifacts: auth-regression.tap and schema-regression.tap. No benchmark rerun.
Original schema/Auth migration/source/hash manifests and old run evidence unchanged.

Initial failures: three new >9-event checks exposed inherited `ORDER BY seq` on
`seq::text`, sorting exports lexicographically. DB chain was intact. New exporter
uses qualified numeric `audit_events.seq`; original helper stays unchanged as
historical source. Fourth was a fixture helper hardcoding revision 0 for existing
revision-1 entities; fixed to use the observed revision. Original initial bytes are
retained in initial-original.log.gz with decompressed SHA-256; the readable log
removes trailing spaces on two blank diagnostic lines only. Failed counts and
stack traces are preserved. Corrected and final runs have no failures.

Executed local coverage (only these subchecks, **no entire threat row PASS**):

| Threats | Executed evidence | Limits |
|---|---|---|
| T6 | strict envelope/payload/target/field/evidence/external allowlists; author/role/status injection 400; server identity; caller mutation snapshot | No HTTP parser/browser/client surface |
| T7/T8 | private ownership SQL, ID guessing 404; cross-user read/revise/withdraw 404; bounded private lists; live read-role revocation | Android and hosted RLS unexecuted |
| T9 | actual handlers call verified SessionService/Authorization; moderator-only queue/actions, no self-approval; all 50 original Auth cases rerun | No real REST/RPC/service-role proof; DB owner trusted |
| T10 | simultaneous distinct moderators on one submission; distinct submissions sharing absent or existing entity; exactly [201,409]; coherent one publication/feed | In-process requests with independent real PG connections, not deployed load |
| T11/T12 | canonical sorted-object hashing includes operation and all fields; exact same-key response replay, concurrent same-key [201,201] with one effect; distinct principals isolated; mismatch 422; malformed UUID; ≥24h and exact expiry | Historical C6 same-key [201,409] is untouched, distinct behavior |
| T13 | schemaVersion; 16 KiB exact bounded-object threshold; 20 fields; 10 URLs; 2 KiB text/URL; depth/node/cycle/accessor checks; HTML/encoded script; malformed and unknown nested content | Object adapter, not raw HTTP/chunking/duplicate-JSON-key tests |
| T14 | canonical HTTPS/exact source+host; reject IP/local/private/link-local/v4-mapped forms, credentials, alternate port, fragment, backslash, unsupported schemes, overlong URLs; source/license rejection | **No network fetch/DNS/redirect handling exists.** Rebinding, live DNS and redirect-chain tests unexecuted; not a completed T14 fetcher |
| T17 | faults after lock/entity/snapshot/projection/approved/audit/feed, unexpected SQL, idempotency insert and deferred commit; exact pre/post snapshot equality and same-key retry; real lock timeout/recovery | No hosted outage/process-kill claim |
| T18 | immutable SQL update/delete denial; >9-event independent export recomputation; content/omission/head tamper detected; feed joins consistent | No full export/restore/RPO or hosted console drill |
| T21 | journal/notes/microphone/backup/sessions/repertoire/profile objects rejected at four nested levels; no uploads or arbitrary nested field values | No Android network assertion; arbitrary plain text may itself contain personal prose |

Also executed actual-handler malformed/expired/revoked sessions 401, self-approval
and role/suspension 403, unknown/existence-hidden 404, stale expected revisions 409,
mismatch 422 and fault/timeout 503 with sanitized logs. Publication snapshot,
evidence, typed UUID/kind and revision projection verified; revisions hash-linked,
old snapshots immutable; request ID matches stored response/audit. Exact named
idempotency primary-key recovery passes; unrelated unique/check/SQL errors are not
indiscriminately converted to 409. Reason, target immutability and terminal-state
checks exercised. No automatic approval/publication or unresolved mapping merge.

Five repository checks PASS (commands in host-checks.log): policy, three backup
fixtures, history paging 106 sessions/4 pages, 922 EN/CZ resources, migration DDL
v1–9→10. Original 22 schema/Auth source hashes verified, original directories have
zero diff from Auth baseline. New source/evidence hashes and diff/scope/link checks
PASS. No database migration introduced; no Android/Room/backup/signing edit.

Unexecuted: Gradle/JVM app/lint/APK builds, instrumentation/emulator/UI screenshots,
phone, real OAuth/JWKS/hosted Auth/RLS, HTTP/CSRF/CORS, quotas/kill switch, DNS or
fetching, public overlay/Admin/quarantine/rollback endpoints, full restore/RPO,
C2 §12 runtime or any cloud operation. No APK generated/hash/signing check applicable.
Only local backend and documentation changed. Stage 5 and Stage 7A/7B unchanged.

Unresolved contract: complete real contribution field/source/evidence/license
compatibility rules need approval; default Validator denies everything and tests
inject authored synthetic CC0 fixtures only. No license policy inferred or live
source rights verified. Relationship/numeric/structured edits fail closed in this
plain-text slice. The reviewed state patch was applied narrowly under the owner's
canonical direction; unknown external callers/unseen data remain unverified.

Q1/D5b, C4, C2 §12 and 6.0f remain unresolved, formal 6.4–6.6 unchecked; no cloud,
production collection, merge or release authorization. Next recommendation only:
separately authorized local **6.7–6.9**, not automatically started.

created by stokonn with love <3
