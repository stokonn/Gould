"""Shared fail-closed guards for legacy maintenance controllers.

No network calls or Git mutations are performed by these helpers.
"""
import json
import os
from pathlib import Path
import tempfile


REPOSITORY_ID = 1421247418
APPROVED_NAMES = {"stokonn/Gould", "stokonn/Goldberg"}


def verified_push_destination(repository, call):
    """Bind a single effective HTTPS push URL to the API-verified identity.

    Return an explicit URL, never a remote alias that can redirect a later push.
    Reject URL rewrite configuration even when it currently resolves correctly.
    """
    name = repository.get("full_name")
    if repository.get("id") != REPOSITORY_ID or name not in APPROVED_NAMES:
        raise RuntimeError("Push repository identity is not approved")
    rewrites = call("git", "config", "--get-regexp", r"^url\..*\.(insteadof|pushinsteadof)$", check=False)
    if rewrites.returncode not in (0, 1) or rewrites.stdout.strip():
        raise RuntimeError("Git URL rewrites are ambiguous; no push allowed")
    urls = call("git", "remote", "get-url", "--push", "--all", "origin").stdout.splitlines()
    expected = f"https://github.com/{name}.git"
    if urls != [expected]:
        raise RuntimeError("Origin must have exactly one approved canonical HTTPS push destination")
    return expected


def refuse_branch_deletion(branch, expected):
    """Retain branches until a separately approved atomic backend exists.

    GitHub REST DELETE refs has no expected-SHA condition. The legacy ordinary
    push deletion is unsafe. Do not substitute another GET, an unconditional
    REST delete, or force-with-lease under the standing no-force-push policy.
    """
    raise RuntimeError(f"Retain {branch} at approved head {expected}: no approved atomic compare-and-delete backend")


def require_unblocked_workflows(decisions):
    allowed = {"KEEP", "KEEP_MANAGED_COPILOT_INTEGRATION", "OBSOLETE_REGISTRATION"}
    if any(row["classification"] not in allowed for row in decisions):
        raise RuntimeError("Blocked workflow decision: no administrative mutations allowed")


def new_receipt_directory(root, name):
    parent = Path(root) / ".gradle" / name
    if parent.is_symlink():
        raise RuntimeError("Receipt parent must not be a symlink")
    parent.mkdir(parents=True, exist_ok=True, mode=0o700)
    return Path(tempfile.mkdtemp(prefix="run-", dir=parent))


def write_receipt(directory, report):
    path = Path(directory) / "report.json"
    # Exclusive creation: even a repeated write in one run cannot erase evidence.
    with os.fdopen(os.open(path, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600), "w") as stream:
        stream.write(json.dumps(report, indent=2) + "\n")
    return path
