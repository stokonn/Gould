> Current native-gate checkpoint — 2026-10-29: corrections02c5298 have required automatic CI3/3 PASS and API26 delta3/3 PASS (media/focus, font metrics, compact marker; all28 tables/37sessions/1draft/4WAV hashes exact). Full API36 run40931469588 failed one Discover test: canonical269 cases/255pass/1failure/13explicit skips, while debug-rebuilt0.10.1 upgrade acceptance passed. Retained frame proved the helper searched only forward after virtualized evidence overscroll. Test-only boundary reversal now passes the isolated full Discover journey1/1,151.288s on disposableAPI36 emulator5590; date/license/assertion checks unchanged, screenshots inspected, before/after2 assertions exact. Production/debugAPK unchanged614521a6…. Corrected full CI remains pending; no release/main/tag. Retained matrix-v3 data-storage files actually show Settings and are invalid for destination visual acceptance. [Evidence and remaining gates](SAMSUNG-BLOCKERS.md). Earlier checkpoints below are history.

> Current Samsung-correction checkpoint — 2026-10-29: three owner-reported Stable blockers implemented and targeted emulator acceptance passed: metronome shared media audio/focus (native1/1 and actual unmuted ST3 track at sound effects0/Stop releases focus), bundled licensed fixed-advance Tux (unchanged ASCII/signature; metric1/1; actual Android font2/About and corrected both-theme probe1/1), Library heading/Previous same row with subtitle below (six-case responsive/touch/action1/1 and real route). Frozen381-input host PASS393JVM/48suites/0failures/errors/skips, debug/testAPK, lint0errors165warnings1hint,3m25s. All28 DB tables/3 WAV hashes exact, widgets retained, emulator font1/sound effects1 restored. No Samsung/phone agent verification claimed. Source base05a92c8 with current verified corrections; commit/CI/compatibility and remaining mandatory release gates OPEN. [Correction evidence](SAMSUNG-BLOCKERS.md) · [Source-bound report](verification/samsung-blockers.json). Earlier pending/failed test-fixture notes below are preserved history. No release/main/tag.


## Owner-reported Samsung Stable blockers — 2026-10-29

Confirmed by owner testing on Samsung; no agent physical-device verification.
These are mandatory0.12 Stable blockers within the existing single Phase2 stage.
The accidental pasted terminal transcript introduced no instructions. Widget
checkpoint05a92c8 is pushed; Issue41 comment6503995190, canonical PMP snapshot51
61258e08-3801-47a8-b3df-77be56839ab3 (check valid/no errors/warnings), Project In Progress.

- [x] Metronome: replace system-sonification usage with media-volume playback;
  preserve focus, explicit start/stop, route/lifecycle handling. Verify system
  sounds muted/media audible routing, stop and focus loss with targeted native
  evidence. Do not equate emulator audio routing with Samsung acoustic testing.
- [x] Tux: inspect actual before rendering, whitespace and font metrics; isolate
  from OEM font substitution/variable advances. Preserve original ASCII/pink
  character and exact signature `created by stokonn with love <3`. Verify actual
  normal/large-font emulator renders and glyph advance/line alignment. Existing
  system Monospace styling is owner-confirmed insufficient on Samsung.
- [x] Library: align Previous horizontally with main heading Vaše sbírky; keep
  Vaše hudba below as subtitle. Verify EN/CZ, normal/large fonts and constrained
  widths, touch target, navigation and no clipping/overlap.
- [ ] Synchronize actual results/source/evidence with Issue41/PMP/Project/handoff;
  retain remaining mandatory gates. Stable remains blocked until all pass.

created by stokonn with love <3

> Current launcher acceptance continuation — 2026-10-29: resumed from50e32ed without repeating pristine WidgetTest8/8 or unchanged13 calendar/projection tests. Actual API36 launcher resize/independent instances/EN-CZ/light-dark/font1–2/offline and protected-data checks executed; found and fixed a clipped200% compact stale marker. Targeted debug/test build PASS37s, new native regression1/1 and actual-host fixed stale/unavailable/accessibility probe1/1 PASS, lint0errors/165warnings/1hint PASS4m25s, localization1211/policy/APK/trusted signer PASS. Exact draft/3WAVs/recording rows/history/badge rows retained; original widget IDs2/3 and added5 preserved. First offline boot Pixel Launcher ANR is retained; corrected settled repeat has no ANR and both original widgets render live. Final readback: ALL28 tables exact,3 WAV hashes exact, integrity/FK PASS; original system-following preferences/font1 restored. Residual accent/spec-cell review and remaining release gates are OPEN. [Current acceptance checklist](WIDGET-LAUNCHER-ACCEPTANCE.md) · [Source-bound evidence](verification/widget-launcher-current.json). Earlier checkpoint counts/statuses below are historical; no main/release/tag or phone operation.

# Gould0.12 — current single-stage implementation checkpoint

> **Resumed verification:** DNS/HTTPS restored,5de87c6 pushed, Issue41 comment6499082257 and canonical PMP snapshot45 synchronized. Preserved Previous patch now compiles;393JVM PASS, final debug/test build and lint0errors/165warnings PASS after correcting one genuine Compose initializer lint error without suppression. Actual affected API36 native31/31 PASS; trusted debug APK13e1fd55… package/version/ZIP/artwork/masks PASS. Actual permission/Previous UI and remaining finite widget/layout/final release gates IN PROGRESS. Effective emulator RAM correction is recorded in controlled-integrity-api36.json (both earlier boots2560MB; CPU/boot-idle changed). Earlier infrastructure-blocked notices below are history.

> **Current infrastructure recovery (superseded — history):** the earlier notice that5de87c6 was unpushed and the Previous patch uncompiled is resolved; see the Resumed verification notice above and [RECOVERY-2026-10-29.md](RECOVERY-2026-10-29.md). Stable remains blocked only by the still-open mandatory gates.

## Previous live-take visibility correction and actual UI verification — 2026-10-29

The Previous availability/export-safety correction was verified and committed at `8ccdd0e` (393 JVM/48suites, lint follow-up0errors/165warnings after one genuine Compose initializer fix, affected API36 native31/31, APK13e1fd55… quality/trusted PASS). A genuine follow-up gap was then found during actual-UI driving: `StudioDao.audioPage` filtered `state IN ('Saved','Recoverable','Missing','Deleting')`, excluding Preparing/Recording/Finalizing, so a live take never appeared in Previous and the new `studio_audio_in_progress` branch was unreachable — the exact reason the prior agent's real-emulator driver failed on that assertion.

Corrected `audioPage` to `state != 'Deleted'` (keyset paging and LIMIT unchanged; history-paging check PASS) and added `StudioRecordingTest.activeTakeAppearsInPagedRecordingsUntilExplicitDeletion`. Affected native suite **32/32 PASS** on emulator-5586/API36/qemu=1 (StudioRecordingTest7, StudioControlsTest, RecordingLifecycleTest, StudioBackupTest). Localization1211/policy/diff-check PASS. JVM393 remains source-bound to8ccdd0e (the @Query change is Room-compiled/native-only, no JVM surface).

Actual real-emulator Previous UI verified with a real microphone recording (driver `previous_ui_livesaved.py`, evidence [previous-live-saved-ui.json](verification/previous-live-saved-ui.json)): the live take renders the in-progress message with Export/Delete **disabled**; a leftover interrupted take on the same screen shows its message with Export/Delete **enabled** and a Recover action; after Stop, the newly saved take shows Play/Export-original/Delete **all enabled**. This closes the "actual Previous live→saved transitions and disabled actions" gate. WidgetTest run on this controlled-probe AVD failed2 empty-fixture cases due to a protected `upgrade-fixture-draft` practice draft; widgets belong on the clean gould012-profile AVD and are unrelated to `audioPage`.

