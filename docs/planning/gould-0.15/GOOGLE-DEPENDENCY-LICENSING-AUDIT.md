# Gould 0.15.0 — Google dependency licensing audit

**Date:** 2026-11-16
**Branch:** `feature/gould-0.15` (worktree `.worktrees/gould-0.15`, HEAD `a908c96d…`)
**Subject:** the Google dependencies bundled into the release APK —
`com.google.android.libraries.identity.googleid:googleid:1.1.1` and
`com.google.android.gms:play-services-auth:21.1.1` (and their transitive
Google closure) — and their compatibility with the owner-approved
GPL-3.0-or-later application license.
**Prepared as:** completion of the licensing half of the Gould 0.15 final
verification, which a previous agent left unfinished.

---

## 1. Verdict

> ### VERDICT: **LEGAL REVIEW REQUIRED**
>
> Keep the combined source/APK release gate **blocked** in the meantime.
> This audit does not clear the gate, does not invent a permission, and does
> not modify `LICENSE`, `LICENSE-DOCS`, `docs/LICENSES.md` or any
> owner-approved licensing decision.

**Why not GO.** Two independent legal questions must both resolve in favour
of distribution, and neither is established by the inspected evidence:

1. whether shipping the compiled Google library code inside a distributed
   APK is permitted by the Android SDK License (§3.4 restricts
   redistribution of "the SDK or any part of the SDK"); and
2. whether that Google code may then form part of the GPL-3.0-covered
   "entire work" — GPLv3 §5(c) requires the whole work to be licensed under
   the GPL, and no Google-granted linking/compatibility permission (GPLv3
   §7-style additional permission) was found anywhere in the inspected
   artifact metadata, SDK terms, or public Google statements.

**Why not a definitive NO-GO either.** A definitive NO-GO would overstate
what this document can prove:

- Google representatives have stated informally (not as a legal grant) that
  Play services/Firebase SDK-license libraries are "intended to be included
  in your apps" (see §7), and Chromium ships `googleid` under the SDK
  License in distributed Chrome builds — so question (1) has substantial
  practice and industry evidence behind it, even though the formal
  clarification request at Google Issue Tracker is still unanswered.
- Question (2) turns on the combined-work-vs-mere-aggregation line and on
  reading GPLv3 §5(c)/§1 against a third-party licence — a genuine legal
  judgment, not a computable fact.

