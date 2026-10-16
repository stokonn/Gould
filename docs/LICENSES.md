# Goldberg 0.8 license audit

The resolved debugRuntimeClasspath report contains 109 coordinates, including platform constraints and multiplatform variants. All cached POMs were inspected; 108 declare Apache License 2.0 directly, and Guava ListenableFuture 1.0 inherits it from guava-parent 26.0-android. Full Apache text is bundled in assets/licenses/Apache-2.0.txt. Existing Cormorant Garamond and Inter OFL files are preserved verbatim. Settings exposes license/privacy notices and the runtime coordinate inventory.

Open Opus separates its public-domain/CC0 catalogue dataset from GPL-licensed server code; Goldberg consumes the API and does not bundle that server code. Source: https://github.com/openopus-org/openopus_api . Wikidata structured data is CC0. Wikipedia biographies retain article, contributor/history and runtime license links in composer credits; introductions are labeled unchanged. Wikimedia images retain item artist/credit/source/license links and disclose editorial cropping. The loader rejects portraits without its recognized license metadata. Source policy: https://en.wikipedia.org/wiki/Wikipedia:Copyrights . No third-party sound sample was added.

The harpsichord uses mathematical synthesis. Android/Compose Material icons retain Apache coverage. No application-source license is invented by this audit. Verify any future asset/dependency addition separately; this audit is scoped to the resolved candidate graph and current content attribution paths, not a blanket legal guarantee.

## Goldberg 0.9 audit

The resolved debug runtime dependency graph contains 123 coordinates after adding AndroidX AppCompat 1.7.1 for per-app languages. Cached published POM licenses were reviewed; runtime notices are recorded in app/src/main/assets/legal/third-party-notices.md. Original Inter and Cormorant Garamond OFL notices remain bundled. The new launcher artwork is original vector geometry and introduces no external image license.
