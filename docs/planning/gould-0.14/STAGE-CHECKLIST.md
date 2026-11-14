# Gould 0.14 — implementation checklist

## One development stage: Activity and weekly progression

Purpose: add a deterministic Activity experience and weekly progression while preserving the existing personal journal and preparing a clean shared Kotlin domain boundary.

Scope: deterministic domain rules; Android/Room persistence and backup; Activity navigation/UI; historical goals, Beta Week, manual activity, weekly streak and Freeze; cosmetic ownership/rewards; temporary navigation hiding; SDK compatibility; documentation, verification, integration, and prerelease.

### Phase 0 — Preflight
- [x] Confirm canonical repository identity and active worktree.
- [x] Confirm starting commit, branch state, and clean status.
- [x] Select one 0.14 feature branch.
- [x] Confirm GitHub authentication and inspect remote PR/release/issue state. PR #50 is open and draft; no merge/tag/release action authorized.
- [x] Inspect signing setup and release workflow. Production signing remains a required later gate; never use the historical debug signing init as production.

### Phase 1 — Domain contracts
- [x] Define week identity, effective goals, Beta Week, day classification, corrections, streak, Freeze, and reward idempotency.
- [x] Extract deterministic logic behind platform-free interfaces in `:activity-domain` `commonMain`.
- [x] Add focused shared/JVM domain tests and compare legacy daily-statistics outputs.

### Phase 2 — Persistence and backup
- [x] Add only required Room records and forward migration from v11.
- [x] Preserve historical dates, profile, achievements, recovery, audio metadata, and backup compatibility in additive schema/backup handling.
- [x] Verify populated v11 upgrade and backup round trip on Android/emulator. `ActivityMigrationTest.populatedV11UpgradePreservesPersonalDataAndAddsEmptyActivityTables` passed on the API 37 emulator (run 2), and the backup round-trip classes `Stage3aBackupTest` (2), `Stage3bBackupTest` (19) and `StudioBackupTest` (10) all passed with zero failures in the same run.

### Phase 3 — Activity and goals UI
- [x] Replace visible Discover tab with Activity while retaining Discover code/routes.
- [x] Implement year heatmap, day details, week overview, statistics details, and retrospective entry/deletion UI.
- [x] Implement goal settings, Beta Week, weekly streak, Day Off/Miss, and automatic Freeze logic.
- [x] Implement cosmetic catalog, durable ownership, weekly reward, and collection UI.
- [x] Hide visible moderation/admin entry; retain implementation.

### Phase 4 — SDK, documentation, and release verification
- [x] Complete API 37/minSdk 30 compatibility assessment and changes.
- [x] Update user/developer/release documentation; emulator screenshots remain pending.
- [x] Run source-final host JVM, static migration/backup/localization, lint and instrumentation compilation checks.
- [x] Execute populated v11 migration/backup/repository instrumentation and Activity UI acceptance on a disposable Android 17/API 37.0 emulator. First execution ever on API 37 (run 1, pre-fix): 279 tests / 17 failures / 0 errors / 13 skipped, all 17 traced to 4 test-side defects (stale schema-11 assertions, one contradicted manual-activity bound, an API-36-only venue pin, a v6-only backup gate) — no production-code defect found. After the five test/contract fixes, run 2 on the fixed `google_apis` AVD `gould014-api37-gapi` (`target=android-37.0`, cmdline-tools 23.0): **279 tests / 0 failures / 0 errors / 13 skipped, 413.9 s, BUILD SUCCESSFUL, exit 0** (`app/build/outputs/androidTest-results/connected/debug/TEST-gould014-api37-gapi(AVD) - 17-_app-.xml`; evidence archived at `stage5-ci-results-instr-api37-r2/`). The 13 skips are explicit `assumeTrue` runner-arg gates (synthetic Stage8, Samsung UI, portrait metadata, launcher, provider fixture) — no `@Ignore`, no excluded classes.
- [ ] Verify release artifact identity, trusted production signature, signed in-place upgrade, and PR CI.

### Phase 5 — GitHub integration and prerelease
- [ ] Synchronize one Issue/checklist and Project state when GitHub access is available.
- [ ] Push branch, open one PR, pass CI, and merge normally preserving commits.
- [ ] Publish and verify prerelease v0.14.0.

