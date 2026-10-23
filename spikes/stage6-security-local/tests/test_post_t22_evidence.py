import importlib.util,shutil,sys,tempfile,unittest
from pathlib import Path
MODULE=Path(__file__).resolve().parents[1]/'post_t22_evidence.py';sys.path.insert(0,str(MODULE.parent))
spec=importlib.util.spec_from_file_location('post_t22_evidence',MODULE);evidence=importlib.util.module_from_spec(spec);spec.loader.exec_module(evidence)
ROOT=MODULE.parents[2]
class PostT22EvidenceTests(unittest.TestCase):
 def copied(self):
  tmp=tempfile.TemporaryDirectory();self.addCleanup(tmp.cleanup);base=Path(tmp.name)
  w=base/'wrapper';n=base/'nested';shutil.copytree(ROOT/evidence.WRAPPER,w);shutil.copytree(ROOT/evidence.NESTED,n);return w,n
 def test_real_run_counts_source_identity_and_cleanup(self):
  r=evidence.verify(ROOT);self.assertEqual((r['successfulExecutions'],r['testsCountingRepeated32SuiteOnce'],r['databaseBackedBodies']),(345,313,205));self.assertEqual(r['nestedExit'],0);self.assertEqual(r['sourceHashes'],82)
 def test_nested_failure_not_hidden_by_successful_outer_flags(self):
  w,n=self.copied();p=n/'auth-regression.tap';p.write_text(p.read_text().replace('# fail 0','# fail 1'))
  with self.assertRaises(ValueError):evidence.read_runs(w,n)
 def test_unexecuted_body_not_hidden_by_matching_summary(self):
  w,n=self.copied();p=n/'auth-regression.tap';p.write_text(p.read_text().replace('ok 1 - Auth extension upgrades seeded base sessions and repeats without altering original migration checksums\n',''))
  with self.assertRaisesRegex(ValueError,'body count differs'):evidence.read_runs(w,n)
 def test_nonzero_nested_exit_refused(self):
  w,n=self.copied();(w/'postgres-runner.exit').write_text('1\n')
  with self.assertRaisesRegex(ValueError,'nonzero'):evidence.read_runs(w,n)
 def test_wrong_nested_run_stdout_link_refused(self):
  w,n=self.copied();p=w/'postgres-runner.log';p.write_text(p.read_text()+'different run\n')
  with self.assertRaisesRegex(ValueError,'linkage'):evidence.read_runs(w,n)
 def test_missing_removal_output_refused(self):
  w,n=self.copied();(n/'cleanup.log').write_text('')
  with self.assertRaisesRegex(ValueError,'removal'):evidence.read_runs(w,n)
if __name__=='__main__':unittest.main(verbosity=2)
