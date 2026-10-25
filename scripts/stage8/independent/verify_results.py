"""Validate measured independent Stage8 results without claiming integrated release acceptance."""
from pathlib import Path
import hashlib,json,subprocess,base64
from PIL import Image
ROOT=Path(__file__).resolve().parents[3]
OUT=ROOT/'docs/planning/goldberg-0.11/stage-8/results/independent-android-2026-10-25'
def read(name):return json.loads((OUT/name).read_text())
def main():
 u=read('upgrade-result.json');assert u['sessions']==37 and u['personalSchema']==10 and u['baselineSchemaVerified']==8 and u['originalRowsUnchanged'] and u['preferencesUnchanged'] and u['integrityCheck']=='ok'
 before=read('upgrade-before.json')
 assert len(before)==12 and before==read('upgrade-after.json')==read('upgrade-after-abandoned-install.json')==read('original-after-all-checks.json')
 assert before['profile_settings'][0][4]=='first_notes' and before['badge_unlocks'][0][1]==123456789
 assert before['profile_settings'][0][2] and before['practice_drafts']
 summary=read('probe-summary.json');assert summary['junitPassed']==12 and summary['expectedProcessTerminations']==6 and len(summary['unsuccessfulNativeAttemptsRetained'])==1
 rows=[json.loads(x) for x in (OUT/'probe-results.jsonl').read_text().splitlines()]
 assert sum(r['junitPass'] for r in rows)==12 and sum(not r['junitPass'] for r in rows)==6
 for r in rows:
  text=(OUT/(r['label']+'.txt')).read_text()
  assert ('OK (1 test)' in text) if r['junitPass'] else ('Process crashed' in text or 'Process crash' in text)
 assert read('native-migration-retry.json')['allOriginalTypedCellsEqual']
 live=read('native-practice-before-kill.json');draft=json.loads(base64.b64decode(live['rawBase64']))
 assert draft['credited']==live['credited']==6008 and live['realElapsed']==6221
 assert sum(p['duration'] for p in draft['parts'])==draft['credited']
 for phase in ['death','reboot']:
  recovery=read('native-practice-recovery-'+phase+'.json');assert recovery['credited']==live['credited'] and not recovery['running'] and recovery['draftBytesUnchanged'] and recovery['downtimeNotCredited']
 assert read('native-practice-completion.json')['sessionCount']==1
 assert read('native-restore-retry.json')['actualBeforeCommitKillRetry'] and read('native-restore-retry.json')['badChecksumRetry']
 reboot=read('reboot.json');assert reboot['beforeBootId']!=reboot['afterBootId'] and reboot['radiosRemainOffline']
 background=read('native-background-after.json');assert background['workIdsPersisted'] and background['readyBlobUnchanged'] and background['cancelled'] and not background['realThreeHourDeliveryVerified']
 work=read('work-before-reboot.json');assert any(r[2].endswith('AdminSyncWorker') and r[3]==10800000 for r in work['rows'])
 orientation=read('orientation-keyboard.json');assert orientation['portraitTabs']==orientation['landscapeTabs']==5 and orientation['focusedSteps']==8 and orientation['ime']['imeDismissalVerified']
 rotations=read('large-font-rotation.json');assert len(rotations)==4 and {(r['fontScale'],r['rotation']) for r in rotations}=={(f,r) for f in [1.3,2] for r in [0,1]}
 artifact=read('artifact-audit.json');assert artifact['package']=='com.opus.piano' and artifact['certificateSha256']=='53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e' and artifact['versionCode']==11 and artifact['debuggable'] and artifact['releaseDecision']=='BLOCKED'
 assert read('release-guard-exit.json')['actualExit']==2
 rollback=read('rollback-rehearsal.json');assert rollback['schema10to8Refused'] and rollback['originalDataUnchanged'] and rollback['forwardReinstallRecovered'] and not rollback['uninstallUsed']
 data=read('final-data-verification.json');assert data['allOriginalRowsUnchanged'] and data['schema']==10 and data['foreignKeyViolations']==0 and data['installedApkSha256']==artifact['apkSha256']
 trace=read('source-artifact-traceability.json')
 for name,digest in {**trace['productionInputsUnchanged'],**trace['currentTestInputs']}.items():assert hashlib.sha256((ROOT/name).read_bytes()).hexdigest()==digest,name
 assert not subprocess.check_output(['git','diff','22fb50bedec7b932e47640673e52fdb57bdc791a','--','app/src/main','app/schemas','contracts','.github','app/build.gradle.kts'],cwd=ROOT)
 for picture in OUT.glob('*.png'):Image.open(picture).verify()
 result={'newTargetedJunitPasses':12,'newJunitSkips':0,'expectedActualProcessTerminations':6,'correctedFixtureFailuresRetained':1,'unresolvedApplicationFailures':0,'actualReboots':1,'originalTablesCompared':12,'originalSessionsRetained':37,'newActualOrientationConfigurations':6,'keyboardFocusSteps':8,'realThreeHourDeliveryVerified':False,'physicalDeviceTested':False,'productionSourceChanged':False,'fullStage8Accepted':False}
 (OUT/'verification-summary.json').write_text(json.dumps(result,indent=2)+'\n');print(json.dumps(result))
if __name__=='__main__':main()
