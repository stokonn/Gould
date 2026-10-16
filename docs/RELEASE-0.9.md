# Goldberg 0.9.0 — The Redesign Update

**Goldberg 0.9.0 Beta** brings a more coherent, personal practice journal: a refreshed visual identity, four focused destinations, flexible layouts, and Czech and English throughout the application.

This is a **Pre-release**, not version 1.0. Back up your journal before updating. Physical-device testing and real installation upgrades have not been performed for this beta; the limitations below are part of this release.

## What's New

- A unified **Ivory** and **Nocturne** design, with refined typography and restrained accents.
- Four main tabs: **Home, Repertoire, Practice and Profile**.
- Balanced Home with quick practice start, two configurable statistics and recently practiced pieces.
- Cleaner repertoire browsing, piece details and practice controls.
- Organized Settings, visual customization and safe appearance reset.
- **Czech and English localization**, including notifications, reviews, achievements and accessibility text.
- A new **Classic Minimal Piano** launcher icon.

## New Design

Warm ivory paper and deep charcoal surfaces frame the journal, with Cormorant Garamond headings and Inter for readable controls, metadata and numbers. Follow Android’s system theme or choose Light or Dark. Curated gold, olive, rose and slate accents complement soft, outlined or open grouped sections.

Contextual headings replace repeated branding and redundant toolbars. Bottom navigation keeps icons and localized labels, while detail pages retain Back controls. Motion is brief and state-driven, with Android reduced-motion settings respected.

## Home

A time-of-day greeting uses your nickname when available. **Start Practice** goes directly into free practice, respecting your Smart Practice preference and any necessary permission step. **Choose Piece** opens selection first. An unfinished session is shown rather than silently replaced.

Choose exactly two statistics from today’s, this week’s or this month’s credited time, your streak, session count and repertoire size. Two distinct recently practiced saved pieces open their details without starting a session. **View All** opens Practice History; the old full latest-session card has moved out of Home.

Reorder or hide the informational sections while keeping the primary practice action available.

## Repertoire

Switch between **My Pieces** and **Catalogue**, with one contextual search interface at a time. The contextual **+** menu retains catalogue discovery and manual entry. Compact rows prioritize title, composer and progress, with a persistent choice of compact or comfortable density.

**Recently Practiced** is the default sort; alphabetical, composer and recently added sorting remain available. Existing filters, notes, identifiers and study links are retained.

Piece details emphasize practice, show available metadata without empty “unknown” cards, and hide unavailable work descriptions. **My Practice** summarizes credited time, last activity and relevant sessions; multi-piece attribution is grouped without counting a parent session twice. Sources and study links remain accessible in a collapsible section.

## Practice

The current piece or Free Practice comes first, followed by a large readable timer, credited/elapsed information and Smart Practice state. **Pause/Resume** and **Finish & Review** stay above bottom navigation, with notes and multi-piece selection in the scrolling content.

A restrained activity indicator responds to actual detector states rather than invented sound levels. Sensitivity and auto-pause settings now live under **Settings → Smart Practice**, leaving a convenient enable control in Practice.

Experimental Smart Practice remains local and uses the existing microphone foreground service for authorized background and screen-off listening. Manual pause releases capture; automatic pause keeps listening for renewed activity. Review, session notes, per-piece notes, durable drafts, notification controls, reminders and CSV exports remain available. Detection does not identify pieces and can mistake speech or recorded music for piano playing.

## Profile & Achievements

Your avatar, nickname, editing controls and Settings stay accessible. A concise overview and one activity chart retain week, month, year and all-time views. Frequently practiced pieces and session history remain available.

The full **Badges** collection has moved into Profile rather than disappearing with its old bottom tab. Your showcase still holds up to five selected badges, with their original unlock dates, rarity, sorting and filters. **Bach’s Secret** and **The Curious Pianist** are preserved: tap the About version seven times to find the synthesized harpsichord.

## Customization

