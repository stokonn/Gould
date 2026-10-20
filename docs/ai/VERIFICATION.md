# Goldberg 0.11 — Stage 5 VERIFICATION ledger

Honest executed vs not-executed record. Only rows marked **EXECUTED** were actually run.

## Executed — second CI dispatch + execution-model fix (2026-10-20, sixth session)

Owner-approved attempt 2: merge PR #33, dispatch against `5cdc0d6`, monitor both jobs,
preserve evidence, diagnose before any further dispatch. Evidence:
`docs/planning/goldberg-0.11/stage-5/results/ci-run-39508388525/`.

| # | Command / action | Result |
|---|---|---|
| 1 | Pre-merge check `gh pr diff 33 --name-only` + merge + post-merge `git diff 577d811 origin/main` | **EXECUTED** — workflow-only (1 file, +79/−8); PR #33 merged as `a4a9509`; restriction "merge ONLY the CI workflow" holds. |
| 2 | `gh workflow run stage5-emulator.yml --ref feature/goldberg-0.11-stage-5` → [run 39508388525](https://github.com/stokonn/Goldberg/actions/runs/39508388525), monitored to completion | **EXECUTED** — `headSha=5cdc0d6…` verified; both jobs **failure at the emulator step only**; all other steps (checkout, SHA binding, SDK, KVM, **all builds**, trusted-download, **signing**) succeeded. Zero instrumentation XMLs — **0 application tests executed; no application conclusion drawn.** |
| 3 | Signing step results from the runner (attempt-2 fix verification) | **EXECUTED** — `apksigner used: /usr/local/lib/android/sdk/build-tools/37.0.0/apksigner` (**runner-side confirmation of the run-1 root cause**); release `53eface4…d59e` = trusted ✓; baseline = candidate = `47aebcfd…34c6` ✓; all exits 0; raw `apksigner-*.log/.err` + `signing-environment.txt` uploaded as artifacts. |
| 4 | Emulator boot (run 2) | **EXECUTED** — API 36 google_apis booted, `Boot completed in 49524 ms`, serial `emulator-5554`. |
| 5 | Action source inspection (`reactivecircus/android-emulator-runner@a421e43`: `src/script-parser.ts`, `src/main.ts`) | **EXECUTED** — `parseScript()` splits `script` into single lines (comments/blanks stripped); `main.ts` runs each line via its own `sh -c`. Matches the observed three consecutive `sh -c` log entries ending in dash's `set: Illegal option -o pipefail`. Root cause of runs 39503450845 **and** 39508388525 established from source + logs. |
| 6 | Fix validation: `validate_v3.py` (YAML; faithful `parseScript` replica must yield exactly 1 chunk; `bash -n` on materialize run steps + materialized bodies) | **EXECUTED** — all pass: 1 chunk per job (`bash "$RUNNER_TEMP/stage5-<job>.sh"`), all `bash -n` OK. |
| 7 | End-to-end execution-model simulation (GH `run:` step with `bash -e` → parseScript → `sh -c` → bash file; stubbed adb/sqlite3/gradlew/`scripts/*`) | **EXECUTED** — job 1 **exit 0** (guards, cross-line `SERIAL` persistence, EXIT trap, gradle task); job 2 **exit 0 through the full acceptance flow** (WiFi→mobile net-id change, Data Saver exact readbacks, force-stop/restart gates, PRAGMA integrity/foreign-key/user_version assertions, 5× startup); negative guard test propagates exit 1. Stubs validate the execution model only — application assertions are verified only by real CI. |

### Not executed — this session (no claim)

- Any `Stage5*Test` instrumentation or real acceptance assertion on an emulator —
  run 39508388525 still produced zero XMLs (failure was pre-test, execution model).
- The third dispatch (documented diagnosis first, per the owner's instruction).
- dash-specific execution of the new single-line input (POSIX `sh -c 'bash …'`
  on the runner; locally /bin/sh is bash — validated through the parseScript replica).

## Executed — first CI dispatch + failure triage (2026-10-20, fifth session)

Phase C/D + triage of [run 39503450845](https://github.com/stokonn/Goldberg/actions/runs/39503450845)
(owner-confirmed: **CI setup/script failures — no application test ran; no application
conclusion drawn**). Full evidence bundle: `docs/planning/goldberg-0.11/stage-5/results/ci-run-39503450845/`.

| # | Command / action | Result |
|---|---|---|
| 1 | Merge CI-only PR #32 to `main` (owner-authorized Phase C) + comment-correction mirror to Stage 5 branch | **EXECUTED** — `main` at merge commit `577d811` (diff = workflow file only, +384 lines); Stage 5 branch at `bcd8894` (pushed). Quality checks passed on both. |
| 2 | `workflow_dispatch` of `stage5-emulator.yml` against `feature/goldberg-0.11-stage-5` @ `bcd8894` (run 39503450845) | **EXECUTED** — both jobs **FAILURE, pre-test**: 5.10 emulator booted ≈53 s (KVM works on runners) then script died at line 1 (`/usr/bin/sh: 1: set: Illegal option -o pipefail`, dash); 5.11 builds + trusted-download + digest OK, signing step `MISSING` ×3, emulator step skipped. **Zero instrumentation XMLs; 0 application tests executed.** SHA binding passed in both jobs. |
| 3 | Run-evidence preservation (artifacts + full raw logs downloaded and re-uploaded to the repo) | **EXECUTED** — `results/ci-run-39503450845/` contains `run-logs.zip` (SHA-256 `a6b48abb…ad85`), both failed-step logs, all six artifact files, `run-summary.md` triage write-up. |
| 4 | apksigner format investigation: install build-tools 37.0.0 locally and run both apksigner versions on the same trusted release APK | **EXECUTED** — 36.0.0 prints `Signer #1 certificate SHA-256 digest: 53eface4…d59e` (exit 0); 37.0.0 prints `V2 Signer: certificate SHA-256 digest: 53eface4…d59e` (exit 0). **Identical digests, different labels** → the old sed pattern matched nothing with apksigner 37.0.0 (preinstalled on the runner, selected by `sort -V \| tail -n1`) — root cause of the `MISSING` ×3, fully explained without any certificate mismatch. |
| 5 | Workflow corrections, static validation (`yaml.safe_load`; `bash -n` on every run step + outer sh + inner bash emulator scripts; `py_compile` on embedded Python) | **EXECUTED** — all pass. |
| 6 | Wrapper mechanics functional test under `sh -c` (stubbed adb/gradlew) | **EXECUTED** — guards pass for one `emulator-NNNN` + `qemu=1`; suite invoked; EXIT trap fires through `exec bash`; guard failure propagates exit 1. dash not installed locally → the 3 outer wrapper lines are POSIX by reasoning, `bash -n`-validated; real dash run happens on the runner. |
| 7 | Signing step sandbox in exact runner conditions (both apksigner versions present; 37.0.0 selected) — success, genuine-cert-mismatch (separately generated test keystore), corrupt-APK paths | **EXECUTED** — success exit 0 with all evidence files; mismatch exit 1 naming both digests; corrupt APK exit 1 with non-zero exit code recorded and raw streams preserved. Assertions strict and unchanged. |

### Not executed — this session (no claim)

- The API 36 emulator suite runs for tasks 5.10/5.11 (both pending the workflow-fix
  re-dispatch after explicit owner approval); no instrumentation XMLs exist yet.
- dash (`/usr/bin/sh` on the runner) is not installed on the host — outer wrapper
  POSIX-compliance is reasoned + `bash -n`, not dash-executed.
- The actual runner-side apksigner selection inventory (recorded next run by
  `signing-environment.txt`); the 37.0.0-preselection root cause is proven by local
  reproduction of `sort -V \| tail -n1` over both versions + identical digest values,
  not by a runner-side listing from run 39503450845 (its step suppressed diagnostics —
  the defect that was fixed).

## Executed — CI activation session (2026-10-20, fourth session, desktop host)

Phase A/B preparation for KVM CI verification (no CI run, no emulator run, no
instrumentation executed this session — nothing is claimed beyond these host-side checks):

| # | Command / action | Result |
|---|---|---|
| 1 | `apksigner verify --print-certs` (build-tools 36.0.0) on the downloaded trusted `Goldberg-v0.10.1.apk` | **EXECUTED** — APK sha256 `854bbfebf617ec920df60d181a12e0dcbd2fed10052f2479903a78a290cef312` (matches expected); signer cert SHA-256 `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e` = the trusted certificate (DN `C=US, O=Android, CN=Android Debug`). Proves a CI-runner debug-signed candidate **cannot** be installed in place over the release-signed baseline (different keystore) → workflow revised to rebuild the v0.10.1 baseline with the same runner debug keystore; production credentials stay out of CI. |
| 2 | AOSP source inspection of API 36 network APIs (`NetworkPolicyManagerShellCommand`, `NetworkPolicyManagerService.dump`, `ConnectivityService`, `NetworkAgentInfo.toString`) | **EXECUTED** — found the previous workflow used a **non-existent command** `cmd netpolicy set data_saver enable` on API 36; correct form is `set restrict-background true\|false` with `get restrict-background` → `Restrict background status: enabled\|disabled`; `dumpsys netpolicy` reports `Restrict background: true\|false`; default-network transport is assertable via `Active default network: <id>` + the id's `NetworkAgentInfo … ni{type: WIFI\|MOBILE …}` block. |
| 3 | `git ls-remote --tags` verification of all five SHA-pinned actions | **EXECUTED** — checkout `11d5960…`=v4.4.0, setup-java `cf277c6…`=v4.9.1, setup-android `9fc6c4e…`=v3.2.2, android-emulator-runner `a421e43…`=v2.38.0, upload-artifact `ea165f8…`=v4.6.2 — every pin resolves to its claimed release tag. |
| 4 | Workflow revision + full local validation (`yaml.safe_load`; `bash -n` on all 12 run/emulator scripts; `py_compile` + functional tests of both embedded Python snippets) | **EXECUTED** — all pass. The default-network parser was unit-tested against AOSP-format dumps: WIFI→OK, MOBILE→OK, transport-mismatch→FATAL, `none`→FATAL (one parser bug found and fixed during this: indented `NetworkAgentInfo` lines). |
| 5 | Exclusion scan: `@Ignore` in `app/src/androidTest`, `testInstrumentationRunnerArguments`/`excludeClasses`/`managedDevices` in Gradle | **EXECUTED** — **0 matches**: the CI `connectedDebugAndroidTest` runs all 42 androidTest classes with no filters, exclusions or skips. |
| 6 | v0.10.1 tag build-config inspection (`git show v0.10.1:…`) | **EXECUTED** — Java 17, compileSdk 36, Gradle 9.3.1, AGP 9.1.0, versionName `0.10.1`, versionCode 11 → the tag rebuild will work with the same CI toolchain and satisfies `verify_emulator_upgrade.py --baseline-version 0.10.1 --baseline-schema 8`. |
| 7 | `origin/main` helper-script inspection | **EXECUTED** — main's `verify_emulator_upgrade.py` is an older revision without the `--baseline-schema/--expected-schema/--offline/--startup-runs` flags → the workflow must be dispatched against `feature/goldberg-0.11-stage-5` (documented in the workflow header). |
| 8 | CI-only branch + draft PR creation (worktree `/tmp/opencode/goldberg-ci-only` off `origin/main`, branch `ci/stage5-emulator-verification`) | **EXECUTED** — commit `9870d99`; `git diff origin/main --stat` = **1 file** (`.github/workflows/stage5-emulator.yml`, +379); draft [PR #32](https://github.com/stokonn/Goldberg/pull/32) opened, **not merged**; `gh pr diff --name-only` confirms the single workflow file. Stage 5 working tree and all untracked files (`.pmp/`, `.pmp.yml`, `RELEASE_NOTES.md`, stage-4 checkpoint archive) untouched. |

## Not executed — CI activation session (no claim)

- **Any GitHub Actions run** — the workflow has never been dispatched; PR #32 awaits the
  owner's Phase C approval (merge + manual dispatch are gated).
- Any emulator run, any `Stage5*Test` instrumentation, any 5.11 acceptance item — all
  still pending the CI run (or notebook); tasks 5.10/5.11 remain open.
- The strengthened Data Saver/Wi-Fi assertions were validated against AOSP source and
  synthetic dumps only; their first real-device-format execution will be the CI run
  (a format surprise there would be diagnosed, never weakened away).

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
| 11 | GUI-mode emulator attempt (owner-requested): `DISPLAY=:0 XAUTHORITY=… .mutter-Xwaylandauth.* emulator -avd stage5-api36 -port 5580 -gpu auto` (no `-no-window`), emulator 37.2.12 | First launch aborted before QEMU (missing X authority in tool shell). Relaunch with session XAUTHORITY: Qt window created on the GNOME desktop, then **`Killed by SIGSEGV`** after the protected-range warning → **GUI mode also fails; no ADB device ever appeared.** All local emulator retries stopped per owner instruction. |
| 12 | CI preparation: verified GitHub-hosted runner KVM support + `reactivecircus/android-emulator-runner` v2.38.0 (pinned `a421e43855164a8197daf9d8d40fe71c6996bb0d`); wrote `.github/workflows/stage5-emulator.yml` (workflow_dispatch only, SHA-pinned actions, explicit emulator-NNNN + `ro.kernel.qemu=1` guards); `yaml.safe_load` validation; `py_compile` on `verify_emulator_upgrade.py`/`emulator_ui.py`; `bash -n` on `measure-startup.sh`; trusted baseline 0.10.1 APK digest `854bbfebf617ec920df60d181a12e0dcbd2fed10052f2479903a78a290cef312` recorded from the GitHub release API | **Workflow prepared locally; later committed+pushed as `1ef15bf` and revised in the CI-activation session (see the top section) — no CI results claimed.** |

## Not executed — third session (no claim)

- All `Stage5*Test` instrumentation (task 5.10) and the emulator acceptance matrix
  (task 5.11) — blocked by the desktop emulator bug (row 4, GUI mode too per row 11);
  execution paths are the notebook (per `docs/ai/HANDOFF.md`) or the prepared CI
  workflow (row 12) — neither has produced results yet. F1/F2 runtime proof still
  pending that run.
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