### Verification and completion status
- Status: IN PROGRESS; domain, persistence, and UI implementation exists, but final verification and review remain open.
- Preflight/integration: active isolated worktree `feature/gould-0.14`; fetched `origin/main` at `00ff0890e76392801b481e6b803db3e1c6a8a4ee` and `origin/feature/gould-0.14` at the expected implementation commit. Inspected all 45 main-only commits; their changed files are limited to current-status/maintenance guidance, changelog and Wiki. Integrated with normal merge commit `dd1ee4f`, preserving all seven implementation commits and main history. GitHub CLI account `stokonn` authentication is restored and PR #50 was inspected; it remains open/draft. No merge/tag/release action has been taken.
- Current implementation: new JVM-targeted KMP domain module; Room schema 12 with additive 11→12 migration; backup format v7/journal schema12; Activity navigation/UI; manual activity; weekly goals/Freeze and cosmetic ownership/reward. Discover and admin implementations remain present.
- Verification so far: final mandatory clean RELEASE host gate passed in 328s after the last app-source edit: debug build, 403 JVM tests (0 failures/errors/skips), instrumentation APK compilation, lint and five policy/static checks. Localization 1,281 EN/CZ, migration DDL v1–v12, backup fixtures and history-paging checks passed. A final bounded unsigned R8 release build passed in 310s and produced `app-release-unsigned.apk` (9,131,062 bytes; SHA-256 `4d9e407a28d4c9d48e8865b3bf3723b8d60a1d38652db827257db53c4976d7fb`), metadata package `com.opus.piano`, code16 / `0.14.0-dev`, minSdk30, target37. It is unsigned and not release evidence. Native instrumentation and visual UI inspection remain unexecuted.
- SDK assessment: Android 17/API37 preview is installable as SDK package `platforms;android-37.0`; the earlier CI package `platforms;android-37` was invalid. The quality workflow now passes with the exact package, preserving compile/target37 and minSdk30. The emulator workflow also installs API 37 successfully. minSdk30 drops update eligibility for API26–29 users while leaving their installed local data intact; this compatibility cost is documented. Large-screen and widget API37 runtime acceptance remains pending.
- Current GitHub acceptance checkpoint (2026-11-12, source `c1516a5`): PR #50 is open and draft. Required host quality run `43243161485` and repository maintenance checks passed. Emulator workflow `43243176345` failed both jobs. API37 built app and test APKs, booted Android 17 and verified QEMU emulator `emulator-5554`, but system services became unavailable during APK install (`Broken pipe`, missing `activity` service); zero tests ran. API36 completed a seeded v8→12 app upgrade and verified the original data before the verifier's table-set assertion failed. The verifier assumed no schema-added tables and had not allowed the three schema-9 catalogue tables, the schema-11 badge notice table, or schema-12's seven Activity/cosmetic tables. Corrections now enumerate those expected additions by target schema; API37 uses the previously accepted `swangle` software renderer after hosted SwiftShader instability. These changes are uncommitted pending repeat workflow validation. The dedicated `ActivityMigrationTest.populatedV11UpgradePreservesPersonalDataAndAddsEmptyActivityTables` covers populated v11→12 and checks protected data, empty new tables and foreign keys; it has not run on an emulator. Artifact download returned HTTP 503 from GitHub artifact storage due to account egress limits.
- Root-cause and local-acceptance checkpoint (2026-11-13, source `02c8701` + uncommitted CI corrections): the API37 zero-test abort was reproduced locally and root-caused end to end. cmdline-tools 12.0 cannot parse the dotted API level `37.0`: `avdmanager` exits 0 but writes `target=android-0` into the AVD `.ini`, gfxstream is misconfigured and the guest surfaceflinger crash-loops in `mapper.ranchu.so` (`Assertion failed: !rcEnc->featureInfo()->hasReadColorBufferDma`) while `sys.boot_completed=1` stays 1 — so `mount` (IStorageManager) publishes too late and `package install-create` dies with `StorageManager.getVolumes()` NPE, producing well-formed `tests="0"` XML. Evidence: both API37 AVD `.ini` files had `target=android-0` vs. the API36 AVD's `target=android-36`; upstream `ReactiveCircus/android-emulator-runner#494` documents the identical failure and bundles cmdline-tools 23.0 (not released in pinned v2.38.0), `actions/runner-images#14484` confirms the runner-side A/B. Local A/B with cmdline-tools 23.0: AVD recreated as `target=android-37.0`, **0 surfaceflinger crashes over 30 s**, `input/settings/package/activity/mount` all published, APK install `Success`. Corrections (uncommitted): shared readiness gate also requires `mount`; retry criterion counts tests in result XML (zero-test XML = infrastructure, `tests>0` never retried); upgrade table assertions derived from Room schema JSONs (`scripts/upgrade_table_expectations.py`, 12 unit tests); both workflow jobs now guarantee cmdline-tools ≥23 before AVD creation (setup-android input bumped to build `16111833` = 23.0; fail-closed check on the resolved `avdmanager`), validated locally including edge cases. Local API36 acceptance `bash scripts/ci_stage6_acceptance.sh` **PASSED (exit 0)** with the schema-12 post-restart check: baseline 0.10.1 schema 8 → 12, all 17 expected additive tables incl. `personal_catalogue_*`, rows/preferences unchanged, 37 sessions, FK none, integrity ok, offline + network/Data-Saver transitions verified (first pass; its evidence directory was superseded by the code-matched re-run below). Full API37 instrumentation suite **passed on the fixed AVD** (279 tests / 0 failures / 0 errors / 13 skipped, 413.9 s — first clean full execution on API 37; the playstore CI target remains the workflow's configuration, local graphics stability was proven on `google_apis` after the target fix).
- Production signing mechanism resolved by inspection (2026-11-13, no release build performed): the trusted signing init script exists in-repo at `docs/releases/goldberg-0.11-stable-signing.init.gradle`, SHA-256 `05b9ed84182ca8ab63e34b36ea17de9c2434f089a2ffad6d0b6e689ffbd629e2` — byte-identical to the trusted `05b9ed84…` script recorded in `docs/ai/HANDOFF-HISTORY.md`; it attaches `signingConfigs.debug` to the release buildType and contains no keystore material (why it is committable). The host `~/.android/debug.keystore` certificate SHA-256 is `53eface…`, the trusted production certificate, and the documented invocation is `./gradlew -I docs/releases/goldberg-0.11-stable-signing.init.gradle assembleRelease …` (per `docs/planning/gould-0.13/CHECKPOINT.md`). Signed in-place upgrade, runtime UI/theme/locale/accessibility acceptance and screenshots remain incomplete. PR #50 must stay draft. No merge, tag or release is authorized; the fresh clean release build itself is performed only after explicit approval. No physical phone or `/home/stokonn/Projects/Opus` was accessed or modified.
- Local commits on `feature/gould-0.14`: `181c4fc` shared domain; `fed0399` Room and backup; `576f30a` Activity UI/navigation; `1a416b5` cosmetic catalog; `c643208` SDK37 CI; `862ba64` docs/checkpoint. The tree was clean after these commits. Final RELEASE audit records are at `b436294-dirty`; the application/build source fingerprint is the same committed source, with documentation/checkpoint commits afterward.
- Code-matched local acceptance checkpoint (2026-11-13, final uncommitted tree): the full API37 instrumentation suite passed **279 tests / 0 failures / 0 errors / 13 skipped in 413.9 s (exit 0)** on the fixed AVD — 67 classes green including `ActivityMigrationTest.populatedV11UpgradePreservesPersonalDataAndAddsEmptyActivityTables`, with all 13 skips being explicit `assumeTrue` runner-arg gates and zero `@Ignore` anywhere in `app/src/androidTest`. Immediately afterwards, `bash scripts/ci_stage6_acceptance.sh` (the final `mount`-gated version) passed **exit 0** on a freshly wiped API36 `stage5-api36` emulator: baseline 0.10.1 debug rebuild schema 8 → 12, `assert_table_transition` accepted exactly the 17 Room-schema-JSON-derived additive tables, 37 sessions, original rows/badge dates/preferences unchanged, FK pass, integrity ok, post-restart `PRAGMA user_version: 12`, offline established, Wi-Fi→mobile default-network transition and Data Saver readback verified, 5+5 startup samples captured (candidate 621–798 ms vs baseline 722–926 ms). Evidence: `stage5-ci-results-instr-api37-r2/` and `stage5-ci-results-acceptance-api36-r2/` (both gitignored; `result.json` SHA-256 `721a619fa564f35609c3fdd65ddf67cdecf89e63417378f7c2f4f5dfee00511d`). Host JVM 403/403 and all seven static checks plus 12/12 upgrade-table unit tests pass on this tree. Remaining before PR #50 can be updated: commit, push, dispatch the emulator workflow on the exact commit, and inspect CI evidence.
- Next: commit all corrections as normal commits (no history rewrite), push, and dispatch `.github/workflows/stage5-emulator.yml` (scope `all`) on the exact resulting commit; inspect CI test counts, runtime and the cmdline-tools ensure step before updating PR #50's description. Continue investigating trusted production signing without exposing or replacing credentials. Keep PR draft and stop short of merge/tag/release absent explicit approval.

