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
- [ ] Commit checkpoint, recheck remote, ordinary push and exact SHA readback.
- [ ] Dispatch Stage7B mode; inspect actual run/logs/artifacts and document result.
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
No workflow/source changes were needed during recovery. Fedora graphics SIGSEGV
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

Only new repository policy adjustment: scoped raw-evidence whitespace attributes;
no source/workflow rewrite. Final staging also includes .gitattributes.
