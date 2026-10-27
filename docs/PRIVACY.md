# Goldberg privacy notice — 0.11

**Review copy dated October 27, 2026. Owner approval is pending. The public Goldberg catalogue API is not activated.** This notice describes the existing local application and the read-only cloud candidate. It does not authorize Google backend authentication, community participation or collection of personal practice data.

## Contact and scope

The official privacy and support contact is **stokonn.us@gmail.com**. Use this address for privacy questions, rights requests and support. Do not post private journals, credentials or personal screenshots in public GitHub issues. This notice does not assert blanket legal compliance; the unresolved launch requirements below must be addressed before public activation.

## Personal practice data stays on your device

Goldberg stores repertoire, profile/avatar, practice history, notes, settings, badge dates and unfinished practice drafts in private application storage. Practice and saved music remain usable offline. The read-only catalogue service has no practice-journal, profile, microphone or backup-upload endpoint. This privacy update does not enable journal synchronization or switch on public catalogue networking.

Goldberg does not add advertising, behavioural analytics, session replay or a crash-reporting SDK to the Android app. Infrastructure providers can still process operational metadata as described below. Bundled fonts make no web-font requests. App reminders are local and optional; they do not start microphone capture.

Smart Practice analyzes short-lived microphone samples on the device only after you explicitly start it, including during an active session while the device is locked or another app is open. Ambient sound may include voices. Goldberg does not save raw audio, upload it, transcribe speech or identify compositions. Its bounded memory buffer is reused and cleared when capture ends. Explicit pause or finish releases capture; automatic silence pauses continue listening. Detection is experimental and Android permissions or power controls can interrupt it.

## Local files, exports and recovery

Personal data remains in app storage until you edit/delete it, replace data through a confirmed restore, clear app storage or uninstall. Restore can retain destination snapshots, original draft/recovery bytes and completion evidence to prevent data loss or duplicate credit. Those retained recovery copies are not automatically deleted on a fixed timetable; explicit later management, clearing app storage or uninstalling is needed. Deleting a current record is not a promise that every recovery copy or previously exported file is erased.

Goldberg 0.11 portable format-5 backups include personal journal/settings data and practice draft/recovery material. Google credentials, administrator privileges, device permissions and network credentials are excluded. Community contribution drafts are separate local data and are not submitted merely because a backup is made. Android automatic backup is disabled.

JSON backups and CSV exports are unencrypted files sent to the document provider you choose; that provider may synchronize them elsewhere. CSV is not a restorable backup. Keep independent copies before restoration. Deleting app data does not delete exports held by your document provider or its retained copies. Downloaded catalogue/media caches are separate from critical journal data, can be refreshed or evicted, and can be cleared through app controls.

## Online Goldberg catalogue

The prepared service allows only catalogue reads: `GET /v1/catalogue/overlay` and `GET /v1/catalogue/changes`. Authentication, administrator functions, submissions and other write routes are blocked. Google backend OAuth and community intake remain disabled. No approved catalogue records have been published by this deployment work.

When an online Goldberg catalogue request is made, Cloudflare Workers handles the HTTPS request and response. Cloudflare Hyperdrive connects the Worker to the existing Supabase PostgreSQL catalogue using verified TLS and a restricted server account. Hyperdrive query caching is disabled. Database credentials are not distributed in the Android APK or public responses.

Requests necessarily involve connection metadata, including the IP address seen by Cloudflare, request time, endpoint, HTTP headers and any catalogue filter or pagination cursor supplied. A response may include a generated request identifier. The catalogue handler does not deliberately write an end-user IP, account or practice record into the catalogue database. Database connections come through the service/pooler; this is not a claim that infrastructure providers receive or retain no metadata. Do not put private information or credentials in catalogue URLs.

The database origin is in Frankfurt. This does not mean every Cloudflare, Supabase, support or operational processing activity occurs only in the EU. International processing and applicable provider arrangements must be confirmed for launch; no EU-only or zero-transfer guarantee is made.

## Logging and retention

Persisted Worker application logs, invocation logs and traces are disabled in the deployed candidate. Diagnostic code uses fixed error categories rather than database credentials or raw driver messages. Authorized synthetic verification may use a temporary authenticated diagnostic session; that is separate from continuously persisted application request logging.

