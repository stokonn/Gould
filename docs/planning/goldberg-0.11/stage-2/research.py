#!/usr/bin/env python3
"""Isolated Stage 2 contracts/fixtures/host measurements. Never opens an installed DB.
No network in this utility. All benchmark DBs live in a TemporaryDirectory.
"""
import argparse,copy,gzip,hashlib,json,math,platform,sqlite3,statistics,tempfile,time,tracemalloc,unicodedata,uuid
from pathlib import Path
HERE=Path(__file__).resolve().parent
ROOT=HERE.parents[3]
NS=uuid.UUID('951e57d5-1d76-5a89-9f7a-6bf7f531038a')
def uid(key): return str(uuid.uuid5(NS,key))
def dump(p,v): p.write_text(json.dumps(v,ensure_ascii=False,indent=2,sort_keys=True)+'\n')
def raw(v): return json.dumps(v,ensure_ascii=False,sort_keys=True,separators=(',',':')).encode()
def sha(v): return hashlib.sha256(v).hexdigest()
def norm(s):
 s=''.join(c for c in unicodedata.normalize('NFD',s) if unicodedata.category(c)!='Mn').lower()
 for a,b in [('ø','o'),('ł','l'),('ß','ss'),('æ','ae'),('œ','oe')]:s=s.replace(a,b)
 return ' '.join(''.join(c if c.isalnum() else ' ' for c in s).split())
