## Stage 8C — targeted UI corrections (authorized October 25, 2026)

**IN PROGRESS.** Baseline `29ce6071741df7567f782730bd78d6d1e0192780`; branch `feature/goldberg-0.11-stage-8`. Owner explicitly authorized A8-1/A8-2 production UI corrections, A8-3 test-scaffold correction, focused/host/emulator/source-bound CI verification, evidence publication and private PMP checkpoint. No backend adoption or production networking/merge/deploy/release.

Purpose: remove duplicate Settings focus stops, preserve complete responsive bottom-navigation labels and make scaffold system bars reflect MainActivity. Scope: AdminSwitch, reusable navigation components/OpusApp, test scaffolds/regressions/recorder, Stage 8 records and private worktree PMP. No database/schema/backup/practice timing change planned.

- [x] Recover policy/maintenance/AI/Stage 8B and inspect exact clean baseline, all worktrees, refreshed Issue31/Project/source heads; preserve APKs/raw evidence/private files. Cloud remains separately published c8b903b and untouched.
- [ ] Implement coherent named Switch row semantics with state, disabled/key/touch and exactly-once behavior.
- [ ] Implement full responsive labels, preserving five destinations/editorial tokens/font scaling/selected state/navigation.
- [ ] Correct test-only window flags and share production navigation rendering in scaffold; keep production theme behavior unchanged.
- [ ] Verify focused state/action/keyboard/semantics regressions and all 32 EN/CZ × Light/Dark × fonts1/1.3/1.5/2 × small/standard configurations.
- [ ] Actual TalkBack before/after traversal, focus events/screenshots/trees and real navigation; retain unsuccessful attempts. Findings remain NOT FIXED until independent verification passes.
- [ ] Appropriate wrapper JVM/build/lint, host checks and actual source-bound emulator CI; source/artifact/signing preservation.
- [ ] Update private PMP, local checklist/matrix/AI and existing Issue31/Project; verified commit/push/exact remote SHA/CI receipt.

[Execution checklist](8C-TARGETED-UI-CORRECTIONS.md). Stage7 acceptance remains COMPLETE; full Stage8 integration/release PENDING/BLOCKED. G1–G9/G11,C4,6.0f and live integration OPEN.

created by stokonn with love <3

---

## Stage 8B — first technical assessment verified, October 25, 2026

**TECHNICAL ASSESSMENT COMPLETE / SOURCE PUBLISHED. Full Stage8 integration/release PENDING/BLOCKED.** Branch `feature/goldberg-0.11-stage-8`, baseline2f4d1b8. Stage7 client/synthetic acceptance remains COMPLETE.