## 2026-11-13 — owner-requested UX and Activity corrections

Purpose: correct the Profile collection entry, make manual activity date bounds
match the requested three-day window, improve Activity heatmap navigation and
card layout, make cosmetics a select-then-apply flow, repair weekly reward
eligibility for completed goals, and make the resizable weekly widget display
a weekly heatmap. The five `~/Downloads/screen014/*.png` references were
inspected; they are review-only and must not be packaged.

Scope: Profile and Activity Compose screens, Activity domain/repository rules,
cosmetic collection presentation and reward persistence, weekly widget
projection/rendering, focused JVM and Android tests, and this checklist. No
Room schema change is expected; backup, signing, package and application IDs
must remain compatible.

- [x] Put the collection icon beside Profile's achievements action and remove
  duplicate inline collection controls from Activity (JVM compilation passed).
- [x] Allow exactly today, yesterday, and two days ago for manual entries;
  enforce the same window and first-use lower bound in repository validation (domain/JVM tests passed; Android repository instrumentation is pending).
- [x] Ensure a manual entry is stored on its selected date and appears in daily,
  weekly, and year activity projections (projection/JVM and API 37 Room
  instrumentation passed, including past-date bounds and historical sessions).
- [x] Improve responsive Activity card alignment, heatmap day ordering and
  current-year scroll position; add the Goals action affordance (Activity
  rendered and was visually inspected on API 37; broad size/theme review remains).
- [x] Keep cosmetics on the dedicated screen with distinct selected, owned and
  equipped states and an explicit Apply action (Room ownership/apply/reopen
  instrumentation passed; direct owned-item collection UI review remains).
- [x] Award one durable cosmetic per completed perfect goal week (session goal
  plus optional minutes goal), including eligible historical weeks, exactly
  once; Room reopen/four-week instrumentation passed on API 37.
- [x] Render the weekly widget as a Monday-to-Sunday heatmap and adapt the
  labels/content to supported size and font-scale combinations (projection and
  9 API 37 RemoteViews render/size/font tests passed; launcher-host placement
  and horizontal/vertical resizing manually passed; activity-driven refresh
  remains pending).
- [x] Run focused domain/JVM tests, relevant database/instrumentation tests,
  lint and instrumentation APK compilation. Domain/app JVM tests passed (414;
  0 failures/errors/skips), lint and instrumentation compilation passed; the
  API 37 focused runtime batch later passed 21/21 with zero skips.
- [ ] Run full instrumentation/release, trusted signature/artifact identity,
  and safe in-place upgrade checks; no signed release APK was created.
