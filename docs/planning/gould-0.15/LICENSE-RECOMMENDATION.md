# Gould — open-source license recommendation (Gould 0.15 Stage 2)

**Status:** the owner has made a binding planning decision selecting
**GPL-3.0-or-later for application source code** and **CC-BY-SA-4.0 for
documentation and original prose** ([DECISIONS.md](DECISIONS.md), owner
decision 3). This decision records planned terms only. It does **not** authorize
applying or publishing either license. Copyright ownership and provenance for
all relevant code contributions, bundled assets, AI-assisted artwork, and
documentation must first be verified; separate explicit owner authorization is
also required before any license is applied. Do not create or modify license
files, metadata, headers, or app assets under this planning decision.

**Label convention used throughout:**

- ✅ **Verified** — read directly from this worktree; cited as `path:line`.
- ⚠️ **Assumed / external** — general license knowledge, GitHub UI state, or a
  repository claim that I could not independently confirm from files here
  (no network used for this deliverable).

---

## 1. Obligations inventory (what bundled components require of the app's license)

### 1.1 Runtime code dependencies — Apache License 2.0 (✅ verified)

- Full runtime dependency inventory for the 0.9 audit (108 of 109 coordinates
  Apache-2.0; Guava ListenableFuture inherits it): [docs/LICENSES.md](../LICENSES.md) L3
  and the per-coordinate list in
  [app/src/main/assets/legal/third-party-notices.md](../../app/src/main/assets/legal/third-party-notices.md)
  L13–135 (AndroidX/Compose/Navigation/Room/WorkManager, Kotlin stdlib,
  kotlinx.coroutines, JetBrains annotations).
- Current Gradle dependencies are consistent with that audit:
  [app/build.gradle.kts](../../app/build.gradle.kts) L34–61 (AndroidX Credentials,
  AppCompat, Compose BOM, Navigation, Room + KSP, WorkManager, JUnit/org.json
  test-only), [activity-domain/build.gradle.kts](../../activity-domain/build.gradle.kts)
  L1–14 (Kotlin Multiplatform JVM target; `kotlin("test")` test-only),
  [build.gradle.kts](../../build.gradle.kts) L1–6 (AGP/Kotlin/KSP plugin versions).
- **Obligation:** Apache-2.0 grants are preserved (attribution, patent grant,
  trademark clauses); notices must be retained and the full Apache text must
  accompany distribution. Already satisfied at runtime by
  `app/src/main/assets/licenses/Apache-2.0.txt` and the Legal screen, which loads
  `legal/privacy.md`, `legal/third-party-notices.md`, the Apache text and both
  OFL texts ([app/src/main/java/com/opus/piano/ui/LegalScreen.kt](../../app/src/main/java/com/opus/piano/ui/LegalScreen.kt) L19).
- **Apache-2.0 is GPLv3-compatible** (official GNU FAQ, see §7): a combined
  work may be distributed under GPLv3 while preserving Apache's applicable
  conditions; this does not relicense upstream Apache components wholesale.
  So every runtime dependency above is compatible with **all** candidates in §2.

### 1.2 Google identity artifacts — Android SDK License (✅ repo claim; ⚠️ terms unverified here)

- `androidx.credentials*` are Apache-2.0, but **Google ID 1.1.1 and Play
  services artifacts are distributed under the Android SDK License**, explicitly
  *not* represented as Apache-2.0:
  [third-party-notices.md](../../app/src/main/assets/legal/third-party-notices.md) L137;
  mirrored in [docs/wiki/Credits-and-Licenses.md](../../docs/wiki/Credits-and-Licenses.md) L23.
- **Obligation:** none of the candidate app licenses changes this. ⚠️ It is,
  however, a **future F-Droid eligibility risk independent of the app license**
  (F-Droid's inclusion policy requires all build dependencies to be FOSS; the
  SDK License is proprietary). If F-Droid becomes a goal, this artifact's exact
  license must be verified upstream and the sign-in path made optional/removable.
  This remains a separate F-Droid follow-up and does not alter the selected
  planned license terms.

