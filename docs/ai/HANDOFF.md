# Gould current maintenance handoff

## Gould 0.15 takeover — November 15, 2026

The owner directed completion of the 100-composer offline Core seed, direct
online MusicBrainz search, unified results, and Stage 3 verification. Work is
in the existing dirty `.worktrees/gould-0.15`, branch `feature/gould-0.15`,
HEAD `a908c96d6c3fa2712b77be1ad1e8941a1ac2e9da`. Preserve all changes and
diagnostic artifacts. No other worktree/main checkout was accessed. No commit,
push, merge, tag, PR, release or public promotion is authorized.

Stage 2 is complete. Stage 3 host gates and signed artifact checks passed; the
stage remains incomplete pending end-to-end online/offline/cache/detail/repertoire
runtime acceptance. Exact evidence is in the current checkpoint and verification
matrix. APK: `app/build/outputs/apk/release/app-release.apk`, 97,910,513 bytes,
SHA-256 `fa2a57025a8c14e0b000ce0462927244834aa5485b53007460f8d3957641bcae`,
trusted signing certificate SHA-256
`53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`.
It contains only the curated 89,123,721-byte compressed catalogue, never the
full dump. No emulator was running and the owner's phone was not accessed;
instrumentation compiled only. GitHub issue sync is pending because no existing
0.15 Stage issue was found; do not create a duplicate.

The older handoff below is historical and superseded only for Gould 0.15 by
this owner direction. All data, backup, signing and release controls remain.

## Current release handoff — November 14, 2026

Gould 0.14.0 has been published as GitHub pre-release `v0.14.0` from merged
commit `b507aaeb47187ac2280ecce2873bdf05cf2b40ee`. See the [current status](GOULD-CURRENT-STATUS.md)
and [release verification](../releases/GOULD-0.14.md). Do not publish another
release or retag this version. Owner physical-device in-place upgrade and
visual/accessibility review remain unreported. The maintenance checkpoint below is dated
historical context; its Gould 0.14 planning statements are superseded.

Start with [the current Gould status](GOULD-CURRENT-STATUS.md), then read the root [AGENTS.md](../../AGENTS.md) and [legacy cleanup policy](../maintenance/LEGACY-CLEANUP.md).

## Current task — Gould 0.15 Stage 2

Stage 2 implementation and feasible host verification are complete on dirty
worktree `.worktrees/gould-0.15`, branch `feature/gould-0.15`, HEAD
`a908c96d6c3fa2712b77be1ad1e8941a1ac2e9da`. Do not begin Stage 3 or publish.
The current checkpoint and verification matrix contain exact results. Latest
app JVM suite: 325 passed; activity-domain JVM suite: 13 passed; lint,
Android-test compilation, migration/backup/localization/policy checks, and
Release-variant build/signature checks passed. Android instrumentation was
compiled but not executed; no physical-device acceptance is claimed.

The obsolete Android legacy admin implementation was removed. The canonical
admin/client/repository and BFF/contract components were removed in turn by the
2026-11-25 owner decision: Gould is local-first with **no** product community
backend, BFF, admin API or custom cloud backend (see
`docs/architecture/BACKEND-RETIRED-2026-11-25.md`). The older preserve-both
wording below is historical and superseded first by the 2026-11-15 and then by
the 2026-11-25 owner decisions in `DECISIONS.md`.
Weekly success uses both configured session and minutes goals; historical
practice records and granted rewards were not rewritten or revoked.

MusicBrainz full-fingerprint authentication remains blocked. A partially
downloaded archive transfer was stopped before verification/extraction; no
records or dataset counts are claimed. Exact next owner action: obtain
independent full-fingerprint confirmation from MetaBrainz through an official
support/security channel. Do not resume archive acquisition before confirmation.
Production catalogue import needs separate owner approval. The Galaxy A55
owner-run upgrade, visual and accessibility checks remain pending. No GitHub
publication or Stage 3 activity occurred.

### Prior Stage 2 batch handoff — historical, superseded below

### Current implementation checkpoint — 2026-11-14

Work remains on dirty `feature/gould-0.15` at `a908c96`; preserve the complete
worktree. Safe community-draft export/recovery is now implemented in
`CommunityDraftArchive`, `CommunityDraftStore`, `CommunityRepository` and the
Settings draft panel. It is a bounded unencrypted version-1 JSON archive,
separate from backup formats 1–8; import is scope-bound, validates before
write, preserves pending frozen requests, rejects conflicts atomically and
rolls back injected publication faults. Read
`docs/planning/gould-0.15/COMMUNITY-RECOVERY-POLICY.md` and
`COMMUNITY-DRAFT-RECOVERY.md` before changing it.

