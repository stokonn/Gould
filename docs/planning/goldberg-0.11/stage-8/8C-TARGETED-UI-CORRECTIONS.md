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


## Verification ledger

No new test/app verification has executed yet. Before evidence is the immutable Stage8B packet at29ce607; no finding is marked fixed. Source-bound CI must test the changed production source.

## Implementation / early verification checkpoint

Three targeted changes are implemented locally, not yet marked FIXED. AdminSwitch uses one48dp row action/semantic owner and a noninteractive Switch indicator. JournalNavigationBar preserves existing colors/icons/label typography and all five routes, with intrinsic height and soft-wrapped full labels. Both test scaffolds use the production bar and a test-only copy of MainActivity's exact window appearance effect; MainActivity/theme production code unchanged.

Fresh before observations on new offline API36 emulator5580/qemu1 reproduce duplicate label/switch traversal in ENLight1 and CZDark1.3;old APK/probe and Stage8B AVD/evidence preserved. Initial host JVM XML:36 suites/275tests/0failures/errors/skips;build/testAPK compiled,lint still running. Native regressions and after matrix not executed yet. Private PMP task/snapshot/check valid;existing project UUID retained in a new worktree-specific private chain,prior histories untouched.

### Host milestone

Initial wrapper gate PASS3m31s (33 executed/50 up-to-date):275 JVM cases in36 suites,0 failures/errors/skips;app/test APK assembly;lint0 errors/119 warnings. Policy/localization/admin-contract fixtures PASS. Candidate app SHA256 `3df2ca3dc2be646ae40126b8accfcb7b9324e26fc7e352a807796acf0c7b2dc3`;default test APK `035054919b22d3912077d64826659bdd0150c2b69fd53ae616bfeb9346d50626`. Preserved copies in ignored `.gradle/stage8c-artifacts/`;raw outputs/input hashes in the8C results directory. Four focused actual emulator tests are now running;no native pass or FIXED claim yet. Fresh before TalkBack ENLight1/CZDark1.3 and opposite-theme scaffold failures retained separately under `before/`.

### Initial focused native result and correction

Actual native run:4 tests,2 passed/2 failed. Existing Admin checkable regression and scaffold-window regression passed. New tests correctly caught (1) row Switch role exported as generic android.view.View and (2) missing Tab input-focus semantics. No assertion weakened. Row now explicitly merges descendants before its named checkable owner;tabs export real FocusRequester/onFocusChanged/requestFocus state with merged selection/action semantics. Full host gate rerun is in progress (`.gradle/stage8c-host-semantics.log`). Findings remain NOT FIXED;rerun focused native tests before after-matrix/CI. Raw failed output retained at `results/targeted-ui-corrections-2026-10-25/after/focused-initial-instrumentation.txt`.

### Superseding interpretation of the initial native failures

