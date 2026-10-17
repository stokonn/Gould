# Goldberg 0.10 — verification and visual review

Status: **published as the normal Goldberg 0.10 GitHub Release** at https://github.com/stokonn/Goldberg/releases/tag/v0.10. The annotated `v0.10` tag points to source commit `7b5874e8ffd2d01eec8de9ad58cf64e195cf0a5b` on `main`. The owner explicitly waived personal screenshot review; minor differences below are candidates for 0.10.1. Google identity results are owner-reported manual verification, not Codex tests. No physical phone was accessed by an agent.

## Source and artifact

- Branch: `feature/goldberg-0.10`; based on published v0.9.2 commit `78fd88fbf4ead8772b87d2b986807a381241b7dd`.
- APK: `app/build/outputs/apk/debug/app-debug.apk`.
- Published asset: `Goldberg-v0.10.apk`; downloaded release bytes were compared against the tested artifact.
- Metadata: `com.opus.piano`, version `0.10`, code `10`.
- Final APK SHA-256: `20a5962cba89075a0d998b156097ddff86a22de1a0a2ac8ef636a8ff963e73d3`.
- APK signing certificate SHA-256: `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`, matching the trusted 0.9.2 identity.
- Debug and instrumentation APKs installed successfully on the explicitly targeted emulator `emulator-5582`. The reference package remains under `docs/design/` and is not an app resource.
- Unrelated `RELEASE_NOTES.md` is preserved unchanged.
- The release asset API reports the same SHA-256; `cmp` of the downloaded APK and local artifact passed.

## Checks actually run

The final source at this checkpoint passed:

- `GRADLE_USER_HOME=/tmp/goldberg-gradle-home ./gradlew --offline assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug --no-daemon --max-workers=1` — `BUILD SUCCESSFUL` (83 tasks, 5 executed and 78 up-to-date).
- `GRADLE_USER_HOME=/tmp/goldberg-gradle-home ./gradlew --offline testDebugUnitTest --rerun-tasks --no-daemon --max-workers=1` — `BUILD SUCCESSFUL`; all 72 JVM tests executed with zero failures/errors/skips.
- 72 JVM tests — 72 passed, zero failures/errors/skips.
- Android instrumentation APK compilation — passed, and tests were separately **executed** on emulator-5582: 36 passed (`OK (36 tests)`).
- `python scripts/check_migrations.py` — schemas 1–7 to 8 pass with original portable values retained.
- `python scripts/check_backup_fixtures.py` — backup formats 1–3 / schemas 4, 6 and 7 pass into schema 8.
- `python scripts/check_history_paging.py` — bounded pages, tied timestamps, legacy attribution, deduplication and untimed associations pass.
- `python scripts/check_localization.py` — 790 English/Czech resources and matching placeholders/plurals pass.
- `python scripts/check_agent_policy.py` — canonical policy and local Wiki navigation pass; this does not test the runtime loading behavior of each AI agent.
- `git diff --check` — pass.
- Android lint — 0 errors, 78 warnings. Categories: 44 unused resources, 14 dependency freshness, 9 KTX recommendations, 3 modifier parameter recommendations, 2 private-resource references, and one each for SharedPreferences, target API, unused attribute, AGP version, untranslatable value and obsolete SDK check. These are non-failing cleanup/freshness warnings; no lint error or identified release-critical defect remains. See `app/build/reports/lint-results-debug.txt`.

The first fresh Gradle attempt was stopped before build configuration because the sandbox blocked writes to shared `~/.gradle`. A writable temporary Gradle home resolved that filesystem issue. The in-sandbox Gradle process also could not enumerate network interfaces for its file-lock service; the cached offline Gradle invocation then completed. An earlier emulator command had returned exit code 1 after reporting successful APK installation: the failing phase was `am instrument`, where stale backup-format and Room-index test assertions failed. Those assertions were corrected; the final emulator execution above passed all 36 tests. This was not an APK installation failure.

## Data compatibility

Schema 8 is additive. `favorite_composers` stores local Repertoire pins independently of Profile's single Favorite Artist. `session_piece_links` stores added historical piece associations without a duration; editing does not rewrite a completed session's ID, duration, timestamps, measured parts, intervals, notes or badge records. Backup format 4 is intentional: it serializes these new tables and the reader retains compatibility with formats 1–3. The corrected format-4 round-trip assertion and persistence test cover the current format; fixtures for historical formats 1–3 and migrations from schemas 1–7 pass. The earlier seeded in-place 0.9.2-to-0.10 upgrade on the isolated emulator retained its controlled schema-7 sessions, profile, preferences and badge data; this is emulator fixture evidence, not real-owner data or a physical installation test.

## Visual comparison performed

All seven supplied screen references were inspected. `01_HOME_APPROVED.png` and `02_PROFILE_APPROVED.png` were treated as the latest approved references. The following final-candidate emulator captures were inspected at normal size in dark and light themes, and the four main tabs at 130% system font scale:

