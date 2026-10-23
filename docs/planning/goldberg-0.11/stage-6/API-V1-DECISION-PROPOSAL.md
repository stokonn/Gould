# PROPOSED — API v1 decision package for Stage 6 → Stage 7B

Status: **PROPOSAL / OWNER REVIEW REQUIRED / INACTIVE / NOT FROZEN**. Proposal
identifier `admin-v1-decision-1`, 2026-10-23. This document is implementation-ready
planning, not approval or implementation of 6.13–6.18, and does not close 6.17.
No listener, route binder, production authentication or privileged action is enabled.
G10 remains **CLOSED_ANDROID_STATIC_COMPATIBILITY_CONFIRMED**. G1–G9/G11 remain
blocked for integration until owner decisions, implementation and tests pass.

## 1. Authority and evidence

| Authority | Meaning in this package |
|---|---|
| Normative design | API-CONTRACTS.md, DATA-MODEL.md, AUTH.md: existing design constraints, including versioned request bodies and unknown-request rejection. Not a complete approved success DTO. |
| Verified local behavior | Baseline `19f5642192148051a7b4eb5bc5efb1090192e3c0`; published [checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6370511889). In-process AuthLocalApi/CommunityApi and PostgreSQL handlers. 345 prior successful executions / 313 tests counting repeated units once / 205 PostgreSQL-backed bodies. No HTTP proof. |
| Android expectation | Read-only pinned working bytes in contracts/admin/v1/stage7b-readonly-source-sha256.json; CONTRACT-AUDIT.md and ACCEPTANCE.md in that tree. Legacy C6 is a distinct dialect. 275 recovered JVM tests are historical Android evidence, not executions by this task. |
| Proposed public binding | Everything below and decision-proposal fixtures. Authored synthetic scenes, never captured HTTP responses or approved policy. No mutation of earlier corpora/provenance. |

Readiness comparison remains in [STAGE7B-CONTRACT-READINESS.md](STAGE7B-CONTRACT-READINESS.md).
The latest Issue31 checkpoint is authoritative for local 6.12: T22 LOCAL PASS,
overall PARTIAL (14 local PASS / 6 PARTIAL / 2 NOT TESTED). The older Issue description
contains superseded implementation summaries; publication should append a current
planning section without rewriting historical checkpoints or implying acceptance.

## 2. Public HTTP binding (G7/G8)

One proposed Android bearer binding under `/v1`; HTTPS deployment is separately gated.
Browser BFF/cookie/CSRF integration remains conditional under AUTH.md; do not replace
that architecture with Android token delivery to browser JavaScript. No extra service,
paid index, cron, websocket or subscription is required. Use bounded PostgreSQL queries
and transactional snapshots in the existing data plane; resource budgets remain gates.

All JSON bodies use UTF-8, Content-Type application/json, no duplicate keys, finite
numbers only, max 16384 encoded bytes, depth ≤7 and ≤1000 nodes. Requests reject
unknown keys at every nested level, including reserved/prototype pollution names.
Responses permit unknown additive fields; required fields, types and enums remain
strict. Do not choose a dialect from response shape. No JSON body on GET or DELETE.
Every nonempty request body requires integer `schemaVersion:1`; schemaVersion is
not a query parameter. Responses carry `schemaVersion:1` and `requestId`, except 204.
All responses carry `X-Request-ID`; 204 has no JSON or Content-Type. Session/private
responses use Cache-Control:no-store. Bearer credentials never appear in URLs.

`requestId` is a server-generated UUID. Reject client body requestId; request identity
is not an idempotency key. Stored write replay returns the original HTTP status and
complete JSON result, including original requestId; X-Request-ID also equals that
receipt ID on replay. An internal fresh transport trace may correlate retries but must
not rewrite the stored receipt. This is an explicit future HTTP rule, preserving the
local exact-result replay invariant. Audit events keep the original operation ID.

### Scalar types and ranges

