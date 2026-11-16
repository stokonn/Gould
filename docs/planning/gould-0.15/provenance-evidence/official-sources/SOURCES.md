# Official source checks

Accessed 2026-11-16. These are first-party MusicBrainz/MetaBrainz references,
checked independently of Gould's local documentation. They are retained as
source URLs and concise findings rather than copied full pages.

| Official source | Finding used in this audit |
|---|---|
| <https://musicbrainz.org/doc/MusicBrainz_Database/Download> | Current download instructions identify the dump key UID as `MusicBrainz (MusicBrainz data dump signing key) <support@musicbrainz.org>` and publish primary fingerprint `D5E63B4BDCCE195642948684B8FC2375C777580F`. The page explains `.asc` detached signatures and SHA256SUMS verification; it also warns that a cryptographically good signature does not establish identity unless the consumer authenticates the key. |
| <https://github.com/metabrainz/musicbrainz-server/blob/master/INSTALL.md> | MetaBrainz's server installation instructions independently corroborate the short key ID `C777580F`, the same MusicBrainz data-dump signing UID, and use of that key for dump-signature verification. It does not publish the complete fingerprint. |
| <https://musicbrainz.org/doc/MusicBrainz_Database> | Defines core entity/relationship contents and supplementary contents; identifies annotations, user tags (including genre associations), ratings, derived statistics, search indexes, edit history, and non-personal user data as supplementary. It lists core data as CC0 and supplementary data as CC BY-NC-SA 3.0. |
| <https://musicbrainz.org/doc/About/Data_License> | Separately confirms Core CC0, supplementary CC BY-NC-SA 3.0, and Live Data Feed replication packets under CC BY-NC-SA 3.0. |
| <https://metabrainz.org/datasets/postgres-dumps> | MetaBrainz describes full PostgreSQL dumps and their separate Core and supplementary licensing. |
| <https://ftp.musicbrainz.org/pub/musicbrainz/data/fullexport/20261114-002230/> | The official historical snapshot index still listed `SHA256SUMS`, `SHA256SUMS.asc`, `mbdump.tar.bz2`, and `mbdump.tar.bz2.asc` on 2026-11-16. The index labels the archive approximately `7G`; the exact local byte count is recorded in `SNAPSHOT-IDENTITY.md`. |
| <https://tickets.metabrainz.org/browse/MBS-14311> | The key-rotation issue remained In Progress / Unresolved when checked; the reporter describes SHA-1 certificate/binding signatures that Sequoia rejects under its current policy. This is not a maintainer-confirmed replacement-key notice. |

The MusicBrainz download documentation and MetaBrainz installation instructions
are separate first-party publication paths. They corroborate the publisher's
key identity claim; they are not an external trust root or a third-party
certification. The key itself was fingerprinted from the preserved public
certificate, not trusted because it came from a keyserver. GnuPG's trust warning
and the unresolved key-rotation issue remain material qualifications.
