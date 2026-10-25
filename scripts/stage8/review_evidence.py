"""Create a review index and pixel observations; does not certify app acceptance."""
import json
from collections import Counter
from pathlib import Path
from PIL import Image, ImageDraw

ROOT=Path(__file__).resolve().parents[2]
OUT=ROOT/'docs/planning/goldberg-0.11/stage-8/results/first-technical-verification-2026-10-25'


def luminance(rgb):
    channels=[v/255 for v in rgb]
    linear=[v/12.92 if v<=.04045 else ((v+.055)/1.055)**2.4 for v in channels]
    return sum(x*y for x,y in zip(linear,[.2126,.7152,.0722]))


def contrast(a,b):
    values=sorted([luminance(a),luminance(b)])
    return (values[1]+.05)/(values[0]+.05)


states=[]
for path in sorted(OUT.rglob('*-settings.json')):
    state=json.loads(path.read_text())
    picture=path.with_suffix('.png')
    assert picture.is_file(),picture
    im=Image.open(picture).convert('RGB')
    assert im.size in [(720,1280),(824,1830)],im.size
    # Status-bar and gesture-indicator regions contain actual icons plus background.
    # Dominant distinct colors are observations, not a WCAG text conformance result.
    status=Counter(im.crop((im.width-220,0,im.width-40,44)).getdata())
    nav=Counter(im.crop((im.width//2-160,im.height-40,im.width//2+160,im.height)).getdata())
    status_colors=status.most_common(2)
    nav_colors=nav.most_common(2)
    states.append({'file':str(path.relative_to(OUT)),'locale':state['locale'],'fontScale':state['fontScale'],
        'theme':state['profileTheme'],'scaffold':state['scaffold'],'systemNight':state['systemNight'],
        'lightStatusBars':state['lightStatusBars'],'lightNavigationBars':state['lightNavigationBars'],
        'pixels':im.size,'statusDominantColors':status_colors,'navDominantColors':nav_colors,
        'statusDominantPairContrast':contrast(status_colors[0][0],status_colors[1][0]) if len(status_colors)>1 else None,
        'navDominantPairContrast':contrast(nav_colors[0][0],nav_colors[1][0]) if len(nav_colors)>1 else None})
(OUT/'render-index.json').write_text(json.dumps(states,indent=2)+'\n')

for group,files in [('small',[p for p in sorted(OUT.glob('*-settings.png')) if 'small-navhost' in p.name or p.name=='en-light-1-settings.png']),('standard',[p for p in sorted(OUT.glob('*standard-navhost-settings.png'))]),('scaffold',[p for p in sorted(OUT.glob('*scaffold-settings.png'))])]:
    for i in range(0,len(files),4):
        sample=files[i:i+4]
        canvas=Image.new('RGB',(1440,2*1308),'#dddddd');draw=ImageDraw.Draw(canvas)
        for index,p in enumerate(sample):
            x=(index%2)*720;y=(index//2)*1308
            draw.text((x+5,y+5),p.stem,fill='black')
            im=Image.open(p).convert('RGB');im.thumbnail((720,1280));canvas.paste(im,(x,y+28))
        canvas.save(OUT/f'review-{group}-{i//4+1}.jpg',quality=90)
    if files:
        canvas=Image.new('RGB',(824,len(files)*240),'#dddddd');draw=ImageDraw.Draw(canvas)
        for index,p in enumerate(files):
            y=index*240;draw.text((5,y+5),p.stem,fill='black')
            im=Image.open(p).convert('RGB');canvas.paste(im.crop((0,im.height-208,im.width,im.height)),(0,y+25))
        canvas.save(OUT/f'review-{group}-navigation.jpg',quality=95)
print('Evidence indexed:',len(states),'settings states; contact sheets are derivatives, originals retained.')
