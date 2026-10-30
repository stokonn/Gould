# Gould — permanent development policy (formerly Goldberg)

This is the canonical policy for every coding agent and every Goldberg version (0.9.2, 0.10 and beyond). Preserve it in future updates. Thin tool-specific instruction files reference this file; do not maintain competing copies. Newer explicit owner instructions and higher-priority system/security rules take precedence.

## Permanent release policy — owner decision, October 25, 2026

Applies to Goldberg 0.11 and all future Gould versions. This supersedes earlier automatic publication authorization and historical RC instructions below.

- During development, build debug APKs only for internal testing and owner review. Do not create new release candidates, prerelease tags or GitHub Releases for development APKs.
- Owner update, final maintenance: remove only GitHub prerelease `v0.11-rc.1` (ID 423537826) and its assets after verifying a complete private metadata/notes/asset backup with SHA-256 hashes. Preserve its Git tag, historical commits, all Stable releases and other tags/assets. Earlier instructions to retain the published RC are superseded by this explicit owner decision.
- Only after full implementation, verification and explicit owner approval, perform a fresh clean release build. It must be non-debuggable, signed with the trusted production certificate, pass all release-specific gates and support verified safe in-place upgrades.
- Publish only the explicitly approved stable version. Merging into `main` and publishing a stable GitHub Release each require explicit owner approval; passing checks alone grants neither.
- The current Stage 8 Discover repair delivers a debug APK and verification report for owner review, with no release publication.

## Gould 0.12 — one development stage (owner decision, October 27, 2026)

Gould 0.12 uses exactly one development stage covering the complete owner-approved feature scope. Internal commits and checkpoints are allowed; do not split it into seven stages or invent scope. Rebranding and repository preparation do not authorize feature implementation. Preserve historical Goldberg stage records. Keep application ID/namespace `com.opus.piano`, all compatibility-sensitive stored identifiers, signing identity, private PMP, existing backups and standalone PianoApp Opus unchanged.

## Final consolidation authorization — October 27, 2026

The verified Gould maintenance history is integrated into canonical `main` in `~/Projects/Opus`; actual repository is `stokonn/Gould` (ID 1421247418). The owner explicitly authorizes normal verified main integration and push, safe backed-up branch retirement, obsolete workflow definition/registration cleanup, and removal of the privately backed-up v0.11-rc.1 prerelease while retaining its tag. Preserve historical workflow runs. Follow [the current main handoff](docs/ai/GOULD-MAIN-MAINTENANCE.md); complete the full Actions API inventory and required main CI before declaring maintenance complete. Preserve the independent PMP repository, private records, application/signing/database identities and all Stable assets. Do not force-push, rewrite history, merge unresolved feature work, publish an application release or implement Gould 0.12 features.

## Verified final administration checkpoint

The full Actions audit is resolved: three required repository YAML definitions and the existing managed Copilot integration (four active registrations); historical runs are preserved. Four backed-up obsolete branches and only the obsolete v0.11-rc.1 prerelease/assets were removed; its tag, Stable and all other releases remain unchanged. See [the execution audit](docs/maintenance/FINAL-ACTIONS-AND-RC-AUDIT.md). Seven historical worktrees remain protected pending private-data custody/disposition; do not repeat destructive administration. Continue repository-wide review and the owner-requested independent OpenCode review without feature implementation. Keep private PMP checkpoints synchronized through documented CLI interfaces; standalone PMP remains unchanged.

## Administration safety review follow-up

Owner-authorized bounded corrections are isolated on `maintenance/admin-safety-review`; do not merge unapproved changes or run historical administration operations. The independent architect review is source analysis, not build/test evidence. Read [the findings and re-review runbook](docs/maintenance/ADMIN-SAFETY-REVIEW.md). Second independent source review at `2c6eb18` confirmed the original four findings addressed; the required-active-registration correction at `85b237f` passed the third host source review using the explicitly requested `--model openai/gpt-6.1-sol`. Require all required active registrations in the initial inventory before backup writes or mutations, retaining the post-operation check. Legacy branch deletion now fails closed without an approved atomic expected-head backend. Bind Git pushes to the API-verified repository, block unresolved workflow decisions before mutations, and preserve per-run receipts. The defined independent source-review gate is PASSED; maintenance stays INCOMPLETE pending explicit owner merge approval and protected historical-data custody. Preserve the original review, existing receipts, owner files and standalone PMP.

