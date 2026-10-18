#!/usr/bin/env python3
"""Validate complete synthetic portable v5 rows against the real Room 10 export."""
import base64, hashlib, json, sqlite3, uuid
from pathlib import Path
from fixtures import canonical, digest, ROOT, OUT, SCHEMA

def check(path):
 raw=path.read_bytes();o=json.loads(raw);expected=o.pop('sha256');assert digest(o)==expected
 assert o['version']==5 and o['journalSchema']==10 and len(raw)<32*1024*1024
 s={}
 for k,v in o['sections'].items():
  assert digest(v['data'])==v['sha256'];s[k]=v['data']
 c=sqlite3.connect(':memory:');c.execute('PRAGMA foreign_keys=ON')
 entities=json.loads(SCHEMA.read_text())['database']['entities']
 for e in entities:
  c.execute(e['createSql'].replace('${TABLE_NAME}',e['tableName']))
  for i in e.get('indices',[]):c.execute(i['createSql'].replace('${TABLE_NAME}',e['tableName']))
 for section in ['journal','identities']:
  for table in [e['tableName'] for e in entities if e['tableName'] in s[section]]:
   for row in s[section][table]:c.execute(f'INSERT INTO {table} ({",".join(row)}) VALUES ({",".join("?" for _ in row)})',list(row.values()))
 for row in s['recovery']['completions']:c.execute('INSERT INTO practice_completion_evidence VALUES (?,?)',(row['token'],row['durationMillis']))
 for section,table in [('drafts','practice_drafts'),('archives','practice_recovery_archives'),('evidence','recovery_evidence')]:
  for r in s['recovery'][section]:
   b=base64.b64decode(r['rawBase64'],validate=True);assert hashlib.sha256(b).hexdigest()==r['sha256']
   if section=='drafts':c.execute('INSERT INTO practice_drafts VALUES (?,CAST(? AS TEXT),?)',(r['id'],b,r['updatedAt']))
   elif section=='archives':c.execute('INSERT INTO practice_recovery_archives VALUES (?,CAST(? AS TEXT),?,?)',(r['id'],b,r['updatedAt'],r['archivedAt']))
   else:c.execute('INSERT INTO recovery_evidence VALUES (?,?,?,?,?,?,?,?)',(r['id'],b,r['sha256'],r['updatedAt'],r['archivedAt'],r['origin'],r['classification'],r['completionToken']))
 assert not c.execute('PRAGMA foreign_key_check').fetchall()
 ids=[r['canonicalId'] for r in s['identities']['personal_catalogue_identities']];assert len(ids)==len(set(ids));assert all(str(uuid.UUID(x))==x for x in ids)
 result=dict(file=path.name,bytes=len(raw),sha256=hashlib.sha256(raw).hexdigest(),pieces=c.execute('SELECT count(*) FROM pieces').fetchone()[0],sessions=c.execute('SELECT count(*) FROM sessions').fetchone()[0],creditMillis=c.execute('SELECT coalesce(sum(durationMillis),0) FROM sessions').fetchone()[0])
 print('PASS',json.dumps(result));return result
if __name__=='__main__':
 result=[check(OUT/f) for f in ['full-v5.json','minimal-v5.json','unknown-v5.json']]
 p=Path(__file__).parent/'results';p.mkdir(exist_ok=True);(p/'fixture-sizes.json').write_text(json.dumps(result,indent=2)+'\n')
