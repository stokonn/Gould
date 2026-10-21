# Goldberg 0.11 — Stage 5 HANDOFF (fresh-session resume instructions)

> Created 2026-10-20; updated at the end of the post-green session (2026-10-21).
> Stage 5 is **in progress, incomplete**. The branch HEAD is a checkpoint commit on top of
> `54f2b45`, pushed to origin as an owner-authorized IN-PROGRESS checkpoint. Nothing was
> merged, tagged or published. **Tasks 5.10 (instrumentation), 5.12 (host gate) are
> COMPLETE with executed evidence; 5.11 is executed-and-green except two genuinely-open
> items (UI screenshots in both themes; real bundle-transfer costs — structural blocker)
> that await an owner decision; 5.13 (final docs/handoff) is the closing task.** The CI
> path (workflow_dispatch on `ubuntu-24.04` with KVM) is the primary execution vehicle
> because the desktop's host emulator is blocked by upstream bug
> [Google Issue Tracker #566807521](https://issuetracker.google.com/issues/566807521).

## Read order (in this order, before touching code)

1. `AGENTS.md` — permanent policy (data safety, no physical phone, publication gates).
2. `docs/ai/STATE.md` — current state, executed evidence, emulator blocker record.
3. `docs/ai/VERIFICATION.md` — executed vs. not-executed ledger.
4. `docs/ai/DECISIONS.md` — owner decisions, incl. approved F1/F2 resolutions.
5. `docs/planning/goldberg-0.11/STAGE-CHECKLIST.md` — Stage 5 section (authoritative list).
6. GitHub Issue #31 — latest checkpoint comment (do not duplicate issues).

## What the checkpoint contains (all prior work preserved)

- Everything from the previous checkpoint (`c61cf12`): F1/F2 fixes, compile-green
  `Stage5BundleJobRunnerTest`/`Stage5BundleWorkerTest`, recovery docs.
- This session's additions: `docs/ai/*` + STAGE-CHECKLIST updated with the **desktop
  emulator blocker** (Google Issue #566807521) and the emulator-independent host
  verification (Gradle host gate + all five `scripts/check_*.py`).
- `.pmp/`, `.pmp.yml`, `RELEASE_NOTES.md` and
  `docs/planning/goldberg-0.11/stage-4/checkpoint-archive/` are private/unrelated —
  never commit them.

## Verified state at this checkpoint (executed — see VERIFICATION.md for exact rows)

- `:app:assembleDebug :app:assembleDebugAndroidTest` **BUILD SUCCESSFUL** (desktop, Java 25
  host, 1m08s, 0 errors). Fresh APKs:
  `app-debug.apk` 32,388,569 B sha256 `44222c59b89ed63041d1e105f074857f5ca3c2f93efef4a9ed0eea115dd0a157`;
  `app-debug-androidTest.apk` 1,331,040 B sha256
  `6030e528474334ae31f0514c0f818aeeb68dcf3f9b2ac35e453762d1e5ba8f2d` (Oct 20 21:13).
- Host gate (`clean assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug`)
  + `scripts/check_*.py` — results recorded in VERIFICATION.md (all executed on the
  desktop this session).
- **Zero instrumentation tests executed anywhere** (emulator blocker below).

## Emulator blocker on the desktop (why 5.10/5.11 were not done here)

Every headless emulator boot on the desktop (Fedora kernel `7.2.10-200.fc44`) dies with
`Killed by SIGSEGV` after `WARNING: cannnot unmap ptr … protected range …`, on both
AVDs (`Medium_Phone_API_37.0` playstore and fresh `stage5-api36` google_apis API 36) and
both emulator versions (37.2.12 SDK + 37.3.4 canary side-by-side), across
swiftshader/auto(lavapipe) GPU modes. This is upstream bug **Google Issue Tracker
#566807521**; unfixed as of 37.3.4. Details in `docs/ai/STATE.md`.

## Exact resume steps for tasks 5.10/5.11 — run on the NOTEBOOK

The notebook has the previously working toolchain (emulator 37.2.12 + API 36 google_apis
x86_64 image + AVD `stage5-api36`, AVD home redirected). On the notebook:

1. Confirm the tree is the pushed Stage 5 branch at the checkpoint commit:
   `git status` (only `.pmp/`, `.pmp.yml`, `RELEASE_NOTES.md`,
   `stage-4/checkpoint-archive/` untracked), `git log --oneline -2`.
2. Rebuild **fresh** APKs there (the notebook's build outputs are from an older source
   state — do NOT reuse them):
   `./gradlew --no-daemon --max-workers=1 :app:assembleDebug :app:assembleDebugAndroidTest`
   and verify timestamps/hashes before installing (the stale-APK lesson from the battery-
   emergency session).
3. Boot the notebook emulator (AVD home redirect is mandatory there):
   `ANDROID_AVD_HOME=/home/stokonn/.config/.android/avd "$ANDROID_HOME/emulator/emulator"
   -avd stage5-api36 -port 5580 -no-window -no-audio -no-boot-anim -gpu swiftshader_indirect
   -no-snapshot -memory 2048 &`
   Keep the process alive across tool calls (managed/background shell, not a bare nohup —
   the desktop session lost nohup'd processes). Wait for
   `adb -s emulator-5580 shell getprop sys.boot_completed` = 1 and verify
   `ro.kernel.qemu`=1. **Every ADB call must use `-s emulator-5580`; never a physical phone.**
4. Install the fresh APKs explicitly:
   `adb -s emulator-5580 install -r app/build/outputs/apk/debug/app-debug.apk`
   `adb -s emulator-5580 install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk`
5. **Task 5.10** — run instrumentation explicitly, per Stage5 class with real counts:
   - `adb -s emulator-5580 shell am instrument -w -e class com.opus.piano.Stage5BundleStoreTest com.opus.piano.test/androidx.test.runner.AndroidJUnitRunner`
   - `adb -s emulator-5580 shell am instrument -w -e class com.opus.piano.Stage5BundleJobRunnerTest com.opus.piano.test/androidx.test.runner.AndroidJUnitRunner`
   - `adb -s emulator-5580 shell am instrument -w -e class com.opus.piano.Stage5BundleWorkerTest com.opus.piano.test/androidx.test.runner.AndroidJUnitRunner`
   Record exact OK/Failure counts. Investigate any failure against the F1/F2 contracts
   (DECISIONS.md); never weaken assertions.
6. **Task 5.11** — emulator acceptance matrix on the same emulator: Wi-Fi→mobile
   transition, Data Saver, consent revocation mid-stream, real process death at
   boundaries, low disk, seeded in-place upgrade from trusted 0.10.1, disk/network cost
   measurements. Stage 4 helpers: `scripts/emulator_ui.py`,
   `scripts/verify_emulator_upgrade.py`, `scripts/measure-startup.sh`.
7. **Task 5.13** — update `docs/ai/*` + STAGE-CHECKLIST with executed evidence, commit,
   push to the Stage 5 branch, sync Issue #31 + Project. **Do not mark 5.10–5.13 complete
   without executed evidence. Do not start Stage 6; no tag/merge/release.**
8. PMP: local `.pmp/`/`.pmp.yml` on the desktop were **preserved as-is, never
   reinitialized**. Leave them private.

Alternative 1 (CI, current primary path): `.github/workflows/stage5-emulator.yml`
(`workflow_dispatch` only, KVM-accelerated `ubuntu-24.04`, SHA-pinned) runs the full
connected suite for 5.10 and the CI-executable 5.11 subset.
**Status as of 2026-10-21 (supersedes the run-1 paragraph below, kept as history):**
attempts 1–4 dispatched from `feature/goldberg-0.11-stage-5`; attempt 4
([run 39518973833](https://github.com/stokonn/Goldberg/actions/runs/39518973833) @
`dcea5b2`) delivered **5.10 PASSED (177/0/0/2 — all 13 earlier failures fixed and
verified on-device)**; 5.11's upgrade validation passed again but its offline premise
was proven never achieved (`svc data disable` no-op on the API 36 CI emulator — see
`results/ci-run-39518973833/run-summary.md`). The evidence-preserving 5.11 offline fix
was implemented + verified at `6da1021`; attempt 5
([run 39552116455](https://github.com/stokonn/Goldberg/actions/runs/39552116455) @
`6da1021`) delivered **5.10 green again (177/0/0/2)** and the **full 5.11 python
upgrade verification passed for the first time** (`offlineVerified: true`, upgrade
all-green, startup measured) but failed in section (b)'s host-side parser (API 36
prints the transport in `nc{[ Transports: …]}`, not the legacy `ni type:` — proven
from artifacts, not an app defect; see `results/ci-run-39552116455/run-summary.md`).
The parser fix is implemented + locally verified **5/5 against the real run-5 dumps**;
the owner approved attempt 6
([run 39557624554](https://github.com/stokonn/Goldberg/actions/runs/39557624554) @
`44645fa`) which is **FULLY GREEN (both jobs; all acceptance sections first executed
and passed — see `results/ci-run-39557624554/run-summary.md`). The CI verification for
5.10/5.11 is green end-to-end; remaining Stage 5 work is notebook/manual-only (5.12/5.13),
so Stage 5 stays IN PROGRESS.**
**Post-green session (2026-10-21, all executed, branch @ `f9f59d1` + docs commits):**
attempt-6 evidence permanently preserved in-repo (22 MB bundle: both full logcats
SHA-verified, per-test logcats, XML, DB, startup, `skipped-tests.md`); the 2 skipped
tests root-caused as designed `assumeTrue` fixture conditions (licensed fixtures never
bundled into either APK; skip set identical runs 3–6; `AppLanguageTest` ran+passed);
5.11 coverage mapping proved **consent revocation mid-stream + low disk were already
executed on the emulator** inside the 177-test suite; **5.12 host gate GREEN**
(gradle BUILD SUCCESSFUL 3 m 41 s, unit 174/0/0/0, lint 0 errors/110 warnings, all 5
`scripts/check_*.py` PASS). What remains: **UI screenshots in both themes** (needs a
purpose-built CI job — host emulator blocked) and **real bundle-transfer costs**
(structural blocker: no production bundle feed/CDN URL and no production enqueue path
in the product; measured byte accounting + real file I/O + `disk-usage.txt` are the
current evidence) — both awaiting an owner decision on Issue #31; then 5.13 finalizes.
See `docs/ai/STATE.md` for the live checkpoint. (Historical: the first dispatch, run
39503450845, failed pre-test in both jobs from CI setup/script defects — not application defects (owner-confirmed; zero application
tests ran; evidence:
`docs/planning/goldberg-0.11/stage-5/results/ci-run-39503450845/`): (1) the
emulator-runner invokes its script with `/usr/bin/sh` (dash), which rejects
`set -o pipefail`; (2) apksigner's print format changed in build-tools 37.0.0
(preinstalled on runners; selected by `sort -V | tail -n1`), so the old sed pattern
matched nothing — the `MISSING` digests were a parser failure, **not** a certificate
mismatch. Minimal workflow-only corrections (bash heredoc wrapper + full
apksigner-diagnostics capture + dual-format parsing, assertions unchanged/strict) are
validated on the host and staged; those workflow-only fixes were merged via PRs #32–#34
(merge commits `577d811`, `a4a9509`, `816d142`) and runs 3–4 executed application
tests. Consent revocation mid-stream and low disk are now covered by executed
emulator instrumentation (see post-green status above); only screenshot inspection
and real bundle-transfer costs remain open, both pending owner decisions.

Alternative 2: if an upstream emulator fix for #566807521 ships (watch the issue), the
desktop's `stage5-api36` AVD + emulator 37.3.4 side-by-side install are already prepared
(`~/Android/Sdk/emulator-37.3.4/emulator -avd stage5-api36 -port 5580 …`).

## Hard prohibitions (unchanged)

No physical phone; no implicit device selection; no merge/tag/release/APK publication
without separate authorization; no weakened test assertions; no fabricated results; no
Stage 5 "complete" claim; no Stage 6 start. Never commit `.pmp/`, `.pmp.yml`,
`RELEASE_NOTES.md` or the stage-4 checkpoint archive.

## Environment

Desktop: Fedora, kernel `7.2.10-200.fc44.x86_64`, Java 25, 15 GiB RAM, Android SDK at
`~/Android/Sdk` (android-36 platform + build-tools 36.0.0 + cmdline-tools 12.0 +
emulator 37.2.12 and side-by-side 37.3.4), AVDs in `~/.android/avd`. Gradle 9.3.1,
`org.gradle.jvmargs=-Xmx3g`; always `--no-daemon --max-workers=1`. `/tmp` is a small
tmpfs — keep large archives in `~/.cache`.
Notebook (for 5.10/5.11): Arch Linux, Java 17, emulator 37.2.12, AVD `stage5-api36` with
`ANDROID_AVD_HOME=/home/stokonn/.config/.android/avd`.

created by stokonn with love <3
