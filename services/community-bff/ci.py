"""Same test bodies as the disposable local runner, CI service DB only."""
from pathlib import Path
import json,os,subprocess,sys
ROOT=Path(__file__).resolve().parents[2]
sys.path.insert(0,str(ROOT/'spikes/stage6-api-v1-local'))
from current_provenance import tap_suites
sys.path.insert(0,str(ROOT/'spikes/stage6-security-local'))
from tap_report import parse_tap
assert os.environ.get('GOLDBERG_DISPOSABLE_CI')=='true','explicit disposable CI required'
out=ROOT/'.gradle/community-bff-ci';out.mkdir(parents=True,exist_ok=True)
suites={'fetch.tap':['services/community-bff/tests/fetch.test.mjs','services/community-bff/tests/identity.test.mjs'],'bff-postgres.tap':['services/community-bff/tests/postgres.test.mjs'],'workerd-postgres.tap':['services/community-bff/tests/runtime.test.mjs'],**tap_suites()}
counts={}
for log,files in suites.items():
    p=subprocess.run(['node','--test','--test-reporter=tap',*files],cwd=ROOT,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True)
    path=out/log;path.write_text(p.stdout)
    if p.returncode:print(p.stdout);sys.exit(p.returncode)
    counts[log]=parse_tap(path,require_success=True)
    print(log,json.dumps(counts[log]))
(out/'counts.json').write_text(json.dumps(counts,indent=2)+'\n')
