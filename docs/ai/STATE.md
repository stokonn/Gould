# Goldberg 0.11 — Stage 5 STATE (recovery checkpoint)

> **Last updated:** 2026-10-21 — **STAGE 5 COMPLETE, OWNER-APPROVED** (owner
> approved the completion claim based on the verified CI evidence, completed
> checklist, archived screenshots and final documentation; GitHub Project item
> #31 moved to Done). Carried-forward dependency: real bundle-transfer costs
> against a production CDN await the Stage 6/7 bundle feed (no feed exists
> today; DECISIONS.md). Prior: screenshot session — **all Stage 5 mandatory
> verification requirements now have passing executed evidence.** Screenshot
> job: 3 attempts failed on 3 distinct *script* defects (each root-caused from
> raw logs with host-side proof, fixed via workflow-only PRs #36–#38, zero
> application defects), then [run 39576173002](https://github.com/stokonn/Goldberg/actions/runs/39576173002)
> @ `3579417` **GREEN — 12/12 screenshots** (Home/Repertoire/Discover/Practice/
> Profile/Settings × Ivory/Nocturne), agent-inspected pair-by-pair, preserved in
> `results/ci-run-39576173002/` (+ `run-summary.md`). Prior session: attempt-6
> FULLY GREEN ([run 39557624554](https://github.com/stokonn/Goldberg/actions/runs/39557624554):
> 5.10 177/0/0/2; 5.11 every section executed); attempt-6 evidence preserved
> permanently (22 MB incl. both full logcats SHA-verified); 2 skips root-caused
> (`skipped-tests.md`); consent-revocation/low-disk coverage mapped to executed
> instrumentation; **5.12 host gate green** (unit 174/0/0/0, lint 0 errors/110
> warnings, 5 check scripts PASS); real-transfer costs owner-dispositioned
> (measured evidence accepted; real-CDN = Stage 6/7 dependency, DECISIONS.md).
> Tasks 5.10/5.11/5.12 complete; **5.13 final documentation/handoff is the
> closing step.** Read order: `AGENTS.md` → this file → `docs/ai/HANDOFF.md` →
> `docs/planning/goldberg-0.11/STAGE-CHECKLIST.md` (Stage 5 section) → Issue #31.
> **No merge/tag/release/APK publication performed.**

## 🚦 Current status: **STAGE 5 COMPLETE — OWNER-APPROVED 2026-10-21** (5.10 ✓ / 5.11 ✓ incl. green screenshots / 5.12 ✓ / 5.13 ✓) — awaiting the owner's separate decision to merge application code / start Stage 6

- **Attempt 6 ([run 39557624554](https://github.com/stokonn/Goldberg/actions/runs/39557624554)
  @ `44645fa`, headSha + both jobs' `source-commit.txt` verified): CONCLUSION
  `success` — BOTH JOBS GREEN.**
  - **5.10 — PASSED (third consecutive green): 177 tests, 0 failures, 0 errors, 2
    skipped.**
  - **5.11 — PASSED IN FULL; every section executed for the first time:**
    (a) python verification (`offlineVerified: true`, upgrade all-green, signing
    green) · **(b) transition `active default network: 101 → 100`, `OK: default
    network transitioned`** (run-5 parser fix verified by real execution) ·
    **(b) Data Saver** enable/disable with `cmd netpolicy` readback + `dumpsys netpolicy`
    both directions · **(c) process-death** force-stop→dead→relaunch→alive ·
    **(d) post-restart DB** `PRAGMA integrity_check: ok`, `foreign_key_check: none`,
    `user_version: 10` · **startup measurement** (workflow cold `TotalTime`
    2262/2408/2587/2445/2004 ms; python samples baseline ≈4.7 s vs candidate ≈2.3 s).
  - Evidence: `docs/planning/goldberg-0.11/stage-5/results/ci-run-39557624554/`
    (now 22 MB: **both full logcats committed in-repo** and SHA-256 verified
    byte-identical to the artifacts, plus per-test logcats, XML, DB, startup and
    `skipped-tests.md`).
- **Post-green session results (2026-10-21, all EXECUTED):**
  - **Evidence permanently preserved** — the attempt-6 bundle at
    `docs/planning/goldberg-0.11/stage-5/results/ci-run-39557624554/` is now 22 MB:
    both full logcats (SHA-256 verified byte-identical to the GitHub artifacts:
    acceptance `1d29f99c…70cdba`, instrumentation `c7c47039…630d9b`), per-test
    logcats for the consent-revocation/low-storage/worker-revocation tests, JUnit
    XML + counts + test-results log, `db-after-restart/opus.db` + integrity text,
    startup measurement, netpolicy/transition/upgrade evidence, and
    `skipped-tests.md`. Nothing needed remains only in 30-day artifacts.
  - **The 2 skipped tests are designed `assumeTrue` fixture skips**
    (`skipped-tests.md`): `Stage4ExternalProviderTest:17` + `Stage4ExternalPortraitTest:26`
    — separately-acquired licensed fixtures that are **never bundled into either
    APK** by policy; the skip set is identical across runs 3/4/5/6;
    `AppLanguageTest` ran and PASSED (9.2 s). Not failures, not masked (run-1 audit
    already proved 0 `@Ignore`/filters).
  - **5.11 coverage mapping: 2 of the 4 notebook/manual items were already EXECUTED
    on the emulator** as part of the 177-test suite — consent revocation mid-stream
    (`Stage5BundleJobRunnerTest::consentRevocationMidStreamPausesAndKeepsReadyContent`,
    revoke fired while bytes stream + worker/persistence variants, per-test logcats
    preserved) and low disk (`lowStorageRefusesBeforeAnyByteMoves` +
    backup-export/portrait-reserve variants).
  - **5.12 host gate GREEN:** `./gradlew clean assembleDebug testDebugUnitTest
    assembleDebugAndroidTest lintDebug --no-daemon --max-workers=1` → BUILD
    SUCCESSFUL (3 m 41 s); unit tests **174/0/0/0**; lint **0 errors / 110
    pre-existing warnings**; all 5 `scripts/check_*.py` PASS.
- **What remains for Stage 5 (truly open):** (1) **UI screenshots in both themes** —
  host emulator still blocked by the upstream SIGSEGV bug, so this needs a
  purpose-built CI job; (2) **real bundle-transfer disk/network costs** —
  structurally not measurable today: the product has **no production bundle
  feed/CDN URL and no production enqueue path** (`BundleScheduler` has no non-test
  caller; download UI is Stage 7); current measured evidence = the executed tests'
  exact byte accounting through the real engine + real file I/O + emulator
  `disk-usage.txt` snapshots. Both items await an owner decision (Issue #31).
  Then task 5.13 final documentation/handoff closes the stage. **Stage 5 stays IN
  PROGRESS until those pass** (owner's standing rule); no merge/tag/release/APK
  publication; Stage 6/7 not started; Home/Profile redesign requirements preserved.
- Active task: report CI-green status + evidence to the owner and proceed with 5.12/5.13
  planning per owner direction.

- **Attempt 5 results ([run 39552116455](https://github.com/stokonn/Goldberg/actions/runs/39552116455)
  @ `6da1021`, headSha + both jobs' `source-commit.txt` verified; evidence:
  `docs/planning/goldberg-0.11/stage-5/results/ci-run-39552116455/`):**
  - **5.10 — PASSED (second consecutive green): 177 tests, 0 failures, 0 errors, 2
    skipped.**
  - **5.11 — the python upgrade verification PASSED IN FULL, first time ever:**
    `offlineVerified: true` (offline reached at attempt 1 via `settings put global
    mobile_data 0`, held through the upgrade); in-place upgrade all-green
    (`originalRowsUnchanged`, `preferencesUnchanged`, 37 sessions, UUID bridge
    3/113, `foreignKeys: pass`); **startup measurement executed for the first time**
    (baseline ≈ 3.6–5.1 s vs candidate ≈ 2.2–2.6 s, small-sample caveat).
  - **Failed in section (b)'s host-side parser (first real execution):** the parser
    read the legacy `ni{…} type:` transport token; API 36 prints it only in
    `nc{[ Transports: … ]}`. 14 loop iterations prove **Wi-Fi reconnected and became
    the validated active default** — only the parser couldn't read it → 60 s deadline
    → exit. Not an app defect; proven from artifacts. Secondary defect: the deadline
    FATAL was swallowed by `$(…)`. Sections (c)/(d) + Data Saver still never executed.
- **Fix implemented + locally verified (workflow-only; assertions unchanged-strict):**
  parser reads `nc{[ Transports:` with `WIFI→WIFI`/`MOBILE→CELLULAR` mapping + legacy
  fallback; deadline FATAL to stderr. Verification EXECUTED: YAML parse OK,
  `bash -n` OK on both materialized scripts, **fixed parser vs the REAL run-5 dumps —
  5/5 checks** (2 positive, 2 negative-strict, 1 legacy fallback).
- Tasks 5.10–5.13 remain open; Stage 5 IN PROGRESS; Stage 6/7 not started.

- **Attempt 4 ([run 39518973833](https://github.com/stokonn/Goldberg/actions/runs/39518973833)
  @ `dcea5b2`, headSha + both jobs' `source-commit.txt` verified):**
  - **5.10 — EXECUTED AND PASSED: 177 tests, 0 failures, 0 errors, 2 skipped** on the
    API 36 emulator. All 13 run-3 fixes verified on-device (worker context, fixture
    bundleIds, URL routing, blob-before-promote, JUnit overload, product
    `settle()`/`readPublished()`/`locate()`). Evidence:
    `docs/planning/goldberg-0.11/stage-5/results/ci-run-39518973833/` (raw logs
    `4b92dc7a…ab28`; full 257-file set in GitHub artifacts, 30-day retention).
  - **5.11 — upgrade validation PASSED again; final offline assertion failed with
    root cause PROVEN from the 84 730-line acceptance logcat:** `svc wifi disable`
    took effect (logcat `WifiService … enable=false` @ 00:11:59.904) but **`svc data
    disable` left zero trace**, and the emulated CELLULAR network registered 00:12:15,
    became the active default 00:12:19, and served `com.opus.piano` throughout the
    "offline" upgrade. The assertion correctly caught a premise **never achieved** —
    not an app regression, not an assertion-format issue.
- **5.11 fix record (implemented `6da1021`, locally verified — details in
  STAGE-CHECKLIST + `docs/ai/VERIFICATION.md`):** `verify_emulator_upgrade.py`
  belt-and-suspenders `settings put global mobile_data 0` + 10×3 s verify/retry loop +
  `cmd connectivity airplane-mode enable` escalation, `dumpsys connectivity` saved to
  the artifact dir at every stage, fail fast with evidence if offline cannot be
  established before the upgrade; final assertion semantics **unchanged** (same
  regex/message, dump saved first); workflow section (b) clears airplane mode + re-enables
  data before the restores. Local verification EXECUTED: py_compile, YAML parse,
  `bash -n` on both materialized scripts, 3/3 stubbed-adb scenarios (retry success,
  airplane fallback, fail-fast before install), `check_agent_policy.py` PASS. The
  owner approved fix + verify + dispatch in the recovery session.
- **Update from attempt 6: everything previously unexecuted has now executed and
  passed** — (b)'s transition assertion + Data Saver, (c) process-death + cold restart,
  (d) post-restart DB integrity, startup measurement (all first real execution; see
  the current-status section + `results/ci-run-39557624554/`).

**Run history (details: STAGE-CHECKLIST + `results/ci-run-*/run-summary.md`):**
attempt 1 (run 39503450845) failed pre-test (dash `pipefail` + apksigner 37.0.0
format) — both root-caused with evidence; attempt 2 (run 39508388525 @ `5cdc0d6`,
after PR #33 merged as `a4a9509`) failed at the emulator step only — signing step
then passed with runner-side confirmation of the apksigner root cause
(`build-tools/37.0.0/apksigner`; release `53eface4…` = trusted, baseline = candidate =
runner key `47aebcfd…`), root cause proven from the action source (`parseScript`
single-line splitting) and fixed by the materialize-then-`bash <file>` design; attempt 3
launch: PR #34 merged workflow-only (`816d142`), dispatched against `9b80d65` — first
run to execute application tests (177 tests, 13 failures, all root-caused from code;
full triage: STAGE-CHECKLIST run-3 entry, `results/ci-run-39511602936/run-summary.md`,
Issue #31 checkpoint comments); attempt 4: fixes `dcea5b2` dispatched — **5.10 green**
(above); attempt 5: offline fix `6da1021` dispatched as
[run 39552116455](https://github.com/stokonn/Goldberg/actions/runs/39552116455) —
**5.10 green again; 5.11 python verification fully passed; failed in section (b)'s
host-side parser (format mismatch, proven from artifacts); parser fix + local
verification committed (`44645fa`) and dispatched as attempt 6
([run 39557624554](https://github.com/stokonn/Goldberg/actions/runs/39557624554),
owner-approved) — **FULLY GREEN (both jobs; all sections above)**.**
Historical note: the triage revealed 12 of 13 run-3 failures were deterministic
latent defects in androidTest-only tests that had never executed anywhere, 1 was a
genuine product bug (`settle()` STALE→QUEUED wedge), plus a `readPublished()` invariant
gap — all fixed in `dcea5b2` with local JVM verification (174/0) before dispatch.


## ⚠️ Emulator blocker on the desktop machine (2026-10-20, third session)

Task 5.10/5.11 are **blocked by a host-level Android emulator bug on this desktop**:

- This is a **different machine** than the notebook (Fedora, kernel
  `7.2.10-200.fc44.x86_64`, Java 25, 15 GiB RAM, no cmdline-tools, no stage5 AVD).
- The desktop's only pre-existing AVD (`Medium_Phone_API_37.0`, google_apis_playstore)
  and a freshly created notebook-style AVD (`stage5-api36`, google_apis API 36 x86_64,
  r07 — installed via newly added cmdline-tools 12.0) were both tried.
- **Every headless boot attempt ends in `Killed by SIGSEGV`** of the qemu process a few
  minutes into guest boot, right after the persistent
  `WARNING: cannnot unmap ptr … as it is in the protected range …` line. Configurations
  tried (all with explicit serial `emulator-5580`, never a physical device):
  API 37 + swiftshader ×2, API 36 + swiftshader (emulator 37.2.12), API 36 + `-gpu auto`
  (lavapipe/swangle, 37.2.12), API 36 + swiftshader (emulator **37.3.4** canary, newest
  published build, installed side-by-side at `~/Android/Sdk/emulator-37.3.4`). One
  API 37 run with lavapipe/swangle stayed alive 13 min but the guest never completed
  boot (`adb` stuck offline).
- This matches the known upstream bug **Google Issue Tracker #566807521** ("Android
  Emulator crashes with Segmentation fault during headless boot on modern Linux/Fedora",
  same protected-range warning, KVM present, independent of GPU option). Not fixed in
  37.3.4.
- Conclusion: **no emulator on this desktop can currently boot**; tasks 5.10/5.11 cannot
  be executed here. No instrumentation tests were executed. Possible paths (owner
  decision): run 5.10/5.11 on the notebook (its `stage5-api36` AVD booted fine), wait for
  an upstream emulator fix, or try a pre-37 emulator binary (not offered by the SDK
  channel; direct-zip install would need owner approval).

## Git / branch state

- Branch `feature/goldberg-0.11-stage-5`, parent `54f2b45` + Stage 5 checkpoint commits.
  **Pushed to origin as owner-authorized IN-PROGRESS checkpoints** (verification
  incomplete — see VERIFICATION.md for what remains unexecuted). No merge/tag/release.
  The third-session checkpoint is **docs-only** (this file, HANDOFF, VERIFICATION,
  STAGE-CHECKLIST): it records the desktop emulator blocker + host-gate evidence and
  adds no production/test code changes.
- In the second-session checkpoint commit (`c61cf12`):
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

## Executed this session (2026-10-20, third resume session — desktop) — real results

1. Environment re-verified: branch `feature/goldberg-0.11-stage-5` at `c61cf12`, in sync
   with origin; untracked private files (`.pmp/`, `.pmp.yml`, `RELEASE_NOTES.md`,
   `docs/planning/goldberg-0.11/stage-4/checkpoint-archive/`) untouched and NOT committed.
2. **EXECUTED** `./gradlew --no-daemon --max-workers=1 :app:assembleDebug
   :app:assembleDebugAndroidTest` → **BUILD SUCCESSFUL** (1m08s, Java 25 host; only the
   known nullable-`listFiles()` warnings). Fresh APKs: `app-debug.apk` 32,388,569 bytes
   (sha256 `44222c59b89ed63041d1e105f074857f5ca3c2f93efef4a9ed0eea115dd0a157`),
   `app-debug-androidTest.apk` 1,331,040 bytes (sha256
   `6030e528474334ae31f0514c0f818aeeb68dcf3f9b2ac35e453762d1e5ba8f2d`), both Oct 20 21:13.
   (These were rebuilt because the desktop's pre-existing outputs were from a different
   build context; they are Stage-5 source-faithful.)
3. Emulator toolchain: cmdline-tools 12.0 installed (side-effect: fixes sdkmanager/
   avdmanager on this desktop), `system-images;android-36;google_apis;x86_64` r07
   installed, AVD `stage5-api36` created in `~/.android/avd`. Emulator 37.3.4 downloaded
   side-by-side (SDK's 37.2.12 untouched). **All boot attempts SIGSEGV — see blocker
   section above. Zero instrumentation executed; APKs not installed (no device to install
   to; the earlier installs were on the notebook's now-unavailable emulator).**
4. **EXECUTED** `scripts/check_agent_policy.py`, `check_backup_fixtures.py`,
   `check_history_paging.py`, `check_localization.py`, `check_migrations.py` → **ALL PASS**
   (exit 0; see VERIFICATION.md row detail).
5. **EXECUTED** full host gate
   (`clean assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug`) →
   **BUILD SUCCESSFUL** (3m24s): unit tests **174/0/0/0** (30 classes), lint
   **0 errors / 110 warnings** (2 in Stage 5 offline code). Clean-build `app-debug.apk`
   22,508,542 B — same byte size as the notebook checkpoint APK (source-identity
   evidence). Task 5.12 host-side portion therefore executed and passing; 5.12 stays
   open only as a formality until 5.10/5.11 evidence exists for the final re-run.
6. **GUI-mode emulator attempt (owner-requested, one controlled configuration)** —
   first launch failed before QEMU started (tool shell lacked `XAUTHORITY`); relaunched
   with the session's `/run/user/1000/.mutter-Xwaylandauth.*` → Qt window opened on the
   GNOME desktop, then **`Killed by SIGSEGV` again** after the protected-range warning.
   GUI mode does not avoid the bug → **all local emulator retries stopped** (owner
   instruction). Details in VERIFICATION.md row 11.
7. **CI investigation + workflow prepared (not yet committed/pushed — pending owner
   approval)** — GitHub-hosted `ubuntu-24.04` runners support KVM-accelerated Android
   emulation (GitHub Changelog 2024-04-02) and
   `reactivecircus/android-emulator-runner` v2.38.0 is current. Prepared
   `.github/workflows/stage5-emulator.yml`: **`workflow_dispatch` only** (no automatic
   runs), `permissions: contents: read`, SHA-pinned actions, KVM enable step, explicit
   `emulator-NNNN` serial + `ro.kernel.qemu=1` guard with abort-on-multiple-devices.
   Job 5.10 runs the **complete** `connectedDebugAndroidTest` suite (no selection/
   skipping). Job 5.11 runs the CI-executable acceptance subset: digest-verified
   in-place upgrade from trusted 0.10.1 (script-asserted), Wi-Fi→mobile + Data Saver
   evidence, process death + cold restart with DB integrity check, 5× startup
   measurements, failure logcat capture. Explicitly NOT covered (notebook follow-up):
   consent revocation mid-stream, low disk, screenshot inspection, real bundle
   transfer costs. YAML validated (`yaml.safe_load` OK); helper scripts compile.

## Executed — second resume session (2026-10-20, battery emergency stop) — real results

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

- **All `Stage5*Test` instrumentation (tasks 5.10/5.11) — still zero tests executed.**
  The desktop emulator path stays blocked by the upstream SIGSEGV bug; the CI path ran
  once (run 39503450845) but both jobs failed pre-test from CI setup/script defects
  (see current-status section). No instrumentation XMLs exist yet anywhere.
- `lintDebug` is part of the running host gate (task 5.12) — see VERIFICATION.md.
- Emulator acceptance matrix (Wi-Fi→mobile, Data Saver, consent revocation mid-stream, real
  process death, low disk, seeded in-place upgrade from trusted 0.10.1, cost measurements).
- No screenshots, no physical phone (forbidden), no merge/tag/release (forbidden).

## Environment

Desktop (third session): Arch-notebook environment superseded — Fedora host, kernel
`7.2.10-200.fc44.x86_64`, Java 25 (Gradle 9.3.1 runs fine), 15 GiB RAM, SDK at
`~/Android/Sdk` (android-36 + android-37.0/37.2 images, build-tools 36.0.0, cmdline-tools
12.0, emulator 37.2.12 + side-by-side 37.3.4), AVDs in `~/.android/avd`
(`stage5-api36` google_apis API 36, `Medium_Phone_API_37.0` playstore). `/tmp` is a
small tmpfs (7.8 GiB) — do not unzip large archives there. All builds
`--no-daemon --max-workers=1`. All ADB calls require explicit `-s emulator-NNNN`.

## Prohibitions (still active)

No physical phone / implicit ADB device; explicit `emulator-NNNN` serial only. No push, tag,
merge, release or APK publication without separate authorization. Do not mark Stage 5 tasks
5.10–5.13 complete; do not start Stage 6. Never weaken test assertions; never fabricate results.
