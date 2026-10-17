"""Small emulator-only UI evidence helper. Screenshots require human/agent inspection."""
import argparse
import os
from pathlib import Path
import re
import shutil
import subprocess
import time
import xml.etree.ElementTree as ET

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--serial', required=True)
parser.add_argument('--output', type=Path, required=True)
parser.add_argument('actions', nargs='+', help='tap:TEXT, scrolltap:TEXT, longpress:TEXT, shot:NAME, back, scroll')
args = parser.parse_args()
if not re.fullmatch(r'emulator-[0-9]+', args.serial):
    parser.error('Only an explicitly identified emulator-NNNN serial is allowed')
adb = shutil.which('adb') or str(Path(os.environ.get('ANDROID_HOME', str(Path.home() / 'Android/Sdk'))) / 'platform-tools/adb')
args.output.mkdir(parents=True, exist_ok=True)

def run(*command):
    return subprocess.check_output([adb, '-s', args.serial, *command])

def locate(label, attempts=5):
    for _ in range(attempts):
        result = run('shell', 'uiautomator', 'dump', '/sdcard/goldberg-verification.xml')
        if b'dumped to:' in result:
            root = ET.fromstring(run('exec-out', 'cat', '/sdcard/goldberg-verification.xml'))
            nodes = [n for n in root.iter('node') if label in (n.get('text'), n.get('content-desc'))]
            if nodes:
                bounds = list(map(int, re.findall(r'\d+', nodes[-1].get('bounds'))))
                return str((bounds[0]+bounds[2])//2), str((bounds[1]+bounds[3])//2)
        time.sleep(.3)
    raise RuntimeError('No visible control: ' + label)

def scroll():
    sizes = re.findall(rb'(\d+)x(\d+)', run('shell', 'wm', 'size'))
    width, height = map(int, sizes[-1])
    run('shell', 'input', 'swipe', str(width//2), str(height*3//4), str(width//2), str(height//4), '350')
    time.sleep(.4)

for action in args.actions:
    print(action, flush=True)
    if action == 'back':
        run('shell', 'input', 'keyevent', '4'); time.sleep(.5)
    elif action == 'scroll':
        scroll()
    elif action.startswith('shot:'):
        name = action[5:]
        if not re.fullmatch(r'[A-Za-z0-9_-]+', name):
            parser.error('Screenshot names must be simple file stems')
        time.sleep(.8)
        (args.output / (name+'.png')).write_bytes(run('exec-out', 'screencap', '-p'))
    elif action.startswith('longpress:'):
        x, y = locate(action.split(':', 1)[1])
        run('shell', 'input', 'swipe', x, y, x, y, '1200'); time.sleep(.7)
    elif action.startswith(('tap:', 'scrolltap:')):
        label = action.split(':', 1)[1]
        for attempt in range(8 if action.startswith('scrolltap:') else 1):
            try:
                x, y = locate(label, 1 if action.startswith('scrolltap:') else 5)
                run('shell', 'input', 'tap', x, y); time.sleep(.7); break
            except RuntimeError:
                if not action.startswith('scrolltap:') or attempt == 7: raise
                scroll()
    else:
        parser.error('Unknown action: ' + action)
