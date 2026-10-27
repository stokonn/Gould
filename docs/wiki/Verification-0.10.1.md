# Goldberg 0.10.1 verification

Goldberg 0.10.1 was verified with automated host checks and Android emulator-5582. No physical device or owner acceptance testing was used for this release.

- 75 JVM tests and 36 separately executed Android instrumentation tests pass.
- Gradle build, instrumentation compilation and lint pass: zero lint errors, 74 warnings.
- Historical migration, backup, paging, localization and policy checks pass; schema 8 / backup format 4 are unchanged.
- A seeded install-over-0.10 preserves persistent table values, unfinished draft and appearance/media/identity preferences. The fixture commits and checkpoints WAL before copy and lets 0.10 perform its existing metadata backfill before comparison.
- Runtime completion/History edits preserve the session's 3000ms total while adding an untimed association and measured/session/labeled-piece notes.
- Home, Library, Practice/completion, Profile, About, Tux and Appearance were captured and inspected in light/dark, English/Czech and narrow/130% font configurations. Personal design references remain private.
- Package com.opus.piano, code 11, version 0.10.1; trusted signer SHA-256: `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`.
- APK SHA-256: `854bbfebf617ec920df60d181a12e0dcbd2fed10052f2479903a78a290cef312`.

The [repository verification ledger](https://github.com/stokonn/Gould/blob/main/docs/VERIFICATION-0.10.1.md) records exact commands, fixture/setup failures, source hashes, captures and limitations. New Google-account login, physical acoustic detection and carrier transport were not tested; automated identity/media policy checks pass. The [legal audit](https://github.com/stokonn/Gould/blob/main/docs/LEGAL-AUDIT-0.10.1.md) describes external legal/provider uncertainties without claiming certification.

[Home](Home) · [Previous 0.10 evidence](Verification-0.10)

created by stokonn with love <3
