# Goldberg 0.9.2

Goldberg 0.9.2 finalizes the editorial design foundation while retaining the local piano-practice journal and existing package/signing compatibility.

## Changes

- Consistent Ivory/Nocturne themes, Cormorant Garamond/Inter typography and shared buttons, rows, dialogs and compact settings.
- Home with real daily practice, streak, seven-day activity and ten sourced offline quotations; daily source/cache access is bounded.
- Pinned repertoire search/Add, compact filters/sorting, licensed cached composer portraits and optional supplementary downloads with explicit metered-media consent.
- Practice is the single session-start destination. Original failed-recovery data is archived unchanged before an explicitly confirmed fresh session. Recovery never automatically restarts capture.
- Compact Profile with musical favorites, biography, four statistics, optional Google identity and a five-badge showcase. Google identity does not synchronize practice history.
- Compact paged history, full session review, improved Czech/English formatting and corrected keyboard insets across Practice and editors.
- Connected thirteen-note Bach's Secret keyboard and a separate offline logo-long-press Tux easter egg.
- Fix manual-session recovery validation, Czech composer filter identifiers, the Home Free Practice empty state and Google-avatar sampling initialization.
- Permanent agent policy, GitHub development tracking, quality-check workflow and English Wiki.

Related work: #2 #3 #4 #5 #6 #7 #8 #9 #10 #11 #12 #13 #14 #15 #16 #18.

## Verification

70 JVM tests and 31 actually executed emulator instrumentation tests passed. Migration, historical backup, history paging, 755-resource CZ/EN, agent-policy and APK checks passed. Lint: zero errors, 57 reviewed warnings. Representative rendered screens were inspected in both themes/locales and at 360dp/130% font scale. Seeded 0.9.1-to-0.9.2 emulator upgrade checks retained the tested records.

Google display-name import, profile-picture import after media consent, customized nickname preservation, authenticated restart, sign-out with local data preservation and account switching all passed **owner-reported manual verification on a Samsung Galaxy A55**. The owner performed those tests; Codex did not perform physical-device testing. No unrelated results are inferred from that report.

## Install or update

Download `Goldberg-v0.9.2.apk` from this release and install it over the existing compatible Goldberg installation. Export a backup first. Keep the installed application/data if Android reports a signature problem; do not uninstall to bypass it. Android 8.0+ is required.

Package: `com.opus.piano`; version code: `9`. Room upgrades explicitly from schema 6 to 7. New backups use format 3 and read older formats 1/2; older Goldberg versions cannot import format-3 backups. Keep previous backups.

The APK deliberately retains the same compatible **Android Debug signing certificate** used by the published 0.9.1 APK; no production key or new identity is substituted. SHA-256 certificate fingerprint: `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`.

APK SHA-256: `7470fe7b477678946040df452813fc47708489883543bb3ea57bb2148bab4e7f`.

## Known limitations

Smart Practice remains experimental. Physical-device screen-off capture, battery behavior and all hardware/Android combinations are not certified. The six owner-reported Google checks are distinct from automated/emulator evidence; no exact installed APK digest was supplied. Audible keyboard playback was not independently heard on the no-audio emulator, and the full screen/theme/font cross-product was not tested. External Google/catalogue/media/quotation services can fail; local profiles, cached media and bundled quotes remain available. Lint warnings and untested third-party AI-agent runtimes remain documented. See the official Wiki verification page for the full evidence boundaries.

[Official Wiki](https://github.com/stokonn/Goldberg/wiki) · [Verification](https://github.com/stokonn/Goldberg/wiki/Verification) · [Development Project](https://github.com/users/stokonn/projects/2) (authorized accounts)

created by stokonn with love <3
