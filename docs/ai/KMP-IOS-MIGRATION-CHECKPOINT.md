# Gould KMP / iOS migration — checkpoint (November 22, 2026)

> **Status: implementation in progress, all changes UNCOMMITTED.** Branch
> `feature/gould-kmp-ios-migration` at `1c0ca26`, worktree
> `.worktrees/gould-kmp-ios-migration`. Android remains the primary platform and
> the only platform with owner acceptance. This stage adds the first functional
> iOS application sharing the weekly Activity rules through the existing
> `:activity-domain` Kotlin Multiplatform module. Nothing is merged, tagged,
> released or published; `main` was not touched.

This checkpoint is the authoritative local record for this branch until a
version/milestone is assigned by the owner. Verification audit lines live in
`docs/planning/gould-0.15/verification-audit.jsonl` (script default); iOS klib
compile evidence is recorded here because the tier scripts do not cover iOS
targets. No historical audit entry was modified — this session only appended.

## Scope actually implemented

### Toolchain configuration (verified against real plugin metadata)

- Kotlin **2.4.21** for `org.jetbrains.kotlin.android`,
  `org.jetbrains.kotlin.multiplatform` and `org.jetbrains.kotlin.plugin.compose`
  (root `build.gradle.kts`). Consistent Kotlin version across `:app` and
  `:activity-domain` avoids cross-version metadata mismatch.
- **KSP pinned to 2.3.12** with an explanatory comment: KSP decoupled from
  Kotlin versioning at 2.3.0 and there is no KSP `2.4.0`; the Kotlin 2.4.21
  quickstart pairs KSP 2.3.x.
- AGP **9.1.1** unchanged (exists, compatible). Gradle **9.3.1** unchanged.
- `gradle.properties`: `android.builtInKotlin=false` and
  `android.newDsl=false` — documented AGP 9 opt-outs that pin the explicitly
  declared KGP instead of AGP's embedded 2.2.10. Deprecation warnings are
  accepted, not errors.
- `app/build.gradle.kts`: explicit `org.jetbrains.kotlin.android` plugin added.
- `activity-domain/build.gradle.kts`: `iosArm64()` + `iosSimulatorArm64()`
  targets, static framework `baseName = "ActivityDomain"`.

### Shared code

- `ActivityDomain.kt` (commonMain): new `ActivityOverview` data class and
  `activityOverview(today, zoneId, goals, workouts, beta)` — one canonical
  builder for the current-week view (goal selection, totals, day
  classification, goal-met flag), reusing the existing `weekFor` /
  `activeGoal` / `aggregate` / `classifyWeek` / `goalMet` rules.
- `ActivityRulesTest.kt` (commonTest): new shared-overview regression test.
- `ActivityRepository.kt` (Android): delegates to `activityOverview` instead of
  re-implementing the same sequence; surrounding snapshot/streak/history logic
  unchanged.

### Kotlin 2.4 intersection-reification fixes (promoted errors)

Explicit type arguments added; no behaviour change:

- `PersonalCatalogueBridge.kt`: `arrayOf<Any>(…)` (2 sites).
- `CatalogueSearch.kt`: `listOf<Any>(…)`.
- `Stage3bBackupTest.kt`: `arrayOf<Any>(…)` (found by the INTEGRATION tier when
  androidTest compilation ran for the first time under Kotlin 2.4.21).

### iOS application (`iosApp/`, new, untracked)

- `iosApp.xcodeproj/project.pbxproj` — Xcode 16 style (`objectVersion 77`,
  `PBXFileSystemSynchronizedRootGroup`), bundle ID `com.opus.piano`, product
  name `Gould`, marketing version 0.15.0, iOS 17.0 deployment target,
  Debug/Release configurations. A "Compile Kotlin Framework" run-script phase
  (alwaysOutOfDate, `ENABLE_USER_SCRIPT_SANDBOXING = NO`) runs
  `./gradlew :activity-domain:embedAndSignAppleFrameworkForXcode` from
  `$SRCROOT/..`; `FRAMEWORK_SEARCH_PATHS` contains both
  `$(BUILT_PRODUCTS_DIR)` (where the embed task copies the framework) and the
  legacy `…/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)` path;
  `OTHER_LDFLAGS` carries `-framework ActivityDomain`.