- [x] Capture actual emulator screenshots and record results/limitations;
  keep owned-cosmetic interaction, widget refresh and release checks open. No commit,
  push, merge, tag or publication occurred.

Progress: implementation changes are present in this follow-up worktree. The five
reference screenshots under `~/Downloads/screen014` were inspected and are not
packaged. Focused domain/app JVM tests passed (414 tests total, 0 failures, 0
errors, 0 skipped); `lintDebug`, `assembleDebugAndroidTest`, migration DDL, backup
fixture, localization and `git diff --check` checks passed. The new Room reward
persistence, database migration, cosmetic Apply boundary, repository and widget
instrumentation subsequently passed on API 37 (21 tests, zero failures/errors/
skips). The earlier exit 139 was root-caused and repaired for the valid API 37
AVD/renderer configuration below. Home/Activity and launcher screenshots were
inspected. Full-suite, complete collection-screen UI, widget activity refresh,
safe in-place upgrade, and signed artifact gates remain pending. No release APK
was created and no GitHub/Git operation beyond this isolated uncommitted worktree
was performed.

## 2026-11-13 — emulator crash diagnosis and focused runtime recovery

Purpose: identify why API 37 QEMU exited 139, preserve Fedora security settings,
and establish a repeatable API 37 startup/readiness/test/shutdown procedure so
the current UX follow-up can receive actual runtime verification.

Scope: host/emulator diagnostics, a local emulator verification runner, focused
Android instrumentation, cosmetic Apply persistence coverage, fixture
expectations exposed by instrumentation, maintenance documentation, and this
checklist. No release artifact, signing material, user database, AVD deletion,
GitHub operation, or physical device is in scope.

- [x] Inspect Fedora/kernel/CPU/KVM, SDK/emulator/system images/AVDs, RAM/disk/
  limits, graphics configuration, processes, audit records, and coredumps.
- [x] Trace both host QEMU exit-139 events to `RenderThread` SIGSEGV
  (`SEGV_ACCERR`) and SELinux `execheap` AVC records under
  `swiftshader_indirect`; identify the separate API 37 Play Store AVD metadata
  error (`target=android-0`). Preserve both cores and affected AVD.
- [x] Use the valid API 37.0 Google APIs AVD and headless `swangle` renderer;
  verify KVM stays usable and guest SELinux remains Enforcing.
- [x] Add a guard-railed API 37 verification script with AVD/version/serial
  validation, boot + mount readiness, one-device policy, launcher smoke,
  focused tests, and clean console shutdown.
- [x] Run Activity repository/reward, populated v11 migration, widget, and
  responsive tab/accessibility instrumentation on API 37; record actual result.
- [x] Correct three exposed test-fixture expectations (weekly totals omit a
  prior-Sunday event, reward weeks must close before grant, 60 seconds is one
  minute) and add cosmetic owned-only Apply/loadout persistence across Room
  reopen.
- [x] Launch MainActivity, navigate to Activity, and inspect Home/Activity
  screenshots and the visible Activity accessibility label.
- [ ] Run remaining full-suite/release and in-place upgrade gates on the final
  source commit; verify widget refresh after a successful manual activity and
  full cosmetic UI owned-item selection/Apply/recreation.
- [ ] Keep the signed artifact, signature, and release publication gates open;
  no signed release APK exists.

Verification (2026-11-13): host is Fedora 45, kernel 7.3.3-300.fc45.x86_64,
Intel Core Ultra 5 235T/VT-x. On the host `/dev/kvm` is mode 0666 and
read/write-accessible; `emulator -accel-check` reports KVM v12 usable. SDK is
Android Emulator 37.2.12, platform-tools 37.0.1, cmdline-tools 23.0, API 37.0
Google APIs image revision 7. Host resources were ample. Exact crash evidence and
the permanent startup procedure are documented in
[`docs/maintenance/ANDROID-EMULATOR-FEDORA.md`](../../maintenance/ANDROID-EMULATOR-FEDORA.md).
The final end-to-end runner
`GOULD014_RESULTS_DIR=/tmp/gould014-emulator-verification-final scripts/run-gould014-emulator-verification.sh`
passed readiness on explicit
`emulator-5586` (`sys.boot_completed=1`, mount found, API 37, ro.kernel.qemu=1),
installed/started `MainActivity` cold (`Status: ok`, 874 ms), passed the
`AppLanguageTest` instrumentation smoke (1/1), then passed 21 focused tests
(8 repository/reward/cosmetic persistence, 1 populated migration, 9 widget, 3
responsive UI/accessibility; zero failures, errors, or skips). Responsive UI
checks covered 32 configurations. Its clean
shutdown left no attached emulator or QEMU process and no new QEMU crash/SELinux
AVC appeared in the host journal. Screenshot evidence
`/tmp/gould014-api37-ui/home-ready.png` and `activity.png` was visually
inspected. The cosmetic Apply persistence assertion is at the Room boundary;
direct collection-screen selection/application remains pending. No full instrumentation suite,
signed build, upgrade journey, actual launcher widget placement/resize, commit,
push, PR update, merge, or publication was performed.

