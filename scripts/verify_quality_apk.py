"""Release gate: compare actual APK certificates, metadata, archives and supplied artwork."""
import hashlib
import io
from pathlib import Path
import re
import subprocess
import sys
import zipfile
from PIL import Image

root=Path(__file__).resolve().parents[1]
sdk=Path('/home/stokonn/Android/Sdk/build-tools/36.0.0')
baseline,candidate=map(Path,sys.argv[1:3])
expected_version=sys.argv[3] if len(sys.argv)>3 else "0.9.1"
expected_code=int(sys.argv[4]) if len(sys.argv)>4 else 8
trusted='53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e'
def cert(path):
    result=subprocess.check_output([str(sdk/'apksigner'),'verify','--print-certs',str(path)],text=True)
    return re.search(r'certificate SHA-256 digest: (\w+)',result).group(1)
assert cert(baseline)==cert(candidate)==trusted,'BLOCKED: certificate mismatch'
def metadata(path):
    return subprocess.check_output([str(sdk/'aapt'),'dump','badging',str(path)],text=True)
old,new=metadata(baseline),metadata(candidate)
assert "name='com.opus.piano'" in old and "name='com.opus.piano'" in new
old_code=int(re.search(r"versionCode='(\d+)'",old).group(1))
assert old_code<expected_code and f"versionCode='{expected_code}'" in new and f"versionName='{expected_version}'" in new
# Release resource optimization shortens ZIP paths. Resolve their actual resource
# table entries instead of assuming that debug resource filenames survive.
resources=subprocess.check_output([str(sdk/'aapt'),'dump','--values','resources',str(candidate)],text=True)
packaged={};configuration='';pending=None
for line in resources.splitlines():
    config=re.match(r'\s+config (.*):$',line)
    if config:
        configuration=re.sub(r'-v\d+','',config.group(1));pending=None
    entry=re.match(r'\s+resource 0x[0-9a-f]+ com\.opus\.piano:((?:drawable|mipmap)/ic_goldberg[^:]*):',line)
    if entry:pending=(entry.group(1),configuration)
    value=re.match(r'\s+\(string8\) "([^"]+)"',line)
    if value and pending:
        packaged[pending]=value.group(1);pending=None
with zipfile.ZipFile(candidate) as apk:
    assert apk.testzip() is None,'BLOCKED: invalid archive'
    for qualifier,resource in [('drawable-nodpi','ic_goldberg_classic_foreground.png'),('drawable-night-nodpi','ic_goldberg_classic_foreground.png'),('drawable-nodpi','ic_goldberg_classic_monochrome.png')]:
        source=root/'app/src/main/res'/qualifier/resource
        # Android adds platform version qualifiers while packaging PNGs.
        matches=[n for n in apk.namelist() if n.startswith('res/'+qualifier) and n.endswith('/'+resource)]
        if not matches:
            path=packaged.get(('drawable/'+resource.removesuffix('.png'),qualifier.removeprefix('drawable-')))
            matches=[path] if path in apk.namelist() else []
        assert len(matches)==1,(qualifier,resource,matches)
        expected=Image.open(source).convert('RGBA'); actual=Image.open(io.BytesIO(apk.read(matches[0]))).convert('RGBA')
        assert actual.size==expected.size==(432,432)
        assert actual.tobytes()==expected.tobytes(),'BLOCKED: artwork differs in APK'
        alpha=actual.getchannel('A')
        assert alpha.getextrema()==(0,255)
        # All foreground pixels fit the central 72dp circular mask; rounded square has more room.
        for y in range(432):
            for x in range(432):
                if alpha.getpixel((x,y))>0:
                    assert (x-216)**2+(y-216)**2<=144**2,'BLOCKED: adaptive circle clipping'
        print('PASS: packaged transparent artwork and circular/rounded-square bounds:',matches[0])
    assert any(name=='mipmap/ic_goldberg' and path.endswith('.xml') and path in apk.namelist() for (name,_),path in packaged.items()),'BLOCKED: adaptive launcher icon absent'
print(f'PASS: com.opus.piano {expected_version} ({expected_code}), published baseline code',old_code)
print('Signing SHA-256:',trusted)
print('APK SHA-256:',hashlib.sha256(candidate.read_bytes()).hexdigest())
