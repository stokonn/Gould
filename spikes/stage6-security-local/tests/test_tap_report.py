import hashlib,importlib.util,json,subprocess,sys,tempfile,unittest
from pathlib import Path
MODULE=Path(__file__).resolve().parents[1]/'tap_report.py'
spec=importlib.util.spec_from_file_location('tap_report',MODULE);tap=importlib.util.module_from_spec(spec);spec.loader.exec_module(tap)

def valid(n=1):
 return 'TAP version 13\n'+''.join(f'ok {i} - synthetic assertion {i}\n' for i in range(1,n+1))+f'1..{n}\n# tests {n}\n# pass {n}\n# fail 0\n# cancelled 0\n# skipped 0\n'

class TapReportTests(unittest.TestCase):
 def setUp(self):
  self.tmp=tempfile.TemporaryDirectory();self.addCleanup(self.tmp.cleanup);self.root=Path(self.tmp.name);self.path=self.root/'input.tap'
 def test_valid_exact_summaries_and_bodies(self):
  self.path.write_text(valid(2));self.assertEqual(tap.parse_tap(self.path,2,True),dict(tests=2,**{'pass':2},fail=0,cancelled=0,skipped=0))
 def test_actual_retained_dependency_error_is_clear_not_none_traceback(self):
  raw=Path(__file__).resolve().parents[3]/'docs/planning/goldberg-0.11/stage-6/results/security-local/run-csaqQFXV/focused.tap'
  with self.assertRaisesRegex(tap.TapSummaryError,'missing TAP summaries: tests, pass, fail, cancelled, skipped') as caught:tap.parse_tap(raw)
  self.assertIn('UNVERIFIED',str(caught.exception));self.assertNotIn('NoneType',str(caught.exception))
 def test_truncated_summary_identifies_missing_field(self):
  self.path.write_text(valid().replace('# skipped 0\n',''))
  with self.assertRaisesRegex(tap.TapSummaryError,'missing TAP summaries: skipped'):tap.parse_tap(self.path)
 def test_duplicate_summary_refused(self):
  self.path.write_text(valid()+'# pass 1\n')
  with self.assertRaisesRegex(tap.TapSummaryError,'duplicate TAP summaries: pass'):tap.parse_tap(self.path)
 def test_missing_file_is_named(self):
  with self.assertRaisesRegex(tap.TapSummaryError,'input.tap: cannot read TAP'):tap.parse_tap(self.path)
 def test_zero_or_inconsistent_or_unexecuted_tests_never_pass_expected_suite(self):
  for text,message in [(valid(0),'expected 27'),(valid().replace('# pass 1','# pass 0'),'inconsistent TAP totals'),(valid().replace('ok 1 - synthetic assertion 1\n',''),'body count differs')]:
   with self.subTest(message=message):
    self.path.write_text(text)
    with self.assertRaisesRegex(tap.TapSummaryError,message):tap.parse_tap(self.path,27,True)
 def test_failed_cancelled_skipped_results_never_pass_success_gate(self):
  for key in ['fail','cancelled','skipped']:
   with self.subTest(key=key):
    text=valid().replace('# pass 1','# pass 0').replace(f'# {key} 0',f'# {key} 1')
    if key=='fail':text=text.replace('ok 1 -','not ok 1 -')
    self.path.write_text(text)
    with self.assertRaisesRegex(tap.TapSummaryError,'TAP reports'):tap.parse_tap(self.path,1,True)
 def test_cli_writes_failure_json_with_unverified_counts_and_no_traceback(self):
  (self.root/'focused.tap').write_text('Error: missing synthetic dependency\n');(self.root/'unit.tap').write_text(valid(32));(self.root/'source-sha256.json').write_text('{}')
  result=subprocess.run([sys.executable,str(MODULE),str(self.root),'1','false'],capture_output=True,text=True)
  report=json.loads((self.root/'result.json').read_text());self.assertEqual(result.returncode,1);self.assertTrue(report['checksOrTestsFailed']);self.assertNotIn('focused.tap',report['counts']);self.assertTrue(report['summaryErrors']);self.assertNotIn('Traceback',result.stdout+result.stderr)
 def test_false_green_summary_with_not_ok_is_failed_and_counts_retained(self):
  (self.root/'focused.tap').write_text(valid(27).replace('ok 1 -','not ok 1 -'));(self.root/'unit.tap').write_text(valid(32));(self.root/'source-sha256.json').write_text('{}')
  result=tap.report(self.root,False,False);self.assertTrue(result['checksOrTestsFailed']);self.assertEqual(result['counts']['focused.tap']['tests'],27);self.assertTrue(any('TAP reports' in e for e in result['summaryErrors']))
 def test_source_change_is_explicit_failed_result(self):
  (self.root/'focused.tap').write_text(valid(27));(self.root/'unit.tap').write_text(valid(32));source=self.root/'source';source.write_text('synthetic original');h=hashlib.sha256(source.read_bytes()).hexdigest();(self.root/'source-sha256.json').write_text(json.dumps({str(source):h}));source.write_text('changed')
  result=tap.report(self.root,False,True);self.assertFalse(result['sourceUnchanged']);self.assertTrue(result['checksOrTestsFailed']);self.assertIn('source changed',result['summaryErrors'][0])

if __name__=='__main__':unittest.main(verbosity=2)
