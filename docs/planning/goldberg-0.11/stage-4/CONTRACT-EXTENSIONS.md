# Stage4 additive contract extensions, revision2

The Stage2 v1 files remain historical inputs. This addendum versions the following implementation extensions without changing personal schema10, catalogue schema1 or portable backup5. No network operation occurs in migration.

## Source observation2

Source identity is `(provider, typed providerKind:externalId, providerRevision|sha256=payloadHash)`; the original provider revision is retained independently in each field assertion JSON. Two different bodies claiming the same upstream revision are separate immutable observations. The hash binds observations to their own body, not an API software version. Repeated identical observations reuse the source row and do not duplicate field assertions. Public source URLs refer to the typed entity; raw active query text is not persisted in source/attribution URLs.

## Unresolved type observations2

A search hit without sufficient structural evidence is a bounded, device-local candidate containing opaque observation UUID, provider namespace/ID, title, type hint, observation time, source revision/hash and reason. A hint does not establish the catalogue kind. It cannot be added to repertoire. An explicit detail lookup can accept a typed entity under the observation UUID; cross-provider mappings still require explicit cross-ID evidence and compatible kind. Existing accepted incompatible identities remain conflicts; there is no cross-kind redirect or personal UUID rewrite.

Typed pending relationships wait until both provider identities resolve. They retain provider endpoints, relation kind, explicit position/medium, source hash/time. Invalid graphs, duplicate positions and incompatible endpoints become quarantined local diagnostics. No unknown movement or ordering is fabricated. A recording's work reference can resolve to a work or arrangement. Structural part edges are child→parent; arrangement derivation, recording performance and edition/track containment remain separate relations.

## MusicBrainz core projection2

Reviewed against the official [API](https://musicbrainz.org/doc/MusicBrainz_API), [core-data license](https://musicbrainz.org/doc/MusicBrainz_Database/Download), [Parts](https://musicbrainz.org/relationship/ca8d3642-ce5f-49f8-91f2-125d72524e6a), [Arrangement](https://musicbrainz.org/relationship/51975ed8-bbfa-486b-9f28-5947f4370299) and [recording URL types](https://musicbrainz.org/relationships/recording-url). Direct document fetches of some individual relationship pages returned429 during resumed review; no retry/proxy bypass was attempted. Cached primary documentation supplied Parts direction/ordering and recording streaming relationship UUIDs. Live compatibility remains unverified.

Additive allowlist: core relations `type`, `type-id`, `direction`, `ordering-key`, reviewed `attributes` movement label, typed endpoint id/title/name; composer/arranger/lyricist artist credits; bounded release-group→release, release→release-group and media.position/tracks.position/title/recording.id/title; recording length/isrc assertions; core aliases; primary/secondary album type. Only explicitly supported semantics are accepted; unreviewed relation fields are dropped. Nested endpoints pass their own typed projection rather than copying arbitrary objects. Tags/genres/user ratings/annotations/editors/cover-art remain blocked.

Recording external listening fields permit only the core URL resource bound to exact free-streaming/streaming type UUIDs, on a validated destination/shape. Arbitrary hosts, title-derived links, authentication and playback remain excluded. Release streaming links stay blocked until their distinct relationship-type evidence is verified.

## Personal optional-link1

Existing personal identity evidence may append `catalogue-link-v1:` JSON containing version1, optional catalogue UUID/kind, exact provider/kind/externalId keys, explicit selected-part UUIDs and original title/creator snapshots. The identity registry's personal canonical UUID and immutable anchor remain independent and unchanged. Portable5 preserves the entire evidence string; readers1–4 remain unchanged. The extra evidence is not an OS permission, provider credential, practice duration or publication license. Missing/corrupt catalogue data cannot prevent personal rendering/export/recovery.

created by stokonn with love <3
