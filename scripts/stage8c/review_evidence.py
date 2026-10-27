"""Derivative review sheets and observed pixel contrast; originals remain authoritative."""
from pathlib import Path
from collections import Counter
import json
from PIL import Image,ImageDraw

ROOT=Path(__file__).resolve().parents[2]
OUT=ROOT/'docs/planning/goldberg-0.11/stage-8/results/targeted-ui-corrections-2026-10-25'
REVIEW=OUT/'review';REVIEW.mkdir(exist_ok=True)

def luminance(rgb):
    c=[v/255 for v in rgb];c=[v/12.92 if v<=.04045 else ((v+.055)/1.055)**2.4 for v in c]
    return sum(x*y for x,y in zip(c,[.2126,.7152,.0722]))

def contrast(a,b):
    a,b=sorted([luminance(a),luminance(b)]);return (b+.05)/(a+.05)

for size in ['small','standard']:
    out=Image.new('RGB',(1440,1320),'#888888');d=ImageDraw.Draw(out);i=0
    for locale in ['en','cs']:
        for theme in ['light','dark']:
            for font in ['1p0','1p3','1p5','2p0']:
                f=OUT/'after'/f'{locale}-{theme}-{font}-{size}-navhost-profile.png'
                im=Image.open(f);crop=im.crop((0,im.height-580,im.width,im.height));crop.thumbnail((360,290))
                x=(i%4)*360;y=(i//4)*330;out.paste(crop,(x,y+35));d.text((x+5,y+10),f'{locale} {theme} font{font} {size}',fill='white');i+=1
    out.save(REVIEW/f'navigation-{size}.png')

observations=[]
for base in [OUT/'before',OUT/'after',OUT/'after/opposite-system']:
    for f in sorted(base.glob('*-settings.png')):
        im=Image.open(f).convert('RGB');state=json.loads(f.with_suffix('.json').read_text())
        status=Counter(im.crop((im.width-220,0,im.width-40,44)).getdata()).most_common(2)
        nav=Counter(im.crop((im.width//2-160,im.height-40,im.width//2+160,im.height)).getdata()).most_common(2)
        observations.append({'file':str(f.relative_to(OUT)),'statusDominantColors':status,'navDominantColors':nav,'statusDominantPairContrast':contrast(status[0][0],status[1][0]),'navDominantPairContrast':contrast(nav[0][0],nav[1][0]),'theme':state['profileTheme'],'systemNight':state['systemNight'],'scaffold':state['scaffold'],'lightStatusBars':state['lightStatusBars'],'lightNavigationBars':state['lightNavigationBars']})
(REVIEW/'pixel-observations.json').write_text(json.dumps({'method':'Two dominant distinct RGB colors in status-icon/gesture-indicator regions;observations,not a text/WCAG or physical-device certification.','observations':observations},indent=2)+'\n')

out=Image.new('RGB',(1440,650),'#888888');d=ImageDraw.Draw(out)
for i,(loc,theme) in enumerate([(l,t) for l in ['en','cs'] for t in ['light','dark']]):
    for row,base,kind in [(0,OUT/'after','scaffold'),(1,OUT/'after/opposite-system','navhost')]:
        f=base/f'{loc}-{theme}-1p0-small-{kind}-settings.png';im=Image.open(f).resize((360,640))
        tile=Image.new('RGB',(360,260));tile.paste(im.crop((0,0,360,170)),(0,0));tile.paste(im.crop((0,550,360,640)),(0,170));out.paste(tile,(i*360,35+row*310));d.text((i*360+5,10+row*310),f'{loc} {theme} {kind} opposite system',fill='white')
out.save(REVIEW/'system-bars.png')
files=[OUT/'before/en-light-1p0-small-navhost-focus-3.png',OUT/'before/en-light-1p0-small-navhost-focus-4.png',OUT/'after/en-light-1p0-small-navhost-focus-2.png',OUT/'after/cs-dark-1p3-small-navhost-focus-2.png']
out=Image.new('RGB',(1440,675),'#888888');d=ImageDraw.Draw(out)
for i,f in enumerate(files):
    im=Image.open(f).resize((360,640));out.paste(im,(360*i,35));d.text((360*i+5,10),str(f.relative_to(OUT)),fill='white')
out.save(REVIEW/'talkback-before-after.png')
print('Review sheets/pixel observations written;visual acceptance requires inspection.')

# CI1's eight Settings samples are independent scaffold renders, not real NavHost.
ci1=OUT/'ci-native-40168644562-failed/stage5-ci-results/admin-evidence'
files=sorted(ci1.glob('*settings.png'))
if files:
    assert len(files)==8
    out=Image.new('RGB',(1440,660),'#888888');d=ImageDraw.Draw(out)
    for i,f in enumerate(files):
        im=Image.open(f);crop=im.crop((0,im.height-580,im.width,im.height));crop.thumbnail((360,290))
        x=i%4*360;y=i//4*330;out.paste(crop,(x,y+35));d.text((x+5,y+10),f.stem,fill='white')
    out.save(REVIEW/'ci1-scaffold-settings-navigation.png')