| Type/field | Exact wire representation / domain |
|---|---|
| UUID | lowercase canonical UUID string; IDs opaque, never numeric/provider IDs |
| schemaVersion | JSON integer exactly 1 |
| EntityRevision | canonical decimal string `0`…`9223372036854775807`; zero only explicit absent-entity expectation, never a default |
| SubmissionRevision, published entity revision | decimal string `1`…`9223372036854775807` |
| AuditSeq | decimal string `1`…`9223372036854775807`; highwater/after permit `0` for empty/genesis |
| Count | decimal string `0`…`9223372036854775807`, including statistics and evidenceCount |
| limit | query canonical decimal integer `1`…`20`, default 20; no signs/fractions/leading zeros |
| timestamp | UTC RFC3339 exact six fractional digits, e.g. `2026-10-23T12:00:00.123456Z`; server authoritative; pad millisecond auth clocks with zeros, never fabricate additional precision |
| hash | lowercase SHA-256 hex, exactly 64 characters; null only where explicitly allowed |
| cursor | opaque URL-safe unpadded base64url string ≤4096 characters, nonempty; null for end; never client JSON or colon-joined timestamps |
| q | optional nonblank plain text ≤200 UTF-8 bytes, no controls/HTML; no implicit locale normalization |
| reason | optional on approve; if supplied 1…1000 UTF-8 bytes nonblank; required on reject/request_changes |
| token/credential | opaque nonempty secret ≤8192 UTF-8 bytes each within body cap; no sample is a usable credential |

Decimal grammar is `0|[1-9][0-9]*`; no JSON numbers, signs, whitespace, exponents,
fractions or leading zeros for these BIGINT fields. Parse with BigInt on Node and
checked Long on Android (or validated decimal value objects); serialize without Number
or Int conversion. Reject overflow. Revision increment at MAX returns 409, rolls back;
no wraparound. Current local writes support safe JS integers only: the preferred full
int8 write range therefore requires separately authorized backend numeric changes.
Until those pass tests, unsupported writes must be unavailable, never narrowed/coerced.

Cursor design: server signs a versioned payload containing endpoint, principal and
capability scope, normalized filters/query, ordering, expiry and continuation key.
Audit also binds highwater and head hash. Queue binds filter scope and last
(created_at,id) but makes no cross-page snapshot claim. Validate signature, exact
payload schema, date precision, int8 bounds and route/scope match before queries.
Malformed/tampered/expired/wrong-filter cursor →400 validation_failed, no silent
restart. New filter starts a new explicit walk. Fixtures use authored opaque labels;
syntactic fixture validation does not prove signatures. Proposed TTL: 15 minutes,
owner approval pending; signing key rotation/expiry requires future tests.

### Endpoint registry (complete for this Admin package)

`S` = valid session; every protected request rechecks principal/session/live grants.
Body fields below are exhaustive in addition to required schemaVersion. ID is in the
path only; a future explicit mapper supplies local body.id, never accepts two IDs.

| Method/path | Capability | Body or allowed query | Success DTO/status |
|---|---|---|---|
| POST /v1/session/challenge | unauthenticated, throttled | body: schemaVersion only | 201 Challenge |
| POST /v1/session | credential verification | credential, challenge | 201 Tokens |
| POST /v1/session/refresh | rotating refresh credential | refreshToken | 200 Tokens |
| GET /v1/session | S | none | 200 Session |
| DELETE /v1/session | bearer identifying family | none | 204 empty |
| GET /v1/me/capabilities | S | none | 200 Capabilities |
| GET /v1/admin/stats | queue.read AND audit.read | none | 200 Stats |
| GET /v1/admin/queue | queue.read | status?, kind?, q?, limit?, cursor? | 200 QueuePage |
| GET /v1/admin/submissions/{id} | queue.read | none | 200 Detail |
| POST /v1/admin/submissions/{id}/open | queue.read, actor≠author | expectedEntityRevision, expectedSubmissionRevision, idempotencyKey | 200 OpenResult |
| POST /v1/admin/submissions/{id}/decisions | decide, actor≠author | decision, expectedEntityRevision, expectedSubmissionRevision, reason?, idempotencyKey | approve 201 ApproveResult; reject/changes 200 DecisionResult |
| GET /v1/admin/entities/{id}/comparison | queue.read | submissionId (required) | 200 Comparison |
| GET /v1/admin/audit | audit.read | action?, actorId?, subjectId?, from?, to?, q?, limit?, cursor? | 200 AuditPage |

Unknown query parameters or duplicate parameters →400. Public envelope/schema validation
precedes authentication, then live authorization, then receipt/business processing;
well-formed protected requests with no session401 and live capability denial403. status is one of submitted,
in_review,changes_requested; omission means all three. kind is one of work,
arrangement,artist,album,release,recording,collection. Audit action accepts one exact
canonical action; IDs UUID; dates use timestamps above and from<to. Other normative
contribution/admin operations remain outside this package; no audit-edit, bootstrap,
auto-approval or bulk endpoint is introduced. Account deletion remains reserved/gated.

