> October28 live/PMP readback: owner-approved normal main integration is recorded in canonical PMP sequence29; local/remote main ef82898 and main CI40596043801/40596043941/40596043982 all successful. The older pending-merge statements below are retained history. Defined independent source-review gate remains passed; full maintenance remains INCOMPLETE due to seven protected historical-worktree custody blockers. No controller or cleanup rerun. Current0.12 plan: [Phase1 review](../planning/gould-0.12/ARCHITECTURE-AND-PLAN.md), awaiting owner implementation approval.

# Gould administration safety follow-up and independent re-review

Overall maintenance: **INCOMPLETE**. Corrections belong to `maintenance/admin-safety-review`, based on accepted main `aa34ca788bffbb77c30d39a6b7dbaeb4c9508cb3`. Do not merge without owner approval. No cleanup controller is to be run against real resources during this task.

## Third independent review — PASSED within defined scope

Reviewed source: `85b237fc477b6010dfd4b6569eee141fcf455f4b`. Receipt: `.gradle/independent-review/admin-safety-KAtPTcmL/`; output SHA-256 `e0cd4dc080c048dd92c523681ac55973dbfb2ba920aee87afec0ddfa21919845`. Exit 0; five step starts, ten tool uses, four step finishes, one text event and zero errors. The owner ran the existing architect/PMP command with the explicitly requested `--model openai/gpt-6.1-sol` flag. The original receipt and owner-only copies/provenance are preserved in `.git/portability-audit/opencode-third-85b237f/`.

Codex validated identical before/after commit IDs, all seven recorded source hashes, and all 1,167 numbered source-read lines against the reviewed checkout. The reviewer also read commit-pinned GitHub source/diff. Independent conclusion: **no unresolved findings within the requested source-review scope**. Required initial active-registration gating precedes processing/backup/mutations; post-operation checking remains; missing/disabled regressions cover all four required paths; previous safety guards and workflow definitions remain unchanged.

The independent source-review gate is PASSED. This is not independently executed build/test evidence, filesystem immutability against an owner, branch-protection enforcement, or merge approval. The original and second review findings/history remain preserved below. Subsequent acceptance edits are restricted to policy/handoff/audit documentation; reviewed executable files and workflow definitions must remain byte-identical to `85b237f`.

Exact-`85b237f` administration CI independently ran **5+7+14 = 26 passing tests**, run `40593120537`; backend run `40593120571` also passed. Android quality run `40593120540` also passed: all three workflows succeeded at the exact reviewed source. Final documentation-commit CI still requires exact-head live readback; its actual outcome is saved in private verification/PMP and reported to the owner, never inferred from this document. All existing runtime/signing inputs, owner files, worktrees, tags and release assets are preserved. No live cleanup controller or destructive cleanup was repeated.

**Overall maintenance remains INCOMPLETE:** owner main-merge approval is outstanding, seven historical worktrees retain unresolved private-data custody, and uncertain historical branches remain protected. Present the final exact branch commit/CI and request explicit normal-merge approval. Do not merge into main, run controllers, retire data or implement Gould 0.12 based solely on this passed source review.

## Second review and required-registration correction

Second host review receipt: `.gradle/independent-review/admin-safety-tA7KgCnV/`; raw output SHA-256 `0baf96a2f3845a93dec40c094ee007e3ed8fb7797fb22cd6cd78751bed38f5f8`. Exit code 0, eight read tools, zero error events. The before/after commit is exactly `2c6eb1870b1681cef1480b87d755bc98d52f2f9a`; all seven supplied source hashes and all 971 numbered read lines matched that checkout. Original receipts and owner-only byte-exact copies/provenance remain in `.git/portability-audit/opencode-second-2c6eb18/`. This is independent source review, not independent test execution or merge approval.

The reviewer confirmed all four original source findings addressed and confirmed the CI definition covers all three suites. Actual CI logs separately show 5+7+12 tests passed at `2c6eb18`. One MEDIUM finding remained: required source definitions were checked early, but required active registrations were checked only after mutations when branch candidates were already absent.

The bounded follow-up checks `REQUIRED` against active paths in the **initial** inventory immediately after classification gating, before ruleset/backup processing or any administrative mutation. Missing or inactive required registrations raise a failure with the initial inventory preserved in a unique receipt. The original post-operation active-registration assertion remains unchanged. Two new mocked regressions cover missing and disabled registrations for each of the four required paths, with an obsolete active row present: every case proves no backup phase or GitHub/Git mutation is reached. The existing branch-race integration fixture now supplies all required active registrations so it still reaches and tests the retention guard.

