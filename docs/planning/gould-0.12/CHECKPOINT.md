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

Next immediate work: shared service/recording capture lifecycle with truthful
record states and one microphone owner, then Studio/Previous UI, achievements/
Profile/widgets/Tux/quote wiring, then native/emulator acceptance. None of those
unimplemented features may be marked complete.

Canonical PMP active task gould-0.12-complete, supported CLI (missing feature-state
not fabricated). Previous main maintenance task was explicitly archived through
CLI after preserving a snapshot; maintenance remains incomplete/private originals
protected. Feature foundation checkpoint sources actual5c32259+manifest, not main.