| Screen | Captures | Comparison and remaining difference |
| --- | --- | --- |
| Home | `home-final.png`, `home-light.png`, `home-font130.png` | Matches the approved greeting/name, separate typography-only quote, two metrics, real seven-day chart, recent sessions and gear hierarchy. It uses a real long name, which wraps to two lines, and omits the mockup's unsupported daily goal. The quote is ellipsized at normal size but opens in a full-quote dialog; with enlarged type it moves below the greeting and occupies more height. The 130% layout scrolls, so only the first recent rows fit above navigation. |
| Repertoire | `repertoire-final.png`, `repertoire-light.png`, `repertoire-font130.png` | Search and Add share a row; Sort and Filters remain side-by-side; My Pieces/Catalogue are retained. Long piece names wrap. Portraits are rounded-rectangle placeholders because the controlled emulator catalogue has no cached/licensed portrait assets. The two-piece fixture leaves substantial empty list area and has no pinned-composer strip because none were selected. These are data-dependent, not fabricated content. |
| Practice | `practice-final.png`, `practice-light.png`, `practice-font130.png`, `practice-standard-ime.png` | Timer/status, Smart Practice, collapsed piece/notes section and bottom-anchored Start/Finish controls remain legible. With standard docked Gboard open, the focused notes field and primary controls stay visible above the keyboard; navigation remains above the IME. The emulator stylus-writing tutorial was disabled in emulator settings to capture the regular keyboard. No application inset change was necessary. The ready-state page leaves more open space than the mockup. |
| Profile | `profile-final.png`, `profile-light.png`, `profile-font130.png` | Prominent identity, gear and badge route, four meaningful stats, favorite artist, bio, showcase and history follow the latest concept. The statistics now use equal-height aligned cards; at 130% they become a readable 2×2 grid and lower sections scroll. The controlled final capture includes a licensed Bach portrait. No fictional profile tags or interests were added. |
| Settings / Appearance | `settings-final.png`, `appearance-final.png`, `appearance-light.png`, `appearance-final-lower.png`, `appearance-final-bottom.png` | Settings categories are grouped; Appearance preserves system/light/dark, Goldberg/system font, accent, card style, density and sort. The lower appearance groups require scrolling; controls are full-width with accessible height. Screenshots are partial viewport captures, not a full-page visual export. |
| Completed-session edit | `history-final.png`, `history-review-final.png`, `history-add-piece-final.png` | Incremental history rows and the edit/add-piece surface were rendered. The editor shows the preserved session duration and notes, explains that new piece links do not add practice credit, and offers only existing repertoire pieces. The picker uses the local rectangular-portrait fallback. |
| Favorite Artist autocomplete | `autocomplete-suggestions-final.png`, `autocomplete-selected-final.png`; approved concept `07_FAVORITE_ARTIST_AUTOCOMPLETE_DETAIL.png` | Entering “Chopin” displays the local “Frédéric Chopin” suggestion with the rectangular portrait placeholder. Selecting it populated the field with the full composer name; the Compose UI hierarchy confirmed the selected text. No network catalogue search was used. |

The candidate honors four bottom tabs and the top-right Settings gear, unlike illustrative references with five tabs. The design references are conceptual; actual screen hierarchy and typography are applied through Goldberg's existing Compose/theme system. No image reference content was copied into the APK.

## Runtime scenarios not established here

**Owner-reported manual Google identity verification on Goldberg 0.10 — PASS** on the Samsung Galaxy A55. These were performed by the owner, not Codex, and are not automated tests or emulator results. Codex did not access the physical phone.

| Scenario | Result | Owner-reported outcome |
| --- | --- | --- |
| Google sign-in | PASS — owner-reported manual verification | Authentication succeeded. |
| Display-name import | PASS — owner-reported manual verification | Google name imported and displayed. |
| Profile-picture import | PASS — owner-reported manual verification | Picture imported after media consent. |
| Manual nickname/avatar overrides | PASS — owner-reported manual verification | Existing manual choices remained preserved. |
| Restart persistence | PASS — owner-reported manual verification | Authentication and identity persisted after restart. |
| Sign-out and account switching | PASS — owner-reported manual verification | Both worked without losing local practice data. |

The separate emulator attempt reached Google's account-entry screen without an account configured; no credentials were entered there.
- Fake-transport Android tests cover daily quote caching/offline fallback and media consent, successful cached image reuse, denied consent, cache clear and failed/revoked downloads. The consent policy matrix is also covered by JVM tests. Live Wi-Fi, live mobile-data-with-consent, real offline network transitions and real licensed portrait retrieval were **not** performed.
- No automated accessibility audit, keyboard/open-IME screenshot matrix, alternate emulator size matrix, real image catalog population, audio playback, screen-off Smart Practice, or physical-device test was performed.
- Emulator display used for screenshots: 1080×2400; 130% font scale was set on that emulator. 0.10 in-place upgrade used a controlled fixture, not a real owner's data set.

## Remaining visual differences and gate

1. The Practice ready-state leaves more open space than the populated mockup and uses the application's current timer card rather than a decorative progress ring.
2. Repertoire/Profile portraits are placeholders on this offline fixture, and Repertoire looks sparse with two pieces and no pinned favorites.
3. Long-name and enlarged-font layouts use a vertical scrolling fallback; Home's quote moves below its greeting at 130% font.
4. The completed-session editor and artist autocomplete still need visual review from dedicated interaction captures.

The owner waived personal screenshot review and authorized judgment on minor differences. These can be considered for 0.10.1: open space in ready-state Practice, sparse data-dependent Repertoire, and scrolling at enlarged font sizes. Autocomplete and keyboard interactions now have actual emulator captures. Google identity verification is reported by the owner as described above. No physical-device test was performed by Codex.