## Work autonomously and protect data

Inspect the current branch, status, remote, tags, releases, milestone and Issues before work. Document an implementation checklist. Perform routine inspection, editing, builds, tests and safe Git operations independently. Ask only for important product decisions, unavailable credentials or genuinely destructive/security-sensitive operations. Respect mandatory sandbox and permission controls; never bypass them. Preserve unrelated work, especially the existing untracked RELEASE_NOTES.md. Never force-push, reset unrelated work, commit secrets, rotate signing keys or overwrite release tags.

Data integrity outranks feature completeness and visual polish. Preserve history, unfinished drafts, multi-piece attribution, repertoire, profile/avatar/nickname, settings, badge dates and showcase. Never use destructive Room migrations or database recreation. Add explicit tested migrations only when necessary. Preserve backup compatibility and isolate disposable media from critical data. Never silently replace/delete an unfinished draft; recovery retry and unchanged-data export must remain possible. New-session consent alone is not consent to delete the original recovery data.

## Discover online search consent — owner decision, October 25, 2026

Online Catalogue in Settings is explicit, default-off consent for automatic catalogue-provider search. Show local results immediately, debounce online requests approximately 500 ms for queries of at least two characters, cancel obsolete query/filter/consent work, and respect mobile-data policy and rate limits. Group local/online results clearly without duplicating verified entity IDs; preserve ranking, paging, cache and provenance. No per-query button is required. This does not authorize production community backend networking.

## Architecture and coding

Gould (formerly Goldberg) is an existing native Kotlin / Jetpack Compose / Material 3 Android application. Keep application ID/namespace com.opus.piano and database opus.db. Reuse the application-scoped PracticeCoordinator state machine, established repositories and Compose Navigation; do not introduce competing timers or unnecessary dependencies. Use monotonic time for elapsed practice and preserve exact per-piece/session reconciliation and idempotent completion. Persist startup before publishing running state or starting capture; recovery must never automatically restart the microphone.

Extend the central design system and reusable components. Use centralized semantic theme tokens, Cormorant Garamond/Inter, consistent spacing, responsive layouts, accessible touch targets and Android font scaling. Support Ivory/Nocturne and CZ/EN; use localized resources, proper plurals, application-locale dates/numbers and accessibility descriptions. Never automatically translate user-entered text or original catalogue work/composer names. Respect reduced motion. Keep network/storage/decoding work off the main thread. Metered media downloads require explicit permission; retain attribution/license metadata and safe offline fallbacks.

## Never use the owner's physical phone

Never install, launch, test, wipe or run ADB/instrumentation against the owner's physical Android phone. Use host tests and emulators. Every ADB operation must explicitly target an identified emulator serial (emulator-NNNN); never use implicit device selection. Never claim that an agent performed physical-device verification. Explicit owner-reported manual results may be recorded with clear attribution; never infer additional checks from them. Emulator screenshots are not physical-device evidence.

## Permanent GitHub workflow

Discover the actual repository (currently stokonn/Gould; legacy Goldberg/Opus URLs may redirect). Inspect existing resources; do not duplicate them. Reuse one modern Project named Goldberg Development across releases. Track significant tasks with Issues, version milestones, labels and Status (Backlog/Todo/In Progress/Done); add Priority/Version/Type when supported. Keep unfinished/unverified work open; close tasks only after relevant implementation and verification, with commit/PR evidence. Do not invent future-version scope.

Maintain English-only official Wiki pages and navigation: Home, Getting Started, User Guide, Settings & Customization, Developer Guide, FAQ & Troubleshooting, Changelog & Roadmap, Credits & Licenses. Document only actual working features; clearly label blocked integrations and unverified behavior. Use verified screenshots and licensed attribution; include `created by stokonn with love <3`. Keep local maintainable Wiki sources in docs/wiki. Check Wiki enablement/initialization and GitHub token permissions; report exact blockers without claiming success or starting unauthorized authentication.

For each release: inspect → identify milestone/Issues → plan → implement → run relevant checks → build APK → verify acceptance → synchronize Project/Wiki → update README/changelog/docs → commit/push → publish release → report results. Passing mandatory gates does not authorize merging into main or publishing: both require explicit owner approval. Never invoke an old release script without inspecting its actions; do not publish merely because Gradle succeeds.

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

## Three-tier verification workflow — owner priority for Gould 0.13+

