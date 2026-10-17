"""Execute the actual Room history SQL against SQLite, including duplicate parts and ties."""
import json
import re
import sqlite3
from pathlib import Path
root=Path(__file__).resolve().parents[1]
schema=json.loads((root/'app/schemas/com.opus.piano.data.OpusDatabase/8.json').read_text())['database']
db=sqlite3.connect(':memory:')
for entity in schema['entities']:
    db.execute(entity['createSql'].replace('${TABLE_NAME}',entity['tableName']))
source=(root/'app/src/main/java/com/opus/piano/data/PracticeData.kt').read_text()
sql=re.search(r'@Query\("""(.*?)"""\)\s*suspend fun historyPage',source,re.S).group(1)
for i in range(1,106):
    db.execute('INSERT INTO sessions(id,pieceId,pieceTitle,startedAt,completedAt,durationMillis) VALUES(?,NULL,?,1,?,60000)',(i,f'Session {i}',i//3))
    for unused in range(2):
        db.execute("INSERT INTO session_parts(sessionId,pieceId,title,composer,catalogWorkId,musicalEra,durationMillis,notes) VALUES(?,7,'Original title','Composer','work-7','',30000,'User note')",(i,))
# legacy session without parts must remain reachable
db.execute("INSERT INTO sessions(id,pieceId,pieceTitle,startedAt,completedAt,durationMillis,catalogWorkId) VALUES(106,7,'Legacy',1,36,60000,'work-7')")
for piece,catalog in [(None,None),(7,None),(None,'work-7'),(7,'work-7')]:
    seen=[]; before=2**63-1; cursor=before; pages=0
    while True:
        rows=db.execute(sql,dict(pieceId=piece,catalogId=catalog,beforeTime=before,beforeId=cursor,limit=30)).fetchall()
        if not rows: break
        pages+=1; assert pages<=5; seen.extend(r[0] for r in rows)
        # completedAt is field 4 in Room's session table.
        before,cursor=rows[-1][4],rows[-1][0]
        if pages==1:
            db.execute("INSERT OR IGNORE INTO sessions(id,pieceId,pieceTitle,startedAt,completedAt,durationMillis) VALUES(200,NULL,'New',1,999,1)")
    assert len(seen)==106 and len(set(seen))==106,(piece,catalog,seen)
    assert seen==sorted(seen,key=lambda i:(36 if i==106 else i//3,i),reverse=True)
    db.execute('DELETE FROM sessions WHERE id=200')
assert db.execute(sql,dict(pieceId=99,catalogId=None,beforeTime=2**63-1,beforeId=2**63-1,limit=30)).fetchall()==[]
print('PASS: 106 sessions across 4 pages, equal timestamps, duplicate parts, legacy attribution, catalogue filtering, newer insert and empty result')

# Untimed associations include legacy sessions once, without changing recorded totals.
db.execute("INSERT INTO session_piece_links VALUES(106,99,'Added later','Composer',NULL,NULL,'',123)")
rows=db.execute(sql,dict(pieceId=99,catalogId=None,beforeTime=2**63-1,beforeId=2**63-1,limit=30)).fetchall()
assert [row[0] for row in rows]==[106]
assert db.execute('SELECT durationMillis FROM sessions WHERE id=106').fetchone()[0]==60000
print('PASS: untimed legacy association visible once; original time unchanged')
