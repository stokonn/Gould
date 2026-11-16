# Gould 0.15 catalogue source and provenance decision brief

**Status: evidence review and proposed policy only. Not legal advice, source
approval, permission to download, ingest, or publish, or authorization to create
licence metadata.** No upstream records were processed. A later incomplete
archive transfer is documented at the end and remains unverified and unusable.

## Repository evidence

- The only local generated-style catalogue fixture is the Goldberg 0.11
  authored fixture: 26 fictional entities. It is explicitly not real source
  data.
- `stage-2/fixtures/provider-facts.json` contains 23 MusicBrainz work search
  candidates and one Open Opus work observation. The accompanying research
  says these are not verified canonical works or cross-source matches. The
  observation log records request URLs, UTC times, response sizes and SHA-256
  values, but explicitly says raw bodies were not committed. The facts file is
  not a complete source snapshot and cannot recreate those bodies.
- Current local credits identify Open Opus catalogue data as public domain,
  Wikidata structured data as CC0, Wikipedia text as generally CC BY-SA and
  Commons images as per-file. These existing notices document the app's
  existing online/source attribution; they do not approve a static catalogue
  import.
- Local commit `b0d7c4b` adds only the Goldberg 0.11 OWNER REVIEW DRAFT. It
  says explicitly it is not approved, not ingest authorization, and not legal
  clearance. No historical PR or draft is treated as authorization.

## Candidate source assessment

| Candidate | Verified upstream facts | Pinning and attribution | Assessment for a first offline package |
|---|---|---|---|
| **MusicBrainz core database dump** | Official docs identify `mbdump.tar.bz2` as CC0. Dumps are separated by content/license; supplementary dumps and live replication packets include CC BY-NC-SA 3.0. The database page says full snapshots are generated twice weekly. Download docs describe SHA256SUMS and detached GPG signatures. | Pin snapshot date, exact official filename, SHA-256SUMS and signature verification; archive the exact dump and extraction tool/schema revision. CC0 has no mandatory attribution; retain a courtesy MusicBrainz credit and per-record MBID/source URL. | **Best documented candidate for owner decision.** Scope to `mbdump.tar.bz2` core entities only. Exclude supplementary dumps, live feed, edits, tags, ratings, annotations, derived statistics, editor data, cover-art references and untrusted CD stubs. A full snapshot is sizeable and must be supplied/acquired only after approval. A field/table allowlist still needs review against the official schema. |
| **Open Opus full JSON dump** | The project states its data is public domain and its API code is GPLv3; it documents a full JSON dump at `/work/dump.json`. Its page notes the catalogue started from lists scraped from sources including Wikipedia and IMSLP and is being standardized. The documented dump response has an API version field (example `1.19.10`). | Pin the exact response bytes by SHA-256, record the API-reported version and retrieval time, and retain per-record numeric IDs and URLs where available. The public-domain statement carries no attribution requirement; keep an Open Opus credit as source courtesy. | **Plausible simpler alternative, but lineage and identifier work need review.** The documented dump example does not show per-record IDs; existing app adapters get IDs from API endpoints. The source project's broad data dedication is evidence of its stated terms, but it does not itself verify every historical input's origin. Do not combine its GPL API code licence with the data licence. |
| **Wikidata structured JSON dump / selected entities** | Wikidata states structured entity data is CC0; other-namespace text is CC BY-SA 4.0. Its database-download docs describe weekly JSON dumps and warn that complete dumps are very large. The licensing page also cautions that the community cannot settle all underlying dataset-ownership disputes. | Pin dump date/file/hash, or for a deliberately bounded API extraction pin exact response bytes, QID, `lastrevid`, statement rank, qualifiers and references. CC0 requires no credit; retain QID/source links and Wikidata courtesy credit. | **Legally plausible, operationally heavy as a complete snapshot.** A selected set needs a defined query/extraction and entity-specific review; do not treat a query response as a complete dataset. No existing QID matches were verified in the local source research. |
| **Wikipedia text** | Existing local provider research observed page-specific REST responses under CC BY-SA 4.0. Wikimedia terms also describe GFDL availability and edition/import exceptions. Reuse requires author/page-history attribution, license information, and indication of changes; adaptations are shared under CC BY-SA terms. | Each document needs page URL, immutable revision/history URL, response/body hash, language, exact returned license, attribution basis, and transformation notice. | Exclude from the first catalogue database. It is prose, not catalogue facts; the existing offline package has one file-level license field and must not incorrectly label mixed CC0 and share-alike content as a single-license CC0 file. |
| **Wikimedia Commons media** | Each file has its own license and source-page requirements; a Wikidata image claim or Commons presence does not supply a blanket media license. | Must pin each file revision/content hash and retain creator, title, source page, license/version, required credit and modification details or reviewed public-domain basis. | Exclude media bytes from the catalogue package. No media is needed for a source-data snapshot. |

