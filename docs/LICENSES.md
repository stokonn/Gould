# Goldberg 0.8 license audit

The resolved debugRuntimeClasspath report contains 109 coordinates, including platform constraints and multiplatform variants. All cached POMs were inspected; 108 declare Apache License 2.0 directly, and Guava ListenableFuture 1.0 inherits it from guava-parent 26.0-android. Full Apache text is bundled in assets/licenses/Apache-2.0.txt. Existing Cormorant Garamond and Inter OFL files are preserved verbatim. Settings exposes license/privacy notices and the runtime coordinate inventory.

Open Opus separates its public-domain/CC0 catalogue dataset from GPL-licensed server code; Goldberg consumes the API and does not bundle that server code. Source: https://github.com/openopus-org/openopus_api . Wikidata structured data is CC0. Wikipedia biographies retain article, contributor/history and runtime license links in composer credits; introductions are labeled unchanged. Wikimedia images retain item artist/credit/source/license links and disclose editorial cropping. The loader rejects portraits without its recognized license metadata. Source policy: https://en.wikipedia.org/wiki/Wikipedia:Copyrights . No third-party sound sample was added.

The harpsichord uses mathematical synthesis. Android/Compose Material icons retain Apache coverage. No application-source license is invented by this audit. Verify any future asset/dependency addition separately; this audit is scoped to the resolved candidate graph and current content attribution paths, not a blanket legal guarantee.

## Goldberg 0.9 audit

The resolved debug runtime dependency graph contains 123 coordinates after adding AndroidX AppCompat 1.7.1 for per-app languages. Cached published POM licenses were reviewed; runtime notices are recorded in app/src/main/assets/legal/third-party-notices.md. Original Inter and Cormorant Garamond OFL notices remain bundled. The new launcher artwork is original vector geometry and introduces no external image license.

## Gould0.12 terminal font

Tux uses an unmodified bundled Droid Sans Mono from the Android Open Source
Project, pinned tree c049f9a/data/fonts (117072bytes, SHA256
089bdaac95caeed25a8392a6f0606328d009473119f1c7465b642d5cebe5320c).
The tree README explicitly licenses its platform fonts under Apache2.0; the
original complete NOTICE is shipped in assets/legal/droid-sans-mono-NOTICE.txt.
Source: https://android.googlesource.com/platform/frameworks/base/+/c049f9a/data/fonts/
The terminal alone selects this font; app editorial typography remains unchanged.
# Gould 0.15 project license scope — owner-approved 2026-11-16

- **Project-owned program source:** `GPL-3.0-or-later` (`LICENSE`). This covers original application and project-owned program source in this repository.
- **Project-owned documentation and original prose:** `CC-BY-SA-4.0` (`LICENSE-DOCS`). Code excerpts remain under the program-source license.
- **MusicBrainz catalogue data:** the bundled curated subset is derived from MusicBrainz Core snapshot `20261114-002230`, licensed CC0-1.0. The provenance report and manifest identify its exact source and transformations.
- **Third-party materials:** excluded from the project-wide grants. AndroidX/Kotlin/Compose/Room/Google artifacts, Cormorant Garamond and Inter fonts, Android Open Source Project Droid Sans Mono, Wikimedia biography/portrait content, public-domain quotation source/translation, and other identified external materials retain their own licenses and attribution. See the bundled notices and relevant source/asset records. Do not infer an individual portrait or future downloaded media license from this inventory.
- **Project-owned artwork:** Gould icon artwork and the Home quote-card landscape are documented as owner-approved original artwork; the landscape is generated artwork, not a third-party photograph. The quote texts and translations retain their separately recorded source terms.

The repository history contains contributions under the owner's established `stokonn` and `Ondra` Git identities. This audit does not invent copyright years or add a copyright holder line. No coding agent is named as an author or co-author. The project-owned source, bundled fonts, production artwork and catalogue data have identified provenance. Existing Open Opus/Wikidata/Wikipedia/Commons attribution paths concern retained legacy records or separately sourced profile content; they do not alter the verified Core-only bundled catalogue. Do not apply the project licenses to these materials.

### Release-blocking Google identity dependency license question

The resolved application includes `com.google.android.libraries.identity.googleid:googleid:1.1.1` and `com.google.android.gms:play-services-auth:21.1.1`. Their cached published POMs declare the **Android Software Development Kit License**, not an open-source license. The signed Release APK contains `com.google.android.gms.auth` classes. The current [Android SDK agreement](https://developer.android.com/studio/terms) §3.4 restricts redistribution of the SDK and its parts except as required by third-party licenses; §3.5 says components licensed under an open-source license are governed by that license. The GNU [GPL FAQ on library compatibility and system libraries](https://www.gnu.org/licenses/gpl-faq.html) makes compatibility depend on the actual permission and whether the system-library criteria apply.

This audit did not establish that these packaged SDK-license components may be redistributed as part of the GPL-covered Gould application, nor that the GPLv3 System Library exception applies to them. No Google-granted GPL-compatible linking permission was found in the inspected artifact metadata or SDK terms. This is a material, **unresolved compatibility question**, not a finding that Google has definitively prohibited the current app. The combined source/APK release gate therefore remains blocked until the owner obtains a definitive rights/compatibility determination or the dependency is separately removed/replaced under an approved scope. Do not add a GPL linking exception or represent these dependencies as Apache-licensed without explicit rights-holder authorization.

**Owner release authorization (2026-11-16):** after the completed
[Google dependency licensing audit](planning/gould-0.15/GOOGLE-DEPENDENCY-LICENSING-AUDIT.md)
returned the verdict *LEGAL REVIEW REQUIRED*, the owner explicitly
authorized completing and publishing Gould 0.15 as a GitHub prerelease
despite the unresolved questions, while instructing that no permission be
invented and the questions not be represented as resolved. That
authorization is recorded here transparently: **the compatibility question
above remains unresolved and this document's analysis is unchanged.** The
authorization is a release decision for Gould's own code only; it grants
nothing for Google's code. The licensing decisions remain application source
GPL-3.0-or-later and project-owned documentation CC-BY-SA-4.0. Google
Sign-In stays unchanged in 0.15; its replacement investigation is deferred
to Gould 0.16.

The root `LICENSE` is a repository-level selector for project-owned program source, not a claim that every file or the whole repository is GPL. `LICENSE-DOCS` applies only to project-owned documentation and original prose. Per-file notices and linked third-party terms take precedence for third-party items.

## Preserved third-party and historical license audits
