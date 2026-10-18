#!/usr/bin/env python3
"""Actual emulator process termination/restart, explicitly distinguished from passed tests."""
import argparse,hashlib,json,re,subprocess
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--serial',required=True);p.add_argument('--output',type=Path,required=True);a=p.parse_args()
assert re.fullmatch(r'emulator-\d+',a.serial)
adb=['/home/stokonn/Android/Sdk/platform-tools/adb','-s',a.serial]
def run(*args):return subprocess.run(adb+list(args),text=True,capture_output=True,timeout=90)
assert run('shell','getprop','ro.kernel.qemu').stdout.strip()=='1'
result={'serial':a.serial,'api':run('shell','getprop','ro.build.version.sdk').stdout.strip(),'cases':[]}
a.output.parent.mkdir(parents=True,exist_ok=True)
for phase in ['before_commit','after_commit','after_preferences','before_ack']:
 common=['shell','am','instrument','-w','-r','-e','restorePhase',phase,'-e','class']
 first=run(*common,'com.opus.piano.Stage3bProcessDeathTest#prepareActualProcessDeath','com.opus.piano.test/androidx.test.runner.AndroidJUnitRunner')
 log=a.output.parent/(a.output.stem+'-'+phase+'-terminated.log');log.write_text(first.stdout+first.stderr)
 assert 'OK (1 test)' not in first.stdout and 'shortMsg=Process crashed.' in first.stdout,'Prepare must actually terminate, not just fail a test'
 marker=run('exec-out','run-as','com.opus.piano','cat',f'files/stage3b-process-{phase}.marker')
 assert marker.returncode==0 and marker.stdout==phase,(phase,first.stdout,first.stderr)
 second=run(*common,'com.opus.piano.Stage3bProcessDeathTest#verifyAfterActualProcessDeath','com.opus.piano.test/androidx.test.runner.AndroidJUnitRunner')
 (a.output.parent/(a.output.stem+'-'+phase+'-verified.log')).write_text(second.stdout+second.stderr)
 assert 'OK (1 test)' in second.stdout and 'FAILURES' not in second.stdout,(phase,second.stdout,second.stderr)
 result['cases'].append({'phase':phase,'actualTermination':'expected, not a passed test','restartVerification':'passed','terminatedLogSha256':hashlib.sha256(log.read_bytes()).hexdigest()})
 print('PASS actual process death/restart:',phase,flush=True)
a.output.write_text(json.dumps(result,indent=2)+'\n')