### DTO catalogue

Notation: `{...}` means exactly listed required members; `?` optional, `T|null`
explicit nullable; `T[]` array. Every top-level success adds schemaVersion/requestId.
Lists are bounded at 20. Field projections cannot expose raw SELECT * rows.

```text
Challenge = {challenge:secret, nonce:secret, expiresAt:timestamp}
Tokens = {principalId:UUID, sessionId:UUID, accessToken:secret,
          refreshToken:secret, expiresAt:timestamp, refreshExpiresAt:timestamp}
Session = {principalId:UUID, sessionId:UUID, expiresAt:timestamp}
Capabilities = {principalId:UUID, sessionId:UUID, capabilities:Capability[]}
Capability = submit | submission.read.own | submission.revise.own | submission.withdraw.own
           | queue.read | decide | quarantine | audit.read | roles.manage
QueueItem = {submissionId:UUID, authorId:UUID, status:QueueStatus, kind:Kind,
             createdAt:timestamp, updatedAt:timestamp,
             expectedEntityRevision:EntityRevision,
             submissionRevision:SubmissionRevision, evidenceCount:Count,
             display:{title:Text|null, composer:Text|null}}
QueuePage = {items:QueueItem[], nextCursor:cursor|null,
             coverage:"page", searchScope:"id_title_composer_kind"}
Detail = {submissionId:UUID, authorId:UUID, status:SubmissionStatus, kind:Kind,
          targetEntityId:UUID|null, targetState:"new"|"existing"|"unmapped",
          expectedEntityRevision:EntityRevision,
          submissionRevision:SubmissionRevision, createdAt:timestamp,
          updatedAt:timestamp, revisions:RevisionPage, reviews:ReviewPage}
RevisionPage = {items:Revision[], nextCursor:cursor|null}
Revision = {submissionRevision:SubmissionRevision, payload:Payload,
            createdAt:timestamp, previousHash:hash, hash:hash}
ReviewPage = {items:Review[], nextCursor:cursor|null}
Review = {auditSeq:AuditSeq, submissionRevision:SubmissionRevision,
          actorId:UUID|null, from:SubmissionStatus, to:SubmissionStatus,
          reason:Text|null, createdAt:timestamp}
OpenResult = {submissionId:UUID, status:"in_review",
              expectedEntityRevision:EntityRevision,
              submissionRevision:SubmissionRevision}
ApproveResult = {submissionId:UUID, decision:"approve", status:"published",
                 submissionRevision:SubmissionRevision, entityId:UUID,
                 publishedEntityRevision:SubmissionRevision}
DecisionResult = {submissionId:UUID, decision:"reject"|"request_changes",
                  status:"rejected"|"changes_requested",
                  submissionRevision:SubmissionRevision}
Comparison = {submissionId:UUID, entityId:UUID, state:"available",
              entityRevision:SubmissionRevision, entityStatus:EntityStatus,
              payloadHash:hash, fields:CurrentField[]}
  | {submissionId:UUID, entityId:UUID|null, state:"unavailable",
     reason:"absent"|"unmapped"|"projection_unavailable"}
CurrentField = {field:Text, value:Text, evidence:Evidence[]}
Stats = {asOf:timestamp, auditHighwater:Count, window:Window,
         state:"exact", pending:Count, needsInfo:Count,
         approvedInWindow:Count, rejectedInWindow:Count}
  | {asOf:timestamp, state:"unavailable",
     reason:"window_unapproved"|"bounded_query_incomplete"|"service_unavailable"}
Window = {timeZone:Text, startInclusive:timestamp, endExclusive:timestamp}
AuditPage = {items:AuditEvent[], nextCursor:cursor|null,
             highwater:Count, headHash:hash, coverage:"filtered"|"chain"}
AuditEvent = {seq:AuditSeq, createdAt:timestamp, action:Text,
              actor:{principalId:UUID|null},
              subject:{kind:Text, id:UUID, revision:SubmissionRevision|null},
              beforeHash:hash|null, afterHash:hash|null, reason:Text|null,
              requestId:UUID, prevHash:hash, thisHash:hash, canonicalContent:Text}
```

