# Measurements and proposed budgets

Research-only baseline, 2026-10-18. Raw numeric results: [host benchmark](results/host-benchmark.json), [emulator launches](results/emulator-startup.json), [bundle cost model](results/bundle-costs.json) and [source requests](results/source-observations.json). No personal database was opened by the host utilities. All SQLite files are generated under TemporaryDirectory and removed when the run ends.

## Host method and results

`python3 docs/planning/goldberg-0.11/stage-2/research.py bench --output /tmp/goldberg011-stage2-host.json`

Environment: Python3.14.8, SQLite3.51.2, x86_64 Linux. Synthetic catalogue scales 1k/10k/50k and 10k journal sessions, actual schema 8 DDL from the checked-in Room export, actual full ordered catalogue query shape, then an isolated proposed normalized B-tree/FTS4 catalogue. Queries use 31 warm repetitions (full journal11). Python normalization/allocation is a proxy, not Kotlin/ART/Compose profiling; no claim of Android memory savings follows. Build/index time includes entity/alias/FTS insertion, not provider parsing. Test dataset labels are uniform and short, not an average worldwide catalogue.

| Catalogue rows | Legacy full snapshot median / p95 ms | Traced snapshot+normalization peak bytes | FTS4 page median / p95 ms | Proposed DB bytes / alias+FTS incremental bytes |
|---:|---:|---:|---:|---:|
| 1000 | 2.244 / 2.8375 | 487015 | 0.0218 / 0.0302 | 888832 / 352256 |
| 10000 | 23.4211 / 24.3692 | 4732815 | 0.0222 / 0.0285 | 6320128 / 3416064 |
| 50000 | 138.0796 / 143.3468 | 24195268 | 0.0325 / 0.0531 | 30654464 / 17264640 |

Prefix B-tree queries independently returned 40 rows in about 0.03 ms; FTS query used `fixture 000010*` and a40-row bound. FTS/token-prefix queries are **not equivalent to arbitrary in-memory substring search**; these numbers compare cost shapes, not a proven end-to-end user-search speedup. Alias/FTS increment includes table data and indexes, not pure index bytes. Query plans show covering B-tree and FTS virtual-index access; production filters/credits/relevance/page merging need later benchmarks.

At10k sessions, full journal snapshots cost about 25 ms versus about 0.16 ms for a40-row history page on this host. Catalogue refresh must not trigger unnecessary full journal evaluation. A 50k catalogue snapshot alone took about 138 ms and 24.2 MB traced Python allocations; lazy opening, bounded query pages and background index generation have clear priority over importing everything during startup. Host timings vary with cache/CPU and cannot set physical-device frame-time claims.

## Existing emulator app baseline

Five force-stopped **process launches**, not cold emulator boots or filesystem-cache flushes. Tool: [measure_emulator.py](measure_emulator.py), explicit emulator-5582 with ro.kernel.qemu=1, API 37 x86_64. Existing disposable data retained; no install/wipe/physical device. Package0.10.1/code 11, installed APK SHA256 `a4bd2192bae315bae317eb68b6c4ef7533de4af581087033e351a92d51593dfb`: this is the verified post-release maintenance build, **not the immutable published APK**. Its baseline architecture is unchanged; the distinction matters for repeatability.

am start -W TotalTime samples: [1557, 1860, 1650, 1762, 1463]ms; median **1650 ms**, range **1463–1860 ms**. PSS measured two seconds after launch: **130086–130428 KiB**. TotalTime is the Android activity launch measurement, not time to usable journal/recovery content or a frame-time trace. This is system PSS, separate from host tracemalloc; it includes shared memory attribution and is not Java heap usage. Five samples do not establish a stable statistical p95. No new screenshot/UI acceptance or Smart acoustic accuracy is claimed.

Reproduce only on a disposable identified emulator:

```sh
python3 docs/planning/goldberg-0.11/stage-2/measure_emulator.py --serial emulator-5582 --adb /home/stokonn/Android/Sdk/platform-tools/adb --output /tmp/goldberg011-stage2-emulator.json
```

## Actual transfer/storage observations and modeled bundles

