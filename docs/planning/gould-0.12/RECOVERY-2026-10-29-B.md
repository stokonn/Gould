> Current launcher acceptance continuation — 2026-10-29: resumed from50e32ed without repeating pristine WidgetTest8/8 or unchanged13 calendar/projection tests. Actual API36 launcher resize/independent instances/EN-CZ/light-dark/font1–2/offline and protected-data checks executed; found and fixed a clipped200% compact stale marker. Targeted debug/test build PASS37s, new native regression1/1 and actual-host fixed stale/unavailable/accessibility probe1/1 PASS, lint0errors/165warnings/1hint PASS4m25s, localization1211/policy/APK/trusted signer PASS. Exact draft/3WAVs/recording rows/history/badge rows retained; original widget IDs2/3 and added5 preserved. First offline boot Pixel Launcher ANR is retained; corrected settled repeat has no ANR and both original widgets render live. Final readback: ALL28 tables exact,3 WAV hashes exact, integrity/FK PASS; original system-following preferences/font1 restored. Residual accent/spec-cell review and remaining release gates are OPEN. [Current acceptance checklist](WIDGET-LAUNCHER-ACCEPTANCE.md) · [Source-bound evidence](verification/widget-launcher-current.json). Earlier checkpoint counts/statuses below are historical; no main/release/tag or phone operation.

# Gould 0.12 — session recovery checkpoint, 2026-10-29 (second takeover)

Phase2 remains ONE IN-PROGRESS development stage. No main merge, release build, tag or
publication. Owner unattended release authorization/visual waiver remain valid once every
mandatory gate passes. This document is the authoritative handoff for the next agent.

## Post-checkpoint addendum (same session, after `f2757dc`)

The widget fixture blocker described above was resolved **without discarding the protected
interrupted draft**: `activeAndPausedWidgetTaps…`'s blank-token precondition was analyzed in
source and found load-bearing (`PracticeEngine.begin` reuses a non-blank token, and the test's
`finally { pause(); reset() }` would then delete the fixture), so it was left unchanged; instead
a fresh pristine AVD `gould012-pristine` (API36, `emulator-5590`, qemu=1) was created and
**WidgetTest passed 8/8** there — three full-class runs plus each formerly blocked test
individually OK(1). Logs: `.gradle/gould012-checks/widget-pristine-5590*.log`. The profile
emulator-5582 was stopped cleanly beforehand (data preserved on disk). Widget unit cases are
now complete; the actual launcher matrix and all other gates listed below remain open.

## Current Git / continuity state

- Branch `feature/gould-0.12`, HEAD `791b36d9bceed9147b85390aab0c6a98b2f6020f`, **pushed and
  matching origin**. Working tree CLEAN (no modified/staged/untracked source files).
- Two commits landed this session (both verified + pushed + Issue41/PMP synced):
  - `8ccdd0e` — committed the inherited verified Previous availability/export-safety patch
    (393 JVM/48suites, lint follow-up 0 errors/165 warnings, affected API36 native 31/31,
    debug APK 13e1fd55… quality/trusted PASS).
  - `791b36d` — fixed a genuine live-take visibility gap + added a native regression; actual
    real-emulator Previous UI verified (see below).
- Required CI on BOTH commits: **3/3 PASS** (Goldberg quality, Gould community BFF, Gould
  maintenance administration). On `791b36d`: quality 9m4s, BFF 2m3s, maintenance 23s, all success.
- PMP canonical (project at `~/Projects/Opus`): **snapshot 47** binds `791b36d`. Issue #41
  dashboard + comments `6500800384` (8ccdd0e) and `6501580955` (791b36d) synchronized.
- Local continuity docs (HANDOFF/STATE/VERIFICATION/CHECKPOINT/STAGE-CHECKLIST) updated and
  committed through `791b36d`. Evidence files: `verification/previous-availability-host-native.json`,
  `verification/previous-live-saved-ui.json`.

## What was completed and verified this session

1. Recovered the exact inherited state by re-verifying ALL prior evidence against source/logs
   (native-log SHA, APK hashes, JVM XML counts, Issue/PMP) rather than trusting summaries.
2. Committed + pushed the verified inherited Previous patch (`8ccdd0e`).
3. Found and fixed a **genuine defect** while driving the real UI: `StudioDao.audioPage`
   filtered `state IN ('Saved','Recoverable','Missing','Deleting')`, excluding live
   Preparing/Recording/Finalizing takes, so an active take never appeared in Previous and the
   new `studio_audio_in_progress` branch was unreachable — exactly why the prior agent's real
   driver failed. Corrected to `state != 'Deleted'` (keyset/LIMIT unchanged) and added
   `StudioRecordingTest.activeTakeAppearsInPagedRecordingsUntilExplicitDeletion` (`791b36d`).
4. Verified `791b36d`: affected API36 native **32/32 PASS** (StudioRecordingTest7,
   StudioControlsTest, RecordingLifecycleTest, StudioBackupTest) on emulator-5586; debug APK
   `30b281eb39074169d80d0520cfc7c6df4e029c78518f2b1ab540f09d55e6f618`, trusted signer PASS;
   localization1211/policy/diff-check PASS.
