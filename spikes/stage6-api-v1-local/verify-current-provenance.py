"""Independently recompute the complete current inventory and historical checks."""
import json
from current_provenance import (ROOT, CUR, META, HIST, HISTORICAL_SNAPSHOT,
                                historical_integrity, source_files, set_hash, sha,
                                APPROVED_HISTORICAL_DRIFT)


def check():
    meta, hist, checked = historical_integrity()
    manifest = json.loads(CUR.read_text())
    assert manifest['schemaVersion'] == 2
    assert manifest['kind'] == 'current-source-provenance'
    assert manifest['historicalSnapshot'] == HISTORICAL_SNAPSHOT
    assert manifest['historicalSourceBaseline'] == meta['baseline']
    assert manifest['historicalMetadataSha256'] == sha(META)
    assert manifest['historicalProvenanceSha256'] == sha(HIST)
    assert manifest['historicalPinnedFiles'] == checked
    current = source_files()
    assert manifest['files'] == current, 'current source inventory/hash mismatch'
    assert manifest['sourceSetHash'] == set_hash(current)
    drift = sorted(n for n, h in hist['files'].items() if sha(ROOT / n) != h)
    assert manifest['historicalSourceDrift'] == drift
    assert set(drift) <= APPROVED_HISTORICAL_DRIFT, 'unapproved historical source drift'
    print(f'PASS: historical integrity: {len(checked)} pins on original source, frozen artifacts unchanged.')
    print(f'PASS: current-source integrity: {len(current)} files, sourceSetHash={set_hash(current)}')
    print('Historical current-file pin comparisons are not reclassified as PASS.')


if __name__ == '__main__':
    check()
