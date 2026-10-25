"""Offline actual NavHost tab and Android Back evidence; no practice starts."""
from assess import configure, probe, matrix
import subprocess
import time
import json


def main():
    configure('after')
    out=probe.OUT/'navigation'
    if out.exists():
        archive=probe.OUT/'attempts'/('navigation-'+str(time.time_ns()));out.rename(archive)
    out.mkdir(exist_ok=True)
    probe.OUT=out;matrix.OUT=out
    probe.guard();matrix.talkback(False)
    probe.adb('shell','wm','density','320');probe.adb('shell','wm','size','720x1280')
    probe.adb('shell','settings','put','system','font_scale','1.3')
    probe.adb('shell','cmd','locale','set-app-locales','com.opus.piano','--locales','en')
    probe.adb('shell','cmd','uimode','night','no')
    probe.adb('shell','run-as','com.opus.piano','rm','-f','files/manual-stop','files/stage7b-manual/ready.txt')
    command=[probe.ADB,'-s',probe.SERIAL,'shell','am','instrument','-w','-e','goldbergManual','interactive','-e','goldbergExpectedPermission','denied','-e','goldbergTheme','Light','-e','goldbergLocale','en','-e','goldbergScaffold','false','com.opus.piano.test/com.opus.piano.Stage8bAssessmentProbe']
    with (out/'instrumentation.txt').open('w') as log:
        proc=subprocess.Popen(command,stdout=log,stderr=subprocess.STDOUT)
        try:
            for _ in range(100):
                r=subprocess.run([probe.ADB,'-s',probe.SERIAL,'shell','run-as','com.opus.piano','test','-e','files/stage7b-manual/ready.txt'],capture_output=True)
                if r.returncode==0:break
                assert proc.poll() is None,'recorder exited'
                time.sleep(.2)
            else:raise RuntimeError('recorder not ready')
            state=probe.snap('routes-ready');transitions=[]
            for label in ['Library','Discover','Practice','Profile','Home']:
                probe.tap(state,label);state=probe.snap('route-'+label.lower())
                selected=[n.get('description') for n in state['nodes'] if n['selected']]
                assert selected==[label],selected
                transitions.append(label)
            # Profile -> Administration -> Settings -> actual Android Back twice.
            probe.tap(state,'Profile');state=probe.snap('route-profile-again')
            matrix.scroll_to(state,'Administration','routes-profile');state=probe.snap('routes-admin')
            matrix.scroll_to(state,'Admin Settings','routes-admin');state=probe.snap('routes-settings')
            probe.adb('shell','input','keyevent','4');time.sleep(.8);state=probe.snap('routes-back-dashboard')
            assert any(n.get('text')=='Administration' for n in state['nodes'])
            probe.adb('shell','input','keyevent','4');time.sleep(.8);state=probe.snap('routes-back-profile')
            assert any(n.get('description')=='Profile' and n['selected'] for n in state['nodes'])
            (out/'events.jsonl').write_text(probe.adb('exec-out','run-as','com.opus.piano','cat','files/stage7b-manual/events.jsonl'))
            (out/'result.json').write_text(json.dumps({'source':'4d245b46936d640740799dfcc6e8d61f3d44b249','actualNavHostTabTransitions':transitions,'actualAndroidBack':2,'realTalkBackDoubleTapActivation':'UNVERIFIED:retained unsuccessful injection attempts;native exactly-once actions and actual TalkBack focus are separately verified','noPracticeStarted':True,'radioActivation':False},indent=2)+'\n')
            print('PASS:five actual routes,two actual Back transitions',flush=True)
        finally:
            matrix.talkback(False);matrix.stop();proc.wait(timeout=15)

if __name__=='__main__':main()
