"""Offline platform readbacks and actual MainActivity rotation/keyboard observations."""
from run_probes import *
import xml.etree.ElementTree as ET

def snap(name):
    adb('shell','uiautomator','dump','/sdcard/stage8-'+name+'.xml')
    raw=adb('exec-out','cat','/sdcard/stage8-'+name+'.xml');(OUT/(name+'.xml')).write_bytes(raw)
    (OUT/(name+'.png')).write_bytes(adb('exec-out','screencap','-p'))
    return ET.fromstring(raw)

def tabs(tree):return [n for n in tree.iter('node') if n.get('content-desc') in ['Home','Library','Discover','Practice','Profile','Domů','Knihovna','Objevovat','Cvičení','Profil']]

guard()
old_saver=adb('shell','cmd','netpolicy','get','restrict-background').decode()
old_bucket=adb('shell','am','get-standby-bucket','com.opus.piano').decode().strip()
try:
    adb('shell','cmd','netpolicy','set','restrict-background','true')
    saver=adb('shell','cmd','netpolicy','get','restrict-background').decode();assert 'enabled' in saver.lower()
    adb('shell','am','set-standby-bucket','com.opus.piano','restricted')
    bucket=adb('shell','am','get-standby-bucket','com.opus.piano').decode().strip();assert int(bucket)>=40
    adb('shell','cmd','devicestoragemonitor','force-low','-f')
    low=adb('shell','dumpsys','devicestoragemonitor').decode();(OUT/'forced-low-storage.txt').write_text(low)
    assert 'LOW' in low
    (OUT/'platform-restrictions.json').write_text(json.dumps({'dataSaverReadback':saver.strip(),'restrictedBucket':bucket,'forcedLowStorageObserved':True,'realDiskEnospcTested':False,'actualMeteredNetworkOrTransferTested':False,'limits':'Platform flags with all radios off;retained deterministic policy/lowspace/interrupt fixtures cover application decisions;not a real disk-full or mobile-transfer result.'},indent=2)+'\n')
finally:
    adb('shell','cmd','devicestoragemonitor','reset','-f')
    adb('shell','cmd','netpolicy','set','restrict-background','true' if 'enabled' in old_saver.lower() else 'false')
    adb('shell','am','set-standby-bucket','com.opus.piano',old_bucket)

old_rotation=adb('shell','settings','get','system','user_rotation').decode().strip()
old_auto=adb('shell','settings','get','system','accelerometer_rotation').decode().strip()
try:
    adb('shell','am','start','-W','-n','com.opus.piano/.MainActivity');time.sleep(2)
    adb('shell','settings','put','system','accelerometer_rotation','0')
    adb('shell','settings','put','system','user_rotation','0');time.sleep(1)
    portrait=snap('actual-portrait');assert len(tabs(portrait))==5
    adb('shell','settings','put','system','user_rotation','1');time.sleep(2)
    landscape=snap('actual-landscape');assert landscape.get('rotation') in ['1','3']
    assert len(tabs(landscape))==5
    # Walk real keyboard focus;record every focused node without activating arbitrary controls.
    focus=[]
    for i in range(8):
        adb('shell','input','keyevent','61');time.sleep(.2)
        tree=snap('landscape-tab-'+str(i))
        focus.append([dict(n.attrib) for n in tree.iter('node') if n.get('focused')=='true'])
    assert any(focus),'No actual keyboard focus observed'
    library=next(n for n in tabs(landscape) if n.get('content-desc') in ['Library','Knihovna'])
    bounds=list(map(int,re.findall(r'\d+',library.get('bounds'))))
    adb('shell','input','tap',str((bounds[0]+bounds[2])//2),str((bounds[1]+bounds[3])//2));time.sleep(1)
    tree=snap('landscape-library')
    edits=[n for n in tree.iter('node') if n.get('class')=='android.widget.EditText' and n.get('enabled')=='true']
    ime={'editableFound':bool(edits),'imeDismissalVerified':False}
    if edits:
        bounds=list(map(int,re.findall(r'\d+',edits[0].get('bounds'))))
        adb('shell','input','tap',str((bounds[0]+bounds[2])//2),str((bounds[1]+bounds[3])//2));time.sleep(1)
        snap('landscape-ime');before=adb('shell','dumpsys','input_method').decode();(OUT/'ime-before-back.txt').write_text(before)
        adb('shell','input','keyevent','4');time.sleep(.8);snap('landscape-ime-back');after=adb('shell','dumpsys','input_method').decode();(OUT/'ime-after-back.txt').write_text(after)
        ime['imeDismissalVerified']=('mInputShown=true' in before and 'mInputShown=false' in after)
    (OUT/'orientation-keyboard.json').write_text(json.dumps({'actualMainActivity':True,'portraitTabs':5,'landscapeTabs':5,'rotation':landscape.get('rotation'),'keyboardSteps':len(focus),'focusedSteps':sum(bool(r) for r in focus),'focus':focus,'ime':ime,'limits':'Existing8C EN/CZ/theme/font matrix retained;this is additional actual orientation/keyboard evidence,not audible TalkBack acceptance.'},indent=2)+'\n')
finally:
    adb('shell','settings','put','system','user_rotation',old_rotation if old_rotation!='null' else '0')
    adb('shell','settings','put','system','accelerometer_rotation',old_auto if old_auto!='null' else '1')
print('Platform readbacks and actual portrait/landscape/keyboard observations complete',flush=True)