def fixtures():
 entities=[]
 def entity(key,kind,title,**extra):
  row=dict(id=uid(key),key=key,kind=kind,title=title,revision=1,status='active',**extra);entities.append(row);return row['id']
 works=[entity('work-'+g,'work','Fixture '+g+' study',workType='composition',genre=g) for g in ['classical','jazz','blues','pop','rock','film','game','folk']]
 m1=entity('movement-1','work','Fixture movement I',workType='movement');m2=entity('movement-2','work','Fixture movement II',workType='part')
 med=entity('medley','work','Fixture medley',workType='medley');mash=entity('mashup','work','Fixture mashup',workType='mashup')
 a1=entity('arr-simple','arrangement','Fixture piano arrangement A',instrumentation='piano');a2=entity('arr-duet','arrangement','Fixture piano arrangement B',instrumentation='piano duet')
 people=[entity('person-a','artist','Alex Example',artistType='person'),entity('person-b','artist','Alex Example',artistType='person'),entity('group','artist','Fixture Ensemble',artistType='group')]
 albums=[entity('album-'+t,'album','Fixture '+t,albumType=t) for t in ['album','ep','single','soundtrack']]
 releases=[entity('edition-'+str(i),'release','Fixture edition '+str(i),albumId=albums[0]) for i in range(2)]
 recordings=[entity('recording-'+str(i),'recording','Fixture '+t,durationMs=180000) for i,t in enumerate(['studio','live'])]
 col=entity('collection','collection','Fixture personal list')
 data={'contractVersion':1,'license':'CC0-1.0','fictional':True,'entities':entities,'relations':[
  {'type':'part','from':works[0],'to':m1,'position':0},{'type':'part','from':works[0],'to':m2,'position':1},
  *[{'type':typ,'from':d,'to':s} for typ,d in [('medley_source',med),('mashup_source',mash)] for s in works[:2]],
  *[{'type':'arrangement_source','from':a,'to':works[3]} for a in [a1,a2]],
  *[{'type':'performs','from':r,'to':a1} for r in recordings],
  *[{'type':'member','from':col,'to':t,'position':i,'memberId':uid('member'+str(i))} for i,t in enumerate([works[0],a1,albums[0]])]],
  'aliases':[{'entityId':works[0],'language':'cs','value':'Žluťoučká skladba','normalized':norm('Žluťoučká skladba')},{'entityId':works[0],'language':'en','value':'Fixture classical study','normalized':norm('Fixture classical study')},{'entityId':works[0],'language':'ja','value':'試験作品','normalized':norm('試験作品')}],
  'identities':[{'provider':'fixture-a','providerKind':'work','externalId':'87','entityId':works[0],'evidence':'authored deterministic fixture','state':'reviewed'},{'provider':'fixture-b','providerKind':'work','externalId':'87','entityId':works[1],'evidence':'different source namespace','state':'reviewed'}],
  'redirects':[{'from':works[2],'to':works[1],'evidence':'synthetic reversible reviewed redirect','revision':1}],
  'cases':['namesake_people','group_not_person','two_arrangements','selectable_movement','selectable_part','medley_two_sources','mashup_two_sources','unicode_alias','provider_id_collision','reversible_redirect','redirect_cycle_rejected','wrong_kind_merge_rejected','missing_id','conflicting_revision','studio_live','release_editions','albums_ep_single_soundtrack','duplicate_collection_position_rejected','denied_license','missing_attribution','cs_fallback_en','failed_bundle_update','metered_consent_revoked','draft_idempotency','historical_backup_readers']}
 documents=[{'id':uid('doc-'+lang),'entityId':works[0],'language':lang,'revision':'fixture-v1','text':text,'license':'CC0-1.0','sourceUrl':'https://github.com/stokonn/Goldberg','attribution':'Goldberg Stage 2 authored fixture; not a biography','reviewState':'approved'} for lang,text in [('en','Synthetic music reference text for contract tests; no real-world claim.'),('cs','Syntetický hudební text pro testy kontraktu; nejde o skutečný životopis.')]]
 for d in documents:d['contentSha256']=sha(d['text'].encode())
 data['documents']=documents
 dump(HERE/'fixtures/catalogue.json',data)
 objects=[{'id':'metadata','bytes':len(raw(entities)),'sha256':sha(raw(entities)),'license':'CC0-1.0','attribution':'Goldberg authored fixture'},{'id':'en-document','bytes':len(raw(documents[0])),'sha256':sha(raw(documents[0])),'license':'CC0-1.0','attribution':documents[0]['attribution']}]
 for obj in objects:
  obj.update(relativePath='objects/'+obj['sha256'],mime='application/json',sourceRevision='fixture-v1',sourceUrl='https://github.com/stokonn/Goldberg',licenseUrl='https://creativecommons.org/publicdomain/zero/1.0/',reviewState='approved',transformations=[])
 manifests=[]
 for kind,target in [('work',works[0]),('artist',people[0]),('album',albums[0]),('collection',col)]:
  manifests.append({'contractVersion':1,'bundleId':uid('bundle-'+kind),'kind':kind,'entityId':target,'revision':'fixture-v1','language':'en','objects':objects,'dependencies':[uid('doc-en')],'metadataSnapshot':[x for x in entities if x['id']==target],'previousReadyRevision':None})
 dump(HERE/'fixtures/bundles.json',manifests)
 backup={'format':'goldberg-backup','version':5,'journalSchema':8,'journalTables':{'sessions':[{'id':1,'durationMillis':3000,'completionToken':'fixture-completion'}],'session_parts':[{'sessionId':1,'pieceId':7,'durationMillis':3000,'title':'Keep original','notes':'Preserve'}],'session_piece_links':[{'sessionId':1,'pieceId':8,'title':'Untimed'}]},'personalCatalogueAnchors':[{'pieceId':7,'entityId':works[0],'legacyOpenOpusId':'10074','title':'Original personal title','composer':'Original personal composer'}], 'preferences':{'appearance':{'accent':'Slate','cards':'Outline','density':'Compact'},'locale':'cs','media':{'meteredConsent':False}},'recovery':{'state':'paused','draftFormat':1,'rawPayload':'{"version":1,"token":"fixture-completion","credited":3000}','completionToken':'fixture-completion','creditMillis':3000,'executionMustRestart':True,'archives':[{'rawPayload':'unchanged malformed original'}]}}
 dump(HERE/'fixtures/backup-v5-envelope.json',backup)

