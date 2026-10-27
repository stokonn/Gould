import argparse,subprocess,json,pathlib,hashlib,zipfile,shutil,gzip,xml.etree.ElementTree as E,sqlite3
p=argparse.ArgumentParser();p.add_argument('run');a=p.parse_args();run=a.run;repo='stokonn/Goldberg';sha='fb1d3f971ed79de09dccbdc771e1c45a09dc0a6c'
def gh(*args):return subprocess.check_output(['gh',*args],text=True)
state=json.loads(gh('api',f'repos/{repo}/actions/runs/{run}'));assert state['head_sha']==sha
art=json.loads(gh('api',f'repos/{repo}/actions/runs/{run}/artifacts'));assert art['total_count']==2,art
cache=pathlib.Path('/tmp/goldberg-final-run-'+run);cache.mkdir(exist_ok=True)
out=pathlib.Path('docs/planning/goldberg-0.11/stage-7/stage-7b/results/ci-run-'+run);out.mkdir(exist_ok=True)
(out/'run.json').write_text(json.dumps(state,indent=2)+'\n');(out/'artifacts.json').write_text(json.dumps(art,indent=2)+'\n');archives=[]
for item in art['artifacts']:
 kind='instrumentation' if 'instrumentation' in item['name'] else 'upgrade';zpath=cache/(kind+'.zip')
 if not zpath.exists():
  with zpath.open('wb') as stream:subprocess.run(['gh','api',f'repos/{repo}/actions/artifacts/{item["id"]}/zip'],stdout=stream,check=True)
 h=hashlib.sha256(zpath.read_bytes()).hexdigest();assert item['digest']=='sha256:'+h,(zpath,h,item['digest'])
 raw=cache/kind;raw.mkdir(exist_ok=True);members=[]
 with zipfile.ZipFile(zpath) as z:
  assert z.testzip() is None
  for n in z.namelist():assert not n.startswith('/') and '..' not in pathlib.PurePosixPath(n).parts,n
  z.extractall(raw)
  for n in z.namelist():
   if not n.endswith('/'):members.append({'path':n,'bytes':len(z.read(n)),'sha256':hashlib.sha256(z.read(n)).hexdigest()})
 archives.append({'id':item['id'],'kind':kind,'zip':str(zpath),'sha256':h,'members':members})
 if kind=='instrumentation':
  evidence=raw/'stage5-ci-results';assert sha in (evidence/'source-commit.txt').read_text()
  shutil.copytree(evidence/'admin-evidence',out/'admin-evidence',dirs_exist_ok=True)
  for file in evidence.iterdir():
   if file.is_file():
    if file.name=='logcat.txt':
     with gzip.open(out/'logcat.txt.gz','wb') as dst:dst.write(file.read_bytes())
    else:shutil.copy(file,out/file.name)
  (out/'instrumentation').mkdir(exist_ok=True)
  for file in (raw/'app/build/outputs/androidTest-results/connected/debug').glob('*.xml'):shutil.copy(file,out/'instrumentation'/file.name)
 else:
  assert sha in (raw/'source-commit.txt').read_text();shutil.copytree(raw,out/'raw-upgrade',dirs_exist_ok=True)
  (out/'upgrade').mkdir(exist_ok=True)
  for n in ['source-commit.txt','baseline-source.txt','signing-environment.txt','signing-evidence.txt']:shutil.copy(raw/n,out/'upgrade'/n)
  for n in ['before.json','after.json','result.json']:shutil.copy(raw/'upgrade'/n,out/'upgrade'/n)
counts={k:0 for k in ['tests','failures','errors','skipped']};cases=[]
for file in (out/'instrumentation').glob('*.xml'):
 t=E.parse(file).getroot()
 for k in counts:counts[k]+=int(t.get(k,0))
 for c in t.iter('testcase'):
  if 'Stage7b' in c.get('classname','') or c.find('skipped') is not None or c.find('failure') is not None:
   cases.append({'class':c.get('classname'),'name':c.get('name'),'status':'failure' if c.find('failure') is not None else 'skipped' if c.find('skipped') is not None else 'pass','details':E.tostring(c,encoding='unicode') if c.find('failure') is not None or c.find('skipped') is not None else None})
before=json.loads((out/'upgrade/before.json').read_text());after=json.loads((out/'upgrade/after.json').read_text());assert before=={k:after[k] for k in before}
dbs=[]
for n,v in [('before',8),('after',10)]:
 with sqlite3.connect(f'file:{out}/raw-upgrade/upgrade/{n}.db?mode=ro',uri=True) as c:
  ver=c.execute('pragma user_version').fetchone()[0];integrity=c.execute('pragma integrity_check').fetchall();fk=c.execute('pragma foreign_key_check').fetchall();assert ver==v and integrity==[('ok',)] and not fk
  dbs.append({'database':n,'userVersion':ver,'integrity':'ok','foreignKeyViolations':0})
summary={'run':run,'source':sha,'junit':counts,'selectedCases':cases,'personalCollectionsEqual':len(before),'databases':dbs,'upgradeResult':json.loads((out/'upgrade/result.json').read_text()),'pngFiles':len(list((out/'admin-evidence').glob('*.png'))),'treeTextFiles':len(list((out/'admin-evidence').glob('*.txt'))),'archives':[{k:v for k,v in x.items() if k!='members'} for x in archives],'result':'PASS' if counts['failures']==counts['errors']==0 else 'FAIL'}
(out/'independent-inventory.json').write_text(json.dumps(summary,indent=2)+'\n');(out/'archive-member-inventory.json').write_text(json.dumps(archives,indent=2)+'\n');print(json.dumps(summary,indent=2))