**Settings → Appearance & Customize** brings together theme, accent, typography, card style, Home metrics/layout, repertoire density/sort and Profile layout. Optional informational sections can be reordered or hidden; identity, Settings and essential navigation remain available.

**Reset Appearance** asks for confirmation and resets visual choices only. It does not delete your profile, practice history, repertoire, notes, badges, language preference or reminder settings.

New layout/accent/density choices stay on the device and survive ordinary updates. They are not added to portable journal backups.

## Localization

Select **Follow system**, **Čeština** or **English** in Settings. AndroidX’s per-app language support integrates with Android’s language mechanism and provides compatibility on older supported versions.

English and Czech resources cover screens, Settings, dialogs, messages, notifications, onboarding, reviews, history, achievements and accessibility descriptions. Czech plural categories and selected-locale date/number formatting are included. Personal titles, composer names, nicknames, notes and imported catalogue content are preserved in their original language. Third-party source and license texts retain their original attribution.

## App Icon

**Classic Minimal Piano** combines a warm ivory field, minimalist black keys and a restrained reflection, with a Nocturne counterpart and monochrome themed artwork. The icon uses original adaptive vector geometry with no text.

Full-color light/dark switching depends on the launcher; some launchers cache icons or do not honor night-qualified artwork. The themed monochrome option also depends on launcher support.

## Data Compatibility

Application ID **com.opus.piano**, database **opus.db**, credited-time semantics and **Room schema 6** are unchanged. No presentation-only database migration or destructive fallback is introduced.

Sessions, multi-piece associations, notes, repertoire, profile, badges, unlock dates, showcase and existing preferences are retained by the existing storage model. Supported **format-1/schema-4–5** and **format-2/schema-6** backup contracts remain available. Restore still validates before replacing saved portable data and requires explicit confirmation. New device-local visual preferences are separate from journal backups.

## Installation & Updating

1. Export a backup from your existing installation and keep an independent copy.
2. Download **Goldberg-v0.9.0.apk** below. Android **8.0 or later** is required.
3. Install it over the compatible existing app. The APK reports **0.9.0 / version code 7** and uses the same actual signing certificate as the published 0.8 APK.
4. If Android reports a signature or installation error, stop. **Do not uninstall Goldberg or clear its data to bypass the error.**

The APK uses the existing compatible **Android Debug signing identity**; it is not production-signed. Matching certificates do not establish a tested installation upgrade. Backups and CSV exports are unencrypted; a chosen cloud document provider may synchronize them.

## Known Limitations

No connected Android device was available. **Android instrumentation execution, real installation upgrades, runtime backup/restore, rendered Czech/English layouts, narrow screens and large fonts, TalkBack/accessibility, launcher behavior, background/screen-off microphone operation and battery/thermal/performance behavior have not been verified on device.**

Smart Practice remains experimental: background noise and recorded music can cause false detections, quiet playing can be missed, and headphone-only digital piano may be inaudible to the microphone. Permission changes, competing capture and device power management can interrupt operation. Microphone access never starts automatically after reboot or process recovery.

The state indicator is not an audio waveform or proof of piano recognition. Large journals still load complete local history for summaries. Reminders are approximate and subject to Android notification/power settings. Catalogue content and external study links depend on their providers; English source biographies are not automatically translated.

## Verification

The final clean automated check passed **52 JVM tests** with zero failures/errors, built the application and Android test APKs, and completed lint with **zero errors**. Five reviewed warnings concern SDK/compatibility or optional style/resource guidance.

Host checks passed all five historical migration paths to schema 6 and preservation of the supplied legacy backup fixture. The localization audit passed **678 English/Czech resources**, placeholder parity and required Czech plural categories. Both fonts cover Czech diacritics; tested semantic text/accent pairs meet at least 4.5:1 host contrast checks.

APK metadata and signature were checked against the actual published 0.8 artifact. These host checks and compiled Android tests **do not prove runtime correctness**. No device-level success or measured battery/startup improvement is claimed.

**APK SHA-256**
```text
d2666da877eb307c084d7454f3257a794e81181e0c4468265d9412d39d73a688
```
