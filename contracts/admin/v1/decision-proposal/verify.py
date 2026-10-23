"""Offline structural/protocol fixture checks; no HTTP adapter or production validator.

Implements only the documented JSON Schema keyword subset used by dto.schema.json,
plus x-* byte/int8/plain-text constraints. Fails on unknown schema keywords.
Real policy, signatures, auth, SQL and transaction acceptance require future tests.
"""
from pathlib import Path
import base64, binascii, datetime, hashlib, json, re, unicodedata
from zoneinfo import ZoneInfo
ROOT = Path(__file__).resolve().parents[4]
BASE = Path(__file__).resolve().parent
KEYWORDS = {'$ref','type','properties','required','additionalProperties','items',
            'maxItems','minItems','const','enum','anyOf','pattern','format','minLength',
            'maxLength','x-base64url','x-int8-min','x-int8-max','x-max-utf8-bytes','x-plain-text'}


def read(path):
    def pairs(items):
        out = {}
        for k, v in items:
            if k in out:
                raise ValueError('duplicate JSON key: ' + k)
            out[k] = v
        return out
    return json.loads(path.read_text(), object_pairs_hook=pairs,
                      parse_constant=lambda _: (_ for _ in ()).throw(ValueError('nonfinite JSON')))


def validate(value, schema, definitions, response=False):
    if set(schema) - KEYWORDS:
        raise RuntimeError('unsupported schema keyword: ' + str(set(schema) - KEYWORDS))
    if '$ref' in schema:
        return validate(value, definitions[schema['$ref'].removeprefix('#/$defs/')], definitions, response)
    if 'anyOf' in schema:
        for option in schema['anyOf']:
            try:
                validate(value, option, definitions, response)
                return
            except ValueError:
                pass
        raise ValueError('no union member matched')
    if 'const' in schema and (type(value) is not type(schema['const']) or value != schema['const']):
        raise ValueError('constant mismatch')
    if 'enum' in schema and value not in schema['enum']:
        raise ValueError('enum mismatch')
    kind = schema.get('type')
    if kind == 'null' and value is not None:
        raise ValueError('expected null')
    if kind == 'string':
        if not isinstance(value, str):
            raise ValueError('expected string')
        try:
            encoded = value.encode('utf-8', errors='strict')
        except UnicodeError as exc:
            raise ValueError('invalid Unicode') from exc
        if not schema.get('minLength', 0) <= len(value) <= schema.get('maxLength', 2**30):
            raise ValueError('string length')
        if len(encoded) > schema.get('x-max-utf8-bytes', 2**30):
            raise ValueError('UTF-8 byte limit')
        if 'pattern' in schema and re.fullmatch(schema['pattern'], value, flags=re.ASCII) is None:
            raise ValueError('pattern mismatch')
        if schema.get('format') == 'date-time':
            try:
                datetime.datetime.fromisoformat(value.replace('Z', '+00:00'))
            except ValueError as exc:
                raise ValueError('invalid calendar timestamp') from exc
        if 'x-int8-min' in schema and not int(schema['x-int8-min']) <= int(value) <= int(schema['x-int8-max']):
            raise ValueError('int8 range')
        if schema.get('x-base64url'):
            try:
                raw = base64.b64decode(value + '=' * (-len(value) % 4), altchars=b'-_', validate=True)
                if base64.urlsafe_b64encode(raw).decode().rstrip('=') != value:
                    raise ValueError('noncanonical base64url')
            except (ValueError, binascii.Error) as exc:
                raise ValueError('invalid base64url cursor representation') from exc
        if schema.get('x-plain-text') and (not value.strip() or re.search(
                r'[<>\x00-\x1f\x7f]|&#|&lt;|&gt;|javascript:|data:|vbscript:', value, re.I)):
            raise ValueError('unsafe plain text')
    if kind == 'object':
        if not isinstance(value, dict):
            raise ValueError('expected object')
        if set(schema['required']) - value.keys():
            raise ValueError('missing required member')
        extra = value.keys() - schema['properties'].keys()
        if not response and extra and schema.get('additionalProperties') is False:
            raise ValueError('unknown request member')
        for key in value.keys() & schema['properties'].keys():
            validate(value[key], schema['properties'][key], definitions, response)
    if kind == 'array':
        if not isinstance(value, list):
            raise ValueError('expected array')
        if not schema.get('minItems', 0) <= len(value) <= schema.get('maxItems', 2**30):
            raise ValueError('array size')
        for item in value:
            validate(item, schema['items'], definitions, response)



