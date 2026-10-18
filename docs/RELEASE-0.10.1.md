# Goldberg 0.10.1 — Final Redesign Polish

Goldberg 0.10.1 refines the existing editorial interface and preserves local journal data, settings and upgrade identity.

- Home removes its gear and gently raises/enlarges the greeting; Settings remains in Profile.
- Library/Knihovna combines personal repertoire and catalogue browsing, with All/My Repertoire, search, sort and adaptive filters including difficulty.
- Practice keeps its timer and piece tracking. After saving, the completion summary offers omitted-piece associations and notes. Added pieces have no invented duration; measured time remains unchanged. Notes for untimed associations are explicitly labeled in the session note, using the existing backup-compatible storage.
- Profile places biography beside the avatar below the name, compacts statistics and provides direct showcase selection with the existing five-badge limit.
- About centers the installed app icon and actual version, with Introduction, GitHub and Privacy & licenses actions. Existing keyboard and pink Tux easter eggs remain.
- Appearance includes compact live previews using the selected theme, font, accent, card style, density and sort.
- Privacy notices now accurately describe optional Google identity, local microphone processing, exports and external requests. Bundled license texts remain accessible.

## Upgrade and compatibility

Version 0.10.1 / code 11; application com.opus.piano; database opus.db; schema 8 and backup format 4 unchanged. Backups in formats 1–3 remain importable. Install over the existing app; do not uninstall to bypass a signer mismatch. Export an independent backup before updates or replacement restoration.

See [verification](VERIFICATION-0.10.1.md), [legal audit](LEGAL-AUDIT-0.10.1.md), [naming inventory](NAMING-AUDIT-0.10.1.md) and the [planning-only 0.11 proposal](ROADMAP-0.11-DATABASE.md). The active checkout has not been renamed. Private design-reference images are excluded from Git and APK assets.

Goldberg 0.11 — Database Update remains planning-only. No catalogue expansion, database migration or cloud sync is implemented here.

created by stokonn with love <3
