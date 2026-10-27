> Current continuation: [validated host acceptance, executed retirement and remaining administration](HOST-ACCEPTED-CLEANUP.md). Earlier blocked-state descriptions and inventories below retain their historical meaning.

# Pre-0.12 cleanup and consolidation — October 27, 2026

This is an index of retained work, not approval to merge experimental code. The permanent policy and maintenance history remain in [LEGACY-CLEANUP.md](LEGACY-CLEANUP.md).

## Checklist and executed scope

- [x] Recheck Stable, current remote main, local refs, worktrees and open draft PRs.
- [x] Remove four clean, merged, inactive worktrees using ordinary `git worktree remove` only. The handlers checkout was removed after its only ignored contents were proved to be duplicate dependency trees and removed separately; all four had no local files remaining at worktree removal.
- [x] Remove 71 untracked Python bytecode files only after their code objects matched compilation of unchanged tracked source and current process checks passed. Five unmatched bytecode candidates were retained.
- [x] Remove eight obsolete local branches with ordinary `git branch -d`; keep remote refs and tags unchanged.
- [x] Remove six ignored dependency directories in inactive temporary worktrees after every entry matched the retained Cloud copies (relative paths, file hashes, executable permissions and symlink targets). No dependency package was installed or downloaded.
- [x] Verify repository object integrity, original owner-file hashes, source HEAD, Stable APK, tags and retained refs. Existing Phase 2 backup checksums pass; no backup was created or changed.
- [x] Centralize retained commit/evidence locations in this index without copying private data or merging unfinished code.
- [ ] Switch the primary checkout to current main: deferred because live shells, tmux and agents use its older Stage 5 checkout. No branch switch, update, commit or push was performed.
- [ ] Consolidate private/untracked evidence: deferred; preserve originals in place. The owner requested no additional backup work. This does not authorize discarding unique files.
- [ ] Remove remaining worktrees/large artifacts: deferred where activity, ignored contents, unique commits or recovery uncertainty remain.

## Authoritative history under Opus

All retained Goldberg worktrees share `/home/stokonn/Projects/Opus/.git`. Their committed work is already available from the primary repository without copying entire checkouts.

| Purpose | Retained commit | Location / treatment |
|---|---|---|
| Goldberg 0.11 Stable | `ab7d66ddafe0bcf2f23270682116ee2e02f828eb` | Annotated `v0.11`; immutable release baseline. |
| Current remote main / final publication receipt | `1c943dc925231a3725a0e9d9aa6b3e34d19e445a` | `origin/main`; documentation-only commit after Stable. Use freshly verified current main for future Gould development. |
| Primary checkout's current source | `32cf5d93f19faef56fcb3bf7d7108088f66a1706` | `feature/goldberg-0.11-stage-5`; active sessions prevent safe switching now. |
| Stage 6 local API | `006fa577ff4faef8ee7a25daf851620feb5bc3a8` | `/home/stokonn/Projects/Goldberg-Stage6-API`; preserve 63 untracked results. |
| Final Stable delivery | `1c943dc925231a3725a0e9d9aa6b3e34d19e445a` | `/home/stokonn/Projects/Goldberg-Stage6-Cloud`; active, contains release delivery and nested RC worktree. |
| Stage 7A acceptance | `1b97281ac639a045151ab4dd01e28c499c2e4ca7` | `/home/stokonn/Projects/Goldberg-Stage7A`; unique acceptance record and six untracked evidence files; active. |
| Stage 7B | `4c6a91d600ad18455a280f941f89740694d5afa0` | `/home/stokonn/Projects/Goldberg-Stage7B`; preserve 2,090 untracked files and ignored evidence; active ADB-server working directory observed. |
| Stage 8 security/evidence record | `7668cd655e7f3ded1706fcec1e8555243e0a4dd0` | `/home/stokonn/Projects/Goldberg-Stage8`; preserve incident/diagnostic originals and private memory; active. |
| Catalogue / Discover acceptance | `9b8266e14a07bed4a84385c1068ded7b3bb1e0b1` | `/home/stokonn/Projects/Goldberg-011-Catalogue` is active; `/home/stokonn/Projects/Goldberg-011-Discover` retains ignored outputs requiring review. |
| Canonical integration receipt | `9260b094292db812dcf99a8aca98fb1648f3dd44` | `/tmp/goldberg-stage8-canonical-integration`; historical acceptance record retained. |
| Historical RC receipt | `f248f45e9480c8b01b7c8a8beaccde4421c7e0c0` | `/home/stokonn/Projects/Goldberg-Stage6-Cloud/.gradle/goldberg-0.11-rc`; nested worktree, not disposable cache. |
| C2 experiment | `7bc6976576353a244533792128e18b9343dc6c7c` | `/home/stokonn/Projects/goldberg-c2-spike`; unique work, private runtime state and running workerd process retained. |
| PostgreSQL contract/concurrency experiment | `c21e550ceb5ec7a42317a4f2fe02e6d1225a2cab` | `/tmp/opencode/c6-pg`; six commits outside Stable and ignored scratch state retained. |
| Stage 7 planning | `467ad29422e2af09fe6f689fd61bf8121b298674` | `/home/stokonn/Projects/goldberg-stage7-docs`; unique planning record retained. |

