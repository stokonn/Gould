# Goldberg 0.7.0 verification and publication gate

Date: 2026-10-16. Status: **local implementation; publication blocked by signing incompatibility**.

## Repository verification

The initial working tree was clean. No AGENTS.md files were found under the project hierarchy. README, Gradle configuration, navigation, Room models/migrations, repositories, screens and existing test sources were inspected before implementation. Remote references were fetched successfully.

`main`, `origin/main`, `v0.6.1` and the existing clean `feature/goldberg-0.7` branch all pointed to `34821ea071734b1f1571b116b2e656e64c1a8587`. The branch already starts at the verified latest main and includes all 0.6.1 changes. The published 0.6.1 release was confirmed to be a Pre-release targeting that commit. Its downloaded APK checksum matches GitHub's asset digest:

`adef8176e32fe4e8eec8a9d0d41b5d3d43585e3fb70139f42b4a97f7e3b7334d`

A frozen copy of that tag was independently built under `/tmp/goldberg061-source` to avoid mixing baseline verification with candidate edits.

## Implemented scope

- Room-backed offline composer/composition records and response-cache reuse; fresh responses skip repeated metadata ingestion.
- A latest-only, shared journal summary with total practice, attributed sessions, part groups, per-piece totals and last-practice dates. Transactional Room snapshots invalidate it after relevant changes and restoration; derivation runs off the main thread.
- Bounded disk portraits (32 MiB) and response payloads (16 MiB), recency/age eviction, cache usage and Clear Cache. Normalized catalogue and all personal tables are retained.
- Serialized downloads/restore, preserved offline fallbacks and lifecycle-bound connectivity retry for visible failed catalogue pages.
- Local background preloading through shared ViewModel flows, deferred badge verification, static delayed skeletons, bounded image decoding, stable list keys and privacy-conscious trace diagnostics.
- Bundled Cormorant Garamond/Inter, complete font licenses inside the APK, Czech glyph coverage, persistent System Font preference and scalable typography roles.
- Classical Editorial shared typography/shapes/separators, Home overview, repertoire entries, timer-first Practice, Profile composition, contextual navigation and organized Settings.
- Curated numbered badge widgets, original unlock dates, five-item selection/order persistence and the complete badge collection.
- Primary-statistic selection and supporting order with accessible controls, persistent stable IDs and safe UI defaults.
- Minimal ordinary-screen branding, including removal of the badge-artwork inscription. The explicitly requested “Goldberg Typography” setting name remains; product/version identification lives in About.
- Schema 4 → 5 migration and schema-4 backup conversion, preserving IDs, histories, theme/profile data, notes, badge dates and selections.

## Build environment and results

The initial SDK variables pointed at incomplete `/opt/android-sdk`. The complete installed SDK is `/home/stokonn/Android/Sdk`; setting both `ANDROID_HOME` and `ANDROID_SDK_ROOT` to it resolves the environment issue. Gradle also requires access to its external cache/lock files. No SDK licenses were accepted or new SDK components installed.

Frozen 0.6.1 baseline:

- Debug APK build, all JVM tests, Android test APK compilation and lint: **PASS**.
- JVM: **29 tests, zero failures/errors**.
- Lint: **zero errors, 12 warnings**.

0.7 candidate:

- JVM tests: **31 tests, zero failures/errors** in the completed test run.
- Debug and instrumentation APK compilation succeeded in that run.
- Final **clean frozen-source** `assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug --no-daemon --max-workers=2`: **PASS**, 84 tasks executed, 5m 56s host build/check duration. No app runtime speed claim is inferred.
- Final JVM: **31 tests, zero failures/errors**. Instrumentation APK compiled successfully.
- Final lint: **zero errors, 11 warnings**, consisting of SDK/dependency version notices and an optional Bitmap KTX suggestion.
- All 68 frozen application/test/build input hashes remained unchanged through final verification. An earlier non-final lint run was stopped after stalling in Kotlin source traversal; the clean, frozen-source run completed successfully.
- `python3 scripts/check_migrations.py`: **PASS** for **1 → 5, 2 → 5, 3 → 5 and 4 → 5** against actual migration SQL and exported Room schemas.
- `git diff --check` and shell syntax checks: **PASS**. Source/new documentation whitespace checks pass; upstream SIL license text remains verbatim, including its original whitespace.
- Fontconfig glyph checks: **PASS** for English/Czech text including uppercase and lowercase Czech diacritics in both bundled fonts.
- Host contrast calculations: Light body 15.38:1, light primary 6.34:1, light secondary 6.86:1; dark body 14.93:1, dark primary 8.42:1, dark secondary 6.95:1. These cover core text pairs, not every rendered element.

## Data and functional verification limits

Host SQLite checks verify existing migration chains and the new additive preference columns, retained legacy repertoire/session rows, session-part totals, badge first timestamps, profile name/theme/showcase defaults and Room schema/index equality. JVM tests cover timer charging, multi-piece attribution, badge rules, statistics/date boundaries, showcase ordering/limits, stable statistic IDs, safe defaults and shared-summary correctness.

Compiled Android regression coverage includes existing migrations, disk reopening, multi-piece persistence, backup validation/replacement, profile compatibility and navigation; new coverage includes actual schema-4 creation/migration/reopen, typography/statistic preference persistence, safe Clear Cache, retained catalogue/user data, duplicate prevention and offline response fallback.

