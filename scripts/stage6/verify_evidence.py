"""Validate the committed Stage6 local acceptance record; never reruns hosted tests."""
from pathlib import Path
import hashlib
import json
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[2]
BASE = ROOT / 'docs/planning/goldberg-0.11/stage-6/results/end-to-end-2026-10-25'
record = json.loads((BASE/'verification.json').read_text())
sha = lambda p: hashlib.sha256(p.read_bytes()).hexdigest()
for name, expected in record['sourceSha256'].items():
    assert sha(ROOT/name) == expected, 'Source differs from verified acceptance: ' + name
for name, expected in record['artifacts'].items():
    assert sha(BASE/name) == expected, 'Raw evidence changed: ' + name
suites = [BASE/'TEST-com.opus.piano.data.community.canonical.CanonicalAdminClientTest.xml', *sorted((BASE/'jvm').glob('TEST-*.xml'))]
totals = {key: 0 for key in ['tests', 'failures', 'errors', 'skipped']}
for suite in suites:
    result = ET.parse(suite).getroot()
    for key in totals:
        totals[key] += int(result.attrib[key])
assert totals == record['jvm'] and len(suites) == record['jvmSuites']
assert totals['tests'] == 233 and totals['failures'] == totals['errors'] == totals['skipped'] == 0
assert record['newJvmTests'] == 19 and record['hostedWrites'] == 0
assert 'BUILD SUCCESSFUL' in (BASE/'goldberg-stage6-source-frozen-host.log').read_text()
assert 'Ran 5 tests' in (BASE/'goldberg-backup-store-final.log').read_text()
assert (BASE/'goldberg-backup-store-final.log').read_text().strip().endswith('OK')
tls = json.loads((ROOT/record['tlsResult']).read_text())
assert tls['status'] == 'PASS' and len(tls['checks']) == 6
assert tls['cleanup'] == 'removed-and-absence-verified' and tls['hostedWrites'] == 0
assert tls['sourceSha256'] == sha(ROOT/'spikes/stage6-private-staging/verify-tls-local.py')
for name, expected in tls['artifacts'].items():
    assert sha((ROOT/record['tlsResult']).parent/name) == expected
print('PASS: source/raw hashes,233 JVM/19 new,5 archive/6 local TLS+restore,compiled-only instrumentation; hosted UNVERIFIED')
