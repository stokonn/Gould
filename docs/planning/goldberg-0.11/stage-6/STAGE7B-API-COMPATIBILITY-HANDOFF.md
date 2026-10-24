# Stage 6 API v1 to Stage 7B compatibility handoff

**Status: local comparison only; no HTTP contract is frozen and no production
adapter is enabled.** Stage 7B sources were inspected through immutable Git
objects. Its worktree was not modified.

## Evidence boundary

`contracts/admin/v1/fixtures.json` and
`contracts/admin/v1/stage7b-comparison/fixtures.json` keep normative design,
candidate-local captures, legacy C6 references, and inactive proposals
separate. The corpus is synthetic and does not prove HTTP, hosted
authentication, Android runtime, or transaction behavior.

## Compatibility findings

| Area | Stage 6 local candidate | Stage 7B expectation | Result |
|---|---|---|---|
| Identity/session | Session and capabilities are separate; capabilities omit principal identity | Principal/session lifecycle is required by the client | BLOCKED; approve one DTO |
| Queue | Fixed 20-item page and structured cursor; no search/status/limit inputs | Search/filter/display fields and legacy string cursor | BLOCKED; no dialect inference |
| Detail | Proposal revisions/reviews with exact allowlists | Legacy detail shape and current-field comparison | BLOCKED; current catalogue projection absent |
| Revisions | Entity and submission expectations are distinct | Legacy client exposes one expected revision | BLOCKED; never substitute or default |
| Decisions | Explicit open; approve may append two transitions; replay is exact | Legacy decision verbs and one revision | BLOCKED; approve dual-revision semantics |
| Audit | Signed opaque cursor, highwater and hash head; raw action vocabulary | Legacy action aliases and chain-head shape | BLOCKED; preserve raw events |
| Numbers/timestamps | BIGINT strings and six-digit UTC timestamps | Int/Long and epoch-millisecond legacy fields | BLOCKED; require explicit wire representation |
| Errors | Typed 400/401/403/404/409/422/503 semantics | Legacy conflict/validation mapping | BLOCKED; approve public envelope first |
| Android transport boundary | Disabled by default and independently gated | Runtime integration still required | CLOSED static boundary; integration OPEN |

## Required next approval

Before any adapter or listener is implemented, approve a versioned shared
fixture set covering session/capabilities, queue filters and cursor encoding,
detail/current comparison, explicit open, dual revisions, audit vocabulary,
error envelope, numeric ranges, and validation/license policy. Until then,
retain exact-or-unavailable behavior and the disabled Android transport.

G10 remains `CLOSED_ANDROID_STATIC_COMPATIBILITY_CONFIRMED`; G1–G9 and G11
remain integration-blocked. This handoff does not change those classifications.

created by stokonn with love <3
