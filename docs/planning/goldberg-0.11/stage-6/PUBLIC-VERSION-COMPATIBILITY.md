# Public version compatibility — evidence and owner-gated proposal

No public API version was selected, activated or frozen by this batch. Existing
local provisional `/v1` routes remain unchanged. O4's conditional owner approval
requires resolving approved/deployed incompatible consumer scope.

## Observed facts and limits

- Stage7B `0887d3f` has an actual local/synthetic `/v1` consumer with legacy single
  numeric revisions, queue/detail/audit shapes and JSON logout expectations.
  Selected actual Kotlin JVM replay proves concrete incompatibilities with the
  current canonical Stage6 DTOs. Its production transport/actions remain disabled.
- Immutable Android release tagsv0.9.2/v0.10/v0.10.1 contain no Admin client files
  under the searched application admin package. Recorded source-history and exact
  disabled-client/version command outputs are in the shared package's
  `version-evidence.json`. These are searched-source facts, not deployed inventory.
- Existing C6 spike/legacy authored contract and Stage7B's synthetic acceptance do
  not establish an approved/deployed public HTTP contract. No external consumer,
  owner approval record for a deployed legacy binding or global absence proof was
  obtained. **External/deployed consumer state remains UNKNOWN.**

Thus: incompatible local consumer CONFIRMED; incompatible approved/deployed public
consumer NOT CONFIRMED; global absence NOT PROVED. The owner version gate stays open.

## Concrete `/v2` proposal if an incompatible approved consumer is confirmed

Use the **same canonical schema and fixture package**, with a separately approved
public prefix. Do not invent another DTO dialect or accept legacy aliases.

| Proposed route | Canonical method/result |
|---|---|
| /v2/session/challenge | POST EmptyRequest →201 Challenge |
| /v2/session | POST LoginRequest →201 Tokens; GET →200 Session; DELETE →204 empty |
| /v2/session/refresh | POST RefreshRequest →200 Tokens |
| /v2/me/capabilities | GET →200 Capabilities including principalId/sessionId |
| /v2/admin/queue | GET bounded status/kind/q/limit/signed cursor →200 QueuePage |
| /v2/admin/submissions/{id} | GET →200 Detail |
| /v2/admin/submissions/{id}/open | POST OpenRequest with dual revisions →200 OpenResult |
| /v2/admin/submissions/{id}/decisions | POST dual-revision request →201 ApproveResult or200 Reject/ChangesResult |
| /v2/admin/entities/{id}/comparison?submissionId={id} | GET →200 Comparison/ComparisonUnavailable |
| /v2/admin/audit | GET bounded canonical filters/cursor →200 AuditPage |
| /v2/admin/stats | GET →200 exact Stats or explicit unavailable behavior |

These are proposed names only: **no `/v2` route exists from this work**. Shared
package version1 is not a public route-version decision.

Migration consequences: update Android's route constant and canonical DTO/session
model together; explicitly clear stale account/endpoint bindings and require fresh
identity where necessary. Do not migrate opaque credentials or pending privileged
intents silently between protocol versions. Resolve uncertain receipts under their
original route/key/account within their original24h context; do not resubmit under
new names, because the receipt operation namespace includes the route. Never add a
Number-to-string or cursor/DTO guessing adapter on a public path. A new backend
binding needs separately approved compatibility/security/runtime/hosted acceptance,
operator/consumer inventory and any deprecation window; no automatic dual-write,
public legacy listener, production family revocation or sunset is authorized here.

Alternative if the owner establishes that no incompatible approved/deployed `/v1`
consumer exists: authorize adoption of the canonical fixture package under `/v1`,
then explicitly migrate the disabled Stage7B synthetic client. Repository search
alone cannot satisfy that condition. Until the owner resolves scope, keep the
existing local routes provisional, G10 disabled and public activation/freeze gated.

created by stokonn with love <3