Final verification continuation (2026-11-13): opened Profile and used its
collection shortcut. On the empty disposable install, the collection showed
the six locked entries with readable names, rarity, lock status, and unlock
requirements; no owned item was available to exercise selection/Apply or app
recreation persistence. On the API 37 Pixel launcher, added the offered Weekly
Activity 2×2 widget, inspected it, and resized it horizontally and vertically.
The resized render showed the title, weekly total/date, seven day blocks,
session/day count, and verification timestamp without clipping. After a
manual-activity dialog attempt, the widget still displayed zero, so activity
refresh and manual-entry correctness remain unverified. Screenshots inspected:
`/tmp/gould014-collection-final.png`,
`/tmp/gould014-widget-placement.png`,
`/tmp/gould014-widget-wide.png`,
`/tmp/gould014-widget-large.png`, and
`/tmp/gould014-widget-after-manual.png`. `:app:lintDebug` passed and
`git diff --check` passed. The full suite was not run because it adds broad
coverage beyond the already passing focused batch without resolving the open
UI and launcher data-path checks; the 414 JVM test pass and prior 21-test API
37 instrumentation pass were not repeated. No production APK exists in the
worktree, no production signing configuration or key was found (only
`~/.android/debug.keystore`, explicitly disallowed for this artifact), and no
previous-release APK fixture is available here. Therefore signing, APK install,
in-place upgrade, and release metadata/hash verification remain blocked. The
emulator was shut down cleanly; its ADB transport and server were cleared.

Current status: **IN PROGRESS**. The emulator exit-139 blocker is resolved for
the validated API 37 AVD/renderer combination. Launcher placement and resizing
passed manually; full-suite, widget activity refresh, owned-cosmetic Apply UI,
production signing, and in-place upgrade remain pending.

## 2026-11-14 — six reported UX/data defects: final follow-up verification

Purpose: complete the six owner-reported Activity defects by making manual and
recorded practice data share Home/Profile/Activity statistics, enforcing the
three-calendar-day manual-entry window, tightening weekly heatmap spacing,
placing goal editing below Profile's edit icon, recovering and applying perfect
week cosmetic rewards, and keeping Day Off labels separate from weekly Freeze
rescue accounting.

Scope: `:activity-domain` rules, Room repository projections and reward scans,
Activity/ViewModel/Home/Profile Compose UI, weekly widget rendering/projection,
localized text, JVM and Android regression tests, and this development record.
No schema, package, version, version-code, signing identity, commit, branch, or
remote repository change was made.

- [x] Manual and recorded sessions feed the same totals, duration and daily
  streak projection; verify recomposition and persistence on database reopen.
- [x] Accept manual activity for today, yesterday, and two days ago; reject
  older dates, including around a week boundary.
- [x] Reduce weekly heatmap spacing and cover projection/render size behavior.
- [x] Place the weekly-goal action directly under Profile's edit icon and
  verify the goal editor opens from that location.
- [x] Verify completed non-Beta perfect-week eligibility against session and
  optional minute goals; grant once, persist ownership, show owned/locked
  collection states, apply only owned items, and retain loadout after reopen.
- [x] Verify Day Off labels do not change weekly success or Freeze balance;
  only a closed weekly goal failure consumes one Freeze.
- [x] Run focused API 37 UI/repository/migration/widget instrumentation and
  clean host RELEASE gates.
- [x] Build and inspect a fresh signed stable-mode APK with version 0.14.0,
  code 16, and the trusted certificate.
- [ ] Run signed in-place upgrade against the published baseline and complete
  manual launcher-widget refresh/resize and broader visual review before any
  publication decision.

Verification on 2026-11-14: `:activity-domain:jvmTest` and
`:app:testDebugUnitTest` passed (Gradle reused the domain task output; the clean
RELEASE run executed/loaded the app unit-test results: 405 tests, zero
failures/errors/skips). `GOULD014_RESULTS_DIR=/tmp/gould014-ux-followup-final
scripts/run-gould014-emulator-verification.sh` passed API 37 readiness on the
explicit disposable `emulator-5586`, cold MainActivity launch, AppLanguage
smoke (1/1), and focused Android tests (24/24, zero failures/errors/skips).
The focused classes were `ActivityRepositoryTest`, `ActivityMigrationTest`,
`WidgetTest`, and `Stage8cUiRegressionTest`; this included the Profile goal
placement and collection selection/Apply/persistence UI flow. The first
unmanaged attempt lost its emulator during test 3 and produced incomplete
diagnostics; the guarded run completed successfully and is the result used
here. No screenshot was captured during this final run.

`scripts/verify-release.sh` passed in 80 seconds: clean debug build, 405 app
unit tests, instrumentation APK compilation, lint, and all five repository
policy checks (agent policy, 1,287 EN/CZ resources, Room migrations, backup
fixtures, and history paging). Its first attempt stopped before Gradle tasks
because the sandbox could not create the Gradle-cache lock; the failed attempt
and successful rerun are both retained in the verification audit. `./gradlew -I
docs/releases/goldberg-0.11-stable-signing.init.gradle -PgoldbergStable=true
assembleRelease lintRelease --no-daemon --max-workers=1` passed in 2m14s.
Artifact: `app/build/outputs/apk/release/app-release.apk`, package
`com.opus.piano`, version `0.14.0`, code 16, `debuggable=false`, ZIP integrity
passed, v2 signature verified, certificate SHA-256
`53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`, APK
SHA-256 `0fe2c9d546148f983a26cb62021c4e4011980ffc86928c935e2e430a30305dcf`.
The previous APK was preserved at `/tmp/gould014-prior-release-before-clean.apk`
with SHA-256 `ff88b504ae42ff884347e6c5f8baed7ba30eec4d4c8619fb1e4b71f28de97eb3`.

