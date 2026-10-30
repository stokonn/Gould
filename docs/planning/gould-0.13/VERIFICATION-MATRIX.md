# Gould 0.13 — verification decision matrix

Three tiers, defined in `AGENTS.md`. Read this matrix to decide what a change
invalidates; do not rerun tiers wholesale without an invalidation reason.
Every executed run appends one line to `verification-audit.jsonl` via
`scripts/verify-audit.py` (called automatically by the tier scripts).

| Change area | FAST (everyday) | INTEGRATION (feature boundary) | RELEASE (source freeze, additive) |
| --- | --- | --- | --- |
| Kotlin logic, no UI/DB contract | `compileDebugKotlin` + focused JVM tests | full JVM suite | full clean policy gate |
| Compose UI / layout / theme | compile + affected JVM tests (`Redesign*Test`, `WidgetProjectionTest`…) | full JVM + affected native UI classes on emulator | full native suite + documented emulator visual QA |
| Room entity / DAO / migration | + `check_migrations`, `check_backup_fixtures`, `check_history_paging` | `DatabaseTest`, `Stage3aMigrationTest`, `StudioMigrationTest`, `Goldberg08MigrationTest` | genuine published-version in-place upgrade on seeded emulator; backup/restore; 28-table preservation |
| Backup / restore / serialization | + `check_backup_fixtures` | `Stage3aBackupTest`, `StudioBackupTest`, `Stage8*Restore*` | cross-version backup compatibility + restore acceptance |
| Audio / recording / metronome | compile + `MetronomePcmTest`, `PlaybackEvidenceTest`, `DurableWavTest` | `RecordingServiceTest`, `StudioRecordingTest`, `RecordingLifecycleTest` on emulator | full audio acceptance (real device/emulator sound routing evidence) |
| Widgets | + `WidgetProjectionTest` | `WidgetTest` native + launcher acceptance | full launcher/widget matrix |
| Practice engine / timing | + `PracticeEngineTest`, `PracticeQueueTest` | `PracticeCoordinatorTest`, `PracticeRecoveryTest` native | controlled-kill/process-death integrity sequence |
| Localization (EN/CZ) | + `check_localization` | `AppLanguageTest` native | full locale/plural/date rendering QA |
| Dependencies / Gradle / build config | compile + full JVM | full JVM + lint + instrumentation **compilation** + smoke install | full clean gate + APK composition + signing + upgrade + CI re-verified at frozen SHA |
| `minifyEnabled` / R8 / resource shrinking | compile + full JVM (shrunk unit tests unaffected) | full JVM + instrumentation compile + emulator **smoke + core journeys** | full native execution + upgrade + every artifact gate; **never ship on compile evidence alone** |
| Docs / planning only | static checks | not applicable | not applicable |
| No change (re-verification) | reuse source-bound evidence | reuse | never rerun without invalidation reason |

## Invalidation rules

- **Source invalidation:** any edit to files under `app/src/main` invalidates all
  tiers that compile or test those files; docs-only edits invalidate nothing.
- **Environment invalidation:** SDK/AGP/Kotlin/Gradle/JDK change, new AVD, or
  dependency refresh invalidates INTEGRATION and RELEASE even if source is unchanged.
- **Evidence invalidation:** a PASS bound to commit X cannot evidence commit Y.
  `verification-audit.jsonl` records the exact commit of every run.
- **Failed-gate rule:** a failed mandatory RELEASE gate blocks publication until
  resolved at the smallest reproducing scope; no blind full-suite retries.

## Tier commands

```bash
scripts/verify-fast.sh                     # FAST: compile + JVM + static checks (daemon, incremental)
scripts/verify-fast.sh --tests 'Pattern*'  # FAST: focused
scripts/verify-integration.sh              # INTEGRATION: full JVM + lint + androidTest compile
INSTRUMENTATION_CLASSES=com.opus.piano.DatabaseTest scripts/verify-integration.sh   # + native
scripts/verify-release.sh                  # RELEASE: exact policy clean gate (mandatory host command)
```

RELEASE-only artifact/signing/upgrade/security gates keep their existing
documented scripts (`verify_quality_apk.py`, `verify-upgrade-signing.sh`,
`verify_emulator_upgrade.py`, `scripts/verify-emulator-upgrade.py` where used)
and are executed by the release runbook, not duplicated here.