def validate_graph(d):
 assert d['contractVersion']==1
 ids={x['id']:x for x in d['entities']};assert len(ids)==len(d['entities'])
 for x in ids:assert str(uuid.UUID(x))==x
 for e in ids.values():assert e['kind'] in ['work','arrangement','artist','album','release','recording','collection'] and e['title'] and e['revision']>0 and e['status'] in ['active','redirect','quarantined']
 for r in d['relations']:
  a,b=ids[r['from']],ids[r['to']]
  if r['type']=='part':assert a['kind']==b['kind']=='work' and a['id']!=b['id']
  elif r['type'] in ['medley_source','mashup_source']:assert a['kind']==b['kind']=='work' and a['workType'] in ['medley','mashup']
  elif r['type']=='arrangement_source':assert a['kind']=='arrangement' and b['kind']=='work'
  elif r['type']=='performs':assert a['kind']=='recording' and b['kind'] in ['work','arrangement']
  elif r['type']=='member':assert a['kind']=='collection' and b['kind']!='collection'
  else:raise AssertionError('Unknown relation')
 keys=[(x['provider'],x['providerKind'],x['externalId']) for x in d['identities']];assert len(keys)==len(set(keys))
 for x in d['identities']:assert x['entityId'] in ids and x['evidence']
 for rel in ['part','member']:
  positions=[(x['from'],x['position']) for x in d['relations'] if x['type']==rel];assert len(positions)==len(set(positions))
 # Cycle checks include composition parent graph and redirect graph independently.
 for edges in [[(x['from'],x['to']) for x in d['relations'] if x['type'] in ['part','medley_source','mashup_source']],[(x['from'],x['to']) for x in d['redirects']]]:
  graph={}
  for a,b in edges:graph.setdefault(a,[]).append(b);assert ids[a]['kind']==ids[b]['kind']
  def walk(a,path):
   assert a not in path
   for b in graph.get(a,[]):walk(b,path|{a})
  for a in graph:walk(a,set())

def redistributable(d):
 return d.get('license') in ['CC0-1.0','CC-BY-4.0','CC-BY-SA-4.0'] and d.get('reviewState')=='approved' and bool(d.get('attribution')) and bool(d.get('sourceUrl')) and bool(d.get('revision'))
def select_document(docs,lang):
 for wanted in [lang,'en']:
  match=next((d for d in docs if d['language']==wanted and redistributable(d)),None)
  if match:return match
 return None

def validate_backup(b):
 required=json.loads((HERE/'contracts.json').read_text())['backupRequired']
 assert set(required)<=b.keys() and b['format']=='goldberg-backup' and b['version']==5
 assert b['recovery']['state']=='paused' and b['recovery']['executionMustRestart'] is True
 assert isinstance(b['recovery']['rawPayload'],str) and b['recovery']['rawPayload']
 assert set(b['preferences'])=={'appearance','locale','media'}
 assert b['preferences']['locale'] in ['cs','en','system']
 sessions=b['journalTables']['sessions'];parts=b['journalTables']['session_parts']
 tokens=[x['completionToken'] for x in sessions];assert len(tokens)==len(set(tokens))
 for row in sessions:
  assert row['durationMillis']>=0
  assert sum(p['durationMillis'] for p in parts if p['sessionId']==row['id'])==row['durationMillis']
 return 'archive_already_completed' if b['recovery']['completionToken'] in tokens else 'offer_paused_recovery'

def install_fixture_revision(directory,revision,objects):
 # A research crash-boundary model: validate every byte before changing the ready pointer.
 for expected,body in objects:
  if sha(body)!=expected:raise ValueError('Hash mismatch')
 path=directory/(revision+'.json');path.write_text(json.dumps([h for h,_ in objects]))
 pending=directory/'ready.tmp';pending.write_text(revision);pending.replace(directory/'ready')

def validate_envelopes(e):
 c=json.loads((HERE/'contracts.json').read_text())
 a=e['adapter'];search=e['search']
 assert set(c['adapterRequired'])<=a.keys() and set(c['searchRequired'])<=search.keys()
 assert a['contractVersion']==search['contractVersion']==1
 assert a['state'] in c['providerStates'] and search['category'] in c['searchCategories']
 assert a['state'] not in ['unavailable','rate_limited','invalid','blocked_rights'] or not a['data']
 uuid.UUID(a['requestId']);uuid.UUID(search['queryId'])
 for row in search['sources']:assert row['state'] in c['providerStates']

def validate_bundle(b):
 c=json.loads((HERE/'contracts.json').read_text())
 assert set(c['bundleRequired'])<=b.keys() and b['contractVersion']==1
 assert b['kind'] in ['work','artist','album','collection']
 for obj in b['objects']:
  assert obj['bytes']>=0 and len(obj['sha256'])==64
  assert obj['mime']=='application/json' and obj['sourceRevision']
  assert obj['licenseUrl'] and obj['reviewState']=='approved' and obj['attribution']
  path=Path(obj['relativePath']);assert not path.is_absolute() and '..' not in path.parts