The independent `/home/stokonn/Projects/Opus-backup-0101` checkout is protected: 19 modified tracked files and 12 individual untracked files remain there. It is not replaced by the primary repository's history or the Phase 2 bundle.

For inspection from Opus, use read-only `git show <commit>:<path>` or `git log <commit>`. Do not cherry-pick entire experimental branches, overwrite existing files, or publish private/untracked evidence merely to consolidate directories.

## Cleanup outcome and limits

Removed worktrees:

- `/tmp/goldberg-stage1-review-c3d5e03` — detached `c3d5e03`.
- `/tmp/opencode/goldberg-ci-only` — `67d9cca`; the first branch deletion attempt from Stage 5 refused. A later ordinary `git branch -d` from the existing current-main Cloud checkout succeeded after fresh ancestry/reference checks; no force option or checkout switch was used.
- `/home/stokonn/Projects/goldberg-stage6-docs` — `b3edc2a`.
- `/tmp/goldberg-stage6-handlers` — `262847e`; its dependency copies were verified redundant against retained Cloud copies before removal.

Removed local branches only: `docs/goldberg-0.11-stage-6-planning`, `planning/goldberg-0.11-stage-1`, `planning/goldberg-0.11-stage-2`, `feature/goldberg-0.11-stage-3a`, `feature/goldberg-0.11-stage-3b`, `feature/goldberg-0.11-stage-4`, `ci/stage5-emulator-verification`, `spike/goldberg-0.11-stage-6-handlers-local`. Each deleted head remains reachable from `v0.11`. No remote branch or tag was removed.

Removed allocated-file footprint: **133,406,720 bytes (127.2 MiB)** across both cleanup batches, including worktree contents, bytecode and duplicate dependency trees. The second batch removed 42,057,728 bytes of allocated footprint, entirely under tmpfs `/tmp`. This is not a guaranteed physical-free-space delta; hard links, filesystem sharing and concurrent activity may affect actual recovery.

Retained: 16 worktrees, 25 local branches, all 33 remote branches, all 14 tags, all unique/uncommitted/private work, existing backups, release artifacts and the trusted 0.10.1 rollback APK. No AVD, QEMU dump, shared Gradle/npm cache, signing credential, production resource or personal project was deleted. Standalone PianoApp Opus was excluded entirely.

The remaining `/tmp/goldberg-stage6-public-admin-abuse/.gradle` and `/tmp/goldberg-stage6-security-local/.gradle` directories contain publication records, not just regenerable caches. Their eight files were hash-checked unchanged, and the worktrees were retained. The two duplicate dependency directories removed from each are `spikes/stage6-auth-local/node_modules` and `spikes/stage6-schema-pg/node_modules`; identical copies remain under `/home/stokonn/Projects/Goldberg-Stage6-Cloud`. A later reuse of these historical temporary checkouts may require restoring dependency resolution from those existing copies or the tracked dependency declarations.

`git fsck --full` passed; existing backup `SHA256SUMS` validation passed. No Android build, instrumentation, ADB command or physical-device operation was run. App source, `com.opus.piano`, database and persistent identifiers are unchanged. The 0.11 Stable APK still hashes to `290a15e7b15da701b8f0a40e98bf8b7c1f7cc00ce487504dcb6d4ab2e11b40d6`.

The primary checkout is not yet ready as the Gould development baseline: it is deliberately still on the active Stage 5 branch, with pre-existing owner files preserved. Close or relocate dependent sessions before a separately controlled checkout transition. No Gould 0.12 implementation or rebranding started.

created by stokonn with love <3

## Subsequent portability consolidation — October 27, 2026

The owner resumed consolidation and confirmed that other Opus/preparation sessions had saved and closed or relocated. Opus now uses local branch `maintenance/gould-portability` at current-main baseline `1c943dc`, with the preparation changes integrated and uncommitted. Original maintenance changes and overlapping untracked originals were retained in two named Git stashes; all overlapping files matched current main exactly. No experimental branch was merged.

The owner explicitly approved deletion of five historical emulator/image directories under Opus `.gradle`: `stage3a-avds`, `stage3b-avds`, `stage4-final-avds`, `stage4-upgrade-avds`, `stage3a-android26`. Their recorded allocated footprint was 20,872,511,488 bytes. Ordinary generated Gradle/compiler/build outputs were also removed. Other ignored evidence, protected backups, private PMP and signing material were preserved.

Source/Git and host data checks passed in an independent copy without linked worktree registrations or dependency caches. A fresh Android build was blocked by denied socket creation; fresh npm installation encountered DNS failure. Therefore preparation and all other registered worktrees remain retained pending final acceptance/content/activity review. See [current checkpoint](../ai/GOULD-PORTABILITY-CHECKPOINT.md), [transfer procedure](PORTABILITY.md), and [remote branch candidates](REMOTE-BRANCH-REVIEW.md). Earlier deferred-state descriptions remain historical.

## Owner stop checkpoint — October 27, 2026

The approved preparation worktree now adds one worktree and one maintenance branch: current totals are **17 worktrees / 26 local branches / 33 remote branches / 14 tags**. The earlier 16/25 figures describe the completed cleanup snapshot. Preparation is paused; see [the durable checkpoint](../ai/GOULD-MAINTENANCE-CHECKPOINT.md) before any continuation. No current session was terminated and no 0.12 feature implementation began.