5. **Actual real-emulator Previous UI** (real microphone recording; driver
   `.gradle/gould012-checks/previous_ui_livesaved.py`; captures under
   `.gradle/gould012-checks/final-ui/previous-functional/flow/`): live take renders the
   in-progress message with **Export/Delete disabled**; a leftover interrupted take shows its
   message with Export/Delete **enabled** + a Recover action; after Stop the newly saved take
   shows **Play / Export original / Delete all enabled**. This closes the "actual Previous
   live→saved transitions and disabled actions" gate.

## WidgetTest status — NOT PASSED (do not claim passed)

- Ran `WidgetTest` on the clean `gould012-profile` AVD (emulator-5582/API36/qemu=1) and on the
  controlled-probe AVD (emulator-5586). **Both: `Tests run: 8, Failures: 2`.** The 6 non-fixture
  cases pass (cache/corruption, real RemoteViews across sizes/themes/fonts, provider/immutable
  intent identity, bounded Room week projection, etc.).
- The 2 failures are `readyWidgetTapDoesNotCreateSessionDraftAudioOrStartCapture` and
  `activeAndPausedWidgetTapsPreserveTheSamePracticeWithoutResumeOrDuplicateStart`. Both trip
  their **precondition** `check(app.practice.state.value.token.isBlank() && !running)` — NOT an
  assertion about widget behavior.
- **Root cause (definitively diagnosed via a temporary instrumentation probe, since removed):**
  the AVD carries the checkpoint-protected **7.805s interrupted practice draft**; on cold start
  the coordinator correctly recovers it into a resumable state:
  `token=[68c2cb60-…] running=false ready=true status=Interrupted elapsed=7805`. Draft count in
  `practice_drafts` is 0 (the interrupted practice is persisted by the practice recovery
  repository, not the draft table — clearing the draft table did not and must not remove it).
  This is **correct recovery behavior + intentional preserved fixture**, NOT an app defect and
  NOT related to the `audioPage` change (widgets do not use that query).
- These 2 cases require a **pristine no-history AVD**. They must NOT be "fixed" by discarding
  the protected interrupted draft. **Widget gate remains OPEN/blocked-by-fixture.**
- A temporary `TempPracticeStateProbe.kt` was created for the diagnosis and has been REMOVED;
  the working tree is clean. A synthetic zero-value practice draft that I had earlier created
  on emulator-5582 was backed up to
  `.gradle/gould012-checks/profile-5582-fixture-backup/practice-draft-backup.json` and cleared;
  the 3 Saved local_audio recordings on that AVD were preserved (verified count=3 after the
  DB edit; DB integrity_check ok, 28 tables intact).

## Emulator / database state

- **emulator-5582** (`gould012-profile`, API36, 2048MB/4cores, lavapipe, offline): RUNNING. Holds
  the preserved 7.805s interrupted practice + 3 Saved recordings + both launcher widgets. A
  synthetic zero-value draft residue was backed up and removed to restore the widget fixture;
  the interrupted draft and recordings are intact.
- **emulator-5586** (`GoldbergStage8Independent`, API36 controlled-probe): STOPPED this session
  (was used for the Previous native31/31 + live-UI verification). Retained; carries the protected
  `upgrade-fixture-draft` practice draft +37-session fixture — widgets must NOT run here.
- **emulator-5584** (`gould012-api26`, API26): not booted this session; retains original 37
  sessions/3s draft/4 verified WAVs + trusted0.11→0.12 upgrade evidence.
- Never API37, never the owner's physical phone. Start only one emulator at a time. Always
  explicit `emulator-NNNN` + `ro.kernel.qemu=1` guards.

## Remaining mandatory gates (all OPEN; Stable stays blocked)

1. Widget acceptance: run the 2 fixture-precondition cases on a pristine AVD (or reconcile the
   test's blank-token precondition with the correct interrupted-practice recovery semantics
   WITHOUT weakening the gate), plus actual launcher add/resize/tap/offline/multiple-instances/
   Mon–Sun/rollover/font/theme/locale/accessibility matrix and full changed-layout matrix.
2. Security / privacy / licensing / performance checks.
3. Required CI already PASS on `791b36d` (rerun if further source changes land).
4. Final clean trusted **non-debuggable** Stable artifact + certificate + seeded in-place
   upgrade acceptance (current debug APK 0.12-dev/code14 is debuggable and is NOT a Stable
   candidate). Verify final package/version/ZIP/artwork/masks/signer/SHA-256 vs published 0.11.

## Exact next action

Address the 2 blocked widget cases without discarding the protected interrupted-practice
fixture: either boot a pristine disposable AVD for them, or — preferred, since the recovery is
correct behavior — update the two tests' `check(token.isBlank())` precondition to accept a
recovered Interrupted (non-running) practice while still asserting the real invariant (widget
taps must not create sessions/drafts/audio/capture nor resume/duplicate a paused practice).
Then complete the finite widget/layout/font/theme/locale matrix, security/licensing/performance,
and the final non-debuggable Stable artifact/upgrade gates. Keep PMP/Issue41/docs synchronized
at each verified milestone. No physical phone, API37, extra stage or destructive cleanup.

created by stokonn with love <3
