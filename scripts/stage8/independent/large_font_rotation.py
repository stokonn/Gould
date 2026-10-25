"""Additional actual CZ/Dark large-font orientation samples;restore system settings."""
from run_probes import *
import xml.etree.ElementTree as ET

guard()
assert not (OUT/'large-font-rotation.json').exists()
old_font=adb('shell','settings','get','system','font_scale').decode().strip()
old_rotation=adb('shell','settings','get','system','user_rotation').decode().strip()
old_auto=adb('shell','settings','get','system','accelerometer_rotation').decode().strip()
old_locale=re.findall(r'\[([^]]*)\]',adb('shell','cmd','locale','get-app-locales','com.opus.piano').decode())[-1]
assert re.fullmatch(r'[a-zA-Z0-9,-]*',old_locale)
results=[]
try:
    adb('shell','settings','put','system','accelerometer_rotation','0')
    adb('shell','cmd','locale','set-app-locales','com.opus.piano','--locales','cs')
    for font in ['1.3','2.0']:
        adb('shell','settings','put','system','font_scale',font)
        for rotation in ['0','1']:
            adb('shell','settings','put','system','user_rotation',rotation)
            adb('shell','am','start','-W','-n','com.opus.piano/.MainActivity');time.sleep(1.5)
            name='cs-dark-f'+font.replace('.','p')+'-rotation'+rotation
            adb('shell','uiautomator','dump','/sdcard/stage8-'+name+'.xml')
            raw=adb('exec-out','cat','/sdcard/stage8-'+name+'.xml');(OUT/(name+'.xml')).write_bytes(raw)
            (OUT/(name+'.png')).write_bytes(adb('exec-out','screencap','-p'))
            tree=ET.fromstring(raw)
            assert tree.get('rotation')==rotation
            names=[n.get('content-desc') for n in tree.iter('node') if n.get('content-desc') in ['Domů','Knihovna','Objevovat','Cvičení','Profil']]
            assert len(names)==len(set(names))==5,names
            results.append({'file':name,'locale':'cs','fontScale':float(font),'rotation':int(rotation),'fiveNamesPresent':True})
finally:
    adb('shell','settings','put','system','font_scale',old_font if old_font!='null' else '1.0')
    adb('shell','settings','put','system','user_rotation',old_rotation if old_rotation!='null' else '0')
    adb('shell','settings','put','system','accelerometer_rotation',old_auto if old_auto!='null' else '1')
    adb('shell','cmd','locale','set-app-locales','com.opus.piano','--locales',old_locale or '""')
(OUT/'large-font-rotation.json').write_text(json.dumps(results,indent=2)+'\n')
print('PASS:four additional actual Czech font1.3/2 portrait/landscape configurations',flush=True)
