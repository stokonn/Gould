# Stage 3A persistence implementation

This describes the Stage 3A branch, **not the published Goldberg 0.10.1 application**. Stage 2 contracts remain the logical specification. No production provider adapter, network synchronization, new UI, backup v5 or cloud service is introduced.

## Personal journal: `opus.db`, Room 9

Schema 8 was verified before implementation. Explicit `MIGRATION_8_9` adds three tables, a seed view and row triggers; existing migrations 1→8 remain unchanged. `ALL_MIGRATIONS` is the shared registration list. There is no destructive fallback. All original 15 tables, columns, indexes, numeric IDs and stored values remain in place, including the three legacy catalogue/cache tables.

- `personal_catalogue_identities`: stable identity key, stored opaque UUIDv4, work/artist kind, legacy provider ID and unreviewed evidence marker. `(canonicalId,kind)` is unique. Numeric work and artist namespaces differ. Manual identities use the journal scope and immutable personal Piece ID, never a title hash.
- `personal_catalogue_anchors`: `(ownerType,ownerId,role)`, registry key, original title/creator/legacy ID and evidence snapshot. Owners include pieces, sessions, measured parts, untimed links, favorites and saved recordings. No foreign key to the auxiliary database or cascade from catalogue removal exists. Original personal rows remain the current display/edit authority; anchors preserve their first captured snapshot.
- `personal_catalogue_state`: persistent random journal scope. Legacy replacement rotates the scope so reused numeric manual Piece IDs from another journal do not adopt unrelated identities. Previous registry evidence is retained.

The migration is transactional and local. The same inputs follow the same identity rules; random UUIDv4 allocation occurs once when a key is first persisted, rather than yielding identical UUIDs across independent installations. A rolled-back migration leaves no partially allocated registry. No timestamps, draft JSON, microphone state, historical credit or completion token are rewritten. Opaque recovery payloads are not parsed to invent catalogue facts.

Insert/update triggers keep new personal rows anchored in their journal transaction. Explicit absence predicates avoid SQLite's outer UPDATE conflict policy overriding an inner INSERT OR IGNORE. Original anchors are immutable snapshots, including after manual edits or legacy metadata backfills; those edits remain in their existing authoritative columns. This bridge does not automatically assert that a manually edited title is a new arrangement.

## Separate catalogue: Room 1

`CatalogueDatabase` defaults to `goldberg-catalogue.db`. Schema exports remain under `app/schemas/` with the historical journal exports. Twenty tables cover roots, seven subtypes, relationships, provider mappings and revision history, redirect revision history, aliases/FTS4, sources, rights, assertions, document/media indexes and copy state.

Roots use UUID/kind/title/revision/status. Composite root `(id,kind)` foreign keys prevent subtype/endpoint kind confusion. Works include compositions, independent movements/parts, medleys and mashups. Arrangements have distinct IDs and source edges. Album families distinguish album/EP/single/soundtrack/unknown; editions and recordings are separate. Artist dates retain explicit precision. Structured extension facts (country, labels, ISRC, catalogue numbers, genres and unresolved credited strings) belong in provenance-bearing field assertions rather than guessed subtype defaults.

### Physical choices relative to Stage 2 research SQL

One typed `relationships` table replaces the proposed individual join tables. Discriminator values are `part`, `source`, `arrangement_source`, `credit`, `edition`, `track`, `performance`, `member`. Both endpoint kinds are composite foreign keys; SQLite guards restrict valid endpoint pairs and work subtypes. Part edges point **child→parent**, matching the research DDL; the authored JSON's parent→child example is explicitly converted by the Android fixture loader. Tracks carry medium/position/credited title; members retain stable relationship UUIDs. Ordered position/parent guards use triggers plus declared indexes because Room does not export partial unique-index predicates. Cross-work part/derivation cycles are rejected in SQLite. Nested collection members are rejected.

Mappings have a unique provider/kind/external-ID key and append-only revision history. Retargeting requires reviewed evidence, sequential revision and the same entity kind. Redirect revisions retain source/target/reason/evidence/reviewer; a new null-target revision reverses a redirect. Repository transactions reject self, cyclic, cross-kind or excessive redirect chains (64 hops). Neither mapping path writes to practice data.

Sources retain provider record/revision/hash/retrieval/source URL. Rights retain license URL, attribution, review state and adaptation notice. Selected assertions and document/media indexes require approved rights; unknown, NC/ND and unlisted approved licenses are rejected. Historical contradictory/unselected assertions can remain. These are local persistence guards, **not a source licensing certification or a replacement for Stage 4 field projection and legal review**. No real article, image, audio or score is bundled here; the Android catalogue fixture is the exact authored CC0 Stage 2 fixture.

[Room FTS4](https://developer.android.com/reference/androidx/room/Fts4) uses external-content synchronization with Unicode61 and normalized aliases. Prefix wildcards belong inside each quoted token. Queries normalize/quote at most eight tokens from 512 characters. Page sizes are 1–100, default40, with UUID keyset pagination and deterministic ID ties. This is a local DAO boundary, not Stage 4's provider/generation-bound public search envelope. DAO visibility is internal; future adapters must use the validated repository boundary. There is no new Library search UI.

## Lazy initialization and failure isolation

`OpusApplication.localCatalogue` is lazy and unused by existing journal/recovery/Library startup paths. `LocalCatalogueStore` serializes catalogue operations on IO; it never joins a personal transaction with a catalogue transaction. Initialization reads at most128 durable identities per call, copies original local snapshots with unreviewed rights and stores progress in the same catalogue transaction. Repeated passes pick up later personal identities. It does not import the whole old cache, call AchievementStore, rewrite journal snapshots, fetch providers or mint replacement personal UUIDs.

A failed copy rolls back only its catalogue batch. Open/query failures produce an unavailable result and leave the journal usable. The helper corruption callback does not delete corrupt files. Explicit rebuild creates another generation, reconstructs personal references, validates FKs/integrity, then atomically publishes a filename pointer. Old/failed generations and sidecars are retained. Access is serialized so readers do not outlive a swap. No downloader or pinned offline manifests exist in this stage; their future reconstruction/GC requires Stage 5. Retained generations currently need deliberate future maintenance, not automatic file deletion.

## Legacy backups and limits

Readers for formats1–4 and the format4/schema8 writer remain. Format4 still exports exactly its old tables, preserving compatibility with older readers. Restore rebuilds derived anchors transactionally and rotates only the local scope, preserving registry evidence and existing provider mappings. A pending destination practice draft now rejects replacement before any journal deletion; the original payload remains available for existing recovery/export. This safety guard is not a v5 reader or multi-store restore implementation.

New identities/settings/drafts are **not yet portable to another installation through format4**. Restoring a manual journal through a historical format intentionally cannot claim its old UUIDs without an identity section. Stage 3B must implement v5 with full fixtures, exact recovery archives, preference replay and token conflict tests. Do not claim v4 transports these new fields. Do not downgrade a schema9 journal with an older APK or uninstall to bypass it; retain a pre-upgrade historical-format export/trusted APK and use a separately reviewed recovery procedure.

created by stokonn with love <3
