## Stage 7B Android Admin — preserved implementation and verified CI (2026-10-23)

**CI recovery COMPLETE; Stage 7B acceptance remains IN PROGRESS.**
Purpose: preserve the interrupted Android Admin work and recover reproducible
verification. Scope: existing queue/review/conflict/audit/settings/sync, contract
fixtures, native tests, dispatch-only CI and durable evidence. Personal Room,
backup and practice systems have no recovery-session changes. Production Admin
networking and decisions remain disabled.

- [x] Preserve original cee9330 work in checkpoint0703cd8, with private PMP excluded.
- [x] Publish corrected source `3a4aac27e2eb8ae70716538d86acd374cdd38077`;
  ordinary push and exact remote SHA readback PASS.
- [x] [Run40023191770 SUCCESS](https://github.com/stokonn/Goldberg/actions/runs/40023191770),
  exact source binding/Ubuntu24.04/KVM/API36/emulator-5554/qemu1 verified.
- [x] Fresh full host gate SUCCESS7m41s:275 JVM tests,0 failures/errors/skips;
  lint0 errors/132 warnings/1 hint; six host scripts PASS,1050 EN/CZ resources.
- [x] Full connected task SUCCESS3m38s: XML182 tests,0 failures/errors,2 existing
  external-fixture skips; all4 Stage7bAdminRuntimeTest bodies PASS. No filters,
  added skips or weakened assertions. Console184/XML182 discrepancy retained;
  XML182 testcase elements are authoritative. Skip messages are empty in XML;
  unchanged source guards require external portrait/provider files not supplied.
- [x] Teardown removal verified in actual logcat. Same-built-APK install-r Success;
  MainActivity cold launch Status:ok. Both smoke screenshots inspected: Home with
  onboarding. These are not Admin/no-scroll/theme/font/accessibility acceptance.
- [x] Artifact12532680601 ZIP integrity and remote SHA256 digest verified:
  5c23ceb0ede9690a82934eae9fa87f75930839aa5859d85bc535bb7ac58ac525.
  Raw logs/XML/skip details/screenshots/file hashes retained in results/ci-run-40023191770.
- [x] Raw evidence retained byte-for-byte; narrow CRLF EOL allowance leaves normal
  source whitespace checks active. Old-source run40015851111 is historical only.
- [x] Issue31 dashboard/checkpoint and local handoffs/checklists synchronized;
  umbrella OPEN / Goldberg Development Project In Progress; private PMP unchanged.
- [ ] Admin visual/no-scroll/locale/font/accessibility, notification/WorkManager
  runtime, seeded upgrade, approved live contract/session integration and owner
  acceptance remain OPEN. G1–G9/G11 integration unresolved; G10 locally closed.

[Final CI checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6374873953) · [Recovery record](SAFE-RECOVERY.md).
The final docs-only checkpoint contains this evidence; its exact SHA/readback is
recorded in Issue31 after publication. No source/runtime claim is transferred from
3a4aac2 to another commit. Next: execute the remaining Admin fixture screenshot/
no-scroll matrix on an explicitly identified emulator, retaining all other gates.
Stage7A final owner acceptance and X10 deferred Gould0.12 remain unchanged; Stage8
readability/accessibility/touch-target checks remain required. No physical device,
main merge, release, deployment or production activation.

---

## Historical recovery chronology — retained unchanged

# Stage 7B safe recovery — 2026-10-23

Purpose: preserve the interrupted Android Admin implementation, verify the existing
source and continue the approved dispatch-only CI recovery. Stage 7B remains
IN PROGRESS; acceptance is incomplete. No implementation restart or redesign.

## Recovery inventory

Initial HEAD `cee9330f82cf2e57dcdc66a63d94b7a4e3749888`, branch
`feature/goldberg-0.11-stage-7b`, upstream
`origin/feature/goldberg-0.11-stage-7b`; remote readback matched exactly.
26 modified tracked files (+1567/-393), no staged changes, 130 untracked files:
125 implementation/contract/documentation/evidence/script files and five private
PMP files. No RELEASE_NOTES.md found in this worktree; none created or removed.
Only this Codex session was visible in the process namespace; no other writer
observed. Initial patch, status, explicit candidate inventory and 1489 file hashes
saved under `/tmp/goldberg-stage7b-recovery-20261023/` before repository edits.
Original source/evidence and private PMP bytes are preserved.

## Scope and tasks

- [x] Read canonical policy, maintenance, AI/PMP/checklists/audit/acceptance/CI records.
- [x] Inspect Issue31 latest description/comments, milestone4 OPEN, release history,
  remote feature SHA and Project2 item31 In Progress; no existing Stage7B CI runs.
- [x] Inventory tracked/staged/untracked work; hash before editing; check processes.
- [x] Audit queue/review/conflict/audit/sync/settings, application transport and
  trust guards, localized resources, native tests and workflow without app edits.
- [x] Run six host scripts, whitespace, YAML and bash syntax checks: PASS.
- [x] Run Gradle assembly/JVM/lint gate: SUCCESS (11s; 82/83 tasks up-to-date).
- [x] Fresh `testDebugUnitTest --rerun`: 275 tests / 0 failures/errors/skips,
  36 suites, SUCCESS in 12s; XML retained under results/safe-recovery-2026-10-23.
- [x] Preserve new verification evidence; review 151 initial checkpoint candidates
  (26 modified +125 new), 1,253,841 bytes; high-confidence credential patterns: none.
  Historical logs/XML are intentional evidence, not accidental build products.
- [x] Explicit staging allowlist inspected (205 initial files); PMP excluded.
  Raw diagnostic whitespace discovered only after including previously untracked
  evidence. Scoped .gitattributes preserves original log/signing bytes while
  retaining default checks for source, structured results and documentation.
- [x] Checkpoint `0703cd8ec40a86e6f92e7fd9cf172c5921e1e90e` committed; immediate remote precheck
  matched base cee9330; ordinary push and exact SHA readback PASS.
- [x] Dispatch Stage7B mode and preserve four run logs/artifacts; fourth run
  executes182 XML tests,0 failures/errors,2 pre-existing skips; four Admin cases PASS.
- [ ] Verify corrected post-suite smoke workflow on its new exact source SHA.
- [ ] Synchronize Issue31 description plus one checkpoint comment and local docs.

## Implementation audit and acceptance limits

Already implemented: fixed dashboard with four tiles/three destinations, retained
queue/search/filter/marker/navigation state, bounded hydration, explicit proposal
confirmations, same-key uncertain replay, fresh role/revision checks, guided
re-review/history/evidence, read-only paged audit and opt-in notification/3h sync.
JVM fixture verification covers these domain behaviors. Native tests cover Keystore,
marker isolation, disabled application transport and Android JSON precision; runtime
execution remains pending. The shared contract corpus is test-only. No personal
Room schema, backup, practice coordinator or application identity changes.

Incomplete: actual Admin visual/no-scroll/locale/font/accessibility acceptance,
WorkManager/notification runtime, seeded upgrade and live approved adapter/session/
independent revision integration. G1–G9/G11 integration gates remain OPEN; G10 local
transport boundary closed. Production networking and decisions remain DISABLED.
Legacy C6 behavior is not the new public API v1 implementation. In-memory reasons
and pending keys do not provide durable process-death reconciliation.

Latest external decisions supersede old local status text: Stage7A final owner
acceptance at issuecomment-6371745218; X10 NOT FIXED/NOT VERIFIED, owner-deferred to
Gould0.12. Stage8 readability/accessibility/touch targets remain required. API-v1
O1–O8 design decisions at issuecomment-6371704250 and Stage6 local M1 at
issuecomment-6372592793 do not authorize production activation or close integration.
Historical records below existing handoff entries remain chronological evidence.

## CI readiness

Existing workflow is dispatch-only, contents:read, pinned actions, Ubuntu24.04/KVM,
API36 with explicit emulator-5554/qemu guards and exact GITHUB_SHA binding.
Stage7B mode runs the full suite without filters; Stage5 acceptance job is excluded.
Host checks and always-upload diagnostics/reports are included. YAML and every run
block pass syntax validation. Native test assembly passes (cached compilation).
Initial static review passed; CI execution then exposed two missing setup
dependencies, corrected below without application changes. Fedora graphics SIGSEGV
is prior host setup failure, not application failure; no local emulator retry.

## Continuation

Use this file and newest AI pointers instead of superseded dirty-tree checkpoints.
After the commit exists: recheck remote branch; push normally only from the recorded
base; dispatch `stage5-emulator.yml --ref feature/goldberg-0.11-stage-7b
-f verification-scope=stage7b`. Bind results to the exact dispatched SHA. Retain
logs/artifacts; report setup failure separately from application test failure.
Never merge main, release, deploy, enable live APIs or use a physical device.

## Host evidence at checkpoint preparation

`assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug --no-daemon
--max-workers=1` SUCCESS; 82 tasks cached and lintDebug executed. Fresh JVM rerun
above supersedes cached test status. Existing lint XML: 0 errors,132 warnings,1 hint.
Initial sandbox attempt failed at read-only Gradle lock before executing build tasks;
approved escalation succeeded. All six `scripts/check_*.py` PASS; 1050 EN/CZ strings.
`bash -n` helper and every workflow run block PASS; workflow YAML dispatch/isolation
assertions PASS. No added skip/class filter. All 1188 protected initial files
(application/contracts/private PMP/prior result files) hash-identical after checks.

APK `app/build/outputs/apk/debug/app-debug.apk`, 24,140,673 bytes,
SHA256 `6976d76e9bd936cd5b45e26f1910f86c3105863a8fe2bbf6f1269aaafc7e269b`;
package com.opus.piano, development version0.10.1/code11, trusted signer
`53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`.
ZIP and test-corpus exclusion PASS. androidTest APK SHA256
`2bd8e457f800ea1bf9796ddd18af9cab63aa4e3b09e7bb1470ba66d431e76224`.
No APK published; no runtime screenshot inspected locally.

Preservation checkpoint adds scoped raw-evidence whitespace attributes; final
staging includes .gitattributes. Later workflow-only dependency fixes follow below.

Preservation checkpoint: `0703cd8ec40a86e6f92e7fd9cf172c5921e1e90e`; source unchanged after this commit.
Only private `.pmp/` and `.pmp.yml` remained untracked after publication.

## First CI result and correction

Run40011091868 at0703cd8 FAILED during pre-build diagnostics: missing emulator
binary, exit127. Zero tests executed. Raw log/metadata/artifact retained; see
CI-RECOVERY.md. Recovery adds explicit emulator SDK package before diagnostics;
YAML ordering and bash syntax verified. A new corrected-SHA dispatch follows.

## Run40011450671 — second independent setup failure

Source3e04e2b: SDK emulator installation succeeded; strict version probe failed
exit127 because qemu-system-x86_64 could not load libpulse.so.0. Zero build/JVM/
instrumentation bodies in this CI run; no application failure. Raw artifact/run
metadata retained under results/ci-run-40011450671. New narrow fix installs Ubuntu
libpulse0 before diagnostics. YAML dependency ordering and all bash run blocks PASS;
no test assertions, production infrastructure or application code changed.

Corrected verification commit `5d3f0ccd03411666ff741ccc894bc85ad98cd280`
ordinary-pushed with exact remote readback; Stage7B run40011713646 dispatched.
Its source SHA matches. No application/test source changed since0703cd8.

## Inspection-backed native test isolation correction

While run40011713646 builds original source5d3f0cc, inspection found the native
test ContextWrapper redirects getSharedPreferences but inherits applicationContext.
Both stores resolve applicationContext first, bypassing the isolated preference
file; the tamper assertion targets a different file. Correct only the test wrapper
by returning itself from getApplicationContext; preserve all assertions. Original
four-test source remains immutable in0703cd8/5d3f0cc. No production storage behavior
changed. `assembleDebugAndroidTest --no-daemon --max-workers=1` SUCCESS in17s
(5 executed/46 cached tasks); runtime verification requires corrected source.
Protected-file accounting after this deliberate edit:1187 unchanged; one native
test file intentionally changed with the original preserved in the checkpoint.

## Run40011713646 — CI host PASS, provenance setup failure

Exact source5d3f0cc: dependency/KVM diagnostics PASS; fresh full Gradle host gate
SUCCESS7m37s;36 suites/275 JVM tests/0 failures/errors/skips, lint report retained.
Five host scripts PASS; contract verifier failed because shallow checkout lacked
immutable bcc7796 source. Zero instrumentation bodies; no emulator launch.
Raw job log, run/artifact metadata, all JVM XMLs and diagnostic/lint files retained
under results/ci-run-40011713646; full extracted artifact retained under the initial
/tmp recovery directory. This is CI provenance setup failure, not an app test failure.

Verified narrow fix: fetch only the exact 40-hex provenance commit without checking
out/changing HEAD, then run existing strict verifier before the expensive build.
Reproduced in a new /tmp Git repository: exact bcc7796 fetch and all29 cases/15 hashes
PASS. YAML/bash parsing PASS. No weakened hash checks or contract content changes.
Native test isolation correction071ea93 is already committed/pushed, compiled17s;
next dispatch must include both verified fixes. Original source/evidence preserved.

Fourth dispatch: source `de792fc06f0e8d8ef898d4d2254489783e3310a5`,
run40013193851 includes pinned-source preflight and native test wrapper correction;
ordinary push/readback verified. Result pending. Raw emulator-version/sdkmanager
text also emits trailing whitespace; extend the existing narrow raw-evidence
attributes for those filenames without editing diagnostic bytes.

## Run40013193851 — instrumentation PASS; post-suite smoke failed

Source de792fc; strict source/provenance/KVM/API36 guards passed. Fresh host gate
and275 JVM tests PASS; lint0 errors/132 warnings/1 hint; six scripts PASS.
Connected task SUCCESS3m22s. XML182 testcase elements, failures0/errors0/skipped2;
all four Stage7bAdminRuntimeTest bodies PASS. Two skips are unchanged Stage4
external portrait/provider fixture tests. Gradle console says184; preserve the
console/XML discrepancy without inventing additional executions.

Workflow FAILURE is post-test smoke: ui-start.txt reports missing MainActivity
because connected-test teardown removed app. Captured final-screen.png inspected:
Android launcher only; no Admin/no-scroll/visual acceptance proof. Preserve all
original downloaded hashes in artifact-files-sha256.json; core XML/diagnostics,
raw job log, losslessly compressed logcat and screenshot retained in results.

Fix only smoke helper: explicit emulator-targeted install-r of the same built APK
before am start. No uninstall/signature bypass. Bash syntax and synthetic host
harness PASS, including fatal install failure and command order; no real device
used by harness. Corrected runtime pending a new exact-SHA dispatch.

## Usage-limit resume — exact state and evidence correction

Resume inventory: HEAD and remote both de792fc06f0e8d8ef898d4d2254489783e3310a5;
80 staged files, no unstaged changes initially; private PMP remained untracked.
Only this session visible in process namespace. Staged patch and byte hashes saved
before edits under /tmp/goldberg-stage7b-resume-20261023.

Run40015851111 completed FAILURE at original de792fc, without the smoke fix.
The preceding staged check stopped the commit; the following shell dispatch still
ran against the old remote. No commit/push/corrected-source verification came from
that URL. Preserve its metadata/logs; do not use it as correction acceptance.

Exact whitespace blocker: raw instrumentation XML uses CRLF. Add only its evidence
path's cr-at-eol allowance, retain normal blank-at-eol/EOF/space-before-tab checks,
and disable text conversion there to preserve bytes. No source checks suppressed.
Staged check now PASS under the scoped attribute; raw XML bytes unchanged.

Teardown cause now verified directly from saved logcat: at19:55:17.862,
ActivityManager force-stops com.opus.piano for deletePackageX; at19:55:18.058,
RollbackManager broadcasts ACTION_PACKAGE_FULLY_REMOVED. Both occur after tests
and before the failed smoke launch. Exact excerpt retained in safe-recovery results.
Restore only the same built APK with emulator-targeted install-r; keep set-euo
pipefail, full suite, strict install and launch failures, and device guards intact.
Synthetic host harness establishes command ordering/error propagation only.
Next: require staged check success, commit, verify ordinary push and exact remote,
then dispatch corrected SHA once. Collect actual final logs/XML/smoke, synchronize
Issue31/local checklists and publish documentation. Stage7B acceptance stays open.

## Final verification commands and boundaries

Executed locally during recovery:

- `./gradlew assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug --no-daemon --max-workers=1`: SUCCESS11s, mostly cached.
- `./gradlew testDebugUnitTest --rerun --no-daemon --max-workers=1`: fresh275/0/0/0,36 suites,12s.
- `./gradlew assembleDebugAndroidTest --no-daemon --max-workers=1`: native helper compilation SUCCESS17s; later all4 native bodies passed in CI.
- `python3 scripts/check_agent_policy.py`, `python3 scripts/check_backup_fixtures.py`, `python3 scripts/check_history_paging.py`, `python3 scripts/check_localization.py`, `python3 scripts/check_migrations.py`, `python3 scripts/check_admin_contract_fixtures.py`: all PASS; repeated in final CI.
- `bash -n scripts/ci_android_instrumentation.sh`, workflow YAML/run-block syntax, pinned-source fetch reproduction and synthetic smoke command/error harness: PASS. Harness is not emulator acceptance.
- Final authored/staged whitespace and explicit-file allowlist checks required before documentation commit; raw instrumentation CRLF is preserved by narrowly scoped attributes.

CI executed the full host gate above plus unfiltered `./gradlew --no-daemon --max-workers=1 connectedDebugAndroidTest`; final timings/results are in the current summary. No local emulator/physical-device operation. Stage5 upgrade job intentionally excluded by Stage7B scope, not an application test skip.

Local app APK remains `app/build/outputs/apk/debug/app-debug.apk`,24140673 bytes,
SHA2566976d76e9bd936cd5b45e26f1910f86c3105863a8fe2bbf6f1269aaafc7e269b,
com.opus.piano, development0.10.1/code11; verified trusted signer
53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e.
No release artifact published. Original1187 protected files unchanged; one deliberate
native-test isolation correction is preserved alongside its original committed form.
The69 staged raw evidence files from the usage-limit interruption match their
committed bytes exactly. New screenshots show Home/onboarding only; prior run's
launcher screenshot was inspected and is retained as smoke-failure evidence.
