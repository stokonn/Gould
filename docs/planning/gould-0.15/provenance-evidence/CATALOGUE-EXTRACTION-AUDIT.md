# Catalogue extraction and reproduction audit

## Reproduced chain

The pipeline was rerun into the separate `reproduction/` directory using the
verified archive copy. It did not write to or replace the production asset or
any historical database.

| Transition | Evidence/result |
|---|---|
| Official `20261114-002230` snapshot → signed Core archive | GPG signatures and signed SHA-256 entry pass; see `ARCHIVE-VERIFICATION.md`. |
| Core archive → full generated SQLite | `scripts/generate_musicbrainz_catalogue.py` rerun exited 0 in 961 seconds. Output: 6,864,670,720 bytes, SHA-256 `7dcd72951f21f3647c7a4e6e08efddba6855debe731156a6df30ea4f6dbadce5`. It byte-matches `.gould-015-diagnostics/generated-catalogue/catalogue.db`. Generator ran SQLite integrity and foreign-key checks and wrote the Room identity hash. |
| Full SQLite → curated 100-composer SQLite | `scripts/build_curated_catalogue.py` rerun exited 0 against the hash-pinned full database and `classical-composers.json`. Output: 283,987,968 bytes, SHA-256 `2b4c67b414dd985f7caa4e2303b5a54562340ed947d83670a85d32c2820b428f`. This matches the preserved curated database. Counts: 100 composers, 105,032 works, 105,421 composer-credit relationships. Integrity, foreign-key, manifest and Room identity checks pass. |
| Curated SQLite → compressed Android asset | `scripts/package_musicbrainz_catalogue.py` rerun exited 0. Output: 89,123,721 bytes, SHA-256 `c96a16be03720d18a606efd1517b30d1daea9fdd2b7467203add7ab930b20a5f`. It round-tripped to the same SQLite byte count/hash and is byte-for-byte identical to `app/src/main/assets/catalogue/musicbrainz-core-20261114/catalogue.dbz`. |
| Compressed asset → signed APK payload | The preserved signed APK checker passed: the APK contains exactly this `.dbz` catalogue entry, the payload and decoded SQLite hashes match, and neither the full diagnostic DB nor its superseded compressed full-dump payload is present. |

Machine-readable output and table counts are in `logs/reproduction-generation.log`,
`logs/reproduction-curation.log`, `logs/reproduction-packaging.log`,
`logs/catalogue-table-and-field-audit.json`,
`logs/check-reproduced-curated-catalogue.json`, and
`logs/check-existing-signed-apk-payload.json`.

## Core table and field lineage

`generate_musicbrainz_catalogue.py` first hashes the complete archive against
the pinned SHA-256. It streams `mbdump/` members, accepts only the explicit
`TABLES` allowlist, and imports only the following 12 members (row counts from
the reproduction):

| Core member | Rows | Source fields consumed |
|---|---:|---|
| `artist` | 3,041,882 | Core numeric ID, MBID (`gid`), name, sort name, birth/death year-month-day, type ID, disambiguation comment. |
| `artist_alias` | 553,891 | ID, artist ID, alias name, locale, alias type ID. |
| `artist_alias_type` | 3 | ID and type name. |
| `artist_type` | 6 | ID and type name. |
| `instrument` | 1,062 | ID, MBID, name; used only for the optional Core piano-link check, not copied to the curated database. |
| `l_artist_work` | 4,807,826 | Relationship row ID, link ID, entity0 artist ID, entity1 work ID. |
| `link` | 1,161,081 | Link ID and relationship type ID. |
| `link_type` | 697 | Type ID, GID, entity endpoint types and relationship type name. |
| `work` | 2,902,937 | Core numeric ID, MBID, title, work-type ID, disambiguation comment. |
| `work_alias` | 164,312 | ID, work ID, alias name, locale, alias type ID. |
| `work_alias_type` | 2 | ID and type name. |
| `work_type` | 29 | ID and type name. |

`l_instrument_work` was in the parser allowlist but was absent from the
archive. No direct piano/work instrument links were available; title matching
was kept as a heuristic only. The entire archive parsed successfully as a
tar/bzip2 stream.

The generator maps Core MBIDs directly to entity IDs. It maps work type names
to Gould's fixed categories; artist type names to person/group/other; sort name
to Core sort-name with name fallback; and Core birth/death fields to ISO-like
date strings with explicit precision. Work/artist names and aliases are
normalized for search with Unicode NFD diacritic removal, lowercase, and
punctuation-to-space handling. It inserts assertions for title, type,
disambiguation, sort-name, and birth/death year. Composer relationships are
selected by the Core Composer relationship GID and verified endpoint types;
relationship UUIDs are deterministic UUID5 values derived from snapshot and
Core relationship row ID. Aliases are deduplicated with `INSERT OR IGNORE`;
composer artist and work IDs use `DISTINCT`. No work IDs or titles are
synthesized.

`classical-composers.json` supplies the 100 pinned composer MBIDs (and Gould's
editorial display names/period labels). The curation builder uses the MBIDs,
then adds every Core work with an actual `credit/composer` relationship to one
of those artists. It retains only corresponding Core entities, aliases,
assertions, external identities and relationships, plus generated source,
rights, catalogue-state and search-index metadata. The period labels are Gould
curation metadata, not fields asserted to have come from MusicBrainz.

## Supplementary data exclusion

The archive used is the separately licensed Core file `mbdump.tar.bz2`; no
`mbdump-derived.tar.bz2`, live replication packet, edit, editor, statistics,
CD-stub, cover-art, event-art, or documentation dump was used. The parser
allowlist does not include annotation, tag, rating, genre-association, search,
edit-history, or derived-statistics tables. The full generated output contains
no tag/rating/annotation tables. Its app-schema tables for releases, recordings,
albums, arrangements, collections, media, documents, redirect revisions and
identity revisions are empty; the curation output preserves those empty
tables. The only source identity is MusicBrainz, and the only rights row is
CC0-1.0. That `reviewState="approved"` value is generated by the script; it is
not evidence of owner production approval or a separate legal review.

The parser's `_decoded()` function does not implement all PostgreSQL COPY
backslash unescaping. A read-only audit of the reproduced curated output found
no literal backslashes in its source-derived titles, artist names, aliases,
dates, work types, credited-as names, or decoded assertion values. The raw
backslashes in assertion JSON are JSON serialization escapes. Thus no COPY
escape corruption signature was found in the bundled curated fields. The
limitation is recorded rather than silently describing `_decoded()` as a
complete PostgreSQL COPY parser.