The earlier “lost native Switch role/tab focusability” diagnosis was premature. Baseline before trees already report `android.view.View` for Switch nodes. Exact cached Compose UI1.9.0 bytecode and upstream [Switch role tests](https://android.googlesource.com/platform/frameworks/support/+/8b13b06116210cf5176cf72ac22a3dd242af0460/compose/ui/ui/src/androidDeviceTest/kotlin/androidx/compose/ui/AndroidComposeViewAccessibilityDelegateCompatTest.kt) confirm localized `roleDescription`,not `android.widget.Switch`,is the contract. [Delegate source](https://android.googlesource.com/platform/frameworks/support/+/60b74610e5179b5e5347276385bbf6f70f8b549f/compose/ui/ui/src/androidMain/kotlin/androidx/compose/ui/platform/AndroidComposeViewAccessibilityDelegateCompat.android.kt) and exact cached bytecode confirm selected tabs intentionally omit accessibility clickability. Initial combined clickable/focusable assertion failed before focus was tested. These two expectations were test defects,not established product regressions. The second interim run repeated those same assumptions and remains retained (4cases/2passes/2failures).

Corrected tests now require the exact localized Switch/Tab roleDescription,one named/checkable Switch owner,actual focus/state/disabled/48dp/exact action behavior,and five currently-unselected tab transitions ending at Home. Every measured TextLayoutResult must reflect the requested font scale and have no overflow/ellipsis. Tab semantics now explicitly name the selection/action/focus owner. Recorder captures roleDescription and selected state. Role-contract assembly PASS34s;third focused native run is in progress. No FIXED claim or source-bound CI dispatch yet.

### Focused native verification PASS

Corrected role-contract run on emulator5580/qemu1: **OK(4 tests)** in75.972s. Three new test bodies cover:Switch single name/exact localized role/checkable state/48dp minimum/native focus/Space/touch/accessibility action counts/disabled suppression;32 actual measured layout configurations with applied font scales,no overflow/ellipsis,five selected-state/action/focus transitions each;four system/app-theme window combinations. Existing Admin checkables regression also passed. First two failed runs remain evidence of incorrect initial representation/selected-tab assumptions,not proven product regressions;the corrected role/action checks are explicit. Final same-source wrapper JVM/build/lint is running,followed by opt-in recorder compilation. Real NavHost after32-config screenshots/TalkBack and source-bound CI remain pending;findings not yet marked FIXED.

### Actual-matrix setup correction / transport recovery

Final same-production-source wrapper gate PASS2m1s (11executed/72up-to-date),275 JVM/0failures/errors/skips,lint0errors119warnings;opt-in recorder initially PASS12s. Native4case regression remains PASS. First actual after-matrix attempt had160dpi because native teardown restored display defaults while the old helper only resized pixels;it is **not representative360/412dp evidence**. Archived all frames/events under `after/attempts/density160/`.

Recorder now exports densityDpi/screenWidthDp/screenHeightDp;8C wrapper forces320dpi per configuration and rejects mismatched viewport/empty trees/missing actual TalkBack focus. Test-only metadata compilation PASS22s;production APK remains unchanged from native-passing1c243626… . The guarded5580 attempt verified320dpi/360dp but then had empty TalkBack trees and repeated ADB“device not found” failures. Logcat shows transport disconnects;QEMU/boot remained alive and no application crash was established. Excluded from after acceptance,retained under `after/attempts/transport-5580/`. Owner ADB server PID remained unchanged;never restarted.

Controlled recovery:stop only our8C emulator5580,retain AVD/data,then restart same Goldberg8C AVD as **emulator5588** (8B original AVD remains untouched/stopped),same Lavapipe renderer. Before evidence/native tests are5580;after recovery explicitly targets5588/qemu1. No CI dispatched yet;findings NOT FIXED until actual after/independent CI passes.

### Recovered actual TalkBack / layout refinement checkpoint

Same preserved8C AVD restarted on5588,qemu1/boot1 PASS. First strict after ENLight1 sample now has actual populated focused nodes/TalkBack service,320dpi/360dp and one coherent Switch stop for each of three rows;green focus frames span label+indicator. Remaining strict matrix proceeded with correct recorded geometry and no accepted empty-focus captures. All first responsive candidate observations retained under `after/attempts/initial-wrap/` with APK1c243626… .

Visual inspection at actual360dp/font2 found full text but orphan final letters on some labels. Refine only horizontal label inset4→1dp and use native Heading line breaking/automatic hyphenation;existing labelSmall font size/family/weight/colors and five routes unchanged. Add an assertion that TextLayoutResult retains the original complete label text. Source changed,so old32-layout pass is historical for1c243626…,not transferred. Host gate for refined layout running (`.gradle/stage8c-host-wrap.log`);rerun the changed32-layout native case,then opt-in recorder/real after matrix and independentCI. Stage8B/all APKs/failures/private history preserved;no FIXED claim.

### Refined source locally verified / candidate freeze

Wrapper PASS2m27s(23executed/60up-to-date),275JVM/0failures/errors/skips,lint0errors119warnings. Changed responsive native case PASS66.134s across32 configurations/160 tab transitions,original complete text/no overflow/ellipsis/requested font scale/exact roles/focus/selection/actions. Unchanged Switch/input/scaffold case results remain source-bound to their prior passing inputs;independent full-suite CI will verify all current inputs. Opt-in recorder build PASS17s. Refined production app SHA256 `68d48b75436f6bd3f865d784714ec48570e2745c14aa0e9682514753009f3db0`. All APKs and interim attempts retained. Final source inputs/hashes are in `refined-source-binding.json`;source candidate publication enables independent CI,not a FIXED or release claim. Final real after matrix is pending on preserved5588 AVD,with strict320dpi/360-or412dp/nonempty-tree/actual-TalkBack-focus guards.
