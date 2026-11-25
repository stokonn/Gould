# Gould privacy notice — 0.15

**Updated November 25, 2026.** This notice describes a local-first Android application. Gould operates no product community backend, BFF, moderation system, admin API, community database or custom cloud backend. It does not authorize collection of personal practice data by any first-party service.

## Contact and scope

The operator is the project owner, publicly identified as **stokonn**, with privacy and support contact **stokonn.us@gmail.com**. Public identity/contact acceptability is owner-reported; no independent legal certification is claimed. Use this address for privacy questions, rights requests and support. Do not post private journals, credentials or personal screenshots in public GitHub issues.

## Personal practice data stays on your device

Gould stores repertoire, profile/avatar, practice history, notes, settings, badge dates and unfinished practice drafts in private application storage. Practice and saved music remain usable offline. There is no first-party server-side store for this data: no practice-journal, profile, microphone or backup-upload endpoint exists, because Gould does not operate a backend service. Nothing in this notice enables journal synchronization.

Gould does not add advertising, behavioural analytics, session replay or a crash-reporting SDK to the Android app. Third-party providers can still process operational metadata as described below. Bundled fonts make no web-font requests. App reminders are local and optional; they do not start microphone capture.

Smart Practice analyzes short-lived microphone samples on the device only after you explicitly start it, including while locked or in another app. Ambient sound may include voices. Analysis alone does not save audio, transcribe speech or identify compositions. Its bounded buffer is reused and cleared when capture ends. The separate explicit Record audio action saves continuous mono PCM/WAV in private local storage. Recordings are never uploaded automatically. Pause stops practice credit while an explicit recording continues; Stop recording or Finish ends that recording. Automatic silence pauses continue Smart listening. Android permissions, routes, storage or power controls can interrupt capture; interrupted original bytes are retained for explicit recovery/export, never automatic microphone resumption. Detection is experimental.

## Local files, exports and recovery

Personal data remains in app storage until you edit/delete it, replace data through a confirmed restore, clear app storage or uninstall. This includes weekly activity goals, manually entered activity dates/durations, owned cosmetic rewards and selected cosmetic loadout, and guided-tour completion/skip/seen-step state, alongside practice history and drafts. These records are used locally to calculate progress, streaks and rewards or resume the tour; they are not sent anywhere. Restore can retain destination snapshots, original draft/recovery bytes and completion evidence to prevent data loss or duplicate credit. Those retained recovery copies are not automatically deleted on a fixed timetable; explicit later management, clearing app storage or uninstalling is needed. Deleting a current record is not a promise that every recovery copy or previously exported file is erased.

Gould 0.15 format-8 JSON backups include journal/settings, activity goals and manual entries, cosmetic ownership/loadout and reward evidence, guided-tour state, practice draft/recovery material, recording metadata and earned event evidence, while excluding audio file bytes. Formats 1–7 remain importable. An explicitly chosen media ZIP also includes private saved and recoverable recording bytes, validates hashes before restore, and retains durable recovery receipts. Personal audio is separate from disposable caches and is deleted only by an explicit user deletion; that action does not erase previously exported copies or earned achievements. Google credentials, device permissions and network credentials are excluded. Community contribution drafts are separate local data and are not submitted to any service, because no submission service exists. Android automatic backup is disabled.

JSON backups, CSV exports and audio ZIP archives are unencrypted files sent to the document provider you choose; that provider may synchronize them elsewhere. CSV is not a restorable backup. Keep independent copies before restoration. Deleting app data does not delete exports held by your document provider or its retained copies. Downloaded catalogue/media caches are separate from critical journal data, can be refreshed or evicted, and can be cleared through app controls.

## Bundled catalogue and online search

The composer/work catalogue is bundled with the application and installed into a local offline store on first use; browsing it makes no network request. Music discovery uses third-party providers — primarily MusicBrainz, and optionally Wikidata, Wikipedia and Wikimedia Commons — contacted directly from the device only when you explicitly enable online catalogue search in Settings. That consent is default-off, per-query requests are debounced, and you can revoke it at any time. There is no Gould-operated catalogue service in front of these providers.

Requests necessarily involve connection metadata, including your IP address, request time and the query or identifiers you supplied, which the provider sees directly. Provider requests do not deliberately include practice notes, journal records or microphone samples. Database credentials, admin credentials and any first-party service credentials do not exist in the Android application, because there is no first-party service.

## Logging and retention

Gould itself persists no server-side application logs: there is no Gould-operated server. The application writes only local diagnostic data on the device (for example logcat lines and local caches), which you can clear with the app or device controls.

