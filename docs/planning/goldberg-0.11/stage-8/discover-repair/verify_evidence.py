"""Validate source-bound native artifacts. This does not replace visual inspection.
Usage: python3 verify_evidence.py ARTIFACT_DIRECTORY EXPECTED_COMMIT
"""
from pathlib import Path
import json
import re
import struct
import sys
import xml.etree.ElementTree as ET
import zlib

root=Path(sys.argv[1]); expected=sys.argv[2]
assert re.fullmatch(r'[0-9a-f]{40}',expected), 'Expected a full immutable source SHA'
bindings=list(root.rglob('source-commit.txt'))
assert bindings,'Missing source-commit attestation'
for path in bindings:
    text=path.read_text()
    assert f'github.sha: {expected}' in text and f'checkout HEAD: {expected}' in text,path
native=[p for p in root.rglob('*.xml') if 'androidTest-results' in p.parts and 'connected' in p.parts]
assert native,'No executed native JUnit XML; compilation is not execution'
counts={k:0 for k in ('tests','failures','errors','skipped')}; cases={}
for path in native:
    suite=ET.parse(path).getroot()
    for key in counts:counts[key]+=int(suite.get(key,0) or 0)
    for case in suite.iter('testcase'):
        cases[(case.get('classname'),case.get('name'))]=case
assert counts['tests']>0 and counts['failures']==counts['errors']==0,counts
repair=[(key,case) for key,case in cases.items() if key[0] in (
    'com.opus.piano.DiscoverRepairTest','com.opus.piano.DiscoverEditorialAcceptanceTest')]
assert len(repair)==4, f'Expected 4 executed Discover cases, found {len(repair)}'
assert all(not any(case.find(tag) is not None for tag in ('failure','error','skipped')) for _,case in repair)
directories=list(root.rglob('discover-evidence'))
assert len(directories)==1,directories
evidence=directories[0]
assert not list(evidence.glob('*FAIL*')),'Failure evidence retained; inspect before claiming acceptance'
required=['en-light-auto-goldberg','en-light-auto-chopin','en-light-provider-failure',
          'en-light-offline','en-light-detail-after','en-light-detail-before-rc-layout',
          'en-light-library-composition']
required += [f'en-light-query-{name}' for name in ('chopin','ballade','goldberg','bach','mozart')]
required += [f'{size}-{locale}-{theme}-f{font}-{screen}'
             for size in ('standard','small')
             for locale,theme,font in [('en','Light','1.0'),('en','Dark','2.0'),('cs','Light','2.0'),('cs','Dark','1.0')]
             for screen in ('detail','credits')]

def png_size(path):
    data=path.read_bytes();assert data[:8]==b'\x89PNG\r\n\x1a\n',path
    position=8;size=None;ended=False
    while position<len(data):
        length=struct.unpack('>I',data[position:position+4])[0]
        kind=data[position+4:position+8];body=data[position+8:position+8+length]
        checksum=struct.unpack('>I',data[position+8+length:position+12+length])[0]
        assert len(body)==length and zlib.crc32(kind+body)&0xffffffff==checksum,path
        if kind==b'IHDR':size=struct.unpack('>II',body[:8])
        if kind==b'IEND':ended=True
        position+=12+length
    assert ended and position==len(data) and size,path
    return size

sizes={}
actual=[p.stem for p in evidence.glob('*.png')]
for name in set(required+actual):
    image=evidence/f'{name}.png';hierarchy=evidence/f'{name}.txt'
    assert image.exists() and hierarchy.exists(),name
    sizes[name]=png_size(image)
    text=hierarchy.read_text();assert 'bounds=Rect(' in text and 'text=' in text,name
    if name.startswith('small-'):assert sizes[name]==(720,1280),(name,sizes[name])
    if name.startswith('standard-'):assert sizes[name]==(824,1830),(name,sizes[name])
for name in ('chopin','ballade','goldberg','bach','mozart'):
    query=json.loads((evidence/f'query-{name}.json').read_text())
    assert query['items'] and len({row['id'] for row in query['items']})==len(query['items']),name
preservation=json.loads((evidence/'assertion-preservation.json').read_text())
assert preservation['unchanged'] is True and preservation['before']==preservation['after']>0,preservation
requests=json.loads((evidence/'request-counts.json').read_text())
assert requests['cancelled']==1 and requests['calls']>0,requests
print(json.dumps(dict(source=expected,native=counts,discoverCases=len(repair),
    screenshotHierarchyPairs=len(actual),requiredPairs=len(required),assertions=preservation,requests=requests,
    visualInspection='REQUIRED: inspect original PNGs and hierarchy content separately'),indent=2))
