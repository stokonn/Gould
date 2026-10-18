# Proposed local-first cache and offline bundles

**Proposed Smart Cache 2.0, not current behavior.** Current 16 MiB response/32 MiB image limits and 7/30-day freshness are audited in [CURRENT-STATE.md](CURRENT-STATE.md); do not silently treat them as new bundle capacity. Final limits require measurements in Stage 2/5.

## Storage classes

| Class | Examples | Disposal and backup rule |
|---|---|---|
| Critical personal | Pieces, session snapshots/parts/intervals, untimed links/notes, draft/archives, profile/avatar, badge/favorite dates, preferences | Never evict for catalogue/media space. Preserve during upgrade; portable backup coverage must be explicitly documented. |
| Stable identity needed by personal data | Legacy provider IDs, reviewed mappings, minimum work/artist display snapshots, personal catalogue sidecars | Retain sufficient references when network/cache/bundles disappear. Catalogue tombstones must not orphan journal. |
| Regenerable cache | Raw provider JSON, query pages, unpinned text/thumbnails | Byte-bounded LRU/expiry by class, safe clear, no critical-table delete. Re-fetch only when policy allows. |
| Explicitly pinned bundles | Work/artist/album/collection encyclopedia manifests and selected dependency documents/media | Persist until user removal or separately explained rights/security quarantine; do not evict silently as generic cache. Removal preserves personal records and shared pinned dependencies. |
| Staging | Partial downloads/temp files/import transactions | Disposable after safe resume/reconciliation; never mistaken for complete offline content. |

Prefer separate app-private file directories and explicit ownership for cache/bundle/staging. Room can store metadata/manifests; whether large normalized catalogue data remains in opus.db or a separate regenerable store is a Stage 2 decision. A second catalogue store must not become a second journal or require a synchronous cross-database personal-data migration. Preserve the existing journal database identity and schema history.

## Bundle contract

Each manifest: bundleId/type/entityId, schemaVersion/revision/language, createdAt/validatedAt, dependency IDs, expected compressed/uncompressed bytes, per-file hashes/MIME, provider record/revision, per-item rights/attribution, minimum reader version, completion state and last error. Dependency expansion is bounded: a work can include selected creators/intro, an artist selected works, an album its edition/track metadata, and a collection its explicitly chosen members. Never recursively download an artist's complete discography or a whole list graph without a preview and approved size bound.

Show estimated download/storage and included scope before confirmation; unknown sizes need a hard streaming cap and a clear estimate label. Full text, portraits and other media must be independently selectable when practical. Text/rights manifest availability is required before publishing an asset as complete. Support metadata-only bundles when images lack rights or consent. Each language is a separate document; no automatic translation of personal/catalogue original text.

Proposed persistent states: `queued → waiting_for_policy → downloading → validating → installing → ready`, with `paused`, `failed_retryable`, `stale` and `quarantined` branches. A partial bundle is never ready. Use unique work IDs, deduplicate dependencies/in-flight requests, persist progress, validate hash/rights/reader version, then publish one manifest revision transactionally. Atomic file rename plus crash reconciliation handles files/Room not sharing a transaction. After a crash keep the previous ready revision or explicit partial status; cleanup removes only unreferenced staged files. Resume only with validated HTTP range/ETag support; otherwise restart that bounded file. Enforce archive path traversal, decompression-size and MIME/decode limits if archives are used.

Reference counting or mark-and-sweep across active manifests prevents deleting media shared by two bundles. Pinned data is accounted separately from opportunistic cache. On low disk stop downloads before exhausting reserve; leave practice/recovery export possible. No promise of unlimited offline storage. A rights withdrawal/takedown changes visibility and records a reason; do not silently erase unrelated journals or imply already-exported third-party copies can be revoked remotely.

## Scheduling, network policy and failure

Propose WorkManager only after dependency review in the implementation stage. Use unique persistent jobs, initial/retry backoff and cancellation, with unmetered scheduling for Wi-Fi-first updates. `UNMETERED` alone is not exactly “Wi-Fi”; runtime policy must also check validated network and actual transport, and honor VPN/captive-portal/metered-Wi-Fi cases. Reuse the meaning of current MediaPolicy rather than interpreting INTERNET as download permission. No background microphone work is added.

Background encyclopedia/media downloads require unmetered Wi-Fi by default; mobile/metered use requires explicit consent, a visible size estimate and optional per-job allowance. Recheck connectivity/consent before each file and during streaming. Revocation pauses current work and cancels pending metered transfers without losing prior ready data. Data Saver/roaming/battery/storage constraints and process restart must preserve a retryable state. Policy override cannot bypass per-provider limits, copyright or backend quotas.

Small user-initiated catalogue searches can remain available online under a disclosed metadata policy. This does **not** automatically grant consent for background metadata refresh, large bundle graphs or images. Owner must settle whether mobile permission is global/per-job and whether any automatic background metadata exception exists. Until decided, no automatic mobile background download. Offline community drafts stay local; reconnect does not automatically publish a queued submission or act with another account.

Freshness is separate from usability: display saved time, stale/partial status and retry action. Proposed initial tuning retains current 7-day Open Opus and 30-day supplements only as starting points, then measures provider policy and traffic. Conditional ETag/Last-Modified where supported; otherwise bounded refresh, no expiry-triggered deletion of useful offline content. Configurable storage caps must have bounded defaults and protected free-space reserve, measured rather than guessed. Shared cache keys include provider/entity kind/ID/language/revision/representation/license-policy version; raw URLs alone do not prove content identity.

## Isolation and acceptance

A future journal restore must pause/coordinate all download writers and invalidate obsolete generations, preserving existing cacheGate/PracticeCoordinator exclusion. Clear cache, Remove bundle, Restore journal and Sign out are separate actions with distinct consequences. New portable backup format should include personal link identities and required provenance, not megabytes of disposable response/image content by default; continue accepting existing format 4 that includes catalogue cache. Decide draft/preferences portability explicitly rather than inventing a complete backup claim.

Tests: offline first open of repertoire/history, saved encyclopedia with complete offline attribution, partial dependency failure, process kill at every state boundary, corrupt hash/MIME/archive, concurrent bundle removal, low disk, consent revocation mid-stream, Wi-Fi→mobile transition, Data Saver, cancellation, provider 429/503, cloud pause/quota exhaustion, shared media retention, cross-account draft handling, journal restore while downloads wait, and bytes/accounting after repeated clear/remove. Repeat on seeded emulator with an unfinished session. None of these future tests are claimed executed in Stage 1.

created by stokonn with love <3
