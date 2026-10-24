## Concurrent work observed during preparation

Initial inventory found clean tracked trees in all15 existing worktrees. A later preservation check detected new uncommitted changes in `/home/stokonn/Projects/Goldberg-Stage6-Cloud` (ten tracked paths, plus new untracked readiness work). They were left untouched, not copied/merged, and are not accepted input evidence or a gate-closing decision. Published preparation pins remain unchanged; re-read the Cloud worktree's eventual published checkpoint/HEAD and Issue31 before dependent integration. All2203 originally inventoried untracked files remained byte-identical, including private PMP; Stage7B tracked/untracked evidence and existing APK were preserved. The complete private audit is retained outside Git.

# Stage 8 — Stage6 dependency and authorization ledger

**OPEN dependencies; local foundation verified independently, not Android/backend live acceptance.** Pinned Stage6 branch `feature/goldberg-0.11-stage-6-api-v1-local` at `006fa577ff4faef8ee7a25daf851620feb5bc3a8`. [Immutable backend readiness ledger](https://github.com/stokonn/Goldberg/blob/006fa577ff4faef8ee7a25daf851620feb5bc3a8/docs/planning/goldberg-0.11/stage-6/INTEGRATION-READINESS.md), [public-version proposal](https://github.com/stokonn/Goldberg/blob/006fa577ff4faef8ee7a25daf851620feb5bc3a8/docs/planning/goldberg-0.11/stage-6/PUBLIC-VERSION-COMPATIBILITY.md). Stage6 source is not in the preparation branch.

Historical local foundation:490 Node bodies passed,22 unmodified legacy Kotlin probes (12 compatible/10 incompatible),34 compatibility rows (12 compatible/12 incompatible/7 unexecuted/2 blocked/1 N/A). These are source-bound backend/reference/parser observations; no Android UI, lifecycle, live hosted sign-in or production adoption is proved. They were not rerun for8A. Incompatibilities are known gates, not an excuse to weaken client assertions.

| Gate | Retained status | Required before integration acceptance |
|---|---|---|
| G1 statistics | OPEN |Canonical server statistics; no fabricated client derivation; freshness/asOf/day boundaries. |
| G2 queue | OPEN |Canonical nullable models, actual server filtering/search, signed live keyset pagination. |
| G3 audit | OPEN |Canonical vocabulary/subjects/content, chain/highwater/cursor semantics. |
| G4 Comparison | OPEN |Actual current Detail+Comparison, absent/unavailable distinction; no invented before-values. |
| G5 sessions | OPEN |Actual Android login/refresh/logout, single-flight/account guards, revoke/expiry/lost-refresh handling. |
| G6 moderation | OPEN |Dual revisions, acknowledgement tuple invalidation/refetch,409 explicit reconfirmation; identical lost-response replay only in original account/route, no automatic re-key. |
| G7 HTTP/errors | OPEN |Bodyless logout204, session methods/errors and actual public/browser/hosted acceptance. |
| G8 int8/time | OPEN |Canonical decimal/string→bounded signed-int8 models without Int truncation; microsecond timestamp handling and overflow rejection. |
| G9 shared/version | OPEN |Actual Android consumer adoption of shared fixtures; owner public-version/compatibility decision. |
| G10 disabled boundary | CLOSED only for current disabled transport/actions |Preserve forApplication ContractUnavailable and decisionsEnabled=false; does not authorize activation. |
| G11 validation/policy | OPEN |Unknown/unsafe/null fields, plain-text reasons≤1000 UTF8 bytes and approved real source/field/license policy. |
| C4 | OPEN (retained8 PASS/19 PARTIAL/6 OPEN) |Complete documentary/legal/operational readiness from the authoritative current Stage6 tracker; no compliance completion inferred from local tests. |
| 6.0f | OPEN / unchecked |Evidence review and explicit owner decision before dependent cloud/production work. |
| Android/backend live integration | OPEN, blocked pending authorization |Actual approved consumer/server/session contract acceptance; synthetic UI evidence does not close it. |
| Production authorization | OPEN |Explicit permission and verified prerequisites for networking, real users/data, provisioning/activation/deployment. |

Other retained gates:6.12 PARTIAL,6.17 NOT FROZEN; C2 operations, controller/privacy/terms/licensing, auth/OAuth configuration, export/restore, zero-cost/quota and real policy approvals remain separate. This is a source-pinned ledger, not a substitute for newer Stage6 decisions. Refresh Issue31 and Stage6 handoff before dependent work, preserving concurrent updates.

**Stop rule:** preparation may continue; dependent integration/activation/release may not. No gate is closed by Stage7 acceptance, a backend prototype PASS, a new worktree or this checklist. No owner approval for main merge, deployment or release is inferred.

created by stokonn with love <3