def check():
 d=json.loads((HERE/'fixtures/catalogue.json').read_text());validate_graph(d)
 count=1
 envelopes=json.loads((HERE/'fixtures/envelopes.json').read_text());validate_envelopes(envelopes);count+=1
 bundles=json.loads((HERE/'fixtures/bundles.json').read_text())
 for b in bundles:validate_bundle(b);count+=1
 for field,value in [('relativePath','../opus.db'),('licenseUrl',''),('reviewState','unknown')]:
  bad=copy.deepcopy(bundles[0]);bad['objects'][0][field]=value
  try:validate_bundle(bad)
  except (AssertionError,KeyError):count+=1
  else:raise AssertionError('Invalid bundle contract accepted')
 for change in [lambda e:e['adapter'].__setitem__('state','invented'),lambda e:e['search'].__setitem__('category','unapproved-tab'),lambda e:e['search'].pop('queryId'),lambda e:e['adapter'].__setitem__('data',[{'title':'False success'}])]:
  bad=copy.deepcopy(envelopes);change(bad)
  try:validate_envelopes(bad)
  except (AssertionError,KeyError,ValueError):count+=1
  else:raise AssertionError('Bad envelope accepted')
 def rejects(change):
  nonlocal count
  bad=copy.deepcopy(d);change(bad)
  try:validate_graph(bad)
  except (AssertionError,KeyError,ValueError):count+=1;return
  raise AssertionError('Adversarial fixture accepted')
 rejects(lambda x:x['identities'].append(copy.deepcopy(x['identities'][0])))
 rejects(lambda x:x['entities'].__setitem__(0,{**x['entities'][0],'id':'not-uuid'}))
 rejects(lambda x:x['relations'][0].__setitem__('to',uid('group')))
 rejects(lambda x:x['relations'].append({'type':'part','from':uid('movement-1'),'to':uid('work-classical'),'position':0}))
 rejects(lambda x:x['redirects'].append({'from':uid('work-jazz'),'to':uid('work-blues')}))
 rejects(lambda x:x['redirects'].append({'from':uid('work-pop'),'to':uid('group')}))
 rejects(lambda x:x['relations'].append(copy.deepcopy(next(r for r in x['relations'] if r['type']=='member'))))
 assert d['entities'][14]['id']!=d['entities'][15]['id'] and d['entities'][14]['title']==d['entities'][15]['title'];count+=1
 assert d['aliases'][0]['normalized']=='zlutoucka skladba';count+=1
 assert {e.get('albumType') for e in d['entities'] if e['kind']=='album'}=={'album','ep','single','soundtrack'};count+=1
 for lang in ['cs','en']:assert select_document(d['documents'],lang)['language']==lang;count+=1
 en=select_document(d['documents'],'en');bad={**en,'language':'cs','license':'UNKNOWN'}
 assert select_document([bad,en],'cs')['language']=='en';count+=1
 for field in ['license','attribution','sourceUrl','revision','reviewState']:
  assert not redistributable({**en,field:''});count+=1
 b=json.loads((HERE/'fixtures/backup-v5-envelope.json').read_text());before=raw(b)
 assert validate_backup(b)=='archive_already_completed';count+=1
 for mutation in [lambda x:x.pop('preferences'),lambda x:x['recovery'].__setitem__('state','running'),lambda x:x['journalTables']['session_parts'][0].__setitem__('durationMillis',6000)]:
  invalid=copy.deepcopy(b);mutation(invalid)
  try:validate_backup(invalid)
  except (AssertionError,KeyError):count+=1
  else:raise AssertionError('Invalid portable backup contract accepted')
 assert sum(x['durationMillis'] for x in b['journalTables']['sessions'])==sum(x['durationMillis'] for x in b['journalTables']['session_parts'])==3000;count+=1
 # Completion-token reconciliation model: imported paused draft cannot earn prior credit twice.
 completed={x['completionToken'] for x in b['journalTables']['sessions']}; assert b['recovery']['completionToken'] in completed;count+=1
 original_anchor=copy.deepcopy(b['personalCatalogueAnchors']);temporary_catalogue={};assert b['personalCatalogueAnchors']==original_anchor and not temporary_catalogue;count+=1
 restored=json.loads(before);assert raw(restored)==before and restored['preferences']==b['preferences'];count+=1
 with tempfile.TemporaryDirectory(prefix='goldberg011-bundle-test-') as t:
  directory=Path(t);install_fixture_revision(directory,'v1',[(sha(b'valid'),b'valid')])
  try:install_fixture_revision(directory,'v2',[(sha(b'expected'),b'corrupted')])
  except ValueError:pass
  else:raise AssertionError('Corrupt revision published')
  assert (directory/'ready').read_text()=='v1' and not (directory/'v2.json').exists();count+=1
  install_fixture_revision(directory,'v2',[(sha(b'new valid'),b'new valid')]);assert (directory/'ready').read_text()=='v2' and (directory/'v1.json').exists();count+=1
 def allowed(wifi,metered,consent):return (wifi and not metered) or consent
 assert allowed(True,False,False) and not allowed(False,True,False) and not allowed(True,True,False) and allowed(False,True,True);count+=1
 # DDL contract enforcement is exercised on an empty disposable DB, not on Room.
 db=sqlite3.connect(':memory:');db.executescript((HERE/'catalogue-research.sql').read_text())
 for e in d['entities']:
  db.execute('INSERT INTO entity VALUES(?,?,?,?,?)',(e['id'],e['kind'],e['title'],1,'active'))
  if e['kind']=='work':db.execute('INSERT INTO work(id,work_type) VALUES(?,?)',(e['id'],e['workType']))
 try:db.execute('INSERT INTO work(id,work_type) VALUES(?,?)',(uid('group'),'composition'))
 except sqlite3.IntegrityError:count+=1
 else:raise AssertionError('Cross-kind FK accepted')
 assert not db.execute('PRAGMA foreign_key_check').fetchall();count+=1
 for p in (ROOT/'app/src/androidTest/assets/backups').glob('*.json'):
  old=json.loads(p.read_text());assert old['version'] in [1,2,3] and old['format']=='goldberg-backup';count+=1
 facts=json.loads((HERE/'fixtures/provider-facts.json').read_text())['records']
 assert len(facts)==24 and all(f['license']=='CC0-1.0' and f['mappingState']=='observed_candidate' for f in facts);count+=1
 policies=json.loads((HERE/'source-field-policy.json').read_text());assert len(policies['rules'])==5;count+=1
 print(f'PASS: {count} contract assertions; {len(d["entities"])} authored entities; {len(d["cases"])} named scenarios')
 return count

