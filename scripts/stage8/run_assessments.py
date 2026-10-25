"""Bounded offline Stage 8B matrix using the opt-in instrumentation recorder.

Run only on the separately created emulator-5588. No production transport is used.
"""
import argparse
import subprocess
import time
import json
from emulator_probe import adb, guard, snap, summary, tap, OUT, ADB, SERIAL


def talkback(enabled):
    if enabled:
        adb('shell','settings','put','secure','enabled_accessibility_services','com.google.android.marvin.talkback/com.google.android.marvin.talkback.TalkBackService')
    else:
        adb('shell','settings','delete','secure','enabled_accessibility_services')
    adb('shell','settings','put','secure','accessibility_enabled', '1' if enabled else '0')
    time.sleep(.8)


def next_focus():
    adb('emu','event','mouse','80','270','0','1')
    for x in [110,150,200,240]:
        adb('emu','event','mouse',str(x),'270','0','1')
        time.sleep(.012)
    adb('emu','event','mouse','240','270','0','0')
    time.sleep(.5)


def scroll_to(state,label,prefix):
    for attempt in range(6):
        try:
            tap(state,label)
            return
        except RuntimeError:
            if attempt==5:raise
            adb('shell','input','swipe','360','850','360','350','300')
            time.sleep(.5)
            state=snap(prefix+'-scroll-'+str(attempt))


def stop():
    # Only this recorder's disposable sentinel, never any personal draft/data.
    adb('shell','run-as','com.opus.piano','touch','files/manual-stop')
    time.sleep(.8)


def run(locale,theme,font,size='small',scaffold=False,with_talkback=False,system_dark=None):
    guard()
    talkback(False)
    adb('shell','wm','size','720x1280' if size=='small' else '824x1830')
    adb('shell','settings','put','system','font_scale',str(font))
    adb('shell','cmd','locale','set-app-locales','com.opus.piano','--locales',locale)
    locale_readback=adb('shell','cmd','locale','get-app-locales','com.opus.piano')
    assert locale in locale_readback
    time.sleep(.5)
    adb('shell','cmd','uimode','night','yes' if (theme=='Dark' if system_dark is None else system_dark) else 'no')
    adb('shell','run-as','com.opus.piano','rm','-f','files/manual-stop','files/stage7b-manual/ready.txt')
    prefix=f'{locale}-{theme.lower()}-{str(font).replace(chr(46), chr(112))}-{size}'+('-scaffold' if scaffold else '-navhost')
    existing=list(OUT.glob(prefix+'-*'))
    if existing:
        archive=OUT/'attempts'/str(time.time_ns())
        archive.mkdir(parents=True)
        for path in existing: path.rename(archive/path.name)
    command=[ADB,'-s',SERIAL,'shell','am','instrument','-w','-e','goldbergManual','interactive','-e','goldbergExpectedPermission','denied','-e','goldbergTheme',theme,'-e','goldbergLocale',locale,'-e','goldbergScaffold','true' if scaffold else 'false','com.opus.piano.test/com.opus.piano.Stage8bAssessmentProbe']
    log=(OUT/(prefix+'-instrumentation.txt')).open('w')
    proc=subprocess.Popen(command,stdout=log,stderr=subprocess.STDOUT)
    try:
        for _ in range(100):
            r=subprocess.run([ADB,'-s',SERIAL,'shell','run-as','com.opus.piano','test','-e','files/stage7b-manual/ready.txt'],capture_output=True)
            if r.returncode==0: break
            if proc.poll() is not None:raise RuntimeError('Recorder exited before ready: '+prefix)
            time.sleep(.2)
        else:raise RuntimeError('Recorder ready timeout: '+prefix)
        state=snap(prefix+'-ready')
        assert abs(state['fontScale']-float(font))<.01
        assert state['profileTheme']==theme
        assert state['locale'].startswith(locale)
        if not scaffold:
            tap(state,'Profile' if locale=='en' else 'Profil')
            state=snap(prefix+'-profile')
            scroll_to(state,'Administration' if locale=='en' else 'Administrace',prefix+'-profile')
            state=snap(prefix+'-dashboard')
            scroll_to(state,'Admin Settings' if locale=='en' else 'Nastavení administrace',prefix+'-dashboard')
        if scaffold:time.sleep(2)
        state=snap(prefix+'-settings')
        if with_talkback:
            talkback(True)
            snap(prefix+'-talkback-start')
            rows=[]
            for i in range(10):
                next_focus()
                s=snap(prefix+'-focus-'+str(i))
                rows.append({'step':i,'focus':summary(s)['focus']})
            (OUT/(prefix+'-traversal.json')).write_text(json.dumps(rows,ensure_ascii=False,indent=2)+'\n')
            print(prefix,rows,flush=True)
        (OUT/(prefix+'-events.jsonl')).write_text(adb('exec-out','run-as','com.opus.piano','cat','files/stage7b-manual/events.jsonl'))
        print(prefix,{k:v for k,v in state.items() if k!='nodes'},flush=True)
    finally:
        talkback(False)
        stop()
        try:proc.wait(timeout=10)
        except subprocess.TimeoutExpired:
            proc.terminate()
            raise
        log.close()


if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('locale',choices=['en','cs']);p.add_argument('theme',choices=['Light','Dark']);p.add_argument('font',choices=['1.0','1.3','1.5','2.0']);p.add_argument('--standard',action='store_true');p.add_argument('--scaffold',action='store_true');p.add_argument('--talkback',action='store_true');p.add_argument('--opposite-system',action='store_true');a=p.parse_args()
    run(a.locale,a.theme,a.font,'standard' if a.standard else 'small',a.scaffold,a.talkback,(a.theme!='Dark') if a.opposite_system else None)
