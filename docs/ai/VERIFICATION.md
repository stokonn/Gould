# Goldberg 0.11 — Stage 5 VERIFICATION ledger

Honest executed vs not-executed record. Only rows marked **EXECUTED** were actually run.

## Executed — third resume session (2026-10-20, desktop machine)

| # | Command | Result |
|---|---|---|
| 1 | `./gradlew --no-daemon --max-workers=1 :app:assembleDebug :app:assembleDebugAndroidTest` | **BUILD SUCCESSFUL**, 1m08s (Java 25 host); only known nullable-`listFiles()` warnings. Fresh `app-debug.apk` 32,388,569 B sha256 `44222c59…a157`; `app-debug-androidTest.apk` 1,331,040 B sha256 `6030e528…a8f2d` (Oct 20 21:13). |
| 2 | `sdkmanager "system-images;android-36;google_apis;x86_64"` (cmdline-tools 12.0 installed first) | Image r07 installed. |
| 3 | `avdmanager create avd -n stage5-api36 -k system-images;android-36;google_apis;x86_64 -d pixel_6` | AVD created (`~/.android/avd/stage5-api36`). |
| 4 | Emulator boots (explicit `-port 5580` / serial `emulator-5580`, headless): API 37 + swiftshader ×2 (37.2.12); API 37 + auto/lavapipe (37.2.12, ran 13 min, guest never booted, adb offline); API 36 + swiftshader (37.2.12); API 36 + auto/lavapipe (37.2.12); API 36 + swiftshader (37.3.4 canary) | **FAILED — `Killed by SIGSEGV`** on every completed attempt after `WARNING: cannnot unmap ptr … protected range …`. Known upstream bug (Google #566807521), Fedora kernel 7.2.10; unfixed in 37.3.4. **No emulator runs; no instrumentation executed.** |
| 5 | `python3 scripts/check_agent_policy.py` | **PASS** (exit 0): canonical policy, thin agent references, Cursor frontmatter, local Wiki navigation; runtime agent loading not tested. |
| 6 | `python3 scripts/check_backup_fixtures.py` | **PASS** (exit 0): 0.6.0-f1 / 0.9.1-f2 / 0.9.2-f3 fixtures → schema 10, all portable fields retained. |
| 7 | `python3 scripts/check_history_paging.py` | **PASS** (exit 0): 106 sessions / 4 pages + legacy association cases. |
| 8 | `python3 scripts/check_localization.py` | **PASS** (exit 0): 922 EN/CZ resources, placeholders, Czech plurals. |
| 9 | `python3 scripts/check_migrations.py` | **PASS** (exit 0): v5–v9 → v10 DDL, legacy data retained; bridge allocation requires Android tests (noted, not run). |

Host gate executed this session (2026-10-20, desktop):

| # | Command | Result |
|---|---|---|
| 10 | `./gradlew --no-daemon --max-workers=1 clean assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug` | **BUILD SUCCESSFUL**, 3m24s, 83/84 tasks executed. `testDebugUnitTest` **174 tests / 0 failures / 0 errors / 0 skipped** (30 classes). Lint **0 errors / 110 warnings** (2 in Stage 5 offline code: `UsableSpace`, `UseKtx`; rest pre-existing). Fresh clean-build `app-debug.apk` 22,508,542 B sha256 `b6ad01206d24b74e371edd16b9aec26d720f41afca8412557f1f7b21e3fb88ff` — **same byte size as the notebook checkpoint APK** (22,508,542 B), cross-machine source-identity evidence. `app-debug-androidTest.apk` 988,287 B (clean build). Note: the earlier incremental build (row 1) produced different-size APKs; the clean-build artifacts supersede them as the authoritative Stage 5 host-gate outputs. |

## Not executed — third session (no claim)

- All `Stage5*Test` instrumentation (task 5.10) and the emulator acceptance matrix
  (task 5.11) — blocked by the desktop emulator bug (row 4); deferred to the notebook
  per `docs/ai/HANDOFF.md`. F1/F2 runtime proof still pending that run.
- `scripts/emulator_ui.py` / `verify_emulator_upgrade.py` / `measure-startup.sh` (need a
  booted emulator).

## Executed — second resume session (2026-10-20, battery-emergency stop)

| # | Command | Result |
|---|---|---|
| 1 | `./gradlew --no-daemon --max-workers=1 :app:assembleDebugAndroidTest` (after L324 fix) | **BUILD SUCCESSFUL**, 2m02s, 0 errors; known nullable-`listFiles()` warnings only. |
| 2 | `./gradlew --no-daemon --max-workers=1 :app:testDebugUnitTest --rerun` | **174 tests / 0 failures / 0 errors / 0 skipped** (30 classes, fresh). |
| 3 | `./gradlew --no-daemon --max-workers=1 :app:testDebugUnitTest :app:assembleDebugAndroidTest` (after F1 retarget + F2 `installBlob` production fix) | **BUILD SUCCESSFUL**, 2m10s; fresh JVM reports 19:23 → **174 / 0 / 0 / 0**. |
| 4 | `sdkmanager "emulator" "system-images;android-36;google_apis;x86_64"` | Installed emulator 37.2.12.0 + system image r07. |
| 5 | `avdmanager create avd -n stage5-api36 …` (needs `ANDROID_AVD_HOME=/home/stokonn/.config/.android/avd` for the emulator binary) | AVD created. |
| 6 | Emulator boot `… -avd stage5-api36 -port 5580 -no-window …` | **emulator-5580** booted; API 36 / Android 16 / `ro.kernel.qemu=1`; boot completed ~82s. |
| 7 | `adb -s emulator-5580 install -r` (app + androidTest APK) | Both `Success` — **but app-debug.apk was the stale Oct 16 build at that moment**. |
| 8 | `./gradlew --no-daemon --max-workers=1 :app:assembleDebug` | **BUILD SUCCESSFUL**, 5m44s → fresh `app-debug.apk` Oct 20 19:45, 22,508,542 bytes. **Not yet reinstalled.** |
| 9 | `adb -s emulator-5580 emu kill` | `OK: killing emulator` — clean stop at battery warning. |

## Executed — first resume session (2026-10-20)

| # | Command | Result |
|---|---|---|
| 1 | `:app:assembleDebugAndroidTest` (after runner-test fixes, before worker test) | BUILD SUCCESSFUL 4m54s, 0 errors. |
| 2 | `:app:testDebugUnitTest` | UP-TO-DATE → 0 tests re-executed (superseded by row 2 above). |

## Last previously executed evidence (prior session, per STAGE-CHECKLIST)

- Full host gate `clean assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug`:
  BUILD SUCCESSFUL, **146 tests / 0 failures**, lint 0 errors (109 warnings).
- After tasks 5.5–5.9: `testDebugUnitTest` **174 tests / 0 failures** (now reproduced fresh).

## Not executed this session (no claim)

- **All instrumentation tests** (`Stage5BundleStoreTest`, `Stage5BundleJobRunnerTest`,
  `Stage5BundleWorkerTest`) — emulator was booted and identified (emulator-5580) but the
  battery emergency stopped the session **before any test execution** (tasks 5.10/5.11 open).
- `lintDebug`, `assembleDebug` was run (row 8) but the full gate + `scripts/check_*.py`
  (migrations/backup/localization/paging/policy) — task 5.12 not run.
- Emulator acceptance matrix (Wi-Fi→mobile, Data Saver, consent revocation mid-stream, real
  process death, low disk, seeded in-place upgrade from trusted 0.10.1, cost measurements).
- F1/F2 fixes are compile-verified + JVM-regression-free; their **runtime** proof requires the
  instrumentation run in 5.10 (pending).

## Warning/error inventory at checkpoint

- androidTest compile: **0 errors**; warnings: 7+ nullable `listFiles()` receivers (runner
  test), 1 pre-existing `Stage3bBackupTest` reified-type warning.
- Installed-on-emulator app APK is stale (Oct 16) — reinstall fresh APKs before running tests.