Remaining: no signed in-place upgrade was run; no final-run screenshots were
captured; launcher-host widget refresh after adding a manual activity and
broader theme/font-size visual checks remain unverified. The signed APK is a
review artifact only; no commit, push, merge, tag, PR update, or publication
occurred. Current work remains uncommitted on
`feature/gould-0.14-ux-followup` at `e20aacb` plus the preserved working-tree
changes.

## 2026-11-14 — compact heatmap, Activity goal action and claimable rewards

Purpose: finish the owner-requested UX adjustments by tightening the contribution
grid, moving Weekly Goal editing into Activity, and changing perfect-week
cosmetics from automatic ownership to an explicit, durable Claim flow.

Scope: Activity/Profile Compose UI, weekly widget sizing, existing Activity
Room grant/ownership records, localized strings, domain/repository/UI tests,
and the 0.14 architecture, guide, FAQ, release and stage documentation. This
reuses schema 12 and the existing 0.14 reward table; no database migration,
package/version/version-code change, or signing change is in scope.

- [x] Tighten annual heatmap cells while retaining 48dp touch targets; use
  proportional compact weekly widget cells and add multi-size spacing tests.
- [x] Remove Weekly Goal action/state from Profile and put the edit pencil in
  Activity's goal section; keep the existing editor, validation and save path.
- [x] Persist eligible rewards as pending grants without writing ownership.
- [x] Claim atomically writes cosmetic ownership and the claim timestamp;
  repeated claims are rejected, pending IDs are reserved across weeks, and
  app restart preserves both pending and claimed states.
- [x] Preserve one cosmetic reward for every eligible completed perfect week;
  the separate four-consecutive-week milestone awards a Streak Freeze and does
  not replace or delay any weekly cosmetic.
- [x] Add an animated claim/reveal dialog and English/Czech Claim, status,
  failure, retry and week-date labels; collection Apply remains unchanged.
- [x] Add repository regressions for pending creation, ownership only after
  claim, unique rewards, retry/duplicate handling, legacy claims, restart
  persistence, and exhausted collections.
- [x] Add non-visual Compose semantics tests for opening GoalEditor from
  Activity and claiming/revealing a pending cosmetic; both compile with the
  Android test target. The editor-opening test passed on API 36. The claim UI
  test has not yet executed because the AVD was not visible to ADB in this
  session; repository claim/persistence tests passed separately.
- [ ] Owner phone visual review, motion/accessibility review, and any requested
  manual adjustment remain pending; no visual emulator testing was performed.

Verification on 2026-11-14: `./gradlew :activity-domain:jvmTest
:app:testDebugUnitTest :app:assembleDebugAndroidTest :app:lintDebug --no-daemon
--max-workers=1 --no-parallel` passed (domain task up to date; app unit tests
passed; app and Android test targets compiled; lint passed). Focused API 37
instrumentation passed all **11 `ActivityRepositoryTest` tests**. The combined
API 37 attempt reported one failure in `ActivityGoalEditorUiTest`, before its
assertions, because Espresso 3.6.2 could not reflectively find
`InputManager.getInstance` on API 37. The same single non-visual semantics test
then passed on the explicit disposable API 36 `stage5-api36` AVD. No screenshots,
coordinate clicks, visual comparison, or app-screen navigation were performed.

The earlier signed APK recorded above predates this claim-flow source change;
no new signed APK, install-upgrade test, commit, push, merge, tag, or
publication was performed. `scripts/run-gould014-emulator-verification.sh` was
used only with focused repository + dialog test selectors for a minimal
non-visual API 37 startup, not as a full visual workflow. Detailed output is
in `/tmp/gould014-final-ux-targeted/`; API 36 run used explicit
`emulator-5586` and was shut down cleanly.

Current status: implementation, repository regressions, Android target
compilation and lint are complete; the claim-dialog Compose semantics test is
compiled but awaits execution on a working disposable emulator. Visual/manual
owner review is pending. Working tree remains uncommitted on
`feature/gould-0.14-ux-followup` at `e20aacb` plus all preserved changes.

## 2026-11-14 — real-device UX correction and signed review APK

Owner phone testing found the Weekly Goal pencil still nested at the bottom of
the Week Overview card and the Claim action absent. Source review confirmed the
pencil, goal progress and Claim row were all nested after `WeekOverview`; Claim
was additionally gated by `snapshot != null`, even though pending rewards are
collected independently. Split Weekly Goal into a distinct section above Week
Overview, aligned the edit action with the section heading, moved goal progress
into that section, and made its Claim row render directly from `pendingReward`
even when the summary snapshot is unavailable. Profile remains without the
goal action. The Compose regression now asserts that Claim is displayed only
for a pending reward, that Claim and edit are descendants of the same Weekly
Goal section, and that the edit action opens the existing editor.

Checks: `:activity-domain:jvmTest`, `:app:testDebugUnitTest`,
`:app:assembleDebugAndroidTest`, and `:app:lintDebug` passed; the domain and
app JVM test tasks were up-to-date, Android app/test Kotlin compilation passed,
and lint passed. The corrected Compose semantics test compiled but could not be
executed: the explicitly started disposable API 36 AVD did not stay registered
with ADB, and this host has no `/dev/kvm`. No visual emulator or screenshot
testing was performed. `git diff --check` passed.

