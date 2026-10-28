> Phase2 approved2026-10-28 from29bb4d1, extended by [mandatory launcher icon](APP_ICON_ADDENDUM.md). [Owner authorization](OWNER-AUTHORIZATION.md) includes conditional final main integration/Stable publication after ALL gates; no redundant approval. Original Phase1 proposal below is preserved with its historical status. One implementation stage continues.

# Gould 0.12 — Phase 1 architectural review and complete proposed plan

Date: 2026-10-28. **PROPOSED; awaiting explicit owner approval for Phase 2.**
One implementation stage, within exactly two main phases. No application features,
schema changes, build, emulator execution, main merge or release in Phase 1.

## Authority and verified baseline

The central specification is [MASTER_PROMPT.md](MASTER_PROMPT.md), with
[IMPLEMENTATION_BRIEF.md](IMPLEMENTATION_BRIEF.md), [WIDGETS_SPEC.md](WIDGETS_SPEC.md),
[the acceptance matrix](../../design/gould-0.12/ACCEPTANCE_MATRIX.md) and
[asset manifest](../../design/gould-0.12/ASSET_MANIFEST.md). Current owner instructions
and data/security policy govern conflicts. Images supply visual direction, never
fictional data, automatic feature scope or production assets.

- Canonical checkout remains `/home/stokonn/Projects/Opus`, `main`,
  `ef828985d27a2f56b36c18cf2277b0ca4373a63f`. Tracked source initially clean;
  owner untracked files retained with a private before-hash inventory.
