> Host acceptance is now validated: all 19 checks passed at `0ffebeb`, including 348 JVM tests and local backend/encrypted-backup checks. Nine obsolete worktrees, including Gould-Preparation, have been retired with preservation verified. Current blocker is GitHub administration access, not the build. Read [HOST-ACCEPTED-CLEANUP.md](../maintenance/HOST-ACCEPTED-CLEANUP.md) first; older entries below are historical. Actual repository remains `stokonn/Goldberg` until the approved rename is confirmed.

> Current Gould maintenance continuation (October 27, 2026): prepared branding and the complete current-main baseline are committed on `maintenance/gould-portability` (`b87d238` plus subsequent maintenance commits). Goldberg 0.11 Stable remains published; no 0.12 features have started. Actual repository: `stokonn/Goldberg`; the approved Gould rename awaits project/CI verification. Read [FINAL-CONSOLIDATION.md](../maintenance/FINAL-CONSOLIDATION.md) and [the current handoff](GOULD-FINAL-CONSOLIDATION.md) first. Historical entries below retain their original evidence.

# Gould portability consolidation checkpoint — October 27, 2026

## Current outcome

**Source consolidation is implemented; full portability acceptance is blocked.** The intended sole development checkout is `/home/stokonn/Projects/Opus`. It now uses local branch `maintenance/gould-portability`, HEAD `1c943dc925231a3725a0e9d9aa6b3e34d19e445a`, with uncommitted preparation and portability changes. No commit, push, remote-main merge, release or 0.12 feature implementation occurred in this continuation.

Read [PORTABILITY.md](../maintenance/PORTABILITY.md) for component classifications, exact toolchain declarations, backend installation, private configuration, USB copy/extraction and destination setup. [The earlier stop checkpoint](GOULD-MAINTENANCE-CHECKPOINT.md) is historical; the owner subsequently authorized this continuation.

## Preserved and integrated

- The owner confirmed all other Opus/Gould-Preparation agents, editors, builds and shells had saved and closed or relocated. Host-process visibility is restricted here: `ps` sees only sandbox processes and the host tmux socket returns `Operation not permitted`. This confirmation does not certify activity in other historical worktrees.
- Preserved the original modified maintenance ledger in named Git stash `preserve original Opus maintenance before Gould consolidation`.
- Preserved 23 overlapping owner files in named Git stash `preserve original Opus untracked files overlapping current main`. Each original matched current main byte for byte. Other owner files stayed in place, including `RELEASE_NOTES.md`, `START_HERE_PROMPT.txt`, private PMP and stage references.
- Switched Opus from the historical Stage 5 branch to a new local maintenance branch based on the verified publication receipt. Local main was not changed. No additional worktree or preparation directory was created.
- Transferred 17 modified tracked preparation paths and reviewed/copied four untracked preparation documents with per-file SHA-256 checks. A three-way comparison proved the preparation maintenance ledger also retained the original Opus maintenance additions.
- All integrated Android/community/backend code, SQL migrations, contracts, regression tests, resources and current documentation already exist in current main. No unfinished historical branch was merged. The application ID, namespace, database, migration/backup implementations, stored identifiers and app build/signing configuration remain unchanged from current main. Prepared cosmetic Gould labels/export filenames are retained.
- Replaced host-specific SDK paths in verification helpers with `ANDROID_HOME` / `ANDROID_SDK_ROOT` / user-home fallbacks. The backend/client runner resolves a relative client path and uses the destination SDK configuration; its README now uses the same checkout rather than a `/tmp` worktree.

## Cleanup actually executed

The owner explicitly approved deleting five historical emulator/image directories under Opus `.gradle`: `stage3a-avds`, `stage3b-avds`, `stage4-final-avds`, `stage4-upgrade-avds`, `stage3a-android26`. An inventory was recorded before removal. Their allocated footprint was **20,872,511,488 bytes**. Ordinary generated `app/build`, `.kotlin`, `.gradle/9.3.1`, `.gradle/buildOutputCleanup` and `.gradle/vcs-1` were removed separately.

Opus fell from about **21 GiB to 1.2 GiB** (approximately 1.19 billion allocated bytes before final small documentation additions). The remaining ignored `.gradle` records include private/historical evidence and published artifacts, so were not blanket-deleted. The documented USB archive excludes `.gradle` entirely. Existing backups, original release APKs, signing keys, private PMP, standalone PianoApp Opus and all external worktree contents were not deleted or moved.

## Verification actually executed

- Original and independent-copy `git fsck --full`: PASS, with dangling objects reported and preserved; no missing/corrupt object errors.
- Git common directory is the real `Opus/.git`, with no external object alternates, partial-clone setting, required Git submodule or detected LFS dependency in the audited baseline. All ordinary branch/tag refs are self-contained. Current totals: **27 local branches, 33 cached remote branches, 14 tags, two new preservation stashes**. Stable `v0.11` remains `ab7d66ddafe0bcf2f23270682116ee2e02f828eb`.
- GitHub connector confirmed public repository `stokonn/Goldberg`, default branch main, and Issue #31 closed/completed with final Stable receipt `1c943dc`. Git CLI DNS access failed, so no fresh remote-head fetch/deletion classification is claimed. No Issue/Project/Wiki was changed for this local maintenance operation.
- Primary and independent copy: migration DDL 1–9 → 10, historical backup formats 1–3, localization **1103 EN/CZ resources**, history paging and canonical agent policy PASS.
- Primary: all three v5 fixture validation functions PASS without rewriting the historical fixture-size report.
- Primary: five private-runtime guard tests PASS; historical Stage 6 source/raw evidence verifier PASS. Its printed historical counts are historical evidence, not new JVM execution.
- Primary: encrypted backup store suite ran **six tests: three passed, three errors** at the GPG encryption step. An isolated synthetic diagnostic confirmed GPG could not start/connect to its agent. No backup implementation was changed to hide this failure.
- Changed Python source AST parsing, changed shell `bash -n`, and `git diff --check`: PASS.
- `git diff origin/main` confirmed no changes to Room schemas, data repositories, services, preferences or app build/signing configuration.
- Original preparation acceptance remains **348 JVM tests**, debug/test APK compilation and lint zero errors, as documented before this session. These results were not relabelled as independent Opus results.