- Swift sources (all reviewed against the Kotlin API surface):
  - `GouldApp.swift` — `@main`, `ActivityStore` environment object.
  - `RootView.swift` — TabView: Activity + Settings.
  - `ActivityStore.swift` — `@MainActor ObservableObject`; JSON persistence
    (`gould-activity.json` in Application Support, atomic writes); epoch-day
    conversion helpers; validation before constructing Kotlin objects so a
    hand-edited file can never trip a Kotlin `require`; bridges to
    `ActivityDomainKt.activityOverview / currentActivityStreak /
    manualActivityDates / nextGoalMonday`.
  - `ActivityView.swift` — week grid (Mon–Sun, `DayKind` colours), goal card,
    stats, daily streak, week entries with manual-entry delete, goal/entry/day
    sheets. Every weekly-rule decision is delegated to the framework.
  - `ActivityEditors.swift` — goal editor (saving applies next Monday via the
    shared rule), manual-entry editor restricted to today and the previous two
    days (shared rule), day detail sheet.
  - `SettingsView.swift` — stored counts, destructive reset with confirmation
    dialog, about section with `created by stokonn with love <3`.
  - `Localization.swift` + `en.lproj` / `cs.lproj` `Localizable.strings`
    (50 keys each, key sets identical; no user-entered text passes through
    `tr()`).
- iOS data is stored locally on iOS only; it is not shared with Android and no
  Android data is read or written.

## Verification actually executed (Linux host, one worker, host Gradle lock)

| Check | Result |
| --- | --- |
| `scripts/verify-fast.sh` (compile + full app JVM suite + 6 static checks) | **PASS, 12 s** @ `1c0ca26-dirty` |
| `:activity-domain:jvmTest` | **14/14**, 0 failures/errors/skips |
| `:app:testDebugUnitTest` (from FAST) | **338/338**, 0 failures/errors/skips |
| `scripts/verify-integration.sh` (full JVM + `lintDebug` + `assembleDebugAndroidTest`) | first run **FAIL** (Kotlin 2.4 error in `Stage3bBackupTest`), fixed with `arrayOf<Any>`, re-run **PASS, 116 s** @ `1c0ca26-dirty` |
| `:activity-domain:compileKotlinIosArm64` + `:activity-domain:compileKotlinIosSimulatorArm64` | **BUILD SUCCESSFUL** (Kotlin/Native 2.4.21, `~/.konan`) — klib cross-compilation on Linux |
| `:activity-domain:compileKotlinIosArm64` + `:activity-domain:compileKotlinIosSimulatorArm64` **re-run after the Issue #55 fix** | **BUILD SUCCESSFUL, 6 s** @ `1c0ca26-dirty` (commonMain changed) |
| `scripts/build-apk.sh` (debug) | **BUILD SUCCESSFUL, 55 s** |
| APK v2 signature vs trusted certificate | **PASS** — `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e` |
| APK package id | `com.opus.piano` |
| iOS string-key parity (EN/CZ, used vs defined) | 50/50, no missing/unused keys |
| Swift static sanity (brace/paren balance, bridge-call names vs Kotlin signatures, framework names vs pbxproj) | consistent |

Audit lines appended (historical entries untouched, file still valid JSONL,
96 lines):

