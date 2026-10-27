"""Owner-approved GitHub administration from a host with working Git/gh access.

Never merges main, publishes releases, force-pushes, or deletes local data.
"""
import argparse
import hashlib
import json
from pathlib import Path
import subprocess
import tempfile
import time
from urllib.parse import quote
from maintenance_admin_safety import (new_receipt_directory, write_receipt,
                                      verified_push_destination, refuse_branch_deletion)

ROOT = Path(__file__).resolve().parents[1]
REPOSITORY_ID = 1421247418
BASELINE = "0ffebeb2d87046aa7074249fea5cb7fd11b9f8a3"
MAIN = "1c943dc925231a3725a0e9d9aa6b3e34d19e445a"
REPORT = ".gradle/final-consolidation/422d1273275945fba6a8c537bd7c1002/report.json"
REPORT_SHA = "29f0101cc25809caf0a61a91b83ba46bdcb988253006e60f9b8b4f9c15740731"
# The inventory mention records this merged commit; no source link depends on its branch.
CANDIDATES = {"copilot/feature-xyz": "3e7c25e1b624759f7ba537fbd8cbee4bed21bd04"}


def call(*args, check=True):
    result = subprocess.run(args, cwd=ROOT, text=True, capture_output=True)
    if check and result.returncode:
        raise RuntimeError(f"{args[0]} command failed: {result.stderr.strip()}")
    return result


def api(path, *args):
    return json.loads(call("gh", "api", path, *args).stdout)


def pages(path):
    return [item for page in api(path, "--paginate", "--slurp") for item in page]


def candidate_errors(branch, expected, actual, protected, unique, prs, releases, used):
    errors = []
    if branch == "main" or branch.startswith("release/"):
        errors.append("protected purpose")
    if actual != expected:
        errors.append("head changed")
    if protected:
        errors.append("branch protected")
    if unique != 0:
        errors.append("unique or unresolved commits")
    if branch in used:
        errors.append("registered worktree")
    if any(branch in (p["head"]["ref"], p["base"]["ref"]) for p in prs):
        errors.append("open pull request")
    if any(p.get("target_commitish") == branch for p in releases):
        errors.append("release dependency")
    return errors


def release_identity(releases):
    return sorted((r["id"], r["tag_name"], sorted(
        (a["id"], a["name"], a["size"], a.get("digest")) for a in r["assets"])) for r in releases)


def validate_local():
    report_path = ROOT / REPORT
    assert hashlib.sha256(report_path.read_bytes()).hexdigest() == REPORT_SHA, "Host receipt changed"
    report = json.loads(report_path.read_text())
    assert report["commit"] == BASELINE and report["accepted"] and report["source_unchanged"]
    assert len(report["checks"]) == 19 and all(c["exit_code"] == 0 for c in report["checks"])
    assert report["jvm"] == {"tests": 348, "failures": 0, "errors": 0, "skipped": 0}
    assert call("git", "branch", "--show-current").stdout.strip() == "maintenance/gould-portability"
    assert not call("git", "status", "--porcelain", "--untracked-files=no").stdout.strip(), "Commit reviewed changes first"
    call("git", "merge-base", "--is-ancestor", BASELINE, "HEAD")
    changed = call("git", "diff", "--name-only", BASELINE, "HEAD").stdout.splitlines()
    allowed = {"README.md", "AGENTS.md", "scripts/finalize_github.py", "scripts/test_finalize_github.py",
               ".github/workflows/stage5-emulator.yml", ".github/workflows/stage5-screenshots.yml"}
    assert all(p in allowed or p.startswith("docs/") for p in changed), "Unverified application/backend changes"
    return call("git", "rev-parse", "HEAD").stdout.strip()