Catalogue generation remains blocked from production use. The generator now
rejects unknown/supplementary/fixture input, and the installer validates the
SQLite before READY publication and only then promotes a higher catalogue
revision. New importer integrity/identity/anchor instrumentation compiled but
was not executed. Do not download the MusicBrainz snapshot or signing key; the
key inquiry remains drafted and unsent. Separate owner approval for production
catalogue generation/import remains necessary.

Publication workflow source review found read-only workflows, no secret
references and no release action/command. A static CI guard and nine tests are
in place; no workflow can publish. Per the owner's closure decision recorded on
2026-11-14, both admin systems and BFF artifacts remained intact at that time;
that state is historical — the 2026-11-25 owner decision retired them
completely. Operational usage/deployment was unverified; no further
investigation is needed for this stage.

Final INTEGRATION PASS: `scripts/verify-integration.sh`, 6 s incremental,
`2026-11-14T22:28:12Z`, 426 JVM tests / 0 failures, lint PASS,
`assembleDebugAndroidTest` PASS (compile only). Host migration, backup fixture,
history, UI literal, policy, localization, catalogue and workflow checks also
passed. Instrumentation, SAF runtime behavior, emulator widget rendering and
Galaxy A55 acceptance were not executed. Two detected issues were fixed before
the final passing run; audit log records retries and final run.

Stage 2 is **COMPLETE** for the authorized implementation scope. MusicBrainz
key/snapshot provenance and separate production-import approval are
prerequisites to production catalogue data, not blockers to this infrastructure
milestone. Owner Galaxy A55 widget/tour acceptance and native instrumentation
execution remain outstanding. No production archive/data, PR, commit, push,
merge, tag, release, publication or Stage 3 work occurred.

Four scoped work areas were reviewed in that earlier batch:

- Catalogue generator now fails closed on unapproved fields (including
  `annotation`, `genres`, `ratings`, `tags`, `popularity`) and on fixture
  provider records even when a synthetic marker is removed. Seven synthetic
  tests verify deterministic schema-1 output, rights/provenance gates,
  fixture rejection, and stable entity/source IDs. Production source data is
  still unavailable. The selected MusicBrainz snapshot is not verified or
  downloaded; its key inquiry to `support@metabrainz.org` is prepared but
  unsent. Snapshot inspection authorization does not imply production import
  approval.
- Canonical weekly success still means sessions **and** minutes. Activity UI,
  streak/week-close/Freeze, and rewards share the rule; Beta cosmetic eligibility
  remains intentionally separate from streak progression. Only a misleading
  KDoc was corrected. Profile/Home streak displays are daily streaks.
- Widget weekday chart labels now use the light/dark semantic text color. A
  rendered bitmap assertion was added to `WidgetTest`; it compiled but was not
  executed. Source review covered invalidation tables, locale refresh, sizing,
  and narrow/wide projections. No visual emulator or Galaxy A55 result is
  claimed.
- Guided-tour review found no demonstrable defect. Six sections, Room-backed
  progress, replay/resume, existing navigation, responsive stacking,
  accessibility semantics and EN/CZ resource parity were reviewed. UI
  instrumentation compiled only; actual process-death and TalkBack journeys
  remain unverified.

Verification: INTEGRATION PASS at `2026-11-14T21:33:54Z` on source SHA-256
`9c7cc47c263abd75b01d66af9e966f0c2016016589f847d0fcc0a69a313cd203`,
`a908c96-dirty`: full app JVM suite, lint, Android-test compilation. The
activity-domain JVM task passed from cache; Python generator tests 7/7,
localization (1315 resources), UI literal scan (8 existing audited literals),
Python byte-compilation, and `git diff --check` passed. One sandbox preflight
failed before Gradle startup; the serialized approved retry passed. No emulator
or physical phone was accessed. Worktree remains dirty with owner changes; no
Git integration action occurred. Next: await owner-approved sending of the
draft inquiry, verify key/fingerprint and signatures without policy overrides,
then continue only the authorized isolated snapshot inspection. Do not generate
or import production catalogue until its separate explicit approval.

## Protected maintenance boundaries

- Preserve all seven unresolved historical worktrees and their private/ignored evidence. No deletion or replacement is authorized.
- Preserve the six blocked historical branches and every release tag/asset.
- Do not modify the independent PMP repository.
- Do not run destructive cleanup, `git reset`, `git clean`, force operations, or `pmp init`.
- Do not install, launch, test or use ADB against the owner's physical phone. Any future ADB work must target one explicitly identified disposable emulator.
- Public Wiki synchronization is still unverified. `docs/wiki/` remains the canonical source; inspect the actual Wiki before attempting a non-destructive sync.

## Historical Gould 0.14 boundary

The older maintenance checkpoint below predates the completed Gould 0.14 work.
Gould 0.14.0 was published as a GitHub pre-release on November 14, 2026; see
the release verification and current status linked above. Its original planning
boundary and checklist remain historical audit evidence.

created by stokonn with love <3
