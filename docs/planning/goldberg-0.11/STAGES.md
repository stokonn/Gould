# Independent implementation stages and acceptance gates

**Stage 1 ends with documentation and a pushed planning branch. Do not start Stage 2 automatically.** Target product scope is approved; physical schemas, API contracts, cloud provider/processing and operational decisions below remain designs. Each later stage needs its own execution instruction, scoped branch and evidence. No merge to main or release is authorized by this planning stage.

## Decision register

| ID | Decision / recommendation | Needed by |
|---|---|---|
| D1 | Agree initial genre/coverage sample, movement/medley/arrangement boundary and Albums treatment of singles/EPs. Recommend explicit gaps and curated representative coverage rather than claiming a complete music universe. | Stage 2 contracts/sample; not a reduction of approved multi-genre direction. |
| D2 | Approve canonical ID ownership and reversible mapping/merge policy; one or several arrangements per personal work. Recommend typed UUID identities with legacy sidecars and no personal-data merge. | Stage 2, before any schema migration. |
| D3 | Select physical catalogue storage (same Room with strict separation vs a disposable auxiliary store), backup scope/version evolution and whether to expand portable settings/draft coverage. Keep opus.db and format 1–4 readers. | Stage 2 design; Stage 3 implementation. |
| D4 | Accept or reject Supabase Free pausing/restrictions and select one backend; alternative Workers/D1 needs a bounded auth/performance spike. No paid escalation regardless of quota. | Before cloud implementation/provisioning; Stage 2 can defer service code. |
| D5 | Name owner/operator/controller contact, moderation roles and escalation route; choose processing region, contributor licensing, audience/minor policy, retention/delete/export policy. | Before collecting any community data or configuring public OAuth. |
| D6 | Choose supported encyclopedia languages/fallback labels, permitted text/media licenses and derivative attribution rendering; classify MusicBrainz fields from primary license evidence. Album artwork/audio/scores excluded without specific rights. | Stage 2 source contracts; any redistributed content. |
| D7 | Set measured storage caps/free-space reserve, bundle dependency scope and per-job/global metered consent. Default no automatic mobile background downloads. | Stage 2 cache contract; Stage 5. |
| D8 | Choose Android Admin packaging and static web auth/session flow; bootstrap trusted admin subjects and secure recovery/MFA policy. Proposed same API and no embedded admin secrets. | Stage 6/7; not needed to implement local catalogue contracts. |
| D9 | Approve performance budgets from reproducible baseline/sample and realistic traffic/moderator capacity. Proposed cloud warning/stop thresholds are conservative, not accepted load estimates. | Stage 2 benchmarks and cloud beta. |

These are recorded decisions for the next stage, not requests for routine edits or paid services in Stage 1. An unanswered decision blocks its dependent task only. No infrastructure is provisioned while deciding.

## Stage 2 — Contracts, fixtures and bounded data-quality investigation

Inputs: this package, owner instruction and applicable D1–D3/D6/D7/D9 choices. Deliver finalized conceptual/physical model proposal, versioned adapter/search/provenance/bundle envelopes, field/license allowlists, identity matching rules, adversarial and multi-genre fixture matrix, measured sample coverage/size/query costs, and migration/backup test design. Use deterministic licensed snapshots; no whole-provider scrape or journal upload. Record source revisions and review Wikipedia endpoint lifecycle (current TextExtracts is not a guaranteed long-term contract).

Acceptance: every approved entity has unambiguous semantics; name collisions/arrangements/editions/redirects have expected outcomes; license/storage budgets are measured, not guessed; legacy numeric consumers are inventoried; old backup/draft examples have explicit preservation expectations; uncertainty appears as unresolved rather than fabricated mappings. A sample ingestion/prototype must be isolated from production if separately authorized. Stage 2 does not implicitly authorize cloud accounts, paid products or deployment. Stop for review before schema implementation.

## Stage 3 — Additive local persistence and compatibility

Depends on Stage 2 approved contracts. Implement only necessary Room migration(s), identity sidecars/indexes/DAOs and backup reader/writer changes. Version numbers are chosen at implementation time, not preallocated by this document. No network in migrations; no destructive fallback or mass journal rewrite. Catalogue-only changes do not require a new backup format unless portable critical fields change.

Acceptance: every historical schema→new schema tested on Android and host fixtures; IDs/notes/title snapshots/timing/tokens/drafts/archives/favorites/badge dates/profile/prefs survive a seeded in-place upgrade; failure rollback and repeated open/import pass; old formats 1–4 validate/import with unchanged original fields; new backup round trip preserves new personal links; offline recovery/export works without provider resolution. No cache eviction can delete personal data. Explicit downgrade/recovery plan documented.

## Stage 4 — Provider adapters and unified read/search

Depends on Stage 2 (and Stage 3 persistence where needed). Separate Open Opus, MusicBrainz and Wikimedia adapters with bounded scheduling, cached/source-partial result envelopes and independent failure. Introduce typed Works/Artists/Albums/Lists result categories behind repository contracts; direct/public reads first, moderated overlay can be mocked until Stage 6. Preserve personal library operation and legacy routes.

