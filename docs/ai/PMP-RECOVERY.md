> Host acceptance is now validated: all 19 checks passed at `0ffebeb`, including 348 JVM tests and local backend/encrypted-backup checks. Nine obsolete worktrees, including Gould-Preparation, have been retired with preservation verified. Current blocker is GitHub administration access, not the build. Read [HOST-ACCEPTED-CLEANUP.md](../maintenance/HOST-ACCEPTED-CLEANUP.md) first; older entries below are historical. Actual repository remains `stokonn/Goldberg` until the approved rename is confirmed.

> Current Gould maintenance continuation (October 27, 2026): prepared branding and the complete current-main baseline are committed on `maintenance/gould-portability` (`b87d238` plus subsequent maintenance commits). Goldberg 0.11 Stable remains published; no 0.12 features have started. Actual repository: `stokonn/Goldberg`; the approved Gould rename awaits project/CI verification. Read [FINAL-CONSOLIDATION.md](../maintenance/FINAL-CONSOLIDATION.md) and [the current handoff](GOULD-FINAL-CONSOLIDATION.md) first. Historical entries below retain their original evidence.

> Current continuation (2026-10-23 recovery): API contract preparation is the
> authorized task, superseding the metadata-only scope below. See STATE.md,
> STAGE_7B_CONTRACT_PREPARATION.md and [Issue recovery checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6368949572).
> Recovered gate: 275 JVM tests PASS (24 contract cases); six host scripts rerun
> PASS in recovery. No application changes during recovery, no live activation.
> Final local PMP checkpoint is resolved with `pmp resume` / `pmp history` after
> documentation synchronization; old sequence-1/sequence-3 facts below are history.

# Goldberg 0.11 — PMP recovery and synchronization

PMP recovery GitHub checkpoint: [6365022849](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6365022849).


Owner-approved scope (2026-10-23): documentation and local checkpoint recovery
only, from the explicit “PMP Recovery & Synchronization” instruction. This does
not authorize application features, Stage 6 edits, testing on a physical phone,
production deployment, merge or release.

## Current project facts

- Stages 1–5 completed on their respective branches; integration is not implied.
- Stage 6 local backend/Android prototypes exist; formal production and integration
  gates remain open. Latest inspected Issue #31 Stage 6.10–6.11 checkpoint records
  host verification at `2f1524e`, with runtime acceptance partial, not complete.
- Stage 7A conditionally accepted; X10 OPEN, with no verified Samsung alignment fix.
- Stage 7B substantially implemented on `feature/goldberg-0.11-stage-7b`, HEAD
  `cee9330f82cf2e57dcdc66a63d94b7a4e3749888` plus uncommitted continuation.
  Prior executed host gate: 251 JVM tests, 0 failures/errors/skips; lint 0 errors /
  132 warnings; APK build and instrumentation compilation passed; five host checks
  passed, including 1050 matching EN/CZ resources. These tests are not rerun by this
  documentation task; metadata assertions are not new application-test evidence.
- Stage 7B emulator/no-scroll/screenshots, runtime and integration acceptance remain
  BLOCKED/UNEXECUTED. G4 current catalogue values unavailable; G5/G6 live sessions /
  dual-revision wire contract unresolved. Production moderation decisions DISABLED.
- Stage 8 pending. Owner acceptance, integration, merge and release remain separate.

Canonical continuation: [STATE.md](STATE.md), [HANDOFF.md](HANDOFF.md),
[VERIFICATION.md](VERIFICATION.md),
[Stage 7B checklist](../planning/goldberg-0.11/stage-7/stage-7b/CHECKLIST.md),
[acceptance packet](../planning/goldberg-0.11/stage-7/stage-7b/ACCEPTANCE.md), and
[verified checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6362810877).
Historical Stage 5 instructions remain history, not the current resumption target.

## Installed integration and recovery method

Installed PMP `1.0.0a1`; CLI documentation at the existing local
`project-memory-protocol/docs/CLI.md` matches the installed implementation for the
configuration, CLI, checkpoint and handoff modules (byte comparison performed).
Stage 7B initially had no `.pmp.yml`; `pmp status` exited 2 with “Cannot load
.pmp.yml”. Existing primary-checkout Goldberg configuration retains project UUID
`ac3a7942-cde5-4e79-8a43-e1a0d2c7df81`. Its old state uses phase `implementation`
and status `in_progress`, unsupported by the current installed schema.

Recovery restores the existing configuration/identity locally in Stage 7B, with a
schema-valid bootstrap state. The old configuration/state and generated Stage 5
handoff are copied into `.pmp/recovery-origin/`; all original files and external
PMP history remain untouched. No `pmp init` is used, avoiding a new project UUID.
Worktree-scoped history stays separate by the integration's actual worktree ID;
old checkpoints are not relabeled or copied into a different identity chain.

After the one-time configuration/state repair, use only documented interfaces:
`pmp task activate`, `pmp task set`, `pmp snapshot`, `pmp handoff`, `pmp status`,
`pmp resume`, `pmp history`, `pmp check`, and `pmp verify`. Generate the private
handoff without `--shared`, which would refuse the existing authored HANDOFF.md.
`.pmp/` and `.pmp.yml` remain local and untracked; no export/commit/push operation.
No applicable PMP-native application-test evidence is fabricated or imported by
editing private evidence files; prior 251-test results remain linked reported facts.

## Exact continuation after this metadata recovery

Preserve all dirty Stage 7B files and read the current acceptance packet. Coordinate
shared Stage 6 wire fixtures through existing Issue #31 without editing its tree.
An allowed working emulator venue and source-bound fixture harness are needed for
visual/runtime/upgrade verification. Do not dispatch the old commit alone and
claim it tests this uncommitted continuation. Do not enable live decisions from an
assumed mapping. No physical device, production deploy, merge or release.

## Verified recovery result

## PMP synchronization — 2026-10-23

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
or release. Recovery scope/method: this document.


The restored configuration retains its original `github: null`. Issue #31 was
synchronized explicitly through the existing authenticated GitHub CLI and checked
separately; no native PMP remote-refresh, commit or push is claimed.
