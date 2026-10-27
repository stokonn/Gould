"""Stage 8B host recorder. Explicit isolated emulator; no implicit device selection."""
import argparse
import json
import os
from pathlib import Path
import subprocess
import time

SERIAL = 'emulator-5588'
ADB = str(Path(os.environ.get('ANDROID_HOME') or os.environ.get('ANDROID_SDK_ROOT') or Path.home() / 'Android/Sdk') / 'platform-tools/adb')
ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / 'docs/planning/goldberg-0.11/stage-8/results/first-technical-verification-2026-10-25'


def adb(*args, binary=False):
    result = subprocess.run([ADB, '-s', SERIAL, *args], capture_output=True, timeout=1300 if "instrument" in args else 45)
    with (OUT / 'host-commands.jsonl').open('a') as stream:
        stream.write(json.dumps({'args': args, 'code': result.returncode,
            'stdout': None if binary else result.stdout.decode(errors='replace'),
            'stderr': result.stderr.decode(errors='replace')}) + '\n')
    result.check_returncode()
    return result.stdout if binary else result.stdout.decode()


def guard():
    assert adb('shell', 'getprop', 'ro.kernel.qemu').strip() == '1'
    assert adb('shell', 'getprop', 'sys.boot_completed').strip() == '1'


def snap(name):
    assert name.replace('-', '').replace('_', '').isalnum()
    adb('shell', 'run-as', 'com.opus.piano', 'touch', 'files/manual-capture-' + name)
    for _ in range(100):
        done = subprocess.run([ADB, '-s', SERIAL, 'shell', 'run-as', 'com.opus.piano', 'test', '-e', 'files/manual-capture-' + name], capture_output=True)
        if done.returncode != 0:
            break
        time.sleep(.1)
    else:
        raise RuntimeError('Recorder did not consume snapshot request')
    body = adb('exec-out', 'run-as', 'com.opus.piano', 'cat', 'files/stage7b-manual/' + name + '.json')
    (OUT / (name + '.json')).write_text(body)
    (OUT / (name + '.png')).write_bytes(adb('exec-out', 'run-as', 'com.opus.piano', 'cat', 'files/stage7b-manual/' + name + '.png', binary=True))
    return json.loads(body)


def summary(state):
    return {'locale': state['locale'], 'font': state['fontScale'], 'theme': state['profileTheme'],
        'focus': [(x.get('text'), x.get('description')) for x in state['nodes'] if x['accessibilityFocused']],
        'labels': [x.get('text') or x.get('description') for x in state['nodes'] if x.get('text') or x.get('description')]}


def tap(state, label):
    for node in state['nodes']:
        if label in [node.get('text'), node.get('description')]:
            while not node['clickable'] and node['parent'] >= 0:
                node = state['nodes'][node['parent']]
            if node['clickable'] and node['enabled'] and node['visible']:
                x1, y1, x2, y2 = map(int, node['bounds'].split())
                adb('shell', 'input', 'tap', str((x1+x2)//2), str((y1+y2)//2))
                time.sleep(.6)
                return
    raise RuntimeError('No enabled visible target ' + label)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('name')
    args = parser.parse_args()
    guard()
    print(json.dumps(summary(snap(args.name)), ensure_ascii=False))
