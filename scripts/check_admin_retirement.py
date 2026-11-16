#!/usr/bin/env python3
"""Guard that the retired legacy admin package is absent and unreachable."""
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
MAIN = ROOT / "app/src/main/java/com/opus/piano"

activity = (MAIN / "MainActivity.kt").read_text()
navigation = (MAIN / "ui/AppNavigation.kt").read_text()
app = (MAIN / "ui/OpusApp.kt").read_text()
application = (MAIN / "OpusApplication.kt").read_text()
profile = (MAIN / "ui/ProfileScreen.kt").read_text()
container = (MAIN / "AppContainer.kt").read_text()
canonical = MAIN / "data/community/canonical"
main_sources = list(MAIN.rglob("*.kt"))

assert "ui.admin.AdminViewModel" not in activity
assert "composable(Routes.ADMIN" not in app
assert "const val ADMIN =" not in navigation
assert "admin/queue" not in navigation
assert "administration" not in profile.lower(), "legacy admin entry point remains in ProfileScreen"
assert "data.community.canonical.CanonicalAdminRepository" in application
assert "data.community.canonical.CanonicalAdminClient" in application
assert 'error("Community networking disabled")' in application
assert 'cancelUniqueWork("goldberg-admin-queue-check")' in application
assert "com.opus.piano.admin" not in container
assert "com.opus.piano.admin" not in application
assert not list((MAIN / "admin").glob("*.kt")), "legacy admin implementation remains"
assert not list((MAIN / "ui/admin").glob("*.kt")), "legacy admin UI remains"
assert not list((ROOT / "app/src/test/java/com/opus/piano/admin").glob("*.kt")), "legacy admin unit tests remain"
assert not any("Stage7b" in path.name for path in (ROOT / "app/src/androidTest/java").rglob("*.kt")), "legacy admin instrumentation remains"
for path in canonical.rglob("*.kt"):
    assert "com.opus.piano.admin" not in path.read_text(), f"canonical code imports legacy admin: {path.relative_to(ROOT)}"
for path in main_sources:
    source = path.read_text()
    assert "ui.admin" not in source and "Routes.ADMIN" not in source, f"legacy admin route reference remains: {path.relative_to(ROOT)}"
    assert not re.search(r"\b(?:AdminTransport|AdminRepository|AdminSettingsStore|AdminSyncWorker)\b", source), f"active application source wires a legacy admin component: {path.relative_to(ROOT)}"
    if path.parent != canonical:
        assert "com.opus.piano.admin" not in source, f"active application source depends on legacy admin: {path.relative_to(ROOT)}"
print("PASS: legacy admin source, routes, wiring, tests, and canonical-package dependencies are retired; obsolete WorkManager job is cancelled on upgrade")
