# Goldberg 0.9 verification

## Baseline and safety

Verified baseline: feature/goldberg-0.8/main and published v0.8.0 resolve to 5b22fba2fdfd2dd4f4dc3f5f68293f3e7d5e7654. Remote branches/tags/releases were inspected before creating feature/goldberg-0.9. The historical untracked RELEASE_NOTES.md remains untouched, SHA-256 db920f0b1ff88579b6bea9f9da567af16e6db0c7ce5b6439781ca1754fcd4b9b.

No existing user database was opened, reset or edited by development. Room schemas and entity storage contracts remain at version 6. Original font assets and OFL notices are reused. Signing identity is retained, not rotated.

## Executed checks

Final source-frozen command:

`ANDROID_HOME=/home/stokonn/Android/Sdk ANDROID_SDK_ROOT=/home/stokonn/Android/Sdk ./gradlew clean assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug --offline --no-daemon --max-workers=1`

PASS: 84 tasks executed; final run 2m 8s; application APK and instrumentation APK compiled; **52 JVM tests, zero failures/errors**; **zero lint errors, five warnings**. Build time is a host/toolchain observation, not startup or battery performance.

Lint warnings reviewed: existing target-SDK guidance and Bitmap KTX suggestion; new API-33-only localeConfig attribute (intentionally ignored on older versions), optional String.toUri KTX suggestion and an empty obsolete v26 resource directory left after moving the adaptive icon into the base resource. The empty directory contains no distributable resource and is not tracked in Git. No lint errors/checks were suppressed. An earlier Czech MissingQuantity error was fixed by adding the required many forms; the complete clean verification then passed.

PASS: `python3 -B scripts/check_localization.py` — **678 English/Czech resources**, key/placeholder parity, required Czech one/few/many/other forms and supported locale declarations. This is not a native-speaker or rendered-device review.

PASS: `python3 -B scripts/check_migrations.py` — every historical schema 1–5 → 6 path, retained seed fields/totals/settings/badge dates, columns, indexes and foreign keys.

PASS: `python3 -B scripts/check_backup_fixtures.py` — existing format-1/schema-4 fixture retains every original portable field after staging/migration. Existing Android format-2 round-trip/malformed restore tests compile; their execution remains pending.

PASS: git diff whitespace and shell syntax checks. Host color contrast: Nocturne body/background 13.26:1, body/surface 11.44:1, secondary/background 8.32:1, gold/background 7.34:1; Ivory body/background 13.14:1, body/surface 14.58:1, secondary/background 4.95:1, gold/background 5.30:1. Every curated light/dark accent also passed the tested 4.5:1 text/button pairs. These checks do not establish every rendered component’s contrast.

PASS: fontconfig checks confirm Czech diacritics in both bundled fonts. Original vector icon was rendered on the host for visual inspection; Android launcher masking/caching behavior was not tested on device.

New JVM coverage includes Home calendar metrics using credited time; notes-only exclusions; per-piece session grouping; distinct recent pieces; deterministic repertoire sorting; normalized two-metric customization and safe section order; badge/history route ownership. Existing timer/Smart Pause, attribution, achievements, showcase, date and CSV tests remain. New instrumentation covers visual preference persistence/reset isolation, real Czech plural resources, AppCompat override/reset; existing persistence/backup/service tests compile.

## Artifact verification

APK: /home/stokonn/Projects/Opus/app/build/outputs/apk/debug/Goldberg-v0.9.0.apk

Package com.opus.piano; version 0.9.0; code 7; min API 26; target API 36. Final metadata/signature are checked with aapt and apksigner. The named artifact is byte-for-byte the tested app-debug.apk.

SHA-256: d2666da877eb307c084d7454f3257a794e81181e0c4468265d9412d39d73a688

Size: 20135328 bytes.

Certificate SHA-256: 53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e — actual certificate matches the downloaded trusted published v0.8.0 APK. This is the existing Android Debug identity, not production signing. Certificate equality is not a real installation-upgrade test.

## Not executed / not verified on device

Elevated adb devices reports no connected device. No device was installed to, uninstalled from or reset. Instrumentation execution, real installation upgrades, runtime backup/restore, locked-screen microphone and interruption handling, full rendered English/Czech UI, TalkBack, narrow/large-font layouts, native launcher behavior, thermal/battery/CPU/memory and startup measurements remain unverified. No runtime success is inferred from compilation or host tests.

The user explicitly permits this beta to publish with missing device checks disclosed. Failed automated checks, missing functionality, data/backup failures, incompatible signing, invalid artifacts or unsafe Git integration remain publication blockers. Publication must be a GitHub Pre-release; existing tags/releases must not be overwritten.
