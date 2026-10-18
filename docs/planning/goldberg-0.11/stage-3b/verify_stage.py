#!/usr/bin/env python3
"""Stage 3B source/artifact, protected-history and documentation checks (no user stores)."""
import argparse,hashlib,json,re,subprocess
from pathlib import Path
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[3];BASE='cb36535d8a733143213347dd2ce355d1bf81ae96'
p=argparse.ArgumentParser();p.add_argument('--record',action='store_true');a=p.parse_args()
def git(*args):return subprocess.check_output(['git',*args],cwd=ROOT)
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
assert subprocess.run(['git','merge-base','--is-ancestor',BASE,'HEAD'],cwd=ROOT).returncode==0
protected=git('ls-tree','-r','--name-only',BASE,'app/schemas','app/src/androidTest/assets/backups','app/src/androidTest/assets/stage3a','app/src/androidTest/assets/schemas').decode().splitlines()
protected+=['app/build.gradle.kts','build.gradle.kts','settings.gradle.kts','app/src/main/AndroidManifest.xml']
for name in protected:assert (ROOT/name).read_bytes()==git('show',f'{BASE}:{name}'),name
assert (ROOT/'app/src/androidTest/assets/schemas/9.json').read_bytes()==(ROOT/'app/schemas/com.opus.piano.data.OpusDatabase/9.json').read_bytes()
notes=ROOT/'RELEASE_NOTES.md';assert sha(notes)=='db920f0b1ff88579b6bea9f9da567af16e6db0c7ce5b6439781ca1754fcd4b9b'
assert 'RELEASE_NOTES.md' not in git('diff','--cached','--name-only').decode().splitlines()
for path in (ROOT/'app/src/main').rglob('*.kt'):assert 'fallbackToDestructiveMigration' not in path.read_text(),path
apk=ROOT/'app/build/outputs/apk/debug/app-debug.apk';testapk=ROOT/'app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk'
manifest=HERE/'results/source-artifact.json'
paths=sorted(p for base in ['app/src/main','app/src/test','app/src/androidTest','app/schemas'] for p in (ROOT/base).rglob('*') if p.is_file())
paths+=[ROOT/p for p in ['app/build.gradle.kts','build.gradle.kts','settings.gradle.kts','gradle.properties']]
if a.record:
 for path in paths:
  artifact=testapk if 'androidTest' in path.parts else apk
  assert path.stat().st_mtime<=artifact.stat().st_mtime,(path,'newer than artifact; rebuild first')
 manifest.write_text(json.dumps(dict(base=BASE,apkSha256=sha(apk),instrumentationApkSha256=sha(testapk),files={str(p.relative_to(ROOT)):sha(p) for p in paths}),indent=2)+'\n')
data=json.loads(manifest.read_text());assert sha(apk)==data['apkSha256'];assert sha(testapk)==data['instrumentationApkSha256']
for name,digest in data['files'].items():assert sha(ROOT/name)==digest,name
links=0
for path in HERE.glob('*.md'):
 content=path.read_text();assert content.count('```')%2==0,path
 for href in re.findall(r'(?<!!)\[[^\]]+\]\(([^\s)]+)\)',content):
  if href.startswith(('http://','https://','#','mailto:')):continue
  target=(path.parent/href.split('#')[0]).resolve();assert target.is_relative_to(ROOT) and target.exists(),(path,href);links+=1
subprocess.run(['git','diff','--check'],cwd=ROOT,check=True)
print(f'PASS Stage3A ancestry, {len(protected)} protected paths, {len(paths)} source/test/schema hashes, both APKs, {links} links and owner release draft')
