# PROPOSAL: Goldberg 0.11 — Database Update

Planning only; no 0.11 catalogue expansion or schema implementation is part of 0.10.1. Owner review must settle scope before development. Tracking: https://github.com/stokonn/Goldberg/issues/31 and milestone 4.

## Current evidence

Room schema 8 stores catalogue composers/works/cache separately from personal pieces, session snapshots, measured parts, untimed session-piece associations and composer favorites. Catalogue IDs are external strings; personal piece IDs are local longs. Open Opus imports have unassessed difficulty. Search normalizes accents and punctuation; catalogue titles and original composer names must not be automatically translated. Composer birth/death strings often encode January 1 placeholders, so the UI currently presents years. Personal records can exist without catalogue IDs. Remote search and cached browsing have separate result/loading paths.

## Proposed investigation

1. Measure coverage and defects on a reproducible licensed source snapshot: missing works/composers, ambiguous catalogue numbers, movements, alternate titles, instrumentation, eras and duplicate IDs. Publish aggregate findings, never journals.
2. Compare Open Opus CC0 with carefully attributed Wikidata enrichment. Review provenance, redistribution rights, update frequency and source stability before choosing additional datasets. Do not equate metadata rights with portrait or score rights.
3. Propose normalization rules with preserved original strings and stable source IDs. Aliases should improve search without merging distinct arrangements, movements or similarly named works automatically.
4. Design bounded local indexes/cache and explicit offline behavior. Preserve user-created pieces and allow unresolved catalogue links; a network failure must not make personal repertoire unavailable. Estimate download size, storage, battery and metered-network costs before choosing any preloaded catalogue.

## Migration and backup proposal

Keep com.opus.piano and opus.db. If approved work requires schema 9, add an explicit transactional 8→9 migration and test every supported historical schema through that path. No destructive fallback. Use new mapping/alias records rather than rewriting personal IDs or deleting duplicates automatically. Preserve session snapshots, measured totals, untimed links, notes, favorite composers, profile, drafts, badge dates and showcase. Unknown source records remain addressable.

Change backup format only if new portable fields require it; continue reading formats 1–4 with documented defaults. Export/restore fixtures must verify every original field and repeated restore behavior. Cache-only additions should remain disposable and excluded from critical backup state. Failed migration/import must leave the original data available for retry/export.

## Costs, decisions and acceptance

Decisions: intended catalogue size; supported instruments; movement granularity; alias policy; source attribution; offline bundle versus downloads; whether manual duplicate review is needed. These are proposals, not approved features. Engineering cost depends on data audit findings; a credible estimate needs a sample and acceptance scope. Storage/network cost should be measured in bytes, not guessed.

Acceptance proposal: deterministic source provenance; accent-insensitive title/composer/catalogue search; bounded paging; zero silent personal-data merges; offline personal-library access; all old backup fixtures and migration paths pass; seeded upgrade preserves exact IDs/totals/notes/drafts/favorites; catalogue failures remain retryable; measured query/first-open performance does not regress beyond an owner-approved budget. Add adversarial duplicates, missing IDs, interrupted imports and rollback tests. No 0.11 branch is created until implementation is authorized.

created by stokonn with love <3

## Stage 1 reconciliation — 2026-10-18

The owner has now approved the broader **Goldberg 0.11 — The Music Database Update** product architecture: multi-genre catalogue; Open Opus, MusicBrainz and Wikimedia integration; linked works/arrangements/artists/albums/recordings/collections; sourced encyclopedias; All/Works/Artists/Albums/Lists search; offline bundles/Smart Cache 2.0; explicit mobile-data permission; moderated Google-authenticated suggestions; Android/Web Admin sharing a secured managed service with a strict 0 CZK monthly budget. These are target requirements, not shipped features.

[Stage 1 architecture and readiness package](planning/goldberg-0.11/README.md) audits actual schema 8/source, preserves the original investigation and data-safety goals above, and adds contracts, free-tier/legal/security constraints, unresolved decisions and independent stages. Earlier statements that scope was wholly undecided or that no 0.11 branch would exist are historical: a dedicated **documentation-only planning branch** is now authorized. Implementation, physical schema selection, provisioning and release still require subsequent stage instructions. No Stage 2 work begins automatically; no main merge or release occurs in Stage 1.