Verify changes at the smallest tier that genuinely covers them; use
[docs/planning/gould-0.13/VERIFICATION-MATRIX.md](docs/planning/gould-0.13/VERIFICATION-MATRIX.md)
to decide what a change invalidates. Every tier run appends an exact-commit
audit line to `docs/planning/gould-0.13/verification-audit.jsonl`.

- **FAST (everyday):** `scripts/verify-fast.sh [--tests <pattern>]` — affected
  module compilation, JVM unit tests and fast pure-Python static checks. Uses
  the Gradle daemon and incremental builds; no clean build, no emulator.
- **INTEGRATION (feature boundary):** `scripts/verify-integration.sh` — full
  JVM suite, lint, instrumentation compilation and, when
  `INSTRUMENTATION_CLASSES` is set, execution of affected native classes on
  exactly ONE identified disposable emulator (`ro.kernel.qemu=1` guard; never
  the owner's phone; refuses concurrent emulators on this host).
- **RELEASE (final source freeze):** `scripts/verify-release.sh` — the exact
  mandatory policy command (`clean assembleDebug testDebugUnitTest
  assembleDebugAndroidTest lintDebug --no-daemon --max-workers=1`) plus all
  mandatory policy checks. Release-only artifact/signing/upgrade/security
  gates run afterwards per the matrix; publication decisions stay separate.

Rules: no blind retries of failed full suites; no repeating PASSing checks
without a recorded source/environment invalidation reason; diagnose failures
at the smallest reproducing scope; compilation evidence is never claimed as
instrumentation execution; mandatory RELEASE gates are never weakened.

## Legacy Cleanup & Repository Maintenance

Before development or repository maintenance, read [docs/maintenance/LEGACY-CLEANUP.md](docs/maintenance/LEGACY-CLEANUP.md). Follow its documented policy for identifying obsolete code, redundant assets, unused files and technical debt; remove only verified unnecessary components. Preserve user data, migrations, backup formats, release history, legal documentation, signing identity and compatibility-sensitive identifiers. Do not start substantial cleanup during unfinished release verification. Perform safe routine maintenance autonomously, using automated checks and explicitly targeted emulators rather than physical user devices. Document cleanup changes and actual verification in the permanent maintenance record, preserving its history across future Goldberg versions. Do not publish a new application release solely for maintenance without authorization.

## Permanent two-phase Gould workflow — owner approval October28,2026

Exactly two main phases per Gould version: Phase1 Architecture, Review & Planning
produces one complete architecture/compatibility/visual/test/acceptance plan with
no application features; explicit owner approval precedes Phase2 Implementation,
Verification & Release. Phase2 uses one complete development stage, allowing
internal commits/checkpoints/verification batches. Preserve historical stages.
Verify required handoffs/private reference availability in the development
worktree. Maintain the single checklist, issue dashboard/history, Project, PMP
and truthful AI handoff. Existing data/signing/release/phone controls apply.

Gould0.12 Phase2 is explicitly approved from29bb4d1, with mandatory icon addendum.
The owner also explicitly authorizes final main integration and Gould0.12Stable
publication conditionally after ALL mandatory gates and acceptance pass. This is
already the applicable merge/publication authorization; do not ask redundantly.
Failure/security blockers/material scope changes stop affected release actions.
Read docs/planning/gould-0.12/OWNER-AUTHORIZATION.md. No authorization for deleting
protected historical data or modifying independent PMP is implied.


## Gould 0.12 unattended completion — owner decision October29,2026

The owner explicitly waives personal visual sign-off for Gould0.12 Stable and
replaces it with documented actual emulator visual QA/screenshots/layout and
accessibility checks/automated acceptance. Full remaining approved implementation,
verified commits/pushes, main integration and signed Stable publication are
conditionally authorized without another confirmation, **only after ALL mandatory
functional/native/audio/backup/migration/data/signing/upgrade/security/privacy/
licensing/UI/accessibility/release/CI gates genuinely pass**. Failed/unverifiable
gates or unavailable essential credentials/decisions stop release and require a
complete recovery checkpoint. Minor nonfunctional/nonaccessibility/nonsafety
cosmetic issues may be documented for possible0.12.1; never ship critical defects.
Use API36 and the existing compatibility matrix; API37 is deferred. No physical
phone, RC, new stage, protected-data deletion or independent PMP modification.
Read docs/planning/gould-0.12/OWNER-AUTHORIZATION.md. This decision supersedes older
personal visual-approval requirements specifically for Gould0.12.