### 1.3 Bundled fonts — SIL Open Font License 1.1 (✅ verified)

- Cormorant Garamond and Inter downloaded unmodified from Google Fonts; OFL 1.1
  files bundled inside the APK and in the repo: [docs/fonts/README.md](../fonts/README.md) L3–8;
  files at `app/src/main/assets/licenses/{Inter-OFL,CormorantGaramond-OFL}.txt` (✅ glob).
- **Obligation:** OFL governs the *font software only*, not the app's code or
  UI text. It forbids selling the fonts alone and forbids using Reserved Font
  Names in modified versions — irrelevant while fonts stay unmodified (✅
  `docs/fonts/README.md` L8: "unmodified font binaries"). Bundling under a
  GPL-3.0, Apache-2.0, MIT or MPL app is permissible (⚠️ external knowledge;
  OFL is FSF-listed as GPL-compatible and font embedding in documents/apps is
  an explicit OFL permission). No conflict with any candidate.
- Droid Sans Mono (Tux terminal) is **Apache-2.0** from AOSP, unmodified, with
  the original NOTICE shipped: [docs/LICENSES.md](../LICENSES.md) L13–21,
  [third-party-notices.md](../../app/src/main/assets/legal/third-party-notices.md) L141–147.
  Same obligation as §1.1.

### 1.4 Copyleft exposure — none in the runtime graph (✅ verified, scoped)

- The audit states 108/109 coordinates are Apache-2.0 and the 0.9 audit covers
  123 coordinates ([docs/LICENSES.md](../LICENSES.md) L3, L11).
- **Open Opus:** its *server code* is GPL, but only the **CC0 catalogue data**
  API is consumed; the GPL server code is *not bundled*:
  [docs/LICENSES.md](../LICENSES.md) L5.
- JUnit 4 (⚠️ EPL-1.0 upstream, not stated in repo notices) and org.json (⚠️
  MIT upstream) are `testImplementation` only and **do not ship in the APK**
  ([app/build.gradle.kts](../../app/build.gradle.kts) L52–55, with the repo's own
  comment at L54: "Not shipped in the APK"). They impose no obligation on the
  distributed app under any candidate license.
- **Conclusion:** choosing GPL/MPL for *Gould's own code* introduces no
  license conflict with anything actually shipped.

### 1.5 Content/data licenses (✅ verified)