Third-party providers keep their own operational and security metadata according to their own policies, described below. This notice cannot establish deletion of provider-held copies.

An exact retention period for third-party provider-controlled operational/security metadata has not been verified. No zero-log or invented deletion-period claim is made. Ordinary support correspondence is retained for 90 days after closure, subject to justified legal exceptions and monthly deletion-eligibility review. This period does not govern provider-controlled copies or security records. Mail-provider Trash and backup retention can delay final deletion outside the operator's control.

## Third-party music and identity services

Android online music searches and information/media requests can contact MusicBrainz, Open Opus, Wikidata, Wikipedia and Wikimedia Commons/image servers. They can receive search terms or selected music identifiers, IP addresses and technical request metadata. These requests do not upload practice notes, journal records or microphone samples.

Daily quotations may request their source text from Project Gutenberg; bundled fallback quotations work offline. External Spotify, YouTube, IMSLP, MuseScore, GitHub and source/license links open services with their own privacy rules. Metered portrait/profile downloads require the media preference. Each provider controls its own service logging and retention; Gould does not promise to delete provider-held records. Music metadata, text and media have different licenses; future catalogue publication must preserve the approved source/license/attribution policy.

Google sign-in uses **Android Credential Manager** on the device. Selected account identifiers, imported names and photo references can be retained locally; a chosen nickname/avatar can remain after sign-out. ID tokens are not stored beyond the session and are never sent to any Gould-operated service, because none exists. Google profile use does not authorize community submissions or synchronize practice history. Profile image requests can contact Google image servers and respect the media download preference. Google profile name/avatar synchronization is a local, on-device update of your profile only.

## Your choices and requests

Use app settings to manage permissions, sign out of a local profile, export data and clear downloaded cache. Local editing, export and deletion do not require a Gould cloud account. We cannot remotely access, edit or erase a journal that exists only on your device. Manage separately any exports and copies held by your document provider.

Depending on applicable law and circumstances, you may have rights to information, access, correction, erasure, restriction, portability, objection and withdrawal of consent where processing relies on consent. You may also complain to the relevant supervisory authority, including the Czech Office for Personal Data Protection where applicable. These rights are not unconditional promises to erase data that must lawfully be retained.

Email **stokonn.us@gmail.com** to explain your request. Include only information needed to identify the issue or request; do not send passwords, tokens or your entire journal by default. Limited identity confirmation may be needed for a request involving data actually held by the operator. Correspondence necessarily includes your email address, message and any attachments you choose to provide, and is handled to respond to the request through the support mailbox. Email to the published Gmail address also passes through Google and your email provider under their mail-service policies. Applicable legal response requirements still apply. No marketing email service is enabled.

## Purposes, legal bases and support operation

For necessary third-party catalogue request/connection metadata initiated by your explicit search consent, Gould relies on Article 6(1)(f): legitimate interests in delivering the requested music metadata and protecting service availability/security. A proportionate purpose, necessity and balancing assessment supports this limited use with minimization, no account requirement and offline practice preserved. This does not authorize advertising, profiling or collecting practice data. Provider processing for its independent purposes is governed separately.

Voluntary support correspondence is handled separately under legitimate interests in responding to the sender's request, using only necessary message/contact details. Where GDPR requires handling a rights request, necessary processing and minimal deadline/outcome records rely on the applicable legal obligation under Article 6(1)(c). Rights evidence is reviewed for necessity; the ordinary-support 90 days is not represented as a statutory retention rule for every legal case.

The owner reports that Inbox and Spam access has been checked, with Tuesday/Friday inbox checks and closed-case/retention review on the first day of each month. Requests and applicable deadlines are tracked privately. Responding to applicable rights requests is required without undue delay, normally within one month; extensions and exceptions require their actual legal conditions. No Gmail monitoring or deletion automation is claimed.

You may object to processing based on legitimate interests using the contact above. Objections are assessed individually; security is not an automatic ground to reject every request. Gould does not create additional visitor tracking solely to identify a requester. Ask about data actually held; do not send credentials or a whole journal. Provider-held information may require the applicable provider procedure. Local practice remains available with no network at all.

There is no first-party backend Google OAuth, no community write path and no personal-data upload path, because Gould operates no backend. New purposes, providers, logging, accounts or functionality require reassessment.

## Further information

- MusicBrainz privacy: https://metabrainz.org/privacy
- Google privacy: https://policies.google.com/privacy
- Wikimedia privacy: https://foundation.wikimedia.org/wiki/Policy:Privacy_policy
- European data-subject rights: https://www.edpb.europa.eu/topics/key-gdpr-concepts/data-subject-rights_en
- Czech supervisory authority: https://uoou.gov.cz/
