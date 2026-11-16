#!/usr/bin/env python3
"""Check that the packaged catalogue is the exact reviewed 100-composer Core subset."""
import argparse
import gzip
import hashlib
import json
import shutil
import sqlite3
import tempfile
import uuid
from pathlib import Path

REQUIRED = {"Johann Sebastian Bach", "Wolfgang Amadeus Mozart", "Ludwig van Beethoven", "Frédéric Chopin",
            "Franz Liszt", "Claude Debussy", "Antonín Dvořák", "Bedřich Smetana"}


def check(database: Path | None, selection_path: Path, manifest_path: Path, compressed: Path | None = None):
    selection = json.loads(selection_path.read_text(encoding="utf-8"))
    ids = {row["mbid"] for row in selection}
    if len(selection) != 100 or len(ids) != 100 or not REQUIRED <= {row["name"] for row in selection}:
        raise AssertionError("selection must contain 100 unique MBIDs and all required composers")
    for row in selection:
        if str(uuid.UUID(row["mbid"])) != row["mbid"]:
            raise AssertionError(f"invalid MBID: {row}")
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    file = manifest["files"][0]
    if database is None:
        if compressed is None or not compressed.is_file():
            raise FileNotFoundError("curated catalogue database or .dbz asset is required")
        work = Path("app/build/tmp")
        work.mkdir(parents=True, exist_ok=True)
        with tempfile.TemporaryDirectory(prefix="gould-catalogue-check-", dir=work) as temp:
            database = Path(temp) / "catalogue.db"
            with gzip.open(compressed, "rb") as source, database.open("wb") as target:
                shutil.copyfileobj(source, target, 1024 * 1024)
            return _check_database(database, selection, manifest, compressed.stat().st_size)
    return _check_database(database, selection, manifest, compressed.stat().st_size if compressed and compressed.exists() else None)


def _check_database(database: Path, selection, manifest, compressed_bytes):
    file = manifest["files"][0]
    ids = {row["mbid"] for row in selection}
    sha = hashlib.sha256()
    with database.open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            sha.update(block)
    digest = sha.hexdigest()
    if manifest["state"] != "READY" or manifest["expectedBytes"] != database.stat().st_size or file["bytes"] != database.stat().st_size or file["sha256"] != digest:
        raise AssertionError("manifest does not match the generated database")
    if file["rights"]["license"] != "CC0-1.0" or manifest["providerKind"] != "catalogue":
        raise AssertionError("unexpected seed rights or provider")
    db = sqlite3.connect(f"file:{database}?mode=ro", uri=True)
    try:
        assert db.execute("PRAGMA integrity_check").fetchone()[0] == "ok"
        assert db.execute("PRAGMA foreign_key_check").fetchone() is None
        artists = {row[0] for row in db.execute("SELECT externalId FROM external_identities WHERE provider='musicbrainz' AND providerKind='composer'")}
        assert artists == ids, (len(artists), len(ids), artists ^ ids)
        composers = db.execute("SELECT count(*) FROM entities WHERE kind='artist'").fetchone()[0]
        works = db.execute("SELECT count(*) FROM entities WHERE kind='work'").fetchone()[0]
        credits = db.execute("SELECT count(*) FROM relationships WHERE type='credit' AND role='composer'").fetchone()[0]
        assert composers == 100 and works > 0 and credits >= works
        outside = db.execute("""SELECT count(*) FROM relationships r JOIN external_identities x
            ON x.entityId=r.toId AND x.provider='musicbrainz' AND x.providerKind='composer'
            WHERE r.type='credit' AND r.role='composer' AND x.externalId NOT IN (%s)""" % ",".join("?" for _ in ids), tuple(sorted(ids))).fetchone()[0]
        assert outside == 0
        assert db.execute("SELECT count(*) FROM sources WHERE provider<>'musicbrainz'").fetchone()[0] == 0
        assert db.execute("SELECT count(*) FROM external_identities WHERE provider<>'musicbrainz'").fetchone()[0] == 0
        assert db.execute("SELECT count(*) FROM documents").fetchone()[0] == 0
        assert db.execute("SELECT count(*) FROM media").fetchone()[0] == 0
        state = dict(db.execute("SELECT `key`,value FROM catalogue_state WHERE `key` LIKE 'musicbrainz_%'"))
        assert state["musicbrainz_snapshot"] == manifest["providerRevision"]
        assert state["musicbrainz_archive_sha256"] == "da4f1afcbca8ca54eb6362ae45173543f079e8491d097a376d060551d1a03335"
        return {"composers": composers, "works": works, "composer_relationships": credits,
                "database_bytes": database.stat().st_size, "database_sha256": digest,
                "compressed_bytes": compressed_bytes,
                "source_snapshot": manifest["providerRevision"], "selection_periods": sorted({row["period"] for row in selection})}
    finally:
        db.close()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", type=Path, default=Path("app/src/main/assets/catalogue/musicbrainz-core-20261114"))
    parser.add_argument("--database", type=Path)
    parser.add_argument("--selection", type=Path)
    parser.add_argument("--manifest", type=Path)
    parser.add_argument("--compressed", type=Path)
    args = parser.parse_args()
    print(json.dumps(check(args.database, args.selection or args.root.parent / "classical-composers.json",
                           args.manifest or args.root / "manifest.json", args.compressed or args.root / "catalogue.dbz"), indent=2, ensure_ascii=False))