| Content | License | Evidence | Effect on app license choice |
| --- | --- | --- | --- |
| Open Opus catalogue facts | CC0 | [docs/LICENSES.md](../LICENSES.md) L5; [third-party-notices.md](../../app/src/main/assets/legal/third-party-notices.md) L7 | None — CC0 is public-domain dedication, compatible with anything |
| Wikidata structured data | CC0 | [docs/LICENSES.md](../LICENSES.md) L5 | None |
| Wikipedia extracts (fetched at runtime) | generally CC BY-SA 4.0 | [docs/LICENSES.md](../LICENSES.md) L5; [docs/planning/goldberg-0.11/SOURCES-LICENSES.md](../planning/goldberg-0.11/SOURCES-LICENSES.md) L12 | Attribution + share-alike travel with the *extract*, not with Gould's code; runtime attribution UI already exists. Not a code-license constraint |
| Wikimedia Commons portraits | per-file CC0/CC BY/CC BY-SA only, strict allowlist | [third-party-notices.md](../../app/src/main/assets/legal/third-party-notices.md) L7; allowlist in `CommonsAdapter.kt` L19–20 (✅) | None — images are cached separately and never bundled in APKs ([docs/OPUS-0.5.md](../OPUS-0.5.md) L87, L: images "outside APKs" also stated in stage-4 evidence) |
| MusicBrainz | core CC0; supplementary **CC BY-NC-SA** excluded by default | [docs/planning/goldberg-0.11/SOURCES-LICENSES.md](../planning/goldberg-0.11/SOURCES-LICENSES.md) L10 | ⚠️ NC data must **never** be redistributed inside the app or repo, or the whole distribution stops being FOSS under *any* candidate. Existing exclusion policy must hold |
| Schumann/Piernet quotations (Gutenberg #28219) | public domain; Czech renderings are Gould's own | [docs/QUOTATIONS.md](../QUOTATIONS.md) L3–5 | None; may be licensed with the project's docs |
| 0.12 Home quote artwork | original AI-generated artwork authorized for this project | [docs/wiki/Credits-and-Licenses.md](../../docs/wiki/Credits-and-Licenses.md) L10 | ⚠️ owner should confirm rights position for AI-assisted assets before relicensing them; provenance in `docs/design/gould-0.12/PRODUCTION-ASSETS.md` |
| Launcher icon / design mockups | original project assets (Classic Piano icon from owner-approved 0.9.1 assets) | [docs/wiki/Credits-and-Licenses.md](../../docs/wiki/Credits-and-Licenses.md) L29; [docs/LICENSES.md](../LICENSES.md) L11 ("original vector geometry… no external image license") | None |

### 1.6 What the repository currently says about *its own* license (✅ verified)

- **There is no LICENSE, COPYING or NOTICE file anywhere in the tree** — a
  `**/LICENSE*` glob matched only `docs/LICENSES.md` (✅).
- The license audit explicitly refuses to invent one: *"No application-source
  license is invented by this audit."* — [docs/LICENSES.md](../LICENSES.md) L7.
- [README.md](../../README.md) has no license section or badge; its only
  license link is to third-party licences (L92: "Third-party licences and credits").
- Wiki credits discuss only third-party components and never state an app
  license ([docs/wiki/Credits-and-Licenses.md](../../docs/wiki/Credits-and-Licenses.md) L21–29).
- No source file carries an SPDX header or copyright header (grep over
  `app/src/main/java/**.kt` found only license-*handling* code, ✅).
- ⚠️ Therefore GitHub's repository page likely shows **"No license"**. Under
  default copyright, reuse permission remains with the relevant rights holders;
  this worktree does not establish the complete ownership/provenance chain.
  Readers currently have **no permission** to reuse Gould's own source — the
  only thing open right now is the dependency set.

### 1.7 Distribution & policy context (✅ verified unless marked)

- APK is sideloaded from GitHub Releases ([README.md](../../README.md) L58–66), not
  Play Store — so Play Store EULA/commercial terms are irrelevant; GPL-style
  obligations are exercised through ordinary distribution of the APK + repo.
- ⚠️ The visible Git history includes display names `Ondra` and `stokonn`, and
  the project includes AI-assisted artwork. This report cannot establish
  whether those names refer to the same rights holder or establish rights in
  every asset. Confirm contributor and asset provenance before adopting or
  relicensing the project. The repository policy forbids AI co-authorship
  trailers, which does not itself establish ownership.
- Possible future F-Droid submission ⇒ needs an FSF/OSI-recognized license
  (GPL, Apache, MIT, MPL all qualify; source-available does not).
- AGENTS.md attribution rules: `created by stokonn with love <3` must be
  preserved ([README.md](../../README.md) L102; [Credits-and-Licenses.md](../../docs/wiki/Credits-and-Licenses.md) L12); no AI co-authors
  (AGENTS.md, "Permanent GitHub workflow" section) — these survive *any* choice
  and are separate from copyright licensing.
- Stage-2 task E4 already plans `CONTRIBUTING.md` with "No LICENSE file until
  owner approves" ([STAGE-CHECKLIST.md](STAGE-CHECKLIST.md) L133–135).

---

## 2. Candidate comparison

Dimensions: (a) Apache-2.0 dependency compatibility, (b) OFL font
compatibility, (c) copyleft surface, (d) patent clause, (e) suitability for a
sideloaded Android APK, (f) F-Droid/FOSS reception, (g) contribution dynamics,
(h) enforcement burden.

| License | (a) Deps | (b) OFL fonts | (c) Copyleft surface | (d) Patents | (e) APK distribution | (f) F-Droid / FOSS | (g) Contributions | (h) Enforcement |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **GPL-3.0-or-later** | ✅ GPLv3-compatible combination with Apache-2.0 components; Apache terms remain, with no blanket relicensing | ✅ font software stays under OFL; embedding permitted (⚠️) | A conveyed combined/derivative work may trigger GPL source and licensing duties; aggregation and system-library cases differ. Assess the actual combination rather than assuming all linked code is covered | ✅ explicit patent grant + defensive termination (from GPLv3) | ✅ works; provide or clearly link complete corresponding source for each distributed binary/version and keep it available while distributing that binary; a different repository commit alone may not suffice | ✅ canonical FOSS license; strongest community trust for an APK | contribution and relicensing rights need provenance/permission review; no sole-holder assumption | moderate: keep corresponding source for every distributed build and verify rights provenance (⚠️) |
| **LGPL-3.0-or-later** | ✅ (LGPL-3 = GPLv3 + extra permissions) | ✅ same as GPL | Designed for *libraries*; applied to a whole app it degrades into awkward whole-app weak-copyleft with relinking duties (object files for Android) | ✅ same as GPL-3.0 | ⚠️ legally fine but practically the worst fit: LGPL relinking obligations are hard to satisfy for an APK | ✅ accepted | same as GPL but confusion about which parts are "the library" | high: relink/source duties for an *application* are a poor fit |
| **MPL-2.0** | ✅ compatible combinations are possible subject to MPL §3.3 conditions; not an unconditional two-way compatibility claim | ✅ | **File-level**: MPL-covered files retain their reciprocal source duties; other files may use different terms under MPL conditions | ✅ patent grant + termination (⚠️) | ✅ fine; provide source for covered modified files and retain required notices | ✅ FSF/OSI approved, accepted by F-Droid | contribution and relicensing rights need provenance/permission review | low, but boundaries must be drawn per file; weaker protection against closed forks |
| **Apache-2.0** | ✅ native — the entire dependency stack already uses it | ✅ | **None** (permissive) | ✅ explicit grant + patent-retaliation clause | ✅ ideal; Android ecosystem default | ✅ accepted | inbound under Apache-2.0; frictionless | very low; NOTICE/attribution retention only |
| **MIT** | ✅ | ✅ | **None** (permissive) | ❌ **no patent grant** (⚠️) | ✅ ideal | ✅ accepted | frictionless | very low; attribution only, no NOTICE mechanism |

**Source-available licenses (BSL 1.1, PolyForm, SSPL, Elastic, "non-commercial") — rejected as a class:**

- ⚠️ Not FSF- or OSI-recognized as open source; **F-Droid rejects them** — which
  conflicts with the owner's stated possible-F-Droid goal (task brief).
- ⚠️ SSPL/PolyForm are specifically designed to block the cloud/appliance reuse
  the owner has not asked to block.
- They contradict the repository's existing posture: dependencies are all
  permissive/Apache, data is CC0, and the project presents itself as open
  ([README.md](../../README.md) L84–92). Going source-available *after* years of
  public source would also strand earlier copies (⚠️: they would remain
  all-rights-reserved anyway, but the community expectation is set).
- If the owner's actual wish is "no proprietary forks," **GPL-3.0-or-later
  achieves that while remaining FOSS** — there is no need to leave open source.

### Why not LGPL for an application? (⚠️ reasoning)

LGPL exists so users can swap a *library* out. Gould is an application, not a
library. Choosing LGPL for the whole app either (i) collapses into GPL-like
obligations with extra steps, or (ii) implies carving out a "library" that does
not exist (`activity-domain` is internal, not published for linking —
[activity-domain/build.gradle.kts](../../activity-domain/build.gradle.kts) L6–7 comment).
More relinking bureaucracy, no extra freedom. Reject.

### Why not MPL-2.0 as primary? (⚠️ reasoning)

MPL is a perfectly good weak-copyleft license and the easiest to mix with
proprietary code. But for a **complete Android app** there are no natural
"library boundaries": the deliverable is one APK, so file-level copyleft buys
almost nothing in practice while letting a downstream distributor ship a
closed-source fork containing unchanged MPL files plus proprietary ones. If the
point of copyleft is to keep *Gould* open, GPL's whole-work surface matches the
deliverable better. MPL stays the best choice only if the owner explicitly wants
permissive-style mixing.

---

## 3. LICENSE ANALYSIS AND OWNER-SELECTED PLAN

### Primary: **GPL-3.0-or-later** for the application source code

Reasoning, tied to verified evidence:

1. **The deliverable is a whole APK, not a library.** Whole-work copyleft maps
   exactly to how Gould is distributed (sideloaded APK from GitHub Releases,
   [README.md](../../README.md) L58–66). Anyone who distributes a modified Gould
   must release *their* modifications' source — protecting the project's
   data-integrity and safety ethos (AGENTS.md: "Data integrity outranks feature
   completeness") from silent closed-source forks.
2. **Zero dependency conflict.** The entire runtime graph is Apache-2.0
   (§1.1) — legally incorporable into GPL-3.0 — plus CC0 data and OFL fonts,
   neither of which constrains the app license (§1.3, §1.5). No copyleft
   dependency exists in the APK (§1.4).
3. **FOSS-recognized, not automatically F-Droid-ready.** F-Droid separately
   requires FLOSS dependencies and a compatible build toolchain. The Google ID
   / Play services artifacts (§1.2) are an independent likely blocker that
   needs upstream verification and possibly a removable sign-in build path.
4. **Patent protection** for the owner comes with GPLv3's express grant and
   defensive-termination clauses — stronger than MIT, comparable to Apache-2.0.
5. **Provenance must be confirmed.** The repository evidence does not establish
   that there is one copyright holder or that every bundled project asset may
   be relicensed. Resolve contributor and asset rights before adopting a
   license or setting contribution terms; later relicensing can require
   permission from each relevant rights holder.
6. **`-or-later`** preserves future flexibility (e.g., GPLv4 migration)
   without any present cost (⚠️ standard practice).
7. **Honest cost:** GPL requires complete corresponding source for each
   distributed binary and ongoing availability while that binary is
   distributed. A public repository is not sufficient if it does not match the
   binary or omits required source/build materials. Rights-holder standing
   cannot be inferred from this source inventory.

### Evaluated alternative, not selected: **Apache-2.0**

- Matches the entire existing dependency ecosystem — Gould would be licensed
  exactly like AndroidX/Compose it is built from (§1.1).
- Adds an explicit **patent grant** (unlike MIT) and a NOTICE mechanism that
  fits the project's existing attribution infrastructure (LegalScreen,
  `third-party-notices.md`).
- Cost: **allows closed-source proprietary forks**. If that is unacceptable to
  the owner, Apache-2.0 is the wrong choice and GPL is the right one.
- MIT is a valid but strictly weaker Apache-2.0 (no patents, no NOTICE);
  no reason to prefer it here (⚠️).

### Owner's planned split: **two distinct licenses, code + documentation**

- **Application source code:** **GPL-3.0-or-later**.
- **Documentation and original prose:** **CC-BY-SA-4.0**. Third-party content,
  quotations, fonts, images, and other bundled assets retain their own terms;
  this decision does not relicense them. Code snippets inside documentation
  remain governed by the planned code license when copied as code. Rationale:
  - The repository already has a precedent for exactly this split: the
    owner-approved contribution-terms decision "CC0 for contributor-owned
    structured catalogue metadata; **CC BY-SA 4.0 for original contributor-authored
    text**" ([docs/planning/goldberg-0.11/stage-6/OPEN-DECISIONS.md](../planning/goldberg-0.11/stage-6/OPEN-DECISIONS.md) L111).
  - The docs already quote/describe CC BY-SA Wikipedia material and public-domain
    quotations (§1.5); CC BY-SA keeps that world coherent, whereas putting the
    wiki under a software license would be a poor fit (⚠️).
  - The owner has selected CC-BY-SA-4.0 for original prose; CC-BY-4.0 is not
    the current plan.
  - **Rejected for docs:** GFDL (awkward, rarely chosen for wikis), putting docs
    under GPL (wrong tool), or one single license for everything (mixes
    software and content regimes and complicates reuse of snippets).
  - ⚠️ Note: code snippets inside docs, if any are extracted later, should be
    pointed at the code license explicitly.

### Rejected outright (with reasons)

| Option | Why rejected |
| --- | --- |
| Source-available / BSL / PolyForm / SSPL / "non-commercial" | Not FOSS; F-Droid-incompatible; unnecessary given GPL already blocks closed-source forks; inconsistent with the project's all-permissive dependency posture (§2) |
| LGPL (app-wide) | Library license applied to an application; relinking obligations meaningless for an APK; no natural library boundary (§2) |
| No license (status quo) | ⚠️ Today all rights are reserved: nobody may legally reuse the source, GitHub shows "No license," F-Droid impossible, and the project's "open" signals (all-permissive deps, CC0 data, public repo) are misleading (§1.6) |
| AGPL-3.0 | Overkill: Gould's own network surface is read-only catalogue endpoints, and the app itself communicates with third-party providers the owner does not control ([README.md](../../README.md) L74–76); AGPL would add obligations on hypothetical server code (`services/community-bff`) that is not shipped in the app. ⚠️ Optional later decision for backend code specifically |

---

## 4. Existing third-party notices and separate follow-ups

These are already implemented and must keep working:

1. **Apache-2.0 dependency notices** — `app/src/main/assets/licenses/Apache-2.0.txt`,
   `legal/third-party-notices.md`, surfaced by `LegalScreen.kt` L19 (✅).
2. **OFL font licenses** — both OFL texts in `assets/licenses/` and
   `docs/fonts/`; fonts stay unmodified; no Reserved-Family/Reserved-Name use
   (✅ `docs/fonts/README.md` L8).
3. **Droid Sans Mono NOTICE** — `legal/droid-sans-mono-NOTICE.txt` (✅,
   [docs/LICENSES.md](../LICENSES.md) L19).
4. **Runtime content attribution** — Open Opus/Wikidata CC0, Wikipedia CC BY-SA
   attribution, per-portrait Commons credits, Gutenberg quotation credit
   (✅ §1.5; attribution UI exists).
5. **Human attribution line** — `created by stokonn with love <3` in README and
   Wiki Credits (✅ [README.md](../../README.md) L102, Credits L12) and **no AI
   co-author trailers** (AGENTS.md policy).
6. **Planned GPL code terms:** before application, verify rights/provenance for
   all relevant code contributions, bundled assets, AI-assisted artwork, and
   documentation. Then obtain separate explicit owner authorization. Before
   distributing any binary under GPL-3.0-or-later, provide the complete
   corresponding source for each distributed binary, matching that binary,
   and keep it available to recipients for the required period. The selected
   CC-BY-SA-4.0 documentation terms remain a separate scope.
7. **MusicBrainz CC BY-NC-SA exclusion must remain** (✅ policy at
   SOURCES-LICENSES.md L10) — NC data would poison FOSS status under any choice.

---

## 5. Exact follow-up steps AFTER owner approval (none executed now)

1. First complete the rights/provenance verification prerequisite in the owner
   decision record, covering relevant code contributions, bundled assets,
   AI-assisted artwork, and documentation. Resolve all uncertainties before
   applying either planned license.
2. Obtain separate explicit owner authorization to apply and publish the
   planned GPL-3.0-or-later code terms and CC-BY-SA-4.0 documentation terms.
3. Only then add root `LICENSE` with the complete GPL-3.0-or-later text and
   apply CC-BY-SA-4.0 to eligible documentation/original prose. Keep the two
   scopes distinct and do not relicense third-party material.
4. Before distributing any GPL-covered binary, ensure its exact corresponding
   source is accessible to all recipients and remains available as required.
5. **README:** add a short `## License` section linking `LICENSE` and stating
   the separate docs license (one paragraph; keep the existing third-party
   link).
6. **`docs/LICENSES.md`:** *append* a new section "Application source license"
   recording the decision, date and owner approval; do **not** rewrite the
   historical audit lines (L3–21 remain as evidence, including the historical
   L7 sentence).
7. **`docs/wiki/Credits-and-Licenses.md`:** add an "Application license" line
   at the top (before the preserved historical block), synchronized with
   README — Wiki policy requires these to agree (AGENTS.md).
8. **In-app (recommended, can follow in a later batch):** add the app license
   text to the Legal screen's asset list (`LegalScreen.kt` L19) as
   `licenses/<AppLicense>.txt`, with EN/CZ strings — otherwise the APK conveys
   no license text (GPL requirement) to offline users.
9. **`CONTRIBUTING.md` (planned task E4, [STAGE-CHECKLIST.md](STAGE-CHECKLIST.md)):
   state that contributions are submitted under the approved license; this is
   what makes future relicensing painless.
10. **Release notes / next release:** mention the license adoption in the next
   release document; include the license text in the APK assets at that build.
11. **GitHub:** after the approved files land on the default branch, the repo page flips
   from "No license" to the SPDX identifier (⚠️ UI behavior); verify
   `stokonn/Gould` shows it.
12. **F-Droid (future, separate decision):** prepare a Fastlane metadata
   submission; separately verify the Google ID / Play-services artifact terms
   (§1.2) and retain the MusicBrainz exclusion (§4.7). These remain separate
   follow-ups and do not change the selected planning terms.
13. **Stage records:** update the checklist and record the separate application
    authorization in [DECISIONS.md](DECISIONS.md), then synchronize the GitHub
    Issue only after the owner's explicit approval and rights verification.

---

## 6. Explicit statement

**This worktree contains no application `LICENSE` file (nor app `COPYING` or
`NOTICE`) created by this planning update. The owner has selected planned terms
— GPL-3.0-or-later for application code and CC-BY-SA-4.0 for documentation and
original prose — but they are not yet applied or published. Ownership/provenance
verification is a blocking prerequisite, followed by separate explicit owner
authorization. This document does not authorize or perform those actions.**

## 7. Primary references checked

- GNU Project, [GPL compatibility FAQ](https://www.gnu.org/licenses/gpl-faq.en.html#WhatDoesCompatMean)
  (including the distinction between compatibility of a combined work and
  relicensing upstream components).
- GNU Project, [source and binary distribution at different sites](https://www.gnu.org/licenses/gpl-faq.en.html#SourceAndBinaryOnDifferentSites)
  and [continued source availability](https://www.gnu.org/licenses/gpl-faq.en.html#DistributeExtendedBinary).
- GNU Project, [license compatibility](https://www.gnu.org/licenses/license-compatibility.en.html).
- Mozilla, [MPL 2.0 FAQ](https://www.mozilla.org/en-US/MPL/2.0/FAQ/) (file-level terms and GPL combination conditions).
- Apache Software Foundation, [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.html)
  and [application guidance](https://apache.org/legal/apply-license.html).
- F-Droid, [current Inclusion Policy](https://fdroid.gitlab.io/jekyll-fdroid/en/docs/Inclusion_Policy/)
  (FLOSS dependencies, legal assets, and build requirements).

These references support the license descriptions above; the owner's decision
sets the planned terms but does not establish rights ownership or Gould's
eligibility for any store.

## Owner approval and completed scope audit — 2026-11-16

The owner subsequently approved GPL-3.0-or-later for project-owned program
source, CC-BY-SA-4.0 for project-owned documentation/original prose, and
production use of the exact verified MusicBrainz Core catalogue. The requested
scope audit is recorded in `docs/LICENSES.md`; canonical texts are now present
as `LICENSE` and `LICENSE-DOCS`. The third-party license boundaries and
original artwork/source records remain separate. This supersedes the pending
authorization language above, while retaining that historical planning record.
