# Catalogue licensing assessment

## MusicBrainz data boundary

The [official MusicBrainz database download documentation](https://musicbrainz.org/doc/MusicBrainz_Database/Download),
[database contents page](https://musicbrainz.org/doc/MusicBrainz_Database),
and [data license page](https://musicbrainz.org/doc/About/Data_License) state:

- Core data, including the `mbdump.tar.bz2` archive, is CC0 1.0.
- Supplementary dumps, including derived tags/ratings/annotations, statistics,
  search data, edit/editor data, and several related dumps, are CC BY-NC-SA 3.0.
- Live Data Feed replication packets are CC BY-NC-SA 3.0.

The verified archive is the official `mbdump.tar.bz2` Core file. The extractor's
table allowlist and reproduced output show only Core-derived entity, alias,
type and relationship fields. No supplementary archive, tag, rating,
annotation, genre association, edit, or derived-statistics input was used. The
final compressed asset exactly reproduces from that Core-only pipeline. The
curated catalogue is therefore demonstrably derived only from the CC0 Core
dump, together with Gould's selection list and generated schema/search/license
metadata.

The `rights` row and asset manifest identify the catalogue as CC0. That
metadata accurately describes the data derivation; it does not license other
project material. The script's generated `reviewState="approved"` field is
not treated as an owner decision.

## Separate project and third-party terms

- Gould application source: owner-approved GPL-3.0-or-later.
- Gould documentation and original documentation prose: owner-approved
  CC-BY-SA-4.0.
- MusicBrainz curated Core data: CC0-1.0, kept distinct from the project source
  and documentation licenses.
- Fonts, runtime dependencies, Wikimedia material, artwork and other
  third-party code/data/assets retain their respective notices and licenses;
  this investigation does not relicense or certify them.

No license file, source header, in-app notice, asset, or application behavior
was changed in this investigation.
