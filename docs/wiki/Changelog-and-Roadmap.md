# Gould 0.12 — development

> Gould 0.12 development documentation. These features are implemented on `feature/gould-0.12`; full emulator and release acceptance is still in progress. The latest published release remains [Goldberg 0.11 Stable](https://github.com/stokonn/Gould/releases/tag/v0.11). Historical sections below retain their original evidence.

Implemented: explicit local WAV recording and recovery/media archive controls; Studio metronome, durable queue/Undo/notes; Previous history/recordings and local playback; centered Profile and persistent editor drafts; six evidence-based badges and durable notices; both adaptive launcher widgets; corrected original ASCII Tux and subtle local Home landscape. Full acceptance and Stable publication remain pending.

# Changelog & Roadmap — Goldberg 0.11

0.11 Stable integrates the approved Music Database Update: shared Discover/Library catalogue, sourced details, optional provider search, offline bundles/cache and verified Android work. Personal data and backup compatibility are preserved.

The existing public catalogue API is read-only and currently empty. Backend OAuth, community writes and practice sync remain disabled. No future-version scope or live integration is implied. The privately backed-up obsolete v0.11-rc.1 prerelease/assets were removed by owner-authorized maintenance; its tag and all Stable history remain. No additional RC was created.

[0.11 release](https://github.com/stokonn/Gould/releases/tag/v0.11) · [Exact validation and limits](https://github.com/stokonn/Gould/blob/main/docs/releases/GOLDBERG-0.11.md).

created by stokonn with love <3

<details>
<summary>Preserved earlier documentation and dated development history</summary>

## Goldberg 0.11-rc.1 — published release candidate (October 25, 2026)

[Download Goldberg 0.11-rc.1](https://github.com/stokonn/Gould/releases/tag/v0.11-rc.1).
The candidate is a signed, non-debuggable release build, versionCode 12. Goldberg
0.10.1 remains the latest stable release. Catalogue, encyclopedia, offline bundles,
Smart Cache and accepted Android UI work are included; the Stage 8 Settings focus,
full navigation labels and test-scaffold contrast corrections are included.

**Production community networking, cloud sign-in and real-user contribution
collection remain disabled. No private Cloudflare Worker has been deployed.**
The existing private Supabase schema is deployed, but it is not a live public
community service. Personal practice data remains local.

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
