"""Deterministic original vector construction of the owner-approved three-key mark.

Never reads/crops/embeds the private owner PNG. Shared paths drive supersampled
production layers, legacy densities and mask previews. Pillow is existing tooling.
"""
from pathlib import Path
from xml.sax.saxutils import escape
from PIL import Image, ImageDraw, ImageChops

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "app/src/main/res"
OUT = ROOT / ".gradle/gould-012-icon"
SCALE = 16

OUTER = [("M", 34, 29), ("L", 74, 29), ("C", 78, 29, 80, 32, 80, 35),
         ("L", 80, 73), ("C", 80, 77, 77, 79, 74, 79), ("L", 34, 79),
         ("C", 30, 79, 28, 77, 28, 73), ("L", 28, 35), ("C", 28, 31, 30, 29, 34, 29), ("Z",)]

def black_key(center):
    a, b = center - 4.7, center + 4.7
    return [("M", a, 29), ("L", b, 29), ("L", b, 51.5),
            ("C", b, 53.1, b + .6, 54, b + 1.2, 54.5),
            ("C", b - .2, 54.9, b - .2, 57.8, b - 2.4, 57.8),
            ("L", a + 2.4, 57.8), ("C", a + .2, 57.8, a + .2, 54.9, a - 1.2, 54.5),
            ("C", a - .6, 54, a, 53.1, a, 51.5), ("Z",)]

KEYS = [black_key(44.4), black_key(63.6)]

def polygon(path):
    points, at, origin = [], None, None
    for c in path:
        if c[0] == "M": at = origin = (c[1], c[2]); points.append(at)
        elif c[0] == "L": at = (c[1], c[2]); points.append(at)
        elif c[0] == "C":
            x, y = at
            for i in range(1, 49):
                t = i / 48; u = 1-t
                points.append((u**3*x + 3*u*u*t*c[1] + 3*u*t*t*c[3] + t**3*c[5],
                               u**3*y + 3*u*u*t*c[2] + 3*u*t*t*c[4] + t**3*c[6]))
            at = (c[5], c[6])
        elif c[0] == "Z": points.append(origin); at = origin
    return [(round(x*SCALE), round(y*SCALE)) for x,y in points]

def mask(path):
    image = Image.new("L", (108*SCALE, 108*SCALE)); ImageDraw.Draw(image).polygon(polygon(path), fill=255)
    return image

def layer():
    image = Image.new("RGBA", (108*SCALE,108*SCALE))
    base = mask(OUTER)
    image.paste("#FFF7E5", (0,0,image.width,image.height), base)
    draw = ImageDraw.Draw(image)
    draw.line(polygon(OUTER), fill="#C6A36A", width=round(.35*SCALE), joint="curve")
    # Three ivory lower keys, with restrained gold seams and two sculpted black keys.
    for x in [44.4, 63.6]:
        draw.line([(round(x*SCALE),round(57.7*SCALE)),(round(x*SCALE),round(78.8*SCALE))],fill="#C5A16B",width=round(.55*SCALE))
    for key in KEYS:
        draw.polygon(polygon(key),fill="#252422")
        draw.line(polygon(key),fill="#BFA171",width=round(.35*SCALE),joint="curve")
    return image

def path_data(path):
    return " ".join(c[0] + ",".join(str(v) for v in c[1:]) for c in path)

def main():
    OUT.mkdir(parents=True,exist_ok=True)
    hi = layer(); foreground = hi.resize((432,432),Image.Resampling.LANCZOS)
    # Suppress subpixel alpha ringing outside the documented 72dp circular safe area.
    safe = Image.new("L",(432,432)); ImageDraw.Draw(safe).ellipse((72,72,360,360),fill=255)
    foreground.putalpha(ImageChops.multiply(foreground.getchannel("A"),safe))
    mono = mask(OUTER)
    for key in KEYS: mono = ImageChops.subtract(mono,mask(key))
    d=ImageDraw.Draw(mono)
    for x in [44.4,63.6]:d.line([(round(x*SCALE),round(57.7*SCALE)),(round(x*SCALE),79*SCALE)],fill=0,width=round(.65*SCALE))
    monochrome=Image.new("RGBA",mono.size,"white");monochrome.putalpha(mono)
    monochrome=monochrome.resize((432,432),Image.Resampling.LANCZOS)
    monochrome.putalpha(ImageChops.multiply(monochrome.getchannel("A"),safe))
    for directory, file, image in [("drawable-nodpi","ic_goldberg_classic_foreground.png",foreground),
                                    ("drawable-night-nodpi","ic_goldberg_classic_foreground.png",foreground),
                                    ("drawable-nodpi","ic_goldberg_classic_monochrome.png",monochrome)]:
        target=RES/directory/file;target.parent.mkdir(parents=True,exist_ok=True);image.save(target,optimize=True)
    # Retain editable native paths alongside production rasters for exact reproduction.
    (ROOT/"docs/design/gould-0.12/launcher-mark.svg").write_text(
        '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108">'+
        '<path d="'+escape(path_data(OUTER))+'" fill="#fff7e5" stroke="#c6a36a" stroke-width=".35"/>'+
        ''.join('<path d="'+escape(path_data(k))+'" fill="#252422" stroke="#bfa171" stroke-width=".35"/>' for k in KEYS)+
        '<path d="M44.4,57.8V79 M63.6,57.8V79" stroke="#c5a16b" stroke-width=".55"/></svg>\n')
    # Pre-adaptive density fallbacks show the same central composition and ivory backplate.
    for density,size in [("mdpi",48),("hdpi",72),("xhdpi",96),("xxhdpi",144),("xxxhdpi",192)]:
        plate=Image.new("RGBA",(72*SCALE,72*SCALE));ImageDraw.Draw(plate).rounded_rectangle((0,0,72*SCALE-1,72*SCALE-1),radius=16*SCALE,fill="#FDFCF8")
        plate.alpha_composite(hi.crop((18*SCALE,18*SCALE,90*SCALE,90*SCALE)))
        target=RES/("mipmap-"+density)/"ic_goldberg.png";target.parent.mkdir(exist_ok=True);plate.resize((size,size),Image.Resampling.LANCZOS).save(target,optimize=True)
    board=Image.new("RGB",(960,360),"#d8d5cf")
    for i,kind in enumerate(["circle","rounded-square","squircle"]):
        plate=Image.new("RGBA",(288,288),"#FDFCF8")
        plate.alpha_composite(hi.crop((18*SCALE,18*SCALE,90*SCALE,90*SCALE)).resize((288,288),Image.Resampling.LANCZOS))
        clip=Image.new("L",plate.size);d=ImageDraw.Draw(clip)
        if kind=="circle":d.ellipse((0,0,287,287),fill=255)
        else:d.rounded_rectangle((0,0,287,287),radius=65 if kind=="rounded-square" else 95,fill=255)
        board.paste(plate,(16+i*320,24),clip);ImageDraw.Draw(board).text((20+i*320,325),kind,fill="black")
    board.save(OUT/"masks.png")
    print("Generated native-path mark, adaptive/monochrome rasters, five legacy densities and mask preview. No private pixels consumed.")

if __name__ == "__main__": main()
