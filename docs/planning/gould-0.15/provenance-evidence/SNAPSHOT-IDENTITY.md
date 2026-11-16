# MusicBrainz snapshot identity

## Identified snapshot

- Snapshot identifier: `20261114-002230` (14 November 2026 export; the generator
  records snapshot time `2026-11-14T00:22:30Z`).
- Archive: `mbdump.tar.bz2`, the official MusicBrainz PostgreSQL **Core** dump.
- Canonical official URL:
  <https://ftp.musicbrainz.org/pub/musicbrainz/data/fullexport/20261114-002230/mbdump.tar.bz2>
- The existing generator embeds the equivalent MetaBrainz URL at
  `scripts/generate_musicbrainz_catalogue.py:21`:
  `https://data.metabrainz.org/pub/musicbrainz/data/fullexport/20261114-002230/mbdump.tar.bz2`.
- Exact local archive size: **7,738,103,395 bytes**.
- Exact local SHA-256:
  `da4f1afcbca8ca54eb6362ae45173543f079e8491d097a376d060551d1a03335`.
- The original file remains at
  `/tmp/gould-015-mb-inspection-20261114/mbdump.tar.bz2`. A byte-identical
  preservation copy is under `raw/original-transfer/mbdump.tar.bz2`.
- The official historical directory index was still available on 2026-11-16
  and listed the archive plus `SHA256SUMS`, `SHA256SUMS.asc`, and the archive's
  `.asc` signature. The index shows a rounded `7G`; the exact byte count above
  is from the local file.

## Local download evidence and limits

The old file's filesystem birth time is 2026-11-15 00:40:37 CET and its
modification time is 00:56:10 CET. The signed archive signature is dated
2026-11-14 01:47:57 UTC. The generator's `sources` row records the canonical
`data.metabrainz.org` URL and `retrievedAt=2026-11-15T00:56:00+01:00`.

The exact historical `curl`/download command and redirect/host transcript were
not retained. Therefore which official mirror served the original transfer is
not independently recoverable from local transport logs. The archive bytes
themselves are authenticated by the detached signature from the publisher's
documented key and match the signed checksum, so the missing transport log does
not leave the archive contents dependent on an unsigned mirror response.

Earlier notes about an absent or partial archive describe earlier points in the
2026-11-14/15 chronology. The full archive's later acquisition, verification,
and generation are documented in `CATALOGUE-PROVENANCE-POLICY.md` and reproduced
under `reproduction/` in this evidence package.
