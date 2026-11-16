import unittest

from check_publication_workflows import violations


class PublicationWorkflowPolicyTest(unittest.TestCase):
    def test_read_only_quality_workflow_is_allowed(self):
        self.assertEqual([], violations("permissions:\n  contents: read\non:\n  pull_request:\njobs: {}"))

    def test_write_permissions_are_rejected(self):
        self.assertTrue(violations("permissions:\n  contents: write"))

    def test_pr_secret_access_is_rejected(self):
        self.assertTrue(violations("on:\n  pull_request:\njobs:\n  x:\n    env:\n      TOKEN: ${{ secrets.TOKEN }}"))

    def test_pull_request_target_is_rejected(self):
        self.assertTrue(violations("permissions:\n  contents: read\non:\n  pull_request_target:\njobs: {}"))

    def test_write_all_is_rejected(self):
        self.assertTrue(violations("permissions: write-all\non:\n  push:\njobs: {}"))

    def test_checkout_credentials_must_not_persist(self):
        workflow = "permissions:\n  contents: read\njobs:\n  x:\n    steps:\n      - uses: actions/checkout@abc123\n      - name: run"
        self.assertTrue(violations(workflow))

    def test_checkout_without_persisted_credentials_is_allowed(self):
        workflow = "permissions:\n  contents: read\njobs:\n  x:\n    steps:\n      - uses: actions/checkout@abc123\n        with:\n          persist-credentials: false\n      - name: run"
        self.assertEqual([], violations(workflow))

    def test_release_command_is_rejected(self):
        self.assertTrue(violations("steps:\n  - run: gh release create v1"))

    def test_release_action_is_rejected(self):
        self.assertTrue(violations("steps:\n  - uses: softprops/action-gh-release@v2"))


if __name__ == "__main__":
    unittest.main()
