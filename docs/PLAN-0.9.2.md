# Goldberg 0.9.2 audit and implementation checklist

Baseline: 9eeabd798dcf8c70766a530f32835e499d68da3b (published 0.9.1). Branch feature/goldberg-0.9.2. Only pre-existing untracked RELEASE_NOTES.md; preserve unchanged. GitHub's canonical repository is now stokonn/Goldberg (origin's Opus URL redirects).

## Audit

- Package/namespace com.opus.piano; version 0.9.1/code 8; next candidate 0.9.2/code 9 subject to final remote verification.
- Native Kotlin/Compose Material 3, Compose Navigation with four top-level destinations and detail routes. Kotlin Compose plugin 2.2.10, AGP 9.1.0, KSP 2.3.12, Gradle 9.3.1, JVM target 17 (host Java 25). SDK min 26 / target and compile 36.
- Central Theme.kt, JournalSpace, Heading, Panel, PieceRowContent and Material controls exist. Home still starts practice and lacks quotations/activity. Profile still scrolls and has Customize/edit-showcase interactions. Settings subpages share large cards. Repertoire search/add are separate and unpinned. Detail actions start practice indirectly. Bach's Secret uses disconnected buttons.
- Room opus.db schema 6, explicit migrations 1→6, no destructive fallback. PracticeCoordinator is the application-scoped mutex/single-writer state machine; initial write gates running state. Recovery retry/export exists, but fresh-after-export currently clears the original draft: change to archive preservation before any fresh-session workflow. Never change timing calculations without regression evidence.
- ProfileSettings stores local nickname, bounded base64 avatar, appearance, showcase and Smart preferences. BadgeUnlock retains acquisition dates; showcase max five. Add personalization separately without changing portable Room backup formats.
- CatalogComposer/CatalogWork and Piece models are established; no catalogue expansion. HTTPS HttpURLConnection client, allowlisted hosts, transactional normalized cache; Wikimedia identity checks and reusable-license metadata. Portrait cache currently allows metered downloads automatically and fetches full profiles on detail entry: introduce explicit media policy and optional profile downloads.
- CZ/EN resources with plural parity checks; L and JournalFormatting provide application locale. Backup formats 1/2 import into isolated Room validation then transactional restore; CSV separates session/part totals. Preserve these contracts.
- Existing host suite: 63 JVM tests; SQLite migration/fixture, localization and paging scripts. Android persistence/navigation/locale tests compile. Accelerated emulator is now available outside sandbox (/dev/kvm); only emulator serials may be used for ADB.
- Initial audit found no OAuth setup. The owner subsequently supplied a real Web client ID and registered an Android client; Credential Manager + Google ID are implemented. The owner reports successful real Google authentication on a Galaxy A55 (not Codex device testing); all six previously outstanding account behaviors were subsequently confirmed as PASS — owner-reported manual verification. Local profiles remain fully usable.
- Initial audit found Projects scope and Wiki initialization missing. The owner subsequently completed both. The permanent Project, 17 Issues, milestones and English Wiki are now created; Wiki publication is verified independently. Board layout and Status grouping were verified on 2026-10-17; the existing table layout was changed to a board through the supported API.
- Claude/Gemini/Cursor executables not found in PATH. Validate documented wrapper syntax, report runtime integrations untested; do not install agents merely to test wrappers.

## Approach and acceptance checklist

- [x] Canonical permanent AGENTS.md and thin agent wrappers; syntax/provenance validation and Developer Guide.
- [x] Inspect/create missing Issues, labels and milestones; reuse one Project when permission is available. Keep actual statuses truthful.
- [x] Central palette/semantic tokens, compact shared headers/cards/settings rows and accessible adaptive layouts.
- [x] Home: today's time/streak, real seven-day chart, stable daily quotation, recent piece details; no Start action.
- [x] Exactly ten public-domain sourced quotations, explicit attribution/translation provenance; bounded daily network attempt/cache and offline tests.
- [x] Repertoire pinned search/+ row and compact Sort/Filters; cached licensed small composer portraits with placeholders.
- [x] Practice as only start entry; details only select/navigate. Preserve recovery originals, regression-test archive failure/retry.
- [x] Profile compact avatar/name/settings/badges, four metrics, manual favorites/bio, five-badge showcase; manage from collection. Google integration must pass real provider verification before release.
- [x] Media policy: Wi-Fi auto small images, metered/cellular explicit opt-in, cached offline, optional supplementary download/status/removal, isolated bounded storage.
- [x] Compact Settings with clear Data/Storage sections and existing backup/restore warnings.
- [x] Connected chromatic keyboard preserving audio; separate offline long-press logo Tux and exact signature.
- [x] CZ/EN resources and accessibility; no rewriting user content.
- [x] Host tests, lint, debug/instrumentation APK; actual emulator UI screenshots across sizes/themes/font scales; seeded emulator in-place upgrade from 0.9.1; network scenarios.
- [x] English Wiki drafts and README documenting only implemented features; publish Wiki after gates and initialization.
- [x] Verify APK package/version/signature/source identity and acceptance evidence. Publish a normal 0.9.2 release only when all mandatory gates pass; otherwise leave release unpublished and document blockers.

## Additional quality work after owner authentication report

- Recorded the owner’s successful installation/authentication separately in MANUAL-VERIFICATION-0.9.2.md; no extra account behavior is inferred.
- Host build/JVM/lint and data/localization checks revalidated; no physical-phone commands.
- Fixed composer collection/sort controls using translated values as filter identifiers; Choice now retains stable identifiers and translates presentation. Czech emulator regression verification passed.
- Live composer optional-content download and offline disk-cache UI verification were checked separately from deterministic HTTP-fixture tests.

## Final owner-verification and publication checklist

- [x] Recover the existing feature branch and preserve unrelated RELEASE_NOTES.md.
- [x] Verify the final candidate and 139-file source manifest match the 70-JVM/31-Android tested source.
- [x] Record six newly confirmed Google scenarios as PASS — owner-reported manual verification.
- [x] Resolve remaining UI/offline acceptance gaps and inspect Issues #7/#16/#17.
- [x] Verify compatible APK/signature/version and prepare accurate release notes.
- [ ] Commit/push verified source, safely fast-forward main, annotate v0.9.2 and publish the normal release.
- [ ] Verify uploaded asset identity; synchronize English Wiki, Issues, milestone and permanent Project.
