# Goldberg 0.10.1 published checkpoint

Published before universal maintenance on 2026-10-18 at 03:28:36 UTC.

- Normal release: https://github.com/stokonn/Goldberg/releases/tag/v0.10.1
- Annotated tag object: `6bb0ecd7c2c63d69c7649ed78e45824b237564db`.
- Release/tag source: `b19b77b81e76feef6baf83e6c4bfa935e7c10bea`; implementation: `56851cb5da30d9e823f6374562f7773e96a35e46`.
- Independent remote quality run PASS: https://github.com/stokonn/Goldberg/actions/runs/39093322636
- Wiki release synchronization: `44d7fe50b0b33a0b393aa6f85438aa19830ad599`, https://github.com/stokonn/Goldberg/wiki.
- APK: `.gradle/release-artifacts/Goldberg-v0.10.1.apk`, version 0.10.1 / code 11 / com.opus.piano.
- APK SHA-256: `854bbfebf617ec920df60d181a12e0dcbd2fed10052f2479903a78a290cef312`.
- Trusted signer: `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`.
- APK, changelog and SHA256SUMS downloaded from GitHub match local verified bytes; GitHub's asset digest matches the APK hash.
- 75 JVM / 36 executed emulator tests; clean build, lint, historical migrations/backups, paging/localization, seeded WAL-safe upgrade, source/artifact verification and rendered UI acceptance passed. See VERIFICATION-0.10.1.md.

Post-release maintenance is a separate change, recorded in [the permanent maintenance ledger](maintenance/LEGACY-CLEANUP.md). The v0.10.1 tag and uploaded APK remain immutable. A later maintenance build must not be confused with the published artifact. No application release is authorized solely for cleanup. 0.11 remains planning-only.

RELEASE_NOTES.md remains unrelated and untracked; the external Opus-backup-0101 backup and active checkout path remain untouched.