def check_design_approval(approval, fixtures):
    """Validate decision/fixture consistency only; never implement a server query."""
    assert approval['proposalBaseline'] == 'fefa6e83c2a7b9c4c12660ff1542ab086356de88'
    assert approval['status'] == 'APPROVED_DESIGN_WITH_QUALIFICATIONS'
    assert all(approval[k] is False for k in ['implementationAuthorized','httpActive',
               'productionAuthenticationActive','productionPolicyApproved','apiFrozen'])
    decisions = approval['decisions']
    assert set(decisions) == {f'O{i}' for i in range(1,9)}
    assert [decisions['O2'][k] for k in ['accessSeconds','familyAbsoluteSeconds',
             'challengeSeconds','providerClockSkewMaxSeconds']] == [900,604800,300,60]
    assert decisions['O3']['receiptRetentionSeconds'] == 86400
    assert decisions['O4']['cursorTtlSeconds'] == 900
    assert decisions['O4']['v1CompatibilityConfirmed'] is False
    assert decisions['O5']['timeZone'] == fixtures['timeZone'] == 'Europe/Prague'
    assert decisions['O6']['normalization'] == 'NFC'
    assert decisions['O6']['caseSensitive'] is False and decisions['O6']['diacriticSensitive'] is True
    assert decisions['O6']['finalQueryBudgets'] == 'PENDING_LATER_LOCAL_MEASUREMENT'
    assert decisions['O7']['status'] == 'PARTIALLY_APPROVED_DESIGN'
    assert decisions['O7']['reasonMaxUtf8Bytes'] == 1000
    assert decisions['O7']['productionPolicies'] == dict.fromkeys(
        ['sources','fields','licenses','attribution','evidence'], [])
    assert approval['gates'] == {'G10':'CLOSED','G1-G9/G11':'INTEGRATION_BLOCKED',
                                '6.12':'PARTIAL; T22 LOCAL PASS','6.17':'NOT FROZEN'}
    assert fixtures['classification'] == 'approved-design-synthetic-fixtures'
    assert all(fixtures[k] is False for k in ['active','httpExecuted','sqlExecuted'])
    assert len(fixtures['windows']) == 4 and len(fixtures['searchCases']) == 10
    zone = ZoneInfo('Europe/Prague')
    def instant(value):
        return value.astimezone(datetime.timezone.utc).isoformat(timespec='microseconds').replace('+00:00','Z')
    for window in fixtures['windows']:
        day = datetime.date.fromisoformat(window['civilDate'])
        start = datetime.datetime.combine(day, datetime.time(), zone)
        end = datetime.datetime.combine(day + datetime.timedelta(days=1), datetime.time(), zone)
        assert instant(start) == window['startInclusive'], window['id']
        assert instant(end) == window['endExclusive'], window['id']
        duration = (end.astimezone(datetime.timezone.utc) - start.astimezone(datetime.timezone.utc)).total_seconds()
        assert duration == window['elapsedHours'] * 3600, window['id']
    for case in fixtures['searchCases']:
        text = unicodedata.normalize('NFC', case['text']).casefold()
        query = unicodedata.normalize('NFC', case['query']).casefold()
        assert (query in text) == case['matches'], case['id']
    print('PASS: owner O1–O8 scope, Prague4 civil/DST windows and10 literal CZ/EN search fixtures; '
          'host fixture checks only, not server/runtime acceptance.')


