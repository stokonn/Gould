"""Resource parity/format audit. Android runtime plural selection is tested by instrumentation."""
from pathlib import Path
import re
import xml.etree.ElementTree as E

root=Path(__file__).resolve().parents[1]
def read(lang):
    r=E.parse(root/f'app/src/main/res/{lang}/strings.xml').getroot()
    values={e.get('name'):e for e in r}
    assert len(values)==len(list(r)), 'Duplicate resource names'
    return values
english=read('values');czech=read('values-cs')
assert set(english)==set(czech), 'English/Czech resource keys differ'
for key,en in english.items():
    cs=czech[key]
    assert en.tag==cs.tag,key
    if en.tag=='string':
        assert cs.text and en.text,key
        formats=lambda text: sorted(re.findall(r'%\d+\$[\d.]*[sdf]',text))
        assert formats(en.text)==formats(cs.text),(key,en.text,cs.text)
    else:
        assert {'one','few','many','other'} <= {x.get('quantity') for x in cs},key
        assert all(x.text for x in cs),key
for key,forms in {'pieces_count':['%d skladba','%d skladby','%d skladeb'],'minutes_count':['%d minuta','%d minuty','%d minut']}.items():
    assert [x.text for x in czech[key] if x.get('quantity')!='many']==forms
assert [x.get('{http://schemas.android.com/apk/res/android}name') for x in E.parse(root/'app/src/main/res/xml/locales_config.xml').getroot()]==['en','cs']
print(f'PASS: {len(english)} English/Czech resources, matching placeholders, Czech plural forms and declared locales')
