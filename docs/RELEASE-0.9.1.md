# Goldberg 0.9.1 — The Quality Update

Goldberg 0.9.1 Beta focuses on dependable practice sessions, a compact and consistent journal, and clearer Czech and English presentation.

## Practice reliability and recovery

- Manual and Smart Practice publish a running session only after the initial checkpoint succeeds. A failed start remains stopped and can be retried; microphone capture begins only after that durable start.
- Recovery failures preserve the original unfinished draft. Retry is available. If recovery remains impossible, export the unchanged draft, then separately confirm before clearing it for a fresh session. Recovery never automatically restarts the microphone.
- Continuous note editing now has a maximum dirty age of approximately five seconds, alongside the existing timing checkpoints. Pause, piece changes, review and completion flush the relevant state.
- Practice and Review show pending unsaved changes and offer a save retry. Duplicate Start requests do not create additional session identities. Requests received while practice is unavailable receive a localized explanation.
- Backup replacement cannot checkpoint over an unreadable recovery draft. Room remains schema 6, with no new migration or package-identity change.

## Design and navigation

- More compact shared headings, consistent themed cards/dialogs and shared piece-row content across Home, Repertoire and Catalogue retain Cormorant Garamond, Inter, Ivory/Nocturne and user-selected accents.
- Home keeps Start Practice prominent, two configured metrics and up to two distinct recent pieces. Repertoire removes its duplicated heading and provides an actionable empty state.
- Practice keeps the current piece and timer near the top. Pieces and notes are collapsed initially, with multi-piece editing still available. Controls remain above navigation and account for the keyboard.
- Profile keeps identity, statistics, activity and up to five showcased badges. History and the complete badge collection have dedicated destinations.
- Full history loads incrementally from the database with stable timestamp/ID ordering. Multiple parts for the same piece do not duplicate sessions. Piece and catalogue detail pages link to full history, which supports visible and system Back navigation. Piece-filtered history shows that piece's credited duration while retaining complete session review.

## Language and visual fixes

- Corrected TOTAL PRACTICE, weekday labels and their accessibility descriptions, generated Free Practice labels, showcase badge names, interruption reasons and catalogue fallback messages.
- Added explicit locale/zone formatting checks and fixed a remaining journal count expression using plural resources. User titles, notes, composer names, original work titles and nicknames remain unchanged.
- Replaced legacy fixed-color overrides in study links and catalogue controls with the selected theme accent.
- New Classic Piano branding uses the supplied 1A Ivory / 1B Nocturne artwork, subtle glass reflection, separate transparent adaptive foregrounds, a monochrome themed-icon layer and updated Android splash artwork. Launchers control masking, caching and themed-icon treatment; the launcher appearance follows Android's resource configuration.

## Installation and update

1. Export a backup from the existing app and keep it somewhere accessible.
2. Download **Goldberg-v0.9.1.apk**. Android 8.0 (API 26) or later is required.
3. Install over the existing compatible Goldberg installation. The package is **com.opus.piano**, version **0.9.1**, version code **8**. The APK certificate is compared directly with the published 0.9.0 APK; the existing compatible signing key is retained.
4. If Android rejects the update, stop and retain the existing installation and backup. Do not uninstall to bypass a signing error.

## Verification and limitations

Automated checks: 63 JVM tests passed; migration paths 1–5 → 6, backup fixture preservation, 689 paired CZ/EN resources and history paging checks passed. Clean application and instrumentation APK builds passed. Lint reported zero errors and 23 nonblocking warnings. See [verification details](https://github.com/stokonn/Opus/blob/v0.9.1/docs/VERIFICATION-0.9.1.md) for scope and limitations. Host SQLite migration and backup-fixture checks do not execute Android's Room/backup implementation. Instrumentation APK compilation does not constitute execution of instrumentation tests.

**This Beta build passed the reported automated checks but has not yet been validated on a physical Android device. Real-device Smart Practice behavior, actual in-place upgrade and launcher rendering remain unverified.**

Physical-phone testing was explicitly waived for this Beta. No phone was installed, uninstalled, updated or wiped. Instrumentation tests were compiled but **NOT EXECUTED**; no accelerated emulator was available (`/dev/kvm` absent). Screen-off detection, microphone/foreground-service behavior, Android accessibility, larger-font rendering and actual installation data preservation remain unverified on Android hardware. Smart Practice remains experimental. Recovery exports preserve raw draft data for diagnosis/recovery; they are not ordinary importable Goldberg backups.

APK SHA-256: `32b246f158e8d97b13089da0150bd9ddb3bba42bcfe09545b69a571583fee31d`

Signing certificate SHA-256: `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`