These settings do not disable every provider security, account, network, database or operational log. Supabase logging can include service connection metadata and query/error information according to provider configuration. Cloudflare and Supabase can process metadata for their infrastructure and security purposes. A dashboard's log-access window does not establish deletion of every provider-held copy.

An exact retention period for this deployment's provider-controlled operational/security metadata has not been verified. No zero-log or invented deletion-period claim is made. Final retention arrangements and the support mailbox's retention/deletion procedure remain unresolved launch requirements. The runtime credential's 90-day validity is a security setting, not a personal-data retention period.

## Third-party music and identity services

Android online music searches and information/media requests can contact Open Opus, MusicBrainz, Wikidata, Wikipedia and Wikimedia Commons/image servers. They can receive search terms or selected music identifiers, IP addresses and technical request metadata. These requests do not upload practice notes, journal records or microphone samples. The Goldberg cloud catalogue read handler does not act as an arbitrary proxy to these services.

Daily quotations may request their source text from Project Gutenberg; bundled fallback quotations work offline. External Spotify, YouTube, IMSLP, MuseScore, GitHub and source/license links open services with their own privacy rules. Metered portrait/profile downloads require the media preference. Each provider controls its own service logging and retention; Goldberg does not promise to delete provider-held records. Music metadata, text and media have different licenses; future catalogue publication must preserve the approved source/license/attribution policy.

The existing optional local Google profile feature can contact Google when used. Selected account identifiers, imported names and photo references can be retained locally; a chosen nickname/avatar can remain after sign-out. ID tokens are not stored, logged or sent to the Goldberg catalogue backend by this configuration. Google profile use does not authorize community submissions or synchronize practice history. Profile image requests can contact Google image servers and respect the media download preference. Backend Google sign-in remains disabled until separately configured and approved.

## Your choices and requests

Use app settings to manage permissions, sign out of a local profile, export data and clear downloaded cache. Local editing, export and deletion do not require a Goldberg cloud account. We cannot remotely access, edit or erase a journal that exists only on your device. Manage separately any exports and copies held by your document provider.

Depending on applicable law and circumstances, you may have rights to information, access, correction, erasure, restriction, portability, objection and withdrawal of consent where processing relies on consent. You may also complain to the relevant supervisory authority, including the Czech Office for Personal Data Protection where applicable. These rights are not unconditional promises to erase data that must lawfully be retained.

Email **stokonn.us@gmail.com** to explain your request. Include only information needed to identify the issue or request; do not send passwords, tokens or your entire journal by default. Limited identity confirmation may be needed for a request involving data actually held by the operator. Correspondence necessarily includes your email address, message and any attachments you choose to send, and is handled to respond to the request through the support mailbox. Email to the published Gmail address also passes through Google and your email provider under their mail-service policies. Applicable legal response requirements still apply. No marketing email service is enabled.

## Unresolved before public activation

Owner approval of this exact notice is required. The owner must also confirm the final public operator identification and applicable processing basis, provider logging/retention and international-processing arrangements, and a support-correspondence retention/deletion and rights-handling procedure. No missing fact, fixed period or legal approval is supplied by this draft. The previously accepted operator-model review is preserved; it is not a substitute for these specific unresolved details.

Public routes remain disabled until the required conditions are verified. Google OAuth, public community writes and personal-data collection are not authorized by approval of a read-only catalogue notice.

## Further information

- Cloudflare privacy: https://www.cloudflare.com/privacypolicy/
- Cloudflare data processing terms: https://www.cloudflare.com/cloudflare-customer-dpa/
- Supabase privacy: https://supabase.com/privacy
- Supabase data residency/transfers: https://supabase.com/legal/privacy-resources/data-residency-and-transfers-faq
- Google privacy: https://policies.google.com/privacy
- Wikimedia privacy: https://foundation.wikimedia.org/wiki/Policy:Privacy_policy
- MusicBrainz privacy: https://metabrainz.org/privacy
- European data-subject rights: https://www.edpb.europa.eu/topics/key-gdpr-concepts/data-subject-rights_en
- Czech supervisory authority: https://uoou.gov.cz/
