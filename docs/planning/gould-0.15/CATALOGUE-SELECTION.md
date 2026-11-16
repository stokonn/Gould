# Gould 0.15 offline catalogue selection

The offline seed contains exactly 100 MusicBrainz Core composer entities. The
reviewed names, canonical Core titles, periods and verified MBIDs are pinned in
[`classical-composers.json`](../../../app/src/main/assets/catalogue/classical-composers.json).

Selection covers Baroque (20), Classical (20), Romantic (40), Impressionist (6)
and modern (14) composers. It prioritizes composers with broad, durable
classical repertoire coverage, includes the major composers requested for
Gould 0.15, and spans orchestral, chamber, vocal and keyboard traditions. The
selection contains Bach, Mozart, Beethoven, Chopin, Liszt, Debussy, Dvořák and
Smetana. The JSON list pins each person to a canonical MBID; no name-only
matching is used by the app.

Works are included only when the approved MusicBrainz Core snapshot contains a
real Composer Artist-Work relationship to a selected MBID. Aliases and
alternate titles are carried over from those same Core records. No piano work,
relationship, identity or title is synthesized.

| Snapshot | Composers | Works | Composer-credit relationships | SQLite size | Packaged gzip size |
|---|---:|---:|---:|---:|---:|
| `20261114-002230` | 100 | 105,032 | 105,421 | 283,987,968 bytes | 89,123,721 bytes |

The generated database SHA-256 is
`2b4c67b414dd985f7caa4e2303b5a54562340ed947d83670a85d32c2820b428f`; the
compressed asset SHA-256 is
`c96a16be03720d18a606efd1517b30d1daea9fdd2b7467203add7ab930b20a5f`. The
selection is generated from the preserved, checksum-verified diagnostic Core
database by `scripts/build_curated_catalogue.py`. `scripts/check_curated_catalogue.py`
checks the pinned identities, relationships, rights, manifest, SQLite integrity
and foreign keys. The 6.86 GB full database and its full-dump compressed copy
remain diagnostic artifacts and are not Android assets.

## Provenance verification addendum — 2026-11-16

The snapshot and output claims above have now been independently reproduced from
the preserved `20261114-002230` archive. The exact archive, detached-signature,
key identity, extraction, curated database, and packaged payload findings are
recorded in [`provenance-evidence/FINAL-PROVENANCE-REPORT.md`](provenance-evidence/FINAL-PROVENANCE-REPORT.md)
and its machine-readable evidence manifest. This addendum does not constitute
owner authorization for production distribution. The recorded GnuPG
owner-trust and importer COPY-escape caveats remain applicable.
