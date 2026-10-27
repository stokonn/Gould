"""Read-only rehearsal/release guard;never signs,builds,installs,tags orpublishes."""
import argparse,hashlib,json,re,subprocess,zipfile
import os
from pathlib import Path

TRUSTED='53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e'
parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--apk',type=Path,required=True)
parser.add_argument('--output',type=Path,required=True)
parser.add_argument('--release-check',action='store_true')
a=parser.parse_args();sdk=Path(os.environ.get('ANDROID_HOME') or os.environ.get('ANDROID_SDK_ROOT') or Path.home() / 'Android/Sdk') / 'build-tools/36.0.0'
raw=subprocess.check_output([str(sdk/'aapt'),'dump','badging',str(a.apk)],text=True)
manifest=subprocess.check_output([str(sdk/'aapt'),'dump','xmltree',str(a.apk),'AndroidManifest.xml'],text=True)
signing=subprocess.check_output([str(sdk/'apksigner'),'verify','--print-certs',str(a.apk)],text=True)
package=re.search(r"package: name='([^']+)' versionCode='(\d+)' versionName='([^']+)'",raw)
assert package and package[1]=='com.opus.piano'
certificate=re.search(r'Signer #1 certificate SHA-256 digest: ([a-f0-9]+)',signing).group(1);assert certificate==TRUSTED
with zipfile.ZipFile(a.apk) as z:
 assert z.testzip() is None
 names=z.namelist();dex=b''.join(z.read(n) for n in names if re.fullmatch(r'classes\d*\.dex',n))
 test_names=['Stage8IndependentPreservationTest','Stage8bAssessmentProbe','Stage7bAcceptanceTest','Stage3bProcessDeathTest','Stage8RestoreRetryTest']
 assert not any(n.encode() in dex for n in test_names)
 forbidden=[n for n in names if any(x in n.lower() for x in ['docs/design','androidtest','full-v5.json','fixtures.json','.pmp','release_notes.md'])]
 assert not forbidden,forbidden
 assets=[{'path':n,'bytes':z.getinfo(n).file_size,'sha256':hashlib.sha256(z.read(n)).hexdigest()} for n in names if n.startswith('assets/') and not n.endswith('/')]
 assert 'com.opus.piano.test' not in manifest and 'Stage8bAssessmentProbe' not in manifest
 debug='application-debuggable' in raw
 blockers=[]
 if package[3]!='0.11':blockers.append('Version name is development0.10.1;official0.11 metadata not selected')
 if int(package[2])<=11:blockers.append('Version code must exceed trusted published code11;owner choice remains pending')
 if debug:blockers.append('Inspected APK is debuggable;final release variant/signing/build acceptance pending')
 blockers+=['Stage6 integration and final combined-source clean/native/upgrade gates remain open','No owner main/tag/release authorization']
 result={'mode':'release-guard' if a.release_check else 'rehearsal','apkSha256':hashlib.sha256(a.apk.read_bytes()).hexdigest(),'package':package[1],'versionCode':int(package[2]),'versionName':package[3],'certificateSha256':certificate,'debuggable':debug,'zipIntegrity':'PASS','testProbeClassesExcluded':test_names,'testFixturesAndPrivateMaterialExcluded':True,'assets':assets,'releaseDecision':'BLOCKED','releaseBlockers':blockers,'signingConfigurationChanged':False,'versionMetadataChanged':False}
 a.output.write_text(json.dumps(result,indent=2)+'\n')
 a.output.with_suffix('.manifest.txt').write_text(manifest);a.output.with_suffix('.badging.txt').write_text(raw);a.output.with_suffix('.signing.txt').write_text(signing)
 print(json.dumps({k:v for k,v in result.items() if k!='assets'},indent=2))
 if a.release_check:raise SystemExit(2)
