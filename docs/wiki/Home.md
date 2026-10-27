# Goldberg 0.11

Goldberg is a native Android practice journal with a shared music catalogue in Discover and Library, sourced encyclopedia details, offline bundles and Smart Cache. Your repertoire, practice history, drafts and backups stay local.

[Download Goldberg 0.11 Stable](https://github.com/stokonn/Goldberg/releases/tag/v0.11) · [Release notes and verified limits](https://github.com/stokonn/Goldberg/blob/main/docs/releases/GOLDBERG-0.11.md).

Use [Getting Started](Getting-Started), [User Guide](User-Guide), [Settings & Customization](Settings-and-Customization), [Developer Guide](Developer-Guide), [FAQ & Troubleshooting](FAQ-and-Troubleshooting), [Changelog & Roadmap](Changelog-and-Roadmap) and [Credits & Licenses](Credits-and-Licenses).

The public catalogue API allows reads and currently has no published records. Backend OAuth, community writes and practice uploads remain disabled. Optional provider search is separate and requires the app's online-search consent.

![Goldberg 0.11 catalogue detail](images/goldberg-0.11-catalogue-detail.png)

Verified API36 emulator render from a7bb2aa; recorded provider fixture, not physical-device or live-provider evidence.

created by stokonn with love <3

<details>
<summary>Preserved earlier documentation and dated development history</summary>

## Goldberg 0.11-rc.1 — published release candidate (October 25, 2026)

[Download Goldberg 0.11-rc.1](https://github.com/stokonn/Goldberg/releases/tag/v0.11-rc.1).
The candidate is a signed, non-debuggable release build, versionCode 12. Goldberg
0.10.1 remains the latest stable release. Catalogue, encyclopedia, offline bundles,
Smart Cache and accepted Android UI work are included; the Stage 8 Settings focus,
full navigation labels and test-scaffold contrast corrections are included.

**Production community networking, cloud sign-in and real-user contribution
collection remain disabled. No private Cloudflare Worker has been deployed.**
The existing private Supabase schema is deployed, but it is not a live public
community service. Personal practice data remains local.

[Candidate CI](https://github.com/stokonn/Goldberg/actions/runs/40212684482):302 JVM
cases passed;203 native cases,192 passed/11 existing fixture or probe-dependent
skips/0 failures on API36. Seeded 0.10.1 upgrade preserved original rows, preferences
and37 sessions;offline startup and process-death integrity checks passed. CI uses
debug rebuilds;the release APK certificate was independently verified against the
published APK. Final0.11/cloud acceptance remains open for credentials/privateTLS/
OAuth,privacy/source policy,operational backup custody and public API/client adoption.

Earlier dated entries below are historical and are superseded by this candidate
status where they describe pending UI corrections or the absence of a candidate.

---

# Goldberg

Goldberg is a native Android music journal for classical pianists, created by stokonn. It combines a personal repertoire, manual and experimental Smart Practice timing, multi-piece session review, statistics and earned badges.

The current release is [Goldberg 0.10.1](https://github.com/stokonn/Goldberg/releases/tag/v0.10.1). It refines the editorial interface, local composer favorites, Favorite Artist selection and historical session editing while preserving the existing practice journal. Google identity is optional profile identity, not cloud practice synchronization.

Start with [Getting Started](Getting-Started), then read the [User Guide](User-Guide). See [Settings & Customization](Settings-and-Customization), [FAQ & Troubleshooting](FAQ-and-Troubleshooting), [Developer Guide](Developer-Guide), [Changelog & Roadmap](Changelog-and-Roadmap) and [Credits & Licenses](Credits-and-Licenses).

Screenshots are added only after actual emulator capture and inspection. No illustrative mockup is presented as a verified app screenshot. Codex performs no physical-phone testing; owner-reported results are attributed separately below.

created by stokonn with love <3

## Goldberg 0.10.1 emulator captures

These inspected captures use a generic local identity. No private reference images or new third-party portrait files are published here.

![Goldberg 0.10.1 Profile](images/goldberg-0.10.1-profile.png)

![Goldberg 0.10.1 About](images/goldberg-0.10.1-about.png)

![Goldberg 0.10.1 Appearance](images/goldberg-0.10.1-appearance.png)

![Goldberg 0.10.1 Tux](images/goldberg-0.10.1-tux.png)

## Historical development screenshots

These are actual emulator captures of the 0.10 verification build, using controlled test data. They are not physical-device verification. The Google scenarios reported below were manually tested by the owner.

![Goldberg 0.10 Home](images/goldberg-0.10-home.png)

![Goldberg 0.10 Repertoire](images/goldberg-0.10-repertoire.png)

![Goldberg 0.10 Practice](images/goldberg-0.10-practice.png)

![Goldberg 0.10 Profile](images/goldberg-0.10-profile.png)

![Goldberg 0.10 Settings](images/goldberg-0.10-settings.png)

The Bach's Secret keyboard and Tux terminal captures below are retained from the verified 0.9.2 emulator build; those screens were not recaptured for 0.10.

![Bach’s Secret](images/bach-keyboard-development.png)

![Offline terminal easter egg](images/tux-development.png)

[Verification status](Verification) separates automated/emulator checks from the owner’s manual report. The six 0.10 Google scenarios are recorded as PASS — owner-reported manual verification.

</details>
