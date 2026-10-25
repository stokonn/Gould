## Stage 8C current disposition — all three targeted findings FIXED

## Stage 8C — targeted UI corrections COMPLETE (October 25, 2026)

**The bounded UI correction milestone is complete. A8-1, A8-2 and A8-3 are FIXED within the verified scope. Full Stage 8 integration/release remains PENDING/BLOCKED.**

Branch `feature/goldberg-0.11-stage-8`; baseline `29ce6071741df7567f782730bd78d6d1e0192780`. Production correction source `4d245b46936d640740799dfcc6e8d61f3d44b249`; corrected test source `a41cd11a1b32af2949c43cb6e5b6aac94a3c2a51`. Later documentation/evidence receipts retain these exact runtime source bindings.

A8-1/A8-2 are corrected Medium/P2 product findings; A8-3 is corrected Medium/P2 scaffold evidence handling.32 actual configurations/four real TalkBack traversals and independent [native CI2](https://github.com/stokonn/Goldberg/actions/runs/40170295032) passed. Production theme behavior is unchanged. Native action/keyboard/pointer/disabled checks passed; supplemental double-tap activation and audible speech remain unverified. [Complete current results, commands, source/artifact binding, screenshots and limitations](8C-TARGETED-UI-CORRECTIONS.md).

The8B/8A sections below preserve historical assessments and their then-current statuses. Current Stage8C disposition supersedes their old NOT FIXED/proposed-approval wording; combined integration/release gates remain open.

---

## Automatic new-source host gate after8B source publication

Run40147220442 SUCCESS at394b6b58b2adcb0eb07b4f873161bbd7cc70ed3c:clean wrapper build/JVM/testAPK compilation/lint PASS7m49s;83tasks executed/1up-to-date;all six workflow validators and schema-diff gate PASS. Individual JVM case count not exported,not inferred from275historical tests;0 native suite executions. This adds V02/V03 host evidence at the8B source,does not create combined-backend/upgrade acceptance or rerun CI25. [Raw result](results/automatic-quality-40147220442/result.json).

## Stage 8B verified execution delta — October 25, 2026

V01:16worktrees/current heads/Issue31/milestone4/Project/releases reviewed;2203external untracked files unchanged,Cloud publication untouched. V02–V04:new opt-in recorder assembly PASS54s/default runner manifest PASS13s;no full CI/JVM/lint/native suite rerun. V05–V09:75historical CI25 critical cases mapped,not rerun;combined-source/true trusted-signer seeded upgrade remain pending. V10–V13:28unique emulator settings configurations,29captures including retry,161PNG/tree pairs,4actualTalkBack traversals/40swipes. A8-1/A8-2 actual Medium/P2 defects reproduced;A8-3 scaffold-only statuscontrast reproduced in tested scope,actual matching NavHost readable. Findings NOT FIXED. V19:retained/baseline ZIP/package/hash/trusted signer verified;source0.10.1/code11,no0.11candidate/version selection. Evidence verifier/Python/whitespace/production preservation PASS.

V14,V16–V18,V20 and broader V05–V09 remain UNVERIFIED/PENDING/BLOCKED as applicable:audible speech,motion/fullkeyboard/lifecycle/full3h-reboot,combined data regression/live contracts/OAuth/hosted/backend/legal/operational/release. [Commands,counts,severity,scope and remaining gates](8B-TECHNICAL-VERIFICATION.md).8A matrix below is historical preparation scope; its unexecuted statuses do not override this bounded8B delta or transfer results to future combined source.

---

# Stage 8 — verification matrix

**Planning matrix, not an execution report.** No application tests/builds/emulators/live calls were run for8A. Historical evidence stays bound to its original source. A preparation-base checkout does not transfer that evidence to future integrated source. New execution is needed only when relevant source/scope changes justify it, after authorization. No physical-device testing.

| ID / area | Retained evidence | Stage8 execution required / venue | Status and dependency |
|---|---|---|---|
| V01 source inputs/preservation |8A audited15 existing worktrees/53 cached refs/fresh remotes; unique work snapshotted privately |Before/after hashes, accepted-source ancestry and remote readback; host read-only Git |8A documentation/preservation verification only; not product verification. |
| V02 host clean gate |Stage7B275 JVM/0failure; lint0errors119warnings1hint; normal restored build passed |`./gradlew clean assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug --no-daemon --max-workers=1`, appropriate offline/cache config, logs/XML |8C:275 JVM/0 failures/errors/skips, build/testAPK/lint PASS; combined-backend candidate remains pending.|
| V03 validators |Seven Stage7B scripts passed |Relevant `scripts/check_*.py`, including admin/policy/backup/paging/localization/migration/Stage3B; Stage6 checks on adopted source |8C local focused validators and both source-bound CI validator sets PASS; no backend adoption.|
| V04 native suite/UI matrix |CI25 187 raw cases/185pass/2existing skips/all9Stage7B;262 PNG/tree pairs reviewed |Actual combined-SHA instrumentation and source-bound screenshot/tree inventory, row reachability, focus, actual Back and settled states |8C CI2 at a41cd11:190 native/188 passed/2 existing skips;262 scaffold PNG/tree pairs. No combined backend or seeded upgrade inferred.|
| V05 migration/seeded upgrade |CI25 schema8→10,12collections/37sessions, prefs/integrity retained; temporary CI signer disclosed |Trusted0.10.1→candidate compatible install; exact seeded personal data/integrity and failure/reopen checks on emulator |PENDING combined candidate; no release-signed install claim from same-runner debug evidence. |
| V06 backups/drafts/recovery |Earlier Stage3B/5/7 accepted source-bound records |Old formats/new roundtrip, untouched original recovery/export, failed/retried recovery, profile/settings/attribution/showcase/badges |PENDING combined source; no DB destruction. |
| V07 practice/time/mic |Earlier accepted regression evidence |Persistence-before-running, monotonic/per-piece/idempotency, process death/recovery no mic restart; host+emulator |PENDING combined-source regression. |
| V08 providers/search/licenses |Stage4/5 accepted bounded fixtures |Host parser/cancellation/rate/cursor/provenance fixtures plus permitted emulator online/offline/partial states |PENDING combined source; live policy/licenses must be authorized/current. |
| V09 offline/cache/metered |Stage5 accepted original source evidence |Process death/resume/lowspace/corruption/remove/clear/shared data, Wi-Fi→mobile/consent/Data Saver; emulator |PENDING combined source; explicit metered permission. |
| V10 semantics/focus/layout |CI25 516named/focus-checked occurrences, actual rows/Back; October24 EN1.0/CZ1.3 real TalkBack |Full real NavHost selected sizes/fonts1.0/1.3/1.5/2.0/EN/CZ/Light/Dark, tree/service events/screenshots/touch/keyboard |8C bounded32-config render/native matrix and4 actual TalkBack traversals PASS; audible speech/motion/broader release matrix remain unverified.|
| V11 duplicate Settings stops |Actual CZ label/control repeats at3/4,5/6,7/8 |A8-1 reproduction/release disposition; actual TalkBack/native checked actions, no duplicated action |FIXED in8C:single named Switch owner;actual TalkBack focus and independent role/state/48dp/disabled/Space/pointer/exact action tests PASS.|
| V12 tab truncation |CI25 and CZ dark1.3 manual screenshots; full semantic names |A8-2 visual readability plus route/touch/semantic reachability in actual app |FIXED in8C:full original-size labels in32 layouts,160 native transitions andfive real NavHost routes PASS.|
| V13 scaffold contrast |Historical CI scaffold finding; one real dark NavHost sample readable |A8-3 compare real app/scaffold/window/theme flags and actual measured colors/context |FIXED in8C test scaffold:four opposite-system pairs match NavHost flags/readable pixels;production theme unchanged,no product defect demonstrated.|
| V14 speech/motion |TalkBack service installed/active; zero3animation scales routes worked/restored1 |Audible EN/CZ speech/pronunciation and any required comfort/frame-timing assessment; suitable emulator/audio setup |UNVERIFIED; no audio/subjective assessment performed in8A or inferred. |
| V15 IME/Back |October24 Tab8,558px IME and layered actual Back |Combined real app text/dialog/routes/system Back/keyboard/touch exploration and accessible Back |PENDING combined source; historical samples remain bounded passes. |
| V16 notification revoke/delivery |Separate-process revoke PID6176 killed→7748 denied→7815 worker no notice; synthetic calls |Actual combined source role/runtime revocation/process restart plus authorized real interval/reboot delivery |Historical bounded revoke verified; real3h/reboot UNVERIFIED. |
| V17 backend canonical adoption |Stage6 local490 bodies/22Kotlin probes,10 incompatible legacy cases |G1–G9/G11 actual Android parser/models/session/review/error semantics and shared consumer fixtures |BLOCKED pending explicit integration/contract/policy scope; G10 disabled only. |
| V18 hosted auth/security/operations |Source-bound Stage6 local security/HTTP/PG evidence, not deployed acceptance |Approved live wrong-role/replay/expiry/refresh/races/revoke/default-deny/export/restore/quota/kill-switch |BLOCKED: G1–G9/G11,C4,6.0f/live/production authorization OPEN. |
| V19 package/signing/artifact |Existing localAPK SHA93ae9ec88472e4f07dc5328980786e9ce252887ccbac507c0034195cd9b51961; trusted signer |Combined candidate package/version/code/ZIP/resources, trusted cert, SHA256, source+artifact identity and compatible upgrade |8C APK68d48b… package/ZIP/version0.10.1/code11/trusted signer/installed bytes PASS; release version and trusted-release seeded upgrade still pending.|
| V20 legal/docs/release decision |Stage7 owner acceptance; Stage6 documentary gates remain partial/open |Current provider/privacy/controller/terms/contact/retention/zero-cost proof; final English Wiki/checklists/report and owner decision |PENDING/BLOCKED; no merge/deploy/release permission. |

For every future execution retain command, exact source/input hashes, environment/emulator serial+qemu guard, expected/actual result/count, errors/skips, original screenshots/tree/service events, artifacts/digests and limits. Distinguish compiled-only from runtime, mocks/reference from actual client/server, synthetic/local from hosted, and proposed deferral from approved release disposition. Failed attempts remain evidence. Do not turn a planned checkbox into a PASS.

created by stokonn with love <3
