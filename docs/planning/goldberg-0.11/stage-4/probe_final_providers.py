"""One-shot public endpoint observations; no retries, redirects, private input or image transfer."""
from pathlib import Path
from urllib.request import Request, build_opener, HTTPRedirectHandler
from urllib.error import HTTPError, URLError
from urllib.parse import urlencode
import datetime
import hashlib
import json
import time

class NoRedirect(HTTPRedirectHandler):
    def redirect_request(self, *args, **kwargs):
        return None

opener = build_opener(NoRedirect())
requests = [
    ('musicbrainz', 'https://musicbrainz.org/ws/2/artist/?' + urlencode({'query':'artist:"Johann Sebastian Bach"','fmt':'json','limit':1,'offset':0})),
    ('wikidata', 'https://www.wikidata.org/w/api.php?' + urlencode({'action':'wbgetentities','format':'json','maxlag':5,'ids':'Q1339','props':'labels|claims','languages':'cs|en'})),
    ('openopus', 'https://api.openopus.org/omnisearch/Goldberg/0.json'),
    ('wikipedia', 'https://en.wikipedia.org/w/rest.php/v1/page/Michael_Seltenreich/with_html'),
    ('wikipedia', 'https://cs.wikipedia.org/w/rest.php/v1/page/Johann_Sebastian_Bach/with_html'),
]
result = {'scope':'bounded public shape observations; no identity merge or Android transport certification', 'requests':[]}
blocked = set()
raw = Path('.gradle/stage4-wip/public-provider-observations')
raw.mkdir(parents=True, exist_ok=True)
out = Path(__file__).parent / 'results/final-provider-observations.json'
for index, (provider, url) in enumerate(requests):
    row = {'provider':provider, 'url':url, 'observedAt':datetime.datetime.now(datetime.timezone.utc).isoformat()}
    if provider in blocked:
        row['state'] = 'skipped_provider_backoff'
    else:
        try:
            try:
                response = opener.open(Request(url, headers={'User-Agent':'Goldberg/0.11-stage4-verification (https://github.com/stokonn/Goldberg)', 'Accept':'application/json', 'Accept-Encoding':'identity'}), timeout=20)
            except HTTPError as error:
                response = error
            with response:
                row.update(http=response.status, retryAfter=response.headers.get('Retry-After'))
                if response.status in (429, 503):
                    blocked.add(provider); row['state'] = 'rate_limited_stop'
                elif response.status != 200:
                    row['state'] = 'http_error_no_retry'
                else:
                    body = response.read(2*1024*1024+1)
                    row['bytes'] = len(body)
                    if len(body)>2*1024*1024:
                        row['state'] = 'oversize_rejected'
                    else:
                        data = json.loads(body)
                        error = data.get('error')
                        if error:
                            row['providerError'] = error.get('code') if isinstance(error,dict) else 'unknown_error'
                            row['state'] = 'json_error_no_retry'
                            if row['providerError'] in ('maxlag','ratelimited','readonly'):
                                blocked.add(provider); row['state'] = 'rate_limited_stop'
                        else:
                            row.update(state='valid_json_shape_requires_adapter_acceptance', sha256=hashlib.sha256(body).hexdigest())
                            (raw / (str(index)+'-'+provider+'.json')).write_bytes(body)
                            if provider=='musicbrainz':
                                row.update(count=data.get('count'), artistCount=len(data.get('artists',[])), typedIds=[item.get('id') for item in data.get('artists',[])])
                            elif provider=='wikidata':
                                entity=data.get('entities',{}).get('Q1339',{})
                                row['revision']=entity.get('lastrevid')
                                row['explicitArtistClaims']=[c.get('mainsnak',{}).get('datavalue',{}).get('value') for c in entity.get('claims',{}).get('P434',[]) if c.get('rank')!='deprecated']
                            elif provider=='openopus':
                                row.update(resultCount=len(data.get('results',[])), statusSuccess=data.get('status',{}).get('success'))
                            else:
                                row.update(revision=data.get('latest',{}).get('id'), license=data.get('license'), htmlBytes=len(data.get('html','').encode()))
        except (URLError, TimeoutError, ValueError) as error:
            row.update(state='unavailable_no_retry', errorType=type(error).__name__)
    result['requests'].append(row)
    out.write_text(json.dumps(result, indent=2)+'\n')
    print(provider, row['state'], flush=True)
    time.sleep(1.2)
