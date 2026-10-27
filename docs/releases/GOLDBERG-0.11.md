# Goldberg 0.11 Stable — The Music Database Update

Android 8.0+ · English/Czech · Ivory/Nocturne · `com.opus.piano` · versionCode 13.

- Discover and Library now read the same canonical catalogue and open the same
  sourced details. Original provider IDs remain distinct; similar titles are not
  silently merged. Existing provider caches are replayed locally with provenance;
  metadata without its original response retains unknown rights.
- Integrated provider search, encyclopedia details, offline bundles and Smart
  Cache. Automatic online search requires the existing default-off Settings
  consent; media downloads retain their network/permission controls.
- Practice history, repertoire, attribution, drafts/recovery, profile, settings,
  badges and portable backups remain local. Personal schema 10, catalogue schema 1
  and backup format 5 are retained; older backup readers remain supported.
- The existing public catalogue API is active for its two read-only routes.
  Its publication projection is currently empty. Backend Google OAuth, community
  writes, live Admin operations and practice-data uploads remain disabled.
- Updated privacy notice and owner-adopted manual support/rights procedure.
  Public developer/contact: stokonn / stokonn.us@gmail.com.

Install `Goldberg-v0.11.apk` over your existing installation. Export a backup
first; do not uninstall to work around a signing mismatch. The existing signing
identity is preserved. Historical v0.11-rc.1 remains unchanged; no new RC was made.

## Verification

- 348 JVM tests passed. Corrected [quality CI](https://github.com/stokonn/Goldberg/actions/runs/40491886948)
  passed build, JVM, instrumentation compilation, lint and data/evidence checks.
- [API36 focused follow-up](https://github.com/stokonn/Goldberg/actions/runs/40491890436):
  6/6 passed, no failures/errors/skips. This includes actual SQLite preservation,
  idempotence, pagination, repair/retry and shared Library/detail UI checks.
  The [initial full run](https://github.com/stokonn/Goldberg/actions/runs/40486014943)
  had 217 cases: 204 passed, two subsequently corrected failures and 11 documented
  external-fixture/separately driven probe skips. Its 200 unaffected passes are
  reused; the focused follow-up is not presented as a new full-suite run.
- Same-source seeded 0.10.1 schema8→10 upgrade, offline/network changes and
  process restart passed: all original rows/preferences and 37 sessions preserved,
  integrity and foreign keys passed. CI used disposable debug rebuilds; release
  certificate compatibility is independently verified.
- Public HTTPS smoke: 10/10 passed once. Both catalogue reads 200; invalid
  cursor/query 400, wrong method 405, private/auth/admin/write paths 404. No-store,
  redaction and unchanged database/security postflight passed.
- Signed release build/lint, package/version, non-debuggable flag, ZIP, exact
  bundled notice and resource-table-resolved artwork checks passed. Inspected
  emulator frames cover both themes, EN/CZ, representative sizes and 1×/2× fonts.
  Small 2× layouts need scrolling to fully reveal the primary action; later
  credits frames verify it is reachable. Navigation labels retain existing wraps.

No physical-device verification, new API26 run, real backend OAuth or community
write acceptance is claimed. Provider-wide log deletion periods and independently
audited Cloudflare invoices remain unverified; no paid resource was enabled.
The public API is not represented as a populated catalogue or journal sync service.

APK SHA-256: `290a15e7b15da701b8f0a40e98bf8b7c1f7cc00ce487504dcb6d4ab2e11b40d6`

Signing certificate SHA-256:
`53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`

Detailed source/artifact and native evidence:
`docs/planning/goldberg-0.11/database-simplifier/results/`.

created by stokonn with love <3
