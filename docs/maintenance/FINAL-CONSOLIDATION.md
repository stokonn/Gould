# Final Gould consolidation — current maintenance scope

Owner authorization: October 27, 2026, “GOULD 0.12 — FINAL CONSOLIDATION, CLEANUP & REBRANDING”. Continue on `maintenance/gould-portability`; preserve existing work/history, standalone PianoApp Opus and the independent Project Memory Protocol repository. This is the same consolidation task, not a new implementation stage.

- [x] Read the maintenance/portability checkpoints, canonical policy and PMP handoff.
- [x] Inspect and preserve tracked, staged and untracked work; exclude private PMP and unrelated owner files from public commits.
- [x] Commit the prepared branding/portable baseline as `b87d238a51ded7b2b7e33ecbb262827af7a655b9`.
- [x] Discover available execution tools and attempt the previously approved unrestricted Gradle command once. Session policy rejected sandbox escalation; there is no alternate shell executor exposed.
- [x] Recheck the actual GitHub repository, all 33 branch names and open review PRs #39/#40 through the connector.
- [x] Audit all four workflow files and retain essential host/backend/emulator gates. Update current workflow display names without changing file paths, triggers, action pins or permissions.
- [ ] Pass fresh Android build/JVM/instrumentation-compilation/lint, encrypted-backup and backend/PostgreSQL/workerd verification in an unrestricted environment.
- [ ] Pass relevant exact-commit CI and signing/seeded-emulator upgrade acceptance.
- [ ] Retire preparation and obsolete external worktrees after verification, content disposition and actual activity checks.
- [ ] Delete only remotely reverified obsolete branches without unresolved work, PR/release dependencies or important references.
- [ ] Rename `stokonn/Goldberg` to `stokonn/Gould` after project/CI verification, then update live URLs/remotes and verify clone/release/issue continuity.
- [ ] Complete the final independent-copy build acceptance. Structural Git/source portability can be checked separately and must not be presented as this build gate passing.

No Gould 0.12 feature scope is inferred or implemented. Application ID `com.opus.piano`, database/storage/API identifiers, historical Goldberg releases and signing identity remain unchanged. No new application release is authorized. Routine verified cleanup and the repository rename are owner-approved, but destructive operations remain gated on verification and preservation of unique data.

## Workflow audit

| Workflow | Decision and evidence |
| --- | --- |
| `quality.yml` | Retain: Android debug build, JVM suite, instrumentation compilation, lint, migration/backup/localization/policy checks, encrypted-backup tests and historical evidence checks. Read-only contents permission; pinned actions; no signing secrets/publication. Display name becomes Gould. |
| `community-bff.yml` | Retain: disposable PostgreSQL 17, locked npm installs, private-runtime security tests, actual backend/workerd regressions and fail-closed packaging. Read-only permission; no cloud secrets/deployment. Display name becomes Gould. |
| `stage5-emulator.yml` | Retain: still the canonical dispatch-only instrumentation, seeded-upgrade and network/restart acceptance workflow used beyond Stage 5. Preserve its path, full-suite default, SHA binding and emulator guards. Update display name and stale intended-branch comment. |
| `stage5-screenshots.yml` | Retain as historical, dispatch-only tooling pending replacement verification. Its `Home Repertoire Discover Practice Profile` capture loop targets an older layout; current product navigation no longer has a Repertoire tab. Do not dispatch it as current UI acceptance. Historical results remain valid for their original commits. Removal/repair waits for verified current screenshot coverage. |

No workflow was proven safely redundant with all its acceptance evidence replaced. Consequently **zero workflows are removed**, and no write permission, automatic emulator trigger, release publication or production credential is added. YAML parsing, full action-pin checks, local-script reference checks and embedded Bash syntax checks are local validation only; GitHub execution of the new commit remains pending.

## Repository and branch audit

Current repository remains `https://github.com/stokonn/Goldberg`, ID `1421247418`, default branch main. Rename permission is explicit; rename execution waits for the required checks. Keep actual current URLs until the rename succeeds rather than writing links to a repository that does not yet exist.

The connector listed 33 branches, obtained exact compare metadata for 32 heads, confirmed current main is still `1c943dc`, and listed open PRs #39 (privacy/operations proposal) and #40 (catalogue licence proposal). Preserve their source branches. Cached ancestry candidates remain in [REMOTE-BRANCH-REVIEW.md](REMOTE-BRANCH-REVIEW.md); zero unique commits against cached main does not prove absence of a release/documentation dependency. Fresh exact heads, protection, references and required verification are prerequisites for any deletion. **Zero remote branches are deleted in this blocked continuation.** The connector has no repository-rename, branch-delete, workflow-dispatch or unrestricted-shell operation; shell GitHub/DNS access was blocked in the prior verification and is not repeatedly retried.

## Minimum owner action for the execution blocker

Run this reviewed command in a normal terminal on this computer, outside the restricted agent execution sandbox, after installing the toolchain in [PORTABILITY.md](PORTABILITY.md):

```sh
cd "$HOME/Projects/Opus"
python3 scripts/verify_consolidation.py
```

The runner writes private logs/report under ignored `.gradle/final-consolidation/`, records the exact commit/source fingerprint, and stops at a failed prerequisite. It does not publish, rename, delete worktrees/branches, change credentials, or use ADB. It runs the Android host gate, host compatibility/security tests, locked backend dependency installation, disposable local PostgreSQL/workerd checks and debug APK/signature inspection. Install the pinned PostgreSQL image/rootless Podman as described in PORTABILITY.md before running backend checks. Keep the original trusted debug signing key restored securely; do not generate a substitute to make upgrade acceptance pass.

After the host report passes, the agent still needs permitted Git transport/CI access to ordinary-push this maintenance branch, inspect exact-commit CI and execute the separately reviewed seeded-emulator upgrade. Never use a physical phone. Then resume the gated retirement/branch cleanup/repository rename in this same task. Do not start Gould 0.12 features while consolidation acceptance is pending.

## PMP preservation

The existing project UUID is retained. The old `.pmp/state.json` is preserved privately before correcting unsupported Stage 5 phase/status values; old checkpoint history is not relabelled or erased. The obsolete Stage 5 task can then be explicitly archived and this owner-approved maintenance scope activated through the documented CLI. Current task is verifying/blocked; historical 0.11 results remain historical and no native test evidence is fabricated.

The standalone `~/Projects/project-memory-protocol` repository remains separate and unmodified. PMP tool version is `1.0.0a1`, requires Python 3.11+, PyYAML 6.x and jsonschema 4.x, and can be installed from its reviewed wheel/source. Private checkpoint history lives outside Opus in the user state directory and must be transferred securely separately if historical local recovery is required. See the current AI handoff for actual readback results.

created by stokonn with love <3
