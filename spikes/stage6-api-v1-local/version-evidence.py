"""Read-only version gate evidence; repository search never proves global absence."""
from pathlib import Path
import json,subprocess,sys
ROOT=Path(__file__).resolve().parents[2]
TARGET='0887d3fe33cf5d49d46e14e1de95168f08a493a2'
def cmd(*args):
 r=subprocess.run(['git',*args],cwd=ROOT,capture_output=True,text=True)
 assert r.returncode in [0,1],r.stderr
 return {'argv':['git',*args],'exitCode':r.returncode,'stdout':r.stdout}
def main():
 evidence=[]
 for tag in ['v0.9.2','v0.10','v0.10.1']:
  entry=cmd('ls-tree','-r','--name-only',tag,'--','app/src/main/java/com/opus/piano/admin');assert not entry['stdout'];evidence.append(entry)
 evidence.append(cmd('log','--all','--format=%H %s','--','app/src/main/java/com/opus/piano/admin/AdminApi.kt'))
 evidence.append(cmd('grep','-n','-e','const val V1','-e','DisabledAdminTransport',TARGET,'--','app/src/main/java/com/opus/piano/admin/AdminApi.kt'))
 evidence.append(cmd('grep','-n','decisionsEnabled = false',TARGET,'--','app/src/main/java/com/opus/piano/OpusApplication.kt'))
 result={'searchedReleaseTags':['v0.9.2','v0.10','v0.10.1'],'consumerCommit':TARGET,'confirmedIncompatibleLocalConsumer':True,'incompatibleConsumerScope':'Stage7B synthetic client source; selected JVM replay measured separately','releasedAndroidAdminConsumerFoundInSearchedTags':False,'confirmedGlobalConsumerAbsence':False,'unknownExternalOrDeployedConsumerState':True,'ownerVersionDecisionRequired':True,'publicVersionSelected':False,'evidence':evidence}
 Path(sys.argv[1]).write_text(json.dumps(result,indent=2)+'\n');print('PASS: release/history/disabled-client version evidence captured; external/deployed consumer state UNKNOWN, owner version gate remains open.')
if __name__=='__main__':main()
