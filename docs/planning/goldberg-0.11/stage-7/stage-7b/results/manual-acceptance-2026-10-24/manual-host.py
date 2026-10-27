import subprocess,time,json,re
from pathlib import Path
root=Path('/tmp/goldberg-manual-evidence');base=['/home/stokonn/Android/Sdk/platform-tools/adb','-s','emulator-5586']
def adb(*args,binary=False,timeout=30):
 r=subprocess.run(base+list(args),capture_output=True,timeout=timeout)
 with (root/'host-commands.jsonl').open('a') as f:f.write(json.dumps({'time':time.time(),'args':args,'code':r.returncode,'stdout':None if binary else r.stdout.decode(errors='replace'),'stderr':r.stderr.decode(errors='replace')})+'\n')
 if r.returncode:raise RuntimeError((args,r.returncode,r.stdout,r.stderr))
 return r.stdout if binary else r.stdout.decode()
def guard():assert adb('shell','getprop','ro.kernel.qemu').strip()=='1'
def snap(name):
 assert re.fullmatch('[A-Za-z0-9_-]+',name)
 adb('shell','run-as','com.opus.piano','touch','files/manual-capture-'+name)
 for i in range(60):
  done=subprocess.run(base+['shell','run-as','com.opus.piano','test','-e','files/manual-capture-'+name],capture_output=True)
  if done.returncode!=0:break
  time.sleep(.1)
 else:raise RuntimeError('Snapshot timeout '+name)
 body=adb('exec-out','run-as','com.opus.piano','cat','files/stage7b-manual/'+name+'.json');(root/(name+'.json')).write_text(body)
 (root/(name+'.png')).write_bytes(adb('exec-out','run-as','com.opus.piano','cat','files/stage7b-manual/'+name+'.png',binary=True))
 return json.loads(body)
def summary(s):
 return {'pid':s['pid'],'ime':s.get('imeVisible'),'imeBottom':s.get('imeBottom'),'a11yFocused':[(n.get('text'),n.get('description')) for n in s['nodes'] if n['accessibilityFocused']],'inputFocused':[(n.get('text'),n.get('description')) for n in s['nodes'] if n['focused']],'text':[n.get('text') or n.get('description') for n in s['nodes'] if n.get('text') or n.get('description')]}
def tap(s,label):
 for n in s['nodes']:
  if label in [n.get('text'),n.get('description')]:
   t=n
   while not t['clickable'] and t['parent']>=0:t=s['nodes'][t['parent']]
   if t['clickable'] and t['enabled'] and t['visible']:
    x1,y1,x2,y2=map(int,t['bounds'].split());adb('shell','input','tap',str((x1+x2)//2),str((y1+y2)//2));time.sleep(.8);return
 raise RuntimeError('No clickable target '+label)
def swipe_next():adb('shell','input','swipe','250','550','500','550','120');time.sleep(.5)
def double_tap():
 # Two physical taps within the TalkBack double-tap interval.
 adb('shell','input','tap','360','700');time.sleep(.07);adb('shell','input','tap','360','700');time.sleep(.8)
def focus_labels(s,kind='focused'):
 for node in s['nodes']:
  if node[kind]:
   descendants=[node];ids={node['index']}
   for item in s['nodes']:
    if item['parent'] in ids:ids.add(item['index']);descendants.append(item)
   return [n.get('text') or n.get('description') for n in descendants if n.get('text') or n.get('description')]
 return []
def kernel_next():
 adb('emu','event','mouse','80','270','0','1')
 for x in [110,150,200,240]:adb('emu','event','mouse',str(x),'270','0','1');time.sleep(.012)
 adb('emu','event','mouse','240','270','0','0');time.sleep(.6)
def kernel_touch(x,y,explore=False):
 adb('emu','event','mouse',str(x),str(y),'0','1');time.sleep(.4 if explore else .035);adb('emu','event','mouse',str(x),str(y),'0','0')
def kernel_double(x,y):
 kernel_touch(x,y);time.sleep(.07);kernel_touch(x,y);time.sleep(.8)
