# Stage 1 evidence, checks and limits

Audit date 2026-10-18. Baseline source `556970ab7407ec6986e3678657adbd2168516468`; production tag `v0.10.1` unchanged. Dedicated branch `planning/goldberg-0.11-stage-1`. Only documentation paths are changed. The unrelated untracked RELEASE_NOTES.md was reviewed: it is a historical Goldberg 0.6.0 release draft, not Stage 1 material; its original digest is unchanged and it is excluded from staging. No accounts, cloud resources, paid services, schema/Gradle/application code, signing, user data or physical-device actions.

## Executed baseline checks

```sh
GRADLE_USER_HOME=/tmp/goldberg-gradle-home ./gradlew --offline assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug --no-daemon --max-workers=1
GRADLE_USER_HOME=/tmp/goldberg-gradle-home ./gradlew --offline testDebugUnitTest --rerun --no-daemon --max-workers=1
python3 scripts/check_migrations.py
python3 scripts/check_backup_fixtures.py
python3 scripts/check_history_paging.py
python3 scripts/check_localization.py
python3 scripts/check_agent_policy.py
```

Results: Gradle **BUILD SUCCESSFUL in 1m 11s**, 83 tasks (4 executed, 79 up-to-date), log `/tmp/goldberg011-stage1-gradle.log`. Cached tasks reused the unchanged baseline; this was not a clean rebuild. The explicit `testDebugUnitTest --rerun` then executed the test task afresh: **BUILD SUCCESSFUL in 12s**, 26 tasks (1 executed, 25 up-to-date), log `/tmp/goldberg011-stage1-jvm.log`. Fresh JVM XML reports contain **75 tests, 0 failures, 0 errors, 0 skips**. Instrumentation APK assembly passed/reused; **instrumentation was not executed in Stage 1**. Lint XML: **0 errors/fatal, 83 warnings**. Existing unused-resource/style/dependency recommendations remain; documentation-only work did not modify app code to suppress them.

Host checks: all schema 1–7→8 fixture migrations preserve data and match Room export; three historical backup fixtures retain original portable fields; history paging covers 106 sessions/four pages, ties/duplicate parts and untimed attribution; localization covers 804 CZ/EN resources; canonical policy/thin agent references/Wiki navigation pass. Agent runtime instruction loading is not tested. Host migration simulation is not proof of every Room migration executed on Android.

Documentation checks: 10 Markdown files were checked for 34 local path links, balanced code fences and inline table escaping using `/tmp/goldberg011-stage1-doc-check.py`; wiki-style navigation checked by existing policy script. Source-linked files resolve. `git diff --check` and a changed-path guard confirm no application/build/schema/scripts/CI changes. External URLs were checked through primary documentation browsing, not a complete automated external-link crawler or live provider contract suite. A source-backed inventory confirms Room schema 8 / 15 entities and app 0.10.1 / code 11. Detailed logs/checkpoint remain local under `/tmp/goldberg011-stage1-*`.

No new emulator/UI/migration-upgrade/signature test was required for documentation changes and none is claimed. The 0.10.1 release and maintenance had independent emulator/signing/upgrade evidence in their existing ledgers; those historical results are not Stage 1 reruns. No 0.11 APK is built or published. Build output still has 0.10.1 metadata.

## External evidence quality

Primary sources reviewed: Open Opus publisher/API documentation; MusicBrainz/MetaBrainz official API/search/license/rate/access documentation; Wikidata access, Wikimedia terms/rate/maxlag and Commons reuse/Imageinfo; Google server verification; Supabase pricing/billing/cost-control/pausing/backups/Auth/RLS; Cloudflare Workers/D1 pricing/limits/Time Travel/Web Crypto and Pages limits. Links are attached to the relevant claims in the design documents, with audit-date scope rather than a promise of future terms.

Some MusicBrainz document opens returned 429; official indexed snippets exposed API/license distinctions but complete core/supplementary field classification remains a Stage 2 gate. No rate-limit bypass, repeated bulk fetch, paid mirror or unofficial legal inference is used. Wikipedia TextExtracts documentation explicitly notes maintenance concerns; endpoint/revision consistency remains a contract-selection gate rather than an assumed robust long-term API. No live data quality/cross-provider mapping benchmark, provider account/eligibility inspection, cloud security test or load test was performed. Free-tier feasibility is conditional on small measured traffic and allowed service denial, not a certified capacity forecast.

## GitHub and Wiki workflow

Verified repository `stokonn/Goldberg`, main default, Wiki enabled/initialized. Existing private [Goldberg Development Project](https://github.com/users/stokonn/projects/2), [milestone 4](https://github.com/stokonn/Goldberg/milestone/4) and [umbrella issue #31](https://github.com/stokonn/Goldberg/issues/31) are reused. Project fields support Status, Priority, Version and Work Type. No duplicate Project/milestone is created. Umbrella remains open because future implementation and owner decisions remain pending. Stage 1 evidence is attached there. No duplicate Stage 1 issue is created or closed; the umbrella returns to Backlog while awaiting decisions and a separate Stage 2 instruction.

English `docs/wiki/Changelog-and-Roadmap.md` receives a planning-only link; it does not advertise 0.11 as installed functionality. Other user guides remain descriptions of released behavior. The planning branch is pushed without merging main and no GitHub Release/tag is created. Final commit/push and remote tracking evidence are reported in the handoff/issue, avoiding self-referential source hashes in this document.

created by stokonn with love <3
