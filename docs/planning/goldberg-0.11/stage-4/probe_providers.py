"""Bounded public compatibility probes. Never import personal data or run from unit tests."""
import hashlib
import json
from pathlib import Path
import time
from urllib.request import Request, urlopen
from urllib.error import HTTPError, URLError

OUT = Path(__file__).parent / 'results' / 'provider-probes.json'
LIMIT = 2 * 1024 * 1024
UA = 'Goldberg/0.11-stage4-verification (https://github.com/stokonn/Goldberg)'
requests = [
    ('openopus', 'https://api.openopus.org/omnisearch/Goldberg/0.json'),
    ('wikidata', 'https://www.wikidata.org/w/api.php?action=wbgetentities&format=json&maxlag=5&ids=Q1339&props=labels%7Cclaims&languages=en%7Ccs'),
    ('wikipedia', 'https://en.wikipedia.org/w/rest.php/v1/page/Johann_Sebastian_Bach/with_html'),
    ('wikipedia', 'https://cs.wikipedia.org/w/rest.php/v1/page/Johann_Sebastian_Bach/with_html'),
    ('commons', 'https://commons.wikimedia.org/w/api.php?action=query&format=json&formatversion=2&maxlag=5&prop=imageinfo&iiprop=url%7Csha1%7Csize%7Cmime%7Ctimestamp%7Cextmetadata&iiurlwidth=320&titles=File%3AJohann_Sebastian_Bach.jpg'),
]
result = {'scope': 'five bounded public nonpersonal metadata requests; no article/image bytes retained; no identity equivalence certified',
          'musicbrainz': {'state': 'not_probed', 'reason': 'Current primary-document review returned HTTP429; stop rather than retry the throttled source.'}, 'requests': []}
blocked = set()
for provider, url in requests:
    if provider in blocked:
        result['requests'].append({'provider': provider, 'url': url, 'state': 'skipped_provider_backoff'})
        continue
    started = time.monotonic()
    item = {'provider': provider, 'url': url, 'fixtureAction': 'authored public compatibility probe, never a personal search'}
    try:
        try:
            response = urlopen(Request(url, headers={'User-Agent': UA, 'Accept': 'application/json', 'Accept-Encoding': 'identity'}), timeout=20)
        except HTTPError as error:
            response = error
        with response:
            item['http'] = response.code
            item['retryAfter'] = response.headers.get('Retry-After')
            body = response.read(LIMIT + 1)
            item['bytes'] = len(body)
            if response.code in (429, 503):
                blocked.add(provider)
                item['state'] = 'rate_limited'
            elif len(body) > LIMIT:
                item['state'] = 'response_too_large'
            elif response.code != 200:
                item['state'] = 'http_error'
            else:
                root = json.loads(body)
                error = root.get('error')
                if error:
                    code = error.get('code') if isinstance(error, dict) else 'unknown_error'
                    item['providerError'] = code
                    item['state'] = 'rate_limited' if code in ('maxlag', 'ratelimited', 'readonly') else 'json_error'
                    if item['state'] == 'rate_limited':
                        blocked.add(provider)
                else:
                    item['state'] = 'valid_json_shape_requires_adapter_acceptance'
                    item['sha256'] = hashlib.sha256(body).hexdigest()
                    if provider == 'openopus':
                        item['resultCount'] = len(root.get('results', []))
                        item['statusSuccess'] = root.get('status', {}).get('success')
                    elif provider == 'wikipedia':
                        item['revision'] = root.get('latest', {}).get('id')
                        item['license'] = root.get('license')
                        item['htmlBytes'] = len(root.get('html', '').encode())
                    elif provider == 'wikidata':
                        entity = root.get('entities', {}).get('Q1339', {})
                        item['revision'] = entity.get('lastrevid')
                    elif provider == 'commons':
                        pages = root.get('query', {}).get('pages', [])
                        info = next(iter(pages), {}).get('imageinfo', [{}])[0]
                        metadata = info.get('extmetadata', {})
                        item['fileSha1'] = info.get('sha1')
                        item['fileBytes'] = info.get('size')
                        item['licenseName'] = metadata.get('LicenseShortName', {}).get('value')
                        item['licenseUrl'] = metadata.get('LicenseUrl', {}).get('value')
                        item['imageDownloaded'] = False
    except (URLError, TimeoutError, ValueError) as error:
        item['state'] = 'unavailable'
        item['errorType'] = type(error).__name__
    item['durationMillis'] = round((time.monotonic() - started) * 1000, 2)
    result['requests'].append(item)
    OUT.write_text(json.dumps(result, indent=2) + '\n')
    print(provider, item['state'], flush=True)
    time.sleep(1.2)
print(OUT)