SubmissionStatus = submitted,in_review,approved,published,changes_requested,
rejected,withdrawn; EntityStatus = published,superseded,quarantined. Text values
are plain text ≤2048 UTF-8 bytes unless overridden. Audit canonicalContent is the
unchanged canonical stored row (≤16384 bytes), not reserialized DTO JSON. Page byte
budget ≤262144; never truncate an event silently. Fail unavailable on oversized
valid historical rows. DTO renaming must retain canonical bytes for hash verification.
No actor email/name inferred; null actor means system/deleted policy, not current user.

Detail embeds at most 20 revisions/reviews. Additional history uses the same GET
submission path with mutually exclusive `revisionCursor` or `reviewCursor` query,
returning the same Detail with only that history page advanced; cursor binds history
kind and submission ID. This is an explicit exception/addition to that row's query
list above. UI must not claim complete history when nextCursor exists. Current
revision must be included on first page (revision descending); reviews seq ascending.

Payload and Evidence use the exact nested allowlists in §6. Comparison fields are
allowlisted text only and ≤20, with ≤10 evidence per field; missing catalogue values
stay absent/unavailable, never copied from a proposal. Display title/composer are
nullable ≤2048-byte reviewed projections, not newly invented catalogue metadata.
A denied comparison returns 404 (existence hidden), not an unavailable disclosure.

Error = `{schemaVersion:1,error:{code,message,requestId,details?}}`.
Allowed details only `{field?:Text}` with a public schema path (≤200 bytes), never
submitted value, tokens, SQL, identities or current-state guesses. No successful DTO
inside an error. 409 is deliberately generic; authoritative refetch is compulsory.

| HTTP | code / client action |
|---|---|
| 400 | validation_failed: malformed JSON/UTF-8/number/cursor, unknown field, byte/schema/policy violation; edit input, no blind retry |
| 401 | unauthenticated: missing/invalid/expired/revoked session; clear trust, authenticate |
| 403 | forbidden: authenticated but live role/suspension/self-review denied; clear privilege, no replay bypass |
| 404 | not_found: missing or existence-hidden resource |
| 405 | method_not_allowed: unsupported method; inspect Allow header, do not retry another method silently |
| 409 | conflict: stale entity OR proposal OR state/mapping/revision-exhaustion; refetch and reconfirm |
| 422 | idempotency_mismatch: same principal/key with changed operation/path/body |
| 429 | rate_limited: honor integer Retry-After seconds 1…86400, retain draft/key |
| 503 | service_unavailable: disabled/quota/provider/kill-switch; no fake success |
| 500 | internal: safe rollback error; uncertain outcome recovery with same key |

This preserves 400 for validation and reserves 422 for key mismatch, unlike APIs
that use 422 for all semantic validation. Do not change body and automatically retry
422. Local T22 absent-operation403 is preserved as a local routing rule; unsupported
public paths/methods propose 404/405 (Allow header on405), no protected dispatch.
This public routing distinction requires owner approval; no local behavior changes.

## 3. G5 — one explicit session lifecycle

1. Request a single-use challenge; bind returned nonce to platform credential flow.
   Credential exchange validates signature/algorithm, issuer, audience, expiry/iat,
   nonce and server challenge expiry/atomic consumption before creating identity.
   Internal principalId derives only from verified issuer+subject, never email/profile.
2. Server creates tracked family/session, returns Tokens. GET session validates it;
   capabilities returns principalId AND sessionId from the same authenticated context
   and computes grants live. FamilyId stays private. Client checks identity/session
   consistency before exposing Admin; mismatch clears trust/account-scoped caches.
3. Access expiry →401. Propose 15-minute access, 7-day absolute family, 5-minute
   challenge, ≤60-second provider clock skew. Refresh does not extend family expiry.
   These exact durations are pending owner approval, not current fixed local config.
4. Single-flight refresh consumes old refresh token, revokes predecessor access,
   rotates both tokens atomically. Used refresh replay revokes the entire family,
   commits session.refresh_replay event, then401. Never use moderation idempotency
   replay for login/refresh. Lost refresh response requires fresh authentication;
   retrying old refresh may revoke the family. Persist replacement pair atomically.
5. Every privileged operation checks live session/family/principal/grants inside its
   transaction before replay lookup; capability UI hints never authorize writes.
   Expiry/revocation401; suspended/role-denied403; no delayed expiry-based enforcement.