**Therefore:** the honest single verdict is **LEGAL REVIEW REQUIRED** — the
owner obtains a definitive rights/compatibility determination (question 1
and/or 2 above), or removes/replaces the Google sign-in path under a
separately approved scope. Until then the existing gate in
`docs/LICENSES.md` ("Release-blocking Google identity dependency license
question") remains in force, unchanged by this report.

---

## 2. Scope, method, and what was *not* done

**Inspected (read-only):**

- the owner-tested release APK
  `.gould-015-cancellation-build/app/outputs/apk/release/app-release.apk`
  (97,931,121 bytes, SHA-256
  `f3df006e36aa55fb940b5a938cc36737156c3ff77a5c960a9c5da10b53ade8ea` —
  verified in this audit, matching `CHECKPOINT.md`);
- that build's `mapping.txt`, `manifest-merger-release-report.txt`, and
  `lintAnalyzeRelease` intermediates;
- a fresh offline Gradle `releaseRuntimeClasspath` dependency report
  (`.gould-015-release-final-verification/dependencies-report.txt`, exit 0);
- cached POMs/AARs in `~/.gradle/caches/modules-2/files-2.1/`;
- the Android SDK terms, GPLv3 text (`LICENSE`), GNU GPL FAQ;
- `app/build.gradle.kts`, `app/src/main/java/com/opus/piano/identity/GoogleIdentity.kt`.

**Not done (per task constraints):** no app code or Gradle file changed; no
`LICENSE`/`LICENSE-DOCS`/`docs/LICENSES.md` edit; no GPL linking exception
added; no owner decision changed; no git commit/push/merge/tag/PR/release;
no build output, APK, report or database deleted, cleaned or overwritten; no
signing material touched; no physical device accessed.

---

## 3. The four-way distinction (required separation)

| # | Category | Status for Gould |
|---|---|---|
| 1 | **Android SDK build-tool licence** (what you may do *with the SDK* to develop) | Governed by the SDK terms; relevant because the same terms' §3.4 also restrict redistribution of SDK parts. Not itself a licence for bundled code. |
| 2 | **Bundled Google library code inside the APK** (`googleid`, `play-services-*`, `guava listenablefuture`) | **The audit subject.** Declared under the *Android Software Development Kit License* (binary-only, no source published) — except `com.google.guava:listenablefuture:1.0`, which is Apache-2.0. |
| 3 | **Play services as an external platform service** (on-device Google Play services app, server-side APIs) | *Not* redistributed by Gould; calling it raises service/ToS, privacy and network questions, not copyleft questions. Out of scope here. |
| 4 | **androidx first-party libraries** (`credentials`, `credentials-play-services-auth`, etc.) | Apache-2.0 per their POMs — GPL-3.0-compatible; not a blocker. |

Conflating (1) with (2) is the classic error: the SDK terms govern the
developer's use of the SDK, while the *license of the code that ends up in
the APK* is what GPLv3 has to be reconciled with. This report keeps them
apart and also separates **direct vs transitive** dependencies and
**facts vs interpretations vs unknowns** (§8).

---

## 4. Dependency inventory

### 4.1 Graph (configuration `releaseRuntimeClasspath`)

Declared directly in `app/build.gradle.kts`:

```kotlin
37: implementation("androidx.credentials:credentials:1.5.0")
38: implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
39: implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
```

| Artifact | Role | Resolved version | Declared licence (POM) |
|---|---|---|---|
| `androidx.credentials:credentials` | direct | 1.5.0 | Apache-2.0 |
| `androidx.credentials:credentials-play-services-auth` | direct | 1.5.0 | Apache-2.0 |
| `com.google.android.libraries.identity.googleid:googleid` | direct (also requested at 1.1.0 by the androidx artifact, upgraded by the direct pin) | **1.1.1** | Android Software Development Kit License |
| `com.google.android.gms:play-services-auth` | transitive (via androidx credentials-play-services-auth) | **21.1.1** | Android Software Development Kit License |
| `com.google.android.gms:play-services-auth-api-phone` | transitive (via auth) | 18.0.2 | Android SDK License |
| `com.google.android.gms:play-services-auth-base` | transitive (via auth) | 18.0.10 | Android SDK License |
| `com.google.android.gms:play-services-base` | transitive (via auth) | **18.5.0** | Android SDK License |
| `com.google.android.gms:play-services-basement` | transitive | **18.4.0** | Android SDK License |
| `com.google.android.gms:play-services-tasks` | transitive | **18.2.0** | Android SDK License |
| `com.google.android.gms:play-services-fido` | transitive (via auth) | 21.0.0 | Android SDK License |
| `com.google.android.gms:play-services-auth-blockstore` | transitive (via auth) | 16.4.0 | Android SDK License |
| `com.google.android.gms:play-services-identity-credentials` | transitive (via auth) | 16.0.0-alpha02 | Android SDK License |
| `com.google.guava:listenablefuture` | transitive (via `androidx.concurrent:concurrent-futures`) | 1.0 | **Apache-2.0** (inherited from `guava-parent:26.0-android`) |

All ten Google (`com.google.*`) artifacts above declare
`<name>Android Software Development Kit License</name>` with
`<url>https://developer.android.com/studio/terms.html</url>` in their
published POMs.

### 4.2 What actually reaches the owner-tested APK (class-level evidence)

Method: parse `classes.dex` class definitions (4,651 classes, one dex),
reverse-map every dex class through the R8 `mapping.txt` to its original
name and `# {"id":"sourceFile","fileName":"…@@…"}` origin annotation, then
attribute. Result — **299 `com.google.*` classes are packaged**:

| Origin artifact | Packaged classes | Mapping `@@` version | Build-time resolved version |
|---|---|---|---|
| `play-services-fido` | 118 | 21.0.0 | 21.0.0 |
| `play-services-base` | 69 | *18.4.0* | **18.5.0** |
| `play-services-basement` | 56 | *18.3.0* | **18.4.0** |
| `play-services-auth` | 39 | 21.1.1 | 21.1.1 |
| `play-services-auth-blockstore` | 8 | 16.4.0 | 16.4.0 |
| `play-services-tasks` | 5 | *18.1.0* | **18.2.0** |
| `googleid` | 3 (`GetSignInWithGoogleOption`, `GoogleIdTokenCredential`, `GoogleIdTokenParsingException`) | 1.1.1 | 1.1.1 |
| `com.google.guava:listenablefuture` | 1 (`ListenableFuture`, recorded by R8 only as `ListenableFuture.java`) | — | 1.0 (Apache-2.0) |
| **SDK-License subtotal** | **298** | | |

- **Zero** packaged classes from `play-services-auth-api-phone`,
  `play-services-auth-base` and `play-services-identity-credentials` (no
  mapping origin annotations, no dex classes under those packages) — they
  contribute manifest/resources only.
- 74 further classes *with* Google origin annotations were removed by R8
  (372 annotated − 298 packaged); removed classes are not distributed.
- Also packaged, but **Apache-2.0** (not part of the problem):
  `androidx.credentials.playservices` — 20 classes; other
  `androidx.credentials` — 20 classes.
- Direct linkage into app code is real (not dead weight):
  `app/src/main/java/com/opus/piano/identity/GoogleIdentity.kt` L33–37 calls
  `GetSignInWithGoogleOption.Builder(...)`, `CredentialManager.create(...).getCredential(...)`
  and `GoogleIdTokenCredential.createFrom(...)`.

### 4.3 Version-annotation discrepancy — diagnosed, not glossed over

The R8 mapping labels `play-services-base@@18.4.0`,
`play-services-basement@@18.3.0`, `play-services-tasks@@18.1.0`. Those
labels disagree with the resolved graph. Evidence weighed:

- `manifest-merger-release-report.txt` **from the same build that produced
  the audited APK** records `play-services-base:18.5.0`,
  `basement:18.4.0`, `tasks:18.2.0`;
- `lintAnalyzeRelease/release-artifact-libraries.xml` (same build) records
  the same three versions, plus `listenablefuture:1.0`;
- today's fresh offline dependency report resolves the same three versions;
- the Gradle module cache contains `play-services-base` **18.3.0 and 18.5.0
  only** — the annotated 18.4.0 was never a cached variant, and no graph
  node requests `play-services-base:18.4.0`.

**Conclusion (fact):** the three `@@` version labels in the mapping are
unreliable; the packaged versions are 18.5.0 / 18.4.0 / 18.2.0 per two
independent build-time records. **Unknown:** the exact mechanism by which R8
produced those labels. This does not affect the licensing outcome — every
candidate version of these artifacts carries the same SDK-License POM
declaration.

---

## 5. Licence evidence for the bundled Google code

1. **Primary — published POMs** (fetched from Google's Maven and compared
   byte-for-byte with the cached copies used by the build):
   - `googleid-1.1.1.pom` SHA-256
     `02748e4194bbe52d389f934c538355c35fa17facd53724c232b47d7690479773`
   - `play-services-auth-21.1.1.pom` SHA-256
     `de931d61f566f280edf691240059151612c86cfb18384e2eb9cf1ed56e00436c`
   - both declare name *Android Software Development Kit License*, url
     `https://developer.android.com/studio/terms.html`.
2. **Artifact contents:** neither AAR ships a `LICENSE` file of its own.
   `googleid-1.1.1.aar` contains `third_party_licenses.json`/`.txt` whose
   first entry is the full Android SDK terms text (labelled "Android SDK
   Platforms") plus an AndroidX inventory — a build-time notice list, not a
   grant of rights, and **not packaged into the APK** (AAR-root files are
   dropped by AGP; confirmed absent from the APK entry list).
   `play-services-auth-21.1.1.aar` contains only its own
   `third_party_licenses.json`/`.txt` inventory of *its* build/dependency
   notices (Animal Sniffer, Checker Framework, Dagger, Error Prone, Guava
   JDK5/JDK7, HighwayHash, …) — again not its own licence.
3. **Upstream corroboration (a POM alone is not proof):**
   - Chromium's `README.chromium` for
     `com_google_android_libraries_identity_googleid_googleid` records
     `License : Android Software Development Kit License`,
     `Shipped : yes`
     (chromium.googlesource.com, exact URL in §9);
   - Persona's published Android SDK licence table lists
     `com.google.android.gms:play-services-auth:21.1.1` (and phone,
     auth-base, blockstore…) under *Android Software Development Kit
     License*, "Source code not available";
   - deps.dev classifies these artifacts as
     `non-standard ("Android Software Development Kit License")`;
   - mvnrepository mirrors the same POM declaration.
4. **No source repository** for `googleid` or `play-services-auth` is
   published; §3.5 of the SDK terms (OSS-licensed components governed by
   their own licence) therefore has nothing to attach to for these two
   artifacts — they are binary-only under a proprietary licence.
5. **The one exception:** `com.google.guava:listenablefuture:1.0` (1 class
   in the APK) is Apache-2.0, inherited from `guava-parent:26.0-android`
   (verified in the upstream POM). Apache-2.0 is GPLv3-compatible and is not
   a blocker.

---

## 6. Controlling licence texts (verbatim)

### 6.1 Android Software Development Kit License (terms dated July 27, 2021, fetched 2026-11-16 from `https://developer.android.com/studio/terms`)

> **3.1** Subject to the terms of the License Agreement, Google grants you a
> limited, worldwide, royalty-free, non-assignable, non-exclusive, and
> non-sublicensable license to use the SDK **solely to develop applications**
> for compatible implementations of Android.

> **3.4** You may not use the SDK for any purpose not expressly permitted by
> the License Agreement. **Except to the extent required by applicable third
> party licenses, you may not copy (except for backup purposes), modify,
> adapt, redistribute, decompile, reverse engineer, disassemble, or create
> derivative works of the SDK or any part of the SDK.**

> **3.5** Use, reproduction and distribution of components of the SDK
> licensed under an open source software license are governed solely by the
> terms of that open source software license and not the License Agreement.

> **3.8** You agree that you will not remove, obscure, or alter any
> proprietary rights notices (including copyright and trademark notices) that
> may be affixed to or contained within the SDK.

### 6.2 GNU GPLv3 (canonical `LICENSE` in this repository)

> **§1, "System Libraries"** (lines 123–132): The "System Libraries" of an
> executable work include anything, **other than the work as a whole**, that
> (a) is included in the normal form of packaging a Major Component, but
> which is not part of that Major Component, and (b) serves only to enable
> use of the work with that Major Component, or to implement a Standard
> Interface for which an implementation is available to the public in source
> code form. A "Major Component", in this context, means a major essential
> component (kernel, window system, and so on) of the specific operating
> system (if any) on which the executable work runs, or a compiler used to
> produce the work, or an object code interpreter used to run it.

> **§5(c)** (line 222): You must license the entire work, as a whole, under
> this License to anyone who comes into possession of a copy. This License
> will therefore apply, along with any applicable section 7 additional terms,
> to the whole of the work, and all its parts, regardless of how they are
> packaged.

> **§7** (line 343): "Additional permissions" are terms that supplement the
> terms of this License by making exceptions from one or more of its
> conditions… If additional permissions apply only to part of the Program,
> that part may be used separately…

### 6.3 GNU GPL FAQ (https://www.gnu.org/licenses/gpl-faq.html)

> (**#GPLIncompatibleLibs**) "If you want your program to link against a
> library not covered by the system library exception, **you need to provide
> permission to do that**… **Only the copyright holders for the program can
> legally release their software under these terms**… if you want to use
> parts of other GPL-covered programs by other authors in your code, you
> cannot authorize the exception for them. You have to get the approval of
> the copyright holders of those programs… If you're using GPLv3, you can
> accomplish this goal by granting an additional permission under section 7."

> (**#SystemLibraryException**) "If the GPL-incompatible libraries you want
> to use meet the criteria for a system library, then you don't have to do
> anything special to use them; the requirement to distribute source code
> for the whole program does not include those libraries, even if you
> distribute a linked executable containing them."

> (**#WhatDoesCompatMean**) compatibility "means that the other license and
> the GNU GPL are compatible; you can combine code released under the other
> license with code released under the GNU GPL in one larger program… The
> other license is compatible with the GPL if it permits this too."

> (**#InterpreterIncompat**, on implicit exceptions) "…people can take that
> as an implicit exception permitting them to link it with those
> GPL-incompatible facilities… **You can't take someone else's GPL-covered
> code and use it that way, or add such exceptions to it. Only the copyright
> holders of that code can add the exception.**"

---

## 7. Compatibility analysis

### 7.1 Does the GPLv3 System Libraries exception cover the Google code? — **No (high-confidence interpretation)**

The §1 criteria fail on both limbs for code *inside the APK*:

- **"other than the work as a whole"** — the 298 Google classes are compiled
  into `classes.dex` of the Gould APK; they are part of the distributed work,
  not something external to it.
- **(a) "included in the normal form of packaging a Major Component, but not
  part of it"** — a Major Component is a kernel/window system/compiler of
  the OS (or an object-code interpreter). Google identity/play-services
  libraries are not part of packaging the Android OS image or toolchain as
  they ship in an APK.
- **(b) "serves only to enable use of the work with that Major Component"**
  — these libraries implement a product feature (Sign in with Google), not
  interfacing with a kernel/window system.

This is an *interpretation*, but it follows directly from the quoted text,
and the GNU FAQ frames the exception the same way. **Fact:** no Google
component here is a System Library as defined in §1.

### 7.2 Does §5(c) tolerate this combination? — **open legal question**

- GPLv3 §5(c) requires licensing "the entire work, as a whole" under GPLv3,
  "regardless of how they are packaged."
- The Google code is under a proprietary licence that (i) grants use "solely
  to develop applications" (§3.1), (ii) forbids redistribution of "the SDK
  or any part of the SDK" except "to the extent required by applicable third
  party licenses" (§3.4), and (iii) contains no GPLv3 §7 additional
  permission and no linking exception in any inspected file.
- Two readings compete, and this is the heart of the matter:
  - **Reading A (permission exists in practice):** §3.1's grant is meant to
    be exercised by shipping apps; the standard Gradle/Maven workflow
    inherently ships these artifacts in APKs/AABs; Google employees said so
    informally — Firebase issue #933 (2019): *"Obviously our SDKs are
    intended to be included in your apps, as described in the setup guides
    and documentation. We'll look into clarifying the licensing language."*
    and *"It sounds like you can use it in android apps… a lot of Google's
    closed-source Android SDKs are under this license, including Firebase,
    Play Services SDKs"* (explicitly "not legal advice"). Chromium ships
    `googleid` in Chrome. Under Reading A, question (1) in §1 leans
    *permitted*, but **question (2) is untouched**: permission to *ship* an
    app is not the same as Google licensing its code *under GPLv3* for
    §5(c) purposes.
  - **Reading B (restriction bites):** §3.4 literally restricts
    redistribution of any part of the SDK, and the "except to the extent
    required by applicable third party licenses" carve-out most plausibly
    refers to third-party licences that already govern SDK components (the
    §3.5 scenario), not to a downstream copyleft licence the recipient
    chooses — otherwise any redistributor could bootstrap permissions.
- **Who could grant what:** only Google can license *its* code in a way that
  satisfies §5(c)/§7 (GNU FAQ, quoted in §6.3). No such grant was found.
  Conversely, the *Gould* copyright holder could add a GPLv3 §7 exception
  for Gould's own code, but that cannot re-license Google's code and would
  not by itself answer SDK §3.4 — which is exactly why this report does not
  add one (and `docs/LICENSES.md` forbids adding one without rights-holder
  authorization).
- **Open corroboration that Google has *not* answered this publicly:**
  Google Issue Tracker issue **515367456**, "License clarification:
  distribution of apps bundling Play Services & ML Kit artifacts" (filed
  2026-05-21), asks Google precisely whether shipping APKs/AABs containing
  these artifacts is permitted and where the line falls between
  redistributing the SDK and distributing an app that links it — **status:
  New, no answer.**

### 7.3 Practical consequence

Both legs must hold for a GO. Neither is established → publication stays
blocked; the determination is a legal one, not an engineering one.

---

## 8. Facts / interpretations / unknowns

**Facts (verified in this audit):**

- F1 — The owner-tested APK (SHA-256 `f3df006e…`) packages 298
  SDK-License-licensed Google classes from 7 artifacts; `auth-api-phone`,
  `auth-base` and `identity-credentials` contribute none.
- F2 — Every `com.google.*` POM in the graph declares the *Android Software
  Development Kit License* pointing at `developer.android.com/studio/terms`;
  the fetched POMs are byte-identical to the cached ones.
- F3 — Neither Google AAR contains an own LICENSE file or any
  GPL-compatible permission; no source is published for them (§3.5
  inapplicable).
- F4 — The SDK terms' §3.4 restricts redistribution of SDK parts except as
  required by applicable third-party licences; §3.1 grants use "solely to
  develop applications"; §3.8 forbids removing proprietary notices.
- F5 — GPLv3 §1 System Libraries definition does not describe these
  in-APK classes; §5(c) requires whole-work GPL licensing; §7 exists for
  additional permissions.
- F6 — No Google-granted linking/compatibility permission exists in any
  inspected artifact, POM, AAR, or the SDK terms.
- F7 — App code directly calls the googleid/credential-manager API
  (`GoogleIdentity.kt` L33–37); this is not an unused dependency.
- F8 — `listenablefuture:1.0` (1 class) and all androidx.credentials
  artifacts are Apache-2.0 — not blockers.
- F9 — Google's public informal statements (Firebase #933, 2019) say
  SDK-license libraries are *intended to be included in apps*; the formal
  clarification request (Issue Tracker 515367456, 2026) is unanswered.

**Interpretations (labeled, not facts):**

- I1 — The System Libraries exception does not apply (§7.1): strong textual
  fit, still an interpretation.
- I2 — APK bundling under §3.4 is probably permitted by Google in practice
  (industry norm + informal statements), but no formal grant exists.
- I3 — Whether GPLv3 §5(c) is satisfied without Google's §7-style
  permission is the genuinely contested question; plausible readings point
  both ways (§7.2).

**Unknowns (honestly unresolved):**

- U1 — Google's formal position on shipping these artifacts inside
  distributed apps (question open since 2019, still open in 2026).
- U2 — Whether the Gould copyright holder's GPL grant plus these binaries
  constitutes a lawful combined work or a mere aggregation under applicable
  law — a legal-judgment call.
- U3 — Why R8's `@@` version labels differ from build-time resolution for
  three artifacts (§4.3); immaterial to licensing.
- U4 — Whether any separate Google Play services/ML Kit additional terms
  (not reachable during this audit — see §10 failures) contain a relevant
  grant; nothing found, but not exhaustively provable.

---

## 9. Sources (exact URLs)

1. Android SDK terms — https://developer.android.com/studio/terms
   (fetched 2026-11-16, 261,218 bytes; clause text in §6.1)
2. `googleid-1.1.1.pom` —
   https://dl.google.com/dl/android/maven2/com/google/android/libraries/identity/googleid/googleid/1.1.1/googleid-1.1.1.pom
   (SHA-256 `02748e41…79773`, identical to build cache)
3. `play-services-auth-21.1.1.pom` —
   https://dl.google.com/dl/android/maven2/com/google/android/gms/play-services-auth/21.1.1/play-services-auth-21.1.1.pom
   (SHA-256 `de931d61…436c`, identical to build cache)
4. GNU GPLv3 text — repository `LICENSE` (§1 lines 123–132, §5(c) line 222,
   §7 line 343)
5. GNU GPL FAQ — https://www.gnu.org/licenses/gpl-faq.html
   (anchors `#GPLIncompatibleLibs`, `#SystemLibraryException`,
   `#WhatDoesCompatMean`, `#InterpreterIncompat`)
6. Chromium `README.chromium` for googleid —
   https://chromium.googlesource.com/chromium/src/+/lkgr/third_party/android_deps/committed/libs/com_google_android_libraries_identity_googleid_googleid/README.chromium
   (`License : Android Software Development Kit License`, `Shipped : yes`)
7. Persona Android SDK licence table —
   https://docs.withpersona.com/android-sdk-v2-licenses
   (`play-services-auth:21.1.1` … "Source code not available")
8. deps.dev —
   https://deps.dev/maven/com.google.android.gms%3Aplay-services-auth/21.1.1
   and
   https://deps.dev/maven/com.google.android.libraries.identity.googleid%3Agoogleid/1.1.1
   (`non-standard ("Android Software Development Kit License")`)
9. Firebase issue #933 —
   https://github.com/firebase/firebase-android-sdk/issues/933
   (Google employee/contributor comments quoted in §7.2; explicitly not
   legal advice)
10. Google Issue Tracker 515367456 —
    https://issuetracker.google.com/issues/515367456
    ("License clarification: distribution of apps bundling Play Services &
    ML Kit artifacts", status New)
11. `listenablefuture-1.0.pom` —
    https://repo1.maven.org/maven2/com/google/guava/listenablefuture/1.0/listenablefuture-1.0.pom
    and parent
    https://repo1.maven.org/maven2/com/google/guava/guava-parent/26.0-android/guava-parent-26.0-android.pom
    (Apache-2.0)
12. mvnrepository mirror —
    https://mvnrepository.com/artifact/com.google.android.gms/play-services-auth/21.3.0
    (same SDK-Licence POM declaration across versions)

---

## 10. Commands executed, failures, and workarounds

**Executed (representative, all read-only except where noted):**

| Command / action | Result |
|---|---|
| `JAVA_TOOL_OPTIONS="-Djava.io.tmpdir=$PWD/.gould-015-release-final-verification/tmp" ./gradlew … --offline --no-daemon --max-workers=1 -I .gould-015-release-final-verification.init.gradle` (dependency report) | **BUILD SUCCESSFUL, exit 0** → `.gould-015-release-final-verification/dependencies-report.txt` (933 lines; the only new file in the worktree besides this report and the `CHECKPOINT.md` edit) |
| `sha256sum .gould-015-cancellation-build/app/outputs/apk/release/app-release.apk` | `f3df006e…ade8ea`, 97,931,121 bytes — matches owner-tested candidate |
| In-memory Python `zipfile`/dex-class-def/R8-mapping cross-parse (APK, AARs, POMs) | 4,651 dex classes; 298 SDK-License Google classes packaged (§4.2); AAR contents verified without extracting to disk |
| `curl` of both POMs from `dl.google.com`, SHA-256 vs cache | byte-identical |
| `curl` of SDK terms, GNU FAQ, Maven Central POMs, Issue/GitHub pages; targeted web searches | succeeded (see §9) |
| Manifest-merger / lint-intermediate version extraction from the audited build tree | resolved-version evidence for §4.3 |

**Failures encountered (each diagnosed, none treated as success):**

1. `unzip` of the AAR failed with *No space left on device* — the host
   `/tmp` user quota (12,611 MB) is exhausted and **must not be cleared**
   (it may hold preserved evidence). Workaround: all archive inspection was
   done in-memory with Python `zipfile`; no extraction to `/tmp`.
2. Writing `curl -o /tmp/opencode/…` failed (curl exit 23) for the same
   quota reason. Workaround: wrote fetched evidence under
   `~/.local/share/opencode/` instead.
3. An earlier `webfetch` of `https://developer.android.com/studio/terms`
   and of `https://developers.google.com/android/guides/terms` /
   `/android/terms` returned **404** (re-confirmed by curl with a browser
   UA: HTTP 404 pages). Workaround: fetched the canonical
   `https://developer.android.com/studio/terms` with `curl -L` and a
   browser user-agent (HTTP 200, full terms text); the two 404 Play
   services URLs remain unreachable and are recorded as unknown U4.
4. One `curl | python3 - <<EOF` attempt failed because the heredoc consumed
   stdin (my scripting error, curl exit 23); re-run with `-o` file + separate
   parse succeeded.
5. (From the previous agent's unfinished run, for the record) the Gradle
   dependency report originally exited 1; the cause was the same exhausted
   `/tmp` (Gradle's default temp dir). Re-run with an isolated workspace
   tmpdir succeeded with exit 0 — no clean, no build-output deletion.

**Not executed:** no instrumentation, no emulator, no APK rebuild, no
signing operation, no git write operations. Nothing in this audit constitutes
a legal opinion.

---

## 11. Relationship to existing records (nothing contradicted, nothing overwritten)

- `docs/LICENSES.md` §"Release-blocking Google identity dependency license
  question" — **unchanged and still in force.** This report supplies the
  verification it says is missing ("This audit did not establish…") and
  concludes, consistently, that a definitive determination is required.
- `docs/planning/gould-0.15/RELEASE-VERIFICATION.md` — gate 2 (licensing)
  remains **not cleared**; gate 1 (APK byte-identity retest) untouched.
- `docs/planning/gould-0.15/LICENSE-RECOMMENDATION.md` §1.2 ("terms
  unverified here") — the licence identity of the Google artifacts is now
  verified by this report; its recommendation and owner decisions are
  unchanged.
- `docs/planning/gould-0.15/STAGE-CHECKLIST.md` L661 — remains **unchecked**
  because the underlying question ("can these be distributed with the
  GPL-covered application?") is still unresolved by design of this verdict.
- `CHECKPOINT.md` — a short dated entry links this report and states the
  audit status; no earlier content was altered.

**Files changed by this audit:** this report (new),
`docs/planning/gould-0.15/CHECKPOINT.md` (one appended entry), and the new
evidence file `.gould-015-release-final-verification/dependencies-report.txt`.
No other file in the repository was created, modified, or deleted; no git
command with write effect was run.

---

## 12. Addendum — owner release authorization (appended 2026-11-16, after the audit)

The findings, verdict (**LEGAL REVIEW REQUIRED**) and evidence above are
preserved exactly as written; nothing in this addendum resolves, softens or
reverses them.

After the audit was completed, the owner explicitly authorized completing
and publishing Gould 0.15 as a GitHub prerelease despite the unresolved
questions. Verbatim owner instructions of 2026-11-16:

> "The owner explicitly authorizes proceeding with the release despite the
> unresolved Google SDK licensing questions documented in the completed
> audit."

> "Do not invent permissions or claim unresolved licensing questions have
> been resolved."

This is a release decision by the rights holder of Gould's own code. It is
**not** a legal resolution of the questions in §1 and §7, must not be
reported as one, and grants nothing to Google's code. No permission was
found, added or implied for the bundled Google components; unknowns U1–U4
remain unknown. The licensing decisions are unchanged: application source
GPL-3.0-or-later, project-owned documentation CC-BY-SA-4.0. The Google
Sign-In implementation is unchanged in Gould 0.15; its replacement
investigation is deferred to Gould 0.16.

For durable evidence, the dependency report underlying §4 is preserved in
the repository as
`docs/planning/gould-0.15/google-dependency-resolution-report.txt` (copy of
`.gould-015-release-final-verification/dependencies-report.txt`).
