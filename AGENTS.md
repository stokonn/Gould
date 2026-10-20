# Goldberg — permanent development policy

This is the canonical policy for every coding agent and every Goldberg version (0.9.2, 0.10 and beyond). Preserve it in future updates. Thin tool-specific instruction files reference this file; do not maintain competing copies. Newer explicit owner instructions and higher-priority system/security rules take precedence.

## Work autonomously and protect data

Inspect the current branch, status, remote, tags, releases, milestone and Issues before work. Document an implementation checklist. Perform routine inspection, editing, builds, tests and safe Git operations independently. Ask only for important product decisions, unavailable credentials or genuinely destructive/security-sensitive operations. Respect mandatory sandbox and permission controls; never bypass them. Preserve unrelated work, especially the existing untracked RELEASE_NOTES.md. Never force-push, reset unrelated work, commit secrets, rotate signing keys or overwrite release tags.

Data integrity outranks feature completeness and visual polish. Preserve history, unfinished drafts, multi-piece attribution, repertoire, profile/avatar/nickname, settings, badge dates and showcase. Never use destructive Room migrations or database recreation. Add explicit tested migrations only when necessary. Preserve backup compatibility and isolate disposable media from critical data. Never silently replace/delete an unfinished draft; recovery retry and unchanged-data export must remain possible. New-session consent alone is not consent to delete the original recovery data.

## Architecture and coding

Goldberg is an existing native Kotlin / Jetpack Compose / Material 3 Android application. Keep application ID/namespace com.opus.piano and database opus.db. Reuse the application-scoped PracticeCoordinator state machine, established repositories and Compose Navigation; do not introduce competing timers or unnecessary dependencies. Use monotonic time for elapsed practice and preserve exact per-piece/session reconciliation and idempotent completion. Persist startup before publishing running state or starting capture; recovery must never automatically restart the microphone.

Extend the central design system and reusable components. Use centralized semantic theme tokens, Cormorant Garamond/Inter, consistent spacing, responsive layouts, accessible touch targets and Android font scaling. Support Ivory/Nocturne and CZ/EN; use localized resources, proper plurals, application-locale dates/numbers and accessibility descriptions. Never automatically translate user-entered text or original catalogue work/composer names. Respect reduced motion. Keep network/storage/decoding work off the main thread. Metered media downloads require explicit permission; retain attribution/license metadata and safe offline fallbacks.

## Never use the owner's physical phone

Never install, launch, test, wipe or run ADB/instrumentation against the owner's physical Android phone. Use host tests and emulators. Every ADB operation must explicitly target an identified emulator serial (emulator-NNNN); never use implicit device selection. Never claim that an agent performed physical-device verification. Explicit owner-reported manual results may be recorded with clear attribution; never infer additional checks from them. Emulator screenshots are not physical-device evidence.

## Permanent GitHub workflow

Discover the actual repository (currently stokonn/Goldberg; legacy Opus URLs may redirect). Inspect existing resources; do not duplicate them. Reuse one modern Project named Goldberg Development across releases. Track significant tasks with Issues, version milestones, labels and Status (Backlog/Todo/In Progress/Done); add Priority/Version/Type when supported. Keep unfinished/unverified work open; close tasks only after relevant implementation and verification, with commit/PR evidence. Do not invent future-version scope.

Maintain English-only official Wiki pages and navigation: Home, Getting Started, User Guide, Settings & Customization, Developer Guide, FAQ & Troubleshooting, Changelog & Roadmap, Credits & Licenses. Document only actual working features; clearly label blocked integrations and unverified behavior. Use verified screenshots and licensed attribution; include `created by stokonn with love <3`. Keep local maintainable Wiki sources in docs/wiki. Check Wiki enablement/initialization and GitHub token permissions; report exact blockers without claiming success or starting unauthorized authentication.

For each release: inspect → identify milestone/Issues → plan → implement → run relevant checks → build APK → verify acceptance → synchronize Project/Wiki → update README/changelog/docs → commit/push → publish release → report results. Commit/push/release are automatically authorized only after every mandatory gate passes. Never invoke an old release script without inspecting its actions; do not publish merely because Gradle succeeds.

## Stage checklist documentation (permanent, from 2026-10-20)

Every implementation stage of every Goldberg version MUST have a detailed entry in the version's project checklist at `docs/planning/<version>/STAGE-CHECKLIST.md`, and the checklist must stay synchronized with actual work — updated continuously as work progresses, not only at the end. Each stage entry must contain:

- **Stage name and purpose** — explain what the stage actually does and why it exists.
- **Scope** — which features, systems, components, databases and files it affects.
- **Tasks** — individual actionable items written as checkboxes.
- **Progress** — completed, in-progress and pending tasks, honestly reflected.
- **Verification** — the tests/acceptance checks with their real executed results (counts, environments, what was *not* executed).
- **Completion status** — mark a stage complete ONLY after its verification passed; unchecked boxes must never be implied as done, and "Stage X — Complete" alone is never acceptable without explaining what the stage accomplished.

Verified project information only: never invent completed work, tests or evidence. For Goldberg 0.11 the checklist is `docs/planning/goldberg-0.11/STAGE-CHECKLIST.md` (retroactively covering Stages 1–5); future versions create their own checklist file under their planning directory and keep it synchronized through release and beyond.

## GitHub Issue stage checklists (permanent, from 2026-10-20)

