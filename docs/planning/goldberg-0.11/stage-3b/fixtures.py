#!/usr/bin/env python3
"""Authored CC0 synthetic fixtures from actual Room exports, never production databases."""
import base64,hashlib,json,sqlite3,uuid
from pathlib import Path
ROOT=Path(__file__).resolve().parents[4]
OUT=ROOT/'app/src/androidTest/assets/stage3b'
SCHEMA=ROOT/'app/schemas/com.opus.piano.data.OpusDatabase/10.json'
def uid(s):return str(uuid.uuid5(uuid.NAMESPACE_URL,'goldberg-stage3b:'+s))
def canonical(o):return json.dumps(o,ensure_ascii=False,separators=(',',':'),sort_keys=True).encode()
def digest(o):return hashlib.sha256(canonical(o)).hexdigest()
def sign(o):
 o.pop('sha256',None)
 for section in o['sections'].values():section['sha256']=digest(section['data'])
 o['sha256']=digest(o)
 return o
def raw(data,**extra):return dict(rawBase64=base64.b64encode(data).decode(),sha256=hashlib.sha256(data).hexdigest(),updatedAt=777,**extra)
def make(full):
 c=sqlite3.connect(':memory:');c.row_factory=sqlite3.Row
 schema=json.loads(SCHEMA.read_text())['database']
 for e in schema['entities']:
  c.execute(e['createSql'].replace('${TABLE_NAME}',e['tableName']))
  for i in e.get('indices',[]):c.execute(i['createSql'].replace('${TABLE_NAME}',e['tableName']))
 def put(table,**row):c.execute(f'INSERT INTO {table} ({",".join(row)}) VALUES ({",".join("?" for _ in row)})',list(row.values()))
 scope=uid('full-scope' if full else 'minimal-scope')
 put('personal_catalogue_state',id=1,scope=scope)
 if full:
  for n,title in [(7,'Fixture source work'),(8,'Fixture piano arrangement'),(9,'Fixture selectable movement'),(10,'Fixture two-source medley')]:
   put('pieces',id=n,title=title,composer='Authored fixture creator',difficulty='INTERMEDIATE',status='LEARNING',notes='Original notes – žluťoučká',catalogWorkId='100' if n==7 else None)
  put('catalog_composers',id='87',name='Authored fixture creator',birth='',death='',epoch='')
  put('catalog_works',id='100',composerId='87',title='Fixture source work',subtitle='',genre='Keyboard',searchTerms='fixture',catalogue='',catalogueNumber='',musicalKey='')
  put('sessions',id=3,pieceId=None,pieceTitle='Arrangement · Movement · Medley',startedAt=1000,completedAt=4000,durationMillis=3000,elapsedMillis=3000,practiceDay='1970-01-01',practiceZone='UTC',timingQuality='Measured',practiceMode='Manual',notes='Session notes',reflection='Keep reflection',completionToken='completed-fixture')
  put('sessions',id=4,pieceId=7,pieceTitle='Historic original snapshot',startedAt=5000,completedAt=10000,durationMillis=5000,catalogWorkId='100',composerId='87')
  for index,(piece,duration) in enumerate([(8,1000),(9,1500),(10,500)]):
   put('session_parts',id=index+1,sessionId=3,pieceId=piece,title=['Arrangement','Movement','Medley'][index],composer='Original creator',catalogWorkId=None,composerId=None,musicalEra='',durationMillis=duration,notes='Part notes')
  put('session_parts',id=4,sessionId=4,pieceId=7,title='Historical source snapshot',composer='Original',catalogWorkId='100',composerId='87',musicalEra='',durationMillis=5000,notes='Historic notes')
  start=0
  for i,duration in enumerate([1000,1500,500]):
   put('session_intervals',id=i+1,sessionId=3,partIndex=i,startMillis=start,endMillis=start+duration,creditedMillis=duration,reason='Manual');start+=duration
  put('session_piece_links',sessionId=3,pieceId=7,title='Untimed source',composer='Original',catalogWorkId='100',composerId='87',musicalEra='',addedAt=123)
  put('favorite_composers',composerId='87',addedAt=444)
  put('badge_unlocks',achievementId='first_notes',unlockedAt=123)
  put('profile_settings',id=1,username='Fixture musician',avatar='',theme='Dark',showcase='first_notes',typography='Editorial',statistics='streak,time,pieces,sessions',smartEnabled=1,sensitivity=55,pauseSeconds=3,biography='Authored fixture biography')
  put('saved_recordings',id=12,workKey='catalog:100',url='https://youtu.be/abcdefghijk',title='Fixture reference',performer='Authored performer',platform='YOUTUBE',savedAt=321,preferred=1)
  def identity(key,kind,legacy=None):
   put('personal_catalogue_identities',identityKey=key,canonicalId=uid(key),kind=kind,provider='openopus' if legacy else 'local',legacyId=legacy,evidence='Authored synthetic fixture; not a verified provider match')
  identity('legacy:work:100','work','100');identity('legacy:artist:87','artist','87')
  for n,kind in [(8,'arrangement'),(9,'work'),(10,'work')]:identity(f'local:{scope}:piece:{n}',kind)
  def anchor(type,owner,key,title,role='work',legacy=None):
   put('personal_catalogue_anchors',ownerType=type,ownerId=str(owner),role=role,identityKey=key,originalTitle=title,originalCreator='Original creator',legacyId=legacy,evidence='Authored original snapshot')
  for p in c.execute('SELECT * FROM pieces').fetchall():anchor('piece',p['id'],'legacy:work:100' if p['id']==7 else f'local:{scope}:piece:{p["id"]}',p['title'],legacy=p['catalogWorkId'])
  # Anonymous multi-piece session gets its own durable local work reference.
  key=f'local:{scope}:session:3';identity(key,'work');anchor('session',3,key,'Original session title')
  anchor('session',4,'legacy:work:100','Historic original snapshot',legacy='100');anchor('session',4,'legacy:artist:87','Original creator','artist','87')
  for p in c.execute('SELECT * FROM session_parts').fetchall():
   key='legacy:work:100' if p['pieceId']==7 else f'local:{scope}:piece:{p["pieceId"]}'
   anchor('part',p['id'],key,p['title'],legacy=p['catalogWorkId'])
   if p['composerId']:anchor('part',p['id'],'legacy:artist:87','Original creator','artist','87')
  anchor('link','3:7','legacy:work:100','Untimed source',legacy='100');anchor('link','3:7','legacy:artist:87','Original creator','artist','87')
  anchor('favorite','87','legacy:artist:87','Original creator','artist','87');anchor('recording',12,'legacy:work:100','Fixture reference',legacy='100')
  put('practice_completion_evidence',token='completed-fixture',durationMillis=3000)
 journal_names=['pieces','sessions','catalog_composers','catalog_works','saved_recordings','badge_unlocks','session_parts','profile_settings','session_intervals','session_piece_links','favorite_composers']
 read=lambda table:[dict(r) for r in c.execute(f'SELECT * FROM {table} ORDER BY rowid')]
 preferences={'appearance':dict(accent='Slate',cards='Outline',homeSections=['recent','statistics'],hiddenHome=[],metrics=['week','streak'],profileSections=['badges','statistics','activity','frequent','history'],hiddenProfile=['history'],density='Compact',sort='Composer'), 'locale':'cs','previousMeteredConsent':True,'local':dict(onboardingDone=True,reminderEnabled=True,reminderHour=19,reminderMinute=15)}
 recovery={'drafts':[],'archives':[],'evidence':[],'completions':read('practice_completion_evidence')}
 if full:
  draft=dict(version=1,execution=77,token='active-fixture',start=10000,credited=1500,elapsed=2000,detected=0,mode='Manual',smart=False,active=1,notes='Keep draft session notes',reflection='Original reflection',sensitivity=55,pause=3,status='Manually paused',quality='Measured',removed=[],parts=[dict(piece=p,title=t,composer='Original creator',catalog=None,era='',duration=d,notes='Keep draft note') for p,t,d in [(8,'Arrangement',500),(9,'Movement',1000)]],intervals=[dict(part=0,start=0,end=500,credit=500,reason='Manual'),dict(part=1,start=500,end=1500,credit=1000,reason='Manual'),dict(part=1,start=1500,end=2000,credit=0,reason='ManualPause')])
  recovery['drafts']=[raw((json.dumps(draft,ensure_ascii=False,indent=2)+'\n').encode(),id=1)]
  recovery['archives']=[raw(b'Original unknown format {v:99}\n',id=1,archivedAt=888),raw(b'\xff\x00unaltered recovery bytes',id=2,archivedAt=999)]
  for i,data in enumerate([b'Authored opaque evidence',b'\xff\xfe\x00evidence']):
   evidence=raw(data,id=hashlib.sha256(f'evidence-{i}'.encode()).hexdigest(),archivedAt=999,origin='authored-fixture',classification='unreadable',completionToken=None)
   recovery['evidence'].append(evidence)
 o={'format':'goldberg-backup','version':5,'journalSchema':10,'createdAt':1792320000000,'sections':{k:{'version':1,'data':v} for k,v in {'journal':{t:read(t) for t in journal_names},'identities':{t:read(t) for t in ['personal_catalogue_identities','personal_catalogue_anchors','personal_catalogue_state']},'preferences':preferences,'recovery':recovery}.items()}}
 c.close();return sign(o)
def main():
 OUT.mkdir(exist_ok=True)
 for full in [False,True]:
  o=make(full);(OUT/('full-v5.json' if full else 'minimal-v5.json')).write_text(json.dumps(o,ensure_ascii=False,indent=2)+'\n')
 o=make(True);o['sections']['recovery']['data']['drafts']=[raw(b'\xff\x00unknown recovery {"version":99}\n',id=1)]
 (OUT/'unknown-v5.json').write_text(json.dumps(sign(o),ensure_ascii=False,indent=2)+'\n')
 old=json.loads((ROOT/'app/src/androidTest/assets/backups/goldberg-0.9.2-format3.json').read_text())
 old.update(version=4,schema=8);old['tables']['session_piece_links']=[];old['tables']['favorite_composers']=[]
 (OUT/'historical-format4.json').write_text(json.dumps(old,ensure_ascii=False,indent=2)+'\n')
 print('Generated complete authored fixtures from Room schema10; historical inputs unchanged')
if __name__=='__main__':main()