Fresh signed artifact after these source changes:
`app/build/outputs/apk/release/app-release.apk`; `assembleRelease` and
`lintRelease` passed. APK verification passed: package `com.opus.piano`, version
`0.14.0`, code 16, `debuggable=false`, v2 signature valid, trusted certificate
SHA-256 `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`, ZIP
integrity passed, APK SHA-256
`ad9343a5b3aa4108c4ea6d8fba3bde46cc3924c187bdf6761428fc3f88b15f8a`.
Visual phone review remains with the owner. No Git or publication operation
was performed.

## 2026-11-14 — weekly cosmetic state and separate Freeze milestone

Product rule confirmation: each closed, non-Beta perfect week earns one
cosmetic; four consecutive successful weeks separately earn one Freeze.
`evaluateRewards` retains per-week eligibility and week-keyed idempotency;
Freeze balance/progress is independently replayed through `closeWeek` and
`evaluateWeeks`. A failed week resets only the Freeze milestone progress and
does not remove reward grants. The Claim status pipeline now observes pending
grants and the latest claimed/legacy-owned grant, then maps to explicit Loading,
Locked, Claim, and Claimed UI states. This keeps a pending reward visible while
the summary loads, presents Locked when evaluation completes without a reward,
and keeps a claimed cosmetic out of Claim after restarts. Multiple pending
weeks remain individually claimable in week order.

Added repository cases for one perfect-week reward, four weekly cosmetics plus
one separate Freeze, broken-run reset without cosmetic revocation, repeated
evaluation, multi-week claiming, and Room reopen. Added JVM mapping tests for
Loading/Locked/Claim/Claimed and a domain regression for the Freeze milestone.
`:activity-domain:jvmTest`, `:app:testDebugUnitTest`,
`:app:assembleDebugAndroidTest`, and `:app:lintDebug` passed; latest runs
reported 12 domain tests and 406 app unit tests, zero failures/errors/skips.
Android repository and Compose tests compiled but were not executed because no
ADB emulator is available on this host (`/dev/kvm` is absent). No visual
emulator testing was performed. Fresh signed APK built after the checks:
`app/build/outputs/apk/release/app-release.apk`; `lintRelease` and
`assembleRelease` passed. Package `com.opus.piano`, version `0.14.0`, code 16,
`debuggable=false`, APK v2 signature verified with trusted certificate SHA-256
`53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`, ZIP
integrity passed; artifact SHA-256
`a49a77a453b8de1c584e1c9537328d0d927491bcea09e9aaeb1e4b8b42205b8e`.

## 2026-11-14 — Beta cosmetics, claim synchronization and Activity header

Purpose: repair the Beta Week reward lock, apply all configured weekly goals to
Freeze milestones, prevent stale pending/claimed emissions, and place the
single weekly-goal editor action in the Activity header.

- [x] Allow a completed Beta Week to grant its independent weekly cosmetic;
  Beta remains neutral to Streak and Freeze progress.
- [x] Require the configured optional minutes goal for both cosmetic and
  official streak-week eligibility.
- [x] Read pending and claimed state transactionally on Room flow invalidation
  and immediately after a successful claim.
- [x] Move the goal edit action to the Activity heading's upper-right slot;
  leave collection access on Profile.
- [x] Update the reactive repository regression for four independent weekly
  grants and verify a claim leaves another available.
- [x] Run the clean host gate, focused repository instrumentation, signed
  artifact checks, and preserve all existing work without Git history changes.
- [x] Keep official streak tracking Monday-bound, per owner confirmation.
  Partial first-use weeks remain Beta; the first actual streak week is a full
  Monday–Sunday week using normal configured goals. Therefore no partial
  non-Beta week can occur and no separate prorated-target persistence/schema
  is needed under the approved behavior.
- [ ] Owner manual phone UI review remains pending; no visual emulator or
  screenshot comparisons were run.

Root cause for the reported `6/3` / “Weekly session goal met” / “Reward
locked” / “Beta Week · streak starts next Monday” state: `perfectWeek` rejected
all Beta weeks before evaluating goals, even though Beta is only a Streak
transition and cosmetics are independent. The configured optional minutes
goal still applies if present. The screenshot's stored goal configuration was
not available to inspect, so whether its optional minutes target was also
unmet cannot be established from this worktree.

Verification on 2026-11-14: `scripts/verify-release.sh` passed (clean debug
build, app JVM tests, instrumentation APK compilation, lint and five policy
checks: agent policy, localization, migrations, backup fixtures and paging).
Shared-domain JVM tests passed (12); app JVM tests passed (410), with zero
failures/errors/skips. Focused API 37 `ActivityRepositoryTest` passed 14/14 on
`emulator-5554` (`ro.kernel.qemu=1`), including Beta cosmetic grant,
independent weekly cosmetics, Freeze breaks/repeated milestones, claim/reopen,
and reactive ViewModel claims. Instrumentation APK compilation passed. No
visual/screenshot tests were run. The updated Compose header semantics test
compiled, but its API 36 emulator failed to register with ADB during startup;
the header placement and Profile shortcut therefore remain runtime-unverified.
`git diff --check` passed.

