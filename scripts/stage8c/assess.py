"""Record Stage 8C before/after evidence without writing any Stage 8B evidence."""
import argparse
from pathlib import Path
import sys
import time

ROOT=Path(__file__).resolve().parents[2]
sys.path.insert(0,str(ROOT/'scripts/stage8'))
import emulator_probe as probe
import run_assessments as matrix

SERIAL='emulator-5588'
BASE=ROOT/'docs/planning/goldberg-0.11/stage-8/results/targeted-ui-corrections-2026-10-25'


original_talkback=matrix.talkback

def stable_talkback(enabled):
    original_talkback(enabled)
    if enabled:
        # Let the real service bind and populate active windows after a cold boot.
        time.sleep(2)

matrix.talkback=stable_talkback

# Native component tests deliberately restore display overrides. Every recording
# independently establishes and checks the intended dp viewport before launch.
original_run=matrix.run

def guarded_run(locale,theme,font,size='small',scaffold=False,with_talkback=False,system_dark=None):
    probe.guard()
    registration=probe.adb('shell','pm','list','instrumentation','com.opus.piano')
    assert 'com.opus.piano.test/com.opus.piano.Stage8bAssessmentProbe' in registration,'recorder not registered;setup must install preserved probe APK'
    probe.adb('shell','wm','density','320')
    original_snap=matrix.snap
    def checked_snap(name):
        state=original_snap(name)
        assert state['nodes'],(name,'empty active accessibility tree;not a verified capture')
        if '-focus-' in name:
            assert state['touchExploration']==1
            assert 'com.google.android.marvin.talkback' in state.get('services','')
            assert any(n['accessibilityFocused'] for n in state['nodes']),(name,'no actual focused node')
        assert state['densityDpi']==320,(name,state['densityDpi'])
        assert state['screenWidthDp']==(360 if size=='small' else 412),(name,state['screenWidthDp'])
        return state
    matrix.snap=checked_snap
    try:return original_run(locale,theme,font,size,scaffold,with_talkback,system_dark)
    finally:matrix.snap=original_snap

matrix.run=guarded_run

def configure(phase):
    assert phase in ['before','after']
    out=BASE/phase;out.mkdir(parents=True,exist_ok=True)
    probe.OUT=out;probe.SERIAL=SERIAL;matrix.OUT=out;matrix.SERIAL=SERIAL
    return out


if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('phase',choices=['before','after']);p.add_argument('locale',choices=['en','cs']);p.add_argument('theme',choices=['Light','Dark']);p.add_argument('font',choices=['1.0','1.3','1.5','2.0']);p.add_argument('--standard',action='store_true');p.add_argument('--scaffold',action='store_true');p.add_argument('--talkback',action='store_true');p.add_argument('--opposite-system',action='store_true');a=p.parse_args()
    configure(a.phase)
    matrix.run(a.locale,a.theme,a.font,'standard' if a.standard else 'small',a.scaffold,a.talkback,(a.theme!='Dark') if a.opposite_system else None)
