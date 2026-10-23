UNPUBLISHED: normal Git push DNS failure; GitHub integration write rejected403.

## Stage 6.12 — approved T22 correction and Stage7B handoff; local update UNPUBLISHED

Local commit `262a76ed08c7910e9a4ad86018188589efc58c37` on
`verify/goldberg-0.11-stage-6-12-local`. Normal push failed DNS resolution.
Remote still `e7dc7ea871f61965600ba31076e8d28dc01822fe`, independently read back.
Published checkpoint6369071469 and all historical raw evidence preserved.

- [x] Traced composed400 before auth/dispatch versus Auth adapter403. Owner explicitly
  approved the prepared local policy/patch: malformed/non-string/empty operation400;
  well-formed unregistered audit/bootstrap operation403 without dispatch;
  registered missing/invalid session401; capability/self-grant403.
- [x] Applied exact approved patch and corresponding local assertions; no audit-edit/
  bootstrap endpoint or privilege added. Rejects array property-key coercion.
- [x] Final119 PASS:38 T22 +11 shared-contract +27 security +32 unit Node tests,
  plus11 reporting tests; zero final failures/skips/cancellations. Five checks PASS.
  Intermediate harness failures retained. T22 tests use real services + scripted SQL,
  not physical PostgreSQL. Denied cases issue no protected role/audit writes.
- [x] T22 now PARTIAL after explicit owner decision and local correction, not full
  acceptance. Original FAIL evidence remains historical; matrix13 local PASS,
  7 PARTIAL,2 NOT TESTED.
- [x] Stage6→7B normative/backend/actual Android/proposal ledger for allG1–G11.
  G10 remains CLOSED on Android via read-only static compatibility; G1–G9/G11
  remain blocked for real integration. No Stage7B files modified.
- [x] Shared corpus:30 actual in-process/scripted-storage captures +11 inactive
  proposals +29 preserved authored Stage7B comparison scenes. Exact hashes;
  no HTTP/PG/frozen-contract claim. Session/capability/schemaVersion, queue/detail/
  open/dual revisions, replay409/422, audit/cursors/BIGINT, all eight error classes,
  before/after projection and approval gaps are explicit.
- [ ] Updated PostgreSQL suites: preflightEPERM, zero bodies. Prior host296 executions/
  264 tests counting repeated suite once is PRE-PATCH evidence only.
- [ ] Actual HTTP binding, G1–G4 functionality, G5–G9/G11 approvals/implementation,
  hosted/browser/fetch/restore/network privacy and Android runtime remain OPEN.
- [ ] Current update publication blocked; guarded Fedora publisher preserved and
  local --verify-only passed. No new remote SHA claimed.

Reports: `stage-6/T22-INVESTIGATION.md`,
`stage-6/STAGE7B-CONTRACT-READINESS.md`, `contracts/admin/v1/readiness.json`.
Production networking and privileged actions stay disabled. Formal6.12 PARTIAL;
no6.17 freeze or6.13–6.18, hosted/OAuth/real data/paid/merge/tag/release/emulator/phone.
No Issue dashboard edits. Next: publish normally from permitted host, run updated
PG17.11 tests, review exact shared DTO/session/revision/validation decisions.

created by stokonn with love <3
<!-- stage6-t22-handoff:262a76e -->
