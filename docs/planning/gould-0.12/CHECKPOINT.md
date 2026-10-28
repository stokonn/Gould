# Gould0.12 — current single-stage implementation checkpoint

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
