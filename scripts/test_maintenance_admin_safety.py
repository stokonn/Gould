"""Non-destructive regressions: Git/GitHub effects are mocked, never executed."""
import contextlib
import hashlib
import io
import json
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest
from unittest.mock import Mock, patch

import finalize_github as old
import finish_maintenance_admin as final
from maintenance_admin_safety import (new_receipt_directory, write_receipt,
                                      verified_push_destination, refuse_branch_deletion)

REPO = {"id": old.REPOSITORY_ID, "full_name": "stokonn/Gould"}
URL = "https://github.com/stokonn/Gould.git"
SHA = "a" * 40


def result(stdout="", code=0):
    return subprocess.CompletedProcess([], code, stdout, "")


class PushDestinationTest(unittest.TestCase):
    def destination(self, urls=URL + "\n", rewrite="", code=1):
        call = Mock(side_effect=[result(rewrite, code), result(urls)])
        return verified_push_destination(REPO, call)

    def test_single_verified_destination_returns_explicit_url(self):
        self.assertEqual(URL, self.destination())

    def test_mismatched_pushurl_is_rejected(self):
        with self.assertRaisesRegex(RuntimeError, "push destination"):
            self.destination("https://github.com/another/repository.git\n")

    def test_multiple_push_destinations_are_rejected(self):
        with self.assertRaises(RuntimeError):
            self.destination(URL + "\nhttps://github.com/another/repository.git\n")

    def test_ssh_and_credential_urls_are_rejected(self):
        for url in ["git@github.com:stokonn/Gould.git", "https://token@github.com/stokonn/Gould.git"]:
            with self.subTest(url=url), self.assertRaises(RuntimeError):
                self.destination(url)

    def test_url_rewrites_and_unavailable_config_fail_closed(self):
        for code, text in [(0, "url.evil.insteadof https://github.com/"), (0, "url.evil.pushinsteadof x"), (2, "")]:
            with self.subTest(code=code, text=text), self.assertRaises(RuntimeError):
                self.destination(rewrite=text, code=code)

    def test_wrong_api_identity_is_rejected_before_git(self):
        call = Mock()
        for repo in [{**REPO, "id": 1}, {**REPO, "full_name": "another/repository"}]:
            with self.assertRaises(RuntimeError):
                verified_push_destination(repo, call)
        call.assert_not_called()


