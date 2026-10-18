#!/usr/bin/env python3
"""Offline Stage 2 scope, fixture and local-document verification."""
import hashlib,json,re,subprocess
from pathlib import Path
import research
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[3]
BASE='c3d5e03b9a4936d8db393df664af209a8853c767'
subprocess.run(['git','merge-base','--is-ancestor',BASE,'HEAD'],cwd=ROOT,check=True)
files=list(HERE.rglob('*.json'))
for p in files:json.loads(p.read_text())
authored=['catalogue.json','bundles.json','backup-v5-envelope.json']
before={name:(HERE/'fixtures'/name).read_bytes() for name in authored}
research.fixtures()
assert all((HERE/'fixtures'/name).read_bytes()==data for name,data in before.items()),'Non-deterministic authored fixtures'
research.check()
count=0
for p in [*HERE.rglob('*.md'),HERE.parent/'README.md',HERE.parent/'STAGES.md']:
 s=p.read_text();assert s.count('```')%2==0,p
 for href in re.findall(r'(?<!!)\[[^\]]+\]\(([^\s)]+)\)',s):
  if href.startswith(('https://','http://','#','mailto:')):continue
  target=(p.parent/href.split('#')[0]).resolve();assert target.is_relative_to(ROOT) and target.exists(),(p,href);count+=1
changed=subprocess.check_output(['git','diff','--name-only',BASE],cwd=ROOT,text=True).splitlines()
unknown=subprocess.check_output(['git','ls-files','--others','--exclude-standard'],cwd=ROOT,text=True).splitlines()
allowed=lambda p:p.startswith('docs/planning/goldberg-0.11/stage-2/') or p in ['docs/planning/goldberg-0.11/README.md','docs/planning/goldberg-0.11/STAGES.md','docs/wiki/Changelog-and-Roadmap.md']
assert all(allowed(p) for p in changed),changed
assert all(allowed(p) or p=='RELEASE_NOTES.md' for p in unknown),unknown
assert not any(p.endswith(('.pyc','.apk','.db','.png','.jpg')) for p in changed+unknown),'Generated/private/binary material staged'
notes=ROOT/'RELEASE_NOTES.md'
if notes.exists():assert hashlib.sha256(notes.read_bytes()).hexdigest()=='db920f0b1ff88579b6bea9f9da567af16e6db0c7ce5b6439781ca1754fcd4b9b'
else:print('NOTE: unrelated owner release draft not present in this checkout')
apk=ROOT/'.gradle/release-artifacts/Goldberg-v0.10.1.apk'
if apk.exists():assert hashlib.sha256(apk.read_bytes()).hexdigest()=='854bbfebf617ec920df60d181a12e0dcbd2fed10052f2479903a78a290cef312'
else:print('NOTE: historical published APK not available for local digest comparison')
subprocess.run(['git','diff','--check'],cwd=ROOT,check=True)
print(f'PASS: {len(files)} JSON documents, deterministic authored fixtures, {count} local links, Stage 1 ancestry and isolated research scope')
