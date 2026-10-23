## Stage 7B preserved checkpoint and CI continuation — 2026-10-23

**Stage7B IN PROGRESS; acceptance incomplete.** Source/evidence checkpoint
`0703cd8ec40a86e6f92e7fd9cf172c5921e1e90e` committed and ordinary-pushed; exact
remote SHA readback PASS. Initial cee9330 +26 modified tracked /125 publishable new
files preserved; private PMP excluded. Fresh JVM **275/0/0/0**,36 suites,12s;
assembly/lint gate SUCCESS (mostly cached), six host scripts and syntax checks PASS.
No app-source/contract/private PMP/prior-evidence bytes changed during recovery.

[API36 CI run40011091868](https://github.com/stokonn/Goldberg/actions/runs/40011091868)
FAILED before build/tests at this exact SHA: emulator executable absent during
pre-build diagnostics (exit127). Zero test bodies; no application failure. Narrow
verified fix explicitly installs emulator before diagnostics. Corrected commit
`3e04e2b716435684a0d4d88a71a4a59407201ada` pushed with exact remote SHA readback;
[run40011450671](https://github.com/stokonn/Goldberg/actions/runs/40011450671)
FAILED before build/tests: installed emulator lacks libpulse.so.0 (exit127).
Second narrow CI fix installs libpulse0 before diagnostics. Commit
`5d3f0ccd03411666ff741ccc894bc85ad98cd280` pushed and remotely verified;
[run40011713646](https://github.com/stokonn/Goldberg/actions/runs/40011713646)
passed dependency/KVM and fresh host gate7m37s (275/0/0/0), then FAILED
contract provenance because shallow checkout lacked bcc7796; zero instrumentation.
Targeted immutable fetch reproduced PASS in disposable repo; corrected CI pending. Issue31 dashboard updated (connector403; authenticated gh succeeded),
umbrella OPEN / Project In Progress; final chronological checkpoint awaits results.
See [SAFE-RECOVERY.md](../planning/goldberg-0.11/stage-7/stage-7b/SAFE-RECOVERY.md) for scope/tasks/audit/commands/artifact identity.

Stage7A finally owner-accepted; X10 NOT FIXED/NOT VERIFIED, owner-deferred Gould0.12.
Stage8 material readability/accessibility/touch targets remain required. Existing
historical summaries below are superseded for current status/authorization only.
Production networking/decisions DISABLED; G1–G9/G11 integration and Admin visual/
accessibility/WorkManager/notification/upgrade/owner acceptance remain pending.

Native test-only isolation fix found by inspection: return the wrapper itself from
applicationContext so stores and tamper assertions use the same disposable prefs.
All assertions retained; compilation SUCCESS17s. Corrected runtime still pending.
Original test preserved in0703cd8; application/contracts/PMP/prior evidence unchanged.

---

## Stage 7B contract preparation — recovered verified checkpoint (2026-10-23)

**Current status: local preparation verified; Stage 7B acceptance remains IN PROGRESS / BLOCKED.**
Branch `feature/goldberg-0.11-stage-7b`, HEAD `cee9330f82cf2e57dcdc66a63d94b7a4e3749888`
plus preserved uncommitted continuation. No application code was changed by this
recovery session. Stage 6 trees untouched; no commit/push, merge, release or deploy.
`.pmp/` and `.pmp.yml` remain private/untracked. This entry supersedes older current-status
summaries below; historical checkpoints and earlier counts remain audit history.

**Purpose/scope:** finish the interrupted API contract integration preparation by
recovering source/evidence, retaining supported Android safety changes, and making
contract decisions explicit without inventing or activating a backend mapping.

- [x] Read policy, PMP sequence-3 audit checkpoint, AI handoffs, Stage 7B checklist /
  acceptance, latest Issue #31 comments, branch/status/diffs and existing evidence.
- [x] Recover immutable `bcc7796` audit and `faf9ba3` comparison; 29 synthetic cases
  and 15 pinned hashes. Normative design, legacy C6, local candidate and inactive
  mapping proposal stay separate. No Stage 6 backend tests executed here.
- [x] Retain already implemented application-disabled transport even with a saved
  endpoint/token; independent decision disable; strict capability principal/string
  validation, exact legacy Int revisions and Long audit sequences, cursor type
  rejection, typed malformed-payload errors. No candidate dialect adapter enabled.
- [x] Preserve recovered successful Gradle outputs: 36 suites / **275 JVM tests,
  0 failures/errors/skips**, including **24 AdminContractIntegrationTest cases**.
  `./gradlew testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug
  --no-daemon --max-workers=1` completed in 2m26s; retained follow-up 13s pass.
  These are **prior-session executions inspected now**, not new JVM executions.
  Initial two capability-test failures remain preserved and are superseded by
  the passing reports after their pre-interruption fix.
- [x] Lint recovered: **0 errors/fatal, 132 warnings, 1 hint**. Instrumentation APK
  compiled/assembled only; **0 instrumentation bodies executed**.
- [x] Recovery session executed all six host scripts: contract fixtures/provenance,
  agent policy, backup formats 1–3, history paging, localization (1050 EN/CZ), and
  migrations v1–9→10 DDL; all PASS. `git diff --check` PASS.
- [x] Recovery session verified APK signing, badging, ZIP integrity and exclusion of
  test corpus. APK `app/build/outputs/apk/debug/app-debug.apk`, 24,140,673 bytes,
  SHA-256 `6976d76e9bd936cd5b45e26f1910f86c3105863a8fe2bbf6f1269aaafc7e269b`;
  `com.opus.piano`, development version **0.10.1/code 11**; trusted signer
  `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`.
  No application source file newer than APK; test-only corpus files are newer.
  Source manifest reconstructed on recovery, not contemporaneous build/CI proof.
- [x] Recover bounded emulator investigation: read-only `stage5-api36`, explicit
  intended serial `emulator-5582`, no snapshot/wipe; **SIGSEGV / exit -11 after
  21.68s**. This is host setup failure, not application evidence. No new launch,
  ADB operation or physical device use in recovery; no screenshots inspected.
- [ ] Stage 6/owner contract approval and actual Android/backend integration.
- [ ] Functioning permitted emulator; rendered/no-scroll screenshots, accessibility,
  WorkManager/notification runtime, seeded upgrade and owner acceptance.

**Unresolved G1–G6:** G1 server stats; G2 server queue search/display labels;
G3 audit server filtering/search; G4 verified current catalogue before-values;
G5 live credential/session/principal lifecycle; G6 explicit open + two revision
expectations and result/conflict/replay HTTP semantics. All remain OPEN. Existing
bounded exact-or-unavailable/client-side fallbacks do not close server gaps.
Additional G7 public DTO/cursor/HTTP binding, G8 numeric representation, G9 audit
vocabulary/time windows and G11 validation/reason/license policy remain OPEN.
**G10 local Android production networking boundary is closed**; this is no backend
approval. Production networking and moderation decisions remain DISABLED. X10 OPEN.
Personal database/schema/backup/practice data unchanged.

**Next milestone:** obtain explicit approval of versioned shared public
session/capability/queue/detail/open/dual-revision/audit/error/validation fixtures;
then implement one approved adapter and run isolated integration tests. Obtain a
working allowed emulator venue before executing the remaining acceptance matrix.
Do not select a dialect heuristically, default missing expectations, revive
networking from an endpoint setting or retry the known crashing emulator unchanged.

Evidence: `docs/planning/goldberg-0.11/stage-7/stage-7b/results/contract-preparation-2026-10-23/`
(recovered raw logs/XML, failure history, emulator diagnostics, source hashes,
artifact identity and recovery host checks). [Recovery Issue checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6368949572). Final PMP checkpoint is read through
`pmp resume` / `pmp history`; native PMP test-count evidence is not fabricated from
recovered JUnit outputs. Issue #31 remains OPEN; Project remains In Progress.

---

## Historical checkpoints (superseded for current recovery status)

> Stage 7B contract-preparation audit milestone (2026-10-23): PMP
> `3dc24493-ea5c-4b2a-8338-fd5d1bef4819`, source candidate `bcc7796` read-only;
> 29 synthetic comparison fixtures/provenance checked. G1–G9 remain integration
> decisions; dual-revision mapping unapproved/inactive. Supported Android safety
> changes now in progress; 251 tests below are prior-run evidence until rerun.
> [Audit Issue checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6365519381).

## PMP synchronization — 2026-10-23

PMP recovery GitHub checkpoint: [6365022849](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6365022849).


Recovered using installed PMP 1.0.0a1 and the existing Goldberg project identity
`ac3a7942-cde5-4e79-8a43-e1a0d2c7df81`. Current task: `goldberg-0.11-stage-7b-continuation`;
phase/status **verifying / blocked** (project acceptance blockers, not failed
metadata synchronization). Checkpoint **`d4d4ef74-7a0f-4282-86eb-7a85e455a6d0`**, worktree sequence 1.
Private generated handoff: `/home/stokonn/.local/state/pmp/ac3a7942-cde5-4e79-8a43-e1a0d2c7df81/2cb2d7ab23b3ae74d4165d8c/HANDOFF.md`.

Readback through `pmp status`, `pmp resume`, `pmp history`, `pmp check` and
`pmp verify` succeeded: current checkpoint/source fingerprint agree; no recovery
uncertainty or damaged records; `check` valid with no errors/warnings. `.pmp/` and
`.pmp.yml` are local/untracked; no export/commit/push. All 37 pre-existing PMP files
(including original primary-checkout configuration/state) and 268 application
files are byte-unchanged. Old Stage 5 records remain under their original worktree
identity; they are not the current Stage 7B resumption target.

Current project facts: **Stages 1–5 complete on their respective branches; Stage 6
local prototypes exist but formal production/integration gates OPEN; Stage 7A
conditionally accepted/X10 OPEN; Stage 7B substantially implemented with prior
251 passing JVM tests, emulator/runtime/integration acceptance BLOCKED; Stage 8
pending.** No acceptance or release status promoted. Production decisions DISABLED.

The 251-test result, lint 0 errors/132 warnings and host checks are preserved
prior-run facts linked to existing reports and Issue #31 checkpoint 6362810877.
PMP-native `verification` remains empty and `last_verified_commit` null because
this documentation task did not execute or import new application-test evidence.
Metadata validation must not be described as 251 tests executed again.

Handoff target: `/home/stokonn/Projects/Goldberg-Stage7B`, branch
`feature/goldberg-0.11-stage-7b`, HEAD `cee9330` plus preserved uncommitted
continuation; read the current Stage 7B CHECKLIST/ACCEPTANCE and Issue #31.
Next: coordinate frozen Stage 6 wire fixtures without changing its tree; obtain a
working permitted emulator venue and execute the unchecked visual/runtime/upgrade
gates only under appropriate scope. No physical phone, production deploy, merge
or release. Recovery scope/method: [PMP-RECOVERY.md](PMP-RECOVERY.md).

---

> Current Stage 7B verified checkpoint — 2026-10-23. Worktree:
> `/home/stokonn/Projects/Goldberg-Stage7B`, branch `feature/goldberg-0.11-stage-7b`,
> HEAD `cee9330` + uncommitted continuation. Preserve all work; no Stage 6 tree edits.
> Queue/review/conflict/audit/consent implementation and acceptance packet prepared.
> Final host gate PASS: **251 JVM tests, 0 failures/errors/skips**, APK + instrumentation
> compilation, lint **0 errors/132 warnings**, 5 host scripts; EN/CZ **1050 resources**.
> Emulator/no-scroll/screenshots/runtime/upgrade gates BLOCKED/UNEXECUTED; X10 OPEN.
> Live decisions DISABLED pending G5/G6 session and dual-revision wire fixtures;
> G4 current catalogue values unavailable. No physical device/deploy/merge/release.
> Read [Stage 7B checklist](../planning/goldberg-0.11/stage-7/stage-7b/CHECKLIST.md),
> [contract gaps](../planning/goldberg-0.11/stage-7/stage-7b/CONTRACT-AUDIT.md),
> [acceptance/next steps](../planning/goldberg-0.11/stage-7/stage-7b/ACCEPTANCE.md)
> and [final Issue #31 checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6362810877) first. Stage 5 notes below are historical.

# Goldberg 0.11 — Stage 5 HANDOFF (fresh-session resume instructions)

> Created 2026-10-20; updated at the umbrella tracking correction (2026-10-21).
> **Umbrella tracking corrected:** Issue #31 is the release-wide 0.11 umbrella
> (Stages 1–8), reopened and set to **In Progress** — it is NOT Stage-5-only and
> must stay open until 0.11 ships. Stage 5 remains **COMPLETE + owner-approved**
> and its checklist entry stays **Done**.
> **STAGE 5 IS COMPLETE — owner-approved 2026-10-21** on the verified CI
> evidence (5.10 run 39557624554; 5.11 run 6 + green screenshot run
> 39576173002 + owner-dispositioned cost measurement; 5.12 host gate; 5.13
> docs). GitHub Project item #31 = Done; Issue #31 keeps the audit trail.
> A next session must NOT assume merge/tag/release authority: application code
> stays unmerged and Stage 6/7 must not start without a fresh owner decision.
> Carried-forward dependency: real bundle-transfer costs await a production
> bundle feed (Stage 6/7; DECISIONS.md). Nothing was merged, tagged or
> published. The CI path (workflow_dispatch on `ubuntu-24.04`
> with KVM) is the primary execution vehicle because the desktop's host emulator
> is blocked by upstream bug
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
`scripts/check_*.py` PASS).
**Screenshot session (2026-10-21, owner-approved "build + dispatch"):** workflow
`.github/workflows/stage5-screenshots.yml` merged workflow-only (PR #35); three
attempts failed on three distinct *script* defects — theme write before first
launch (PR #36 `prime_app`), adb device-shell re-parse splitting SQL (PR #37
stdin `sql()`, root cause proven byte-for-byte on the host), and no
`profile_settings` row on fresh installs (PR #38 upsert, caught by the script's
own readback guard) — each root-caused BEFORE re-dispatch, zero application
defects; then **run [39576173002](https://github.com/stokonn/Goldberg/actions/runs/39576173002)
@ `3579417` GREEN — 12/12 screenshots** (Home/Repertoire/Discover/Practice/
Profile/Settings × Ivory/Nocturne via the product's own persisted theme),
agent-inspected pair-by-pair (identical layouts, correct palettes, real content,
no blank frames), preserved in `results/ci-run-39576173002/` (+ `run-summary.md`).
Real bundle-transfer costs were owner-dispositioned: executed tests' exact byte
accounting + real file I/O + `disk-usage.txt` accepted as the Stage 5 answer,
with the structural blocker (no bundle feed/CDN/enqueue path exists; download UI
is Stage 7) documented as a Stage 6/7 dependency (DECISIONS.md). **All mandatory
Stage 5 verification has passed; 5.13 final docs/handoff close the stage.**
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
tests. Consent revocation mid-stream and low disk are covered by executed
emulator instrumentation; screenshot inspection completed green (run
39576173002, 12/12 captured + agent-inspected); real bundle-transfer costs were
owner-dispositioned with the structural blocker documented (DECISIONS.md).

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
