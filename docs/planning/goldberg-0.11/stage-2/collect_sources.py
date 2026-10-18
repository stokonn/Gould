#!/usr/bin/env python3
"""Explicit bounded network research, output only in /tmp; never part of offline checks.
Historical first run is preserved in results; this corrected collector also stops on HTTP200 maxlag.
Raw content is not licensed wholesale and must not be committed.
"""
import urllib.request,urllib.parse,urllib.error,time,json,hashlib,pathlib,datetime,argparse,tempfile
parser=argparse.ArgumentParser();parser.add_argument('--full-html',action='store_true');args=parser.parse_args()
out=pathlib.Path(tempfile.mkdtemp(prefix='goldberg011-stage2-source-'));print('Raw local research output:',out,flush=True)
log=[]; stopped=set();last={}
samples=[('classical','Goldberg Variations'),('jazz','Take Five'),('blues','Saint Louis Blues'),('pop','Yesterday'),('rock','Bohemian Rhapsody'),('film','Hedwig theme'),('game','Zelda lullaby'),('folk','Greensleeves')]
def get(provider,key,url):
 if provider in stopped:
  log.append(dict(provider=provider,key=key,url=url,status='skipped_after_throttle'));return None
 time.sleep(max(0,3-(time.monotonic()-last.get(provider,0))))
 last[provider]=time.monotonic();start=time.perf_counter()
 req=urllib.request.Request(url,headers={'User-Agent':'GoldbergResearch/0.11-stage2 (https://github.com/stokonn/Goldberg; bounded architecture research)','Accept':'application/json','Accept-Encoding':'identity'})
 rec=dict(provider=provider,key=key,url=url,at=datetime.datetime.now(datetime.timezone.utc).isoformat())
 try:
  with urllib.request.urlopen(req,timeout=20) as r:
   data=r.read(2*1024*1024+1);assert len(data)<=2*1024*1024
   rec.update(status=r.status,bytes=len(data),sha256=hashlib.sha256(data).hexdigest(),headers={k:r.headers.get(k) for k in ['ETag','Last-Modified','Content-Type','Content-Length','Cache-Control']})
   (out/(key+'.json')).write_bytes(data)
   value=json.loads(data)
   if isinstance(value,dict) and value.get('error'):
    rec['api_error']=value['error'].get('code','unknown')
    if rec['api_error'] in ('maxlag','ratelimited','readonly'):stopped.add(provider)
    value=None
 except urllib.error.HTTPError as e:
  rec.update(status=e.code,retry_after=e.headers.get('Retry-After'));value=None
  if e.code in (429,503):stopped.add(provider)
 except Exception as e:rec.update(status='error',error=type(e).__name__+': '+str(e)[:160]);value=None
 rec['elapsed_ms']=round((time.perf_counter()-start)*1000,3);log.append(rec)
 (out/'requests.json').write_text(json.dumps(log,ensure_ascii=False,indent=2)+'\n')
 print(key,rec['status'],rec.get('bytes'),flush=True);return value
ids=[];coverage=[]
for genre,title in samples:
 w=get('wikidata','wd-search-'+genre,'https://www.wikidata.org/w/api.php?'+urllib.parse.urlencode(dict(action='wbsearchentities',search=title,language='en',type='item',limit=3,format='json',maxlag=5)))
 row=dict(genre=genre,query=title,wikidata_candidates=[dict(id=x['id'],label=x.get('label','')) for x in (w or {}).get('search',[])])
 if row['wikidata_candidates']:ids.append(row['wikidata_candidates'][0]['id'])
 m=get('musicbrainz','mb-search-'+genre,'https://musicbrainz.org/ws/2/work/?'+urllib.parse.urlencode(dict(query='work:"'+title+'"',limit=3,fmt='json')))
 row['musicbrainz_candidates']=[dict(id=x['id'],title=x.get('title','')) for x in (m or {}).get('works',[])];coverage.append(row)
if ids:get('wikidata','wd-entities','https://www.wikidata.org/w/api.php?'+urllib.parse.urlencode(dict(action='wbgetentities',ids='|'.join(dict.fromkeys(ids)),props='labels|aliases|claims|sitelinks',languages='en|cs',sitefilter='enwiki|cswiki',format='json',maxlag=5)))
get('openopus','oo-classical','https://api.openopus.org/omnisearch/goldberg/0.json')
get('openopus','oo-pop','https://api.openopus.org/omnisearch/yesterday/0.json')
for lang in ['en','cs']:
 get('wikipedia-'+lang,'wp-summary-'+lang,'https://'+lang+'.wikipedia.org/api/rest_v1/page/summary/Johann_Sebastian_Bach')
 get('wikipedia-'+lang,'wp-revision-'+lang,'https://'+lang+'.wikipedia.org/w/api.php?'+urllib.parse.urlencode(dict(action='query',titles='Johann Sebastian Bach',prop='info|revisions',rvprop='ids|timestamp',inprop='url',meta='siteinfo',siprop='rightsinfo',format='json',formatversion=2,maxlag=5)))
get('commons','commons-metadata','https://commons.wikimedia.org/w/api.php?'+urllib.parse.urlencode(dict(action='query',titles='File:Johann Sebastian Bach.jpg',prop='imageinfo',iiprop='url|extmetadata|size|sha1|mime|timestamp',iiurlwidth=480,format='json',formatversion=2,maxlag=5)))
(out/'coverage.json').write_text(json.dumps(coverage,ensure_ascii=False,indent=2)+'\n')

(out/'requests.json').write_text(json.dumps(log,ensure_ascii=False,indent=2)+'\n')

if args.full_html:
 for lang in ['en','cs']:
  get('wikipedia-rest-'+lang,'wp-withhtml-'+lang,'https://'+lang+'.wikipedia.org/w/rest.php/v1/page/Johann_Sebastian_Bach/with_html')
(out/'requests.json').write_text(json.dumps(log,ensure_ascii=False,indent=2)+'\n')
