#!/usr/bin/env python3
"""Generate the bundled Gould catalogue from one checksum-pinned MusicBrainz Core dump."""
import argparse
import datetime
import hashlib
import json
import re
import sqlite3
import tarfile
import time
import unicodedata
import uuid
from pathlib import Path

SNAPSHOT_ID = "20261114-002230"
CORE_SNAPSHOT_SHA256 = "da4f1afcbca8ca54eb6362ae45173543f079e8491d097a376d060551d1a03335"
APPROVED_FINGERPRINT = "D5E63B4BDCCE195642948684B8FC2375C777580F"
COMPOSER_RELATIONSHIP_GID = "d59d99ea-23d4-4a80-b066-edca32ee158f"
PIANO_INSTRUMENT_GID = "b3eac5f9-7859-4416-ac39-7154e2e8d348"
LICENSE_URL = "https://creativecommons.org/publicdomain/zero/1.0/"
DOWNLOAD_URL = f"https://data.metabrainz.org/pub/musicbrainz/data/fullexport/{SNAPSHOT_ID}/mbdump.tar.bz2"
DOCS_URL = "https://musicbrainz.org/doc/MusicBrainz_Database/Download"
ATTRIBUTION = f"MusicBrainz Core snapshot {SNAPSHOT_ID}; CC0 1.0 Universal"
NAMESPACE = uuid.UUID("d4e6c45a-7528-5f53-9c75-ff7ee7ea8122")
PINNED_COUNTS = {"works": 2_902_937, "works_linked_to_composers": 2_059_464, "composer_relationships": 2_488_215}
PROGRESS_INTERVAL = 100_000
CORE_PARSER_VERSION = 1
TABLES = {
    "artist": "artists_raw", "artist_type": "artist_types_raw", "artist_alias_type": "artist_alias_types_raw",
    "artist_alias": "artist_aliases_raw", "work": "works_raw", "work_type": "work_types_raw",
    "work_alias_type": "work_alias_types_raw", "work_alias": "work_aliases_raw", "instrument": "instruments_raw",
    "l_artist_work": "artist_work_raw", "l_instrument_work": "instrument_work_raw", "link": "links_raw", "link_type": "link_types_raw",
}


def _hash(path):
    digest = hashlib.sha256()
    with Path(path).open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def _decoded(columns, index):
    value = columns[index].decode("utf-8")
    return None if value in ("", "\\N") else value


def _normal(value):
    value = "".join(c for c in unicodedata.normalize("NFD", value) if unicodedata.category(c) != "Mn").lower()
    value = "".join(c if c.isalpha() or c.isdigit() else " " for c in value)
    return re.sub(r"([a-z])([0-9])", r"\1 \2", value).strip()


def _valid_mbid(value):
    parsed = str(uuid.UUID(value))
    if parsed != value:
        raise ValueError(f"noncanonical MusicBrainz UUID: {value}")
    return value


