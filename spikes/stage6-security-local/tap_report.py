"""Strict reporting for the flat node:test suites used by this local runner."""
from pathlib import Path
import hashlib,json,re,sys

FIELDS=('tests','pass','fail','cancelled','skipped')
class TapSummaryError(ValueError):pass

def parse_tap(path,expected=None,require_success=False):
 path=Path(path)
 try:text=path.read_text()
 except (OSError,UnicodeError) as error:raise TapSummaryError(f'{path}: cannot read TAP ({type(error).__name__})') from error
 values={key:re.findall(r'^# '+key+r' (\d+)$',text,re.M) for key in FIELDS}
 missing=[key for key,matches in values.items() if not matches]
 duplicate=[key for key,matches in values.items() if len(matches)>1]
 if missing or duplicate:
  detail=[]
  if missing:detail.append('missing TAP summaries: '+', '.join(missing))
  if duplicate:detail.append('duplicate TAP summaries: '+', '.join(duplicate))
  raise TapSummaryError(f'{path}: '+ '; '.join(detail)+'; test counts are UNVERIFIED (inspect retained raw log)')
 counts={key:int(matches[0]) for key,matches in values.items()}
 if counts['tests']!=sum(counts[k] for k in FIELDS[1:]):raise TapSummaryError(f'{path}: inconsistent TAP totals')
 bodies=re.findall(r'^(?:ok|not ok) \d+ - .+$',text,re.M)
 if len(bodies)!=counts['tests']:raise TapSummaryError(f'{path}: TAP body count differs from summary')
 if expected is not None and counts['tests']!=expected:raise TapSummaryError(f"{path}: expected {expected} tests, found {counts['tests']}")
 if require_success and (any(counts[k] for k in ['fail','cancelled','skipped']) or re.search(r'^not ok|hookFailed|failureType:',text,re.M)):
  raise TapSummaryError(f'{path}: TAP reports failed, cancelled, skipped or setup-failed tests')
 return counts

def report(folder,prior_failed,blocked):
 folder=Path(folder);counts={};errors=[]
 for name,expected in [('focused.tap',27),('unit.tap',32)]:
  try:
   counts[name]=parse_tap(folder/name,expected)
   parse_tap(folder/name,expected,require_success=True)
  except TapSummaryError as error:errors.append(str(error))
 unchanged=False
 try:
  manifest=json.loads((folder/'source-sha256.json').read_text())
  if not isinstance(manifest,dict) or not manifest or any(not isinstance(p,str) or not isinstance(sha,str) or not re.fullmatch('[0-9a-f]{64}',sha) for p,sha in manifest.items()):raise ValueError('invalid source manifest')
  changed=[p for p,sha in manifest.items() if hashlib.sha256(Path(p).read_bytes()).hexdigest()!=sha]
  unchanged=not changed
  if changed:errors.append('source changed during verification: '+', '.join(changed))
 except (OSError,ValueError,TypeError,AttributeError) as error:errors.append('source manifest verification failed: '+type(error).__name__)
 result={'counts':counts,'summaryErrors':errors,'checksOrTestsFailed':bool(prior_failed or errors),'postgresBlocked':blocked,'postgresFreshBodies':0 if blocked else 'see unchanged PostgreSQL runner outputs','sourceUnchanged':unchanged,'acceptance':'PARTIAL; see threat matrix, successful probes do not close all threats'}
 (folder/'result.json').write_text(json.dumps(result,indent=2)+'\n')
 return result

if __name__=='__main__':
 result=report(sys.argv[1],bool(int(sys.argv[2])),sys.argv[3]=='true')
 print(json.dumps(result,indent=2))
 sys.exit(1 if result['checksOrTestsFailed'] else 0)
