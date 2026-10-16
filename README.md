# Goldberg 0.7.0 — Smart Cache & Classical Editorial

A private classical piano practice journal for Android 8.0+ (API 26), built with Kotlin, Jetpack Compose, Material 3, Room and MVVM. No accounts, analytics or mandatory cloud services.

**Local release candidate; publication is blocked by a signing-key mismatch.** The available debug certificate differs from published 0.6.1. Do not uninstall or clear storage to install this candidate over an existing journal. See [verification](docs/VERIFICATION-0.7.md) for the exact blocker and test limitations. No 0.7 tag or GitHub release has been published by this update.

## Features

- **Offline catalogue:** previously saved composers and compositions remain in Room. Fresh response copies avoid repeat requests and repeated ingestion. Optional refresh failures retain saved data and identify cached results. Visible failed catalogue pages retry when validated connectivity returns while the app is started.
- **Smart Cache:** a shared background journal summary supplies total practice, per-piece totals, last-practice dates and session-part groups. Transactional Room snapshots invalidate it after repertoire/session edits and restores. Downloads are bounded to 16 MiB of response payloads and 32 MiB of portraits. Clear Cache in Settings preserves all personal tables and the normalized offline catalogue; usage counts disposable payload/image bytes.
- **Classical Editorial:** a personal Home overview, compact repertoire entries, a timer-first Practice layout, a curated Profile, contextual navigation headings and organized Settings. Warm ivory/light and warm-neutral/dark themes share restrained colors, separators and moderate radii.
- **Bundled typography:** Cormorant Garamond for editorial headings and Inter for ordinary text, controls and statistics. English/Czech glyphs are included. System Font applies the device font to every role. Complete SIL Open Font licenses ship in the APK.
- **Personal statistics:** choose the prominent metric and reorder supporting metrics using accessible buttons. Order persists in Room and backups. Week/month/year/all-time activity filters, current streak, most-practiced works and expandable history remain.
- **Badge widgets:** up to five selected earned badges, ordered with accessible arrows, display their icons, names and original unlock timestamps. All 16 badges remain in the complete collection. Existing selections and unlock dates are retained.
- **Multi-piece practice:** monotonic timer, pause/resume, active-piece attribution, per-piece notes, multi-piece summaries and interrupted-process checkpoint recovery remain. Removing a piece retains recorded time. Completing a session persists its header, parts and earned badges transactionally.
- **Backup & Restore:** complete versioned JSON snapshots, validated in an isolated database before atomic replacement. Schema-4 backups from 0.6.0/0.6.1 are accepted; new preference defaults are added during conversion. New schema-5 backups include typography and statistic order. Keep the original backup for older app versions.
- **Preserved tools:** search, filtering/sorting, repertoire editing, composer biographies and credits, composition detail, recording/score links, saved preferred interpretations, profile editing and Wrapped preview.

The installed label remains Goldberg. Ordinary screen headings use their context; the requested “Goldberg Typography” preference label and About identify the typography/product. Android automatic cloud backup remains disabled. Gallery uses the system picker; avatars are bounded local copies. Backups are plain, unencrypted JSON and are exported only through the chosen document provider.

## Compatibility

Application ID **`com.opus.piano`** and database **`opus.db`** are unchanged. Version name **0.7.0**, version code **5**. Schema **5** preserves the full **1 → 2 → 3 → 4 → 5** chain and adds only two preference columns to `profile_settings`; it does not alter history, repertoire, unlocks or source identifiers. There is no destructive migration fallback.

Signing configuration is unchanged, but the current host has a different debug key. Matching configuration does **not** prove matching certificates. Publication requires the original 0.6.1 signing key and a successful comparison of actual APK certificates.

## Build and verification

Use the pinned Gradle/AGP configuration, Java 17 target and Android SDK 36. Point both SDK environment variables at the same installed SDK, or configure `local.properties` appropriately.

```bash
./gradlew assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug
python3 scripts/check_migrations.py
# With a test device/emulator:
./gradlew connectedDebugAndroidTest
# Local debug APK only:
bash scripts/build-apk.sh
# Mandatory upgrade-signing gate:
bash scripts/verify-upgrade-signing.sh published-0.6.1.apk candidate-0.7.0.apk
```

Named candidate: `app/build/outputs/apk/debug/Goldberg-v0.7.0.apk`. Standard debug output: `app/build/outputs/apk/debug/app-debug.apk`. These are debug-signed builds, not production-signed distributions.

- [Verification and publication status](docs/VERIFICATION-0.7.md)
- [Performance diagnostics and reproducible procedure](docs/PERFORMANCE-0.7.md)
- [Draft English release notes](docs/RELEASE-0.7.md)
- [Complete modified-file list](docs/MODIFIED-FILES-0.7.md)
- [Backup format and conversion](docs/BACKUP.md)
- [Font provenance and licenses](docs/fonts/README.md)
- [Previous verification](docs/VERIFICATION-0.6.1.md) and [historical architecture](docs/OPUS-0.5.md)

No phone or emulator was available for this update. Compiling instrumentation does not establish on-device upgrade, visual, TalkBack, large-font, picker, offline-network transition or timer lifecycle correctness. No measured startup or frame-rate improvement is claimed.
