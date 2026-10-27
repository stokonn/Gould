"""Validate bounded Stage8C captures; native/text-layout and visual checks stay separate."""
import hashlib
import json
import subprocess
from pathlib import Path
from PIL import Image

ROOT=Path(__file__).resolve().parents[2]
OUT=ROOT/'docs/planning/goldberg-0.11/stage-8/results/targeted-ui-corrections-2026-10-25'
NAMES={'en':['Home','Library','Discover','Practice','Profile'], 'cs':['Domů','Knihovna','Objevovat','Cvičení','Profil']}


def main():
    manifest=OUT/'SHA256.json'
    if manifest.exists():
        for name,digest in json.loads(manifest.read_text()).items():
            assert hashlib.sha256((OUT/name).read_bytes()).hexdigest()==digest,name
    binding=json.loads((OUT/'refined-source-binding.json').read_text())
    for name,digest in binding['inputs'].items():
        historic=subprocess.check_output(['git','show','4d245b46936d640740799dfcc6e8d61f3d44b249:'+name],cwd=ROOT)
        assert hashlib.sha256(historic).hexdigest()==digest,name
        if not name.endswith('/Stage8cUiRegressionTest.kt'):
            assert hashlib.sha256((ROOT/name).read_bytes()).hexdigest()==digest,name
    # A follow-up test-only safe-inset correction does not change captured production UI.
    followup=json.loads((OUT/'pointer-harness-binding.json').read_text())
    for name,digest in followup['inputs'].items():
        assert hashlib.sha256((ROOT/name).read_bytes()).hexdigest()==digest,name
    old=OUT.parent/'first-technical-verification-2026-10-25'
    old_manifest=json.loads((old/'SHA256.json').read_text())
    for name,digest in old_manifest.items():
        assert hashlib.sha256((old/name).read_bytes()).hexdigest()==digest,name
    records=[]
    clippedRows=[]
    for size,width,height in [('small',360,640),('standard',412,915)]:
        for locale in NAMES:
            for theme in ['Light','Dark']:
                for font in [1.0,1.3,1.5,2.0]:
                    f=OUT/'after'/f'{locale}-{theme.lower()}-{str(font).replace(".","p")}-{size}-navhost-settings.json'
                    state=json.loads(f.read_text())
                    assert (state['densityDpi'],state['screenWidthDp'],state['screenHeightDp'])==(320,width,height),f
                    assert state['locale'].startswith(locale) and state['profileTheme']==theme and abs(state['fontScale']-font)<.01,f
                    assert state['nodes'] and not state['scaffold'] and not state['notificationGranted'],f
                    assert state['lightStatusBars']==state['lightNavigationBars']==(theme=='Light'),f
                    assert state['systemNight']==(16 if theme=='Light' else 32),f
                    for name in NAMES[locale]:
                        nodes=[n for n in state['nodes'] if n.get('description')==name]
                        assert len(nodes)==1 and nodes[0]['roleDescription'] in ['Tab','Karta'],(f,name,nodes)
                    assert len([n for n in state['nodes'] if n.get('selected')])==1,f
                    controls=[n for n in state['nodes'] if n['checkable']]
                    assert len(controls)==3 and len({n['description'] for n in controls})==3,f
                    assert all(n.get('roleDescription')==('Switch' if locale=='en' else 'Přepnout') and not n['checked'] for n in controls),f
                    for n in controls:
                        assert sum((x.get('text')==n['description'] or x.get('description')==n['description']) for x in state['nodes'])==1,(f,n)
                        x1,y1,x2,y2=map(int,n['bounds'].split());assert y2>y1 and x2-x1>=96,(f,n)
                        # Platform bounds are viewport-clipped for scrollable rows.
                        # Native component regression independently asserts >=48dp layout.
                        if y2-y1<96:clippedRows.append({'file':str(f.relative_to(OUT)),'name':n['description'],'visibleHeightPx':y2-y1})
                    im=Image.open(f.with_suffix('.png'));assert im.size==(width*2,height*2);im.verify()
                    records.append({'file':str(f.relative_to(OUT)),'locale':locale,'theme':theme,'fontScale':font,'widthDp':width,'heightDp':height})
    beforeTraversals=[]
    for f in sorted((OUT/'before').glob('*-traversal.json')):
        rows=json.loads(f.read_text());assert len(rows)==10
        focus=[pair for row in rows for pair in row['focus']]
        labels=[desc for text,desc in focus if desc]
        assert len(labels)==3 and len(set(labels))==3,(f,focus)
        for label in labels:
            assert focus.count([label,None])==focus.count([None,label])==1,(f,label)
        beforeTraversals.append({'file':str(f.relative_to(OUT)),'duplicateLabelControlPairs':3,'emptyFocusSteps':sum(not row['focus'] for row in rows)})
    assert len(beforeTraversals)==2
    traversals=[]
    for f in sorted((OUT/'after').glob('*-traversal.json')):
        rows=json.loads(f.read_text());assert len(rows)==10,f
        descriptions=[]
        events=[json.loads(l) for l in f.with_name(f.name.replace('-traversal.json','-events.jsonl')).read_text().splitlines()]
        for i in range(10):
            s=json.loads(f.with_name(f.name.replace('-traversal.json',f'-focus-{i}.json')).read_text())
            assert s['touchExploration']==1 and 'TalkBackService' in s['services'],f
            focused=[n for n in s['nodes'] if n['accessibilityFocused']];assert len(focused)==1,f
            assert any(e['type']=='TYPE_VIEW_ACCESSIBILITY_FOCUSED' and e.get('package')=='com.opus.piano' and e.get('sourceDescription')==focused[0].get('description') and e.get('sourceText')==focused[0].get('text') and 0<=s['uptime']-e['uptime']<=2500 for e in events),(f,i,'no matching current focus event')
            if focused[0]['checkable']:descriptions.append(focused[0]['description'])
        assert len(descriptions)==len(set(descriptions))==3,(f,descriptions)
        for name in descriptions:
            assert any(e['type']=='TYPE_VIEW_ACCESSIBILITY_FOCUSED' and e.get('sourceDescription')==name for e in events),(f,name)
        traversals.append({'file':str(f.relative_to(OUT)),'steps':10,'singleSwitchStops':descriptions})
    assert len(traversals)==4,len(traversals)
    opposite=[]
    for locale in NAMES:
        for theme in ['Light','Dark']:
            prefix=f'{locale}-{theme.lower()}-1p0-small-'
            scaffold=OUT/'after'/f'{prefix}scaffold-settings.json'
            actual=OUT/'after/opposite-system'/f'{prefix}navhost-settings.json'
            a,b=[json.loads(p.read_text()) for p in [scaffold,actual]]
            assert a['scaffold'] and not b['scaffold']
            for s in [a,b]:
                assert s['profileTheme']==theme and s['systemNight']==(32 if theme=='Light' else 16)
                assert s['lightStatusBars']==s['lightNavigationBars']==(theme=='Light')
            opposite.append({'scaffold':str(scaffold.relative_to(OUT)),'navhost':str(actual.relative_to(OUT)),'theme':theme,'locale':locale,'statusAndNavigationFlagsMatch':True})
    result={'source':'4d245b46936d640740799dfcc6e8d61f3d44b249','validBaseConfigurations':len(records),'freshBaselineTraversals':beforeTraversals,'actualTalkBackTraversals':len(traversals),'actualFocusedSteps':40,'matchingCurrentFocusEvents':40,'oppositeSystemPairs':len(opposite),'preservedStage8bFiles':len(old_manifest),'viewportClippedRows':clippedRows,'configurations':records,'traversals':traversals,'oppositeSystem':opposite,'limits':'Tree/geometry/source checks only; text overflow is verified by native TextLayoutResult; screenshots require human-agent visual inspection; no audible speech-quality claim.'}
    (OUT/'evidence-verification.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n')
    print({k:v for k,v in result.items() if k not in ['configurations','traversals','oppositeSystem']})

if __name__=='__main__':main()