6. DELETE session revokes family, returns204 even for already revoked/unknown
   well-formed bearer; missing/malformed bearer401. Local logout is already204/null.
   Clear tokens/trust immediately even if offline. Account switching cancels pending
   requests and isolates drafts/markers by principal; delayed replies from prior
   account cannot update new account. Fresh challenge/exchange required for new user.
   **Known normative tension:** AUTH.md says queued logout retry after local secrets
   gone. Prefer no persisted bearer retry queue: failed remote logout remains
   unconfirmed, client says so, relies on server expiry and future separately approved
   account session management. Owner must approve this amendment; never claim offline
   logout proved server revocation or retain credentials in a backup to retry it.
7. No credential/access/refresh/challenge in logs, audit content, crash reports, URLs,
   backups or exports. Store only token hashes server-side; Keystore-protected
   app-private pair excluded from backup; body/header redaction tested in future.
   Synthetic fixture labels are deliberately unusable secrets. Rate-limit public auth.
8. Production stays unconfigured/inactive. Android DisabledAdminTransport and independent
   decisionsEnabled=false remain; startup/local practice never waits for network.
   A future disabled HTTP service returns503 before provider access/token minting.

**schemaVersion discrepancy decision:** public POST challenge/login/refresh require
schemaVersion:1. AuthLocalApi currently rejects it through operation-specific allowlists.
Approve a named, tested `admin-v1-decision-1` HTTP mapper: validate exact public schema
first, then explicitly project only credential/challenge/refreshToken to that local
operation; GETs map to empty local bodies. Never spread public JSON then silently
strip unknowns or retry another version. This adapter is proposed, not written here.
New expiry/session result fields require service DTO support; do not synthesize them
by decoding opaque tokens. Authentication POSTs are explicit exceptions to normative
“All writes authenticated/idempotent”: challenge/login/refresh use single-use security
semantics, no idempotencyKey. Logout is naturally idempotent, no JSON body/key.

## 4. G6 — moderation protocol and recovery

GET latest detail is a read, not “open”. POST open alone moves submitted→in_review;
requires both expectations, a UUID key, live queue.read and actor≠author. Proposal
revision does not increment on open/decision; new author revision increments it.
Only in_review may approve/reject/request_changes. Reject→rejected terminal;
request_changes→changes_requested; author revision→in_review; approve atomically
in_review→approved→published. No externally visible intermediate approved success.
Actor cannot approve their own work. No auto/bulk approval or silent conflict retry.

`expectedEntityRevision` explicitly names the entity baseline recorded on the
submission (local submission.expected_revision → local expectedRevision).
`expectedSubmissionRevision` names immutable proposal snapshot (local
current_revision → expectedSubmissionRevision). They are independent: entity7,
proposal3, published8 is the fixture boundary. Zero entity baseline is allowed only
when detail explicitly says new/absent; missing never means zero. Opening/rejecting/
requesting changes checks both against submission header; approval additionally locks
current entity (including absent-row lock), validates mapping/state and compares live
entity revision before publishing. A new entity is not inferred from a missing lookup.

If current catalogue revision differs from submitted baseline, client must show it
and block approval; no “retry with latest” rewrite of the proposal. Return to contributor
revision/rebase workflow, separately authorized. Reconfirming does not waive mismatch.
Published revision is the next immutable entity revision, not proposal revision.
Reject/changes return proposal revision but no invented new entity revision. Approve
returns201 even updating an existing entity because it creates a published revision;
open/reject/changes return200. Replay returns original status/body, including201.

Client keeps immutable operation intent/path/body/key scoped to principal; refresh
access credentials may change, intent may not. Operation identity hashes canonical
public version + method + canonical path + exact validated body (key included), not
headers or JSON whitespace/key order; no normalization of strings/reasons/revisions.
The explicit mapper must prove this preserves local operation+body identity. Same key
with changed input/path/operation→422; never reuse it for a changed confirmation.
Retention ≥24h (propose exactly24h from original completion). Store exact public
result in the transaction, rather than rebuilding a replay DTO from changed rows.
Live auth check happens before receipt lookup: revoked role403/session401 even if
old key had succeeded. Same-body authorized replay has no second audit/feed/review.

On timeout/disconnect/5xx: retain draft and stable key, show outcome unknown. After
valid current session, explicitly retry exact intent within receipt retention;
response is the authoritative receipt. GET detail can show current state but does
not prove which request caused it. On409 refetch latest detail and authorized current
comparison, show changed proposal/entity/state, invalidate confirmation, require
explicit acknowledgement and new confirmation/new key for any newly permitted intent.
No loop that substitutes revisions. If key retention elapsed or replay is denied,
refetch and show unresolved/administrator review as needed; never blindly issue a new
approval to “recover”. No separate unauthenticated receipt endpoint is proposed.

