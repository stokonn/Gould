## Stage 8C — targeted UI corrections COMPLETE (October 25, 2026)

**The bounded UI correction milestone is complete. A8-1, A8-2 and A8-3 are FIXED within the verified scope. Full Stage 8 integration/release remains PENDING/BLOCKED.**

Branch `feature/goldberg-0.11-stage-8`; baseline `29ce6071741df7567f782730bd78d6d1e0192780`. Production correction source `4d245b46936d640740799dfcc6e8d61f3d44b249`; corrected test source `a41cd11a1b32af2949c43cb6e5b6aac94a3c2a51`. Later documentation/evidence receipts retain these exact runtime source bindings.

Purpose: remove duplicate Admin Settings focus stops, retain readable full navigation labels with Android font scaling and make test-scaffold system bars reflect MainActivity. Scope: three production UI files, three instrumentation files, host evidence scripts and Stage 8 records. Application ID/namespace `com.opus.piano`, database `opus.db`, schema, backup/practice logic, contracts, dependencies, production theme behavior and version metadata are unchanged.

- [x] Recover policy/readiness/AI/Issue31/Project and source heads; preserve all concurrent work and historical evidence.
- [x] A8-1: one named, stateful Switch row; minimum48dp, disabled suppression, keyboard/touch/native accessibility actions and exactly-once callbacks verified.
- [x] A8-2: all five destinations retain full names, original font metrics, icons/colors, selection and navigation; natural bar height/wrapping/hyphenation replaces ellipsis.
- [x] A8-3: test-only system-bar effect mirrors MainActivity; production theme handling unchanged; four opposite-system scaffold/NavHost pairs agree.
- [x] Verify32 EN/CZ × Light/Dark × fonts1.0/1.3/1.5/2.0 ×360×640/412×915dp configurations, four real TalkBack traversals/40 actual focused steps, five actual routes/two Back transitions and worst-case2x scroll reachability.
- [x] Gradle/JVM/build/lint/host checks and independent source-bound native acceptance; preserve unsuccessful attempts and the failed first CI run.
- [x] Update local reports/checklists/matrix/AI and publish verified source/evidence; synchronize existing Issue31/Project and private PMP checkpoints. Private PMP/APKs/secrets are excluded from Git.

