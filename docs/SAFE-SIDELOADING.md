# Is it safe to sideload Gould?

Gould is distributed as an Android APK from the project's GitHub Releases. The
current published build is Gould 0.14.0, a pre-release, not Stable. Install
only the APK linked from the [official release page](https://github.com/stokonn/Gould/releases/tag/v0.14.0);
avoid third-party mirrors and APK repackaging sites.

Before updating, export a backup in **Settings → Data & Storage** and keep it
somewhere outside Gould. The APK uses application ID `com.opus.piano` and
requires Android 11 (API 30) or newer. Install it over the existing compatible
Gould/Goldberg app. If Android reports a signing or version mismatch, stop and
keep the installed app; do not uninstall to bypass the check.

For the published 0.14.0 APK, compare its SHA-256 with the release record before
installing:

```text
62c83ddef804a83b71bd05f3f0b09cec29a0784902ad79b0b16ec8bae75b1f2c
```

You can calculate a downloaded file's hash with `sha256sum gould-0.14.0.apk` on
Linux/macOS or `Get-FileHash gould-0.14.0.apk -Algorithm SHA256` in PowerShell.
The expected hash is also published in [release verification](releases/GOULD-0.14.md).

Android may show an installation warning because the APK is installed outside
Google Play. That warning does not verify the file's source; use the official
release link and hash above. Do not grant unrelated permissions to complete an
install. Gould does not require practice data, microphone recordings or backup
files to be uploaded to install or update the app.

## Compatibility and data

- Android 11 / API 30 is the minimum for 0.14.0. Android 8–10 cannot install
  this version; their existing installations and data are not changed by a
  failed install.
- The package remains `com.opus.piano`, and the release uses the trusted
  project signing identity. Do not uninstall if an update is rejected.
- A backup is a safety copy, not a substitute for verifying the downloaded APK.
  JSON backups exclude recording bytes; media exports are a separate explicit
  action.
- 0.14.0 is a pre-release. Signed in-place upgrade verification and visual
  emulator review remain unexecuted; see the linked release verification.

created by stokonn with love <3
