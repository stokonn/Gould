# Goldberg 0.11 — Stage 5 STATE (recovery checkpoint)

> **SUPERSESSION NOTE (2026-10-21, Stage 6 planning branch):** frozen historical
> checkpoint from before Stage 5 completion. **Stage 5 is COMPLETE and owner-approved
> (2026-10-21)** — CI runs 39557624554 (177/0/0/2) and 39576173002 (12/12 screenshots);
> the authoritative record is on `feature/goldberg-0.11-stage-5` (completion commit
> `736f6d3`). Everything below is retained as historical evidence only.

> **Stage 6 addendum (2026-10-21, later session — Stage 6 planning branch,
> documentation-only):** C2 spike executed → **CONDITIONAL PASS**
> (`spike/goldberg-0.11-c2-bff`, final commit `7bc6976`, spike code commit
> `d2ce77b`; 19/19 security, P1–P7 executed, kernel CPU **2.03
> ms/invocation**; per-invocation production CPU **unverified**; §12
> production-runtime verification **owner-gated**; spike code unmerged).
> C3 edge-privacy documentary review **complete**
> (`docs/planning/goldberg-0.11/stage-6/C3-EDGE-PRIVACY-REVIEW.md`: conditional
> privacy viability at 0 CZK, **no compliance claim**, legal/operator questions
> open — controller/operator identity incl. the minor-owner legal-capacity
> question is now blocking for 6.0d). C4 open; **task 6.0f unchecked; Stage 6
> implementation NOT STARTED**; nothing provisioned or deployed. Authoritative
> current state: `docs/planning/goldberg-0.11/stage-6/CHECKLIST.md` (progress
> log) + GitHub Issue #31 checkpoints.

> **Last updated:** 2026-10-20, CI-activation session (Phase A/B of the owner's CI plan;
> approval gate reached — nothing dispatched or merged).
> Read order for a fresh session: `AGENTS.md` → this file → `docs/ai/HANDOFF.md` →
> `docs/planning/goldberg-0.11/STAGE-CHECKLIST.md` (Stage 5 section) → GitHub Issue #31.
> **Stage 5 is NOT complete. No merge/tag/release/APK publication performed.**

## 🚦 Current status: awaiting owner approval (Phase C gate)

- The Stage 5 emulator verification workflow was **revised and CI-only PR prepared**:
  draft [PR #32](https://github.com/stokonn/Goldberg/pull/32), branch
  `ci/stage5-emulator-verification` (based on `origin/main`), commit `9870d99` —
  **workflow-only diff (1 file), NOT merged**.
- **Phase B findings fixed in the workflow** (details in `docs/ai/VERIFICATION.md`
  CI-activation section): in-place upgrade now uses a same-keystore debug rebuild of
  the v0.10.1 tag (CI debug can never install over the release-signed APK; trusted cert
  `53eface4…` apksigner-verified); the invalid `cmd netpolicy set data_saver enable`
  was replaced with AOSP-verified `set/get restrict-background` + `dumpsys` state
  assertions; Wi-Fi→mobile transition now asserts transport + network-ID change;
  process death now includes pulled-DB `PRAGMA integrity_check/foreign_key_check/
  user_version`; source commit SHA recorded+asserted in both jobs; 30-day evidence
  retention. The identical revised workflow is committed on this Stage 5 branch too
  (so a `workflow_dispatch` against `feature/goldberg-0.11-stage-5` uses the fixed
  version).
- **STOPPED here per owner instruction:** merging PR #32 into `main` and manually
  dispatching the workflow both require explicit owner approval. Until then: no CI run,
  no instrumentation, no 5.11 evidence. Tasks 5.10–5.13 remain open.
- This checkpoint commit contains **only** the revised `.github/workflows/stage5-emulator.yml`
  (identical to PR #32's file) and documentation sync (`docs/ai/STATE.md`,
  `docs/ai/VERIFICATION.md`, `docs/ai/HANDOFF.md`,
  `docs/planning/goldberg-0.11/STAGE-CHECKLIST.md`) — **no application/test source
  changes**.

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

- **All `Stage5*Test` instrumentation (tasks 5.10/5.11) — still zero tests executed; now
  blocked by the desktop emulator bug (see blocker section). The fresh APKs were built
  but not installed anywhere.**
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