Current local administration results: **26 passed** (5 legacy, 7 workflow/backup, 14 safety/controller tests). The four workflow definitions, shared safety guards, older controller, Android/backend/contracts/spikes inputs and protected owner data are unchanged by this additional correction. No live cleanup controller, force push, deletion or main merge was performed. The final correction's exact SHA/CI are recorded in private receipts/PMP and the final report. At this second-review checkpoint the third review was pending; its completed acceptance is recorded above. Maintenance remains INCOMPLETE pending owner merge approval and protected historical-data custody.

## Original independent evidence and scope

The host OpenCode architect review completed with wrapper exit 0. Its complete output is `.gradle/independent-review/opencode-review.jsonl`, SHA-256 `41f742134212ff0eab622168632d83fd770993b301243c9c30f0767aa7dacf53`; stderr is empty (SHA-256 `e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855`). Private byte-exact copies, parsed events and provenance are preserved in `.git/portability-audit/opencode-original-aa34ca7/`. No original receipt was overwritten. The output includes 10 step_start, 42 tool_use, 9 step_finish, 3 text and zero error events, followed by a pretty-printed PMP wrapper receipt. It is not strictly one JSON object per line at the end; parsing used consecutive JSON documents without altering the original.

The original brief named `12746b8`; the current accepted main and host PMP exit checkpoint 19 record `aa34ca7`. Codex compared all 2,575 numbered source lines returned by 34 read calls: every observed line matches that main checkout. All other tool results and the complete review text were inspected. No scripts/workflows/application/backend inputs changed between `12746b8` and `aa34ca7`; the intervening changes were handoff/audit/transfer documentation. This establishes the findings' applicability to current main. It does not expand the review to unseen lines or make the architect an independent Git/build/test verifier. The architect explicitly did not run Git, builds or tests.

Compared with maintenance baseline `bf837f2`, application/backend/contracts/spikes are unchanged. `finalize_github.py` was introduced at `bf837f2`; `finish_maintenance_admin.py` was introduced at `6d372a3`, with later backup/classifier corrections. These findings arose in maintenance tooling, not Android or backend feature code. Historical cleanup evidence is retained without claiming that the source defects prove any recorded operation deleted the wrong resource.

## Findings disposition

| Finding | Verified reachability and correction |
| --- | --- |
| HIGH-1: unchecked deletion race | Both controllers used ordinary deletion after API inspection. `finish_maintenance_admin.py --apply` could reach it; the older controller additionally requires its historical branch/main gates. Both deletion paths now unconditionally retain the branch and report failure through a shared guard. No approved atomic deletion backend is configured. No second GET, unconditional REST delete or force push is substituted. Mocked integration tests move the branch after validation and prove neither controller deletes it; the final controller also stops before workflow/RC mutations. |
| HIGH-2: wrong Git push destination | API identity did not bind origin's push destination; the older helper pushed before changing origin. Both now validate API repository ID/name, require exactly one canonical HTTPS effective origin push URL, reject URL rewrites, credentials/SSH ambiguity and multiple destinations, and fail closed on unavailable configuration. The remaining older helper's ordinary publication/tag reads use the explicit verified URL rather than the origin alias. Wrong-destination integration tests stop both controllers before any push. |
| MEDIUM-1: blocked workflow gate too late | Unknown managed/security rows affected only the final completion flag. A shared preflight now rejects any blocked/unrecognized classification immediately after preserving inventory/decisions in memory, before backup writes or any administrative mutation. Mocked tests include an obsolete active workflow plus an unknown security row and verify zero mutations and two independently preserved failure receipts. The already resolved managed Copilot classification remains KEEP. |
| MEDIUM-2: receipt overwrite | Both helpers reused a single report.json on dry-run, failure and apply. Each run now gets a private unique run directory and an exclusively created owner-only report. Existing legacy and earlier run receipts remain untouched; a second same-run write raises instead of replacing evidence. No latest-pointer overwrite is needed. |
| CI gap | New `.github/workflows/maintenance-admin.yml` runs all three administration test files with contents-read permission, pinned checkout, credentials persistence disabled and no account secrets. Existing historically pinned workflows remain byte-exact. The new administration workflow is explicitly required by the final helper; it cannot be classified obsolete merely because it first appears on the review branch. Before main integration, missing required main source causes that legacy helper to fail closed. |