def _create_database(db, schema):
    for query in schema["setupQueries"]:
        db.execute(query)
    for entity in schema["entities"]:
        db.execute(entity["createSql"].replace("${TABLE_NAME}", entity["tableName"]))
    db.execute("PRAGMA user_version=1")
    db.executescript("""
    CREATE TABLE IF NOT EXISTS artists_raw(id INTEGER,gid TEXT,name TEXT,sort_name TEXT,birth_y TEXT,birth_m TEXT,birth_d TEXT,death_y TEXT,death_m TEXT,death_d TEXT,type INTEGER,comment TEXT);
    CREATE TABLE IF NOT EXISTS artist_types_raw(id INTEGER,name TEXT);
    CREATE TABLE IF NOT EXISTS artist_alias_types_raw(id INTEGER,name TEXT);
    CREATE TABLE IF NOT EXISTS artist_aliases_raw(id INTEGER,artist INTEGER,name TEXT,locale TEXT,type INTEGER);
    CREATE TABLE IF NOT EXISTS works_raw(id INTEGER,gid TEXT,name TEXT,type INTEGER,comment TEXT);
    CREATE TABLE IF NOT EXISTS work_types_raw(id INTEGER,name TEXT);
    CREATE TABLE IF NOT EXISTS work_alias_types_raw(id INTEGER,name TEXT);
    CREATE TABLE IF NOT EXISTS work_aliases_raw(id INTEGER,work INTEGER,name TEXT,locale TEXT,type INTEGER);
    CREATE TABLE IF NOT EXISTS instruments_raw(id INTEGER,gid TEXT,name TEXT);
    CREATE TABLE IF NOT EXISTS artist_work_raw(id INTEGER,link INTEGER,entity0 INTEGER,entity1 INTEGER);
    CREATE TABLE IF NOT EXISTS instrument_work_raw(id INTEGER,link INTEGER,entity0 INTEGER,entity1 INTEGER);
    CREATE TABLE IF NOT EXISTS links_raw(id INTEGER,link_type INTEGER);
    CREATE TABLE IF NOT EXISTS link_types_raw(id INTEGER,gid TEXT,entity_type0 TEXT,entity_type1 TEXT,name TEXT);
    CREATE TABLE IF NOT EXISTS core_load_state(table_name TEXT PRIMARY KEY,row_count INTEGER NOT NULL,source_sha256 TEXT NOT NULL,parser_version INTEGER NOT NULL);
    """)


def _row(table, columns):
    if table == "artists_raw":
        return (int(columns[0]), _decoded(columns, 1), _decoded(columns, 2), _decoded(columns, 3),
                _decoded(columns, 4), _decoded(columns, 5), _decoded(columns, 6), _decoded(columns, 7),
                _decoded(columns, 8), _decoded(columns, 9), int(columns[10]) if _decoded(columns, 10) else None,
                _decoded(columns, 13))
    if table == "artist_aliases_raw":
        return int(columns[0]), int(columns[1]), _decoded(columns, 2), _decoded(columns, 3), int(columns[6]) if _decoded(columns, 6) else None
    if table == "works_raw":
        return int(columns[0]), _decoded(columns, 1), _decoded(columns, 2), int(columns[3]) if _decoded(columns, 3) else None, _decoded(columns, 4)
    if table == "work_aliases_raw":
        return int(columns[0]), int(columns[1]), _decoded(columns, 2), _decoded(columns, 3), int(columns[6]) if _decoded(columns, 6) else None
    if table in ("artist_types_raw", "artist_alias_types_raw", "work_types_raw", "work_alias_types_raw"):
        return int(columns[0]), _decoded(columns, 1)
    if table == "instruments_raw":
        return int(columns[0]), _decoded(columns, 1), _decoded(columns, 2)
    if table in ("artist_work_raw", "instrument_work_raw"):
        # MusicBrainz l_* layout is id, link, entity0, entity1.
        return int(columns[0]), int(columns[1]), int(columns[2]), int(columns[3])
    if table == "links_raw":
        return int(columns[0]), int(columns[1])
    if table == "link_types_raw":
        return int(columns[0]), _decoded(columns, 3), _decoded(columns, 4), _decoded(columns, 5), _decoded(columns, 6)
    raise ValueError(f"unhandled Core table: {table}")