The25 API calls transferred526451 bytes in the initial 23 JSON responses plus1, 372, 935 bytes for two full revision-bearing article responses: **1, 899, 386 bytes**, excluding protocol headers/documentation browsing. Nine initial responses contained JSONmaxlag errors; successful HTTP status is not successful API coverage. No full catalogue scrape or image transfer occurred. MusicBrainz calls were spaced conservatively and returned at most3 candidates/query. See per-request measured durations/bytes/hashes rather than interpreting variable latency as a provider SLA.

English full article JSON was1, 171, 228 bytes (HTML1, 092, 181), Czech201, 707 bytes (HTML189, 470). Offline local gzip estimates were180372/34381 bytes for those JSON bodies; these are **local compression estimates, not measured HTTP encoded transfer**. Previews contained only840/384 bytes of text and cannot stand in for full encyclopedias. Current response cache freshness is not an offline-copy retention obligation.

The cost worksheet deliberately models two language article bodies + an original portrait size630150 bytes from metadata (rights not accepted, not downloaded) +100 median383byte CC0 projection records. Result: **2, 041, 385 bytes/reference bundle**, before manifests/indexes/decode/staging overhead. One hundred such independent bundles is204, 138, 500 bytes before dedup. Two identical dependency sets store2, 041, 385 rather than4, 082, 770 bytes, excluding separate root manifests; real bundles overlap partially, so this is an upper-bound dedup example, not a measured hit rate. Thumbnail transfer/decoded Android memory, full rich-relationship metadata, collection size distribution and real work-specific encyclopedia sizes remain unmeasured.

## Proposed starting budgets, derived after measurement

These are tunable implementation/experiment guardrails, not newly invented product guarantees or owner blockers. Broader sample/Android tests must revise them before release:

- **Catalogue query:** on this same host fixture, bounded prefix/token-page p95≤1 ms at 50k (measured≤0.054 ms, allowing substantial overhead). No full normalized catalogue list delivered to Compose;40-item pages follow the existing UI batch size. Measure combined filters/artist credits/FTS dedup and Android query time before release; do not claim1 ms on phones.
- **Startup:** open catalogue lazily after journal/recovery readiness; zero mandatory network calls and no catalogue rebuild on the startup critical path. In matched emulator runs, investigate median launch regression>20% from 1650 ms (1980 ms) or a repeatable idle-PSS increase>10% from 130428 KiB (~143471 KiB). Confirm with at least30 comparable samples before treating a marginal result as failure; no hardware inference.
- **Storage:** propose configurable **256 MiB total catalogue/cache/offline limit** initially, with pinned content counted and explicit “cannot fit” behavior rather than silent pin eviction. It fits the measured50k prototype (~30.7 MB) plus roughly100 reference bundles (~204 MB) with modest metadata margin; it is not a forecast of rich/global metadata. User can lower/increase within free-space policy; never delete personal data to meet it.
- **Large jobs:** propose explicit confirmation above **8 MiB**, about four modeled reference bundles; preview expanded+staging growth as well as transfer. Unknown size requires explicit bounded approval and pauses before exceeding its ceiling. A changed manifest invalidates approval; user settings can make this stricter. Actual distribution requires broader samples later.
- **Free-space reserve:** proposed minimum128 MiB, and dynamically at least `personalDbBytes + 2 × supportedCriticalExportBytes + otherReservedJournalSpace`; staging/new+previous ready bytes must fit **in addition**. The historical export bound alone can need2×32 MiB. Reserve remains enforced even with a raised catalogue limit. Measure real WAL/export/recovery footprint in Stage 3; this guard must not prevent unchanged-data export when disk is already low.
- **Provider/cache growth:** in-flight coalescing, byte-bounded responses and no automatic prefetch of full graphs. Refresh one accepted entity/document revision, retain old usable data, and stop on overload. Projected raw-response growth is request-count × observed mean size, not unlimited persisted cache; FTS/alias disk cost is visible above. Media byte estimates cannot be derived from JSON-only fixtures.
- **Cloud:** no account/service/paid request created. Direct client provider reads/offline assembly avoid serving large bundles from a5 GB cloud API allowance. Community/backend budgets stay in Stage 1 CLOUD.md pending D4/D5; zero-cost quotas mean optional service denial, never a paid upgrade.

These measurements support local Stage 3 contracts, not claims that Android FTS/Room, extended backups or production bundles already work. Timer fairness under background load, frame timing, full filtered query plans and Android allocation traces are explicit future measurements.

created by stokonn with love <3
