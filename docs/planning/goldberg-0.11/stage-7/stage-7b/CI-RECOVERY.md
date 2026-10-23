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

> Current safe recovery: see [SAFE-RECOVERY.md](SAFE-RECOVERY.md). The owner now
> authorizes checkpoint commit/push and Stage7B dispatch. Latest Issue31 records
> O1–O8 design decisions and local Stage6 M1; integration stays gated. Stage7A
> finally accepted; X10 deferred to Gould0.12, unfixed/unverified. Earlier statuses
> below describe the interrupted preparation, not current authorization.

# Stage 7B — GitHub Actions CI recovery

Purpose: move blocked Android verification to the existing dispatch-only Actions
workflow; this is verification recovery, not a new stage or Admin redesign.
Branch/worktree verified: `feature/goldberg-0.11-stage-7b`,
`/home/stokonn/Projects/Goldberg-Stage7B`; main checkout `/home/stokonn/Projects/Opus`.
Initial HEAD `cee9330`; all pre-existing dirty source preserved.

## Scope and tasks

- [x] Read AGENTS/maintenance/checkpoints/checklist/acceptance and latest Issue31.
- [x] Audit existing quality, Stage5 emulator and screenshot workflows. Existing
  API36 full-suite job uses Ubuntu24.04, JDK17, KVM, pinned actions, ephemeral
  runner debug signing and exact dispatched SHA checks. No production keys.
- [x] Preserve prior275 JVM passes (24 contract tests), lint0 errors/132 warnings/
  1 hint, native APK compilation and recovered emulator SIGSEGV diagnostics.
- [x] Latest Stage6 checkpoint inspected: API-v1 package at `fefa6e8` is PROPOSED;
  owner O1–O8 pending. G1–G9/G11 remain integration gates; G10 locally closed.
- [x] Extend existing workflow with stage7b dispatch mode, one API36 emulator job,
  Gradle cache, build/JVM/lint/host checks and always-upload reports (14days).
- [x] Add four native Admin regressions: Keystore roundtrip/tamper, Android marker
  persistence/account isolation, disabled transport with saved credentials,
  native JSON exact BIGINT/revision overflow. Synthetic data only.
- [x] Workflow YAML/run-block syntax and helper bash syntax PASS; assembly gate
  SUCCESS (cached compilation), fresh JVM task 275/0/0/0 in12s. Six host scripts PASS.
- [x] Reviewed checkpoint0703cd8 committed/pushed under explicit owner scope;
  exact remote SHA readback PASS.
- [x] Dispatch exact checkpoint0703cd8: run40011091868; source binding PASS.
- [ ] Inspect completed job logs/results and retrieve artifacts.
- [ ] Inspect captured smoke screenshots; classify their limited scope honestly.
- [ ] Synchronize Issue31/dashboard/Project, AI docs, master/stage checklist and PMP.

## Coverage and limits

Full `connectedDebugAndroidTest` suite; no class filters, exclusions or new skips.
Existing `Stage7aCatalogueBootstrapTest` may assumption-skip when public OpenOpus
is unavailable; report actual XML skips. Explicit target emulator-5554 with qemu=1
and API36 guards. CI smoke screencaps do not prove Admin dashboard/no-scroll,
confirmations, re-review, TalkBack, locale/font/theme matrix, notifications/worker
runtime, seeded upgrade or live contracts. These acceptance rows stay pending.
API37 compatibility is UNVERIFIED and is not scheduled alongside the stable job.
Fedora44/emulator37.2.12 graphics SIGSEGV/offline is environmental evidence, not
an app defect. No emulator reinstall, Fedora driver change or local relaunch.

No Stage6 worktree/contracts, application models, production networking/moderation,
physical devices, main merge, deployment or release. X10 OPEN; Stage8 pending.

## Run40011091868 — setup failure, narrow correction

Exact source0703cd8; job120611362267 failed at pre-build SDK/emulator diagnostics,
exit127. Downloaded raw emulator-version.txt says emulator executable missing.
Zero JVM or instrumentation bodies executed in this CI run; no application failure.
Artifact12525568699 (1368 bytes), remote ZIP SHA256
cf4fd1040124d460998c033a252320f04f21f82c9aac8f96da15653e0885bf1d;
raw job log, run metadata and extracted five diagnostic files retained under
results/ci-run-40011091868. No screenshots or test XMLs exist for this run.

Narrow fix: add `emulator` to the existing instrumentation-job setup-android
package list, before strict version/KVM diagnostics. No change to tests/assertions,
permissions, physical-device guards, trigger scope, acceptance job or app source.
YAML ordering/package assertion and bash parsing PASS. Re-dispatch requires this
verified fix committed/pushed, binding evidence to its new SHA. Host tests need no
rerun because application/test/build source is unchanged.

Corrected source `3e04e2b716435684a0d4d88a71a4a59407201ada` ordinarily pushed and remotely verified;
run40011450671 dispatched in Stage7B mode. Result pending; no test PASS claimed.

## Run40011450671 — second independent setup failure

Source3e04e2b: SDK emulator installation succeeded; strict version probe failed
exit127 because qemu-system-x86_64 could not load libpulse.so.0. Zero build/JVM/
instrumentation bodies in this CI run; no application failure. Raw artifact/run
metadata retained under results/ci-run-40011450671. New narrow fix installs Ubuntu
libpulse0 before diagnostics. YAML dependency ordering and all bash run blocks PASS;
no test assertions, production infrastructure or application code changed.

Third dispatch: run40011713646, source5d3f0ccd03411666ff741ccc894bc85ad98cd280;
exact SHA confirmed, result pending.

Native helper inspection: stores resolve context.applicationContext; inherited
ContextWrapper behavior bypasses isolated preferences. Override only test
getApplicationContext to retain isolation and test the actual tampered file.
Original source preserved in checkpoint; no assertion or app-source change.

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
