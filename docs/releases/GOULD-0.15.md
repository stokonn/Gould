# Gould 0.15.0 — Pre-release (published)

**Owner release authorization (2026-11-16):** the owner explicitly
authorized completing and publishing Gould 0.15 as a GitHub prerelease
**despite the unresolved Google SDK licensing questions** documented in
[the dependency licensing audit](../planning/gould-0.15/GOOGLE-DEPENDENCY-LICENSING-AUDIT.md)
(verdict: *LEGAL REVIEW REQUIRED*). That authorization is recorded
transparently; it does **not** resolve the licensing questions, does not
grant any permission that Google has not granted, and does not change the
licensing decisions: application source remains GPL-3.0-or-later and
project-owned documentation remains CC-BY-SA-4.0. The audit and all supporting
evidence are preserved unchanged in the repository.

The published artifact is the exact signed APK tested by the owner on a
Samsung Galaxy A55 (`.gould-015-cancellation-build/.../app-release.apk`,
SHA-256 `f3df006e…ade8ea` below). The separately rebuilt candidate
(SHA-256 `9dfce12a…318a054`) has identical ZIP entries and uncompressed
contents but was not published, because it was not the owner-tested
artifact. The Google Sign-In implementation is unchanged in 0.15; its
replacement investigation is deferred to Gould 0.16.

Gould 0.15.0 is a **GitHub pre-release**, not Stable. It keeps the existing
Android application ID, Room database identity, signing certificate, and
upgrade path. Android 11 (API 30) or newer is required. The package metadata
remains `com.opus.piano`, version name `0.15.0-dev` (the owner-tested
build's metadata), version code `17`.
Export a backup before updating and install over the existing compatible app;
do not uninstall or clear its data to work around a signing/version warning.

## What changed

- **Offline catalogue:** Gould includes a curated set of 100 composers,
  105,032 works, and 105,421 composer relationships from the verified
  MusicBrainz Core snapshot `20261114-002230`. MusicBrainz Core data is CC0.
  [Provenance, extraction, and license boundary](../planning/gould-0.15/provenance-evidence/FINAL-PROVENANCE-REPORT.md).
- **Online catalogue search:** when the user enables online catalogue search,
  the active public search provider is MusicBrainz Core. Local results appear
  offline; online results are added through the existing rate-limited,
  cached MusicBrainz client. Provider availability and coverage depend on the
  public MusicBrainz service.
- **Private repertoire stays separate:** a user's Repertoire contains only
  user-added pieces. Browsing public Catalogue records, adding a repertoire
  entry, or removing that entry does not delete the public catalogue record.
- **Library:** removes the redundant subtitle, gives result lists more room,
  and uses full-width equal Repertoire/Catalogue tabs with a larger touch area.
- **Artist pages:** search is scoped to the selected artist's works; title
  sorting is deterministic in both directions; available work-type/period
  metadata can filter results. Search, filters, reset, and empty states work
  with offline catalogue records.
- **Search cancellation:** changing between All, Works, and Artists keeps the
  query and starts a search for the newly selected category. Obsolete requests
  cannot replace newer results or clear their loading state.
- **Activity and Streak:** a successful week consistently requires both the
  configured weekly session goal and weekly practice-minutes goal. Activity
  summaries and widgets follow the same rule.
- **Guidance and recovery:** adds the guided onboarding flow and safer export/
  recovery for unfinished community drafts. These drafts remain separate from
  private practice repertoire and catalogue records.

## Data and licensing

Application/project-owned program source is GPL-3.0-or-later. Project-owned
documentation and original prose are CC-BY-SA-4.0. The curated MusicBrainz
Core data is CC0. Fonts, dependencies, artwork, quotations, and other third-
party content retain their own terms and notices; see [LICENSES.md](../LICENSES.md).
The source and corresponding license texts are available from the tagged
repository.

## Verification and known limitations

The release APK is the exact signed cancellation-fix candidate tested by the
owner on a Samsung Galaxy A55. Host JVM tests, Android-test APK compilation,
lint, release assembly, and catalogue checks are recorded in the final
verification report. Android instrumentation was compiled but **not executed**;
this is not reported as an instrumentation pass. The agent did not access the
owner's device.

The catalogue covers the curated identities and relationships present in the
pinned snapshot, not every work or arrangement. Online search requires the
user's opt-in and depends on MusicBrainz service availability and rate limits.
The verified provenance report records accepted qualifications: GnuPG owner
trust is unknown; some verification implementations may reject legacy
certificate/binding signatures; the importer does not fully implement
PostgreSQL COPY backslash unescaping (no affected sequence was found in the
curated fields); and the original transfer redirect transcript is unavailable.

APK SHA-256: `f3df006e36aa55fb940b5a938cc36737156c3ff77a5c960a9c5da10b53ade8ea`.

This version is a **pre-release**, not Stable.
