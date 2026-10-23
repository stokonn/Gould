# Stage 6 API local M1 — guarded source publication

Branch feature/goldberg-0.11-stage-6-api-v1-local, worktree
`/home/stokonn/Projects/Goldberg-Stage6-API`, baseline6dd3136269ec7f5932c8c4b3689fb065d4db9105.
Successful local verification permits source publication only; no public API activation,
formal6.13–6.18, freeze, Android integration, merge, cloud/OAuth or release.

After final exact commit, attempt ordinary push and factual Issue31 dashboard/checkpoint.
If DNS/integration permissions fail, retain local source/evidence, record exact errors,
verify prior issue history is unchanged and provide this command with FINAL_SHA replaced
by the actual final reviewed SHA (never unreviewed HEAD):

```bash
cd /home/stokonn/Projects/Goldberg-Stage6-API
python3 spikes/stage6-api-v1-local/publish.py --expected-head FINAL_SHA --verify-only
python3 spikes/stage6-api-v1-local/publish.py --expected-head FINAL_SHA
```

Guards: exact clean SHA/branch/origin/baseline ancestry, scoped source/docs paths and
exact two dependency-cache ignores; executed106+38 TAP and18 verifier results, source
before/after/current hashes, immutable old service/Android/contract/evidence diff, raw
artifact hashes and whitespace. Normal push only, remote SHA readback before writing;
append current Issue31 section, one SHA-marked M1 comment with duplicate detection,
preserve prior comments and verify readback. Concurrent edits stop for inspection.
No test rerun/DB launch in the publisher; no bypass or authentication escalation.
Generated readback stays in `.gradle/stage6-api-m1-publication/`.

created by stokonn with love <3

## Actual attempt — source and checkpoint unpublished

Implementation337847770a579e1f6689c135805ad07a9b2bab19 passed guarded local checks.
Ordinary push failed exit128 DNS `Could not resolve host: github.com`. Connected
branch lookup returns422 `No commit found`; no new remote SHA verified. Both Issue31
writes denied403 `Resource not accessible by integration`. Readback confirms original
description and81 prior comments unchanged,zero new M1 marker. No checkpoint URL.
Exact failed attempt/draft retained under package results/publication-attempt.json.
Final receipt commit keeps implementation source identical to the successful run;
use its exact final SHA in the guarded commands above on a permitted Fedora host.
