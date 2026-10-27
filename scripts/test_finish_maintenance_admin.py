"""Actions cleanup classification must retain required/ambiguous security coverage."""
import unittest
import tempfile
from pathlib import Path
import hashlib
from finish_maintenance_admin import classify_workflow, write_backup_manifest


class WorkflowAuditTest(unittest.TestCase):
    def test_legacy_name_with_required_definition_is_kept(self):
        row = {"path": ".github/workflows/quality.yml", "name": "Goldberg checks"}
        self.assertEqual("KEEP", classify_workflow(row, {row["path"]}, set()))

    def test_definition_on_maintenance_is_kept(self):
        row = {"path": ".github/workflows/still-needed.yml"}
        self.assertEqual("KEEP", classify_workflow(row, set(), {row["path"]}))

    def test_absent_canonical_yaml_is_an_obsolete_registration(self):
        row = {"path": ".github/workflows/stage5-screenshots.yml"}
        self.assertEqual("OBSOLETE_REGISTRATION", classify_workflow(row, set(), set()))

    def test_managed_security_workflow_needs_review(self):
        row = {"path": "dynamic/github-code-scanning/codeql"}
        self.assertTrue(classify_workflow(row, set(), set()).startswith("BLOCKED"))

    def test_release_checksum_asset_is_never_overwritten_by_backup_manifest(self):
        with tempfile.TemporaryDirectory() as directory:
            backup = Path(directory)
            (backup / "assets").mkdir()
            asset = backup / "assets/SHA256SUMS"
            original = b"original published checksum asset\n"
            asset.write_bytes(original)
            result = write_backup_manifest(backup)
            self.assertEqual(original, asset.read_bytes())
            self.assertEqual(hashlib.sha256(original).hexdigest(), result["assets/SHA256SUMS"])
            self.assertTrue((backup / "BACKUP-SHA256SUMS.txt").is_file())


if __name__ == "__main__":
    unittest.main()
