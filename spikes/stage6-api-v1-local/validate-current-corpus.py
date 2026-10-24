"""Current-source design-corpus gate for the full-int8 acceptance run.

The frozen decision-proposal package (test_*.py -> verify.check()) is historical
evidence pinned to pre-full-int8 handler bytes and is preserved byte-for-byte and
strict; it intentionally rejects the evolved source, so it is NOT a current-acceptance
gate. This gate reuses the frozen package's actual schema artifact (dto.schema.json)
and its validate() implementation unchanged to prove the current-source corpus still
conforms to original fixture expectations, including negative cases, int8 extremes-as-strings, replay/audit accounting, and design
approval. No historical pin is consulted or relaxed here; historical integrity is
proven separately by verify-current-provenance.py.

No network, PostgreSQL, Android, emulator, production or release action. Local-only.
"""
from pathlib import Path
import copy, importlib.util, json, sys

ROOT = Path(__file__).resolve().parents[2]
DP = ROOT / 'contracts/admin/v1/decision-proposal'


def load_verify():
    spec = importlib.util.spec_from_file_location('frozen_verify', DP / 'verify.py')
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def run() -> None:
    verify = load_verify()
    definitions = verify.read(DP / 'dto.schema.json')['$defs']
    corpus = verify.read(DP / 'fixtures.json')
    cases = {c['id']: c for c in corpus['cases']}

    # Reuse the frozen validator's exact positive/negative expectation semantics.
    # This is the 61-case authored proposal, not the separate 41-case capture corpus.
    meta = verify.read(DP / 'metadata.json')
    assert len(cases) == len(corpus['cases']) == sum(meta['fixtureCounts'].values())
    assert corpus['classification'] == 'proposal-unapproved'
    assert all(corpus[k] is False for k in ['active', 'frozen', 'httpApproved'])
    counts = {'positive': 0, 'negative': 0}
    for case in corpus['cases']:
        assert case['classification'] == 'proposal-unapproved'
        assert case['synthetic'] is True and case['active'] is False
        response = case.get('mode', 'response' if case['httpStatus'] is not None else 'request') == 'response'
        valid = True
        try:
            if case['dto'] == 'EmptyBody':
                if case['value'] is not None or case['httpStatus'] != 204:
                    raise ValueError('204 must be bodyless')
            else:
                verify.validate(case['value'], definitions[case['dto']], definitions, response)
        except ValueError:
            valid = False
        assert valid == case['valid'], 'unexpected fixture acceptance: ' + case['id']
        counts['positive' if valid else 'negative'] += 1
        if 'replayOf' in case:
            original = cases[case['replayOf']]
            assert case['value'] == original['value'] and case['httpStatus'] == original['httpStatus']
    assert counts == meta['fixtureCounts']

    # 3) Int8 extremes remain lossless strings (full-int8 boundary preserved).
    for v in ['9007199254740993', '9223372036854775807']:
        verify.validate(v, definitions['Revision'], definitions)
    for v in [9007199254740993, '9223372036854775808', '01', '+1', '1e3']:
        try:
            verify.validate(v, definitions['Revision'], definitions)
        except ValueError:
            pass
        else:
            raise AssertionError(f'int8 non-lossless value accepted: {v!r}')

    # 4) Replay/audit accounting: original receipt is not a new request; seq contiguous.
    original, replay = cases['approve-result'], cases['approve-replay']
    assert original['value'] == replay['value'] and original['httpStatus'] == replay['httpStatus']
    events = cases['audit-paired-approval']['value']['items']
    assert int(events[1]['seq']) - int(events[0]['seq']) == 1
    assert sum(e['action'] == 'in_review->approved' for e in events) == 1 and len(events) == 2

    # 5) Approved design only; policies default-deny and integration stays blocked.
    approval = verify.read(DP / 'design-approval-2026-10-23/approval.json')
    fixtures = verify.read(DP / 'design-approval-2026-10-23/fixtures.json')
    verify.check_design_approval(approval, copy.deepcopy(fixtures))

    print(f'PASS: current-source DTO/schema fixtures: {len(cases)} original proposal cases; '
          f'{counts["positive"]} accepted / {counts["negative"]} intentionally rejected; '
          'original classification and expectations preserved; int8/replay/audit checks PASS. '
          'Offline schema evidence only; actual HTTP/PG captures verified separately.')


if __name__ == '__main__':
    if len(sys.argv) != 1:
        raise SystemExit('usage: validate-current-corpus.py')
    run()
