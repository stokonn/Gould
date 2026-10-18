# Bounded source and license investigation

Research date 2026-10-18. Primary documentation plus **25 bounded API requests** (23 initial + two revision-bearing REST probes), no account/secret/personal query, no bulk scrape. Initial requests were serialized with at least three seconds between starts for each provider, a 20s timeout and 2 MiB response bound. Full logs: [source-observations.json](results/source-observations.json); only small CC0 projections are committed. A query candidate is not a verified work identity or genre assertion.

## Sample and observed coverage

| Sampling stratum | Query | MusicBrainz work candidates (limit 3) | Open Opus / Wikidata |
|---|---|---:|---|
| classical | Goldberg Variations | 3 | Open Opus found work 10074 + Bach 87; first MB matches included “New Goldberg Variations” and an Aria movement, not proven equivalent to the parent work. |
| jazz | Take Five | 3 | Several identical titles with distinct MBIDs; creator/version verification still required. |
| blues | Saint Louis Blues | 2 | Search returned related/live/derivative titles, not proof of the intended standard. |
| pop | Yesterday | 3 | Open Opus sample returned no matching work; repeated titles are ambiguous. |
| rock | Bohemian Rhapsody | 3 | Distinct candidates; no automatic merge. |
| film | Hedwig theme | 3 | Original/combined/arrangement-style titles require typed relationships. |
| game | Zelda lullaby | 3 | Multiple candidates with apostrophe variants; no verified original/arrangement mapping. |
| additional/folk | Greensleeves | 3 | Traditional tune/version ambiguity remains. |

These are **23 MusicBrainz candidates plus one Open Opus work projection**, not 24 verified canonical compositions. Eight of eight query strata returned some MusicBrainz candidate; this is a retrieval hit rate, not source completeness/identity precision. Open Opus was sampled only twice and is classical-focused; no claim of broad-genre coverage follows. Artist/release/recording/collection live coverage was not sampled; those relationships are represented by authored fixtures. More rare genres, non-Latin scripts, bands, arrangements, new artists, independent game scores and incomplete local Wikipedia editions remain coverage gaps; approved scope is not reduced.

All eight Wikidata queries returned **HTTP200 JSON maxlag**, so identity/alias/language coverage is **unknown/unavailable**, not zero. The first exploratory loop did not yet stop on JSON overload and sent eight distinct bounded queries; this is recorded, not concealed as success. [collect_sources.py](collect_sources.py) was corrected to defer a provider on JSON maxlag/ratelimited/readonly as well as HTTP429/503. No further Wikidata retry, alternative-IP bypass or removal of maxlag was attempted. Czech Action API revision lookup also returned maxlag; later separate REST probes are documented independently, not represented as successful Action API calls.

## Field/license allowlists

Normative default-deny projection: [source-field-policy.json](source-field-policy.json). Every permitted field still requires provenance and entity type validation; permit-list membership is not identity proof.

