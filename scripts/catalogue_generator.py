#!/usr/bin/env python3
"""Build schema-1 catalogue bundles from hash-pinned source and review policy."""
import argparse
import hashlib
import json
import re
import sqlite3
import unicodedata
import uuid
from pathlib import Path

LICENSES = {"CC0-1.0", "CC-BY-4.0", "CC-BY-SA-4.0", "CC-BY-3.0", "CC-BY-SA-3.0", "Public-domain-verified"}
KINDS = {"work", "arrangement", "artist", "album", "release", "recording", "collection"}
NAMESPACE = uuid.UUID("d4e6c45a-7528-5f53-9c75-ff7ee7ea8122")


def sha256(data):
    return hashlib.sha256(data).hexdigest()


def _https(value):
    return isinstance(value, str) and value.startswith("https://") and len(value) > 8


def _normalized(value):
    value = "".join(c for c in unicodedata.normalize("NFD", value) if not unicodedata.category(c).startswith("M")).lower()
    value = "".join(c if c.isalpha() or c.isdigit() else " " for c in value)
    return re.sub(r"([a-z])([0-9])", r"\1 \2", value).strip()


def _approved(rights, approved):
    if (not isinstance(rights, dict) or rights.get("reviewState") != "approved"
            or rights.get("license") not in LICENSES or rights.get("license") not in approved
            or not _https(rights.get("licenseUrl")) or not rights.get("attribution", "").strip()):
        raise ValueError("missing, incompatible, or unapproved rights provenance")


def _keys(value, required, allowed, label):
    if not isinstance(value, dict) or not required <= value.keys() or not value.keys() <= allowed:
        raise ValueError(f"{label} contains missing or unapproved fields")


