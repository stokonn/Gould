# Goldberg 0.10 — Verification

Goldberg 0.10 is published as a normal [GitHub Release](https://github.com/stokonn/Goldberg/releases/tag/v0.10). The APK was built as package `com.opus.piano`, version `0.10`, version code `10`. The APK uses the trusted compatible certificate. Its uploaded bytes were downloaded and compared against the tested local artifact. APK SHA-256: `20a5962cba89075a0d998b156097ddff86a22de1a0a2ac8ef636a8ff963e73d3`.

## Automated and emulator checks

- 72 JVM tests passed.
- 36 Android instrumentation tests executed and passed on the explicitly targeted Android emulator `emulator-5582`.
- Schema migrations 1–7 to schema 8 passed with legacy data retained.
- Backup formats 1–3 remain readable; current format 4 round-trip and persistence coverage passed.
- History paging/reconciliation, 790 English/Czech resource and plural checks, canonical agent-policy checks and `git diff --check` passed.
- Android lint reported zero errors and 78 warnings, principally unused resources and dependency/API freshness recommendations.
- Home, Repertoire, Practice, Profile and Settings were inspected on an emulator in light/dark themes and at enlarged font settings. The selected Favorite Artist suggestion and standard keyboard behavior were also captured.

## Owner-reported Google identity verification

On the Goldberg 0.10 candidate, the owner personally tested the following scenarios on a Samsung Galaxy A55. **These results are owner-reported manual verification, not automated tests or Codex physical-device testing.**

| Scenario | Result |
| --- | --- |
| Google sign-in | PASS — owner-reported manual verification |
| Google display-name import | PASS — owner-reported manual verification |
| Profile-picture import after media consent | PASS — owner-reported manual verification |
| Manual nickname/avatar override preservation | PASS — owner-reported manual verification |
| Authentication and identity after restart | PASS — owner-reported manual verification |
| Sign-out, account switching and local practice-data preservation | PASS — owner-reported manual verification |

The emulator's Google account-entry screen was reached, but no emulator account or credentials were configured. Codex did not authenticate on a physical device.

## Visual differences and limitations

The owner waived personal screenshot review for this release. Possible 0.10.1 refinements include the open area on the ready-state Practice screen, sparse Repertoire appearance with small libraries, and scrolling at enlarged fonts. Live Wi-Fi and metered-data downloads were not certified end to end; consent, caching, offline fallback and download failure handling have automated coverage. No physical-device behavior is certified by Codex.

Screenshots in the Wiki Home are actual emulator captures with controlled test data. More detailed evidence and the APK build path are maintained in the [repository verification ledger](https://github.com/stokonn/Goldberg/blob/main/docs/VERIFICATION-0.10.md).

[Home](Home) · [Changelog & Roadmap](Changelog-and-Roadmap) · [Developer Guide](Developer-Guide)
