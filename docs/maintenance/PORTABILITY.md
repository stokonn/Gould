# Gould development transfer

Current status: canonical development checkout `~/Projects/Opus` is on main in renamed repository `stokonn/Gould`. The integrated source passed 19 host checks/348 JVM tests and exact-source maintenance CI; remote clone verification passed. Full Actions registration cleanup and backed-up prerelease removal remain mandatory. Read [the main handoff](../ai/GOULD-MAIN-MAINTENANCE.md); no Gould 0.12 features have started.

## Development baseline and layout

The sole intended development checkout is `~/Projects/Opus`. Its canonical branch is `main`, integrating the verified maintenance history based on publication receipt `1c943dc925231a3725a0e9d9aa6b3e34d19e445a`. The verified Stable tag remains `v0.11` at `ab7d66ddafe0bcf2f23270682116ee2e02f828eb`. The GitHub repository is `https://github.com/stokonn/Gould.git`.

| Component | Classification | Treatment |
| --- | --- | --- |
| `app/src/main`, Android resources/assets, Gradle wrapper/configuration | REQUIRED FOR DEVELOPMENT | Current main, plus the previously prepared cosmetic Gould changes. |
| `app/src/test`, `app/src/androidTest`, `app/src/canonicalIntegration` | REQUIRED FOR DEVELOPMENT | Retain regression and optional HTTP integration tests. |
| `app/schemas`, migration implementations, backup readers/fixtures | REQUIRED FOR COMPATIBILITY | Retain schema 1–10 compatibility and backup formats 1–5. |
| `services/community-bff`, `contracts`, `scripts/stage6` | REQUIRED FOR DEVELOPMENT | Backend composition, wire contracts, operational tooling and tests. |
| `spikes/stage6-{schema-pg,auth-local,handlers-local,public-admin-abuse,api-v1-local,security-local}` | REQUIRED FOR DEVELOPMENT | Despite their historical directory names, current backend imports/test runners use these modules. Keep their SQL migrations, source and lockfiles. |
| `services/community-bff/deployment` | OPTIONAL DEVELOPMENT TOOLING | Preserve packaging configuration; do not deploy during transfer. |
| `docs/wiki`, `docs/planning`, `AGENTS.md`, current maintenance documents | REQUIRED FOR DEVELOPMENT | Preserve policy, architecture, verified limitations and the single-stage 0.12 plan. |
| Existing Git branches/tags/stashes | HISTORICAL ONLY / development history | Keep refs and objects. Do not merge unfinished branches to consolidate directories. |
| Signing key, account tokens, OAuth/backend secrets, private PMP | REQUIRED PRIVATE CONFIGURATION | Transfer securely and separately; never commit credentials. |
| Gradle/npm caches, `node_modules`, ordinary build outputs, Python bytecode | REGENERABLE | Install/resolve again. Do not copy SDKs, caches or AVDs. |
| Historical logs/captures and APKs already tracked by Git | HISTORICAL ONLY | Retained to preserve existing source-bound evidence; no new worktree archives imported. |
| Unique ignored/untracked files in external worktrees | UNRESOLVED | Preserve at their original locations until individually reviewed. Not required to build current main merely because they exist. |

Current main already contains the integrated Android application and backend modules. No additional unfinished branch was identified as approved Gould 0.12 scope. No experimental code was merged. Historical worktree paths in evidence documents are provenance, not installation paths.

## Toolchain

Versions below come from repository build files and the observed host, not a newly validated installation:

- Linux x86_64; Git; Bash; Python 3 (host observed 3.14.8); GNU tar; GnuPG for encrypted backup tests. Pillow is needed by optional artwork/APK inspection scripts.
- JDK 17 is the CI configuration. Existing host has OpenJDK 25.0.4.1; the owner-executed 19-check host gate passed with the installed host toolchain. Prefer JDK 17 for the documented CI setup.
- Gradle **9.3.1**, supplied by `./gradlew`; Android Gradle plugin **9.1.0**; Compose Kotlin plugin **2.2.10**; KSP **2.3.12**. Do not install a system Gradle in place of the wrapper.
- Android SDK `platform-tools`, `platforms;android-36`, `build-tools;36.0.0`. Minimum app API 26; target/compile API 36. An emulator and its system image are optional, separate installations.
- Node **22.23.1** and npm **10.9.8** were observed on the current host. Package lockfiles pin JS dependencies. PostgreSQL **17** is the synthetic backend test target; rootless Podman is used by the local runner.
- Backend pins include `pg` 8.23.1, `jose` 6.2.13, `esbuild` 0.28.2 and `miniflare` 5.20261019.2-beta. Optional deployment pins `cf` 1.0.0-beta.21 and Wrangler 4.152.0.

