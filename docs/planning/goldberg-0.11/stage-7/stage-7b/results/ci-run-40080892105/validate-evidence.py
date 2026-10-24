#!/usr/bin/env python3
"""Independent retained-artifact checks; screenshots still need human/model inspection."""
import argparse, hashlib, itertools, json, re
from pathlib import Path
from PIL import Image
parser=argparse.ArgumentParser()
parser.add_argument('evidence',type=Path)
parser.add_argument('--output',type=Path,required=True)
a=parser.parse_args(); root=a.evidence
assert (root/'matrix-results.txt').read_text().strip()=='PASS: all configurations completed',(root/'matrix-results.txt').read_text()
expected=[f'stage7b-{s}-{l}-{t}-f{f}' for s,l,t,f in itertools.product(['small','standard'],['en','cs'],['Light','Dark'],['1.0','1.3','1.5','2.0'])]
inventory=[]
for prefix in expected:
 p=root/(prefix+'-dashboard.txt');body=p.read_text()
 assert 'font='+prefix.split('-f')[1] in body,p
 assert ('locale='+prefix.split('-')[2]) in body,p
 if float(prefix.split('-f')[1])<=1.3:assert 'scrollable=true' not in body,p
 else:
  footer=root/(prefix+'-dashboard-reachable-footer.txt')
  assert ('description=Refresh' if '-en-' in prefix else 'description=Obnovit') in footer.read_text(),footer
 with Image.open(p.with_suffix('.png')) as im:
  expected_size=(720,1280) if '-small-' in prefix else (824,1830)
  assert im.size==expected_size,(p,im.size)
 inventory.append({'configuration':prefix,'dashboardPngSha256':hashlib.sha256(p.with_suffix('.png').read_bytes()).hexdigest(),'treeSha256':hashlib.sha256(p.read_bytes()).hexdigest()})
required={'stage7b-checkables-en','stage7b-checkables-cs'}
for size,locale,theme,font in itertools.product(['small','standard'],['en','cs'],['Light','Dark'],['1.0','1.3','1.5','2.0']):
 prefix=f'stage7b-{size}-{locale}-{theme}-f{font}';required.add(prefix+'-dashboard')
 if float(font)>1.3:required.add(prefix+'-dashboard-reachable-footer')
 if (size=='standard' and font=='1.0') or (size=='small' and font=='2.0'):
  for destination in ['queue','proposal','audit','settings']:required.update([prefix+'-'+destination,prefix+'-'+destination+'-bottom'])
  for mode in ['empty','error','loading']:
   for destination in ['dashboard','queue','audit']:required.add(prefix+'-'+destination+'-'+mode)
  if font=='1.0':
   required.update(prefix+'-'+name for name in ['queue-stale-update','re-review-incomplete-history','metered-confirmation','endpoint-keyboard','endpoint-validation'])
   required.update(prefix+'-proposal-tab-'+tab for tab in ['details','history','evidence','comparison'])
   required.update(prefix+'-confirm-'+decision for decision in ['approve','reject','request_changes'])
   required.update(prefix+'-confirm-'+decision+'-reason-keyboard' for decision in ['reject','request_changes'])
for name in required:
 assert (root/(name+'.png')).is_file() and (root/(name+'.txt')).is_file(),('Missing required state capture',name)
assert {p.stem for p in root.glob('*.png')}==required,'Missing or unexpected screenshot state'
nodes=0;trees=sorted(root.glob('*.txt'))
for p in trees:
 for line in p.read_text().splitlines():
  if 'checkable=true' not in line:continue
  nodes+=1;m=re.search(r'text=(.*?) description=(.*?) clickable=',line)
  assert m and any(v.strip() not in ('','null') for v in m.groups()),(p,line)
  if 'enabled=true' in line:assert 'focusable=true' in line,(p,'Enabled checkable lost input focus',line)
  else:assert 'focusable=false' in line,(p,'Disabled checkable unexpectedly focusable',line)
for p in root.glob('*.png'):
 with Image.open(p) as im:im.verify()
a.output.write_text(json.dumps({'configurations':inventory,'treeFiles':len(trees),'namedCheckableOccurrences':nodes,'pngFiles':len(list(root.glob('*.png'))),'enabledCheckableFocus':'PASS','requiredScreenshots':len(required),'result':'PASS','limits':'Static artifact checks do not establish TalkBack, complete navigation, platform permission revocation or live backend integration.'},indent=2)+'\n')
print('PASS:',len(expected),'dashboard configurations;',len(trees),'tree/text files;',nodes,'named/focus-checked checkable occurrences; all required state pairs and PNG integrity verified')
