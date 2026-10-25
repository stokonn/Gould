"""Confirm the bottom switch scrolls fully into view at CZ/font2 on360dp."""
from assess import configure,probe,matrix
import json

configure('after')
out=probe.OUT/'scroll-2x';out.mkdir(exist_ok=True);probe.OUT=out;matrix.OUT=out
original=matrix.snap

def snap(name):
    state=original(name)
    if name.endswith('-settings'):
        probe.adb('shell','input','swipe','360','860','360','350','350')
        import time
        time.sleep(.8)
        after=probe.snap('cs-dark-2p0-small-settings-scrolled')
        controls=[n for n in after['nodes'] if n['checkable']]
        target=next(n for n in controls if n.get('description','').startswith('Povolit mobilní'))
        x1,y1,x2,y2=map(int,target['bounds'].split())
        assert y2-y1>=96 and x2-x1>=96 and target['visible'] and target['clickable']
        assert not target['checked']
        tabs=[n for n in after['nodes'] if n.get('roleDescription')=='Karta']
        assert len(tabs)==5
        assert y2<=min(int(n['bounds'].split()[1]) for n in tabs)
        (out/'result.json').write_text(json.dumps({'source':'4d245b46936d640740799dfcc6e8d61f3d44b249','locale':'cs','fontScale':2,'widthDp':360,'scrolledSwitchBounds':target['bounds'],'fullAtLeast48dpTouchTarget':True,'allFiveDestinationsRetained':True,'actionInvoked':False},indent=2)+'\n')
    return state

matrix.snap=snap
matrix.run('cs','Dark','2.0')
print('PASS:full lower Switch row reachable above all5tabs after scroll')
