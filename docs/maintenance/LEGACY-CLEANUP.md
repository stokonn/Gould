# Legacy Cleanup & Repository Maintenance

Current portability continuation: [October 27 handoff](../ai/GOULD-PORTABILITY-CHECKPOINT.md) and [Linux transfer instructions](PORTABILITY.md). The owner authorized this continuation after the historical stop checkpoint.

This is Goldberg's living, version-independent maintenance record. Read it with root AGENTS.md before development or maintenance. Append dated findings and verification; do not create a competing cleanup policy or a fresh version-specific ledger for every release.

## Maintenance policy

- Finish mandatory release verification and publication before substantial cleanup. Routine verified-safe maintenance is autonomous within owner-authorized scope.
- Prove disuse across Kotlin/Compose calls, resources, manifests, navigation, reflection/dynamic lookup, tests, Gradle, CI, scripts and current documentation. Age or a lint warning alone is insufficient.
- Remove only conclusively unnecessary items. Preserve uncertainty as technical debt. No blanket reset/clean operations, force pushes or history rewriting.
- Preserve user data, drafts, history, repertoire, notes, profile/avatar, preferences, badge dates/showcase, migrations, schema exports, backup formats/fixtures, regression tests, legal notices, signing identity, release tags and published assets.
- Keep com.opus.piano, opus.db, manifest component names and compatibility-sensitive stored keys. Cosmetic naming must not break installed upgrades.
- Use automated checks and explicitly targeted emulators; never use the owner's physical device or require owner acceptance testing.
- Never rename the active checkout while Codex, Gradle, shells or tmux use it. Preserve external personal backups. See the deferred path procedure in NAMING-AUDIT-0.10.1.md.
- Branch deletion requires exact reachability/unique-commit checks, no protected/active worktree or open-PR dependency, and a fresh remote-head check. Use ordinary safe deletion; retain uncertain/unmerged work and all tags.
- Record paths, evidence, compatibility, measured storage and actual verification. Keep historical evidence distinct from current-source manifests.

## Cleanup history

### 2026-10-18 — after the published Goldberg 0.10.1 checkpoint

Release first: v0.10.1 points to `b19b77b81e76feef6baf83e6c4bfa935e7c10bea`; implementation is `56851cb5da30d9e823f6374562f7773e96a35e46`. The normal GitHub Release, downloaded APK/changelog/checksums and remote CI were verified before any cleanup. See [release checkpoint](../RELEASE-CHECKPOINT-0.10.1.md). Maintenance is independently committed under `maintenance/legacy-cleanup-2026-10-18`; locate its commit by subject `chore: establish permanent Goldberg maintenance and remove verified legacy code`.

| Affected path | Change and evidence | Compatibility |
|---|---|---|
| app/src/main/java/com/opus/piano/ui/CustomizePanel.kt | Removed private SectionEditor and its sectionLabel helper. Repository-wide symbol search found only these definitions/their mutual references; no caller or registered entry point exists. | Active Appearance controls, reset, preview and saved preference keys retained. |
| app/src/main/java/com/opus/piano/ui/ProfileScreen.kt | Removed private ActivityChart and imports used only by it. Symbol search found its definition only; active Home activity rendering is elsewhere. | Current Profile statistics/showcase/editor and all journal calculations retained. |
| app/src/main/res/drawable/ic_goldberg_foreground.xml | Removed unused old foreground vector. No Kotlin/XML/manifest/test/script reference or dynamic resource lookup exists; lint also marks it unused. | Current adaptive icon uses ic_goldberg_classic_foreground PNG. |
| app/src/main/res/drawable-night/ic_goldberg_foreground.xml | Removed the identical unused night-qualified copy of that vector. | Current classic light/dark/monochrome artwork is retained and package-verified. |
| scripts/__pycache__/ | Removed only regenerable .pyc files created by verification; ignored by Git. | No source or test removed. |
| .gradle/goldberg101-avds/ | Removed only the inactive, failed disposable AVD created during this session. Actual QEMU processes were checked first. | Active Goldberg092 test AVD, verified release artifacts, Gradle caches and external backups retained. |
| README.md; docs/wiki/User-Guide.md; docs/wiki/Settings-and-Customization.md | Replaced stale current-guide release labels with accurate version-independent descriptions. Historical release evidence remains in its original ledgers. | No feature or provider result invented. |
| AGENTS.md; this file; docs/wiki/Developer-Guide.md | Added the permanent maintenance entry point and workflow. | Existing canonical policy and thin tool-specific references preserved. |

