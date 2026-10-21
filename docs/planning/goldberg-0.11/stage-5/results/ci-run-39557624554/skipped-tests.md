# Skipped tests in run 39557624554 (and all prior runs) — investigation

**Result: both skips are designed JUnit assumption-skips for fixtures that must NEVER
be bundled into either APK (licensing/provenance policy). They are identical in all
four runs that executed the suite (3/4/5/6): 177 total, 0 failures, 0 errors, 2
skipped — the same two tests every time. Not failures, not masked, not order-dependent.**

## The two skipped tests

### 1. `Stage4ExternalProviderTest :: boundedObservedProviderResponsesPassNativeProjectionAndSearchPaging`

Source (`app/src/androidTest/java/com/opus/piano/Stage4ExternalProviderTest.kt:17`):

```kotlin
import org.junit.Assume.assumeTrue
…
assumeTrue("Requires separately acquired bounded public provider responses", path != null)
```

Skips when the separately-acquired observed provider-response fixture file is absent
(instrumentation argument `path`). CI supplies no such fixture → assumption → skip.

### 2. `Stage4ExternalPortraitTest :: separatelyAcquiredLicensedOriginalCanBeVerifiedAndRenderedOffline`

Source (`app/src/androidTest/java/com/opus/piano/Stage4ExternalPortraitTest.kt:26`):

```kotlin
/** Opt-in acceptance of a separately acquired public original; never bundled into either APK. */
assumeTrue("Requires separately acquired original and per-file metadata",
    args.containsKey("stage4PortraitMetadata") && args.containsKey("stage4PortraitOriginal"))
```

Skips unless instrumentation args provide the separately-acquired licensed portrait
original + metadata. KDoc states the fixture is **never bundled into either APK by
design** (licensed content must not ship with the app or test APK).

## Verification of the investigation

- The skip set parsed from the authoritative per-run JUnit XML (`<testcase>…<skipped`)
  is **byte-identical across runs 3, 4, 5 and 6** — runs:
  `ci-run-39511602936`, `ci-run-39518973833`, `ci-run-39552116455`,
  `ci-run-39557624554`. A first, looser parse of run 6 that appeared to show a
  different pair (`AppLanguageTest` instead of `Stage4ExternalPortraitTest`) was a
  REGEX BUG in the ad-hoc extraction script (non-greedy `<testcase…</testcase>`
  pairing crossed nested tags); the strict `<testcase name=… classname=…>\s*<skipped`
  parse above is consistent across all runs.
- `AppLanguageTest :: languageOverrideUsesAppCompatAndCanReturnToSystem` **RAN and
  PASSED** in run 6 (`time="9.226"`, self-closing `/>` = pass in the XML).
- Neither skip produces a `<failure>`; both appear under `Skipped: 2` in
  `instrumentation-counts.txt`.

## Consequence for Stage 5 acceptance

No Stage 5 behavior is hidden by these skips: they are Stage 4 external-provider/
portrait opt-in acceptance tests whose fixtures are excluded from both APKs by
licensing policy. They remain skipped **by design** on every CI run; if the owner
later acquires the separately-licensed fixture files, they can be supplied via
instrumentation arguments to execute them (opt-in, not a Stage 5 requirement).

Evidence: this run's `instrumentation-TEST-results.xml`,
`instrumentation-counts.txt`, `instrumentation-logcat.txt`; fixture conditions quoted
from the branch source at `f9f59d1`.