Install Git, JDK 17, Bash, Python, GnuPG, Node/npm and optional rootless Podman using the destination Linux distribution's supported packages or the tools' official installers. Install Android command-line tools separately and accept SDK licences interactively. With `sdkmanager` available:

```sh
export ANDROID_HOME="$HOME/Android/Sdk"
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"
sdkmanager 'platform-tools' 'platforms;android-36' 'build-tools;36.0.0'
sdkmanager --licenses
cd "$HOME/Projects/Opus"
printf 'sdk.dir=%s\n' "$ANDROID_HOME" > local.properties
./gradlew clean assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug --no-daemon --max-workers=1
```

Set `JAVA_HOME` to the installed JDK 17 location. The first build needs network access to the Gradle distribution, Google Maven, Maven Central and plugin repositories. Do not add `--offline` until dependencies have actually resolved on the new computer. SDK paths belong in ignored `local.properties` or environment variables, never tracked machine paths.

For backend development, all source imports resolve within Opus:

```sh
npm ci --prefix spikes/stage6-schema-pg --ignore-scripts --no-audit --no-fund
npm ci --prefix spikes/stage6-auth-local --ignore-scripts --no-audit --no-fund
npm ci --prefix services/community-bff --ignore-scripts --no-audit --no-fund
npm test --prefix services/community-bff
podman pull docker.io/library/postgres:17@sha256:3518071376d0b6156b02e1db2747a78511b5b9dc3763cc79c506ebe2dc2ee71c
podman tag docker.io/library/postgres@sha256:3518071376d0b6156b02e1db2747a78511b5b9dc3763cc79c506ebe2dc2ee71c docker.io/library/postgres:17
python3 services/community-bff/run-local.py
python3 services/community-bff/run-local.py --client-worktree "$PWD" --client-only
node services/community-bff/build-local.mjs
```

The local runner uses a disposable loopback-only synthetic PostgreSQL container and removes that container/its volumes. These commands do not configure or migrate a hosted database. Optional deployment dependencies can be installed with `npm ci --prefix services/community-bff/deployment`; building/deploying live services is outside this transfer task. `scripts/community-local/verify-jvm.py` is a historical direct-cache compiler helper; use Gradle tests for normal development instead.

## Private configuration and upgrade compatibility

Android builds do not require backend production credentials. Google identity and community writes retain their existing disabled/configuration gates; do not invent substitute configuration. See `docs/GOOGLE-IDENTITY-SETUP.md`, `docs/SIGNING.md` and the latest Stage 6 runtime documents for existing external setup.

Securely transfer the existing trusted Android signing keystore and its passwords, any required private signing init scripts, private PMP (`.pmp/`, `.pmp.yml`), necessary account configuration and server secrets separately in an encrypted container. Preserve owner-only permissions and restore credentials outside Git. Do not paste values into documentation, shell history or command output. Authenticate GitHub again on the destination computer; host credential stores are not part of the repository.

The existing app build uses the Android debug signing identity for debug APKs. A new machine's automatically generated debug key will differ: restore the original trusted debug keystore securely before using a debug build as an in-place upgrade. Production signing also requires the original private signing configuration; never rotate or generate a replacement identity as a transfer workaround. This task did not copy signing keys into the project or configure a new release build.

Preserved identifiers: `com.opus.piano`, `opus.db`, schema and backup implementations, version code 13, existing stored keys. Trusted certificate SHA-256: `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`. Original Stable APK SHA-256: `290a15e7b15da701b8f0a40e98bf8b7c1f7cc00ce487504dcb6d4ab2e11b40d6`. New certificate comparison and a seeded emulator upgrade remain necessary before distributing a newly built APK; static unchanged-code checks are not a fresh upgrade test. Never uninstall to bypass a signature mismatch; never use the owner's physical phone.

## USB transfer procedure

Complete the pending acceptance checks before treating this as a verified development handoff. Close project sessions and inspect `git status --short` first. Keep `.git`, including local branches, tags, stashes and objects; a source-only ZIP or GitHub download is insufficient. A tar archive works on FAT/exFAT USB filesystems while preserving Linux permissions and symlinks internally. FAT32 cannot hold an individual file of 4 GiB or larger.

