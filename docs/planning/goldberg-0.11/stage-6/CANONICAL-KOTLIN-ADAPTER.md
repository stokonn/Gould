# Stage6 canonical Kotlin adapter — local contract side

The new data/community/canonical package implements the approved shared DTO/API
semantics against the unchanged contracts/admin/v1/decision-proposal/dto.schema.json.
AdminContract takes that exact reviewed schema explicitly; Stage8 must bind its
source/version when wiring the application. It supports the schema's used rules,
not arbitrary JSON Schema extensions. No public version is selected or frozen.

The client exposes challenge/login/session/capabilities/refresh/logout, server
queue/statistics/audit, Detail/Comparison, explicit open, confirmed decisions and
explicit exact uncertain-request replay. Transport is mandatory injection; no
HTTP/socket implementation, URL configuration, storage, scheduled work or production
activation is added. Tokens stay in memory and diagnostic representations omit
payloads/secrets. Existing disabled Community transport/application code is unchanged.

Decoding requires strict bounded JSON, exact required field types, full signed-int8
decimal strings, six-digit UTC timestamps, Unicode and byte limits. Known fields
validate while additive response fields remain permitted; requests reject unknown
members. Null labels remain null. Expensive schema decoding runs off the main
thread. The codec remains a synchronous pure API for callers using it directly.

Session/account generation guards reject stale responses; identity mismatch clears
authentication. Refresh is single-flight; loss requires fresh authentication. Logout
clears locally before I/O and confirms revocation only on empty204. 401 clears tokens;
403 invalidates review/uncertain intent. Offline/validation/forbidden/conflict/422/
quota/unavailability stay distinct. No raw upstream error is displayed.

Decisions require explicit acknowledgement, live capabilities, fresh Detail and
Comparison, both revisions and matching current values; self-review/unavailable
existing comparison/rebase conflicts deny before POST.409 clears pending intent;
lost outcomes retain original body/key for explicit replay, bounded24h. No automatic
retry/rekey/publication exists. Audit verification binds canonical event fields to
the exact stored bytes/hash, preserves fixed highwater/head/coverage across a walk
and rejects account changes, gaps or tampering. No totals are derived from pages.

Integration handoff: Stage8 must preserve the accepted Stage7 UI/Keystore/settings/
re-review semantics while binding canonical models/session/store/services explicitly.
Resolve public-version and real disclosure policy first, retain G10 until separately
authorized networking, then execute the shared fixtures in the actual adopted client,
Android lifecycle/offline/upgrade/UI tests and hosted authentication. New JVM adapter
acceptance does not close those native/consumer integration gates. Other worktrees
were read only; no wholesale merge or fixture/provenance replacement occurred.

created by stokonn with love <3

Final portability check: duplicate-key detection is enforced by the strict wire
scanner itself, including escaped aliases, independently of Android/host org.json
behavior.20 focused canonical tests PASS; final whole host suite234 PASS. Earlier
source-bound19-test acceptance is preserved rather than relabeled.