These are upstream-published licensing claims and documented terms, not
independent legal opinions or proof that any particular record is correctly
classified. Public availability, an API response, or an upstream CC0 label
does not prove that two same-title works are identical or that an image,
recording, score, prose description or linked content has the same rights.

## Proposed policy compatible with `catalogue_generator.py`

Use **one CC0 dataset per generated package** until the owner approves a more
complex licensing decision. Do not mix Wikipedia text, Commons assets, or
MusicBrainz supplementary data into a CC0 database file. This keeps the single
existing Bundle v1 `BundleFile.rights` value accurate; no replacement format is
proposed.

For a proposed MusicBrainz-core-only package, the external review policy and
generator input should carry these values:

- Source `formatVersion: 1`; stable `datasetId`; monotonically increasing
  dataset `revision` tied to the exact dump date; each entity uses a persisted
  canonical UUID. Preserve existing catalogue IDs where records already
  exist; never derive IDs from mutable names/titles.
- Each source record's `source` object: `provider: "musicbrainz"`, exact
  `providerKind`, MBID as `externalId`, and `payloadSha256` computed from the
  reviewed canonical source-record bytes. Keep a deterministic extraction
  recipe and source schema revision alongside the input snapshot.
- Each matching external policy `records[]` row: identical provider/type/MBID
  and payload hash; `sourceName: "MusicBrainz"`; the canonical MBID URL;
  `revision` containing dump date plus entity revision if present;
  `retrievedAt` in UTC; `license: "CC0-1.0"`; canonical CC0 URL; a courtesy
  attribution such as `MusicBrainz — work <MBID> (CC0 1.0)`; and
  `reviewState: "approved"` only after a reviewer validates that the fields
  came exclusively from the core snapshot.
- The external policy's `inputSha256` pins the exact canonical generator input
  bytes. `approvedLicenses` contains only `CC0-1.0`. `packageFileRights`
  identifies the single generated `catalogue.db` artifact as derived solely
  from that reviewed CC0 core snapshot, names MusicBrainz, links the official
  dataset/download and CC0 pages, and retains a concise courtesy attribution.
  The generated manifest then pins the database artifact SHA-256 and byte
  length in the existing Bundle format v1.
- Reject missing IDs, ambiguous identity mappings, fields outside the
  explicitly reviewed table/field allowlist, any supplementary data, changed
  source bytes, missing hashes, unknown licenses or incomplete attribution.
  Review each MBID as a source record; a search candidate is not automatically
  a verified musical identity or relationship.

The generator's source hash plus an externally reviewed per-record payload
hash permits deterministic verification once the exact source snapshot and
canonical extraction are held. A hash proves byte identity, not redistribution
rights or source accuracy. No actual dump SHA-256 can be recorded until an
approved snapshot is supplied or its acquisition is explicitly authorized.

## Decision and missing evidence

The owner selected MusicBrainz Core-only and authorized inspection of snapshot
`20261114-002230`, restricted to composer, work, and relationship data. That
authorization does not waive signature verification or approve production
catalogue generation/import. Acquisition stopped before the archive because
the signing key could not be independently authenticated. Still missing for
production use:

1. Independent authentication of the full MusicBrainz signing-key fingerprint
   and successful checksum-list/archive signature verification under normal
   GPG policy. The prepared MetaBrainz inquiry remains unsent.
2. After verified acquisition, a recorded archive SHA-256, extraction recipe,
   per-record hashes/provenance review, and source-field review. No real records
   were inspected in this work.
