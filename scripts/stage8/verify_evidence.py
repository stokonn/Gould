"""Verify Stage 8B evidence integrity and measured scope, never mark defects fixed."""
import hashlib
import json
from pathlib import Path
import subprocess
from PIL import Image

ROOT=Path(__file__).resolve().parents[2]
OUT=ROOT/'docs/planning/goldberg-0.11/stage-8/results/first-technical-verification-2026-10-25'
BASE='2f4d1b87a31fb4d8167652a411813a7909416900'
EXPECTED_APK='93ae9ec88472e4f07dc5328980786e9ce252887ccbac507c0034195cd9b51961'


def main():
    assert not subprocess.check_output(['git','diff',BASE,'--','app/src/main','app/schemas','contracts','.github'],cwd=ROOT)
    binding=json.loads((OUT/'recorder-build-binding.json').read_text())
    for name,digest in binding['inputsAndOutputs'].items():
        if name.endswith('.apk'):continue  # Preserved APKs are ignored local artifacts.
        assert hashlib.sha256((ROOT/name).read_bytes()).hexdigest()==digest,name
    assert binding['inputsAndOutputs']['app/build/outputs/apk/debug/app-debug.apk']==EXPECTED_APK
    assert 'androidx.test.runner.AndroidJUnitRunner' in (OUT/'default-runner-manifest.xml').read_text()
    states=json.loads((OUT/'render-index.json').read_text())
    tuples={(s['locale'],s['theme'],s['fontScale'],tuple(s['pixels']),s['scaffold'],s['systemNight']) for s in states}
    assert len(tuples)==28, len(tuples)
    nav=[s for s in states if not s['scaffold']]
    for locale in ['en','cs']:
        for theme in ['Light','Dark']:
            for font in [1,1.3,1.5,2]:
                assert any(s['locale'].startswith(locale) and s['theme']==theme and s['fontScale']==font and s['pixels']==[720,1280] for s in nav)
    for state in states:
        path=OUT/state['file'];data=json.loads(path.read_text());im=Image.open(path.with_suffix('.png'));im.verify()
        if not state['scaffold']:
            labels={n.get('text') or n.get('description') for n in data['nodes']}
            expected=['Home','Library','Discover','Practice','Profile'] if state['locale'].startswith('en') else ['Domů','Knihovna','Objevovat','Cvičení','Profil']
            assert set(expected)<=labels,(path,labels)
            assert data['lightStatusBars']==(state['theme']=='Light')
            assert data['lightNavigationBars']==(state['theme']=='Light')
    traversals=list(OUT.glob('*-traversal.json'));assert len(traversals)==4
    for path in traversals:
        rows=json.loads(path.read_text());assert len(rows)==10
        for i in [2,4,6]:
            assert rows[i]['focus'][0][0]==rows[i+1]['focus'][0][1],path
        events=[json.loads(line) for line in (OUT/path.name.replace('-traversal.json','-events.jsonl')).read_text().splitlines()]
        assert any(e['type']=='TYPE_VIEW_ACCESSIBILITY_FOCUSED' and e.get('sourceDescription') for e in events)
    offline=json.loads((OUT/'final-emulator-state.json').read_text())
    assert offline['qemu']=='1' and offline['airplane']=='1' and offline['wifi']=='0' and offline['mobileData']=='0'
    assert offline['activeDefaultNetwork']==['Active default network: none']
    assert offline['installedApkSha256']==EXPECTED_APK
    result={'evidenceIntegrity':'PASS','uniqueSettingsConfigurations':28,'settingsCapturesIncludingRetriedScaffold':len(states),'talkbackTraversals':4,'swipeNextSteps':40,'newJUnitSuiteExecutions':0,'productionSourceUnchanged':True,'productionApkByteIdentical':True,'A8-1':'REPRODUCED, NOT FIXED','A8-2':'VISUALLY REPRODUCED, NOT FIXED','A8-3':'SCAFFOLD STATUS-CONTRAST REPRODUCED; PRODUCT DEFECT NOT DEMONSTRATED IN TESTED CONFIGURATIONS'}
    (OUT/'verification.json').write_text(json.dumps(result,indent=2)+'\n')
    for name,digest in json.loads((OUT/'SHA256.json').read_text()).items():
        assert hashlib.sha256((OUT/name).read_bytes()).hexdigest()==digest,name
    print(json.dumps(result))


if __name__=='__main__':main()