Next: finite widget/layout/font/theme/locale matrix on the correct AVD, remaining security/licensing/performance, required CI on8ccdd0e plus this fix, and final clean trusted non-debuggable Stable artifact/upgrade gates. Stable remains blocked until all mandatory gates genuinely PASS; one Phase2 stage stays IN PROGRESS.

## WidgetTest fixture diagnosis — gate OPEN, NOT passed — 2026-10-29

Ran WidgetTest on the clean gould012-profile AVD (emulator-5582) and the controlled-probe AVD (emulator-5586): both `Tests run:8, Failures:2`. The 6 non-fixture cases pass (cache/corruption, real RemoteViews across sizes/themes/fonts, provider/immutable-intent identity, bounded Room week projection). The 2 failures — `readyWidgetTapDoesNotCreateSessionDraftAudioOrStartCapture` and `activeAndPausedWidgetTapsPreserveTheSamePracticeWithoutResumeOrDuplicateStart` — trip their PRECONDITION `check(token.isBlank() && !running)`, not a widget assertion.

**RESOLVED (same day): WidgetTest 8/8 PASS on a fresh pristine API36 AVD** (`gould012-pristine`, `emulator-5590`, qemu=1) — three full-class runs `OK (8 tests)` plus each formerly blocked case individually OK(1). Source analysis confirmed the profile-AVD failures were fixture-only: `PracticeEngine.begin` reuses a non-blank token (`state.token.ifEmpty{token}`), so `activeAndPaused`'s `start(false)` on the profile AVD would resume the protected 7.8s practice and its `finally { pause(); reset() }` would destroy the draft — that blank-token precondition is load-bearing and remains unchanged. `readyWidgetTap` accepts a recovered non-running fixture (`f2757dc`, verified7/8 on profile AVD). The protected interrupted draft was not touched; profile emulator-5582 was stopped cleanly with data on disk. Evidence: `.gradle/gould012-checks/widget-pristine-5590*.log`. The widget **unit** cases are done; the actual launcher widget matrix (add/resize/tap/offline/multiple-instances/Mon–Sun/rollover/font/theme/locale/accessibility + changed-layout) remains OPEN.