Verification:275 JVM cases/36 suites,0 failures/errors/skips; independent native190 cases/**188 passed/0 failures/0 errors/2 existing external-source skips**, including all3 Stage8C and9 Stage7B cases. CI lint0 errors/132 warnings/1 hint; local lint0 errors/119 warnings. [Native CI2](https://github.com/stokonn/Goldberg/actions/runs/40170295032) succeeded at `a41cd11a1b32af2949c43cb6e5b6aac94a3c2a51`; [quality](https://github.com/stokonn/Goldberg/actions/runs/40168589919) succeeded at identical production source `4d245b46936d640740799dfcc6e8d61f3d44b249`. Unchanged successful automatic quality was not repeated after the test-only correction.

Limits: actual TalkBack focus/events are verified; audible speech quality and supplemental double-tap activation remain unverified (unsuccessful injections retained). Native ACTION_CLICK/Space/pointer/disabled and exact callback counts independently passed. Two terminal Czech baseline focus snapshots without a focused node are excluded from focus proof. Combined backend/source, trusted-release seeded upgrade, full lifecycle/three-hour/reboot/live operational acceptance and final release/version selection remain open. Stage6 G1–G9/G11, C4,6.0f and live integration remain OPEN; concurrent Cloud56ff724/hosted NO-GO was not adopted. No main merge, cloud activation, physical-device test, deployment or Release.

Current verified APK: ignored local `app/build/outputs/apk/debug/app-debug.apk` (preserved copy `.gradle/stage8c-artifacts/candidate-app-wrap.apk`), development0.10.1/code11,23,067,944 bytes. SHA256 `68d48b75436f6bd3f865d784714ec48570e2745c14aa0e9682514753009f3db0`; trusted certificate SHA256 `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`. Installed-byte hash/ZIP/package/signature checks passed; CI uses its temporary debug signer and is not a release artifact.

created by stokonn with love <3

## Verified changes and evidence

| Finding | Original severity | Verified correction |
|---|---|---|
| A8-1 | Medium/P2 product usability | AdminSwitch gives one merged row the complete name, localized Switch role, checked/enabled state and action. The visual Switch is noninteractive. Before ENLight1/CZDark1.3 each reproduced three Text/control pairs; all four accepted after traversals have one stop for each row. Native strict action, Space, pointer, state, disabled and48dp assertions pass independently. |
| A8-2 | Medium/P2 product readability | JournalNavigationBar uses five equal selectable columns and intrinsic height, original labelSmall font metrics and native Heading/Auto hyphenation. No font shrinking, hidden destination, max-line clamp or ellipsis. Native TextLayoutResult retains the original full string/requested font scale with no overflow/ellipsis in32 configurations and160 transitions. Actual NavHost renders and five routes confirm behavior. |
| A8-3 | Medium/P2 test evidence validity | Stage7B acceptance and opt-in recorder share the production navigation component and test-only MainActivity-equivalent window effect. Four opposing app/system-theme pairs match real NavHost flags and rendered status/navigation contrast. No production system-bar defect was demonstrated or production theme behavior changed. |

Changed production paths: `ui/admin/AdminSettingsScreen.kt`, `ui/JournalNavigationBar.kt`, `ui/OpusApp.kt`. Test paths: `Stage7bAcceptanceTest.kt`, `Stage8bAssessmentProbe.kt`, `Stage8cUiRegressionTest.kt`. No database migration or backup/practice architecture change. Existing notification-permission/mobile-consent behavior remains in its ViewModel/UI flow.

## Checks actually executed

- `./gradlew assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug --offline --no-daemon --max-workers=1`: current production gate PASS2m27s,275 JVM cases,0 failures/errors/skips; local lint0 errors/119 warnings. Updated pointer-harness `assembleDebugAndroidTest` PASS25s; no unchanged full local gate repeated after that test-only edit.
- Explicit guarded emulator instrumentation: corrected3-case Stage8C class PASS72.838s. Earlier4-case focused run including existing Admin semantics PASS75.972s; changed refined-layout case independently rerun PASS66.134s. Results are attributed to their recorded inputs, not added as distinct cases.
- Local policy/localization/admin-contract validators PASS. Automatic quality40168589919 PASS(clean wrapper6m45s and six validators). Independent CI2 ata41cd11a1b32af2949c43cb6e5b6aac94a3c2a51: full `connectedDebugAndroidTest`,190 native/188 passed/2 existing external acquisition skips;275 JVM/0 failures/errors/skips; six policy/data/contract validators and schema-diff check PASS. Actual counts/XML and exact checkout SHA are in `ci-verification.json` and the preserved run.
- `python3 scripts/stage8c/verify_evidence.py`, `verify_ci.py`, Python compilation and whitespace/source-scope checks PASS. Original426-file Stage8B manifest remains byte-identical. Current APK/installed hash, ZIP CRC/package/version/trusted signer and compatible `-r` installs verified; no uninstall/signature bypass.

## Render and TalkBack observations

The32 base configurations are EN/CZ × Light/Dark ×font1.0/1.3/1.5/2.0 at360×640 and412×915dp,320dpi. Four additional scaffold configurations and four matched real NavHost opposing-system configurations give40 unique settings configurations.217 accepted after PNG/tree pairs include navigation/scroll supplements;34 raw fresh-before pairs and244 excluded attempt pairs remain preserved. Two final Czech baseline steps lack a focused node and are not used for focus proof. All40 accepted after focus steps require real TalkBack/service/touch exploration and an actual focused node; event streams retain `TYPE_VIEW_ACCESSIBILITY_FOCUSED` sources. Recorder sessions are observations, not JUnit passes. TalkBack16.0.0.738667889, API36, isolated Goldberg8C AVD; before/native on emulator5580, recovered final captures/native on emulator5588. Never a physical phone.

Screenshots actually inspected: both32-configuration navigation contact sheets (all32 navigation crops), four-way before/after Switch focus sheet, all8 opposing-theme scaffold/NavHost bar crops, worst-case Czech2x scrolled row original and eight independent CI1 Settings scaffold navigation crops. Derivatives are under `results/targeted-ui-corrections-2026-10-25/review/`; originals/trees/events remain alongside them. The small2x Czech lower row is initially viewport-clipped; actual scrolling reveals its full243px row above all five tabs. Native component layout independently enforces48dp. No clipping of a scroll viewport is reported as a smaller intrinsic touch target.

Before scaffold status dominant-pair contrast was1.047–1.126:1; after observations5.56–19.48:1,gesture handles5.74–15.25:1. This samples actual RGB colors in icon/handle regions; it is not a WCAG, audible speech, comfort or physical-device certification. Actual NavHost was already correct. Supplemental double-tap injections were unsuccessful and remain UNVERIFIED; the notification prompt from one wrong-coordinate attempt was dismissed without granting app permission. These attempts do not replace native action-count verification or establish an app defect.

## Independent CI recovery and source binding

CI1 [40168644562](https://github.com/stokonn/Goldberg/actions/runs/40168644562) at4d245b is retained as **FAILED**:190 cases/187 passed/1 failure/2 existing skips. The sole failure was test pointer injection targeting SystemUI StatusBar; raw InputDispatcher names the rejected window. Corrected test-only safeDrawing padding and explicit pointer-center/status-inset assertion retain all original semantic/action assertions. Local3-case PASS preceded publishinga41cd11 and one meaningful CI2 dispatch; no unchanged failed source was redispatched. CI2 independent success above closes that gate. CI1's responsive/scaffold cases independently passed and its eight reviewed Settings samples show full navigation labels.

CI has132 warnings/1 hint versus119 local warnings:13 additional CI1 notices were dependency metadata (11GradleDependency,AndroidGradlePluginVersion,NewerVersionAvailable), not new dependency/source edits. No dependency upgrade or warning suppression was introduced. Raw reports retain the exact counts for each run.

All actual after captures remain bound to production source4d245b and its original recorder APK; test-only safe-inset sourcea41cd11 leaves production/recorder helpers byte-identical. `refined-source-binding.json`, `pointer-harness-binding.json`, `production-scope-check.json`, `runtime-artifact-offline.json` and `ci-verification.json` distinguish those inputs. Later documentation-only receipt uses `[skip ci]`; no runtime result is falsely attributed to its later SHA.

## Remaining dependencies and next milestone

Stage6 G1–G9/G11,C4,6.0f and live integration remain OPEN. Cloud advanced independently to56ff724 (Phase1/2 prepared,hosted NO-GO), not adopted. [All13 potential overlaps](INTEGRATION-SEQUENCE.md) retain5 high/7 medium/1 low risks; no merge/conflict resolution was attempted. Resolve contract/model/session/revision/provenance and lifecycle/storage/navigation choices explicitly, union locale/diagnostic records by key/path and preserve newer accepted client decisions. Future route integration must retain the new single Switch owner and responsive five-tab component. Avoid blanket ours/theirs replacements.

Recommend Stage8D: a separately scoped Stage6 contract/consumer and operational-gate readiness milestone, followed by explicitly authorized source adoption and combined-source data/upgrade acceptance. Version/code selection, genuine trusted-release seeded upgrade, hosted policy/legal/operational acceptance and final owner merge/release authorization remain future gates. No additional production fix is proposed in this bounded milestone.

Preservation:2321/2322 original external untracked files remain byte-identical; one external Cloud CLI cache disappeared during independent Cloud progression. No historical evidence/private PMP/APK is changed or missing; Stage8C did not edit external worktrees. Full raw attempted downloads/reports/APKs remain ignored locally. PMP configuration/checkpoints remain private and excluded. Existing [Issue31](https://github.com/stokonn/Goldberg/issues/31) remains OPEN and [Goldberg Development](https://github.com/users/stokonn/projects/2) In Progress for the whole release. [Wiki](https://github.com/stokonn/Goldberg/wiki) and Release publication are unchanged; final documentation receipt/remote SHA are recorded in Issue31 and the final handoff.

Verified acceptance checkpoint: [Issue31 comment6394753327](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6394753327). Final documentation-only remote receipt is appended to Issue31 after publication; it does not create a new runtime claim.

<details>
<summary>Historical execution ledger — superseded by the verified completion record above</summary>

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

### Published source / independent CI checkpoint

Scoped candidate **4d245b46936d640740799dfcc6e8d61f3d44b249** pushed and exact remote SHA verified. [Quality40168589919](https://github.com/stokonn/Goldberg/actions/runs/40168589919) and [full native emulator40168644562](https://github.com/stokonn/Goldberg/actions/runs/40168644562) running at this source;one manual dispatch,scope stage7b/full native,seeded upgrade job excluded. No result transferred to a later docs receipt SHA. [Issue checkpoint6394336495](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6394336495),Project and private PMP synchronized.

Final actual source68d48b… captures now cover all32 intended configurations at verified320dpi/360-or412dp. Both locale/theme2x navigation crops inspected:full original font sizes,no ellipsis or missing destination,2–3 naturally hyphenated lines where needed. Four10-step TalkBack traversals retain populated actual focused nodes and one checkable named row per switch. Remaining opposite-system scaffold/NavHost and real tab/Back/double-tap checks underway;independent CI pending,NOT FIXED yet.

### Final local observation milestone

Evidence verifier PASS:32 unique base configurations at320dpi and360×640/412×915dp,all five named tabs/selected states,three uniquely named checkable localized Switch owners;four actual TalkBack traversals/40focused steps and source events;four matched opposite-system scaffold/NavHost pairs. Original426-file8B manifest remains identical. Captured34 fresh before and217 accepted after PNG/tree pairs;244 attempt pairs retained/excluded. Inspected32 navigation crops,EN/CZ Switch focus comparison andall8opposite-theme bar crops. Observed after status dominant-pair contrast5.56–19.48:1,gesture handles5.74–15.25:1;pixel method is observational,not WCAG/physical-device certification.

Five actual NavHost tab transitions andtwo Android Back transitions PASS after settlement wait. CZ2x lower mobile-consent row initially has61px visible bounds because the viewport clips scroll content;native component >=48dp layout assertions remain PASS. Additional actual scroll reveals full243px row above allfive tabs,without invoking it. Installed production APK bytes match68d48b…;qemu/API36/boot1 andairplane1,wifi0,mobile0readback PASS.

Supplemental real TalkBack double-tap activation **UNVERIFIED**:shell coordinate tap first opened notification prompt (dismissed without granting);subsequent coordinate/hardware mappings didnot produce the expected single state transition. These injection attempts remain raw evidence,not accepted action verification or a proven application defect. Actual TalkBack focus is independently verified;native accessibility ACTION_CLICK/Space/pointer/disabled/exact callback counts passed. First Back snapshot was premature;with0.8ssettlement actual dashboard/profile results passed. No production change followed these host-harness corrections.

Exact-source quality40168589919 **SUCCESS**,clean wrapper6m45s andsixworkflow validators PASS;full native40168644562 still executing. Cloud independently advanced to56ff724 (Phase1/2 prepared,hosted NO-GO),not adopted. Preservation audit:2321/2322 original external untracked files byte-identical,one external Cloud CLI cache absent;no historical evidence/private PMP/APK changed/missing,no external worktree edited by8C. Findings remain NOT FIXED until independent native success.

### Independent CI1 failed / verified test-harness correction

Run40168644562 at4d245b:raw native XML **190cases/187pass/1failure/0errors/2existing external-source skips**,516.18s. Responsive32-layout case independently PASS95.103s;scaffold case PASS11.379s. Sole failure is the pointer phase of Switch regression:InputDispatcher explicitly rejected the event because it would hit `StatusBar` owned by SystemUI. Isolated test Column omitted safe display insets;production Settings uses its established scaffold/insets. This is a test-harness failure,not a failed application callback or verified product regression. Original failed run/source/raw XML/263screens/diagnostics preserved under `ci-native-40168644562-failed/`;full downloaded archive/reports retained ignored locally.

Correction only in Stage8cUiRegressionTest:WindowInsets.safeDrawing padding around isolated Switch content;assert target center lies below system-bar inset;retain strict original names,roles,48dp,state,disabled,keyboard,pointer and exactly-once callback assertions. AndroidTest compile PASS25s;all3focused cases PASS72.838s on5588. Production and recorder helpers byte-identical to4d245b;APK68d48b unchanged. New pointer-harness-binding.json records exact inputs/testAPKf683e9… . Original capture verifier now checks historical4d source binding plus current follow-up test inputs;doesnot rewrite old capture provenance.

CI1 host XML also verifies275JVM/36suites/0failures/errors/skips. CI lint0errors/**132warnings/1hint**,compared with local119warnings:13extra dependency-metadata notices(11GradleDependency,AndroidGradlePluginVersion,NewerVersionAvailable),not source changes or permission to update dependencies. Quality40168589919 remains SUCCESS at identical production source. Next:publish the verified test correction with`[skip ci]`(avoid repeating unchanged successful automatic quality),then one meaningful full-native dispatch at its exact new SHA. No FIXED claim until that independent gate passes.

### Published CI recovery source

Test-only correction/evidence commit **a41cd11a1b32af2949c43cb6e5b6aac94a3c2a51** pushed;exact remote ref readback matches. Production/helpers/artifact remain4d245b/68d48b unchanged. `[skip ci]` avoids redundant successful quality;one meaningful [full-native CI2 run40170295032](https://github.com/stokonn/Goldberg/actions/runs/40170295032) dispatched at this exact source after verified correction/local3casePASS. Scope stage7b excludes seeded-upgrade job. PENDING independent result;findings NOT FIXED. Issue31/Project/private PMP checkpoint synchronized with CI1 failure and exact recovery source.

Additional independent visual review:CI1 eight Settings scaffold navigation crops (smallfont2/standardfont1 ×EN/CZ×Light/Dark) inspected in `review/ci1-scaffold-settings-navigation.png`. All five full labels are retained,including3-line hyphenation when native wrapping differs;these are scaffold evidence,not actual NavHost or physical-device captures. CI1's overall failure remains intact despite these passing UI cases.

Before-evidence qualification:the34fresh baseline PNG/tree pairs are raw captures,not34independent passes. ENLight1/CZDark1.3 each show exactlythree duplicate Text-label/control pairs. The finaltwo CZ traversal snapshots have no focused node and are retained butexcluded from focus proof. All40accepted after focus snapshots independently assert active TalkBack/touch exploration andone actual focused node.

Superseded AI brief checkpoint retained for history (current source/status above):

> Stage8C current checkpoint: semantic candidate1c243626… passed host275JVM/lint and4native cases. Actual5588/320dpi/360dp ENLight1 TalkBack now verifies one Switch stop per row. Initial density/5580transport attempts excluded/preserved. New native linebreak/hyphenation+1dp label inset refinement addresses observed2x orphan letters;new host gate running,changed32-layout native/actual matrix/CI pending. No source commit/CI dispatch/FIXED claim yet. See8C report;private PMP snapshot updated.

</details>