No standalone Kotlin implementation file, migration, schema, backup fixture, legal text, signing material or valuable historical test was deleted. Historical modified-file lists and source hashes may still name removed vectors because they describe old commits; those records must not be rewritten.

## Preserved legacy components

- `app/src/main/res/drawable/ic_goldberg_monochrome.xml` is still used by SmartPracticeService and ReminderScheduler notifications. It is deliberately retained despite its older appearance.
- `app/schemas/` and androidTest schema/backup copies support migration and import regression checks. Their duplication is intentional.
- Internal OpusApplication, OpusDatabase, OpusViewModel and package/resource/storage identities support current code and upgrades. Open Opus is a third-party source name, not obsolete Goldberg branding.
- `docs/icon-0.9.1/`, licensed fonts/notices, old release/verification documents and logs preserve provenance and reproducibility.
- `docs/TESTED-SOURCE-0.10.1.json` describes the immutable release source at its tag. Later maintenance intentionally changes two UI files/removes two resources; do not overwrite release hashes with current-head values.
- Untracked RELEASE_NOTES.md is owner work. `/home/stokonn/Projects/Opus-backup-0101` is external protected backup data and was never changed.

## Repository organization

| Directory/file | Responsibility |
|---|---|
| app/src/main/ | Native Kotlin/Compose application, Android manifest and resources. |
| app/src/test/ | JVM regression tests. |
| app/src/androidTest/ | Emulator instrumentation and compatibility fixtures. |
| app/schemas/ | Historical Room schema exports; preserve permanently. |
| scripts/ | Build, data/localization/policy checks and explicit-emulator helpers. |
| docs/design/ | Approved written specifications; private references stay ignored. |
| docs/wiki/ | Maintainable English official Wiki sources and inspected public captures. |
| docs/maintenance/ | Permanent maintenance policy, dated history and debt. |
| docs/*RELEASE*, docs/*VERIFICATION*, docs/*TESTED-SOURCE* | Release-specific immutable evidence and compatibility reports. |
| .github/ | CI and thin agent integration; root AGENTS.md remains canonical. |
| .gradle/release-artifacts/ | Ignored local preserved published APK/checksums; do not confuse with later build outputs. |

## Branch maintenance

Audit date: 2026-10-18, after publication. Every deleted remote head below was reachable from main with **zero unique commits**, unprotected, absent from open PR heads and unused by all worktrees. Local heads, where present, matched remote heads. Eight remote deletions were pushed atomically; seven matching local branches used `git branch -d`. No force deletion/push, tag deletion or history rewriting occurred.

| Branch | Audited head | Outcome |
|---|---|---|
| feature/goldberg-0.6 | `20cf5eb86711c9126915f682e50311c54b892d90` | Deleted remote; deleted local |
| feature/goldberg-0.6.1 | `29d7d7a764a1a33d9d8d7c7d5e5552c5169ad86c` | Deleted remote; deleted local |
| feature/goldberg-0.7 | `5d85749ac52ed01f44d755a0eacc1ccfc3765844` | Deleted remote; no local branch existed |
| feature/goldberg-0.8 | `5b22fba2fdfd2dd4f4dc3f5f68293f3e7d5e7654` | Deleted remote; deleted local |
| feature/goldberg-0.9 | `4618b50c67e223d7f1b5bf9ee0951f3a79ecfac7` | Deleted remote; deleted local |
| feature/goldberg-0.9.1 | `9eeabd798dcf8c70766a530f32835e499d68da3b` | Deleted remote; deleted local |
| feature/goldberg-0.9.2 | `78fd88fbf4ead8772b87d2b986807a381241b7dd` | Deleted remote; deleted local |
| feature/goldberg-0.10 | `7b5874e8ffd2d01eec8de9ad58cf64e195cf0a5b` | Deleted remote; deleted local |

Preserved: `main` (default/release history), `feature/goldberg-0.10.1` (recent implementation checkpoint), and `maintenance/legacy-cleanup-2026-10-18` (this maintenance/audit branch). No unmerged branch was deleted. No 0.11 development branch was created; issue #31 and milestone 4 remain planning-only. Older branch heads remain reachable through main/release history. Recheck current PRs/worktrees/protection before future deletion; this table is historical evidence, not blanket authorization for a later head.

The earlier docs/BRANCH-CLEANUP-0.10.1.md is the pre-release audit snapshot. Future branch maintenance belongs here.

## Technical debt and retained uncertainties

- Remaining unused-resource warnings need focused reference/provenance checks; do not bulk-delete all lint findings. Preserve localized key parity and dynamic i18n mappings.
- CI reports Node 20/setup-java v4 deprecation and a forthcoming ubuntu-latest image change. Existing pinned actions pass. Upgrade pins/tooling in a separately verified change rather than silently moving dependencies during cleanup.
- AppearanceRules retains older section-order/hide keys for stored-preference compatibility even though the former private SectionEditor had no caller. Do not erase stored values for cosmetic cleanup.
- Historical docs and verified logs retain the old physical checkout path. Current scripts are repo-relative and Gradle display naming is Goldberg; no mass rename is justified. Defer physical directory migration until every dependent session is closed.
- Provider/controller/audience questions remain external legal follow-ups in LEGAL-AUDIT-0.10.1.md. No speculative compliance infrastructure or 0.11 implementation was added.

## Verification

Post-cleanup Gradle build, 75 JVM tests, instrumentation compilation and lint passed using:

```sh
GRADLE_USER_HOME=/tmp/goldberg-gradle-home ./gradlew --offline assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug --no-daemon --max-workers=1
```

Log: /tmp/goldberg-maintenance-checks.log. Post-cleanup instrumentation was separately executed on emulator-5582: **36 passed** (18.198s). Profile and Appearance renders in /tmp/goldberg-maintenance-ui were captured and inspected; active layouts remain intact. APK package/version, trusted signer and canonical icon ZIP/resource checks pass. Local maintenance APK hash: `a4bd2192bae315bae317eb68b6c4ef7533de4af581087033e351a92d51593dfb`; this APK is not uploaded as the release. Lint reports zero errors / 83 warnings, with additional now-unused chart strings retained for a focused future audit. Localization (804 resources), canonical policy/Wiki links, git diff whitespace, unrelated RELEASE_NOTES.md digest and immutable published-APK digest checks pass. The previously published APK and release tag are not replaced by the maintenance build.

## Storage optimization

- Tracked source/resource bytes removed: **6,719 bytes**, measured before/after across the four changed/deleted source paths, before adding maintenance documentation. This is not a claim of net Git-history savings.
- Local regenerable Python bytecode removed: **18,580 bytes**.
- Inactive session-created failed AVD removed: **986,587,136 allocated file bytes** (st_blocks × 512); sparse logical size was not treated as physical savings.

No Git garbage collection, backup deletion or published-artifact removal was performed. Branch deletion does not imply object storage savings because release/main history retains those commits.

## Future maintenance guidance

Read this ledger and AGENTS.md first. Use rg and Git reachability to narrow investigation; consult the preserved-component list before repeating migration/icon/signing audits. Append only new findings, evidence and actual checks. Keep release work and maintenance commits separate, retain uncertain items, and record explicit deferred work. Do not publish a new application release solely for maintenance without authorization.

## 2026-10-27 — pre-0.12 Phase 3 cleanup and consolidation

Purpose: remove only independently verified obsolete development artifacts, retain unique work and make retained commits/evidence discoverable from the primary Opus repository. The owner requested no additional backups and no broad rebranding.

- [x] Rechecked all candidate heads/status/ignored files, current GitHub heads and draft PRs #39/#40; neither PR nor GitHub was modified.
- [x] Removed the clean Stage 1 detached review, CI-only, and Stage 6 docs worktrees with normal `git worktree remove`. All three had zero untracked/ignored files, zero commits outside Stable and no observed process use; their commits remain reachable from `v0.11`.
- [x] Removed 71 source-equivalent Python bytecode files. Unchanged tracked source was checked against each worktree HEAD, regenerated code objects compared without executing modules, and current file-use checks performed. Five unmatched candidates were retained.
- [x] Removed six merged, unused local branches with `git branch -d`: Stage 6 planning, Stage 1/2 planning and Stage 3A/3B/4. All original heads remain reachable. Git refused normal deletion of `ci/stage5-emulator-verification`; it remains untouched. No force deletion, remote deletion, tag change or history rewrite occurred.
- [x] Verified `git fsck --full` (exit 0), original owner-file hashes/current source HEAD, exact unchanged tag/remaining-ref identities, Stable APK hash and existing Phase 2 backup checksums. Seventeen worktrees and 27 local branches remain.
- [x] Added [the retained-work consolidation index](PRE012-CONSOLIDATION.md). Committed content is already accessible through the shared `Opus/.git`; no unfinished code or private evidence was copied into Stable.
- [ ] Switching/updating the active primary checkout is deferred: shells, tmux and agents still use its older Stage 5 state. Catalogue, Stage 7A, Stage 7B, Stage 8, Cloud and C2 also have live process dependencies.
- [ ] Remaining untracked/ignored/private evidence, large AVDs, dumps and dependency caches stay in place where unique-data or recovery questions remain. No extra backup was created and no signing material, user data or standalone PianoApp Opus was touched.

Removed allocated-file footprint: **91,348,992 bytes (87.1 MiB)** across `/home` and tmpfs `/tmp`; this is not a guaranteed Btrfs physical-space delta. No Android build/test, emulator, ADB or physical-device verification was performed for this source-preserving cleanup. No application source, package ID/namespace `com.opus.piano`, database/schema, preferences or release artifact changed. `v0.11` remains `ab7d66ddafe0bcf2f23270682116ee2e02f828eb`; remote main remains the recorded publication receipt `1c943dc925231a3725a0e9d9aa6b3e34d19e445a`. Phase 3 documentation is left reviewable; no commit, push, release or Gould 0.12 implementation occurred.

### 2026-10-27 — Phase 3 continuation, duplicate-dependency verification

This checkpoint supersedes the initial Phase 3 counts above while preserving that earlier audit trail.

- [x] Compared complete dependency trees under `/tmp/goldberg-stage6-handlers`, `/tmp/goldberg-stage6-public-admin-abuse` and `/tmp/goldberg-stage6-security-local` against retained Cloud copies. Relative paths, every regular-file hash/executable mode and symlink targets matched. Rechecked process use and both copies immediately before removing only the six redundant `spikes/stage6-auth-local/node_modules` / `spikes/stage6-schema-pg/node_modules` directories.
- [x] Removed the handlers worktree with ordinary `git worktree remove` once status/untracked/ignored checks were empty and its original HEAD `262847e` was confirmed reachable from Stable.
- [x] Removed local `ci/stage5-emulator-verification` (`67d9cca`) and `spike/goldberg-0.11-stage-6-handlers-local` (`262847e`) using ordinary `git branch -d` from the existing Cloud checkout at current main. The CI branch was merged into that HEAD, resolving the earlier refusal from old Stage 5 without any force flag, history rewrite or source checkout switch. Fresh GitHub branch/PR checks found no PR dependency; no GitHub branch was deleted.
- [x] Preserved eight publication-record files under the public/admin and security temporary worktrees' ignored `.gradle` directories; before/after hashes match. Those worktrees remain because the records may be unique. No cache-name assumption was used to delete them.
- [x] `git fsck --full` passed again; source HEADs/status and all refs other than the two explicitly removed local branches remained unchanged. Sixteen worktrees and 25 local branches remain, with all 33 remote branches and all 14 tags retained.
- [ ] The primary source checkout transition remains deferred: a fresh host check still found 16 live processes with working directories under Opus, including shells, tmux and other agents. No sessions were terminated or modified.

Second-batch removed allocated footprint: **42,057,728 bytes**, entirely under tmpfs `/tmp`; cumulative Phase 3 footprint: **133,406,720 bytes (127.2 MiB)**. These are allocated-file accounting figures, not guaranteed physical-space deltas. No extra backup, package installation, build/test, ADB action, release, rebranding, source implementation, commit or push occurred. Existing backups, primary owner files, standalone PianoApp Opus, `com.opus.piano`, signing and persistent-data identifiers remain protected.

created by stokonn with love <3

### 2026-10-23 — local API M2 disposable resource cleanup

No application/legacy components removed. Scoped API M2 rootless PostgreSQL runner
now records its anonymous volume identities, removes only its UUID-named container
with --volumes, and verifies both container and volume absence. Four initial M2
anonymous volumes were attributed through saved container receipts and adjacent
Podman create events (<20ms, no intervening creates), checked unused, removed
individually and absence-verified; unrelated volumes retained. Evidence is in
`spikes/stage6-api-v1-local/results/m2/volume-recovery-{attribution,cleanup}.json`.
Final source-bound `pg-lg7ei69p` passed19 actual PostgreSQL tests and all focused
regressions; container/volume cleanup verified. No system services, shared/hosted
DBs, user data, migrations, backup formats, assets or signing identity changed.

### 2026-10-24 — private cloud readiness preparation

No cleanup or legacy component removal. New isolated readiness packet only;
historical001–004 SQL,prototype source/evidence,Android/Room/backups/signing and
all existing worktrees preserved. Disposable runner uses network=none/no ports,
immutable cached PG17 image and UUID-owned container/volume removal plus absence
verification.20 SQL checks passed in final source-bound run; no hosted mutations.
See Stage6 PRIVATE-CLOUD-READINESS.md.

### 2026-10-25 — private probe preparation

No application/legacy cleanup. Only new UUID-owned,network=none rootless PG
fixtures were removed with their volumes and absence verified (18 new role/
restore checks). Local workerd instances were loopback-only and disposed.
Existing migrations/ACL/readiness shell/evidence and other worktrees retained.
Accepted suites not rerun. See PRIVATE-DEPLOYMENT-PHASE1-2.md;no hosted mutation.

Initial local workerd generated only .wrangler/cache/cf.json outside the intended
build folder. Audited as new session-generated cache (absent at recovery),copied
byte-identically to private/tmp custody and hash-verified before removing only
that generated file and its now-empty directories. No metadata contents published.
Final runner cf:false/telemetry:false prevents another lookup.

### 2026-10-25 — bounded Stage 8 Discover repair

The isolated `fix/goldberg-0.11-discover-ux` branch consolidates Library/Discover metadata rendering into the shared responsive `EditorialMetadata` component. Only the duplicated active layout is replaced; no historical source evidence, schema, migration, backup format, release artifact or unrelated asset is removed. This is part of the authorized UX repair, not substantial legacy cleanup or release publication. Source-bound verification is maintained in [the Stage 8 Discover checkpoint](../planning/goldberg-0.11/stage-8/discover-repair/CHECKPOINT.md); do not infer native execution from host compilation.

## October 27, 2026 — host-accepted final consolidation retirement

The owner completed the aggregate host gate: 19/19 checks passed at 0ffebeb, 348 JVM tests/zero failures/errors/skips, Android/lint/artifact and synthetic backend/security/encrypted-backup acceptance passed; every one of 9,574 recorded source hashes was independently matched. After the owner confirmed historical sessions/services were saved and closed or relocated, nine worktrees were retired with ordinary Git commands, original commits/owner files/private records preserved by hash. No force, reset, clean, feature implementation or release publication occurred. One obsolete five-tab screenshot workflow was removed; essential quality/backend/emulator gates and byte-exact historical quality pins remain. See [executed record](HOST-ACCEPTED-CLEANUP.md). Seven external worktrees remain for unresolved private/historical artifact disposition; GitHub administrative mutations remain blocked by this executor’s network/capabilities, not by the cleared host test gate.
