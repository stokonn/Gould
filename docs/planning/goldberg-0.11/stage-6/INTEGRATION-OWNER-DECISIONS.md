# Stage 6 integration owner decisions

This is a consolidated decision package, not an approval record. Until an
owner decision is recorded, the Android transport and public HTTP binding stay
disabled and G1–G9/G11 remain blocked.

## Recommended defaults

1. **Shared contract:** approve one versioned fixture set derived from the
   Stage 6 normative design, with explicit public DTOs and no dialect inference.
2. **Identity:** capabilities include the authenticated `principalId` and
   `sessionId`; every protected request rechecks the live session and grants.
3. **Queue/audit:** use signed opaque cursors bound to principal, filters,
   ordering, expiry, audit highwater and chain head; reject legacy cursor
   strings rather than silently converting them.
4. **Decisions:** retain explicit `open`; require separate entity and
   submission expectations; return the new published entity revision only on
   approval; preserve exact idempotent replay.
5. **Numbers/time:** use decimal strings for PostgreSQL `int8` values and
   six-digit UTC timestamps; unsupported writes fail closed instead of rounding.
6. **Errors:** freeze the existing 400/401/403/404/409/422/429/503 envelope
   and keep unknown fields rejected.

## Alternatives and consequences

| Decision | Recommended | Alternative | Consequence |
|---|---|---|---|
| Cursor | Signed opaque cursor | Legacy timestamp/string cursor | Loses filter binding and precision guarantees |
| Revisions | Dual entity/submission values | One legacy `expectedRevision` | Cannot safely map stale decisions |
| Queue search | Server-bound filters | Client exhaustive walk | Incomplete results under concurrent changes |
| Stats | Exact server snapshot or unavailable | Partial client count | Mislabels incomplete data as authoritative |
| Current comparison | Allowlisted current projection | Proposal as “before” values | Displays incorrect catalogue history |
| Numeric writes | Full checked `int8` or unavailable | JS safe-number coercion | Rounding or incorrect revision mutation |

## Decisions required before integration

- Approve the shared fixture version and public `/v1` or `/v2` binding.
- Approve queue display/search fields, audit vocabulary, statistics window and
  timezone semantics.
- Approve current-catalogue comparison fields and source/license policy.
- Approve session/token lifecycle and Android credential handling.
- Authorize implementation and isolated HTTP/backend integration tests.

These choices are not implied by local tests or the Stage 7B comparison corpus.
G10 remains closed only for the disabled Android transport boundary.

created by stokonn with love <3
