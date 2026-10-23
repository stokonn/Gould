"""Regression tests for offline fixture verification, not backend behavior."""
import copy
import tempfile
import unittest
from pathlib import Path
import verify

class VerificationTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.defs = verify.read(verify.BASE / 'dto.schema.json')['$defs']
        cls.cases = {c['id']: c for c in verify.read(verify.BASE / 'fixtures.json')['cases']}

    def test_corpus(self):
        verify.check()

    def test_unknown_schema_keyword_fails_closed(self):
        with self.assertRaises(RuntimeError):
            verify.validate('x', {'type':'string', 'unimplemented':True}, self.defs)

    def test_boolean_is_not_version_one(self):
        with self.assertRaises(ValueError):
            verify.validate({'schemaVersion':True}, self.defs['EmptyRequest'], self.defs)

    def test_utf8_multibyte_boundary(self):
        verify.validate('é'*500, self.defs['Reason'], self.defs)
        with self.assertRaises(ValueError):
            verify.validate('é'*501, self.defs['Reason'], self.defs)

    def test_int8_extremes_remain_strings(self):
        for v in ['9007199254740993', '9223372036854775807']:
            verify.validate(v, self.defs['Revision'], self.defs)
        for v in [9007199254740993, '9223372036854775808', '01', '+1', '1e3']:
            with self.assertRaises(ValueError):
                verify.validate(v, self.defs['Revision'], self.defs)

    def test_invalid_unicode(self):
        with self.assertRaises(ValueError):
            verify.validate('\ud800', self.defs['Reason'], self.defs)

    def test_duplicate_json_keys(self):
        with tempfile.TemporaryDirectory() as directory:
            p = Path(directory) / 'sample.json'
            p.write_text('{"schemaVersion":1,"schemaVersion":2}')
            with self.assertRaises(ValueError):
                verify.read(p)

    def test_nonfinite_json(self):
        with tempfile.TemporaryDirectory() as directory:
            p = Path(directory) / 'sample.json'
            p.write_text('{"n":NaN}')
            with self.assertRaises(ValueError):
                verify.read(p)

    def test_unknown_fields_request_vs_response(self):
        value = copy.deepcopy(self.cases['queue']['value'])
        value['items'][0]['future'] = 'additive'
        verify.validate(value, self.defs['QueuePage'], self.defs, response=True)
        with self.assertRaises(ValueError):
            verify.validate(value, self.defs['QueuePage'], self.defs)

    def test_original_receipt_is_not_new_request(self):
        original = self.cases['approve-result']
        replay = self.cases['approve-replay']
        self.assertEqual(original['value'], replay['value'])
        self.assertEqual(original['httpStatus'], replay['httpStatus'])
        changed = copy.deepcopy(replay['value'])
        changed['requestId'] = '00000000-0000-4000-8000-000000000099'
        self.assertNotEqual(original['value'], changed)

    def test_sequence_not_javascript_number(self):
        events = self.cases['audit-paired-approval']['value']['items']
        self.assertEqual(int(events[1]['seq'])-int(events[0]['seq']), 1)
        self.assertEqual(sum(e['action']=='in_review->approved' for e in events), 1)
        self.assertEqual(len(events), 2)

    def test_invalid_calendar_date(self):
        with self.assertRaises(ValueError):
            verify.validate('2026-02-30T00:00:00.000000Z', self.defs['Timestamp'], self.defs)


class PublisherGuardTests(unittest.TestCase):
    def test_requires_exact_sha_before_commands(self):
        import publish
        from unittest.mock import patch
        with patch.object(publish, 'command') as cmd:
            with self.assertRaisesRegex(RuntimeError, 'exact 40-character'):
                publish.local_guards('HEAD')
            cmd.assert_not_called()

    def test_wrong_branch_stops_before_network(self):
        import publish
        from unittest.mock import patch
        with patch.object(publish, 'command', return_value='main') as cmd:
            with self.assertRaisesRegex(RuntimeError, 'wrong branch'):
                publish.local_guards('1'*40)
            self.assertEqual(cmd.call_count, 1)

    def test_implementation_diff_stops_before_publication(self):
        import publish
        from unittest.mock import patch
        def command(*args):
            if args[1:3] == ('branch','--show-current'):return publish.BRANCH
            if args[1:3] == ('rev-parse','HEAD'):return '1'*40
            if args[1:3] == ('status','--porcelain=v1'):return ''
            if args[1:3] == ('remote','get-url'):return 'https://github.com/stokonn/Goldberg.git'
            if args[1:3] == ('merge-base','--is-ancestor'):return ''
            if args[1:3] == ('diff','--name-only'):return 'app/src/changed.kt'
            raise AssertionError('unexpected command '+str(args))
        with patch.object(publish, 'command', side_effect=command):
            with self.assertRaisesRegex(RuntimeError, 'non-planning change'):
                publish.local_guards('1'*40)

if __name__ == '__main__':
    unittest.main()