- `2026-11-21T01:15:06Z` INTEGRATION PASS 123 s @ `1c0ca26`
- `2026-11-21T02:48:04Z` FAST PASS 12 s @ `1c0ca26-dirty`
- `2026-11-21T02:50:42Z` INTEGRATION FAIL 102 s @ `1c0ca26-dirty` (honest failure record)
- `2026-11-21T02:53:25Z` INTEGRATION PASS 116 s @ `1c0ca26-dirty`
- `2026-11-23T12:13:22Z` FAST PASS 12 s @ `1c0ca26-dirty` (after Issue #55 tests + fix)
- `2026-11-23T12:16:24Z` INTEGRATION PASS 103 s @ `1c0ca26-dirty` (after Issue #55 fix)

## Issue #55 — "weekly streak bug / the week doesn't refresh on next Monday"

Reported 2026-11-22 with a Sunday 2026-11-22 23:17 Europe/Prague screenshot
showing week 16–22 fully completed (8/5 sessions, 166/90 minutes, goal met).

### Root cause

The *rules* were never wrong. `mondayOf`/`dayOfWeek`, `activityOverview`,
`classifyWeek`, `evaluateWeeks`, `weekSuccess`, `ensureWeekRows` and
`widgetSnapshotStale` all place the boundary correctly — now locked by the
regression tests below. The defect was in the **refresh trigger layer**:

`ActivityViewModel.snapshot` only advances when `refresh()` *succeeds*, and
`refresh()` was reachable from exactly three places — ViewModel creation, Room
invalidation of `sessions`/`manual_activity_entries`, and one in-process
`delay(30_000)` watcher inside `viewModelScope`. Nothing re-checked the
rendered week against the calendar:

- no lifecycle/foreground-resume trigger anywhere (`MainActivity.onResume`
  only requests a *widget* refresh),
- `ActivityScreen` recomputed `today` every 30 s but never compared it to
  `snapshot.week.monday`,
- `refresh()` cancels its predecessor, and its `catch (Exception)` path keeps
  the previous week's snapshot on screen with only a small error line.

So the rollover depended entirely on a background timer surviving process
caching/Doze and on a refresh that was not cancelled or swallowed. When that
did not happen on Monday, Sunday's completed week (8/5, all green, "goal met",
"For week starting Nov 9") stayed on screen — exactly the reported symptom.

**Honest limit:** the timer-failure path itself was not observed at runtime
(policy forbids emulator/device testing on this host). What *is* verified is
that every pure week/streak/staleness rule passes, and that the screen now
re-reads the week on composition, on calendar-day change and on foreground
resume instead of relying on that single background watcher.

### Fix (minimal, no schema / migration / backup / Room changes)

- `activity-domain` … `ActivityDomain.kt` — new pure
  `weekRolloverRequired(snapshotMonday, today)`.
- `ui/ActivityViewModel.kt` — new `revalidate(today)`; refreshes only when the
  rendered week is no longer today's week (no refresh storm on resume).
- `ui/ActivityScreen.kt` — `LaunchedEffect(today) { model.revalidate(today) }`
  re-checks on composition and on every calendar-day change.
- `ui/OpusApp.kt` — `ON_RESUME` lifecycle observer calls `revalidate()` so the
  guard also covers Home/Profile, which read the same snapshot.

### Regression tests added (all executed, all passing)

- `ActivityRulesTest.weekAdvancesAtMondayBoundaryAndResetsCurrentTotals`
- `ActivityRulesTest.renderedWeekRequiresRereadExactlyAtMondayBoundary`
- `ActivityRulesTest.closedWeekBecomesEvaluableOnlyAfterItsSunday`
- `ActivityRulesTest.weeklyStreakIsContinuousAcrossTheMondayBoundary`
  (Sunday's `N + goalMet` equals Monday's closed-week `N` — no drop, no
  double count)
- `WidgetProjectionTest.weeklySnapshotIsStaleTheMomentEuropePragueCrossesIntoMonday`
- `WidgetProjectionTest.practiceJustAfterMidnightBelongsToTheNewEuropePragueWeek`
  (00:30 Europe/Prague is still Sunday 23:30 UTC — the UTC regression case)

Totals after the fix: `:activity-domain:jvmTest` **18/18**,
`:app:testDebugUnitTest` **340/340**, 0 failures/errors/skips.

**Baseline comparison:** the Activity feature (0.14) does not exist on `main`
(`17d483f`, Gould 0.13), so this symptom cannot occur there; it is pre-existing
on this branch's baseline `1c0ca26` and was **not** introduced by the KMP/iOS
changes (`ActivityRepository`'s migration diff only extracts
`activityOverview()` and preserves behaviour).

**Not changed:** the static `activity_goal_minimum` copy ("At least one
session; changes begin next Monday.") does not reflect the configured goal —
a separate cosmetic issue visible in the screenshot, deliberately left alone.

## Debug APK evidence

Built after the Issue #55 fix with `:app:assembleDebug` (**BUILD SUCCESSFUL,
18 s**, incremental, `1c0ca26-dirty`):

- Path: `app/build/outputs/apk/debug/app-debug.apk`
- Size: **166,362,572 bytes**
- SHA-256: `af04e727f386ca4162471601b1ade052d77c4b6fa846fd5d8d51304a34911f37`
- Package: `com.opus.piano`, `versionCode=17`, `versionName=0.15.0-dev`,
  `minSdk=30`, `targetSdk=37` (debug — internal testing and owner review only,
  per permanent release policy)
- Signing: APK v2, certificate SHA-256
  `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e` (trusted,
  unchanged)
- No design-reference/mockup files packaged; `AndroidManifest.xml`,
  `classes.dex`, `resources.arsc` present.

Earlier artifact `Gould-v0.15.0-dev.apk` (162,691,451 bytes,
`e51d9ad9cbca31577df3a418b54960146b2509f68c8388165c335c48c3821ce6`) predates
the Issue #55 fix and is retained unmodified as historical evidence — it is
**not** the current build.

## NOT verified — honest limits

- **Swift compilation and Xcode build were NOT executed.** This host has no
  Swift toolchain, no Xcode and no macOS. Kotlin→Swift interop details
  (lowercase enum statics, `NSNumber` bridging for nullable `Int`,
  `ActivityDomainKt` top-level accessors) were checked against the Kotlin
  signatures and official interop documentation, but no compiler has seen the
  Swift sources yet.
- **Framework linking (`linkDebugFrameworkIosSimulatorArm64`) and
  `embedAndSignAppleFrameworkForXcode` were NOT executed** — both require
  macOS/Xcode. This is an environment limit, not a configuration claim.
- The iOS application has never been launched anywhere.
- No emulator or physical-device testing was performed (policy). Galaxy A55
  acceptance is owner-run only and has not been requested for this debug build.
- RELEASE tier was not run (no release/publication is authorized by this
  stage).

## Remaining work

1. **macOS/Xcode verification (primary blocker):** open `iosApp/iosApp.xcodeproj`
   on a Mac with Xcode 16+, build the `Gould` scheme for a simulator, and fix
   any Swift compile/interop errors the compiler reports. Expected first-run
   commands:
   `xcodebuild -project iosApp/iosApp.xcodeproj -scheme Gould -destination 'generic/platform=iOS Simulator' build`.
2. **Owner review of this diff** → decision on committing this branch
   (nothing has been committed; no author/co-author trailers for agents will
   be added).
3. Optional: owner-run Android acceptance of the debug APK on the Galaxy A55.
4. Assign a version/milestone and add the required stage entry to that
   version's `STAGE-CHECKLIST.md` once scope is approved (no future-version
   scope is invented here; 0.15 is already published and its checklist is
   untouched).
5. Any release/publication decision remains explicitly owner-gated and is not
   part of this stage.

## Continuation update (November 23, 2026)

Phase B/C/D continuation implemented on top of the existing KMP/iOS work:

- Unified activity history support: `unifiedActivityHistory`, `activityWorkouts`, manual edit window helper, tests.
- Manual activity editing UI added (edit button + dialog) and `ActivityViewModel.updateManual`.
- Activity board redesigned to use shared `activityBoard` (GitHub-style week columns, month labels aligned, day numbers in cells).
- Weekly-first statistics surfaced on Home and Profile via shared `weeklyStatistics`.
- Common tests added for board boundaries/leap years/December-January transition/day labels and weekly statistics.
- App tests added for unified history, manual edit window, deduplication and workouts.

Verification:
- `scripts/verify-fast.sh`: PASS
- `scripts/verify-integration.sh`: PASS
- `:activity-domain:compileKotlinIosArm64` / `:activity-domain:compileKotlinIosSimulatorArm64`: BUILD SUCCESSFUL
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`, 166406816 bytes, SHA-256 `d14e969aed50133f996044022b091f55384ea0d8927c605e6bfcaf4b677109b4`.
