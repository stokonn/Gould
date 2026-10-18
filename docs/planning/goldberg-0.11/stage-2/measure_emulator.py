#!/usr/bin/env python3
"""Explicit emulator-only baseline. Force-stops/launches existing app; never installs/wipes."""
import argparse,hashlib,json,re,subprocess,tempfile,time
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--serial',required=True);p.add_argument('--adb',default='adb');p.add_argument('--output',type=Path,required=True);a=p.parse_args()
if not re.fullmatch(r'emulator-\d+',a.serial):raise SystemExit('Only explicit emulator serials accepted')
def run(*args):return subprocess.check_output([a.adb,'-s',a.serial,*args],text=True,timeout=35)
assert run('shell','getprop','ro.kernel.qemu').strip()=='1'
info=run('shell','dumpsys','package','com.opus.piano');assert 'versionName=0.10.1' in info and 'versionCode=11' in info
with tempfile.TemporaryDirectory(prefix='goldberg011-apk-identify-') as folder:
 target=Path(folder)/'base.apk';source=run('shell','pm','path','com.opus.piano').strip().split('package:')[1];run('pull',source,str(target));digest=hashlib.sha256(target.read_bytes()).hexdigest()
result={'serial':a.serial,'emulator':True,'package':'com.opus.piano','version':'0.10.1','code':11,'installed_apk_sha256':digest,'api':run('shell','getprop','ro.build.version.sdk').strip(),'abi':run('shell','getprop','ro.product.cpu.abi').strip(),'method':'five force-stopped process launches; existing disposable emulator data; no install, cold boot or disk cache flush','runs':[]}
for _ in range(5):
 run('shell','am','force-stop','com.opus.piano');start=run('shell','am','start','-W','-n','com.opus.piano/.MainActivity');time.sleep(2);mem=run('shell','dumpsys','meminfo','com.opus.piano')
 row={k:int(v) for k,v in re.findall(r'^(ThisTime|TotalTime|WaitTime):\s*(\d+)',start,re.M)};m=re.search(r'TOTAL PSS:\s*(\d+)',mem);row['pss_kib']=int(m.group(1)) if m else None;result['runs'].append(row)
a.output.write_text(json.dumps(result,indent=2)+'\n');print(json.dumps(result,indent=2))