Acceptance: request/parse/license fixtures for every field; current User-Agent/contact and host redirects; MusicBrainz rate ceiling; Wikimedia maxlag/Retry-After handling; cancellation/stale-response tests; identity/duplicate/revision conflict tests; deterministic cursor pages, bounded database queries/memory; original names/aliases and CZ/EN UI resources; 404 vs offline vs quota errors distinct. No live copyrighted album-art/score/audio download by default. Genre/coverage limits documented.

## Stage 5 — Offline bundles and Smart Cache 2.0

Depends on Stage 3/4 and D7. Implement durable manifest/progress and bounded background scheduler with explicit metered consent, byte budgets, attribution and shared dependency accounting. Work/artist/album/collection scopes remain visible and bounded. No background microphone changes.

Acceptance: process-death/resume/low-space/corrupt asset/concurrent remove/clear/restore tests; Wi-Fi→mobile/consent-revoked/Data Saver cases on emulator; no unexpected mobile traffic; complete offline encyclopedia credits and partial states; old ready revision survives interrupted refresh; critical practice stays available when all caches/bundles are removed. Measure actual disk/network costs and verify limits.

## Stage 6 — Secured optional community backend

Depends on D4/D5, approved auth/role/API contracts and zero-cost operational plan. Implement locally/test first; provisioning needs a subsequent explicit cloud instruction and verified Free plan. Managed Google token exchange/server validation, role/row rules, revisioned submissions/reviews and export/restore. No journal sync. Use the same service for Android/Web Admin.

Acceptance: threat tests in COMMUNITY-ADMIN.md, no public access to pending/private data; concurrent/idempotent approval and audit transaction tests; role removal/replay/expiry/JWK rotation; rate/quota/kill-switch/budget tests; cold/pause/unavailable backend does not block local app; export+isolated restore drill; privacy/terms/contact/licensing published before collection. No credentials in client/repo/logs and no paid feature activation. If free quotas cannot meet measured load, restrict optional service scope/availability through a reviewed decision; do not upgrade.

## Stage 7 — Encyclopedia UX and Android/Web Admin clients

Depends on stable Stage 4/5 contracts; admin actions depend on Stage 6. Integrate encyclopedia pages/categories and download management into established four-tab editorial navigation. Preserve Appearance controls, semantic colors/fonts, font scaling/reduced motion, light/dark and localization. Build Admin queue/diff/source/decision UI in both clients with identical authorization semantics.

Acceptance: emulator screenshots compared against 0.10.1 design identity; narrow/large-font/empty/error/offline/partial states; accessibility/touch targets and CZ/EN; wrong-role clients and forged API calls denied server-side; source/license/revision display and explicit action confirmations; concurrent edits show conflict; sign-out/account switch cannot submit a different user's local draft. Web keyboard/CSP/XSS/CSRF/session handling tested. No speculative integration shown as working.

## Stage 8 — Integration, legal review and release readiness

Only after the separately authorized implemented scope passes all prior gates. Run the wrapper clean build/JVM/lint/instrumentation compile and actual emulator execution; migration/backup/localization/paging checks; representative provider/offline/metered behavior; recovery/time accounting; seeded production 0.10.1 upgrade; package/version/ZIP/trusted certificate/hash/source-artifact identity. Verify provider terms/licenses, privacy/controller, quota constraints and restore operation again at release time.

Update English Wiki/user/installation/developer/FAQ/changelog/credits with verified features and limitations. Reuse milestone 4 and existing Project; close only completed, verified tasks with commit evidence. Release needs later explicit instructions and current mandatory gates; Stage 1 grants no 0.11 publication permission. No physical phone or mandatory owner manual tests.

## Handoff rules

Each stage report identifies input commit, changed paths, data/architecture effects, exact checks/results, failures and unexecuted checks, license/privacy implications, cost/infra state, next dependency and branch/commit. Keep unfinished implementation issues open. Stages are independently reviewable; backend work may remain blocked while a separately authorized local stage proceeds, but no silent omission of approved final product scope. Re-read canonical policy and permanent maintenance ledger; preserve all historical evidence and unrelated work.

created by stokonn with love <3

## Stage 2 decision reconciliation — 2026-10-18

Owner instructions now settle D1/D2/D3/D6 and authorize measured D7/D9 proposals. Read the [updated decision register and Stage 3 prerequisites](stage-2/READINESS.md) before applying the historical Stage 1 decision table. The catalogue is designed as a separate database with durable personal references in opus.db; future portable backups include safe paused recovery/settings. D4/D5/D8 remain later cloud/Admin gates. Stage 2 is isolated research and specifications only; Stage 3 requires a separate instruction.

## Owner-directed Stage 3 split

Stage 3A implements only the safe local persistence bridge and separate catalogue; see [implementation and evidence](stage-3a/README.md). Stage 3B remains a separately instructed portable-backup v5/settings/recovery project. The historical combined Stage 3 acceptance criteria above are therefore not all claimed complete by Stage 3A. Preserve format1–4 compatibility and schema9 UUID anchors before implementing v5. Do not start Stage 3B or Stage 4 automatically.

## Stage 3B implementation checkpoint

The owner separately authorized [portable v5 and safe recovery](stage-3b/README.md). This branch adds the additive9→10 personal migration, format1–4 readers, UUID/settings/raw-recovery portability, completion evidence and durable preference replay. Acceptance is recorded in its own verification ledger. Stage4 remains a separate future instruction, with no automatic continuation, provider adapter, cloud deployment, main merge or release.
