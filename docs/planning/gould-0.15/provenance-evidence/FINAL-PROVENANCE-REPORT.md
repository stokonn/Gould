# Gould 0.15 catalogue provenance — final report

**Outcome: A — VERIFIED**, under the publisher's documented fingerprint-pinning
trust model and GnuPG 2.4.9 default policy. This is a provenance finding only;
it is **not approval to distribute** the catalogue.

## Findings

- Exact source: MusicBrainz Core snapshot `20261114-002230`,
  `mbdump.tar.bz2`, 7,738,103,395 bytes, SHA-256
  `da4f1afcbca8ca54eb6362ae45173543f079e8491d097a376d060551d1a03335`.
- The official snapshot directory still lists this exact archive and its
  checksum/signature files. The old transfer's command/redirect transcript is
  missing, so the exact mirror used by the original transfer is not established;
  the archive's content is established by its valid publisher signature and
  match with the signed SHA256SUMS entry.
- The signing certificate fingerprint was calculated locally and cross-checked
  against MusicBrainz's current official download page and MetaBrainz's
  official server installation guide. GPG verified both the checksum-list and
  archive signatures. The archive SHA-256 independently matches the signed
  manifest. These are separate checks.
- Re-running the complete source generator from the authenticated archive
  produced the same full SQLite SHA-256 as the preserved diagnostic source
  database. Re-running curation produced the same 100-composer database hash;
  deterministic gzip output was byte-for-byte identical to the current APK
  asset.
- The extractor read 12 Core table members. The separate supplementary dump
  and CC BY-NC-SA material were not used. Only Core-derived records and the
  Gould MBID selection/derived metadata are present in the curated database.
- Official MusicBrainz sources identify that Core data as CC0. Gould's
  application and documentation licenses remain separate; third-party
  materials retain their own licenses.

## Limitations retained

- GnuPG reports the publisher key's owner trust as unknown. The identity link
  is supported by the publisher's two official publication channels, not an
  external Web-of-Trust certification.
- MetaBrainz issue MBS-14311 remained unresolved and describes legacy SHA-1
  certificate/binding signatures that Sequoia may reject. GnuPG 2.4.9 accepted
  the captured key and signatures under default policy without overrides.
- The original archive download command/redirect transcript was not preserved;
  the signed content hash closes the data-origin question but not the precise
  transport route.
- The importer does not fully decode PostgreSQL COPY backslash escapes. The
  retained curated text values were audited and showed no literal escaped
  sequences; JSON serialization backslashes were separately decoded and were
  not source text. Keep this parser limitation documented.

## Contradictory records reconciled

The early provenance-policy sections accurately recorded the state before the
owner's later 2026-11-15 approval to authenticate and generate from this exact
snapshot. At that time the key was absent, the earlier partial transfer had
been removed, and no archive processing had occurred. The later
`Owner-authorized Core generation — 2026-11-15` section records successful
GPG verification, a matching archive hash and completed generation. The
selection document's database/hash/count claims are now independently
reproduced from the preserved signed archive. The 2026-11-16 release checkpoint
that cited only the earlier stop state was incomplete; it is superseded by
this evidence package. Prior dated records are retained, not rewritten.

## Separate owner decision recommended

Recommend that the owner make an explicit production-catalogue decision for
this exact 100-composer CC0-derived subset, reviewing the key interoperability
caveat and the retained parser/download-log limits. This report establishes
provenance; it does not grant production distribution authorization.

Evidence index: `SNAPSHOT-IDENTITY.md`, `SIGNING-KEY-AUTHENTICATION.md`,
`ARCHIVE-VERIFICATION.md`, `CATALOGUE-EXTRACTION-AUDIT.md`,
`LICENSING-ASSESSMENT.md`, `evidence-manifest.json`, and the adjacent `raw/`,
`official-sources/`, `logs/`, `gnupg/`, and `reproduction/` directories.