Branch retirement is deliberately unavailable under the standing no-force-push policy. [GitHub's reference DELETE API](https://docs.github.com/en/rest/git/refs#delete-a-reference) has no expected-head request parameter. Git's explicit [force-with-lease](https://git-scm.com/docs/git-push) can guard a ref update, but this maintenance does not introduce or execute it under the owner's policy. A separately approved and independently verified atomic backend would be required before re-enabling branch deletion. Retention is the safe outcome, not a claim that another check removes the race.

## Verification and preservation at the first correction (`2c6eb18`)

Local administration regressions: **24 passed** (5 legacy guard, 7 workflow/backup, 12 controller/receipt/destination tests). Every controller Git/GitHub effect in integration tests is mocked and all fixture data lives in temporary directories. No real controller execution, deletion, rename, workflow disable or force push occurred.

Also passed: migration DDL 1–9→10, three historical backup fixtures, 1,103 EN/CZ resources, history paging, agent policy, historical evidence integrity, five backend private-runtime guard tests and parsing of all four workflow YAML definitions. All 2,463 accepted application/backend/contracts/spikes input hashes remain unchanged. Existing host19/348-JVM/main-CI evidence remains attributed to its original source, not independently executed by the reviewer. Exact follow-up commit CI and final preservation readbacks are recorded in private verification receipts/PMP and the final report; do not infer success from test definitions.

Seven historical worktrees remain protected pending approved independent private-data custody. The standalone PMP repository, original owner files, private review/administration receipts, signing identity, com.opus.piano, database compatibility, release assets and tags remain protected. Independent re-review is pending; Codex's corrections/tests do not constitute independent approval.

## Preserved third-review command — completed, do not rerun merely for status

Use the final commit SHA reported with this follow-up. The following command captures the actual checkout identity and hashes before invoking the existing architect/PMP wrapper, uses a new receipt directory, and verifies unchanged source afterward. Do not run it on dirty tracked source or another branch. Do not run either administration helper.

```sh
(
set -eu
cd "$HOME/Projects/Opus"
test "$(git branch --show-current)" = maintenance/admin-safety-review
git diff --quiet HEAD
review_sha="$(git rev-parse HEAD)"
mkdir -p .gradle/independent-review
review_dir="$(mktemp -d "$PWD/.gradle/independent-review/admin-safety-XXXXXXXX")"
printf '%s\n' "$review_sha" > "$review_dir/commit.before"
sha256sum scripts/maintenance_admin_safety.py scripts/finalize_github.py scripts/finish_maintenance_admin.py scripts/test_maintenance_admin_safety.py scripts/test_finalize_github.py scripts/test_finish_maintenance_admin.py .github/workflows/maintenance-admin.yml > "$review_dir/source.before.sha256"
review_exit=0
pmp --project "$PWD" launch opencode -- run --agent architect --model openai/gpt-6.1-sol --format json --file docs/maintenance/ADMIN-SAFETY-REVIEW.md "Perform the third independent review at exact commit $review_sha, narrowly covering the change since 2c6eb1870b1681cef1480b87d755bc98d52f2f9a: required active workflow registrations must be checked against the initial inventory before backup writes or administrative mutations, the post-operation check must remain, and missing/disabled registration regressions must prove zero mutations. Read scripts/finish_maintenance_admin.py and scripts/test_maintenance_admin_safety.py; confirm the previously reviewed retention/destination/blocked-decision/receipt guards and workflow definitions remain unchanged. Read tracked source only; no shell, edits, delegation, private records, tests or GitHub mutations. Report unresolved findings with severity and file/line. Do not infer executed tests or independent approval from Codex's report. Respond in English." > "$review_dir/opencode-review.jsonl" 2> "$review_dir/opencode-review.stderr" || review_exit=$?
printf '%s\n' "$review_exit" > "$review_dir/exit-code.txt"
git rev-parse HEAD > "$review_dir/commit.after"
cmp "$review_dir/commit.before" "$review_dir/commit.after"
git diff --quiet HEAD
sha256sum --check "$review_dir/source.before.sha256" > "$review_dir/source.after-check.txt"
printf 'Review exit: %s\nReceipt directory: %s\n' "$review_exit" "$review_dir"
exit "$review_exit"
)
```

Return the receipt directory and actual exit result. A zero exit is review execution success, not automatic acceptance of findings. Read the new independent conclusions before requesting merge approval or declaring the review gate resolved. The sandbox's known listen EPERM prevents this OpenCode service here; do not retry it or weaken the existing architect permissions/native-hook protections.

created by stokonn with love <3
