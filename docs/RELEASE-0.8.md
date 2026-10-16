# Goldberg 0.8.0 — Smart Practice

**Goldberg 0.8.0 Beta is a GitHub Pre-release for early testing.** It introduces a more flexible practice journal, optional microphone activity detection and a quieter editorial interface.

**Before you update:** export a backup and keep an independent copy. Physical-device testing, screen-off microphone behavior, Android instrumentation execution, real installation upgrades, accessibility and battery behavior have **not yet been verified**. This beta is being released with those limitations explicitly accepted; it is not a stable release.

## What's New

- Experimental **Smart Practice** with local microphone-based piano activity detection.
- A microphone foreground service designed for **screen-off and background practice**, with notification controls.
- **Smart Pause**, configurable sensitivity and an inactivity delay from 2–30 seconds.
- **Practice Review**, richer notes, optional daily reminders and separate CSV reports.
- Integrated screen headers, a skippable introduction and **Bach’s Secret**.

## Smart Practice

Choose the piece yourself, then explicitly enable Smart Practice. Goldberg estimates piano-like activity on your device; it does not identify compositions. Smart Pause stops adding credited time during longer rests and resumes when activity is detected. The default inactivity delay is approximately five seconds, with sensitivity and delay controls available.

Smart Practice requires **microphone permission** and uses an Android **microphone foreground service** while active. That service is designed to continue listening when the screen is locked or you temporarily leave Goldberg. Its notification offers Pause and Stop where Android allows notifications. Automatically paused sessions keep listening so playing can be detected again; manual Pause releases capture.

The existing manual timer remains available. After process death or reboot, unfinished practice recovers paused and microphone access requires a new explicit action.

**Detection is experimental.** Speech, television, recorded music and other tonal sounds may count as playing; quiet passages, sustained notes, distance and microphone processing may cause misses. Headphone-only digital piano playing may be inaudible to the phone. Screen-off detection and interruption handling are not yet verified on physical hardware, and uninterrupted operation on every Android device is not promised.

## Practice Experience

**Review your session before saving.** Compare elapsed session duration, credited practice time and detected active time where available. See how credited time was attributed to individual pieces, along with recorded pauses and interruptions. Historical entries retain their original totals; measurements that were never recorded are shown as unavailable.

**Keep the right notes in the right place.** Add per-piece session notes, whole-session notes and an optional reflection. Edit notes on saved sessions without changing their timing. Repertoire notes remain separate. Notes-only entries do not count toward practice milestones, and unfinished drafts are checkpointed locally. Abrupt termination can lose the newest uncommitted interval.

**Build a routine.** Enable an optional daily reminder around your chosen local time. Android notification settings and power restrictions can delay or block delivery; no exact-alarm permission is required. Tapping a reminder opens Practice without starting the microphone.

**Explore your history in a spreadsheet.** Export session history and individual piece times as separate UTF-8 CSV files through Android’s document picker. Fields support commas, quotes and multiline notes, with spreadsheet-safe text handling. Separate reports avoid repeating parent-session totals on every piece row. CSV is a report, not a restorable backup.

## Design & Navigation

Goldberg’s Classical Editorial design now places actions within the content: Settings sits beside the Profile avatar, and Search is integrated into the Repertoire header. Redundant standalone page chrome has been removed while bottom navigation, Android’s status bar and contextual Back controls remain.

A lightweight, skippable introduction explains repertoire, practice, statistics and microphone privacy. Existing installations are excluded from first-run onboarding; the introduction can be revisited from Settings.

For a small musical surprise, tap the version in **Settings → About seven times**. **Bach’s Secret** reveals a synthesized harpsichord and the permanent **“The Curious Pianist”** badge, with its original unlock date retained.

## Privacy & Reliability

Microphone processing stays **entirely on your device**. Goldberg does not store raw audio, upload audio, transcribe speech or automatically identify the piece. While listening, the microphone hears ambient sounds, potentially including voices, even when the screen is locked. Pause or Stop releases capture.

If notification permission or the Smart Practice channel is disabled, notification-drawer visibility and controls may be unavailable. Use Goldberg’s controls or Android’s Active apps controls as available on your device.

Backups and CSV exports are unencrypted and use the document provider you choose; a cloud-backed provider may synchronize them. Catalogue discovery continues to use Open Opus and Wikimedia services. Privacy information and license notices are accessible in Settings.

## Fixes & Improvements

- One practice state machine handles UI, detector and notification commands, with protection against stale capture callbacks and duplicate completion.
- Explicit Room schema-6 migrations preserve historical records and add review/timing data without a destructive reset.
- New format-2 backups include portable session information; format-1 backups from Goldberg 0.6 and 0.7 remain importable.
- Durable drafts and device-local reminder activation remain outside portable backups. Restoring a backup never starts microphone capture.
- Notes-only entries are excluded from practice milestones, and credited time remains separate from elapsed time.
- Privacy, font and dependency-license notices are available in the app.

Automated verification completed successfully: **47 JVM tests**, host migration checks from schemas **1–5 to 6**, a legacy-backup fixture preservation check, application and Android test APK compilation, and lint with **zero errors**. Compilation of Android tests does not mean those tests were executed.

## Known Limitations

The following have **not yet been verified** for this beta:

- **Physical-device testing**, including real piano detection and microphone permissions/interruption handling.
- **Screen-off microphone behavior**, including locked-screen Smart Pause and automatic resume.
- **Android instrumentation execution**; the test APK compiled but was not run.
- **Real installation upgrades**, including on-device migration, restore and user-data preservation.
- **Accessibility**, including TalkBack, large fonts, screen sizes and rendered layouts.
- **Battery behavior**, including CPU, memory, thermal impact and vendor power-management restrictions during extended practice.

Host checks and signature matching do not establish these runtime results. Exceptionally large journals still load complete local history for summaries. Catalogue availability depends on external services, and reminders are approximate.

## Installation & Updating

1. Export a backup from your existing Goldberg installation and retain the original file.
2. Download **Goldberg-v0.8.0.apk** from this pre-release. Android **8.0 or later** is required.
3. Install it over your existing compatible installation. The APK retains application ID `com.opus.piano`, reports version `0.8.0` / code `6`, and its actual signing certificate matches the published 0.6.1 and 0.7 APKs. **Real installation upgrades remain untested.**
4. If Android reports a signature or installation error, stop. **Do not uninstall Goldberg or clear its data to bypass it.**

This APK uses the existing compatible Android Debug signing identity. New format-2 backups require Goldberg 0.8 or later; keep pre-upgrade backups for older-version compatibility. Restore replaces rather than merges data. Microphone use and reminders are optional.

**APK SHA-256**
```text
57e0cefa60730a1415fd351bf8a679e30baef563c62dfd1372e1f2b7228c5be1
```