Every implementation stage MUST maintain its existing GitHub Issue as a living checklist, synchronized with the local stage checklist and GitHub Project. Each stage issue must describe the stage's purpose, scope and tasks as checkboxes. Update the checkboxes continuously as work progresses; mark a task complete only after its verification has actually passed. Keep GitHub Issues, the GitHub Project status and local documentation (`docs/planning/<version>/STAGE-CHECKLIST.md`, `docs/ai/*`) synchronized — never let them disagree about what is done. Preserve existing issue content and history: never delete or rewrite prior comments or task descriptions; append progress. Never create duplicate issues for a stage — reuse the existing tracking issue (for 0.11: umbrella Issue #31).

For every stage, update BOTH the main Issue description and the checkpoint comments: the description carries the authoritative stage checklist and current status (in-progress/completed with evidence), while appended comments carry point-in-time checkpoints; never let either drift or contradict the other, and never leave a stale status in the description after a stage becomes authorized or progresses. After a stage passes final acceptance, consolidate its historical checkpoint comments into one concise authoritative summary in the description (purpose, status, verified commit/branch, final results, limitations, link to the final acceptance comment) and keep the comments as the permanent chronological audit trail — never delete them, and never leave superseded WIP summaries or intermediate test counts where they could be mistaken for current status. The description is the dashboard; comments are history.

## Automatic GitHub Issue checkpoints (permanent, from 2026-10-20)

Automatically update the existing Stage GitHub Issue and its checklist at every verified milestone, before planned session termination, and proactively when context usage approaches 75–80%. Each checkpoint must state: completed and pending tasks; current implementation status; test results and blockers; current branch and commit; and exact next steps for a new agent. Keep the GitHub Issue, GitHub Project status, local checkpoint documentation (`docs/planning/<version>/STAGE-CHECKLIST.md`, `docs/ai/*`) and AGENTS.md synchronized. Never create duplicate Stage Issues and never mark unverified tasks complete. At the beginning of a new session, read the latest checkpoint before continuing work.

Implement appropriate persistent workflow safeguards where supported; do not assume context compaction automatically triggers GitHub synchronization. If GitHub is unavailable, save a local checkpoint in the planning/ai docs and report that GitHub synchronization is pending. This rule applies to the existing workflow without disrupting the current stage in progress.

## Quality and publication gates

Run appropriate existing/new JVM tests, migration and backup checks, localization/paging checks, lint, debug APK build and instrumentation compilation/execution where available. Add meaningful regression tests for data/timing/cache/permission changes. Inspect rendered UI across light/dark themes, representative sizes and larger fonts. Verify network/offline behavior, recovery and an in-place upgrade on a seeded emulator when required. Distinguish executed/passed, failed, compiled-only and unexecuted checks. No fabricated results.

Use the project's Gradle wrapper; typical checks:
`./gradlew clean assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug --no-daemon --max-workers=1`
Run relevant `scripts/check_*.py` checks. Do not treat APK compilation as instrumentation execution. Verify actual package/version/code, ZIP/resources, compatible certificate against the trusted previous APK, APK SHA-256 and source/artifact identity. Trusted signing SHA-256: 53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e. Preserve existing compatible signing configuration; never uninstall to bypass mismatch.

Emulator verification (Stage 5 and beyond) may run through the dispatch-only CI workflow `.github/workflows/stage5-emulator.yml` (KVM `ubuntu-24.04`, explicit `emulator-NNNN` + `ro.kernel.qemu=1` guards, SHA binding between the run and the dispatched commit, raw tool diagnostics preserved as artifacts, never the owner's physical phone, never production signing credentials). Treat CI results as evidence only for the exact dispatched commit; classify pre-test/setup failures honestly as CI failures rather than application results, and never re-dispatch without a meaningful verified fix.

If a mandatory check fails or cannot be verified, stop publication, explain the blocker, and continue independent safe work. Missing Google OAuth configuration must be reported; never fake sign-in. Release type follows the explicit owner requirement for that version (0.9.2: normal release titled Goldberg 0.9.2, only after its mandatory gates pass). Safely integrate verified source without history rewriting, create a new annotated version tag, upload the verified APK/changelog, verify remote release/asset digests and update tracking/docs. Never overwrite conflicting tags/releases.

Final reports must include significant files/architecture/database changes, exact commands/results, screenshots actually inspected, unexecuted checks, external setup/blockers, branch/status/commit, APK path/hash/signing, and truthful Project/Wiki/release URLs and Issue statuses.

## Goldberg 0.10 visual approval gate

Use docs/design/goldberg-0.10/README.md and manifest.json; 01_HOME_APPROVED and 02_PROFILE_APPROVED are the primary references. Inspect all supplied screens, retain four tabs/top-right Settings and every appearance preference, and never package reference images in the APK. Compare actual emulator renders in both themes/font scales and document meaningful differences. Obtain explicit owner visual approval before publication unless the owner explicitly waives screenshot review for that release. For 0.10 the owner waived personal screenshot review on 2026-10-17; minor differences are listed for possible 0.10.1 improvements. Earlier mockup approval and passing automated tests alone are not final visual sign-off.

## Legacy Cleanup & Repository Maintenance

Before development or repository maintenance, read [docs/maintenance/LEGACY-CLEANUP.md](docs/maintenance/LEGACY-CLEANUP.md). Follow its documented policy for identifying obsolete code, redundant assets, unused files and technical debt; remove only verified unnecessary components. Preserve user data, migrations, backup formats, release history, legal documentation, signing identity and compatibility-sensitive identifiers. Do not start substantial cleanup during unfinished release verification. Perform safe routine maintenance autonomously, using automated checks and explicitly targeted emulators rather than physical user devices. Document cleanup changes and actual verification in the permanent maintenance record, preserving its history across future Goldberg versions. Do not publish a new application release solely for maintenance without authorization.
