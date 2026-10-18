#!/usr/bin/env python3
"""Stage 3A offline evidence/compatibility/document checks; never modifies user stores."""
import hashlib
import json
from pathlib import Path
import re
import subprocess

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[3]
BASE = 'd6049add3e46399d8f48afcb2aaa03422ebe0017'
subprocess.run(['git','merge-base','--is-ancestor',BASE,'HEAD'],cwd=ROOT,check=True)
# Historical schema exports and compatibility/build identities must not drift.
protected = subprocess.check_output(['git','ls-tree','-r','--name-only',BASE,'app/schemas'],cwd=ROOT,text=True).splitlines()
protected += ['app/build.gradle.kts','build.gradle.kts','settings.gradle.kts','app/src/main/AndroidManifest.xml']
for name in protected:
    original=subprocess.check_output(['git','show',f'{BASE}:{name}'],cwd=ROOT)
    assert (ROOT/name).read_bytes()==original,name
for path in HERE.rglob('*.json'):
    json.loads(path.read_text())
fixture=ROOT/'docs/planning/goldberg-0.11/stage-2/fixtures/catalogue.json'
assert fixture.read_bytes()==(ROOT/'app/src/androidTest/assets/stage3a/catalogue.json').read_bytes()
manifest=json.loads((HERE/'results/source-artifact.json').read_text())
for name,digest in manifest['files'].items():
    assert hashlib.sha256((ROOT/name).read_bytes()).hexdigest()==digest,name
apk=ROOT/'app/build/outputs/apk/debug/app-debug.apk'
if apk.exists():
    assert hashlib.sha256(apk.read_bytes()).hexdigest()==manifest['apkSha256']
notes=ROOT/'RELEASE_NOTES.md'
if notes.exists():
    assert hashlib.sha256(notes.read_bytes()).hexdigest()=='db920f0b1ff88579b6bea9f9da567af16e6db0c7ce5b6439781ca1754fcd4b9b'
assert 'RELEASE_NOTES.md' not in subprocess.check_output(['git','diff','--cached','--name-only'],cwd=ROOT,text=True).splitlines()
for path in (ROOT/'app/src/main').rglob('*.kt'):
    assert 'fallbackToDestructiveMigration' not in path.read_text(),path
links=0
for path in HERE.glob('*.md'):
    content=path.read_text();assert content.count('```')%2==0,path
    assert 'replace this line' not in content.lower(),path
    for href in re.findall(r'(?<!!)\[[^\]]+\]\(([^\s)]+)\)',content):
        if href.startswith(('http://','https://','#','mailto:')):continue
        target=(path.parent/href.split('#')[0]).resolve()
        assert target.is_relative_to(ROOT) and target.exists(),(path,href)
        links+=1
subprocess.run(['git','diff','--check'],cwd=ROOT,check=True)
print(f'PASS: Stage 2 ancestry, {len(protected)} protected files, source/APK identity, authored fixtures, {links} local links, owner draft and migration policy')
