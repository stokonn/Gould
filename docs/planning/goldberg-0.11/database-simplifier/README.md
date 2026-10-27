## Goldberg 0.11 Stable — published and remotely verified

Purpose and scope: complete the approved read-only launch, integrate accepted
Android work and unify Discover/Library catalogue reads while preserving offline
user data. Implementation, verification and publication are complete.

- [x] Stage 6 API activated; the single public security pass passed 10/10 checks.
  Table counts, role flags and disabled intake were unchanged. Owner-confirmed
  mailbox operation and the scoped processing assessment are recorded. The public
  projection remains empty; backend OAuth and community/private-data writes stay off.
- [x] Accepted Stage 7/8/Discover work integrated safely. The dedicated Database
  Simplifier branch started from verified baseline e05c692. Library and Discover
  share canonical reads/details; original identities, caches, metadata and unknown
  rights are retained, with atomic, idempotent retry. No destructive migration:
  opus.db schema 10, catalogue schema 1 and backup format 5 remain compatible.
- [x] 348 JVM tests passed. Quality run 40491886948 passed; unchanged backend run
  40486014854 is reused. The original full native run had 217 cases: 204 passed,
  two subsequently corrected failures and 11 documented skips. Focused run
  40491890436 passed all six affected cases; 200 unaffected passes are reused.
  Seeded 0.10.1 schema 8→10 upgrade/offline/network/restart acceptance preserved
  every original row/preference and 37 sessions, with integrity/foreign-key checks.
- [x] Nine final PNGs inspected across themes, EN/CZ, sizes and font scales;
  three are Library component renders. At small 2x scale the action needs scrolling;
  later credits frames show it fully. Existing navigation wrapping remains.
  No physical-phone execution or new API 26 run is claimed.
- [x] Signed release: com.opus.piano, version 0.11, code 13, non-debuggable.
  Trusted certificate, ZIP, notice, optimized resources, artwork and exclusion
  checks passed. The canceled/unsigned signing attempts remain documented;
  the final secret-free finalizeDsl adapter uses the existing trusted identity.
- [x] Verified implementation committed/pushed and main safely fast-forwarded,
  preserving history. Annotated v0.11 points to ab7d66ddafe0bcf2f23270682116ee2e02f828eb.
  All 195 application/build inputs match accepted signed source a7bb2aa.
  Experimental branches and draft planning PRs were not blindly merged.
- [x] Stable published as latest, with draft/prerelease both false. The signed APK,
  release notes and checksum asset digests match local files; the APK was actually
  downloaded again and its hash matched.
- [x] README, changelog, release documentation and English Wiki synchronized.
  Issue #31 is closed, the existing Project item is Done and milestone 4 is closed.
  The final publication receipt commit contains documentation only; the release
  tag and artifact remain immutable.

Release: https://github.com/stokonn/Goldberg/releases/tag/v0.11
Acceptance: Issue #31 comment 6440551737; publication readback: comment 6440664437.
Evidence: docs/planning/goldberg-0.11/database-simplifier/results/
(final-acceptance.json, stable-artifact.json and publication.json).
APK: .gradle/stable-delivery/Goldberg-v0.11.apk
SHA-256: 290a15e7b15da701b8f0a40e98bf8b7c1f7cc00ce487504dcb6d4ab2e11b40d6
Signer SHA-256: 53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e

No release blocker remains. Evidence does not claim provider-wide deletion
periods or an independent Cloudflare invoice audit. No new RC, paid activation,
key rotation, secret publication, force push or user-data deletion occurred.
Earlier checkpoints below are preserved history, not current pending work.
