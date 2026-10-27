"""Resume only after the preserved UUID-fixture failure;do not repeat seeded probes."""
import run_probes as probes
from run_probes import *
guard()
assert not (OUT/'background-prepare-valid-uuid.txt').exists(),'Completed recovery run must never be overwritten'
test=ROOT/'app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk'
source=ROOT/'app/src/androidTest/java/com/opus/piano/Stage8IndependentPreservationTest.kt'
assert test.stat().st_mtime_ns>source.stat().st_mtime_ns
assert 'BUILD SUCCESSFUL' in (ART/'test-build-uuid.log').read_text()
probes.BINDING='probe-input-binding-after-uuid.json'
(OUT/probes.BINDING).write_text(json.dumps({'testSourceSha256':hashlib.sha256(source.read_bytes()).hexdigest(),'testApkSha256':hashlib.sha256(test.read_bytes()).hexdigest(),'correction':'Only background fixture IDs changed to valid deterministic UUIDs;completed earlier experiment methods unchanged;initial APK/source retained locally','initialBinding':'probe-input-binding.json'},indent=2)+'\n')
adb('install','-r',str(test))
instrument('prepareOfflineBundleAndPeriodicWorkForReboot',label='background-prepare-valid-uuid')
# Preserve actual WorkManager rows before reboot;no fixture TestDriver advances time.
db=pull_db('androidx.work.workdb','work-before-reboot')
(OUT/'work-before-reboot.json').write_text(json.dumps({'columns':[r[1] for r in db.execute('PRAGMA table_info(WorkSpec)')],'rows':[list(r) for r in db.execute('SELECT id,state,worker_class_name,interval_duration FROM WorkSpec')]},indent=2)+'\n');db.close()
boot=adb('shell','cat','/proc/sys/kernel/random/boot_id').decode().strip();adb('reboot')
for _ in range(180):
    p=subprocess.run(BASE+['shell','getprop','sys.boot_completed'],capture_output=True)
    if p.returncode==0 and p.stdout.strip()==b'1':
        new=adb('shell','cat','/proc/sys/kernel/random/boot_id').decode().strip()
        if new!=boot:break
    time.sleep(.5)
else:raise RuntimeError('Reboot did not complete')
guard();(OUT/'reboot.json').write_text(json.dumps({'beforeBootId':boot,'afterBootId':new,'changed':True,'radiosRemainOffline':True},indent=2)+'\n')
instrument('recoverLivePracticeWithoutCountingDowntime',label='practice-after-reboot',extra={'round':'reboot'})
instrument('verifyOfflineBundleAndPeriodicWorkAfterReboot')
instrument('recoveredPracticeCompletesExactlyOnce')
for phase in ['before_commit','after_commit','after_preferences','before_ack']:
    instrument('prepareActualProcessDeath',label='restore-kill-'+phase,killed=True,extra={'restorePhase':phase},cls='com.opus.piano.Stage3bProcessDeathTest',marker='files/stage3b-process-'+phase+'.marker')
    instrument('verifyAfterActualProcessDeath',label='restore-verify-'+phase,extra={'restorePhase':phase},cls='com.opus.piano.Stage3bProcessDeathTest')
# Critical original application data remains separate from every named probe database.
db=pull_db('opus.db','original-after-probes');before=json.loads((ART/'upgrade/before.json').read_text())
actual={name:[list(r) for r in db.execute('SELECT * FROM '+name+' ORDER BY rowid')] for name in before};db.close()
assert actual==before,'Original rehearsal application data changed during independent probes'
names=adb('shell','run-as','com.opus.piano','ls','files/stage8-independent').decode().splitlines()
for name in names:
    if not name.endswith('.json') or name=='upgraded-export-v5.json':continue
    assert re.fullmatch(r'[a-zA-Z0-9_-]+\.json',name)
    (OUT/('native-'+name)).write_bytes(adb('exec-out','run-as','com.opus.piano','cat','files/stage8-independent/'+name))
rows=[json.loads(line) for line in (OUT/'probe-results.jsonl').read_text().splitlines()]
(OUT/'probe-summary.json').write_text(json.dumps({'junitPassed':sum(r['junitPass'] for r in rows),'expectedProcessTerminations':sum(not r['junitPass'] for r in rows),'failures':0,'originalApplicationTablesUnchanged':True,'actualReboots':1,'realThreeHourDeliveryVerified':False,'results':rows},indent=2)+'\n')
print('Independent probes complete;source-bound raw outcomes retained',flush=True)