Definitive diagnosis (temporary instrumentation probe, since removed): the AVD carries the checkpoint-protected **7.805s interrupted practice draft**, which the coordinator correctly recovers on cold start into `token=[68c2cb60-…] running=false ready=true status=Interrupted elapsed=7805`. `practice_drafts` count is 0 — the interrupted practice is persisted by the practice recovery repository, not the draft table, so clearing the draft table (done for unrelated synthetic residue, backed up) did not and must not remove it. This is **correct recovery behavior + intentional preserved fixture, NOT an app defect** and unrelated to the `audioPage` change (widgets don't use it). These 2 cases need a pristine no-history AVD or a precondition that accepts a recovered Interrupted (non-running) practice while still asserting the real invariant (widget taps must not create sessions/drafts/audio/capture nor resume/duplicate a paused practice). **Widget gate remains OPEN/blocked-by-fixture; do not mark passed and do not discard the protected interrupted draft.** Emulator-5582 (3 Saved recordings preserved) and emulator-5586 both retained; the synthetic zero-value draft residue on 5582 was backed up and cleared. Full handoff: [RECOVERY-2026-10-29-B.md](RECOVERY-2026-10-29-B.md).

## Verified integrity and bounded accessibility checkpoint — 2026-10-29

Ordinary fullAPI36 CI40871204814 remains251PASS/0failures/errors/11 explicit opt-in skips. The2 bounded-public-response/portrait cases separately PASS on API26. The existing controlled preservation sequence now completes **12 JUnit PASS plus6 deliberately verified process terminations**, with2 actual reboots, offline queued work/blob preservation, migration transaction rollback/retry to11, exact manual-credit recovery, idempotent completion, restore interruption at four boundaries and checksum/restore retry. **All14 original critical typed tables and37 sessions remain unchanged.** The9 probe-dependent methods have been exercised separately; deliberate kills are not JUnit passes. [Evidence](verification/controlled-integrity-api36.json).

Retained failure: immediate post-boot instrumentation on the1536MB/two-core AVD hit a startup ANR. The trace main thread was idle with about24s scheduling wait amid boot low-memory pressure. The same recovery code PASS after preserving/restarting the AVD at2048MB/four cores and waiting for all boot broadcasts idle; new exit-info has no ANR. No app timeout was weakened and the original failure/trace remain retained. Final production-artifact startup remains required.

ActualAPI26 same-state SAF audio ZIP restore passed13 critical table comparisons plus historic badge preservation and all4 WAV hashes, with exact original3s draft/37sessions. Genuine decoded playback produced the Critical Listener notice; View Badge opened its permanent-earned detail. [Audio evidence](verification/actual-audio-ui-recovery-api26.json).

Live official-source TalkBack on API36 was bound with touch exploration and not suppressed by the read-only real-Activity recorder. Raw emulator gestures moved actual focus, real double-tap opened recording consent, Cancel returned Studio, and Profile/Settings focus events/screenshots were retained. Existing full native32-configuration keyboard/focus/action cases pass. Exploratory injected/guessed shortcut attempts did not establish navigation and are not passes. No audible speech fidelity claim. **Owner steering: stop prolonged exploratory TalkBack; prioritize mandatory functional/data/release checks.** The recorder stopped and previous no-service setting was restored. Remaining meaningful layout/touch/IME/Back/widget semantics are part of finite final functional QA. [Bounded evidence](verification/live-accessibility-api36.json).

Next: correct observed Previous state presentation for metadata-only/missing and live recordings, verify those actual flows, complete launcher/resize/offline/week/instances and finite full changed-layout matrix, then remaining security/licensing/performance/clean trusted non-debuggable Stable/upgrade gates. Current ordinary app source remains1d06f08; no Stable build/main/tag/release. Preserve all15 private references and all AVD data. No physical phone/API37.


## Current verification checkpoint — 2026-10-29, source2c5420c

- **Required automatic CI3/3 PASS**, and [full API36 native + upgrade run40871204814 PASS](https://github.com/stokonn/Gould/actions/runs/40871204814). Canonical XML totals:262 cases,251 passed,0 failures/errors,11 fixture/probe-dependent skips. Two separately acquired provider/portrait cases and nine opt-in synthetic migration/restore/process-death/reboot probes still need separate applicable execution; skips are not passes. The preceding failed run remains preserved.
- Actual API26 visible recording consent → capture → intentional app force-stop → cold start produced a recoverable take with no restarted capture service. The original3s draft payload/updatedAt and37 sessions remained exact. Android SAF export of interrupted bytes is byte-identical; explicit recovery retains all661504 PCM samples at22050Hz,1323052-byte WAV SHA256 `816cb1c3761994e4c323e4e28d92f921b351553f629dc793a3409d00c316927f`. Actual player pause remains3s across observations, seek moves to21s and resume advances23s. Saved WAV SAF export is byte-identical. Emulator input only; no acoustic piano fidelity claim.
- Actual SAF format6/schema11 JSON and journal/audio ZIP validate; all4 actual audio entries match manifest sizes/hashes and recovered PCM. Same-state restore UI validation is in progress. Evidence: [audio UI](verification/actual-audio-ui-recovery-api26.json), [native CI](verification/full-native-fixture-followup.json).
- EN/CZ, Light/Dark,320/360/412dp and1/1.5/2 font extreme captures exist; only inspected scenes count as review. Full meaningful visual/accessibility/widget matrix remains open. The temporary driver now excludes decorative headings and derives scroll gestures from the actual viewport. Earlier Data & Storage captures showing Settings root are invalid for backup-page acceptance and are retained as diagnostics. Official open-source Google TalkBack QA-tool build is in progress with isolated throwaway signing; no spoken/TalkBack PASS inferred.
- English Wiki sources now describe implemented0.12 behavior and format6/schema11 while clearly retaining0.11 as latest published Stable. No Wiki publication or Stable claim is implied. App inputs/debug APK remain unchanged from1d06f08,393JVM host and affected API26 evidence retained. Owner unattended conditional publication/visual waiver remain applicable.

**Phase2 remains IN PROGRESS.** Complete actual UI/widgets/editor/queue/IME/TalkBack, remaining applicable synthetic/backup/recording/privacy/security/licensing/performance, then final clean trusted non-debuggable Stable artifact/upgrade gates. No main merge/tag/release build/publication. API36 + API26 only; no physical phone. Preserve all15 private references and original drafts/recordings.

Historical chronological checkpoints below retain their original source-bound scope; this current dashboard supersedes their pending implementation descriptions.


Branch: feature/gould-0.12; planning ancestor29bb4d1, canonical main ef82898.
Issue41 / milestone5 / Goldberg Development InProgress, High, Feature, Version0.12.
Owner approved full scope and icon15, plus conditional main/Stable authorization;
all mandatory gates and actual visual/product acceptance remain required.

Implemented foundation (not complete product functionality):
- Additive Room10→11 local-audio metadata/associations, idempotent event and badge
  notice tables. Original URL recordings, profile and journal tables unchanged.
  Migration marks existing awards already seen; no historical popup flood.
- Pure achievement rules/regressions for SHA-based recording identity, legacy
  Goldberg pre1.0 eligibility (no invented cutoff), three actually credited pieces
  and verified playback threshold. NOT YET wired into achievement registry/events.
- Streaming DurableWav staging, silence/signed-sample preservation, explicit crash
  recovery and atomic no-clobber hard-link publication. It is NOT YET connected
  to microphone/service/UI. Final recording format/codec decision remains subject
  to native/performance verification; AAC preference has not been claimed tested.
- New icon production construction, adaptive/themed layers and five legacy
  densities. Host circle/rounded-square/squircle preview inspected and safe-zone
  alpha checked; native launcher sizes/masks/UI acceptance NOT EXECUTED.
- Original generated nature landscape copied locally for future quote-card
  integration; no mockup/reference pixels consumed, no UI wiring yet.

Verification so far: initial JVM+instrumentation compilation passed353cases;
latest source-bound foundation JVM358cases/0failures/errors/skips. Debug/test APK
build and lint gate PASS:0errors/132warnings; all390source inputs unchanged.
Report verification/foundation-host.json; raw logs/source manifest/XML preserved
privately in .gradle/gould012-checks. Native test execution remains pending. Host migration DDL1→11 through10→11/schema matching/original
rows/notice backfill passed all10 paths; agent policy and whitespace checks pass.
Original schema10 copied as native migration fixture; schema11 exported by Room.

Native instrumentation, emulator renders/launcher/widget, actual microphone,
backup v6/media archive and interrupted restore, trusted upgrade/signing and
release acceptance remain PENDING. No release/main merge/tag/publication.
Historical custody worktrees remain protected; no administration rerun.

PMP: canonical documented CLI is authorized reporting interface. Pre-transition
snapshot preserves unresolved maintenance/Phase1. task archive/activate/set used
supported interfaces; active task gould-0.12-complete, implementing/active. Old
history/UUID/private stores preserved. Feature-worktree state was not fabricated;
its config remains private. Bind next verified checkpoint to actual feature SHA
and source manifest, never canonical-main native-test applicability.

Next: finish current host gate and source/hash readback, commit/push checkpoint,
sync issue description+comment and canonical PMP; then implement v6 metadata/media
backup with preservation and LocalRecordingRepository/shared capture lifecycle,
followed by remaining approved UI/achievements/widgets. Update target-schema native
assertions from10to11 while retaining original fixture/data assertions. No extra
stage or approval phase, no physical phone or unchanged crashing AVD retry.

created by stokonn with love <3

Internal APK: app/build/outputs/apk/debug/app-debug.apk,0.12-dev/code14,
SHA25656f7825c9b14acdc43529ce1f976010af063e29a13ff96a2e6894f637b098c68.
Trusted signer53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e.
ZIP/private-reference exclusion passed. This foundation APK is not an owner
acceptance candidate or Stable release; most approved feature wiring is pending.

## Current continuation after published foundation5c32259

Uncommitted continuation is preserved in the feature worktree:
LocalRecordingRepository plus4 native filesystem/repository cases compile;
StudioBackup v6 metadata section and backward-compatible v1–v5 import compile.
Journal restore merges (never deletes) destination recordings/events, preserves
permanent awards and labels imported excluded audio Missing. Added4 native backup
cases for explicit exclusions, preserved destination audio, repeated-import event
dedup, v5 compatibility and transaction interruption (not yet executed). English/
Czech journal-only disclosure added before export and to success notice; format
copy updated6 with old resource keys preserved. Native target-version assertions
changed10→11 without changing historical fixture/data-preservation assertions.
The streaming media archive (RecordingArchive + MediaBackupRepository) with
staged hash/size/path-traversal validation, duplicate-entry rejection, no-clobber
hard-link publication before journal replacement and replayable receipt is
implemented; the previously flagged Missing-row promotion on verified media
restore is present in StudioBackup.merge.

### Verified host gate on this continuation (2026-10-28, post-Codex-limit resumption)

Full host command PASSED: clean assembleDebug testDebugUnitTest
assembleDebugAndroidTest lintDebug (--no-daemon --max-workers=1).
JVM:42 suites/363 tests/0 failures/0 errors/0 skipped (358 foundation +
5 new RecordingArchiveTest cases, freshly rerun with --rerun-tasks). Instrumentation
COMPILED ONLY (not executed). Lint0 errors/145 warnings (foundation baseline132;
delta is UseKtx/UsableSpace/ModifierParameter style classes from new sources).
Policy/migration/backup-fixture/localization/paging checks all PASS. Debug APK
com.opus.piano0.12-dev/code14, SHA256 b5ea9e1def70ea1b1b22988cfa31efc7e9f9494370de5442336685e86eafbc43,
signer53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e verified
against the trusted certificate. Source manifest(302 app inputs)
037cacf9f7fa5ca395a804b30b25642c4bd704fe148a1adf1e61737613d4c38e. Evidence:
verification/backup-v6-host.json; raw logs privately in .gradle/gould012-checks.
This verifies the Backup v6 + recording repository + media archive host surface
only; native StudioRecordingTest/StudioBackupTest execution remains PENDING.

### Shared capture/recording lifecycle (ST-01–03) — implemented, host-verified 2026-10-28

RecordingController (new): application-scoped state machine Idle → Preparing →
Recording → Finalizing → Saved (+ Recoverable). Durable identity (DB row +
staging file) precedes any sample via LocalRecordingRepository.prepare().
WriterLock serializes append/finish races between capture loop and stop().
Sink (AtomicReference) is read atomically each capture-loop iteration.
selectRate() probes the same rates as MicrophoneController (22050/44100/16000).
reconcile() delegates to repository for startup metadata-only recovery.
No auto-start on recovery, reboot or process death.

MicrophoneController: extended with recordingSink (AtomicReference of
(ShortArray,Int)->Unit), detectionEnabled flag and preferredRate. One AudioRecord;
detection and recording are separate consumers of the same PCM stream. Recording
sink fed first each iteration; detection runs only when enabled. No second capture.

SmartPracticeService: dual-action (START / START_RECORDING). Single capture loop;
combine(practice.running, recording.phase==Recording) watcher keeps service alive
while either is active. releaseCapture stops mic only when recording is not also
consuming it. Recording-only mode skips practice.startSmart(). Notification title
reflects recording state. START_RECORDING companion method added.

OpusApplication: recording lazy property wired to LocalRecordingRepository
(filesDir/recordings). Startup reconcile() called (metadata only, never capture).

OpusViewModel: recordingState flow, startRecording()/stopRecording() commands.
AppContext passed as constructor parameter (ViewModel, not AndroidViewModel).

PracticeScreen: Studio header (L.fixed("Studio")), Record audio / Stop recording
button in bottom bar, recording consent dialog (separate from Smart consent),
red recording dot indicator, Finish stops recording before review (300ms delay
for finalize). permissionForRecording flag routes permission result correctly.

Destination enum: PRACTICE label changed "Practice" → "Studio" (route unchanged).

L.kt: added Studio, Recording, Preparing, Finalizing, Previous fixed labels.

Strings EN/CZ: 14 new resources (label_studio, msg_record_audio, msg_stop_recording,
msg_recording_saved, msg_recording_interrupted, msg_recording_title,
msg_studio_recording, msg_recording_consent, label_recording, label_preparing,
label_finalizing, msg_record_error, label_previous, msg_recordings).

Host verification: compileDebugKotlin BUILD SUCCESSFUL. JVM tests 42 suites /
363 tests / 0 failures / 0 errors / 0 skipped. Lint 0 errors / 147 warnings
(2 new from new code). Localization check PASS (1118 resources).
Agent policy check PASS. git diff --check PASS.
Native instrumentation and emulator execution NOT performed in this batch.

### Previous screen (LB-01) — implemented, host-verified, committed d937cdc 2026-10-28

PreviousScreen (new): two tabs — Practice History (bounded keyset paging,
reuses history DAO) and Recordings (newest-first, MediaPlayer play/pause with
lifecycle disposal, SAF CreateDocument export, explicit delete confirmation,
Recoverable state shown honestly). "previous" route in OpusApp NavHost;
selectedTab maps to REPERTOIRE. Studio header history icon + Library discreet
Previous entry. Host gate: 363 JVM tests pass, lint 0 errors/146 warnings,
localization PASS (1118 resources).

### Achievements (AC-01–03) — implemented, host-verified 2026-10-28

Achievements.kt: 3 new MedalArt (MICROPHONE/PEN/EAR), 5 new metrics
(RECORDING_SAVED_COUNT, SESSION_NOTES_COUNT, VERSATILE_SESSIONS, PLAYBACK_COUNT,
FOUNDING_PRACTICE), 6 new definitions (first_take, recording_artist,
versatile_pianist, musical_journal, critical_listener, founding_pianist),
distinctNotes() helper, facts() extended with studioEvents + foundingVerifiedAt
(defaulted; backward compatible).

AchievementStore: studio events (db.studioDao().recordings()/events()) merged
into badges flow and evaluateInTransaction; newly earned awards enqueue a
durable BadgeNotice (acknowledgedAt=0) for the unlock popup; migration
backfills historical unlocks as already-acknowledged. FoundingPolicy.
stableOneCutoffMillis stays null until a real 1.0 release.

BadgeLocalization + EN/CZ strings: 6 new badge entries (24 strings) +
msg_view_badge/msg_dismiss_11a3.

MedalArtwork: MICROPHONE (capsule + grille + stand), PEN (nib + ink),
EAR (ear curve + sound arcs); metric-aware inscription (REC/NOTE/LISTEN/
1.0·FOUNDING).

OpusApp: durable unlock popup replaces the old Snackbar unlock notification.
One themed AlertDialog per new award (medal icon, rarity, description,
View Badge → BADGES route / Dismiss). Defers while practice or recording is
active; acknowledges via studioDao.acknowledge() on dismiss/view.

AchievementEngineTest.allDefinedMilestonesCanBeEarnedFromRealFacts updated:
studio-event fixtures with 64-hex subjects via StudioAchievementRules.recordingKey(),
foundingVerifiedAt=3e12; studio badges verified individually (first_take,
critical_listener, founding_pianist earned; recording_artist/versatile_pianist/
musical_journal correctly not earned from the base fixture).

Host verification (this batch): compile OK, JVM tests 42 suites / 363 tests /
0 failures / 0 errors / 0 skipped, lint 0 errors / 147 warnings,
localization PASS (1144 EN/CZ resources), agent policy PASS, git diff --check
PASS. Native instrumentation and emulator execution NOT performed.

Next: Profile 2×2 centered statistics + era autocomplete (distinct local
catalogue eras, accent-insensitive) + SavedStateHandle edit draft with
Save/Discard/Keep-editing guards (PR-01–02); both widgets (WG-01–04);
Tux mono TextStyle fix + Home quote licensed image + scrim; metronome/queue-Undo
(ST-03); then native/emulator acceptance. None complete yet.

Canonical PMP active task gould-0.12-complete, supported CLI (missing feature-state
not fabricated). Previous main maintenance task was explicitly archived through
CLI after preserving a snapshot; maintenance remains incomplete/private originals
protected. Feature foundation checkpoint sources actual5c32259+manifest, not main.


### Codex resumption / Profile and native recording blocker — 2026-10-28

Actual inherited HEAD `4f4b256`, branch `feature/gould-0.12`; intentional
OpenCode HANDOFF/STATE edits preserved and pushed as `096c9d3`. Read canonical
PMP via supported CLI (task gould-0.12-complete, sequence 34), Issue #41 dashboard
and all three comments. Dashboard synchronized to actual inherited milestones.
All 15 private references match SHA-256/ignore rules; no protected data changed.
Inherited reports checked: 42 suites/363 tests/0 failures/errors/skips and lint
0 errors/147 warnings. These are inspected earlier host reports.

PR-01–02 sources implemented: centered 2×2 row-equal statistics (one column for
constrained widths/200% fonts), local accent-insensitive distinct-era suggestions,
SavedStateHandle original/current draft, private staged photo, Save/Discard/Keep
editing, atomic complete profile commit, three-way conflict protection and locked
manual identity override intent. Eight new JVM cases and three native editor cases.
Profile host gate: 43 suites/371 tests/0 failures/errors/skips; debug APK and
instrumentation compile, lint task BUILD SUCCESSFUL. Two inherited obsolete
OpusViewModel instrumentation constructors first failed and were corrected.

Disposable local API36 AVD resides only under ignored `.gradle/profile-avds`,
serial emulator-5582, ro.kernel.qemu=1 verified. Initial SwiftShader boot attempts
segfaulted; alternate renderer eventually booted. An initial native attempt was
killed by Android startup ANR during heavily contended first-boot verification;
raw logs retained. Subsequent targeted native execution completed 11 cases:
**3 ProfileEditorTest PASS; 8 StudioRecordingTest/StudioBackupTest FAIL**, all at
`Files.createLink` in DurableWav.finish with AccessDeniedException. This is a real
Android recording blocker; host success did not establish native acceptance.

Correction underway before widgets: replace both recording/restore hard links
with shared per-directory/cross-process locked atomic rename; retain archive
originals, fail closed on collisions. Add four host publication regressions and
two real Android media-archive collision/interruption/retry tests. Rebuild and
native rerun pending. Sources are not release-ready; do not mark audio/backup or
full Profile rendered acceptance complete. Full mandatory scope remains open.
Private raw evidence: `.gradle/gould012-checks/profile-*`.

Also corrected malformed inherited backup-v6-host.json signature syntax without
changing any evidence claim; `created by stokonn with love <3` is retained exactly.


### Corrected host/native gate — 2026-10-29 (local)

RecordingPublication now serializes all recording-name publishers (writer and
media restore) using one private persistent directory lock plus FileChannel lock,
checks collisions inside that guard and performs same-directory atomic rename.
Restore copies/syncs/hash-verifies a temporary duplicate and retains archive
staging. No hard links remain in either path; no existing destination is replaced.
Added four host publication cases and two Android archive collision/retry cases.

Final gate `./gradlew assembleDebug testDebugUnitTest assembleDebugAndroidTest
lintDebug --offline --no-daemon --max-workers=1` PASSED: **44 suites/375 JVM tests/
0 failures/errors/skips; lint 0 errors/153 warnings; debug/test APK built**.
**13 actual native tests PASS** on emulator-5582/API36/qemu=1 (Profile3,
recording4, backup/media6; 4.123 seconds). This includes original-byte recovery,
recording dedup/deletion/integrity, journal v5/v6 and new streaming archive
interruption/publication collision checks. It is not full microphone/service,
player/UI/export/migration/upgrade/widget acceptance.

All 336 runtime/test/build inputs match the before manifest after these gates;
manifest SHA256 `46589d6f56e39784dca5c41e93c6887676fa0e6c40ee99dc716f2322914203eb`.
Debug APK `app/build/outputs/apk/debug/app-debug.apk`, package com.opus.piano,
0.12-dev/code14, SHA256
`f9665dc7c78c30a8657a116a117b7021da28f314ea075a2510ce0c4491e1fba2`;
apksigner verifies trusted `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`.
APK ZIP contains no private references/PMP. Policy/localization1151, host migration
DDL1–10→11, historic backup fixtures and history paging checks PASS. Full clean
release/production/security/seeded upgrade gates remain open.

Actual Ivory Profile and editor renders inspected locally. Profile shows centered
2×2 stats with avatar beside name. Dirty Back displayed Save/Discard/Keep editing;
real background process-death/recreation and wider theme/font acceptance in progress.
[Machine-readable evidence](verification/profile-publication-host-native.json).
Next commit this verified source/checkpoint, synchronize Issue #41/PMP, continue
both required widgets. No release/main integration or owner visual approval.


### PR-02 real process-death gap — correction in progress, 2026-10-29

Real UI test: typed synthetic MusicianDraft, Back showed all three choices and
Keep editing retained it. `am kill` did not kill PID5754 (HOT/same-PID readback),
so was NOT counted as process-death verification. After backgrounding and killing
only that disposable app PID, cold launch PID6199 did not restore the editor.
SavedStateHandle-only persistence is insufficient for this tested task/restart
path. **PR-02 remains open**; the previous host/native13 gate does not prove this
acceptance item.

Correction uses a private AtomicFile editor-v1.json alongside SavedStateHandle,
o database schema change. Off-main serialized generation-checked writes retain
small original/current fields and staged-photo identity, never avatar bitmap bytes.
Save/discard await draft-file clear; a saved profile with failed draft cleanup is
reported explicitly and can retry idempotently. Unreadable original draft fails
closed, offers Retry/unchanged SAF export and is never silently replaced. Added
native cold-task-without-Bundle and corrupt-draft-preservation regressions.
Rebuild `.gradle/gould012-checks/profile-durable-host.log` and native/real cold
restart retest pending; no acceptance promotion yet.


Additional rendered evidence: actual Profile at Nocturne/Czech/font2.0 inspected,
with single-column centered statistic blocks, full labels and retained avatar/name
layout. Navigation wraps long Czech labels at 2× as in the existing app; this is
not owner visual sign-off. No application crash in the inspected emulator crash
buffer. Captures are private ignored `.gradle/gould012-checks/profile-cs-2x.png`
and profile-ivory-en.png, plus inspected editor/dirty guard captures. The first
cold-death path lost only synthetic test edits, never owner data.
PMP canonical snapshot35 (`202e9be2-c4b0-4848-800d-b256347740d5`) and Issue #41
comment6483046606 record the corrected native recording gate and open draft gap.


### Final durable Profile verification PASSED — 2026-10-29

Private AtomicFile fallback resolves the reproduced cold-task loss. All15 native
cases PASS (Profile5/recording4/backup6;2.948s). Actual UI test: MusicianKept was
persisted, app backgrounded and only its emulator PID6650 killed; COLD restart
PID6858 restored the editor/name and byte-identical draft. Dirty Back offered all
three choices; confirmed Discard cleared only this synthetic draft file. Before
this correction the same real process-death path lost the synthetic draft.

Final host gate BUILD SUCCESSFUL:375 JVM/0failures/errors/skips,44suites;
assembleDebug/assembleDebugAndroidTest/lintDebug; lint0errors/153warnings;
localization1157/policy/hostDDL/backup-fixture/paging/whitespace PASS. All336 source
inputs unchanged, manifest SHA256
`fba671866ebd9cf259eea85a7e64c0a44abe3a3eff293243ef7db63f7c6b599f`.
Final debug APK SHA256
`d851e54cff4526c0c57b10e3ee74f1edb3fdc5d4ad86b0d04fb510ac4726e12c`,
com.opus.piano0.12-dev/code14, trusted signer53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e verified. No private references/PMP in ZIP.
[Final evidence](verification/profile-durable-host-native.json). Intermediate
13-case evidence remains historical; final15-case evidence governs this source.

Remaining: full Profile theme/size/font/avatar/autocomplete/export interactions
and owner visual approval; both widgets next, then Tux/quote and remaining Studio
metronome/queue/Undo/notes; full real capture/player/backup/migration/seeded upgrade,
security/signing/performance/release gates. Main/releases unchanged. One Phase2
stage remains IN PROGRESS; no source-only/host-only acceptance promotion.


### Active widget batch and efficient continuation — 2026-10-29

Verified/pushed base80650b1a054533873610d712765fd8161bf93338. Working-tree changes
implement both required providers: widgets/WidgetProjection, WidgetSnapshot,
WidgetRenderer, WidgetRefresh and PracticeWidgets; XML layouts/provider metadata,
immutable explicit per-instance Studio PendingIntents, coordinator-ready navigation,
transactional week-bounded PracticeDao query, semantic journalColors shared with
Compose, AppearanceStore.current, application observers and appCategory=audio.
Installed SDK36 and official Android docs have no appCategory education enum;
no unsupported manifest flag or storefront update is used. Week values are
credited session rows, stored-day semantics retained, never expanded parts,
elapsed or audio time. Missing/invalid cache shows unavailable, stale cache keeps
its verified week/date. No new dependency, schema migration or backend networking.

New tests: WidgetProjectionTest13 host cases; WidgetTest7 native cases (cache,
strict corruption/zero distinction, actual RemoteViews inflation across sizes/
themes/fonts, provider/immutable intent identities, bounded Room projection and
ready/active/paused zero-auto-start taps). These are source additions, not yet
claimed executed. Initial widget compile/build/test-APK gate passed before final
regression additions. Full meaningful-checkpoint host gate now running:
`.gradle/gould012-checks/widgets-host.log`, tool session65968, command
`./gradlew assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug
--offline --no-daemon --max-workers=1`. Frozen348-input manifest:
`.gradle/gould012-checks/widgets-source-before.json`. Read completion/failures;
do not start competing Gradle processes. Native WidgetTest7, launcher add/resize/
tap/rollover/stale/locale/theme/font/reboot and owner visual approval are pending.

Host emulator remains disposable API36/qemu=1 **emulator-5582**, AVDgould012-profile
under ignored `.gradle/profile-avds`; it booted with `-gpu lavapipe -feature -Vulkan`,
1.5GB RAM,2cores, no snapshot/audio/window. Earlier SwiftShader boots segfaulted;
logs are retained. ADB must always explicitly use this identified emulator serial;
no physical phone. Current synthetic app is idle with no practice draft after
Profile confirmed discard. Do not rerun the old Profile typing helper blindly.
`adb shell cmd appwidget help` reports no shell implementation; actual launcher
widget binding must use supported UI/native host tooling, not invented commands.

Owner efficiency steering: implement coherent batches. During development use
`:app:compileDebugKotlin`, targeted `testDebugUnitTest --tests ...` and focused
native classes as needed. Use complete host gates at meaningful checkpoints;
retain all high-risk recording/backup/migration/data-integrity regression tests
and the full final native/emulator/security/migration/UI/release acceptance matrix.
Do not reread unchanged approved architecture or duplicate unchanged-source gates.
Preserve one Phase2 stage, routine checkpoint commits/pushes/PMP/Issue41 continuity.
Latest verified source80650b1 retains375JVM/15native/trusted APK evidence; widget
work is uncommitted and unaccepted. Issue41 checkpoint6483220816 and canonical
PMP feature binding preserve that baseline; update them at the widget milestone.
Next: inspect current host completion; fix genuine failures with targeted checks;
run WidgetTest and actual launcher verification; checkpoint verified widget source,
then Tux/quote and remaining Studio/metronome/queue/Undo/notes/full acceptance.


Widget checkpoint progress (same frozen348 inputs): JVM45suites/388tests/
0failures/errors/skips; debug/testAPK compilation PASS. Focused actual API36
WidgetTest **7/7 PASS** (28.564s): strict disposable cache/privacy/zero distinction,
real RemoteViews apply for both kinds across small/large/light/dark/font1/2,
provider registration and immutable per-instance activity identities, bounded Room
week projection, ready/no-row/no-capture tap and active/paused preservation.
This is native core evidence, NOT actual launcher/add/resize/widget-host refresh
or full rendered UI acceptance. Current checkpoint lint is still running; inspect
`.gradle/gould012-checks/widgets-host.log` before declaring the host gate passed.
Native raw log `.gradle/gould012-checks/widgets-native.log`; test command used only
`adb -s emulator-5582 ... -e class com.opus.piano.WidgetTest`.
Manifest348 SHA256d3e5fa6c6f1c575773739912a433cc502faf10cad121ee24e9aaa1ef860a61ec;
all hashes unchanged. Candidate debugAPK SHA256
2dad30e77d4a57ac826f3e4ef3e04946b1cc7fd4d30bacc66d58bdbc99734d92;
trusted signer53eface4… and private-reference/PMP ZIP exclusion PASS.


### Widgets core verified checkpoint — 2026-10-29

The in-flight host gate finished BUILD SUCCESSFUL (4m31s):45suites/388JVM/
0failures/errors/skips; lint0errors/166warnings; debug/testAPK compilation.
Native WidgetTest7/7 PASS on emulator-5582/API36/qemu=1 (28.564s). All348 before
hashes unchanged; source manifestd3e5fa6c6f1c575773739912a433cc502faf10cad121ee24e9aaa1ef860a61ec.
APK SHA2562dad30e77d4a57ac826f3e4ef3e04946b1cc7fd4d30bacc66d58bdbc99734d92;
trusted signer verified and private-reference/PMP ZIP exclusion PASS. Native
coverage/limits: [evidence](verification/widgets-core-host-native.json).
High-risk writer/publication/recording/media-backup/journal-backup/practice-
persistence source hashes exactly match the prior verified80650b1 core; no high-
risk check is skipped for a changed file. Final full combined native/migration/
backup/microphone/upgrade/security/UI/performance/release gates remain mandatory.

Current next action: actual launcher add both widgets, resize/tap/refresh/date/
stale/offline/reboot and rendered theme/locale/font checks. Then Tux/quote and
remaining Studio/metronome/queue/Undo/notes, remaining full acceptance and owner
visual sign-off. No release or main integration. Do not rerun a full host gate
for minor unchanged-source inspections; use targeted compile/affected regressions
until the next meaningful verified batch. Do not repeat completed architecture.


Published recovery pointer: source026874928f1298601b01c52cbdf396bcbc7639af,
feature/gould-0.12; ordinary push and exact remote SHA verified. Issue41 dashboard
and comment6483665427 synchronized. Canonical supported PMP snapshot37
23ccc71e-5372-471d-b308-32fb1ba9ff67 binds this feature source explicitly; canonical Git
metadata remains main, correctly distinguished from feature verification.
No Gradle/native check remains running; local disposable emulator-5582 is still
available for next launcher validation. Earlier in-progress host notices above
are chronological history, superseded by the successful final widget gate.
Next agent should inspect status/HEAD and this latest checkpoint, then continue
actual launcher/widget acceptance without repeating completed architecture or
unchanged-source gates. Remaining scope and all owner/safety/data controls remain.


### Owner unattended authorization — 2026-10-29

Personal visual sign-off is explicitly waived for0.12 Stable, replaced by actual
emulator visual/screenshots/layout/accessibility/automated acceptance. Full remaining
scope, verified push, main integration and Stable publication are authorized only
after all unchanged mandatory gates/required CI pass. API36/existing compatibility
matrix only; API37 deferred. Current OWNER-AUTHORIZATION.md and AGENTS.md record the
decision. Do not treat historical personal-sign-off text as a new blocker. No
critical gate or physical-phone/data/signing restriction is waived. Continue actual
launcher/widget acceptance from verified0268749 / docs3cdf49d, then remaining scope.


### Actual widget launcher and visual batch — 2026-10-29, in progress

On identified API36 emulator-5582, added both providers through real Pixel launcher
Widgets picker. Live provider refresh showed ready/Free Practice and true empty
Mon–Sun week of2026-10-26 with0s, all seven day labels and verified timestamp.
Moved Weekly one grid row and resized to4columns×2rows through actual touch resize
handles; expanded sessions/day metadata visible. Actual captures inspected:
widget-launcher/both-settled.png and weekly-4x2.png under ignored checks directory.
Initial unavailable placeholder was honest; subsequent ready snapshot verified.
Quick2×1 actual host had room for context, but responsive52dp key hid it. Added
120×90dp responsive key plus focused regression case; retest pending.

Visual batch: reused existing authorized original quote_nature.png (production
provenance and SHA256ff0ff48d02ca298ceacec4c99eeb22689c0a12cf1882d9abe8dc8a18aaeae603
verified; no regeneration/reference pixels). NatureQuoteCard decodes off main,
keeps semantic accent/source/text and a strong theme scrim with solid fallback.
Tux uses isolated explicit mono metrics/font features; original ASCII bytes and
exact signature unchanged. Targeted assembleDebug/assembleDebugAndroidTest PASS
(58s); focused native inflation and actual Home/Tux/widget visuals in progress.

Remaining source audit found release-critical unfinished paths to complete in the
next coherent audio/Studio batch, not defer cosmetically: RecordingController
lifecycle is not mutex-serialized; interrupt drops writer without closing; retained
sink can append after finalization. Studio Finish uses an unsafe300ms delay instead
of awaiting finalization. Studio notes/metronome/queue Undo controls absent.
Previous player prepares on main, lacks true pause/seek/focus/lifecycle handling,
verified-playback event and recovery action; row observation is not paged despite
its comment. Media archive repository has no Settings UI entry. All require actual
implementation/regressions before release; none is accepted from earlier host gates.


### Usage-limit recovery — actual1d4234a, 2026-10-29

Actual branch/HEADfeature/gould-0.12@1d4234a matches confirmed remote; no later local
commit. Preserved all8 inherited modified/untracked visual/widget files and private
references. Latest Issue41 still open with unattended visual-waiver authorization.
Widget7/JVM388 prior evidence unchanged; inherited visual targeted build and focused
RemoteViews1case passed. Actual real launcher both providers+Weekly4×2 captures
inspected. First XML placeholder tap used Home fallback; provider now installs an
explicit Studio tap immediately before snapshot I/O (compiled, runtime retest pending).

Recovered Tux normal screenshot is correctly aligned and pink with original ASCII
and signature. The file named tux-large-en.png is **not Tux evidence**: font change
replayed the unconsumed widget navigation request and opened Studio. Corrected
OpusApp to consume openPractice via compareAndSet after routing; added native ready-
widget assertion. Targeted build now in visual-navigation-targeted-build.log;
retest two affected WidgetTest methods and actual Home/Tux/widget paths. Preserve
that failed/misnamed capture as historical diagnostics, never count it as Tux2×.

Additional critical audio gap found: OpusViewModel passes Application context to
SmartPracticeService.startRecording, whose resumed-LifecycleOwner guard always
rejects it; returned false was ignored. Thus controller could claim Recording
without a started microphone. Must pass actual visible Activity context and handle
FGS failure honestly, alongside controller serialization/close/late-sink/Finish
awaiting, notification Stop, player/media-backup UI and remaining Studio features.
All remain release blockers; no current release readiness claim.


### Recovered visual batch — targeted checkpoint, 2026-10-29

Preserved and completed inherited changes on feature/gould-0.12 from1d4234a.
Targeted debug/test APK build PASS39s; two actual affected native WidgetTest cases
PASS on emulator-5582/API36/qemu=1. Trusted certificate verified; debug APK hash
b81a15a6f76eebdb4e73ed40de57957b4b98ac2fa44af67bb18dda46da1b770b.
Actual settled Home nature and normal pink aligned Tux screenshots inspected.
Font recreation now remains About instead of replaying widget Studio navigation;
Tux dialog itself requires reopening. Initial blank Home capture is not accepted.
Widget safe initial Studio action, actual2×1 context size and4×2 large-font size
threshold implemented. Full launcher/offline/resize/reboot/font/theme matrix pending.
All14 v2 reference hashes match; separate original penguin remains private.
See verification/visual-recovery-targeted.json. Prior388JVM/7native widget evidence
is unchanged-source history, not a fresh complete gate for this batch. Remaining
critical audio/Studio/player/archive and final acceptance gates block publication.
Next coherent batch: serialize writer lifecycle, fix resumed Activity recording
start, await finalization at a frozen practice boundary and repair notification Stop.


### Combined Studio host and native audit — 2026-10-29

Implemented serialized recording lifecycle and resumed-Activity startup, frozen
Finish boundary/notification Stop; persistent stable-index queue/Undo and notes;
bounded deterministic manual metronome; async lifecycle/focus/pause/seek playback
with verified advancing-playback events; keyset recordings/recovery/delete retry;
explicit SAF audio archive/receipt UI and live-capture restore guards.
Full meaningful host gate studio-combined-host.log PASS7m28s:393JVM/0failures/errors/
skips, debug/testAPK and lint0errors/165warnings/1hint. Localization1199/policy,
host migration1–10→11 DDL and historic backup fixtures pass. Paging checker query
extractor swallowed the newer widget query; fixed bounded extraction then all106
sessions/4pages/ties/catalogue/legacy/newer-insert assertions PASS.
Actual combined native run34:32PASS,2FAIL, retained studio-combined-native.log.
Failures: static AudioTrack is NO_STATIC_DATA until initial write (fixed state
check order); migration test fixture wrongly required optional schema indices
(fixed optJSONArray while retaining all existing indexes). Native retest running
in studio-native-fixes.log, plus new delayed-startup reconcile regression.
Actual consent→microphone→Stop saved93.158s/22050Hz/monoPCM16 WAV4108332bytes,
SHA2564fd881e67f2472f26348effde76cb83606c7cb5de714fd1df4f4a7758f1b57b1;
original remains on emulator. No acoustic-piano fidelity is inferred.
Offline large-font widget tap opened Studio ready/no auto-start; first assertion
was obscured by genuine First Take popup, dismissed before Start Practice check.
Normal and2× Tux inspected (2× horizontally scrollable text, Close visible).
Refreshed2× weekly chart/week/verified label fit; transient old RemoteViews after
font change clipped the footer. New pinned footer/flexible chart and short compact
Studio title fix this; native footer regression and actual retest pending.
Badge cards now measure real localized slots at available width for equal size/
alignment without clipping/shrinking; popup View Badge routes to actual detail.
API26 required compatibility image installed; not booted/tested yet.
Next: finish corrected native/layout checks, checkpoint/push, then complete full
mandatory native/launcher/visual/data/upgrade/security/CI/release gates. Stable
remains blocked by unverified mandatory acceptance, never by waived owner sign-off.


### Corrected native and layout checkpoint — 2026-10-29

Native retest13/13 PASS (RecordingLifecycle7/StudioRecording4/schema10→11 migration1/
metronome1),0.643s. Corrected both genuine failures from the34-case run; do not hide
that failed run. Delayed startup reconciliation cannot interrupt the current live
take. Final widget RemoteViews/font-change footer2/2 PASS,0.379s; final targeted
debug/test APK build2m43s PASS. Source/evidence in verification/studio-controls-
checkpoint.json. Full393JVM host gate and lint0errors/165warnings/1hint preceded
these bounded native/layout fixes; final lint/full native remain pending.
Current required quality CI e2e0b0a failure40832264736 is the same paging extractor
bug now fixed locally; backend and maintenance CI passed. No unchanged CI retry.
New source checkpoint must be pushed then quality/full native/acceptance verified.
Additional source audit to finish: rapid recording stop/new-start service ownership
and capture-start acknowledgment; do not promote final audio acceptance prematurely.
Actual new controls/badge/player/archive flow and full widget/font/theme/locale/
reboot/rollover matrix, API26, seeded trusted upgrade, security/release and required
CI remain pending. No Stable tag/build/publication or main integration performed.


### Live full-native/capture follow-up recovery — 2026-10-29

Remote09b1fe4 is pushed; required CI3/3 PASS: quality40835545804, maintenance
40835545828, backend40835545895. Canonical PMP snapshot40/30e537c2-457f-413e-8a98-
7b816c2ae095; Issue41 comment6490687721. Unfiltered actual API36 native run
full-native-09b1fe4.log (tool session33894) is still running through253 cases;
do NOT start another run or modify emulator configuration while it runs. It began
with inherited font2/offline settings. Current known failures: obsolete-request
fixture assumes exactly one cancellation though filter changes can start another;
direct receiver invocation has no PendingResult; controlled-worker test requires
a default network, unavailable because offline was retained. Known gated external/
synthetic assumptions are skips, not passes. Final counts pending; retain all logs.

Uncommitted follow-up is intentional and must be preserved: AudioRecord startup
acknowledgment keeps controller Preparing until capture really starts; service
tracks closing/active ownership, stops microphone after recording even during
manual timing, and waits before a new explicit take. Preparing interruption retains
bytes; notifications update at phase transitions; duplicate UI start jobs guarded.
Native real-service two-take/manual-timer preservation test and acknowledgment
regressions added. Receiver null PendingResult guard and all-obsolete-jobs fixture
accounting fixed. Also discovered unused audio association table: now records exact
PCM boundaries/title/composer snapshots on manual switch/Undo and links practice
started after recording; Previous shows bounded previews and related saved practice.
Native exact sample/recovery/backup validation and complete decoded playback award
regressions added. All are pending current-source verification.

Single frozen host gate capture-associations-final-host.log (session23543) runs
assembleDebug/assembleDebugAndroidTest/testDebugUnitTest/lintDebug with3GB/in-process
compiler. Earlier capture-ownership-build.log passed but source changed during
that build, so it is NOT final evidence for the newest ownership/association fixes.
Do not duplicate Gradle or install its new APK over the running old-source suite.
When suite ends, restore network online/font1 for nominal targeted native cases;
classify offline controlled-worker failure as setup, not an application pass/fail.
Then install the final built APKs and execute RecordingLifecycleTest,
RecordingServiceTest, StudioControlsTest, StudioBackupTest, PracticeServicePolicyTest,
DiscoverEditorialAcceptanceTest and affected worker test; fix genuine failures.
New RecordingServiceTest expects no unrelated pending practice and creates/pauses
a named emulator-owned manual draft without discarding data. Preserve it honestly.

API26 image installed and isolated gould012-api26 AVD prepared under .gradle/profile-
avds (not booted yet). Never API37/phone. New badge/widget layouts compile and
focused widget footer2/2 passed at09b1fe4; full actual new badge/Studio/player/archive
visual matrix and final font-change launcher retest remain pending. Additional
audit item: popup acknowledgement currently lacks graceful storage-failure handling;
fix before final release if not yet addressed. Main/Stable untouched.


### API26 trusted upgrade and native correction checkpoint — 2026-10-29

Unfiltered API36 source09b1fe4 finished253:238PASS/3FAIL/12assumption skips.
Full log retained, not accepted as a clean full-native pass. Failures: obsolete
query fixture's exact-one cancellation assumption; direct receiver PendingResult
null; controlled-worker expected network absent under inherited offline state.
Current source fixes accounting of every obsolete request and null receiver token.
Online nominal worker then revealed its second fixture requirement: notifications
must start denied. After pre-run emulator-only revoke, worker passed fully. No
application check was weakened. Discover progressed beyond cancellation, then its
helper clicked a different partially visible duplicate Source records button;
whole-control viewport selection fixed in the test, native retest pending.

Current capture/association full host PASS11m50s:393JVM/0failures/errors/skips,
debug/testAPK and lint0errors/165warnings/1hint. API36 affected native26 had25PASS
and1worker-fixture failure; the worker subsequently passed with correct preconditions.
Exact sample-boundary associations and archive roundtrip tests added, positive
full decoded short-take award verified. Burst metronome settings now apply the last
requested tempo/beat state; serialized queued seeks exclude jump credit; popup
dismissal storage errors retain durable notices instead of crashing.

Actual published Goldberg-v0.11.apk downloaded SHA256290a15e7b15da701b8f0a40e98bf8b7c1f7cc00ce487504dcb6d4ab2e11b40d6,
trusted signer53eface4… verified. Isolated API26 gould012-api26/emulator-5584 booted
(qemu=1). Guarded root-fresh upgrade from actual non-debuggable0.11/schema10 to
debug0.12-dev/schema11 PASS:37 original sessions, original tables/notes/profile/
badge dates/draft bytes/preferences retained, integrity/FK PASS, no manufactured
audio/events. Candidate83e96f8e5c77886aac65169fd59b8a6edff7f114a7f9a088ce13f2e246a6b774.
Evidence: .gradle/gould012-checks/trusted-upgrade-api26/result.json and snapshots.
Upgrade harness now requires exact published digest/trusted certificates and an
empty identified API26/36 AVD before root seeding; normal CI keeps throwaway signing.
Current CI acceptance expected schema updated10→11; old stable tags/history unchanged.

API26 actual native compatibility33/33 PASS5.351s: recording lifecycle/clock/
associations, repository/media backup8, decoded playback/seek/metronome, historical
migration and focused widget inflation/footer/provider/DAO. No global widget tests
that reset the seeded recovery were used. Real API26 microphone record-only test
PASS1/1/4.291s, saved validated WAV and exact original draft/token/notes/credit and
37-session count unchanged. Initial test helper used UiAutomation method unavailable
onAPI26; preserved failure and corrected to shell permission grant + assertion.
API36 emulator-5582 was stopped gracefully with userdata retained, never wiped;
its profile AVD can be restarted. Current active target is emulator-5584/API26 only.

Final privacy/focus polish is uncommitted: actual capture now blocks local playback
(and no idle playback polling), recording duration uses semantic error ink, saved
vs incomplete export labels are honest, analysis/record consent/onboarding and
bundled/full privacy notices distinguish saved PCM and unencrypted audio ZIPs.
Privacy dialogs scroll at200%. Current single final host gate is capture-privacy-
final-host.log (session to recover from tool if needed); do not duplicate Gradle.
Then install final APK/testAPK on identified emulator, reverify capture/playback
block/regressions and actual UI renders, commit/push and synchronize Issue41/PMP.
Full final API36 CI/native run, actual new UI/widget matrix, boot/rollover/recovery/
SAF exports/backups/security/licensing and final Stable artifact gates remain open.
No main merge, release build, tag or publication; owner visual waiver stays valid.


### Verified capture/privacy/compatibility source checkpoint — 2026-10-29

Final full host PASS393JVM/0failures/errors/skips; debug/testAPK; lint0errors/165
warnings/1hint,6m54s. Localization1207/policy PASS. Follow-up bounded frame ownership/
prompt teardown build2m52s and playback-delete integrity build2m16s PASS.
API26 final affected18/18 PASS7.205s, then final playback/delete/real microphone
6/6 PASS6.303s. Already-started frames are not assigned retroactively to a new
take; stopped/replaced sinks cannot append. Recording uses actual microphone-start
acknowledgment and stops capture promptly when only manual timing remains.
Playback is blocked during microphone use, has no idle polling, serializes queued
seeks and awaits verified listening event commits before explicit deletion/restore.
Exact original recovered draft/token/notes/credit and37 original sessions remained
unchanged through actual API26 microphone tests. Earlier33/33 compatibility and
actual published trusted0.11 upgrade remain separately source/artifact-bound.

Consent/onboarding/settings/public+bundled privacy now truthfully distinguish
analysis from explicit recorded PCM/WAV, storage/export privacy, optional audio
ZIPs and incomplete unchanged exports; original backend/legal policy retained.
Profile/signature/15 private references and protected worktrees remain intact.
Final debug APK e109904ab46e1e6de197848eb776fe718adb8592f47d5e4bba33353d1af1b613;
trusted signer53eface4… verified. See verification/capture-privacy-api26-checkpoint.json.

Next: push this checkpoint, synchronize canonical PMP/Issue41, dispatch existing
API36 full native+upgrade CI on exact source; continue actual rendered UI/widget/
recovery/SAF/backup/security/licensing acceptance. Do not claim a clean full native
pass from the failed253-case run or assumption skips. Discover whole-control
helper correction needs rerun. Final non-debuggable Stable remains unbuilt/unpublished.
Active emulator5584/API26; preserved profile5582/API36 stopped, can restart for
final full UI matrix. No physical phone, data reset, new stage or main integration.

### Full API 36 CI follow-up — 2026-10-29

Exact source `1d06f0830d013ec443071982cf55651d8218ebed` passed the three required automatic CI workflows and the API36 acceptance-subset job in [run40849112564](https://github.com/stokonn/Gould/actions/runs/40849112564). The full native job **FAILED** two widget cases: a preceding actual-microphone test left its own paused practice, violating their empty-fixture precondition. The recording test now resets only its own matching synthetic token in `finally`, preserving actual WAVs and unrelated recovery data. Native test compilation passed; corrected full-suite execution remains pending. Application source and its prior393JVM/affected-native evidence are unchanged. Raw CI logs/reports are retained under ignored `.gradle/gould012-checks/`.

Actual320dp/font2 UI checking is in progress on identified `emulator-5584`/API26. A driver gesture that began over Studio's fixed footer failed to scroll its content; the driver now derives gestures from the observed scrollable viewport. Captures alone are not visual/accessibility acceptance. Remaining full UI/widgets/SAF/process-death/native/security/Stable artifact gates stay open. No main integration, release build, tag or publication.

