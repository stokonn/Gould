# Gould 0.12 Stable — The Practice Studio Update

Android 8.0+ · English/Czech · Ivory/Nocturne · `com.opus.piano` · versionCode 14.

- **Studio**: record local practice takes through one explicit Record → Stop
  flow with truthful preparing/recording/finalizing states. `Previous` lists
  Practice History and Recordings with live, interrupted and saved takes;
  playback, export and delete stay explicit, and recovery never restarts the
  microphone automatically.
- **Metronome and queue**: compact metronome now plays through the media audio
  volume with proper focus, route and lifecycle handling; multi-piece queue with
  manual switching, safe Undo and notes; accent/neutral Stop shortcut.
- **Four launcher widgets**: Quick Practice, Weekly Activity, Monday–Sunday
  activity and the adaptive widget add from the launcher with offline snapshots,
  stale/unavailable states, honest accessibility descriptions and bounded
  refresh (`updatePeriodMillis=0` plus a bounded WorkManager pass). Widget taps
  navigate safely and never start practice, recording or capture on their own.
- **Profile and achievements**: centered 2×2 statistics with adaptive reflow,
  local era autocomplete and a dirty-exit guard; the original 17 badges, earned
  dates, art and showcase are preserved, joined by five genuine new awards plus
  Founding Pianist, with queued unlock popups that survive restart.
- **Data**: additive personal schema 10 → 11 (local recordings/segments,
  achievement events, popup acknowledgements) and backup format 6 with JSON and
  journal/audio ZIP export/restore; older backups remain importable. Practice
  journal, repertoire, attribution, drafts, profile, settings and media stay on
  the device.
- **Polish**: faithful adaptive/monochrome/legacy launcher icon, bundled
  licensed Tux font rendering (Samsung-safe ASCII), Library `Previous` heading
  alignment, original landscape artwork in the Home quote card, EN/CZ
  localization and Android font scaling through 200%.

The catalogue/backend boundary from 0.11 is unchanged: read-only catalogue
routes, default-off online search consent, no community writes or practice-data
upload. Signing identity, application ID and database name are unchanged.

Install `Gould-v0.12.apk` over your existing installation. Export a backup
first; do not uninstall to work around a signing mismatch.

## Verification

- Required CI is green for the release source: emulator acceptance run
  [`40966258525`](https://github.com/stokonn/Gould/actions/runs/40966258525) at
  `46db844` (instrumentation 5.10 PASS, release-acceptance subset 5.11 PASS) and
  the required quality/maintenance/BFF workflows 3/3 success at `38825d7`
  (`40967492484`, `40967492439`, `40967492411`). 393 host JVM tests (48 suites,
  0 failures) and the full API36 native acceptance suite back the same source;
  `38825d7` is docs-only versus `46db844`.
- **Trusted in-place upgrade PASS**: the published `Goldberg-v0.11.apk`
  (SHA-256 `290a15e7…`) upgraded in place to this build on a disposable API36
  emulator. All persistent rows unchanged — 37 sessions, notes, profile, badges
  and dates, repertoire, recovery archive, paused draft bytes and appearance
  preferences retained; schema 11, integrity `ok`, foreign keys pass, no
  manufactured audio/events, personal-catalogue bridge intact
  ([evidence](../planning/gould-0.12/verification/trusted-upgrade-011-to-012.json)).
- **Performance PASS**: matched 30-sample startup A/B on the same
  emulator/fixture/renderer — 0.11 median 1623 ms vs 0.12 median 1587 ms
  (no regression, both below the 1980 ms investigation threshold); idle PSS
  64584–66715 KiB vs the 143471 KiB threshold; frame percentiles improved
  (p90 1400 vs 2000 ms); storage shows no abnormal growth
  ([evidence](../planning/gould-0.12/verification/performance-gate.json)).
- **Release artifact gates PASS**: clean `assembleRelease` + `lintRelease` with
  the preserved 0.11 signing init script, non-debuggable, versionCode 14 /
  versionName 0.12, permission set byte-identical to published 0.11, ZIP
  integrity, v2 signature parity, packaged artwork checks, zero secret patterns,
  no keystore material and all 15 private design references absent
  ([evidence](../planning/gould-0.12/verification/release-artifact-gate.json)).
- **Security/privacy/licensing PASS**: manifest delta limited to app category,
  string label and two exported=false widget receivers; `allowBackup=false`
  retained; no dependency changes; bundled legal/OFL/Apache notices and font/art
  hashes verified
  ([evidence](../planning/gould-0.12/verification/security-privacy-licensing-gate.json)).
- The owner personally verified the three Samsung blockers (metronome media
  routing, Tux ASCII rendering, Library Previous alignment) on his Galaxy A55 —
  owner-reported manual acceptance recorded in
  [Issue #41](https://github.com/stokonn/Gould/issues/41#issuecomment-6505538907);
  not agent-operated device testing.

No agent physical-device verification, acoustic-piano fidelity, API37 run or
real backend OAuth/community acceptance is claimed. Emulator performance numbers
were measured under a software renderer because the host GPU driver crashes the
emulator (documented in the performance evidence); the startup A/B is matched on
both sides. Widget wakeups reuse the already-accepted bounded-refresh evidence
rather than a new measurement. The public catalogue API keeps its empty
projection.

APK SHA-256: `14f4ef28e13901f0dbbf54510fb9b5bd668e35b0a409e9e14d057bae00d932e8`

Signing certificate SHA-256:
`53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`

created by stokonn with love <3
