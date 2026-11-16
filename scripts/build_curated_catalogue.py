#!/usr/bin/env python3
"""Build a compact Room catalogue from selected MBIDs in an inspected Core database."""
import argparse
import hashlib
import json
import sqlite3
import uuid
from pathlib import Path

NAMESPACE = uuid.UUID("d4e6c45a-7528-5f53-9c75-ff7ee7ea8122")
LICENSE_URL = "https://creativecommons.org/publicdomain/zero/1.0/"
SOURCE_DATABASE_SHA256 = "7dcd72951f21f3647c7a4e6e08efddba6855debe731156a6df30ea4f6dbadce5"


def sha256(path):
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


def build(source_path: Path, selection_path: Path, output_dir: Path):
    selection = json.loads(selection_path.read_text(encoding="utf-8"))
    if len(selection) != 100 or len({row["mbid"] for row in selection}) != 100:
        raise ValueError("selection must contain exactly 100 unique MBIDs")
    if sha256(source_path) != SOURCE_DATABASE_SHA256:
        raise ValueError("source database SHA-256 mismatch")
    output_dir.mkdir(parents=True, exist_ok=True)
    output = output_dir / "catalogue.db"
    manifest_path = output_dir / "manifest.json"
    if output.exists() or manifest_path.exists():
        raise FileExistsError(output)
    source = sqlite3.connect(f"file:{source_path}?mode=ro", uri=True)
    target = sqlite3.connect(output)
    try:
        target.execute("PRAGMA foreign_keys=OFF")
        target.execute("PRAGMA journal_mode=DELETE")
        target.execute("PRAGMA synchronous=NORMAL")
        source_tables = source.execute("SELECT name,sql FROM sqlite_master WHERE type='table' AND sql IS NOT NULL ORDER BY name").fetchall()
        for name, sql in source_tables:
            if name.startswith("alias_fts_") or name in {"sqlite_sequence", "alias_fts"}:
                continue
            target.execute(sql)
        source.execute("CREATE TEMP TABLE keep_ids(id TEXT PRIMARY KEY)")
        source.executemany("INSERT INTO keep_ids VALUES(?)", [(row["mbid"],) for row in selection])
        target.execute("CREATE TEMP TABLE keep_ids(id TEXT PRIMARY KEY)")
        target.executemany("INSERT INTO keep_ids VALUES(?)", [(row["mbid"],) for row in selection])
        # MBIDs are entity IDs in the generated Core catalogue. Keep every available
        # work with an actual composer credit to one of the reviewed artists.
        works = source.execute("""SELECT DISTINCT r.fromId FROM relationships r
            JOIN keep_ids k ON k.id=r.toId
            WHERE r.type='credit' AND r.role='composer' AND r.fromKind='work' AND r.toKind='artist'""").fetchall()
        target.executemany("INSERT INTO keep_ids VALUES(?)", works)
        source.executemany("INSERT INTO keep_ids VALUES(?)", works)
        keep_count = target.execute("SELECT count(*) FROM keep_ids").fetchone()[0]

        for name, _ in source_tables:
            if name.startswith("alias_fts_") or name in {"sqlite_sequence", "alias_fts"}:
                continue
            cols = [row[1] for row in source.execute(f"PRAGMA table_info({name})")]
            col_sql = ",".join('"' + col.replace('"', '""') + '"' for col in cols)
            if name in {"entities", "artists", "works", "arrangements", "albums", "collections", "releases", "recordings"}:
                where = "id IN (SELECT id FROM keep_ids)"
            elif name in {"aliases", "assertions", "external_identities", "identity_revisions"}:
                where = "entityId IN (SELECT id FROM keep_ids)"
            elif name == "relationships":
                where = "fromId IN (SELECT id FROM keep_ids) AND toId IN (SELECT id FROM keep_ids)"
            elif name == "redirect_revisions":
                where = "sourceId IN (SELECT id FROM keep_ids) AND (targetId IS NULL OR targetId IN (SELECT id FROM keep_ids))"
            elif name == "documents":
                where = "entityId IN (SELECT id FROM keep_ids)"
            else:
                where = "1"
            rows = source.execute(f"SELECT {col_sql} FROM \"{name}\" WHERE {where}")
            target.executemany(f"INSERT INTO \"{name}\"({col_sql}) VALUES({','.join('?' for _ in cols)})", rows)

        fts_sql = source.execute("SELECT sql FROM sqlite_master WHERE name='alias_fts'").fetchone()[0]
        target.execute(fts_sql)
        target.execute("INSERT INTO alias_fts(docid,normalized) SELECT rowid,normalized FROM aliases")
        indexes = source.execute("SELECT sql FROM sqlite_master WHERE type='index' AND sql IS NOT NULL").fetchall()
        for (sql,) in indexes:
            try:
                target.execute(sql)
            except sqlite3.Error as error:
                raise sqlite3.Error(f"creating catalogue index failed: {sql}: {error}") from error
        triggers = source.execute("SELECT sql FROM sqlite_master WHERE type='trigger' AND sql IS NOT NULL").fetchall()
        for (sql,) in triggers:
            target.execute(sql)

        composers = target.execute("SELECT count(*) FROM entities WHERE kind='artist'").fetchone()[0]
        work_count = target.execute("SELECT count(*) FROM entities WHERE kind='work'").fetchone()[0]
        credits = target.execute("SELECT count(*) FROM relationships WHERE type='credit' AND role='composer'").fetchone()[0]
        if composers != 100 or work_count == 0 or credits == 0:
            raise ValueError(f"unexpected curated counts: composers={composers}, works={work_count}, credits={credits}")
        scope = f"100 reviewed MusicBrainz composer MBIDs; {work_count} Core works with composer credits; Core aliases and relationships"
        target.execute("UPDATE catalogue_state SET value=? WHERE key='musicbrainz_composers'", (str(composers),))
        target.execute("UPDATE catalogue_state SET value=? WHERE key='musicbrainz_works'", (str(work_count),))
        target.execute("UPDATE catalogue_state SET value=? WHERE key='musicbrainz_composer_relationships'", (str(credits),))
        target.execute("UPDATE catalogue_state SET value=? WHERE key='musicbrainz_works_linked_to_composers'", (str(work_count),))
        target.execute("UPDATE catalogue_state SET value=? WHERE key='musicbrainz_scope'", (scope,))
        target.commit()
        if target.execute("PRAGMA integrity_check").fetchone()[0] != "ok":
            raise ValueError("curated catalogue integrity_check failed")
        if target.execute("PRAGMA foreign_key_check").fetchone() is not None:
            raise ValueError("curated catalogue foreign_key_check failed")
        identity_hash = source.execute("SELECT identity_hash FROM room_master_table WHERE id=42").fetchone()[0]
        if target.execute("SELECT identity_hash FROM room_master_table WHERE id=42").fetchone()[0] != identity_hash:
            raise ValueError("curated Room identity hash differs from source schema")
        size = output.stat().st_size
        digest = sha256(output)
        source_row = source.execute("SELECT recordId,revision,payloadSha256,retrievedAt,sourceUrl FROM sources WHERE provider='musicbrainz' LIMIT 1").fetchone()
        snapshot, _, archive_hash, retrieved_at, source_url = source_row
        dataset_id = str(uuid.uuid5(NAMESPACE, "curated-classical:" + snapshot + ":" + digest))
        manifest = {
            "formatVersion": 1, "bundleId": str(uuid.uuid5(NAMESPACE, dataset_id)), "type": "COLLECTION",
            "entityId": dataset_id, "schemaVersion": 1, "revision": int(snapshot.replace("-", "")), "language": "und",
            "createdAt": 1794615750000, "validatedAt": 1794615750000, "dependencies": [], "expectedBytes": size,
            "files": [{"sha256": digest, "bytes": size, "mime": "application/vnd.sqlite3", "relativePath": "catalogue.db", "language": None,
                       "rights": {"license": "CC0-1.0", "licenseUrl": LICENSE_URL,
                                  "attribution": f"MusicBrainz Core snapshot {snapshot}; CC0 1.0 Universal; curated to 100 selected composer MBIDs",
                                  "sourceName": "MusicBrainz", "sourceUrl": source_url}}],
            "providerKind": "catalogue", "providerRevision": snapshot, "attribution": f"MusicBrainz Core snapshot {snapshot}; curated 100-composer offline seed",
            "minReaderVersion": 1, "state": "READY", "progress": {"completedBytes": size, "totalBytes": size, "completedFiles": 1},
            "lastError": None, "updatedAt": 1794615750000,
        }
        manifest_path.write_text(json.dumps(manifest, ensure_ascii=False, sort_keys=True, separators=(",", ":")) + "\n", encoding="utf-8")
        return {"composers": composers, "works": work_count, "composer_relationships": credits, "database_bytes": size, "database_sha256": digest, "scope_entity_count": keep_count, "source_snapshot": snapshot, "source_archive_sha256": archive_hash}
    except BaseException:
        target.close()
        output.unlink(missing_ok=True)
        Path(str(output) + "-journal").unlink(missing_ok=True)
        Path(str(output) + "-wal").unlink(missing_ok=True)
        Path(str(output) + "-shm").unlink(missing_ok=True)
        raise
    finally:
        source.close()
        target.close()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("source", type=Path)
    parser.add_argument("selection", type=Path)
    parser.add_argument("output_dir", type=Path)
    args = parser.parse_args()
    print(json.dumps(build(args.source, args.selection, args.output_dir), indent=2))
