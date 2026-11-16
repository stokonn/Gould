#!/usr/bin/env python3
"""Verify the packaged curated seed, or opt in to checking preserved full-dump diagnostics."""
import hashlib
import gzip
import json
import re
import sqlite3
import argparse
from pathlib import Path

SNAPSHOT = "20261114-002230"
ARCHIVE_SHA256 = "da4f1afcbca8ca54eb6362ae45173543f079e8491d097a376d060551d1a03335"
EXPECTED = {"works": 2_902_937, "composer_relationships": 2_488_215,
            "works_linked_to_composers": 2_059_464, "composers": 202_867,
            "piano_title_candidates": 23_980}


def sha256(path):
    digest = hashlib.sha256()
    with path.open("rb") as source:
        for block in iter(lambda: source.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


def check(root, database_path=None):
    database = Path(database_path) if database_path else root / "catalogue.db"
    manifest = json.loads((root / "manifest.json").read_text(encoding="utf-8"))
    item = manifest["files"][0]
    assert manifest["state"] == "READY" and manifest["providerRevision"] == SNAPSHOT
    assert manifest["expectedBytes"] == database.stat().st_size == item["bytes"]
    assert item["sha256"] == sha256(database)
    assert item["rights"]["license"] == "CC0-1.0"
    assert manifest["createdAt"] and manifest["validatedAt"] and manifest["updatedAt"]
    packaged = root / "catalogue.dbz"
    if not packaged.exists():
        packaged = root / "catalogue.db.gz"
    if packaged.exists():
        digest = hashlib.sha256()
        size = 0
        with gzip.open(packaged, "rb") as stream:
            for block in iter(lambda: stream.read(1024 * 1024), b""):
                digest.update(block)
                size += len(block)
        assert size == item["bytes"] and digest.hexdigest() == item["sha256"]

    db = sqlite3.connect(f"file:{database}?mode=ro", uri=True)
    try:
        assert db.execute("PRAGMA integrity_check").fetchone()[0] == "ok"
        assert db.execute("PRAGMA foreign_key_check").fetchone() is None
        assert db.execute("SELECT identity_hash FROM room_master_table WHERE id=42").fetchone()[0] == "437013a61680fd9e34065937d3ef14a8"
        counts = {
            "works": db.execute("SELECT count(*) FROM entities WHERE kind='work'").fetchone()[0],
            "composers": db.execute("SELECT count(*) FROM artists").fetchone()[0],
            "composer_relationships": db.execute("SELECT count(*) FROM relationships WHERE type='credit' AND role='composer'").fetchone()[0],
            "works_linked_to_composers": db.execute("SELECT count(DISTINCT fromId) FROM relationships WHERE type='credit' AND role='composer'").fetchone()[0],
            "piano_title_candidates": db.execute("SELECT count(*) FROM entities WHERE kind='work' AND lower(title) LIKE '%piano%'").fetchone()[0],
        }
        assert counts == EXPECTED, counts
        assert db.execute("SELECT count(*) FROM assertions WHERE field='title' AND selected=1").fetchone()[0] == db.execute("SELECT count(*) FROM entities").fetchone()[0]
        assert db.execute("SELECT count(*) FROM sources WHERE provider<>'musicbrainz'").fetchone()[0] == 0
        assert db.execute("SELECT count(*) FROM external_identities WHERE provider<>'musicbrainz'").fetchone()[0] == 0
        assert db.execute("SELECT count(*) FROM rights WHERE license<>'CC0-1.0' OR reviewState<>'approved'").fetchone()[0] == 0
        assert db.execute("SELECT count(*) FROM documents").fetchone()[0] == 0
        assert db.execute("SELECT count(*) FROM media").fetchone()[0] == 0
        state = dict(db.execute("SELECT `key`,value FROM catalogue_state WHERE `key` LIKE 'musicbrainz_%'"))
        assert state["musicbrainz_archive_sha256"] == ARCHIVE_SHA256
        assert state["musicbrainz_direct_piano_link_status"] == "not_in_core_snapshot"
        assert state["musicbrainz_direct_piano_work_links"] == "0"

        duplicates = db.execute("""SELECT count(*),coalesce(sum(n-1),0),coalesce(sum(n),0) FROM (
            SELECT a.normalized,count(*) n FROM aliases a JOIN entities e ON e.id=a.entityId
            WHERE e.kind='work' AND a.language='und' AND a.aliasType='name'
            GROUP BY a.normalized HAVING count(*)>1)""").fetchone()
        assert db.execute("SELECT count(*) FROM aliases").fetchone()[0] > counts["works"]
        assert db.execute("SELECT count(*) FROM aliases WHERE language<>'und' AND entityId IN (SELECT id FROM entities WHERE kind='work')").fetchone()[0] > 0

        def verify_fts(kind, language_clause):
            row = db.execute(f"""SELECT a.rowid,a.value,a.normalized FROM aliases a JOIN entities e ON e.id=a.entityId
                WHERE e.kind=? AND {language_clause} ORDER BY a.rowid LIMIT 1""", (kind,)).fetchone()
            assert row is not None
            token = re.findall(r"[a-z0-9]+", row[2])
            assert token
            hit = db.execute("SELECT 1 FROM alias_fts WHERE alias_fts MATCH ? AND docid=? LIMIT 1", (token[0], row[0])).fetchone()
            assert hit is not None, (kind, row)
            return row

        composer_alias = verify_fts("artist", "a.language<>'und'")
        work_alt = verify_fts("work", "a.language<>'und'")
        sample = db.execute("""SELECT w.id,w.title,a.title FROM relationships r
            JOIN entities w ON w.id=r.fromId JOIN entities a ON a.id=r.toId
            WHERE r.type='credit' AND r.role='composer' AND lower(w.title) LIKE '%piano%'
            ORDER BY w.id LIMIT 5""").fetchall()
        assert sample
        return {"counts": counts, "duplicate_work_title_groups": duplicates[0],
                "duplicate_extra_work_records": duplicates[1], "records_in_duplicate_groups": duplicates[2],
                "alternative_title_aliases": db.execute("SELECT count(*) FROM aliases WHERE language<>'und' AND entityId IN (SELECT id FROM entities WHERE kind='work')").fetchone()[0],
                "composer_alias_search_example": composer_alias[1], "work_alternative_title_search_example": work_alt[1],
                "piano_title_work_samples": sample, "database_bytes": database.stat().st_size,
                "database_sha256": item["sha256"],
                "compressed_bundle_bytes": packaged.stat().st_size if packaged.exists() else None}
    finally:
        db.close()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--full-dump", action="store_true", help="check the preserved full-dump diagnostic artifact")
    parser.add_argument("--root", type=Path)
    parser.add_argument("--database", type=Path)
    args = parser.parse_args()
    if args.full_dump:
        root = args.root or Path(".gould-015-diagnostics/full-dump-superseded")
        database = args.database or Path(".gould-015-diagnostics/generated-catalogue/catalogue.db")
        result = check(root, database)
    else:
        from check_curated_catalogue import check as check_curated
        root = args.root or Path("app/src/main/assets/catalogue/musicbrainz-core-20261114")
        result = check_curated(args.database, root.parent / "classical-composers.json",
                               root / "manifest.json", root / "catalogue.dbz")
    print(json.dumps(result, indent=2, ensure_ascii=False))
