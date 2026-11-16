#!/usr/bin/env python3
import hashlib
import io
import sqlite3
import tarfile
import tempfile
import unittest
from pathlib import Path
import json

import generate_musicbrainz_catalogue as generator
import package_musicbrainz_catalogue as packager
from generate_musicbrainz_catalogue import build_catalogue


def member(tar, name, text):
    data = text.encode("utf-8")
    info = tarfile.TarInfo(name)
    info.size = len(data)
    tar.addfile(info, io.BytesIO(data))


class MusicBrainzCatalogueTest(unittest.TestCase):
    def test_gzip_asset_round_trips_and_refuses_overwrite(self):
        with tempfile.TemporaryDirectory() as temp:
            source = Path(temp) / "catalogue.db"
            target = Path(temp) / "catalogue.db.gz"
            source.write_bytes((b"MusicBrainz Core work and composer metadata\n" * 1000) + bytes(range(256)))
            result = packager.package(source, target)
            self.assertEqual(source.stat().st_size, result["source_bytes"])
            self.assertEqual(hashlib.sha256(source.read_bytes()).hexdigest(), result["source_sha256"])
            with self.assertRaisesRegex(ValueError, "target must not exist"):
                packager.package(source, target)

    def test_complete_core_checkpoint_reuses_rows_without_reopening_archive(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            archive = root / "mbdump.tar.bz2"
            with tarfile.open(archive, "w:bz2") as tar:
                member(tar, "mbdump/artist", "7\t11111111-1111-4111-8111-111111111111\tAda Composer\tComposer, Ada\t1800\t1\t2\t1860\t3\t4\t1\t\\N\t\\N\t\t\n")
                member(tar, "mbdump/artist_type", "1\tPerson\t\\N\t0\t\t11111111-1111-4111-8111-111111111112\n")
                member(tar, "mbdump/work", "11\t22222222-2222-4222-8222-222222222221\tPiano Study\t1\tAn example\t0\t2026-01-01\n")
                member(tar, "mbdump/work_type", "1\tStudy\t\\N\t0\t\t22222222-2222-4222-8222-222222222223\n")
                member(tar, "mbdump/link_type", f"168\t\\N\t0\t{generator.COMPOSER_RELATIONSHIP_GID}\tartist\twork\tcomposer\t\n")
                member(tar, "mbdump/link", "1000\t168\t\\N\t\\N\t\\N\t\\N\t\\N\t\\N\t0\t2026-01-01\t0\n")
                member(tar, "mbdump/l_artist_work", "1\t1000\t7\t11\t0\t2026-01-01\t0\t\t\n")
            digest = hashlib.sha256(archive.read_bytes()).hexdigest()
            staging = sqlite3.connect(root / "staging.db")
            schema = json.loads(Path("app/schemas/com.opus.piano.data.catalogue.CatalogueDatabase/1.json").read_text())["database"]
            try:
                generator._create_database(staging, schema)
                first = generator._load_core(staging, archive, digest)
                second = generator._load_core(staging, root / "archive-already-verified-and-consumed", digest)
                self.assertEqual(first, second)
                self.assertEqual(staging.execute("SELECT count(*) FROM works_raw").fetchone()[0], 1)
            finally:
                staging.close()

    def test_core_rows_build_searchable_catalogue_with_ids_aliases_and_composer_edge(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            archive = root / "mbdump.tar.bz2"
            with tarfile.open(archive, "w:bz2") as tar:
                member(tar, "mbdump/artist", "7\t11111111-1111-4111-8111-111111111111\tAda Composer\tComposer, Ada\t1800\t1\t2\t1860\t3\t4\t1\t\\N\t\\N\t\t")
                member(tar, "mbdump/artist_type", "1\tPerson\t\\N\t0\t\t11111111-1111-4111-8111-111111111112\n")
                member(tar, "mbdump/artist_alias_type", "1\tArtist name\t\\N\t0\t\t11111111-1111-4111-8111-111111111113\n")
                member(tar, "mbdump/artist_alias", "1\t7\tAda C.\ten\t0\t2026-01-01\t1\tAda C.\n")
                member(tar, "mbdump/work", "11\t22222222-2222-4222-8222-222222222221\tPiano Study\t1\tAn example\t0\t2026-01-01\n12\t22222222-2222-4222-8222-222222222222\tUnlinked Work\t\\N\t\t0\t2026-01-01\n")
                member(tar, "mbdump/work_type", "1\tStudy\t\\N\t0\t\t22222222-2222-4222-8222-222222222223\n")
                member(tar, "mbdump/work_alias_type", "1\tAlternative title\t\\N\t0\t\t22222222-2222-4222-8222-222222222224\n")
                member(tar, "mbdump/work_alias", "1\t11\tEtude for piano\ten\t0\t2026-01-01\t1\tEtude\n")
                member(tar, "mbdump/link_type", f"168\t\\N\t0\t{generator.COMPOSER_RELATIONSHIP_GID}\tartist\twork\tcomposer\t\n")
                member(tar, "mbdump/link", "1000\t168\t\\N\t\\N\t\\N\t\\N\t\\N\t\\N\t0\t2026-01-01\t0\n")
                member(tar, "mbdump/l_artist_work", "1\t1000\t7\t11\t0\t2026-01-01\t0\t\t\n")
                member(tar, "mbdump-derived/work_tag", "must not be read\n")

            digest = hashlib.sha256(archive.read_bytes()).hexdigest()
            result = build_catalogue(archive, root / "bundle", expected_sha256=digest,
                                     expected_counts=None, retrieved_at="2026-11-15T00:56:00+01:00")
            db = sqlite3.connect(root / "bundle" / "catalogue.db")
            try:
                self.assertEqual(result["works"], 2)
                self.assertEqual(result["composers"], 1)
                self.assertEqual(result["composer_relationships"], 1)
                self.assertEqual(result["direct_piano_instrument_links"], 0)
                self.assertEqual(db.execute("SELECT id FROM entities WHERE title='Piano Study'").fetchone()[0], "22222222-2222-4222-8222-222222222221")
                self.assertEqual(db.execute("SELECT value FROM aliases WHERE entityId=? AND language='en'", ("22222222-2222-4222-8222-222222222221",)).fetchone()[0], "Etude for piano")
                self.assertEqual(db.execute("SELECT count(*) FROM relationships WHERE type='credit' AND role='composer' AND fromId=? AND toId=?", ("22222222-2222-4222-8222-222222222221", "11111111-1111-4111-8111-111111111111")).fetchone()[0], 1)
                self.assertEqual(db.execute("SELECT externalId FROM external_identities WHERE providerKind='work'").fetchall(), [("22222222-2222-4222-8222-222222222221",), ("22222222-2222-4222-8222-222222222222",)])
                self.assertEqual(db.execute("SELECT count(*) FROM alias_fts WHERE alias_fts MATCH 'Etude'").fetchone()[0], 1)
                self.assertEqual(db.execute("PRAGMA integrity_check").fetchone()[0], "ok")
                self.assertIsNone(db.execute("SELECT 1 FROM sources WHERE recordId LIKE '%derived%'").fetchone())
            finally:
                db.close()

    def test_wrong_archive_digest_is_rejected_before_output(self):
        with tempfile.TemporaryDirectory() as temp:
            archive = Path(temp) / "mbdump.tar.bz2"
            with tarfile.open(archive, "w:bz2"):
                pass
            output = Path(temp) / "not-built"
            with self.assertRaisesRegex(ValueError, "archive SHA-256 mismatch"):
                build_catalogue(archive, output, expected_sha256="0" * 64,
                                expected_counts=None, retrieved_at="2026-11-15T00:56:00+01:00")
            self.assertFalse(output.exists())


if __name__ == "__main__":
    unittest.main()