- Live API confirms `stokonn/Gould`, ID 1421247418, same main SHA. Main quality
  [40596043801](https://github.com/stokonn/Gould/actions/runs/40596043801), backend
  [40596043941](https://github.com/stokonn/Gould/actions/runs/40596043941) and admin
  [40596043982](https://github.com/stokonn/Gould/actions/runs/40596043982) all succeeded.
  Readback is verification of existing runs, not tests rerun in Phase 1.
- PMP documented `status/resume/history/check/verify` interfaces inspected.
  Latest canonical checkpoint sequence 29, `9d495826-7782-4f10-883a-b70e96607314`,
  records explicit owner-approved fast-forward main integration and its successful
  CI. Older pending-merge notices in AI/maintenance documents are superseded by
  that checkpoint and live main. Review branch remains retained.
- Full current Actions response has **five active registrations**: quality,
  community-bff, stage5-emulator, maintenance-admin and managed Copilot. Older
  four-registration totals predate maintenance-admin. No administration rerun.
- 30 live branches; 14 local tags retained; 13 published releases including Stable
  v0.11; obsolete RC release absent, RC tag retained. Issue #31 and milestone 4
  are closed for completed 0.11. Draft PRs #39/#40 remain unmerged proposals.
- Existing private Project [Goldberg Development](https://github.com/users/stokonn/projects/2)
  has Status, Priority, Version and Work Type fields. No 0.12 issue or milestone
  existed in the initial inventory. Wiki enabled and initialized; remote master
  `721ef5c02438fc05fc40af5c9b2167641a4cce91` is readable. No Wiki write/auth change.
- Seven historical worktrees still require owner-controlled private-data custody.
  **Overall maintenance remains INCOMPLETE.** These are not application build
  dependencies. Isolated planning and, after approval, isolated feature work can
  avoid them entirely. No retirement, controller execution or custody assumption.
- Personal Room schema 10, catalogue schema 1, backup format 5, minSdk 26,
  target/compile SDK 36, version code 13; `com.opus.piano` and `opus.db` unchanged.
  Existing trusted certificate remains
  `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`.

## Existing architecture and findings

This is an evolutionary Compose/Material 3 change. Keep application composition
in `OpusApplication`, existing navigation, repositories, semantic colors,
Cormorant Garamond/Inter and all appearance settings. Do not introduce another
practice owner, app framework, cloud integration or general dependency upgrade.

| Finding / evidence | Consequence and proposed solution |
| --- | --- |
| `PracticeScreen.kt` already displays `timer.elapsed`; `PracticeEngine` separately stores `wallElapsed`/`detectedActive`, enforces evidence freshness and bridges short rests. Existing JVM tests cover silence/stalls/short gaps. | The reported Smart symptom is not proven to be a wall-time binding defect. Characterize detector input and interval boundaries before changing thresholds. Keep one monotonic credit ledger and exact sums. |
| `SmartPracticeService` calls `startSmart()` and stops capture when `running=false`; `PracticeCoordinator.pause/review` invoke `releaseCapture`. | Continuous recording cannot be bolted onto this lifecycle. Separate recording intent/state from credit-running state while retaining one service/capture owner. |
| `MicrophoneController` streams one bounded PCM buffer into the heuristic only; no local audio persistence exists. | Fan out a shared timestamped PCM stream to detector and bounded recording writer, without a second `AudioRecord` or sound-triggered file cutting. |
| Current service/evidence callbacks carry token + execution identity. | Preserve generation checks during stop/start/route/recovery races; extend to recording operation IDs and idempotent finalization. |
| Existing `SavedRecording`/`RecordingsScreen` represent HTTPS interpretation/score bookmarks. | Create distinct local-audio entities/repository/player. Never repurpose or migrate bookmark rows into audio. |
| History is bounded keyset paging with full review/edit semantics; generic history currently selects Profile in `selectedTab`. | Reuse the history list/review, add Library-associated Previous with two tabs, and deliberately test navigation ownership/Back. Retain historical route identifiers. |
| Profile uses four columns at normal width and starts card contents at the top. Editor dismisses with dirty local fields; save operations expose `busy` but no editor commit acknowledgement. | Implement actual 2×2 centered cards and a draft editor with explicit write results and Save/Discard/Keep editing guards. Favorite Era storage already exists: no new era column needed. |
| Badges use content-sized adaptive grid cards; unlock handling remembers IDs in Compose and shows only the first new award in a Snackbar. | Equalize each row's natural height/slots; persist an unlock-event outbox and acknowledgements, queue all events and defer during playing. |
| Registry has exactly 17 historical definitions; unlock insert uses IGNORE and preserves first timestamp. | Extend definitions/events without changing IDs, earned dates, existing medals or showcase selections. |
| `BalancedHome.WeeklyJournalChart` uses a trailing seven days, not a calendar Mon–Sun week. | Widgets need an explicit calendar-week projection from the existing ledger; do not silently alter the approved Home chart. Share attribution/day rules, parameterize the window. |
| No metronome/BPM/Undo implementation found in current Kotlin source; multi-piece selection and compact expansion already exist. | Include a bounded metronome and queue Undo implementation in this plan. Do not describe nonexistent controls as preserved functionality. No automatic tempo/piece recognition inferred. |
| Tux source already requests Monospace and disables wrap; private screenshot shows substituted-looking glyphs, historical emulator screenshot is aligned. | Font substitution/root cause remains unconfirmed. Use an explicit isolated mono TextStyle and stable metrics; preserve ASCII bytes/pink/wording/access/signature. Do not change global typography to guess at Samsung behavior. |
| Backup v5 has strict section/schema validation and 32 MiB JSON limits. | Version the extended journal backup; never inject audio/base64 into v5 or label omitted audio a complete media backup. Preserve old readers/import paths. |
| No widget providers or appCategory in manifest; WorkManager is already present. | Prefer platform AppWidgetProvider/RemoteViews with existing WorkManager, avoiding Glance dependency for two small surfaces. Declare Education using supported application metadata, not widgetCategory. |
| Current new private PNG folder initially had no exclusion. | Added shared local exclusion and proposed tracked `.gitignore` protection in this isolated worktree. All owner PNGs stay local/untracked and outside resources. |

Source analysis is not runtime reproduction or an independent review. No new
critical app security vulnerability is asserted from these findings. Existing
read-only catalogue consent, rate limits, caches and provenance remain intact;
backend OAuth, community writes and journal/audio uploads stay disabled.

## Proposed technical design

### Studio, credit and audio (ST-01–03)

Keep persisted `PRACTICE` route and service/action/storage identities; rename
user-visible Practice to Studio in EN/CZ. The five destinations remain
Home / Library / Discover / Studio / Profile. Settings stays in Profile.

`PracticeCoordinator` remains the single serialized practice writer. Detection
emits timestamped activity evidence; only the engine decides credited intervals.
Capture freshness, debounce/onset, short-gap bridging, long silence, manual pause,
route interruption and recovery each need explicit tests. The primary timer is
credited playing time in Smart mode; secondary elapsed and recording duration
are clearly different values. Keep manual fallback and save-before-completion.

Extend the existing microphone service with a shared capture session and separate
consumers: detection enabled, recording enabled, or both. Permission acquisition
and foreground startup require a visible, explicit Studio action. The recording
state machine is Idle → Preparing → Recording → Finalizing → Saved, with
Interrupted/Recoverable/Failed states. Durable operation identity precedes capture.
No UI composition, Smart toggle, navigation, widget, reboot or process recovery
starts recording. Recovered practice remains stopped; recovered audio offers
review/retry/export, never capture resumption.
Update the existing analysis-only microphone disclosure so it truthfully
distinguishes transient detection from explicitly saved local audio. Recording
consent is not inferred from existing Smart consent. Show storage/export privacy
information where it supports that choice; no automatic personal-audio upload.

Proposed interaction: Record explicitly starts audio in the current Studio
context; if practice is idle, it does not implicitly start credit. Pause affects
practice credit but leaves an explicitly running recording continuous. Stop
Recording finalizes audio without completing practice. Finish practice stops and
finalizes associated recording safely before showing saved results; a failed
audio finalization remains recoverable and does not erase the journal. Fatal
capture/permission/route interruptions stop capture truthfully rather than
manufacturing continuous audio. Never silently overwrite incomplete files.

Prefer platform PCM capture + AAC-LC/M4A encoding with bounded queues and
recoverable staging/segments. Probe codec/rate support on API 26/36 before fixing
the final format; keep recovery bytes until verified finalization. The writer
must not block evidence delivery or hold unbounded buffers. If storage/encoder
cannot keep up, surface a stopped/interrupted recording; do not drop samples and
pretend continuity. Verify silence/rest/reverb and encoder padding against known
PCM input; final audio duration derives from media samples, not practice credit.

Persist recording ID, operation ID, format/sample count/duration, safe relative
path, size/hash, lifecycle state, creation/stop metadata and session token;
store multi-piece associations/offsets separately. Association to a completed
session is idempotent and may be nullable: deleting a journal session/piece must
not cascade-delete personal audio. Titles retained as snapshots. File publication
uses same-filesystem atomic moves with a reconciler for file/DB crash boundaries.
Only verified Saved files appear as playable saved recordings; recoverable files
have a separate honest status. Explicit delete confirms and uses retryable file/DB
coordination. No automatic retention purge or cache-clear deletion of recordings.
Exports stream through SAF or a narrowly scoped, grant-only FileProvider; no raw
absolute paths or broad storage permissions. Stop playback before recording to
avoid self-credit; audio focus/interruption behavior is covered by native tests.

Preserve prominent timer, compact Smart control/notes and multi-piece queue.
Implement queue editing/manual switch through coordinator commands. Undo restores
selection/queue intent from the operation boundary; it never retroactively moves
already credited intervals or invents per-piece durations. Baseline metronome
proposal: compact manual BPM/beat controls and deterministic audio clicks,
independent of practice credit, no automatic tempo inference. Verify that clicks
do not generate false Smart credit; no competing practice timer. Stop styling uses
semantic accent/neutral; recording indicator stays small and accessible.

### Library, Previous and playback (LB-01–02)

Retain `RepertoireScreen` Search+Add, All/My Repertoire/Artists, Sort/Filters,
canonical catalogue/local distinctions, favorites and current details. Add a
discreet Previous entry; Studio upper-right history icon opens the same route.
Previous has Practice History and Recordings tabs, retains tab/scroll/Back state,
and is associated with Library without adding a Library filter or bottom tab.
Home and piece-filtered history remain reachable and editable as before.

Reuse bounded history paging and `HistoricalReview`/journal edits. Local audio
gets bounded newest-first paging, real durations and piece/session snapshots.
Provide basic play/pause/seek, error/retry, explicit share/export and delete.
Use a lifecycle-owned platform player; background playback is not assumed scope.
No trimming, speed editor, generated waveform, stock player picture or external
audio import is inferred from the concept image. URL bookmarks remain in their
existing Listen & Study flow.

### Profile and editor (PR-01–02)

Normal profile statistics are exactly 2×2, equal size per row and centered as a
whole number/label block in both axes. At constrained width/200% fonts use one
column as needed, allowing natural text height without shrinking important text.
Keep configured statistic order, avatar beside name/bio, settings/admin gates,
favorite composer and direct showcase management (max five).

Add era autocomplete from distinct supported local catalogue periods and localized
labels, using existing accent-insensitive normalization; no invented subperiods
from image 08. Retain historical/manual field values and original composer names.
Use a SavedStateHandle-backed edit draft with an explicit original revision and
persisted result. Guard Back, outside dismissal, navigation and rotation with
Save/Discard/Keep editing as appropriate. Rotation retains drafts; it need not
prompt. Save remains open on failure and closes only after transaction success.
Handle avatar/name changes explicitly so partial saves are not represented as
all-fields saved; preserve identity override semantics and unrelated settings.

### Achievements, legacy eligibility and popup (AC-01–03)

Keep all 17 old IDs, art and first-earned timestamps. Adaptive collection uses
two columns normally and one when constrained; measure row content to equalize
card heights and align medal/title/rarity/date/progress slots without clipping
long Czech labels. Reduced motion applies to cards and unlock popup.

Proposed definitions for owner approval with this plan:

| Name | Proposed genuine qualifying event |
| --- | --- |
| First Take | One successfully finalized, nonempty, decodable owned recording. |
| Recording Artist | Ten distinct successfully saved recording IDs, counted once; deletion/re-save/replay cannot farm the counter. |
| Versatile Pianist | Three distinct canonical/local pieces with positive measured credited time in one valid completed session; free practice and untimed added associations do not count. |
| Musical Journal | Ten distinct nonblank saved session/part notes attached to valid practice, counted once by stable note identity; repeated edits/whitespace do not count. Repertoire biography/bookmarks do not qualify. |
| Critical Listener | Actual advancing decoded playback of owned Saved audio; proposed threshold min(10 seconds, full duration), with positive duration. A button click, failed decode or seek does not qualify. |
| Founding Pianist | Valid practice before the actual eventual Gould 1.0 Stable release cutoff, including preserved Goldberg history; permanent Legacy/Legendary award. |

Recording/notes/playback events are committed durably after successful operations,
with stable idempotency keys, and evaluated within the existing transactional
achievement boundary. Historical note eligibility is recomputed from persisted
valid notes; no historical recording/playback events are invented. Counts and
earned dates have explicit restore semantics; previously earned awards never
revoke when source content is edited/deleted.

Founding Pianist has no invented 1.0 date. Pre-1.0 builds allow valid pre-1.0
practice and preserve eligibility evidence; at the actual 1.0 Stable release the
release policy supplies its real immutable cutoff. Later legacy imports may
qualify by completedAt before that cutoff. Fresh post-cutoff practice must not
qualify. Tests inject a hypothetical cutoff solely as fixture data, covering
before/equal/after, delayed migration and restore. Offline clocks/history are not
tamper-proof. Preserve awarded dates separately from qualifying practice dates.

Persist unlock-notification events with acknowledgement, ordered deterministically.
Backfill existing historical awards as already seen rather than flooding users;
newly earned legacy/new awards enqueue once. One themed popup shows original
medal, title/rarity, View Badge and Dismiss. Defer during active playing/capture
and important dialogs. Ack only on explicit dismissal/view; process recreation
retains pending events and prevents replay of acknowledged events. No system
notification permission is needed for this in-app popup.

### Both Android widgets (WG-01–04)

Implement **Quick Practice A** and **Weekly Activity C** using platform
`AppWidgetProvider` + `RemoteViews`, immutable explicit activity PendingIntents
and appWidgetId-specific identities. Existing WorkManager handles bounded refresh;
no Glance dependency is proposed. API 31+ size variants and pre-31 options-based
layouts support practical sizes, not guaranteed pixel-identical launcher cells.

Quick Practice: 1×1 icon/clear accessible shortcut, 2×1/4×1 contextual label/piece
where persisted. Intelligent Open B resolves live coordinator state first, then
durable recovery/draft, otherwise ready context. The activity revalidates stale
extras and awaits recovery before routing; never send `practiceRequest` values
which can trigger the current start branch. No tap starts mic/timer/recording,
creates a row, selects over an active draft or bypasses a confirmation.

Weekly Activity: 2×2/4×2 seven actual credited totals, Monday–Sunday in current
locale/time zone using existing recorded-day compatibility semantics; small form
shows total/status without an illegible chart. Preserve historical recorded days;
do not reassign sessions or sum expanded per-piece rows as session totals.
Recompute week boundaries on zone/date changes. Shared projection consumes a
coherent persisted journal snapshot. Current unsaved timer/recording duration
never enters published totals.

Store a minimal versioned last-known verified snapshot with timestamp/week/zone,
appearance/locale generation and no notes, identities or audio paths. Separate
zero, empty, unavailable and stale. Invalid/previous-week snapshots cannot be
shown as fresh current totals. Compact form keeps an accessible stale label.
Update on actual journal changes/restores, relevant draft state changes,
appearance/locale/zone/date change, boot/package replacement and widget resize.
Coalesce invalidations; no 250 ms widget ticker. Use bounded inexact rollover
work and a low-frequency safety refresh while instances exist; scheduling delays
must remain visible as stale data, never promised exact midnight execution.
Uninstalling the last widget cancels only its own refresh work.

Education classification: the platform ApplicationInfo category list has no
Education enum. Record Education in distribution metadata where that channel
supports it; leave the application category unchanged pending a supported mapping.
Keep widgetCategory `home_screen`. Verify exact platform enum before any manifest
change; no unsupported `education` flag or Play publication is proposed.

### Tux and Home (TX-01, HM-01)

Preserve exact ASCII content/intent, pink terminal tokens, kernel-panic wording,
About-logo long press, close action and `created by stokonn with love <3`.
Use an explicit mono style with stable line metrics, preserved whitespace and
horizontal/vertical overflow fallback. Compare existing historical emulator render
and original private screenshot to narrow/large-font new renders. Samsung-specific
X10 remains unverified until attributed owner observations support closure; no
agent phone operation or guessed global font fix.

Home changes only inside the existing quote card: one small locally bundled
authorized original/licensed nature image, subtle theme-aware scrim/gradient,
current text/source/cache/attribution and accent preserved. Quote card stacks on
small/high-font layouts as today. Keep a solid fallback for resource/contrast
constraints and exclude decorative art from spoken semantics. Asset rights are
an acceptance gate: reference 05 is not production media. Select a documented
permissively licensed local landscape or produce an authorized original in Phase
2; preserve license/source/hash and scaled decoding. No new Home buttons/gear.

## Persistence, backup, privacy and performance

Propose one additive personal **10→11 migration**, with schema export and all
historical migration paths retained: local recordings/segments/associations,
achievement event/progress evidence and popup acknowledgement tables; editor draft
persistence only if SavedStateHandle is insufficient for the agreed process-death
contract. Exact columns/indexes are implementation details reviewed against tests.
No era column duplication, catalogue schema rebuild or destructive fallback.

Propose **backup v6** for new journal metadata/events/acknowledgements, retaining
v1–v5 import normalization. Document that an old binary need not read a new format;
backward compatibility means old exports remain readable without lost fields.
Keep existing critical-journal JSON export bounded and independent of audio size.
Add an explicit optional streaming audio archive/restore with manifest, hashes,
path traversal/duplicate/size/expansion limits and staged validation. A journal-only
backup labels audio as excluded/missing; never creates fake playable recordings.
Preserve destination audio and raw recovery before replacement; coordinate staged
files and DB with a replayable restore journal rather than assuming a filesystem
move is a Room transaction. Interrupted/failing media restore must not corrupt
critical journal restore or silently discard recoverable originals. Cache clear
cannot touch audio, badge evidence or drafts. No cloud/device-transfer backup
permission expansion, default upload, network widget work or public personal data.

Keep filesystem/encoding/hashing/decoding and database projections off main.
Bound buffers and paged queries; index recording dates, operation identities and
associations. Reuse coherent journal snapshots; avoid all-session re-evaluation
on capture ticks. Measure capture→evidence latency, encoder backlog, UI frame
responsiveness, storage growth and widget wakeups against the baseline; fix
observed regressions rather than performing unrelated repository cleanup.

## Dependency-ordered execution — ONE development stage

Expected existing edit points: `practice/PracticeCoordinator.kt`,
`practice/PracticeEngine.kt`, `audio/MicrophoneController.kt`,
`audio/PianoActivityDetector.kt`, `service/SmartPracticeService.kt`,
`data/PracticeRepository.kt`, `data/OpusDatabase.kt`, `data/BackupRepository.kt`,
`data/Achievements.kt`, `data/AchievementStore.kt`, `ui/PracticeScreen.kt`,
`ui/OpusApp.kt`, `ui/AppNavigation.kt`, `MainActivity.kt`,
`ui/RepertoireScreen.kt`, `ui/PracticeHistoryScreen.kt`, `ui/ProfileScreen.kt`,
`ui/ProfileViewModel.kt`, `ui/BadgesScreens.kt`, `ui/TuxScreen.kt`,
`ui/BalancedHome.kt`, manifest/resources and meaningful JVM/androidTest suites.
Proposed new responsibilities: `audio/PracticeCaptureSession`,
`audio/RecordingWriter`, `data/LocalRecordingRepository`, local recording Room
entities/DAO, `data/AchievementEvents`, `ui/PreviousScreen`,
`ui/LocalRecordingPlayer`, profile edit draft controller, and
`widgets/{QuickPracticeWidget,WeeklyActivityWidget,WidgetSnapshot,WidgetRefresh}`
with XML layouts/provider metadata. Names may follow established conventions;
these boundaries must not duplicate existing repository/state-machine logic.

These are internal work batches, not separately approved stages or new phases.

1. Record explicit Phase 2 approval and approved definition choices. Revalidate
   HEAD/CI and private custody boundaries. Use this already isolated worktree on
   a feature branch; carry its complete spec/reference inventory forward.
   Recheck live tracking first, then create/reuse one Gould0.12 milestone and one
   umbrella/stage issue in the existing Project, with purpose/scope and this
   living checklist. Keep implementation Todo until approved, then In Progress;
   Done only after verification/acceptance. Preserve Issue31 as closed history.
2. Establish meaningful baseline regressions and synthetic PCM fixtures. Define
   capture/recording/credit contracts, additive schema and backup v6/restore tests
   before exposing recording controls. Retain source-bound before screenshots.
3. Implement shared capture, recoverable writer, truthful states and idempotent
   audio finalization. Verify detector/accounting behavior, manual fallback,
   interruptions, queue/Undo/metronome and pause-versus-record semantics.
4. Wire Studio layout, Previous/history/local player, navigation and explicit
   export/delete; test old URL bookmarks and history editing unchanged.
5. Complete Profile grid/era/draft persistence and achievement definitions,
   eligibility/event outbox/popups; finish extended backups and seeded migration.
6. Implement both widgets from the persisted ledger and safe activity routing;
   complete resize/rollover/stale/reboot tests with real launcher screenshots.
7. Apply isolated Tux/quote changes and finish localization/accessibility,
   performance, full changed-scope regressions and actual emulator comparison.
8. Deliver verified debug APK/report for owner visual/product acceptance; keep
   the single stage open while required gates fail or remain unverified. Update
   existing tracking dashboards/comments, PMP and AI handoff at each verified
   milestone and before termination/75–80% context; no auto-compaction assumption.
9. Only with separate applicable owner authorizations: main integration, clean
   non-debuggable production-signed build, release-specific gates/upgrade and
   stable publication. No RC, tags, release build or release upload in development.

## Test strategy and acceptance gates

| Gate | Required evidence before completion |
| --- | --- |
| Practice algorithms | Synthetic tonal/quiet piano/noise/silence/rest/reverb fixtures, sample-rate/partial-buffer cases; fake monotonic clock; onset/gap boundaries, stale generations, stalls, mixed/manual pause and multi-piece sums. No claim of perfect piano identification. |
| Audio durability | One capture owner in all mode combinations; explicit Record only; continuous known PCM span including silence while credit pauses; valid decoded duration, stop races, permission revoke/focus/routes/background, file/DB failure boundaries, corrupt/short/large files, ENOSPC injection and actual available storage observations distinguished. |
| Data/restore | Every supported old migration→11 and fresh schema; seeded trusted 0.11→debug 0.12 in-place upgrade preserving original typed cells, dates/showcase/profile/bookmarks/unfinished and unreadable drafts; v1–v5 imports, v6 roundtrip, missing audio, bad hashes/path traversal, interrupted restore/retry/export, SQLite integrity and FK checks. |
| Achievements | Old17 IDs/art/dates unchanged, once-only events, no click awards, per-piece measured credit, note edit/restore dedup, hypothetical cutoff before/equal/after, late Goldberg imports, permanent unlocks and popup queue/ack recovery/defer/reduced motion. |
| Editor/navigation | Dirty Back/outside dismissal/route/rotation/process recreation, failed writes/no false Saved, autocomplete accent matching and manual values; Previous both entry points and tabs, historical route restoration, widget stale/missing extras and zero auto-start side effects. |
| Widgets | Both providers added on actual API36 launcher, compact/expanded sizes, multiple instances, resize/taps/ready-active-draft-recovery, process death/reboot, date/week/year/zone/DST change, locale/theme/accent/font1/1.5/2, genuine persisted totals and explicit stale/unavailable distinction, bounded refresh logs. API26 compatibility smoke. |
| Visual/accessibility | All ST/LB/WG/PR/AC/TX/HM IDs: Ivory/Nocturne × EN/CZ × representative 320/360/412dp and larger width × fonts1/1.5/2; meaningful full matrix for changed layouts, named real captures/trees, touch48dp, contrast, TalkBack/keyboard/IME/Back and zero animations. No clipped important names/labels or hiding destinations. |
| Build/artifact | Wrapper debug/JVM/androidTest compile/lint, applicable scripts, actual instrumentation XML counts separately; manifest/package/version/code, trusted certificate, APK ZIP/resources/reference/secret exclusion, hash, source SHA and upgrade binding. No release success inferred from debug assembly. |
| Owner acceptance | Actual Studio/Previous, compact Profile, badges/popups, Tux, Home quote and **both widgets** reviewed; residual limits recorded; no unverified boxes checked. |

Planned host command (not run in Phase 1):
`./gradlew clean assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug --no-daemon --max-workers=1`.
Run relevant `scripts/check_*.py`, migration/backup/paging/localization/policy
checks and native regressions. ADB always explicitly names a confirmed
`emulator-NNNN` and checks `ro.kernel.qemu=1`; never the owner's phone. Existing
dispatch-only emulator CI may be used with exact commit binding, raw failures
retained and no unchanged retry. CI debug-rebuilt baselines are not trusted-signer
upgrade evidence. Do not change signing to force an upgrade or uninstall data.

## Reference inspection and worktree continuity

Platform recommendations were checked against primary documentation:
[responsive RemoteViews layouts](https://developer.android.com/develop/ui/views/appwidgets/layouts),
[bounded widget updates](https://developer.android.com/develop/ui/views/appwidgets/advanced),
[microphone foreground-service restrictions](https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start),
[media formats](https://developer.android.com/media/platform/supported-formats) and
[ApplicationInfo categories](https://developer.android.com/reference/android/content/pm/ApplicationInfo).
RemoteViews/codec selection is this review's engineering recommendation, not a
claim that the existing app already implements it.

All 14 v2 PNGs inspected individually and SHA-256 matched the manifest. References
01–09 supply Studio/Previous/player, Profile defect, badge defect, Tux defect,
quote/popup mood, Library, autocomplete and grid. References 10–14 supply widget
board/Quick/Weekly/Intelligent/Adaptive examples. Reject image03 uneven heights,
image09 three-column layout, fictional labels/statistics, automatic-play-looking
widget controls, player editing features and moodboard quote attribution as
requirements. Written 2×2 Profile/two-column-or-one badge rules prevail.

Reviewed historic 0.10 approved Home/Profile and 0.10.1 approval matrix/private
references, prior real Profile/Practice/Tux emulator captures and current source
including 0.10.1→0.11 diffs. These are prior/concept evidence, not new renders.

Planning worktree: `/home/stokonn/Projects/Opus/.gradle/worktrees/gould-0.12`,
branch `planning/gould-0.12-architecture`, base ef82898. Required tracked handoffs
arrived via Git; 28 additional files copied and hash-verified: three scope MDs,
three design MDs, all14 PNGs, seven historical private references and the existing
PMP configuration. Receipt: private `.gradle/phase1-reference-transfer.json`.
No private PMP history relabeled/copied; new worktree uses its own history under
the same project UUID once supported bootstrap is available. Its copied config
is intact, but `pmp status`/`doctor` return exit2 because the new worktree has no
`.pmp/state.json`. Installed `pmp init` preserves an existing configuration and
does not bootstrap that missing state. No private state is fabricated, existing
history copied, UUID regenerated or standalone PMP modified. Phase1 checkpoint
uses the canonical checkout's documented CLI and retains the unresolved
maintenance task. Before Phase2, resolve supported worktree bootstrap or retain
canonical PMP as the documented reporting interface with explicit feature-SHA
binding; do not imply new-worktree native evidence exists.
Shared local Git exclusion protects the new PNGs in both
checkouts; tracked `.gitignore` adds the permanent reference-directory rule.
Build assets have no reference-folder linkage. Future branch work must retain this
worktree or verify the same full inventory after any transfer; Git alone cannot
carry ignored PNGs or currently untracked scope files. Do not delete originals.

## Proposed permanent policy wording

For owner approval, add to canonical root AGENTS.md (thin tool files reference it):

> Every Gould version has exactly two main phases: (1) Architecture, Review &
> Planning, with full policies/PMP/specification/visual/compatibility review and
> one complete implementation plan; no application features; explicit owner
> approval required before (2) Implementation, Verification & Release. Phase 2
> implements the entire approved version scope as one development stage, allowing
> internal commits/checkpoints/test batches. Preserve historical stage records.
> Verify mandatory handoffs and private references in the development worktree.
> Debug builds are for acceptance. Main merge, production-signed release build
> and stable publication retain their applicable separate owner approvals and
> mandatory gates. Preserve data/signing identities, private PMP and owner files;
> never operate the owner's physical phone. Maintain one living stage checklist,
> its GitHub dashboard/history, Project, PMP and AI handoff with truthful evidence.

This session follows the owner's two-phase instruction already. The generalized
permanent policy edit is proposed, not silently declared owner-approved.

## Approval requested and remaining limitations

Approve this complete plan to begin Phase 2, including the proposed award
names/qualifying rules, pause/record semantics, bounded manual metronome/queue
Undo behavior and backup v6 with explicit optional audio archive. These clarify
undefined behavior; they are not additional development stages. Final codec/rate
selection is a tested engineering decision; a material incompatibility returns
to the owner with evidence. No exact Gould 1.0 cutoff or production nature asset
has been invented. Their real provenance must be settled before acceptance.

Phase 1 produced source analysis, Git/GitHub/PMP inventory and reference hash/
transfer/privacy checks. **No new Gradle, JVM, instrumentation, emulator,
recording, migration, upgrade or performance execution; no debug APK produced.**
Historical CI/test results retain their original source identities. Samsung-only
X10 remains unverified. Custody remains unresolved independently of 0.12.
Executed documentation checks: authored-file `git diff --check` and
`python3 scripts/check_agent_policy.py` pass. Staging the supplied brief reveals
four original Markdown two-space hard breaks (lines3–6); standard whitespace
check reports those as trailing spaces. Preserve the owner-supplied bytes and
check that one file with blank-at-eol disabled; all other whitespace rules and
all other files retain normal checks. This is a document-format exception,
not a code/test failure or a changed specification; all14 mandatory worktree documents
exist and the new plan/checklist relative links resolve. All25 original
canonical untracked file hashes match the before inventory. No application,
backend, contract or workflow changes are present. No0.12 GitHub issue/milestone
was created in this planning-only session; their creation/reuse is queued at
approval and no implementation status was falsely published.

created by stokonn with love <3
