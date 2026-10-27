"""Synthetic-only negative binary rollback and forward retry;never uninstall."""
from run_probes import *

def original_rows(db):
    shape=json.loads((ART/'upgrade/before.json').read_text())
    result={table:[list(row) for row in db.execute('SELECT * FROM '+table+' ORDER BY rowid')] for table in shape}
    assert result==shape
    return result

guard()
# Explicitly verify the immutable baseline has no destructive fallback before this negative probe.
check=subprocess.run(['git','grep','-n','fallbackToDestructiveMigration','v0.10.1','--','app/src/main'],capture_output=True);assert check.returncode==1
adb('shell','am','force-stop','com.opus.piano')
db=pull_db('opus.db','before-binary-rollback');original_rows(db);assert db.execute('PRAGMA user_version').fetchone()[0]==10;db.close()
baseline=Path(os.environ.get('GOLDBERG_ROLLBACK_APK', str(ROOT/'.gradle/release-artifacts/Goldberg-v0.10.1.apk')))
assert hashlib.sha256(baseline.read_bytes()).hexdigest()=='854bbfebf617ec920df60d181a12e0dcbd2fed10052f2479903a78a290cef312'
adb('install','-r',str(baseline));adb('shell','am','start','-W','-n','com.opus.piano/.MainActivity',check=False);time.sleep(2)
log=adb('logcat','-d','-t','5000').decode(errors='replace');(ART/'rollback-logcat.txt').write_text(log)
matched=[line for line in log.splitlines() if 'migration from 10 to 8' in line.lower() or 'downgrade' in line.lower()]
assert matched,'Expected non-destructive schema downgrade refusal not observed'
(OUT/'rollback-refusal.txt').write_text('\n'.join(matched)+'\n')
adb('shell','am','force-stop','com.opus.piano')
db=pull_db('opus.db','after-refused-binary-rollback');original_rows(db);assert db.execute('PRAGMA user_version').fetchone()[0]==10;db.close()
adb('install','-r',str(ROOT/'.gradle/stage8c-artifacts/candidate-app-wrap.apk'))
adb('shell','am','start','-W','-n','com.opus.piano/.MainActivity');time.sleep(2);adb('shell','am','force-stop','com.opus.piano')
db=pull_db('opus.db','after-forward-retry');original_rows(db);assert db.execute('PRAGMA user_version').fetchone()[0]==10;db.close()
(OUT/'rollback-rehearsal.json').write_text(json.dumps({'syntheticOnly':True,'baselineBinaryInstalledInPlace':True,'schema10to8Refused':True,'originalDataUnchanged':True,'schemaStill10':True,'forwardReinstallRecovered':True,'uninstallUsed':False,'expectation':'Binary rollback is not a supported database downgrade. Preserve source snapshots/compatible backups;never uninstall or destroy data to bypass this guard.'},indent=2)+'\n')
print('PASS:binary downgrade refused without data loss;forward candidate reinstall opens same intact schema10',flush=True)
