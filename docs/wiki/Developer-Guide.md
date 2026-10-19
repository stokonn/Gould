# Developer Guide

Goldberg is the existing Kotlin/Jetpack Compose/Material 3 app in the stokonn/Goldberg repository. Legacy stokonn/Opus links redirect. The Android app module is app/, package/namespace com.opus.piano, database opus.db. The Compose plugin uses Kotlin 2.2.10, AGP 9.1.0, KSP 2.3.12 and Gradle wrapper 9.3.1; compile/target SDK 36, min SDK 26, JVM target 17. Use a compatible installed JDK and Android SDK.

PracticeCoordinator is the application-scoped serialized practice owner. PracticeEngine handles monotonic accounting; Room repositories persist checkpoints and complete sessions idempotently. UI/service must not own independent timers. Preserve the initial durable-start gate and no-auto-capture recovery policy. Schema 8 adds local composer favorites and untimed links for historical session-piece edits through an additive 7→8 migration; prior profile personalization and recovery archives were added in schema 7. Migrations and historical Room schemas remain under app/schemas. Backup format 4 serializes the new data and retains readers for formats 1–3; do not change these contracts silently.

CatalogRepository uses bounded, allowlisted HTTPS responses and separate disposable media. MediaPreferences supplies metered consent. DailyQuotes bundles exactly ten sourced public-domain excerpts and uses stable local-day selection plus a once-daily bounded verification/cache request. No speculative catalogue expansion or new cloud data model is introduced.

Build and check:

```
./gradlew clean assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug --no-daemon --max-workers=1
python3 scripts/check_migrations.py
python3 scripts/check_backup_fixtures.py
python3 scripts/check_localization.py
python3 scripts/check_history_paging.py
python3 scripts/check_agent_policy.py
```

JVM tests and host SQLite checks are distinct from Android instrumentation. Compiling an instrumentation APK is not test execution. UI acceptance includes actual emulator rendering across size/theme/font scales, relevant network scenarios and seeded in-place upgrade. Never target the owner's physical phone. Every ADB invocation must name an emulator serial explicitly.

## Google local identity

`identity/GoogleIdentity.kt` owns optional Credential Manager requests and device-local identity selection. The public Web client ID is a non-secret string resource. Credentials 1.5.0 and Google ID 1.1.1 are selected for the existing Kotlin compiler; Google ID 1.2.1 requires incompatible Kotlin metadata and is not used. Tokens are never persisted. `IdentityPolicy` protects legacy/manual nicknames; local avatars and media consent remain authoritative. This is not backend authorization or journal synchronization. Read docs/GOOGLE-IDENTITY-SETUP.md before changing signing or identity configuration.

## AI coding agents

AGENTS.md is the canonical permanent policy for Codex, Claude Code, Gemini CLI, Cursor and GitHub Copilot. Claude's CLAUDE.md imports @AGENTS.md; GEMINI.md imports @./AGENTS.md; Cursor uses .cursor/rules/goldberg.mdc with alwaysApply: true and an explicit read requirement; Copilot uses .github/copilot-instructions.md pointing to the canonical file. Update policy only in AGENTS.md, and keep tool adapters thin.

