"""Reject new bare user-visible Text literals; listed legacy labels are audited."""
from collections import Counter
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
LEGACY = {
    "BalancedHome.kt": Counter({"— Robert Schumann": 1}),
    "CustomizePanel.kt": Counter({"♫": 1}),
    "DiscoverScreen.kt": Counter({"SHA-256: ": 1}),
    "SettingsScreen.kt": Counter({"›": 1, "GitHub": 1, "created by stokonn with love <3": 1}),
}
pattern = re.compile(r'\bText\s*\(\s*"((?:\\.|[^"\\$])*)"')
actual = Counter()
for path in (ROOT / "app/src/main/java/com/opus/piano/ui").rglob("*.kt"):
    for value in pattern.findall(path.read_text()):
        actual[(path.name, value)] += 1

expected = Counter({(name, value): count for name, entries in LEGACY.items() for value, count in entries.items()})
if actual != expected:
    for key, count in (actual - expected).items():
        print(f"Unapproved raw Text literal {key[0]}: {key[1]!r} ({count})")
    for key, count in (expected - actual).items():
        print(f"Legacy Text literal changed {key[0]}: {key[1]!r} ({count})")
    raise SystemExit(1)
print(f"PASS: {sum(actual.values())} audited legacy Text literals; no new raw UI strings")
