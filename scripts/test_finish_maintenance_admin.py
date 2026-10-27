"""Actions cleanup classification must retain required/ambiguous security coverage."""
import unittest
from finish_maintenance_admin import classify_workflow


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


if __name__ == "__main__":
    unittest.main()