Official syntax references: [Codex](https://developers.openai.com/codex/guides/agents-md/), [Claude](https://code.claude.com/docs/en/memory), [Gemini](https://geminicli.com/docs/cli/gemini-md/), [Cursor](https://cursor.com/docs/rules), [Copilot](https://docs.github.com/en/copilot/how-tos/configure-custom-instructions/add-repository-instructions). File references do not universally inline content; agents must explicitly read the canonical [AGENTS.md](https://github.com/stokonn/Goldberg/blob/main/AGENTS.md) if necessary. Gemini/Claude/Cursor executables were not installed in this environment; no runtime compatibility test is claimed. Copilot loading depends on its client settings and remains unverified. Codex is the active development agent.

## Permanent GitHub workflow

Reuse the [Goldberg Development Project](https://github.com/users/stokonn/projects/2) across versions and organize Issues by milestone. Its Board view is grouped by Status; it remains private to authorized accounts. Update statuses truthfully and close only verified tasks. Maintain the English Wiki from the repository's `docs/wiki` sources and keep navigation/screenshot evidence current. The canonical [AGENTS.md](https://github.com/stokonn/Goldberg/blob/main/AGENTS.md) authorizes automatic commit/push/release only after all mandatory acceptance gates pass, including source/artifact/version/signing checks. These instruction files are behavioral policy, not a GitHub branch-protection enforcement mechanism. Missing permissions or an uninitialized Wiki remain reported blockers; never claim remote operations succeeded without checking the API.

Do not invoke old release scripts blindly. A successful compile alone cannot authorize publication. Keep the current compatible signing key, compare actual APK certificates, publish only a new nonconflicting annotated version tag and verify the uploaded asset digest.

[Home](Home) · [Credits](Credits-and-Licenses)

## Goldberg 0.10 development

Goldberg 0.10 is released. The owner waived personal screenshot review and reports the six Google identity scenarios as manual PASS; these results are not Codex physical-device tests. The repository's [implementation plan](https://github.com/stokonn/Goldberg/blob/main/docs/PLAN-0.10.md) and [verification ledger](https://github.com/stokonn/Goldberg/blob/main/docs/VERIFICATION-0.10.md) retain the detailed evidence. Future work belongs in approved milestones and is not inferred from this release.

## Permanent repository maintenance

Before development or cleanup, read root AGENTS.md and the [permanent maintenance ledger](https://github.com/stokonn/Goldberg/blob/main/docs/maintenance/LEGACY-CLEANUP.md). It records proven unused components, protected compatibility files, branch decisions, measured storage and verification. Finish release verification before substantial cleanup; preserve historical schemas, fixtures, attribution and upgrade identities.

## Goldberg 0.11 Stage 3A development checkpoint

The published application remains Goldberg 0.10.1. On the dedicated [Stage 3A branch](https://github.com/stokonn/Goldberg/tree/feature/goldberg-0.11-stage-3a), the verified persistence implementation adds personal Room schema 9 and a separate catalogue Room schema 1. This is unreleased development work; no Library redesign, new provider integration, downloads or backend is implied.

`opus.db` remains authoritative. Explicit migration 8→9 retains all legacy tables and adds a durable UUID registry, original personal snapshots and journal scope. Original Piece IDs, numeric Open Opus keys, session timing/tokens, notes, recovery bytes, profile, favorite dates and recording links remain intact. The optional `goldberg-catalogue.db` has typed entities/relationships, aliases/FTS4, provenance/rights and reversible mapping evidence. Catalogue opening and bounded local reconstruction are lazy and independent of practice startup; failed generations are preserved and rebuilt through a validated generation pointer.

Actual API 26 and API 37 emulator suites cover historical migrations 1–8, rollback, catalogue corruption/deletion/rebuild, FTS4, timing/recovery and legacy backup compatibility. A seeded signed upgrade from 0.10.1 preserved 37 sessions and critical data offline. See the [verification ledger](https://github.com/stokonn/Goldberg/blob/feature/goldberg-0.11-stage-3a/docs/planning/goldberg-0.11/stage-3a/VERIFICATION.md) for exact results, source/APK identity, performance and limitations.

Portable exports still use format 4/schema 8 with readers 1–4. They do not yet carry the new UUID registry, portable settings or active drafts across installations. An unfinished destination draft blocks replacement before deletion. Stage 3B must separately implement backup v5 and tested multi-store recovery. Never open a newer personal schema with an old APK or uninstall to bypass incompatibility. Keep all historical schemas and fixtures.


## Goldberg 0.11 Stage 3B development checkpoint

Published production remains0.10.1. The separately verified [Stage3B branch](https://github.com/stokonn/Goldberg/tree/feature/goldberg-0.11-stage-3b) adds an explicit personal Room9→10 migration and portable backup format5. The independent catalogue stays schema1 and is unnecessary for restore. This is unreleased branch functionality, with no provider/cloud/auth/download/UI redesign or public version change.

V5 includes the complete personal journal/profile/badges/favorites, durable UUID registry/anchors/snapshots, allowlisted settings and exact raw drafts/recovery archives/completion evidence. Historical readers1–4 remain. Validate hashes/types/references in isolated Room staging, save a complete destination snapshot, then replace the journal in one Room transaction while retaining destination recovery/completion evidence. Fresh restored SQLite allocation must not reuse deleted anchored owner IDs. Preferences use a durable pending generation/hash with idempotent startup/retry application; do not claim a transaction spanning SQLite/preferences/filesystems. Valid recovery is paused with a fresh execution identity, including zero-credit preparatory notes; unknown/malformed bytes stay exportable. Duplicate token handling never removes another draft or revives completed credit.

Never export authentication secrets/accounts/admin roles/platform permission grants. Cellular consent is inactive after import; desired reminders respect actual permissions. Private snapshot publication uses AtomicFile; external document providers cannot promise atomic replacement. Backups remain plaintext with32MiB and complexity limits; original data is never truncated. Recovery/source/snapshot copies deliberately have no automatic GC. Keep all schemas, historical fixtures and recovery evidence; never open a newer database with an older APK.

Executed:78 JVM tests; lint0 errors/89 warnings;75 suite entries on each API26/API37, plus four real crash/restart cases per API; both signed seeded0.10.1 upgrades preserved37 sessions and critical data. Actual offline Android picker import/export verified live Czech/appearance settings, raw drafts, original destination snapshot and inactive microphone/cellular grants. [Complete verification](https://github.com/stokonn/Goldberg/blob/feature/goldberg-0.11-stage-3b/docs/planning/goldberg-0.11/stage-3b/VERIFICATION.md) distinguishes host checks, actual Android execution, deliberate process termination, resolved failures, measurements and limits. No physical phone was used. Stage4 requires a separate owner instruction; do not merge or release automatically.

created by stokonn with love <3

## Goldberg 0.11 Stage 4 — implementation in progress

Published production remains **0.10.1**. The [Stage 4 feature branch](https://github.com/stokonn/Goldberg/tree/feature/goldberg-0.11-stage-4) is unreleased work, with acceptance gates still open. It retains personal Room schema10, optional catalogue schema1 and portable backup5/readers1–4; no destructive migration, application-version bump, cloud service or account setup is introduced.

The development branch adds native local-first Discover, explicit bounded provider actions, typed identities/relationships, immutable accepted evidence, isolated disposable observations and strictly licensed optional portraits. Repertoire filtering uses saved content only, independently from explicit online catalogue search. Accepted contradictions remain evidence and never merge identities by title. Same-journal catalogue rebuilding retains readable accepted provenance and regenerates FTS; explicit corruption recovery retains the unreadable generation and its bytes. Failure must leave the personal journal, backups and recovery available.

Verification is incremental: [the recovery ledger](https://github.com/stokonn/Goldberg/blob/feature/goldberg-0.11-stage-4/docs/planning/goldberg-0.11/stage-4/RESUME-CHECKPOINT.md) records exact source/artifact scopes, executed emulator matrices, deliberate failure/fix evidence, bounded provider observations and measured synthetic search vectors. Earlier full suites/upgrades do not certify newer APKs. Wikidata overload and MusicBrainz throttling stop further probing. Large-font navigation, broader localized details/accessibility, storage/native/startup measurements and final complete suites/process-death/seeded upgrades remain acceptance work. No Stage 4 completion, publication readiness or real provider-identity certification is claimed.
