import hashlib
import importlib.util
import json
import sqlite3
import tempfile
import unittest
from pathlib import Path


MODULE = Path(__file__).with_name("catalogue_generator.py")
SPEC = importlib.util.spec_from_file_location("catalogue_generator", MODULE)
generator = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(generator)
ROOT = Path(__file__).resolve().parents[1]
SCHEMA = ROOT / "app/schemas/com.opus.piano.data.catalogue.CatalogueDatabase/1.json"


class CatalogueGeneratorTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name)
        source = {
            "formatVersion": 1,
            "datasetId": "f711591a-13b7-4abd-a3a2-12658da47eb0",
            "revision": 1,
            "records": [{
                "id": "67d738c4-bf1f-4a75-a31a-df0b5476725b",
                "kind": "artist",
                "title": "Synthetic test composer",
                "artistType": "person",
                "sortName": "Synthetic test composer",
                "source": {
                    "provider": "fixture",
                    "providerKind": "artist",
                    "externalId": "synthetic-artist-1",
                    "payloadSha256": "1" * 64,
                },
                "aliases": [{"language": "en", "value": "Synthetický alias"}],
            }],
        }
        self.input_path = self.root / "source.json"
        self.input_path.write_text(json.dumps(source, ensure_ascii=False, separators=(",", ":")), encoding="utf-8")
        policy = {
            "policyVersion": 1,
            "syntheticTestFixture": True,
            "inputSha256": hashlib.sha256(self.input_path.read_bytes()).hexdigest(),
            "approvedLicenses": ["CC0-1.0"],
            "generatedAt": 1794614400000,
            "packageFileRights": {
                "license": "CC0-1.0",
                "licenseUrl": "https://creativecommons.org/publicdomain/zero/1.0/",
                "attribution": "Synthetic test fixture; not real catalogue data",
                "sourceName": "Synthetic test fixture",
                "sourceUrl": "https://fixture.invalid/source-set",
            },
            "records": [{
                "provider": "fixture",
                "providerKind": "artist",
                "externalId": "synthetic-artist-1",
                "payloadSha256": "1" * 64,
                "sourceName": "Synthetic test fixture",
                "sourceUrl": "https://fixture.invalid/record/1",
                "revision": "synthetic-r1",
                "retrievedAt": "2026-11-14T00:00:00Z",
                "license": "CC0-1.0",
                "licenseUrl": "https://creativecommons.org/publicdomain/zero/1.0/",
                "attribution": "Synthetic test fixture; no real-world claims",
                "reviewState": "approved",
            }],
        }
        self.policy_path = self.root / "policy.json"
        self.write_policy(policy)

    def tearDown(self):
        self.temp.cleanup()

    def write_policy(self, policy):
        self.policy_path.write_text(json.dumps(policy, ensure_ascii=False, separators=(",", ":")), encoding="utf-8")

    def test_generation_is_deterministic_room_schema_one_and_bundle_manifest_v1(self):
        first, second = self.root / "first", self.root / "second"
        generator.generate(self.input_path, self.policy_path, SCHEMA, first, allow_synthetic=True)
        generator.generate(self.input_path, self.policy_path, SCHEMA, second, allow_synthetic=True)
        database = first / "catalogue.db"
        self.assertEqual(database.read_bytes(), (second / "catalogue.db").read_bytes())
        self.assertEqual((first / "manifest.json").read_bytes(), (second / "manifest.json").read_bytes())
        with sqlite3.connect(database) as db:
            self.assertEqual(1, db.execute("PRAGMA user_version").fetchone()[0])
            self.assertEqual("ok", db.execute("PRAGMA integrity_check").fetchone()[0])
            self.assertEqual("Synthetic test composer", db.execute("SELECT title FROM entities").fetchone()[0])
            self.assertEqual("67d738c4-bf1f-4a75-a31a-df0b5476725b", db.execute("SELECT id FROM entities").fetchone()[0])
            self.assertEqual(1, db.execute("SELECT COUNT(*) FROM sources").fetchone()[0])
            self.assertEqual("synthetic-artist-1", db.execute("SELECT recordId FROM sources").fetchone()[0])
            self.assertEqual("approved", db.execute("SELECT reviewState FROM rights").fetchone()[0])
            self.assertEqual("syntheticky alias", db.execute("SELECT normalized FROM aliases").fetchone()[0])
            self.assertEqual(1, db.execute("SELECT COUNT(*) FROM alias_fts WHERE alias_fts MATCH 'syntheticky'").fetchone()[0])
        manifest = json.loads((first / "manifest.json").read_text(encoding="utf-8"))
        self.assertEqual(1, manifest["formatVersion"])
        self.assertEqual("COLLECTION", manifest["type"])
        self.assertEqual(1, manifest["schemaVersion"])
        self.assertEqual(hashlib.sha256(database.read_bytes()).hexdigest(), manifest["files"][0]["sha256"])
        self.assertEqual("CC0-1.0", manifest["files"][0]["rights"]["license"])

    def test_rejects_source_without_exact_reviewed_record_provenance(self):
        policy = json.loads(self.policy_path.read_text(encoding="utf-8"))
        policy["records"][0]["attribution"] = ""
        self.write_policy(policy)
        with self.assertRaises(ValueError):
            generator.generate(self.input_path, self.policy_path, SCHEMA, self.root / "out", allow_synthetic=True)

    def test_rejects_license_outside_explicit_policy_allowlist(self):
        policy = json.loads(self.policy_path.read_text(encoding="utf-8"))
        policy["approvedLicenses"] = []
        self.write_policy(policy)
        with self.assertRaises(ValueError):
            generator.generate(self.input_path, self.policy_path, SCHEMA, self.root / "out", allow_synthetic=True)

    def test_rejects_input_changed_after_review_pin(self):
        with self.input_path.open("a", encoding="utf-8") as stream:
            stream.write(" ")
        with self.assertRaises(ValueError):
            generator.generate(self.input_path, self.policy_path, SCHEMA, self.root / "out", allow_synthetic=True)

    def test_production_generation_rejects_synthetic_fixture_policy(self):
        with self.assertRaises(ValueError):
            generator.generate(self.input_path, self.policy_path, SCHEMA, self.root / "out")

    def test_production_generation_rejects_fixture_records_without_fixture_marker(self):
        policy = json.loads(self.policy_path.read_text(encoding="utf-8"))
        policy.pop("syntheticTestFixture")
        self.write_policy(policy)
        with self.assertRaisesRegex(ValueError, "MusicBrainz Core"):
            generator.generate(self.input_path, self.policy_path, SCHEMA, self.root / "out")

    def test_production_generation_rejects_non_musicbrainz_source_even_with_approved_rights(self):
        source = json.loads(self.input_path.read_text(encoding="utf-8"))
        policy = json.loads(self.policy_path.read_text(encoding="utf-8"))
        policy.pop("syntheticTestFixture")
        source["records"][0]["source"]["provider"] = "openopus"
        policy["records"][0]["provider"] = "openopus"
        self.input_path.write_text(json.dumps(source, separators=(",", ":")), encoding="utf-8")
        policy["inputSha256"] = hashlib.sha256(self.input_path.read_bytes()).hexdigest()
        self.write_policy(policy)
        with self.assertRaisesRegex(ValueError, "MusicBrainz Core"):
            generator.generate(self.input_path, self.policy_path, SCHEMA, self.root / "out")

    def test_rejects_supplementary_or_unapproved_metadata_at_every_input_level(self):
        original_source = json.loads(self.input_path.read_text(encoding="utf-8"))
        original_policy = json.loads(self.policy_path.read_text(encoding="utf-8"))
        cases = (
            ("dataset", lambda source, policy: source.update({"genres": ["classical"]})),
            ("record", lambda source, policy: source["records"][0].update({"ratings": [5]})),
            ("source", lambda source, policy: source["records"][0]["source"].update({"popularity": 99})),
            ("alias", lambda source, policy: source["records"][0]["aliases"][0].update({"tags": ["piano"]})),
            ("policy", lambda source, policy: policy["records"][0].update({"annotation": "unreviewed"})),
            ("file rights", lambda source, policy: policy["packageFileRights"].update({"ratings": []})),
        )
        for name, add_unapproved_field in cases:
            with self.subTest(name=name):
                source = json.loads(json.dumps(original_source))
                policy = json.loads(json.dumps(original_policy))
                add_unapproved_field(source, policy)
                self.input_path.write_text(json.dumps(source, ensure_ascii=False, separators=(",", ":")), encoding="utf-8")
                policy["inputSha256"] = hashlib.sha256(self.input_path.read_bytes()).hexdigest()
                self.write_policy(policy)
                with self.assertRaises(ValueError):
                    generator.generate(self.input_path, self.policy_path, SCHEMA, self.root / f"out-{name}", allow_synthetic=True)


if __name__ == "__main__":
    unittest.main()
