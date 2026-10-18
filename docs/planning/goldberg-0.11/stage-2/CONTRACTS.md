# Stage 2 contract v1 — finalized logical boundaries

This contract supersedes the unresolved local-design choices in Stage 1 for the owner's D1/D2/D3/D6/D7/D9 instructions. It is a specification for subsequent implementation, not shipped behavior. Normative enums/required keys: [contracts.json](contracts.json). Non-normative examples are [authored fixtures](fixtures/catalogue.json), [bundle examples](fixtures/bundles.json) and [backup envelope](fixtures/backup-v5-envelope.json). Fixture UUIDv5 is deterministic only for test repeatability; production entity IDs are opaque random UUIDv4. Do not derive production IDs from mutable titles.

## Identity and music semantics

Every entity has required `id: UUID`, `kind: enum`, `title: nonempty original string`, `revision: positive int64` and `status: active|redirect|quarantined`. Dates have nullable value plus explicit year/month/day precision; unknown is null, not January 1. Original text and script are preserved. All numeric legacy identifiers remain strings within a provider namespace. UUIDs identify catalogue records, not personal repertoire rows or recording URLs.

| Kind | Required payload / identity boundary |
|---|---|
| work | workType=composition/movement/part/medley/mashup. Movement/part has ordered parent relation; independently selectable. Medley/mashup has its own ID and zero or more known source-work edges; unknown sources explicitly remain unresolved. |
| arrangement | Instrumentation string (may be empty/unknown), one or more source-work edges when verified, independent creator/arranger credits. Two piano arrangements of a song remain distinct even when titles coincide. |
| artist | artistType=person/group/other; original and sort names, aliases with language/script; dates optional. Namesakes retain separate IDs. |
| album | albumType=album/ep/single/soundtrack; maps to a release family, not one recording. Keep provider primary/secondary type assertions as well; soundtrack is the explicit display label when verified. Unknown types stay labeled unknown rather than guessed. |
| release | Parent album ID; edition/date/country/label/catalogue/barcode assertions; ordered medium/track positions with credited titles and recording links. Reissues are distinct. |
| recording | Optional nonnegative durationMillis, ISRC assertions, artist credits, many performed-work/arrangement links. Duration never determines practice credit; metadata licenses do not license audio. |
| collection | Ordered typed members with stable member UUID/position. Artist/work/album/custom-list offline targets supported. Duplicate members may be intentional; duplicate positions are invalid. Nested collection membership is excluded in v1 to bound expansion. |

Credits are relationship records: entityId, artistId, role, creditedAs, position and source assertion. Preserve unknown credited strings without inventing an artist. Composition part and derivation graphs must be acyclic. Recording→performance targets are works/arrangements; members cannot be collections. Typed foreign keys plus transaction validation enforce these rules. No source error may fill missing facts, genres or biographies with invented data. The eight query categories in the research sample are sampling strata, not a complete genre taxonomy.

`ExternalIdentity(provider,providerKind,externalId)` is unique and maps to one current entity, with evidence/review state and source revision. A provider's `87` never equals another provider's `87`. Open Opus numeric work/composer IDs, MusicBrainz typed MBIDs and Wikidata QIDs stay distinct. Missing IDs yield an unresolved candidate with stable local observation ID, never an empty external primary key. Cross-provider mapping needs explicit verifiable cross-ID evidence or reviewed corroborating creator/catalogue/version facts. Name/title/search-score alone is insufficient.

Merges are reversible same-kind redirects with reason/evidence/reviewer and monotonic revision; reject self/cyclic redirects and cross-kind merges. Preserve source identities and all pre-merge assertions. Reversal creates a new mapping revision, not a journal rewrite. Ambiguous mappings remain candidates. The fixtures' reviewed redirects are explicitly fictional and do not establish real provider equivalence.

## Provider adapter result v1

Required fields and types:

| Field | Type / rule |
|---|---|
| contractVersion | integer 1; incompatible major rejected before persistence |
| requestId | opaque UUID, correlates cancellation/generation |
| provider / providerKind / externalId | strings; externalId nullable only for search, providerKind always typed |
| observedAt | UTC RFC3339 timestamp; not a provider edit date |
| revision | string or null at envelope level; each accepted record has a provider revision or content hash |
| rights | array of provenance records keyed to individual fields/documents/assets; never one blanket license for a composite |
| data | array of typed candidate/accepted records; empty on error; explicit missing differs from unavailable |
| state | enum in contracts.json, with optional error {code,retryAfterUtc,detailCode}; no raw token/PII/server trace |

Open Opus may have both composer-only and work entries; do not trust status.rows as array length. MusicBrainz search ranking does not establish identity. MediaWiki JSON `error` is a failure even with HTTP200; `maxlag`, `ratelimited`, 429 and 503 defer the provider, not “no results.” Respect Retry-After (and maxlag backoff), cancellation and response-byte bounds. On failure retain the previous accepted revision. An unversioned provider uses SHA256 of canonical stored response bytes plus observedAt; API software version/response creation time is not an entity revision.

Field projection is **default deny**, defined by [source-field-policy.json](source-field-policy.json). Unknown nested content never passes through merely because a parent is allowed. Raw provider responses are local disposable research/cache material, not automatically redistributable fixtures. Provider identity, license and transport success are separate checks.