Automatic new-source quality [40147220442](https://github.com/stokonn/Goldberg/actions/runs/40147220442) **SUCCESS at394b6b58b2adcb0eb07b4f873161bbd7cc70ed3c**:clean Gradle build/JVM/androidTest compilation/lint PASS7m49s,83executed/1up-to-date;all six workflow validators PASS. Individual new JVM counts not exported;0 native suite executions. This supersedes earlier local-only no-full-host statements below;CI25 was not manually rerun. Documentation-only receipt skips redundant CI;source-bound success is not transferred to its later SHA.


Purpose/scope: reconcile accepted7A documentation, audit mainCI/13 Stage 6 overlaps, assess A8-1/A8-2/A8-3 offline, verify retained APK/source/upgrade prerequisites and map critical-data coverage. Production behavior/database/schema/contracts/CI unchanged; new opt-in instrumentation recorder/host evidence tools only.

- [x] Refresh16 worktrees/remotes/Issue31/milestone4/releases/Wiki/Goldberg Development; preserve all 2203 external original untracked files/APKs/PMP. Concurrent Cloud source 2c38177 / receipt c8b903b untouched, not adopted; PRIVATE STAGING NO-GO retained.
- [x] Reconcile9 Stage 7A documentation deltas:four isolated acceptance/follow-up files adopted, shared current dashboards preserve newer Stage7B/8 decisions. X10 NOT FIXED/NOT VERIFIED,deferred Gould0.12; material accessibility retained.
- [x] Audit main-onlyCI and classify all 13overlaps; stronger accepted Stage7B workflows retained; no source combination.
- [x] New test-recorder wrapper assembly PASS54 s; default runner manifest PASS13 s. App APK byte-identical93ae9ec…; trusted package/signature/ZIP/source/version/baseline review PASS,still0.10.1/code11.75 historical critical CI25 cases inventoried,not rerun.
- [x] Offline API36 emulator5588/qemu1:28 unique settings configurations (16small+4standard+4opposite-theme NavHost+4scaffold),29 captures including retry;161 PNG/tree pairs. Four real TalkBack traversals / 40 swipes with events. Emulator stopped; AVD/APKs retained.
- [x] A8-1 actual duplicate Settings stops and A8-2 actual large-font label ellipsis reproduced, bothMedium/P2,NOT FIXED. A8-3 scaffold statuscontrast~1.05–1.13:1 reproduced; matching real NavHost5.56–19.48:1,gesture5.74–15.25:1; no product contrast defect demonstrated in tested scope. Proposed fixes await separate implementation approval.
- [x] Evidence verifier,Python compilation,production-preservation and whitespace checks PASS. No new JUnit/full CI/lint/data-suite execution claimed; no audible speech/motion/full3h-reboot/live integration acceptance.
- [x] Source `394b6b58b2adcb0eb07b4f873161bbd7cc70ed3c` ordinary-pushed and remote SHA exact; [authoritative Issue checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6391483885), Issue OPEN / Project In Progress synchronized/read back. Final documentation receipt follows without changing runtime evidence.

[Full commands/results/limitations and next milestone](8B-TECHNICAL-VERIFICATION.md); [all 13risk resolutions](INTEGRATION-SEQUENCE.md). No production correction, networking activation, Android/backend combination, main merge, deploy or release.

Next: retain published bounded evidence; seek separate Stage8C scope for targeted row semantics/responsive labels/scaffold-window correction and affected before/after regressions. Stage6 G1–G9/G11,C4,6.0f/public-version/real-policy/live/production dependencies remain OPEN. Trusted-signer seeded0.11upgrade/version selection and broader release gates remain pending.

created by stokonn with love <3

---

## Stage8A preparation completion and publication receipt

**8A readiness preparation COMPLETE / packet PREPARED AND PUBLISHED. Stage8 integration/release acceptance PENDING/BLOCKED.** Published preparation commit `ea1758c6fb10c9f4cdecf83d4c47523e9a5b553a` on `feature/goldberg-0.11-stage-8`, ordinary push and exact remote readback PASS. [Existing Issue31 source checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6388549455) and Project overview/item readbacks PASS; Issue OPEN / umbrella In Progress. Documentation-only preservation checks passed; no runtime tests/builds/CI/merges/deploy/releases executed. Stage7 client/synthetic acceptance remains COMPLETE. Concurrent Stage6-Cloud WIP was recorded untouched, not adopted; all Stage6/live/production gates remain OPEN. Final handoff commit SHA is recorded in the next Issue31 receipt.

## Concurrent work observed during preparation

Initial inventory found clean tracked trees in all15 existing worktrees. A later preservation check detected new uncommitted changes in `/home/stokonn/Projects/Goldberg-Stage6-Cloud` (ten tracked paths, plus new untracked readiness work). They were left untouched, not copied/merged, and are not accepted input evidence or a gate-closing decision. Published preparation pins remain unchanged; re-read the Cloud worktree's eventual published checkpoint/HEAD and Issue31 before dependent integration. All2203 originally inventoried untracked files remained byte-identical, including private PMP; Stage7B tracked/untracked evidence and existing APK were preserved. The complete private audit is retained outside Git.

# Stage 8A — readiness preparation

Status: **readiness packet PREPARED; integration/release acceptance NOT EXECUTED**. Stage8A preparation publication/checkpoint checks are complete; Stage8 integration and release gates remain PENDING/BLOCKED. Owner authorization is the current request to proceed with previously authorized Stage8A preparation. No separate earlier Stage8A execution authorization was found in the inspected local planning documents or all143 existing Issue31 comments; this request authorizes the bounded preparation performed here.

Stage7 Android client/synthetic scope is officially COMPLETE and owner-approved. Stage7B is ACCEPTED WITH DOCUMENTED LIMITATIONS; [owner decision](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6388393525), [published final checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6388422250). Stage7A acceptance is preserved. This packet does not reopen that acceptance or claim live compatibility.

Purpose: identify safe inputs, order future integration, assign real readiness checks and carry known defects/unverified behavior into release decisions without silently bypassing Stage6. Scope: planning/AI checkpoint documentation and source-control inventories only. No application/test/workflow/DB/migration/signing/APK/evidence changes, merges, live calls, deployments or releases.

## Worktree and integration-base decision

Created `/home/stokonn/Projects/Goldberg-Stage8` on `feature/goldberg-0.11-stage-8` at accepted Stage7B documentation HEAD `4c6a91d600ad18455a280f941f89740694d5afa0`, only after inspecting all15 existing worktrees,53 local/cached remote refs and fresh remote heads/tags. No Stage8 branch/directory previously existed. All15 had clean tracked trees; untracked work remained in its owning worktree:44 files in Stage5,63 in Stage6API,6 in Stage7A,2090 in Stage7B, zero elsewhere. Private file names/hashes remain outside Git. Stage7B PMP and raw unique evidence are not copied or moved. [Public inventory](BASE-INVENTORY.json) omits private file paths/hashes; the complete private before/after audit is retained separately.

The selected base contains the Stage3A/3B/4/5 heads and Stage7A implementation through `576f0d2dee561bd93f22daa616fe2f866eeb3365`. It preserves the exact accepted Stage7B executable source and committed evidence. It is a **preparation base**, not an integrated0.11 release candidate.

| Input | Pinned HEAD | Relationship / handling |
|---|---|---|
| Accepted Stage7B | `4c6a91d600ad18455a280f941f89740694d5afa0` | Selected preparation base; CI25 tested executable source remains `fb1d3f971ed79de09dccbdc771e1c45a09dc0a6c`. |
| Stage7A final acceptance | `1b97281ac639a045151ab4dd01e28c499c2e4ca7` | Not an ancestor:27 base-only commits /1 Stage7A-only commit. Sole unique commit changes9 documentation files, no app/test/build source. Carry its decision by reference now; reconcile unique docs in future authorized integration. X10 remains NOT FIXED/NOT VERIFIED, deferred Gould0.12; do not pull it into the three Stage8 findings. |
| Stage6 local API foundation | `006fa577ff4faef8ee7a25daf851620feb5bc3a8` | Divergent:54 base-only /78 backend-only commits; local foundation is independently verified, client compatibility is OPEN. No backend merge/adoption in8A. |
| Remote main | `3e7c25e1b624759f7ba537fbd8cbee4bed21bd04` | Divergent:81 base-only /11 main-only commits. Main-only history is CI work; inspect current workflow content before future integration and preserve stronger accepted Stage7B guards. Local main is older and is not a suitable preparation base. |

The Stage6 merge base is `bcd88949b7998ce30df25b19b71de30dd413483a`. Read-only path intersection found13 potentially overlapping paths; see [sequence](INTEGRATION-SEQUENCE.md). This is a potential-overlap inventory, not an attempted merge or a conflict-resolution result. Remote branch heads must be rechecked before any future integration; pin updates require new evidence review.

## Packet and preparation tasks

- [x] Inspect policy/maintenance, accepted Stage7 docs, Stage6 current handoff/integration ledger, existing Issue31 and Project; inspect all existing worktrees/branches and unique local changes before creating a new worktree.
- [x] Select/record a source-pinned preparation base and preserve all pre-existing worktrees, private PMP, APK and accepted evidence.
- [x] Create the separate Stage8 worktree without merging source branches.
- [x] Prepare [integration sequence](INTEGRATION-SEQUENCE.md), [release-readiness checklist](CHECKLIST.md), [deferred accessibility assessments](ACCESSIBILITY-ASSESSMENTS.md), [Stage6 dependencies](STAGE6-DEPENDENCIES.md) and [verification matrix](VERIFICATION-MATRIX.md).
- [x] Verify documentation links/JSON/whitespace,270 unchanged accepted source hashes/Stage7 evidence/APK and2203 original private/untracked files; concurrent Cloud work recorded untouched. [Preparation results](PREPARATION-VERIFICATION.json). This is documentation/preservation verification, not a fresh application test result.
- [x] Commit/push the documentation-only packet and verify remote SHA; existing Issue31 and Project/AI checkpoints synchronized and read back. No duplicate issue/project.
- [ ] Stage8 integration/release acceptance: PENDING. Stage6 G1–G9/G11,C4,6.0f, Android/backend live integration and all production authorization gates remain OPEN. G10 is closed only for the existing disabled boundary.

Completion of8A preparation means the readiness packet and durable checkpoints are reviewable. It does not mark any unexecuted matrix row passed, fix a deferred finding, authorize client/backend adoption, waive legal/operational gates or complete Stage8.

created by stokonn with love <3

[Verified preparation milestone](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6388534366); final publication receipt will bind the remote documentation SHA.