def check():
    metadata = read(BASE / 'metadata.json')
    corpus = read(BASE / 'fixtures.json')
    definitions = read(BASE / 'dto.schema.json')['$defs']
    assert metadata['baseline'] == '19f5642192148051a7b4eb5bc5efb1090192e3c0'
    assert corpus['classification'] == metadata['classification'] == 'proposal-unapproved'
    assert all(corpus[k] is False for k in ['active', 'frozen', 'httpApproved'])
    assert metadata['ownerApproved'] is True and metadata['approvalScope'] == 'DESIGN_ONLY_WITH_QUALIFICATIONS'
    assert metadata['productionPolicyApproved'] is False and metadata['g10'] == 'CLOSED'
    assert all(metadata[k] is False for k in ['active', 'frozen', 'httpApproved'])
    check_design_approval(read(BASE / 'design-approval-2026-10-23/approval.json'),
                          read(BASE / 'design-approval-2026-10-23/fixtures.json'))
    for name, digest in metadata['files'].items():
        assert hashlib.sha256((ROOT / name).read_bytes()).hexdigest() == digest, name
    for name, digest in metadata['baselineFiles'].items():
        assert hashlib.sha256((ROOT / name).read_bytes()).hexdigest() == digest, 'immutable evidence changed: ' + name
    cases = corpus['cases']
    by_id = {c['id']: c for c in cases}
    assert len(by_id) == len(cases)
    counts = {'positive': 0, 'negative': 0}
    for case in cases:
        assert case['classification'] == 'proposal-unapproved'
        assert case['synthetic'] is True and case['active'] is False
        response = case.get('mode', 'response' if case['httpStatus'] is not None else 'request') == 'response'
        valid = True
        try:
            if case['dto'] == 'EmptyBody':
                if case['value'] is not None or case['httpStatus'] != 204:
                    raise ValueError('204 must be bodyless')
            else:
                validate(case['value'], definitions[case['dto']], definitions, response)
        except ValueError:
            valid = False
        assert valid == case['valid'], 'unexpected fixture acceptance: ' + case['id']
        counts['positive' if valid else 'negative'] += 1
        if 'replayOf' in case:
            original = by_id[case['replayOf']]
            assert case['value'] == original['value'] and case['httpStatus'] == original['httpStatus']
    assert counts == metadata['fixtureCounts']
    assert by_id['open-request']['value']['expectedEntityRevision'] == '7'
    assert by_id['open-request']['value']['expectedSubmissionRevision'] == '3'
    assert by_id['approve-result']['value']['publishedEntityRevision'] == '8'
    events = by_id['audit-paired-approval']['value']['items']
    assert [e['action'] for e in events] == ['in_review->approved', 'approved->published']
    assert sum(e['action'] == 'in_review->approved' for e in events) == 1
    assert [int(e['seq']) for e in events] == [9007199254740992, 9007199254740993]
    status_codes = {400:'validation_failed',401:'unauthenticated',403:'forbidden',404:'not_found',
                    409:'conflict',422:'idempotency_mismatch',429:'rate_limited',503:'service_unavailable',500:'internal'}
    for status, code in status_codes.items():
        assert by_id['error-' + str(status)]['value']['error']['code'] == code
    protocol = read(BASE / 'protocol.json')
    assert protocol['executed'] is False and len(protocol['cases']) == 8
    for scene in protocol['cases']:
        assert scene['classification'] == 'proposal-unapproved' and scene['active'] is False
        validate(scene['request'], definitions['ApproveRequest'], definitions)
        if scene.get('expectedErrorFixture'):
            assert by_id[scene['expectedErrorFixture']]['httpStatus'] == scene['expectedHttpStatus']
        if 'storedRequest' in scene:
            assert scene['request']['idempotencyKey'] == scene['storedRequest']['idempotencyKey']
            assert scene['effects'] == 0
            assert (scene['request'] != scene['storedRequest']) == (scene['expectedHttpStatus'] == 422)
    document = (ROOT / metadata['document']).read_text()
    assert all('G' + str(i) in document for i in range(1,12))
    assert all('O' + str(i) in document for i in range(1,9))
    assert 'APPROVED DESIGN / UNIMPLEMENTED / INACTIVE / NOT FROZEN' in document
    assert 'Europe/Prague' in document and '6.17 NOT FROZEN' in document
    assert len(metadata['canonicalLocalActions']) == len(set(metadata['canonicalLocalActions']))
    print(f'PASS: {len(cases)} proposed synthetic fixtures ({counts["positive"]} positive / {counts["negative"]} negative); '
          '8 authored protocol scenes; DTO structure/bytes/int8/replay/audit accounting; hashes; approved design only, policies default-deny and integration blocked. No HTTP/PG/Android execution.')
    return counts

if __name__ == '__main__':
    check()
