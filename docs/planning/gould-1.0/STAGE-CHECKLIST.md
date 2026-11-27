# Gould 1.0 — Confirmed UI/UX Fixes

## Stage purpose

Apply the owner-confirmed Settings, Studio, Library/Catalog and Home layout fixes
in English and Czech while preserving local practice, repertoire and backup data.

## Scope and tasks

- [x] Settings: rename Theme; remove theme/font previews; shorten Data & Storage copy; improve Google-account spacing.
- [x] Retire the Community drafts editor and page; add a separate safe archive export path without deleting local files.
- [x] Studio: center title, add confirmed discard action, show and rename session, and move existing tools to square shortcuts.
- [x] Library/Catalog: remove manual repertoire creation and Lists tab; load 25 local composers on All; fix candidate period filtering and inspect duplicates.
- [x] Home: compact weekly overview and remove Recent Practices.
- [x] Localize new/changed user-facing text in English and Czech; remove retired page strings.
- [x] Keep an unnamed completed session's title empty so history uses the existing locale-specific Free Practice label.
- [x] Run full JVM suite, lint, debug APK build, instrumentation compilation, and relevant static checks.
- [ ] Review Studio and Home at multiple runtime widths; screenshots/device acceptance remain pending.

## Follow-up — Home and Library layout review

- [x] Keep weekday labels on one line in compact layouts; reduce day-column gaps.
- [x] Move Library Sort to the heading's top-right and show it only for My Repertoire.
- [x] Remove the Library Previous action and its navigation callback.
- [x] Keep Catalog category buttons fixed above the vertically scrolling catalogue content; use equal-width buttons instead of a horizontal scroller.
- [x] Update the existing responsive Library header fixture to assert the Sort action.
- [ ] Runtime-check Home and Library at narrow and wide widths; no usable emulator/KVM was available in this environment.
- [x] Source-review current search flow and record next-task findings in the implementation report; search behavior was not rewritten.

The follow-up source compiles, the focused JVM lifecycle test and FAST static checks pass, and both debug APK and instrumentation APK compile. Instrumentation remains compile-only; no emulator runtime or screenshot evidence is available.

## Final build checkpoint — 2026-11-27

- [x] `./scripts/verify-release.sh` passed after the localized-title correction in 187 seconds. An earlier run hit transient Gradle test-result bookkeeping; the isolated `:app:testDebugUnitTest` rerun passed, followed by full RELEASE passes in 488 and 187 seconds.
- [x] `:app:assembleRelease` passed using the repository's documented signing init script and the existing Gould debug key. `apksigner verify` passed with the expected certificate SHA-256 `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`; ZIP integrity passed.
- [x] APK metadata verified: package `com.opus.piano`, versionCode `17`, versionName `0.15.0-dev`; size `97,999,180` bytes; SHA-256 `fc15a0861c6f14b35cdc6e84383ec05afa7e46c63b16499fda21eb9936bfa087`.
- [ ] Runtime visual and interaction acceptance remains pending. No usable emulator/KVM was available; no physical device was accessed. Instrumentation was compiled, not executed.

## Progress and verification

- Implementation and verification completed on `patchofbugsbfr1.0` from source based on `4ecbf5c`; no merge or release publication was performed.
- FAST verification passed on current dirty source at `4ecbf5c`: `compileDebugKotlin`, focused `PracticeQueueTest`, resource parity, Room schema/migration checks v1–v14, historical backup fixtures, history paging, UI literal audit, and curated-catalogue validation. It passed again after removing two unused strings. Audit: `verification-audit.jsonl` in this directory.
- The first two FAST attempts exposed and corrected a JVM-vs-Android test placement issue, an empty fixture assertion, and the UI literal whitelist count. Their failed audit lines are retained.
- An initial compile caught and fixed unavailable Compose `TextAutoSize` usage and restored the independent backup/restore preview collection in Settings.
- Added Android instrumentation coverage for renamed session recovery, completion, portable export and restore; it has not yet executed.
- Bundled catalogue inspection was read-only: `PRAGMA integrity_check=ok`; 100 active composer records have distinct case-insensitive titles; all 100 curated MusicBrainz IDs resolve. No duplicate source records were found, so no catalogue data was changed.
- Session title uses additive Room migration 13→14 and portable backup v9/journal schema 14. Old backup imports default to an empty session title. Historical v4 export remains compatible and does not include this newer field.
- Saved Community draft files are retained. The old editor is removed; archive export is available in Data & Storage. No export/import screen remains for editing or submitting those drafts.
- RELEASE host gate passed in 154 seconds: clean `assembleDebug`, complete `testDebugUnitTest`, `assembleDebugAndroidTest`, `lintDebug`, and all seven mandatory policy scripts. Instrumentation was compile-only, not executed. After the final deletion of two unused string resources, FAST passed and `assembleDebug` succeeded again. `check_admin_retirement.py` and `git diff --check` also passed.
- The debug artifact is `app/build/outputs/apk/debug/app-debug.apk` (SHA-256 recorded in the final report). Responsive runtime screenshots and owner-device acceptance were not performed by the agent; the stage remains open for that visual check.
