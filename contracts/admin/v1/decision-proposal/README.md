# Admin v1 — approved design and inactive synthetic fixtures

**Owner-approved design with qualifications / unimplemented / inactive / synthetic / not frozen.**
O7 approves safety structure only; no real production policy is approved. No capture
is promoted into implemented HTTP behavior or integration acceptance. Review the complete
[decision document](../../../../docs/planning/goldberg-0.11/stage-6/API-V1-DECISION-PROPOSAL.md).

- `dto.schema.json`: structural JSON Schema definitions for the proposed request /
  response DTOs; `x-*` annotations express UTF-8 bytes, int8 ranges and plain text.
  These are not production policy or a route binder. Response consumers ignore
  unknown additive fields; requests reject unknowns recursively.
- `fixtures.json`: authored positive/negative boundary scenes. Status is null for
  structural request tests; HTTP statuses are proposed outcomes, never observations.
  The original UTC stats scene remains a historical, unselected proposal example.
  Current approved Europe/Prague cases live in design-approval-2026-10-23/.
  Audit hashes/cursors are placeholders, not chain/signature proofs. Structural
  payload acceptance does not grant source/field/license approval: actual policies
  remain empty/default-deny.
- `protocol.json`: eight unexecuted expected conflict/replay/revocation/uncertain-outcome
  scenes for future shared adapter tests, preserving key and independent revisions.
- `design-approval-2026-10-23/`: explicit owner record plus4 Prague civil/DST windows
  and10 NFC/case-insensitive/diacritic-sensitive literal CZ/EN search cases. Checks
  use host timezone/Unicode data only; no backend queries or policy activation.
- `metadata.json`: baseline/evidence identity, separate new-package hashes and old
  immutable-source/corpus hashes, G10 status, exact canonical local action vocabulary.
- `verify.py` / `test_verify.py`: dependency-free deterministic fixture checker and
  verifier regressions. Supported schema keywords are deliberately enumerated;
  this checks the package, not live HTTP/auth/SQL/Android behavior. Source policy,
  cross-field transitions, idempotency storage and cursor signatures require future
  acceptance tests listed in the decision document.
- `publish.py`: guarded normal push, append-only Issue31 planning dashboard section,
  and one idempotently marked checkpoint. Exact clean SHA/branch/origin/ancestry/
  planning-only diff/fixture hashes checked first; no PostgreSQL replay, deploy or
  release. A failure stops publication; guards are never bypassed.

```bash
python3 contracts/admin/v1/decision-proposal/verify.py
python3 -m unittest discover -s contracts/admin/v1/decision-proposal -p 'test_*.py'
python3 spikes/stage6-security-local/verify_contract.py
node spikes/stage6-security-local/tests/contract.test.mjs
```

No changes in the Stage7B worktree. Existing41 primary and29 comparison cases and
all provenance remain byte-preserved. New fixtures are separate from those captures.

created by stokonn with love <3

Current metadata ownerApproved=true means DESIGN ONLY WITH QUALIFICATIONS. The original
fixtures retain classification=proposal-unapproved as immutable authoring provenance;
that label does not negate the new design approval or imply production policy approval.
active/httpApproved/frozen remain false; concrete policy sets remain empty/default-deny.
Historical proposal and prior verification outputs remain pinned at fefa6e8.
