# Goldberg

A native Android piano practice journal built with Kotlin, Jetpack Compose, Material 3 and Room. Android 8.0+; English and Czech; light and dark themes. Your journal stays local.

**Goldberg 0.10.1 — Final Redesign Polish** refines the integrated Library, saved-session completion, Profile, About and appearance previews. See [release notes](docs/RELEASE-0.10.1.md) and [verification](docs/VERIFICATION-0.10.1.md).

[Download](https://github.com/stokonn/Goldberg/releases) · [Official Wiki](https://github.com/stokonn/Goldberg/wiki) · [Issues](https://github.com/stokonn/Goldberg/issues) · [Development Project](https://github.com/users/stokonn/projects/2) (authorized collaborators)

- Durable manual and experimental Smart Practice, multi-piece sessions, notes and review.
- Local repertoire, catalogue information, paged history and per-piece accounting.
- Profile, earned badges and a five-badge showcase.
- Weekly activity, local composer favorites and searchable Favorite Artist selection.
- Post-session piece-list edits that preserve recorded session time.
- Portable backups, restore, CSV exports and optional reminders.
- Classic Piano branding and Bach’s Secret.

![Goldberg 0.10.1 Profile](docs/wiki/images/goldberg-0.10.1-profile.png)

Before updating, export a backup. Install over an existing Goldberg version; never uninstall to bypass a signing error. Google identity does not synchronize practice data. For 0.10, sign-in, display-name/avatar import, manual override preservation, restart persistence, sign-out, account switching and local-data preservation passed owner-reported manual verification on a Galaxy A55. Codex performed no physical-phone testing. See [0.10 verification evidence](docs/VERIFICATION-0.10.md) and [release notes](docs/RELEASE-0.10.md).

## Development

Read [AGENTS.md](AGENTS.md) for the canonical development and release policy, [the Developer Guide](docs/wiki/Developer-Guide.md) for architecture and AI-agent integrations, and [the 0.10.1 implementation checklist](docs/PLAN-0.10.1.md). Use the existing Gradle wrapper, JDK 17-compatible toolchain and Android SDK 36:

```sh
./gradlew clean assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug --no-daemon --max-workers=1
```

Build artifacts are under `app/build/outputs/apk/`. Compilation is not device verification. Mandatory quality, data-compatibility, rendered-UI and signing checks must pass before the automatic release workflow. Use emulators only; never target the owner's physical phone. The GitHub quality workflow runs host checks; compatible release signing and emulator/owner acceptance remain separate gates. Local and remote CI results are recorded separately.

[Signing](docs/SIGNING.md) · [Backup](docs/BACKUP.md) · [Google configuration](docs/GOOGLE-IDENTITY-SETUP.md) · [Changelog](CHANGELOG.md) · [Licenses](docs/LICENSES.md)

created by stokonn with love <3
