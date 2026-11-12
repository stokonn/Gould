# Gould 0.14 development architecture

**Status:** implementation branch; unreleased. This document describes the current branch and its remaining verification limits.

## Boundaries

The Android application remains in `:app` and owns Compose screens, Android ViewModels, Room, widgets, audio, microphone services, notifications, WorkManager and file access. `:activity-domain` is a Kotlin Multiplatform module with deterministic week, goal, classification, streak, Freeze and aggregate rules in `commonMain`; it currently declares and validates a JVM target. This boundary is shared-ready, not an iOS application or a claim that Android persistence is cross-platform.

PracticeCoordinator and PracticeEngine remain authoritative for practice timing and session completion. Activity reads valid existing sessions through their recorded practice day/time zone and adds separately typed manual entries. It does not create or rewrite PracticeSession rows. New activity dates use local Monday-first calendar weeks; observed week records retain their zone. Rules accept dates/zone metadata explicitly rather than reading the global clock.

## Progression and durability

Historical goal rows take effect on a Monday. Weeks are projected chronologically; daily inactivity receives the Day Off allowance in ascending date order, then Miss. Weekly streak success depends on actual qualifying session count, separately from daily color. Beta Week suppresses first-partial-week streak processing. Freeze and longest/current streak state are recalculated from source activity, avoiding duplicate consumption on repeat evaluation or historical correction.

Optional minutes reward grants use a unique week key. Ownership and grant records are durable and are not revoked after correction. The catalog centralizes item IDs, categories, rarity weights and unlock descriptions; current weights are Common 60, Rare 25, Epic 12 and Legendary 3. If no unowned item remains, a collection-complete grant record is stored without duplicate ownership.

## Persistence and backups

The existing Room `opus.db` remains authoritative at schema 12. Migration 11→12 adds Activity settings, weekly goals, week metadata, manual entries, cosmetic ownership, reward grants and loadout records; the prior migration chain is preserved. Backup format 7/schema 12 includes the new tables. Older readers remain. Format 7 restore validates in an isolated Room database and merges the new Activity records without replacing the legacy personal journal.

## Deferred verification and constraints

The API 37 SDK packages and host integration build are configured. API 37's large-screen orientation/resizability changes were checked against the manifest: the app does not force orientation or a fixed aspect ratio, so those platform rules do not require a manifest exception; responsive Compose screens still need emulator validation. The RemoteViews memory changes matter to the existing widgets; their generated chart is bounded to a small bitmap, but API 37 widget rendering has not been executed. Android 17 background-audio behavior needs runtime validation for the app's lifecycle-bound local playback and foreground microphone service. The repository already stops local playback when Practice leaves foreground; no 0.14 audio rewrite is planned without a reproduced regression.

The API 37 emulator's userdata requirement exceeds available `/tmp` capacity. A proposed relocation was rejected by the safety review. Do not work around that rejection. A populated schema 11 upgrade, backup restore flows, UI acceptance and new repository tests therefore remain unexecuted; instrumentation compiles but has not run. Release signing identity, actual APK in-place upgrade and GitHub PR/release are also not verified/completed. The locally stored GitHub CLI credential is invalid.

The selected minSdk 30 means devices on Android 8–10 (API 26–29), supported by Gould 0.13, cannot install a 0.14 update. Their existing app/database is not reset, but they will not receive 0.14. This is the direct compatibility consequence of the agreed minimum SDK direction; release notes must state it clearly.

Discover source/routes and administration/moderation routing/security remain present while their navigation entries are hidden. Historical reports describe prior Discover repair work but do not establish a newly reproduced defect in this branch. No specific moderation runtime error is claimed. Reactivation should first validate the exact reported issue, provider-consent/cache behavior, access control and focused navigation tests.

Rust was not added. The current deterministic workload has no measured performance constraint that would justify native interop, build, ABI and maintenance overhead.
