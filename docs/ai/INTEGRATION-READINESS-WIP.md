Historical intermediate checkpoint only. Final accepted state and exact next steps
are in HANDOFF.md and stage-6/INTEGRATION-READINESS.md. This file preserves the
chronological unfinished-work record, not current remaining work.

# Stage6/7B integration readiness — live implementation checkpoint

Branch feature/goldberg-0.11-stage-6-api-v1-local, published baseline/HEAD
`a3fb7e2f1436c93b49a0f4f0596d8d06e241fcef`; Stage7B read-only source pin
`0887d3fe33cf5d49d46e14e1de95168f08a493a2` matches remote. No other worktree writes.

Implemented, acceptance pending:
- contracts/admin/shared/v1/:64 actual HTTP scenario definitions,22 actual selected
  Kotlin JVM client replay cases,34 compatibility matrix rows, consumer source and
  version/history evidence, canonical consumer reference (not Android transport).
- shared-contract-postgres.test.mjs:17 real HTTP/PG test bodies covering session
  lifecycle, destroyed refresh/decision response streams, exact replay, conflicts/
  explicit reconfirmation, roles/accounts, queue/audit cursors, coherent stats/DST,
  policy/outages/timeouts, unknown fields and maximum int8.
- query-budget-postgres.test.mjs:1 actual-query EXPLAIN body over5000 valid seeded
  submissions/revisions/audit events;5 plans, caps/5s timeout; no index changes.
- consumer-compatibility.py + ConsumerCompatibilityProbe.kt compile unmodified
  selected original Kotlin AdminApi/Models/WireValues/Stats from Git into/tmp,
  using cached compiler/dependencies; no Android runtime or network activation.
- Backend recognizes explicit UTF8 JSON charset, retaining strict JSON/UTF8/DTO
  validation and rejection of unsafe legacy aliases. Existing venue exposes URL.
- Current provenance uses NEW integration-readiness-source-provenance.json;
  original full-int8 manifest pinned byte-exact to baseline. Runner integrates all
  new suites plus469 baseline Node bodies, historical18+17/parser11/DTO61 gates.

Evidence:
- pg-d8h0hv95:16/17 shared tests PASS; incorrect authored suspension401 expectation
  failed on actual established403. Raw failure/source-unchanged/cleanup retained.
  Corrected scenario expectation after inspecting current authorization semantics.
- pg-ipgctj55 running:shared17/17 and query1/1 PASS; baseline suites/JVM replay and
  independent final verifier still pending. Do not edit source during execution.

Remaining:
1. Finish running venue, inspect failures/JVM actual outcomes/EXPLAIN metrics.
2. Tighten reference invalidation of old acknowledgements after403 and comparison
   entity status/current unavailable conditions; add meaningful regressions.
3. Independently validate matrix classifications/static facts and version evidence
   in verify-shared-contract.py (currently capture/JVM/plan checks only).
4. Document exact consumer handoff/G1–G11, measured query limits and gated concrete
   /v2 proposal. No global/external consumer absence is proven; published Android
   tagsv0.9.2/v0.10/v0.10.1 have no Admin client sources; Stage7B local C6 consumer
   is incompatible but disabled. No public version choice/route activation.
5. Final unchanged-source full matrix+independent live cleanup, docs/ai/checklists/
   Issue31/PMP, scoped commit/push, exactremote and exact-SHA CI. Preserve original
   two untracked M2/M3 evidence dirs, all old logs/pins/privatePMP. No app/Stage7,
   production/provider/OAuth/device/merge/release/deploy.

created by stokonn with love <3
