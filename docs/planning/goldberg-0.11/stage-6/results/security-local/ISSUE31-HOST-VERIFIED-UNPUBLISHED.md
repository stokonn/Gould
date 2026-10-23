UNPUBLISHED: integration write rejected403; direct gh API cannot connect. No new checkpoint URL.

## Stage 6.12 — fresh local PostgreSQL verification confirmed; PARTIAL

Isolated branch `verify/goldberg-0.11-stage-6-12-local`.
Local verification/tooling commit `a21dbafb1180563b88ed36909a801da179b5bf55`;
owner-tested original source `faf9ba3ec24e7098289c80d6e119519ccc21b95e`.

- [x] Independently inspected wrapper `run-vQd6iGn5` and nested PG17.11
  `run-qx9TUTDk`:27 focused +32 unit;50 Auth +87 handler +34 schema
  +34 integration +32 unit. **296 successful executions;264 tests when the
  repeated32 unit suite is counted once. Zero failures/skips/cancellations.**
- [x] All five repository checks PASS in both runners. All62 at-run source hashes
  matched before tooling edits;55 raw artifacts hashed. Wrapper stdout exactly
  matches nested TAP/check logs. Original failed/successful runs retained.
- [x] Cleanup output records removal of the disposable container. Numeric exit
  sidecar was not captured by v1; success uses owner report/result/control flow.
  Agent live Podman inventory is blocked by read-only runtime configuration.
- [x] Fix local runner's missing-summary NoneType failure: structured failure JSON
  now names missing/duplicate/truncated/inconsistent summaries and refuses false
  green reports. **10 reporting regressions PASS**, separately from296 host executions.
  Full version2 PostgreSQL runner NOT executed. No service/API/SQL/Android changes.
- [x] Matrix updated to fresh exact evidence:168 references; **13 local PASS,
  6 PARTIAL,2 NOT TESTED,1 FAIL**. Hosted [I] remains OPEN.
- [ ] **T22 remains FAIL:** audit-edit operation returns400 vs required403;
  no mutation handler dispatched or privilege bypass demonstrated. No silent waiver.
- [ ] T13 rendering; T14 DNS/redirect/rebinding/fetch; T15 HTTP/browser; T19 actual
  restore/RPO; T16/T20 runtime usability; T21 actual network privacy remain OPEN.
- [ ] Source/checkpoint publication: guarded normal Git publication stopped at
  remote lookup with `Could not resolve host: github.com`. Connected read confirms
  branch404. **Local source is UNPUBLISHED**; no force push or remote success claimed.

Publisher now requires successful actual counts, immutable evidence hashes,
tested-source ancestry and current tool/index hashes, preserving PARTIAL wording,
duplicate checks and Issue description/history. Local --verify-only passed.

Formal6.12 remains UNCHECKED/PARTIAL; Android runtime/emulator recovery deferred.
Stage5/7A/X10/7B untouched. No hosted/OAuth/real data/paid resources, emulator/phone,
merge/tag/release or6.13–6.18. No dashboard edit.

Next: normal guarded publication from a permitted host, exact remote readback,
explicit T22 contract/security review and remaining local assertions. The successful
host PostgreSQL run is now VERIFIED and must not be described as still blocked.

created by stokonn with love <3
<!-- stage6-security-host-faf9ba3 -->