## Search result v1

Required envelope: `contractVersion:1`, `queryId:UUID`, `query:string`, `category:all|works|artists|albums|lists`, `items:array`, `sources:array`, `nextCursor:string|null`. Each item has entityId/kind/original title, disambiguation/credit display, matchedAlias or null, provider evidence, local/offline status and explicit candidate/verified state. Each source has state/fetchedAt/error independently. A rate-limited source is not complete or empty.

Works includes compositions, movements, parts, arrangements, medleys and mashups with type labels. Albums includes albums, EPs, singles and soundtracks; releases/recordings are linked details in v1, not new top-level categories. All groups the four supported result categories and removes only proven same-identity duplicates. Preserve All/My Repertoire filtering independently from search category. Local personal rows are available when catalogue/network is absent, using opus.db anchors.

Normalization is accent/punctuation-insensitive matching only, never replacement of original titles or automatic translation. Use normalized aliases/catalogue identifiers plus artist credits. Prototype B-tree handles leading-prefix queries; FTS4 handles bounded multi-token prefix matching. It does not claim arbitrary substring equivalence. Escape/build FTS expressions from normalized tokens, not raw query syntax. Apply source/genre/status filters through indexed joins, not global in-memory materialization.

Cursor v1 is an opaque encoded object {queryHash,category,catalogueGeneration,sort,lastRank,lastStableId,providerCursors}; validate all bindings before use. Provider offset and local rowid are never interchangeable. A generation change invalidates the cursor and offers restart; late responses carrying an older queryId cannot overwrite current results. Deterministic ties use entity ID within one generation. Search relevance/ranking measurements beyond the bounded prototype remain Stage 4 work, not an excuse to rebuild the entire catalogue at startup.

## Provenance, revisions and rights

Each selected field or document references a SourceRecord: provider, typed recordId, immutable revision (or content hash), sourceUrl, retrievedAt, payloadSha256, license/URL, attribution and reviewState. Retain contradictory assertions/ranks/references; selecting a new value never erases the old evidence. No catalogue reconciliation rewrites personal titles, notes, session snapshots, favorite dates or earned badges.

Wikipedia document: entity/language, pageId, returned revisionId/timestamp, response/content hashes, canonical/revision/history URLs, license/version, contributor attribution via article/history, adaptation notice and selected-language/fallback reason. Prefer revision-bearing `w/rest.php/v1/revision/{id}/with_html`; discovery `page/{title}/with_html` binds returned HTML to its own latest.id. Do not attach a separately fetched latest revision to unrelated text. Sanitize without executing HTML, scripts, styles or external resource loads; record plain-text/excerpt extraction as an adaptation. Wikipedia summaries are a small preview, not a complete encyclopedia or immutable text proof. [Endpoint evidence](SOURCE-RESEARCH.md).

Document selection: approved requested language (`cs` or `en`) first, then approved English; otherwise a sourced metadata-only empty state. Never silently translate or present Czech stub/unknown rights as verified. Metadata fields can fall back independently with language labels. Commons assets need verified file-specific author/source/revision/license or public-domain basis, restrictions and crop/transform notice; a public-domain label alone is insufficient. Unknown/NC/ND/fair-use content is blocked. No lyrics, scores, cover art or audio redistribution is licensed by a catalogue ID.

## Offline bundle and consent v1

Required: contractVersion, bundleId UUID, kind=work/artist/album/collection, entityId, revision string, language, objects[], dependencies[], metadataSnapshot and previousReadyRevision (nullable). Object fields: content hash, byte size (nullable estimate before fetch, exact after validation), MIME/representation, source revision, relative path, complete rights/attribution and transformations. Paths must be relative, canonical and traversal-free; reject decompression bombs, hash/MIME mismatches and unsupported reader/schema versions. Bundle entity snapshots and rights manifest live outside the disposable catalogue index.

Dependency graph is bounded, deduplicated by content hash + rights representation, with independent roots. Never recursively include a full artist discography by default. Preview explicitly selected works/editions/members, estimated transfer/disk growth and unknown sizes; require large-job confirmation under the measured policy in MEASUREMENTS.md. Network Content-Length is a hint, not authority. Budget compressed transfer, expanded bytes, staging and previous valid revision together.

Lifecycle: queued→waiting_for_policy→downloading→validating→installing→ready; pause/cancel/failure keeps the old ready pointer. Stage all files, verify rights/hash/length then publish an atomic revision pointer; persist progress and reconcile orphan staging after crash. GC marks ready/previous/staging/pinned dependency roots and never deletes opus.db anchors or personal data. Removing a shared bundle does not delete another bundle's objects. Catalogue DB rebuild reindexes retained manifests, not deletes them.

Automatic work uses validated unmetered Wi-Fi. A metered/mobile job requires explicit approval bound to bundle manifest hash, estimated byte ceiling and consent generation. Prefer per-job consent; an optional stored preference needs an explicit separate choice, never implied by sign-in/INTERNET. Recheck before every read/file and pause on revocation or transport change. Changed dependency scope/size invalidates earlier approval. Restore never imports OS runtime permissions or silently activates mobile allowance. Offline job state never restarts microphone or auto-submits community drafts.

created by stokonn with love <3