def _load_core(db, archive, archive_sha256):
    started = time.monotonic()
    saved_rows = {name: (rows, digest, parser) for name, rows, digest, parser in db.execute("SELECT table_name,row_count,source_sha256,parser_version FROM core_load_state")}
    complete = saved_rows.get("__archive_complete__")
    if complete == (0, archive_sha256, CORE_PARSER_VERSION):
        counts = {name: rows for name, (rows, digest, parser) in saved_rows.items() if name != "__archive_complete__"}
        required = {"artist", "artist_type", "work", "work_type", "l_artist_work", "link", "link_type"}
        if required <= counts.keys():
            print(f"phase=read-core resumed-complete tables={len(counts)} elapsed={time.monotonic() - started:.0f}s", flush=True)
            return counts
    counts = {}
    placeholders = {}
    with tarfile.open(archive, mode="r|bz2") as source:
        for member in source:
            if not member.isfile() or not member.name.startswith("mbdump/"):
                continue
            table_name = member.name.removeprefix("mbdump/")
            target = TABLES.get(table_name)
            if target is None:
                continue
            stream = source.extractfile(member)
            if stream is None:
                raise ValueError(f"could not read Core member {member.name}")
            saved = saved_rows.get(table_name)
            if saved is None or saved[1:] != (archive_sha256, CORE_PARSER_VERSION):
                db.execute(f"DELETE FROM {target}")
                db.execute("DELETE FROM core_load_state WHERE table_name=?", (table_name,))
                db.commit()
                saved_count = 0
            else:
                saved_count = saved[0]
            batch = []
            count = 0
            for line in stream:
                count += 1
                if count <= saved_count:
                    continue
                columns = line.rstrip(b"\n").split(b"\t")
                batch.append(_row(target, columns))
                if count % PROGRESS_INTERVAL == 0:
                    print(f"phase=read table={table_name} rows={count:,} checkpoint={saved_count:,} elapsed={time.monotonic() - started:.0f}s", flush=True)
                if len(batch) >= 20_000:
                    if target not in placeholders:
                        placeholders[target] = ",".join("?" for _ in batch[0])
                    db.executemany(f"INSERT INTO {target} VALUES({placeholders[target]})", batch)
                    batch.clear()
                    db.execute("INSERT OR REPLACE INTO core_load_state VALUES(?,?,?,?)", (table_name,count,archive_sha256,CORE_PARSER_VERSION))
                    db.commit()
            if batch:
                if target not in placeholders:
                    placeholders[target] = ",".join("?" for _ in batch[0])
                db.executemany(f"INSERT INTO {target} VALUES({placeholders[target]})", batch)
            if count < saved_count:
                raise ValueError(f"checkpoint row count exceeds source table {table_name}: {saved_count} > {count}")
            if count != saved_count:
                db.execute("INSERT OR REPLACE INTO core_load_state VALUES(?,?,?,?)", (table_name,count,archive_sha256,CORE_PARSER_VERSION))
                db.commit()
            counts[table_name] = count
            print(f"phase=read table={table_name} rows={count:,} complete elapsed={time.monotonic() - started:.0f}s", flush=True)
    required = {"artist", "artist_type", "work", "work_type", "l_artist_work", "link", "link_type"}
    missing = required - counts.keys()
    if missing:
        raise ValueError(f"required MusicBrainz Core tables are absent: {', '.join(sorted(missing))}")
    db.execute("INSERT OR REPLACE INTO core_load_state VALUES('__archive_complete__',0,?,?)", (archive_sha256, CORE_PARSER_VERSION))
    db.commit()
    return counts


def _date(year, month, day):
    if not year or int(year) == 0:
        return None, None
    precision = "year"
    result = f"{int(year):04d}"
    if month and int(month) != 0:
        result += f"-{int(month):02d}"
        precision = "month"
        if day and int(day) != 0:
            result += f"-{int(day):02d}"
            precision = "day"
    return result, precision


def _assertion_id(entity_id, field):
    return str(uuid.uuid5(NAMESPACE, f"assertion:{entity_id}:{field}"))


def _create_indices_and_fts(db, schema):
    for entity in schema["entities"]:
        for index in entity.get("indices", []):
            db.execute(index["createSql"].replace("${TABLE_NAME}", entity["tableName"]))
    db.execute("INSERT INTO alias_fts(docid,normalized) SELECT rowid,normalized FROM aliases")
    db.executescript("""
    CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_alias_fts_BEFORE_UPDATE BEFORE UPDATE ON aliases BEGIN DELETE FROM alias_fts WHERE docid=OLD.rowid; END;
    CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_alias_fts_BEFORE_DELETE BEFORE DELETE ON aliases BEGIN DELETE FROM alias_fts WHERE docid=OLD.rowid; END;
    CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_alias_fts_AFTER_UPDATE AFTER UPDATE ON aliases BEGIN INSERT INTO alias_fts(docid,normalized) VALUES(NEW.rowid,NEW.normalized); END;
    CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_alias_fts_AFTER_INSERT AFTER INSERT ON aliases BEGIN INSERT INTO alias_fts(docid,normalized) VALUES(NEW.rowid,NEW.normalized); END;
    """)