## 5. G1–G4 and G9 — reads, statistics and audit

**G1:** preferred server stats uses ONE bounded read-only REPEATABLE READ snapshot
for queue counts, audit highwater/head and reporting bounds. pending counts submissions
with status submitted OR in_review; needsInfo counts changes_requested. Counts are
current at asOf, not sums of events. approvedInWindow counts ONLY events with exact
`in_review->approved`; rejectedInWindow ONLY `in_review->rejected`, created_at in
[startInclusive,endExclusive), seq≤highwater. approved->published is retained but
counts zero approvals. Atomic publication makes the two approval transitions co-commit.
No COUNT DISTINCT by submission (would hide invalid duplicates); duplicate illegal
transitions are an integrity failure, not silently repaired statistics.

Recommend UTC civil-day window `[00:00Z,next00:00Z)`, server chosen, all dates absolute;
owner may choose Europe/Prague instead. **Timezone/day boundary/window definition
PENDING OWNER APPROVAL.** No client ZoneId.systemDefault determines “today”. Until
approved/implemented, Stats unavailable/window_unapproved. Bound query work to
measured free-tier limits; timeout/incomplete snapshot →unavailable, no approximate
counts. Existing approved bounded exact-or-unavailable fallback remains permitted
only where completeness and a coherent reporting window can be proven; concurrent
mutable queue walks cannot prove an instantaneous global count. Never invent totals.

**G2:** queue display uses stored reviewed title/composer projection if available,
otherwise null with unavailable UI. Evidence count counts evidence entries on current
proposal, not downloads/URLs guessed by client. Server search exact UUID match OR
case-insensitive literal substring (no wildcard/regex semantics) over available title,
composer and kind. Define Unicode normalization/collation in the owner decision before
implementation; recommend NFC + PostgreSQL deterministic lower-case comparison,
fixture-test CZ/EN diacritics and `%_` literals. Do not promise transliteration/fuzzy or
missing-field search. Deterministic ascending(createdAt,id); cursor does not claim
snapshot isolation across calls. Search scope is explicit, never “whole catalogue”.
Rows may change during paging; refresh explicitly. Preserve bounded detail hydration/
exhaustive-or-unavailable fallback; do not label searched fetched pages exhaustive.

**G3:** audit filters combine AND; q literal substring only in action, subject UUID,
and safe reason text, case-insensitive as above. actorId/subjectId exact; action exact;
from inclusive/to exclusive. No token/profile/raw canonical-content search. Filters
run server-side before limit. Signed cursor scopes all filters and caller, fixes
highwater/head from first snapshot. Subsequent pages return seq ascending ≤highwater,
new events require explicit new walk. Highwater=head sequence at snapshot, not count
of matched events. Filtered gaps are expected; coverage=filtered must never claim
contiguous hash-chain verification. Unfiltered export coverage=chain can verify links
through highwater; missing/gapped pages cannot establish integrity/completeness.
Empty history highwater0/head all-zero hash; nextCursor null at end of matching set.
If role/scope changes, deny/restart explicitly; no cursor privilege carryover.

**G4:** comparison is authorized by both submission and entity scope. Return only
reviewed current published-entity fields/evidence plus actual revision/hash/status,
not a prior proposal, public title or inferred before-value. Missing/unmapped or
unsupported projection yields unavailable union. UI displays absent fields honestly;
current-value response cannot replace the submitted baseline or authorize a decision.

**G9:** preserve canonical local audit actions, no `decision.approve` alias:
submitted->in_review, in_review->approved, approved->published,
in_review->rejected, in_review->changes_requested, changes_requested->in_review,
submitted->withdrawn, in_review->withdrawn, and actual role.*, principal.*, session.*
actions as recorded by the corresponding operation. The exact additional local actions are submission.created, role.granted,
role.revoked, principal.suspended, principal.unsuspended, session.created,
session.rotated, session.refresh_replay, session.logout, admin.quarantine and
admin.rollback. Do not replace admin.quarantine/admin.rollback with invented
state-transition aliases or invent events for operations not implemented. Unknown additive action strings display
as raw safe text and count toward no known decision metric. Sequence, not wall clock,
defines authoritative chronology; equal/regressing timestamps do not reorder history.
Actor is internal principal UUID or explicit null; subject carries original kind/id/
revision, never a client account identity. Exact existing vocabulary is pinned in
proposal metadata; future action additions need documented metric impact.