Fresh signed build passed:
`./gradlew -I docs/releases/goldberg-0.11-stable-signing.init.gradle
-PgoldbergStable=true clean assembleRelease lintRelease --no-daemon
--max-workers=1 --no-parallel --offline`. Artifact
`app/build/outputs/apk/release/app-release.apk`: package `com.opus.piano`,
version `0.14.0`, code 16, `debuggable=false`, APK v2 verified, trusted
certificate SHA-256
`53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`, ZIP
integrity passed, size 8,947,326 bytes, SHA-256
`99c42a6cd3803c80a3237dee5a3a1c600a460fa812cc3d26ad3ecfdec53d0e92`.
Certificate matches the preserved prior 0.14 review APK; a published-version
in-place upgrade was not run. No commit, push, merge, tag, or publication was
performed. Branch remains `feature/gould-0.14-ux-followup` at `e20aacb` with
the original dirty work and this follow-up preserved.

## 2026-11-14 — active Beta-week cosmetic eligibility regression

The screenshot's exact persisted values (6 sessions / goal 3 and 96 minutes /
goal 90) exposed a second eligibility gate after Beta was removed from
`perfectWeek`: `evaluateRewards` only granted when `weekMonday < currentMonday`.
That closed-week guard still excluded the *active* Beta week. Reward UI state
was correctly showing LOCKED because no `activity_reward_grants` row existed;
the pending-grant Flow, ownership query, ViewModel and Compose state were not
the cause.

Changed the repository eligibility gate so a fulfilled Beta week can grant
while active, while other active weeks remain closed before granting. Grant
creation remains persisted and idempotent by week Monday. Beta still returns
`null` from streak `weekSuccess`, so it cannot advance streak or Freeze
milestones. Added exact API repository regression with six 16-minute sessions
(6/3 and 96/90), checking one pending persisted cosmetic, repeat-evaluation
idempotency, and zero Freeze progress.

Verification: `scripts/verify-fast.sh` passed (compile; 410 app JVM tests,
zero failures/errors/skips; localization, migrations, backup and paging static
checks). API 37 `ActivityRepositoryTest` passed **15/15**, including the exact
active Beta scenario. The earlier signed APK recorded above predates this
repository eligibility fix and is stale for this source; regenerate after the
clean host RELEASE gate. No visual tests, commit, push, merge, tag, or
publication were performed.

Final delivery refresh: `scripts/verify-release.sh` passed in 59 seconds after
the active-Beta grant correction. `scripts/verify-fast.sh` also passed with
410/410 app JVM tests and all static checks. API 37 repository instrumentation
passed 15/15, and the exact 6/3 + 96/90 regression passed individually.
Fresh signed `assembleRelease lintRelease` passed. The current artifact at
`app/build/outputs/apk/release/app-release.apk` is package `com.opus.piano`,
version `0.14.0` / code 16, `debuggable=false`, v2-signature verified, ZIP
integrity valid, and has trusted certificate SHA-256
`53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e` and APK
SHA-256 `62c83ddef804a83b71bd05f3f0b09cec29a0784902ad79b0b16ec8bae75b1f2c`.
No commit, push, merge, tag, or publication was performed.

## 2026-11-14 — final PR audit

Reviewed the workflow-dispatch emulator gate, service-readiness helper, local
API 37 runner, changed schema/backup paths, CI SDK configuration and modified
0.14 documentation. Kept the emulator workflow workflow-dispatch-only and did
not dispatch it.

- [x] Require the shared two-probe readiness gate before the local API 37
  runner installs or launches the app; reject a missing mount service using an
  exact positive match rather than the ambiguous substring `found`.
- [x] Preserve evidence by refusing an existing results directory, use a
  unique default evidence path, validate even emulator console ports, and
  terminate only the emulator PID started by this runner if console shutdown
  does not stop it.
- [x] Retry instrumentation only when there is no result evidence and a
  positive infrastructure marker exists: the emulator action failed before
  the script-start marker, or the shared readiness gate timed out before its
  ready marker. Missing XML alone is not enough. Malformed/invalid-count XML,
  failures, errors, skipped-test evidence and failures after readiness are not
  retried; the final result gate still fails on malformed results, command
  failures, failures, errors or zero tests.
- [x] Correct the API 37 SDK/platform and deterministic Kotlin domain wording;
  append current final verification without changing historical APK hashes or
  prior dated verification entries.
- [ ] Provide a downloadable PR APK artifact. Existing Actions workflows do
  not access the trusted signing identity, and the CLI has no supported APK
  attachment route; preserve the APK outside Git and report this limitation.

The Room 11→12 migration is additive and provides storage for Activity weeks,
goals, manual entries, cosmetic ownership, reward grants and loadout. Backup
format 7 carries those records while retaining readers for formats 1–6. The CI
API 37 platform installation matches the configured compile/target SDK; the
separate historical acceptance emulator remains API 36. Raising minSdk to 30
still means Android 8–10 cannot install 0.14; this compatibility limit is
documented and was not changed in this audit.

Focused verification after this audit: shell syntax, YAML parsing and embedded
workflow shell syntax passed; synthetic retry accounting covered absent/zero,
passing, failure, error, malformed and invalid-count XML; agent policy,
migration DDL and backup-fixture checks passed. No Gradle build, app test,
emulator run, visual check or APK rebuild was performed because this audit made
no application-source change. The previously recorded 410 app JVM tests and
15/15 API 37 repository tests remain the latest executed application tests.

created by stokonn with love <3
