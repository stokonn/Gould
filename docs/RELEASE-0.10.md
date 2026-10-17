# Goldberg 0.10 — The Definitive UI/UX Redesign

Goldberg 0.10 refines the existing piano journal with a consistent editorial interface while preserving its four destinations, local-first data model and appearance customization.

## What's changed

- Redesigned Home activity, weekly practice chart, daily quotation and recent-session summary using recorded data.
- Reorganized Repertoire search, Add, Sort and Filters; added locally pinned composer favorites and rounded-rectangle composer portraits.
- Refined Profile identity, statistics, favorite composer and local composer autocomplete. Google identity remains optional and does not synchronize practice data.
- Added safe editing of the pieces associated with completed sessions. Added historical piece links receive no invented duration or practice credit.
- Refined Settings and retained theme, accent, typography, card, density and sorting preferences.
- Added additive Room schema 8 and backup format 4. Import continues to support backup formats 1–3.
- Preserved the application ID `com.opus.piano` and compatible signing identity.

## Verification

- 72 JVM tests passed.
- 36 Android instrumentation tests executed and passed on an Android emulator.
- Room schema migrations 1–7 to 8, historical backup formats 1–3, history paging, localization and agent-policy checks passed.
- Android lint reported zero errors and 78 non-failing warnings, primarily unused resources and dependency/API recommendations.
- The UI was inspected in light and dark themes and at normal and enlarged text on an emulator. The owner waived personal screenshot review.
- **Owner-reported manual verification:** Google sign-in, display-name and avatar import with consent, preservation of manual nickname/avatar overrides, restart persistence, sign-out, account switching and local practice-data preservation passed on a Samsung Galaxy A55. Codex did not perform these physical-device tests.
- APK SHA-256: `20a5962cba89075a0d998b156097ddff86a22de1a0a2ac8ef636a8ff963e73d3`.

## Upgrade and data

Install the APK over Goldberg 0.9.2; do not uninstall to upgrade. Export a backup first as a general precaution. The application package and compatible signing certificate are unchanged. Schema migration is additive, and older supported backup formats remain readable. New historical piece associations do not alter the original session duration or create credited practice time.

Goldberg stores practice data locally. Google sign-in supplies optional profile identity only; it is not cloud backup or practice synchronization.

## Known limitations

- Live Wi-Fi and metered mobile-data transfers were not certified end to end; consent, cache, offline fallback and failure behavior have automated coverage.
- At enlarged font sizes, Home and Profile use accessible scrolling. The ready-state Practice screen has more open space than the supplied mockup, and small test repertoires naturally look sparse. These are documented as possible 0.10.1 refinements.
- Codex did not test the application on a physical Android device. The Google results above are explicitly owner-reported manual verification.

See [verification details](VERIFICATION-0.10.md) for APK identity, checksum, test evidence, screenshots and comparison notes.
