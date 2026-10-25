"""Require independent complete native success at the corrected test-source SHA."""
import json
import hashlib
import subprocess
import xml.etree.ElementTree as ET
from collections import Counter
from pathlib import Path
from PIL import Image

ROOT=Path(__file__).resolve().parents[2]
OUT=ROOT/'docs/planning/goldberg-0.11/stage-8/results/targeted-ui-corrections-2026-10-25'
SOURCE='a41cd11a1b32af2949c43cb6e5b6aac94a3c2a51'
RUN=40170295032


def totals(files):
    result={k:0 for k in ['tests','failures','errors','skipped']}
    for f in files:
        x=ET.parse(f).getroot()
        for k in result:result[k]+=int(x.get(k,0))
    return result


def main():
    base=OUT/f'ci-native-{RUN}'
    run=json.loads((base/'result.json').read_text())
    assert run['headSha']==SOURCE and run['status']=='completed' and run['conclusion']=='success'
    actual=(base/'stage5-ci-results/source-commit.txt').read_text()
    assert f'github.sha: {SOURCE}' in actual and f'checkout HEAD: {SOURCE}' in actual
    assert 'emulator-5554' in (base/'stage5-ci-results/emulator-identity.txt').read_text()
    files=list((base/'app/build/outputs/androidTest-results/connected').rglob('TEST-*.xml'));assert len(files)==1
    native=totals(files);assert native==dict(tests=190,failures=0,errors=0,skipped=2),native
    cases=[c for f in files for c in ET.parse(f).getroot().iter('testcase')]
    required={'switchRowHasOneNameStateRoleAndExactlyOneActionPerInput','responsiveLabelsFitAndTabsKeepNamesSelectionAndActionsAcross32Configurations','scaffoldWindowFlagsFollowAppThemeWhenSystemThemeDiffers'}
    current=[c for c in cases if c.get('classname')=='com.opus.piano.Stage8cUiRegressionTest']
    assert {c.get('name') for c in current}==required
    assert all(not list(c) for c in current)
    accepted=[c for c in cases if c.get('classname','').startswith('com.opus.piano.Stage7b')];assert len(accepted)==9 and all(not list(c) for c in accepted)
    skips=[f"{c.get('classname')}.{c.get('name')}" for c in cases if c.find('skipped') is not None]
    assert len(skips)==2 and all('Stage4External' in c for c in skips),skips
    jvm_files=list((base/'app/build/test-results/testDebugUnitTest').glob('TEST-*.xml'));jvm=totals(jvm_files)
    assert len(jvm_files)==36 and jvm==dict(tests=275,failures=0,errors=0,skipped=0),jvm
    lint=Counter(i.get('severity') for i in ET.parse(base/'lint-results-debug.xml').getroot().findall('issue'))
    assert lint['Error']==lint['Fatal']==0,lint
    # Quality passed at the original production source; later source changes are test-only.
    quality=json.loads((OUT/'ci-quality-40168589919/result.json').read_text());assert quality['conclusion']=='success' and quality['headSha']=='4d245b46936d640740799dfcc6e8d61f3d44b249'
    assert not subprocess.check_output(['git','diff',quality['headSha'],SOURCE,'--','app/src/main','app/schemas','contracts','.github','app/build.gradle.kts'],cwd=ROOT)
    inputs=json.loads((OUT/'pointer-harness-binding.json').read_text())['inputs']
    for name,digest in inputs.items():
        assert hashlib.sha256(subprocess.check_output(['git','show',SOURCE+':'+name],cwd=ROOT)).hexdigest()==digest,name
        assert hashlib.sha256((ROOT/name).read_bytes()).hexdigest()==digest,name
    pictures=list((base/'stage5-ci-results/admin-evidence').glob('*.png'));assert len(pictures)==262,len(pictures)
    for p in pictures:
        assert p.with_suffix('.txt').is_file(),p
        Image.open(p).verify()
    result={'run':RUN,'url':run['url'],'source':SOURCE,'conclusion':'success','native':native,'nativePassed':188,'nativeClasses':len({c.get('classname') for c in cases}),'stage8cCases':[{'name':c.get('name'),'seconds':float(c.get('time','0'))} for c in current],'stage7bCases':len(accepted),'existingSkips':skips,'jvm':jvm,'jvmSuites':len(jvm_files),'lint':dict(lint),'scaffoldPngTreePairs':len(pictures),'productionUnchangedFromQualitySource':quality['headSha'],'limitations':'CI runner uses its temporary debug signer;no trusted-signer seeded upgrade,live backend,physical phone orrelease acceptance inferred.'}
    (OUT/'ci-verification.json').write_text(json.dumps(result,indent=2)+'\n')
    print(json.dumps(result,indent=2))

if __name__=='__main__':main()
