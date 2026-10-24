# Local synthetic query evidence

Actual PostgreSQL17.11/localhost/rootless Podman, synthetic5000 submitted work
headers,5000 valid immutable revisions and5000 corresponding created audit events
(plus synthetic session events). Every FK/chain/audit guard stays enabled. Tables
were ANALYZEd. Payloads are uniform with one synthetic field/evidence entry.

The probe intercepts actual server read SQL/arguments, executes
`EXPLAIN (ANALYZE, BUFFERS, FORMAT JSON)` on the same read-only connection and
checks statement_timeout=5s. It then lets the actual HTTP read execute normally.
There is no substituted response, changed schema, speculative index or optimizer
setting. Full JSON plans, query parameters, buffer/node/row statistics and wall
measurements are source-bound in each run's query-plans.json.

| Actual query | Before header bound (pg-ipgctj55) | New bound (pg-ojxz4mbi) | New revision rows × loops |
|---|---|---|---|
| queue-default | 13.148ms | 2.877ms | 21 |
| queue-keyset | 13.368ms | 2.871ms | 21 |
| queue-filtered | 0.487ms | 0.508ms | 21 |
| audit-filtered | 0.159ms | 0.257ms | not a revision query |
| stats-exact | 3.697ms | 3.684ms | not a revision query |

Before: default/keyset plans hash-joined all5000 revision rows, then top-N sorted
and limited. After: candidate headers are bounded first, so revision payload work
is≤21 for all three measured queue variants. The test and independent verifier
assert that measured bound; no particular index name or latency target is assumed.
Existing submission_queue(status,created_at,id) benefits the single-status filtered
case. Default/keyset still scan/sort eligible headers, an explicit remaining
bottleneck for larger distributions. A future partial/order index or query strategy
needs measured justification and its own approved schema review; none is added here.
Audit's measured selective walk uses audit_events_pkey; exact statistics still scan
relevant submissions/audit events and cannot be promised constant-time.

Hard local checks: page≤20, SQL limit+1=21, known query/filter caps, signed scoped
15m cursors,5s statement timeout, explicit503 on actual SQL timeout followed by
successful unchanged-data recovery. Bounded output is not a promise of bounded
rows scanned for every filter. Missing rare matches and other distributions may
scan far more data. Queue is a live page walk; only audit highwater/statistics have
their documented snapshot guarantees.

These are single local samples, not percentiles, sustained-load/concurrency
capacity, production/free-tier capacity or an approved final query budget. They
exclude provider network latency, RLS/hosted overhead, realistic payload variety,
contention, cold caches and infrastructure limits. No Supabase/Cloudflare/billing
resource or index change. C4/6.0f and final query budgets remain open.

created by stokonn with love <3
