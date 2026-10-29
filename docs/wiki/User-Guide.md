# User Guide — Gould

> Gould 0.12 development documentation. These features are implemented on `feature/gould-0.12`; full emulator and release acceptance is still in progress. The latest published release remains [Goldberg 0.11 Stable](https://github.com/stokonn/Gould/releases/tag/v0.11). Historical sections below retain their original evidence.


Use Library for your personal repertoire and the shared local catalogue, and Discover to explore sourced music records. Both tabs open the same canonical catalogue details. Public catalogue rows are labelled separately from music saved in your repertoire; open details to add an eligible work or open its existing repertoire entry.

Search uses saved catalogue data first. Automatic online provider search requires Settings consent. Source and licence information is available in Sources & credits. Existing cached metadata is retained; a local snapshot without its original provider response does not acquire approved rights merely by being copied.

Manual and Smart Practice, session review, piece attribution, history, profile and backups remain local. Keep unfinished recovery data until it has been successfully recovered or separately exported. Network or catalogue failures do not authorize deleting the journal or draft.

## Studio and local audio in 0.12

Studio keeps credited practice time separate from recording duration. Manual timing, Smart Practice and explicit audio recording share the existing practice coordinator and one microphone owner. Smart Practice is experimental. It analyzes transient sound to estimate active practice; it does not identify a piano perfectly. Recording starts only after explicit Record audio consent. Pause affects practice credit; an explicitly started take continues through silence until Stop recording or Finish & Review. Recovery never restarts the microphone automatically.

Expand Metronome for tempo and beats per bar. It stops when Studio leaves the foreground. Pieces played opens the session queue: choose, reorder or remove future intentions and use Undo for the latest queue change. Existing credited intervals remain assigned to the piece that earned them. Session and piece notes use the same durable draft as timing.

The upper-right Previous action opens separate Practice History and Recordings tabs. Local recordings retain the title/composer and piece changes captured during the take. Open a saved recording to play, pause or seek; export its WAV through the Android file picker. Playback stops before capture. Deletion requires confirmation. Interrupted audio offers explicit recovery or unchanged original-byte export; do not delete it while recovery is unresolved. Existing external recording links remain separate.

## Profile, badges and widgets in 0.12

Profile presents four centered statistics in a 2×2 grid. The editor offers composer and era suggestions while preserving manual entries. Draft edits survive interruption. Leaving a changed editor offers Save, Discard or Keep editing; successful persistence is required before a save is acknowledged.

Existing badges and unlock dates are retained. Six new badges use persisted practice, notes, verified recording and listening evidence. Unlock notices queue in the app and defer during active practice. Their View Badge action opens the corresponding badge.

Add Quick Practice and Weekly Activity from the Android launcher widget picker. Both open Studio without starting a timer or microphone. Quick Practice reflects the current or interrupted draft. Weekly Activity uses credited practice on Monday–Sunday local calendar days, with compact and expanded layouts. An unavailable or stale snapshot is labelled; it is not a newly verified zero. The widgets work from local data without online catalogue consent.

created by stokonn with love <3

<details>
<summary>Preserved earlier documentation and dated development history</summary>

# User Guide

## Home

Home shows today's practice, current streak, seven local calendar days, a daily sourced music quotation and recent pieces. It removes practice-start actions from Home. Zero activity is not drawn as a practice bar. Sessions use their recorded local practice day, consistently with historical statistics.

## Library and repertoire

Goldberg 0.10.1 uses one Library with All and My Repertoire filters. Search, sort and filter personal pieces and browse catalogue suggestions; add manually or from a catalogue work, and open piece details. Original titles, composer names and personal notes are retained. Library pins search/Add and uses compact licensed composer portraits with placeholders when unavailable. This release does not expand or repair the entire catalogue database.

## Practice and review

Practice is the entry point for starting sessions. Piece details select an intended piece and navigate there; they do not start timing. Timer calculations remain centralized. Multi-piece session parts and their notes are preserved. Pause/Resume and Finish & Review are the essential controls. Pieces expand when needed. New note entry is offered after saving in the Practice completed summary. Add pieces played creates untimed associations without extra credit. Add note offers measured piece notes or session notes; notes about untimed added pieces are labeled by title in the session note. Done saves optional notes and dismisses; skipping extras leaves the completed session saved. Later editing remains in History. Unsaved changes are explicitly indicated; keep the app open and retry persistence when necessary.

Recovery never automatically restarts the microphone. If a draft cannot be read, retry or export the unchanged data. A separately confirmed fresh session first archives the original payload transactionally; failure to archive prevents the fresh session. Exported recovery payloads are diagnostic data, not ordinary importable backups. Full history loads incrementally and supports per-piece history without duplicate sessions for multiple parts.

## Profile, statistics and badges

Local nickname and avatar editing remain independent of any account. Profile supports manually entered favorite artist, piece, era and biography, four compact statistics and up to five showcased earned badges. Use the avatar pencil to edit identity, biography and favorite preferences. The badge icon opens the collection; the showcase + opens selection directly from Profile, including removal/reordering and the five-badge limit. History is accessible from Home. Unlock dates and progress remain part of the journal. History has its own destination.

## Composer information

Catalogue data and original composer/work names are not automatically translated. Licensed small portraits may download on unmetered Wi-Fi; metered/cellular downloads need permission. Biography/large portrait content is optional and requested from composer details. Missing or unlicensed portraits use placeholders. Open portrait/source credits for artist, license and original source.

[Home](Home) · [Settings](Settings-and-Customization)

</details>