## 6. G11 — enforcement-compatible validation

Android changes reason validation from legacy2000 characters to UTF-8 encoded bytes
≤1000 (Kotlin UTF-8 byte array length), same plain-text/nonblank rule as server. Show
remaining bytes; a 251-emoji reason fails while250 emoji fits the byte cap if otherwise
valid. Reject malformed Unicode before counting. Do not silently trim/truncate, decode
HTML or normalize entered reasons; exact text is part of idempotency identity.
Server remains authoritative at confirmation and transaction time.

Payload top-level allowlist: schemaVersion,target,changeKind,fields,intendedLicense,
contributorNote?. target: kind,catalogueUuid?,externalIdentity? (at least one identity);
externalIdentity: provider,providerKind,externalId. field item: field,newValue,evidence.
Evidence: type(source_url|reference),value,license. No author/role/status/publication/
personal practice fields anywhere. Fields1…20 unique; per-field evidence1…10;
source_url total≤10, URL≤2048 bytes, reference≤500, license≤80, note≤1000;
newValue reviewed plain text max≤2048 per field; provider/providerKind≤40,
externalId≤128. Payload/request total≤16KiB; metadata depth/node caps above apply.

Typed external identities use current local provider/kind syntax validation; valid
syntax is not source/license approval. Sources must match exact reviewed type/value/
license/kind/field/intendedLicense tuple. URLs HTTPS, exact reviewed host+URL; no IP,
userinfo, custom port, fragment, redirects, backslashes, trailing-dot host or embedded
credentials. Evidence stored only; no fetching in this scope. Plain text rejects
angle brackets, C0/DEL controls, encoded-dangerous forms and javascript:/data:/vbscript:.
No files, HTML, arbitrary nested values or third-party text mirroring. Current fields,
sources and intended-license policies remain **empty/default-deny** for actual use;
synthetic fixture-only allowlists never grant production permissions. Owner must
approve exact policies and attribution/provenance projection before any real content.
All these validation/policy failures400; authentication401, authorization403,
state/revision409, changed-key reuse422 stay distinct. No policy implies legal approval.

## 7. Compatibility and migration

| Gap | Existing Android/local mismatch | Preferred future change |
|---|---|---|
| G1 | No server stats; device-local day fallback | Exact snapshot DTO or unavailable; no new total guessed |
| G2 | Legacy owner/revision/epoch/string cursor; candidate lacks display/search | One explicit QueueItem projection, nullable labels, opaque scoped cursor |
| G3 | Client fetched-page filter only | Server filters/search, snapshot highwater; retain limited UX label meanwhile |
| G4 | No actual before-values service | Authorized Comparison union, unavailable first |
| G5 | Android requires principal; candidate capabilities omits it; logout JSON expectation | Session-derived principalId/sessionId;204 handling; explicit lifecycle/version mapper |
| G6 | Single Int proposal expectation/no open/all-success legacy result | Explicit open, two revisions, typed result unions and exact replay |
| G7 | Raw rows/underscore hashes/object queue cursor/no binder | Named public DTO and method/path mapper, microseconds |
| G8 | Android Int revision; local safe Number writes/PG string reads | Checked decimal-string int8 end to end; no fallback cast |
| G9 | Legacy decision.* aliases/device zone | Original transitions, approval counted once, approved server window |
| G10 | Application transport and decisions disabled | CLOSED; retain both controls |
| G11 | Android2000 chars vs backend1000 bytes | UTF-8 validation mirror plus authoritative default-deny server policy |

These are **breaking** versus legacy C6 and portions of normative design: renamed
entity expectation; string numeric revisions; changed success shapes/status handling;
capability session binding; session auth/idempotency exceptions; schemaVersion HTTP
projection; precise timestamp/cursor/reporting semantics; generic conflict/refetch;
o persisted logout retry secret. No deployed approved v1 exists in this evidence;
owner may approve this as the first public v1 baseline, but must explicitly retire the
legacy test dialect. If any approved/deployed v1 consumer is identified, publish this
under v2 instead, never silently change /v1. No dual-dialect shape detection.

Migration sequence after decision: pin reviewed package hash → separately authorize
numeric/service DTO changes and HTTP mapper → backend projection/transaction tests →
shared fixtures through one Android facade with decisions disabled → isolated HTTP
and Android runtime acceptance → security/cloud/legal gates → separate freeze decision.
No app database/backup migrations required for this planning. Token/local caches must
be versioned/invalidated by future client code without deleting drafts or practice data.