- **Open Opus:** publisher's [data dedication](https://openopus.org/) links CC0; allow catalogue/composer factual fields only. API portrait URLs do not license images. Record response hashes because API version 1.20.6 is software, not a work revision. The observed search response's rows value differed from array count; parse entries, not advertised row count.
- **MusicBrainz:** direct Data_License/Database documentation opens returned429. An accessible primary [Download/license page](https://musicbrainz.org/doc/MusicBrainz_Database/Download) explicitly separates core `mbdump` under CC0 from derived data under CC BY-NC-SA 3.0, and identifies annotations/user ratings/tags/search indexes as derived; genre associations depend on user tags. WikiDocs transcluded revision **77812** was visible. [Core schema](https://musicbrainz.org/doc/MusicBrainz_Database/Schema) supplies entity/table structure. The allowlist projects typed core identifiers/names/date/credits/track structure; it excludes genre-to-entity tags, ratings, annotations, editor/history, cover-art/event-art mapping datasets and unknown fields. Only IDs/titles (and Open Opus composer facts) are redistributed in real fixtures. Further nested fields need their own review before adapter expansion; do not import search-engine indexes or assume API ranking is reusable factual data.
- **Wikidata:** [structured-data CC0 policy](https://www.wikidata.org/wiki/Wikidata:Licensing) covers item labels/aliases/descriptions/claims/qualifiers/references/sitelinks. Preserve rank/date precision/reference provenance and lastrevid. Linked prose/media and non-item namespace text have separate rights. No actual QID mapping was established in this run.
- **Wikipedia:** general [terms](https://foundation.wikimedia.org/wiki/Policy:Terms_of_Use) and per-response license govern text reuse. English and Czech REST responses both returned CC BY-SA 4.0, revision and timestamp tied to their own HTML. Offline copies/extracts need article/revision/history attribution, license and adaptation notice; no broad CC0 relicensing. Actual article prose/HTML is **not committed**; only hashes, sizes and license observations are recorded. Authored Czech/English test text is explicitly synthetic, not a fabricated real biography.
- **Commons:** [per-file reuse policy](https://commons.wikimedia.org/wiki/Commons:Reusing_content_outside_Wikimedia) applies. A Bach portrait metadata probe returned author Elias Gottlob Haussmann, a public-domain label, no LicenseUrl, blank Restrictions, original size630150 bytes and a nominal480px request rounded by the provider to a500px URL. This is insufficient for the new strict reusable-asset gate without a recorded public-domain rationale/file review. **No image bytes were downloaded or distributed.** Original metadata size is used only for a labeled cost model. Never treat it as measured thumbnail transfer size. Commons metadata source page: https://commons.wikimedia.org/wiki/File:Johann_Sebastian_Bach.jpg .

The project-wide application-source license is not invented or changed. Newly authored test data is explicitly dedicated to CC0 in [fixture notes](fixtures/README.md); code/documentation rights elsewhere are unaffected. Provider facts retain source URLs/hashes/dedications. Unknown rights block redistribution without blocking local practice.

## Wikipedia endpoint lifecycle and Czech/English behavior

Current [MediaWiki REST reference](https://www.mediawiki.org/wiki/API:REST_API/Reference) documents `w/rest.php/v1/page/{title}/with_html` and `revision/{id}/with_html`. Both article probes succeeded:

| Language | Returned revision | Response bytes / HTML bytes | License |
|---|---:|---:|---|
| en | 1380548329 | 1, 171, 228 / 1, 092, 181 | CC BY-SA 4.0, response license URL |
| cs | 25698610 | 201, 707 / 189, 470 | CC BY-SA 4.0, response license URL |

Responses included ETags bound to their revisions and `Cache-Control: max-age=5`; that cache directive is not a copyright retention limit or a promise content cannot change. Persist response revision/hash together. Prefer the immutable revision route after discovery for a stable downloaded document; its request shape is documented but that separate route was not live-probed. Do not claim atomic association by combining independent latest-content and latest-revision requests.

Legacy PCS `/api/rest_v1/page/summary/...` also returned a revision and short extracts (840 bytes EN/384 bytes CS), useful for previews but not full encyclopedias; actual article texts were not committed. [PCS documentation](https://www.mediawiki.org/wiki/Page_Content_Service) still describes summaries. [TextExtracts](https://www.mediawiki.org/wiki/Extension:TextExtracts) has maintenance concerns, so keep it only as an explicitly tested compatibility fallback. No claim is made that every RESTBase path is removed, stable forever or licensed differently. Observe lifecycle changes and test both languages before Stage 4 rollout.

If verified Czech content is missing/blocked/unavailable, choose approved English with an explicit language/fallback indicator; if neither is verified show metadata/source links and an empty state. No automatic translation. Rights/identity uncertainty, not transport success alone, determines whether a document may be rendered or included offline.

## Remaining provider gates

Wikidata is currently overload-blocked; no live cross-provider mapping is certified. Real releases/recordings/groups and additional genres need a later bounded identity-validation sample before broad ingestion. Rate/backoff/cancellation and external rights policies must be implemented/tested in Stage 4; this research collector is not an Android network client. MetaBrainz service-use/commercial distribution terms and Wikimedia load policy remain distinct from CC0 field reuse; the strict budget authorizes no paid feed/proxy. Sources have no assumed SLA. None of these source-availability gates prevents separately instructed Stage 3 local persistence with synthetic/CC0 fixtures and no network migration.

created by stokonn with love <3
