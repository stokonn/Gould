# Goldberg 0.11 — Stage 5 HANDOFF (fresh-session resume instructions)

> Created 2026-10-20; updated at the end of the second resume session (battery emergency stop).
> Stage 5 is **in progress, incomplete**. The checkpoint commit (single commit on top of
> `54f2b45`) was **pushed to origin** as the owner-authorized IN-PROGRESS checkpoint — verify
> with `git log --oneline -2`. Nothing was merged, tagged or published.

## Read order (in this order, before touching code)

1. `AGENTS.md` — permanent policy (data safety, no physical phone, publication gates).
2. `docs/ai/STATE.md` — current state, executed evidence, PMP investigation result.
3. `docs/ai/VERIFICATION.md` — executed vs. not-executed ledger.
4. `docs/ai/DECISIONS.md` — owner decisions, incl. approved F1/F2 resolutions.
5. `docs/planning/goldberg-0.11/STAGE-CHECKLIST.md` — Stage 5 section (authoritative list).
6. GitHub Issue #31 — latest checkpoint comment (do not duplicate issues).

## What the checkpoint commit contains (all uncommitted work preserved)

- `Stage5BundleJobRunnerTest.kt` — compile fixes + fixture corrections + owner-approved
  F1 retarget (`invalidRightsOnDiskQuarantineManifestAndDeactivatesJob`).
- `Stage5BundleWorkerTest.kt` (new) — 12 tests; the L324 nullable-`TestDriver?` compile error
  is **fixed**; androidTest source set compiles with 0 errors.
- `BundleStore.kt` — owner-approved F2 fix: `installBlob` dedup verifies existing target
  length+sha256; invalid bytes → quarantine (`dedup_corrupt`), verified staged copy installed;
  rename failure → `false` (fail-safe).
- `docs/ai/*` (this checkpoint) + STAGE-CHECKLIST Stage 5 progress notes.

## Verified state at checkpoint (executed, see VERIFICATION.md for commands)

- `:app:assembleDebugAndroidTest` **BUILD SUCCESSFUL, 0 errors** (after all test changes).
- `testDebugUnitTest --rerun` **174 tests / 0 failures / 0 errors** — twice (pre- and
  post-F1/F2 changes).
- `:app:assembleDebug` **BUILD SUCCESSFUL** → fresh `app-debug.apk` (Oct 20 19:45,
  22,508,542 bytes) — the previously installed one on the emulator was **stale (Oct 16)** and
  was never used for a test run.
- Emulator toolchain installed: emulator 37.2.12.0 + `system-images;android-36;google_apis/
  x86_64`; AVD `stage5-api36` created; **booted as emulator-5580** (API 36, Android 16,
  `ro.kernel.qemu=1`), then **stopped cleanly** (`adb -s emulator-5580 emu kill` → OK).
- **Zero instrumentation tests executed** (battery emergency hit before task 5.10 started).

## Exact resume steps for a new session

1. Reboot the emulator (AVD home is redirected — the env var is mandatory):
   `ANDROID_AVD_HOME=/home/stokonn/.config/.android/avd "$ANDROID_HOME/emulator/emulator"
   -avd stage5-api36 -port 5580 -no-window -no-audio -no-boot-anim -gpu swiftshader_indirect
   -no-snapshot -memory 2048 &`
   then wait for `adb -s emulator-5580 shell getprop sys.boot_completed` = 1 and confirm
   `ro.kernel.qemu`=1. **Every ADB call must use `-s emulator-5580`; never a physical phone.**
2. Rebuild if source changed since the checkpoint: `./gradlew --no-daemon --max-workers=1
   :app:assembleDebug :app:assembleDebugAndroidTest`.
3. Install **fresh** APKs explicitly:
   `adb -s emulator-5580 install -r app/build/outputs/apk/debug/app-debug.apk` and
   `adb -s emulator-5580 install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk`
   (verify timestamps first — the app APK was stale once).
4. Task 5.10 — run instrumentation explicitly on the emulator (am instrument with
   `-e class …` per Stage5 class, or Gradle connected task only with the emulator as the sole
   attached device). Record real counts.
5. Task 5.11 — emulator acceptance matrix (Wi-Fi→mobile, Data Saver, consent revocation
   mid-stream, real process death, low disk, seeded in-place upgrade from trusted 0.10.1,
   cost measurements) — Stage 4 helpers exist: `scripts/emulator_ui.py`,
   `scripts/verify_emulator_upgrade.py`, `scripts/measure-startup.sh`.
6. Task 5.12 — host gate: `./gradlew --no-daemon --max-workers=1 clean assembleDebug
   testDebugUnitTest assembleDebugAndroidTest lintDebug` + `scripts/check_*.py`.
7. Task 5.13 — docs sync, reviewed commit/push, Issue #31 + Project sync (separate
   authorization per AGENTS.md). **Do not mark 5.10–5.13 complete without executed evidence.
   Do not start Stage 6; no tag/merge/release.**
8. PMP: do **not** reinitialize without owner instruction — see STATE.md; local state was
   verified absent (nothing to preserve, nothing overwritten).

## Hard prohibitions (unchanged)

No physical phone; no implicit device selection; no push/tag/merge/release/APK publication
without separate authorization; no weakened test assertions; no fabricated results; no
Stage 5 "complete" claim; no Stage 6 start.

## Environment

Arch Linux, ~7.5 GiB RAM, Java 17.0.20.1, Android SDK 36 + emulator + API 36 google_apis
x86_64 image, Gradle 9.3.1 wrapper, `org.gradle.jvmargs=-Xmx3g`; always use
`--no-daemon --max-workers=1`. AVD location `/home/stokonn/.config/.android/avd`.