class ReceiptTest(unittest.TestCase):
    def test_two_runs_preserve_legacy_and_first_run_receipts(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            parent = root / ".gradle/administration"
            parent.mkdir(parents=True)
            legacy = parent / "report.json"
            legacy.write_bytes(b"original execution receipt\n")
            first = write_receipt(new_receipt_directory(root, "administration"), {"run": 1})
            before = first.read_bytes()
            second = write_receipt(new_receipt_directory(root, "administration"), {"run": 2})
            self.assertNotEqual(first, second)
            self.assertEqual(before, first.read_bytes())
            self.assertEqual(b"original execution receipt\n", legacy.read_bytes())
            self.assertEqual(0o600, first.stat().st_mode & 0o777)

    def test_same_run_write_cannot_replace_receipt(self):
        with tempfile.TemporaryDirectory() as temp:
            p = write_receipt(temp, {"original": True})
            before = p.read_bytes()
            with self.assertRaises(FileExistsError):
                write_receipt(temp, {"original": False})
            self.assertEqual(before, p.read_bytes())


class ControllerTest(unittest.TestCase):
    def run_controller(self, module, root, api, call, **patches):
        with contextlib.ExitStack() as stack:
            stack.enter_context(patch.object(module, "ROOT", root))
            stack.enter_context(patch.object(module, "api", api))
            stack.enter_context(patch.object(module, "call", call))
            for name, value in patches.items():
                stack.enter_context(patch.object(module, name, value))
            stack.enter_context(patch.object(sys, "argv", [module.__name__, "--apply"]))
            stack.enter_context(contextlib.redirect_stdout(io.StringIO()))
            return module.main()

    def assert_required_registration_blocks(self, missing_path, disabled=False):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            rows = [{"id": i, "name": path, "path": path, "state": "active"}
                    for i, path in enumerate(sorted(final.REQUIRED))]
            if disabled:
                next(w for w in rows if w["path"] == missing_path)["state"] = "disabled_manually"
            else:
                rows = [w for w in rows if w["path"] != missing_path]
            # Include a removable registration: it must never be disabled here.
            rows.append({"id": 99, "name": "obsolete", "path": ".github/workflows/old.yml", "state": "active"})
            calls = Mock(side_effect=[result("", 1), result(URL + "\n")])
            api = Mock(side_effect=[REPO, {"commit": {"sha": SHA}}])
            forbidden = Mock(side_effect=AssertionError("reached past required registration gate"))
            self.assertEqual(1, self.run_controller(final, root, api, calls,
                definitions=Mock(return_value=final.REQUIRED), inventory=Mock(return_value=rows),
                pages=forbidden, tag_identity=forbidden, write_backup_manifest=forbidden))
            receipt = json.loads(next(root.glob(".gradle/final-maintenance-administration/run-*/report.json")).read_text())
            self.assertIn(missing_path, receipt["error"])
            self.assertIn("missing or inactive", receipt["error"])
            self.assertEqual(rows, receipt["workflows_before"])
            self.assertEqual([], receipt["disabled_workflows"])
            self.assertEqual([], receipt["deleted_branches"])
            self.assertFalse(receipt["release_deleted_by_this_run"])
            # Only read-only Git config/remote lookups occurred; no GitHub call
            # was delegated to call(), and no later API/write phase was reached.
            self.assertTrue(all(c.args[0] == "git" and c.args[1] in ("config", "remote")
                                for c in calls.call_args_list))
            self.assertEqual(2, api.call_count)
            forbidden.assert_not_called()

    def test_missing_required_registration_stops_before_all_mutations(self):
        for path in sorted(final.REQUIRED):
            with self.subTest(path=path):
                self.assert_required_registration_blocks(path)

    def test_disabled_required_registration_stops_before_all_mutations(self):
        for path in sorted(final.REQUIRED):
            with self.subTest(path=path):
                self.assert_required_registration_blocks(path, disabled=True)

    def test_blocked_workflow_stops_before_any_later_action_and_preserves_both_runs(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            calls = Mock(side_effect=[result("", 1), result(URL + "\n")] * 2)
            api = Mock(side_effect=[REPO, {"commit": {"sha": SHA}}] * 2)
            unknown = {"id": 1, "name": "unknown security", "path": "dynamic/security/unknown", "state": "active"}
            obsolete = {"id": 2, "name": "old", "path": ".github/workflows/old.yml", "state": "active"}
            # Any later preflight or backup access is itself a failure here.
            forbidden = Mock(side_effect=AssertionError("reached past blocked workflow gate"))
            for _ in range(2):
                self.assertEqual(1, self.run_controller(final, root, api, calls,
                    definitions=Mock(return_value=final.REQUIRED), inventory=Mock(return_value=[obsolete, unknown]),
                    pages=forbidden, tag_identity=forbidden))
            reports = list((root / ".gradle/final-maintenance-administration").glob("run-*/report.json"))
            self.assertEqual(2, len(reports))
            for p in reports:
                receipt = json.loads(p.read_text())
                self.assertEqual(2, len(receipt["workflows_before"]))
                self.assertEqual([], receipt["disabled_workflows"])
                self.assertEqual([], receipt["deleted_branches"])
                self.assertFalse(receipt["release_deleted_by_this_run"])
                self.assertIn("Blocked workflow", receipt["error"])
            self.assertTrue(all(c.args[0] == "git" and c.args[1] in ("config", "remote") for c in calls.call_args_list))
            forbidden.assert_not_called()

    def test_both_controllers_reject_mismatched_origin_before_mutations(self):
        for module in [old, final]:
            with self.subTest(controller=module.__name__), tempfile.TemporaryDirectory() as temp:
                def call(*args, **kwargs):
                    if args[:2] == ("python3", "-m"): return result()
                    if args[:2] == ("gh", "api"): return result(json.dumps(REPO))
                    if args[:2] == ("git", "config"): return result("", 1)
                    if args[:2] == ("git", "remote"): return result("https://github.com/other/repo.git\n")
                    self.fail(f"unexpected effect after origin mismatch: {args}")
                mocked = Mock(side_effect=call)
                extra = {"validate_local": Mock(return_value=SHA)} if module is old else {}
                self.assertEqual(1, self.run_controller(module, Path(temp), Mock(return_value=REPO), mocked, **extra))
                self.assertFalse(any(c.args[:2] == ("git", "push") for c in mocked.call_args_list))

    def test_final_controller_retains_branch_that_moves_after_review_without_any_mutation(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            def save(path, value):
                p = root / path; p.parent.mkdir(parents=True, exist_ok=True); p.write_text(json.dumps(value))
            backup = ".gradle/private-release-backups/v0.11-rc.1/verified-backup/"
            save(backup + "metadata.json", {"id": final.RC_ID, "tag_name": "v0.11-rc.1", "prerelease": True, "assets": []})
            save(backup + "preserved-tag-refs.json", {})
            bundle = root / "history.bundle"; bundle.write_bytes(b"# synthetic bundle\n\n")
            save(".git/portability-audit/verified-backup-heads.json", {"path": str(bundle), "sha256": hashlib.sha256(bundle.read_bytes()).hexdigest()})
            save("docs/maintenance/BRANCH-DELETION-PLAN.json", [{"branch": "old", "sha": SHA}])
            remote = {"head": SHA}
            def call(*args, **kwargs):
                if args[:2] == ("git", "config"): return result("", 1)
                if args[:2] == ("git", "remote"): return result(URL + "\n")
                if args[:3] == ("git", "bundle", "list-heads"): return result(SHA + " refs/heads/old\n")
                if args[:2] in [("git", "bundle"), ("git", "worktree")]: return result()
                if args[:2] == ("gh", "api"): return result(json.dumps({"commit": {"sha": remote["head"]}, "protected": False}))
                if args[:2] == ("git", "grep"):
                    remote["head"] = "b" * 40  # Concurrent owner push after API validation.
                    return result("", 1)
                self.fail(f"unexpected mutation: {args}")
            def api(path, *args):
                if path == "repos/stokonn/Gould": return REPO
                if path.endswith("branches/main"): return {"commit": {"sha": SHA}, "protected": False}
                if "/compare/" in path: return {"behind_by": 0}
                if path.endswith("issues/31"): return {"body": ""}
                self.fail(path)
            stable = {"id": final.STABLE_ID, "tag_name": "v0.11", "prerelease": False, "draft": False, "assets": []}
            pages = Mock(side_effect=lambda path: [stable] if "/releases?" in path else [])
            obsolete = {"id": 2, "name": "old", "path": ".github/workflows/old.yml", "state": "active"}
            active_required = [{"id": i + 10, "name": path, "path": path, "state": "active"}
                               for i, path in enumerate(sorted(final.REQUIRED))]
            self.assertEqual(1, self.run_controller(final, root, Mock(side_effect=api), Mock(side_effect=call),
                definitions=Mock(return_value=final.REQUIRED), inventory=Mock(return_value=active_required + [obsolete]),
                pages=pages, tag_identity=Mock(return_value=[])))
            self.assertEqual("b" * 40, remote["head"])
            receipt = json.loads(next(root.glob(".gradle/final-maintenance-administration/run-*/report.json")).read_text())
            self.assertEqual([], receipt["disabled_workflows"])
            self.assertEqual([], receipt["deleted_branches"])
            self.assertIn("atomic compare-and-delete", receipt["error"])

    def test_old_controller_retains_branch_after_head_race(self):
        with tempfile.TemporaryDirectory() as temp:
            remote = {"head": SHA}
            pushes = []
            def call(*args, **kwargs):
                if args[:2] == ("python3", "-m"): return result()
                if args[:2] == ("gh", "api"):
                    return result(json.dumps({"commit": {"sha": remote["head"]}, "protected": False} if "/branches/" in args[2] else REPO))
                if args[:2] == ("git", "config"): return result("", 1)
                if args[:2] == ("git", "remote"): return result(URL + "\n")
                if args[:2] in [("git", "ls-remote"), ("git", "worktree"), ("git", "merge-base")]: return result()
                if args[:2] == ("git", "push"):
                    self.assertEqual((URL, "HEAD:refs/heads/maintenance/gould-portability"), args[2:])
                    pushes.append(args); return result()
                if args[:2] == ("git", "grep"):
                    remote["head"] = "b" * 40
                    return result("", 1)
                self.fail(f"unexpected effect: {args}")
            def api(path, *args):
                if path.endswith("branches/main"): return {"commit": {"sha": old.MAIN}}
                if "/compare/" in path: return {"behind_by": 0}
                if "/actions/workflows?" in path: return {"workflows": [{"id": i, "path": ".github/workflows/" + p} for i, p in enumerate(["quality.yml", "community-bff.yml"])]}
                if "/actions/runs?" in path: return {"workflow_runs": [{"id": i, "workflow_id": i, "head_sha": SHA, "status": "completed", "conclusion": "success"} for i in [0, 1]]}
                self.fail(path)
            self.assertEqual(1, self.run_controller(old, Path(temp), Mock(side_effect=api), Mock(side_effect=call),
                validate_local=Mock(return_value=SHA), pages=Mock(return_value=[]), CANDIDATES={"old": SHA}))
            self.assertEqual("b" * 40, remote["head"])
            self.assertEqual(1, len(pushes))  # Only a mocked non-destructive publication; no deletion.
            receipt = json.loads(next(Path(temp).glob(".gradle/final-github-administration/run-*/report.json")).read_text())
            self.assertEqual([], receipt["deleted_branches"])
            self.assertIn("atomic compare-and-delete", receipt["error"])


if __name__ == "__main__":
    unittest.main()
