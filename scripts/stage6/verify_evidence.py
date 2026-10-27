"""Validate the committed Stage6 local acceptance record; never reruns hosted tests."""
from pathlib import Path
import hashlib
import json
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[2]
BASE = ROOT / 'docs/planning/goldberg-0.11/stage-6/results/end-to-end-2026-10-25/android-json-final'
record = json.loads((BASE/'verification.json').read_text())
sha = lambda p: hashlib.sha256(p.read_bytes()).hexdigest()
# Preserve the original source acceptance while binding two reviewed tooling-only
# changes separately. Every other original source/raw pin remains exact.
EVOLUTION = ROOT / 'docs/planning/goldberg-0.11/stage-6/results/final-source-evolution'
evolution = json.loads((EVOLUTION/'receipt.json').read_text())
assert set(evolution) == {'.gitattributes', 'scripts/stage6/verify_evidence.py'}
INTEGRATION = ROOT / 'docs/planning/goldberg-0.11/stage-6/results/integration-tooling-evolution'
integration = json.loads((INTEGRATION/'receipt.json').read_text())
assert sha(EVOLUTION/'receipt.json') == integration['previousReceiptSha256']
for name, entry in integration['sources'].items():
    assert name in evolution
    assert sha(INTEGRATION/entry['previousFile']) == evolution[name]['currentSha256']
    assert sha(ROOT/name) == entry['currentSha256'], 'Reviewed integration tooling changed: ' + name
for name, expected in record['sourceSha256'].items():
    if name in evolution:
        entry = evolution[name]
        assert sha(EVOLUTION/entry['historicalFile']) == expected, 'Historical source changed: ' + name
        assert name in integration['sources'], 'Unreviewed tooling evolution: ' + name
    else:
        assert sha(ROOT/name) == expected, 'Source differs from verified acceptance: ' + name
# The attribute delta can exempt only the new byte-exact raw evidence paths.
original = (EVOLUTION/'gitattributes.original').read_bytes() + b"\n# Preserve byte-exact BFF raw logs and TAP failure evidence.\nservices/community-bff/results/*/*.log -whitespace\nservices/community-bff/results/*/*.tap -whitespace\n"
assert (INTEGRATION/'gitattributes.preintegration').read_bytes() == original
for filename in ['gitattributes.stage8','gitattributes.additions']:
    assert sha(INTEGRATION/filename) == integration['attributeInputs'][filename]
    for line in (INTEGRATION/filename).read_text().splitlines():
        preserved_crlf = filename == 'gitattributes.stage8' and '.xml' in line and line.endswith(' -text whitespace=blank-at-eol,blank-at-eof,space-before-tab,cr-at-eol')
        assert not line.strip() or line.startswith('#') or line.endswith(' -whitespace') or preserved_crlf, 'Only raw whitespace rules may evolve'
assert (ROOT/'.gitattributes').read_bytes() == original + b'\n' + (INTEGRATION/'gitattributes.stage8').read_bytes() + (INTEGRATION/'gitattributes.additions').read_bytes()
for name, expected in record['artifacts'].items():
    assert sha(BASE/name) == expected, 'Raw evidence changed: ' + name
suites = sorted(BASE.glob('TEST-*.xml'))
totals = {key: 0 for key in ['tests', 'failures', 'errors', 'skipped']}
for suite in suites:
    result = ET.parse(suite).getroot()
    for key in totals:
        totals[key] += int(result.attrib[key])
assert totals == record['jvm'] and len(suites) == record['jvmSuites']
assert totals['tests'] == 234 and totals['failures'] == totals['errors'] == totals['skipped'] == 0
assert record['newJvmTests'] == 20 and record['hostedWrites'] == 0
assert 'BUILD SUCCESSFUL' in (BASE/'host-gradle.log').read_text()
assert 'Ran 6 tests' in (BASE/'backup-store.log').read_text()
assert (BASE/'backup-store.log').read_text().strip().endswith('OK')
tls = json.loads((ROOT/record['tlsResult']).read_text())
assert tls['status'] == 'PASS' and len(tls['checks']) == 6
assert tls['cleanup'] == 'removed-and-absence-verified' and tls['hostedWrites'] == 0
assert tls['sourceSha256'] == sha(ROOT/'spikes/stage6-private-staging/verify-tls-local.py')
for name, expected in tls['artifacts'].items():
    assert sha((ROOT/record['tlsResult']).parent/name) == expected
print('PASS: source/raw hashes,234 JVM/20 new,6 archive/6 local TLS+restore,compiled-only instrumentation; hosted UNVERIFIED')
