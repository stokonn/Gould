# Stage 8 — proposed integration sequence

**Plan only; no source integration executed or authorized by this preparation.** Preparation base `4c6a91d600ad18455a280f941f89740694d5afa0` on `feature/goldberg-0.11-stage-8`. Stage7 client/synthetic acceptance stays COMPLETE. Read [base decision](README.md) and [inventory](BASE-INVENTORY.json).

| Order | Future operation | Prerequisite / stop condition | Evidence required before next step |
|---|---|---|---|
| 0 | Preserve pinned inputs and inspect current branch/worktree/remote deltas again |8A packet may progress independently. Changed HEADs or unique work require review; never reset/force-push/overwrite. |Fresh inventory and exact accepted/source/artifact identity. |
| 1 | Reconcile Stage7A's9 acceptance-document deltas with Stage7B/master handoffs |Separate integration authorization; do not blindly cherry-pick stale master/AI content. Its app implementation is already in the base. |Stage7A final acceptance/X10 Gould0.12 disposition preserved, Stage7B accepted limitations unchanged; no fabricated fixes. |
| 2 | Review main-only CI history against selected Stage7B workflows |No main merge. Approval for a future integration branch does not authorize merging to main. Avoid replacing accepted262-pair/focus/row/Back/upgrade checks with older CI variants. |Content/guard/action/signing comparison; source binding and emulator-only controls preserved. |
| 3 | Review isolated Stage6 local foundation and older local client changes |Current pinned Stage6 `006fa577ff4faef8ee7a25daf851620feb5bc3a8` is local evidence only; open contracts/public-version/policy gates remain. Explicit authorization required for source combination. |Local source/test/result manifests; approved compatibility/version plan; independent backend security/int8/HTTP/PG regression evidence. |
| 4 | Adopt canonical client contracts/session/review/error semantics in an authorized integration branch |G1–G9/G11 adoption must be explicit; public version/compatibility and real policy decisions unresolved. No ad-hoc DTO translation, false data or production enablement. |Shared fixtures in actual Android consumer; int8/timestamps, nulls, fresh Detail+Comparison, account/role/refresh/loss/conflict tests; per-gate results. |
| 5 | Combine authorized Android changes while preserving data/offline behavior |Review potential overlaps below; stable contracts and implementation scope must be authorized. No destructive DB migration or recovery replacement. |Clean host gate, migrations/backups/localization/paging, signed APK/source identity and actual emulator verification at combined SHA. |
| 6 | Assess the three deferred accessibility/layout findings and unverified coverage |Stage7 acceptance is retained, findings remain NOT FIXED; preparation did not authorize a production fix. |Real NavHost reproduction in EN/CZ, both themes and representative fonts; documented release decision for each, before/after evidence if a separately authorized fix is made. |
| 7 | Run permitted hosted/live integration and operational/legal readiness |G1–G9/G11,C4,6.0f and production gates must be resolved with explicit authorization. No OAuth claim without valid configuration. |Independent real-consumer/session/role/server behavior, security, export/restore, attribution/privacy/controller/cost evidence. Local mocks do not substitute. |
| 8 | Assemble final integrated candidate and owner decision packet |All mandatory Stage8 matrix checks pass or owner explicitly dispositions allowed exceptions; no silent exclusion of backend scope. |Exact combined source SHA, trusted-signed APK/hash, seeded upgrade/personal-data invariants, actual screenshots/tests/limits and documentation. |
| 9 | Seek separate main merge/tag/deploy/release authorization |This request explicitly forbids merge, deploy and release. |Concrete reviewable candidate plus explicit later permission; no action performed by8A. |

## Potential overlap inventory

Two-sided changed-path intersection against common ancestor `bcd88949b7998ce30df25b19b71de30dd413483a` (13 paths, not confirmed merge conflicts):

- `.gitattributes`
- `app/src/main/java/com/opus/piano/OpusApplication.kt`
- `app/src/main/java/com/opus/piano/ui/SettingsScreen.kt`
- `app/src/main/res/values-cs/strings.xml`
- `app/src/main/res/values/strings.xml`
- `contracts/admin/v1/README.md`
- `contracts/admin/v1/fixtures.json`
- `contracts/admin/v1/source-provenance.json`
- `docs/ai/DECISIONS.md`
- `docs/ai/HANDOFF.md`
- `docs/ai/STATE.md`
- `docs/ai/VERIFICATION.md`
- `docs/planning/goldberg-0.11/STAGE-CHECKLIST.md`

The application initialization/settings/resource overlaps can affect repositories, lifecycle, locale and data/settings preservation. Contract fixture/provenance overlaps require semantic reconciliation, not whole-file replacement. AI/master docs must retain accepted client status and newer Stage6 evidence. Preserve raw evidence formatting attributes. Separate untouched source paths can still have semantic conflicts;13 is not an exhaustive compatibility proof.

No branches were merged or cherry-picked, no working-tree merge attempted, no conflict automatically resolved. If Stage6 remains blocked, keep the final integration/release verdict BLOCKED. Any owner-approved scope change must be explicit and documented;8A does not silently redefine0.11 as offline-only.

created by stokonn with love <3