def main():
    if not __debug__:
        raise RuntimeError("Run without Python optimization; all safety assertions must remain enabled")
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--apply", action="store_true", help="Execute the already approved, guarded administration")
    args = parser.parse_args()
    output = new_receipt_directory(ROOT, "final-github-administration")
    result = {"complete": False, "deleted_branches": [], "repository_renamed": False}
    try:
        head = validate_local()
        call("python3", "-m", "unittest", "discover", "-s", "scripts", "-p", "test_finalize_github.py")
        result["local_commit"] = head
        old = call("gh", "api", "repos/stokonn/Goldberg", check=False)
        if old.returncode:
            raise RuntimeError("Cannot read original repository; check host gh authentication/network")
        repository = json.loads(old.stdout)
        assert repository["id"] == REPOSITORY_ID
        repo = repository["full_name"]
        assert repo in ("stokonn/Goldberg", "stokonn/Gould")
        result["repository"] = repo
        push_destination = verified_push_destination(repository, call)
        assert api(f"repos/{repo}/branches/main")["commit"]["sha"] == MAIN, "Main changed; re-audit first"
        target = call("gh", "api", "repos/stokonn/Gould", check=False)
        if target.returncode == 0:
            assert json.loads(target.stdout)["id"] == REPOSITORY_ID, "Target name belongs to another repository"
        elif "HTTP 404" not in target.stderr:
            raise RuntimeError("Target repository availability could not be established")
        releases = pages(f"repos/{repo}/releases?per_page=100")
        before_assets = release_identity(releases)
        before_tags = sorted(call("git", "ls-remote", "--tags", push_destination).stdout.splitlines())
        prs = pages(f"repos/{repo}/pulls?state=open&per_page=100")
        result["plan"] = {"rename": [repo, "stokonn/Gould"], "branch_candidates": CANDIDATES,
                          "required_ci": ["quality.yml", "community-bff.yml"], "main_merge": False}
        if not args.apply:
            print(json.dumps(result["plan"], indent=2))
            return 0
        call("git", "push", push_destination, "HEAD:refs/heads/maintenance/gould-portability")
        workflows = api(f"repos/{repo}/actions/workflows?per_page=100")["workflows"]
        ids = {w["id"] for w in workflows if w["path"] in
               (".github/workflows/quality.yml", ".github/workflows/community-bff.yml")}
        assert len(ids) == 2, "Required CI workflows missing"
        deadline = time.monotonic() + 2400
        while True:
            runs = api(f"repos/{repo}/actions/runs?head_sha={head}&per_page=100")["workflow_runs"]
            selected = {}
            for run in sorted(runs, key=lambda r: r["id"]):
                if run["workflow_id"] in ids and run["head_sha"] == head:
                    selected[run["workflow_id"]] = run
            if len(selected) == 2 and all(r["status"] == "completed" for r in selected.values()):
                result["ci"] = [{"id": r["id"], "sha": r["head_sha"], "conclusion": r["conclusion"]}
                                for r in selected.values()]
                assert all(r["conclusion"] == "success" for r in selected.values()), "Exact-commit CI failed"
                break
            if time.monotonic() >= deadline:
                raise RuntimeError("Exact-commit CI did not finish; no deletion/rename attempted")
            print("Waiting for exact-commit quality/backend CI", flush=True)
            time.sleep(15)
        used = {s.removeprefix("branch refs/heads/") for s in
                call("git", "worktree", "list", "--porcelain").stdout.splitlines() if s.startswith("branch ")}
        for branch, expected in CANDIDATES.items():
            info = call("gh", "api", f"repos/{repo}/branches/{quote(branch, safe='')}", check=False)
            if info.returncode:
                assert "HTTP 404" in info.stderr, "Candidate could not be read"
                continue  # Already deleted: leave it absent.
            data = json.loads(info.stdout)
            compare = api(f"repos/{repo}/compare/{expected}...{MAIN}")
            current_prs = pages(f"repos/{repo}/pulls?state=open&per_page=100")
            errors = candidate_errors(branch, expected, data["commit"]["sha"], data["protected"],
                                      compare["behind_by"], current_prs, releases, used)
            assert not errors, f"Retain {branch}: {errors}"
            for kind in ("blob", "tree"):
                references = call("git", "grep", "-l", "-F", f"/{kind}/{branch}/", "--", ".", check=False)
                assert references.returncode in (0, 1), "Reference audit failed"
                assert not references.stdout.strip(), "A source link still needs this branch"
            call("git", "merge-base", "--is-ancestor", expected, MAIN)
            refuse_branch_deletion(branch, expected)
        if repo == "stokonn/Goldberg":
            renamed = api(f"repos/{repo}", "--method", "PATCH", "--field", "name=Gould")
            assert renamed["id"] == REPOSITORY_ID and renamed["full_name"] == "stokonn/Gould"
            result["repository_renamed"] = True
        result["repository"] = "stokonn/Gould"
        call("git", "remote", "set-url", "origin", "https://github.com/stokonn/Gould.git")
        assert sorted(call("git", "ls-remote", "--tags", push_destination).stdout.splitlines()) == before_tags, "Tag refs changed"
        assert release_identity(pages("repos/stokonn/Gould/releases?per_page=100")) == before_assets, "Release assets changed"
        assert api("repos/stokonn/Gould")["id"] == REPOSITORY_ID
        assert api("repos/stokonn/Gould/branches/main")["commit"]["sha"] == MAIN
        # A real independent remote clone, with no checkout in which owner files could accumulate.
        with tempfile.TemporaryDirectory(prefix="gould-clone-verification-") as directory:
            clone = Path(directory) / "repo"
            call("git", "clone", "--no-checkout", "https://github.com/stokonn/Gould.git", str(clone))
            call("git", "-C", str(clone), "fsck", "--full")
            assert call("git", "-C", str(clone), "rev-parse", "refs/remotes/origin/maintenance/gould-portability").stdout.strip() == head
            assert {p.name for p in clone.iterdir()} == {".git"}
            result["independent_remote_clone"] = "PASS"
        result["remaining_branches"] = len(pages("repos/stokonn/Gould/branches?per_page=100"))
        result["complete"] = True
    except (AssertionError, OSError, RuntimeError) as error:
        result["error"] = str(error)
    finally:
        receipt = write_receipt(output, result)
        print("Administration receipt: " + str(receipt))
    return 0 if result["complete"] else 1


if __name__ == "__main__":
    raise SystemExit(main())
