# Stage4 local-content network audit (in progress)

The journal/personal database remains authoritative; a provider failure must not block personal detail/practice. This is a source audit with targeted tests, not a packet capture or complete final privacy acceptance.

| Entry | Observed behavior / required action |
|---|---|
| Discover landing/detail/history | LocalCatalogueStore reads only; portrait reader uses cached manifest/bytes. Explicit active online-search checkbox and permitted policy gate precede provider requests. Detail navigation cannot restart an online supplement. |
| Discover metadata/document/portrait actions | Explicit buttons; approved title evidence, field rights, provider policy and byte/storage guards. Source failure retains accepted evidence. No personal title/note/session field is supplied by the Discover repository. |
| CompositionScreen with personal pieceId | The LaunchedEffect only calls legacy loadWork when pieceId is null. Personal title/composer/notes render from the journal/saved catalogue. Listening/study destinations expand only after the user's action. |
| ComposerPortrait / legacy smallPortrait | Previously corrected to cached-only rendering; explicit supplement retrieval and image consent remain separate. Default rendering cannot initiate provider discovery. |
| Legacy ComposerScreen / catalogue detail | Public numeric provider IDs are validated before lookup. Optional biography discovery uses provider catalogue metadata and requires explicit supplement action. Cached personal snapshots are not promoted into verified rights or raw search input. |
| Repertoire local query | Audit found unconditional catalogModel.query(query), which debounced an Open Opus request, plus startBrowsing on entry. This is a real privacy gap; separate local matching from the online query model, remove automatic network initialization/refresh controls from local repertoire and verify zero HTTP calls. Fix/test pending at initial entry. |
| Legacy CatalogScreens | Explicit catalogue route retains its own online browse/search behavior; no local repertoire filter may be copied into its saved query or active request. |

No personal phone, service credential, network proxy or external account was used. Provider throttling/overload remains a stop condition, not a reason to retry via another host.

created by stokonn with love <3

## Local-filter correction

Source now uses CatalogViewModel.savedResults/filterSaved over local snapshots, separate from online browse query/state and SavedStateHandle. Repertoire entry no longer calls startBrowsing; typing/reset no longer dispatch online query; remote loading/error/retry/continuation controls are removed from this local view. The All/My Repertoire filter, local saved suggestions, favorites and explicit Discover action remain. CZ/EN text explains this boundary. RepertoireSearchPrivacyTest requires saved matching/reset, zero HTTP calls after the old debounce interval and unchanged online/saved query state. Build/execution pending; source inspection is not a passed runtime privacy check.

Executed API37: the zero-HTTP regression and affected navigation/evidence/graph/media/cache matrix passed44/44 (`results/privacy-matrix-api37.log`,21.54s). This verifies the model boundary and current source wiring audit. It is not a packet capture, complete legacy-provider compliance approval or final API26 acceptance. Full acceptance remains open.
