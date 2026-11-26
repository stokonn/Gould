#!/usr/bin/env python3
"""Guard that the retired admin/community backend surface stays retired.

Gould is an Android-first, local-first application: there is no product
community backend, BFF, moderation system, admin API, community database or
custom cloud backend. This check fails if any of that retired surface reappears
in active application source, Gradle configuration, workflows or user-facing
documentation. It never touches the network, a device or remote infrastructure.
"""
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
MAIN = ROOT / "app/src/main/java/com/opus/piano"
APP_SRC = ROOT / "app/src"
WORKFLOWS = ROOT / ".github/workflows"

# --- Obsolete operated-backend claims in public/user-facing documentation. ---
# Exact phrasings that only ever appeared in descriptions of the retired service.
OBSOLETE_CLAIM_LITERALS = (
    "services/community-bff",
    "community-bff.yml",
    "GET /v1/catalogue/overlay",
    "GET /v1/catalogue/changes",
    "Hyperdrive",
    "Supabase PostgreSQL",
    "Cloudflare Workers handles",
    "read-only catalogue service",
    "owner-authorized read-only",
    "public cloud catalogue",
)
# A sentence that denies or historicises first-party operation is truthful, not a claim.
NEGATION_OR_HISTORY = re.compile(
    r"(?i)\b(no|not|never|none|neither|nor|without|zero|retire[ds]?|retiring"
    r"|remove[ds]?|former|formerly|historical|superseded|obsolete|legacy|deleted"
    r"|abolished|withdrawn|shut down)\b"
)
SERVICE_TERMS = (
    "admin api",
    "catalogue api",
    "community backend",
    "cloud backend",
    "bff",
)


def claim_violations(text):
    """Return obsolete operated-backend claims found in one document's text.

    Statements that deny first-party operation or mark it as history ("retired",
    "former", "no longer") are truthful and must not fail the guard; only
    present-tense claims about an operated service are violations.
    """
    problems = []
    for sentence in re.split(r"(?<=[.!?])\s+", re.sub(r"\s+", " ", text)):
        normalized = sentence.strip().lower()
        if not normalized or NEGATION_OR_HISTORY.search(normalized):
            continue
        for literal in OBSOLETE_CLAIM_LITERALS:
            if literal.lower() in normalized:
                problems.append(f"obsolete operated-backend phrase {literal!r}")
        for term in SERVICE_TERMS:
            if term in normalized:
                problems.append(f"positive claim about a first-party service: {term!r}")
    return problems


def documentation_targets():
    """User-facing documentation: public pages plus every notice bundled in the APK."""
    return (
        [ROOT / "README.md", ROOT / "docs/PRIVACY.md"]
        + sorted((ROOT / "docs/wiki").glob("*.md"))
        + sorted((ROOT / "app/src/main/assets/legal").glob("*.md"))
    )


def check_legacy_admin():
    activity = (MAIN / "MainActivity.kt").read_text()
    navigation = (MAIN / "ui/AppNavigation.kt").read_text()
    app = (MAIN / "ui/OpusApp.kt").read_text()
    application = (MAIN / "OpusApplication.kt").read_text()
    profile = (MAIN / "ui/ProfileScreen.kt").read_text()
    container = (MAIN / "AppContainer.kt").read_text()

    assert "ui.admin.AdminViewModel" not in activity
    assert "composable(Routes.ADMIN" not in app
    assert "const val ADMIN =" not in navigation
    assert "admin/queue" not in navigation
    assert "administration" not in profile.lower(), "legacy admin entry point remains in ProfileScreen"
    assert 'cancelUniqueWork("goldberg-admin-queue-check")' in application
    assert "com.opus.piano.admin" not in container
    assert "com.opus.piano.admin" not in application
    assert not list((MAIN / "admin").glob("*.kt")), "legacy admin implementation remains"
    assert not list((MAIN / "ui/admin").glob("*.kt")), "legacy admin UI remains"
    assert not list((ROOT / "app/src/test/java/com/opus/piano/admin").glob("*.kt")), "legacy admin unit tests remain"
    assert not any("Stage7b" in path.name for path in (ROOT / "app/src/androidTest/java").rglob("*.kt")), "legacy admin instrumentation remains"


def check_canonical_admin():
    assert not (MAIN / "data/community/canonical").exists(), "canonical admin client remains in main source"
    for source_set in ("main", "test", "androidTest", "canonicalIntegration"):
        leftover = list((APP_SRC / source_set).rglob("*.kt")) if (APP_SRC / source_set).exists() else []
        for path in leftover:
            text = path.read_text()
            assert "CanonicalAdmin" not in text, f"canonical admin reference remains: {path.relative_to(ROOT)}"
            assert "AdminContract" not in text, f"admin contract reference remains: {path.relative_to(ROOT)}"
    assert not (APP_SRC / "canonicalIntegration").exists(), "canonical integration source set remains"
    assets_community = ROOT / "app/src/main/assets/community"
    assert not assets_community.exists() or not list(assets_community.glob("*.json")), "canonical admin contract asset remains"


def check_gradle_wiring():
    for gradle_file in [ROOT / "app/build.gradle.kts", ROOT / "build.gradle.kts", ROOT / "settings.gradle.kts"]:
        text = gradle_file.read_text()
        assert "contracts/admin" not in text, f"contracts/admin test resources remain: {gradle_file.name}"
        assert "goldbergCanonicalIntegration" not in text, f"canonical integration switch remains: {gradle_file.name}"


def check_backend_removal():
    assert not (ROOT / "services/community-bff").exists(), "community BFF sources remain"
    assert not (ROOT / "contracts/admin").exists(), "admin contract corpus remains"
    assert not (ROOT / "spikes").exists() or not any(p.name.startswith("stage6") for p in (ROOT / "spikes").iterdir()), "Stage 6 backend spikes remain"
    for workflow in ("community-bff.yml", "maintenance-admin.yml"):
        assert not (WORKFLOWS / workflow).exists(), f"retired workflow remains: {workflow}"
    for path in WORKFLOWS.glob("*.y*ml"):
        text = path.read_text()
        for deleted in ("ci_stage6", "scripts/stage6", "stage6-accepted-src", "contracts/admin", "services/community-bff", "check_admin_contract_fixtures", "finalize_github", "maintenance_admin"):
            assert deleted not in text, f"{path.name} references deleted backend file/step: {deleted}"


def check_community_transport():
    transport = (MAIN / "data/community/CommunityTransport.kt").read_text()
    assert "object DisabledCommunityTransport" in transport
    assert "DISABLED" in transport
    assert not re.search(r"https?://", transport), "CommunityTransport must not carry a URL"


def check_documentation_claims():
    for doc in documentation_targets():
        assert doc.exists(), f"expected user-facing document is missing: {doc.relative_to(ROOT)}"
        problems = claim_violations(doc.read_text())
        assert not problems, f"{doc.relative_to(ROOT)} still claims a retired backend: {problems}"


def main():
    check_legacy_admin()
    check_canonical_admin()
    check_gradle_wiring()
    check_backend_removal()
    check_community_transport()
    check_documentation_claims()
    print("PASS: legacy admin, canonical admin client, contracts/admin corpus, backend workflows and Gradle integration switches are retired; community networking remains disabled; public and bundled privacy documents claim no operated backend")


if __name__ == "__main__":
    main()
