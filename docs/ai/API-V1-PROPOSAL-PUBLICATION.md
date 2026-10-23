# Proposed API v1 package — publication handoff

Scope: planning-only from published baseline19f5642192148051a7b4eb5bc5efb1090192e3c0,
branch `verify/goldberg-0.11-stage-6-12-local`. Baseline checkpoint6370511889 is
verified through the connected GitHub reader. Earlier unpublished receipts in AI
history are superseded for that baseline; historical records remain immutable.

Current package is PROPOSED/INACTIVE, G10 CLOSED, G1–G9/G11 integration BLOCKED,
6.12 PARTIAL,6.17 NOT FROZEN. Owner must review O1–O8 in
`stage-6/API-V1-DECISION-PROPOSAL.md` before implementation.

Terminal GitHub reads in this session failed `error connecting to api.github.com`.
Connected issue/comments reads succeeded. No permission or DNS bypass is authorized.
Normal source push and issue checkpoint are attempted after a verified local commit;
publication must be reported from actual readback, never inferred from local commit.

On a permitted Fedora host with this exact clean worktree/source, use the final
40-character SHA from the final report:

```bash
cd /tmp/goldberg-stage6-security-local
python3 contracts/admin/v1/decision-proposal/publish.py --expected-head FINAL_SHA --verify-only
python3 contracts/admin/v1/decision-proposal/publish.py --expected-head FINAL_SHA
```

Replace FINAL_SHA literally with that SHA; do not substitute unreviewed HEAD. The
publisher checks branch/origin/ancestry, planning-only changed paths, source/fixture
hashes and15 offline tooling tests; checks remote ancestry, performs ordinary push,
requires exact remote SHA, preserves the entire Issue31 description by appending a
current section, appends one SHA-marked checkpoint, and reads back history preservation.
No PostgreSQL rerun, live endpoint, cloud, Android tree/build/device, merge or release.
If a concurrent Issue description edit is detected, stop and inspect before retry.
Generated readback receipt is local under `.gradle/api-v1-proposal-publication/`.
Do not invoke the older security publisher, whose checkpoint describes another milestone.

created by stokonn with love <3