## Independent-copy portability test

An explicitly authorized temporary test copy was created outside the Git worktree structure, with **9,588 initial source/document files**, plus its own complete `.git` copy. No SDK, dependency cache, AVD, build output, private PMP or external linked-worktree registrations were copied. Every copied project file matched its captured source SHA-256. The copy opened with common directory `.git`, listed only itself as a worktree, retained all local branches/tags and passed Git integrity plus the host checks listed above.

With a completely fresh `GRADLE_USER_HOME`, the documented Android build attempted to fetch Gradle 9.3.1 and failed with **`java.net.SocketException: Operation not permitted`**. A primary-checkout attempt using the installed wrapper also failed before compilation: `Could not determine a usable wildcard IP`, caused by denied socket creation while inspecting network interfaces. No Android compile, fresh JVM suite, APK, instrumentation execution or lint result was produced in this continuation.

With a fresh npm cache and lockfile installation, npm failed (`Exit handler never called`); its diagnostic log contains **`EAI_AGAIN`**, consistent with the observed DNS restriction. Backend dependency installation and PostgreSQL/workerd integration were therefore not accepted as passed. The current host has JDK 25, not the documented CI JDK 17; a destination-toolchain build remains necessary.

The temporary test copy was removed after checking its source/ref manifests and confirming all additional files were generated test artifacts. Final maintenance documents were also copied and byte-checked before removal. Its receipts and manifests are retained privately under `.git/portability-audit/`; that directory also holds owner-file hashes, inventories, remote branch ancestry and diagnostic logs. It is excluded from the documented public/source transfer path.

## Worktree disposition

**No registered worktree was removed.** All unique commits remain referenced in the shared Git repository. The external worktrees are not needed for current-main source imports or normal Gradle/backend builds, but retain unresolved local evidence/private state or require fresh activity review:

| Worktree | Reason retained |
| --- | --- |
| `Gould-Preparation` | Original verified preparation and APK preserved until independent Opus Android acceptance passes. |
| `Goldberg-011-Catalogue`, `Goldberg-011-Discover` | Historical acceptance; Catalogue is clean, Discover contains ignored outputs; final retirement waits for portable acceptance/activity review. |
| `Goldberg-Stage6-API` | 63 untracked files and ignored state require disposition. |
| `Goldberg-Stage6-Cloud` | Ignored runtime/delivery state and nested registered RC worktree. |
| nested `.gradle/goldberg-0.11-rc` | Historical RC commit/evidence; preserve immutable release identity. |
| `Goldberg-Stage7A` | Unique acceptance commit and six untracked evidence files. |
| `Goldberg-Stage7B` | 2,090 untracked files and substantial ignored evidence; prior service activity requires recheck. |
| `Goldberg-Stage8` | Unique historical commit, private PMP and ignored incident/diagnostic evidence. |
| `goldberg-c2-spike` | Two commits outside current main and private runtime state; prior workerd activity unresolved. |
| `goldberg-stage7-docs` | Unique planning commit. |
| `/tmp/goldberg-stage6-android-local` | Historical ignored evidence/generated outputs. |
| `/tmp/goldberg-stage6-public-admin-abuse` | Ignored publication records. |
| `/tmp/goldberg-stage6-security-local` | Ignored publication records. |
| `/tmp/goldberg-stage8-canonical-integration` | Unique acceptance commit and ignored outputs. |
| `/tmp/opencode/c6-pg` | Six commits outside current main and ignored scratch state. |

The original Opus repository still registers 17 worktrees. The independently tested transfer excludes `.git/worktrees` from its own Git copy to avoid obsolete host-specific back-pointers; it does not alter registrations on this computer. A literal unfiltered copy of the original directory would retain those registrations. See the documented archive command. [Remote branch candidates](../maintenance/REMOTE-BRANCH-REVIEW.md) are for later review only; no deletion is approved or executed.

## Exact remaining work

1. Run the documented clean Android build/JVM/instrumentation-compilation/lint command on a Linux host with working sockets/network, JDK 17 and SDK 36. Also rerun the six encrypted-backup tests with functional GPG agent support.
2. Resolve locked npm dependencies from a fresh cache and execute backend synthetic PostgreSQL/workerd checks; no hosted changes are needed for local tests.
3. Restore the original signing identity securely, inspect the newly built APK and certificate, and perform any required seeded emulator upgrade. Do not claim the earlier 348-test preparation run verifies a new environment.
4. After these pass, compare preparation source/docs again, preserve its private verification receipt intentionally, and retire it only through ordinary safe worktree removal. Do not force-remove a dirty worktree.
5. Review each external worktree's unique/private files and actual host activity before ordinary removal. Owner approval is required for uncertain/unique data. Do not merge experiments, delete GitHub branches, rename the repository, publish, or start 0.12 features to complete this maintenance task.
6. Review the uncommitted diff and create a local maintenance commit when appropriate. Keep unrelated owner files/private configuration out of public Git. A main merge or publication still requires explicit owner approval.

created by stokonn with love <3
