# Gould 0.14.0 — prerelease notes

**Publication status:** prepared for GitHub prerelease publication; not downloadable yet. Gould 0.13 remains the latest available release until the 0.14.0 prerelease is published.

Gould 0.14 focuses on a clearer view of practice activity and a more consistent weekly progression system, while keeping the personal practice journal on the device.

## What's new

### Activity and weekly progress

- Activity replaces Discover in the visible bottom navigation.
- A Monday-first annual heatmap shows Training, Day Off and Miss days, with day details and a weekly overview.
- Weekly statistics include session and optional minutes progress, with detail views.
- Manual Activity entries can be added retrospectively for today and the previous two local calendar dates. They contribute to activity totals without being represented as microphone-detected practice sessions.
- Weekly session goals take effect from the following Monday when changed.

### Weekly rewards, Streak and Freeze

- Each eligible perfect week grants its own cosmetic reward, which can be claimed separately in Activity.
- Beta Week can earn its own weekly cosmetic when its configured requirements are met, but does not advance official Streak or Freeze progress.
- Four consecutive perfect official Streak weeks grant one additional Streak Freeze. This milestone is separate from, and does not replace, weekly cosmetic rewards.
- Claimed cosmetics are saved to the Profile collection, where they can be selected and applied.

### Home and Profile

- Home, Activity and Profile use the same combined activity sources for totals and daily streaks.
- Profile includes a shortcut to the cosmetic collection.
- Activity and weekly-goal layouts have been refined to make progress and pending reward actions easier to find.

## Compatibility

- **Minimum Android version: Android 11 (API 30).**
- Gould 0.13 supports Android 8.0 (API 26) and newer. Devices running Android 8–10 cannot install Gould 0.14; they can remain on Gould 0.13.
- Application ID remains `com.opus.piano`.
- Room database schema: 12.
- Backup format: 7, with readers retained for earlier supported formats.
- The update preserves the existing local-first practice journal model. As with any major update, export a backup before installing.

## Verification notes

The final PR #51 verification recorded 410/410 passing app JVM tests, 15/15 passing API 37 ActivityRepositoryTest cases, and a passing release verification gate. Android instrumentation APK compilation passed; this must not be confused with execution of every instrumentation test. The owner separately reported successful signed in-place update and visual review on a Samsung Galaxy A55. Those device checks are owner-reported and are not automated test results.

## Prerelease notice

Gould 0.14.0 is intended to be distributed as a GitHub **pre-release**, not as a Stable release. This page describes the prepared version; download and installation links will be added when the GitHub prerelease is published.

created by stokonn with love <3