def build_catalogue(archive, output, *, expected_sha256=CORE_SNAPSHOT_SHA256, expected_counts=PINNED_COUNTS,
                    schema_path=Path("app/schemas/com.opus.piano.data.catalogue.CatalogueDatabase/1.json"),
                    retrieved_at="2026-11-15T00:56:00+01:00"):
    archive, output = Path(archive), Path(output)
    if not archive.is_file() or _hash(archive) != expected_sha256:
        raise ValueError("archive SHA-256 mismatch")
    if output.exists() and any(p.name not in {"catalogue.db", "catalogue.db-journal", "catalogue.db-wal", "catalogue.db-shm"} for p in output.iterdir()):
        raise ValueError("output directory has files not owned by this generator; refusing to overwrite")
    output.mkdir(parents=True, exist_ok=True)
    schema = json.loads(Path(schema_path).read_text(encoding="utf-8"))["database"]
    db_path = output / "catalogue.db"
    db = sqlite3.connect(db_path)
    started = time.monotonic()
    try:
        db.execute("PRAGMA foreign_keys=ON")
        db.execute("PRAGMA journal_mode=DELETE")
        db.execute("PRAGMA synchronous=NORMAL")
        # The only generated file is under the worktree; SQLite temporary sorts stay in memory.
        db.execute("PRAGMA temp_store=MEMORY")
        _create_database(db, schema)
        # Room's composite foreign keys require the parent unique index before first insertion.
        _create_indices_and_fts(db, schema)
        print("phase=read-core starting", flush=True)
        table_counts = _load_core(db, archive, expected_sha256)
        print(f"phase=read-core complete tables={len(table_counts)} elapsed={time.monotonic() - started:.0f}s", flush=True)
        raw_indices = ("raw_law_link_idx ON artist_work_raw(link)", "raw_links_type_idx ON links_raw(link_type)",
                       "raw_artist_id_idx ON artists_raw(id)", "raw_work_id_idx ON works_raw(id)",
                       "raw_links_id_idx ON links_raw(id)", "raw_link_type_gid_idx ON link_types_raw(gid)",
                       "raw_instrument_name_idx ON instruments_raw(name)")
        for index in raw_indices:
            began = time.monotonic()
            print(f"phase=index start={index}", flush=True)
            db.execute(f"CREATE INDEX IF NOT EXISTS {index}")
            print(f"phase=index complete={index} elapsed={time.monotonic() - began:.0f}s", flush=True)
        composer_type = db.execute("SELECT id,entity_type0,entity_type1 FROM link_types_raw WHERE gid=?", (COMPOSER_RELATIONSHIP_GID,)).fetchone()
        if composer_type != (168, "artist", "work"):
            raise ValueError(f"unexpected Composer relationship schema: {composer_type!r}")
        db.execute("CREATE TEMP TABLE selected_artists(id INTEGER PRIMARY KEY)")
        db.execute("""INSERT INTO selected_artists SELECT DISTINCT r.entity0 FROM artist_work_raw r
            JOIN links_raw l ON l.id=r.link WHERE l.link_type=?""", (composer_type[0],))
        counts = {
            "works": db.execute("SELECT count(*) FROM works_raw").fetchone()[0],
            "composers": db.execute("SELECT count(*) FROM selected_artists").fetchone()[0],
            "composer_relationships": db.execute("SELECT count(*) FROM artist_work_raw r JOIN links_raw l ON l.id=r.link WHERE l.link_type=?", (composer_type[0],)).fetchone()[0],
            "works_linked_to_composers": db.execute("SELECT count(DISTINCT r.entity1) FROM artist_work_raw r JOIN links_raw l ON l.id=r.link WHERE l.link_type=?", (composer_type[0],)).fetchone()[0],
            "title_contains_piano_candidates": db.execute("SELECT count(*) FROM works_raw WHERE lower(name) LIKE '%piano%'").fetchone()[0],
            "direct_piano_instrument_links": 0,
        }
        if "l_instrument_work" in table_counts:
            piano = db.execute("SELECT id FROM instruments_raw WHERE gid=?", (PIANO_INSTRUMENT_GID,)).fetchone()
            if piano:
                counts["direct_piano_instrument_links"] = db.execute("SELECT count(DISTINCT entity1) FROM instrument_work_raw WHERE entity0=?", (piano[0],)).fetchone()[0]
        if expected_counts and any(counts[key] != value for key, value in expected_counts.items()):
            raise ValueError(f"pinned snapshot counts differ: {counts!r}")
        wrong_endpoints = db.execute("""SELECT 1 FROM artist_work_raw r JOIN links_raw l ON l.id=r.link
            WHERE l.link_type=? AND (NOT EXISTS(SELECT 1 FROM artists_raw a WHERE a.id=r.entity0)
            OR NOT EXISTS(SELECT 1 FROM works_raw w WHERE w.id=r.entity1)) LIMIT 1""", (composer_type[0],)).fetchone()
        if wrong_endpoints:
            raise ValueError("Composer relationship endpoint is absent from Core artist/work tables")

        dataset_id = str(uuid.uuid5(NAMESPACE, "dataset:" + SNAPSHOT_ID))
        source_id = str(uuid.uuid5(NAMESPACE, "source:musicbrainz:" + SNAPSHOT_ID + ":" + expected_sha256))
        rights_id = str(uuid.uuid5(NAMESPACE, "rights:musicbrainz-core:" + expected_sha256))
        with db:
            print("phase=populate entities/aliases/assertions/relationships starting", flush=True)
            db.execute("INSERT INTO sources VALUES(?,?,?,?,?,?,?)", (source_id, "musicbrainz", SNAPSHOT_ID, expected_sha256,
                expected_sha256, retrieved_at, DOWNLOAD_URL))
            db.execute("INSERT INTO rights VALUES(?,?,?,?,?,?)", (rights_id, "CC0-1.0", LICENSE_URL, ATTRIBUTION, "approved", ""))
            db.execute("""INSERT INTO entities(id,kind,title,revision,status)
                SELECT gid,'work',name,1,'active' FROM works_raw ORDER BY gid""")
            db.execute("""INSERT INTO works(id,kind,workType)
                SELECT w.gid,'work',CASE lower(coalesce(t.name,'')) WHEN 'movement' THEN 'movement' WHEN 'part' THEN 'part' WHEN 'medley' THEN 'medley' WHEN 'mashup' THEN 'mashup' ELSE 'composition' END
                FROM works_raw w LEFT JOIN work_types_raw t ON t.id=w.type ORDER BY w.gid""")
            db.execute("""INSERT INTO entities(id,kind,title,revision,status)
                SELECT a.gid,'artist',a.name,1,'active' FROM artists_raw a JOIN selected_artists s ON s.id=a.id ORDER BY a.gid""")
            db.execute("""INSERT INTO artists(id,kind,artistType,sortName,birth,birthPrecision,death,deathPrecision)
                SELECT a.gid,'artist',CASE lower(coalesce(t.name,'')) WHEN 'person' THEN 'person' WHEN 'group' THEN 'group' ELSE 'other' END,
                    coalesce(nullif(a.sort_name,''),a.name),birth.value,birth.precision,death.value,death.precision
                FROM artists_raw a JOIN selected_artists s ON s.id=a.id LEFT JOIN artist_types_raw t ON t.id=a.type
                LEFT JOIN (SELECT id, printf('%04d',cast(birth_y as integer)) || CASE WHEN birth_m IS NULL OR cast(birth_m as integer)=0 THEN '' ELSE printf('-%02d',cast(birth_m as integer)) || CASE WHEN birth_d IS NULL OR cast(birth_d as integer)=0 THEN '' ELSE printf('-%02d',cast(birth_d as integer)) END END AS value,
                    CASE WHEN birth_d IS NOT NULL AND cast(birth_d as integer)<>0 THEN 'day' WHEN birth_m IS NOT NULL AND cast(birth_m as integer)<>0 THEN 'month' ELSE 'year' END AS precision FROM artists_raw WHERE birth_y IS NOT NULL AND cast(birth_y as integer)<>0) birth ON birth.id=a.id
                LEFT JOIN (SELECT id, printf('%04d',cast(death_y as integer)) || CASE WHEN death_m IS NULL OR cast(death_m as integer)=0 THEN '' ELSE printf('-%02d',cast(death_m as integer)) || CASE WHEN death_d IS NULL OR cast(death_d as integer)=0 THEN '' ELSE printf('-%02d',cast(death_d as integer)) END END AS value,
                    CASE WHEN death_d IS NOT NULL AND cast(death_d as integer)<>0 THEN 'day' WHEN death_m IS NOT NULL AND cast(death_m as integer)<>0 THEN 'month' ELSE 'year' END AS precision FROM artists_raw WHERE death_y IS NOT NULL AND cast(death_y as integer)<>0) death ON death.id=a.id
                ORDER BY a.gid""")
            db.execute("""INSERT INTO external_identities(provider,providerKind,externalId,entityId,evidence,reviewState,revision,sourceId)
                SELECT 'musicbrainz','work',gid,gid,?,'reviewed',1,? FROM works_raw ORDER BY gid""", (f"Core snapshot {SNAPSHOT_ID}; SHA-256 {expected_sha256}; CC0", source_id))
            db.execute("""INSERT INTO external_identities(provider,providerKind,externalId,entityId,evidence,reviewState,revision,sourceId)
                SELECT 'musicbrainz','composer',a.gid,a.gid,?,'reviewed',1,? FROM artists_raw a JOIN selected_artists s ON s.id=a.id ORDER BY a.gid""", (f"Core snapshot {SNAPSHOT_ID}; SHA-256 {expected_sha256}; CC0", source_id))

            db.create_function("normalize_alias", 1, _normal)
            db.execute("INSERT INTO aliases(entityId,language,value,normalized,aliasType) SELECT gid,'und',name,normalize_alias(name),'name' FROM works_raw")
            db.execute("INSERT OR IGNORE INTO aliases(entityId,language,value,normalized,aliasType) SELECT a.gid,'und',a.name,normalize_alias(a.name),'name' FROM artists_raw a JOIN selected_artists s ON s.id=a.id")
            db.execute("""INSERT OR IGNORE INTO aliases(entityId,language,value,normalized,aliasType)
                SELECT w.gid,coalesce(nullif(x.locale,''),'und'),x.name,normalize_alias(x.name),coalesce(t.name,'name')
                FROM work_aliases_raw x JOIN works_raw w ON w.id=x.work LEFT JOIN work_alias_types_raw t ON t.id=x.type
                WHERE trim(coalesce(x.name,''))<>''""")
            db.execute("""INSERT OR IGNORE INTO aliases(entityId,language,value,normalized,aliasType)
                SELECT a.gid,coalesce(nullif(x.locale,''),'und'),x.name,normalize_alias(x.name),coalesce(t.name,'name')
                FROM artist_aliases_raw x JOIN artists_raw a ON a.id=x.artist JOIN selected_artists s ON s.id=a.id
                LEFT JOIN artist_alias_types_raw t ON t.id=x.type WHERE trim(coalesce(x.name,''))<>''""")

            db.create_function("stable_assertion", 2, _assertion_id)
            db.execute("""INSERT INTO assertions(id,entityId,field,valueJson,sourceId,rightsId,selected)
                SELECT stable_assertion(id,'title'),id,'title',json_object('value',title,'language','und'),?,?,1 FROM entities""", (source_id, rights_id))
            for entity_id, field, value in db.execute("""SELECT w.gid,'type',t.name FROM works_raw w JOIN work_types_raw t ON t.id=w.type WHERE trim(coalesce(t.name,''))<>''
                UNION ALL SELECT w.gid,'disambiguation',w.comment FROM works_raw w WHERE trim(coalesce(w.comment,''))<>''
                UNION ALL SELECT a.gid,'type',t.name FROM artists_raw a JOIN selected_artists s ON s.id=a.id JOIN artist_types_raw t ON t.id=a.type WHERE trim(coalesce(t.name,''))<>''
                UNION ALL SELECT a.gid,'sort-name',a.sort_name FROM artists_raw a JOIN selected_artists s ON s.id=a.id WHERE trim(coalesce(a.sort_name,''))<>''
                UNION ALL SELECT a.gid,'disambiguation',a.comment FROM artists_raw a JOIN selected_artists s ON s.id=a.id WHERE trim(coalesce(a.comment,''))<>''"""):
                db.execute("INSERT INTO assertions VALUES(?,?,?,?,?,?,1)", (_assertion_id(entity_id, field), entity_id, field,
                    json.dumps({"value": value, "language": "und"}, ensure_ascii=False, separators=(",", ":")), source_id, rights_id))
            for _, entity_id, field, value in db.execute("""SELECT id,gid,'birth',printf('%04d',cast(birth_y as integer)) FROM artists_raw WHERE birth_y IS NOT NULL AND cast(birth_y as integer)<>0 AND EXISTS(SELECT 1 FROM selected_artists s WHERE s.id=artists_raw.id)
                UNION ALL SELECT id,gid,'death',printf('%04d',cast(death_y as integer)) FROM artists_raw WHERE death_y IS NOT NULL AND cast(death_y as integer)<>0 AND EXISTS(SELECT 1 FROM selected_artists s WHERE s.id=artists_raw.id)"""):
                db.execute("INSERT INTO assertions VALUES(?,?,?,?,?,?,1)", (_assertion_id(entity_id, field), entity_id, field,
                    json.dumps({"value": value, "language": "und"}, separators=(",", ":")), source_id, rights_id))

            db.create_function("stable_edge", 1, lambda row_id: str(uuid.uuid5(NAMESPACE, f"composer-edge:{SNAPSHOT_ID}:{row_id}")))
            db.execute("""INSERT INTO relationships(id,type,fromId,fromKind,toId,toKind,position,medium,role,creditedAs,sourceId)
                SELECT stable_edge(r.id),'credit',w.gid,'work',a.gid,'artist',0,1,'composer',a.name,?
                FROM artist_work_raw r JOIN links_raw l ON l.id=r.link AND l.link_type=?
                JOIN works_raw w ON w.id=r.entity1 JOIN artists_raw a ON a.id=r.entity0 ORDER BY r.id""", (source_id, composer_type[0]))
            piano_title_candidates = counts["title_contains_piano_candidates"]
            direct_status = "present" if "l_instrument_work" in table_counts else "not_in_core_snapshot"
            states = {
                "musicbrainz_snapshot": SNAPSHOT_ID, "musicbrainz_archive_sha256": expected_sha256,
                "musicbrainz_works": counts["works"], "musicbrainz_composers": counts["composers"],
                "musicbrainz_composer_relationships": counts["composer_relationships"],
                "musicbrainz_works_linked_to_composers": counts["works_linked_to_composers"],
                "musicbrainz_title_piano_candidates": piano_title_candidates,
                "musicbrainz_direct_piano_link_status": direct_status,
                "musicbrainz_direct_piano_work_links": counts["direct_piano_instrument_links"],
                "musicbrainz_scope": "all Core works; composer-linked artists; their Core aliases; Composer Artist-Work relationships",
            }
            db.executemany("INSERT INTO catalogue_state(`key`,value) VALUES(?,?)", [(key, str(value)) for key, value in states.items()])

        print(f"phase=populate complete elapsed={time.monotonic() - started:.0f}s", flush=True)
        print("phase=validate integrity/foreign-keys/Room-hash starting", flush=True)
        _create_indices_and_fts(db, schema)
        identity_hash = schema["identityHash"]
        if db.execute("SELECT identity_hash FROM room_master_table WHERE id=42").fetchone()[0] != identity_hash:
            raise ValueError("Room identity hash mismatch")
        if db.execute("PRAGMA foreign_key_check").fetchone() is not None:
            raise ValueError("generated catalogue failed foreign-key validation")
        if db.execute("PRAGMA integrity_check").fetchone()[0] != "ok":
            raise ValueError("generated catalogue failed integrity validation")
        db.execute("BEGIN IMMEDIATE")
        for table in (*TABLES.values(), "core_load_state"):
            db.execute(f"DROP TABLE {table}")
        db.commit()
        db.execute("DROP TABLE selected_artists")
        db.commit()
        db.execute("VACUUM")
        if db.execute("PRAGMA integrity_check").fetchone()[0] != "ok":
            raise ValueError("vacuumed catalogue failed integrity validation")
        print(f"phase=database complete bytes={db_path.stat().st_size:,} elapsed={time.monotonic() - started:.0f}s", flush=True)
    finally:
        db.close()

    database_bytes = db_path.stat().st_size
    file_sha = _hash(db_path)
    revision = int(SNAPSHOT_ID.replace("-", ""))
    snapshot_created_ms = int(datetime.datetime(2026, 11, 14, 0, 22, 30, tzinfo=datetime.timezone.utc).timestamp() * 1000)
    validated_ms = int(datetime.datetime.now(datetime.timezone.utc).timestamp() * 1000)
    manifest = {
        "formatVersion": 1, "bundleId": str(uuid.uuid5(NAMESPACE, dataset_id)), "type": "COLLECTION",
        "entityId": dataset_id, "schemaVersion": 1, "revision": revision, "language": "und",
        "createdAt": snapshot_created_ms, "validatedAt": validated_ms,
        "dependencies": [], "expectedBytes": database_bytes,
        "files": [{"sha256": file_sha, "bytes": database_bytes, "mime": "application/vnd.sqlite3",
                    "relativePath": "catalogue.db", "language": None,
                    "rights": {"license": "CC0-1.0", "licenseUrl": LICENSE_URL,
                               "attribution": ATTRIBUTION, "sourceName": "MusicBrainz", "sourceUrl": DOWNLOAD_URL}}],
        "providerKind": "catalogue", "providerRevision": SNAPSHOT_ID, "attribution": ATTRIBUTION,
        "minReaderVersion": 1, "state": "READY",
        "progress": {"completedBytes": database_bytes, "totalBytes": database_bytes, "completedFiles": 1},
        "lastError": None, "updatedAt": validated_ms,
    }
    (output / "manifest.json").write_text(json.dumps(manifest, ensure_ascii=False, sort_keys=True, separators=(",", ":")) + "\n", encoding="utf-8")
    print(f"phase=manifest complete sha256={file_sha} elapsed={time.monotonic() - started:.0f}s", flush=True)
    counts.update({"database_bytes": database_bytes, "database_sha256": file_sha, "table_members": table_counts})
    return counts


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("archive", type=Path)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--schema", type=Path, default=Path("app/schemas/com.opus.piano.data.catalogue.CatalogueDatabase/1.json"))
    args = parser.parse_args()
    print(json.dumps(build_catalogue(args.archive, args.output, schema_path=args.schema), indent=2, ensure_ascii=False))
