# Changelog & Roadmap — Gould

> **Latest release:** [Gould 0.15.0 (pre-release)](https://github.com/stokonn/Gould/releases/tag/v0.15.0). This is not Stable.

## Gould 0.15.0 — published pre-release

- Curated offline catalogue from the verified MusicBrainz Core snapshot: 100 composers, 105,032 works and 105,421 composer relationships.
- Opt-in online MusicBrainz search with existing debounce, cancellation, rate-limit and cache behaviour.
- Redesigned Library and Artist pages, scoped search, deterministic sorting and work-type/period filters.
- Guided onboarding and safer community-draft recovery.
- Consistent Activity week completion based on both configured weekly goals.

The published APK is the exact artifact the owner tested on a Samsung Galaxy A55. Android instrumentation was compiled but **not executed**. The unresolved Google SDK licensing questions and the owner's explicit release authorization are documented transparently.

[Release notes and verification](https://github.com/stokonn/Gould/blob/main/docs/releases/GOULD-0.15.md) · [Dependency licensing audit](https://github.com/stokonn/Gould/blob/main/docs/planning/gould-0.15/GOOGLE-DEPENDENCY-LICENSING-AUDIT.md) · [GitHub pre-release](https://github.com/stokonn/Gould/releases/tag/v0.15.0)

## Gould 0.16 — planning

The current planning focus is catalogue/database loading and search performance, plus UI responsiveness and loading states. The architecture and implementation scope remain under review; this is not a promise that a particular optimization or rewrite will ship. Performance claims must be based on repeatable measurements on representative Android hardware. Room/SQLite remain the baseline unless profiling demonstrates a concrete reason to change them.

The Google Sign-In replacement investigation was deferred from 0.15 and remains a separate scope decision; it is not silently included in the performance work.

## Earlier published releases

- [Gould 0.14.0](https://github.com/stokonn/Gould/releases/tag/v0.14.0) — deterministic weekly Activity, yearly heatmap, retrospective manual records, historical goals, Beta Week, Streak Freeze and durable cosmetic rewards.
- [Gould 0.13](https://github.com/stokonn/Gould/releases/tag/v0.13) — Project Renaissance, including recording metadata/database query improvements and Studio recordings-list refresh/recovery fixes.
- Earlier releases are listed in [GitHub Releases](https://github.com/stokonn/Gould/releases) and [CHANGELOG.md](https://github.com/stokonn/Gould/blob/main/CHANGELOG.md).

See [current project status](https://github.com/stokonn/Gould/blob/main/docs/ai/GOULD-CURRENT-STATUS.md) for the engineering handoff. Older development notes below are preserved as historical records, not current status.

created by stokonn with love <3

<details>
<summary>Preserved earlier documentation and dated development history</summary>

## Goldberg 0.11-rc.1 — published release candidate (October 25, 2026)

[Download Goldberg 0.11-rc.1](https://github.com/stokonn/Gould/releases/tag/v0.11-rc.1).
The candidate is a signed, non-debuggable release build, versionCode 12. Goldberg
0.10.1 remains the latest stable release. Catalogue, encyclopedia, offline bundles,
Smart Cache and accepted Android UI work are included; the Stage 8 Settings focus,
full navigation labels and test-scaffold contrast corrections are included.

**At that time, production community networking, cloud sign-in and real-user
contribution collection remained disabled, and no private Cloudflare Worker had
been deployed. A private Supabase schema existed but was never a live public
community service. The community/backend architecture has since been retired
entirely (see [backend retirement](https://github.com/stokonn/Gould/blob/main/docs/architecture/BACKEND-RETIRED-2026-11-25.md));
personal practice data remains local.**

[Candidate CI](https://github.com/stokonn/Gould/actions/runs/40212684482):302 JVM
cases passed;203 native cases,192 passed/11 existing fixture or probe-dependent
skips/0 failures on API36. Seeded 0.10.1 upgrade preserved original rows, preferences
and37 sessions;offline startup and process-death integrity checks passed. CI uses
debug rebuilds;the release APK certificate was independently verified against the
published APK. Final0.11/cloud acceptance remains open for credentials/privateTLS/
OAuth,privacy/source policy,operational backup custody and public API/client adoption.

Earlier dated entries below are historical and are superseded by this candidate
status where they describe pending UI corrections or the absence of a candidate.

---

## Current owner decision — October 24, 2026

**Stage7B ACCEPTED WITH DOCUMENTED LIMITATIONS; Stage7 Android client/synthetic acceptance scope COMPLETED.** Stage7A was already accepted. [Explicit owner decision](https://github.com/stokonn/Gould/issues/31#issuecomment-6388393525). Existing CI25 and emulator evidence is unchanged; no tests were rerun for this documentation-only decision.

Duplicate Settings TalkBack focus stops, large-font bottom-tab truncation and scaffold contrast findings are deferred to Stage8 for explicit release-readiness assessment, **NOT FIXED**. Speech quality, subjective motion, exhaustive accessibility coverage and real three-hour/reboot notification delivery remain UNVERIFIED. Stage6 G1–G9/G11, C4, 6.0f, Android/backend live integration and all production authorization gates remain OPEN. Stage8 remains PENDING; no main merge, production activation or release is authorized. Issue31 remains OPEN and its umbrella Project item In Progress. Prior entries below are historical.

created by stokonn with love <3

# Changelog & Roadmap

## Gould 0.15.0 — pre-release

Gould 0.15.0 adds a curated offline catalogue from the verified MusicBrainz Core snapshot `20261114-002230` (100 composers, 105,032 works and 105,421 composer relationships; MusicBrainz Core data is CC0) with repository provenance evidence, plus opt-in online MusicBrainz search using the existing debounce, cancellation, rate-limit and cache behaviour. Private Repertoire stays separate from public Catalogue records. The Library gains full-width equal Repertoire/Catalogue tabs; Artist pages gain scoped search, deterministic sorting and work-type/period filters. Guided onboarding and safer community-draft recovery are added, and a successful Activity week now consistently requires both configured weekly goals. The release is a pre-release, not Stable; the published APK is the exact artifact the owner tested on a Samsung Galaxy A55, and Android instrumentation was compiled but not executed. The Google dependency licensing question remains unresolved and is transparently recorded together with the owner's explicit release authorization. See [the release notes and verification status](../releases/GOULD-0.15.md) and [the licensing audit](../planning/gould-0.15/GOOGLE-DEPENDENCY-LICENSING-AUDIT.md).

## Gould 0.14.0 — pre-release

Gould 0.14.0 adds a deterministic weekly Activity model, a yearly heatmap, retrospective manual records, historical weekly goals, Beta Week, automatic Streak Freeze and durable cosmetic rewards. It retains Room, adds an additive personal database migration, and includes a Kotlin Multiplatform domain module with `commonMain` rules and a JVM target; Android remains the only application platform. Discover and visible moderation/admin navigation are hidden temporarily while implementations remain preserved. Cosmetic rewards are granted per eligible perfect week, independently of the four-consecutive-official-week Freeze milestone; Beta Week may earn a cosmetic but does not advance Streak or Freeze progress. The release is a pre-release, not Stable. Signed in-place upgrade verification and visual emulator review remain outstanding. See [the release notes and verification status](../releases/GOULD-0.14.md).

The latest published release is [Gould 0.15.0](https://github.com/stokonn/Gould/releases/tag/v0.15.0), a pre-release. [Gould 0.14.0](https://github.com/stokonn/Gould/releases/tag/v0.14.0) and [Gould 0.13](https://github.com/stokonn/Gould/releases/tag/v0.13) remain available as earlier releases.

**[0.10.1 — Final Redesign Polish](https://github.com/stokonn/Gould/releases/tag/v0.10.1)** refines the unified Library, saved completion/notes, Profile, About, Tux and compact previews. Package/signing identity, schema 8 and format 4 remain unchanged. See [verification](Verification-0.10.1).

- **0.9.1 Beta — The Quality Update:** published. Durable startup, retry/export recovery, bounded note persistence, paged history, localization fixes and supplied Classic Piano adaptive artwork. Automated verification and limitations are in its release notes; no physical-device certification.
- **0.9.0 Beta — The Redesign Update:** published. Editorial themes, four main destinations and CZ/EN groundwork.
- **0.8.0 Beta — Smart Practice:** published. Experimental microphone-based practice detection and durable multi-piece review.
- Earlier releases are listed in the repository changelog and [GitHub Releases](https://github.com/stokonn/Gould/releases).

**[0.10 — The Definitive UI/UX Redesign](https://github.com/stokonn/Gould/releases/tag/v0.10)** refines Home, Repertoire, Practice, Profile and Settings; adds local composer favorites, Favorite Artist autocomplete and safe post-session piece editing; and introduces additive schema 8 / backup format 4 with support for formats 1–3. It passed 72 JVM tests and 36 executed emulator instrumentation tests. Google identity scenarios passed owner-reported manual testing on a Galaxy A55; Codex did not test on a physical phone. See [Verification](Verification).

**[0.9.2 — Final Redesign](https://github.com/stokonn/Gould/releases/tag/v0.9.2)** establishes the unified design, daily Home activity/quotes, compact repertoire/profile/settings, single Practice entry, preserved recovery archives, explicit metered-media controls and keyboard/Tux easter eggs. Optional Google identity passed the six owner-reported manual scenarios; Codex did not perform phone testing. Seventy JVM and 31 executed Android tests pass, with migration/backup/localization/APK verification. See [Verification](Verification) for limitations. The permanent private Project and version milestones track maintenance and future work.

Goldberg 0.10.1 delivers the approved Final Redesign Polish. See [Verification](Verification). Goldberg 0.11 — The Music Database Update progressed through Stage 1 architecture planning, Stage 2 research/contracts, Stage 3 catalogue/backup implementation and Stage 4 Discover integration on the feature branch `feature/goldberg-0.11-stage-4`. Its approved target includes multi-genre catalogue integrations, sourced encyclopedias, offline bundles and moderated contributions with Android/Web Admin under a strict zero-cost cloud budget; practice cloud synchronization is not part of that approval. Stages 1–4 are implemented and verified on that branch only: no 0.11 release, tag or main-merge exists yet, and the published APKs remain 0.10.1. Provider integrations are local/offline-first: live Wikidata queries stopped on recorded maxlag and MusicBrainz on recorded HTTP 429, and no quota bypass was attempted. The [planning package](https://github.com/stokonn/Gould/blob/planning/goldberg-0.11-stage-1/docs/planning/goldberg-0.11/README.md) records the source audit, recommendations and decisions.


[Home](Home) · [Developer Guide](Developer-Guide)

Goldberg 0.11 [Stage 2 research and contracts](https://github.com/stokonn/Gould/blob/planning/goldberg-0.11-stage-2/docs/planning/goldberg-0.11/stage-2/README.md) specifies separate catalogue storage, typed identities, licensed fixtures, offline/backup safety and measured performance budgets. Stage 3 implemented the separate catalogue journal (schema 1) beside personal schema 10 with additive backup format 5 (formats 1–4 remain readable); Stage 4 added the Discover tab with offline-first search, typed unresolved candidates, licensed-portrait verification, projected response caching, per-page work-subtype row labels and localized unnamed-entity placeholders. Both stages passed full 143-test instrumentation suites, four actual process-death phases and fresh seeded signed 0.10.1 (schema 8) to schema 10 offline upgrades on API 26 and API 37 emulators with exact persistent-table equality. This work is un-released branch verification, not a published feature; live provider calls remain blocked by the recorded Wikidata maxlag and MusicBrainz 429 responses.

created by stokonn with love <3


## Goldberg 0.11 Stage 7B — verified synthetic recovery, October 24, 2026

Published production remains Goldberg 0.10.1. The unreleased Stage7B branch's test-only correction `fb1d3f971ed79de09dccbdc771e1c45a09dc0a6c` respects the required changed-revision acknowledgement before preparing decisions; production code, personal database, backups and practice behavior are unchanged. [Exact-source acceptance40097540179](https://github.com/stokonn/Gould/actions/runs/40097540179) and automatic quality40097530580 passed. Independent raw XML:187 tests,0 failures/errors,2 existing external-fixture skips; all9 Stage7B cases passed. Clean local verification:275 JVM tests,0 failures/errors/skips;lint0 errors,119 warnings,1 hint;seven validation scripts passed.

All262 required screenshot/tree pairs were retained and inspected, covering32 dashboard configurations and16 actual Queue/Audit row frames, native focus checks and row-open/Back. Seeded same-runner debug upgrade from immutable0.10.1 preserves all12 personal collections/37 sessions, preferences and schema8→10 integrity. Temporary CI signing is not release-signed installation verification. [Owner report, actual evidence and limitations](https://github.com/stokonn/Gould/blob/feature/goldberg-0.11-stage-7b/docs/planning/goldberg-0.11/stage-7/stage-7b/FINAL-OWNER-REPORT.md).

Stage7B remains **In Progress**: owner acceptance, manual TalkBack/complete app navigation/reduced motion, separate-process platform notification-permission revocation and live G1–G9/G11 integration remain open. Larger-font navigation-label truncation, dark-parent status-bar contrast, awkward wrapping and timed-frame limitations remain disclosed. Production networking/moderation stay disabled. No phone testing, production activation, main merge or new release. [Issue31](https://github.com/stokonn/Gould/issues/31) is the current release dashboard; earlier development notes are historical checkpoints.

created by stokonn with love <3


### October 24, 2026 — Stage 7B manual verification closure

Stage 7B Android client verification is **READY FOR OWNER REVIEW**, not accepted or released. CI #25 remains the verified source-bound result (187 native tests, 2 existing skips, no failures; 262 screenshot/tree pairs; seeded upgrade preserved). Representative emulator checks now cover real English/Czech TalkBack navigation and accessible Back, touch exploration, keyboard/IME/system Back, reduced-motion navigation and cross-process notification-permission revocation. The temporary recorder is archived; normal app/test/CI sources and AndroidJUnitRunner are restored.

Proposed owner disposition: ACCEPT WITH DOCUMENTED LIMITATIONS for the verified client scope. Three Admin Settings label/control pairs duplicate TalkBack focus stops; audible speech quality, subjective motion comfort and exhaustive configurations remain unverified. Live G1–G9/G11 integration is BLOCKED until Stage 6 authorization; Stage 8 integration/release gates remain separate. No main merge, production activation or release has occurred. See the branch's Stage 7B FINAL-OWNER-REPORT.md and existing Issue #31 for evidence and the pending owner decision.

created by stokonn with love <3

</details>
