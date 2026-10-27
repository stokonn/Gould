"""Safety regressions for the remote-deletion guard; no network or mutations."""
import unittest
from finalize_github import candidate_errors


class DeletionGuardTest(unittest.TestCase):
    def errors(self, **patch):
        data = dict(branch="old", expected="a", actual="a", protected=False,
                    unique=0, prs=[], releases=[], used=set())
        data.update(patch)
        return candidate_errors(**data)

    def test_only_unchanged_merged_unreferenced_branch_is_eligible(self):
        self.assertEqual([], self.errors())

    def test_changed_or_unresolved_head_is_retained(self):
        self.assertIn("head changed", self.errors(actual="b"))
        self.assertIn("unique or unresolved commits", self.errors(unique=1))
        self.assertIn("unique or unresolved commits", self.errors(unique=None))

    def test_protected_and_release_branches_are_retained(self):
        self.assertTrue(self.errors(branch="main"))
        self.assertTrue(self.errors(branch="release/version"))
        self.assertIn("branch protected", self.errors(protected=True))

    def test_pull_request_head_and_base_dependencies_are_retained(self):
        for head, base in [("old", "main"), ("proposal", "old")]:
            self.assertIn("open pull request", self.errors(prs=[{"head": {"ref": head}, "base": {"ref": base}}]))

    def test_worktree_and_release_dependencies_are_retained(self):
        self.assertIn("registered worktree", self.errors(used={"old"}))
        self.assertIn("release dependency", self.errors(releases=[{"target_commitish": "old"}]))


if __name__ == "__main__":
    unittest.main()
