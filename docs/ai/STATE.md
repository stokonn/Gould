# Goldberg 0.11 — Stage 5 STATE (recovery checkpoint)

> **Last updated:** 2026-10-20, end of the second resume session (battery emergency stop).
> Read order for a fresh session: `AGENTS.md` → this file → `docs/ai/HANDOFF.md` →
> `docs/planning/goldberg-0.11/STAGE-CHECKLIST.md` (Stage 5 section) → GitHub Issue #31.
> **Stage 5 is NOT complete. No push/tag/merge/release/APK publication performed.**

## Git / branch state

- Branch `feature/goldberg-0.11-stage-5`, parent `54f2b45` + **one checkpoint commit**
  (Stage 5 test/production/checkpoint work). **Pushed to origin as the owner-authorized
  IN-PROGRESS checkpoint** (2026-10-20 battery emergency; verification incomplete — see
  VERIFICATION.md for what remains unexecuted). No merge/tag/release.
- In the checkpoint commit:
  - `M app/src/androidTest/java/com/opus/piano/Stage5BundleJobRunnerTest.kt` — compile fixes,
    fixture corrections, F1 retarget (owner-approved, test-only).
  - `M app/src/main/java/com/opus/piano/data/offline/BundleStore.kt` — F2 fix
    (owner-approved): installBlob dedup now verifies existing target length+sha256; invalid
    bytes moved to quarantine (`dedup_corrupt`, never deleted), verified staged copy installed
    so the same attempt repairs; quarantine-rename failure returns false (fail-safe).
  - `?? app/src/androidTest/java/com/opus/piano/Stage5BundleWorkerTest.kt` — 12-test draft,
    L324 nullable `TestDriver?` compile error FIXED (`?.setAllConstraintsMet`, assertions
    unchanged).
  - `?? docs/ai/*` — this checkpoint (STATE/VERIFICATION/HANDOFF/DECISIONS).
  - `M docs/planning/goldberg-0.11/STAGE-CHECKLIST.md` — Stage 5 progress notes.

## Executed this session (2026-10-20, second resume session) — real results

1. Fixed the 1 open compile error (`Stage5BundleWorkerTest.kt:324` nullable TestDriver call).
2. **EXECUTED** `:app:assembleDebugAndroidTest` → **BUILD SUCCESSFUL** (2m02s, 0 errors;
   only the 7+ known nullable-`listFiles()` warnings).
3. **EXECUTED** `testDebugUnitTest --rerun` → **174 tests / 0 failures / 0 errors / 0 skipped**
   (30 classes, fresh evidence).
4. **F1 + F2 fully investigated and confirmed against source; owner approved both recommended
   resolutions** (recorded in `docs/ai/DECISIONS.md`):
   - F1 retarget implemented: `invalidRightsOnDiskQuarantineManifestAndDeactivatesJob`
     (hand-tampered on-disk manifest → Permanent(manifest_missing), job deactivated, bytes
     preserved in quarantine, never READY). No production change.
   - F2 `BundleStore.installBlob` fix implemented (~15 lines incl. KDoc quarantine line).
5. **EXECUTED** `:app:testDebugUnitTest :app:assembleDebugAndroidTest` after F1/F2 changes →
   **BUILD SUCCESSFUL** (2m10s); fresh JVM counts **174 / 0** (reports regenerated 19:23).
6. **PMP investigated** (see section below) — actual state confirmed, nothing reinitialized.
7. Emulator setup: **EXECUTED** sdkmanager install of `emulator` (37.2.12.0) +
   `system-images;android-36;google_apis;x86_64` (r07); AVD `stage5-api36` created.
   **AVD home is redirected**: `/home/stokonn/.config/.android/avd` — the emulator binary
   needs `ANDROID_AVD_HOME=/home/stokonn/.config/.android/avd` or it fails with
   "Unknown AVD name". Booted **emulator-5580** headless (API 36, Android 16,
   `ro.kernel.qemu=1` — verified emulator, explicit serial, no physical phone ever used).
8. **EXECUTED** `:app:assembleDebug` → BUILD SUCCESSFUL (5m44s) after catching that the
   existing `app-debug.apk` was stale (Oct 16, pre-Stage-5). Fresh `app-debug.apk`
   (22,508,542 bytes, Oct 20 19:45) exists in `app/build/outputs/apk/debug/`.
9. Installed to emulator-5580 (both `Success`) — but **before** the staleness catch, so the
   *installed* app APK was the stale one; **no instrumentation test was ever executed**.
   Emulator stopped safely on battery warning.

## PMP investigation result (question #6 — answered, NOT reinitialized)

- **PMP = Project Memory Protocol** — owner's public repo `stokonn/project-memory-protocol`
  (created 2026-10-19): "Local-first, agent-agnostic development memory, checkpoints and
  handoffs for Git projects" (Python CLI, `pmp init/status/resume/handoff`, XDG state dirs,
  `PMP_STATE_HOME` override).
- Evidence the earlier initialization succeeded: GitHub Issue #31 checkpoint comment (2026-10-20
  13:46Z) says "**PMP state local/private**"; the `docs/ai/*` HANDOFF/STATE/VERIFICATION/
  DECISIONS structure mirrors PMP's handoff style.
- **Why the previous session found nothing: no durable local trace exists anywhere now.**
  Verified absent: `pmp` CLI (PATH, `~/.local/bin`, pipx, `python3 -m pip`, uv); PMP state
  (`~/.local/state/pmp`, `~/.config/pmp`, `~/.local/share/pmp`, `~/.pmp`, `$PMP_STATE_HOME`
  unset); `.pmp/`/`.pmp.yml` in the working tree **or in any commit on any branch**; a local
  clone of the repo under `~/Projects` or `/tmp`. Conclusion: the earlier init evidently lived
  in an ephemeral location (e.g. /tmp or a since-removed worktree) or was deleted — PMP state
  is by design local/private and never committed, so its loss leaves no git trace.
- **Not reinitialized** (owner instruction: confirm state first). Re-init is an owner decision;
  `docs/ai/*` serves Stage 5 handoff in the meantime.

## Not executed / not claimed

- **All `Stage5*Test` instrumentation (tasks 5.10/5.11) — zero tests executed on the emulator.**
- `lintDebug`, `scripts/check_*.py` host checks (task 5.12) this session.
- The installed app APK on the stopped emulator is stale — must reinstall fresh APKs first.
- No screenshots, no physical phone (forbidden), no push/merge/tag/release (forbidden).

## Environment

Arch Linux, 7.5 GiB RAM, Java 17.0.20.1, Android SDK 36 (+ emulator + API 36 google_apis
x86_64 image), AVD `stage5-api36`, Gradle 9.3.1, `org.gradle.jvmargs=-Xmx3g`, all builds
`--no-daemon --max-workers=1`. Battery emergency stopped the session; emulator already killed
(`adb -s emulator-5580 emu kill` returned OK).

## Prohibitions (still active)

No physical phone / implicit ADB device; explicit `emulator-NNNN` serial only. No push, tag,
merge, release or APK publication without separate authorization. Do not mark Stage 5 tasks
5.10–5.13 complete; do not start Stage 6. Never weaken test assertions; never fabricate results.