def generate(source_path, policy_path, schema_path, output, allow_synthetic=False):
    source_bytes = Path(source_path).read_bytes()
    source = json.loads(source_bytes)
    policy = json.loads(Path(policy_path).read_text(encoding="utf-8"))
    schema = json.loads(Path(schema_path).read_text(encoding="utf-8"))["database"]
    _keys(source, {"formatVersion", "datasetId", "revision", "records"},
          {"formatVersion", "datasetId", "revision", "records"}, "source dataset")
    _keys(policy, {"policyVersion", "inputSha256", "approvedLicenses", "generatedAt", "packageFileRights", "records"},
          {"policyVersion", "syntheticTestFixture", "inputSha256", "approvedLicenses", "generatedAt", "packageFileRights", "records"}, "review policy")
    if source.get("formatVersion") != 1 or schema.get("version") != 1 or policy.get("policyVersion") != 1:
        raise ValueError("only source, policy, and Room schema version 1 are supported")
    if sha256(source_bytes) != policy.get("inputSha256"):
        raise ValueError("source input differs from reviewed SHA-256")
    if policy.get("syntheticTestFixture") and not allow_synthetic:
        raise ValueError("synthetic fixture is not valid production input")
    if not isinstance(policy.get("syntheticTestFixture", False), bool):
        raise ValueError("synthetic fixture marker must be boolean")
    if str(uuid.UUID(source["datasetId"])) != source["datasetId"] or not isinstance(source.get("revision"), int) or source["revision"] < 1:
        raise ValueError("dataset identity or revision is invalid")
    approved = set(policy.get("approvedLicenses", []))
    package_rights = policy.get("packageFileRights", {})
    _keys(package_rights, {"license", "licenseUrl", "attribution", "sourceName", "sourceUrl"},
          {"license", "licenseUrl", "attribution", "sourceName", "sourceUrl"}, "package file rights")
    _approved({**package_rights, "reviewState": "approved"}, approved)
    if not package_rights.get("sourceName", "").strip() or not _https(package_rights.get("sourceUrl")):
        raise ValueError("package file source provenance is incomplete")
    records = source.get("records")
    reviewed = policy.get("records")
    if not isinstance(records, list) or not records or not isinstance(reviewed, list):
        raise ValueError("source and reviewed record lists are required")
    if any(not isinstance(row, dict) for row in records + reviewed):
        raise ValueError("source and reviewed records must be objects")
    review_map = {}
    for item in reviewed:
        _keys(item, {"provider", "providerKind", "externalId", "payloadSha256", "sourceName", "sourceUrl", "revision", "retrievedAt", "license", "licenseUrl", "attribution", "reviewState"},
              {"provider", "providerKind", "externalId", "payloadSha256", "sourceName", "sourceUrl", "revision", "retrievedAt", "license", "licenseUrl", "attribution", "reviewState"}, "reviewed source record")
        key = (item.get("provider"), item.get("providerKind"), item.get("externalId"), item.get("payloadSha256"))
        if key in review_map or any(not isinstance(part, str) or not part.strip() for part in key[:3]):
            raise ValueError("duplicate reviewed source identity")
        _approved(item, approved)
        if (item.get("reviewState") != "approved" or not item.get("sourceName", "").strip()
                or not _https(item.get("sourceUrl")) or not item.get("revision", "").strip()
                or not item.get("retrievedAt", "").strip() or not re.fullmatch(r"[0-9a-f]{64}", key[3] or "")):
            raise ValueError("record source provenance is incomplete")
        review_map[key] = item
    output = Path(output)
    output.mkdir(parents=True, exist_ok=True)
    db_path = output / "catalogue.db"
    db = sqlite3.connect(db_path)
    try:
        db.execute("PRAGMA foreign_keys=ON")
        for query in schema["setupQueries"]:
            db.execute(query)
        for entity in schema["entities"]:
            table = entity["tableName"]
            db.execute(entity["createSql"].replace("${TABLE_NAME}", table))
        for entity in schema["entities"]:
            for index in entity.get("indices", []):
                db.execute(index["createSql"].replace("${TABLE_NAME}", entity["tableName"]))
        db.executescript("""
        CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_alias_fts_BEFORE_UPDATE BEFORE UPDATE ON aliases BEGIN DELETE FROM alias_fts WHERE docid=OLD.rowid; END;
        CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_alias_fts_BEFORE_DELETE BEFORE DELETE ON aliases BEGIN DELETE FROM alias_fts WHERE docid=OLD.rowid; END;
        CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_alias_fts_AFTER_UPDATE AFTER UPDATE ON aliases BEGIN INSERT INTO alias_fts(docid,normalized) VALUES(NEW.rowid,NEW.normalized); END;
        CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_alias_fts_AFTER_INSERT AFTER INSERT ON aliases BEGIN INSERT INTO alias_fts(docid,normalized) VALUES(NEW.rowid,NEW.normalized); END;
        """)
        db.execute("PRAGMA user_version=1")
        source_rows = {}
        rights_rows = {}
        assertions = []
        used_reviews = set()
        for record in sorted(records, key=lambda row: row.get("id", "")):
            kind = record.get("kind") if isinstance(record, dict) else None
            fields = {
                "artist": {"artistType", "sortName", "birth", "birthPrecision", "death", "deathPrecision"},
                "work": {"workType"}, "arrangement": {"instrumentation"}, "album": {"albumType"},
                "release": {"edition", "date", "datePrecision"}, "recording": {"durationMillis"},
                "collection": {"description"},
            }.get(kind)
            if fields is None:
                raise ValueError("record has unsupported catalogue kind")
            _keys(record, {"id", "kind", "title", "source"},
                  {"id", "kind", "title", "source", "aliases"} | fields, "catalogue record")
            entity_id = str(uuid.UUID(record["id"]))
            title = record.get("title", "").strip()
            if kind not in KINDS or not title or str(uuid.UUID(entity_id)) != entity_id:
                raise ValueError("record has invalid stable UUID, kind, or title")
            ref = record.get("source", {})
            _keys(ref, {"provider", "providerKind", "externalId", "payloadSha256"},
                  {"provider", "providerKind", "externalId", "payloadSha256"}, "source reference")
            if not allow_synthetic and ref.get("provider") != "musicbrainz":
                raise ValueError("production input must contain MusicBrainz Core records only")
            key = (ref.get("provider"), ref.get("providerKind"), ref.get("externalId"), ref.get("payloadSha256"))
            provenance = review_map.get(key)
            if provenance is None:
                raise ValueError("record lacks exact reviewed source and payload provenance")
            used_reviews.add(key)
            source_id = str(uuid.uuid5(NAMESPACE, "source:" + "|".join(key)))
            rights_id = str(uuid.uuid5(NAMESPACE, "rights:" + "|".join(key)))
            source_rows[source_id] = (source_id, key[0], key[2], provenance["revision"], key[3], provenance["retrievedAt"], provenance["sourceUrl"])
            rights_rows[rights_id] = (rights_id, provenance["license"], provenance["licenseUrl"], provenance["attribution"], "approved", "")
            db.execute("INSERT INTO entities(id,kind,title,revision,status) VALUES(?,?,?,1,'active')", (entity_id, kind, title))
            subtype = {
                "artist": ("artists", (entity_id, "artist", record.get("artistType", "person"), record.get("sortName", title), record.get("birth"), record.get("birthPrecision"), record.get("death"), record.get("deathPrecision"))),
                "work": ("works", (entity_id, "work", record.get("workType", "composition"))),
                "arrangement": ("arrangements", (entity_id, "arrangement", record.get("instrumentation", ""))),
                "album": ("albums", (entity_id, "album", record.get("albumType", "unknown"))),
                "release": ("releases", (entity_id, "release", record.get("edition", ""), record.get("date"), record.get("datePrecision"))),
                "recording": ("recordings", (entity_id, "recording", record.get("durationMillis"))),
                "collection": ("collections", (entity_id, "collection", record.get("description", ""))),
            }[kind]
            columns = [field["columnName"] for field in next(x for x in schema["entities"] if x["tableName"] == subtype[0])["fields"]]
            db.execute(f"INSERT INTO {subtype[0]}({','.join(columns)}) VALUES({','.join('?' for _ in columns)})", subtype[1])
            assertions.append((str(uuid.uuid5(NAMESPACE, f"assertion:{entity_id}:title")), entity_id, "title", json.dumps(title, ensure_ascii=False), source_id, rights_id, 1))
            aliases = record.get("aliases", [])
            if not isinstance(aliases, list) or any(not isinstance(alias, dict) for alias in aliases):
                raise ValueError("aliases must be a list of objects")
            for alias in sorted(aliases, key=lambda a: (a.get("language", ""), a.get("value", ""))):
                _keys(alias, {"language", "value"}, {"language", "value", "aliasType"}, "alias")
                value = alias.get("value", "").strip()
                if not value:
                    raise ValueError("empty alias")
                db.execute("INSERT INTO aliases(entityId,language,value,normalized,aliasType) VALUES(?,?,?,?,?)",
                           (entity_id, alias["language"], value, _normalized(value), alias.get("aliasType", "name")))
        if used_reviews != set(review_map):
            raise ValueError("review policy includes records outside the pinned source input")
        db.executemany("INSERT INTO sources VALUES(?,?,?,?,?,?,?)", [source_rows[k] for k in sorted(source_rows)])
        db.executemany("INSERT INTO rights VALUES(?,?,?,?,?,?)", [rights_rows[k] for k in sorted(rights_rows)])
        db.executemany("INSERT INTO assertions(id,entityId,field,valueJson,sourceId,rightsId,selected) VALUES(?,?,?,?,?,?,?)", assertions)
        db.commit()
        check = db.execute("PRAGMA integrity_check").fetchone()[0]
        if check != "ok" or db.execute("PRAGMA foreign_key_check").fetchone() is not None:
            raise ValueError("generated catalogue failed SQLite integrity validation")
        identity = schema["identityHash"]
        if db.execute("SELECT identity_hash FROM room_master_table WHERE id=42").fetchone()[0] != identity:
            raise ValueError("Room identity hash mismatch")
    finally:
        db.close()
    db_bytes = db_path.read_bytes()
    file_rights = package_rights
    manifest = {
        "formatVersion": 1,
        "bundleId": str(uuid.uuid5(NAMESPACE, source["datasetId"])),
        "type": "COLLECTION", "entityId": source["datasetId"], "schemaVersion": 1,
        "revision": source["revision"], "language": "und", "createdAt": policy["generatedAt"],
        "validatedAt": policy["generatedAt"], "dependencies": [], "expectedBytes": len(db_bytes),
        "files": [{"sha256": sha256(db_bytes), "bytes": len(db_bytes), "mime": "application/vnd.sqlite3",
                   "relativePath": "catalogue.db", "language": None,
                   "rights": {"license": file_rights["license"], "licenseUrl": file_rights["licenseUrl"],
                              "attribution": file_rights["attribution"], "sourceName": file_rights["sourceName"], "sourceUrl": file_rights["sourceUrl"]}}],
        "providerKind": "catalogue", "providerRevision": str(source["revision"]),
        "attribution": file_rights["attribution"], "minReaderVersion": 1,
        "state": "READY", "progress": {"completedBytes": len(db_bytes), "totalBytes": len(db_bytes), "completedFiles": 1},
        "lastError": None, "updatedAt": policy["generatedAt"]
    }
    (output / "manifest.json").write_text(json.dumps(manifest, ensure_ascii=False, sort_keys=True, separators=(",", ":")) + "\n", encoding="utf-8")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("source", type=Path); parser.add_argument("policy", type=Path)
    parser.add_argument("--schema", type=Path, default=Path("app/schemas/com.opus.piano.data.catalogue.CatalogueDatabase/1.json"))
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--allow-synthetic-test-fixture", action="store_true")
    args = parser.parse_args()
    generate(args.source, args.policy, args.schema, args.output, args.allow_synthetic_test_fixture)
