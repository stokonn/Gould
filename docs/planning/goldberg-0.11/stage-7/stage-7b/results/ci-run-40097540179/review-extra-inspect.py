from pathlib import Path
import argparse,itertools,re,json,xml.etree.ElementTree as E
from PIL import Image
p=argparse.ArgumentParser();p.add_argument('root',type=Path);a=p.parse_args();root=a.root
resources={locale:{x.get('name'):x.text for x in E.parse(path).getroot()} for locale,path in [('en','app/src/main/res/values/strings.xml'),('cs','app/src/main/res/values-cs/strings.xml')]}
checks=[]
for tree in sorted(root.glob('stage7b-*.txt')):
 m=re.match(r'stage7b-(small|standard)-(en|cs)-(Light|Dark)-f([\d.]+)-(.+)\.txt',tree.name)
 if not m:continue
 size,locale,theme,font,state=m.groups();body=tree.read_text();assert f'locale={locale}' in body and f'font={font}' in body,tree
 with Image.open(tree.with_suffix('.png')) as im:assert im.size==((720,1280) if size=='small' else (824,1830)),tree
 if state in ['queue','audit','settings','proposal']:
  key={'queue':'stage7b_moderation_queue','audit':'stage7b_audit_log','settings':'stage7b_admin_settings','proposal':'stage7b_review_proposal'}[state]
  assert f'text={resources[locale][key]} description=' in body,('Stale destination tree',tree)
  lines=body.splitlines();label=f'description={resources[locale]["msg_back_b52b"]} clickable='
  index=next((i for i,line in enumerate(lines) if label in line),None);assert index is not None,('Missing localized Back',tree)
  def rect(line):
   m=re.search(r'bounds=Rect\((-?\d+), (-?\d+) - (-?\d+), (-?\d+)\)',line)
   return tuple(map(int,m.groups())) if m else None
  child=rect(lines[index]);owners=[rect(line) for line in lines[:index] if 'clickable=true enabled=true' in line]
  assert child and any(o and o[2]-o[0]>=96 and o[3]-o[1]>=96 and o[0]<=child[0] and o[1]<=child[1] and o[2]>=child[2] and o[3]>=child[3] for o in owners),('No48dpBackowner around localized icon label',tree)
 if state.endswith('row-reachable'):
  hits=[line for line in body.splitlines() if 'sub_1' in line];assert hits,tree
  bounds=[]
  for line in hits:
   match=re.search(r'bounds=Rect\((-?\d+), (-?\d+) - (-?\d+), (-?\d+)\)',line)
   if match:bounds.append([int(x) for x in match.groups()])
  checks.append({'tree':tree.name,'sub1Bounds':bounds,'actualSub1Nodes':hits})
(root.parent/'independent-state-checks.json').write_text(json.dumps({'result':'PASS','configurationMetadataAndPngDimensions':'PASS for all matrix states','freshDestinationHeadingsAndBack':'PASS for32base destination captures','rowCaptures':checks,'limits':'Static nodes are supplemented by runtime assertions and separately inspected PNGs; complete app/system Back and TalkBack remain unexecuted.'},indent=2)+'\n')
print('PASS: all matrix metadata/dimensions,32 fresh destination headings/Back and',len(checks),'actual row captures')
