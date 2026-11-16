#!/usr/bin/env python3
"""Resolve the reviewed classical composer selection to MusicBrainz Core MBIDs."""
import argparse
import json
import sqlite3
import uuid
from pathlib import Path

GROUPS = {
    "Baroque": "Johann Sebastian Bach|George Frideric Handel|Antonio Vivaldi|Domenico Scarlatti|Henry Purcell|Georg Philipp Telemann|Jean-Philippe Rameau|Arcangelo Corelli|Claudio Monteverdi|Jean-Baptiste Lully|François Couperin|Dieterich Buxtehude|Johann Pachelbel|Tomaso Giovanni Albinoni|Benedetto Marcello|Francesco Geminiani|Pietro Antonio Locatelli|Jan Dismas Zelenka|Giovanni Battista Pergolesi|Carl Philipp Emanuel Bach",
    "Classical": "Joseph Haydn|Wolfgang Amadeus Mozart|Ludwig van Beethoven|Christoph Willibald Gluck|Luigi Boccherini|Muzio Clementi|Jan Ladislav Dusík|Johann Nepomuk Hummel|Antonio Salieri|Luigi Cherubini|Carl Maria von Weber|Domenico Cimarosa|Giovanni Paisiello|Johann Christian Bach|Leopold Mozart|Carl Ditters von Dittersdorf|Carl Stamitz|Johann Stamitz|Thomas Arne|Ignace Joseph Pleyel",
    "Romantic": "Frédéric Chopin|Franz Liszt|Robert Schumann|Clara Schumann|Johannes Brahms|Richard Wagner|Giuseppe Verdi|Pyotr Ilyich Tchaikovsky|Antonín Dvořák|Bedřich Smetana|Edvard Grieg|Felix Mendelssohn|Hector Berlioz|Gustav Mahler|Anton Bruckner|Richard Strauss|Sergei Rachmaninoff|Alexander Scriabin|Modest Mussorgsky|Nikolai Rimsky-Korsakov|Alexander Borodin|Mikhail Glinka|Mily Balakirev|Anton Rubinstein|César Franck|Gabriel Fauré|Camille Saint-Saëns|Georges Bizet|Jules Massenet|Jacques Offenbach|Giacomo Puccini|Gaetano Donizetti|Vincenzo Bellini|Gioachino Rossini|Niccolò Paganini|Charles-Valentin Alkan|Charles Gounod|Édouard Lalo|Max Bruch|Edward Elgar",
    "Impressionist": "Claude Debussy|Maurice Ravel|Manuel de Falla|Erik Satie|Isaac Albéniz|Enrique Granados",
    "Modern": "Jean Sibelius|Carl Nielsen|Leoš Janáček|Béla Bartók|Igor Stravinsky|Sergei Prokofiev|Dmitri Shostakovich|Gustav Holst|Ralph Vaughan Williams|Benjamin Britten|George Gershwin|Aaron Copland|Charles Ives|Olivier Messiaen",
}


def resolve(database: Path):
    names = [(period, name) for period, values in GROUPS.items() for name in values.split("|")]
    if len(names) != 100 or len({name for _, name in names}) != 100:
        raise ValueError("reviewed selection must contain exactly 100 distinct names")
    db = sqlite3.connect(f"file:{database}?mode=ro", uri=True)
    try:
        result = []
        for period, name in names:
            rows = db.execute("""SELECT DISTINCT e.id,e.title FROM entities e
                JOIN external_identities x ON x.entityId=e.id
                LEFT JOIN aliases a ON a.entityId=e.id
                WHERE x.provider='musicbrainz' AND x.providerKind='composer'
                  AND (e.title=? OR a.normalized=?) ORDER BY e.title""", (name, _normal(name))).fetchall()
            exact = [row for row in rows if row[1] == name]
            rows = exact or rows
            if len(rows) != 1:
                raise ValueError(f"{name}: expected one verified Core composer identity, found {rows!r}")
            mbid, title = rows[0]
            if str(uuid.UUID(mbid)) != mbid:
                raise ValueError(f"{name}: invalid MBID {mbid}")
            result.append({"name": name, "coreName": title, "mbid": mbid, "period": period})
        if len({row["mbid"] for row in result}) != 100:
            raise ValueError("selection contains duplicate MusicBrainz identities")
        return result
    finally:
        db.close()


def _normal(value):
    import re
    import unicodedata
    value = "".join(c for c in unicodedata.normalize("NFD", value) if unicodedata.category(c) != "Mn").lower()
    value = "".join(c if c.isalpha() or c.isdigit() else " " for c in value)
    return re.sub(r"([a-z])([0-9])", r"\1 \2", value).strip()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("database", type=Path)
    parser.add_argument("output", type=Path)
    args = parser.parse_args()
    if args.output.exists():
        raise FileExistsError(args.output)
    args.output.write_text(json.dumps(resolve(args.database), ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
