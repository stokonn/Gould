"""Run explicit opt-in synthetic probes on the owned offline emulator;never uninstall."""
from pathlib import Path
import subprocess,json,re,time,hashlib,sqlite3,os

ROOT=Path(__file__).resolve().parents[3]
ART=Path(os.environ.get('GOLDBERG_STAGE8_ARTIFACTS',str(ROOT/'.gradle/stage8-independent-artifacts')))
OUT=Path(os.environ.get('GOLDBERG_STAGE8_OUTPUT',str(ROOT/'docs/planning/goldberg-0.11/stage-8/results/independent-android-2026-10-25')))
ADB=str(Path(os.environ.get('ANDROID_HOME') or os.environ.get('ANDROID_SDK_ROOT') or Path.home() / 'Android/Sdk') / 'platform-tools/adb');SERIAL=os.environ.get('GOLDBERG_STAGE8_SERIAL','emulator-5592');RUN=os.environ.get('GOLDBERG_STAGE8_RUN','oct25r1')
assert re.fullmatch(r'emulator-\d+',SERIAL),'Only explicit emulator serials are allowed'
assert re.fullmatch(r'[a-zA-Z0-9]+',RUN)
CLASS='com.opus.piano.Stage8IndependentPreservationTest'
BINDING='probe-input-binding.json'
BASE=[ADB,'-s',SERIAL]

def adb(*args,check=True):
    p=subprocess.run(BASE+list(args),capture_output=True,timeout=240)
    with (ART/'host-commands.jsonl').open('a') as f:f.write(json.dumps({'args':args,'code':p.returncode,'stdout':p.stdout.decode(errors='replace') if args[0]!='exec-out' else None,'stderr':p.stderr.decode(errors='replace')})+'\n')
    if check:p.check_returncode()
    return p.stdout

def guard():
    assert adb('shell','getprop','ro.kernel.qemu').strip()==b'1'
    assert adb('emu','avd','name').decode().splitlines()[0]=='GoldbergStage8Independent'
    assert adb('shell','settings','get','global','airplane_mode_on').strip()==b'1'
    assert re.search(rb'Active default network:\s*(none|null|-1)(?:\s|$)',adb('shell','dumpsys','connectivity'),re.I)

def instrument(method,label=None,killed=False,extra=None,cls=CLASS,marker=None):
    guard();name=label or method
    file=OUT/(name+'.txt');assert not file.exists(),'Preserve previous attempt;use a distinct output label/run before retry'
    command=['shell','am','instrument','-w','-e','class',cls+'#'+method,'-e','goldbergStage8','synthetic','-e','goldbergStage8Run',RUN]
    for key,value in (extra or {}).items():command+=['-e',key,value]
    command+=['com.opus.piano.test/androidx.test.runner.AndroidJUnitRunner']
    output=adb(*command).decode(errors='replace');file.write_text(output)
    if killed:
        assert 'Process crashed' in output or 'Process crash' in output,output
        assert marker and adb('shell','run-as','com.opus.piano','test','-e',marker)==b''
        result={'method':method,'label':name,'result':'EXPECTED_PROCESS_TERMINATION','junitPass':False}
    else:
        assert re.search(r'OK \(1 test\)',output) and 'FAILURES' not in output,output
        result={'method':method,'label':name,'result':'PASS','junitPass':True}
    result['inputBinding']=BINDING
    with (OUT/'probe-results.jsonl').open('a') as f:f.write(json.dumps(result)+'\n')
    print(result,flush=True)

def pull_db(name,label):
    folder='no_backup' if name=='androidx.work.workdb' else 'databases'
    target=ART/(label+'.db')
    for suffix in ['','-wal','-shm']:
        p=subprocess.run(BASE+['exec-out','run-as','com.opus.piano','cat',folder+'/'+name+suffix],capture_output=True)
        if p.returncode==0:Path(str(target)+suffix).write_bytes(p.stdout)
        elif not suffix:raise RuntimeError('Missing database '+name)
    db=sqlite3.connect(target);db.execute('PRAGMA wal_checkpoint(TRUNCATE)');assert db.execute('PRAGMA integrity_check').fetchall()==[('ok',)];assert not db.execute('PRAGMA foreign_key_check').fetchall();return db

if __name__=='__main__':
    guard()
    assert subprocess.run(BASE+['shell','run-as','com.opus.piano','test','-e','files/stage8-independent-owned'],capture_output=True).returncode==1,'Never reseed or overwrite an existing probe run'
    test=ROOT/'app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk'
    source=ROOT/'app/src/androidTest/java/com/opus/piano/Stage8IndependentPreservationTest.kt'
    assert test.stat().st_mtime_ns>source.stat().st_mtime_ns
    assert 'BUILD SUCCESSFUL' in (ART/'test-build-background.log').read_text()
    assert not (OUT/'probe-results.jsonl').exists()
    (OUT/'probe-input-binding.json').write_text(json.dumps({'productionSource':'4d245b46936d640740799dfcc6e8d61f3d44b249','baseReceipt':'22fb50bedec7b932e47640673e52fdb57bdc791a','testSourceSha256':hashlib.sha256(source.read_bytes()).hexdigest(),'testApkSha256':hashlib.sha256(test.read_bytes()).hexdigest(),'productionApkSha256':'68d48b75436f6bd3f865d784714ec48570e2745c14aa0e9682514753009f3db0','serial':SERIAL,'avd':'GoldbergStage8Independent','run':RUN},indent=2)+'\n')
    adb('install','-r',str(test))
    owned=ART/'owned';owned.write_text('Stage8-owned synthetic emulator');adb('push',str(owned),'/data/local/tmp/stage8-owned');adb('shell','run-as','com.opus.piano','cp','/data/local/tmp/stage8-owned','files/stage8-independent-owned')
    assert subprocess.run(BASE+['shell','run-as','com.opus.piano','test','-e','databases/stage8-migration-'+RUN+'.db'],capture_output=True).returncode==1
    adb('push',str(ART/'upgrade/before.db'),'/data/local/tmp/stage8-baseline-copy.db')
    adb('shell','run-as','com.opus.piano','cp','/data/local/tmp/stage8-baseline-copy.db','databases/stage8-migration-'+RUN+'.db')
    instrument('upgradedJournalBackupRoundTripAndRejectedRestoreRetainData')
    instrument('interruptRealMigrationProcess',killed=True,marker='files/stage8-independent/migration-killed.json')
    instrument('retryInterruptedMigrationRetainsEveryOriginalCell')
    instrument('prepareLivePracticeAndTerminateProcess',killed=True,marker='files/stage8-independent/practice-before-kill.json')
    instrument('recoverLivePracticeWithoutCountingDowntime',label='practice-after-process-death',extra={'round':'death'})
    instrument('prepareOfflineBundleAndPeriodicWorkForReboot')
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
    instrument('actualKilledRestoreCanRetryAndChecksumFailureCanRetryWithoutDataLoss',cls='com.opus.piano.Stage8RestoreRetryTest')
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