3. Separate explicit owner approval for production catalogue generation/import
   after reviewing the pinned real-source evidence. Legal guidance remains
   necessary if the owner's review finds the upstream dedication insufficient.

The owner-provided inspection scope and implementation infrastructure are
complete; these are prerequisites to any future production data, not blockers
to Stage 2 code completion.

The source-to-field policy above is a proposed review procedure, not an
approved policy artifact for production; no license metadata or production
input has been created.

## Upstream references

- Open Opus data/API rights and dump: [project site](https://www.openopus.org/),
  [official API repository](https://github.com/openopus-org/openopus_api),
  [full data dump documentation](https://github.com/openopus-org/openopus_api/blob/master/ALLDATA.md).
- MusicBrainz: [data license](https://musicbrainz.org/doc/About/Data_License),
  [database field groups](https://musicbrainz.org/doc/MusicBrainz_Database),
  [dump contents, checksums and file licenses](https://musicbrainz.org/doc/MusicBrainz_Database/Download).
- Wikidata: [structured data licensing](https://www.wikidata.org/wiki/Wikidata:Licensing),
  [database download formats and snapshot notes](https://www.wikidata.org/wiki/Wikidata:Database_download).
- Wikimedia text: [Terms of Use](https://foundation.wikimedia.org/wiki/Policy:Terms_of_Use)
  and [app reuse guidance](https://foundation.wikimedia.org/wiki/Legal:Wikimedia_Developer_App_Guidelines).
- Commons: [file-specific reuse guidance](https://commons.wikimedia.org/wiki/Commons:Reusing_content_outside_Wikimedia/licenses).
- CC0 legal deed: [CC0 1.0 Universal](https://creativecommons.org/publicdomain/zero/1.0/).

## Authorized snapshot inspection attempt — 2026-11-14

The owner authorized acquisition of one exact MusicBrainz Core snapshot for
inspection only, without production generation/import or personal data use.
The selected snapshot is `20261114-002230`, from the official directory
`https://ftp.musicbrainz.org/pub/musicbrainz/data/fullexport/20261114-002230/`.
The index timestamp is 2026-11-14 05:10 UTC; the Core archive listing showed
2026-11-14 01:47. The official `SHA256SUMS` entry for `mbdump.tar.bz2` is
`da4f1afcbca8ca54eb6362ae45173543f079e8491d097a376d060551d1a03335`.
These are published metadata, not local archive verification.

The expected verification sequence is to authenticate the official MusicBrainz
signing key at fingerprint `D5E63B4BDCCE195642948684B8FC2375C777580F`, verify
`SHA256SUMS.asc` against `SHA256SUMS`, verify `mbdump.tar.bz2.asc` against the
Core archive, then independently compare the archive SHA-256 to its one
`SHA256SUMS` entry. Only after all checks pass may the archive be processed.
The official signature files and checksum list were saved in
`/tmp/gould-015-mb-inspection-20261114/`; local SHA-256 values are:

- `SHA256SUMS`: `e09d4470c42548ef73f8d1cf3762af9f1bc9b03a74a4370a1be531887a953f09`
- `SHA256SUMS.asc`: `7ff8b1de19df6787ff1861464cb9157564c8fe883456f43af552fdad7cb8f001`
- `mbdump.tar.bz2.asc`: `ec0016687eb4657d15c6f9ae41a9f666445cd2469bc4a0c71db532fecacd41c1`

GPG could not verify `SHA256SUMS.asc`: the isolated keyring had no public key.
Retrieval from the attempted keyserver failed; another keyserver endpoint could
not resolve. The signature's key ID display is not a substitute for key
authentication. Thus neither signature verification nor archive checksum
verification was completed. To obey the authorization boundary, the 7 GB Core
archive was not downloaded, and no dump was extracted or processed. No derived,
CD stub, or supplementary archive was downloaded. Consequently there are no
actual-record statistics or examples; all such counts are unavailable.

**Exact next decision/evidence required:** the owner must supply an
independently authenticated public key matching the published fingerprint, or
authorize a working network path to an independently verifiable official key
distribution. Resume with the same pinned snapshot only after key
authentication and both signature/hash checks pass. For a future inspection
extraction, use only the Core archive's required artist/work/link tables,
retain MBIDs, exclude release tables and supplementary files, and record the
extraction recipe and output hashes. Since the inspected format is PostgreSQL
Core dump data (not JSON), the JSON-only fields `annotation`, `genres`,
`ratings`, and `tags` were not processed; any future JSON conversion must
explicitly exclude and test those fields. Production catalogue generation or
import still requires separate explicit owner approval.

## Signing-key investigation — 2026-11-14

### Verified source facts

- The official `metabrainz/musicbrainz-server` `INSTALL.md`, read on
  2026-11-14 at
  <https://github.com/metabrainz/musicbrainz-server/blob/master/INSTALL.md>,
  still instructs dump consumers to retrieve key ID `C777580F` and verify
  signatures. It does not specify a full fingerprint or announce a replacement
  key in that procedure. This is official, not independent fingerprint proof.
- Official MusicBrainz download documentation at
  <https://musicbrainz.org/doc/MusicBrainz_Database/Download> states the full
  fingerprint `D5E63B4BDCCE195642948684B8FC2375C777580F` and shows historical
  verification; it is the source being cross-checked, not independent proof.
- MBS-14311 exists at
  <https://tickets.metabrainz.org/browse/MBS-14311>. Its page showed **In
  Progress**, **Unresolved**, no fix version, and updated 2026-11-08 12:05 UTC
  when inspected on 2026-11-14. The reporter-submitted description, dated
  2026-03-28, gives that same full fingerprint, RSA 2048, subkey fingerprint
  `3CA330EE23B47A79AB76FA3619AF6A3243513089`, and an `sq inspect` output
  rejecting SHA-1 positive-certification and subkey-binding packets under
  Sequoia policy. This is reporter evidence, not a maintainer resolution or a
  test of current GnuPG behavior; activity/comments were not available in the
  inspected rendering.
- Local GnuPG is 2.4.9 with libgcrypt 1.12.2. `gpg --list-packets` parsed the
  previously downloaded `SHA256SUMS.asc` and `mbdump.tar.bz2.asc` as OpenPGP v4
  RSA signatures with digest algorithm 10 (SHA-512), and each embeds issuer
  fingerprint `D5E63B4BDCCE195642948684B8FC2375C777580F`. Parsing does not verify
  a signature or authenticate a key.
- No replacement key, newer signature format, official announcement, or
  maintainer statement was found in the inspected INSTALL instructions,
  download documentation, or ticket view. This bounded search does not prove
  none exists elsewhere.
- The GitHub file-history endpoint could not be fetched (web access returned a
  cache miss); no claim is made about historical commits beyond the currently
  visible file. Searches of MetaBrainz GitHub and community announcement
  results did not return a maintainer key-rotation confirmation.

### Unresolved authentication and verification

The exact fingerprint is present in official MusicBrainz documentation and in
MBS-14311, and matches the signature issuer field. The independent cross-check
is still insufficient: the official GitHub instructions expose only the short
key ID, and the full fingerprint plus Sequoia analysis in the ticket is an
external reporter's unreviewed submission. No independent maintained record
or maintainer confirmation was verified. Retrieval from Ubuntu's keyserver
failed; the restricted shell could not resolve it, and the previously tried
keys.openpgp.org fingerprint endpoint returned 404. The isolated GPG keyring
remains empty and verification reports `Can't check signature: No public key`.

Although the detached signatures use SHA-512, this does not resolve whether
current GnuPG accepts the signing certificate under normal policy. The ticket
reporter says Sequoia rejects SHA-1 certificate/binding signatures; GnuPG could
not be tested with the key. No weak-algorithm override or global policy change
was used. Do not treat packet parsing or keyserver retrieval as authentication.

**Recommended next step:** obtain the public certificate from a
MetaBrainz-maintainer-confirmed channel and confirm its full fingerprint via a
separate trustworthy channel, such as a maintainer announcement or signed
repository artifact/history. Then test the already-held signature metadata
using the existing GnuPG version and isolated keyring without overrides. If
normal policy rejects it, request a replacement key or modern signature
procedure from MetaBrainz; do not bypass the rejection. The snapshot archive
remains undownloaded and no catalogue data was processed.

## Owner-authorized Core generation — 2026-11-15 (supersedes the prior stop state)

The owner accepted the fingerprint published in official MusicBrainz download
documentation as the trust anchor and authorized inspection and generation of
snapshot `20261114-002230`. This is not represented as independent institutional
authentication. The isolated GnuPG key's primary fingerprint was
`D5E63B4BDCCE195642948684B8FC2375C777580F`, exactly matching the owner-approved
fingerprint. Under normal GPG policy, both detached signatures verified as
cryptographically good. GPG's separate owner-trust warning remains:

```text
gpg: Signature made Sat Nov 14 06:08:46 2026 CET
gpg:                using RSA key D5E63B4BDCCE195642948684B8FC2375C777580F
[GNUPG:] NEWSIG
[GNUPG:] KEY_CONSIDERED D5E63B4BDCCE195642948684B8FC2375C777580F 0
gpg: Good signature from "MusicBrainz (MusicBrainz data dump signing key) <support@musicbrainz.org>" [unknown]
gpg: WARNING: This key is not certified with a trusted signature!
gpg:          There is no indication that the signature belongs to the owner.
Primary key fingerprint: D5E6 3B4B DCCE 1956 4294 8684 B8FC 2375 C777 580F
[GNUPG:] SIG_ID cNM1SJKc9TS47No5diprJFr+11I 2026-11-14 1794632926
[GNUPG:] GOODSIG B8FC2375C777580F MusicBrainz (MusicBrainz data dump signing key) <support@musicbrainz.org>
[GNUPG:] VALIDSIG D5E63B4BDCCE195642948684B8FC2375C777580F 2026-11-14 1794632926 0 4 0 1 10 00 D5E63B4BDCCE195642948684B8FC2375C777580F
[GNUPG:] TRUST_UNDEFINED 0 pgp
```

This is `SHA256SUMS.asc` over `SHA256SUMS`. The archive detached signature
output was:

```text
gpg: Signature made Sat Nov 14 02:47:57 2026 CET
gpg:                using RSA key D5E63B4BDCCE195642948684B8FC2375C777580F
[GNUPG:] NEWSIG
[GNUPG:] KEY_CONSIDERED D5E63B4BDCCE195642948684B8FC2375C777580F 0
gpg: Good signature from "MusicBrainz (MusicBrainz data dump signing key) <support@musicbrainz.org>" [unknown]
gpg: WARNING: This key is not certified with a trusted signature!
gpg:          There is no indication that the signature belongs to the owner.
Primary key fingerprint: D5E6 3B4B DCCE 1956 4294 8684 B8FC 2375 C777 580F
[GNUPG:] SIG_ID mzG8KHSZZt0yf8vBlFHqi/WzcIU 2026-11-14 1794620877
[GNUPG:] GOODSIG B8FC2375C777580F MusicBrainz (MusicBrainz data dump signing key) <support@musicbrainz.org>
[GNUPG:] VALIDSIG D5E63B4BDCCE195642948684B8FC2375C777580F 2026-11-14 1794620877 0 4 0 1 10 00 D5E63B4BDCCE195642948684B8FC2375C777580F
[GNUPG:] TRUST_UNDEFINED 0 pgp
```

The archive SHA-256 was
recomputed as
`da4f1afcbca8ca54eb6362ae45173543f079e8491d097a376d060551d1a03335`, matching
the signed checksum list. Only `mbdump.tar.bz2` was acquired; no supplementary
or derived dump was used.

The generated, bundled SQLite catalogue is
`app/src/main/assets/catalogue/musicbrainz-core-20261114/catalogue.db` with
SHA-256 `7dcd72951f21f3647c7a4e6e08efddba6855debe731156a6df30ea4f6dbadce5`
and size 6,864,670,720 bytes. It contains every Core work, the composers
appearing in MusicBrainz `Composer` Artist-Work links, all those artists' Core
aliases, all Core work aliases, and those composer relationships. It preserves
MusicBrainz UUIDs. Selected metadata is limited to title, work/artist type,
disambiguation, sort name, and composer birth/death; it contains no recordings,
releases, annotations, tags, ratings, genres, supplementary data, or derived
datasets. All embedded source assertions and the package are CC0 1.0.

The generated-data check independently verified: 2,902,937 works; 202,867
composer artists; 2,488,215 composer relationship rows; 2,059,464 works linked
to composers; and 23,980 work titles containing “piano”. MusicBrainz Core in
this snapshot has no `l_instrument_work` table, so no piano-instrument links
were available and no piano work is described as a confirmed instrument match.
The 23,980 count is only a title heuristic. Searchable FTS has 27,556 postings
for `piano`, including alternate-title matches. Among primary work titles,
normalization found 206,566 duplicate-title groups with 819,070 surplus rows
(1,025,636 records in those groups); all records were retained because title
collisions are not proof of duplicate identity. Work alternate titles in
non-`und` locales number 52,202. Actual composer and alternate-title FTS lookup,
MusicBrainz-only source/CC0 checks, Room identity, foreign keys, SQLite
`integrity_check`, package size, and SHA-256 are exercised by
`scripts/check_musicbrainz_catalogue.py`.

Representative real piano-title candidates (MBID — title — linked composer):

- `0001fc3f-3efb-4f9c-8a65-1bbb267ba886` — *Concierto en do para piano y
  orquesta: II. Sostenuto* — Carlos Jiménez Mabarak.
- `00054088-3264-47ae-99a3-6538df7e732e` — *Sonata for Piano 4-hands in C
  Minor, op. 122: II. Adagio* — Josef Gabriel Rheinberger.
- `0005b6d0-d47c-478a-bded-f945a24a1da4` — *Piano Sonata no. 1 in C major,
  op. 8, M8: IV. Finale. Introduzione* — Karol Szymanowski.
- `0005f1d4-64f1-4981-9273-c6f2444f076e` — *Concerto in E-flat major for Piano
  and Orchestra: III. Allegro* — František Xaver Dušek.
- `000933ff-257f-4b02-85b6-df86c3538603` — *8 Variations for Piano in C major
  on the Romance “Une fièvre brulante” from Grétry's opera “Richard Cœur de
  Lion”: Variation 7* — Ludwig van Beethoven.

The exact-generation verification reported SQLite `integrity_check = ok`, an
empty `foreign_key_check`, Room schema identity `437013a61680fd9e34065937d3ef14a8`,
and manifest/database byte and SHA-256 agreement. The runtime package path
installs the bundled data through `LocalCatalogueStore`'s candidate-generation
validation and atomic pointer switch. It does not touch personal work records;
personal identities are copied through the existing identity bridge. The
canonical database is bundled for offline use; no MusicBrainz production
requests or production import were enabled.

Two interrupted diagnostic outputs are retained under the ignored build
directory `app/build/catalogue-generation-failed/`; neither is packaged or
installed. The generator now uses rollback journaling, committed 20,000-row
Core batches with snapshot/parser checkpoints, safe re-read skipping, indexed
raw IDs/link fields, and progress output. Its complete Core checkpoint can be
resumed without reopening or parsing the archive, as covered by a Python test.

## Proposed MetaBrainz inquiry — draft only, not sent

**Recommended destination:** `support@metabrainz.org`, the direct staff contact
published on the [official MetaBrainz contact page](https://metabrainz.org/contact)
(checked 2026-11-14). The [MusicBrainz help page](https://musicbrainz.org/doc/Help)
also points users to support/community channels. Ask the support team to route
this technical signing question to the MusicBrainz database/dump maintainer if
needed. Do not send without explicit owner approval.

**Subject:** MusicBrainz dump signing key and verification for snapshot 20261114-002230

> Hello MetaBrainz team,
>
> We are preparing an offline catalogue-data review using only the MusicBrainz
> Core dump from snapshot `20261114-002230`. The expected SHA-256 for
> `mbdump.tar.bz2`, as listed in that snapshot's `SHA256SUMS`, is
> `da4f1afcbca8ca54eb6362ae45173543f079e8491d097a376d060551d1a03335`.
> We have not downloaded the archive because we cannot yet independently
> authenticate the dump-signing key.
>
> Could you please clarify:
>
> 1. What is the currently valid full fingerprint for MusicBrainz dump
>    signatures, and is the existing key with short ID `C777580F` still
>    supported for this snapshot?
> 2. Does the current signing certificate still rely on SHA-1
>    self-certification/subkey-binding signatures, and what is the recommended
>    verification approach under current GnuPG/Sequoia cryptographic policy?
>    We refer to the concern and Sequoia output recorded in MBS-14311:
>    <https://tickets.metabrainz.org/browse/MBS-14311>.
> 3. What trusted distribution channel should consumers use to obtain the
>    public signing key, and where can they independently confirm its complete
>    fingerprint? If the key is being rotated, please provide the replacement
>    key fingerprint and transition/verification instructions.
> 4. Is a newer signature format or signing procedure available for snapshot
>    `20261114-002230` and its Core archive, or is the published detached
>    OpenPGP signature the only supported option?
>
> We will not bypass cryptographic policy checks. We need the authenticated
> fingerprint and supported verification procedure before proceeding. Thank
> you.

This is an owner-review draft only. It has not been sent, and no support ticket,
forum post, or maintainer contact was created.

## Independent fingerprint search — 2026-11-15

The requested search targeted the exact full fingerprint
`D5E63B4BDCCE195642948684B8FC2375C777580F`, the spaced fingerprint, and key ID
`C777580F`, excluding MusicBrainz/MetaBrainz documentation where practical.
Searches covered Debian package/keyring references and independent key
references. No trustworthy institutional or independently maintained
confirmation of the full fingerprint was found.

Sources surfaced but **do not count as independent confirmation**:

- `musicbrainz.org` publishes the full fingerprint, but is the project being
  authenticated: <https://musicbrainz.org/doc/MusicBrainz_Database/Download>.
- `musicbrainz-server/INSTALL.md` is hosted in MetaBrainz's own GitHub
  organization and gives only the short ID `C777580F`:
  <https://github.com/metabrainz/musicbrainz-server/blob/master/INSTALL.md>.
- The `musicbrainz.readthedocs.io` download manual reproduces the MusicBrainz
  instructions and is not independent evidence:
  <https://musicbrainz.readthedocs.io/en/latest/musicbrainz_database/download.html>.
- Ticket MBS-14311 is hosted by MetaBrainz and its fingerprint/algorithm report
  is reporter-submitted, not an independent institutional confirmation:
  <https://tickets.metabrainz.org/browse/MBS-14311>.
- Ubuntu/OpenPGP keyservers are key-distribution services, not independent
  identity attestations. A retrieved key or matching fingerprint there alone
  would not satisfy the independent-confirmation requirement. No key was
  retrieved or imported during this search.

**Conclusion:** the full fingerprint remains unauthenticated independently.
Per the stop condition, no key retrieval/import and no signature verification
were attempted in this continuation. The previously recorded isolated-keyring
verification failure (`Can't check signature: No public key`) remains the
latest actual GPG result at the time of that search. A later continuation is
recorded below. No SHA-1 override or other GPG policy change was used. Resume
only if a trustworthy independent source for the full fingerprint becomes
available.

## Current trust-gate continuation — 2026-11-15

The key was subsequently fetched from an Ubuntu keyserver into an isolated
temporary keyring. GPG displayed the expected full fingerprint
`D5E63B4BDCCE195642948684B8FC2375C777580F` and verified the detached signature
on `SHA256SUMS` under normal cryptographic policy, reporting a good signature
but an uncertified/untrusted key. This proves signature consistency with that
key; it does **not** independently authenticate MetaBrainz as the key owner.
No weak-algorithm override was used. The full archive signature and its
published SHA-256 were not verified.

A transfer of the exact pinned `mbdump.tar.bz2` was mistakenly started before
the independent identity gate was satisfied. It was stopped at approximately
2.9 GiB of the 7.20 GiB archive. The partial file remains in
`/tmp/gould-015-mb-inspection-20261114/mbdump.tar.bz2`; it was not extracted,
parsed, or used. It is not a verified snapshot. No MusicBrainz records, counts,
duplicates, or piano-repertoire relevance results are claimed. Do not resume
or restart acquisition until the trust gate is resolved.

**Exact outstanding owner action:** obtain independent full-fingerprint
confirmation for `D5E63B4BDCCE195642948684B8FC2375C777580F` from MetaBrainz
through an official support/security channel. If the complete archive
signature fails under normal GPG policy after that confirmation, stop and
request a supported replacement key/signing procedure. Production catalogue
generation/import remains separately approval-gated.

## Owner-approved fingerprint and isolated verification attempt — 2026-11-15

The owner explicitly approved the full fingerprint published in official
MusicBrainz documentation as the trust anchor:
`D5E63B4BDCCE195642948684B8FC2375C777580F`. This is owner acceptance of the
publisher's stated fingerprint; it is **not** independent institutional
authentication.

A fresh GPG home was created at
`/tmp/gould015-keycheck.Z1b4N0` (GnuPG 2.4.9, libgcrypt 1.12.2). Retrieving
documented key ID `C777580F` with the normal GPG HKPS keyserver command failed
before contacting the keyserver: `/usr/bin/dirmngr` is unavailable (`failed to
start dirmngr`; `keyserver receive failed: No dirmngr`). Direct HTTPS retrieval
from `keyserver.ubuntu.com` also failed because the restricted shell could not
resolve that host (`curl: (6) Could not resolve host`). The read-only web fetch
of the same keyserver endpoint was inaccessible. No key was obtained or
imported into the fresh keyring, so no complete imported fingerprint is
available to compare. No key from an existing or unrelated keyring/file was
substituted.

**Result at that checkpoint:** fingerprint comparison was not completed, and no
detached signature was verified in that attempt. This was a
retrieval/environment failure, not a GPG cryptographic rejection. Later
verification/transfer events are recorded below.

## Latest acquisition and verification state — 2026-11-15

During the continuation, the key was fetched from an Ubuntu keyserver into an
isolated temporary keyring. GPG showed full fingerprint
`D5E63B4BDCCE195642948684B8FC2375C777580F` and verified `SHA256SUMS.asc`
against `SHA256SUMS` under normal policy as a good signature, with the expected
uncertified/untrusted-key warning. This validates the signature against the
retrieved key; it does not independently authenticate MetaBrainz identity.
The full archive signature and published archive checksum were not verified.

An archive transfer was stopped at 3,120,558,080 bytes of the 7.20 GiB archive
before the required independent fingerprint confirmation. The incomplete file
was removed from `/tmp/gould-015-mb-inspection-20261114/mbdump.tar.bz2` without
inspection or processing. No verified archive or real-record counts/relevance
analysis are available.

**Outstanding owner action:** obtain independent full-fingerprint confirmation
for `D5E63B4BDCCE195642948684B8FC2375C777580F` from MetaBrainz through an
official support/security channel. If normal-policy verification of the full
archive then fails, stop and request a supported replacement key/signing
procedure. Separate owner approval is still required before production import.

## Evidence review update — 2026-11-16

This update preserves the dated decision history above. The early statements
that the key/archive were unavailable and generation had not occurred were
accurate for those earlier checkpoints. They were superseded by the later
owner-authorized 2026-11-15 acquisition and generation entry, whose verification
was subsequently re-run and collected in
[`provenance-evidence/`](provenance-evidence/FINAL-PROVENANCE-REPORT.md).

The exact Core archive now has a verified detached GPG signature under the key
whose full fingerprint was independently checked against two current official
MusicBrainz/MetaBrainz publication channels, and its SHA-256 matches the entry
in the signed `SHA256SUMS`. The complete generator, curation, and packaging
pipeline was rerun; the full database, curated database, and compressed asset
match the preserved outputs byte-for-byte. See the evidence reports and raw
logs for separate results and limitations. GnuPG owner trust remains unknown;
MetaBrainz issue MBS-14311 retains a verifier-interoperability caveat; the
original download redirect transcript is missing; and the COPY parser limit is
documented. These do not change the successful recorded GnuPG checks, but must
remain visible in any provenance claim.

Accordingly, the earlier unresolved archive/signature/reproducibility claims
are historical, not current. Record-level provenance was established for the
generated field pipeline described in the extraction audit. The generated
`reviewState="approved"` field is not an owner production approval. This
investigation does not itself authorize distribution of the production
catalogue.
