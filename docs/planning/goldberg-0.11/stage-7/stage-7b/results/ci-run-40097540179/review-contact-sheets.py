from pathlib import Path
from PIL import Image,ImageDraw,ImageFont
import argparse,json,math
p=argparse.ArgumentParser();p.add_argument('root',type=Path);a=p.parse_args();root=a.root;out=root.parent/'visual-review';out.mkdir(exist_ok=True)
files=sorted(root.glob('*.png'));ordered=[]
for suffix in ['-dashboard.png','-dashboard-reachable-footer.png','-queue.png','-audit.png','-proposal.png','-settings.png','-queue-row-reachable.png','-audit-row-reachable.png','-re-review-acknowledged.png']:
 ordered.extend(x for x in files if x.name.endswith(suffix) and x not in ordered)
ordered.extend(x for x in files if x not in ordered);assert len(set(ordered))==len(files)
font=ImageFont.truetype('/usr/share/fonts/dejavu-sans-fonts/DejaVuSans.ttf',13) if Path('/usr/share/fonts/dejavu-sans-fonts/DejaVuSans.ttf').exists() else ImageFont.load_default()
manifest=[]
for n in range(0,len(ordered),8):
 batch=ordered[n:n+8];images=[]
 for path in batch:
  im=Image.open(path).convert('RGB');h=round(im.height*360/im.width);im=im.resize((360,h));images.append(im)
 cellheight=max(im.height for im in images)+52;sheet=Image.new('RGB',(1440,cellheight*2),'#dddddd');draw=ImageDraw.Draw(sheet)
 for i,(path,im) in enumerate(zip(batch,images)):
  x=(i%4)*360;y=(i//4)*cellheight;name=path.stem.removeprefix('stage7b-');name=name.replace('-f',' f',1)
  draw.text((x+3,y+3),name[:43],fill='black',font=font)
  if len(name)>43:draw.text((x+3,y+21),name[43:],fill='black',font=font)
  sheet.paste(im,(x,y+48))
 name=f'sheet-{n//8+1:02d}.jpg';sheet.save(out/name,quality=94);manifest.append({'sheet':name,'screenshots':[p.name for p in batch],'inspected':False})
(out/'manifest.json').write_text(json.dumps(manifest,indent=2)+'\n');print('Created',len(manifest),'sheets for',len(files),'screenshots',out)
