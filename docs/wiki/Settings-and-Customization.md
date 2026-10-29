# Settings & Customization — Gould

> Gould 0.12 development documentation. These features are implemented on `feature/gould-0.12`; full emulator and release acceptance is still in progress. The latest published release remains [Goldberg 0.11 Stable](https://github.com/stokonn/Gould/releases/tag/v0.11). Historical sections below retain their original evidence.


Ivory/Nocturne appearance, English/Czech, font scaling and existing appearance preferences are retained. Online catalogue search remains default-off until enabled in Settings; downloaded media and offline bundles retain their separate network and storage controls.

The bundled privacy notice describes request metadata, provider processing and the manual support/rights procedure. Privacy/support contact: stokonn.us@gmail.com. Backend Google OAuth, community writes and practice uploads remain disabled; optional local Google profile identity is separate.

Data & Storage exports a format-6/schema-11 portable JSON backup and retains readers for formats 1–5. JSON preserves recording metadata; the optional media ZIP is the separate transfer of local audio bytes. These exports are not encrypted. Choose their destination carefully and retain original backups. Restore validates the archive and requires explicit confirmation; active capture must end first. Missing audio remains explicitly unavailable rather than becoming a fabricated recording. Diagnostic recovery exports and CSV reports serve different purposes from portable backups.

Smart Practice transient analysis and explicit continuous WAV recording have distinct consent and privacy disclosures. Recorded audio stays in app-private local storage unless you explicitly export it. Neither starts an upload or cloud synchronization.

created by stokonn with love <3

<details>
<summary>Preserved earlier documentation and dated development history</summary>

# Settings & Customization

Goldberg 0.10.1 provides compact live previews for theme, typography, accent, card style, density and sorting. Choose Ivory, Nocturne or the system theme; editorial or system typography; accent, density and supported layout preferences. Android font scaling and reduced-motion settings remain relevant. Appearance reset must preserve profile identity, musical favorites, repertoire, drafts, history and badges.

Language supports Czech, English and system selection. User titles, notes, nicknames and original catalogue names are not translated.

Data & Storage provides explicit metered-media consent. It is off by default. Unmetered Wi-Fi permits small portrait downloads automatically. Optional composer biographies/large portraits are requested on their composer page and may be removed there. Cached images work offline. Cache clearing affects downloaded responses/media, not critical journal tables. Downloaded content is separate from practice data.

Historical Goldberg 0.10.1 backup behavior: portable journal/profile/settings data used format 4/schema 8 and retain import compatibility with formats 1–3 (schemas 4–7). Older app versions cannot import a new format-4 backup. Keep your original backups. Restore validates data separately and requires explicit replacement confirmation. Recovery exports differ from portable backups. Session and per-piece CSV exports are separate to avoid double-counting.

Google identity is optional. Credential Manager + Google ID use the public Web client ID supplied by the owner. On the 0.10 candidate, the owner reports successful sign-in, display-name/avatar import after consent, nickname/avatar override preservation, authenticated restart, sign-out, account switching and local-data preservation on a Samsung Galaxy A55. This is owner-reported manual verification, not Codex physical-device testing. Local profiles remain fully usable. No client secret belongs in the APK. Sign-out retains journal and local profile data. This is not cloud synchronization. See [official Credential Manager guidance](https://developer.android.com/identity/sign-in/credential-manager-siwg).

About centers the installed app icon/version and provides Introduction, GitHub and Privacy & licenses actions with a narrow-screen fallback. About retains the seven-tap Bach's Secret activation with its connected thirteen-note keyboard and a separate logo-long-press terminal easter egg. Neither requires downloaded artwork.

[Home](Home) · [FAQ](FAQ-and-Troubleshooting)

</details>
