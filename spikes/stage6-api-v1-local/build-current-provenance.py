"""Build a separate current-source manifest; never regenerate historical pins."""
import json
from current_provenance import (ROOT, CUR, META, HIST, HISTORICAL_SNAPSHOT,
                                historical_integrity, source_files, set_hash, sha)


def main():
    meta, hist, checked = historical_integrity()
    files = source_files()
    manifest = {
        'schemaVersion': 2,
        'kind': 'current-source-provenance',
        'classification': 'local-synthetic-current-source; not API freeze or production acceptance',
        'historicalSnapshot': HISTORICAL_SNAPSHOT,
        'historicalSourceBaseline': meta['baseline'],
        'historicalMetadataSha256': sha(META),
        'historicalProvenanceSha256': sha(HIST),
        'historicalPinnedFiles': checked,
        'historicalSourceDrift': sorted(n for n, h in hist['files'].items() if sha(ROOT / n) != h),
        'files': files,
        'sourceSetHash': set_hash(files),
    }
    CUR.parent.mkdir(parents=True, exist_ok=True)
    CUR.write_text(json.dumps(manifest, indent=2, sort_keys=True) + '\n')
    print(f'PASS: historical pins ({len(checked)}) on recorded source; frozen artifacts unchanged.')
    print(f'PASS: current manifest: {len(files)} files; sourceSetHash={manifest["sourceSetHash"]}')


if __name__ == '__main__':
    main()
