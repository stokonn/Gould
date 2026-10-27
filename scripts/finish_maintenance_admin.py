"""Audit all Actions registrations and remove only the backed-up 0.11 prerelease.

Requires normal host gh access. Never deletes workflow runs or Git tags.
"""
import argparse
import hashlib
import json
import os
from pathlib import Path
from urllib.parse import quote, unquote
from finalize_github import api, call, pages, release_identity, candidate_errors, REPOSITORY_ID, ROOT

REPO = "stokonn/Gould"
RC_ID = 423537826
STABLE_ID = 425356546
REQUIRED = {".github/workflows/quality.yml", ".github/workflows/community-bff.yml",
            ".github/workflows/stage5-emulator.yml"}


def classify_workflow(workflow, main_paths, maintenance_paths):
    path = workflow["path"]
    if path in main_paths or path in maintenance_paths or path in REQUIRED:
        return "KEEP"
    if path.startswith(".github/workflows/") and path.endswith((".yml", ".yaml")):
        return "OBSOLETE_REGISTRATION"
    return "BLOCKED_UNKNOWN_OR_MANAGED_SECURITY_WORKFLOW"


def inventory():
    response = api(f"repos/{REPO}/actions/workflows?per_page=100", "--paginate", "--slurp")
    rows = [w for page in response for w in page["workflows"]]
    assert len({w["id"] for w in rows}) == len(rows)
    assert len(rows) == response[0]["total_count"], "Incomplete workflow pagination"
    return rows


def definitions(ref):
    rows = api(f"repos/{REPO}/contents/.github/workflows?ref={ref}")
    return {r["path"] for r in rows if r["type"] == "file" and r["name"].endswith((".yml", ".yaml"))}


def tag_identity():
    return sorted((r["ref"], r["object"]["sha"]) for r in api(f"repos/{REPO}/git/matching-refs/tags/"))


def require_absent(endpoint):
    response = call("gh", "api", endpoint, check=False)
    assert response.returncode != 0 and "HTTP 404" in response.stderr, "Resource absence not verified"


def write_backup_manifest(backup):
    """Keep published asset names separate from the administrative manifest."""
    manifest = backup / "BACKUP-SHA256SUMS.txt"
    hashes = {}
    for path in backup.rglob("*"):
        assert not path.is_symlink(), "Unexpected backup symlink"
        if path.is_file() and path != manifest:
            os.chmod(path, 0o600)
            hashes[path.relative_to(backup).as_posix()] = hashlib.sha256(path.read_bytes()).hexdigest()
    manifest.write_text("".join(h + "  " + n + "\n" for n, h in sorted(hashes.items())))
    os.chmod(manifest, 0o600)
    return hashes