**Android instrumentation was not executed.** Elevated `adb devices` found no connected devices, and no emulator package is installed. No real APK upgrade, physical-device behavior, picker interaction, pause/resume lifecycle stress test, rendered light/dark review, large-font/narrow-screen test, TalkBack or keyboard navigation is claimed. Compilation and host migration checks do not replace Android runtime validation.

No startup or screen-load runtime measurements are available. See [performance procedure](PERFORMANCE-0.7.md); no percentage improvement is claimed. Complete session lists remain in memory for aggregation, so exceptionally large journals still need device profiling.

## Critical signing blocker

The published 0.6.1 APK verifies with certificate SHA-256:

`53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`

The available host debug key verifies with:

`c2ba61b6805b923ff89eebf182c9c84b1457c1f10a8ce4473796573622f43509`

These differ. Matching Gradle signing configuration does not preserve compatibility when the keystore itself changes. A candidate signed by the current key cannot update a published 0.6.1 installation in place. **Do not uninstall or clear storage to bypass this.** The original private signing key is required. Its location was requested from the user while independent implementation continued.

`scripts/verify-upgrade-signing.sh` compares the actual APK certificates and fails on mismatch. This is a mandatory publication gate.

## Git and publication status

All implementation remains recoverable on `feature/goldberg-0.7`. Since successful upgrade verification is a condition of the user's commit/merge/publication authorization, no commit, push, PR, merge, tag or release was performed while the signing gate fails. Main remains `34821ea071734b1f1571b116b2e656e64c1a8587`.

There is no 0.7 PR URL, merge commit or Release URL. [Release notes](RELEASE-0.7.md) are an unpublished English draft. After obtaining the original key: rebuild with it, pass actual certificate comparison, repeat checks for changed inputs, complete the authorized PR/merge workflow, rebuild/verify the merged commit, tag it, publish the Pre-release and verify downloaded asset checksums. Branch protections must remain respected.

## Local artifact

`/home/stokonn/Projects/Opus/app/build/outputs/apk/debug/Goldberg-v0.7.0.apk`

Signing: **Android Debug**, certificate `c2ba61b6805b923ff89eebf182c9c84b1457c1f10a8ce4473796573622f43509`. APK signature verification passes; **upgrade-signing comparison fails**. Package metadata verifies ID `com.opus.piano`, version `0.7.0` / code `5`, min SDK `26`, target SDK `36`, and installed label `Goldberg`. Both fonts and complete licenses are present in the APK; license bytes match the upstream copies. The named APK equals `app-debug.apk` byte for byte.

SHA-256: `726a4b7ce676c90f208749bf8c8b8c0e3323d6d0d411efeaf9cfedafd34557ad`

Size: 18503662 bytes. Adjacent checksum file: `Goldberg-v0.7.0.apk.sha256`.

This local artifact is not uploaded and must not be represented as an upgrade-compatible or production-signed release. An APK re-signed with the original key will have a different checksum and must be verified again.

## Signing recovery investigation

The project uses the default Android debug signing configuration: no `signingConfigs`, `storeFile` or password overrides are present in tracked build files. `local.properties`, `keystore.properties` and `signing.properties` are absent. No signing/keystore environment variables or historical project signing/keystore references were found. No passwords or private keys were printed or committed.

Read-only searches covered accessible `/home`, `/tmp` and `/opt` development locations, followed by cache/local-data/config/backup-named key searches. `~/.android/debug.keystore` is absent. The only real candidate found is `~/.config/.android/debug.keystore` (the host sets XDG_CONFIG_HOME). Its `.lock` companion is not a key. The key predates this work and was not deleted, replaced, regenerated or overwritten.

`/root` is inaccessible to the current user, and `sudo -n` requires a password. Root-owned private system-service temporary directories are also inaccessible. The investigation therefore cannot rule out a key in those locations or outside this environment. No permissions were bypassed.

The frozen **0.6.1 source rebuild** passes signature validation but has certificate `c2ba61b6805b923ff89eebf182c9c84b1457c1f10a8ce4473796573622f43509`, exactly matching the current 0.7 candidate and differing from the published 0.6.1 certificate. This establishes that switching source versions does not recover the original signature; the missing private key is the issue.

**Resolution status: unresolved; publication stopped.** No signing configuration was changed to imply compatibility.

Safe next steps:

1. Recover the original keystore from the machine/build environment that produced the published APK, a private backup, or an authorized administrator's inspection of inaccessible development locations. Verify its certificate before rebuilding either version. Store it outside Git and do not overwrite the existing local key.
2. If the key is permanently unavailable, keep the installed 0.6.1 application and its data intact. Export its format-1 backup through Settings and retain independent copies. Continue using that installation while a migration is designed.
3. A separately installed application with a different package ID could import that backup after thorough restore/data checks, keeping the original app installed. This requires an explicit future identity/migration decision and is **not** implemented here because this update must preserve `com.opus.piano`.

The public certificate in an APK cannot supply its missing private signing key. A same-package in-place update requires matching signing identity or a valid signing-key rotation lineage; a new unrelated debug key cannot provide either. Neither `adb install -r` nor a Git tag repairs this mismatch. No uninstall, application-data clearing or unsupported signature bypass is proposed.

Android signing reference: [Sign your app — Android Developers](https://developer.android.com/studio/publish/app-signing).