## 8. Exact owner decisions still required

| ID | Recommended choice; alternatives/consequence |
|---|---|
| O1 G5/G7 | Approve explicit lifecycle/HTTP schemaVersion mapper and204 logout; confirm no durable secret-bearing offline logout queue amendment. Otherwise specify safe revocation recovery. |
| O2 G5 | Approve access15m/family7d/challenge5m/skew≤60s, single-flight refresh/lost-response reauthentication and family revocation. Current lifetimes remain injected, production inactive. |
| O3 G6 | Approve expectedEntityRevision + expectedSubmissionRevision, explicit open, result unions/statuses, generic409/refetch/reconfirm and24h exact receipt semantics. |
| O4 G7/G8 | Approve decimal-string full int8 domain, microseconds, DTO projection/cursor15m scope and public404/405 routing; authorize future numeric changes separately. If compatibility exists, choose v2. |
| O5 G1/G9 | Approve metric definitions and choose UTC day vs Europe/Prague day (or explicit different window). This decision is deliberately pending; no timezone selected by agent. |
| O6 G2/G3 | Approve nullable metadata, literal search scope/NFC/collation, audit filters and privacy/coverage; choose bounded budget after measurement. No completeness beyond declared scope. |
| O7 G4/G11 | Supply/review exact kind/field/source/license/attribution policies and current-value projection. Remain empty/default-deny until approved; no synthetic policy promoted. |
| O8 sequencing | Accept package as design only, then separately authorize implementation/testing. Production activation,6.17 freeze, hosted provisioning/OAuth, merge/release remain separate decisions. |

## 9. Future acceptance tests and blocked gates

- Backend: schemaVersion missing/extra/nested/duplicate fields; auth projection known
  version only; challenge consumption races, issuer/audience/nonce/JWKS rotation;
  access/family expiry, lost refresh response, consumed-refresh replay revocation,
  signout offline/204/revoked session, suspended principal and live grant change before
  stored moderation replay; redaction and backup exclusion.
- Moderation: unequal entity7/proposal3; missing expectation; explicit zero versus null;
  open wrong state/self; all three outcomes200/201; author revision; simultaneous
  approve/open/reject; row+absent-entity locks; rollback at every audit/feed boundary;
  exact public receipt replay original requestId; changed path/body422; denied replay;
  expiry24h and uncertain outcomes;409 never auto-resubmitted. Prior PG proof remains
  evidence for its source, not new public mapper correctness.
- Wire: int8 MAX and9007199254740993 round trips; numeric JSON/leading zero/fraction/
  overflow rejected; revision increment exhaustion; microsecond keyset ties; malformed/
  tampered/wrong-principal/expired/filter-changed cursors; query bounds; unknown response
  additive fields accepted without dialect switch; GET bodies rejected;204 no JSON.
- Reads/audit/stats: concurrent queue mutation → no global exact-walk claim; bounded
  stats snapshot timeout unavailable; paired approval events count once; sequence
  ordering despite clock skew; filtered highwater/gaps not chain verification; new
  events excluded from existing walk; UTC/Prague DST boundaries after window approval;
  nullable labels, literal wildcards, CZ/EN normalization, hidden entity404 and missing
  field unavailable; no proposal snapshot mislabeled as current catalogue.
- Android: shared positive/negative fixtures against the one approved adapter;
  principal/account generation switch, stale delayed responses, protected token pair
  persistence, explicit open/dual revision UX/receipt recovery, byte-limit accents/
  emoji1000/1001, server policy rejection preserving draft. Preserve disabled production
  transport and independent action control; future authorized runtime/UI/upgrade and
  accessibility tests remain mandatory, no execution claimed here.

Formal6.12 remains PARTIAL; browser/CORS/CSRF, SSRF DNS/fetch, restore/RPO, network
privacy and runtime/hosted checks open. 6.13–6.18 remain unimplemented/unaccepted by
this task;6.17 NOT FROZEN. Q1/D5b, C4, C2§12 and applicable cloud/zero-cost/legal gates
remain unresolved; no compliance or capacity proof added. Stage7B acceptance BLOCKED,
X10 OPEN, Stage8 pending. Stop at this owner decision gate.

Fixtures and deterministic verifier: contracts/admin/v1/decision-proposal/README.md.

created by stokonn with love <3