def main():
    if not __debug__:
        raise RuntimeError("Safety assertions require non-optimized Python")
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--apply", action="store_true")
    args = parser.parse_args()
    output = ROOT / ".gradle/final-maintenance-administration"
    output.mkdir(parents=True, exist_ok=True, mode=0o700)
    report = {"complete": False, "repository": REPO, "disabled_workflows": [],
              "release_deleted_by_this_run": False, "workflow_runs_deleted": 0, "deleted_branches": []}
    try:
        assert api(f"repos/{REPO}")["id"] == REPOSITORY_ID
        report["main_commit"] = api(f"repos/{REPO}/branches/main")["commit"]["sha"]
        main_paths = definitions("main")
        maintenance_paths = definitions("maintenance/gould-portability")
        assert REQUIRED <= main_paths, "Required CI definition missing from main"
        before = inventory()
        report.update(workflows_before=before, main_definitions=sorted(main_paths),
                      maintenance_definitions=sorted(maintenance_paths))
        decisions = [{"id": w["id"], "name": w["name"], "path": w["path"], "state": w["state"],
                      "classification": classify_workflow(w, main_paths, maintenance_paths)} for w in before]
        report["workflow_decisions"] = decisions
        # Retain ambiguous required-check/ruleset dependencies rather than guessing.
        rules = pages(f"repos/{REPO}/rulesets?includes_parents=true&per_page=100")
        report["rulesets"] = rules
        main_branch = api(f"repos/{REPO}/branches/main")
        required_contexts = []
        if main_branch["protected"]:
            protection = api(f"repos/{REPO}/branches/main/protection")
            report["main_protection"] = protection
            status = protection.get("required_status_checks") or {}
            required_contexts = status.get("contexts", []) + [c["context"] for c in status.get("checks", [])]
        if rules or required_contexts:
            assert not any(d["classification"] == "OBSOLETE_REGISTRATION" and d["state"] == "active"
                           for d in decisions), "Required-check/ruleset dependency needs review before disabling"
        tags = tag_identity()
        releases = pages(f"repos/{REPO}/releases?per_page=100")
        stable = next(r for r in releases if r["id"] == STABLE_ID)
        assert stable["tag_name"] == "v0.11" and not stable["prerelease"] and not stable["draft"]
        other_releases = release_identity([r for r in releases if r["id"] != RC_ID])
        rc = next((r for r in releases if r["id"] == RC_ID), None)
        backup = ROOT / ".gradle/private-release-backups/v0.11-rc.1/verified-backup"
        metadata = json.loads((backup / "metadata.json").read_text())
        assert metadata["id"] == RC_ID and metadata["tag_name"] == "v0.11-rc.1" and metadata["prerelease"]
        report["rc_backup"] = str(backup.relative_to(ROOT))
        os.chmod(backup, 0o700)
        if rc:
            assert rc["tag_name"] == "v0.11-rc.1" and rc["prerelease"] and not rc["draft"]
            assert {(a["id"], a["name"], a["digest"]) for a in rc["assets"]} == {
                (a["id"], a["name"], a["digest"]) for a in metadata["assets"]}, "RC assets changed; re-backup first"
            (backup / "metadata-at-deletion.json").write_text(json.dumps(rc, indent=2) + "\n")
            (backup / "release-notes-at-deletion.md").write_text(rc["body"] or "")
        for asset in metadata["assets"]:
            assert Path(asset["name"]).name == asset["name"]
            path = backup / "assets" / asset["name"]
            assert path.is_file() and not path.is_symlink()
            assert path.stat().st_size == asset["size"]
            assert "sha256:" + hashlib.sha256(path.read_bytes()).hexdigest() == asset["digest"]
        saved_tags = json.loads((backup / "preserved-tag-refs.json").read_text())
        for tag, sha in saved_tags.items():
            assert ("refs/tags/" + tag, sha) in tags
        hashes = write_backup_manifest(backup)
        # Recheck after manifest creation as well as before it.
        for asset in metadata["assets"]:
            assert "sha256:" + hashes["assets/" + asset["name"]] == asset["digest"]
        report["backup_sha256"] = hashes
        branch_plan = json.loads((ROOT / "docs/maintenance/BRANCH-DELETION-PLAN.json").read_text())
        history = json.loads((ROOT / ".git/portability-audit/verified-backup-heads.json").read_text())
        bundle = Path(history["path"])
        digest = hashlib.sha256()
        with bundle.open("rb") as stream:
            for chunk in iter(lambda: stream.read(1024 * 1024), b""):
                digest.update(chunk)
        assert digest.hexdigest() == history["sha256"], "Independent history backup changed"
        call("git", "bundle", "verify", str(bundle))
        with bundle.open("rb") as stream:
            for line in stream:
                if line == b"\n":
                    break
                assert not line.startswith(b"-"), "History bundle needs external prerequisites"
        saved_heads = {line.split()[0] for line in call("git", "bundle", "list-heads", str(bundle)).stdout.splitlines()}
        assert all(row["sha"] in saved_heads for row in branch_plan), "Deletion head absent from independent backup"
        report["branch_deletion_plan"] = branch_plan
        if not args.apply:
            print(json.dumps(decisions, indent=2))
            return 0
        for row in decisions:
            if row["classification"] == "OBSOLETE_REGISTRATION" and row["state"] == "active":
                call("gh", "api", f"repos/{REPO}/actions/workflows/{row['id']}/disable", "--method", "PUT")
                report["disabled_workflows"].append(row["id"])
        used = {line.removeprefix("branch refs/heads/") for line in
                call("git", "worktree", "list", "--porcelain").stdout.splitlines() if line.startswith("branch ")}
        for row in branch_plan:
            branch, expected = row["branch"], row["sha"]
            current = call("gh", "api", f"repos/{REPO}/branches/{quote(branch, safe='')}", check=False)
            if current.returncode:
                assert "HTTP 404" in current.stderr, "Branch state unavailable"
                continue
            branch_data = json.loads(current.stdout)
            compare = api(f"repos/{REPO}/compare/{expected}...{report['main_commit']}")
            prs = pages(f"repos/{REPO}/pulls?state=open&per_page=100")
            assert not candidate_errors(branch, expected, branch_data["commit"]["sha"], branch_data["protected"],
                                        compare["behind_by"], prs, releases, used), "Branch dependencies changed"
            issue = api(f"repos/{REPO}/issues/31")
            comments = pages(f"repos/{REPO}/issues/31/comments?per_page=100")
            for document in [issue, *comments]:
                body = unquote(document.get("body") or "")
                assert all(f"/{kind}/{branch}" not in body for kind in ("blob", "tree")), "Historical issue source link depends on branch"
            for kind in ("blob", "tree"):
                for spelling in (branch, quote(branch, safe="")):
                    refs = call("git", "grep", "-l", "-F", f"/{kind}/{spelling}", "--", ".", check=False)
                    assert refs.returncode in (0, 1) and not refs.stdout.strip(), "Source link depends on branch"
            call("git", "push", "origin", "--delete", branch)
            report["deleted_branches"].append(branch)
        if rc:
            call("gh", "api", f"repos/{REPO}/releases/{RC_ID}", "--method", "DELETE")
            report["release_deleted_by_this_run"] = True
        require_absent(f"repos/{REPO}/releases/tags/v0.11-rc.1")
        for asset in metadata["assets"]:
            require_absent(f"repos/{REPO}/releases/assets/{asset['id']}")
        assert tag_identity() == tags, "Tag refs changed"
        after_releases = pages(f"repos/{REPO}/releases?per_page=100")
        assert release_identity(after_releases) == other_releases, "Another release or asset changed"
        after = inventory()
        report["workflows_after"] = after
        report["active_workflow_count"] = sum(w["state"] == "active" for w in after)
        assert all(w["state"] != "active" for w in after if w["id"] in report["disabled_workflows"])
        assert REQUIRED <= {w["path"] for w in after if w["state"] == "active"}
        report["rc_no_longer_published"] = True
        report["rc_tag_preserved"] = True
        report["stable_and_other_releases_unchanged"] = True
        report["complete"] = not any(d["classification"].startswith("BLOCKED") for d in decisions)
    except (AssertionError, OSError, RuntimeError, ValueError) as error:
        report["error"] = str(error)
    finally:
        path = output / "report.json"
        path.write_text(json.dumps(report, indent=2) + "\n")
        print("Final maintenance administration receipt: " + str(path))
    return 0 if report["complete"] else 1


if __name__ == "__main__":
    raise SystemExit(main())
