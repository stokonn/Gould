#!/usr/bin/env python3
"""Validate the synthetic comparison corpus and immutable source provenance.
No backend execution, network traffic or approval/mapping activation.
"""
import hashlib
import json
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / 'contracts/admin/v1'


def check():
    corpus = json.loads((BASE / 'fixtures.json').read_text())
    proposal = json.loads((BASE / 'mapping-proposal.json').read_text())
    provenance = json.loads((BASE / 'source-provenance.json').read_text())
    assert corpus['schemaVersion'] == 1 and corpus['synthetic'] is True
    assert corpus['candidate']['commit'] == provenance['commit']
    cases = corpus['cases']
    indexed = {case['id']: case for case in cases}
    assert len(indexed) == len(cases) == 29
    assert {case['classification'] for case in cases} == {
        'normative-design', 'legacy-c6-reference', 'candidate-local', 'client-condition'}
    assert proposal['classification'] == 'proposal-unapproved'
    assert proposal['active'] is False and proposal['requiresApproval'] is True
    assert indexed['candidate-detail']['response']['body']['submission']['author_id'] != indexed['candidate-session']['response']['body']['principalId']
    assert indexed['candidate-open']['requestBody']['expectedSubmissionRevision'] == 1
    original = indexed['candidate-approved']
    replay = indexed['candidate-idempotent-replay']
    assert original['requestBody'] == replay['requestBody']
    assert original['response'] == replay['response']
    assert replay['replayOf'] == original['id']
    expected_entity = original['requestBody']['expectedRevision']
    expected_submission = original['requestBody']['expectedSubmissionRevision']
    published = original['response']['body']['revision']
    assert (expected_entity, expected_submission, published) == (7, 3, 8)
    assert len({expected_entity, expected_submission, published}) == 3
    for name in ['candidate-stale-entity', 'candidate-stale-submission', 'candidate-not-open']:
        assert indexed[name]['response']['status'] == 409
        assert 'details' not in indexed[name]['response']['body']['error']
    for name in ['candidate-reject', 'candidate-request_changes']:
        assert indexed[name]['response']['status'] == 200
        assert 'revision' not in indexed[name]['response']['body']
    audit = indexed['candidate-audit']['response']['body']
    assert audit['items'][0]['action'] == 'approved->published'
    assert isinstance(audit['items'][0]['seq'], str)
    assert int(audit['items'][0]['seq']) > 2**53
    assert int(audit['head']['last_seq']) == 2**63-1
    assert isinstance(indexed['candidate-queue']['response']['body']['nextCursor'], dict)
    # Bind claims to committed sources; no checkout, reset or other worktree mutation.
    for name, digest in provenance['files'].items():
        data = subprocess.check_output(['git', '-C', str(ROOT), 'show', provenance['commit'] + ':' + name])
        assert hashlib.sha256(data).hexdigest() == digest, name
    print(f'PASS: {len(cases)} authored admin fixtures, distinct contract authorities, inactive mapping, replay/dual-revision/numeric cases and {len(provenance["files"])} pinned source hashes; no backend execution')


if __name__ == '__main__':
    check()