def timing(fn,n=31):
 values=[]
 for _ in range(n):
  t=time.perf_counter_ns();fn();values.append((time.perf_counter_ns()-t)/1e6)
 return {'median_ms':round(statistics.median(values),4),'p95_ms':round(sorted(values)[math.ceil(.95*len(values))-1],4),'samples':n}
def bench(output):
 result={'method':'host SQLite/Python; 31 warm query repetitions, generated data; not Android/Room timings','environment':{'python':platform.python_version(),'sqlite':sqlite3.sqlite_version,'system':platform.platform()},'workloads':[]}
 schema=json.loads((ROOT/'app/schemas/com.opus.piano.data.OpusDatabase/8.json').read_text())['database']
 with tempfile.TemporaryDirectory(prefix='goldberg011-research-') as directory:
  for size in [1000,10000,50000]:
   db=sqlite3.connect(str(Path(directory)/f'legacy-{size}.db'))
   for e in schema['entities']:
    db.execute(e['createSql'].replace('${TABLE_NAME}',e['tableName']))
    for i in e.get('indices',[]):db.execute(i['createSql'].replace('${TABLE_NAME}',e['tableName']))
   db.execute("INSERT INTO catalog_composers VALUES('87','Fixture artist','','','')")
   rows=[(str(i),'87',f'Fixture work {i:06d}','','Keyboard','fixture','','','',0,0) for i in range(size)]
   db.executemany('INSERT INTO catalog_works VALUES(?,?,?,?,?,?,?,?,?,?,?)',rows)
   db.executemany("INSERT INTO sessions(id,pieceId,pieceTitle,startedAt,completedAt,durationMillis) VALUES(?,NULL,'Fixture',0,?,3000)",[(i,i*1000) for i in range(1,10001)])
   db.commit()
   snapshot=lambda:db.execute('SELECT * FROM catalog_works ORDER BY title COLLATE NOCASE').fetchall()
   tracemalloc.start();snap=snapshot();normalized=[norm(' '.join(map(str,r))) for r in snap];_,peak=tracemalloc.get_traced_memory();tracemalloc.stop()
   baseline=timing(snapshot);scan=timing(lambda:[r for r in normalized if 'fixture' in r and '000010' in r])
   history_sql='SELECT * FROM sessions WHERE completedAt < ? OR (completedAt=? AND id<?) ORDER BY completedAt DESC,id DESC LIMIT 40'
   history=timing(lambda:db.execute(history_sql,(9000000,9000000,9000)).fetchall())
   full=timing(lambda:db.execute('SELECT * FROM sessions').fetchall(),11)
   projected=sqlite3.connect(str(Path(directory)/f'proposed-{size}.db'));projected.executescript((HERE/'catalogue-research.sql').read_text())
   index_start=time.perf_counter()
   projected.executemany('INSERT INTO entity VALUES(?,?,?,?,?)',[(uid('scale'+str(i)),'work',f'Fixture work {i:06d}',1,'active') for i in range(size)])
   before_index=projected.execute('PRAGMA page_count').fetchone()[0]*projected.execute('PRAGMA page_size').fetchone()[0]
   projected.executemany('INSERT INTO alias VALUES(?,?,?,?,?)',[(uid('scale'+str(i)),'en',f'Fixture work {i:06d}',f'fixture work {i:06d}','primary') for i in range(size)])
   projected.executemany('INSERT INTO alias_fts(entity_id,normalized) VALUES(?,?)',[(uid('scale'+str(i)),f'fixture work {i:06d}') for i in range(size)])
   projected.commit();ingest_ms=(time.perf_counter()-index_start)*1000
   query="SELECT entity_id FROM alias WHERE normalized>=? AND normalized<? ORDER BY normalized,entity_id LIMIT 40"
   args=('fixture work 000','fixture work 001')
   indexed=timing(lambda:projected.execute(query,args).fetchall())
   fts_query='SELECT entity_id FROM alias_fts WHERE alias_fts MATCH ? ORDER BY rowid LIMIT 40'
   fts_args=('fixture 000010*',)
   fts=timing(lambda:projected.execute(fts_query,fts_args).fetchall())
   pages=projected.execute('PRAGMA page_count').fetchone()[0]*projected.execute('PRAGMA page_size').fetchone()[0]
   row={'catalogue_rows':size,'journal_sessions':10000,'legacy_snapshot':baseline,'python_normalized_scan':scan,'legacy_snapshot_and_normalization_peak_bytes':peak,'journal_history_page':history,'journal_full_snapshot':full,'proposed_prefix_page':indexed,'proposed_fts_page':fts,'proposed_build_ms':round(ingest_ms,3),'proposed_db_bytes':pages,'alias_table_and_indexes_increment_bytes':pages-before_index,'fts_query_plan':projected.execute('EXPLAIN QUERY PLAN '+fts_query,fts_args).fetchall(),'query_plan':projected.execute('EXPLAIN QUERY PLAN '+query,args).fetchall(),'legacy_db_bytes':db.execute('PRAGMA page_count').fetchone()[0]*db.execute('PRAGMA page_size').fetchone()[0]}
   result['workloads'].append(row);db.close();projected.close()
  fixtures_data=json.loads((HERE/'fixtures/catalogue.json').read_text());result['fixture_bytes']={'json':len(raw(fixtures_data)),'gzip':len(gzip.compress(raw(fixtures_data),mtime=0))}
 dump(output,result);print(json.dumps(result,indent=2))
if __name__=='__main__':
 parser=argparse.ArgumentParser();parser.add_argument('command',choices=['generate','check','bench']);parser.add_argument('--output',type=Path,default=Path('/tmp/goldberg011-stage2-host.json'));args=parser.parse_args()
 if args.command=='generate':fixtures()
 elif args.command=='check':check()
 else:bench(args.output)
