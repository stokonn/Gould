# Stage4 additive contract extensions, revision3

The Stage2 v1 files remain historical inputs. This addendum versions the following implementation extensions without changing personal schema10, catalogue schema1 or portable backup5. No network operation occurs in migration.

## Source observation2

Source identity is `(provider, typed providerKind:externalId, providerRevision|sha256=payloadHash)`; the original provider revision is retained independently in each field assertion JSON. Two different bodies claiming the same upstream revision are separate immutable observations. The hash binds observations to their own body, not an API software version. Repeated identical observations reuse the source row and do not duplicate field assertions. Public source URLs refer to the typed entity; raw active query text is not persisted in source/attribution URLs.

## Unresolved type observations2

A search hit without sufficient structural evidence is a device-local candidate containing opaque observation UUID, provider namespace/ID, title, type hint, observation time, source revision/hash and reason. A hint does not establish the catalogue kind. It cannot be added to repertoire. An explicit detail lookup can accept a typed entity under the observation UUID; cross-provider mappings still require explicit cross-ID evidence and compatible kind. Existing accepted incompatible identities remain conflicts; there is no cross-kind redirect or personal UUID rewrite.

Candidate reads are capped at200 observations and40 displayed matches. Global durable candidate/diagnostic retention remains unfinished; do not interpret read bounds as a complete storage-eviction policy.

Typed pending relationships wait until both provider identities resolve. They retain provider endpoints, relation kind, explicit position/medium, source hash/time. Invalid graphs, duplicate positions and incompatible endpoints become quarantined local diagnostics. No unknown movement or ordering is fabricated. A recording's work reference can resolve to a work or arrangement. Structural part edges are child→parent; arrangement derivation, recording performance and edition/track containment remain separate relations.

## MusicBrainz core projection2

Reviewed against the official [API](https://musicbrainz.org/doc/MusicBrainz_API), [core-data license](https://musicbrainz.org/doc/MusicBrainz_Database/Download), [Parts](https://musicbrainz.org/relationship/ca8d3642-ce5f-49f8-91f2-125d72524e6a), [Arrangement](https://musicbrainz.org/relationship/51975ed8-bbfa-486b-9f28-5947f4370299) and [recording URL types](https://musicbrainz.org/relationships/recording-url). Direct document fetches of some individual relationship pages returned429 during resumed review; no retry/proxy bypass was attempted. Cached primary documentation supplied Parts direction/ordering and recording streaming relationship UUIDs. Live compatibility remains unverified.

Additive allowlist: core relations `type`, `type-id`, `direction`, `ordering-key`, reviewed `attributes` movement label, typed endpoint id/title/name; composer/arranger/lyricist artist credits; bounded release-group→release, release→release-group and media.position/tracks.position/title/recording.id/title; recording length/isrc assertions; core aliases; primary/secondary album type. Only explicitly supported semantics are accepted; unreviewed relation fields are dropped. Nested endpoints pass their own typed projection rather than copying arbitrary objects. Tags/genres/user ratings/annotations/editors/cover-art remain blocked.

Recording external listening fields permit only the core URL resource bound to exact free-streaming/streaming type UUIDs, on a validated destination/shape. Arbitrary hosts, title-derived links, authentication and playback remain excluded. Release streaming links stay blocked until their distinct relationship-type evidence is verified.

## Personal optional-link1

Existing personal identity evidence may append `catalogue-link-v1:` JSON containing version1, optional catalogue UUID/kind, exact provider/kind/externalId keys, explicit selected-part UUIDs and original title/creator snapshots. The identity registry's personal canonical UUID and immutable anchor remain independent and unchanged. Portable5 preserves the entire evidence string; readers1–4 remain unchanged. The extra evidence is not an OS permission, provider credential, practice duration or publication license. Missing/corrupt catalogue data cannot prevent personal rendering/export/recovery.

## Local search cursor2 and projected response cache2

Local cursors encode version2, normalized query/category/artist/period hash, catalogue generation, sort identity `relevance-id-v2`, final rank and canonical UUID tie-breaker. Old sort/version, changed query/filter/generation and malformed IDs/ranks are rejected without closing or recreating the catalogue. Provider continuation is not represented by this local cursor and remains an unfinished Stage4 requirement.

Projected response envelope version2 retains adapter-selected public records/relationships/fields, original accepted observation/revision/hash/license, freshness and conditional validators. It excludes request URLs and raw query text. The filename hashes the public request URL; an atomic access index records opaque filenames and monotonic access times to remain deterministic on API26 filesystems. Default response budgets32MiB/200 entries/30days are client guardrails. Cache disposal does not remove accepted normalized catalogue assertions or personal data. The aggregate256MiB catalogue guard and128MiB reserve are provisional; dynamic critical-export reserve and full accepted-revision retention/eviction remain unfinished. Discover history stores at most20 UUIDs separately; transient query text is not saved by the UI.

created by stokonn with love <3

## Revision4 — active-search continuation1 and projected cache3

Discover continuation tokens are opaque, process-local `discover1:` UUIDs. Server continuation offsets are accepted only from explicit provider response evidence, integral/nonnegative/progressing and bounded at10000. Tokens bind exact trimmed query, category, artist/period filters, language, explicit-online state, catalogue generation, append/relevance/UUID sort, stable last identity, already emitted identity sets and per-provider/per-kind offsets. Eight active sessions,12 page transitions,500 accepted/candidate identities per session and15-minute idle expiry bound retained memory. Incompatible/expired/generation-changed cursors fail before a provider request. A repeated continuation returns the same memoized page, including under concurrent callers. Displayed rows keep their order while later observations append; unseen local rows precede further provider requests. Search scores never establish identity. Raw queries and cursors are not persisted. A cancelled shared-request owner permits an active waiter one bounded takeover; cancelled waiters cannot cancel a successor's request.

Projected response cache3 adds an optional verified provider continuation offset. Legacy envelope2 remains usable offline but is stale, loses conditional validators and requires unconditional refresh before trusting paging. An unsolicited304 cannot certify that refresh. Neither response format changes personal/catalogue schemas or portable backups.

## Revision4 — unresolved observation cache1

Only `candidate:`, `pending-edge:` and `diagnostic:` state rows enter this disposable cache. Each prefix is capped at200 rows, each value at32KiB; writes evict least recently accessed rows, including legacy rows without an access index. A persisted monotonic access index handles same-tick access and wall-clock rollback. Paired access entries are removed together; aggregate saturated eviction counts/times remain available. Repeated identical relationship evidence reuses its slot independently of retrieval time; distinct response hashes retain separate observations within the bound. Candidates retain their observation UUID while cached. An evicted unresolved observation is not a verified catalogue identity; later acquisition can create a new observation UUID. Accepted entity UUIDs, immutable assertions/source evidence, identity revisions, redirects and graph rows never enter this eviction path. This completes bounded unresolved observations, not accepted-ledger lifecycle/storage acceptance.