Set the USB mount path to the actual mounted drive. This example excludes machine state, dependency caches, private local configuration and external worktree registration metadata; it does not delete them from the original project:

```sh
USB='/run/media/your-user/your-usb'
tar -C "$HOME/Projects" \
  --exclude='Opus/.git/worktrees' \
  --exclude='Opus/.git/portability-audit' \
  --exclude='Opus/.gradle' --exclude='Opus/.kotlin' \
  --exclude='Opus/app/build' --exclude='node_modules' --exclude='__pycache__' \
  --exclude='Opus/.idea' --exclude='Opus/local.properties' \
  --exclude='Opus/.pmp' --exclude='Opus/.pmp.yml' \
  --exclude='Opus/docs/design/goldberg-0.10.1/references' \
  -czf "$USB/Opus.tar.gz" Opus
(cd "$USB" && sha256sum Opus.tar.gz > Opus.tar.gz.sha256)
sync
```

Review other ignored/untracked files before transferring them: exclusion patterns are not a secret scanner. Keep separately encrypted private files secure. On the destination:

```sh
USB='/run/media/your-user/your-usb'
(cd "$USB" && sha256sum -c Opus.tar.gz.sha256)
mkdir -p "$HOME/Projects"
# Extract only when Projects/Opus does not already exist; do not overwrite another checkout.
test ! -e "$HOME/Projects/Opus" && tar -xzf "$USB/Opus.tar.gz" -C "$HOME/Projects"
cd "$HOME/Projects/Opus"
git rev-parse --git-common-dir
git status --short
git fsck --full
git remote -v
git branch
git tag
git worktree list
```

The copied repository should report `.git` as its common directory and only itself as a worktree. No alternate Git object directory, Git submodule or LFS dependency was found in the audited current-main baseline. Omitting `.git/worktrees` from a copy preserves all ordinary branches/tags/objects and does not retire or alter the original host's worktrees.

## Acceptance checklist

- [x] Preserve primary owner modifications and overlapping untracked originals in named Git stashes.
- [x] Transition Opus from Stage 5 to the verified current-main baseline on a local maintenance branch.
- [x] Transfer prepared rebranding and planning changes with per-file hash checks; preserve the original preparation worktree.
- [x] Verify backend source/migrations/contracts are already present; integrate no unfinished branch.
- [x] Remove host-specific SDK paths from executable verification helpers and support backend/client testing in this checkout.
- [x] Check full repository integrity, branches/tags, object independence and compatibility-sensitive source preservation.
- [x] Remove the five owner-approved historical emulator/image directories after recording their inventory.
- [ ] Complete an independent-copy Android build using the documented toolchain and freshly resolved dependencies.
- [x] Pass encrypted backup tests on the host: all six passed in the accepted 19-check report.
- [x] Verify the consolidated host-built debug APK and trusted signing identity; no physical-phone tests.
- [x] Retire Gould-Preparation after committed integration, accepted host verification and preservation of its originals.
- [ ] Resolve external worktrees' unique/private content and activity before any ordinary safe removal.

created by stokonn with love <3

## Final verification and PMP continuation

The owner has approved later verified remote cleanup and the repository rename, gated on project/CI acceptance. Current outcome and the one-command host verification request are in [FINAL-CONSOLIDATION.md](FINAL-CONSOLIDATION.md); current private PMP recovery/readback is summarized in [the AI handoff](../ai/GOULD-FINAL-CONSOLIDATION.md). Historical restrictions above describe the earlier snapshot and do not revoke the newer owner authorization.

Install PMP 1.0.0a1 from the reviewed standalone Project Memory Protocol source or its verified `project_memory_protocol-1.0.0a1-py3-none-any.whl`, with Python 3.11+, PyYAML 6.x and jsonschema 4.x. Keep that standalone project separate and unmodified. Restore `.pmp.yml` and `.pmp/` securely with owner-only permissions. Preserve historical private checkpoint storage from `${XDG_STATE_HOME:-$HOME/.local/state}/pmp/` separately in encrypted custody if needed. A moved checkout gets a new path-derived worktree identity: retain old history under its original identity; do not relabel it as freshly verified. The restored project state and tracked handoff describe the current task even without importing old private checkpoint records. Run `pmp --project "$HOME/Projects/Opus" check`, `status`, `resume`, and `verify` after restoration. Never run `pmp init` to replace the existing project UUID.
